 package client;
 
 public class SendThread extends Thread {
 	
 	private DrawingMonitor monitor;
 	
 	public SendThread(DrawingMonitor monitor) {
 		this.monitor = monitor;
 	}
 	
 	public void run() {
 		while (true) {
			System.out.println("Sending cmd");
 			monitor.send();
 		}
 	}
 
 }
