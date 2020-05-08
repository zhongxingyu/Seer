 package org.makumba.parade.auth;
 
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 
 import org.makumba.parade.init.ParadeProperties;
 
 public class JNDIAuthorizer implements DirectoryAuthorizer {
 
     private static String ldapHost;
 
     private static String baseDN;
 
     private static String[] encryption;
 
     static {
         // read the configuration from the property file
         ldapHost = ParadeProperties.getParadeProperty("parade.authorization.ldapHost");
         baseDN = ParadeProperties.getParadeProperty("parade.authorization.baseDN");
         String encryptionProp = ParadeProperties.getParadeProperty("parade.authorization.encryption");
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
 
         // Set up environment for creating initial context
         Hashtable<String, String> env = new Hashtable<String, String>();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
         env.put(Context.PROVIDER_URL, "ldap://" + ldapHost + ":389");
 
         // Authenticate
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
 
         String loginDN = "uid=" + username + "," + baseDN;
 
         env.put(Context.SECURITY_PRINCIPAL, loginDN);
         env.put(Context.SECURITY_CREDENTIALS, password);
 
         String returnAttrs[] = { "displayName", "givenName", "employeeType", "sn", "mail", "cn", "jpegPhoto" };
 
         try {
             // Create initial context
             DirContext ctx = new InitialDirContext(env);
             Attributes a = ctx.getAttributes(loginDN, returnAttrs);
 
            displayName = (String) a.get("displayName").get();
            givenName = (String) a.get("givenName").get();
            employeeType = (String) a.get("sn").get();
            mail = (String) a.get("mail").get();
            sn = (String) a.get("sn").get();
 
             NamingEnumeration cns = a.get("cn").getAll();
             String cn = cns.next().toString();
 
             if (cns.hasMore()) {
                 // take the shortname
                 cn = cns.next().toString();
             }
 
             // this may fail, let's see
             Attribute picture = a.get("jpegPhoto");
             if (picture != null) {
                 jpegPhoto = (byte[]) picture.get();
             }
 
             // Close the context when we're done
             ctx.close();
             return true;
 
         } catch (NamingException e) {
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
