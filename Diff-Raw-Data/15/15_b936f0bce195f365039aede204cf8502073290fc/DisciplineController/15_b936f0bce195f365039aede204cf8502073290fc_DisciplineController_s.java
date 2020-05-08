 package com.khobar.springgames.controller;
 
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.khobar.springgames.domain.Discipline;
 import com.khobar.springgames.domain.Game;
 import com.khobar.springgames.domain.Player;
 import com.khobar.springgames.repository.DisciplineRepository;
 
 @Controller
 @RequestMapping("/discipline")
 public class DisciplineController {
 
 	@Autowired
 	DisciplineRepository discRepo;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String addDiscipline(Model model) {
 		model.addAttribute("discipline", new Discipline());
 		return "discipline/edit";
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.POST)
 	public String saveDiscipline(@Validated @ModelAttribute Discipline disc,
 			BindingResult result) {
 		if (result.hasErrors()) {
 			return "discipline/edit";
 		} else {
 			System.out.println(disc.getPlayersNumber());
 			if (disc.getPlayersNumber() < 2) {
				disc.setPlayersNumber(2);// set 2 , as there is problem with H2
											// DB
 			}
 			Discipline saved = discRepo.save(disc);
 			return "redirect:./list";
 		}
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
 	public String editDiscipline(@PathVariable Integer id, Model model) {
 		Discipline disc = discRepo.findOne(id);
 
 		if (disc != null) {
 			model.addAttribute("discipline", disc);
 			return "discipline/edit";
 		}
 		return "redirect:.";
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
 	public String saveDisc(@PathVariable Integer id,
 			@Valid @ModelAttribute Discipline disc, BindingResult result) {
 		if (!result.hasErrors()) {
 			disc.setId(id);
 			discRepo.save(disc);
 		}
 
 		return "discipline/edit";
 	}
 
 	@RequestMapping(value = "/list", method = RequestMethod.GET)
 	public String listDisciplines(@RequestParam(required = false) String name,
 			Model model) {
 		List<Discipline> distList;
 		if (name == null) {
 			distList = discRepo.findAll();
 		} else {
 			distList = discRepo.findByName(name);
 		}
 		model.addAttribute("distList", distList);
 		return "discipline/list";
 	}

 	@RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
 	public String deleteDiscipline(@PathVariable Integer id, Model model) {
 		Discipline disc = discRepo.findOne(id);
 		if (disc != null) {
 			discRepo.delete(disc);
			return "/discipline/list";
 		} else {
 			return "redirect:.";
 		}
 	}
 }
