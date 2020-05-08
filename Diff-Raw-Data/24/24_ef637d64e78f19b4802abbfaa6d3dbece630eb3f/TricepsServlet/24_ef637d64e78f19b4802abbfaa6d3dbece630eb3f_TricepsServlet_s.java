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
 	private Triceps triceps;
 	private HttpServletRequest req;
 	private HttpServletResponse res;
 	private PrintWriter out;
 	private String firstFocus = null;
 
 	private String scheduleList = "";
 	private String scheduleSrcDir = "";
 	private String workingFilesDir = "";
 	private String completedFilesDir = "";
 	
 	/* hidden variables */
 	private boolean debug = false;
 	private boolean developerMode = false;
 	private boolean refuseToAnswerCurrent = false;
 	private boolean showQuestionNum = false;	
 	
 	private String directive = null;	// the default
 
 	/**
 	 * This method runs only when the servlet is first loaded by the
 	 * webserver.  It calls the loadSchedule method to input all the
 	 * nodes into memory.  The Schedule is then available to all
 	 * sessions that might be running.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		String s;
 
 		s = config.getInitParameter("scheduleList");
 		if (s != null && !s.trim().equals(""))
 			scheduleList = s.trim();
 		s = config.getInitParameter("scheduleSrcDir");
 		if (s != null && !s.trim().equals(""))
 			scheduleSrcDir = s.trim();
 		s = config.getInitParameter("workingFilesDir");
 		if (s != null && !s.trim().equals(""))
 			workingFilesDir = s.trim();
 		s = config.getInitParameter("completedFilesDir");
 		if (s != null && !s.trim().equals(""))
 			completedFilesDir = s.trim();			
 			
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
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		this.req = req;
 		this.res = res;
 		HttpSession session = req.getSession(true);
 		String form = null;
 		String hiddenStr = "";
 		firstFocus = null; // reset it each time
 		
 		triceps = (Triceps) session.getValue("triceps");
 
 		res.setContentType("text/html");
 		
 		directive = req.getParameter("directive");	// XXX: directive must be set before calling processHidden
 		
 		hiddenStr = processHidden();
 
 		form = processDirective();
 
 		out = res.getWriter();
 		out.println(header());
 		out.println(getCustomHeader());
 		
 		if (form != null) {
 			out.println("<FORM method='POST' name='myForm' action='" + HttpUtils.getRequestURL(req) + "'>\n");
 			out.println(hiddenStr);
 			out.println(form);
 			out.println("</FORM>\n");
 		}
 		out.println(footer());
 		
 		out.flush();
 		out.close();	// XXX:  causes "Network Connection reset by peer" with Ham-D.txt - WHY?  Without close, dangling resources?
 
 		/* Store appropriate stuff in the session */
 		if (triceps != null)
 			session.putValue("triceps", triceps);
 			
 		if ("next".equals(directive)) {
 			triceps.toTSV();
 		}
 	}
 	
 	private String processHidden() {
 		StringBuffer sb = new StringBuffer();
 		
 		if ("on".equals(req.getParameter("DEBUG"))) {
 			debug = true;
 		}
 		else
 			debug = false;
 
 		if ("on".equals(req.getParameter("developerMode"))) {
 			developerMode = true;
 		}
 		else
 			developerMode = false;
 			
 		/* XXX: Kludge - put in HIDDEN if developerMode && debug = false (else lose its state)
 			This must follow assessement of developerMode && debug, else lose state */
 		if ("on".equals(req.getParameter("showQuestionNum"))) {
 			showQuestionNum = true;
 			if (!debug && !developerMode) {
 				sb.append("<input type='HIDDEN' name='showQuestionNum' value='on'>\n");
 			}
 		}
 		else
 			showQuestionNum = false;
 			
 		String attemptingToRefuse = null;
 		refuseToAnswerCurrent = false;	// the default value
 		
 		if (triceps != null) {
 			/* XXX: Refusals only apply once Triceps has been initialized */
 			attemptingToRefuse = req.getParameter("passwordForRefused");
 			if (attemptingToRefuse != null && !attemptingToRefuse.trim().equals("")) {
 				/* if try to enter a password, make sure that doesn't reset the form if password fails */
 				directive = "next";	// XXX - since JavaScript can't set a SUBMIT value in the subjectRefusesToAnswer() function
 				
 				if (triceps.getPasswordForRefused() == null) {
 					sb.append("You are not allowed to refuse to answer these questions<BR>");
 				}
 				else {
 					if (triceps.getPasswordForRefused().equals(attemptingToRefuse)) {
 						refuseToAnswerCurrent = true;
 					}
 					else {
 						sb.append("Incorrect password to refuse to answer these questions<BR>");
 					}
 				}
 			}
 		}
 		
 		sb.append("<input type='HIDDEN' name='passwordForRefused' value=''>\n");	// must manually bypass each time
 		
 		return sb.toString();
 	}
 
 	private String header() {
 		StringBuffer sb = new StringBuffer();
 
 		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\n");
 		sb.append("<html>\n");
 		sb.append("<body bgcolor='white'");
 		if (firstFocus != null) {
 			sb.append(" onload='javascript:document.myForm." + firstFocus + ".focus()'");
 		}
 		sb.append(">\n");
 		sb.append("<head>\n");
 		sb.append("<META HTTP-EQUIV='Content-Type' CONTENT='text/html;CHARSET=iso-8859-1'>\n");
 		sb.append("<title>" + ((triceps == null) ? "TRICEPS SYSTEM" : triceps.getTitle()) + "</title>\n");
 		sb.append("</head>\n");
 		sb.append("<body>\n");
 		
 		sb.append("<SCRIPT>\n");
 		sb.append("<!--\n");
 		sb.append("function subjectRefusesToAnswer() {\n");
 		sb.append("	var ans = prompt('Enter the password to refuse to answer this question','');\n");
 		sb.append("	if (ans == null) { return; /* to avoid submit */ }\n");
 		sb.append("	document.myForm.passwordForRefused.value = ans;\n");
 		sb.append("	document.myForm.submit();\n");
 		sb.append("} //-->\n");
 		sb.append("</SCRIPT>\n");		
 		
 		return sb.toString();
 	}
 	
 	private String getCustomHeader() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append("<TABLE BORDER='0' CELLPADDING='0' CELLSPACING='3' WIDTH='100%'>\n");
 		sb.append("<TR>\n");
 		sb.append("	<TD WIDTH='18%'>\n");
 		sb.append("		<A HREF='javascript:subjectRefusesToAnswer();'>\n");
 		sb.append("			<IMG SRC='file:///C|/cic/images/ciclogo.gif' ALIGN='BOTTOM' BORDER='0' ALT='Children In the Community'>\n");
 		sb.append("		</A>\n");
 		sb.append("	</TD>\n");
 		sb.append("	<TD WIDTH='82%'><FONT SIZE='5'><B>Transitions Study</B></FONT>\n");
 		sb.append("</TR>\n");
 		sb.append("</TABLE>\n");
 		sb.append("<HR>\n");
 		
 		return sb.toString();
 	}
 	
 	private String getCustomFooter() {
 		return "";
 	}
 
 	private String footer() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(getCustomFooter());
 
 		sb.append("</body>\n");
 		sb.append("</html>\n");
 		return sb.toString();
 	}
 
 	private String processDirective() {
 		boolean ok = true;
 		int gotoMsg = Triceps.OK;
 		StringBuffer sb = new StringBuffer();
 		StringBuffer schedules = new StringBuffer();
 		StringBuffer suspendedInterviews = new StringBuffer();
 
 		// get the POSTed directive (start, back, next, help, suspend, etc.)	- default is opening screen
 		if (directive == null || "select new interview".equals(directive)) {
 			/* read list of available schedules from file */
 			
 			BufferedReader br = Triceps.getReader(scheduleList, scheduleSrcDir);
 			if (br == null) {
				sb.append("<B>Unable to find '" + scheduleList + "'</B><HR>");
 			}			
 			else {
 				try {
 					int count = 0;
 					int line=0;
 					String fileLine;
 					String src;
 					while ((fileLine = br.readLine()) != null) {
 						++line;
 
 						if (fileLine.startsWith("COMMENT"))
 							continue;
 
 						try {
 							StringTokenizer schedule = new StringTokenizer(fileLine,"\t");
 							String title = schedule.nextToken();
 							String fileLoc = schedule.nextToken();
 
 							if (title == null || fileLoc == null)
 								continue;
 
 							/* Test whether these files exist */
 							Reader target = Triceps.getReader(fileLoc,scheduleSrcDir);
 							if (target == null) {
								sb.append("Unable to access file '" + fileLoc + "'");
 							}
 							else {
 								try { target.close(); } catch (Exception e) {}
 
 								++count;
 								schedules.append("	<option value='" + Node.encodeHTML(fileLoc) + "'>" + Node.encodeHTML(title) + "</option>\n");								
 							}
 						}
 						catch (NullPointerException e) {
 							sb.append("Error tokenizing schedule list '" + scheduleList + "' on line " + line + ": " + e);
 						}
 						catch (NoSuchElementException e) {
 							sb.append("Error tokenizing schedule list '" + scheduleList + "' on line " + line + ": " + e);
 						}
 						catch (Throwable t) {}
 					}
 				}
 				catch(IOException e) {
 					sb.append("Error reading from " + scheduleList);
 				}
 				catch (Throwable t) {}
 				finally {
 					if (br != null) {
 						try { br.close(); } catch (Throwable t) { }
 					}
 				}
 				
 				/* Now build the list of uncompleted interviews */
 				
 				try {
 					File dir = new File(workingFilesDir);
 					
 					if (dir.isDirectory() && dir.canRead()) {
 						String[] files = dir.list();
 						
 						int count=0;
 						for (int i=0;i<files.length;++i) {
 							try {
 								File f = new File(files[i]);
 								if (!f.isDirectory()) {
 									if (count == 0) {
 										suspendedInterviews.append("<select name='RestoreSuspended'>\n	<option value=''></option>\n");
 									}
 									suspendedInterviews.append("	<option value='" + files[i] + "'>" + files[i] + "</option>\n");
 									++count;
 								}
 							}
 							catch (Throwable t) {
 								System.out.println(t.getMessage());
 							}
 						}
 						if (count > 0) {
 							suspendedInterviews.append("</select><BR>");
 						}
 						else {
 							suspendedInterviews.append("&nbsp;");
 						}
 					}
 					else {
 						System.out.println("can't read from dir " + dir.toString());
 					}
 				}
 				catch(Throwable t) {
 					System.out.println(t.getMessage());
 				}
 			}
 
 			/* Now construct splash screen */
 
 			sb.append("<TABLE CELLPADDING='2' CELLSPACING='2' BORDER='1'>\n");
 			sb.append("<TR><TD>Please select an interview/questionnaire from the pull-down list:  </TD>\n");
 			sb.append("	<TD><select name='schedule'>\n");
 			sb.append(schedules);
 			sb.append("	</select></TD>\n");
 			sb.append("	<TD><input type='SUBMIT' name='directive' value='START'></TD>\n");
 			sb.append("</TR>\n");
 			
 			sb.append("<TR><TD>OR, restore an interview/questionnaire in progress:  </TD>\n");
 			sb.append("	<TD>" + suspendedInterviews + 
 				((developerMode) ? "<input type='text' name='RESTORE'>" : "") +
 				"</TD>\n");
 			sb.append("	<TD><input type='SUBMIT' name='directive' value='RESTORE'></TD>\n");
 			
 			sb.append(showOptions());
 			
 			sb.append("</TABLE>\n");
 			return sb.toString();
 		}
 		else if (directive.equals("START")) {
 			// load schedule
 			triceps = new Triceps(scheduleSrcDir, workingFilesDir, completedFilesDir);
 			ok = triceps.setSchedule(req.getParameter("schedule"),scheduleSrcDir);
 
 			if (!ok) {
 				try {
 					this.doGet(req,res);
 				}
 				catch (ServletException e) {
 				}
 				catch (IOException e) {
 				}
 				return sb.toString();
 			}
 
 			ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
 			// ask question
 		}
 		else if (directive.equals("RESTORE")) {
 			String restore;
 			
 			restore = req.getParameter("RESTORE");
 			if (restore == null || restore.trim().equals("")) {
 				restore = req.getParameter("RestoreSuspended");
 			}
 			
 			// load schedule
 			triceps = new Triceps(scheduleSrcDir, workingFilesDir, completedFilesDir);
 			ok = triceps.setSchedule(restore,workingFilesDir);
 			
 			if (!ok) {
 				directive = null;	// so that processDirective() will select new interview
 				
 				return "<B>Unable to find or access schedule '" + restore + "'</B><HR>" +
 					processDirective();
 			}
 
 			ok = ok && ((gotoMsg = triceps.gotoStarting()) == Triceps.OK);	// don't proceed if prior error
 
 			// ask question
 		}
 		else if (directive.equals("jump to:")) {
 			gotoMsg = triceps.gotoNode(req.getParameter("jump to:"));
 			ok = (gotoMsg == Triceps.OK);
 			// ask this question
 		}
 		else if (directive.equals("restart (clean)")) { // restart from scratch
 			ok = triceps.resetEvidence();
 			ok = ok && ((gotoMsg = triceps.gotoFirst()) == Triceps.OK);	// don't proceed if prior error
 			// ask first question
 		}
 		else if (directive.equals("reload questions")) { // debugging option
 			ok = triceps.reloadSchedule();
 			if (ok) {
 				sb.append("<B>Schedule restored successfully</B><HR>\n");
 			}
 			// re-ask current question
 		}
 		else if (directive.equals("save to:")) {
 			String name = req.getParameter("save to:");
 			String file = workingFilesDir + name;
 			ok = triceps.toTSV(file);
 			if (ok) {
 				sb.append("<B>Interview saved successfully as " + Node.encodeHTML(name) + " (" + Node.encodeHTML(file) + ")</B><HR>\n");
 			}
 		}
 		else if (directive.equals("evaluate expr:")) {
 			String expr = req.getParameter("evaluate expr:");
 			if (expr != null) {
 				Datum datum = triceps.evaluateExpr(expr);
 
 				sb.append("<TABLE WIDTH='100%' CELLPADDING='2' CELLSPACING='1' BORDER=1>\n");
 				sb.append("<TR><TD>Equation</TD><TD><B>" + Node.encodeHTML(expr) + "</B></TD><TD>Type</TD><TD><B>" + Datum.TYPES[datum.type()] + "</B></TD></TR>\n");
 				sb.append("<TR><TD>String</TD><TD><B>" + Node.encodeHTML(datum.stringVal(true)) + "</B></TD><TD>boolean</TD><TD><B>" + datum.booleanVal() + "</B></TD></TR>\n");
 				sb.append("<TR><TD>double</TD><TD><B>" + datum.doubleVal() + "</B></TD><TD>long</TD><TD><B>" + datum.longVal() + "</B></TD></TR>\n");
 				sb.append("<TR><TD>date</TD><TD><B>" + datum.dateVal() + "</B></TD><TD>month</TD><TD><B>" + datum.monthVal() + "</B></TD></TR>\n");
 				sb.append("</TABLE>\n");
 				
 				Enumeration errs = triceps.getErrors();
 				if (errs.hasMoreElements()) {
 					sb.append("<B>There were errors parsing that equation:</B><BR>");
 					while (errs.hasMoreElements()) {
 						sb.append("<B>" + Node.encodeHTML((String) errs.nextElement()) + "</B><BR>\n");
 					}
 				}				
 			}
 		}
 		else if (directive.equals("show XML")) {
 			sb.append("<B>Use 'Show Source' to see data in Schedule as XML</B><BR>\n");
 			sb.append("<!--\n" + triceps.toXML() + "\n-->\n");
 			sb.append("<HR>\n");
 		}
 		else if (directive.equals("show Errors")) {
 			Vector pes = triceps.collectParseErrors();
 
 			if (pes.size() == 0) {
 				sb.append("<B>No errors were found</B><HR>");
 			}
 			else {
 				Vector errs;
 
 				for (int i=0;i<pes.size();++i) {
 					ParseError pe = (ParseError) pes.elementAt(i);
 					Node n = pe.getNode();
 
 					if (i == 0) {
 						sb.append("<FONT color='red'>The following errors were found in file <B>" + Node.encodeHTML(n.getSourceFile()) + "</B></FONT><BR>\n");
 						sb.append("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'>\n");
 						sb.append("<TR><TD>line#</TD><TD>name</TD><TD>Dependencies</TD><TD><B>Dependency Errors</B></TD><TD>Action Type</TD><TD>Action</TD><TD><B>Action Errors</B></TD><TD><B>Other Errors</B></TD></TR>\n");
 					}
 
 					sb.append("\n<TR><TD>" + n.getSourceLine() + "</TD><TD>" + Node.encodeHTML(n.getQuestionRef(),true) + "</TD>");
 					sb.append("\n<TD>" + Node.encodeHTML(pe.getDependencies(),true) + "</TD>\n<TD>");
 
 					errs = pe.getDependenciesErrors();
 					if (errs.size() == 0) {
 						sb.append("&nbsp;");
 					}
 					else {
 						for (int j=0;j<errs.size();++j) {
 							if (j > 0)
 								sb.append("<BR>");
 							sb.append("" + (j+1) + ")&nbsp;" + Node.encodeHTML((String) errs.elementAt(j),true));
 						}
 					}
 
 					sb.append("</TD>\n<TD>" + Node.ACTION_TYPES[n.getActionType()] + "</TD><TD>" + Node.encodeHTML(pe.getAction(),true) + "</TD><TD>");
 
 					errs = pe.getActionErrors();
 					if (errs.size() == 0) {
 						sb.append("&nbsp;");
 					}
 					else {
 						for (int j=0;j<errs.size();++j) {
 							if (j > 0)
 								sb.append("<BR>");
 							sb.append("" + (j+1) + ")&nbsp;" + Node.encodeHTML((String) errs.elementAt(j),true));
 						}
 					}
 
 					sb.append("</TD>\n<TD>");
 
 					errs = pe.getNodeErrors();
 					if (errs.size() == 0) {
 						sb.append("&nbsp;");
 					}
 					else {
 						for (int j=0;j<errs.size();++j) {
 							if (j > 0)
 								sb.append("<BR>");
 							sb.append("" + (j+1) + ")&nbsp;" + (String) errs.elementAt(j));	// XXX: don't Node.encodeHTML() these, since pre-processed within Node
 						}
 					}
 					sb.append("</TD></TR>");
 				}
 				sb.append("</TABLE><HR>\n");
 			}
 		}
 		else if (directive.equals("next")) {
 			// store current answer(s)
 			Enumeration questionNames = triceps.getQuestions();
 			
 			while(questionNames.hasMoreElements()) {
 				Node q = (Node) questionNames.nextElement();
 				boolean status;
 
 				status = triceps.storeValue(q, req.getParameter(q.getName()),refuseToAnswerCurrent);
 				ok = status && ok;
 
 			}
 			// goto next
 			ok = ok && ((gotoMsg = triceps.gotoNext()) == Triceps.OK);	// don't proceed if prior errors - e.g. unanswered questions
 
 			if (gotoMsg == Triceps.AT_END) {
 				// save the file, but still give the option to go back and change answers
 				boolean savedOK;
 				String name = Datum.format(triceps.getStartTime(),Datum.DATE,Datum.TIME_MASK);
 				String file = completedFilesDir + name;
 
 				sb.append("<B>Thank you, the interview is completed</B><BR>\n");
 				savedOK = triceps.toTSV(file);
 				ok = savedOK && ok;
 				if (savedOK) {
 					sb.append("<B>Interview saved successfully as " + Node.encodeHTML(name) + " (" + Node.encodeHTML(file) + ")</B><HR>\n");
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
 //		if (!ok) 
 		{
 			/* should do this regardless of OK status?  Might catch interesting parsing errors? */
 			int errCount = 0;
 			Enumeration errs = triceps.getErrors();
 			if (errs.hasMoreElements()) {
 				while (errs.hasMoreElements()) {
 					++errCount;
 					sb.append("<B>" + Node.encodeHTML((String) errs.nextElement()) + "</B><BR>\n");
 				}
 			}
 
 			Enumeration nodes = triceps.getQuestions();
 			while (nodes.hasMoreElements()) {
 				Node n = (Node) nodes.nextElement();
 				if (n.hasRuntimeErrors()) {
 					if (++errCount == 1) {
 						sb.append("<B>Please answer the question(s) listed in <FONT color='red'>RED</FONT> before proceeding</B><BR>\n");
 					}
 					if (n.focusableArray()) {
 						firstFocus = Node.encodeHTML(n.getName()) + "[0]";
 						break;
 					}
 					else if (n.focusable()) {
 						firstFocus = Node.encodeHTML(n.getName());
 						break;
 					}
 				}
 			}
 
 			if (errCount > 0) {
 				sb.append("<HR>\n");
 			}
 		}
 
 		if (firstFocus == null) {
 			Enumeration nodes = triceps.getQuestions();
 			while (nodes.hasMoreElements()) {
 				Node n = (Node) nodes.nextElement();
 				if (n.focusableArray()) {
 					firstFocus = Node.encodeHTML(n.getName()) + "[0]";
 					break;
 				}
 				else if (n.focusable()) {
 					firstFocus = Node.encodeHTML(n.getName());
 					break;
 				}
 			}
 		}
 		if (firstFocus == null) {
 			firstFocus = "directive[0]";	// try to focus on Next button if nothing else available
 		}		
 
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
 	
 		if (debug) {
 			sb.append("<H4>QUESTION AREA</H4>\n");
 		}
 
 		Enumeration questionNames = triceps.getQuestions();
 		String color;
 		String errMsg;
 
 		sb.append("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'>\n");
 		for(int count=0;questionNames.hasMoreElements();++count) {
 			Node node = (Node) questionNames.nextElement();
 			Datum datum = triceps.getDatum(node);
 
 			if (node.hasRuntimeErrors()) {
 				color = " color='red'";
 				StringBuffer errStr = new StringBuffer("<FONT color='red'>");
 
 				Vector errs = node.getRuntimeErrors();
 
 				for (int j=0;j<errs.size();++j) {
 					if (j > 0) {
 						errStr.append("<BR>\n");
 					}
 					errStr.append(Node.encodeHTML((String) errs.elementAt(j)));
 				}
 				errStr.append("</FONT>");
 				errMsg = errStr.toString();
 			}
 			else {
 				color = "";
 				errMsg = "";
 			}
 
 			sb.append("	<TR>\n");
 			
 			if (showQuestionNum) {
 				sb.append("<TD><FONT" + color + "><B>" + Node.encodeHTML(node.getQuestionRef()) + "</FONT></B></TD>\n");
 			}
 			
 			switch(node.getAnswerType()) {
 				case Node.NOTHING:
 					sb.append("		<TD COLSPAN='2'><FONT" + color + ">" + Node.encodeHTML(triceps.getQuestionStr(node)) + "</FONT></TD>\n");
 					break;
 				case Node.RADIO_HORIZONTAL:
 					sb.append("		<TD COLSPAN='2'><FONT" + color + ">" + Node.encodeHTML(triceps.getQuestionStr(node)) + "</FONT></TD>\n");
 					sb.append("</TR>\n<TR>\n");
 					if (showQuestionNum) {
 						sb.append("<TD>&nbsp;</TD>");
 					}
 					sb.append(node.prepareChoicesAsHTML(datum,errMsg));
 					break;
 				default:
 					sb.append("		<TD><FONT" + color + ">" + Node.encodeHTML(triceps.getQuestionStr(node)) + "</FONT></TD>\n");
 					sb.append("		<TD>" + node.prepareChoicesAsHTML(datum) + errMsg + "</TD>\n");				
 					break;				
 			}
 			sb.append("	</TR>\n");
 		}
 		sb.append("	<TR><TD COLSPAN='" + ((showQuestionNum) ? 3 : 2 ) + "' ALIGN='center'>\n");
 		sb.append("<input type='SUBMIT' name='directive' value='next'>\n");
 		sb.append("<input type='SUBMIT' name='directive' value='previous'>");
 		
 		sb.append("	</TD></TR>\n");
 
 		if (developerMode || debug) {
 			sb.append("	<TR><TD COLSPAN='" + ((showQuestionNum) ? 3 : 2 ) + "' ALIGN='center'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='select new interview'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='restart (clean)'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='jump to:' size='10'>\n");
 			sb.append("<input type='text' name='jump to:'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='save to:'>\n");
 			sb.append("<input type='text' name='save to:'>\n");
 			sb.append("	</TD></TR>\n");
 			sb.append("	<TR><TD COLSPAN='" + ((showQuestionNum) ? 3 : 2 ) + "' ALIGN='center'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='reload questions'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='show Errors'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='show XML'>\n");
 			sb.append("<input type='SUBMIT' name='directive' value='evaluate expr:'>\n");
 			sb.append("<input type='text' name='evaluate expr:'>\n");
 			sb.append("	</TD></TR>\n");
 		}
 		
 		sb.append(showOptions());
 
 		sb.append("</TABLE>\n");
 
 		// Complete printout of what's been collected per node
 
 		if (debug) {
 			sb.append("<hr>\n");
 			sb.append("<H4>CURRENT QUESTION(s)</H4>\n");
 			sb.append("<TABLE CELLPADDING='2' CELLSPACING='1'  WIDTH='100%' BORDER='1'>\n");
 			questionNames = triceps.getQuestions();
 
 			while(questionNames.hasMoreElements()) {
 				Node n = (Node) questionNames.nextElement();
 				sb.append("<TR>");
 				sb.append("<TD>" + Node.encodeHTML(n.getQuestionRef(),true) + "</TD>");
 				sb.append("<TD><B>" + Node.encodeHTML(triceps.toString(n,true),true) + "</B></TD>");
 				sb.append("<TD>" + Node.encodeHTML(Datum.TYPES[n.getDatumType()]) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getName(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getConcept(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getDependencies(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getActionTypeField(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getAction(),true) + "</TD>");
 				sb.append("</TR>\n");
 			}
 			sb.append("</TABLE>\n");
 
 
 			sb.append("<hr>\n");
 			sb.append("<H4>EVIDENCE AREA</H4>\n");
 			sb.append("<TABLE CELLPADDING='2' CELLSPACING='1'  WIDTH='100%' BORDER='1'>\n");
 			for (int i = triceps.size()-1; i >= 0; i--) {
 				Node n = triceps.getNode(i);
 				if (!triceps.isSet(n))
 					continue;
 				sb.append("<TR>");
 				sb.append("<TD>" + (i + 1) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getQuestionRef(),true) + "</TD>");
 				sb.append("<TD><B>" + Node.encodeHTML(triceps.toString(n,true),true) + "</B></TD>");
 				sb.append("<TD>" +  Node.encodeHTML(Datum.TYPES[n.getDatumType()]) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getName(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getConcept(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getDependencies(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getActionTypeField(),true) + "</TD>");
 				sb.append("<TD>" + Node.encodeHTML(n.getAction(),true) + "</TD>");
 				sb.append("</TR>\n");
 			}
 			sb.append("</TABLE>\n");
 		}
 		return sb.toString();
 	}
 	
 	private String showOptions() {
 		if (developerMode || debug) {
 			StringBuffer sb = new StringBuffer();
 		
 			sb.append("	<TR><TD COLSPAN='" + ((showQuestionNum) ? 3 : 2 ) + "' ALIGN='center'>\n");
 			sb.append("  developerMode<input type='checkbox' name='developerMode' value='on'" + ((developerMode) ? " CHECKED" : "") + ">\n");
 			sb.append("  showQuestionNum<input type='checkbox' name='showQuestionNum' value='on'" + ((showQuestionNum) ? " CHECKED" : "") + ">\n");
 			sb.append("  debug<input type='checkbox' name='DEBUG' value='on'" + ((debug) ? " CHECKED" : "") + ">\n");
 			sb.append("</TD></TR>\n");
 			return sb.toString();
 		}
 		else
 			return "";
 	}
 }
