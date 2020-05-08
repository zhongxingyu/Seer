 package org.makumba.parade.auth;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.Security;
 import java.util.StringTokenizer;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 
 import org.makumba.parade.init.ParadeProperties;
 
 import com.novell.ldap.LDAPConnection;
 import com.novell.ldap.LDAPException;
 import com.novell.security.sasl.RealmCallback;
 import com.novell.security.sasl.RealmChoiceCallback;
 
 /**
  * LDAP Authorizer using JLDAP.
  * 
  * This authorizer reads the following properties from parade.properties:
  * <ul>
  * <li>parade.authorizer.ldapHost: the LDAP server</li>
  * <li>parade.authorizer.baseDN: the base DN of the LDAP</li>
  * <li>parade.authorizer.encryption: the encryptions that can be used in order to authenticate. If empty, plain text is
  * used.
  * </ul>
  * 
  * @author Manuel Gay
  * 
  */
 public class LDAPAuthorizer implements Authorizer {
 
     private static String ldapHost;
 
     private static String baseDN;
 
     private static String[] encryption;
 
     static {
         // read the configuration from the property file
         ldapHost = ParadeProperties.getProperty("parade.authorization.ldapHost");
         baseDN = ParadeProperties.getProperty("parade.authorization.baseDN");
         String encryptionProp = ParadeProperties.getProperty("parade.authorization.encryption");
         if (encryptionProp != null && encryptionProp.indexOf(",") > -1) {
 
             StringTokenizer st = new StringTokenizer(encryptionProp, ",");
             encryption = new String[st.countTokens()];
             int i = 0;
             while (st.hasMoreTokens()) {
                 encryption[i] = st.nextToken();
                 i++;
             }
         } else if (!encryptionProp.equals("")) {
             encryption = new String[1];
             encryption[0] = encryptionProp;
         } else {
             encryption = new String[0];
         }
     }
 
     public boolean auth(String username, String password) {
 
         int ldapPort = LDAPConnection.DEFAULT_PORT;
         int ldapVersion = LDAPConnection.LDAP_V3;
 
         LDAPConnection lc = new LDAPConnection();
 
         String loginDN = "uid=" + username + "," + baseDN;
 
         if (encryption.length != 0) {
             try {
                 Security.addProvider(new com.novell.sasl.client.SaslProvider());
             } catch (Exception e) {
                 System.err.println("Error loading security provider (" + e.getMessage() + ")");
             }
         }
 
         // connect to the server
 
         try {
             lc.connect(ldapHost, ldapPort);
 
             // bind to the server
             // the way we bind depends on whether we use encryption or not
 
             if (encryption.length != 0) {
                 lc.bind(loginDN, "dn: " + loginDN, encryption, null, new BindCallbackHandler(password));
             } else {
                 lc.bind(ldapVersion, loginDN, password.getBytes("UTF8"));
             }
 
             // in the end we disconnect
             lc.disconnect();
             return true;
 
         } catch (LDAPException e) {
            System.err.println("LDAP AUTHORIZER ERROR: login failed for user "+username+", loginDN "+loginDN);
             return false;
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return false;
         }
 
     }
 
 }
 
 class BindCallbackHandler implements CallbackHandler {
 
     private char[] m_password;
 
     BindCallbackHandler(String password) {
         m_password = new char[password.length()];
         password.getChars(0, password.length(), m_password, 0);
     }
 
     public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
 
         for (int i = 0; i < callbacks.length; i++) {
 
             if (callbacks[i] instanceof PasswordCallback) {
                 ((PasswordCallback) callbacks[i]).setPassword(m_password);
             } else if (callbacks[i] instanceof NameCallback) {
                 ((NameCallback) callbacks[i]).setName(((NameCallback) callbacks[i]).getDefaultName());
             } else if (callbacks[i] instanceof RealmCallback) {
                 ((RealmCallback) callbacks[i]).setText(((RealmCallback) callbacks[i]).getDefaultText());
             } else if (callbacks[i] instanceof RealmChoiceCallback) {
                 ((RealmChoiceCallback) callbacks[i]).setSelectedIndex(0);
             }
         }
     }
 }
