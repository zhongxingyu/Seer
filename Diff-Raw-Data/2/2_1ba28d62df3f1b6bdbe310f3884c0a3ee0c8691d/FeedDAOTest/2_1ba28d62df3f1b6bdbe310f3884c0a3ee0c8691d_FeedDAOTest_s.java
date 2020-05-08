 package com.mycompany.dao;
 
 import com.mycompany.dao.exception.*;
 import com.mycompany.dao.impl.CompanyDAOImpl;
 import com.mycompany.dao.impl.FeedDAOImpl;
 import com.mycompany.entity.Company;
 import com.mycompany.entity.Feed;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 8/12/12
  * Time: 17:27
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(MockitoJUnitRunner.class)
 public class FeedDAOTest {
 
     private FeedDAO fDao;
     private CompanyDAO cDao;
     private EntityManager em;   // we need to preserve the em to use getTransaction() on it
 
     @Before
     public void setUp() {
         /* Clean database of any previous content */
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HibernateApp");
         EntityManager em = emf.createEntityManager();
         fDao = new FeedDAOImpl(em);
         cDao = new CompanyDAOImpl(em);
         this.em = em;
     }
 
     @After
     public void tearDown () {
         /* End pending transaction we could have (otherwise the test runner "blocks"
            between test and test */
         if (em.getTransaction().isActive()) {
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void createFeedSameCompanyOk() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             DuplicatedFeedException, FeedConstraintsViolationException, FeedNotFoundException, NullCompanyException {
 
         Company c1;
         Feed f1, f2, f3, f4;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         /* Insert feeds */
         em.getTransaction().begin();
         f1 = fDao.create("employees", c1);
         em.getTransaction().commit();
 
         em.getTransaction().begin();
         f2 = fDao.create("developers", c1);
         em.getTransaction().commit();
 
         /* Check that the feeds are there */
         f3 = fDao.load("employees", c1);
         f4 = fDao.load("developers", c1);
         assertEquals(f1, f3);
         assertEquals(f2, f4);
     }
 
     @Test
     public void createFeedDifferentCompanyOk() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             DuplicatedFeedException, FeedConstraintsViolationException, FeedNotFoundException, NullCompanyException {
 
         Company c1, c2;
         Feed f1, f2, f3, f4;
 
         /* Insert "ACME" and "IBM" companies in database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         em.getTransaction().begin();
         c2 = cDao.create("IBM");
         em.getTransaction().commit();
 
         /* Insert feeds */
         em.getTransaction().begin();
         f1 = fDao.create("employees", c1);
         em.getTransaction().commit();
         em.getTransaction().begin();
         f2 = fDao.create("developers", c2);
         em.getTransaction().commit();
 
         /* Check that the feeds are there */
         f3 = fDao.load("employees", c1);
         f4 = fDao.load("developers", c2);
         assertEquals(f1, f3);
         assertEquals(f2, f4);
 
     }
 
     @Test
     public void createFeedNullCompanyFails() throws DuplicatedFeedException, FeedConstraintsViolationException {
 
         /* Try to insert a feed with null company */
         try {
             em.getTransaction().begin();
             fDao.create("developers", null);
             em.getTransaction().commit();
             fail();
         }
         catch (NullCompanyException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     @Ignore("this will not work until we fix Feed entity")
     public void createFeedSameNameSameCompanyFails() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             DuplicatedFeedException, FeedConstraintsViolationException, NullCompanyException {
 
         Company c1;
         Feed f1, f2;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         /* Insert feeds */
         em.getTransaction().begin();
         f1 = fDao.create("employees", c1);
         em.getTransaction().commit();
 
         /* Try to insert again the same */
         try {
             em.getTransaction().begin();
             fDao.create("employees", c1);
             em.getTransaction().commit();
             fail();
         }
         catch (FeedConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
 
     }
 
     @Test
     public void createFeedNullNameFails() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
         DuplicatedFeedException, NullCompanyException {
 
         Company c1;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         try {
             em.getTransaction().begin();
             fDao.create(null, c1);
             em.getTransaction().commit();
             fail();
         }
         catch (FeedConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void createFeedTooLongNameFails() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
     DuplicatedFeedException, NullCompanyException {
         Company c1;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         try {
             em.getTransaction().begin();
             fDao.create("looooooooooooooooooooooooooooooooooooooooooooooong", c1);
             em.getTransaction().commit();
             fail();
         }
         catch (FeedConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void createFeedTooShortNameFails() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
         DuplicatedFeedException, NullCompanyException {
         Company c1;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         try {
             em.getTransaction().begin();
             fDao.create("", c1);
             em.getTransaction().commit();
             fail();
         }
         catch (FeedConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void deleteFeedOk() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             CompanyNotFoundException, DuplicatedFeedException, FeedConstraintsViolationException, NullCompanyException,
         FeedNotFoundException {
 
         Company c1, c2;
         Feed f1, f2;
 
         /* Insert a feed in the database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
         em.getTransaction().begin();
         f1 = fDao.create("employees", c1);
         em.getTransaction().commit();
 
         /* Check that the feed is there */
         f2 = fDao.load("employees", c1);
         assertEquals(f1, f2);
 
         /* Now delete the feed */
         em.getTransaction().begin();
         fDao.delete("employees", c1);
         em.getTransaction().commit();
 
         /* Check that the feed is not there */
         try {
             fDao.load("employees", c1);
             fail();
         }
         catch (FeedNotFoundException e) {
             /* If we ends here that means that exception was raised and everything is ok */
         }
 
         /* Check that deleting the feed doesn't delete the associated company */
         c2 = cDao.load("ACME");
         assertEquals(c1, c2);
 
     }
 
     @Test
     public void deleteFeedNotExistingFail() throws CompanyConstraintsViolationException, DuplicatedCompanyException,
         CompanyNotFoundException, NullCompanyException {
 
         Company c1;
 
         /* Insert a Feed in the database */
         em.getTransaction().begin();
         c1 = cDao.create("ACME");
         em.getTransaction().commit();
 
         /* Check that trying to delete a non existing feed causes exception */
         try {
             em.getTransaction().begin();
             fDao.delete("employees", c1);
             em.getTransaction().commit();
             fail();
         }
         catch (FeedNotFoundException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void deleteFeedNullCompanyFail() throws FeedNotFoundException {
         /* Check that trying to delete a a feed with null company causes exception */
         try {
             em.getTransaction().begin();
             fDao.delete("employees", null);
             em.getTransaction().commit();
             fail();
         }
         catch (NullCompanyException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void findAllFeeds() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
         NullCompanyException, CompanyNotFoundException {
 
         /* Populate feeds in database */
         createSomeFeeds();
 
         /* Search the feeds */
         Company c1 = cDao.load("IBM");
         List<Feed> l = fDao.findAllByCompany(c1, 4, 0);
 
         /* Check that the list size is ok */
         assertEquals(null, 4, l.size());
 
         /* Check individual elements */
         /* Note that find always return feeds ordered by name, no matter how there were inserted */
         assertEquals("a1",l.get(0).getName());
         assertEquals(c1,l.get(0).getCompany());
         assertEquals("b1",l.get(1).getName());
         assertEquals(c1,l.get(1).getCompany());
         assertEquals("d1",l.get(2).getName());
         assertEquals(c1,l.get(2).getCompany());
         assertEquals("z1",l.get(3).getName());
         assertEquals(c1,l.get(3).getCompany());
 
     }
 
     @Test
     public void findAllFeedsNullCompanyFails() {
 
         try {
             em.getTransaction().begin();
             fDao.findAllByCompany(null, 0, 5);
             em.getTransaction().commit();
             fail();
         }
         catch (NullCompanyException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
 
     }
 
     @Test
     public void findFeedsPaginationOk() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             NullCompanyException, CompanyNotFoundException {
         /* Populate feeds in database */
         createSomeFeeds();
 
         /* Search the feeds */
         Company c1 = cDao.load("IBM");
         List<Feed> l = fDao.findAllByCompany(c1, 3, 0);
 
         /* Check that the list size is ok */
         assertEquals(null, 3, l.size());
 
         /* Check individual elements */
         /* Note that find always return feeds ordered by name, no matter how there were inserted */
         assertEquals("a1",l.get(0).getName());
         assertEquals(c1,l.get(0).getCompany());
         assertEquals("b1",l.get(1).getName());
         assertEquals(c1,l.get(1).getCompany());
         assertEquals("d1",l.get(2).getName());
         assertEquals(c1,l.get(2).getCompany());
 
     }
 
     @Test
     public void findFeedsPaginationAndOffsetOk() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             NullCompanyException, CompanyNotFoundException {
 
         /* Populate feeds in database */
         createSomeFeeds();
 
         /* Search the feeds */
         Company c1 = cDao.load("IBM");
         List<Feed> l = fDao.findAllByCompany(c1, 2, 1);
 
         /* Check that the list size is ok */
         assertEquals(null, 2, l.size());
 
         /* Check individual elements */
         /* Note that find always return feeds ordered by name, no matter how there were inserted */
         assertEquals("b1",l.get(0).getName());
         assertEquals(c1,l.get(0).getCompany());
         assertEquals("d1",l.get(1).getName());
         assertEquals(c1,l.get(1).getCompany());
     }
 
     @Test
     public void findFeedsAllEmpty() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
             NullCompanyException, CompanyNotFoundException {
 
         em.getTransaction().begin();
         Company c1 = cDao.create("IBM");
         em.getTransaction().commit();
 
         /* Search the feeds */
         List<Feed> l = fDao.findAllByCompany(c1, 4, 0);
 
         /* Check that the list size is ok */
         assertEquals(null, 0, l.size());
 
         /* Same result, no matter pagination limit or offset*/
         l = fDao.findAllByCompany(c1, 2, 1);
         assertEquals(null, 0, l.size());
 
     }
 
     @Test
     public void findFeedsPaginationWrongLimit() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
         NullCompanyException, CompanyNotFoundException {
 
         /* Populate feeds in database */
         createSomeFeeds();
 
         /* Search with wrong offset */
         Company c1 = cDao.load("IBM");
         List<Feed> l = fDao.findAllByCompany(c1, 0, 1);
         assertEquals(null, 0, l.size());
 
         /* Search with wrong limit */
         l = fDao.findAllByCompany(c1, 5, -1);
         assertEquals(null, 0, l.size());
 
     }
 
     @Test
    public void countAllFiveOk() throws CompanyNotFoundException, NullCompanyException {
 
         /* Populate feeds in database */
         createSomeFeeds();
 
         /* Count */
         int n = fDao.countAllByCompany(cDao.load("IBM"));
 
         /* Check the count is right */
         assertEquals(null, 4, n);
     }
 
     @Test
     public void countAllNullCompanyFail() {
         try {
             fDao.countAllByCompany(null);
             fail();
         }
         catch (NullCompanyException e) {
             /* If we ends here that means that exception was raised and everything is ok */
         }
     }
 
     @Test
     public void countAllEmptyOk() throws DuplicatedCompanyException, CompanyConstraintsViolationException,
         NullCompanyException {
 
         em.getTransaction().begin();
         Company c1 = cDao.create("IBM");
         em.getTransaction().commit();
 
         /* Count */
         int n = fDao.countAllByCompany(c1);
 
         /* Check the count is right */
         assertEquals(null, 0, n);
     }
 
     /************
      * Helper methods
      */
 
     private void createSomeFeeds()  {
 
         try {
             /* Insert 2 companies */
             Company c1, c2;
             em.getTransaction().begin();
             c1 = cDao.create("ACME");
             c2 = cDao.create("IBM");
             em.getTransaction().commit();
 
             /* Insert 4 feeds per company */
             em.getTransaction().begin();
             fDao.create("a1", c1);
             fDao.create("d1", c1);
             fDao.create("z1", c1);
             fDao.create("b1", c1);
             fDao.create("a1", c2);
             fDao.create("d1", c2);
             fDao.create("z1", c2);
             fDao.create("b1", c2);
             em.getTransaction().commit();
         }
         catch (Exception e) {
             // By construction, this can not happen
         }
     }
 
 }
