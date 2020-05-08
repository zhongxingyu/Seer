 import javax.swing.*;
 import java.awt.*;
 
 
 public class GUI extends JFrame {
 	private static final long serialVersionUID = -934931618056969704L;
 	private static Color titleColor;
	
 	/* Applies our could scheme to a swing component */
 	public static void colorize(JComponent component) {
 		if (component instanceof JButton) {
 			component.setBackground(Color.DARK_GRAY);
 			component.setForeground(Color.WHITE);
 		} else {
 			component.setBackground(Color.BLACK);
 			component.setForeground(GUI.titleColor());
 		}
 	}
	
 	/* Returns the color used in the title image */
 	public static Color titleColor() {
 		if (titleColor == null)
 			titleColor =  new Color(0x22, 0xb1, 0x4c);
		
 		return titleColor;
 	}
	
	
 	//These are the fields for the GUI class
 	private ScenePanel scene;
 	private ScorePanel score;
 	private JPanel banner;
 
 	public GUI() {
 		// This the title that shows in the main window
 		setTitle("Asteroids!");
 
 		// This means the program stops at the close of the main window.
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Create a BorderLayout manager for the panels
 		setLayout(new BorderLayout());
 
 		scene = new ScenePanel();
 		score = new ScorePanel();
 		banner = new BannerPanel();
		JPanel blackSpace = new JPanel();
		GUI.colorize(blackSpace);

 
 		// Add them to the content pane and put where you want them.
 		add(banner, BorderLayout.NORTH);
 		add(scene, BorderLayout.CENTER);
 		add(score, BorderLayout.SOUTH);
		add(blackSpace, BorderLayout.EAST);
		add(blackSpace, BorderLayout.WEST);
 
 		// Pack the contents of the window and display it.
 		pack();
 		//Initially it is not going to be visible
 		setVisible(false);
 	}
	
 	public ScorePanel getScore() {
 		return score;
 	}
 }
