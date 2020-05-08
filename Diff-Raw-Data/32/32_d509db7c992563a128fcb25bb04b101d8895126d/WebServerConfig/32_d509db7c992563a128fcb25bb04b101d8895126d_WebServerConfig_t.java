 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.simplerestserver;
 
 import java.io.File;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * WebServerConfig.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0, 20100511
  */
 public final class WebServerConfig implements Cloneable {
 
     //~ Instance fields --------------------------------------------------------
 
     // not final to be able to reassign after clone()
     private transient Map<String, String> serverParams;
     private transient int port;
     private transient File logFile;
     private transient boolean debug;
     private transient boolean console;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WebServerConfig object. Equal to <code>WebServerConfig(port, null, false, false)</code>
      *
      * @param   port  the server port
      *
      * @throws  IllegalArgumentException  if the port or the logFile values do not satisfy several conditions
      * @throws  IllegalStateException     if the initialisation results in an illegal state
      *
      * @see     #WebServerConfig(int, java.io.File, boolean, boolean)
      */
     public WebServerConfig(final int port) throws IllegalArgumentException, IllegalStateException {
         this(port, null, false, false);
     }
 
     /**
      * Creates a new WebServerConfig object. Equal to <code>WebServerConfig(port, logFile, false, false)</code>
      *
      * @param   port     the server port
      * @param   logFile  the log file
      *
      * @throws  IllegalArgumentException  if the port or the logFile values do not satisfy several conditions
      * @throws  IllegalStateException     if the initialisation results in an illegal state
      *
      * @see     #WebServerConfig(int, java.io.File, boolean, boolean)
      */
     public WebServerConfig(final int port, final File logFile) throws IllegalArgumentException, IllegalStateException {
         this(port, logFile, false, false);
     }
 
     /**
      * Creates a new WebServerConfig object. Equal to <code>WebServerConfig(port, logFile, debug, false)</code>
      *
      * @param   port     the server port
      * @param   logFile  the log file
      * @param   debug    debug flag
      *
      * @throws  IllegalArgumentException  if the port or the logFile values do not satisfy several conditions
      * @throws  IllegalStateException     if the initialisation results in an illegal state
      *
      * @see     #WebServerConfig(int, java.io.File, boolean, boolean)
      */
     public WebServerConfig(final int port, final File logFile, final boolean debug) throws IllegalArgumentException,
         IllegalStateException {
         this(port, logFile, debug, false);
     }
 
     /**
      * Creates a new WebServerConfig object.
      *
      * @param   port     the server port
      * @param   logFile  the log file
      * @param   debug    debug flag
      * @param   console  console flag
      *
      * @throws  IllegalArgumentException  if the port or the logFile values do not satisfy several conditions
      * @throws  IllegalStateException     if the initialisation results in an illegal state
      *
      * @see     #setPort(int)
      * @see     #setLogFile(java.io.File)
      */
     public WebServerConfig(final int port, final File logFile, final boolean debug, final boolean console)
             throws IllegalArgumentException, IllegalStateException {
         setPort(port);
         setLogFile(logFile);
         this.debug = debug;
         this.console = console;
         serverParams = new HashMap<String, String>();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Sets the port number. Port number must be 1000 < <code>port</code> <= 66535.
      *
      * @param   port  the port number to set
      *
      * @throws  IllegalArgumentException  if the port number is not 1000 < <code>port</code> <= 66535
      */
     public void setPort(final int port) {
         if ((port < 1000) || (port > 65535)) {
             throw new IllegalArgumentException(
                 "port out of range: " // NOI18N
                         + port
                         + " | see http://www.iana.org/assignments/port-numbers"); // NOI18N
         }
         this.port = port;
     }
 
     /**
      * Gets the port number. Port number is guaranteed to be 1000 < <code>port</code> <= 66535.
      *
      * @return  the port number
      */
     public int getPort() {
         return port;
     }
 
     /**
      * Stores a the given value by using the given key. If the key is null or empty or the value is null or empty this
      * implementation returns false and nothing is done.
      *
      * @param   key    parameter key
      * @param   value  parameter value
      *
      * @return  true if the key and value were put into the server parameter map, false otherwise
      */
     public boolean putServerParam(final String key, final String value) {
         if ((key != null) && !key.isEmpty() && (value != null) && !value.isEmpty()) {
             serverParams.put(key, value);

             return true;
         }

         return false;
     }
 
     /**
      * Removes the given parameter from the server parameter map. If the key is null or empty or the parameter map does
      * not contain the specified key this implementation returns false.
      *
      * @param   key  the key to remove
      *
      * @return  true if the key was removed from the map, false otherwise.
      */
     public boolean removeServerParam(final String key) {
         if ((key != null) && !key.isEmpty()) {
             return serverParams.remove(key) != null;
         }
 
         return false;
     }
 
     /**
      * Removes all server parameters.
      */
     public void clearServerParams() {
         serverParams.clear();
     }
 
     /**
      * Adds all given parameters to the parameter map. If a server parameter already exists its value is replaced by the
      * new value of the given parameter map.
      *
      * @param  serverParams  the server parameters to add
      */
     public void putAllServerParams(final Map<String, String> serverParams) {
        this.serverParams.putAll(serverParams);
     }
 
     /**
      * Getter for the server parameter map.
      *
      * @return  the server parameter map
      */
     public Map<String, String> getServerParams() {
         return serverParams;
     }
 
     /**
      * Setter for the log file. If the log file is null this implementation tries to set the default log file specified
      * by {@link WebServerMain#OPTION_DEFAULT_LOG}. If the resulting log file (the given one or the default one) is not
      * a file or if the file is not writable an exception is thrown. If none of this happens the resulting log file will
      * be used.
      *
      * @param   logFile  the file to log to
      *
      * @throws  IllegalStateException     if the given log file was null and the default log file is not a file or
      *                                    cannot be written
      * @throws  IllegalArgumentException  if the given log file is not null and is not a file or cannot be written
      */
     public void setLogFile(final File logFile) {
         final File file;
         if ((logFile == null)) {
             file = new File(WebServerMain.OPTION_DEFAULT_LOGFILE);
             if (file.exists() && (!file.isFile() || !file.canWrite())) {
                 throw new IllegalStateException("cannot set log file to default: " + file);           // NOI18N
             }
         } else if (logFile.exists() && (!logFile.isFile() || !logFile.canWrite())) {
             throw new IllegalArgumentException("given log file no file or not writable: " + logFile); // NOI18N
         } else {
             file = logFile;
         }
 
         this.logFile = file;
     }
 
     /**
      * Getter for the log file.
      *
      * @return  the log file
      */
     public File getLogFile() {
         return logFile;
     }
 
     /**
      * Setter for the debug flag.
      *
      * @param  debug  the debug flag
      */
     public void setDebug(final boolean debug) {
         this.debug = debug;
     }
 
     /**
      * Indicates whether the debug flag is set or not.
      *
      * @return  true if the debug flag is set, or false otherwise
      */
     public boolean isDebug() {
         return debug;
     }
 
     /**
      * Setter for the console flag.
      *
      * @param  console  the console flag
      */
     public void setConsole(final boolean console) {
         this.console = console;
     }
 
     /**
      * Indicates whether the console flag is set or not.
      *
      * @return  true if the console flag is set, or false otherwise
      */
     public boolean isConsole() {
         return console;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  CloneNotSupportedException  DOCUMENT ME!
      */
     @Override
     public WebServerConfig clone() throws CloneNotSupportedException {
         final WebServerConfig clone = (WebServerConfig)super.clone();
         clone.serverParams = (Map)((HashMap)this.serverParams).clone();
         clone.logFile = new File(this.logFile.getPath());
 
         return clone;
     }
 }
