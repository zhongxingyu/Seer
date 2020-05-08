 package org.openmrs.module.logmanager.web.view;
 
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.spi.LoggingEvent;
 import org.openmrs.ImplementationId;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.logmanager.Constants;
 import org.openmrs.module.logmanager.LogManagerService;
 import org.openmrs.module.logmanager.util.LogManagerUtils;
 import org.springframework.web.servlet.view.AbstractView;
 
 /**
  * View for event reports
  */
 public class EventReportView extends AbstractView {
 
 	protected static final SimpleDateFormat dfFilename = new SimpleDateFormat("yyyyMMdd-HHmm");
 	
 	/**
 	 * Gets the filename of the response
 	 * @param model the model from the controller
 	 * @return the filename string
 	 */
 	protected String getFilename(Map<String, Object> model) {
 		return "log-" + dfFilename.format(new Date()) + ".orep";
 	}
 	
 	/**
 	 * @see org.springframework.web.servlet.view.AbstractView
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 		
 		// Respond as a text file
 		response.setContentType("text/plain");
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + getFilename(model) + "\"");
 		
 		// Disable caching
 		response.setHeader("Pragma", "No-cache");
 		response.setDateHeader("Expires", 0);
 		response.setHeader("Cache-Control", "no-cache");
 		
 		PrintWriter out = response.getWriter();
 		
 		AdministrationService admSvc = Context.getAdministrationService();
 		LogManagerService logSvc = Context.getService(LogManagerService.class);
 		SortedMap<String, String> sysVars = admSvc.getSystemVariables();
 		ImplementationId implId = admSvc.getImplementationId();
 		
 		// Output system info
 		out.println("=================== SYSTEM INFO ===================");
 		if (implId != null)
 			out.println("Implementation: " + implId.getName() + " (" + implId.getImplementationId() + ")");
 		out.println("OpenMRS version: " + sysVars.get("OPENMRS_VERSION"));
 		out.println("Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
 		out.println("Server: " + getServletContext().getServerInfo());
 		out.println("MySQL: " + logSvc.getMySQLVersion());
 		out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
 		/*out.println("Hostname: " + sysVars.get("OPENMRS_HOSTNAME"));*/
 		
 		// Output module info
 		out.println("=================== MODULE INFO ===================");
 		Map<String, String> modMap = LogManagerUtils.createModuleVersionMap();
 		
 		for (Map.Entry<String, String> entry : modMap.entrySet()) {
 			String name = entry.getKey();
 			String version = entry.getValue();
 			
 			out.println(name + " (" + version + ")");
 		}
 		
 		// Output event 
 		out.println("=================== LOG EVENT =====================");
		LoggingEvent event = (LoggingEvent)model.get("event");
		PatternLayout layout = new PatternLayout(Constants.DEF_LAYOUT);
		out.println(layout.format(event));
 		
 		// Add finally the previous N events
 		out.println("================ PREVIOUS EVENTS ==================");
 		List<LoggingEvent> prevEvents = (List<LoggingEvent>)model.get("contextEvents");
 		for (LoggingEvent e : prevEvents)
			out.println(layout.format(e));
 	}
 }
