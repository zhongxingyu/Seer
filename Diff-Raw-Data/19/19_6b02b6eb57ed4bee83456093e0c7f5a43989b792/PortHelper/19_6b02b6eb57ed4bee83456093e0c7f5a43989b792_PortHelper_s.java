 package invoices.manager.network;
 
 import java.net.ServerSocket;
 
 /**
  * 
  * @author jjankovi
  *
  */
 public class PortHelper {
 
 	private static PortHelper instance = null;
 	
 	private PortHelper() {
 		
 	}
 	
 	public static PortHelper getInstance() {
 		if (instance == null) {
 			instance = new PortHelper();
 		}
 		return instance;
 	}
 	
 	public void listeningOnPort(int port) {
		try {
			ServerSocket server = new ServerSocket(7800);
			while (true) {
				server.accept();
 			}
		} catch (Exception exc) {
			
		}
 	}
 	
 }
