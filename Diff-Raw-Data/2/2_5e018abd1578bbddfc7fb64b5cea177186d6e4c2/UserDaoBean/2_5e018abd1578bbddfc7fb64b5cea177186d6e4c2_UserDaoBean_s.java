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
 
 import usersManagement.BusinessRole;
 import usersManagement.LdapManager;
 import usersManagement.RolePermission;
 import usersManagement.User;
 import annotations.Updated;
 import businessLayer.ChiefScientist;
 import businessLayer.Department;
 
 @Stateful
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
 		/*	crea e aggiorna un utente(eventualmente creando anche il dipartimento se non è gia presente), lancia un evento che segnale la creazione 
 			dell'utente che può essere catturato per creare il chiefScientist corrispondente
 		 */
 		 
 		
 		User foundUser = getBySerialNumber(user.getSerialNumber());
 		if(foundUser !=null){
 	
 			foundUser.copy(user);
 			user = foundUser;
 		}
 		
 		Department d = deptDao.getByCode(user.getDepartment().getCode());
 		if(d != null){
 			user.setDepartment(d);
 		}else{
 			em.persist(user.getDepartment());
 		}
 		
 		em.persist(user);
 		
		if(user.hasRolePermission(RolePermission.PROFESSOR)){
 			profCreationEvent.fire(user);
 		}
 		return user;
 			
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
 		User u = getBySerialNumber(c.getSerialNumber());
 		if(u == null){
 			 u =ldap.getUserBySerial(c.getSerialNumber());
 			//u.addRolePermission(RolePermission.PROFESSOR);
 		}else if(!u.hasRolePermission(RolePermission.PROFESSOR)) {
 			
 			u.addRole(new BusinessRole(RolePermission.PROFESSOR,u.getDepartment()));
 			
 		}
 		
 		create(u);
 	}
 
 
 	@Remove
 	public void close() {
 
 	}
 
 }
