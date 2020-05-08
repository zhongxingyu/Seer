 package gui;
 
import settings.Settings;

 public class Main {
 
 	public final static InputHandler inputHandler = new InputHandler();
 	
 	public static void main(String[] args) {
		Settings.init();
 		MainFrame mf = new MainFrame();
 	}
 }
