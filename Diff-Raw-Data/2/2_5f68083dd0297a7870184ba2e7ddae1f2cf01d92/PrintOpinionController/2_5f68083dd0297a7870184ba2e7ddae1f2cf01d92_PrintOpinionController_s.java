 package org.esupportail.opi.web.controllers.opinions;
 
 import fj.F;
 import fj.data.Array;
 import fj.data.Option;
 import fj.data.Stream;
 import gouv.education.apogee.commun.transverse.dto.geographie.communedto.CommuneDTO;
 import org.esupportail.commons.services.i18n.I18nService;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.services.smtp.SmtpService;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.parameters.InscriptionAdm;
 import org.esupportail.opi.domain.beans.parameters.Refused;
 import org.esupportail.opi.domain.beans.parameters.Transfert;
 import org.esupportail.opi.domain.beans.parameters.TypeDecision;
 import org.esupportail.opi.domain.beans.references.calendar.CalendarCmi;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.ContactCommission;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.user.Adresse;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.User;
 import org.esupportail.opi.domain.beans.user.candidature.Avis;
 import org.esupportail.opi.domain.beans.user.indcursus.IndBac;
 import org.esupportail.opi.services.export.CastorService;
 import org.esupportail.opi.services.export.ISerializationService;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.*;
 import org.esupportail.opi.web.beans.utils.ExportUtils;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.PDFUtils;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.opi.web.controllers.references.CommissionController;
 import org.esupportail.opi.web.controllers.user.CursusController;
 import org.esupportail.opi.web.controllers.user.IndividuController;
 import org.esupportail.opi.web.utils.MiscUtils;
 import org.esupportail.opi.web.utils.io.SuperCSV;
 import org.esupportail.opi.web.utils.paginator.LazyDataModel;
 import org.esupportail.wssi.services.remote.BacOuxEqu;
 import org.esupportail.wssi.services.remote.Pays;
 import org.esupportail.wssi.services.remote.SignataireDTO;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 import org.springframework.util.StringUtils;
 import org.supercsv.io.ICsvBeanWriter;
 
 import javax.faces.context.FacesContext;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.zip.ZipOutputStream;
 
 import static fj.P.p;
 import static fj.data.Array.iterableArray;
 import static fj.data.IterableW.wrap;
 import static fj.data.Option.fromNull;
 import static fj.data.Stream.iterableStream;
 import static fj.function.Booleans.not;
 import static org.esupportail.opi.web.utils.fj.Conversions.individuToPojo;
 import static org.esupportail.opi.web.utils.fj.Functions.getRICode;
 import static org.esupportail.opi.web.utils.fj.Predicates.indWithVoeux;
 import static org.esupportail.opi.web.utils.fj.Predicates.typeTrtEquals;
 import static org.esupportail.opi.web.utils.fj.parallel.ParallelModule.parMod;
 import static org.esupportail.opi.web.utils.io.SuperCSV.*;
 
 
 public class PrintOpinionController extends AbstractContextAwareController {
 
     // ******************* PROPERTIES *******************
 
     private static final long serialVersionUID = 7174653291470562703L;
 
     private static final List<String> HEADER_CVS =
             new ArrayList<String>() {
                 private static final long serialVersionUID = 4451087010675988608L;
 
                 {
                     add("Commission");
                     add("Num_Dos_OPI");
                     add("Nom_Patrony");
                     add("Prenom");
                     add("Date_Naiss");
                     add("Code_clef_INE");
                     add("Adresse_1");
                     add("Adresse_2");
                     add("Adresse_3");
                     add("Cedex");
                     add("Code_Postal");
                     add("Lib_Commune");
                     add("Lib_Pays");
                     add("Telephone_fixe");
                     add("Mail");
                     add("Bac");
                     add("Dernier_Etab_Cur");
                     add("Dernier_Etab_Etb");
                     add("Dernier_Etab_Result_Ext");
                     add("Date_depot_voeu");
                     add("Type_Traitement");
                     add("Voeu_Lib_Vet");
                     add("Etat_Voeu");
                     add("Avis_Result_Lib");
                     add("Rang");
                     add("Avis_Motivation_Commentaire");
                     add("Avis_Result_Code");
                     add("Avis_Result_Code_Apogee");
                     add("Avis_temoin_validation");
                     add("Avis_date_validation");
                 }
             };
 
     private final Logger log = new LoggerImpl(getClass());
 
     /**
      * Champs dispo pour l'export.
      */
     private List<String> champsDispos;
     /**
      * liste des champs selectionnes.
      */
     private String[] champsChoisis;
 
     /**
      * The result (of type Final or not final)
      * *selectionned or not by the gestionnaire.
      */
     private Object[] resultSelected;
 
     /**
      * List of commissions selected.
      */
     private Object[] commissionsSelected;
     /**
      * Has true if all Commission are selected.
      * Default value = false
      */
     private Boolean allChecked;
 
     /**
      * List of individus for a commission and avis positionned by gestionnaire.
      */
     private List<IndividuPojo> lesIndividus;
 
     /**
      * Data for pdf generation.
      */
     private Map<Commission, List<NotificationOpinion>> pdfData;
 
     private IndividuController individuController;
 
     private CommissionController commissionController;
 
     private ExportFormOrbeonController exportFormOrbeonController;
 
     private CursusController cursusController;
 
     private ActionEnum actionEnum;
 
     private InscriptionAdm inscriptionAdm;
 
     private Boolean printOnlyDef;
 
     private Refused refused;
 
     private Transfert transfert;
 
     private ISerializationService castorService;
 
     private SmtpService smtpService;
 
     private IndividuPojo individuPojoSelected;
 
     private LazyDataModel<IndividuPojo> indPojoLDM;
 
     private boolean renderTable;
 
     public PrintOpinionController() {
         super();
 
     }
 
     @Override
     public void reset() {
         commissionController.reset();
         this.resultSelected = new Object[0];
         this.commissionsSelected = new Object[0];
         this.lesIndividus = new ArrayList<IndividuPojo>();
         champsDispos = new ArrayList<String>();
         List<String> lChampschoisis = new ArrayList<String>();
         for (String champs : HEADER_CVS) {
             champsDispos.add(champs);
             lChampschoisis.add(champs);
         }
         champsChoisis = lChampschoisis.toArray(new String[lChampschoisis.size()]);
         this.allChecked = false;
         this.pdfData = new HashMap<Commission, List<NotificationOpinion>>();
         this.actionEnum = new ActionEnum();
         renderTable = false;
     }
 
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
         Assert.notNull(this.castorService,
                 "property castorService of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.commissionController,
                 "property commissionController of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.exportFormOrbeonController,
                 "property exportFormOrbeonController of class " + this.getClass().getName()
                         + " can not be null");
 
         indPojoLDM = individuController.getIndLDM().map(
                 individuToPojo(getDomainApoService(), getParameterService()));
 
         reset();
     }
 
     /*
           ******************* CALLBACK ********************** */
 
     /**
      * Callback for the print of opinions.
      *
      * @return String
      */
     public String goPrintOpinions() {
         reset();
         return NavigationRulesConst.DISPLAY_PRINT_OPINIONS;
     }
 
     /**
      * Callback for the print of TR opinions.
      *
      * @return String
      */
     public String goPrintTROpinions() {
         reset();
         return NavigationRulesConst.DISPLAY_PRINT_TR_OPINIONS;
     }
     /*
           ******************* METHODS ********************** */
 
     /**
      * Find student.
      * call in printOpinions.jsp
      */
     public void seeCandidats() {
         makeAllIndividus(
                 individuController
                         .getIndividuPaginator()
                         .getIndRechPojo()
                         .getSelectValid(), false, true);
     }
 
     /**
      * Print pdf after set the list of students.
      * call in printOpinions.jsp
      */
     public void printPDFValidation() {
         makeAllIndividus(
                 individuController
                         .getIndividuPaginator()
                         .getIndRechPojo()
                         .getSelectValid(), true, true);
         makePDFValidation();
         this.lesIndividus = new ArrayList<>();
     }
 
     /**
      * @deprecated use {@see makeAllIndividusNew()} instead
      *             Make pdf after set the list of students.
      *             call in printOpinions.jsp
      */
     public void makeCsvValidation() {
         makeAllIndividus(
                 individuController
                         .getIndividuPaginator()
                         .getIndRechPojo()
                         .getSelectValid(), true, true);
         csvGeneration(lesIndividus,
                 "exportAvis_" + commissionController.getCommission().getCode() + ".csv");
         this.lesIndividus = new ArrayList<>();
     }
 
     /**
      * Make pdf after set the list of students.
      * call in printOpinions.jsp
      */
     public void makeCsvValidationNew() {
         final String fileNamePrefix = "exportAvis";
         final String fileNameSuffix = ".csv";
         // list of indivius from the commission selected
         // with an opinion not validate
         Integer idCmi = individuController.getIndividuPaginator().getIndRechPojo().getIdCmi();
         if (idCmi != null) {
             Commission cmi = retrieveOSIVCommission(idCmi, null);
             this.commissionController.setCommission(cmi);
             Boolean selectValid = individuController.getIndividuPaginator().getIndRechPojo().getSelectValid();
             generateCSVListes(
                     cmi,
                     getIndividus(cmi, selectValid, not(typeTrtEquals(transfert))),
                     fileNamePrefix,
                     fileNameSuffix);
         }
     }
 
     /**
      * Make pdf after set the list of students.
      * call in printOpinions.jsp
      */
     public void makeCsvFormulaire() {
         IndRechPojo pojo = individuController.getIndividuPaginator().getIndRechPojo();
 
         Option<Integer> idComOpt = fromNull(pojo.getIdCmi());
         List<RegimeInscription> listRI = new ArrayList<>(pojo.getListeRI());
 
         for (Integer idCom : idComOpt)
             exportFormOrbeonController.makeCsvFormulaire(
                     retrieveOSIVCommission(idCom, null),
                     listRI);
 
         if (idComOpt.isNone())
             addErrorMessage(null, "ERROR.PRINT.COMMISSION_NULL");
 
         if (listRI.isEmpty())
             addErrorMessage(null, "ERROR.PRINT.LIST_RI_NULL");
     }
 
     /**
      * Make csv after see the list of students.
      * call in seeNotEtudiant.jsp
      */
     public void makeCsvInSeeEtuVal() {
         csvGeneration(lesIndividus,
                 "exportAvis_" + commissionController.getCommission().getCode() + ".csv");
         this.lesIndividus = new ArrayList<IndividuPojo>();
     }
 
     /**
      * print pdf of notification for the student selected.
      * call in printOpinions.jsp
      */
     public void printOneNotification() {
         this.pdfData.clear();
 
         List<IndividuPojo> individus = new ArrayList<IndividuPojo>();
         individus.add(individuPojoSelected);
 
         Commission com = retrieveOSIVCommission(
                 individuController.getIndividuPaginator().getIndRechPojo()
                         .getIdCmi(), null);
 
         makePdfData(individus, com);
 
         if (!this.pdfData.isEmpty()) {
             printOnlyDef = false;
             notificationPdfGeneration();
         } else {
             addInfoMessage(null, "INFO.PRINT.NO_NOTIFICATION");
         }
     }
 
     /**
      * print pdf of notifications for the student of the commission selected.
      * call in printOpinions.jsp
      */
     public void printPDFAllNotifications() {
         this.pdfData.clear();
         makeAllIndividus(
                 individuController.getIndividuPaginator().getIndRechPojo().getSelectValid(), false, true);
 
         Commission com = retrieveOSIVCommission(
                 individuController.getIndividuPaginator().getIndRechPojo()
                         .getIdCmi(), null);
 
         makePdfData(lesIndividus, com);
 
         if (!this.pdfData.isEmpty()) {
             printOnlyDef = false;
             notificationPdfGeneration();
         } else {
             addInfoMessage(null, "INFO.PRINT.NO_NOTIFICATION");
         }
         this.lesIndividus = new ArrayList<>();
     }
 
     /**
      * Generate the CSV de la liste preparatoire de la commission.
      * call in printListsPrepa.jsp
      */
     public void generateCSVListesPreparatoire() {
         final String fileNamePrefix = "listePrepa";
         final String fileNameSuffix = ".csv";
         final Commission commission = commissionController.getCommission();
         generateCSVListes(
                 commission,
                 getIndividus(
                         retrieveOSIVCommission(commission.getId(), commission.getCode()),
                         true,
                         not(typeTrtEquals(transfert))),
                 fileNamePrefix,
                 fileNameSuffix);
     }
 
     public void generateCSVListesTransfert() {
         final String prefix = "listeTransfert";
         final String suffix = ".csv";
         final Commission commission = commissionController.getCommission();
         generateCSVListes(
                 commission,
                 getIndividus(
                         retrieveOSIVCommission(commission.getId(), commission.getCode()),
                         true,
                         typeTrtEquals(transfert)),
                 prefix,
                 suffix);
     }
 
     public void generateCSVListes(final Commission commission,
                                   final Stream<IndividuPojo> individus,
                                   final String fileNamePrefix,
                                   final String fileNameSuffix) {
         // seems dumb but we prefer to access a copy of the session variable in case of concurrent accesses
         final String[] champs =
                 (champsChoisis == null) ? HEADER_CVS.toArray(new String[HEADER_CVS.size()]) : champsChoisis;
 
         final User currentUser = getSessionController().getCurrentUser();
         final I18nService i18n = getI18nService();
         final String prefix = fileNamePrefix + "_" + commission.getCode() + "_";
         // a helper class to get a handle on the temp Path within the try-with-resources block
         class FileHolder implements Closeable {
             private Path path;
             public void close() throws IOException {}
             public Path getFile() throws IOException {
                 if (path == null) path = Files.createTempFile(prefix, fileNameSuffix);
                 return path;
             }
         }
 
         try (final FileHolder holder = new FileHolder();
              final SuperCSV<ICsvBeanWriter> csv = superCSV(holder.getFile(), champs)) {
             forEach(individus,
                     new IOF<IndividuPojo, IOUnit>() {
                         public IOUnit fio(IndividuPojo ip) throws IOException {
                             return forEach(indPojoToLignes(ip, commission), new IOF<LigneCSV, IOUnit>() {
                                 public IOUnit fio(LigneCSV ligne) throws IOException {
                                     return csv.map(SuperCSV.<LigneCSV>write_().fio(p(ligne, champs)))
                                             .run();
                                 }
                             });
                         }
                     });
             Utilitaires.sendEmail.f(smtpService, false).e(p(
                     new InternetAddress(currentUser.getAdressMail()),
                     i18n.getString("EXPORT.CSV.MAIL.SUBJECT"),
                     "",
                     i18n.getString("EXPORT.CSV.MAIL.BODY"),
                     Arrays.<File>asList(holder.getFile().toFile())
             ));
 
         } catch (Exception e) {
             log.error(e);
             try {
                 Utilitaires.sendEmail.f(smtpService, false).e(p(
                         new InternetAddress(currentUser.getAdressMail()),
                         i18n.getString("EXPORT.CSV.ERROR.MAIL.SUBJECT"),
                         "",
                         i18n.getString("EXPORT.CSV.ERROR.MAIL.BODY"),
                         Collections.<File>emptyList()));
 
             } catch (AddressException ae) {
                 log.error(ae);
             }
         }
     }
 
     /**
      * @deprecated see {@see generateCSVListes()} for new implementation
      *             Generate a CSV of the list of student.
      */
     public String csvGeneration(final List<IndividuPojo> individus, final String fileName) {
         if (champsChoisis == null) {
             champsChoisis = HEADER_CVS.toArray(new String[HEADER_CVS.size()]);
         }
         List<LigneCSV> listePrepa = new ArrayList<>(); //indPojoToLignes(individus);
         try {
             ExportUtils.superCsvGenerate(listePrepa, champsChoisis, fileName);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * Generate a CSV of the list of student.
      *
      * @return String superCsvGeneration
      */
     public Stream<LigneCSV> indPojoToLignes(IndividuPojo ind, Commission commission) {
         Individu i = ind.getIndividu();
         Pays pays = null;
         CommuneDTO commune = null;
         Adresse adresse = i.getAdresses().get(Constantes.ADR_FIX);
         if (adresse != null) {
             commune = getDomainApoService().getCommune(adresse.getCodCommune(), adresse.getCodBdi());
             pays = getDomainApoService().getPays(adresse.getCodPays());
         }
         List<LigneCSV> lignes = new ArrayList<>();
         for (IndVoeuPojo v : ind.getIndVoeuxPojo()) {
             LigneCSV ligne = new LigneCSV();
             ligne.setCommission(commission.getLibelle());
             ligne.setNum_Dos_OPI(i.getNumDossierOpi());
             ligne.setNom_Patrony(i.getNomPatronymique());
             ligne.setPrenom(i.getPrenom());
             ligne.setDate_Naiss(i.getDateNaissance());
 
             String ine = ExportUtils.isNotNull(i.getCodeNNE())
                     + ExportUtils.isNotNull(i.getCodeClefNNE());
             ligne.setCode_clef_INE(ExportUtils.isNotNull(ine));
 
             // adresse
             if (adresse != null) {
                 ligne.setAdresse_1(ExportUtils.isNotNull(adresse.getAdr1()));
                 ligne.setAdresse_2(ExportUtils.isNotNull(adresse.getAdr2()));
                 ligne.setAdresse_3(ExportUtils.isNotNull(adresse.getAdr3()));
                 ligne.setCedex(ExportUtils.isNotNull(adresse.getCedex()));
                 ligne.setCode_Postal(ExportUtils.isNotNull(adresse.getCodBdi()));
                 ligne.setLib_Commune(commune != null ?
                         commune.getLibCommune() :
                         ExportUtils.isNotNull(adresse.getLibComEtr()));
                 ligne.setLib_Pays(pays != null ? pays.getLibPay() : "");
                 ligne.setTelephone_fixe(ExportUtils.isNotNull(adresse.getPhoneNumber()));
             } else {
                 ligne.setAdresse_1("");
                 ligne.setAdresse_2("");
                 ligne.setAdresse_3("");
                 ligne.setCedex("");
                 ligne.setCode_Postal("");
                 ligne.setLib_Commune("");
                 ligne.setLib_Pays("");
                 ligne.setTelephone_fixe("");
             }
 
             ligne.setMail(i.getAdressMail());
             // bac
             boolean hasCodeBac = false;
             for (IndBac iB : i.getIndBac()) {
                 BacOuxEqu b = getDomainApoService().getBacOuxEqu(
                         iB.getDateObtention(),
                         ExportUtils.isNotNull(iB.getCodBac()));
                 if (b != null) {
                     ligne.setBac(b.getLibBac());
                 } else {
                     ligne.setBac(iB.getCodBac());
                 }
                 hasCodeBac = true;
                 break;
             }
             if (!hasCodeBac) {
                 ligne.setBac("");
             }
             // dernier cursus
             IndCursusScolPojo d = ind.getDerniereAnneeEtudeCursus();
             if (d != null) {
                 ligne.setDernier_Etab_Cur(ExportUtils.isNotNull(d.getLibCur()));
                 ligne.setDernier_Etab_Etb(ExportUtils.isNotNull(d.getLibEtb()));
                 ligne.setDernier_Etab_Result_Ext(ExportUtils.isNotNull(cursusController.getResultatExt(d)));
             } else {
                 ligne.setDernier_Etab_Cur("");
                 ligne.setDernier_Etab_Etb("");
                 ligne.setDernier_Etab_Result_Ext("");
             }
             // Voeux
             DateFormat sdf = new SimpleDateFormat(Constantes.DATE_HOUR_FORMAT);
             ligne.setDate_depot_voeu(sdf.format(v.getIndVoeu().getDateCreaEnr()));
             ligne.setType_Traitement(ExportUtils.isNotNull(v.getTypeTraitement().getCode()));
             ligne.setVoeu_Lib_Vet(ExportUtils.isNotNull(v.getVrsEtape().getLibWebVet()));
             ligne.setEtat_Voeu(ExportUtils.isNotNull(getI18nService().getString(v.getEtat().getCodeLabel())));
 
             if (v.getAvisEnService() != null) {
 
                 ligne.setAvis_Result_Lib(ExportUtils.isNotNull(v.getAvisEnService().
                         getResult().getLibelle()));
 
                 if (v.getAvisEnService().getRang() != null) {
                     ligne.setRang(v.getAvisEnService().getRang().toString());
                 } else {
                     ligne.setRang("");
                 }
 
                 String comm = null;
                 if (v.getAvisEnService().getMotivationAvis() != null) {
                     comm = ExportUtils.isNotNull(v.getAvisEnService().getMotivationAvis().getLibelle());
                 }
                 if (comm != null && StringUtils.hasText(v.getAvisEnService().
                         getCommentaire())) {
                     comm += "/" + v.getAvisEnService().getCommentaire();
                 } else {
                     comm += ExportUtils.isNotNull(
                             v.getAvisEnService().getCommentaire());
                 }
                 ligne.setAvis_Motivation_Commentaire(ExportUtils.isNotNull(comm));
 
                 ligne.setAvis_Result_Code(ExportUtils.isNotNull(v.getAvisEnService().
                         getResult().getCode()));
                 ligne.setAvis_Result_Code_Apogee(ExportUtils.isNotNull(v.getAvisEnService().
                         getResult().getCodeApogee()));
                 ligne.setAvis_temoin_validation(ExportUtils.isNotNull("" + v.getAvisEnService().
                         getValidation()));
 
                 if (v.getAvisEnService().getValidation()) {
                     ligne.setAvis_date_validation(ExportUtils.isNotNull(
                             Utilitaires.convertDateToString(
                                     v.getAvisEnService().
                                             getDateModifEnr(),
                                     Constantes.DATE_FORMAT)));
                 } else {
                     ligne.setAvis_date_validation("");
                 }
             } else {
                 ligne.setAvis_Result_Lib("");
                 ligne.setRang("");
                 ligne.setAvis_Motivation_Commentaire("");
                 ligne.setAvis_Result_Code("");
                 ligne.setAvis_Result_Code_Apogee("");
                 ligne.setAvis_temoin_validation("");
                 ligne.setAvis_date_validation("");
             }
             lignes.add(ligne);
         }
         return iterableStream(lignes);
     }
 
     /**
      * @deprecated better use {@see getIndividus()} instead
      *             clear and found the list of IndividuPojo and IndVoeuPjo
      *             filtred by commission and typeDecision selected by the gestionnaire.
      */
     public void lookForIndividusPojo(final Commission laCommission,
                                      final Boolean onlyValidate, final Boolean initCursusPojo, final Boolean excludeTR) {
         throw new UnsupportedOperationException("DEPRECATED !");
     }
 
 
     /**
      * @return a {@link Stream} of filtered {@link IndividuPojo}s
      */
     public Stream<IndividuPojo> getIndividus(Commission laCommission,
                                              final Boolean onlyValidate,
                                              final F<IndVoeuPojo, Boolean> voeuFilter) {
         final HashSet<Integer> listeRI =
                 new HashSet<>(wrap(commissionController.getListeRI()).map(getRICode()).toStandardList());
         final F<String, Individu> fetchInd = new F<String, Individu>() {
             public Individu f(String id) {
                 return getDomainService().fetchIndById(id, fromNull(onlyValidate));
             }
         };
         final F<Individu, IndividuPojo> buildPojos =
                 individuToPojo(getDomainApoService(), getParameterService())
                 .andThen(new F<IndividuPojo, IndividuPojo>() {
                     public IndividuPojo f(IndividuPojo ip) {
                         ip.setIndVoeuxPojo(ip.getIndVoeuxPojo().filter(voeuFilter));
                         return ip;
                     }
                 });
         final Stream<String> indsIds =
                 iterableStream(getDomainService().getIndsIds(laCommission, onlyValidate, listeRI));
         return parMod.parMap(indsIds, fetchInd.andThen(buildPojos))
                 .fmap(Stream.<IndividuPojo>filter().f(indWithVoeux()))
                 .claim();
     }
 
     /**
      * Reattachment to hibernate session
      */
     private Commission retrieveOSIVCommission(Integer id, String code) {
         return getParameterService().getCommission(id, code);
     }
 
     /**
      * @deprecated use {@see makeAllIndividusNew()} instead
      *             Int the commission and make the individuals list.
      */
     private void makeAllIndividus(final Boolean onlyValidate,
                                   final Boolean initCursusPojo, final Boolean excludeTR) {
         // list of indivius from the commission selected
         // with an opinion not validate
         Integer idCmi = individuController.getIndividuPaginator().getIndRechPojo().getIdCmi();
         if (idCmi != null) {
             this.commissionController.setCommission(getParameterService().
                     getCommission(idCmi, null));
             lesIndividus = new ArrayList<>(getIndividus(
                     commissionController.getCommission(), onlyValidate, not(typeTrtEquals(transfert))).toCollection());
 //            lookForIndividusPojo(
 //                    this.commissionController.getCommission(),
 //                    onlyValidate, initCursusPojo, excludeTR);
         }
     }
 
 
     /**
      * Labels of the Results selected by the gestionnaire.
      *
      * @return String
      */
     public String getLabelResultSelected() {
         StringBuilder r = new StringBuilder();
         Boolean first = true;
         for (Object o : this.resultSelected) {
             TypeDecision result = (TypeDecision) o;
             if (first) {
                 first = false;
             } else {
                 r.append(", ");
             }
             r.append(result.getLibelle());
         }
         return r.toString();
     }
 
     /**
      * Return the list of Type Opinion of the type selected by the user.
      *
      * @return String
      */
     public String setListTypeOpinions() {
         this.resultSelected = new Object[0];
         return NavigationRulesConst.DISPLAY_VALID_OPINIONS;
     }
 
     /**
      * Genere le PDF de validation des avis.
      */
     public void makePDFValidation() {
         if (log.isDebugEnabled()) {
             log.debug("entering makePDFValidation()");
         }
 
         // Map repartissant les IndListePrepaPojo par etape puis par avis
         Map<VersionEtapeDTO, Map<TypeDecision, List<IndListePrepaPojo>>> mapIndListByEtapeAndAvis =
                 new TreeMap<VersionEtapeDTO, Map<TypeDecision, List<IndListePrepaPojo>>>(
                         new ComparatorString(VersionEtapeDTO.class));
 
         // on boucle sur la liste des individus de la commission avec les avis selectionnes
         for (IndividuPojo iP : this.lesIndividus) {
             // hibernate session reattachment
             Individu ind = iP.getIndividu();
             iP.setIndividu(getDomainService().getIndividu(
                     ind.getNumDossierOpi(), ind.getDateNaissance()));
 
             // initialisation des cursus scolaires
             MiscUtils.initIndCursusScolPojo(iP, getDomainApoService());
             
             // on boucle sur les listes des avis de chaque individu
             for (IndVoeuPojo indVoeuPojo : iP.getIndVoeuxPojo()) {
                 Avis unAvis = indVoeuPojo.getAvisEnService();
                 if (unAvis != null) {
                     TraitementCmi trtCmi =
                             unAvis.getIndVoeu().getLinkTrtCmiCamp().getTraitementCmi();
                     // on recupere l'etape de l'avis
                     VersionEtapeDTO vDTO = getDomainApoService().getVersionEtape(
                             trtCmi.getVersionEtpOpi().getCodEtp(),
                             trtCmi.getVersionEtpOpi().getCodVrsVet());
                     // on cree l'entree de l'etape si elle n'existe pas
                     if (!mapIndListByEtapeAndAvis.containsKey(vDTO)) {
                         mapIndListByEtapeAndAvis.put(vDTO, new TreeMap<TypeDecision,
                                 List<IndListePrepaPojo>>(new ComparatorString(
                                 TypeDecision.class)));
                     }
 
                     // on recupere le type de decision de l'avis
                     TypeDecision typeDec = unAvis.getResult();
                     // on cree l'entree du type de decision si elle n'existe pas
                     if (!mapIndListByEtapeAndAvis.get(vDTO).containsKey(typeDec)) {
                         mapIndListByEtapeAndAvis.get(vDTO).put(typeDec,
                                 new ArrayList<IndListePrepaPojo>());
                     }
                     // on cree l'IndListePrepaPojo correspondant pour l'ajouter dans la map
                     IndListePrepaPojo unIndPrepa = new IndListePrepaPojo();
                     // code de la commission from Commission.code
                     unIndPrepa.setCodeCmi(commissionController.getCommission().getCode());
                     // code du dossier de l'individu from IndividuPojo.individu.numDossierOpi
                     unIndPrepa.setNumDossierOpi(iP.getIndividu().getNumDossierOpi());
                     // nom de l'individu from IndividuPojo.individu.nomPatronymique
                     unIndPrepa.setNom(iP.getIndividu().getNomPatronymique());
                     // prenom de l'individu from IndividuPojo.individu.prenom
                     unIndPrepa.setPrenom(iP.getIndividu().getPrenom());
                     // codeEtu de l'individu from IndividuPojo.individu.codeEtu
                     unIndPrepa.setCodeEtu(iP.getIndividu().getCodeEtu());
                     // bac de l'individu from IndividuPojo.individu.indBac
                     // (premier element de la liste)
                     IndBac iB = iP.getIndividu().getIndBac().iterator().next();
                     BacOuxEqu b = getDomainApoService().getBacOuxEqu(
                             iB.getDateObtention(),
                             ExportUtils.isNotNull(iB.getCodBac()));
                     if (b != null) {
                         unIndPrepa.setBac(b.getLibBac());
                     } else {
                         unIndPrepa.setBac(iB.getCodBac());
                     }
                     if (iP.getDerniereAnneeEtudeCursus() != null) {
                         // titre fondant la demande from
                         // IndividuPojo.derniereAnneeEtudeCursus.libCur
                         unIndPrepa.setTitreAccesDemande(
                                 iP.getDerniereAnneeEtudeCursus().getLibCur());
                         // dernier cursus from  IndividuPojo.derniereAnneeEtudeCursus.cursus
                         unIndPrepa.setDernierIndCursusScol(iP.getDerniereAnneeEtudeCursus()
                                 .getCursus());
                     }
                     // creation d'un indVoeuPojo
                     IndVoeuPojo indPojo = new IndVoeuPojo();
                     indPojo.setIndVoeu(unAvis.getIndVoeu());
                     indPojo.setVrsEtape(vDTO);
 
                     indPojo.setAvisEnService(unAvis);
                     // on ajoute indPojo
                     unIndPrepa.setIndVoeuxPojo(new HashSet<IndVoeuPojo>());
                     unIndPrepa.getIndVoeuxPojo().add(indPojo);
 
                     // on ajoute l'indPrepa dans la map
                     mapIndListByEtapeAndAvis.get(vDTO).get(typeDec).add(unIndPrepa);
                 }
             }
         }
 
         // tri de chaque sous liste par ordre alphabetique
         for (Map.Entry<VersionEtapeDTO, Map<TypeDecision, List<IndListePrepaPojo>>>
                 indListForOneEtape : mapIndListByEtapeAndAvis.entrySet()) {
             for (Map.Entry<TypeDecision, List<IndListePrepaPojo>>
                     indListForOneAvis : indListForOneEtape.getValue().entrySet()) {
                 Collections.sort(indListForOneAvis.getValue(),
                         new ComparatorString(IndListePrepaPojo.class));
             }
         }
 
         // on recupere le xsl correspondant e l'edition
         // par ordre alphabetique
         String fileNameXsl = Constantes.LISTE_VALIDATION_AVIS_XSL;
         String fileNamePdf = "listeValidationAvis_"
                 + commissionController.getCommission().getCode() + ".pdf";
         String fileNameXml = String.valueOf(System.currentTimeMillis())
                 + "_" + commissionController.getCommission().getCode() + ".xml";
 
         // on genere le pdf
         commissionController.generatePDFListePreparatoire(
                 fileNameXsl, fileNameXml, fileNamePdf, mapIndListByEtapeAndAvis);
     }
 
     /**
      * Genere le pdf des notifications.
      *
      * @return String
      */
     public String notificationPdfGeneration() {
         ByteArrayOutputStream zipByteArray = new ByteArrayOutputStream();
         ZipOutputStream zipStream = new ZipOutputStream(zipByteArray);
         // generate the pdf if exist
         if (!this.pdfData.isEmpty()) {
             Set<Commission> lesCommissions = this.pdfData.keySet();
             for (Commission laCommission : lesCommissions) {
                 String fileNameXml = String.valueOf(System.currentTimeMillis())
                         + "_" + laCommission.getCode() + ".xml";
                 String fileNamePdf = "commission_" + laCommission.getCode() + ".pdf";
                 List<NotificationOpinion> lesNotifs = this.pdfData.get(laCommission);
 
                 for (NotificationOpinion n : lesNotifs) {
                     if (printOnlyDef) {
                         n.setVoeuxFavorable(new HashSet<IndVoeuPojo>());
                         n.setVoeuxFavorableAppel(new HashSet<IndVoeuPojo>());
                     }
                 }
                 castorService.objectToFileXml(lesNotifs, fileNameXml);
                 CastorService cs = (CastorService) castorService;
                 if (lesCommissions.size() > 1) {
                     // zip file
                     PDFUtils.preparePDFinZip(
                             fileNameXml, zipStream,
                             cs.getXslXmlPath(),
                             fileNamePdf, Constantes.NOTIFICATION_IND_XSL);
 
                 } else {
                     // one pdf
                     PDFUtils.exportPDF(fileNameXml, FacesContext.getCurrentInstance(),
                             cs.getXslXmlPath(),
                             fileNamePdf, Constantes.NOTIFICATION_IND_XSL);
                 }
             }
             if (lesCommissions.size() > 1) {
                 try {
                     zipStream.close();
                 } catch (IOException e) {
                     log.error("probleme lors du zipStream.close() "
                             + " les notification des commissions "
                             + "n ont pas ete telechargee");
                 }
                 PDFUtils.setDownLoadAndSend(
                         zipByteArray.toByteArray(),
                         FacesContext.getCurrentInstance(),
                         Constantes.HTTP_TYPE_ZIP, "NotifsCommissions.zip");
             }
         }
         actionEnum.setWhatAction(ActionEnum.EMPTY_ACTION);
         return NavigationRulesConst.DISPLAY_VALID_OPINIONS;
     }
 
     /**
      * Make the Map Map < Commission , List < NotificationOpinion>> pdfData.
      *
      * @param individus
      * @param laCommission
      */
     public void makePdfData(final List<IndividuPojo> individus, final Commission laCommission) {
         // hibernate session reattachment
         Commission com = retrieveOSIVCommission(
                 laCommission.getId(), laCommission.getCode());
 
         List<NotificationOpinion> dataPDF = new ArrayList<NotificationOpinion>();
         for (IndividuPojo i : individus) {
             Set<IndVoeuPojo> indVoeuPojoFav = new HashSet<IndVoeuPojo>();
             Set<IndVoeuPojo> indVoeuPojoDef = new HashSet<IndVoeuPojo>();
             Set<IndVoeuPojo> indVoeuPojoFavAppel = new HashSet<IndVoeuPojo>();
             Set<IndVoeuPojo> indVoeuPojoDefAppel = new HashSet<IndVoeuPojo>();
             for (IndVoeuPojo indVPojo : i.getIndVoeuxPojo()) {
                 Avis a = indVPojo.getAvisEnService();
                 if (a != null) {
                     if (a.getResult().getIsFinal()
                             && a.getResult().getCodeTypeConvocation()
                             .equals(inscriptionAdm.getCode())) {
                         if (!a.getAppel()) {
                             indVoeuPojoFav.add(indVPojo);
                         } else {
                             indVoeuPojoFavAppel.add(indVPojo);
                         }
                     } else {
                         if (a.getResult().getIsFinal()
                                 && a.getResult().getCodeTypeConvocation()
                                 .equals(refused.getCode())) {
                             if (!a.getAppel()) {
                                 indVoeuPojoDef.add(indVPojo);
                             } else {
                                 indVoeuPojoDefAppel.add(indVPojo);
                             }
                         }
                     }
                 }
             }
             // data for pdf if necessery
             if (!indVoeuPojoFav.isEmpty() || !indVoeuPojoDef.isEmpty()
                     || !indVoeuPojoFavAppel.isEmpty() || !indVoeuPojoDefAppel.isEmpty()) {
                 NotificationOpinion notificationOpinion =
                         initNotificationOpinion(
                                 i,
                                 com,
                                 indVoeuPojoFav,
                                 indVoeuPojoDef,
                                 indVoeuPojoFavAppel,
                                 indVoeuPojoDefAppel);
                 dataPDF.add(notificationOpinion);
             }
         }
 
         // add data to pdfData
         if (!dataPDF.isEmpty()) {
             this.pdfData.put(laCommission, dataPDF);
         }
 
 
     }
 
     /**
      * Initialisation pojo.
      *
      * @param i
      * @param laCommission
      * @param indVoeuPojoFav
      * @param indVoeuPojoDef
      * @param indVoeuPojoFavAppel
      * @param indVoeuPojoDefAppel
      * @return NotificationOpinion
      */
     private NotificationOpinion initNotificationOpinion(
             final IndividuPojo i,
             final Commission laCommission,
             final Set<IndVoeuPojo> indVoeuPojoFav,
             final Set<IndVoeuPojo> indVoeuPojoDef,
             final Set<IndVoeuPojo> indVoeuPojoFavAppel,
             final Set<IndVoeuPojo> indVoeuPojoDefAppel) {
 
         NotificationOpinion notificationOpinion = new NotificationOpinion();
 
         notificationOpinion.setVoeuxFavorable(indVoeuPojoFav);
         notificationOpinion.setVoeuxDefavorable(indVoeuPojoDef);
         notificationOpinion.setVoeuxFavorableAppel(indVoeuPojoFavAppel);
         notificationOpinion.setVoeuxDefavorableAppel(indVoeuPojoDefAppel);
         notificationOpinion.setCodeEtu(i.getIndividu().getCodeEtu());
         notificationOpinion.setNom(i.getIndividu().getNomPatronymique());
         notificationOpinion.setNumDossierOpi(i.getIndividu().getNumDossierOpi());
         notificationOpinion.setPrenom(i.getIndividu().getPrenom());
         notificationOpinion.setSexe(i.getIndividu().getSexe());
         notificationOpinion.setPeriodeScolaire(i.getCampagneEnServ(getDomainService()).getCode());
 
         ContactCommission contactCommission = laCommission.getContactsCommission().get(
                 Utilitaires.getCodeRIIndividu(i.getIndividu(), getDomainService()));
         AdressePojo aPojo = null;
         if (contactCommission != null) {
             aPojo = new AdressePojo(contactCommission.getAdresse(),
                     getDomainApoService());
             notificationOpinion.setCoordonneesContact(aPojo);
         }
         aPojo = null;
         //init hib proxy adresse
         getDomainService().initOneProxyHib(i.getIndividu(),
                 i.getIndividu().getAdresses(), Adresse.class);
         if (i.getIndividu().getAdresses() != null) {
             if (i.getIndividu().getAdresses().get(Constantes.ADR_FIX) != null) {
                 aPojo = new AdressePojo(i.getIndividu().getAdresses().
                         get(Constantes.ADR_FIX), getDomainApoService());
             }
         }
         if (laCommission.getCalendarCmi() != null) {
             getDomainService().initOneProxyHib(
                     laCommission,
                     laCommission.getCalendarCmi(),
                     CalendarCmi.class);
             if (laCommission.getCalendarCmi().getEndDatConfRes() != null) {
                 notificationOpinion.setDateCloture(
                         Utilitaires.convertDateToString(laCommission.
                                 getCalendarCmi().getEndDatConfRes(),
                                 Constantes.DATE_FORMAT));
             }
         }
 
         SignataireDTO s = null;
         Integer codeRI = i.getCampagneEnServ(getDomainService()).getCodeRI();
         if (StringUtils.hasText(laCommission.getContactsCommission()
                 .get(codeRI).getCodSig())) {
             s = getDomainApoService().getSignataire(laCommission.getContactsCommission()
                     .get(codeRI).getCodSig());
         }
         notificationOpinion.setSignataire(s);
         notificationOpinion.setNomCommission(laCommission.getContactsCommission()
                 .get(codeRI).getCorresponding());
         notificationOpinion.setAdresseEtu(aPojo);
 
         return notificationOpinion;
     }
 
 
    public void generationWarning() {
         addWarnMessage(null, "Votre document est en cours de génération. Il vous sera envoyé " +
                 "par mail à l'issu du processus.");
     }
 
     /**
      * Filter individu by commision, validated avis, typeTraitement not equals transfert;
      * foreach individu filter its voeux by avis enService and typeDecision selected.
      * Better look to delegate to DAO layer directly as it's the same operation performed by
      * {@link org.esupportail.opi.dao.IndividuDaoServiceImpl.typeDecFilter}
      */
     public void initIndividus() {
         final List<TypeDecision> typeDecisions = buildSelectedTypeDecision();
         setLesIndividus(new ArrayList<IndividuPojo>(
                 getIndividus(commissionController.getCommission(), false, not(typeTrtEquals(transfert)))
                         .map(new F<IndividuPojo, IndividuPojo>() {
                             @Override
                             public IndividuPojo f(IndividuPojo individuPojo) {
                                 individuPojo.setIndVoeuxPojo(
                                         individuPojo.getIndVoeuxPojo()
                                                 .filter(new F<IndVoeuPojo, Boolean>() {
                                                     @Override
                                                     public Boolean f(IndVoeuPojo indVoeuPojo) {
                                                         return !iterableArray(indVoeuPojo.getIndVoeu().getAvis())
                                                                 .filter(new F<Avis, Boolean>() {
                                                                     @Override
                                                                     public Boolean f(Avis avis) {
                                                                         return avis.getTemoinEnService() && typeDecisions.contains(avis.getResult());
                                                                     }
                                                                 })
                                                                 .isEmpty();
                                                     }
                                                 }));
                                 return individuPojo;
                             }
                         })
                         .toCollection()));
     }
 
     /**
      * Transform the {@link Object[]} into {@link List}
      * Primefaces hold the datatable's selection into Object[] see this.resultSelected
      * @return List of TypeDecision
      */
     private List<TypeDecision> buildSelectedTypeDecision() {
         List<TypeDecision> result = new ArrayList<>();
         for (Object o : this.resultSelected) {
             if (o instanceof TypeDecision) {
                 TypeDecision t = (TypeDecision) o;
                 result.add(t);
             }
         }
         return result;
     }
     // ******************* ACCESSORS ********************
 
     public IndividuController getIndividuController() {
         return individuController;
     }
 
     public void setIndividuController(IndividuController individuController) {
         this.individuController = individuController;
     }
 
     /**
      * @return the commissionsSelected
      */
     public Object[] getCommissionsSelected() {
         return commissionsSelected;
     }
 
     /**
      * @param commissionsSelected the commissionsSelected to set
      */
     public void setCommissionsSelected(final Object[] commissionsSelected) {
         this.commissionsSelected = commissionsSelected;
     }
 
     /**
      * @return the allChecked
      */
     public Boolean getAllChecked() {
         return allChecked;
     }
 
     /**
      * @param allChecked the allChecked to set
      */
     public void setAllChecked(final Boolean allChecked) {
         this.allChecked = allChecked;
     }
 
     /**
      * @return the champsDispo
      */
     public List<String> getChampsDispos() {
         return champsDispos;
     }
 
     /**
      * @return the champsChoisis
      */
     public String[] getChampsChoisis() {
         return champsChoisis;
     }
 
     /**
      * @param champsChoisis the champsChoisis to set
      */
     public void setChampsChoisis(final String[] champsChoisis) {
         this.champsChoisis = champsChoisis;
     }
 
     /**
      * @return the lesIndividus
      */
     public List<IndividuPojo> getLesIndividus() {
         return lesIndividus;
     }
 
     /**
      * @param lesIndividus the lesIndividus to set
      */
     public void setLesIndividus(final List<IndividuPojo> lesIndividus) {
         this.lesIndividus = lesIndividus;
     }
 
     /**
      * @return the resultSelected
      */
     public Object[] getResultSelected() {
         return resultSelected;
     }
 
     /**
      * @param resultSelected the resultSelected to set
      */
     public void setResultSelected(final Object[] resultSelected) {
         this.resultSelected = resultSelected;
     }
 
 
     /**
      * @return the pdfData
      */
     public Map<Commission, List<NotificationOpinion>> getPdfData() {
         return pdfData;
     }
 
     /**
      * @param pdfData the pdfData to set
      */
     public void setPdfData(final Map<Commission, List<NotificationOpinion>> pdfData) {
         this.pdfData = pdfData;
     }
 
     /**
      * @param commissionController the commissionController to set
      */
     public void setCommissionController(final CommissionController commissionController) {
         this.commissionController = commissionController;
     }
 
     /**
      * @param castorService the castorService to set
      */
     public void setCastorService(final ISerializationService castorService) {
         this.castorService = castorService;
     }
 
     /**
      * @return the transfert
      */
     public Transfert getTransfert() {
         return transfert;
     }
 
     /**
      * @param transfert the transfert to set
      */
     public void setTransfert(final Transfert transfert) {
         this.transfert = transfert;
     }
 
     /**
      * @return the inscriptionAdm
      */
     public InscriptionAdm getInscriptionAdm() {
         return inscriptionAdm;
     }
 
     /**
      * @param inscriptionAdm the inscriptionAdm to set
      */
     public void setInscriptionAdm(final InscriptionAdm inscriptionAdm) {
         this.inscriptionAdm = inscriptionAdm;
     }
 
     /**
      * @return the refused
      */
     public Refused getRefused() {
         return refused;
     }
 
     /**
      * @param refused the refused to set
      */
     public void setRefused(final Refused refused) {
         this.refused = refused;
     }
 
     /**
      * @return the actionEnum
      */
     public ActionEnum getActionEnum() {
         return actionEnum;
     }
 
     /**
      * @param actionEnum the actionEnum to set
      */
     public void setActionEnum(final ActionEnum actionEnum) {
         this.actionEnum = actionEnum;
     }
 
     /**
      * @return the printOnlyDef
      */
     public Boolean getPrintOnlyDef() {
         return printOnlyDef;
     }
 
     /**
      * @param printOnlyDef the printOnlyDef to set
      */
     public void setPrintOnlyDef(final Boolean printOnlyDef) {
         this.printOnlyDef = printOnlyDef;
     }
 
     /**
      * @return the castorService
      */
     public ISerializationService getCastorService() {
         return castorService;
     }
 
     public void setSmtpService(SmtpService smtpService) {
         this.smtpService = smtpService;
     }
 
     /**
      * @return the individuPojoSelected
      */
     public IndividuPojo getIndividuPojoSelected() {
         return individuPojoSelected;
     }
 
     /**
      * @param individuPojoSelected the individuPojoSelected to set
      */
     public void setIndividuPojoSelected(final IndividuPojo individuPojoSelected) {
         this.individuPojoSelected = individuPojoSelected;
     }
 
 
     /**
      * @param exportFormOrbeonController
      */
     public void setExportFormOrbeonController(
             final ExportFormOrbeonController exportFormOrbeonController) {
         this.exportFormOrbeonController = exportFormOrbeonController;
     }
 
     public LazyDataModel<IndividuPojo> getIndPojoLDM() {
         return indPojoLDM;
     }
 
     public void setIndPojoLDM(LazyDataModel<IndividuPojo> indPojoLDM) {
         this.indPojoLDM = indPojoLDM;
     }
 
     public boolean isRenderTable() {
         return renderTable;
     }
 
     public void doInitSelectValid() {
         IndRechPojo rp = individuController.getIndividuPaginator().getIndRechPojo();
         if (rp.getSelectValid() == null) {
             rp.setSelectValid(false);
         }
     }
 
     public void doRenderTable() {
         renderTable = true;
     }
 
 }
 
