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
 	private Node node;
 	private Evidence evidence;
 	private boolean forward;
 	private HttpServletRequest req;
 	private HttpServletResponse res;
 	private PrintWriter out;
 
 	public void destroy() {
 		super.destroy();
 	}
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		doPost(req, res);
 	}
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		this.req = req;
 		this.res = res;
 		HttpSession session = req.getSession(true);
 		node = (Node)session.getValue("currentNode");
 		evidence = (Evidence)session.getValue("evidence");
 		res.setContentType("text/html");
 		out = res.getWriter();
 		out.println("<html>");
 		out.println("<body bgcolor='white'>");
 		out.println("<head>");
 		out.println("<title>Diagnostic Interview Schedule for Children -- ADHD</title>");
 		out.println("</head>");
 		out.println("<body>");
 		doit();
 		out.println("</body>");
 		out.println("</html>");
 		if (node != null)
 			session.putValue("currentNode", node);
 		if (evidence != null)
 			session.putValue("evidence", evidence);
 	}
 	/**
 	 * This method basically gets the next node in the schedule, checks the nodes dependencies to see if it should be executed,
 	 * then, if it's a question it invokes queryUser(), otherwise, it evaluates the evidence and moves to the next node.
 	 */
 	private void doit() {
 		try {
 			infoMessage = null; // reset to nothing each time
 			if (node == null) {
 				step = -1;
 				forward = true;
 			}
 			if (evidence == null) {
 				evidence = new Evidence(nodes.size());
 			}
 
 			/* next, check what the user wants and handle it */
 
 			// get the posted directive (back, forward, help, suspend)
 			String directive = req.getParameter("directive");
 			if (directive == null) {
 				// very first question
 				directive = "forward"; // to avoid null pointer exception
 				step = -1; // so that increments back to 0 - allows bypassing of first question, if necessary
 			}
 			String answer = null;
 			if (directive.equals("jump-to")) {
 				answer = req.getParameter("jump-to");
 				Node next = evidence.getNode(answer);
 				if (next == null) {
 					infoMessage = "Unable to jump to Node " + answer + ": not found";
 					queryUser();
 					return;
 				}
 				else {
 					node = next;
 					queryUser();
 					return;
 					//                      forward = true; // fall through to skipping eval elements
 				}
 			}
 			else if (directive.equals("clear evidence")) {
 				evidence = new Evidence(nodes.size());
 				step = -1;
 				forward = true;
 			}
 			else if (directive.equals("reload questions")) {
 				loadSchedule();
 				queryUser();
 				return;
 			}
 			else if (directive.equals("suspend")) {
 				infoMessage = suspendInterview();
 				queryUser();
 				return;
 			}
 			else if (directive.equals("help")) {
 				infoMessage = getHelp();
 				queryUser();
 				return;
 			}
 			else if (directive.equals("forward")) {
 				forward = true;
 			}
 			else if (directive.equals("backward")) {
 				forward = false;
 			}
 			if (forward) {
 				if (node != null) {
 					try {
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
 				if (answer == null && step >= 0) {
 					infoMessage = "<bold>You cannot proceed without answering.</bold>";
 				}
 				else {
 					if (step >= 0 && node != null) {
 						Datum d = new Datum(answer);
 						evidence.set(node, d);
 					}
 					while (true) {
 						if (++step >= nodes.size()) {
 							infoMessage = "Interview Completed...";
 							break;
 						}
 						if ((node = nodes.getNode(step)) == null)
 							break;
 						if (parser.booleanVal(evidence, node.getDependencies())) {
 
 							/* if meets the criteria to ask this question ... */
 
 							if (node.getActionType().equals("q")) {
 								break;
 							}
 							else if (node.getActionType().equals("e")) {
 								evidence.set(node, new Datum(parser.StringVal(evidence, node.getAction())));
 							}
 						}
 						else {
 
 							/* indicate that this evidice is not applicable */
 
 							evidence.set(node, new Datum(Datum.NA));
 						}
 					}
 				}
 			}
 			else {
 
 				/* backwards */
 
 				//            evidence.unset(node);
 				while (true) {
 					if (--step < 0) {
 						infoMessage = "You can't back up any further";
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
 			queryUser();
 		} catch(Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
 	}
 	private String getHelp() {
 		System.out.println(evidence.toString());
 		return node.toString();
 	}
 	/**
 	 * This method runs only when the servlet is first loaded by the
 	 * webserver.  It calls the loadSchedule method to input all the nodes into memory.  The Schedule is then available to all
 	 * sessions that might be running.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		loadSchedule();
 	}
 	/**
 	 * This method loads all the nodes that comprise a Schedule. Currently, these are represented as tuples in a tab-delimited
 	 * spreadsheet file called "navigation.txt".
 	 */
 	public void loadSchedule() {
 		nodes = new Schedule("http://localhost/navigation.txt");
 	}
 	private void queryUser() {
		out.println("<form method='POST' action='http://localhost/triceps/TricepsServlet'>");
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
 			out.println("<B>" + infoMessage + "</B><br>");
 		out.println("<hr>");
 		out.println("<input type='SUBMIT' name='directive' value='backward'>");
 		out.println("<input type='SUBMIT' name='directive' value='forward'>");
 		out.println("<input type='SUBMIT' name='directive' value='help'>");
 		out.println("<input type='SUBMIT' name='directive' value='suspend'>");
 		out.println("<input type='SUBMIT' name='directive' value='jump-to'>");
 		out.println("<input type='text' name='jump-to'>");
 		out.println("<input type='SUBMIT' name='directive' value='clear evidence'>");
 		out.println("<input type='SUBMIT' name='directive' value='reload questions'>");
 		out.println("</form>");
 		out.println("<hr>" + node.toString());
 		out.println("<hr> Evidence so far: <br>");
 		for (int i = 0; i < nodes.size(); i++) {
 			Node n = nodes.getNode(i);
 			if (evidence.toString(n) == "null")
 				continue;
 			out.println("" + (i + 1) + "(" + n.getName() + "): " + n.getConcept() + ": <B>" +
 				evidence.toString(n) + "</B><BR>");
 		}
 	}
 	private String suspendInterview() {
 		return "Suspending Interview....";
 	}
 }
