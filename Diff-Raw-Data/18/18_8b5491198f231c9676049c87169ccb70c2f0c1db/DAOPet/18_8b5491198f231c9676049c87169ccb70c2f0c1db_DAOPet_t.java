 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DAO;
 
 import Model.MedicalRecord;
 import Model.Owner;
 import Model.Pet;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 /**
  *
  * @author Fredy Virguez
  */
 public class DAOPet {
 
     public void create(Pet pet) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         try {
             em.persist(pet);
             em.getTransaction().commit();
         } catch (Exception e) {
             em.getTransaction().rollback();
         } finally {
             em.close();
         }
     }
 
     public boolean delete(Pet pet) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         boolean ret = false;
         try {
 
             new DAOMedicalRecord().delete(new DAO.DAOMedicalRecord().read(pet));
             pet = read(pet.getName(), pet.getOwner());
             Query q = em.createQuery("DELETE FROM Pet p WHERE p.id = :id")
                     .setParameter("id", pet.getId());
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
 
     public Pet read(String name, Owner owner) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q = em.createQuery("SELECT p FROM Pet p "
                 + "WHERE p.name LIKE :name AND p.owner = :owner")
                 .setParameter("name", name)
                 .setParameter("owner", owner);
         Pet pet = null;
         try {
             pet = (Pet) q.getSingleResult();
         } catch (NonUniqueResultException e) {
             pet = (Pet) q.getResultList().get(0);
         } catch (Exception e) {
         } finally {
             em.close();
             return pet;
         }
     }
 
     public List<Pet> read(Owner owner) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q = em.createQuery("SELECT p FROM Pet p "
                 + "WHERE p.owner = :owner")
                 .setParameter("owner", owner);
         List<Pet> pets = null;
         try {
             pets = q.getResultList();
         } catch (Exception e) {
         } finally {
             em.close();
             return pets;
         }
     }
 
     public List<Pet> readByName(String name) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         Query q;
         q = em.createQuery("SELECT p FROM Pet p "
          + "WHERE p.name LIKE CONCAT('%',:name,'%')")
           .setParameter("name", name);
         List<Pet> pets = null;
         try {
             pets = q.getResultList();
         } catch (Exception e) {
         } finally {
             em.close();
             return pets;
         }
     }
 
     public boolean update(Pet actual, Pet theNew) {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("HPVAS");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         boolean ret = false;
         try {
             actual = read(actual.getName(), actual.getOwner());
             actual.setAge(theNew.getAge());
             actual.setPicture(theNew.getPicture());
             actual.setName(theNew.getName());
             actual.setSpecies(theNew.getSpecies());
             actual.setWeight(theNew.getWeight());
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
 }
