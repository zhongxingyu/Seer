 package com.capgemini.contacts.test.persistence;
 
 import static org.junit.Assert.assertEquals;
 
import java.util.Map;

 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.capgemini.contacts.persistence.dao.ContactDao;
 import com.capgemini.contacts.persistence.model.Contact;
 import com.capgemini.contacts.persistence.test.GenericEntityTest;
 
 public class ContactPersistenceTest extends GenericEntityTest<Contact> {
 
 	ContactDao contacts = null;;
 
 	@Before
 	public void setup() {
 		contacts = new ContactDao(ContactPersistenceTest.emf);
 	}
 
 	@After
 	public void destroy() {
 		contacts = null;
 	}
 
 	/*
 	 * Initialize persistence !
 	 */
 	public ContactPersistenceTest() {
 		super();
 	}
 
 	/**
 	 * Verify Entity persist.
 	 */
 	@Test
 	public void saveContactTest() {
 		Contact contact = new Contact("user01", "user.01@mail.com", "user",
 				"01");
 		contacts.save(contact);
 		assertEquals("Contact was not correctly initialized", false,
 				contact.getContactId()!=null);
 	}
 
 	/**
 	 * 
 	 */
 	@Test
 	public void loadContactFromYamlTest() {
		Map<String,Contact> list = contacts.loadFromYaml("data/contact.yaml");
		assertEquals("contact.yaml are not loaded !",true,list.get("fde").getUsername().equals("fdelorme"));
		assertEquals("contact.yaml are not loaded !",true,list.get("gse").getUsername().equals("gscheibe"));
		assertEquals("contact.yaml are not loaded !",true,list.get("jmu").getUsername().equals("jmuhr"));
 	}
 
 }
