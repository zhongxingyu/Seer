 package gui;
 
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 public class MainPanel {
 
 	private static Boolean isRunning = true;
 	private static LoginPanel pane;
 	
 	public static void main(String args[]) {
 		JFrame frame = new JFrame("Main");
 		MainPanel mainPanel = new MainPanel();
 		
 		try {
 			startLogIn(frame, mainPanel);
 		} 
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	private static void startLogIn(JFrame frame, MainPanel mainPanel) throws IOException, InterruptedException {
 		pane = new LoginPanel(mainPanel);
 		frame.getContentPane().add(pane);
 		frame.pack();
 		frame.setVisible(true);
 		frame.setResizable(false);
 	}
 	
 	private Boolean waitForLogIn() {
 		
 		return null;
 	}
 	
 	public void loginApproved() {
 		pane.removeAll();
 		pane.updateUI();
 		//constructCalender();
 	}
 	
 	private void removeAll() {
 		removeAll();
 	}
 	
 }
