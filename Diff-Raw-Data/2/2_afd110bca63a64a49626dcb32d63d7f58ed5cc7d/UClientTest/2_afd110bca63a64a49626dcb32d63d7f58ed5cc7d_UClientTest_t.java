 package fr.flafla.android.urbi.test;
 
 import static fr.flafla.android.urbi.log.LoggerFactory.logger;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import fr.flafla.android.urbi.UBinary;
 import fr.flafla.android.urbi.UCallback;
 import fr.flafla.android.urbi.UClient;
 import fr.flafla.android.urbi.UMessage;
 import fr.flafla.android.urbi.log.Logger.Level;
 import fr.flafla.android.urbi.log.LoggerFactory;
 import fr.flafla.android.urbi.test.UrbiFakeServer.Message;
 
 public class UClientTest {
 	private UClient uClient;
 	private UrbiFakeServer server;
 
 	@BeforeClass
 	public static void beforeClass() {
 		LoggerFactory.defaultLevel = Level.DEBUG;
 	}
 
 	@Before
 	public void setUp() throws IOException {
 		final int port = 3000;
 		server = new UrbiFakeServer(port);
 		uClient = new UClient("localhost", port);
 	}
 
 	@After
 	public void tearDown() throws IOException, InterruptedException {
 		Thread.sleep(2000);
 		uClient.closeConnection();
 		server.stop();
 	}
 
 	@Test
 	public void testParser() throws InterruptedException, IOException {
 		server.messages.add(new Message() {
 			@Override
 			public byte[] message(String msg) {
 				logger().d("Client", "msg : " + msg);
 				msg += "\n";
 				return msg.getBytes();
 			}
 		});
 
 		final Counter counter = new Counter();
 
 		uClient.addCallback("start", new UCallback() {
 			@Override
 			public boolean handle(UMessage msg) {
 				logger().d("Client", "handle : " + msg);
 				counter.inc();
 				return true;
 			}
 		});
 
 		Thread.sleep(2000);
 		uClient.sendScript("[12538860:start] *** See http://www.urbiforge.com for news and updates.");
 		Thread.sleep(2000);
 		logger().d("Client", "counter.value = " + counter.value());
 		Assert.assertEquals(1, counter.value());
 	}
 
 	@Test
 	public void testParserBinary() throws InterruptedException, IOException {
 		server.messages.add(new Message() {
 			@Override
 			public byte[] message(String msg) {
 				logger().d("Client", "open file : " + msg);
 				try {
 					FileInputStream file = new FileInputStream(getClass().getResource(msg).getFile());
 					int length = file.available();
 					byte[] cmd = new byte[length];
 					file.read(cmd, 0, length);
 					return cmd;
 				} catch (IOException e) {
 					logger().e("Client", "Error on test.bin reading", e);
 					throw new RuntimeException(e);
 				}
 			}
 		});
 		
 		final Counter counter = new Counter();
 		final Counter counterBinary = new Counter();
 		
 		uClient.addCallback("start", new UCallback() {
 			@Override
 			public boolean handle(UMessage msg) {
 				logger().d("Client", "handle : " + msg);
 				counter.inc();
 				return true;
 			}
 		});
 		
 		uClient.addCallback("uimg", new UCallback() {
 			@Override
 			public boolean handle(UMessage msg) {
 				logger().d("Client", "handle : " + msg);
 				if (msg instanceof UBinary) {
 					counterBinary.inc();
 				}
 				return true;
 			}
 		});
 
 		Thread.sleep(2000);
 		uClient.sendScript("test.bin");
 		uClient.sendScript("img.bin");
 		uClient.sendScript("img.bin");
 		uClient.sendScript("img.bin");
 		uClient.sendScript("img.bin");
 		Thread.sleep(6000);
 		logger().d("Client", "counter.value = " + counter.value());
 		logger().d("Client", "counterBinary.value = " + counterBinary.value());
 
 		Assert.assertEquals(16, counter.value());
		Assert.assertEquals(5, counterBinary.value());
 	}
 
 }
