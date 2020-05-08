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
  * state. Its job is to: <DIR> <LI>Prepare upload for the file (calls to
  * {@link FileData#beforeUpload()} for each file in the file list. <LI>Create
  * the thread to send a packet of files. <LI>Prepare the packets, that will be
  * red by the upload thread. <LI>Manage the end of upload: trigger the call to
  * {@link JUploadPanel#updateButtonState()} and the call to
  * {@link UploadPolicy#afterUpload(Exception, String)}. <LI>Manage the 'stop'
  * button reaction. </DIR> This class is created by {@link JUploadPanel}, when
  * the user clicks on the upload button.
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
      * A chunk (a part) of the file identified by numOfFileInCurrentRequest has
      * been sent. But the server response has not been received yet.
      */
     public static final int UPLOAD_STATUS_CHUNK_UPLOADED_WAITING_FOR_RESPONSE = 3;
 
     /**
      * All data for the file identified by numOfFileInCurrentRequest has been
      * sent. But the server response has not been received yet.
      */
     public static final int UPLOAD_STATUS_FILE_UPLOADED_WAITING_FOR_RESPONSE = 4;
 
     /**
      * The upload for the file identified by numOfFileInCurrentRequest is
      * finished
      */
     public static final int UPLOAD_STATUS_UPLOADED = 5;
 
     // /////////////////////////////////////////////////////////////////////////////////////////
     // //////////////////// Possible Status for file upload
     // /////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Contains the date/time (as a long) of the start of the current upload.
      * This allows to sum the time of the actual upload, and ignore the time the
      * applet is waiting for the server's response. Once the request is
      * finished, and the applet waits for the server's response, the duration of
      * the sending to the server is added to currentRequestStartTime, and
      * currentRequestStartTime is reseted to 0. It's then ready for the next
      * upload request.
      */
     private long currentRequestStartTime = 0;
 
     /** The current file list. */
     private FilePanel filePanel = null;
 
     /**
      * The upload thread, that will wait for the next file packet to be ready,
      * then send it.
      */
     private FileUploadThread fileUploadThread = null;
 
     /**
      * Contains the system time of the start of the global upload. This is used
      * to calculate the ETA, and display it to the user, on the status bar.
      */
     private long globalStartTime = 0;
 
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
      * Number of files that have already been sent. The control on the upload
      * success may be done or not. It's used to properly display the progress
      * bar.
      */
     private int nbSentFiles = 0;
 
     /**
      * Number of files that have been successfully uploaded. already been sent.
      * The control on the upload success may be done or not. It's used to
      * properly display the progress bar.
      */
     private int nbSuccessfullyUploadedFiles = 0;
 
     /**
      * Indicated the number of bytes that have currently been sent for the
      * current file. This allows the management of the progress bar.
      */
     private long nbBytesUploadedForCurrentFile = 0;
 
     /**
      * Sum of the length for all prepared files. This allow the calculation of
      * the estimatedTotalLength.
      * 
      * @see #anotherFileHasBeenSent(FileData)
      */
     private long nbTotalNumberOfPreparedBytes = 0;
 
     /**
      * During the upload, when uploading several files in one packet, this
      * attribute indicates which file is currently being uploaded.
      */
     private int numOfFileInCurrentRequest = 0;
 
     /**
      * Indicates what is the current file being uploaded, and its upload status.
      */
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
      * Indicates whether the upload is finished or not. Passed to true as soon
      * as one of these conditions becomes true: <DIR> <LI>All files are uploaded
      * (in the {@link #currentRequestIsFinished(UploadFileData[])} method) <LI>
      * An exception occurs (in the {@link #setUploadException(JUploadException)}
      * method) <LI>The user stops the upload (in the {@link #stopUpload()}
      * method) </DIR>
      */
     private boolean uploadFinished = false;
 
     /**
      * Contains the time of the actual start of upload. Doesn't take into
      * account the time for preparing files.
      */
     private long uploadStartTime = 0;
 
     /**
      * If set to 'true', the thread will stop the current upload. This attribute
      * is not private as the {@link UploadFileData} class us it.
      * 
      * @see UploadFileData#uploadFile(java.io.OutputStream, long)
      */
     private boolean stop = false;
 
     /** Thread Exception, if any occurred during upload. */
     private JUploadException uploadException = null;
 
     /**
      * Contains the sum of the upload duration for all requests. For instance,
      * if sending in 10 chunks one big file, the uploadDuration contains the sum
      * of the sending of these 10 request to the server. This allows to
      * calculate the true upload speed, and ignore the time we'll wait for the
      * server's response.
      */
     private long uploadDuration = 0;
 
     /** Current number of bytes that have been uploaded. */
     private long nbUploadedBytes = 0;
 
     /** A shortcut to the upload panel */
     private JUploadPanel uploadPanel = null;
 
     /** The current upload policy. */
     private UploadPolicy uploadPolicy = null;
 
     /** The list of files to upload */
     private UploadFileData[] uploadFileDataArray = null;
 
     // ////////////////////////////////////////////////////////////////////////////
     // To follow the upload speed.
     // ////////////////////////////////////////////////////////////////////////////
 
     /**
      * Used to update the status bar (upload speed, ETA...): 300ms make it
      * accurate, and avoid an always changing value.
      */
     private Timer timerStatusBar = new Timer(1000, this);
 
     /**
      * Used to update the progress: 50ms is nice, as it is fast enough, and
      * doesn't make CPU rise to 100%.
      */
     private Timer timerProgressBar = new Timer(50, this);
 
     /**
      * Standard constructor of the class.
      * 
      * @param uploadPolicy
      */
     public FileUploadManagerThread(UploadPolicy uploadPolicy) {
         super("FileUploadManagerThread thread");
 
         // General shortcuts on the current applet.
         this.uploadPolicy = uploadPolicy;
         this.uploadPanel = uploadPolicy.getContext().getUploadPanel();
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
 
             // Let's let the current upload policy have any preparation work
             this.uploadPolicy.beforeUpload();
 
             // The upload is started. Let's change the button state.
             this.uploadPanel.updateButtonState();
 
             // Let's prepare the progress bar, to display the current upload
             // stage.
             initProgressBar();
 
             // Create a timer, to update the status bar.
             this.timerProgressBar.start();
             this.timerStatusBar.start();
             this.uploadPolicy.displayDebug("Timer started", 50);
 
             // We have to prepare the files, then to create the upload thread
             // for
             // each file packet.
             prepareFiles();
 
             // The thread upload may need some information about the current
             // one, like ... knowing that upload is actually finished (no more
             // file to send).
             while (this.fileUploadThread != null
                     && this.fileUploadThread.isAlive()
                     && this.nbSuccessfullyUploadedFiles < this.uploadFileDataArray.length
                     && !isUploadStopped() // Stopped by the user
                     && this.getUploadException() == null // An error occurs
             ) {
                 try {
                     this.uploadPolicy.displayDebug(
                             "Waiting for fileUploadThread to die", 10);
                     this.fileUploadThread.join();
                 } catch (InterruptedException e) {
                     // This should not occur, and should not be a problem. Let's
                     // trace a warning info.
                     this.uploadPolicy
                             .displayWarn("An InterruptedException occured in FileUploadManagerThread.run()");
                 }
             }
 
             // Let's restore the button state.
             this.uploadPanel.updateButtonState();
             this.uploadPolicy.getContext().showStatus("");
             this.uploadPolicy.getContext().getUploadPanel().getStatusLabel()
                     .setText("");
 
             // If no error occurs, we tell to the upload policy that a
             // successful
             // upload has been done.
 
             if (getUploadException() != null) {
                 this.uploadPolicy.sendDebugInformation("Error in Upload",
                         getUploadException());
             } else if (isUploadStopped()) {
                 this.uploadPolicy
                        .displayInfo("Upload stopped by the user. "
                                + this.nbSuccessfullyUploadedFiles
                                + " file(s) uploaded in "
                                + (int) ((System.currentTimeMillis() - this.globalStartTime) / 1000)
                                + " seconds. Average upload speed: "
                                + ((this.uploadDuration > 0) ? ((int) (this.nbUploadedBytes / this.uploadDuration))
                                        : 0) + " (kbytes/s)");
             } else {
                 this.uploadPolicy
                         .displayInfo("Upload finished normally. "
                                 + this.uploadFileDataArray.length
                                 + " file(s) uploaded in "
                                 + (int) ((System.currentTimeMillis() - this.globalStartTime) / 1000)
                                 + " seconds. Average upload speed: "
                                 + ((this.uploadDuration > 0) ? ((int) (this.nbUploadedBytes / this.uploadDuration))
                                         : 0) + " (kbytes/s)");
                 // FIXME uploadDuration displayed is 0!
                 try {
                     this.uploadPolicy.afterUpload(this.getUploadException(),
                             this.fileUploadThread.getResponseMsg());
                 } catch (JUploadException e1) {
                     this.uploadPolicy.displayErr(
                             "error in uploadPolicy.afterUpload (JUploadPanel)",
                             e1);
                 }
             }
 
             // If the upload was successful, we wait for 5 seconds, before
             // clearing the progress bar.
             if (!isUploadStopped() && getUploadException() != null) {
                 try {
                     sleep(5000);
                 } catch (InterruptedException e) {
                     // Nothing to do
                 }
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
             setUploadException(jue);
             this.uploadPolicy.displayErr(
                     "Uncaught exception in FileUploadManagerThread/run()", jue);
 
             // And go back into a 'normal' way.
             stopUpload();
         } finally {
             // We restore the button state, just to be sure.
             this.uploadPanel.updateButtonState();
         }
 
         this.timerProgressBar.stop();
         this.timerStatusBar.stop();
 
         // And we die of our beautiful death ... until next upload.
     }// run
 
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
         return this.nbSentFiles;
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
      * Stores the last upload exception that occurs. This method won't write to
      * the log file.
      * 
      * @param uploadException
      */
    public void setUploadException(JUploadException uploadException) {
         // We don't override an existing exception
         if (this.uploadException != null) {
             this.uploadPolicy
                     .displayWarn("An exception has already been set in FileUploadManagerThread. The next one is just logged.");
         } else {
             this.uploadException = uploadException;
         }
 
         this.uploadPolicy.displayErr(uploadException);
         this.preparationProgressBar.setString(uploadException.getMessage());
         // We stop the upload as soon as an error occurs.
         this.uploadFinished = true;
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
         // Indicate whether or not the upload is finished. Several conditions.
         return this.uploadFinished;
     }
 
     /**
      * Indicates if the upload has been stopped by the user, or by any upload
      * error.
      * 
      * @return true if the current upload has been asked to stop by the user,
      *         false otherwise.
      */
     private boolean isUploadStopped() {
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
         this.nbUploadedBytes += nbBytes;
         this.nbBytesUploadedForCurrentFile += nbBytes;
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
         if (globalStartTime == 0) {
             // Ok, the upload just starts. We keep the date, to later calculate
             // the ETA.
             globalStartTime = System.currentTimeMillis();
         }
         switch (uploadStatus) {
             case UPLOAD_STATUS_CHUNK_UPLOADED_WAITING_FOR_RESPONSE:
             case UPLOAD_STATUS_FILE_UPLOADED_WAITING_FOR_RESPONSE:
                 // We're waiting for the server: let's add it to the sending
                 // duration.
                 uploadDuration += System.currentTimeMillis()
                         - currentRequestStartTime;
                 currentRequestStartTime = 0;
                 break;
             case UPLOAD_STATUS_UPLOADING:
                 if (currentRequestStartTime == 0) {
                     currentRequestStartTime = System.currentTimeMillis();
                 }
                 break;
             case UPLOAD_STATUS_UPLOADED:
                 // Nothing to do
                 break;
             default:
                 this.uploadPolicy
                         .displayWarn("Unknown value for uploadStatus: "
                                 + uploadStatus);
         }
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
     public synchronized void stopUpload() {
         this.stop = true;
 
         // The upload is now finished ...
         this.uploadFinished = true;
 
         // We notify the upload thread.
         if (fileUploadThread != null) {
             fileUploadThread.interrupt();
         }
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
                 this.timerProgressBar.stop();
             }
             if (e.getSource() == timerProgressBar) {
                 updateUploadProgressBar();
             }
             if (e.getSource() == timerStatusBar) {
                 updateUploadStatusBar();
             }
         }
     }
 
     /**
      * Displays the current upload speed on the status bar.
      */
     private void updateUploadStatusBar() {
         // We'll update the status bar, only if it exists and if the upload
         // actually started.
         if (null != this.uploadPanel.getStatusLabel()
                 && getUploadStartTime() != 0 && this.nbUploadedBytes > 0) {
             // actualUploadDuration: contains the sum of the time, when the
             // applet is actually sending data to the server, and ignores the
             // time when it waits for the server's response.
             // This is used to calculate the upload speed.
             long actualUploadDuration;
             double totalFileBytesToSend;
             double percent;
             // uploadCPS: contains the upload speed.
             double uploadSpeed;
             // globalCPS: contains the average speed, including the time the
             // applet is waiting for the server response.
             double globalCPS;
             long remaining;
             String eta;
 
             // Let's calculate the actual upload duration. That is: the time
             // during which we're really sending data to the server.
             if (currentRequestStartTime == 0) {
                 // We're currently sending nothing to the server.
                 actualUploadDuration = uploadDuration;
             } else {
                 // We're currently sending data to the server. We add the time
                 // of the current request to the stored upload duration.
                 actualUploadDuration = uploadDuration
                         + System.currentTimeMillis() - currentRequestStartTime;
             }
             // For next steps, we expect a duration in seconds:
             actualUploadDuration /= 1000;
 
             // Let's estimate the total, or calculate it, of all files are
             // prepared
             if (this.nbPreparedFiles == this.uploadFileDataArray.length) {
                 // All files are prepared: it's no more an estimation !
                 totalFileBytesToSend = this.nbTotalNumberOfPreparedBytes;
             } else {
                 // We sum the total number of prepared bytes, and we estimate
                 // the size of the files that are not prepared yet
                 totalFileBytesToSend = this.nbTotalNumberOfPreparedBytes
                         +
                         // And we sum it with the average amount per file
                         // prepared for the others
                         (this.uploadFileDataArray.length - this.nbPreparedFiles)
                         * this.nbTotalNumberOfPreparedBytes
                         / this.nbPreparedFiles;
             }
             try {
                 percent = 100.0 * this.nbUploadedBytes / totalFileBytesToSend;
             } catch (ArithmeticException e1) {
                 percent = 100;
             }
 
             // Calculation of the 'pure' upload speed.
             try {
                 uploadSpeed = this.nbUploadedBytes / actualUploadDuration;
             } catch (ArithmeticException e1) {
                 uploadSpeed = this.nbUploadedBytes;
             }
 
             // Calculation of the 'global' upload speed.
             try {
                 globalCPS = this.nbUploadedBytes
                         / (System.currentTimeMillis() - this.globalStartTime)
                         * 1000;
             } catch (ArithmeticException e1) {
                 globalCPS = this.nbUploadedBytes;
             }
 
             // Calculation of the ETA. It's based on the global upload speed.
             try {
                 remaining = (long) ((totalFileBytesToSend - this.nbUploadedBytes) / globalCPS);
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
                     SizeRenderer.formatFileUploadSpeed(uploadSpeed,
                             this.uploadPolicy), eta);
             this.uploadPanel.getStatusLabel().setText(status);
             // this.uploadPanel.getStatusLabel().repaint();
             this.uploadPolicy.getContext().showStatus(status);
             // this.uploadPolicy.displayDebug("[updateUploadStatusBar] " +
             // status, 101);
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
         } else if (this.nbSentFiles + this.nbFilesBeingUploaded == this.nbPreparedFiles) {
             // No file is ready to sent: Upload is finished or all files are
             // been sent to the FileUploadThread.
         } else {
             // Some new files are ready, let's look if the packet is ready. We
             // add at least the first one.
             int maxPacketSize = Math.min(this.nbPreparedFiles
                     - this.nbSentFiles - this.nbFilesBeingUploaded,
                     this.nbFilesPerRequest);
             FileData[] tempFileData = new FileData[maxPacketSize];
             int nbFilesInPacket = 0;
             long packetLength = 0;
             boolean isPacketFinished = false;
             FileData currentFileData;
 
             // We'll add the files, up to :
             // 1) The number of new files prepared and not uploaded (or being
             // uploaded by another upload thread),
             // 2) The number of files per request is no more than the
             // nbFilesPerRequest applet parameter.
             // 3) The total length of files in the packet may be more than the
             // maxChunkSize applet parameter.
             while (!isPacketFinished && nbFilesInPacket < maxPacketSize
                     && packetLength < this.maxChunkSize) {
                 // We're working on this file:
                 currentFileData = this.uploadFileDataArray[this.nbSentFiles
                         + this.nbFilesBeingUploaded + nbFilesInPacket];
 
                 if (nbFilesInPacket > 0
                         && packetLength + currentFileData.getUploadLength() > this.maxChunkSize) {
                     // We can't add this file: the file size would be bigger
                     // than maxChunkSize. So this packet is ready.
                     isPacketFinished = true;
                 } else {
                     // Let's add this file.
                     tempFileData[nbFilesInPacket] = currentFileData;
                     packetLength += currentFileData.getUploadLength();
 
                     nbFilesInPacket += 1;
                 }
             }
 
             // We've extracted some files into the tempFileData array. The
             // question is: is this packet full ?
             if (!isPacketFinished) {
                 if (packetLength > this.maxChunkSize) {
                     // The packet can't contain more bytes!
                     if (nbFilesInPacket > 1) {
                         throw new JUploadException(
                                 "totalContentLength >= chunkSize: this.filesToUpload.length should not be more than 1 (checkIfNextPacketIsReady)");
                     }
                     isPacketFinished = true;
                 } else if (nbFilesInPacket == this.nbFilesPerRequest) {
                     // The packet can't contain more files!
                     isPacketFinished = true;
                 } else if (this.nbSentFiles + this.nbFilesBeingUploaded
                         + nbFilesInPacket == this.uploadFileDataArray.length) {
                     // We're up to the last file.
                     isPacketFinished = true;
                 }
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
     }
 
     /**
      * This method is called each time a new file is sent to the server. It's
      * main aim is to allow a proper display of the progress bar. It is public,
      * as upload is done in another thread, whose class maybe in another
      * package.
      * 
      * @param newlyUploadedFileData
      * @throws JUploadException
      */
     public synchronized void anotherFileHasBeenSent(
             FileData newlyUploadedFileData) throws JUploadException {
         this.nbSentFiles += 1;
         this.nbFilesBeingUploaded -= 1;
         this.nbBytesUploadedForCurrentFile = 0;
         this.nbBytesReadyForUpload -= newlyUploadedFileData.getUploadLength();
 
         // We are finished with this one. Let's display it.
         this.uploadStatus = UPLOAD_STATUS_UPLOADED;
         updateUploadProgressBar();
     }
 
     /**
      * This method is called when the server response for the upload indicates a
      * success. It is public, as upload is done in another thread, whose class
      * maybe in another package.
      * 
      * @param currentPacket The packet of files that was successfully uploaded.
      * @throws JUploadException
      */
     public synchronized void currentRequestIsFinished(
             UploadFileData[] currentPacket) throws JUploadException {
         // If no error occurs, we're happy ! (that's a useful comment...)
         if (this.getUploadException() == null) {
             // We should now remove this file from the list of files to upload,
             // to show the user that there is less and less work to do.
             for (int i = 0; i < currentPacket.length; i += 1) {
                 this.filePanel.remove(currentPacket[i]);
                 this.nbSuccessfullyUploadedFiles += 1;
             }
 
             // If all files have been sent, the upload is finished.
             if (!this.uploadFinished) {
                 this.uploadFinished = (this.nbSuccessfullyUploadedFiles == this.uploadFileDataArray.length);
             }
 
         } else {
             // Hum, we're not happy! We stop here.
             this.uploadFinished = true;
             // Nothing else to do: the error is already displayed to the user.
         }
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
 
         // If the upload is finished, we stop here.
         if (isUploadFinished()) {
             return null;
         }
 
         // If no packet was ready before, perhaps one is ready now ?
         if (this.nextPacket == null) {
             checkIfNextPacketIsReady();
         }
 
         // If the next packet is ready, let's manage it.
         if (this.nextPacket == null || isUploadFinished()) {
             return null;
         } else {
             // If it's the first packet, we noted the current time as the upload
             // start time.
             if (this.nbSentFiles == 0 && this.uploadStartTime == 0) {
                 this.uploadStartTime = System.currentTimeMillis();
             }
 
             UploadFileData[] fileDataTmp = this.nextPacket;
             this.nextPacket = null;
             this.nbFilesBeingUploaded += fileDataTmp.length;
 
             return fileDataTmp;
         }
     }
 
     /**
      * Update the progress bar, based on the following data: <DIR> <LI>
      * nbSentFiles: number of files that have already been updated. <LI>
      * nbBytesUploadedForCurrentFile: allows calculation of the upload progress
      * for the current file, based on it total upload length. </DIR> <BR>
      * Note: The progress bar update is ignored, if last update was less than
      * 100ms before.
      * 
      * @throws JUploadException
      */
     private synchronized void updateUploadProgressBar() {
         final String msgInfoUploaded = this.uploadPolicy
                 .getString("infoUploaded");
         final String msgInfoUploading = this.uploadPolicy
                 .getString("infoUploading");
         final String msgNbUploadedFiles = this.uploadPolicy
                 .getString("nbUploadedFiles");
         int percent = 0;
 
         // First, we update the bar itself.
         if (this.nbBytesUploadedForCurrentFile == 0
                 || this.nbSentFiles == this.uploadFileDataArray.length) {
             percent = 0;
         } else {
             try {
                 percent = (int) (this.nbBytesUploadedForCurrentFile * 100 / this.uploadFileDataArray[this.nbSentFiles]
                         .getUploadLength());
             } catch (JUploadException e) {
                 this.uploadPolicy.displayWarn(e.getClass().getName()
                         + " in updateUploadProgressBar (" + e.getMessage()
                         + "). percent forced to 0.");
                 percent = 0;
             }
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
 
         this.uploadProgressBar.setValue(100 * this.nbSentFiles + percent);
 
         String msg = null;
         switch (this.uploadStatus) {
             case UPLOAD_STATUS_NOT_STARTED:
                 msg = "";
                 break;
             case UPLOAD_STATUS_UPLOADING:
             case UPLOAD_STATUS_CHUNK_UPLOADED_WAITING_FOR_RESPONSE:
                 // Uploading files %1$s
                 msg = String.format(msgInfoUploading, (this.nbSentFiles + 1));
                 break;
             case UPLOAD_STATUS_FILE_UPLOADED_WAITING_FOR_RESPONSE:
                 // %1$s file(s) uploaded. Waiting for server response ...
                 if (this.numOfFileInCurrentRequest == 1) {
                     msg = (this.nbSentFiles) + "/"
                             + (this.uploadFileDataArray.length);
                 } else {
                     msg = (this.nbSentFiles - this.numOfFileInCurrentRequest + 1)
                             + "-"
                             + (this.nbSentFiles)
                             + "/"
                             + (this.uploadFileDataArray.length);
                 }
                 msg = String.format(msgInfoUploaded, msg);
                 break;
             case UPLOAD_STATUS_UPLOADED:
                 // %1$d file(s) uploaded
                 msg = String.format(msgNbUploadedFiles, (this.nbSentFiles));
                 break;
             default:
                 // Hum, that's strange !
                 this.uploadPolicy
                         .displayWarn("Unknown upload status in FileUploadManagerThread.updateProgressBar(): "
                                 + this.uploadStatus);
         }
 
         // Let's show the modifications to the user
         this.uploadProgressBar.setString(msg);
         this.uploadProgressBar.repaint();
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
             // We loop through all files, and check before each if we should
             // stop (for instance if an error occurs)
             for (int i = 0; i < this.uploadFileDataArray.length
                     && !isUploadFinished(); i += 1) {
                 this.uploadPolicy.displayDebug(
                         "============== Start of file preparation ("
                                 + this.uploadFileDataArray[i].getFileName()
                                 + ")", 30);
 
                 // Let's indicate to the user what's running on.
                 this.preparationProgressBar.setString(String.format(
                         this.uploadPolicy.getString("preparingFile"),
                         new Integer(i + 1), new Integer(
                                 this.uploadFileDataArray.length)));
                 this.preparationProgressBar.repaint();
 
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
             setUploadException(e);
             stopUpload();
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
