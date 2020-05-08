 package fr.cg95.cvq.service.request;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 
 import org.w3c.dom.Node;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestForm;
 import fr.cg95.cvq.business.request.RequestFormType;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.payment.Payment;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * High level service interface to deal with requests.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public interface IRequestService {
 
     /** service name used by Spring's application context */
     String SERVICE_NAME = "requestService";
     
     String REQUEST_CREATION_NOTIFICATION = "REQUEST_CREATION_NOTIFICATION";
     String REQUEST_ORANGE_ALERT_NOTIFICATION = "REQUEST_ORANGE_ALERT_NOTIFICATION";
     String REQUEST_RED_ALERT_NOTIFICATION = "REQUEST_RED_ALERT_NOTIFICATION";
     String CREATION_ACTION = "CREATION_ACTION";
     String STATE_CHANGE_ACTION = "STATE_CHANGE_ACTION";
     
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
     String VO_CARD_REGISTRATION_REQUEST = "VO Card Request";
     /** @deprecated */
     String HOME_FOLDER_MODIFICATION_REQUEST = "Home Folder Modification Request";
 
     //////////////////////////////////////////////////////////
     // CRUD related methods
     //////////////////////////////////////////////////////////
 
     Long create(final Request request, final Long requesterId)
         throws CvqException, CvqObjectNotFoundException;
 
     Long create(final Node node) throws CvqException;
 
     /**
      * Return a fresh new request object of the type managed by the implementing class.
      * This method must be implemented by classes implementing this interface.
      */
     Request getSkeletonRequest() throws CvqException;
     
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
      */
     Node getRequestClone(final Long subjectId, Long homeFolderId, final String requestLabel) 
     	throws CvqException;
     
     /**
      * Modify a request.
      */
     void modify(final Request request)
         throws CvqException;
 
     /**
      * Remove permanently a request.
      */
     void delete(final Long id)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get a list of requests according to a set of criteria.
      *
      * @param criteriaSet a {@link Set} of criteria to be applied to search request.
      * @param orderedBy an ordering to apply to results. value is one of the SEARCH_* static
      *        string defined in this service.
      * @param onlyIds whether we only want a {@link Set} of request ids or the requests.
      */
     Set get(Set criteriaSet, final String orderedBy, final boolean onlyIds)
         throws CvqException;
 
     /**
      * Get a constrained list of requests according to a set of criteria and requirements.
      *
      * @param criteriaSet a {@link Set} of criteria to be applied to search request
      * @param sort an ordering to apply to results. value is one of the SEARCH_* static
      *        string defined in this service
      * @param dir the direction of the sort (asc or desc)
      * @param recordsReturned the number of records to return
      * @param startIndex the start index of the records to return
      */
     Set extendedGet(Set<Critere> criteriaSet, final String sort, final String dir, 
             final int recordsReturned, final int startIndex)
         throws CvqException;
     
     /**
      * Get a request by id.
      */
     Request getById(final Long id)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get a set of requests by id.
      */
     Set<Request> getByIds(final Long[] ids)
         throws CvqException, CvqObjectNotFoundException;
     
     /**
      * Get requests by requester's id.
      */
     Set getByRequesterId(final Long requesterId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get requests by subject's id.
      */
     Set getBySubjectId(final Long subjectId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests of the given type issued for the given subject.
      * @param retrieveArchived
      */
     Set getBySubjectIdAndRequestLabel(final Long subjectId, final String requestLabel, boolean retrieveArchived)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests belonging to the given home folder.
      */
     Set<Request> getByHomeFolderId(final Long homeFolderId)
     		throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get all requests of the given type belonging to the given home folder.
      */
     Set getByHomeFolderIdAndRequestLabel(final Long homeFolderId, final String requestLabel)
             throws CvqException, CvqObjectNotFoundException;
     
     /**
      * Get a request by last intervening agent's id.
      */
     Set getByLastInterveningAgentId(final Long agentId)
         throws CvqException, CvqObjectNotFoundException;
 
     //////////////////////////////////////////////////////////
     // Notes, actions and documents related methods
     //////////////////////////////////////////////////////////
     /**
      * Get notes related to a given request.
      *
      * @return a set of {@link fr.cg95.cvq.business.users.RequestNote} objects
      */
     Set getNotes(final Long id)
         throws CvqException;
 
     /**
      * Add a note to a request.
      *
      * @param requestId the request to which note has to be added
      * @param rnt the type of the note
      * @param note the body of the note itself
      */
     void addNote(final Long requestId, final RequestNoteType rnt, final String note)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Add a set of documents to a request.
      *
      * @param requestId the request to which documents have to be linked
      * @param documentsId a set of documents id that must have been created with
      *        the creation method provided by the
      *        {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
     void addDocuments(final Long requestId, final Set documentsId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Add a single document to a request.
      *
      * @param requestId the request to which the document has to linked
      * @param documentId a document that must have been created with the creation
      *  method provided by the {@link fr.cg95.cvq.service.document.IDocumentService} service
      */
     void addDocument(final Long requestId, final Long documentId)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Get actions related to a given request.
      *
      * @return a set of {@link fr.cg95.cvq.business.request.RequestAction} objects
      */
     Set getActions(final Long id)
         throws CvqException;
 
     /**
      * Add an (non-workflow) action trace for the given request.
      */
     public void addAction(final Request request, final String label, final String note)
         throws CvqException;
     
     /**
      * Get documents associated to a request.
      *
      * As they are not automatically loaded from DB, they have to be explicitely
      * asked for
      *
      * @param id request id
      * @return a set of {@link Document} objects associated to the request with the given id
      */
     Set getAssociatedDocuments(final Long id)
         throws CvqException;
 
     /**
      * Get the generated certificate for the given request at the given step.
      *
      * @param id request id
      * @param requestState the resulting state for which we want to retrieve the certificate
      */
     byte[] getCertificate(final Long id, final RequestState requestState)
         throws CvqException;
 
     //////////////////////////////////////////////////////////
     // General request information related methods
     //////////////////////////////////////////////////////////
 
     /**
      * Get a list of all existing requests types.
      *
      * @return a set of {@link RequestType} objects
      */
     Set<RequestType> getAllRequestTypes()
         throws CvqException;
 
     RequestType getRequestTypeById(final Long id)
         throws CvqException;
 
     /**
      * Modify a request type properties.
      */
     void modifyRequestType(RequestType requestType)
         throws CvqException;
 
     /**
      * Modify requirements associated to a request type.
      *
      * @param requestType the request type to modify
      * @param requirements a set of {@link fr.cg95.cvq.business.request.Requirement} to associate 
      *                     to the given request type
      */
     void modifyRequestTypeRequirements(RequestType requestType, Set requirements)
         throws CvqException;
 
     /**
      * Get a list of all requests types handled by a c.
      *
      * @return a set of {@link RequestType} objects
      */
     Set getRequestsTypesByCategory(final Long categoryId)
         throws CvqException;
 
     /**
      * Get a request type by its label.
      *
      * @param requestLabel the label of the request, as given by {@link #getLabel()}
      */
     RequestType getRequestTypeByLabel(final String requestLabel)
         throws CvqException;
 
     /**
      * Get a list of documents types allowed for a given request type.
      *
      * @return a set of {@link fr.cg95.cvq.business.document.DocumentType} objects
      */
     Set getAllowedDocuments(final RequestType requestType)
         throws CvqException;
 
     //////////////////////////////////////////////////////////
     // Seasons related methods
     //////////////////////////////////////////////////////////
 
     /**
      * @deprecated
      * @see isRegistrationOpen (final Long requestTypeId)
      */
     boolean hasOpenSeasons(final Long requestTypeId) throws CvqException;
     
     
     boolean isRegistrationOpen (final Long requestTypeId) throws CvqException;
     
     /**
      * Associate a new season to requestType
      * 
      * @throws CvqException
      * <br><br>
      * Expected business error code are :
      * <dl>
      *   <dt>request.season.not_supported</dt>
      *     <dd>Request Type don't support season management</dd>
      *   <dt>request.season.seasons_registration_overlapped</dt>
      *     <dd>Season registration dates overlap an other season registration dates</dd>
      *   <dt>request.season.seasons_effect_overlapped</dt>
      *     <dd>Season effect dates overlap an other season effect dates</dd>
      *   <dt>request.season.registration_start_required</dt>
      *     <dd>-</dd>
      *   <dt>request.season.registration_end_required</dt>
      *     <dd>-</dd>
      *   <dt>request.season.effect_start_required</dt>
      *     <dd>-</dd>
      *   <dt>request.season.effect_end_required</dt>
      *     <dd>-</dd>
      *   <dt>request.season.registration_start_after_registration_end</dt>
      *     <dd>-</dd>
      *   <dt>request.season.effect_start_after_effect_end</dt>
      *     <dd>-</dd>
      *   <dt>request.season.registration_start_after_effect_start</dt>
      *     <dd>-</dd>
      *   <dt>request.season.registration_end_after_effect_end</dt>
      *     <dd>-</dd>
      *   <dt>registration_start_before_now</dt>
      *     <dd>Season registration start is define in past</dd>
      *   <dt>request.season.already_used_label</dt>
      *     <dd>-</dd>
      * </dl>
      */
     void createRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason)
         throws CvqException;
 
     /**
      * @deprecated
      * @see modifyRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason)
      */
     void modifyRequestTypeSeasons(RequestType requestType, Set<RequestSeason> seasons)
         throws CvqException;
     /**
      * Modify a season associate to requestType
      * 
      * @param requestSeason - Don't forget to set season's uuid. It's use to identify season.
      * @throws CvqException
      * <br><br>
      * Refer to createRequestTypeSeasons  business error code.
      * <br>
      * Specific business error code:
      * <dl>
      *   <dt>request.season.effect_ended</dt>
      *     <dd>Season effect end has been occured (only in modify season context)</dd>
      *   <dt>request.season.registration_started</dt>
      *     <dd>Season effect end has been occured (only in modify season context)</dd>
      * </dl>
      */
     void modifyRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason)
         throws CvqException;
     
     void removeRequestTypeSeasons(RequestType requestType, RequestSeason requestSeason)
         throws CvqException;
     
     /**
      * Return season associated to request (with id='requestId').
      * <br/> If no season is associated return null. 
      *
      */
     RequestSeason getRequestAssociatedSeason(Long requestId) throws CvqException;
     
     //////////////////////////////////////////////////////////
     // RequestForm related Methods
     //////////////////////////////////////////////////////////
     
     /**
      * Add a new requestForm to the requestType identify requestTypeId parameter
      * <ul>
      *  <li>add model association</li>
      *  <li>store the requestForm in '&lt;asset_dir&gt;/&lt;local_authority&gt;/xsl'</li>
      *  <li>
      *   rename the requestForm file as follow :
      *   &lt;request_type_label&gt;_&lt;request_form_type&gt;_&lt;request_form_short_label&gt;.extension
      *  </li>
      *  </ul>
      */
     void addRequestTypeForm(final Long requestTypeId, RequestFormType requestFormType, 
             String label, String shortLabel, String filename, byte[] data)
         throws CvqException;
     
     /**
      * Modify a requestForm
      * <ul>
      *  <li>Modify request_form_label if newLabel parameter not null</li>
      *  <li>
      *      Modify request_form_short_label if newShortLabel parameter not null, 
      *      and rename requestForm resource on file system.
      *  </li>
      *  <li>Modify requestForm file if newData parameter not null</li>
      * </ul>
      */
     void modifyRequestTypeForm (Long requestTypeId, Long requestFormId, 
             String newLabel, String newShortLabel, String newFilename, byte[] newData) throws CvqException;
 
     /**
      * Remove a requestForm
      * <ul>
      *  <li>delete model association</li>
      *  <li>remove the requestForm file stored in '&lt;asset_dir&gt;/&lt;local_authority&gt;/xsl'</li>
      * </ul>
      */
     void removeRequestTypeForm(final Long requestTypeId, final Long requestFormId)
         throws CvqException;
     /**
      * Get requestForms by request_type and by request_form_type
      */
     Set<RequestForm> getRequestTypeForms(Long requestTypeId, 
             RequestFormType requestFormType) throws CvqException;
     
     
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
      * Return whether given request type can provide consumptions summary.
      */
     boolean hasConsumptions(final String requestLabel)
         throws CvqException;
 
     /**
      * Get consumption events for a given request.
      */
     Map getConsumptionsByRequest(final Long requestId, final Date dateFrom, final Date dateTo)
         throws CvqException;
 
     String getConsumptionsField()
         throws CvqException;
 
     //////////////////////////////////////////////////////////
     // Workflow related methods
     //////////////////////////////////////////////////////////
 
     /**
      * Dispatcher method to update request data  state
      */
     void updateRequestDataState(final Long id, final DataState rs)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
     
     /**
      * Get possible data state transitions from the given data state
      * (see {@link fr.cg95.cvq.business.request.DataState}).
      */
     DataState[] getPossibleTransitions(DataState ds);
     
     /**
      * Get a set of home folder subjects that are authorized to be the subject of a request
      * of the type handled by current service.
      *
      * @return a set of home folder subjects or the home folder itself if a request of the
      * given type is issuable or null if not.
      */
     Map<Object, Set<RequestSeason>> getAuthorizedSubjects(final Long homeFolderId)
         throws CvqException, CvqObjectNotFoundException;
     
     /**
      * Dispatcher method to update request state
      */
     void updateRequestState(final Long id, RequestState rs, String motive)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
     
     /**
      * Set the request state to complete.
      * (see {@link fr.cg95.cvq.business.request.RequestState})
      */
     void complete(final Long id)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     void complete(final Request request)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
     
     /**
      * Ask for more information about a request
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void specify(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     void specify(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     /**
      * Validate a request
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void validate(final Long id)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     void validate(final Request request)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     /**
      * Notify a user its request has been validated
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void notify(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     void notify(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException;
 
     /**
      * Cancel a request
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void cancel(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     void cancel(final Request request)
         throws CvqException, CvqInvalidTransitionException;
     
     /**
      * Activate a request.
      * 
      * @see RequestState#ACTIVE
      */
     void activate(final Long id) 
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
     
     /**
      * Activate a request.
      * 
      * @see RequestState#ACTIVE
      */
     void activate(final Request request) 
         throws CvqException, CvqInvalidTransitionException;
     
     /**
      * Expire a request.
      * 
      * @see RequestState#EXPIRED
      */
     void expire(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     /**
      * Expire a request.
      * 
      * @see RequestState#EXPIRED
      */
     void expire(final Request request)
         throws CvqException, CvqInvalidTransitionException;
     
     /**
      * Reject the validation of a request
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void reject(final Long id, final String motive)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     void reject(final Request request, final String motive)
         throws CvqException, CvqInvalidTransitionException;
     
     /**
      * Close a request
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void close(final Long id)
         throws CvqException, CvqInvalidTransitionException,
                CvqObjectNotFoundException;
 
     void close(final Request request)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     /**
      * Archive a request
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      */
     void archive(final Long id)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     void archive(final Request request)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     /**
      * Archive all requests belonging to the given {@link HomeFolder home folder}.
      */
     void archiveHomeFolderRequests(final HomeFolder homeFolder)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     /**
      * Get possible state transitions from the given request state
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      *
      * @return an array of {@link fr.cg95.cvq.business.request.RequestState}
      *         objects
      */
     RequestState[] getPossibleTransitions(RequestState rs);
 
     /**
      * Return the list of states that precede the given state.
      */
     public Set<RequestState> getStatesBefore(RequestState rs);
 
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
 
     /**
      * Chain of responsabilities pattern.
      */
     boolean accept(Request request);
 
     void onPaymentValidated(Request request, String paymentReference) throws CvqException;
     
     void onPaymentRefused(Request request) throws CvqException;
 
     void onPaymentCancelled(Request request) throws CvqException;
 
     /**
      * Realize specific task, just after the call 'sendRequest' method in
      * 'ExternalService'.
      */
     void onExternalServiceSendRequest(Request request, String sendRequestResult) throws CvqException;
     
 }
