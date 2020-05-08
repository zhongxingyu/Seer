 package fr.cg95.cvq.external.impl;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.external.RequestExternalAction;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowDraftEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowPendingEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowInProgressEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowExtInProgressEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowCompleteEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowUncompleteEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowRectifiedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowRejectedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowCancelledEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowValidatedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowNotifiedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowClosedEvent;
 import fr.cg95.cvq.business.request.workflow.event.impl.WorkflowArchivedEvent;
 import fr.cg95.cvq.business.users.external.HomeFolderMapping;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.request.external.IRequestExternalActionService;
 import fr.cg95.cvq.service.request.external.IRequestExternalService;
 import fr.cg95.cvq.service.users.external.IExternalHomeFolderService;
 import fr.cg95.cvq.xml.common.RequestType;
 
 public abstract class ExternalProviderServiceAdapter implements IExternalProviderService {
 
     private static Logger logger = Logger.getLogger(ExternalProviderServiceAdapter.class);
 
     protected IRequestExternalService requestExternalService;
     protected IRequestExternalActionService requestExternalActionService;
     private IExternalHomeFolderService externalHomeFolderService;
 
     public void setRequestExternalService(IRequestExternalService requestExternalService) {
         this.requestExternalService = requestExternalService;
     }
 
     public void setRequestExternalActionService(
             IRequestExternalActionService requestExternalActionService) {
         this.requestExternalActionService = requestExternalActionService;
     }
 
     public void setExternalHomeFolderService(IExternalHomeFolderService externalHomeFolderService) {
         this.externalHomeFolderService = externalHomeFolderService;
     }
 
     @Override
     public void visit(final WorkflowDraftEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowPendingEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowInProgressEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowExtInProgressEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowCompleteEvent wfEvent) throws CvqException {
         checkExtReferentialAndSendRequest(wfEvent.getRequest());
     }
 
     @Override
     public void visit(final WorkflowUncompleteEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowRectifiedEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowCancelledEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowRejectedEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowValidatedEvent wfEvent) throws CvqException {
 // Example of a state change from an external provider
 //      wfEvent.setWorkflowPostAction(new IWorkflowPostAction() {
 //          @Override
 //          public String getExecutor() {
 //              return getLabel();
 //          }
 //
 //          @Override
 //          public void execute(IRequestWorkflowService requestWorkflowService) {
 //              try {
 //                  requestWorkflowService.updateRequestState(wfEvent.getRequest().getId(),
 //                          RequestState.CLOSED, null);
 //              } catch (CvqException e) {
 //                  logger.error(e.getMessage());
 //              }
 //          }
 //      });
     }
 
     @Override
     public void visit(final WorkflowNotifiedEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowClosedEvent wfEvent) throws CvqException {}
 
     @Override
     public void visit(final WorkflowArchivedEvent wfEvent) throws CvqException {}
 
     protected void checkExtReferentialAndSendRequest(Request request) throws CvqException {
         List<String> externalCheckErrors = checkExternalReferential(requestExternalService.getRequestType(request));
         if (!externalCheckErrors.isEmpty()) {
             logger.error(StringUtils.join(externalCheckErrors.iterator(), '\n'));
             // FIXME can't rollback to previous state here.
             // so let sendRequest() fail with an ERROR or NOT_SENT trace
             //
             // Conclusion: is checkExternalReferential worth it?
         }
         sendRequest(request);
     }
 
     @Override
     @Context(types = {ContextType.AGENT, ContextType.EXTERNAL_SERVICE}, privilege = ContextPrivilege.WRITE)
     public void sendRequest(Request request) throws CvqException {
         // First of all get the payload
         RequestType payload = requestExternalService.getRequestPayload(request, this);
 
         //add trace for sending the request
         RequestExternalAction rea = new RequestExternalAction(new Date(),
                 payload.getId(),
                 "capdemat",
                 null,
                 getLabel(),
                 RequestExternalAction.Status.SENT
             );
 
         String externalId = null;
         try {
             logger.debug("sendRequest() routing request to external service " + getLabel());
             externalId = sendRequest(payload);
         } catch (Exception e) {
             logger.error("sendRequest() error while sending request to " + getLabel());
             rea.setStatus(RequestExternalAction.Status.ERROR);
         }
 
         if (!handlesTraces())
             requestExternalActionService.addTrace(rea);
 
         if (externalId != null && !externalId.equals("")) {
             HomeFolderMapping mapping = externalHomeFolderService.getHomeFolderMapping(getLabel(),payload.getHomeFolder().getId());
             mapping.setExternalId(externalId);
             externalHomeFolderService.modifyHomeFolderMapping(mapping);
         }
     }
     
     public void sendMergedHomeFolder(XmlObject xmlObject) throws CvqException {
         logger.debug("sendMergedHomeFolder() sending " + xmlObject.xmlText());
     }
 
     @Override
     public String sendHomeFolderModification(XmlObject requestXml) throws CvqException {
         return sendRequest(requestXml);
     }
 }
