 package net.canadensys.dataportal.vascan.controller;
 
import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
import javax.servlet.http.HttpServletResponse;

 import net.canadensys.dataportal.vascan.TaxonService;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class PagesController {
 	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
 
 	@Autowired
 	private TaxonService taxonService;
 	
 	@RequestMapping(value={"/about"}, method={RequestMethod.GET})
 	public ModelAndView handleAboutPage(){
 		
 		Map<String,String> model = new HashMap<String,String>();
 		model.put("currentDate", SDF.format(Calendar.getInstance().getTime()));
 		return new ModelAndView("about", model);
 	}
	
	//TODO remove me after development phase
    @RequestMapping(value="/generateNestedSets", method=RequestMethod.GET)
    public void generateNestedSets(HttpServletResponse response) {
    	try {
	    	if(taxonService.generateNestedSets()){
	    		response.getWriter().print("Nested sets generated");
	    	}
	    	else{
	    		response.getWriter().print("Can NOT generate Nested sets on the current database");
	    	}
	    	response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

 }
