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
 
 
 /**
  * @author tducreux
  *         ValidOpinionController :
  */
 public class ValidOpinionController extends AbstractContextAwareController {    /*
     ******************* PROPERTIES ******************* */
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
    /*
 	 ******************* INIT ************************* */
 
 
     /**
      * Constructors.
      */
     public ValidOpinionController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         valideFC = false;
         sendMail = true;
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractContextAwareController#afterPropertiesSetInternal()
      */
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
 
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to the validation of opinions.
      *
      * @return String
      */
     public String goValidOpinions() {
         reset();
         this.printOpinionController.reset();
         return NavigationRulesConst.DISPLAY_VALID_OPINIONS;
     }
 
     /**
      * Callback to the list of student for the selected commission.
      *
      * @return String
      */
     public String goSeeStudientForCommission() {
         // list of indivius from the commission selected
         // with an opinion not validate
        this.printOpinionController.filterIndividuPojos(
                 commissionController.getCommission(),
                false, false);
         return NavigationRulesConst.DISPLAY_NOT_VALIDATED_STUDENT;
     }
 
     /**
      * Callback for the print of opinions.
      *
      * @return String
      */
     public String goPrintOpinions() {
         reset();
         this.printOpinionController.setListTypeOpinions();
         return NavigationRulesConst.DISPLAY_PRINT_OPINIONS;
     }
 
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * Send Mail to a student.
      *
      * @param i
      * @param a
      * @param mailContentService
      * @param currentCmiPojo
      * @param sendToIndividu
      * @param sendToMail
      */
     public void sendMail(final IndividuPojo i,
                          final Set<Avis> a,
                          final MailContentService mailContentService,
                          final CommissionPojo currentCmiPojo,
                          final Boolean sendToIndividu,
                          final String sendToMail) {
 
         // hibernate session reattachment
         Individu ind = i.getIndividu();
         ind = getDomainService().getIndividu(
                 ind.getNumDossierOpi(), ind.getDateNaissance());
 
         List<Object> list = new ArrayList<Object>();
         list.add(ind);
         list.add(a);
         list.add(currentCmiPojo);
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
             String mail = sendToMail;
             mailContentService.send(mail, list);
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
      *
      * @param envCandidat
      */
     private void sendOneMailToCandidatOrCommission(final boolean envCandidat) {
         // récupération du régime d'inscription  du gestionnaire
         final Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         final int codeRI = gest.getProfile().getCodeRI();
         final RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
 
         final Set<Avis> a = new HashSet<Avis>();
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
 
             String mailDestination = null;
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
      *
      * @return String
      */
     public String validateStudentsForCommissions() {
         if (log.isDebugEnabled()) {
             log.debug("entering validateStudentsForCommissions()");
         }
 
         this.printOpinionController.getPdfData().clear();
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         int codeRI = gest.getProfile().getCodeRI();
         RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
 
         if (regimeIns instanceof FormationContinue && !valideFC) {
             this.printOpinionController.getActionEnum().setWhatAction(ActionEnum.SEND_MAIL);
         } else {
             if (this.printOpinionController.getCommissionsSelected().length != 0) {
                 for (Object c : this.printOpinionController.getCommissionsSelected()) {
                     Commission laCommission = (Commission) c;
                     validateStudentsForTheCommission(laCommission);
                 }
             } else if (this.printOpinionController.getAllChecked()) {
                 List<Commission> c = commissionController.getCommissionsItemsByRight();
                 for (Commission laCommission : c) {
                     validateStudentsForTheCommission(laCommission);
                 }
             }
             if (!this.printOpinionController.getPdfData().isEmpty()) {
                 this.printOpinionController.getActionEnum().setWhatAction(ActionEnum.CONFIRM_ACTION);
             } else {
                 this.printOpinionController.getActionEnum().setWhatAction(ActionEnum.EMPTY_ACTION);
             }
             this.printOpinionController.setResultSelected(new Object[0]);
             this.printOpinionController.setCommissionsSelected(new Object[0]);
             this.printOpinionController.setAllChecked(false);
         }
         return NavigationRulesConst.DISPLAY_VALID_OPINIONS;
     }
 
 
     /**
      * Validate the students for one commission passed by param.
      *
      * @param laCommission
      */
     private void validateStudentsForTheCommission(
             final Commission laCommission) {
         if (log.isDebugEnabled()) {
             log.debug("entering validateStudentsForTheCommission() " + laCommission.toString());
         }
         // hibernate session reattachment
         Commission com = getParameterService().getCommission(
                 laCommission.getId(), laCommission.getCode());
 
         // témoin par défaut à false pour l'envoie de mail informant le gestionnaire FC
         // de la validation d'un de leurs candidats
         boolean sendMailValideFC = false;
 
         // récupération du régime d'inscription  du gestionnaire
         Gestionnaire gest = (Gestionnaire) getSessionController().getCurrentUser();
         int codeRI = gest.getProfile().getCodeRI();
         RegimeInscription regimeIns = getSessionController().getRegimeIns().get(codeRI);
 
         this.printOpinionController.lookForIndividusPojo(com, false, false, true);
         for (IndividuPojo i : this.printOpinionController.getLesIndividus()) {
             Set<Avis> avisFavorable = new HashSet<Avis>();
             Set<Avis> avisFavorableAppel = new HashSet<Avis>();
             Set<Avis> avisDefavorable = new HashSet<Avis>();
             Set<Avis> avisDefavorableAppel = new HashSet<Avis>();
             Set<Avis> avisPreselection = new HashSet<Avis>();
             Set<Avis> avisLC = new HashSet<Avis>();
             Set<Avis> autresAvis = new HashSet<Avis>();
             for (IndVoeuPojo indVPojo : i.getIndVoeuxPojo()) {
                 Avis a = indVPojo.getAvisEnService();
                 // 1 - Update the database
                 a.setValidation(true);
                 a = (Avis) getDomainService().update(a, getCurrentGest().getLogin());
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
                         sendMail(i, avisFavorable, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisDefavorable.isEmpty()) {
                     MailContentService mail = regimeIns.
                             getMailContentServiceTypeConvoc(refused, false);
 
                     if (mail != null) {
                         sendMail(i, avisDefavorable, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisFavorableAppel.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             inscriptionAdm, true);
 
                     if (mail != null) {
                         sendMail(i, avisFavorableAppel, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisDefavorableAppel.isEmpty()) {
                     MailContentService mail = regimeIns.
                             getMailContentServiceTypeConvoc(refused, true);
 
                     if (mail != null) {
                         sendMail(i, avisDefavorableAppel, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisPreselection.isEmpty()) {
                     MailContentService mail = regimeIns.
                             getMailContentServiceTypeConvoc(preselection, null);
 
                     if (mail != null) {
                         sendMail(i, avisPreselection, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!avisLC.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             listeComplementaire, null);
 
                     if (mail != null) {
                         sendMail(i, avisLC, mail, currentCmiPojo, true, null);
                     }
                 }
                 if (!autresAvis.isEmpty()) {
                     MailContentService mail = regimeIns.getMailContentServiceTypeConvoc(
                             intermediary, null);
 
                     if (mail != null) {
                         sendMail(i, autresAvis, mail, currentCmiPojo, true, null);
                     }
                 }
             }
             // si le candidat validé est du régime FC alors que le gestionnaire est FI,
             // on passe le témoin d'envoie de mail au gestionnaire FC de la commission
             if (codeRI == FormationInitiale.CODE
                     && i.getCampagneEnServ(getDomainService()).getCodeRI() == FormationContinue.CODE) {
                 sendMailValideFC = true;
             }
         }
 
         if (sendMailValideFC) {
             ContactCommission contactCommission =
                     com.getContactsCommission().get(FormationContinue.CODE);
             if (contactCommission != null && contactCommission.getAdresse() != null) {
                 List<Object> list = new ArrayList<Object>();
                 list.add(com);
                 infoValidWishesFC.send(contactCommission.getAdresse().getMail(), list);
             }
         }
         // add data to pdfData
         this.printOpinionController.makePdfData(this.printOpinionController.getLesIndividus(), com);
         if (sendMail) {
             addInfoMessage(null, "OPINION.VALIDATION_OK");
         } else {
             addInfoMessage(null, "OPINION.VALIDATION_OK.WITHOUT_MAIL");
         }
         reset();
     }
 
 
     /**
      * If sendToIndividu == false send the mail to the currentCmiPojo.
      * @param i
      * @param a
      * @param typeConvocation
      * @param currentCmiPojo
      */
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
     /**
      * @param printOpinionController the printOpinionController to set
      */
     public void setPrintOpinionController(
             final PrintOpinionController printOpinionController) {
         this.printOpinionController = printOpinionController;
     }
 
 
     /**
      * @param inscriptionAdm the inscriptionAdm to set
      */
     public void setInscriptionAdm(final InscriptionAdm inscriptionAdm) {
         this.inscriptionAdm = inscriptionAdm;
     }
 
     /**
      * @param refused the refused to set
      */
     public void setRefused(final Refused refused) {
         this.refused = refused;
     }
 
     /**
      * @param preselection the preselection to set
      */
     public void setPreselection(final Preselection preselection) {
         this.preselection = preselection;
     }
 
     /**
      * @param listeComplementaire the listeComplementaire to set
      */
     public void setListeComplementaire(final ListeComplementaire listeComplementaire) {
         this.listeComplementaire = listeComplementaire;
     }
 
     /**
      * @param intermediary the intermediary to set
      */
     public void setIntermediary(final Intermediary intermediary) {
         this.intermediary = intermediary;
     }
 
     /**
      * @return the indVoeuPojo
      */
     public IndVoeuPojo getIndVoeuPojo() {
         return indVoeuPojo;
     }
 
     /**
      * @param indVoeuPojo the indVoeuPojo to set
      */
     public void setIndVoeuPojo(final IndVoeuPojo indVoeuPojo) {
         this.indVoeuPojo = indVoeuPojo;
     }
 
     /**
      * @param infoValidWishesFC the infoValidWishesFC to set
      */
     public void setInfoValidWishesFC(final MailContentService infoValidWishesFC) {
         this.infoValidWishesFC = infoValidWishesFC;
     }
 
     /**
      * @return the valideFC
      */
     public Boolean getValideFC() {
         return valideFC;
     }
 
     /**
      * @param valideFC the valideFC to set
      */
     public void setValideFC(final Boolean valideFC) {
         this.valideFC = valideFC;
     }
 
     /**
      * @param commissionController the commissionController to set
      */
     public void setCommissionController(final CommissionController commissionController) {
         this.commissionController = commissionController;
     }
 
     /**
      * @return the sendMail
      */
     public Boolean getSendMail() {
         return sendMail;
     }
 
     /**
      * @param sendMail the sendMail to set
      */
     public void setSendMail(final Boolean sendMail) {
         this.sendMail = sendMail;
     }
 
 }
