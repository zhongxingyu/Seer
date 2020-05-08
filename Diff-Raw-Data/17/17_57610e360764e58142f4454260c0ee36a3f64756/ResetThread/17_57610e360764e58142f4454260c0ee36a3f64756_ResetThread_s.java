 package ui;
 
 public class ResetThread extends Thread {
 	private Main frame;
 
 	public ResetThread(Main frame) {
 		this.frame = frame;
 	}
 
 	@Override
 	public void run() {
 		Main main = new Main();
 		main.hostRegistry = frame.hostRegistry;
 		main.clientRegistry = frame.clientRegistry;
 	}
 }
