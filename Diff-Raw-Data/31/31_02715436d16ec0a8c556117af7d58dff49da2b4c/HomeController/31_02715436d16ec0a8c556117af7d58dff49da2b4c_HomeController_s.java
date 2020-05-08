 package com.hansson.rento.controllers;
 
import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.google.gson.Gson;
 import com.hansson.rento.dao.ApartmentDAO;
 import com.hansson.rento.entities.Apartment;
 
 @Controller
 public class HomeController {
 
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 	
 	@Autowired
 	private ApartmentDAO mApartmentDAO;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		List<Apartment> apartmentList = mApartmentDAO.findAll();
 		model.addAttribute("apartments", new Gson().toJson(apartmentList));
 		return "home";
 	}
 
 	@RequestMapping(value = "/flat", method = RequestMethod.GET)
 	public String flat(Locale locale, Model model) {
 		return "flat";
 	}
 }
