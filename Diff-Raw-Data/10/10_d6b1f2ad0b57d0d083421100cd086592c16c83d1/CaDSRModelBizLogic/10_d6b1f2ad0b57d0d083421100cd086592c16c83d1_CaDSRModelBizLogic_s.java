 package edu.wustl.cab2b.admin.bizlogic;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.log4j.Logger;
 import org.cagrid.cadsr.client.CaDSRUMLModelService;
 import org.cagrid.mms.client.MetadataModelServiceClient;
 import org.cagrid.mms.domain.UMLProjectIdentifer;
 
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.wustl.cab2b.admin.beans.CaDSRModelDetailsBean;
 import edu.wustl.cab2b.admin.beans.LoadModelResult;
 import edu.wustl.cab2b.common.authentication.util.CagridPropertyLoader;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.cab2b.server.path.DomainModelParser;
 import edu.wustl.cab2b.server.path.PathBuilder;
 import edu.wustl.cab2b.server.path.PathFinder;
 import edu.wustl.cab2b.server.util.ConnectionUtil;
 import gov.nih.nci.cadsr.umlproject.domain.Project;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 
 /**
  * @author chetan_patil
  * @author Hrishikesh Rajpathak
  */
 public class CaDSRModelBizLogic {
     private static final int PATH_MAX_LENGTH = 6;
 
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(CaDSRModelBizLogic.class);
 
     /**
      * This method returns the list of model details needed for display.
      * 
      * @return List of CaDSRModelDetailsBean
      * @throws RemoteException f exception occurs while finding projects
      */
     public List<CaDSRModelDetailsBean> getProjectsDisplayDetails() throws RemoteException {
         CaDSRUMLModelService client = getCaDSRService();
         Project[] projects = client.findAllProjects();
         Collection<EntityGroupInterface> cab2bEntityGroups = getCab2bEntityGroups();
 
         List<CaDSRModelDetailsBean> projectDetails = new ArrayList<CaDSRModelDetailsBean>();
         for (Project project : projects) {
             String projectLongName = project.getLongName();
 
             if (projectLongName != null && !isProjectPresent(project, cab2bEntityGroups)) {
                 CaDSRModelDetailsBean caDSRModelDataBean = new CaDSRModelDetailsBean();
                caDSRModelDataBean.setId(project.getId());
                 caDSRModelDataBean.setLongName(projectLongName);
                 caDSRModelDataBean.setDescription(project.getDescription());
                 caDSRModelDataBean.setVersion(project.getVersion());
                 projectDetails.add(caDSRModelDataBean);
             }
         }
         Collections.sort(projectDetails);
         return projectDetails;
     }
 
     /**
      * This method checks if project is already loaded in local application
      * 
      * @param project
      * @param cab2bEntityGroups
      * @return
      */
     private boolean isProjectPresent(Project project, Collection<EntityGroupInterface> cab2bEntityGroups) {
         boolean isPresent = false;
 
         String projectName = Utility.createModelName(project.getLongName(), project.getVersion());
         for (EntityGroupInterface entityGroup : cab2bEntityGroups) {
             String groupName = entityGroup.getName();
 
             if (projectName.compareToIgnoreCase(groupName) == 0) {
                 isPresent = true;
                 break;
             }
         }
 
         return isPresent;
     }
 
     /**
      * This method returns the corresponding domain model form MMS for the given project's long name.
      * 
      * @param projectLongName long name of the project
      * @param version
      * @return domain model
      * @throws RemoteException on invalid project name or could not get the instance of MMSServiceClient
      */
     public DomainModel getDomainModel(String projectLongName, String version, LoadModelResult result) {
         UMLProjectIdentifer project = new UMLProjectIdentifer();
         project.setIdentifier(projectLongName);
         project.setVersion(version);
 
         final String errorMsg = "Unable to get model from MMS for project";
         MetadataModelServiceClient client = getMMSServiceClient();
         DomainModel model = null;
         try {
             model = client.generateDomainModelForProject(project);
         } catch (RemoteException e) {
             handleException(result, e, errorMsg);
 
             String error = "Unable to create MMSServiceClient: " + e.getMessage();
             logger.error(error, e);
             //throw new RuntimeException(error, e);
         }
 
         if (model == null) {
             String error = "Error fetching model from index service";
             logger.error(error, null);
             result.setErrorMessage(error);
             result.setLoaded(false);
         }
 
         return model;
     }
 
     /**
      * This method returns the instance of CaDSRServiceClient
      * 
      * @return Object of type CaDSRServiceClient
      * @throws RemoteException on error while getting caDSR service client or caDSR service URL is incorrect
      */
     private CaDSRUMLModelService getCaDSRService() {
         CaDSRUMLModelService client = null;
         String url = CagridPropertyLoader.getCaDSRUrl();
 
         try {
             client = new CaDSRUMLModelService(url);
         } catch (MalformedURIException e) {
             String error = "caDSR URL " + url + " is incorrect: " + e.getMessage();
             logger.error(error, e);
             throw new RuntimeException(error, e);
         } catch (RemoteException e) {
             String error = "Unable to create CaDSRServiceClient" + e.getMessage();
             logger.error(error, e);
             throw new RuntimeException(error, e);
         }
 
         return client;
     }
 
     /**
      * This method returns the instance of MMSServiceClient
      * 
      * @return Object of type MMSServiceClient
      * @throws RemoteException on error while getting caDSR service client or MMS service URL is incorrect
      */
     private MetadataModelServiceClient getMMSServiceClient() {
         MetadataModelServiceClient client = null;
         String url = CagridPropertyLoader.getMMSUrl();
 
         try {
             client = new MetadataModelServiceClient(url);
         } catch (MalformedURIException e) {
             String error = "MMS URL " + url + " is incorrect: " + e.getMessage();
             logger.error(error, e);
             throw new RuntimeException(error, e);
         } catch (RemoteException e) {
             String error = "Unable to create MMSServiceClient" + e.getMessage();
             logger.error(error, e);
             throw new RuntimeException(error, e);
         }
 
         return client;
     }
 
     /**
      * This method saves the selected domain models in data bases. Also does
      * path forming and saving operations.
      * 
      * @param modelsToLoad
      * @return Map of String(model_name) -> LoadModelResult
      */
     public Map<String, LoadModelResult> persistDomainModel(Collection<CaDSRModelDetailsBean> modelsToLoad) {
         Map<String, LoadModelResult> models = new HashMap<String, LoadModelResult>();
 
         Connection connection = null;
         try {
             connection = ConnectionUtil.getConnection();
 
             for (CaDSRModelDetailsBean model : modelsToLoad) {
                 LoadModelResult result = new LoadModelResult(model, true, "", null);
 
                DomainModel domainModel = getDomainModel(model.getLongName(), model.getVersion(), result);
                 if (domainModel != null) {
                     DomainModelParser parser = new DomainModelParser(domainModel);
                     try {
                         PathBuilder.loadSingleModelFromParserObject(connection, parser, model.getLongName(),
                                                                     PATH_MAX_LENGTH);
                     } catch (Throwable e) {
                         String error = "Unable to load model into MDR";
 
                         handleException(result, e, error);
                         logger.error(e.getMessage(), e);
                     }
                 }
                 models.put(model.getLongName(), result);
             }
 
             PathFinder.refreshCache(connection, true);
         } finally {
             ConnectionUtil.close(connection);
         }
         return models;
     }
 
     private void handleException(LoadModelResult result, Throwable e, String error) {
         if (result != null) {
             result.setErrorMessage(error);
             result.setStackTrace(Utility.getStackTrace(e));
             result.setLoaded(false);
         }
     }
 
     Collection<EntityGroupInterface> getCab2bEntityGroups() {
         return EntityCache.getInstance().getEntityGroups();
     }
 }
