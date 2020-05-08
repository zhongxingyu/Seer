 package patel.dipesh.jsr303.controller;
 
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import patel.dipesh.jsr303.model.Address;
 import patel.dipesh.jsr303.model.Person;
 
 /**
  * 
  * Sample controller for processing input for our Person and Address objects
  * Note that sample is using velocity template engine for UI rendering.  
  * Controller doesn't care as it uses logical view name.
  * @author Dipesh Patel
  *
  */
 
 @Controller
 public class RegistrationController {
 
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/*
 	 * Present input form
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String presentRegistrationForm(ModelMap model) {
 				
 		//  Form fields will be binded to Person and Address object.
 		Person person = new Person() ;
 						
 		//  Make Models available to form ;
 		model.addAttribute("person", person) ;
 		return "inputForm" ;
 		
 	}	
 
 	/*
 	 * Input processor.  Present results page or error page which is registration page with in-line error
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.POST)
 	public String processRegistrationForm(@ModelAttribute("person") @Valid Person person, BindingResult result, ModelMap model) {	
 
 		/*
 		 * Spring will invoke Hibernate's JSR303 validator for validating user input that maps to
 		 * Person / Address objects 
 		 */
 		
 		if (result.hasErrors()) {
 			
 			//  Error during registration!!!
 			log.error("Data validation error during form processing.");	
 			model.addAttribute("person", person) ;
 			return "inputForm" ;
 			
 		} else {
 
 			//  load success page.
 			log.info("JSR303 data validation success.") ; 
 			
 			/**** Perform your business logic processing ;  This sample has none ; ****/
 			
 			//  Go to success page.
 			return "results" ;			
 		}
 	}

 	
 	/*
 	 * Error page template
 	 */
 	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String showError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		log.error("Invalid request.") ;
 		return "error" ;
 		
 	}	
		
 
 }
