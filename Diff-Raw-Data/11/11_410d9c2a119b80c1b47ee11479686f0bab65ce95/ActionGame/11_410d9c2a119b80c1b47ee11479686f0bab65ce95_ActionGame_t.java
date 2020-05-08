 package org.powertac.tourney.actions;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.powertac.tourney.beans.Agent;
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.constants.Constants;
 import org.powertac.tourney.services.HibernateUtil;
 import org.powertac.tourney.services.Utils;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @ManagedBean
 @RequestScoped
 public class ActionGame
 {
   private Game game;
   private List<String> gameInfo = new ArrayList<String>();
   private List<Map.Entry<String, Double>> resultMap = new ArrayList<Map.Entry<String, Double>>();
 
   public ActionGame()
   {
     loadData();
   }
 
   private void loadData ()
   {
     int gameId = getGameId();
     if (gameId < 1) {
       return;
     }
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = session.beginTransaction();
     try {
       Query query = session.createQuery(Constants.HQL.GET_GAME_BY_ID);
       query.setInteger("gameId", gameId);
       game = (Game) query.uniqueResult();
 
       if (game == null) {
         transaction.rollback();
         Utils.redirect();
         return;
       }
 
       loadGameInfo();
       loadResultMap();
       transaction.commit();
     }
     catch (Exception e) {
       transaction.rollback();
       e.printStackTrace();
     }
     finally {
       session.close();
     }
   }
 
   private int getGameId ()
   {
     FacesContext facesContext = FacesContext.getCurrentInstance();
     try {
       return Integer.parseInt(facesContext.getExternalContext().
           getRequestParameterMap().get("gameId"));
     }
     catch (NumberFormatException ignored) {
       Utils.redirect();
       return -1;
     }
   }
 
   private void loadGameInfo ()
   {
    gameInfo.add("Id : " + game.getGameId());
    gameInfo.add("Name : " + game.getGameName());
     gameInfo.add("Status : " + game.getStatus());
 
     gameInfo.add("Tournament : <a href=\"tournament.xhtml?tournamentId=" +
         + game.getTournament().getTournamentId() + "\">"
         + game.getTournament().getTournamentName() + "</a>");
 
     if (game.getMachine() != null) {
       gameInfo.add("Running on : " + game.getMachine().getMachineName());
       gameInfo.add("Viz Url : " + game.getMachine().getVizUrl());
     }
 
     gameInfo.add("StartTime : " + game.startTimeUTC());
     gameInfo.add("Real StartTime : " + game.readyTimeUTC());
     gameInfo.add("Location : " + game.getLocation());
     gameInfo.add("SimStartTime : " + game.getSimStartTime());
   }
 
   private void loadResultMap ()
   {
     for (Agent agent: game.getAgentMap().values()) {
       Map.Entry<String, Double> entry2 =
           new AbstractMap.SimpleEntry<String, Double>(
           agent.getBroker().getBrokerName(), agent.getBalance());
       resultMap.add(entry2);
     }
   }
 
   //<editor-fold desc="Setters and Getters">
   public Game getGame() {
     return game;
   }
   public void setGame(Game game) {
     this.game = game;
   }
 
   public List<String> getGameInfo() {
     return gameInfo;
   }
   public List<Map.Entry<String, Double>> getResultMap() {
     return resultMap;
   }
   //</editor-fold>
 }
