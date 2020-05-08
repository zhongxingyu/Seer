 package edu.ucsb.cs290.touch.to.chat.crypto;
 
 import java.io.Serializable;
 import java.security.PublicKey;
 import java.security.SignedObject;
 
 import javax.crypto.SealedObject;
 
 import android.database.Cursor;
 import edu.ucsb.cs290.touch.to.chat.remote.Helpers;
 
 public class Contact implements Serializable {
 	/**
 	 * Three public keys, a name, and a signedObject.
 	 */
 	private static final long serialVersionUID = 1L;
 	private final PublicKey signingKey;
 	private final PublicKey encryptingKey;
 	private final PublicKey tokenKey;
 	private final SignedObject so;
 	private final String name;
 	private long id;
 
 	public Contact(Cursor c) {
 		this(c.getString(c.getColumnIndex(DatabaseHelper.NICKNAME)), 
 			(SealablePublicKey) Helpers.deserialize(c.getBlob(c.getColumnIndex(DatabaseHelper.PUBLIC_KEY))), 
 			(SignedObject) Helpers.deserialize(c.getBlob(c.getColumnIndex(DatabaseHelper.CONTACT_TOKEN))), 
 			c.getLong(c.getColumnIndex(DatabaseHelper.CONTACTS_ID)));
 	}
 	
 	public Contact(String name, PublicKey signing, PublicKey encrypting, PublicKey tokenKey,SignedObject so, long id) {
 		this.signingKey = signing;
 		this.encryptingKey = encrypting;
 		this.tokenKey = tokenKey;
 		this.name = name;
 		this.so = so;
 		this.id = id;
 	}
 	
 	public Contact(Contact c, SignedObject token) {
 		this(c.name,c.signingKey,c.encryptingKey,c.tokenKey,token,c.id);
 	}
 	
 	public Contact(String name, SealablePublicKey key, SignedObject token,
 			long newContactId) {
 		this.signingKey = key.sign();
 		this.encryptingKey = key.encrypt();
 		this.tokenKey = key.address();
 		this.name = name;
 		this.so = token;
		this.id = id;	
 		}
 
 	public PublicKey getSigningKey() {
 		return signingKey;
 	}
 
 	public PublicKey getEncryptingKey() {
 		return encryptingKey;
 	}
 	
 	public SignedObject getToken() {
 		return so;
 	}
 
 	public String toString() {
 		return getName();
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public PublicKey getTokenKey() {
 		return tokenKey;
 	}
 
 	public SealablePublicKey getSealablePublicKey() {
 		return new SealablePublicKey(signingKey, encryptingKey,tokenKey, so);
 	}
 
 	public long getID() {
 		return id;
 	}
 	
 	public void setID(long newID) {
 		id = newID;
 	}
 }
