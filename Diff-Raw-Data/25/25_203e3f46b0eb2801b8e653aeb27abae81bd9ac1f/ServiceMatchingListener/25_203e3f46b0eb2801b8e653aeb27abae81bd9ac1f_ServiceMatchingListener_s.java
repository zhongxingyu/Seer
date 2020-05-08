 package org.easysoa.registry;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.matching.Query;
 import org.easysoa.registry.types.Deliverable;
 import org.easysoa.registry.types.InformationService;
 import org.easysoa.registry.types.RestInfo;
 import org.easysoa.registry.types.ServiceImplementation;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.event.Event;
 import org.nuxeo.ecm.core.event.EventContext;
 import org.nuxeo.ecm.core.event.EventListener;
 import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * 
  * @author mkalam-alami
  *
  */
 public class ServiceMatchingListener implements EventListener {
 
     private static Logger logger = Logger.getLogger(ServiceMatchingListener.class);
     
     @Override
     public void handleEvent(Event event) throws ClientException {
     	
         // Ensure event nature
         EventContext context = event.getContext();
         if (!(context instanceof DocumentEventContext)) {
             return;
         }
         DocumentEventContext documentContext = (DocumentEventContext) context;
         DocumentModel sourceDocument = documentContext.getSourceDocument();
         CoreSession documentManager = documentContext.getCoreSession();
 		DocumentService documentService;
 		try {
 			documentService = Framework.getService(DocumentService.class);
 		} catch (Exception e) {
 			logger.error("Document service unavailable, aborting");
 			return;
 		}
 
         // Service impl: Link to information service
         if (documentService.isTypeOrSubtype(documentManager, sourceDocument.getType(), ServiceImplementation.DOCTYPE)) {
        	lookForAnInformationServiceToMatch(documentManager, documentService, sourceDocument, 
        			!event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE));
         }
 
         // Information service: Find matching serviceimpls
         if (documentService.isTypeOrSubtype(documentManager, sourceDocument.getType(), InformationService.DOCTYPE)) {
         	String serviceImplQuery = NXQLQueryBuilder.getQuery("SELECT * FROM " + ServiceImplementation.DOCTYPE + 
         			" WHERE " + ServiceImplementation.XPATH_WSDL_PORTTYPE_NAME + " = '?'",
         			new Object[] { sourceDocument.getPropertyValue(InformationService.XPATH_WSDL_PORTTYPE_NAME) },
         			false, true);
         	DocumentModelList foundServiceImpls = documentService.query(documentManager,
         			serviceImplQuery, true, false);
         	if (foundServiceImpls.size() > 0) {
         		if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
 	        		documentManager.saveDocument(sourceDocument);
 	        		documentManager.save();
         		}
             	for (DocumentModel serviceImpl : foundServiceImpls) {
            		lookForAnInformationServiceToMatch(documentManager, documentService, serviceImpl, true);
             	}
         	}
         }
         
 	}
 
	private void lookForAnInformationServiceToMatch(CoreSession documentManager,
			DocumentService documentService, DocumentModel serviceImplModel, boolean save) throws ClientException {
     	if (serviceImplModel.getPropertyValue(ServiceImplementation.XPATH_LINKED_INFORMATION_SERVICE) == null) {
         	DocumentModelList foundInfoServices = findIServicesForImpl(documentManager, serviceImplModel);
         	if (foundInfoServices != null && foundInfoServices.size() > 0) {
         		serviceImplModel.setPropertyValue(ServiceImplementation.XPATH_LINKED_INFORMATION_SERVICE,
         				foundInfoServices.get(0).getId());
        		if (save) {
        			documentManager.saveDocument(serviceImplModel);
        			documentManager.save();
        		}
         	} // else none, or too much (go to matching dashboard)
         }
 	}
 	
 	public DocumentModelList findIServicesForImpl(CoreSession documentManager, DocumentModel impl)
 			throws PropertyException, ClientException {
 		DocumentService documentService;
 		try {
 			documentService = Framework.getService(DocumentService.class);
 		} catch (Exception e) {
 			logger.error("Document service unavailable, aborting");
 			return null;
 		}
 
     	String implReferredSubProjectIds = "AXXXSpecifications"; // or in 2 pass & get it from subProject ?? 
     	String implSubProjectId = "AXXXRealisation";
     	//String matchingGuideComponentId = (String) impl.getPropertyValue(ServiceImplementation.XPATH_COMPONENT_ID); // TODO check existence at source disco start
 
     	//String implIDE = "Eclipse"; // OPT
     	String implLanguage = (String) impl.getPropertyValue(ServiceImplementation.XPATH_LANGUAGE); // "Java"; // TODO from source disco
     	String implBuild = "Maven"; // MavenPom ?? or Ivy ; or in top-level deliverable ?? TODO from source disco or deduced from del:nature if possible
     	String implDeliverableNature = (String) impl.getPropertyValue(Deliverable.XPATH_NATURE); // "Maven" ; MavenArtifact ? copied from Deliverable
     	String implDeliverableRepositoryUrl = (String) impl.getPropertyValue(Deliverable.XPATH_REPOSITORY_URL); // "http://maven.nuxeo.org/nexus/content/groups/public" ; acts as id, copied from Deliverable TODO add in source disco
     	String implServiceLanguage = (String) impl.getPropertyValue(ServiceImplementation.XPATH_TECHNOLOGY); // JAXWS, JAXRS
 
     	Query query = new Query("SELECT * FROM " + InformationService.DOCTYPE);
 
     	query.addOptionalCriteria("platform:language", implLanguage);
     	query.addOptionalCriteria("platform:build", implBuild);
     	query.addOptionalCriteria("platform:deliverableNature", implDeliverableNature);
     	query.addOptionalCriteria("platform:deliverableRepositoryUrl", implDeliverableRepositoryUrl);
     	query.addOptionalCriteria("platform:serviceLanguage", implServiceLanguage);
     	/*
     			" WHERE  " +
     			//"soan:subProjectId IN " + implReferredSubProjectIds + "' OR soan:subProjectId='" + implSubProjectId + "'" +
     			//" AND impl:componentId='" + matchingGuideComponentId + "'" + // if any
     			
     			"    platform:language='" + implLanguage + "'" + // if any ; OPT multiple options (consistency handled in logic)
             	" AND platform:build='" + implBuild + "'" + // if any
             	" AND platform:deliverableNature='" + implDeliverableNature + "'" + // if any
             	" AND platform:deliverableRepositoryUrl='" + implDeliverableRepositoryUrl + "'" + // if any
     			" AND platform:serviceLanguage='" + implServiceLanguage + "'"; // if any ; OPT multiple options (consistency handled in logic)
     	*/
     	
     	boolean isWsdl = impl.hasFacet("WsdlInfo"); // TODO impl.hasFacet("WsdlInfo");
     	boolean isRest = impl.hasFacet("RestInfo");// OPT dynamic
     	// or platform:serviceDefinition=WSDL|JAXRS ?
     	
     	if (isWsdl) { // consistency logic
         	String implPortTypeName = (String) impl.getPropertyValue(ServiceImplementation.XPATH_WSDL_PORTTYPE_NAME); // if JAXWS
         	query.addCriteria("ecm:mixinType = 'WsdlInfo'");
         	query.addCriteria(InformationService.XPATH_WSDL_PORTTYPE_NAME, implPortTypeName);
         			
     	} else if (isRest) {
         	String implRestPath = (String) impl.getPropertyValue(RestInfo.XPATH_REST_PATH); // if JAXRS
         	//OPT String implMediaType = (String) impl.getPropertyValue(ServiceImplementation.XPATH_REST_MEDIA_TYPE); // if JAXRS
     		//infoServiceQueryString +=
 					//" AND ecm:mixinType = 'RestInfo' AND " +
         			//" AND " InformationService.XPATH_REST_PATH + "='" + implRestPath + "'" +
         			//OPT " AND " InformationService.XPATH_REST_MEDIA_TYPE + "='" + implMediaType + "'" +
         			
     	}
 
     	/*
     	//////////////////////////////////////////////////////////
 		// only for discovery in web or message monitoring :
     	String endpointServiceTransport = "HTTP"; // if WS extracted from WSDL binding/transport=="http://schemas.xmlsoap.org/soap/http"
     	// NB. endpointServiceProtocol=WS|REST is implied by platform:serviceDefinition=WSDL|JAXRS
     	
 		infoServiceQueryString +=
 				" AND platform:serviceTransport='" + endpointServiceTransport + "'"; // if any
 		boolean isHttp = "HTTP".equals(endpointServiceTransport);
 		if (isHttp) { // consistency logic
         	String endpointServiceTransportHttpContentType = "application/json+nxautomation";
         	String endpointServiceTransportHttpContentKind = "XML"; // deduced from ContentType if possible, else from message monitoring
     		infoServiceQueryString +=
     				" AND platform:serviceTransport='" + endpointServiceTransportHttpContentType + "'" +
     				" AND platform:serviceTransport='" + endpointServiceTransportHttpContentKind + "'"; // if any
 		}
 		
 		// OPT String endpointServiceRuntime = "CXF";
 		// OPT appServer=Apache Tomcat|Jetty
 		// NB. endpointProtocolServer (Apache Tomcat, Jetty) is different but not as interesting
 		*/
     	
     	String infoServiceQuery = query.build();
     	DocumentModelList foundInfoServices = documentService.query(documentManager,
     			infoServiceQuery, true, false);
     	return foundInfoServices;
 	}
 
 }
