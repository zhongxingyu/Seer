 package pl.agh.enrollme.repository;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.agh.enrollme.model.Event;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import java.util.List;
 
 @Repository
 public class EventDAO implements IEventDAO {
 
 	@PersistenceContext
     EntityManager em;
     
     @Transactional
     public void addEvent(Event event) {
         em.persist(event);
     }
 
     // TODO: czy musi byc Transactional?
     @Transactional
     public Integer createEventAndReturnID() {
         Event event = new Event();
         addEvent(event);
         return event.getId();
     }
 
     @Transactional
     public List<Event> listEvents() {
         CriteriaQuery<Event> c = em.getCriteriaBuilder().createQuery(Event.class);
         Root<Event> from = c.from(Event.class);
        c.orderBy(em.getCriteriaBuilder().asc(from.get("name")));
         
         return em.createQuery(c).getResultList();
     }
 
     @Transactional
     public void removeEvent(Integer id) {
         Event event = em.find(Event.class, id);
         if (null != event) {
             em.remove(event);
         }
     }
     
 }
