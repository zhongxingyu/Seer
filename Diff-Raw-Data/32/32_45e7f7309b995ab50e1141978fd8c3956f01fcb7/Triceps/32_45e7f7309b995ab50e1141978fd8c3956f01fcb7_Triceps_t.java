 package org.dianexus.triceps;
 
 /*import java.lang.*;*/
 /*import java.util.*;*/
 /*import java.text.*;*/
 /*import java.io.*;*/
 /*import java.net.*;*/
 /*import java.util.zip.*;*/
 /*import java.util.jar.*;*/
 
 import java.util.Date;
 import java.lang.String;
 import java.util.Random;
 import java.util.Locale;
 import java.util.ResourceBundle;
import java.util.Hashtable;
 import java.io.File;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.jar.JarOutputStream;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.jar.JarEntry;
 import java.io.UnsupportedEncodingException;
 import java.util.StringTokenizer;
 import java.util.MissingResourceException;
 import java.text.SimpleDateFormat;
 import java.io.FileNotFoundException;
 import java.lang.SecurityException;
 
 
 /*public*/ class Triceps implements VersionIF {
 	private static final String DATAFILE_PREFIX = "tri";
 	private static final String DATAFILE_SUFFIX = ".dat";
 	private static final String EVENTFILE_SUFFIX = ".evt";
 	
 	/*public*/ static final int ERROR = 1;
 	/*public*/ static final int OK = 2;
 	/*public*/ static final int AT_END = 3;
 	/*public*/ static final int WORKING_DIR = 1;
 	/*public*/ static final int COMPLETED_DIR = 2;
 	/*public*/ static final int FLOPPY_DIR = 3;
 
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
 	Logger dataLogger = Logger.NULL;	
 	private Logger eventLogger = Logger.NULL;
 
 
 	/** formerly from Lingua */
 	private static final Locale defaultLocale = Locale.getDefault();
 	private ResourceBundle bundle = null;
 	private static final String BUNDLE_NAME = "TricepsBundle";
 	private Locale locale = defaultLocale;
 
 	/* Hold on to instances of Date and Number format for fast and easy retrieval */
	private static final Hashtable dateFormats = new Hashtable();
	private static final Hashtable numFormats = new Hashtable();
 	private static final String DEFAULT = "null";
 
 	/*public*/ static final Triceps NULL = new Triceps();
 
 	private Triceps() {
 		this(null,null,null,null);
 		isValid = false;
 	}
 
 
 	/*public*/ Triceps(String scheduleLoc, String workingFilesDir, String completedFilesDir, String floppyDir) {
 		/* initialize required variables */
 		parser = new Parser();
 		setLocale(null);	// the default
 		errorLogger = new Logger();
 		if (scheduleLoc != null) {
 			createDataLogger(workingFilesDir,null);
 		}
 		isValid = init(scheduleLoc,workingFilesDir,completedFilesDir,floppyDir);
 	}
 	
 	private boolean init(String scheduleLoc, String workingFilesDir, String completedFilesDir, String floppyDir) {
 		evidence = new Evidence(this);
 		return setSchedule(scheduleLoc,workingFilesDir,completedFilesDir,floppyDir);
 	}
 	
 	/*public*/ void deleteDataLoggers() {
 		dataLogger.delete();
 		dataLogger = Logger.NULL;
 		eventLogger.delete();
 		eventLogger = Logger.NULL;
 	}
 	
	/*public*/ void closeDataLogger() {
if (DEPLOYABLE) {
		if (dataLogger != null)
			dataLogger.close();
		if (eventLogger != null)
			eventLogger.close();
}			
	}
 	
 	/*public*/ void createDataLogger(String dir, String name) {
 if (DEPLOYABLE) {		
 		try {
 			File tempDataFile = null;
 			File tempEventFile = null;
 			
 			if (name == null) {
 				tempDataFile = File.createTempFile(DATAFILE_PREFIX, DATAFILE_SUFFIX, new File(dir));
 				tempEventFile = new File(tempDataFile.toString() + EVENTFILE_SUFFIX);
 			}
 			else {
 				tempDataFile = new File(name);
 				tempEventFile = new File(name + EVENTFILE_SUFFIX);
 			}
 			if (!tempDataFile.toString().equals(dataLogger.getFilename())) {
 				dataLogger.delete();
 			}
 			if (!tempEventFile.toString().equals(eventLogger.getFilename())) {
 				eventLogger.delete();
 			}
 			
 			dataLogger = new Logger(Logger.UNIX_EOL,false,tempDataFile);
 			eventLogger = new Logger(Logger.UNIX_EOL,false,tempEventFile);
 		}
 		catch (Throwable t) {
 if (DEBUG) Logger.writeln("##Triceps.createDataLogger()-unable to create temp file" + t.getMessage());
 		}
}	// DEPLOYABLE
 		if (dataLogger == null) {
 			dataLogger = Logger.NULL;
 if (DEBUG) Logger.writeln("##Triceps.createDataLogger()->writer is null");			
		}
 		if (eventLogger == null) {
 			eventLogger = Logger.NULL;
 if (DEBUG) Logger.writeln("##Triceps.createEventLogger()->writer is null");			
 		}	
 	}
 
 	/*public*/ boolean setSchedule(String scheduleLoc, String workingFilesDir, String completedFilesDir, String floppyDir) {
 		if (scheduleLoc == null) {
 			nodes = Schedule.NULL;
 			return false;
 		}
 		
 		nodes = new Schedule(this, scheduleLoc);
 		setLanguage(null);	// the default until overidden
 
 		nodes.setReserved(Schedule.WORKING_DIR,workingFilesDir);
 		nodes.setReserved(Schedule.COMPLETED_DIR,completedFilesDir);
 		nodes.setReserved(Schedule.FLOPPY_DIR,floppyDir);
 		
 		createTempPassword();
 
 		if (nodes.init()) {
 			return true;
 		}
 		else {
 			setError(nodes.getErrors());
 			return true;
 		}
 	}
 
 	/*public*/ boolean isValid() { return isValid; }
 
 	/*public*/ boolean reloadSchedule() {
 if (AUTHORABLE) {
 		Schedule oldNodes = nodes;
 		Evidence oldEvidence = evidence;
 		boolean ok = false;
 		
 		ok = init(oldNodes.getReserved(Schedule.SCHEDULE_SOURCE), 
 			oldNodes.getReserved(Schedule.WORKING_DIR), 
 			oldNodes.getReserved(Schedule.COMPLETED_DIR), 
 			oldNodes.getReserved(Schedule.FLOPPY_DIR));
 			
 		if (!ok) {
 if (DEBUG) Logger.writeln("##Unable to reload schedule");			
 			nodes = oldNodes;
 			evidence = oldEvidence;
 			return false;
 		}
 		
 		for (int i=0;i<oldNodes.size();++i) {
 			Node oldNode = oldNodes.getNode(i);
 			Node newNode = evidence.getNode(oldNode);	// get newNode with same name or concept as old ones
 			if (newNode != null) {
 				evidence.set(newNode, oldEvidence.getDatum(oldNode),oldNode.getTimeStampStr(),false);	// don't record this, since already recorded
 			}
 		}
 
 		/* data/evidence is loaded from working file; but the nodes are from the schedule source directory */
 		nodes.overloadReserved(oldNodes);
 }
 		return true;
 	}
 
 	/*public*/ Datum getDatum(Node n) {
 		return evidence.getDatum(n);
 	}
 
 	/*public*/ Date getStartTime() {
 		return startTime;
 	}
 
 	/*public*/ String getStartTimeStr() {
 		return startTimeStr;
 	}
 	/*public*/ String getStopTimeStr() {
 		return stopTimeStr;
 	}
 
 	/*public*/ String getScheduleErrors() { return nodes.getErrors(); }
 
 	/*public*/ Enumeration getQuestions() {
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
 
 	/*public*/ String getQuestionStr(Node q) {
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
 
 	/*public*/ int gotoFirst() {
 		currentStep = 0;
 		numQuestions = 0;
 		int ok = gotoNext();
 		firstStep = currentStep;
 		return ok;
 	}
 
 	/*public*/ int gotoStarting() {
 //		gotoFirst();	// to set firstStep for determining minimum step number
 		currentStep = Integer.parseInt(nodes.getReserved(Schedule.STARTING_STEP));
 		if (currentStep < 0)
 			currentStep = 0;
 		numQuestions = 0;
 		return gotoNext();
 	}
 
 	/*public*/ int gotoNext() {
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
 					return ERROR;
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
 		
 		numQuestions = 0;
 		
 		if (currentStep != step) {
 			currentStep = step;
 	
 			/* The current state should be saved in the background after the next set of questions is retrieved.
 				It is up to the calling program to call toTSV() to save the state */
 			nodes.setReserved(Schedule.STARTING_STEP,Integer.toString(currentStep));
 			dataLogger.flush();
 		}
 					
 		return OK;
 	}
 
 	/*public*/ int gotoNode(Object val) {
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
 
 	/*public*/ int gotoPrevious() {
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
 		if (currentStep != step) {
 			currentStep = step;
 			
 			nodes.setReserved(Schedule.STARTING_STEP,Integer.toString(currentStep));
 			dataLogger.flush();
 		}
 		
 		return OK;
 	}
 
 	private void startTimer(Date time) {
 		startTime = time;
 		startTimeStr = formatDate(startTime,Datum.TIME_MASK);
 		stopTime = null;	// reset stopTime, since re-starting
 		nodes.setReserved(Schedule.START_TIME,startTimeStr);	// so that saved schedule knows when it was started
 	}
 
 	private void stopTimer() {
 		stopTime = new Date(System.currentTimeMillis());
 		stopTimeStr = formatDate(stopTime,Datum.TIME_MASK);
 	}
 
 	/*public*/ void resetEvidence(boolean toUnasked) {
 		startTimer(new Date(System.currentTimeMillis()));	// use current time
 		evidence.reset();
 	}
 
 	/*public*/ void resetEvidence() {
 		resetEvidence(true);
 	}
 
 	/*public*/ boolean storeValue(Node q, String answer, String comment, String special, boolean adminMode) {
 		boolean ok = false;
 		Datum d = null;
 		
 		if (q == null) {
 			setError(get("node_does_not_exist"));
 			return false;
 		}
 
 		if (answer == null || answer.trim().equals("")) {
 			if (q.getAnswerType() == Node.CHECK) {
 				answer = "0";	// unchecked defaults to false
 			}
 		}
 
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
 				else {
 					setError(get("unknown_special_datatype"));
 					return false;
 				}
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
 			/* check for type error */
 			
 			if (!d.exists()) {
 				String s = d.getError();
 				if (s.length() == 0) {
 					q.setError("<- " + get("answer_this_question"));
 				}
 				else {
 					q.setError("<- " + s);
 				}
 				d = Datum.getInstance(this,Datum.UNASKED);
 				ok = false;
 			}
 			else {
 				/* check if out of range */
 				if (!q.isWithinRange(d)) {
 					d = Datum.getInstance(this,Datum.UNASKED);
 					ok = false;	// shouldn't wording of error be done here, not in Node?
 				}
 				else {
 					ok = true;
 				}
 			}
 			evidence.set(q,d);
 			return ok;			
 		}
 	}
 	
 	/*public*/ int size() { return nodes.size(); }
 
 	/*public*/ String toString(Node n) {
 		return toString(n,false);
 	}
 
 	/*public*/ String toString(Node n, boolean showReserved) {
 		Datum d = getDatum(n);
 		if (d == null)
 			return "null";
 		else
 			return d.stringVal(showReserved);
 	}
 
 	/*public*/ boolean isSet(Node n) {
 		Datum d = getDatum(n);
 		if (d == null || d.isType(Datum.UNASKED))
 			return false;
 		else
 			return true;
 	}
 
 	/*public*/ Vector collectParseErrors() {
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
 
 	/*public*/ boolean saveCompletedInfo() {
 if (DEPLOYABLE) {
 		/* create jar or zip file of data and events */
 		String filename = null;
 		String filenameA = null;
 		FileOutputStream fos = null;
 		FileOutputStream fosA = null;
 		JarOutputStream jos = null;
 		JarOutputStream josA = null;
 		
 		try {
 			filename = nodes.getReserved(Schedule.COMPLETED_DIR) + nodes.getReserved(Schedule.FILENAME) + ".jar";
 			fos = new FileOutputStream(filename);
 		}
 		catch (FileNotFoundException e) {
 			setError(get("error_writing_to") + filename + ": " + e.getMessage());
 			return false;
 		}
 		catch (SecurityException e) {
 			setError(get("error_writing_to") + filename + ": " + e.getMessage());
 			return false;
 		}
 		
 		try {
 			filenameA = nodes.getReserved(Schedule.FLOPPY_DIR) + nodes.getReserved(Schedule.FILENAME) + ".jar";
 			fosA = new FileOutputStream(filenameA);			
 		}
 		catch (FileNotFoundException e) {
 			setError(get("error_writing_to") + filenameA + ": " + e.getMessage());
 		}
 		catch (SecurityException e) {
 			setError(get("error_writing_to") + filenameA + ": " + e.getMessage());
 		}
 		
 		try {
 			jos = new JarOutputStream(fos);
 		}
 		catch (IOException e) {
 			setError(get("error_writing_to") + filename + ": " + e.getMessage());
 			if (fos != null) { try { fos.close(); } catch (Throwable t) { ; } }
 			return false;
 		}
 		
 		try {
 			if (fosA != null) {
 				josA = new JarOutputStream(fosA);
 			}
 		}
 		catch (IOException e) {
 			setError(get("error_writing_to") + filename + ": " + e.getMessage());
 			if (fosA != null) { try { fosA.close(); } catch (Throwable t) { ; } }
 		}
 		
 		if (!writeData(jos,josA,dataLogger,DATAFILE_SUFFIX,filename))
 			return false;
 		if (!writeData(jos,josA,eventLogger,DATAFILE_SUFFIX + EVENTFILE_SUFFIX,filename))
 			return false;
 			
 		if (jos != null) { try { jos.close(); } catch (Throwable t) { ; } }
 		if (josA != null) { try { josA.close(); } catch (Throwable t) { ; } }
 		deleteDataLoggers();
 }
 		return true;
 	}
 	
 	private boolean writeData(JarOutputStream jos, JarOutputStream josA, Logger logger, String extension, String filename) {
 if (DEPLOYABLE) {
 		String data = null;
 		ZipEntry ze = null;
 		Throwable err = null;
 
 		logger.flush();
 		data = logger.toString();
 		
 		try {
 			ze = new JarEntry(nodes.getReserved(Schedule.FILENAME) + extension);
 		}
 		catch (NullPointerException e) {
 			err = e;
 		}
 		catch (IllegalArgumentException e) {
 			err = e;
 		}
 		
 		try {
 			byte[] bytes = data.getBytes();
 			jos.putNextEntry(ze);
 			jos.write(bytes,0,bytes.length);
 			if (josA != null) {
 				josA.putNextEntry(ze);
 				josA.write(bytes,0,bytes.length);
 			}
 		}
 		catch (UnsupportedEncodingException e) {
 			err = e;
 		}
 		catch (ZipException e) {
 			err = e;
 		}
 		catch (IOException e) {
 			err = e;
 		}
 	
 		if (err != null) {
 			setError(get("error_writing_to") + filename + ": " + err.getMessage());
 			if (jos != null) { try { jos.close(); } catch (Throwable t) { ; } }
 			if (josA != null) { try { josA.close(); } catch (Throwable t) { ; } }
 		}
 		
 		return (err == null);
 }
 		return false;
 	}
 	
 
 	/*public*/ boolean saveToFloppy() {
 		return false;
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
 if (DEBUG)	Logger.writeln("##delete(" + filename + ") -> " + ok);
 		return ok;
 	}
 
 	/*public*/ String getTitle() {
 		return nodes.getReserved(Schedule.TITLE);
 	}
 
 	/*public*/ String getPasswordForAdminMode() {
 		String s = nodes.getReserved(Schedule.PASSWORD_FOR_ADMIN_MODE);
 		if (s == null || s.trim().length() == 0)
 			return null;
 		else
 			return s;
 	}
 
 	/*public*/ String getIcon() { return nodes.getReserved(Schedule.ICON); }
 	/*public*/ String getHeaderMsg() { return nodes.getReserved(Schedule.HEADER_MSG); }
 
 	/*public*/ Datum evaluateExpr(String expr) {
 if (AUTHORABLE) {
 		return parser.parse(this,expr);
 } else { return null; }
 	}
 
 	/*public*/ String getFilename() { return nodes.getReserved(Schedule.FILENAME); }
 
 	/*public*/ boolean setLanguage(String language) {
 		return nodes.setReserved(Schedule.CURRENT_LANGUAGE,language);
 	}
 	/*public*/ int getLanguage() { return nodes.getLanguage(); }
 
 	/*public*/ String createTempPassword() {
 		tempPassword = Long.toString(random.nextLong());
 		return tempPassword;
 	}
 
 	/*public*/ boolean isTempPassword(String s) {
 		String temp = tempPassword;
 		createTempPassword();	// reset it
 
 		if (s == null)
 			return false;
 		return s.equals(temp);
 	}
 
 	private void setError(String s) { 
 if (DEBUG) Logger.writeln("##" + s);		
 		errorLogger.println(s); 
 	}
 	/*public*/ boolean hasErrors() { return (errorLogger.size() > 0); }
 	/*public*/ String getErrors() { return errorLogger.toString(); }
 
 	/*public*/ Schedule getSchedule() { return nodes; }
 	/*public*/ Evidence getEvidence() { return evidence; }
 	/*public*/ Parser getParser() { return parser; }
 
 	/*public*/ boolean isAtBeginning() { return (currentStep <= firstStep); }
 	/*public*/ boolean isAtEnd() { return (currentStep >= size()); }
 	/*public*/ int getCurrentStep() { return currentStep; }
 
 	/*public*/ void processEventTimings(String src) {
 if (DEPLOYABLE) {		
 		if (src == null) {
 			return;
 		}
 
 		StringTokenizer lines = new StringTokenizer(src,"|",false);
 
 		while(lines.hasMoreTokens()) {
 			String s = lines.nextToken();
 			eventLogger.println(s);
 		}
 		eventLogger.flush();	// so that committed to disk
 }		
 	}
 
 	/* Formerly from Lingua */
 
 	/*public*/ static Locale getLocale(String lang, String country, String extra) {
 		return new Locale((lang == null) ? "" : lang,
 			(country == null) ? "" : country,
 			(extra == null) ? "" : extra);
 	}
 
 	/*public*/ void setLocale(Locale loc) {
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
 
 	/*public*/ String get(String localizeThis) {
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
		dateFormats.put(key,sdf);
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
			numFormats.put(key,df);
 			return df;
 		}
 	}
 
 	/*public*/ Number parseNumber(Object obj, String mask) {
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
 
 	/*public*/ Date parseDate(Object obj, String mask) {
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
 
 	/*public*/ boolean parseBoolean(Object obj) {
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
 
 
 	/*public*/ String formatNumber(Object obj, String mask) {
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
 
 	/*public*/ String formatDate(Object obj, String mask) {
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
