 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2011 jweintraut
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.tools.gui.downloadmanager;
 
 import org.apache.log4j.Logger;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 
 import java.net.URL;
 
 import java.util.Observable;
 
 import de.cismet.security.AccessHandler.ACCESS_METHODS;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
 import de.cismet.security.exceptions.MissingArgumentException;
 import de.cismet.security.exceptions.NoHandlerForURLException;
 import de.cismet.security.exceptions.RequestFailedException;
 
 /**
  * The objects of this class represent a shape export download. Each download starts an own thread to download the
  * associated shape export. There are three states defined: DOWNLOADING, COMPLETE, ERROR. The objects of this class are
  * observed by the download manager.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class SingleDownload extends Observable implements Download, Runnable, Comparable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(SingleDownload.class);
     private static final int MAX_BUFFER_SIZE = 1024;
 
     //~ Instance fields --------------------------------------------------------
 
     private URL url;
     private String request;
     private String directory;
     private File fileToSaveTo;
     private State status;
     private String title;
     private Thread downloadThread;
     private Exception caughtException;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Constructor for Download.
      *
      * @param  url        The URL of the server to download from.
      * @param  request    The request to send.
      * @param  directory  Specifies in which directory to save the file. This should be specified relative to the
      *                    general download directory.
      * @param  title      The title of the download.
      * @param  filename   A String containing the filename.
      * @param  extension  A String containing the file extension.
      */
     public SingleDownload(final URL url,
             final String request,
             final String directory,
             final String title,
             final String filename,
             final String extension) {
         this.url = url;
         this.request = request;
         this.directory = directory;
         this.title = title;
 
         if (DownloadManager.instance().isEnabled()) {
             determineDestinationFile(filename, extension, url);
             status = State.WAITING;
         } else {
             status = State.COMPLETED_WITH_ERROR;
             caughtException = new Exception("DownloadManager is disabled. Cancelling download.");
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Returns the title of the download.
      *
      * @return  The title.
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Returns a file object pointing to the download location of this download.
      *
      * @return  A file object pointing to the download location.
      */
     @Override
     public File getFileToSaveTo() {
         return fileToSaveTo;
     }
 
     /**
      * Returns the status of this download. Is one of DOWNLOADING, COMPLETE or ERROR.
      *
      * @return  The status of this download.
      */
     @Override
     public State getStatus() {
         return status;
     }
 
     /**
      * Returns the exception which is caught during the download. If an exception occurs the download is aborted.
      *
      * @return  The caught exception.
      */
     public Exception getCaughtException() {
         return caughtException;
     }
 
     @Override
     public int getDownloadsTotal() {
         return 1;
     }
 
     @Override
     public int getDownloadsCompleted() {
         if (status == State.RUNNING) {
             return 0;
         }
 
         return 1;
     }
 
     @Override
     public int getDownloadsErraneous() {
         if (status == State.COMPLETED_WITH_ERROR) {
             return 1;
         }
 
         return 0;
     }
 
     /**
      * Logs a caught exception and sets some members accordingly.
      *
      * @param  exception  The caught exception.
      */
     private void error(final Exception exception) {
         LOG.error("Exception occurred while downloading '" + fileToSaveTo + "'.", exception);
         fileToSaveTo.deleteOnExit();
         caughtException = exception;
         status = State.COMPLETED_WITH_ERROR;
         stateChanged();
     }
 
     /**
      * Starts a thread which downloads the shape export.
      */
     @Override
     public void startDownload() {
         if (downloadThread == null) {
             downloadThread = new Thread(this);
             downloadThread.start();
         }
     }
 
     @Override
     public void run() {
         if (status != State.WAITING) {
             return;
         }
 
         status = State.RUNNING;
 
         FileOutputStream out = null;
         InputStream resp = null;
 
         stateChanged();
 
         try {
            if ("file".equals(url.getProtocol())) {
                resp = new FileInputStream(new File(url.toURI()));
             } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending request \n" + request + "\n to '" + url.toExternalForm() + "'.");
                }

                if ((request == null) || (request.trim().length() <= 0)) {
                    resp = WebAccessManager.getInstance().doRequest(url);
                } else {
                    resp = WebAccessManager.getInstance()
                                .doRequest(
                                        url,
                                        new StringReader(request),
                                        ACCESS_METHODS.POST_REQUEST);
                }
             }
 
             out = new FileOutputStream(fileToSaveTo);
             boolean downloading = true;
             while (downloading) {
                 // Size buffer according to how much of the file is left to download.
                 final byte[] buffer;
                 buffer = new byte[MAX_BUFFER_SIZE];
 
                 // Read from server into buffer.
                 final int read = resp.read(buffer);
                 if (read == -1) {
                     downloading = false;
                 } else {
                     // Write buffer to file.
                     out.write(buffer, 0, read);
                 }
             }
         } catch (MissingArgumentException ex) {
             error(ex);
         } catch (AccessMethodIsNotSupportedException ex) {
             error(ex);
         } catch (RequestFailedException ex) {
             error(ex);
         } catch (NoHandlerForURLException ex) {
             error(ex);
         } catch (Exception ex) {
             error(ex);
         } finally {
             // Close file.
             if (out != null) {
                 try {
                     out.close();
                 } catch (Exception e) {
                     LOG.warn("Exception occured while closing file.", e);
                 }
             }
 
             // Close connection to server.
             if (resp != null) {
                 try {
                     resp.close();
                 } catch (Exception e) {
                     LOG.warn("Exception occured while closing response stream.", e);
                 }
             }
         }
 
         if (status == State.RUNNING) {
             status = State.COMPLETED;
             stateChanged();
         }
     }
 
     /**
      * Determines the destination file for this download. There exist given parameters like a download destination and a
      * pattern for the file name. It's possible that a previous download with equal parameters still exists physically,
      * therefore this method adds a counter (2..999) which is appended to the filename.
      *
      * @param  filename   The file name for this download.
      * @param  extension  The extension for the downloaded file.
      * @param  url        The url is needed for logging purposes if it's not possible to determine a download file. It
      *                    mustn't be null.
      */
     private void determineDestinationFile(final String filename,
             final String extension,
             final URL url) {
         File directoryToSaveTo = null;
         if ((directory != null) && (directory.trim().length() > 0)) {
             directoryToSaveTo = new File(DownloadManager.instance().getDestinationDirectory(), directory);
         } else {
             directoryToSaveTo = DownloadManager.instance().getDestinationDirectory();
         }
 
         if (!directoryToSaveTo.exists()) {
             if (!directoryToSaveTo.mkdirs()) {
                 error(new Exception(
                         "Couldn't create destination directory '"
                                 + directoryToSaveTo.getAbsolutePath()
                                 + "'. Cancelling download."));
             }
         }
 
         fileToSaveTo = new File(directoryToSaveTo, filename + extension);
         boolean fileFound = false;
         int counter = 2;
 
         while (!fileFound) {
             while (fileToSaveTo.exists() && (counter < 1000)) {
                 fileToSaveTo = new File(directoryToSaveTo, filename + counter + extension);
                 counter++;
             }
 
             try {
                 fileToSaveTo.createNewFile();
 
                 if (fileToSaveTo.exists() && fileToSaveTo.isFile() && fileToSaveTo.canWrite()) {
                     fileFound = true;
                 }
             } catch (IOException ex) {
                 fileToSaveTo.deleteOnExit();
             }
 
             if ((counter >= 1000) && !fileFound) {
                 error(new FileNotFoundException(
                         "Could not create a file for the download '"
                                 + url.toExternalForm()
                                 + "'. The tested path is '"
                                 + directoryToSaveTo.getAbsolutePath()
                                 + File.separatorChar
                                 + filename
                                 + "<1.."
                                 + 999
                                 + ">."
                                 + extension
                                 + "."));
             }
         }
     }
 
     /**
      * Marks this observable as changed and notifies observers.
      */
     private void stateChanged() {
         setChanged();
         notifyObservers();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof SingleDownload)) {
             return false;
         }
 
         final SingleDownload other = (SingleDownload)obj;
 
         boolean result = true;
 
         if ((this.url != other.url) && ((this.url == null) || !this.url.equals(other.url))) {
             result &= false;
         }
         if ((this.request == null) ? (other.request != null) : (!this.request.equals(other.request))) {
             result &= false;
         }
         if ((this.fileToSaveTo == null) ? (other.fileToSaveTo != null)
                                         : (!this.fileToSaveTo.equals(other.fileToSaveTo))) {
             result &= false;
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
 
         hash = (43 * hash) + ((this.url != null) ? this.url.hashCode() : 0);
         hash = (43 * hash) + ((this.request != null) ? this.request.hashCode() : 0);
         hash = (43 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);
 
         return hash;
     }
 
     @Override
     public int compareTo(final Object o) {
         if (!(o instanceof SingleDownload)) {
             return 1;
         }
 
         final SingleDownload other = (SingleDownload)o;
         return this.title.compareTo(other.title);
     }
 }
