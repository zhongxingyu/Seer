 package decentchat.internal.remotes;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 
 public interface PullInterface extends Remote {
 	
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
	public String authenticate(int nonce);
 
 }
