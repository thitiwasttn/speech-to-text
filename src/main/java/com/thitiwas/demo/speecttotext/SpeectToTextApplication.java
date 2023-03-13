package com.thitiwas.demo.speecttotext;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.threeten.bp.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@Slf4j
public class SpeectToTextApplication {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(SpeectToTextApplication.class, args);
//        shortFile();
//        longFile();
        asyncRecognizeWords();
    }


    private static void longFile() throws Exception {
//        String file = "C:\\Users\\Admin\\Downloads\\thai2.wav";
        String file = "C:\\Users\\Admin\\Downloads\\thai2_1.wav";
        // Configure polling algorithm
        SpeechSettings.Builder speechSettings = SpeechSettings.newBuilder();
        TimedRetryAlgorithm timedRetryAlgorithm =
                OperationTimedPollAlgorithm.create(
                        RetrySettings.newBuilder()
                                .setInitialRetryDelay(Duration.ofMillis(500L))
                                .setRetryDelayMultiplier(1.5)
                                .setMaxRetryDelay(Duration.ofMillis(5000L))
                                .setInitialRpcTimeout(Duration.ZERO) // ignored
                                .setRpcTimeoutMultiplier(1.0) // ignored
                                .setMaxRpcTimeout(Duration.ZERO) // ignored
                                .setTotalTimeout(Duration.ofHours(24L)) // set polling timeout to 24 hours
                                .build());
        speechSettings.longRunningRecognizeOperationSettings().setPollingAlgorithm(timedRetryAlgorithm);

        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create(speechSettings.build())) {

            // Configure remote file request for FLAC
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setLanguageCode("th-TH")
                            .build();
//            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();
            ByteString bytes = ByteString.copyFrom(Files.readAllBytes(Paths.get(file)));
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(bytes).build();
            // Use non-blocking call for getting file transcription
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speech.longRunningRecognizeAsync(config, audio);
            while (!response.isDone()) {
                System.out.println("Waiting for response...");
                Thread.sleep(10000);
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                log.info("spoke : {}", alternative.getTranscript());
            }
        }
    }

    public static void asyncRecognizeWords() throws Exception {
        String file = "E:\\videos\\teacher\\output_001.wav";
        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create()) {

            // Configure remote file request for FLAC
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setLanguageCode("th-TH")
                            .setEnableWordTimeOffsets(true)
                            .build();
            ByteString bytes = ByteString.copyFrom(Files.readAllBytes(Paths.get(file)));
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(bytes).build();

            // Use non-blocking call for getting file transcription
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speech.longRunningRecognizeAsync(config, audio);
            while (!response.isDone()) {
                System.out.println("Waiting for response...");
                Thread.sleep(10000);
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                log.info("spoke : {}", alternative.getTranscript());
                for (WordInfo wordInfo : alternative.getWordsList()) {
                    System.out.println(wordInfo.getWord());
                    log.info("wordInfo.getStartTime().getSeconds() :{}\n wordInfo.getStartTime().getNanos() / 100000000:{}\nwordInfo.getEndTime().getSeconds() :{}\n wordInfo.getEndTime().getNanos() / 100000000:{}",
                            wordInfo.getStartTime().getSeconds(),
                            wordInfo.getStartTime().getNanos() / 100000000,
                            wordInfo.getEndTime().getSeconds(),
                            wordInfo.getEndTime().getNanos() / 100000000);
                }
            }
        }
    }

    private static void shortFile() throws Exception {
        log.info("hello world");

        String googleApplicationCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        log.info("googleApplicationCredentials :{}", googleApplicationCredentials);

        String javaHome = System.getenv("JAVA_HOME");


        try (SpeechClient speechClient = SpeechClient.create()) {

            // The path to the audio file to transcribe
            String file = "C:\\Users\\Admin\\Downloads\\thai2_1.wav";
//            String file = "C:\\Users\\Admin\\Downloads\\test1.wav";

            // Builds the sync recognize request
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setLanguageCode("th-TH")
                            .build();
            ByteString bytes = ByteString.copyFrom(Files.readAllBytes(Paths.get(file)));
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(bytes).build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                log.info("spoke : {}", alternative.getTranscript());
                for (WordInfo wordInfo : alternative.getWordsList()) {
                    log.info("word :{}", wordInfo.getWord());
                    System.out.printf(
                            "\t%s.%s sec - %s.%s sec\n",
                            wordInfo.getStartTime().getSeconds(),
                            wordInfo.getStartTime().getNanos() / 100000000,
                            wordInfo.getEndTime().getSeconds(),
                            wordInfo.getEndTime().getNanos() / 100000000);
                    log.info("wordInfo.getStartTime().getSeconds() :{}\n wordInfo.getStartTime().getNanos() / 100000000:{}\nwordInfo.getEndTime().getSeconds() :{}\n wordInfo.getEndTime().getNanos() / 100000000:{}",
                            wordInfo.getStartTime().getSeconds(),
                            wordInfo.getStartTime().getNanos() / 100000000,
                            wordInfo.getEndTime().getSeconds(),
                            wordInfo.getEndTime().getNanos() / 100000000);
                }
            }
        }


    }

}
