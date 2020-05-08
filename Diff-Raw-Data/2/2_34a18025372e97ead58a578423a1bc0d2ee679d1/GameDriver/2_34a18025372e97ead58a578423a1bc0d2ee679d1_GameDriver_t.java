 import javax.swing.*;
 
 public class GameDriver {
 	
 	private static void createAndShowUI() {
		// create the model/view/control and connect them together
 		GameModel model = new GameModel();
 		GameView view = new GameView(model);
 		GameControl control = new GameControl(model);
 		GameMenu menu = new GameMenu(control);
 		
 		view.setGuiControl(control);
 		
 		// create the GUI to display the view
 		JFrame frame = new JFrame("Hollywood Squares");
 		frame.getContentPane().add(view.getMainPanel()); 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setJMenuBar(menu.getMenuBar()); 
 		frame.setSize(945,700);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	// call Swing code in a thread-safe manner per the tutorials
 	public static void main(String[] args) {
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				createAndShowUI();
 		  	}
 		});
 	}
 }
