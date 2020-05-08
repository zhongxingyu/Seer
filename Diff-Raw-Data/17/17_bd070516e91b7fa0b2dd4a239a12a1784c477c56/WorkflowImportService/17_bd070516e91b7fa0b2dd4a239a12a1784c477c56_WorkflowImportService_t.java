 package org.iplantc.workflow.service;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.iplantc.hibernate.util.SessionTask;
 import org.iplantc.hibernate.util.SessionTaskWrapper;
 import org.iplantc.workflow.WorkflowException;
 import org.iplantc.workflow.core.TransformationActivity;
 import org.iplantc.workflow.dao.DaoFactory;
 import org.iplantc.workflow.dao.NotificationSetDao;
 import org.iplantc.workflow.dao.hibernate.HibernateDaoFactory;
 import org.iplantc.workflow.dao.hibernate.HibernateNotificationSetDao;
 import org.iplantc.workflow.data.DataElementPreservation;
 import org.iplantc.workflow.data.ImportedWorkflow;
 import org.iplantc.workflow.integration.AnalysisGeneratingTemplateImporter;
 import org.iplantc.workflow.integration.AnalysisImporter;
 import org.iplantc.workflow.integration.DeployedComponentImporter;
 import org.iplantc.workflow.integration.NotificationSetImporter;
 import org.iplantc.workflow.integration.TemplateGroupImporter;
 import org.iplantc.workflow.integration.TemplateImporter;
 import org.iplantc.workflow.integration.WorkflowImporter;
 import org.iplantc.workflow.integration.util.HeterogeneousRegistry;
 import org.iplantc.workflow.integration.util.HeterogeneousRegistryImpl;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * A service used to import workflows.
  * 
  * @author Dennis Roberts
  */
 public class WorkflowImportService {
 
     /**
      * Used to log error messages.
      */
     private static final Logger LOG = Logger.getLogger(WorkflowImportService.class);
 
     /**
      * The database session factory.
      */
     private SessionFactory sessionFactory;
 
     /**
      * The index of the development analysis group.
      */
     private int devAnalysisGroupIndex;
 
     /**
      * The index of the favorites analysis group.
      */
     private int favoritesAnalysisGroupIndex;
 
     /**
      * Used to initialize the user's workspace if necessary.
      */
     private WorkspaceInitializer workspaceInitializer;
 
     /**
      * Initializes a new workflow import service.
      * 
      * @param sessionFactory the database sessionFactory.
      * @param devAnalysisGroupIndex the index of the analysis group used for tool development.
      * @param favoritesAnalysisGroupIndex the index of the analysis group used to store the user's favorites.
      * @param workspaceInitializer used to initialize the user's workspace if necessary.
      */
     public WorkflowImportService(SessionFactory sessionFactory, String devAnalysisGroupIndex,
             String favoritesAnalysisGroupIndex, WorkspaceInitializer workspaceInitializer) {
         this.sessionFactory = sessionFactory;
         this.devAnalysisGroupIndex = parseAnalysisGroupIndex(devAnalysisGroupIndex, "development");
         this.favoritesAnalysisGroupIndex = parseAnalysisGroupIndex(favoritesAnalysisGroupIndex, "favorites");
         this.workspaceInitializer = workspaceInitializer;
     }
 
     /**
      * Parses the development analysis group index, throwing an exception of the index is not a valid integer.
      * 
      * @param devAnalysisGroupIndex the development analysis group index as a string.
      * @param description the analysis description.
      * @return the development analysis group index as an integer.
      */
     private int parseAnalysisGroupIndex(String devAnalysisGroupIndex, String description) {
         try {
             return Integer.parseInt(devAnalysisGroupIndex);
         }
         catch (NumberFormatException e) {
             String msg = "invalid " + description + " analysis group index: " + devAnalysisGroupIndex;
             throw new WorkflowException(msg, e);
         }
     }
 
     /**
      * Creates the object used to import all workflow elements.
      * 
      * @param registry the object registry
      * @param session the hibernate session
      * @param updateVetted true if we should allow vetted analyses to be updated.
      * @return an instance of workflow importer.
      */
     private WorkflowImporter createWorkflowImporter(HeterogeneousRegistry registry, Session session, boolean update,
             boolean updateVetted) {
         WorkflowImporter importer = new WorkflowImporter();
         importer.addImporter("components", createDeployedComponentImporter(session, registry));
         importer.addImporter("templates", createTemplateImporter(session, registry, updateVetted));
         importer.addImporter("analyses", createAnalysisImporter(session, registry, updateVetted));
         importer.addImporter("notification_sets", createNotificationSetImporter(session, registry));
         if (update) {
             importer.enableReplacement();
         }
         return importer;
     }
 
     /**
      * Creates the object used to import template groups.
      * 
      * @param daoFactory used to obtain data access objects.
      * @return the template group importer.
      */
     private TemplateGroupImporter createTemplateGroupImporter(DaoFactory daoFactory) {
         return new TemplateGroupImporter(daoFactory, devAnalysisGroupIndex, favoritesAnalysisGroupIndex);
     }
 
     /**
      * Creates the object used to import analyses (that is, transformation activities).
      * 
      * @param session the database session.
      * @param registry the registry of named objects.
      * @param updateVetted true if we should allow vetted analyses to be updated.
      * @return the analysis importer.
      */
     private AnalysisImporter createAnalysisImporter(Session session, HeterogeneousRegistry registry,
             boolean updateVetted) {
         DaoFactory daoFactory = new HibernateDaoFactory(session);
         TemplateGroupImporter templateGroupImporter = createTemplateGroupImporter(daoFactory);
         AnalysisImporter analysisImporter =
                 new AnalysisImporter(daoFactory, templateGroupImporter, workspaceInitializer, updateVetted);
         analysisImporter.setRegistry(registry);
         return analysisImporter;
     }
 
     /**
      * Creates the object used to import templates.
      * 
      * @param session the database session.
      * @param registry the registry of named workflow elements.
      * @param updateVetted true if we should allow vetted analyses to be updated.
      * @return the template importer.
      */
     private TemplateImporter createTemplateImporter(Session session, HeterogeneousRegistry registry,
             boolean updateVetted) {
         DaoFactory daoFactory = new HibernateDaoFactory(session);
         TemplateImporter templateImporter = new TemplateImporter(daoFactory, updateVetted);
         templateImporter.setRegistry(registry);
         return templateImporter;
     }
 
     /**
      * Creates the object used to import and automatically generate analyses for templates.
      * 
      * @param session the database session.
      * @param registry the registry of named workflow elements.
      * @return the template importer.
      */
     private AnalysisGeneratingTemplateImporter createAnalysisGeneratingTemplateImporter(Session session,
             HeterogeneousRegistry registry) {
         DaoFactory daoFactory = new HibernateDaoFactory(session);
         TemplateGroupImporter templateGroupImporter = createTemplateGroupImporter(daoFactory);
         AnalysisGeneratingTemplateImporter templateImporter = new AnalysisGeneratingTemplateImporter(daoFactory,
                 templateGroupImporter, workspaceInitializer);
         templateImporter.setRegistry(registry);
         return templateImporter;
     }
 
     /**
      * Creates the object used to import notification sets.
      * 
      * @param session the database session.
      * @param registry the registry of named workflow elements.
      * @return the notification set importer.
      */
     private NotificationSetImporter createNotificationSetImporter(Session session, HeterogeneousRegistry registry) {
         NotificationSetDao notificationSetDao = new HibernateNotificationSetDao(session);
         NotificationSetImporter notificationSetImporter = new NotificationSetImporter(notificationSetDao);
         notificationSetImporter.setRegistry(registry);
         return notificationSetImporter;
     }
 
     /**
      * Creates the object used to import deployed components.
      * 
      * @param session the database session.
      * @param registry the registry of named workflow elements.
      * @return the deployed component importer.
      */
     private DeployedComponentImporter createDeployedComponentImporter(Session session, HeterogeneousRegistry registry) {
         DaoFactory daoFactory = new HibernateDaoFactory(session);
         DeployedComponentImporter deployedComponentImporter = new DeployedComponentImporter(daoFactory);
         deployedComponentImporter.setRegistry(registry);
         return deployedComponentImporter;
     }
 
     /**
      * Imports a workflow.
      * 
      * @param jsonString the string representing the JSON object to import.
      * @throws JSONException if the JSON string is invalid or doesn't meet the expectations of the workflow importer.
      */
     public void importWorkflow(String jsonString) throws JSONException {
         importOrUpdateWorkflow(jsonString, false, false);
     }
 
     /**
      * Updates a workflow.
      * 
      * @param jsonString the string representing the JSON object to update.
      * @throws JSONException if the JSON string is invalid or doesn't meet the expectations of the workflow importer.
      */
     public void updateWorkflow(String jsonString) throws JSONException {
         importOrUpdateWorkflow(jsonString, true, false);
     }
 
     /**
      * Forces the update of a workflow.
      * 
      * @param jsonString the string representing the JSON object to update.
      * @throws JSONException if the JSON string is invalid or doesn't meet the expectations of the workflow importer.
      */
     public void forceUpdateWorkflow(String jsonString) throws JSONException {
         importOrUpdateWorkflow(jsonString, true, true);
     }
 
     /**
      * Either imports or updates a workflow. These two operations are similar enough to share a single method.
      * 
      * @param jsonString the string representing the JSON object to import.
      * @param update true if imported objects should replace existing objects with the same name.
      * @param updateVetted true if we should allow vetted analyses to be updated.
      */
     private void importOrUpdateWorkflow(final String jsonString, final boolean update, final boolean updateVetted) {
         new SessionTaskWrapper(sessionFactory).performTask(new SessionTask<Void>() {
             @Override
             public Void perform(Session session) {
                 importOrUpdateWorkflow(session, jsonString, update, updateVetted);
                 return null;
             }
         });
     }
 
     /**
      * Either imports or updates a workflow.
      * 
      * @param session the Hibernate session.
      * @param jsonString the string representing the JSON object to import.
      * @param update true if imported objects should replace existing objects with the same name.
      * @param updateVetted true if we should allow vetted analyses and templates to be updated.
      */
     private void importOrUpdateWorkflow(Session session, String jsonString, boolean update, boolean updateVetted) {
         try {
             HeterogeneousRegistry registry = new HeterogeneousRegistryImpl();
             JSONObject json = new JSONObject(jsonString);
             WorkflowImporter importer = createWorkflowImporter(registry, session, update, updateVetted);
             importer.importWorkflow(json);
             perservationDataElements(registry, session);
             captureImportJson(jsonString, registry, session);
         }
         catch (JSONException e) {
             throw new WorkflowException(e);
         }
         catch (HibernateException e) {
             logHibernateExceptionCause(e);
             throw e;
         }
     }
 
     private void captureImportJson(String jsonString, HeterogeneousRegistry registry, Session session) {
         Collection<TransformationActivity> registeredObjects = registry.getRegisteredObjects(
                 TransformationActivity.class);
         StringBuilder ids = new StringBuilder();
         for (TransformationActivity activity : registeredObjects) {
             ids.append(activity.getId()).append("|");
         }
 
         ImportedWorkflow imp = new ImportedWorkflow();
         imp.setImportedJson(jsonString);
         imp.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
         imp.setAnalysisIds(ids.toString());
 
         session.save(imp);
     }
 
     /*
      * 573 curl -v -H 'Expect:' -d @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow 574 curl -v
      * -H 'Expect:' -d @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow 575 curl -v -H 'Expect:'
      * -d @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow 576 curl -v -H 'Expect:' -d
      * 
      * @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow 577 curl -v -H 'Expect:' -d
      * 
      * @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow 578 curl -v -H 'Expect:' -d
      * 
      * @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow 579 curl -v -H 'Expect:' -d
      * 
      * @/Users/lenards/Desktop/test.json http://localhost:14445/import-workflow
      */
     /**
      * Persists the DataElements specified in an imported Workflow in the DataElementPerservation table for later use.
      * 
      * A DataElement is also known as a DataObject in the model. The general nature of the term caused
      * 
      * This method is a temporary "shunt" that funnels all mention of DataElements into a table in the schema.
      * 
      * @param registry
      * @param session
      */
     private void perservationDataElements(HeterogeneousRegistry registry, Session session) {
         Collection<DataElementPreservation> registeredObjects = registry.getRegisteredObjects(
                 DataElementPreservation.class);
         for (DataElementPreservation dataEl : registeredObjects) {
             session.save(dataEl);
         }
 
     }
 
     /**
      * Imports a template, generating a single-step analysis for it.
      * 
      * @param jsonString the string representing the JSON object to import.
      * @throws JSONException if the JSON string is invalid or doesn't meet the expectations of the importer.
      */
     public void importTemplate(String jsonString) throws JSONException {
         importOrUpdateTemplate(jsonString, false);
     }
 
     /**
      * Updates a template.
      * 
      * @param jsonString the string representing the template to update.
      * @throws JSONException if the JSON string is invalid or doesn't meet the expectations of the importer.
      */
     public void updateTemplate(String jsonString) throws JSONException {
         importOrUpdateTemplate(jsonString, true);
     }
 
     /**
      * Either imports or updates a template.
      * 
      * @param jsonString the string representing the template to import or update.
      * @param update true if existing analyses should be updated.
      */
     private void importOrUpdateTemplate(final String jsonString, final boolean update) {
         new SessionTaskWrapper(sessionFactory).performTask(new SessionTask<Void>() {
             @Override
             public Void perform(Session session) {
                 importOrUpdateTemplate(session, jsonString, update);
                 return null;
             }
         });
     }
 
     /**
      * Either imports or updates a template.
      * 
      * @param session the Hibernate session.
      * @param jsonString the string representing the template to import or update.
      * @param update true if existing analyses should be updated.
      */
     private void importOrUpdateTemplate(Session session, String jsonString, boolean update) {
         try {
             HeterogeneousRegistry registry = new HeterogeneousRegistryImpl();
             JSONObject json = new JSONObject(jsonString);
             AnalysisGeneratingTemplateImporter importer = createAnalysisGeneratingTemplateImporter(session, registry);
             if (update) {
                 importer.enableReplacement();
             }
             importer.importObject(json);
             perservationDataElements(registry, session);
         }
         catch (JSONException e) {
             throw new WorkflowException(e);
         }
         catch (HibernateException e) {
             logHibernateExceptionCause(e);
         }
     }
 
     /**
      * Logs the cause of a Hibernate exception.
      * 
      * @param e the exception.
      */
    private void logHibernateExceptionCause(HibernateException e) throws WorkflowException {
         Throwable currentException = e.getCause();
        Throwable nextException = null;
         while (currentException != null) {
             if (currentException instanceof SQLException) {
                 SQLException sqlException = (SQLException) currentException;
                 if (sqlException.getNextException() != null) {
                     LOG.error("Next Exception: ", sqlException.getNextException());
                    if (nextException == null) {
                        nextException = sqlException.getNextException();
                    }
                 }
             }
             currentException = currentException.getCause();
         }
        if (nextException != null) {
            throw new WorkflowException(nextException.getMessage());
        }
        else {
            throw e;
        }
     }
 }
