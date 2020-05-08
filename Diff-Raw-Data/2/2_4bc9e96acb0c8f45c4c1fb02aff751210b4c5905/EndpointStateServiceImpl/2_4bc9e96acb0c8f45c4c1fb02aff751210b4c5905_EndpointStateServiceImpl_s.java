 /**
  * 
  */
 package org.easysoa.registry.integration;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Context;
 import org.easysoa.registry.rest.integration.EndpointStateService;
 import org.easysoa.registry.rest.integration.ServiceLevelHealth;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicator;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicators;
 import org.easysoa.registry.types.Endpoint;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.directory.DirectoryException;
 import org.nuxeo.ecm.directory.Session;
 import org.nuxeo.ecm.directory.api.DirectoryService;
 import org.nuxeo.ecm.directory.sql.filter.SQLBetweenFilter;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * Endpoint state service implementation
  * 
  * @author jguillemotte
  * 
  */
 @Path("easysoa/endpointStateService")
 public class EndpointStateServiceImpl implements EndpointStateService {
 
     // Servlet context
     @Context
     HttpServletRequest request;
     
     /**
      * @see org.easysoa.registry.rest.integration.EndpointStateService#updateSlaOlaIndicators(SlaOrOlaIndicator[])
      */
     @Override
     public void createSlaOlaIndicators(SlaOrOlaIndicators slaOrOlaIndicators) throws Exception {
 
         DirectoryService directoryService = Framework.getService(DirectoryService.class);
         
         if(slaOrOlaIndicators != null){
 
             // Open a session on slaOrOlaIndicator directory
             Session session = directoryService.open("slaOrOlaIndicator");
             if(session == null){
                 throw new Exception("Unable to open a new session on directory 'slaOrOlaIndicator'");
             }
 
             try{
                 // Update each indicator
                 for(SlaOrOlaIndicator indicator : slaOrOlaIndicators.getSlaOrOlaIndicatorList()){
                     // Create new indicator
                     createIndicator(session, indicator);
                 }
             }
             catch(Exception ex){
                  // Return the exception and cancel the transaction
                  throw new Exception("Failed to update SLA or OLA indicators ", ex);                   
             } finally {
                 if (session != null) {
                     // NB. container manages transaction, so no need to commit or rollback
                     // (see doc of deprecated session.commit())
                     session.close();
                 }
             }
         }
     }
     
     private void createIndicator(Session session, SlaOrOlaIndicator indicator)
             throws DirectoryException, ClientException, Exception {
         checkNotNull(indicator.getEndpointId(), "SlaOrOlaIndicator.endpointId");
         checkNotNull(indicator.getSlaOrOlaName(), "SlaOrOlaIndicator.slaOrOlaName");
         // TODO LATER check that both exist
         Map<String, Object> properties = new HashMap<String, Object>();
         properties.put("endpointId", indicator.getEndpointId());
         properties.put("slaOrOlaName", indicator.getSlaOrOlaName());
         properties.put("serviceLeveHealth", indicator.getServiceLevelHealth().toString());    
         properties.put("serviceLevelViolation", indicator.isServiceLevelViolation());
         if(indicator.getTimestamp() != null){
             GregorianCalendar calendar = new GregorianCalendar();
             calendar.setTime(indicator.getTimestamp());
             properties.put("timestamp", calendar);
         }
         session.createEntry(properties);
     }
 
     private void checkNotNull(String value, String displayName) throws Exception {
         if (value == null || value.trim().isEmpty()) {
             throw new Exception(displayName + " must not be empty !");
         }
     }
 
     /**
      * @see org.easysoa.registry.rest.integration.EndpointStateService#updateSlaOlaIndicators(SlaOrOlaIndicator[])
      */
     @Override
     public void updateSlaOlaIndicators(SlaOrOlaIndicators slaOrOlaIndicators) throws Exception {
 
         DirectoryService directoryService = Framework.getService(DirectoryService.class);
         
         if(slaOrOlaIndicators != null){
 
             // Open a session on slaOrOlaIndicator directory
             Session session = directoryService.open("slaOrOlaIndicator");
             if(session == null){
                 throw new Exception("Unable to open a new session on directory 'slaOrOlaIndicator'");
             }
 
             try{
                 // Update each indicator
                 for(SlaOrOlaIndicator indicator : slaOrOlaIndicators.getSlaOrOlaIndicatorList()){
                     // get the indicator if exists
                     Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                     parameters.put("slaOrOlaName", indicator.getSlaOrOlaName());
                     parameters.put("endpointId", indicator.getEndpointId());
                     GregorianCalendar calendar = new GregorianCalendar();
                     calendar.setTime(indicator.getTimestamp());
                     parameters.put("timestamp", calendar);
                     DocumentModelList documentModelList = session.query(parameters);
                     DocumentModel indicatorModel;
                     
                     if(documentModelList != null && documentModelList.size() > 0){
                         // Update existing indicator
                         indicatorModel = documentModelList.get(0);
                         indicatorModel.setPropertyValue("serviceLeveHealth", indicator.getServiceLevelHealth().toString());
                         indicatorModel.setPropertyValue("serviceLevelViolation", String.valueOf(indicator.isServiceLevelViolation()));
                         session.updateEntry(indicatorModel);
                     } else {
                         // Create new indicator
                         createIndicator(session, indicator);
                     }
                 }
             }
             catch(Exception ex){
                  // Return the exception and cancel the transaction
                  throw new Exception("Failed to update SLA or OLA indicators ", ex);                   
             } finally {
                 if (session != null) {
                     // NB. container manages transaction, so no need to commit or rollback
                     // (see doc of deprecated session.commit())
                     session.close();
                 }
             }
         }
     }
 
     /**
      * @see org.easysoa.registry.rest.integration.EndpointStateService#getSlaOrOlaIndicatorsByEnv(String, String, Date,
      *      Date, int, int)
      */
     @Override
     public SlaOrOlaIndicators getSlaOrOlaIndicatorsByEnv(String environment, String projectId,
             String periodStart, String periodEnd, int pageSize, int pageStart) throws Exception {
         // TODO NXQL query returning all endpoints where environment & projectId
         // TODO put them in call to getSlaOrOlaIndicators(String endpointIds...) and return it
         
         CoreSession documentManager = SessionFactory.getSession(request);
 
         if(environment != null && !"".equals(environment) && projectId != null && !"".equals(projectId)){
             throw new IllegalArgumentException("Environment or projectid parameter must not be null or empty");
         }
         
         // Fetch SoaNode list
         ArrayList<String> parameters = new ArrayList<String>(); 
         StringBuilder query = new StringBuilder(); 
         query.append("SELECT * FROM Endpoint WHERE ");
  
         // Search parameters
         if(environment != null && !"".equals(environment)){
             query.append(Endpoint.XPATH_ENDP_ENVIRONMENT + " like '?' ");
             parameters.add(environment);
         }
         
         if(projectId != null && !"".equals(projectId)){
             if(environment != null && !"".equals(environment)){
                 query.append(" AND ");
             }
             query.append("endp:projectid" + " like '?' ");
             parameters.add(projectId);
         }
         
         // Execute query
         String nxqlQuery = NXQLQueryBuilder.getQuery(query.toString(), parameters.toArray(), false, true);
         DocumentModelList soaNodeModelList = documentManager.query(nxqlQuery);        
         
         // Get endpoints list
         List<String> endpointsList = new ArrayList<String>();
         for(DocumentModel documentModel : soaNodeModelList){
             endpointsList.add((String)documentModel.getPropertyValue(Endpoint.XPATH_UUID));
         }
         
         // Get endpoints indicators
         return this.getSlaOrOlaIndicators(endpointsList, periodStart, periodEnd, pageSize, pageStart);
     }
     
     /**
      * 
      * not REST, used by above
      * TODO LATER ask Nuxeo to support OR in SQL Directory queries 
      * 
      * @param endpointIds
      * @param periodStart
      * @param periodEnd
      * @param pageSize
      * @param pageStart
      * @return
      * @throws Exception
      */
     protected SlaOrOlaIndicators getSlaOrOlaIndicators(List<String> endpointIds,
             String periodStart, String periodEnd, int pageSize, int pageStart) throws Exception {
 
         // For each endpoint, get the corresponding indicators and returns the indicator list
         SlaOrOlaIndicators slaOrOlaIndicators = new SlaOrOlaIndicators();
         for(String endpointId : endpointIds){
             slaOrOlaIndicators.getSlaOrOlaIndicatorList().addAll(getSlaOrOlaIndicators(endpointId, "", periodStart, periodEnd, pageSize, pageStart).getSlaOrOlaIndicatorList());
         }
         return slaOrOlaIndicators;        
     }
 
     /**
      * @see org.easysoa.registry.rest.integration.EndpointStateService#getSlaOrOlaIndicators(String, String, Date,
      *      Date, int, int)
      */
     @Override
     public SlaOrOlaIndicators getSlaOrOlaIndicators(String endpointId, 
             String slaOrOlaName, String periodStart, String periodEnd, int pageSize, int pageStart) throws Exception {
         
         DirectoryService directoryService = Framework.getService(DirectoryService.class);        
         Session session = directoryService.open(org.easysoa.registry.types.SlaOrOlaIndicator.DOCTYPE);        
         
         /*
         * Returns level indicators, in the given period (default : daily)
         * OPT paginated navigation
         * @param periodStart : if null day start, if both null returns all in the current day
         * @param periodEnd : if null now, if both null returns all in the current day
         * @param pageSize OPT pagination : number of indicators per page, if not specified, all results are returned
         * @param pageStart OPT pagination : index of the first indicator to return (starts with 0)
         * @return SlaOrOlaIndicators array of SlaOrOlaIndicator
         */
         
         if(session == null){
             throw new Exception("Unable to open a new session on directory '" + org.easysoa.registry.types.SlaOrOlaIndicator.DOCTYPE + "'");
         }
 
         Map<String, Serializable> parameters = new HashMap<String, Serializable>();
         
         if(endpointId != null && !"".equals(endpointId)){
             parameters.put("endpointId", endpointId);
         }
         
         if(slaOrOlaName != null && !"".equals(slaOrOlaName)){
             parameters.put("slaOrOlaName", slaOrOlaName);
         }
 
         SlaOrOlaIndicators slaOrOlaIndicators = new SlaOrOlaIndicators();
         // Execute query        
         try {
 
             // Use this method to have request on date range and pagination
             Map<String, String> orderByParams = new HashMap<String, String>();
             Set<String> fullTextSearchParams = new HashSet<String>();
 
             SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
             Calendar calendarFrom = new GregorianCalendar();
             Calendar calendarTo = new GregorianCalendar();
             Calendar currentDate = new GregorianCalendar();
             
             // Add date Range param
             // If periodEnd is null and period start is null, set to current day end
             if(periodEnd == null && periodStart == null){
                 calendarTo.clear();
                 calendarTo.set(currentDate.get(Calendar.YEAR) , currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
             } 
             // If period End is null and periodStart not null, set to now
             else if(periodEnd == null && periodStart != null) {
                 calendarTo.setTime(currentDate.getTime());
             } 
             // else set with the periodEnd param 
             else {
                 calendarTo.setTime(dateFormater.parse(periodEnd));
             }
             // Set period start
             if(periodStart != null){
                 calendarFrom.setTime(dateFormater.parse(periodStart));
             } 
             // If periodStart is null, set to current day start
             else {
                 calendarFrom.clear();
                 // TODO : previous setting was to fix the from date to the current day
                 // But cause some problems to test the UI when the model is not reloaded daily
                 // Remove the "-1" when finished
                calendarFrom.set(currentDate.get(Calendar.YEAR) - 1, currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
             }            
             SQLBetweenFilter dateRangeFilter = new SQLBetweenFilter(calendarFrom, calendarTo);
             parameters.put("timestamp", dateRangeFilter);
             // Execute the query
             DocumentModelList soaNodeModelList = session.query(parameters, fullTextSearchParams, orderByParams, false, pageSize, pageStart * pageSize);
             
             SlaOrOlaIndicator indicator;
             for(DocumentModel model : soaNodeModelList){
                 indicator = new SlaOrOlaIndicator();
                 indicator.setEndpointId((String)model.getPropertyValue(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_ENDPOINT_ID));
                 indicator.setType(model.getType());
                 indicator.setServiceLevelHealth(ServiceLevelHealth.valueOf((String)model.getPropertyValue(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_SERVICE_LEVEL_HEALTH)));
                 indicator.setServiceLevelViolation((Boolean)model.getPropertyValue(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_SERVICE_LEVEL_VIOLATION));
                 indicator.setSlaOrOlaName((String)model.getPropertyValue(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_SLA_OR_OLA_NAME));
                 GregorianCalendar calendar = (GregorianCalendar)model.getPropertyValue(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_TIMESTAMP);
                 indicator.setTimestamp(calendar.getTime());
                 slaOrOlaIndicators.getSlaOrOlaIndicatorList().add(indicator);
             }
         } catch (ClientException ex) {
             ex.printStackTrace();
             throw ex;
         }
         
         return slaOrOlaIndicators;
     }
     
 }
