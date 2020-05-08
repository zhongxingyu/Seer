 /**
  *
  */
 package org.esupportail.opi.web.controllers;
 
 
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.etat.EtatIndividu;
 import org.esupportail.opi.domain.beans.parameters.Nomenclature;
 import org.esupportail.opi.domain.beans.parameters.PieceJustificative;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.IndFormulaire;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.domain.beans.user.indcursus.CursusPro;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursus;
 import org.esupportail.opi.domain.beans.user.indcursus.QualifNonDiplomante;
 import org.esupportail.opi.domain.beans.user.situation.IndSituation;
 import org.esupportail.opi.domain.beans.user.situation.SituationSalarie;
 import org.esupportail.opi.services.export.CastorService;
 import org.esupportail.opi.services.export.ISerializationService;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.utils.Conversions;
 import org.esupportail.opi.web.beans.parameters.FormationContinue;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.*;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.PDFUtils;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.formation.FormationController;
 import org.esupportail.opi.web.controllers.formation.FormulairesController;
 import org.esupportail.opi.web.controllers.user.IndividuController;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 
 import javax.faces.context.FacesContext;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import static org.esupportail.opi.domain.beans.etat.EtatIndividu.EtatIncomplet;
 
 
 /**
  * @author cleprous
  *         <p/>
  *         AccueilController : gestion de l'accueil d'un candidat.
  */
 public class AccueilController extends AbstractAccessController {
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = -29141193796056578L;
 
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
 	
 	/*
      ******************* PROPERTIES ******************* */
     /**
      * Service to generate Xml.
      */
     private ISerializationService castorService;
 
     /**
      * {@link IndividuController}.
      */
     private IndividuController individuController;
 
     /**
      * {@link FormulairesController}.
      */
     private FormulairesController formulairesController;
 
     /**
      * {@link FormationController}.
      */
     private FormationController formationController;
 
     /**
      * Object contains the method to invoque.
      */
     private Object object;
 
     /**
      * Le context de deploiement de l'application.
      */
     private String context;
 
 	
 	/*
 	 ******************* INIT ************************* */
 
     /**
      * Constructors.
      */
     public AccueilController() {
         super();
 
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         Assert.notNull(this.castorService, "property castorService of class "
                 + this.getClass().getName() + " can not be null");
         Assert.notNull(this.individuController, "property individuController of class "
                 + this.getClass().getName() + " can not be null");
     }
 	
 	
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to logged in.
      *
      * @return String
      */
     public String goLoginCandidat() {
         IndividuPojo individu = getCurrentInd();
         if (individu == null) {
             // The numDossier has not been find
             addErrorMessage(null, "ERROR.ACTION.INDIVIDU_NOT_FOUND");
             return null;
         }
         if (individu.getEtat() == EtatIncomplet) {
             //on informe l'individu qu'il doit completer sur dossier avant de deposer de voeux
             if (!getSessionController().getCurrentInd()
                     .getRegimeInscription().getDisplayInfoFC()) {
                 addInfoMessage(null, "INFO.CANDIDAT.ETAT_INCOMPLET.1");
             } else {
                 addInfoMessage(null, "INFO.CANDIDAT.ETAT_INCOMPLET.1.FC");
             }
             addInfoMessage(null, "INFO.CANDIDAT.ETAT_INCOMPLET.2");
 
         }
         // initialisation de la situation de l'individu si FC
         if (individu.getRegimeInscription() instanceof FormationContinue) {
             individuController.getSituationController().setIndSituation(
                     getDomainService().getIndSituation(individu.getIndividu()));
         }
 
         return NavigationRulesConst.ACCUEIL_CANDIDAT;
     }
 
     /**
      * Callback to logged in.
      *
      * @return String
      */
     public String goWelcomeCandidat() {
         IndividuPojo individu = getCurrentInd();
         individu.setIndividu(getDomainService().getIndividu(
                 individu.getIndividu().getNumDossierOpi(),
                 individu.getIndividu().getDateNaissance()));
 
         if(!getSessionController().getCurrentInd().getIsManager()) {
 	        getSessionController().initCurrentInd(
 	                individu.getIndividu().getNumDossierOpi(),
 	                individu.getIndividu().getDateNaissance(),
 	                false,
 	                false);
 	        getCurrentInd();
         }
 
         if (getCurrentInd().getEtat() == EtatIncomplet) {
             //on informe l'individu qu'il doit completer sur dossier avant de deposer de voeux
             if (!getSessionController()
                     .getCurrentInd()
                     .getRegimeInscription()
                     .getDisplayInfoFC())
                 addInfoMessage(null, "INFO.CANDIDAT.ETAT_INCOMPLET.1");
             else
                 addInfoMessage(null, "INFO.CANDIDAT.ETAT_INCOMPLET.1.FC");
             addInfoMessage(null, "INFO.CANDIDAT.ETAT_INCOMPLET.2");
         }
         return NavigationRulesConst.ACCUEIL_CANDIDAT;
     }
 
     /**
      * @return null
      */
     public String verifyCursusScol() {
         if (individuController.getCursusController().isValidCursusScol(false)) {
             addInfoMessage(null, "INFO.CANDIDAT.OUBLIE.CURSUS_SCOL");
             return null;
         }
         return goWelcomeCandidat();
     }
 
     public String verifyCursusScolAndSave() {
         if (individuController.getCursusController().isValidCursusScol(false)) {
             addInfoMessage(null, "INFO.CANDIDAT.OUBLIE.CURSUS_SCOL");
             return null;
         }
         return individuController.goSaveDossier();
     }
 
     /**
      * @return null
      */
     public String verifyCursusPro() {
         if (individuController.getCursusController().isValidIndCursus(CursusPro.class, false)) {
             addInfoMessage(null, "INFO.CANDIDAT.OUBLIE.CURSUS_PRO");
             return null;
         }
         return goWelcomeCandidat();
     }
 
     /**
      * @return null
      */
     public String verifyQualif() {
         if (individuController.getCursusController().isValidIndCursus(QualifNonDiplomante.class, false)) {
             addInfoMessage(null, "INFO.CANDIDAT.OUBLIE.QUALIF");
             return null;
         }
         return goWelcomeCandidat();
     }
 	
 	
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * Method used to know if a dossier can be downloaded or an error has to be
      * displayed.
      *
      * @throws IOException
      */
     public void validatePrintDossier() throws IOException {
         if (getParameterService().isAllFormulairesCreated(
                 getSessionController().getCurrentInd().getIndividu(),
                 String.valueOf(getSessionController().getCurrentInd()
                         .getRegimeInscription().getCode()))) {
             printDossier();
         } else {
             addErrorMessage(null, "ERROR.ACTION.DOSSIER.DOWNLOAD");
             goDossierInvalide();
         }
     }
 
     /**
      * Print the candidate document.
      *
      * @throws IOException
      */
     public void printDossier() throws IOException {
         IndDocument indDocument = prepareIndDocument();
         makePDF(null, indDocument);
     }
 
     /**
      * Method used to go to the welcome page.
      *
      * @return String
      */
     private String goDossierInvalide() {
         return NavigationRulesConst.ACCUEIL_CANDIDAT;
     }
 
     /**
      * Method used to know if a dossier can be downloaded or an error has to be
      * displayed.
      *
      * @throws IOException
      */
    public void validatePrintOneDossier() throws IOException {
 
         Set<TraitementCmi> traitementsCmi = new HashSet<TraitementCmi>();
 
         for (SummaryWishesPojo sumWishPojo : formationController.getSummaryWishes()) {
             sumWishPojo.getVows();
             for (IndVoeuPojo indVoeuPojo : sumWishPojo.getVows()) {
                 traitementsCmi.add(indVoeuPojo.getIndVoeu().getLinkTrtCmiCamp()
                         .getTraitementCmi());
             }
         }
 
         if (getParameterService().isAllFormulairesCreatedByTraitementsCmi(
                 getSessionController().getCurrentInd().getIndividu(),
                 getSessionController().getCurrentInd()
                         .getRegimeInscription().getCode(),
                 traitementsCmi)) {
            printOneDossier();
         } else {
             addErrorMessage(null, "ERROR.ACTION.DOSSIER.ONE_DOWNLOAD");
             goOneDossierInvalide();
         }
     }
 
     /**
      * Print the candidate document for one cmi.
      *
      * @throws IOException
      */
    private void printOneDossier() throws IOException {
        CommissionPojo cmiPojo = (CommissionPojo) object;
         IndDocument indDocument = prepareIndDocument();
         makePDF(cmiPojo.getCommission(), indDocument);
     }
 
     /**
      * Method used to go to the welcoem page.
      *
      * @return String
      */
     private String goOneDossierInvalide() {
         return NavigationRulesConst.SUMMARY_WISHES;
     }
 
     /**
      * make the indDocument.
      *
      * @return IndDocument
      * @throws IOException
      */
    private IndDocument prepareIndDocument() throws IOException {
         individuController.setPojoIndividu(getCurrentInd());
         individuController.initAllPojo();
 
         IndDocument indDocument = new IndDocument();
         indDocument.setAnneeUniversitaire(
                 individuController.getPojoIndividu().getCampagneEnServ(getDomainService()).getCode());
         indDocument.setAnneeUniversitaireApogee(
                 individuController.getPojoIndividu().getCampagneEnServ(getDomainService()).getCodAnu());
         indDocument.setIndividuPojo(individuController.getPojoIndividu());
         indDocument.setAdressePojo(individuController.getAdressController().getFixAdrPojo());
         indDocument.setIndBacPojo(individuController.getIndBacController().getIndBacPojos());
         indDocument.setIndCursusScolPojos(individuController.getCursusController().getCursusList());
         List<IndCursus> cursusList = individuController.getCursusController()
                 .getIndCursusPojo().getCursusList();
 
         //Enhancement tri cursus et stage dans dossiers PDF
         triListCursus(cursusList);
 
         for (IndCursus cursus : cursusList) {
             if (org.springframework.util.StringUtils.hasText(cursus.getComment())) {
                 cursus.setComment(cursus.getComment()); //StringUtils.htmlToText(cursus.getComment())
             }
         }
         indDocument.setIndCursusPro(cursusList);
         Collections.sort(indDocument.getIndCursusPro(), new ComparatorString(IndCursus.class));
         List<IndCursus> qualifList = individuController.getCursusController().getPojoQualif().getCursusList();
         for (IndCursus qualif : qualifList) {
             if (org.springframework.util.StringUtils.hasText(qualif.getComment())) {
                 qualif.setComment(qualif.getComment()); //StringUtils.htmlToText(qualif.getComment())
             }
         }
         indDocument.setQualifPro(qualifList);
         Collections.sort(indDocument.getQualifPro(), new ComparatorString(IndCursus.class));
 
         // cas d'un candidat FC, on ajout la situation
         int codeRI = Utilitaires.getCodeRIIndividu(individuController.getPojoIndividu().getIndividu(),
                 getDomainService());
         if (codeRI == FormationContinue.CODE) {
             IndSituation indSituation = getDomainService().getIndSituation(
                     individuController.getPojoIndividu().getIndividu());
             indDocument.setIndSituation(indSituation);
             if (indSituation instanceof SituationSalarie) {
                 indDocument.setAdresseEmployeurPojo(new AdressePojo(
                         ((SituationSalarie) indSituation).getAdresseEmployeur(),
                         getDomainApoService()));
             }
         }
 
         return indDocument;
     }
 
 
     /**
      * Tri la liste des stages dans un ordre décroissant par année
      * renvoi la liste non modifié si format de date différent de yyyy
      *
      * @param listCursus
      * @return list
      */
     private List<IndCursus> triListCursus(List<IndCursus> listCursus) {
         ArrayList<IndCursus> listCursusFinal = new ArrayList<IndCursus>(listCursus);
 
         for (int i = 0; i < listCursus.size() - 1; i++) {
             IndCursus ic1 = listCursus.get(i);
             for (int j = i + 1; j < listCursus.size(); j++) {
                 IndCursus ic2 = listCursus.get(j);
                 String pattern = "yyyy";
                 Date date1;
                 Date date2;
 	         
 	        	/* Parsing String -> Date */
                 try {
                     date1 = (new SimpleDateFormat(pattern)).parse(ic1.getAnnee());
                     date2 = (new SimpleDateFormat(pattern)).parse(ic2.getAnnee());
 
                     if (date1.before(date2)) {
                         listCursusFinal.remove(i);
                         listCursusFinal.add(i, ic2);
                         listCursusFinal.remove(j);
                         listCursusFinal.add(j, ic1);
                     }
                 } catch (ParseException ex) {
                     ex.printStackTrace();
                     return listCursus;
                 }
 
             }
         }
 
         return listCursusFinal;
 
 
     }
 
     /**
      * @param cmiSelect
      * @param indDocument
      */
     private void makePDF(final Commission cmiSelect, final IndDocument indDocument) {
         Individu i = getCurrentInd().getIndividu().clone();
         //liste de toutes les commissions en service.
         Set<Commission> cmi = getParameterService().getCommissions(true);
 
         //map contenant la commission et ses etapes sur lesquelles le candidat e deposer des voeux
         Map<Commission, Set<VersionEtapeDTO>> mapCmi = Utilitaires.getCmiForIndVoeux(cmi,
                 individuController.getPojoIndividu().getIndVoeuxPojo(),
                 individuController.getPojoIndividu().getCampagneEnServ(getDomainService()));
 
         // map contenant les etapes sur lesquelles il y a des formulaires
         Map<VersionEtpOpi, IndFormulaire> mapIndFormulaires = formulairesController.getIndFormulaires();
 
         if (cmiSelect != null) {
             //make the pdf only for cmiSelect.
             Map<Commission, Set<VersionEtapeDTO>> map = new HashMap<Commission, Set<VersionEtapeDTO>>();
             map.put(cmiSelect, mapCmi.get(cmiSelect));
             mapCmi = map;
 
             //test s'il y a des formulaires pour cette commission.
             Map<VersionEtpOpi, IndFormulaire> mapIndF = new HashMap<VersionEtpOpi, IndFormulaire>();
             for (Map.Entry<VersionEtpOpi, IndFormulaire> vOpi : mapIndFormulaires.entrySet()) {
                 Set<VersionEtpOpi> listVOpi = Conversions.convertVetInVetOpi(mapCmi.get(cmiSelect));
                 if (listVOpi.contains(vOpi.getKey())) {
                     mapIndF.put(vOpi.getKey(), vOpi.getValue());
                 }
             }
 
             mapIndFormulaires = mapIndF;
 
         }
 
         //si les voeux du candidat ne sont que dans une commission on genere un pdf
         final Boolean returnOnePdf = mapCmi.size() == 1 && mapIndFormulaires.isEmpty();
 
         ByteArrayOutputStream zipByteArray = new ByteArrayOutputStream();
         ZipOutputStream zipStream = new ZipOutputStream(zipByteArray);
 
         for (Map.Entry<Commission, Set<VersionEtapeDTO>> commissionMap : mapCmi.entrySet()) {
             Commission commission = getParameterService()
                     .getCommission(commissionMap.getKey().getId(), commissionMap.getKey().getCode());
             String fileNameXml = String.valueOf(System.currentTimeMillis())
                     + "_" + commission.getCode() + ".xml";
             Map<CommissionPojo, Set<VersionEtapeDTO>> mapOneCmi =
                     new HashMap<CommissionPojo, Set<VersionEtapeDTO>>();
             CommissionPojo cmiPojo = new CommissionPojo(
                     commission,
                     new AdressePojo(commission.getContactsCommission()
                             .get(Utilitaires.getCodeRIIndividu(i,
                                     getDomainService()))
                             .getAdresse(), getDomainApoService()),
                     commission.getContactsCommission()
                             .get(Utilitaires.getCodeRIIndividu(i,
                                     getDomainService())));
 
             mapOneCmi.put(cmiPojo, commissionMap.getValue());
 
             indDocument.setCmiAndVowsInd(mapOneCmi);
             Set<VersionEtpOpi> vetOpi = Conversions.convertVetInVetOpi(commissionMap.getValue());
             List<PieceJustificative> listPJ = getParameterService()
                     .getPiecesJ(vetOpi, Utilitaires.getCodeRIIndividu(i,
                             getDomainService()).toString());
             Collections.sort(listPJ, new ComparatorString(Nomenclature.class));
             indDocument.setPieces(listPJ);
             Collections.sort(indDocument.getPieces(), new ComparatorString(Nomenclature.class));
             castorService.objectToFileXml(indDocument, fileNameXml);
 
             String fileNamePdf = "dossier_" + commission.getCode() + ".pdf";
 
             String fileNameXsl = Constantes.DOSSIER_IND_XSL;
             int codeRI = Utilitaires.getCodeRIIndividu(i,
                     getDomainService());
             if (codeRI == FormationContinue.CODE) {
                 fileNameXsl = Constantes.DOSSIER_IND_FC_XSL;
             }
 
             CastorService cs = (CastorService) castorService;
             if (returnOnePdf) {
                 PDFUtils.exportPDF(fileNameXml, FacesContext.getCurrentInstance(),
                         cs.getXslXmlPath(), fileNamePdf, fileNameXsl);
             } else {
                 zipStream = PDFUtils.preparePDFinZip(
                         fileNameXml, zipStream, cs.getXslXmlPath(),
                         fileNamePdf, fileNameXsl);
 
                 // For all the etapes avec formulaire, on recupere le PDF et on l'ajoute e l'archive
                 for (Map.Entry<VersionEtpOpi, IndFormulaire> vEtpOpiMap : mapIndFormulaires.entrySet()) {
                     try {
                         VersionEtpOpi vEtpOpi = vEtpOpiMap.getKey();
                         if (vetOpi.contains(vEtpOpi)) {
                             fileNamePdf = "formulaire_" + commission.getCode() + "_"
                                     + vEtpOpi.getCodEtp() + "-"
                                     + vEtpOpi.getCodVrsVet() + ".pdf";
 
                             RegimeInscription regime = getRegimeIns()
                                     .get(Utilitaires.getCodeRIIndividu(i,
                                             getDomainService()));
 
                             zipStream.putNextEntry(new ZipEntry(fileNamePdf));
                             zipStream.write(
                                     formulairesController.getPdf(
                                             vEtpOpiMap.getValue(),
                                             regime));
                             zipStream.closeEntry();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
 
         if (!returnOnePdf) {
             try {
                 zipStream.close();
             } catch (IOException e) {
                 log.error("probleme lors du zipStream.close() "
                         + " les dossiers du candidat "
                         + i.getNumDossierOpi() + "n ont pas ete telecharge");
             }
 
             PDFUtils.setDownLoadAndSend(
                     zipByteArray.toByteArray(),
                     FacesContext.getCurrentInstance(),
                     Constantes.HTTP_TYPE_ZIP, "mesDossiers.zip");
         }
         individuController.reset();
         // initialisation de la situation de l'individu si FC
         if (getCurrentInd().getRegimeInscription() instanceof FormationContinue) {
             individuController.getSituationController().setIndSituation(
                     getDomainService().getIndSituation(getCurrentInd().getIndividu()));
         }
     }
 	
 	
 	/*
 	 ******************* ACCESSORS ******************** */
 
     /**
      * @param castorService the castorService to set
      */
     public void setCastorService(final ISerializationService castorService) {
         this.castorService = castorService;
     }
 
     /**
      * @param individuController the individuController to set
      */
     public void setIndividuController(final IndividuController individuController) {
         this.individuController = individuController;
     }
 
     /**
      * @param formulairesController the formulairesController to set
      */
     public void setFormulairesController(final FormulairesController formulairesController) {
         this.formulairesController = formulairesController;
     }
 
     /**
      * @param formationController the formationController to set
      */
     public void setFormationController(final FormationController formationController) {
         this.formationController = formationController;
     }
 
     /**
      * @return the object
      */
     public Object getObject() {
         return object;
     }
 
     /**
      * @param object the object to set
      */
     public void setObject(final Object object) {
         this.object = object;
     }
 
     /**
      * @return the context
      */
     public String getContext() {
         return context;
     }
 
     /**
      * @param context
      */
     public void setContext(final String context) {
         this.context = context;
     }
 
 
 }
