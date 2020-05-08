 /*
  * $Id$
  */
 package org.xins.util.io;
 
 import java.io.File;
 import org.xins.util.MandatoryArgumentChecker;
 
 /**
  * File watcher thread. This thread checks if a file changed and if it has, it
  * notifies the listener. The check is performed every <em>n</em> seconds,
  * where <em>n</em> can be configured.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.121
  */
 public final class FileWatcher extends Thread {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>FileWatcher</code> for the specified file.
     *
     * @param file
     *    the name of the file to watch, cannot be <code>null</code>.
     *
     * @param delay
     *    the delay in seconds, must be greater than or equal to 1.
     *
     * @throws IllegalArgumentException
     *    if <code>file == null || delay &lt; 1</code>
     */
    public FileWatcher(String file, int delay, Listener listener) {
 
       // Check preconditions
       MandatoryArgumentChecker.check("file", file);
       if (delay < 1) {
          throw new IllegalArgumentException("delay (" + delay + ") < 1");
       }
 
       // Store the information
       _file     = new File(file);
       _delay    = delay;
       _listener = listener;
 
       // Configure thread as daemon
       setDaemon(true);
 
       // Immediately check if the file exists
       check();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The file to watch. Not <code>null</code>.
     */
    private final File _file;
 
    /**
     * Delay in seconds, at least 1.
     */
    private final int _delay;
 
    /**
     * The listener. Not <code>null</code>
     */
    private final Listener _listener;
 
    /**
     * Timestamp of the last modification of the file. Initially this field is
     * <code>0L</code>.
     */
    private long _lastModified;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Runs this thread. This method should not be called directly, call
     * {@link #start()} instead. That method will call this method.
     *
     * @throws IllegalStateException
     *    if <code>{@link Thread#currentThread()} != this</code>.
     */
    public void run() throws IllegalStateException {
 
       // Check preconditions
       if (Thread.currentThread() != this) {
          throw new IllegalStateException("Thread.currentThread() != this");
       }
 
       try {
          while(true) {
             // Wait for the designated amount of time
             sleep(_delay);
 
             // Check if the file changed
             check();
          }
       } catch (InterruptedException exception) {
          // Fall through
       }
    }
 
    /**
     * Checks if the file changed. The following algorithm is used:
     *
     * <ul>
     *    <li>if the file does not exist, then {@link Listener#fileNotFound()} is called;
     *    <li>if the file was modified, then {@link Listener#fileModified()} is called;
    *    <li>if {@link File#exists()} or {@link File#lastModified()} throws a {@link SecurityException}, then {@link Listener#securityException()} is called;
     *    <li>if the file was not modified and no {@link SecurityException}, then {@link Listener#fileNotModified()} is called.
     * </ul>
     */
    private void check() {
 
       long lastModified;
       try {
          // If the file exists, then check when it was last modified...
          if (_file.exists()) {
             lastModified = _file.lastModified();
 
          // ...otherwise notify the listener and return.
          } else {
             _listener.fileNotFound();
             return;
          }
 
       // If there was an authorisation error, notify the listener.
       } catch (SecurityException exception) {
          _listener.securityException(exception);
          return;
       }
 
       // No authorisation error, check if the file was modified.
       if (lastModified > _lastModified) {
          _listener.fileModified();
       } else {
          _listener.fileNotModified();
       }
    }
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Interface for file watcher listeners.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.121
     */
    public interface Listener {
 
       /**
        * Callback method, called if the file is checked but cannot be found.
        */
       void fileNotFound();
 
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
