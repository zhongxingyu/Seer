 package org.xezz.timeregistration.dao;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xezz.timeregistration.model.Coworker;
 
 import java.util.Date;
 
 import static org.junit.Assert.*;
 
 
 /**
  * User: Xezz
  * Date: 08.06.13
  * Time: 16:01
  */
 // FIXME: Add logic tests and not only setter/getter tests
 // FIXME: Add tests that fail!
 public class CoworkerDAOTest {
     private final String firstname = "Firstname";
     private final String lastname = "Lastname";
     private final Date creationDate = new Date();
     private final Date lastUpdatedDate = new Date();
     private Long coworkerId = 1337666L;
     private CoworkerDAO dao;
 
     @Before
     public void setUp() throws Exception {
         dao = new CoworkerDAO(coworkerId, firstname, lastname, creationDate, lastUpdatedDate);
     }
 
     @Test
     public void testGetCoworkerId() throws Exception {
         assertEquals("Test of ID equality", coworkerId, dao.getCoworkerId());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testSetCoworkerIdFailsDueToSetId() throws Exception {
         final Long id = dao.getCoworkerId() + 15L;
         dao.setCoworkerId(id);
     }
 
 
     @Test
     public void testSetCoworkerIdOnFreshCoworkerDAO() throws Exception {
         final Long id = dao.getCoworkerId() + 15L;
         CoworkerDAO assertee = new CoworkerDAO();
         assertee.setCoworkerId(id);
        assertEquals("Test ID equality", id, assertee.getCoworkerId());
         assertNotEquals("Test inequality of given ID and preset ID", coworkerId, assertee.getCoworkerId());
     }
 
     @Test
     public void testGetFirstName() throws Exception {
         assertEquals("Test of equal names", firstname, dao.getFirstName());
     }
 
     @Test
     public void testSetFirstName() throws Exception {
         final String toAssert = "new first name";
         dao.setFirstName(toAssert);
         assertEquals("Test setting new first name equality", toAssert, dao.getFirstName());
         assertNotEquals("Test setting new first name non-equality with old first name", firstname, dao.getFirstName());
     }
 
     @Test
     public void testGetLastName() throws Exception {
         assertEquals("Test getting last name", lastname, dao.getLastName());
     }
 
     @Test
     public void testSetLastName() throws Exception {
         final String toAssert = "new last name";
         dao.setLastName(toAssert);
         assertEquals("Test setting new last name equality", toAssert, dao.getLastName());
         assertNotEquals("Test setting new last name non-equality with old last name", lastname, dao.getLastName());
     }
 
     @Test
     public void testGetCreationDate() throws Exception {
         assertEquals("Test getting creation Date", creationDate, dao.getCreationDate());
     }
 
     @Test
     public void testSetCreationDate() throws Exception {
         final Date toAssert = new Date(123456L);
         dao.setCreationDate(toAssert);
         assertEquals("Test setting creation date", toAssert, dao.getCreationDate());
         assertNotEquals("Test setting creation date not equal to old value", creationDate, dao.getCreationDate());
     }
 
     @Test
     public void testGetLastUpdatedDate() throws Exception {
         assertEquals("Test getting creation date", creationDate, dao.getCreationDate());
     }
 
     @Test
     public void testSetLastUpdatedDate() throws Exception {
         final Date toAssert = new Date(1337090L);
         dao.setLastUpdatedDate(toAssert);
         assertEquals("Test setting creation date", toAssert, dao.getLastUpdatedDate());
         assertNotEquals("Test setting creation date not equal to old date", lastUpdatedDate, dao.getLastUpdatedDate());
     }
 
     @Test
     public void testEquals() throws Exception {
         CoworkerDAO toAssert = new CoworkerDAO(coworkerId, firstname, lastname, creationDate, lastUpdatedDate);
         assertTrue("Test equals() with two created objects of same values", toAssert.equals(dao) && dao.equals(toAssert));
 
     }
 
     @Test
     public void testNotEqualsNull() throws Exception {
         assertFalse("Test equals() with two created objects of same values", dao.equals(null));
 
     }
 
     @Test
     public void testNotEquals() throws Exception {
         final String failFirstname = firstname + "Fail Firstname";
         final String failLastname = lastname + "Fail Lastname";
         final Date failCreationDate = new Date(creationDate.getTime() + 1000);
         final Date failLastUpdatedDate = new Date(lastUpdatedDate.getTime() + 1500);
         final Long failCoworkerId = coworkerId + 123;
         assertNotEquals("Equals Not Test: coworkerId", dao, new CoworkerDAO(failCoworkerId, firstname, lastname, creationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: firstName", dao, new CoworkerDAO(coworkerId, failFirstname, lastname, creationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: lastName", dao, new CoworkerDAO(coworkerId, firstname, failLastname, creationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: creationDate", dao, new CoworkerDAO(coworkerId, firstname, lastname, failCreationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: lastUpdatedDate", dao, new CoworkerDAO(coworkerId, firstname, lastname, creationDate, failLastUpdatedDate));
     }
 
     @Test
     public void testNotEqualsNullValues() throws Exception {
         assertNotEquals("Equals Not Test: coworkerId", dao, new CoworkerDAO(null, firstname, lastname, creationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: firstName", dao, new CoworkerDAO(coworkerId, null, lastname, creationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: lastName", dao, new CoworkerDAO(coworkerId, firstname, null, creationDate, lastUpdatedDate));
         assertNotEquals("Equals Not Test: creationDate", dao, new CoworkerDAO(coworkerId, firstname, lastname, null, lastUpdatedDate));
         assertNotEquals("Equals Not Test: lastUpdatedDate", dao, new CoworkerDAO(coworkerId, firstname, lastname, creationDate, null));
     }
 
     @Test
     public void testHashCode() throws Exception {
         final CoworkerDAO assertDao = new CoworkerDAO(coworkerId, firstname, lastname, creationDate, lastUpdatedDate);
         assertEquals("Hashcode equality", dao.hashCode(), assertDao.hashCode());
     }
 
     @Test
     public void testNotHashCode() throws Exception {
         final String failFirstname = firstname + "Fail Firstname";
         final String failLastname = lastname + "Fail Lastname";
         final Date failCreationDate = new Date(creationDate.getTime() + 1000);
         final Date failLastUpdatedDate = new Date(lastUpdatedDate.getTime() + 1500);
         final Long failCoworkerId = coworkerId + 123;
         assertNotEquals("hashCode Not Test: coworkerId", dao.hashCode(), new CoworkerDAO(failCoworkerId, firstname, lastname, creationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: firstName", dao.hashCode(), new CoworkerDAO(coworkerId, failFirstname, lastname, creationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: lastName", dao.hashCode(), new CoworkerDAO(coworkerId, firstname, failLastname, creationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: creationDate", dao.hashCode(), new CoworkerDAO(coworkerId, firstname, lastname, failCreationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: lastUpdatedDate", dao.hashCode(), new CoworkerDAO(coworkerId, firstname, lastname, creationDate, failLastUpdatedDate).hashCode());
     }
 
     @Test
     public void testNotHashCodeNullValues() throws Exception {
         assertNotEquals("hashCode Not Test: coworkerId", dao.hashCode(), new CoworkerDAO(null, firstname, lastname, creationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: firstName", dao.hashCode(), new CoworkerDAO(coworkerId, null, lastname, creationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: lastName", dao.hashCode(), new CoworkerDAO(coworkerId, firstname, null, creationDate, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: creationDate", dao.hashCode(), new CoworkerDAO(coworkerId, firstname, lastname, null, lastUpdatedDate).hashCode());
         assertNotEquals("hashCode Not Test: lastUpdatedDate", dao.hashCode(), new CoworkerDAO(coworkerId, firstname, lastname, creationDate, null).hashCode());
     }
 
     @Test
     public void testDefaultConstructor() throws Exception {
         final CoworkerDAO toAssert = new CoworkerDAO();
         toAssert.setCoworkerId(coworkerId);
         toAssert.setFirstName(firstname);
         toAssert.setLastName(lastname);
         toAssert.setCreationDate(creationDate);
         toAssert.setLastUpdatedDate(lastUpdatedDate);
         assertTrue("Default constructor objects equal", dao.equals(toAssert) && toAssert.equals(dao));
     }
 
     @Test
     public void testDefaultConstructorFails() throws Exception {
         final String failFirstname = firstname + "Fail Firstname";
         final String failLastname = lastname + "Fail Lastname";
         final Date failCreationDate = new Date(creationDate.getTime() + 1000);
         final Date failLastUpdatedDate = new Date(lastUpdatedDate.getTime() + 1500);
         final Long failCoworkerId = coworkerId + 123;
         CoworkerDAO assertDao;
         assertDao = getFailCoworkerDAO(failCoworkerId, firstname, lastname, creationDate, lastUpdatedDate);
         assertNotEquals("Default constructor objects non-equal - ID", dao, assertDao);
         assertDao = getFailCoworkerDAO(coworkerId, failFirstname, lastname, creationDate, lastUpdatedDate);
         assertNotEquals("Default constructor objects non-equal - firstName", dao, assertDao);
         assertDao = getFailCoworkerDAO(coworkerId, firstname, failLastname, creationDate, lastUpdatedDate);
         assertNotEquals("Default constructor objects non-equal - lastName", dao, assertDao);
         assertDao = getFailCoworkerDAO(coworkerId, firstname, lastname, failCreationDate, lastUpdatedDate);
         assertNotEquals("Default constructor objects non-equal - creationDate", dao, assertDao);
         assertDao = getFailCoworkerDAO(coworkerId, firstname, lastname, creationDate, failLastUpdatedDate);
         assertNotEquals("Default constructor objects non-equal - lastUpdatedDate", dao, assertDao);
     }
 
     // Less boilerplate moving the calls of the setters into a dedicated method
     private CoworkerDAO getFailCoworkerDAO(final Long failCoworkerId, final String failFirstname, final String failLastname, final Date failCreationDate, final Date failLastUpdatedDate) {
         final CoworkerDAO assertDao = new CoworkerDAO();
         assertDao.setCoworkerId(failCoworkerId);
         assertDao.setFirstName(failFirstname);
         assertDao.setLastName(failLastname);
         assertDao.setCreationDate(failCreationDate);
         assertDao.setLastUpdatedDate(failLastUpdatedDate);
         return assertDao;
     }
 
     @Test
     public void testEntityConstructor() throws Exception {
         final Coworker createdEntity = new Coworker();
         createdEntity.setCoworkerId(coworkerId);
         createdEntity.setFirstName(firstname);
         createdEntity.setLastName(lastname);
         createdEntity.setCreationDate(creationDate);
         createdEntity.setLastUpdatedDate(lastUpdatedDate);
         final CoworkerDAO assertDao = new CoworkerDAO(createdEntity);
         assertEquals("Entity constructor objects equal", dao, assertDao);
     }
 
 }
