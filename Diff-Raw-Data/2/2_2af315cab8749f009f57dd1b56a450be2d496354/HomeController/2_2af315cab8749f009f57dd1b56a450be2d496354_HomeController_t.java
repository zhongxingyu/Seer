 package edu.wm.werewolf;
 
 import java.security.Principal;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 
 import edu.wm.werewolf.domain.GPSLocation;
 import edu.wm.werewolf.domain.Game;
 import edu.wm.werewolf.domain.Kill;
 import edu.wm.werewolf.domain.Player;
 import edu.wm.werewolf.domain.User;
 import edu.wm.werewolf.service.GameService;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	@Autowired 
 	private GameService gameService;
 	
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) 
 	{
 		logger.info("Welcome home! The client locale is {}.", locale);
 		
 		Date date = new Date();
 		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
 		String formattedDate = dateFormat.format(date);
 		
 		model.addAttribute("serverTime", formattedDate);
 		
 		return "home";
 	}
 	
 	@RequestMapping(value = "/addUser", method=RequestMethod.POST)
	public void newUser(@RequestParam("user") User user)
 	{
 		logger.info("Adding new user : " + user.getFirstName() + user.getHashedPassword() + user.getId()
 				+user.getLastName());
 		gameService.addUser(user);
 	}
 	
 	@RequestMapping(value = "admin/newGame", method=RequestMethod.POST)
 	public void newGame(Player player, int dayNightFreq)
 	{
 		if (gameService.isAdmin(player)) 
 		{
 			gameService.newGame(dayNightFreq);
 			logger.info("New game");
 		}
 		else 
 		{
 			logger.info("New game could not be created. User is not Admin");
 		}
 	}
 	
 	@RequestMapping(value = "/players/alive", method=RequestMethod.GET)
 	public @ResponseBody List<Player> getAllAlive()
 	{
 		logger.info("Get all alive");
 		List<Player> players = gameService.getAllAlive();
 		return players;
 	}
 
 	@RequestMapping(value = "/players/near", method=RequestMethod.GET)
 	public @ResponseBody List<Player> getAllPlayersNear(Player player)
 	{
 		logger.info("Get all players near");
 		List<Player> players = gameService.getAllPlayersNear(player);
 		return players;
 	}
 	
 	@RequestMapping(value = "players/kill", method=RequestMethod.POST)
 	public void setKill(Player killer, Player victim)
 	{
 		logger.info("in kill");
 		if (gameService.canKill(killer, victim))
 		{
 			Kill kill = new Kill(killer.getId(), victim.getId(), new Date(), killer.getLat(), killer.getLng());
 			gameService.setKill(kill);
 			logger.info("Set kill");
 		}
 	}
 	
 	@RequestMapping(value = "/location", method=RequestMethod.POST)
 	public void setLocation(GPSLocation location, Principal principal)
 	{
 		logger.info("Setting location");
 		gameService.updatePosition(principal.getName(), location);
 	}
 	
 	@RequestMapping(value = "/players/vote", method=RequestMethod.POST)
 	public void vote(Player player, Player votee)
 	{
 		gameService.vote(player, votee);
 		logger.info("Vote cast");
 	}	
 	
 	@RequestMapping(value = "/players/new", method=RequestMethod.POST)
 	public void newPlayer(Player player)
 	{
 		logger.info("Adding new player");
 		gameService.addPlayer(player);
 	}
 	
 	public void CheckGameOperation () {
 		logger.info("Checking game operation...");
 		gameService.checkGame();
 	}
 }
