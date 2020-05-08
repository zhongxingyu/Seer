 package org.alt60m.servlet;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.text.DateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.MDC;
 import org.apache.log4j.NDC;
 
 public class LoggingFilter implements Filter {
 
 	private final int MAX_HISTORY_SIZE = 15;
 	private static Log log = LogFactory.getLog(LoggingFilter.class);
 	
 	public void destroy() {
 		log.debug("destroying Logging Filter");
 	}
 
 	@SuppressWarnings("unchecked")
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain filterChain) throws IOException, ServletException {
 		try {
 			HttpServletRequest req = (HttpServletRequest) request;
 			String userIPAddress = req.getRemoteAddr();
 			MDC.put("userIPAddress", userIPAddress);
 			String machineName = InetAddress.getLocalHost().getHostName();
 			MDC.put("machineName", machineName);
 
 			String user = (String) req.getSession().getAttribute("userName");
 			if (user == null) {
 				user = (String) req.getSession().getAttribute("userLoggedIn");
 			}
 			if (user == null) {
 				user = "(anonymous)";
 			}
 
 			MDC.put("username", user);
 			NDC.push(user);
 			String actionName = req.getParameter("action");
 			if (actionName == null) {
 				actionName = "(not specified)";
 			}
 			MDC.put("action", actionName);
 			NDC.push(actionName);
 
 			Map<String, String> requestMap = getHashedRequest(req);
 			StringBuffer url = req.getRequestURL();
 
 			String lineSep = System.getProperty("line.separator");
 			lineSep = (lineSep == null ? "\n" : lineSep);
 			MDC.put("request", url.append(lineSep)
 					.append(requestMap.toString()).toString());
 
			List<String> history = (LinkedList<String>) req.getSession()
 					.getAttribute("history");
 			if (history == null) {
 				history = Collections.synchronizedList(new LinkedList<String>());
 				req.getSession().setAttribute("history", history);
 			}
 			String currentTime = DateFormat.getTimeInstance().format(new Date());
 			String path = req.getRequestURI();
 			path = lineSep + path + " " + requestMap.toString() + " [" + currentTime + "]" + lineSep;
 			history.add(path);
 
 			if (history.size() > MAX_HISTORY_SIZE) {
 				history.remove(0);
 			}
 
 			Map<String, Object> sessionCopy = getHashedSession(req);
 
 			MDC.put("session", sessionCopy.toString());
 			log.debug("Forwarding request for " + req.getRequestURI());
 			filterChain.doFilter(request, response);
 		} finally {
 
 			NDC.pop();
 			NDC.pop();
 			MDC.remove("username");
 			MDC.remove("action");
 			MDC.remove("session");
 			MDC.remove("request");
 			MDC.remove("userIPAddress");
 			MDC.remove("machineName");
 		}
 	}
 
 	private Map<String, Object> getHashedSession(HttpServletRequest req) {
 		Map<String, Object> sessionCopy = new HashMap<String, Object>();
 		for (Enumeration<String> attributeNames = (Enumeration<String>) req
 				.getSession().getAttributeNames(); attributeNames
 				.hasMoreElements();) {
 			String attributeName = attributeNames.nextElement();
 			sessionCopy.put(attributeName, req.getSession().getAttribute(
 					attributeName));
 		}
 		return sessionCopy;
 	}
 
 	public void init(FilterConfig config) throws ServletException {
 		log.debug("Starting logging filter");
 	}
 
 	public Map<String, String> getHashedRequest(HttpServletRequest request) {
 		Map<String, String> h = new HashMap<String, String>();
 		for (Enumeration enumer = request.getParameterNames(); enumer
 				.hasMoreElements();) {
 			String key = (String) enumer.nextElement();
 			h.put(key, request.getParameter(key));
 		}
 		return h;
 	}
 }
