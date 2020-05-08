 /**
  * 
  */
 package de.md.ejb.contact;
 
 import java.util.Date;
 
 import javax.persistence.EntityManager;
 
 import de.md.DatabaseTestCase;
 import de.md.ejb.contact.impl.ContactsBeanImpl;
 import de.md.ejb.contact.model.Contact;
 import de.md.ejb.contact.model.ContactReason;
 
 /**
  * @author dilgerma
  * 
  */
 public class ContactBeanImplTest extends DatabaseTestCase {
 
     private ContactsBeanImpl bean;
     private EntityManager manager;
 
     private final Date testDate = new Date(new java.util.Date().getTime());
 
     private Contact expected = new Contact("HansMueller", "Hans@mueller.de",
 	    testDate, ContactReason.COMMENT, "This is a test");
 
     private Contact expectedChild = new Contact("Child", "Hans@mueller.de",
 	    testDate, ContactReason.COMMENT, "This is a test");
 
     public void setUp() throws Exception {
 	super.setUp();
 	bean = new ContactsBeanImpl();
 	try {
 	    manager = injectEntityManager("wicket-test", "entityManager", bean);
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
 
     }
 
     public void tearDown() {
 	super.tearDown();
     }
 
     public void testPersistContact() throws Exception {
 
 	manager.getTransaction().begin();
 	bean.saveContact(expected);
 	manager.getTransaction().commit();
 	manager.close();
 
 	manager = injectEntityManager("wicket-test", "entityManager", bean);
 	Contact loaded = manager.find(Contact.class, expected.getId());
 
 	Date loadedDate = loaded.getCreationDate();
 	Date eDate = expected.getCreationDate();
 
 	System.out.println(loadedDate.getTime());
 	System.out.println(eDate.getTime());
 
 	assertNotSame(expected, loaded);
 	assertEquals(expected, loaded);
     }
 
     public void testPersistChild() throws Exception {
 
 	manager.getTransaction().begin();
 	bean.saveContact(expected);
 	manager.getTransaction().commit();
 	manager.close();
 
 	manager = injectEntityManager("wicket-test", "entityManager", bean);
 	Contact loaded = manager.find(Contact.class, expected.getId());
 	assertNull(loaded.getParent());
 
 	manager.getTransaction().begin();
 	bean.replyTo(expected, expectedChild);
 	manager.getTransaction().commit();
 	manager.clear();
 	manager.close();
 	
 	manager = injectEntityManager("wicket-test", "entityManager", bean);
 
 	Contact loaded2 = manager.find(Contact.class, expectedChild.getId());
 	assertEquals(expectedChild, loaded2);
 
 	assertEquals(expected, loaded2.getParent());
	assertEquals(2, bean.loadAllContacts().size());
 	assertEquals(2, manager.createQuery("select p from Contact p")
 		.getResultList().size());
     }
 
     protected String getSqlSetUpScript() {
 	return "src/main/resources/META-INF/schema.sql";
     }
 
     protected String getSqlTearDownScript() {
 	return "src/main/resources/META-INF/schema.drop.sql";
     }
 
     protected String getDatabaseProperties() {
 	return "src/main/resources/META-INF/database.properties";
     }
 
 }
