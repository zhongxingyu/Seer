 package th.or.innova.skima2013.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import th.or.innova.skima2013.model.Person;
 import th.or.innova.skima2013.model.RegistrationStatus;
 import th.or.innova.skima2013.model.creditCardPaymentStatus;
 import th.or.innova.skima2013.repository.DatabaseSchemaExport;
 import th.or.innova.skima2013.service.registrationService;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 
 	
 	@Autowired
 	private DatabaseSchemaExport databaseSchemaExport;
 	
 	@Autowired
 	private registrationService registrationService;
 	
 	private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("en_US"));
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/admin/createNewDatabaseSchema", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		
 		//registrationDao.Register(new Person());
 		
 		databaseSchemaExport.getSchema();
 		
		model.addAttribute("title", "SKIMA 2012 Registration Admin Page");
 		
 		return "admin/createNewDatabaseSchema";
 	}
 	
 	@RequestMapping(value="paymentSuccess") 
 	public String paymentSuccess(@RequestParam String Ref, Model model) {
 		Person person = registrationService.findPersonByRegistrationInfoRefCode(Ref);
 		
 		if(person != null) {
 			person.getRegistrationInfo().setCreditCardPaymentStatus(creditCardPaymentStatus.ACCEPT);
 			registrationService.save(person);
 		}
 		
 		model.addAttribute("registrator", person);
 		
 		//before return send email
 		registrationService.sendRegistrationMail(person);
 		
 		return "paymentSuccess";
 	}
 	
 	
 	
 	@RequestMapping(value="paymentFail") 
 	public String paymentFail(@RequestParam String Ref, Model model) {
 		Person person = registrationService.findPersonByRegistrationInfoRefCode(Ref);
 		
 		if(person != null) {
 			person.getRegistrationInfo().setCreditCardPaymentStatus(creditCardPaymentStatus.FAIL);
 			registrationService.save(person);
 		}
 		
 		model.addAttribute("registrator", person);
 		
 		//before return send email
 		registrationService.sendRegistrationMail(person);
 		
 		return "paymentFail";
 	}
 	
 	@RequestMapping(value="paymentCancel") 
 	public String paymentCancel(@RequestParam String Ref, Model model) {
 		Person person = registrationService.findPersonByRegistrationInfoRefCode(Ref);
 		
 		if(person != null) {
 			person.getRegistrationInfo().setCreditCardPaymentStatus(creditCardPaymentStatus.CANCEL);
 			registrationService.save(person);
 		}
 		
 		model.addAttribute("registrator", person);
 		
 		//before return send email
 		registrationService.sendRegistrationMail(person);
 		
 		return "paymentCancel";
 	}
 	
 	@RequestMapping(value="admin/registrationDetail")
 	public String registrationDetail(@RequestParam String refCode, Model model) {
 		Person p = registrationService.findPersonByRegistrationInfoRefCode(refCode);
 		
 		if(p != null) {
 			logger.debug(p.getFirstName());
 		}
 		
 		model.addAttribute("registrator", p);
 		return "admin/registrationDetail";
 	}
 	
 	@RequestMapping(value="skimaAdmin/allRegisters/{filter}")
 	public String skimaAdmin(
 			@PathVariable String filter,
 			@RequestParam Integer index,
 			Model model) {
 		
 		Page<Person> personList = registrationService.findAllRegisters(filter, index);
 		
 		model.addAttribute("personList", personList);
 		logger.debug("filter: "+ filter);
 		String filter_title=filter;
 		if(filter.equals("ALLJUNK")) {
 			filter_title = "ALL+JUNK";
 		}
 		model.addAttribute(filter+"active", "disabled");
 		model.addAttribute("filter", filter);
 		model.addAttribute("filter_title", filter_title);
 		
 		return "skimaAdmin/allSkimaRegisters";
 	}
 	
 	@RequestMapping(value="skimaAdmin/registrationDetail")
 	public String skimaAdminRegistrationDetail(@RequestParam String refCode, Model model) {
 		Person p = registrationService.findPersonByRegistrationInfoRefCode(refCode);
 		
 		model.addAttribute("registrator", p);
 		return "admin/registrationDetail";
 	}
 	
 	@RequestMapping(value="admin/toggleJunk")
 	public @ResponseBody String toggleJunk(
 			@RequestParam String refCode) {
 		
 		logger.debug("toggle Junk");
 		Person p = registrationService.findPersonByRegistrationInfoRefCode(refCode);
 		
 		if(p!= null) {
 			logger.debug("found registrator: " + p.getFirstName() + " " + p.getLastName());
 			
 			p.getRegistrationInfo().setJunkRegister(!p.getRegistrationInfo().getJunkRegister());
 			// now save p back
 			
 			registrationService.save(p);
 			
 		}
 		
 		return "success";
 	}
 	
 	@RequestMapping(value="admin/sendReceipt/{refCode}")
 	public @ResponseBody String sendReceipt(
 		@PathVariable String refCode) {
 		Person p = registrationService.findPersonByRegistrationInfoRefCode(refCode);
 		
 		if(p!= null) {
 			logger.debug("found registrator: " + p.getFirstName() + " " + p.getLastName());
 			registrationService.sendReceiptMail(p);
 			
 		} else {
 			return "no such Person";
 		}
 		
 		//now update p
 		p.getRegistrationInfo().setReceiptSent(true);
 		registrationService.save(p);
 		
 		return "success";
 	}
 	
 	@RequestMapping(value="admin/updateStatus") 
 	public @ResponseBody String updateStatus(
 			@RequestParam String refCode,
 			@RequestParam RegistrationStatus changeToStatus,
 			@RequestParam(required=false) String receiptDateString,
 			@RequestParam(required=false) String receiptNo,
 			@RequestParam(required=false) Double receiptExchageRate,
 			@RequestParam(required=false) Double receiptTotalThaiBaht
 			) {
 		Person p = registrationService.findPersonByRegistrationInfoRefCode(refCode);
 		
 		if(p!= null) {
 			logger.debug("found registrator: " + p.getFirstName() + " " + p.getLastName());
 			
 			p.getRegistrationInfo().setStatus(changeToStatus);
 			
 			if(changeToStatus == RegistrationStatus.Paid) {
 				logger.debug("-- " + receiptDateString);
 				Date receiptDate;
 				try {
 					receiptDate = df.parse(receiptDateString);
 					logger.debug("++ "+ receiptDate);
 				} catch (ParseException e) {
 					logger.warn("receipt date is not formatted correctly");
 					return "false";
 				}
 				p.getRegistrationInfo().setReceiptDate(receiptDate);
 				p.getRegistrationInfo().setReceiptNo(receiptNo);
 				logger.debug("exchangeRate = " + receiptExchageRate);
 				p.getRegistrationInfo().setReceiptExchangeRate(receiptExchageRate);
 				p.getRegistrationInfo().setReceiptTotalThaiBaht(receiptTotalThaiBaht);
 				p.getRegistrationInfo().setReceiptSent(false);
 			} else {
 				p.getRegistrationInfo().setReceiptNo(null);
 				p.getRegistrationInfo().setReceiptDate(null);
 				p.getRegistrationInfo().setReceiptExchangeRate(null);
 				p.getRegistrationInfo().setReceiptTotalThaiBaht(null);
 				p.getRegistrationInfo().setReceiptSent(false);
 			}
 			registrationService.save(p);
 
 			return "success";
 		}			
 		return "fail";
 	}
 	
 	@RequestMapping(value="admin/allRegisters/{filter}")
 	public String allRegisters(
 			@PathVariable String filter, 
 			@RequestParam Integer index,
 			Model model) {
 		
 	
 		
 		Page<Person> personList = registrationService.findAllRegisters(filter, index);
 		
 		
 		model.addAttribute("personList", personList);
 		logger.debug("filter: "+ filter);
 		model.addAttribute(filter+"active", "disabled");
 		String filter_title = filter;
 		if(filter.equals("ALLJUNK")) {
 			filter_title = "ALL+JUNK";
 		}
 		model.addAttribute("filter", filter);
 		model.addAttribute("filter_title", filter_title);
 		
 		return "admin/allRegisters";
 	}
 	
 	@RequestMapping(value="/adminlogin")
 	public String login(Model model) {
 		return "common/login";
 	}
 	
 	@RequestMapping(value="/adminloginfailed", method = RequestMethod.GET)
 	public String loginerror(Model model) {
  
 		model.addAttribute("error", "true");
 		return "common/login";
 	}
 	
 }
