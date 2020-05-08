 package decentchat.api;
 
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

 public class DeCentInstance {
 
	public DeCentInstance(String bootstrap_ip, int port) {
		try {
			Node bootstrapNode = (Node)Naming.lookup("rmi://" + bootstrap_ip +":" +port+ "/node");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
	
 }
