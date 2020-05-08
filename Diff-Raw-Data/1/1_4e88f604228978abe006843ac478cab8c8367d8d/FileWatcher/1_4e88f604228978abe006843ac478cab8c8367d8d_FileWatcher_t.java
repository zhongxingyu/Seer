 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.io;
 
 import java.io.File;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 
 /**
  * File watcher thread. This thread checks if a file or a set of files
  * changed and if it has, it notifies the listener. 
  * The check is performed every <em>n</em> seconds, where <em>n</em> can be configured.
  *
  * <p>Initially this thread will be a daemon thread. This can be changed by
  * calling {@link #setDaemon(boolean)}.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.0.0
  */
 public class FileWatcher extends Thread {
 
    /**
     * Instance counter. Used to generate a unique ID for each instance.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * Lock object for <code>INSTANCE_COUNT</code>. Never <code>null</code>.
     */
    private static final Object INSTANCE_COUNT_LOCK = new Object();
 
    /**
     * State in which this file watcher thread is not running.
     */
    private static final int NOT_RUNNING = 1;
 
    /**
     * State in which this file watcher thread is currently running and has not
     * been told to stop.
     */
    private static final int RUNNING = 2;
 
    /**
     * State in which this file watcher thread is currently running, but has
     * been told to stop.
     */
    private static final int SHOULD_STOP = 3;
 
    /**
     * Fully-qualified name of this class.
     */
    private final String _className;
 
    /**
     * Unique instance identifier.
     */
    private final int _instanceID;
 
    /**
     * The files to watch. Not <code>null</code>.
     */
    private File[] _files;
 
    /**
     * The string representation of the files to watch. Not <code>null</code>.
     */
    protected String _filePaths;
 
    /**
     * Delay in seconds, at least 1. When the interval is uninitialized, the
     * value of this field is less than 1.
     */
    private int _interval;
 
    /**
     * The listener. Not <code>null</code>
     */
    private final Listener _listener;
 
    /**
     * Timestamp of the last modification of the file. The value
     * <code>-1L</code> indicates that the file could not be found the last
     * time this was checked.
     *
     * <p>Initially this field is <code>-1L</code>.
     */
    protected long _lastModified;
 
    /**
     * Current state. Never <code>null</code>. Value is one of the following
     * values:
     *
     * <ul>
     *    <li>{@link #NOT_RUNNING}
     *    <li>{@link #RUNNING}
     *    <li>{@link #SHOULD_STOP}
     * </ul>
     *
     * Once the thread is stopped, the state will be changed to
     * {@link #NOT_RUNNING} again.
     */
    private int _state;
 
    /**
     * Creates a new <code>FileWatcher</code> for the specified file.
     *
     * <p>The interval must be set before the thread can be started.
     *
     * @param file
     *    the name of the file to watch, cannot be <code>null</code>.
     *
     * @param listener
     *    the object to notify on events, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>file == null || listener == null</code>
     *
     * @since XINS 1.2.0
     */
    public FileWatcher(String file, Listener listener)
    throws IllegalArgumentException {
       this(file, 0, listener);
    }
 
    /**
     * Creates a new <code>FileWatcher</code> for the specified file, with the
     * specified interval.
     *
     * @param file
     *    the name of the file to watch, cannot be <code>null</code>.
     *
     * @param interval
     *    the interval in seconds, must be greater than or equal to 0.
     *    if the interval is 0 the interval must be set before the thread can
     *    be started.
     *
     * @param listener
     *    the object to notify on events, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>file == null || listener == null || interval &lt; 0</code>
     */
    public FileWatcher(String file, int interval, Listener listener)
    throws IllegalArgumentException {
       this(new String[]{file}, interval, listener);
    }
 
    /**
     * Creates a new <code>FileWatcher</code> for the specified set of files, 
     * with the specified interval.
     *
     * @param files
     *    the name of the files to watch, cannot be <code>null</code>.
     *    It should also have at least one file and none of the file should be <code>null</code>.
     *
     * @param interval
     *    the interval in seconds, must be greater than or equal to 0.
     *    if the interval is 0 the interval must be set before the thread can
     *    be started.
     *
     * @param listener
     *    the object to notify on events, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>files == null || listener == null || interval &lt; 0 || files.length &lt; 1</code>
     *    or if one of the file is <code>null</code>.
     */
    public FileWatcher(String[] files, int interval, Listener listener)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("files", files, "listener", listener);
       if (interval < 0) {
          throw new IllegalArgumentException("interval (" + interval + ") < 0");
       }
       if (files.length < 1) {
          throw new IllegalArgumentException("At least one file should be specified.");
       }
       for (int i = 0; i < files.length; i++) {
          if (files[i] == null) {
             throw new IllegalArgumentException("The file specified at index " + i + " is null.");
          }
       }
 
       _className = getClass().getName();
 
       // Determine the unique instance ID
       int instanceID;
       synchronized (INSTANCE_COUNT_LOCK) {
          instanceID = INSTANCE_COUNT++;
       }
 
       // Initialize the fields
       _instanceID    = instanceID;
       storeFiles(files);
       _interval      = interval;
       _listener      = listener;
       _state         = NOT_RUNNING;
 
       // Configure thread as daemon
       setDaemon(true);
 
       // Set the name of this thread
       configureThreadName();
 
       // Immediately check if the file can be read from
       firstCheck();
    }
 
    /**
     * Stores the files in a class variable.
     * 
     * @param files
     *    the String files to check, cannot be <code>null</code>.
     */
    protected void storeFiles(String[] files) {
       _files = new File[files.length];
       _files[0] = new File(files[0]);
       File baseDir = _files[0].getParentFile();
       _filePaths = _files[0].getPath();
       for (int i = 1; i < files.length; i++) {
          _files[i] = new File(baseDir, files[i]);
          _filePaths += ";" + _files[i].getPath();
       }
    }
 
    /**
     * Configures the name of this thread.
     */
    private synchronized void configureThreadName() {
       String name = _className + " #" + _instanceID + " [files=\"" + 
             _filePaths + "\"; interval=" + _interval + ']';
       setName(name);
    }
 
    /**
     * Performs the first check on the file to determine the date the file was
     * last modified. This method is called from the constructors. If the file
     * cannot be accessed due to a {@link SecurityException}, then this
     * exception is logged and ignored.
     */
    protected void firstCheck() {
 
       for (int i = 0; i < _files.length; i++) {
          File file = _files[i];
          try {
             if (file.canRead()) {
                _lastModified = Math.max(_lastModified, file.lastModified());
             }
 
          // Ignore a SecurityException
          } catch (SecurityException exception) {
             Utils.logIgnoredException(exception);
          }
       }
    }
 
    /**
     * Runs this thread. This method should not be called directly, call
     * {@link #start()} instead. That method will call this method.
     *
     * @throws IllegalStateException
     *    if <code>{@link Thread#currentThread()} != this</code>, if the thread
     *    is already running or should stop, or if the interval was not set
     *    yet.
     */
    public void run() throws IllegalStateException {
 
       int interval;
       int state;
 
       synchronized (this) {
          interval = _interval;
          state    = _state;
       }
 
       // Check preconditions
       if (Thread.currentThread() != this) {
          throw new IllegalStateException("Thread.currentThread() != this");
       } else if (state == RUNNING) {
          throw new IllegalStateException("Thread already running.");
       } else if (state == SHOULD_STOP) {
          throw new IllegalStateException("Thread should stop running.");
       } else if (interval < 1) {
          throw new IllegalStateException("Interval has not been set yet.");
       }
 
       Log.log_1200(_instanceID, _filePaths, interval);
 
       // Move to the RUNNING state
       synchronized (this) {
          _state = RUNNING;
       }
 
       // Loop while we should keep running
       boolean shouldStop = false;
       while (! shouldStop) {
          synchronized (this) {
             try {
 
                // Wait for the designated amount of time
                wait(((long) interval) * 1000L);
 
             } catch (InterruptedException exception) {
                // The thread has been notified
             }
 
             // Should we stop?
             shouldStop = (_state != RUNNING);
          }
 
          // If we do not have to stop yet, check if the file changed
          if (! shouldStop) {
             check();
          }
       }
 
       // Thread stopped
       Log.log_1203(_instanceID, _filePaths);
    }
 
    /**
     * Returns the current interval.
     *
     * @return interval
     *    the current interval in seconds, always greater than or equal to 1,
     *    except if the interval is not initialized yet, in which case 0 is
     *    returned.
     */
    public synchronized int getInterval() {
       return _interval;
    }
 
    /**
     * Changes the file check interval.
     *
     * @param newInterval
     *    the new interval in seconds, must be greater than or equal to 1.
     *
     * @throws IllegalArgumentException
     *    if <code>interval &lt; 1</code>
     */
    public synchronized void setInterval(int newInterval)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (newInterval < 1) {
          throw new IllegalArgumentException(
             "newInterval (" + newInterval + ") < 1");
       }
 
       // Change the interval
       if (newInterval != _interval) {
          Log.log_1201(_instanceID, _filePaths, _interval, newInterval);
          _interval = newInterval;
       }
 
       // Update the thread name
       configureThreadName();
 
       // TODO: Interrupt the thread (see #HERE#)
    }
 
    /**
     * Stops this thread.
     *
     * @throws IllegalStateException
     *    if the thread is currently not running or already stopping.
     */
    public synchronized void end() throws IllegalStateException {
 
       // Check state
       if (_state == NOT_RUNNING) {
          throw new IllegalStateException("Thread currently not running.");
       } else if (_state == SHOULD_STOP) {
          throw new IllegalStateException("Thread already stopping.");
       }
 
       Log.log_1202(_instanceID, _filePaths);
 
       // Change the state and interrupt the thread
       _state = SHOULD_STOP;
       this.interrupt();
    }
 
    /**
     * Checks if the file changed. The following algorithm is used:
     *
     * <ul>
     *    <li>check if the file is readable;
     *    <li>if so, then determine when the file was last modified;
     *    <li>if either the file existence check or the file modification check
     *        causes a {@link SecurityException} to be thrown, then
     *        {@link Listener#securityException(SecurityException)} is called
     *        and the method returns;
     *    <li>otherwise if the file is not readable (it may not exist), then
     *        {@link Listener#fileNotFound()} is called and the method returns;
     *    <li>otherwise if the file is readable, but previously was not,
     *        then {@link Listener#fileFound()} is called and the method
     *        returns;
     *    <li>otherwise if the file was modified, then
     *        {@link Listener#fileModified()} is called and the method returns;
     *    <li>otherwise the file was not modified, then
     *        {@link Listener#fileNotModified()} is called and the method
     *        returns.
     * </ul>
     *
     * @since XINS 1.2.0
     */
    public synchronized void check() {
 
       // Variable to store the file modification timestamp in. The value -1
       // indicates the file does not exist.
       long lastModified = 0L;
 
       // Check if the file can be read from and if so, when it was last
       // modified
       try {
         lastModified = getLastModified();
 
       // Authorisation problem; our code is not allowed to call canRead()
       // and/or lastModified() on the File object
       } catch (SecurityException securityException) {
 
          // Notify the listener
          try {
             _listener.securityException(securityException);
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
          }
 
          // Short-circuit
          return;
       }
 
       // A least one file can not be found
       if (lastModified == -1L) {
 
          // Set _lastModified to -1, which indicates the file did not exist
          // last time it was checked.
          _lastModified = -1L;
 
          // Notify the listener
          try {
             _listener.fileNotFound();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
          }
 
       // Previously a file could not be found, but now it can
       } else if (_lastModified == -1L) {
 
          // Update the field that stores the last known modification date
          _lastModified = lastModified;
 
          // Notify the listener
          try {
             _listener.fileFound();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
          }
 
       // At least one file has been modified
       } else if (lastModified != _lastModified) {
 
          // Update the field that stores the last known modification date
          _lastModified = lastModified;
 
          // Notify listener
          try {
             _listener.fileModified();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
          }
 
       // None of the files has not been modified
       } else {
 
          // Notify listener
          try {
             _listener.fileNotModified();
 
          // Ignore any exceptions thrown by the listener callback method
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
          }
       }
    }
 
    /**
     * Gets the time at which the last file was modified.
     * If for any reason, a file could no be read -1 is returned.
     * 
     * @return
     *    the time of the last modified file or -1.
     * 
     * @throws SecurityException
     *    if one of the file could not be read because of a security issue.
     */
    protected long getLastModified() throws SecurityException {
       long lastModified = 0L;
       for (int i = 0; i < _files.length; i++) {
          File file = _files[i];
          if (file.canRead()) {
             lastModified = Math.max(lastModified, file.lastModified());
          } else {
             return -1L;
          }
       }
       return lastModified;
    }
 
    /**
     * Interface for file watcher listeners.
     *
     * <p>Note that exceptions thrown by these callback methods will be ignored
     * by the <code>FileWatcher</code>.
     *
     * @version $Revision$ $Date$
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     *
     * @since XINS 1.0.0
     */
    public interface Listener {
 
       /**
        * Callback method, called if the file is checked but cannot be found.
        * This method is called the first time the file is determined not to
        * exist, but also each consecutive time the file is still determined
        * not to be found.
        */
       void fileNotFound();
 
       /**
        * Callback method, called if the file is found for the first time since
        * the <code>FileWatcher</code> was started. Each consecutive time the
        * file still exists (and is readable), either
        * {@link #fileModified()} or {@link #fileNotModified()} is called.
        */
       void fileFound();
 
       /**
        * Callback method, called if an authorisation error prevents that the
        * file is checked for existence and last modification date.
        *
        * @param exception
        *    the caught exception, not <code>null</code>.
        */
       void securityException(SecurityException exception);
 
       /**
        * Callback method, called if the file was checked and found to be
        * modified.
        */
       void fileModified();
 
       /**
        * Callback method, called if the file was checked but found not to be
        * modified.
        */
       void fileNotModified();
    }
 }
