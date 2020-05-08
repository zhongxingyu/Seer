 package decentchat.internal.remotes;
 
 import java.rmi.RemoteException;
 import java.rmi.server.ServerNotActiveException;
 import java.rmi.server.UnicastRemoteObject;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import decentchat.api.DeCentInstance;
 import decentchat.api.Status;
 import decentchat.exceptions.ContactNotFoundException;
 import decentchat.internal.ContactImpl;
 
 public class ProtocolInterfaceImpl extends UnicastRemoteObject implements ProtocolInterface {
 
 	private static final long serialVersionUID = -4017058237863632726L;
 
 	static Logger logger = Logger.getLogger(ProtocolInterfaceImpl.class);
 
 	private DeCentInstance instance;
 	private Map<String, ContactImpl> contacts = new HashMap<String, ContactImpl>();
 	
 	public ProtocolInterfaceImpl(DeCentInstance instance) throws RemoteException {
 		super();
 		this.instance = instance;
 	}
 	
 	/**
 	 * Looks up the contact that belongs to the
 	 * calling node. It does so in a two step
 	 * process:
 	 * 1. It tries to find it by IP
 	 * 2. If that fails, it gets it's protocol
 	 *    interface and from there it's public key
 	 *    and tries to find it by that.
 	 * @return The contact associated with the
 	 * calling node.
 	 */
 	private ContactImpl getContact() throws ContactNotFoundException {
 		if (!isAuthenticated()) {
 			throw new ContactNotFoundException();
 		}
 		try {
 			String ip = getClientHost();
 			if(contacts.containsKey(ip)) {
 				return contacts.get(ip);
 			}
 		} catch (ServerNotActiveException e) {
 			e.printStackTrace();
 		}
 		throw new ContactNotFoundException();
 	}
 	
 	/**
 	 * Looks up a contact by using it's {@link ProtocolInterface}.
 	 * @return The contact associated with the
 	 * calling node.
 	 */
 	private ContactImpl getContact(ProtocolInterface prot) throws ContactNotFoundException {
		if (!authenticate()) {
 			throw new ContactNotFoundException();
 		}
 		PublicKey key = null;
 		try {
 			key = prot.getPubKey();
 		} catch (RemoteException e) {
 			throw new ContactNotFoundException();
 		}
 		ContactImpl ret = null;
 		ret = (ContactImpl) instance.getContactManager().getContact(key);
 		return ret;
 	}
 
 	/**
 	 * Performs the authentication challenge on the currently
 	 * connected client. 
 	 * @return <code>true</code> on successful authentication
 	 * and <code>false</code> on a failure (of any kind).
 	 */
 	private boolean isAuthenticated() {
 		// TODO don't do this everytime
 		// TODO maybe there already is some java method for this?
 		int nonce = 0; // TODO generate real nonce
 		String message = "";
 		try {
 			message = authenticate(nonce);
 		} catch (RemoteException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
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
 	public void notifyOnline(ProtocolInterface protocolInterface) throws RemoteException {
 		try {
 			ContactImpl contact = getContact(protocolInterface);
 			if(contact != null) {
 				contact.setOnline(protocolInterface);
 				contacts.put(getClientHost(), contact);
 			}
 		} catch (ContactNotFoundException e) {
 		} catch (ServerNotActiveException e) {
 			e.printStackTrace();
 		}
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
 
 	@Override
 	public String authenticate(int nonce) throws RemoteException {
 		String message = instance.getIP() + "/" + nonce;
 		PrivateKey privkey = instance.getPrivateKey();
 		// TODO encrypt message with private key
 		return message;
 	}
 
 	@Override
 	public void ping() throws RemoteException {
 		// TODO is there anything that needs to be done
 		// here at all, except maybe logging?
 	}
 
 	@Override
 	public PublicKey getPubKey() throws RemoteException {
 		return instance.getPublicKey();
 	}
 
 
 }
