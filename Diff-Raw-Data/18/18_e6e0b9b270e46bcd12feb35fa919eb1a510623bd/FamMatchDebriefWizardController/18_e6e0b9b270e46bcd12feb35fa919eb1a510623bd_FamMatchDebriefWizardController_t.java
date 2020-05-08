 package org.fam.jsf.controller;
 
 import lombok.Getter;
 import org.fam.common.cdi.Loggable;
 import org.fam.common.cdi.LoggedIn;
 import org.fam.common.cdi.Player;
 import org.fam.ejb.model.FamAnswer;
 import org.fam.ejb.model.FamCard;
 import org.fam.ejb.model.FamGoal;
 import org.fam.ejb.model.FamMatch;
 import org.fam.ejb.model.FamMatchPlayer;
 import org.fam.ejb.model.FamMatchTeam;
 import org.fam.ejb.model.FamPlayer;
 import org.fam.ejb.model.FamSubstitution;
 import org.fam.ejb.model.FamTeam;
 import org.fam.ejb.model.FamUser;
 import org.fam.ejb.session.FamAnswerFacade;
 import org.fam.ejb.session.FamCardFacade;
 import org.fam.ejb.session.FamEventFacade;
 import org.fam.ejb.session.FamGoalFacade;
 import org.fam.ejb.session.FamMatchFacade;
 import org.fam.ejb.session.FamMatchPlayerFacade;
 import org.fam.ejb.session.FamMatchTeamFacade;
 import org.fam.ejb.session.FamSubstitutionFacade;
 import org.fam.ejb.session.FamTeamFacade;
 import org.fam.jsf.bean.TeamComposition;
 import org.fam.jsf.bean.util.JsfUtil;
 import org.fam.jsf.cache.CacheBean;
 import org.fam.jsf.cache.CachePlayer;
 import org.primefaces.event.FlowEvent;
 import org.primefaces.event.SelectEvent;
 import org.primefaces.event.UnselectEvent;
 import org.slf4j.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.validation.constraints.NotNull;
 import java.util.ArrayList;
 import java.util.List;
 
 @ManagedBean(name = "famMatchDebriefWizardController")
 @ViewScoped
 @Loggable
 @Getter
 public class FamMatchDebriefWizardController extends AbstractController<FamMatch> {
 
     @Inject
     Logger LOGGER;
     @Inject
     private FamMatchFacade ejbFacade;
     @Inject
     private FamAnswerFacade ejbAnswer;
     @Inject
     private FamTeamFacade ejbTeam;
     @Inject
     private FamGoalFacade ejbGoal;
     @Inject
     private FamCardFacade ejbCard;
     @Inject
     private FamSubstitutionFacade ejbSub;
     @Inject
     private FamEventFacade ejbEvent;
     @Inject
     private FamMatchTeamFacade ejbMatchTeam;
     @Inject
     private FamMatchPlayerFacade ejbMatchPlayer;
     //
     @Inject
     private CacheBean cacheBean;
     @Inject
     private CachePlayer cachePlayer;
     //
     @Inject
     @LoggedIn
     private FamUser currentUser;
     @Inject
     @Player
     private FamPlayer currentPlayer;
     //
     private FamMatchTeam matchTeamHome = new FamMatchTeam();
     private FamMatchTeam matchTeamAway = new FamMatchTeam();
 
     //
     private Long idTeam;
     private FamTeam famTeam;
     //
     private List<TeamComposition> lstTeamComposition;
 
     @NotNull
     private TeamComposition teamComposition;
     private List<FamMatchPlayer> famMatchPlayerList = new ArrayList<FamMatchPlayer>();
 
     //    private List<FamAnswer> lstYes;
 //    private List<FamAnswer> lstNo;
 //    private List<FamAnswer> lstMaybe;
 //    private List<FamPlayer> lstPlayer;
 //    private FamAnswer[] selectedYes;
 //    private List<FamAnswer> selectedNo;
 //    private List<FamAnswer> selectedMaybe;
 //    private FamPlayer[] selectedPlayers;
 //    //
 //    private FamPlayer selectedPlayer;
 //    private List<FamPlayer> players;
 //    private List<FamPlayer> preselectedLst;
     //
     private List<FamGoal> lstGoal;
     private FamGoal famGoal;
     private List<FamCard> lstCard;
     private FamCard famCard;
     private List<FamSubstitution> lstSubstitution;
     private FamSubstitution famSubstitution;
     //
     private FamMatchTeam famMatchTeam;
    private FamMatchPlayer selectedFamMatchPlayer = new FamMatchPlayer();
     //
     private int nbTit;
     private int nbSub;
     private int nbPlayers;
 
     private UIComponent userListComponent = null;
     private UIComponent selectedComponent = null;
 
     private boolean skip;
 
 //    //
 //    private FamPlayerDataModel playerDM;
 
     public FamMatchDebriefWizardController() {
     }
 
     @PostConstruct
     private void postConstruct() {
 
     }
 
     @PreDestroy
     private void preDestroy() {
 
     }
 
     @Override
     public FamMatch getSelected() {
         if (current == null) {
             current = new FamMatch();
             selectedItemIndex = -1;
         }
         return current;
     }
 
     @Override
     public FamMatchFacade getFacade() {
         return ejbFacade;
     }
 
 //    @Override
 //    public String prepareCreate() {
 //        current = new FamMatch();
 //        matchTeamHome = new FamMatchTeam();
 //        matchTeamAway = new FamMatchTeam();
 //        matchTeamHome.setHome(Boolean.TRUE);
 //        matchTeamAway.setHome(Boolean.FALSE);
 //
 //        famEvent = ejbEvent.newEvent();
 //        famEvent.setFamTypEvent(cacheBean.getTypEventMatch());
 //
 //        selectedItemIndex = -1;
 //        return "pretty:createMatch";
 //    }
 //
 //    @Override
 //    public String create() {
 //        try {
 //            StringBuilder sb = new StringBuilder();
 //            sb.append(matchTeamHome.getFamTeam().getLibTeam()).append(" - ").append(matchTeamAway.getFamTeam().getLibTeam());
 //            famEvent.setLibEvent(sb.toString());
 //            List<FamTeam> teams = new ArrayList<FamTeam>();
 //            teams.add(matchTeamHome.getFamTeam());
 //            teams.add(matchTeamAway.getFamTeam());
 //            famEvent.setFamTeamList(teams);
 //            current.setFamEvent(famEvent);
 //
 //            List<FamMatchTeam> mt = new ArrayList<FamMatchTeam>();
 //            mt.add(matchTeamHome);
 //            mt.add(matchTeamAway);
 //            current.setFamMatchTeamList(mt);
 //
 //            getFacade().create(current);
 //            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FamMatchCreated"));
 //            return prepareView();
 //        } catch (Exception e) {
 //            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
 //            return null;
 //        }
 //    }
 
     @Override
     public String prepareEdit() {
         loadForEdit();
         id = current.getIdMatch();
         return "pretty:editMatch";
     }
 
     public void loadForEdit() {
         super.loadAction();
 
 
     }
 
     @Override
     public String prepareView() {
         id = current.getIdMatch();
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("matchId " + id);
         }
         return "pretty:viewMatch";
     }
 
     @Override
     public String prepareList() {
         return "pretty:listMatch";
     }
 
     public String prepareCompose() {
         id = current.getIdMatch();
         return "pretty:composeMatch";
     }
 
     public String prepareDebrief() {
         id = current.getIdMatch();
         return "pretty:debriefMatch";
     }
 
     public String onFlowProcess(FlowEvent event) {
         String currentStepId = event.getOldStep();
         String stepToGo = event.getNewStep();
         LOGGER.info("Current wizard step:" + currentStepId);
         LOGGER.info("Next step:" + stepToGo);
 
 
         if (skip && currentStepId.equals("tabCard")) {
 //            return "confirm";
             return stepToGo;
         } else {
             return stepToGo;
         }
 
 //        if (skip) {
 //            skip = false;   //reset in case user goes back
 //            return "confirm";
 //        } else {
 //            return event.getNewStep();
 //        }
     }
 
 
     public void loadForDebrief() {
         famTeam = famMatchTeam.getFamTeam();
         famGoal = new FamGoal();
         famCard = new FamCard();
         famSubstitution = new FamSubstitution();
 
         lstGoal = ejbGoal.findByMatchAndTeam(current, famTeam);
         lstCard = ejbCard.findByMatchAndTeam(current, famTeam);
         lstSubstitution = ejbSub.findByMatchAndTeam(current, famTeam);
 
     }
 
     public String saveScore() {
         return null;
     }
 
 
     public String addGoal() {
         lstGoal.add(famGoal);
         famGoal = new FamGoal();
         return null;
     }
 
     public String addCard() {
         lstCard.add(famCard);
         famCard = new FamCard();
         return null;
     }
 
     public String addSubstitution() {
         lstSubstitution.add(famSubstitution);
         famSubstitution = new FamSubstitution();
         return null;
     }
 
     public void save(ActionEvent actionEvent) {
         //Persist user
         saveLineup();
 //        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + user.getFirstname());
 //        FacesContext.getCurrentInstance().addMessage(null, msg);
     }
 
     public String saveLineup() {
         LOGGER.debug("saveLineup");
         try {
             for (FamMatchPlayer mp : famMatchTeam.getFamMatchPlayerList()) {
                 if (mp.getFamPlayer() != null) {
                     ejbMatchPlayer.edit(mp);
                 }
             }
 
 //            ejbMatchTeam.edit(famMatchTeam);
             JsfUtil.addSuccessMessage("Enregistré");
         } catch (Exception e) {
             LOGGER.error("saveLineup", e);
             JsfUtil.addErrorMessage(e.getMessage(), "saveLineup");
         }
         return "pretty:";
     }
 
     @Override
     public void onRowSelect(SelectEvent event) {
         String msg = "";
         if (event.getObject() instanceof FamAnswer) {
             msg = ((FamAnswer) event.getObject()).getFamPlayer().getDisplayName();
         } else if (event.getObject() instanceof FamPlayer) {
             msg = ((FamPlayer) event.getObject()).getDisplayName();
         }
 
         JsfUtil.addInfoMessage("RowSelect", msg);
     }
 
     @Override
     public void onRowUnselect(UnselectEvent event) {
         String msg = "";
         if (event.getObject() instanceof FamAnswer) {
             msg = ((FamAnswer) event.getObject()).getFamPlayer().getDisplayName();
         } else if (event.getObject() instanceof FamPlayer) {
             msg = ((FamPlayer) event.getObject()).getDisplayName();
         }
 
         JsfUtil.addInfoMessage("UnSelect", msg);
     }
 
     public void setEjbFacade(FamMatchFacade ejbFacade) {
         this.ejbFacade = ejbFacade;
     }
 
     public void setEjbAnswer(FamAnswerFacade ejbAnswer) {
         this.ejbAnswer = ejbAnswer;
     }
 
     public void setEjbTeam(FamTeamFacade ejbTeam) {
         this.ejbTeam = ejbTeam;
     }
 
     public void setEjbGoal(FamGoalFacade ejbGoal) {
         this.ejbGoal = ejbGoal;
     }
 
     public void setEjbCard(FamCardFacade ejbCard) {
         this.ejbCard = ejbCard;
     }
 
     public void setEjbSub(FamSubstitutionFacade ejbSub) {
         this.ejbSub = ejbSub;
     }
 
     public void setEjbEvent(FamEventFacade ejbEvent) {
         this.ejbEvent = ejbEvent;
     }
 
     public void setEjbMatchTeam(FamMatchTeamFacade ejbMatchTeam) {
         this.ejbMatchTeam = ejbMatchTeam;
     }
 
     public void setEjbMatchPlayer(FamMatchPlayerFacade ejbMatchPlayer) {
         this.ejbMatchPlayer = ejbMatchPlayer;
     }
 
     public void setCacheBean(CacheBean cacheBean) {
         this.cacheBean = cacheBean;
     }
 
     public void setCachePlayer(CachePlayer cachePlayer) {
         this.cachePlayer = cachePlayer;
     }
 
     public void setCurrentUser(FamUser currentUser) {
         this.currentUser = currentUser;
     }
 
     public void setCurrentPlayer(FamPlayer currentPlayer) {
         this.currentPlayer = currentPlayer;
     }
 
     public void setMatchTeamHome(FamMatchTeam matchTeamHome) {
         this.matchTeamHome = matchTeamHome;
     }
 
     public void setMatchTeamAway(FamMatchTeam matchTeamAway) {
         this.matchTeamAway = matchTeamAway;
     }
 
     public void setIdTeam(Long idTeam) {
         this.idTeam = idTeam;
     }
 
     public void setFamTeam(FamTeam famTeam) {
         this.famTeam = famTeam;
     }
 
     public void setLstTeamComposition(List<TeamComposition> lstTeamComposition) {
         this.lstTeamComposition = lstTeamComposition;
     }
 
     public void setTeamComposition(TeamComposition teamComposition) {
         this.teamComposition = teamComposition;
     }
 
     public void setFamMatchPlayerList(List<FamMatchPlayer> famMatchPlayerList) {
         this.famMatchPlayerList = famMatchPlayerList;
     }
 
     public void setLstGoal(List<FamGoal> lstGoal) {
         this.lstGoal = lstGoal;
     }
 
     public void setFamGoal(FamGoal famGoal) {
         this.famGoal = famGoal;
     }
 
     public void setLstCard(List<FamCard> lstCard) {
         this.lstCard = lstCard;
     }
 
     public void setFamCard(FamCard famCard) {
         this.famCard = famCard;
     }
 
     public void setLstSubstitution(List<FamSubstitution> lstSubstitution) {
         this.lstSubstitution = lstSubstitution;
     }
 
     public void setFamSubstitution(FamSubstitution famSubstitution) {
         this.famSubstitution = famSubstitution;
     }
 
     public void setFamMatchTeam(FamMatchTeam famMatchTeam) {
         this.famMatchTeam = famMatchTeam;
     }
 
     public void setSelectedFamMatchPlayer(FamMatchPlayer selectedFamMatchPlayer) {
         this.selectedFamMatchPlayer = selectedFamMatchPlayer;
     }
 
     public void setNbTit(int nbTit) {
         this.nbTit = nbTit;
     }
 
     public void setNbSub(int nbSub) {
         this.nbSub = nbSub;
     }
 
     public void setNbPlayers(int nbPlayers) {
         this.nbPlayers = nbPlayers;
     }
 
     public void setUserListComponent(UIComponent userListComponent) {
         this.userListComponent = userListComponent;
     }
 
     public void setSelectedComponent(UIComponent selectedComponent) {
         this.selectedComponent = selectedComponent;
     }
 
     public void setSkip(boolean skip) {
         this.skip = skip;
     }
 }
