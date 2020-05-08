 package decentchat.internal.remotes;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 import decentchat.api.Status;
 
 /**
  * Encapsulates all remote services, i.e. all services
  * where we want to inform a contact of certain events.
  * 
  * @author k
  */
 public interface ProtocolInterface extends Remote {
 
 	/**
 	 * Sends a message to a contact.
 	 * @param message The message we want to send.
 	 * @throws RemoteException
 	 */
 	public void sendMessage(String message) throws RemoteException;
 
 	/**
 	 * Tells a contact that we want to go offline soon.
 	 * @throws RemoteException
 	 */
 	public void notifyOffline() throws RemoteException;
 	
 	/**
 	 * Tells a contact that we are now online.
 	 * @param port TODO
 	 * @throws RemoteException
 	 */
 	public void notifyOnline(int port) throws RemoteException;
 
 	/**
 	 * Tells a contact that our {@link Status} has changed.
 	 * @param status Our current @link Status}.
 	 */
 	public void setStatus(Status status) throws RemoteException;
 
 	/**
 	 * Tells a contact that our status message has changed.
 	 * @param message Our current status message.
 	 */
 	public void setStatusMessage(String message) throws RemoteException;
 	
 	/**
 	 * Pings a contact.
 	 * @throws RemoteException
 	 */
 	public void ping() throws RemoteException;
 	
 	/**
 	 * Challenges a contact to authenticate itself by
 	 * asking it to encrypt a String of the form
 	 * "<contactIP>/<nonce>" with it's private key
 	 * and return the result.
 	 * @param nonce The nonce to use in the encryption
 	 * process. Prevents replay attacks.
 	 * @return The result of the encryption.
 	 */
	public String authenticate(int nonce) throws RemoteException;
 
 }
