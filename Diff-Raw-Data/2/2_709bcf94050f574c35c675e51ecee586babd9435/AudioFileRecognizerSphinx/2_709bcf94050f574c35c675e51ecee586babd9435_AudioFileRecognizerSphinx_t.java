 /*
  * This class purpose is to recognize audiofile to text
  */
 package ru.urbancamper.audiobookmarker.audio;
 
 import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
 import edu.cmu.sphinx.linguist.language.grammar.TextAlignerGrammar;
 import edu.cmu.sphinx.recognizer.Recognizer;
 import edu.cmu.sphinx.result.Result;
 import edu.cmu.sphinx.util.props.ConfigurationManager;
 import edu.cmu.sphinx.util.props.PropertyException;
 import java.io.File;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 
 /**
  *
  * @author pozpl
  */
 public class AudioFileRecognizerSphinx implements AudioFileRecognizerInterface{
 
     protected final Log logger = LogFactory.getLog(getClass());
 
     private ConfigurationManager sphinxConfigurationManager;
 
     public AudioFileRecognizerSphinx(String sphinxConfigPath) {
         ConfigurationManager cm = new ConfigurationManager(sphinxConfigPath);
         this.sphinxConfigurationManager = cm;
     }
 
     public AudioFileRecognizerSphinx(ConfigurationManager sphinxConfigManager) {
         this.sphinxConfigurationManager = sphinxConfigManager;
 
     }
 
     protected String getTextFromAudioFile(String filePath, String fileUnicIdentifier){
         try {
             Recognizer recognizer = (Recognizer) this.sphinxConfigurationManager.lookup("recognizer");
             TextAlignerGrammar grammar = (TextAlignerGrammar) this.sphinxConfigurationManager.lookup("textAlignGrammar");
             recognizer.addResultListener(grammar);
 
             /*
              * allocate the resource necessary for the recognizer
              */
             recognizer.allocate();
 
             // configure the audio input for the recognizer
             AudioFileDataSource dataSource = (AudioFileDataSource) sphinxConfigurationManager.lookup("audioFileDataSource");
 
             dataSource.setAudioFile(new File(filePath), filePath);//setAudioFile(filePath, null);
 
             Result result;
             String resultTextAggregated = "";
             StringBuilder resultTextAggregator = new StringBuilder();
             result = recognizer.recognize();
             while ((result) != null) {
                 String resultText = result.getTimedBestResult(false, true);
                 resultTextAggregator.append(resultText);
                 logger.info("Recognized text: " + resultText);
 
                 result = recognizer.recognize(); //get next chunk of text
             }
             resultTextAggregated = resultTextAggregator.toString();
             return resultTextAggregated;
         } catch (PropertyException ex) {
 //            Logger.getLogger(AudioFileRecognizerSphinx.class.getName()).log(Level.SEVERE, null, ex);
             logger.error("Excsption during file recognition: " + ex);
         }
         return "";
     }
 
     public RecognizedTextOfSingleAudiofile recognize(String filePath, String fileUnicIdentifier) {
         String resultTextAggregated = getTextFromAudioFile(filePath, fileUnicIdentifier);
 
         RecognizedTextOfSingleAudiofile recognizedTextObj = new RecognizedTextOfSingleAudiofile(resultTextAggregated, fileUnicIdentifier);
 
         return recognizedTextObj;
 
 
     }
 
 }
