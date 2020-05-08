 package decentchat.internal.remotes;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.Registry;
 import java.rmi.server.ServerNotActiveException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import decentchat.api.Status;
 import decentchat.exceptions.ContactNotFoundException;
 import decentchat.internal.ContactImpl;
 
 public class PushInterfaceImpl extends UnicastRemoteObject implements PushInterface {
 
 	private static final long serialVersionUID = -4017058237863632726L;
 
 	static Logger logger = Logger.getLogger(PushInterfaceImpl.class);
 
 	private Registry registry;
 	private PullInterface pullInterface;
 	private Map<String, ContactImpl> contacts = new HashMap<String, ContactImpl>();  
 	
 	public PushInterfaceImpl(Registry registry, PullInterface pullInterface) throws RemoteException {
 		super();
 		this.registry = registry;
 		this.pullInterface = pullInterface;
 	}
 	
 	/**
 	 * Looks up the contact that belongs to the
 	 * calling node. It does so in a two step
 	 * process:
 	 * 1. It tries to find it by IP
 	 * 2. If that fails, it gets it's push
 	 *    interface and from there it's public key
 	 *    and tries to find it by that.
 	 * @return The contact associated with the
 	 * calling node.
 	 */
 	private ContactImpl getContact() throws ContactNotFoundException {
 		if (!authenticate()) {
 			throw new ContactNotFoundException();
 		}
 		try {
 			String ip = getClientHost();
 			if(contacts.containsKey(ip)) {
 				return contacts.get(ip);
 			}
 			//Naming.lookup("rmi://" + ip +":"+port+ "/node");
 			
 		} catch (ServerNotActiveException e) {
 			e.printStackTrace();
 		}
 		// TODO implement
 		return null;
 	}
 
 	private boolean authenticate() {
 		// TODO don't do this everytime
 		// TODO maybe there already is some java method for this?
 		int nonce = 0; // TODO generate real nonce
		String message = "";
		try {
			message = pullInterface.authenticate(nonce);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // TODO This should be the pullinterface from the client instead!
 		// TODO decrypt message
 		try {
 			if (!message.equals(getClientHost() + "/" + nonce)) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (ServerNotActiveException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public void notifyOffline() throws RemoteException {
 		try {
 			getContact().setOffline();
 			contacts.remove(getClientHost());
 		} catch (ContactNotFoundException e) {
 			// We're not interested then.
 		} catch (ServerNotActiveException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void notifyOnline(int port) throws RemoteException {
 		try {
 			// TODO Use port in the future
 			ContactImpl contact = getContact();
 			contact.setOnline(getPushInterfaceForContact(contact));
 			contacts.put(getClientHost(), contact);
 		} catch (ContactNotFoundException e) {
 		} catch (ServerNotActiveException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private PushInterface getPushInterfaceForContact(ContactImpl contact) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void sendMessage(String message) throws RemoteException {
 		try {
 			getContact().receiveMessage(message);
 		} catch (ContactNotFoundException e) {
 			// TODO client needs to be notified that someone
 			// unknown tries to contact him
 		}
 	}
 
 	@Override
 	public void setStatus(Status status) throws RemoteException {
 		try {
 			getContact().setStatus(status);
 		} catch (ContactNotFoundException e) {
 			// We're not interested then.
 		}
 	}
 
 	@Override
 	public void setStatusMessage(String message) throws RemoteException {
 		try {
 			getContact().setStatusMessage(message);
 		} catch (ContactNotFoundException e) {
 			// We're not interested then.
 		}
 	}
 
 }
