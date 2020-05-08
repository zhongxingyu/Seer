 package edu.gatech.oad.rocket.findmythings.server.model;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.shiro.authc.credential.CredentialsMatcher;
 import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
 import org.apache.shiro.crypto.RandomNumberGenerator;
 import org.apache.shiro.crypto.SecureRandomNumberGenerator;
 import org.apache.shiro.crypto.hash.Sha256Hash;
 import org.apache.shiro.crypto.hash.SimpleHash;
 import org.apache.shiro.util.ByteSource;
 import org.apache.shiro.util.SimpleByteSource;
 
 import com.google.appengine.api.datastore.PhoneNumber;
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Unindex;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * This abstract class specifies features of a AppMember
  *
  * @author TeamRocket
  * */
 @Cache @Unindex @Entity
 public abstract class AppMember implements Serializable {
 
 	private static final long serialVersionUID = -9162883247171299555L;
 
 	protected static final Logger LOGGER = Logger.getLogger(AppMember.class.getName());
 
     private static final int HASH_ITERATIONS = 1;
     private static final String HASH_ALGORITHM = Sha256Hash.ALGORITHM_NAME;
 
     @Id private String email;
 
     private String hashedPassword;
 
     /** The salt, used to make sure that a dictionary attack is harder given a list of all the
      *  hashed passwords, as each salt will be different.
      */
     private byte[] salt;
 
     private Set<String> roles;
 
     private Set<String> permissions;
 
     @Index private Date dateRegistered;
 
     private String name;
 	private String address;
 	private PhoneNumber phone;
 
 	/** Constructors **/
 
     /** For objectify to create instances on retrieval */
	protected AppMember() {
         this.roles = new HashSet<String>();
         this.permissions = new HashSet<String>();
     }
 
     AppMember(String email) {
         this(email, null, new HashSet<String>(), new HashSet<String>());
     }
 
 
     AppMember(String email, String password) {
         this(email, password, new HashSet<String>(), new HashSet<String>());
     }
 
     public AppMember(String email, Set<String> roles, Set<String> permissions) {
         this(email, null, roles, permissions);
     }
 
     public AppMember(String email, String password, Set<String> roles, Set<String> permissions) {
         this(email, password, roles, permissions, false);
     }
 
     AppMember(String email, String password, Set<String> roles, Set<String> permissions, boolean isRegistered) {
         Preconditions.checkNotNull(email, "AppUser email can't be null");
         Preconditions.checkNotNull(roles, "AppUser roles can't be null");
         Preconditions.checkNotNull(permissions, "AppUser permissions can't be null");
         this.email = email;
 
         this.salt = salt().getBytes();
         this.hashedPassword = hash(password, salt);
         this.roles = Collections.unmodifiableSet(roles);
         this.permissions = Collections.unmodifiableSet(permissions);
         this.dateRegistered = isRegistered ? new Date() : null;
     }
 
     /** Definitions **/
 
     public abstract boolean isAdmin();
 
     /**
 	 * returns true if account is locked, false otherwise
 	 * @return locked
 	 */
     public abstract boolean isLocked();
 
     /** Global utilities **/
 
     @Provides @Singleton
     public static CredentialsMatcher getCredentialsMatcher() {
         HashedCredentialsMatcher credentials = new HashedCredentialsMatcher(HASH_ALGORITHM);
         credentials.setHashIterations(HASH_ITERATIONS);
         credentials.setStoredCredentialsHexEncoded(true);
         return credentials;
     }
 
     private static RandomNumberGenerator salter = new SecureRandomNumberGenerator();
 
     private static ByteSource salt() {
         return salter.nextBytes();
     }
 
     private static String hash(String password, byte[] salt) {
         return (password == null) ? null : new SimpleHash(HASH_ALGORITHM, password, new SimpleByteSource(salt), HASH_ITERATIONS).toHex();
     }
 
     /** Object overrides **/
 
     @Override
     public int hashCode() {
         return Objects.hashCode(name, hashedPassword);
     }
 
     @Override
 	public boolean equals(Object o) {
 	if (o instanceof AppMember) {
 		AppMember u = (AppMember)o;
             return Objects.equal(getName().trim(), u.getName().trim()) &&
                    Objects.equal(getHashedPassword(), u.getHashedPassword());
         } else {
             return false;
         }
 	}
 
 	@Override
 	public String toString() {
 		return email;
 	}
 
 	/** Read-only accessors **/
 
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * @return the salt
 	 */
 	public byte[] getSalt() {
 		return salt;
 	}
 
 	/**
 	 * @return the roles
 	 */
 	public Set<String> getRoles() {
 		return roles;
 	}
 
 	/**
 	 * @return the permissions
 	 */
 	public Set<String> getPermissions() {
 		return permissions;
 	}
 
 	public boolean isRegistered() {
         return getDateRegistered() != null;
     }
 
 	/** Mutable accessors **/
 
 	/**
 	 * @return the hashedPassword
 	 */
 	public String getHashedPassword() {
 		return hashedPassword;
 	}
 
 	/**
 	 * @param password the password to set
 	 */
 	public void setPassword(String password) {
         Preconditions.checkNotNull(password);
         this.salt = salt().getBytes();
         this.hashedPassword = hash(password, salt);
     }
 
 	/**
 	 *
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * set name
 	 * @param s
 	 */
 	public void setName(String s) {
 		name = s.trim();
 	}
 
 	/**
 	 * @return the dateRegistered
 	 */
 	public Date getDateRegistered() {
         return dateRegistered == null ? null : new Date(dateRegistered.getTime());
     }
 
 	public void register() {
         dateRegistered = new Date();
     }
 
 	public PhoneNumber getPhoneNumber() {
 		return phone;
 	}
 
 	public void setPhone(PhoneNumber p) {
 		phone = p;
 	}
 
 	/**
 	 *
 	 * @return address
 	 */
 	public String getAddress(){
 		return address;
 	}
 
 	/**
 	 * set address
 	 * @param s
 	 */
 	public void setAddress(String s) {
 		address = s.trim();
 	}
 
 }
