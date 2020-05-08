 package edu.neu.webapp.graphiccodegen.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.neu.webapp.graphiccodegen.model.AppFlowStart;
 import edu.neu.webapp.graphiccodegen.model.FlowNodeStart;
 
 @Component
 public class RunAppDao {
 	// Injected database connection:
 	@PersistenceContext
 	private EntityManager em;
 
 	// Stores a new application
 	@Transactional
 	public void persist(AppFlowStart appFlow) {
 		em.flush();
 		em.persist(appFlow);
 	}
 
 	// Returns a application object whose id is = value of id
 	public AppFlowStart getAppFlowStart(String appID) {
 		//AppFlowStart app = em.find(AppFlowStart.class, Integer.parseInt(id));
 		TypedQuery<AppFlowStart> app = em.createQuery("SELECT a FROM AppFlowStart a where a.application.id=:id", AppFlowStart.class);
 		app.setParameter("id", appID);
 		return app.getSingleResult();
 	}
 
 	// Retrieves all the applications:
 	public List<AppFlowStart> getAllAppFlowStart() {
 		TypedQuery<AppFlowStart> query = em.createQuery(
 				"SELECT a FROM AppFlowStart a", AppFlowStart.class);
 		return query.getResultList();
 	}
 
 	@Transactional
 	public void deleteByAppFlowStartId(String Id) {
 		AppFlowStart app = em.find(AppFlowStart.class, Integer.parseInt(Id));
 		if (app != null) {
 			em.remove(app);
 		}
 	}
 
 	// Returns a application object whose id is = value of id
 	public FlowNodeStart getFlowNodeStart(int flowID) {
 		FlowNodeStart flow = em.find(FlowNodeStart.class,
 				flowID);
 		return flow;
 	}
 
 	// Retrieves all the applications:
 	public List<FlowNodeStart> getAllFlowNodeStart() {
 		TypedQuery<FlowNodeStart> query = em.createQuery(
 				"SELECT a FROM FlowNodeStart a", FlowNodeStart.class);
 		return query.getResultList();
 	}
 
 	@Transactional
 	public void deleteByFlowNodeStartId(String Id) {
 		FlowNodeStart app = em.find(FlowNodeStart.class, Integer.parseInt(Id));
 		if (app != null) {
 			em.remove(app);
 		}
 	}
 
 }
