 import java.io.IOException;
 import java.net.InetSocketAddress;
 import multicast.MulticastQueue;
 import org.junit.*;
 import static org.junit.Assert.*;
 import week4.*;
 import week4.multicast.*;
 
 /**
  *
  * @author Martin
  */
 public class TotallyOrderedMultiCastStressTest {
 	private ChatQueue q1;
 	private ChatQueue q2;
 	private ChatQueue q3;
 	private ChatQueue q4;
 	private int port = 1337;
 	
 	@Before
 	public void setup() {
 		q1 = new ChatQueue();
 		q2 = new ChatQueue();
 		q3 = new ChatQueue();
 		q4 = new ChatQueue();
 		
 		try {
 			q1.createGroup(port, MulticastQueue.DeliveryGuarantee.FIFO);
 			q2.joinGroup(port+1, new InetSocketAddress("localhost", port), MulticastQueue.DeliveryGuarantee.FIFO);
 			q3.joinGroup(port+2, new InetSocketAddress("localhost", port+1), MulticastQueue.DeliveryGuarantee.FIFO);
 			q4.joinGroup(port+3, new InetSocketAddress("localhost", port+2), MulticastQueue.DeliveryGuarantee.FIFO);
 			
 		} catch (IOException e) {}
 	}
 	
 	@Test
 	public void doesItWork() {
 		int x=5;
 		for (int i=0; i<x;i++) {
 			q1.put("test1");
 			q2.put("test2");
 			q3.put("test3");
 			q4.put("test4");
 		}
 		
 		for (int i=0; i<(x*4);i++) {
			assertEquals(q1.get(), q2.get());
			assertEquals(q2.get(), q3.get());
			assertEquals(q3.get(), q4.get());
 		}
 	}
 }
