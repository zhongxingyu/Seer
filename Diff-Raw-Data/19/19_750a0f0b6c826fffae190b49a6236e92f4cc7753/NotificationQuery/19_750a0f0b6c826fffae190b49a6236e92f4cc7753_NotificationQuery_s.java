 package uk.ac.cam.dashboard.queries;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.models.Notification;
 import uk.ac.cam.dashboard.models.NotificationUser;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 
 public class NotificationQuery {
 	
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(NotificationQuery.class);
 	
 	private Criteria criteria;
 	
 	public NotificationQuery() {}
 	
 	public NotificationQuery(Criteria criteria) {
 		this.criteria = criteria;
 	}
 
 	public static NotificationQuery all() {
 		return new NotificationQuery (
 			HibernateUtil.getTransactionSession()
 			.createCriteria(NotificationUser.class)
 			.createAlias("notification", "n")
 			.addOrder(Order.desc("n.timestamp"))
 		);
 	}
 	
 	public static Notification get(int id) {
 		Session session = HibernateUtil.getTransactionSession();
 		Notification n = (Notification) session
 			.createQuery("from Notification where id = :id")
 			.setParameter("id", id)
 			.uniqueResult();
 			
 			return n;
 	}
 	
 	public NotificationQuery byNotification(Notification notification) {
 		log.debug("Getting notification id: " + notification.getId());
 		criteria.add(Restrictions.eq("notification", notification));
 	return this;
 	}
 	
 	public NotificationQuery byUser(User user) {
 			log.debug("Getting notifications for user: " + user.getCrsid());
 			criteria.add(Restrictions.eq("user", user));
 		return this;
 	}
 	
 	public NotificationQuery offset(int offset) {
 		this.criteria.setFirstResult(offset);
 		return this;
 	}
 
 	public NotificationQuery limit(int limit) {
 		this.criteria.setMaxResults(limit);
 		return this;
 	}
 	
 	public NotificationQuery inSection(String section) {
		this.criteria.add(Restrictions.eq("section", section));
 		return this;
 	}
	
 	public NotificationQuery isRead(boolean read) {
		criteria.createAlias("users", "nu")
		.add(Restrictions.eq("nu.read", true));
 		return this;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<NotificationUser> list() {
 		return this.criteria.list();
 	}
 
 	public NotificationUser uniqueResult() {
 		NotificationUser notificationUser = (NotificationUser)criteria.uniqueResult();
 		return notificationUser;
 	}
 	
 //	public HashMap<String, ?> map() {
 //		NotificationUser notificationUser = (NotificationUser)criteria.uniqueResult();
 //		
 //		return notificationUser;
 //	}
 	
 }
