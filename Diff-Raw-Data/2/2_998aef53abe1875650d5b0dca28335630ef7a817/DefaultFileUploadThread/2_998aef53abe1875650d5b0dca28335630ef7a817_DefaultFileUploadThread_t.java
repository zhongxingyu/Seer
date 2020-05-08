 //
 // $Id: DefaultFileUploadThread.java 287 2007-06-17 09:07:04 +0000 (dim., 17
 // juin 2007) felfert $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: ?
 // Creator: William JinHua Kwong
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.upload;
 
 import java.io.OutputStream;
 import java.util.regex.Pattern;
 
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class is based on the {@link FileUploadThread} class. It's an abstract
  * class that contains the default implementation for the
  * {@link FileUploadThread} interface. <BR>
  * It contains the following abstract methods, which must be implemented in the
  * children classes. These methods are called in this order: <DIR>
  * <LI>For each upload request (for instance, upload of 3 files with
  * nbFilesPerRequest to 2, makes 2 request: 2 files, then the last one): <DIR>
  * <LI><I>try</I>
  * <LI>{@link #startRequest}: start of the UploadRequest.
  * <LI>Then, for each file to upload (according to the nbFilesPerRequest and
  * maxChunkSize applet parameters) <DIR>
  * <LI>beforeFile(int) is called before writting the bytes for this file (or
  * this chunk)
  * <LI>afterFile(int) is called after writting the bytes for this file (or this
  * chunk) </DIR>
  * <LI>finishRequest() </DIR> </LI>
  * <I>finally</I>cleanRequest()
  * <LI>Call of cleanAll(), to clean up any used resources, common to the whole
  * upload. </DIR>
  */
 public abstract class DefaultFileUploadThread extends Thread implements
         FileUploadThread {
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// VARIABLES ///////////////////////////////////////
     // ////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * The array that contains the current packet to upload.
      * 
      * @see FileUploadManagerThread#getNextPacket()
      */
     UploadFileData[] filesToUpload = null;
 
     /**
      * The upload manager. The thread that prepares files, and is responsible to
      * manage the upload process.
      * 
      * @see FileUploadManagerThread
      */
     FileUploadManagerThread fileUploadManagerThread = null;
 
     /**
      * The upload policy contains all parameters needed to define the way files
      * should be uploaded, including the URL.
      */
     protected UploadPolicy uploadPolicy = null;
 
     /**
      * The value of the applet parameter maxChunkSize, or its default value.
      */
     private long maxChunkSize;
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// PRIVATE ATTRIBUTES
     // ///////////////////////////////////////
     // ////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * The full response message from the server, if any. For instance, in HTTP
      * mode, this contains both the headers and the body.
      */
     protected String responseMsg = "";
 
     /**
      * The response message from the application. For instance, in HTTP mode,
      * this contains the body response.<BR>
      * Note: for easier management on the various server configurations, all end
      * or line characters (CR, LF or CRLF) are changed to uniform CRLF.
      */
     protected String responseBody = "";
 
     /**
      * Creates a new instance.
      * 
      * @param uploadPolicy The upload policy to be applied.
      * @param fileUploadManagerThread The thread that is managing the upload.
      */
     public DefaultFileUploadThread(UploadPolicy uploadPolicy,
             FileUploadManagerThread fileUploadManagerThread) {
         // Thread parameters.
         super("FileUploadThread");
 
         // Specific stuff.
         this.uploadPolicy = uploadPolicy;
         this.fileUploadManagerThread = fileUploadManagerThread;
         // Let's read up to date upload parameters.
         this.maxChunkSize = this.uploadPolicy.getMaxChunkSize();
 
         this.uploadPolicy.displayDebug("DefaultFileUploadThread created", 30);
     }
 
     /**
      * This method is called before the upload. It calls the
      * {@link FileData#beforeUpload()} method for all files to upload, and
      * prepares the progressBar bar (if any), with total number of bytes to
      * upload.
      * 
      * final private void beforeUpload() throws JUploadException { for (int i =
      * 0; i < this.filesToUpload.length &&
      * !this.fileUploadManager.isUploadStopped(); i++) {
      * this.filesToUpload[i].beforeUpload(); } }
      * 
      * /** This methods upload overhead for the file number indexFile in the
      * filesDataParam given to the constructor. For instance, in HTTP, the
      * upload contains a head and a tail for each files.
      * 
      * @param indexFile The index of the file in the filesDataParam array, whose
      *            addtional length is asked.
      * @return The additional number of bytes for this file.
      */
     abstract long getAdditionnalBytesForUpload(int indexFile)
             throws JUploadIOException;
 
     /**
      * This method is called before starting of each request. It can be used to
      * prepare any work, before starting the request. For instance, in HTTP, the
      * tail must be properly calculated, as the last one must be different from
      * the others.<BR>
      * The files to prepare are stored in the {@link #filesToUpload} array.
      */
     abstract void beforeRequest() throws JUploadException;
 
     /**
      * This method is called for each upload request to the server. The number
      * of request to the server depends on: <DIR>
      * <LI>The total number of files to upload.
      * <LI>The value of the nbFilesPerRequest applet parameter.
      * <LI>The value of the maxChunkSize applet parameter. </DIR> The main
      * objective of this method is to open the connection to the server, where
      * the files to upload will be written. It should also send any header
      * necessary for this upload request. The {@link #getOutputStream()} methods
      * is then called to know where the uploaded files should be written. <BR>
      * Note: it's up to the class containing this method to internally manage
      * the connection.
      * 
      * @param contentLength The total number of bytes for the files (or the
      *            chunk) to upload in this query.
      * @param bChunkEnabled True if this upload is part of a file (can occurs
      *            only if the maxChunkSize applet parameter is set). False
      *            otherwise.
      * @param chunkPart The chunk number. Should be ignored if bChunkEnabled is
      *            false.
      * @param bLastChunk True if in chunk mode, and this upload is the last one.
      *            Should be ignored if bChunkEnabled is false.
      */
     abstract void startRequest(long contentLength, boolean bChunkEnabled,
             int chunkPart, boolean bLastChunk) throws JUploadException;
 
     /**
      * This method is called at the end of each request.
      * 
      * @return The response status code from the server (200 == OK)
      * @see #startRequest(long, boolean, int, boolean)
      */
     abstract int finishRequest() throws JUploadException;
 
     /**
      * This method is called before sending the bytes corresponding to the file
      * whose index is given in argument. If the file is splitted in chunks (see
      * the maxChunkSize applet parameter), this method is called before each
      * chunk for this file.
      * 
      * @param index The index of the file that will be sent just after
      */
     abstract void beforeFile(int index) throws JUploadException;
 
     /**
      * Idem as {@link #beforeFile(int)}, but is called after each file (and
      * each chunks for each file).
      * 
      * @param index The index of the file that was just sent.
      */
     abstract void afterFile(int index) throws JUploadException;
 
     /**
      * Clean any used resource of the last executed request. In HTTP mode, the
      * output stream, input stream and the socket should be cleaned here.
      */
     abstract void cleanRequest() throws JUploadException;
 
     /**
      * Clean any used resource, like a 'permanent' connection. This method is
      * called after the end of the last request (see on the top of this page for
      * details).
      */
     abstract void cleanAll() throws JUploadException;
 
     /**
      * Get the output stream where the files should be written for upload.
      * 
      * @return The target output stream for upload.
      */
     abstract OutputStream getOutputStream() throws JUploadException;
 
     /**
      * Return the the body for the server response. That is: the server response
      * without the http header. This is the functional response from the server
      * application, that has been as the HTTP reply body, for instance: all
      * 'echo' PHP commands. <BR>
      * 
      * @return The last application response (HTTP body, in HTTP upload)
      */
     public String getResponseBody() {
         return this.responseBody;
     }
 
     /**
      * Get the server Output.
      * 
      * @return The status message from the first line of the response (e.g. "200
      *         OK").
      */
     public String getResponseMsg() {
         return this.responseMsg;
     }
 
     /**
      * Unused Store the String that contains the server response body.
      * 
      * @param body The response body that has been read.
      */
     void setResponseBody(String body) {
         this.responseBody = normalizeCRLF(body);
     }
 
     /**
      * Add a String that has been read from the server response.
      * 
      * @param msg The status message from the first line of the response (e.g.
      *            "200 OK").
      */
     void setResponseMsg(String msg) {
         this.responseMsg = normalizeCRLF(msg);
     }
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// PRIVATE FUNCTIONS
     // ////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This method loops on the {@link FileUploadManagerThread#getNextPacket()}
      * method, until a set of files is ready. Then, it calls the doUpload()
      * method, to send these files to the server.
      */
     @Override
     final public void run() {
         this.uploadPolicy.displayDebug("Start of the FileUploadThread", 5);
 
         try {
             // We'll stop the upload if an error occurs. So the try/catch is
             // outside the while.
             // FIXME isUploadFinished will be true, when this thread stops!
             while (!this.fileUploadManagerThread.isUploadStopped()
                     && !this.fileUploadManagerThread.isUploadFinished()) {
                 // If a packet is ready, we take it into account. Otherwise, we
                 // wait for a new packet.
                 this.filesToUpload = this.fileUploadManagerThread
                         .getNextPacket();
                 if (this.filesToUpload != null) {
                     this.uploadPolicy.displayDebug("Before do upload", 5);
 
                     // Let's go to work.
                     doUpload();
 
                     this.uploadPolicy.displayDebug("After do upload", 5);
                 } else {
                     try {
                         // We wait a little. If a file is prepared in the
                         // meantime, this thread is notified. The wait duration,
                         // is just to be sure to go and see if there is still
                         // some work from time to time.
                         sleep(200);
                     } catch (InterruptedException e) {
                         // Nothing to do. We'll just take a look at the loop
                         // condition.
                     }
                 }
             }
         } catch (JUploadException e) {
             this.fileUploadManagerThread.setUploadException(e);
         }
 
         this.uploadPolicy.displayDebug("End of the FileUploadThread", 5);
     }// run
 
     /**
      * Actual execution file(s) upload. It's called by the run methods, once for
      * all files, or file by file, depending on the UploadPolicy. The list of
      * files to upload is stored in the {@link #filesToUpload} array.<BR>
      * This method is called by the run() method. The prerequisite about the
      * filesToUpload array are: <DIR>
      * <LI>If the sum of contentLength for the files in the array is more than
      * the maxChunkSize, then nbFilesToUploadParam is one.
      * <LI>The number of elements in filesToUpload is less (or equal) than the
      * nbMaxFilesPerUpload. </DIR>
      * 
      * @throws JUploadException
      */
     final private void doUpload() throws JUploadException {
         boolean bChunkEnabled = false;
         long totalContentLength = 0;
         long totalFileLength = 0;
 
         // We are about to start a new upload.
         this.fileUploadManagerThread.setUploadStatus(0,
                 FileUploadManagerThread.UPLOAD_STATUS_UPLOADING);
 
         // Prepare upload, for all files to be uploaded.
         beforeRequest();
 
         for (int i = 0; i < this.filesToUpload.length
                 && !this.fileUploadManagerThread.isUploadStopped(); i++) {
             // Total length, for HTTP upload.
             totalContentLength += this.filesToUpload[i].getUploadLength();
             totalContentLength += getAdditionnalBytesForUpload(i);
             // Total file length: used to manage the progress bar (we don't
             // follow the bytes uploaded within headers and forms).
             totalFileLength += this.filesToUpload[i].getUploadLength();
 
             this.uploadPolicy.displayDebug("file "
                     + (this.fileUploadManagerThread.getNbUploadedFiles() + i)
                     + ": content=" + this.filesToUpload[i].getUploadLength()
                     + " bytes, getAdditionnalBytesForUpload="
                     + getAdditionnalBytesForUpload(i) + " bytes", 50);
         }// for
 
         // Ok, now we check that the totalContentLength is less than the chunk
         // size.
         if (totalFileLength >= this.maxChunkSize) {
             // hum, hum, we have to download file by file, with chunk enabled.
             // This a prerequisite of this method.
             if (this.filesToUpload.length > 1) {
                 this.fileUploadManagerThread
                         .setUploadException(new JUploadException(
                                 "totalContentLength >= chunkSize: this.filesToUpload.length should be 1 (doUpload)"));
             }
             bChunkEnabled = true;
         }
 
         // Now, we can actually do the job. This is delegate into smaller
         // method, for easier understanding.
         if (bChunkEnabled) {
             doChunkedUpload(totalContentLength, totalFileLength);
         } else {
             doNonChunkedUpload(totalContentLength, totalFileLength);
         }
 
         this.fileUploadManagerThread
                 .currentRequestIsFinished(this.filesToUpload);
 
         // We are finished with this packet. Let's display it.
         this.fileUploadManagerThread.setUploadStatus(this.filesToUpload.length,
                 FileUploadManagerThread.UPLOAD_STATUS_UPLOADED);
     }
 
     /**
      * Execution of an upload, in chunk mode. This method expects that the
      * {@link #filesToUpload} array contains only one line.
      * 
      * @throws JUploadException When any error occurs, or when there is more
      *             than one file in {@link #filesToUpload}.
      */
     final private void doChunkedUpload(final long totalContentLength,
             final long totalFileLength) throws JUploadException {
         boolean bLastChunk = false;
         int chunkPart = 0;
 
         long contentLength = 0;
         long thisChunkSize = 0;
 
         // No more than one file, when in chunk mode.
         if (this.filesToUpload.length > 1) {
             throw new JUploadException(
                    "totalContentLength >= chunkSize: this.filesToUpload.length should not be more than 1 (doUpload)");
         }
 
         // This while enables the chunk management:
         // In chunk mode, it loops until the last chunk is uploaded. This works
         // only because, in chunk mode,
         // files are uploaded one y one (the for loop within the while loops
         // through ... 1 unique file).
         // In normal mode, it does nothing, as the bLastChunk is set to true in
         // the first test, within the while.
         while (!bLastChunk
                 && this.fileUploadManagerThread.getUploadException() == null
                 && !this.fileUploadManagerThread.isUploadStopped()) {
             // Let's manage chunk:
             // Files are uploaded one by one. This is checked just above.
             chunkPart += 1;
             bLastChunk = (contentLength > this.filesToUpload[0]
                     .getRemainingLength());
 
             // Is this the last chunk ?
             if (bLastChunk) {
                 thisChunkSize = this.filesToUpload[0].getRemainingLength();
             } else {
                 thisChunkSize = this.maxChunkSize;
             }
             contentLength = thisChunkSize + getAdditionnalBytesForUpload(0);
 
             // Ok, we've prepare the job for chunk upload. Let's do it!
             startRequest(contentLength, true, chunkPart, bLastChunk);
 
             // Let's add any file-specific header.
             beforeFile(0);
 
             // Actual upload of the file:
             this.filesToUpload[0].uploadFile(getOutputStream(), thisChunkSize);
 
             // If we are not in chunk mode, or if it was the last chunk,
             // upload should be finished.
             if (bLastChunk && this.filesToUpload[0].getRemainingLength() > 0) {
                 throw new JUploadExceptionUploadFailed(
                         "Files has not be entirely uploaded. The remaining size is "
                                 + this.filesToUpload[0].getRemainingLength()
                                 + " bytes. File size was: "
                                 + this.filesToUpload[0].getUploadLength()
                                 + " bytes.");
 
             }
             // Let's add any file-specific header.
             afterFile(0);
 
             // Let's finish the request, and wait for the server Output, if
             // any (not applicable in FTP)
             int status = finishRequest();
 
             // We now ask to the uploadPolicy, if it was a success.
             // If not, the isUploadSuccessful should raise an exception.
             this.uploadPolicy.checkUploadSuccess(status, getResponseMsg(),
                     getResponseBody());
 
             cleanRequest();
         }
 
         // Let's tell our manager that we've done the job!
         this.fileUploadManagerThread
                 .anotherFileHasBeenSent(this.filesToUpload[0]);
     }// doChunkedUpload
 
     /**
      * Execution of an upload, in standard mode. This method uploads all files
      * in the {@link #filesToUpload} array.
      * 
      * @throws JUploadException When any error occurs, or when there is more
      *             than one file in {@link #filesToUpload}.
      */
     final private void doNonChunkedUpload(final long totalContentLength,
             final long totalFileLength) throws JUploadException {
 
         // First step is to prepare all files.
         startRequest(totalContentLength, false, 0, true);
 
         // Then, upload each file.
         for (int i = 0; i < this.filesToUpload.length
                 && !this.fileUploadManagerThread.isUploadStopped(); i++) {
             // We are about to start a new upload.
             this.fileUploadManagerThread.setUploadStatus(i,
                     FileUploadManagerThread.UPLOAD_STATUS_UPLOADING);
 
             // Let's add any file-specific header.
             beforeFile(i);
 
             // Actual upload of the file:
             this.filesToUpload[i].uploadFile(getOutputStream(),
                     this.filesToUpload[i].getUploadLength());
 
             // Let's add any file-specific header.
             afterFile(i);
 
             // Let's tell our manager that we've done the job!
             // Ok, maybe the server will refuse it, but we won't say that now!
             this.fileUploadManagerThread
                     .anotherFileHasBeenSent(this.filesToUpload[i]);
         }
 
         // We are finished with this one. Let's display it.
         this.fileUploadManagerThread
                 .setUploadStatus(
                         this.filesToUpload.length,
                         FileUploadManagerThread.UPLOAD_STATUS_UPLOADED_WAITING_FOR_RESPONSE);
 
         // Let's finish the request, and wait for the server Output, if
         // any (not applicable in FTP)
         int status = finishRequest();
 
         // We now ask to the uploadPolicy, if it was a success.
         // If not, the isUploadSuccessful should raise an exception.
         this.uploadPolicy.checkUploadSuccess(status, getResponseMsg(),
                 getResponseBody());
 
         cleanRequest();
 
     }// doNonChunkedUpload
 
     /** @see FileUploadThread#close() */
     public void close() {
         try {
             cleanAll();
         } catch (JUploadException e) {
             this.uploadPolicy.displayErr(e);
         }
     }
 
     /**
      * Replace single \r and \n by uniform end of line characters (CRLF). This
      * makes it easier, to search for string within the body.
      * 
      * @param s The original string
      * @return The string with single \r and \n modified changed to CRLF (\r\n).
      */
     public final String normalizeCRLF(String s) {
         Pattern p = Pattern.compile("\\r\\n|\\r|\\n", Pattern.MULTILINE);
         String[] lines = p.split(s);
         // Worst case: the s string contains only \n or \r characters: we then
         // need to triple the string length. Let's say double is enough.
         StringBuffer sb = new StringBuffer(s.length() * 2);
         for (int i = 0; i < lines.length; i += 1) {
             sb.append(lines[i]).append("\r\n");
         }
 
         return sb.toString();
     }
 
     /**
      * Replace \r and \n by correctly displayed end of line characters. Used to
      * display debug output. It also replace any single \r or \n by \r\n, to
      * make it easier, to search for string within the body.
      * 
      * @param s The original string
      * @return The string with \r and \n modified, to be correctly displayed.
      */
     public final String quoteCRLF(String s) {
         return s.replaceAll("\r\n", "\\\\r\\\\n\n");
     }
 }
