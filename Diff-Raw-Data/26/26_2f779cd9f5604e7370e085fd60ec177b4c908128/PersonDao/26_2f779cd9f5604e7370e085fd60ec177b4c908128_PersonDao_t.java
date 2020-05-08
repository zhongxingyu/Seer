 package net.usefulbits.dao;
 
import javax.persistence.EntityManager;

 import net.usefulbits.model.Person;
 
 public interface PersonDao {
     Person getById(long id);
     
     void save(Person person);
 
     void delete(Person person);
 
     void update(Person person);
    
    EntityManager getEntityManager()
 }
