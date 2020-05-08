 package server;
 
 import barker.Bark;
 import barker.ServerInterface;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import barker.Doggy;
 
public class Server extends UnicastRemoteObject implements ServerInterface {
 	
 	private HashMap<String, String> loginMdp;
 	private HashMap<String, Doggy> loginDoggy;
 	private HashMap<String, List<String>> sniffers;
 	private List<Bark> barks;
 
 	private static final long serialVersionUID = -7666130486088047448L;
 
 	protected Server() throws RemoteException {
 		super();
 		loginMdp = new HashMap<String, String>();
 		loginDoggy = new HashMap<String, Doggy>();
 		barks = new ArrayList<Bark>();
 		sniffers = new HashMap<String, List<String>>();
 	}
 	
	@Override
 	public void register(String login, String mdp) throws RemoteException {
 		loginMdp.put(login, mdp);
 		loginDoggy.put(login, new Dog(login, this));
 	}
 	
	@Override
	public Doggy connect(String login, String mdp) throws RemoteException {
 		if (mdp.equals(loginMdp.get(login))) {
 			return loginDoggy.get(login);
 		}
 		return null;
 	}
 
 	public List<Bark> getBarks() {
 		return barks;
 	}
 	
 	public List<String> getSniffers(String login) {
 		return	sniffers.get(login);
 	}
 	
 	public HashMap<String, List<String>> getSniffers() {
 		return sniffers;
 	}
 
 }
