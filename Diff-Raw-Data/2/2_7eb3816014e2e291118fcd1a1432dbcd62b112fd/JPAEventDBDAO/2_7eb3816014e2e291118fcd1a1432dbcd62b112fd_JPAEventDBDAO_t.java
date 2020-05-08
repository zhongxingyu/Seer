 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.radioaine.database;
 
 import java.sql.Timestamp;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import ohtu.radioaine.domain.Event;
 import ohtu.radioaine.domain.Event3;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author Richard Moneybags
  */
 @Repository
 public class JPAEventDBDAO implements EventDBDao {
     
     @PersistenceContext
     EntityManager entityManager;
     
     @Override
     public List<Event> list() {
         Query q = entityManager.createQuery("SELECT e FROM Event e");
         return q.getResultList();
     }
     
     @Override
     public List<Event> list(String name) {
        Query q = entityManager.createQuery("SELECT e FROM Event e WHERE e.info LIKE'%"+name+"%'");
         return q.getResultList();
     }
 
     @Override
     public Event createOrUpdate(Event instance) {
         return entityManager.merge(instance);
     }
 
     @Override
     public Event read(int id) {
         return entityManager.find(Event.class, id);
     }
 
     @Override
     public void delete(Event instance) {
         entityManager.remove(instance);
     }
 
     @Override
     public Event update(Event instance) {
         return entityManager.merge(instance);
     }
 
     @Override
     public List<Event> listArrivedByDate(Timestamp start, Timestamp end) {
         Query q = entityManager.createQuery("SELECT e FROM Event e WHERE e.arrivalDate between '"+start+"' AND '"+end+"'");
         return q.getResultList();
     }
     
     
     
 }
