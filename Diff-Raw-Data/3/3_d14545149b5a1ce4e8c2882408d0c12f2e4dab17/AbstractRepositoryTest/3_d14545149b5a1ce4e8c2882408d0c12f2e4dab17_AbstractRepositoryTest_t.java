 package org.lazydog.repository.jpa;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLNonTransientConnectionException;
 import javax.persistence.EntityNotFoundException;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.operation.DatabaseOperation;
 import org.junit.AfterClass;
import static org.junit.Assert.assertNull;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.lazydog.addressbook.AddressBookRepository;
 import org.lazydog.addressbook.model.Address;
 import org.lazydog.addressbook.model.Address2;
 import org.lazydog.repository.Criteria;
 import org.lazydog.repository.criterion.Comparison;
 import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
 
 
 /**
  * Abstract repository test.
  * 
  * @author  Ron Rickard
  */
 public class AbstractRepositoryTest {
 
     private static final String TEST_FILE = "dataset.xml";
     private static IDatabaseConnection connection;
     private static AddressBookRepository repository;
     private static Address expectedAddress1;
     private static Address expectedAddress2;
     private static Address expectedAddress3;
     private static Address2 expectedAddress4;
 
     @BeforeClass
     public static void beforeClass() throws Exception {
 
         // Ensure the derby.log file is in the target directory.
         System.setProperty("derby.system.home", "./target");
         repository = new AddressBookRepository();
         
         // Get the database connection.
         beginTransaction();
         connection = new DatabaseConnection(repository.getEntityManager().unwrap(Connection.class));
         commitTransaction();
  
         expectedAddress1 = new Address();
         expectedAddress1.setCity("Los Angeles");
         expectedAddress1.setId(3);
         expectedAddress1.setState("California");
         expectedAddress1.setStreetAddress("111 Street Avenue");
         expectedAddress1.setZipcode("11111");
 
         expectedAddress2 = new Address();
         expectedAddress2.setCity("Phoenix");
         expectedAddress2.setId(4);
         expectedAddress2.setState("Arizona");
         expectedAddress2.setStreetAddress("222 Street Avenue");
         expectedAddress2.setZipcode("22222");
 
         expectedAddress3 = new Address();
         expectedAddress3.setCity("Denver");
         expectedAddress3.setState("Colorado");
         expectedAddress3.setStreetAddress("333 Street Avenue");
         expectedAddress3.setZipcode("33333");
         
         expectedAddress4 = new Address2();
         expectedAddress4.setCity("Baltimore");
         expectedAddress4.setId(5);
         expectedAddress4.setState("Maryland");
         expectedAddress4.setStreetAddress("444 Street Avenue");
         expectedAddress4.setZipcode("44444");
     }
 
     @AfterClass
     public static void afterClass() throws Exception {
                
         // Close the database connection.
         connection.close();
         
         // Close the entity manager factory.
         repository.getEntityManager().getEntityManagerFactory().close();
         
         try {
             
             // Drop the addressbook database.
             DriverManager.getConnection("jdbc:derby:memory:./target/addressbook;drop=true");
         }
         catch(SQLNonTransientConnectionException e) {
             // Ignore.
         }
     }
     
     @Before
     public void beforeTest() throws Exception {
         
         // Refresh the database with the dataset.
         DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
     }
 
     @Test
     public void testFind() {
         assertReflectionEquals(expectedAddress1, repository.find(Address.class, expectedAddress1.getId()));
     }
 
     @Test
     public void testFindNot() {
         assertNull(repository.find(Address.class, new Integer(2)));
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testFindNullClass() {
         repository.find(null, expectedAddress1.getId());
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testFindNonEntity() {
         repository.find(Address2.class, expectedAddress1.getId());
     }
 
     @Test
     public void testFindByCritera() {
         Criteria<Address> criteria = repository.getCriteria(Address.class);
         criteria.add(Comparison.eq("id", expectedAddress1.getId()));
         assertReflectionEquals(expectedAddress1, repository.find(Address.class, criteria));
     }
 
     @Test
     public void testFindByCriteriaNot() {
         Criteria<Address> criteria = repository.getCriteria(Address.class);
         criteria.add(Comparison.eq("id", new Integer(2)));
         assertNull(repository.find(Address.class, criteria));
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testFindByCriteriaNullClass() {
         Criteria<Address> criteria = repository.getCriteria(Address.class);
         criteria.add(Comparison.eq("id", expectedAddress1.getId()));
         repository.find(null, criteria);
     }
 
     @Test
     public void testGetCriteria() {
         repository.getCriteria(Address.class);
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testGetCriteriaNull() {
         repository.getCriteria(null);
     }
 
     @Test
     public void testPersist() {
         beginTransaction();
         Address actualAddress3 = repository.persist(expectedAddress3);
         commitTransaction();
         expectedAddress3.setId(1);
         assertReflectionEquals(expectedAddress3, actualAddress3);
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testPersistNull() {
         repository.persist(null);
     }
     
     @Test(expected=IllegalArgumentException.class)
     public void testPersistNonEntity() {
         repository.persist(expectedAddress4);
     }
 
     @Test
     public void testRemove() {
         beginTransaction();
         repository.remove(Address.class, expectedAddress1.getId());
         commitTransaction();
         assertNull(repository.find(Address.class, expectedAddress1.getId()));
     }
 
     @Test(expected=EntityNotFoundException.class)
     public void testRemoveNot() {
         beginTransaction();
         repository.remove(Address.class, expectedAddress1.getId());
         commitTransaction();
         assertNull(repository.find(Address.class, expectedAddress1.getId()));
         repository.remove(Address.class, expectedAddress1.getId());
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testRemoveNullClass() {
         repository.remove(null, expectedAddress1.getId());
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testRemoveNullId() {
         repository.remove(Address.class, null);
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void testRemoveNonEntity() {
         repository.remove(Address2.class, expectedAddress1.getId());    
     }
       
     private static void beginTransaction() {
         repository.getEntityManager().getTransaction().begin();
     }
     
     private static void commitTransaction() {
         repository.getEntityManager().getTransaction().commit();
     }
     
     private static IDataSet getDataSet() throws Exception {
         return new FlatXmlDataSetBuilder().build(Thread.currentThread().getContextClassLoader().getResourceAsStream(TEST_FILE));
     }
 }
