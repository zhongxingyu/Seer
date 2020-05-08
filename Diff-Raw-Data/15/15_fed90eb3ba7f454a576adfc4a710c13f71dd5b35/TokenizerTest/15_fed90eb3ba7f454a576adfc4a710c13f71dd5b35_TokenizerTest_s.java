 package no.iterate.javazone;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
 
 import org.junit.Test;
 
 public class TokenizerTest {
     @Test
     public void tokenize() {
        String text = "This is a text. This should be, perhaps, tokenized.. Oh yeah";
         String[] tokenizedWords = Tokenizer.tokenize(text);
        System.out.println(tokenizedWords[tokenizedWords.length-1]);
         assertEquals("This", tokenizedWords[0]);
         assertEquals("yeah", tokenizedWords[tokenizedWords.length-1]);
     }
 }
