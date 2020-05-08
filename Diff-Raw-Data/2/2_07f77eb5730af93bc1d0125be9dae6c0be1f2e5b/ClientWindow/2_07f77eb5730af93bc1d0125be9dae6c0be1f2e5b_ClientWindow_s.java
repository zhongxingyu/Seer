 package elfville.client;
 
 import javax.swing.*;
 
 import elfville.client.views.WelcomeScreen;
 
 public class ClientWindow extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	// TODO - remove global variable, or at least find a way to make it final
 	public static ClientWindow window;
 	
 	//private final JPanel navigation;
 	private JPanel current;
 
 
 	public static void switchScreen(JPanel next) {
 		//window.getContentPane().remove(window.current);
 		window.getContentPane().removeAll();
 		window.getContentPane().add(next);
 		window.current = next;
		window.revalidate();
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public ClientWindow() {
 		super("ElfVille");
 		setBounds(100, 100, 800, 600);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//TODO: consider adding navigation panel
 		current = new WelcomeScreen(); 
 		
 		this.getContentPane().add(current);
 		window = this;
 	}
 
 	/**
 	 * Used when a socket error occurs. Shows an alert dialog.
 	 */
 	public static void showConnectionError() {
 		showError("Socket connection broke. Try again.",
 				"Connection error");
 		System.exit(-1);
 	}
 	
 	public static void showError(String msg, String title) {
 		JOptionPane.showMessageDialog(null, msg,
 				title, JOptionPane.ERROR_MESSAGE);
 	}
 
 }
