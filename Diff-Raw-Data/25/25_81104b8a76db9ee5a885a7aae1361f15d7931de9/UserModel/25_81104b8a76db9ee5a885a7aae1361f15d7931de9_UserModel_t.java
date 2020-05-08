 
 package com.zanoccio.axirassa.domain;
 
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.Query;
 import org.hibernate.annotations.NaturalId;
 import org.hibernate.classic.Session;
 
 import com.zanoccio.axirassa.domain.exception.NoSaltException;
 import com.zanoccio.axirassa.util.HibernateUtil;
 import com.zanoccio.axirassa.util.MessageDigestProvider;
 import com.zanoccio.axirassa.util.RandomStringGenerator;
 
 @Entity
 @Table(name = "Users")
 public class UserModel implements Serializable {
 
 	private static final long serialVersionUID = 1375674968928774909L;
 
 
 	//
 	// Static
 	//
 
 	public static boolean isEmailRegistered(String email) {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 
 		Query query = session.createQuery("select user.Email from UserModel as user where user.Email = ?");
 		query.setString(0, email);
 
 		List results = query.list();
 		session.getTransaction().commit();
 
 		if (results.size() > 0)
 			return true;
 		else
 			return false;
 	}
 
 
 	//
 	// Instance
 	//
 
 	private final Boolean confirmed = false;
 
 	private final Boolean active = true;
 
 	// @Embedded
 	// private Set accessevents;
 
 	// ID
 	private Long id;
 
 
 	@Id
	@Basic
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "user_id")
 	public Long getId() {
 		return id;
 	}
 
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 
 	// ACCOUNTS
 	private Set<AccountModel> accounts;
 
 
	@Basic
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "users", targetEntity = AccountModel.class)
 	public Set<AccountModel> getAccounts() {
 		return accounts;
 	}
 
 
 	public void setAccounts(Set<AccountModel> accounts) {
 		this.accounts = accounts;
 	}
 
 
 	// NAME
 	private String name;
 
 
	@Basic
 	@Column(name = "name")
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	// SALT
 	private String salt;
 
 
	@Basic
 	@Column(name = "salt")
 	public String getSalt() {
 		return salt;
 	}
 
 
 	public void setSalt(String salt) {
 		this.salt = salt;
 	}
 
 
 	private String createSalt() {
 		return RandomStringGenerator.getInstance().randomString(16);
 	}
 
 
 	// PASSWORD
 	private String password;
 
 
	@Basic
 	@Column(name = "password")
 	public String getPassword() {
 		return password;
 	}
 
 
 	/**
 	 * Sets the password for this UserModel by salting and encrypting it
 	 * 
 	 * @param password
 	 * @throws NoSaltException
 	 */
 	public void createPassword(String password) throws NoSaltException {
 		if (salt == null)
 			salt = createSalt();
 
 		this.password = hashPassword(password);
 	}
 
 
 	public void setPassword(String password) throws NoSaltException {
 		this.password = password;
 	}
 
 
 	public String hashPassword(String password) throws NoSaltException {
 		if (salt == null)
 			throw new NoSaltException(this);
 
 		MessageDigest msgdigest = MessageDigestProvider.generate();
 		msgdigest.update(MessageDigestProvider.salt());
 		msgdigest.update(salt.getBytes());
 		msgdigest.update(password.getBytes());
 
 		return new String(msgdigest.digest());
 	}
 
 
 	/**
 	 * @return gives true if the given password matches the recorded password
 	 *         for this user when salted and encrypted.
 	 * @throws NoSaltException
 	 */
 	public boolean matchPassword(String password) throws NoSaltException {
 		return this.password.equals(hashPassword(password));
 	}
 
 
 	// SIGN UP DATE
 	private Date signupdate;
 
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "signupdate")
 	public Date getSignUpDate() {
 		return signupdate;
 	}
 
 
 	public void setSignUpDate(Date signupdate) {
 		this.signupdate = signupdate;
 	}
 
 
 	// EMAIL
 	private String email;
 
 
 	@NaturalId
 	@Column(name = "email")
 	public String getEMail() {
 		return email;
 	}
 
 
 	public void setEMail(String email) {
 		this.email = email;
 
 		if (name == null)
 			name = email;
 	}
 }
