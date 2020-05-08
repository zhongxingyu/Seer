 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 
 
 package org.webmacro.util;
 
 import java.util.*;
 
 import org.webmacro.Log;
 
 /**
  * The LogSystem class allows you to create and manage log objects
  * and log targets. The LogSystem takes responsibility for ensuring
  * that the appropriate targets are connected to the appropriate
  * log objects.
  * <p>
  * There is a global LogSystem in place which you can access by
  * calling getInstance(). Alternately you can create a new Logsystem
  * by calling getInstance(category). Each category is a singleton
  * with only one instance corresponding to its name existing in
  * the JVM at any given time.
  */
 final public class LogSystem {
 
    final static public int ALL = 0;
    final static public int DEBUG = 0;
    final static public int INFO = 1;
    final static public int NOTICE = 2;
    final static public int WARNING = 3;
    final static public int ERROR = 4;
    final static public int NONE = 5;
 
    /**
     * Mapping of numbers to names. The index into this array
     * is the number corresponding to the log level.
     */
    final static protected String[] LEVELS =
          {
             "DEBUG", "INFO", "NOTICE", "WARNING", "ERROR"
          };
 
    /**
     * Convert a string like "DEBUG" into a level number.
     * This method also converts the strings ALL and NONE
     * to the lowest (ALL) and one past highest (NONE) values.
     */
    public static int getLevel(String levelName) {
       for (int i = 0; i < LEVELS.length; i++) {
          if (LEVELS[i].equalsIgnoreCase(levelName)) return i;
       }
       if ("NONE".equalsIgnoreCase(levelName))
          return LogSystem.NONE;
       else if ("ALL".equalsIgnoreCase(levelName))
          return LogSystem.ALL;
       else
          return LogSystem.INFO;
    }
 
    private final static Map _instances = new HashMap();
    private final static LogSystem _singleton;
    private final static Log _log;
 
    private final static LogFile _defaultTarget = new LogFile(System.err);
 
    static {
       _defaultTarget.setLogLevel(LogSystem.NOTICE);
       _defaultTarget.setTraceExceptions(true);
       _singleton = new LogSystem("system");
       _log = _singleton.getLog("log", "information about the logging system");
    }
 
    /**
     * Get the global, system-wide, default log system
     */
    public static LogSystem getInstance() {
       return getInstance(null);
    }
 
    /**
     * Get a system-wide log
     */
    public static Log getSystemLog(String type) {
       return _singleton.getLog(type, type);
    }
 
    /**
     * Get a system log, providing it with a description of itself
     */
    public static Log getSystemLog(String type, String description) {
       return _singleton.getLog(type, description);
    }
 
    /**
     * Return the log-system with the specified category
     */
    public static LogSystem getInstance(String category) {
       synchronized (_instances) {
          if (category == null) return _singleton;
          LogSystem ls = (LogSystem) _instances.get(category);
          if (ls == null) {
             ls = new LogSystem(category);
             _instances.put(category, ls);
          }
          return ls;
       }
    }
 
    /**
     * Remove the specified LogSystem instance from the internal
     * cache of LogSystems.
     */ 
    public static void removeInstance(LogSystem instance) {
        synchronized (_instances) {
            if (_instances.containsValue(instance)) {
                for (Iterator itr = _instances.entrySet().iterator(); itr.hasNext();) {
                    Map.Entry e = (Map.Entry) itr.next();
                    if (e.getValue() == instance) {
                        itr.remove();
                    }
                }
            }
        }
    }


    /////////////////////////////////////////////
 
    final private String _category;
 
    final private Map _logs = new HashMap();
    final private Set _targets = new HashSet();
 
    private LogSystem(String category) {
       _category = category;
       _targets.add(_defaultTarget);
    }
 
    /**
     * Get an instance of a Log object for the specified type.
     */
    public Log getLog(String type) {
       return getLog(type, type);
    }
 
    /**
     * Get the Log object within this LogSystem that has
     * the specified Log type. These are the common Log
     * objects which you write Log messages to. The description
     * will be used to print out an explanation of the log in
     * various places.
     */
    synchronized public Log getLog(String type, String description) {
       LogSource l = (LogSource) _logs.get(type);
 
       if (l != null)
          return l;
       l = new LogSource(_category, type, description);
       _logs.put(type, l);
 
       Iterator i = _targets.iterator();
       while (i.hasNext()) {
          LogTarget lt = (LogTarget) i.next();
          for (int level = LogSystem.ALL; level < LogSystem.NONE; level++) {
             if (lt.subscribe(_category, type, level)) {
                l.addTarget(lt, level);
             }
          }
       }
 
       if (_log != null) {
          _log.info("Started log " + l.getType() + ": " + l.getDescription());
       }
 
       return l;
    }
 
    /**
     * Add a new LogTarget to the LogSystem. The LogSystem will
     * ask the target whether it wants to receive log messages
     * for all of the Log objects existing in the system now or
     * in the future.
     */
    synchronized public void addTarget(LogTarget t) {
       if (t == null) return;
       if (_targets.contains(_defaultTarget)) removeTarget(_defaultTarget);
       Iterator i = _logs.values().iterator();
       while (i.hasNext()) {
          LogSource l = (LogSource) i.next();
          for (int level = LogSystem.ALL; level < LogSystem.NONE; level++) {
             if (t.subscribe(_category, l.getType(), level)) {
                l.addTarget(t, level);
             }
          }
       }
       _targets.add(t);
       t.addObserver(this);
    }
 
    /**
     * Remove a LogTarget from the LogSystem. It will be disconnected
     * from any Log objects that are feeding it messages.
     */
    synchronized public void removeTarget(LogTarget t) {
       if (t == null) return;
       Iterator i = _logs.values().iterator();
       while (i.hasNext()) {
          LogSource l = (LogSource) i.next();
          for (int level = LogSystem.ALL; level < LogSystem.NONE; level++) {
             if (t.subscribe(_category, l.getType(), level)) {
                l.removeTarget(t, level);
             }
          }
       }
       _targets.remove(t);
       if ((t != _defaultTarget) && (_targets.size() == 0)) {
          addTarget(_defaultTarget);
       }
       t.removeObserver(this);
    }
 
    /**
     * LogTarget should call this method to notify LogSystem that
     * conditions have changed and that the subscribe() method on
     * LogTarget may now return a different value. The type field
     * may be null, or it may be a String type if conditions have
     * changed only for a single Log source.
     */
    synchronized public void update(LogTarget target, String type) {
       if (target == null) return;
       if (type != null) {
          LogSource l = (LogSource) _logs.get(type);
          if (l != null) update(target, type, l);
       }
       else {
          Iterator i = _logs.values().iterator();
          while (i.hasNext()) {
             LogSource l = (LogSource) i.next();
             update(target, l.getType(), l);
          }
       }
    }
 
    private void update(LogTarget lt, String type, LogSource l) {
       for (int level = LogSystem.ALL; level < LogSystem.NONE; level++) {
          if (lt.subscribe(_category, type, level)) {
             l.addTarget(lt, level);
          }
          else {
             l.removeTarget(lt, level);
          }
       }
    }
 
 
    /**
     * Flush all of the LogTarget objects in the system
     */
    synchronized public void flush() {
       Iterator i = _targets.iterator();
       while (i.hasNext()) {
          LogTarget t = (LogTarget) i.next();
          t.flush();
       }
    }
 
    /**
     * Flush all log systems
     */
    static public void flushAll() {
       _singleton.flush();
       Iterator i = _instances.values().iterator();
       while (i.hasNext()) {
          LogSystem ls = (LogSystem) i.next();
          ls.flush();
       }
    }
 
 
    /**
     * Test out the logging system
     */
    public static void main(String arg[]) throws Exception {
       System.out.println("-------- starting main -----------");
       Log l = LogSystem.getInstance().getLog("test", "just a test");
       l.debug("1:testing debug");
       l.info("1:testing info");
       l.notice("1:testing notice");
       l.warning("1:testing warning");
       l.error("1:testing error");
 
       LogFile lf = new LogFile("test.log");
 
       System.out.println("Adding new log target: " + lf);
       LogSystem.getInstance().addTarget(lf);
 
       l.debug("2:testing debug");
       l.info("2:testing info");
       l.notice("2:testing notice");
       l.warning("2:testing warning");
       l.error("2:testing error");
 
       LogSystem.flushAll();
 
 
    }
 }
 
