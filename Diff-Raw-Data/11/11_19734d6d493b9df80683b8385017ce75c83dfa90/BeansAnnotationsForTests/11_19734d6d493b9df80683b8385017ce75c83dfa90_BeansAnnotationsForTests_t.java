 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.context;
 
 import edu.cmu.sphinx.util.props.ConfigurationManager;
 import edu.cmu.sphinx.util.props.PropertyException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import opennlp.tools.tokenize.TokenizerModel;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.context.annotation.Scope;
 import org.springframework.core.env.Environment;
 import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerSphinx;
 import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerSphinxCached;
 import ru.urbancamper.audiobookmarker.audio.AudioFileRecognizerStub;
import ru.urbancamper.audiobookmarker.text.BitapSubtextFinding;
 import ru.urbancamper.audiobookmarker.text.BookText;
 import ru.urbancamper.audiobookmarker.text.LanguageModelBasedTextTokenizer;
 import ru.urbancamper.audiobookmarker.text.LongestSubsequenceFinder;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextSnippetInterval;
 import ru.urbancamper.audiobookmarker.text.WordsToNumsMap;
 
 /**
  *
  * @author pozpl
  */
 @Configuration
 @PropertySource("classpath:test.properties")
 public class BeansAnnotationsForTests {
     @Autowired
     private Environment env;
 
     @Bean
     public TokenizerModel tokenizerModel() {
         InputStream modelPathInputStream = null;
         TokenizerModel tokenizerModel = null;
         try {
             File modelFile = new File(env.getProperty("TOKENIZER_MODEL_PATH"));//TOKENIZER_MODEL_PATH);
             modelPathInputStream = new FileInputStream(modelFile.getAbsolutePath());
             tokenizerModel = new TokenizerModel(modelPathInputStream);
         } catch (IOException ex) {
             Logger.getLogger(LanguageModelBasedTextTokenizer.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 modelPathInputStream.close();
             } catch (IOException ex) {
                 Logger.getLogger(LanguageModelBasedTextTokenizer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return tokenizerModel;
     }
 
     @Bean
     public LanguageModelBasedTextTokenizer textTokenizer() {
         return new LanguageModelBasedTextTokenizer(tokenizerModel(), env.getProperty("DETOKENIZER_DICTONARY_PATH"));
     }
 
     @Bean
     public WordsToNumsMap wordsToNumMap() {
         return new WordsToNumsMap();
     }
 
     @Bean
     public LongestSubsequenceFinder longestSubsequenceFinder() {
         return new LongestSubsequenceFinder();
     }
 
     @Bean
     @Scope("singleton")
     public RecognizedTextSnippetInterval recognizedTextSnippetInterval(){
         return new RecognizedTextSnippetInterval();
     }
 
     @Bean
    @Scope("singleton")
    public BitapSubtextFinding bitapSubtextFinding(){
        return new BitapSubtextFinding();
    }

    @Bean
     @Scope("prototype")
     public BookText bookText() {
        return new BookText(this.textTokenizer(), this.wordsToNumMap(), this.longestSubsequenceFinder(), this.bitapSubtextFinding());
     }
 
     @Bean
     public ConfigurationManager configurationManager(){
         try {
             URL configURL = new URL(env.getProperty("SPHINX_CONFIG_PATH"));
             try {
                 return new ConfigurationManager(configURL);
             } catch (PropertyException ex) {
                 Logger.getLogger(BeansAnnotationsForTests.class.getName()).log(Level.SEVERE, null, ex);
             }
         } catch (MalformedURLException ex) {
             Logger.getLogger(BeansAnnotationsForTests.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
 
  @Bean
  public AudioFileRecognizerSphinx audioFileRecognozerSphinx(){
      AudioFileRecognizerSphinx sphin4Instance = new AudioFileRecognizerSphinx(configurationManager());
      return sphin4Instance;
  }
 
  @Bean
  public AudioFileRecognizerSphinxCached audioFileRecognozerSphinxCached(){
      AudioFileRecognizerSphinxCached sphin4Instance = new AudioFileRecognizerSphinxCached(audioFileRecognizerStub());
      return sphin4Instance;
  }
 
 
 
  @Bean
  public AudioFileRecognizerStub audioFileRecognizerStub(){
      return new AudioFileRecognizerStub(null);
  }
 
 }
