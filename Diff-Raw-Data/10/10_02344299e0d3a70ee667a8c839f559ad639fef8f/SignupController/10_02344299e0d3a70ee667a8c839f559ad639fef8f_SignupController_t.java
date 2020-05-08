 package ie.cit.cloudapp.web;
 
 import ie.cit.cloudapp.Player;
 import ie.cit.cloudapp.PlayerRepository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping("signup")
 public class SignupController {
 
 	@Autowired
 	PlayerRepository playerRepository;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public void justShowPage(Model model) {
 		model.addAttribute("players", playerRepository.getPlayers());
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public void createNewPlayer(Model model, @RequestParam String firstName,
 			@RequestParam("surname") String lastName, @RequestParam int age) {
 		Player player = new Player();
 		player.setFirstName(firstName);
 		player.setSurname(lastName);
 		player.setAge(age);
 
 		playerRepository.addPlayer(player);
 		model.addAttribute("players", playerRepository.getPlayers());
 	}
 
 	
	@RequestMapping(method = RequestMethod.DELETE)
	public void deletePlayer(Model model, @RequestParam int playerId) {
		playerRepository.getPlayers().remove(playerId - 1);
		model.addAttribute("players", playerRepository.getPlayers());
	}
 
 }
