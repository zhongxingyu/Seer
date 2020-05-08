 package org.vamdc.portal.session.queryLog;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.log.Log;
 import org.vamdc.portal.entity.query.HttpHeadResponse;
 import org.vamdc.portal.entity.query.Query;
 import org.vamdc.portal.entity.security.User;
 import org.vamdc.portal.session.security.UserInfo;
 
 @Name("persistentQueryLog")
 public class PersistentQueryLog {
 
 	@Logger private Log log;
 	
 	@In private UserInfo auth;
 	@In private EntityManager entityManager;
 	
 	@SuppressWarnings("unchecked")
 	public List<Query> getStoredQueries(){
 		log.info("Reading saved queries");
 		
 		List<Query> queries = null;
 		
 		User user = auth.getUser();
 		
 		if (user!=null){
 			queries=entityManager.createQuery("from Query where user.username =:username").setParameter("username", user.getUsername()).getResultList();
 		}
 		
 		if (queries!=null && queries.size()>0){
 			log.info("Read #0 queries",queries.size());
 			return Collections.unmodifiableList(queries);
 		}
 		return new ArrayList<Query>();
 	}
 
 	public void save(Query query) {
 		deleteStaleResponses(query.getQueryID());
 		entityManager.persist(query);
 		for (HttpHeadResponse response:query.getResponses()){
 			response.setQuery(query);
 			entityManager.persist(response);
 		}
 	}
 
 	private void deleteStaleResponses(Integer queryID){
 		User user = auth.getUser();
 		if (user!=null&& queryID!=null){
 			entityManager.createQuery("delete from HttpHeadResponse where queryID = :queryID")
 					.setParameter("queryID", queryID).executeUpdate();
 		}
 	}
 	
 	public void delete(Integer queryID) {
 		User user = auth.getUser();
 		
 		if (user!=null){
 			Query toRemove = (Query)entityManager.createQuery("from Query where user.username = :username and queryID = :queryID")
 					.setParameter("username",user.getUsername())
 					.setParameter("queryID", queryID).getSingleResult();
 			for (HttpHeadResponse response:toRemove.getResponses())
 				entityManager.remove(response);
 			entityManager.remove(toRemove);
 		}
 	}
 	
 }
