 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.downloadmanager;
 
 import org.openide.util.Cancellable;
 
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import de.cismet.security.WebDavClient;
 
 /**
  * DOCUMENT ME!
  *
  * @author   therter
  * @version  $Revision$, $Date$
  */
 public class WebDavDownload extends AbstractDownload implements Cancellable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final int MAX_BUFFER_SIZE = 1024;
 
     //~ Instance fields --------------------------------------------------------
 
     private WebDavClient client;
     private String path;
     private String filename;
     private String extension;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WebDavDownload object.
      *
      * @param  client     DOCUMENT ME!
      * @param  path       DOCUMENT ME!
      * @param  directory  DOCUMENT ME!
      * @param  title      DOCUMENT ME!
      * @param  filename   DOCUMENT ME!
      * @param  extension  DOCUMENT ME!
      */
     public WebDavDownload(final WebDavClient client,
             final String path,
             final String directory,
             final String title,
             final String filename,
             final String extension) {
         this.client = client;
         this.path = path;
         this.directory = directory;
         this.title = title;
         this.filename = filename;
         this.extension = extension;
 
         status = State.WAITING;
 
         determineDestinationFile(filename, extension);
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
             resp = client.getInputStream(path);
 
             out = new FileOutputStream(fileToSaveTo);
             boolean downloading = true;
             while (downloading) {
                 if (Thread.interrupted()) {
                     log.info("Download was interuppted");
                     out.close();
                     resp.close();
                     deleteFile();
                     return;
                 }
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
         if (!(obj instanceof WebDavDownload)) {
             return false;
         }
 
         final WebDavDownload other = (WebDavDownload)obj;
 
         boolean result = true;
 
         if ((this.path != other.path) && ((this.path == null) || !this.path.equals(other.path))) {
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
 
         hash = (43 * hash) + ((this.path != null) ? this.path.hashCode() : 0);
         hash = (43 * hash) + ((this.fileToSaveTo != null) ? this.fileToSaveTo.hashCode() : 0);
 
         return hash;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void deleteFile() {
         if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
             fileToSaveTo.delete();
         }
     }
 
     @Override
     public boolean cancel() {
        boolean cancelled = true;
        if (downloadFuture != null) {
            cancelled = downloadFuture.cancel(true);
        }
         if (cancelled) {
             status = State.ABORTED;
             stateChanged();
         }
         return cancelled;
     }
 }
