 package org.alt60m.servlet;
 
 import java.util.*;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.*;
 import java.net.InetAddress;
 
 import javax.servlet.http.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.*;
 import org.apache.log4j.joran.JoranConfigurator;
 import org.apache.log4j.spi.LoggerRepository;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.alt60m.util.LogHelper;
 
 /**
  * Defines functionality common to web controllers.
  *
  */
 public abstract class Controller extends HttpServlet {
 
 	private final int MAX_HISTORY_SIZE = 15;
 	
 	protected Log log = LogFactory.getLog(this.getClass());
 	
 	// Mapping of views to URLs
 	private Hashtable _views = new Hashtable();
 
 	// Default action, if action token not provided
     protected String _defaultErrorView = "error";
     protected String _defaultAction = "";
 	protected String _viewsFile = "";
 
 	// Maintains count of # of threads in this object
 	private int _thread_ctr;
 
 	protected class ActionContext
 	{
 		private final String ERR_TOKEN = "error";
 		private final String RETURN_TOKEN = "tub";
 		private final String LAST_ACTION_TOKEN = "last_action";
 		private final String LAST_LAST_ACTION_TOKEN = "last_last_action";
 		private final String LAST_VIEW_TOKEN = "last_view";
 		private final String LAST_LAST_VIEW_TOKEN = "last_last_view";
 
 		private HttpSession			_session;			/** User's session object */
 		private HttpServletRequest	_request;			/** Request object */
 		private HttpServletResponse _response;
 
 		public ActionContext(HttpServletRequest request, HttpServletResponse response) {
 
 			_session = request.getSession();
 			_request = request;
 			_response = response;
 
 			log.debug("New ActionContext for session ID:" + _session.getId());
 
 			// Clear state
 			_session.removeAttribute(RETURN_TOKEN);
 			_session.removeAttribute(ERR_TOKEN);
 
 
 		}
 		public HttpSession			getSession()	{ return _session; }
 		public HttpServletRequest	getRequest()	{ return _request; }
 		public HttpServletResponse getResponse()	{ return _response; }
 
 		public String getInputString(String key) {
 			return _request.getParameter(key);
 		}
 
 		public String getInputString(String key, boolean required) throws MissingRequestParameterException {
 
 			Object value = _request.getParameter(key);
 
 			if ((value == null) && (required == true))
 				throw new MissingRequestParameterException(key);
 			else
 				return (String) value;
 		}
 		public String getInputString(String key, String[] acceptableValues)
 			throws MissingRequestParameterException, UnexpectedParameterValueException {
 
 			String value = getInputString(key, true);
 
 			for(int i =0;i<acceptableValues.length;i++) {
 				if (acceptableValues[i].equals(value))
 					return value;
 			}
 
 			throw new UnexpectedParameterValueException(value, acceptableValues);
 		}
 
 		public String[] getInputStringArray(String key) {
 			return _request.getParameterValues(key);
 		}
 
 
 
 		public String getProfileID() {
 			return (String) _session.getAttribute("loggedIn");
 		}
 		public Hashtable getProfile() {
 			return (Hashtable) _session.getAttribute("profile");
 		}
 
 		public String getLastAction() {
 			return (String) _session.getAttribute(LAST_ACTION_TOKEN);
 		}
 
 		public String getLastView() {
 			return (String) _session.getAttribute(LAST_VIEW_TOKEN);
 		}
 		private void setLastView(String lastView) {
 			setLastLastView(getLastView());
 			_session.setAttribute(LAST_VIEW_TOKEN, lastView);
 		}
 
 		public String getLastLastView() {
 			return (String) _session.getAttribute(LAST_LAST_VIEW_TOKEN);
 		}
 		private void setLastLastView(String lastView) {
 			_session.setAttribute(LAST_LAST_VIEW_TOKEN, lastView);
 		}
 
 		private void setLastAction(String lastAction) {
 			setLastLastAction(getLastAction());
 			_session.setAttribute(LAST_ACTION_TOKEN, lastAction);
 		}
 
 		public String getLastLastAction() {
 			return (String) _session.getAttribute(LAST_LAST_ACTION_TOKEN);
 		}
 
 		private void setLastLastAction(String lastAction) {
 			_session.setAttribute(LAST_LAST_ACTION_TOKEN, lastAction);
 		}
 
 		public void setReturnValue(Object returnValue) {
 			_session.setAttribute(RETURN_TOKEN, returnValue);
 		}
 
 		public void setSessionValue(String key, Object value) {
 			_session.setAttribute(key, value);
 		}
 		public Object getSessionValue(String key) {
 			return _session.getAttribute(key);
 		}
 		public void setError(String errorMsg) {
 			log.info("Error state set: "+errorMsg);
 			_session.setAttribute(ERR_TOKEN, errorMsg);
 		}
 		public void setError() {
 			log.info("Error state set");
 			_session.setAttribute(ERR_TOKEN, "Error processing request");
 		}
 
 		public void goToLastAction() {
 			goToView(getLastAction());
 		}
 
 		
 		
 		public void goToErrorView() {
 			if (_response.isCommitted()) {
 				displaySimpleErrorMessage();
 				return;
 			}
 			
 			log.debug("going to error view: " + _defaultErrorView);
 
 			// remember view as last view
 			setLastView(_defaultErrorView);
 
 			String url = (String) _views.get(_defaultErrorView);
 			try {
 				if (url != null) {
 					log.debug("Forwarding to default error view: " + url);
 					getServletConfig().getServletContext()
 							.getRequestDispatcher(url).forward(_request,
 									_response);
 				} else {
 
 					log.warn("Couldn't locate default error view: "
 							+ _defaultErrorView);
 					displayGlobalErrorPage();
 				}
 			} catch (Exception e) {
 				log.error("Exception caught forwarding to default error page: "
 						+ url + "; displaying global error page", e);
 				displayGlobalErrorPage();
 
 			}
 		}
 
 		private void displayGlobalErrorPage() {
 			try {
 				String url = "/Error.jsp";
 				getServletConfig().getServletContext()
 						.getRequestDispatcher(url).forward(_request, _response);
 			} catch (Exception e2) {
 
 				log.error("Unable to display global error page", e2);
 					displaySimpleErrorMessage();
 			}
 		}
 
 		private void displaySimpleErrorMessage() {
 			try {
 				PrintWriter out = _response.getWriter();
 				out
 						.print("A error has occured; the Campus Ministry IT team "
 								+ "has been notified.  If you need assistance, please email "
 								+ "help@campuscrusadeforchrist.com");
 			} catch (IOException ioe) {
 				log
 						.error(
 								"Unable to get output stream! Can't display error to user",
 								ioe);
 			}
 		}
 
 		public void goToView(String view) {
 			log.debug("going to view: " + view);
 
 			if (_views.get(view) != null)
 			{
 				// remember view as last view
 				setLastView(view);
 
 				String url = (String) _views.get(view);
 				goToURL(url);
 			} else {
 				log.error("Couldn't locate view: " + view);
 				goToErrorView();
 			}
 		}
 
 		public void goToLastView() {
 			log.debug("Going to last view");
 			String lastView = getLastView();
 			goToView(lastView);
 		}
 
 		public void goToURL(String url) {
 			try
 			{
 				log.debug("Forwarding to: " + url);
 				getServletConfig().getServletContext().getRequestDispatcher(url).forward(_request, _response);
 			}
 			catch (Exception e)
 			{
 				log.error("Exception forwarding to: " + url, e);
 				goToErrorView();
 				
 			}
 		}
 
 
 		public Hashtable getHashedRequest() {
 			Hashtable h = new Hashtable();
 
 			for (Enumeration enumer = _request.getParameterNames(); enumer.hasMoreElements();) {
 				String key = (String) enumer.nextElement();
 				h.put(key, _request.getParameter(key));
 			}
 
 			return h;
 		}
 
 		public String fetchId() {
 			String id = new String();
 			if (_request.getParameter("id") != null) {
 				id = _request.getParameter("id");
 			} else {
 				id = (String) _session.getAttribute("id");
 			}
 			return id;
 		}
 
 	};
 
 	public Controller()
 	{
 	}
 
 	/**
 	 *   IF YOU OVERRIDE THIS METHOD DON'T FORGET TO CALL THIS CODE
 	 */
 	public void init() {
 		log.debug("init() called on Controller");
 	}
 
 	/**
 	 *   IF YOU OVERRIDE THIS METHOD DON'T FORGET TO CALL THIS CODE
 	 */
 	public void initViews(String xmlViews)
 	{
 		log.debug("Parsing view file: '" + xmlViews + "'.");
 
 		_views = ViewsProcessor.parse(xmlViews);
 
 		for (Enumeration e = _views.keys(); e.hasMoreElements();) {
 			String k = (String) e.nextElement();
 			log.debug(k + "=" + _views.get(k));
 		}
 	}
 
 
 
 	public void setViewsFile(String viewsFile) {
 		_viewsFile = viewsFile;
 		initViews(_viewsFile);
 	}
 	public void setLog4JConfigFile(String logConfFile) {
 		JoranConfigurator configurator = new JoranConfigurator();
 		LoggerRepository repository = LogManager.getLoggerRepository();
 		configurator.doConfigure(logConfFile, repository);
 		
 	}
 
 	public String getViewsFile() { return _viewsFile; }
 
 	public String getDefaultAction() { return _defaultAction; }
 	public void setDefaultAction(String defaultAction) { _defaultAction = defaultAction; }
 
     public String getDefaultErrorView() { return _defaultErrorView; }
     public void setDefaultErrorView(String defaultErrorView) { _defaultErrorView = defaultErrorView; }
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) {
 
 		processRequest(request, response);
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) {
 
 		processRequest(request, response);
 	}
 
 		
 	/** Comment */
 	protected void processRequest(HttpServletRequest req, HttpServletResponse res) {
 
 		synchronized (this) {
 			_thread_ctr++;
 			if (_thread_ctr > 1)
 			{
 				log.debug("Thread count: " + _thread_ctr );
 			}
 		}
 
 		ActionContext ctx = new ActionContext(req, res);
 
 		String actionName = null;
 		try {
 
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
 			if(req.getParameter("action") == null){
 				log.debug("Invoking default action: " +_defaultAction );
 				actionName = _defaultAction;
 			} else {
 				String requestedAction = req.getParameter("action");
 				log.info("Invoking action: " + requestedAction);
 				actionName = requestedAction;
 			}
 			MDC.put("action", actionName);
 			NDC.push(actionName);
 
 			Map<String, Object> requestMap = ctx.getHashedRequest();
 			StringBuffer url = req.getRequestURL();
 
 			String lineSep = System.getProperty("line.separator");
 			lineSep = (lineSep == null ? "\n" : lineSep);
 			MDC.put("request", url.append(lineSep).append(requestMap.toString()).toString());
 			
 			LinkedList<String> history = (LinkedList<String>) req.getSession().getAttribute("history");
 			if (history == null)
 			{
 				history = new LinkedList<String>();
 				req.getSession().setAttribute("history", history);
 			}
 			String path = req.getRequestURI();
 			path = lineSep + path + " " + requestMap.toString() + lineSep;
 			history.add(path);
 
 			if (history.size() > MAX_HISTORY_SIZE) {
 				history.remove();
 			}
 			
 			HashMap<String, Object> sessionCopy = new HashMap<String, Object>();
 			for (Enumeration<String> attributeNames = (Enumeration<String>) req.getSession().getAttributeNames(); attributeNames.hasMoreElements();)
 			{
 				String attributeName = attributeNames.nextElement(); 
 				sessionCopy.put(attributeName, req.getSession().getAttribute(attributeName));
 			}
 			
 			MDC.put("session", sessionCopy.toString());
 			
 			
 			ctx.setLastAction(actionName);
 			Method action = this.getClass().getMethod(actionName,  new Class[] {ActionContext.class});
 			long beginTime = System.currentTimeMillis();
 			action.invoke(this, new Object[] {ctx});
 			actionInvoked(actionName, ctx);
 			long endTime = System.currentTimeMillis();
 			log.info("Finished action: " + actionName + " in " + (endTime - beginTime)+ " ms");
 			
 		} catch (java.lang.NoSuchMethodException e) {
			log.warn("Action doesn't exist", e );
 
 			ctx.setError();
 			ctx.goToErrorView();
 
         } catch (java.lang.Exception e) {
         	log.error("Error invoking action " + actionName, e );
         	ctx.setError();
             ctx.goToErrorView();
 		} finally {
 			NDC.pop();
 			NDC.pop();
 			MDC.remove("username");
 			MDC.remove("action");
 			MDC.remove("session");
 			MDC.remove("request");
 			MDC.remove("userIPAddress");
 			MDC.remove("machineName");
 			synchronized (this) { _thread_ctr--; }
 		}
 	}
 
 	/**
 	 *   Designed to be implemented by child classes
 	 *	  Called immediately after an action was called on a controller
 	 */
 	protected void actionInvoked(String action, ActionContext ctx) {}
 
 	/**
 	 *   Designed to be implemented by child classes
 	 *	  Called when reload action called
 	 */
 	protected void reload() throws Exception {}
 
 	public void health(ActionContext ctx)
 	{
 		try {
 			String value = ctx.getInputString("value", true);
 
 			ctx.getResponse().setHeader("response", value);
 
 		} catch (Exception e) {
 			log.error("Error getting health", e);
 			ctx.setError();
 		}
 	}
 
 	public void reload(ActionContext ctx)
 	{
 		try {
 			javax.servlet.ServletOutputStream out = ctx.getResponse().getOutputStream();
 			try {
 				reload();
 				out.println("Reload successful.");
 				log.info("Reload successful.");
 			} catch (Exception e) {
 				out.println("Reload failed!<BR>"+e.toString());
 				log.error("Reload failed!", e);
 				ctx.setError();
 			}
 		} catch (Exception ignore) {}
 	}
 }
