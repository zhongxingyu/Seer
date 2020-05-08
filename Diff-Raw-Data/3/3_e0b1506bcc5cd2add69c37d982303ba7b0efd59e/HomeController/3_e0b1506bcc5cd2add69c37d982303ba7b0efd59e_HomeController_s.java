 package com.codesessions.spring;
 
 import java.util.Comparator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Handles requests for the application home page.
  * @author Vladimir Cetkovic
  */
 @Controller
 public class HomeController {
 
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 
 	@Autowired
 	Comparator<String> comparator;
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value="/", method=RequestMethod.GET)
 	public String home() {
 		logger.info("handling home request, returning home view");
 		return "home";
 	}
 	
 	@RequestMapping(value = "/compare", method = RequestMethod.GET)
 	public String compare(@RequestParam("input1") String input1,
 			@RequestParam("input2") String input2, Model model) {
 
 		int result = comparator.compare(input1, input2);
 		String inEnglish = (result < 0) ? "less than" : (result > 0 ? "greater than" : "equal to");
 
 		String output = "According to our Comparator, '" + input1 + "' is " + inEnglish + "'" + input2 + "'";
 
 		model.addAttribute("output", output);
 		return "compareResult";
 	}
 	private void testMethod1(){
 		logger.info("dodatak koji je napravio drugi developer");
 	}
 	
 }
 
