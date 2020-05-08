 package com.powertac.tourney.beans;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.faces.bean.ManagedBean;
 
 import com.powertac.tourney.services.Database;
 
 // Technically not a managed bean, this is an internal Class to the 
 // Tournaments bean which is an application scoped bean that acts as 
 // a collection for all the active tournaments
 @ManagedBean
 public class Tournament {
 	public enum TourneyType {
 		SINGLE_GAME, MULTI_GAME;
 	}
 	private static int maxTournamentId = 0;
 	private int tourneyId = 0;
 	private Date startTime;
 	private String tournamentName;
 	private int maxBrokers; // -1 means inf, otherwise integer specific
 	private boolean openRegistration = false;
 	private int maxGames;
 	
 	private int size1 = 2;
 	private int numberSize1 = 2;
 	private int size2 = 4;
 	private int numberSize2 = 4;
 	private int size3 = 8;
 	private int numberSize3 = 4;
 	
 	private int maxBrokerInstances = 2;
 	
 	private String pomName;
 	
 	
 	// Probably Should check name against auth token
 	private HashMap<Integer,String> registeredBrokers;
 
 	private String pomUrl;
 
 	private HashMap<Integer,Game> allGames;
 	
 	public Tournament(){
 		//System.out.println("Created Tournament Bean: " + tourneyId);
 		//tournyId = maxTournamentId;
 		//maxTournamentId++;
 		
 		allGames = new HashMap<Integer,Game>();
 		registeredBrokers = new HashMap<Integer,String>();
 	}
 	
 	public Tournament(ResultSet rsTs){
 		try{
 			this.setTournamentId(rsTs.getInt("tourneyId"));
 			this.setOpenRegistration(rsTs.getBoolean("openRegistration"));
 			this.setMaxGames(rsTs.getInt("maxGames"));
 			this.setPomUrl(rsTs.getString("pomUrl"));
 			this.setMaxBrokers(rsTs.getInt("maxBrokers"));
 			this.setStartTime(rsTs.getDate("startTime"));
 			this.setSize1(rsTs.getInt("gameSize1"));
 			this.setSize2(rsTs.getInt("gameSize2"));
 			this.setSize3(rsTs.getInt("gameSize3"));
 			this.setNumberSize1(rsTs.getInt("numberGameSize1"));
 			this.setNumberSize2(rsTs.getInt("numberGameSize2"));
 			this.setNumberSize3(rsTs.getInt("numberGameSize3"));
 			this.setMaxBrokerInstances(rsTs.getInt("maxBrokerInstances"));
 			this.setTournamentName(rsTs.getString("tourneyName"));
 		}catch(Exception e){
 			System.out.println("[ERROR] Error creating tournament from result set");
 			e.printStackTrace();
 		}
 	}
 
 	public int getTournamentId() {
 		return tourneyId;
 	}
 
 	public void setTournamentId(int competitionId) {
 		this.tourneyId = competitionId;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public String getTournamentName() {
 		return tournamentName;
 	}
 
 	public void setTournamentName(String tournamentName) {
 		this.tournamentName = tournamentName;
 	}
 
 	public int getMaxBrokers() {
 		return maxBrokers;
 	}
 
 	public void setMaxBrokers(int maxBrokers) {
 		this.maxBrokers = maxBrokers;
 	}
 
 
 	public String getPomUrl() {
 		return pomUrl;
 	}
 
 	public void setPomUrl(String pomUrl) {
 		this.pomUrl = pomUrl;
 	}
 
 	
 	public void addGame(Game game){
 		this.allGames.put(game.getGameId(), game);		
 	}
 	
 	public List<Game> getGames(){
 		List<Game> result = new ArrayList<Game>();
 		Database db = new Database();
 		try {
 			result = db.getGamesInTourney(this.tourneyId);
 			db.closeConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 	/*
 	public String register(String name,int id, String authToken){
 		//TODO: Fix this so that brokers are added to games according to the csp spec
 		
 		System.out.println("Registering broker: " + name);
 		// Only open registration
 		if(registeredBrokers.size() < maxBrokers && maxBrokers != -1){
 			registeredBrokers.put(id, name);
 			for(Game g : allGames.values()){
 				if (g.getCompetitionId() == this.getTournamentId()){
 					g.addBrokerLogin(name, authToken);	
 					System.out.println("Broker: " + name + ":" + authToken + " is registered for Competition:" + g.getCompetitionName());
 				}
 			}
 			
 			return "Success";
 		}else{
 			return "Failure";
 		}
 	}*/
 	
 	public boolean isRegistered(String authToken){
 		return registeredBrokers.containsValue(authToken);
 	}
 	
 	public int getNumberRegistered(){
 		Database db = new Database();
 		int result = 0;
 		try {
 			result = db.getBrokersInTournament(tourneyId).size();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 	/**
 	 * @return the pomName
 	 */
 	public String getPomName() {
 		return pomName;
 	}
 
 	/**
 	 * @param pomName the pomName to set
 	 */
 	public void setPomName(String pomName) {
 		this.pomName = pomName;
 	}
 
 	public boolean isOpenRegistration() {
 		return openRegistration;
 	}
 
 	public void setOpenRegistration(boolean openRegistration) {
 		this.openRegistration = openRegistration;
 	}
 
 	public int getMaxGames() {
 		return maxGames;
 	}
 
 	public void setMaxGames(int maxGames) {
 		this.maxGames = maxGames;
 	}
 
 	public int getSize1() {
 		return size1;
 	}
 
 	public void setSize1(int size1) {
 		this.size1 = size1;
 	}
 
 	public int getNumberSize1() {
 		return numberSize1;
 	}
 
 	public void setNumberSize1(int numberSize1) {
 		this.numberSize1 = numberSize1;
 	}
 
 	public int getSize2() {
 		return size2;
 	}
 
 	public void setSize2(int size2) {
 		this.size2 = size2;
 	}
 
 	public int getNumberSize2() {
 		return numberSize2;
 	}
 
 	public void setNumberSize2(int numberSize2) {
 		this.numberSize2 = numberSize2;
 	}
 
 	public int getSize3() {
 		return size3;
 	}
 
 	public void setSize3(int size3) {
 		this.size3 = size3;
 	}
 
 	public int getNumberSize3() {
 		return numberSize3;
 	}
 
 	public void setNumberSize3(int numberSize3) {
 		this.numberSize3 = numberSize3;
 	}
 
 	public int getMaxBrokerInstances() {
 		return maxBrokerInstances;
 	}
 
 	public void setMaxBrokerInstances(int maxBrokerInstances) {
 		this.maxBrokerInstances = maxBrokerInstances;
 	}
 
 
 
 }
