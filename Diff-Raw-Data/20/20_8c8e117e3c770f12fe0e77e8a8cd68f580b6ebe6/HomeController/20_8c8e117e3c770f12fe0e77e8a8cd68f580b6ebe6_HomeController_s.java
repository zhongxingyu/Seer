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
 public class HomeController {
 
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Autowired
 	private ApartmentDAO mApartmentDAO;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		model.addAttribute("cities", new Gson().toJson(mApartmentDAO.findAllCities()));
 		return "home";
 	}
 
 	@RequestMapping(value = "/apartments", method = RequestMethod.GET)
 	public String getApartments(Locale locale, Model model) {
 		model.addAttribute("apartments", new Gson().toJson(mApartmentDAO.findAll()));
 		return "apartments";
 	}
 
 	@RequestMapping(value = "/apartments", method = RequestMethod.POST)
 	public String postApartments(Locale locale, Model model, @RequestParam("city") String city) {
 		if (city.equals("")) {
 			model.addAttribute("apartments", new Gson().toJson(mApartmentDAO.findAll()));
 		} else {
 			model.addAttribute("apartments", new Gson().toJson(mApartmentDAO.findAllByCity(city)));
 		}
 		return "apartments";
 	}
 
 	@RequestMapping(value = "/flat", method = RequestMethod.GET)
 	public String flat(Locale locale, Model model) {
 		return "flat";
 	}
 
 	// @RequestMapping(value = "/del", method = RequestMethod.GET)
 	// public String del(Locale locale, Model model) {
 	// List<Apartment> findAll = mApartmentDAO.findAll();
 	// for(Apartment apartment : findAll) {
 	// mApartmentDAO.delete(apartment);
 	// }
 	// return "home";
 	// }
 }
