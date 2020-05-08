 package net.latroquette.common.database.session;
 
import org.hibernate.HibernateException;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;
 
 public class HibernateUtils {
 
     private static final SessionFactory sessionFactory = buildSessionFactory();
    private static final String HIBERNATE_CFG_PATH = "/WEB-INF/hibernate.cfg.xml";
    private static SessionFactory buildSessionFactory() { 
    	// Create the SessionFactory from hibernate.cfg.xml
    	//ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder();
    	Configuration config = new Configuration();
    	try{
    		return  config
    				.configure()
    				.buildSessionFactory();
    	}catch (HibernateException ex) {
             // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed. Unable to found " +HIBERNATE_CFG_PATH + ex);
             throw new ExceptionInInitializerError(ex);
         }
        
     }
     
     public static SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
 }
