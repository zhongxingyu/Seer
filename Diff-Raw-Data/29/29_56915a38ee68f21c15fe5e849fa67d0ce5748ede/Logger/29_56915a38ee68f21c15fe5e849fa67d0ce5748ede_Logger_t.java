 package training.concurrency;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 public class Logger implements Runnable {
 
 	private final Queue<String> messages = new LinkedList<String>();
 
 	public void log(String message) {
		synchronized (this) {
			messages.add(message);
			this.notify();
		}
 	}
 
 	@Override
 	public void run() {
		synchronized (this) {
			while (true) {
				if (messages.isEmpty()) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				} else {
					System.out.println(messages.remove());
				}
 			}
 		}
 	}
 
 }
