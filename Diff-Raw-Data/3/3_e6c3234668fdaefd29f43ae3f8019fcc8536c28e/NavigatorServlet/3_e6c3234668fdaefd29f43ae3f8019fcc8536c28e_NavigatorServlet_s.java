 /* OpenMark online assessment system
  * Copyright (C) 2007 The Open University
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.rmi.RemoteException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.rpc.ServiceException;
 
 import om.AbstractOpenMarkServlet;
 import om.Log;
 import om.OmException;
 import om.OmFormatException;
 import om.OmUnexpectedException;
 import om.OmVersion;
 import om.PreProcessingRequestHandler;
 import om.RenderedOutput;
 import om.RequestAssociates;
 import om.RequestHandler;
 import om.RequestHandlingException;
 import om.RequestParameterNames;
 import om.RequestResponse;
 import om.ShutdownManager;
 import om.axis.qengine.CustomResult;
 import om.axis.qengine.ProcessReturn;
 import om.axis.qengine.Resource;
 import om.axis.qengine.Results;
 import om.axis.qengine.Score;
 import om.axis.qengine.StartReturn;
 import om.tnavigator.auth.Authentication;
 import om.tnavigator.auth.AuthenticationFactory;
 import om.tnavigator.auth.AuthenticationInstantiationException;
 import om.tnavigator.db.DatabaseAccess;
 import om.tnavigator.db.OmQueries;
 import om.tnavigator.reports.ReportDispatcher;
 import om.tnavigator.request.tinymce.TinyMCERequestHandler;
 import om.tnavigator.scores.CombinedScore;
 
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import util.misc.ErrorManagement;
 import util.misc.ErrorMessageParts;
 import util.misc.HTTPS;
 import util.misc.IO;
 import util.misc.IPAddressCheckUtil;
 import util.misc.MimeTypes;
 import util.misc.NameValuePairs;
 import util.misc.OMVisitor;
 import util.misc.PeriodicThread;
 import util.misc.QuestionVersion;
 import util.misc.RequestHelpers;
 import util.misc.StopException;
 import util.misc.Strings;
 import util.misc.UserAgent;
 import util.misc.UtilityException;
 import util.misc.VersionUtil;
 import util.xml.XHTML;
 import util.xml.XML;
 import util.xml.XMLException;
 
 /** Om test navigator; implementation of the test delivery engine. */
 public class NavigatorServlet extends HttpServlet {
 
 	private static final String INPUTTOOLONG = "Input too long";
 
 	private static final String SEQUENCEFIELD = "sequence";
 
 	private static final String COOKIENAME = "tnavigator_session";
 
 	private static final String FAKEOUCUCOOKIENAME = "tnavigator_xid";
 
 	private static final String SUMMARYTABLE_NOTANSWERED = "Not completed";
 
 	private static String SCRIPT_JS = "script.js";
 
 	/**
 	 * User passed on question. Should match the definition in
 	 * Om.question.Results.
 	 */
 	public final static int ATTEMPTS_PASS = 0;
 	/**
 	 * User got question wrong after all attempts. Should match the definition
 	 * in om.question.Results.
 	 */
 	public final static int ATTEMPTS_WRONG = -1;
 	/**
 	 * User got question partially correct after all attempts. Should match the
 	 * definition in om.question.Results.
 	 */
 	public final static int ATTEMPTS_PARTIALLYCORRECT = -2;
 	/**
 	 * If developer hasn't set the value. Should match the definition in
 	 * om.question.Results.
 	 */
 	public final static int ATTEMPTS_UNSET = -99;
 
 	private static String USE_NEW_SUMMARY_IMPLEMENTATION = "useNewHandleSummaryTableImplementation";
 
 	/** Database access */
 	DatabaseAccess da;
 
 	/** @return the DatabaseAccess object used by this servlet. */
 	public DatabaseAccess getDatabaseAccess() {
 		return da;
 	}
 
 	/** Authentication system */
 	private Authentication authentication;
 
 	/** Config file contents */
 	private NavigatorConfig nc;
 
 	protected Authentication getAuthentication() {
 		return authentication;
 	}
 
 	/** @return the NavigatorConfig for this servlet. */
 	public NavigatorConfig getNavigatorConfig() {
 		return nc;
 	}
 
 	/** Question bank folder */
 	private File questionBankFolder;
 
 	private ErrorManagement errorManagement;
 
 	// *************************************************************************
 	// TMH - FIX THESE - get them out of here and change the implementation .....
 	// *************************************************************************
 	
 	/** Map of cookie value (String) -> UserSession */
 	private Map<String, UserSession> sessions = new HashMap<String, UserSession>();
 
 	/** Map of OUCU-testID (String) -> UserSession */
 	private Map<String, UserSession> usernames = new HashMap<String, UserSession>();
 
 	/** Map of OUCU-testID (String) -> Long (date that prohibition expires) */
 	private Map<String, Long> tempForbid = new HashMap<String, Long>();
 
 	/** Load balancer for Om question engines */
 	private OmServiceBalancer osb;
 
 	/** Session expiry thread */
 	private SessionExpirer sessionExpirer;
 
 	/** Tracks when last error occurred while sending forbids to each other nav */
 	private long[] lastSessionKillerError;
 
 	private Object sessionKillerErrorSynch = new Object();
 
 	/**
 	 * List of NewSession objects that are stored to check we don't start a new
 	 * session twice in a row to same address (= cookies off)
 	 */
 	private LinkedList<NewSession> cookiesOffCheck = new LinkedList<NewSession>();
 
 	/**
 	 * Cache of question metadata: String (ID\nversion) -> Document.
 	 * <p>
 	 * This cache is kept in memory until server restart because come on, how
 	 * many questions can there be? It's only one document each. There is no
 	 * need to refresh it, because questions are guaranteed to change in version
 	 * if their content changes.
 	 */
 	private Map<String, Element> questionMetadata = new HashMap<String, Element>();
 
 	private static String navigatorCSS = null;
 
 	/** Cache label replacement (Map of String (labelset id) -> Map ) */
 	private Map<String, Map<String, String>> labelReplace = new HashMap<String, Map<String, String>>();
 
 	// *************************************************************************
 	// TMH - END OF ............................................................
 	// *************************************************************************
 	
 	private static class NewSession {
 		long lTime;
 		String sAddr;
 	}	
 	
 	/** Log */
 	protected Log l;
 
 	/** @return the log for this servlet. */
 	public Log getLog() {
 		return l;
 	}
 
 	int checkOmServiceAvailable() throws RemoteException {
 		return osb.checkAvailable();
 	}
 
 	/** Reports-handling code */
 	private ReportDispatcher reports;
 
 	/** Status page display code */
 	private StatusPages status = new StatusPages(this);
 
 	/** SQL queries */
 	private OmQueries oq;
 
 	private static String PRE_PROCESSING_REQUEST_HANDLER = "PreProcessingRequestHandler";
 
 	private Class<?> preProcessingRequestHandler;
 
 	/** How long an unused session lurks around before expiring (8 hrs) */
 	private final static int SESSIONEXPIRY = 8 * 60 * 60 * 1000;
 
 	/** How often we check for expired sessions (15 mins) */
 	private final static int SESSIONCHECKDELAY = 15 * 60 * 1000;
 
 	/**
 	 * File that is included to put up a maintenance message during problem
 	 * periods.
 	 */
 	private File maintenanceFile;
 
 	/** Just to stop us checking the file more than once in 3 seconds */
 	private long lastMaintenanceCheck = 0;
 
 	private final static int MAINTENANCECHECK_LATENCY = 3000;
 
 	private final static Pattern ZOOMPATTERN = Pattern
 			.compile(".*\\[zoom=(.*?)\\].*");
 
 	private final static String ACCESSOUTOFSEQUENCE = "Access out of sequence";
 
 	/** Used to remove CSS lines that have the FIXED comment at beginning */
 	private final static Pattern FIXEDCOLOURCSSLINE = Pattern.compile(
 			"^/\\*FIXED\\*/.*?$", Pattern.MULTILINE);
 
 	/** @return Folder used for template files */
 	public File getTemplatesFolder() {
 		return new File(getServletContext().getRealPath("WEB-INF/templates"));
 	}
 
 	protected void initialiseAuthentication() throws ServletException {
 		try {
 			authentication = AuthenticationFactory.initialiseAuthentication(nc,
 				da, getTemplatesFolder(), l);
 		} catch (AuthenticationInstantiationException x) {
 			throw new ServletException(
 				"Error creating authentication class.", x);
 		}
 	}
 
 	@Override
 	public void init() throws ServletException {
 		ServletContext sc = getServletContext();
 		try {
 			maintenanceFile = new File(sc.getRealPath("maintenance.xhtml"));
 			nc = new NavigatorConfig(new File(sc.getRealPath("navigator.xml")));
 		} catch (MalformedURLException e) {
 			throw new ServletException("Unexpected error parsing service URL",
 					e);
 		} catch (IOException e) {
 			throw new ServletException("Error loading config file", e);
 		}
 
 		try {
 			l = new Log(new File(sc.getRealPath("logs")), "navigator", nc
 					.hasDebugFlag("log-general"));
 		} catch (IOException e) {
 			throw new ServletException("Error creating log", e);
 		}
 		try {
 			errorManagement = new ErrorManagement(nc, l);
 		} catch (UtilityException x) {
 			throw new ServletException(x);
 		}
 
 		try {
 			osb = new OmServiceBalancer(nc.getOmServices(), l, nc
 					.hasDebugFlag("log-balancer"));
 		} catch (ServiceException e) {
 			throw new ServletException("Unexpected error obtaining service", e);
 		}
 
 		questionBankFolder = new File(sc.getRealPath("questionbank"));
 
 		lastSessionKillerError = new long[nc.getOtherNavigators().length];
 
 		String dbClass = nc.getDBClass();
 		String dbPrefix = nc.getDBPrefix();
 		try {
 			oq = (OmQueries) Class.forName(dbClass).getConstructor(
 					new Class[] { String.class }).newInstance(
 					new Object[] { dbPrefix });
 			da = new DatabaseAccess(nc.getDatabaseURL(oq), nc
 					.hasDebugFlag("log-sql") ? l : null);
 		} catch (Exception e) {
 			throw new ServletException(
 					"Error creating database class or JDBC driver (make sure DB plugin and JDBC driver are both installed): "
 							+ e.getMessage());
 		}
 
 		DatabaseAccess.Transaction dat = null;
 		try {
 			dat = da.newTransaction();
 			oq.checkTables(dat, l, nc);
 		} catch (Exception e) {
 			throw new ServletException("Error initialising database tables: "
 					+ e.getMessage(), e);
 		} finally {
 			if (dat != null)
 				dat.finish();
 		}
 		initialiseAuthentication();	
 		try {
 			plainTemplate = XML.parse(new File(sc
 					.getRealPath("WEB-INF/templates/plaintemplate.xhtml")));
 			template = XML.parse(new File(sc
 					.getRealPath("WEB-INF/templates/template.xhtml")));
 			singlesPlainTemplate = XML
 					.parse(new File(
 							sc
 									.getRealPath("WEB-INF/templates/singlesplaintemplate.xhtml")));
 			singlesTemplate = XML.parse(new File(sc
 					.getRealPath("WEB-INF/templates/singlestemplate.xhtml")));
 			authorshipConfirmation = XML.parse(new File(getServletContext()
 					.getRealPath(
 							"WEB-INF/templates/authorship-confirmation.xml")));
 		} catch (IOException ioe) {
 			throw new ServletException("Failed to initialise XML templates",
 					ioe);
 		}
 
 		try {
 			reports = new ReportDispatcher(this, Arrays.asList(nc
 					.getExtraReports()));
 		} catch (Exception e) {
 			throw new ServletException("Error creating report classes", e);
 		}
 
 		// Start expiry thread
 		sessionExpirer = new SessionExpirer();
 		setUpPreProcessingRequestHandler();
 	}
 
 	/**
 	 * Here we pickup the configured PreProcessingRequestHandler and if there
 	 * was one then we try to actually load the Class so that it may be used
 	 * later on. It is expected that this is invoked within the init() method of
 	 * this class so that things are setup and in place for the actual user
 	 * requests.
 	 * 
 	 * @author Trevor Hinson
 	 */
 	private void setUpPreProcessingRequestHandler() {
 		String pre = getServletConfig().getInitParameter(
 				PRE_PROCESSING_REQUEST_HANDLER);
 		if (StringUtils.isNotEmpty(pre)) {
 			try {
 				preProcessingRequestHandler = getClass().getClassLoader()
 						.loadClass(pre);
 			} catch (ClassNotFoundException x) {
 				l.logError("There was a problem with loading the"
 					+ " PreProcessingRequestHandler with the configured String : "
 					+ preProcessingRequestHandler, x);
 			}
 		}
 	}
 
 	/**
 	 * Here we dynamically class load the configured PreProcessingRequestHandler
 	 * implementation (if configured) so that it may be used.
 	 * 
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	private PreProcessingRequestHandler getPreProcessingRequestHandler() {
 		PreProcessingRequestHandler pre = null;
 		Class<?> rh = nc.retrievePreProcessingRequestHandler();
 		if (null != rh) {
 			try {
 				Object o = rh.newInstance();
 				if (null != o ? o instanceof PreProcessingRequestHandler
 						: false) {
 					pre = (PreProcessingRequestHandler) o;
 				}
 			} catch (InstantiationException x) {
 				l.logError(
 					"Problem loading the configured PreProcessingRequestHandler",
 					x);
 			} catch (IllegalAccessException x) {
 				l.logError(
 					"Problem loading the configured PreProcessingRequestHandler",
 					x);
 			}
 		} else {
 			String msg = "The <preprocessing> node has not been specified"
 				+ " in the navigator.xml.";
 			l.logError(msg);
 			errorManagement.sendAdminAlert(msg, null);
 		}
 		return pre;
 	}
 
 	/** Thread that gets rid of unused sessions */
 	class SessionExpirer extends PeriodicThread {
 		SessionExpirer() {
 			super(SESSIONCHECKDELAY);
 		}
 
 		@Override
 		protected void tick() {
 			synchronized (sessions) {
 				// See if any sessions need expiring
 				long lNow = System.currentTimeMillis();
 				long lExpiry = lNow - SESSIONEXPIRY;
 
 				for (Iterator<UserSession> i = sessions.values().iterator(); i
 						.hasNext();) {
 					UserSession us = i.next();
 					if (us.getLastActionTime() < lExpiry) {
 						i.remove();
 						usernames.remove(us.sCheckedOUCUKey);
 					}
 				}
 
 				for (Iterator<Long> i = tempForbid.values().iterator(); i
 						.hasNext();) {
 					if (lNow > i.next()) {
 						i.remove();
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void destroy() {
 		// Kill expiry thread
 		sessionExpirer.close();
 		// Close SAMS and kill their threads
 		getAuthentication().close();
 		// Close database connections
 		da.close();
 		// Close log
 		l.close();
 		// Tell shutdown manager we've shut down. (I don't think this is
 		// necessary,
 		// it kind of got copied from Promises, but could potentially be useful
 		// for clearing static data...)
 		ShutdownManager.shutdown();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		handle(false, request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		handle(true, request, response);
 	}
 
 	/**
 	 * Initialises the basic information in a UserSession that relates to a
 	 * particular test. Before calling this, you must initialise the random
 	 * seed.
 	 * 
 	 * @param us
 	 *            User session
 	 * @param rt
 	 *            Used for storing performance information
 	 * @param sTestID
 	 *            Test ID
 	 * @param request
 	 *            HTTP request
 	 * @param response
 	 *            HTTP response (used if they don't have access)
 	 * @param bFinished
 	 *            Whether the students has finished this test.
 	 * @param bStarted
 	 *            Whether the students has already started this test.
 	 * @throws StopException
 	 *             If something was sent to user
 	 * @throws OmException
 	 *             Any error
 	 */
 	private void initTestSession(UserSession us, RequestTimings rt,
 			String sTestID, HttpServletRequest request,
 			HttpServletResponse response, boolean bFinished, boolean bStarted,
 			long randomSeed, int fixedVariant) throws Exception {
 		// Check access
 		if (!us.getTestDeployment().isWorldAccess()
 				&& !us.getTestDeployment().hasAccess(us.ud)) {
 			sendError(us, request, response, HttpServletResponse.SC_FORBIDDEN,
 					false, false, null, "Access denied",
 					"You do not have access to this test.", null);
 		}
 		us.bAdmin = us.getTestDeployment().isAdmin(us.ud);
 		us.bAllowReports = us.getTestDeployment().allowReports(us.ud);
 
 		if (!us.bAdmin && !us.getTestDeployment().isAfterOpen()) {
 			sendError(us, request, response, HttpServletResponse.SC_FORBIDDEN,
 					false, false, null, "Test not yet available",
 					"This test is not yet available.", null);
 		}
 
 		// Realise the test for this student.
 		// Initialise test settings
 		us.realiseTest(sTestID, bFinished, randomSeed, fixedVariant);
 
 		// If the test is finished, we allow them through even after the forbid
 		// date
 		// so they can see their results.
 		if (!us.bAdmin && us.getTestDeployment().isAfterForbid()
 				&& !us.isFinished() && !us.bAllowAfterForbid) {
 			if (us.getTestDeployment().isAfterForbidExtension() || !bStarted) {
 				sendError(us, request, response,
 						HttpServletResponse.SC_FORBIDDEN, false, false, null,
 						"Test no longer available",
 						"This test is no longer available to students.", null);
 			} else {
 				// OK, they have an unfinished test. Offer them the end page
 				handleEnd(rt, false, true, us, request, response);
 				throw new StopException();
 			}
 		}
 	}
 
 	/**
 	 * Class for accumulating performance information.
 	 */
 	public static class RequestTimings {
 		long lStart;
 		long lDatabaseElapsed;
 		long lQEngineElapsed;
 
 		/**
 		 * @param time
 		 *            add this much time to the questionEngine time.
 		 */
 		public void recordServiceTime(long time) {
 			lQEngineElapsed += time;
 		}
 	}
 
 	/** Thread used to 'kill' a session on other servers by sending forbid calls */
 	private class RemoteSessionKiller extends Thread {
 		private String sOUCUTest;
 
 		RemoteSessionKiller(String sOUCUTest) {
 			this.sOUCUTest = sOUCUTest;
 			start();
 		}
 
 		@Override
 		public void run() {
 			String[] asURL = nc.getOtherNavigators();
 			for (int i = 0; i < asURL.length; i++) {
 				try {
 					URL u = new URL(asURL[i] + "!forbid/" + sOUCUTest);
 					HttpURLConnection huc = (HttpURLConnection) u
 							.openConnection();
 					HTTPS.allowDifferentServerNames(huc);
 					HTTPS.considerCertificatesValid(huc);
 
 					huc.connect();
 					if (huc.getResponseCode() != HttpServletResponse.SC_OK)
 						throw new IOException("Error with navigator "
 								+ huc.getResponseCode());
 					IO.eat(huc.getInputStream());
 					if (lastSessionKillerError[i] != 0) {
 						synchronized (sessionKillerErrorSynch) {
 							lastSessionKillerError[i] = 0;
 						}
 						// Because we only display errors once per hour, better
 						// display the
 						// 'OK' state too if it was marked as error before
 						l.logNormal("Forbids", asURL[i]
 								+ ": Forbid call OK now");
 					}
 				} catch (IOException ioe) {
 					// Display an error once per hour if it's still erroring
 					long lNow = System.currentTimeMillis();
 					synchronized (sessionKillerErrorSynch) {
 						if (lastSessionKillerError[i] == 0
 								|| (lNow - lastSessionKillerError[i]) > 60 * 60 * 1000) {
 							l.logWarning("Forbids", asURL[i]
 									+ ": Forbid call failed", ioe);
 							lastSessionKillerError[i] = lNow;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Obtains cookie based on its name.
 	 * 
 	 * @param request
 	 *            HTTP request
 	 * @param sName
 	 *            Name of cookie
 	 * @return Cookie value
 	 */
 	private String getCookie(HttpServletRequest request, String sName) {
 		Cookie[] ac = request.getCookies();
 		if (ac == null)
 			ac = new Cookie[0];
 		for (int iCookie = 0; iCookie < ac.length; iCookie++) {
 			if (ac[iCookie].getName().equals(sName))
 				return ac[iCookie].getValue();
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether the maintenance-mode file exists. If it does then it is
 	 * loaded and displayed.
 	 * 
 	 * @param request
 	 * @param response
 	 */
 	private void checkMaintenanceMode(HttpServletRequest request,
 			HttpServletResponse response) throws StopException {
 		// Treat as OK up to 3 seconds since last check
 		if (System.currentTimeMillis() - lastMaintenanceCheck < MAINTENANCECHECK_LATENCY)
 			return;
 		// File still there? OK
 		if (!maintenanceFile.exists()) {
 			lastMaintenanceCheck = System.currentTimeMillis();
 			return;
 		}
 		// They have the magic cookie or are requesting it?
 		if ("/!letmein".equals(request.getPathInfo())) {
 			Cookie c = new Cookie("openmark-letmein", "pretty please!");
 			c.setPath("/");
 			response.addCookie(c);
 			response.setContentType("text/plain");
 			try {
 				response
 						.getWriter()
 						.println(
 								"OK, you're in! (Close the browser entirely, "
 										+ "or clear session cookies, to lock yourself back out.)");
 			} catch (IOException e) {
 			}
 			throw new StopException();
 		} else if (getCookie(request, "openmark-letmein") != null) {
 			return;
 		}
 
 		// No file!
 		lastMaintenanceCheck = 0;
 		try {
 			Document d = XML.parse(maintenanceFile);
 			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
 
 			XHTML.output(d, request, response, "en");
 		} catch (Throwable t) {
 			response.setContentType("text/plain");
 			try {
 				t.printStackTrace(response.getWriter());
 			} catch (IOException e) {
 			}
 		}
 		throw new StopException();
 	}
 
 	private void handle(boolean bPost, HttpServletRequest request,
 			HttpServletResponse response) {
 		RequestTimings rt = new RequestTimings();
 		rt.lStart = System.currentTimeMillis();
 		UserSession us = null;
 		String sPath = null;
 		try {
 			// Vitally important, otherwise any input with unicode gets screwed
 			// up
 			request.setCharacterEncoding("UTF-8");
 			// Check path
 			sPath = request.getPathInfo();
 			if (sPath == null)
 				sPath = "";
 
 			// Handle requests for question, test and deploy files separately
 			// as they're not from users, so don't need the session stuff.
 			if (sPath.equals("/!question/list")) {
 				// Delegate producing the question list to the
 				// DeployedQuestionReport
 				// by pretending we came from a different URL with an extra
 				// parameter.
 				sPath = "/!report/allquestions";
 				request = new HttpServletRequestWrapper(request) {
 					@Override
 					public String getParameter(String name) {
 						if ("format".equals(name)) {
 							return "xml";
 						}
 						return super.getParameter(name);
 					}
 				};
 			} else if (!bPost && sPath.startsWith("/!question/")) {
 				handleQuestion(sPath.substring("/!question/".length()),
 						request, response);
 				return;
 			}
 			if (!bPost && sPath.startsWith("/!test/")) {
 				handleTest(sPath.substring("/!test/".length()), request,
 						response);
 				return;
 			}
 			if (!bPost && sPath.startsWith("/!deploy/")) {
 				handleDeploy(sPath.substring("/!deploy/".length()), request,
 						response);
 				return;
 			}
 
 			// Handle session-forbid requests [from other TNs]
 			if (!bPost && sPath.startsWith("/!forbid/")) {
 				handleForbid(sPath.substring("/!forbid/".length()), request,
 						response);
 				return;
 			}
 
 			// Handle status requests
 			if (!bPost && sPath.startsWith("/!status/")) {
 				status.handle(sPath.substring("/!status/".length()), request,
 						response);
 				return;
 			}
 
 			checkMaintenanceMode(request, response);
 
 			// Allow shared files to everyone too
 			if (!bPost && sPath.startsWith("/!shared/")) {
 				handleShared(sPath.substring("/!shared/".length()), request,
 						response);
 				return;
 			}
 			if (!bPost && sPath.startsWith("/navigator.")
 					&& sPath.endsWith(".css")) {
 				if (sPath.equals("/navigator.css"))
 					handleNavigatorCSS(null, request, response);
 				else
 					handleNavigatorCSS(sPath.substring("/navigator.".length(),
 							sPath.length() - ".css".length()), request,
 							response);
 				return;
 			}
 
 			if (sPath.contains("tiny_mce/") || sPath.contains("subsup.html")) {
 				handleTinyMCEResponse(sPath, bPost, request, response);
 				return;
 			}
 			
 			// Handle requests that go via the authentication system
 			if (sPath.startsWith("/!auth/")) {
 				if (!getAuthentication().handleRequest(sPath.substring("/!auth/".length()),
 						request, response))
 					throw new Exception(
 							"Requested URL is not handled by authentication plugin");
 				return;
 			}
 
 			// Handle special test user requests
 			if (!bPost && sPath.startsWith("/!test/")) {
 				handleTestCookie(sPath.substring("/!test/".length()), request,
 						response);
 				return;
 			}
 
 			// Handle system report requests
 			if (!bPost && sPath.startsWith("/!report/")) {
 				reports.handleReport(sPath.substring("/!report/".length()),
 						request, response);
 				return;
 			}
 
 			// Get test ID and determine type of request
 			Pattern pURL = Pattern.compile("^/([^/]+)/?(.*)$");
 			Matcher m = pURL.matcher(sPath);
 			if (!m.matches()) {
 				sendError(
 						us,
 						request,
 						response,
 						HttpServletResponse.SC_NOT_FOUND,
 						false,
 						false,
 						null,
 						"Not found",
 						"The URL you requested is not provided by this server.",
 						null);
 			}
 			String sTestID = m.group(1), sCommand = m.group(2);
 
 			if ("".equals(sCommand) && !request.getRequestURI().endsWith("/")) {
 				response.sendRedirect(request.getRequestURI() + "/");
 				return;
 			}
 			if (sCommand.startsWith("_"))
 				sCommand = ""; // Used to allow random different URLs in plain
 			// mode
 			if (request.getQueryString() != null)
 				sCommand += "?" + request.getQueryString();
 
 			// Get OUCU (null if no SAMS cookie), used to synch sessions for
 			// single user
 			String sOUCU = getAuthentication().getUncheckedUserDetails(request).getUsername();
 
 			// See if they've got a cookie for this test
 			String sTestCookieName = COOKIENAME + "_" + sTestID;
 			String sCookie = getCookie(request, sTestCookieName);
 
 			// See if they've got a fake OUCU (null if none)
 			String sFakeOUCU = getCookie(request, FAKEOUCUCOOKIENAME);
 
 			// Auth hash
 			int iAuthHash = (getAuthentication().getUncheckedUserDetails(request).getCookie()
 					+ "/" + sFakeOUCU).hashCode();
 
 			// If they haven't got a cookie or it's unknown, assign them one and
 			// redirect.
 			boolean bNewCookie = false, bTempForbid = false;
 			String sKillOtherSessions = null;
 			synchronized (sessions) {
 				// Remove entries from cookies-off list after 1 second
 				while (!cookiesOffCheck.isEmpty()
 						&& rt.lStart - (cookiesOffCheck.getFirst()).lTime > 1000) {
 					cookiesOffCheck.removeFirst();
 				}
 
 				// Check whether they already have a session or not
 				if (sCookie == null || !sessions.containsKey(sCookie)) {
 					// No session, need a new one
 					bNewCookie = true;
 				} else {
 					// Get session
 					us = sessions.get(sCookie);
 
 					// Check cookies in case they changed
 					if (us.iAuthHash != 0 && us.iAuthHash != iAuthHash) {
 						// If credentials change, they need a new session
 						sessions.remove(sCookie);
 						bNewCookie = true;
 					}
 				}
 
 				// New sessions!
 				if (bNewCookie) {
 					String sAddr = request.getRemoteAddr();
 
 					// Check if we've already been redirected
 					for (NewSession ns : cookiesOffCheck) {
 						if(ns.sAddr.equals(sAddr)) {
 								sendError(null, request, response,
 										HttpServletResponse.SC_FORBIDDEN,
 										false, false,null,
 										"Unable to create session cookie",
 										"In order to use this website you must enable cookies in your browser settings.",
 										null);
 						}
 					}
 
 					// Record this redirect so that we notice if it happens
 					// twice
 					NewSession ns = new NewSession();
 					ns.lTime = rt.lStart;
 					ns.sAddr = sAddr;
 					cookiesOffCheck.addLast(ns);
 
 					do {
 						// Make 7-letter random cookie
 						sCookie = Strings.randomAlNumString(7);
 					} while (sessions.containsKey(sCookie)); // And what are the
 					// chances of
 					// that?
 
 					us = new UserSession(this, sCookie);
 					sessions.put(sCookie, us);
 					// We do the actual redirect later on outside this synch
 
 					// At same time as creating new session, if they're logged
 					// in supposedly,
 					// check it's for real. If their cookie doesn't
 					// authenticated, this will
 					// cause the cookie to be removed and avoid multiple
 					// redirects.
 					if (sOUCU != null) {
 						if (!getAuthentication().getUserDetails(request, response, false)
 								.isLoggedIn()) {
 							// And we need to set this to zero to reflect that
 							// we just wiped
 							// their cookie.
 							iAuthHash = 0;
 						}
 					}
 				}
 
 				// If this is the first time we've had an OUCU for this session,
 				// check
 				// it to make sure we don't need to ditch any other sessions
 				if (us.sCheckedOUCUKey == null && sOUCU != null) {
 					us.sCheckedOUCUKey = sOUCU + "-" + sTestID;
 
 					// Check the temp-forbid list
 					Long lTimeout = tempForbid.get(us.sCheckedOUCUKey);
 					if (lTimeout != null
 							&& lTimeout.longValue() > System
 									.currentTimeMillis()) {
 						// Kill session from main list & mark it to send error
 						// message later
 						sessions.remove(us.sCookie);
 						bTempForbid = true;
 					} else {
 						// If it was a timed-out forbid, get rid of it
 						if (lTimeout != null)
 							tempForbid.remove(us.sCheckedOUCUKey);
 
 						// Put this in the OUCU->session map
 						UserSession usOld = usernames.put(us.sCheckedOUCUKey,
 								us);
 						// If there was one already there, get rid of it
 						if (usOld != null) {
 							sessions.remove(usOld.sCookie);
 						}
 						sKillOtherSessions = us.sCheckedOUCUKey;
 					}
 				}
 			}
 			// If they started a session, tell other servers to kill that
 			// session (in thread)
 			if (sKillOtherSessions != null)
 				new RemoteSessionKiller(sKillOtherSessions);
 			// Error if forbidden
 			if (bTempForbid) {
 				sendError(
 						null,
 						request,
 						response,
 						HttpServletResponse.SC_FORBIDDEN,
 						false,
 						false,
 						null,
 						"Simultaneous sessions forbidden",
 						"The system thinks you have tried to log on using two different "
 								+ "browsers at the same time, which isn't permitted. If this message "
 								+ "has appeared in error, please wait 60 seconds then try again.",
 						null);
 			}
 
 			// Synchronize on the session - if we get multiple requests for same
 			// session at same time the others will just have to wait.
 			// (Except see below for resources.)
 			synchronized (us) {
 				// Set last action time (so session doesn't time out)
 				us.touch();
 
 				// If they have an OUCU but also a temp-login then we need to
 				// chuck away
 				// their session...
 				if (us.ud != null && sOUCU != null && !us.ud.isLoggedIn()) {
 					Cookie c = new Cookie(sTestCookieName, "");
 					c.setMaxAge(0);
 					c.setPath("/");
 					response.addCookie(c);
 					response.sendRedirect(request.getRequestURI());
 					return;
 				}
 
 				// Get auth if needed
 				if (us.ud == null) {
 					try {
 						us.loadTestDeployment(sTestID);
 					} catch (OmException oe) {
 						if (oe.getCause() != null
 								&& oe.getCause() instanceof FileNotFoundException) {
 							sendError(us, request, response,
 									HttpServletResponse.SC_NOT_FOUND, false,
 									false, null, "No such test",
 									"There is currently no test with the ID "
 											+ sTestID + ".", null);
 						} else
 							throw oe;
 					}
 
 					us.ud = getAuthentication().getUserDetails(request, response, !us
 							.getTestDeployment().isWorldAccess());
 					if (us.ud == null) {
 						// They've been redirected to SAMS. Chuck their session
 						// as soon
 						// as expirer next runs, they won't be needing it as we
 						// didn't give
 						// them a cookie yet
 						us.markForDiscard();
 						return;
 					}
 
 					// We only give them a cookie after passing this stage. If
 					// they were
 					// redirected to SAMS, they don't get a cookie until the
 					// next request.
 					if (bNewCookie) {
 						Cookie c = new Cookie(sTestCookieName, sCookie);
 						c.setPath("/");
 						response.addCookie(c);
 					}
 
 					// If they're not logged in, give them a not-logged-in
 					// cookie with
 					// a made-up OUCU in it
 					if (us.ud.isLoggedIn()) {
 						us.sOUCU = us.ud.getUsername();
 					} else {
 						if (sFakeOUCU == null) {
 							// Make 8-letter random OUCU. We don't bother
 							// storing a list,
 							// but there are about 3,000 billion possibilities
 							// so it should
 							// be OK.
 							us.sOUCU = "_" + Strings.randomAlNumString(7);
 
 							// Set it in cookie for future sessions
 							Cookie c = new Cookie(FAKEOUCUCOOKIENAME, us.sOUCU);
 							c.setPath("/");
 							// Expiry is 4 years
 							c.setMaxAge((3 * 365 + 366) * 24 * 60 * 60);
 							response.addCookie(c);
 							response.sendRedirect(request.getRequestURI());
 							return;
 						} else {
 							us.sOUCU = sFakeOUCU;
 						}
 					}
 
 					// Remember auth hash so that it'll know if they change
 					// cookie now
 					us.iAuthHash = iAuthHash;
 				}
 
 				us.bAllowAfterForbid =
 				// * a posted 'end session' request
 				(bPost && sCommand.equals("?end")) ||
 				// * Access options for that page;
 						sCommand.equals("?access") ||
 						// * Stylesheet
 						sCommand.matches("resources/[0-9]+/style-[0-9]+\\.css");
 
 				// Check test hasn't timed out
 				if (us.getTestId() != null
 						&& us.getTestDeployment().isAfterForbid()
 						// Only exceptions we allow are:
 						// * admin
 						&& !us.bAdmin
 						// * 'finished test' requests to show results pages
 						&& !us.isFinished()
 						// * Things that are allowed after forbid
 						&& !(us.bAllowAfterForbid && !us.getTestDeployment()
 								.isAfterForbidExtension())) {
 					// It is forbidden. Drop session.
 					Cookie c = new Cookie(sTestCookieName, "false");
 					c.setMaxAge(0);
 					c.setPath("/");
 					response.addCookie(c);
 					response.sendRedirect(request.getRequestURI());
 					return;
 				}
 
 				if (sCommand.equals("")) {
 					// Have they possibly lost an existing session? If so, go
 					// find it
 					if (us.getTestId() == null) {
 						if (!us.isSingle()
 								&& checkRestartSession(rt, sTestID, us,
 										request, response))
 							return;
 					}
 
 					// If it's a GET and either they have no test or they aren't
 					// on this one...
 					// (the latter should not be possible since cookies are
 					// test-specific)
 					if (!bPost
 							&& (us.getTestId() == null || !sTestID.equals(us
 									.getTestId()))) {
 						// Start this test
 						handleStart(rt, sTestID, us, -1, request, response);
 						return;
 					}
 					// Otherwise check they're on current test (if not, wtf?)
 					if (us.getTestId() == null
 							|| !sTestID.equals(us.getTestId())) {
 						sendError(
 								us,
 								request,
 								response,
 								HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 								true,
 								false,
 								null,
 								"Unexpected request",
 								"The action you just took doesn't seem to "
 										+ "match the test you are currently on.",
 								null);
 					}
 
 					// OK, handle action
 					if (bPost && us.oss != null)
 						// us.oss check is to make sure there's actually a
 						// question session.
 						// In certain cases of bad timing/impatient users, the
 						// post might
 						// occur when there isn't one, which causes a crash. So
 						// in that
 						// case let's just do handleNothing and hope it gets
 						// back onto
 						// even keel.
 						handleProcess(rt, us, request, response);
 					else
 						handleNothing(rt, us, request, response);
 					return;
 				}
 
 				// Single mode reset flag
 				if (us.isSingle()) {
 					if (sCommand.equals("?restart")) {
 						handleStart(rt, sTestID, us, -1, request, response);
 						return;
 					}
 					if (sCommand.matches("\\?variant=[0-9]+")) {
 						handleStart(rt, sTestID, us, Integer.parseInt(sCommand
 								.substring(9)), request, response);
 						return;
 					}
 					if (sCommand.startsWith("?autofocus=")) {
 						handleStart(rt, sTestID, us, -1, request, response);
 						return;
 					}
 				}
 
 				// Beyond here, we must be logged into a test to continue
 				if (us.getTestId() == null) {
 					sendError(us, request, response,
 							HttpServletResponse.SC_FORBIDDEN, false, false,
 							sTestID, "Not logged in",
 							"You are not currently logged into the test on this server. Please "
 									+ "re-enter the test before continuing.",
 							null);
 				}
 
 				// Accessibility options form, post version when submitting
 				// answers
 				if (sCommand.equals("?access")) {
 					handleAccess(rt, bPost, false, us, request, response);
 					return;
 				}
 				// Toggle plain mode
 				if (sCommand.equals("?plainmode")) {
 					handleAccess(rt, false, true, us, request, response);
 					return;
 				}
 
 				if (!us.isSingle()) {
 					if (sCommand.startsWith("reports!")) {
 						reports.handleTestReport(us, sCommand
 								.substring("reports!".length()), request,
 								response);
 						return;
 					}
 					// Redo command is posted
 					if (sCommand.equals("?redo") && bPost) {
 						if (!us.getTestDefinition().isRedoQuestionAllowed()
 								&& !us.bAdmin)
 							sendError(
 									us,
 									request,
 									response,
 									HttpServletResponse.SC_FORBIDDEN,
 									true,
 									false,
 									null,
 									"Forbidden",
 									"This test does not permit questions to be redone.",
 									null);
 						else
 							handleRedo(rt, us, request, response);
 						return;
 					}
 					// So is restart command
 					if (sCommand.equals("?restart") && bPost) {
 						if (!us.getTestDefinition().isRedoTestAllowed()
 								&& !us.bAdmin)
 							sendError(us, request, response,
 									HttpServletResponse.SC_FORBIDDEN, true,
 									false, null, "Forbidden",
 									"This test does not permit restarting it.",
 									null);
 						else
 							handleRestart(rt, us, request, response);
 						return;
 					}
 					// Request to show 'end test, ok?' screen, post version is
 					// if you say
 					// yes
 					if (sCommand.equals("?end")) {
 						handleEnd(rt, bPost, false, us, request, response);
 						return;
 					}
 					if (sCommand.startsWith("?variant=")) {
 						handleVariant(rt, sCommand.substring("?variant="
 								.length()), us, request, response);
 						return;
 					}
 
 					// Check it's not a post request
 					if (bPost) {
 						sendError(us, request, response,
 								HttpServletResponse.SC_METHOD_NOT_ALLOWED,
 								true, false, null, "Method not allowed",
 								"You cannot post data to the specified URL.",
 								null);
 					}
 					// Check they're on current test, otherwise redirect to
 					// start
 					if (us.getTestId() == null
 							|| !sTestID.equals(us.getTestId())) {
 						sendError(
 								us,
 								request,
 								response,
 								HttpServletResponse.SC_FORBIDDEN,
 								true,
 								false,
 								null,
 								"Forbidden",
 								"Cannot access resources for non-current test.",
 								null);
 					}
 					if (sCommand.startsWith("?jump=")) {
 						try {
 							handleJump(rt, Integer.parseInt(sCommand
 									.substring("?jump=".length())), us,
 									request, response);
 						} catch (NumberFormatException nfe) {
 							sendError(us, request, response,
 									HttpServletResponse.SC_NOT_FOUND, true,
 									false, null, "Not found",
 									"Can only jump to numbered index.", null);
 						}
 						return;
 					}
 					// Request to move to next question
 					if (sCommand.equals("?next")) {
 						handleNext(rt, us, request, response);
 						return;
 					}
 					if (sCommand.equals("?summary")) {
 						if (!us.getTestDefinition().isSummaryAllowed())
 							sendError(
 									us,
 									request,
 									response,
 									HttpServletResponse.SC_FORBIDDEN,
 									true,
 									false,
 									null,
 									"Forbidden",
 									"This test does not permit summary display.",
 									null);
 						else
 							handleSummary(rt, us, request, response);
 						return;
 					}
 				}
 			}
 
 			// Resources occur un-synchronized so that they can download more
 			// than one
 			// at a time
 			String sResourcesPrefix = "resources/" + us.getTestPosition() + "/";
 			if (sCommand.equals(sResourcesPrefix + "style-" + us.iCSSIndex
 					+ ".css")) {
 				handleCSS(us, request, response);
 				return;
 			}
 			if (sCommand.startsWith(sResourcesPrefix)) {
 				handleResource(sCommand.substring((sResourcesPrefix).length()),
 						us, request, response);
 				return;
 			}
 
 			sendError(us, request, response, HttpServletResponse.SC_NOT_FOUND,
 					false, true, null, "Not found",
 					"The URL you requested is not provided by this server.",
 					null);
 		} catch (StopException se) {
 			// This just means that data was already sent to user
 		} catch (Throwable t) {
 			try {
 				sendError(us, request, response,
 						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, true,
 						false, null, "Error handling request",
 						"An error occurred while processing your request.", t);
 			} catch (StopException se) {
 				// This throws a stopexception
 			}
 		} finally {
 			String sOUCU, sPlace;
 			if (us == null) {
 				sOUCU = "?";
 				sPlace = "?";
 			} else {
 				if (us.sOUCU == null) {
 					sOUCU = "??";
 				} else {
 					sOUCU = us.sOUCU;
 				}
 				sPlace = "ind=" + us.getTestPosition() + ",seq=" + us.iDBseq;
 			}
 
 			long lTotal = System.currentTimeMillis() - rt.lStart;
 
 			l.logNormal("Request", sOUCU
 					+ ",[total="
 					+ lTotal
 					+ ",qe="
 					+ rt.lQEngineElapsed
 					+ ",db="
 					+ rt.lDatabaseElapsed
 					+ "]"
 					+ ",["
 					+ sPlace
 					+ "]"
 					+ ","
 					+ sPath
 					+ (request.getQueryString() == null ? "" : "?"
 							+ request.getQueryString()));
 		}
 	}
 
 	private void handleStart(RequestTimings rt, String sTestID, UserSession us,
 			int variant, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		initTestAttempt(rt, sTestID, us, variant, request, response);
 
 		// Redirect to system-check page if needed
 		doSystemCheck(us, request, response);
 
 		// Start first page
 		servePage(rt, us, false, request, response);
 	}
 
 	private void initTestAttempt(RequestTimings rt, String sTestID,
 			UserSession us, int variant, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		stopQuestionSession(rt, us);
 
 		// Set up the basic parts of the test session
 
 		// Random seed is normally time in milliseconds. For system testing we
 		// fix
 		// it to always be the same value.
 		initTestSession(us, rt, sTestID, request, response, false, false, us.ud
 				.isSysTest() ? 1124965882611L : System.currentTimeMillis(),
 				variant);
 
 		// Don't store anything in database for singles version
 		if (us.isSingle())
 			return;
 
 		// Store details in database
 		DatabaseAccess.Transaction dat = da.newTransaction();
 		try {
 			ResultSet rs = oq.queryMaxTestAttempt(dat, us.sOUCU, sTestID);
 			int iMaxAttempt = 0;
 			if (rs.next() && rs.getMetaData().getColumnCount() > 0)
 				iMaxAttempt = rs.getInt(1);
 
 			oq.insertTest(dat, us.sOUCU, sTestID, us.getRandomSeed(),
 					iMaxAttempt + 1, us.bAdmin,
 					// Use same PI as OUCU for non-logged-in guests
 					us.ud.isLoggedIn() ? us.ud.getPersonID() : us.sOUCU, us
 							.getFixedVariant(), us.navigatorVersion);
 			int dbTi = oq.getInsertedSequenceID(dat, "tests", "ti");
 			l.logDebug("TI = " + dbTi);
 			us.setDbTi(dbTi);
 
 			for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 				if (us.getTestLeavesInOrder()[i] instanceof TestQuestion) {
 					TestQuestion tq = (TestQuestion) us.getTestLeavesInOrder()[i];
 					oq.insertTestQuestion(dat, us.getDbTi(), tq.getNumber(), tq
 							.getID(), tq.getVersion(), tq.getSection());
 				}
 			}
 
 			storeSessionInfo(request, dat, us.getDbTi());
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 	}
 
 	/**
 	 * Should be called when a new session is started. Records the IP address.
 	 * 
 	 * @param dat
 	 *            Transaction
 	 * @param iTI
 	 *            Test index.
 	 */
 	private void storeSessionInfo(HttpServletRequest request,
 			DatabaseAccess.Transaction dat, int iTI) throws SQLException {
 		// Check IP address, allowing the firewall-provided one to win (note:
 		// this means people inside the firewall, or a system without a
 		// firewall,
 		// allows these IPs to be spoofed. But we don't rely on them for
 		// anything
 		// security-ish anyhow).
		String sIP = request.getHeader("Client-IP");
 		if (sIP == null)
 			sIP = request.getRemoteAddr();
 
 		// Browser
 		String sAgent = request.getHeader("User-Agent");
 		if (sAgent == null)
 			sAgent = "";
 		if (sAgent.length() > 255)
 			sAgent = sAgent.substring(0, 255);
 
 		// Store in database
 		oq.insertSessionInfo(dat, iTI, sIP, sAgent);
 	}
 
 	private void stopQuestionSession(RequestTimings rt, UserSession us)
 			throws RemoteException, om.axis.qengine.OmException {
 		// Stop existing question session if present
 		if (us.oss != null) {
 			us.oss.stop(rt);
 			us.oss = null;
 			us.mResources.clear();
 		}
 	}
 
 	private void handleJump(RequestTimings rt, int iIndex, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		stopQuestionSession(rt, us);
 
 		// Check & set index
 		if (iIndex < 0 || iIndex >= us.getTestLeavesInOrder().length) {
 			sendError(us, request, response, HttpServletResponse.SC_FORBIDDEN,
 					true, false, null, "Question out of range",
 					"There is no question with that index.", null);
 		}
 		if (!us.getTestLeavesInOrder()[iIndex].isAvailable()) {
 			sendError(us, request, response, HttpServletResponse.SC_FORBIDDEN,
 					true, false, null, "Unavailable question",
 					"That question is not currently available.", null);
 		}
 		setIndex(rt, us, iIndex);
 
 		// Redirect
 		response.sendRedirect(request.getRequestURL().toString().replaceAll(
 				"/[^/]*$", "/")
 				+ RequestHelpers.endOfURL(request));
 	}
 
 	private void handleSummary(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		if (returnPreProcessingResponse(us, request, response)) {
 			return;
 		}
 		Document d = XML.parse(new File(getServletContext().getRealPath(
 				"WEB-INF/templates/progresssummary.xhtml")));
 		Map<String, String> mReplace = new HashMap<String, String>();
 		mReplace.put("EXTRA", RequestHelpers.endOfURL(request));
 		XML.replaceTokens(d, mReplace);
 		Element eDiv = XML.find(d, "id", "summarytable");
 		boolean bScores = us.getTestDefinition().doesSummaryIncludeScores();
 		if (bScores) {
 			// Build the scores list from database
 			us.getTestRealisation().getScore(rt, this, da, oq);
 		}
 
 		addSummaryTableDelegate(rt, us, eDiv, RequestHelpers.inPlainMode(request), us
 				.getTestDefinition().doesSummaryIncludeQuestions(), us
 				.getTestDefinition().doesSummaryIncludeAttempts(), bScores);
 
 		serveTestContent(us, "Your answers so far", "", null, null, XML
 				.saveString(d), false, request, response, true);
 	}
 
 	/**
 	 * Here we check the configuration within the web.xml for a setting of
 	 * "useNewHandleSummaryTableImplementation". If this is set to true then we
 	 * delegate to the
 	 * 
 	 * @param rt
 	 * @param us
 	 * @param nParent
 	 * @param bPlain
 	 * @param bIncludeQuestions
 	 * @param bIncludeAttempts
 	 * @param bIncludeScore
 	 * @throws Exception
 	 * @author Trevor Hinson
 	 */
 	private void addSummaryTableDelegate(RequestTimings rt, UserSession us,
 			Node nParent, boolean bPlain, boolean bIncludeQuestions,
 			boolean bIncludeAttempts, boolean bIncludeScore) throws Exception {
 		String useNew = getServletConfig().getInitParameter(
 				USE_NEW_SUMMARY_IMPLEMENTATION);
 		if (StringUtils.isNotEmpty(useNew) ? "true".equalsIgnoreCase(useNew)
 				: false) {
 			addSummaryTableLatest(rt, us, nParent, bPlain, bIncludeQuestions,
 					bIncludeAttempts, bIncludeScore);
 		} else {
 			addSummaryTableDeprecate(rt, us, nParent, bPlain,
 					bIncludeQuestions, bIncludeAttempts, bIncludeScore);
 		}
 	}
 
 	private void addSummaryTableLatest(RequestTimings rt, UserSession us,
 			Node nParent, boolean bPlain, boolean bIncludeQuestions,
 			boolean bIncludeAttempts, boolean bIncludeScore) throws Exception {
 		SummaryDetails sd = SummaryDetailsGeneration.generateSummaryDetails(us,
 				nParent, bPlain, bIncludeQuestions, bIncludeAttempts,
 				bIncludeScore);
 		SummaryTableBuilder stb = new SummaryTableBuilder(da, oq);
 		rt.lDatabaseElapsed = stb.addSummaryTable(sd);
 	}
 
 	/**
 	 * Existing (26042011) implementation for the adding the summary table to
 	 * the end of the test. This implementation has been re-factored and remains
 	 * in place as default while testing of the new version continues.
 	 * 
 	 * @param rt
 	 * @param us
 	 * @param nParent
 	 * @param bPlain
 	 * @param bIncludeQuestions
 	 * @param bIncludeAttempts
 	 * @param bIncludeScore
 	 * @throws Exception
 	 * @Deprecated
 	 */
 	private void addSummaryTableDeprecate(RequestTimings rt, UserSession us,
 			Node nParent, boolean bPlain, boolean bIncludeQuestions,
 			boolean bIncludeAttempts, boolean bIncludeScore) throws Exception {
 		Node nTableParent, nPlainParent = null;
 		// check whether too renumber within section
 		boolean bisNumberBySection = us.getTestDefinition().isNumberBySection();
 
 		if (bPlain) {
 			Element eSkip = XML.createChild(nParent, "a");
 			XML.setText(eSkip, "Skip table");
 			eSkip.setAttribute("href", "#plaintable");
 
 			nTableParent = XML.createChild(nParent, "div");
 
 			Element eAnchor = XML.createChild(nParent, "a");
 			eAnchor.setAttribute("name", "plaintable");
 
 			XML.createText(nParent, "h3",
 					"Text-only version of preceding table");
 
 			eSkip = XML.createChild(nParent, "a");
 			XML.setText(eSkip, "Skip text-only version");
 			eSkip.setAttribute("href", "#endtable");
 
 			nPlainParent = XML.createChild(nParent, "div");
 
 			eAnchor = XML.createChild(nParent, "a");
 			eAnchor.setAttribute("name", "endtable");
 		} else {
 			nTableParent = nParent;
 		}
 		// Create basic table
 		Element eTable = XML.createChild(nTableParent, "table");
 		eTable.setAttribute("class", "topheaders");
 		Element eTR = XML.createChild(eTable, "tr");
 		Element eTH = XML.createChild(eTR, "th");
 		eTH.setAttribute("scope", "col");
 		Element eAbbr = XML.createChild(eTH, "abbr");
 		XML.createText(eAbbr, "#");
 		eAbbr.setAttribute("title", "Question number");
 
 		if (bIncludeQuestions) {
 			eTH = XML.createChild(eTR, "th");
 			eTH.setAttribute("scope", "col");
 			XML.createText(eTH, "Question");
 			eTH = XML.createChild(eTR, "th");
 			eTH.setAttribute("scope", "col");
 			XML.createText(eTH, "Your answer");
 		}
 		if (bIncludeAttempts) {
 			eTH = XML.createChild(eTR, "th");
 			eTH.setAttribute("scope", "col");
 			XML.createText(eTH, "Result");
 		}
 		String[] asAxes = null;
 		if (bIncludeScore) {
 			CombinedScore ps = us.getRootTestGroup().getFinalScore();
 			asAxes = ps.getAxesOrdered();
 			for (int iAxis = 0; iAxis < asAxes.length; iAxis++) {
 				String sAxis = asAxes[iAxis];
 				eTH = XML.createChild(eTR, "th");
 				eTH.setAttribute("scope", "col");
 				XML.createText(eTH, "Marks"
 						+ (sAxis == null ? "" : " (" + sAxis + ")"));
 				eTH = XML.createChild(eTR, "th");
 				eTH.setAttribute("scope", "col");
 				XML.createText(eTH, "Out of");
 			}
 		}
 
 		// Query for questions and answers
 		DatabaseAccess.Transaction dat = da.newTransaction();
 		try {
 			ResultSet rs = oq.querySummary(dat, us.getDbTi());
 
 			// Build table
 			int iCurrentQuestion = 1;
 			int iOutputCurrentQuestionNumber = 0;
 			int iMaxQuestion = 0;
 			String sLastQuestion = null;
 			String sDisplayedSection = null; // Section heading that has already
 			// been displayed
 			String sPreviousSection = null; // Section of last row (null if
 			// none)
 
 			while (rs.next()) {
 				// Keep track of max number
 				int iQuestionNumber = rs.getInt(1);
 				iMaxQuestion = Math.max(iMaxQuestion, iQuestionNumber);
 
 				// Ignore answers after we're looking for next question
 				if (iQuestionNumber < iCurrentQuestion)
 					continue;
 
 				// Get section
 				String sSection = rs.getString(7);
 				boolean checkForNumberingRestart = bisNumberBySection
 						&& !(sPreviousSection == null || sPreviousSection
 								.equals(sDisplayedSection));
 				// check if we need to restart the numbering of the questions
 				// if (bisNumberBySection && !(sPreviousSection==null ||
 				// sPreviousSection.equals(sDisplayedSection)))
 				if (checkForNumberingRestart) {
 					iOutputCurrentQuestionNumber = 0;
 				}
 				if (iQuestionNumber > iCurrentQuestion) {
 					// If we didn't put out an answer for current question,
 					// chuck one out now
 					iOutputCurrentQuestionNumber++;
 
 					sDisplayedSection = addSectionRow(bPlain,
 							bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 							nPlainParent, eTable, asAxes, sPreviousSection,
 							sDisplayedSection);
 					addUnfinishedRow(us, bPlain, bIncludeQuestions,
 							bIncludeAttempts, bIncludeScore, nPlainParent,
 							eTable, iCurrentQuestion,
 							iOutputCurrentQuestionNumber, sLastQuestion);
 
 					iCurrentQuestion++;
 
 					// This only works because there always will be at least one
 					// line per
 					// question thanks to the LEFT JOIN
 				}
 
 				sLastQuestion = rs.getString(5);
 
 				// Ignore unfinished attempts, wait for a finished one
 				if (rs.getInt(2) != 0) {
 					// Woo! We have an answer
 
 					iOutputCurrentQuestionNumber++;
 
 					sDisplayedSection = addSectionRow(bPlain,
 							bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 							nPlainParent, eTable, asAxes, sSection,
 							sDisplayedSection);
 
 					if (bisNumberBySection && !(sPreviousSection == null)
 							&& !(sPreviousSection.equals(sDisplayedSection))) {
 						iOutputCurrentQuestionNumber = 1;
 					}
 					eTR = XML.createChild(eTable, "tr");
 					eTR.setAttribute("class", "answered");
 					XML
 							.createText(eTR, "td", ""
 									+ iOutputCurrentQuestionNumber);
 					Element ePlainRow = null;
 					if (bPlain) {
 						ePlainRow = XML.createChild(nPlainParent, "div");
 						XML.createText(ePlainRow, "Question "
 								+ iOutputCurrentQuestionNumber + ". ");
 					}
 
 					if (bIncludeQuestions) {
 						String sQ = rs.getString(3), sA = rs.getString(4);
 						String socqn = Integer
 								.toString(iOutputCurrentQuestionNumber);
 						// XML.createText(eTR,"td",sQ);
 						XML.createText(eTR, "td", socqn);
 						XML.createText(eTR, "td", sA);
 						if (bPlain) {
 							XML.createText(ePlainRow, "Question: " + sQ
 									+ ". Your answer: " + sA + ". ");
 						}
 					}
 					if (bIncludeAttempts) {
 						String sAttempts = getAttemptsString(rs.getInt(6), us
 								.getTestDefinition());
 						XML.createText(eTR, "td", sAttempts);
 						if (bPlain) {
 							XML.createText(ePlainRow, "Result: " + sAttempts
 									+ ". ");
 						}
 					}
 					if (bIncludeScore)
 						addSummaryTableScore(eTR, ePlainRow, us, sLastQuestion);
 
 					// Start looking for next question now
 					iCurrentQuestion++;
 				}
 
 				sPreviousSection = sSection;
 			}
 
 			// If we didn't do the last one, put that out
 			if (iCurrentQuestion <= iMaxQuestion) {
 				iOutputCurrentQuestionNumber++;
 				// check if we need to restart the numbering of the questions
 				if (bisNumberBySection && !(sPreviousSection == null)
 						&& !sPreviousSection.equals(sDisplayedSection))
 
 				{
 					iOutputCurrentQuestionNumber = 1;
 				}
 				sDisplayedSection = addSectionRow(bPlain, bIncludeQuestions,
 						bIncludeAttempts, bIncludeScore, nPlainParent, eTable,
 						asAxes, sPreviousSection, sDisplayedSection);
 
 				addUnfinishedRow(us, bPlain, bIncludeQuestions,
 						bIncludeAttempts, bIncludeScore, nPlainParent, eTable,
 						iCurrentQuestion, iOutputCurrentQuestionNumber,
 						sLastQuestion);
 			}
 
 			if (bIncludeScore) {
 				CombinedScore ps = us.getRootTestGroup().getFinalScore();
 				eTR = XML.createChild(eTable, "tr");
 				eTR.setAttribute("class", "totals");
 				Element eTD = XML.createChild(eTR, "td");
 				eTD
 						.setAttribute(
 								"colspan",
 								""
 										+ (1 + (bIncludeQuestions ? 2 : 0) + (bIncludeAttempts ? 1
 												: 0)));
 				XML.createText(eTD, "Total");
 				Element ePlainRow = null;
 				if (bPlain) {
 					ePlainRow = XML.createChild(nPlainParent, "div");
 					XML.createText(ePlainRow, "Totals: ");
 				}
 
 				for (int iAxis = 0; iAxis < asAxes.length; iAxis++) {
 					String sAxis = asAxes[iAxis];
 					String sScore = displayScore(ps.getScore(sAxis)), sMax = displayScore(ps
 							.getMax(sAxis));
 					XML.createText(eTR, "td", sScore);
 					XML.createText(eTR, "td", sMax);
 					if (ePlainRow != null) {
 						XML.createText(ePlainRow, "Marks"
 								+ (sAxis == null ? "" : " (" + sAxis + ")")
 								+ ": " + sScore + ". Out of: " + sMax + ". ");
 					}
 				}
 			}
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 	}
 
 	private String addSectionRow(boolean bPlain, boolean bIncludeQuestions,
 			boolean bIncludeAttempts, boolean bIncludeScore, Node nPlainParent,
 			Element eTable, String[] asAxes, String sSection,
 			String sCurrentSection) {
 		// Don't output anything if there has been no change to the current
 		// section
 		if (sSection == null || sSection.equals(sCurrentSection))
 			return sCurrentSection;
 
 		Element eTR = XML.createChild(eTable, "tr");
 		eTR.setAttribute("class", "sectionname");
 		Element eTD = XML.createChild(eTR, "td");
 		eTD
 				.setAttribute(
 						"colspan",
 						""
 								+ (1 + (bIncludeQuestions ? 2 : 0)
 										+ (bIncludeAttempts ? 1 : 0) + (bIncludeScore ? asAxes.length * 2
 										: 0)));
 		XML.createText(eTD, sSection);
 
 		if (bPlain)
 			XML.createText(nPlainParent, "h4", sSection);
 
 		return sSection;
 	}
 
 	private void addUnfinishedRow(UserSession us, boolean bPlain,
 			boolean bIncludeQuestions, boolean bIncludeAttempts,
 			boolean bIncludeScore, Node nPlainParent, Element eTable,
 			int iCurrentQuestion, int iOutputCurrentQuestionNumber,
 			String sLastQuestion) throws OmFormatException {
 		Element eTR = XML.createChild(eTable, "tr");
 		Element ePlainRow = null;
 		eTR.setAttribute("class", "unanswered");
 		XML.createText(eTR, "td", "" + iOutputCurrentQuestionNumber);
 		if (bPlain) {
 			ePlainRow = XML.createChild(nPlainParent, "div");
 			XML.createText(ePlainRow, "Question "
 					+ iOutputCurrentQuestionNumber + ". ");
 		}
 
 		if (bIncludeQuestions) {
 			XML.createText(eTR, "td", SUMMARYTABLE_NOTANSWERED);
 			XML.createChild(eTR, "td");
 		}
 		if (bIncludeAttempts) {
 			if (bIncludeQuestions)
 				XML.createChild(eTR, "td");
 			else
 				XML.createText(eTR, "td", SUMMARYTABLE_NOTANSWERED);
 		}
 		if (bPlain && (bIncludeAttempts || bIncludeQuestions)) {
 			XML.createText(ePlainRow, SUMMARYTABLE_NOTANSWERED + ". ");
 		}
 
 		if (bIncludeScore)
 			addSummaryTableScore(eTR, ePlainRow, us, sLastQuestion);
 	}
 
 	private void addSummaryTableScore(Element eTR, Element ePlainRow,
 			UserSession us, String sQuestion) throws OmFormatException {
 		// Find question
 		TestQuestion tq = null;
 		for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 			if (us.getTestLeavesInOrder()[i] instanceof TestQuestion
 					&& ((TestQuestion) us.getTestLeavesInOrder()[i]).getID()
 							.equals(sQuestion)) {
 				tq = (TestQuestion) us.getTestLeavesInOrder()[i];
 				break;
 			}
 		}
 
 		// Get score (scaled)
 		CombinedScore ps = tq.getScoreContribution(us.getRootTestGroup());
 		String[] asAxes = ps.getAxesOrdered();
 		for (int iAxis = 0; iAxis < asAxes.length; iAxis++) {
 			String sAxis = asAxes[iAxis];
 			String sScore = displayScore(ps.getScore(sAxis)), sMax = displayScore(ps
 					.getMax(sAxis));
 
 			XML.createText(eTR, "td", sScore);
 			XML.createText(eTR, "td", sMax);
 
 			if (ePlainRow != null) {
 				XML.createText(ePlainRow, "Marks"
 						+ (sAxis == null ? "" : " (" + sAxis + ")") + ": "
 						+ sScore + ". Out of: " + sMax + ". ");
 			}
 		}
 	}
 
 	private static String displayScore(double dScore) {
 		if (Math.abs(dScore - Math.round(dScore)) < 0.001) {
 			return (int) Math.round(dScore) + "";
 		} else {
 			NumberFormat nf = DecimalFormat.getNumberInstance();
 			nf.setMaximumFractionDigits(1);
 			return nf.format(dScore);
 		}
 	}
 
 	private void handleNext(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		stopQuestionSession(rt, us);
 
 		// Get next question that's permitted
 		if (!findNextQuestion(rt, us, false, !us.getTestDefinition()
 				.isRedoQuestionAllowed(), !us.getTestDefinition()
 				.isRedoQuestionAllowed()))
 			// If there are none left, go to end page
 			redirectToEnd(request, response);
 		else
 			// Redirect
 			redirectToTest(request, response);
 	}
 
 	private void redirectToEnd(HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		response.sendRedirect(request.getRequestURL().toString().replaceAll(
 				"/[^/]*$", "/")
 				+ "?end");
 	}
 
 	private void handleRedo(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		// Start new attempt at question
 		initQuestionAttempt(rt, us);
 
 		// Redirect back to question page
 		response.sendRedirect(request.getRequestURL().toString().replaceAll(
 				"/[^/]*$", "/")
 				+ RequestHelpers.endOfURL(request));
 	}
 
 	private void handleRestart(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		// Start new attempt at question
 		initTestAttempt(rt, us.getTestId(), us, -1, request, response);
 
 		// Redirect back to question page
 		response.sendRedirect(request.getRequestURL().toString().replaceAll(
 				"/[^/]*$", "/")
 				+ RequestHelpers.endOfURL(request));
 	}
 
 	/**
 	 * Returns appropriate version of question to use.
 	 * @param sQuestionID  Question ID
 	 * @param iRequiredVersion
 	 *            Desired version or TestQuestion.VERSION_UNSPECIFIED
 	 * @return Appropriate version
 	 * @throws OmException
 	 */
 	public QuestionVersion getLatestVersion(String sQuestionID,
 		int iRequiredVersion) throws OmException {
 		// This should use a proper question bank at some point
 		QuestionVersion qv = new QuestionVersion();
 		File[] af = IO.listFiles(questionBankFolder);
 		boolean bFound = VersionUtil.findLatestVersion(sQuestionID, iRequiredVersion, qv, af);
 		if (!bFound) {
 			throw new OmException(
 				"Question file missing: " + sQuestionID
 					+ (iRequiredVersion != VersionUtil.VERSION_UNSPECIFIED ? " "
 						+ iRequiredVersion : ""));
 		}
 		return qv;
 	}
 
 	private void applySessionChanges(UserSession us, ProcessReturn pr) {
 		// Add resources
 		Resource[] arResources = pr.getResources();
 		for (int i = 0; i < arResources.length; i++) {
 			us.mResources.put(arResources[i].getFilename(), arResources[i]);
 		}
 
 		// Set style
 		if (pr.getCSS() != null) {
 			us.sCSS = pr.getCSS();
 			us.iCSSIndex++;
 		}
 
 		// Set progress info
 		if (pr.getProgressInfo() != null)
 			us.sProgressInfo = pr.getProgressInfo();
 	}
 
 	private StartReturn startQuestion(RequestTimings rt,
 			HttpServletRequest request, UserSession us, TestQuestion tq,
 			int iAttempt, int iMajor, int iMinor) throws Exception {
 		// Question URL
 		String sQuestionBase = getQuestionBase();
 
 		// Determine version if not specified
 		QuestionVersion qv;
 		if (iMajor == 0) {
 			// Major=0 in two case: first, this is a fresh start not a replay;
 			// or second, the database has become somehow corrupted before we
 			// have actually
 			// set up the question versions in nav_questions (they default to
 			// 0).
 			qv = getLatestVersion(tq.getID(), tq.getVersion());
 			if (!us.isSingle()) {
 				if (us.iDBqi == 0 || qv.iMajor == 0)
 					throw new OmUnexpectedException(
 							"Unexpected data setting question versions ("
 									+ us.iDBqi + "): " + qv.iMajor);
 				DatabaseAccess.Transaction dat = da.newTransaction();
 				try {
 					oq.updateSetQuestionVersion(dat, us.iDBqi, qv.iMajor,
 							qv.iMinor);
 				} finally {
 					rt.lDatabaseElapsed += dat.finish();
 				}
 			}
 		} else {
 			qv = new QuestionVersion();
 			qv.iMajor = iMajor;
 			qv.iMinor = iMinor;
 		}
 
 		// Question parameters
 		NameValuePairs p = new NameValuePairs();
 
 		long randomSeed = us.getRandomSeed();
 		p.add("randomseed", Long.toString(randomSeed));
 		p.add("attempt", Integer.toString(iAttempt));
 		p.add("navigatorVersion", us.navigatorVersion);
 
 		String sAccess = RequestHelpers.getAccessibilityCookie(request);
 		if (sAccess.indexOf("[plain]") != -1)
 			p.add("plain", "yes");
 		Matcher m = ZOOMPATTERN.matcher(sAccess);
 		if (m.matches()) {
 			// This check avoids a potential security issue where somebody could
 			// crash it or use all memory by choosing a silly zoom value in the
 			// cookie
 			if (m.group(1).equals("1.5") || m.group(1).equals("2.0"))
 				p.add("zoom", m.group(1));
 		}
 		m = RequestHelpers.COLOURSPATTERN.matcher(sAccess);
 		if (m.matches()) {
 			String sColours = m.group(1);
 			if (sColours.matches("[0-9a-f]{12}")) {
 				p.add("fixedfg", "#" + sColours.substring(0, 6));
 				p.add("fixedbg", "#" + sColours.substring(6, 12));
 			}
 		}
 		if (us.getFixedVariant() > -1)
 			p.add("fixedvariant", us.getFixedVariant() + "");
 
 		l.logDebug("Starting a session on question " + tq.getID() + " version "
 				+ qv + " with random seed " + randomSeed + ".");
 
 		// Start question
 		us.oss = osb.start(rt, tq.getID(), qv.toString(), sQuestionBase, p
 				.getNames(), p.getValues(), new String[0]);
 		StartReturn sr = us.oss.eatStartReturn();
 
 		// Set question session
 		us.iDBseq = 1;
 
 		// Add resources
 		Resource[] arResources = sr.getResources();
 		;
 		for (int i = 0; i < arResources.length; i++) {
 			us.mResources.put(arResources[i].getFilename(), arResources[i]);
 		}
 
 		// Set style
 		us.sCSS = sr.getCSS();
 		us.iCSSIndex = Math.abs(p.hashCode()); // Start with a value that
 		// depends on the params
 		if (sr.getProgressInfo() != null)
 			us.sProgressInfo = sr.getProgressInfo();
 
 		return sr;
 	}
 
 	private String getQuestionBase() {
 		return nc.getThisTN() + "!question";
 	}
 
 	/**
 	 * Serves the final page - either information about their results, or
 	 * telling them that they aren't allowed to see it yet! Also includes option
 	 * to re-take test if permitted.
 	 * 
 	 * @param rt
 	 *            Timings
 	 * @param us
 	 *            Session
 	 * @param request
 	 *            HTTP request
 	 * @param response
 	 *            HTTP response
 	 * @throws Exception
 	 *             If anything goes wrong
 	 */
 	private void serveFinalPage(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		// Create document to hold results
 		Document dTemp = XML.createDocument();
 		Element eMain = XML.createChild(dTemp, "div");
 		eMain.setAttribute("class", "basicpage");
 
 		// If an email should've been sent, say so.
 		if (us.iEmailSent == 1) {
 			Element eMsg = XML.createChild(eMain, "p");
 			eMsg.setAttribute("class", "email");
 			XML
 					.createText(
 							eMsg,
 							"Your answers to this test have been submitted, "
 									+ "and an email receipt will be sent to your OU-registered "
 									+ "email address within a few hours.");
 		} else if (us.iEmailSent == -1) {
 			Element eMsg = XML.createChild(eMain, "p");
 			eMsg.setAttribute("class", "email");
 			XML
 					.createText(
 							eMsg,
 							"Your answers to this test have been submitted. "
 									+ "We were unable to send an email receipt but please rest assured "
 									+ "that your answers have been correctly stored.");
 		}
 
 		// See if user's allowed to see results yet
 		boolean bAfterFeedback = us.getTestDeployment().isAfterFeedback();
 		if (us.bAdmin || bAfterFeedback) {
 			if (!bAfterFeedback) {
 				Element eInfo = XML.createChild(eMain, "div");
 				eInfo.setAttribute("class", "adminmsg");
 				XML
 						.createText(
 								eInfo,
 								"Results are not yet visible to students. Students will currently see "
 										+ "only a message telling them that results are unavailable at present "
 										+ "and the date on which they will be able to see results, which is "
 										+ us.getTestDeployment()
 												.displayFeedbackDate() + ".");
 			}
 
 			// OK, work out the student's score
 			CombinedScore ps = us.getTestRealisation().getScore(rt, this, da,
 					oq);
 
 			// Now process each element from the final part of the definition
 			processFinalTags(rt, us, us.getTestDefinition().getFinalPage(),
 					eMain, ps, request);
 		} else {
 			XML.createText(eMain, "p", "Thank you for completing this test.");
 			XML
 					.createText(
 							eMain,
 							"p",
 							"Feedback on your results is not available "
 									+ "immediately. You will be able to see information about your results "
 									+ "if you return here after "
 									+ us.getTestDeployment()
 											.displayFeedbackDate() + ".");
 		}
 
 		// Show restart button, if enabled
 		if ((us.getTestDefinition().isRedoTestAllowed() && !us
 				.getTestDeployment().isAfterForbid())
 				|| us.bAdmin) {
 			if (!us.getTestDefinition().isRedoTestAllowed()) {
 				Element eMsg = XML.createChild(eMain, "div");
 				eMsg.setAttribute("class", "adminmsg");
 				XML
 						.createText(
 								eMsg,
 								"You are only permitted to restart the "
 										+ "test because you have admin privileges. Students will not see "
 										+ "the following button.");
 			}
 
 			Element eForm = XML.createChild(eMain, "form");
 			eForm.setAttribute("action", "?restart");
 			eForm.setAttribute("method", "post");
 			Element eInput;
 			eInput = XML.createChild(eForm, "input");
 			eInput.setAttribute("type", "submit");
 			eInput.setAttribute("value", "Restart entire test");
 		}
 
 		serveTestContent(us, "Results for "
 				+ (us.ud.isLoggedIn() ? us.ud.getPersonID() : "guest"), "",
 				null, null, XML.saveString(dTemp), false, request, response,
 				true);
 	}
 
 	/**
 	 * Do everything possible to the response to stop the browser's back button
 	 * from working.
 	 * 
 	 * @request
 	 * @param response
 	 *            the response to add headers too.
 	 */
 	public void breakBack(HttpServletRequest request,
 			HttpServletResponse response) {
 		response.setHeader("Pragma", "No-cache");
 		response.setHeader("Cache-Control", "no-store");
 		response.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
 		if (XHTML.isIE9(request)) {
 			response.addHeader("X-UA-Compatible", "IE=8");
 		}
 	}
 
 	private void processFinalTags(RequestTimings rt, UserSession us,
 			Element eParent, Element eTarget, CombinedScore ps,
 			HttpServletRequest request) throws Exception {
 		Element[] ae = XML.getChildren(eParent);
 		for (int i = 0; i < ae.length; i++) {
 			Element e = ae[i];
 			String sTag = e.getTagName();
 			if (sTag.equals("p")) {
 				// Copy it
 				eTarget.appendChild(eTarget.getOwnerDocument().importNode(e,
 						true));
 			} else if (sTag.equals("scores")) {
 				// Make container
 				Element eUL = XML.createChild(eTarget, "ul");
 				eUL.setAttribute("class", "scores");
 
 				// Get axis labels
 				Element[] aeLabels = XML.getChildren(e, "axislabel");
 
 				// Loop through each score axis
 				String[] asAxes = ps.getAxesOrdered();
 				axisloop: for (int iAxis = 0; iAxis < asAxes.length; iAxis++) {
 					String sAxis = asAxes[iAxis];
 
 					// Get default label - use the axis name, capitalised,
 					// followed by
 					// colon
 					String sLabel = (sAxis == null) ? "" : sAxis
 							.substring(0, 1).toUpperCase()
 							+ sAxis.substring(1) + ":";
 
 					// Obtain axislabel if one is provided
 					for (int iLabel = 0; iLabel < aeLabels.length; iLabel++) {
 						Element eLabel = aeLabels[iLabel];
 						if ((sAxis == null && !eLabel.hasAttribute("axis"))
 								|| (sAxis != null && sAxis.equals(eLabel
 										.getAttribute("axis")))) {
 							// If hide=yes, skip this axis entirely
 							if ("yes".equals(eLabel.getAttribute("hide")))
 								continue axisloop;
 
 							// Otherwise just update label
 							sLabel = XML.getText(eLabel);
 						}
 					}
 
 					// Make LI and put the label in it
 					Element eLI = XML.createChild(eUL, "li");
 					XML.createText(eLI, " " + sLabel + " ");
 
 					// Add actual scores
 					if ("yes".equals(e.getAttribute("marks"))) {
 						int iScore = (int) Math.round(ps.getScore(sAxis)), iMax = (int) Math
 								.round(ps.getMax(sAxis));
 
 						Element eSpan = XML.createChild(eLI, "span");
 						eSpan.setAttribute("class", "marks");
 						XML.createText(eSpan, "" + iScore);
 						XML.createText(eLI, " (out of ");
 						eSpan = XML.createChild(eLI, "span");
 						eSpan.setAttribute("class", "outof");
 						XML.createText(eSpan, "" + iMax);
 						XML.createText(eLI, ") ");
 					}
 					if ("yes".equals(e.getAttribute("percentage"))) {
 						int iPercentage = (int) Math.round(100.0
 								* ps.getScore(sAxis) / ps.getMax(sAxis));
 						Element eSpan = XML.createChild(eLI, "span");
 						eSpan.setAttribute("class", "percentage");
 						XML.createText(eSpan, iPercentage + "%");
 					}
 				}
 			} else if (sTag.equals("conditional")) {
 				// Find marks on the specified axis
 				String sAxis = e.hasAttribute("axis") ? e.getAttribute("axis")
 						: null;
 				String sOn = e.getAttribute("on");
 				int iCompare;
 				if (sOn.equals("marks")) {
 					iCompare = (int) Math.round(ps.getScore(sAxis));
 				} else if (sOn.equals("percentage")) {
 					iCompare = (int) Math.round(ps.getScore(sAxis) * 100.0
 							/ ps.getMax(sAxis));
 				} else
 					throw new OmFormatException(
 							"Unexpected on= for conditional: " + sOn);
 
 				boolean bOK = true;
 				try {
 					if (e.hasAttribute("gt")) {
 						if (!(iCompare > Integer.parseInt(e.getAttribute("gt"))))
 							bOK = false;
 					}
 					if (e.hasAttribute("gte")) {
 						if (!(iCompare >= Integer.parseInt(e
 								.getAttribute("gte"))))
 							bOK = false;
 					}
 					if (e.hasAttribute("e")) {
 						if (!(iCompare == Integer.parseInt(e.getAttribute("e"))))
 							bOK = false;
 					}
 					if (e.hasAttribute("lte")) {
 						if (!(iCompare <= Integer.parseInt(e
 								.getAttribute("lte"))))
 							bOK = false;
 					}
 					if (e.hasAttribute("lt")) {
 						if (!(iCompare < Integer.parseInt(e.getAttribute("lt"))))
 							bOK = false;
 					}
 					if (e.hasAttribute("ne")) {
 						if (!(iCompare != Integer
 								.parseInt(e.getAttribute("ne"))))
 							bOK = false;
 					}
 				} catch (NumberFormatException nfe) {
 					throw new OmFormatException(
 							"Not valid integer in <conditional>");
 				}
 				if (bOK) // Passed the conditional! Process everything within it
 				{
 					processFinalTags(rt, us, e, eTarget, ps, request);
 				}
 			} else if (sTag.equals("summary")) {
 				addSummaryTableDelegate(rt, us, eTarget, RequestHelpers.inPlainMode(request),
 						"yes".equals(e.getAttribute("questions")), "yes"
 								.equals(e.getAttribute("attempts")), "yes"
 								.equals(e.getAttribute("marks")));
 			}
 		}
 	}
 
 	/**
 	 * @param rt
 	 * @param sID
 	 * @param sVersion
 	 * @return something to do with scores.
 	 * @throws IOException
 	 * @throws RemoteException
 	 */
 	public Score[] getMaximumScores(RequestTimings rt, String sID,
 			String sVersion) throws IOException, RemoteException {
 		Element eMetadata = getQuestionMetadata(rt, sID, sVersion);
 		if (XML.hasChild(eMetadata, "scoring")) {
 			try {
 				Element[] aeMarks = XML.getChildren(XML.getChild(eMetadata,
 						"scoring"), "marks");
 
 				Score[] as = new Score[aeMarks.length];
 				for (int i = 0; i < aeMarks.length; i++) {
 					String sAxis = aeMarks[i].hasAttribute("axis") ? aeMarks[i]
 							.getAttribute("axis") : null;
 					as[i] = new Score(sAxis, Integer.parseInt(XML
 							.getText(aeMarks[i])));
 				}
 
 				return as;
 			} catch (NumberFormatException nfe) {
 				throw new IOException(
 						"Question metadata error: Invalid <marks> in <scoring>: not an integer");
 			} catch (XMLException e) {
 				// Can't happen as we just checked hasChild
 				throw new OmUnexpectedException(e);
 			}
 		} else {
 			return new Score[] {};
 		}
 	}
 
 	/**
 	 * Obtains metadata for a question, from the cache or by requesting it from
 	 * the question engine.
 	 * 
 	 * @param rt
 	 *            Timings
 	 * @param sID
 	 *            ID of question
 	 * @param sVersion
 	 *            Version of question
 	 * @return Metadata document
 	 * @throws RemoteException
 	 *             If service has a problem
 	 * @throws IOException
 	 *             If there's an I/O problem before/after the remote service
 	 */
 	Element getQuestionMetadata(RequestTimings rt, String sID, String sVersion)
 			throws RemoteException, IOException {
 		String sKey = sID + "\n" + sVersion;
 		Element e;
 		synchronized (questionMetadata) {
 			e = questionMetadata.get(sKey);
 			if (e != null)
 				return e;
 		}
 
 		e = XML.parse(
 				osb.getQuestionMetadata(rt, sID, sVersion, getQuestionBase()))
 				.getDocumentElement();
 
 		synchronized (questionMetadata) {
 			questionMetadata.put(sKey, e);
 		}
 
 		return e;
 	}
 
 	private boolean worksInPlainMode(RequestTimings rt, TestQuestion tq,
 			HttpServletRequest request) throws RemoteException, IOException,
 			OmException {
 		Element eMetadata = getQuestionMetadata(rt, tq.getID(),
 				getLatestVersion(tq.getID(), tq.getVersion()).toString());
 		return "yes".equals(XML.getText(eMetadata, "plainmode"));
 	}
 
 	/**
 	 * Here we delegate a request to the configured PreProcessorRequestHandler
 	 * to determine what to do with a UserSession prior to carrying out the
 	 * normal processing.
 	 * 
 	 * @param session
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	private RequestResponse preProcess(UserSession session,
 			HttpServletRequest request, HttpServletResponse response)
 			throws RequestHandlingException {
 		RequestResponse rr = null;
 		PreProcessingRequestHandler pre = getPreProcessingRequestHandler();
 		if (null != pre) {
 			RequestAssociates associates = pre
 				.generateRequiredRequestAssociates(this, request, response,
 				session);
 			rr = pre.handle(request, response, associates);
 		}
 		return rr;
 	}
 
 	/**
 	 * .
 	 * @param us
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws RequestHandlingException
 	 * @throws IOException
 	 * @author Trevor Hinson
 	 */
 	private boolean returnPreProcessingResponse(UserSession us,
 		HttpServletRequest request, HttpServletResponse response)
 		throws RequestHandlingException, IOException {
 		boolean shouldDisplay = false;
 		RequestResponse rr = preProcess(us, request, response);
 		if (null != rr ? !rr.isSuccessful() : false) {
 			if (rr instanceof RenderedOutput) {
 				Object res = ((RenderedOutput) rr).getResponse();
 				if (res instanceof Document) {
 					breakBack(request, response);
 					XHTML.output((Document) res, request, response, "en");
 					shouldDisplay = true;
 				}
 			}
 		}
 		return shouldDisplay;
 	}
 
 	private void servePage(RequestTimings rt, UserSession us,
 			boolean bOnRestart, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		stopQuestionSession(rt, us);
 		if (returnPreProcessingResponse(us, request, response)) {
 			return;
 		}
 		if (us.isFinished()) {
 			serveFinalPage(rt, us, request, response);
 			return;
 		}
 
 		// OK, let's see where they are
 		TestLeaf tl = us.getTestLeavesInOrder()[us.getTestPosition()];
 
 		if (tl instanceof TestQuestion) {
 			boolean bNotReallyQuestion = false;
 
 			TestQuestion tq = (TestQuestion) tl;
 
 			if (RequestHelpers.inPlainMode(request) && !worksInPlainMode(rt, tq, request)) {
 				tq.setDone(true);
 
 				serveTestContent(
 						us,
 						"Question cannot be attempted in plain mode",
 						"",
 						null,
 						null,
 						"<div class='basicpage'>"
 								+ "<p>This question cannot be attempted in plain mode, as it uses "
 								+ "some features which can't be converted to text. "
 								+ (us.isSingle() ? "</p>"
 										: ("If you need to use "
 												+ "plain mode, skip this question and continue to the next.</p>"
 												+ (us.getTestDeployment()
 														.getType() == TestDeployment.TYPE_NOTASSESSED ? ""
 														: "<p>Be certain to <em>inform "
 																+ "the course team</em> (via your tutor) that you needed to use plain mode, "
 																+ "and for that reason could not complete all questions. They can make "
 																+ "appropriate adjustments to the marking. "
 																+ "The system does not handle this automatically.</p>") + "<p><a href='?next'>Next</a></p>"))
 								+ "</div>", true, request, response, true);
 			} else {
 				// Look for existing instance of question in database to see if
 				// we
 				// should shove through some process actions
 				String sXHTML = null;
 				if (!us.isSingle()) {
 					DatabaseAccess.Transaction dat = da.newTransaction();
 					try {
 						// Find actions from the latest attempt on this question
 						// in this test.
 						// This returns qi and the maximum sequence number. If
 						// the latest
 						// attempt wasn't unfinished then it returns 0.
 						ResultSet rs = oq.queryQuestionActions(dat, us
 								.getDbTi(), tq.getID());
 
 						// If there was a previous attempt that wasn't finished
 						// then it's time to resurrect the question!
 						if (rs.next()) {
 							// Immediately on a test restart, we show the 'final
 							// screen' of a
 							// question if you restart it, even though we don't
 							// do that
 							// normally.
 							boolean bFinished = (bOnRestart ? rs.getInt(3) >= 2
 									: rs.getInt(3) >= 1);
 
 							if (bFinished) {
 								int iQI = rs.getInt(1);
 
 								if (us.getTestDefinition()
 										.isAutomaticRedoQuestionAllowed()) {
 									handleRedo(rt, us, request, response);
 									throw new StopException();
 								}
 
 								sXHTML = showCompletedQuestion(rt, us, request,
 										iQI);
 								bNotReallyQuestion = true;
 							} else {
 								us.iDBqi = rs.getInt(1); // Required in
 								// startQuestion
 								int iMaxSeq = rs.getInt(2); // May be null, but
 								// that's ok 'cause
 								// it returns 0
 
 								StartReturn sr = startQuestion(rt, request, us,
 										tq, rs.getInt(4), rs.getInt(5), rs
 												.getInt(6));
 								sXHTML = sr.getXHTML();
 
 								// Get list of parameters
 								rs = oq
 										.queryQuestionActionParams(dat,
 												us.iDBqi);
 
 								NameValuePairs[] ap = new NameValuePairs[iMaxSeq + 1];
 								boolean bValid = rs.next();
 								for (int iSeq = 1; iSeq <= iMaxSeq; iSeq++) {
 									// Build parameter list
 									ap[iSeq] = new NameValuePairs();
 									while (bValid && rs.getInt(1) == iSeq) {
 										ap[iSeq].add(rs.getString(2), rs
 												.getString(3));
 										bValid = rs.next();
 									}
 								}
 								rt.lDatabaseElapsed += dat.finish();
 
 								// Loop around each sequenced event
 								for (int iSeq = 1; iSeq <= iMaxSeq; iSeq++) {
 									// Do process
 									ProcessReturn pr = us.oss.process(rt,
 											ap[iSeq].getNames(), ap[iSeq]
 													.getValues());
 
 									if (pr.isQuestionEnd())
 										throw new OmException(
 												"Unepected end of question in replay attempt");
 
 									// Apply changes to session
 									applySessionChanges(us, pr);
 
 									// Note: Any results that are sent are
 									// ignored, as presumably
 									// we already stored them.
 
 									// Get XHTML from last one
 									if (iSeq == iMaxSeq) {
 										sXHTML = pr.getXHTML();
 									}
 								}
 								us.iDBseq = iMaxSeq + 1;
 							}
 						}
 					} finally {
 						rt.lDatabaseElapsed += dat.finish();
 					}
 				}
 
 				// If there was no previous question, start question afresh
 				if (sXHTML == null) {
 					int iAttempt = initQuestionAttempt(rt, us);
 					StartReturn sr = startQuestion(rt, request, us, tq,
 							iAttempt, 0, 0);
 					sXHTML = sr.getXHTML();
 				}
 
 				// Serve XHTML
 				serveQuestionPage(rt, us, tq, sXHTML, bNotReallyQuestion,
 						request, response);
 			}
 		} else if (tl instanceof TestInfo) {
 			TestInfo ti = (TestInfo) tl;
 			boolean bNav = true;
 			if (us.getTestPosition() == 0) {
 				if (!ti.isDone())
 					bNav = false;
 				String sMessage = ti.getXHTMLString();
 				if (us.getTestDeployment().isAfterClose()) {
 					// Show message only if it's not after forbid - if it's
 					// after forbid
 					// students can't see it anyway and the message confuses
 					// admin.
 					if (!us.getTestDeployment().isAfterForbid()) {
 						sMessage += "<p><em class='warning'>This test closed on "
 								+ us.getTestDeployment().displayCloseDate()
 								+ "</em>. You can still take "
 								+ "the test and get immediate feedback on your performance. ";
 						switch (us.getTestDeployment().getType()) {
 						case TestDeployment.TYPE_NOTASSESSED:
 							break;
 						case TestDeployment.TYPE_ASSESSED_OPTIONAL:
 							sMessage += "However, you will not receive any credit</em> unless "
 									+ "you have previously obtained special permission to submit late.";
 						}
 						sMessage += "</p>";
 					}
 				} else if (us.getTestDeployment().hasCloseDate()) {
 					sMessage += "<p>This test will remain available until <strong>"
 							+ us.getTestDeployment().displayCloseDate()
 							+ "</strong>. ";
 					switch (us.getTestDeployment().getType()) {
 					case TestDeployment.TYPE_NOTASSESSED:
 						sMessage += "Please ensure that you complete and submit the test by "
 								+ "the end of that day.";
 						break;
 					case TestDeployment.TYPE_ASSESSED_REQUIRED:
 						sMessage += "Please ensure that you complete and submit the test by "
 								+ "the end of that day. You will not receive credit for answers "
 								+ "submitted later.";
 						break;
 					case TestDeployment.TYPE_ASSESSED_OPTIONAL:
 						sMessage += "If you wish this test to count towards your course mark, "
 								+ "please ensure that you complete and submit the test by the end "
 								+ "of that day. You will not receive credit for answers submitted "
 								+ "later.";
 						break;
 					}
 					sMessage += "</p>";
 				}
 
 				if (us.sOUCU.startsWith("_")) {
 					sMessage += "<p>Your progress through the test will be stored in your browser, "
 							+ "so you can leave the test and return on another day if you "
 							+ "don't have time to complete it now.</p>"
 							+ getAuthentication().getLoginOfferXHTML(request);
 				}
 
 				if (us.bAdmin) {
 					sMessage += "<div class='adminmsg'><p>You have administrative privileges on this test. The questions you will "
 							+ "see are the same as those shown to students, but you also have the "
 							+ "ability to repeat an individual question or the whole test, and jump "
 							+ "between questions. Depending on the test settings, these options may "
 							+ "not be available to students.</p>"
 							+ "<p>Even if you end the test, your results will not be "
 							+ "transferred to any reporting systems (so feel free to "
 							+ "end it if you want to see what happens; you can restart it "
 							+ "afterwards).</p>";
 
 					if (!us.getTestDeployment().isAfterOpen())
 						sMessage += "<p>This test is not yet open to students. It opens on "
 								+ us.getTestDeployment().displayOpenDate()
 								+ ". Students will receive "
 								+ "an error if they try to access it before then.</p>";
 
 					if (us.getTestDeployment().isAfterForbid())
 						sMessage += "<p>This test is no longer available to students. Access was "
 								+ "forbidden to students after "
 								+ us.getTestDeployment().displayForbidDate()
 								+ ". If students try to access "
 								+ "it now, they will receive an error.</p>";
 
 					if (us.bAllowReports)
 						sMessage += "<p>You have access to view "
 								+ "<a href='reports!'>reports on this test</a>, such as student "
 								+ "results.</p>";
 
 					sMessage += "</div>";
 				}
 
 				serveTestContent(
 						us,
 						ti.getTitle(),
 						"",
 						null,
 						"",
 						"<div class='basicpage'>"
 								+ sMessage
 								+ "<p><a id='focusthis' href='?next'>Begin</a></p>"
 								+ "</div>", bNav, request, response, true);
 			} else {
 				serveTestContent(
 						us,
 						ti.getTitle(),
 						"",
 						null,
 						"",
 						"<div class='basicpage'>"
 								+ ti.getXHTMLString()
 								+ "<p><a id='focusthis' href='?next'>Next</a></p>"
 								+ "</div>", true, request, response, true);
 			}
 			if (!ti.isDone()) {
 				ti.setDone(true);
 				DatabaseAccess.Transaction dat = da.newTransaction();
 				try {
 					oq.insertInfoPage(dat, us.getDbTi(), us.getTestPosition());
 				} finally {
 					rt.lDatabaseElapsed += dat.finish();
 				}
 			}
 		}
 	}
 
 	private void serveQuestionPage(RequestTimings rt, UserSession us,
 			TestQuestion tq, String sXHTML, boolean notReallyQuestion,
 			HttpServletRequest request, HttpServletResponse response)
 			throws IOException, OmException {
 		// get the section number if we are numbering by section
 
 		us.sSequence = Math.random() + "";
 		if (!notReallyQuestion) {
 			sXHTML = "<form method='post' action='./' autocomplete='off'>"
 					+ "<input type='hidden' name='" + SEQUENCEFIELD
 					+ "' value = '" + us.sSequence + "' />" + sXHTML
 					+ "</form>";
 		}
 		String qnh = "Question";
 		String sQuestionref = qnh;
 		if (us.isSingle()) {
 
 			Element eMetadata = getQuestionMetadata(rt, tq.getID(),
 					getLatestVersion(tq.getID(), tq.getVersion()).toString());
 			serveTestContent(us, XML.hasChild(eMetadata, "title") ? XML
 					.getText(eMetadata, "title") : qnh, "", null,
 					us.sProgressInfo, sXHTML, true, request, response, false);
 		} else {
 			// Either not using names, or question is unnamed
 			// check for numberbysection and use that if specified
 
 			if (us.getTestDefinition().getQuestionNumberHeader().compareTo("") > 0) {
 				qnh = us.getTestDefinition().getQuestionNumberHeader();
 				sQuestionref = qnh + " " + getSectionNum(us, tq) + "."
 						+ getNumInSection(us, tq);
 
 			} else {
 				if (us.getTestDefinition().isNumberBySection()) {
 					qnh = us.getTestDefinition().getQuestionNumberHeader();
 					sQuestionref = qnh + " " + getSectionNum(us, tq) + "."
 							+ getNumInSection(us, tq);
 				} else {
 					sQuestionref = qnh + " " + tq.getNumber();
 				}
 			}
 
 			// if its a defult question, then include the (of XXX) but if nbs is
 			// specified then dont
 			String ofmax1 = (us.getTestDefinition().isNumberBySection()) ? ""
 					: "(" + tq.getNumber() + "of " + getQuestionMax(us) + ")";
 			String ofmax2 = (us.getTestDefinition().isNumberBySection()) ? ""
 					: "(of " + getQuestionMax(us) + ")";
 
 			if (us.getTestDefinition() != null
 					&& us.getTestDefinition().areQuestionsNamed()) {
 				Element eMetadata = getQuestionMetadata(rt, tq.getID(),
 						getLatestVersion(tq.getID(), tq.getVersion())
 								.toString());
 
 				if (XML.hasChild(eMetadata, "title")) {
 					serveTestContent(us, XML.getText(eMetadata, "title"),
 							ofmax1, us.bAdmin ? tq.getID() : null,
 							us.sProgressInfo, sXHTML, true, request, response,
 							notReallyQuestion);
 					return;
 				}
 			}
 			serveTestContent(us, sQuestionref, ofmax2, us.bAdmin ? tq.getID()
 					: null, us.sProgressInfo, sXHTML, true, request, response,
 					notReallyQuestion);
 		}
 	}
 
 	private static int getSectionNum(UserSession us, TestQuestion tq)
 			throws OmException {
 		// check all the leaves and when you find one that matches, return the
 		// section
 		// number its in
 		int iSectionNum = 0;
 		String sCurrentSection = "";
 		TestLeaf tl = us.getTestLeavesInOrder()[0];
 
 		for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 			tl = us.getTestLeavesInOrder()[i];
 			if ((tl.getSection() != null && sCurrentSection != null)) {
 				// if this section is differenct
 				if (!tl.getSection().equals(sCurrentSection)) {
 					iSectionNum++;
 				}
 				if (tl instanceof TestQuestion) {
 
 					if (((TestQuestion) tl).getNumber() == tq.getNumber()) {
 						return iSectionNum;
 					}
 				}
 				sCurrentSection = tl.getSection();
 
 			}
 		}
 		throw new OmException("No questions?? ");
 	}
 
 	private static int getNumInSection(UserSession us, TestQuestion tq)
 			throws OmException {
 		int iSectionNum = 0;
 		int iNumInSection = 0;
 		String sCurrentSection = "";
 		TestLeaf tl = us.getTestLeavesInOrder()[0];
 
 		for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 			tl = us.getTestLeavesInOrder()[i];
 			// Check if new section
 			if ((tl.getSection() != null && !tl.getSection().equals(
 					sCurrentSection))
 					|| (tl.getSection() == null && sCurrentSection != null)) {
 				iNumInSection = 0;
 				sCurrentSection = tl.getSection();
 			}
 			if (tl instanceof TestQuestion) {
 				iNumInSection++;
 
 				// getNumber is wrong, need to get the numb
 				if (((TestQuestion) tl).getNumber() == tq.getNumber()) {
 					return iNumInSection;
 				}
 			}
 
 		}
 		throw new OmException("No questions??");
 	}
 
 	/**
 	 * Performs database initialisation for current question so that a new
 	 * attempt may start. Does not actually start a question session.
 	 * 
 	 * @param us
 	 *            Session (uses current index to determine the question)
 	 * @return Number of this attempt
 	 * @throws SQLException
 	 *             If the database bitches
 	 */
 	private int initQuestionAttempt(RequestTimings rt, UserSession us)
 			throws SQLException {
 		if (us.isSingle())
 			return (int) System.currentTimeMillis();
 
 		int iAttempt;
 		TestQuestion tq = (TestQuestion) us.getTestLeavesInOrder()[us
 				.getTestPosition()];
 		DatabaseAccess.Transaction dat = da.newTransaction();
 		try {
 			// See if there's an existing attempt
 			ResultSet rs = oq.queryMaxQuestionAttempt(dat, us.getDbTi(), tq
 					.getID());
 			int iMaxAttempt = 0;
 			if (rs.next() && rs.getMetaData().getColumnCount() > 0)
 				iMaxAttempt = rs.getInt(1);
 			iAttempt = iMaxAttempt + 1;
 
 			// Initially zero version - we set this later when question is
 			// started
 			oq.insertQuestion(dat, us.getDbTi(), tq.getID(), iAttempt);
 			us.iDBqi = oq.getInsertedSequenceID(dat, "questions", "qi");
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 		return iAttempt;
 	}
 
 	/**
 	 * @param iAttempts
 	 *            'Attempts' value
 	 * @param td
 	 * @return String describing value, for use in summary tables
 	 */
 	public static String getAttemptsString(int iAttempts, TestDefinition td) {
 		String str = retrieveSummaryConfirmation(td);
 		boolean substitute = StringUtils.isNotEmpty(str);
 		switch (iAttempts) {
 		case ATTEMPTS_PARTIALLYCORRECT:
 			return "Partially correct";
 		case ATTEMPTS_WRONG:
 			return substitute ? str : "Incorrect";
 		case ATTEMPTS_PASS:
 			return "Passed";
 		case 1:
 			return substitute ? str : "Correct at 1st attempt";
 		case 2:
 			return substitute ? str : "Correct at 2nd attempt";
 		case 3:
 			return substitute ? str : "Correct at 3rd attempt";
 		default:
 			return substitute ? str : "Correct at " + iAttempts + "th attempt";
 		}
 	}
 
 	public static String retrieveSummaryConfirmation(TestDefinition td) {
 		String s = null;
 		if (null != td) {
 			try {
 				s = td.retrieveSummaryConfirmation();
 			} catch (XMLException x) {
 				// ignore ...
 			}
 		}
 		return s;
 	}
 
 	/**
 	 * @return the OmQueries object for this servlet.
 	 */
 	public OmQueries getOmQueries() {
 		return oq;
 	}
 
 	private String showCompletedQuestion(RequestTimings rt, UserSession us,
 			HttpServletRequest request, int iQI) throws Exception {
 		// Produce output based on this
 		String sXHTML = "<div class='basicpage'>";
 
 		if (us.getTestDefinition().isSummaryAllowed()) {
 			if (us.getTestDefinition().doesSummaryIncludeScores()) {
 				// Update score data, we'll need it later
 				us.getTestRealisation().getScore(rt, this, da, oq);
 			}
 
 			// Get results (question and users's answer only) from this question
 			DatabaseAccess.Transaction dat = da.newTransaction();
 			try {
 				ResultSet rs = oq.queryQuestionResults(dat, iQI);
 				if (rs.next()) {
 					TestDefinition td = us.getTestDefinition();
 					sXHTML += "<p>You have already answered this question.</p>"
 							+ "<table class='leftheaders'>";
 					if (us.getTestDefinition().doesSummaryIncludeQuestions())
 						sXHTML += "<tr><th scope='row'>Question</th><td>"
 								+ XHTML.escape(rs.getString(1),
 										XHTML.ESCAPE_TEXT)
 								+ "</td></tr>"
 								+ "<tr><th scope='row'>Your answer</th><td>"
 								+ XHTML.escape(rs.getString(2),
 										XHTML.ESCAPE_TEXT) + "</td></tr>";
 					if (us.getTestDefinition().doesSummaryIncludeAttempts())
 						sXHTML += "<tr><th scope='row'>Result</th><td>"
 								+ XHTML.escape(getAttemptsString(rs.getInt(3),
 										td), XHTML.ESCAPE_TEXT) + "</td></tr>";
 					if (us.getTestDefinition().doesSummaryIncludeScores()) {
 						// Get score (scaled)
 						TestQuestion tq = (TestQuestion) us
 								.getTestLeavesInOrder()[us.getTestPosition()];
 						CombinedScore ps = tq.getScoreContribution(us
 								.getRootTestGroup());
 						String[] asAxes = ps.getAxesOrdered();
 						for (int iAxis = 0; iAxis < asAxes.length; iAxis++) {
 							String sAxis = asAxes[iAxis];
 							String sScore = displayScore(ps.getScore(sAxis)), sMax = displayScore(ps
 									.getMax(sAxis));
 
 							sXHTML += "<tr><th scope='row'>Score"
 									+ (sAxis == null ? "" : " (" + sAxis + ")")
 									+ "</th><td>" + sScore + " out of " + sMax
 									+ "</td></tr>";
 						}
 					}
 					sXHTML += "</table>";
 				} else {
 					sXHTML += "<p>You have already answered this question, but question and "
 							+ "answer text have not been recorded.</p>";
 				}
 			} finally {
 				rt.lDatabaseElapsed += dat.finish();
 			}
 			sXHTML += "<p><a href='?summary'>View a summary of all your answers so far</a></p>"
 					+ "<p><a href='?next'>Next question</a></p>";
 		} else {
 			sXHTML += "<p>You have already answered this question.</p>";
 		}
 
 		if (us.getTestDefinition().isRedoQuestionAllowed() || us.bAdmin) {
 			if (!us.getTestDefinition().isRedoQuestionAllowed()) {
 				sXHTML += "<div class='adminmsg'>You are only permitted to redo the "
 						+ "question because you have admin privileges. Students will not see "
 						+ "the following button.</div>";
 			}
 
 			if (RequestHelpers.inPlainMode(request)) {
 				sXHTML += "<form action='?redo' method='post'>"
 						+ "<p>If you redo the question, your new answer will be considered instead of "
 						+ "the one above.</p>"
 						+ "<input type='submit' value='Redo this question'/> "
 						+ "</form>";
 			} else {
 				sXHTML += "<form action='?redo' method='post'>"
 						+ "<input type='submit' value='Redo this question'/> "
 						+ "(If you redo it, your new answer will be considered instead of the one above.)"
 						+ "</form>";
 			}
 		}
 
 		sXHTML += "</div>";
 
 		return sXHTML;
 	}
 
 	private void handleNothing(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		if (us.oss == null)
 			servePage(rt, us, false, request, response);
 		else
 			doQuestion(rt, us, request, response, new NameValuePairs());
 	}
 
 	private void handleEnd(RequestTimings rt, boolean bPost, boolean bTimeOver,
 			UserSession us, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		// Get rid of question session if there is one
 		stopQuestionSession(rt, us);
 
 		if (returnPreProcessingResponse(us, request, response)) {
 			return;
 		}
 
 		// If they can't navigate then this really is the end, mark it finished
 		// and we're gone. Also if we are currently actually posting.
 		if (!us.getTestDefinition().isNavigationAllowed() || bPost) {
 			// Set in database
 			DatabaseAccess.Transaction dat = da.newTransaction();
 			try {
 				oq.updateTestFinished(dat, us.getDbTi());
 				us.setFinished(true);
 			} finally {
 				rt.lDatabaseElapsed += dat.finish();
 			}
 
 			// Send an email if desired - to non-admins and only people who look
 			// like students
 			if (us.getTestDeployment().requiresSubmitEmail() && !us.bAdmin
 					&& us.ud.shouldReceiveTestMail()) {
 				String sEmail = IO.loadString(new FileInputStream(
 						getServletContext().getRealPath(
 								"WEB-INF/templates/submit.email.txt")));
 				Map<String, String> mReplace = new HashMap<String, String>();
 				mReplace.put("NAME", us.getTestDefinition().getName());
 				mReplace.put("TIME",
 						(new SimpleDateFormat("dd MMMM yyyy HH:mm"))
 								.format(new Date()));
 				sEmail = XML.replaceTokens(sEmail, "%%", mReplace);
 
 				try {
 					String token = getAuthentication().sendMail(us.sOUCU, us.ud.getPersonID(),
 							sEmail, Authentication.EMAIL_CONFIRMSUBMIT);
 					getLog().logNormal(
 							"Sent submit confirm email to " + us.sOUCU
 									+ " for " + us.getTestId()
 									+ " (message despatch ID " + token + ")");
 					us.iEmailSent = 1;
 				} catch (Exception e) {
 					getLog().logError(
 							"Error sending submit confirm email to " + us.sOUCU
 									+ " for " + us.getTestId(), e);
 					us.iEmailSent = -1;
 				}
 			}
 
 			// Redirect back to test page
 			response.sendRedirect(request.getRequestURL().toString()
 					.replaceAll("/[^/]*$", "/")
 					+ RequestHelpers.endOfURL(request));
 		} else {
 			// The 'end, are you sure?' page
 			Document d = XML.parse(new File(getServletContext().getRealPath(
 					"WEB-INF/templates/endcheck.xhtml")));
 			Map<String, Object> mReplace = new HashMap<String, Object>();
 			mReplace.put("EXTRA", RequestHelpers.endOfURL(request));
 			mReplace.put("BUTTON", us.getTestDefinition()
 					.getConfirmButtonLabel());
 			mReplace.put("CONFIRMPARAS", us.getTestDefinition()
 					.getConfirmParagraphs());
 			// only ask for a confirm if the question is assessed
 
 			if ((us.getTestDeployment().getType() == TestDeployment.TYPE_ASSESSED_REQUIRED)
 					|| (us.getTestDeployment().getType() == TestDeployment.TYPE_ASSESSED_OPTIONAL)) {
 				mReplace
 						.put(
 								"CONFIRMSUBMIT",
 								"javascript:return confirm(\'You are about to complete this test. Once completed you will no longer be able to "
 										+ "return to any questions that are marked \\\'Not completed\\\'.\');");
 			} else {
 				mReplace.put("CONFIRMSUBMIT", "");
 			}
 
 			if (bTimeOver)
 				XML.remove(XML.find(d, "id", "return"));
 			else
 				XML.remove(XML.find(d, "id", "timeover"));
 
 			XML.replaceTokens(d, mReplace);
 			if (us.getTestDefinition().isSummaryIncludedAtEndCheck()) {
 				Element eDiv = XML.find(d, "id", "summarytable");
 				// XML.createText(eDiv,"h3","Your answers");
 				boolean bScores = us.getTestDefinition()
 						.doesSummaryIncludeScores();
 				if (bScores) {
 					// Build the scores list from database
 					us.getTestRealisation().getScore(rt, this, da, oq);
 				}
 				addSummaryTableDelegate(rt, us, eDiv, RequestHelpers.inPlainMode(request), us
 						.getTestDefinition().doesSummaryIncludeQuestions(), us
 						.getTestDefinition().doesSummaryIncludeAttempts(),
 						bScores);
 			}
 
 			serveTestContent(us, us.getTestDefinition().getConfirmTitle(), "",
 					null, null, XML.saveString(d), false, request, response,
 					true);
 		}
 	}
 
 	private void handleVariant(RequestTimings rt, String sVariant,
 			UserSession us, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		// Check access
 		if (!us.bAdmin) {
 			sendError(us, request, response, HttpServletResponse.SC_FORBIDDEN,
 					false, false, null, "Access denied",
 					"You do not have access to this feature.", null);
 		}
 
 		// Check not mid-test
 		DatabaseAccess.Transaction dat = da.newTransaction();
 		int iCount = 0;
 		try {
 			ResultSet rs = oq.queryQuestionAttemptCount(dat, us.getDbTi());
 			rs.next();
 			iCount = rs.getInt(1);
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 
 		if (iCount > 0 || us.getTestPosition() != 0) {
 			sendError(
 					us,
 					request,
 					response,
 					HttpServletResponse.SC_FORBIDDEN,
 					false,
 					false,
 					null,
 					"Cannot change variant mid-test",
 					"You cannot change variant in the "
 							+ "middle of a test. End the test first, then change variant while on "
 							+ "the initial page.", null);
 		}
 
 		// Get rid of question session if there is one
 		stopQuestionSession(rt, us);
 
 		// Set variant
 		try {
 			us.setFixedVariant(Integer.parseInt(sVariant));
 		} catch (NumberFormatException nfe) {
 			sendError(us, request, response, HttpServletResponse.SC_NOT_FOUND,
 					false, false, null, "Invalid variant",
 					"Variant must be a number.", null);
 		}
 
 		// Set in database
 		dat = da.newTransaction();
 		try {
 			oq.updateTestVariant(dat, us.getDbTi(), us.getFixedVariant());
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 
 		// Redirect back
 		redirectToTest(request, response);
 	}
 
 	private void handleAccess(RequestTimings rt, boolean bPost,
 			boolean bPlainJump, UserSession us, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		// Get rid of question session if there is one
 		stopQuestionSession(rt, us);
 		// Either if jumping or submitting the form...
 		if (bPost || bPlainJump) {
 			String sNewCookie;
 			if (bPlainJump) {
 				// Get current cookie
 				sNewCookie = RequestHelpers.getAccessibilityCookie(request);
 				if (sNewCookie.indexOf("[plain]") == -1)
 					sNewCookie += "[plain]";
 				else
 					sNewCookie = sNewCookie.replaceAll("\\[plain\\]", "");
 			} else {
 				sNewCookie = "";
 				String sZoom = request.getParameter("zoom"), sColours = request
 						.getParameter("colours"), sPlain = request
 						.getParameter("plainmode");
 				if (sZoom != null && !sZoom.equals("normal"))
 					sNewCookie += "[zoom=" + sZoom + "]";
 				if (sColours != null && !sColours.equals("normal"))
 					sNewCookie += "[colours=" + sColours + "]";
 				if (sPlain != null && sPlain.equals("yes"))
 					sNewCookie += "[plain]";
 			}
 
 			// Set cookie
 			Cookie c = new Cookie(RequestHelpers.ACCESSCOOKIENAME, sNewCookie);
 			c.setPath("/");
 			if (sNewCookie.equals(""))
 				c.setMaxAge(0); // Delete cookie
 			else
 				c.setMaxAge(60 * 60 * 24 * 365 * 10); // 10 years
 			response.addCookie(c);
 
 			// Redirect back to test page
 			redirectToTest(request, response);
 		} else {
 			// The form
 			Document d = XML.parse(new File(getServletContext().getRealPath(
 					"WEB-INF/templates/accessform.xhtml")));
 			Map<String, String> mReplace = new HashMap<String, String>();
 			mReplace.put("EXTRA", RequestHelpers.endOfURL(request));
 			XML.replaceTokens(d, mReplace);
 
 			// Get cookie and update initial values
 			String sCookie = RequestHelpers.getAccessibilityCookie(request);
 			if (sCookie.indexOf("[zoom=1.5]") != -1)
 				XML.find(d, "id", "bzoom").setAttribute("checked", "checked");
 			else if (sCookie.indexOf("[zoom=2.0]") != -1)
 				XML.find(d, "id", "czoom").setAttribute("checked", "checked");
 			else
 				XML.find(d, "id", "azoom").setAttribute("checked", "checked");
 
 			Matcher m = RequestHelpers.COLOURSPATTERN.matcher(sCookie);
 			if (m.matches()) {
 				Element e = XML.find(d.getDocumentElement(), "value", m
 						.group(1));
 				if (e != null)
 					e.setAttribute("checked", "checked");
 			} else
 				XML.find(d, "id", "acolours")
 						.setAttribute("checked", "checked");
 
 			if (sCookie.indexOf("[plain]") != -1)
 				XML.find(d, "id", "plainmode").setAttribute("checked",
 						"checked");
 
 			serveTestContent(us, "Display options", "", null, null, XML
 					.saveString(d), false, request, response, true);
 		}
 	}
 
 	private void redirectToTest(HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		response.sendRedirect(request.getRequestURL().toString().replaceAll(
 				"/[^/]*$", "/")
 				+ RequestHelpers.endOfURL(request));
 	}
 
 	private void handleProcess(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		NameValuePairs p = new NameValuePairs();
 		for (Enumeration<?> e = request.getParameterNames(); e
 				.hasMoreElements();) {
 			String sName = (String) e.nextElement();
 			String sValue = request.getParameter(sName);
 
 			if (sName.equals(SEQUENCEFIELD)) {
 				if (us.sSequence == null || !us.sSequence.equals(sValue)) {
 					sendError(
 							us,
 							request,
 							response,
 							HttpServletResponse.SC_FORBIDDEN,
 							false,
 							false,
 							null,
 							ACCESSOUTOFSEQUENCE,
 							"You have entered data outside the normal sequence. This can occur "
 									+ "if you use your browser's Back or Forward buttons; please don't use "
 									+ "these during the test. It can also happen if you click on something "
 									+ "while a page is loading.", null);
 				}
 			} else if (sValue.length() > 4000) {
 				sendError(us, request, response,
 						HttpServletResponse.SC_FORBIDDEN, false, false, null,
 						INPUTTOOLONG,
 						"You have entered more data than we allow. Please don't enter more "
 								+ "data than is reasonable.", null);
 			} else {
 				p.add(sName, sValue);
 			}
 		}
 		// Plain mode gets added as a parameter because components might need to
 		// know it in order to properly interpret input data (e.g. when
 		// replayed,
 		// even after not in plain mode any more)
 		if (RequestHelpers.inPlainMode(request))
 			p.add("plain", "yes");
 
 		doQuestion(rt, us, request, response, p);
 	}
 
 	private void doQuestion(RequestTimings rt, UserSession us,
 			HttpServletRequest request, HttpServletResponse response,
 			NameValuePairs p) throws Exception {
 		if (returnPreProcessingResponse(us, request, response)) {
 			return;
 		}
 		ProcessReturn pr = us.oss.process(rt, p.getNames(), p.getValues());
 
 		// Store details in database
 		TestQuestion tq = ((TestQuestion) us.getTestLeavesInOrder()[us
 				.getTestPosition()]);
 		if (!us.isSingle()) {
 			DatabaseAccess.Transaction dat = da.newTransaction();
 			try {
 				// Add action and params
 				oq.insertAction(dat, us.iDBqi, us.iDBseq);
 				for (int i = 0; i < p.getNames().length; i++) {
 					oq.insertParam(dat, us.iDBqi, us.iDBseq, p.getNames()[i], p
 							.getValues()[i]);
 				}
 				us.iDBseq++;
 
 				// Add results if any
 				if (pr.getResults() != null) {
 					// Mark done as soon as we get results
 					tq.setDone(true);
 
 					Results r = pr.getResults();
 
 					// Store summary text
 					oq.insertResult(dat, us.iDBqi,
 							r.getQuestionLine() == null ? "" : r
 									.getQuestionLine(),
 							r.getAnswerLine() == null ? "" : r.getAnswerLine(),
 							r.getActionSummary() == null ? "" : r
 									.getActionSummary(), r.getAttempts());
 
 					// Store scores
 					Score[] as = r.getScores();
 					for (int iScore = 0; iScore < as.length; iScore++) {
 						Score s = as[iScore];
 						oq.insertScore(dat, us.iDBqi, s.getAxis() == null ? ""
 								: s.getAxis(), s.getMarks());
 					}
 
 					// Store custom results
 					CustomResult[] acr = pr.getResults().getCustomResults();
 					for (int iCustom = 0; iCustom < acr.length; iCustom++) {
 						oq.insertCustomResult(dat, us.iDBqi, acr[iCustom]
 								.getName(), acr[iCustom].getValue());
 					}
 
 					if (!pr.isQuestionEnd()) // Don't bother if we're about to
 						// mark it as 2
 						oq.updateQuestionFinished(dat, us.iDBqi, 1);
 				}
 
 				// Add end if any
 				if (pr.isQuestionEnd()) {
 					oq.updateQuestionFinished(dat, us.iDBqi, 2);
 				}
 			} finally {
 				rt.lDatabaseElapsed += dat.finish();
 			}
 		}
 
 		if (pr.isQuestionEnd()) {
 			// Mark done again just so it works while the questions don't return
 			// results
 			tq.setDone(true);
 
 			// Get rid of current question session (no need to stop it)
 			us.oss = null;
 			us.mResources.clear();
 
 			if (us.isSingle()) // Restart question
 			{
 				servePage(rt, us, false, request, response);
 			} else {
 				// Go onto next un-done question that's available
 				if (!findNextQuestion(rt, us, false, !us.getTestDefinition()
 						.isRedoQuestionAllowed(), !us.getTestDefinition()
 						.isRedoQuestionAllowed())) {
 					// No more? Jump to end
 					redirectToEnd(request, response);
 				} else {
 					// Serve that page
 					servePage(rt, us, false, request, response);
 				}
 			}
 		} else {
 			applySessionChanges(us, pr);
 
 			// Serve XHTML
 			serveQuestionPage(rt, us, tq, pr.getXHTML(), false, request,
 					response);
 		}
 	}
 
 	private static int getQuestionMax(UserSession us) throws OmException {
 		for (int i = us.getTestLeavesInOrder().length - 1; i >= 0; i--) {
 			if (us.getTestLeavesInOrder()[i] instanceof TestQuestion) {
 				return ((TestQuestion) us.getTestLeavesInOrder()[i])
 						.getNumber();
 			}
 		}
 		throw new OmException("No questions??");
 	}
 
 	/**
 	 * Moves to the next question, skipping those that are unavailable or that
 	 * have been done already.
 	 * 
 	 * @param us
 	 *            Current session
 	 * @param bFromBeginning
 	 *            If true, starts from first question instead of looking at the
 	 *            next one after us.iIndex.
 	 * @param bSkipDone
 	 *            If true, skips 'done' questions, otherwise only skips
 	 *            unavailable ones
 	 * @param bWrap
 	 *            If false, doesn't wrap to first question
 	 * @return True if moved successfully, false if all questions are
 	 *         unavailable/done
 	 */
 	private boolean findNextQuestion(RequestTimings rt, UserSession us,
 			boolean bFromBeginning, boolean bSkipDone, boolean bWrap)
 			throws SQLException {
 		// Initial check for whether all questions are done; if so it skips to
 		// end
 		if (bSkipDone) {
 			boolean bAllQuestionsDone = true;
 			for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 				if (!us.getTestLeavesInOrder()[i].isDone()
 						&& (us.getTestLeavesInOrder()[i] instanceof TestQuestion)) {
 					bAllQuestionsDone = false;
 					break;
 				}
 			}
 			if (bAllQuestionsDone)
 				return false; // Make it skip to end, not to an info page
 		}
 
 		int iNewIndex = us.getTestPosition(), iBeforeIndex = us
 				.getTestPosition();
 		if (bFromBeginning) {
 			iNewIndex = -1;
 		}
 
 		while (true) {
 			// Next question (wrap around)
 			iNewIndex++;
 			if (iNewIndex >= us.getTestLeavesInOrder().length) {
 				if (bWrap)
 					iNewIndex = 0;
 				else {
 					iNewIndex--;
 					return false;
 				}
 			}
 
 			// Run out of questions?
 			if (iNewIndex == iBeforeIndex) {
 				return false;
 			}
 
 			// After first time (so we don't stop immediately), make it only
 			// stop
 			// after looping once
 			if (bFromBeginning) {
 				iBeforeIndex = 0;
 				bFromBeginning = false;
 			}
 
 			// See if question is available and not done
 			if ((!bSkipDone || !us.getTestLeavesInOrder()[iNewIndex].isDone())
 					&& us.getTestLeavesInOrder()[iNewIndex].isAvailable())
 				break;
 		}
 
 		setIndex(rt, us, iNewIndex);
 
 		return true;
 	}
 
 	/**
 	 * Update position within text.
 	 * 
 	 * @param rt
 	 *            Request timings
 	 * @param us
 	 *            Session
 	 * @param iNewIndex
 	 *            New index value
 	 * @throws SQLException
 	 *             Any database problem in storing new position
 	 */
 	private void setIndex(RequestTimings rt, UserSession us, int iNewIndex)
 			throws SQLException {
 		if (us.getTestPosition() == iNewIndex)
 			return;
 
 		DatabaseAccess.Transaction dat = da.newTransaction();
 		try {
 			oq.updateSetTestPosition(dat, us.getDbTi(), iNewIndex);
 			us.setTestPosition(iNewIndex);
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 	}
 
 	// Method is NOT synchronized on UserSession
 	private void handleCSS(UserSession us, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		String sCSS;
 		synchronized (us) {
 			sCSS = us.sCSS;
 		}
 
 		response.setContentType("text/css");
 		response.setCharacterEncoding("UTF-8");
 		stopBrowserCaching(request, response);
 		response.getWriter().write(sCSS);
 		response.getWriter().close();
 	}
 
 	/**
 	 * Set the http headers to stop browsers caching the resources. This fixes
 	 * the random questions problems which occur on the second run; Problems
 	 * were due to cached javascript files, now new javascript files will be
 	 * reloaded for new set of questions.
 	 * 
 	 * @param response
 	 *            the response to add headers
 	 */
 	private void stopBrowserCaching(HttpServletRequest request,
 			HttpServletResponse response) {
 		response.setHeader("Cache-Control", "must-revalidate");
 		response.addHeader("Cache-Control", "no-cache");
 		response.addHeader("Cache-Control", "no-store");
 		response.setDateHeader("Expires", 0);
 		if (XHTML.isIE9(request)) {
 			response.addHeader("X-UA-Compatible", "IE=8");
 		}
 	}
 
 	// Method is NOT synchronized on UserSession
 	private void handleResource(String sResource, UserSession us,
 		HttpServletRequest request, HttpServletResponse response)
 		throws Exception {
 		Resource r;
 		sResource = subStringResourceName(sResource);
 		synchronized (us) {
 			r = us.mResources.get(sResource);
 		}
 		if (r == null) {
 			sendError(null, request, response,
 					HttpServletResponse.SC_NOT_FOUND, true, false, null,
 					"Not found", "Requested resource '" + sResource
 							+ "' not found.", null);
 		}
 		response.setContentType(r.getMimeType());
 		response.setContentLength(r.getContent().length);
 		stopBrowserCaching(request, response);
 		if (r.getEncoding() != null)
 			response.setCharacterEncoding(r.getEncoding());
 		response.getOutputStream().write(r.getContent());
 		response.getOutputStream().close();
 	}
 
 	/**
 	 * In order to enforce that the script itself is not cached we append
 	 *  something unique when sending it to the client.  Here we simply strip
 	 *  that off from the request so to pick up the script itself from the
 	 *  UserSession resources.
 	 * @param sResource
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	private String subStringResourceName(String sResource) {
 		if (StringUtils.isNotEmpty(sResource)
 			? sResource.startsWith(SCRIPT_JS) : false) {
 			int n = sResource.indexOf("?");
 			if (n > -1 ? sResource.length() > n + 1 : false) {
 				sResource = sResource.substring(0, n);
 			}
 		}
 		return sResource;
 	}
 
 	private boolean checkRestartSession(RequestTimings rt, String sTestID,
 			UserSession us, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		// Look in database for session to restart
 		DatabaseAccess.Transaction dat = da.newTransaction();
 		try {
 			// Search for matching unfinished sessions for this OUCU
 			String sOUCU = us.sOUCU;
 			l.logDebug("Looking for a lost session for user " + sOUCU
 					+ " on test " + sTestID);
 			ResultSet rs = oq.queryUnfinishedSessions(dat, sOUCU, sTestID);
 			int iDBti = -1;
 			long lRandomSeed = 0;
 			boolean bFinished = false;
 			int iTestVariant = -1;
 			int iPosition = -1;
 			String navigatorversion = "";
 			if (rs.next()) {
 				iDBti = rs.getInt(1);
 				lRandomSeed = rs.getLong(2);
 				bFinished = rs.getInt(3) == 1;
 				iTestVariant = rs.getInt(4);
 				if (rs.wasNull())
 					iTestVariant = -1;
 				iPosition = rs.getInt(5);
 				navigatorversion = rs.getString(6);
 			}
 
 			// No match? OK, return false
 			if (iDBti == -1)
 				return false;
 
 			// Log IP address
 			storeSessionInfo(request, dat, iDBti);
 
 			// Set up basic data
 			us.setDbTi(iDBti);
 			initTestSession(us, rt, sTestID, request, response, bFinished,
 					true, lRandomSeed, iTestVariant);
 			us.setTestPosition(iPosition);
 			us.navigatorVersion = navigatorversion;
 
 			// Find out which question they're on
 			if (!us.isFinished()) {
 				// Find out which questions they've done (counting either
 				// 'getting
 				// results' or 'question end')
 				rs = oq.queryDoneQuestions(dat, us.getDbTi());
 
 				// Get all those question IDs
 				Set<String> sDone = new HashSet<String>();
 				while (rs.next())
 					sDone.add(rs.getString(1));
 
 				// Go through marking questions done
 				for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 					if (us.getTestLeavesInOrder()[i] instanceof TestQuestion) {
 						TestQuestion tq = (TestQuestion) us
 								.getTestLeavesInOrder()[i];
 						if (sDone.contains(tq.getID()))
 							tq.setDone(true);
 					}
 				}
 
 				// Find out info pages they've done
 				rs = oq.queryDoneInfoPages(dat, us.getDbTi());
 				while (rs.next()) {
 					int iIndex = rs.getInt(1);
 					TestInfo ti = (TestInfo) us.getTestLeavesInOrder()[iIndex];
 					ti.setDone(true);
 				}
 			}
 		} finally {
 			rt.lDatabaseElapsed += dat.finish();
 		}
 
 		// Redirect to system-check page if needed
 		doSystemCheck(us, request, response);
 
 		// Serve that question, or end page if bFinished
 		servePage(rt, us, true, request, response);
 
 		return true;
 	}
 
 	/**
 	 * If necessary, redirects user to the system check page. This is done only
 	 * once the system is actually ready to serve test content, in order to
 	 * avoid potential issues if the user requests something else unexpectedly
 	 * between sending this and returning to the main test page.
 	 * <p>
 	 * Does not continue (throws StopException) if the redirect is sent.
 	 * 
 	 * @param us
 	 *            Session
 	 * @param request
 	 *            HTTP request
 	 * @param response
 	 *            HTTP response
 	 * @throws IOException
 	 *             Any error
 	 * @throws StopException
 	 */
 	private void doSystemCheck(UserSession us, HttpServletRequest request,
 			HttpServletResponse response) throws IOException, StopException {
 		// Initial request gets redirected to the system-check Javascript page
 		if (!us.bCheckedBrowser && !us.getTestDeployment().isSingleQuestion()) // Don't
 		// do
 		// that
 		// for
 		// single
 		// question
 		{
 			us.bCheckedBrowser = true;
 			response.sendRedirect(RequestHelpers.getServletURL(request)
 					+ "!shared/systemcheck.html?"
 					+ URLEncoder.encode(request.getRequestURI(), "UTF-8"));
 			throw new StopException();
 		}
 	}
 
 	private void handleTestOrQuestion(File file, String what,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		// Check that access is allowed.
 		if (!(nc.isTrustedQE(InetAddress.getByName(request.getRemoteAddr()))
 			|| IPAddressCheckUtil.checkSecureIP(request, l, nc))) {
 			sendError(null, request, response,
 					HttpServletResponse.SC_FORBIDDEN, false, false, null,
 					"Forbidden", "You are not authorised to access this URL.",
 					null);
 		}
 
 		// Check that the requested file exits.
 		if (!file.exists()) {
 			sendError(null, request, response,
 					HttpServletResponse.SC_NOT_FOUND, false, false, null,
 					"Not found", "The requested " + what
 							+ " is not present on this server.", null);
 		}
 
 		// Then send it.
 		byte[] abQuestion = IO.loadBytes(new FileInputStream(file));
 		if (what.equals("question")) {
 			response.setContentType("application/x-openmark");
 			if (file.getName().endsWith(".omxml")) {
 				response.setContentType("application/x-openmark-dynamics");
 			}
 		} else {
 			response.setContentType("application/xml");
 			response.setCharacterEncoding("UTF-8");
 		}
 		response.setContentLength(abQuestion.length);
 		OutputStream os = response.getOutputStream();
 		os.write(abQuestion);
 		os.close();
 	}
 
 	private void handleQuestion(String sIDVersion, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		File file = new File(questionBankFolder, sIDVersion + ".jar");
 		if (!file.exists()) {
 			file = new File(questionBankFolder, sIDVersion + ".omxml");
 		}
 		handleTestOrQuestion(file, "question", request, response);
 	}
 
 	private void handleTest(String testId, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		File file = new File(getTestbankFolder(), testId + ".test.xml");
 		handleTestOrQuestion(file, "test definition", request, response);
 	}
 
 	private void handleDeploy(String deployId, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		File file = new File(getTestbankFolder(), deployId + ".deploy.xml");
 		handleTestOrQuestion(file, "deploy file", request, response);
 	}
 
 	private void handleForbid(String sOucuTest, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		if (!nc.isTrustedTN(InetAddress.getByName(request.getRemoteAddr()))) {
 			sendError(null, request, response,
 					HttpServletResponse.SC_FORBIDDEN, false, false, null,
 					"Forbidden", "You are not authorised to access this URL.",
 					null);
 		}
 
 		synchronized (sessions) {
 			// Ditch existing session
 			UserSession us = usernames.remove(sOucuTest);
 			if (us != null)
 				sessions.remove(us.sCookie);
 
 			// Forbid that user for 1 minute [this is intended to prevent the
 			// possibility of timing issues allowing a user to get logged on to
 			// both
 			// servers at once; should that happen, chances are they'll instead
 			// be
 			// *dumped* from both servers at once (for 60 seconds).
 			tempForbid.put(sOucuTest, System.currentTimeMillis() + 60000L);
 
 			// Send response
 			response.setContentType("text/plain");
 			response.setCharacterEncoding("UTF-8");
 			Writer w = response.getWriter();
 			w.write("OK");
 			w.close();
 		}
 	}
 
 	private void handleTestCookie(String suffix, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		getAuthentication().becomeTestUser(response, suffix);
 
 		response.setContentType("text/plain");
 		response.getWriter().write(
 				"Cookie set - you are now (on this server) !tst" + suffix);
 		response.getWriter().close();
 	}
 
 	private void handleShared(String sFile, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		// Look in both the 'shared' and 'WEB-INF/shared' folders (allows for
 		// standard files that are replaced in update, plus extra files)
 		File fUser = new File(getServletContext()
 				.getRealPath("shared/" + sFile)), fInternal = new File(
 				getServletContext().getRealPath("WEB-INF/shared/" + sFile));
 
 		// Pick one to use
 		File fActual = (fUser.exists() ? fUser
 				: (fInternal.exists() ? fInternal : null));
 		if (fActual == null) {
 			sendError(null, request, response,
 					HttpServletResponse.SC_NOT_FOUND, true, false, null,
 					"Not found", "The requested resource is not present.", null);
 		}
 
 		// Handle If-Modified-Since
 		long lIfModifiedSince = request.getDateHeader("If-Modified-Since");
 		if (lIfModifiedSince != -1) {
 			if (fActual.lastModified() <= lIfModifiedSince) {
 				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
 				return;
 			}
 		}
 
 		// Set type and length
 		response.setContentType(MimeTypes.getMimeType(sFile));
 		response.setContentLength((int) fActual.length());
 
 		// Set last-modified, and expiry for 4 hours
 		response.addDateHeader("Last-Modified", fActual.lastModified());
 		response.addDateHeader("Expires", System.currentTimeMillis() + 4L * 60L
 				* 60L * 1000L);
 
 		// Send actual data
 		OutputStream os = response.getOutputStream();
 		IO.copy(new FileInputStream(fActual), os, true);
 	}
 
 	/**
 	 * Annoyingly the String replaceAll method doesn't support multi-line
 	 * replace. This does.
 	 * 
 	 * @param p
 	 *            Pattern (must have MULTILINE flag)
 	 * @param sSource
 	 *            Source string
 	 * @param sReplace
 	 *            Replacement text
 	 * @return Replaced content
 	 */
 	private static String multiLineReplace(Pattern p, String sSource,
 			String sReplace) {
 		Matcher m = p.matcher(sSource);
 		StringBuffer sb = new StringBuffer();
 		while (m.find()) {
 			m.appendReplacement(sb, "");
 		}
 		m.appendTail(sb);
 		return sb.toString();
 	}
 
 	/**
 	 * Caters for the request for the TinyMCE associated files in the typical 
 	 *  OpenMark pattern.
 	 * 
 	 * @param path
 	 * @param post
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 * @author Trevor Hinson
 	 */
 	private void handleTinyMCEResponse(String path, boolean post,
 		HttpServletRequest request, HttpServletResponse response)
 		throws Exception {
 		Map<String, Object> config = new HashMap<String, Object>();
 		config.put(RequestParameterNames.logPath.toString(),
 			getServletContext().getContextPath());
 		RequestAssociates ra = new RequestAssociates(getServletContext(), path,
 			post, config);
 		Map<String, String> params = AbstractOpenMarkServlet.getParameters(request);
 		params.put(TinyMCERequestHandler.FILE_PATH, path);
 		ra.setRequestParameters(params);
 		RequestHandler rh = new TinyMCERequestHandler();
 		RequestResponse rr = rh.handle(request, response, ra);
 		rr.output();
 		OutputStream os = response.getOutputStream();
 		os.write(rr.output());
 		os.close();
 	}
 
 	private void handleNavigatorCSS(String sAccessBit,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		// Get CSS
 		if (navigatorCSS == null) {
 			navigatorCSS = IO.loadString(new FileInputStream(new File(
 					getServletContext().getRealPath(
 							"WEB-INF/templates/navigator.css"))));
 		}
 
 		// Do replace
 		Map<String, String> mReplace = new HashMap<String, String>();
 		if (sAccessBit == null) // No accessibility
 		{
 			mReplace.put("FG", "black");
 			mReplace.put("BG", "white");
 		} else {
 			try {
 				mReplace.put("FG", "#" + sAccessBit.substring(0, 6));
 				mReplace.put("BG", "#" + sAccessBit.substring(6, 12));
 			} catch (IndexOutOfBoundsException ioobe) {
 				mReplace.put("FG", "black");
 				mReplace.put("BG", "white");
 			}
 		}
 		String sCSS = XML.replaceTokens(navigatorCSS, "%%", mReplace);
 
 		// Get rid of fixed bits if it isn't fixed
 		if (sAccessBit == null) {
 			sCSS = multiLineReplace(FIXEDCOLOURCSSLINE, sCSS, "");
 		}
 
 		byte[] abBytes = sCSS.getBytes("UTF-8");
 
 		// Set type and length
 		response.setContentType("text/css");
 		response.setCharacterEncoding("UTF-8");
 		response.setContentLength(abBytes.length);
 
 		// Set expiry for 4 hours
 		response.addDateHeader("Expires", System.currentTimeMillis() + 4L * 60L
 				* 60L * 1000L);
 
 		// Send actual data
 		OutputStream os = response.getOutputStream();
 		os.write(abBytes);
 		os.close();
 	}
 
 	public static void addAccessibilityClasses(Document d,
 			HttpServletRequest request, String sInitialClass) {
 		// Fix up accessibility details from cookie
 		String sAccessibility = RequestHelpers.getAccessibilityCookie(request);
 		String sRootClass = "";
 		if (sInitialClass != null) {
 			try {
 				XML.getChild(d.getDocumentElement(), "body").setAttribute(
 						"class", sInitialClass);
 			} catch (XMLException e) {
 				throw new OmUnexpectedException(e);
 			}
 		}
 		// If in plain mode, no CSS so don't need no steenking classes
 		boolean bPlain = sAccessibility.indexOf("[plain]") != -1;
 		if (bPlain)
 			return;
 
 		// Zoom
 		Matcher m = ZOOMPATTERN.matcher(sAccessibility);
 		if (m.matches()) {
 			if (m.group(1).equals("1.5")) {
 				sRootClass += "zoom15 ";
 			} else if (m.group(1).equals("2.0")) {
 				sRootClass += "zoom20 ";
 			}
 		}
 
 		// Browser detection, for use in CSS
 		sRootClass += UserAgent.getBrowserString(request) + " ";
 
 		if (sRootClass.trim().length() > 0)
 			d.getDocumentElement().setAttribute("class", sRootClass.trim());
 	}
 
 	/** Cached template documents */
 	private Document plainTemplate, template, singlesPlainTemplate,
 			singlesTemplate, authorshipConfirmation;
 
 	public Document getAuthorshipConfirmation() {
 		return authorshipConfirmation;
 	}
 
 	public Document getTemplate() {
 		return template;
 	}
 
 	/**
 	 * Called from StatusPages to add key performance information into a map.
 	 * @param m Map to fill with info
 	 */
 	void obtainPerformanceInfo(Map<String, Object> m) throws ServletException {
 		System.gc();
 		System.gc();
 
 		String sMemoryUsed = Strings.formatBytes(Runtime.getRuntime()
 				.totalMemory()
 				- Runtime.getRuntime().freeMemory());
 		m.put("MEMORY", sMemoryUsed);
 
 		synchronized (sessions) {
 			m.put("SESSIONS", sessions.size() + "");
 			m.put("TEMPFORBIDS", tempForbid.size() + "");
 		}
 
 		m.put("VERSION", OmVersion.getVersion());
 		m.put("BUILDDATE", OmVersion.getBuildDate());
 
 		getAuthentication().obtainPerformanceInfo(m);
 
 		m.put("DBCONNECTIONS", da.getConnectionCount() + "");
 
 		URL uThis = nc.getThisTN();
 		m.put("MACHINE", uThis.getHost().replaceAll(".open.ac.uk", "")
 				+ uThis.getPath());
 
 		m.put("_qeperformance", osb.getPerformanceInfo());
 	}
 
 	/**
 	 * Serves a test content page to the user, including navigation.
 	 * 
 	 * @param us
 	 *            Session
 	 * @param sTitle
 	 *            Page main title
 	 * @param sAuxTitle
 	 *            Auxiliary title (use "" if not needed)
 	 * @param sTip
 	 *            Text that appears as tooltip on main heading (null if none)
 	 * @param sProgressInfo
 	 *            Text that appears where question progress info is shown.
 	 * @param sXHTML
 	 *            XHTML content of main part of page
 	 * @param bIncludeNav
 	 *            If true, includes the question numbers etc.
 	 * @param request
 	 *            HTTP request
 	 * @param response
 	 *            HTTP response
 	 * @param bClearCSS
 	 *            True if CSS should be cleared before serving this page
 	 * @throws IOException
 	 *             Any error in serving page
 	 */
 	public void serveTestContent(UserSession us, String sTitle,
 			String sAuxTitle, String sTip, String sProgressInfo, String sXHTML,
 			boolean bIncludeNav, HttpServletRequest request,
 			HttpServletResponse response, boolean bClearCSS) throws IOException {
 		String sAccessibility = RequestHelpers.getAccessibilityCookie(request);
 		boolean plainMode = sAccessibility.indexOf("[plain]") != -1;
 
 		if (us.getFixedVariant() >= 0)
 			sAuxTitle += " [variant " + us.getFixedVariant() + "]";
 
 		// Create basic template
 		Document d = getTemplate(plainMode, us.isSingle(), bClearCSS);
 		Map<String, Object> mReplace = new HashMap<String, Object>();
 		if (us.isSingle() || sTitle.equals(us.getTestDefinition().getName()))
 			mReplace.put("TITLEBAR", sTitle);
 		else
 			mReplace.put("TITLEBAR", us.getTestDefinition().getName() + " - "
 					+ sTitle);
 		mReplace.put("CSSINDEX", "" + us.iCSSIndex);
 		mReplace.put("RESOURCES", "resources/" + us.getTestPosition());
 		mReplace.put("ACCESS", RequestHelpers.getAccessCSSAppend(request));
 
 		if (!us.isSingle()) {
 			// Tooltip stuff is only there for non-plain, non-single mode
 			if (!plainMode) {
 				if (sTip != null)
 					mReplace.put("TOOLTIP", sTip);
 				else
 					XML.find(d, "title", "%%TOOLTIP%%")
 							.removeAttribute("title");
 			}
 
 			mReplace.put("TESTTITLE", us.getTestDefinition().getName());
 			mReplace.put("TITLE", sTitle);
 			mReplace.put("AUXTITLE", sAuxTitle);
 			if ((sProgressInfo == null || sProgressInfo.equals(""))) {
 				XML.remove(XML.find(d, "id", "progressinfo"));
 			} else {
 				mReplace.put("PROGRESSINFO", sProgressInfo);
 			}
 		}
 		XML.replaceTokens(d, mReplace);
 
 		// If debug hack is on, do that
 		if (nc.hasDebugFlag("allow-hacks")) {
 			if ((new File("c:/hack.css")).exists()) {
 				Element eHead = XML.getChild(d.getDocumentElement(), "head");
 				Element eLink = XML.createChild(eHead, "link");
 				eLink.setAttribute("rel", "stylesheet");
 				eLink.setAttribute("type", "text/css");
 				eLink.setAttribute("href", "file:///c:/hack.css");
 			}
 			if ((new File("c:/hack.js")).exists()) {
 				Element eBody = XML.getChild(d.getDocumentElement(), "body");
 				Element eLink = XML.createChild(eBody, "script");
 				eLink.setAttribute("type", "text/javascript");
 				eLink.setAttribute("src", "file:///c:/hack.js");
 			}
 		}
 
 		// Get question top-level element and clone it into new document
 		Element eQuestion = (Element) d.importNode(XML.parse(sXHTML)
 				.getDocumentElement(), true);
 		Element questionDiv = XML.find(d, "id", "question");
 		questionDiv.appendChild(eQuestion);
 
 		if (!us.isSingle()) {
 			// Build progress indicator
 			Element eProgress;
 			if (plainMode) {
 				eProgress = XML.find(d, "id", "progressPlain");
 			} else if (us.getTestDefinition().getNavLocation() == TestDefinition.NAVLOCATION_LEFT) {
 				XML.remove(XML.find(d, "id", "progressBottom"));
 				eProgress = XML.find(d, "id", "progressLeft");
 				eProgress.setAttribute("id", "progress");
 				addAccessibilityClasses(d, request, "progressleft");
 			} else {
 				XML.remove(XML.find(d, "id", "progressLeft"));
 				eProgress = XML.find(d, "id", "progressBottom");
 				eProgress.setAttribute("id", "progress");
 				if (us.getTestDefinition().getNavLocation() == TestDefinition.NAVLOCATION_WIDE) {
 					addAccessibilityClasses(d, request, "progresswide");
 				} else {
 					addAccessibilityClasses(d, request, "progressbottom");
 				}
 			}
 
 			Element eCurrentSection = null;
 			String sCurrentSection = null;
 
 			// Div for the buttons section
 			Element eButtons = XML.find(d, "id", "buttons");
 			if (plainMode) {
 				Element eH2 = eButtons.getOwnerDocument().createElement("h2");
 				XML.createText(eH2, "Options");
 				eButtons.insertBefore(eH2, eButtons.getFirstChild());
 			}
 
 			// Div for the main numbers section
 			boolean bAllDone = true;
 			if (bIncludeNav) {
 				if (plainMode)
 					XML.createText(eProgress, "h2", "Progress so far");
 				Element eNumbers = XML.createChild(eProgress, plainMode ? "ul"
 						: "div");
 				if (!plainMode)
 					eNumbers.setAttribute("class", "numbers");
 
 				boolean bAllowNavigation = us.bAdmin
 						|| us.getTestDefinition().isNavigationAllowed();
 
 				boolean bFirstInSection = true;
 				boolean bNotFirstNonInfoInSection = true;
 				int iFirstInSection = 0;
 
 				for (int i = 0; i < us.getTestLeavesInOrder().length; i++) {
 					TestLeaf tl = us.getTestLeavesInOrder()[i];
 
 					// Check if new section
 					if ((tl.getSection() != null && !tl.getSection().equals(
 							sCurrentSection))
 							|| (tl.getSection() == null && sCurrentSection != null)) {
 						sCurrentSection = tl.getSection();
 						if (sCurrentSection == null)
 							eCurrentSection = null;
 						else {
 							if (plainMode) {
 								Element eLI = XML.createChild(eNumbers, "li");
 								XML.createText(eLI, "h3", sCurrentSection);
 								eCurrentSection = XML.createChild(eLI, "ul");
 							} else {
 								eCurrentSection = XML.createChild(eNumbers,
 										"div");
 								eCurrentSection
 										.setAttribute("class", "section");
 								Element eTag = XML.createChild(eCurrentSection,
 										"div");
 								eTag.setAttribute("class", "sectiontag");
 								XML.createText(eTag, sCurrentSection);
 								Element eTagAfter = XML.createChild(
 										eCurrentSection, "div");
 								eTagAfter.setAttribute("class",
 										"sectiontagafter");
 							}
 						}
 						bFirstInSection = true;
 						bNotFirstNonInfoInSection = true;
 						iFirstInSection = 0;
 					}
 
 					if (bFirstInSection) {
 						bFirstInSection = false;
 					} else {
 						// Needed to allow IE to wrap line
 						if (!plainMode)
 							XML.createText(
 									eCurrentSection != null ? eCurrentSection
 											: eNumbers, " \u00a0 ");
 					}
 
 					boolean bText = (tl instanceof TestInfo), bCurrent = (i == us
 							.getTestPosition()), bDone = tl.isDone(), bAvailable = tl
 							.isAvailable();
 
 					// if we are restarting numbering at beginning of each
 					// section, then check if
 					// its the first non-info item in the section
 					if (us.getTestDefinition().isNumberBySection()
 							& bNotFirstNonInfoInSection & !bText) {
 						iFirstInSection = ((TestQuestion) tl).getNumber() - 1;
 						bNotFirstNonInfoInSection = false;
 
 					}
 
 					if (!bDone)
 						bAllDone = false;
 					boolean bLink = (!bCurrent || us.getTestDefinition()
 							.isRedoQuestionAllowed())
 							&& bAllowNavigation && bAvailable;
 
 					// Make child directly in progress or in current section if
 					// there is one
 					Element eThis = XML.createChild(
 							eCurrentSection != null ? eCurrentSection
 									: eNumbers, plainMode ? "li" : "div");
 
 					if (plainMode) {
 						Element eThingy = bLink ? XML.createChild(eThis, "a")
 								: eThis;
 						XML.createText(eThingy, bText ? (((TestInfo) tl)
 								.getTitle()) : ((TestQuestion) tl).getNumber()
 								- iFirstInSection + "");
 						if (bCurrent) {
 							XML.createText(eThingy, " (current)");
 						} else {
 							if (!bText)
 								XML.createText(eThingy, bDone ? " (done)"
 										: (" (not done" + (bAvailable ? ")"
 												: "; not yet available)")));
 						}
 						if (bLink)
 							eThingy.setAttribute("href", "?jump=" + i);
 					} else {
 						eThis.setAttribute("class", (bText ? "text"
 								: "question")
 								+ (bCurrent ? " current" : "")
 								+ (bDone ? " done" : "")
 								+ (!bAvailable ? " unavailable" : ""));
 
 						Element eThingy = XML.createChild(eThis, bLink ? "a"
 								: "span");
 						eThingy.setAttribute("class", "t");
 						XML.createText(eThingy, bText ? "Info"
 								: ((TestQuestion) tl).getNumber()
 										- iFirstInSection + " ");
 
 						if (bLink)
 							eThingy.setAttribute("href", "?jump=" + i);
 					}
 				}
 
 				// Do the summary
 				boolean bStopButton = (us.getTestDefinition().isStopAllowed() || bAllDone);
 				if (us.getTestDefinition().isSummaryAllowed()) {
 					Element eSummary = XML.createChild(eButtons, "div");
 					if (!plainMode)
 						eSummary.setAttribute("class", "button");
 					Element eThingy = XML.createChild(eSummary, "a");
 					XML.createText(eThingy, plainMode ? "Review your answers"
 							: "Your answers");
 					eThingy.setAttribute("href", "?summary");
 					if (!plainMode && bStopButton)
 						XML.createChild(eButtons, "div").setAttribute("class",
 								"buttonspacer");
 				}
 
 				if (bStopButton) {
 					Element eStop = XML.createChild(eButtons, "div");
 					if (!plainMode)
 						eStop.setAttribute("class", "button");
 					Element eThingy = XML.createChild(eStop, "a");
 					XML.createText(eThingy, "End test");
 					eThingy.setAttribute("href", "?end");
 				}
 			}
 		} else {
 			addAccessibilityClasses(d, request, "progressbottom singles");
 		}
 
 		// Fix up the replacement variables
 		mReplace = new HashMap<String, Object>(getLabelReplaceMap(us));
 		mReplace.put("RESOURCES", "resources/" + us.getTestPosition());
 		mReplace.put("IDPREFIX", "");
 		
 		XML.replaceTokens(eQuestion, mReplace);
 
 		// Whew! Now send to user
 		breakBack(request, response);
 
 		XHTML.output(d, request, response, "en");
 	}
 
 	/**
 	 * @param plainMode
 	 *            whether we are in plain mode.
 	 * @param singleQuestionMode
 	 *            whether we are in single question mode.
 	 * @param removeQuestionCss
 	 *            whether to remove the question stylesheet link.
 	 * @return the appropriate template for a page, based on the parameters.
 	 * @throws XMLException
 	 */
 	public Document getTemplate(boolean plainMode, boolean singleQuestionMode,
 			boolean removeQuestionCss) throws XMLException {
 		Document d;
 		if (singleQuestionMode) {
 			if (plainMode) {
 				d = singlesPlainTemplate;
 			} else {
 				d = singlesTemplate;
 			}
 		} else {
 			if (plainMode) {
 				d = plainTemplate;
 			} else {
 				d = template;
 			}
 		}
 		d = XML.clone(d);
 		if (!plainMode) {
 			if (removeQuestionCss) {
 				XML.remove(XML.find(d, "ss", "here"));
 			} else {
 				XML.find(d, "ss", "here").removeAttribute("ss");
 			}
 		}
 		return d;
 	}
 
 	/**
 	 * Returns the map of label replacements appropriate for the current
 	 * session.
 	 * 
 	 * @param us
 	 *            Session
 	 * @return Map of replacements (don't change this)
 	 * @throws IOException
 	 *             Any problems loading it
 	 */
 	private Map<String, String> getLabelReplaceMap(UserSession us)
 			throws IOException {
 		// Check labelset ID
 		String sKey;
 		if (us.getTestDefinition() == null
 				|| us.getTestDefinition().getLabelSet() == null
 				|| us.getTestDefinition().getLabelSet().equals(""))
 			sKey = "!default";
 		else
 			sKey = us.getTestDefinition().getLabelSet();
 
 		// Get from cache
 		Map<String, String> mLabels = labelReplace.get(sKey);
 		if (mLabels != null)
 			return mLabels;
 
 		// Load from file
 		Map<String, String> m = new HashMap<String, String>();
 		File f = new File(getServletContext().getRealPath(
 				"WEB-INF/labels/" + sKey + ".xml"));
 		if (!f.exists())
 			throw new IOException("Unable to find requested label set: " + sKey);
 		Document d = XML.parse(f);
 		Element[] aeLabels = XML.getChildren(d.getDocumentElement());
 		for (int i = 0; i < aeLabels.length; i++) {
 			m.put(XML.getRequiredAttribute(aeLabels[i], "id"), XML
 					.getText(aeLabels[i]));
 		}
 
 		// Cache and return
 		labelReplace.put(sKey, m);
 		return m;
 	}
 
 	/**
 	 * Wraps the call through to the ErrorManagement.sendErrorForUserSession(...
 	 *  method and then throws away the session also if told to. (though this is
 	 *  under question at the time of refactoring this).
 	 */
 	public void sendError(UserSession us, HttpServletRequest request,
 		HttpServletResponse response, int code, boolean isBug,
 		boolean keepSession, String backToTest, String title,
 		String message, Throwable exception) throws StopException {
 		OMVisitor visitor = new OMVisitor(da, oq, getAuthentication(), getServletContext());
 		// Note that no call though actually looks to remove the session so 
 		// Im looking to remove this kind of thing.
 //		if (!keepSession) {
 //			l.logDebug("Throwing away session.");
 //			synchronized (sessions) {
 //				sessions.values().remove(us);
 //			}
 //		}
 		ErrorMessageParts emp = new ErrorMessageParts(title, message, false,
 			exception, ErrorManagement.ERROR_TEMPLATE);
 		errorManagement.sendErrorForUserSession(us, request, response, code,
 			isBug, backToTest, visitor, emp);
 	}
 
 	public ReportDispatcher getReports() {
 		return reports;
 	}
 
 	public File getQuestionbankFolder() {
 		return questionBankFolder;
 	}
 
 	public File getTestbankFolder() {
 		return resolveRelativePath("testbank");
 	}
 
 	public File pathForTestDeployment(String testId) {
 		return resolveRelativePath("testbank/" + testId + ".deploy.xml");
 	}
 
 	public File resolveRelativePath(String relativePath) {
 		return new File(getServletContext().getRealPath(relativePath));
 	}
 }
