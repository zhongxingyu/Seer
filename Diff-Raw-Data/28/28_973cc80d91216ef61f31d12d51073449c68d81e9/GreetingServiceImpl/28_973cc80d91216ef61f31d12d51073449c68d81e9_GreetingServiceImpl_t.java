 package cmg.org.monitor.entry.server;
 
import javax.persistence.PersistenceException;

import cmg.org.monitor.entity.SystemMonitor;
 import cmg.org.monitor.entry.client.GreetingService;
 import cmg.org.monitor.entry.shared.FieldVerifier;
import cmg.org.monitor.exception.BaseException;
import cmg.org.monitor.exception.MonitorException;
import cmg.org.monitor.services.SystemService;

 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class GreetingServiceImpl extends RemoteServiceServlet implements
 		GreetingService {
 
 	public String greetServer(String input) throws IllegalArgumentException {
 		// Verify that the input is valid. 
 		if (!FieldVerifier.isValidName(input)) {
 			// If the input is not valid, throw an IllegalArgumentException back to
 			// the client.
 			throw new IllegalArgumentException(
 					"Name must be at least 4 characters long");
 		}
 
 		String serverInfo = getServletContext().getServerInfo();
 		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
 
 		// Escape data from the client to avoid cross-site script vulnerabilities.
 		input = escapeHtml(input);
 		userAgent = escapeHtml(userAgent);
 
		try {
			SystemService aService = new SystemService();
			aService.addSystemMonitor( new SystemMonitor());
		}catch(Exception e) {
			return "System exception occurrence";
		} 
		
		
		
 		return "Hello, " + input + "!<br><br>I am running " + serverInfo
 				+ ".<br><br>It looks like you are using:<br>" + userAgent;
 	}
 
 	/**
 	 * Escape an html string. Escaping data received from the client helps to
 	 * prevent cross-site script vulnerabilities.
 	 * 
 	 * @param html the html string to escape
 	 * @return the escaped string
 	 */
 	private String escapeHtml(String html) {
 		if (html == null) {
 			return null;
 		}
 		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
 				.replaceAll(">", "&gt;");
 	}
 }
