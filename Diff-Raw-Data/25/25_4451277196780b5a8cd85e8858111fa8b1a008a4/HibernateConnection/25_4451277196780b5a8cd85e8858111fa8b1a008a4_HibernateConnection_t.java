 package com.thoughtworks.twu.persistence;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 
 
 public class HibernateConnection {
 
     private static HibernateConnection instance;
     private Session session;
     private SessionFactory sessionFactory;
     private Configuration configuration;
     private ServiceRegistry service;
 
     public static HibernateConnection getInstance() {
         if ( instance == null )
             instance = new HibernateConnection();
 
         return instance;
     }
 
     public HibernateConnection() {
         configDb();
 
     }
 
     private void configDb() {
         configuration = new Configuration().configure();
         service = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
         createSession();
     }
 
     private void createSession() {
         sessionFactory = configuration.buildSessionFactory(service);
         session = sessionFactory.openSession();
     }
 
     public Session getSession() {
        if ( !session.isConnected() || !session.isOpen())
             createSession();
         return session;
     }
 }
