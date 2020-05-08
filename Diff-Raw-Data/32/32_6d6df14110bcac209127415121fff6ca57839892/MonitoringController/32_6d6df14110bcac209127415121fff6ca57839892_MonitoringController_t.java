 /**
  * EasySOA Registry
  * Copyright 2012 Open Wide
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact : easysoa-dev@googlegroups.com
  */
 
 package org.easysoa.registry.monitoring.rest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.indicators.rest.IndicatorValue;
 import org.easysoa.registry.indicators.rest.IndicatorsController;
 import org.easysoa.registry.integration.EndpointStateServiceImpl;
 import org.easysoa.registry.rest.EasysoaModuleRoot;
 import org.easysoa.registry.rest.integration.EndpointStateService;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicator;
 import org.easysoa.registry.types.ServiceImplementation;
 import org.easysoa.registry.types.SubprojectNode;
 import org.easysoa.registry.types.ids.SoaNodeId;
 import org.easysoa.registry.utils.ContextData;
 import org.easysoa.registry.utils.NXQLQueryHelper;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.ecm.webengine.model.Template;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.runtime.api.Framework;
 
 
 /**
  * Indicators
  *
  * impl : TODO see ServiceDocumentationController, MatchingDashboard...
  *
  * @author mdutoo, jguillemotte
  *
  */
 @WebObject(type = "monitoring")
 @Path("easysoa/monitoring")
 public class MonitoringController extends EasysoaModuleRoot {
 
     @SuppressWarnings("unused")
     private static Logger logger = Logger.getLogger(MonitoringController.class);
 
    // Number of indicators par page
    public int RESULTS_PER_PAGE = 10;

     /**
      * Default constructor
      */
     public MonitoringController() {
 
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     public Object doGetHTML(@QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
         String subprojectCriteria = NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, visibility);
 
         // Get the environments
         List<String> envs = docService.getEnvironmentsInCriteria(session, subprojectCriteria);
 
         // Get the deployed services
         List<DocumentModel> services = docService.getInformationServicesInCriteria(session, subprojectCriteria);
 
         Map<String, List<DocumentModel>> endpoints = new HashMap<String, List<DocumentModel>>();
 
         for(DocumentModel service : services){
             // Get the endpoints
         	List<DocumentModel> endpointsList = docService.getEndpointsOfServiceInCriteria(service, subprojectCriteria);
             endpoints.put(service.getName(), endpointsList);
         }
 
         return getView("dashboard")
                 .arg("envs", envs) // TODO later by (sub)project
                 .arg("endpoints", endpoints)
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId))
 
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor());
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
     }
 
     @GET
     @Path("envIndicators/{endpointId:.+}/{page}") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doGetByPathHTML(@PathParam("endpointId") String endpointId, @PathParam("page") String page, @QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
         Pagination pagination = new Pagination();
 
         // Check there is at least on environment
         if(endpointId == null || "".equals(endpointId)){
             throw new IllegalArgumentException("At least one endpointID must be specified");
         }
 
         // Check page Number
         int pageNumber;
         try{
             pageNumber = Integer.parseInt(page) - 1;
             if(pageNumber < 1){
                 pageNumber = 0;
             }
         }
         catch(Exception ex){
             throw new IllegalArgumentException("Page argument must be a positive integer number greater than 0 ");
         }
 
         // Get the enpoints associated with the environment
         //DocumentModelList endpointsModel = session.query("SELECT * FROM " + Endpoint.DOCTYPE + " WHERE " + Endpoint.XPATH_ENDP_ENVIRONMENT + " = " + envName);
 
         // Get the endpoint
         DocumentModel endpoint = session.getDocument(new IdRef(endpointId)); ///DocumentModel endpoint = docService.query(session, "SELECT * FROM " + Endpoint.DOCTYPE + " WHERE ecm:uuid = '" + endpointId + "'" , true, false).get(0);
         // Get the service
         String serviceId = endpoint.getProperty(ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE).getValue(String.class);
         DocumentModel service = session.getDocument(new IdRef(serviceId)); /// docService.query(session, "SELECT * FROM " + InformationService.DOCTYPE + " WHERE ecm:uuid = '" + serviceId + "'" , true, false).get(0);
         String slaOrOlaSubprojectId = (String) service.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT);
 
         //String serviceFolder = session.getDocument(service.getParentRef()).getPathAsString();
 
         // Get the indicators for the endpoint in the Nuxeo store
         EndpointStateService endpointStateService = new EndpointStateServiceImpl();
        List<SlaOrOlaIndicator> indicators =  endpointStateService.getSlaOrOlaIndicators(endpointId, "", null, null, RESULTS_PER_PAGE, pageNumber).getSlaOrOlaIndicatorList();
 
         // Set pagination
         List<String> endpointIds = new ArrayList<String>();
         endpointIds.add(endpointId);
         int totalIndicatorNumber = endpointStateService.getTotalNumberOfSlaOrOlaindicators(endpointIds, null, null);
 
         // To finish : Set data in pagination, add indicators in database to test the pagination
        pagination.setTotalPageNumber(totalIndicatorNumber % RESULTS_PER_PAGE);
        pagination.setCurrentPage(pageNumber+1);
        if(totalIndicatorNumber > RESULTS_PER_PAGE){
            pagination.setHasNextPage(true);
        }
        if(pageNumber > 0){
            pagination.setHasPreviousPage(true);
        }
 
         // TODO Complete the returned indicators
         // Complete each indicator with the description / services ...
         for(SlaOrOlaIndicator indicator : indicators){
             indicator.setDescription("Aucune description");
 
             DocumentModel slaOrOla;
             // Get only the first par of the subprojectID
             /*String subPath = "";
             if(subProjectId != null && !"".equals(subProjectId)){
                 subPath = subProjectId.substring(0, subProjectId.lastIndexOf("/"));
             }*/
             // Get the SLA or OLA indicator in the Nuxeo registry
             slaOrOla = docService.findSoaNode(session, new SoaNodeId(slaOrOlaSubprojectId, org.easysoa.registry.types.SlaOrOlaIndicator.SLA_DOCTYPE, indicator.getSlaOrOlaName()), true);
             if (slaOrOla == null){
                 slaOrOla = docService.findSoaNode(session, new SoaNodeId(slaOrOlaSubprojectId, org.easysoa.registry.types.SlaOrOlaIndicator.OLA_DOCTYPE, indicator.getSlaOrOlaName()), true);
             }
 
             // Set additionnal indicator informatons
             if(slaOrOla != null){
                 indicator.setDescription(slaOrOla.getProperty(org.easysoa.registry.types.SlaOrOlaIndicator.XPATH_SLA_OR_OLA_DESCRIPTION).getValue(String.class));
                 indicator.setPath(slaOrOla.getPathAsString());
             }
         }
 
         Template view = getView("envIndicators"); // TODO see services.ftl, dashboard/*.ftl...
         if (indicators != null) {
             view.arg("indicators", indicators);
         }
         view.arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId))
                 .arg("service", service)
                 .arg("servicePath", service.getPathAsString())
                 .arg("endpoint", endpoint)
                 .arg("pagination", pagination)
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor());
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
 
         return view;
     }
 
     @GET
     @Path("usage") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doGetUsagePageHTML(@QueryParam("subprojectId") String subprojectId,
     		@QueryParam("visibility") String visibility) throws Exception {
 
         CoreSession session = SessionFactory.getSession(request);
 
         // Indicators
         IndicatorsController indicatorsController = new IndicatorsController();
         Map<String, IndicatorValue> indicators = indicatorsController.computeIndicators(session, null, null, subprojectId, visibility);
 
         return getView("usage")
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("indicators", indicators)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId))
 
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor());
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
     }
 
     @GET
     @Path("jasmine")
     @Produces(MediaType.TEXT_HTML)
     public Object doGetJasminePageHTML(@QueryParam("subprojectId") String subProjectId, @QueryParam("visibility") String visibility) throws Exception {
 
         CoreSession session = SessionFactory.getSession(request);
 
         Template view = getView("jasmine");
         view.arg("subprojectId", subProjectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subProjectId))
 
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor());
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
 
         return view;
     }
 
 }
