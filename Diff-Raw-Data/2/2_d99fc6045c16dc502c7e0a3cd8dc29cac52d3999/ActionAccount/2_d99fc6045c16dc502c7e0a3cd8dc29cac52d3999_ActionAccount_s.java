 package org.powertac.tourney.actions;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 
 import org.powertac.tourney.beans.Broker;
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.beans.Tournament;
 import org.powertac.tourney.beans.User;
 import org.powertac.tourney.services.Database;
 import org.powertac.tourney.services.SpringApplicationContext;
 
 @ManagedBean
 @RequestScoped
 public class ActionAccount
 {
 
   private String newBrokerName;
   private String newBrokerShortDescription;
   private int selectedBrokerId;
   private String selectedBrokerName;
   private String selectedBrokerAuth;
 
   public ActionAccount ()
   {
 
   }
 
   public String getNewBrokerName ()
   {
     return newBrokerName;
   }
 
   public void setNewBrokerName (String newBrokerName)
   {
     this.newBrokerName = newBrokerName;
   }
 
   public String addBroker ()
   {
     User user =
       (User) FacesContext.getCurrentInstance().getExternalContext()
               .getSessionMap().get(User.getKey());
     // Check if user is null?
     user.addBroker(getNewBrokerName(), getNewBrokerShortDescription());
 
     return "Account";
   }
 
   public List<Broker> getBrokers ()
   {
     User user =
       (User) FacesContext.getCurrentInstance().getExternalContext()
               .getSessionMap().get(User.getKey());
     return user.getBrokers();
   }
 
   public void deleteBroker (Broker b)
   {
     User user =
       (User) FacesContext.getCurrentInstance().getExternalContext()
               .getSessionMap().get(User.getKey());
     user.deleteBroker(b.getBrokerId());
 
   }
 
   public void editBroker (Broker b)
   {
     User user =
       (User) FacesContext.getCurrentInstance().getExternalContext()
               .getSessionMap().get(User.getKey());
     user.setEdit(true);
     b.setEdit(true);
     b.setNewAuth(b.getBrokerAuthToken());
     b.setNewName(b.getBrokerName());
     b.setNewShort(b.getShortDescription());
   }
 
   public void saveBroker (Broker b)
   {
     User user =
       (User) FacesContext.getCurrentInstance().getExternalContext()
               .getSessionMap().get(User.getKey());
     user.setEdit(false);
     b.setEdit(false);
     b.setBrokerName(b.getNewName());
     b.setShortDescription(b.getNewShort());
     b.setBrokerAuthToken(b.getNewAuth());
 
     Database db = new Database();
 
     try {
       db.startTrans();
       db.updateBrokerByBrokerId(b.getBrokerId(), b.getBrokerName(),
                                 b.getBrokerAuthToken(), b.getShortDescription());
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
   }
 
   public void cancelBroker (Broker b)
   {
     User user =
       (User) FacesContext.getCurrentInstance().getExternalContext()
               .getSessionMap().get(User.getKey());
     user.setEdit(false);
     b.setEdit(false);
   }
 
   public List<Tournament> getAvailableTournaments (Broker b)
   {
     if (b == null) {
       return null;
     }
 
     List<Tournament> allTournaments = new ArrayList<Tournament>();
     Vector<Tournament> availableTourneys = new Vector<Tournament>();
     Database db = new Database();
     try {
       db.startTrans();
       allTournaments = db.getTournaments("pending");
       allTournaments.addAll(db.getTournaments("in-progress"));
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     for (Tournament t: allTournaments) {
       try {
         if (!db.isRegistered(t.getTournamentId(), b.getBrokerId())
             && t.getNumberRegistered() < t.getMaxBrokers()
            && t.getStartTime().before(new Date())) {
           availableTourneys.add(t);
         }
 
       }
       catch (SQLException e) {
         db.abortTrans();
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     }
     db.commitTrans();
 
     return (List<Tournament>) availableTourneys;
 
   }
 
   public String register (Broker b)
   {
 
     String tournamentName = b.getSelectedTourney();
     if (tournamentName == null || tournamentName == "") {
       return null;
     }
     Database db = new Database();
     List<Tournament> allTournaments = new ArrayList<Tournament>();
 
     try {
       db.startTrans();
       allTournaments = db.getTournaments("pending");
       allTournaments.addAll(db.getTournaments("in-progress"));
       for (Tournament t: allTournaments) {
         if (!db.isRegistered(t.getTournamentId(), b.getBrokerId())
             && t.getTournamentName().equalsIgnoreCase(tournamentName)) {
 
           if (t.getNumberRegistered() < t.getMaxBrokers()) {
             System.out.println("Registering broker: " + b.getBrokerId()
                                + " with tournament: " + t.getTournamentId());
             db.registerBroker(t.getTournamentId(), b.getBrokerId());
 
             // Only do this for single game, otherwise the scheduler handles multigame tourneys
             if (t.getType().equalsIgnoreCase("SINGLE_GAME")) {
               for (Game g: t.getGames()) {
                 if (g.getNumBrokersRegistered() < g.getMaxBrokers()) {
                   System.out.println("Number registered: "
                                      + g.getNumBrokersRegistered());
 
                   g.addBroker(b.getBrokerId());
                 }
               }
             }
 
           }
         }
       }
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return null;
   }
 
   public String getNewBrokerShortDescription ()
   {
     return newBrokerShortDescription;
   }
 
   public void setNewBrokerShortDescription (String newBrokerShortDescription)
   {
     this.newBrokerShortDescription = newBrokerShortDescription;
   }
 
 }
