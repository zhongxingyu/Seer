 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.io;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.xins.common.Utils;
 
 /**
  * File watcher thread. This thread checks if a URL or a set of URLs
  * changed and if it has, it notifies the listener. 
  * The check is performed every <em>n</em> seconds, where <em>n</em> can be configured.
  *
  * <p>Initially this thread will be a daemon thread. This can be changed by
  * calling {@link #setDaemon(boolean)}.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 2.1
  */
 public class HTTPFileWatcher extends FileWatcher {
 
    /**
     * The URLs to watch. Not <code>null</code>.
     */
    private URL[] _urls;
 
    /**
     * Creates a new <code>HTTPFileWatcher</code> for the specified URL.
     *
     * <p>The interval must be set before the thread can be started.
     *
     * @param url
     *    the name of the URL to watch, cannot be <code>null</code>.
     *
     * @param listener
     *    the object to notify on events, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>url == null || listener == null</code>
     */
    public HTTPFileWatcher(String url, Listener listener)
    throws IllegalArgumentException {
       this(url, 0, listener);
    }
 
    /**
     * Creates a new <code>HTTPFileWatcher</code> for the specified URL, with the
     * specified interval.
     *
     * @param url
     *    the name of the URL to watch, cannot be <code>null</code>.
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
     *    if <code>url == null || listener == null || interval &lt; 0</code>
     */
    public HTTPFileWatcher(String url, int interval, Listener listener)
    throws IllegalArgumentException {
       this(new String[]{url}, interval, listener);
    }
 
    /**
     * Creates a new <code>HTTPFileWatcher</code> for the specified set of URLs, 
     * with the specified interval.
     *
     * @param urls
     *    the name of the URLs to watch, cannot be <code>null</code>.
     *    It should also have at least one URL and none of the URLs should be <code>null</code>.
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
     *    if <code>urls == null || listener == null || interval &lt; 0 || urls.length &lt; 1</code>
     *    or if one of the URL is <code>null</code>.
     */
    public HTTPFileWatcher(String[] urls, int interval, Listener listener)
    throws IllegalArgumentException {
 
       super(urls, interval, listener);
    }
 
    protected void storeFiles(String[] files) {
       try {
          _urls = new URL[files.length];
          _urls[0] = new URL(files[0]);
          _filePaths = files[0];
          for (int i = 1; i < files.length; i++) {
             _urls[i] = new URL(_urls[0], files[i]);
             _filePaths += ";" + _urls[i].getPath();
          }
       } catch (MalformedURLException murlex) {
         // TODO log
       }
    }
 
    protected void firstCheck() {
 
       for (int i = 0; i < _urls.length; i++) {
          URL url = _urls[i];
          try {
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.connect();
             if (connection.getResponseCode() < 400) {
                _lastModified = Math.max(_lastModified, connection.getHeaderFieldDate("Last-Modified", 0L));
             }
 
          // Ignore a IOException
          } catch (IOException exception) {
            Utils.logIgnoredException(exception);
 
          // Ignore a SecurityException
          } catch (SecurityException exception) {
             Utils.logIgnoredException(exception);
          }
       }
    }
 
    protected long getLastModified() throws SecurityException {
       long lastModified = 0L;
       for (int i = 0; i < _urls.length; i++) {
          URL url = _urls[i];
          try {
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setIfModifiedSince(_lastModified);
             connection.connect();
             if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
             } else if (connection.getResponseCode() < 400) {
                lastModified = Math.max(lastModified, connection.getHeaderFieldDate("Last-Modified", 0L));
             } else {
                return -1L;
             }
          } catch (IOException ioe) {
            // TODO log
          }
       }
       if (lastModified == 0L) {
          return _lastModified;
       } else {
          return lastModified;
       }
    }
 }
