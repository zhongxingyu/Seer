 package com.robonobo.sonar;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class IpDetectController {
 	@RequestMapping("/ipdetect")
 	public void ipDetect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String ipAddr = req.getRemoteAddr();
 		resp.setStatus(HttpServletResponse.SC_OK);
 		resp.setContentType("text/plain");
		resp.getWriter().print(ipAddr);
 	}
 }
