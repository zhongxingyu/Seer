 package fr.capwebct.capdemat.plugins.externalservices.capwebctpaymentmodule.service;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 
 import fr.capwebct.capdemat.plugins.externalservices.capwebctpaymentmodule.webservice.client.ICapwebctPaymentModuleClient;
 import fr.capwebct.modules.payment.schema.acc.AccountDetailType;
 import fr.capwebct.modules.payment.schema.acc.AccountDetailsDocument;
 import fr.capwebct.modules.payment.schema.acc.AccountDetailsRequestDocument;
 import fr.capwebct.modules.payment.schema.acc.AccountDetailsRequestDocument.AccountDetailsRequest;
 import fr.capwebct.modules.payment.schema.ban.AccountUpdateType;
 import fr.capwebct.modules.payment.schema.ban.BankTransactionDocument;
 import fr.capwebct.modules.payment.schema.ban.ContractUpdateType;
 import fr.capwebct.modules.payment.schema.ban.FamilyType;
 import fr.capwebct.modules.payment.schema.ban.InvoiceUpdateType;
 import fr.capwebct.modules.payment.schema.ban.PaymentType;
 import fr.capwebct.modules.payment.schema.ban.BankTransactionDocument.BankTransaction;
 import fr.capwebct.modules.payment.schema.fam.AccountType;
 import fr.capwebct.modules.payment.schema.fam.ContractType;
 import fr.capwebct.modules.payment.schema.fam.FamilyAccountsRequestDocument;
 import fr.capwebct.modules.payment.schema.fam.FamilyDocument;
 import fr.capwebct.modules.payment.schema.fam.IndividualContractType;
 import fr.capwebct.modules.payment.schema.fam.InvoiceType;
 import fr.capwebct.modules.payment.schema.fam.FamilyAccountsRequestDocument.FamilyAccountsRequest;
 import fr.capwebct.modules.payment.schema.fam.FamilyDocument.Family;
 import fr.capwebct.modules.payment.schema.inv.InvoiceDetailType;
 import fr.capwebct.modules.payment.schema.inv.InvoiceDetailsDocument;
 import fr.capwebct.modules.payment.schema.inv.InvoiceDetailsRequestDocument;
 import fr.capwebct.modules.payment.schema.inv.InvoiceDetailsRequestDocument.InvoiceDetailsRequest;
 import fr.capwebct.modules.payment.schema.sre.SendRequestRequestDocument;
 import fr.capwebct.modules.payment.schema.sre.SendRequestRequestDocument.SendRequestRequest;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItemDetail;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItemDetail;
 import fr.cg95.cvq.business.users.payment.ExternalTicketingContractItem;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.payment.impl.PaymentService;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.xml.common.RequestType;
 
 public class CapwebctPaymentModuleService implements IExternalProviderService {
 
     private static Logger logger = Logger.getLogger(CapwebctPaymentModuleService.class);
     
     /*
      * CapwebctPaymentModule needs 'externalFamilyAccountId',
      * 'externalApplicationLabel' properties to uniquely identify an external account item.
      */
     public static final String EXTERNAL_APPLICATION_ID_KEY = "externalApplicationId";
     public static final String EXTERNAL_FAMILY_ACCOUNT_ID_KEY = "externalFamilyAccountId";
     public static final String EXTERNAL_INDIVIDUAL_ID_KEY = "externalIndividualId";
     
     private String label;
 
     private ICapwebctPaymentModuleClient capwebctPaymentModuleClient;
 
     public void init() {
     }
     
     public Map<String, List<ExternalAccountItem>> getAccountsByHomeFolder(Long homeFolderId, String externalHomeFolderId, String externalId)
         throws CvqException {
         
         FamilyAccountsRequestDocument farDocument = 
             FamilyAccountsRequestDocument.Factory.newInstance();
         FamilyAccountsRequest far = farDocument.addNewFamilyAccountsRequest();
         far.setLocalAuthority(SecurityContext.getCurrentSite().getName());
         far.setHomeFolderId(homeFolderId);
 
         FamilyDocument familyDocument = 
             (FamilyDocument) capwebctPaymentModuleClient.getFamilyAccounts(farDocument);
         Family family = familyDocument.getFamily();
 
         Map<String, List<ExternalAccountItem>> resultMap = 
             new HashMap<String, List<ExternalAccountItem>>();
 
         if (family.getAccounts() != null) {
             List<ExternalAccountItem> resultEaiList = new ArrayList<ExternalAccountItem>();
             AccountType[] accountTypes = family.getAccounts().getAccountArray();
             for (AccountType accountType : accountTypes) {
                 ExternalDepositAccountItem eai = new ExternalDepositAccountItem();
                 eai.setSupportedBroker(accountType.getBroker());
                 eai.setExternalServiceLabel(getLabel());
                 eai.setExternalItemId(accountType.getAccountId());
                 eai.setLabel(accountType.getAccountLabel());
                 eai.setOldValue(new Double(accountType.getAccountValue()));
                 Calendar oldValueDate = accountType.getAccountDate();
                 eai.setOldValueDate(oldValueDate.getTime());
 
                 eai.addExternalServiceSpecificData(EXTERNAL_FAMILY_ACCOUNT_ID_KEY,
                         accountType.getExternalFamilyAccountId());
                 eai.addExternalServiceSpecificData(EXTERNAL_APPLICATION_ID_KEY, 
                         String.valueOf(accountType.getExternalApplicationId()));
 
                 resultEaiList.add(eai);
             }
             resultMap.put(PaymentService.EXTERNAL_DEPOSIT_ACCOUNTS, resultEaiList);
         }
 
         if (family.getInvoices() != null) {
             List<ExternalAccountItem> resultEaiList = new ArrayList<ExternalAccountItem>();
             InvoiceType[] invoiceTypes = family.getInvoices().getInvoiceArray();
             for (InvoiceType invoiceType : invoiceTypes) {
                 ExternalInvoiceItem eii = new ExternalInvoiceItem();
                 eii.setSupportedBroker(invoiceType.getBroker());
                 eii.setExternalServiceLabel(getLabel());
                 eii.setExternalItemId(invoiceType.getInvoiceId());
                 eii.setLabel(invoiceType.getInvoiceLabel());
                 eii.setAmount(new Double(invoiceType.getInvoiceValue()));
                 Calendar calendar = invoiceType.getInvoiceExpirationDate();
                 eii.setExpirationDate(calendar.getTime());
                 calendar = invoiceType.getInvoiceDate();
                 eii.setIssueDate(calendar.getTime());
                 if (invoiceType.getInvoicePaymentDate() != null) {
                     calendar = invoiceType.getInvoicePaymentDate();
                     eii.setPaymentDate(calendar.getTime());
                 }
                 eii.setIsPaid(invoiceType.getInvoicePaid());
 
                 eii.addExternalServiceSpecificData(EXTERNAL_FAMILY_ACCOUNT_ID_KEY,
                         invoiceType.getExternalFamilyAccountId());
                 eii.addExternalServiceSpecificData(EXTERNAL_APPLICATION_ID_KEY, 
                         String.valueOf(invoiceType.getExternalApplicationId()));
 
                 resultEaiList.add(eii);
             }
             resultMap.put(PaymentService.EXTERNAL_INVOICES, resultEaiList);
         }
 
         if (family.getContracts() != null) {
             List<ExternalAccountItem> resultEaiList = new ArrayList<ExternalAccountItem>();
             IndividualContractType[] individualContractTypes = family.getContracts().getContractArray();
             for (IndividualContractType individualContractType : individualContractTypes) {
                 if (individualContractType.getContractArray() != null) {
                     ContractType[] contractTypes = individualContractType.getContractArray();
                     for (ContractType contractType : contractTypes) {
                         ExternalTicketingContractItem etci = new ExternalTicketingContractItem();
                         etci.setSupportedBroker(contractType.getBroker());
                         etci.setExternalServiceLabel(getLabel());
                         etci.setExternalItemId(contractType.getContractId());
                         etci.setAmount(new Double(contractType.getContractValue()));
                         etci.setOldQuantity(contractType.getContractValue() / contractType.getBuyPrice());
                         etci.setCreationDate(contractType.getContractDate().getTime());
                         etci.setLabel(contractType.getContractLabel());
                         etci.setMaxBuy(contractType.getMaxBuy());
                         etci.setMinBuy(contractType.getMinBuy());
                         etci.setUnitPrice(new Double(contractType.getBuyPrice()));
                         etci.setSubjectId(individualContractType.getCapwebctIndividualId());
 
                         etci.addExternalServiceSpecificData(EXTERNAL_FAMILY_ACCOUNT_ID_KEY,
                                 contractType.getExternalFamilyAccountId());
                         etci.addExternalServiceSpecificData(EXTERNAL_APPLICATION_ID_KEY, 
                                 String.valueOf(contractType.getExternalApplicationId()));
                         etci.addExternalServiceSpecificData(EXTERNAL_INDIVIDUAL_ID_KEY, 
                                 contractType.getExternalIndividualId());
 
                         resultEaiList.add(etci);
                     }
                     resultMap.put(PaymentService.EXTERNAL_TICKETING_ACCOUNTS, resultEaiList);
                 }
             }
         }
         return resultMap;
     }
 
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
         if (edai.getExternalItemId() == null
                 || edai.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY) == null
                 || edai.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY) == null) {
             edai = null;
             return;
         }
         
         AccountDetailsRequestDocument accountDetailsRequestDocument =
             AccountDetailsRequestDocument.Factory.newInstance();
         AccountDetailsRequest accountDetailsRequest = 
             accountDetailsRequestDocument.addNewAccountDetailsRequest();
         accountDetailsRequest.setAccountId(edai.getExternalItemId());
         accountDetailsRequest.setExternalApplicationId(
                 Long.valueOf(edai.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY)));
         accountDetailsRequest.setExternalFamilyAccountId(
                 edai.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY));
 
         // Calls webservice
         AccountDetailsDocument accountDetailsDocument = (AccountDetailsDocument) 
             capwebctPaymentModuleClient.loadAccountDetails(accountDetailsRequestDocument);
 
         AccountDetailType[] accountDetailTypes = 
             accountDetailsDocument.getAccountDetails().getAccountDetailArray();
         for (int i = 0; i < accountDetailTypes.length; i++) {
             AccountDetailType accountDetailType = accountDetailTypes[i];
             ExternalDepositAccountItemDetail edaiDetail = new ExternalDepositAccountItemDetail();
             edaiDetail.setDate(accountDetailType.getDate().getTime());
             edaiDetail.setHolderName(accountDetailType.getHolderName());
             edaiDetail.setHolderSurname(accountDetailType.getHolderSurname());
             edaiDetail.setPaymentId(accountDetailType.getPaymentAck());
             edaiDetail.setPaymentType(accountDetailType.getPaymentType());
             edaiDetail.setValue(accountDetailType.getValue());
             if (edai.getAccountDetails() == null)
                 edai.setAccountDetails(new HashSet<ExternalDepositAccountItemDetail>());
             edai.addAccountDetail(edaiDetail);
         }
     }
 
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
         if (eii.getExternalItemId() == null
                 || eii.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY) == null
                 || eii.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY) == null) {
             eii = null;
             return;
         }
         
         InvoiceDetailsRequestDocument invoiceDetailsRequestDocument =
             InvoiceDetailsRequestDocument.Factory.newInstance();
         InvoiceDetailsRequest invoiceDetailsRequest =
             invoiceDetailsRequestDocument.addNewInvoiceDetailsRequest();
         invoiceDetailsRequest.setInvoiceId(eii.getExternalItemId());
         invoiceDetailsRequest.setExternalApplicationId(
                 Long.valueOf(eii.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY)));
         invoiceDetailsRequest.setExternalFamilyAccountId(
                 eii.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY));
 
         // Calls webservice
         InvoiceDetailsDocument invoiceDetailsDocument = (InvoiceDetailsDocument) 
             capwebctPaymentModuleClient.loadInvoiceDetails(invoiceDetailsRequestDocument);
 
         InvoiceDetailType[] invoiceDetailTypes = 
             invoiceDetailsDocument.getInvoiceDetails().getInvoiceDetailArray();
         for (int i = 0; i < invoiceDetailTypes.length; i++) {
             ExternalInvoiceItemDetail eiiDetail = new ExternalInvoiceItemDetail();
             InvoiceDetailType invoiceDetailType = invoiceDetailTypes[i];
             eiiDetail.setSubjectName(invoiceDetailType.getChildName());
             eiiDetail.setSubjectSurname(invoiceDetailType.getChildSurname());
             eiiDetail.setLabel(invoiceDetailType.getLabel());
             eiiDetail.setQuantity(invoiceDetailType.getQuantity());
             eiiDetail.setUnitPrice(invoiceDetailType.getUnitPrice());
             eiiDetail.setValue(invoiceDetailType.getValue());
             if (eii.getInvoiceDetails() == null)
                 eii.setInvoiceDetails(new HashSet<ExternalInvoiceItemDetail>());
             eii.getInvoiceDetails().add(eiiDetail);
         }
     }
 
     public void creditHomeFolderAccounts(Collection purchaseItems, String cvqReference,
             String bankReference, Long homeFolderId, String externalHomeFolderId, String externalId, Date validationDate) throws CvqException {
         
         BankTransactionDocument bankTransactionDocument = 
             BankTransactionDocument.Factory.newInstance();
         BankTransaction bankTransaction = bankTransactionDocument.addNewBankTransaction();
 
         FamilyType familyType = bankTransaction.addNewFamily();
         familyType.setId(homeFolderId);
         familyType.setZip(SecurityContext.getCurrentSite().getPostalCode());
 
         Calendar calendar = Calendar.getInstance();
         int totalAmount = 0;
         String broker = null;
         for (Iterator iter = purchaseItems.iterator(); iter.hasNext();) {
             PurchaseItem purchaseItem = (PurchaseItem) iter.next();
             // purchase items in a payment transaction can not belong to more than one broker
             // so take the first we meet
             if (broker == null)
                 broker = purchaseItem.getSupportedBroker();
             totalAmount += purchaseItem.getAmount().intValue();
         }
         PaymentType paymentType = bankTransaction.addNewPayment();
         paymentType.setPaymentBroker(broker);
         paymentType.setCvqAck(cvqReference);
         paymentType.setPaymentAck(bankReference);
         paymentType.setPaymentAmount(totalAmount);
         calendar.setTime(validationDate);
         paymentType.setPaymentDate(calendar);
 
         List<AccountUpdateType> accountUpdateTypes = new ArrayList<AccountUpdateType>();
         List<ContractUpdateType> contractUpdateTypes = new ArrayList<ContractUpdateType>();
         List<InvoiceUpdateType> invoiceUpdateTypes = new ArrayList<InvoiceUpdateType>();
         for (Iterator iter = purchaseItems.iterator(); iter.hasNext();) {
             PurchaseItem purchaseItem = (PurchaseItem) iter.next();
 
             if (purchaseItem instanceof ExternalDepositAccountItem) {
                 ExternalDepositAccountItem edai = (ExternalDepositAccountItem) purchaseItem;
                 AccountUpdateType updateType = AccountUpdateType.Factory.newInstance();
                 updateType.setAccountId(edai.getExternalItemId());
                 updateType.setExternalApplicationId(
                         Long.valueOf(edai.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY)));
                 updateType.setExternalFamilyAccountId(
                         edai.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY));
                 updateType.setAccountNewValue(edai.getOldValue().intValue() + edai.getAmount().intValue());
                 updateType.setAccountOldValue(edai.getOldValue().intValue());
                 calendar.setTime(edai.getOldValueDate());
                 updateType.setAccountOldValueDate(calendar);
                 accountUpdateTypes.add(updateType);
             }
 
             if (purchaseItem instanceof ExternalInvoiceItem) {
                 ExternalInvoiceItem eii = (ExternalInvoiceItem) purchaseItem;
                 InvoiceUpdateType updateType = InvoiceUpdateType.Factory.newInstance();
                 updateType.setInvoiceId(eii.getExternalItemId());
                 updateType.setExternalApplicationId(
                         Long.valueOf(eii.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY)));
                 updateType.setExternalFamilyAccountId(
                         eii.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY));
                 updateType.setAmount(eii.getAmount().intValue());
                 invoiceUpdateTypes.add(updateType);
             }
 
             if (purchaseItem instanceof ExternalTicketingContractItem) {
                 ExternalTicketingContractItem etci = (ExternalTicketingContractItem) purchaseItem;
                 ContractUpdateType updateType = ContractUpdateType.Factory.newInstance();
                 updateType.setContractId(etci.getExternalItemId());
                 updateType.setExternalApplicationId(
                         Long.valueOf(etci.getExternalServiceSpecificDataByKey(EXTERNAL_APPLICATION_ID_KEY)));
                 updateType.setExternalFamilyAccountId(
                         etci.getExternalServiceSpecificDataByKey(EXTERNAL_FAMILY_ACCOUNT_ID_KEY));
                 updateType.setExternalIndividualId(
                         etci.getExternalServiceSpecificDataByKey(EXTERNAL_INDIVIDUAL_ID_KEY));
                 updateType.setCapwebctIndividualId(etci.getSubjectId());
                 updateType.setPrice(etci.getUnitPrice().intValue());
                 updateType.setQuantity(etci.getQuantity());
                 updateType.setAmount(etci.getAmount().intValue());
                 contractUpdateTypes.add(updateType);
             }
         }
         if (accountUpdateTypes.size() > 0)
             bankTransaction.addNewAccounts().setAccountArray(
                     accountUpdateTypes.toArray(new AccountUpdateType[]{}));
         if (contractUpdateTypes.size() > 0)
             bankTransaction.addNewContracts().setContractArray(
                     contractUpdateTypes.toArray(new ContractUpdateType[]{}));
         if (invoiceUpdateTypes.size() > 0)
             bankTransaction.addNewInvoices().setInvoiceArray(
                     invoiceUpdateTypes.toArray(new InvoiceUpdateType[]{}));
         
         capwebctPaymentModuleClient.creditAccount(bankTransactionDocument);
     }
 
     public String sendRequest(XmlObject requestXml) throws CvqException {
         SendRequestRequestDocument sendRequestRequestDocument =
             SendRequestRequestDocument.Factory.newInstance();
         SendRequestRequest sendRequestRequest =
             sendRequestRequestDocument.addNewSendRequestRequest();
        RequestType request = null;
         try {
             request = (RequestType)requestXml.getClass()
                 .getMethod("get" + requestXml.getClass().getSimpleName()
                 .replace("DocumentImpl", "")).invoke(requestXml);
         } catch (IllegalAccessException e) {
             logger.error("fillXmlObject() Illegal access exception while filling request xml");
             throw new CvqException("Illegal access exception while filling request xml");
         } catch (InvocationTargetException e) {
             logger.error("fillXmlObject() Invocation target exception while filling request xml");
             throw new CvqException("Invocation target exception while filling request xml");
         } catch (NoSuchMethodException e) {
             logger.error("fillXmlObject() No such method exception while filling request xml");
             throw new CvqException("No such method exception while filling request xml");
         }
         sendRequestRequest.setRequest(request);
         sendRequestRequest.setRequestTypeLabel(request.getRequestTypeLabel());
         capwebctPaymentModuleClient.sendRequest(sendRequestRequestDocument);
         return "";
     }
 
     /** ***** Not Implemented methods ****** */
     /** *********************************** */
 
     public Map<Date, String> getConsumptionsByRequest(Request request, Date dateFrom, Date dateTo)
             throws CvqException {
         return null;
     }
 
     public Map<Individual, Map<String, String>> getIndividualAccountsInformation(Long homeFolderId, String externalHomeFolderId, String externalId)
             throws CvqException {
         return null;
     }
 
     public void checkConfiguration(ExternalServiceBean externalServiceBean)
             throws CvqConfigurationException {
         logger.debug("checkConfiguration() nothing special to do");
     }
 
     public String helloWorld() throws CvqException {
         return null;
     }
 
     /** ******************************* */
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public void setCapwebctPaymentModuleClient(ICapwebctPaymentModuleClient capwebctPaymentModuleClient) {
         this.capwebctPaymentModuleClient = capwebctPaymentModuleClient;
     }
 
     public boolean supportsConsumptions() {
         return false;
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
 }
