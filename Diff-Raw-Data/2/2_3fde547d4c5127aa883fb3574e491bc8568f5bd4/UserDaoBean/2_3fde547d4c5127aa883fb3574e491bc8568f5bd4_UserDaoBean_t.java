 package daoLayer;
 
 import javax.ejb.Remove;
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceContextType;
 
 import annotations.Updated;
 import businessLayer.ChiefScientist;
 import businessLayer.Department;
 import usersManagement.LdapManager;
 import usersManagement.Role;
 import usersManagement.User;
 
 @Stateful
 @ConversationScoped
 public class UserDaoBean {
 	@PersistenceContext(unitName = "primary", type = PersistenceContextType.EXTENDED)
 	private EntityManager em;
 	@Inject 
 	private DepartmentDaoBean deptDao;
 	
 	@Inject
 	private LdapManager ldap;
 	
 	@Inject
 	@Updated
 	private Event<User> profCreationEvent;
 
 	public UserDaoBean() {}
 
 
 	public User create(User user) {
 		
 		User foundUser = getBySerialNumber(user.getSerialNumber());
 		if(foundUser ==null){
 
 		Department d = deptDao.getByCode(user.getDepartment().getCode());
 		if(d != null){
 			user.addDepartment(d);
 		}else{
 			em.persist(user.getDepartment());
 		}
 		em.persist(user);
 			if(user.hasRole(Role.PROFESSOR)){
 				profCreationEvent.fire(user);
 			}
 			return user;
 		}
 		else{
 			foundUser.copy(user);
			em.persist(foundUser);
 			return foundUser;
 		}
 		
 	}
 
 
 	public void delete(int id) {
 
 		User u = em.find(User.class, id);
 		if (u != null) {
 
 			em.remove(u);
 		}
 	}
 
 	 public User getById(int id) {
 	
 	 return em.find(User.class, id);
 	
 	 }
 
 	public User getBySerialNumber(String serialNumber) {
 		User result = null;
 
 		try {
 			result = em.createNamedQuery("User.findBySerialNumber", User.class).setParameter("number", serialNumber).getSingleResult();
 
 		} catch (NoResultException e) {
 			return null;
 		}
 
 		return result;
 
 	}
 	
 	
 	public void onChiefCreation(@Observes @Updated ChiefScientist c){
 		if(getBySerialNumber(c.getSerialNumber())== null){
 			User u =ldap.getUserBySerial(c.getSerialNumber());
 			u.setRole(Role.PROFESSOR);
 			create(u);
 		}
 	}
 
 
 	@Remove
 	public void close() {
 
 	}
 
 }
