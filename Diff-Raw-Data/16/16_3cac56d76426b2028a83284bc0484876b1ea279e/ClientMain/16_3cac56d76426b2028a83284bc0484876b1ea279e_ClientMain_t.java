 
 package client;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import client.gui.panels.LoginPanel;
 import client.gui.panels.MainPanel;
 import client.model.ActiveUserModel;
 
 /**
  * Main entry point for the client
  * 
  * @author Magne
  *
  */
 @SuppressWarnings("serial")
 public class ClientMain extends JFrame implements IServerConnectionListener{
 
 	private LoginPanel loginPanel;
 	private MainPanel  mainPanel;
 	
 	private JPanel contentPane;
 	private static ClientMain client;
 	private static ActiveUserModel activeUser;
 	
 	public ClientMain() {
 		super("Kalender");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		contentPane = new JPanel(new BorderLayout());
 		this.setContentPane(contentPane);
 		
 		loginPanel = new LoginPanel();
 		
 		contentPane.add(loginPanel,BorderLayout.CENTER);
 		this.pack();
 		centerOnScreen();
 		this.setResizable(false);
 		
 		ServerConnection.addServerConnectionListener(this);
 		
 		this.setVisible(true);
 	}
 	
 
 	@Override
 	public void serverConnectionChange(String change) {
 		if (change == IServerConnectionListener.LOGIN) {
 			this.setResizable(true);
 			contentPane.remove(loginPanel);
 			
 			ClientMain.getActiveUser().getCalendarModel().requestDefaultBuffer();
 			mainPanel = new MainPanel();
 			
 			contentPane.add(mainPanel, BorderLayout.CENTER);
 			this.pack();
 			centerOnScreen();
 		}
 		else if (change == IServerConnectionListener.LOGOUT){
 			contentPane.remove(mainPanel);
 			contentPane.add(loginPanel, BorderLayout.CENTER);
 			this.pack();
 			this.setResizable(false);
 			centerOnScreen();
 			
 			mainPanel = null; //Dispose of mainPanel. This might need more work.
 		}
 	}
 	
 	private void centerOnScreen() {
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Dimension screenSize = tk.getScreenSize();
 		Dimension frameSize = this.getSize();
 		
 		this.setLocation(
 				screenSize.width/2 - frameSize.width/2, 
 				(int)Math.max(screenSize.height * 0.4 - frameSize.height/2,0)
 				);
 	}
 	
 	/**
 	 * Get Singleton instance
 	 * @return
 	 */
 	public static ClientMain client() {
 		return client;
 	}
 	
 	public static void setActiveUser(ActiveUserModel aumodel) {
 		if (activeUser != null)
 			activeUser.clearListeners();
 		
 		activeUser = aumodel;
 	}
 	
 	public static ActiveUserModel getActiveUser() {
 		return activeUser;
 	}
 	
 	public static void main(String[] args) {
 		client = new ClientMain();
 	}
 }
