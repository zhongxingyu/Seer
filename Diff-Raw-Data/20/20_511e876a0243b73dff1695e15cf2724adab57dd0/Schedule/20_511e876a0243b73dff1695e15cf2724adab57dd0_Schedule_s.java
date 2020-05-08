 package org.dianexus.triceps;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Vector;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.NoSuchElementException;
 import java.util.Locale;
 import java.util.logging.*;
 import org.dialogix.timing.DialogixTimingCalculator;
//import org.dialogix.timing.DialogixV1TimingCalculator;
 
 class Schedule implements VersionIF {
 
     private static final String LoggerName = "org.dianexus.triceps.Schedule";
     static final int LANGUAGES = 0;
     static final int TITLE = 1;
     static final int ICON = 2;
     static final int HEADER_MSG = 3;
     static final int STARTING_STEP = 4;
     static final int PASSWORD_FOR_ADMIN_MODE = 5;
     static final int SHOW_QUESTION_REF = 6;
     static final int AUTOGEN_OPTION_NUM = 7;
     static final int DEVELOPER_MODE = 8;
     static final int DEBUG_MODE = 9;
     static final int START_TIME = 10;
     static final int FILENAME = 11;
     static final int SHOW_ADMIN_ICONS = 12;
     static final int TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS = 13;
     static final int ALLOW_COMMENTS = 14;
     static final int SCHEDULE_SOURCE = 15;
     static final int LOADED_FROM = 16;
     static final int CURRENT_LANGUAGE = 17;
     static final int ALLOW_LANGUAGE_SWITCHING = 18;
     static final int ALLOW_REFUSED = 19;
     static final int ALLOW_UNKNOWN = 20;
     static final int ALLOW_DONT_UNDERSTAND = 21;
     static final int RECORD_EVENTS = 22;
     static final int WORKING_DIR = 23;
     static final int COMPLETED_DIR = 24;
     static final int FLOPPY_DIR = 25;
     static final int IMAGE_FILES_DIR = 26;
     static final int COMMENT_ICON_ON = 27;
     static final int COMMENT_ICON_OFF = 28;
     static final int REFUSED_ICON_ON = 29;
     static final int REFUSED_ICON_OFF = 30;
     static final int UNKNOWN_ICON_ON = 31;
     static final int UNKNOWN_ICON_OFF = 32;
     static final int DONT_UNDERSTAND_ICON_ON = 33;
     static final int DONT_UNDERSTAND_ICON_OFF = 34;
     static final int TRICEPS_VERSION_MAJOR = 35;
     static final int TRICEPS_VERSION_MINOR = 36;
     static final int SCHED_AUTHORS = 37;
     static final int SCHED_VERSION_MAJOR = 38;
     static final int SCHED_VERSION_MINOR = 39;
     static final int SCHED_HELP_URL = 40;
     static final int HELP_ICON = 41;
     static final int ACTIVE_BUTTON_PREFIX = 42;
     static final int ACTIVE_BUTTON_SUFFIX = 43;
     static final int TRICEPS_FILE_TYPE = 44;
     static final int DISPLAY_COUNT = 45;
     static final int SCHEDULE_DIR = 46;
     static final int ALLOW_JUMP_TO = 47;
     static final int BROWSER_TYPE = 48;
     static final int IP_ADDRESS = 49;
     static final int SUSPEND_TO_FLOPPY = 50;
     static final int JUMP_TO_FIRST_UNASKED = 51;
     static final int REDIRECT_ON_FINISH_URL = 52;
     static final int REDIRECT_ON_FINISH_MSG = 53;
     static final int SWAP_NEXT_AND_PREVIOUS = 54;
     static final int ANSWER_OPTION_FIELD_WIDTH = 55;
     static final int SET_DEFAULT_FOCUS = 56;
     static final int ALWAYS_SHOW_ADMIN_ICONS = 57;
     static final int SHOW_SAVE_TO_FLOPPY_IN_ADMIN_MODE = 58;
     static final int WRAP_ADMIN_ICONS = 59;
     static final int DISALLOW_COMMENTS = 60;
     static final int CONNECTION_TYPE = 61;
     static final int REDIRECT_ON_FINISH_DELAY = 62;
     static final int MAX_TEXT_LEN_FOR_COMBO = 63;
     private static final String DEFAULT_LANGUAGE = "en";
     static final String TRICEPS_DATA_FILE = "DATA";
     static final String TRICEPS_SCHEDULE_FILE = "SCHEDULE";
     static final String TRICEPS_UNKNOWN_FILE = "UNKNOWN";
     static final String[] RESERVED_WORDS = {
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
         "__ALLOW_REFUSED__",
         "__ALLOW_UNKNOWN__",
         "__ALLOW_DONT_UNDERSTAND__",
         "__RECORD_EVENTS__",
         "__WORKING_DIR__",
         "__COMPLETED_DIR__",
         "__FLOPPY_DIR__",
         "__IMAGE_FILES_DIR__",
         "__COMMENT_ICON_ON__",
         "__COMMENT_ICON_OFF__",
         "__REFUSED_ICON_ON__",
         "__REFUSED_ICON_OFF__",
         "__UNKNOWN_ICON_ON__",
         "__UNKNOWN_ICON_OFF__",
         "__DONT_UNDERSTAND_ICON_ON__",
         "__DONT_UNDERSTAND_ICON_OFF__",
         "__TRICEPS_VERSION_MAJOR__",
         "__TRICEPS_VERSION_MINOR__",
         "__SCHED_AUTHORS__",
         "__SCHED_VERSION_MAJOR__",
         "__SCHED_VERSION_MINOR__",
         "__SCHED_HELP_URL__",
         "__HELP_ICON__",
         "__ACTIVE_BUTTON_PREFIX__",
         "__ACTIVE_BUTTON_SUFFIX__",
         "__TRICEPS_FILE_TYPE__",
         "__DISPLAY_COUNT__",
         "__SCHEDULE_DIR__",
         "__ALLOW_JUMP_TO__",
         "__BROWSER_TYPE__",
         "__IP_ADDRESS__",
         "__SUSPEND_TO_FLOPPY__",
         "__JUMP_TO_FIRST_UNASKED__",
         "__REDIRECT_ON_FINISH_URL__",
         "__REDIRECT_ON_FINISH_MSG__",
         "__SWAP_NEXT_AND_PREVIOUS__",
         "__ANSWER_OPTION_FIELD_WIDTH__",
         "__SET_DEFAULT_FOCUS__",
         "__ALWAYS_SHOW_ADMIN_ICONS__",
         "__SHOW_SAVE_TO_FLOPPY_IN_ADMIN_MODE__",
         "__WRAP_ADMIN_ICONS__",
         "__DISALLOW_COMMENTS__",
         "__CONNECTION_TYPE__",
         "__REDIRECT_ON_FINISH_DELAY__",
         "__MAX_TEXT_LEN_FOR_COMBO__"
     ,
 	    };
 
     private Date startTime = null;
     private int languageCount = 0;
     private Vector<Locale> locales = null;
     private Vector<String> languageNames = null;
     private Vector<String> languagesISO = null;
     private int currentLanguage = 0;
     private boolean isFound = false;
     private boolean isLoaded = false;
     private StringBuffer errorLogger = new StringBuffer();
     private Vector<Node> nodes = new Vector<Node>();	// formerly null
     private HashMap<String, String> reserved = new HashMap<String, String>();
     private Triceps triceps = null;
     private Evidence evidence = null;
 //    private ScheduleSource scheduleSource = null;
 //    private boolean isDatafile = false;
 //    static final Schedule NULL = new Schedule(null, null);  // CONCURRENCY RISK?: YES
 
     Schedule(Triceps lang,
              String src,
              boolean isRestore) {
         triceps = (lang == null) ? null : lang; // FIXME - used to be Triceps.NULL
         evidence = (lang == null) ? null : triceps.getEvidence(); // FIXME - used to be new Evidence(null)
 
         setReserved(LANGUAGES, DEFAULT_LANGUAGE);	// needed for language changing
         setDefaultReserveds();
         setReserved(SCHEDULE_SOURCE, src);	// this defaults to LOADED_FROM, but want to keep track of the original source location
 
         if (lang != null) {
             if (src.matches("^\\d+$")) {
                 /* Load this from database instead of from file */
                 if (DB_LOG_FULL) {
                   Long instrumentOrSessionID = null;
                     try {
                         instrumentOrSessionID = Long.parseLong(src);
                         if (instrumentOrSessionID != null) {
                             DialogixTimingCalculator dtc = new DialogixTimingCalculator(instrumentOrSessionID,isRestore, getReserveds());
                             if (dtc.isInitialized()) {
                                 triceps.setDtc(dtc);   
                                 setReserved(LOADED_FROM, src);  // FIXME - this isn't right if it is restoring a session.  Does it matter?
                                 setReserved(STARTING_STEP, dtc.getCurrentVarNum());
 //                                logger.log(Level.FINE, "Loaded instrument from database - version " + src);                        
                                 isFound = true;
                             }
                         }                        
                     } catch (Exception e) {
                         setError(triceps.get("unable_to_find_or_access_schedule") + " \'" + src + "\'");
                     }                    
                 }
             } else {
 //                scheduleSource = ScheduleSource.getInstance(src);
 //                if (scheduleSource.isValid() && parseHeaders(src)) {
 //                    // LOADED_FROM used to by ScheduleList to know from where to load the selected file
 //                    setReserved(LOADED_FROM, src);
 //
 //                    logger.log(Level.FINE, "Loaded instrument from " + src);
 //                    isFound = true;
 //                }                
             }
         } else if (src != null) {
             setError("Unable to load instrument from " + src);
         }
     }
 
     boolean isFound() {
         return isFound;
     }
 
     boolean isLoaded() {
         return isLoaded;
     }
 
     String getScheduleSource() {
         return ((isFound) ? getReserved(SCHEDULE_SOURCE) : "");
     }
 
     String getLoadedFrom() {
         return ((isFound) ? getReserved(LOADED_FROM) : "");
     }
 
     private boolean loadScheduleFromDB() {
         if (!triceps.getDtc().isInitialized()) {
             return false;
         }
         String sourceContents = triceps.getDtc().getInstrumentAsSpreadsheet();
         String source = triceps.getDtc().getInstrumentTitle();
         String[] lines = sourceContents.split("\n");
         String line = null;
         int linenum = 1;
         boolean ok = false;
         
         for (int i = 0; i < lines.length; ++i, ++linenum) {
             line = lines[i];
             if (line.startsWith("COMMENT")) {
                 continue;
             }
             else if (line.startsWith("RESERVED")) {
                 ok = parseReserved(linenum, 0, source, line);
             }
             else {
                 Node node = new Node(triceps, linenum, source, line, languageCount);
                 nodes.addElement(node);                
                 ok = !node.hasParseErrors();
             }
             if (!AUTHORABLE) {
                 if (!ok) {
                     return false;
                 }
             }
         }
 
         /* once schedule is loaded, set initial values */
         evidence.init(); //
         return true;
     }
 
     private void setDefaultReserveds() {
         setReserved(TRICEPS_FILE_TYPE, TRICEPS_UNKNOWN_FILE);
         setReserved(TITLE, VERSION_NAME);
         setReserved(STARTING_STEP, "0");
         // START_TIME and *_DIR must preceed FILENAME, which uses the values from each of those //
         setReserved(START_TIME, null);
         setReserved(WORKING_DIR, null);
         setReserved(COMPLETED_DIR, null);
         setReserved(FLOPPY_DIR, null);
         setReserved(FILENAME, getReserved(START_TIME));	// sets the default value
         setReserved(PASSWORD_FOR_ADMIN_MODE, "");
         setReserved(AUTOGEN_OPTION_NUM, "true");
         setReserved(ICON, "");
         setReserved(HEADER_MSG, LICENSE_MSG);
         setReserved(SHOW_QUESTION_REF, "false");
         setReserved(DEVELOPER_MODE, "false");
         setReserved(DEBUG_MODE, "false");
         setReserved(LANGUAGES, DEFAULT_LANGUAGE);
         setReserved(CURRENT_LANGUAGE, null);
         setReserved(SHOW_ADMIN_ICONS, "false");
         setReserved(TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS, "");	// default is unnamed until initialized
         setReserved(ALLOW_COMMENTS, "false");
         setReserved(SCHEDULE_SOURCE, "");
         setReserved(LOADED_FROM, "");
         setReserved(ALLOW_LANGUAGE_SWITCHING, "true");
         setReserved(ALLOW_REFUSED, "true");
         setReserved(ALLOW_UNKNOWN, "true");
         setReserved(ALLOW_DONT_UNDERSTAND, "true");
         setReserved(RECORD_EVENTS, "true");
         setReserved(IMAGE_FILES_DIR, null);
         setReserved(COMMENT_ICON_ON, "comment_true.gif");
         setReserved(COMMENT_ICON_OFF, "comment_false.gif");
         setReserved(REFUSED_ICON_ON, "refused_true.gif");
         setReserved(REFUSED_ICON_OFF, "refused_false.gif");
         setReserved(UNKNOWN_ICON_ON, "unknown_true.gif");
         setReserved(UNKNOWN_ICON_OFF, "unknown_false.gif");
         setReserved(DONT_UNDERSTAND_ICON_ON, "not_understood_true.gif");
         setReserved(DONT_UNDERSTAND_ICON_OFF, "not_understood_false.gif");
         setReserved(TRICEPS_VERSION_MAJOR, VERSION_MAJOR);
         setReserved(TRICEPS_VERSION_MINOR, VERSION_MINOR);
         setReserved(SCHED_AUTHORS, null);
         setReserved(SCHED_VERSION_MAJOR, null);
         setReserved(SCHED_VERSION_MINOR, null);
         setReserved(SCHED_HELP_URL, null);
         setReserved(HELP_ICON, "help_true.gif");
         setReserved(ACTIVE_BUTTON_PREFIX, "<<");
         setReserved(ACTIVE_BUTTON_SUFFIX, ">>");
         setReserved(DISPLAY_COUNT, "0");
         setReserved(SCHEDULE_DIR, "");
         setReserved(ALLOW_JUMP_TO, "false");
         setReserved(BROWSER_TYPE, null);
         setReserved(IP_ADDRESS, null);
         setReserved(SUSPEND_TO_FLOPPY, "false");
         setReserved(JUMP_TO_FIRST_UNASKED, "false");
         setReserved(REDIRECT_ON_FINISH_URL, "");
         setReserved(REDIRECT_ON_FINISH_MSG, "");
         setReserved(SWAP_NEXT_AND_PREVIOUS, "false");
         setReserved(ANSWER_OPTION_FIELD_WIDTH, "0");
         setReserved(SET_DEFAULT_FOCUS, "true");
         setReserved(ALWAYS_SHOW_ADMIN_ICONS, "false");
         setReserved(SHOW_SAVE_TO_FLOPPY_IN_ADMIN_MODE, "false");
         setReserved(WRAP_ADMIN_ICONS, "false");
         setReserved(DISALLOW_COMMENTS, "false");
         setReserved(CONNECTION_TYPE, "HTTP");
         setReserved(REDIRECT_ON_FINISH_DELAY, "3");
         setReserved(MAX_TEXT_LEN_FOR_COMBO, "60");
     }
 
     boolean init(boolean log, boolean isRestore) {
         boolean ok = init2(log, isRestore);
 //        logger.log(Level.FINE, "##@@Schedule.load(" + getReserved(SCHEDULE_SOURCE) + ")-> " + ((ok) ? "SUCCESS" : "FAILURE"));
         if (!ok) {
             setError(triceps.get("unable_to_find_or_access_schedule") + " \'" + getReserved(SCHEDULE_SOURCE) + "\'");
         }
         return ok;
     }
 
     private boolean init2(boolean log, boolean isRestore) {
         nodes = new Vector<Node>();
         evidence.createReserved();
         evidence.initReserved();
 
         if (!load()) {
             return false;
         }
         if (!bracesMatch()) {
             return false;
         }
 
         if (!log) {
             isLoaded = true;
             return true;	// reloading schedule, so don't need or want to reset values
         }
 
         if (isRestore == true) {
             /* Instrument has been loaded: re-load the values */
             HashMap<String,String> currentValues = triceps.getDtc().getCurrentValues();
             ArrayList<String> reserveds = new ArrayList<String>();
             for (int i=0;i< RESERVED_WORDS.length; ++i) {
                 reserveds.add(RESERVED_WORDS[i]);
             }
             Iterator<String> keys = currentValues.keySet().iterator();
             while (keys.hasNext()) {
                 String key = keys.next();
                 String value = currentValues.get(key);
                 
                 int idx = reserveds.lastIndexOf(key);
                 if (idx >= 0) {
                     /* then this is a reserved word */
                     setReserved(idx, value, true, false);
                     continue;
                 }
                 /* Set node value */
                 if (!evidence.containsKey(key)) {
                    continue;   // FIXME - will this be the reserved values?
                 }
                 Node node = evidence.getNode(key);
                 if (node == null) {
                     continue;
                 }
                 Datum datum;
                 if (value == null) {
                     datum = new Datum(Datum.INVALID, triceps);
                 } 
                 else if (value.equals("*UNASKED*")) {
                     datum = new Datum(Datum.UNASKED, triceps);
                 }
                 else {
                     datum = new Datum(triceps, value, Datum.STRING);
                 }
                 evidence.set(node, datum, null, false);    // set the value in Evidence, but don't re-write it to the database
             }
         }
         else {
             if (!prepareDataLogging()) {
                 return false;
             }
         }
         isLoaded = true;
 
         setReserved(START_TIME, Long.toString(System.currentTimeMillis()));
 
         if (getReserved(FILENAME) == null) {
             setReserved(FILENAME, getReserved(START_TIME));	// sets the default value
         }
 //        setReserved(CURRENT_LANGUAGE, getReserved(CURRENT_LANGUAGE));	// sets default language nowthat loaded		
 
         return true;
     }
 
 //    private boolean parseHeaders(String source) {
 //        int reservedCount = 0;
 //        Vector<String> lines = null;
 //        String line = null;
 //        int linenum = 1;
 //        int nodeCount = 0;
 //
 //        lines = scheduleSource.getHeaders();
 //        for (int i = 0; i < lines.size(); ++i, ++linenum) {
 //            line = (String) lines.elementAt(i);
 //            if (!line.startsWith("RESERVED")) {
 //                continue;
 //            }
 //            if (parseReserved(linenum, nodeCount, source, line)) {
 //                ++reservedCount;
 //            }
 //        }
 //
 //        String fileType = getReserved(TRICEPS_FILE_TYPE);
 //
 //        if (fileType == null || fileType.equals(TRICEPS_UNKNOWN_FILE)) {
 //            return false;
 //        }
 //
 //        isDatafile = TRICEPS_DATA_FILE.equals(fileType);
 //
 //        if (isDatafile) {
 //            if (DEPLOYABLE) {
 //                // will have many RESERVED lines interspersed - needed for knowing FILENAME, TITLE_FOR_PICKLIST, etc.
 //                lines = scheduleSource.getBody();
 //                for (int i = 0; i < lines.size(); ++i, ++linenum) {
 //                    line = (String) lines.elementAt(i);
 //                    if (!line.startsWith("RESERVED")) {
 //                        continue;
 //                    }
 //                    if (parseReserved(linenum, nodeCount, source, line)) {
 //                        ++reservedCount;
 //                    }
 //                }
 //            }
 //        }
 //
 //        return (reservedCount > 0);
 //    }
 
     /* Can either create a new schedule, or load a datafile */
     private boolean load() {
         return loadScheduleFromDB();
         
 //        isDatafile = TRICEPS_DATA_FILE.equals(getReserved(TRICEPS_FILE_TYPE));
 //
 //        if (isDatafile) {
 //            if (!loadDataHeaders(scheduleSource)) {
 //                logger.log(Level.SEVERE, "##@@Error loading dataHeaders");
 //                return false;
 //            }
 //            // load schedule
 //            if (!loadSchedule(scheduleSource)) {    // XXX Is this correct for re-loading an instrument?s
 //                logger.log(Level.SEVERE, "##@@Error loading schedule");
 //                return false;
 //            }
 //            if (!loadDataBody(scheduleSource)) {
 //                logger.log(Level.SEVERE, "##@@Error loading dataBody");
 //                return false;
 //            }
 //            return true;
 //        } else {
 //            return loadSchedule(scheduleSource);
 //        }
     }
 
 //    private boolean loadDataHeaders(ScheduleSource ss) {
 //        if (ss == null || !ss.isValid()) {
 //            return false;
 //        }
 //        // overload data
 //        Vector<String> lines = ss.getHeaders();
 //        String source = ss.getSourceInfo().getSource();
 //        String line = null;
 //        int linenum = 1;
 //        int nodeCount = 0;
 //
 //        for (int i = 0; i < lines.size(); ++i, ++linenum) {
 //            line = (String) lines.elementAt(i);
 //            if (line.startsWith("COMMENT")) {
 //                continue;
 //            }
 //            if (!parseReserved(linenum, nodeCount, source, line)) {
 //                return false;
 //            }
 //        }
 //        return true;
 //    }
 
 //    private boolean loadDataBody(ScheduleSource ss) {
 //        // overload data
 //        if (ss == null || !ss.isValid()) {
 //            return false;
 //        }
 //
 //        Vector<String> lines = ss.getBody();
 //        String source = ss.getSourceInfo().getSource();
 //        int linenum = 1 + ss.getHeaders().size();
 //        String line = null;
 //        int nodeCount = 0;
 //
 //        for (int i = 0; i < lines.size(); ++i, ++linenum) {
 //            line = (String) lines.elementAt(i);
 //            if (line.startsWith("COMMENT")) {
 //                continue;
 //            }
 //            // FIXME - on reload, somehow not seeing that lines start with RESERVED, and  passing to parseNode()
 //            if (line.startsWith("RESERVED")) {
 //                if (!parseReserved(linenum, nodeCount, source, line)) {
 //                    return false;
 //                }
 //            } else {
 //                if (!parseNode(line)) {
 //                    return false;
 //                }
 //                ++nodeCount;
 //            }
 //        }
 //        return true;
 //    }
 
 //    private boolean loadSchedule(ScheduleSource ss) {
 //        if (ss == null || !ss.isValid()) {
 //            return loadScheduleFromDB();
 //        }
 //        return false;
 
 //        if ((AUTHORABLE && ss.getSrcName().toLowerCase().endsWith(".txt")) ||
 //            ((DEPLOYABLE || DEMOABLE) && ss.getSrcName().toLowerCase().endsWith(".jar"))) {
 //            ;
 //        } else {
 //            logger.log(Level.SEVERE, "##ScheduleSource.loadSchedule(" + ss.getSrcName() + ")-> error");
 //            return false;
 //        }
 //
 //        Vector<String> lines = ss.getHeaders();
 //        String source = ss.getSourceInfo().getSource();
 //        String line = null;
 //
 //        int linenum = 1;
 //        boolean ok = false;
 //        for (int i = 0; i < lines.size(); ++i, ++linenum) {
 //            line = (String) lines.elementAt(i);
 //            if (line.startsWith("COMMENT")) {
 //                continue;
 //            }
 //            ok = parseReserved(linenum, 0, source, line);
 //            if (!AUTHORABLE) {
 //                if (!ok) {
 //                    return false;
 //                }
 //            }
 //        }
 //        lines = ss.getBody();
 //        for (int i = 0; i < lines.size(); ++i, ++linenum) {
 //            line = (String) lines.elementAt(i);
 //            if (line.startsWith("COMMENT")) {
 //                continue;
 //            }
 //            Node node = new Node(triceps, linenum, source, line, languageCount);
 //            if (!AUTHORABLE) {
 //                if (node.hasParseErrors()) {
 //                    return false;	// schedule must be fully debugged before deployment, otherwise won't load
 //                }
 //            }
 //            nodes.addElement(node);
 //        }
 //
 //        /* once schedule is loaded, set initial values */
 //        evidence.init(); //
 //        return true;
 //    }
 
     private boolean bracesMatch() {
         /* check for mismatching braces */
         int braceLevel = 0;
         Node node = null;
         for (int i = 0; i < nodes.size(); ++i) {
             node = getNode(i);
             if (node == null) {
                 setError(triceps.get("null_node_at_index") + i);
                 continue;
             }
             switch (node.getQuestionOrEvalType()) {
                 case Node.QUESTION:
                     break;
                 case Node.EVAL:
                     if (braceLevel > 0) {
                         if (AUTHORABLE) {
                             setError(triceps.get("evaluations_disallowed_within_block") + node.getSourceLine());
                         }
                     }
                     break;
                 case Node.GROUP_OPEN:
                     if (++braceLevel > 1) {
                         if (AUTHORABLE) {
                             setError(triceps.get("starting_nested_group_at_line") + node.getSourceLine());
                         }
                     }
                     break;
                 case Node.GROUP_CLOSE:
                     if (--braceLevel < 0) {
                         if (AUTHORABLE) {
                             setError(triceps.get("extra_closing_brace_at_line") + node.getSourceLine());
                         }
                     }
                     break;
                 case Node.BRACE_OPEN:
                 case Node.BRACE_CLOSE:
                 case Node.CALL_SCHEDULE:
                     if (AUTHORABLE) {
                         setError(node.getQuestionOrEvalTypeField() + triceps.get("not_yet_suppported___line") + node.getSourceLine());
                     }
                     break;
                 default:
                     if (AUTHORABLE) {
                         setError(triceps.get("unknown_actionType_at_line") + node.getSourceLine());
                         break;
                     } else {
                         return false;
                     }
             }
         }
         if (braceLevel > 0) {
             if (AUTHORABLE) {		// so that schedules with flow errors don't load in Interviewer Mode
                 setError(triceps.get("missing") + braceLevel + triceps.get("closing_braces"));
                 return true;
             } else {
                 setError("syntax error");
                 return false;
             }
         }
         return true;
     }
 
     private boolean prepareDataLogging() {
 //        String s = getReserved(TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS);
 //        String source = (scheduleSource == null) ? "" : scheduleSource.getSourceInfo().getSource(); // FIXME - does this need a value?
 //        if (s == null || s.trim().length() == 0) {
 //            // set a reasonable default value
 //            setReserved(TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS, getReserved(TITLE) + " [" + (new Date()) + "]");
 //        }
 //        setReserved(LOADED_FROM, source);	// keep LOADED_FROM up to date
 //
         /* initialize datafiles */
         if (DEPLOYABLE) {
 //            if (isDatafile) {
 //                triceps.createDataLogger(getReserved(WORKING_DIR), source);
 //            } else {
                 evidence.writeDatafileHeaders();
                 evidence.writeStartingValues();
 //            }
         }
         return true;
     }
 
     boolean parseNode(String tsv) {
         StringTokenizer tokens = new StringTokenizer(tsv, "\t", true);
         int field = 0;
         String localName = null;
         String answerLanguagenum = null;
         String ans = null;
         String comment = null;
         String timeStamp = null;
         Datum datum = null;
         Node node = null;
         Value val = null;
         boolean ok = true;
 
         while (tokens.hasMoreTokens()) {
             String s = null;
             s = tokens.nextToken();
 
             if (s.equals("\t")) {
                 ++field;
                 continue;
             }
 
             switch (field) {
                 case 0:
                     break;
                 case 1:
                     localName = (new ExcelDecoder()).decode(s);
                     break;
                 case 2:
                     answerLanguagenum = (new ExcelDecoder()).decode(s);
                     break;
                 case 3:
                     timeStamp = (new ExcelDecoder()).decode(s);
                     break;
                 case 4:
                     break; // don't reload questionAsAsked
                 case 5:
                     ans = (new ExcelDecoder()).decode(s);
                     break;
                 case 6:
                     comment = (new ExcelDecoder()).decode(s);
                     break;
             }
         }
 
         val = evidence.getValue(localName);
         if (val == null) {
             setError("Unknown variable '" + localName + "'");
             return false;
         }
 
         node = val.getNode();
         if (node != null) {
             node.setComment(comment);
 
             datum = Datum.parseSpecialType(triceps, ans);
             if (datum == null) {
                 /* then not special, so use the init value */
                 if (ans == null || ans.trim().length() == 0) {
                     /* then a 0 length answer from the datafile - not same as *UNASKED* from schedule file! */
                     ans = "";
                 }
                 datum = new Datum(triceps, ans, node.getDatumType(), node.getMask());
             }
 
             int langNum = 0;
             try {
                 if (answerLanguagenum != null) {	// should only be null if saved via Evidence.set() from 'e' nodes
                     langNum = Integer.parseInt(answerLanguagenum);
                 } else {
                     Logger.getLogger(LoggerName).log(Level.SEVERE, "!! null answerLangNum @ " + tsv);
                 }
             } catch (NumberFormatException t) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE, "languagenum_must_be_an_integer", t);
                 if (AUTHORABLE) {
                     node.setParseError(triceps.get("languagenum_must_be_an_integer") + t.getMessage());
                 } else {
                     node.setParseError("syntax error");
                 }
                 ok = false;
             }
             node.setAnswerLanguageNum(langNum);
             evidence.set(node, datum, timeStamp, false);	// must be called last, since my try to write to data file.
         } else {
             /* a reserved word */
             evidence.set(localName, new Datum(triceps, ans, Datum.STRING, null));
         }
         return ok;
     }
 
     Node getNode(int index) {
         if (index < 0) {
             setError("Node[" + index + "] does not exist");
             return null;
         }
         if (index > size()) {
             setError("Node[" + index + "/" + size() + "] does not exist");
             return null;
         }
         return (Node) nodes.elementAt(index);
     }
 
     int size() {
         return nodes.size();
     }
 
     private boolean parseReserved(int line,
                                    int nodeCount,
                                    String filename,
                                    String fileLine) {
         StringTokenizer tokens = new StringTokenizer(fileLine, "\t", true);
         int field = 0;
         String name = null;
         String value = null;
 
         while (tokens.hasMoreTokens()) {
             String s = null;
             s = tokens.nextToken();
 
             if (s.equals("\t")) {
                 ++field;
                 continue;
             }
 
             switch (field) {
                 case 0:
                     break;
                 case 1:
                     name = (new ExcelDecoder()).decode(s);
                     break;
                 case 2:
                     value = (new ExcelDecoder()).decode(s);
                     break;
                 default:
                     break;
             }
         }
         if (field < 2 || name == null) {
             setError(triceps.get("incorrect_syntax_for") + ((name != null) ? name : "") + " [" + filename + "(" + line + ")]");
             return false;
         }
 
         int resIdx = -1;
         for (int i = 0; i < RESERVED_WORDS.length; ++i) {
             if (RESERVED_WORDS[i].equals(name)) {
                 resIdx = i;
                 break;
             }
         }
 
         if (!setReserved(resIdx, value)) {
             setError(name + " " + triceps.get("not_recognized") + filename + "(" + line + ")]");
             return false;
         }
         return true;
     }
 
     void writeReserved(int resIdx) {
         if (DEPLOYABLE) {
             if (resIdx >= 0 && resIdx < RESERVED_WORDS.length) {
                 triceps.getDataLogger().println("RESERVED\t" + RESERVED_WORDS[resIdx] + "\t" + getReserved(resIdx) +
                     "\t" + System.currentTimeMillis() + "\t\t\t");
 //                if (DB_LOG_MINIMAL) {
 //                    triceps.getTtc().writeReserved(RESERVED_WORDS[resIdx], (new InputEncoder()).encode(getReserved(resIdx)));
 //                }
                 if (DB_LOG_FULL) {
                     triceps.getDtc().writeReserved(RESERVED_WORDS[resIdx], (new InputEncoder()).encode(getReserved(resIdx)));
                 }
             }
         }
     }
 
     void writeAllReserved() {
         for (int i = 0; i < RESERVED_WORDS.length; ++i) {
             writeReserved(i);
         }
     }
 
     boolean overloadReserved(Schedule oldNodes) {
         /* FIXME - need to use String Objects for reserved words, not integers, otherwise subject to re-ordering! */
         if (nodes == null || nodes.size() == 0) {
             return false;
         }
         boolean ok = true;
 
         boolean oldIsLoaded = isLoaded;
         isLoaded = false;	// so that don't write out values	FIXME - this might be preventing the setRerved updating on reload
 
         for (int i = 0; i < RESERVED_WORDS.length; ++i) {
             ok = setReserved(i, oldNodes.getReserved(i)) && ok;
         }
 
         isLoaded = oldIsLoaded;
         return ok;
     }
 
     boolean setReserved(int resIdx,
                         String value) {
         return setReserved(resIdx, value, true, false);
     }
 
     boolean setReserved(int resIdx,
                         String value,
                         boolean expert,
                         boolean writeToDb) {
         String s = null;
         expert = true;	// FIXME - ultimately want ability to restrict who can change values
         if (value == null) {
             value = "";
         }
         switch (resIdx) {
             case TITLE:
                 s = value;
                 break;
             case STARTING_STEP:
                 if (expert) {
                     s = setStartingStep(value);
                 }
                 break;
             case START_TIME:
                 if (expert) {
                     s = setStartTime(value);
                 }
                 break;
             case PASSWORD_FOR_ADMIN_MODE:
                 s = value;
                 break;
             case AUTOGEN_OPTION_NUM:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case FILENAME:
                 s = setFilename(value);
                 break;
             case ICON:
                 s = value;
                 break;
             case HEADER_MSG:
                 s = value;
                 break;
             case SHOW_QUESTION_REF:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case DEVELOPER_MODE:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case DEBUG_MODE:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case LANGUAGES:
                 s = setLanguages(value);
                 break;
             case SHOW_ADMIN_ICONS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case TITLE_FOR_PICKLIST_WHEN_IN_PROGRESS:
                 s = value;
                 break;
             case ALLOW_COMMENTS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case SCHEDULE_SOURCE:
                 if (expert) {
                     s = value;
                 }
                 break;
             case LOADED_FROM:
                 if (expert) {
                     s = value;
                 }
                 break;
             case CURRENT_LANGUAGE:
                 if (expert) {
                     s = setLanguage(value.trim());
                 }
                 break;
             case ALLOW_LANGUAGE_SWITCHING:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case ALLOW_REFUSED:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case ALLOW_UNKNOWN:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case ALLOW_DONT_UNDERSTAND:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case RECORD_EVENTS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case WORKING_DIR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case COMPLETED_DIR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case FLOPPY_DIR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case IMAGE_FILES_DIR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case COMMENT_ICON_ON:
                 s = value;
                 break;
             case COMMENT_ICON_OFF:
                 s = value;
                 break;
             case REFUSED_ICON_ON:
                 s = value;
                 break;
             case REFUSED_ICON_OFF:
                 s = value;
                 break;
             case UNKNOWN_ICON_ON:
                 s = value;
                 break;
             case UNKNOWN_ICON_OFF:
                 s = value;
                 break;
             case DONT_UNDERSTAND_ICON_ON:
                 s = value;
                 break;
             case DONT_UNDERSTAND_ICON_OFF:
                 s = value;
                 break;
             case TRICEPS_VERSION_MAJOR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case TRICEPS_VERSION_MINOR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case SCHED_AUTHORS:
                 s = value;
                 break;
             case SCHED_VERSION_MAJOR:
                 s = value;
                 break;
             case SCHED_VERSION_MINOR:
                 s = value;
                 break;
             case SCHED_HELP_URL:
                 s = value;
                 break;
             case HELP_ICON:
                 s = value;
                 break;
             case ACTIVE_BUTTON_PREFIX:
                 s = value;
                 break;
             case ACTIVE_BUTTON_SUFFIX:
                 s = value;
                 break;
             case TRICEPS_FILE_TYPE:
                 if (expert) {
                     s = setTricepsFileType(value);
                 }
                 break;
             case DISPLAY_COUNT:
                 if (expert) {
                     s = setDisplayCount(value);
                 }
                 break;
             case SCHEDULE_DIR:
                 if (expert) {
                     s = value;
                 }
                 break;
             case ALLOW_JUMP_TO:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case BROWSER_TYPE:
                 if (expert) {
                     s = value;
                 }
                 break;
             case IP_ADDRESS:
                 if (expert) {
                     s = value;
                 }
                 break;
             case SUSPEND_TO_FLOPPY:
                 if (expert) {
                     s = Boolean.valueOf(value.trim()).toString();
                 }
                 break;
             case JUMP_TO_FIRST_UNASKED:
                 if (expert) {
                     s = Boolean.valueOf(value.trim()).toString();
                 }
                 break;
             case REDIRECT_ON_FINISH_URL:
                 if (expert) {
                     s = value.trim();
                 }
                 break;
             case REDIRECT_ON_FINISH_MSG:
                 if (expert) {
                     s = value.trim();
                 }
                 break;
             case REDIRECT_ON_FINISH_DELAY:
                 s = value;
                 break;
             case SWAP_NEXT_AND_PREVIOUS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case ANSWER_OPTION_FIELD_WIDTH:
                 s = setAnswerOptionFieldWidth(value);
                 break;
             case SET_DEFAULT_FOCUS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case ALWAYS_SHOW_ADMIN_ICONS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case SHOW_SAVE_TO_FLOPPY_IN_ADMIN_MODE:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case WRAP_ADMIN_ICONS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case DISALLOW_COMMENTS:
                 s = Boolean.valueOf(value.trim()).toString();
                 break;
             case CONNECTION_TYPE:
                 if (expert) {
                     s = value.trim();
                 }
                 break;
             case MAX_TEXT_LEN_FOR_COMBO:
                 s = value;
                 break;
             default:
                 return false;
         }
 
         if (s != null) {
             reserved.put(RESERVED_WORDS[resIdx], s);
             if (DEPLOYABLE) {
                 if (isLoaded && writeToDb) {
                     writeReserved(resIdx);
                 }
             }
 
             return true;
         } else {
             return false;
         }
     }
 
     private String setDisplayCount(String value) {
         Integer ii = null;
         if (value == null || value.trim().equals("")) {
             return "0"; // FIXME:  hack to avoid  NullPointerException when trying to re-load schedules  - some RESERVED words inaccurately parsed as Nodes
         }
         try {
             ii = new Integer(value);
         } catch (NumberFormatException e) {
             setError(triceps.get("invalid_number_for_starting_step") + ": '" + value + "': " + e.getMessage());
             ii = new Integer(0);
         }
         return ii.toString();
     }
 
     private String setAnswerOptionFieldWidth(String value) {
         Integer ii = new Integer(0);
         try {
             ii = new Integer(value);
         } catch (NumberFormatException e) {
             setError(triceps.get("invalid_number_for_field_width") + ": '" + value + "': " + e.getMessage());
             ii = new Integer(0);
         }
         return ii.toString();
     }
 
     private String setTricepsFileType(String value) {
         if (value.equalsIgnoreCase(TRICEPS_DATA_FILE)) {
             return TRICEPS_DATA_FILE;
         } else if (value.equalsIgnoreCase(TRICEPS_SCHEDULE_FILE)) {
             return TRICEPS_SCHEDULE_FILE;
         } else {
             return TRICEPS_UNKNOWN_FILE;
         }
     }
 
     String getReserved(int resIdx) {
         if (resIdx >= 0 && resIdx < RESERVED_WORDS.length) {
             return (String) reserved.get(RESERVED_WORDS[resIdx]);
         } else {
             return null;
         }
     }
 
     boolean getBooleanReserved(int resIdx) {
         if (resIdx >= 0 && resIdx < RESERVED_WORDS.length) {
             return Boolean.valueOf((String) reserved.get(RESERVED_WORDS[resIdx])).booleanValue();
         } else {
             return false;
         }
     }
 
     private String setFilename(String s) {
         String name = s;
         if (name == null) {
             return null;
         }
         name = name.trim();
         if (name.length() == 0) {
             return null;
         }
         return name;
     }
 
     private String setStartingStep(String s) {
         Integer startingStep = null;
         try {
             startingStep = new Integer(s);
         } catch (NumberFormatException e) {
             setError(triceps.get("invalid_number_for_starting_step") + ": '" + s + "': " + e.getMessage());
             startingStep = new Integer(0);
         }
         return startingStep.toString();
     }
 
     private String setStartTime(Date t) {
         startTime = t;
         String str = null;
         if (t == null) {
             str = Long.toString(System.currentTimeMillis());
         } else {
             str = Long.toString(t.getTime());
         }
         return str;
     }
 
     private String setStartTime(String t) {
         Date time = null;
         if (t != null && t.trim().length() > 0) {
             try {
                 time = new Date(Long.parseLong(t));
             } catch (NumberFormatException e) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE, "", e);
             }
         }
         return setStartTime(time);
     }
 
     private String setLanguages(String value) {
         StringBuffer sb = new StringBuffer();
         if (value == null) {
             return setLanguages(DEFAULT_LANGUAGE);
         }
 
         languageCount = 0;
         locales = new Vector<Locale>();
         StringTokenizer ans = new StringTokenizer(value, "|");
         while (ans.hasMoreTokens()) {
             String s = null;
             try {
                 s = ans.nextToken();
                 if (s == null || s.trim().length() == 0) {
                     continue;
                 }
                 s = s.trim();
 
             } catch (NoSuchElementException e) {
                 Logger.getLogger(LoggerName).log(Level.SEVERE, "", e);
             }
             /* regenerate the string, stipping excess pipe characters */
             ++languageCount;
             if (sb.length() != 0) {
                 sb.append("|" + s);
             } else {
                 sb.append(s);
             }
 
             String lang = null;
             String country = null;
             String extra = null;
 
             try {
                 StringTokenizer part = new StringTokenizer(s, "_");
                 lang = part.nextToken();
                 country = part.nextToken();
                 extra = part.nextToken();
             } catch (NoSuchElementException e) { /* if no subparts, keep as null */ }
 
             Locale loc = new Locale((lang == null) ? "" : lang, (country == null) ? "" : country, (extra == null) ? "" : extra);
 
             locales.addElement(loc);
         }
         buildLanguageNames();
 
         return sb.toString();
     }
 
     private void buildLanguageNames() {
         languageNames = new Vector<String>();
         languagesISO = new Vector<String>();
 
         for (int i = 0; i < locales.size(); ++i) {
             Locale loc = (Locale) locales.elementAt(i);
             languageNames.addElement(loc.getDisplayLanguage(loc));
             languagesISO.addElement(loc.toString());
         }
     }
 
     Vector<String> getLanguages() {
         return languageNames;
     }
 
     Vector<String> getLanguagesISO() {
         return languagesISO;
     }
 
     String setLanguage(String s) {
         Locale loc = null;
 
         int lang = -1;
 
         if (s == null || s.trim().length() == 0) {
             lang = 0;
         } else {
 //            logger.log(Level.WARNING, "languages.ISO = " + languagesISO);
             for (int i = 0; i < languagesISO.size(); ++i) {
                 if (s.equals((String) languagesISO.elementAt(i))) {
                     lang = i;
                 }
             }
 //            logger.log(Level.WARNING, "locales = " + locales);
             for (int i = 0; i < locales.size(); ++i) {
                 loc = (Locale) locales.elementAt(i);
                 if (s.equals(loc.toString())) {
                     lang = i;
                 }
             }
             if (lang == -1) {
                 if (isFound && isLoaded) {  //  FIXME - 6/17/08 - does this really need to happen at startup?
                     setError(triceps.get("tried_to_switch_to_unsupported_language") + s);   // FIXME - This is showing a re-entrancy problem when switching between instruments with differents of languages
                 }
             } else {
                 currentLanguage = lang;
             }
 
         }
 
         loc = (Locale) locales.elementAt(currentLanguage);
         if (triceps != null) {
             triceps.setLocale(loc);
         }	// so can parse subsequent data correctly
 
         recalculateInNewLanguage();
 
         return loc.toString();
     }
 
     boolean recalculateInNewLanguage() {
         boolean ok = false;
 
         if (!isLoaded()) // or isFound?  Want to be able to set the default language to something else?
         //		if (!isFound || triceps == null)
         {
             return ok;
         }
 
         Parser parser = triceps.getParser();
 
         /* re-calculate all eval nodes that meet dependencies, using the new language */
         for (int i = 0; i < triceps.getCurrentStep(); ++i) {
             Node node = getNode(i);
             if (node == null) {
                 continue;
             }
 
             if (node.getQuestionOrEvalType() == Node.EVAL) {
                 node.setAnswerLanguageNum(currentLanguage);	// don't change the language for non-EVAL nodes - want to know what was asked
                 if (parser.booleanVal(triceps, node.getDependencies())) {
                     Datum datum = parser.parse(triceps, node.getQuestionOrEval());
 //					node.setDatumType(datum.type());
                     int type = node.getDatumType();
                     if (type != Datum.STRING && type != datum.type()) {
                         datum = datum.cast(type, null);
                     }
                     evidence.set(node, datum);
                 } else {
                     evidence.set(node, new Datum(Datum.NA,triceps));	// if doesn't satisfy dependencies, store NA
                 }
             }
         }
         return true;
     }
 
     int getLanguage() {
         return currentLanguage;
     }
 
     private void setError(String s) {
         Logger.getLogger(LoggerName).log(Level.WARNING, s);
         errorLogger.append(s).append("<br/>");
     }
 
     boolean hasErrors() {
         return (errorLogger.length() > 0);
     }
 
     String getErrors() {
         String errors = errorLogger.toString();
         errorLogger = new StringBuffer();
         return errors;
     }
 
     Triceps getTriceps() {
         return triceps;
     }
 
     public HashMap<String, String> getReserveds() {
         return reserved;
     }    
 
 //    String signAndSaveAsJar() {
 //        if (AUTHORABLE) {
 //            String name = scheduleSource.saveAsJar(getReserved(Schedule.SCHEDULE_SOURCE));
 //            if (name == null) {
 //                return null;
 //            }
 //
 //            File f = new File(name);
 //            if (f.length() == 0L) {
 //                triceps.setError("signAndSaveAsJar: file has 0 size");
 //                return null;
 //            }
 //            return name;
 //        }
 //        return null;
 //    }
 }
