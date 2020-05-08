 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.Callable;
 
 
 public class Logger implements Callable<Void> {
 
 	private final Queue<String> messages = new LinkedList<>();
 
 	@Override
 	public Void call() throws Exception {
		while (true) {
			if (!messages.isEmpty()) {
				System.out.println(messages.remove());
			}
			Thread.yield();
		}
 	}
 
 	public void log(String message) {
		messages.add(message);
 	}
 }
