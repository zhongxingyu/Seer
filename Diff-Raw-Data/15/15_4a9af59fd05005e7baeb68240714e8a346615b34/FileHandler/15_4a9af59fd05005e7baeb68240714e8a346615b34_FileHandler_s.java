 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: FileHandler.java,v 1.3 2006-04-27 07:53:31 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.log.handlers;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.LogRecord;
 
 import com.iplanet.log.NullLocationException;
 import com.sun.identity.log.LogConstants;
 import com.sun.identity.log.LogManagerUtil;
 import com.sun.identity.log.Logger;
 import com.sun.identity.log.spi.Debug;
 
 /**
  * This <tt> FileHandler </tt> is very much similar to the
  * <t> java.util.logging.FileHandler </tt>. <p> The <TT> FileHandler </TT>
  * can either write to a specified file, or it can write to a rotating set
  * of files. <P>
  * For a rotating set of files, as each file reaches the limit
  * (<i> LogConstants.MAX_FILE_SIZE</I>), it is closed, rotated out, and a
  * new file opened. Successively older files and named by adding "-1", "-2",
  * etc., * to the base filename. The Locking mechanism is much more relaxed 
  * (in JDK's  FileHandler an exclusive lock is created on the file till the
  * handler is closed which makes reading impossible)
  */
 public class FileHandler extends java.util.logging.Handler {
     
     private LogManager lmanager = LogManagerUtil.getLogManager();
     private OutputStream output;
     private Writer writer;
     private MeteredStream meteredStream;
     private File files[];
     private boolean headerWritten;
     private int count; // count represent number of history files
     private int maxFileSize;
     private String location;
     private String fileName;
     private int recCount = 0;
     private int recCountLimit;
     private String recordBuffer[];
     private Timer bufferTimer;
     private boolean timeBufferingEnabled = false;
     private static String headerString = null;
     
     private class MeteredStream extends OutputStream {
         OutputStream out;
         int written;
         
         MeteredStream(OutputStream out, int written) {
             this.out = out;
             this.written = written;
         }
         /**
          * writes a single integer to the outputstream and increments 
          * the number of bytes written by one.
          * @param b integer value to be written.
          * @throws IOException if it fails to write out.
          */
         public void write(int b) throws IOException {
             out.write(b);
             written ++;
         }
         /**
          * Writes the array of bytes to the output stream and increments 
          * the number of bytes written by the size of the array.
          * @param b the byte array to be written. 
          * @throws IOException if it fails to write out.
          */
         public void write(byte [] b) throws IOException {
             out.write(b);
             written += b.length;
         }
         /**
          * Writes the array of bytes to the output stream and increments 
          * the number of bytes written by the size of the array.
          * @param b the byte array to be written. 
          * @param offset the offset of array to be written. 
          * @param length the length of bytes to be written. 
          * @throws IOException if it fails to write out.
          */
         public void write(byte [] b, int offset, int length)
         throws IOException {
             out.write(b, offset, length);
             written += length;
         }
         /**
          * Flush any buffered messages.
          * @throws IOException if it fails to write out.
          */
         public void flush() throws IOException {
             out.flush();
         }
         /**
          * close the current output stream.
          * @throws IOException if it fails to close output stream.
          */
         public void close() throws IOException {
             out.close();
         }
     }
     
     /**
      * sets the output stream to the specified output stream ..picked up from
      * StreamHandler.
      */
     private void setOutputStream(OutputStream out) throws SecurityException,
     UnsupportedEncodingException {
         if (out == null) {
             if (Debug.warningEnabled()) {
                 Debug.warning(fileName + ":FileHandler: OutputStream is null");
             }
         }
         output = out;
         headerWritten = false;
         String encoding = getEncoding();
         if (encoding == null) {
             writer = new OutputStreamWriter(output);
         } else {
             try {
                 writer = new OutputStreamWriter(output, encoding);
             } catch (UnsupportedEncodingException e) {
                 Debug.error(fileName + ":FileHandler: Unsupported Encoding", e);
                 throw new UnsupportedEncodingException(e.getMessage());
             }
         }
     }
     
     
     /**
      * Set (or change) the character encoding used by this <tt>Handler</tt>.
      * The encoding should be set before any <tt>LogRecords</tt> are written
      * to the <tt>Handler</tt>.
      *
      * @param encoding  The name of a supported character encoding.
      *        May be null, to indicate the default platform encoding.
      * @exception  SecurityException  if a security manager exists and if
      *             the caller does not have
      *             <tt>LoggingPermission("control")</tt>.
      * @exception UnsupportedEncodingException if the named encoding is
      *           not supported.
      */
     public void setEncoding(String encoding) throws SecurityException,
     UnsupportedEncodingException {
         super.setEncoding(encoding);
         if (output == null) {
             return;
         }
         // Replace the current writer with a writer for the new encoding.
         cleanup();
         if (encoding == null) {
             writer = new OutputStreamWriter(output);
         } else {
             writer = new OutputStreamWriter(output, encoding);
         }
     }
     
     /**
      * This method is used for getting the properties from LogManager
      * and setting the private variables count, maxFileSize etc.
      */
     private void configure() 
     throws NullLocationException, FormatterInitException {
         
         String bufferSize = lmanager.getProperty(LogConstants.BUFFER_SIZE);
         if (bufferSize != null && bufferSize.length() > 0) {
             try {
                 recCountLimit = Integer.parseInt(bufferSize);
             } catch (NumberFormatException nfe) {
                 Debug.warning(fileName + 
                     ":FileHandler: NumberFormatException ", nfe);
                 if (Debug.messageEnabled()) {
                     Debug.message(fileName + 
                         ":FileHandler: Setting buffer size to 1");
                 }
                 recCountLimit = 1;
             }
         } else {
             Debug.warning(fileName + 
                 ":FileHandler: Invalid buffer size: " + bufferSize);
             if (Debug.messageEnabled()) {
                 Debug.message(fileName + 
                     ":FileHandler: Setting buffer size to 1");
             }
             recCountLimit = 1;
         }
         
         String status = lmanager.getProperty(
             LogConstants.TIME_BUFFERING_STATUS);
          
         if ( status != null && status.equalsIgnoreCase("ON")) 
         {
             timeBufferingEnabled = true;
         }
         
         String strCount = lmanager.getProperty(LogConstants.NUM_HISTORY_FILES);
         if ((strCount == null) || (strCount.length() == 0)) {
             count = 0;
         } else {
             count = Integer.parseInt(strCount);
         }
         
         String strMaxFileSize = lmanager.getProperty(
             LogConstants.MAX_FILE_SIZE);
         if ((strMaxFileSize == null) || (strMaxFileSize.length() == 0)) {
             maxFileSize = 0;
         } else {
             maxFileSize = Integer.parseInt(strMaxFileSize);
         }
         location = lmanager.getProperty(LogConstants.LOG_LOCATION);
         if ((location == null) || (location.length() == 0)) {
             throw new NullLocationException(
                 "Location Not Specified"); //localize
         }
         if (!location.endsWith(File.separator)) {
             location += File.separator;
         }
     }
     
     private void openFiles(String fileName) throws IOException {
         // making sure that we have correct count and maxFileSize
         if (count < 0) {
             Debug.error(fileName + 
                 ":FileHandler: no. of history files negative " + count);
             count = 0;
         }
         if (maxFileSize < 0) {
             Debug.error(fileName + 
                 ":FileHandler: maxFileSize cannot be negative");
             maxFileSize = 0;
         }
         files = new File[count + 1]; // count is the number of history files
         for (int i = 0; i < count + 1; i ++) {
             if (i != 0) {
                 files[i] = new File(fileName + "-" + i);
             } else {
                 files[0] = new File(fileName);
             }
         }
         open(files[0], true);
     }
     
     /** 
      * Algorithm: Check how many bytes have already been written to to that file
      * Get an instance of MeteredStream and assign it as the output stream. 
      * Create a file of the name of FileOutputStream.
      */
     private void open(File fileName, boolean append) throws IOException {
         String filename = fileName.toString();
         int len = 0;
         len = (int)fileName.length();
         FileOutputStream fout = new FileOutputStream(filename, append);
         
         BufferedOutputStream bout = new BufferedOutputStream(fout);
         meteredStream = new MeteredStream(bout, len);
         setOutputStream(meteredStream);
         checkForHeaderWritten(fileName.toString());
     }
     
     /**
      * Creates a new FileHandler. It takes a string parameter which represents
      * file name. When this constructor is called a new file to be created.
      * Assuming that the fileName logger provides is the timestamped fileName.
      * @param fileName The filename associate with file handler.
      */
     public FileHandler(String fileName) {
         if ((fileName == null) || (fileName.length() == 0)) {
             return;
         }
         this.fileName = fileName;
         try {
             configure();
         } catch (NullLocationException nle) {
             Debug.error(fileName + ":FileHandler: Location not specified", nle);
         } catch (FormatterInitException fie) {
             Debug.error(fileName + 
                 ":FileHandler: couldnot instantiate Formatter", fie);
         }
         fileName = location + fileName;
         Logger logger = (Logger)Logger.getLogger(this.fileName);
         if (logger.getLevel() != Level.OFF) {
             try {
                 openFiles(fileName);
             } catch (IOException ioe) {
                 Debug.error(fileName + ":FileHandler: Unable to open Files",
                     ioe);
             }
         }
         logger.setCurrentFile(this.fileName);
         
         recordBuffer = new String[recCountLimit];
         
         if (timeBufferingEnabled) {
             startTimeBufferingThread();
         }
     }
     
     private void cleanup() {
         if (writer != null) {
             try {
                 writer.flush();
             } catch (Exception ex) {
                 Debug.error(fileName + 
                     ":FileHandler: Couldnot Flush Output", ex);
             }
         }
     }
     
     /**
      * Flush any buffered messages and Close all the files.
      */
     public void close() {
         flush();
         if (writer != null) {
             try {
                 writer.close();
             } catch (IOException e) {
                 Debug.error(fileName + ":FileHandler: Error closing writer",e);
             }
         }
         stopBufferTimer();
     }
     
     /**
      * Format and publish a LogRecord.
      * <p>
      * This FileHandler is associated with a Formatter, which has to format the
      * LogRecord according to ELF and return back the string formatted as per 
      * ELF. This method first checks if the header is already written to the 
      * file, if not, gets the header from the Formatter and writes it at the 
      * beginning of the file.
      * @param lrecord the log record to be published.
      */
     public synchronized void publish(LogRecord lrecord) {
         if (maxFileSize <= 0) {
             return;
         }
         if (!isLoggable(lrecord)) {
             return;
         }
         String message = getFormatter().format(lrecord);
         recordBuffer[recCount] = message;
         this.recCount++;
        if (this.recCount == recCountLimit) {
             if (Debug.messageEnabled()) {
                 Debug.message(fileName + ":FileHandler.publish(): got " + 
                         recCount + " records, writing all");
             }
             flush();
         }
     }
 
     private String getHeaderString() {
         if (headerString == null) {
             headerString = getFormatter().getHead(this);
         }
         return headerString;        
     }
     /**
      * Flush any buffered messages.
      */
     public synchronized void flush() {
         if (recCount <= 0) {
             if (Debug.messageEnabled()) {
                 Debug.message(fileName + 
                     ":FileHandler.flush: no records in buffer to write");
             }
             return;
         }
         if (writer == null) {
             Debug.error(fileName + ":FileHandler: Writer is null");
             this.recCount = 0;
             return;
         }
         if (Debug.messageEnabled()) {
             Debug.message(fileName + ":FileHandler.flush: writing " +
                             "buffered records");
         }
         for (int i=0; i < recCount; ++i) {
             String message = recordBuffer[i];
             if ((message.length() > 0 ) && 
                 (meteredStream.written + message.length()) >= maxFileSize) 
             {
                 if (Debug.messageEnabled()) {
                     Debug.message(fileName + 
                         ":FileHandler: Rotation condition reached");
                 }
                 rotate();
             }
             try {
                 if (!headerWritten) {
                     writer.write(getHeaderString());
                     headerWritten = true;
                 }
                 writer.write(message);
             } catch (IOException ex) {
                 Debug.error(fileName + ":FileHandler: couldnot write " +
                                 "to file", ex);
             }
             cleanup();
         }
         this.recCount = 0;
     }
     
     private void rotate() {
         if (writer != null) {
             try {
                 writer.flush();
                 writer.close();
             } catch (Exception ex) {
                 Debug.error(fileName + ":FileHandler: " +
                                 "Error closing writer", ex);
             }
         }
         //
         //  delete file<n>; file<n-1> becomes file<n>; and so on.
         //
         for (int i = count-2; i >= 0; i--) {
             File f1 = files[i];
             File f2 = files[i+1];
             if (f1.exists()) {
                 if (f2.exists()) {
                     try {
                         f2.delete();
                     } catch (SecurityException secex) {
                         Debug.error(fileName +
                             ":FileHandler: could not delete file. msg = " +
                             secex.getMessage());
                     }
                 }
                 boolean renameSuccess = f1.renameTo(f2);
                 // In case renaming fails, copy the contents of source file
                 // to destination file.
                 if (!renameSuccess) {
                     copyFile(f1.toString(), f2.toString());
                 }
             }
         }
         try {
             open(files[0], false);
         } catch (IOException ix) {
             Debug.error(fileName + ":FileHandler: error opening file" + ix);
         }
     }
     
     private void copyFile(String input, String output) {
         if (Debug.messageEnabled()) {
             Debug.message(fileName + ":FileHandler: CopyFile Method called");
         }
         try {
             //input file
             FileInputStream fis = new FileInputStream(input);
             int s;
             //output file
             FileOutputStream fos = new FileOutputStream(output);
             while ((s = fis.read()) > -1 ) {
                 fos.write(s);
             }
         } catch(FileNotFoundException fnfe) {
             Debug.error(fileName + ":FileHandler: copyFile: File not found: ", 
                 fnfe);
         } catch(IOException ioex) {
             Debug.error(fileName + ":FileHandler: copyFile: IOException", 
                 ioex);
         }
     }
     
     private void checkForHeaderWritten(String fileName) {
         byte [] bytes = new byte[1024];
         try {
             FileInputStream fins = new FileInputStream(new File(fileName));
             fins.read(bytes);
         } catch (IOException ioe) {
             Debug.error(fileName + ":FileHandler: couldnot read file content", 
                 ioe);
         }
         String fileContent = new String(bytes);
         fileContent = fileContent.trim();
         if (fileContent.startsWith("#Version")) {
             headerWritten = true;
         } else {
             headerWritten = false;
         }
     }
     
     private class TimeBufferingTask extends TimerTask {
         /**
          * The method which implements the TimerTask.
          */
         public void run() {
             if (Debug.messageEnabled()) {
                 Debug.message(fileName + 
                     ":FileHandler:TimeBufferingTask.run() called");
             }
             flush();
         }
     }
     
     private void startTimeBufferingThread() {
         String period = lmanager.getProperty(LogConstants.BUFFER_TIME);
         long interval;
         if((period != null) || (period.length() != 0)) {
             interval = Long.parseLong(period);
         } else {
             interval = LogConstants.BUFFER_TIME_DEFAULT;
         }
         interval *=1000;
         if(bufferTimer == null){
             bufferTimer = new Timer();
             bufferTimer.scheduleAtFixedRate(
                 new TimeBufferingTask(), interval, interval);
             if (Debug.messageEnabled()) {
                 Debug.message(fileName + 
                     ":FileHandler: Time Buffering Thread Started");
             }
         }
     }
 
     private void stopBufferTimer() {
         if(bufferTimer != null) {
             bufferTimer.cancel();
             if (Debug.messageEnabled()) {
                 Debug.message(fileName + ":FileHandler: Buffer Timer Stopped");
             }
         }
     }
 }
