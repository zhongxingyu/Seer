 
 package org.webmacro.util;
 
 import java.io.*;
 import java.util.*;
 import java.text.MessageFormat;
 
 public class LogFile implements LogTarget {
 
    private Map _levels = new HashMap();
    private boolean _trace = false;
    private MessageFormat _mf = new MessageFormat("{0,time}\t{1}\t{2}\t{3}");
    private int _defaultLevel = LogSystem.NOTICE;
    private PrintStream _out;
 
    private List _observers = new LinkedList();
 
    private String _name;
 
 
    /**
      * Create a new LogFile instance reading properties from the 
      * supplied Settings object: <pre>
      *     LogFile: filename|stderr
      *     LogTraceExceptions: true|false|yes|no|on|off
      *     Log
      */
    public LogFile(Settings s) throws FileNotFoundException {
       this(s.getSetting("LogFile"));
 
       _trace = s.getBooleanSetting("LogTraceExceptions");
       String slevel = s.getSetting("LogLevel", "NOTICE");
       _defaultLevel = LogSystem.getLevel(slevel);
       String format = s.getSetting("LogFormat");
       if (format != null) {
          _mf = new MessageFormat(format);
       }
       Settings levels = s.getSubSettings("LogLevel");
       String[] keys = levels.keys();
       for (int i = 0; i < keys.length; i++) {
         _levels.put(keys[i], levels.getSetting(keys[i]));
       }
    }
 
    /**
      * Create a new LogFile instance. 
      */
    public LogFile(String fileName) throws FileNotFoundException
    {
       if ( (fileName == null) 
             || (fileName.equalsIgnoreCase("system.err") 
             || fileName.equalsIgnoreCase("none")
             || fileName.equalsIgnoreCase("stderr")))
       {
          _out = System.err;
          _name = "System.err";
       } else {
          _out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName,true)));
          _name = fileName;
       }
       if (_defaultLevel <= LogSystem.NOTICE) {
          log(Clock.getDate(), "LogFile", "NOTICE", "--- Log Started ---", null);
       }
    }
 
    /**
      * Create a new LogFile instance
      */
    public LogFile(PrintStream out) {
       _out = out;
       _name = out.toString();
    }
 
 
    public String toString() {
       return "LogFile(name=" + _name + ", level=" + _defaultLevel + ", trace=" + _trace + ")"; 
    }
 
    /**
      * Set the log level for this Logfile. The default is LogSystem.NOTICE
      */
    public void setLogLevel(int level) {
       _defaultLevel = level;
       Iterator i = _observers.iterator();
       while (i.hasNext()) {
          LogSystem ls = (LogSystem) i.next();
          ls.update(this,null);
       }
    }
 
    /**
      * Set the log level for a specific category name. 
      */
    public void setLogLevel(String name, int level) {
       _levels.put(name, new Integer(level));   
       Iterator i = _observers.iterator();
       while (i.hasNext()) {
          LogSystem ls = (LogSystem) i.next();
          ls.update(this,name);
       }
    }
 
    /**
      * Set whether this LogFile traces exceptions. The 
      * default is false.
      */
    public void setTraceExceptions(boolean trace) {
       _trace = trace;
    }
 
    private Object[] _args = new Object[4];
    public void log(Date date, String name, String level, String message, Exception e)
    {
       synchronized(_args) {
          _args[0] = date;
          _args[1] = name;
          _args[2] = level;
          _args[3] = message;
          _out.println(_mf.format(_args));
          if (_trace && (e != null)) {
             e.printStackTrace(_out);
          }
       }
    }
 
    public boolean subscribe(String category, String name, int level) {
       Integer ilevel = (Integer) _levels.get(name);
       boolean sub;
       if (ilevel != null) {
          sub = (ilevel.intValue() <= level);
       } else {
          sub =(_defaultLevel <= level);
       }
       return sub;
    }
 
 
    public void addObserver(LogSystem ls) {
       _observers.add(ls);
    }
 
    public void removeObserver(LogSystem ls) {
       _observers.remove(ls);
    }
 
    public void flush() {
       _out.flush();
    }
 
 }
 
 
