 package fr.airsoft.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class IndexController {
 
 	@RequestMapping({ "/", "/index.html" })
 	public String index(Model model) {
 		model.addAttribute("Title", "OP AIRSOFT");
		String nulle = null;
		nulle.equals("");
 		return "index";
 	}
 }
