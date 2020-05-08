 package fr.cg95.cvq.service.request;
 
 import java.util.List;
 import java.util.Set;
 
 import fr.capwebct.capdemat.GetDocumentListResponseDocument;
 import fr.capwebct.capdemat.GetDocumentResponseDocument;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.PermissionException;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 
 /**
  * Manage the associations between a request and documents.
  * 
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public interface IRequestDocumentService {
 
     /**
      * Add a set of documents to a request.
      *
      * @param requestId the request to which documents have to be linked
      * @param documentsId a set of documents id that must have been created with
      *        the creation method provided by the
      *        {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
     void addDocuments(@IsRequest final Long requestId, final Set<Long> documentsId);
 
     /**
      * Add a single document to a request.
      *
      * @param requestId the request to which the document has to linked
      * @param documentId a document that must have been created with the creation
      *  method provided by the {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
    void addDocument(@IsRequest final Long requestId, final Long documentId);

     /**
      * Add a single existing document to a not yet persisted request.
      *
      * @param request to which the document has to linked
      * @param documentId a document that must have been created with the creation
      *  method provided by the {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
    void addDocument(@IsRequest Request request, final Long documentId);

     void addDocuments(@IsRequest Request request, List<Document> documents) throws CvqException;
 
     /**
      * Remove link betwenn a document and a request.
      *
      * @param request to which the document is linked
      * @param documentId 
      */
     void removeDocument(@IsRequest Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get references of documents associated to a request.
      *
      * As they are not automatically loaded from DB, they have to be explicitely
      * asked for.
      */
     Set<RequestDocument> getAssociatedDocuments(@IsRequest final Long requestId);
     
     GetDocumentListResponseDocument getAssociatedFullDocuments(@IsRequest final Long requestId)
         throws CvqException, PermissionException;
     
     GetDocumentResponseDocument getAssociatedDocument(@IsRequest final Long requestId, final Long documentId,
             final boolean mergeDocument) throws CvqException, PermissionException;
 
     List<Document> getProvidedNotAssociatedDocumentsByType(@IsRequest final Long requestId, final Long documentTypeId)
         throws CvqException;
 
     /**
      * Get associated documents of the given type.
      */
     Set<Document> getAssociatedDocumentsByType(@IsRequest final Long requestId, final Long documentTypeId)
         throws CvqException;
 
     Boolean hasAllDocuments(Request request)
         throws CvqException;
 }
