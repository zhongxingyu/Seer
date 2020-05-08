 package wombat;
 
 import gui.MainFrame;
 
 /**
  * Main entry point of the program.
  */
 public class Wombat {
	public static final String VERSION = "1.16.6";
 	public static boolean AllowUpdate = false;
 	
 	public static void main(String[] argv) {
 		System.setSecurityManager(null);
 		new Wombat();
 	}
 
 	public Wombat() {
 		MainFrame main = new MainFrame();
 		main.setVisible(true);
 	}
 }
