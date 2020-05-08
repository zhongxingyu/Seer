 package com.khobar.springgames.controller;
 
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.khobar.springgames.domain.Discipline;
 import com.khobar.springgames.domain.Player;
 import com.khobar.springgames.repository.DisciplineRepository;
 import com.khobar.springgames.repository.PlayerRepository;
 import com.khobar.springgames.service.DisciplineService;
 import com.khobar.springgames.service.PlayerService;
 
 @Controller
 @RequestMapping("/player")
 public class PlayerController {
 
 	@Autowired
 	private PlayerRepository playerRepository;
 
 	@Autowired
 	private PlayerService playerService;
 
 	@Autowired
 	private DisciplineRepository discRepository;
 
 	@Autowired
 	private DisciplineService disciplineService;
 
 	@ModelAttribute("disciplines")
 	public List<Discipline> disciplines() {
 		return discRepository.findAll();
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String addPlayer(Model model) {
 		model.addAttribute("player", new Player());
 		return "player/edit";
 	}
 
 	@RequestMapping(value = "/list", method = RequestMethod.GET)
 	public String listPlayers(@RequestParam(required = false) String name,
 			Model model) {
 		List<Player> playerList;
 		if (name == null) {
 			playerList = playerRepository.findAll();
 		} else {
 			playerList = playerRepository.findByName(name);
 		}
 		model.addAttribute("playerList", playerList);
 		return "player/list";
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.POST)
 	public String savePlayer(@Valid @ModelAttribute Player player,
 			BindingResult result) {
 		if (result.hasErrors()) {
 			return "player/edit";
 		} else {
 			disciplineService.addNo(player.getDiscipline());
 			playerService.save(player);
 			return "redirect:./list";
 		}
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public String editPlayer(@PathVariable Integer id, Model model) {
 		Player player = playerRepository.findOne(id);
 
 		if (player != null) {
 			model.addAttribute("player", player);
 			return "player/edit";
 		} else {
 			return "redirect:.";
 		}
 	}
 
 	@RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
 	public String deletePlayer(@PathVariable Integer id, Model model) {
 		Player player = playerRepository.findOne(id);
 		if (player != null) {
 			playerRepository.delete(id);
 			return "redirect:/player/list";
 		} else {
 			return "redirect:.";
 		}
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
 	public String savePlayer(@PathVariable Integer id,
 			@Valid @ModelAttribute Player player, BindingResult result) {
 		if (!result.hasErrors()) {
 			player.setId(id);
 			Player playerInRepo = playerRepository.findOne(player.getId());
 			if (playerInRepo != null) {
 				Discipline oldDisc = playerInRepo.getDiscipline();
 				System.out.println("Old discipline" + oldDisc.getName());
 				Discipline newDisc = player.getDiscipline();
 				System.out.println("New discipline" + newDisc.getName());
 				if (!oldDisc.getName().equals(newDisc.getName())) {
					disciplineService.updateNo(oldDisc, newDisc); //number not updating 
 				}
 			} else {
 				playerRepository.save(player);
 			}
 		}
 		return "player/edit";
 	}
 
 }
