 package com.vaguehope.lookfar.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class EchoServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 7474120519103213760L;
 
 	public static final String CONTEXT = "/echo";
 
 	private static final String CONTENT_TYPE_PLAIN = "text/plain;charset=UTF-8"; // TODO move to constants file.
 
 	@Override
 	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType(CONTENT_TYPE_PLAIN);
 		PrintWriter w = resp.getWriter();
 
 		w.println("Protocol=" + req.getProtocol());
 		w.println("Scheme=" + req.getScheme());
 		w.println("Method=" + req.getMethod());
 		w.println("RequestURI=" + req.getRequestURI());
 		w.println("ContextPath=" + req.getContextPath());
 		w.println("PathInfo=" + req.getPathInfo());
 		w.println("PathTranslated=" + req.getPathTranslated());
 
 		w.println("RemoteAddr=" + req.getRemoteAddr());
 		w.println("RemoteHost=" + req.getRemoteHost());
 		w.println("RemotePort=" + req.getRemotePort());
 		w.println("RemoteUser=" + req.getRemoteUser());
 		w.println("AuthType=" + req.getAuthType());
 		w.println("UserPrincipal=" + req.getUserPrincipal());
 
 		@SuppressWarnings("rawtypes")
 		Enumeration headerNames = req.getHeaderNames();
 		while(headerNames.hasMoreElements()) {
 			String headerName = headerNames.nextElement().toString();
			w.println("header:" + headerName + "=" + req.getHeaders(headerName));
 		}
 
 		@SuppressWarnings("rawtypes")
 		Enumeration paramNames = req.getParameterNames();
 		while(paramNames.hasMoreElements()) {
 			String paramName = paramNames.nextElement().toString();
			w.println("param:" + paramName + "=" + req.getParameterValues(paramName));
 		}
 
 		@SuppressWarnings("rawtypes")
 		Enumeration locales = req.getLocales();
 		while(locales.hasMoreElements()) {
 			String locale = locales.nextElement().toString();
 			w.println("Locale=" + locale);
 		}
 
 	}
 
 }
