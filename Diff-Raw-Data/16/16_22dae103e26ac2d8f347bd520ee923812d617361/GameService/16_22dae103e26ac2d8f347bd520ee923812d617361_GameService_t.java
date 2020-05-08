 package edu.wm.camckenna.werewolves.service;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
import java.util.TimeZone;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import com.mongodb.MongoClient;
 
 import edu.wm.camckenna.werewolves.HomeController;
 import edu.wm.camckenna.werewolves.dao.IKillDAO;
 import edu.wm.camckenna.werewolves.dao.IPlayerDAO;
 import edu.wm.camckenna.werewolves.dao.IUserDAO;
 import edu.wm.camckenna.werewolves.dao.IVoteDAO;
 import edu.wm.camckenna.werewolves.domain.BooleanMessage;
 import edu.wm.camckenna.werewolves.domain.GPSLocation;
 import edu.wm.camckenna.werewolves.domain.Game;
 import edu.wm.camckenna.werewolves.domain.Kill;
 import edu.wm.camckenna.werewolves.domain.Player;
 import edu.wm.camckenna.werewolves.domain.User;
 import edu.wm.camckenna.werewolves.domain.Vote;
 import edu.wm.camckenna.werewolves.exceptions.MultiplePlayersWithSameIDException;
 import edu.wm.camckenna.werewolves.exceptions.MultipleUsersWithSameIDException;
 import edu.wm.camckenna.werewolves.exceptions.NoPlayerFoundException;
 import edu.wm.camckenna.werewolves.exceptions.NoUserFoundException;
 import edu.wm.camckenna.werewolves.utils.GameServiceUtil;
 import edu.wm.camckenna.werewolves.utils.Values;
 
 public class GameService {
 	
 	private static final Logger logger = LoggerFactory.getLogger(GameService.class);
 	
 	@Autowired private IPlayerDAO playerDAO;
 	@Autowired private IUserDAO userDAO;
 	@Autowired private IVoteDAO voteDAO;
 	@Autowired private IKillDAO killDAO;
 	
 	private Game game;
 	
 	
 	public GameService(){
 		
 	}	
 	public List<Player> getAllAlive()
 	{
 		return playerDAO.getAllAlive();
 	}
 	public List<Player> getAllWerewolves()
 	{
 		return playerDAO.getAllWerewolves();
 	}
 	public List<Player> getAllAliveWerewolves()
 	{
 		return playerDAO.getAllAliveWerewolves();
 	}
 	private List<Player> getAllTownpeople() {
 		return playerDAO.getAllTownspeople();
 	}
 	public List<Player> getAllAliveTownspeople()
 	{
 		return playerDAO.getAllAliveTownspeople();
 	}
 	public List<Player> getAllPlayers() {
 		return playerDAO.getAllPlayers();
 	}
 	private List<Player> getAllAliveHunters() {
 		
 		return playerDAO.getAllAliveHunters();
 	}
 	public void createPlayer(Player player)
 	{
 		playerDAO.createPlayer(player);
 	}	
 	public void updatePlayer(Player p)
 	{
 		playerDAO.updatePlayer(p);
 	}	
 	public void updateUser(User user)
 	{
 		userDAO.updateUser(user);
 	}	
 	public Player getPlayerByUsername(String name){
 		try {
 			return playerDAO.getPlayerbyUsername(name);
 		} catch (NoPlayerFoundException | MultiplePlayersWithSameIDException e) {
 			logger.error("Problem with username: " + name);
 			return null;
 		}
 	}
 	private List<User> getAllUsers() {
 		return userDAO.getAllUsers();
 	}
 	
 	private boolean hasGameEnded(){
 		if((getAllAliveWerewolves().size() == 0) 
 				|| (getAllAliveTownspeople().size() == 0)){
 			return true;
 		}
 		return false;
 	}
 	private void endGame(boolean werewolvesWon){
 		List<Player> playersToReward;
 		List<Player> alivePlayersToReward;
 		if(werewolvesWon){
 			playersToReward = getAllWerewolves();
 			alivePlayersToReward = getAllAliveWerewolves();
 		}
 		else{
 			playersToReward = getAllTownpeople();
 			alivePlayersToReward = getAllAliveTownspeople();
 		}
 		
 		for(Player p : playersToReward){
 			updateScoreBecauseOfPlayersAction(p, 
 					Values.ON_WINNING_SIDE_POINTS);
 		}
 		for(Player p : alivePlayersToReward){
 			updateScoreBecauseOfPlayersAction(p, 
 					Values.SURVIVED_UNTIL_END_POINTS);
 		}
 		
 	}
 
 	public void switchFromDayToNight(){
 		game.setDay(false);
 		hangPlayer();
 		List<Player> werewolves = getAllAliveWerewolves();
 		for(Player w : werewolves){
 			w.setCanKill(true);
 			logger.info("Can kill: " + w.getUsername());
 			updatePlayer(w);
 		}
 		List<Player> allPlayers = getAllAlive();
 		for(Player p : allPlayers){
 			p.setVotedAgainst(null);
 			updatePlayer(p);
 		}
 		List<Player> allAliveTownspeople = getAllAliveTownspeople();
 		for(Player p : allAliveTownspeople){
 			if(p.isHunter()){
 				p.setCanKill(true);
 				updatePlayer(p);
 			}
 		}
 	}
 	public void switchFromNightToDay(){
 		game.setDay(true);
 		List<Player> werewolves = getAllAliveWerewolves();
 		for(Player w : werewolves){
 			w.setCanKill(false);
 			updatePlayer(w);
 		}		
 		List<Player> allPlayers = getAllAlive();
 		for(Player p : allPlayers){
 			p.setVotedAgainst("");
 			updateScoreBecauseOfPlayersAction(p, Values.LIVING_POINTS);
 			updatePlayer(p);
 		}
 		
 		List<Player> allAliveTownspeople = getAllAliveTownspeople();
 		for(Player p : allAliveTownspeople){
 			if(p.isHunter()){
 				p.setCanKill(false);
 				updatePlayer(p);
 			}
 		}
 		
 	}
 	//Currently set at 5 minutes and does nothing
 	@Scheduled(fixedDelay=300000)
 	public void checkHasReported(){
 		logger.info("Checking that everyone has reported in");
 		/*
 		List<Player> players = getAllAlive();
 		
 		for(Player p : players){
 			if(!p.hasReported()){
 				p.setDead(true);
 			}
 			p.setHasReported(false);
 			updatePlayer(p);
 		}
 		*/
 		
 		
 	}
 	@Scheduled(fixedDelay=1000)
 	public void checkGameOperation(){
 		if(game == null){
 			startGame();
 		}
 		if(!game.isRunning()){
 			return;
 		}
 		
 		if(hasGameEnded()){
 			if(getAllAliveWerewolves().size() > 0 
 					&& getAllAliveTownspeople().size() == 0){
 				endGame(true);
 			}
 			else if(getAllAliveWerewolves().size() == 0 
 					&& getAllAliveTownspeople().size() > 0){
 				endGame(false);
 			}
 			else{
 				
 			}
 			game.setRunning(false);
 			logger.info("End of game");
 		}
 		//Check if people are reporting
 		//Check if Game is running
 		//Check day and night
 		long timeElapsed = (new Date().getTime() - game.getCreatedDate().getTime())/1000;
 		
 		long dayNightInSec = game.getDayNightFreq()*60;
 		
 		if(timeElapsed % dayNightInSec == 0) //TIme to switch periods
 		{
 			if((timeElapsed/dayNightInSec) % 2 == 1) //Becoming Night
 			{
 				switchFromDayToNight();
 				logger.info("Switching to Night");
 			}
 			else{
 				switchFromNightToDay();
 				logger.info("Switching to Day");
 			}				
 		}		
 		logger.info("Time since game began in seconds: " + timeElapsed);
 	}
 	public void startGame(String freq, String kill, String scent){
 		/*int dayNightFreq;
 		double killRange;
 		double scentRange;
 		
 		if(GameServiceUtil.isValidFreq(freq))
 			dayNightFreq = Integer.parseInt(freq);
 		else
 			dayNightFreq = Integer.parseInt(Values.TEST_DAYNIGHTFREQ);
 		
 		if(GameServiceUtil.isValidRange(scent, kill)){
 			scentRange = Double.parseDouble(scent);
 			killRange = Double.parseDouble(kill);
 		}
 		else{
 			scentRange = Double.parseDouble(Values.DEFAULT_SCENTRANGE);
 			killRange = Double.parseDouble(Values.DEFAULT_KILLRANGE);
 		}
 		
 			dayNightFreq = Integer.parseInt(Values.TEST_DAYNIGHTFREQ);
 			scentRange = Double.parseDouble(Values.DEFAULT_SCENTRANGE);
 			killRange = Double.parseDouble(Values.DEFAULT_KILLRANGE);
 			*/
 		logger.info("Starting game!");
 		playerDAO.discardTable();
 		killDAO.discardTable();
 		voteDAO.discardTable();
 	
 		List<User> users = getAllUsers();
 		List<Player> players = new ArrayList<Player>();
 
 		for(User u: users){
 			Player p = new Player();
 			p.setId(UUID.randomUUID().toString());
 			p.setCanKill(false);
 			p.setLat(0.0);
 			p.setLng(0.0);
 			p.setUsername(u.getUsername());
 			p.setDead(false);
 			p.setWerewolf(false);
 			p.setVotedAgainst("");
 
 			createPlayer(p);
 			players.add(p);			
 		}
 		
 		long seed = System.nanoTime();
 		Collections.shuffle(players, new Random(seed));
 		
 		int numOfWolves = (int)(Values.DEFAULT_WEREWOLF_PERCENTAGE * players.size());
 		for(int x = 0; x < numOfWolves; x++){
 			players.get(x).setWerewolf(true);
 			updatePlayer(players.get(x));
 		}
 		
 		//Hardwiring for testing
 			Player cas = getPlayerByUsername("Castiel");
 			cas.setWerewolf(true);
 			updatePlayer(cas);
 			
 			Player joenash = getPlayerByUsername("joenash");
 			joenash.setWerewolf(false);
 			updatePlayer(joenash);
 			
 			
 			User admin = convertFromPrincipalNameToUser("Admin");
 			admin.setAdmin(true);
 			updateUser(admin);
 			
 		//TODO: Allow setting of start time
 		game = new Game(1, new Date());
 		game.setRunning(true);
 		game.setDay(true);
 
 	}
 	public void startGame(){
 		startGame(Values.TEST_DAYNIGHTFREQ, Values.DEFAULT_SCENTRANGE, Values.DEFAULT_KILLRANGE);
 	}
 
 	public List<String> getScores(){	
 		List<User> allUsers = userDAO.getUsersInOrderofScore();
 		List<String> namesAndScore = new ArrayList<String>();
 		for(User u : allUsers){
 			namesAndScore.add(u.getUsername() + ",  + " + u.getScore());
 		}
 		return namesAndScore;
 	}
 	public void voteForPlayer(String voterString, String votedAgainstString){
 		try{
 		Player voter = convertFromPrincipalNameToPlayer(voterString);
 		Player votedAgainst = convertFromPrincipalNameToPlayer(votedAgainstString);
 		
 		if(!GameServiceUtil.canVote(voter, votedAgainst)){
 			logger.info("Cannot vote at this time");
 			return;
 		}
 		
 		voter.setVotedAgainst(votedAgainst.getUsername());
 		Vote vote = new Vote(voterString, votedAgainstString, new Date());
 		voteDAO.addVote(vote);
 
 		updatePlayer(voter);
 		//return new BooleanMessage(true,voterString + " voted for " + votedAgainst);
 		
 		}
 		catch(Exception e){
 			logger.info(e.toString());
 			//return new BooleanMessage(false, "There was an exception: " + e.toString());
 		}
 	}
 	public void hangPlayer() {
 		Player p = findPlayerToHang();
 		if(p == null) //if nobody voted...
 			return;
 		killVotedAgainstPlayer(p);	
 		checkToRewardPointsforVoting(p);
 	}
 	
 	private void killVotedAgainstPlayer(Player p){
 
 		p.setDead(true);
 		updatePlayer(p);
 	}
 	
 	private Player findPlayerToHang(){
 		Map<String, Integer> map = new HashMap<String, Integer>();
 		List<Player> players = getAllAlive();
 		for(Player p : players){
 			if(!p.isDead() && p.getVotedAgainst() != null && !p.getVotedAgainst().equals("")){
 				String votedAgainst = p.getVotedAgainst();
 				
 				if(map.containsKey(votedAgainst)){
 					int num = map.get(votedAgainst).intValue();
 					num++;
 					map.put(votedAgainst, Integer.valueOf(num));
 				}
 				else
 					map.put(votedAgainst, Integer.valueOf(1));
 			}
 		}	
 		
 		int maxVotes = 1;
 		for(Integer i : map.values())
 		{
 			if(Integer.valueOf(i) >= maxVotes){
 				maxVotes = Integer.valueOf(i);
 			}
 		}
 		//List of Player Ids with maxVotes
 		List<String> ids = new ArrayList<String>();
 		for(String s : map.keySet()){
 			if(Integer.valueOf(map.get(s)) == maxVotes){
 				ids.add(s);
 			}
 		}
 		logger.info("Number of players to choose from: " + ids.size());
 		try{
 			if(ids.size() == 1){
 				String playerName = ids.get(0);
 				logger.info("The player to die is: "+ playerName);
 				return getPlayerByUsername(playerName);
 			}
 			else if(ids.size() == 0){
 				logger.info("Nobody was voted for, so nobody dies");
 				return null;
 			}
 			else{
 				int choice = new Random().nextInt(ids.size());
 				String playerName = ids.get(choice);
 				return getPlayerByUsername(playerName);
 			}
 		}
 		catch(Exception e){
 			logger.info("Exception while finding player to hang: " + e.toString());
 			return null;
 		}
 		
 	}
 	public void checkToRewardPointsforVoting(Player p){
 		//Do not reward people for killing townspeople
 		if(!p.isWerewolf())
 			return;
 	
 		List<Player> players = getAllAlive();
 		
 		//If the player helped vote for the werewolf
 		for(Player player : players){
 			if(player.getVotedAgainst().equals(p.getUsername())){
 				updateScoreBecauseOfPlayersAction(player, Values.VOTING_POINTS);
 				logger.info("Rewarding " + player.getUsername() + " " + 
 						Values.VOTING_POINTS +" points");
 			}
 		}		
 	}
 
 
 	
 	private void updateScoreBecauseOfPlayersAction(Player player, int points){
 		List<User> users = getAllUsers();
 		for(User u : users){
 			//if an user is in the list of players to reward, increase score by points
 			if(player.getUsername().equals(u.getUsername())){
 				u.increaseScore(points);
 				userDAO.updateUser(u);
 			}
 		}
 	}
 
 	public void updatePosition(String name, double lat, double lng) {
 		
 		Player player = convertFromPrincipalNameToPlayer(name);
 		player.setLat(lat);
 		player.setLng(lng);
 		player.setHasReported(true);
 		updatePlayer(player);
 	}
 	private User convertFromPrincipalNameToUser(String name){
 		try{
 			return userDAO.getUserByUsername(name);
 		}
 		catch(MultipleUsersWithSameIDException e){
 			logger.error("Too many users found for username: " + name);
 			return null;
 		} catch (NoUserFoundException e) {
 			logger.error("No users found for username: " + name);
 			return null;
 		}
 	}
 	private Player convertFromPrincipalNameToPlayer(String name){
 		
 		return getPlayerByUsername(name);
 	}
 	public Map<String, List<String>> getPlayerInfo(String name){
 		Map<String, List<String>> map= new HashMap<String, List<String>>();
 		
 		Player player = getPlayerByUsername(name);
 		User user = convertFromPrincipalNameToUser(name);
 		List<String> list = new ArrayList<String>();
 		list.add(name);
 		map.put("username", list);
 		list = new ArrayList<String>();
 		list.add(""+user.getScore());
 		map.put("score",list );
 		
 		String role = "";
 		if(player.isDead()){
 			role= "Dead";
 		}
 		else if(player.isWerewolf()){
 			role= "Werewolf";
 		}
 		else if(player.isHunter()){
 			role= "Hunter";
 		}
 		else{
 			role= "Townsperson";
 		}
 		list = new ArrayList<String>();
 		list.add(role);
 		map.put("role", list);
 		
 		list = new ArrayList<String>();
 		list.add("" + player.getLat() + ", " + player.getLng());
 		map.put("curPos", list);
 		
 		list = new ArrayList<String>();
 		list.add(""+player.getCanKill());
 		map.put("canKill", list);
 		
 		list = new ArrayList<String>();
 		if(player.getVotedAgainst() != null && !player.getVotedAgainst().equals("")){
 			list.add(player.getVotedAgainst());
 		}
 		else{
 			list.add("null");
 		}
 			
 		map.put("voted", list);
 		
 		list = new ArrayList<String>();
 		List<Vote> votes = getVotes(name);
 		for(Vote vote: votes){
 			list.add(vote.getVotedAgainstID());
 		}
 		map.put("votes", list);
 		
 		list = new ArrayList<String>();
 		map.put("kills", list);
 		
 		List<Kill> kills = killDAO.getKillsByKillerID(name);
 		
		SimpleDateFormat sdf = new SimpleDateFormat("MM.dd 'at' hh:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
 		for(Kill kill: kills){
			long timeElapsed = (kill.getTimestampDate().getTime() - game.getCreatedDate().getTime())/1000;
			
			long dayNightInSec = game.getDayNightFreq()*60;
			
			int day = (int)(timeElapsed/dayNightInSec + 1)/2;
			
			list.add(kill.getVictimID() + ", killed on Day " + day
					+ " at (" + kill.getLat() + ", " + kill.getLng() + ")");
 		}
 		
 		return map;
 	}
 	//Change to booleant
 	public void kill(String killerName, String victimName){
 	
 			Player victim = getPlayerByUsername(victimName);
 			Player killer = getPlayerByUsername(killerName);
 			
 
 			if(!GameServiceUtil.canKill(killer, victim, game.getKillRadius())){
 				logger.info("Cannot kill at this time");
 				return;
 			}				
 			
 			if(GameServiceUtil.doesCounterAttackOccur(getAllAliveWerewolves(), getAllPlayers())){
 				logger.info("YOWZAH! Counterattack!" + victim.getUsername() + " countered and killed " + killer.getUsername());
 				killer.setDead(true);
 				victim.setHunter(true);
 				Kill kill = new Kill(victim.getUsername(), killer.getUsername(), 
 						new Date(), victim.getLat(), victim.getLng());
 				
 				killDAO.addKill(kill);
 				updateScoreBecauseOfPlayersAction(victim, Values.COUNTERATTACK_POINTS);
 			}
 			else{
 				
 				logger.info(killer.getUsername() + " killed " + victim.getUsername());
 			
 				killer.setCanKill(false);
 				victim.setDead(true);
 				
 				Kill kill = new Kill(killer.getUsername(), victim.getUsername(), 
 						new Date(), victim.getLat(), victim.getLng());
 				
 				killDAO.addKill(kill);
 				
 				updateScoreBecauseOfPlayersAction(killer, Values.KILL_POINTS);
 			}
 			
 			updatePlayer(killer);
 			updatePlayer(victim);
 
 	}
 	public void killByHunter(String killerName, String victimName){
 		
 		Player victim = getPlayerByUsername(victimName);
 		Player killer = getPlayerByUsername(killerName);
 		
 
 		if(!GameServiceUtil.canKill(killer, victim, game.getKillRadius())){
 			logger.info("Cannot kill at this time");
 			return;
 		}				
 
 		else{
 			
 			logger.info(killer.getUsername() + " killed " + victim.getUsername());
 			if(GameServiceUtil.doesHunterKill(getAllAliveHunters(), getAllAliveTownspeople()));
 			killer.setCanKill(false);
 			victim.setDead(true);
 			
 			Kill kill = new Kill(killer.getUsername(), victim.getUsername(), 
 					new Date(), victim.getLat(), victim.getLng());
 			
 			killDAO.addKill(kill);
 			if(victim.isWerewolf()){
 				updateScoreBecauseOfPlayersAction(killer, Values.KILL_POINTS);
 			}
 			else{
 				if(Math.random() > .3){
 					logger.info(killer.getUsername() + " commited suicide in anguish over killing the innocent " + victim.getUsername());
 					killer.setDead(true);
 				}
 			}
 			
 		}
 		
 		updatePlayer(killer);
 		updatePlayer(victim);
 	}
 
 	public List<String> getAllNearby(String name){		
 		
 			Player killer = convertFromPrincipalNameToPlayer(name);
 			if(killer.isWerewolf())
 				return getAllNearby_Werewolves(killer);
 			else if(killer.isHunter()){ //cannot get nearby for townspeople
 				return getAllNearby_Hunters(killer);
 			}
 			else
 				return new ArrayList<String>();
 			
 	}
 	private List<String> getAllNearby_Werewolves(Player killer){		
 		
 		List<Player> allAlive = getAllAlive();
 		List<String> nearbyPlayers = new ArrayList<String>();
 		for(Player p : allAlive){
 			if(!p.getUsername().equals(killer.getUsername())){ //different people
 				if(GameServiceUtil.isInRange(killer, p, game.getScentRadius())){
 					if(p.isWerewolf()){
 						nearbyPlayers.add("Werewolf: " + p.getUsername());
 					}
 					else{
 						nearbyPlayers.add(p.getUsername());
 					}
 				}
 			}
 		}
 		
 		return nearbyPlayers;
 }
 	private List<String> getAllNearby_Hunters(Player killer){		
 		
 		List<Player> allAlive = getAllAlive();
 		List<String> nearbyPlayers = new ArrayList<String>();
 		for(Player p : allAlive){
 			if(!p.getUsername().equals(killer.getUsername())){ //different people
 				if(GameServiceUtil.isInRange(killer, p, game.getKillRadius())){
 					nearbyPlayers.add(p.getUsername());
 				}
 			}
 		}
 		
 		return nearbyPlayers;
 }
 
 	public List<String> getAllAliveAsStrings() {
 		List<Player> players = getAllAlive();
 		List<String> playerNames = new ArrayList<String>();
 		
 		for(Player p : players){
 			playerNames.add(p.getUsername());
 		}
 		return playerNames;
 		
 	}
 	public Map<String, String> getListOfPlayers(String name) {
 		Player player = getPlayerByUsername(name);		
 		List<Player> players = getAllPlayers();
 		return GameServiceUtil.getListofPlayersWithStatus(game, player, players);
 
 	}	
 	public String getDayOrNight(){
 		if(game.isDay())
 			return "day";
 		else
 			return "night";
 	}
 	public List<Vote> getVotes(String name) {
 		Player player = getPlayerByUsername(name);
 		List<Vote> collection = new ArrayList<Vote>();
 		
 		List<Vote> votes = voteDAO.getAllVotes();
 		
 		for(Vote v : votes){
 			if(v.getVoterID().equals(player.getUsername()))
 				collection.add(v);
 		}
 		
 		return collection;
 	}
 	public List<Kill> getKills(String name) {
 		Player player = getPlayerByUsername(name);
 		List<Kill> collection = new ArrayList<Kill>();
 		
 		List<Kill> kills = killDAO.getAllKills();
 		
 		for(Kill k:kills){
 			if(k.getKillerID().equals(player.getUsername()))
 				collection.add(k);
 		}
 		
 		return collection;
 	}
 	public Map<String, Integer> getGameStats() {
 		Map<String, Integer> coll = new HashMap<>();
 		coll.put("Players", getAllPlayers().size());
 		coll.put("Alive", getAllAlive().size());
 		coll.put("Townspeople", getAllAliveTownspeople().size());
 		coll.put("Werewolves", getAllAliveWerewolves().size());
 		
 		return coll;
 	}
 
 }
