 package org.xezz.timeregistration.dao;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xezz.timeregistration.model.Coworker;
 
 import java.util.Date;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotEquals;
 
 
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
 
     @Test
     public void testSetCoworkerId() throws Exception {
         final Long id = 123451232L;
         dao.setCoworkerId(id);
         assertEquals("Test ID equality", id, dao.getCoworkerId());
         assertNotEquals("Test inequality of given ID and preset ID", coworkerId, dao.getCoworkerId());
     }
 
     @Test
     public void testGetFirstName() throws Exception {
         assertEquals("Test of equal names", firstname, dao.getFirstName());
     }
 
     @Test
     public void testSetFirstName() throws Exception {
         final String newFirst = "new first name";
         dao.setFirstName(newFirst);
         assertEquals("Test setting new first name equality", newFirst, dao.getFirstName());
         assertNotEquals("Test setting new first name non-equality with old first name", firstname, dao.getFirstName());
     }
 
     @Test
     public void testGetLastName() throws Exception {
         assertEquals("Test getting last name", lastname, dao.getLastName());
     }
 
     @Test
     public void testSetLastName() throws Exception {
         final String newLast = "new last name";
         dao.setLastName(newLast);
         assertEquals("Test setting new last name equality", newLast, dao.getLastName());
         assertNotEquals("Test setting new last name non-equality with old last name", lastname, dao.getLastName());
     }
 
     @Test
     public void testGetCreationDate() throws Exception {
         assertEquals("Test getting creation Date", creationDate, dao.getCreationDate());
     }
 
     @Test
     public void testSetCreationDate() throws Exception {
         final Date date = new Date(123456L);
         dao.setCreationDate(date);
         assertEquals("Test setting creation date", date, dao.getCreationDate());
         assertNotEquals("Test setting creation date not equal to old value", creationDate, dao.getCreationDate());
     }
 
     @Test
     public void testGetLastUpdatedDate() throws Exception {
         assertEquals("Test getting creation date", creationDate, dao.getCreationDate());
     }
 
     @Test
     public void testSetLastUpdatedDate() throws Exception {
         final Date date = new Date(1337090L);
         dao.setLastUpdatedDate(date);
         assertEquals("Test setting creation date", date, dao.getLastUpdatedDate());
         assertNotEquals("Test setting creation date not equal to old date", lastUpdatedDate, dao.getLastUpdatedDate());
     }
 
     @Test
     public void testEquals() throws Exception {
         CoworkerDAO createdDao = new CoworkerDAO(coworkerId, firstname, lastname, creationDate, lastUpdatedDate);
         assertEquals("Test equals() with two created objects of same values", createdDao, dao);
 
     }
 
     @Test
     public void testNotEquals() throws Exception {
         final String failFirstname = "Fail Firstname";
         final String failLastname = "Fail Lastname";
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
     public void testHashCode() throws Exception {
         final CoworkerDAO assertDao = new CoworkerDAO(coworkerId, firstname, lastname, creationDate, lastUpdatedDate);
         assertEquals("Hashcode equality", dao.hashCode(), assertDao.hashCode());
     }
 
     @Test
     public void testNotHashCode() throws Exception {
         final String failFirstname = "Fail Firstname";
         final String failLastname = "Fail Lastname";
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
     public void testDefaultConstructor() throws Exception {
         final CoworkerDAO assertDao = new CoworkerDAO();
         assertDao.setCoworkerId(coworkerId);
         assertDao.setFirstName(firstname);
         assertDao.setLastName(lastname);
         assertDao.setCreationDate(creationDate);
         assertDao.setLastUpdatedDate(lastUpdatedDate);
         assertEquals("Default constructor objects equal", dao, assertDao);
     }
 
     @Test
     public void testDefaultConstructorFails() throws Exception {
         final String failFirstname = "Fail Firstname";
         final String failLastname = "Fail Lastname";
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
 
     private CoworkerDAO getFailCoworkerDAO(Long failCoworkerId, String failFirstname, String failLastname, Date failCreationDate, Date failLastUpdatedDate) {
         CoworkerDAO assertDao = new CoworkerDAO();
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
 
     // TODO: Add tests for failed failed constructor equality
     // TODO: Add tests for equals with null value
 
 }
