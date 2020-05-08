 package fr.cg95.cvq.service.payment.job;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.payment.Payment;
 import fr.cg95.cvq.dao.payment.IPaymentDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.util.mail.IMailService;
 
 /**
  * This job checks initialization's date of payment. Set commitAlert to true and
  * sends a mail alert if initilizationDate occured since more than 3 hours and
  * state is set to INITIALIZED.
  * 
  * @author Rafik Djedjig (rdj@zenexity.fr)
  */
 public class PaymentInitializationDateCheckerJob {
 
 	private Logger logger = Logger.getLogger(PaymentInitializationDateCheckerJob.class);
 
 	private ILocalAuthorityRegistry localAuthorityRegistry;
 
 	private IMailService mailService;
 
 	private IPaymentDAO paymentDAO;
 
 	/**
 	 * Entry point for the <b>PaymentInitializationDateCheckerJob</b>.
 	 * 
 	 * @see #checkInitializedPayment(String)
 	 */
 	public void launchJob() {
 		localAuthorityRegistry.browseAndCallback(this, "checkInitializedPayment", null);
 	}
 	
 	/**
 	 * Search payments older than 3 hours that are still in initialized state and
 	 * send an email alert if there are some.
 	 */
 	public void checkInitializedPayment() throws CvqException {
 		logger.debug("checkInitializedPayment() starting job for local authority " 
             + SecurityContext.getCurrentSite().getName());
 
 		List<Payment> paymentList = paymentDAO.searchNotCommited();
 		logger.debug("checkInitializedPayment() number of not commited payments : " 
 		        + paymentList.size());
 		
 		LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         
         Map<String, String> agentNotificationMap = 
             lacb.getMailAsMap("hasAgentNotification", "getAgentNotificationData", 
                     "NotCommitPaymentAlert");
         
         if (agentNotificationMap == null) {
             logger.info("checkInitializedPayment() " + SecurityContext.getCurrentSite().getName()
                     + " has not configured a notification for pending payments, returning");
             return;
         }
         
 		String mailSubject = agentNotificationMap.get("mailSubject");
 		String mailBodyFilename = agentNotificationMap.get("mailData");
         String mailBody = 
             localAuthorityRegistry.getBufferedLocalAuthorityResource(
                     Type.TXT, mailBodyFilename, false);
 		String mailSendTo = agentNotificationMap.get("mailSendTo");
 		
         String TAG_LOOP_OPEN = "<loop>";
 		String TAG_LOOP_CLOSE = "</loop>";
 		String mailBodyLoop = mailBody.substring(
 				mailBody.indexOf(TAG_LOOP_OPEN) + TAG_LOOP_OPEN.length(),
 				mailBody.indexOf(TAG_LOOP_CLOSE));
 		
 		StringBuffer mailBodyLoopBuffer = new StringBuffer();
 		
 		for (Payment payment : paymentList) {
 			String mailBodyLoopTp = mailBodyLoop;
 			
 			mailBodyLoopTp = mailBodyLoopTp.replace("${broker}",
 					payment.getBroker() != null ? payment.getBroker() : "" );
 			mailBodyLoopTp = mailBodyLoopTp.replace("${cvqReference}",
 					payment.getCvqReference() != null ? payment.getCvqReference() : "" );
 			mailBodyLoopTp = mailBodyLoopTp.replace("${paymentMode}",
 					payment.getPaymentMode() !=  null ? payment.getPaymentMode().toString() : "");
 			mailBodyLoopTp = mailBodyLoopTp.replace("${initializationDate}",
 					payment.getInitializationDate().toString());
 			
 			mailBodyLoopBuffer.append(mailBodyLoopTp);
 			
 			payment.setCommitAlert(true);
 			//paymentDAO.update(payment);
 		}
 		
 		mailBody = mailBody.replace(TAG_LOOP_OPEN, "");
 		mailBody = mailBody.replace(TAG_LOOP_CLOSE, "");
 		mailBody = mailBody.replace(mailBodyLoop, mailBodyLoopBuffer);
 		
 		mailService.send(null, mailSendTo, null, mailSubject, mailBody);	
 	}
 	
 	public void setLocalAuthorityRegistry(ILocalAuthorityRegistry localAuthorityRegistry) {
 		this.localAuthorityRegistry = localAuthorityRegistry;
 	}
 
 	public void setMailService(IMailService mailService) {
 		this.mailService = mailService;
 	}
 
 	public void setPaymentDAO(IPaymentDAO paymentDAO) {
 		this.paymentDAO = paymentDAO;
 	}
 
 }
