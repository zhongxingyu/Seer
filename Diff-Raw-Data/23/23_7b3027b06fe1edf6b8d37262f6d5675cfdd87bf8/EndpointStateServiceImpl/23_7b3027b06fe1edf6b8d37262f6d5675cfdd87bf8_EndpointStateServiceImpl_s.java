 /**
  * 
  */
 package org.easysoa.registry.integration;
 
 import java.util.ArrayList;
 import java.util.Date;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Context;
 
import org.easysoa.registry.DiscoveryService;
import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.rest.integration.EndpointStateService;
 import org.easysoa.registry.rest.integration.ServiceLevelHealth;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicator;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicators;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 
import com.google.inject.Inject;
 
 /**
  * Endpoint state service implementation
  * 
  * @author jguillemotte
  * 
  */
 public class EndpointStateServiceImpl implements EndpointStateService {
 
     // Servlet context
     @Context
     HttpServletRequest request;
 
     // Nuxeo document service
    @Inject
    DocumentService documentService;
 
    @Inject
    DiscoveryService discoveryService;
     
     /**
      * @see org.easysoa.registry.rest.integration.EndpointStateService#updateSlaOlaIndicators(SlaOrOlaIndicator[])
      */
     @Override
     public void updateSlaOlaIndicators(SlaOrOlaIndicators slaOrOlaIndicators) throws Exception {
         CoreSession documentManager = SessionFactory.getSession(request);
         if(slaOrOlaIndicators != null){
             for(SlaOrOlaIndicator indicator : slaOrOlaIndicators.getSlaOrOlaIndicatorList()){
                 
                 // get the indicator
                 ArrayList<String> parameters = new ArrayList<String>();
                 StringBuffer query = new StringBuffer(); 
                 query.append("SELECT * FROM SLA, OLA WHERE soan:name = '?'");
                 parameters.add(indicator.getSlaOrOlaName());
                 // name is enough to find the good sla or ola ??
                 //indicator.getEndpointId()
                 
                 try{
                     String nxqlQuery = NXQLQueryBuilder.getQuery(query.toString(), parameters.toArray(), false, true);
                     DocumentModelList soaNodeModelList = documentManager.query(nxqlQuery);
                     
                     if(soaNodeModelList != null){
                         // Get the indicator
                         DocumentModel indicatorModel = soaNodeModelList.get(0);
                         // Update
                         indicatorModel.setPropertyValue("", indicator.getServiceLevelHealth());
                         indicatorModel.setPropertyValue("", indicator.getTimestamp());
                         // Save
                         //documentService.
                     }
                 }
                catch(Exception ex){
                    // Return the exception and cancel the transaction
                    throw new Exception("Failed to update SLA or OLA indicators ", ex);                   
                }
                 
                 
                 // Update
                 // To update indicators ???
                 //discoveryService.runDiscovery(documentManager, identifier, properties, parentDocuments);
                 
                 //documentService.
                 
             }
         }
     }
 
     /**
      * @see org.easysoa.registry.rest.integration.EndpointStateService#getSlaOrOlaIndicators(Date,
      *      Date, int, int)
      */
     @Override
     public SlaOrOlaIndicators getSlaOrOlaIndicators(String endpointId, 
             String slaOrOlaName, String environment, String projectId,
             Date periodStart, Date periodEnd, int pageSize, int pageStart) throws Exception {
         
         CoreSession documentManager = SessionFactory.getSession(request);
 
         ArrayList<String> parameters = new ArrayList<String>();
         StringBuffer query = new StringBuffer(); 
         query.append("SELECT * FROM SLA, OLA ");
         
         /*
         * Returns level indicators, in the given period (default : daily)
         * OPT paginated navigation
         * @param periodStart : if null day start, if both null returns all in the current day
         * @param periodEnd : if null now, if both null returns all in the current day
         * @param pageSize OPT pagination : number of indicators per page
         * @param pageStart OPT pagination : index of the first indicator to return (starts with 0)
         * @return SlaOrOlaIndicators array of SlaOrOlaIndicator
         */
         
         // TODO : Add parameters
         if(endpointId != null){
             
             // check if there is a WHERE keyword
             // can be done if parameters == 0
             // else if parameters > 0 => add a AND keyword
             query.append(addQueryKeyWord(parameters.size()));
             //query.append("dc:title = '?'");
             parameters.add(endpointId);
         }
         
         if(slaOrOlaName != null){
             query.append(addQueryKeyWord(parameters.size()));
             query.append("soan:name = '?'");
             parameters.add(slaOrOlaName);
         }
         
         if(environment != null){
             query.append(addQueryKeyWord(parameters.size()));
             
             parameters.add(environment);
         } else {
             
         }
         
         if(projectId != null){
             query.append(addQueryKeyWord(parameters.size()));
             
             parameters.add(projectId);
         } else {
             
         }
         
         if(periodStart == null){
             query.append(addQueryKeyWord(parameters.size()));
             
             parameters.add(String.valueOf(periodStart));
         } else {
         
         }
         
         if(periodEnd == null){
             query.append(addQueryKeyWord(parameters.size()));
             
             parameters.add(String.valueOf(periodEnd));
         } else {
         
         }
         
         SlaOrOlaIndicators slaOrOlaIndicators = new SlaOrOlaIndicators();
         ArrayList<SlaOrOlaIndicator> indicatorList = new ArrayList<SlaOrOlaIndicator>();
         // Execute query        
         try {
             String nxqlQuery = NXQLQueryBuilder.getQuery(query.toString(), parameters.toArray(), false, true);
             DocumentModelList soaNodeModelList = documentManager.query(nxqlQuery);            
             SlaOrOlaIndicator indicator;
             for(DocumentModel model : soaNodeModelList){
                 indicator = new SlaOrOlaIndicator();
                 indicator.setEndpointId("");
                 indicator.setServiceLevelHealth(ServiceLevelHealth.gold);
                 indicator.setServiceLevelViolation(false);
                 indicator.setSlaOrOlaName((String)model.getPropertyValue("soan:name"));
                 indicator.setTimestamp(0);
                 indicatorList.add(indicator);
 
             }
         } catch (ClientException ex) {
             ex.printStackTrace();
             throw ex;
             
         }
         
         // TODO : add pagination
         int itemStartIndex = pageSize * pageStart;
         slaOrOlaIndicators.setSlaOrOlaIndicatorList(indicatorList.subList(itemStartIndex, itemStartIndex + pageSize)) ;                
 
         return slaOrOlaIndicators;
     }
     
     /**
      * 
      * @param parameterListSize
      * @return
      */
     private String addQueryKeyWord(int parameterListSize){
         if(parameterListSize <= 0){
             return "WHERE";
         } else {
             return "AND";
         }
         
     }
     
 }
