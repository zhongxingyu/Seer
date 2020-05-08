 package org.dianexus.triceps;
 
 import java.lang.*;
 import java.util.*;
 import java.text.*;
 import java.io.*;
 import java.net.*;
 
 /* Triceps
  */
 public class Triceps implements VersionIF {
 	public static final boolean AUTHORABLE = true;
 	public static final String VERSION_MAJOR = "1.3";
 	public static final String VERSION_MINOR = "5";
 	public static final String VERSION_NAME = "Triceps version " + VERSION_MAJOR + "." + VERSION_MINOR;
 
 	public static final int ERROR = 1;
 	public static final int OK = 2;
 	public static final int AT_END = 3;
 	public static final int WORKING_DIR = 1;
 	public static final int COMPLETED_DIR = 2;
 	public static final int FLOPPY_DIR = 3;
 
 	private Schedule nodes = null;
 	private Evidence evidence = null;
 	private Parser parser = null;
 
 	private Logger errorLogger = null;
 	private int currentStep=0;
 	private int numQuestions=0;	// so know how many to skip for compount question
 	private int firstStep = 0;
 	private Date startTime = null;
 	private Date stopTime = null;
 	private String startTimeStr = null;
 	private String stopTimeStr = null;
 	private boolean isValid = false;
 	private Random random = new Random();
 	private String tempPassword = null;
 
 
 	/** formerly from Lingua */
 	private static final Locale defaultLocale = Locale.getDefault();
 	private ResourceBundle bundle = null;
 	private static final String BUNDLE_NAME = "TricepsBundle";
 	private Locale locale = defaultLocale;
 
 	/* Hold on to instances of Date and Number format for fast and easy retrieval */
 	private static final HashMap dateFormats = new HashMap();
 	private static final HashMap numFormats = new HashMap();
 	private static final String DEFAULT = "null";
 
 	public static final Triceps NULL = new Triceps();
 
 	private Triceps() {
 		this(null,null,null,null);
 		isValid = false;
 	}
 
 
 	public Triceps(String scheduleLoc, String workingFilesDir, String completedFilesDir, String floppyDir) {
 		/* initialize required variables */
 		parser = new Parser();
 		setLocale(null);	// the default
 		errorLogger = new Logger();
 		setSchedule(scheduleLoc,workingFilesDir,completedFilesDir,floppyDir);
 	}
 
 	public void setSchedule(String scheduleLoc, String workingFilesDir, String completedFilesDir, String floppyDir) {
 		nodes = new Schedule(this, scheduleLoc);
 		setLanguage(null);	// the default until overidden
 
 		nodes.setReserved(Schedule.WORKING_DIR,workingFilesDir,true);
 		nodes.setReserved(Schedule.COMPLETED_DIR,completedFilesDir,true);
 		nodes.setReserved(Schedule.FLOPPY_DIR,floppyDir,true);
 
 		if (!nodes.init()) {
 			setError(nodes.getErrors());
 		}
 
 		resetEvidence(false);
 		createTempPassword();
 
 		isValid = true;
 	}
 
 	public boolean isValid() { return isValid; }
 
 	public boolean reloadSchedule() {
 		return loadDatafile(null);
 	}
 
 	public boolean loadDatafile(String name) {
 if (AUTHORABLE) {
 		Schedule oldNodes = nodes;
 		Evidence oldEvidence = evidence;
 
 		boolean ok = false;
 
 		if (name != null) {
 			nodes = new Schedule(this, name);
 		}
 		else {
 			nodes = new Schedule(this, nodes.getScheduleSource());
 		}
 
 		if (!nodes.init()) {
 			nodes = oldNodes;
 			return false;
 		}
 
 		resetEvidence(false);
 
 		for (int i=0;i<oldNodes.size();++i) {
 			Node oldNode = oldNodes.getNode(i);
 			Node newNode = evidence.getNode(oldNode);	// get newNode with same name or concept as old ones
 			if (newNode != null) {
 				evidence.set(newNode, oldEvidence.getDatum(oldNode),oldNode.getTimeStampStr());
 			}
 		}
 
 		/* data/evidence is loaded from working file; but the nodes are from the schedule soruce directory */
 		nodes.overloadReserved(oldNodes);
 }
 		return true;
 	}
 
 	public Datum getDatum(Node n) {
 		return evidence.getDatum(n);
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public String getStartTimeStr() {
 		return startTimeStr;
 	}
 	public String getStopTimeStr() {
 		return stopTimeStr;
 	}
 
 	public String getScheduleErrors() { return nodes.getErrors(); }
 
 	public Enumeration getQuestions() {
 		Vector e = new Vector();
 		int braceLevel  = 0;
 		int actionType;
 		Node node;
 		int step = currentStep;
 		int currentLanguage = nodes.getLanguage();
 
 		// should loop over available questions
 		do {
 			if (step >= size()) {
 				if (braceLevel > 0) {
 					setError(get("missing") + braceLevel + get("closing_braces"));
 				}
 				break;
 			}
 			node = nodes.getNode(step++);
 
 			node.setAnswerLanguageNum(currentLanguage);
 
 			actionType = node.getQuestionOrEvalType();
 			e.addElement(node);	// add regardless of type
 
 			if (actionType == Node.GROUP_OPEN) {
 				++braceLevel;
 			}
 			else if (actionType == Node.EVAL) {
 				node.setError(get("evals_disallowed_within_question_block") + braceLevel);
 				break;
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;
 				if (braceLevel < 0) {
 					node.setError(get("extra_closing_brace"));
 					break;
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 			}
 			else {
 				node.setError(get("invalid_action_type"));
 				break;
 			}
 		} while (braceLevel > 0);
 
 		numQuestions = e.size();	// what about error conditions?
 		return e.elements();
 	}
 
 	public String getQuestionStr(Node q) {
 		/* recompute the min and max ranges, if necessary - must be done before premature abort (if invalid entry)*/
 
 		String s;
 
 		s = q.getMinStr();
 		if (s != null) {
 			Datum d = parser.parse(this,s);
 			q.setMinDatum(d);
 		}
 		else
 			q.setMinDatum(null);
 
 		s = q.getMaxStr();
 		if (s != null) {
 			Datum d = parser.parse(this,s);
 			q.setMaxDatum(d);
 		}
 		else
 			q.setMaxDatum(null);
 
 		Vector v = q.getAllowableValues();
 		if (v != null) {
 			Vector vd = new Vector();
 			for (int i=0;i<v.size();++i) {
 				vd.addElement(parser.parse(this,(String) v.elementAt(i)));
 			}
 			q.setAllowableDatumValues(vd);
 		}
 
 		q.setQuestionAsAsked(parser.parseJSP(this, q.getQuestionOrEval()) + q.getSampleInputString());
 		return q.getQuestionAsAsked();
 	}
 
 	public int gotoFirst() {
 		currentStep = 0;
 		numQuestions = 0;
 		int ok = gotoNext();
 		firstStep = currentStep;
 		return ok;
 	}
 
 	public int gotoStarting() {
 		gotoFirst();	// to set firstStep for determining minimum step number
 		currentStep = Integer.parseInt(nodes.getReserved(Schedule.STARTING_STEP));
 		if (currentStep < 0)
 			currentStep = 0;
 		numQuestions = 0;
 		return gotoNext();
 	}
 
 	public int gotoNext() {
 		Node node;
 		int braceLevel = 0;
 		int actionType;
 		int step = currentStep + numQuestions;
 		int currentLanguage = nodes.getLanguage();
 
 		if (currentStep == size()) {
 			/* then already at end */
 			setError(get("already_at_end_of_interview"));
 			return ERROR;
 		}
 
 		do {		// loop forward through nodes -- break to query user or to end
 			if (step >= size()) {	// then the schedule is complete
 				if (braceLevel > 0) {
 					setError(get("missing") + braceLevel + get("closing_braces"));
 				}
 				currentStep = size();	// put at last node
 				numQuestions = 0;
 
 				/* The current state should be saved in the background after the next set of questions is retrieved.
 					It is up to the calling program to call toTSV() to save the state */
 
 				return AT_END;
 			}
 			if ((node = nodes.getNode(step)) == null) {
 				setError(get("invalid_node_at_step") + step);
 				return ERROR;
 			}
 
 			actionType = node.getQuestionOrEvalType();
 			node.setAnswerLanguageNum(currentLanguage);
 
 			if (actionType == Node.GROUP_OPEN) {
 				if (braceLevel == 0) {
 					if (parser.booleanVal(this, node.getDependencies())) {
 						break;	// this is the first node of a block - break out of loop to ask it
 					}
 					else {
 						++braceLevel;	// XXX:  skip this entire section
 						evidence.set(node, Datum.getInstance(this,Datum.NA));	// and set all of this brace's values to NA
 					}
 				}
 				else {
 					++braceLevel;	// skip this inner block
 					evidence.set(node, Datum.getInstance(this,Datum.NA));	// set all of this brace's values to NA
 				}
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;	// close an open block
 				evidence.set(node, Datum.getInstance(this,Datum.NA));	// closing an open block, so set value to NA
 				if (braceLevel < 0) {
 					node.setError(get("extra_closing_brace"));
 					return OK;	// otherwise won't be able to progress past the dangling closing brace!
 //					return ERROR;
 				}
 			}
 			else if (actionType == Node.EVAL) {
 				if (braceLevel > 0) {
 					evidence.set(node, Datum.getInstance(this,Datum.NA));	// NA if internal to a brace when going forwards
 				}
 				else {
 					if (parser.booleanVal(this, node.getDependencies())) {
 						Datum datum = parser.parse(this, node.getQuestionOrEval());
 //						node.setDatumType(datum.type());
 						int type = node.getDatumType();
 						if (type != Datum.STRING && type != datum.type()) {
 							datum = datum.cast(type,null);
 						}
 						evidence.set(node, datum);
 					}
 					else {
 						evidence.set(node, Datum.getInstance(this,Datum.NA));	// if doesn't satisfy dependencies, store NA
 					}
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 				if (braceLevel > 0) {
 					evidence.set(node, Datum.getInstance(this,Datum.NA));	// NA if internal to a brace when going forwards
 				}
 				else {
 					if (parser.booleanVal(this, node.getDependencies())) {
 						break;	// ask this question
 					}
 					else {
 						evidence.set(node, Datum.getInstance(this,Datum.NA));	// if doesn't satisfy dependencies, store NA
 					}
 				}
 			}
 			else {
 				node.setError(get("invalid_action_type"));
 				evidence.set(node, Datum.getInstance(this,Datum.NA));
 				return ERROR;
 			}
 			++step;
 		} while (true);
 		currentStep = step;
 		numQuestions = 0;
 
 		/* The current state should be saved in the background after the next set of questions is retrieved.
 			It is up to the calling program to call toTSV() to save the state */
 		return OK;
 	}
 
 	public int gotoNode(Object val) {
 		int step = currentStep;
 
 		Node n = evidence.getNode(val);
 		if (n == null) {
 			setError(get("unknown_node") + val.toString());
 			return ERROR;
 		}
 		int result = evidence.getStep(n);
 		if (result == -1) {
 			setError(get("node_does_not_exist_within_schedule") + n);
 			return ERROR;
 		} else {
 			currentStep = result;
 			return OK;
 		}
 	}
 
 	public int gotoPrevious() {
 		Node node;
 		int braceLevel = 0;
 		int actionType;
 		int step = currentStep;
 		int currentLanguage = nodes.getLanguage();
 
 		while (true) {
 			if (--step < 0) {
 				if (braceLevel < 0)
 					setError(get("missing") + braceLevel + get("opening_braces"));
 
 				setError(get("already_at_beginning"));
 				return ERROR;
 			}
 			if ((node = nodes.getNode(step)) == null)
 				return ERROR;
 
 			actionType = node.getQuestionOrEvalType();
 //			node.setAnswerLanguageNum(currentLanguage);	// do this going backwards?
 
 			if (actionType == Node.EVAL) {
 				;	// skip these going backwards, but don't reset values when going backwards
 			}
 			else if (actionType == Node.GROUP_CLOSE) {
 				--braceLevel;
 			}
 			else if (actionType == Node.GROUP_OPEN) {
 				++braceLevel;
 				if (braceLevel > 0) {
 					setError(get("extra_opening_brace"));
 					return ERROR;
 				}
 				if (braceLevel == 0 && parser.booleanVal(this, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				else {
 					// try the next question
 				}
 			}
 			else if (actionType == Node.QUESTION) {
 				if (braceLevel == 0 && parser.booleanVal(this, node.getDependencies())) {
 					break;	// ask this block of questions
 				}
 				else {
 					// else within a brace, or not applicable, so skip it.
 				}
 			}
 			else {
 				node.setError(get("invalid_action_type"));
 				return ERROR;
 			}
 		}
 		currentStep = step;
 		return OK;
 	}
 
 	private void startTimer(Date time) {
 		startTime = time;
 		startTimeStr = formatDate(startTime,Datum.TIME_MASK);
 		stopTime = null;	// reset stopTime, since re-starting
 		nodes.setReserved(Schedule.START_TIME,startTimeStr,true);	// so that saved schedule knows when it was started
 	}
 
 	private void stopTimer() {
 		stopTime = new Date(System.currentTimeMillis());
 		stopTimeStr = formatDate(stopTime,Datum.TIME_MASK);
 	}
 
 	public void resetEvidence(boolean toUnasked) {
 		startTimer(new Date(System.currentTimeMillis()));	// use current time
 		evidence = new Evidence(this,toUnasked);
 	}
 
 	public void resetEvidence() {
 		resetEvidence(true);
 	}
 
 	public boolean storeValue(Node q, String answer, String comment, String special, boolean adminMode) {
 		if (q == null) {
 			setError(get("node_does_not_exist"));
 			return false;
 		}
 
 		if (answer == null || answer.trim().equals("")) {
 			if (q.getAnswerType() == Node.CHECK) {
 				answer = "0";	// unchecked defaults to false
 			}
 		}
 		Datum d;
 
 		if (comment != null) {
 			q.setComment(comment);
 		}
 
 		if (special != null && special.trim().length() > 0) {
 			if (adminMode) {
 				d = Datum.parseSpecialType(this, special);
 				if (d != null) {
 					evidence.set(q,d);
 					return true;
 				}
 				setError(get("unknown_special_datatype"));
 				return false;
 			}
 			else {
 				setError(get("entry_into_admin_mode_disallowed"));
 				return false;
 			}
 		}
 
 		if (q.getAnswerType() == Node.NOTHING && q.getQuestionOrEvalType() != Node.EVAL) {
 			evidence.set(q,new Datum(this, "",Datum.STRING));
 			return true;
 		}
 		else {
 			d = new Datum(this, answer,q.getDatumType(),q.getMask()); // use expected value type
 		}
 
 		/* check for type error */
 		if (!d.exists()) {
 			String s = d.getError();
 			if (s.length() == 0) {
 				q.setError("<- " + get("answer_this_question"));
 			}
 			else {
 				q.setError("<- " + s);
 			}
 			evidence.set(q,Datum.getInstance(this,Datum.UNASKED));
 			return false;
 		}
 
 		/* check if out of range */
 		if (!q.isWithinRange(d)) {
 			evidence.set(q,Datum.getInstance(this,Datum.UNASKED));
 			return false;	// shouldn't wording of error be done here, not in Node?
 		}
 		else {
 			evidence.set(q, d);
 			return true;
 		}
 	}
 
 	public int size() { return nodes.size(); }
 
 	public String toString(Node n) {
 		return toString(n,false);
 	}
 
 	public String toString(Node n, boolean showReserved) {
 		Datum d = getDatum(n);
 		if (d == null)
 			return "null";
 		else
 			return d.stringVal(showReserved);
 	}
 
 	public boolean isSet(Node n) {
 		Datum d = getDatum(n);
 		if (d == null || d.isType(Datum.UNASKED))
 			return false;
 		else
 			return true;
 	}
 
 	public Vector collectParseErrors() {
 		/* Simply cycle through nodes, processing dependencies & actions */
 
 		Node n = null;
 		Datum d = null;
 		Vector parseErrors = new Vector();
 if (AUTHORABLE) {
 		String dependenciesErrors = null;
 		String actionErrors = null;
 		String answerChoicesErrors = null;
 		String readbackErrors = null;
 		String nodeParseErrors = null;
 		String nodeNamingErrors = null;
 		boolean hasErrors = false;
 		int currentLanguage = nodes.getLanguage();
 
 		parser.resetErrorCount();
 
 		for (int i=0;i<size();++i) {
 			n = nodes.getNode(i);
 			if (n == null)
 				continue;
 
 			hasErrors = false;
 			dependenciesErrors = null;
 			actionErrors = null;
 			answerChoicesErrors = null;
 			readbackErrors = null;
 			nodeParseErrors = null;
 			nodeNamingErrors = null;
 
 			parser.booleanVal(this, n.getDependencies());
 
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				dependenciesErrors = parser.getErrors();
 			}
 
 			int actionType = n.getQuestionOrEvalType();
 			String s = n.getQuestionOrEval();
 
 			/* Check questionOrEval for syntax errors */
 			if (s != null) {
 				if (actionType == Node.QUESTION) {
 					parser.parseJSP(this, s);
 				}
 				else if (actionType == Node.EVAL) {
 					parser.stringVal(this, s);
 				}
 			}
 
 			/* Check min & max range delimiters for syntax errors */
 			s = n.getMinStr();
 			if (s != null) {
 				parser.stringVal(this, s);
 			}
 			s = n.getMaxStr();
 			if (s != null) {
 				parser.stringVal(this, s);
 			}
 
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				actionErrors = parser.getErrors();
 			}
 
 			Vector v = n.getAnswerChoices();
 			if (v != null) {
 				for (int j=0;j<v.size();++j) {
 					AnswerChoice ac = (AnswerChoice) v.elementAt(j);
 					if (ac != null)
 						ac.parse(this);	// any errors will be associated with the parser, not the node (although this is misleading)
 				}
 			}
 
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				answerChoicesErrors = parser.getErrors();
 			}
 
 			if (n.hasParseErrors()) {
 				hasErrors = true;
 				nodeParseErrors = n.getParseErrors();
 			}
 			if (n.hasNamingErrors()) {
 				hasErrors = true;
 				nodeNamingErrors = n.getNamingErrors();
 			}
 
 			if (n.getReadback(currentLanguage) != null) {
 				parser.parseJSP(this,n.getReadback(currentLanguage));
 			}
 			if (parser.hasErrors()) {
 				hasErrors = true;
 				readbackErrors = parser.getErrors();
 			}
 
 			if (hasErrors) {
 				parseErrors.addElement(new ParseError(n, dependenciesErrors, actionErrors, answerChoicesErrors, readbackErrors, nodeParseErrors, nodeNamingErrors));
 			}
 		}
 }
 		return parseErrors;
 	}
 
 	public boolean saveWorkingInfo(String name) {
 		String dir = nodes.getReserved(Schedule.WORKING_DIR);
 		boolean ok = toTSV(dir,name);
 		return ok;
 	}
 
 	public boolean saveWorkingInfo() {
 		return saveWorkingInfo(nodes.getReserved(Schedule.FILENAME));
 	}
 
 	public boolean saveCompletedInfo() {
 		String name = nodes.getReserved(Schedule.FILENAME);
 		boolean ok = toTSV(nodes.getReserved(Schedule.COMPLETED_DIR),name);
 		if (ok) {
 			ok = deleteFile(nodes.getReserved(Schedule.WORKING_DIR),name) && ok;
 		}
 		return ok;
 	}
 
 	public boolean saveToFloppy() {
 		return toTSV(nodes.getReserved(Schedule.FLOPPY_DIR),nodes.getReserved(Schedule.FILENAME));
 	}
 
 	private boolean deleteFile(String dir, String targetName) {
 		String filename = dir + targetName;
 		boolean ok = false;
 
 		try {
 			File f = new File(filename);
 			ok = f.delete();
 		}
 		catch (SecurityException e) {
 if (DEBUG) Logger.writeln("##SecurityException @ Triceps.deleteFile()" + e.getMessage());
 			String msg = get("error_deleting") + filename + ": " + e.getMessage();
 			setError(msg);
 		}
 		if (!ok)
 			Logger.writeln("##delete(" + filename + ") -> " + ok);
 		return ok;
 	}
 
 	private boolean toTSV(String dir, String targetName) {
 		String filename = dir + targetName;
 		Writer fw = null;
 		boolean ok = false;
 
 		stopTimer();
 
 		fw = nodes.getWriter(filename);
 		ok = writeTSV(fw);
 
 		if (!ok) {
 			setError(get("error_writing_to") + filename);
 		}
 		if (fw != null) {
 			try { fw.close(); } catch (IOException t) { }
 		}
 		if (!ok)
 			Logger.writeln("##save(" + filename + ") -> " + ok);
 		return ok;
 	}
 
 	private boolean writeTSV(Writer out) {
 		Node n;
 		Datum d;
 		String ans;
 
 		if (out == null)
 			return false;
 
 
 		try {
 			/* Write header information */
 			/* set save file so can resume at same position */
 			nodes.setReserved(Schedule.STARTING_STEP,Integer.toString(currentStep),true);
 			nodes.toTSV(out);
 
 			/* Write comments saying when started and stopped.  If multiply resumed, will list these several times */
 			out.write("COMMENT " + "Schedule: " + nodes.getReserved(Schedule.LOADED_FROM) + "\n");
 			out.write("COMMENT " + "Started: " + getStartTimeStr() + "\n");
 			out.write("COMMENT " + "Stopped: " + getStopTimeStr() + "\n");
 
 			for (int i=0;i<size();++i) {
 				n = nodes.getNode(i);
 				if (n == null)
 					continue;
 
 				d = evidence.getDatum(n);
 				if (d == null) {
 					ans = "";	// null values displayed as empty string
 				}
 				else {
 					ans = d.stringVal(true);
 				}
 				String comment = n.getComment();
 				if (comment == null)
 					comment = "";
 
if (AUTHORABLE) {
                out.write(n.toTSV() + "\t");
}
if (!AUTHORABLE) {
                out.write(n.getLocalName() + "\t");
}
                out.write(n.getAnswerLanguageNum() + "\t");
                out.write(n.getQuestionAsAsked() + "\t");
                out.write(ans + "\t");
                out.write(comment + "\t") ;
                out.write(n.getTimeStampStr() + "\n");
 			}
 			out.flush();
 			return true;
 		}
 		catch (IOException e) {
 if (DEBUG) Logger.writeln("##IOException @ Triceps.toTSV()" + e.getMessage());
 			String msg = get("Unable_to_write_schedule_file") + e.getMessage();
 			setError(msg);
 			return false;
 		}
 	}
 
 	public String getTitle() {
 		return nodes.getReserved(Schedule.TITLE);
 	}
 
 	public String getPasswordForAdminMode() {
 		String s = nodes.getReserved(Schedule.PASSWORD_FOR_ADMIN_MODE);
 		if (s == null || s.trim().length() == 0)
 			return null;
 		else
 			return s;
 	}
 
 	public String getIcon() { return nodes.getReserved(Schedule.ICON); }
 	public String getHeaderMsg() { return nodes.getReserved(Schedule.HEADER_MSG); }
 
 	public void setPasswordForAdminMode(String s) { nodes.setReserved(Schedule.PASSWORD_FOR_ADMIN_MODE,s); }
 
 	public Datum evaluateExpr(String expr) {
 if (AUTHORABLE) {
 		return parser.parse(this,expr);
 } else { return null; }
 	}
 
 	public String getFilename() { return nodes.getReserved(Schedule.FILENAME); }
 
 	public boolean setLanguage(String language) {
 		return nodes.setReserved(Schedule.CURRENT_LANGUAGE,language,true);
 	}
 	public int getLanguage() { return nodes.getLanguage(); }
 
 	public String createTempPassword() {
 		tempPassword = Long.toString(random.nextLong());
 		return tempPassword;
 	}
 
 	public boolean isTempPassword(String s) {
 		String temp = tempPassword;
 		createTempPassword();	// reset it
 
 		if (s == null)
 			return false;
 		return s.equals(temp);
 	}
 
 	private void setError(String s) { errorLogger.println(s); }
 	public boolean hasErrors() { return (errorLogger.size() > 0); }
 	public String getErrors() { return errorLogger.toString(); }
 
 	public Schedule getSchedule() { return nodes; }
 	public Evidence getEvidence() { return evidence; }
 	public Parser getParser() { return parser; }
 
 	public boolean isAtBeginning() { return (currentStep <= firstStep); }
 	public boolean isAtEnd() { return (currentStep >= size()); }
 	public int getCurrentStep() { return currentStep; }
 
 	public void processEventTimings(String src) {
 		if (src == null) {
 			return;
 		}
 
 		StringTokenizer lines = new StringTokenizer(src,"|",false);
 
 		while(lines.hasMoreTokens()) {
 			String s = lines.nextToken();
 			Logger.writeln(s);
 		}
 	}
 
 	/* Formerly from Lingua */
 
 	public static Locale getLocale(String lang, String country, String extra) {
 		return new Locale((lang == null) ? "" : lang,
 			(country == null) ? "" : country,
 			(extra == null) ? "" : extra);
 	}
 
 	public void setLocale(Locale loc) {
 		locale = (loc == null) ? defaultLocale : loc;
 		loadBundle();
 	}
 
 	private void loadBundle() {
 		try {
 			bundle = ResourceBundle.getBundle(BUNDLE_NAME,locale);
 		}
 		catch (MissingResourceException t) {
 if (DEBUG) Logger.writeln("##error loading resources '" + BUNDLE_NAME + "': " + t.getMessage());
 		}
 	}
 
 	public String get(String localizeThis) {
 		if (bundle == null || localizeThis == null) {
 			return "";
 		}
 		else {
 			String s = null;
 
 			try {
 				s = bundle.getString(localizeThis);
 			}
 			catch (MissingResourceException e) {
 if (DEBUG) Logger.writeln("##MissingResourceException @ Triceps.get()" + e.getMessage());
 			}
 
 			if (s == null || s.trim().length() == 0) {
 if (DEBUG) Logger.writeln("##error accessing resource '" + BUNDLE_NAME + "[" + localizeThis + "]'");
 				return "";
 			}
 			else {
 				return s;
 			}
 		}
 	}
 
 	private DateFormat getDateFormat(String mask) {
 		String key = locale.toString() + "_" + ((mask == null) ? DEFAULT : mask);
 
 		Object obj = dateFormats.get(key);
 		if (obj != null) {
 			return (DateFormat) obj;
 		}
 
 		DateFormat sdf = null;
 
 		if (mask != null) {
 			sdf = new SimpleDateFormat(mask,locale);
 		}
 
 		if (sdf == null) {
 			Locale.setDefault(locale);
 			sdf = new SimpleDateFormat();	// get the default for the locale
 			Locale.setDefault(defaultLocale);
 		}
 		synchronized (dateFormats) {
 			dateFormats.put(key,sdf);
 		}
 		return sdf;
 	}
 
 	private DecimalFormat getDecimalFormat(String mask) {
 		String key = locale.toString() + "_" + ((mask == null) ? DEFAULT : mask);
 
 		Object obj = numFormats.get(key);
 		DecimalFormat df = null;
 
 		if (obj != null) {
 			return (DecimalFormat) obj;
 		}
 		else {
 			try {
 				if (mask != null) {
 					Locale.setDefault(locale);
 					df = new DecimalFormat(mask);
 					Locale.setDefault(defaultLocale);
 				}
 			}
 			catch (SecurityException e ) {
 if (DEBUG) Logger.writeln("##SecurityException @ Triceps.getDecimalFormat()" + e.getMessage());
 				}
 			catch (NullPointerException e) {
 if (DEBUG) Logger.writeln("##error creating DecimalFormat for locale " + locale.toString() + " using mask " + mask);
 			}
 			if (df == null) {
 				;	// allow this - will use Double.format() internally
 			}
 			synchronized (numFormats) {
 				numFormats.put(key,df);
 			}
 			return df;
 		}
 	}
 
 	public Number parseNumber(Object obj, String mask) {
 		Number num = null;
 
 		if (obj == null || obj instanceof Date) {
 			num = null;
 		}
 		else if (obj instanceof Number) {
 			num = (Number) obj;
 		}
 		else {
 			DecimalFormat df;
 			String str = (String) obj;
 			if (str.trim().length() == 0)
 				return null;
 
 			if (mask == null || ((df = getDecimalFormat(mask)) == null)) {
 				Double d = null;
 
 				try {
 					d = Double.valueOf(str);
 				}
 				catch (NumberFormatException t) {}
 				catch (NullPointerException e) {}
 				if (d != null) {
 					num = assessDouble(d);
 				}
 			}
 			else {
 				try {
 					num = df.parse(str);
 				}
 				catch (java.text.ParseException e) {
 if (DEBUG) Logger.writeln("##ParseException @ Triceps.parseNumber()" + e.getMessage());
 				}
 			}
 		}
 
 		return num;
 	}
 
 	public Date parseDate(Object obj, String mask) {
 		Date date = null;
 		if (obj == null) {
 			date = null;
 		}
 		else if (obj instanceof Date) {
 			date = (Date) obj;
 		}
 		else if (obj instanceof String) {
 			String src = (String) obj;
 			try {
 				if (src.trim().length() > 0) {
 					DateFormat df = getDateFormat(mask);
 					date = df.parse(src);
 				}
 				else {
 					date = null;
 				}
 			}
 			catch (java.text.ParseException e) {
 if (DEBUG) Logger.writeln("##Error parsing date " + obj + " with mask " + mask);
 			}
 		}
 		else {
 			date = null;
 		}
 		return date;
 	}
 
 	public boolean parseBoolean(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		else if (obj instanceof Number) {
 			return (((Number) obj).doubleValue() != 0);
 		}
 		else if (obj instanceof String) {
 			return Boolean.valueOf((String) obj).booleanValue();
 		}
 		else {
 			return false;
 		}
 	}
 
 	private Number assessDouble(Double d) {
 		Double nd = new Double((double) d.longValue());
 		if (nd.equals(d)) {
 			return new Long(d.longValue());
 		}
 		else {
 			return d;
 		}
 	}
 
 
 	public String formatNumber(Object obj, String mask) {
 		String s = null;
 
 		if (obj == null) {
 			return null;
 		}
 
 		DecimalFormat df;
 
 		if (mask == null || ((df = getDecimalFormat(mask)) == null)) {
 			if (obj instanceof Date) {
 				s = "**DATE**";		// FIXME
 			}
 			else if (obj instanceof Long) {
 				s = ((Long) obj).toString();
 			}
 			else if (obj instanceof Boolean) {
 				s = ((Boolean) obj).toString();
 			}
 			else if (obj instanceof Double) {
 				Number num = assessDouble((Double) obj);
 				s = num.toString();
 			}
 			else if (obj instanceof Number) {
 				s = ((Number) obj).toString();
 			}
 			else {
 				try {
 					Double d = Double.valueOf((String) obj);
 					if (d == null) {
 						s = null;
 					}
 					else {
 						s = d.toString();
 					}
 				}
 				catch(NumberFormatException t) { }
 			}
 		}
 		else {
 			try {
 				s = df.format(obj);
 			}
 			catch(IllegalArgumentException e) {
 if (DEBUG) Logger.writeln("##IllegalArgumentException @ Triceps.formatNumber()" + e.getMessage());
 				}
 		}
 
 		return s;
 	}
 
 	public String formatDate(Object obj, String mask) {
 		if (obj == null) {
 			return null;
 		}
 
 		DateFormat df = getDateFormat(mask);
 
 		try {
 			return df.format(obj);
 		}
 		catch (IllegalArgumentException e) {
 if (DEBUG) Logger.writeln("##IllegalArgumentException @ Triceps.formatDate()" + e.getMessage());
 			return null;
 		}
 	}
 }
