 package info.geared.corki.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 
 public class LoginFrame extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 1L;
 
 	/* All JComponents fields here. */
 	// 4 text areas
 	// 4 labels
 	// 1 button
 
 	LoginFrame()
 	{
 
 		buildUI();
 	}
 
 	private void buildUI()
 	{
 
 	}
 
 	/**
 	 * This handler will be called when the user clicks the Login button. The
 	 * method will verify the user entered valid information and alert them if
 	 * there is a problem. It will attempt to construct a ChatSession, and if it
 	 * is successful, then it will construct a ClientFrame and dispose.
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		// Get data from fields
 
 		ChatSession s = new ChatSession("localhost", "david", "");
 		if (s.getStatus() != ChatSession.Status.DISCONNECTED)
 		{
 			// Session failed
 		}
 		else
 		{
 			ClientFrame client = new ClientFrame(s);
 			s.addChatSessionListener(client);
 			dispose();
 		}
 	}
 
 	public static void main(String[] args) throws InterruptedException
 	{	
 		ChatSession s = new ChatSession("localhost:37195", "david", "");;
 		try
 		{
 			if (!s.open())
 			{
 				System.out.println("The Chat session could not be opened!");
 			}
 			else
 			{
 				Thread.sleep(5000);
 				s.send("Hi I'm a banana");
 				s.send("Ohh boy!");
 				s.send("WHAT THE FUCKKKK!");
 			}
 		}
 		finally
 		{
 			s.close();
 		}
 	}
 
 }
