 package xhl.core;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import xhl.core.elements.CodeList;
 import xhl.core.exceptions.EvaluationException;
 
 /**
  * LanguageProcessor, which reads code from the file and outputs error messages
  * to the standard error output.
  *
  * @author Sergej Chodarev
  */
 public class ConsoleLanguageProcessor extends LanguageProcessor {
 
     public ConsoleLanguageProcessor(Language lang) {
         super(lang);
     }
 
     public void process(String filename) throws FileNotFoundException,
             IOException {
         Reader reader = new Reader();
         CodeList program = reader.read(new FileReader(filename));
         try {
             execute(program);
         } catch (EvaluationException e) {
            System.err.printf("%s: %s\n", e.getPosition(), e);
         }
     }
 }
