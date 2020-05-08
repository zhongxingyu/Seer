 package edu.hust.k54.persistence;
 
 // Generated Nov 6, 2012 9:41:48 PM by Hibernate Tools 4.0.0
 
 import java.util.List;
 import javax.naming.InitialContext;

 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.LockMode;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Example;
 
 /**
  * Home object for domain model class Capuy.
  * @see edu.hust.k54.persistence.Capuy
  * @author Hibernate Tools
  */
 public class CapuyHome {
 
 	private static final Log log = LogFactory.getLog(CapuyHome.class);
 
 	private final SessionFactory sessionFactory = getSessionFactory();
	//private final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
 
 	protected SessionFactory getSessionFactory() {
 		try {
 			return (SessionFactory) new InitialContext()
 					.lookup("SessionFactory");
 		} catch (Exception e) {
 			log.error("Could not locate SessionFactory in JNDI", e);
 			throw new IllegalStateException(
 					"Could not locate SessionFactory in JNDI");
 		}
 	}
 
 	public void persist(Capuy transientInstance) {
 		log.debug("persisting Capuy instance");
 		try {
 			sessionFactory.getCurrentSession().persist(transientInstance);
 			log.debug("persist successful");
 		} catch (RuntimeException re) {
 			log.error("persist failed", re);
 			throw re;
 		}
 	}
 
 	public void attachDirty(Capuy instance) {
 		log.debug("attaching dirty Capuy instance");
 		try {
 			sessionFactory.getCurrentSession().saveOrUpdate(instance);
 			log.debug("attach successful");
 		} catch (RuntimeException re) {
 			log.error("attach failed", re);
 			throw re;
 		}
 	}
 
 	public void attachClean(Capuy instance) {
 		log.debug("attaching clean Capuy instance");
 		try {
 			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
 			log.debug("attach successful");
 		} catch (RuntimeException re) {
 			log.error("attach failed", re);
 			throw re;
 		}
 	}
 
 	public void delete(Capuy persistentInstance) {
 		log.debug("deleting Capuy instance");
 		try {
 			sessionFactory.getCurrentSession().delete(persistentInstance);
 			log.debug("delete successful");
 		} catch (RuntimeException re) {
 			log.error("delete failed", re);
 			throw re;
 		}
 	}
 
 	public Capuy merge(Capuy detachedInstance) {
 		log.debug("merging Capuy instance");
 		try {
 			Capuy result = (Capuy) sessionFactory.getCurrentSession().merge(
 					detachedInstance);
 			log.debug("merge successful");
 			return result;
 		} catch (RuntimeException re) {
 			log.error("merge failed", re);
 			throw re;
 		}
 	}
 
 	public Capuy findById(java.lang.Integer id) {
 		log.debug("getting Capuy instance with id: " + id);
 		try {
 			Capuy instance = (Capuy) sessionFactory.getCurrentSession().get(
 					"edu.hust.k54.persistence.Capuy", id);
 			if (instance == null) {
 				log.debug("get successful, no instance found");
 			} else {
 				log.debug("get successful, instance found");
 			}
 			return instance;
 		} catch (RuntimeException re) {
 			log.error("get failed", re);
 			throw re;
 		}
 	}
 
 	public List findByExample(Capuy instance) {
 		log.debug("finding Capuy instance by example");
 		try {
 			List results = sessionFactory.getCurrentSession()
 					.createCriteria("edu.hust.k54.persistence.Capuy")
 					.add(Example.create(instance)).list();
 			log.debug("find by example successful, result size: "
 					+ results.size());
 			return results;
 		} catch (RuntimeException re) {
 			log.error("find by example failed", re);
 			throw re;
 		}
 	}
 }
