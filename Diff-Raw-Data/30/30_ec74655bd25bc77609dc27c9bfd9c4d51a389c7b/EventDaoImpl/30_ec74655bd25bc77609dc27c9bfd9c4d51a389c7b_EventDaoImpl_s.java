 package envite.model.dao.jpa;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import envite.model.Event;
 import envite.model.User;
 import envite.model.Guest;
 import envite.model.dao.EventDao;
 import envite.model.dao.GuestDao;
 
 @Repository
 public class EventDaoImpl implements EventDao {
 
 	@PersistenceContext
 	private EntityManager entityManager;
 
 	@Autowired
 	GuestDao guestDao;
 
 	@Override
 	@Transactional
     public Event getEvent( Integer id )
     {
         return entityManager.find( Event.class, id );
     }
 
 	@Override
 	@Transactional
 	public List<Event> getEvents( User user ) {
 		return entityManager
 				.createQuery(
 						"SELECT e FROM Event e WHERE e.creator.username LIKE :username order by id",
 						Event.class)
 				.setParameter("username", user.getUsername()).getResultList();
 	}
 
 	@Transactional
     @Override
     public Event saveEvent( Event event ) {
    	for ( Guest guest:  event.getGuests() ) {
    		guestDao.saveGuest( guest );
     		entityManager.flush();
     	}
 
         return entityManager.merge( event );
     }
 }
