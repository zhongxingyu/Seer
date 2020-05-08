 package com.robonobo.sonar;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class IpDetectController {
	Log log = LogFactory.getLog(getClass());
	
 	@RequestMapping("/ipdetect")
 	public void ipDetect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.setStatus(HttpServletResponse.SC_OK);
 		resp.setContentType("text/plain");
		String clientIp = getClientIpAddress(req);
		log.info("Returning IP address to "+clientIp);
		resp.getWriter().print(clientIp);
 	}
	
	/** If we're behind a front end cache, the client ip of the request won't be accurate - figure out the real ip */
	protected String getClientIpAddress(HttpServletRequest req) {
		String forwardFor = req.getHeader("X-Forwarded-For");
		if(forwardFor == null)
			return req.getRemoteAddr();
		return forwardFor;
	}

 }
