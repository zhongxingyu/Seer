 import java.io.IOException;
 import java.net.InetSocketAddress;
 import multicast.MulticastMessage;
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
 	private int port = 1337;
 	private int peers = 2;
 	private int passes = 1;
 	private ChatQueue[] queue;
 	
 	@Before
 	public void setup() {
 		queue = new ChatQueue[peers];
 		
 		for(int p=0; p<peers; p++)
 			queue[p] = new ChatQueue();
 		
 		try {
 			queue[0].createGroup(port, MulticastQueue.DeliveryGuarantee.FIFO);
 			for (int i=1; i<peers; i++) {
 				queue[i].joinGroup(port+1, new InetSocketAddress("localhost", port), MulticastQueue.DeliveryGuarantee.FIFO);
 				port++;
 			}
 			
 		} catch (IOException e) {}
 	}
 	
 	@Test
 	public void doesItWork() {
 		for (int i=0; i<passes;i++) {
 			for (int j=0; j<peers; j++)
				queue[j].put(Integer.toString(j));
 		}
 		
 		MulticastMessage[] message = new MulticastMessage[peers];
 		
 		for (int i=0; i<(passes*peers);i++) {
 			for (int j=0; j<peers; j++) {
 				message[j] = queue[j].get();
 			}
 							
 			for (int k=0; k<peers; k++) {
 				if (k+1 < message.length)
 					assertEquals(message[k].toString(), message[k+1].toString());
 			}
 			
 			message = new MulticastMessage[peers];
 		}
 	}
 }
