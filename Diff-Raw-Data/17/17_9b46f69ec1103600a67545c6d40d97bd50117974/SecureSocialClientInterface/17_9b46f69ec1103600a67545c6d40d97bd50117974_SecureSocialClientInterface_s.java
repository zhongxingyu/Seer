 package net.securesocial.client;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Map;
 
 public interface SecureSocialClientInterface {
 
 	/**
 	 * get list of new IDs
 	 * 
 	 * @return list of ids
 	 */
 	public List<String> getSuggestedIds();
 
 	/**
 	 * create new identity
 	 * 
 	 * @param privateKey
 	 * @param publicKey
 	 * @param password
 	 * @param id
 	 *            - new id
 	 */
 	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id);
 
 	/**
 	 * 
 	 * @param privateKey
 	 * @param publicKey
 	 * @param password
 	 * @param id
 	 *            - new id
 	 * @param name
 	 *            - name of new identity
 	 */
 	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id, String name);
 
 	/**
 	 * 
 	 * @param privateKey
 	 * @param publicKey
 	 * @param password
 	 * @param id
 	 *            - new id
 	 * @param properties
 	 *            - map of identity properties
 	 */
 	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id, Map<String, String> properties);
 
 	
 	/**
 	 * Retrieve identity information
 	 * @param id of identity
 	 * @return identity details
 	 */
 	public Identity getIdentity(String id);
 
 	/**
 	 * update existing identity
 	 * 
 	 * @param id
 	 * @param oldPrivateKey
 	 * @param newPublicKey
 	 * @param password
 	 * @param newName
 	 */
	
	/**
	 * find identities by name
	 * @param name
	 * @return
	 */
	public List<Identity> findIdentitiesByName(String name);
	
 	public void updateIdentity(String id, InputStream oldPrivateKey, InputStream newPublicKey, String password, String newName);
 
 	/**
 	 * update existing identity
 	 * 
 	 * @param id
 	 * @param oldPrivateKey
 	 * @param newPublicKey
 	 * @param password
 	 * @param newProperties
 	 */
 	public void updateIdentity(String id, InputStream oldPrivateKey, InputStream newPublicKey, String password, Map<String, String> newProperties);
 
 	/**
 	 * save small amount of private content keyed by guid. It is encrypted with
 	 * public key of owner.
 	 * 
 	 * @param id
 	 * @param privateKey
 	 * @param password
 	 * @param path
 	 * @param content
 	 */
 	public void savePrivateContent(String id, InputStream privateKey, InputStream publicKey, String password, String guid, InputStream content);
 
 	/**
 	 * get private content back
 	 * 
 	 * @param id
 	 * @param privateKey
 	 * @param password
 	 * @param path
 	 * @param content
 	 */
 	public void getPrivateContent(String id, InputStream privateKey, String password, String guid, OutputStream content);
 
 	/**
 	 * get list of users who sent you messages
 	 * @param your id
 	 * @param your privateKey
 	 * @param your password
 	 * @return list of ids
 	 */
 	
 	public List<String> getIncomingUsers(String id, InputStream privateKey, String password);
 
 	/**
 	 * send message
 	 * @param data
 	 * @param sender id
 	 * @param password
 	 * @param senderPrivateKey
 	 * @param recepientIds
 	 */
 	public void sendMessage(String data, String sender, String password, InputStream senderPrivateKey, String... recepientIds);
 
 	/**
 	 * send message with attachment
 	 * @param data
 	 * @param attachment
 	 * @param senderId
 	 * @param password
 	 * @param senderPrivateKey
 	 * @param recepientIds
 	 */
 	
 	public void sendMessage(String data, InputStream attachment, String senderId, String password, InputStream senderPrivateKey, String... recepientIds);
 
 	/**
 	 * accept messages from specific user
 	 * @param myId
 	 * @param password
 	 * @param myPrivateKey
 	 * @param senderId
 	 */
 	public void acceptMessages(String myId, String password, InputStream myPrivateKey, String senderId);
 
 	/**
 	 * get messages from my timeline
 	 * @param myId
 	 * @param password
 	 * @param myPrivateKey
 	 * @param timeline mask such as yyyy-MM-ddd
 	 * @return list of messages
 	 */
 	
 	public List<Message> getMessages(String myId, String password, InputStream myPrivateKey, String timeline);
 
 	/**
 	 * get message attachment
 	 * @param myId
 	 * @param password
 	 * @param myPrivateKey
 	 * @param timeline mask
 	 * @param message
 	 * @param where to write attachment
 	 */
 	public void getMessageAttachment(String myId, String password, InputStream myPrivateKey, String timeline, Message msg, OutputStream output);
 
 	/**
 	 * delete individual message
 	 * @param myId
 	 * @param password
 	 * @param myPrivateKey
 	 * @param timeline mask
 	 * @param message to delete
 	 */
 	public void deleteMessage(String myId, String password, InputStream myPrivateKey, String timeline, Message msg);
 
 }
