 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DAO;
 
 /**
  *
  * @author Fredy Virguez
  */
 import Model.Appointment;
 import Model.Veterinarian;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 public class DAOVeterinarian {
 
     public void create(Veterinarian veterinarian) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         try {
             em.persist(veterinarian);
             em.getTransaction().commit();
         } catch (Exception e) {
             em.getTransaction().rollback();
         } finally {
             em.close();
         }
 
     }
 
     public Veterinarian read(String ssn) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q = em.createQuery("SELECT v FROM Veterinarian v "
                 + "WHERE v.ssn LIKE :ssn")
                 .setParameter("ssn", ssn);
         Veterinarian veterinarian = null;
         try {
             veterinarian = (Veterinarian) q.getSingleResult();
         } catch (Exception e) {
         } finally {
             em.close();
             return veterinarian;
         }
     }
     
     public List<Veterinarian> readByName(String name) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q;
         q = em.createQuery("SELECT v FROM Veterinarian v "
          + "WHERE v.name LIKE %"+":name"+"%")
           .setParameter("name", name);
         List<Veterinarian> veterinarians = null;
         try {
             veterinarians = q.getResultList();
         } catch (Exception e) {
         } finally {
             em.close();
             return veterinarians;
         }
     }
 
     public boolean update(Veterinarian actual, Veterinarian newV) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         boolean ret = false;
         try {
             actual = read(actual.getSsn());
             actual.setName(newV.getName());
             actual.setLastName(newV.getLastName());
             actual.setBirthdate(newV.getBirthdate());
             actual.setBirthplace(newV.getBirthplace());
             actual.setFocusOnPractice(newV.getFocusOnPractice());
             actual.setAppointments(newV.getAppointments());
             actual.setGraduatedAt(newV.getGraduatedAt());
             actual.setSalary(newV.getSalary());
             actual.setSpecialities(newV.getSpecialities());
             em.merge(actual);
             em.getTransaction().commit();
             ret = true;
         } catch (Exception e) {
             em.getTransaction().rollback();
         } finally {
             em.close();
             return ret;
         }
     }
 
     public boolean delete(Veterinarian veterinarian) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         boolean ret = false;
         try {
             for (Appointment a : veterinarian.getAppointments()) {
                 new DAOAppointment().delete(a);
             }
             Query q = em.createQuery("DELETE FROM Veterinarian v WHERE v.ssn = :ssn")
                     .setParameter("ssn", veterinarian.getSsn());
             q.executeUpdate();
             em.getTransaction().commit();
             ret = true;
         } catch (Exception e) {
             e.printStackTrace();
             em.getTransaction().rollback();
         } finally {
             em.close();
             return ret;
         }
     }
 }
