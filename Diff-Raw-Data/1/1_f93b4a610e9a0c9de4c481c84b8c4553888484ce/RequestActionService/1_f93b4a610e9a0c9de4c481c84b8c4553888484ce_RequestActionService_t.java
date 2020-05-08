 package fr.cg95.cvq.service.request.impl;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.dao.request.IRequestActionDAO;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.request.IRequestActionService;
 import fr.cg95.cvq.service.request.annotation.IsRequest;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author bor@zenexity.fr
  */
 public class RequestActionService implements IRequestActionService {
 
     private IRequestActionDAO requestActionDAO;
     private IRequestDAO requestDAO;
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public List<RequestAction> getActions(final Long requestId)
         throws CvqException {
 
         return requestActionDAO.listByRequest(requestId);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public RequestAction getAction(final Long id)
         throws CvqObjectNotFoundException {
         return
             (RequestAction)requestActionDAO.findById(RequestAction.class, id);
     }
 
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public RequestAction getLastWorkflowAction(@IsRequest final Long requestId)
         throws CvqException {
         
         return requestActionDAO.findLastAction(requestId,
             RequestActionType.STATE_CHANGE);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.READ)
     public RequestAction getActionByResultingState(final Long requestId,
         final RequestState requestState) throws CvqException {
         
         return requestActionDAO.findByRequestIdAndResultingState(requestId, requestState);
     }
 
     @Override
     public boolean hasAction(final Long requestId, final RequestActionType type)
         throws CvqException {
         return requestActionDAO.hasAction(requestId, type);
     }
 
     @Override
     @Context(type=ContextType.AGENT,privilege=ContextPrivilege.WRITE)
     public void addAction(final Long requestId, final RequestActionType type,
         final String message, final String note, final byte[] pdfData)
         throws CvqException {
 
         addActionTrace(type, message, note, new Date(), null, requestId, pdfData);
     }
 
     @Override
     @Context(type=ContextType.SUPER_ADMIN,privilege=ContextPrivilege.WRITE)
     public void addSystemAction(final Long requestId,
         final RequestActionType type)
         throws CvqException {
 
         addActionTrace(type, null, null, new Date(), null, requestId, null);
     }
 
     @Override
     public void addCreationAction(Long requestId, Date date, byte[] pdfData)
         throws CvqException {
         addActionTrace(RequestActionType.CREATION, null, null, date,
             RequestState.PENDING, requestId, pdfData);
     }
 
     @Override
     @Context(type=ContextType.ECITIZEN_AGENT,privilege=ContextPrivilege.WRITE)
     public void addWorfklowAction(final Long requestId, final String note, final Date date,
             final RequestState resultingState, final byte[] pdfData)
         throws CvqException {
 
         addActionTrace(RequestActionType.STATE_CHANGE, null, note, date,
             resultingState, requestId, pdfData);
     }
 
     private void addActionTrace(final RequestActionType type, final String message,
         final String note, final Date date, final RequestState resultingState,
         final Long requestId, final byte[] pdfData)
         throws CvqException {
 
         Request request = (Request) requestDAO.findById(Request.class, requestId);
 
         // retrieve user or agent id according to context
         Long userId = SecurityContext.getCurrentUserId();
         if (userId == null && request instanceof VoCardRequest) {
             VoCardRequest vocr = (VoCardRequest) request;
             // there can't be a logged in user at VO card request creation time
             userId = vocr.getRequesterId();
         }
 
         RequestAction requestAction = new RequestAction();
         requestAction.setAgentId(userId);
         requestAction.setType(type);
         requestAction.setMessage(message);
         requestAction.setNote(note);
         requestAction.setDate(date);
         requestAction.setResultingState(resultingState);
         requestAction.setFile(pdfData);
        requestAction.setRequest(request);
 
         if (request.getActions() == null) {
             Set<RequestAction> actionsSet = new HashSet<RequestAction>();
             actionsSet.add(requestAction);
             request.setActions(actionsSet);
         } else {
             request.getActions().add(requestAction);
         }
 
         requestDAO.update(request);
     }
 
     public void setRequestActionDAO(IRequestActionDAO requestActionDAO) {
         this.requestActionDAO = requestActionDAO;
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 }
