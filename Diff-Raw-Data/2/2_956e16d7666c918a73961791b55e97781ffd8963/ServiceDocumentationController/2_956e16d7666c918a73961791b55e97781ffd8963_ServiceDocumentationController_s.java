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
 
 package org.easysoa.registry.documentation.rest;
 
 import static org.easysoa.registry.utils.NuxeoListUtils.getProxiedIdLiteralList;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.SoaMetamodelService;
 import org.easysoa.registry.SubprojectServiceImpl;
 import org.easysoa.registry.facets.ServiceImplementationDataFacet;
 import org.easysoa.registry.indicators.rest.IndicatorValue;
 import org.easysoa.registry.indicators.rest.IndicatorsController;
 import org.easysoa.registry.rest.EasysoaModuleRoot;
 import org.easysoa.registry.subproject.SubprojectId;
 import org.easysoa.registry.types.BusinessService;
 import org.easysoa.registry.types.Deliverable;
 import org.easysoa.registry.types.Endpoint;
 import org.easysoa.registry.types.InformationService;
 import org.easysoa.registry.types.ServiceImplementation;
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.types.SubprojectNode;
 import org.easysoa.registry.types.TaggingFolder;
 import org.easysoa.registry.types.adapters.SoaNodeAdapter;
 import org.easysoa.registry.types.ids.SoaNodeId;
 import org.easysoa.registry.utils.ContextData;
 import org.easysoa.registry.utils.NXQLQueryHelper;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.NuxeoPrincipal;
 import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
 import org.nuxeo.ecm.webengine.model.Template;
 import org.nuxeo.ecm.webengine.model.WebObject;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * Indicators
  *
  * @author mdutoo
  *
  */
 @WebObject(type = "EasySOA")
 @Path("easysoa/services")
 public class ServiceDocumentationController extends EasysoaModuleRoot {
 
     /** properties to be displayed in lists of services */
     //private static final String SERVICE_LIST_PROPS = "*"; // "ecm:title" // TODO is this an optimization worth the hassle ??
 
     @SuppressWarnings("unused")
     private static Logger logger = Logger.getLogger(ServiceDocumentationController.class);
 
     public ServiceDocumentationController() {
 
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     public Object doGetHTML(/*@DefaultValue(null) */@QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
 
         String subprojectCriteria = NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, visibility);
 
         // getting (phase-scoped) services
         List<DocumentModel> services = docService.getInformationServicesInCriteria(session, subprojectCriteria);
         // getting (phase-scoped) tagging folders
         List<DocumentModel> tags = docService.getByTypeInCriteria(session, TaggingFolder.DOCTYPE, subprojectCriteria);
         // getting (phase-scoped) tagged services
         // WARNING : looking for proxies only work when proxy-containing TaggingFolder are in the same Phase as services
         // (else wrong phase id metadata), and using Path only works for live documents !
         ///DocumentModelList serviceProxies = docService.query(session, DocumentService.NXQL_SELECT_FROM
         ///        + InformationService.DOCTYPE + subprojectCriteria
         ///        + DocumentService.NXQL_AND + DocumentService.NXQL_PATH_STARTSWITH
         ///        + RepositoryHelper.getRepositoryPath(session, subprojectId) + TaggingFolder.DOCTYPE + "'", false, true);
         List<DocumentModel> serviceProxies = new ArrayList<DocumentModel>();
         // so rather getting them using getChildren :
         for (DocumentModel tag : tags) {
             DocumentModelList tagChildrenProxies = session.getChildren(tag.getRef());
             serviceProxies.addAll(tagChildrenProxies);
         }
         //DocumentModelList serviceProxyIds = session.query("SELECT " + "ecm:uuid, ecm:parentid" + " FROM "
         //        + Service.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
         //        + DocumentService.NXQL_PATH_STARTSWITH + RepositoryHelper.getRepositoryPath(session, subprojectId) + TaggingFolder.DOCTYPE + "'", false, true);
 
         // TODO id to group / aggregate, use... http://stackoverflow.com/questions/5023743/does-guava-have-an-equivalent-to-pythons-reduce-function
         // collection utils :
         // TreeMap(Comparator) http://docs.oracle.com/javase/6/docs/api/java/util/TreeMap.html#TreeMap%28java.util.Comparator%29
         // google collections Maps : uniqueIndex, transformValues (multimaps) http://code.google.com/p/guava-libraries/wiki/CollectionUtilitiesExplained
         // custom code in this spirit http://code.google.com/p/guava-libraries/issues/detail?id=546
         // (NB. guava itself brings nothing more)
         // or more functional stuff :
         // jedi http://jedi.codehaus.org/Examples
         // lambdaj : count, group (& filter, join) http://lambdaj.googlecode.com/svn/trunk/html/apidocs/ch/lambdaj/Lambda.html
         // (java)script in java (ex. Rhino)
         // or more SQL-like stuff ex. josql http://josql.sourceforge.net/
         // (or true olap)
 
         HashMap<String, HashSet<DocumentModel>> tagId2Services = new HashMap<String, HashSet<DocumentModel>>();
         for (DocumentModel serviceProxyDoc : serviceProxies) {
             String serviceProxyParentId = (String) serviceProxyDoc.getParentRef().reference();
             HashSet<DocumentModel> taggedServices = tagId2Services.get(serviceProxyParentId);
             if (taggedServices == null) {
                 taggedServices = new HashSet<DocumentModel>();
                 tagId2Services.put(serviceProxyParentId, taggedServices);
             }
             // unwrapping proxy
             taggedServices.add(session.getSourceDocument(serviceProxyDoc.getRef())); // TODO or by looking in a services map ?
         }
 
         /*HashMap<String, Integer> tagId2ServiceNbs = new HashMap<String, Integer>();
         for (DocumentModel serviceProxyIdDoc : serviceProxyIds) {
             String serviceProxyParentId = (String) serviceProxyIdDoc.getParentRef().reference();
             Integer serviceProxyNb = tagId2ServiceNbs.get(serviceProxyParentId);
             if (serviceProxyNb == null) {
                 serviceProxyNb = 0;
             } else {
                 serviceProxyNb++;
             }
             tagId2ServiceNbs.put(serviceProxyParentId, serviceProxyNb);
         }*/
         String proxiedServicesIdLiteralList = getProxiedIdLiteralList(session, serviceProxies);
         String proxiedServicesCriteria = proxiedServicesIdLiteralList.length() == 2 ? "" :
             DocumentService.NXQL_AND + " NOT ecm:uuid IN " + proxiedServicesIdLiteralList;
         DocumentModelList untaggedServices = docService.query(session, DocumentService.NXQL_SELECT_FROM
         		+ InformationService.DOCTYPE + subprojectCriteria + proxiedServicesCriteria, true, false);
 
         List<DocumentModel> impls = docService.getServiceImplementationsInCriteria(session, subprojectCriteria);
         List<DocumentModel> endpoints = docService.getEndpointsInCriteria(session, subprojectCriteria);
         
         // Indicators TODO are they required on this page ??
         IndicatorsController indicatorsController = new IndicatorsController();
         Map<String, IndicatorValue> indicators = indicatorsController.computeIndicators(session, null, null, subprojectId, visibility);
 
         return getView("services")
                 .arg("services", services)
                 .arg("impls", impls)
                 .arg("endpoints", endpoints)
                 
                 .arg("tags", tags)
                 .arg("tagId2Services", tagId2Services)
                 //.arg("tagId2ServiceNbs", tagId2ServiceNbs)
                 .arg("untaggedServices", untaggedServices)
                 
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor())
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
                 
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("indicators", indicators)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
         // services.get(0).getProperty(schemaName, name)
         // services.get(0).getProperty(xpath)
     }
 
     @GET
     @Path("path{nodeSubprojectId:[^:]+}:{type:[^:]*}:{name:.+}")
     @Produces(MediaType.TEXT_HTML)
     public Object doGetByPathHTML(
     		@PathParam("nodeSubprojectId") String nodeSubprojectId,
     		@PathParam("name") String name, @PathParam("type") String type,
             @QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility)
             		throws Exception {
     	
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
         SoaMetamodelService soaMetamodelService = Framework.getService(SoaMetamodelService.class);
         
     	if (type == null || type.length() == 0) {
     		// default is iserv
     		type = InformationService.DOCTYPE;
     	}
 
         nodeSubprojectId = SubprojectServiceImpl.getSubprojectIdOrCreateDefault(session, nodeSubprojectId);
         DocumentModel soaNode = docService.findSoaNode(session, new SoaNodeId(nodeSubprojectId, type, name));
     	
     	if (soaMetamodelService.isAssignable(type, TaggingFolder.DOCTYPE)) {
     		// allowing to serve tags under same pattern
     		return doGetTagHTML(soaNode, subprojectId, visibility);
     	} else if (!soaNode.getFacets().contains(InformationService.FACET_INFORIMATIONSERVICEDATA)) {
     		return doGetSoaNodeHTML(soaNode, subprojectId, visibility);
     	}
 
         Template view = getView("servicedoc");
         if (soaNode != null) { // TODO else "not found"
 
             String subprojectCriteria = NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, visibility);
 
     		DocumentModel service = null;
     		DocumentModel serviceimpl = null;
     		DocumentModel productionImpl = null;
     		List<DocumentModel> actualImpls = null; // TODO also check platform, also nonActualNonMockImpls
     		List<DocumentModel> mockImpls = null;
     		List<DocumentModel> userConsumerImpls = null;
     		DocumentModel endpoint = null;
     		DocumentModel productionEndpoint = null;
     		List<DocumentModel> endpoints = null;
     		List<DocumentModel> actualEndpoints = null;
     		List<DocumentModel> mockEndpoints = null;
     		List<DocumentModel> userConsumerEndpoints = null;
         	if (soaMetamodelService.isAssignable(type, InformationService.DOCTYPE)) {
         		service = soaNode;
         	} else if (soaMetamodelService.isAssignable(type, ServiceImplementation.DOCTYPE)) {
         		serviceimpl = soaNode;
         		service = docService.getParentInformationService(serviceimpl);
         	} else if (soaMetamodelService.isAssignable(type, Endpoint.DOCTYPE)) {
         		endpoint = soaNode;
         		serviceimpl = docService.getParentServiceImplementation(endpoint);
         		if (serviceimpl != null) {
         		    service = docService.getParentInformationService(serviceimpl);
         		}
         	} // TODO else not supported, OR simple display
         	
         	
         	// Business
         	SoaNodeAdapter serviceAdapter = null;
         	DocumentModel businessService = null;
         	DocumentModel consumerActor = null;
         	DocumentModel providerActor = null;
         	DocumentModel component = null;
 			if (service != null) {
         		serviceAdapter = service.getAdapter(SoaNodeAdapter.class);
         	
         	    String businessServiceId = (String) service.getPropertyValue(InformationService.XPATH_LINKED_BUSINESS_SERVICE);
         	    if (businessServiceId != null && !businessServiceId.isEmpty()) {
         		    businessService = session.getDocument(new IdRef(businessServiceId));
         		
             	    String businessConsumerActorId = (String) businessService.getPropertyValue(BusinessService.XPATH_CONSUMER_ACTOR);
             	    if (businessConsumerActorId != null && !businessConsumerActorId.isEmpty()) {
                 		consumerActor = session.getDocument(new IdRef(businessConsumerActorId));
             		}
             	}
         	
         	    String providerActorId = (String) service.getPropertyValue(InformationService.XPATH_PROVIDER_ACTOR);
         	    if (providerActorId != null && !providerActorId.isEmpty()) {
         		    providerActor = session.getDocument(new IdRef(providerActorId));
         	    }
         	    String componentId = (String) service.getPropertyValue(InformationService.XPATH_COMPONENT_ID);
         	    if (componentId != null && !componentId.isEmpty()) {
         		    component = session.getDocument(new IdRef(componentId));
         	    }
         	
             }
 
         	// Implementations
             if (serviceimpl == null && service != null) {
 	            mockImpls = docService.getMockImplementationsOfServiceInCriteria(service, subprojectCriteria);
 	            actualImpls = docService.getActualImplementationsOfServiceInCriteria(service, subprojectCriteria);
             } // else only displaying selected impl
             
             // Endpoints
             if (endpoint == null) {
             	if (serviceimpl != null) {
                     endpoints = docService.getEndpointsOfImplementationInCriteria(serviceimpl, subprojectCriteria);
             	} else {
                     endpoints = docService.getEndpointsOfServiceInCriteria(service, subprojectCriteria);
             	}
             	actualEndpoints = new ArrayList<DocumentModel>(endpoints.size());
             	mockEndpoints = new ArrayList<DocumentModel>(endpoints.size());
             	for (DocumentModel curEndpoint : endpoints) {
             		String isMock = (String) curEndpoint.getPropertyValue(ServiceImplementationDataFacet.XPATH_ISMOCK);
             		if (isMock != null && Boolean.parseBoolean(isMock)) { // TODO & not placeholder
             			mockEndpoints.add(curEndpoint);
             		} else {
             			actualEndpoints.add(curEndpoint);
             		}
             	}
             } // else only displaying selected endpoint
             
             if (service != null) {
                 productionEndpoint = docService.getEndpointOfServiceInCriteria(service, Endpoint.ENV_PRODUCTION, subprojectCriteria);
                 if (productionEndpoint != null) {
                 	productionImpl = docService.getServiceImplementationFromEndpoint(productionEndpoint);
                 }
             } else if (serviceimpl != null) { // & !Endpoint.ENV_PRODUCTION.equals(endpoint.getPropertyValue("env:environment"))
                 productionEndpoint = docService.getEndpointOfImplementation(serviceimpl, Endpoint.ENV_PRODUCTION, subprojectCriteria);
                 productionImpl = serviceimpl;
             }
             if (productionEndpoint == null && actualImpls != null && !actualImpls.isEmpty()) {
             	productionImpl = actualImpls.get(0); // TODO also check it matches component
             }
             
             
 
 
             // user & roles :
             String userName = getCurrentUser();
             String testUser = request.getParameter("testUser");
             if (testUser != null && testUser.length() != 0 && getUserManager().getPrincipal(testUser) != null) {
             	userName = testUser;
             }
             NuxeoPrincipal user = getUserManager().getPrincipal(userName);
             
 
             // Getting suborganisation
             // get it from Actor group :
             // (which is defined company-wide, TODO LATER actually define them in a parent company / SI subproject & manage consistency)
             String userConsumerGroupName = getUserActorGroupName(user, consumerActor);
             boolean isUserConsumer = userConsumerGroupName != null;
             String userProviderGroupName = getUserActorGroupName(user, providerActor);
             boolean isUserProvider = userProviderGroupName != null;
             // LATER get it from Component NO because too costly, or at least use explicit concept of SubActor
             // LATER finer : get it from development project task management
             
             // Getting role :
             SubprojectId spId = SubprojectServiceImpl.parseSubprojectId(subprojectId);
             String userBusinessAnalystRoleGroupName = getUserRoleGroupName(spId, user, "Business Analyst");
             boolean isUserBusinessAnalyst = userBusinessAnalystRoleGroupName != null;
             String userDeveloperRoleGroupName = getUserRoleGroupName(spId, user, "Developer");
             boolean isUserDeveloper = userDeveloperRoleGroupName != null;
             String userOperatorRoleGroupName = getUserRoleGroupName(spId, user, "Operator");
             boolean isUserOperator = userOperatorRoleGroupName != null;
             
             // else (default) getting it from which Phase is current in visibility Context  :
             /*if (!isUserBusinessAnalyst && !isUserDeveloper && !isUserOperator) {
 	            if (spId.getVersion().length() == 0) {
 	            	isUserBusinessAnalyst = Subproject.SPECIFICATIONS_SUBPROJECT_NAME.equals(spId.getSubprojectName());
 	                isUserDeveloper = Subproject.REALISATION_SUBPROJECT_NAME.equals(spId.getSubprojectName());
 	                isUserOperator = Subproject.DEPLOIEMENT_SUBPROJECT_NAME.equals(spId.getSubprojectName());
 	            } else {
 	                isUserDeveloper = Subproject.SPECIFICATIONS_SUBPROJECT_NAME.equals(spId.getSubprojectName());
 	                isUserOperator = Subproject.REALISATION_SUBPROJECT_NAME.equals(spId.getSubprojectName());
 	            }
             }*/
             
             
             // if user consumer OR not provider (rarely consumer because on business
             // service), display his mock impls :
             if ((isUserConsumer || !isUserProvider)) {
             	if (serviceimpl != null) {
             		// NB. was not computed before in this case
            		mockImpls = docService.getMockImplementationsOfServiceInCriteria(service, subprojectCriteria);
             	}
             	if (mockImpls != null && endpoints != null) { // TODO or sub if for endpoints null ??
 	            	userConsumerImpls = new ArrayList<DocumentModel>(mockImpls.size());
 	            	userConsumerEndpoints = new ArrayList<DocumentModel>(endpoints.size());
 	            	for (DocumentModel mockImpl : mockImpls) {
 	                	DocumentModel mockImplDeliverable = docService.getSoaNodeParent(mockImpl, Deliverable.DOCTYPE);
 	                	String mockImplDeliverableApp = (String) mockImplDeliverable.getPropertyValue(Deliverable.XPATH_APPLICATION);
 	            		// checking if owner, by matching deliverable app to component app
 	                	String actorAppPrefix = (!isUserConsumer) ? null : consumerActor.getName() + '/';
 	                	if (mockImplDeliverableApp != null && (!isUserConsumer
 	                			|| mockImplDeliverableApp.startsWith(actorAppPrefix))) {
 	                		// NB. consumerActor != null because isUserConsumer
 	                		userConsumerImpls.add(mockImpl);
 	                		userConsumerEndpoints.addAll(docService.getEndpointsOfImplementationInCriteria(mockImpl, subprojectCriteria));
 	                	}
 	            	}
             	}
             }
             
             
             view = view
                     .arg("subproject", nodeSubprojectId)
                     .arg("soaNode", soaNode)
                     .arg("service", service)
                     .arg("businessService", businessService)
                     .arg("consumerActor", consumerActor)
                     .arg("providerActor", providerActor)
                     .arg("component", component)
                     .arg("serviceimpl", serviceimpl)
                     .arg("actualImpls", actualImpls)
                     .arg("mockImpls", mockImpls)
                     .arg("userConsumerImpls", userConsumerImpls)
                     .arg("endpoint", endpoint)
                     .arg("endpoints", endpoints)
                     .arg("productionEndpoint", productionEndpoint)
                     .arg("productionImpl", productionImpl)
                     .arg("userConsumerEndpoints", userConsumerEndpoints)
                     .arg("mockEndpoints", mockEndpoints)
                     .arg("actualEndpoints", actualEndpoints)
                     
                     .arg("user", user)
                     .arg("userName", userName)
                     .arg("isUserConsumer", isUserConsumer)
                     .arg("userConsumerGroupName", userConsumerGroupName)
                     .arg("isUserProvider", isUserProvider)
                     .arg("userProviderGroupName", userProviderGroupName)
                     .arg("isUserBusinessAnalyst", isUserBusinessAnalyst)
                     .arg("userBusinessAnalystRoleGroupName", userBusinessAnalystRoleGroupName)
                     .arg("isUserDeveloper", isUserDeveloper)
                     .arg("userDeveloperRoleGroupName", userDeveloperRoleGroupName)
                     .arg("isUserOperator", isUserOperator)
                     .arg("userOperatorRoleGroupName", userOperatorRoleGroupName)
                     
                     .arg("new_f", new freemarker.template.utility.ObjectConstructor())
                     // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                     // and not "new" else conflicts with Nuxeo's NewMethod helper
                     .arg("servicee", serviceAdapter)
                     
                     .arg("subprojectId", subprojectId)
                     .arg("visibility", visibility)
                     .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
         }
         return view;
     }
 
     /**
      * From actor name
      * TODO rename actor groups from SI DPS to AXXX/SI DPS
      * @param user
      * @param actor
      * @return
      */
     private String getUserActorGroupName(NuxeoPrincipal user, DocumentModel actor) {
     	if (actor == null) {
     		return null;
     	}
         // get it from Actor group :
         // (which is defined company-wide, TODO LATER actually define them in a parent company / SI subproject & manage consistency)
     	// TODO rename actor groups from SI DPS to AXXX/SI DPS according to said parent company
     	// because it is not the same as the company group of the user ex. OW Consulting !!!
         /*String actorGroupNamePrefix = "";
         boolean hasUserCompany = user.getCompany() != null && user.getCompany().length() != 0;
         if (hasUserCompany) {
         	actorGroupNamePrefix = user.getCompany() + '/';
         }
         String actorGroupName = actorGroupNamePrefix + actor.getName();*/
         String actorGroupName = actor.getName(); // TODO rather soaname ?? (though should be the same)
         if (user.isMemberOf(actorGroupName)) {
         	return actorGroupName;
         }
 		return null;
 	}
 
     /**
      * From (sub)project, company & global groups
      * @param spId if null only company & global groups
      * @param user
      * @param roleName
      * @return
      */
 	private String getUserRoleGroupName(SubprojectId spId, NuxeoPrincipal user, String roleName) {
 		if (spId != null) {
 	        // (?) getting it from (TODO versioned ???) subproject-local group :
 	        String subprojectWideGroupName = spId.getProjectName() + '/' + spId.getSubprojectName() + '/' + roleName;
 	        if (user.isMemberOf(subprojectWideGroupName)) {
 	        	return subprojectWideGroupName;
 	        }
 	        // TODO LATER also per component ??
 	        
 	        // (??) else getting it from project-local group :
 	        String projectWideGroupName = spId.getProjectName() + '/' + roleName;
 	        if (user.isMemberOf(projectWideGroupName)) {
 	        	return projectWideGroupName;
 	        }
 		}
         
         // LATER (?) else getting it from company / SI-wide group :
         boolean hasUserCompany = user.getCompany() != null && user.getCompany().length() != 0;
         if (hasUserCompany) {
             String companyWideGroupName = user.getCompany() + '/' + roleName;
         	if (user.isMemberOf(companyWideGroupName)) {
         		return companyWideGroupName;
         	}
         }
         
         // else getting it from global group :
         if (user.isMemberOf(roleName)) {
         	return roleName;
         }
         
         return null;
 	}
 
     private Object doGetSoaNodeHTML(DocumentModel soaNode, String subprojectId, String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
 
         Template view = getView("soaNode");
         return view
                 .arg("soaNode", soaNode)
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor())
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
     }
 
 	/*
 	@GET
     @Path("tag/path{tagSubprojectId:[^:]+}:{tagName:.+}") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doGetByTagHTML(@PathParam("tagSubprojectId") String tagSubprojectId, @PathParam("tagName") String tagName,
             @QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility) throws Exception {
             */
     private Object doGetTagHTML(DocumentModel tag, String subprojectId, String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
 
         String subprojectPathCriteria = NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, visibility);
 
         String tagSoaName = (String) tag.getPropertyValue(SoaNode.XPATH_SOANAME);
         String query = DocumentService.NXQL_SELECT_FROM
         		+ InformationService.DOCTYPE + DocumentService.NXQL_WHERE
         		+ InformationService.XPATH_PARENTSIDS + "/* = '" + tag.getType() + ":" + tagSoaName + "'" // TaggingFolder.DOCTYPE
         		+ subprojectPathCriteria;
         DocumentModelList tagServices = docService.query(session, query, true, false);
 
         Template view = getView("tagServices");
         return view
                 .arg("tag", tag)
                 .arg("tagServices", tagServices)
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor())
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
     }
 
     @GET
     @Path("path{serviceSubprojectId:[^:]+}:{serviceName:.+}/tags") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doGetTagsHTML(@PathParam("serviceSubprojectId") String serviceSubprojectId,
     		@PathParam("serviceName") String serviceName,
             @QueryParam("subprojectId") String subprojectId,
             @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
 
         serviceSubprojectId = SubprojectServiceImpl.getSubprojectIdOrCreateDefault(session, serviceSubprojectId);
 
         String subprojectCriteria = NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, visibility);
 
         //TODO ?? SubprojectID mandatory to find service ....
         DocumentModel service = docService.findSoaNode(session, new SoaNodeId(serviceSubprojectId, InformationService.DOCTYPE, serviceName));
         List<DocumentModel> tags = docService.getByTypeInCriteria(session, TaggingFolder.DOCTYPE, subprojectCriteria);
 
         Template view = getView("servicetags");
         // TODO problem here : A freemarker arg cannot be null or absent
         if (service != null) {
             view.arg("service", service);
         }
 
         return view
                 .arg("tags", tags)
                 .arg("new_f", new freemarker.template.utility.ObjectConstructor())
                 // see http://freemarker.624813.n4.nabble.com/best-practice-to-create-a-java-object-instance-td626021.html
                 // and not "new" else conflicts with Nuxeo's NewMethod helper
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
     }
 
     @POST
     @Path("path{serviceSubprojectId:[^:]+}:{serviceName:.+}/tags") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doPostTagsHTML(@PathParam("serviceSubprojectId") String serviceSubprojectId,
     		@PathParam("serviceName") String serviceName, @FormParam("tagId") String tagId,
             @QueryParam("subprojectId") String subprojectId, @QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentService docService = Framework.getService(DocumentService.class);
 
         serviceSubprojectId = SubprojectServiceImpl.getSubprojectIdOrCreateDefault(session, serviceSubprojectId);
 
         subprojectId = SubprojectServiceImpl.getSubprojectIdOrCreateDefault(session, subprojectId);
 
         DocumentModel service = docService.findSoaNode(session, new SoaNodeId(serviceSubprojectId, InformationService.DOCTYPE, serviceName));
         DocumentModel tag = session.getDocument(new IdRef(tagId));
 
         if (service != null && tag != null) {
             DocumentService documentService = Framework.getService(DocumentService.class);
             documentService.create(session, documentService.createSoaNodeId(service), tag.getPathAsString());
             session.save();
         }
         return doGetTagsHTML(serviceSubprojectId, serviceName, subprojectId, visibility);
     }
 
     //@DELETE // doesn't work from browser
     @POST
     @Path("proxy/{documentId:.+}") // TODO encoding
     @Produces(MediaType.TEXT_HTML)
     public Object doDeleteProxyHTML(@PathParam("documentId") String documentId, @FormParam("delete") String delete,
     		@QueryParam("visibility") String visibility) throws Exception {
         CoreSession session = SessionFactory.getSession(request);
         DocumentModel serviceProxy = session.getDocument(new IdRef(documentId));
 
         if (serviceProxy != null) {
             DocumentModel proxyParentDocument = session.getParentDocument(serviceProxy.getRef()); // TODO does it work ???
             DocumentModel proxiedService = session.getSourceDocument(serviceProxy.getRef());
 
             session.removeDocument(serviceProxy.getRef());
             session.save();
 
             String subprojectId = (String) proxyParentDocument.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT);
             String serviceSubprojectId = (String) proxiedService.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT);
             String serviceName = (String) proxiedService.getPropertyValue(InformationService.XPATH_SOANAME);
             return doGetTagsHTML(serviceSubprojectId, serviceName, subprojectId, visibility); // TODO removing lead slash
         }
         return doGetHTML(null, visibility); //TODO better
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Object doGetJSON() throws Exception {
         return null; // TODO
     }
 
     @GET
     @Path("matchingFull")
     @Produces(MediaType.TEXT_HTML)
     public Object doGetMatchingFullPageHTML(@QueryParam("subprojectId") String subprojectId,
     		@QueryParam("visibility") String visibility) throws Exception {
 
         CoreSession session = SessionFactory.getSession(request);
 
         // Indicators
         IndicatorsController indicatorsController = new IndicatorsController();
         Map<String, IndicatorValue> indicators = indicatorsController.computeIndicators(session, null, null, subprojectId, visibility);
 
         return getView("matchingFull")
                 .arg("subprojectId", subprojectId)
                 .arg("visibility", visibility)
                 .arg("indicators", indicators)
                 .arg("contextInfo", ContextData.getVersionData(session, subprojectId));
     }
 
 }
