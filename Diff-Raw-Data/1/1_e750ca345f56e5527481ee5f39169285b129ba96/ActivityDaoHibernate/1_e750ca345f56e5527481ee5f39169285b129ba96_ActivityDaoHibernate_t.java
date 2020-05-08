 package es.ficonlan.web.backend.model.activity;
 
 import java.util.List;
 
 import org.hibernate.Query;
 
 import es.ficonlan.web.backend.model.util.dao.GenericDaoHibernate;
 
 /**
  * @author Miguel Ángel Castillo Bellagona
  * @version 1.0
  */
 public class ActivityDaoHibernate extends GenericDaoHibernate<Activity,Integer> implements ActivityDao {
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Activity> findActivitiesByEventByType(long eventId, int type) {
 		// TODO Not Tested
 		
 		String hql = "SELECT u FROM Activity u";
 		if ((eventId != 0) || (type != 0)) hql = hql + " WHERE ";
 		String aux = "";
 		if (eventId != 0) { hql = hql + aux + "(u.category.eventId = :eventId)"; }
		if ((eventId != 0) && (type != 0)) hql = hql + " AND ";
 		if (type != 0)    { hql = hql + aux + "(u.type = :type)";                }
 		
 		hql = hql + " ORDER BY u.dateStart";
 		
 		Query query = getSession().createQuery(hql);
 		
 		if (eventId != 0) { query = query.setParameter("eventId", eventId); }
 		if (type != 0)    { query = query.setParameter("type", type);       }
 		
 		return query.list();
 	}
 
 }
