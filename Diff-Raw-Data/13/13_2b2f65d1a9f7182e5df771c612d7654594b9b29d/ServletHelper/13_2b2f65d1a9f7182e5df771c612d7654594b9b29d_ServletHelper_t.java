 package com.vaguehope.senkyou.servlets;
 
 import static com.vaguehope.senkyou.util.Numbers.isNumeric;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public final class ServletHelper {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private static final String ROOTPATH = "/";
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private ServletHelper () {/* Static helper */}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	public static String validateStringParam (HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
 		String p = req.getParameter(param);
 		if (p != null && !p.isEmpty()) {
 			return p;
 		}
 		error(resp, HttpServletResponse.SC_BAD_REQUEST, "Param '" + param + "' not valid.");
 		return null;
 	}
 	
 	public static long validatePositiveLongParam (HttpServletRequest req, HttpServletResponse resp, String param) throws IOException {
 		String p = req.getParameter(param);
 		if (p != null && !p.isEmpty() && isNumeric(p)) {
 			long n = Long.parseLong(p);
 			if (n > 0) {
 				return n;
 			}
 			error(resp, HttpServletResponse.SC_BAD_REQUEST, "Param '" + param + "' not positive.");
 			return 0;
 		}
 		error(resp, HttpServletResponse.SC_BAD_REQUEST, "Param '" + param + "' not valid.");
 		return 0;
 	}
 	
 	public static void error (HttpServletResponse resp, int status, String msg) throws IOException {
 		resp.reset();
 		resp.setStatus(status);
 		resp.setContentType("text/plain");
 		resp.getWriter().println("HTTP Error " + status + ": " + msg);
 	}
 	
 	public static void resetSession (HttpServletRequest req) {
 		HttpSession session = req.getSession(false);
 		if (session != null) session.invalidate();
 		req.getSession(true);
 	}
 	
 	public static String requestSubPath (HttpServletRequest req, String baseContext) {
 		String requestURI = req.getRequestURI();
 		String reqPath = requestURI.startsWith(baseContext) ? requestURI.substring(baseContext.length()) : requestURI;
		return reqPath.startsWith(ROOTPATH) ? reqPath.substring(ROOTPATH.length()) : reqPath;
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
