 package edu.helsinki.sulka.controllers;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpSession;
 
 import org.joda.time.LocalDate;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import edu.helsinki.sulka.models.User;
 import edu.helsinki.sulka.services.FieldsService;
 import edu.helsinki.sulka.services.RingersService;
 import edu.helsinki.sulka.services.RowsService;
 import edu.helsinki.sulka.services.RowsService.QueryException;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	@Autowired
 	private Logger logger;
 	
 	@Autowired
 	private RingersService ringersService;
 	
 	@Autowired
 	private RowsService rowsService;
 
 	@Autowired
 	private FieldsService fieldsService;
 	
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model, HttpSession session) {
 		User user = (User) session.getAttribute("user");
 		
 		model.addAttribute("ringers", ringersService.getAllRingers());
 		
 		try {
 			model.addAttribute("rows", rowsService.getRows(
 					user.getRingerIdAsArray(), new String[]{ "ESPOO" }, null, null,
					LocalDate.now().minusMonths(3), LocalDate.now(), null));
 			model.addAttribute("rowsError", null);
 		} catch (QueryException e) {
 			e.printStackTrace();
 			model.addAttribute("rows", null);
 			model.addAttribute("rowsError", e.getMessage());
 		} catch (NumberFormatException e){
 			e.printStackTrace();
 			model.addAttribute("rows", null);
 			model.addAttribute("rowsError", e.getMessage());
 		}
 		return "home";
 	}
 	
 	/**
 	 * Show slickgrid testing ground
 	 */
 	@RequestMapping(value = "/slick", method = RequestMethod.GET)
 	public String slick(Model model){
 		return "slick";
 	}
 }
