 package fr.cg95.cvq.service.request;
 
 import net.sf.oval.Validator;
 import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
 import net.sf.oval.context.OValContext;
 import net.sf.oval.exception.OValException;
 import fr.cg95.cvq.business.request.RequestData;
 import fr.cg95.cvq.exception.CvqException;
 
 public class SubjectIdCheck extends AbstractAnnotationCheck<LocalReferential> {
 
     private static final long serialVersionUID = 1L;
 
     private static IRequestServiceRegistry requestServiceRegistry;
 
     private static IRequestWorkflowService requestWorkflowService;
 
     @Override
     public boolean isSatisfied(Object validatedObject, Object valueToValidate, OValContext context,
         Validator validator) throws OValException {
         RequestData requestData = (RequestData)validatedObject;
         try {
             requestWorkflowService.checkSubjectPolicy(
                 (Long)valueToValidate,
                 requestData.getHomeFolderId(),
                 requestServiceRegistry.getRequestService(requestData.getRequestType().getLabel())
                     .getSubjectPolicy(),
                 requestData.getRequestType()
             );
             return true;
         } catch (CvqException e) {
             return false;
         }
     }
 
     public static void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         SubjectIdCheck.requestServiceRegistry = requestServiceRegistry;
     }
 
     public static void setRequestWorkflowService(IRequestWorkflowService requestWorkflowService) {
         SubjectIdCheck.requestWorkflowService = requestWorkflowService;
     }
 }
