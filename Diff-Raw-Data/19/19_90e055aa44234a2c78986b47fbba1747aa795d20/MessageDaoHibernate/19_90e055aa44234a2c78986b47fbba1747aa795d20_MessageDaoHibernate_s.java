 package nl.bhit.mtor.dao.hibernate;
 
 import java.util.List;
 
 import nl.bhit.mtor.dao.MessageDao;
 import nl.bhit.mtor.model.MTorMessage;
 import nl.bhit.mtor.model.User;
 
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
        Query query = getSession().createQuery("from MTorMessage where timestamp <= :timeStamp and project = :project");
        query.setDate("timeStamp", message.getTimestamp());
         query.setLong("project", message.getProject().getId());
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<MTorMessage> getAllByUser(User user) {
         Query query = getSession()
                 .createQuery(
                         "select m as message from MTorMessage as m left join m.project as p left join p.users as u where u = :user");
         query.setLong("user", user.getId());
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<MTorMessage> getAllByUser(User user, boolean resolved) {
         Query query = getSession()
                 .createQuery(
                         "select m as message from MTorMessage as m left join m.project as p left join p.users as u where m.resolved = :resolved AND u = :user");
         query.setLong("user", user.getId());
         query.setBoolean("resolved", resolved);
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public MTorMessage getAliveByProject(Long projectId) {
         String hql = "select m as message from MTorMessage as m left join m.project as p where p = :project and m.content like '%alive%'";
         log.trace("running hql:" + hql);
         Query query = getSession().createQuery(hql);
         query.setLong("project", projectId);
         List<MTorMessage> results = query.list();
         if (results != null && results.size() != 0) {
             log.trace("found message" + results.get(0));
             return results.get(0);
         }
         return null;
     }
 
     @Override
     public List<MTorMessage> getUnresolvedAll(User user) {
         return getUnresolvedAll(user.getId());
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<MTorMessage> getUnresolvedAll(Long userId) {
         Query query = getSession()
                 .createQuery(
                         "select m as message from MTorMessage as m left join m.project as p left join p.users as u where m.resolved=false AND u = :user");
         query.setLong("user", userId);
         return query.list();
     }
 }
