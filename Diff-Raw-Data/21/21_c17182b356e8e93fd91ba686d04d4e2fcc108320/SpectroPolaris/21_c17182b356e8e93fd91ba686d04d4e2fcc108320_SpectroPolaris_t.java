 import java.util.Timer;
 
 public class SpectroPolaris 
 {
 	private static Frame s_frame;
 	private static Server s_server;
 	
 	private static boolean s_paused;
 	
 	public SpectroPolaris() {
 		
 	}
 	
 	public static Frame frame() {
 		return s_frame;
 	}
 	
 	public static Server server() {
 		return s_server;
 	}
 	
 	public static void setPaused(boolean paused) {
 		s_paused = paused;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		
 		
 		s_server = new Server(1337);
 		s_server.start();
 		
		s_frame = new Frame();
		
 		s_paused = true;
 		
 		while(s_frame.isVisible())
 		{			
 			if(s_paused == false) {
 				s_frame.gamePanel().model().step();
 			
 				s_frame.gamePanel().repaint();
 			}
 			
 			try {
 				Thread.sleep(33);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static boolean paused() {
 		return s_paused;
 	}
 
 }
