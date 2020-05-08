 /**
  * 
  */
 package com.capgemini.contacts.persistence.dao;
 
 import javax.persistence.EntityManagerFactory;
 
 import com.capgemini.contacts.persistence.GenericDao;
 import com.capgemini.contacts.persistence.model.Contact;
 
 /**
  * @author FDELORME
  * 
  */
 public class ContactDao extends GenericDao<Contact, Long> {
 
 	/**
 	 * Default constructor
 	 */
 	public ContactDao() {
 		super();
 	}
 
 	/**
 	 * Parameterized constructor.
 	 * 
 	 * @param emf
 	 */
 	public ContactDao(String unitName) {
 		super(unitName);
 	}
 
 	/**
 	 * Parameterized constructor.
 	 * 
 	 * @param emf
 	 */
 	public ContactDao(EntityManagerFactory emf) {
 		super(emf);
 	}
 
 
 }
