 package cs.dsn.dwc.client;
 
 import cs.dsn.dwc.server.CountServer;
 import cs.dsn.dwc.swp.client.MalformedResponseException;
import cs.dsn.dwc.wordcounter.WordCountMap;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  * Tests a count client
  */
 public class CountServerConnectionTest {
   @Test
   public void testCount() throws IOException, MalformedResponseException, InterruptedException {
     int port = 45678;
     CountServer server = new CountServer(port);
     Thread thread = new Thread(server);
     thread.start();
     Thread.sleep(1000);
 
     InetAddress serverAddress = InetAddress.getLocalHost();
     CountServerConnection client = CountServerConnection.connect(serverAddress, port);
     WordCountMap counts = client.count("There are two pens on the table, one is red and the other is black.");
     server.stop();
 
     Set<String> expectedWords = new HashSet<String>(Arrays.asList(
             "there", "are", "two", "pens", "on", "the", "table", "one", "is", "red", "and", "other", "black"
     ));
 
     assertEquals(expectedWords, counts.words());
 
     assertEquals(counts.get("There"), 1);
     assertEquals(counts.get("Are"), 1);
     assertEquals(counts.get("Two"), 1);
     assertEquals(counts.get("Pens"), 1);
     assertEquals(counts.get("On"), 1);
     assertEquals(counts.get("The"), 2);
     assertEquals(counts.get("Table"), 1);
     assertEquals(counts.get("One"), 1);
     assertEquals(counts.get("Is"), 2);
     assertEquals(counts.get("Red"), 1);
     assertEquals(counts.get("And"), 1);
     assertEquals(counts.get("Other"), 1);
     assertEquals(counts.get("Black"), 1);
   }
 }
