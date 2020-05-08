 package uk.frequency.glance.server.data_access;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.criterion.Restrictions;
 
 import uk.frequency.glance.server.model.event.Event;
 
 @SuppressWarnings("unchecked")
 public class EventDAL extends GenericDAL<Event>{
 
 	//TODO order by time
 	public List<Event> findByUser(long authorId){
 		Query q = getSession().createQuery("from Event where " +
 				"user.id = :userId " +
 				"order by startTime")
 			.setParameter("userId", authorId);
 		return q.list();
 	}
 	
 	public List<Event> findByTimeRange(long userId, Date start, Date end){
 		Query q = getSession().createQuery("from Event e where " +
 				"e.user.id = :userId " + 
 				"and ((e.startTime >= :start and e.startTime < :end) " +
 					"or (e.endTime >= :start and e.endTime < :end))" +
 					"order by startTime")
 			.setParameter("userId", userId)
 			.setParameter("start", start)
 			.setParameter("end", end);
 		return q.list();
 	}
 	
 	public List<Event> findCreatedAfter(long userId, Date time) {
         return findByCriteria(
         		Restrictions.eq("user.id", userId),
         		Restrictions.gt("creationTime", time));
     }
 	
 	public Event findMostRecent(long userId){
 		Query q = getSession().createQuery("from Event e where " +
 				"e.user.id = :userId " + 
 				"and e.startTime = (select max(e2.startTime) from Event e2 where " +
 					"e2.user.id = :userId)")
 			.setParameter("userId", userId);
		return (Event)q.list().get(0); //TODO there should be just 1 stay/move, but there can possibly be listen, etc at the same time 
 	}
 	
 }
