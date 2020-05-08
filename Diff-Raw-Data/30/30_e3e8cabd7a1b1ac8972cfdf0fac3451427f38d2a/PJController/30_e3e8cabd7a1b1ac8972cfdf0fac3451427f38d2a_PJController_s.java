 package org.esupportail.opi.web.controllers.opinions;
 
 import fj.F;
 import fj.F2;
 import fj.F5;
 import fj.P2;
 import fj.data.Stream;
 import org.esupportail.commons.services.smtp.SmtpService;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.NormeSI;
 import org.esupportail.opi.domain.beans.etat.Etat;
 import org.esupportail.opi.domain.beans.etat.EtatArriveComplet;
 import org.esupportail.opi.domain.beans.etat.EtatArriveIncomplet;
 import org.esupportail.opi.domain.beans.etat.EtatVoeu;
 import org.esupportail.opi.domain.beans.parameters.Transfert;
 import org.esupportail.opi.domain.beans.references.commission.Commission;
 import org.esupportail.opi.domain.beans.user.Individu;
 import org.esupportail.opi.domain.beans.user.candidature.IndVoeu;
 import org.esupportail.opi.domain.beans.user.candidature.MissingPiece;
 import org.esupportail.opi.web.beans.beanEnum.ActionEnum;
 import org.esupportail.opi.web.beans.paginator.PMPaginator;
 import org.esupportail.opi.web.beans.parameters.FormationContinue;
 import org.esupportail.opi.web.beans.parameters.FormationInitiale;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.*;
 import org.esupportail.opi.web.beans.utils.NavigationRulesConst;
 import org.esupportail.opi.web.beans.utils.Utilitaires;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.opi.web.controllers.AbstractContextAwareController;
 import org.esupportail.opi.web.controllers.references.CommissionController;
 import org.esupportail.opi.web.controllers.user.IndividuController;
 import org.esupportail.opi.web.utils.paginator.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import javax.faces.model.SelectItem;
 import java.util.*;
 
 import static org.esupportail.opi.web.utils.fj.Conversions.individuToPojo;
 import static org.esupportail.opi.web.utils.paginator.LazyDataModel.lazyModel;
 
 /**
  * @author tducreux
  *         TypeTraitController :
  */
 public class PJController extends AbstractContextAwareController {
 

	/*
     ******************* PROPERTIES ******************* */


     /**
      * serialization.
      */
     private static final long serialVersionUID = -4483704731784087003L;
 
 
     /**
      * The current CommissionPojo.
      */
     private CommissionPojo currentCmiPojo;
 
     /**
      * true if all missing piece are selected.
      */
     private Boolean allChecked;
 
     /**
      * Select all the state idem of stateSelected.
      */
     private String stateSelected;
 
     /**
      * Missing piece selected.
      */
     private Object[] missingPiece;
 
     /**
      * MissPiece just save.
      */
     private Set<MissingPiece> missPieceForInd;
 
     /**
      * see {@link ActionEnum}.
      */
     private ActionEnum actionEnum;
 
     /**
      * see {@link IndividuController}.
      */
     private IndividuController individuController;
 
     /**
      * see {@link CommissionController}.
      */
     private CommissionController commissionController;
 
 
     /**
      * see {@link SmtpService}.
      */
     private SmtpService smtpService;
 
     /**
      * Injected.
      */
     private PMPaginator paginatorPM;
 
     /**
      * see {@link Transfert}.
      */
     private Transfert transfert;
 
     /**
      * MissingPiecePojo selected.
      */
     private MissingPiecePojo mpPojoSelected;
 
     private LazyDataModel<MissingPiecePojo> missingPiecePojoLDM;
 
     private boolean renderTable = false;
 

	/*
	 ******************* INIT ************************* */

     /**
      * Constructors.
      */
    public PJController() {
        super();

    }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#reset()
      */
     @Override
     public void reset() {
         super.reset();
         allChecked = false;
         stateSelected = "";
         currentCmiPojo = null;
         missingPiece = new Object[0];
         actionEnum = new ActionEnum();
         missPieceForInd = new HashSet<MissingPiece>();
 
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractContextAwareController#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         super.afterPropertiesSetInternal();
         Assert.notNull(this.individuController,
                 "property individuController of class "
                         + this.getClass().getName() + " can not be null");
         Assert.notNull(this.smtpService,
                 "property smtpService of class "
                         + this.getClass().getName() + " can not be null");
         Assert.notNull(this.transfert,
                 "property transfert of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.commissionController,
                 "property commissionController of class " + this.getClass().getName()
                         + " can not be null");
         Assert.notNull(this.paginatorPM,
                 "property paginatorPM of class " + this.getClass().getName()
                         + " can not be null");
 
         missingPiecePojoLDM = individuController.getIndLDM().map(
                 individuToPojo(getDomainApoService(), getParameterService(), getI18nService()).andThen(
                         paginatorPM.indPojoToMPPojo()));
 
         reset();
     }
 
 	/*
 	 ******************* CALLBACK ********************** */
 
     /**
      * Callback to see the students in a commission for the PM treatment.
      *
      * @return String
      */
     @SuppressWarnings({"serial", "synthetic-access"})
     public String goSeePM() {
         reset();
         return NavigationRulesConst.DISPLAY_PIECE_MANQUANTE_STUDENTS;
     }
 
     /**
      * Callback to enter the States of PM for all the chose of an student.
      *
      * @return String
      */
     public String goSeePMEtudiant() {
         return NavigationRulesConst.ENTER_PIECE_MANQUANTE_STUDENT;
     }
 
     /**
      * Back to student list.
      *
      * @return String
      */
     public String goBackMPStudent() {
         reset();
         return NavigationRulesConst.DISPLAY_PIECE_MANQUANTE_STUDENTS;
     }
 
 	/*
 	 ******************* METHODS ********************** */
 
     /**
      * search Students.
      */
     @SuppressWarnings({"serial", "synthetic-access"})
     public void searchStudents() {
         reset();
        this.paginatorPM.filterInMannagedCmi(
                new TreeSet<Commission>(new ComparatorString(NormeSI.class)) {{
                    addAll(commissionController.getCommissionsItemsByRight());
                }},
                transfert.getCode(), false);
        paginatorPM.forceReload();
     }
 
     /**
      * items state for the wishes.
      *
      * @return List< SelectItem>
      */
     public List<SelectItem> getStateItems() {
         List<SelectItem> list = new ArrayList<SelectItem>();
         list.add(new SelectItem("STATE.NON_ARRIVE",
                 getString("STATE.NON_ARRIVE")));
         list.add(new SelectItem("STATE.ARRIVE_COMPLET",
                 getString("STATE.ARRIVE_COMPLET")));
         list.add(new SelectItem("STATE.ARRIVE_INCOMPLET",
                 getString("STATE.ARRIVE_INCOMPLET")));
         return list;
     }
 
     /**
      * Change all the state for the elements
      * Display in the paginator.
      */
     public void changeStateAll() {
         List<MissingPiecePojo> allPojo = this.paginatorPM.getMissingPiecePojos();
         for (MissingPiecePojo m : allPojo) {
             this.mpPojoSelected = m;
             if (m.getCommissions() != null) {
                 for (CommissionPojo c : m.getCommissions().keySet()) {
                     this.currentCmiPojo = c;
                     if (!currentCmiPojo.getStateCurrent().equals(
                             currentCmiPojo.getStateCurrentOld())) {
                         changeState(false, m.getIndividuPojo());
                     }
                 }
             }
         }
         addInfoMessage(null, "INFO.PM.ALL.SUCCESS");
         reset();
 //		paginatorPM.reset();
     }
 
     /**
      * change state for one individu.
      * from enterPM;
      */
     public void changeState() {
         changeState(true, mpPojoSelected.getIndividuPojo());
     }
 
     /**
      * Change the state for all wishes in one commission.
      *
      * @param trtUnit
      */
     private void changeState(final Boolean trtUnit, final IndividuPojo pojoIndividu) {
         currentCmiPojo.setStateCurrentOld(currentCmiPojo.getStateCurrent());
         currentCmiPojo.setState(
                 (EtatVoeu) Etat.instanceState(
                         currentCmiPojo.getStateCurrent(), getI18nService()));
 
         RegimeInscription regimeIns = getRegimeIns().get(
                 Utilitaires.getCodeRIIndividu(pojoIndividu.getIndividu(),
                         getDomainService()));
         if (currentCmiPojo.getState() instanceof EtatArriveIncomplet
                 && trtUnit) {
             // Arrive Incomplet
             seeMissingPiecePrincipal();
         } else if (currentCmiPojo.getState() instanceof EtatArriveComplet) {
             // Arrive Complet
             //delete all missing pieces
             Commission c = getParameterService()
                     .getCommission(currentCmiPojo.getCommission().getId(), null);
             currentCmiPojo.setCommission(c);
             List<MissingPiece> missP =
                     getDomainService().getMissingPiece(
                             pojoIndividu.getIndividu(), currentCmiPojo.getCommission());
             if (missP != null && !missP.isEmpty()) {
                 getDomainService().deleteMissingPiece(missP, null);
             }
 
             //send Mail if FI ou FC && not(trtUnit)
             if (regimeIns instanceof FormationContinue && trtUnit) {
                 actionEnum.setWhatAction(ActionEnum.SEND_MAIL);
             } else {
                 sendMail(false);
             }
 
 
         }
         //update state all wishes in cmi.
         List<IndVoeuPojo> iList = this.mpPojoSelected.getCommissions().get(currentCmiPojo);
 
         for (IndVoeuPojo i : iList) {
             i.getIndVoeu().setState(currentCmiPojo.getStateCurrent());
             IndVoeu iV = (IndVoeu) getDomainService().update(i.getIndVoeu(), getCurrentGest().getLogin());
             getDomainService().updateIndVoeu(iV);
         }
         if (regimeIns instanceof FormationInitiale
                 && !(currentCmiPojo.getState() instanceof EtatArriveIncomplet)) {
             reset();
         }
     }
 
     /**
      * see the missing pieces if state = EtatArriveIncomplet.
      */
     public void seeMissingPiecePrincipal() {
         IndividuPojo pojoIndividu = this.mpPojoSelected.getIndividuPojo();
 
         if (currentCmiPojo.getState()
                 instanceof EtatArriveIncomplet) {
             List<MissingPiece> missP =
                     getDomainService().getMissingPiece(
                             pojoIndividu.getIndividu(), currentCmiPojo.getCommission());
             if (missP != null) {
                 missingPiece = missP.toArray();
             }
         } else {
             addInfoMessage(null, "MISSING_PIECE.NOT_EMPTY_STATE", getString("STATE.ARRIVE_INCOMPLET"));
         }
     }
 
 
     /**
      * Update the state in all the wishes of the commissions.
      */
     public void saveMissingPiecePrincipal() {
         List<MissingPiece> listMPToDelete = new ArrayList<MissingPiece>();
         if (allChecked) {
             missPieceForInd = new HashSet<MissingPiece>(
                     this.mpPojoSelected.getPiecesForCmi().get(currentCmiPojo.getCommission()));
         } else {
             //TODO probleme de lazy aleatoire a voir 01042009
             for (Object o : missingPiece) {
                 MissingPiece mp = (MissingPiece) o;
                 missPieceForInd.add(mp);
             }
             for (MissingPiece mp
                     : this.mpPojoSelected.getPiecesForCmi().get(currentCmiPojo.getCommission())) {
                 //missing piece already in dataBase
                 if (!mp.getId().equals(0)) {
                     if (!missPieceForInd.contains(mp)) {
                         //if not contains --> delete missingPiece
                         listMPToDelete.add(mp);
                     }
                 }
             }
         }
 
         if (!missPieceForInd.isEmpty()) {
             getDomainService().saveOrUpdateMissingPiece(
                     new ArrayList<MissingPiece>(missPieceForInd),
                     getCurrentGest().getLogin());
         }
 
         //DELETE THE MP NOT SELECTED
         if (!listMPToDelete.isEmpty()) {
             getDomainService().deleteMissingPiece(listMPToDelete, null);
         }
 
         actionEnum.setWhatAction(ActionEnum.SEND_MAIL);
         addInfoMessage(null, "MISSING_PIECE.SAVE_OK");
 
     }
 
 
     /**
      * Change all state of visibleItems for stateSelected.
      */
     public void putStateAll() {
         for (MissingPiecePojo mPojo : missingPiecePojoLDM.getData()) {
             for (CommissionPojo c : mPojo.getCommissions().keySet()) {
                 c.setStateCurrent(stateSelected);
             }
         }
     }
 
     /**
      * Send mail when confirm.
      */
     public void sendMail() {
         sendMail(true);
     }
 
 
     /**
      * send mail.
      */
     public void sendMail(final Boolean doReset) {
         //send Mail
         IndividuPojo pojoIndividu = this.mpPojoSelected.getIndividuPojo();
 
         RegimeInscription regimeIns = getRegimeIns().get(
                 Utilitaires.getCodeRIIndividu(pojoIndividu.getIndividu(),
                         getDomainService()));
         if (currentCmiPojo.getState() instanceof EtatArriveIncomplet) {
             if (regimeIns.getDossArriveIncomplet() != null) {
                 currentCmiPojo.setAdressePojo(new AdressePojo(currentCmiPojo.getCommission()
                         .getContactsCommission()
                         .get(regimeIns.getCode()).getAdresse(), getDomainApoService()));
                 currentCmiPojo.setContactCommission(currentCmiPojo.getCommission().
                         getContactsCommission().get(regimeIns.getCode()));
                 List<Object> list = new ArrayList<Object>();
                 list.add(pojoIndividu.getIndividu());
                 list.add(this.mpPojoSelected.getCommissions().get(currentCmiPojo));
                 list.add(missPieceForInd);
                 list.add(currentCmiPojo);
 
                 regimeIns.getDossArriveIncomplet().send(pojoIndividu.getIndividu().getAdressMail(),
                         pojoIndividu.getIndividu().getEmailAnnuaire(), list);
             }
 
             addInfoMessage(null, "INFO.CANDIDAT.SEND_MAIL.MISSING_PIECE");
         } else if (currentCmiPojo.getState() instanceof EtatArriveComplet) {
             if (regimeIns.getDossArriveComplet() != null) {
                 currentCmiPojo.setAdressePojo(new AdressePojo(
                         currentCmiPojo.getCommission().getContactsCommission().
                                 get(regimeIns.getCode()).getAdresse(), getDomainApoService()));
                 currentCmiPojo.setContactCommission(currentCmiPojo.getCommission().
                         getContactsCommission().get(regimeIns.getCode()));
                 List<Object> list = new ArrayList<Object>();
                 list.add(pojoIndividu.getIndividu());
                 list.add(this.mpPojoSelected.getCommissions().get(currentCmiPojo));
                 list.add(currentCmiPojo);
 
                 regimeIns.getDossArriveComplet().send(pojoIndividu.getIndividu().getAdressMail(),
                         pojoIndividu.getIndividu().getEmailAnnuaire(), list);
             }
             addInfoMessage(null, "INFO.CANDIDAT.SEND_MAIL.STATE.FULL_ARRIVE",
                     pojoIndividu.getIndividu().getNumDossierOpi());
 
         }
         if (doReset) {
             reset();
         }
 
     }
 	
 
 	/*
 	 ******************* ACCESSORS ******************** */
 
     /**
      * @return the currentCmiPojo
      */
     public CommissionPojo getCurrentCmiPojo() {
         return currentCmiPojo;
     }
 
     /**
      * @param currentCmiPojo the currentCmiPojo to set
      */
     public void setCurrentCmiPojo(final CommissionPojo currentCmiPojo) {
         this.currentCmiPojo = currentCmiPojo;
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
      * @return the missingPiece
      */
     public Object[] getMissingPiece() {
         return missingPiece;
     }
 
     /**
      * @param missingPiece the missingPiece to set
      */
     public void setMissingPiece(final Object[] missingPiece) {
         this.missingPiece = missingPiece;
     }
 
     /**
      * @param individuController the individuController to set
      */
     public void setIndividuController(final IndividuController individuController) {
         this.individuController = individuController;
     }
 
     /**
      * @param smtpService the smtpService to set
      */
     public void setSmtpService(final SmtpService smtpService) {
         this.smtpService = smtpService;
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
      * @return the missPieceForInd
      */
     public Set<MissingPiece> getMissPieceForInd() {
         return missPieceForInd;
     }
 
     /**
      * @param missPieceForInd the missPieceForInd to set
      */
     public void setMissPieceForInd(final Set<MissingPiece> missPieceForInd) {
         this.missPieceForInd = missPieceForInd;
     }
 
 
     /**
      * @param paginatorPM the paginatorPM to set
      */
     public void setPaginatorPM(final PMPaginator paginatorPM) {
         this.paginatorPM = paginatorPM;
     }
 
     /**
      * @param commissionController the commissionController to set
      */
     public void setCommissionController(final CommissionController commissionController) {
         this.commissionController = commissionController;
     }
 
     /**
      * @param transfert the transfert to set
      */
     public void setTransfert(final Transfert transfert) {
         this.transfert = transfert;
     }
 
     /**
      * @return the mpPojoSelected
      */
     public MissingPiecePojo getMpPojoSelected() {
         return mpPojoSelected;
     }
 
     /**
      * @param mpPojoSelected the mpPojoSelected to set
      */
     public void setMpPojoSelected(final MissingPiecePojo mpPojoSelected) {
         this.mpPojoSelected = mpPojoSelected;
     }
 
     /**
      * @return the stateSelected
      */
     public String getStateSelected() {
         return stateSelected;
     }
 
     /**
      * @param stateSelected the stateSelected to set
      */
     public void setStateSelected(final String stateSelected) {
         this.stateSelected = stateSelected;
     }
 
     public LazyDataModel<MissingPiecePojo> getMissingPiecePojoLDM() {
         return missingPiecePojoLDM;
     }
 
     public boolean isRenderTable() {
         return renderTable;
     }
 
     public void setRenderTable(boolean renderTable) {
         this.renderTable = renderTable;
     }
 
     public void doRenderTable() {
         renderTable = true;
     }
 }
