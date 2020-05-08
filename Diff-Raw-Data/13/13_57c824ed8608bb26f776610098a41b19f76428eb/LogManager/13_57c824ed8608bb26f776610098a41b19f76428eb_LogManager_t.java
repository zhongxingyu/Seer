 
 package org.webmacro.util;
 
 import java.util.LinkedList;
 import java.util.Properties;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Iterator;
 import java.io.IOException;
 import org.webmacro.Log;
 
 /**
   * Central registration class which controls the relationship between
   * a Log and a LogTarget. 
   */
 final public class LogManager {
 
    private Hashtable  _sources = new Hashtable();
    private List _targets = new LinkedList();
    private Log _log = getLog("log");
 
    /**
      * See getSystemLog()
      */
    private final static LogSource _sysLog = new LogSource("sys") {
       final private LogFile _err = 
            new LogFile("System.err", System.err, "{0,time,medium} {1}:{2} {3}", 
                "WARNING", null, true);
       protected void log(int level, String msg, Exception e) {
          super.log(level,msg,e); 
         if ((level >= LogSource.NOTICE) && !hasTargets()) {
                _err.log("sys", LEVELS[level],msg,e);
          }
       }
    };
 
    // instance initializer block: we make sure all constructors do this
    {
       addSource(_sysLog); 
    }
 
    /**
      * Create a new LogManager with no LogTarget objects
      */
    public LogManager() { }
 
    /**
      * Create a new LogManager with a LogFile set up from the 
      * supplied Properties. The Properties look like this:
      * <pre>
          LogFile: filename
          LogFormat: {0} {1} {2} {3}   # {0} is a date, 1,2,3 strings
          LogExceptions: true|false meaning stack traces or not?
          LogLevel: ALL|DEBUG|INFO|NOTICE|WARNING|ERROR
          LogLevel.source: ALL|..|ERROR, but just for log "source"
        </pre>
      * There are two build in log sources "sys" which is a generic
      * system log source, and "log" which is for messages from 
      * the log manager itself. Other sources are created by your
      * application when it calls getLog(). You can get the system
      * log by calling getSystemLog(). It can be used from locations
      * where accessing a log manager instance is infeasible.
      */
    public LogManager(Properties config) {
 
       String fileName = config.getProperty("LogFile", null);
       if ( ("System.error").equalsIgnoreCase(fileName)
            || ("stderr").equalsIgnoreCase(fileName)
            || ("none").equalsIgnoreCase(fileName))
       {
          fileName = null;
       }
 
       String format = 
          config.getProperty("LogFormat", "{0,time,medium} {1}:{2} {3}");
       String strace =
          config.getProperty("LogExceptions", null);
       String logLevel = 
          config.getProperty("LogLevel", "NOTICE");
 
       if (strace == null) {
          strace = config.getProperty("LogTraceExceptions", "false");
       }
       boolean trace = 
          ("true".equalsIgnoreCase(strace) || "yes".equalsIgnoreCase(strace));
 
       Enumeration e = config.propertyNames();
       Properties levels = new Properties();
       while (e.hasMoreElements()) {
          String key = (String) e.nextElement();
          if (key.startsWith("LogLevel.")) {
             String type = key.substring(9);
             String level = config.getProperty(key);
             levels.setProperty(type, level);
          }
       }
   
       LogFile lf;
       try {
          lf = (fileName != null) ?
             new LogFile(fileName,format,logLevel,levels,trace) :
             new LogFile("stderr",System.err,format,logLevel,levels,trace);
       } catch (IOException ioe) {
          _sysLog.error("Unable to write to logfile: " + format, ioe);
          lf = new LogFile("System.err", null, format, logLevel, levels, trace);
       }
       addTarget(lf);
 
    }
 
    /**
      * Instantiate a new Log object
      */
    public Log getLog(String name) {
       LogSource l = (LogSource) _sources.get(name);
       if (l != null) {
          return l;
       }
       if (_log != null) {
          _log.info("new log category: " + name);
       }
       l = new LogSource(name);
       addSource(l);
       return l;
    }
 
    /**
      * Used internally by LogManager to register new sources. 
      * Ordinarily you would get a new source by calling getLog().
      */
    protected void addSource(LogSource l) {
       _sources.put(l.getName(),l);
       Iterator t = _targets.iterator();
       while (t.hasNext()) {
          LogTarget lt = (LogTarget) t.next();
          lt.attach(l);
       }
    }
 
    /**
      * Flush all log targets
      */
    public void flush() {
       Iterator t = _targets.iterator();
       while (t.hasNext()) {
          LogTarget lt = (LogTarget) t.next();
          lt.flush();
       }
    }
 
    /**
      * This is the "sys" log source. This log source is shared between
      * all instances of LogManager. Furthermore, if there are no targets
      * registered to receive it ERROR and WARNING messagse will be 
      * printed on the system console (or wherever Systerm.err goes). 
      * As soon as there is a target registered to receive "sys" 
      * messages it will stop writing to System.err.
      */
    public static Log getSystemLog() {
       return _sysLog;
    }
 
    /**
      * Register a new target to receive log messages
      */
    public void addTarget(LogTarget t) {
       _targets.add(t);
       Enumeration s = _sources.elements();
       while (s.hasMoreElements()) {
          LogSource l = (LogSource) s.nextElement();
          t.attach(l);
       }
       if (_log != null) {
          _log.info("target: " + t);
       }
    }
 
    /**
      * Remove a target from the list of recipients
      */
    public void removeTarget(LogTarget t) {
       _targets.remove(t);
       Enumeration s = _sources.elements();
       while (s.hasMoreElements()) {
          LogSource l = (LogSource) s.nextElement();
          l.removeTarget(t);
       }
    }
 
    public static void main(String arg[]) {
 
       Properties p = new Properties();
       if (arg.length < 3) {
          System.out.println("args = file format trace? LogLevel.*");
          System.out.println("example format: {0,time} {1}:{2} {3}");
          return;
       } 
       String fileName = arg[0];
       String format = arg[1];
       String trace = arg[2];
       String level = arg[3];
 
 
       p.setProperty("LogFile", fileName);
       p.setProperty("LogFormat", format);
       p.setProperty("LogExceptions", trace);
       p.setProperty("LogLevel.*", level);
       p.setProperty("LogLevel.bug", "DEBUG");
       p.setProperty("LogLevel.test", "ERROR");
 
       LogManager m = new LogManager(p);
 
       Log bug = m.getLog("bug");
       Log test = m.getLog("test");
       Log other = m.getLog("other");
 
       bug.debug("debug to bug");
       test.debug("debug to test");
       other.debug("debug to other");
 
       bug.info("info to bug");
       test.info("info to test");
       other.info("info to other");
 
       bug.notice("notice to bug");
       test.notice("notice to test");
       other.notice("notice to other");
 
       bug.warning("warn to bug");
       test.warning("warn to test");
       other.warning("warn to other");
 
       bug.error("error to bug");
       test.error("error to test");
       other.error("error to other");
 
       try {
          ((Object) null).toString();
       } catch (NullPointerException e) {
          bug.warning("warn to bug with exception",e);
          test.warning("warn to test with exception",e);
          other.warning("warn to other with exception",e);
 
          bug.error("error to bug with exception",e);
          test.error("error to test with exception",e);
          other.error("error to other with exception",e);
       }
    }
 }
