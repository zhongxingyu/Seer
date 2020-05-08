 package dao;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
  
 public class HibernateUtils {
 	
 	private static SessionFactory sessionFactory;
 	private static ServiceRegistry serviceRegistry;
  
     // Cre une unique instance de la SessionFactory  partir de hibernate.cfg.xml
     static {
         try {
         	Configuration configuration = new Configuration();
            configuration.configure();
             serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
             sessionFactory = configuration.buildSessionFactory(serviceRegistry);
         } catch (HibernateException ex) {
         	
             throw new RuntimeException("Problme de configuration : " + ex.getMessage(), ex);
         }
     }
  
     // Renvoie une session Hibernate
     public static Session getSession() throws HibernateException {
         return sessionFactory.openSession();
     }
 }
