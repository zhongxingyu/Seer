 package gov.nih.nci.cagrid.portal.portlet.browse.sharedQuery;
 
 import gov.nih.nci.cagrid.portal.dao.GridServiceDao;
 import gov.nih.nci.cagrid.portal.dao.QueryInstanceDao;
 import gov.nih.nci.cagrid.portal.dao.QueryResultTableDao;
 import gov.nih.nci.cagrid.portal.domain.GridDataService;
 import gov.nih.nci.cagrid.portal.domain.GridService;
 import gov.nih.nci.cagrid.portal.domain.catalog.SharedQueryCatalogEntry;
 import gov.nih.nci.cagrid.portal.domain.dataservice.CQLQuery;
 import gov.nih.nci.cagrid.portal.domain.dataservice.Query;
 import gov.nih.nci.cagrid.portal.domain.dataservice.QueryInstance;
 import gov.nih.nci.cagrid.portal.domain.dataservice.QueryInstanceState;
 import gov.nih.nci.cagrid.portal.domain.table.QueryResultTable;
 import gov.nih.nci.cagrid.portal.portlet.UserModel;
 import gov.nih.nci.cagrid.portal.portlet.browse.ajax.CatalogEntryManagerFacade;
 import gov.nih.nci.cagrid.portal.portlet.query.QueryService;
 import gov.nih.nci.cagrid.portal.portlet.query.results.ServiceErrorInterpretor;
 import gov.nih.nci.cagrid.portal.service.PortalFileService;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  * @author <a href="mailto:joshua.phillips@semanticbits.com">Joshua Phillips</a>
  */
 @Transactional
 public class QueryExecutionManager extends CatalogEntryManagerFacade {
 
     private int maxActiveQueries = 5;
     private QueryService queryService;
     private UserModel userModel;
     private GridServiceDao gridServiceDao;
     private String resultsView;
     private String errorView;
     private QueryInstanceDao queryInstanceDao;
     private QueryResultTableDao queryResultTableDao;
     private PortalFileService portalFileService;
     private List<ServiceErrorInterpretor> serviceErrorInterpretors = new ArrayList<ServiceErrorInterpretor>();
 
 
     @Override
     public String validate() {
 
         return null;
     }
 
     public String startQueries(String[] urls) {
 
         String message = null;
         try {
             if (urls == null) {
                 String msg = "No endpoints specified.";
                 logger.error(msg);
                 throw new RuntimeException(msg);
             }
             if (getActiveQueryCount() + urls.length >= getMaxActiveQueries()) {
                 message = "Exceeded maximum number of queries. Please wait for some queries to finish";
             } else {
 
                 Query query = ((SharedQueryCatalogEntry) getUserModel()
                         .getCurrentCatalogEntry()).getAbout();
                 for (String url : urls) {
                     GridService service = getGridServiceDao().getByUrl(url);
                     if (service == null) {
                         String msg = "No service with the selected URL was found: "
                                 + url;
                         logger.error(msg);
                         throw new RuntimeException(msg);
                     }
                     if (query instanceof CQLQuery
                             && !(service instanceof GridDataService)) {
                         String msg = "The selected service is not a data service.";
                         logger.error(msg);
                         throw new RuntimeException(msg);
                     }
                    getQueryService().submitQuery(query.getXml().trim(), url);
                 }
 
             }
         } catch (Exception ex) {
             String msg = "Error submitting query: " + ex.getMessage();
             logger.error(msg, ex);
             return msg;
         }
         return message;
     }
 
     public int getActiveQueryCount() {
         int active = 0;
         for (QueryInstance instance : getQueryService().getSubmittedQueries()) {
             if (QueryInstanceState.RUNNING.equals(instance.getState())
                     || QueryInstanceState.SCHEDULED.equals(instance.getState())
                     || QueryInstanceState.UNSCHEDULED.equals(instance
                     .getState())) {
                 active++;
             }
         }
         return active;
     }
 
     /**
      * Returns all query instances for a cql query
      *
      * @param cql
      * @return
      */
     public List<QueryInstance> getQueryInstances(String cql) {
        Query query = getQueryService().loadQuery(cql.trim());
         List<QueryInstance> result = new ArrayList<QueryInstance>();
         if (query != null) {
             for (QueryInstance instance : getQueryService()
                     .getSubmittedQueries()) {
                 if (query.getId().equals(instance.getQuery().getId()))
                     result.add(instance);
             }
         }
         return result;
     }
 
     public String getResults(String instanceId) {
 
 
         String result = "Could not find query with ID " + instanceId;
 
         QueryInstance instance = loadInstance(instanceId);
         if (instance != null) {
             getUserModel().setCurrentQueryInstance(instance);
 
             // if no results.Then return message
             QueryResultTable table = getQueryResultTableDao().getByQueryInstanceId(
                     Integer.valueOf(instanceId));
 
             if (table != null && table.getId() != null) {
                 Integer numRows = getQueryResultTableDao().getRowCount(table.getId());
 
                 if (numRows < 1) {
                     return getNoResults(instance);
                 }
             }
 
             Map<String, Object> infoMap = new HashMap<String, Object>();
             infoMap.put("instance", instance);
 
             try {
                 return getView(getResultsView(), infoMap);
             } catch (Exception ex) {
                 String msg = "Error rendering query results: "
                         + ex.getMessage();
                 logger.error(msg, ex);
                 throw new RuntimeException(msg, ex);
             }
         }
 
         return result;
 
     }
 
 
     public String getError(String instanceId) {
         String result = "Could not find query with ID " + instanceId;
 
         QueryInstance instance = loadInstance(instanceId);
         Map<String, Object> infoMap = new HashMap<String, Object>();
         infoMap.put("instance", instance);
 
         if (instance != null) {
             result = instance.getError();
 
             String error = instance.getError();
             if (error != null) {
                 String message = null;
                 for (ServiceErrorInterpretor interpretor : getServiceErrorInterpretors()) {
                     logger.debug("Trying interpretor: " + interpretor);
                     try {
                         message = interpretor.getErrorMessage(error);
                         if (message != null) {
                             break;
                         }
                     } catch (Exception ex) {
                         logger.error("Couldn't get error message: " + ex.getMessage(), ex);
                     }
                 }
                 if (message != null) {
                     result = message;
                     infoMap.put("summaryErrorMsg", result);
                 }
             }
 
             try {
                 return getView(getErrorView(), infoMap);
             } catch (Exception ex) {
                 String msg = "Error rendering query error log: "
                         + ex.getMessage();
                 logger.error(msg, ex);
                 throw new RuntimeException(msg, ex);
             }
         }
 
         return result;
 
     }
 
 
     public String getNoResults(QueryInstance instance) {
         Map<String, Object> infoMap = new HashMap<String, Object>();
         infoMap.put("errorMessage", "Query returned no results");
         infoMap.put("instance", instance);
 
         try {
             return getView(getErrorView(), infoMap);
         } catch (Exception ex) {
             String msg = "Error rendering query error log: "
                     + ex.getMessage();
             logger.error(msg, ex);
             throw new RuntimeException(msg, ex);
         }
     }
 
     // should throw no exceptions
     public void navigateToInstance(String instanceId) {
         logger.debug("Navigating to Query instance " + instanceId);
         QueryInstance instance = loadInstance(instanceId);
         if (instance != null) {
             getUserModel().setCurrentQueryInstance(instance);
         }
     }
 
     public QueryInstance loadInstance(String instanceId) {
 
         for (QueryInstance inst : getQueryService().getSubmittedQueries()) {
             if (instanceId.equals(String.valueOf(inst.getId()))) {
 
                 return inst;
             }
         }
         return null;
     }
 
     public void deleteQueryInstance(Integer instanceId) {
         try {
             getQueryService().removeQueryInstance(instanceId);
             QueryInstanceDao dao = getQueryInstanceDao();
             HibernateTemplate templ = dao.getHibernateTemplate();
             QueryInstance inst = getQueryInstanceDao().getById(instanceId);
             QueryResultTable table = inst.getQueryResultTable();
             if (table != null) {
 
                 if (table.getData().getFileName() != null) {
                     logger.debug("Will delete query results file");
                     boolean deleted = getPortalFileService().delete(table.getData().getFileName());
                     if (!deleted)
                         logger.warn("Could not delete file on disk. Filename " + table.getData().getFileName());
                 }
                 templ.delete(table.getData());
                 templ.delete(table);
             }
             dao.delete(inst);
         } catch (Exception ex) {
             String msg = "Error deleting query instance: " + ex.getMessage();
             logger.error(msg, ex);
             throw new RuntimeException(msg, ex);
         }
     }
 
     public GridServiceDao getGridServiceDao() {
         return gridServiceDao;
     }
 
     public void setGridServiceDao(GridServiceDao gridServiceDao) {
         this.gridServiceDao = gridServiceDao;
     }
 
     public int getMaxActiveQueries() {
         return maxActiveQueries;
     }
 
     public void setMaxActiveQueries(int maxActiveQueries) {
         this.maxActiveQueries = maxActiveQueries;
     }
 
     public QueryService getQueryService() {
         return queryService;
     }
 
     public void setQueryService(QueryService queryService) {
         this.queryService = queryService;
     }
 
     public UserModel getUserModel() {
         return userModel;
     }
 
     public void setUserModel(UserModel userModel) {
         this.userModel = userModel;
     }
 
     public String getResultsView() {
         return resultsView;
     }
 
     public void setResultsView(String resultsView) {
         this.resultsView = resultsView;
     }
 
     public String getErrorView() {
         return errorView;
     }
 
     public void setErrorView(String errorView) {
         this.errorView = errorView;
     }
 
     public QueryInstanceDao getQueryInstanceDao() {
         return queryInstanceDao;
     }
 
     public void setQueryInstanceDao(QueryInstanceDao queryInstanceDao) {
         this.queryInstanceDao = queryInstanceDao;
     }
 
     public QueryResultTableDao getQueryResultTableDao() {
         return queryResultTableDao;
     }
 
     public void setQueryResultTableDao(QueryResultTableDao queryResultTableDao) {
         this.queryResultTableDao = queryResultTableDao;
     }
 
     public PortalFileService getPortalFileService() {
         return portalFileService;
     }
 
     public void setPortalFileService(PortalFileService portalFileService) {
         this.portalFileService = portalFileService;
     }
 
     public List<ServiceErrorInterpretor> getServiceErrorInterpretors() {
         return serviceErrorInterpretors;
     }
 
     public void setServiceErrorInterpretors(List<ServiceErrorInterpretor> serviceErrorInterpretors) {
         this.serviceErrorInterpretors = serviceErrorInterpretors;
     }
 }
