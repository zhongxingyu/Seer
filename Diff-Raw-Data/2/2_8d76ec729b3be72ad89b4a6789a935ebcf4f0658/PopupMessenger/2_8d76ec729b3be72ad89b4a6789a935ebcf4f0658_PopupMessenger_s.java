 package ted.ui.messaging;
 
 import javax.swing.JOptionPane;
 
 import ted.TedMainDialog;
 
 public class PopupMessenger implements MessengerInterface 
 {
 	TedMainDialog tedMain;
 	private int type = MessengerInterface.MESSENGER_POPUP;
 	
 	public PopupMessenger(TedMainDialog inTedMain)
 	{
 		this.tedMain = inTedMain;
 	}
 
 	public void displayMessage(String title, String body) 
 	{
 		this.messagePopUp(title, body);
 	}
 
 	public void displayError(String title, String body) 
 	{
 		this.messagePopUp(title, body);
 		
 	}
 
 	public void displayHurray(String title, String body) 
 	{
 		this.messagePopUp(title, body);
 		
 	}
 	
 	private void messagePopUp(String title, String body)
 	{
		JOptionPane.showMessageDialog(null, title, body,  JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public int getType() {
 		return this.type;
 	}
 
 }
