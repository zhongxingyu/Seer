 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2012 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.httpServer;
 
 import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.http.MimeTypes;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.util.ByteArrayISO8859Writer;
 import org.eclipse.jetty.util.IO;
 import org.meerkat.services.WebApp;
 import org.meerkat.util.HtmlOperations;
 import org.meerkat.util.PropertiesLoader;
 import org.meerkat.webapp.WebAppCollection;
 import org.meerkat.webapp.WebAppEvent;
 import org.meerkat.webapp.WebAppEventListIterator;
 
 public class CustomResourceHandler extends ResourceHandler {
 	private static Logger log = Logger.getLogger(CustomResourceHandler.class);
 	final long _faviconModified = (System.currentTimeMillis() / 1000) * 1000;
 	byte[] _favicon;
 	boolean _serveIcon = true;
 	private String eventRequestID = "/event-id-";
 	private String eventListRequest = "/event-list-";
 	private String eventListGoogleVisualizationRequest = "/event-gv-list-";
 	int eventListRequestLength = eventListRequest.length();
 	private WebAppCollection wac;
 	private static String propertiesFile = "meerkat.properties";
 	private HttpServer httpServer;
 
 	private String notFound = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n"
 			+ "<html>\n"
 			+ "<head>\n"
 			+ " <meta content=\"text/html; charset=ISO-8859-1\"\n"
 			+ " http-equiv=\"content-type\">\n"
 			+ "  <style type=\"text/css\">\n"
 			+ "   body {text-align: center;}"
 			+ "   div.content {margin-left: auto; margin-right: auto; text-align: center; \n"
 			+ "        background-color: #ffa200; width: 410px; height: 300} </style>\n"
 			+ "  <title>404 Page Not Found</title>\n"
 			+ "</head>\n"
 			+ "<body>\n"
 			+ "<h1>Whoops 404.&nbsp;Page not found.</h1>\n"
 			+ "<div class=\"content\">\n"
 			+ "<br>\n"
 			+ "<div><big>"
 			+ "<span style=\"color: rgb(0, 0, 102);\">"
 			+ "And you've found a dead Meerkat!</span></big><br>\n"
 			+ "</div>\n"
 			+ "<br>\n"
 			+ "<img style=\"width: 400px; height: 258px;\"\n"
 			+ "alt=\"Meerkat in the sun\" src=\"resources/404_meerkat.png\"\n"
 			+ " hspace=\"5\"><br>\n"
 			+ "<br>\n"
 			+ "<div><big><span style=\"color: rgb(0, 0, 102);\">\n"
 			+ "(Not really. Just bathing in the sun...)</span></big><br>\n"
 			+ "</div>\n"
 			+ "<br>\n"
 			+ "</div>\n"
 			+ "</body>\n"
 			+ "</html>\n";
 
 
 	public CustomResourceHandler(HttpServer httpServer) {
 		this.httpServer = httpServer;
 
 		// Set the favicon
 		try {
 			URL fav = this.getClass().getClassLoader()
 					.getResource("resources/favicon.ico");
 			if (fav != null) {
 				_favicon = IO.readBytes(fav.openStream());
 			}
 		} catch (Exception e) {
 			log.warn("Unable to set favicon", e);
 		}
 	}
 
 	public final void setWebAppCollection(WebAppCollection wac){
 		this.wac = wac;
 	}
 
 	@Override
 	public final void handle(String target, Request baseRequest,
 			HttpServletRequest request, HttpServletResponse response)
 					throws IOException, ServletException {
		
		log.info("|-- CustomHandler Received request!!");
		
 		if (response.isCommitted() || baseRequest.isHandled()) {
 			return;
 		} else {
 			// little cheat for common request - favicon hack
 			if (request.getRequestURI().equals("/favicon.ico")) {
 				response.setStatus(HttpServletResponse.SC_OK);
 				response.setContentType("image/x-icon");
 				response.setContentLength(_favicon.length);
 				response.getOutputStream().write(_favicon);
 				baseRequest.setHandled(true);
 				return;
 
 			}else if(request.getRequestURI().contains("index.html")){ // Handle index.html
 				String responseIndex = httpServer.getIndexPageContents();
 				ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
 				response.setContentType(MimeTypes.TEXT_HTML);
 				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
 				writer.write(responseIndex);
 				writer.flush();
 				response.setContentLength(writer.size());
 				OutputStream out = response.getOutputStream();
 				writer.writeTo(out);
 				out.close();
 				writer.close();
 				baseRequest.setHandled(true);
 				return;
 
 			}else if(request.getRequestURI().contains(eventRequestID)){ // This is a request for event
 				// Get request id
 				String requestRef = request.getRequestURI();
 				requestRef = requestRef.substring(eventRequestID.length(), requestRef.length());
 				int id = Integer.valueOf(requestRef);
 				WebAppEvent ev = WebAppEvent.getEventByID(id);
 
 				ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
 				response.setContentType(MimeTypes.TEXT_HTML);
 				if(ev != null){
 					response.setStatus(HttpServletResponse.SC_OK);
 
 					// Escape the response
 					String escapedResponse = escapeHtml3(ev.getCurrentResponse());
 					//String escapedResponse = StringUtils.replaceEach(ev.getCurrentResponse(), new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"});
 
 					// Prettify response
 					String prettified = HtmlOperations.addPrettifier(escapedResponse);
 
 					writer.write(prettified);
 					writer.flush();
 					response.setContentLength(writer.size());
 					OutputStream out = response.getOutputStream();
 					writer.writeTo(out);
 					out.close();
 					writer.close();
 					baseRequest.setHandled(true);
 					return;
 				}else{
 					writer.close();
 					//log.info("-- prepare to load 404 handler..");
 					processNotFound404(response, baseRequest);
 				}
 
 			}
 
 			// Deal with request for datatables events
 			else if(request.getRequestURI().contains(eventListRequest)){
 
 				// Get application
 				String requestRef = request.getRequestURI();
 				requestRef = requestRef.substring(eventListRequest.length(), requestRef.length());
 				String appName = URLDecoder.decode(requestRef, "UTF-8");
 
 				// Handle paging
 				String displayStart, displayLength;
 				displayStart = request.getParameter("iDisplayStart");
 				displayLength = request.getParameter("iDisplayLength");
 				if(displayStart == null || displayLength == null){
 					displayStart = "0";
 					displayLength = "10";
 				}
 
 				// Ordering
 				String orderBy = request.getParameter("iSortCol_0");
 				String sortOrder = request.getParameter("sSortDir_0");
 				if(orderBy == null || sortOrder == null){
 					orderBy = "0";
 					sortOrder = "ASC";
 				}
 
 				String sEcho = request.getParameter("sEcho");
 				if(sEcho == null){
 					sEcho = "1";
 				}
 
 				// Get number of events of application
 				WebApp webapp = wac.getWebAppByName(appName);
 				if(webapp == null){
 					log.info("Application "+appName+" not present!");
 					webapp = new WebApp(); // prevent null in getCustomEventsList
 					processNotFound404(response, baseRequest);
 				}
 
 				int numberEvents = 0;
 				try{
 					numberEvents = webapp.getNumberOfEvents();
 				}catch(NullPointerException e){
 					log.error("Failed to get number of events from app. - "+e.getMessage());
 				}
 
 				int numberOfEventsToShow = Integer.valueOf(displayStart) + Integer.valueOf(displayLength);
 				ArrayList<WebAppEvent> requestedEventList = webapp.getCustomEventsList(displayStart, String.valueOf(numberOfEventsToShow), orderBy, sortOrder.toUpperCase());
 
 				String returnResp = "{ \n"+
 						"\"sEcho\": "+sEcho+", \n"+
 						"\"iTotalRecords\": \""+numberEvents+"\", \n"+
 						"\"iTotalDisplayRecords\": \""+numberEvents+"\", \n"+
 						"\"aaData\": [ \n";
 
 				Iterator<WebAppEvent> it = requestedEventList.iterator();
 				WebAppEvent ev;
 				String jSONresponse = returnResp;
 				while(it.hasNext()){
 					ev = it.next();
 
 					jSONresponse += "[\n"+
 							"\""+ev.getID()+"\", \n"+
 							"\""+ev.getDate()+"\", \n";
 
 					if(ev.getStatus()){
 						jSONresponse +=	"\"Online\", \n";
 					}else{
 						jSONresponse +=	"\"Offline\", \n";
 					}
 
 					jSONresponse += "\""+ev.getAvailability()+"\", \n"+
 							"\""+ev.getPageLoadTime()+"\", \n"+
 							"\""+ev.getLatency()+"\", \n";
 
 
 					jSONresponse +=	"\""+ev.getHttpStatusCode()+"\", \n";
 
 
 					jSONresponse += "\""+ev.getDescription()+"\", \n"+
 							"\"<a href=\\\"event-id-"+ev.getID()+"\\\" onclick=\\\"return popitup('event-id-"+
 							+ev.getID()+ "')\\\"><img src=\\\"resources/tango_edit-find.png\\\" border=\\\"0\\\" alt=\\\"\\\" /></a>\" \n"+
 							"],"; // only the one doesnt have ","
 				}
 
 				// remove the last ","
 				jSONresponse = jSONresponse.substring(0, jSONresponse.length()-1);
 
 				jSONresponse += "\n] \n"+
 						"}";
 
 				ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
 				response.setContentType(MimeTypes.TEXT_JSON_UTF_8);
 				response.setStatus(HttpServletResponse.SC_OK);
 				// Disable cache
 				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
 				writer.write(jSONresponse);
 				writer.flush();
 				response.setContentLength(writer.size());
 				OutputStream out = response.getOutputStream();
 				writer.writeTo(out);
 				out.close();
 				writer.close();
 				baseRequest.setHandled(true);
 				return;
 			}
 
 			// Deal with request for Google Visualization events
 			else if(request.getRequestURI().contains(eventListGoogleVisualizationRequest)){
 				// Get properties
 				PropertiesLoader pl = new PropertiesLoader(propertiesFile);
 				Properties prop = pl.getPropetiesFromFile();
 
 				// Get the max number of records
 				int maxNumberRecordsToShow = Integer.valueOf(prop.getProperty("meerkat.app.timeline.maxrecords"));
 
 
 				// Get application
 				String requestRef = request.getRequestURI();
 				requestRef = requestRef.substring(eventListGoogleVisualizationRequest.length(), requestRef.length());
 				String appName = URLDecoder.decode(requestRef, "UTF-8");
 				WebApp webapp = wac.getWebAppByName(appName);
 
 				WebAppEventListIterator wAppEIt = new WebAppEventListIterator(webapp);
 
 				String jsonResponse = wAppEIt.getJsonFormatLastXAppEvents(maxNumberRecordsToShow);
 
 				ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
 				response.setContentType(MimeTypes.TEXT_JSON_UTF_8);
 				response.setStatus(HttpServletResponse.SC_OK);
 				// Disable cache
 				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
 				writer.write(jsonResponse);
 				writer.flush();
 				response.setContentLength(writer.size());
 				OutputStream out = response.getOutputStream();
 				writer.writeTo(out);
 				out.close();
 				writer.close();
 				baseRequest.setHandled(true);
 				return;
 			}
 
 			// Otherwise return not found 404
 			processNotFound404(response, baseRequest);
 		}
 	}
 
 
 	private final void processNotFound404(HttpServletResponse response, Request baseRequest)
 			throws IOException, ServletException{
 		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
 		response.setContentType(MimeTypes.TEXT_HTML);
 		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		writer.write(notFound);
 
 		writer.flush();
 		response.setContentLength(writer.size());
 		OutputStream out = response.getOutputStream();
 		writer.writeTo(out);
 		out.close();
 		writer.close();
 		baseRequest.setHandled(true);
 		return;
 	}
 
 
 }
