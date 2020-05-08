 package ui;
 
 public class ResetThread extends Thread {
 	private Main frame;
 
 	public ResetThread(Main frame) {
 		this.frame = frame;
 	}
 
 	@Override
 	public void run() {
		try {
			if (frame.hostRegistry != null)
				frame.hostRegistry.unbind("reversi");
			if (frame.clientRegistry != null)
				frame.clientRegistry.unbind("reversi");
		} catch (Exception e) {
		}
 		Main main = new Main();
 		main.hostRegistry = frame.hostRegistry;
 		main.clientRegistry = frame.clientRegistry;
 	}
 }
