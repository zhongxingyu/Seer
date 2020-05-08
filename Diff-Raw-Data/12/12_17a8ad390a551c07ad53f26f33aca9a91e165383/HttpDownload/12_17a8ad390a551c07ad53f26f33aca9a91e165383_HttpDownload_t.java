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
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 
 import java.net.URL;
 
 import de.cismet.security.AccessHandler.ACCESS_METHODS;
 
 import de.cismet.security.WebAccessManager;
 
 import de.cismet.security.exceptions.AccessMethodIsNotSupportedException;
 import de.cismet.security.exceptions.MissingArgumentException;
 import de.cismet.security.exceptions.NoHandlerForURLException;
 import de.cismet.security.exceptions.RequestFailedException;
 
 /**
  * The objects of this class represent a HTTP download. The objects of this class are observed by the download manager.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class HttpDownload extends AbstractDownload {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final int MAX_BUFFER_SIZE = 1024;
 
     //~ Instance fields --------------------------------------------------------
 
     private URL url;
     private String request;
 
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
     public HttpDownload(final URL url,
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
             determineDestinationFile(filename, extension);
             status = State.WAITING;
         } else {
             status = State.COMPLETED_WITH_ERROR;
             caughtException = new Exception("DownloadManager is disabled. Cancelling download.");
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
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
                 if (log.isDebugEnabled()) {
                     log.debug("Sending request \n" + request + "\n to '" + url.toExternalForm() + "'.");
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
                     log.warn("Exception occured while closing file.", e);
                 }
             }
 
             // Close connection to server.
             if (resp != null) {
                 try {
                     resp.close();
                 } catch (Exception e) {
                     log.warn("Exception occured while closing response stream.", e);
                 }
             }
         }
 
         if (status == State.RUNNING) {
             status = State.COMPLETED;
             stateChanged();
         }
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof HttpDownload)) {
             return false;
         }
 
         final HttpDownload other = (HttpDownload)obj;
 
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
 }
