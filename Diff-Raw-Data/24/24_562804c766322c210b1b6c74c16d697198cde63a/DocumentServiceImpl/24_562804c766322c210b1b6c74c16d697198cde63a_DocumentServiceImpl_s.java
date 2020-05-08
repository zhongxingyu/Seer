 package org.easysoa.registry;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.easysoa.registry.facets.ServiceImplementationDataFacet;
 import org.easysoa.registry.types.Component;
 import org.easysoa.registry.types.Deliverable;
 import org.easysoa.registry.types.Endpoint;
 import org.easysoa.registry.types.InformationService;
 import org.easysoa.registry.types.IntelligentSystem;
 import org.easysoa.registry.types.ServiceConsumption;
 import org.easysoa.registry.types.ServiceImplementation;
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.types.SubprojectNode;
 import org.easysoa.registry.types.ids.SoaNodeId;
 import org.easysoa.registry.utils.DocumentModelHelper;
 import org.easysoa.registry.utils.NXQLQueryHelper;
 import org.easysoa.registry.utils.NuxeoListUtils;
 import org.easysoa.registry.utils.RepositoryHelper;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
 import org.nuxeo.ecm.core.query.sql.NXQL;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.ComponentContext;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 public class DocumentServiceImpl extends DefaultComponent implements DocumentService {
 
 	private Properties properties;
 
     @Override
     public void activate(ComponentContext context) throws Exception {
         super.activate(context);
 
         // Loading configuration
         // NB. accesses the file in config/ using bundle resource (like SchedulerImpl)
         // rather than using Environment.getDefault().getConfig() (like Composer.createMailer())
         // TODO LATER maybe rather as an extension point / contribution ?? else in its own service
     	properties = new Properties();
         URL cfg = context.getRuntimeContext().getResource("config/easysoa.properties");
         if (cfg != null) {
             InputStream cfgIn = cfg.openStream();
             try {
             	properties.load(cfgIn);
             } finally {
             	cfgIn.close();
             }
         } //  will have to use default config (unit tests...)
     }
 
 	public Properties getProperties() {
 		return properties;
 	}
 
     /* (non-Javadoc)
      * @see org.easysoa.registry.DocumentService#createDocument(org.nuxeo.ecm.core.api.CoreSession, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
      */
     public DocumentModel createDocument(CoreSession documentManager,
             String doctype, String name, String parentPath, String title) throws ClientException {
         // TODO if doctype belongs to SoaNode subtypes, throw new Exception("createDocument() doesn't work for SoaNode types, rather use create()")
         DocumentModel documentModel = documentManager.createDocumentModel(doctype);
         documentModel.setPathInfo(parentPath, safeName(name));
         documentModel.setPropertyValue(SoaNode.XPATH_TITLE, title);
         // TODO fully recursive spnode copy, to ease setting spnode props (in SubprojectNode event listener) ??
         //if (!documentModel.hasFacet("SubprojectNode")) {
         //    documentModel.addFacet("SubprojectNode");
         //    }
         //documentModel.setPropertyValue(SubprojectNode.XPATH_SUBPROJECT, subprojectId);
         // TODO or only if facet ??
         documentModel = documentManager.createDocument(documentModel);
         return documentModel;
     }
 
     /* (non-Javadoc)
      * @see org.easysoa.registry.DocumentService#create(org.nuxeo.ecm.core.api.CoreSession, org.easysoa.registry.types.ids.SoaNodeId, java.lang.String)
      */
     public DocumentModel create(CoreSession documentManager, SoaNodeId identifier, String parentPath) throws ClientException {
         String doctype = identifier.getType();
 
         if (isSoaNode(documentManager, doctype)) {
 
             // Create or fetch source document
             DocumentModel documentModel = findSoaNode(documentManager, identifier);
         	if (documentModel == null) {
                 documentModel = newSoaNodeDocument(documentManager, identifier);
                 documentModel = documentManager.createDocument(documentModel);
         	}
 
             // Create proxy if needed (but make sure the parent is the instance of the repository,
             // otherwise the child proxy will only be visible in the context of the parent proxy)
             boolean createProxy = !parentPath.equals(getSourceFolderPath(documentManager, identifier)); // XXX Redundant with RepositoryManagementListener?
             if (createProxy) {
                 PathRef parentRef = new PathRef(parentPath);
                 DocumentModel parentModel = documentManager.getDocument(parentRef);
                 if (parentModel != null) {
                     if (parentModel.isProxy()) {
                         parentModel = findSoaNode(documentManager, createSoaNodeId(parentModel));
                     }
                 }
                 else {
                     throw new ClientException("Invalid parent path: " + parentPath);
                 }
 
                 DocumentModel existingProxy = null;
             	DocumentModelList foundProxies = findProxies(documentManager, identifier);
             	for (DocumentModel foundProxy : foundProxies) {
             		if (foundProxy.getParentRef().equals(parentModel.getRef())) {
             			existingProxy = foundProxy;
             			break;
             		}
             	}
 
             	if (existingProxy == null) {
             		return documentManager.createProxy(documentModel.getRef(), parentModel.getRef());
             	}
             	else {
             		return existingProxy;
             	}
             }
             else {
                 return documentModel;
             }
         }
         else {
             return null;
         }
     }
 
     /**
      * If a document of the same identifier exists, returns it. Else creates a new document
      * and puts it in the repository.
      * BEWARE : it triggers documentCreate event (so don't use it if properties have still to be set
      * ex. in DiscoveryServiceImpl or EndpointMatchingServiceImpl.linkInformationServiceThroughPlaceholder()
      * but rather use find() then newSoaNodeDocument())
      * Works only with SoaNode types (returns null otherwise).
      *
      * @throws ClientException
      */
     public DocumentModel create(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
         DocumentModel documentModel = null;
 
         if (isSoaNode(documentManager, identifier.getType())) {
         	documentModel = findSoaNode(documentManager, identifier);
         	if (documentModel == null) {
                 documentModel = newSoaNodeDocument(documentManager, identifier);
                 documentModel = documentManager.createDocument(documentModel);
         	}
         }
 
         return documentModel;
     }
 
     public DocumentModel newSoaNodeDocument(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
         return newSoaNodeDocument(documentManager, identifier, null);
     }
     /* (non-Javadoc)
      * @see org.easysoa.registry.DocumentService#newSoaNodeDocument(org.nuxeo.ecm.core.api.CoreSession, org.easysoa.registry.SoaNodeId)
      */
     public DocumentModel newSoaNodeDocument(CoreSession documentManager, SoaNodeId identifier,
             Map<String, Serializable> nuxeoProperties) throws ClientException {
         String subprojectId = SubprojectServiceImpl.setDefaultSubprojectIfNone(documentManager, identifier);
         String doctype = identifier.getType();
         String name = identifier.getName();
         DocumentModel sourceFolder = getSourceFolder(documentManager, subprojectId, doctype); // ensuring it exists at the same time
         DocumentModel documentModel = documentManager.createDocumentModel(doctype);
         documentModel.setPathInfo(sourceFolder.getPathAsString(), safeName(name));
 
         // setting default props from SOA node id
         documentModel.setPropertyValue(SoaNode.XPATH_TITLE, name);
 
         // other default properties : (user ex. for Endpoint)
         for (Entry<String, Serializable> defaultPropertyValue : identifier.getDefaultPropertyValues().entrySet()) {
             documentModel.setPropertyValue(defaultPropertyValue.getKey(), defaultPropertyValue.getValue());
         }
 
         if (nuxeoProperties != null) {
             // user-provided properties
             for (Entry<String, Serializable> nuxeoProperty : nuxeoProperties.entrySet()) {
                 String propertyKey = nuxeoProperty.getKey();
                 Serializable propertyValue = nuxeoProperty.getValue();
                 documentModel.setPropertyValue(propertyKey, propertyValue);
             }
         }
 
         // setting props than must not be overriden by user-provided props
         documentModel.setPropertyValue(SoaNode.XPATH_SOANAME, name);
         documentModel.setPropertyValue(SubprojectNode.XPATH_SUBPROJECT, subprojectId);
 
         // TODO copy spnode metas, or in listener, or using facet inheritance ??
         return documentModel;
     }
 
 	/**
      * Transforms the parameter into a string suitable for use as Nuxeo's name.
      */
     private String safeName(String name) {
 		return name.replace('/', '|');
 	}
 
     /**
      * TODO across subproject
      */
 	@Override
     public DocumentModel copy(CoreSession documentManager, DocumentModel sourceModel, DocumentRef ref) throws ClientException {
         if (sourceModel.isProxy()) {
             return documentManager.copy(sourceModel.getRef(), ref, sourceModel.getName());
         }
         else {
             return documentManager.createProxy(sourceModel.getRef(), ref);
         }
     }
 
     @Override
     public boolean delete(CoreSession documentManager, SoaNodeId soaNodeId) throws ClientException {
         DocumentModel sourceDocument = findSoaNode(documentManager, soaNodeId);
         if (sourceDocument != null) {
             documentManager.removeDocument(sourceDocument.getRef());
             return true;
         }
         else {
             return false;
         }
     }
 
     @Override
     public boolean deleteProxy(CoreSession documentManager, SoaNodeId soaNodeId, String parentPath)
             throws ClientException {
         DocumentModelList instances = findAllInstances(documentManager, soaNodeId);
         for (DocumentModel instance : instances) {
             if (instance.getPath().removeLastSegments(1).equals(new Path(parentPath))) {
                 documentManager.removeDocument(instance.getRef());
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public DocumentModel findDocument(CoreSession documentManager,
             String subprojectId, String type, String name) throws ClientException {
         return findDocument(documentManager, subprojectId, type, name, false); ///TODO ??!!
     }
 
     @Override
     public DocumentModel findDocument(CoreSession documentManager,
             String subprojectId, String type, String name, boolean deepSearch) throws ClientException {
 
         StringBuilder queryString = new StringBuilder();
         queryString.append("SELECT * FROM ? WHERE ");
         queryString.append(NXQL.ECM_NAME);
         queryString.append(" = '?' ");
         queryString.append(NXQLQueryHelper.buildSubprojectCriteria(documentManager,
         		SubprojectServiceImpl.getSubprojectIdOrCreateDefault(documentManager, subprojectId), deepSearch));
 
         String query = NXQLQueryBuilder.getQuery(queryString.toString(),
                 new Object[] { type, safeName(name) },
                 false, true);
         DocumentModelList results = this.query(documentManager, query, true, false);
         return results.size() > 0 ? results.get(0) : null;
     }
 
     @Override
     public DocumentModel findSoaNode(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
         return findSoaNode(documentManager, identifier, false); // TODO true not supported in discovery
         // for now, else allowing several SOA IDs (in different subproject / Phase) for a single node ;
         // at worse it should create an "inheriting" one
     }
 
     @Override
     public DocumentModel findSoaNode(CoreSession documentManager, SoaNodeId identifier, boolean deepSearch) throws ClientException {
 
         StringBuilder queryString = new StringBuilder();
         queryString.append("SELECT * FROM ? WHERE ");
 
         // checking SOA name :
         queryString.append(SoaNode.XPATH_SOANAME);
         queryString.append(" = '?' ");
 
         // checking subproject :
         queryString.append(NXQLQueryHelper.buildSubprojectCriteria(documentManager,
         		SubprojectServiceImpl.setDefaultSubprojectIfNone(documentManager, identifier), deepSearch));
         // NB. not using subproject path because doesn't work on versions
 
         // NB. not checking that it's below the Repository (in ex. REPOSITORY_PATH = /default-domain/MyProject/Default/Repository)
         // because costlier, so check it in RepositoryManagementListener when still putting it there !
         /// if (!resDoc.isVersion()) if (!resDoc.startsWith(RepositoryHelper.getRepositoryPath(documentManager, subprojectId)) return null;
 
         String query = NXQLQueryBuilder.getQuery(queryString.toString(),
                 // TODO must be
                 new Object[] { identifier.getType()/*, Repository.REPOSITORY_PATH*/, identifier.getName() },
                 false, true);
         DocumentModelList results = query(documentManager, query, true, false);
         return results.size() > 0 ? results.get(0) : null;
     }
 
     @Override
     public DocumentModelList findProxies(CoreSession documentManager, SoaNodeId identifier)
             throws ClientException {
         String query = NXQLQueryBuilder.getQuery("SELECT * FROM ? WHERE " + SoaNode.XPATH_SOANAME
                 + " = '?' AND " + SubprojectNode.XPATH_SUBPROJECT + " = '?'",
                 new Object[] { identifier.getType(), identifier.getName(),
                 SubprojectServiceImpl.setDefaultSubprojectIfNone(documentManager, identifier) },
                 false, true);
         return query(documentManager, query, false, true);
     }
 
     public DocumentModelList findProxies(CoreSession documentManager, DocumentModel model) throws ClientException {
         if (!model.isProxy()) {
             return documentManager.getProxies(model.getRef(), null);
         }
         else {
             return findProxies(documentManager, createSoaNodeId(model));
         }
     }
 
     @Override
     public DocumentModel findProxy(CoreSession documentManager, SoaNodeId identifier,
             String parentPath) throws ClientException {
         // Check parent
         PathRef parentRef = new PathRef(parentPath);
         DocumentModel parentModel = documentManager.getDocument(parentRef);
         if (parentModel.isProxy()) {
             parentModel = findSoaNode(documentManager, createSoaNodeId(parentModel));
         }
 
         // Find proxy among children
         DocumentModelList childrenModels = documentManager.getChildren(parentModel.getRef());
         for (DocumentModel childModel : childrenModels) {
             if (this.isSoaNode(documentManager, childModel.getType()) // else case of .doc in biz archi along BS
                     && createSoaNodeId(childModel).equals(identifier)) {
                 return childModel;
             }
         }
         return null;
     }
 
     public DocumentModelList findAllInstances(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
     	if (identifier == null) {
     		return new DocumentModelListImpl();
     	}
         String query = NXQLQueryBuilder.getQuery("SELECT * FROM ? WHERE " + SoaNode.XPATH_SOANAME
                 + " = '?' AND " + SubprojectNode.XPATH_SUBPROJECT + " = '?'",
                 new Object[] { identifier.getType(), identifier.getName(),
                 SubprojectServiceImpl.setDefaultSubprojectIfNone(documentManager, identifier) },
                 false, true);
         return query(documentManager, query, false, false);
     }
 
     public DocumentModelList query(CoreSession documentManager, String query,
     		boolean nonProxiesCriteria, boolean proxiesCriteria) throws ClientException {
     	String filteredQuery = query +
         		((nonProxiesCriteria) ? NQXL_NON_PROXIES_CRITERIA : "") +
                 ((proxiesCriteria) ? NQXL_PROXIES_CRITERIA : "") +
         		NXQL_NO_DELETED_DOCUMENTS_CRITERIA;
     	if (!filteredQuery.contains("WHERE")) {
     		filteredQuery = filteredQuery.replaceFirst("AND", "WHERE");
     	}
         return documentManager.query(filteredQuery);
     }
 
     public DocumentModelList findAllInstances(CoreSession documentManager, DocumentModel model) throws ClientException {
 		return findAllInstances(documentManager, createSoaNodeId(model));
     }
 
     public DocumentModelList findAllParents(CoreSession documentManager, DocumentModel documentModel) throws Exception {
         // Find working copy of document
         DocumentModel sourceDocument;
         if (documentModel.isProxy()) {
         	sourceDocument = documentManager.getSourceDocument(documentModel.getRef());
         }
         else {
         	sourceDocument = documentModel;
         }
 
         // Build proxies list
         DocumentModelList modelInstances = documentManager.getProxies(documentModel.getRef(), null);
         modelInstances.add(sourceDocument);
 
         // Fetch parents
         DocumentModelList parents = new DocumentModelListImpl();
         for (DocumentModel modelInstance : modelInstances) {
             DocumentModel parentDocument = documentManager.getParentDocument(modelInstance.getRef());
             if (parentDocument != null // happens if modelInstance is a version
             		&& !parents.contains(parentDocument)) {
                 parents.add(parentDocument);
             }
         }
         return parents;
     }
 
     @Override
     public boolean hasChild(CoreSession documentManager, DocumentModel document,
             SoaNodeId childId) throws ClientException {
         if (document != null && childId != null) {
             DocumentModelList children = documentManager.getChildren(document.getRef(), childId.getType());
             for (DocumentModel child : children) {
                 if (createSoaNodeId(child).equals(childId)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public String getSourceFolderPath(CoreSession documentManager,
             SoaNodeId soaNodeId) throws PropertyException, ClientException {
         return getSourceFolderPath(documentManager, soaNodeId.getSubprojectId(), soaNodeId.getType());
     }
 
     public String getSourceFolderPath(CoreSession documentManager,
             DocumentModel spNode) throws PropertyException, ClientException {
         String subprojectId = (String) spNode.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT);
         String doctype = spNode.getType();
         return getSourceFolderPath(documentManager, subprojectId, doctype);
     }
 
     public String getSourceFolderPath(CoreSession documentManager,
             String subprojectId, String doctype) throws ClientException {
         String repositoryPath = RepositoryHelper.getRepositoryPath(documentManager, subprojectId);
         return getSourceFolderPathBelowSubprojectRepository(repositoryPath, doctype);
     }
 
     public String getSourceFolderPathBelowSubprojectRepository(
     		String subprojectRepositoryPath, String doctype) throws ClientException {
         return subprojectRepositoryPath + '/' + doctype;
     }
 
     public DocumentModelList getChildren(CoreSession session, DocumentRef parentRef, String type) throws ClientException {
         parentRef = session.getSourceDocument(parentRef).getRef(); // making sure it's not a proxy
         return session.getChildren(parentRef, type);
     }
     public DocumentModelList getChildren(DocumentModel docModel, String type) throws ClientException {
     	docModel = docModel.getCoreSession().getSourceDocument(docModel.getRef()); // making sure it's not a proxy
 		//return soaNodeModel.getCoreSession().getChildren(docModel.getRef(), type); // NO doesn't handle subtypes
 		return query(docModel.getCoreSession(), NXQL_SELECT_FROM + type + NXQL_WHERE
 				+ "ecm:parentId='" + docModel.getRef().toString() + "'", false, false);
     }
 
     public DocumentModel getSourceFolder(CoreSession documentManager,
             DocumentModel spNode) throws ClientException {
         String subprojectId = (String) spNode.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT);
         return getSourceFolder(documentManager, subprojectId, spNode.getType());
     }
 
     public DocumentModel getSourceFolder(CoreSession documentManager,
             String subprojectId, String doctype) throws ClientException {
         PathRef sourceFolderRef = new PathRef(getSourceFolderPath(documentManager, subprojectId, doctype));
         // TODO not using PathRef ??
         if (documentManager.exists(sourceFolderRef)) {
             return documentManager.getDocument(sourceFolderRef);
         } else {
             DocumentModel subprojectRepository = RepositoryHelper.getRepository(documentManager, subprojectId);
             DocumentModel sourceFolderModel = documentManager.createDocumentModel(
                     subprojectRepository.getPathAsString(), doctype, IntelligentSystem.DOCTYPE);
             sourceFolderModel.setPropertyValue("dc:title", DocumentModelHelper.getDocumentTypeLabel(doctype) + "s");
             sourceFolderModel = documentManager.createDocument(sourceFolderModel);
             return sourceFolderModel;
         }
     }
 
     @Override
     public SoaNodeId createSoaNodeId(DocumentModel model) throws ClientException {
         try {
             return new SoaNodeId((String) model.getPropertyValue(SubprojectNode.XPATH_SUBPROJECT),
                     model.getType(), (String) model.getPropertyValue(SoaNode.XPATH_SOANAME));
         }
         catch (PropertyNotFoundException e) {
             throw new ClientException("Invalid document type (" + model.getType() + "), an SoaNode is expected");
         }
     }
 
     @Override
     public List<SoaNodeId> createSoaNodeIds(DocumentModel... models) throws PropertyException, ClientException {
         List<SoaNodeId> soaNodeIds = new ArrayList<SoaNodeId>(models.length);
         for (DocumentModel model : models) {
             soaNodeIds.add(createSoaNodeId(model));
         }
         return soaNodeIds;
     }
 
     public boolean isSoaNode(CoreSession documentManager, String doctype) throws ClientException {
         return documentManager.getDocumentType(doctype).hasSchema(SoaNode.SCHEMA);
     }
 
 	@Override
 	public boolean isTypeOrSubtype(CoreSession documentManager,
 			String doctypeToTest, String expectedDoctype)
 			throws ClientException {
 		/*if (doctypeToTest == null || expectedDoctype == null) {
 			return false;
 		}
 		if (doctypeToTest.equals(expectedDoctype)) {
 			return true;
 		}
 		DocumentType documentType = documentManager.getDocumentType(doctypeToTest);
 		for (Type parentType : documentType.getTypeHierarchy()) {
 			if (parentType.getName().equals(expectedDoctype)) {
 				return true;
 			}
 		}
 		return false;*/
 		SoaMetamodelService soaMetamodelService;
 		try {
 			soaMetamodelService = Framework.getService(SoaMetamodelService.class);
 		} catch (Exception e) {
 			throw new ClientException("Unable to get SoaMetamodelService", e);
 		}
 		return soaMetamodelService.isAssignable(doctypeToTest, expectedDoctype);
 	}
 
 	/**
      * @obsolete actual code is in Service/EndpointMatchingService
 	 * TODO test component-scoped request in actual scenario
 	 * TODO impl multi component-scoped request
 	 */
 	public DocumentModel findEndpoint(CoreSession documentManager,
 			SoaNodeId identifier, Map<String, Object> properties,
 			List<SoaNodeId> suggestedParentIds /*NOT USED*/, List<SoaNodeId> knownComponentIds)
 					throws ClientException {
 
         ArrayList<Object> params = new ArrayList<Object>(5);
         params.add(identifier.getType());
         params.add(identifier.getName()); // TODO MDU and not safeName(identifier.getName()) !?
         StringBuffer querySbuf = new StringBuffer("SELECT * FROM ? WHERE "
         		+ SoaNode.XPATH_SOANAME + " = '?'"); // environment:url);
 
         // query context restricted to known components :
         if (knownComponentIds != null) {
 	        for (SoaNodeId knownComponentId : knownComponentIds) {
 	        	querySbuf.append(" AND ? IN soan:parentIds");
 	        	params.add(knownComponentId);
 	        }
         }
 
         // match extracted metas to service'as :
         Object portTypeName = properties.get(Endpoint.XPATH_WSDL_PORTTYPE_NAME);
         Object serviceName = properties.get(Endpoint.XPATH_WSDL_SERVICE_NAME);
         querySbuf.append(" AND " + InformationService.XPATH_WSDL_PORTTYPE_NAME + " = '?'");
 		params.add(portTypeName != null ? portTypeName : "");
         querySbuf.append(" AND " + InformationService.XPATH_WSDL_SERVICE_NAME + " = '?'");
         params.add(serviceName != null ? serviceName : "");
 
 		String query = NXQLQueryBuilder.getQuery(querySbuf.toString(), params.toArray(), false, true);
         DocumentModelList results = this.query(documentManager, query, true, false);
         DocumentModel documentModel = results.size() > 0 ? results.get(0) : null;
 		return documentModel;
 	}
 
 
 
 	@Override
 	public List<DocumentModel> getByType(CoreSession session, String type, String subprojectId) throws ClientException {
 		return getByTypeInCriteria(session, type, NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getByTypeInCriteria(CoreSession session, String type,
 			String subprojectCriteria) throws ClientException {
         String query = DocumentService.NXQL_SELECT_FROM + type + subprojectCriteria;
         DocumentModelList services = this.query(session, query, true, false);
         return services;
 	}
 	@Override
 	public DocumentModel getSoaNodeParent(DocumentModel soaNodeModel, String type) throws ClientException {
		Endpoint endpointAdapter = soaNodeModel.getAdapter(Endpoint.class);
 		try {
			SoaNodeId parentSoaNodeId = endpointAdapter.getParentOfType(type);
 			if (parentSoaNodeId != null) {
 				return this.findSoaNode(soaNodeModel.getCoreSession(), parentSoaNodeId);
 			}
 		} catch (Exception e) {
 			throw new ClientException(e);
 		}
 		return null;
 	}
 	@Override
 	public List<DocumentModel> getSoaNodeChildren(DocumentModel soaNodeModel, String type) throws ClientException {
 		if (soaNodeModel.isProxy()) {
 			soaNodeModel = findSoaNode(soaNodeModel.getCoreSession(), this.createSoaNodeId(soaNodeModel));
 		}
 		//return soaNodeModel.getCoreSession().getChildren(soaNodeModel.getRef(), type); // NO doesn't handle subtypes
 		return query(soaNodeModel.getCoreSession(), NXQL_SELECT_FROM + type + NXQL_WHERE
 				+ "ecm:parentId='" + soaNodeModel.getRef().toString() + "'", false, false);
 	}
 
 
 	@Override
 	public List<DocumentModel> getInformationServices(CoreSession session, String subprojectId) throws ClientException {
         return getInformationServicesInCriteria(session,
         		NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getInformationServicesInCriteria(CoreSession session, String subprojectCriteria) throws ClientException {
         return getByTypeInCriteria(session, InformationService.DOCTYPE, subprojectCriteria);
 	}
 	@Override
 	public List<DocumentModel> getServiceImplementations(CoreSession session, String subprojectId) throws ClientException {
         return getServiceImplementationsInCriteria(session,
         		NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getServiceImplementationsInCriteria(CoreSession session, String subprojectCriteria) throws ClientException {
         return getByTypeInCriteria(session, ServiceImplementation.DOCTYPE, subprojectCriteria);
 	}
 	@Override
 	public List<DocumentModel> getEndpoints(CoreSession session, String subprojectId) throws ClientException {
 		return getEndpointsInCriteria(session,
         		NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getEndpointsInCriteria(CoreSession session, String subprojectCriteria) throws ClientException {
 		return getByTypeInCriteria(session, Endpoint.DOCTYPE, subprojectCriteria);
 	}
 
 	@Override
 	public List<DocumentModel> getImplementationsOfService(DocumentModel serviceModel, String subprojectId) throws ClientException {
 		return getImplementationsOfServiceInCriteria(serviceModel,
 				NXQLQueryHelper.buildSubprojectCriteria(serviceModel.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getImplementationsOfServiceInCriteria(
 			DocumentModel serviceModel, String subprojectCriteria) throws ClientException {
 		CoreSession session = serviceModel.getCoreSession();
 		// mock impls :
         List<DocumentModel> impls = this.query(session, DocumentService.NXQL_SELECT_FROM
         		+ ServiceImplementation.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                 + ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE + "='" + serviceModel.getId() + "'", true, false);
         return impls;
 	}
 	@Override
 	public List<DocumentModel> getMockImplementationsOfService(DocumentModel serviceModel, String subprojectId) throws ClientException {
 		return getMockImplementationsOfServiceInCriteria(serviceModel,
 				NXQLQueryHelper.buildSubprojectCriteria(serviceModel.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getMockImplementationsOfServiceInCriteria(
 			DocumentModel serviceModel, String subprojectCriteria) throws ClientException {
 		CoreSession session = serviceModel.getCoreSession();
 		// NB. alt way would be getMockImplementationsOfService() and check is mock
         List<DocumentModel> mockImpls = this.query(session, DocumentService.NXQL_SELECT_FROM
         		+ ServiceImplementation.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                 + ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE + "='" + serviceModel.getId() + "'"
                 + DocumentService.NXQL_AND + ServiceImplementation.XPATH_ISMOCK + "='true'", true, false);
         return mockImpls;
 	}
 	@Override
 	public List<DocumentModel> getActualImplementationsOfService(DocumentModel serviceModel, String subprojectId) throws ClientException {
 		return getActualImplementationsOfServiceInCriteria(serviceModel,
 				NXQLQueryHelper.buildSubprojectCriteria(serviceModel.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getActualImplementationsOfServiceInCriteria(
 			DocumentModel serviceModel, String subprojectCriteria) throws ClientException {
 		CoreSession session = serviceModel.getCoreSession();
 		// NB. alt way would be getMockImplementationsOfService() and check is mock
         // WARNING IS NULL DOESN'T WORK IN RELEASE BUT DOES IN JUNIT
     	// old impl using proxy :
         //List<DocumentModel> actualImpls = new java.util.ArrayList<DocumentModel>();
         /* = session.query(DocumentService.NXQL_SELECT_FROM + ServiceImplementation.DOCTYPE
                 + DocumentService.NXQL_WHERE_NO_PROXY
                 + DocumentService.NXQL_AND + "ecm:uuid IN "
                 + getProxiedIdLiteralList(session,
                         session.query(DocumentService.NXQL_SELECT_FROM + ServiceImplementation.DOCTYPE
                 + DocumentService.NXQL_WHERE_PROXY + DocumentService.NXQL_AND
                 + DocumentService.NXQL_PATH_STARTSWITH + RepositoryHelper.getRepositoryPath(session, subprojectId) + InformationService.DOCTYPE + "'"
                 + DocumentService.NXQL_AND + "ecm:parentId='" + service.getId() + "'"
                 + DocumentService.NXQL_AND + ServiceImplementation.XPATH_ISMOCK + " IS NULL")));*/
         List<DocumentModel> actualImpls = this.query(session, DocumentService.NXQL_SELECT_FROM
         		+ ServiceImplementation.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                 + ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE + "='" + serviceModel.getId() + "'"
                 + DocumentService.NXQL_AND + ServiceImplementation.XPATH_ISMOCK + "<>'true'", true, false); // WARNING 'true' doesn't work in junit
         if (actualImpls.isEmpty()) {
         	// TODO HACK if empty, try using junit-only alternative query, in case we're in tests :
             actualImpls = this.query(session, DocumentService.NXQL_SELECT_FROM
             		+ ServiceImplementation.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                     + ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE + "='" + serviceModel.getId() + "'"
                     + DocumentService.NXQL_AND + ServiceImplementation.XPATH_ISMOCK + " IS NULL", true, false); // WARNING IS NULL works in junit only
             // alternate solution using "not mock impl"
             /*actualImpls = docService.query(session, DocumentService.NXQL_SELECT_FROM
                     + ServiceImplementation.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                     + DocumentService.NXQL_AND + "ecm:uuid NOT IN " + toLiteral(getIds(mockImpls)), true, false);*/ 
         }
         return actualImpls;
 	}
 	@Override
 	public List<DocumentModel> getDeliverableConsumptions(DocumentModel delModel) throws ClientException {
 		return getSoaNodeChildren(delModel, ServiceConsumption.DOCTYPE);
 	}
 	@Override
 	public List<DocumentModel> getDeliverablesConsumptions(List<DocumentModel> delModels) throws ClientException {
 		ArrayList<DocumentModel> consumptions = new ArrayList<DocumentModel>();
 		for (DocumentModel delModel : delModels) {
 			consumptions.addAll(this.getDeliverableConsumptions(delModel));
 		}
 		return consumptions;
 	}
 	@Override
 	public List<DocumentModel> getApplicationDeliverables(CoreSession session, String applicationName, String subprojectId) throws ClientException {
 		return getApplicationDeliverablesInCriteria(session, applicationName,
 				NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getApplicationDeliverablesInCriteria(
 			CoreSession session, String applicationName, String subprojectCriteria) throws ClientException {
         List<DocumentModel> applicationDeliverables = this.query(session, DocumentService.NXQL_SELECT_FROM
         		+ Deliverable.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
         		+ Deliverable.XPATH_APPLICATION + "='" + applicationName + "'", true, false);
         return applicationDeliverables;
 	}
 
 	// NOOO USELESS consumptions are at deliverable level
 	/*@Override
 	public List<DocumentModel> getInterfaceConsumptionsOfJavaImplementation(DocumentModel javaImplModel, String subprojectId) throws ClientException {
 		return getInterfaceConsumptionsOfJavaImplementationInCriteria(javaImplModel,
 				NXQLQueryHelper.buildSubprojectCriteria(javaImplModel.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getInterfaceConsumptionsOfJavaImplementationInCriteria(
 			DocumentModel javaImplModel, String subprojectCriteria) throws ClientException {
 		CoreSession session = javaImplModel.getCoreSession();
 		String implementedJavaInterface = (String) javaImplModel.getPropertyValue("javasi:implementedInterface");
 		String implementedJavaInterfaceLocation = (String) javaImplModel.getPropertyValue("javasi:implementedInterfaceLocation");
 		// NB. consumption resides at same place than implementation so can use location
         List<DocumentModel> javaConsumptions = this.query(session, DocumentService.NXQL_SELECT_FROM
         		+ "JavaServiceConsumption" + subprojectCriteria + DocumentService.NXQL_AND
                 + "javasc:consumedInterface" + "='" +implementedJavaInterface + "'" + DocumentService.NXQL_AND
                 + "javasc:consumedInterfaceLocation" + "='" + implementedJavaInterfaceLocation + "'", true, false);
         // TODO move to JavaServiceImplementation project by using an adapter, use JavaServiceImplementation & JavaServiceConsumption constants
         // TODO handle maven dependencies ??
         return javaConsumptions;
 	}*/
 
 	@Override
 	public DocumentModel getConsumerImplementationOfJavaConsumption(DocumentModel javaConsumptionModel, String subprojectId) throws ClientException {
 		return getConsumerImplementationOfJavaConsumptionInCriteria(javaConsumptionModel,
 				NXQLQueryHelper.buildSubprojectCriteria(javaConsumptionModel.getCoreSession(), subprojectId, true));
 	}
 	public DocumentModel getConsumerImplementationOfJavaConsumptionInCriteria(
 			DocumentModel javaConsumptionModel, String subprojectCriteria) throws ClientException {
 		CoreSession session = javaConsumptionModel.getCoreSession();
 		String consumerJavaClass = (String) javaConsumptionModel.getPropertyValue("javasc:consumerClass");
 		String consumedJavaInterfaceLocation = (String) javaConsumptionModel.getPropertyValue("javasc:consumedInterfaceLocation");
 		// NB. consumption resides at same place than implementation so can use location
         List<DocumentModel> res = this.query(session, DocumentService.NXQL_SELECT_FROM
         		+ "JavaServiceImplementation" + subprojectCriteria + DocumentService.NXQL_AND
                 + "javasi:implementationClass" + "='" +consumerJavaClass + "'" + DocumentService.NXQL_AND
                 + "javasi:implementedInterfaceLocation" + "='" + consumedJavaInterfaceLocation + "'", true, false);
         // TODO move to JavaServiceImplementation project by using an adapter, use JavaServiceImplementation & JavaServiceConsumption constants
         // TODO handle maven dependencies ??
         if (!res.isEmpty()) {
         	return res.get(0);
         }
         // TODO can there be more than one ???
         return null;
 	}
 
 	@Override
 	public List<DocumentModel> getConsumerImplementationsOfJavaConsumptions(List<DocumentModel> javaConsumptionModels, String subprojectId, boolean nonMock, boolean mock) throws ClientException {
 		return getConsumerImplementationsOfJavaConsumptionsInCriteria(javaConsumptionModels,
 				NXQLQueryHelper.buildSubprojectCriteria(javaConsumptionModels.get(0).getCoreSession(), subprojectId, true), nonMock, mock);
 	}
 	@Override
 	public List<DocumentModel> getConsumerImplementationsOfJavaConsumptionsInCriteria(
 			List<DocumentModel> javaConsumptionModels, String subprojectCriteria, boolean nonMock, boolean mock) throws ClientException {
 		ArrayList<DocumentModel> filteredImpls = new ArrayList<DocumentModel>(javaConsumptionModels.size());
 		for (DocumentModel javaConsumptionModel : javaConsumptionModels) {
 			DocumentModel impl = getConsumerImplementationOfJavaConsumptionInCriteria(javaConsumptionModel, subprojectCriteria);
 			if (impl != null) {
 				boolean isMock = "true".equals(impl.getPropertyValue(ServiceImplementation.XPATH_ISMOCK));
 	        	if (nonMock && !isMock || mock && isMock) {
 	        		filteredImpls.add(impl);
 	        	}
 			}
 		}
 		return filteredImpls;
 	}
 
 	@Override
 	public List<DocumentModel> getConsumedInterfaceImplementationsOfJavaConsumption(
 			DocumentModel javaConsumptionModel, String subprojectId, boolean nonMock, boolean mock) throws ClientException {
 		return getConsumedInterfaceImplementationsOfJavaConsumptionInCriteria(javaConsumptionModel,
 				NXQLQueryHelper.buildSubprojectCriteria(javaConsumptionModel.getCoreSession(), subprojectId, true), nonMock, mock);
 	}
 	@Override
 	public List<DocumentModel> getConsumedInterfaceImplementationsOfJavaConsumptionInCriteria(
 			DocumentModel javaConsumptionModel, String subprojectCriteria, boolean nonMock, boolean mock) throws ClientException {
 		String consumedJavaInterface = (String) javaConsumptionModel.getPropertyValue("javasc:consumedInterface");
         DocumentModelList impls = this.query(javaConsumptionModel.getCoreSession(), DocumentService.NXQL_SELECT_FROM
         		+ "JavaServiceImplementation" + subprojectCriteria + DocumentService.NXQL_AND
                 + "javasi:implementedInterface" + "='" + consumedJavaInterface + "'", true, false);
         // TODO move to JavaServiceImplementation project by using an adapter, use JavaServiceImplementation & JavaServiceConsumption constants
         // TODO use javasc:consumedInterfaceLocation ? handle maven dependencies ??
         return filterImpls(impls, nonMock, mock);
 	}
 	@Override
 	public DocumentModel getConsumedJavaInterfaceService(
 			DocumentModel javaConsumptionModel, String subprojectId) throws ClientException {
 		return getConsumedJavaInterfaceServiceInCriteria(javaConsumptionModel,
 				NXQLQueryHelper.buildSubprojectCriteria(javaConsumptionModel.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public DocumentModel getConsumedJavaInterfaceServiceInCriteria(
 			DocumentModel javaConsumptionModel, String subprojectCriteria) throws ClientException {
 		List<DocumentModel> consumedImpls = getConsumedInterfaceImplementationsOfJavaConsumptionInCriteria(javaConsumptionModel, subprojectCriteria, true, true);
 		// getting non mock impl IF POSSIBLE because it is a better guess (TODO how even better ??)
 		DocumentModel impl = null;
 		DocumentModel actualImpl = null;
 		for (DocumentModel consumedImpl : consumedImpls) {
 			if ("true".equals(consumedImpl.getPropertyValue(ServiceImplementation.XPATH_ISMOCK))) {
 				actualImpl = consumedImpl;
 				break;
 			}
 			impl = consumedImpl;
 		}
 		if (actualImpl != null) {
 			impl = actualImpl;
 		}
 		if (impl == null) {
 			// if none i.e. is a non-impl'd itf,
 			// TODO handle this case, ex.
 			// * by adding (consumed) iserv info on consumption
 			// * or by modeling java interface independently
 			// * or by adding java info to technical iserv NO DOESN'T SOLVE PB IF NO TECHNICAL ISERV
 		}
 		if (impl == null) {
 			return null;
 		}
 		return getParentInformationService(impl);
 	}
 	
 
 	@Override
 	public DocumentModel getServiceImplementationFromEndpoint(DocumentModel endpointModel) throws ClientException {
 		// TODO rather using implId on endpoint
         //List<DocumentModel> productionImplRes = docService.query(session, DocumentService.NXQL_SELECT_FROM
         //		+ ServiceImplementation.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND, false, true);
     	/*SoaMetamodelService soaMetamodelService;
 		try {
 			soaMetamodelService = Framework.getService(SoaMetamodelService.class);
 			SoaNode productionEndpointNode = endpointModel.getAdapter(SoaNode.class);
 	    	for (SoaNodeId productionEndpointParentNode : productionEndpointNode.getParentIds()) {
 	    		if (soaMetamodelService.isAssignable(productionEndpointParentNode.getType(), ServiceImplementation.DOCTYPE) ) {
 	    			return this.findSoaNode(endpointModel.getCoreSession(), productionEndpointParentNode);
 	    		}
 	    	}
 		} catch (Exception e) {
 			throw new RuntimeException("Can't get SoaMetamodelService", e);
 		}*/
 		return getSoaNodeParent(endpointModel, ServiceImplementation.DOCTYPE);
 	}
 
 	@Override
 	public List<DocumentModel> getEndpointsOfService(DocumentModel service, String subprojectId) throws ClientException {
 		return this.getEndpointsOfServiceInCriteria(service,
 				NXQLQueryHelper.buildSubprojectCriteria(service.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getEndpointsOfServiceInCriteria(DocumentModel service, String subprojectCriteria) throws ClientException {
 		return this.query(service.getCoreSession(), DocumentService.NXQL_SELECT_FROM
         		+ Endpoint.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                 + ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE + "='" + service.getId() + "'", true, false);
 	}
 
 	@Override
 	public List<DocumentModel> getEndpointsOfImplementation(DocumentModel serviceImpl, String subprojectId) throws ClientException {
 		return this.getEndpointsOfImplementationInCriteria(serviceImpl,
 				NXQLQueryHelper.buildSubprojectCriteria(serviceImpl.getCoreSession(), subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getEndpointsOfImplementationInCriteria(DocumentModel serviceImpl, String subprojectCriteria) throws ClientException {
             List<DocumentModel> endpoints = this.getSoaNodeChildren(serviceImpl, Endpoint.DOCTYPE);
             if(endpoints == null || endpoints.isEmpty()){
                return null;
             }
             return this.query(serviceImpl.getCoreSession(), DocumentService.NXQL_SELECT_FROM
         		+ Endpoint.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
                 + "ecm:uuid in " + NuxeoListUtils.toLiteral(NuxeoListUtils.getProxiedIds(serviceImpl.getCoreSession(), endpoints)), true, false);
 	}
 
     @Override
     public DocumentModel getEndpointOfImplementation(DocumentModel serviceImpl, String environment, String subprojectId) throws ClientException {
         return this.getEndpointOfImplementationInCriteria(serviceImpl, environment,
                 NXQLQueryHelper.buildSubprojectCriteria(serviceImpl.getCoreSession(), subprojectId, true));
     }
 
     @Override
     public DocumentModel getEndpointOfImplementationInCriteria(DocumentModel serviceImpl, String environment, String subprojectCriteria) throws ClientException {
         List<DocumentModel> endpoints = this.getSoaNodeChildren(serviceImpl, Endpoint.DOCTYPE);
         if(endpoints == null || endpoints.isEmpty()){
             return null;
         }
         StringBuilder query = new StringBuilder(DocumentService.NXQL_SELECT_FROM
             + Endpoint.DOCTYPE + subprojectCriteria);
         query.append(DocumentService.NXQL_AND + "ecm:uuid in " + NuxeoListUtils.toLiteral(NuxeoListUtils.getProxiedIds(serviceImpl.getCoreSession(), endpoints)));
         query.append(DocumentService.NXQL_AND + Endpoint.XPATH_ENDP_ENVIRONMENT + "='" + environment + "'");
         List<DocumentModel> endpointsOfImplementation = this.query(serviceImpl.getCoreSession(), query.toString(), true, false);
         if (!endpointsOfImplementation.isEmpty()) {
             return endpointsOfImplementation.get(0);
         }
         return null;
     }
 
     @Override
     public DocumentModel getEndpointOfService(DocumentModel service, String environment, String subprojectId) throws ClientException {
         return getEndpointOfServiceInCriteria(service, environment,
             NXQLQueryHelper.buildSubprojectCriteria(service.getCoreSession(), subprojectId, true));
     }
 
     @Override
     public DocumentModel getEndpointOfServiceInCriteria(DocumentModel service, String environment, String subprojectCriteria) throws ClientException {
         List<DocumentModel> endpoints = this.query(service.getCoreSession(), DocumentService.NXQL_SELECT_FROM
             + Endpoint.DOCTYPE + subprojectCriteria + DocumentService.NXQL_AND
             + ServiceImplementation.XPATH_PROVIDED_INFORMATION_SERVICE + "='" + service.getId() + "'"
             + DocumentService.NXQL_AND + Endpoint.XPATH_ENDP_ENVIRONMENT + "='" + environment + "'", true, false);
         if (!endpoints.isEmpty()) {
             return endpoints.get(0);
         }
         return null;
     }
 
 	@Override
 	public List<String> getEnvironments(CoreSession session, String subprojectId) throws ClientException {
 		return getEnvironmentsInCriteria(session,
 				NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<String> getEnvironmentsInCriteria(CoreSession session, String subprojectCriteria) throws ClientException {
         List<String> envs = new ArrayList<String>();
 
         // Get the environments
         DocumentModelList environments = this.query(session, "SELECT DISTINCT " + Endpoint.XPATH_ENDP_ENVIRONMENT
         		+ " FROM " + Endpoint.DOCTYPE + subprojectCriteria, true, false);
         // Fill the envs list
         for(DocumentModel model : environments){
             envs.add((String) model.getPropertyValue(Endpoint.XPATH_ENDP_ENVIRONMENT));
         }
         return envs;
 	}
 
 	@Override
 	public List<DocumentModel> getComponents(CoreSession session, String subprojectId) throws ClientException {
 		return getComponentsInCriteria(session,
 				NXQLQueryHelper.buildSubprojectCriteria(session, subprojectId, true));
 	}
 	@Override
 	public List<DocumentModel> getComponentsInCriteria(CoreSession session, String subprojectCriteria) throws ClientException {
 		return this.query(session, DocumentService.NXQL_SELECT_FROM
 				+ Component.DOCTYPE + subprojectCriteria, true, false);
 	}
 
 	@Override
 	public DocumentModel getParentInformationService(DocumentModel model) throws ClientException {
 		// is it itself an iserv ?
     	/*SoaMetamodelService soaMetamodelService;
 		try {
 			soaMetamodelService = Framework.getService(SoaMetamodelService.class);
 			if (soaMetamodelService.isAssignable(model.getType(), InformationService.DOCTYPE)) {
 				return model.getRef();
 			}
 		} catch (Exception e) {
 			throw new RuntimeException("Can't get SoaMetamodelService", e);
 		}*/
 		String iservId;
 		try {
 			// NB. this prop is defined in ServiceImplementationDataFacet on serviceimpl or endpoint
 			iservId = (String) model.getPropertyValue(ServiceImplementationDataFacet.XPATH_PROVIDED_INFORMATION_SERVICE);
 			if (iservId != null && iservId.length() != 0) {
 				return model.getCoreSession().getDocument(new IdRef(iservId));
 			}
 		} catch (PropertyException e) {
 			// not a serviceimpl or endpoint
 		}
     	return null;
 	}
 
 	@Override
 	public DocumentModel getParentServiceImplementation(DocumentModel model) throws ClientException {
 		// is it itself a serviceimpl ?
     	/*SoaMetamodelService soaMetamodelService;
 		try {
 			soaMetamodelService = Framework.getService(SoaMetamodelService.class);
 			if (soaMetamodelService.isAssignable(model.getType(), ServiceImplementation.DOCTYPE)) {
 				return model.getRef();
 			}
 		} catch (Exception e) {
 			throw new RuntimeException("Can't get SoaMetamodelService", e);
 		}*/
 		return getSoaNodeParent(model, ServiceImplementation.DOCTYPE);
 	}
 	
 
 	protected List<DocumentModel> filterImpls(List<DocumentModel> impls, boolean nonMock, boolean mock)
 			throws PropertyException, ClientException {
 		if (nonMock && mock) {
         	return impls;
         }
         ArrayList<DocumentModel> filteredImpls = new ArrayList<DocumentModel>(impls.size());
         for (DocumentModel impl : impls) {
         	boolean isMock = "true".equals(impl.getPropertyValue(ServiceImplementation.XPATH_ISMOCK));
         	if (nonMock && !isMock || mock && isMock) {
         		filteredImpls.add(impl);
         	}
         }
         return filteredImpls;
 	}
 
 }
