 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package server.utils;
 
 import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
 
 /**
  * Hibernate Utility class with a convenient method to get Session Factory object.
  *
  * @author Markus Mohanty <markus.mo at gmx.net>
  */
 public class HibernateUtil
 {
     private static final SessionFactory sessionFactory;
     
     static
     {
         try
         {
             // Create the SessionFactory from standard (hibernate.cfg.xml) 
             // config file.
            Configuration conf = new Configuration();
            conf.configure();
            ServiceRegistryBuilder registrybuilder = new ServiceRegistryBuilder().applySettings(conf.getProperties());
            sessionFactory = new Configuration().configure().buildSessionFactory(registrybuilder.buildServiceRegistry());
         }
         catch (Throwable ex)
         {
             // Log the exception. 
             System.err.println("Initial SessionFactory creation failed." + ex);
             throw new ExceptionInInitializerError(ex);
         }
     }
     
     public static SessionFactory getSessionFactory()
     {
         return sessionFactory;
     }
 }
