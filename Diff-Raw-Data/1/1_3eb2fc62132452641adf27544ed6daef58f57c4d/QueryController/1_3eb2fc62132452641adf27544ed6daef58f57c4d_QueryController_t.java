 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import ar.edu.itba.paw.grupo1.dto.PropertyQuery;
 import ar.edu.itba.paw.grupo1.service.PropertyService;
 
 @Controller
 @RequestMapping(value="query")
 public class QueryController extends BaseController {
 
 	PropertyService propertyService;
 	
 	@Autowired
 	public QueryController(PropertyService propertyService) {
 		this.propertyService = propertyService;
 	}
 	
 	@RequestMapping(method = RequestMethod.GET)
 	protected ModelAndView query(@Valid PropertyQuery propertyQuery, Errors errors)
 			throws ServletException, IOException {
 
 		ModelAndView mav = new ModelAndView();
 
 		if (errors.hasErrors()) {
 			propertyQuery = new PropertyQuery();
 		}
 
 		mav.addObject("queryResults", propertyService.query(propertyQuery));
 
 		return render("query.jsp", "Query", mav);
 	}
 
 }
