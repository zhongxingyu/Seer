 package fr.cg95.cvq.service.request.impl;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.request.IRequestDocumentService;
 
 public class RequestDocumentService implements IRequestDocumentService {
 
     private IRequestDAO requestDAO;
     
     private IDocumentService documentService;
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocuments(final Long requestId, final Set<Long> documentsId)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         for (Long documentId : documentsId) {
             RequestDocument requestDocument = new RequestDocument();
             requestDocument.setDocumentId(documentId);
             if (request.getDocuments() == null) {
                 Set<RequestDocument> documentSet = new HashSet<RequestDocument>();
                 documentSet.add(requestDocument);
                 request.setDocuments(documentSet);
             } else {
                 request.getDocuments().add(requestDocument);
             }
         }
 
         updateLastModificationInformation(request);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocument(final Long requestId, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
 
         Request request = getById(requestId);
 
         RequestDocument requestDocument = new RequestDocument();
         requestDocument.setDocumentId(documentId);
         if (request.getDocuments() == null) {
             Set<RequestDocument> documents = new HashSet<RequestDocument>();
             documents.add(requestDocument);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(requestDocument);
         }
 
         updateLastModificationInformation(request);
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocument(Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
         RequestDocument requestDocument = new RequestDocument();
         requestDocument.setDocumentId(documentId);
         if (request.getId() != null)
             request = getById(request.getId());
         if (request.getDocuments() == null) {
             Set<RequestDocument> documents = new HashSet<RequestDocument>();
             documents.add(requestDocument);
             request.setDocuments(documents);
         } else {
             request.getDocuments().add(requestDocument);
         }
 
         updateLastModificationInformation(request);
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addDocuments(Request request, List<Document> documents)
         throws CvqException {
         
         if (documents == null)
             return;
 
         Set<Long> documentIds = new HashSet<Long>();
         for (Document document : documents) {
             document.setId(null);
             document.setHomeFolderId(request.getHomeFolderId());
             documentIds.add(documentService.create(document));
         }
         for (Long documentId : documentIds)
             addDocument(request, documentId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void removeDocument(Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException {
         if (request.getId() != null)
             request = getById(request.getId());
         Iterator<RequestDocument> it = request.getDocuments().iterator();
         while (it.hasNext()) {
             RequestDocument rd = it.next();
             if (rd.getDocumentId().equals(documentId)){
                 it.remove();
             }
         }
 
         updateLastModificationInformation(request);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestDocument> getAssociatedDocuments(final Long requestId)
         throws CvqException {
 
         Request request = getById(requestId);
         return request.getDocuments();
     }
     
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<RequestDocument> getAssociatedDocuments(Request request)
         throws CvqException {
         if (request.getId() != null)
             request = getById(request.getId());
         return request.getDocuments();
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public Set<Document> getAssociatedDocumentsByType(final Long requestId, final Long documentTypeId)
         throws CvqException {
         Request request = getById(requestId);
         Set<Document> result = new HashSet<Document>();
         for (RequestDocument requestDocument : request.getDocuments()) {
             Document document = documentService.getById(requestDocument.getDocumentId());
             if (document.getDocumentType().getId().equals(documentTypeId))
                 result.add(document);
         }
         return result;
     }
 
     private void updateLastModificationInformation(Request request) {
 
         // update request's last modification date
         request.setLastModificationDate(new Date());
         request.setLastInterveningUserId(SecurityContext.getCurrentUserId());
         
         requestDAO.update(request);
     }
 
     private Request getById(final Long requestId) throws CvqObjectNotFoundException {
         return (Request) requestDAO.findById(Request.class, requestId);
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }    
 }
