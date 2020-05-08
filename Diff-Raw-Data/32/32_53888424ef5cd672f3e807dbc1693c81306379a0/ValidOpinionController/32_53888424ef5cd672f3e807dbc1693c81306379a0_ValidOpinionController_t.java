 package org.esupportail.opi.web.controllers.opinions;
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.BusinessUtil;
 import org.esupportail.opi.domain.beans.parameters.*;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.ContactCommission;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.services.mails.MailContentService;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.parameters.FormationContinue;
 import org.esupportail.opi.web.beans.parameters.FormationInitiale;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.AdressePojo;
 import org.esupportail.opi.web.beans.pojo.CommissionPojo;
 import org.esupportail.opi.web.beans.pojo.IndVoeuPojo;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.opi.web.controllers.references.CommissionController;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
import static fj.data.Option.some;
 import static fj.function.Booleans.not;
 import static org.esupportail.opi.web.utils.DTOs.commissionDTO;
 import static org.esupportail.opi.web.utils.fj.Predicates.typeTrtEquals;
 
 
 public class ValidOpinionController extends AbstractContextAwareController {
     /**
      * the serialization id.
      */
     private static final long serialVersionUID = 1651952641047805731L;
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
     /**
      * default false.
      * if gest FC, must true to valide opinions
      */
     private Boolean valideFC;
 
     /**
      * default true.
      * can be false if gest FC
      */
     private Boolean sendMail;
 
     /**
      * see {@link PrintOpinionController}.
      */
     private PrintOpinionController printOpinionController;
 
     /**
      * see {@link Preselection}.
      */
     private Preselection preselection;
 
     /**
      * see {@link InscriptionAdm}.
      */
     private InscriptionAdm inscriptionAdm;
 
     /**
      * see {@link Refused}.
      */
     private Refused refused;
 
     /**
      * see {@link ListeComplementaire}.
      */
     private ListeComplementaire listeComplementaire;
 
     /**
      * see {@link Intermediary}.
      */
     private Intermediary intermediary;
 
     /**
      * The wish selected for test send Mail.
      */
     private IndVoeuPojo indVoeuPojo;
 
     /**
      * mail send to contact FC when valide FC candidates.
      */
     private MailContentService infoValidWishesFC;
 
     /**
      * {@link CommissionController}.
      */
     private CommissionController commissionController;
 
     public ValidOpinionController() {
         super();
     }
 
 
     @Override
     public void reset() {
         super.reset();
         valideFC = false;
         sendMail = true;
     }
 
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
         Assert.notNull(this.printOpinionController,
                 "property printOpinionController of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.listeComplementaire,
                 "property listeComplementaire of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.refused,
                 "property refused of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.inscriptionAdm,
                 "property inscriptionAdm of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.preselection,
                 "property preselection of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.intermediary,
                 "property preselection of class " + this.getClass().getName()
                         + " can not be null");
         reset();
     }
 
     public String goValidOpinions() {
         reset();
         this.printOpinionController.reset();
         return NavigationRulesConst.DISPLAY_VALID_OPINIONS;
     }
 
     /**
      * Callback to the list of student for the selected commission.
      */
     public String goSeeStudientForCommission() {
         // list of indivius from the commission selected
         // with an opinion not validate
         printOpinionController.initIndividus();
         return NavigationRulesConst.DISPLAY_NOT_VALIDATED_STUDENT;
     }
 
     /**
      * Callback for the print of opinions.
      */
     public String goPrintOpinions() {
         reset();
         this.printOpinionController.setListTypeOpinions();
         return NavigationRulesConst.DISPLAY_PRINT_OPINIONS;
     }
 
     /**
      * Send Mail to a student.
      */
     public void sendMail(final IndividuPojo indPojo,
                          final Set<Avis> avis,
                          final MailContentService mailContentService,
                          final CommissionPojo currentCmiPojo,
                          final Boolean sendToIndividu,
                          final String sendToMail) {
 
         // hibernate session reattachment
         Individu ind = indPojo.getIndividu();
         ind = getDomainService().getIndividu(
                 ind.getNumDossierOpi(), ind.getDateNaissance());
 
         List<Object> list = new ArrayList<>();
         list.add(ind);
         list.add(avis);
         list.add(commissionDTO(currentCmiPojo));
         Campagne camp = null;
 
         for (Campagne c : ind.getCampagnes()) {
             if (c.getTemoinEnService()) {
                 camp = c;
             }
         }
         if (camp != null) {
             list.add(camp);
         }
 
         if (sendToIndividu) {
             // send mail to the individu
             if (ind.getEmailAnnuaire() != null) {
                 mailContentService.send(ind.getAdressMail(),
                         ind.getEmailAnnuaire(), list);
             } else {
                 mailContentService.send(ind.getAdressMail(),
                         null, list);
             }
         } else if (sendToMail == null || sendToMail.isEmpty()) {
             String mail = currentCmiPojo.getAdressePojo().getAdresse().getMail();
             mailContentService.send(mail, list);
         } else {
             mailContentService.send(sendToMail, list);
         }
     }
 
 
     /**
      * Send one mail to commission of indVoeuPojo.
      */
     public void sendOneMail() {
         sendOneMailToCandidatOrCommission(false);
     }
 
     /**
      * Send one mail to candidat of indVoeuPojo.
      */
     public void sendOneMailCandidat() {
         sendOneMailToCandidatOrCommission(true);
     }
 
     /**
      * Send one mail to candidat or to commission of indVoeuPojo.
      */
     private void sendOneMailToCandidatOrCommission(final boolean envCandidat) {
         // récupération du régime d'inscription  du gestionnaire
         final Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         final int codeRI = gest.getProfile().getCodeRI();
         final RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
 
         final Set<Avis> a = new HashSet<>();
         final Avis av = indVoeuPojo.getAvisEnService();
         a.add(av);
 
         final MailContentService mail =
                 (regimeIns.getMailTypeConvoc() != null) ?
                         regimeIns.getMailContentServiceTypeConvoc(
                                 BusinessUtil.getTypeConvocation(
                                         getParameterService().getTypeConvocations(),
                                         av.getResult().getCodeTypeConvocation()), av.getAppel()) :
                         null;
 
         final Commission c = getParameterService().getCommission(
                 printOpinionController.getIndividuController().getIndividuPaginator().getIndRechPojo().getIdCmi(),
                 null);
         final ContactCommission cc = c.getContactsCommission().get(regimeIns.getCode());
         final CommissionPojo currentCmiPojo = new CommissionPojo(c,
                 new AdressePojo(cc.getAdresse(), getDomainApoService()), cc);
 
         if (mail != null) {
             sendMail(printOpinionController.getIndividuPojoSelected(), a,
                     mail, currentCmiPojo, envCandidat, null);
 
             String mailDestination;
             if (envCandidat) {
                 if (printOpinionController.getIndividuPojoSelected().
                         getIndividu().getEmailAnnuaire() != null) {
                     mailDestination = printOpinionController.getIndividuPojoSelected().
                             getIndividu().getEmailAnnuaire();
                 } else {
                     mailDestination = printOpinionController.getIndividuPojoSelected().
                             getIndividu().getAdressMail();
                 }
             } else {
                 mailDestination = currentCmiPojo.getAdressePojo().getAdresse().getMail();
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("Envoi du mail à : " + mailDestination);
             }
             addInfoMessage(null, "OPINION.PRINT.INFO.MAIL_FOR_CMI", mailDestination);
         }
 
         //REINIT
         indVoeuPojo = null;
         printOpinionController.setIndividuPojoSelected(null);
     }
 
 
     /**
      * Validate the students for all commissions selected by gestionnaire.
      */
     public String validateStudentsForCommissions() {
         if (log.isDebugEnabled()) {
             log.debug("entering validateStudentsForCommissions()");
         }
 
         printOpinionController.getPdfData().clear();
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         int codeRI = gest.getProfile().getCodeRI();
         RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
 
         if (regimeIns instanceof FormationContinue && !valideFC) {
             printOpinionController.getActionEnum().setWhatAction(ActionEnum.SEND_MAIL);
         } else {
             if (printOpinionController.getCommissionsSelected().length != 0) {
                 for (Object c : printOpinionController.getCommissionsSelected()) {
                     Commission laCommission = (Commission) c;
                     validateStudentsForTheCommission(laCommission);
                 }
             } else if (printOpinionController.getAllChecked()) {
                 List<Commission> c = commissionController.getCommissionsItemsByRight();
                 for (Commission laCommission : c) {
                     validateStudentsForTheCommission(laCommission);
                 }
             }
             if (!printOpinionController.getPdfData().isEmpty()) {
                 printOpinionController.getActionEnum().setWhatAction(ActionEnum.CONFIRM_ACTION);
             } else {
                 printOpinionController.getActionEnum().setWhatAction(ActionEnum.EMPTY_ACTION);
             }
             printOpinionController.setResultSelected(new Object[0]);
             printOpinionController.setCommissionsSelected(new Object[0]);
             printOpinionController.setAllChecked(false);
         }
         return NavigationRulesConst.DISPLAY_VALID_OPINIONS;
     }
 
 
     /**
      * Validate the students for one commission passed by param.
      */
     private void validateStudentsForTheCommission(
             final Commission laCommission) {
         if (log.isDebugEnabled()) {
             log.debug("entering validateStudentsForTheCommission() " + laCommission.toString());
         }
         // hibernate session reattachment
         Commission com = getParameterService().getCommission(laCommission.getId(), laCommission.getCode());
 
         // témoin par défaut à false pour l'envoie de mail informant le gestionnaire FC
         // de la validation d'un de leurs candidats
         boolean sendMailValideFC = false;
 
         // récupération du régime d'inscription  du gestionnaire
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         int codeRI = gest.getProfile().getCodeRI();
         RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
         final List<IndividuPojo> individus = new ArrayList<>(
                printOpinionController.getIndividus(com, some(false), not(typeTrtEquals(printOpinionController.getTransfert())))
                 .toCollection());
         for (IndividuPojo ind : individus) {
             Set<Avis> avisFavorable = new HashSet<>();
             Set<Avis> avisFavorableAppel = new HashSet<>();
             Set<Avis> avisDefavorable = new HashSet<>();
             Set<Avis> avisDefavorableAppel = new HashSet<>();
             Set<Avis> avisPreselection = new HashSet<>();
             Set<Avis> avisLC = new HashSet<>();
             Set<Avis> autresAvis = new HashSet<>();
             for (IndVoeuPojo indVPojo : ind.getIndVoeuxPojo()) {
                 Avis a = indVPojo.getAvisEnService();
                 // 1 - Update the database
                 a.setValidation(true);
                 a = getDomainService().update(a, getCurrentGest().getLogin());
                 getDomainService().updateAvis(a);
                 // 2 - Sort the type of avis
                 if (a.getResult().getIsFinal()
                         && a.getResult().getCodeTypeConvocation()
                         .equals(inscriptionAdm.getCode())) {
                     if (!a.getAppel()) {
                         avisFavorable.add(a);
                     } else {
                         avisFavorableAppel.add(a);
                     }
                 } else {
                     if (a.getResult().getIsFinal()
                             && a.getResult().getCodeTypeConvocation()
                             .equals(refused.getCode())) {
                         if (!a.getAppel()) {
                             avisDefavorable.add(a);
                         } else {
                             avisDefavorableAppel.add(a);
                         }
                     } else {
                         if (a.getResult().getCodeTypeConvocation()
                                 .equals(preselection.getCode())) {
                             avisPreselection.add(a);
                         } else {
                             if (a.getResult().getCodeTypeConvocation()
                                     .equals(listeComplementaire.getCode())) {
                                 avisLC.add(a);
                             } else {
                                 if (a.getResult().getCodeTypeConvocation()
                                         .equals(intermediary.getCode())) {
                                     autresAvis.add(a);
                                 }
                             }
                         }
                     }
                 }
             }
             // 4 - Send Mails
             if (sendMail && regimeIns.getMailTypeConvoc() != null) {
                 CommissionPojo currentCmiPojo = new CommissionPojo(com,
                         new AdressePojo(com.getContactsCommission()
                                 .get(codeRI).getAdresse(),
                                 getDomainApoService()),
                         com.getContactsCommission()
                                 .get(codeRI));
                 if (!avisFavorable.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             inscriptionAdm, false);
                     if (mail != null) {
                         sendMail(ind, avisFavorable, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisDefavorable.isEmpty()) {
                     MailContentService mail = regimeIns.
                             getMailContentServiceTypeConvoc(refused, false);
 
                     if (mail != null) {
                         sendMail(ind, avisDefavorable, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisFavorableAppel.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             inscriptionAdm, true);
 
                     if (mail != null) {
                         sendMail(ind, avisFavorableAppel, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisDefavorableAppel.isEmpty()) {
                     MailContentService mail = regimeIns.
                             getMailContentServiceTypeConvoc(refused, true);
 
                     if (mail != null) {
                         sendMail(ind, avisDefavorableAppel, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisPreselection.isEmpty()) {
                     MailContentService mail = regimeIns.
                             getMailContentServiceTypeConvoc(preselection, null);
 
                     if (mail != null) {
                         sendMail(ind, avisPreselection, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisLC.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             listeComplementaire, null);
 
                     if (mail != null) {
                         sendMail(ind, avisLC, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!autresAvis.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             intermediary, null);
 
                     if (mail != null) {
                         sendMail(ind, autresAvis, mail, currentCmiPojo, true, null);
                     }
                 }
             }
             // si le candidat validé est du régime FC alors que le gestionnaire est FI,
             // on passe le témoin d'envoie de mail au gestionnaire FC de la commission
             if (codeRI == FormationInitiale.CODE
                     && ind.getCampagneEnServ(getDomainService()).getCodeRI() == FormationContinue.CODE) {
                 sendMailValideFC = true;
             }
         }
 
         if (sendMailValideFC) {
             ContactCommission contactCommission =
                     com.getContactsCommission().get(FormationContinue.CODE);
             if (contactCommission != null && contactCommission.getAdresse() != null) {
                 List<Object> list = new ArrayList<>();
                 list.add(com);
                 infoValidWishesFC.send(contactCommission.getAdresse().getMail(), list);
             }
         }
         // add data to pdfData
         printOpinionController.makePdfData(individus, com);
         if (sendMail) {
             addInfoMessage(null, "OPINION.VALIDATION_OK");
         } else {
             addInfoMessage(null, "OPINION.VALIDATION_OK.WITHOUT_MAIL");
         }
         reset();
     }
 
     public void setPrintOpinionController(
             final PrintOpinionController printOpinionController) {
         this.printOpinionController = printOpinionController;
     }
 
     public void setInscriptionAdm(final InscriptionAdm inscriptionAdm) {
         this.inscriptionAdm = inscriptionAdm;
     }
 
     public void setRefused(final Refused refused) {
         this.refused = refused;
     }
 
     public void setPreselection(final Preselection preselection) {
         this.preselection = preselection;
     }
 
     public void setListeComplementaire(final ListeComplementaire listeComplementaire) {
         this.listeComplementaire = listeComplementaire;
     }
 
     public void setIntermediary(final Intermediary intermediary) {
         this.intermediary = intermediary;
     }
 
     public IndVoeuPojo getIndVoeuPojo() {
         return indVoeuPojo;
     }
 
     public void setIndVoeuPojo(final IndVoeuPojo indVoeuPojo) {
         this.indVoeuPojo = indVoeuPojo;
     }
 
     public void setInfoValidWishesFC(final MailContentService infoValidWishesFC) {
         this.infoValidWishesFC = infoValidWishesFC;
     }
 
     public Boolean getValideFC() {
         return valideFC;
     }
 
     public void setValideFC(final Boolean valideFC) {
         this.valideFC = valideFC;
     }
 
     public void setCommissionController(final CommissionController commissionController) {
         this.commissionController = commissionController;
     }
 
     public Boolean getSendMail() {
         return sendMail;
     }
 
     public void setSendMail(final Boolean sendMail) {
         this.sendMail = sendMail;
     }
 
 }
