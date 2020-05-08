 package src;
 
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import src.ui.TitleScreen;
 
 /**
  * The main entry point for the entire application.  Displays the main window, and provides simple
  * utilities for changing which screen is currently displayed in this window.
  */
 public class GameMain extends JFrame {
 	private static final long serialVersionUID = 1L;	
 	
 	private JPanel mainPanel;
 	private static final GameMain main = new GameMain();
 
 	public GameMain() {
 		// set up window
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(800, 600); 
 		setResizable(false);
 		
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new CardLayout());
 		getContentPane().add(mainPanel);
 	}
 
 	/**
 	 * The main entry point for our program
 	 */
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				TitleScreen title = new TitleScreen(main);
 				main.showScreen(title);
 				main.setVisible(true);
 
 				// refresh the window at about 30 fps
 				Timer t = new Timer(33, new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						main.repaint();
 					}
 				});
 
 				t.start();
 			}
 		});
 	}
 	
 	/**
 	 * Shows a given screen as the content of the main window.
 	 * @param screen A JPanel representing the main screen.
 	 */
 	public void showScreen(JPanel screen) {
 		mainPanel.removeAll();
 		mainPanel.add(screen, "visible-panel");
 		validate();
 	}
 }
