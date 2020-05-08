 package net.frontlinesms.plugins.learn.data.repository;
 
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 
 import static org.hibernate.criterion.Restrictions.*;
 
 import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
 import net.frontlinesms.plugins.learn.data.domain.Assessment;
 import net.frontlinesms.plugins.learn.data.domain.AssessmentMessage;
 import net.frontlinesms.plugins.learn.data.domain.Topic;
 import net.frontlinesms.plugins.learn.data.domain.TopicItem;
 
 public class TopicItemDao extends BaseHibernateDao<TopicItem> {
 	protected TopicItemDao() {
 		super(TopicItem.class);
 	}
 	
 	public int count() {
 		return super.countAll();
 	}
 	
 	public void delete(TopicItem topicItem) {
 		DetachedCriteria assessmentCriteria = getCriterion(Assessment.class);
 		for(Object o : getHibernateTemplate().findByCriteria(assessmentCriteria)) {
 			Assessment a = (Assessment) o;
 			for(AssessmentMessage message : a.getMessages().toArray(new AssessmentMessage[0])) {
				if(message.getTopicItem().getId() == topicItem.getId()) {
 					a.getMessages().remove(message);
 					getHibernateTemplate().save(a);
 					getHibernateTemplate().delete(message);
 					break;
 				}
 			}
 		}
 		super.delete(topicItem);
 	}
 
 	public List<TopicItem> getAllByTopic(Topic t) {
 		DetachedCriteria criteria = getCriterion();
 		criteria.add(eq("topic", t));
 		return super.getList(criteria );
 	}
 }
