 package com.powertac.tourney.actions;
 
 import java.util.List;
 import java.util.Vector;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 
 import com.powertac.tourney.beans.Broker;
 import com.powertac.tourney.beans.Tournament;
 import com.powertac.tourney.beans.Tournaments;
 import com.powertac.tourney.beans.User;
 
 
 @ManagedBean
 @RequestScoped
 public class ActionAccount {
 	
 
 	private String newBrokerName;
 	private String newBrokerShortDescription;
 	private int selectedBrokerId;
 	private String selectedBrokerName;
 	private String selectedBrokerAuth;
 	
 	public ActionAccount() {
 
 	}
 	
 	
 
 	public String getNewBrokerName() {
 		return newBrokerName;
 	}
 
 	public void setNewBrokerName(String newBrokerName) {
 		this.newBrokerName = newBrokerName;
 	}
 
 	public String addBroker() {
 		User user = (User) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get(User.getKey());

 		Broker b = user.addBroker(getNewBrokerName(), getNewBrokerShortDescription());
 		
 
 		return "Account";
 	}
 	
 	public Vector<Broker> getBrokers(){
 		User user = (User) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get(User.getKey());
 		return user.getBrokers();
 	}
 	
 	public void deleteBroker(Broker b){
 		User user = (User) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get(User.getKey());
 		user.getBrokers().remove(b);
 		
 	}
 	
 	public void editBroker(Broker b){
 		b.setEdit(true);
 		b.setNewAuth(b.getBrokerAuthToken());
 		b.setNewName(b.getBrokerName());
 		b.setNewShort(b.getShortDescription());
 	}
 	
 	public void saveBroker(Broker b){
 		b.setEdit(false);
 		b.setBrokerName(b.getNewName());
 		b.setShortDescription(b.getNewShort());
 		b.setBrokerAuthToken(b.getNewAuth());
 		
 	}
 	
 	public void cancelBroker(Broker b){
 		b.setEdit(false);
 	}
 	
 
 
 	public List<Tournament> getAvailableTournaments(Broker b) {
 		if (b == null) {
 			return null;
 		}
 
 		Tournaments allTournaments = Tournaments.getAllTournaments();
 		Vector<Tournament> availableTourneys = new Vector<Tournament>();
 		for (Tournament t : allTournaments.getLists()) {
 			if (!t.isRegistered(b.getBrokerName())) {
 				availableTourneys.add(t);
 			}
 		}
 
 		return (List<Tournament>) availableTourneys;
 
 	}
 
 	public String register(Broker b) {
 		String tournamentName = b.getSelectedTourney();
 		if(tournamentName==null || tournamentName==""){
 			return null;
 		}
 		
 		Tournaments allTournaments = Tournaments.getAllTournaments();
 		for (Tournament t : allTournaments.getLists()) {
 			if (!t.isRegistered(b.getBrokerAuthToken())
 					&& t.getTournamentName().equalsIgnoreCase(tournamentName)) {
 				
 				
 				
 				t.register(b.getBrokerName(), b.getBrokerId(), b.getBrokerAuthToken());
 				
 			}
 		}
 
 		return null;
 	}
 
 
 
 	public String getNewBrokerShortDescription() {
 		return newBrokerShortDescription;
 	}
 
 
 
 	public void setNewBrokerShortDescription(String newBrokerShortDescription) {
 		this.newBrokerShortDescription = newBrokerShortDescription;
 	}
 
 
 
 }
