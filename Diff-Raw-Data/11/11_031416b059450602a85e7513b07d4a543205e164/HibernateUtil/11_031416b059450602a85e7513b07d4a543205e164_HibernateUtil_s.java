 package Utils;
 
 import Model.Subjects;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.SessionFactory;
 
 /**
  *
  * @author AgtLucas
  */
 public class HibernateUtil {
     
     public static SessionFactory sessionFactory;
     
     private HibernateUtil() {
         
     }
     
     public static SessionFactory getSessionFactory() {
         if (sessionFactory == null) {
             
             try {
                 Configuration ac = new Configuration();
                 ac.addAnnotatedClass(Subjects.class);
                 sessionFactory = ac.configure().buildSessionFactory();
            } catch (Throwable ex) {
                 System.err.println("Initial Session creation failed :v" + ex);
                 throw new ExceptionInInitializerError(ex);
             }
             return sessionFactory;
         } else {
             return sessionFactory;
         }
     }
     
 }
