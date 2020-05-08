 package fr.cg95.cvq.external.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.rmi.RemoteException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.jaxen.JaxenException;
 import org.jaxen.XPath;
 import org.jaxen.dom.DOMXPath;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import fr.capwebct.modules.payment.schema.fam.FamilyAccountsResponseDocument;
 import fr.cg95.cvq.business.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItemDetail;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItemDetail;
 import fr.cg95.cvq.business.payment.ExternalTicketingContractItem;
 import fr.cg95.cvq.business.payment.PurchaseItem;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqRemoteException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.ExternalServiceUtils;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.payment.IPaymentService;
 import fr.cg95.cvq.service.request.IRequestSearchService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 
 /**
  * A fake implementation of the {@link IExternalProviderService external provider service interface}
  * that is meant to be used for demonstration purposes only.
  *
  * TODO : this service has to be migrated to use the XML schemas APIs 
  * instead of custom XML mapping.
  * 
  * @author bor@zenexity.fr
  */
 public class FakeExternalService implements IExternalProviderService {
 
     private static Logger logger = Logger.getLogger(FakeExternalService.class);
 
     private IRequestSearchService requestSearchService;
     private IHomeFolderService homeFolderService;
     private IPaymentService paymentService;
     
     private String label;
     private String authorizingRequestLabel;
     private String testDataDirectory;
     private String xmlDirectory;
     private String consumptionsFile;
     private String accountsFile;
     private String depositAccountDetailsFile;
     private String invoiceDetailsFile;
 
     public final String sendRequest(XmlObject requestXml) throws CvqException {
         logger.debug("sendRequest() sending request data " + requestXml.xmlText());
         return null;
     }
 
     public final void creditHomeFolderAccounts(final Collection<PurchaseItem> purchaseItems, 
             final String cvqReference, final String bankReference, final Long homeFolderId, 
             String externalHomeFolderId, String externalId, final Date validationDate)
         throws CvqException {
 
         logger.debug("creditHomeFolderAccounts() Gonna credit home folder " + homeFolderId);
         logger.debug("creditHomeFolderAccounts() for transaction " + cvqReference + " / "
                 + bankReference);
     }
 
     public final Map<Date, String> getConsumptions(final Long key, 
             final Date dateFrom, final Date dateTo) throws CvqException {
 
         logger.debug("getConsumptionsByRequest() request " + key);
         logger.debug("getConsumptionsByRequest() from " + dateFrom);
         logger.debug("getConsumptionsByRequest() to " + dateTo);
 
         try {
             Map<Date, String> results = new LinkedHashMap<Date, String>();
             String pathToFile = testDataDirectory + xmlDirectory + "/" + consumptionsFile;
             File file = new File(pathToFile);
             Document consumptionXMLDocument = DocumentBuilderFactory.newInstance()
                     .newDocumentBuilder().parse(file);
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
             XPath xpath = new DOMXPath("//events/event");
             List eventNodes = xpath.selectNodes(consumptionXMLDocument);
             for (int i = 0; i < eventNodes.size(); i++) {
                 Node event = (Node) eventNodes.get(i);
                 NamedNodeMap attributes = event.getAttributes();
                 Node dateNode = attributes.getNamedItem("event-date");
                 Node labelNode = attributes.getNamedItem("event-label3");
                 Date eventDate = simpleDateFormat.parse(dateNode.getNodeValue());
                 logger.debug("adding label " + labelNode.getNodeValue() 
                         + " with date " + eventDate);
                 results.put(eventDate, labelNode.getNodeValue());
             }
 
             return results;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public final Map<String, List<ExternalAccountItem> > getAccountsByHomeFolder(final Long homeFolderId, 
             String externalHomeFolderId, String externalId) throws CvqException {
         
         String pathToFile = testDataDirectory + xmlDirectory + "/" + accountsFile;
         File file = new File(pathToFile);
         logger.debug("getAccountsByHomeFolder() gonna parse file " + file);
         try {
             FamilyAccountsResponseDocument familyDocument = FamilyAccountsResponseDocument.Factory.parse(file);
             Map<String, List<ExternalAccountItem> > externalAccounts = 
                 ExternalServiceUtils.parseFamilyDocument(familyDocument, getLabel());
             
             String supportedBroker = paymentService.getAllBrokers().keySet().iterator().next();
             for (ExternalAccountItem eai :
                 externalAccounts.get(IPaymentService.EXTERNAL_TICKETING_ACCOUNTS)) {
                 eai.setSupportedBroker(supportedBroker);
                 // manually insert subject id as the currently logged in user
                 ExternalTicketingContractItem etci = (ExternalTicketingContractItem) eai;
                 etci.setSubjectId(SecurityContext.getCurrentUserId());
             }
             for (ExternalAccountItem eai :
                 externalAccounts.get(IPaymentService.EXTERNAL_DEPOSIT_ACCOUNTS)) {
                 eai.setSupportedBroker(supportedBroker);
             }
             for (ExternalAccountItem eai :
                 externalAccounts.get(IPaymentService.EXTERNAL_INVOICES)) {
                 eai.setSupportedBroker(supportedBroker);
             }
             return externalAccounts;
         } catch (XmlException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
 
     public Map<Individual, Map<String, String>> getIndividualAccountsInformation(Long homeFolderId, String externalHomeFolderId, String externalId) 
         throws CvqException {
         
         Map<Individual, Map<String, String> > results =
             new HashMap<Individual, Map<String, String> >();
 
         try {
             List<Request> requests = 
                 requestSearchService.getByHomeFolderIdAndRequestLabel(homeFolderId,
                     authorizingRequestLabel, false);
             if (requests == null || requests.size() == 0)
                 return null;
             // pick the first request
             Request request = requests.get(0);
             logger.debug("getIndividualAccountsInformation() using request : " + request.getId());
             
             String pathToFile = testDataDirectory + xmlDirectory + "/" + accountsFile;
             File file = new File(pathToFile);
             logger.debug("getIndividualAccountsInformation() got file " + file);
             Document accountsXMLDocument = 
                 DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
 
             //  contracts accounts
             XPath xpath = new DOMXPath("//child");
             int i = 0;
             List childElements = (List) xpath.evaluate(accountsXMLDocument);
             for (Iterator iter = childElements.iterator(); iter.hasNext();) {
                 Node node = (Node) iter.next();
                 String childCsn = node.getAttributes().getNamedItem("child-csn").getNodeValue();
 
                 List<Individual> individuals = 
                     homeFolderService.getIndividuals(request.getHomeFolderId());
                 Individual subject = null;
                 for (Individual individual : individuals) {
                     if (individual.getId().equals(request.getSubjectId())) {
                         subject = individual;
                         break;
                     }
                 }
                 HomeFolder homeFolder = 
                     homeFolderService.getById(request.getHomeFolderId());
                 if (subject == null) {
                     int k = i % homeFolder.getIndividuals().size();
                     subject = (Individual) homeFolder.getIndividuals().toArray()[k]; 
                 }
 
                 Map<String, String> individualData = new HashMap<String, String>();
                 individualData.put("child-csn", childCsn);
                 results.put(subject, individualData);
                 
                 i++;
             }
             
         } catch (JaxenException jaxe) {
             throw new CvqException("Failed to parse received data : " + jaxe.getMessage());
         } catch (SAXException sae) {
             throw new CvqException("Failed to parse received data : " + sae.getMessage());
         } catch (IOException ioe) {
             throw new CvqRemoteException("Failed to read received data : " + ioe.getMessage());
         } catch (ParserConfigurationException pce) {
             throw new CvqException("Failed to parse received data : " + pce.getMessage());
         }
 
         return results;
     }
 
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
         logger.debug("loadDepositAccountDetails()");
 
         try {
 
             String pathToFile = testDataDirectory + xmlDirectory + "/" + depositAccountDetailsFile;
             File file = new File(pathToFile);
             logger.debug("loadDepositAccountDetails() got file " + file);
             Document depositAccountDetailsDocument = 
                 DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
 
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
             XPath xpath = new DOMXPath("//accountDetails[@account-id='" 
                     + edai.getExternalItemId() + "']/accountDetail");
             List accountDetailNodes = xpath.selectNodes(depositAccountDetailsDocument);
             for (int i = 0; i < accountDetailNodes.size(); i ++) {
                 Node accountDetailNode = (Node) accountDetailNodes.get(i);
                 ExternalDepositAccountItemDetail edaiDetail =
                     new ExternalDepositAccountItemDetail();
                edaiDetail.setExternalDepositAccountItem(edai);
                 NodeList accountDetailChildren = accountDetailNode.getChildNodes();
                 for (int j = 0; j < accountDetailChildren.getLength(); j++) {
                     Node childNode = accountDetailChildren.item(j);
                     if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                         if (childNode.getNodeName().equals("date")) {
                             edaiDetail.setDate(simpleDateFormat.parse(childNode.getTextContent()));
                         } else if (childNode.getNodeName().equals("holder-name")){
                             edaiDetail.setHolderName(childNode.getTextContent());
                         } else if (childNode.getNodeName().equals("holder-surname")) {
                             edaiDetail.setHolderSurname(childNode.getTextContent());
                         } else if (childNode.getNodeName().equals("value")) {
                             edaiDetail.setValue(Integer.valueOf(childNode.getTextContent()));
                         } else if (childNode.getNodeName().equals("payment-type")) {
                             edaiDetail.setPaymentType(childNode.getTextContent());
                         } else if (childNode.getNodeName().equals("payment-id")) {
                             edaiDetail.setPaymentId(childNode.getTextContent());
                         }
                     }
                 }
                 edai.addAccountDetail(edaiDetail);
             }
         } catch (RemoteException re) {
             throw new CvqRemoteException("Failed to connect to Horanet Service" + re.getMessage());
         } catch (SAXException saxe) {
             throw new CvqRemoteException("Failed to parse The schoolCanteen Registration to send to Horanet" + saxe.getMessage());
         } catch (IOException ioe) {
             throw new CvqRemoteException("Failed to parse The schoolCanteen Registration to send to Horanet" + ioe.getMessage());
         } catch (JaxenException jaxe) {
             throw new CvqRemoteException("Failed to parse consumption XML file" + jaxe.getMessage());
         } catch (ParseException pe) {
             throw new CvqRemoteException("Failed to parse Dates" + pe.getMessage());
         } catch (ParserConfigurationException pce) {
             throw new CvqException("Erreur de configuration XML dans l'envoi de doc Horanet: " + pce.getMessage());
         }
     }
 
 
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
         logger.debug("loadInvoiceDetails()");
 
         try {
 
             String pathToFile = testDataDirectory + xmlDirectory + "/" + invoiceDetailsFile;
             File file = new File(pathToFile);
             logger.debug("loadInvoiceDetails() got file " + file);
             Document invoiceDetailsDocument = 
                 DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
 
             XPath xpath = new DOMXPath("//invoiceDetails[@invoice-id='" 
                     + eii.getExternalItemId() + "']/invoiceDetail");
             List invoiceDetailNodes = xpath.selectNodes(invoiceDetailsDocument);
             for (int i = 0; i < invoiceDetailNodes.size(); i ++) {
                 Node invoiceDetailNode = (Node) invoiceDetailNodes.get(i);
                 ExternalInvoiceItemDetail eiiDetail =
                     new ExternalInvoiceItemDetail();
                 NodeList invoiceDetailChildren = invoiceDetailNode.getChildNodes();
                 for (int j = 0; j < invoiceDetailChildren.getLength(); j++) {
                     Node childNode = invoiceDetailChildren.item(j);
                     if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                         if (childNode.getNodeName().equals("label")) {
                             eiiDetail.setLabel(childNode.getTextContent());
                         } else if (childNode.getNodeName().equals("child-name")){
                             eiiDetail.setSubjectName(childNode.getTextContent());
                         } else if (childNode.getNodeName().equals("child-surname")) {
                             eiiDetail.setSubjectSurname(childNode.getTextContent());
                         } else if (childNode.getNodeName().equals("value")) {
                             eiiDetail.setValue(Integer.valueOf(childNode.getTextContent()));
                         } else if (childNode.getNodeName().equals("quantity")) {
                             eiiDetail.setQuantity(new BigDecimal(childNode.getTextContent()));
                         } else if (childNode.getNodeName().equals("unit-price")) {
                             eiiDetail.setUnitPrice(Integer.valueOf(childNode.getTextContent()));
                         }
                     }
                 }
                 eii.addInvoiceDetail(eiiDetail);
             }
         } catch (RemoteException re) {
             throw new CvqRemoteException("Failed to connect to external service" 
                     + re.getMessage());
         } catch (SAXException saxe) {
             throw new CvqRemoteException("Failed to parse data from external service" 
                     + saxe.getMessage());
         } catch (IOException ioe) {
             throw new CvqRemoteException("Failed to parse data from external service" 
                     + ioe.getMessage());
         } catch (JaxenException jaxe) {
             throw new CvqRemoteException("Failed to parse data from external service" 
                     + jaxe.getMessage());
         } catch (ParserConfigurationException pce) {
             throw new CvqException("Erreur de configuration XML dans l'envoi de doc Horanet: " + pce.getMessage());
         }
     }
 
     public final void checkConfiguration(final ExternalServiceBean externalServiceBean, String localAuthorityName)
             throws CvqConfigurationException {
     }
 
     public final String helloWorld() throws CvqException {
         return "Hello World";
     }
 
     public final void setXmlDirectory(final String xmlDirectory) {
         this.xmlDirectory = xmlDirectory;
     }
 
     public final void setConsumptionsFile(final String consumptionsFile) {
         this.consumptionsFile = consumptionsFile;
     }
 
     public void setAccountsFile(final String accountsFile) {
         this.accountsFile = accountsFile;
     }
 
     public void setTestDataDirectory(String testDataDirectory) {
         this.testDataDirectory = testDataDirectory;
     }
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public final void setAuthorizingRequestLabel(String authorizingRequestLabel) {
         this.authorizingRequestLabel = authorizingRequestLabel;
     }
 
     public final void setDepositAccountDetailsFile(String depositAccountDetailsFile) {
         this.depositAccountDetailsFile = depositAccountDetailsFile;
     }
 
     public final void setInvoiceDetailsFile(String invoiceDetailsFile) {
         this.invoiceDetailsFile = invoiceDetailsFile;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 
     public void setRequestSearchService(IRequestSearchService requestSearchService) {
         this.requestSearchService = requestSearchService;
     }
 
     public boolean supportsConsumptions() {
         return true;
     }
 
     public boolean handlesTraces() {
         return false;
     }
 
     public List<String> checkExternalReferential(final XmlObject requestXml) {
         return new ArrayList<String>(0);
     }
 
     public Map<String, Object> loadExternalInformations(XmlObject requestXml)
         throws CvqException {
         return Collections.emptyMap();
     }
 
     public void setPaymentService(IPaymentService paymentService) {
         this.paymentService = paymentService;
     }
 }
