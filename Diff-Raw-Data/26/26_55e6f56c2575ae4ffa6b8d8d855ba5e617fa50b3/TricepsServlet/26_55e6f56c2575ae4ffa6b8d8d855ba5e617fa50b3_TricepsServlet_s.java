 import java.util.*;
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 /**
  *   This is the central engine that iterates through the nodes 
  *	in a schedule producing, e.g., an interview. It also organizes 
  *	the connection to the display. In the first version, this is 
  *	an http response as defined in the JSDK.
  */
 public class TricepsServlet extends HttpServlet {
 	private Qss parser = new Qss();
 	private Schedule nodes;
 	private String infoMessage = null;
 	private int step;
 	private Node node = null;
 	private Evidence evidence;
 	private boolean forward;
 	private HttpServletRequest req;
 	private HttpServletResponse res;
 	private PrintWriter out;
 	private String schedToUse = "ADHD.txt";	// default value for now
 
 	/**
 	 * This method runs only when the servlet is first loaded by the
 	 * webserver.  It calls the loadSchedule method to input all the
 	 * nodes into memory.  The Schedule is then available to all
 	 * sessions that might be running.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
		loadSchedule(schedToUse);
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
		doPost(req, res);
 
 		HttpSession session = req.getSession(true);
 		res.setContentType("text/html");
 		out = res.getWriter();
 		out.println("<html>");
 		out.println("<body bgcolor='white'>");
 		out.println("<head>");
 		out.println("<title>TRICEPS SYSTEM</title>");
  		out.println("</head>");
 		out.println("<body>");
 		out.println("<H2>TRICEPS SYSTEM</H2>");
 		out.println("<hr>Please provide the following information to proceed. <br>");
 		out.println("<form method='POST' action='http://localhost/triceps/servlet/TricepsServlet'>");
 		out.println("<pre>");
 		out.println("The system currently supports one schedule.");
 		out.println("Select a schedule:	<select name='schedule'>");
 		out.println("							<option selected>ADHD.txt");
 		out.println("							<option>EatDis.txt");
 		out.println("							</select>");
 //		out.println("The system currently only starts a new interview.");
 		out.println("Select an interview:	<select name='interview'>");
 		out.println("								<option selected>new");
 //		out.println("								<option> test-completed");
 		out.println("								<option> test-suspended");
 		out.println("								</select>");
 		out.println("<hr>");
 		out.println("<input type='SUBMIT' name='directive' value='START'>		<input type='RESET'>");
 		out.println("</pre>");
 		out.println("</body>");
 		out.println("</html>");
 	}
 
 	/**
 	 * This method is invoked when the servlet is requested with POST variables.  This is
 	 * the case after the first request, handled by doGet(), and all further requests.
 	 */
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		this.req = req;
 		this.res = res;
 		HttpSession session = req.getSession(true);
 
 		node = (Node)session.getValue("currentNode");			// retrieve the node stored in the session
 		evidence = (Evidence)session.getValue("evidence");		// retrieve the evidence stored in the session
 		schedToUse = (String)session.getValue("schedToUse");	// retrieve the schedule used in this session
 
 		res.setContentType("text/html");
 		out = res.getWriter();
 		out.println("<html>");
 		out.println("<body bgcolor='white'>");
 		out.println("<head>");
 		out.println("<title>TRICEPS SYSTEM -- Diagnostic Interview Schedule for Children</title>");
  		out.println("</head>");
 		out.println("<body>");
 		
 		/* This is the meat. */
 		doit();
 		
 		out.println("</body>");
 		out.println("</html>");
 		
 		/* Store appropriate stuff in the session */
 		if (node != null)
 			session.putValue("currentNode", node);
 		if (evidence != null)
 			session.putValue("evidence", evidence);
 		if (schedToUse != null)
 			session.putValue("schedToUse", schedToUse);
 	}
 	/**
 	 * This method basically gets the next node in the schedule, checks the activation
 	 * dependencies to see if it should be executed, then, if it's a question it
 	 * invokes queryUser(), otherwise, it evaluates the evidence and moves to
 	 * the next node.
 	 */
 	private void doit() {
 		try {
 			String answer = null;	// reset to nothing each time
 			infoMessage = null; 		// reset to nothing each time
 
 			/* check what the user selected and do it */
 
 			// get the POSTed directive (start, back, forward, help, suspend, etc.)
 			String directive = req.getParameter("directive");
 			if (directive.equals("START")) {		// very first question -- set everything up
 				directive = "forward"; 				// to avoid null pointer exception
 				forward = true;
 				step = -1; 								// will increment to 0 below - allows bypassing of first question, if necessary
 				
 				/* get the right Schedule */
 				schedToUse = req.getParameter("schedule");
 				loadSchedule(schedToUse);
 				
 				/* prepare the Evidence -- either new or retrieved */
 				if ("test-suspended".equals(req.getParameter("interview"))) {
 //					evidence = new Evidence("/tmp/test-suspended");
 					try {
 						FileInputStream fis = new FileInputStream("/tmp/test-suspended");
 						ObjectInputStream ois = new ObjectInputStream(fis);
 						evidence = (Evidence) ois.readObject();
 						ois.close();
 					}
 					catch (IOException e) {
 						System.out.println(e);
 					}
 					// gotta get the last node stored in the evidence
 					if (evidence == null) evidence = new Evidence(nodes.size());
					else node = evidence.getNode("_400");
 				}
 				else if ("new".equals(req.getParameter("interview"))) {
 					evidence = new Evidence(nodes.size());	//  initialize the Evidence object
 				}
 			}
 			else if (directive.equals("jump-to")) {		// debugging option
 				String dest = req.getParameter("jump-to");
 				Node next = evidence.getNode(dest);		// jumps to a node in the evidence, not the schedule!!
 				if (next == null) {
 					infoMessage = "Unable to jump to Node " + dest + ": not found";
 					queryUser();		// re-display current node
 					return;
 				}
 				else {
 					node = next;
 					queryUser();		// re-display current node
 					return;
 				}
 			}
 			else if (directive.equals("clear evidence")) { // restart from scratch
 				evidence = new Evidence(nodes.size());
 				step = -1;
 				directive = "forward";
 			}
 			else if (directive.equals("reload questions")) { // debugging option
 				loadSchedule(schedToUse);
 				queryUser();	// re-display current node
 				return;
 			}
 			else if (directive.equals("suspend")) {		// gotta go -- be back later :-)
 				String status = "suspended";
 				infoMessage = "Suspending interview.....";
 				evidence.save("/tmp/test-" + status);
 				queryUser();	// re-display current node  ****** this should change!!!
 				return;
 			}
 			else if (directive.equals("help")) {		
 				infoMessage = getHelp();
 				queryUser();	// re-display current node
 				return;
 			}
 			else if (directive.equals("forward")) {
 				forward = true;
 			}
 			else if (directive.equals("backward")) {
 				forward = false;
 			}
 			
 			/* OK -- if the flow's still here, either move forward or backward through the nodes */
 			if (forward) {
 				if (node != null) {	// if a node was stored in the session (as currentNode) then --
 					try {					// set answer to the value returned with the "name" of the node
 						answer = req.getParameter(node.getName());
 						if (answer == null) {
 							if ("check".equals(node.getAnswerType())) {
 								answer = "0";
 							}
 						}
 					}
 					catch(NullPointerException e) {
 						e.printStackTrace();
 					}
 				}
 				if ((answer == null || answer.equals("")) && step >= 0) {
 					infoMessage = "<bold>You cannot proceed without answering.</bold>";
 				}
 				else {	// got a proper answer -- handle it
 					if (step >= 0 && node != null) {
 						Datum d = new Datum(answer);
 						evidence.set(node, d);
 					}
 					while (true) {		// loop forward through nodes -- break to query user or to end
 						if (++step >= nodes.size()) {	// then the schedule is complete
 							String status = "completed";
 							// store evidence here
 							infoMessage = "The interview is completed.";
 							evidence.save("/tmp/test-" + status);				
 							// close the session
 							// session.invalidate();
 							break;
 						}
 						if ((node = nodes.getNode(step)) == null)		// just in case something wierd happens
 							break;
 						
 						/* Active or inactive */
 						if (parser.booleanVal(evidence, node.getDependencies())) { // the node is active and requires action
 
 							/* get answer from user or from evidence */
 							if (node.getActionType().equals("q")) {	// queryUser()
 								break;	
 							}
 							else if (node.getActionType().equals("e")) {	// evaluate evidence, set the Datum for this node, and loop to next node
 								evidence.set(node, new Datum(parser.StringVal(evidence, node.getAction())));
 							}
 						}
 						else {	// the node is inactive and the datum value is "not applicable"
 
 							/* store in evidence a "not applicable" Datum for this node*/
 							evidence.set(node, new Datum(Datum.NA));
 						}
 					} // end while loop
 				}	// end handling of proper answer
 			}	// end forward directive
 
 			else {	/* backwards */
 
 				// evidence.unset(node);
 				while (true) {		// loop back through nodes -- break to query user
 					if (--step < 0) {
 						infoMessage = "<bold>You can't back up any further.</bold>";
 						step = 0;
 						break;
 					}
 					if ((node = nodes.getNode(step)) == null)
 						break;
 					//              evidence.unset(node);
 					if (node.getActionType().equals("e")) {
 
 						/* then can skip this going backwards */
 						continue;
 					}
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 
 						/* if meets the criteria to ask this question ... */
 						if (node.getActionType().equals("q")) {
 							break;
 						}
 					}
 				}
 			}
 
 			/* this is where the breaks fall :-) */			
 			queryUser();
 
 		} catch(Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
 	}
 
 	/**
 	 * For diagnostics -- dumps the evidence to stdout and to the infoMessage variable for display
 	 */
 	private String getHelp() {
 		System.out.println(evidence.toString());
 		return node.toString();
 	}
 
 	/**
 	 * This method loads all the nodes that comprise a Schedule. Currently,
 	 * these are represented as tuples in a tab-delimited
 	 * spreadsheet file called "navigation.txt".
 	 */
 	public void loadSchedule(String sched) {
 		nodes = new Schedule("http://localhost/" + sched);
 	}
 
 	/**
 	 * This method assembles the displayed question and answer options
 	 * and formats them in HTML for return to the client browser.
 	 */
 	private void queryUser() {
 		out.println("<form method='POST' action='http://localhost/triceps/servlet/TricepsServlet'>");
 		out.println("<H4>QUESTION AREA</H4>");
 		out.println("<B>Question " + node.getQuestionRef() + "</B>: " + parser.parseJSP(evidence, node.getAction()) + "<br>");
 		// display the answer options
 		StringTokenizer ans;
 		Datum datum = evidence.getDatum(node);
 		try {
 			String defaultValue = "";
 			try {
 				defaultValue = datum.StringVal();
 				if (null == defaultValue)
 					defaultValue = "";
 			}
 			catch(Exception e) {}
 			ans = new StringTokenizer(node.getAnswerOptions(), ";");
 			String answerType = ans.nextToken();
 			if (answerType.equals("radio")) {
 				while (ans.hasMoreTokens()) { // for however many radio buttons there are
 					String v = ans.nextToken();
 					String msg = ans.nextToken();
 					out.println("<input type='radio'" + "name='" + node.getName() + "' " + "value=" + v + (DatumMath.eq(datum,
 						new Datum(v)).booleanVal() ? " CHECKED" : "") + ">" + msg + "<br>");
 				}
 			}
 			if (answerType.equals("check")) {
 				while (ans.hasMoreTokens()) { // for however many check boxes there are
 
 					/* Add the CHECKED attribute to indicate a default choice */
 
 					String v = ans.nextToken();
 					String msg = ans.nextToken();
 					out.println("<input type='checkbox'" + "name='" + node.getName() + "' " + "value=" + v + (DatumMath.eq(datum,
 						new Datum(v)).booleanVal() ? " CHECKED" : "") + ">" + msg + "<br>");
 				}
 			}
 			if (answerType.equals("combo")) {
 				out.println("<select name='" + node.getName() + "'>");
 				while (ans.hasMoreTokens()) { // for however many check boxes there are
 
 					/* Add the CHECKED attribute to indicate a default choice */
 
 					String v = ans.nextToken();
 					String msg = ans.nextToken();
 					out.println("<option value='" + v + "'" + (DatumMath.eq(datum,
 						new Datum(v)).booleanVal() ? " SELECTED" : "") + ">" + msg + "</option>");
 				}
 				out.println("</select>");
 			}
 			if (answerType.equals("date")) {
 				out.println("Date: <input type='text' name='" + node.getName() + "' value='" + defaultValue + "'>");
 			}
 			if (answerType.equals("age")) {
 				out.println("Age: <input type='text' name='" + node.getName() + "' value='" + defaultValue + "'>");
 			}
 			if (answerType.equals("grade")) {
 				out.println("Grade: <input type='text' name='" + node.getName() + "' value='" + defaultValue + "'>");
 			}
 			if (answerType.equals("text")) {
 				out.println("Type your answer: <input type='text' name='" + node.getName() + "' value='" + defaultValue + "'>");
 			}
 		}
 		catch(Exception e) {
 			System.out.println("Error tokenizing answer options...." + e.getMessage());
 		}
 		if (infoMessage != null)
 			out.println("<br><B>" + infoMessage + "</B><br>");
 
 		// Navigation buttons
 		out.println("<hr>");
 		out.println("<H4>NAVIGATION AREA</H4>");
 		out.println("<input type='SUBMIT' name='directive' value='backward'>");
 		out.println("<input type='SUBMIT' name='directive' value='forward'>");
 		out.println("<input type='SUBMIT' name='directive' value='help'>");
 		out.println("<input type='SUBMIT' name='directive' value='suspend'>");
 		out.println("<BR>");
 		// the following buttons are for debugging
 		out.println("<input type='SUBMIT' name='directive' value='jump-to'>");
 		out.println("<input type='text' name='jump-to'>");
 		out.println("<input type='SUBMIT' name='directive' value='clear evidence'>");
 		out.println("<input type='SUBMIT' name='directive' value='reload questions'>");
 		out.println("</form>");
 
 		// Node info area
 		out.println("<hr>");
 		out.println("<H4>NODE INFORMATION AREA</H4>" + node.toString());
 
 		// Complete printout of what's been collected per node
 		out.println("<hr>");
 		out.println("<H4>EVIDENCE AREA</H4>");
		for (int i = 0; i < nodes.size(); i++) {
 			Node n = nodes.getNode(i);
 			if (evidence.toString(n) == "null")
 				continue;
			out.println("" + (i + 1) + "(" + n.getName() + "): " + n.getConcept() + ": <B>" +
				evidence.toString(n) + "</B><BR>");
 		}
 	}
 }
