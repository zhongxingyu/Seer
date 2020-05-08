 package com.twistlet.falcon.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.twistlet.falcon.model.entity.FalconFeedback;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.service.CustomerFeedbackService;
 import com.twistlet.falcon.model.service.UserAdminService;
 
 @Controller
 public class FalconFeedbackController {
 	
 	private final CustomerFeedbackService customerFeedbackService;
 	
 	private final UserAdminService userAdminService;
 	
 	
 	@Autowired
 	public FalconFeedbackController(
 			CustomerFeedbackService customerFeedbackService,
 			UserAdminService userAdminService) {
 		this.customerFeedbackService = customerFeedbackService;
 		this.userAdminService = userAdminService;
 	}
 
 	@RequestMapping(value = "/admin/feedback", method = RequestMethod.GET)
 	public ModelAndView adminFeedback() {
		return prepareForm();
 	}
 	
 	@RequestMapping(value = "/patron/feedback", method = RequestMethod.GET)
 	public ModelAndView patronFeedback() {
		return prepareForm();
 	}
 	
 	@RequestMapping(value = "/registration/feedback", method = RequestMethod.GET)
 	public ModelAndView feedback() {
 		ModelAndView mav = new ModelAndView("registration/customersupport");
 		FalconFeedback feedback = new FalconFeedback();
 		mav.addObject("feedback", feedback);
 		return mav;
 	}
 	
 	public ModelAndView prepareForm(){
 		ModelAndView mav = new ModelAndView("customersupport");
 		FalconFeedback feedback = new FalconFeedback();
 		mav.addObject("feedback", feedback);
 		return mav; 
 	}
 	
 	@RequestMapping(value = "/admin/send-feedback", method = RequestMethod.POST)
 	public ModelAndView adminFeedbackSubmit(@ModelAttribute("feedback") FalconFeedback falconFeedback) {
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		String loggedInUser = auth.getName();
 		FalconUser user = userAdminService.getFalconUser(loggedInUser);
 		falconFeedback.setEmailFrom(user.getEmail());
 		saveFeedback(falconFeedback);
 		return new ModelAndView("redirect:admin_landing");
 	}
 	
 	@RequestMapping(value = "/patron/send-feedback", method = RequestMethod.POST)
 	public ModelAndView patronFeedbackSubmit(@ModelAttribute("feedback") FalconFeedback falconFeedback) {
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		String loggedInUser = auth.getName();
 		FalconUser user = userAdminService.getFalconUser(loggedInUser);
 		falconFeedback.setEmailFrom(user.getEmail());
 		saveFeedback(falconFeedback);
 		return new ModelAndView("redirect:patron_landing");
 	}
 	
 	@RequestMapping(value = "/registration/send-feedback", method = RequestMethod.POST)
 	public ModelAndView feedbackSubmit(@ModelAttribute("feedback") FalconFeedback falconFeedback) {
 		saveFeedback(falconFeedback);
 		return new ModelAndView("redirect:../index");
 	}
 	
 	void saveFeedback(FalconFeedback feedback){
 		customerFeedbackService.sendFeedback(feedback);
 	}
 
 }
