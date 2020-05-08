 package com.morgajel.spoe.model;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 import org.apache.log4j.Logger;
 import org.springframework.format.annotation.DateTimeFormat;
 
 import java.util.Random;
 //import org.hibernate.validator.constraints.impl.EmailValidator;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.hibernate.annotations.NamedQueries;
 import org.hibernate.annotations.NamedQuery;
 import org.hibernate.validator.constraints.Email;
 
 import com.morgajel.spoe.web.RegistrationForm;
 
 @NamedQueries({
 	/**
 	 * Returns an account matching a given username.
 	 */	
 	@NamedQuery(
 		name = "findAccountByUsername",
 		query = "from Account acc where acc.username = :username"
 	),
 	/**
 	 * Returns an account matching a given email address. 
 	 */	
 	@NamedQuery(
 		name = "findAccountByEmail",
 		query = "from Account acc where acc.email = :email"
 	),
 	/**
 	 * Returns an account matching a given username and password. 
 	 */	
 	@NamedQuery(
 		name = "findAccountByUsernameAndPassword",
 		query = "from Account acc where acc.username = :username and acc.password = sha1(:password)"
 	),
 	/**
 	 * Returns an account matching a given username and checksum. 
 	 * The checksum is calculated by concatenating the username password and whether or not the 
 	 * account is enabled.
 	 */	
 	@NamedQuery(
 			name = "findAccountByUsernameAndChecksum",
 			query = "from Account acc where acc.username = :username and sha1(concat(acc.username,acc.password,acc.enabled)) = :checksum" 
 	)
 })
 
 /** 
  * Models a users interaction with the rest of the system.
  */
 @Entity
 @Table(name="account")
 public class Account implements Serializable {
     
    	@NotNull
 	private Long accountId;
 	@NotNull
 	@Size(min = 1, max = 25)
 	private String username;
 	@NotNull
 	@Size(min = 5, max = 255)
 	private String email;
 	@NotNull
 	@Size(min=6, max=255)
 	private String password;
 	@NotNull
 	private Boolean enabled;
 	@NotNull
 	@Size(min=5, max=255)
 	private String firstname;
 	@NotNull
 	@Size(min=5, max=255)
 	private String lastname;
 	@DateTimeFormat
 	private Date lastAccessDate;
 	@DateTimeFormat
 	private Date creationDate;
    	public Set<Role> roles;
    	
 	public static final String ALGORITHM = "SHA1";
 	public static final String PASSWDCHARSET = "!0123456789abcdefghijklmnopqrstuvwxyz";
 	private static final long serialVersionUID = -6987219647522500285L;
 	private transient static Logger logger = Logger.getLogger("com.morgajel.spoe.model.Account");
 	
 	/**
      * Takes a given string and hashes it with ALGORITHM 
      * Same as sha1() in mysql when using ALGORITHM=SHA1; 
      **/
 	public static String hashText(String text) {
 		//TODO factor this out into a utility class
 		StringBuilder hexStr= new StringBuilder();
 		try {
 			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
 			md.reset();
 			byte[] buffer = text.getBytes();
 			md.update(buffer);
 			byte[] digest = md.digest();
 			for (int i = 0; i < digest.length; i++) {
 				hexStr.append( Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 ));
 			}
 		}catch(NoSuchAlgorithmException ex){
 			logger.error("Couldn't find "+ALGORITHM+" to hash the password. ");
 			//This should never ever happen, but needs to be caught.
 			//I'd rather have an unusable password than a blank password.
 			hexStr.append(Account.generatePassword(25)); 
 		}
 		logger.trace("Created hash "+hexStr.toString()+" from "+text);
 		return hexStr.toString();
 	}
 	
 	/**
      * Create a checksum that can be used for activation purposes. Checksum consists 
      * of username + password hash + enabled status hashed together. 
      **/
 	public String activationChecksum(){
 		//TODO can this be refactored with the namedQuery findAccountByUsernameAndChecksum?
 		//converting enabled to int rather than string representation so NamedQuery using mysql int works.
 		Integer enabled= this.enabled?1:0;
 		String checksum=hashText(username+password+enabled);
 		logger.info("create checksum: "+username+" + "+password+" + "+enabled+" = "+checksum);
 		return  checksum;
 	}
 	
 	/**
      * Takes a given string, hashes it, and compares it to the password; returns true if equal 
      **/
 	public boolean verifyPassword(String password){
 		//FIXME is this still needed?
 		if (Account.hashText(password).equals(this.password)){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
      * Constructor for Account class.
      * Disables and sets LastAccessDate and CreationDate, creates a new Set of roles 
      * and disables the account. 
      **/
     public Account() {
     	this.enabled=false;
 	    this.setLastAccessDate(new Date());
 	    this.setCreationDate(new Date());
 	    this.roles= new HashSet<Role>();
     }
 	/**
      * Returns this.accountId, which is the Primary Key for Accounts.  
      **/
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO)
     @Column(name="account_id")
 	public Long getAccountId() {
 		return accountId;
 	}
     
 
 	/**
 	 * Returns all of the roles currently assigned to a user.
 	 **/
     @ManyToMany
     @JoinTable(name="account_role",
 	        joinColumns=@JoinColumn(name="account_id"),
 	        inverseJoinColumns=@JoinColumn(name="role_id")
     )
     public Set<Role> getRoles() {
     	return roles; 
     }
     /**
      * Returns all of the roles currently assigned to a user.
      **/
     public void setRoles( Set<Role> roles) {
 		this.roles= roles;
 	}
 	/**
      * Adds a role to the current set of Role. if roles is null, instantiates a new 
      * HashSet and adds the role to it. 
      **/
 	public void addRole(Role role) {
 		roles.add(role);
 		logger.info("added roll to "+username+", check it out:"+roles);
 	}
 
 	/**
      * Sets this.accountId, which is the Primary Key for Accounts.  
      **/	
 	public void setAccountId(Long accountId) {
 		this.accountId = accountId;
 	}
 	/**
      * Returns this.email, which is required for password recovery and account creation.  
      **/
     @Email
     @Column(name="email", length=255, nullable=false)
 	public String getEmail() {
 		return email;
 	}
     /**
      * Sets this.email, which is required for password recovery and account creation.  
      **/
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	/**
      * Returns this.username, which is required for login.  
      **/
     @Column(name="username")
 	public String getUsername() {
 		return username;
 	}
     /**
      * Sets this.username, which is required for login.  
      **/
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	/**
      * Returns this.password, which is a hashed copy of the user's password.  
      **/
     @Column(name="password")
 	public String getPassword() {
     	logger.debug("getting password:"+this.password);
 		return this.password;
 	}
     /**
      * Sets this.password directly without hashing.  
      **/
 	private void setPassword(String password) {
 		logger.trace("setting password: "+password);
 		this.password = password;
 		logger.trace("password field is now: "+this.password);
 	}
 	/**
      * Sets this.password after hashing the given string.  
      **/
 	public void setHashedPassword(String password) {
 		logger.trace("H setting password: "+password);
 		this.setPassword(Account.hashText(password));
 		logger.trace("H password field is now: "+this.password);
 	}
 	/**
      * Gets this.enabled, which is required for login.  
      **/
     @Column(name="enabled", nullable=false )
 	public Boolean getEnabled() {
 		return enabled;
 	}
 	/**
      * Sets this.enabled, which is required for login.  
      **/
 	public void setEnabled(Boolean enabled) {
 		this.enabled = enabled;
 	}
 	/**
      * Gets this.Firstname, which is rarely ever used except for addressing people.  
      **/
     @Column(name="firstname")
 	public String getFirstname() {
 		return firstname;
 	}
     /**
      * Sets this.Firstname, which is rarely ever used except for addressing people.  
      **/
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 	/**
      * Gets this.Lastname, which is rarely ever used except for addressing people.  
      **/
     @Column(name="lastname")
 	public String getLastname() {
 		return lastname;
 	}
 	/**
      * Sets this.Lastname, which is rarely ever used except for addressing people.  
      **/
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 	/**
      * Gets this.creationDate, the date when the account was created.  
      **/
     @Column(name="creation_date")
 	public Date getCreationDate() {
 		return (Date) creationDate.clone();
 	}
 
     /**
      * Sets this.creationDate, which should only be used once.
      **/
 	public void setCreationDate(Date creationDate) {
 		//TODO: need to enforce this as only happening once; perhaps final?
 		this.creationDate = (Date) creationDate.clone();
 	}
 	/**
      * Gets this.lastAccessDate, which tells when the account was last modified.  
      **/
     @Column(name="last_access_date")
 	public Date getLastAccessDate() {
 		return (Date) lastAccessDate.clone();
 	}
     /**
      * Sets this.lastAccessDate, which tells when the account was last modified.  
      **/
 	public void setLastAccessDate(Date lastAccessDate) {
 		//TODO change this to timestamp in mysql and it will auto update
 		//FIXME should be lastModifiedDate
 		this.lastAccessDate = (Date) lastAccessDate.clone();
 	}
 	/**
 	 * Import registration form to populate data 
 	 */
 	public void importRegistration(RegistrationForm registerForm){
 		firstname=registerForm.getFirstname();
 		lastname=registerForm.getLastname();
 		email=registerForm.getEmail();
 		username=registerForm.getUsername();
 	}
 	/**
      * Overrides the default toString, which should tell you quite a bit about your account.  
      **/
 	@Override
 	public String toString() {
 		//FIXME: I need to update this. perhaps include roles
 		return "Account "
 				+ "[ accountId=" + accountId 
 				+ ", username=" + username
 				+ ", email=" + email
 				+ ", password="	+ password
 				+ ", enabled=" + enabled
 				+ ", firstname=" + firstname
 				+ ", lastname=" + lastname
 				+ ", lastAccessDate=" +	lastAccessDate
 				+ ", creationDate=" + creationDate
 				+ ", PASSWDCHARSET=" + PASSWDCHARSET
 				+ ", ALGORITHM=" + ALGORITHM
 				+  "]";
 	}
 	/**
 	 * Generates a temporary password length characters long using PASSWDCHARSET.   
 	 **/
	public static int MAXLENGTH=25;
 	public static String generatePassword(int length) {
 		//TODO Do I still need this?
 		if (length < 0 || length >25){
 			length=MAXLENGTH;
 		}
         Random rand = new Random(System.currentTimeMillis());
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < length; i++) {
             int pos = rand.nextInt(PASSWDCHARSET.length());
             sb.append(PASSWDCHARSET.charAt(pos));
         }
         return sb.toString();
 	}
 }
