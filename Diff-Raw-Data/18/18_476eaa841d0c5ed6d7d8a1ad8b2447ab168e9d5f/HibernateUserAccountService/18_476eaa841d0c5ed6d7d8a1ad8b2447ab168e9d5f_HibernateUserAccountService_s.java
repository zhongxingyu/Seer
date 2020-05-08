 package messages.repository.user;
 
 import java.security.NoSuchAlgorithmException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import messages.orm.UserAccount;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * User account service manager, uses Hibernate to find, create, update, remove
  * user accounts.
  */
 @Repository
 public class HibernateUserAccountService implements UserAccountService {
 
 	private Log log = LogFactory.getLog(this.getClass());
 	
 	private SessionFactory sessionFactory;
 
 	/**
 	 * Creates a new Hibernate user account service.
 	 * 
 	 * @param sessionFactory the Hibernate session factory
 	 */
 	public HibernateUserAccountService(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	/**
 	 * @see messages.repository.user.UserAccountService#getAllUsers()
 	 */
 	@Transactional(readOnly = true)
 	@SuppressWarnings("unchecked")
 	public List<UserAccount> getAllUsers() {
 		return this.getCurrentSession().createQuery("from UserAccount").list();
 	}
 
 	/**
 	 * @see messages.repository.user.UserAccountService#findByUsername(java.lang.String)
 	 */
 	@Transactional(readOnly = true)
 	public UserAccount findByUsername(String username) {
		return (UserAccount) this.getCurrentSession().load(UserAccount.class, username);
 	}
 
 	/**
 	 * @throws NoSuchAlgorithmException
 	 * @see messages.repository.user.UserAccountService#create(messages.orm.UserAccount)
 	 */
 	@Transactional
 	public void create(UserAccount user) throws NoSuchAlgorithmException {
 		log.info("Create a user: " + user); 
 		user.setTimestamp(new Timestamp(System.currentTimeMillis()));
 
 		// @PrePersist doesn't work with Session API
 		user.preparePasswordSave();
 
 		this.getCurrentSession().persist(user);
 	}
 
 	/**
 	 * @see messages.repository.user.UserAccountService#remove(messages.orm.UserAccount)
 	 */
 	@Transactional
 	public void remove(UserAccount user) {
 		log.info("Remove a user: " + user); 
 		this.getCurrentSession().delete(user);
 		this.getCurrentSession().flush();
 	}
 
 	/**
 	 * @throws NoSuchAlgorithmException
 	 * @see messages.repository.user.UserAccountService#update(messages.orm.UserAccount)
 	 */
 	@Transactional
 	public void update(UserAccount user) throws NoSuchAlgorithmException {
 		log.info("Update a user: " + user); 
 		user.setTimestamp(new Timestamp(System.currentTimeMillis()));
 
 		// @PreUpdate doesn't work with Session API
 		user.preparePasswordSave();
 
 		this.getCurrentSession().update(user);
 	}
 
 	/**
 	 * Returns the session associated with the ongoing user account transaction.
 	 * 
 	 * @return the transactional session
 	 */
 	protected Session getCurrentSession() {
 		return this.sessionFactory.getCurrentSession();
 	}
 }
