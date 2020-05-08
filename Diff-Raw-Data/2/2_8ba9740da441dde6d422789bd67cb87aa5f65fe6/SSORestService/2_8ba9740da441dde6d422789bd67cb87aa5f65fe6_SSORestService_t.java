 package com.railinc.jook.drawers.myapps;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 
 
 
 
 public class SSORestService extends HttpServlet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8712181340575026147L;
 	private String dataServicesUrl;
 	private SAXParserFactory saxParserFactory;
 	
 	private Pattern pattern;
 	public String getDataServicesUrl() {
 		return dataServicesUrl;
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		String user = req.getRemoteUser();
 		user ="sdtxs01";
 		
 		String pathInfo = req.getRequestURI();
 		
 		if (user == null) {
 			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 			return;
 		}
 		
 		Matcher matcher = pattern.matcher(pathInfo); 
 		if (!matcher.matches()) {
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND, pathInfo);
 			return;
 		}
 		// path , servlet_path/APPLICATION.(json|xml)
 		String format = matcher.group(1);
 		
 		// return the user's preferences
 		List<Resource> appsForUser = getAppsForUser(user);
 		
 		req.setAttribute("apps", appsForUser);
 		req.getRequestDispatcher(String.format("/myapps.%s.jsp", format)).forward(req, resp);
 				
 	}
 
 	public void setDataServicesUrl(String dataServicesUrl) {
 		this.dataServicesUrl = dataServicesUrl;
 	}
 	
 	
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			Context ctx = new InitialContext();
 			String url = (String) ctx.lookup("java:comp/env/dataServicesUrl");
 			this.setDataServicesUrl(url);
 		} catch (NamingException e) {
 			throw new ServletException(e);
 		}
 		pattern = Pattern.compile("^.+\\.(html|json|xml)$");
 	}
 
 	public List<Resource> getAppsForUser(String userId) {
 		String theUrl = MessageFormat.format(getDataServicesUrl() + "/SSOServices/sso/v1/resources/user/{0}", userId);
 		HttpURLConnection conn = null;
 		try {
 			URL url = new URL(theUrl);
 			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
 			int responseCode = conn.getResponseCode();
 			InputStream xml = conn.getInputStream();
 			
 			
 			try {
 				return parseResources(xml);
 			} finally {
 				try {
 				if (xml != null) { xml.close(); }
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (null != conn) {
 				conn.disconnect();
 			}
 		}
 		return null;
 	}
 	
 	public List<Resource> parseResources(InputStream xml) {
 		List<Resource> resources = new ArrayList<Resource>();
 		if (null == xml) {
 			return resources;
 		}
 		SSOResourcesSAXHandler handler = new SSOResourcesSAXHandler(resources);
 		
 		
 		try {
 			SAXParser parser = getParser();
 			parser.parse(xml, handler);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}		
 		Collections.sort(resources);
 		return resources;
 	}
 	
 	private SAXParser getParser() throws ParserConfigurationException, SAXException {
 		return getParserFactory().newSAXParser();
 	}
 	
 	private SAXParserFactory getParserFactory() {
 		if (null == saxParserFactory) {
 			saxParserFactory = SAXParserFactory.newInstance();
 			saxParserFactory.setNamespaceAware(true);
 		}
 		return saxParserFactory;
 		
 	}
 
 
 }
