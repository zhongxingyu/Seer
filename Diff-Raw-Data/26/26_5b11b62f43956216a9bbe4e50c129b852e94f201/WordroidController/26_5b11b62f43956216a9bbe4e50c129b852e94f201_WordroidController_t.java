 package com.hoyoul.wordroid.controller;
 
import javax.servlet.http.HttpServletRequest;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.hoyoul.wordroid.service.WordService;
 
 @Controller
 public class WordroidController {
 	
 	@Autowired
 	private WordService wordService;
 	
 	@RequestMapping(value = "study", method = RequestMethod.GET)
 	public String study() {	
 		return "study";
 	}
 
 	@RequestMapping(value = "data", method = RequestMethod.GET)
	public String data(Model model,HttpServletRequest request) {
 		
		model.addAttribute("wordList",wordService.listWord());
 		
 		return "data";
 	}	
 }
