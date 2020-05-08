 package service.lds.domain.dao;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Repository;
 import service.lds.domain.dom.Question;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.*;
 
 @Repository
 public class JpaQuestionDao implements QuestionDao {
 
 	private static Logger logger = Logger.getLogger(JpaQuestionDao.class);
 
 	@PersistenceContext
 	EntityManager entityManager;
 
 	@Override
 	public Question findQuestionById(long id) {
 		return entityManager.find(Question.class, id);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Question> findQuestionsByTag(List<String> tags) {
		String query = "select distinct q from Question q left join q.tags t where ";
		for (Iterator<String> i = tags.iterator(); i.hasNext();) {
			query += "t.value='" + i.next() + "'";
			if (i.hasNext()) {
				query += " or ";
			}
		}
        query += " order by q.id ASC";
 		if (logger.isDebugEnabled()) {
 			logger.debug("QUERY: " + query);
 		}
		return entityManager.createQuery(query).getResultList();
 
 	}
 
     @Override
     @SuppressWarnings("unchecked")
     public List<Question> findQuestionsByActiveUncapped() {
         String query = "select q from Question q where q.capped = false and q.active = true";
         return entityManager.createQuery(query).getResultList();
     }
 }
