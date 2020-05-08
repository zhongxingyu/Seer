 package com.pb.gaetest;
 
import java.text.DateFormat;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.context.support.ApplicationObjectSupport;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 public class SpringTestController extends ApplicationObjectSupport implements Controller {
 	public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {
 		logger.info("Hello from the AOS logger object");
 		resp.setContentType("text/plain");
 		resp.getWriter().println("Hello from a Spring Controller! Right now, it is " + new Date());
 		return null;
 	}
 }
