 package net.es.oscars.authN.common;
 
 import java.util.*;
 
 import net.es.oscars.authN.beans.User;
 import net.es.oscars.authN.dao.UserDAO;
 import net.es.oscars.authN.soap.gen.DNType;
 import net.es.oscars.logging.OSCARSNetLogger;
 
 import org.apache.log4j.Logger;
 
 import oasis.names.tc.saml._2_0.assertion.AttributeType;
 
 /**
  * AuthNManager handles all authentication related method calls.
  *
  * @author David Robertson, Mary Thompson
  */
 public class AuthNManager {
     private Logger log;
     private String dbname;
     private String salt;
     private AuthNUtils authNUtils;
 
     public AuthNManager(String dbname, String salt) {
         this.log = Logger.getLogger(this.getClass());
         this.dbname = dbname;
         this.salt = salt;
         this.authNUtils = new AuthNUtils(dbname);
     }
 
     /**
      * Gets list of user attributes, given their DN.
      *
      * @param dn contains the subject and issue DNs from a certificate
      * @return a list of user attributes, if any
      */
     public List<AttributeType> verifyDN(DNType dn)  throws  AuthNException {
 
         String event = "mgrVerifyDN";
         String subjectDN = dn.getSubjectDN();
         String issuerDN = dn.getIssuerDN();
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
         this.log.debug(netLogger.start(event,"subjectDN is " +  subjectDN));
 
         List<AttributeType> attributes = new ArrayList<AttributeType>();
         UserDAO userDAO = new UserDAO(this.dbname);
         User user = userDAO.fromDN(subjectDN);
         if (user == null) {
             // try the reverse of the elements in the DN
             String reverseDN = reverseElements(subjectDN);
             this.log.debug(netLogger.getMsg(event, "checkUser reverse DN: " + reverseDN));
             user = userDAO.fromDN(reverseDN);
             if (user == null){
                this.log.error(netLogger.getMsg(event, "No user found for DN: " + subjectDN));
                 return attributes;
             }
         }
         if (!user.getCertIssuer().equals(issuerDN)) {
             String reverseDN = reverseElements(issuerDN);
             this.log.debug(netLogger.getMsg(event, "checkUser issuer reverse DN: " + reverseDN));
             if (!user.getCertIssuer().equals(reverseDN)) {
                this.log.error(netLogger.getMsg(event, "User found for DN, '" + subjectDN + "', but issuer does not match issuer DN: " + issuerDN ));
                 return attributes;
             }
         }
         String login = user.getLogin();
         attributes = this.authNUtils.getAttributesForUser(user.getLogin());
         this.log.debug(netLogger.end(event,"loginName is " + login));
         return attributes;
     }
 
     /**
      * Authenticates user via login name and password.
      * Check to see that user exists, the password is correct
      * and that the user has at least one attribute
      *
      * @param userName a string with the user's login name
      * @param password a string with the user's password
      * @return a list of all the user's attributes
      */
 
     public List<AttributeType>
         verifyLogin(String userName, String password) throws AuthNException {
 
         User user = null;
         String event = "mgrVerifyLogin";
         OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
 
         this.log.debug(netLogger.start(event,"loginId is " + userName));
         UserDAO userDAO = new UserDAO(this.dbname);
         if (password == null) {
             throw new AuthNException(
                 "verifyLogin: null password given for user " + userName);
         }
         user = userDAO.query(userName);
         if (user == null) {
             throw new AuthNException(
                     "verifyLogin: User not registered " + userName + ".");
         }
         // encrypt the password before comparison
         String encryptedPwd = Jcrypt.crypt(this.salt, password);
         if (!encryptedPwd.equals(user.getPassword())) {
             throw new AuthNException( "verifyLogin: password is incorrect for " + userName);
         }
 
         List<AttributeType> attributes =
             this.authNUtils.getAttributesForUser(user.getLogin());
         userDAO.update(user);
         this.log.info(netLogger.end(event,"verified login for " + userName));
         return attributes;
     }
 
     /**
      * reverse the elements in a DN
      * @param dn
      * @return
      */
     private String reverseElements(String dn){
         String[] dnElems = dn.split(",");
         String reverseDN = " " + dnElems[0];
         for (int i = 1; i < dnElems.length; i++) {
             reverseDN = dnElems[i] + "," + reverseDN;
         }
         reverseDN = reverseDN.substring(1);
         return reverseDN;
     }
 }
