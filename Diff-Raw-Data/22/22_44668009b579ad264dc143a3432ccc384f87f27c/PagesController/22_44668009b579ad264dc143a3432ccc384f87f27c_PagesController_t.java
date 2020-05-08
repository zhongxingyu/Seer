 package net.canadensys.dataportal.vascan.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.canadensys.dataportal.vascan.TaxonService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class PagesController {
 	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
 
 	@Autowired
 	private TaxonService taxonService;
 	
 	@RequestMapping(value={"/about"}, method={RequestMethod.GET})
 	public ModelAndView handleAboutPage(){
 		
 		Map<String,String> model = new HashMap<String,String>();
 		model.put("currentDate", SDF.format(Calendar.getInstance().getTime()));
 		return new ModelAndView("about", model);
 	}
 }
