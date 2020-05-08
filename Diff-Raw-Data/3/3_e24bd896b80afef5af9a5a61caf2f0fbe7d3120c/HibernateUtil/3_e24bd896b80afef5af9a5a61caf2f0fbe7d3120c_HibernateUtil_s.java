 package ICS.SND.Utilities;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 import ICS.SND.Entities.Entry;
 
 public class HibernateUtil {
 
     private static final SessionFactory sessionFactory = buildSessionFactory();
 
     private static SessionFactory buildSessionFactory() {
         try {
             Configuration config = new Configuration().configure("hibernate.cfg.xml");
             config.addPackage("ICS.SND.Entities").addAnnotatedClass(Entry.class);
            SessionFactory factory = config.buildSessionFactory();
            return factory;
         } catch (Throwable ex) {
             // Make sure you log the exception, as it might be swallowed
             System.err.println("Initial SessionFactory creation failed." + ex);
             throw new ExceptionInInitializerError(ex);
         }
     }
 
     public static SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     public static void shutdown() {
         // Close caches and connection pools
         getSessionFactory().close();
     }
 }
