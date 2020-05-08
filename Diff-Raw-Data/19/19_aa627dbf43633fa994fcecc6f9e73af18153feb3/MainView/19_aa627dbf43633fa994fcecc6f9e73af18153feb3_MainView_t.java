package view;
 
 import java.awt.Rectangle;
 
 import javax.swing.JFrame;
 
import controller.GameKeyboardListener;
 
 /**
  * The main window inside which states are rendered
  */
 
 @SuppressWarnings("serial")
 public class MainView extends JFrame {
 	public MainView() {
 		super("Space Invaders");
 		this.setBounds(new Rectangle(0, 0, 300, 300));
 		this.setResizable(false);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.addKeyListener(new GameKeyboardListener());
 	}
 }
