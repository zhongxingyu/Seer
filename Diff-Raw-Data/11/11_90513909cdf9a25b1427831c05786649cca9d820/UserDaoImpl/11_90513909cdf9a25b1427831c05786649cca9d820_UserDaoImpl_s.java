 package pl.edu.agh.iosr.brokers;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.annotation.Transactional;
 
 @Transactional
 public class UserDaoImpl implements UserDao {
 
 	private SessionFactory sessionFactory;
 	
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	@Override
 	public User getUser(String name) {
		User user = new User("user", "password");
		sessionFactory.getCurrentSession().save(user);

 		Criteria crit = sessionFactory.getCurrentSession().createCriteria(User.class);	
 		return (User) crit.add(Restrictions.eq("name", name)).setMaxResults(1).list().get(0);
 	}
 
 }
