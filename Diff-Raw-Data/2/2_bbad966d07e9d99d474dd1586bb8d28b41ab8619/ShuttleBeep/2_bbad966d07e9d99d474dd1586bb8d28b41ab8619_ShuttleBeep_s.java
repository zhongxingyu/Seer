 package test1;
 
 
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 
 
 public class ShuttleBeep {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(true);
 		frame.setTitle("ShuttleBeep");
 		
 		/*// Set to the middle of the screen
 		Toolkit toolk = Toolkit.getDefaultToolkit(); 
 		Dimension screenSize = tk.getScreenSize();
 		int screenHeight = (screenSize.height/2);
 		int screenWidth = (screenSize.width/2);
 		*/
 		
 		//Create the menu bar and its submenus
 		
 		
 		frame.setSize(631, 600);
		
 		Logic logic = new Logic();
 		frame.setJMenuBar(logic.bar);
 		frame.add(logic.southJPanel, BorderLayout.PAGE_END);
 		frame.add(logic);
 		frame.setVisible(true);
 
 	}
 
 }
