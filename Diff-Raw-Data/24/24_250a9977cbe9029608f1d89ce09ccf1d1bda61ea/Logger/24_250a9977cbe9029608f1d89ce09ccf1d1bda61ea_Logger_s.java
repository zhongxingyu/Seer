 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.Callable;
 
 
 public class Logger implements Callable<Void> {
 
 	private final Queue<String> messages = new LinkedList<>();
 
 	@Override
 	public Void call() throws Exception {
		// entry point for logger thread:
		//   - loop forever
		//   - pop messages off the queue and print them when they are available
		return null;
 	}
 
 	public void log(String message) {
		// - insert a message in the queue
 	}
 }
