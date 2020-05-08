 package fr.capwebct.capdemat.plugins.externalservices.cirilnetenfance.service;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 
 import fr.capwebct.capdemat.plugins.externalservices.cirilnetenfance.ws.Registration;
 
 import fr.cg95.cvq.business.authority.School;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.service.authority.ISchoolService;
 import fr.cg95.cvq.service.request.school.ISchoolRegistrationRequestService;
 import fr.cg95.cvq.xml.common.RequestType;
 import fr.cg95.cvq.xml.request.ecitizen.HomeFolderModificationRequestDocument.HomeFolderModificationRequest;
 import fr.cg95.cvq.xml.request.school.SchoolRegistrationRequestDocument;
 import fr.cg95.cvq.xml.request.school.PerischoolActivityRegistrationRequestDocument.PerischoolActivityRegistrationRequest;
 import fr.cg95.cvq.xml.request.school.RecreationActivityRegistrationRequestDocument.RecreationActivityRegistrationRequest;
 import fr.cg95.cvq.xml.request.school.SchoolCanteenRegistrationRequestDocument.SchoolCanteenRegistrationRequest;
 import fr.cg95.cvq.xml.request.school.SchoolRegistrationRequestDocument.SchoolRegistrationRequest;
 
 public class CirilNetEnfanceService implements IExternalProviderService {
 
     private static Logger logger = Logger.getLogger(CirilNetEnfanceService.class);
 
     private static final String END_POINT = "EndPoint";
     private String endPoint;
     private String label;
     
     private IExternalService externalService;
     private ISchoolService schoolService;
     private ISchoolRegistrationRequestService schoolRegistrationRequestService;
 
     public void checkConfiguration(ExternalServiceBean externalServiceBean)
         throws CvqConfigurationException {
         setEndPoint((String) externalServiceBean.getProperty(END_POINT));
         if (endPoint == null)
             throw new CvqConfigurationException("Missing " + END_POINT
                     + " configuration parameter");
     }
 
     public void creditHomeFolderAccounts(Collection<PurchaseItem> purchaseItems,
             String cvqReference, String bankReference, Long homeFolderId,
             String externalHomeFolderId, String externalId, Date validationDate)
         throws CvqException {      
     }
 
     public Map<String, List<ExternalAccountItem>> getAccountsByHomeFolder(Long homeFolderId,
             String externalHomeFolderId, String externalId) throws CvqException {
         return null;
     }
 
     public Map<Date,String> getConsumptionsByRequest(Request request, Date dateFrom, Date dateTo) {
         return null;
     }
     
     public Map<Individual, Map<String, String>> getIndividualAccountsInformation(Long homeFolderId,
             String externalHomeFolderId, String externalId) throws CvqException {
         return null;
     }
 
     public String helloWorld() throws CvqException {
         return null;
     }
 
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
     }
 
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
     }
     
     public String sendRequest(final XmlObject xmlRequest) throws CvqException {
 
         RequestType request = null;
         try {
             String classSimpleName = xmlRequest.getClass().getSimpleName();
             String methodNameToInvoke = "get" + classSimpleName.substring(0, classSimpleName.lastIndexOf("Document"));
             request = (RequestType) xmlRequest.getClass().getMethod(methodNameToInvoke).invoke(xmlRequest);
         } catch (Exception e) {
             // unlikely to happen
             e.printStackTrace();
             throw new CvqException();
         }
 
        logger.debug("sendRequest() request id " + request.getId());
        logger.debug("sendRequest() external id :" + request.getHomeFolder().getExternalId());
         
         String cirilId = request.getHomeFolder().getExternalId();
         Long homeFolderId = request.getHomeFolder().getId();
         
         if (request instanceof SchoolRegistrationRequest) {
             HashMap<String, Object> repDoc = 
                 sendRegistration(xmlRequest, "SchoolRegistration", homeFolderId);
            if (repDoc.containsKey("school")) {
                logger.debug("sendRequest() got school : " + repDoc.get("school"));
                 School school = getSchool((String)repDoc.get("school"));
                 SchoolRegistrationRequestDocument srrDocument =
                     (SchoolRegistrationRequestDocument) xmlRequest;
                 fr.cg95.cvq.business.request.school.SchoolRegistrationRequest srr =
                     fr.cg95.cvq.business.request.school.SchoolRegistrationRequest.xmlToModel(srrDocument);
                 srr.setSchool(school);
                 schoolRegistrationRequestService.modify(srr);
             }
         }
         else if (request instanceof SchoolCanteenRegistrationRequest) {
             sendRegistration(xmlRequest, "SchoolCanteenRegistration", homeFolderId);
         }
         else if (request instanceof PerischoolActivityRegistrationRequest) {
             sendRegistration(xmlRequest, "PerischoolActivityRegistration", homeFolderId);
         }
         else if (request instanceof RecreationActivityRegistrationRequest) {
             sendRegistration(xmlRequest, "RecreationActivityRegistration", homeFolderId);
         }
         else if (request instanceof HomeFolderModificationRequest && cirilId != null) {
             sendRegistration(xmlRequest, "HomeFolderModification", homeFolderId);
         }
 
         return null;
     }
 
     private HashMap<String, Object> sendRegistration(XmlObject xmlRequest, String registrationType,
             Long homeFolderId) 
         throws CvqException {
 
         Registration registration = new Registration(endPoint, xmlRequest, registrationType);
         HashMap<String, Object> repDoc = registration.getReturnRegistration();
         if (repDoc.containsKey("error")) {
             String messageError = (String) repDoc.get("error");
             throw new CvqModelException("Retour du logiciel metier : " + messageError);
         } else if (repDoc.containsKey("homeFolderMapping")) {
             logger.debug("sendRegistration() adding mapping External id : " 
                     + (String) repDoc.get("homeFolderMapping"));
             externalService.addHomeFolderMapping(getLabel(), homeFolderId, 
                     (String) repDoc.get("homeFolderMapping"));
             if(repDoc.containsKey("indMapp")) {
                 HashMap<String, String> indmapping = (HashMap<String, String>) repDoc.get("indMapp");
                 for(String individu : indmapping.keySet()) {
                     externalService.setExternalId(getLabel(), homeFolderId, Long.valueOf(individu), 
                             indmapping.get(individu));
                 }
             }
         }
         
         return repDoc;
     }
     
     private School getSchool(String schoolName) throws CvqException {
         School school = schoolService.getByName(schoolName);
         if (school == null) {
             if(schoolName.equals("")||schoolName.equals(null))
                 schoolName = "ecole de demo";
             logger.info("getSchool() school " + schoolName + " not found, creating it");
             school = new School();
             school.setActive(true);
             school.setName(schoolName);
             schoolService.create(school);
         }
         return school;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
     
     public String getLabel() {
         return label;
     }
 
     public void setEndPoint(String endPoint) {
         this.endPoint = endPoint;
     }
 
     public void setExternalService(IExternalService externalService) {
         this.externalService = externalService;
     }
 
     public void setSchoolService(ISchoolService schoolService) {
         this.schoolService = schoolService;
     }
 
     public void setSchoolRegistrationRequestService(
             ISchoolRegistrationRequestService schoolRegistrationRequestService) {
         this.schoolRegistrationRequestService = schoolRegistrationRequestService;
     }
 
     @Override
     public List<String> checkExternalReferential(XmlObject requestXml) {
         return Collections.emptyList();
     }
 
     @Override
     public boolean handlesTraces() {
         return false;
     }
 
     @Override
     public Map<String, Object> loadExternalInformations(XmlObject requestXml) throws CvqException {
         return Collections.emptyMap();
     }
 
     @Override
     public boolean supportsConsumptions() {
         return false;
     }
 }
