 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import junit.framework.TestCase;
 import multicast.MulticastMessage;
 import multicast.MulticastQueue;
 import org.junit.*;
 import static org.junit.Assert.*;
 import week4.*;
 import week4.multicast.*;
 import week4.multicast.messages.AbstractLamportMessage;
 
 /**
  *
  * @author Martin
  */
public class TotallyOrderedMultiCastStressTest extends TestCase {
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
 		try{
 			Thread.sleep(2000);
 		}catch(InterruptedException e){
 			System.err.println("Interrupted...");
 			return;
 		}
 		for (int i=0; i<passes;i++) {
 			for (int j=0; j<peers; j++)
 				queue[j].put(Integer.toString(j));
 		}
 		
 		AbstractLamportMessage[] message = new AbstractLamportMessage[peers];
 		
 		for (int i=0; i<(passes*peers);i++) {
 			for (int j=0; j<peers; j++) {
 				message[j] = queue[j].get();
 					while (!queue[j].shouldHandleMessage(message[j]))
 						message[j] = queue[j].get();
 			}
 							
 			for (int k=0; k<peers; k++) {
 				if (k+1 < message.length)
 					assertEquals(message[k].toString(), message[k+1].toString());
 			}
 			
 			message = new AbstractLamportMessage[peers];
 		}
 	}
 }
