 package com.ocbcmcd.monitoring.web;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 
 import com.ocbcmcd.monitoring.command.RegistrationCommand;
 import com.ocbcmcd.monitoring.service.IRegistrationService;
 import com.ocbcmcd.monitoring.validator.RegistrationValidator;
 
 @Controller
 @RequestMapping("/register")
 public class RegisterController {
 	private static final int REGISTER_MESSAGE_ID = 1;
 	
 	@Autowired
 	private RegistrationValidator validator;
 	
 	@Autowired
 	private IRegistrationService registrationService;
 	
 	@RequestMapping(method = GET)
 	public ModelMap setupForm(@RequestParam(required = false) RegistrationCommand command) {
 		command = new RegistrationCommand();
 		return new ModelMap("command", command);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST)
 	public String submitForm(@ModelAttribute("command") RegistrationCommand command, BindingResult result, SessionStatus status) {
 		validator.validate(command, result);
 		
 		if (result.hasErrors())
 			return "register";
 		else {
 			registrationService.register(command);
			return "redirect:user/?message=" + REGISTER_MESSAGE_ID;
 		}
 	}
 }
