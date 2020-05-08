 import java.lang.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 /**
  * Schedule holds a collection of nodes.
 */
 public class Schedule  {
 	public static final int LANGUAGES = 0;
 	public static final int TITLE = 1;
 	public static final int ICON = 2;
 	public static final int HEADER_MSG = 3;
 	public static final int STARTING_STEP = 4;
 	public static final int PASSWORD_FOR_ADMIN_MODE = 5;
 	public static final int SHOW_QUESTION_REF = 6;
 	public static final int AUTOGEN_OPTION_NUM = 7;
 	public static final int DEVELOPER_MODE = 8;
 	public static final int DEBUG_MODE = 9;
 	public static final int START_TIME = 10;
 	public static final int FILENAME = 11;
 	public static final int SHOW_ADMIN_ICONS = 12;
 	public static final int TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS = 13;
 	public static final int ALLOW_COMMENTS = 14;
 	public static final int SCHEDULE_SOURCE = 15;
 	public static final int LOADED_FROM = 16;
 	public static final int CURRENT_LANGUAGE = 17;
 	public static final int ALLOW_LANGUAGE_SWITCHING = 18;
 	
 	private static final String DEFAULT_LANGUAGE = "en_US";
 
 	public static final String[] RESERVED_WORDS = {
 		"__LANGUAGES__",
 		"__TITLE__",
 		"__ICON__",
 		"__HEADER_MSG__",
 		"__STARTING_STEP__",
 		"__PASSWORD_FOR_ADMIN_MODE__",
 		"__SHOW_QUESTION_REF__",
 		"__AUTOGEN_OPTION_NUM__",
 		"__DEVELOPER_MODE__",
 		"__DEBUG_MODE__",
 		"__START_TIME__",
 		"__FILENAME__",
 		"__SHOW_ADMIN_ICONS__",
 		"__TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS__",
 		"__ALLOW_COMMENTS__",
 		"__SCHEDULE_SOURCE__",
 		"__LOADED_FROM__",
 		"__CURRENT_LANGUAGE__",
 		"__ALLOW_LANGUAGE_SWITCHING__",
 	};
 
 	private Date startTime = null;
 	private int languageCount = 0;
 	private Vector locales = null;
 	private Vector languageNames = null;
 	private int currentLanguage = 0;
 	private boolean isFound = false;
 	private boolean isLoaded = false;
 	private String source = null;
 	private Logger errorLogger = new Logger();
 
 	private Vector nodes = null;
 	private Vector comments = null;
 	private Hashtable reserved = new Hashtable();
 	private Lingua lingua = Lingua.NULL;
 
 	public Schedule(Lingua lang, String source) {
     	lingua = (lang == null) ? Lingua.NULL : lang;
     	
 		setDefaultReserveds();
 
 		setReserved(SCHEDULE_SOURCE,source);	// this defaults to LOADED_FROM, but want to keep track of the original source location
 
 		if (load(source,false)) {
 			isFound = true;
 		}
 	}
 
 	public boolean isFound() { return isFound; }
 	public boolean isLoaded() { return isLoaded; }
 	public String getScheduleSource() { return ((isFound) ? getReserved(SCHEDULE_SOURCE) : ""); }
 	public String getLoadedFrom() { return ((isFound) ? getReserved(LOADED_FROM) : ""); }
 
 	private void setDefaultReserveds() {
 		setReserved(TITLE,"Triceps");
 		setReserved(STARTING_STEP,"0");
 		setReserved(START_TIME,lingua.formatDate(new Date(System.currentTimeMillis()),Datum.TIME_MASK));
 		setReserved(PASSWORD_FOR_ADMIN_MODE,"");
 		setReserved(AUTOGEN_OPTION_NUM,"true");
 		setReserved(FILENAME,getReserved(START_TIME));
 		setReserved(ICON,"");
 		setReserved(HEADER_MSG,"Triceps");
 		setReserved(SHOW_QUESTION_REF,"false");
 		setReserved(DEVELOPER_MODE,"false");
 		setReserved(DEBUG_MODE,"false");
 		setReserved(LANGUAGES,DEFAULT_LANGUAGE);
 		setReserved(CURRENT_LANGUAGE,null);
 		setReserved(SHOW_ADMIN_ICONS,"false");
 		setReserved(TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS,"");	// default is unnamed until initialized
 		setReserved(ALLOW_COMMENTS,"false");
 		setReserved(SCHEDULE_SOURCE,"");
 		setReserved(LOADED_FROM,"");
 		setReserved(ALLOW_LANGUAGE_SWITCHING,"true");
 	}
 
 	public boolean init() {
 		nodes = new Vector();
 		comments = new Vector();
 		isLoaded = load(getLoadedFrom(),true);
 		return isLoaded;
 	}
 
 	public boolean reload() {
 		return init();
 	}
 
 	private boolean load(String source, boolean parseNodes) {
 		BufferedReader br = getReader(source);
 		if (br == null)
 			return false;
 
 		boolean err = false;
 
 		try {
 			int line = 0;
 			int count=0;
 			int reservedCount=0;
 			String fileLine;
 
 			while ((fileLine = br.readLine()) != null) {
 				++line;
 				if (fileLine.trim().equals(""))
 					continue;
 
 				if (fileLine.startsWith("COMMENT")) {
 					if (parseNodes) {
 						comments.addElement(fileLine);
 					}
 					continue;
 				}
 				if (fileLine.startsWith("RESERVED")) {
 					if (parseReserved(line, source, fileLine)) {
 						++reservedCount;
 					}
 					continue;
 				}
 
 				if (!parseNodes)
 					break;	// so that only set RESERVED values
 
 				if (reservedCount == 0) {
 					return false;	// must have some reserved lines before nodes, else can't be a schedule file
 				}
 
 				Node node = new Node(lingua, line, source, fileLine, languageCount);
 				++count;
 				nodes.addElement(node);
 			}
 			if (reservedCount == 0) {
 				return false;
 			}
 //			if (parseNodes) {
 //				setError("Read " + count + " nodes from " + source);
 //			}
 
 			/* check for mismatching braces */
 			int braceLevel = 0;
 			Node node = null;
 			for (int i=0;i<count;++i) {
 				node = getNode(i);
 				if (node == null) {
 					setError(lingua.get("null_node_at_index") + i);
 					continue;
 				}
 				switch(node.getQuestionOrEvalType()) {
 					case Node.QUESTION:
 						break;
 					case Node.EVAL:
 						if (braceLevel > 0)
 							setError(lingua.get("evaluations_disallowed_within_block") + node.getSourceLine());
 						break;
 					case Node.GROUP_OPEN:
 						if (++braceLevel > 1) {
 							setError(lingua.get("starting_nested_group_at_line") + node.getSourceLine());
 						}
 						break;
 					case Node.GROUP_CLOSE:
 						if (--braceLevel < 0) {
 							setError(lingua.get("extra_closing_brace_at_line") + node.getSourceLine());
 						}
 						break;
 					case Node.BRACE_OPEN:
 					case Node.BRACE_CLOSE:
 					case Node.CALL_SCHEDULE:
 						setError(node.getQuestionOrEvalTypeField() + lingua.get("not_yet_suppported___line") + node.getSourceLine());
 						break;
 					default:
 						setError(lingua.get("unknown_actionType_at_line") + node.getSourceLine());
 						break;
 				}
 			}
 			if (braceLevel > 0) {
 				setError(lingua.get("missing") + braceLevel + lingua.get("closing_braces"));
 			}
 		}
 		catch(IOException e) { }
 		if (br != null) {
 			try { br.close(); } catch (IOException t) { }
 		}
 		setReserved(START_TIME,lingua.formatDate(startTime,Datum.TIME_MASK));	// XXX is this really needed?
 
 		String s = getReserved(TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS);
 		if (s == null || s.trim().length() == 0) {
 			// set a reasonable default value
 			setReserved(TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS,getReserved(TITLE) + " [" + getReserved(START_TIME) + "]");
 		}
 		setReserved(LOADED_FROM,source);	// keep LOADED_FROM up to date
 
 		return (!err);
 	}
 
 	public Node getNode(int index) {
 		if (index < 0) {
 			setError("Node[" + index + "] does not exist");
 			return null;
 		}
 		if (index > size()) {
 			setError("Node[" + index + "/" + size() + "] does not exist");
 			return null;
 		}
 		return (Node)nodes.elementAt(index);
 	}
 
 	public int size() {
 		return nodes.size();
 	}
 
 	private boolean parseReserved(int line, String filename, String fileLine) {
 		StringTokenizer ans = new StringTokenizer(fileLine,"\t",true);
 		int field = 0;
 		String name=null;
 		String value=null;
 		boolean ok = false;
 
 		while(ans.hasMoreTokens()) {
 			String s = null;
 			s = ans.nextToken();
 
 			if (s.equals("\t")) {
 				++field;
 				continue;
 			}
 
 			switch(field) {
 				case 0: break;
 				case 1: name = Node.EMPTY_NODE.fixExcelisms(s); break;
 				case 2: value = Node.EMPTY_NODE.fixExcelisms(s); break;
 				default: break;
 			}
 		}
 		if (field < 2) {
 			setError(lingua.get("incorrect_syntax_for") + ((name != null) ? name : "") + " [" + filename + "(" + line + ")]");
 		}
 		if (name == null || value == null)
 			return false;
 
 		int resIdx=-1;
 		for (int i=0;i<RESERVED_WORDS.length;++i) {
 			if (RESERVED_WORDS[i].equals(name)) {
 				resIdx = i;
 				break;
 			}
 		}
 
 		if (!setReserved(resIdx, value)) {
 			setError(name + lingua.get("not_recognized") + filename + "(" + line + ")]");
 			return false;
 		}
 		return true;
 	}
 
 	public boolean overloadReserved(Schedule oldNodes) {
 		/* FIXME - need to use String Objects for reserved words, not integers, otherwise subject to re-ordering! */
 		if (nodes == null)
 			return false;
 		boolean ok = true;
 
 		for (int i=0;i<RESERVED_WORDS.length;++i) {
 			ok = setReserved(i,oldNodes.getReserved(i)) && ok;
 		}
 		return ok;
 	}
 
 	public boolean setReserved(int resIdx, String value) {
 		String s;
 		if (value == null)
 			value = "";
 		switch (resIdx) {
 			case TITLE: s = value; break;
 			case STARTING_STEP: s = setStartingStep(value); break;
 			case START_TIME: s = setStartTime(value); break;
 			case PASSWORD_FOR_ADMIN_MODE: s = value; break;
 			case AUTOGEN_OPTION_NUM: s = Boolean.valueOf(value.trim()).toString(); break;
 			case FILENAME: s = value; break;
 			case ICON: s = value; break;
 			case HEADER_MSG: s = value; break;
 			case SHOW_QUESTION_REF: s = Boolean.valueOf(value.trim()).toString(); break;
 			case DEVELOPER_MODE: s = Boolean.valueOf(value.trim()).toString(); break;
 			case DEBUG_MODE: s = Boolean.valueOf(value.trim()).toString(); break;
 			case LANGUAGES: s = setLanguages(value); break;
 			case SHOW_ADMIN_ICONS: s = Boolean.valueOf(value.trim()).toString(); break;
 			case TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS: s = value; break;
 			case ALLOW_COMMENTS: s = Boolean.valueOf(value.trim()).toString(); break;
 			case SCHEDULE_SOURCE: s = value; break;
 			case LOADED_FROM: s = value; break;
 			case CURRENT_LANGUAGE: s = setLanguage(value.trim()); break;
 			case ALLOW_LANGUAGE_SWITCHING: s = Boolean.valueOf(value.trim()).toString(); break;
 			default: return false;
 		}
 		if (s != null) {
 			reserved.put(RESERVED_WORDS[resIdx], s);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	public String getReserved(int resIdx) {
 		if (resIdx >= 0 && resIdx < RESERVED_WORDS.length) {
 			return (String) reserved.get(RESERVED_WORDS[resIdx]);
 		}
 		else
 			return null;
 	}
 
 	private String setStartingStep(String s) {
 		Integer startingStep = null;
 		try {
 			startingStep = new Integer(s);
 		}
 		catch(NumberFormatException e) {
 			setError(lingua.get("invalid_number_for_starting_step") + e.getMessage());
 			startingStep = new Integer(0);
 		}
 		return startingStep.toString();
 	}
 
 	private String setStartTime(Date t) {
 		startTime = t;
 		String str = lingua.formatDate(t,Datum.TIME_MASK);
 		return str;
 	}
 
 	private String setStartTime(String t) {
 		Date time = null;
 		time = lingua.parseDate(t,Datum.TIME_MASK);
 		if (time == null) {
 			time = new Date(System.currentTimeMillis());
 		}
 		return setStartTime(time);
 	}
 
 	private String setLanguages(String value) {
 		StringBuffer sb = new StringBuffer();
 		if (value == null) {
 			return setLanguages(DEFAULT_LANGUAGE);
 		}
 
 		languageCount = 0;
 		locales = new Vector();
 		languageNames = new Vector();
 		StringTokenizer ans = new StringTokenizer(value,"|");
 		while(ans.hasMoreTokens()) {
 			String s = null;
 			try {
 				s = ans.nextToken();
 				if (s == null || s.trim().length() == 0)
 					continue;
 				s = s.trim();
 				
 			}
 			catch (NoSuchElementException e) {
 			}
 			/* regenerate the string, stipping excess pipe characters */
 			++languageCount;
 			if (sb.length() != 0) {
 				sb.append("|" + s);
 			}
 			else {
 				sb.append(s);
 			}
 			
 			String lang = null;
 			String country = null;
 			String extra = null;
 			
 			try {
 				StringTokenizer part = new StringTokenizer(s,"_");
 				lang = part.nextToken();
 				country = part.nextToken();
 				extra = part.nextToken();
 			}
 			catch (NoSuchElementException e) { }
 			
 			Locale loc = Lingua.getLocale(lang,country,extra);
 				
 			locales.addElement(loc);
 		}
 		buildLanguageNames();
 		
 		return sb.toString();
 	}
 	
 	private void buildLanguageNames() {
 		languageNames = new Vector();
 		
 		for (int i=0;i<locales.size();++i) {
 			Locale loc = (Locale) locales.elementAt(i);
 			languageNames.addElement(loc.getDisplayLanguage(loc));
 		}
 	}
 
 	public Vector getLanguages() { return languageNames; }
 
 	public String setLanguage(String s) {
		if (s != null) {
 			for (int i=0;i<languageNames.size();++i) {
 				if (s.equals((String) languageNames.elementAt(i))) {
 					currentLanguage = i;
 					Locale loc = (Locale) locales.elementAt(i);
 					lingua.setLocale(loc);
 					return loc.toString();
 				}
 			}
 			for (int i=0;i<locales.size();++i) {
 				Locale loc = (Locale) locales.elementAt(i);
 				if (s.equals(loc.toString())) {
 					currentLanguage = i;
 					lingua.setLocale(loc);
 					return loc.toString();
 				}
 			}			
 			setError(lingua.get("tried_to_switch_to_unsupported_language") + s);
 		}
		else {
			currentLanguage = 0;
			lingua.setLocale((Locale) locales.elementAt(currentLanguage));
		}
 		return ((Locale) locales.elementAt(currentLanguage)).toString();
 	}
 
 	public int getLanguage() { return currentLanguage; }
 
 	public void toTSV(Writer out) {
 		Enumeration keys = reserved.keys();
 
 		for (int i=0;i<RESERVED_WORDS.length;++i) {
 			String s = RESERVED_WORDS[i];
 			try {
 				out.write("RESERVED\t" + s + "\t" + reserved.get(s).toString() + "\n");
 			}
 			catch (IOException e) {
 				setError(lingua.get("error_writing_to") + out + ": " + e.getMessage());
 			}
 		}
 
 		for (int i=0;i<comments.size();++i) {
 			try {
 				out.write((String) comments.elementAt(i) + "\n");
 			}
 			catch (IOException e) {
 				setError(lingua.get("error_writing_to") + out + ": " + e.getMessage());
 			}
 		}
 	}
 
 	private BufferedReader getReader(String source) {
 		boolean ok = false;
 		BufferedReader br = null;
 
 		/* First try to access the source as a file */
 		try {
 			if (source != null) {
 				File file = new File(source);
 				if (!file.exists()) {
 					setError(lingua.get("error_file") + " '" + source + "' " + lingua.get("does_not_exist"));
 				}
 				else if (!file.isFile()) {
 					setError(lingua.get("error_file") + " '" + source + "' " + lingua.get("is_not_a_file"));
 				}
 				else if (!file.canRead()) {
 					setError(lingua.get("error_file") + " '" + source + "' " + lingua.get("is_not_accessible"));
 				}
 				else {
 					br = new BufferedReader(new FileReader(file));
 					ok = true;
 				}
 			}
 			else {
 				setError(lingua.get("error_null_filename"));
 			}
 		}
 		catch (IOException e) {
 			setError(lingua.get("error_accessing_file") + e.getMessage());
 			Logger.printStackTrace(e);
 		}
 		if (ok) {
 			return br;
 		}
 		else {
 			if (br != null) {
 				try { br.close(); } catch (IOException t) { }
 			}
 		}
 
 /* Shows how URLs can be accessed.  No longer supported.
 		// Is source a URL pointing to a file? If so, try reading from it
 		try {
 			URL url = new URL(source);
 			br = new BufferedReader(new InputStreamReader(url.openStream()));
 
 			String fileLine;
 			while ((fileLine = br.readLine()) != null) {
 				fileLine = fileLine.trim();
 				if (fileLine.equals(""))
 					continue;
 
 				// If this is an HTML page instead of a text file, then an error has occurred
 				if (fileLine.startsWith("<")) {
 					setError("unable to access " + url.toExternalForm());
 					break;
 				}
 				else {
 					// Close, then re-open the stream (resetting a BufferedReader isn't supported on all platforms)
 					if (br != null) {
 						try { br.close(); } catch (IOException t) { }
 					}
 					br = new BufferedReader(new InputStreamReader(url.openStream()));
 					ok = true;
 					break;
 				}
 			}
 		}
 		catch (MalformedURLException t) {
 		}
 		catch (Throwable t) {
 			setError("can't access as url: " + t.getMessage());
 		}
 		if (ok) {
 			return br;
 		}
 		else {
 			if (br != null) {
 				try { br.close(); } catch (IOException t) { }
 			}
 		}
 */
 		return null;
 	}
 
 	private void setError(String s) { errorLogger.println(s); }
 	public boolean hasErrors() { return (errorLogger.size() > 0); }
 	public String getErrors() { return errorLogger.toString(); }
 }
