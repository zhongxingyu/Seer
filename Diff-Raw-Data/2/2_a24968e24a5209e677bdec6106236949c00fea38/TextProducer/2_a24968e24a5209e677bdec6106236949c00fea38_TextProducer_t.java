 import org.apache.thrift.transport.TTransportException;
 
 import uk.co.fredemmott.jp.PoolException;
 import uk.co.fredemmott.jp.Producer;
 
 /**
  * @author danharvey42@gmail.com
  *
  * TextProducer is an example of using the producer help library to add a message to the pool.
  * 
  */
 public class TextProducer {
 	
 	public static void main(String[] args) throws TTransportException {
 		// Create a new producer
 		Producer<String> producer = new uk.co.fredemmott.jp.producers.TextProducer("localhost", 9090, "text");
 		
 		// Add 100 messages to the pool
		for (int i=0; i<100000000; i++) {
 			try {
 				producer.add("test message " + i);
 			} catch (PoolException e) {
 				System.out.println("Could not add message " + i + " to the pool: " + e.getMessage());
 			}
 		}
 	}
 }
