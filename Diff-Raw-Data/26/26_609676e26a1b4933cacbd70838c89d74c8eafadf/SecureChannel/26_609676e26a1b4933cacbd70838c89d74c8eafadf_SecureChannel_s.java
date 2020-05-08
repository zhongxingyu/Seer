 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dslab.channels;
 
 import java.nio.charset.Charset;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.AlgorithmParameterSpec;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import org.bouncycastle.util.encoders.Base64;
 
 /**
  *
  * @author Robert Rainer
  */
 public class SecureChannel extends ChannelDecorator {
 
     private Key secretKey;
     private AlgorithmParameterSpec iv;
 
     public SecureChannel(Channel decoratedChannel, Key key, AlgorithmParameterSpec paramSpec) {
 	super(decoratedChannel);
 	this.secretKey = key;
 	this.iv = paramSpec;
     }
 
     @Override
     public String receive() {
 	String message = super.receive();
 	if(message==null){
 	    return null;
 	}
 	Cipher crypt = null;
 	try {
 	    crypt = Cipher.getInstance("AES/CTR/NoPadding");
	} catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
 	    Logger.getLogger(SecureChannel.class.getName()).log(Level.SEVERE, null, ex);
 	    return null;
 	}
 	try {
 	    crypt.init(Cipher.DECRYPT_MODE, secretKey, iv);
	} catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {
 	    Logger.getLogger(SecureChannel.class.getName()).log(Level.SEVERE, null, ex);
 	    return null;
 	}
 	byte[] decryptedMessage = null;
 	try {
 	    decryptedMessage = crypt.doFinal(message.getBytes());
	} catch (IllegalBlockSizeException | BadPaddingException ex) {
 	    Logger.getLogger(SecureChannel.class.getName()).log(Level.SEVERE, null, ex);
 	    return null;
 	}
 	return new String(decryptedMessage);
 
 
 
     }
 
     @Override
     public void send(String message) {
 	if(message==null){
 	    return;
 	}
 	Cipher crypt = null;
 	try {
 	    crypt = Cipher.getInstance("AES/CTR/NoPadding");
 	} catch (NoSuchAlgorithmException ex) {
 	    Logger.getLogger(SecureChannel.class
 		    .getName()).log(Level.SEVERE, null, ex);
 	} catch (NoSuchPaddingException ex) {
 	    Logger.getLogger(SecureChannel.class
 		    .getName()).log(Level.SEVERE, null, ex);
 	}
 	try {
 	    crypt.init(Cipher.ENCRYPT_MODE, secretKey, iv);
 	} catch (InvalidKeyException ex) {
 	    Logger.getLogger(SecureChannel.class
 		    .getName()).log(Level.SEVERE, null, ex);
 	} catch (InvalidAlgorithmParameterException ex) {
 	    Logger.getLogger(SecureChannel.class
 		    .getName()).log(Level.SEVERE, null, ex);
 	}
 	byte[] encryptedMessage = null;
 	try {
 	    encryptedMessage = crypt.doFinal(message.getBytes());
 	} catch (IllegalBlockSizeException ex) {
 	    Logger.getLogger(SecureChannel.class
 		    .getName()).log(Level.SEVERE, null, ex);
 	} catch (BadPaddingException ex) {
 	    Logger.getLogger(SecureChannel.class
 		    .getName()).log(Level.SEVERE, null, ex);
 	}
 	super.send(new String(encryptedMessage));
     }
 }
