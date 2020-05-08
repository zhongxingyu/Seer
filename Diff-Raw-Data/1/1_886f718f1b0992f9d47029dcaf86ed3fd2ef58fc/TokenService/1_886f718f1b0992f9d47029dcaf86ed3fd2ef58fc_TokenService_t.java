 package utils;
 
 import java.io.File;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 
 /**
  * Class has static methods regarding Tokens and encryption/decryption of these. 
  * @author Claus - clih@itu.dk
  * @author Michael - msoa@itu.dk
  * @author BieberFever
  *
  */
 public class TokenService {
   /**
    * Public method for retrieving a Token.
    * The Token is encrypted.
    * A Token describes an authenticated user and when the authentication was done.
    * @param credentials
    * @return Encrypted Token
    * @return Returns null if authentication failed
    */
   public static byte[] getToken(byte[] credentials) {
     try {
       String credentialsStr;
       // Decrypt bytes to String 
       credentialsStr = Encrypter.decryptByteArray(credentials);
       // Split into user and password
       String[] split = credentialsStr.split(",");
       String user = split[0];
       String pass = split[1];
       
       // Authenticate credentials via ITU
       // Method returns timestamp
       long ts = 0;
       ts = ItuAuthentication.authenticate(user, pass);
       System.out.println("Authenticated");
       // Create and dobbel encrypt
       // getToken returns an encryptet token
       Token tk = new Token(user, ts);
       return Encrypter.encryptString(new String(tk.getToken()));
     } catch (JSchException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
     	return null;
     }
   }
   
   // HashMap linking names to roles
   private static final Map<String, String> roleMap;
   static
   {
       roleMap = new HashMap<>();
       roleMap.put("Rao", "TEACHER");
       roleMap.put("Thomas", "TEACHER");
       roleMap.put("TA-01", "TA");
   }
 
   /**
    * Static class for authenticating with ITU service 
    *
    */
   private static class ItuAuthentication {
     /**
      * 
      * @param user 
      * @param password
      * @return Timstamp as long
      * @throws JSchException
      */
     public static long authenticate(String user, String password) throws JSchException {
       String host = "ssh.itu.dk";
       JSch jsch = new JSch();
       // Add to known hosts
       String fs = File.separator;
       jsch.setKnownHosts(System.getProperty("user.home")+fs+".ssh"+fs+"known_hosts");
       
       Session session;
       session = jsch.getSession(user, host, 22);
       session.setPassword(password);
       session.setConfig("StrictHostKeyChecking", "no");
       session.connect(1000);
       // Return timestamp as long
       return System.currentTimeMillis();
     }
   }
   
   /**
    * A Token describes an authenticated user and the time of authentication.  
    *
    */
   private static class Token {
     private String user;
     private long timeStamp;
     
     public Token(String user, long timeStamp){
       this.user = user;
       this.timeStamp = timeStamp;
     }
     
     /**
      * 
      * @return Encrypted token
      */
     public byte[] getToken(){
       try {
         return Encrypter.encryptString(getRole()+","+timeStamp, Encrypter.generateKeyFromString("toserver"));
       } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
         return null;
       }
     }
    
     /**
      * 
      * @return The role of the user
      */
     private String getRole() {
       if (roleMap.containsKey(user)) return roleMap.get(user);
       else return "STUDENT";
     }
   }
   
   /**
    * Encrypt stuff 
    *
    */
   private static class Encrypter {
     private static final SecretKeySpec desKey = new SecretKeySpec("clitoken".getBytes(), "DES");
     
     /**
      * 
      * @param str String as seed
      * @return SecretKey
      */
     private static SecretKey generateKeyFromString(String str) {
       final SecretKeySpec desKey = new SecretKeySpec(str.getBytes(), "DES");
       return desKey;
     }
     
     /**
      * 
      * @param str
      * @return str in encrypted byte-array
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      * @throws InvalidKeyException
      * @throws IllegalBlockSizeException
      * @throws BadPaddingException
      */
     private static byte[] encryptString(String str) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
       return encryptString(str, desKey);
     }
     
     /**
      * 
      * @param str
      * @param desKey
      * @return str in encrypted byte-array
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      * @throws InvalidKeyException
      * @throws IllegalBlockSizeException
      * @throws BadPaddingException
      */
     private static byte[] encryptString(String str, SecretKey desKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
       // Create the cipher
       Cipher desCipher;
       desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
       
       // Initialize the cipher for encryption
       desCipher.init(Cipher.ENCRYPT_MODE, desKey);
 
       // Our cleartext
       byte[] cleartext = str.getBytes();
 
       // Encrypt the cleartext
       byte[] ciphertext = desCipher.doFinal(cleartext);
       
       return ciphertext;
     }
     
     /**
      * 
      * @param arr Byte-array to be decrypted
      * @return String
      * @throws IllegalBlockSizeException
      * @throws BadPaddingException
      * @throws InvalidKeyException
      * @throws NoSuchAlgorithmException
      * @throws NoSuchPaddingException
      */
     private static String decryptByteArray(byte[] arr) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
       Cipher desCipher;
       desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
       
       // Initialize the same cipher for decryption
       desCipher.init(Cipher.DECRYPT_MODE, desKey);
 
       // Decrypt the ciphertext
       return new String(desCipher.doFinal(arr));
     }
   }
 }
