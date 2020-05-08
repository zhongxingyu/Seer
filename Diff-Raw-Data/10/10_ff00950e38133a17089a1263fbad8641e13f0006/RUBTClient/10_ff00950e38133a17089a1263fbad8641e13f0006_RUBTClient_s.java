 import java.util.Scanner;
 
 import javax.swing.UIManager;
 
 import bt.Exceptions.NotifyPromptException;
 import bt.Model.Bittorrent;
 import bt.Utils.CommandParser;
 import bt.View.ClientGUI;
 import bt.View.UserInterface;
 
 /**
  * This is the main entry point for the client. It will just simply
  * initialize the Bittorrent client.
  * 
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci
  *
  */
 
 public class RUBTClient {
 	
 	
 	/**
 	 * Client entry point.
 	 * @param args command-line arguments.
 	 * 	(e.g. somefile.torrent, picture.jpg)
 	 */
 	public static void main(String[] args) {
 		
 		Bittorrent bittorrent = null;
 		@SuppressWarnings("unused")
 		UserInterface ui = null;
 		try {
 			// GUI initialization
 			try {
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			} catch (Exception e) {}
 			ClientGUI cGUI = ClientGUI.getInstance();
 			cGUI.startGUI();
 		} catch (Exception e) {}
 	}
 }
