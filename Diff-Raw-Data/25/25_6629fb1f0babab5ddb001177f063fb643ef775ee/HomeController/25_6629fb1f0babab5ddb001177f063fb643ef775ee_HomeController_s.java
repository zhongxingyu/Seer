 package edu.wm.camckenna.werewolves;
 
 import java.security.Principal;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.wm.camckenna.validators.ChangePasswordUserValidator;
 import edu.wm.camckenna.validators.TempUserValidator;
 import edu.wm.camckenna.werewolves.domain.BooleanMessage;
 import edu.wm.camckenna.werewolves.domain.ChangePasswordUser;
 import edu.wm.camckenna.werewolves.domain.GPSLocation;
 import edu.wm.camckenna.werewolves.domain.JsonResponse;
 import edu.wm.camckenna.werewolves.domain.Kill;
 import edu.wm.camckenna.werewolves.domain.Player;
 import edu.wm.camckenna.werewolves.domain.TempUser;
 import edu.wm.camckenna.werewolves.domain.User;
 import edu.wm.camckenna.werewolves.domain.Vote;
 import edu.wm.camckenna.werewolves.service.GameService;
 import edu.wm.camckenna.werewolves.service.UserService;
 
 
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	@Autowired GameService gameService;
 	@Autowired UserService userService;
 	// Autowired finds a satisfied bean of being a playerDAO and connects the controller to it
 	// Beans are defined in root-context (these are the Singleton classes)
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! The client locale is {}.", locale);
 		
 		Date date = new Date();
 		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
 		
 		String formattedDate = dateFormat.format(date);
 		
 		model.addAttribute("serverTime", formattedDate );
 		
 		return "home";
 	}
 	/*
 	 * For testing purposes
 	 */
 	@RequestMapping(value = "/players", method=RequestMethod.GET)
 	public @ResponseBody List<Player> getAllPlayers()
 	{
 		// add responseBody to package as a JSON object
 		List<Player> players = gameService.getAllPlayers();
 		return players;
 	}
 	
 	@RequestMapping(value = "/players/alive", method=RequestMethod.GET)
 	public @ResponseBody List<String> getAllAlive()
 	{
 		// add responseBody to package as a JSON object
 		return gameService.getAllAliveAsStrings();
 	}
 
 	@RequestMapping(value = "/dayToNight", method=RequestMethod.POST)
 	public @ResponseBody String switchToNight()
 	{
 		gameService.switchFromDayToNight();
 		return "Switching to night";
 	}
 	@RequestMapping(value = "/nightToDay", method=RequestMethod.POST)
 	public @ResponseBody String switchToDay()
 	{
 		gameService.switchFromNightToDay();
 		return "Switching to day";
 	}
 	@RequestMapping(value = "/nearby", method=RequestMethod.GET)
 	public @ResponseBody List<String> getAllNearby(Principal principal)
 	{
 		List<String> players = gameService.getAllNearby(principal.getName());
 		return players;
 	}
 	@RequestMapping(value = "/votable", method=RequestMethod.GET)
 	public @ResponseBody List<Player> getVotablePlayers()
 	{
 		//If alive, can vote
 		List<Player> players = gameService.getAllAlive();
 		return players;
 	}
 
 
 	@RequestMapping(value = "/start", method= RequestMethod.GET)
 	public String startGame()
 	{
 		return "restart";		
 	}
 	@RequestMapping(value = "/restart", method= RequestMethod.POST)
 	public @ResponseBody String restartGame(@RequestParam("frequency") String freq, 
 			@RequestParam("killRange") String kill, @RequestParam("scentRange") String scent)
 	{
 		gameService.startGame(freq, scent, kill);
 		return "Restarted game";		
 	}
 
 	@RequestMapping(value = "/logout", method=RequestMethod.GET)
 	public String logout(Principal principal)
 	{
 		SecurityContextHolder.clearContext();
 		return "logout"; //Goes to logout.jsp, 
 	}
 	@RequestMapping(value = "/vote", method=RequestMethod.GET)
 	public String voteResult(Principal principal)
 	{
 		return "vote"; //Goes to vote.jsp, the form page
 	}
 	@RequestMapping(value = "/voteForPlayer", method=RequestMethod.POST)
 	public @ResponseBody String voteSubmit(@RequestParam("username") String name, Principal principal)
 	{
 		
 		gameService.voteForPlayer(principal.getName(), name);
 		
 		return (principal.getName() + " voted for " + name);
 	}
 
 	@RequestMapping(value = "/kill", method= RequestMethod.GET)
 	public String killResult(Principal principal)
 	{
 		return "kill"; //go to kill.jsp
 	}
 	@RequestMapping(value = "/killPlayer",  method=RequestMethod.POST)
 	public @ResponseBody String killPlayer(@RequestParam("username") String name, Principal principal)
 	{
 
 		gameService.kill(principal.getName(), name);
 		return (principal.getName() + " attempted to kill " + name);
 	}
  
 	@RequestMapping(value= "/location", method=RequestMethod.POST)
 	public @ResponseBody String setLocation(@RequestParam("lat") double lat, 
 			@RequestParam("lng") double lng, Principal principal){		
 		
 		logger.info("Setting for " + principal.getName() + "'s location to: " + lat + ", " + lng);
 		gameService.updatePosition(principal.getName(), lat, lng);
 		return "Setting for " + principal.getName() + "'s location to: " + lat + ", " + lng;
 	}
 
 
 	@RequestMapping(value= "/home", method=RequestMethod.GET)
 	public @ResponseBody String checkGameOperation(){		
 
 		gameService.checkGameOperation();
 		return "Checking operation...";			
 	}
  
 	@RequestMapping(value= "/scores", method=RequestMethod.GET)
 	public @ResponseBody List<String> getScores(Principal principal){				
 		List<String> scores = gameService.getScores();
 		return scores;			
 	}	
 	@RequestMapping(value= "/info", method=RequestMethod.GET)
 	public @ResponseBody Map<String, List<String>> getStatus(Principal principal){				
 		return gameService.getPlayerInfo(principal.getName());
 	}
 	@RequestMapping(value= "/listOfPlayers", method=RequestMethod.GET)
 	public @ResponseBody Map<String, String> getListOfPlayers(Principal principal){				
 		return gameService.getListOfPlayers(principal.getName());
 	}
 	@RequestMapping(value= "/day", method=RequestMethod.GET)
 	public @ResponseBody String getDayOrNight(Principal principal){				
 		return gameService.getDayOrNight();
 	}
 	@RequestMapping(value= "/getVotes", method=RequestMethod.GET)
 	public @ResponseBody List<Vote> getVotes(Principal principal){				
 		return gameService.getVotes(principal.getName());
 	}
 	@RequestMapping(value= "/getKills", method=RequestMethod.GET)
 	public @ResponseBody List<Kill> getKills(Principal principal){				
 		return gameService.getKills(principal.getName());
 	}
 	
 	/*
 	@RequestMapping(value = "/register", method=RequestMethod.POST)
 	public @ResponseBody String register(
 			@RequestParam("email") String email,
 			@RequestParam("firstName") String firstName, 
 			@RequestParam("lastName") String lastName,
 			@RequestParam("username") String username,
 			@RequestParam("password") String password,
 			@RequestParam("confirmPassword") String confirmPassword
 			){
 		
 		userService.createAccount(new User(UUID.randomUUID().toString(), firstName, lastName, username, email, password, null));
 				
 		return ("Registered");
 	}*/
 	@RequestMapping(value = "/register", method=RequestMethod.GET)
 	public ModelAndView register() {
 		return new ModelAndView("register", "command", new TempUser());
 	}
 	
 	@RequestMapping(value = "/register", method = RequestMethod.POST)
 	public String addStudent(@ModelAttribute TempUser tempUser, BindingResult result) {
 
 		
 	      logger.info("Got to the Post");
 	      logger.info(tempUser.getUsername());
 	      TempUserValidator validator = new TempUserValidator();
 	      validator.validate(tempUser, result);
 	      if(result.hasErrors()){
 	    	  logger.info("There were errors");
 	    	  return "redirect:/home";
 	      }
 	      else{
 	    	  logger.info("Going to user service");
 		      userService.createAccount(tempUser);
 		      return "registered";
 	      }	     
 	 }
 	@RequestMapping(value = "/delete", method=RequestMethod.GET)
 	public String deletePage(Principal principal) {
 		return "delete";
 	}	
 	@RequestMapping(value = "/delete", method= RequestMethod.POST)
 	public String deleteAccount(Principal principal, @RequestParam("password") String password)
 	{
 		if(userService.deleteAccount(principal.getName(), password)){
 			return "redirect:/home"; 
 		}
 		else{
 	    	  logger.info("There were errors");
 	    	  return "redirect:/";
 		}
 		
 	}
 
 	
 	@RequestMapping(value = "/gameStats", method= RequestMethod.GET)
	public @ResponseBody Map<String, Integer> getGameStats()
 	{
 		return gameService.getGameStats();
 	}
 	
 }
