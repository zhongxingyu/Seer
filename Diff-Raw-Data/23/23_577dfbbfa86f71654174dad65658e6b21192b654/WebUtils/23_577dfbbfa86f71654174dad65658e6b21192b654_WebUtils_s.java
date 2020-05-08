 package net.iteach.web.support;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang3.StringUtils;
 
 public class WebUtils {
 
 	public static final String ITEACH_URL = "iteach.url";
 
 	public static String getBaseURL(HttpServletRequest request) {
 		String systemDefinedURL = System.getProperty(ITEACH_URL);
 		if (StringUtils.isNotBlank(systemDefinedURL)) {
 			return systemDefinedURL;
 		} else {
 			return computeBaseURL(request);
 		}
 	}
 
 	public static String computeBaseURL(HttpServletRequest request) {
 		StringBuilder s = new StringBuilder();
 		// Scheme
 		s.append(request.getScheme()).append("://");
 		// Host
 		s.append(request.getServerName());
 		// Port
 		int port = request.getServerPort();
 		if (port != 80) {
 			s.append(":").append(port);
 		}
 		// Context path
 		String path = request.getContextPath();
 		if (StringUtils.isNotBlank(path)) {
 			s.append(path);
 		}
 		// OK
 		return s.toString();
 	}
 
 }
