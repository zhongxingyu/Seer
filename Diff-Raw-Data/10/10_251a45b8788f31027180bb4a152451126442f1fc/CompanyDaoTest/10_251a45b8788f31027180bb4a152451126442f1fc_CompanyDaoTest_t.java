 package com.mycompany.dao;
 
import com.mycompany.dao.exception.CompanyConstraintsViolationException;
import com.mycompany.dao.exception.CompanyNotFoundException;
import com.mycompany.dao.exception.DuplicatedCompanyException;
 import com.mycompany.entity.Company;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.mycompany.dao.impl.*;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fermin
  * Date: 3/11/12
  * Time: 18:48
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(MockitoJUnitRunner.class)
 public class CompanyDAOTest {
 
     private CompanyDAO dao;
     private EntityManager em;   // we need to preserve the em to use getTransaction() on it
 
     @Before
     public void setUp() {
         /* Clean database of any previous content */
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HibernateApp");
         EntityManager em = emf.createEntityManager();
         dao = new CompanyDAOImpl(em);
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
     public void createCompanyOk()
             throws DuplicatedCompanyException, CompanyNotFoundException, CompanyConstraintsViolationException {
 
         Company c1, c2;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = dao.create("ACME");
         em.getTransaction().commit();
 
         /* Check that the company is there */
         c2 = dao.load("ACME");
         assertEquals(c1, c2);
     }
 
     @Test
     public void createCompanyNotUniqueNameFails()
             throws DuplicatedCompanyException, CompanyConstraintsViolationException {
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         dao.create("ACME");
         em.getTransaction().commit();
 
         /* Try to insert again the same */
         try {
             em.getTransaction().begin();
             dao.create("ACME");
             em.getTransaction().commit();
             fail();
         }
         catch (CompanyConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void createCompanyNullNameFails() throws DuplicatedCompanyException {
         try {
             em.getTransaction().begin();
             dao.create(null);
             em.getTransaction().commit();
             fail();
         }
         catch (CompanyConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void createCompanyTooLongNameFails() throws DuplicatedCompanyException {
         try {
             em.getTransaction().begin();
             dao.create("looooooooooooooooooooooooooooooooooooooooooooooong");
             em.getTransaction().commit();
             fail();
         }
         catch (CompanyConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void createCompanyTooShortNameFails() throws DuplicatedCompanyException {
         try {
             em.getTransaction().begin();
             dao.create("");
             em.getTransaction().commit();
         }
         catch (CompanyConstraintsViolationException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
     }
 
     @Test
     public void deleteCompanyOk()
             throws DuplicatedCompanyException, CompanyNotFoundException, CompanyConstraintsViolationException {
 
         Company c1, c2;
 
         /* Insert "ACME" company in database */
         em.getTransaction().begin();
         c1 = dao.create("ACME");
         em.getTransaction().commit();
 
         /* Check that "ACME" is there */
         c2 = dao.load("ACME");
         assertEquals(c1, c2);
 
         /* Now delete the "ACME" company */
         em.getTransaction().begin();
         dao.delete("ACME");
         em.getTransaction().commit();
 
         /* Check that the company is not there */
         try {
             dao.load("ACME");
             fail();
         }
         catch (CompanyNotFoundException e) {
             /* If we ends here that means that exception was raised and everything is ok */
         }
 
     }
 
     @Test
     public void deleteCompanyNotExistingFail() {
 
         /* Check that trying to delete a non existing company causes exception */
         try {
             em.getTransaction().begin();
             dao.delete("ACME");
             em.getTransaction().commit();
             fail();
         }
         catch (CompanyNotFoundException e) {
             /* If we ends here that means that exception was raised and everything is ok */
             em.getTransaction().rollback();
         }
 
     }
 
     @Test
     public void findAllCompanies() {
 
             /* Populate 5 companies in database */
             createFive();
 
             /* Search the companies */
             List<Company> l = dao.findAll(5, 0);
 
             /* Check that the list size is ok */
             assertEquals(null, 5, l.size());
 
             /* Check individual elements */
             /* Note that find always return companies ordered by name, no matter how there were inserted */
             assertEquals("ACME",l.get(0).getName());
             assertEquals("Big Evil Corp.",l.get(1).getName());
             assertEquals("OCP",l.get(2).getName());
             assertEquals("Other",l.get(3).getName());
             assertEquals("Weyland Yutani",l.get(4).getName());
 
     }
 
     @Test
     public void findCompaniesPaginationOk() {
 
         /* Populate 5 companies in database */
         createFive();
 
         /* Search the companies */
         List<Company> l = dao.findAll(3, 0);
 
         /* Check that the list size is ok */
         assertEquals(null, 3, l.size());
 
         /* Check individual elements */
         /* Note that find always return companies ordered by name, no matter how there were inserted */
         assertEquals("ACME",l.get(0).getName());
         assertEquals("Big Evil Corp.",l.get(1).getName());
         assertEquals("OCP",l.get(2).getName());
     }
 
     @Test
     public void findCompaniesPaginationAndOffsetOk() {
 
         /* Populate 5 companies in database */
         createFive();
 
         /* Search the companies */
         List<Company> l = dao.findAll(3, 2);
 
         /* Check that the list is ok */
         assertEquals(null, 3, l.size());
         /* Check individual elements */
         /* Note that find always return companies ordered by name, no matter how there were inserted */
         assertEquals("OCP",l.get(0).getName());
         assertEquals("Other",l.get(1).getName());
         assertEquals("Weyland Yutani",l.get(2).getName());
     }
 
     @Test
     public void findCompaniesAllEmpty() {
 
         /* Search the companies */
         List<Company> l = dao.findAll(5, 0);
 
         /* Check that the list size is ok */
         assertEquals(null, 0, l.size());
 
         /* Same result, no matter pagination limit or offset*/
         l = dao.findAll(3, 2);
         assertEquals(null, 0, l.size());
     }
 
     @Test
     public void findCompaniesPaginationWrongLimit() {
 
         /* Populate 5 companies in database */
         createFive();
 
         /* Search with wrong limit */
         List<Company> l = dao.findAll(0, 2);
         assertEquals(null, 0, l.size());
 
         /* Search with wrong offset */
         l = dao.findAll(5, -1);
         assertEquals(null, 0, l.size());
 
     }
 
     @Test
     public void countAllFiveOk() {
         /* Populate 5 companies in database */
         createFive();
 
         /* Count */
         int n = dao.countAll();
 
         /* Check the count is right */
         assertEquals(null, 5, n);
 
     }
 
     @Test
     public void countAllEmptyOk() {
 
         /* Count */
         int n = dao.countAll();
 
         /* Check the count is right */
         assertEquals(null, 0, n);
 
     }
 
     /************
      * Helper methods
      */
 
     private void createFive()  {
 
         /* Insert 5 companies */
         try {
             em.getTransaction().begin();
             dao.create("ACME");
             dao.create("Weyland Yutani");
             dao.create("Big Evil Corp.");
             dao.create("OCP");
             dao.create("Other");
             em.getTransaction().commit();
         }
         catch (Exception e) {
             // By construction, this can not happen
         }
     }
 
 }
