 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker;
 
 import java.io.File;
 import java.util.HashMap;
 import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerInterface;
 import ru.urbancamper.audiobookmarker.document.MarkedDocument;
 import ru.urbancamper.audiobookmarker.text.BookText;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 
 /**
  *
  * @author pozpl
  */
 public class AudioBookMarkerUtil {
 
     private BookText bookTextAggregator;
 
     private AudioFileRecognizerInterface audioRecognizer;
 
     public AudioBookMarkerUtil(BookText bookText, AudioFileRecognizerInterface audioRecognizer){
         this.bookTextAggregator = bookText;
         this.audioRecognizer = audioRecognizer;
     }
 
     public MarkedDocument makeMarkers(String[] audioBookFilesPaths, String fullText){
         this.bookTextAggregator.setFullText(fullText);
         HashMap<String, String> audioFilesIdentificatorMap = new HashMap<String, String>();
         String audioFilePath;
         String fileName;
         for(Integer fileCounter = 0; fileCounter < audioBookFilesPaths.length; fileCounter++){
             audioFilePath = audioBookFilesPaths[fileCounter];
             fileName = this.getAudioFileName(audioFilePath);
             audioFilesIdentificatorMap.put(fileName, fileCounter.toString());
            RecognizedTextOfSingleAudiofile recognizedFile = this.audioRecognizer.recognize(audioFilePath, audioFilePath);
             this.bookTextAggregator.registerRecognizedTextPiece(recognizedFile);
         }
         String markedText = this.bookTextAggregator.buildTextWithAudioMarks();
         MarkedDocument markedDokument = new MarkedDocument(markedText, audioFilesIdentificatorMap);
         return markedDokument;
     }
 
     private String getAudioFileName(String audioFilePath){
         String fileName;
         fileName = new File(audioFilePath).getName();
         return fileName;
     }
 
 }
