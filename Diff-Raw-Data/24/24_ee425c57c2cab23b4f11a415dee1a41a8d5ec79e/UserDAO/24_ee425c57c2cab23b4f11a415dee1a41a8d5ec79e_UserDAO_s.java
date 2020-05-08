 
 
 package org.vpac.grisu.fs.model;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.Query;
 import org.vpac.grisu.hibernate.BaseHibernateDAO;
 
 /**
  * This class takes care of storing users and their properties into the a database using hibernate.
  * 
  * @author Markus Binsteiner
  *
  */
 public class UserDAO extends BaseHibernateDAO {
 	
 	private static Logger myLogger = Logger.getLogger(UserDAO.class.getName());
 
 	//TODO improve this (check: http://www.hibernate.org/hib_docs/v3/reference/en/html/objectstate.html#objectstate-modifying)
 	
 	private static String getDN(User user) {
 		return user.getDn();
 	}
 	
 	/**
 	 * Looks up the database whether a user with the specified dn is already 
 	 * persisted
 	 * 
 	 * @param dn the dn of the user
 	 * @return the {@link User} or null if not found
 	 */
 	public User findUserByDN(String dn){
 		myLogger.debug("Loading user with dn: "+dn+" from db.");
 		String queryString = "from org.vpac.grisu.fs.model.User as user where user.dn = ?";
 		Query queryObject = getSession(dn).createQuery(queryString);
 		queryObject.setParameter(0, dn);
 		
 		List users = queryObject.list();
 		
 		// this should never happen
         if ( users.size() > 1 ) throw new HibernateException("Too many users for the dn: "+dn);
         
         if ( users.size() == 0 )
         	return null;
         else
         	return (User)users.get(0);
 	}
 	
 	
     public void save(User transientInstance) {
         myLogger.debug("saving Job instance");
         try {
         	String dn = getDN(transientInstance);
             getSession(dn).save(transientInstance);
             myLogger.debug("save successful");
            getSession(dn).flush();
//            getSession(dn).close();
         } catch (RuntimeException re) {
             myLogger.error("save failed", re);
             throw re;
         }
     }
     
 	public void delete(User persistentInstance) {
         myLogger.debug("deleting Job instance");
         try {
         	String dn = getDN(persistentInstance);
             getSession(dn).delete(persistentInstance);
             myLogger.debug("delete successful");
            getSession(dn).flush();
//            getSession(dn).close();
         } catch (RuntimeException re) {
             myLogger.error("delete failed", re);
             throw re;
         }
     }
 	
     public User merge(User detachedInstance) {
         myLogger.debug("merging Job instance");
         try {
         	String dn = getDN(detachedInstance);
             User result = (User) getSession(dn)
                     .merge(detachedInstance);
             myLogger.debug("merge successful");
            getSession(dn).flush();
//            getSession(dn).close();
             return result;
         } catch (RuntimeException re) {
             myLogger.error("merge failed", re);
             throw re;
         }
     }
 
     public void attachDirty(User instance) {
         myLogger.debug("attaching dirty Job instance");
         try {
         	String dn = getDN(instance);
             getSession(dn).saveOrUpdate(instance);
             myLogger.debug("attach successful");
            getSession(dn).flush();
//            getSession(dn).close();
         } catch (RuntimeException re) {
             myLogger.error("attach failed", re);
             throw re;
         }
     }
     
  
     public void attachClean(User instance) {
         myLogger.debug("attaching clean Job instance");
         try {
         	String dn = getDN(instance);
             getSession(dn).lock(instance, LockMode.NONE);
             myLogger.debug("attach successful");
            getSession(dn).flush();
//            getSession(dn).close();
         } catch (RuntimeException re) {
             myLogger.error("attach failed", re);
             throw re;
         }
     }
 	
 }
