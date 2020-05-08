 package fr.cg95.cvq.service.request;
 
 import java.math.BigInteger;
 
 import net.sf.oval.Validator;
 import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
 import net.sf.oval.context.OValContext;
 import net.sf.oval.exception.OValException;
 import fr.cg95.cvq.business.request.RequestData;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.exception.CvqException;
 
public class SubjectIdCheck extends AbstractAnnotationCheck<LocalReferential> {
 
     private static final long serialVersionUID = 1L;
 
     private static IRequestServiceRegistry requestServiceRegistry;
 
     private static IRequestWorkflowService requestWorkflowService;
 
     @Override
     public boolean isSatisfied(Object validatedObject, Object valueToValidate, OValContext context,
         Validator validator) throws OValException {
         RequestData requestData = (RequestData)validatedObject;
         if (requestData.getId() != null) {
             BigInteger subjectId = (BigInteger)HibernateUtil.getSession()
                 .createSQLQuery("select subject_id from request where id = :id")
                     .setLong("id", requestData.getId()).uniqueResult();
             if (subjectId != null) {
                 if (Long.valueOf(subjectId.longValue()).equals(valueToValidate)) {
                     return true;
                 } else if (!RequestState.DRAFT.equals(requestData.getState())) {
                     return false;
                 }
             }
         }
 
         if (requestServiceRegistry.getRequestService(requestData.getRequestType().getLabel()).getSubjectPolicy() != IRequestWorkflowService.SUBJECT_POLICY_NONE) {
             HibernateUtil.getSession().evict(requestData);
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
             } finally {
                 requestData = (RequestData) HibernateUtil.getSession().merge(requestData);
             }
         } else {
             return true;
         }
     }
 
     public static void setRequestServiceRegistry(IRequestServiceRegistry requestServiceRegistry) {
         SubjectIdCheck.requestServiceRegistry = requestServiceRegistry;
     }
 
     public static void setRequestWorkflowService(IRequestWorkflowService requestWorkflowService) {
         SubjectIdCheck.requestWorkflowService = requestWorkflowService;
     }
 }
