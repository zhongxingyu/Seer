 package data.contents;
 
import crypto.Cryptography;
 import data.Content;
 
 import java.security.PublicKey;
 
 /**
  * @author Gary Ye
  * @version 12/1/13
  */
 public class Registration extends Content {
     private Login login;
    private byte[] publicKey;
 
     /**
      * Constructs a registration content object.
      * @param login the login data
      * @param publicKey the public key of the user
      */
     public Registration(Login login, PublicKey publicKey) {
         this.type = Type.REGISTRATION;
         this.login = login;
        this.publicKey = publicKey.getEncoded();
     }
 
     @Override
     public String toString() {
         return login.toString();
     }
 
     public Login getLogin(){
         return login;
     }
 
     public PublicKey getPublicKey() {
        return Cryptography.getPublicKeyFromBytes(publicKey);
     }
 }
