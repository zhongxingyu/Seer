 package ac.hw.services.collabquiz.dao.impl;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HibernateUtil {
     private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
 
     private static final SessionFactory sessionFactory = buildSessionFactory();
 
     private static SessionFactory buildSessionFactory() {
         try {
             return new AnnotationConfiguration()
                     .configure()
                    .addPackage("ac.hw.services.collabquiz.entities")
                     .buildSessionFactory();
 
         } catch (Throwable ex) {
             log.error("Initial SessionFactory creation failed", ex);
             throw new ExceptionInInitializerError(ex);
         }
     }
 
     public static SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 }
