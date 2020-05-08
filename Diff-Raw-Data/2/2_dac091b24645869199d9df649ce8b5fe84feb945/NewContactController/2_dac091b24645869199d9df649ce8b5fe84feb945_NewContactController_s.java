 package au.com.some.dodgy.company.webapp.web.controllers;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import au.com.some.dodgy.company.webapp.web.formobjects.NewContactFormModel;
 
 @Controller
 @RequestMapping("/newContact")
 @Scope("request")
 public class NewContactController {
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView onPageLoad() {
 		ModelAndView pageLoadModelAndView = new ModelAndView("newContact");
 		pageLoadModelAndView.addObject("newContactFormModel", new NewContactFormModel());
 		return pageLoadModelAndView;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
	public String onNewContact(NewContactFormModel newContact) {
 		return "redirect:welcomePage";
 	}	
 }
