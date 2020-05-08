 package nl.zuyd.marktplaats_revised.repositories;
 
 import java.util.List;
 
 import javax.ejb.Local;
 import javax.ejb.Singleton;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import nl.zuyd.marktplaats_revised.entities.User;
 
 @Local(IUserRepository.class)
 @Singleton
 public class UserRepository implements IUserRepository 
 {
 	@PersistenceContext
 	private EntityManager em;
 	
 	@Override
	public User getById(int id)
 	{
 		return this.em.find(User.class, id);
 	}
 
 	@Override
 	public List<User> getAll()
 	{
 		TypedQuery<User> q = this.em.createQuery("SELECT c FROM User c", User.class);
 		return q.getResultList();
 	}
 	
 	/**
 	 * Find instances of User differentiated by username
 	 * @param username String
 	 * @return
 	 */
 	public User getByUsername(String username) {
		TypedQuery<User> q = this.em.createQuery("SELECT c FROM USER c WHERE c.username = " + username, User.class);
		return q.getSingleResult();
 	}
 }
