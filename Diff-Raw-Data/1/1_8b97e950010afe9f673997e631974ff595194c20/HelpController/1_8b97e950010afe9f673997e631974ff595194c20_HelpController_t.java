 package com.hansson.rento.controllers;
 
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.gson.Gson;
 import com.hansson.rento.dao.ApartmentDAO;
 
 @Controller
 public class HelpController {
 
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Autowired 
 	private ApartmentDAO mApartmentDAO;
 	
 	@RequestMapping(value = "/help", method = RequestMethod.GET)
 	public String help(Locale locale, Model model) {
		model.addAttribute("cities", new Gson().toJson(mApartmentDAO.findAllCities()));
 		return "view_help";
 	}
 
 	@RequestMapping(value = "/landlords", method = RequestMethod.GET)
 	public String getLandlords(Locale locale, Model model) {
 		model.addAttribute("apartments", new Gson().toJson(mApartmentDAO.findAllByCity("Karlskrona")));
 		return "landlords";
 	}
 	
 	@RequestMapping(value = "/landlords", method = RequestMethod.POST)
 	public String postLandlords(Locale locale, Model model,  @RequestParam("city") String city) {
 		model.addAttribute("apartments", new Gson().toJson(mApartmentDAO.findAllByCity(city)));
 		return "landlords";
 	}
 
 }
