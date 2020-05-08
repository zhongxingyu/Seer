 package com.powertac.tourney.actions;
 
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 
 import com.powertac.tourney.beans.Game;
 import com.powertac.tourney.beans.Games;
 import com.powertac.tourney.beans.Machines;
 import com.powertac.tourney.beans.Scheduler;
 import com.powertac.tourney.beans.Tournament;
 import com.powertac.tourney.beans.Tournaments;
 import com.powertac.tourney.services.StartServer;
 import com.powertac.tourney.services.Upload;
 
 @Component("actionTournament")
 @Scope("request")
 public class ActionTournament {
 
 	@Autowired
 	private Upload upload;
 
 	public enum TourneyType {
 		SINGLE_GAME, MULTI_GAME;
 	}
 
 	private Date startTime = new Date(); // Default to current date/time
 	private String tournamentName;
 	private int maxBrokers; // -1 means inf, otherwise integer specific
 	private List<String> machines;
 	private String pomName;
 	private String bootName;
 	private String propertiesName;
 	private UploadedFile pom;
 	private UploadedFile boot;
 	private UploadedFile properties;
 	private TourneyType type = TourneyType.SINGLE_GAME;
 
 	/**
 	 * @return the properties
 	 */
 	public UploadedFile getProperties() {
 		return properties;
 	}
 
 	/**
 	 * @param properties
 	 *            the properties to set
 	 */
 	public void setProperties(UploadedFile properties) {
 		this.properties = properties;
 	}
 
 	/**
 	 * @return the boot
 	 */
 	public UploadedFile getBoot() {
 		return boot;
 	}
 
 	/**
 	 * @param boot
 	 *            the boot to set
 	 */
 	public void setBoot(UploadedFile boot) {
 		this.boot = boot;
 	}
 
 	/**
 	 * @return the pom
 	 */
 	public UploadedFile getPom() {
 		return pom;
 	}
 
 	/**
 	 * @param pom
 	 *            the pom to set
 	 */
 	public void setPom(UploadedFile pom) {
 		this.pom = pom;
 	}
 
 	/**
 	 * @return the propertiesName
 	 */
 	public String getPropertiesName() {
 		return propertiesName;
 	}
 
 	/**
 	 * @param propertiesName
 	 *            the propertiesName to set
 	 */
 	@SuppressWarnings("static-access")
 	public void setPropertiesName(String propertiesName) {
 
 		// Generate MD5 hash
 		DigestUtils dg = new DigestUtils();
 		this.propertiesName = dg.md5Hex(propertiesName
 				+ (new Date()).toString() + Math.random());
 	}
 
 	/**
 	 * @return the bootName
 	 */
 	public String getBootName() {
 		return bootName;
 	}
 
 	/**
 	 * @param bootName
 	 *            the bootName to set
 	 */
 	@SuppressWarnings("static-access")
 	public void setBootName(String bootName) {
 		// Generate MD5 hash
 		DigestUtils dg = new DigestUtils();
 		this.bootName = dg.md5Hex(bootName + (new Date()).toString()
 				+ Math.random());
 		
 	}
 
 	public String getPomName() {
 		return pomName;
 	}
 
 	@SuppressWarnings("static-access")
 	public void setPomName(String pomName) {
 		// Generate MD5 hash
 		DigestUtils dg = new DigestUtils();
 		this.pomName = dg.md5Hex(pomName
 				+ (new Date()).toString() + Math.random());
 	}
 
 	public List<String> getMachines() {
 		return machines;
 	}
 
 	public void setMachines(List<String> machines) {
 		this.machines = machines;
 	}
 
 	// Method to list the type enumeration in the jsf select Item component
 	public SelectItem[] getTypes() {
 		SelectItem[] items = new SelectItem[TourneyType.values().length];
 		int i = 0;
 		for (TourneyType t : TourneyType.values()) {
 			items[i++] = new SelectItem(t, t.name());
 		}
 		return items;
 	}
 
 	public TourneyType getType() {
 		return type;
 	}
 
 	public void setType(TourneyType type) {
 		this.type = type;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public int getMaxBrokers() {
 		return maxBrokers;
 	}
 
 	public void setMaxBrokers(int maxBrokers) {
 		this.maxBrokers = maxBrokers;
 	}
 
 	public String getTournamentName() {
 		return tournamentName;
 	}
 
 	public void setTournamentName(String tournamentName) {
 		this.tournamentName = tournamentName;
 	}
 
 	public String createTournament() {
 		// Create a tournament and insert it into the application context
 		Tournament newTourney = new Tournament();
 		if (type == TourneyType.SINGLE_GAME) {
 			// TODO: Change this to the correct hosted files
 			
 			// Creates hashed names for each name
 			this.setBootName(boot.getName());
 			newTourney.setBootName(this.getBootName());
			
			this.setPomName(pom.getName());
			newTourney.setPomName(pom.getName());
			
			this.setPropertiesName(properties.getName());
			newTourney.setPropertiesName(properties.getName());
 			
 			
 			// Use upload service to upload files with hashed links
 			upload.setUploadedFile(getBoot());
 			upload.submit(getBootName());
 			
			upload.setUploadedFile(getPom());
			upload.submit(getPomName());
 			
			upload.setUploadedFile(getProperties());
			upload.submit(getPropertiesName());
 			
 			
 			newTourney.setBootstrapUrl("http://www-users.cselabs.umn.edu/~onarh001/bootstraprun.xml");
 			newTourney.setPomUrl("default");
 			newTourney.setMaxBrokers(getMaxBrokers());
 			newTourney.setStartTime(getStartTime());
 			newTourney.setTournamentName(getTournamentName());
 
 			// Add one game to the global context and to the tournament
 			Game newGame = new Game();
 			newGame.setMaxBrokers(getMaxBrokers());
 			newGame.setCompetitionId(newTourney.getCompetitionId());
 			newGame.setCompetitionName(getTournamentName());
 			newGame.setStatus("pending");
 			newGame.setStartTime(getStartTime());
 
 			Games allGames = (Games) FacesContext.getCurrentInstance()
 					.getExternalContext().getApplicationMap()
 					.get(Games.getKey());
 
 			// Add game to all games and to Tournament
 			allGames.addGame(newGame);
 			newTourney.addGame(newGame);
 
 			Tournaments.getAllTournaments().addTournament(newTourney);
 
 			// Start a single game and send jenkins request to kick the server
 			// at the appropriate time
 			Scheduler.getScheduler().schedule(
 					new StartServer(newGame, Machines.getAllMachines(),
 							Tournaments.getAllTournaments()),
 					newGame.getStartTime());
 			try {
 				// TODO:REMOVE this is only to simulate the message from the
 				// server
 				// Thread.sleep(6000);
 				// URL test = new
 				// URL("http://localhost:8080/TournamentScheduler/faces/serverInterface.jsp?action=status&status=ready&gameId="+newGame.getGameId());
 				// test.openConnection().getInputStream();
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} else if (type == TourneyType.MULTI_GAME) {
 
 		} else {
 
 		}
 
 		// Tournaments allTournaments = (Tournaments)
 		// FacesContext.getCurrentInstance()
 		// .getExternalContext().getApplicationMap().get(Tournaments.getKey());
 
 		// allTournaments.addTournament(newTourney);
 
 		return "Success";
 
 	}
 
 }
