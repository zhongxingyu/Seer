 /* ******************************************************** 
 ** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
 ** $Header$
 ******************************************************** */ 
 
 package org.dianexus.triceps;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpUtils;
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.util.TreeMap;
 import java.io.File;
 import java.io.Writer;
 import java.util.Vector;
 import java.util.Iterator;
 import java.util.Enumeration;
 import java.io.StringWriter;
 
 
 public class TricepsEngine implements VersionIF {
 	static final String USER_AGENT = "User-Agent";
 	static final String ACCEPT_LANGUAGE = "Accept-Language";
 	static final String ACCEPT_CHARSET = "Accept-Charset";
 	
 	static final int BROWSER_MSIE = 1;
 	static final int BROWSER_NS = 2;
 	static final int BROWSER_OTHER = 0;
 	private int browserType = BROWSER_OTHER;
 
 	private Logger errors = new Logger();
 	private Logger info = new Logger();
 
 	private HttpServletRequest req;
 	private HttpServletResponse res;
 	private String firstFocus = null;
 
 	private String scheduleSrcDir = "";
 	private String workingFilesDir = "";
 	private String completedFilesDir = "";
 	private String imageFilesDir = "";
 	private String logoIcon = "";
 	private String floppyDir = "";
 	private String helpURL = "";
 	private String activePrefix = "";
 	private String activeSuffix = "";
 	private String inactivePrefix = "";
 	private String inactiveSuffix = "";
 
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
 	private boolean allowRecordEvents = false;
 	private boolean allowRefused = false;
 	private boolean allowUnknown = false;
 	private boolean allowNotUnderstood = false;
 	private boolean allowLanguageSwitching = false;
 	private boolean allowJumpTo = false;
 
 	private String directive = null;	// the default
 	private Triceps triceps = Triceps.NULL;
 	private Schedule schedule = Schedule.NULL;	// triceps.getSchedule()
 	private boolean isloaded = false;
 
 	public TricepsEngine(ServletConfig config) {
 		init(config);
 		isloaded = getNewTricepsInstance(null);
 	}
 		 
 	public void init(ServletConfig config) {
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
 	}
 	
 	/*public*/ String getIcon(int which) {
 		return schedule.getReserved(Schedule.IMAGE_FILES_DIR) + schedule.getReserved(which);
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
 		try {
 			this.req = req;
 			this.res = res;
 			XmlString form = null;
 			firstFocus = null; // reset it each time
 			
 			res.setContentType("text/html");
 			PrintWriter out = res.getWriter();
 
 			directive = req.getParameter("DIRECTIVE");	// XXX: directive must be set before calling processHidden
 //if (DEBUG) Logger.writeln("##directive='" + directive + "'");			
 			if (directive != null && directive.trim().length() == 0) {
 				directive = null;
 			}
 if (DEPLOYABLE) {
 			triceps.processEventTimings(req.getParameter("EVENT_TIMINGS"));
 			triceps.receivedResponseFromUser();
 }			
 
 			setGlobalVariables();
 
 			processPreFormDirectives();
 			processHidden();
 
 			form = new XmlString(triceps, createForm());
 
 			out.println(header());	// must be processed AFTER createForm, otherwise setFocus() doesn't work
 			new XmlString(triceps, getCustomHeader(),out);
 
 			if (info.size() > 0) {
 				out.println("<b>");
 				new XmlString(triceps, info.toString(),out);
 				out.println("</b><hr>");
 			}
 			if (errors.size() > 0) {
 				out.println("<b>");
 				new XmlString(triceps, errors.toString(),out);
 				out.println("</b><hr>");
 			}
 
 			if (form.hasErrors() && developerMode) {
 if (AUTHORABLE)	new XmlString(triceps, "<b>" + form.getErrors() + "</b>",out);
 			}
 
 			out.println(form.toString());
 
 			if (!isSplashScreen) {
 				new XmlString(triceps, generateDebugInfo(),out);
 			}
 			
 			triceps.sentRequestToUser();	// XXX when should this be set? before, during, or near end of writing to out buffer?
 			
 if (DEBUG && XML) cocoonXML();			
 
 			out.println(footer());	// should not be parsed
 			out.flush();
 			out.close();
 		}
 		catch (Throwable t) {
 if (DEBUG) Logger.writeln("##Throwable @ Servlet.doPost()" + t.getMessage());
 			Logger.writeln("##" + triceps.get("unexpected_error") + t.getMessage());
 			Logger.printStackTrace(t);
 		}
 	}
 
 	private void processPreFormDirectives() {
 		/* setting language doesn't use directive parameter */
 		if (triceps.isValid()) {
 			String language = req.getParameter("LANGUAGE");
 			if (language != null && language.trim().length() > 0) {
 				triceps.setLanguage(language.trim());
 				directive = "refresh current";
 			}
 		}
 		else {
 			return;
 		}
 
 if (AUTHORABLE) {
 		/* Want to evaluate expression before doing rest so can see results of changing global variable values */
 		if (directive != null && directive.equals("evaluate_expr")) {
 			String expr = req.getParameter("evaluate_expr_data");
 			if (expr != null) {
 				Datum datum = triceps.evaluateExpr(expr);
 
 				errors.print("<table width='100%' cellpadding='2' cellspacing='1' border='1'>");
 				errors.print("<tr><td>Equation</td><td><b>" + expr + "</b></td><td>Type</td><td><b>" + datum.getTypeName() + "</b></td></tr>");
 				errors.print("<tr><td>String</td><td><b>" + datum.stringVal(true) +
 					"</b></td><td>boolean</td><td><b>" + datum.booleanVal() +
 					"</b></td></tr>" + "<tr><td>double</td><td><b>" +
 					datum.doubleVal() + "</b></td><td>&nbsp;</td><td>&nbsp;</td></tr>");
 				errors.print("<tr><td>date</td><td><b>" + datum.dateVal() + "</b></td><td>month</td><td><b>" + datum.monthVal() + "</b></td></tr>");
 				errors.print("</table>");
 
 				errors.print(triceps.getParser().getErrors());
 			}
 			else {
 				errors.println("empty expression");
 			}
 		}
 }
 	}
 
 	private void setGlobalVariables() {
 		whichBrowser();
 		if (triceps.isValid()) {
 			debugMode = schedule.getBooleanReserved(Schedule.DEBUG_MODE);
 			developerMode = schedule.getBooleanReserved(Schedule.DEVELOPER_MODE);
 			showQuestionNum = schedule.getBooleanReserved(Schedule.SHOW_QUESTION_REF);
 			showAdminModeIcons = schedule.getBooleanReserved(Schedule.SHOW_ADMIN_ICONS);
 			autogenOptionNums = schedule.getBooleanReserved(Schedule.AUTOGEN_OPTION_NUM);
 			allowComments = schedule.getBooleanReserved(Schedule.ALLOW_COMMENTS);
 			allowRecordEvents = schedule.getBooleanReserved(Schedule.RECORD_EVENTS);
 			allowRefused = schedule.getBooleanReserved(Schedule.ALLOW_REFUSED);
 			allowUnknown = schedule.getBooleanReserved(Schedule.ALLOW_UNKNOWN);
 			allowNotUnderstood = schedule.getBooleanReserved(Schedule.ALLOW_DONT_UNDERSTAND);
 			allowLanguageSwitching = schedule.getBooleanReserved(Schedule.ALLOW_LANGUAGE_SWITCHING);
 			allowJumpTo = schedule.getBooleanReserved(Schedule.ALLOW_JUMP_TO);
 		}
 		else {
 			debugMode = false;
 			developerMode = false;
 			showQuestionNum = false;
 			showAdminModeIcons = false;
 			autogenOptionNums = true;
 			allowComments = false;
 			allowRecordEvents = true;	// so captures info about opening screens
 			allowRefused = false;
 			allowUnknown = false;
 			allowNotUnderstood = false;
 			allowLanguageSwitching = false;
 			allowJumpTo = false;
 		}
 		allowEasyBypass = false;
 		okPasswordForTempAdminMode = false;
 		okToShowAdminModeIcons = false;
 		isSplashScreen = false;
 		activePrefix = schedule.getReserved(Schedule.ACTIVE_BUTTON_PREFIX);
 		activeSuffix = schedule.getReserved(Schedule.ACTIVE_BUTTON_SUFFIX);
 		inactivePrefix = spaces(activePrefix);
 		inactiveSuffix = spaces(activeSuffix);
 	}
 	
 	private String spaces(String src) {
 		StringBuffer sb = new StringBuffer();
 		if (src == null)
 			return "";
 		for (int i=0;i<src.length();++i) {
 			sb.append("  ");
 		}
 		return sb.toString();
 	}
 
 	private void processHidden() {
 		/* Has side-effects - so must occur before createForm() */
 		if (!triceps.isValid())
 			return;
 
 		String settingAdminMode = req.getParameter("PASSWORD_FOR_ADMIN_MODE");
 		if (settingAdminMode != null && settingAdminMode.trim().length()>0) {
 			/* if try to enter a password, make sure that doesn't reset the form if password fails */
 			String passwd = triceps.getPasswordForAdminMode();
 			if (passwd != null) {
 				if (passwd.trim().equals(settingAdminMode.trim())) {
 					okToShowAdminModeIcons = true;	// so allow AdminModeIcons to be displayed
 				}
 				else {
 					info.println(triceps.get("incorrect_password_for_admin_mode"));
 				}
 			}
 			directive = "refresh current";	// so that will set the admin mode password
 		}
 
 		if (triceps.isTempPassword(req.getParameter("TEMP_ADMIN_MODE_PASSWORD"))) {
 			// enables the password for this session only
 			okPasswordForTempAdminMode = true;	// allow AdminModeIcon values to be accepted
 		}
 
 if (AUTHORABLE) {
 		/** Process requests to change developerMode-type status **/
 		if (directive != null) {
 			/* Toggle these values, as requested */
 			if (directive.equals("turn_developerMode")) {
 				developerMode = !developerMode;
 				schedule.setReserved(Schedule.DEVELOPER_MODE, String.valueOf(developerMode));
 				directive = "refresh current";
 			}
 			else if (directive.equals("turn_debugMode")) {
 				debugMode = !debugMode;
 				schedule.setReserved(Schedule.DEBUG_MODE, String.valueOf(debugMode));
 				directive = "refresh current";
 			}
 			else if (directive.equals("turn_showQuestionNum")) {
 				showQuestionNum = !showQuestionNum;
 				schedule.setReserved(Schedule.SHOW_QUESTION_REF, String.valueOf(showQuestionNum));
 				directive = "refresh current";
 			}
 			else if (directive.equals("sign_schedule")) {
 				String name = schedule.signAndSaveAsJar();
 				if (name != null) {
 					errors.println(triceps.get("signed_schedule_saved_as") + name);
 				}
 				else {
 					errors.println(triceps.get("unable_to_save_signed_schedule"));
 				}
 				directive = "refresh current";
 			}
 		}
 }
 	}
 
 	private String getCustomHeader() {
 		StringBuffer sb = new StringBuffer();
 
 		sb.append("<table border='0' cellpadding='0' cellspacing='3' width='100%'>");
 		sb.append("<tr>");
 		sb.append("<td width='1%'>");
 
 		String logo = (!isSplashScreen && triceps.isValid()) ? triceps.getIcon() : logoIcon;
 		if (logo.trim().length()==0) {
 			sb.append("&nbsp;");
 		}
 		else {
 			sb.append("<img name='icon' src='" + (imageFilesDir + logo) + "' align='top' border='0'" +
 				((!isSplashScreen) ? " onMouseUp='evHandler(event);setAdminModePassword();'":"") +
 				((!isSplashScreen) ? (" alt='" + triceps.get("LogoMessage") + "'") : "") +
 				">");
 		}
 		sb.append("	</td>");
 		sb.append("	<td align='left'><font SIZE='4'>" + ((triceps.isValid() && !isSplashScreen) ? triceps.getHeaderMsg() : LICENSE_MSG) + "</font></td>");
 
 		String globalHelp = null;
 		if (triceps.isValid() && !isSplashScreen) {
 			globalHelp = schedule.getReserved(Schedule.SCHED_HELP_URL);
 		}
 		else {
 			globalHelp = helpURL;
 		}
 
 		sb.append("	<td width='1%'>");
 		if (globalHelp != null && globalHelp.trim().length() != 0) {
 			sb.append("<img src='" + getIcon(Schedule.HELP_ICON) + "' alt='" + triceps.get("Help") + "' align='top' border='0' onMouseUp='evHandler(event);help(\"_TOP_\",\"" + globalHelp + "\");'>");
 		}
 		else {
 			sb.append("&nbsp;");
 		}
 		sb.append("</td></tr>");
 		sb.append("</table>");
 //		sb.append("<hr>");
 
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
 			ScheduleList interviews = new ScheduleList(triceps, dir);
 
 			if (interviews.hasErrors()) {
 				errors.println(triceps.get("error_getting_list_of_available_interviews"));
 				errors.print(interviews.getErrors());
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
 								title = defaultTitle + " (# " + count + ")";
 							}
 							else {
 								break;
 							}
 						}
 					}
 					catch (Throwable t) {
 if (DEBUG) Logger.writeln("##Throwable @ Servlet.getSortedNames()" + t.getMessage());
 						errors.println(triceps.get("unexpected_error") + t.getMessage());
 						Logger.printStackTrace(t);
 					}
 				}
 			}
 		}
 		catch (Throwable t) {
 if (DEBUG) Logger.writeln("##Throwable @ Servlet.getSortedNames()" + t.getMessage());
 			errors.println(triceps.get("unexpected_error") + t.getMessage());
 			Logger.printStackTrace(t);
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
 			Vector v = sched.getLanguages();
 			if (v.size() > 1) {
 				sb.append("[");
 				for (int i=0;i<v.size();++i) {
 					sb.append((String) v.elementAt(i));
 					if (i != v.size()-1) {
 						sb.append(",");
 					}
 				}
 				sb.append("]");
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
 if (DEBUG) Logger.writeln("##Throwable @ Servlet.selectFromInterviewsInDir" + t.getMessage());
 			errors.println(triceps.get("error_building_sorted_list_of_interviews") + t.getMessage());
 			Logger.printStackTrace(t);
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
 		
 		if ("finished".equals(directive)) {
 			return formStr;
 		}
 
 		sb.append(languageButtons());
 
 		sb.append(formStr);
 		
 		sb.append("<input type='hidden' name='PASSWORD_FOR_ADMIN_MODE' value=''>");	// must manually bypass each time
 		sb.append("<input type='hidden' name='LANGUAGE' value=''>");
 if (DEPLOYABLE) {		
 		sb.append("<input type='hidden' name='EVENT_TIMINGS' value=''>");	// list of event timings
 }		
 		if (directive == null || directive.equals("select_new_interview")) {
 			sb.append("<input type='hidden' name='DIRECTIVE' value='START'>");	// so that ENTER tries to go next, and will be trapped if needed
 		}
 		else {
 			sb.append("<input type='hidden' name='DIRECTIVE' value='next'>");	
 		}
 		
 
 		sb.append("</FORM>");
 
 		return sb.toString();
 	}
 
 	private String languageButtons() {
 		if (isSplashScreen || !triceps.isValid() || !allowLanguageSwitching)
 			return "";
 
 		StringBuffer sb = new StringBuffer();
 
 		/* language switching section */
 //		if (!isSplashScreen && triceps.isValid()) {
 		if (triceps.isValid()) {
 			Vector languages = schedule.getLanguages();
 			if (languages.size() > 1) {
 				sb.append("<table width='100%' border='0'><tr><td align='center'>");
 				for (int i=0;i<languages.size();++i) {
 					String language = (String) languages.elementAt(i);
 					boolean selected = (i == triceps.getLanguage());
 					sb.append(((selected) ? "\n<u>" : "") +
 						"<input type='button' onClick='evHandler(event);setLanguage(\"" + language + "\");' name='select_" + language + "' value='" + language + "'>" +
 						((selected) ? "</u>" : ""));
 				}
 				sb.append("</td></tr></table>");
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
 		if (directive == null || directive.equals("select_new_interview")) {
 			/* Construct splash screen */
 			isSplashScreen = true;
 			triceps.setLanguage(null);	// the default
 
 			sb.append("<table cellpadding='2' cellspacing='2' border='1'>");
 			sb.append("<tr><td>" + triceps.get("please_select_an_interview") + "</td>");
 			sb.append("<td>");
 
 			/* Build the list of available interviews */
 			sb.append(selectFromInterviewsInDir("schedule",scheduleSrcDir,false));
 
 			sb.append("</td><td>");
 			sb.append(buildSubmit("START"));
 			sb.append("</td></tr>");
 
 			/* Build the list of suspended interviews */
 			sb.append("<tr><td>");
 			sb.append(triceps.get("or_restore_an_interview_in_progress"));
 			sb.append("</td><td>");
 
 			sb.append(selectFromInterviewsInDir("RestoreSuspended",workingFilesDir,true));
 
 			sb.append("</td><td>");
 			sb.append(buildSubmit("RESTORE"));
 			sb.append("</td></tr></table>");
 
 			return sb.toString();
 		}
 		else if (directive.equals("START")) {
 			// load schedule
 			ok = getNewTricepsInstance(req.getParameter("schedule"));
 
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
 
 			restore = req.getParameter("RestoreSuspended");
 			if (restore == null || restore.trim().length()==0) {
 				directive = null;
 				return processDirective();
 			}
 
 
 			// load schedule
 			ok = getNewTricepsInstance(restore);
 
 			if (!ok) {
 				directive = null;
 
 				errors.println(triceps.get("unable_to_find_or_access_schedule") + " '" + restore + "'");
 				return processDirective();
 			}
 			// re-check developerMode options - they aren't set via the hidden options, since a new copy of Triceps created
 			setGlobalVariables();
 
 			ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
 
 			// ask question
 		}
 		else if (directive.equals("jump_to")) {
 			if ((AUTHORABLE && developerMode) || allowJumpTo) {
 				gotoMsg = triceps.gotoNode(req.getParameter("jump_to_data"));
 				ok = (gotoMsg == Triceps.OK);
 				// ask this question
 			}
 		}
 		else if ("refresh current".equals(directive)) {
 			ok = true;
 			// re-ask the current question
 		}
 		else if (directive.equals("restart_clean")) { // restart from scratch
 if (AUTHORABLE) {
 			triceps.resetEvidence();
 			ok = ((gotoMsg = triceps.gotoFirst()) == Triceps.OK);	// don't proceed if prior error
 			// ask first question
 }
 		}
 		else if (directive.equals("reload_questions")) { // debugging option
 if (AUTHORABLE) {
 			ok = triceps.reloadSchedule();
 			if (ok) {
 				info.println(triceps.get("schedule_restored_successfully"));
 			}
 			// re-ask current question
 }
 		}
 		else if (directive.equals("save_to")) {
 if (AUTHORABLE) {
 			String name = req.getParameter("save_to_data");
 //			ok = triceps.saveWorkingInfo(name);
 			if (ok) {
 				info.println(triceps.get("interview_saved_successfully_as") + (workingFilesDir + name));
 			}
 }
 		}
 		else if (directive.equals("show_Syntax_Errors")) {
 if (AUTHORABLE) {
 			Vector pes = triceps.collectParseErrors();
 
 			if (pes == null || pes.size() == 0) {
 				info.println(triceps.get("no_syntax_errors_found"));
 			}
 			else {
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
 						errors.print("<font color='red'>" +
 							triceps.get("The_following_syntax_errors_were_found") + (n.getSourceFile()) + "</font>");
 						errors.print("<table cellpadding='2' cellspacing='1' width='100%' border='1'>");
 						errors.print("<tr><td>line#</td><td>name</td><td>Dependencies</td><td><b>Dependency Errors</b></td><td>Action Type</td><td>Action</td><td><b>Action Errors</b></td><td><b>Node Errors</b></td><td><b>Naming Errors</b></td><td><b>AnswerChoices Errors</b></td><td><b>Readback Errors</b></td></tr>");
 					}
 
 					errors.print("<tr><td>" + n.getSourceLine() + "</td><td>" + (n.getLocalName()) + "</td>");
 					errors.print("<td>" + n.getDependencies() + "</td><td>");
 
 					errors.print(pe.hasDependenciesErrors() ? ("<font color='red'>" + pe.getDependenciesErrors() + "</font>") : "&nbsp;");
 					errors.print("</td><td>" + Node.ACTION_TYPES[n.getQuestionOrEvalType()] + "</td><td>" + n.getQuestionOrEval() + "</td><td>");
 
 					errors.print(pe.hasQuestionOrEvalErrors() ? ("<font color='red'>" + pe.getQuestionOrEvalErrors() + "</font>") : "&nbsp;");
 					errors.print("</td><td>");
 
 					if (!pe.hasNodeParseErrors()) {
 						errors.print("&nbsp;");
 					}
 					else {
 						errors.print("<font color='red'>" + pe.getNodeParseErrors() + "</font>");
 					}
 					errors.print("</td><td>");
 
 					if (!pe.hasNodeNamingErrors()) {
 						errors.print("&nbsp;");
 					}
 					else {
 						errors.print("<font color='red'>" + pe.getNodeNamingErrors() + "</font>");
 					}
 
 					errors.print("<td>" + ((pe.hasAnswerChoicesErrors()) ? ("<font color='red'>" + pe.getAnswerChoicesErrors() + "</font>"): "&nbsp;") + "</td>");
 					errors.print("<td>" + ((pe.hasReadbackErrors()) ? ("<font color='red'>" + pe.getReadbackErrors() + "</font>") : "&nbsp;") + "</td>");
 
 					errors.print("</tr>");
 				}
 				errors.print("</table><hr>");
 			}
 			if (schedule.hasErrors()) {
 				errors.print("<font color='red'>" +
 					triceps.get("The_following_flow_errors_were_found") + "</font>");
 				errors.print("<table cellpadding='2' cellspacing='1' width='100%' border='1'><tr><td>");
 				errors.print("<font color='red'>" + schedule.getErrors() + "</font>");
 				errors.print("</td></tr></table>");
 			}
 			if (triceps.getEvidence().hasErrors()) {
 				errors.print("<font color='red'>" +
 					triceps.get("The_following_data_access_errors_were") + "</font>");
 				errors.print("<table cellpadding='2' cellspacing='1' width='100%' border='1'><tr><td>");
 				errors.print("<font color='red'>" + triceps.getEvidence().getErrors() + "</font>");
 				errors.print("</td></tr></table>");
 			}
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
 				directive = "finished";
 				return processDirective();
 			}
 
 			// don't goto next if errors
 			// ask question
 		}
 		else if (directive.equals("finished")) {
 			// save the file, but still give the option to go back and change answers
 			String savedName = null;
 
 			info.println(triceps.get("the_interview_is_completed"));
 			if (DEPLOYABLE) {
 				savedName = triceps.saveCompletedInfo();
 				/*
 				if (savedName == null) {
 					info.println(triceps.get("interview_saved_successfully_as") + savedName);
 				}
 				*/
 	
 				savedName = triceps.copyCompletedToFloppy();
 				/*
 				if (savedName != null) {
 					info.println(triceps.get("interview_saved_successfully_as") + savedName);
 				}
 				*/
 			}
 			return sb.toString();
 		}
 		else if (directive.equals("previous")) {
 			// don't store current
 			// goto previous
 			gotoMsg = triceps.gotoPrevious();
 			ok = ok && (gotoMsg == Triceps.OK);
 			// ask question
 		}
 
 
 		/* Show any accumulated errors */
 		if (triceps.hasErrors()) {
 			errors.print(triceps.getErrors());
 		}
 
 		nodes = triceps.getQuestions();
 		int errCount = 0;
 		while (nodes.hasMoreElements()) {
 			Node n = (Node) nodes.nextElement();
 			if (n.hasRuntimeErrors()) {
 				if (++errCount == 1) {
 					info.println(triceps.get("please_answer_the_questions_listed_in") + "<font color='red'>" + triceps.get("RED") + "</font>" + triceps.get("before_proceeding"));
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
 			firstFocus = "next";	// try to focus on Next button if nothing else available
 		}
 
 		firstFocus = (new XmlString(triceps, firstFocus)).toString();	// make sure properly formatted
 
 		sb.append(queryUser());
 		
 		return sb.toString();
 	}
 
 	private boolean getNewTricepsInstance(String name) {
 		if (triceps != null) {
 			triceps.closeDataLogger();
 		}
 		
 		if (name == null || name.trim().length() == 0) {
 			triceps = Triceps.NULL;
 		}
 		else {
 			triceps = new Triceps(name,workingFilesDir,completedFilesDir,floppyDir);
 		}
 		if (!AUTHORABLE && !triceps.getSchedule().isLoaded()) {
 			triceps = Triceps.NULL;
 		}
 		schedule = triceps.getSchedule();
 		schedule.setReserved(Schedule.IMAGE_FILES_DIR,imageFilesDir);
 		schedule.setReserved(Schedule.SCHEDULE_DIR,scheduleSrcDir);
 		return triceps.isValid();
 	}
 	
 	private String nodesXML() {
 		StringBuffer sb = new StringBuffer();
 if (XML) {
 		Enumeration questionNames = triceps.getQuestions();
 		
 		sb.append("<nodes allowComment=\"1\" allowRefused=\"1\" allowUnknown=\"1\" allowHuh=\"1\">\n");
 		for(int count=0;questionNames.hasMoreElements();++count) {
 			Node node = (Node) questionNames.nextElement();
 			Datum datum = triceps.getDatum(node);
 			
 			sb.append(node.toXML(datum,autogenOptionNums));	
 			
 		}
 		sb.append("</nodes>\n");
 }		
 		return sb.toString();
 	}
 	
 	private String metadataXML() {
 		StringBuffer sb = new StringBuffer();
 if (XML) {		
 		sb.append("<icons>\n");
 		sb.append("	<icon name=\"comment_on\">" + getIcon(Schedule.COMMENT_ICON_ON) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Add_a_Comment")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"comment_off\">" + getIcon(Schedule.COMMENT_ICON_OFF) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Add_a_Comment")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"refused_on\">" + getIcon(Schedule.REFUSED_ICON_ON) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Set_as_Refused")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"refused_off\">" + getIcon(Schedule.REFUSED_ICON_OFF) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Set_as_Refused")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"unknown_on\">" + getIcon(Schedule.UNKNOWN_ICON_ON) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Set_as_Unknown")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"unknown_off\">" + getIcon(Schedule.UNKNOWN_ICON_OFF) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Set_as_Unknown")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"not_understood_on\">" + getIcon(Schedule.DONT_UNDERSTAND_ICON_ON) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Set_as_Not_Understood")) + "\"/></icon>\n");
 		sb.append("	<icon name=\"not_understood_off\">" + getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Set_as_Not_Understood")) + "\"/></icon>\n");
 		sb.append("</icons>\n");
 		
 		sb.append("<navigation>\n");		
 		if (isSplashScreen) {
 			sb.append(actionXML("START","button",triceps.get("START"),null));
 			sb.append(actionXML("RESTORE","button",triceps.get("RESTORE"),null));
 		}
 		else {
 			/* hidden variables */
 			sb.append(actionXML("PASSWORD_FOR_ADMIN_MODE","hidden","",null));
 			sb.append(actionXML("LANGUAGE","hidden","",null));
 			sb.append(actionXML("DIRECTIVE","hidden","next",null));
 			
 			if (allowEasyBypass || okToShowAdminModeIcons) {
 				sb.append(actionXML("TEMP_ADMIN_MODE_PASSWORD","hidden",triceps.createTempPassword(),null));
 			}
 			
 			if (DEPLOYABLE) {
 				sb.append(actionXML("EVENT_TIMINGS","hidden","",null));
 			}
 			
 			if (!triceps.isAtEnd()) {
 				sb.append(actionXML("next","button",triceps.get("next"),null));
 			}
 			if (!triceps.isAtBeginning()) {
 				sb.append(actionXML("previous","button",triceps.get("previous"),null));
 			}
 			
 			if (allowJumpTo || (developerMode && AUTHORABLE)) {
 				sb.append(actionXML("jump_to","textGo",triceps.get("jump_to"),null));	// should build a jump_to button and jump_to_data text field
 			}
 		}
 		sb.append("</navigation>\n");
 		
 		sb.append("<admin>\n");
 		sb.append("	<icon name=\"help\">" + getIcon(Schedule.HELP_ICON) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("Help")) + "\"/></icon>\n");
		sb.append("	<icon name=\"logo\">" + ((!isSplashScreen && triceps.isValid()) ? triceps.getIcon() : logoIcon) + "<alt value=\"" + XMLAttrEncoder.encode(triceps.get("LogoMessage")) + "\"/></icon>\n");
 		
 		if (AUTHORABLE && developerMode) {
 			sb.append(actionXML("select_new_interview","button",triceps.get("select_new_interview"),null));
 			sb.append(actionXML("restart_clean","button",triceps.get("restart_clean"),null));
 			sb.append(actionXML("reload_questions","button",triceps.get("reload_questions"),null));
 			sb.append(actionXML("show_Syntax_Errors","button",triceps.get("show_Syntax_Errors"),null));
 			
 			sb.append(actionXML("turn_developerMode","button",triceps.get("turn_developerMode"),null));
 			sb.append(actionXML("turn_debugMode","button",triceps.get("turn_debugMode"),null));
 			sb.append(actionXML("turn_showQuestionNum","button",triceps.get("turn_showQuestionNum"),null));
 			sb.append(actionXML("sign_schedule","button",triceps.get("sign_schedule"),null));
 			
 			sb.append(actionXML("save_to","textGo",triceps.get("save_to"),null));	
 			sb.append(actionXML("evaluate_expr","textGo",triceps.get("evaluate_expr"),null));	
 		}
 		sb.append("</admin>\n");
 }		
 		return sb.toString();
 	}
 	
 	private String actionXML(String name, String type, String value, String on) {
 		StringBuffer sb = new StringBuffer();
 if (XML) {
 		sb.append("	<act type=\"");
 		sb.append(type);
 		sb.append("\" name=\"");
 		sb.append(name);
 		sb.append("\" value=\"");
 		sb.append(XMLAttrEncoder.encode(value));
 		if (on != null) {
 			sb.append("\" on=\"");
 			sb.append(on);
 		}
 		sb.append("\"/>\n");
 }		
 		return sb.toString();
 	}
 	
 	private void whichBrowser() {
 		String userAgent = req.getHeader(USER_AGENT);
 		if (userAgent.indexOf("MSIE") != -1) {
 			browserType = BROWSER_MSIE;
 		}
 		else if (userAgent.indexOf("Mozilla") != -1) {
 			browserType = BROWSER_NS;
 		}
 		else {
 			browserType = BROWSER_OTHER;
 		}
 	}
 
 	private String responseXML() {
 		StringBuffer sb = new StringBuffer();
 if (XML) {		
 		String browser = null;
 		if (browserType == BROWSER_MSIE) {
 			browser = "MSIE";
 		}
 		else if (browserType == BROWSER_NS) {
 			browser = "NS";
 		}
 		else {
 			browser = "other";
 		}
 		
 		String title=null;
 		if (isSplashScreen || !triceps.isValid()) {
 			title = VERSION_NAME;
 		}
 		else {
 			title = triceps.getTitle();
 		}
 				
 		sb.append("<triceps lang=\"");
 		sb.append(req.getHeader(ACCEPT_LANGUAGE));
 		sb.append("\" charset=\"");
 		sb.append(req.getHeader(ACCEPT_CHARSET));
 		sb.append("\" target=\"");
 		sb.append(HttpUtils.getRequestURL(req));
 		sb.append("\" firstFocus=\"");
 		sb.append(firstFocus);
 		sb.append("\" title=\"");
 		sb.append(XMLAttrEncoder.encode(title));
 		
 
 		sb.append("\">\n	<script browser=\"");
 		sb.append(browser);
 		sb.append("\"/>\n");
 		sb.append(headerXML());
 		sb.append(languagesXML());
 		sb.append(nodesXML());
 		sb.append(metadataXML());
 		sb.append("</triceps>");
 }		
 		return sb.toString();
 	}
 	
 	private String languagesXML() {
 		StringBuffer sb = new StringBuffer();
 if (XML) {
 		sb.append("<languages>\n");
 
 		/* languages */
 		Vector languages = schedule.getLanguages();
 		for (int i=0;i<languages.size();++i) {
 			String language = (String) languages.elementAt(i);
 			boolean selected = (i == triceps.getLanguage());
 			sb.append("	<language name=\"select_" + language + "\" value=\"" +  language + "\" on=\"" + ((selected) ? "1" : "0") + "\"/>\n");
 		}
 		sb.append("</languages>\n");
 }
 		return sb.toString();		
 	}
 	
 	private String headerXML() {
 		StringBuffer sb = new StringBuffer();
 if (XML) {
 		String headerMsg = ((triceps.isValid() && !isSplashScreen) ? triceps.getHeaderMsg() : LICENSE_MSG);
 		sb.append("<header>\n");
 		sb.append((new XmlString(triceps, headerMsg)).toString());
 		sb.append("</header>\n");
 }		
 		return sb.toString();
 	}
 	
 	
 	public void cocoonXML() {
 		StringBuffer result = new StringBuffer();
 if (XML) {
 		result.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
 		result.append("<?xml-stylesheet href=\"triceps.xsl\" type=\"text/xsl\"?>\n");
 		result.append("<?cocoon-process type=\"xslt\"?>\n");
 			
 		String file = "/usr/local/triceps/webapps/samples/test5.xml";
 		try {
 			result.append(responseXML());
 			
 			java.io.FileWriter fw = new java.io.FileWriter(file,false);
 			fw.write(result.toString());
 			fw.close();
 		}
 		catch (IOException e) {
 			Logger.writeln("#*#Unable to create or write to file " + file);
 		}
 }		
 	}
 	
 	
 	/**
 	 * This method assembles the displayed question and answer options
 	 * and formats them in HTML for return to the client browser.
 	 */
 	private String queryUser() {
 		// if parser internal to Schedule, should have method access it, not directly
 		StringBuffer sb = new StringBuffer();
 
 		if (!triceps.isValid())
 			return "";
 
 		if (debugMode && developerMode && AUTHORABLE) {
 			sb.append(triceps.get("QUESTION_AREA"));
 		}
 
 		Enumeration questionNames = triceps.getQuestions();
 		String color;
 		String errMsg;
 
 		sb.append("<table cellpadding='2' cellspacing='1' width='100%' border='1'>");
 		for(int count=0;questionNames.hasMoreElements();++count) {
 			Node node = (Node) questionNames.nextElement();
 			Datum datum = triceps.getDatum(node);
 
 			if (node.hasRuntimeErrors()) {
 				color = " color='red'";
 				StringBuffer errStr = new StringBuffer("<font color='red'>");
 				errStr.append(node.getRuntimeErrors());
 				errStr.append("</font>");
 				errMsg = errStr.toString();
 			}
 			else {
 				color = null;
 				errMsg = "";
 			}
 
 			sb.append("<tr>");
 
 			if (showQuestionNum) {
 				if (color != null) {
 					sb.append("<td><font" + color + "><b>" + node.getExternalName() + "</b></font></td>");
 				}
 				else {
 					sb.append("<td>" + node.getExternalName() + "</td>");
 				}
 			}
 
 			String inputName = node.getLocalName();
 
 			boolean isSpecial = (datum.isRefused() || datum.isUnknown() || datum.isNotUnderstood());
 			allowEasyBypass = allowEasyBypass || isSpecial;	// if a value has already been refused, make it easy to re-refuse it
 
 			String clickableOptions = buildClickableOptions(node,inputName,isSpecial);
 
 			switch(node.getAnswerType()) {
 				case Node.RADIO_HORIZONTAL:
 					sb.append("<td colspan='3'>");
 					sb.append("<input type='hidden' name='" + (inputName + "_COMMENT") + "' value='" + XMLAttrEncoder.encode(node.getComment()) + "'>");
 					sb.append("<input type='hidden' name='" + (inputName + "_SPECIAL") + "' value='" +
 						((isSpecial) ? (triceps.toString(node,true)) : "") +
 						"'>");
 					sb.append("<input type='hidden' name='" + (inputName + "_HELP") + "' value='" + node.getHelpURL() + "'>");
 					if (color != null) {
 						sb.append("<font" + color + ">" + triceps.getQuestionStr(node) + "</font>");
 					}
 					else {
 						sb.append(triceps.getQuestionStr(node));
 					}
 					sb.append("</td></tr><tr>");
 
 					if (showQuestionNum) {
 						sb.append("<td>&nbsp;</td>");
 					}
 					sb.append("<td colspan='2' bgcolor='lightgrey'>");
 					sb.append(node.prepareChoicesAsHTML(datum,errMsg,autogenOptionNums));
 					sb.append(errMsg);
 					sb.append("</td>");
 					sb.append("<td width='1%' NOWRAP>" + clickableOptions + "</td>");
 					break;
 				default:
 					if (node.getAnswerType() == Node.NOTHING) {
 						sb.append("<td colspan='2'>");
 					}
 					else {
 						sb.append("<td>");
 					}
 					sb.append("<input type='hidden' name='" + (inputName + "_COMMENT") + "' value='" + XMLAttrEncoder.encode(node.getComment()) + "'>");
 					sb.append("<input type='hidden' name='" + (inputName + "_SPECIAL") + "' value='" +
 						((isSpecial) ? (triceps.toString(node,true)) : "") +
 						"'>");
 					sb.append("<input type='hidden' name='" + (inputName + "_HELP") + "' value='" + node.getHelpURL() + "'>");
 					if (color != null) {
 						sb.append("<font" + color + ">" + triceps.getQuestionStr(node) + "</font>");
 					}
 					else {
 						sb.append(triceps.getQuestionStr(node));
 					}
 					if (node.getAnswerType() != Node.NOTHING) {
 						sb.append("<td>" + node.prepareChoicesAsHTML(datum,autogenOptionNums) + errMsg + "</td>");
 					}
 					sb.append("</td><td width='1%' NOWRAP>" + clickableOptions + "</td>");
 					break;
 			}
 
 			sb.append("</tr>");
 		}
 		sb.append("<tr><td colspan='" + ((showQuestionNum) ? 4 : 3) + "' align='center'>");
 
 		if (!triceps.isAtEnd()) {
 			sb.append(buildSubmit("next"));
 		}
 	
 		if (!triceps.isAtBeginning()) {
 			sb.append(buildSubmit("previous"));
 		}
 	
 		if (allowJumpTo || (developerMode && AUTHORABLE)) {
 			sb.append(buildSubmit("jump_to"));
 			sb.append("<input type='text' name='jump_to_data' size='10'>");
 		}
 
 		if (allowEasyBypass || okToShowAdminModeIcons) {
 			/* enables TEMP_ADMIN_MODE going forward for one screen */
 			sb.append("<input type='hidden' name='TEMP_ADMIN_MODE_PASSWORD' value='" + triceps.createTempPassword() + "'>");
 		}
 
 		sb.append("</td></tr>");
 
 if (AUTHORABLE) {
 		if (developerMode) {
 			sb.append("<tr><td colspan='" + ((showQuestionNum) ? 4 : 3 ) + "' align='center'>");
 			sb.append(buildSubmit("select_new_interview"));
 			sb.append(buildSubmit("restart_clean"));
 			sb.append(buildSubmit("save_to"));
 			sb.append("<input type='text' name='save_to_data' size='10'>");
 			sb.append("</td></tr>");
 			sb.append("<tr><td colspan='" + ((showQuestionNum) ? 4 : 3 ) + "' align='center'>");
 			sb.append(buildSubmit("reload_questions"));
 			sb.append(buildSubmit("show_Syntax_Errors"));
 			sb.append(buildSubmit("evaluate_expr"));
 			sb.append("<input type='text' name='evaluate_expr_data'>");
 			sb.append("</td></tr>");
 		}
 }
 
 		sb.append(showOptions());
 
 		sb.append("</table>");
 
 		return sb.toString();
 	}
 	
 		
 	private String buildSubmit(String name) {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append("<input type='submit' name='");
 		sb.append(name);
 		sb.append("' value='");
 		sb.append(inactivePrefix);
 		sb.append(triceps.get(name));
 		sb.append(inactiveSuffix);
 		sb.append("'>");
 		
 		sb.append("<input type='hidden' name='DIRECTIVE_");
 		sb.append(name);
 		sb.append("' value='");
 		sb.append(triceps.get(name));
 		sb.append("'>");
 		return sb.toString();
 	}
 	
 	private String buildClickableOptions(Node node, String inputName, boolean isSpecial) {
 		StringBuffer sb = new StringBuffer();
 
 		if (!triceps.isValid())
 			return "";
 
 		Datum datum = triceps.getDatum(node);
 
 		if (datum == null) {
 			return "&nbsp;";
 		}
 
 		boolean isRefused = false;
 		boolean isUnknown = false;
 		boolean isNotUnderstood = false;
 
 		if (datum.isRefused())
 			isRefused = true;
 		else if (datum.isUnknown())
 			isUnknown = true;
 		else if (datum.isNotUnderstood())
 			isNotUnderstood = true;
 
 		String localHelpURL = node.getHelpURL();
 		if (localHelpURL != null && localHelpURL.trim().length() != 0) {
 			sb.append("<img src='" + getIcon(Schedule.HELP_ICON) +
 				"' align='top' border='0' alt='" + triceps.get("Help") + "' onMouseUp='evHandler(event);help(\"" + inputName + "\",\"" + localHelpURL + "\");'>");
 		}
 		else {
 			// don't show help icon if no help is available?
 		}
 
 		String comment = node.getComment();
 		if (showAdminModeIcons || okToShowAdminModeIcons || allowComments) {
 			if (comment != null && comment.trim().length() != 0) {
 				sb.append("<img name='" + inputName + "_COMMENT_ICON" + "' src='" + getIcon(Schedule.COMMENT_ICON_ON) +
 					"' align='top' border='0' alt='" + triceps.get("Add_a_Comment") + "' onMouseUp='evHandler(event);comment(\"" + inputName + "\");'>");
 			}
 			else  {
 				sb.append("<img name='" + inputName + "_COMMENT_ICON" + "' src='" + getIcon(Schedule.COMMENT_ICON_OFF) +
 					"' align='top' border='0' alt='" + triceps.get("Add_a_Comment") + "' onMouseUp='evHandler(event);comment(\"" + inputName + "\");'>");
 			}
 		}
 
 		/* If something has been set as Refused, Unknown, etc, allow going forward without additional headache */
 		/* Don't want to be able to refuse Nothing nodes, since can be used to prevent advancement */
 
 		if (!(node.getAnswerType() == Node.NOTHING) && (showAdminModeIcons || okToShowAdminModeIcons || isSpecial)) {
 			if (allowRefused || isRefused) {
 				sb.append("<img name='" + inputName + "_REFUSED_ICON" + "' src='" + ((isRefused) ? getIcon(Schedule.REFUSED_ICON_ON) : getIcon(Schedule.REFUSED_ICON_OFF)) +
 					"' align='top' border='0' alt='" + triceps.get("Set_as_Refused") + "' onMouseUp='evHandler(event);markAsRefused(\"" + inputName + "\");'>");
 			}
 			if (allowUnknown || isUnknown) {
 				sb.append("<img name='" + inputName + "_UNKNOWN_ICON" + "' src='" + ((isUnknown) ? getIcon(Schedule.UNKNOWN_ICON_ON) : getIcon(Schedule.UNKNOWN_ICON_OFF)) +
 					"' align='top' border='0' alt='" + triceps.get("Set_as_Unknown") + "' onMouseUp='evHandler(event);markAsUnknown(\"" + inputName + "\");'>");
 			}
 			if (allowNotUnderstood || isNotUnderstood) {
 				sb.append("<img name='" + inputName + "_NOT_UNDERSTOOD_ICON" + "' src='" + ((isNotUnderstood) ? getIcon(Schedule.DONT_UNDERSTAND_ICON_ON) : getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF)) +
 					"' align='top' border='0' alt='" + triceps.get("Set_as_Not_Understood") + "' onMouseUp='evHandler(event);markAsNotUnderstood(\"" + inputName + "\");'>");
 			}
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
 if (AUTHORABLE) {
 		// Complete printout of what's been collected per node
 		if (!triceps.isValid())
 			return "";
 
 		if (developerMode && debugMode) {
 			sb.append("<hr>");
 			sb.append(triceps.get("CURRENT_QUESTIONS"));
 			sb.append("<table cellpadding='2' cellspacing='1'  width='100%' border='1'>");
 			Enumeration questionNames = triceps.getQuestions();
 			Evidence evidence = triceps.getEvidence();
 
 			while(questionNames.hasMoreElements()) {
 				Node n = (Node) questionNames.nextElement();
 				Datum d = evidence.getDatum(n);
 				sb.append("<tr>");
 				sb.append("<td>" + n.getExternalName() + "</td>");
 				if (d.isSpecial()) {
 					sb.append("<td><b><i>" + triceps.toString(n,true) + "</i></b></td>");
 				}
 				else {
 					sb.append("<td><b>" + triceps.toString(n,true) + "</b></td>");
 				}
 				sb.append("<td>" + Datum.getTypeName(triceps,n.getDatumType()) + "</td>");
 				sb.append("<td>" + n.getLocalName() + "</td>");
 				sb.append("<td>" + n.getConcept() + "</td>");
 				sb.append("<td>" + n.getDependencies() + "</td>");
 				sb.append("<td>" + n.getQuestionOrEvalTypeField() + "</td>");
 				sb.append("<td>" + n.getQuestionOrEval() + "</td>");
 				sb.append("</tr>");
 			}
 			sb.append("</table>");
 
 
 			sb.append("<hr>");
 			sb.append(triceps.get("EVIDENCE_AREA"));
 			sb.append("<table cellpadding='2' cellspacing='1'  width='100%' border='1'>");
 
 			for (int i = schedule.size()-1; i >= 0; i--) {
 				Node n = schedule.getNode(i);
 				Datum d = triceps.getDatum(n);
 				if (!triceps.isSet(n))
 					continue;
 				sb.append("<tr>");
 				sb.append("<td>" + (i + 1) + "</td>");
 				sb.append("<td>" + n.getExternalName() + "</td>");
 				if (d.isSpecial()) {
 					sb.append("<td><b><i>" + triceps.toString(n,true) + "</i></b></td>");
 				}
 				else {
 					sb.append("<td><b>" + triceps.toString(n,true) + "</b></td>");
 				}
 				sb.append("<td>" +  Datum.getTypeName(triceps,n.getDatumType()) + "</td>");
 				sb.append("<td>" + n.getLocalName() + "</td>");
 				sb.append("<td>" + n.getConcept() + "</td>");
 				sb.append("<td>" + n.getDependencies() + "</td>");
 				sb.append("<td>" + n.getQuestionOrEvalTypeField() + "</td>");
 				sb.append("<td>" + n.getQuestionOrEval(triceps.getLanguage()) + "</td>");
 				sb.append("</tr>");
 			}
 			sb.append("</table>");
 		}
 }
 		return sb.toString();
 	}
 
 	private String showOptions() {
 if (AUTHORABLE) {
 		if (developerMode) {
 			StringBuffer sb = new StringBuffer();
 
 			sb.append("<tr><td colspan='" + ((showQuestionNum) ? 4 : 3 ) + "' align='center'>");
 			sb.append(buildSubmit("turn_developerMode"));
 			sb.append(buildSubmit("turn_debugMode"));
 			sb.append(buildSubmit("turn_showQuestionNum"));
 			sb.append(buildSubmit("sign_schedule"));
 			sb.append("</td></tr>");
 			return sb.toString();
 		}
 		else
 			return "";
 } else return "";
 	}
 
 	private String createJavaScript() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("<script  language=\"JavaScript1.2\"> <!--\n");
 		
 		sb.append("var val = null;\n");
 		sb.append("var name = null;\n");
 		sb.append("var msg = null;\n");
 		
 		sb.append("var startTime = new Date();\n");
 		sb.append("var el = null;\n");
 		sb.append("var evH = null;\n");
 		sb.append("var ans = null;\n");
 		sb.append("var target = null;\n");
 		
 		sb.append("var Ns4 = false; var Ns5 = false; var Ns6 = false; var Ns4up = false; \n");
 		sb.append("var Ie4 = false; var Ie5 = false; var Ie6 = false; var Ie4up = false; \n");
 		
 		// note the workaround to get Ns6 
 		sb.append("if (navigator.appName.indexOf('Netscape') != -1) { \n");
 		sb.append("  if (navigator.userAgent.indexOf('Netscape6') != -1) Ns6 = true; \n");
 		sb.append("  else if (parseInt(navigator.appVersion) >= 5) Ns5 = true; \n");
 		sb.append("  else if (parseInt(navigator.appVersion) >= 4) Ns4 = true; \n");
 		sb.append("  if (Ns4 || Ns5 || Ns6) Ns4up = true; \n");
 		sb.append("} \n");
 		// and the workarounds to get Ie5 and Ie6 
 		sb.append("else if (navigator.appName.indexOf('Explorer') != -1) { \n");
 		sb.append("  if (navigator.userAgent.indexOf('MSIE 6') != -1) Ie6 = true; \n");
 		sb.append("  if (navigator.userAgent.indexOf('MSIE 5') != -1) Ie5 = true; \n");
 		sb.append("  else if (parseInt(navigator.appVersion) >= 4) Ie4 = true; \n");
 		sb.append("  if (Ie4 || Ie5 || Ie6) Ie4up = true; \n");
 		sb.append("} \n");
 
 if (DEPLOYABLE) {		
 		sb.append("function keyHandler(e) {\n");
 		sb.append("	now = new Date();\n");
 		sb.append("	if (Ns4up) { target=e.target; } else { target=e.srcElement;}\n");
 		sb.append("	if (Ns4up) {\n");
 			sb.append("	val = String.fromCharCode(e.which) + ',';\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append(" } else {\n");
 			sb.append("	val = String.fromCharCode(e.keyCode) + ',';\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append("	} \n");
 		sb.append("	document.myForm.EVENT_TIMINGS.value += msg;\n");
 		sb.append("	return true;\n");
 		sb.append("}\n");
 }		
 
 		sb.append("function submitHandler(e) {\n");
 if (DEPLOYABLE) {		
 		sb.append("	now = new Date();\n");
 		sb.append("	if (Ns4up) { target=e.target; } else { target=e.srcElement;}\n");
 		sb.append("	if (Ns4up) {\n");
 			sb.append("	val = ',';\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append(" } else {\n");
 			sb.append("	val = ',';\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append("	} \n");
 		sb.append("	document.myForm.EVENT_TIMINGS.value += msg;\n");
 }
 		sb.append("	name = document.myForm.elements['DIRECTIVE_' + target.name].value;\n");
 		sb.append("	if (e.type == 'focus') { target.value='" + activePrefix + "' + name + '" + activeSuffix + "'; }\n");
 		sb.append("	else if (e.type == 'blur') { target.value='" + inactivePrefix + "' + name + '" + inactiveSuffix + "'; }\n");
 		sb.append("	document.myForm.elements['DIRECTIVE'].value = target.name;\n");
 		sb.append("	return true;\n");
 		sb.append("}\n");
 
 if (DEPLOYABLE) {		
 		sb.append("function selectHandler(e) {\n");
 		sb.append("	now = new Date();\n");
 		sb.append("	if (Ns4up) { target=e.target; } else { target=e.srcElement;}\n");
 		sb.append("	if (Ns4up) {\n");
 			sb.append("	val = target.options[target.selectedIndex].value + ',' + target.options[target.selectedIndex].text;\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append(" } else {\n");
 			sb.append("	val = target.options[target.selectedIndex].value + ',' + target.options[target.selectedIndex].text;\n");
 			sb.append("	name = target.name;\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append("	} \n");
 
 		sb.append("	document.myForm.EVENT_TIMINGS.value += msg;\n");
 		sb.append("	return true;\n");
 		sb.append("}\n");
 }		
 		sb.append("function evHandler(e) {\n");
 if (DEPLOYABLE) {		
 		sb.append("	now = new Date();\n");
 		sb.append("	if (Ns4up) { target=e.target; } else { target=e.srcElement;}\n");
 		sb.append("	if (Ns4up) {\n");
 			sb.append("	val = ',' + target.value;\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append(" } else {\n");
 			sb.append(" if (target == null) {\n");
 			sb.append("	msg = 'null,null,' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',null\t';\n");
 		sb.append("	} else {\n");
 			sb.append("	val = ',' + target.value;\n");
 			sb.append("	msg = target.name + ',' + target.type + ',' + e.type + ',' + now.getTime() + ',' + (now.getTime() - startTime.getTime()) + ',' + val + '\t';\n");
 		sb.append("		}\n	} \n");
 		sb.append("	document.myForm.EVENT_TIMINGS.value += msg;\n");
 }
 		sb.append("	return true;\n");
 		sb.append("}\n");
 	
 if (DEPLOYABLE) {	
 		sb.append("	if (Ns4up) { window.captureEvents(Event.Load); }\n");
 		sb.append("window.onLoad = evHandler;\n");
 }		
 
 		sb.append("function init(e) {\n");
 		sb.append(" evHandler(e);\n");
 		sb.append("	for (var i=0;i<document.myForm.elements.length;++i) {\n");
 		sb.append("		el = document.myForm.elements[i];\n");
 if (DEPLOYABLE) {		
 		sb.append("		evH = evHandler;\n");
 		sb.append("		if (el.type == 'select-multiple' || el.type == 'select-one') { evH = selectHandler; } else \n");
 }
 		sb.append("		if (el.type == 'submit') { evH = submitHandler; }\n");
 		sb.append("		el.onBlur = evH;\n");
 		sb.append("		el.onClick = evH;\n");
 		sb.append("		el.onFocus = evH;\n");
 if (DEPLOYABLE) {		
 		sb.append("		el.onChange = evH;\n");
 		sb.append("		el.onKeyPress = keyHandler;\n");
 }		
 		sb.append("	}\n");
 		sb.append("	for (var k=0;k<document.images.length;++k){\n");
 		sb.append("		el = document.images[k];\n");
 		sb.append("		el.onMouseUp = evHandler;\n");
 		sb.append("	}\n");
 
 		if (firstFocus != null) {
 			sb.append("	document.myForm." + firstFocus + ".focus();\n");
 		}
 
 		sb.append("}\n");
 		sb.append("function setAdminModePassword(name) {\n");
 		sb.append("	ans = prompt('" +
 			triceps.get("Enter_password_for_Administrative_Mode") +
 				"','');\n");
 		sb.append("	if (ans == null || ans == '') return;\n");
 		sb.append("	document.myForm.PASSWORD_FOR_ADMIN_MODE.value = ans;\n");
 		sb.append("	document.myForm.submit();\n\n");
 		sb.append("}\n");
 		sb.append("function markAsRefused(name) {\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	val = document.myForm.elements[name + '_SPECIAL'];\n");
 		sb.append("	if (val.value == '" + Datum.getSpecialName(Datum.REFUSED) + "') {\n");
 		sb.append("		val.value = '';\n");
 		sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + getIcon(Schedule.REFUSED_ICON_OFF) + "';\n");
 		sb.append("	} else {\n");
 		sb.append("		val.value = '" + Datum.getSpecialName(Datum.REFUSED) + "';\n");
 		if (allowRefused)
 			sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + getIcon(Schedule.REFUSED_ICON_ON) + "';\n");
 		if (allowUnknown)
 			sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + getIcon(Schedule.UNKNOWN_ICON_OFF) + "';\n");
 		if (allowNotUnderstood)
 			sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF) + "';\n");
 		sb.append("	}\n");
 		sb.append("}\n");
 		sb.append("function markAsUnknown(name) {\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	val = document.myForm.elements[name + '_SPECIAL'];\n");
 		sb.append("	if (val.value == '" + Datum.getSpecialName(Datum.UNKNOWN) + "') {\n");
 		sb.append("		val.value = '';\n");
 		sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + getIcon(Schedule.UNKNOWN_ICON_OFF) + "';\n");
 		sb.append("	} else {\n");
 		sb.append("		val.value = '" + Datum.getSpecialName(Datum.UNKNOWN) + "';\n");
 		if (allowRefused)
 			sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + getIcon(Schedule.REFUSED_ICON_OFF) + "';\n");
 		if (allowUnknown)
 			sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + getIcon(Schedule.UNKNOWN_ICON_ON) + "';\n");
 		if (allowNotUnderstood)
 			sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF) + "';\n");
 		sb.append("	}\n");
 		sb.append("}\n");
 		sb.append("function markAsNotUnderstood(name) {\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	val = document.myForm.elements[name + '_SPECIAL'];\n");
 		sb.append("	if (val.value == '" + Datum.getSpecialName(Datum.NOT_UNDERSTOOD) + "') {\n");
 		sb.append("		val.value = '';\n");
 		sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + getIcon(Schedule.DONT_UNDERSTAND_ICON_OFF) + "';\n");
 		sb.append("	} else {\n");
 		sb.append("		val.value = '" + Datum.getSpecialName(Datum.NOT_UNDERSTOOD) + "';\n");
 		if (allowRefused)
 			sb.append("		document.myForm.elements[name + '_REFUSED_ICON'].src = '" + getIcon(Schedule.REFUSED_ICON_OFF) + "';\n");
 		if (allowUnknown)
 			sb.append("		document.myForm.elements[name + '_UNKNOWN_ICON'].src = '" + getIcon(Schedule.UNKNOWN_ICON_OFF) + "';\n");
 		if (allowNotUnderstood)
 			sb.append("		document.myForm.elements[name + '_NOT_UNDERSTOOD_ICON'].src = '" + getIcon(Schedule.DONT_UNDERSTAND_ICON_ON) + "';\n");
 		sb.append("	}\n");
 		sb.append("}\n");
 		sb.append("function help(nam,targ) {\n");
 		sb.append("	if (targ != null && targ.length != 0) { window.open(targ,'__HELP__'); }\n");
 		sb.append("}\n");
 		sb.append("function comment(name) {\n");
 		sb.append("	if (!name) return;\n");
 		sb.append("	ans = prompt('" +
 			triceps.get("Enter_a_comment_for_this_question") +
 				"',document.myForm.elements[name + '_COMMENT'].value);\n");
 		sb.append("	if (ans == null) return;\n");
 		sb.append("	document.myForm.elements[name + '_COMMENT'].value = ans;\n");
 		sb.append("	if (ans != null && ans.length > 0) {\n");
 		sb.append("		document.myForm.elements[name + '_COMMENT_ICON'].src = '" + getIcon(Schedule.COMMENT_ICON_ON) + "';\n");
 		sb.append("	} else { document.myForm.elements[name + '_COMMENT_ICON'].src = '" + getIcon(Schedule.COMMENT_ICON_OFF) + "'; }\n");
 		sb.append("}\n");
 		sb.append("function setLanguage(lang) {\n");
 		sb.append("	document.myForm.LANGUAGE.value = lang;\n");
 		sb.append("	document.myForm.submit();\n");
 		sb.append("}\n");
 		sb.append("// --> </script>\n");
 
 		return sb.toString();
 	}
 
 	private String header() {
 		StringBuffer sb = new StringBuffer();
 		String title = null;
 
 		if (isSplashScreen || !triceps.isValid()) {
 			title = VERSION_NAME;
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
 		sb.append("<body bgcolor='white' onload='init(event);'>");
 
 		return sb.toString();
 	}
 }
