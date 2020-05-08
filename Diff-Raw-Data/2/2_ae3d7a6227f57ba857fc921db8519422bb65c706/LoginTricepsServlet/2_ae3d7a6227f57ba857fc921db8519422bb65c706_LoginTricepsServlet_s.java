 /* ******************************************************** 
 ** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
 ** $Header$
 ******************************************************** */ 
 
 package org.dianexus.triceps;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpUtils;
 
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Random;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.io.PrintWriter;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 
 public class LoginTricepsServlet extends TricepsServlet {
 	/* Strings for storing / retrieving state of authentication */
 	static final String LOGIN_TOKEN = "_DlxLTok";
 	static final String LOGIN_COMMAND = "_DlxLCom";
 	static final String LOGIN_COMMAND_LOGON = "logon";
 	static final String LOGIN_IP = "_DlxLIP";
 	static final String LOGIN_USERNAME = "_DlxUname";
 	static final String LOGIN_PASSWORD = "_DlxPass";
 	static final String LOGIN_RECORD = "_DlxLRec";
 	
 	/* Strings serving as messages for login error pages - these should really be JSP */
 	static final String LOGIN_ERR_NO_TOKEN = "Please login";
 	static final String LOGIN_ERR_NEW_SESSION = "Please login";
 	static final String LOGIN_ERR_MISSING_UNAME_OR_PASS = "Please enter both your username and password";
 	static final String LOGIN_ERR_INVALID_UNAME_OR_PASS = "The username or password you entered was incorrect";	/* don't mix so many messages? */
	static final String LOGIN_ERR_INVALID = "Please login again --  You will resume from where you left off.<br><br>(Your login session was invalided either because you accidentaly pressed the browser's back button (instead of the 'previous' button, or you attempted to use a bookmarked page from the instrument)";
 	static final String LOGIN_ERR_ALREADY_COMPLETED = "Thank you!  You have already completed this instrument.";
 	static final String LOGIN_ERR_UNABLE_TO_LOAD_FILE = "Please contact the administrator -- the program was unable to load the interview: ";
 	
 	LoginRecords loginRecords = LoginRecords.NULL;
 	static Random random = new Random();
 		
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		
 		/* Load login info file from init param */
 		if (!loadLoginInfoFile(config)) {
 			Logger.writeln("Unable to initialize LoginTricepsServlet");
 		}
 	}
 
 	public void destroy() {
 		super.destroy();
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) {
 		doPost(req,res);
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
 		try {
 			processPost(req,res);
 		}
 		catch (Throwable t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}	
 	}
 	
 	void processPost(HttpServletRequest req, HttpServletResponse res)  {
 		/* validate session */
 		/* Ensure that there is actually a session object  -- if not, create one */
 		HttpSession session = req.getSession(true);
 		
 		if (session.isNew()) {
 			/* If session is expired, give appropriiate error page */
 			if ("POST".equals(req.getMethod())) {
 				expiredSessionErrorPage(req,res);
 				shutdown(req,"post-expired session");
 				return;
 			}
 			/* if new, require login */
 			loginPage(req,res,LOGIN_ERR_NEW_SESSION);
 			return;
 		}
 		
 		String loginCommand = req.getParameter(LOGIN_COMMAND);
 		String loginToken = req.getParameter(LOGIN_TOKEN);	// need to way to tell whether this is a authenticated session
 		String loginIP = req.getRemoteAddr();
 		String storedLoginToken = (String) session.getAttribute(LOGIN_TOKEN);
 		String storedIP = (String) session.getAttribute(LOGIN_IP);
 		LoginRecord loginRecord = (LoginRecord) session.getAttribute(LOGIN_RECORD);
 		
 		if (LOGIN_COMMAND_LOGON.equals(loginCommand)) {
 			/* then try to validate this person */
 			String uname = req.getParameter(LOGIN_USERNAME);
 			String pass = req.getParameter(LOGIN_PASSWORD);
 			if (uname == null || uname.trim().equals("") ||
 				pass == null || pass.trim().equals("")) {
 				loginPage(req,res,LOGIN_ERR_MISSING_UNAME_OR_PASS);
 				return;
 			}
 			
 			loginRecord = loginRecords.validateLogin(uname,pass);
 			if (loginRecord == LoginRecord.NULL) {
 				/* then invalid */
 				loginPage(req,res,LOGIN_ERR_INVALID_UNAME_OR_PASS);
 				return;
 			}
 			
 			/* create tokens needed for remainder of processing */
 			storedIP = loginIP;
 			session.setAttribute(LOGIN_IP,storedIP);
 			session.setAttribute(LOGIN_RECORD,loginRecord);	// stores the username/password info for later access
 			storedLoginToken = createLoginToken();
 			loginToken = storedLoginToken;
 			
 			/* need to either create new instrument, or know to load appropriate existing one */
 			/* might like to extend this like Perl "Prefill" programs so that sets some default parameters when creates instance */
 		}
 		
 		/* validate that the session is authenticated */
 					
 		if (loginToken == null || loginToken.trim().equals("") || 
 			storedLoginToken == null || storedLoginToken.trim().equals("") ||
 			loginRecord == null) {
 			/* Then user has not logged in */
 			loginPage(req,res,LOGIN_ERR_NO_TOKEN);
 			return;
 		}
 		
 		/* compare login token to the one stored in the session */
 		if (storedLoginToken.equals(loginToken) && storedIP.equals(loginIP)) {
 			/* create and store new login token -- so can't re-submit */
 			loginToken = createLoginToken();
 			session.setAttribute(LOGIN_TOKEN,loginToken);
 			
 			/* ensure that this hidden parameter is sent with each new form -- this is a hack */
 			String hiddenLoginToken = "<input type='hidden' name='" + LOGIN_TOKEN + "' value='" + loginToken + "'>";
 
 			/* pass control through to main Dialogix servlet */
 			processAuthenticatedRequest(req,res,hiddenLoginToken);
 		}
 		else {
 			/* What if try to re-submit -- what will happen? */
 			loginPage(req,res,LOGIN_ERR_INVALID);
 			return;
 		}
 	}
 	
 	void processAuthenticatedRequest(HttpServletRequest req, HttpServletResponse res, String hiddenLoginToken)  {
 		/* Now, must load the proper instrument from the "database" */
 		/* Might also want to warn about the unsupported browser feature earlier? */
 		if (isSupportedBrowser(req)) {
 			okPage(req,res,hiddenLoginToken);
 		}
 		else {
 			errorPage(req,res);
 		}
 	}
 	
 	
 	private static String createLoginToken() {
 		return System.currentTimeMillis() + "." + Long.toString(random.nextLong());
 	}
 	
 	private boolean loadLoginInfoFile(ServletConfig config) {
 		/* N.B. This approach assumes that a single username / password is never used more than once -- one to one mapping between uname/pass and instrument instance */
 		String s = config.getInitParameter("loginInfoFile");
 		loginRecords = new LoginRecords(s);
 //		loginRecords.showValues();
 		return loginRecords.isLoaded();
 	}
 	
 	void loginPage(HttpServletRequest req, HttpServletResponse res, String message) {
 		logAccess(req, " LOGIN - " + message);
 		
 		if (tricepsEngine != null) {
 			/* then this is an error condition -- must reset state and reload instrument from where left off */
 			shutdown(req,"at start of loginPage, when tricepsEngine = null");	// graceful exit;
 		}
 		
 		try {
 			res.setContentType("text/html");
 			PrintWriter out = res.getWriter();
 			
 			out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN'>");
 			out.println("<html>");
 			out.println("<head>");
 			out.println("<META HTTP-EQUIV='Content-Type' CONTENT='text/html;CHARSET=iso-8859-1'>");
 			out.println("<title>Login</title>");
 			out.println("</head>");
 			out.println("<body bgcolor='white'>");
 			
 			out.print("<FORM method='POST' name='myForm' action='");
 			out.print(res.encodeURL(HttpUtils.getRequestURL(req).toString()));
 			out.print("'>");
 			
 			out.println(createStudyTitleBar());	// study header
 			
 			out.println("<P><font size='+1'><b>");
 			out.println(message);
 			out.println("</b></font></P>");
 			
 			out.println(createLoginForm());
 			
 			out.println(createHelpInfo());
 			
 			out.println("</form>");
 			out.println("</body>");
 			out.println("</html>");
 	
 			out.flush();
 			out.close();
 		}
 		catch (Throwable t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}		
 	}
 	
 	private String createStudyTitleBar() {
 		StringBuffer sb = new StringBuffer();
 
 		/* create common header row, indicating which study is being conducted */
 		sb.append("<table border='0' cellpadding='0' cellspacing='3' width='100%'>");
 		sb.append("<tr>");
 		sb.append("<td width='1%'>");
 		sb.append("<img name='icon' src='" + STUDY_ICON + "' align='top' border='0' alt='" + STUDY_NAME + "'>");
 		sb.append("</td><td>");
 		sb.append("<font size='4'>");
 		sb.append(STUDY_NAME);
 		sb.append("</font></td></tr></table>");
 		
 		return sb.toString();		
 	}
 	
 	private String createLoginForm() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append("<table border='1' cellpadding='0' cellspacing='0' width='100%'>");
 		sb.append("<tr><td width='50%'>Username</td><td width='50%'><input type='text' name='" + LOGIN_USERNAME + "' size='25'></td></tr>");
 		sb.append("<tr><td width='50%'>Password</td><td width='50%'><input type='password' name='" + LOGIN_PASSWORD + "' size='25'></td></tr>");
 		sb.append("<tr><td colspan='2' align='center'><input type='submit' name='Submit' value='Submit'></td></tr>");
 		sb.append("</table>");
 		sb.append("<input type='hidden' name='" + LOGIN_COMMAND + "' value='" + LOGIN_COMMAND_LOGON + "'>");
 		
 		return sb.toString();
 	}
 	
 	private String createHelpInfo() {
 		StringBuffer sb = new StringBuffer();
 		boolean hasInfo = false;
 		
 		sb.append("<hr>");
 		sb.append("For assistance, please contact:<br>");
 		if (SUPPORT_PERSON != null && !SUPPORT_PERSON.trim().equals("")) {
 			sb.append(SUPPORT_PERSON);
 			sb.append("<br>");
 			hasInfo = true;
 		}
 		if (SUPPORT_EMAIL != null && !SUPPORT_EMAIL.trim().equals("")) {
 			sb.append("<a href='mailto:");
 			sb.append(SUPPORT_EMAIL);
 			sb.append("'>");
 			sb.append(SUPPORT_EMAIL);
 			sb.append("</a><br>");
 			hasInfo = true;
 		}
 		if (SUPPORT_PHONE != null && !SUPPORT_PHONE.trim().equals("")) {
 			sb.append(SUPPORT_PHONE);
 			hasInfo = true;
 		}
 		if (hasInfo) {
 			return sb.toString();
 		}
 		else {
 			return "";
 		}
 	}
 	
 	void okPage(HttpServletRequest req, HttpServletResponse res, String hiddenLoginToken) {
 		HttpSession session = req.getSession();	// must alreay exist by this stage
 		
 		sessionID = session.getId();
 		String restoreFile = null;	// means that the file has already been loaded.  If non-null, asks the system to load it.
 		
 		tricepsEngine = (TricepsEngine) session.getAttribute(TRICEPS_ENGINE);
 		
 		
 		/* Now ensure that using proper instrument for this authenticated subject */
 		LoginRecord loginRecord = (LoginRecord) session.getAttribute(LOGIN_RECORD);	// can't be null here		
 		
 		/* Can I assume that once the tricepsEngine is active, that the proper instrument will have been loaded? */
 		if (tricepsEngine == null) {
 			/* different behavior based upon the status of this instance of the interview */
 			if (loginRecord.isCompleted()) {
 				/* if completed, give the option to sign on as someone else */
 				loginPage(req,res,LOGIN_ERR_ALREADY_COMPLETED);
 				return;
 			}
 			
 			tricepsEngine = new TricepsEngine(config);
 			
 			/* create new instance, or validate that old instance exists; then "resume" it via normal TricepsEngine mechanisms */
 			String src = null;
 			
 			if (loginRecord.isWorking()) {
 				/* then load the existing one, from where left off */
 				src = loginRecord.getFilename();
 			}
 			else {
 				/* hasn't been started yet -- load it from the instrument, and keep track of the location information (the tmp file) */
 				src = loginRecord.getInstrument();
 			}
 			
 			boolean ok = tricepsEngine.getNewTricepsInstance(tricepsEngine.getCanonicalPath(src));
 			if (ok) {
 				String filename = tricepsEngine.getTriceps().dataLogger.getFilename();
 				
 				if (!loginRecord.isWorking()) {
 					/* This is a new file, so add additional config information to data file */
 					tricepsEngine.setExtraParameters(loginRecord.getStartingStep(),loginRecord.getMappings());
 				}
 				
 				loginRecord.setFilename(filename);
 				loginRecord.setStatusWorking();
 				loginRecords.updateLoginInfo();
 				
 				/* override whatever command was passed via req -- should be RESTORE */
 				restoreFile = filename;
 			}
 			else {
 				loginPage(req,res,LOGIN_ERR_UNABLE_TO_LOAD_FILE + " '" + src + "'");
 				return;
 			}
 		}
 		
 		logAccess(req, " OK");
 		
 		try {
 			res.setContentType("text/html");
 			PrintWriter out = res.getWriter();
 			
 			tricepsEngine.doPost(req,res,out,hiddenLoginToken,restoreFile);
 			
 			out.close();
 			out.flush();
 		}
 		catch (Throwable t) {
 if (DEBUG) Logger.printStackTrace(t);
 		}
 		
 		session.setAttribute(TRICEPS_ENGINE, tricepsEngine);
 		
 		/* disable session if completed */
 		if (tricepsEngine.isFinished()) {
 			logAccess(req, " FINISHED");
 			Logger.writeln("...instrument finished.  Discarding session " + sessionID);
 			try {
 				shutdown(req,"post-FINISHED");
 				loginRecord.setStatusCompleted();
 				loginRecords.updateLoginInfo();
 			}
 			catch (java.lang.IllegalStateException e) {
 				Logger.writeln(e.getMessage());
 			}
 		}
 	}	
 }
 
 class LoginRecords implements VersionIF  {
 	static String LOCK_FILE = "LOCK_FILE";
 	private boolean isLoaded = false;
 	private Hashtable loginRecords = new Hashtable();
 	static LoginRecords NULL = new LoginRecords(null);
 	static String sourceFile = null;	// so can modify this as needed
 	
 	LoginRecords(String filename) {
 		this.sourceFile = filename;
 		if (filename != null) {
 			isLoaded = loadLoginInfo(filename);
 		}
 	}
 
 	private boolean loadLoginInfo(String filename) {
 		BufferedReader br = null;
 		boolean loaded = true;
 		synchronized(LOCK_FILE) {
 			try {
 				br = new BufferedReader(new FileReader(filename));
 				String fileLine = null;
 				while ((fileLine = br.readLine()) != null) {
 					if ("".equals(fileLine.trim())) {
 						continue;
 					}
 					if (fileLine.startsWith("#")) {
 						continue;
 					}
 					loaded = loaded && addLoginRecord(fileLine);
 				}
 			}
 			catch (Throwable e) {
 	if (DEBUG)	Logger.writeln("##Throwable @ loadLoginInfoFile() " + e.getMessage());
 				loaded = false;	// to incidate that an error occurred
 			}
 			if (br != null) {
 				try { br.close(); } catch (IOException t) { }
 			}
 		}
 		if (!loaded) {		
 			Logger.writeln("Unable to load loginInfoFile \"" + filename + "\"");
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean addLoginRecord(String line) {
 		int field = 0;
 		StringTokenizer st = new StringTokenizer(line,"\t",true);
 		String varname=null, value=null;
 		
 		LoginRecord lr = new LoginRecord();
 
 		while(st.hasMoreTokens()) {
 			String s = null;
 			s = st.nextToken();
 
 			if (s.equals("\t")) {
 				++field;
 				continue;
 			}
 			switch(field) {
 				case 0:
 					lr.setUsername(s);
 					break;
 				case 1:
 					lr.setPassword(s);
 					break;
 				case 2:
 					lr.setFilename(s);
 					break;
 				case 3:
 					lr.setInstrument(s);
 					break;
 				case 4:
 					lr.setStatus(s);
 					break;
 				case 5:
 					lr.setStartingStep(s);
 					break;
 				default:
 					/* can add arbitrary number of additional parmeters, in pairs of tab delimited (VARIABLE_NAME, VALUE)
 					I have not done robust checking to ensure that the variable names are valid, since this is a hack.
 					When a new instrument is started, these pairs of values will be added to the end of the data file, before any subject interaction */
 					if (((field - 6) % 2) == 0) {
 						varname = s;
 					}
 					else {
 						value = s;
 						lr.addMapping(varname,value);
 					}
 					break;
 			}
 		}
 		if (lr.isValid()) {
 			loginRecords.put(lr.getUsername(),lr);
 			return true;
 		}
 		return false;
 	}
 
 	LoginRecord validateLogin(String username, String password) {
 		if (!isLoaded || username == null || username.trim().equals("") || password == null || password.trim().equals("")) {
 			return LoginRecord.NULL;
 		}
 		LoginRecord lr = (LoginRecord) loginRecords.get(username);
 		if (lr == null) {
 			return LoginRecord.NULL;
 		}
 		if (password.equals(lr.getPassword())) {
 			return lr;
 		}
 		else {
 			return LoginRecord.NULL;
 		}
 	}
 	
 	boolean isLoaded() {
 		return isLoaded;
 	}
 	
 	void showValues() {
 		Enumeration enum = loginRecords.elements();
 		while (enum.hasMoreElements()) {
 			LoginRecord lr = (LoginRecord) enum.nextElement();
 			Logger.writeln(lr.showValue());
 		}
 	}
 	
 	boolean updateLoginInfo() {
 		synchronized(LOCK_FILE) {
 			PrintWriter pw = null;
 			try {
 				pw = new PrintWriter(new BufferedWriter(new FileWriter(sourceFile)));
 				
 				Enumeration enum = loginRecords.elements();
 				while (enum.hasMoreElements()) {
 					LoginRecord lr = (LoginRecord) enum.nextElement();
 					pw.println(lr.showValue());
 				}			
 			}
 			catch (Throwable e) {
 	if (DEBUG)	Logger.writeln("##Throwable @ updateLoginInfo() " + e.getMessage());
 				isLoaded = false;	// since a fatal error?
 			}
 			if (pw != null) {
 				pw.close();
 			}
 		}
 		if (!isLoaded) {		
 			Logger.writeln("Error updating loginInfoFile \"" + sourceFile + "\"");
 			return false;
 		}
 		return true;
 	}
 }
 
 
 class LoginRecord {
 	static final String STATUS_UNSTARTED = "unstarted";
 	static final String STATUS_WORKING = "working";
 	static final String STATUS_COMPLETED = "completed";
 	
 	static LoginRecord NULL = new LoginRecord();
 	
 	String username = null;
 	String password = null;
 	String filename = null;
 	String instrument = null;
 	String status = null;
 	String startingStep = null;
 	Hashtable mappings  = new Hashtable();
 	
 	LoginRecord() {
 	}
 	
 	void setUsername(String username) {
 		this.username = username;
 	}
 	
 	String getUsername() {
 		return this.username;
 	}
 	
 	void setPassword(String password) {
 		this.password = password;
 	}
 	
 	String getPassword() {
 		return this.password;
 	}
 	
 	void setFilename(String filename) {
 		this.filename = filename;
 	}
 	
 	String getFilename() {
 		return this.filename;
 	}
 	
 	void setInstrument(String instrument) {
 		this.instrument = instrument;
 	}
 	
 	String getInstrument() {
 		return this.instrument;
 	}
 	
 	String getStatus() {
 		return this.status;
 	}
 	
 	void setStatus(String val) {
 		this.status = val;
 	}
 	
 	boolean isUnstarted() {
 		return STATUS_UNSTARTED.equals(status);
 	}
 	
 	boolean isWorking() {
 		return STATUS_WORKING.equals(status);
 	}
 	
 	boolean isCompleted() {
 		return STATUS_COMPLETED.equals(status);
 	}
 	
 	void setStatusUnstarted() {
 		status = STATUS_UNSTARTED;
 	}
 	
 	void setStatusWorking() {
 		status = STATUS_WORKING;
 	}
 	
 	void setStatusCompleted() {
 		status = STATUS_COMPLETED;
 	}
 	
 	void setStartingStep(String s) {
 		this.startingStep = s;
 	}
 	
 	String getStartingStep() {
 		return this.startingStep;
 	}
 	
 	boolean addMapping(String varname, String value) {
 		if (varname == null || value == null) {
 			return false;
 		}
 		
 		mappings.put(varname,value);
 		return true;
 	}
 	
 	String getMapping(String varname) {
 		if (varname == null) {
 			return null;
 		}
 		else {
 			return (String) mappings.get(varname);
 		}
 	}
 	
 	Hashtable getMappings() {
 		/* assumes that they won't be changed */
 		return mappings;
 	}
 	
 	/* should I also validate that the status value is valid? */
 	boolean isValid() {
 		if (username == null || password == null || filename == null || instrument == null || status == null) {
 			return false;
 		}
 		else {
 			return true;
 		}
 	}
 	
 	String showValue() {
 		StringBuffer sb = new StringBuffer();
 		sb.append(username);
 		sb.append("\t");
 		sb.append(password);
 		sb.append("\t");
 		sb.append(filename);
 		sb.append("\t");
 		sb.append(instrument);
 		sb.append("\t");
 		sb.append(status);
 		sb.append("\t");
 		sb.append(startingStep);
 		
 		Enumeration keys = mappings.keys();
 		while (keys.hasMoreElements()) {
 			String key = (String) keys.nextElement();
 			sb.append("\t");
 			sb.append(key);
 			sb.append("\t");
 			sb.append((String) mappings.get(key));
 		}
 		
 		return sb.toString();
 	}
 }
