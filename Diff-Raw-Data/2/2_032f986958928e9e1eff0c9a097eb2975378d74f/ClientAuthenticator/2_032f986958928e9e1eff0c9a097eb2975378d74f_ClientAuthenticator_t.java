 package chatter.server;
 
 import chatter.common.Constants;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  * @author Bharath Ravi
  * @author Kapil Gole
  * @author Alban Dumouilla
  *
  * An authenticator that verifies a username and password
  * by comparing hashed values of the password from a predetermined
  * Database of authentic users and passwords.
  */
 public class ClientAuthenticator {
   String uname;
   String passwd;
   UserDatabase database;
 
   public ClientAuthenticator(String unamePasswd) {
     int separator = unamePasswd.indexOf(Constants.PASSWORD_SEPARATOR);
     uname = unamePasswd.substring(0, separator);
     passwd = unamePasswd.substring(separator + 1, unamePasswd.length());
     database = UserDatabase.getInstance();
   }
 
   public synchronized boolean authenticate() {
     try {
       MessageDigest md5 = MessageDigest.getInstance(
           Constants.HASHING_ALGORITHM);
       if (database.database.containsKey(uname)) {
         User user = database.database.get(uname);
         md5.update(user.getSalt().getBytes());
         md5.update(passwd.getBytes());
         byte[] passwdHash = md5.digest();
         BigInteger number = new BigInteger(1, passwdHash);
         String hashtext = number.toString(16);
 
 
         if(!user.isLoggedIn() && user.getPasswordHash().equals(hashtext)) {
           System.out.println("Loggedin: no" );
           user.setLoggedIn(true);
           return true;
         }
 
         if (user.isLoggedIn()) {
           System.out.println("Loggedin: yes" );
         }
       }
     } catch (NoSuchAlgorithmException e) {
       e.printStackTrace();
     }
 
     return false;
   }
 }
