 package edu.gatech.oad.rocket.findmythings.server.db.model;
 
 import com.google.appengine.api.datastore.PhoneNumber;
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import edu.gatech.oad.rocket.findmythings.server.model.AppMutableMember;
 import edu.gatech.oad.rocket.findmythings.server.security.DatabaseRealm;
 import edu.gatech.oad.rocket.findmythings.server.util.HashHelper;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.mgt.RealmSecurityManager;
 import org.apache.shiro.realm.Realm;
 
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

 /**
  * CS 2340 - FindMyStuff Android App
  * This abstract class specifies features of a DBMember
  *
  * @author TeamRocket
  * */
 @Cache @Entity
 public class DBMember implements AppMutableMember {
 
 	private static final long serialVersionUID = -9162883247171299555L;
 
 	protected static final Logger LOGGER = Logger.getLogger(DBMember.class.getName());
 
 	@Id private String email;
 
 	private String hashedPassword;
 
 	/** The salt, used to make sure that a dictionary attack is harder given a list of all the
 	 *  hashed passwords, as each salt will be different.
 	 */
 	private byte[] salt;
 
 	private final Set<String> roles;
 
 	private final Set<String> permissions;
 
 	@Index private Date dateRegistered;
 
 	private String name;
 	private String address;
 	private PhoneNumber phone;
 
 	private boolean isLocked;
 
 	/** Constructors **/
 
 	/** For objectify to create instances on retrieval */
 	protected DBMember() {
 		this.roles = new HashSet<>();
 		this.permissions = new HashSet<>();
 	}
 
 	DBMember(String email) {
 		this(email, null, new HashSet<String>(), new HashSet<String>());
 	}
 
 	public DBMember(String email, String password) {
 		this(email, password, new HashSet<String>(), new HashSet<String>());
 	}
 
 	public DBMember(String email, Set<String> roles, Set<String> permissions) {
 		this(email, null, roles, permissions);
 	}
 
 	public DBMember(String email, String password, Set<String> roles, Set<String> permissions) {
 		this(email, password, roles, permissions, false);
 	}
 
 	public DBMember(String email, String password, Set<String> roles, Set<String> permissions, boolean isRegistered) {
 		Preconditions.checkNotNull(email, "DBMember email can't be null");
 		this.email = email;
 		
 		setPassword(password);
 		
 		if (roles == null) {
 			this.roles = new HashSet<>();
 		} else {
 			this.roles = new HashSet<>(roles);
 		}
 		
 		if (permissions == null) {
 			this.permissions = new HashSet<>();
 		} else {
 			this.permissions = new HashSet<>(permissions);
 		}
 		
 		this.dateRegistered = isRegistered ? new Date() : null;
 	}
 
 	/** Global utilities **/
 
 	/** Object overrides **/
 
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(name, hashedPassword);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof DBMember) {
 			DBMember u = (DBMember)o;
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
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getEmail()
 	 */
 
 	@Override
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * @return the salt
 	 */
 	public byte[] getSalt() {
 		return salt;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getRoles()
 	 */
 	@Override
 	public Collection<String> getRoles() {
 		return roles;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getPermissions()
 	 */
 	@Override
 	public Collection<String> getStringPermissions() {
 		return permissions;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#isRegistered()
 	 */
 	@Override
 	public boolean isRegistered() {
 		return getDateRegistered() != null;
 	}
 
 	@Override
 	public boolean isEditable() {
 		return true;
 	}
 
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
 		this.salt = HashHelper.getSaltedBytes();
 		this.hashedPassword = HashHelper.getHashString(password, getSalt());
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getName()
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#setName(java.lang.String)
 	 */
 	public void setName(String s) {
 		name = s.trim();
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getDateRegistered()
 	 */
 	public Date getDateRegistered() {
 		return dateRegistered == null ? null : new Date(dateRegistered.getTime());
 	}
 
 	public void register() {
 		dateRegistered = new Date();
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getPhoneNumber()
 	 */
 	public PhoneNumber getPhone() {
 		return phone;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#setPhone(com.google.appengine.api.datastore.PhoneNumber)
 	 */
 	public void setPhone(PhoneNumber p) {
 		phone = p;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#getAddress()
 	 */
 	public String getAddress(){
 		return address;
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.gatech.oad.rocket.findmythings.server.db.model.AppMember#setAddress(java.lang.String)
 	 */
 	public void setAddress(String s) {
 		address = s.trim();
 	}
 
 	@Override
 	public void setLocked(boolean locked) {
 		this.isLocked = locked;
 	}
 
 	@Override
 	public void setAdmin(boolean admin) {
 		permissions.clear();
 		roles.clear();
 		if (admin) {
 			roles.add("admin");
 			permissions.add("*");
 		} else {
 			roles.add("user");
 			permissions.add("submit");
 			permissions.add("browse");
 		}
 		
 		RealmSecurityManager manager = (RealmSecurityManager)SecurityUtils.getSecurityManager();
 		for (Realm realm : manager.getRealms()) {
 			// really really bad design
 			if (realm instanceof DatabaseRealm) {
 				((DatabaseRealm) realm).clearCachedAuthorizationInfo(getEmail());
 			}
 		}
 	}
 
 	@Override
 	public boolean isAdmin() {
 		return roles.contains("admin");
 	}
 
 	@Override
 	public boolean isLocked() {
 		return isLocked;
 	}
 }
