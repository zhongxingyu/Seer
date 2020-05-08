 import java.io.IOException;
 import java.net.InetAddress;
 
 import nl.tudelft.ewi.in4150.group18.ChandyLamportGlobalStateAlgorithm;
 import nl.tudelft.in4150.group18.NodeController;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AlgorithmTest {
 
 	private NodeController c1, c2;
 	private ChandyLamportGlobalStateAlgorithm a1, a2;
 
 	@Before
 	public void setUp() throws IOException {
 		a1 = new ChandyLamportGlobalStateAlgorithm();
 		a2 = new ChandyLamportGlobalStateAlgorithm();
 
 		InetAddress localHost = InetAddress.getLocalHost();
 		c1 = new NodeController(localHost, true, a1);
 		c2 = new NodeController(localHost, true, a2);
 
 		// fully connected network
 		c1.addRemote(c2.getLocalAddress());
 		c2.addRemote(c1.getLocalAddress());
 	}
 
 	@Test
 	public void test() throws InterruptedException {
 		a1.start();
 		Thread.sleep(5);
 		a2.start();
 
 		Thread.sleep(200);
 		
 		a1.captureState();
		Thread.sleep(20000);
 	}
 
 }
