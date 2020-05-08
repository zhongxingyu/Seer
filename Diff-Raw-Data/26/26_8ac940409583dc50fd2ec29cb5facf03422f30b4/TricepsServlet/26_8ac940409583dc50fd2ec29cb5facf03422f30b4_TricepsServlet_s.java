 import java.util.*;
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.net.*;
 
 /**
  *	 This is the central engine that iterates through the nodes
  *	in a schedule producing, e.g., an interview. It also organizes
  *	the connection to the display. In the first version, this is
  *	an http response as defined in the JSDK.
  */
 public class TricepsServlet extends HttpServlet {
 	public static String HELP_T_ICON = null;
 	public static String COMMENT_T_ICON = null;
 	public static String COMMENT_F_ICON = null;
 	public static String REFUSED_T_ICON = null;
 	public static String REFUSED_F_ICON = null;
 	public static String UNKNOWN_T_ICON = null;
 	public static String UNKNOWN_F_ICON = null;
 	public static String NOT_UNDERSTOOD_T_ICON = null;
 	public static String NOT_UNDERSTOOD_F_ICON = null;
 
 	private static int cycle = 0;
 
 	private Triceps triceps = null;
 	private HttpServletRequest req;
 	private HttpServletResponse res;
 	private PrintWriter out;
 	private String firstFocus = null;
 
 	private String scheduleSrcDir = "";
 	private String workingFilesDir = "";
 	private String completedFilesDir = "";
 	private String imageFilesDir = "";
 	private String logoIcon = "";
 	private String floppyDir = "";
 	private String helpURL = "";
 
 	/* hidden variables */
 	private boolean debugMode = false;
 	private boolean developerMode = false;
 	private boolean okToShowAdminModeIcons = false;	// allows AdminModeIcons to be visible
 	private boolean okPasswordForTempAdminMode = false;	// allows AdminModeIcon values to be accepted
 	private boolean showQuestionNum = false;
 	private boolean showAdminModeIcons = false;
 	private boolean autogenOptionNums = true;	// default is to make reading options easy
 	private boolean isSplashScreen = false;
 	private boolean allowEasyBypass = false;	// means that a special value is present, so enable the possibility of okPasswordForTempAdminMode
 	private boolean allowComments = false;
 
 	private String directive = null;	// the default
 	private StringBuffer errors = new StringBuffer();
 	private int currentLanguage = 0;
 
 	/**
 	 * This method runs only when the servlet is first loaded by the
 	 * webserver.  It calls the loadSchedule method to input all the
 	 * nodes into memory.  The Schedule is then available to all
 	 * sessions that might be running.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		String s;
 
 		s = config.getInitParameter("scheduleSrcDir");
 		if (s != null)
 			scheduleSrcDir = s.trim();
 		s = config.getInitParameter("workingFilesDir");
 		if (s != null)
 			workingFilesDir = s.trim();
 		s = config.getInitParameter("completedFilesDir");
 		if (s != null)
 			completedFilesDir = s.trim();
 		s = config.getInitParameter("imageFilesDir");
 		if (s != null)
 			imageFilesDir = s.trim();
 		s = config.getInitParameter("logoIcon");
 		if (s != null)
 			logoIcon = s.trim();
 		s = config.getInitParameter("floppyDir");
 		if (s != null)
 			floppyDir = s.trim();
 		s = config.getInitParameter("helpURL");
 		if (s != null)
 			helpURL = s.trim();
 
 		HELP_T_ICON = imageFilesDir + "help_true.gif";
 		COMMENT_T_ICON = imageFilesDir + "comment_true.gif";
 		COMMENT_F_ICON = imageFilesDir + "comment_false.gif";
 		REFUSED_T_ICON = imageFilesDir + "refused_true.gif";
 		REFUSED_F_ICON = imageFilesDir + "refused_false.gif";
 		UNKNOWN_T_ICON = imageFilesDir + "unknown_true.gif";
 		UNKNOWN_F_ICON = imageFilesDir + "unknown_false.gif";
 		NOT_UNDERSTOOD_T_ICON = imageFilesDir + "not_understood_true.gif";
 		NOT_UNDERSTOOD_F_ICON = imageFilesDir + "not_understood_false.gif";
 
 	}
 
 	public void destroy() {
 		super.destroy();
 	}
 
 	/**
 	 * This method is invoked when an initial URL request is made to the servlet.
 	 * It initializes a session and prepares a response to the client that will
 	 * invoke the POST method on further requests.
 	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		doPost(req,res);
 	}
 
 	/**
 	 * This method is invoked when the servlet is requested with POST variables.  This is
 	 * the case after the first request, handled by doGet(), and all further requests.
 	 */
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
 		try {
 			this.req = req;
 			this.res = res;
 			HttpSession session = req.getSession(true);
 			XmlString form = null;
 			firstFocus = null; // reset it each time
 
 			triceps = (Triceps) session.getValue("triceps");
 
 			res.setContentType("text/html");
 			out = res.getWriter();
 
 			directive = req.getParameter("directive");	// XXX: directive must be set before calling processHidden
 			
 			setGlobalVariables();
 			
 			processPreFormDirectives();
 			processHidden();
 			
 			form = new XmlString(createForm());
 			
 			out.println(header());	// must be processed AFTER createForm, otherwise setFocus() doesn't work
 			new XmlString(getCustomHeader(),out);
 			
 			if (errors.length() > 0) {
 				out.println("<B>");
 				new XmlString(errors.toString(),out);
 				out.println("</B><HR>");
 				errors =  new StringBuffer();
 			}
 			if (form.hasErrors() && (developerMode || debugMode)) {
 				StringBuffer errs = new StringBuffer("<B>");
 				Vector parsingErrs = form.getErrors();
 				for (int i=0;i<parsingErrs.size();++i) {
 					errs.append((String) parsingErrs.elementAt(i) + "<br>");
 				}
 				errs.append("</B>");
 				new XmlString(errs.toString(),out);
 			}
 			
 			out.println(form.toString());		
 
 			if (!isSplashScreen) {
 				new XmlString(generateDebugInfo(),out);
 			}
 			
 			out.println(footer());	// should not be parsed
 			out.flush();
 			out.close();
 
 			/* Store appropriate stuff in the session */
 			if (triceps != null)
 				session.putValue("triceps", triceps);
 
 			if ("next".equals(directive)) {
 				triceps.toTSV(workingFilesDir);
 			}
 		}
 		catch (Throwable t) {
 			System.err.println("Unexpected error: " + t.getMessage());
 			t.printStackTrace(System.err);
 		}
 	}
 	
 	private void processPreFormDirectives() {
 		/* setting language doesn't use directive parameter */
 		if (triceps != null) {
 			String language = req.getParameter("LANGUAGE");
 			if (language != null && language.trim().length() > 0) {
 				triceps.setLanguage(language.trim());
 				directive = "refresh current";
 			}
 		}
 		
 		if (triceps == null)
 			return;
 
 			
 		/* Want to evaluate expression before doing rest so can see results of changing global variable values */
 		if ("evaluate expr:".equals(directive)) {
 			String expr = req.getParameter("evaluate expr:");
 			if (expr != null) {
 				Datum datum = triceps.evaluateExpr(expr);
 
 				setError("<TABLE WIDTH='100%' CELLPADDING='2' CELLSPACING='1' BORDER='1'>");
 				setError("<TR><TD>Equation</TD><TD><B>" + expr + "</B></TD><TD>Type</TD><TD><B>" + Datum.TYPES[datum.type()] + "</B></TD></TR>");
 				setError("<TR><TD>String</TD><TD><B>" + datum.stringVal(true) + "</B></TD><TD>boolean</TD><TD><B>" + datum.booleanVal() + "</B></TD></TR>");
 				setError("<TR><TD>double</TD><TD><B>" + datum.doubleVal() + "</B></TD><TD>&nbsp;</TD><TD>&nbsp;</TD></TR>");
 				setError("<TR><TD>date</TD><TD><B>" + datum.dateVal() + "</B></TD><TD>month</TD><TD><B>" + datum.monthVal() + "</B></TD></TR>");
 				setError("</TABLE>");
 
 				Vector errs = triceps.getErrors();
 				for (int i=0;i<errs.size();++i) {
 					setError((String) errs.elementAt(i));
 				}
 			}
 		}
 	}	
 	
 	private void setGlobalVariables() {
 		if (triceps != null) {
 			debugMode = triceps.isDebugMode();
 			developerMode = triceps.isDeveloperMode();
 			showQuestionNum = triceps.isShowQuestionRef();
 			showAdminModeIcons = triceps.isShowAdminModeIcons();
 			autogenOptionNums = triceps.isAutoGenOptionNum();
 			allowComments = triceps.isAllowComments();
 		}
 		else {
 			debugMode = false;
 			developerMode = false;
 			showQuestionNum = false;
 			showAdminModeIcons = false;
 			autogenOptionNums = true;
 			allowComments = false;
 		}
 		allowEasyBypass = false;
 		okPasswordForTempAdminMode = false;	
 		okToShowAdminModeIcons = false;
 		isSplashScreen = false;
 	}
 
 	private void processHidden() {
 		/* Has side-effects - so must occur before createForm() */
 		if (triceps == null)
 			return;
 			
 		String settingAdminMode = req.getParameter("PASSWORD_FOR_ADMIN_MODE");
 		if (settingAdminMode != null && !settingAdminMode.trim().equals("")) {
 			/* if try to enter a password, make sure that doesn't reset the form if password fails */
 			String passwd = triceps.getPasswordForAdminMode();
 			if (passwd != null) {
 				if (passwd.trim().equals(settingAdminMode.trim())) {
 					okToShowAdminModeIcons = true;	// so allow AdminModeIcons to be displayed
 				}
 				else {
 					setError("Incorrect password to enter Administrative Mode");
 				}
 			}
 			directive = "refresh current";	// so that will set the admin mode password
 		}
 			
 		if (triceps.isTempPassword(req.getParameter("TEMP_ADMIN_MODE_PASSWORD"))) {
 			// enables the password for this session only
 			okPasswordForTempAdminMode = true;	// allow AdminModeIcon values to be accepted
 		}
 
 		/** Process requests to change developerMode-type status **/
 		if (directive != null) {
 			/* Toggle these values, as requested */
 			if (directive.startsWith("turn developerMode")) {
 				developerMode = !developerMode;
 				triceps.nodes.setReserved(Schedule.DEVELOPER_MODE, String.valueOf(developerMode));
 				directive = "refresh current";
 			}
 			else if (directive.startsWith("turn debugMode")) {
 				debugMode = !debugMode;
 				triceps.nodes.setReserved(Schedule.DEBUG_MODE, String.valueOf(debugMode));
 				directive = "refresh current";
 			}
 			else if (directive.startsWith("turn showQuestionNum")) {
 				showQuestionNum = !showQuestionNum;
 				triceps.nodes.setReserved(Schedule.SHOW_QUESTION_REF, String.valueOf(showQuestionNum));
 				directive = "refresh current";
 			}
 		}
 	}
 
 	private String getCustomHeader() {
 		StringBuffer sb = new StringBuffer();
 
 		sb.append("<TABLE BORDER='0' CELLPADDING='0' CELLSPACING='3' WIDTH='100%'>");
 		sb.append("<TR>");
 		sb.append("<TD WIDTH='1%'>");
 
 		String logo = (!isSplashScreen && triceps != null) ? triceps.getIcon() : logoIcon;
 		if (logo.trim().equals("")) {
 			sb.append("&nbsp;");
 		}
 		else {
 			sb.append("<IMG NAME='icon' SRC='" + (imageFilesDir + logo) + "' ALIGN='top' BORDER='0'" +
 				((!isSplashScreen) ? " onMouseDown='javascript:setAdminModePassword();'":"") + " ALT='Logo'>");
 		}
 		sb.append("	</TD>");
 		sb.append("	<TD ALIGN='left'><FONT SIZE='5'><B>" + ((triceps != null && !isSplashScreen) ? triceps.getHeaderMsg() : "Triceps") + "</B></FONT></TD>");
 		sb.append("	<TD WIDTH='1%'><IMG SRC='" + HELP_T_ICON + "' ALIGN='top' BORDER='0' ALT='Help' onMouseDown='javascript:help(\"" + helpURL + "\");'></TD>");
 		sb.append("</TR>");
 		sb.append("</TABLE>");
 		sb.append("<HR>");
 
 		return sb.toString();
 	}
 
 	private String footer() {
 		StringBuffer sb = new StringBuffer();
 
 		sb.append("</body>\n");
 		sb.append("</html>\n");
 		return sb.toString();
 	}
 
 	private TreeMap getSortedNames(String dir, boolean isSuspended) {
 		TreeMap names = new TreeMap();
 		Schedule sched = null;
 		Object prevVal = null;
 		String defaultTitle = null;
 		String title = null;
 
 		try {
 			ScheduleList interviews = new ScheduleList(dir);
 
 			if (interviews.hasErrors()) {
 				setError("Error getting list of available interviews:");
 				setError(interviews.getErrors());
 			}
 			else {
 				Vector schedules = interviews.getSchedules();
 				for (int i=0;i<schedules.size();++i) {
 					sched = (Schedule) schedules.elementAt(i);
 
 					try {
 						defaultTitle = getScheduleInfo(sched,isSuspended);
 						title = defaultTitle;
 						for (int count=2;true;++count) {
 							prevVal = names.put(title,sched.getLoadedFrom());
 							if (prevVal != null) {
 								names.put(title,prevVal);
 								title = defaultTitle + " (copy " + count + ")";
 							}
 							else {
 								break;
 							}
 						}
 					}
 					catch (Throwable t) {
 						setError("Unexpected error: " + t.getMessage());
 					}
 				}
 			}
 		}
 		catch (Throwable t) {
 			setError("Unexpected error: " + t.getMessage());
 		}
 		return names;
 	}
 	
 	private String getScheduleInfo(Schedule sched, boolean isSuspended) {
 		if (sched == null)
 			return null;
 			
 		StringBuffer sb = new StringBuffer();
 		String s = null;
 		
 		if (isSuspended) {
 			sb.append(sched.getReserved(Schedule.TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS));
 		}
 		else {
 			s = sched.getReserved(Schedule.TITLE);
 			if (s != null && s.trim().length() > 0) {
 				sb.append(s);
 			}
 			else {
 				sb.append("NO_TITLE");
 			}
 			s = sched.getReserved(Schedule.LANGUAGES);
 			if (s != null && s.trim().length() > 0 && s.indexOf("|") != -1) {
 				sb.append(" [" + s + "]");
 			}
 		}
 		
 		return sb.toString();
 	}	
 	
 	private String selectFromInterviewsInDir(String selectTarget, String dir, boolean isSuspended) {	
 		StringBuffer sb = new StringBuffer();
 		
 		try {
 			TreeMap names = getSortedNames(dir,isSuspended);
 
 			if (names.size() > 0) {
 				sb.append("<select name='" + selectTarget + "'>");
 				if (isSuspended) {
 					/* add a blank line so don't accidentally resume a file instead of starting one */
 					sb.append("<option value=''>&nbsp;</option>");
 				}
 				Iterator iterator = names.keySet().iterator();
 				while(iterator.hasNext()) {
 					String title = (String) iterator.next();
 					String target = (String) names.get(title);
 					sb.append("	<option value='" + target + "'>" + title + "</option>");
 				}
 				sb.append("</select>");
 			}
 		}
 		catch (Throwable t) {
 			setError("Error building sorted list of interviews: " + t.getMessage());
 		}
 		
 		if (sb.length() == 0)
 			return "&nbsp;";
 		else
 			return sb.toString();
 	}
 	
 	private String createForm() {
 		StringBuffer sb = new StringBuffer();
 		String formStr = null;
 		
 		sb.append("<FORM method='POST' name='myForm' action='" + HttpUtils.getRequestURL(req) + "'>");
 		
 		formStr = processDirective();	// since this sets isSplashScreen, which is needed to decide whether to display language buttons
 
 		sb.append(languageButtons());
 
 		sb.append(formStr);
 		
 		sb.append("<input type='HIDDEN' name='PASSWORD_FOR_ADMIN_MODE' value=''>");	// must manually bypass each time
 		sb.append("<input type='HIDDEN' name='LANGUAGE' value=''>");	// must manually bypass each time
 
 		sb.append("</FORM>");
 		
 		return sb.toString();
 	}
 	
 	private String languageButtons() {
 		if (isSplashScreen || triceps == null || !triceps.isAllowLanguageSwitching())
 			return "";
 			
 		StringBuffer sb = new StringBuffer();
 		
 		/* language switching section */
 		if (!isSplashScreen && triceps != null) {
 			Vector languages = triceps.nodes.getLanguages();
 			if (languages.size() > 1) {
 				sb.append("<TABLE WIDTH='100%' BORDER='0'><TR><TD ALIGN='center'>");
 				for (int i=0;i<languages.size();++i) {
 					String language = (String) languages.elementAt(i);
 					boolean selected = (i == triceps.getLanguage());
 					sb.append(((selected) ? "<U>" : "") +
 						"<INPUT TYPE='button' onClick='javascript:setLanguage(\"" + language + "\");' VALUE='" + language + "'>" +
 						((selected) ? "</U>" : ""));
 				}
 				sb.append("</TD></TR></TABLE>");
 			}
 		}		
 		return sb.toString();
 	}
 
 	private String processDirective() {
 		boolean ok = true;
 		int gotoMsg = Triceps.OK;
 		StringBuffer sb = new StringBuffer();
 		Enumeration nodes;
 		
 		// get the POSTed directive (start, back, next, help, suspend, etc.)	- default is opening screen
 		if (directive == null || "select new interview".equals(directive)) {
 			/* Construct splash screen */
 			isSplashScreen = true;
 			
 			sb.append("<TABLE CELLPADDING='2' CELLSPACING='2' BORDER='1'>");
 			sb.append("<TR><TD>" + "Please select an interview/questionnaire from the pull-down list:  " + "</TD>");
 			sb.append("<TD>");
 			
 			/* Build the list of available interviews */
 			sb.append(selectFromInterviewsInDir("schedule",scheduleSrcDir,false));
 
 			sb.append("</TD>");
 			sb.append("<TD><input type='SUBMIT' name='directive' value='" + "START" + "'></TD>");
 			sb.append("</TR>");
 			
 			/* Build the list of suspended interviews */
 			sb.append("<TR><TD>" + "OR, restore an interview/questionnaire in progress:  " + "</TD>");
 			sb.append("<TD>");
 			
 			sb.append(selectFromInterviewsInDir("RestoreSuspended",workingFilesDir,true));
 			
 			if (developerMode) {
 				sb.append("<input type='text' name='" + "RESTORE" + "'>");
 			}
 			sb.append("</TD>");
 			sb.append("<TD><input type='SUBMIT' name='directive' value='" + "RESTORE" + "'></TD>");
 			sb.append("</TR></TABLE>");
 			
 			return sb.toString();
 		}
 		else if (directive.equals("START")) {
 			// load schedule
 			triceps = new Triceps(req.getParameter("schedule"));
 			ok = triceps.isValid();
 
 			if (!ok) {
 				directive = null;
 				return processDirective();
 			}
 			
 			// re-check developerMode options - they aren't set via the hidden options, since a new copy of Triceps created
 			setGlobalVariables();
 
 			ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
 			// ask question
 		}
 		else if (directive.equals("RESTORE")) {
 			String restore;
 
 			restore = req.getParameter("RESTORE");
 			if (restore == null || restore.trim().equals("")) {
 				restore = req.getParameter("RestoreSuspended");
 				if (restore == null || restore.trim().equals("")) {
 					directive = null;
 					return processDirective();
 				}
 			}
 
 
 			// load schedule
 			triceps = new Triceps(restore);
 			ok = triceps.isValid();
 
 			if (!ok) {
 				directive = null;
 				
 				setError("Unable to find or access schedule '" + restore + "'");
 				return processDirective();
 			}
 			// re-check developerMode options - they aren't set via the hidden options, since a new copy of Triceps created
 			setGlobalVariables();
 
 			ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
 
 			// ask question
 		}
 		else if (directive.equals("jump to:")) {
 			gotoMsg = triceps.gotoNode(req.getParameter("jump to:"));
 			ok = (gotoMsg == Triceps.OK);
 			// ask this question
 		}
 		else if (directive.equals("refresh current")) {
 			ok = true;
 			// re-ask the current question
 		}
 		else if (directive.equals("restart (clean)")) { // restart from scratch
 			triceps.resetEvidence();
 			ok = ((gotoMsg = triceps.gotoFirst()) == Triceps.OK);	// don't proceed if prior error
 			// ask first question
 		}
 		else if (directive.equals("reload questions")) { // debugging option
 			ok = triceps.reloadSchedule();
 			if (ok) {
 				setError("Schedule restored successfully");
 			}
 			// re-ask current question
 		}
 		else if (directive.equals("save to:")) {
 			String name = req.getParameter("save to:");
 			ok = triceps.toTSV(workingFilesDir,name);
 			if (ok) {
 				setError("Interview saved successfully as " + (workingFilesDir + name));
 			}
 		}
 		else if (directive.equals("show XML")) {
 			setError("Use 'Show Source' to see data in Schedule as XML");
 			sb.append("<!--\n" + triceps.toXML() + "\n-->");
 			sb.append("<HR>");
 		}
 		else if (directive.equals("show Syntax Errors")) {
 			Vector pes = triceps.collectParseErrors();
 
 			if (pes == null || pes.size() == 0) {
 				setError("No syntax errors were found");
 			}
 			else {
 				Vector errs;
 				Vector syntaxErrors = new Vector();
 				
 				int numToBeShown = 0;
 				
 				for (int i=0;i<pes.size();++i) {
 					ParseError pe = (ParseError) pes.elementAt(i);
 					
 					/* switch over available diplay options */
 				}
 				syntaxErrors = pes;
 				for (int i=0;i<syntaxErrors.size();++i) {
 					ParseError pe = (ParseError) syntaxErrors.elementAt(i);
 					Node n = pe.getNode();
 
 					if (i == 0) {
 						setError("<FONT color='red'>" +
 							"The following syntax errors were found in file " + (n.getSourceFile()) + "</FONT>");
 						setError("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'>");
 						setError("<TR><TD>line#</TD><TD>name</TD><TD>Dependencies</TD><TD><B>Dependency Errors</B></TD><TD>Action Type</TD><TD>Action</TD><TD><B>Action Errors</B></TD><TD><B>Node Errors</B></TD><TD><B>Naming Errors</B></TD><TD><B>AnswerChoices Errors</B></TD><TD><B>Readback Errors</B></TD></TR>");
 					}
 
 					setError("<TR><TD>" + n.getSourceLine() + "</TD><TD>" + (n.getLocalName()) + "</TD>");
 					setError("<TD>" + n.getDependencies() + "</TD><TD>");
 
 					setError(pe.hasDependenciesErrors() ? pe.getDependenciesErrors() : "&nbsp;");
 					setError("</TD><TD>" + Node.ACTION_TYPES[n.getQuestionOrEvalType()] + "</TD><TD>" + n.getQuestionOrEval() + "</TD><TD>");
 
 					setError(pe.hasQuestionOrEvalErrors() ? pe.getQuestionOrEvalErrors() : "&nbsp;");
 					setError("</TD><TD>");
 
 					if (!pe.hasNodeParseErrors()) {
 						setError("&nbsp;");
 					}
 					else {
 						errs = pe.getNodeParseErrors();
 						for (int j=0;j<errs.size();++j) {
 							setError("" + (j+1) + ")&nbsp;" + (String) errs.elementAt(j));	
 						}
 					}
 					setError("</TD><TD>");
 					
 					if (!pe.hasNodeNamingErrors()) {
 						setError("&nbsp;");
 					}
 					else {
 						errs = pe.getNodeNamingErrors();
 						for (int j=0;j<errs.size();++j) {
 							setError("" + (j+1) + ")&nbsp;" + (String) errs.elementAt(j));	
 						}
 					}
 					
 					setError("<TD>" + ((pe.hasAnswerChoicesErrors()) ? pe.getAnswerChoicesErrors() : "&nbsp;") + "</TD>");
 					setError("<TD>" + ((pe.hasReadbackErrors()) ? pe.getReadbackErrors() : "&nbsp;") + "</TD>");
 					 
 					setError("</TR>");					
 				}
 				setError("</TABLE><HR>");
 			}
 			Vector scheduleErrors = triceps.nodes.getErrors();
 			if (scheduleErrors.size() > 0) {
 				setError("<FONT color='red'>" +
 					"The following flow errors were found" + "</FONT>");
 				setError("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'><TR><TD>");
 				for (int i=0;i<scheduleErrors.size();++i) {
 					setError((String) scheduleErrors.elementAt(i) + "<BR>");
 				}
 				setError("</TD></TR></TABLE>");
 			}
 			Vector evidenceErrors = triceps.evidence.getErrors();
 			if (evidenceErrors.size() > 0) {
 				setError("<FONT color='red'>" +
 					"The following data access errors were found" + "</FONT>");
 				setError("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'><TR><TD>");
 				for (int i=0;i<evidenceErrors.size();++i) {
 					setError((String) evidenceErrors.elementAt(i) + "<BR>");
 				}
 				setError("</TD></TR></TABLE>");
 			}			
 		}
 		else if (directive.equals("next")) {
 			// store current answer(s)
 			Enumeration questionNames = triceps.getQuestions();
 
 			while(questionNames.hasMoreElements()) {
 				Node q = (Node) questionNames.nextElement();
 				boolean status;
 
 				String answer = req.getParameter(q.getLocalName());
 				String comment = req.getParameter(q.getLocalName() + "_COMMENT");
 				String special = req.getParameter(q.getLocalName() + "_SPECIAL");
 
 				status = triceps.storeValue(q, answer, comment, special, (okPasswordForTempAdminMode || showAdminModeIcons));
 				ok = status && ok;
 
 			}
 			// goto next
 			ok = ok && ((gotoMsg = triceps.gotoNext()) == Triceps.OK);	// don't proceed if prior errors - e.g. unanswered questions
 
 			if (gotoMsg == Triceps.AT_END) {
 				// save the file, but still give the option to go back and change answers
 				boolean savedOK;
 				String filename = triceps.getFilename();
 
 				setError("Thank you, the interview is completed");
 				triceps.toTSV(workingFilesDir,filename);
 				savedOK = triceps.toTSV(completedFilesDir,filename);
 				ok = savedOK && ok;
 				if (savedOK) {
 					setError("Interview saved successfully as " + (completedFilesDir + filename));
 				}
 
 				savedOK = triceps.toTSV(floppyDir,filename);
 				ok = savedOK && ok;
 				if (savedOK) {
 					setError("Interview saved successfully as " + (floppyDir + filename));
 				}
 			}
 
 			// don't goto next if errors
 			// ask question
 		}
 		else if (directive.equals("previous")) {
 			// don't store current
 			// goto previous
 			gotoMsg = triceps.gotoPrevious();
 			ok = ok && (gotoMsg == Triceps.OK);
 			// ask question
 		}
 
 
 		/* Show any accumulated errors */
 		Vector errs = triceps.getErrors();
 		int errCount=0;
 		for (;errCount<errs.size();++errCount) {
 			setError((String) errs.elementAt(errCount));
 		}
 
 		nodes = triceps.getQuestions();
 		while (nodes.hasMoreElements()) {
 			Node n = (Node) nodes.nextElement();
 			if (n.hasRuntimeErrors()) {
 				if (++errCount == 1) {
 					setError("Please answer the question(s) listed in " + "<FONT color='red'>" + "RED" + "</FONT>" + " before proceeding");
 				}
 				if (n.focusableArray()) {
 					firstFocus = n.getLocalName() + "[0]";
 					break;
 				}
 				else if (n.focusable()) {
 					firstFocus = n.getLocalName();
 					break;
 				}
 			}
 		}
 
 		if (firstFocus == null) {
 			nodes = triceps.getQuestions();
 			while (nodes.hasMoreElements()) {
 				Node n = (Node) nodes.nextElement();
 				if (n.focusableArray()) {
 					firstFocus = n.getLocalName() + "[0]";
 					break;
 				}
 				else if (n.focusable()) {
 					firstFocus = n.getLocalName();
 					break;
 				}
 			}
 		}
 		if (firstFocus == null) {
 			firstFocus = "directive[0]";	// try to focus on Next button if nothing else available
 		}
 
 		firstFocus = (new XmlString(firstFocus)).toString();	// make sure properly formatted
 		
 		sb.append(queryUser());
 		
 		return sb.toString();
 	}
 
 	/**
 	 * This method assembles the displayed question and answer options
 	 * and formats them in HTML for return to the client browser.
 	 */
 	private String queryUser() {
 		// if parser internal to Schedule, should have method access it, not directly
 		StringBuffer sb = new StringBuffer();
 		
 		if (triceps == null)
 			return "";
 
 		if (debugMode && developerMode) {
 			sb.append("QUESTION AREA");
 		}
 
 		Enumeration questionNames = triceps.getQuestions();
 		String color;
 		String errMsg;
 
 		sb.append("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'>");
 		for(int count=0;questionNames.hasMoreElements();++count) {
 			Node node = (Node) questionNames.nextElement();
 			Datum datum = triceps.getDatum(node);
 
			node.setAnswerLanguageNum(triceps.getLanguage());	// must do this first

 			if (node.hasRuntimeErrors()) {
 				color = " color='red'";
 				StringBuffer errStr = new StringBuffer("<FONT color='red'>");
 
 				Vector errs = node.getRuntimeErrors();
 
 				for (int j=0;j<errs.size();++j) {
 					if (j > 0) {
 						errStr.append("<BR>");
 					}
 					errStr.append((String) errs.elementAt(j));
 				}
 				errStr.append("</FONT>");
 				errMsg = errStr.toString();
 			}
 			else {
 				color = null;
 				errMsg = "";
 			}
 
 			sb.append("<TR>");
 
 			if (showQuestionNum) {
 				if (color != null) {
 					sb.append("<TD><FONT" + color + "><B>" + node.getExternalName() + "</B></FONT></TD>");
 				}
 				else {
 					sb.append("<TD>" + node.getExternalName() + "</TD>");
 				}
 			}
 
 			String inputName = node.getLocalName();
 
 			boolean isSpecial = (datum.exists() && !datum.isType(Datum.STRING) && !datum.isType(Datum.NA));
 			allowEasyBypass = allowEasyBypass || isSpecial;	// if a value has already been refused, make it easy to re-refuse it
 
 			String clickableOptions = buildClickableOptions(node,inputName,isSpecial);
 
 			switch(node.getAnswerType()) {
 				case Node.NOTHING:
 					if (color != null) {
 						sb.append("<TD COLSPAN='3'><FONT" + color + ">" + triceps.getQuestionStr(node) + "</FONT></TD>");
 					}
 					else {
 						sb.append("<TD COLSPAN='3'>" + triceps.getQuestionStr(node) + "</TD>");
 					}
 					break;
 				case Node.RADIO_HORIZONTAL:
 					sb.append("<TD COLSPAN='3'>");
 					sb.append("<input type='HIDDEN' name='" + (inputName + "_COMMENT") + "' value='" + (node.getComment()) + "'>");
 					sb.append("<input type='HIDDEN' name='" + (inputName + "_SPECIAL") + "' value='" +
 						((isSpecial) ? (triceps.toString(node,true)) : "") +
 						"'>");
 					sb.append("<input type='HIDDEN' name='" + (inputName + "_HELP") + "' value='" + (node.getHelpURL()) + "'>");
 					if (color != null) {
 						sb.append("<FONT" + color + ">" + triceps.getQuestionStr(node) + "</FONT>");
 					}
 					else {
 						sb.append(triceps.getQuestionStr(node));
 					}
 					sb.append("</TD></TR><TR>");
 					if (showQuestionNum) {
 						sb.append("<TD>&nbsp;</TD>");
 					}
 					sb.append("<TD WIDTH='1%' NOWRAP>" + clickableOptions + "</TD>");
 					sb.append(node.prepareChoicesAsHTML(triceps.parser,triceps.evidence,datum,errMsg,autogenOptionNums));
 					break;
 				default:
 					sb.append("<TD>");
 					sb.append("<input type='HIDDEN' name='" + (inputName + "_COMMENT") + "' value='" + node.getComment() + "'>");
 					sb.append("<input type='HIDDEN' name='" + (inputName + "_SPECIAL") + "' value='" +
 						((isSpecial) ? (triceps.toString(node,true)) : "") +
 						"'>");
 					sb.append("<input type='HIDDEN' name='" + (inputName + "_HELP") + "' value='" + node.getHelpURL() + "'>");
 					if (color != null) {
 						sb.append("<FONT" + color + ">" + triceps.getQuestionStr(node) + "</FONT>");
 					}
 					else {
 						sb.append(triceps.getQuestionStr(node));
 					}
 					sb.append("</TD><TD WIDTH='1%' NOWRAP>" + clickableOptions + "</TD>");
 					sb.append("<TD>" + node.prepareChoicesAsHTML(triceps.parser,triceps.evidence,datum,autogenOptionNums) + errMsg + "</TD>");
 					break;
 			}
 
 			sb.append("</TR>");
 		}
 		sb.append("<TR><TD COLSPAN='" + ((showQuestionNum) ? 4 : 3) + "' ALIGN='center'>");
 		sb.append("<input type='SUBMIT' name='directive' value='next'>");
 		sb.append("<input type='SUBMIT' name='directive' value='previous'>");
 		
 		if (allowEasyBypass || okToShowAdminModeIcons) {
 			/* enables TEMP_ADMIN_MODE going forward for one screen */
 			sb.append("<input type='HIDDEN' name='TEMP_ADMIN_MODE_PASSWORD' value='" + triceps.createTempPassword() + "'>");
 		}
 
 		sb.append("</TD></TR>");
 
 		if (developerMode) {
 			sb.append("<TR><TD COLSPAN='" + ((showQuestionNum) ? 4 : 3 ) + "' ALIGN='center'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "select new interview" + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "restart (clean)" + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "jump to:" + "' size='10'>");
 			sb.append("<input type='text' name='" + "jump to:" + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "save to:" + "'>");
 			sb.append("<input type='text' name='" + "save to:" + "'>");
 			sb.append("</TD></TR>");
 			sb.append("<TR><TD COLSPAN='" + ((showQuestionNum) ? 4 : 3 ) + "' ALIGN='center'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "reload questions" + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "show Syntax Errors" + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "show XML" + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "evaluate expr:" + "'>");
 			sb.append("<input type='text' name='" + "evaluate expr:" + "'>");
 			sb.append("</TD></TR>");
 		}
 
 		sb.append(showOptions());
 
 		sb.append("</TABLE>");
 
 		return sb.toString();
 	}
 
 	private String buildClickableOptions(Node node, String inputName, boolean isSpecial) {
 		StringBuffer sb = new StringBuffer();
 		
 		if (triceps == null)
 			return "";
 
 		Datum datum = triceps.getDatum(node);
 		
 		if (datum == null) {
 			return "&nbsp;";
 		}
 
 		boolean isRefused = false;
 		boolean isUnknown = false;
 		boolean isNotUnderstood = false;
 
 		if (datum.isType(Datum.REFUSED))
 			isRefused = true;
 		else if (datum.isType(Datum.UNKNOWN))
 			isUnknown = true;
 		else if (datum.isType(Datum.NOT_UNDERSTOOD))
 			isNotUnderstood = true;
 
 		String helpURL = node.getHelpURL();
 		if (helpURL != null && helpURL.trim().length() != 0) {
 			sb.append("<IMG SRC='" + HELP_T_ICON +
 				"' ALIGN='top' BORDER='0' ALT='Help' onMouseDown='javascript:help(\"" + helpURL + "\");'>");
 		}
 		else {
 			// don't show help icon if no help is available?
 		}
 
 		String comment = node.getComment();
 		if (showAdminModeIcons || okToShowAdminModeIcons || allowComments) {
 			if (comment != null && comment.trim().length() != 0) {
 				sb.append("<IMG NAME='" + inputName + "_COMMENT_ICON" + "' SRC='" + COMMENT_T_ICON +
 					"' ALIGN='top' BORDER='0' ALT='" + "Add a Comment" + "' onMouseDown='javascript:comment(\"" + inputName + "\");'>");
 			}
 			else  {
 				sb.append("<IMG NAME='" + inputName + "_COMMENT_ICON" + "' SRC='" + COMMENT_F_ICON +
 					"' ALIGN='top' BORDER='0' ALT='" + "Add a Comment" + "' onMouseDown='javascript:comment(\"" + inputName + "\");'>");
 			}
 		}
 		
 		/* If something has been set as Refused, Unknown, etc, allow going forward without additional headache */
 
 		if (showAdminModeIcons || okToShowAdminModeIcons || isSpecial) {
 			sb.append("<IMG NAME='" + inputName + "_REFUSED_ICON" + "' SRC='" + ((isRefused) ? REFUSED_T_ICON : REFUSED_F_ICON) +
 				"' ALIGN='top' BORDER='0' ALT='" + "Set as Refused" + "' onMouseDown='javascript:markAsRefused(\"" + inputName + "\");'>");
 			sb.append("<IMG NAME='" + inputName + "_UNKNOWN_ICON" + "' SRC='" + ((isUnknown) ? UNKNOWN_T_ICON : UNKNOWN_F_ICON) +
 				"' ALIGN='top' BORDER='0' ALT='" + "Set as Unknown" + "' onMouseDown='javascript:markAsUnknown(\"" + inputName + "\");'>");
 			sb.append("<IMG NAME='" + inputName + "_NOT_UNDERSTOOD_ICON" + "' SRC='" + ((isNotUnderstood) ? NOT_UNDERSTOOD_T_ICON : NOT_UNDERSTOOD_F_ICON) +
 				"' ALIGN='top' BORDER='0' ALT='" + "Set as Not Understood" + "' onMouseDown='javascript:markAsNotUnderstood(\"" + inputName + "\");'>");
 		}
 		
 		if (sb.length() == 0) {
 			return "&nbsp;";
 		}
 		else {
 			return sb.toString();
 		}
 	}
 
 	private String generateDebugInfo() {
 		StringBuffer sb = new StringBuffer();
 		// Complete printout of what's been collected per node
 		if (triceps == null)
 			return "";
 
 		if (developerMode && debugMode) {
 			sb.append("<hr>");
 			sb.append("CURRENT QUESTION(s)");
 			sb.append("<TABLE CELLPADDING='2' CELLSPACING='1'  WIDTH='100%' BORDER='1'>");
 			Enumeration questionNames = triceps.getQuestions();
 
 			while(questionNames.hasMoreElements()) {
 				Node n = (Node) questionNames.nextElement();
 				sb.append("<TR>");
 				sb.append("<TD>" + n.getExternalName() + "</TD>");
 				sb.append("<TD><B>" + triceps.toString(n,true) + "</B></TD>");
 				sb.append("<TD>" + Datum.TYPES[n.getDatumType()] + "</TD>");
 				sb.append("<TD>" + n.getLocalName() + "</TD>");
 				sb.append("<TD>" + n.getConcept() + "</TD>");
 				sb.append("<TD>" + n.getDependencies() + "</TD>");
 				sb.append("<TD>" + n.getQuestionOrEvalTypeField() + "</TD>");
 				sb.append("<TD>" + n.getQuestionOrEval() + "</TD>");
 				sb.append("</TR>");
 			}
 			sb.append("</TABLE>");
 
 
 			sb.append("<hr>");
 			sb.append("EVIDENCE AREA");
 			sb.append("<TABLE CELLPADDING='2' CELLSPACING='1'  WIDTH='100%' BORDER='1'>");
 			for (int i = triceps.size()-1; i >= 0; i--) {
 				Node n = triceps.getNode(i);
 				Datum d = triceps.getDatum(n);
 				if (!triceps.isSet(n))
 					continue;
 				sb.append("<TR>");
 				sb.append("<TD>" + (i + 1) + "</TD>");
 				sb.append("<TD>" + n.getExternalName() + "</TD>");
 				if (!d.isType(Datum.STRING)) {
 					sb.append("<TD><B><I>" + triceps.toString(n,true) + "</I></B></TD>");
 				}
 				else {
 					sb.append("<TD><B>" + triceps.toString(n,true) + "</B></TD>");
 				}
 				sb.append("<TD>" +  Datum.TYPES[n.getDatumType()] + "</TD>");
 				sb.append("<TD>" + n.getLocalName() + "</TD>");
 				sb.append("<TD>" + n.getConcept() + "</TD>");
 				sb.append("<TD>" + n.getDependencies() + "</TD>");
 				sb.append("<TD>" + n.getQuestionOrEvalTypeField() + "</TD>");
 				sb.append("<TD>" + n.getQuestionOrEval() + "</TD>");
 				sb.append("</TR>");
 			}
 			sb.append("</TABLE>");
 		}
 		return sb.toString();
 	}
 
 	private String showOptions() {
 		if (developerMode) {
 			StringBuffer sb = new StringBuffer();
 
 			sb.append("<TR><TD COLSPAN='" + ((showQuestionNum) ? 4 : 3 ) + "' ALIGN='center'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "turn developerMode " + ((developerMode) ? "OFF" : "ON") + "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "turn debugMode " + ((debugMode) ? "OFF" : "ON" )+ "'>");
 			sb.append("<input type='SUBMIT' name='directive' value='" + "turn showQuestionNum " + ((showQuestionNum) ? "OFF" : "ON") + "'>");
 			sb.append("</TD></TR>");
 			return sb.toString();
 		}
 		else
 			return "";
 	}
 	
 	private String createJavaScript() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("<SCRIPT  type=\"text/javascript\"> <!--\n");
 		sb.append("var actionName = null;\n");
 		sb.append("\n");
 		sb.append("function init() {\n");
 
 		if (firstFocus != null) {
 			sb.append("	document.myForm." + firstFocus + ".focus();\n");
 		}
 
 		sb.append("}\n");
 		sb.append("function setAdminModePassword(name) {\n");
 		sb.append("	var ans = prompt('" + 
 			"Enter password to enter Administrative Mode" + 
 				"','');\n");
 		sb.append("	if (ans == null || ans == '') return;\n");
 		sb.append("	document.myForm.PASSWORD_FOR_ADMIN_MODE.value = ans;\n");
 		sb.append("	document.myForm.submit();\n\n");
 		sb.append("}\n");
 		sb.append("function markAsRefused(name) {\n");
 		sb.append("	if (!name) name = actionName;\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	var val = document.myForm.elements[name + '_SPECIAL'];\n");
 		sb.append("	if (val.value == '*REFUSED*') {\n");
 		sb.append("		val.value = '';\n");
 		sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + REFUSED_F_ICON + "';\n");
 		sb.append("	} else {\n");
 		sb.append("		val.value = '*REFUSED*';\n");
 		sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + REFUSED_T_ICON + "';\n");
 		sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + UNKNOWN_F_ICON + "';\n");
 		sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + NOT_UNDERSTOOD_F_ICON + "';\n");
 		sb.append("	}\n}\n");
 		sb.append("function markAsUnknown(name) {\n");
 		sb.append("	if (!name) name = actionName;\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	var val = document.myForm.elements[name + '_SPECIAL'];\n");
 		sb.append("	if (val.value == '*UNKNOWN*') {\n");
 		sb.append("		val.value = '';\n");
 		sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + UNKNOWN_F_ICON + "';\n");
 		sb.append("	} else {\n");
 		sb.append("		val.value = '*UNKNOWN*';\n");
 		sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + REFUSED_F_ICON + "';\n");
 		sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + UNKNOWN_T_ICON + "';\n");
 		sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + NOT_UNDERSTOOD_F_ICON + "';\n");
 		sb.append("	}\n}\n");
 		sb.append("function markAsNotUnderstood(name) {\n");
 		sb.append("	if (!name) name = actionName;\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	var val = document.myForm.elements[name + '_SPECIAL'];\n");
 		sb.append("	if (val.value == '*NOT UNDERSTOOD*') {\n");
 		sb.append("		val.value = '';\n");
 		sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + NOT_UNDERSTOOD_F_ICON + "';\n");
 		sb.append("	} else {\n");
 		sb.append("		val.value = '*NOT UNDERSTOOD*';\n");
 		sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + REFUSED_F_ICON + "';\n");
 		sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + UNKNOWN_F_ICON + "';\n");
 		sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + NOT_UNDERSTOOD_T_ICON + "';\n");
 		sb.append("	}\n}\n");
 		sb.append("function help(target) {\n");
 		sb.append("	if (target != null && target.length != 0) { window.open(target,'__HELP__'); }\n");
 		sb.append("}\n");
 		sb.append("function comment(name) {\n");
 		sb.append("	if (!name) name = actionName;\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	var ans = prompt('" + 
 			"Enter a comment for this question" + 
 				"',document.myForm.elements[name + '_COMMENT'].value);\n");
 		sb.append("	if (ans == null) return;\n");
 		sb.append("	document.myForm.elements[name + '_COMMENT'].value = ans;\n");
 		sb.append("	if (ans != null && ans.length > 0) {\n");
 		sb.append("		document.myForm.elements[name + '_COMMENT_ICON'].src = '" + COMMENT_T_ICON + "';\n");
 		sb.append("	} else { document.myForm.elements[name + '_COMMENT_ICON'].src = '" + COMMENT_F_ICON + "'; }\n");
 		sb.append("}\n");
 		sb.append("function setLanguage(lang) {\n");
 		sb.append("	document.myForm.LANGUAGE.value = lang;\n");
 		sb.append("	document.myForm.submit();\n");
 		sb.append("}\n");
 		sb.append("// --> </SCRIPT>\n");
 		
 		return sb.toString();
 	}
 
 	private String header() {
 		StringBuffer sb = new StringBuffer();
 		String title = null;
 		
 		if (isSplashScreen || triceps == null) {
 			title = "Triceps";
 		}
 		else {
 			title = triceps.getTitle();
 		}
 
 		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
 		sb.append("<html>\n");
 		sb.append("<head>\n");
 		sb.append("<META HTTP-EQUIV='Content-Type' CONTENT='text/html;CHARSET=iso-8859-1'>\n");
 		sb.append("<title>" + title + "</title>\n");
 
 		sb.append(createJavaScript());
 		
 		sb.append("</head>\n");
 		sb.append("<body bgcolor='white' onload='javascript:init();'>");
 
 		return sb.toString();
 	}
 	
 	private void setError(String s) {
 		if (s != null)
 			errors.append(s);
 	}
 }
