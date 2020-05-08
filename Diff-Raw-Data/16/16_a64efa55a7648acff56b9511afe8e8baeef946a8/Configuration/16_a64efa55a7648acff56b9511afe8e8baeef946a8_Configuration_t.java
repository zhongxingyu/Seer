 /*
  * JOnAS: Java(TM) Open Application Server
  * Copyright (C) 2004 Bull S.A.
  * All rights reserved.
  * 
  * Contact: howl@objectweb.org
  * 
  * This software is licensed under the BSD license.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *     
  *   * Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *     
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.objectweb.howl.log;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Provides configuration information for a
  * {@link org.objectweb.howl.log.Logger Logger}
  * instance.
  * @author girouxm
  */
 public class Configuration {
   
   /**
    * Construct a Configuration object with default values.
    * <p>Caller will use setter methods to change the defaults.
    */
   public Configuration()
   {
   }
   
   /**
    * Construct a Configuration object using a Properties
    * object supplied by the caller.
    * @param prop Properties object containing default settings
    */
   public Configuration(Properties prop) throws LogConfigurationException
   {
     // perform default construction
     this();
     
     this.prop = prop;
     
     // apply settings from caller supplied Properties parameter
     parseProperties();
   }
   
   /**
    * Construct a Configuration object using a Properties
    * file specified by the caller.
    * @param propertyFile File object describing a properties file
    * @throws LogConfigurationException
    * if property file cannot be processed.
    */
   public Configuration(File propertyFile) throws LogConfigurationException 
   {
     // perform default construction
     this();
     
     // TODO: if File has xml extension then parse as xml
     
     prop = new Properties();
     try {
       prop.load(new FileInputStream(propertyFile));
       parseProperties();
     } catch (FileNotFoundException e)
     {
       LogConfigurationException lce = new LogConfigurationException(e.toString());
       lce.initCause(e);
       throw lce;
     } catch (IOException e)
     {
       throw new LogConfigurationException(e.toString());
     }
     
   }
   
   /**
    * maximum size of a LogBuffer (number of K bytes).
    * <p>Good performance can be achieved with buffers between
    * 2K and 6K when using a reasonably fast disk.  Larger
    * sizes may help with slower disks, but large buffers
    * may be mostly empty in lightly loaded systems.
    */
  private static final int MAX_BUFFER_SIZE = 32;
   
   /**
    * The Properties used to construct this object.
    */
   private Properties prop = null;
   
   /**
    * Display the value of an int configuration parameter to System.err
    * if <var> listConfig </var> is true.
    * <p>The <i> text </i> parameter allows the program to
    * provide additional text that will be displayed following the
    * value to explain the type of value.  For example, values that
    * represent Milliseconds might be displayed with "Ms".
    * 
    * @param key name of the parameter being displaed
    * @param val value for the parameter
    * @param text additional text to be displayed such as "Kb" or "Ms".
    */
   private void showConfig(String key, int val, String text)
   {
     if (listConfig) System.err.println(key + ": " + val + " " + text);
   }
   
   /**
    * called by parseProperties to obtain an int configuration
    * property and optionally display the configured value.
    * @param key name of the parameter to return
    * @param val default value if the parameter is not configured
    * @param text additional text to pass to showConfig(String, int, String)
    * @return int value of requested parameter
    * @see #showConfig(String, int, String)
    */
   private int getInteger(String key, int val, String text)
   {
    // BUG: 300738 - trim property value
    val = Integer.parseInt(prop.getProperty(key, Integer.toString(val)).trim());
     showConfig(key, val, text);
     return val;
   }
   
   /**
    * called by parseProperties to obtain an int configuration
    * property and optionally display the configured value.
    * <p>This routine calls getInteger(String, ing, String) passing
    * a zero length string as the third parameter.
    * 
    * @param key name of parameter to return
    * @param val default value if the parameter is not configured
    * @return int value of requested parameter
    * @see #getInteger(String, int, String)
    */
   private int getInteger(String key, int val)
   {
     return getInteger(key, val, "");
   }
   
   /**
    * called by parseProperties to obtain a boolean configuration
    * property and optionally display the configured value.
    * 
    * @param key name of parameter to return
    * @param val default value if the parameter is not configured
    * @return boolean value of the requested parameter
    * @throws LogConfigurationException
    * if the configured value of the property is something other than
    * 'true' or 'false'
    */
   private boolean getBoolean(String key, boolean val)
   throws LogConfigurationException
   {
    // BUG: 300738 - trim property value
    String pval = prop.getProperty(key, Boolean.toString(val)).toLowerCase().trim();
     if (!pval.equals("true") && !pval.equals("false"))
       throw new LogConfigurationException(key + "[" + pval +
           "] must be true of false");
     
     val = Boolean.valueOf(pval).booleanValue();
     if (listConfig) System.err.println(key + ": " + val);;
     return val;
   }
   
   /**
    * called by parseProperties to obtain a String configuration
    * property and optionally display the configured value.
    * 
    * @param key name of parameter to return
    * @param val default value if the parameter is not configured
    * @return String value of the requested parameter
    */
   private String getString(String key, String val)
   {
     val = prop.getProperty(key, val);
     if (listConfig) System.err.println(key + ": " + val);;
     return val;
   }
   
   /**
    * initialize member variables from property file.
    * @throws LogConfigurationException
    * with text explaining the reason for the exception.
    */
   private void parseProperties() throws LogConfigurationException
   {
     String  val = null;
     String  key = null; 
     int     ival;
     
     listConfig = getBoolean("listConfig", listConfig);
 
     bufferClassName = getString("bufferClassName", bufferClassName);
     
     bufferSize = getInteger("bufferSize", (bufferSize / 1024), "Kb");
    if (bufferSize < 1 || bufferSize > MAX_BUFFER_SIZE)
       throw new LogConfigurationException("bufferSize [" + bufferSize + "] must be" +
          " between 1 and "+ MAX_BUFFER_SIZE);
     bufferSize *= 1024;
     showConfig("bufferSize", bufferSize, "bytes");
     
     checksumEnabled = getBoolean("checksumEnabled", checksumEnabled);
     
     flushSleepTime = getInteger("flushSleepTime", flushSleepTime);
     
     logFileDir = getString("logFileDir", logFileDir);
     
     logFileExt = getString("logFileExt", logFileExt);
     
     logFileName = getString("logFileName", logFileName);
 
     maxBlocksPerFile = getInteger("maxBlocksPerFile", maxBlocksPerFile);
     
     minBuffers = getInteger("minBuffers", minBuffers);
 
     maxBuffers = getInteger("maxBuffers", maxBuffers);
     
     maxLogFiles = getInteger("maxLogFiles", maxLogFiles);
     
     threadsWaitingForceThreshold = getInteger("threadsWaitingForceThreshold", threadsWaitingForceThreshold);
 
     if (maxBuffers > 0 && maxBuffers < minBuffers)
       throw new LogConfigurationException("minBuffers [" + minBuffers +
           "] must be <= than maxBuffers[" + maxBuffers +  "]");
     
     if (minBuffers <= 0)
       throw new LogConfigurationException("minBuffers[" + minBuffers + 
           "] must be > 0");
   }
   
   /* ---------------------------------------------------
    * LogBufferManager configuration settings
    * ---------------------------------------------------
    */
   
   /**
    * When set to <b> true </b> the configuration properties
    * are displayed to System.out following construction
    * of a Configuration object.
    * <p>Default is false --> config is not displayed
    */
   private boolean listConfig = false;
   
   /**
    * When set to <b> true </b> checksums are computed on the contents
    * of each buffer prior to writing buffer contents to disk.
    * 
    * <p>The checksum is used when blocks of data are retrieved
    * from the log during replay to validate the content of the
    * file.
    * <p>Default value is true.
    * <p>Setting this option to <b> false </b> may reduce
    * slightly the amount of cpu time that is used by the
    * logger. 
    */
   private boolean checksumEnabled = false;
 
   /**
    * Size (in K bytes) of buffers used to write log blocks.
    * 
    * <p>Specify values between 1 and 32 to allocate buffers
    * between 1K and 32K in size. 
    * 
    * <p>The default size of 4K bytes should be suitable
    * for most applications.
    * <p>Larger buffers may provide improved performance
    * for applications with transaction rates that
    * exceed 5K TX/Sec and a large number of threads.
    */
   private int bufferSize = 4 * 1024;
   
   /**
    * Name of class that implements LogBuffer used by
    * LogBufferManager.
    * <p>Class must extend LogBuffer.
    */
   private String bufferClassName = "org.objectweb.howl.log.BlockLogBuffer";
   
   /**
    * maximum number of buffers to be allocated by LogBufferManager.
    * <p>Default value is 0 (zero) -- no limit.
    */
   private int maxBuffers = 0;
   
   /**
    * minimum number of buffers to be allocated by LogBufferManager.
    * <p>Default value is 4.
    */
   private int minBuffers = 4;
 
   /**
    * The amount of time
    * (specified in number of milli-seconds)
    * the ForceManager sleeps between log forces.
    *  
    * <p>During periods of low activity, threads could
    * wait an excessive amount of time 
    * (possibly for ever) for buffers to fill and be
    * flushed.  To mitigate this situation, the
    * Logger runs a ForceManager thread that wakes
    * up periodically and forces IO when other
    * threads are waiting.  
    *
    * <p>The default value is 50 milli-seconds.
    */
   private int flushSleepTime = 50;
   
   /**
    * the maximum number of threads that should wait
    * for an IO force.
    * <p>Setting this value may have an effect
    * on latency when threads are waiting for
    * the force. 
    * 
    * <p>By default, there is no limit.
    */
   private int threadsWaitingForceThreshold = Integer.MAX_VALUE;
   
   /* ---------------------------------------------------
    * LogFileManager configuration settings
    * ---------------------------------------------------
    */
   
   /**
    * maximum number of blocks to store in each LogFile.
    * 
    * <p>controls when logging is switched to a new log file, and/or when a circular
    * log is reset to  seek address zero.
    */
   int maxBlocksPerFile = Integer.MAX_VALUE;
   
   /**
    * number of log files to configure.
    * <p>Default is 2 log files.
    */
   int maxLogFiles = 2;
   
   /**
    * directory used to create log files.
    * <p>Default is current directory.
    */
   String logFileDir = "../logs";
   
   /**
    * file name extension for log files.
    * <p>Default value is "log"
    */
   String logFileExt = "log";
   
   /**
    * filename used to create log files.
    * <p>Default value is "logger"
    * <p>file names are generated using the following pattern:
    * <pre>
    *   <value of logFileName> + "_" + <file number> + "." + <value of logFileExt>
    * </pre>
    */
   String logFileName = "logger";
   
   /**
    * @return Returns the logDir.
    */
   public String getLogFileDir() {
     return logFileDir;
   }
   /**
    * @param logDir The logDir to set.
    */
   public void setLogFileDir(String logDir) {
     this.logFileDir = logDir;
   }
   
   /**
    * @return Returns the logFileExt.
    */
   public String getLogFileExt() {
     return logFileExt;
   }
   /**
    * @param logFileExt The logFileExt to set.
    */
   public void setLogFileExt(String logFileExt) {
     this.logFileExt = logFileExt;
   }
   /**
    * @return Returns the logFileName.
    */
   public String getLogFileName() {
     return logFileName;
   }
   /**
    * @param logFileName The logFileName to set.
    */
   public void setLogFileName(String logFileName) {
     this.logFileName = logFileName;
   }
   /**
    * @return Returns the checksumEnabled option.
    */
   public boolean isChecksumEnabled() {
     return checksumEnabled;
   }
   
   /**
    * @param checksumOption The checksumOption to set.
    */
   public void setChecksumEnabled(boolean checksumOption) {
     this.checksumEnabled = checksumOption;
   }
   
   /**
    * @return Returns the bufferSize.
    */
   public int getBufferSize() {
     return bufferSize;
   }
   /**
    * @param bufferSize The size of a log buffer
    * specified as a number of 1024 byte blocks.
    * <p>The value specified by bufferSize is
    * multiplied by 1024 to establish the actual
    * buffer size used by the logger.
    */
   public void setBufferSize(int bufferSize) {
     this.bufferSize = bufferSize * 1024;
   }
   
   /**
    * @return Returns the bufferClassName.
    */
   public String getBufferClassName() {
     return bufferClassName;
   }
   /**
    * @param bufferClassName The bufferClassName to set.
    */
   public void setBufferClassName(String bufferClassName) {
     this.bufferClassName = bufferClassName;
   }
   
   /**
    * @return Returns the maxBuffers.
    */
   public int getMaxBuffers() {
     return maxBuffers;
   }
   /**
    * @param maxBuffers The maxBuffers to set.
    */
   public void setMaxBuffers(int maxBuffers) {
     this.maxBuffers = maxBuffers;
   }
   
   /**
    * @return Returns the minBuffers.
    */
   public int getMinBuffers() {
     return minBuffers;
   }
   /**
    * @param minBuffers The minBuffers to set.
    */
   public void setMinBuffers(int minBuffers) {
     this.minBuffers = minBuffers;
   }
   
   /**
    * @return Returns the flushSleepTime.
    */
   public int getFlushSleepTime() {
     return flushSleepTime;
   }
   /**
    * @param flushSleepTime The amount of time
    * (specified in milli-seconds) the FlushManager
    * should sleep.
    */
   public void setFlushSleepTime(int flushSleepTime) {
     this.flushSleepTime = flushSleepTime;
   }
   /**
    * @return Returns the threadsWaitingForceThreshold.
    */
   public int getThreadsWaitingForceThreshold() {
     return threadsWaitingForceThreshold;
   }
   /**
    * @param threadsWaitingForceThreshold The threadsWaitingForceThreshold to set.
    */
   public void setThreadsWaitingForceThreshold(int threadsWaitingForceThreshold) {
     this.threadsWaitingForceThreshold = threadsWaitingForceThreshold;
   }
   /**
    * @return Returns the maxBlocksPerFile.
    */
   public int getMaxBlocksPerFile() {
     return maxBlocksPerFile;
   }
   /**
    * @param maxBlocksPerFile The maxBlocksPerFile to set.
    */
   public void setMaxBlocksPerFile(int maxBlocksPerFile) {
     this.maxBlocksPerFile = maxBlocksPerFile;
   }
 
   /**
    * @return Returns the maxLogFiles.
    */
   public int getMaxLogFiles() {
     return maxLogFiles;
   }
   /**
    * @param maxLogFiles The maxLogFiles to set.
    */
   public void setMaxLogFiles(int maxLogFiles) {
     this.maxLogFiles = maxLogFiles;
   }
 }
