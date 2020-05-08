 package automation.api.authenticator;
 
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.Map;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import javax.xml.ws.handler.MessageContext;
 
 public class Authenticator {
 	
 	private Password password;
 	private byte[] encryptedPassword;
 	private byte[] initialisationVector;
 	private SecretKeySpec key;
 	
 	public Authenticator(Password password) {
 		this.password = password;
 		this.encryptedPassword = null;
 		this.initialisationVector = null; 
 		this.key = new SecretKeySpec(password.toString().getBytes(), "AES");
 		
 		//Encrypt Password
 		encryptPassword(password);
 	}
 
 	private void encryptPassword(Password passwordToEncrypt) {
 		try {
 			
 			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
 			cipher.init(Cipher.ENCRYPT_MODE, key);
 			encryptedPassword = cipher.doFinal(passwordToEncrypt.toString().getBytes());
 			initialisationVector = cipher.getIV();
 		
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	// TODO Have better var names!
 	@SuppressWarnings("rawtypes")
 	public boolean isPasswordCorrect(MessageContext mctx) {
     	//get detail from request headers
         Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
         List passList = (List) http_headers.get("password");
         List ivList = (List) http_headers.get("iv");
  
        String incomingPassword = null;
        String iv = null;
         
         if(passList!=null){
         	//get password
        	incomingPassword = passList.get(0).toString();
         }
         
         if(ivList!=null){
         	//get initialization vector
        	iv = ivList.get(0).toString();
         }
 		
 		if (password.toString().equals(
				decryptPassword(incomingPassword.getBytes(), iv.getBytes()))) {
 			return true;
 		}
 		
 		else {
 			return false;
 		}
 	}
 	
 	// TODO Should be changed to private
 	public String decryptPassword(byte[] encryptedData, byte[] initialisationVector) {
 		byte[] unencryptedData = null;
 		byte[] iv = initialisationVector;
 		
 		try {
 		
 			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
 			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
 			unencryptedData = cipher.doFinal(encryptedData);
 		
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();	
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return new String(unencryptedData);
 	}
 	
 	public byte[] getEncryptedPassword() {
 		return encryptedPassword;
 	}
 
 	public byte[] getInitialisationVector() {
 		return initialisationVector;
 	}
 }
