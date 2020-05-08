 package controllerLayer;
 
 import java.io.Serializable;
 import java.security.GeneralSecurityException;
 
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 
 import security.Principal;
 import security.annotations.CreateUserAllowed;
 import usersManagement.LdapEntityWrapper;
 import usersManagement.User;
 import annotations.Logged;
 import annotations.TransferObj;
 import daoLayer.UserDaoBean;
 
 //TODO splittare in presentation e controller
 @Stateful
 @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
 public class UserControllerBean implements Serializable {
 	private static final long serialVersionUID = -4966124878956728047L;
 
	@Inject
 	private UserDaoBean userDao;
 
 	@PersistenceContext(unitName = "primary", type = PersistenceContextType.EXTENDED)
 	private EntityManager em;
 	
 
 	@Inject
 	private LdapEntityWrapper ldapManager;
 
 
 	@Inject
 	@Logged
 	private Principal loggedUser;
 
 	private User currentUser;
 	
 	private User tempLdapUser;
 
 	public UserControllerBean() {
 		super();
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void save() throws GeneralSecurityException {
 		userDao.create(currentUser);
 	}
 
 	public void cancel() {
 	}
 
 	@CreateUserAllowed
 	public String createUser() {
 		currentUser = new User();
 		return "userWiz";
 	}
 
 
 	public String editLoggedUser() {
 		currentUser = userDao.getBySerialNumber(loggedUser.getSerialNumber());
 		System.out.println("Current User 1: "+currentUser);
 		return "userProfile";
 	}
 
 	@Produces
 	@RequestScoped
 	@TransferObj
 	public User getCurrentUser() {
 		return currentUser;
 	}
 	
 	public void editUser(User u){
 		this.currentUser = em.find(User.class, u.getId());
 	}
 
 	public void setCurrentUser(User currentUser) {
 		this.currentUser = currentUser;
 	}
 	
 	public User getFromDbBySerial(String serial){
 		return userDao.getBySerialNumber(serial);
 	}
 	
 	public User getFromLdapBySerial(String serial){
 		tempLdapUser = ldapManager.getUserBySerial(serial);
 		return tempLdapUser;
 	}
 	
 	@CreateUserAllowed
 	public void importUser(){
 		System.out.println("User controller: importing user " + tempLdapUser);
 		currentUser = tempLdapUser;
 	}
 	
 
 }
