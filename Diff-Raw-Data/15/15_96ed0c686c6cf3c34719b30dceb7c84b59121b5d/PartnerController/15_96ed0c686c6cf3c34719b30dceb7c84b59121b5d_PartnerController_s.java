 package com.compannex.controller;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.compannex.biz.CountryMethods;
 import com.compannex.biz.IndustryMethods;
 import com.compannex.biz.PartnerMethods;
 import com.compannex.constants.CompANNEXConstants;
 import com.compannex.form.Registration;
 import com.compannex.validator.RegistrationValidation;
 
 @Controller
 public class PartnerController {
 
 	private static Logger logger = Logger.getLogger(PartnerController.class);
 
	@Autowired
 	private RegistrationValidation registrationValidation;
 
	public void setRegistrationValidation(
 			RegistrationValidation registrationValidation) {
 		this.registrationValidation = registrationValidation;
	}
 
	@RequestMapping("/registernew.do")
 	public ModelAndView registerNew(HttpServletRequest request) {
 		ModelAndView result = new ModelAndView("register", "activeTab",
 				"clients");
 
 		WebApplicationContext context = WebApplicationContextUtils
 				.getRequiredWebApplicationContext(request.getSession()
 						.getServletContext());
 		IndustryMethods indMeth = (IndustryMethods) context
 				.getBean("industryMethods");
 		CountryMethods countrMeth = (CountryMethods) context
 				.getBean("countryMethods");
 
 		Registration registration = new Registration();
 		result.addObject("registration", registration);
 		result.addObject("industries",
 				indMeth.getAllIndustries(CompANNEXConstants.DEFAULT_LANGUAGE));
 		result.addObject("countries",
 				countrMeth.getAllCountries(CompANNEXConstants.DEFAULT_LANGUAGE));
 
 		return result;
 	}
 
 	@RequestMapping("/register.do")
 	public ModelAndView register(
 			HttpServletRequest request,
 			@RequestParam(value = "name", required = false) String name,
 			@RequestParam(value = "email", required = false) String email,
 			@RequestParam(value = "password", required = false) String password,
 			@RequestParam(value = "repassword", required = false) String repassword,
 			@RequestParam(value = "industry", required = false) String industry,
 			@RequestParam(value = "category", required = false) String category,
 			@RequestParam(value = "websiteurl", required = false) String websiteurl,
 			@RequestParam(value = "telephone", required = false) String telephone,
 			@RequestParam(value = "fax", required = false) String fax,
 			@RequestParam(value = "contactperson", required = false) String contactperson,
 			@RequestParam(value = "address", required = false) String address,
 			@RequestParam(value = "country", required = false) String country,
 			@RequestParam(value = "slogan", required = false) String slogan,
 			@RequestParam(value = "employeecount", required = false) String employeecount,
 			@RequestParam(value = "description", required = false) String description,
 			@Valid Registration registration, BindingResult result) {
 
 		ModelAndView success = new ModelAndView("clients", "activeTab",
 				"clients");
 		ModelAndView error = new ModelAndView("register", "activeTab",
 				"clients");
 
 		registrationValidation.validate(registration, result);
 		if (result.hasErrors()) {
 			return error;
 		}
 
 		WebApplicationContext context = WebApplicationContextUtils
 				.getRequiredWebApplicationContext(request.getSession()
 						.getServletContext());
 		PartnerMethods partnerMeth = (PartnerMethods) context
 				.getBean("partnerMethods");
 
 		partnerMeth.addNewPartner(registration.getName(),
 				registration.getEmail(), registration.getPassword(),
 				registration.getCategory(), registration.getWebsiteurl(),
 				registration.getTelephone(), registration.getFax(),
 				registration.getContactperson(), registration.getAddress(),
 				registration.getCountry(), registration.getSlogan(),
 				registration.getEmployeecount(), registration.getDescription());
 
 		return success;
 	}
 }
