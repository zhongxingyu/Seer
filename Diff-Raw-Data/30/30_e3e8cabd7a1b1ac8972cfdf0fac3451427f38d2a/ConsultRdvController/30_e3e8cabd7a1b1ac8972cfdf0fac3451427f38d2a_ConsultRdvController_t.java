 package org.esupportail.opi.web.controllers.references;
 
 
 import org.apache.myfaces.custom.schedule.ScheduleMouseEvent;
 import org.apache.myfaces.custom.schedule.UISchedule;
 import org.apache.myfaces.custom.schedule.model.ScheduleModel;
 import org.apache.myfaces.custom.schedule.model.SimpleScheduleModel;
 import org.esupportail.commons.exceptions.ConfigException;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.services.smtp.SmtpService;
 import org.esupportail.commons.utils.BeanUtils;
 import org.esupportail.opi.domain.beans.etat.EtatConfirme;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.references.commission.TraitementCmi;
 import org.esupportail.opi.domain.beans.references.rendezvous.CalendarRDV;
 import org.esupportail.opi.domain.beans.references.rendezvous.IndividuDate;
 import org.esupportail.opi.domain.beans.references.rendezvous.VetCalendar;
 import org.esupportail.opi.domain.beans.user.Adresse;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.domain.beans.user.candidature.VersionEtpOpi;
 import org.esupportail.opi.utils.Constantes;
 import org.esupportail.opi.utils.Conversions;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.components.ExtendedEntry;
 import org.esupportail.opi.web.beans.paginator.IndividuPaginator;
 import org.esupportail.opi.web.beans.utils.ExportUtils;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.wssi.services.remote.VersionEtapeDTO;
 
 import javax.faces.event.ActionEvent;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 
 /**
  * @author brice.quillerie
  */
 public class ConsultRdvController extends AbstractContextAwareController {
     /**
      * serialVersionUID.
      */
     private static final long serialVersionUID = 1397855327856855038L;
 
     /**     */
     private static final int NB_MS_BY_HALF_HOUR = 1800000;
 
     /**     */
     private static final int DOUZE = 12;
 
     /**
      * Constante Trente.
      */
     private static final int TRENTE = 30;
     /**     */
     private static final List<String> HEADER_CVS = new ArrayList<String>() {
         private static final long serialVersionUID = 4451087010675988608L;
 
         {
             add("Commission_CGE_VersionEtape");
             add("Date_RDV");
             add("Num_Dos_OPI");
             add("Nom_Patronymique");
             add("Prenom");
             add("Date_Naiss");
             add("Telephone");
             add("Mail");
             add("Voeux_du_RDV");
             add("Voeux_confirmes");
             add("Autres_voeux");
         }
     };
     /**     */
     private int startHour = DOUZE;
     /**     */
     private int endHour = DOUZE;
 
 	/*
 	 * ****************** PROPERTIES *******************
 	 */
     /**
      * A logger.
      */
     private final Logger log = new LoggerImpl(getClass());
 
     /**
      * The actionEnum.
      */
     private ActionEnum actionEnum;
 
     /**
      * see {@link IndividuPaginator}.
      */
     private IndividuPaginator individuPaginator;
 
     /**  */
     private CalendarRDV calendarRdv;
 
     /**  */
     private Individu candidat;
 
     /**
      * Calendar.
      */
     private Date dateSelect;
 
     /**
      * Schedule Model.
      */
     private ScheduleModel weekScheduleModel;
 
     /**
      * Flag Moved entry.
      */
     private boolean movedEntryMode;
 
     /**
      * Id of Entry Selected.
      */
     private String idEntrySelected;
 
     /**  */
     private UISchedule uiSchedule;
 
     /**  */
     private boolean inSearch;
 
 	/*
 	 * ****************** INIT *************************
 	 */
 
     /**
      * Controller.
      */
     public ConsultRdvController() {
         //Controller.
         movedEntryMode = false;
         dateSelect = new Date();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         log.debug("Reset ConsultRdvController");
         movedEntryMode = false;
         weekScheduleModel = null;
         dateSelect = new Date();
         startHour = DOUZE;
         endHour = DOUZE;
 
         if (calendarRdv != null) {
             getDomainService().initOneProxyHib(calendarRdv, calendarRdv.getCandidats(), IndividuDate.class);
         }
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         //
     }
 
 	/*
 	 * ****************** CALLBACK **********************
 	 */
 
     /**
      * Callback to calendar list.
      *
      * @return String
      */
     public String goSeeAllConsultRdv() {
         reset();
         return NavigationRulesConst.SEE_CONSULT_RDV;
     }
 
     /**
      * Callback to calendar add.
      *
      * @return String
      */
     public String goAddConsultRdv() {
         reset();
         getActionEnum().setWhatAction(ActionEnum.ADD_ACTION);
         return null;
     }
 
 	/*
 	 * ****************** METHODS **********************
 	 */
 
     /**
      * @return list
      */
     private List<IndVoeu> getVoeuIndCal() {
         String cge = calendarRdv.getCodeCge();
         List<IndVoeu> listIndVoeu = new ArrayList<IndVoeu>();
         if (cge == null) {
             Set<VersionEtpOpi> listVet = new HashSet<VersionEtpOpi>();
             Set<VersionEtapeDTO> listVetDto = new HashSet<VersionEtapeDTO>();
             //Récupération des versionEtpOpi de la liste commissions
             for (Commission comm : calendarRdv.getCommissions()) {
                 for (TraitementCmi tcmi : comm.getTraitementCmi()) {
                     listVet.add(tcmi.getVersionEtpOpi());
                 }
             }
             //Récupération des versionEtpOpi de la liste VetCalendar
             for (VetCalendar vetCal : calendarRdv.getVets()) {
                 listVetDto.add(getDomainApoService().getVersionEtape(
                         vetCal.getCodEtp(), vetCal.getCodVrsVet()));
             }
             if (!listVetDto.isEmpty()) {
                 listVet.addAll(Conversions.convertVetInVetOpi(listVetDto));
             }
             log.debug("nb VET : " + listVet.size());
             //Recupération des voeux d'un individu correspondant au calendrier de rendez-vous
             for (IndVoeu voeu : candidat.getVoeux()) {
                 VersionEtpOpi vetCandidat = voeu.getLinkTrtCmiCamp().getTraitementCmi().
                         getVersionEtpOpi();
                 log.debug("VET candidat : " + vetCandidat);
                 for (VersionEtpOpi vet : listVet) {
                     log.debug("VET listVet : " + vet);
                     if (vet.getCodEtp().equals(vetCandidat.getCodEtp())
                             && vet.getCodVrsVet().equals(vetCandidat.getCodVrsVet())
                             && voeu.getState().equals(EtatConfirme.I18N_STATE)) {
                         log.debug("ADD VET indVoeu");
                         listIndVoeu.add(voeu);
                         break;
                     }
                 }
             }
         } else {
             //Recupération des voeux d'un individu correspondant au calendrier de rendez-vous
             for (IndVoeu voeu : candidat.getVoeux()) {
                 String codCgeCandidat = voeu.getLinkTrtCmiCamp().getTraitementCmi().
                         getVersionEtpOpi().getCodCge();
                 if (cge.equals(codCgeCandidat) && voeu.getState().equals(EtatConfirme.I18N_STATE)) {
                     listIndVoeu.add(voeu);
                 }
             }
         }
         return listIndVoeu;
     }
 
     /**
      * @return la chaine de retour
      */
     public String ajouter() {
         // Rafraichissement des objets
 
         // Recuperation de l'entry ==> message si aucune selectionnee
         ExtendedEntry entry = (ExtendedEntry) weekScheduleModel
                 .getSelectedEntry();
         if (entry == null) {
             addErrorMessage("form:saveEntryMessage",
                     "CALENDAR_RDV.MESSAGES.SAVE_ENTRY.SELECT_ENTRY");
             return "";
         }
 
         List<IndVoeu> listVoeu = getVoeuIndCal();
         if (listVoeu.isEmpty()) {
             addErrorMessage("form:saveEntryMessage",
                     "CALENDAR_RDV.MESSAGES.SAVE_ENTRY.NOT_VOEU");
             return "";
         }
 
 
         // Mise e jour de l'etudiant
         Date date = new Date(Long.parseLong(entry.getId()));
         Date now = new Date();
 
         for (IndVoeu indVoeu : listVoeu) {
             IndividuDate indDate = new IndividuDate();
             indDate.setCandidat(candidat);
             indDate.setDateRdv(date);
             indDate.setDateCreation(now);
             indDate.setDateModification(now);
             indDate.setCalendrierRdv(calendarRdv);
             indDate.setVoeu(indVoeu);
 
             // Mise e jour du CalendarRdv
             calendarRdv.getCandidats().add(indDate);
 
             // Enregistrement de l'etudiant
             getDomainService().addIndividuDate(indDate);
         }
 
         calendarRdv.resetMap();
 
         // Mise e jour du scheduleModel
         Calendar cal = new GregorianCalendar();
         cal.setTime(entry.getStartTime());
         weekScheduleModel.removeEntry(entry);
         weekScheduleModel.addEntry(createWeekEntry(cal));
         weekScheduleModel.refresh();
         weekScheduleModel.setSelectedEntry(entry);
 
         addInfoMessage("form:saveEntryMessage",
                 "CALENDAR_RDV.MESSAGES.SAVE_ENTRY");
         return "";
     }
 
     /**
      * Listener on button move entry.
      *
      * @return la chaine d'entree
      */
     public String deplacer() {
         // Recuperation de la liste IndividuDate
         Date laDate = calendarRdv.getDateParCandidat().get(candidat);
         List<IndividuDate> listIndDate = calendarRdv.getCandidatsAsMap()
                 .get(laDate).get(candidat);
 
         // Mise e jour de l'etudiant
         dateSelect = weekScheduleModel.getSelectedEntry().getStartTime();
         for (IndividuDate indDate : listIndDate) {
             indDate.setDateRdv(dateSelect);
 
             // Enregistrement de l'etudiant
             getDomainService().updateIndividuDate(indDate);
         }
 
         calendarRdv.resetMap();
 
         // Mise e jour du scheduleModel
         Calendar cal = new GregorianCalendar();
         // 1 : Ancien RDV ; ne peut etre trouve que si la semaine n'a pas change
         // !
         // Recuperation de l'entry
         // TODO: Fix this !!
         ExtendedEntry entry = null; //(ExtendedEntry) uiSchedule.findEntry(
         //Long.toString(laDate.getTime()));
         if (entry != null) {
             weekScheduleModel.removeEntry(entry);
             cal.setTime(laDate);
             weekScheduleModel.addEntry(createWeekEntry(cal));
         }
         // 2 : Nouveau RDV
         cal.setTime(dateSelect);
         weekScheduleModel.removeSelectedEntry();
         weekScheduleModel.addEntry(createWeekEntry(cal));
         weekScheduleModel.refresh();
 
         // Mise e jour du controlleur
         movedEntryMode = false;
 
         // Envoi du mail
         InternetAddress to = null;
         try {
             String subject = this.getString("CALENDAR_RDV.MAIL.MOVE.SUBJECT");
             String body = this.getString("CALENDAR_RDV.MAIL.MOVE.BODY", dateSelect);
             body += "<br />"
                     + calendarRdv.getMsgMailConfirmation()
                     + this.getString("CALENDAR_RDV.MAIL.FOOTER");
 
             to = new InternetAddress(candidat.getAdressMail());
             ((SmtpService) BeanUtils.getBean("smtpService")).send(to, subject,
                     body, body);
         } catch (AddressException e) {
             e.printStackTrace();
         }
 
         addInfoMessage("form:saveEntryMessage",
                 "CALENDAR_RDV.MESSAGES.SAVE_ENTRY");
         return "";
     }
 
     /**
      * Supprime l'indDate.
      *
      * @return la chaine de redirection
      */
     public String supprimer() {
         // Recuperation de la liste IndividuDate
         Date laDate = calendarRdv.getDateParCandidat().get(candidat);
         List<IndividuDate> listIndDate = calendarRdv.getCandidatsAsMap()
                 .get(laDate).get(candidat);
 
         // Delete de l'etudiant
         for (IndividuDate indDate : listIndDate) {
             getDomainService().deleteIndividuDate(indDate);
             calendarRdv.getCandidats().remove(indDate);
         }
         calendarRdv.resetMap();
 
         // Maj du weekScheduleModel
         //TODO: Fix this !!
         ExtendedEntry entry = null; // (ExtendedEntry) uiSchedule.findEntry(Long
         //.toString(laDate.getTime()));
         if (entry != null) {
             Calendar cal = new GregorianCalendar();
             cal.setTime(laDate);
             weekScheduleModel.removeEntry(entry);
             weekScheduleModel.addEntry(createWeekEntry(cal));
             weekScheduleModel.refresh();
             weekScheduleModel.setSelectedEntry(entry);
         } else {
             log.info("ATTENTION : cal null");
         }
 
         // envoi du mail
         InternetAddress to = null;
         try {
             String subject = this.getString("CALENDAR_RDV.MAIL.DEL.SUBJECT");
             String body = this.getString("CALENDAR_RDV.MAIL.DEL.BODY", dateSelect);
             body += this.getString("CALENDAR_RDV.MAIL.FOOTER");
             to = new InternetAddress(candidat.getAdressMail());
             ((SmtpService) BeanUtils.getBean("smtpService")).send(to, subject,
                     body, body);
         } catch (AddressException e) {
             e.printStackTrace();
         }
 
         addInfoMessage("form:saveEntryMessage",
                 "CALENDAR_RDV.MESSAGES.REMOVE_ENTRY", candidat.getPrenom()
                 + " " + candidat.getNomPatronymique(),
                 dateSelect.toString());
         return "";
     }
 
     /**
      * construct the weekScheduleModel.
      */
     public void setWeekScheduleModel() {
         if (weekScheduleModel == null) {
             log.info("Dans setWeekScheduleModel");
 
             weekScheduleModel = new SimpleScheduleModel();
 
             Date now = new Date();
             if (calendarRdv != null &&
                     now.before(calendarRdv.getDateDebutInsc())) {
                 now = calendarRdv.getDateDebutInsc();
             }
             weekScheduleModel.setSelectedDate(now);
             weekScheduleModel.setMode(ScheduleModel.WORKWEEK);
 
             dateSelect = weekScheduleModel.getSelectedDate();
 
             List<Date> datesDisponibles = (calendarRdv != null)
                     ? calendarRdv.getDatesRdv(false) : new ArrayList<Date>();
 
             if (!datesDisponibles.isEmpty()) {
 
                 dateSelect = datesDisponibles.get(0);
 
                 for (Date date : datesDisponibles) {
                     Calendar calDebut = new GregorianCalendar();
                     calDebut.setTime(date);
                     weekScheduleModel.addEntry(createWeekEntry(calDebut));
                 }
 
                 weekScheduleModel.refresh();
             }
         }
     }
 
     /**
      * @param calDebut
      * @return l'entry pour la semaine
      */
     public ExtendedEntry createWeekEntry(final Calendar calDebut) {
         Calendar calFin = new GregorianCalendar();
         calFin.setTimeInMillis(calDebut.getTimeInMillis() + NB_MS_BY_HALF_HOUR);
 
         if (startHour > calDebut.get(Calendar.HOUR_OF_DAY)) {
             startHour = calDebut.get(Calendar.HOUR_OF_DAY);
         } else if (endHour <= calFin.get(Calendar.HOUR_OF_DAY)) {
             if (calFin.get(Calendar.MINUTE) < TRENTE) {
                 endHour = calFin.get(Calendar.HOUR_OF_DAY);
             } else {
                 endHour = calFin.get(Calendar.HOUR_OF_DAY) + 1;
             }
         }
 
         ExtendedEntry entry = new ExtendedEntry();
         entry.setId(Long.toString(calDebut.getTimeInMillis()));
         entry.setStartTime(calDebut.getTime());
         entry.setEndTime(calFin.getTime());
 
         SimpleDateFormat sdf = new SimpleDateFormat(Constantes.HOUR_FORMAT);
         String titre = " e " + sdf.format(calDebut.getTime());
         int nbLibre = calendarRdv.getCountNbPlaceLibre(calDebut.getTime());
         if (nbLibre == 0) {
             titre = "Aucune place libre " + titre;
         } else if (nbLibre == 1) {
             titre = "1 place libre " + titre;
         } else {
             titre = nbLibre + " places libres " + titre;
         }
 
         entry.setTitle(titre);
         entry.setDescription(
                 "Cliquez ici pour voir la liste des etudiants ayant rendez-vous e ce creneau.");
 
         if (calFin.getTime().before(new Date())) {
             entry.setColor("#ff8e8e");
         } else {
             entry.setColor("#b2ff88");
         }
 
         return entry;
     }
 
     /**
      * @return schduleModel
      */
     public ScheduleModel getWeekScheduleModel() {
         if (weekScheduleModel == null) {
             setWeekScheduleModel();
         }
         return weekScheduleModel;
     }
 
     /** * */
     public void annuler() {
 //		addedEntryMode = false;
         movedEntryMode = false;
     }
 
 	/*
 	 * ****************** ACCESSORS ********************
 	 */
 
     /**
      * @return actionEnum
      */
     public ActionEnum getActionEnum() {
         if (actionEnum == null) {
             actionEnum = new ActionEnum();
         }
         return actionEnum;
     }
 
     /**
      * @param actionEnum
      */
     public void setActionEnum(final ActionEnum actionEnum) {
         this.actionEnum = actionEnum;
     }
 
     /**
      * @return the calendarRdv
      */
     public CalendarRDV getCalendarRdv() {
         return calendarRdv;
     }
 
     /**
      * @param calendarRdv the calendarRdv to set
      */
     public void setCalendarRdv(final CalendarRDV calendarRdv) {
         this.calendarRdv = calendarRdv;
     }
 
     /**
      * @param date
      * @return date transformed
      */
     private Date transformHour(final Date date) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
 
         if (cal.get(Calendar.MINUTE) < TRENTE) {
             cal.set(Calendar.MINUTE, 0);
         } else {
             cal.set(Calendar.MINUTE, TRENTE);
         }
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         return cal.getTime();
     }
 
     /**
      * Action clicked on schedule day.
      */
     public void scheduleAction() {
         log.info("Dans scheduleAction");
 
         if (weekScheduleModel.isEntrySelected()) {
             dateSelect = transformHour(weekScheduleModel.getSelectedEntry().getStartTime());
 
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G HH:mm:ss z");
             log.info(sdf.format(dateSelect));
         }
     }
 
     /**
      * Listener when schedule is clicked.
      *
      * @param event
      */
     public void scheduleClicked(final ScheduleMouseEvent event) {
         log.info("ScheduleMouseEvent " + event.getEventType());
 
         if (event.getEventType() == ScheduleMouseEvent.SCHEDULE_BODY_CLICKED) {
             weekScheduleModel.setSelectedDate(event.getClickedTime());
         } else if (event.getEventType() == ScheduleMouseEvent.SCHEDULE_HEADER_CLICKED) {
             dateSelect = transformHour(event.getClickedTime());
             weekScheduleModel.setSelectedDate(dateSelect);
         }
     }
 
     /**
      * @return the day as String (
      */
     public String getSelectedDay() {
         SimpleDateFormat sdf = new SimpleDateFormat(Constantes.DATE_COMPLETE_FORMAT);
         if (weekScheduleModel.getSelectedEntry() != null) {
             return sdf.format(weekScheduleModel.getSelectedEntry().getStartTime());
         }
         return sdf.format(weekScheduleModel.getSelectedDate());
     }
 
     /**
      * Listener link before (Month/WorkWeek/Day).
      *
      * @param event
      */
     public void before(final ActionEvent event) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(weekScheduleModel.getSelectedDate());
 
         if (weekScheduleModel.getMode() == ScheduleModel.WORKWEEK) {
             cal.add(Calendar.WEEK_OF_MONTH, -1);
             weekScheduleModel.setSelectedDate(cal.getTime());
             weekScheduleModel.refresh();
         }
         dateSelect = cal.getTime();
     }
 
     /**
      * Listener link next (Month/WorkWeek/Day).
      *
      * @param event
      */
     public void next(final ActionEvent event) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(weekScheduleModel.getSelectedDate());
 
         if (weekScheduleModel.getMode() == ScheduleModel.WORKWEEK) {
             cal.add(Calendar.WEEK_OF_MONTH, 1);
             weekScheduleModel.setSelectedDate(cal.getTime());
             weekScheduleModel.refresh();
         }
         dateSelect = cal.getTime();
     }
 
     /**
      * @return the movedEntryMode
      */
     public boolean isMovedEntryMode() {
         return movedEntryMode;
     }
 
     /**
      * @return the idEntrySelected
      */
     public String getIdEntrySelected() {
         return idEntrySelected;
     }
 
     /**
      * @param idEntrySelected the idEntrySelected to set
      */
     public void setIdEntrySelected(final String idEntrySelected) {
         this.idEntrySelected = new String(idEntrySelected);
     }
 
     /**
      * @return the startHour
      */
     public int getStartHour() {
         return startHour;
     }
 
     /**
      * @return the endHour
      */
     public int getEndHour() {
         return endHour;
     }
 
     /**
      * Generate a CSV of the list of student.
      *
      * @return String
      */
     public String csvGeneration() {
         // key ligne value vlaue list
         Map<Integer, List<String>> mapCsv = new HashMap<Integer, List<String>>();
         Integer counter = 0;
         mapCsv.put(counter, HEADER_CVS);
 
         Set<Date> dates = new HashSet<Date>();
         if (getActionEnum().getWhatAction().equals(getActionEnum().getEmptyAction())) {
             dates.addAll(calendarRdv.getCandidatsAsMap().keySet());
         } else {
             Calendar cald = new GregorianCalendar();
             Calendar calSelectDate = new GregorianCalendar();
             calSelectDate.setTime(dateSelect);
             int a = calSelectDate.get(Calendar.YEAR);
             int m = calSelectDate.get(Calendar.MONTH);
             int j = calSelectDate.get(Calendar.DAY_OF_MONTH);
             for (Date d : calendarRdv.getCandidatsAsMap().keySet()) {
                 cald.setTime(d);
                 if (a == cald.get(Calendar.YEAR) && m == cald.get(Calendar.MONTH)
                         && j == cald.get(Calendar.DAY_OF_MONTH)) {
                     dates.add(d);
                 }
             }
         }
 
         for (Date date : dates) {
             Set<Individu> listKey = calendarRdv.getCandidatsAsMap().get(date).keySet();
             for (Individu ind : listKey) {
                 List<String> ligne = new ArrayList<String>();
                 ++counter;
 
                 //Commission_CGE_VersionEtape
                 if (calendarRdv.getCodeCge() != null) {
                     ligne.add(calendarRdv.getCodeCge());
                 } else if (calendarRdv.getCommissions() != null) {
                     StringBuffer listComms = new StringBuffer();
                     for (Commission comm : calendarRdv.getCommissions()) {
                         listComms.append(comm.getLibelle() + " - ");
                     }
                     ligne.add(listComms.toString());
                 } else if (calendarRdv.getVets() != null) {
                     String listVets = "";
                     for (VetCalendar vet : calendarRdv.getVets()) {
                         listVets += vet.getLibelle() + " - ";
                     }
                     ligne.add(listVets);
                 }
                 //Date_RDV
                 ligne.add(date.toString());
                 //Num_Dos_OPI
                 ligne.add(ind.getNumDossierOpi());
                 //Nom_Patronymique
                 ligne.add(ind.getNomPatronymique());
                 //Prenom
                 ligne.add(ind.getPrenom());
                 //Date_Naiss
                 ligne.add("" + ind.getDateNaissance());
 
                 //Telephone
                 //init hib proxy adresse
                 getDomainService().initOneProxyHib(ind,
                         ind.getAdresses(), Adresse.class);
 
                 ligne.add(ExportUtils.isNotNull(
                         ind.getAdresses().get(Constantes.ADR_FIX)
                                 .getPhoneNumber()));
                 //Mail
                 ligne.add(ExportUtils.isNotNull(ind.getAdressMail()));
 
                 //Voeux_confirmes et Autres_voeux
                 String vRdv = "";
                 String vows = "";
                 String others = "";
                 for (IndVoeu voeu : ind.getVoeux()) {
                     //init hib proxy adresse
                     getDomainService().initOneProxyHib(voeu.getLinkTrtCmiCamp(),
                             voeu.getLinkTrtCmiCamp().getTraitementCmi(), TraitementCmi.class);
 
                     TraitementCmi trtCmi = voeu.getLinkTrtCmiCamp().getTraitementCmi();
                     if (voeu.getState().equals(EtatConfirme.I18N_STATE)) {
                         vows += trtCmi.getVersionEtpOpi().getCodEtp() + "-"
                                 + trtCmi.getVersionEtpOpi().getCodVrsVet() + " ";
                     } else {
                         others += trtCmi.getVersionEtpOpi().getCodEtp() + "-"
                                 + trtCmi.getVersionEtpOpi().getCodVrsVet() + " ";
                     }
                     for (IndividuDate indDate
                             : calendarRdv.getCandidatsAsMap().get(date).get(ind)) {
                         if (voeu.getId().equals(indDate.getVoeu().getId())) {
                             vRdv += trtCmi.getVersionEtpOpi().getCodEtp() + "-"
                                     + trtCmi.getVersionEtpOpi().getCodVrsVet() + " ";
                         }
                     }
                 }
                 //Voeux_du_RDV
                 ligne.add(vRdv);
                 //Voeux_confirmes_of_calendarRdv
                 ligne.add(vows);
                 //Autres_voeux
                 ligne.add(others);
 
                 //Ajout de la ligne dans mapCsv
                 if (ligne.size() != HEADER_CVS.size()) {
                     throw new ConfigException("Construction du csv RDV : "
                             + "le nombre de colonne par ligne ("
                             + ligne.size() + ")est different "
                             + "que celui du header("
                             + HEADER_CVS.size() + ")(method csvGeneration in "
                             + getClass().getName() + " )");
                 }
 
                 mapCsv.put(counter, ligne);
             }
         }
 
         ExportUtils.csvGenerate(mapCsv, "Rendez-vous.csv");
         return null;
     }
 
     /**
      * @return the inSearch
      */
     public boolean isInSearch() {
         return inSearch;
     }
 
     /**
      * @param inSearch the inSearch to set
      */
     public void setInSearch(final boolean inSearch) {
         this.inSearch = inSearch;
     }
 
     /**
      * @return the individuPaginator
      */
     public IndividuPaginator getIndividuPaginator() {
         return individuPaginator;
     }
 
     /**
      * @param individuPaginator the individuPaginator to set
      */
     public void setIndividuPaginator(final IndividuPaginator individuPaginator) {
         this.individuPaginator = individuPaginator;
     }
 
     /**
      * @return the candidat
      */
     public Individu getCandidat() {
         return candidat;
     }
 
     /**
      * @param candidat the candidat to set
      */
     public void setCandidat(final Individu candidat) {
         this.candidat = candidat;
     }
 
     /**
      *
      */
     public void moveEntry() {
         movedEntryMode = true;
         if (weekScheduleModel != null && weekScheduleModel.getSelectedEntry() != null) {
             setIdEntrySelected(weekScheduleModel.getSelectedEntry().getId());
         } else {
             setIdEntrySelected(Long.toString(dateSelect.getTime()));
         }
         addInfoMessage("agentEditCalendrierForm:moveEntryMessage",
                 "CALENDAR_RDV.MESSAGES.MOVE_ENTRY");
     }
 
     /**
      * @return the uiSchedule
      */
     public UISchedule getUiSchedule() {
         return uiSchedule;
     }
 
     /**
      * @param uiSchedule the uiSchedule to set
      */
     public void setUiSchedule(final UISchedule uiSchedule) {
         this.uiSchedule = uiSchedule;
     }
 
     /**
      * Search method to list the students with the filter.
      */
     public void searchStudents() {
        //individuPaginator.filtreRechercheEtudiants();
     }
 }
