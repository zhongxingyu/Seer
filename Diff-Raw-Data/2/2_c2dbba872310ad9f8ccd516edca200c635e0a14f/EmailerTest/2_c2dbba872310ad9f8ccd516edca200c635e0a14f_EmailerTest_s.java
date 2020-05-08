 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import static org.junit.Assert.assertTrue;
 
 public class EmailerTest {
     private Emailer emailer;
     private final String[] TO = new String[]{"toEmail1@example.com"};
     private final String FROM = "fromEmail@example.com";
 
 
     @Before
     public void setUp() {
         emailer = new Emailer();
		//this assumes localhost is setup as an smtp server
         emailer.setHost("localhost");
     }
 
     @Test
     public void testSendWithoutAttachment() {
         assertTrue(emailer.sendEmail(TO, FROM, "test subject", "test body"));
     }
 
     @Test
     public void testSendWithAttachment() throws IOException {
         File testFile = new File("test.txt");
         BufferedWriter output = new BufferedWriter(new FileWriter(testFile));
         output.write("This is a test.");
         output.close();
         assertTrue(emailer.sendEmail(TO, FROM, "test subject", "test body", testFile));
         testFile.delete();
     }
 }
