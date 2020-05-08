 /**
  * 
  */
 package brokerPac;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.security.auth.login.LoginException;
 
 /**
  * @author tom
 *
  */
 public class StartBroker {
 
 	/**
 	 * @param args
	 * @throws IOException 
	 * @throws LoginException 
	 * @throws FileNotFoundException 
	 * @throws NumberFormatException 
 	 */
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException, LoginException, IOException {
		Broker broker1=new Broker(args[0],args[1],Integer.parseInt(args[2]));;
 		broker1.connectToStockExcange();
//		Broker broker2=new Broker("broker2",args[1],Integer.parseInt(args[2]));;
//		broker2.connectToStockExcange();
//		Broker broker3=new Broker("broker3",args[1],Integer.parseInt(args[2]));;
//		broker3.connectToStockExcange();
 	}
 
 }
