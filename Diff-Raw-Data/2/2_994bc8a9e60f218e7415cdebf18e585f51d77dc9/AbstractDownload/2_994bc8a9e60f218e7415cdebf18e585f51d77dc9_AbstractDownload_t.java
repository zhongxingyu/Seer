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
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.util.Observable;
 
 import javax.swing.JPanel;
 
 import de.cismet.tools.CismetThreadPool;
 
 /**
  * The objects of this class represent downloads. This class encompasses several default methods which should be the
  * same for most download which care about single files.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public abstract class AbstractDownload extends Observable implements Download, Runnable, Comparable {
 
     //~ Instance fields --------------------------------------------------------
 
     protected String directory;
     protected File fileToSaveTo;
     protected State status;
     protected String title;
     protected boolean started = false;
     protected Exception caughtException;
     protected final Logger log = Logger.getLogger(this.getClass());
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Returns the title of the download.
      *
      * @return  The title.
      */
     @Override
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
      * Returns the status of this download.
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
     @Override
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
     public int getDownloadsErroneous() {
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
     protected void error(final Exception exception) {
         log.error("Exception occurred while downloading '" + fileToSaveTo + "'.", exception);
         fileToSaveTo.deleteOnExit();
         caughtException = exception;
         status = State.COMPLETED_WITH_ERROR;
         stateChanged();
     }
 
     /**
      * Starts a thread which starts the download by starting this Runnable.
      */
     @Override
     public void startDownload() {
         if (!started) {
             started = true;
             CismetThreadPool.execute(this);
         }
     }
 
     @Override
     public JPanel getExceptionPanel(final Exception exception) {
         return null;
     }
 
     @Override
     public abstract void run();
 
     /**
      * Determines the destination file for this download. There exist given parameters like a download destination and a
      * pattern for the file name. It's possible that a previous download with equal parameters still exists physically,
      * therefore this method adds a counter (2..999) which is appended to the filename.
      *
      * @param  filename   The file name for this download.
      * @param  extension  The extension for the downloaded file.
      */
     protected void determineDestinationFile(final String filename,
             final String extension) {
         final File directoryToSaveTo;
         if ((directory != null) && (directory.trim().length() > 0)) {
             directoryToSaveTo = new File(DownloadManager.instance().getDestinationDirectory(), directory);
         } else {
             directoryToSaveTo = DownloadManager.instance().getDestinationDirectory();
         }
         if (log.isDebugEnabled()) {
             log.debug("Determined path '" + directoryToSaveTo + "' for file '" + filename + extension + "'.");
         }
 
         if (!directoryToSaveTo.exists()) {
             if (!directoryToSaveTo.mkdirs()) {
                 log.error("Couldn't create destination directory '"
                             + directoryToSaveTo.getAbsolutePath()
                             + "'. Cancelling download.");
                 error(new Exception(
                         "Couldn't create destination directory '"
                                 + directoryToSaveTo.getAbsolutePath()
                                 + "'. Cancelling download."));
                 return;
             }
         }
 
         fileToSaveTo = new File(directoryToSaveTo, filename + extension);
         boolean fileFound = false;
         int counter = 2;
 
         while (!fileFound) {
             while (fileToSaveTo.exists() && (counter < 1000)) {
                fileToSaveTo = new File(directoryToSaveTo, filename + "(" + counter + ")" + extension);
                 counter++;
             }
 
             try {
                 fileToSaveTo.createNewFile();
 
                 if (fileToSaveTo.exists() && fileToSaveTo.isFile() && fileToSaveTo.canWrite()) {
                     fileFound = true;
                 }
             } catch (IOException ex) {
                 log.warn("IOEXception while trying to create destination file '" + fileToSaveTo.getAbsolutePath()
                             + "'.",
                     ex);
                 fileToSaveTo.deleteOnExit();
             }
 
             if ((counter >= 1000) && !fileFound) {
                 log.error("Could not create a file for the download. The tested path is '"
                             + directoryToSaveTo.getAbsolutePath()
                             + File.separatorChar
                             + filename
                             + "<1.."
                             + 999
                             + ">."
                             + extension
                             + ".");
                 error(new FileNotFoundException(
                         "Could not create a file for the download. The tested path is '"
                                 + directoryToSaveTo.getAbsolutePath()
                                 + File.separatorChar
                                 + filename
                                 + "<1.."
                                 + 999
                                 + ">."
                                 + extension
                                 + "."));
                 return;
             }
         }
     }
 
     /**
      * Marks this observable as changed and notifies observers.
      */
     protected void stateChanged() {
         setChanged();
         notifyObservers();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof AbstractDownload)) {
             return false;
         }
 
         final AbstractDownload other = (AbstractDownload)obj;
 
         boolean result = true;
 
         if ((this.fileToSaveTo == null) ? (other.fileToSaveTo != null)
                                         : (!this.fileToSaveTo.equals(other.fileToSaveTo))) {
             result &= false;
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
 
         hash = (43 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);
 
         return hash;
     }
 
     @Override
     public int compareTo(final Object o) {
         if (!(o instanceof AbstractDownload)) {
             return 1;
         }
 
         final AbstractDownload other = (AbstractDownload)o;
         return this.title.compareTo(other.title);
     }
 }
