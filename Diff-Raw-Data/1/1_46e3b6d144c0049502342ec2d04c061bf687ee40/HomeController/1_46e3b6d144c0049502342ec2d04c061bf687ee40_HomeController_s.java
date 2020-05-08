 package edu.wm.werewolf;
 
 import java.security.Principal;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.Locale;
 
 import org.apache.taglibs.standard.tag.common.xml.SetTag;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import edu.wm.werewolf.dao.IPlayerDAO;
 import edu.wm.werewolf.dao.IUserDAO;
 import edu.wm.werewolf.dao.IVoteDAO;
 import edu.wm.werewolf.exceptions.NoPlayerFoundException;
 import edu.wm.werewolf.exceptions.PlayerAlreadyExistsException;
 import edu.wm.werewolf.model.GPSLocation;
 import edu.wm.werewolf.model.JsonResponse;
 import edu.wm.werewolf.model.Kill;
 import edu.wm.werewolf.model.Player;
 import edu.wm.werewolf.model.WerewolfUser;
 import edu.wm.werewolf.model.Vote;
 import edu.wm.werewolf.service.GameService;
 import edu.wm.werewolf.service.UserServiceImpl;
 import edu.wm.werewolf.service.VoteListener;
 
 
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	@Autowired private IUserDAO userDAO;
 	@Autowired private IPlayerDAO playerDAO;
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	@Autowired IVoteDAO voteDAO;
 	@Autowired private GameService gameService;
 	private boolean wasDay = true;
 	// Autowired finds a satisfied bean of being a playerDAO and connects the controller to it
 	// Beans are defined in root-context (these are the Singleton classes)
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/newgame", method = {RequestMethod.POST})
 	public @ResponseBody JsonResponse newGame(@RequestParam("dayNight") long dayNight, Model model) {
 		dayNight *= 60000;
 		JsonResponse response = new JsonResponse("success");
 		logger.info("Starting new game with time interval: " + dayNight);
 		try {
 			gameService.newGame(dayNight);
 			wasDay = true;
 		} catch (Exception e) {
 			response.setStatus("failure;\n" + e.getStackTrace().toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/players/alive", method=RequestMethod.GET)
 	public @ResponseBody List<Player> getAllAlive()
 	{
 		// add responseBody to package as a JSON object
 		List<Player> players = gameService.getAllAlive();
 		if(!gameService.isOver()) {
 			for(int i = 0; i < players.size(); i++) {
 				players.get(i).setScore(0);
 				players.get(i).setLat(0);
 				players.get(i).setLng(0);
 				players.get(i).setVotedAgainst(null);
 				players.get(i).setWerewolf(false);
 				players.get(i).setUserId(null);
 			}
 		}
 		return players;
 	}
 	
 	@RequestMapping(value = "/players/all", method=RequestMethod.GET)
 	public @ResponseBody List<Player> getAllPlayers(Principal principal)
 	{
 		List<Player> players = gameService.getAllPlayers();
 		for(int i = 0; i < players.size(); i++) {
 			players.get(i).setScore(0);
 			players.get(i).setLat(0);
 			players.get(i).setLng(0);
 			players.get(i).setVotedAgainst(null);
 			players.get(i).setWerewolf(false);
 			players.get(i).setUserId(null);
 		}
 		return players;
 	}
 	
 	@RequestMapping(value = "/players/vote", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse voteForPlayer(@RequestParam("voted") String voted, Principal principal)
 	{
 		System.out.println("voted " + voted);
 		JsonResponse response = new JsonResponse("success");
 		
 		try {
 			WerewolfUser voter = userDAO.getUserByUsername(principal.getName());
 			if(!gameService.vote(voter.getId(), voted)) {
 				response.setStatus("failure;");
 			}
 		} catch (Exception e) {
 			response.setStatus("failure;" + e.getStackTrace().toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/players/kill", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse killPlayerById(@RequestParam("victim") String victim, Principal principal) throws NoPlayerFoundException
 	{
 		JsonResponse response = new JsonResponse("success");
 		WerewolfUser user = userDAO.getUserByUsername(principal.getName());
 		try {
 			 if(!gameService.kill(user.getId(), victim)) {
 				 response.setStatus("failed");
 			 }
 		} catch (Exception e) {
 			logger.info("Exception thrown.");
 			response.setStatus("failure;" + e.getStackTrace().toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/players/scent", method=RequestMethod.GET)
 	public @ResponseBody List<Player> scent(Principal principal) {
 		try {
 		WerewolfUser user = userDAO.getUserByUsername(principal.getName());
 		List<Player> players = gameService.scent(user.getId());
 		if(players == null) {
 			// Person making a the request is not a werewolf.
 			players= new ArrayList<Player>();
 			Player player = new Player("NOT A WEREWOLF", false, 0, 0, "NOT A WEREWOLF", false, false);
 			players.add(player);
 			return players;
 			
 		}
 		List<Player> killplayers = gameService.killable(user.getId());
 		for(int i = 0; i < players.size(); i++) {
 			if(killplayers != null && killplayers.size() != 0 && killplayers.contains(players.get(i))) {
 				// Is a kill-able player
 				players.get(i).setScore(1);
 			}
 			else{
 				// Is a nearby, but not a kill-able player
 				players.get(i).setScore(0);
 			}
 			if(players.get(i).isWerewolf()) {
 				// If a scented player is a werewolf the score the other player sees is 2.
 				players.get(i).setScore(2);
 			}
 			players.get(i).setLat(0);
 			players.get(i).setLng(0);
 			players.get(i).setVotedAgainst(null);
 			players.get(i).setWerewolf(false);
 			players.get(i).setUserId(null);
 		}
 		return players;
 		} catch (Exception e){
 			return null;
 		}
 		
 	}
 	
 	@RequestMapping(value = "/players/location", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setLocation(@RequestParam("lng") double lng, @RequestParam("lat") double lat, Principal principal) {
 		JsonResponse response = new JsonResponse("success");
 		try {
 			WerewolfUser user = userDAO.getUserByUsername(principal.getName());
 			GPSLocation location = new GPSLocation();
 			location.setLat(lat);
 			location.setLng(lng);
 			gameService.updatePosition(user.getId(), location);
 		} catch (Exception e) {
 			// TODO: handle exception
 			response.setStatus("failure;\n" + e.toString());
 			e.printStackTrace();
 		}
 
 		return response;
 	}
 	
 	@RequestMapping(value = "/", method=RequestMethod.GET)
 	public String home(ModelMap model) {
 		return "home";
 	}
 	
 	@RequestMapping(value = "/players", method=RequestMethod.GET)
 	public String playerMenu(ModelMap model, Principal principal) {
 		return "player";
 	}
 	
 	@RequestMapping(value = "/logout", method=RequestMethod.GET)
 	public String rage(ModelMap model, Principal principal) {
 		System.out.println(principal.getName());
 		System.out.println(principal.toString());
 		return "logout";
 	}
 	
 	@RequestMapping(value = "/admin", method=RequestMethod.GET)
 	public String admin(ModelMap model, Principal principal) {
 		return "admin";
 	}
 	
 	@RequestMapping(value = "/gameStats", method=RequestMethod.GET)
 	public @ResponseBody JsonResponse gameStats(ModelMap model, Principal principal) {
 		JsonResponse response = new JsonResponse("success");
 		System.out.println(principal.getName());
 		response.setGameStatus("isOver");
 		if(gameService.isOver()) {
 			return response;
 		}
 		response.setGameStatus(gameService.getGame().isNight() + " " + gameService.getAllAlive().size());
 		if(principal != null && principal.getName() != null) {
 			System.out.println("MADE IT");
 			WerewolfUser user = userDAO.getUserByUsername(principal.getName());
 			Player player = playerDAO.getPlayerByID(user.getId());
 			response.setCreated(gameService.getGame().getTimer() +"");
 			response.setNightFreq(gameService.getGame().getDayNightFreq() +"");
 			response.setIsDead(player.isDead() + "");
 			response.setIsWerewolf(player.isWerewolf()+"");
 			/*if(player.isWerewolf()) {
 				 response.setKills(gameService.getNumKills(player) + "");
 			}*/
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/admin/test", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse testGame(Principal principal){
 		JsonResponse response = new JsonResponse("success");
 		try {
 			userDAO.clearUsers();
 		}catch(Exception e) {
 			response.setStatus("failure;\n" + e.getStackTrace().toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/admin/newgametest", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse testNewGame(Principal principal){
 		JsonResponse response = new JsonResponse("success");
 		try {
 			gameService.newGameTest(60000);
 		}catch(Exception e) {
 			response.setStatus("failure;\n" + e.getStackTrace().toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/addUser", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse addUser(@RequestParam("userName")String username, @RequestParam("id")String id,
 			@RequestParam("firstName")String firstName, @RequestParam("lastName")String lastName,
 			@RequestParam("hashedPassword")String hashedPassword, Principal principal)
 	{
 		JsonResponse response = new JsonResponse("success");
 		try {
 		String imageURL = "";
 		BCryptPasswordEncoder encoded = new BCryptPasswordEncoder();
 		Collection<GrantedAuthorityImpl> auth = new ArrayList<GrantedAuthorityImpl>();
 		auth.add(new GrantedAuthorityImpl("ROLE_USER"));
 		WerewolfUser user = new WerewolfUser(id, firstName, lastName, username, encoded.encode(hashedPassword), imageURL);
 		userDAO.createUser(user);
 		} catch (Exception e) {
 			response.setStatus("failure);" + e.getMessage().toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/players/getVotable", method=RequestMethod.GET)
 	public List<Player> getVotable(Principal principal)
 	{
 		return gameService.getAllAlive();
 
 	}
 	
 	@RequestMapping(value = "/highscores", method=RequestMethod.GET)
 	public @ResponseBody List<WerewolfUser> allTimeHighscoreList(){
 		List<WerewolfUser> scoreList =  userDAO.getAllUsers();
 		for(int i = 0; i < scoreList.size(); i++) {
 			scoreList.get(i).setAdmin(false);
 			scoreList.get(i).setFirstName("");
 			scoreList.get(i).setLastName("");
 			scoreList.get(i).setHashedPassword("");
 			scoreList.get(i).setImageURL("");
 		}
 		return scoreList;
 	}
 	
 	@RequestMapping(value = "/game", method=RequestMethod.GET)
 	public @ResponseBody boolean gameIsRunning() {
 		return !gameService.isOver();
 	}
 	
 	public void timeIteration()
 	{
 		if(gameService.getGame()!=null&& !gameService.isOver() && wasDay && gameService.getGame().isNight() && (long)(new Date()).getTime() / (gameService.getGame().getDayNightFreq()*2) != 0 ) {
 			
 			logger.info("Going to get the most votes for day " + ((long)(new Date()).getTime() - gameService.getGame().getTimer()) / (gameService.getGame().getDayNightFreq()*2));
 			List<Vote> voteList = voteDAO.mostVotes(((long)(new Date()).getTime() - gameService.getGame().getTimer()) / (gameService.getGame().getDayNightFreq()*2));
 			logger.info("Vote List: " + voteList.toString());
 			for(int i = 0; i < voteList.size(); i++) {
 				if(gameService.voteKill(voteList.get(i).getName())) {
 					gameService.endGame();
 				}
 			}
 			wasDay = false;
 		}
 		if(gameService.getGame()!=null && !gameService.isOver() &&!wasDay && !gameService.getGame().isNight()) {
 			wasDay = true;
 		}// Players must update every 5 minutes, do not have to update for the first ten minutes, could be changed to fit game time
 		if(gameService.getGame() != null && ((((new Date()).getTime())- gameService.getGame().getTimer()) / 60000) % 5  == 0 && ((((new Date()).getTime())- gameService.getGame().getTimer()) / 60000) / 5 != 0){
 			gameService.checkLocationUpdates();
 		}
 	}
 	
 	
 }
