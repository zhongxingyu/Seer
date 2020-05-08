 package com.leopin.parkfifty.server.controller;
 
 import static com.leopin.parkfifty.shared.exception.ErrorKeys.ERROR_APP_ADMIN_COMPANY_BINDING_ERRORS;
 import static com.leopin.parkfifty.shared.exception.ErrorKeys.ERROR_UNEXPECTED;
 
 import java.util.Locale;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.LocaleResolver;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import com.google.appengine.repackaged.com.google.common.base.CharMatcher;
 import com.leopin.parkfifty.server.service.AdminService;
 import com.leopin.parkfifty.shared.domain.Company;
 import com.leopin.parkfifty.shared.domain.CompanyUser;
 import com.leopin.parkfifty.shared.domain.ExceptionInfo;
 import com.leopin.parkfifty.shared.domain.Location;
 import com.leopin.parkfifty.shared.exception.AppException;
 import com.leopin.parkfifty.shared.exception.SysException;
 
 @Controller
 @RequestMapping("/admin")
 public class AdminController {
 
 	// TODO Replace LOGGER with Aspects
 	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
 	private AdminService adminService;	
 	private MessageSource messages;
 	private Locale locale;
 
 	
 	public AdminController() {
 		super();
 	}
 	
 
 	@Autowired
 	Validator validator;
 	
 	@Autowired
 	public AdminController(AdminService service, 
 			MessageSource messages) {
 		this.adminService = service;
 		this.messages = messages;
 	}
 	
 	@RequestMapping(value="/locale")
 	public void resolveLocale(HttpServletRequest request) {
 		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request); 
 		if (localeResolver != null) {
 			this.locale = localeResolver.resolveLocale(request);
 		} else {
 			this.locale = Locale.US;
 		}
 	}
 	
 	@RequestMapping(value="/company", method=RequestMethod.GET, headers="Accept=application/json")
 	public @ResponseBody String getName() {
 		return "Glenn Pinto";
 		
 	}
 	
 	/**
 	 * GET - Retrieve Company by ID or Name 
 	 * @param id Identifier that can be an Record ID or Name
 	 * @return
 	 */
 	@RequestMapping(value="/company/{id}", method=RequestMethod.GET, headers="Accept=application/json")
 	public @ResponseBody Company getCompany(@PathVariable String id) {
 		LOGGER.debug("Retrieving [" + id + "]");
 		if (CharMatcher.DIGIT.matchesAllOf(id)) 
 			return adminService.getCompany(Long.parseLong(id));
 		else 
 			return adminService.getCompany(id);
 //		(CharMatcher.is('+').replaceFrom(id, ' '))
 		
 	}
 	
 	/**
 	 * POST - Insert a new Company
 	 * @param company
 	 */
 	@RequestMapping(value="/company", 
 			method=RequestMethod.POST,
 			headers = {"Accept=application/json"})
 	@ResponseStatus(HttpStatus.OK)
 	public @ResponseBody Company addCompany(@RequestBody Company company) {
 		LOGGER.debug("Adding Company [" + company.toString() + "]");
 		Set<ConstraintViolation<Company>> constraints = validator.validate(company);
 		if (!constraints.isEmpty()) {
 			StringBuilder sb = new StringBuilder();
 			boolean comma = false;
 			for (ConstraintViolation<Company> constraintViolation : constraints) {
 				sb.append((comma? ", " : "") + constraintViolation.getMessage());
 				comma = true;
 			}
 			LOGGER.debug(sb.toString());
 			throw new AppException(ERROR_APP_ADMIN_COMPANY_BINDING_ERRORS, new Object[]{sb.toString()});
 		}
 		
//		return adminService.addCompany(company);
		return company;
 	}
 
 	/**
 	 * POST - Insert a new Location
 	 * @param company
 	 */
 	@RequestMapping(value="/company/location", method=RequestMethod.POST)
 	@ResponseStatus(HttpStatus.OK)
 	public @ResponseBody Location addLocation(@RequestBody Location location) {
 		LOGGER.debug("Adding Location [" + location.toString() + "]");
 		Set<ConstraintViolation<Location>> constraints = validator.validate(location);
 		if (!constraints.isEmpty()) {
 			StringBuilder sb = new StringBuilder();
 			boolean comma = false;
 			for (ConstraintViolation<Location> constraintViolation : constraints) {
 				sb.append((comma? ", " : "") + constraintViolation.getMessage());
 				comma = true;
 			}
 			LOGGER.debug(sb.toString());
 			throw new AppException(ERROR_APP_ADMIN_COMPANY_BINDING_ERRORS, new Object[]{sb.toString()});
 		}
 		
 		return adminService.addLocation(location);
 	}
 	
 
 	/**
 	 * POST - Insert a new Location
 	 * @param company
 	 */
 	@RequestMapping(value="/company/companyUser", 
 			method=RequestMethod.POST,
 			headers = {"Accept=application/json"})
 	@ResponseStatus(HttpStatus.OK)
 	public @ResponseBody CompanyUser addCompanyUser(@RequestBody CompanyUser companyUser) {
 		LOGGER.debug("Adding CompanyUser [" + companyUser.toString() + "]");
 		Set<ConstraintViolation<CompanyUser>> constraints = validator.validate(companyUser);
 		if (!constraints.isEmpty()) {
 			StringBuilder sb = new StringBuilder();
 			boolean comma = false;
 			for (ConstraintViolation<CompanyUser> constraintViolation : constraints) {
 				sb.append((comma? ", " : "") + constraintViolation.getMessage());
 				comma = true;
 			}
 			LOGGER.debug(sb.toString());
 			throw new AppException(ERROR_APP_ADMIN_COMPANY_BINDING_ERRORS, new Object[]{sb.toString()});
 		}
 		
//		return adminService.addCompanyUser(companyUser);
		return companyUser;
 	}
 
 	@RequestMapping(value="/company/{id}", method=RequestMethod.DELETE)
 	@ResponseStatus(HttpStatus.OK)
 	public void deleteCompany(@PathVariable String id) {
 		LOGGER.debug("Deleting [" + id + "]");
 		if (CharMatcher.DIGIT.matchesAllOf(id)) 
 			adminService.deleteCompany(Long.parseLong(id));
 		else 
 			adminService.deleteCompany(id);
 	}
 
 
 	/**
 	 * Exception handling for any application notification that was anticipated by the application
 	 * @param ex
 	 * @return
 	 */
 	@ExceptionHandler(AppException.class)
 	@ResponseStatus(HttpStatus.METHOD_FAILURE)
 	public @ResponseBody ExceptionInfo handleThrowable(AppException ex) {
 		String message = getMessage(ex.getErrorKey(), ex.getPlaceholderValues());
 		LOGGER.error(ex.getErrorKey() + ": " + message);
 		return new ExceptionInfo(ex.getErrorKey(), message);
 	}
 	
 	/**
 	 * Exception handling for any application exception that was NOT anticipated by the application
 	 * @param ex
 	 * @return
 	 */
 	@ExceptionHandler(SysException.class)
 	@ResponseStatus(HttpStatus.METHOD_FAILURE)
 	public @ResponseBody ExceptionInfo handleThrowable(SysException ex) {
 		LOGGER.error(ex.getErrorKey(), ex);
 		return new ExceptionInfo(ex.getErrorKey(), getMessage(ex.getErrorKey(), ex.getPlaceholderValues()));
 	}
 	
 	/**
 	 * Exception handling for any System runtime exception not anticipated by the program
 	 * @param ex
 	 * @return
 	 */
 	@ExceptionHandler(Exception.class)
 	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
 	public @ResponseBody ExceptionInfo handleThrowable(Exception ex) {
 		LOGGER.error("Exception", ex);
 		return new ExceptionInfo(ERROR_UNEXPECTED, getMessage(ERROR_UNEXPECTED, new Object[]{}));
 	}
 	
 	
 	private String getMessage(String key, Object[] placeholderValues) {
 		return this.messages.getMessage(key, placeholderValues, locale);
 	}
 	
 	public void setAdminService(AdminService adminService) {
 		this.adminService = adminService;
 	}
 	
 	public void setMessages(MessageSource messages) {
 		this.messages = messages;
 	}
 	
 	public void setValidator(Validator validator) {
 		this.validator = validator;
 	}
 	
 }
