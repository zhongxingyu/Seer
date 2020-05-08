 package com.github.kpacha.mafia.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.github.kpacha.mafia.service.PopulatorService;
 
 @Controller
@RequestMapping("/populate")
 public class PopulatorController {
     private static final Logger log = LoggerFactory
 	    .getLogger(PopulatorController.class);
 
     @Autowired
     private PopulatorService populator;
 
    @RequestMapping("/{deep}")
     public String populateByParam(Model model, @PathVariable int deep) {
 	return doPopulate(model, deep);
     }
 
     public String populate(Model model) {
 	return doPopulate(model, 3);
     }
 
     private String doPopulate(Model model, int deep) {
 	log.info("Populating the db with deep=" + deep);
 	int gangSize = populator.populate(deep);
 	log.info("Created " + gangSize + " instances");
 	return "forward:/search/sample";
     }
 }
