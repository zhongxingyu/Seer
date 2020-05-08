 package fr.capwebct.capdemat.plugins.externalservices.edemande.service;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.apache.xmlbeans.XmlObject;
 import org.jaxen.JaxenException;
 import org.jaxen.dom.DOMXPath;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.SftpException;
 import com.unilog.gda.edem.service.EnregistrerValiderFormulaireResponseDocument;
 import com.unilog.gda.glob.service.GestionCompteResponseDocument;
 
 import fr.capwebct.capdemat.plugins.externalservices.edemande.webservice.client.IEdemandeClient;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentBinary;
 import fr.cg95.cvq.business.external.ExternalServiceTrace;
 import fr.cg95.cvq.business.external.TraceStatusEnum;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.SexType;
 import fr.cg95.cvq.business.users.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.users.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.users.payment.PurchaseItem;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.external.ExternalServiceBean;
 import fr.cg95.cvq.external.IExternalProviderService;
 import fr.cg95.cvq.external.IExternalService;
 import fr.cg95.cvq.service.document.IDocumentService;
 import fr.cg95.cvq.service.document.IDocumentTypeService;
 import fr.cg95.cvq.service.request.IRequestWorkflowService;
 import fr.cg95.cvq.service.request.school.IStudyGrantRequestService;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.translation.ITranslationService;
 import fr.cg95.cvq.xml.common.AddressType;
 import fr.cg95.cvq.xml.request.school.StudyGrantRequestDocument;
 import fr.cg95.cvq.xml.request.school.StudyGrantRequestDocument.StudyGrantRequest;
 
 public class EdemandeService implements IExternalProviderService, BeanFactoryAware {
 
     private String label;
     private IEdemandeClient edemandeClient;
     private IExternalService externalService;
     private IStudyGrantRequestService requestService;
     private IDocumentService documentService;
     private IRequestWorkflowService requestWorkflowService;
     private ITranslationService translationService;
     private IHomeFolderService homeFolderService;
     private EdemandeUploader uploader;
     private ListableBeanFactory beanFactory;
 
     private static final String SUBJECT_TRACE_SUBKEY = "subject";
     private static final String ACCOUNT_HOLDER_TRACE_SUBKEY = "accountHolder";
     private DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
     private List<String> documentTypesToSend = Collections.emptyList();
 
     public void init() {
         this.homeFolderService = (IHomeFolderService)beanFactory.getBean("homeFolderService");
     }
 
     @Override
     public String sendRequest(XmlObject requestXml) {
         StudyGrantRequest sgr = ((StudyGrantRequestDocument) requestXml).getStudyGrantRequest();
         String psCodeTiersAH = null;
         if (!sgr.getIsSubjectAccountHolder()) {
             psCodeTiersAH = sgr.getAccountHolderEdemandeId();
             if (psCodeTiersAH == null || psCodeTiersAH.trim().isEmpty()) {
                 psCodeTiersAH = searchAccountHolder(sgr);
                 if (psCodeTiersAH == null || psCodeTiersAH.trim().isEmpty()) {
                     if (mustCreateAccountHolder(sgr)) {
                         createAccountHolder(sgr);
                     } else if (psCodeTiersAH != null) {
                         addTrace(sgr.getId(), ACCOUNT_HOLDER_TRACE_SUBKEY, TraceStatusEnum.IN_PROGRESS, 
                             "Le tiers viré n'est pas encore créé");
                     }
                     return null;
                 } else {
                     sgr.setAccountHolderEdemandeId(psCodeTiersAH);
                     try {
                         requestService.setAccountHolderEdemandeId(sgr.getId(), psCodeTiersAH);
                     } catch (CvqException e) {
                         // TODO
                     }
                 }
             }
         }
         String psCodeTiersS = sgr.getSubject().getIndividual().getExternalId();
         if (psCodeTiersS == null || psCodeTiersS.trim().isEmpty()) {
             // external id (code tiers) not known locally : 
             //     either check if tiers has been created in eDemande
             //     either ask for its creation in eDemande
             psCodeTiersS = searchSubject(sgr);
             // add a "hack" condition when psCodeTiersS == psCodeTiersAH
             // to handle homonyms until individual search accepts birth date etc.
             if (psCodeTiersS == null || psCodeTiersS.trim().isEmpty() || psCodeTiersS.trim().equals(psCodeTiersAH)) {
                 // tiers has not been created in eDemande ...
                 if (mustCreateSubject(sgr)) {
                     // ... and no request in progress so ask for its creation
                     createSubject(sgr);
                 } else if (psCodeTiersS != null) {
                     // eDemande answered since psCodeTiers is not null,
                     // and that means psCodeTiers is empty, so tiers
                     // has not been created yet.
                     // If psCodeTiers was null, that would mean searchIndividual
                     // caught an exception while contacting eDemande, and
                     // has already added a NOT_SENT trace.
                     // FIXME BOR : is this trace really needed ?
                     addTrace(sgr.getId(), SUBJECT_TRACE_SUBKEY, TraceStatusEnum.IN_PROGRESS, 
                             "Le tiers sujet n'est pas encore créé");
                 }
                 return null;
             } else {
                 // tiers has been created in eDemande, store its code locally
                 sgr.getSubject().getIndividual().setExternalId(psCodeTiersS);
                 externalService.setExternalId(label, sgr.getHomeFolder().getId(), 
                         sgr.getSubject().getIndividual().getId(), psCodeTiersS);
             }
         }
         
         // reaching this code means we have valid psCodeTiers (either because
         // they were already set since it is not the subject and account holder's first request, or because
         // searchIndividual returned the newly created tiers' psCodeTiers)
         // Try to get the external ID if we don't already know it
         String psCodeDemande = sgr.getEdemandeId();
         if (psCodeDemande == null || psCodeDemande.trim().isEmpty()) {
             psCodeDemande = searchRequest(sgr, psCodeTiersS);
             if (psCodeDemande != null && !psCodeDemande.trim().isEmpty() && !"-1".equals(psCodeDemande)) {
                 sgr.setEdemandeId(psCodeDemande);
                 try {
                     requestService.setEdemandeId(sgr.getId(), psCodeDemande);
                 } catch (CvqException e) {
                     // TODO
                 }
             }
         }
         // (Re)send request if needed
         if (mustSendNewRequest(sgr)) {
             submitRequest(sgr, psCodeTiersS, true);
         } else if (mustResendRequest(sgr)) {
             submitRequest(sgr, psCodeTiersS, false);
         }
         // check request status
         String msStatut = getRequestStatus(sgr, psCodeTiersS);
         if (msStatut == null) {
             // got an exception while contacting Edemande
             return null;
         }
         if (msStatut.trim().isEmpty()) {
             addTrace(sgr.getId(), null, TraceStatusEnum.NOT_SENT, 
                 "La demande n'a pas encore été reçue");
             return null;
         }
         if ("En attente de réception par la collectivité".equals(msStatut)) {
             return null;
         } else if ("A compléter ou corriger".equals(msStatut) ||
             "A compléter".equals(msStatut) ||
             "En erreur".equals(msStatut)) {
             addTrace(sgr.getId(), null, TraceStatusEnum.ERROR, msStatut);
         } else if ("En cours d'analyse".equals(msStatut) ||
             "En attente d'avis externe".equals(msStatut) ||
             "En cours d'instruction".equals(msStatut)) {
             addTrace(sgr.getId(), null, TraceStatusEnum.ACKNOWLEDGED, msStatut);
         } else if ("Accepté".equals(msStatut) ||
             "En cours de paiement".equals(msStatut) ||
             "Payé partiellement".equals(msStatut) ||
             "Terminé".equals(msStatut)) {
             addTrace(sgr.getId(), null, TraceStatusEnum.ACCEPTED, msStatut);
         } else if ("Refusé".equals(msStatut)) {
             addTrace(sgr.getId(), null, TraceStatusEnum.REJECTED, msStatut);
         }
         return null;
     }
 
     private void addTrace(Long requestId, String subkey, TraceStatusEnum status, String message) {
         ExternalServiceTrace est = new ExternalServiceTrace();
         est.setDate(new Date());
         est.setKey(String.valueOf(requestId));
         est.setSubkey(subkey);
         est.setKeyOwner("capdemat");
         est.setMessage(message);
         est.setName(label);
         est.setStatus(status);
         externalService.addTrace(est);
         if (TraceStatusEnum.ERROR.equals(status)) {
             try {
                 requestWorkflowService.updateRequestState(requestId, RequestState.UNCOMPLETE, message);
             } catch (CvqObjectNotFoundException e) {
                 // TODO
             } catch (CvqInvalidTransitionException e) {
                 // TODO
             } catch (CvqException e) {
                 // TODO
             }
         }
     }
 
     /**
      * Search for this request's individual in eDemande.
      * 
      * @return the individual's code in eDemande, an empty string if the individual is not found,
      * or null if there is an error while contacting eDemande.
      */
     private String searchIndividual(StudyGrantRequest sgr, String firstName,
         String lastName, Calendar birthDate, String subkey) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("lastName", StringUtils.upperCase(lastName));
         model.put("bankCode", sgr.getBankCode());
         model.put("counterCode", sgr.getCounterCode());
         model.put("accountNumber", sgr.getAccountNumber());
         model.put("accountKey", sgr.getAccountKey());
         String searchResults;
         int resultsNumber;
         try {
             searchResults =
                 edemandeClient.rechercherTiers(escapeStrings(model))
                 .getRechercherTiersResponse().getReturn();
             resultsNumber = parseDatas(searchResults,
                 "//resultatRechTiers/listeTiers/tiers/codeTiers").size();
         } catch (CvqException e) {
             addTrace(sgr.getId(), subkey, TraceStatusEnum.NOT_SENT, e.getMessage());
             return null;
         }
         if (resultsNumber == 0) {
             return "";
         }
         if (resultsNumber == 1) {
             try {
                 return parseData(searchResults,
                     "//resultatRechTiers/listeTiers/tiers/codeTiers");
             } catch (CvqException e) {
                 addTrace(sgr.getId(), subkey, TraceStatusEnum.NOT_SENT, e.getMessage());
                 return null;
             }
         }
         for (int i = 1; i <= resultsNumber; i++) {
             try {
                 String code =
                     parseData(searchResults,
                         "//resultatRechTiers/listeTiers/tiers[" + i
                         + "]/codeTiers");
                 String informations =
                     edemandeClient.initialiserFormulaire(code)
                     .getInitialiserFormulaireResponse().getReturn();
                 if (parseData(informations,
                     "/CBdosInitFormulaireBean/moTierInit/msPrenom")
                     .equalsIgnoreCase(WordUtils.capitalizeFully(
                         firstName, new char[]{' ', '-'}))
                     && parseData(informations,
                         "/CBdosInitFormulaireBean/moTierInit/mdtDateNaissance")
                         .equals(new SimpleDateFormat("yyyy-MM-dd")
                         .format(birthDate.getTime()))) {
                     return code;
                 }
             } catch (CvqException e) {
                 continue;
             }
         }
         return "";
     }
 
     private String searchSubject(StudyGrantRequest sgr) {
         return searchIndividual(sgr,
             sgr.getSubject().getIndividual().getFirstName(),
             sgr.getSubject().getIndividual().getLastName(),
             sgr.getSubjectInformations().getSubjectBirthDate(),
             SUBJECT_TRACE_SUBKEY);
     }
 
     private String searchAccountHolder(StudyGrantRequest sgr) {
         return searchIndividual(sgr, sgr.getAccountHolderFirstName(),
             sgr.getAccountHolderLastName(), sgr.getAccountHolderBirthDate(),
             ACCOUNT_HOLDER_TRACE_SUBKEY);
     }
 
     private void createSubject(StudyGrantRequest sgr) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("lastName", StringUtils.upperCase(sgr.getSubject().getIndividual().getLastName()));
         model.put("address", sgr.getSubjectInformations().getSubjectAddress());
         if (sgr.getSubjectInformations().getSubjectPhone() != null && !sgr.getSubjectInformations().getSubjectPhone().trim().isEmpty()) {
             model.put("phone", sgr.getSubjectInformations().getSubjectPhone());
         } else if (sgr.getSubjectInformations().getSubjectMobilePhone() != null && !sgr.getSubjectInformations().getSubjectMobilePhone().trim().isEmpty()) {
             model.put("phone", sgr.getSubjectInformations().getSubjectMobilePhone());
         }
         if (sgr.getSubject().getAdult() != null) {
             model.put("title",
                 translationService.translate("homeFolder.adult.title."
                 + sgr.getSubject().getAdult().getTitle().toString().toLowerCase(), Locale.FRANCE));
         } else {
             if (SexType.MALE.toString().equals(sgr.getSubject().getIndividual().getSex().toString())) {
                 model.put("title",
                     translationService.translate("homeFolder.adult.title.mister", Locale.FRANCE));
             } else if (SexType.FEMALE.toString().equals(sgr.getSubject().getIndividual().getSex().toString())) {
                 model.put("title",
                     translationService.translate("homeFolder.adult.title.miss", Locale.FRANCE));
             } else {
                 model.put("title",
                     translationService.translate("homeFolder.adult.title.unknown", Locale.FRANCE));
             }
         }
         model.put("firstName", WordUtils.capitalizeFully(
             sgr.getSubject().getIndividual().getFirstName(), new char[]{' ', '-'}));
         model.put("birthPlace",
             sgr.getSubject().getIndividual().getBirthPlace() != null ?
             StringUtils.defaultString(sgr.getSubject().getIndividual().getBirthPlace().getCity())
             : "");
         model.put("birthDate", formatDate(sgr.getSubjectInformations().getSubjectBirthDate()));
         model.put("bankCode", sgr.getBankCode());
         model.put("counterCode", sgr.getCounterCode());
         model.put("accountNumber", sgr.getAccountNumber());
         model.put("accountKey", sgr.getAccountKey());
         try {
             model.put("email",
                 StringUtils.defaultIfEmpty(sgr.getSubjectInformations().getSubjectEmail(),
                 homeFolderService.getHomeFolderResponsible(sgr.getHomeFolder().getId()).getEmail()));
             GestionCompteResponseDocument response =
                 edemandeClient.creerTiers(escapeStrings(model));
             if (!"0".equals(parseData(response.getGestionCompteResponse().getReturn(), "//Retour/codeRetour"))) {
                 addTrace(sgr.getId(), SUBJECT_TRACE_SUBKEY, TraceStatusEnum.ERROR, parseData(response.getGestionCompteResponse().getReturn(), "//Retour/messageRetour"));
             } else {
                 addTrace(sgr.getId(), SUBJECT_TRACE_SUBKEY, TraceStatusEnum.IN_PROGRESS, "Demande de création du tiers sujet");
             }
         } catch (CvqException e) {
             e.printStackTrace();
             addTrace(sgr.getId(), SUBJECT_TRACE_SUBKEY, TraceStatusEnum.NOT_SENT, e.getMessage());
         }
     }
 
     private void createAccountHolder(StudyGrantRequest sgr) {
         Map<String, Object> model = new HashMap<String, Object>();
         model.put("title",
             translationService.translate("homeFolder.adult.title."
             + sgr.getAccountHolderTitle().toString().toLowerCase(), Locale.FRANCE));
         model.put("lastName", StringUtils.upperCase(sgr.getAccountHolderLastName()));
         //FIXME placeholders; are these really needed ?
         model.put("address", sgr.getSubjectInformations().getSubjectAddress());
         model.put("phone", "");
         model.put("birthPlace", "");
         //ENDFIXME
         model.put("firstName", WordUtils.capitalizeFully(
             sgr.getAccountHolderFirstName(), new char[]{' ', '-'}));
         model.put("birthDate", formatDate(sgr.getAccountHolderBirthDate()));
         model.put("bankCode", sgr.getBankCode());
         model.put("counterCode", sgr.getCounterCode());
         model.put("accountNumber", sgr.getAccountNumber());
         model.put("accountKey", sgr.getAccountKey());
         try {
             //FIXME placeholder
             model.put("email",
                 homeFolderService.getHomeFolderResponsible(sgr.getHomeFolder().getId()).getEmail());
             GestionCompteResponseDocument response =
                 edemandeClient.creerTiers(escapeStrings(model));
             if (!"0".equals(parseData(response.getGestionCompteResponse().getReturn(), "//Retour/codeRetour"))) {
                 addTrace(sgr.getId(), ACCOUNT_HOLDER_TRACE_SUBKEY, TraceStatusEnum.ERROR, parseData(response.getGestionCompteResponse().getReturn(), "//Retour/messageRetour"));
             } else {
                 addTrace(sgr.getId(), ACCOUNT_HOLDER_TRACE_SUBKEY, TraceStatusEnum.IN_PROGRESS, "Demande de création du tiers viré");
             }
         } catch (CvqException e) {
             e.printStackTrace();
             addTrace(sgr.getId(), ACCOUNT_HOLDER_TRACE_SUBKEY, TraceStatusEnum.NOT_SENT, e.getMessage());
         }
     }
 
     private void submitRequest(StudyGrantRequest sgr, String psCodeTiers, boolean firstSending) {
         Map<String, Object> model = new HashMap<String, Object>();
         String requestData = null;
         if (!firstSending) {
             try {
                 requestData = edemandeClient.chargerDemande(psCodeTiers, sgr.getEdemandeId()).getChargerDemandeResponse().getReturn();
             } catch (CvqException e) {
                 e.printStackTrace();
                 addTrace(sgr.getId(), null, TraceStatusEnum.NOT_SENT, e.getMessage());
             }
         }
         model.put("externalRequestId", buildExternalRequestId(sgr));
         model.put("psCodeTiers", psCodeTiers);
         model.put("psCodeDemande",
             StringUtils.defaultIfEmpty(sgr.getEdemandeId(), "-1"));
         model.put("etatCourant", firstSending ? 2 : 1);
         model.put("firstName", WordUtils.capitalizeFully(
             sgr.getSubject().getIndividual().getFirstName(), new char[]{' ', '-'}));
         model.put("lastName", StringUtils.upperCase(sgr.getSubject().getIndividual().getLastName()));
         model.put("address", sgr.getSubjectInformations().getSubjectAddress());
         if (sgr.getSubjectInformations().getSubjectPhone() != null && !sgr.getSubjectInformations().getSubjectPhone().trim().isEmpty()) {
             model.put("phone", sgr.getSubjectInformations().getSubjectPhone());
         } else if (sgr.getSubjectInformations().getSubjectMobilePhone() != null && !sgr.getSubjectInformations().getSubjectMobilePhone().trim().isEmpty()) {
             model.put("phone", sgr.getSubjectInformations().getSubjectMobilePhone());
         }
         model.put("bankCode", sgr.getBankCode());
         model.put("counterCode", sgr.getCounterCode());
         model.put("accountNumber", sgr.getAccountNumber());
         model.put("accountKey", sgr.getAccountKey());
         model.put("firstRequest", sgr.getSubjectInformations().getSubjectFirstRequest());
         model.put("creationDate", formatDate(sgr.getCreationDate()));
         model.put("taxHouseholdCityCode",
             sgr.getTaxHouseholdCityArray().length == 0 ? "" :
             sgr.getTaxHouseholdCityArray(0).getName());
         model.put("taxHouseholdIncome", sgr.getTaxHouseholdIncome());
         model.put("hasCROUSHelp", sgr.getHasCROUSHelp());
         model.put("hasRegionalCouncilHelp", sgr.getHasRegionalCouncilHelp());
         model.put("hasEuropeHelp", sgr.getHasEuropeHelp());
         model.put("hasOtherHelp", sgr.getHasOtherHelp());
         model.put("AlevelsDate", sgr.getALevelsInformations().getAlevelsDate());
         model.put("AlevelsType",
             translationService.translate("sgr.property.alevels."
             + sgr.getALevelsInformations().getAlevels().toString().toLowerCase(), Locale.FRANCE));
         model.put("currentStudiesType",
             StringUtils.defaultIfEmpty(sgr.getCurrentStudiesInformations().getOtherStudiesLabel(),
             translationService.translate("sgr.property.currentStudies."
            + sgr.getCurrentStudiesInformations().getCurrentStudiesDiploma().toString(), Locale.FRANCE)));
         model.put("currentStudiesLevel",
             translationService.translate("sgr.property.currentStudiesLevel."
             + sgr.getCurrentStudiesInformations().getCurrentStudiesLevel().toString(), Locale.FRANCE));
         model.put("sandwichCourses", sgr.getCurrentStudiesInformations().getSandwichCourses());
         model.put("abroadInternship", sgr.getCurrentStudiesInformations().getAbroadInternship());
         model.put("abroadInternshipStartDate",
             formatDate(sgr.getCurrentStudiesInformations().getAbroadInternshipStartDate()));
         model.put("abroadInternshipEndDate",
                 formatDate(sgr.getCurrentStudiesInformations().getAbroadInternshipEndDate()));
         model.put("currentSchoolName",
             StringUtils.defaultIfEmpty(sgr.getCurrentSchool().getCurrentSchoolNamePrecision(),
             sgr.getCurrentSchool().getCurrentSchoolNameArray().length == 0 ? "" :
             sgr.getCurrentSchool().getCurrentSchoolNameArray(0).getName()));
         model.put("currentSchoolPostalCode",
             StringUtils.defaultString(sgr.getCurrentSchool().getCurrentSchoolPostalCode()));
         model.put("currentSchoolCity",
             StringUtils.defaultString(sgr.getCurrentSchool().getCurrentSchoolCity()));
         model.put("currentSchoolCountry", sgr.getCurrentSchool().getCurrentSchoolCountry() != null ?
             translationService.translate("sgr.property.currentSchoolCountry."
             + sgr.getCurrentSchool().getCurrentSchoolCountry()) : "");
         model.put("abroadInternshipSchoolName", sgr.getCurrentStudiesInformations().getAbroadInternship() ?
             sgr.getCurrentStudiesInformations().getAbroadInternshipSchoolName() : "");
         model.put("abroadInternshipSchoolCountry", sgr.getCurrentStudiesInformations().getAbroadInternship() ?
             translationService.translate("sgr.property.abroadInternshipSchoolCountry."
             + sgr.getCurrentStudiesInformations().getAbroadInternshipSchoolCountry()) : "");
         model.put("distance",
             translationService.translate("sgr.property.distance."
             + sgr.getDistance().toString(), Locale.FRANCE));
         List<Map<String, String>> documents = new ArrayList<Map<String, String>>();
         model.put("documents", documents);
         try {
             for (RequestDocument requestDoc : requestService.getAssociatedDocuments(sgr.getId())) {
                 Document document = documentService.getById(requestDoc.getDocumentId());
                 for (String documentTypeToSend : documentTypesToSend) {
                     if (documentTypeToSend.equals(document.getDocumentType().getType().toString())) {
                         int i = 1;
                         for (DocumentBinary documentBinary : document.getDatas()) {
                             Map<String, String> doc = new HashMap<String, String>();
                             documents.add(doc);
                             String filename = org.springframework.util.StringUtils.arrayToDelimitedString(
                                 new String[] {
                                     "CapDemat", document.getDocumentType().getName(),
                                     String.valueOf(sgr.getId()), String.valueOf(i++)
                                 }, "-");
                             doc.put("filename", filename);
                             if (IDocumentTypeService.BANK_IDENTITY_RECEIPT_TYPE.equals(
                                 document.getDocumentType().getType())) {
                                 doc.put("label", "RIB");
                             } else if (IDocumentTypeService.SCHOOL_CERTIFICATE_TYPE.equals(
                                 document.getDocumentType().getType())) {
                                 doc.put("label", "Certificat d'inscription");
                             } else if (IDocumentTypeService.REVENUE_TAXES_NOTIFICATION_TWO_YEARS_AGO.equals(
                                 document.getDocumentType().getType())) {
                                 doc.put("label", "Avis d'imposition");
                             } else {
                                 // should never happen
                                 doc.put("label", document.getDocumentType().getName());
                             }
                             try {
                                 doc.put("remotePath", uploader.upload(filename, documentBinary.getData()));
                             } catch (JSchException e) {
                                 addTrace(sgr.getId(), null, TraceStatusEnum.ERROR, "Erreur à l'envoi d'une pièce jointe");
                             } catch (SftpException e) {
                                 addTrace(sgr.getId(), null, TraceStatusEnum.ERROR, "Erreur à l'envoi d'une pièce jointe");
                             }
                         }
                         break;
                     }
                 }
             }
             model.put("email",
                 StringUtils.defaultIfEmpty(sgr.getSubjectInformations().getSubjectEmail(),
                 homeFolderService.getHomeFolderResponsible(sgr.getHomeFolder().getId()).getEmail()));
             model.put("taxHouseholdCityPrecision",
                 StringUtils.defaultString(sgr.getTaxHouseholdCityPrecision()));
             model.put("msStatut", firstSending ? "" :
                 getRequestStatus(sgr, psCodeTiers));
             model.put("millesime", firstSending ? "" :
                 parseData(requestData, "//donneesDemande/Demande/miMillesime"));
             model.put("msCodext", firstSending ? "" :
                 parseData(requestData, "//donneesDemande/Demande/msCodext"));
             model.put("requestTypeCode",
                 parseData(edemandeClient.chargerTypeDemande().getChargerTypeDemandeResponse().getReturn(), "//typeDemande/code"));
             EnregistrerValiderFormulaireResponseDocument
                 enregistrerValiderFormulaireResponseDocument =
                     edemandeClient.enregistrerValiderFormulaire(escapeStrings(model));
             if (!"0".equals(parseData(enregistrerValiderFormulaireResponseDocument.getEnregistrerValiderFormulaireResponse().getReturn(), "//Retour/codeRetour"))) {
                 addTrace(sgr.getId(), null, TraceStatusEnum.ERROR, parseData(enregistrerValiderFormulaireResponseDocument.getEnregistrerValiderFormulaireResponse().getReturn(), "//Retour/messageRetour"));
             } else {
                 addTrace(sgr.getId(), null, TraceStatusEnum.SENT, "Demande transmise");
             }
         } catch (CvqException e) {
             e.printStackTrace();
             addTrace(sgr.getId(), null, TraceStatusEnum.NOT_SENT, e.getMessage());
         }
     }
 
     private String searchRequest(StudyGrantRequest sgr, String psCodeTiers) {
         try {
             return parseData(edemandeClient.rechercheDemandesTiers(psCodeTiers)
                 .getRechercheDemandesTiersResponse().getReturn(),
                 "//resultatRechDemandes/listeDemandes/Demande/moOrigineApsect[msIdentifiant ='"
                 + buildExternalRequestId(sgr) + "']/../miCode");
         } catch (CvqException e) {
             addTrace(sgr.getId(), null, TraceStatusEnum.NOT_SENT, e.getMessage());
             return null;
         }
     }
 
     private String getRequestStatus(StudyGrantRequest sgr, String psCodeTiers) {
         try {
             if (sgr.getEdemandeId() == null || sgr.getEdemandeId().trim().isEmpty()) {
                 return parseData(edemandeClient.rechercheDemandesTiers(psCodeTiers)
                     .getRechercheDemandesTiersResponse().getReturn(),
                     "//resultatRechDemandes/listeDemandes/Demande/moOrigineApsect[msIdentifiant ='"
                         + buildExternalRequestId(sgr) + "']/../msStatut");
             } else {
                 return parseData(edemandeClient.chargerDemande(psCodeTiers, sgr.getEdemandeId())
                         .getChargerDemandeResponse().getReturn(),
                         "//donneesDemande/Demande/msStatut");
             }
         } catch (CvqException e) {
             addTrace(sgr.getId(), null, TraceStatusEnum.NOT_SENT, e.getMessage());
             return null;
         }
     }
 
     public List<String> checkExternalReferential(final XmlObject requestXml) {
         StudyGrantRequest sgr = ((StudyGrantRequestDocument) requestXml).getStudyGrantRequest();
         List<String> result = new ArrayList<String>();
         try {
             String postalCodeAndCityCheck = edemandeClient.existenceCommunePostale(sgr.getSubjectInformations().getSubjectAddress().getPostalCode(), sgr.getSubjectInformations().getSubjectAddress().getCity()).getExistenceCommunePostaleResponse().getReturn();
             if (!"0".equals(parseData(postalCodeAndCityCheck, "//FluxWebService/msCodeRet"))) {
                 result.add(parseData(postalCodeAndCityCheck, "//FluxWebService/erreur/message"));
             }
             String bankInformationsCheck = edemandeClient.verifierRIB(sgr.getBankCode(), sgr.getCounterCode(), sgr.getAccountNumber(), sgr.getAccountKey()).getVerifierRIBResponse().getReturn();
             if (!"0".equals(parseData(bankInformationsCheck, "//FluxWebService/msCodeRet"))) {
                 result.add(parseData(bankInformationsCheck, "//FluxWebService/erreur/message"));
             }
         } catch (CvqException e) {
             result.add("Impossible de contacter Edemande");
         }
         return result;
     }
 
     public Map<String, Object> loadExternalInformations(XmlObject requestXml)
         throws CvqException {
         StudyGrantRequest sgr = ((StudyGrantRequestDocument) requestXml).getStudyGrantRequest();
         if (sgr.getSubject().getIndividual().getExternalId() == null
             || sgr.getSubject().getIndividual().getExternalId().trim().isEmpty()
             || sgr.getEdemandeId() == null || sgr.getEdemandeId().trim().isEmpty()) {
             return Collections.emptyMap();
         }
         Map<String, Object> informations = new TreeMap<String, Object>();
         String request = edemandeClient.chargerDemande(
             sgr.getSubject().getIndividual().getExternalId(), sgr.getEdemandeId())
             .getChargerDemandeResponse().getReturn();
         String status = getRequestStatus(sgr, sgr.getSubject().getIndividual().getExternalId());
         if (status != null && !status.trim().isEmpty()) {
             informations.put("sgr.property.externalStatus", status);
         }
         String grantedAmount = parseData(request, "//donneesDemande/Demande/mdMtAccorde");
         if (grantedAmount != null && !grantedAmount.trim().isEmpty()) {
             informations.put("sgr.property.grantedAmount",
                 new DecimalFormat(translationService.translate("format.currency"))
                 .format(new BigDecimal(grantedAmount)));
         }
         String paidAmount = parseData(request, "//donneesDemande/Demande/mdMtRealise");
         if (paidAmount != null && !paidAmount.trim().isEmpty()) {
             informations.put("sgr.property.paidAmount",
                 new DecimalFormat(translationService.translate("format.currency"))
                 .format(new BigDecimal(paidAmount)));
         }
         return informations;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public void setEdemandeClient(IEdemandeClient edemandeClient) {
         this.edemandeClient = edemandeClient;
     }
 
     public boolean supportsConsumptions() {
         return false;
     }
 
     public boolean handlesTraces() {
         return true;
     }
 
     private String parseData(String returnElement, String path)
         throws CvqException {
         try {
             return new DOMXPath(path)
                 .stringValueOf(
                     DocumentBuilderFactory.newInstance().newDocumentBuilder()
                         .parse(new InputSource(new StringReader(returnElement)))
                         .getDocumentElement());
         } catch (JaxenException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         } catch (SAXException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         } catch (IOException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         }
     }
 
     /**
      * Same as {@link #parseData(String, String)}
      * but selects all matching elements
      */
     private List<?> parseDatas(String returnElement, String path)
         throws CvqException {
         try {
             return new DOMXPath(path)
                 .selectNodes(
                     DocumentBuilderFactory.newInstance().newDocumentBuilder()
                         .parse(new InputSource(new StringReader(returnElement)))
                         .getDocumentElement());
         } catch (JaxenException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         } catch (SAXException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         } catch (IOException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
             throw new CvqException("Erreur lors de la lecture de la réponse du service externe");
         }
     }
 
     private String buildExternalRequestId(StudyGrantRequest sgr) {
         return org.springframework.util.StringUtils.arrayToDelimitedString(
             new String[] {
                 "CapDemat",
                 new SimpleDateFormat("yyyyMMdd").format(new Date(sgr.getCreationDate().getTimeInMillis())),
                 String.valueOf(sgr.getId())
             },
             "-");
     }
 
     /**
      * Whether or not we have to send the request.
      * 
      * @return true if the request has no SENT trace (it has never been successfully sent)
      * or it has an error trace and no Edemande ID (it was sent and received, but rejected and must be sent as new)
      */
     private boolean mustSendNewRequest(StudyGrantRequest sgr) {
         Set<Critere> criteriaSet = new HashSet<Critere>(3);
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
             String.valueOf(sgr.getId()), Critere.EQUALS));
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_NAME,
             label, Critere.EQUALS));
         criteriaSet.add(new Critere(
             ExternalServiceTrace.SEARCH_BY_STATUS, TraceStatusEnum.SENT,
             Critere.EQUALS));
         if (externalService.getTraces(criteriaSet, null, null).isEmpty())
             return true;
         criteriaSet.clear();
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
             String.valueOf(sgr.getId()), Critere.EQUALS));
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_NAME,
             label, Critere.EQUALS));
         criteriaSet.add(new Critere(
             ExternalServiceTrace.SEARCH_BY_STATUS, TraceStatusEnum.ERROR,
             Critere.EQUALS));
         return (!externalService.getTraces(criteriaSet, null, null).isEmpty()
             && (sgr.getEdemandeId() == null || sgr.getEdemandeId().trim().isEmpty()));
     }
 
     /**
      * Whether or not we have to resend the request.
      * 
      * @return true if the request has an Edemande ID (so it was already sent),
      * and an ERROR trace not followed by a SENT trace
      */
     private boolean mustResendRequest(StudyGrantRequest sgr) {
         Set<Critere> criteriaSet = new HashSet<Critere>(3);
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
             String.valueOf(sgr.getId()), Critere.EQUALS));
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_NAME,
             label, Critere.EQUALS));
         criteriaSet.add(new Critere(
             ExternalServiceTrace.SEARCH_BY_STATUS, TraceStatusEnum.ERROR,
             Critere.EQUALS));
         if (externalService.getTraces(criteriaSet, null, null).isEmpty()
             || sgr.getEdemandeId() == null || sgr.getEdemandeId().trim().isEmpty()) {
             return false;
         }
         Set<Critere> criteres = new HashSet<Critere>();
         criteres.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
             String.valueOf(sgr.getId()), Critere.EQUALS));
         criteres.add(new Critere(ExternalServiceTrace.SEARCH_BY_NAME, label,
             Critere.EQUALS));
         for (ExternalServiceTrace est : externalService.getTraces(criteres,
             ExternalServiceTrace.SEARCH_BY_DATE, "desc")) {
             if (TraceStatusEnum.SENT.equals(est.getStatus())) {
                 return false;
             } else if (TraceStatusEnum.ERROR.equals(est.getStatus())) {
                 return true;
             }
         }
         // we should never execute the next line :
         // the above loop should have found a SENT trace and returned false,
         // or found the ERROR trace that IS in the list
         // (otherwise the first test would have succeded)
         // however, for compilation issues (and an hypothetic concurrent traces deletion)
         // we return false, to do nothing rather than doing something wrong
         return false;
     }
 
     /**
      * Determines if we must send an individual creation request for the request's subject
      * or account holder when this individual has no psCodeTiers yet.
      */
     private boolean mustCreateIndividual(StudyGrantRequest sgr, String subkey) {
         Set<Critere> criteriaSet = new HashSet<Critere>(4);
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
             String.valueOf(sgr.getId()), Critere.EQUALS));
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_SUBKEY,
             subkey, Critere.EQUALS));
         criteriaSet.add(new Critere(ExternalServiceTrace.SEARCH_BY_NAME,
             label, Critere.EQUALS));
         criteriaSet.add(new Critere(
             ExternalServiceTrace.SEARCH_BY_STATUS, TraceStatusEnum.IN_PROGRESS,
             Critere.EQUALS));
         if (externalService.getTraces(criteriaSet, null, null).isEmpty()) {
             return true;
         }
         Set<Critere> criteres = new HashSet<Critere>();
         criteres.add(new Critere(ExternalServiceTrace.SEARCH_BY_KEY,
             String.valueOf(sgr.getId()), Critere.EQUALS));
         criteres.add(new Critere(ExternalServiceTrace.SEARCH_BY_SUBKEY, subkey,
             Critere.EQUALS));
         criteres.add(new Critere(ExternalServiceTrace.SEARCH_BY_NAME, label,
             Critere.EQUALS));
         for (ExternalServiceTrace est : externalService.getTraces(criteres,
             ExternalServiceTrace.SEARCH_BY_DATE, "desc")) {
             if (TraceStatusEnum.IN_PROGRESS.equals(est.getStatus())) {
                 return false;
             } else if (TraceStatusEnum.ERROR.equals(est.getStatus())) {
                 return true;
             }
         }
         return false;
     }
 
     private String formatDate(Calendar calendar) {
         if (calendar == null) return "";
         return formatter.format(new Date(calendar.getTimeInMillis()));
     }
 
     private boolean mustCreateAccountHolder(StudyGrantRequest sgr) {
         return mustCreateIndividual(sgr, ACCOUNT_HOLDER_TRACE_SUBKEY);
     }
 
     private boolean mustCreateSubject(StudyGrantRequest sgr) {
         return mustCreateIndividual(sgr, SUBJECT_TRACE_SUBKEY);
     }
 
     private Map<String, Object> escapeStrings(Map<String, Object> model) {
         for (Map.Entry<String, Object> entry : model.entrySet()) {
             if (entry.getValue() instanceof String) {
                 entry.setValue(
                     StringEscapeUtils.escapeXml((String)entry.getValue()));
             } else if (entry.getValue() instanceof AddressType) {
                 AddressType addressType = (AddressType)entry.getValue();
                 Map<String, String> address = new HashMap<String, String>();
                 address.put("additionalDeliveryInformation",
                     StringEscapeUtils.escapeXml(
                         addressType.getAdditionalDeliveryInformation()));
                 address.put("additionalGeographicalInformation",
                     StringEscapeUtils.escapeXml(
                         addressType.getAdditionalGeographicalInformation()));
                 address.put("city",
                     StringEscapeUtils.escapeXml(addressType.getCity()));
                 address.put("countryName",
                     StringEscapeUtils.escapeXml(addressType.getCountryName()));
                 address.put("placeNameOrService",
                     StringEscapeUtils.escapeXml(
                         addressType.getPlaceNameOrService()));
                 address.put("postalCode",
                     StringEscapeUtils.escapeXml(addressType.getPostalCode()));
                 address.put("streetName",
                     StringEscapeUtils.escapeXml(addressType.getStreetName()));
                 address.put("streetNumber",
                     StringEscapeUtils.escapeXml(addressType.getStreetNumber()));
                 entry.setValue(address);
             }
         }
         return model;
     }
 
     @Override
     public void checkConfiguration(ExternalServiceBean externalServiceBean)
         throws CvqConfigurationException {
         List<String> documentTypesToSend =
             (List<String>)externalServiceBean.getProperty("documentTypesToSend");
         if (documentTypesToSend != null) {
             this.documentTypesToSend = documentTypesToSend;
         }
     }
 
     @Override
     public void creditHomeFolderAccounts(Collection<PurchaseItem> purchaseItems,
             String cvqReference, String bankReference, Long homeFolderId,
             String externalHomeFolderId, String externalId, Date validationDate)
             throws CvqException {
     }
 
     @Override
     public Map<String, List<ExternalAccountItem>> getAccountsByHomeFolder(Long homeFolderId,
             String externalHomeFolderId, String externalId) throws CvqException {
         return null;
     }
 
     @Override
     public Map<Date, String> getConsumptionsByRequest(Request request, Date dateFrom, Date dateTo)
             throws CvqException {
         return null;
     }
 
     @Override
     public Map<Individual, Map<String, String>> getIndividualAccountsInformation(Long homeFolderId,
             String externalHomeFolderId, String externalId) throws CvqException {
         return null;
     }
 
     @Override
     public String getLabel() {
         return label;
     }
 
     @Override
     public String helloWorld() throws CvqException {
         return null;
     }
 
     @Override
     public void loadDepositAccountDetails(ExternalDepositAccountItem edai) throws CvqException {
     }
 
     @Override
     public void loadInvoiceDetails(ExternalInvoiceItem eii) throws CvqException {
     }
 
     public void setRequestService(IStudyGrantRequestService requestService) {
         this.requestService = requestService;
     }
 
     public void setDocumentService(IDocumentService documentService) {
         this.documentService = documentService;
     }
 
     public void setExternalService(IExternalService externalService) {
         this.externalService = externalService;
     }
 
     public void setRequestWorkflowService(IRequestWorkflowService requestWorkflowService) {
         this.requestWorkflowService = requestWorkflowService;
     }
 
     public void setTranslationService(ITranslationService translationService) {
         this.translationService = translationService;
     }
 
     public void setUploader(EdemandeUploader uploader) {
         this.uploader = uploader;
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = (ListableBeanFactory)beanFactory;
     }
 }
