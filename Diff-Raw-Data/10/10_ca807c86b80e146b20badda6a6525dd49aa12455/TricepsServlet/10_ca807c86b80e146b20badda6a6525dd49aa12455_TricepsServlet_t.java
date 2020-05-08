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
 	private static final String SUSPENDED = "suspendedInterviews";
 	private Triceps triceps;
 	private HttpServletRequest req;
 	private HttpServletResponse res;
 	private PrintWriter out;
 
 	/**
 	 * This method runs only when the servlet is first loaded by the
 	 * webserver.  It calls the loadSchedule method to input all the
 	 * nodes into memory.  The Schedule is then available to all
 	 * sessions that might be running.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
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
 		
 		triceps = (Triceps) session.getValue("triceps");
 
 		res.setContentType("text/html");
 		out = res.getWriter();
 		out.println("<html>");
 		out.println("<body bgcolor='white'>");
 		out.println("<head>");
 		out.println("<title>TRICEPS SYSTEM</title>");
 		out.println("</head>");
 		out.println("<body>");
 		out.println("<FORM method='POST' action='" + HttpUtils.getRequestURL(req) + "'>");
 		
 		/* This is the meat. */
 		try {
 			processDirective(req.getParameter("directive"));
 		}
 		catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		
 		out.println("</FORM>");
 		out.println("</body>");
 		out.println("</html>");
 		
 		/* Store appropriate stuff in the session */
 		if (triceps != null)
 			session.putValue("triceps", triceps);
 	}
 	/**
 	 * This method basically gets the next node in the schedule, checks the activation
 	 * dependencies to see if it should be executed, then, if it's a question it
 	 * invokes queryUser(), otherwise, it evaluates the evidence and moves to
 	 * the next node.
 	 */
 	private void processDirective(String directive) {
 		boolean ok = true;
 		
 		// get the POSTed directive (start, back, forward, help, suspend, etc.)	- default is opening screen
 		if (directive == null || "select new interview".equals(directive)) {
 			out.println("<H2>Triceps Interview/Questionnaire System</H2><HR>");
 			out.println("<TABLE CELLPADDING='2' CELLSPACING='2' BORDER='1'>");
 			out.println("<TR><TD>Please select an interview/questionnaire from the pull-down list:  </TD>");
 			out.println("	<TD><select name='schedule'>");
 			out.println("		<option value='ADHD.txt' selected>ADHD");
 			out.println("		<option value='EatDis.txt'>Eating Disorders");
 			out.println("		<option value='MiHeart.txt'>MiHeart-combo");
 			out.println("		<option value='MiHeart2.txt'>MiHeart-radio");
			out.println("		<option value='GAFTree.txt'>GAFTree");
 			out.println("	</select></TD>");
 			out.println("	<TD><input type='SUBMIT' name='directive' value='START'></TD>");
 			out.println("</TR>");
 			out.println("<TR><TD>OR, restore an interview/questionnaire in progress:  </TD>");
 			out.println("	<TD><input type='text' name='RESTORE'></TD>");
 			out.println("	<TD><input type='SUBMIT' name='directive' value='RESTORE'></TD>");
 			out.println("</TR><TR><TD>&nbsp;</TD><TD COLSPAN='2' ALIGN='center'><input type='checkbox' name='DEBUG' value='1'>Show debugging information</input></TD></TR>");			
 			out.println("</TABLE>");
 			return;
 		} 
 		else if (directive.equals("START")) {
 			// load schedule
 			triceps = new Triceps();
 			ok = triceps.setSchedule("http://" + req.getServerName() + "/" + req.getParameter("schedule"));
 			if (!ok) {
 				ok = triceps.setSchedule(new File("c:/cvs/triceps/docs/" + req.getParameter("schedule")));
 			}
 			
 			if (!ok) {
 				try {
 					this.doGet(req,res);
 				}
 				catch (ServletException e) {
 				}
 				catch (IOException e) {
 				}
 				return;
 			}
 				
 			ok = ok && triceps.gotoFirst();
 
 			// ask question
 		} 
 /*
 		else if (directive.equals("restore-from-object")) {
 			String restore = req.getParameter("restore-from-object");
 			restore = restore + "." + req.getRemoteUser() + "." + req.getRemoteHost() + ".suspend";
 			
 			Triceps temp;
 			if (restore == null || restore.trim().equals("") || ((temp = Triceps.restore(restore)) == null)) {
 				try {
 					this.doGet(req,res);
 				}
 				catch (ServletException e) {
 				}
 				catch (IOException e) {
 				}
 				return;				
 			}
 			else {
 				triceps = temp;
 				out.println("<B>Successfully restored interview from " + restore + "</B><HR>");
 				// check for errors (should probably throw them)
 				// ask question
 			}
 		} 
 */
 		else if (directive.equals("RESTORE")) {
 			String restore = req.getParameter("RESTORE");
 			restore = restore + "." + req.getRemoteUser() + "." + req.getRemoteHost() + ".tsv";
 			
 			// load schedule
 			triceps = new Triceps();
 			ok = triceps.setSchedule("http://" + req.getServerName() + "/" + restore);
 			if (!ok) {
 				ok = triceps.setSchedule(new File(restore));
 			}
 			
 			if (!ok) {
 				try {
 					this.doGet(req,res);
 				}
 				catch (ServletException e) {
 				}
 				catch (IOException e) {
 				}
 				return;
 			}
 				
 			ok = ok && triceps.gotoFirst();
 
 			// ask question			
 		}
 			
 		else if (directive.equals("jump to:")) {	
 			ok = triceps.gotoNode(req.getParameter("jump to:"));
 			// ask this question
 		}
 		else if (directive.equals("clear all and re-start")) { // restart from scratch
 			ok = triceps.resetEvidence();
 			ok = ok && triceps.gotoFirst();
 			// ask first question
 		}
 		else if (directive.equals("reload questions")) { // debugging option
 			ok = triceps.reloadSchedule();
 			if (ok) {
 				out.println("<B>Schedule restored successfully</B><HR>");
 			}
 			// re-ask current question
 		}
 		/*
 		else if (directive.equals("suspend-as-object-to")) {		
 			String name = req.getParameter("suspend-as-object-to");
 			String file = name + "." + req.getRemoteUser() + "." + req.getRemoteHost() + ".suspend";
 			ok = triceps.save(file);
 			if (ok) {
 				out.println("<B>Interview saved successfully as " + name + " (" + file + ")</B><HR>");
 			}
 			// re-ask same question
 		}
 		*/
 		else if (directive.equals("save to:")) {		
 			String name = req.getParameter("save to:");
 			String file = name + "." + req.getRemoteUser() + "." + req.getRemoteHost() + ".tsv";
 			
 			ok = triceps.toTSV(file);
 			if (ok) {
 				out.println("<B>Interview saved successfully as " + name + " (" + file + ")</B><HR>");
 			}
 		}
 		else if (directive.equals("evaluate expr:")) {
 			String expr = req.getParameter("evaluate expr:");
 			if (expr != null) {
 				Datum datum = triceps.parser.parse(triceps.evidence, expr);
 				
 				out.println("<TABLE WIDTH='100%' CELLPADDING='2' CELLSPACING='1' BORDER=1>");
 				out.println("<TR><TD>Equation</TD><TD><B>" + expr + "</B></TD><TD>Type</TD><TD><B>" + Datum.TYPES[datum.type()] + "</B></TD></TR>");
 				out.println("<TR><TD>String</TD><TD><B>" + datum.stringVal() + "</B></TD><TD>boolean</TD><TD><B>" + datum.booleanVal() + "</B></TD></TR>");
 				out.println("<TR><TD>double</TD><TD><B>" + datum.doubleVal() + "</B></TD><TD>long</TD><TD><B>" + datum.longVal() + "</B></TD></TR>");
 				out.println("<TR><TD>date</TD><TD><B>" + datum.dateVal() + "</B></TD><TD>month</TD><TD><B>" + datum.monthVal() + "</B></TD></TR>");
 				out.println("</TABLE>");
 			}
 		}	
 /*
 		else if (directive.equals("help")) {	// FIXME
 			out.println("<B>No help currently available</B><HR>");
 			// re-ask same question
 		}
 */
 /*
 		else if (directive.equals("show evidence as XML (unordered, duplicated)")) {
 			out.println("<B>Use 'Show Source' to see data in Evidence as XML</B><BR>");
 			out.println("<!--\n" + triceps.evidenceToXML() + "\n-->");
 			out.println("<HR>");
 			// re-ask same question
 		}
 */
 		else if (directive.equals("show XML")) {
 			out.println("<B>Use 'Show Source' to see data in Schedule as XML</B><BR>");
 			out.println("<!--\n" + triceps.toXML() + "\n-->");
 			out.println("<HR>");			
 		}
 		else if (directive.equals("forward")) {
 			// store current answer(s)
 			Enumeration questionNames = triceps.getQuestions();
 			
 			while(questionNames.hasMoreElements()) {
 				Node q = (Node) questionNames.nextElement();
 				
 				ok = triceps.storeValue(q, req.getParameter(q.getName())) && ok;	// parse all possible errors
 			}
 			// goto next
 			ok = ok && triceps.gotoNext();	// don't goto next if errors
 			// ask question
 		}
 		else if (directive.equals("backward")) {
 			// don't store current
 			// goto previous
 			ok = triceps.gotoPrevious();
 			// ask question
 		}
 		if (!ok) {
 			Enumeration errs = triceps.getErrors();
 			if (errs.hasMoreElements()) {
 				while (errs.hasMoreElements()) {
 					out.println("<B>" + (String) errs.nextElement() + "</B><BR>");
 				}
 			}
 			out.println("<HR>");
 		}
 		queryUser();
 	}
 
 	/**
 	 * This method assembles the displayed question and answer options
 	 * and formats them in HTML for return to the client browser.
 	 */
 	private void queryUser() {
 		// if parser internal to Schedule, should have method access it, not directly
 		boolean debug = false;
 		if ("1".equals(req.getParameter("DEBUG"))) {
 			debug = true;
 			out.println("<input type='HIDDEN' name='DEBUG' value='1'>");
 		}
 		
 		out.println("<H4>QUESTION AREA</H4>");
 		
 		Enumeration questionNames = triceps.getQuestions();
 		
 		out.println("<TABLE CELLPADDING='2' CELLSPACING='1' WIDTH='100%' border='1'>");
 		for(int count=0;questionNames.hasMoreElements();++count) {
 			Node node = (Node) questionNames.nextElement();
 			Datum datum = triceps.getDatum(node);
 			
 			out.println("	<TR>");
 			out.println("		<TD><B>" + node.getQuestionRef() + "</B></TD>");
 			out.println("		<TD>" + triceps.getQuestionStr(node) + "</TD>");
 			out.println("		<TD>" + node.prepareChoicesAsHTML(datum) + "</TD>");
 			out.println("	</TR>");
 		}
 		out.println("	<TR><TD COLSPAN='3' ALIGN='center'>");
 		out.println("<input type='SUBMIT' name='directive' value='forward'>");
 		out.println("<input type='SUBMIT' name='directive' value='backward'>");
 		out.println("<input type='SUBMIT' name='directive' value='clear all and re-start'>");
 		out.println("<input type='SUBMIT' name='directive' value='select new interview'>");
 		out.println("	</TD></TR>");
 		
 		if (debug) {
 			out.println("	<TR><TD COLSPAN='3' ALIGN='center'>");
 			out.println("<input type='SUBMIT' name='directive' value='jump to:'>");
 			out.println("<input type='text' name='jump to:'>");
 			out.println("<input type='SUBMIT' name='directive' value='save to:'>");
 			out.println("<input type='text' name='save to:'>");				
 			out.println("	</TD></TR>");
 			out.println("	<TR><TD COLSPAN='3' ALIGN='center'>");
 			out.println("<input type='SUBMIT' name='directive' value='reload questions'>");		
 			out.println("<input type='SUBMIT' name='directive' value='show XML'>");	
 			out.println("<input type='SUBMIT' name='directive' value='evaluate expr:'>");
 			out.println("<input type='text' name='evaluate expr:'>");
 			out.println("	</TD></TR>");
 		}
 		
 		out.println("</TABLE>");
 
 /*
 		// Node info area
 		out.println("<hr>");
 		
 		questionNames = triceps.getQuestions();
 			
 		for(int count=0;questionNames.hasMoreElements();++count) {
 			Node node = (Node) questionNames.nextElement();
 		
 			out.println("<H4>NODE INFORMATION AREA</H4>" + node.toString());
 		}
 */
 		// Complete printout of what's been collected per node
 		
 		if (debug) {
 			out.println("<hr>");
 			out.println("<H4>CURRENT QUESTION(s)</H4>");
 			out.println("<TABLE CELLPADDING='2' CELLSPACING='1'  WIDTH='100%' BORDER='1'>");
 			questionNames = triceps.getQuestions();
 				
 			while(questionNames.hasMoreElements()) {
 				Node n = (Node) questionNames.nextElement();
 				out.println("<TR>" + 
 					"<TD>" + n.getQuestionRef() + "</TD>" +
 					"<TD><B>" + triceps.toString(n) + "</B></TD>" +
 					"<TD>" + n.getName() + "</TD>" +
 					"<TD>" + n.getConcept() + "</TD>" +
 					"<TD>" + n.getDependencies() + "</TD>" +
 					"<TD>" + n.getAction() + "</TD>" +
 					"</TR>\n");			
 			}		
 			out.println("</TABLE>");
 			
 			
 			out.println("<hr>");
 			out.println("<H4>EVIDENCE AREA</H4>");
 			out.println("<TABLE CELLPADDING='2' CELLSPACING='1'  WIDTH='100%' BORDER='1'>");
 			for (int i = triceps.size()-1; i >= 0; i--) {
 				Node n = triceps.getNode(i);
 				if (!triceps.isSet(n))
 					continue;
 				out.println("<TR>" + 
 					"<TD>" + (i + 1) + "</TD>" + 
 					"<TD>" + n.getQuestionRef() + "</TD>" +
 					"<TD><B>" + triceps.toString(n) + "</B></TD>" +
 					"<TD>" + Datum.TYPES[n.getDatumType()] + "</TD>" +
 					"<TD>" + n.getName() + "</TD>" +
 					"<TD>" + n.getConcept() + "</TD>" +
 					"<TD>" + n.getDependencies() + "</TD>" +
 					"<TD>" + n.getAction() + "</TD>" +
 					"</TR>\n");
 			}
 			out.println("</TABLE>");
 		}
 	}
 }
