 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DAO;
 
 import Model.Owner;
 import Model.Pet;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 /**
  *
  * @author Fredy Virguez
  */
 public class DAOOwner {
 public void create(Owner owner) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         try {
             em.persist(owner);
             em.getTransaction().commit();
         } catch (Exception e) {
             em.getTransaction().rollback();
         } finally {
             em.close();
         }
 
     }
 
     public Owner read(String ssn) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q = em.createQuery("SELECT o FROM Owner o "
                 + "WHERE o.ssn LIKE :ssn")
                 .setParameter("ssn", ssn);
         Owner owner = null;
         try {
             owner = (Owner) q.getSingleResult();
         } catch (Exception e) {
         } finally {
             em.close();
             return owner;
         }
     }
     
     public List<Owner> readByName(String name) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q;
         q = em.createQuery("SELECT o FROM Owner o "
          + "WHERE o.name LIKE %"+":name"+"%")
          .setParameter("name", name);
         List<Owner> owners = null;
         try {
             owners = q.getResultList();
         } catch (Exception e) {
         } finally {
             em.close();
             return owners;
         }
     }
 
     public boolean update(Owner actual, Owner newO) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         boolean ret = false;
         try {
             actual = read(actual.getSsn());
             actual.setName(newO.getName());
             actual.setLastName(newO.getLastName());
             actual.setBirthdate(newO.getBirthdate());
             actual.setBirthplace(newO.getBirthplace());
             actual.setPets(newO.getPets());
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
 
     public boolean delete(Owner owner) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         boolean ret = false;
         try {
             for (Pet p : owner.getPets()) {
                 new DAOPet().delete(p);
             }
             Query q = em.createQuery("DELETE FROM Owner o WHERE o.ssn = :ssn")
                     .setParameter("ssn", owner.getSsn());
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
