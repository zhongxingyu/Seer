 import java.lang.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 /* Triceps
  * TODO:
  *  if gotoXXX returns false, will currentStep be reset?
  */
 public class Triceps implements Serializable {
 	private static final String NULL = "not set";	// a default value to represent null in config files
 	
 	private Object scheduleURL = null;
 	private Schedule nodes = null;
 	public Evidence evidence = null;	// XXX - should be private - made public for debugging from TricepsServlet
 	static public transient Parser parser = new Parser();	// XXX - should not be public - only making it so for debugging
 	
 	private Stack errors = new Stack();
 	private int currentStep=0;
 	private int numQuestions=0;	// so know how many to skip for compount question
 
 	public Triceps() {
 	}
 	
 	public boolean setSchedule(File file) {
 		if (file == null || !file.exists())
 			return false;
 		scheduleURL = file;
 		return (reloadSchedule() && resetEvidence() && setDebugEvidence());
 	}
 
 	public boolean setSchedule(String src) {
 		if (src == null)
 			return false;
 		try {
 			URL url = new URL(src);
 			return setSchedule(url);
 		}
 		catch(MalformedURLException e) {
 			System.out.println("Malformed url '" + src + "':" + e.getMessage());
 			return false;
 		}			
 	}
 	
 	public boolean setSchedule(URL url) {
 		if (url == null)
 			return false;
 			
 		scheduleURL = url;
 		return (reloadSchedule() && resetEvidence() && setDebugEvidence());
 	}
 	
 	public boolean reloadSchedule() {
 		nodes = new Schedule();
 		if (scheduleURL instanceof URL)
 			return nodes.load((URL) scheduleURL);
 		else if (scheduleURL instanceof File)
 			return nodes.load((File) scheduleURL);
 		else
 			return false;
 	}
 	
 	public Datum getDatum(Node n) {
 		return evidence.getDatum(n);
 	}
 	
 	public Enumeration getErrors() {
 		/* when there is an error in getting a node */
 		Stack tmp = errors;
 		errors = new Stack();
 		return tmp.elements();
 	}
 	
 	public Enumeration getQuestions() {
 		Vector e = new Vector();
 		int braceLevel  = 0;
 		String actionType;
 		Node node;
 		int step = currentStep;
 		
 		// should loop over available questions
 		do {
 			if (step >= size()) {
 				if (braceLevel > 0) {
 					errors.push("missing " + braceLevel + " closing brace(s)");
 				}
 				break;
 			}
 			node = nodes.getNode(step++);
 			actionType = node.getActionType();
 			e.addElement(node);	// add regardless of type
 			
 			if ("[".equals(actionType)) {
 				++braceLevel;
 			}
 			else if ("e".equals(actionType)) {
 				errors.push("Should not have expression evaluations within a query block (brace level " + braceLevel);
 				break;
 			}
 			else if ("]".equals(actionType)) {
 				--braceLevel;
 				if (braceLevel < 0) {
 					errors.push("Extra closing brace");
 					break;
 				}
 			}
 			else if ("q".equals(actionType)) {
 			}
 			else {
				errors.push("Invalid action type: " + actionType);
 				break;				
 			}
 		} while (braceLevel > 0);
 		
 		numQuestions = e.size();	// what about error conditions?
 		return e.elements();
 	}
 	
 	public String getQuestionStr(Node q) {
 		return parser.parseJSP(evidence, q.getAction()) + q.getQuestionMask();
 	}
 	
 	public boolean gotoFirst() {
 		currentStep = 0;
 		numQuestions = 0;
 		return gotoNext();
 	}
 	
 	public boolean gotoNext() {
 		Node node;
 		int braceLevel = 0;
 		String actionType;
 		int step = currentStep + numQuestions;
 		
 		do {		// loop forward through nodes -- break to query user or to end
 			if (step >= size()) {	// then the schedule is complete
 				if (braceLevel > 0) {
 					errors.push("Missing " + braceLevel + " closing brace(s)");
 				}
 				errors.push("The interview is completed.");
 				return false;	// XXX - need better message passing		
 			}
 			if ((node = nodes.getNode(step)) == null) {
 				errors.push("Invalid node at step " + step);
 				return false;
 			}
 				
 			actionType = node.getActionType();
 			
 			if ("[".equals(actionType)) {
 				if (braceLevel == 0) {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						break;	// this is the first node of a block
 					}
 					else {
 						++braceLevel;	// skip this entire section
 					}
 				}
 				else {
 					++braceLevel;	// skip this inner block
 				}
 			}
 			else if ("]".equals(actionType)) {
 				--braceLevel;	// close an open block
 				if (braceLevel < 0) {
 					errors.push("Extra closing brace");
 					return false;
 				}
 			}
 			else if ("e".equals(actionType)) {
 				if (braceLevel > 0) {
 					evidence.set(node, new Datum(Datum.NA));	// NA if internal to a brace?
 				}
 				else {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						evidence.set(node, new Datum(parser.stringVal(evidence, node.getAction()),node.getDatumType()));
 					}
 					else {
 						evidence.set(node, new Datum(Datum.NA));	// if doesn't satisfy dependencies, store NA
 					}
 				}
 			}
 			else if ("q".equals(actionType)) {
 				if (braceLevel > 0) {
 					;	// skip over it, keeping current value - looking for end of block
 				}
 				else {
 					if (parser.booleanVal(evidence, node.getDependencies())) {
 						break;	// ask this question
 					}
 					else {
 						evidence.set(node, new Datum(Datum.NA));	// if doesn't satisfy dependencies, store NA
 					}
 				}
 			}
 			else {
				errors.push("Unknown actionType " + actionType);
 				return false;
 			}
 			++step;
 		} while (true);
 		currentStep = step;
 		numQuestions = 0;
 		return true;
 	}
 	
 	public boolean gotoNode(Object val) {
 		int step = currentStep;
 		
 		Node n = evidence.getNode(val);
 		if (n == null) {
			errors.push("Unknown node: " + val);
 			return false;
 		}
 		int result = evidence.getStep(n);
 		if (result == -1) {
 			errors.push("Unable to find index for node: " + n);
 			return false;
 		} else {
 			currentStep = result;
 			return true;
 		}
 	}
 		
 	public boolean gotoPrevious() {
 		Node node;
 		int braceLevel = 0;
 		String actionType;
 		int step = currentStep;
 		
 		while (true) {
 			if (--step < 0) {
 				if (braceLevel < 0) 
 					errors.push("Missing " + braceLevel + " openining braces");
 					
 				errors.push("You are already at the beginning.");
 				return false;	// XXX need better messaging system
 			}
 			if ((node = nodes.getNode(step)) == null)
 				return false;
 				
 			actionType = node.getActionType();
 				
 			if ("e".equals(actionType)) {
 				;	// skip these going backwards?
 //				evidence.set(node,new Datum(Datum.NA));	// reset evaluations when going backwards
 			}
 			else if ("]".equals(actionType)) {
 				--braceLevel;
 			}
 			else if ("[".equals(actionType)) {
 				++braceLevel;
 				if (braceLevel > 0) {
 					errors.push("extra opening brace");
 					return false;
 				}
 				if (braceLevel == 0 && parser.booleanVal(evidence, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 			}
 			else if ("q".equals(actionType)) {
 				if (braceLevel == 0 && parser.booleanVal(evidence, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				// else within a brace, or not applicable, so skip it.
 			}
 			else {
				errors.push("invalid actionType " + actionType);
 				return false;				
 			}
 		}
 		currentStep = step;
 		return true;
 	}
 	
 	public boolean resetEvidence() {
 		evidence = new Evidence(nodes);
 		return true;
 	}
 	
 	private boolean setDebugEvidence() {
 		Node n;
 		String init;
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 			
 			init = n.getDebugAnswer();
 			
 			if (init == null || init.equals(NULL) || init.equals(Datum.TYPES[Datum.UNKNOWN])) {
 				evidence.set(n,new Datum(Datum.UNKNOWN));
 			}
 			else if (init.equals(Datum.TYPES[Datum.NA])) {
 				evidence.set(n,new Datum(Datum.NA));
 			}
 			else if (init.equals(Datum.TYPES[Datum.INVALID])) {
 				evidence.set(n,new Datum(Datum.INVALID));
 			}
 			else {	
 				evidence.set(n,new Datum(init,n.getDatumType()));	// set an initial value for the node
 			}
 		}
 		return true;
 	}
 	
 	public boolean save(String filename) {
 		try {
 			FileOutputStream fos = new FileOutputStream(filename);
 			ObjectOutputStream out = new ObjectOutputStream(fos);
 			out.writeObject(this);
 			out.flush();
 			out.close();
 			System.out.println("Saved interview to " + filename);
 			return true;
 		}
 		catch (IOException e) {
 			System.out.println(e);
 			return false;
 		}
 	}
 	
 	public boolean storeValue(Node q, String answer) {
 		try {	// set answer to the value returned with the "name" of the node
 			if (answer == null) {
 				if (q.getAnswerType() == Node.CHECK || q.getAnswerType() == Node.NOTHING) {
 					answer = "0";
 				}
 			}
 		}
 		catch(NullPointerException e) {
 			e.printStackTrace();
 		}
 
 		if ((answer == null || answer.trim().equals("")) && currentStep >= 0) {
			errors.push("<bold>Please enter a <i>" + Datum.TYPES[q.getDatumType()] + "</i> for question <i>" + q.getQuestionRef() + "</i>.");
 			return false;
 		}
 		else {	// got a proper answer -- handle it
 			if (currentStep >= 0 && q != null) {
 				Datum d = new Datum(answer,q.getDatumType()); // use expected value type
 				if (d.isInvalid()) {
 					errors.push(d.getError());
 					return false;
 				}
 				evidence.set(q, d);
 			}
 			return true;
 		}	// end handling of proper answer					
 	}
 		
 	public static Triceps restore(String filename) {
 		Triceps triceps = null;
 		try {
 			FileInputStream fis = new FileInputStream(filename);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			triceps = (Triceps) ois.readObject();
 			ois.close();
 			triceps.parser = new Parser();	// restore unsaved parser
 			return triceps;
 		}
 		catch (IOException e) {
 			System.out.println(e);
 			return null;
 		}
 		catch (java.lang.ClassNotFoundException e) {
 			System.out.println(e);
 			return null;
 		}
 	}
 	
 	public int size() { return nodes.size(); }
 	
 	public Node getNode(int i) { 
 		return nodes.getNode(i);	// XXX should not allow direct access to this?
 	}
 	
 	public String toString(Node n) {
 		Datum d = getDatum(n);
 		if (d == null)
 			return "null";
 		else
 			return d.stringVal();
 	}
 	
 	public boolean isSet(Node n) {
 		Datum d = getDatum(n);
 		if (d == null || d.isUnknown())
 			return false;
 		else
 			return true;
 	}
 	
 	public String evidenceToXML() {
 		return evidence.toXML();
 	}
 	
 	public String toXML() {
 		StringBuffer sb = new StringBuffer("<Evidence>\n");
 		Node n;
 		Datum d;
 		
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 				
 			d = evidence.getDatum(n);
 			if (d == null)
 				continue;
 				
 			sb.append(" <datum name='" + n.getName() + "' value='" + d.stringVal() + "'/>\n");
 		}
 		sb.append("</Evidence>\n");
 		return sb.toString();
 	}
 	
 	public boolean toTSV(String filename) {
 		FileWriter fw;
 		
 		try {
 			fw = new FileWriter(filename);
 			boolean ok = writeTSV(fw);
 			fw.close();
 			return ok;
 		}
 		catch (IOException e) {
			errors.push("error writing to " + filename + ": " + e);
 			return false;
 		}
 	}
 	
 	public boolean writeTSV(Writer out) {
 		Node n;
 		Datum d;
 		String ans;
 		
 		if (out == null)
 			return false;
 			
 		try {
 			for (int i=0;i<size();++i) {
 				n = nodes.getNode(i);
 				if (n == null)
 					continue;
 					
 				d = evidence.getDatum(n);
 				if (d == null) {
 					ans = NULL;
 				}
 				else {
 					ans = d.stringVal();
 				}
 					
 				out.write(n.toTSV() + "\t" + ans + "\n");
 			}
 			out.flush();
 			return true;
 		}
 		catch (IOException e) {
			errors.push("Unable to write schedule file: " + e);
 			return false;
 		}
 	}
 }
