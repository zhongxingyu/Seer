 package de.lbb.mentoring.example.model;
 
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
import javax.persistence.Query;
 
 public class EmployeeTest
 {
 
     private EntityManagerFactory emf;
     private EntityManager em;
 
     @Before
     public void setup()
     {
         emf = Persistence.createEntityManagerFactory("DataModel");
         em = emf.createEntityManager();
     }
 
     @Test
     public void testEmployeeIdentity()
     {
         Employee employee = new Employee();
         employee.setName("Hans");
         employee.setSurename("Mller");
 
         // saving Employee
         em.getTransaction().begin();
         em.persist(employee);
         em.getTransaction().commit();
 
         // loading Employee from same EM
         Employee reloadedEmployee = em.find(Employee.class, employee.getId());
         Assert.assertTrue(employee == reloadedEmployee);
         Assert.assertTrue(employee.equals(reloadedEmployee));
 
         // loading Employee from new EM
         EntityManager otherEM = emf.createEntityManager();
         reloadedEmployee = otherEM.find(Employee.class, employee.getId());
         Assert.assertFalse(employee == reloadedEmployee);
         Assert.assertFalse(employee.equals(reloadedEmployee));
 
         // Implementing .equals and .hashcode to get equality again ...
     }
 
 
 }
