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
 
 import java.io.*;
 import java.net.*;
 import java.rmi.RemoteException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.MessagingException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import javax.xml.rpc.ServiceException;
 
 import om.*;
 import om.OmException;
 import om.axis.qengine.*;
 import om.tnavigator.auth.Authentication;
 import om.tnavigator.db.DatabaseAccess;
 import om.tnavigator.db.OmQueries;
 import om.tnavigator.reports.ReportDispatcher;
 import om.tnavigator.scores.CombinedScore;
 
 import org.apache.axis.AxisFault;
 import org.w3c.dom.*;
 
 import util.misc.*;
 import util.xml.*;
 
 /** Om test navigator; implementation of the test delivery engine. */
 public class NavigatorServlet extends HttpServlet
 {
 	private static final String INPUTTOOLONG="Input too long";
 	private static final String SEQUENCEFIELD="sequence";
 	private static final String COOKIENAME="tnavigator_session";
 	private static final String ACCESSCOOKIENAME="tnavigator_access";
 	private static final String FAKEOUCUCOOKIENAME="tnavigator_xid";
 
 	private static final String SUMMARYTABLE_NOTANSWERED="Not completed";
 
 	/** User passed on question. Should match the defitition in om.question.Results. */
 	public final static int ATTEMPTS_PASS=0;
 	/** User got question wrong after all attempts. Should match the defitition in om.question.Results. */
 	public final static int ATTEMPTS_WRONG=-1;
 	/** User got question partially correct after all attempts. Should match the defitition in om.question.Results. */
 	public final static int ATTEMPTS_PARTIALLYCORRECT=-2;
 	/** If developer hasn't set the value. Should match the defitition in om.question.Results. */
 	public final static int ATTEMPTS_UNSET=-99;
 
 	private static final int MAGIC_RANDOM_SEED_INCREMENT = 12637946;
 
 	/** Database access */
 	DatabaseAccess da;
 	/** @return the DatabaseAccess object used by this servlet. */
 	public DatabaseAccess getDatabaseAccess() { return da; }
 
 	/** Authentication system */
 	private Authentication auth;
 
 	/** Config file contents */
 	private NavigatorConfig nc;
 	/** @return the NavigatorConfig for this servlet. */
 	public NavigatorConfig getNavigatorConfig() { return nc; }
 
 	/** Question bank folder */
 	private File questionBankFolder;
 
 	/** Map of cookie value (String) -> UserSession */
 	private Map<String,UserSession> sessions=new HashMap<String,UserSession>();
 
 	/** Map of OUCU-testID (String) -> UserSession */
 	private Map<String,UserSession> usernames=new HashMap<String,UserSession>();
 
 	/** Map of OUCU-testID (String) -> Long (date that prohibition expires) */
 	private Map<String,Long> tempForbid=new HashMap<String,Long>();
 
 	/** Log */
 	private Log l;
 	/** @return the log for this servlet. */
 	public Log getLog() { return l; }
 
 	/** Load balancer for Om question engines */
 	private OmServiceBalancer osb;
 	int checkOmServiceAvailable() throws RemoteException { return osb.checkAvailable(); }
 
 	/** Reports-handling code */
 	private ReportDispatcher reports;
 
 	/** Status page display code */
 	private StatusPages status=new StatusPages(this);
 
 	/** SQL queries */
 	private OmQueries oq;
 
 	/** @return Folder used for template files */
 	public File getTemplatesFolder()
 	{
 		return new File(getServletContext().getRealPath("WEB-INF/templates"));
 	}
 
 	@Override
 	public void init() throws ServletException
 	{
 		ServletContext sc=getServletContext();
 
 		try
 		{
 			maintenanceFile=new File(
 					sc.getRealPath("maintenance.xhtml"));
 
 			nc=new NavigatorConfig(new File(
 				sc.getRealPath("navigator.xml")));
 		}
 		catch(MalformedURLException e)
 		{
 			throw new ServletException("Unexpected error parsing service URL",e);
 		}
 		catch(IOException e)
 		{
 			throw new ServletException("Error loading config file",e);
 		}
 
 		try
 		{
 			l=new Log(new File(sc.getRealPath("logs")),"navigator",
 				nc.hasDebugFlag("log-general"));
 		}
 		catch(IOException e)
 		{
 			throw new ServletException("Error creating log",e);
 		}
 
 		try
 		{
 			osb=new OmServiceBalancer(nc.getOmServices(),l,nc.hasDebugFlag("log-balancer"));
 		}
 		catch(ServiceException e)
 		{
 			throw new ServletException("Unexpected error obtaining service",e);
 		}
 
 		questionBankFolder=new File(
 			sc.getRealPath("questionbank"));
 
 		lastSessionKillerError=new long[nc.getOtherNavigators().length];
 
 		String dbClass=nc.getDBClass();
 		String dbPrefix=nc.getDBPrefix();
 		try
 		{
 			oq=(OmQueries)Class.forName(dbClass).getConstructor(new Class[] {String.class}).
 					newInstance(new Object[] {dbPrefix});
 			da=new DatabaseAccess(nc.getDatabaseURL(oq),
 				nc.hasDebugFlag("log-sql") ? l : null);
 		}
 		catch(Exception e)
 		{
 			throw new ServletException(
 				"Error creating database class or JDBC driver (make sure DB plugin and JDBC driver are both installed): "+e.getMessage());
 		}
 
 		DatabaseAccess.Transaction dat=null;
 		try
 		{
 			dat=da.newTransaction();
 			oq.checkTables(dat);
 		}
 		catch(Exception e)
 		{
 			throw new ServletException(
 				"Error initialising database tables: "+e.getMessage(),e);
 		}
 		finally
 		{
 			if(dat!=null)	dat.finish();
 		}
 
 		String authClass=nc.getAuthClass();
 		try
 		{
 			// Note: can't change this to another class without fixing handleStatusHome as well
 			auth=(Authentication)Class.forName(authClass).
 				getConstructor(new Class[] {NavigatorServlet.class}).
 				newInstance(new Object[] {this});
 		}
 		catch(Exception e)
 		{
 			throw new ServletException("Error creating authentication class: "+authClass,e);
 		}
 
 		try
 		{
 			plainTemplate=XML.parse(new File(sc.getRealPath("WEB-INF/templates/plaintemplate.xhtml")));
 			template=XML.parse(new File(sc.getRealPath("WEB-INF/templates/template.xhtml")));
 			singlesPlainTemplate=XML.parse(new File(sc.getRealPath("WEB-INF/templates/singlesplaintemplate.xhtml")));
 			singlesTemplate=XML.parse(new File(sc.getRealPath("WEB-INF/templates/singlestemplate.xhtml")));
 		}
 		catch(IOException ioe)
 		{
 			throw new ServletException("Failed to initialise XML templates",ioe);
 		}
 
 		try
 		{
 			reports = new ReportDispatcher(this, Arrays.asList(nc.getExtraReports()));
 		}
 		catch(Exception e)
 		{
 			throw new ServletException("Error creating report classes",e);
 		}
 
 		// Start expiry thread
 		sessionExpirer=new SessionExpirer();
 	}
 
 	/** How long an unused session lurks around before expiring (8 hrs) */
 	private final static int SESSIONEXPIRY=8 * 60 * 60 * 1000;
 
 	/** How often we check for expired sessions (15 mins) */
 	private final static int SESSIONCHECKDELAY=15*60*1000;
 
 	/** Session expiry thread */
 	private SessionExpirer sessionExpirer;
 
 	/** Thread that gets rid of unused sessions */
 	class SessionExpirer extends PeriodicThread
 	{
 		SessionExpirer()
 		{
 			super(SESSIONCHECKDELAY);
 		}
 		@Override
 		protected void tick()
 		{
 			synchronized(sessions)
 			{
 				// See if any sessions need expiring
 				long lNow=System.currentTimeMillis();
 				long lExpiry=lNow - SESSIONEXPIRY;
 
 				for(Iterator i=sessions.values().iterator();i.hasNext();)
 				{
 					UserSession us=(UserSession)i.next();
 					if(us.getLastActionTime() < lExpiry)
 					{
 						i.remove();
 						usernames.remove(us.sCheckedOUCUKey);
 					}
 				}
 
 				for(Iterator i=tempForbid.values().iterator();i.hasNext();)
 				{
 					if(lNow > ((Long)i.next()).longValue())
 					{
 						i.remove();
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void destroy()
 	{
 		// Kill expiry thread
 		sessionExpirer.close();
 
 		// Close SAMS and kill their threads
 		auth.close();
 
 		// Close database connections
 		da.close();
 
 		// Close log
 		l.close();
 
 		// Tell shutdown manager we've shut down. (I don't think this is necessary,
 		// it kind of got copied from Promises, but could potentially be useful
 		// for clearing static data...)
 		ShutdownManager.shutdown();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request,HttpServletResponse response)
 		throws ServletException,IOException
 	{
 		handle(false,request,response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,HttpServletResponse response)
 		throws ServletException,IOException
 	{
 		handle(true,request,response);
 	}
 
 	/**
 	 * Initialises the basic information in a UserSession that relates to a
 	 * particular test. Before calling this, you must initialise the random
 	 * seed.
 	 * @param us User session
 	 * @param rt Used for storing performance information
 	 * @param sTestID Test ID
 	 * @param request HTTP request
 	 * @param response HTTP response (used if they don't have access)
 	 * @param bFinished Whether the students has finished this test.
 	 * @param bStarted Whether the students has already started this test.
 	 * @throws StopException If something was sent to user
 	 * @throws OmException Any error
 	 */
 	private void initTestSession(UserSession us, RequestTimings rt,
 			String sTestID, HttpServletRequest request, HttpServletResponse response,
 			boolean bFinished, boolean bStarted, long randomSeed, int fixedVariant) throws Exception
 	{
 		// Check access
 		if(!us.getTestDeployment().isWorldAccess() && !us.getTestDeployment().hasAccess(us.ud))
 		{
 			sendError(us,request,response,
 				HttpServletResponse.SC_FORBIDDEN,false,false, null, "Access denied", "You do not have access to this test.", null);
 		}
 		us.bAdmin=us.getTestDeployment().isAdmin(us.ud);
 		us.bAllowReports=us.getTestDeployment().allowReports(us.ud);
 
 		if(!us.bAdmin && !us.getTestDeployment().isAfterOpen())
 		{
 			sendError(us,request,response,
 				HttpServletResponse.SC_FORBIDDEN,false,false, null, "Test not yet available", "This test is not yet available.", null);
 		}
 		// If the test is finished, we allow them through even after the forbid date
 		// so they can see their results.
 		if(!us.bAdmin && us.getTestDeployment().isAfterForbid() && !us.isFinished() && !us.bAllowAfterForbid)
 		{
 			if(us.getTestDeployment().isAfterForbidExtension() || !bStarted)
 			{
 				sendError(us,request,response,
 					HttpServletResponse.SC_FORBIDDEN,false,false, null, "Test no longer available", "This test is no longer available to students.", null);
 			}
 			else
 			{
 				// OK, they have an unfinished test. Offer them the end page
 				handleEnd(rt,false,true,us,request,response);
 				throw new StopException();
 			}
 		}
 
 		// Realise the test for this student.
 		// Initialise test settings
 		us.realiseTest(sTestID, bFinished, randomSeed, fixedVariant);
 	}
 
 	private static class StopException extends OmException
 	{
 		StopException()
 		{
 			super("Stopped");
 		}
 	}
 
 	/**
 	 * Class for accumulating performance information.
 	 */
 	public static class RequestTimings
 	{
 		long lStart,lDatabaseElapsed,lQEngineElapsed;
 	}
 
 	/** Tracks when last error occurred while sending forbids to each other nav */
 	private long[] lastSessionKillerError;
 	private Object sessionKillerErrorSynch=new Object();
 
 	/** Thread used to 'kill' a session on other servers by sending forbid calls */
 	private class RemoteSessionKiller extends Thread
 	{
 		private String sOUCUTest;
 
 		RemoteSessionKiller(String sOUCUTest)
 		{
 			this.sOUCUTest=sOUCUTest;
 			start();
 		}
 
 		@Override
 		public void run()
 		{
 			String[] asURL=nc.getOtherNavigators();
 			for(int i=0;i<asURL.length;i++)
 			{
 				try
 				{
 					URL u=new URL(asURL[i]+"!forbid/"+sOUCUTest);
 					HttpURLConnection huc=(HttpURLConnection)u.openConnection();
 					HTTPS.allowDifferentServerNames(huc);
 					HTTPS.considerCertificatesValid(huc);
 
 					huc.connect();
 					if(huc.getResponseCode()!=HttpServletResponse.SC_OK)
 						throw new IOException("Error with navigator "+huc.getResponseCode());
 					IO.eat(huc.getInputStream());
 					if(lastSessionKillerError[i]!=0)
 					{
 						synchronized(sessionKillerErrorSynch)
 						{
 							lastSessionKillerError[i]=0;
 						}
 						// Because we only display errors once per hour, better display the
 						// 'OK' state too if it was marked as error before
 						l.logNormal("Forbids",asURL[i]+": Forbid call OK now");
 					}
 				}
 				catch(IOException ioe)
 				{
 					// Display an error once per hour if it's still erroring
 					long lNow=System.currentTimeMillis();
 					synchronized(sessionKillerErrorSynch)
 					{
 						if(lastSessionKillerError[i]==0 || (lNow - lastSessionKillerError[i]) > 60*60*1000)
 						{
 							l.logWarning("Forbids",asURL[i]+": Forbid call failed",ioe);
 							lastSessionKillerError[i]=lNow;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Obtains cookie based on its name.
 	 * @param request HTTP request
 	 * @param sName Name of cookie
 	 * @return Cookie value
 	 */
 	private String getCookie(HttpServletRequest request,String sName)
 	{
 		Cookie[] ac=request.getCookies();
 		if(ac==null) ac=new Cookie[0];
 		for(int iCookie=0;iCookie<ac.length;iCookie++)
 		{
 			if(ac[iCookie].getName().equals(sName))
 				return ac[iCookie].getValue();
 		}
 		return null;
 	}
 
 	/**
 	 * List of NewSession objects that are stored to check we don't start a new
 	 * session twice in a row to same address (= cookies off)
 	 */
 	private LinkedList<NewSession> cookiesOffCheck=new LinkedList<NewSession>();
 	private static class NewSession
 	{
 		long lTime;
 		String sAddr;
 	}
 
 	/**
 	 * File that is included to put up a maintenance message during problem
 	 * periods.
 	 */
 	private File maintenanceFile;
 
 	/** Just to stop us checking the file more than once in 3 seconds */
 	private long lastMaintenanceCheck=0;
 
 	private final static int MAINTENANCECHECK_LATENCY=3000;
 
 	/**
 	 * Checks whether the maintenance-mode file exists. If it does then it is
 	 * loaded and displayed.
 	 * @param request
 	 * @param response
 	 */
 	private void checkMaintenanceMode(HttpServletRequest request,HttpServletResponse response)
 		throws StopException
 	{
 		// Treat as OK up to 3 seconds since last check
 		if(System.currentTimeMillis() - lastMaintenanceCheck < MAINTENANCECHECK_LATENCY)
 			return;
 		// File still there? OK
 		if(!maintenanceFile.exists())
 		{
 			lastMaintenanceCheck=System.currentTimeMillis();
 			return;
 		}
 		// They have the magic cookie or are requesting it?
 		if("/!letmein".equals(request.getPathInfo()))
 		{
 			Cookie c=new Cookie("openmark-letmein","pretty please!");
 			c.setPath("/");
 			response.addCookie(c);
 			response.setContentType("text/plain");
 			try
 			{
 				response.getWriter().println("OK, you're in! (Close the browser entirely, " +
 						"or clear session cookies, to lock yourself back out.)");
 			}
 			catch (IOException e)
 			{
 			}
 			throw new StopException();
 		}
 		else if(getCookie(request,"openmark-letmein")!=null)
 		{
 			return;
 		}
 
 		// No file!
 		lastMaintenanceCheck=0;
 		try
 		{
 			Document d=XML.parse(maintenanceFile);
 			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
 
 			XHTML.output(d,request,response,"en");
 		}
 		catch(Throwable t)
 		{
 			response.setContentType("text/plain");
 			try
 			{
 				t.printStackTrace(response.getWriter());
 			}
 			catch (IOException e)
 			{
 			}
 		}
 		throw new StopException();
 	}
 
 	private void handle(boolean bPost,
 		HttpServletRequest request,HttpServletResponse response)
 	{
 		RequestTimings rt=new RequestTimings();
 		rt.lStart=System.currentTimeMillis();
 		UserSession us=null;
 		String sPath=null;
 		try
 		{
 			// Vitally important, otherwise any input with unicode gets screwed up
 			request.setCharacterEncoding("UTF-8");
 
 			// Check path
 			sPath=request.getPathInfo();
 			if(sPath==null) sPath="";
 
 			// Handle question requests separately as they're not from users [from QE]
 			if(!bPost && sPath.startsWith("/!question/"))
 			{
 				handleQuestion(sPath.substring("/!question/".length()),request,response);
 				return;
 			}
 			// Handle session-forbid requests [from other TNs]
 			if(!bPost && sPath.startsWith("/!forbid/"))
 			{
 				handleForbid(sPath.substring("/!forbid/".length()),request,response);
 				return;
 			}
 
 			// Handle status requests
 			if(!bPost && sPath.startsWith("/!status/"))
 			{
 				status.handle(sPath.substring("/!status/".length()),request,response);
 				return;
 			}
 
 			checkMaintenanceMode(request,response);
 
 			// Allow shared files to everyone too
 			if(!bPost && sPath.startsWith("/!shared/"))
 			{
 				handleShared(sPath.substring("/!shared/".length()),request,response);
 				return;
 			}
 			if(!bPost && sPath.startsWith("/navigator.") && sPath.endsWith(".css"))
 			{
 				if(sPath.equals("/navigator.css"))
 					handleNavigatorCSS(null,request,response);
 				else
 					handleNavigatorCSS(sPath.substring("/navigator.".length(),sPath.length()-".css".length()),request,response);
 				return;
 			}
 
 			// Handle requests that go via the authentication system
 			if(sPath.startsWith("/!auth/"))
 			{
 				if(!auth.handleRequest(sPath.substring("/!auth/".length()),request,response))
 					throw new Exception("Requested URL is not handled by authentication plugin");
 				return;
 			}
 
 			// Handle special test user requests
 			if(!bPost && sPath.startsWith("/!test/"))
 			{
 				handleTestCookie(sPath.substring("/!test/".length()),request,response);
 				return;
 			}
 
 			// Handle system report requests
 			if(!bPost && sPath.startsWith("/!report/"))
 			{
 				reports.handleReport(sPath.substring("/!report/".length()),request,response);
 				return;
 			}
 
 			// Get test ID and determine type of request
 			Pattern pURL=Pattern.compile("^/([^/]+)/?(.*)$");
 			Matcher m=pURL.matcher(sPath);
 			if(!m.matches())
 			{
 				sendError(us,request,response,
 					HttpServletResponse.SC_NOT_FOUND,false, false, null, "Not found", "The URL you requested is not provided by this server.", null);
 			}
 			String sTestID=m.group(1),sCommand=m.group(2);
 
 			if ("".equals(sCommand) && !request.getRequestURI().endsWith("/"))
 			{
 				response.sendRedirect(request.getRequestURI()+"/");
 				return;
 			}
 			if(sCommand.startsWith("_")) sCommand=""; // Used to allow random different URLs in plain mode
 			if(request.getQueryString()!=null) sCommand+="?"+request.getQueryString();
 
 			// Get OUCU (null if no SAMS cookie), used to synch sessions for
 			// single user
 			String sOUCU=auth.getUncheckedUserDetails(request).getUsername();
 
 			// See if they've got a cookie for this test
 			String sTestCookieName=COOKIENAME+"_"+sTestID;
 			String sCookie=getCookie(request,sTestCookieName);
 
 			// See if they've got a fake OUCU (null if none)
 			String sFakeOUCU=getCookie(request,FAKEOUCUCOOKIENAME);
 
 			// Auth hash
 			int iAuthHash=
 				(auth.getUncheckedUserDetails(request).getCookie()+"/"+
 				sFakeOUCU).hashCode();
 
 			// If they haven't got a cookie or it's unknown, assign them one and
 			// redirect.
 			boolean bNewCookie=false,bTempForbid=false;
 			String sKillOtherSessions=null;
 			synchronized(sessions)
 			{
 				// Remove entries from cookies-off list after 1 second
 				while(!cookiesOffCheck.isEmpty() &&
 						rt.lStart - (cookiesOffCheck.getFirst()).lTime > 1000)
 				{
 					cookiesOffCheck.removeFirst();
 				}
 
 				// Check whether they already have a session or not
 				if(sCookie==null || !sessions.containsKey(sCookie))
 				{
 					// No session, need a new one
 					bNewCookie=true;
 				}
 				else
 				{
 					// Get session
 					us=sessions.get(sCookie);
 
 					// Check cookies in case they changed
 					if(us.iAuthHash!=0 && us.iAuthHash!=iAuthHash)
 					{
 						// If credentials change, they need a new session
 						sessions.remove(sCookie);
 						bNewCookie=true;
 					}
 				}
 
 				// New sessions!
 				if(bNewCookie)
 				{
 					String sAddr=request.getRemoteAddr();
 
 					// Check if we've already been redirected
 					for(Iterator i=cookiesOffCheck.iterator();i.hasNext();)
 					{
 						NewSession ns=(NewSession)i.next();
 						if(ns.sAddr.equals(sAddr))
 						{
 							sendError(null,request,response,HttpServletResponse.SC_FORBIDDEN,
 									false,
 									false,null, "Unable to create session cookie", "In order to use this website you must enable cookies in your browser settings.", null);
 						}
 					}
 
 					// Record this redirect so that we notice if it happens twice
 					NewSession ns=new NewSession();
 					ns.lTime=rt.lStart;
 					ns.sAddr=sAddr;
 					cookiesOffCheck.addLast(ns);
 
 					do
 					{
 						// Make 7-letter random cookie
 						sCookie = Strings.randomAlNumString(7);
 					}
 					while(sessions.containsKey(sCookie)); // And what are the chances of that?
 
 					us=new UserSession(this, sCookie);
 					sessions.put(sCookie,us);
 					// We do the actual redirect later on outside this synch
 
 					// At same time as creating new session, if they're logged in supposedly,
 					// check it's for real. If their cookie doesn't authenticated, this will
 					// cause the cookie to be removed and avoid multiple redirects.
 					if(sOUCU!=null)
 					{
 						if(!auth.getUserDetails(request,response,false).isLoggedIn())
 						{
 							// And we need to set this to zero to reflect that we just wiped
 							// their cookie.
 							iAuthHash=0;
 						}
 					}
 				}
 
 				// If this is the first time we've had an OUCU for this session, check
 				// it to make sure we don't need to ditch any other sessions
 				if(us.sCheckedOUCUKey==null && sOUCU!=null)
 				{
 					us.sCheckedOUCUKey=sOUCU+"-"+sTestID;
 
 					// Check the temp-forbid list
 					Long lTimeout=tempForbid.get(us.sCheckedOUCUKey);
 					if(lTimeout!=null && lTimeout.longValue() > System.currentTimeMillis())
 					{
 						// Kill session from main list & mark it to send error message later
 						sessions.remove(us.sCookie);
 						bTempForbid=true;
 					}
 					else
 					{
 						// If it was a timed-out forbid, get rid of it
 						if(lTimeout!=null) tempForbid.remove(us.sCheckedOUCUKey);
 
 						// Put this in the OUCU->session map
 						UserSession usOld=usernames.put(us.sCheckedOUCUKey,us);
 						// If there was one already there, get rid of it
 						if(usOld!=null)
 						{
 							sessions.remove(usOld.sCookie);
 						}
 						sKillOtherSessions=us.sCheckedOUCUKey;
 					}
 				}
 			}
 			// If they started a session, tell other servers to kill that session (in thread)
 			if(sKillOtherSessions!=null)	new RemoteSessionKiller(sKillOtherSessions);
 			// Error if forbidden
 			if(bTempForbid)
 			{
 				sendError(null,request,response,HttpServletResponse.SC_FORBIDDEN,
 					false,
 					false,
 					null, "Simultaneous sessions forbidden", "The system thinks you have tried to log on using two different " +
 					"browsers at the same time, which isn't permitted. If this message "+
 					"has appeared in error, please wait 60 seconds then try again.", null);
 			}
 
 			// Synchronize on the session - if we get multiple requests for same
 			// session at same time the others will just have to wait.
 			// (Except see below for resources.)
 			synchronized(us)
 			{
 				// Set last action time (so session doesn't time out)
 				us.touch();
 
 				// If they have an OUCU but also a temp-login then we need to chuck away
 				// their session...
 				if(us.ud!=null && sOUCU!=null && !us.ud.isLoggedIn())
 				{
 					Cookie c=new Cookie(sTestCookieName,"");
 					c.setMaxAge(0);
 					c.setPath("/");
 					response.addCookie(c);
 					response.sendRedirect(request.getRequestURI());
 					return;
 				}
 
 				// Get auth if needed
 				if(us.ud==null)
 				{
 					try
 					{
 						us.loadTestDeployment(sTestID);
 					}
 					catch(OmException oe)
 					{
 						if(oe.getCause()!=null && oe.getCause() instanceof FileNotFoundException)
 						{
 							sendError(us,request,response,HttpServletResponse.SC_NOT_FOUND,
 								false,false,null, "No such test", "There is currently no test with the ID "+sTestID+".", null);
 						}
 						else
 							throw oe;
 					}
 
 					us.ud=auth.getUserDetails(request,response,!us.getTestDeployment().isWorldAccess());
 					if(us.ud==null)
 					{
 						// They've been redirected to SAMS. Chuck their session as soon
 						// as expirer next runs, they won't be needing it as we didn't give
 						// them a cookie yet
 						us.markForDiscard();
 						return;
 					}
 
 					// We only give them a cookie after passing this stage. If they were
 					// redirected to SAMS, they don't get a cookie until the next request.
 					if(bNewCookie)
 					{
 						Cookie c=new Cookie(sTestCookieName,sCookie);
 						c.setPath("/");
 						response.addCookie(c);
 					}
 
 					// If they're not logged in, give them a not-logged-in cookie with
 					// a made-up OUCU in it
 					if(us.ud.isLoggedIn())
 					{
 						us.sOUCU = us.ud.getUsername();
 					}
 					else
 					{
 						if(sFakeOUCU==null)
 						{
 							// Make 8-letter random OUCU. We don't bother storing a list,
 							// but there are about 3,000 billion possibilities so it should
 							// be OK.
 							us.sOUCU = "_" + Strings.randomAlNumString(7);
 
 							// Set it in cookie for future sessions
 							Cookie c = new Cookie(FAKEOUCUCOOKIENAME,us.sOUCU);
 							c.setPath("/");
 							// Expiry is 4 years
 							c.setMaxAge( (3*365 + 366) * 24 * 60 * 60 ) ;
 							response.addCookie(c);
 							response.sendRedirect(request.getRequestURI());
 							return;
 						}
 						else
 						{
 							us.sOUCU = sFakeOUCU;
 						}
 					}
 
 					// Remember auth hash so that it'll know if they change cookie now
 					us.iAuthHash = iAuthHash;
 				}
 
 				us.bAllowAfterForbid=
 					// * a posted 'end session' request
 					(bPost && sCommand.equals("?end")) ||
 					// * Access options for that page;
 					sCommand.equals("?access") ||
 					// * Stylesheet
 					sCommand.matches("resources/[0-9]+/style-[0-9]+\\.css")
 					;
 
 				// Check test hasn't timed out
 				if(us.getTestId()!=null && us.getTestDeployment().isAfterForbid()
 						// Only exceptions we allow are:
 						// * admin
 						&& !us.bAdmin
 						// * 'finished test' requests to show results pages
 						&& !us.isFinished()
 						// * Things that are allowed after forbid
 						&& !(us.bAllowAfterForbid && !us.getTestDeployment().isAfterForbidExtension())
 						)
 				{
 					// It is forbidden. Drop session.
 					Cookie c=new Cookie(sTestCookieName,"false");
 					c.setMaxAge(0);
 					c.setPath("/");
 					response.addCookie(c);
 					response.sendRedirect(request.getRequestURI());
 					return;
 				}
 
 				if(sCommand.equals(""))
 				{
 					// Have they possibly lost an existing session? If so, go find it
 					if(us.getTestId()==null)
 					{
 						if(!us.isSingle() && checkRestartSession(rt,sTestID,us,request,response))
 							return;
 					}
 
 					// If it's a GET and either they have no test or they aren't on this one...
 					// (the latter should not be possible since cookies are test-specific)
 					if(!bPost && (us.getTestId()==null || !sTestID.equals(us.getTestId())))
 					{
 						// Start this test
 						handleStart(rt,sTestID,us,-1, request, response);
 						return;
 					}
 					// Otherwise check they're on current test (if not, wtf?)
 					if(us.getTestId()==null || !sTestID.equals(us.getTestId()))
 					{
 						sendError(us,request,response,
 							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,true,false, null, "Unexpected request", "The action you just took doesn't seem to " +
 							"match the test you are currently on.", null);
 					}
 
 					// OK, handle action
 					if(bPost && us.oss!=null)
 						// us.oss check is to make sure there's actually a question session.
 						// In certain cases of bad timing/impatient users, the post might
 						// occur when there isn't one, which causes a crash. So in that
 						// case let's just do handleNothing and hope it gets back onto
 						// even keel.
 						handleProcess(rt,us,request, response);
 					else
 						handleNothing(rt,us,request, response);
 					return;
 				}
 
 				// Single mode reset flag
 				if(us.isSingle())
 				{
 					if(sCommand.equals("?restart"))
 					{
 						handleStart(rt,sTestID,us,-1, request, response);
 						return;
 					}
 					if(sCommand.matches("\\?variant=[0-9]+"))
 					{
 						handleStart(rt,sTestID,us,Integer.parseInt(sCommand.substring(9)),request, response);
 						return;
 					}
 				}
 
 				// Beyond here, we must be logged into a test to continue
 				if(us.getTestId()==null)
 				{
 					sendError(us,request,response,
 							HttpServletResponse.SC_FORBIDDEN,false,false,
 							sTestID, "Not logged in", "You are not currently logged into the test on this server. Please "+
 							"re-enter the test before continuing.", null);
 				}
 
 				// Accessibility options form, post version when submitting answers
 				if(sCommand.equals("?access"))
 				{
 					handleAccess(rt,bPost,false,us,request, response);
 					return;
 				}
 				// Toggle plain mode
 				if(sCommand.equals("?plainmode"))
 				{
 					handleAccess(rt,false,true,us,request, response);
 					return;
 				}
 
 				if(!us.isSingle())
 				{
 					if(sCommand.startsWith("reports!"))
 					{
 						reports.handleTestReport(us,sCommand.substring("reports!".length()),request,response);
 						return;
 					}
 					// Redo command is posted
 					if(sCommand.equals("?redo") && bPost)
 					{
 						if(!us.getTestDefinition().isRedoQuestionAllowed() && !us.bAdmin)
 							sendError(us,request,response,
 								HttpServletResponse.SC_FORBIDDEN,true,false, null, "Forbidden", "This test does not permit questions to be redone.", null);
 						else
 							handleRedo(rt,us,request, response);
 						return;
 					}
 					// So is restart command
 					if(sCommand.equals("?restart") && bPost)
 					{
 						if(!us.getTestDefinition().isRedoTestAllowed() && !us.bAdmin)
 							sendError(us,request,response,
 								HttpServletResponse.SC_FORBIDDEN,true,false, null, "Forbidden", "This test does not permit restarting it.", null);
 						else
 							handleRestart(rt,us,request, response);
 						return;
 					}
 					// Request to show 'end test, ok?' screen, post version is if you say
 					// yes
 					if(sCommand.equals("?end"))
 					{
 						handleEnd(rt,bPost,false,us,request, response);
 						return;
 					}
 					if(sCommand.startsWith("?variant="))
 					{
 						handleVariant(rt,sCommand.substring("?variant=".length()),us,request,response);
 						return;
 					}
 
 					// Check it's not a post request
 					if(bPost)
 					{
 						sendError(us,request,response,
 							HttpServletResponse.SC_METHOD_NOT_ALLOWED,true, false, null, "Method not allowed", "You cannot post data to the specified URL.", null);
 					}
 					// Check they're on current test, otherwise redirect to start
 					if(us.getTestId()==null || !sTestID.equals(us.getTestId()))
 					{
 						sendError(us,request,response,
 							HttpServletResponse.SC_FORBIDDEN, true, false, null, "Forbidden", "Cannot access resources for non-current test.", null);
 					}
 					if(sCommand.startsWith("?jump="))
 					{
 						try
 						{
 							handleJump(rt,Integer.parseInt(sCommand.substring("?jump=".length())),us,request, response);
 						}
 						catch(NumberFormatException nfe)
 						{
 							sendError(us,request,response,
 								HttpServletResponse.SC_NOT_FOUND,true, false, null, "Not found", "Can only jump to numbered index.", null);
 						}
 						return;
 					}
 					// Request to move to next question
 					if(sCommand.equals("?next"))
 					{
 						handleNext(rt,us,request,response);
 						return;
 					}
 					if(sCommand.equals("?summary"))
 					{
 						if(!us.getTestDefinition().isSummaryAllowed())
 							sendError(us,request,response,
 								HttpServletResponse.SC_FORBIDDEN,true,false, null, "Forbidden", "This test does not permit summary display.", null);
 						else
 							handleSummary(rt,us,request, response);
 						return;
 					}
 				}
 			}
 
 			// Resources occur un-synchronized so that they can download more than one
 			// at a time
 			String sResourcesPrefix="resources/"+us.getTestPosition()+"/";
 			if(sCommand.equals(sResourcesPrefix+"style-"+us.iCSSIndex+".css"))
 			{
 				handleCSS(us,request,response);
 				return;
 			}
 			if(sCommand.startsWith(sResourcesPrefix))
 			{
 				handleResource(sCommand.substring((sResourcesPrefix).length()),us,request,response);
 				return;
 			}
 
 			sendError(us,request,response,
 				HttpServletResponse.SC_NOT_FOUND,false, true, null, "Not found", "The URL you requested is not provided by this server.", null);
 		}
 		catch(StopException se)
 		{
 			// This just means that data was already sent to user
 		}
 		catch(Throwable t)
 		{
 			try
 			{
 				sendError(us,request,
 					response,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,true, false, null, "Error handling request", "An error occurred while processing your request.", t);
 			}
 			catch(StopException se)
 			{
 				// This throws a stopexception
 			}
 		}
 		finally
 		{
 			String sOUCU,sPlace;
 			if(us==null)
 			{
 				sOUCU="?";
 				sPlace="?";
 			}
 			else
 			{
 				if(us.sOUCU==null)
 				{
 					sOUCU="??";
 				}
 				else
 				{
 					sOUCU=us.sOUCU;
 				}
 				sPlace="ind="+us.getTestPosition()+",seq="+us.iDBseq;
 			}
 
 			long lTotal=System.currentTimeMillis()-rt.lStart;
 
 			l.logNormal("Request",
 				sOUCU+
 				",[total="+lTotal+",qe="+rt.lQEngineElapsed+",db="+rt.lDatabaseElapsed+"]"+
 				",["+sPlace+"]"+
 				","+sPath+(request.getQueryString()==null ? "" : "?"+request.getQueryString()));
 		}
 	}
 
 	private void handleStart(RequestTimings rt,String sTestID,UserSession us,int variant,
 			HttpServletRequest request, HttpServletResponse response)
 		throws Exception
 	{
 		initTestAttempt(rt,sTestID,us,variant,request, response);
 
 		// Redirect to system-check page if needed
 		doSystemCheck(us,request,response);
 
 		// Start first page
 		servePage(rt,us,false,request, response);
 	}
 
 	private void initTestAttempt(RequestTimings rt,String sTestID,UserSession us,
 			int variant,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		stopQuestionSession(rt, us);
 
 		// Set up the basic parts of the test session
 
 		// Random seed is normally time in milliseconds. For system testing we fix
 		// it to always be the same value.
 		initTestSession(us,rt,sTestID,request, response, false, false,
 				us.ud.isSysTest() ? 1124965882611L : System.currentTimeMillis(), variant);
 
 		// Don't store anything in database for singles version
 		if(us.isSingle()) return;
 
 		// Store details in database
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		try
 		{
 			ResultSet rs=oq.queryMaxTestAttempt(dat,us.sOUCU,sTestID);
 			int iMaxAttempt=0;
 			if(rs.next() && rs.getMetaData().getColumnCount()>0) iMaxAttempt=rs.getInt(1);
 
 			oq.insertTest(dat,us.sOUCU,sTestID,us.getRandomSeed(),iMaxAttempt+1,
 				us.bAdmin,
 				// Use same PI as OUCU for non-logged-in guests
 				us.ud.isLoggedIn() ? us.ud.getPersonID() : us.sOUCU,
 				us.getFixedVariant(),
 				us.navigatorVersion);
 			int dbTi = oq.getInsertedSequenceID(dat,"tests","ti");
 			l.logDebug("TI = " + dbTi);
 			us.setDbTi(dbTi);
 
 			for(int i=0;i<us.getTestLeavesInOrder().length;i++)
 			{
 				if(us.getTestLeavesInOrder()[i] instanceof TestQuestion)
 				{
 					TestQuestion tq=(TestQuestion)us.getTestLeavesInOrder()[i];
 					oq.insertTestQuestion(dat,us.getDbTi(),tq.getNumber(),tq.getID(),
 						tq.getVersion(),tq.getSection());
 				}
 			}
 
 			storeSessionInfo(request,dat,us.getDbTi());
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 	}
 
 	/**
 	 * Should be called when a new session is started. Records the IP address.
 	 * @param dat Transaction
 	 * @param iTI Test index.
 	 */
 	private void storeSessionInfo(HttpServletRequest request,DatabaseAccess.Transaction dat,int iTI)
 		throws SQLException
 	{
 		// Check IP address, allowing the firewall-provided one to win (note:
 		// this means people inside the firewall, or a system without a firewall,
 		// allows these IPs to be spoofed. But we don't rely on them for anything
 		// security-ish anyhow).
 		String sIP=request.getHeader("Client-IP");
 		if(sIP==null) sIP=request.getRemoteAddr();
 
 		// Browser
 		String sAgent=request.getHeader("User-Agent");
 		if(sAgent==null) sAgent="";
 		if(sAgent.length()>255) sAgent=sAgent.substring(0,255);
 
 		// Store in database
 		oq.insertSessionInfo(dat,iTI,sIP,sAgent);
 	}
 
 	private void stopQuestionSession(RequestTimings rt, UserSession us) throws RemoteException,om.axis.qengine.OmException
 	{
 		// Stop existing question session if present
 		if(us.oss!=null)
 		{
 			us.oss.stop(rt);
 			us.oss=null;
 			us.mResources.clear();
 		}
 	}
 
 	private void handleJump(RequestTimings rt,int iIndex,UserSession us,HttpServletRequest request, HttpServletResponse response)
 		throws Exception
 	{
 		stopQuestionSession(rt, us);
 
 		// Check & set index
 		if(iIndex<0 || iIndex>=us.getTestLeavesInOrder().length)
 		{
 			sendError(us,request,response,
 				HttpServletResponse.SC_FORBIDDEN,true,false, null, "Question out of range", "There is no question with that index.", null);
 		}
 		if(!us.getTestLeavesInOrder()[iIndex].isAvailable())
 		{
 			sendError(us,request,response,
 				HttpServletResponse.SC_FORBIDDEN,true,false, null, "Unavailable question", "That question is not currently available.", null);
 		}
 		setIndex(rt,us,iIndex);
 
 		// Redirect
 		response.sendRedirect(
 			request.getRequestURL().toString().replaceAll("/[^/]*$","/")+endOfURL(request));
 	}
 
 	private void handleSummary(RequestTimings rt,UserSession us,HttpServletRequest request, HttpServletResponse response)
 		throws Exception
 	{
 		Document d=XML.parse(new File(getServletContext().getRealPath("WEB-INF/templates/progresssummary.xhtml")));
 		Map<String,String> mReplace=new HashMap<String,String>();
 		mReplace.put("EXTRA",endOfURL(request));
 		XML.replaceTokens(d,mReplace);
 		Element eDiv=XML.find(d,"id","summarytable");
 
 		boolean bScores=us.getTestDefinition().doesSummaryIncludeScores();
 		if(bScores)
 		{
 			// Build the scores list from database
 			us.getTestRealisation().getScore(rt, this, da, oq);
 		}
 		addSummaryTable(rt,us,eDiv,inPlainMode(request),
 			us.getTestDefinition().doesSummaryIncludeQuestions(),
 			us.getTestDefinition().doesSummaryIncludeAttempts(),
 			bScores);
 
 		serveTestContent(us,"Your answers so far","",null,null,XML.saveString(d),false, request, response, true);
 	}
 
 	private void addSummaryTable(RequestTimings rt,UserSession us,Node nParent,boolean bPlain,
 		boolean bIncludeQuestions,boolean bIncludeAttempts,boolean bIncludeScore) throws Exception
 	{
 		Node nTableParent,nPlainParent=null;
 		if(bPlain)
 		{
 			Element eSkip=XML.createChild(nParent,"a");
 			XML.setText(eSkip,"Skip table");
 			eSkip.setAttribute("href","#plaintable");
 
 			nTableParent=XML.createChild(nParent,"div");
 
 			Element eAnchor=XML.createChild(nParent,"a");
 			eAnchor.setAttribute("name","plaintable");
 
 			XML.createText(nParent,"h3","Text-only version of preceding table");
 
 			eSkip=XML.createChild(nParent,"a");
 			XML.setText(eSkip,"Skip text-only version");
 			eSkip.setAttribute("href","#endtable");
 
 			nPlainParent=XML.createChild(nParent,"div");
 
 			eAnchor=XML.createChild(nParent,"a");
 			eAnchor.setAttribute("name","endtable");
 		}
 		else
 		{
 			nTableParent=nParent;
 		}
 		// Create basic table
 		Element eTable=XML.createChild(nTableParent,"table");
 		eTable.setAttribute("class","topheaders");
 		Element eTR=XML.createChild(eTable,"tr");
 		Element eTH=XML.createChild(eTR,"th");
 		eTH.setAttribute("scope","col");
 		Element eAbbr=XML.createChild(eTH,"abbr");
 		XML.createText(eAbbr,"#");
 		eAbbr.setAttribute("title","Question number");
 		if(bIncludeQuestions)
 		{
 			eTH=XML.createChild(eTR,"th");
 			eTH.setAttribute("scope","col");
 			XML.createText(eTH,"Question");
 			eTH=XML.createChild(eTR,"th");
 			eTH.setAttribute("scope","col");
 			XML.createText(eTH,"Your answer");
 		}
 		if(bIncludeAttempts)
 		{
 			eTH=XML.createChild(eTR,"th");
 			eTH.setAttribute("scope","col");
 			XML.createText(eTH,"Result");
 		}
 		String[] asAxes=null;
 		if(bIncludeScore)
 		{
 			CombinedScore ps=us.getRootTestGroup().getFinalScore();
 			asAxes=ps.getAxesOrdered();
 			for(int iAxis=0;iAxis<asAxes.length;iAxis++)
 			{
 				String sAxis=asAxes[iAxis];
 				eTH=XML.createChild(eTR,"th");
 				eTH.setAttribute("scope","col");
 				XML.createText(eTH,"Marks"+
 					(sAxis==null ? "" : " ("+sAxis+")"));
 				eTH=XML.createChild(eTR,"th");
 				eTH.setAttribute("scope","col");
 				XML.createText(eTH,"Out of");
 			}
 		}
 
 		// Query for questions and answers
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		try
 		{
 			ResultSet rs=oq.querySummary(dat,us.getDbTi());
 
 			// Build table
 			int iCurrentQuestion=1;
 			int iMaxQuestion=0;
 			String sLastQuestion=null;
 			String sDisplayedSection=null; // Section heading that has already been displayed
 			String sPreviousSection=null; // Section of last row (null if none)
 
 			while(rs.next())
 			{
 				// Keep track of max number
 				int iQuestionNumber=rs.getInt(1);
 				iMaxQuestion=Math.max(iMaxQuestion,iQuestionNumber);
 
 				// Ignore answers after we're looking for next question
 				if(iQuestionNumber<iCurrentQuestion) continue;
 
 				// Get section
 				String sSection=rs.getString(7);
 
 				// If we didn't put out an answer for current question, chuck one out now
 				if(iQuestionNumber>iCurrentQuestion)
 				{
 					sDisplayedSection=addSectionRow(
 							bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 							nPlainParent, eTable, asAxes,sPreviousSection,sDisplayedSection);
 					addUnfinishedRow(
 							us, bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 							nPlainParent, eTable,
 							iCurrentQuestion, sLastQuestion);
 
 					iCurrentQuestion++;
 					// This only works because there always will be at least one line per
 					// question thanks to the LEFT JOIN
 				}
 
 				sLastQuestion=rs.getString(5);
 
 				// Ignore unfinished attempts, wait for a finished one
 				if(rs.getInt(2)!=0)
 				{
 					// Woo! We have an answer
 					sDisplayedSection=addSectionRow(
 							bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 							nPlainParent, eTable, asAxes, sSection,sDisplayedSection);
 
 					eTR=XML.createChild(eTable,"tr");
 					eTR.setAttribute("class","answered");
 					XML.createText(eTR,"td",""+iCurrentQuestion);
 					Element ePlainRow=null;
 					if(bPlain)
 					{
 						ePlainRow=XML.createChild(nPlainParent,"div");
 						XML.createText(ePlainRow,"Question "+iCurrentQuestion+". ");
 					}
 
 					if(bIncludeQuestions)
 					{
 						String sQ=rs.getString(3),sA=rs.getString(4);
 						XML.createText(eTR,"td",sQ);
 						XML.createText(eTR,"td",sA);
 						if(bPlain)
 						{
 							XML.createText(ePlainRow,"Question: "+sQ+". Your answer: "+sA+". ");
 						}
 					}
 					if(bIncludeAttempts)
 					{
 						String sAttempts=getAttemptsString(rs.getInt(6));
 						XML.createText(eTR,"td",sAttempts);
 						if(bPlain)
 						{
 							XML.createText(ePlainRow,"Result: "+sAttempts+". ");
 						}
 					}
 					if(bIncludeScore) addSummaryTableScore(eTR,ePlainRow,us,sLastQuestion);
 
 					// Start looking for next question now
 					iCurrentQuestion++;
 				}
 
 				sPreviousSection=sSection;
 			}
 
 			// If we didn't do the last one, put that out
 			if(iCurrentQuestion<=iMaxQuestion)
 			{
 				sDisplayedSection=addSectionRow(
 						bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 						nPlainParent, eTable, asAxes, sPreviousSection,sDisplayedSection);
 				addUnfinishedRow(
 						us, bPlain, bIncludeQuestions, bIncludeAttempts, bIncludeScore,
 						nPlainParent, eTable,
 						iCurrentQuestion, sLastQuestion);
 			}
 
 			if(bIncludeScore)
 			{
 				CombinedScore ps=us.getRootTestGroup().getFinalScore();
 				eTR=XML.createChild(eTable,"tr");
 				eTR.setAttribute("class","totals");
 				Element eTD=XML.createChild(eTR,"td");
 				eTD.setAttribute("colspan",""+(
 						1 + (bIncludeQuestions ? 2 : 0) + (bIncludeAttempts ? 1 : 0)
 						));
 				XML.createText(eTD,"Total");
 				Element ePlainRow=null;
 				if(bPlain)
 				{
 					ePlainRow=XML.createChild(nPlainParent,"div");
 					XML.createText(ePlainRow,"Totals: ");
 				}
 
 				for(int iAxis=0;iAxis<asAxes.length;iAxis++)
 				{
 					String sAxis=asAxes[iAxis];
 					String
 						sScore=displayScore(ps.getScore(sAxis)),
 						sMax=displayScore(ps.getMax(sAxis));
 					XML.createText(eTR,"td",sScore);
 					XML.createText(eTR,"td",sMax);
 					if(ePlainRow!=null)
 					{
 						XML.createText(ePlainRow,"Marks" +
 								(sAxis==null ? "" : " ("+sAxis+")")+
 								": "+sScore+". Out of: "+sMax+". ");
 					}
 				}
 			}
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 	}
 
 	private String addSectionRow(boolean bPlain, boolean bIncludeQuestions,boolean bIncludeAttempts,boolean bIncludeScore,Node nPlainParent,Element eTable,String[] asAxes,String sSection,String sCurrentSection)
 	{
 		// Don't output anything if there has been no change to the current section
 		if(sSection==null || sSection.equals(sCurrentSection)) return sCurrentSection;
 
 		Element eTR=XML.createChild(eTable,"tr");
 		eTR.setAttribute("class","sectionname");
 		Element eTD=XML.createChild(eTR,"td");
 		eTD.setAttribute("colspan",""+(
 				1+(bIncludeQuestions?2:0)+(bIncludeAttempts?1:0)+
 				(bIncludeScore ? asAxes.length*2 : 0)));
 		XML.createText(eTD,sSection);
 
 		if(bPlain)
 			XML.createText(nPlainParent,"h4",sSection);
 
 		return sSection;
 	}
 
 	private void addUnfinishedRow(UserSession us, boolean bPlain, boolean bIncludeQuestions, boolean bIncludeAttempts, boolean bIncludeScore, Node nPlainParent, Element eTable, int iCurrentQuestion, String sLastQuestion) throws OmFormatException
 	{
 		Element eTR=XML.createChild(eTable,"tr");
 		Element ePlainRow=null;
 		eTR.setAttribute("class","unanswered");
 		XML.createText(eTR,"td",""+iCurrentQuestion);
 		if(bPlain)
 		{
 			ePlainRow=XML.createChild(nPlainParent,"div");
 			XML.createText(ePlainRow,"Question "+iCurrentQuestion+". ");
 		}
 
 		if(bIncludeQuestions)
 		{
 			XML.createText(eTR,"td",SUMMARYTABLE_NOTANSWERED);
 			XML.createChild(eTR,"td");
 		}
 		if(bIncludeAttempts)
 		{
 			if(bIncludeQuestions)
 				XML.createChild(eTR,"td");
 			else
 				XML.createText(eTR,"td",SUMMARYTABLE_NOTANSWERED);
 		}
 		if(bPlain && (bIncludeAttempts||bIncludeQuestions))
 		{
 			XML.createText(ePlainRow,SUMMARYTABLE_NOTANSWERED+". ");
 		}
 
 		if(bIncludeScore) addSummaryTableScore(eTR,ePlainRow,us,sLastQuestion);
 	}
 
 	private void addSummaryTableScore(Element eTR,Element ePlainRow,UserSession us,String sQuestion)
 		throws OmFormatException
 	{
 		// Find question
 		TestQuestion tq=null;
 		for(int i=0;i<us.getTestLeavesInOrder().length;i++)
 		{
 			if(us.getTestLeavesInOrder()[i] instanceof TestQuestion &&
 				((TestQuestion)us.getTestLeavesInOrder()[i]).getID().equals(sQuestion))
 			{
 				tq=(TestQuestion)us.getTestLeavesInOrder()[i];
 				break;
 			}
 		}
 
 		// Get score (scaled)
 		CombinedScore ps=tq.getScoreContribution(us.getRootTestGroup());
 		String[] asAxes=ps.getAxesOrdered();
 		for(int iAxis=0;iAxis<asAxes.length;iAxis++)
 		{
 			String sAxis=asAxes[iAxis];
 			String
 				sScore=displayScore(ps.getScore(sAxis)),
 				sMax=displayScore(ps.getMax(sAxis));
 
 			XML.createText(eTR,"td",sScore);
 			XML.createText(eTR,"td",sMax);
 
 			if(ePlainRow!=null)
 			{
 				XML.createText(ePlainRow,"Marks" +
 					(sAxis==null ? "" : " ("+sAxis+")")+
 					": "+sScore+". Out of: "+sMax+". ");
 			}
 		}
 	}
 
 	private static String displayScore(double dScore)
 	{
 		if(Math.abs(dScore - Math.round(dScore)) < 0.001)
 		{
 			return (int)Math.round(dScore)+"";
 		}
 		else
 		{
 			NumberFormat nf=DecimalFormat.getNumberInstance();
 			nf.setMaximumFractionDigits(1);
 			return nf.format(dScore);
 		}
 	}
 
 
 	private void handleNext(RequestTimings rt,UserSession us,
 		HttpServletRequest request,HttpServletResponse response) throws Exception
 	{
 		stopQuestionSession(rt, us);
 
 		// Get next question that's permitted
 		if(!findNextQuestion(rt,us,false,!us.getTestDefinition().isRedoQuestionAllowed(),!us.getTestDefinition().isRedoQuestionAllowed()))
 			// If there are none left, go to end page
 			redirectToEnd(request,response);
 		else
 			// Redirect
 			redirectToTest(request,response);
 	}
 
 	private void redirectToEnd(HttpServletRequest request,HttpServletResponse response) throws IOException
 	{
 		response.sendRedirect(
 			request.getRequestURL().toString().replaceAll("/[^/]*$","/")+"?end");
 	}
 
 	/** @return A stupid bit to add to the end of the test URL */
 	private String endOfURL(HttpServletRequest request)
 	{
 		return endOfURL(inPlainMode(request));
 	}
 	/** @return A stupid bit to add to the end of the test URL */
 	private String endOfURL(boolean bPlain)
 	{
 		// The form URL gets a random number after it in plain mode because otherwise
 		// JAWS doesn't reread the page properly, thinking it's the same one. GAH!
 		if(bPlain)
 			return "_"+Math.random();
 		else
 			return "";
 	}
 
 	private void handleRedo(RequestTimings rt,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Start new attempt at question
 		initQuestionAttempt(rt,us);
 
 		// Redirect back to question page
 		response.sendRedirect(
 			request.getRequestURL().toString().replaceAll("/[^/]*$","/")+endOfURL(request));
 	}
 
 	private void handleRestart(RequestTimings rt,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Start new attempt at question
 		initTestAttempt(rt,us.getTestId(),us,-1,request, response);
 
 		// Redirect back to question page
 		response.sendRedirect(
 			request.getRequestURL().toString().replaceAll("/[^/]*$","/")+endOfURL(request));
 	}
 
 	/**
 	 * @param request HTTP request
 	 * @return Servlet URL ending in / e.g. http://wherever/om-tn/
 	 */
 	private String getServletURL(HttpServletRequest request)
 	{
 		// Works for http and https URLs on any server as long as the test
 		// navigator root is http://somewhere/something/ (i.e. it must be
 		// one level down).
 		return request.getRequestURL().toString().replaceAll(
 			"^(https?://[^/]+/[^/]+/).*$","$1");
 	}
 
 	/**
 	 * Class for storing a question version number.
 	 */
 	public static class QuestionVersion
 	{
 		int iMajor,iMinor;
 		@Override
 		public String toString()
 		{
 			return iMajor+"."+iMinor;
 		}
 	}
 
 	/**
 	 * Returns appropriate version of question to use.
 	 * @param sQuestionID Question ID
 	 * @param iRequiredVersion Desired version or TestQuestion.VERSION_UNSPECIFIEDD
 	 * @return Appropriate version
 	 * @throws OmException
 	 */
 	public QuestionVersion getLatestVersion(String sQuestionID,int iRequiredVersion)
 		throws OmException
 	{
 		// This should use a proper question bank at some point
 		Pattern p=Pattern.compile(sQuestionID+".([0-9]+).([0-9]+).jar");
 		QuestionVersion qv=new QuestionVersion();
 		boolean bFound=false;
 		File[] af=IO.listFiles(questionBankFolder);
 		for(int i=0;i<af.length;i++)
 		{
 			// See if it's the question we're looking for
 			Matcher m=p.matcher(af[i].getName());
 			if(!m.matches()) continue;
 
 			// Compare version
 			int
 				iMajor=Integer.parseInt(m.group(1)),
 				iMinor=Integer.parseInt(m.group(2));
 
 			if(
 				// Major version is better than before and either matches version or unspec
 				(iMajor > qv.iMajor && (iRequiredVersion==iMajor || iRequiredVersion==TestQuestion.VERSION_UNSPECIFIED))
 				||
 				// Same major version as before, better minor version
 				(iMajor == qv.iMajor && iMinor > qv.iMinor)
 				)
 			{
 				qv.iMajor=iMajor;
 				qv.iMinor=iMinor;
 				bFound=true;
 			}
 		}
 
 		if(!bFound)
 		{
 			throw new OmException("Question file missing: "+sQuestionID+
 					(iRequiredVersion!=TestQuestion.VERSION_UNSPECIFIED ? " "+iRequiredVersion : ""));
 		}
 		return qv;
 	}
 
 	private void applySessionChanges(UserSession us,ProcessReturn pr)
 	{
 		// Add resources
 		Resource[] arResources=pr.getResources();
 		for(int i=0;i<arResources.length;i++)
 		{
 			us.mResources.put(arResources[i].getFilename(),arResources[i]);
 		}
 
 		// Set style
 		if(pr.getCSS()!=null)
 		{
 			us.sCSS=pr.getCSS();
 			us.iCSSIndex++;
 		}
 
 		// Set progress info
 		if(pr.getProgressInfo()!=null) us.sProgressInfo=pr.getProgressInfo();
 	}
 
 	private final static Pattern ZOOMPATTERN=Pattern.compile(".*\\[zoom=(.*?)\\].*");
 
 	private final static Pattern COLOURSPATTERN=Pattern.compile(".*\\[colours=(.*?)\\].*");
 
 	private boolean inPlainMode(HttpServletRequest request)
 	{
 		String sAccess=getAccessibilityCookie(request);
 		return (sAccess.indexOf("[plain]")!=-1);
 	}
 
 	private StartReturn startQuestion(RequestTimings rt,HttpServletRequest request,UserSession us,TestQuestion tq, int iAttempt,int iMajor,int iMinor)
 		throws Exception
 	{
 		// Question URL
 		String sQuestionBase=getQuestionBase();
 
 		// Determine version if not specified
 		QuestionVersion qv;
 		if(iMajor==0)
 		{
 			// Major=0 in two case: first, this is a fresh start not a replay;
 			// or second, the database has become somehow corrupted before we have actually
 			// set up the question versions in nav_questions (they default to 0).
 			qv=getLatestVersion(tq.getID(),tq.getVersion());
 			if(!us.isSingle())
 			{
 				if(us.iDBqi==0 || qv.iMajor==0) throw new OmUnexpectedException(
 						"Unexpected data setting question versions ("+us.iDBqi+"): "+qv.iMajor);
 				DatabaseAccess.Transaction dat=da.newTransaction();
 				try
 				{
 					oq.updateSetQuestionVersion(dat,us.iDBqi,qv.iMajor,qv.iMinor);
 				}
 				finally
 				{
 					rt.lDatabaseElapsed+=dat.finish();
 				}
 			}
 		}
 		else
 		{
 			qv=new QuestionVersion();
 			qv.iMajor=iMajor;
 			qv.iMinor=iMinor;
 		}
 
 		// Question parameters
 		NameValuePairs p=new NameValuePairs();
 		int seedIncrement = MAGIC_RANDOM_SEED_INCREMENT;
 		if (OmVersion.compareVersions(us.navigatorVersion, "1.3.0") <= 0) {
 			seedIncrement = 1;
 		}
 		long randomSeed = us.getRandomSeed()+iAttempt*seedIncrement;
 		p.add("randomseed",randomSeed+"");
 		String sAccess=getAccessibilityCookie(request);
 		if(sAccess.indexOf("[plain]")!=-1)
 			p.add("plain","yes");
 		Matcher m=ZOOMPATTERN.matcher(sAccess);
 		if(m.matches())
 		{
 			// This check avoids a potential security issue where somebody could
 			// crash it or use all memory by choosing a silly zoom value in the cookie
 			if(m.group(1).equals("1.5") || m.group(1).equals("2.0"))
 				p.add("zoom",m.group(1));
 		}
 		m=COLOURSPATTERN.matcher(sAccess);
 		if(m.matches())
 		{
 			String sColours=m.group(1);
 			if(sColours.matches("[0-9a-f]{12}"))
 			{
 				p.add("fixedfg","#"+sColours.substring(0,6));
 				p.add("fixedbg","#"+sColours.substring(6,12));
 			}
 		}
 		if(us.getFixedVariant()>-1)
 			p.add("fixedvariant",us.getFixedVariant()+"");
 
 		l.logDebug("Starting a session on question " + tq.getID() + " version " + qv +
 				" with random seed " + randomSeed + ".");
 		
 		// Start question
 		us.oss=osb.start(rt,
 			tq.getID(),qv.toString(),sQuestionBase,p.getNames(),p.getValues(),new String[0]);
 		StartReturn sr=us.oss.eatStartReturn();
 
 		// Set question session
 		us.iDBseq=1;
 
 		// Add resources
 		Resource[] arResources=sr.getResources();;
 		for(int i=0;i<arResources.length;i++)
 		{
 			us.mResources.put(arResources[i].getFilename(),arResources[i]);
 		}
 
 		// Set style
 		us.sCSS=sr.getCSS();
 		us.iCSSIndex=Math.abs(p.hashCode()); // Start with a value that depends on the params
 		if(sr.getProgressInfo()!=null)
 			us.sProgressInfo=sr.getProgressInfo();
 
 		return sr;
 	}
 
 	private String getQuestionBase()
 	{
 		return nc.getThisTN()+"!question";
 	}
 
 	/**
 	 * Serves the final page - either information about their results, or
 	 * telling them that they aren't allowed to see it yet! Also includes
 	 * option to re-take test if permitted.
 	 * @param rt Timings
 	 * @param us Session
 	 * @param request HTTP request
 	 * @param response HTTP response
 	 * @throws Exception If anything goes wrong
 	 */
 	private void serveFinalPage(RequestTimings rt,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Create document to hold results
 		Document dTemp=XML.createDocument();
 		Element eMain=XML.createChild(dTemp,"div");
 		eMain.setAttribute("class","basicpage");
 
 		// If an email should've been sent, say so.
 		if(us.iEmailSent==1) {
 			Element eMsg=XML.createChild(eMain,"p");
 			eMsg.setAttribute("class","email");
 			XML.createText(eMsg,"Your answers to this test have been submitted, " +
 				"and an email receipt will be sent to your OU-registered "+
 				"email address within a few hours.");
 		} else if(us.iEmailSent==-1) {
 			Element eMsg=XML.createChild(eMain,"p");
 			eMsg.setAttribute("class","email");
 			XML.createText(eMsg,"Your answers to this test have been submitted. " +
 				"We were unable to send an email receipt but please rest assured " +
 				"that your answers have been correctly stored.");
 		}
 
 		// See if user's allowed to see results yet
 		boolean bAfterFeedback=us.getTestDeployment().isAfterFeedback();
 		if(us.bAdmin || bAfterFeedback)
 		{
 			if(!bAfterFeedback)
 			{
 				Element eInfo=XML.createChild(eMain,"div");
 				eInfo.setAttribute("class","adminmsg");
 				XML.createText(eInfo,
 					"Results are not yet visible to students. Students will currently see " +
 					"only a message telling them that results are unavailable at present " +
 					"and the date on which they will be able to see results, which is " +
 					us.getTestDeployment().displayFeedbackDate()+".");
 			}
 
 			// OK, work out the student's score
 			CombinedScore ps = us.getTestRealisation().getScore(rt, this, da, oq);
 
 			// Now process each element from the final part of the definition
 			processFinalTags(rt,us,us.getTestDefinition().getFinalPage(),eMain, ps,request);
 		}
 		else
 		{
 			XML.createText(eMain,"p","Thank you for completing this test.");
 			XML.createText(eMain,"p","Feedback on your results is not available " +
 				"immediately. You will be able to see information about your results " +
 				"if you return here after "+us.getTestDeployment().displayFeedbackDate()+".");
 		}
 
 		// Show restart button, if enabled
 		if((us.getTestDefinition().isRedoTestAllowed()&&!us.getTestDeployment().isAfterForbid()) || us.bAdmin)
 		{
 			if(!us.getTestDefinition().isRedoTestAllowed())
 			{
 				Element eMsg=XML.createChild(eMain,"div");
 				eMsg.setAttribute("class","adminmsg");
 				XML.createText(eMsg,"You are only permitted to restart the " +
 					"test because you have admin privileges. Students will not see " +
 					"the following button.");
 			}
 
 			Element eForm=XML.createChild(eMain,"form");
 			eForm.setAttribute("action","?restart");
 			eForm.setAttribute("method","post");
 			Element eInput;
 			eInput=XML.createChild(eForm,"input");
 			eInput.setAttribute("type","submit");
 			eInput.setAttribute("value","Restart entire test");
 		}
 
 		serveTestContent(us,"Results for "+
 				(us.ud.isLoggedIn() ? us.ud.getPersonID() : "guest"),
 				"",null,null,XML.saveString(dTemp),false, request, response, true);
 	}
 
 	/**
 	 * Do everything possible to the response to stop the browser's back button from working.
 	 * @param response the response to add headers too.
 	 */
 	public void breakBack(HttpServletResponse response)
 	{
 		response.setHeader("Pragma","No-cache");
 		response.setHeader("Cache-Control","no-store");
 		response.setHeader("Expires","Thu, 01 Jan 1970 00:00:00 GMT");
 	}
 
 	private void processFinalTags(RequestTimings rt,UserSession us,Element eParent,Element eTarget, CombinedScore ps,
 		HttpServletRequest request)
 		throws Exception
 	{
 		Element[] ae=XML.getChildren(eParent);
 		for(int i=0;i<ae.length;i++)
 		{
 			Element e=ae[i];
 			String sTag=e.getTagName();
 			if(sTag.equals("p"))
 			{
 				// Copy it
 				eTarget.appendChild(eTarget.getOwnerDocument().importNode(e,true));
 			}
 			else if(sTag.equals("scores"))
 			{
 				// Make container
 				Element eUL=XML.createChild(eTarget,"ul");
 				eUL.setAttribute("class","scores");
 
 				// Get axis labels
 				Element[] aeLabels=XML.getChildren(e,"axislabel");
 
 				// Loop through each score axis
 				String[] asAxes=ps.getAxesOrdered();
 				axisloop: for(int iAxis=0;iAxis<asAxes.length;iAxis++)
 				{
 					String sAxis=asAxes[iAxis];
 
 					// Get default label - use the axis name, capitalised, followed by
 					// colon
 					String sLabel=
 						(sAxis==null)
 						? ""
 						: sAxis.substring(0,1).toUpperCase()+sAxis.substring(1)+":";
 
 					// Obtain axislabel if one is provided
 					for(int iLabel=0;iLabel<aeLabels.length;iLabel++)
 					{
 						Element eLabel=aeLabels[iLabel];
 						if(
 							(sAxis==null && !eLabel.hasAttribute("axis")) ||
 							(sAxis!=null && sAxis.equals(eLabel.getAttribute("axis"))) )
 						{
 							// If hide=yes, skip this axis entirely
 							if("yes".equals(eLabel.getAttribute("hide")))
 								continue axisloop;
 
 							// Otherwise just update label
 							sLabel=XML.getText(eLabel);
 						}
 					}
 
 					// Make LI and put the label in it
 					Element eLI=XML.createChild(eUL,"li");
 					XML.createText(eLI," "+sLabel+" ");
 
 					// Add actual scores
 					if("yes".equals(e.getAttribute("marks")))
 					{
 						int
 							iScore=(int)Math.round(ps.getScore(sAxis)),
 							iMax=(int)Math.round(ps.getMax(sAxis));
 
 						Element eSpan=XML.createChild(eLI,"span");
 						eSpan.setAttribute("class","marks");
 						XML.createText(eSpan,""+iScore);
 						XML.createText(eLI," (out of ");
 						eSpan=XML.createChild(eLI,"span");
 						eSpan.setAttribute("class","outof");
 						XML.createText(eSpan,""+iMax);
 						XML.createText(eLI,") ");
 					}
 					if("yes".equals(e.getAttribute("percentage")))
 					{
 						int iPercentage=(int)	Math.round(
 							100.0 * ps.getScore(sAxis) / ps.getMax(sAxis));
 						Element eSpan=XML.createChild(eLI,"span");
 						eSpan.setAttribute("class","percentage");
 						XML.createText(eSpan,iPercentage+"%");
 					}
 				}
 			}
 			else if(sTag.equals("conditional"))
 			{
 				// Find marks on the specified axis
 				String sAxis = e.hasAttribute("axis") ? e.getAttribute("axis") : null;
 				String sOn=e.getAttribute("on");
 				int iCompare;
 				if(sOn.equals("marks"))
 				{
 					iCompare=(int)Math.round(ps.getScore(sAxis));
 				}
 				else if(sOn.equals("percentage"))
 				{
 					iCompare=(int)Math.round(ps.getScore(sAxis) * 100.0 / ps.getMax(sAxis));
 				}
 				else throw new OmFormatException("Unexpected on= for conditional: "+sOn);
 
 				boolean bOK=true;
 				try
 				{
 					if(e.hasAttribute("gt"))
 					{
 						if(!(iCompare > Integer.parseInt(e.getAttribute("gt")))) bOK=false;
 					}
 					if(e.hasAttribute("gte"))
 					{
 						if(!(iCompare >= Integer.parseInt(e.getAttribute("gte")))) bOK=false;
 					}
 					if(e.hasAttribute("e"))
 					{
 						if(!(iCompare == Integer.parseInt(e.getAttribute("e")))) bOK=false;
 					}
 					if(e.hasAttribute("lte"))
 					{
 						if(!(iCompare <= Integer.parseInt(e.getAttribute("lte")))) bOK=false;
 					}
 					if(e.hasAttribute("lt"))
 					{
 						if(!(iCompare < Integer.parseInt(e.getAttribute("lt")))) bOK=false;
 					}
 					if(e.hasAttribute("ne"))
 					{
 						if(!(iCompare != Integer.parseInt(e.getAttribute("ne")))) bOK=false;
 					}
 				}
 				catch(NumberFormatException nfe)
 				{
 					throw new OmFormatException("Not valid integer in <conditional>");
 				}
 				if(bOK) // Passed the conditional! Process everything within it
 				{
 					processFinalTags(rt,us,e,eTarget, ps,request);
 				}
 			}
 			else if(sTag.equals("summary"))
 			{
 				addSummaryTable(rt,us,eTarget,inPlainMode(request),
 					"yes".equals(e.getAttribute("questions")),
 					"yes".equals(e.getAttribute("attempts")),
 					"yes".equals(e.getAttribute("marks")));
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
 	public Score[] getMaximumScores(RequestTimings rt,String sID,String sVersion)
 		throws IOException,RemoteException
 	{
 		Element eMetadata = getQuestionMetadata(rt, sID, sVersion);
 		if(XML.hasChild(eMetadata,"scoring"))
 		{
 			try
 			{
 				Element[] aeMarks=XML.getChildren(
 					XML.getChild(eMetadata,"scoring"),"marks");
 
 				Score[] as=new Score[aeMarks.length];
 				for(int i=0;i<aeMarks.length;i++)
 				{
 					String sAxis=
 						aeMarks[i].hasAttribute("axis")
 						? aeMarks[i].getAttribute("axis")
 						: null;
 					as[i]=new Score(sAxis,Integer.parseInt(XML.getText(aeMarks[i])));
 				}
 
 				return as;
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new IOException("Question metadata error: Invalid <marks> in <scoring>: not an integer");
 			}
 			catch(XMLException e)
 			{
 				// Can't happen as we just checked hasChild
 				throw new OmUnexpectedException(e);
 			}
 		}
 		else
 		{
 			return new Score[] {};
 		}
 	}
 
 	/**
 	 * Cache of question metadata: String (ID\nversion) -> Document.
 	 * <p>
 	 * This cache is kept in memory until server restart because come on, how
 	 * many questions can there be? It's only one document each.
 	 * There is no need to refresh it, because questions are
 	 * guaranteed to change in version if their content changes.
 	 */
 	private Map<String,Element> questionMetadata=new HashMap<String,Element>();
 
 	/**
 	 * Obtains metadata for a question, from the cache or by requesting it from
 	 * the question engine.
 	 * @param rt Timings
 	 * @param sID ID of question
 	 * @param sVersion Version of question
 	 * @return Metadata document
 	 * @throws RemoteException If service has a problem
 	 * @throws IOException If there's an I/O problem before/after the remote service
 	 */
 	Element getQuestionMetadata(
 		RequestTimings rt,String sID,
 		String sVersion) throws RemoteException, IOException
 	{
 		String sKey=sID+"\n"+sVersion;
 		Element e;
 		synchronized(questionMetadata)
 		{
 			e=questionMetadata.get(sKey);
 			if(e!=null) return e;
 		}
 
 		e=XML.parse(osb.getQuestionMetadata(rt,sID,sVersion,getQuestionBase())).
 			getDocumentElement();
 
 		synchronized(questionMetadata)
 		{
 			questionMetadata.put(sKey,e);
 		}
 
 		return e;
 	}
 
 	private boolean worksInPlainMode(
 		RequestTimings rt,TestQuestion tq,HttpServletRequest request)
 		throws RemoteException,IOException,OmException
 	{
 		Element eMetadata=getQuestionMetadata(rt,tq.getID(),
 			getLatestVersion(tq.getID(),tq.getVersion()).toString());
 		return "yes".equals(XML.getText(eMetadata,"plainmode"));
 	}
 
 	private void servePage(RequestTimings rt,UserSession us,
 		boolean bOnRestart,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		stopQuestionSession(rt, us);
 
 		if(us.isFinished())
 		{
 			serveFinalPage(rt,us,request, response);
 			return;
 		}
 
 		// OK, let's see where they are
 		TestLeaf tl=us.getTestLeavesInOrder()[us.getTestPosition()];
 
 		if(tl instanceof TestQuestion)
 		{
 			boolean bNotReallyQuestion=false;
 
 			TestQuestion tq=(TestQuestion)tl;
 
 			if(inPlainMode(request) && !worksInPlainMode(rt,tq,request))
 			{
 				tq.setDone(true);
 
 				serveTestContent(us,"Question cannot be attempted in plain mode","",null,
 					null,"<div class='basicpage'>"+
 					"<p>This question cannot be attempted in plain mode, as it uses " +
 					"some features which can't be converted to text. " +
 					(us.isSingle() ? "</p>" : (
 					"If you need to use "+
 					"plain mode, skip this question and continue to the next.</p>"+
 					(us.getTestDeployment().getType()==TestDeployment.TYPE_NOTASSESSED ? "" :
 					"<p>Be certain to <em>inform " +
 					"the course team</em> (via your tutor) that you needed to use plain mode, " +
 					"and for that reason could not complete all questions. They can make " +
 					"appropriate adjustments to the marking. " +
 					"The system does not handle this automatically.</p>")+
 					"<p><a href='?next'>Next</a></p>")) +
 					"</div>",true, request, response, true);
 			}
 			else
 			{
 				// Look for existing instance of question in database to see if we
 				// should shove through some process actions
 				String sXHTML=null;
 				if(!us.isSingle())
 				{
 					DatabaseAccess.Transaction dat=da.newTransaction();
 					try
 					{
 						// Find actions from the latest attempt on this question in this test.
 						// This returns qi and the maximum sequence number. If the latest
 						// attempt wasn't unfinished then it returns 0.
 						ResultSet rs=oq.queryQuestionActions(dat,us.getDbTi(),tq.getID());
 
 						// If there was a previous attempt that wasn't finished
 						// then it's time to resurrect the question!
 						if(rs.next())
 						{
 							// Immediately on a test restart, we show the 'final screen' of a
 							// question if you restart it, even though we don't do that
 							// normally.
 							boolean bFinished=(bOnRestart ? rs.getInt(3)>=2 : rs.getInt(3)>=1);
 
 							if(bFinished)
 							{
 								int iQI=rs.getInt(1);
 
 								if(us.getTestDefinition().isAutomaticRedoQuestionAllowed())
 								{
 									handleRedo(rt,us,request,response);
 									throw new StopException();
 								}
 
 								sXHTML=
 									showCompletedQuestion(rt,us,request, iQI);
 								bNotReallyQuestion=true;
 							}
 							else
 							{
 								us.iDBqi=rs.getInt(1); // Required in startQuestion
 								int iMaxSeq=rs.getInt(2); // May be null, but that's ok 'cause it returns 0
 
 								StartReturn sr=startQuestion(rt,request,us,tq, rs.getInt(4),rs.getInt(5),rs.getInt(6));
 								sXHTML=sr.getXHTML();
 
 								// Get list of parameters
 								rs=oq.queryQuestionActionParams(dat,us.iDBqi);
 
 								NameValuePairs[] ap=new NameValuePairs[iMaxSeq+1];
 								boolean bValid=rs.next();
 								for(int iSeq=1;iSeq<=iMaxSeq;iSeq++)
 								{
 									// Build parameter list
 									ap[iSeq]=new NameValuePairs();
 									while(bValid && rs.getInt(1)==iSeq)
 									{
 										ap[iSeq].add(rs.getString(2),rs.getString(3));
 										bValid=rs.next();
 									}
 								}
 								rt.lDatabaseElapsed+=dat.finish();
 
 								// Loop around each sequenced event
 								for(int iSeq=1;iSeq<=iMaxSeq;iSeq++)
 								{
 									// Do process
 									ProcessReturn pr=us.oss.process(rt,
 										ap[iSeq].getNames(),ap[iSeq].getValues());
 
 									if(pr.isQuestionEnd())
 										throw new OmException("Unepected end of question in replay attempt");
 
 									// Apply changes to session
 									applySessionChanges(us,pr);
 
 									// Note: Any results that are sent are ignored, as presumably
 									// we already stored them.
 
 									// Get XHTML from last one
 									if(iSeq==iMaxSeq)
 									{
 										sXHTML=pr.getXHTML();
 									}
 								}
 								us.iDBseq=iMaxSeq+1;
 							}
 						}
 					}
 					finally
 					{
 						rt.lDatabaseElapsed+=dat.finish();
 					}
 				}
 
 				// If there was no previous question, start question afresh
 				if(sXHTML==null)
 				{
 					int iAttempt=initQuestionAttempt(rt,us);
 					StartReturn sr=startQuestion(rt,request,us,tq, iAttempt,0,0);
 					sXHTML=sr.getXHTML();
 				}
 
 				// Serve XHTML
 				serveQuestionPage(rt,us,tq,sXHTML,bNotReallyQuestion,request, response);
 			}
 		}
 		else if(tl instanceof TestInfo)
 		{
 			TestInfo ti=(TestInfo)tl;
 			boolean bNav=true;
 			if(us.getTestPosition()==0)
 			{
 				if(!ti.isDone()) bNav=false;
 				String sMessage=ti.getXHTMLString();
 				if(us.getTestDeployment().isAfterClose())
 				{
 					// Show message only if it's not after forbid - if it's after forbid
 					// students can't see it anyway and the message confuses admin.
 					if(!us.getTestDeployment().isAfterForbid())
 					{
 						sMessage+=
 							"<p><em class='warning'>This test closed on "+
 							us.getTestDeployment().displayCloseDate()+"</em>. You can still take " +
 							"the test and get immediate feedback on your performance. ";
 						switch(us.getTestDeployment().getType())
 						{
 						case TestDeployment.TYPE_NOTASSESSED :
 							break;
 						case TestDeployment.TYPE_ASSESSED_OPTIONAL :
 							sMessage+="However, you will not receive any credit</em> unless " +
 								"you have previously obtained special permission to submit late.";
 						}
 						sMessage+="</p>";
 					}
 				}
 				else if(us.getTestDeployment().hasCloseDate())
 				{
 					sMessage+=
 						"<p>This test will remain available until <strong>"+
 						us.getTestDeployment().displayCloseDate()+"</strong>. ";
 					switch(us.getTestDeployment().getType())
 					{
 					case TestDeployment.TYPE_NOTASSESSED :
 						sMessage+="Please ensure that you complete and submit the test by " +
 							"the end of that day.";
 						break;
 					case TestDeployment.TYPE_ASSESSED_REQUIRED :
 						sMessage+="Please ensure that you complete and submit the test by " +
 							"the end of that day. You will not receive credit for answers " +
 							"submitted later.";
 						break;
 					case TestDeployment.TYPE_ASSESSED_OPTIONAL :
 						sMessage+="If you wish this test to count towards your course mark, " +
 							"please ensure that you complete and submit the test by the end " +
 							"of that day. You will not receive credit for answers submitted " +
 							"later.";
 						break;
 					}
 					sMessage+="</p>";
 				}
 
 				if(us.sOUCU.startsWith("_"))
 				{
 					sMessage+=
 						"<p>Your progress through the test will be stored in your browser, " +
 						"so you can leave the test and return on another day if you " +
 						"don't have time to complete it now.</p>" +
 						auth.getLoginOfferXHTML(request);
 				}
 
 				if(us.bAdmin)
 				{
 					sMessage+=
 						"<div class='adminmsg'><p>You have administrative privileges on this test. The questions you will " +
 						"see are the same as those shown to students, but you also have the " +
 						"ability to repeat an individual question or the whole test, and jump " +
 						"between questions. Depending on the test settings, these options may " +
 						"not be available to students.</p>" +
 						"<p>Even if you end the test, your results will not be " +
 						"transferred to any reporting systems (so feel free to " +
 						"end it if you want to see what happens; you can restart it " +
 						"afterwards).</p>";
 
 					if(!us.getTestDeployment().isAfterOpen())
 						sMessage+=
 							"<p>This test is not yet open to students. It opens on " +
 							us.getTestDeployment().displayOpenDate()+". Students will receive " +
 							"an error if they try to access it before then.</p>";
 
 					if(us.getTestDeployment().isAfterForbid())
 						sMessage+=
 							"<p>This test is no longer available to students. Access was " +
 							"forbidden to students after " +
 							us.getTestDeployment().displayForbidDate()+". If students try to access " +
 							"it now, they will receive an error.</p>";
 
 					if(us.bAllowReports)
 						sMessage+=
 							"<p>You have access to view " +
 							"<a href='reports!'>reports on this test</a>, such as student " +
 							"results.</p>";
 
 					sMessage+="</div>";
 				}
 
 				serveTestContent(us,
 					ti.getTitle(),
 					"",null,
 					"","<div class='basicpage'>"+
 					sMessage+
 					"<p><a id='focusthis' href='?next'>Begin</a></p>" +
 					"</div>",bNav, request, response, true);
 			}
 			else
 			{
 				serveTestContent(us,
 					ti.getTitle(),
 					"",null,
 					"","<div class='basicpage'>"+
 					ti.getXHTMLString()+
 					"<p><a id='focusthis' href='?next'>Next</a></p>" +
 					"</div>",true, request, response, true);
 			}
 			if(!ti.isDone())
 			{
 				ti.setDone(true);
 				DatabaseAccess.Transaction dat=da.newTransaction();
 				try
 				{
 					oq.insertInfoPage(dat,us.getDbTi(),us.getTestPosition());
 				}
 				finally
 				{
 					rt.lDatabaseElapsed+=dat.finish();
 				}
 			}
 		}
 	}
 
 	private void serveQuestionPage(RequestTimings rt,UserSession us,TestQuestion tq,String sXHTML,
 			boolean notReallyQuestion,HttpServletRequest request, HttpServletResponse response) throws IOException,OmException
 	{
 		us.sSequence=Math.random()+"";
 		if (!notReallyQuestion) {
 		sXHTML = "<form method='post' action='./' autocomplete='off'>" +
 				"<input type='hidden' name='" + SEQUENCEFIELD + "' value = '" + us.sSequence + "' />" +
 				sXHTML +
 				"</form>";
 		}
 		
 		if(us.isSingle())
 		{
 			Element eMetadata=getQuestionMetadata(rt,tq.getID(),
 				getLatestVersion(tq.getID(),tq.getVersion()).toString());
 			serveTestContent(us,XML.hasChild(eMetadata,"title") ? XML.getText(eMetadata,"title") : "Question","",
 					null,us.sProgressInfo,sXHTML,true, request, response, false);
 		}
 		else
 		{
 			if(us.getTestDefinition()!=null && us.getTestDefinition().areQuestionsNamed())
 			{
 				Element eMetadata=getQuestionMetadata(rt,tq.getID(),
 					getLatestVersion(tq.getID(),tq.getVersion()).toString());
 				if(XML.hasChild(eMetadata,"title"))
 				{
 					serveTestContent(us,XML.getText(eMetadata,"title"),"("+tq.getNumber()+" of "+getQuestionMax(us)+")",
 							us.bAdmin ? tq.getID() : null,us.sProgressInfo,sXHTML,true, request, response, notReallyQuestion);
 					return;
 				}
 			}
 
 			// Either not using names, or question is unnamed
 			serveTestContent(us,"Question "+tq.getNumber(),"(of "+getQuestionMax(us)+")",
 				us.bAdmin ? tq.getID() : null,us.sProgressInfo,sXHTML,true, request, response, notReallyQuestion);
 		}
 	}
 
 	/**
 	 * Performs database initialisation for current question so that a new attempt
 	 * may start. Does not actually start a question session.
 	 * @param us Session (uses current index to determine the question)
 	 * @return Number of this attempt
 	 * @throws SQLException If the database bitches
 	 */
 	private int initQuestionAttempt(RequestTimings rt,UserSession us) throws SQLException
 	{
 		if(us.isSingle()) return 1;
 
 		int iAttempt;
 		TestQuestion tq=(TestQuestion)us.getTestLeavesInOrder()[us.getTestPosition()];
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		try
 		{
 			// See if there's an existing attempt
 			ResultSet rs=oq.queryMaxQuestionAttempt(dat,us.getDbTi(),tq.getID());
 			int iMaxAttempt=0;
 			if(rs.next() && rs.getMetaData().getColumnCount()>0) iMaxAttempt=rs.getInt(1);
 			iAttempt=iMaxAttempt+1;
 
 			// Initially zero version - we set this later when question is started
 			oq.insertQuestion(dat,us.getDbTi(),tq.getID(),iAttempt);
 			us.iDBqi=oq.getInsertedSequenceID(dat,"questions","qi");
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 		return iAttempt;
 	}
 
 	/**
 	 * @param iAttempts 'Attempts' value
 	 * @return String describing value, for use in summary tables
 	 */
 	public static String getAttemptsString(int iAttempts)
 	{
 		switch(iAttempts)
 		{
 		case ATTEMPTS_PARTIALLYCORRECT : return "Partially correct";
 		case ATTEMPTS_WRONG : return "Incorrect";
 		case ATTEMPTS_PASS : return "Passed";
 		case 1 : return "Correct at 1st attempt";
 		case 2 : return "Correct at 2nd attempt";
 		case 3 : return "Correct at 3rd attempt";
 		default : return "Correct at "+iAttempts+"th attempt";
 		}
 	}
 
 	/**
 	 * @return the OmQueries object for this servlet.
 	 */
 	public OmQueries getOmQueries()
 	{
 		return oq;
 	}
 
 	private String showCompletedQuestion(RequestTimings rt,UserSession us,HttpServletRequest request, int iQI) throws Exception
 	{
 		// Produce output based on this
 		String sXHTML=
 			"<div class='basicpage'>";
 
 		if(us.getTestDefinition().isSummaryAllowed())
 		{
 			if(us.getTestDefinition().doesSummaryIncludeScores())
 			{
 				// Update score data, we'll need it later
 				us.getTestRealisation().getScore(rt, this, da, oq);
 			}
 
 			// Get results (question and users's answer only) from this question
 			DatabaseAccess.Transaction dat=da.newTransaction();
 			try
 			{
 				ResultSet rs=oq.queryQuestionResults(dat,iQI);
 				if(rs.next())
 				{
 					sXHTML+=
 						"<p>You have already answered this question.</p>" +
 						"<table class='leftheaders'>";
 					if(us.getTestDefinition().doesSummaryIncludeQuestions()) sXHTML+=
 						"<tr><th scope='row'>Question</th><td>"+XHTML.escape(rs.getString(1),XHTML.ESCAPE_TEXT)+"</td></tr>" +
 						"<tr><th scope='row'>Your answer</th><td>"+XHTML.escape(rs.getString(2),XHTML.ESCAPE_TEXT)+"</td></tr>";
 					if(us.getTestDefinition().doesSummaryIncludeAttempts()) sXHTML+=
 						"<tr><th scope='row'>Result</th><td>"+XHTML.escape(getAttemptsString(rs.getInt(3)),XHTML.ESCAPE_TEXT)+"</td></tr>";
 					if(us.getTestDefinition().doesSummaryIncludeScores())
 					{
 						// Get score (scaled)
 						TestQuestion tq=(TestQuestion)us.getTestLeavesInOrder()[us.getTestPosition()];
 						CombinedScore ps=tq.getScoreContribution(us.getRootTestGroup());
 						String[] asAxes=ps.getAxesOrdered();
 						for(int iAxis=0;iAxis<asAxes.length;iAxis++)
 						{
 							String sAxis=asAxes[iAxis];
 							String
 								sScore=displayScore(ps.getScore(sAxis)),
 								sMax=displayScore(ps.getMax(sAxis));
 
 							sXHTML+="<tr><th scope='row'>Score"+
 								(sAxis==null? "" : " ("+sAxis+")")+"</th><td>"+
 								sScore+" out of "+sMax+"</td></tr>";
 						}
 					}
 					sXHTML+=
 						"</table>";
 				}
 				else
 				{
 					sXHTML+=
 						"<p>You have already answered this question, but question and " +
 						"answer text have not been recorded.</p>";
 				}
 			}
 			finally
 			{
 				rt.lDatabaseElapsed+=dat.finish();
 			}
 			sXHTML+=
 				"<p><a href='?summary'>View a summary of all your answers so far</a></p>"+
 				"<p><a href='?next'>Next question</a></p>";
 		}
 		else
 		{
 			sXHTML+=
 				"<p>You have already answered this question.</p>";
 		}
 
 		if(us.getTestDefinition().isRedoQuestionAllowed() || us.bAdmin)
 		{
 			if(!us.getTestDefinition().isRedoQuestionAllowed())
 			{
 				sXHTML+="<div class='adminmsg'>You are only permitted to redo the " +
 					"question because you have admin privileges. Students will not see " +
 					"the following button.</div>";
 			}
 
 			if(inPlainMode(request))
 			{
 				sXHTML+=
 					"<form action='?redo' method='post'>" +
 					"<p>If you redo the question, your new answer will be considered instead of " +
 					"the one above.</p>"+
 					"<input type='submit' value='Redo this question'/> " +
 					"</form>";
 			}
 			else
 			{
 				sXHTML+=
 					"<form action='?redo' method='post'>" +
 					"<input type='submit' value='Redo this question'/> " +
 					"(If you redo it, your new answer will be considered instead of the one above.)" +
 					"</form>";
 			}
 		}
 
 		sXHTML+=
 			"</div>";
 
 		return sXHTML;
 	}
 
 	private void handleNothing(RequestTimings rt,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		if(us.oss==null)
 			servePage(rt,us,false,request, response);
 		else
 			doQuestion(rt,us,request,response,new NameValuePairs());
 	}
 
 	private void handleEnd(RequestTimings rt,boolean bPost,boolean bTimeOver,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Get rid of question session if there is one
 		stopQuestionSession(rt, us);
 
 		// If they can't navigate then this really is the end, mark it finished
 		// and we're gone. Also if we are currently actually posting.
 		if(!us.getTestDefinition().isNavigationAllowed() || bPost)
 		{
 			// Set in database
 			DatabaseAccess.Transaction dat=da.newTransaction();
 			try
 			{
 				oq.updateTestFinished(dat,us.getDbTi());
 				us.setFinished(true);
 			}
 			finally
 			{
 				rt.lDatabaseElapsed+=dat.finish();
 			}
 
 			// Send an email if desired - to non-admins and only people who look like students
 			if(us.getTestDeployment().requiresSubmitEmail() && !us.bAdmin && us.ud.shouldReceiveTestMail())
 			{
 				String sEmail=IO.loadString(
 						new FileInputStream(getServletContext().getRealPath("WEB-INF/templates/submit.email.txt")));
 				Map<String,String> mReplace=new HashMap<String,String>();
 				mReplace.put("NAME", us.getTestDefinition().getName());
 				mReplace.put("TIME",(new SimpleDateFormat("dd MMMM yyyy HH:mm")).format(new Date()));
 				sEmail=XML.replaceTokens(sEmail, "%%", mReplace);
 
 				try
 				{
 					String token=auth.sendMail(us.sOUCU,us.ud.getPersonID(),sEmail,
 						Authentication.EMAIL_CONFIRMSUBMIT);
 					getLog().logNormal("Sent submit confirm email to "+us.sOUCU+" for "+
 						us.getTestId()+" (message despatch ID "+token+")");
 					us.iEmailSent=1;
 				}
 				catch(Exception e)
 				{
 					getLog().logError("Error sending submit confirm email to "+us.sOUCU+
 							" for "+us.getTestId(),e);
 					us.iEmailSent=-1;
 				}
 			}
 
 			// Redirect back to test page
 			response.sendRedirect(
 				request.getRequestURL().toString().replaceAll("/[^/]*$","/")+endOfURL(request));
 		}
 		else
 		{
 			// The 'end, are you sure?' page
 			Document d=XML.parse(new File(getServletContext().getRealPath("WEB-INF/templates/endcheck.xhtml")));
 			Map<String,Object> mReplace=new HashMap<String,Object>();
 			mReplace.put("EXTRA",endOfURL(request));
 			mReplace.put("BUTTON",us.getTestDefinition().getConfirmButtonLabel());
 			mReplace.put("CONFIRMPARAS",us.getTestDefinition().getConfirmParagraphs());
 			if(bTimeOver)
 				XML.remove(XML.find(d,"id","return"));
 			else
 				XML.remove(XML.find(d,"id","timeover"));
 
 			XML.replaceTokens(d,mReplace);
 			if(us.getTestDefinition().isSummaryIncludedAtEndCheck())
 			{
 				Element eDiv=XML.find(d,"id","summarytable");
 //				XML.createText(eDiv,"h3","Your answers");
 				boolean bScores=us.getTestDefinition().doesSummaryIncludeScores();
 				if(bScores)
 				{
 					// Build the scores list from database
 					us.getTestRealisation().getScore(rt, this, da, oq);
 				}
 				addSummaryTable(rt,us,eDiv,inPlainMode(request),
 					us.getTestDefinition().doesSummaryIncludeQuestions(),
 					us.getTestDefinition().doesSummaryIncludeAttempts(),
 					bScores);
 			}
 
 			serveTestContent(us,us.getTestDefinition().getConfirmTitle(),"",null,null,XML.saveString(d),false, request, response, true);
 		}
 	}
 
 	private void handleVariant(RequestTimings rt,String sVariant,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Check access
 		if(!us.bAdmin)
 		{
 			sendError(us,request,response,HttpServletResponse.SC_FORBIDDEN,
 				false,false,null, "Access denied", "You do not have access to this feature.", null);
 		}
 
 		// Check not mid-test
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		int iCount=0;
 		try
 		{
 			ResultSet rs=oq.queryQuestionAttemptCount(dat,us.getDbTi());
 			rs.next();
 			iCount=rs.getInt(1);
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 
 		if(iCount>0 || us.getTestPosition()!=0)
 		{
 			sendError(us,request,response,HttpServletResponse.SC_FORBIDDEN,
 				false,false,null, "Cannot change variant mid-test", "You cannot change variant in the " +
 				"middle of a test. End the test first, then change variant while on " +
 				"the initial page.", null);
 		}
 
 		// Get rid of question session if there is one
 		stopQuestionSession(rt, us);
 
 		// Set variant
 		try
 		{
 			us.setFixedVariant(Integer.parseInt(sVariant));
 		}
 		catch(NumberFormatException nfe)
 		{
 			sendError(us,request,response,HttpServletResponse.SC_NOT_FOUND,
 				false,false,null, "Invalid variant", "Variant must be a number.", null);
 		}
 
 		// Set in database
 		dat=da.newTransaction();
 		try
 		{
 			oq.updateTestVariant(dat,us.getDbTi(),us.getFixedVariant());
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 
 		// Redirect back
 		redirectToTest(request,response);
 	}
 
 	private void handleAccess(RequestTimings rt,boolean bPost,boolean bPlainJump,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		// Get rid of question session if there is one
 		stopQuestionSession(rt, us);
 
 		// Either if jumping or submitting the form...
 		if(bPost || bPlainJump)
 		{
 			String sNewCookie;
 			if(bPlainJump)
 			{
 				// Get current cookie
 				sNewCookie=getAccessibilityCookie(request);
 				if(sNewCookie.indexOf("[plain]")==-1)
 					sNewCookie+="[plain]";
 				else
 					sNewCookie=sNewCookie.replaceAll("\\[plain\\]","");
 			}
 			else
 			{
 				sNewCookie="";
 				String
 					sZoom=request.getParameter("zoom"),
 					sColours=request.getParameter("colours"),
 					sPlain=request.getParameter("plainmode");
 				if(sZoom!=null && !sZoom.equals("normal"))
 					sNewCookie+="[zoom="+sZoom+"]";
 				if(sColours!=null && !sColours.equals("normal"))
 					sNewCookie+="[colours="+sColours+"]";
 				if(sPlain!=null && sPlain.equals("yes"))
 					sNewCookie+="[plain]";
 			}
 
 			// Set cookie
 			Cookie c=new Cookie(ACCESSCOOKIENAME,sNewCookie);
 			c.setPath("/");
 			if(sNewCookie.equals(""))
 				c.setMaxAge(0); // Delete cookie
 			else
 				c.setMaxAge(60*60*24*365*10); // 10 years
 			response.addCookie(c);
 
 			// Redirect back to test page
 			redirectToTest(request,response);
 		}
 		else
 		{
 			// The form
 			Document d=XML.parse(new File(getServletContext().getRealPath("WEB-INF/templates/accessform.xhtml")));
 			Map<String,String> mReplace=new HashMap<String,String>();
 			mReplace.put("EXTRA",endOfURL(request));
 			XML.replaceTokens(d,mReplace);
 
 			// Get cookie and update initial values
 			String sCookie=getAccessibilityCookie(request);
 			if(sCookie.indexOf("[zoom=1.5]")!=-1)
 				XML.find(d,"id","bzoom").setAttribute("checked","checked");
 			else if(sCookie.indexOf("[zoom=2.0]")!=-1)
 				XML.find(d,"id","czoom").setAttribute("checked","checked");
 			else
 				XML.find(d,"id","azoom").setAttribute("checked","checked");
 
 			Matcher m=COLOURSPATTERN.matcher(sCookie);
 			if(m.matches())
 			{
 				Element e=XML.find(d.getDocumentElement(),"value",m.group(1));
 				if(e!=null) e.setAttribute("checked","checked");
 			}
 			else
 				XML.find(d,"id","acolours").setAttribute("checked","checked");
 
 			if(sCookie.indexOf("[plain]")!=-1)
 				XML.find(d,"id","plainmode").setAttribute("checked","checked");
 
 			serveTestContent(us,"Display options","",null,null,XML.saveString(d),false, request, response, true);
 		}
 	}
 
 	private void redirectToTest(HttpServletRequest request,HttpServletResponse response) throws IOException
 	{
 		response.sendRedirect(
 			request.getRequestURL().toString().replaceAll("/[^/]*$","/")+endOfURL(request));
 	}
 
 	/**
 	 * @param request HTTP request
 	 * @return The accessibility cookie, or an empty string if it isn't set
 	 */
 	private String getAccessibilityCookie(HttpServletRequest request)
 	{
 		String sCookie="";
 		Cookie[] ac=request.getCookies();
 		if(ac==null) ac=new Cookie[0];
 		for(int i=0;i<ac.length;i++)
 		{
 			if(ac[i].getName().equals(ACCESSCOOKIENAME))
 			{
 				sCookie=ac[i].getValue();
 			}
 		}
 		return sCookie;
 	}
 
 	private final static String ACCESSOUTOFSEQUENCE="Access out of sequence";
 
 	private void handleProcess(RequestTimings rt,
 		UserSession us,HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		NameValuePairs p=new NameValuePairs();
 		for(Enumeration e=request.getParameterNames();e.hasMoreElements();)
 		{
 			String
 				sName=(String)e.nextElement(),
 				sValue=request.getParameter(sName);
 
 			if(sName.equals(SEQUENCEFIELD))
 			{
 				if(us.sSequence==null || !us.sSequence.equals(sValue))
 				{
 					sendError(us,request,response,
 						HttpServletResponse.SC_FORBIDDEN,
 						false,
 						false, null, ACCESSOUTOFSEQUENCE, "You have entered data outside the normal sequence. This can occur " +
 						"if you use your browser's Back or Forward buttons; please don't use " +
 						"these during the test. It can also happen if you click on something " +
 						"while a page is loading.", null);
 				}
 			}
 			else if(sValue.length() > 2048)
 			{
 				sendError(us,request,response,
 					HttpServletResponse.SC_FORBIDDEN,
 					false,
 					false,null, INPUTTOOLONG, "You have entered more data than we allow. Please don't enter more " +
 					"data than is reasonable.", null);
 			}
 			else
 			{
 				p.add(sName,sValue);
 			}
 		}
 		// Plain mode gets added as a parameter because components might need to
 		// know it in order to properly interpret input data (e.g. when replayed,
 		// even after not in plain mode any more)
 		if(inPlainMode(request))
 			p.add("plain","yes");
 
 		doQuestion(rt,us,request,response,p);
 	}
 
 	private void doQuestion(RequestTimings rt,UserSession us,
 		HttpServletRequest request,HttpServletResponse response,NameValuePairs p)
 		throws Exception
 	{
 		ProcessReturn pr=us.oss.process(rt,p.getNames(),p.getValues());
 
 		// Store details in database
 		TestQuestion tq=((TestQuestion)us.getTestLeavesInOrder()[us.getTestPosition()]);
 		if(!us.isSingle())
 		{
 			DatabaseAccess.Transaction dat=da.newTransaction();
 			try
 			{
 				// Add action and params
 				oq.insertAction(dat,us.iDBqi,us.iDBseq);
 				for(int i=0;i<p.getNames().length;i++)
 				{
 					oq.insertParam(dat,us.iDBqi,us.iDBseq,p.getNames()[i],p.getValues()[i]);
 				}
 				us.iDBseq++;
 
 				// Add results if any
 				if(pr.getResults()!=null)
 				{
 					// Mark done as soon as we get results
 					tq.setDone(true);
 
 					Results r=pr.getResults();
 
 					// Store summary text
 					oq.insertResult(dat,us.iDBqi,
 						r.getQuestionLine()==null ? "" : r.getQuestionLine(),
 						r.getAnswerLine()==null ? "" : r.getAnswerLine(),
 						r.getActionSummary()==null ? "" : r.getActionSummary(),
 						r.getAttempts());
 
 					// Store scores
 					Score[] as=r.getScores();
 					for(int iScore=0;iScore<as.length;iScore++)
 					{
 						Score s=as[iScore];
 						oq.insertScore(dat,us.iDBqi,
 							s.getAxis()==null?"":s.getAxis(),s.getMarks());
 					}
 
 					// Store custom results
 					CustomResult[] acr=pr.getResults().getCustomResults();
 					for(int iCustom=0;iCustom<acr.length;iCustom++)
 					{
 						oq.insertCustomResult(dat,us.iDBqi,
 							acr[iCustom].getName(),acr[iCustom].getValue());
 					}
 
 					if(!pr.isQuestionEnd()) // Don't bother if we're about to mark it as 2
 						oq.updateQuestionFinished(dat,us.iDBqi,1);
 				}
 
 				// Add end if any
 				if(pr.isQuestionEnd())
 				{
 					oq.updateQuestionFinished(dat,us.iDBqi,2);
 				}
 			}
 			finally
 			{
 				rt.lDatabaseElapsed+=dat.finish();
 			}
 		}
 
 		if(pr.isQuestionEnd())
 		{
 			// Mark done again just so it works while the questions don't return
 			// results
 			tq.setDone(true);
 
 			// Get rid of current question session (no need to stop it)
 			us.oss=null;
 			us.mResources.clear();
 
 			if(us.isSingle()) // Restart question
 			{
 				servePage(rt,us,false,request, response);
 			}
 			else
 			{
 				// Go onto next un-done question that's available
 				if(!findNextQuestion(rt,us,false,!us.getTestDefinition().isRedoQuestionAllowed(),!us.getTestDefinition().isRedoQuestionAllowed()))
 				{
 					// No more? Jump to end
 					redirectToEnd(request,response);
 				}
 				else
 				{
 					// Serve that page
 					servePage(rt,us,false,request, response);
 				}
 			}
 		}
 		else
 		{
 			applySessionChanges(us,pr);
 
 			// Serve XHTML
 			serveQuestionPage(rt,us,tq,pr.getXHTML(),false,request, response);
 		}
 	}
 
 	private static int getQuestionMax(UserSession us) throws OmException
 	{
 		for(int i=us.getTestLeavesInOrder().length-1;i>=0;i--)
 		{
 			if(us.getTestLeavesInOrder()[i] instanceof TestQuestion)
 			{
 				return ((TestQuestion)us.getTestLeavesInOrder()[i]).getNumber();
 			}
 		}
 		throw new OmException("No questions??");
 	}
 
 	/**
 	 * Moves to the next question, skipping those that are unavailable or that
 	 * have been done already.
 	 * @param us Current session
 	 * @param bFromBeginning If true, starts from first question instead
 	 *	 of looking at the next one after us.iIndex.
 	 * @param bSkipDone If true, skips 'done' questions, otherwise only skips
 	 *	 unavailable ones
 	 * @param bWrap If false, doesn't wrap to first question
 	 * @return True if moved successfully, false if all questions are
 	 *	 unavailable/done
 	 */
 	private boolean findNextQuestion(RequestTimings rt,UserSession us,boolean bFromBeginning,boolean bSkipDone,boolean bWrap)
 		throws SQLException
 	{
 		// Initial check for whether all questions are done; if so it skips to end
 		if(bSkipDone)
 		{
 			boolean bAllQuestionsDone=true;
 			for(int i=0;i<us.getTestLeavesInOrder().length;i++)
 			{
 				if(!us.getTestLeavesInOrder()[i].isDone() && (us.getTestLeavesInOrder()[i] instanceof TestQuestion))
 				{
 					bAllQuestionsDone=false;
 					break;
 				}
 			}
 			if(bAllQuestionsDone)
 				return false; // Make it skip to end, not to an info page
 		}
 
 		int iNewIndex=us.getTestPosition(),iBeforeIndex=us.getTestPosition();
 		if(bFromBeginning)
 		{
 			iNewIndex=-1;
 		}
 
 		while(true)
 		{
 			// Next question (wrap around)
 			iNewIndex++;
 			if(iNewIndex>=us.getTestLeavesInOrder().length)
 			{
 				if(bWrap)
 					iNewIndex=0;
 				else
 				{
 					iNewIndex--;
 					return false;
 				}
 			}
 
 
 			// Run out of questions?
 			if(iNewIndex==iBeforeIndex)
 			{
 				return false;
 			}
 
 			// After first time (so we don't stop immediately), make it only stop
 			// after looping once
 			if(bFromBeginning)
 			{
 				iBeforeIndex=0;
 				bFromBeginning=false;
 			}
 
 			// See if question is available and not done
 			if((!bSkipDone || !us.getTestLeavesInOrder()[iNewIndex].isDone()) && us.getTestLeavesInOrder()[iNewIndex].isAvailable())
 				break;
 		}
 
 		setIndex(rt,us,iNewIndex);
 
 		return true;
 	}
 
 	/**
 	 * Update position within text.
 	 * @param rt Request timings
 	 * @param us Session
 	 * @param iNewIndex New index value
 	 * @throws SQLException Any database problem in storing new position
 	 */
 	private void setIndex(RequestTimings rt,UserSession us,int iNewIndex) throws SQLException
 	{
 		if(us.getTestPosition()==iNewIndex) return;
 
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		try
 		{
 			oq.updateSetTestPosition(dat,us.getDbTi(),iNewIndex);
 			us.setTestPosition(iNewIndex);
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 	}
 
 	// Method is NOT synchronized on UserSession
 	private void handleCSS(UserSession us,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		String sCSS;
 		synchronized(us)
 		{
 			sCSS=us.sCSS;
 		}
 
 		response.setContentType("text/css");
 		response.setCharacterEncoding("UTF-8");
 		response.setHeader("Cache-Control","max-age="+RESOURCEEXPIRETIME);
 		response.getWriter().write(sCSS);
 		response.getWriter().close();
 	}
 
 	/** Resources expire after 1 hour */
 	private final static long RESOURCEEXPIRETIME=60*60;
 
 	// Method is NOT synchronized on UserSession
 	private void handleResource(String sResource,UserSession us,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		Resource r;
 		synchronized(us)
 		{
 			r=us.mResources.get(sResource);
 		}
 		if(r==null)
 		{
 			sendError(null, request, response, HttpServletResponse.SC_NOT_FOUND,
 					true, false, null, "Not found",
 					"Requested resource '" + sResource + "' not found.", null);
 		}
 		response.setContentType(r.getMimeType());
 		response.setContentLength(r.getContent().length);
 		response.setHeader("Cache-Control","max-age="+RESOURCEEXPIRETIME);
 		if(r.getEncoding()!=null)
 			response.setCharacterEncoding(r.getEncoding());
 		response.getOutputStream().write(r.getContent());
 		response.getOutputStream().close();
 	}
 
 	private boolean checkRestartSession(RequestTimings rt,String sTestID,UserSession us,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		// Look in database for session to restart
 		DatabaseAccess.Transaction dat=da.newTransaction();
 		try
 		{
 			// Search for matching unfinished sessions for this OUCU
 			String sOUCU=us.sOUCU;
 			l.logDebug("Looking for a lost session for user " + sOUCU + " on test " + sTestID);
 			ResultSet rs=oq.queryUnfinishedSessions(dat,sOUCU,sTestID);
 			int iDBti=-1;
 			long lRandomSeed=0;
 			boolean bFinished=false;
 			int iTestVariant=-1;
 			int iPosition=-1;
 			String navigatorversion = "";
 			if(rs.next())
 			{
 				iDBti=rs.getInt(1);
 				lRandomSeed=rs.getLong(2);
 				bFinished=rs.getInt(3)==1;
 				iTestVariant=rs.getInt(4);
 				if(rs.wasNull()) iTestVariant=-1;
 				iPosition=rs.getInt(5);
 				navigatorversion=rs.getString(6);
 			}
 
 			// No match? OK, return false
 			if(iDBti==-1)	return false;
 
 			// Log IP address
 			storeSessionInfo(request,dat,iDBti);
 
 			// Set up basic data
 			us.setDbTi(iDBti);
 			initTestSession(us,rt,sTestID,request, response, bFinished, true, lRandomSeed, iTestVariant);
 			us.setTestPosition(iPosition);
 			us.navigatorVersion = navigatorversion;
 
 			// Find out which question they're on
 			if(!us.isFinished())
 			{
 				// Find out which questions they've done (counting either 'getting
 				// results' or 'question end')
 				rs=oq.queryDoneQuestions(dat,us.getDbTi());
 
 				// Get all those question IDs
 				Set<String> sDone=new HashSet<String>();
 				while(rs.next()) sDone.add(rs.getString(1));
 
 				// Go through marking questions done
 				for(int i=0;i<us.getTestLeavesInOrder().length;i++)
 				{
 					if(us.getTestLeavesInOrder()[i] instanceof TestQuestion)
 					{
 						TestQuestion tq=(TestQuestion)us.getTestLeavesInOrder()[i];
 						if(sDone.contains(tq.getID()))
 							tq.setDone(true);
 					}
 				}
 
 				// Find out info pages they've done
 				rs=oq.queryDoneInfoPages(dat,us.getDbTi());
 				while(rs.next())
 				{
 					int iIndex=rs.getInt(1);
 					TestInfo ti=(TestInfo)us.getTestLeavesInOrder()[iIndex];
 					ti.setDone(true);
 				}
 			}
 		}
 		finally
 		{
 			rt.lDatabaseElapsed+=dat.finish();
 		}
 
 		// Redirect to system-check page if needed
 		doSystemCheck(us,request,response);
 
 		// Serve that question, or end page if bFinished
 		servePage(rt,us,true,request,response);
 
 		return true;
 	}
 
 	/**
 	 * If necessary, redirects user to the system check page. This is done only
 	 * once the system is actually ready to serve test content, in order to
 	 * avoid potential issues if the user requests something else unexpectedly
 	 * between sending this and returning to the main test page.
 	 * <p>
 	 * Does not continue (throws StopException) if the redirect is sent.
 	 * @param us Session
 	 * @param request HTTP request
 	 * @param response HTTP response
 	 * @throws IOException Any error
 	 * @throws StopException
 	 */
 	private void doSystemCheck(UserSession us,HttpServletRequest request,HttpServletResponse response)
 		throws IOException,StopException
 	{
 		// Initial request gets redirected to the system-check Javascript page
 		if(!us.bCheckedBrowser && !us.getTestDeployment().isSingleQuestion()) // Don't do that for single question
 		{
 			us.bCheckedBrowser=true;
 			response.sendRedirect(
 				getServletURL(request)+"!shared/systemcheck.html?"+
 					URLEncoder.encode(request.getRequestURI(),"UTF-8"));
 			throw new StopException();
 		}
 	}
 
 
 
 
 	private void handleQuestion(String sIDVersion,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		if(!nc.isTrustedQE(InetAddress.getByName(request.getRemoteAddr())))
 		{
 			sendError(null,request,response,HttpServletResponse.SC_FORBIDDEN,
 				false,false,null, "Forbidden", "You are not authorised to access this URL.", null);
 		}
 
 		// Get question jar file
 		File f=new File(
 			questionBankFolder,
 			sIDVersion+".jar");
 		if(!f.exists())
 		{
 			sendError(null,request,response,
 				HttpServletResponse.SC_NOT_FOUND,false,false, null, "Not found", "The requested question is not present on this server.", null);
 		}
 
 		// Send jar to requester
 		byte[] abQuestion=IO.loadBytes(new FileInputStream(f));
 		response.setContentType("application/x-openmark");
 		response.setContentLength(abQuestion.length);
 		OutputStream os=response.getOutputStream();
 		os.write(abQuestion);
 		os.close();
 	}
 
 	private void handleForbid(String sOucuTest,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		if(!nc.isTrustedTN(InetAddress.getByName(request.getRemoteAddr())))
 		{
 			sendError(null,request,response,HttpServletResponse.SC_FORBIDDEN,
 				false,false,null, "Forbidden", "You are not authorised to access this URL.", null);
 		}
 
 		synchronized(sessions)
 		{
 			// Ditch existing session
 			UserSession us=usernames.remove(sOucuTest);
 			if(us!=null) sessions.remove(us.sCookie);
 
 			// Forbid that user for 1 minute [this is intended to prevent the
 			// possibility of timing issues allowing a user to get logged on to both
 			// servers at once; should that happen, chances are they'll instead be
 			// *dumped* from both servers at once (for 60 seconds).
 			tempForbid.put(sOucuTest,System.currentTimeMillis() + 60000L);
 
 			// Send response
 			response.setContentType("text/plain");
 			response.setCharacterEncoding("UTF-8");
 			Writer w=response.getWriter();
 			w.write("OK");
 			w.close();
 		}
 	}
 
 	private void handleTestCookie(String suffix,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		auth.becomeTestUser(response,suffix);
 
 		response.setContentType("text/plain");
 		response.getWriter().write("Cookie set - you are now (on this server) !tst"+suffix);
 		response.getWriter().close();
 	}
 
 	/**
 	 * @param request HTTP request
 	 * @return True if originating IP is within OU network
 	 * @throws UnknownHostException
 	 */
 	public boolean checkLocalIP(HttpServletRequest request) throws UnknownHostException
 	{
 		// IP address must be in OU
 		if(!isIPInList(InetAddress.getByName(request.getRemoteAddr()), nc.getTrustedAddresses())) return false;
 
 		// Check originating address, if it went through the load balancer
 
 		// students.open.ac.uk actually provides the one with the underline,
 		// but I think the more standard header would be as below
 		String sClientIP=request.getHeader("client_ip");
 		if(sClientIP==null) sClientIP=request.getHeader("Client-IP");
 		if(sClientIP!=null)
 			return isIPInList(InetAddress.getByName(sClientIP), nc.getTrustedAddresses());
 
 		// No load-balancer? OK.
 		return true;
 	}
 
 
 	/**
 	 * @param request HTTP request
 	 * @return True if originating IP is within OU network
 	 * @throws UnknownHostException
 	 */
 	public boolean checkSecureIP(HttpServletRequest request) throws UnknownHostException
 	{
 		// IP address must be in OU
 		if(!isIPInList(InetAddress.getByName(request.getRemoteAddr()), nc.getSecureAddresses())) return false;
 
 		// Check originating address, if it went through the load balancer
 
 		// students.open.ac.uk actually provides the one with the underline,
 		// but I think the more standard header would be as below
 		String sClientIP=request.getHeader("client_ip");
 		if(sClientIP==null) sClientIP=request.getHeader("Client-IP");
 		if(sClientIP!=null)
 			return isIPInList(InetAddress.getByName(sClientIP), nc.getSecureAddresses());
 
 		// No load-balancer? OK.
 		return true;
 	}
 
 	/**
 	 * @param ia An address
 	 * @return True if that address is in an OU IP range
 	 */
 	private boolean isIPInList(InetAddress ia, String[] addresses)
 	{
 		byte[] ab=ia.getAddress();
 		if(ab.length==16)
 		{
 			// Check that IPv6 addresses are actually representations of IPv4 -
 			// these have lots of zeros then either 0000 or FFFF, then the number
 			for(int i=0;i<10;i++)
 				if(ab[i]!=0) return false;
 			if(!(
 				(ab[10]==0xff && ab[11]==0xff) ||
 				(ab[10]==0 && ab[11]==0) )) return false;
 			byte[] abNew=new byte[4];
 			System.arraycopy(ab,12,abNew,0,4);
 		}
 		else if(ab.length!=4) throw new Error(
 			"InetAddress that wasn't 4 or 16 bytes long?!");
 
 		for(int i=0;i<addresses.length;i++)
 		{
 			String[] bytes=addresses[i].split("\\.");
 			boolean ok=true;
 			for(int pos=0;pos<4;pos++)
 			{
 				// * allows anything
 				if(bytes[pos].equals("*")) continue;
 
 				int actual=getUnsigned(ab[pos]);
 
 				// Plain number, not a range
 				if(bytes[pos].indexOf('-')==-1)
 				{
 					if(actual!=Integer.parseInt(bytes[pos]))
 					{
 						ok=false;
 						break;
 					}
 				}
 				else // Range
 				{
 					String[] range=bytes[pos].split("-");
 					if(actual<Integer.parseInt(range[0]) || actual > Integer.parseInt(range[1]))
 					{
 						ok=false;
 						break;
 					}
 				}
 			}
 			if(ok) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param b Byte value
 	 * @return The unsigned version of that byte
 	 */
 	private int getUnsigned(byte b)
 	{
 		int i=b;
 		if(i>=0) return i;
 		return 256+i;
 	}
 
 	private void handleShared(String sFile,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		// Look in both the 'shared' and 'WEB-INF/shared' folders (allows for
 		// standard files that are replaced in update, plus extra files)
 		File
 			fUser=new File(getServletContext().getRealPath("shared/"+sFile)),
 			fInternal=new File(getServletContext().getRealPath("WEB-INF/shared/"+sFile));
 
 		// Pick one to use
 		File fActual=
 			(fUser.exists() ? fUser : (fInternal.exists() ? fInternal : null));
 		if(fActual==null)
 		{
 			sendError(null,request,response,
 				HttpServletResponse.SC_NOT_FOUND,true,false, null, "Not found", "The requested resource is not present.", null);
 		}
 
 		// Handle If-Modified-Since
 		long lIfModifiedSince=request.getDateHeader("If-Modified-Since");
 		if(lIfModifiedSince!=-1)
 		{
 			if(fActual.lastModified() <= lIfModifiedSince)
 			{
 				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
 				return;
 			}
 		}
 
 		// Set type and length
 		response.setContentType(MimeTypes.getMimeType(sFile));
 		response.setContentLength((int)fActual.length());
 
 		// Set last-modified, and expiry for 4 hours
 		response.addDateHeader("Last-Modified",fActual.lastModified());
 		response.addDateHeader("Expires",System.currentTimeMillis() + 4L*60L*60L*1000L);
 
 		// Send actual data
 		OutputStream os=response.getOutputStream();
 		IO.copy(new FileInputStream(fActual),os,true);
 	}
 
 	/** Used to remove CSS lines that have the FIXED comment at beginning */
 	private final static Pattern FIXEDCOLOURCSSLINE=Pattern.compile(
 		"^/\\*FIXED\\*/.*?$",Pattern.MULTILINE);
 
 	private static String navigatorCSS=null;
 
 	/**
 	 * Annoyingly the String replaceAll method doesn't support multi-line replace.
 	 * This does.
 	 * @param p Pattern (must have MULTILINE flag)
 	 * @param sSource Source string
 	 * @param sReplace Replacement text
 	 * @return Replaced content
 	 */
 	private static String multiLineReplace(Pattern p,String sSource,String sReplace)
 	{
 		Matcher m=p.matcher(sSource);
 		StringBuffer sb = new StringBuffer();
 		while (m.find())
 		{
 			m.appendReplacement(sb, "");
 		}
 		m.appendTail(sb);
 		return sb.toString();
 	}
 
 	private void handleNavigatorCSS(String sAccessBit,HttpServletRequest request,HttpServletResponse response)
 		throws Exception
 	{
 		// Get CSS
 		if(navigatorCSS==null)
 		{
 			navigatorCSS=IO.loadString(	new FileInputStream(new File(
 				getServletContext().getRealPath("WEB-INF/templates/navigator.css"))));
 		}
 
 		// Do replace
 		Map<String,String> mReplace=new HashMap<String,String>();
 		if(sAccessBit==null) // No accessibility
 		{
 			mReplace.put("FG","black");
 			mReplace.put("BG","white");
 		}
 		else
 		{
 			try
 			{
 				mReplace.put("FG","#"+sAccessBit.substring(0,6));
 				mReplace.put("BG","#"+sAccessBit.substring(6,12));
 			}
 			catch(IndexOutOfBoundsException ioobe)
 			{
 				mReplace.put("FG","black");
 				mReplace.put("BG","white");
 			}
 		}
 		String sCSS=XML.replaceTokens(navigatorCSS,"%%",mReplace);
 
 		// Get rid of fixed bits if it isn't fixed
 		if(sAccessBit==null)
 		{
 			sCSS=multiLineReplace(FIXEDCOLOURCSSLINE,sCSS,"");
 		}
 
 		byte[] abBytes=sCSS.getBytes("UTF-8");
 
 		// Set type and length
 		response.setContentType("text/css");
 		response.setCharacterEncoding("UTF-8");
 		response.setContentLength(abBytes.length);
 
 		// Set expiry for 4 hours
 		response.addDateHeader("Expires",System.currentTimeMillis() + 4L*60L*60L*1000L);
 
 		// Send actual data
 		OutputStream os=response.getOutputStream();
 		os.write(abBytes);
 		os.close();
 	}
 
 
 	private void addAccessibilityClasses(Document d,HttpServletRequest request,
 		 String sInitialClass)
 	{
 		// Fix up accessibility details from cookie
 		String sAccessibility=getAccessibilityCookie(request);
 		String sRootClass="";
 		if(sInitialClass!=null)
 		{
 			try
 			{
 				XML.getChild(d.getDocumentElement(),"body").setAttribute("class",sInitialClass);
 			}
 			catch(XMLException e)
 			{
 				throw new OmUnexpectedException(e);
 			}
 		}
 
 		// If in plain mode, no CSS so don't need no steenking classes
 		boolean bPlain=sAccessibility.indexOf("[plain]")!=-1;
 		if(bPlain) return;
 
 		// Zoom
 		Matcher m=ZOOMPATTERN.matcher(sAccessibility);
 		if(m.matches())
 		{
 			if(m.group(1).equals("1.5"))
 			{
 				sRootClass+="zoom15 ";
 			}
 			else if(m.group(1).equals("2.0"))
 			{
 				sRootClass+="zoom20 ";
 			}
 		}
 
 		// Browser detection, for use in CSS
 		sRootClass+=UserAgent.getBrowserString(request)+" ";
 
 		if(sRootClass.trim().length()>0)
 			d.getDocumentElement().setAttribute("class",sRootClass.trim());
 	}
 
 	/** Cached template documents */
 	private Document plainTemplate,template,singlesPlainTemplate,singlesTemplate;
 
 	/**
 	 * @param request the reqiest we are responding to.
 	 * @return a bit that goes inthe navigator(here).css name depending on access cookie.
 	 */
 	public String getAccessCSSAppend(HttpServletRequest request)
 	{
 		// Fix up accessibility details from cookie
 		String sAccessibility=getAccessibilityCookie(request);
 
 		// If in plain mode, no CSS, why is this being called anyway?
 		boolean bPlain=sAccessibility.indexOf("[plain]")!=-1;
 		if(bPlain) return "";
 
 		// Ignore zoom as it doesn't affect file (yet)
 
 		// File has colour code in
 		Matcher m=COLOURSPATTERN.matcher(sAccessibility);
 		if(m.matches())
 		{
 			return "."+m.group(1);
 		}
 
 		return "";
 	}
 
 	/**
 	 * Called from StatusPages to add key performance information into a map.
 	 * @param m Map to fill with info
 	 */
 	void obtainPerformanceInfo(Map<String,Object> m)
 	{
 		System.gc();
 		System.gc();
 
 		String sMemoryUsed=Strings.formatBytes(
 			Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
 		m.put("MEMORY",sMemoryUsed);
 
 		synchronized(sessions)
 		{
 			m.put("SESSIONS",sessions.size()+"");
 			m.put("TEMPFORBIDS",tempForbid.size()+"");
 		}
 
 		m.put("VERSION",OmVersion.getVersion());
 		m.put("BUILDDATE",OmVersion.getBuildDate());
 
 		auth.obtainPerformanceInfo(m);
 
 		m.put("DBCONNECTIONS",da.getConnectionCount()+"");
 
 		URL uThis=nc.getThisTN();
 		m.put("MACHINE",uThis.getHost().replaceAll(".open.ac.uk","")+uThis.getPath());
 
 		m.put("_qeperformance",osb.getPerformanceInfo());
 	}
 
 	/**
 	 * Serves a test content page to the user, including navigation.
 	 * @param us Session
 	 * @param sTitle Page main title
 	 * @param sAuxTitle Auxiliary title (use "" if not needed)
 	 * @param sTip Text that appears as tooltip on main heading (null if none)
 	 * @param sProgressInfo Text that appears where question progress info is shown.
 	 * @param sXHTML XHTML content of main part of page
 	 * @param bIncludeNav If true, includes the question numbers etc.
 	 * @param request HTTP request
 	 * @param response HTTP response
 	 * @param bClearCSS True if CSS should be cleared before serving this page
 	 * @throws IOException Any error in serving page
 	 */
 	public void serveTestContent(UserSession us,String sTitle,String sAuxTitle,
 		String sTip,String sProgressInfo,
 		String sXHTML,boolean bIncludeNav, HttpServletRequest request, HttpServletResponse response, boolean bClearCSS)
 		throws IOException
 	{
 		String sAccessibility=getAccessibilityCookie(request);
 		boolean bPlain=sAccessibility.indexOf("[plain]")!=-1;
 
 		if(us.getFixedVariant()>=0) sAuxTitle+=" [variant "+us.getFixedVariant()+"]";
 
 		// Create basic template
 		Document d=XML.clone(
 			us.isSingle()
 			? ( bPlain ? singlesPlainTemplate : singlesTemplate )
 			: ( bPlain ? plainTemplate : template) );
 		Map<String,Object> mReplace=new HashMap<String,Object>();
 		if(us.isSingle() || sTitle.equals(us.getTestDefinition().getName()))
 			mReplace.put("TITLEBAR",sTitle);
 		else
 			mReplace.put("TITLEBAR",us.getTestDefinition().getName()+" - "+sTitle);
 		mReplace.put("RESOURCES","resources/"+us.getTestPosition());
 		if(!bPlain)
 		{
 			if(bClearCSS)
 			{
 				// Get rid of stylesheet link
 				XML.remove(XML.find(d,"ss","here"));
 			}
 			else
 			{
 				// Get rid of stupid marker attribute, & set CSS index
 				XML.find(d,"ss","here").removeAttribute("ss");
 				mReplace.put("CSSINDEX",""+us.iCSSIndex);
 			}
 		}
 		mReplace.put("ACCESS",getAccessCSSAppend(request));
 
 		if(!us.isSingle())
 		{
 			// Tooltip stuff is only there for non-plain, non-single mode
 			if(!bPlain)
 			{
 				if(sTip!=null)
 					mReplace.put("TOOLTIP",sTip);
 				else
 					XML.find(d,"title","%%TOOLTIP%%").removeAttribute("title");
 			}
 
 			mReplace.put("TESTTITLE",us.getTestDefinition().getName());
 			mReplace.put("TITLE",sTitle);
 			mReplace.put("AUXTITLE",sAuxTitle);
 			if( (sProgressInfo==null || sProgressInfo.equals("") ))
 			{
 				XML.remove(XML.find(d,"id","progressinfo"));
 			}
 			else
 			{
 				mReplace.put("PROGRESSINFO",sProgressInfo);
 			}
 		}
 		XML.replaceTokens(d,mReplace);
 
 		// If debug hack is on, do that
 		if(nc.hasDebugFlag("allow-hacks"))
 		{
 			if((new File("c:/hack.css")).exists())
 			{
 				Element eHead=XML.getChild(d.getDocumentElement(),"head");
 				Element eLink=XML.createChild(eHead,"link");
 				eLink.setAttribute("rel","stylesheet");
 				eLink.setAttribute("type","text/css");
 				eLink.setAttribute("href","file:///c:/hack.css");
 			}
 			if((new File("c:/hack.js")).exists())
 			{
 				Element eBody=XML.getChild(d.getDocumentElement(),"body");
 				Element eLink=XML.createChild(eBody,"script");
 				eLink.setAttribute("type","text/javascript");
 				eLink.setAttribute("src","file:///c:/hack.js");
 			}
 		}
 
 		// Get question top-level element and clone it into new document
 		Element eQuestion=(Element)d.importNode(XML.parse(sXHTML).getDocumentElement(),true);
 		Element questionDiv=XML.find(d,"id","question");
 		questionDiv.appendChild(eQuestion);
 
 		if(!us.isSingle())
 		{
 			// Build progress indicator
 			Element eProgress;
 			if(bPlain)
 			{
 				eProgress=XML.find(d,"id","progressPlain");
 			}
 			else if(us.getTestDefinition().getNavLocation()==TestDefinition.NAVLOCATION_LEFT)
 			{
 				XML.remove(XML.find(d,"id","progressBottom"));
 				eProgress=XML.find(d,"id","progressLeft");
 				eProgress.setAttribute("id","progress");
 				addAccessibilityClasses(d,request,"progressleft");
 			}
 			else
 			{
 				XML.remove(XML.find(d,"id","progressLeft"));
 				eProgress=XML.find(d,"id","progressBottom");
 				eProgress.setAttribute("id","progress");
 				if (us.getTestDefinition().getNavLocation()==TestDefinition.NAVLOCATION_WIDE) {
 					addAccessibilityClasses(d,request,"progresswide");
 				} else {
 					addAccessibilityClasses(d,request,"progressbottom");
 				}
 			}
 
 			Element eCurrentSection=null;
 			String sCurrentSection=null;
 
 			// Div for the buttons section
 			Element eButtons=XML.find(d,"id","buttons");
 			if(bPlain)
 			{
 				Element eH2=eButtons.getOwnerDocument().createElement("h2");
 				XML.createText(eH2,"Options");
 				eButtons.insertBefore(eH2,eButtons.getFirstChild());
 			}
 
 			// Div for the main numbers section
 			boolean bAllDone=true;
 			if(bIncludeNav)
 			{
 				if(bPlain) XML.createText(eProgress,"h2","Progress so far");
 				Element eNumbers=XML.createChild(eProgress,bPlain ? "ul" : "div");
 				if(!bPlain) eNumbers.setAttribute("class","numbers");
 
 				boolean bAllowNavigation=us.bAdmin || us.getTestDefinition().isNavigationAllowed();
 
 				boolean bFirstInSection=true;
 				for(int i=0;i<us.getTestLeavesInOrder().length;i++)
 				{
 					TestLeaf tl=us.getTestLeavesInOrder()[i];
 
 					// Check if new section
 					if( (tl.getSection()!=null && !tl.getSection().equals(sCurrentSection))
 						|| (tl.getSection()==null && sCurrentSection!=null) )
 					{
 						sCurrentSection=tl.getSection();
 						if(sCurrentSection==null)
 							eCurrentSection=null;
 						else
 						{
 							if(bPlain)
 							{
 								Element eLI=XML.createChild(eNumbers,"li");
 								XML.createText(eLI,"h3",sCurrentSection);
 								eCurrentSection=XML.createChild(eLI,"ul");
 							}
 							else
 							{
 								eCurrentSection=XML.createChild(eNumbers,"div");
 								eCurrentSection.setAttribute("class","section");
 								Element eTag=XML.createChild(eCurrentSection,"div");
 								eTag.setAttribute("class","sectiontag");
 								XML.createText(eTag,sCurrentSection);
 								Element eTagAfter=XML.createChild(eCurrentSection,"div");
 								eTagAfter.setAttribute("class","sectiontagafter");
 							}
 						}
 						bFirstInSection=true;
 					}
 
 					if(bFirstInSection)
 					{
 						bFirstInSection=false;
 					}
 					else
 					{
 						// Needed to allow IE to wrap line
 						if(!bPlain)
 							XML.createText(eCurrentSection!=null ? eCurrentSection : eNumbers," \u00a0 ");
 					}
 
 					boolean
 						bText=(tl instanceof TestInfo),
 						bCurrent=(i==us.getTestPosition()),
 						bDone=tl.isDone(),
 						bAvailable=tl.isAvailable();
 					if(!bDone)bAllDone=false;
 					boolean bLink=(!bCurrent || us.getTestDefinition().isRedoQuestionAllowed())
 						&& bAllowNavigation && bAvailable;
 
 					// Make child directly in progress or in current section if there is one
 					Element eThis=XML.createChild(
 						eCurrentSection!=null ? eCurrentSection : eNumbers,
 						bPlain ? "li" : "div");
 
 					if(bPlain)
 					{
 						Element eThingy=bLink ? XML.createChild(eThis,"a") : eThis;
 						XML.createText(eThingy,
 							bText
 							? (((TestInfo)tl).getTitle())
 							:	((TestQuestion)tl).getNumber()+"");
 						if(bCurrent)
 						{
 							XML.createText(eThingy," (current)");
 						}
 						else
 						{
 							if(!bText) XML.createText(eThingy,bDone ? " (done)"
 								: (" (not done" + (bAvailable ? ")" : "; not yet available)")) );
 						}
 						if(bLink) eThingy.setAttribute("href","?jump="+i);
 					}
 					else
 					{
 						eThis.setAttribute("class",
 							(bText ? "text" : "question")+
 							(bCurrent ? " current" : "")+
 							(bDone ? " done" : "")+
 							(!bAvailable ? " unavailable" : ""));
 
 						Element eThingy=XML.createChild(eThis,bLink?"a":"span");
 						eThingy.setAttribute("class","t");
 						XML.createText(eThingy,bText?"Info":((TestQuestion)tl).getNumber()+"");
 
 						if(bLink) eThingy.setAttribute("href","?jump="+i);
 					}
 				}
 
 				// Do the summary
 				boolean bStopButton=(us.getTestDefinition().isStopAllowed() || bAllDone);
 				if(us.getTestDefinition().isSummaryAllowed())
 				{
 					Element eSummary=XML.createChild(eButtons,"div");
 					if(!bPlain) eSummary.setAttribute("class","button");
 					Element eThingy=XML.createChild(eSummary,"a");
 					XML.createText(eThingy,bPlain ? "Review your answers" : "Your answers");
 					eThingy.setAttribute("href","?summary");
 					if(!bPlain && bStopButton)
 						XML.createChild(eButtons,"div").setAttribute("class","buttonspacer");
 				}
 
 				if(bStopButton)
 				{
 					Element eStop=XML.createChild(eButtons,"div");
 					if(!bPlain) eStop.setAttribute("class","button");
 					Element eThingy=XML.createChild(eStop,"a");
 					XML.createText(eThingy,"End test");
 					eThingy.setAttribute("href","?end");
 				}
 			}
 		}
 		else
 		{
 			addAccessibilityClasses(d,request,"progressbottom singles");
 		}
 
 		// Fix up the replacement variables
 		mReplace=new HashMap<String,Object>(getLabelReplaceMap(us));
 		mReplace.put("RESOURCES","resources/"+us.getTestPosition());
 		mReplace.put("IDPREFIX","");
 
 		XML.replaceTokens(eQuestion,mReplace);
 
 		// Whew! Now send to user
 		breakBack(response);
 		XHTML.output(d,request,response,"en");
 	}
 
 	/** Cache label replacement (Map of String (labelset id) -> Map ) */
 	private Map<String,Map<String,String> > labelReplace=new HashMap<String,Map<String,String> >();
 
 	/**
 	 * Returns the map of label replacements appropriate for the current session.
 	 * @param us Session
 	 * @return Map of replacements (don't change this)
 	 * @throws IOException Any problems loading it
 	 */
 	private Map<String,String> getLabelReplaceMap(UserSession us) throws IOException
 	{
 		// Check labelset ID
 		String sKey;
 		if(us.getTestDefinition()==null || us.getTestDefinition().getLabelSet()==null ||
 				us.getTestDefinition().getLabelSet().equals(""))
 			sKey="!default";
 		else
 			sKey=us.getTestDefinition().getLabelSet();
 
 		// Get from cache
 		Map<String,String> mLabels=labelReplace.get(sKey);
 		if(mLabels!=null) return mLabels;
 
 		// Load from file
 		Map<String,String> m=new HashMap<String,String>();
 		File f=new File(getServletContext().getRealPath("WEB-INF/labels/"+sKey+".xml"));
 		if(!f.exists())
 			throw new IOException("Unable to find requested label set: "+sKey);
 		Document d=XML.parse(f);
 		Element[] aeLabels=XML.getChildren(d.getDocumentElement());
 		for(int i=0;i<aeLabels.length;i++)
 		{
 			m.put(
 					XML.getRequiredAttribute(aeLabels[i],"id"),
 					XML.getText(aeLabels[i]));
 		}
 
 		// Cache and return
 		labelReplace.put(sKey,m);
 		return m;
 	}
 
 	/**
 	 * Sends an error to the student, logs it, and kills their session. Does not
 	 * return but throws a StopException in order to abort further processing.
 	 * @param us Session to be killed (null if we should keep it)
 	 * @param request HTTP request
 	 * @param response HTTP response
 	 * @param code Desired HTTP status code; HttpServletResponse.SC_INTERNAL_SERVER_ERROR
 	 *	 is treated differently in that these are logged as errors, not warnings
 	 * @param isBug Set true if this is a bug and should be logged as such,
 	 *	 complete with the option for users to re-enter test. Generally I have
 	 *	 erred on the side of setting this true...
 	 * @param keepSession normally, this should be false, and OM will throw away the user's
 	 *   session in case it was corrupted in a way that caused the error. Then on the next request
 	 *   it will silently give them a new session. But very occasionally we don't want this behaviour
 	 *   in which case this parameter can be set to true.
 	 * @param backToTest If set to test ID (not null), this forces the back-to-test
 	 *	 option to appear.
 	 * @param title Title of message
 	 * @param message Actual message
 	 * @param exception Any exception that should be reported (null if none)
 	 * @throws StopException Always, to abort processing
 	 */
 	public void sendError(UserSession us, HttpServletRequest request, HttpServletResponse response,
 			int code, boolean isBug, boolean keepSession,
 			String backToTest, String title, String message, Throwable exception)
 			throws StopException
 	{
 		// Get rid of user session
 		String sTestID=backToTest;
 		boolean bClearedPosition=false;
 		if(us!=null && title!=ACCESSOUTOFSEQUENCE)
 		{
 			// If they just came in and it crashed, and they're on a question page,
 			// let's forget the position too. Conditions:
 			// * Error is caused by a bug, not permission failure etc
 			// * The test allows navigation (otherwise we shouldn't change their position)
 			if(isBug && us.getDbTi()>0 && us.getTestDefinition()!=null && us.getTestDefinition().isNavigationAllowed() &&
 				((System.currentTimeMillis() - us.lSessionStart) < 5000))
 			{
 				DatabaseAccess.Transaction dat=null;
 				try
 				{
 					dat=da.newTransaction();
 					oq.updateSetTestPosition(dat,us.getDbTi(), 0);
 					bClearedPosition=true;
 				}
 				catch(SQLException se)
 				{
 					// Damn, something maybe wrong with database?
 				}
 				finally
 				{
 					if(dat!=null) dat.finish();
 				}
 			}
 
 			if (!keepSession) {
 				l.logDebug("Throwing away session.");
 				synchronized(sessions)
 				{
 					sessions.values().remove(us);
 				}
 			}
 			if(sTestID==null) sTestID=us.getTestId();
 		}
 
 		// Detect particular errors that we want to make more friendly
 		if(exception!=null && exception.getMessage()!=null)
 		{
 			String TEMPPROBLEM="(This error indicates a temporary problem with our " +
 						"systems; wait a minute then try again.)";
 
 			if(exception instanceof SQLException)
 			{
 				if(exception.getMessage().indexOf("Connection reset")!=-1)
 				{
 					title="Database connection lost";
 					message="A required database connection has been lost. (Normally " +
 							"this error goes away after a reload. If it persists, it may " +
 							"indicate a problem with our systems; try " +
 							"again later.)";
 					exception=null;
 				}
 				else if(exception.getMessage().indexOf("Error establishing socket")!=-1)
 				{
 					title="Database connection fault";
 					message="Could not connect to a required database. " +
 						TEMPPROBLEM;
 					exception=null;
 
 				}
 			}
 			else if(exception instanceof AxisFault)
 			{
 				if(exception.getMessage().indexOf("java.net.SocketTimeoutException")!=-1
 						|| exception.getMessage().indexOf("This application is not currently available")!=-1
 						|| exception.getMessage().indexOf("java.net.ConnectException")!=-1)
 				{
 					title="Question engine connection fault";
 					message="The system could not connect to a required component. " +
 							TEMPPROBLEM;
					// exception=null; Leave exception to display - we are getting inexplicable errors.
 				}
 				else
 				{
 					title="Question engine fault";
 					message="An error occurred in the question or a required system " +
 							"component.";
 					// Leave exception to display; this could be a question developer
 					// error.
 				}
 			}
 		}
 
 		boolean bRemovedBackto=false;
 		Map<String,String> m=new HashMap<String,String>();
 		try
 		{
 			response.setStatus(code);
 
 			Document d=XML.parse(new File(getServletContext().getRealPath("WEB-INF/templates/errortemplate.xhtml")));
 			m.put("TITLE",title);
 			m.put("MESSAGE",message);
 			m.put("STATUSCODE",code+"");
 			String sOUCU=auth.getUncheckedUserDetails(request).getUsername();
 			m.put("OUCU",sOUCU==null ? "[not logged in]" : sOUCU);
 			m.put("REQUEST",request.getPathInfo()+
 				(request.getQueryString()==null ? "" : "?"+request.getQueryString()));
 			m.put("TIME",Log.DATETIMEFORMAT.format(new Date()));
 			m.put("ACCESSCSS",getAccessCSSAppend(request));
 
 			if(title!=ACCESSOUTOFSEQUENCE)
 			{
 				XML.remove(XML.find(d,"id","accessoutofsequence"));
 			}
 			else
 			{
 				sTestID=us.getTestId();
 			}
 
 			if(us!=null && us.oss!=null)
 			{
 				m.put("QENGINE",displayServletURL(us.oss.getEngineURL()));
 			}
 			else
 			{
 				m.put("QENGINE","n/a");
 				XML.remove(XML.find(d,"id","qengine"));
 			}
 
 			if(us!=null)
 			{
 				m.put("TINDEX",""+us.getTestPosition());
 				m.put("TSEQ",""+us.iDBseq);
 			}
 			else
 			{
 				XML.remove(XML.find(d,"id","indseq"));
 			}
 
 			m.put("TNAVIGATOR",displayServletURL(nc.getThisTN()));
 
 			String sAccess=getAccessibilityCookie(request);
 			if(sAccess.equals(""))
 			{
 				XML.remove(XML.find(d,"id","access"));
 				m.put("ACCESS","n/a");
 			}
 			else
 			{
 				m.put("ACCESS",sAccess);
 			}
 
 			if(exception==null)
 			{
 				XML.remove(XML.find(d,"id","exception"));
 			}
 			else
 			{
 				m.put("EXCEPTION",Log.getOmExceptionString(exception));
 			}
 
 			m.put("TESTURL",getServletURL(request)+sTestID+"/"+endOfURL(request));
 
 			if(backToTest==null && (sTestID==null || title==ACCESSOUTOFSEQUENCE))
 			{
 				XML.remove(XML.find(d,"id","backtotest"));
 				bRemovedBackto=true;
 			}
 			else
 			{
 				if(bClearedPosition)
 				{
 					XML.remove(XML.find(d,"id","keptposition"));
 				}
 				else
 				{
 					XML.remove(XML.find(d,"id","clearedposition"));
 				}
 
 				if(code==HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
 				{
 					String[] asOther=nc.getOtherNavigators();
 					if(asOther.length==0)
 						XML.remove(XML.find(d,"id","otherservers"));
 					else
 					{
 						Element eUL=XML.find(d,"id","otherserverlist");
 						for(int i=0;i<asOther.length;i++)
 						{
 							Element eLI=XML.createChild(eUL,"li");
 							XML.createText(eLI,"Alternate server: ");
 							Element eA=XML.createChild(eLI,"a");
 							XML.createText(eA,
 								asOther[i].replaceAll("\\.open\\.ac\\.uk.*$","").
 								replaceAll("http://",""));
 							eA.setAttribute("href",asOther[i]+sTestID+"/"+endOfURL(request));
 						}
 					}
 				}
 				else
 				{
 					XML.remove(XML.find(d,"id","otherservers"));
 				}
 			}
 			if(isBug)
 			{
 				XML.remove(XML.find(d,"id","notbug"));
 			}
 			else
 			{
 				if(!bRemovedBackto && backToTest==null) XML.remove(XML.find(d,"id","backtotest"));
 			}
 			XML.replaceTokens(d,m);
 			XHTML.output(d,request,response,"en");
 		}
 		catch(XMLException ioe)
 		{
 			l.logError("Errors","Error processing error template",ioe);
 		}
 		catch(IOException ioe)
 		{
 			// Ignore exception, they must have closed browser or something
 		}
 
 		// Get requested path too, fo use in logged/displayed error
 		String sPath=request.getPathInfo();
 		if(sPath==null) sPath="";
 		if(request.getQueryString()!=null) sPath+="?"+request.getQueryString();
 		m.put("REQUEST",sPath);
 
 		String sMessageSummary="??";
 		try
 		{
 			sMessageSummary=IO.loadString(new FileInputStream(getServletContext().getRealPath("WEB-INF/templates/errortemplate.txt")));
 		}
 		catch(IOException ioe)
 		{
 			// Ignore
 		}
 		sMessageSummary=XML.replaceTokens(sMessageSummary,"%%",m);
 
 		if(isBug)
 		{
 			l.logError("Displayed error",sMessageSummary,exception);
 
 			// Optionally send email - output is already sent to user so no need
 			// to worry if it takes a while
 			sendAdminAlert(sMessageSummary,exception);
 		}
 		else
 			l.logWarning("Displayed error",sMessageSummary,exception);
 
 		throw new StopException();
 	}
 
 	/**
 	 * Sends an admin alert message by email. Messages are automatically queued
 	 * so it doesn't send more than one per ADMINALERTDELAY. Time is automatically
 	 * recorded.
 	 * @param sMessage Message text; will be included in one line after the date.
 	 *	 Can include a multi-line message with \n if necessary
 	 * @param tException Exception to trace (optional, null if none)
 	 */
 	private void sendAdminAlert(String sMessage,Throwable tException)
 	{
 		// Don't send any mails if nobody is receiving them!
 		if(nc.getAlertMailTo().length==0) return;
 
 		synchronized(adminAlertSynch)
 		{
 			boolean bNew=false;
 			if(adminAlerts==null)
 			{
 				adminAlerts=new StringBuffer();
 				bNew=true;
 			}
 
 			adminAlerts.append(
 				"----------------------------------------------------------------------\n"+
 				Log.DATETIMEFORMAT.format(new Date())+"\n" +	sMessage+"\n\n"+
 				(tException==null ? "" : (Log.getOmExceptionString(tException)+"\n\n")));
 
 			if(bNew &&
 				(System.currentTimeMillis() - lastAdminAlert) > ADMINALERTDELAY)
 			{
 				sendAdminAlertNow();
 			}
 			else if(bNew)
 			{
 				new AdminAlertLater();
 			}
 		}
 	}
 
 	/** Sends the actual email */
 	private void sendAdminAlertNow()
 	{
 		try
 		{
 			synchronized(adminAlertSynch)
 			{
 				lastAdminAlert=System.currentTimeMillis();
 				adminAlerts.append(
 					"======================================================================\n"+
 					"Next email will not be sent until "+
 					Log.TIMEFORMAT.format(new Date(lastAdminAlert+ADMINALERTDELAY))+"\n");
 
 				// Send mail now
 				Mail.send(nc.getAlertMailFrom(),null,nc.getAlertMailTo(),
 					nc.getAlertMailCC(),"Om alert: "+getFriendlyServerName(),adminAlerts.toString(), Mail.TEXTPLAIN);
 				adminAlerts=null;
 			}
 		}
 		catch(MessagingException me)
 		{
 			l.logError("Admin alerts","Failed to send message",me);
 		}
 	}
 
 	/** @return A 'friendly' version of the server's own name, e.g. pclt166/om-tn */
 	private String getFriendlyServerName()
 	{
 		return	nc.getThisTN().getHost().replaceAll(".open.ac.uk","")+
 			nc.getThisTN().getPath();
 	}
 
 	/** Synch object for admin alerts */
 	private Object adminAlertSynch=new Object();
 
 	/** Buffer holding text of next admin alert to send, null if thread not running */
 	private StringBuffer adminAlerts=null;
 
 	/** Time last alert was sent at */
 	private long lastAdminAlert=0;
 
 	/** Delay per which admin alert emails are sent (4 hours) */
 	private static final int ADMINALERTDELAY=4*60*60*1000;
 
 	/** Thread that hangs around for a bit then sends alert message */
 	class AdminAlertLater extends Thread
 	{
 		AdminAlertLater()	{	start();}
 
 		@Override
 		public void run()
 		{
 			try
 			{
 				Thread.sleep(ADMINALERTDELAY);
 			}
 			catch(InterruptedException e)
 			{
 			}
 
 			sendAdminAlertNow();
 		}
 	}
 
 	private static Pattern SERVLETPATH=Pattern.compile("/([^/]*).*");
 
 	/**
 	 * Displays servlet URLs in a form suitable for including in error messages
 	 * etc. (The idea is not to show the full URL because then maybe users will
 	 * click it.)
 	 * @param u Actual URL
 	 * @return String displaying the servlet context @ hostname
 	 */
 	private String displayServletURL(URL u)
 	{
 		Matcher m=SERVLETPATH.matcher(u.getPath());
 		m.matches();
 		return m.group(1) +" @ "+u.getHost().replaceAll("\\.open\\.ac\\.uk","");
 	}
 
 	/**
 	 * @return the reports
 	 */
 	public ReportDispatcher getReports() {
 		return reports;
 	}
 
 	/**
 	 * @param testId a test id.
 	 * @return the path to its deploy file.
 	 */
 	public File pathForTestDeployment(String testId)
 	{
 		return resolveRelativePath("testbank/" + testId + ".deploy.xml");
 	}
 
 	/**
 	 * @param relativePath a path relative to servlet folder.
 	 * @return the corresponding canonical path.
 	 */
 	public File resolveRelativePath(String relativePath) {
 		return new File(getServletContext().getRealPath(relativePath));
 	}
 }
