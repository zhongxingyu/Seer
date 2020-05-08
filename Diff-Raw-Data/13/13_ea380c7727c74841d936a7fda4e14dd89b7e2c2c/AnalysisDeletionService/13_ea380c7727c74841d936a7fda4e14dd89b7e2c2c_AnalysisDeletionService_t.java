 package org.iplantc.workflow.service;
 
import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.iplantc.hibernate.util.SessionTask;
 import org.iplantc.hibernate.util.SessionTaskWrapper;
 import org.iplantc.workflow.WorkflowException;
 import org.iplantc.workflow.client.ZoidbergClient;
 import org.iplantc.workflow.dao.hibernate.HibernateDaoFactory;
 import org.iplantc.workflow.integration.AnalysisDeleter;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * A service used to delete workflows.
  * 
  * @author Dennis Roberts
  */
 public class AnalysisDeletionService {
    private static final Logger LOG = Logger.getLogger(AnalysisDeletionService.class);
 
     /**
      * The database session factory.
      */
     private SessionFactory sessionFactory;
 
     /**
      * A client used to communicate with Zoidberg.
      */
     private ZoidbergClient zoidbergClient;
 
     /**
      * @param sessionFactory the database session factory.
      */
     public AnalysisDeletionService(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     /**
      * @param zoidbergClient a client used to communicate with Zoidberg.
      */
     public void setZoidbergClient(ZoidbergClient zoidbergClient) {
         this.zoidbergClient = zoidbergClient;
     }
 
     /**
      * Deletes an analysis.
      * 
      * @param jsonString a JSON string describing the analysis to delete.
      * @throws WorkflowException if the analysis can't be deleted for any reason.
      */
     public void deleteAnalysis(final String jsonString) throws WorkflowException {
         new SessionTaskWrapper(sessionFactory).performTask(new SessionTask<Void>() {
             @Override
             public Void perform(Session session) {
                 deleteAnalysis(session, jsonString);
                 return null;
             }
         });
     }
 
     /**
      * Deletes an analysis.
      * 
      * @param session the database session.
      * @param jsonString a JSON string describing the analysis to delete.
      * @throws WorkflowException if the analysis can't be deleted for any reason.
      */
     private void deleteAnalysis(Session session, String jsonString) throws WorkflowException {
         try {
             JSONObject app = new JSONObject(jsonString);
 
            try {
                zoidbergClient.deleteAnalysis(app.optString("user"), app.optString("analysis_id"));
            }
            catch (WorkflowException zoidbergException) {
                // Intentionally ignore zoidberg deletion errors.
                LOG.warn("Could not delete App in Zoidberg", zoidbergException);
            }

             createAnalysisDeleter(session).logicallyDelete(app);
         }
         catch (JSONException e) {
             throw new WorkflowException("invalid analysis deletion request", e);
         }
     }
     
     /**
      * Physically deletes an analysis.
      * 
      * @param jsonString a JSON string representing the analysis to delete.
      * @throws WorkflowException if the analysis can't be deleted for any reason.
      */
     public void physicallyDeleteAnalysis(final String jsonString) throws WorkflowException {
         new SessionTaskWrapper(sessionFactory).performTask(new SessionTask<Void>() {
             @Override
             public Void perform(Session session) {
                 physicallyDeleteAnalysis(session, jsonString);
                 return null;
             }
         });
     }
 
     /**
      * Physically deletes an analysis.
      * 
      * @param session the database session.
      * @param jsonString a JSON string representing the analysis to delete.
      * @throws WorkflowException if the analysis can't be deleted for any reason.
      */
 	private void physicallyDeleteAnalysis(Session session, String jsonString) throws WorkflowException {
         try {
             createAnalysisDeleter(session).physicallyDelete(new JSONObject(jsonString));
         }
         catch (JSONException e) {
             throw new WorkflowException("invalid analysis deletion request", e);
         }
 	}
 
 	/**
 	 * Creates a new analysis deleter.
 	 * 
 	 * @param session the database session.
 	 * @return the analysis deleter.
 	 */
     private AnalysisDeleter createAnalysisDeleter(Session session) {
         return new AnalysisDeleter(new HibernateDaoFactory(session));
     }
 }
