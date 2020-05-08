 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Channel;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 
 /**
  *
  * @author daniela
  */
 public class SecureChannel extends TCPChannel {
     protected IChannel channel;
     
     private SecretKey secretKey;
     private byte[] ivParameter;
     private boolean sessionKeyExists = false;
     
     private PublicKey otherPubKey;
     private PrivateKey myPrivKey;
     
     private Cipher cEncypt;
     private Cipher cDecrypt;
 
     public SecureChannel(PrintWriter out, BufferedReader in) {
         super(out, in);
 
     }
    
     //to delete?
     public SecureChannel(TCPChannel channel){
         super(channel);
         this.channel = channel;
     }
     
    /*public SecureChannel(TCPChannel channel, PublicKey otherPubKey, PrivateKey myPrivKey){
         super(channel);
         this.channel = channel;
         this.otherPubKey = otherPubKey;
         this.myPrivKey = myPrivKey;
    }*/
 
     public void send(String message) {
         System.out.println("step 1 sec");
         //TODO Encrypt message here
         
         if(!sessionKeyExists) { //RSA pub encryption
             
         } else { //AES encryption
             
         }
         
         channel.send(message);
     
     }
 
     public String receive() throws IOException{
         //TODO Decrypt message here
         if(!sessionKeyExists) { //RSA priv decryption
             
         } else { //AES decryption
             
         }
         
         
         return channel.receive();
     
     }
     
     
     public void setSessionKey(SecretKey secretKey, byte[] ivParameter) {
         this.secretKey = secretKey;
         this.ivParameter = ivParameter;
         sessionKeyExists = true;
     }
     
     public void removeSessionKey() {
         secretKey = null;
         ivParameter = null;
         sessionKeyExists = false;
     }
 }
