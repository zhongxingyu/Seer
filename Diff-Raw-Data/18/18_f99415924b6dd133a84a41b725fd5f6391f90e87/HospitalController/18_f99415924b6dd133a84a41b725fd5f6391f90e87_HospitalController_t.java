 package at.ac.tuwien.dse.fairsurgeries.web;
 
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

 import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
import at.ac.tuwien.dse.fairsurgeries.domain.Hospital;

 @RequestMapping("/hospitals")
 @Controller
 @RooWebScaffold(path = "hospitals", formBackingObject = Hospital.class)
 public class HospitalController {
	
	@RequestMapping
	public void get(HttpServletResponse response) throws Exception {
		PrintWriter out = response.getWriter();
		
		out.println("Gerhard as a Service");
	}
 }

