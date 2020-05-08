 package wjhk.jupload2.upload;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JProgressBar;
 import javax.swing.Timer;
 
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.gui.JUploadPanel;
 import wjhk.jupload2.gui.filepanel.FilePanel;
 import wjhk.jupload2.gui.filepanel.SizeRenderer;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class is responsible for managing the upload. At the end of the upload,
  * the {@link JUploadPanel#updateButtonState()} is called, to refresh the button
  * state. Its job is to: <DIR>
  * <LI>Prepare upload for the file (calls to {@link FileData#beforeUpload()}
  * for each file in the file list.
  * <LI>Create the thread to send a packet of files.
  * <LI>Prepare the packets, that will be red by the upload thread.
  * <LI>Manage the end of upload: trigger the call to
  * {@link JUploadPanel#updateButtonState()} and the call to
  * {@link UploadPolicy#afterUpload(Exception, String)}.
  * <LI>Manage the 'stop' button reaction. </DIR> This class is created by
  * {@link JUploadPanel}, when the user clicks on the upload button.
  * 
  * @author etienne_sf
  */
 public class FileUploadManagerThread extends Thread implements ActionListener {
 
     // /////////////////////////////////////////////////////////////////////////////////////////
     // //////////////////// Possible Status for file upload
     // /////////////////////////////////////////////////////////////////////////////////////////
 
     /** Indicates that nothings has begun */
     public static final int UPLOAD_STATUS_NOT_STARTED = 1;
 
     /**
      * We're sending data to the server, for the file identified by
      * numOfFileInCurrentRequest.
      */
     public static final int UPLOAD_STATUS_UPLOADING = 2;
 
     /**
      * All data for the file identified by numOfFileInCurrentRequest has been
      * sent. But the server response has not been received.
      */
     public static final int UPLOAD_STATUS_UPLOADED_WAITING_FOR_RESPONSE = 3;
 
     /**
      * The upload for the file identified by numOfFileInCurrentRequest is
      * finished
      */
     public static final int UPLOAD_STATUS_UPLOADED = 4;
 
     // /////////////////////////////////////////////////////////////////////////////////////////
     // //////////////////// Possible Status for file upload
     // /////////////////////////////////////////////////////////////////////////////////////////
 
     /** The current file list. */
     private FilePanel filePanel = null;
 
     /**
      * The upload thread, that will wait for the next file packet to be ready,
      * then send it.
      */
     private FileUploadThread fileUploadThread = null;
 
     /** @see UploadPolicy#getMaxChunkSize() */
     long maxChunkSize = -1;
 
     /** Contains the sum of the files, ready for upload, and not uploaded yet */
     private long nbBytesReadyForUpload = 0;
 
     /**
      * Number of files that has been read by the {@link FileUploadThread}
      * thread. These files have been read by the {@link #getNextPacket()}
      * method.
      */
     private int nbFilesBeingUploaded = 0;
 
     /** @see UploadPolicy#getNbFilesPerRequest() */
     int nbFilesPerRequest = -1;
 
     /**
      * Number of files that are prepared for upload. A file is prepared for
      * upload, if the {@link FileData#beforeUpload()} has been called.
      */
     private int nbPreparedFiles = 0;
 
     /**
      * Number of files that have already been uploaded. Include the success
      * server response.
      */
     private int nbUploadedFiles = 0;
 
     /**
      * Indicated the number of bytes that have currently been sent for the
      * current file. This allows the management of the progress bar.
      */
     private long nbBytesUploadedForCurrentFile = 0;
 
     /**
      * Sum of the length for all prepared files. This allow the calculation of
      * the estimatedTotalLength.
      * 
      * @see #anotherFileHasBeenUploaded(FileData)
      */
     private long nbTotalNumberOfPreparedBytes = 0;
 
     /**
      * During the upload, when uploading several files in one packet, this
      * attribute indicates which file is currently being uploaded.
      */
     private int numOfFileInCurrentRequest = 0;
 
     /** Indicates what is the current file being uploaded, and its upload status. */
     private int uploadStatus = UPLOAD_STATUS_NOT_STARTED;
 
     /**
      * Contains the next packet to upload.
      * 
      * @see #getNextPacket()
      */
     private UploadFileData[] nextPacket = null;
 
     /**
      * The {@link JUploadPanel} progress bar, to follow the file preparation
      * progress.
      */
     private JProgressBar preparationProgressBar = null;
 
     /**
      * The {@link JUploadPanel} progress bar, to follow the upload of the
      * prepared files to the server.
      */
     private JProgressBar uploadProgressBar = null;
 
     /**
      * Indicates whether the upload is finished or not. Passed to true in the
      * {@link #run()} method.
      */
     private boolean uploadFinished = false;
 
     /**
      * Contains the time of the actual start of upload. Doesn't take into
      * account the time for preparing files.
      */
     private long uploadStartTime = 0;
 
     /**
      * If set to 'true', the thread will stop the crrent upload. This attribute
      * is not private as the {@link UploadFileData} class us it.
      * 
      * @see UploadFileData#uploadFile(java.io.OutputStream, long)
      */
     private boolean stop = false;
 
     /**
      * Contains an estimation of the total number of bytes to upload. It's the
      * average upload file size, divided by the total number of files. It is
      * calculated in {@link  #anotherFileIsReady(FileData)}.
      */
     private long estimatedTotalLength = 0;
 
     /** Thread Exception, if any occurred during upload. */
     private JUploadException uploadException = null;
 
     /** Current number of bytes that have been uploaded. */
     private long uploadedLength = 0;
 
     /** A shortcut to the upload panel */
     private JUploadPanel uploadPanel = null;
 
     /** The current upload policy. */
     private UploadPolicy uploadPolicy = null;
 
     /** The list of files to upload */
     private UploadFileData[] uploadFileDataArray = null;
 
     // ////////////////////////////////////////////////////////////////////////////
     // To follow the upload speed.
     // ////////////////////////////////////////////////////////////////////////////
 
     // Timeout at DEFAULT_TIMEOUT milliseconds
     private final static int DEFAULT_TIMEOUT = 100;
 
     /**
      * The upload status (progress bar) gets updated every (DEFAULT_TIMEOUT *
      * PROGRESS_INTERVAL) ms.
      */
     private final static int PROGRESS_INTERVAL = 10;
 
     /**
      * The counter for updating the upload status. The upload status (progress
      * bar) gets updated every (DEFAULT_TIMEOUT * PROGRESS_INTERVAL) ms.
      */
     private int update_counter = 0;
 
     /**
      * Used to wait for the upload to finish.
      */
     private Timer timerUpload = new Timer(DEFAULT_TIMEOUT, this);
 
     /**
      * Standard constructor of the class.
      * 
      * @param uploadPolicy
      */
     public FileUploadManagerThread(UploadPolicy uploadPolicy) {
         super("FileUploadManagerThread thread");
 
         // General shortcuts on the current applet.
         this.uploadPolicy = uploadPolicy;
         this.uploadPanel = uploadPolicy.getApplet().getUploadPanel();
         this.filePanel = this.uploadPanel.getFilePanel();
         this.uploadProgressBar = this.uploadPanel.getUploadProgressBar();
         this.preparationProgressBar = this.uploadPanel
                 .getPreparationProgressBar();
 
         // Let's start the upload thread. It will wait until the first
         // packet is ready.
         createUploadThread();
 
         // Let's store some upload parameters, to avoid querying all the time.
         this.nbFilesPerRequest = this.uploadPolicy.getNbFilesPerRequest();
         this.maxChunkSize = this.uploadPolicy.getMaxChunkSize();
 
         // Prepare the list of files to upload. We do this here, to minimize the
         // risk of concurrency, if the user drops or pastes files on the applet
         // while uploading.
         FileData[] fileDataArray = this.uploadPanel.getFilePanel().getFiles();
         this.uploadFileDataArray = new UploadFileData[fileDataArray.length];
         for (int i = 0; i < this.uploadFileDataArray.length; i += 1) {
             this.uploadFileDataArray[i] = new UploadFileData(fileDataArray[i],
                     this, this.uploadPolicy);
         }
     }
 
     /**
      * The heart of the program. This method prepare the upload, then calls
      * doUpload for each HTTP request.
      * 
      * @see java.lang.Thread#run()
      */
     @Override
     final public void run() {
         try {
             this.uploadPolicy.displayDebug(
                     "Start of the FileUploadManagerThread", 5);
 
             // The upload is started. Let's change the button state.
             this.uploadPanel.updateButtonState();
 
             // Let's prepare the progress bar, to display the current upload
             // stage.
             initProgressBar();
 
             // Create a timer, to update the status bar.
             this.timerUpload.start();
             this.uploadPolicy.displayDebug("Timer started", 50);
 
             // We have to prepare the files, then to create the upload thread
             // for
             // each file packet.
             prepareFiles();
 
             // The thread upload may need some information about the current
             // one,
             // like ... knowing that upload is actually finished (no more file
             // to
             // send).
             while (this.fileUploadThread != null
                     && this.fileUploadThread.isAlive()
                     && this.nbUploadedFiles < this.uploadFileDataArray.length
                     && !this.stop) {
                 try {
                     this.fileUploadThread.join(100);
                 } catch (InterruptedException e) {
                     // This should not occur, and should not be a problem. Let's
                     // trace a warning info.
                     this.uploadPolicy
                             .displayWarn("An InterruptedException occured in FileUploadManagerThread.run()");
                 }
             }
 
             // The upload is finished.
             this.uploadFinished = true;
 
             // Let's restore the button state.
             this.uploadPanel.updateButtonState();
             this.uploadPolicy.getApplet().getAppletContext().showStatus("");
             this.uploadPolicy.getApplet().getUploadPanel().getStatusLabel()
                     .setText("");
 
             // If no error occurs, we tell to the upload policy that a
             // successful
             // upload has been done.
 
             if (getUploadException() == null) {
                 afterUploadOk();
            } else {
                this.uploadPolicy.sendDebugInformation("Error in Upload",
                        getUploadException());
             }
 
             // We wait for 5 seconds, and clear the progress bar.
             try {
                 sleep(5000);
             } catch (InterruptedException e) {
                 // Nothing to do
             }
             // The job is finished for long enough, let's clear the progression
             // bars.
             this.preparationProgressBar.setValue(0);
             this.preparationProgressBar.setString("");
             this.uploadProgressBar.setValue(0);
             this.uploadProgressBar.setString("");
 
             this.uploadPolicy.displayDebug(
                     "End of the FileUploadManagerThread", 5);
         } catch (JUploadException jue) {
             // Let's have a little information.
             if (this.uploadException == null) {
                 this.uploadException = jue;
             }
             this.uploadPolicy.displayErr(
                     "Uncaught exception in FileUploadManagerThread/run()", jue);
 
             // And go back into a 'normal' way.
             stopUpload();
 
             // We restore the button state, just to be sure.
             this.uploadPanel.updateButtonState();
         }
 
         // And we die of our beautiful death ... until next upload.
     }// run
 
     /**
      * Returns the estimated length of the whole upload. This is an estimation,
      * as file are prepared in a thread and uploaded in another. The more file
      * are prepared, the most accurate this estimation is. When all files are
      * prepared, this is no more an estimation, but the real count. This value
      * is calculated in the anotherFileIsReady(FileData) method.
      * 
      * @return The estimation of the total number of bytes to upload, based on
      *         the average size of the already prepared files.
      */
     public long getEstimatedTotalLength() {
         return this.estimatedTotalLength;
 
     }
 
     /**
      * Get the exception that occurs during upload.
      * 
      * @return The exception, or null if no exception were thrown.
      */
     public Exception getException() {
         return this.uploadException;
     }
 
     /**
      * Get the total number of files which have been successfully uploaded.
      * 
      * @return Total number of uploaded files.
      */
     public int getNbUploadedFiles() {
         return this.nbUploadedFiles;
     }
 
     /**
      * Retrieve the start time of the upload. That is: when the first upload
      * starts. It can be some delay after this thread creation, as it first need
      * to prepare files to upload.
      * 
      * @return The time this thread was started in ms.
      */
     public final long getUploadStartTime() {
         return this.uploadStartTime;
     }
 
     /**
      * Return the number of bytes for the files that have been successfully
      * uploaded. This doesn't count the additional information, like HTTP
      * headers, form data...
      * 
      * @return Total number of uploaded bytes.
      * 
      */
     public long getUploadedLength() {
         return this.uploadedLength;
     }
 
     /**
      * Stores the last upload exception that occurs. This method won't write to
      * the log file.
      * 
      * @param uploadException
      */
     public void setUploadException(JUploadException uploadException) {
         this.uploadPolicy.displayErr(uploadException);
         this.uploadException = uploadException;
     }
 
     /**
      * Get the last upload exception that occurs.
      * 
      * @return The last upload exception, or null if no exception occurs.
      */
     public JUploadException getUploadException() {
         return this.uploadException;
     }
 
     /**
      * Indicates whether the upload is finished or not. As several conditions
      * can make the upload being finished (all files uploaded, an error occured,
      * the user stops the upload), a specific boolean is built. It's managed by
      * the {@link #run()} method.
      * 
      * @return true if the upload is finished. False otherwise.
      */
     public boolean isUploadFinished() {
         // Indicate whether or not the upload is finished. Several condit
         return this.uploadFinished;
     }
 
     /**
      * Indicates if the upload has been stopped by the user, or by any upload
      * error.
      * 
      * @return true if the current upload has been asked to stop by the user,
      *         false otherwise.
      */
     public boolean isUploadStopped() {
         return this.stop;
     }
 
     /**
      * Used by the UploadFileData#uploadFile(java.io.OutputStream, long) for
      * each uploaded buffer
      * 
      * @param nbBytes Number of additional bytes that where uploaded.
      * @throws JUploadException
      */
     public synchronized void nbBytesUploaded(long nbBytes)
             throws JUploadException {
         this.uploadedLength += nbBytes;
         this.nbBytesUploadedForCurrentFile += nbBytes;
         // Let's display some information
         updateUploadProgressBar();
     }
 
     /**
      * Indicate the current state of the upload, to allow a correct display of
      * the upload progress bar.
      * 
      * @param numOfFileInCurrentRequest
      * @param uploadStatus
      * @throws JUploadException
      */
     public synchronized void setUploadStatus(int numOfFileInCurrentRequest,
             int uploadStatus) throws JUploadException {
         this.numOfFileInCurrentRequest = numOfFileInCurrentRequest;
         this.uploadStatus = uploadStatus;
 
         this.updateUploadProgressBar();
     }
 
     /**
      * Reaction to the user click on the 'Stop' button, or any action from the
      * user asking to stop the upload. The upload should go on for the current
      * file, and stop before starting the next upload request to the server, to
      * avoid strange problems on the server.
      */
     public void stopUpload() {
         this.stop = true;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// TIMER MANAGEMENT
     // (to display the current upload status in the status bar)
     // //////////////////////////////////////////////////////////////////////////////////////////////
     /**
      * @param e
      */
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() instanceof Timer) {
             // If the upload is finished, we stop the timer here.
             if (isUploadFinished()) {
                 this.timerUpload.stop();
             } else if ((this.update_counter++ > PROGRESS_INTERVAL)
                     || (!this.fileUploadThread.isAlive())) {
                 updateUploadStatusBar();
             }
         }
     }
 
     /**
      * Displays the current upload speed on the status bar.
      */
     private void updateUploadStatusBar() {
         // Time for an update now.
         this.update_counter = 0;
         if (null != this.uploadProgressBar && (getUploadStartTime() != 0)) {
             long duration = (System.currentTimeMillis() - getUploadStartTime()) / 1000;
             double done = getUploadedLength();
             double total = getEstimatedTotalLength();
             double percent;
             double cps;
             long remaining;
             String eta;
             try {
                 percent = 100.0 * done / total;
             } catch (ArithmeticException e1) {
                 percent = 100;
             }
             try {
                 cps = done / duration;
             } catch (ArithmeticException e1) {
                 cps = done;
             }
             try {
                 remaining = (long) ((total - done) / cps);
                 if (remaining > 3600) {
                     eta = String.format(this.uploadPolicy
                             .getString("timefmt_hms"), new Long(
                             remaining / 3600), new Long((remaining / 60) % 60),
                             new Long(remaining % 60));
                 } else if (remaining > 60) {
                     eta = String.format(this.uploadPolicy
                             .getString("timefmt_ms"), new Long(remaining / 60),
                             new Long(remaining % 60));
                 } else
                     eta = String.format(this.uploadPolicy
                             .getString("timefmt_s"), new Long(remaining));
             } catch (ArithmeticException e1) {
                 eta = this.uploadPolicy.getString("timefmt_unknown");
             }
             String format = this.uploadPolicy.getString("status_msg");
             String status = String.format(format, new Integer((int) percent),
                     SizeRenderer.formatFileUploadSpeed(cps, this.uploadPolicy),
                     eta);
             this.uploadPanel.getStatusLabel().setText(status);
             this.uploadPolicy.getApplet().getAppletContext().showStatus(status);
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// SYNCHRONIZATION METHODS
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Check if a new packet is ready. A packet is ready if enough file are
      * prepared for upload (compared to the nbFilesPerRequest applet parameter)
      * or if the sum of bytes for the prepared files are more than the
      * maxChunkSize applet parameter (if it was given as an applet parameter).
      * The result is stored in the {@link #nextPacket} attribute. <BR>
      * Note: Take care that the result of this method (isNextPacketReady)
      * doesn't take into account the number of files that are actually being
      * uploaded.
      * 
      * @throws JUploadException
      */
     private synchronized boolean checkIfNextPacketIsReady()
             throws JUploadException {
 
         if (this.nextPacket != null) {
             // Nothing to do: the next packet is already prepared
         } else if (this.nbUploadedFiles + this.nbFilesBeingUploaded == this.nbPreparedFiles) {
             // No file is ready to sent: Upload is finished or all files are
             // been sent to the FileUploadThread.
         } else {
             // Some new files are ready, let's look if the packet is ready. We
             // add at least the first one.
             int maxPacketSize = Math.min(this.nbPreparedFiles
                     - this.nbUploadedFiles - this.nbFilesBeingUploaded,
                     this.nbFilesPerRequest);
             FileData[] tempFileData = new FileData[maxPacketSize];
             int nbFilesInPacket = 0;
             long packetLength = 0;
             boolean isPacketFinished = false;
 
             // We'll add the files, up to :
             // 1) The number of new files prepared and not uploaded (or being
             // uploaded by another upload thread),
             // 2) The number of files per request is no more than the
             // nbFilesPerRequest applet parameter.
             while (nbFilesInPacket < maxPacketSize) {
                 // If the packet is not empty, we don't allow to add a new file,
                 // if this new file make the total upload size be more than a
                 // chunk size, as chunk upload expects that files are sent file
                 // by file.
                 if (nbFilesInPacket > 0
                         && packetLength
                                 + this.uploadFileDataArray[nbFilesInPacket]
                                         .getUploadLength() > this.maxChunkSize) {
                     // The packet is full, now.
                     isPacketFinished = true;
                     break;
                 }
                 // Let's add this file.
                 tempFileData[nbFilesInPacket] = this.uploadFileDataArray[this.nbUploadedFiles
                         + this.nbFilesBeingUploaded + nbFilesInPacket];
                 packetLength += this.uploadFileDataArray[nbFilesInPacket]
                         .getUploadLength();
 
                 nbFilesInPacket += 1;
             }
 
             // We've extracted some files into the tempFileData array. The
             // question is: is this packet full ?
             if (this.nbUploadedFiles + this.nbFilesBeingUploaded
                     + nbFilesInPacket == this.uploadFileDataArray.length) {
                 // Ok, we've up to the last file.
                 isPacketFinished = true;
             } else if (nbFilesInPacket == this.nbFilesPerRequest) {
                 isPacketFinished = true;
             }
 
             if (isPacketFinished) {
                 // The packet is full. Let's copy the temp data to the next
                 // packet data.
                 this.nextPacket = new UploadFileData[nbFilesInPacket];
                 System.arraycopy(tempFileData, 0, this.nextPacket, 0,
                         nbFilesInPacket);
             }
         }
 
         return this.nextPacket != null;
     }
 
     /**
      * This method is called each time a new file is ready to upload. It
      * calculates if a new packet of files is ready to upload. It is private, as
      * it may be called only from this class.
      * 
      * @throws JUploadException
      */
     private synchronized void anotherFileIsReady(FileData newlyPreparedFileData)
             throws JUploadException {
         this.nbPreparedFiles += 1;
         this.nbBytesReadyForUpload += newlyPreparedFileData.getUploadLength();
         this.nbTotalNumberOfPreparedBytes += newlyPreparedFileData
                 .getUploadLength();
 
         // Let's estimate the average size;
         this.estimatedTotalLength = this.nbTotalNumberOfPreparedBytes
                 / this.nbPreparedFiles;
     }
 
     /**
      * This method is called each time a new file is successfully uploaded. It
      * calculates if a new packet of files is ready to upload. It is public, as
      * upload is done in another thread, whose class maybe in another package.
      * 
      * @param newlyUploadedFileData
      * @throws JUploadException
      */
     public synchronized void anotherFileHasBeenUploaded(
             FileData newlyUploadedFileData) throws JUploadException {
         this.nbUploadedFiles += 1;
         this.nbFilesBeingUploaded -= 1;
         this.nbBytesUploadedForCurrentFile = 0;
         this.nbBytesReadyForUpload -= newlyUploadedFileData.getUploadLength();
 
         // We are finished with this one. Let's display it.
         this.uploadStatus = UPLOAD_STATUS_UPLOADED;
         updateUploadProgressBar();
 
         // We should now remove this file from the list of files to upload, to
         // show the user that there is less and less work to do.
         this.filePanel.remove(newlyUploadedFileData);
     }
 
     /**
      * Returns the next packet of files, for upload, according to the current
      * upload policy.
      * 
      * @return The array of files to upload.
      * @throws JUploadException
      */
     public synchronized UploadFileData[] getNextPacket()
             throws JUploadException {
 
         // If no packet was ready before, perhaps one is ready now ?
         if (this.nextPacket == null) {
             checkIfNextPacketIsReady();
         }
 
         // If the next packet is ready, let's manage it.
         if (this.nextPacket == null) {
             return null;
         } else {
             // If it's the first packet, we noted the current time as the upload
             // start time.
             if (this.nbUploadedFiles == 0 && this.uploadStartTime == 0) {
                 this.uploadStartTime = System.currentTimeMillis();
             }
 
             UploadFileData[] fileDataTmp = this.nextPacket;
             this.nextPacket = null;
             this.nbFilesBeingUploaded += fileDataTmp.length;
 
             return fileDataTmp;
         }
     }
 
     /**
      * Update the progress bar, based on the following data: <DIR>
      * <LI>nbUploadedFiles: number of files that have already been updated.
      * <LI>nbBytesUploadedForCurrentFile: allows calculation of the upload
      * progress for the current file, based on it total upload length. </DIR>
      * 
      * @throws JUploadException
      */
     private synchronized void updateUploadProgressBar() throws JUploadException {
         final String msgInfoUploaded = this.uploadPolicy
                 .getString("infoUploaded");
         final String msgInfoUploading = this.uploadPolicy
                 .getString("infoUploading");
         final String msgNbUploadedFiles = this.uploadPolicy
                 .getString("nbUploadedFiles");
         int percent = 0;
 
         // First, we update the bar itself.
         if (this.nbBytesUploadedForCurrentFile == 0
                 || this.nbUploadedFiles == this.uploadFileDataArray.length) {
             percent = 0;
         } else {
             percent = (int) (this.nbBytesUploadedForCurrentFile * 100 / this.uploadFileDataArray[this.nbUploadedFiles]
                     .getUploadLength());
             // Usually, a percentage if advancement for one file is no more than
             // 100. Let's check that.
             if (percent > 100) {
                 this.uploadPolicy
                         .displayWarn("percent is more than 100 ("
                                 + percent
                                 + ") in FileUploadManagerThread.update.UploadProgressBar");
                 percent = 100;
             }
         }
 
         this.uploadPolicy.displayDebug("   ProgressBar value: "
                 + (100 * this.nbUploadedFiles + percent), 101);
         this.uploadProgressBar.setValue(100 * this.nbUploadedFiles + percent);
 
         String msg = null;
         switch (this.uploadStatus) {
             case UPLOAD_STATUS_NOT_STARTED:
                 msg = "";
                 break;
             case UPLOAD_STATUS_UPLOADING:
                 // Uploading files %1$s
                 msg = String.format(msgInfoUploading,
                         (this.nbUploadedFiles + 1));
                 break;
             case UPLOAD_STATUS_UPLOADED_WAITING_FOR_RESPONSE:
                 // %1$s file(s) uploaded. Waiting for server response ...
                 if (this.numOfFileInCurrentRequest == 1) {
                     msg = (this.nbUploadedFiles) + "/"
                             + (this.uploadFileDataArray.length);
                 } else {
                     msg = (this.nbUploadedFiles
                             - this.numOfFileInCurrentRequest + 1)
                             + "-"
                             + (this.nbUploadedFiles)
                             + "/"
                             + (this.uploadFileDataArray.length);
                 }
                 msg = String.format(msgInfoUploaded, msg);
                 break;
             case UPLOAD_STATUS_UPLOADED:
                 // %1$d file(s) uploaded
                 msg = String.format(msgNbUploadedFiles, (this.nbUploadedFiles));
                 break;
             default:
                 // Hum, that's strange !
                 this.uploadPolicy
                         .displayWarn("Unknown upload status in FileUploadManagerThread.updateProgressBar(): "
                                 + this.uploadStatus);
         }
 
         // Let's show the modifications to the user
         this.uploadProgressBar.setString(msg);
         this.uploadProgressBar.repaint(0);
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// PRIVATE METHODS
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This method is called by the {@link #run()} method, to prepare all files.
      * It's executed in the current thread, while upload is executed in the
      * another thread, by {@link FileUploadThread}.
      */
     private void prepareFiles() {
         this.preparationProgressBar
                 .setMaximum(100 * this.uploadFileDataArray.length);
         try {
             for (int i = 0; i < this.uploadFileDataArray.length; i += 1) {
                 this.uploadPolicy.displayDebug(
                         "============== Start of file preparation ("
                                 + this.uploadFileDataArray[i].getFileName()
                                 + ")", 30);
 
                 // Let's indicate to the user what's running on.
                 this.preparationProgressBar.setString(String.format(
                         this.uploadPolicy.getString("preparingFile"),
                         new Integer(i + 1), new Integer(
                                 this.uploadFileDataArray.length)));
                 this.preparationProgressBar.repaint(100);
 
                 // Then, we work
                 this.uploadFileDataArray[i].beforeUpload();
                 this.uploadPolicy.displayDebug(
                         "============== End of file preparation ("
                                 + this.uploadFileDataArray[i].getFileName()
                                 + ")", 30);
                 anotherFileIsReady(this.uploadFileDataArray[i]);
 
                 // The file preparation is finished. Let's update the progress
                 // bar.
                 this.preparationProgressBar
                         .setValue(this.nbPreparedFiles * 100);
                 this.preparationProgressBar.repaint();
             }
 
         } catch (JUploadException e) {
             this.uploadException = e;
             this.uploadPolicy.displayErr(e);
             this.preparationProgressBar.setString(e.getMessage());
             stopUpload();
         }
     }
 
     /**
      * Reaction on a successful upload.
      */
     private void afterUploadOk() {
         this.uploadPolicy.displayDebug(
                 "FileUploadManagerThread: in afterUploadOk()", 10);
         String svrRet = this.fileUploadThread.getResponseMsg();
 
         try {
             this.uploadPolicy.afterUpload(this.getUploadException(), svrRet);
         } catch (JUploadException e1) {
             this.uploadPolicy.displayErr(
                     "error in uploadPolicy.afterUpload (JUploadPanel)", e1);
         }
     }
 
     /**
      * Creates and starts the upload thread. It will wait until the first packet
      * is ready.
      */
     private void createUploadThread() {
         try {
             if (this.uploadPolicy.getPostURL().substring(0, 4).equals("ftp:")) {
                 this.fileUploadThread = new FileUploadThreadFTP(
                         this.uploadPolicy, this);
             } else {
                 this.fileUploadThread = new FileUploadThreadHTTP(
                         this.uploadPolicy, this);
             }
         } catch (JUploadException e1) {
             // Too bad !
             this.uploadPolicy.displayErr(e1);
         }
         this.fileUploadThread.start();
     }
 
     /**
      * Initialize the maximum value for the two progress bar: 100*the number of
      * files to upload.
      * 
      * @throws JUploadException
      * 
      * @see #updateUploadProgressBar()
      */
     private synchronized void initProgressBar() throws JUploadException {
         // To follow the state of file preparation
         this.preparationProgressBar
                 .setMaximum(100 * this.uploadFileDataArray.length);
         this.preparationProgressBar.setString("");
 
         // To follow the state of the actual upload
         this.uploadProgressBar
                 .setMaximum(100 * this.uploadFileDataArray.length);
         this.uploadProgressBar.setString("");
 
         this.updateUploadProgressBar();
     }
 
 }
