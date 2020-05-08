 package unittest.network;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.ConnectException;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import enduro.network.client.EnduroClient;
 import enduro.network.server.EnduroServer;
 
 public class NewNetworkTest {
 
 	private static EnduroServer server;
 	private static PrintStream oldstream;
 	private static PVGStream str;
 	@BeforeClass public static void setupServer() {
 		str = new PVGStream(System.out);
 		oldstream = System.out;
 		System.setOut(str);
 		server = new EnduroServer(1338);
 		
 		new Thread(server).start();
 		
 		
 	}
 	@Before public void resetCrap() {
 		try {
 			Thread.sleep(200);
 		} catch (InterruptedException e) {}
 		
 		str.reset();
 		System.setOut(str);
 	}
 	@Test public void connectAndDisconnect() throws ConnectException {
 		try {
 			Thread.sleep(1000);
 		} catch(Exception E) {}
		EnduroClient client = new EnduroClient("127.0.0.1", 1338, "Start", "starttider.txt");
 		client.shutDown();
 		System.setOut(oldstream);
 		
 		String[] lines = str.getoutput().split("\n");
 
 		/*for(String line: lines) {
 			System.out.println("test connect line: " + line);
 		}*/
 		
 		assertEquals("Client is handling Start", lines[0]);
 		assertEquals("Client: quit", lines[1]);
 		assertEquals("Goodbye!", lines[2]);
 	}
 	
 	@Test public void sendLine() throws ConnectException {
 		try {
 			Thread.sleep(1000);
 		} catch(Exception E) {}
		EnduroClient client = new EnduroClient("127.0.0.1", 1338, "Start", "starttider.txt");
 		client.registerLine("1;00.00.00");
 		client.shutDown();
 		System.setOut(oldstream);
 		
 		String[] lines = str.getoutput().split("\n");
 		
 		/*for(String line: lines) {
 			System.out.println("test send line: " + line);
 		}*/
 		
 		assertEquals("Client: 1;00.00.00", lines[1]);
	}	
 }
 
 class PVGStream extends PrintStream {
 	
 	private StringBuilder tmp = new StringBuilder();
 	
 	public PVGStream(OutputStream out) {
 		super(out);
 	}
 	
 	public void println(String x) {
 		tmp.append(x + "\n");
 	}
 	
 	public String getoutput() {
 		return tmp.toString();
 	}
 	
 	public void reset() {
 		tmp = new StringBuilder();
 	}
 	
 }
