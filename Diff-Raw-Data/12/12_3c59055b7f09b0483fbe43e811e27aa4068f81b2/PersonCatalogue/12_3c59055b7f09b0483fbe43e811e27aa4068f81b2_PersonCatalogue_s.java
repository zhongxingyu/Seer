 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.adde.webbapp.model.dao;
 
 import com.adde.webbapp.model.entity.Person;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 /**
  *
  * @author eric
  */
 public class PersonCatalogue extends AbstractDAO<Person, Long> {
 
     public static PersonCatalogue newInstance() {
         return new PersonCatalogue();
     }
 
     private PersonCatalogue() {
         super(Person.class);
     }
     
     @Override
     public void add(Person person) {
         final Calendar dateCreated = new GregorianCalendar();
         person.setDateCreated(dateCreated);
         super.add(person);
     }
     
     public Person getByUserName(String username) {
         EntityManager em = getEntityManager();
         try{
             Query q = em.createQuery("select p from Person p where"
                 + " p.username = :username", Person.class).
                 setParameter("username", username);
            return (Person) q.getSingleResult();
         } catch(NoResultException e){
             return null;
         }
     }
     
     public List<Person> getByFirstName(String firstname) {
         EntityManager em = getEntityManager();
         List<Person> found = em.createQuery("select p from Person p where"
                 + " p.firstname = :firstname", Person.class).
                 setParameter("firstname", firstname).getResultList();
         return found;
     }
 
     public List<Person> getByLastName(String lastname) {
         EntityManager em = getEntityManager();
         List<Person> found = em.createQuery("select p from Person p where"
                 + " p.lastname = :lastname", Person.class).
                 setParameter("lastname", lastname).getResultList();
         return found;
     }
 
     public Person getByEmail(String email) {
         EntityManager em = getEntityManager();
         try{
             Query q = em.createQuery("select p from Person p where"
                 + " p.email = :email", Person.class).
                 setParameter("email", email);
             return (Person) q.getSingleResult();
         } catch(NoResultException e) {
             return null;
         }
     }
 }
