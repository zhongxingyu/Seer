 package demo.vmware.commands;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Scanner;
 
 import org.junit.Test;
 
 public class CommandProcessorTest {
 
     /**
      * 
      */
     @Test
     public void testScanner() {
         CommandProcessor jig = new CommandProcessor(null);
         // can't be null since it's a constructor
         assertNotNull(jig);
        InputStream stream = new ByteArrayInputStream("0 cow \"my dog\" cat".getBytes());
         Scanner scanJig = jig.createScanner(stream);
        String token;
         assertEquals("0", jig.getNextToken(scanJig));
         assertEquals("cow", jig.getNextToken(scanJig));
         assertEquals("my dog", jig.getNextToken(scanJig));
         assertEquals("cat", jig.getNextToken(scanJig));
     }
 }
