 package org.microx.archiftp.logpage.internal;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.microx.archiftp.logpage.LogpageService;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogEntry;
 import org.osgi.service.log.LogReaderService;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 public final class LogpageServiceImpl implements LogpageService, ManagedService {
 
 	private ServiceTracker logServiceTracker;
 	private ServiceTracker httpServiceTracker;
 	private ServiceTracker logReaderServiceTracker;
 
 	private String servletAlias = "/archiftp/log";
 
 	private LogpageServlet servlet;
 	private String currentServletAlias = "/archiftp/log";
 	private String currentStyleAlias;
 	private String currentImageAlias;
 	private boolean firstRunPassed = false;
 
 	public LogpageServiceImpl(ServiceTracker logServiceTracker, ServiceTracker httpServiceTracker,
 			ServiceTracker logReaderServiceTracker) {
 		this.logServiceTracker = logServiceTracker;
 		this.httpServiceTracker = httpServiceTracker;
 		this.logReaderServiceTracker = logReaderServiceTracker;
 	}
 
 	public void registerNewLogpageServletWithRetries() {
 		HttpService service;
 		int attemp = 3;
 		long delayToAttemp = 3000;
 
 		while (true) {
 			service = getHttpService();
 			if (service != null) {
 				registerNewLogpageServlet(service);
 				return;
 			}
 			else {
 				if (attemp > 0) {
 					logWarningHttpServiceNotFoundAndTryAgain();
 					attemp--;
 					try {
 						Thread.sleep(3000);
 					}
 					catch (InterruptedException e) {
 						logErrorUnexpected(e);
 					}
 				}
 				else {
 					logErrorRegisterHttpServiceNotFound();
 					break;
 				}
 			}
 		}
 	}
 
 	private void registerNewLogpageServlet(HttpService service) {
 		if (service != null) {
 			this.servlet = new LogpageServlet();
 			this.currentServletAlias = this.servletAlias;
 			this.currentStyleAlias = String.format("%s/style", this.currentServletAlias);
 			this.currentImageAlias = String.format("%s/images", this.currentServletAlias);
 			try {
 				service.registerResources(this.currentStyleAlias, "/style", null);
 				service.registerResources(this.currentImageAlias, "/images", null);
 				service.registerServlet(this.currentServletAlias, this.servlet, null, null);
 				logInfoReistered();
 			}
 			catch (NamespaceException e) {
 				logErrorDuplicatedAlias(e);
 				unregisterServlet();
 				return;
 			}
 			catch (ServletException e) {
 				unregisterServlet();
 				logErrorServletNotFound(e);
 				return;
 			}
 		}
 		else {
 			logErrorRegisterHttpServiceNotFound();
 			return;
 		}
 	}
 
 	public void unregisterServlet() {
 		if (this.servlet != null) {
 			HttpService service = getHttpService();
 			if (service != null) {
 				service.unregister(this.currentStyleAlias);
 				service.unregister(this.currentImageAlias);
 				service.unregister(this.currentServletAlias);
 				logInfoUnregistered();
 			}
 			else {
 				logErrorUnregisterHttpServiceNotFound();
 			}
 		}
 	}
 
 	private HttpService getHttpService() {
 		if (this.httpServiceTracker != null) {
 			return (HttpService) this.httpServiceTracker.getService();
 		}
 		else {
 			return null;
 		}
 	}
 
 	@SuppressWarnings("serial")
 	public class LogpageServlet extends HttpServlet {
 
 		private PrintWriter writer;
 		private StringBuffer content;
 		private int logLevel = LogService.LOG_INFO;
 
 		private Enumeration<LogEntry> logs;
 		private boolean isNextRowOdd = true;
 		private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd KK:mm:ss");
 
 		protected void doGet(HttpServletRequest request, HttpServletResponse response)
 				throws ServletException, IOException {
 			resetVariables();
 			readAndSetLogs();
 			setLogLevelParameter(request);
 			setContent();
 			startAndSetPrintWriter(response);
 			render();
 		}
		
 		private void resetVariables() {
 			this.isNextRowOdd = true;
 			this.logLevel = LogService.LOG_INFO;
 		}
 
 		@SuppressWarnings("unchecked")
 		private void readAndSetLogs() {
 			LogReaderService reader = getLogReaderService();
 			if (reader != null) {
 				this.logs = reader.getLog();
 			}
 			else {
 				this.logs = null;
 			}
 		}
 
 		private void setLogLevelParameter(HttpServletRequest request) {
 			Object levelParam = request.getParameter("level");
 			if (levelParam != null) {
 				String level = ((String) levelParam).trim();
 				if (level.equalsIgnoreCase("error")) {
 					this.logLevel = LogService.LOG_ERROR;
 				}
 				else if (level.equalsIgnoreCase("warning")) {
 					this.logLevel = LogService.LOG_WARNING;
 				}
 				else if (level.equalsIgnoreCase("info")) {
 					this.logLevel = LogService.LOG_INFO;
 				}
 				else if (level.equalsIgnoreCase("debug")) {
 					this.logLevel = LogService.LOG_DEBUG;
 				}
 			}
 		}
 
 		private void startAndSetPrintWriter(HttpServletResponse response) throws IOException {
 			response.setCharacterEncoding("utf-8");
 			response.setContentType("text/html");
 			this.writer = response.getWriter();
 		}
 
 		private void setContent() {
 			this.content = new StringBuffer();
 			appendHeader();
 			appendBody();
 			appendFooter();
 
 		}
 
 		private void appendHeader() {
 			appendToContent("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
 					+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
 			appendToContent("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
 			appendToContent("<head>");
 			appendToContent("</head>");
 			appendToContent("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
 			appendToContent(String.format(
 					"<link href=\"%s/style/default.css\" rel=\"stylesheet\" type=\"text/css\"/>",
 					currentServletAlias));
 			appendToContent("<title>Archiftp Logpage</title>");
 		}
 
 		private void appendBody() {
 			appendToContent("<body>");
 			appendToContent("<div id=\"outer\">");
 			appendDivHeader();
 			appendDivMenu();
 			appendDivContent();
 			appendToContent("</div>");
 			appendToContent("</body>");
 		}
 
 		private void appendDivHeader() {
 			appendToContent("<div id=\"header\">");
 			appendToContent("<h1><a href=\".\">Archiftp</a></h1>");
 			appendToContent("<h2>Logpage</h2>");
 			appendToContent("</div>");
 		}
 
 		private void appendDivMenu() {
 			appendToContent("<div id=\"menu\">");
			appendToContent("<li class=\"first\"><a href=\".?level=error\">Error</a></li>");
			appendToContent("<li><a href=\".?level=warning\">Warning</a></li>");
			appendToContent("<li><a href=\".?level=info\">Info</a></li>");
			appendToContent("<li><a href=\".?level=debug\">Debug</a></li>");
 			appendToContent("<ul>");
 			appendToContent("</ul>");
 			appendToContent("</div>");
 		}
 
 		private void appendDivContent() {
 			appendToContent("<div id=\"content\">");
 			appendToContent("<div id=\"primaryContentContainer\">");
 			appendToContent("<div id=\"primaryContent\">");
 
 			appendLogs();
 			appendToContent("</div>");
 			appendToContent("</div>");
 			appendToContent("</div>");
 		}
 
 		private void appendLogs() {
 			if (this.logs != null) {
 				appendLogsHeader();
 				appendNoticeIfDebugLevel();
 				appendTableHeader();
 				while (this.logs.hasMoreElements()) {
 					LogEntry entry = this.logs.nextElement();
 					appendLogIfLogLevelHigher(entry);
 				}
 				appendTableFooter();
 			}
 			else {
 				appendErrorNoLogReader();
 			}
 		}
 
 		private void appendLogsHeader() {
 			String level = getStringOfLogLevel(this.logLevel);
 			appendToContent(String.format("<h2>Log (%s level)</h2>", level));
 		}
 
 		private void appendNoticeIfDebugLevel() {
 			if (this.logLevel == LogService.LOG_DEBUG) {
 				appendToContent("<h3>Notice</h3>");
 				appendToContent("<blockquote>");
 				appendToContent("<p>If there is no debug log. The configuration of OSGi framework "
 						+ "maybe set to don't log in debug level. (Ex. in Felix, you must to add "
 						+ "'org.apache.felix.log.storeDebug=true' in conf/config.properties)</p>");
 				appendToContent("</blockquote>");
 			}
 		}
 
 		private void appendTableHeader() {
 			appendToContent("<table>");
 			appendToContent("<tr class=\"rowH\">");
 			appendToContent("<th>Date/Time</th>");
 			appendToContent("<th>Level</th>");
 			appendToContent("<th>Message</th>");
 			appendToContent("<th>Exception</th>");
 			appendToContent("</tr>");
 		}
 
 		private void appendTableFooter() {
 			appendToContent("</table>");
 		}
 
 		private void appendLogIfLogLevelHigher(LogEntry entry) {
 			if (entry.getLevel() <= this.logLevel) {
 				appendLog(entry);
 			}
 		}
 
 		private void appendLog(LogEntry entry) {
 			String date = getStringOfDate(entry);
 			String level = getStringOfLogLevel(entry.getLevel());
 			String message = entry.getMessage();
 			String exception = "";
 			if (entry.getException() != null) {
 				exception = entry.getException().toString();
 			}
 			appendRowHeader();
 			appendToContent(String.format("<td>%s</td>", date));
 			appendToContent(String.format("<td>%s</td>", level));
 			appendToContent(String.format("<td>%s</td>", message));
 			appendToContent(String.format("<td>%s</td>", exception));
 			appendToContent("</tr>");
 			swapRowColor();
 		}
 
 		private String getStringOfDate(LogEntry entry) {
 			return this.dateFormat.format(new Date(entry.getTime()));
 		}
 
 		private void appendRowHeader() {
 			if (this.isNextRowOdd) {
 				appendToContent("<tr class=\"rowA\">");
 			}
 			else {
 				appendToContent("<tr class=\"rowB\">");
 			}
 		}
 
 		private void swapRowColor() {
 			this.isNextRowOdd = !this.isNextRowOdd;
 		}
 
 		private void appendErrorNoLogReader() {
 			appendToContent("<h2>Error</h2>");
 			appendToContent("<p>Cannot display logs (LogReaderService required).</p>");
 		}
 
 		private void appendFooter() {
 			this.content.append("<div class=\"clear\"></div>");
 			this.content.append("<div id=\"footer\">");
 			this.content.append("<p>Powered by <a href=\"http://www.microx.co.th/\">MicroX</a>, "
 					+ "Template by <a href=\"http://www.freecsstemplates.org/\">"
 					+ "Free CSS Templates</a></p>");
 			this.content.append("</div>");
 			this.content.append("</body>");
 			this.content.append("</html>");
 		}
 
 		private void appendToContent(String string) {
 			this.content.append(string);
 			this.content.append("\n");
 		}
 
 		private void render() {
 			String printedContent = this.content.toString();
 			this.writer.print(printedContent);
 		}
 	}
 
 	private LogReaderService getLogReaderService() {
 		if (this.logReaderServiceTracker == null) {
 			return null;
 		}
 
 		return (LogReaderService) this.logReaderServiceTracker.getService();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void updated(Dictionary properties) throws ConfigurationException {
 		if (properties != null) {
 			updateAlias(properties);
 			logInfoPropertiesUpdated();
 			restartServlet();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void updateAlias(Dictionary properties) throws ConfigurationException {
 		String newServletAlias = (String) properties.get("alias");
 		setServletAlias(newServletAlias);
 	}
 
 	private void restartServlet() {
 		if (this.firstRunPassed == true) {
 			unregisterServlet();
 		}
 		registerNewLogpageServletWithRetries();
 		this.firstRunPassed = true;
 	}
 
 	public String getServletAlias() {
 		return servletAlias;
 	}
 
 	public void setServletAlias(String servletAlias) {
 		this.servletAlias = servletAlias;
 	}
 
 	private void logWarningHttpServiceNotFoundAndTryAgain() {
 		String logMessage = "Can't register servlet (HttpService required). "
 				+ "Trying to register again in a few seconds.";
 		logWarning(logMessage);
 	}
 
 	private void logErrorUnexpected(Exception e) {
 		String logMessage = "Interrupted when waiting for retry.";
 		logError(logMessage, e);
 	}
 
 	private void logErrorDuplicatedAlias(Exception e) {
 		String logMessage = "Can't register servlet or resource "
 				+ "(The alias is using by other servlet).";
 		logError(logMessage, e);
 	}
 
 	private void logErrorServletNotFound(Exception e) {
 		String logMessage = "Can't register servlet (Servlet object maybe null).";
 		logError(logMessage, e);
 	}
 
 	private void logErrorRegisterHttpServiceNotFound() {
 		String logMessage = "Can't register servlet (HttpService required).";
 		logError(logMessage);
 	}
 
 	private void logErrorUnregisterHttpServiceNotFound() {
 		String logMessage = "Can't unregister servlet (HttpService required).";
 		logError(logMessage);
 	}
 
 	private void logInfoReistered() {
 		String logMessage = String.format("Logpage servlet '%s' was registered.",
 				this.currentServletAlias);
 		logInfo(logMessage);
 	}
 
 	private void logInfoUnregistered() {
 		String logMessage = String.format("Logpage servlet '%s' was unregistered.",
 				this.currentServletAlias);
 		logInfo(logMessage);
 	}
 
 	private void logInfoPropertiesUpdated() {
 		String logMessage = String.format("Properties of LogpageService updated. " + "{%s=%s}",
 				"alias", this.servletAlias);
 		logInfo(logMessage);
 	}
 
 	private void logError(String message) {
 		log(LogService.LOG_ERROR, message);
 	}
 
 	private void logError(String message, Exception e) {
 		log(LogService.LOG_ERROR, message, e);
 	}
 
 	private void logWarning(String message) {
 		log(LogService.LOG_WARNING, message);
 	}
 
 	private void logInfo(String message) {
 		log(LogService.LOG_INFO, message);
 	}
 
 	private void log(int level, String message) {
 		LogService logService = getLogService();
 		if (logService != null) {
 			logService.log(level, message);
 		}
 
 		String nameOfLevel = getStringOfLogLevel(level);
 		System.out.println(String.format("[%s] %s", nameOfLevel, message));
 	}
 
 	private void log(int level, String message, Exception e) {
 		LogService logService = getLogService();
 		if (logService != null) {
 			logService.log(level, message, e);
 		}
 
 		String nameOfLevel = getStringOfLogLevel(level);
 		System.out.println(String.format("[%s] %s (%s: %s)", nameOfLevel, message, e.getClass()
 				.getName(), e.getMessage()));
 	}
 
 	private String getStringOfLogLevel(int level) {
 		switch (level) {
 			case LogService.LOG_DEBUG:
 				return "Debug";
 			case LogService.LOG_ERROR:
 				return "Error";
 			case LogService.LOG_INFO:
 				return "Info";
 			case LogService.LOG_WARNING:
 				return "Warning";
 			default:
 				return "";
 		}
 	}
 
 	private LogService getLogService() {
 		if (this.logServiceTracker == null) {
 			return null;
 		}
 
 		return (LogService) this.logServiceTracker.getService();
 	}
 
 }
