 package org.devilry.core.session.dao;
 
 import java.util.List;
 
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.devilry.core.entity.Identity;
 import org.devilry.core.entity.User;
 
 public class UserImpl implements UserRemote {
 	@PersistenceContext(unitName = "DevilryCore")
 	protected EntityManager em;
 
 	protected User getUser(long userId) {
 		return em.find(User.class, userId);
 	}
 
 	protected Identity getIdentity(String identity) {
 		return em.find(Identity.class, identity);
 	}
 
 	public void addIdentity(long userId, String identity) {
 		Identity i = new Identity();
 		i.setIdentity(identity);
 		i.setUser(getUser(userId));
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long create(String name, String email, String phoneNumber) {
 		User u = new User();
 		u.setName(name);
 		u.setEmail(email);
 		u.setPhoneNumber(phoneNumber);
 		em.persist(u);
		return 0;
 	}
 
 	public long findUser(String identity) {
 		Identity i = getIdentity(identity);
 		return i.getUser().getId();
 	}
 
 	public String getEmail(long userId) {
 		return getUser(userId).getEmail();
 	}
 
 	public List<String> getIdentities(long userId) {
 		return null;
 	}
 
 	public String getName(long userId) {
 		return getUser(userId).getName();
 	}
 
 	public String getPhoneNumber(long userId) {
 		return getUser(userId).getPhoneNumber();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Long> getUsers() {
 		Query q = em.createQuery("SELECT u.id FROM User u");
 		List<Long> r = q.getResultList();
 		return r;
 	}
 
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void remove(long userId) {
		Query q = em.createQuery("REMOVE FROM Identity i WHERE i.user.id = :userId");
 		q.setParameter("userId", userId);
 		q.executeUpdate();		
 		em.remove(getUser(userId));
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void removeIdentity(long userId, String identity) {
 		Identity i = getIdentity(identity);
 		em.remove(i);
 	}
 
 	public void setEmail(long userId, String email) {
 		getUser(userId).setEmail(email);
 	}
 
 	public void setName(long userId, String name) {
 		getUser(userId).setName(name);
 	}
 
 	public void setPhoneNumber(long userId, String phoneNumber) {
 		getUser(userId).setPhoneNumber(phoneNumber);
 	}
 
 	public boolean emailExists(String email) {
 		Query q = em.createQuery("SELECT u.email FROM User u WHERE u.email = :email");
 		q.setParameter("email", email);
 		List<String> l = q.getResultList();
 		return l.size() == 1;
 	}
 
 	public boolean identityExists(String identity) {
 		Query q = em.createQuery("SELECT i.identity FROM Identity i WHERE i.identity = :identity");
 		q.setParameter("identity", identity);
 		List<String> l = q.getResultList();
 		return l.size() == 1;
 	}
 }
