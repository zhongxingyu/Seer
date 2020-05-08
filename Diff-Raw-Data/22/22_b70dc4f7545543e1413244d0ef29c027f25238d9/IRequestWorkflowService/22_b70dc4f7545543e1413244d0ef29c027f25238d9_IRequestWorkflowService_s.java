 package fr.cg95.cvq.service.request;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.request.DataState;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestSeason;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.RequestType;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
import fr.cg95.cvq.exception.CvqValidationException;
 import fr.cg95.cvq.security.annotation.IsRequester;
 import fr.cg95.cvq.security.annotation.IsSubject;
 import fr.cg95.cvq.security.annotation.IsUser;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 import fr.cg95.cvq.service.request.annotation.IsRequestType;
 
 /**
  * @author bor@zenexity.fr
  */
 public interface IRequestWorkflowService {
 
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
 
     /**
      * Dispatcher method to update request data  state.
      */
     void updateRequestDataState(@IsRequest final Long id, final DataState rs)
         throws CvqException, CvqInvalidTransitionException, CvqObjectNotFoundException;
 
     /**
      * Dispatcher method to update request state.
      */
     void updateRequestState(@IsRequest final Long id, RequestState rs, String motive)
         throws CvqException, CvqInvalidTransitionException,
             CvqObjectNotFoundException;
 
     /**
      * Set a request in pending state after edition by an ecitizen
      */
     void rewindWorkflow(@IsRequest Request request, String note)
         throws CvqException, CvqInvalidTransitionException;
 
     /**
      * Get possible data state transitions from the given data state
      * (see {@link fr.cg95.cvq.business.request.DataState}).
      */
     DataState[] getPossibleTransitions(DataState ds);
 
     /**
      * Get possible state transitions from the given request state
      * (see {@link fr.cg95.cvq.business.request.RequestState}).
      *
      * @return an array of {@link fr.cg95.cvq.business.request.RequestState} objects
      */
     RequestState[] getPossibleTransitions(RequestState rs);
 
     /**
      * Return the list of states that precede the given state.
      */
     Set<RequestState> getStatesBefore(RequestState rs);
 
     /**
      * Get the list of states for which request edition in BO is authorized.
      */
     List<RequestState> getEditableStates();
 
     /**
      * Return whether the given request is editable in FO.
      * 
      * Currently, a request is editable if it is in pending or uncomplete state
      * and is not a account creation or modification request.
      */
     boolean isEditable(@IsRequest final Long requestId) throws CvqObjectNotFoundException;
     
     /**
      * Get the list of states for which instruction is done.
      */
     List<RequestState> getInstructionDoneStates();
 
     RequestState[] getStatesExcludedForRequestsCloning();
 
     RequestState[] getStatesExcludedForRunningRequests();
 
     /**
      * Create a new request from given data.
      * 
      * It is meant to be used <strong>only</strong> by requests who require an home folder, 
      * requester will be the currently logged in ecitizen, eventual subject id must be set
      * directly on request object.
      * 
      * A default implementation suitable for requests types that do not have any specific stuff 
      * to perform upon creation is provided. For others, the default implementation will have to
      * be overrided.
      */
     Long create(@IsRequest Request request, String note) throws CvqException;
 
     /**
      * Create a new request from given data.
      * 
      * It is meant to be used by requests issued outside an home folder. An home folder
      * containing at least the requester will be created. The subject is optional.
      */
     @Deprecated
     Long create(@IsRequest Request request, @IsRequester Adult requester, String note)
         throws CvqException;
 
     /**
      * The same as {@link #create(Request, Adult, Individual, String)} but with a provided
      * documents list.
      */
     @Deprecated
     Long create(@IsRequest Request request, @IsRequester Adult requester, List<Document> documents, String note)
         throws CvqException;
 
     /**
      * Get a set of home folder subjects that are authorized to be the subject of a request
      * of the type handled by current service.
      *
      * @return a map of home folder subjects or the home folder itself and authorized
      *                seasons if a request of the given type is issuable or null if not.
      */
     Map<Long, Set<RequestSeason>> getAuthorizedSubjects(RequestType requestType, 
         @IsUser Long homeFolderId)
         throws CvqException, CvqObjectNotFoundException;
 
     List<Long> getAuthorizedSubjects(@IsRequest final Request request)
         throws CvqException, CvqObjectNotFoundException;
 
     List<String> getMissingSteps(@IsRequest final Request request);
 
     /**
      * Get a clone of a request with the given request id
      * 
      * @return a new request without administrative and persistence information.
      */
     Request getRequestClone(@IsRequest Long requestId)
         throws CvqException;
 
     Request getRequestClone(@IsRequest final Long requestId, final Long requestSeasonId)
         throws CvqException;
 
     Map<Individual, Request> getRenewableRequests(@IsRequestType final String label)
         throws CvqException;
 
     Request getSkeletonRequest(final String requestTypeLabel) throws CvqException;
 
     Request getSkeletonRequest(final String requestTypeLabel, final Long requestSeasonId)
         throws CvqException;
 
     /**
     * Edit a request.
     */
    void rewindWorkflow(@IsRequest Request request, List<Document> documents, String note) throws CvqException;

    /**
      * Modify a request.
      */
     void modify(@IsRequest Request request) throws CvqException;
 
     void delete(@IsRequest Request request)
         throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Remove permanently a request.
      */
     void delete(@IsRequest Long id) throws CvqException, CvqObjectNotFoundException;
 
     /**
      * Perform checks wrt subject policies :
      * <ul>
      *   <li>Check that subject is coherent wrt the request's policy.</li>
      *   <li>Check that subject is allowed to issue a request of the given type</li>
      * </ul>
      *
      * @throws CvqModelException if there's a policy violation
      */
     void checkSubjectPolicy(@IsSubject final Long subjectId, @IsUser Long homeFolderId,
         final String policy, @IsRequestType final RequestType requestType)
         throws CvqException, CvqModelException;
 
     boolean validateSeason(@IsRequestType RequestType requestType, RequestSeason requestSeason)
         throws CvqException;
 
     void checkRequestTypePolicy(@IsRequestType RequestType requestType, HomeFolder homeFolder)
         throws CvqException;
 
     boolean isSupportMultiple(String requestLabel) throws CvqException;
 
     void validate(@IsRequest Request request, List<String> steps, boolean useAcceptance)
         throws ClassNotFoundException, IllegalAccessException,
             InvocationTargetException, NoSuchMethodException;
 }
