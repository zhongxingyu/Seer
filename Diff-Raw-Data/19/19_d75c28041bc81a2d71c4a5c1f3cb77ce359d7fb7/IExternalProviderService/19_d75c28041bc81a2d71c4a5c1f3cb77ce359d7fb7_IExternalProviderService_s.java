 package fr.cg95.cvq.external;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlbeans.XmlObject;
 
 import fr.cg95.cvq.business.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.payment.PurchaseItem;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.workflow.event.IWorkflowEventVisitor;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.service.payment.IPaymentService;
 
 /**
  * The interface all external services must implement.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public interface IExternalProviderService extends IWorkflowEventVisitor{
 
     /**
      * Send a request to an external service.
      * 
      * @param the xml of the request we want to send
      * @return business id coming from the plugged application
      */
     String sendRequest(final XmlObject requestXml)
         throws CvqException;
 
     void sendRequest(final Request request) throws CvqException;
 
     String sendHomeFolderModification(final XmlObject requestXml) throws CvqException;
 
     void sendMergedHomeFolder(XmlObject xmlObject) throws CvqException;
 
     /**
      * Get consumptions for a specific request.
      *
      * @param request the request we want associated consumptions of
      * @param dateFrom date down limit for the returned consumptions for this request
      * @param dateTo date up limit for the returned consumptions for this request
      *
      * @return a map of (date, label) pairs
      */
     Map<Date, String> getConsumptions(final Long key, final Date dateFrom, final Date dateTo)
         throws CvqException;
 
     /**
      * Get accounts grouped by home folder.
      *
      * @return Map of accounts for this home folder, keys are
      *               {@link IPaymentService#EXTERNAL_INVOICES}, 
      *               {@link IPaymentService#EXTERNAL_TICKETING_ACCOUNTS},
      *               {@link IPaymentService#EXTERNAL_DEPOSIT_ACCOUNTS}
      */
     Map<String, List<ExternalAccountItem> > getAccountsByHomeFolder(final Long homeFolderId, 
             final String externalHomeFolderId, final String externalId) 
         throws CvqException;
 
     /**
      * Load details of operations performed on given deposit account.
      */
     void loadDepositAccountDetails(ExternalDepositAccountItem edai)
         throws CvqException;
 
     /**
      * Load details of items paid along given external invoice.
      */
     void loadInvoiceDetails(ExternalInvoiceItem eii)
         throws CvqException;
     
     /**
      * Credit accounts of a given home folder with the provided purchase items.
      */
     void creditHomeFolderAccounts(final Collection<PurchaseItem> purchaseItems, 
             final String cvqReference, final String bankReference, final Long homeFolderId, 
             final String externalHomeFolderId, final String externalId, final Date validationDate)
         throws CvqException;
 
     /**
      * Initialization callback called for each declared local authority.
      * It's then up to the external service to check that it has all the configuration
      * parameters required to function properly
      */
     void checkConfiguration(final ExternalServiceBean externalServiceBean, final String localAuthorityName)
         throws CvqConfigurationException;
 
     /**
      * Utility method that can be used for integration tests with external services
      * @fixme is it really a good idea ?
      */
     String helloWorld()
         throws CvqException;
     
     /**
      * A label to uniquely identify an external service.
      * 
      * It is currently used to know when we have to transmit payments results to an external service.
      */
     String getLabel();
 
     /**
      * Whether this external service supports consumptions,
      * used to display the activities tab in frontoffice.
      */
     boolean supportsConsumptions();
 
     /**
      * Whether this external services handles its RequestExternalAction itself.
      * Answer false to let CapDemat handle the traces (it was the old behavior).
      */
     boolean handlesTraces();
 
     /**
      * Check the coherence of CapDemat's local referentials and external service's referentials.
      * @return a list of reasons for failed tests.
      */
     List<String> checkExternalReferential(final XmlObject requestXml);
 
     /**
      * Asks the external service for informations it knows about the request
      * (for example, its state) to display them to the ecitizen
      * @return The map of corresponding i18nKey - value
      */
     Map<String, Object> loadExternalInformations(XmlObject requestXml)
         throws CvqException;
 }
