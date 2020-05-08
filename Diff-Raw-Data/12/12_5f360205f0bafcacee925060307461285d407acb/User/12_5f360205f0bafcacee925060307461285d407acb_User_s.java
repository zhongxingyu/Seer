 package models;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.SearchControls;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import models.deadbolt.RoleHolder;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.springframework.ldap.NameNotFoundException;
 import org.springframework.ldap.core.AttributesMapper;
 import org.springframework.ldap.core.DistinguishedName;
 import org.springframework.ldap.core.LdapTemplate;
 import org.springframework.ldap.filter.AndFilter;
 import org.springframework.ldap.filter.EqualsFilter;
 
 import play.Logger;
 import play.data.validation.MinSize;
 import play.data.validation.Required;
 import play.modules.spring.Spring;
 import util.Config;
 import controllers.Security;
 
 @Entity
 public class User extends TemporalModel implements RoleHolder {
     @Required
     @Column(unique = true, name = "name", nullable = false)
     public String name;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String fullname;
 
     @Required
     @ManyToOne
     public Role role;
 
     public int loginCount;
 
     @Temporal(TemporalType.TIMESTAMP)
     public Date lastSuccessfulLogin;
 
     @Temporal(TemporalType.TIMESTAMP)
     public Date lastFailedLogin;
 
     @MinSize(8)
     @Required
     public String passwordHash;
 
     public String salt;
 
     @ManyToOne
     public AuthMethod authMethod;
 
     public void setPassword(String string) {
         Logger.debug("User.setPassword(" + string + ")");
         if(!isPersistent()) {
             salt = secureHash((new Date()).toString());
         }
         passwordHash = secureHash(salt + string);
     }
 
     @PrePersist
     @PreUpdate
     public void encryptPassword() {
         Logger.debug("User.encryptPassword() passwordHash = " + passwordHash);
         if(!isPersistent() || !getByUserName(name).passwordHash.equals(passwordHash)) {
             String pass = passwordHash;
             setPassword(pass);
         }
     }
 
     public boolean hasPassword(String submittedPassword) {
         Logger.debug("User.hasPassword()\n" + passwordHash + "\n" + secureHash(salt + submittedPassword) );
         return passwordHash.equals(secureHash(salt + submittedPassword));
     }
 
     private void storeLoginSuccess() {
         loginCount++;
         lastSuccessfulLogin = new Date();
         merge();
         save();
     }
 
     private void storeLoginFailure() {
         lastFailedLogin = new Date();
         merge();
         save();
     }
 
     public static User connect(String login, String password) {
         Logger.setUp("DEBUG");
         User user = getByUserName(login);
         boolean loggedIn = false;
         if(user != null) {
             Logger.debug("User.connect() user = " + user + " authMethod = " + user.authMethod.name);
             // we have user stored in the database
             if(user.authMethod.name.equals(AuthMethod.LOCAL)) {
                 // Check locally stored password
                 loggedIn = user.hasPassword(password);
             } else if (user.authMethod.name.equals(AuthMethod.LDAP)) {
                 // Try to connect using LDAP
                 // The user entry exists in the database so we don't have to retrievie it
                 loggedIn = connectLdap(login, password);
             }
 
             if(loggedIn) {
                 user.storeLoginSuccess();
                 return user;
             } else {
                 user.storeLoginFailure();
                 return null;
             }
 
         } else if (Config.LDAP_SEARCH) {
             // Search naming directory and add the user if listed
             if(connectLdap(login, password)) {
                 // User entry was newly created so we have to retrieve it
                 user = getByUserName(login);
                 user.storeLoginSuccess();
                 return user;
             }
             // If the password was wrong, the user entry was created anyway, so we store the login failure.
             user = getByUserName(login);
             if (user != null) {
                 user.storeLoginFailure();
             }
         }
         return null;
     }
 
     public static boolean connectLdap(String login, String password) {
         LdapTemplate ldapTemplate = Spring.getBeanOfType(LdapTemplate.class);
 
         AndFilter filter = null;
         filter = new AndFilter();
         filter.and(new EqualsFilter("objectClass", "person")).and(new EqualsFilter("uid", login));
 
         SearchControls sc = new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 15, new String[] {"uid", "cn"}, true, false) ;
         try {
             ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.toString(), sc, new UserAttributesMapper());
             // Upon successful search we are sure, that we have the user entry in the database
             Logger.debug("LDAP found user: %s", filter.toString());
         } catch (NameNotFoundException e) {
             Logger.error("LDAP name not found: %s", filter.toString());
             return false;
         }
 
         return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, filter.toString(), password);
     }
 
     @Override
     public String toString() {
         return "User {"
         + "id: "                   + id + ","
         + "created: "              + created + ","
         + "updated: "              + updated + ","
         + "name: "                 + name + ","
         + "role: "                 + role.name + ","
         + "loginCount: "           + loginCount + ","
         + "lastSuccessfulLogin: "  + lastSuccessfulLogin + ","
         + "lastFailedLogin: "      + lastFailedLogin + ","
         + "passwordHash: "         + (passwordHash != null ? passwordHash.substring(0, 10) : "null") + ","
         + "salt: "                 + (salt != null ? salt.substring(0, 10) : "null")
         + "}";
     }
 
     public static User getByUserName(String userName) {
         return find("byName", userName).first();
     }
 
     @Override
     public List<? extends Role> getRoles() {
         return Arrays.asList(role);
     }
 
     private String secureHash(String arg) {
         return DigestUtils.sha512Hex(arg);
     }
    
     private static class UserAttributesMapper  implements AttributesMapper {
         public Object mapFromAttributes(Attributes attrs) throws NamingException {
             String uid = (String)attrs.get("uid").get();
             String fullname = (String)attrs.get("cn").get();
             Logger.debug("UserAttributesMapper: fullname = '%s', uid = '%s'", fullname, uid);
             User user = User.getByUserName(uid);
             if (user == null) {
                 user = new User();
                 user.name = uid;
                 user.fullname = fullname;
                 user.role = Role.getByName("guest");
                 user.authMethod = AuthMethod.getByName("LDAP");
                 user.create();
             }
             return user;
         }
     }
 
 }
