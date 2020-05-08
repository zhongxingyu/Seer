 package com.powertac.tourney.beans;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.faces.bean.ManagedBean;
 
 import com.powertac.tourney.services.Database;
 
 @ManagedBean
 public class Game {
 	private String competitionName = "";
 	private Date startTime;
 	private int competitionId = -1;
 	private int tourneyId = 0;
 	private int gameId = 0;
 	private int machineId;
 	private static int maxGameId = 0;
 	private String status = "pending";
 	private Machine runningMachine = null; // This is set when the game is
 											// actually running on a machine
 	private boolean hasBootstrap = false;
 	private String brokers = "";
 	private HashMap<String, String> brokerAuth = new HashMap<String, String>();
 
 	private String gameName = "";
 	private String location = "";
 	private String jmsUrl = "";
 	private String serverConfigUrl = "";
 	private String tournamentSchedulerUrl = "";
 	private String bootstrapUrl = "";
 	private String propertiesUrl = "";
 	private String visualizerUrl = "";
 	private String pomUrl = "";
 
 	private HashMap<String, String> brokersToLogin = null;
 
 	private String[] brokersLoggedIn = null;
 	private int maxBrokers = 1;
 
 	public static final String key = "game";
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public String toUTCStartTime() {
 		SimpleDateFormat dateFormatUTC = new SimpleDateFormat(
 				"yyyy-MMM-dd HH:mm:ss");
 
 
 		// Time in GMT
 		return dateFormatUTC.format(startTime);
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public Game() {
 		// System.out.println("Created Game Bean: " + gameId);
 		// gameId = maxGameId;
 		// maxGameId++;
 
 		brokersToLogin = new HashMap<String, String>();
 
 	}
 
 	public Game(ResultSet rs) {
 		try{
 			this.setStatus(rs.getString("status"));
 			this.setMaxBrokers(rs.getInt("maxBrokers"));
 			this.setStartTime(new Date(rs.getTimestamp("startTime").getTime()));
 			this.setBrokers(rs.getString("brokers"));
 			this.setTourneyId(rs.getInt("tourneyId"));
 			this.setMachineId(rs.getInt("machineId"));
 			this.setHasBootstrap(rs.getBoolean("hasBootstrap"));
 			this.setGameName(rs.getString("gameName"));
 			this.setGameId(rs.getInt("gameId"));
 			this.setJmsUrl(rs.getString("jmsUrl"));
 			this.setVisualizerUrl(rs.getString("visualizerUrl"));
 			this.setBootstrapUrl(rs.getString("bootstrapUrl"));
 			this.setPropertiesUrl(rs.getString("propertiesUrl"));
 			this.setLocation(rs.getString("location"));
 		}catch(Exception e){
 			System.out.println("[ERROR] Error creating game from result set");
 		}
 	}
 
 	public int getNumBrokersRegistered() {
 		Database db = new Database();
 		int result = 0;
 
 		try {
 			result = db.getBrokersInGame(gameId).size();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 
 	}
 
 	public void addBroker(int brokerId) {
 		Database db = new Database();
 		Broker b = new Broker("new");
 		try {
 			b = db.getBroker(brokerId);
 			db.addBrokerToGame(gameId, b);
 			db.closeConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		brokers += b.getBrokerName() + ", ";
 		this.brokerAuth.put(b.getBrokerAuthToken(), b.getBrokerName());
 	}
 
 	public HashMap<String, String> getBrokersToLogin() {
 		return brokersToLogin;
 	}
 
 	public void setBrokersToLogin(HashMap<String, String> brokersToLogin) {
 		this.brokersToLogin = brokersToLogin;
 	}
 
 	public static String getKey() {
 		return key;
 	}
 
 	public String getCompetitionName() {
 		return competitionName;
 	}
 
 	public String getPomUrl() {
 		return pomUrl;
 	}
 
 	public void setPomUrl(String pomUrl) {
 		this.pomUrl = pomUrl;
 	}
 
 	public void setCompetitionName(String competitionName) {
 		this.competitionName = competitionName;
 	}
 
 	public int getCompetitionId() {
 		return competitionId;
 	}
 
 	public void setCompetitionId(int competitionId) {
 		this.competitionId = competitionId;
 	}
 
 	public String getStatus() {
 		return status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	public String getJmsUrl() {
 		return jmsUrl;
 	}
 
 	public void setJmsUrl(String jmsUrl) {
 		this.jmsUrl = jmsUrl;
 	}
 
 	public String[] getBrokersLoggedIn() {
 		return brokersLoggedIn;
 	}
 
 	public int getMaxBrokers() {
 		return maxBrokers;
 	}
 
 	public void setMaxBrokers(int maxBrokers) {
 		this.maxBrokers = maxBrokers;
 	}
 
 	public int getGameId() {
 		return gameId;
 	}
 
 	public void setGameId(int gameId) {
 		this.gameId = gameId;
 	}
 
 	// public void addBrokerLogin(String brokerName, String authToken){
 	// brokersToLogin.put(authToken, brokerName);
 	// }
 
 	public void addGameLogin(String gameToken) {
 
 	}
 
 	public boolean isGameTokenValid(String gameToken) {
 		return true;
 	}
 
 	public boolean isBrokerRegistered(String authToken) {
 		System.out.println("Broker token: " + authToken);
 		Database db = new Database();
 		boolean ingame = false;
 		try {
 
 			List<Broker> allBrokers = db.getBrokersInGame(gameId);
 			for (Broker b : allBrokers) {
 				if (b.getBrokerAuthToken().equalsIgnoreCase(authToken)) {
 					ingame = true;
 					break;
 				}
 			}
 			db.closeConnection();
 
 		} catch (SQLException e) {
 
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return ingame;
 	}
 
 	/*
 	 * public boolean authorizeBroker(String brokerName, String gameToken) { if
 	 * (getBrokersToLogin() != null && getBrokersToLogin().get(brokerName) ==
 	 * gameToken) {
 	 * 
 	 * // Send http response indicating success return true; } else { // Send
 	 * http response back that authorization has failed return false; }
 	 * 
 	 * }
 	 */
 
 	public String getTournamentSchedulerUrl() {
 		return tournamentSchedulerUrl;
 	}
 
 	public void setTournamentSchedulerUrl(String tournamentSchedulerUrl) {
 		this.tournamentSchedulerUrl = tournamentSchedulerUrl;
 	}
 
 	public String getServerConfigUrl() {
 		return serverConfigUrl;
 	}
 
 	public void setServerConfigUrl(String serverConfigUrl) {
 		this.serverConfigUrl = serverConfigUrl;
 	}
 
 	public String getBootstrapUrl() {
 		return bootstrapUrl;
 	}
 
 	public void setBootstrapUrl(String bootstrapUrl) {
 		this.bootstrapUrl = bootstrapUrl;
 	}
 
 	public boolean isHasBootstrp() {
 		return hasBootstrap;
 	}
 
 	public void setHasBootstrap(boolean hasBootstrap) {
 		this.hasBootstrap = hasBootstrap;
 	}
 
 	public String getGameName() {
 		return gameName;
 	}
 
 	public void setGameName(String gameName) {
 		this.gameName = gameName;
 	}
 
 	public String getPropertiesUrl() {
 		return propertiesUrl;
 	}
 
 	public void setPropertiesUrl(String propertiesUrl) {
 		this.propertiesUrl = propertiesUrl;
 	}
 
 	public String getVisualizerUrl() {
 		return visualizerUrl;
 	}
 
 	public void setVisualizerUrl(String visualizerUrl) {
 		this.visualizerUrl = visualizerUrl;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public int getTourneyId() {
 		return tourneyId;
 	}
 
 	public void setTourneyId(int tourneyId) {
 		this.tourneyId = tourneyId;
 	}
 
 	public String getBrokers() {
 		return brokers;
 	}
 
 	public void setBrokers(String brokers) {
 		this.brokers = brokers;
 	}
 
 	public int getMachineId() {
 		return machineId;
 	}
 
 	public void setMachineId(int machineId) {
 		this.machineId = machineId;
 	}
 
 }
