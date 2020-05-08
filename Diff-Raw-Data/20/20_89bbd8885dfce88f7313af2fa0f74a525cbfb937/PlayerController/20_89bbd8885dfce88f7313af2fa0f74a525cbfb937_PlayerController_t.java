 package org.bpaye1.research.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.bpaye1.research.model.Player;
import org.bpaye1.research.model.Position;
 import org.bpaye1.research.repository.PlayerRepository;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Transactional
 @Controller
 public class PlayerController {
 	
 	@Inject
 	PlayerRepository repository;
 	
 	@InitBinder
 	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
 	}
 	
 	@RequestMapping(value="/players", method=RequestMethod.GET)
 	public String players(Model model){
 		model.addAttribute("players", repository.list());
 		return "players";
 	}
 	
 	@RequestMapping(value="/players/player", method=RequestMethod.GET)
 	public String add(Model model){
 		model.addAttribute("player", new Player());
		model.addAttribute("positions", Position.values());
 		return "player";
 	}
 	
 	@RequestMapping(value="/players/player", method=RequestMethod.POST)
 	public String add(@Valid Player player, BindingResult bindingResult){
 		if(bindingResult.hasErrors()){
 			return "player";
 		}
 		repository.add(player);
 		return "redirect:/players/";
 	}
 	
 	@RequestMapping(value="/players/player/{id}", method=RequestMethod.GET)
 	public String edit(@PathVariable Long id, Model model){
 		model.addAttribute("player", repository.find(Long.valueOf(id)));
 		return "player";
 	}
 
 }
 	
