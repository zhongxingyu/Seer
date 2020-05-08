 /**
  *  DiffieHellman.java
  *
  *  @author Nilo Redini
  *  @author Davide Pellegrino
  *
  *  This class allows to handle DiffieHellman protocol.
  * 
 */
 
 
 package SecureChat.crypto;
 import java.math.BigInteger;
 import java.security.spec.InvalidKeySpecException;
 import java.io.IOException;
 import javax.crypto.SecretKey;
 import javax.crypto.KeyAgreement;
 import java.security.KeyFactory;
 import java.security.spec.X509EncodedKeySpec;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import javax.crypto.spec.DHParameterSpec;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.ClassNotFoundException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Random;
 import java.security.SignatureException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays; 
 
 
 
 //modo migliore per la gestione da int a byte[]!!!
 public class DiffieHellman{
     /** Length of random exponent */
     static final int LENGTH_RANDOM_EXPONENT = 200;
     /** Decimal numbers representable by one byte */
     static final int NUM_BYTE = 64;
     /** Custom separator in a message signed */
     static final String SIGSEPARATOR = "DIGSIG";
     /** Numeber whereby do the module operation */
     private BigInteger p; 
     /** Base number generator */
     private BigInteger g; 
     /** Strings length */
     private int len;  
     /** PublicKey in bytes used to send part of DH shared key */
     byte[] publicKeyBytes ;
     /** Used to check whethet the class istance is valid or not */
     boolean valid;
     /** Used to sign messages in order to avoid Man in the middle (MID)*/
     Rsa rsa;
 
     /** 
         Main constructor
         @param rsa : An rsa object used to sign data
      */
     public  DiffieHellman(Rsa rsa){
         len = LENGTH_RANDOM_EXPONENT;
         publicKeyBytes = null;
         valid = false;
         p = null;
         g = null;
         this.rsa = rsa;
     }
 
     /**
        Allows to check whether an istance of this class is valid or not
        @return boolean: True if the istance is valid, false otherwise
      */
     public boolean isValid(){
         return valid;
     }
     
     /**
        Allows to convert byte[] to String
        @param arr : Byte array to convert
        @return String : Text converted
        @throws UnsupportedEncodingException
      */
     private String getText (byte[] arr) throws UnsupportedEncodingException
     {
         String s = new String( arr, "UTF-8" );
         return s;
     }
     
     
     /**
        This function reads the two numbers P and Q used by Diffie-Hellman
        @param PathBase : Path where the file is stored
        @throws IOException
      */
     public void readBaseKey(String PathBase) throws IOException{
         BufferedReader fis = new BufferedReader(new FileReader(PathBase));
         p = new BigInteger(fis.readLine()); //almeno 300 cifre
         g = new BigInteger(fis.readLine());
         fis.close();
     }
 
     /**
        Allows to concatenate due byte arrays
        @param A : First byte array
        @param B : Second byte array
        @return byte[]
      */
     private byte[] concatBytes(byte[] A, byte[] B) {
         byte[] C= new byte[A.length + B.length];
         System.arraycopy(A, 0, C, 0, A.length);
         System.arraycopy(B, 0, C, A.length, B.length);
         return C;
         }
     
     /**
        Allows to find a string in a byte array
        @param b : Byte array to search in
        @param off : offset
        @param sep : String to search
        @return int : Initial position of the string 
      */
     private int findDelimiterBytes(byte [] b, int off,String sep){
         int len = sep.length();
         int k=0;
         int ap=0;
         for(int i = off ; i < b.length ; i++){
             if((char)b[i]== sep.charAt(0) ){
                 ap=len;
                 k=0;
                 for(int j=i; j<ap+i ;j++){                                        
                     if((char)b[j]!=sep.charAt(k)){len=ap; break;}
                     len--; k++;
                 }
                 if(len == 0) return i;
             }
         }
         return -1;
     }
 
     /**
        This function allows to avoid main in the middle attack,
        signing every message sent and usign a nonce.
        @param StreamOut : A socket stream (out)
        @param StreamIn : A socket stream (in)
        @param publicKey : Part of the shared secret key to send on the other side
        @param FName : The friend name whereby main user is talking
        @return byte[] : The final part of the shared key
        @throws IOException
        @throws ClassNotFoundException
        @throws InvalidKeyException
        @throws SignatureException
        @throws UnsupportedEncodingException
        @throws InvalidKeySpecException
        @throws NoSuchAlgorithmException 
      */
     private byte[] CheckFreshness(ObjectOutputStream StreamOut,ObjectInputStream StreamIn,PublicKey publicKey,String FName)throws IOException,
                                                                                                                                    ClassNotFoundException,
                                                                                                                                    InvalidKeyException, 
                                                                                                                                    SignatureException,
                                                                                                                                    UnsupportedEncodingException,
                                                                                                                                    InvalidKeySpecException,
                                                                                                                                    NoSuchAlgorithmException {
         Random rnd = new Random();
         int na = rnd.nextInt(NUM_BYTE * 8);
        
        StreamOut.writeObject((Integer.toString(na)).getBytes());
 
        byte[] num = (byte[]) StreamIn.readObject();
         
         int nb = Integer.parseInt(getText(num));
         //signs the new nonces and the the part of secret
        String p1 = Integer.toString(na) + SIGSEPARATOR + Integer.toString(nb) + SIGSEPARATOR;
         byte[] tosign = concatBytes(p1.getBytes(),publicKey.getEncoded());
        byte[] signed = rsa.SignMessage(tosign);
         
         //Sends number
         byte[] tocn2 = concatBytes(tosign,SIGSEPARATOR.getBytes());
         byte[] toSend2 = concatBytes(tocn2,signed);
         StreamOut.writeObject(toSend2);
 
 
         //message form: Nmine,Nother,Pkey,F(Nmine,Nother,Pkey)
         byte[] nonce2 = (byte[]) StreamIn.readObject();
         
        int del1 = findDelimiterBytes(nonce2,0,SIGSEPARATOR);
         int del2 = findDelimiterBytes(nonce2,del1 + SIGSEPARATOR.length(),SIGSEPARATOR);
         int del3 = findDelimiterBytes(nonce2,del2 + SIGSEPARATOR.length(),SIGSEPARATOR);
 
         byte[] Part1 = Arrays.copyOfRange(nonce2, 0, del3);
         byte[] sig2 = Arrays.copyOfRange(nonce2,del3 + SIGSEPARATOR.length(),nonce2.length);
         if(!rsa.CheckSign(Part1,sig2,FName))
             return null;
         byte[] num2 = Arrays.copyOfRange(nonce2, del1 + SIGSEPARATOR.length() , del2);
         byte[] key = Arrays.copyOfRange(nonce2,del2 + SIGSEPARATOR.length() ,del3); 
       
         if( Integer.parseInt(getText(num2)) != (na)){
             //trust exception
             return null;
             }
         return key;
             
     }
 
     /**
        This function allows to generate a secret key using Diffie-Hellman algorithm
        @param PathBase : Path of file wich contains P and Q numbers to use in Diffie-Hellman
        @param StreamOut : A socket stream (out)
        @param StreamIn : A socket stream (in)
        @param FName : The friend name whereby main user is talking
        @return SecretKey : A shared secret key to use for the talk session
        @throws IOException
        @throws SignatureException              
      */
     public SecretKey genKeystream(String PathBase,ObjectOutputStream StreamOut,ObjectInputStream StreamIn,String FName) throws IOException,
                                                                                                                                SignatureException{    
         
         try {    
             readBaseKey(PathBase);
             
             // Use the values to generate a key pair
             KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
             DHParameterSpec dhSpec = new DHParameterSpec(p, g, len);
             keyGen.initialize(dhSpec);
             KeyPair keypair = keyGen.generateKeyPair();
             
             // Get the generated public and private keys
             PrivateKey privateKey = keypair.getPrivate();
             PublicKey publicKey = keypair.getPublic();
 
             //nounces
             publicKeyBytes = CheckFreshness(StreamOut,StreamIn,publicKey,FName);
             if(publicKeyBytes == null) return null;
               
             
             // Convert the public key bytes into a PublicKey object
             X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
             KeyFactory keyFact = KeyFactory.getInstance("DH");
             publicKey = keyFact.generatePublic(x509KeySpec);
             
             // Prepare to generate the secret key with the private key and public key of the other party
             KeyAgreement ka = KeyAgreement.getInstance("DH");
             ka.init(privateKey);
             ka.doPhase(publicKey, true);
             // Specify the type of key to generate;
             // see Listing All Available Symmetric Key Generators
             String algorithm = "DESede";
         
             // Generate the secret key
             SecretKey secretKey = ka.generateSecret(algorithm);
             valid = true;
             return secretKey;
            
         } 
         catch (java.security.InvalidKeyException e) {valid = false;}
         catch (java.security.spec.InvalidKeySpecException e) {valid = false;}
         catch (java.security.InvalidAlgorithmParameterException e) {valid = false;}
         catch (java.security.NoSuchAlgorithmException e) {valid = false;}
         catch(ClassNotFoundException x){valid = false;}
         catch (IOException e) {
             valid = false;
             throw e;
         }
         
         return null;
          
     }
 }
