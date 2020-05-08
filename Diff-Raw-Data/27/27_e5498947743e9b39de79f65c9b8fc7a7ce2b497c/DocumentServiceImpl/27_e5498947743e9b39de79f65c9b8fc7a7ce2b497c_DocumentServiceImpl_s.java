 package org.easysoa.registry;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.easysoa.registry.types.Endpoint;
 import org.easysoa.registry.types.InformationService;
 import org.easysoa.registry.types.IntelligentSystem;
 import org.easysoa.registry.types.Repository;
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.utils.DocumentModelHelper;
 import org.easysoa.registry.utils.RepositoryHelper;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
 import org.nuxeo.ecm.core.query.sql.NXQL;
 import org.nuxeo.ecm.core.schema.DocumentType;
 import org.nuxeo.ecm.core.schema.types.Type;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 
 public class DocumentServiceImpl implements DocumentService {
 
 	public void checkSoaName(DocumentModel soaNodeDoc) throws ClientException {
         if (soaNodeDoc.getPropertyValue(SoaNode.XPATH_SOANAME) == null) {
         	throw new ClientException("Null soaname for " + soaNodeDoc.getPathAsString()
         			+ " - did happen when : doc was created when it already existed "
         			+ "because it was queried for using erroneously safeName(), "
         			+ "doc was created before setting soaname");
         }
 	}
 	
     public DocumentModel createDocument(CoreSession documentManager, String doctype, String name, String parentPath, String title) throws ClientException {
         // TODO if doctype belongs to SoaNode subtypes, throw new Exception("createDocument() doesn't work for SoaNode types, rather use create()")
         DocumentModel documentModel = documentManager.createDocumentModel(doctype);
         documentModel.setPathInfo(parentPath, safeName(name));
         documentModel.setProperty("dublincore", "title", title);
         documentModel = documentManager.createDocument(documentModel);
         return documentModel;
     }
     
     public DocumentModel create(CoreSession documentManager, SoaNodeId identifier, String parentPath) throws ClientException {
         String doctype = identifier.getType();
  
         if (isSoaNode(documentManager, doctype)) {
         	// XXX Redundant with RepositoryManagementListener?
             boolean createProxy = false;
             if (!parentPath.equals(getSourceFolderPath(doctype))) {
                 createProxy = true;
             }
             
             // Create or fetch source document
             DocumentModel documentModel = find(documentManager, identifier);
         	if (documentModel == null) {
                 documentModel = newSoaNodeDocument(documentManager, identifier);
                 documentModel = documentManager.createDocument(documentModel);
         	}
             
             // Create proxy if needed (but make sure the parent is the instance of the repository,
             // otherwise the child proxy will only be visible in the context of the parent proxy)
             if (createProxy) {
                 PathRef parentRef = new PathRef(parentPath);
                 DocumentModel parentModel = documentManager.getDocument(parentRef);
                 if (parentModel != null) {
                     if (parentModel.isProxy()) {
                         parentModel = find(documentManager, createSoaNodeId(parentModel));
                     }
                 }
                 else {
                     throw new ClientException("Invalid parent path: " + parentPath);
                 }
                return documentManager.createProxy(documentModel.getRef(), parentModel.getRef());
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
      * TODO NO triggers documentCreate event but properties have not yet been set !
      */
     public DocumentModel create(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
         DocumentModel documentModel = null;
         
         if (isSoaNode(documentManager, identifier.getType())) {
         	documentModel = find(documentManager, identifier);
         	if (documentModel == null) {
                 documentModel = newSoaNodeDocument(documentManager, identifier);
                 documentModel = documentManager.createDocument(documentModel);
         	}
         }
         
         return documentModel;
     }
 
     /* (non-Javadoc)
      * @see org.easysoa.registry.DocumentService#newSoaNodeDocument(org.nuxeo.ecm.core.api.CoreSession, org.easysoa.registry.SoaNodeId)
      */
     public DocumentModel newSoaNodeDocument(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
         String doctype = identifier.getType(), name = identifier.getName();
         ensureSourceFolderExists(documentManager, doctype);
         DocumentModel documentModel = documentManager.createDocumentModel(doctype);
         documentModel.setPathInfo(getSourceFolderPath(doctype), safeName(name));
         documentModel.setPropertyValue(SoaNode.XPATH_TITLE, name);
         documentModel.setPropertyValue(SoaNode.XPATH_SOANAME, name);
         return documentModel;
     }
 
 	/**
      * Transforms the parameter into a string suitable for use as Nuxeo's name.
      */
     private String safeName(String name) {
 		return name.replace('/', '|');
 	}
 
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
         DocumentModel sourceDocument = find(documentManager, soaNodeId);
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
 
     public DocumentModel findDocument(CoreSession documentManager, String type, String name) throws ClientException {
         String query = NXQLQueryBuilder.getQuery("SELECT * FROM ? WHERE " + NXQL.ECM_NAME + " = '?'",
                 new Object[] { type, safeName(name) },
                 false, true);
         DocumentModelList results = query(documentManager, query, true, false);
         return results.size() > 0 ? results.get(0) : null;
     }
     
     public DocumentModel find(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
         String query = NXQLQueryBuilder.getQuery("SELECT * FROM ? WHERE " + SoaNode.XPATH_SOANAME + " = '?'",
                 new Object[] { identifier.getType(), safeName(identifier.getName()) },
                 false, true);
         DocumentModelList results = query(documentManager, query, true, false);
         return results.size() > 0 ? results.get(0) : null;
     }
 
     @Override
     public DocumentModelList findProxies(CoreSession documentManager, SoaNodeId identifier)
             throws ClientException {
         String query = NXQLQueryBuilder.getQuery("SELECT * FROM ? WHERE " + SoaNode.XPATH_SOANAME + " = '?'",
                 new Object[] { identifier.getType(), safeName(identifier.getName()) },
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
             parentModel = find(documentManager, createSoaNodeId(parentModel));
         }
         
         // Find proxy among children
         DocumentModelList childrenModels = documentManager.getChildren(parentModel.getRef());
         for (DocumentModel childModel : childrenModels) {
             if (createSoaNodeId(childModel).equals(identifier)) {
                 return childModel;
             }
         }
         return null;
     }
 
     public DocumentModelList findAllInstances(CoreSession documentManager, SoaNodeId identifier) throws ClientException {
     	if (identifier == null) {
     		return new DocumentModelListImpl();
     	}
         String query = NXQLQueryBuilder.getQuery("SELECT * FROM ? WHERE " + SoaNode.XPATH_SOANAME + " = '?'",
                 new Object[] { identifier.getType(), identifier.getName() }, // TODO MDU and not safeName(identifier.getName()) !?
                 false, true);
         return query(documentManager, query, false, false);
     }
     
     public DocumentModelList query(CoreSession documentManager, String query,
     		boolean filterProxies, boolean filterNonProxies) throws ClientException {
     	String filteredQuery = query +
         		((filterProxies) ? PROXIES_QUERY_FILTER : "") + 
                 ((filterNonProxies) ? NON_PROXIES_QUERY_FILTER : "") + 
         		DELETED_DOCUMENTS_QUERY_FILTER +
         		VERSIONS_QUERY_FILTER;
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
         DocumentModel workingModel;
         if (documentModel.isProxy()) {
             workingModel = documentManager.getWorkingCopy(documentModel.getRef());
         }
         else {
             workingModel = documentModel;
         }
     
         // Build proxies list
         DocumentModelList modelInstances = documentManager.getProxies(documentModel.getRef(), null);
         modelInstances.add(workingModel);
         
         // Fetch parents
         DocumentModelList parents = new DocumentModelListImpl();
         for (DocumentModel modelInstance : modelInstances) {
             DocumentModel parentDocument = documentManager.getParentDocument(modelInstance.getRef());
             if (!parents.contains(parentDocument)) {
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
 
     public String getSourceFolderPath(String doctype) {
         return Repository.REPOSITORY_PATH + '/' + doctype; 
     }
     
     public void ensureSourceFolderExists(CoreSession documentManager, String doctype) throws ClientException {
         RepositoryHelper.getRepositoryInstance(documentManager);
         getSourceFolder(documentManager, doctype);
     }
     
     public DocumentModelList getChildren(CoreSession session, DocumentRef parentRef, String type) throws ClientException {
         parentRef = session.getWorkingCopy(parentRef).getRef(); // making sure it's not a proxy
         return session.getChildren(parentRef, type);
     }
 
     private DocumentModel getSourceFolder(CoreSession documentManager, String doctype) throws ClientException {
         PathRef sourceFolderRef = new PathRef(getSourceFolderPath(doctype));
         if (documentManager.exists(sourceFolderRef)) {
             return documentManager.getDocument(sourceFolderRef);
         }
         else {
             DocumentModel sourceFolderModel = documentManager.createDocumentModel(Repository.REPOSITORY_PATH,
                     doctype, IntelligentSystem.DOCTYPE);
             sourceFolderModel.setProperty("dublincore", "title", DocumentModelHelper.getDocumentTypeLabel(doctype) + "s");
             sourceFolderModel = documentManager.createDocument(sourceFolderModel);
             return sourceFolderModel;
         }
     }
     
     public SoaNodeId createSoaNodeId(DocumentModel model) throws ClientException {
         try {
             return new SoaNodeId(model.getType(), (String) model.getPropertyValue(SoaNode.XPATH_SOANAME));
         }
         catch (PropertyNotFoundException e) {
             throw new ClientException("Invalid document type (" + model.getType() + "), an SoaNode is expected");
         }
     }
 
     @Override
     public List<SoaNodeId> createSoaNodeIds(DocumentModel... models) throws PropertyException, ClientException {
         List<SoaNodeId> soaNodeIds = new ArrayList<SoaNodeId>();
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
 		if (doctypeToTest == null || expectedDoctype == null) {
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
 		return false;
 	}
 
 	@Override
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
 
 
 }
