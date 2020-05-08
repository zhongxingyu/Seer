 package nl.bhit.mtor.dao.hibernate;
 
 import java.util.List;
 
 import nl.bhit.mtor.dao.MessageDao;
 import nl.bhit.mtor.model.MTorMessage;
 
 import org.hibernate.Query;
import org.hibernate.SQLQuery;
 import org.springframework.stereotype.Repository;
 
 @Repository("messageDao")
 public class MessageDaoHibernate extends GenericDaoHibernate<MTorMessage, Long> implements MessageDao {
 
 	public MessageDaoHibernate() {
 		super(MTorMessage.class);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<MTorMessage> getMessagesWithTimestamp(MTorMessage message) {
/*		Query query = getSession().createQuery("from MTorMessage where timestamp <= :timeStamp");
				//" and project_fk = :project_id)");
 		query.setDate("timeStamp", message.getTimestamp());
		//query.setLong("project_id", message.getProject().getId());
		return query.list();*/

		String query1 = "select * from message where (TIMESTAMP<='" 
				+ message.getTimestamp().toString().substring(0, 19) + "' and " +
						"PROJECT_FK = " + message.getProject().getId() + ");"; 
		SQLQuery qry = getSession().createSQLQuery(query1);
		qry.addEntity(MTorMessage.class); 
		return qry.list(); 
 	}
 }
