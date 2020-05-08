 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.binout.soccer5.boundary;
 
 import com.binout.soccer5.controller.MatchEJB;
 import com.binout.soccer5.controller.PlayerEJB;
 import com.binout.soccer5.entity.Match;
 import com.binout.soccer5.entity.Player;
 import java.util.Date;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.enterprise.inject.Model;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 
 /**
  *
  * @author benoit
  */
 @Model
 public class CalendarBean {
 
     private Date newDate;
     private List<Match> matches;
     private Player selectedPlayer;
     private String guest;
 
     @Inject
     private MatchEJB matchEjb;
     @Inject
     private PlayerEJB playerEjb;
     
     @PostConstruct
     public void initNextMatches() {
         matches = matchEjb.getNextMatches();
     }
     
     public Date getMinDate() {
         return new Date();
     }
 
     public Date getNewDate() {
         return newDate;
     }
 
     public void setNewDate(Date newDate) {
         this.newDate = newDate;
     }
 
     public List<Match> getMatches() {
         return matches;
     }
 
     public void register() throws Exception {
         if (newDate != null) {
             Match m = new Match();
             m.setDate(newDate);
             matchEjb.registerMatch(m);
             initNextMatches();
         }
         newDate = null;
     }
     
 
     public Player getSelectedPlayer() {
         return selectedPlayer;
     }
 
     public void setSelectedPlayer(Player selectedPlayer) {
         this.selectedPlayer = selectedPlayer;
     }
 
     public List<Player> complete(String query) {
         return playerEjb.listPlayers();
     }
     
     public void join(Match m) {
         if (selectedPlayer != null) {
             matchEjb.registerPlayerToMatch(m, selectedPlayer);
             initNextMatches();
         } else {
             FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unkonwn Player", "Unkonwn Player");
             FacesContext.getCurrentInstance().addMessage(null, message);
         }
         selectedPlayer = null;
     }
     
     public void removePlayer(Match m, Player p) {
         matchEjb.unregisterPlayerToMatch(m, p);
         initNextMatches();
     }
         
     public String getGuest() {
         return guest;
     }
     
     public void setGuest(String g) {
         guest = g;
     }
     
     public void addGuest(Match m)  {
        matchEjb.registerGuestToMatch(m, guest);
        guest = null;
        initNextMatches();
     }
     
     public void removeGuest(Match m, String g) {
        matchEjb.unregisterGuestToMatch(m, g);
        initNextMatches();
     }
 
 }
