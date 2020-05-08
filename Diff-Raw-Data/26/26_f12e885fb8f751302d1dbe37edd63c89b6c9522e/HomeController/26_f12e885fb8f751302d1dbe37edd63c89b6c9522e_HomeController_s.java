 package com.id3alsolutions.web;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.id3alsolutions.domain.Measurement;
 import com.id3alsolutions.domain.Species;
 import com.id3alsolutions.service.SpeciesService;
 
 @Controller
 public class HomeController {
 	@Autowired
 	SpeciesService speciesService;
 
 	@Autowired
 	MessageSource messageSource;
 
 	private Logger logger = Logger.getLogger(HomeController.class);
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, HttpServletRequest request) {
 		Iterable<Species> species = speciesService.findAll();
 		request.setAttribute("species", species);
 		return "home";
 	}
 
 	@RequestMapping(value = "/calculateCollision", method = RequestMethod.POST)
 	public String calculateCollision(@RequestParam("dbh") int dbh,
 			@RequestParam("species") String speciesName,
 			@RequestParam("boulevardSize") int boulevardSize,
 			HttpServletRequest request) {
 		Iterable<Species> species = speciesService.findAll();
 		request.setAttribute("species", species);
 		String collisionMessage = messageSource.getMessage(
 				"boulevard.collision", null, LocaleContextHolder.getLocale());
 		Species specie = speciesService.findByName(speciesName);
 		if (specie != null) {
 			Measurement crownWidth = speciesService.calculateCrownWidthInFeet(
 					specie, dbh);
 			request.setAttribute("crownWidth", crownWidth);
 			Measurement rootFlare = speciesService.calculateRootFlareInInches(
 					specie, dbh);
 			request.setAttribute("rootFlare", rootFlare);
 			// Check for collision
 			if (boulevardSize <= rootFlare.getValueInInches()) {
 				logger.info("Found collision for species: " + speciesName
 						+ " with dbh: " + dbh);
 				request.setAttribute("collisionMessage", collisionMessage);
 			}
 		}
 		return "home";
 	}
 }
