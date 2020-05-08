 package decentchat.api;
 
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import decentchat.internal.nodes.Node;
 import decentchat.internal.nodes.NodeImpl;
 
 public class DeCentInstance {
 	
 	private String ip;
 	private int port;
 	private Registry reg = null;
 	private Node localNode = null;
 
 	/**
 	 * The {@link DeCentInstance} is the main instance the client will be 
 	 * using when using the DeCentChat protocol. It defines the main
 	 * public interface.
 	 *   
 	 */
 	public DeCentInstance() {		
 	}
 	
 	public boolean init(String bootstrap_ip, int port) {
 		this.port = port;
 		Node bootstrapNode = null;
 		try {
 			bootstrapNode = (Node)Naming.lookup("rmi://" + bootstrap_ip +":" +this.port+ "/node");
			ip = bootstrapNode.getIP();
 			// Now init registry
 			System.setProperty("java.rmi.server.hostname", ip);
 			reg = LocateRegistry.createRegistry(1099);
 		} catch (Exception e) {
 			System.err.println("Problem connecting to " + bootstrap_ip + ":" + port);
 			return false;
 		}
 		if(ip == null || reg == null) return false;
 		try {
 			localNode = new NodeImpl(); //TODO
 		} catch (RemoteException e) {
 		}
 		return true;
 	}
 	
 	public int getPort() {
 		return this.port;
 	}
 	
 }
