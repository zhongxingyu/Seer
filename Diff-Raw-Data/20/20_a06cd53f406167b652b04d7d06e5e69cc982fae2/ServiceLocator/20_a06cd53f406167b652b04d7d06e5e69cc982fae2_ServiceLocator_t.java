 package com.iknition.firstzk.service;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
import org.zkoss.spring.SpringUtil;

 
 
 public class ServiceLocator {
 	 
     private ServiceLocator() {}
    
 	public static Session getHibernateSession() {
 		return ((SessionFactory) SpringUtil.getBean("sessionFactory", SessionFactory.class)).getCurrentSession();
 	}
 	
 	public static CompanyManager getCompanyManager() {
                System.out.println("singletoooon");
 		return (CompanyManager) SpringUtil.getBean("companyManager", CompanyManager.class);
 	}
 	
 	public static ContactManager getContactManager() {
 		return (ContactManager) SpringUtil.getBean("contactManager", ContactManager.class);
 	}
 }
