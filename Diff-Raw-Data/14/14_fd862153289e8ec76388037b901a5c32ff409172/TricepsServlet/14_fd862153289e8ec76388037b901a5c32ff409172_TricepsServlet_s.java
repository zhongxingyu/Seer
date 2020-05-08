 package org.dianexus.triceps;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.PrintWriter;
 import java.util.Date;
 
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.logging.*;
 
 /**
 The main HttpServlet page
  */
 public class TricepsServlet extends HttpServlet implements VersionIF {
 
     private Logger logger;
     static final long serialVersionUID = 0;
     static final String TRICEPS_ENGINE = "TricepsEngine";
     static final String USER_AGENT = "User-Agent";
     static final String ACCEPT_LANGUAGE = "Accept-Language";
     static final String ACCEPT_CHARSET = "Accept-Charset";
     static final String CONTENT_TYPE = "text/html; charset=UTF-8";	// can make UTF-8 by default?
     static final String CHARACTER_ENCODING = "UTF-8";
 
     /* Strings for storing / retrieving state of authentication */
     static final String LOGIN_TOKEN = "_DlxLTok";
     static final String LOGIN_COMMAND = "_DlxLCom";
     static final String LOGIN_COMMAND_LOGON = "logon";
     static final String LOGIN_IP = "_DlxLIP";
     static final String LOGIN_USERNAME = "_DlxUname";
     static final String LOGIN_PASSWORD = "_DlxPass";
     static final String LOGIN_RECORD = "_DlxLRec";
     static final String LOGIN_BROWSER = "_DlxBrws";
 
     /* Strings serving as messages for login error pages - these should really be JSP */
     static final int LOGIN_ERR_NO_TOKEN = 0;
     static final int LOGIN_ERR_NEW_SESSION = 1;
     static final int LOGIN_ERR_MISSING_UNAME_OR_PASS = 2;
     static final int LOGIN_ERR_INVALID_UNAME_OR_PASS = 3;
     static final int LOGIN_ERR_INVALID = 4;
     static final int LOGIN_ERR_ALREADY_COMPLETED = 5;
     static final int LOGIN_ERR_UNABLE_TO_LOAD_FILE = 6;
     static final int LOGIN_ERR_EXPIRED_SESSION = 7;
     static final int LOGIN_ERR_INVALID_RELOGON = 8;
     static final int LOGIN_ERR_UNSUPPORTED_BROWSER = 9;
     static final int LOGIN_ERR_OK = 10;
     static final int LOGIN_ERR_FINISHED = 11;
     static final int LOGIN_ERR_ODD_CHANGE = 12;
     static final String[] LOGIN_ERRS_BRIEF = {
         " LOGIN_ERR_NO_TOKEN",
         " LOGIN_ERR_NEW_SESSION",
         " LOGIN_ERR_MISSING_UNAME_OR_PASS",
         " LOGIN_ERR_INVALID_UNAME_OR_PASS",
         " LOGIN_ERR_INVALID",
         " LOGIN_ERR_ALREADY_COMPLETED",
         " LOGIN_ERR_UNABLE_TO_LOAD_FILE",
         " LOGIN_ERR_EXPIRED_SESSION",
         " LOGIN_ERR_INVALID_RELOGON",
         " LOGIN_ERR_UNSUPPORTED_BROWSER",
         " OK",
         " FINISHED",
         " LOGIN_ERR_ODD_CHANGE"
     ,
 	        };
 
 	/**
 		These logging mesages should be internationalized
 	*/
 	static final String[] LOGIN_ERRS_VERBOSE = {
 		"Please login",
          "Please login",
          "Please enter both your username and password",
          "The username or password you entered was incorrect",
          "Please login again --  You will resume from where you left off.<br><br>(Your login session was invalidated either because you accidentally pressed the browser's back button instead of the 'previous' button; or you attempted to use a bookmarked page from the instrument; or you triple-clicked an icon or button)",
          "Thank you!  You have already completed this instrument.",
          "Please contact the administrator -- the program was unable to load the interview: ",
          "Please login again -- You will resume from where you left off.<br><br>(Your session expired, either because of prolonged inactivity, or because the server was restarted)",
          "Please login again --  You will resume from where you left off.<br><br>(Your login session was invalidated because the login page was submitted twice)",
          "Please login again -- You will resume from where you left off.<br><br>There was an unexpected error from your browser",
          "OK",
          "Thank you for completing this instrument",
          "Please login again -- You will resume from where you left off.<br><br>(There was an unexpected network error)"
     ,
 	      };
         
     /**
     Initialize the servlet by starting up the logging functions
      */
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         logger = Logger.getLogger("org.dianexus.triceps.TricepsServlet");
     //	org.dianexus.triceps.Logger.init(config.getInitParameter("dialogix.dir"));
     }
 
     /**
     Destroy the servlet
      */
     public void destroy() {
         super.destroy();
     }
 
     /**
     Process HTTP GET requests, referring them to doPost
      */
     public void doGet(HttpServletRequest req,
                        HttpServletResponse res) {
         doPost(req, res);
     }
 
     /**
     Process HTTP POST requests.  Handles all possible interactions and returns the next logical page.<br>
     Should really be replaced with a web framework, security, etc.
      */
     public void doPost(HttpServletRequest req,
                         HttpServletResponse res) {
         try {
             req.setCharacterEncoding(TricepsServlet.CHARACTER_ENCODING);
             res.setCharacterEncoding(TricepsServlet.CHARACTER_ENCODING);
             if (!initSession(req, res)) {
                 throw new RuntimeException("Unable to Init Session");
             }
 
             int result = LOGIN_ERR_OK;
             result = okPage(req, res);
         } catch (Exception t) {
             logger.log(Level.SEVERE, "", t);
             expiredSessionErrorPage(req,res);
         }
     }
 
     /**
     Handle all page requests.  Calls tricepsEngine.doPost()
      */
     private int okPage(HttpServletRequest req,
                         HttpServletResponse res) {
         HttpSession session = req.getSession(false);
 
         logAccess(req, " OK");
 
         try {
             res.setContentType(CONTENT_TYPE);
             PrintWriter out = res.getWriter();
 
             TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
             HashMap<String,String> requestParameters = new HashMap<String,String>();
             Enumeration<String> params = req.getParameterNames();
             while (params.hasMoreElements()) {
                 String key = params.nextElement();
                 requestParameters.put(key, req.getParameter(key));
             }
             
             tricepsEngine.doPost(requestParameters, 
                 res.encodeURL(req.getRequestURL().toString()), 
                 out, 
                 null,
                 ((req == null) ? null : req.getRemoteAddr()),
                 req.isSecure(),
                 req.getHeader(USER_AGENT),
                 null);
 
             out.flush();
             out.close();
 
             /* disable session if completed */
             if (tricepsEngine != null && tricepsEngine.isFinished()) {  
                 logAccess(req, " FINISHED");
                 shutdown(req, LOGIN_ERRS_BRIEF[LOGIN_ERR_FINISHED], false);	// if don't remove the session, can't login as someone new
                 return -1;
             }
         } catch (Exception t) {
             logger.log(Level.SEVERE, "", t);
         }
         return LOGIN_ERR_OK;
     }
 
     /**
     Log information about the access request
      */
     void logAccess(HttpServletRequest req,
                    String msg) {
         HttpSession session = req.getSession(false);
         TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 //        if (DB_LOG_MINIMAL) {
 //            if (tricepsEngine != null) tricepsEngine.getTriceps().getTtc().setStatusMsg(msg);
 //        }
        if (DB_LOG_FULL) {
            if (tricepsEngine != null) tricepsEngine.getTriceps().getDtc().setStatusMsg(msg);
         }
 
         if (logger.isLoggable(Level.FINE)) {
             /* 2/5/03:  Explicitly ask for session info everywhere (vs passing it as needed) */
             String sessionID = session.getId();
             Runtime rt = Runtime.getRuntime();
             long used = (rt.totalMemory() - rt.freeMemory());
             double kb = Math.floor(used / 1000);
             double mb = kb / 1000;
             String memoryUsed = Double.toString(mb) + "MB";
 
             /* standard Apache log format (after the #@# prefix for easier extraction) */
             /*
             logger.log(Level.FINE, "#@#(" + req.getParameter("DIRECTIVE") + ") [" + new Date(System.currentTimeMillis()) + "] " +
                 sessionID +
                 ((WEB_SERVER) ? (" " + req.getRemoteAddr() + " \"" +
                 req.getHeader(USER_AGENT) + "\" \"" + req.getHeader(ACCEPT_LANGUAGE) + "\" \"" + req.getHeader(ACCEPT_CHARSET) + "\"") : "") +
                 ((tricepsEngine != null) ? tricepsEngine.getScheduleStatus() : "") + msg + " " + (req.isSecure() ? "HTTPS" : "HTTP") +
                 " [" + rt.totalMemory() + ", " + rt.freeMemory() + "]");
              */
             logger.log(Level.FINE, "#@#(" + req.getParameter("DIRECTIVE") + ") [" + new Date(System.currentTimeMillis()) + "] " +
                 sessionID +
                 "[" + memoryUsed + "]" + 
                 ((tricepsEngine != null) ? tricepsEngine.getScheduleStatus() : "") + msg
                 );
         }
     }
 
     /**
     Session expired, show appropriate page.  Should be internationalized
      */
     void expiredSessionErrorPage(HttpServletRequest req,
                                  HttpServletResponse res) {
         logAccess(req, " EXPIRED SESSION");
         try {
             res.setContentType(CONTENT_TYPE);
             PrintWriter out = res.getWriter();
 
             out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>");
             out.println("<html>");	// may not know locale in this case - should be replaced by web management framework
             out.println("<head>");
             out.println("<META HTTP-EQUIV='Content-Type' CONTENT='" + CONTENT_TYPE + "'>");
             out.println("<title>Triceps Error-Expired Session</title>");
             out.println("</head>");
             out.println("<body bgcolor='white'>");
             out.println("   <table border='0' cellpadding='0' cellspacing='3' width='100%'>");
             out.println("      <tr>");
             out.println("         <td width='1%'><img name='icon' src='/images/dialogo.jpg' align='top' border='0' alt='Logo' /> </td>");
             out.println("         <td align='left'><font SIZE='4'>Sorry for the inconvenience, but web session you were using is no longer valid.  Either you finished an instrument, the session ran out of time, or the server was restarted.");
             if (!WEB_SERVER) {
                 out.print("  You can resume the instrument from where you left off by clicking <a href=\"JavaScript:void;\"");
                 out.print(" onclick=\"JavaScript:window.top.open('");
                 out.print(res.encodeURL(req.getRequestURL().toString()));
                 out.print("','_blank','resizable=yes,scrollbars=yes');JavaScript:top.close();\">here</a>");
                 out.print(" and selecting it from the RESTORE list.");
             }
             out.println("</font></td>");
             out.println("      </tr>");
             out.println("   </table>");
             out.println("</body>");
             out.println("</html>");
 
             out.flush();
             out.close();
         } catch (Exception t) {
             logger.log(Level.SEVERE, "", t);
         }
     }
 
     /**
     Shutdown session gracefully
      */
     void shutdown(HttpServletRequest req,
                   String msg,
                   boolean createNewSession) {
         /* want to invalidate sessions -- even though this confuses the log issue on who is accessing from where, multiple sessions can indicate problems with user interface */
         /* 2/5/03:  Explicitly ask for session info everywhere (vs passing it as needed) */
         HttpSession session = req.getSession(false);
         String sessionID = session.getId();
 
         logger.log(Level.FINE, "...discarding session: " + sessionID + ":  " + msg);
 
         TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 
         if (tricepsEngine != null) {
             tricepsEngine.getTriceps().shutdown();
             // code added by Gary Lyons 12/12/06 to fix memory leak
             tricepsEngine.releaseTriceps();
         }
 
         try {
             if (session != null) {
                 session.invalidate();	// so that retrying same session gives same message
             }
 
             sessionID = null;
 
             if (createNewSession) {
                 /* Finally, create a new session so that session so that it is available, and so that session time-outs can be detected */
                 /* this cannot be done after the page is sent? */
                 session = req.getSession(true);	// the only place to create new sessions
             }
         } catch (java.lang.IllegalStateException e) {
             logger.log(Level.SEVERE, "", e);
         }
     }
 
     /**
     Start a session, creating a new one if needed
      */
     boolean initSession(HttpServletRequest req,
                         HttpServletResponse res) {
         try {
             HttpSession session = req.getSession(true);
 
             if (session == null || session.isNew()) {
                 if ("POST".equals(req.getMethod())) {
                     /* an expired session */
                     logAccess(req, LOGIN_ERRS_BRIEF[LOGIN_ERR_EXPIRED_SESSION]);
                     expiredSessionErrorPage(req, res);	// this should really be a redirect to a language neutral page
                     return false;
                 }
             /* otherwise this is a session that requires a login page? */
             }
             TricepsEngine tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
             if (tricepsEngine == null) {
                 ServletConfig config = this.getServletConfig();
                 HashMap<String,String> initParams = new HashMap<String,String>();
                 Enumeration<String> keys =  config.getInitParameterNames();
                 while (keys.hasMoreElements()) {
                     String key = keys.nextElement();
                     initParams.put(key, config.getInitParameter(key));
                 }
                 tricepsEngine = new TricepsEngine(initParams);
                 session.setAttribute(TRICEPS_ENGINE, tricepsEngine);
             }
             return true;
         } catch (Exception e) {
             logger.log(Level.SEVERE, "", e);
             return false;
         }
     }
 }
