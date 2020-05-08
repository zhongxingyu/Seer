 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.context;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import opennlp.tools.tokenize.TokenizerModel;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import ru.urbancamper.audiobookmarker.text.LanguageModelBasedTextTokenizez;
 /**
  *
  * @author pozpl
  */
 @Configuration
 public class BeansAnnotations {
 
    private String TOKENIZER_MODEL_PATH = "resources/tokenizer_models/en-token.bin";
    private String DETOKENIZER_DICTONARY_PATH = "resources/tokenizer_models/en-detokenizer.xml";
     @Bean
     public TokenizerModel tokenizerModel(){
         InputStream modelPathInputStream = null;
         TokenizerModel tokenizerModel = null;
         try {
            File modelFile = new File(TOKENIZER_MODEL_PATH);
             modelPathInputStream = new FileInputStream(modelFile.getAbsolutePath());
             tokenizerModel = new TokenizerModel(modelPathInputStream);
         } catch (IOException ex) {
             Logger.getLogger(LanguageModelBasedTextTokenizez.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 modelPathInputStream.close();
             } catch (IOException ex) {
                 Logger.getLogger(LanguageModelBasedTextTokenizez.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return tokenizerModel;
     }
 
     @Bean
     public LanguageModelBasedTextTokenizez textTokenizer(){
        return new LanguageModelBasedTextTokenizez(tokenizerModel(), this.DETOKENIZER_DICTONARY_PATH);
     }
 }
