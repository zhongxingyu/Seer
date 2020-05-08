 package edu.chip.carranet.auth.backend;
 
 import com.unboundid.ldap.sdk.*;
 import edu.chip.carranet.auth.exception.AuthException;
 import edu.chip.carranet.auth.exception.PermissionException;
 import edu.chip.carranet.auth.exception.ServerException;
 import edu.chip.carranet.jaxb.AuthCapabilities;
 import edu.chip.carranet.jaxb.CarraUserInfo;
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.spin.tools.crypto.signature.Identity;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Dave Ortiz
  *         CHIP 2011
  *         chip.org
  *         <p/>
  *         <p/>
  *         This class uses LDAP for password authentication, authorization will be
  *         done locally.
  */
 @Transactional
 public class LDAPAuthenticator extends AbstractBaseAuthenticator{
 
 
     //We pick out one one LDAP RDN to use as our system only uses "username"
     private String userIdentifier;
     private String basedn;
     private LDAPConnectionPool pool;
 
     @Resource
     private SessionFactory sessionFactory;
 
 
     private List<DN> adminDNs = new ArrayList<DN>();
     private LDAPConnection ldap;
 
     Logger log = Logger.getLogger(LDAPAuthenticator.class);
 
     public LDAPAuthenticator(String ldapAddress, int ldapPort, String ldapBindDN,
                              String ldapBindPassword, String basedn, String userIdentifier)
             throws LDAPException {
 
         this.basedn = basedn;
         this.userIdentifier = userIdentifier;
 
         LDAPConnection con = new LDAPConnection(ldapAddress,
                 ldapPort,
                 ldapBindDN, ldapBindPassword);
 
         pool = new LDAPConnectionPool(con, 10, 20);
     }
 
 
     public LDAPAuthenticator(String ldapAddress, int ldapPort, String basedn, String userIdentifier) throws LDAPException {
         this.basedn = basedn;
         this.userIdentifier = userIdentifier;
 
         LDAPConnection con = new LDAPConnection(ldapAddress, ldapPort);
         pool = new LDAPConnectionPool(con, 10, 20);
 
     }
 
     @Override
     public void changeUserPassword(Identity id, String username, String oldPassword, String newPassword) throws AuthException {
         throw new ServerException("Unimplemented in this edu.chip.carranet.auth backend, LDAP controlled by external entity");
     }
 
     @Transactional
     public Identity authenticate(String username, String pass) {
         try {
 
 
             LDAPConnection ldap = getLdapConnection();
             SearchResult sr = ldap.search(basedn,
                     SearchScope.SUB, (userIdentifier + "=" + username));
 
            if(sr.getSearchEntries().size() < 1){
                throw new PermissionException("No such user");
            }
             String dn = sr.getSearchEntries().get(0).getDN();
 
             DN userDN = new DN(dn);
             if (adminDNs.contains(userDN) && sr.getEntryCount() > 0 && ldap.bind(dn, pass) != null) {
                 //admin user, do something smart
                 User localInfo = findUser(usernameFromDN(userDN));
                 //We have no local info for this user, just make him an ADMIN and nothing else
                 if (localInfo == null) {
                     CarraUserInfo userInfo = new CarraUserInfo();
                     userInfo.setUsername(username);
                     userInfo.getAssertions().add(UserPermissions.admin.toAssertionString());
                     return TokenUtil.createToken(userInfo);
 
 
                 } else {
                     CarraUserInfo userInfo = BackendUtil.createUserInfo(localInfo.getUserName(),
                             localInfo.getAssertions());
                     return TokenUtil.createToken(userInfo);
                 }
             }
 
             BindResult bindResult =
                     ldap.bind(dn, pass);
 
             if(bindResult == null){
                 throw new PermissionException("Error binding");
             }
 
 
             User localUserInfo = findUser(username);
             if (localUserInfo != null) {
 
                 CarraUserInfo userInfo = BackendUtil.createUserInfo(localUserInfo.getUserName(),
                         localUserInfo.getAssertions());
 
                 return TokenUtil.createToken(userInfo);
 
 
             } else {
                 throw new PermissionException("No such user in the local database, that's a no no");
             }
         } catch (LDAPException e) {
             log.error("Recieved LDAP exception", e);
         } catch (AuthException e) {
             log.error("Couldn't sign token", e);
         }
 
         return null;
 
     }
 
 
 
 
 
 
 
     /**
      * in carranet we use uid as the "username"
      *
      * @param distinguishedName
      * @return
      */
     private String usernameFromDN(DN distinguishedName) {
         String uid;
         for (RDN r : distinguishedName.getRDNs()) {
             for (String s : r.getAttributeNames()) {
                 if (s.equalsIgnoreCase(userIdentifier) && r.getAttributeValues().length > 0) {
                     //UID should only have 1 result or things got effed up
                     return r.getAttributeValues()[0];
                 }
             }
         }
 
         log.fatal("Didn't find the attribute " + userIdentifier + " in the distinguished name, " +
                 "check how LDAP is configured");
 
         return null;
 
 
     }
 
     @Override
     public AuthCapabilities getAuthCapabilities() {
 
         AuthCapabilities capabilities = new AuthCapabilities();
 
 
         return new AuthCapabilities();
 
     }
 
     protected LDAPConnection getLdapConnection() throws LDAPException {
         return pool.getConnection();
     }
 
     public void setAdminDNs(List<DN> adminDNs) {
         this.adminDNs = adminDNs;
     }
 
 
 
 }
