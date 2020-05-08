 package server;
 
 import database.DatabaseManager;
 import database.UserDataset;
 import database.UserStatusEnum;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.handler.codec.base64.Base64;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.util.CharsetUtil;
 
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
  */
 public class Authorizer extends RequestHandler {
 
     private static Logger log = Logger.getLogger("server");
 
     private final DatabaseManager dbm;
 
     private MessageDigest digester;
 
 
     public Authorizer(DatabaseManager dbm) {
         super(null);
         this.dbm = dbm;
         try {
             digester = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             log.log(Level.SEVERE, "Can't load SHA-1 Digester", e);
             digester = null;
         }
     }
 
 
     /**
      * Generates a Hash of the given salt/pw combination
      *
      * @param salt The salt
      * @param pw The password
      * @return The generated hash
      */
     protected String generateHash(final String salt, final String pw) {
         // Compute SHA1 of PW:SALT
         String toHash = pw + ":" + salt;
 
         final byte[] bindigest = digester.digest(toHash.getBytes(CharsetUtil.UTF_8));
         // Convert to Hex String
         final StringBuilder hexbuilder = new StringBuilder(bindigest.length * 2);
         for (byte b : bindigest) {
             hexbuilder.append(Integer.toHexString((b >>> 4) & 0x0F));
             hexbuilder.append(Integer.toHexString(b & 0x0F));
         }
         toHash = hexbuilder.toString();
         return toHash;
     }
 
 
     /**
      * Generates a Salt, necessary for hash generation
      * @return The generated salt
      */
     protected String generateSalt() {
         // TODO optimize salt-generation
         final Random rand = new Random();
         final StringBuilder saltBuilder = new StringBuilder(64);
         for (int i = 0; i < 4; i++) {
             saltBuilder.append(Long.toHexString(rand.nextLong()));
         }
 
         return saltBuilder.toString();
     }
 
     /**
      * Authenticates a Request using HTTP Basic Authentication and returns the
      * UserDataset object of the authenticated user or null if authentication
      * failed. Errors will be sent to the client as error messages see protocol
      * specification for details. The connection will get closed after the error
      * has been sent. <br />
      * This method will check if the user is verified. If the user is not verified,
      * an error will be sent and the connection will get closed.
      *
      * @param myReq HttpRequest
      * @return the UserDataset object of the user or null if auth failed
      * @throws java.sql.SQLException Thrown if database query fails
      * @throws java.io.IOException Thrown if error message sending fails
      */
     public UserDataset auth(final HttpRequest myReq) throws SQLException, IOException {
         UserDataset user = authNoResponse(myReq);
 
         if (user == null) {
             responder.writeErrorMessage("EAUTH", "Wrong username or password", null, HttpResponseStatus.UNAUTHORIZED);
             return null;
         }
 
         if (user.status != UserStatusEnum.verified) {
            if (user.status == UserStatusEnum.needs_verification) {
                 responder.writeErrorMessage("ENOTVERIFIED", "User account is not verified",
                         null, HttpResponseStatus.FORBIDDEN);
             } else {
                 // for example if user is marked as deleted
                 responder.writeErrorMessage("EAUTH", "Wrong username or password", null,
                         HttpResponseStatus.UNAUTHORIZED);
             }
             return null;
         }
 
         return user;
     }
 
     /**
      * Authenticates a Request using HTTP Basic Authentication and returns the
      * UserDataset object of the authenticated user or null if authentication
      * failed. No error responses will be sent to the client. This method will
      * not check if the user is verified.
      *
      * @param myReq HttpRequest
      * @return the UserDataset object of the user or null if auth failed
      * @throws java.sql.SQLException Thrown if database query fails
      */
     public UserDataset authNoResponse(final HttpRequest myReq) throws SQLException {
         String email, emailandpw, pw;
         UserDataset user;
         int index;
         // Why between heaven and earth does Java have AES Encryption in
         // the standard library but not Base64 though it has it internally
         // several times
         emailandpw = myReq.getHeader("Authorization");
         if (emailandpw == null) {
             log.info("Missing Authorization header");
             return null;
         }
         // Basic Auth is: "realm BASE64OFPW"
         String[] parts = emailandpw.split(" ");
         if(parts.length != 2){
             log.warning("Wrong Basic Auth Syntax");
             return null;
         }
 
         ChannelBuffer encodeddata;
         ChannelBuffer data;
         // Base64 is always ASCII
         encodeddata = ChannelBuffers.wrappedBuffer(parts[1].getBytes(CharsetUtil.US_ASCII));
 
         data = Base64.decode(encodeddata);
         // The string itself is utf-8
         emailandpw = data.toString(CharsetUtil.UTF_8);
         index = emailandpw.indexOf(':');
         if (index <= 0) {
             log.warning("Wrong Password Syntax in Basic Auth");
             return null;
         }
 
         email = emailandpw.substring(0, index);
         pw = emailandpw.substring(index + 1);
         user = dbm.getUser(email);
 
         if (user == null) {
             log.info("Wrong username");
             return null;
         }
 
         // Compute SHA1 of PW:SALT
         final String toHash = generateHash(user.salt, pw);
 
         log.fine(pw + ":" + user.salt + " : " + toHash);
         if (!user.passwordhash.equals(toHash)) {
             log.info("Wrong username or password");
             return null;
         }
 
         return user;
     }
 }
