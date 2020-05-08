 package net.pgp2p.networkhandler;
 
import java.math.BigInteger;

 import net.jxta.endpoint.Message;
 import net.jxta.endpoint.MessageElement;
 import net.jxta.endpoint.StringMessageElement;
 
 public class PGP2PMessage extends Message {
 
 	/**
 	 * Generated serial version UID
 	 */
 	private static final long serialVersionUID = -3521104394888893998L;
 	
 	private static final String NAMESPACE 		= PGP2PService.NAMESPACE;
 	public static final String USER_ID_FIELD		= "USER_ID";
 	public static final String KEY_ID_FIELD		= "KEY_ID";
 	public static final String PUBLIC_KEY_FIELD	= "PUBLIC_KEY";
 	public static final String TYPE_FIELD 		= "TYPE";
	public static final String STATUS_FIELD		= "STATUS";

 	private String userID;
 	private long keyID;
 	private String armoredPublicKey;
 	private int type;
	private int status;
 	
 	public PGP2PMessage fromMessage(Message message) {
		this.setUserID(message.getMessageElement(NAMESPACE,USER_ID_FIELD).toString())
			.setKeyID(new BigInteger(message.getMessageElement(NAMESPACE, KEY_ID_FIELD).toString(), 16).longValue())
 			.setArmoredPublicKey(message.getMessageElement(NAMESPACE, PUBLIC_KEY_FIELD).toString())
 			.setType(Integer.valueOf(message.getMessageElement(NAMESPACE, TYPE_FIELD).toString()));
 		return this;
 	}
 
 	public PGP2PMessage() {
 		super();
 	}
 	
 	public PGP2PMessage setUserID(String userID) {
 		this.userID = userID;
 		MessageElement elemUserID = new StringMessageElement(USER_ID_FIELD, userID, null);
 		addMessageElement(NAMESPACE, elemUserID);
 		return this;
 	}
 	
 	public String getUserID() {
 		return this.userID;
 	}
 	
 	public PGP2PMessage setKeyID(Long keyID) {
 		this.keyID = keyID;
 		MessageElement elemKeyID = new StringMessageElement(KEY_ID_FIELD, Long.toHexString(keyID), null);
 		addMessageElement(NAMESPACE, elemKeyID);
 		return this;
 	}
 	
 	public long getKeyID() {
 		return this.keyID;
 	}
 	
 	public PGP2PMessage setArmoredPublicKey(String publicKey) {
 		this.armoredPublicKey = publicKey;
 		// FIXME - avaliar o uso de TextDocumentElement
 		MessageElement elemArmoredPublicKey = new StringMessageElement(PUBLIC_KEY_FIELD, publicKey, null);
 		addMessageElement(NAMESPACE, elemArmoredPublicKey);
 		return this;
 	}
 	
 	public String getArmoredPublicKey() {
 		return this.armoredPublicKey;
 	}
 	
 	public PGP2PMessage setType(int messageType) {
 		this.type = messageType;
 		MessageElement elemKeyID = new StringMessageElement(TYPE_FIELD, String.valueOf(messageType), null);
 		addMessageElement(NAMESPACE, elemKeyID);
 		return this;
 	}
 	
 	public int getType() {
 		return this.type;
 	}
 	
	public PGP2PMessage setStatus(int status) {
		this.status = status;
		MessageElement elemUserID = new StringMessageElement(STATUS_FIELD, String.valueOf(status), null);
		addMessageElement(NAMESPACE, elemUserID);
		return this;
	}
	
	public int getStatus() {
		return this.status;
	}
 }
