 package nl.bhit.mtor.dao.hibernate;
 
 import java.util.List;
 
 import nl.bhit.mtor.dao.MessageDao;
 import nl.bhit.mtor.model.MTorMessage;
 
 import org.hibernate.Query;
 import org.springframework.stereotype.Repository;
 
 @Repository("messageDao")
 public class MessageDaoHibernate extends GenericDaoHibernate<MTorMessage, Long> implements MessageDao {
 
 	public MessageDaoHibernate() {
 		super(MTorMessage.class);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<MTorMessage> getMessagesWithTimestamp(MTorMessage message) {
		Query query = getSession().createQuery("from MTorMessage where timestamp <= :timeStamp");
 		query.setDate("timeStamp", message.getTimestamp());
		return query.list();
 	}
 }
