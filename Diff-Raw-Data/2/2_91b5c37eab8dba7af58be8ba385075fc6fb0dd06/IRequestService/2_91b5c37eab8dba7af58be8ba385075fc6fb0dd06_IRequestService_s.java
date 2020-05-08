 package fr.cg95.cvq.service.request;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.security.annotation.IsHomeFolder;
 import fr.cg95.cvq.security.annotation.IsRequester;
 import fr.cg95.cvq.security.annotation.IsSubject;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.util.Critere;
 
 import org.apache.xmlbeans.XmlObject;
 import org.w3c.dom.Node;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * High level service interface to deal with requests.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public interface IRequestService {
 
     /** service name used by Spring's application context */
     String SERVICE_NAME = "requestService";
     
     /** 
      * Subject policy for request types that have a whole account (aka home folder) as subject.
      */
     String SUBJECT_POLICY_NONE = "SUBJECT_POLICY_NONE";
     /** 
      * Subject policy for request types that have an individual (adult or child) as subject.
      */
     String SUBJECT_POLICY_INDIVIDUAL = "SUBJECT_POLICY_INDIVIDUAL";
     /** 
      * Subject policy for request types that have an adult as subject.
      */
     String SUBJECT_POLICY_ADULT = "SUBJECT_POLICY_ADULT";
     /** 
      * Subject policy for request types that have a child as subject.
      */
     String SUBJECT_POLICY_CHILD = "SUBJECT_POLICY_CHILD";
     
     /** @deprecated */
     String VO_CARD_REGISTRATION_REQUEST = "VO Card";
     /** @deprecated */
    String HOME_FOLDER_MODIFICATION_REQUEST = "Home Folder Modification Request";
 
     //////////////////////////////////////////////////////////
     // CRUD related methods
     //////////////////////////////////////////////////////////
 
     /**
      * Prepares request draft.
      */
     void prepareDraft(@IsRequest Request request) throws CvqException;
     
     Long processDraft(@IsRequest Request request) throws CvqException;
     
     /**
      * Creates a draft request, bypass standard validation procedure. 
      * 
      * @param request current request
      * @return Newly created request identifier
      * @throws CvqException standard capdemat exception
      * @throws CvqObjectNotFoundException
      */
     Long createDraft(@IsRequest Request request) throws CvqException;
     
     /**
      * Modifies a draft of request
      * 
      * @param request draft
      * @throws CvqException standard capdemat exception
      */
     void modifyDraft(@IsRequest Request request) throws CvqException;
 
     /**
      * Finalizes a request draft
      * 
      * @param request draft to finalize
      * @throws CvqException standard exception
      */
     void finalizeDraft(@IsRequest Request request) throws CvqException;
     
     /**
      * Create a new request from given data.
      * 
      * It is meant to be used <strong>only</strong> by requests who require an home folder, 
      * requester will be the currently logged in ecitizen, eventual subject id will be set
      * directly on request object.
      * 
      * A default implementation suitable for requests types that do not have any specific stuff 
      * to perform upon creation is provided. For others, the default implementation will have to
      * be overrided.
      */
     Long create(@IsRequest Request request)
         throws CvqException, CvqObjectNotFoundException;
     
     Long create(@IsRequest Request request, List<Document> documents)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Create a new request from given data.
      * 
      * It is meant to be used by requests issued outside an home folder. An home folder
      * containing at least the requester will be created. The subject is optional (FIXME : is
      * it ever used ?)
      */
     Long create(@IsRequest Request request, @IsRequester Adult requester, 
             @IsSubject Individual subject)
         throws CvqException;
     
     Long create(@IsRequest Request request, @IsRequester Adult requester, 
             @IsSubject Individual subject, List<Document> documents)
         throws CvqException;
     
     /**
      * Get a clone of a request with the given label whose subject is either the given subject 
      * either the given home folder (depending on the subject policy supported by the associated
      * request type).
      * 
      * @param subjectId optional subject id
      * @param homeFolderId optional home folder id
      * @param requestLabel mandatory label of the request type
      * 
      * @return a new request without administrative and persistence information.
      * 
      * TODO REFACTORING : maybe return type will have to be migrated to a Request object
      */
     Node getRequestClone(@IsSubject final Long subjectId, @IsHomeFolder Long homeFolderId, 
             final String requestLabel) 
     	throws CvqException;
 
     /**
      * Edit a request
      */
     void rewindWorkflow(@IsRequest Request request)
         throws CvqException;
 
     /**
      * Modify a request.
      */
     void modify(@IsRequest Request request)
         throws CvqException;
 
     /**
      * Remove permanently a request.
      */
     void delete(@IsRequest final Long id)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get a constrained list of requests according to a set of criteria and requirements.
      *
      * @param criteriaSet a set of {@link Critere criteria} to be applied to the search
      * @param sort an ordering to apply to results. value is one of the SEARCH_* static
      *        string defined in this service (null to use default sort on requests ids)
      * @param dir the direction of the sort (asc or desc, asc by default)
      * @param recordsReturned the number of records to return (-1 to get all results)
      * @param startIndex the start index of the records to return
      */
     List<Request> get(Set<Critere> criteriaSet, final String sort, final String dir, 
             final int recordsReturned, final int startIndex)
         throws CvqException;
 
     /**
      * Get a count of requests matching the given criteria.
      */
     Long getCount(Set<Critere> criteriaSet) throws CvqException;
     
     /**
      * Get a request by id.
      */
     Request getById(@IsRequest final Long id)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get requests by requester's id.
      */
     List<Request> getByRequesterId(@IsRequester final Long requesterId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get requests by subject's id.
      */
     List<Request> getBySubjectId(@IsSubject final Long subjectId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests of the given type issued for the given subject.
      * @param retrieveArchived
      */
     List<Request> getBySubjectIdAndRequestLabel(@IsSubject final Long subjectId, 
             final String requestLabel, final boolean retrieveArchived)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests belonging to the given home folder.
      */
     List<Request> getByHomeFolderId(@IsHomeFolder final Long homeFolderId)
     		throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests of the given type belonging to the given home folder.
      */
     List<Request> getByHomeFolderIdAndRequestLabel(@IsHomeFolder final Long homeFolderId, 
             final String requestLabel)
             throws CvqException, CvqObjectNotFoundException;
     
     //////////////////////////////////////////////////////////
     // Notes and documents related methods
     //////////////////////////////////////////////////////////
     
     /**
      * Get notes related to a given request.
      * Optionnal type parameter, used to filter notes if it is not null.
      * Filters notes that must not be readable
      * (private notes which don't belong to the current context)
      *
      * @return a list of {@link fr.cg95.cvq.business.request.RequestNote} objects
      */
     List<RequestNote> getNotes(@IsRequest final Long requestId, final RequestNoteType type)
         throws CvqException;
 
     /**
      * Get the last readable note (of this type, if not null).
      */
     RequestNote getLastNote(@IsRequest final Long requestId, final RequestNoteType type)
         throws CvqException;
 
     /**
      * Get the last readable note written by an agent (of this type, if not null).
      */
     RequestNote getLastAgentNote(@IsRequest final Long requestId, final RequestNoteType type)
         throws CvqException;
 
     /**
      * Add a note to a request.
      *
      * @param requestId the request to which note has to be added
      * @param rnt the type of the note
      * @param note the body of the note itself
      */
     void addNote(@IsRequest final Long requestId, final RequestNoteType rnt, final String note)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Add a set of documents to a request.
      *
      * @param requestId the request to which documents have to be linked
      * @param documentsId a set of documents id that must have been created with
      *        the creation method provided by the
      *        {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
     void addDocuments(@IsRequest final Long requestId, final Set<Long> documentsId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Add a single document to a request.
      *
      * @param requestId the request to which the document has to linked
      * @param documentId a document that must have been created with the creation
      *  method provided by the {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
     void addDocument(@IsRequest final Long requestId, final Long documentId)
         throws CvqException, CvqObjectNotFoundException;
     
     /**
      * Add a single document to a request.
      * Enable adding document to not persisted request
      *
      * @param request to which the document has to linked
      * @param documentId a document that must have been created with the creation
      *  method provided by the {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
     void addDocument(@IsRequest Request request, final Long documentId)
         throws CvqException, CvqObjectNotFoundException;
     
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
     Set<RequestDocument> getAssociatedDocuments(@IsRequest final Long requestId) throws CvqException;
     
     Set<RequestDocument> getAssociatedDocuments(@IsRequest Request request) throws CvqException;
 
     /**
      * Get the generated certificate for the given request at the given step.
      */
     byte[] getCertificate(@IsRequest final Long requestId, final RequestState requestState)
         throws CvqException;
 
     //////////////////////////////////////////////////////////
     // General request information related methods
     //////////////////////////////////////////////////////////
 
     /**
      * Return the season associated to the given request, null if none.
      */
     RequestSeason getRequestAssociatedSeason(@IsRequest Long requestId) throws CvqException;
 
     //////////////////////////////////////////////////////////
     // Payment & activities related methods
     //////////////////////////////////////////////////////////
 
     /**
      * Called by payment service on the reception of a payment operation status.
      *
      * If payment is successful, performs the following :
      * <ul>
      *  <li>Notify service associated to request type</li>
      *  <li>Notify external services</li>
      * </ul>
      */
     void notifyPaymentResult(final Payment payment) throws CvqException;
     
     /**
      * Return whether given request type is associated with an external service.
      * 
      * The result is delegated to the {@link IExternalService external service}.
      */
     boolean hasMatchingExternalService(final String requestTypeLabel)
         throws CvqException;
 
     /**
      * Get consumption events for a given request.
      */
     Map<Date, String> getConsumptionsByRequest(@IsRequest final Long requestId, 
             final Date dateFrom, final Date dateTo)
         throws CvqException;
 
     String getConsumptionsField()
         throws CvqException;
     
     /**
      * Get a set of home folder subjects that are authorized to be the subject of a request
      * of the type handled by current service.
      *
      * @return a map of home folder subjects or the home folder itself and authorized
      *                seasons if a request of the given type is issuable or null if not.
      */
     Map<Long, Set<RequestSeason>> getAuthorizedSubjects(@IsHomeFolder final Long homeFolderId)
         throws CvqException, CvqObjectNotFoundException;
     
     //////////////////////////////////////////////////////////////////
     // Properties set by configuration in Spring's application context
     //////////////////////////////////////////////////////////////////
 
     /**
      * Return a string used to uniquely identify the service.
      */
     String getLabel();
 
     /**
      * Return name of the XSL-FO file used to render request certificate.
      */
     String getXslFoFilename();
     
     /**
      * Return the file name of local referential data specific to this request type (or null if
      * none defined).
      */
     String getLocalReferentialFilename();
 
     /**
      * Return the file name of place reservation referential data specific to this request type 
      * (or null if none defined).
      */
     String getPlaceReservationFilename();
 
     /**
      * Return the file name of external referential data specific to this request type (or null
      * if not defined)
      */
     String getExternalReferentialFilename();
     
     /**
      * Whether the request type handled by current service authorizes creation operation without 
      * having already an account.
      */
     boolean supportUnregisteredCreation();
     
     /**
      * Return the subject policy supported by the current service, one of
      * {@link #SUBJECT_POLICY_NONE}, {@link #SUBJECT_POLICY_INDIVIDUAL},
      * {@link #SUBJECT_POLICY_ADULT} or {@link #SUBJECT_POLICY_CHILD}.
      * 
      * If not overrided in the service configuration, defaults to
      * {@link #SUBJECT_POLICY_NONE}.
      *   
      */
     String getSubjectPolicy();
     
     /**
      * Whether the request type handled by current service is of registration
      * kind.
      */
     boolean isOfRegistrationKind();
 
     // ////////////////////////////////////////////////////////
     // Methods to be overridden by implementing services
     // ////////////////////////////////////////////////////////
 
     void onRequestValidated(Request request) throws CvqException;
 
     void onRequestCancelled(Request request) throws CvqException;
     
     void onRequestRejected(Request request) throws CvqException;
 
     /**
      * Chain of responsabilities pattern.
      */
     boolean accept(Request request);
 
     /**
      * Return a fresh new request object of the type managed by the implementing class.
      * This method must be implemented by classes implementing this interface.
      */
     Request getSkeletonRequest() throws CvqException;
     
     void onPaymentValidated(Request request, String paymentReference) throws CvqException;
     
     void onPaymentRefused(Request request) throws CvqException;
 
     void onPaymentCancelled(Request request) throws CvqException;
 
     /**
      * Realize specific task, just after the call 'sendRequest' method in
      * 'ExternalService'.
      */
     void onExternalServiceSendRequest(Request request, String sendRequestResult) 
         throws CvqException;
     
     /**
      * Entry point for business conditions treatments
      * @param triggers - A map where key=control.name and value=control.value, for all controls triggering the same condition 
      */
     boolean isConditionFilled (Map<String, String> triggers);
 
     /**
      * Insert home folder, subject and requester XML data into the request XML data
      * to generate a full XML to send to external services
      */
     XmlObject fillRequestXml(Request request)
         throws CvqException;
 
     /**
      * Get all the requests that are sendable to this external service
      */
     List<Request> getSendableRequests(String externalServiceLabel);
 }
