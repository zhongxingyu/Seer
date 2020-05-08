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
 
 import com.novell.ldap.LDAPAttributeSet;
 import com.novell.ldap.LDAPConnection;
 import com.novell.ldap.LDAPEntry;
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
 
     /** LDAP attributes */
 
     private String displayName;
 
     private String givenName;
 
     private String employeeType;
 
     private String sn;
 
     private String mail;
 
     private String cn;
 
     private byte[] jpegPhoto;
 
     public boolean auth(String username, String password) {
 
         if (username.equals(""))
             return false;
 
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
 
             String returnAttrs[] = { "displayName", "givenName", "employeeType", "sn", "mail", "cn", "jpegPhoto" };
 
             LDAPEntry entry = lc.read(loginDN, returnAttrs);
 
             LDAPAttributeSet attributeSet = entry.getAttributeSet();
 
             displayName = attributeSet.getAttribute("displayName").getStringValue();
             givenName = attributeSet.getAttribute("givenName").getStringValue();
             employeeType = attributeSet.getAttribute("sn").getStringValue();
             mail = attributeSet.getAttribute("mail").getStringValue();
             sn = attributeSet.getAttribute("sn").getStringValue();
 
             String[] cns = attributeSet.getAttribute("cn").getStringValueArray();
             if (cns.length > 1) {
                 // take the shortname
                 cn = cns[1];
             } else {
                 cn = attributeSet.getAttribute("cn").getStringValue();
             }
            jpegPhoto = attributeSet.getAttribute("jpegPhoto").getByteValue();
 
             // in the end we disconnect
             lc.disconnect();
             return true;
 
         } catch (LDAPException e) {
             System.err.println("LDAP AUTHORIZER ERROR: login failed for user " + username + ", loginDN " + loginDN);
             return false;
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return false;
         }
 
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public String getGivenName() {
         return givenName;
     }
 
     public String getEmployeeType() {
         return employeeType;
     }
 
     public String getSn() {
         return sn;
     }
 
     public String getMail() {
         return mail;
     }
 
     public String getCn() {
         return cn;
     }
 
     public byte[] getJpegPhoto() {
         return jpegPhoto;
     }
 
 }
 
 class BindCallbackHandler implements CallbackHandler {
 
     private char[] m_password;
 
     BindCallbackHandler(String password) {
         m_password = new char[password.length()];
         password.getChars(0, password.length(), m_password, 0);
     }
 
     public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
 
         for (Callback element : callbacks) {
 
             if (element instanceof PasswordCallback) {
                 ((PasswordCallback) element).setPassword(m_password);
             } else if (element instanceof NameCallback) {
                 ((NameCallback) element).setName(((NameCallback) element).getDefaultName());
             } else if (element instanceof RealmCallback) {
                 ((RealmCallback) element).setText(((RealmCallback) element).getDefaultText());
             } else if (element instanceof RealmChoiceCallback) {
                 ((RealmChoiceCallback) element).setSelectedIndex(0);
             }
         }
     }
 }
