 package barker;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 public interface ServerInterface extends Remote {
 
 	public void register(String login, String mdp) throws RemoteException;
 	
	public Doggy connect(String login, String mdp);
 }
