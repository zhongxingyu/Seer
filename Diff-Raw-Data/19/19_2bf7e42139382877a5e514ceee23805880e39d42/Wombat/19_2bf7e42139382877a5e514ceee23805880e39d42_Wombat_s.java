 package wombat;
 
 import gui.MainFrame;
 
 /**
  * Main entry point of the program.
  */
 public class Wombat {
	public static final String VERSION = "{VERSION}";
 
	public static void main(String[] argv) {
		new Wombat();
	}
 
	public Wombat() {
		MainFrame main = new MainFrame();
		main.setVisible(true);
	}
 }
