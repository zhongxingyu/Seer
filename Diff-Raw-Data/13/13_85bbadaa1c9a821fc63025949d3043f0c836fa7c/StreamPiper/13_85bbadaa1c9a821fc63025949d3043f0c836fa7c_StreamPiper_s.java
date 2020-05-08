 package expectj;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 /**
  * This class is responsible for piping the output of one stream to the
  * other. Optionally it also copies the content to standard out or
  * standard err.
  *
  * @author	Sachin Shekar Shetty
  */
 
 class StreamPiper extends Thread implements Runnable {
     /**
      * Log messages go here.
      */
     private final static Log LOG = LogFactory.getLog(StreamPiper.class);
 
     /**
      * Read data from here.
      */
     private InputStream inputStream = null;
 
     /**
      * Write data to here.
      */
     private OutputStream outputStream = null;
 
     /**
      * Optionally send a copy of all piped data to here.
      */
     private PrintStream copyStream = null;
 
     /**
      * When true we drop data from {@link #inputStream} rather than passing it
      * to {@link #outputStream}.
      */
     boolean pipingPaused = false;
 
     /**
      * When this turns false, we shut down.  All accesses to this variable should be
      * synchronized.
      */
     private boolean continueProcessing = true;
 
     /**
      * String Buffer to hold the contents of output and err.
      */
     private volatile StringBuffer sCurrentOut = new StringBuffer();
 
     /**
      * When data piping is paused, we just drop data from the input stream
      * rather than copying it to the output stream.
      *
      * @return True if piping is paused.  False otherwise.
      */
     private synchronized boolean getPipingPaused() {
         return pipingPaused;
     }
 
     /**
      * @param copyStream Stream to copy the contents to before piping
      * the data to another stream. When this parameter is null, it does
      * not copy the contents
      * @param pi Input stream to read the data
      * @param po Output stream to write the data
      */
     StreamPiper(PrintStream copyStream, InputStream pi, OutputStream po) {
         if (pi == null) {
             throw new NullPointerException("Input stream must not be null");
         }
         this.inputStream = pi;
         this.outputStream = po;
         this.copyStream = copyStream;
         // So that JVM does not wait for these threads
         this.setDaemon(true);
         this.setName("ExpectJ Stream Piper");
     }
 
     /**
      * This method is used to stop copying on to Standard out and err.
      * This is used after interact.
      */
     public synchronized void stopPipingToStandardOut() {
         pipingPaused = true;
     }
 
     /**
      * This method is used to start copying on to Standard out and err.
      * This is used after interact.
      */
     public synchronized void startPipingToStandardOut() {
         pipingPaused = false;
     }
 
     /**
      * This is used to stop the thread, after the process is killed
      */
     public synchronized void stopProcessing() {
         continueProcessing = false;
     }
 
     /**
      * Should we keep doing our thing?
      *
      * @return True if we should keep piping data.  False if we should shut down.
      */
     private synchronized boolean getContinueProcessing() {
         return continueProcessing;
     }
 
     /**
      * @return the entire available contents read from the stream
      */
     synchronized String getCurrentContents() {
         return sCurrentOut.toString();
     }
 
     /**
      * Thread method that reads from the stream and writes to the other.
      */
     public void run() {
         byte[] buffer = new byte[512];
         int bytes_read;
 
         try {
             while(getContinueProcessing()) {
                 bytes_read = inputStream.read(buffer);
                 if (bytes_read == -1) {
                     LOG.debug("Stream ended, closing");
                     inputStream.close();
                     outputStream.close();
                     return;
                 }
                 outputStream.write(buffer, 0, bytes_read);
                sCurrentOut.append(new String(buffer, 0, bytes_read));
                 if (copyStream != null && !getPipingPaused()) {
                     copyStream.write(buffer, 0, bytes_read);
                     copyStream.flush();
                 }
                 outputStream.flush();
             }
         } catch (IOException e) {
             if (getContinueProcessing()) {
                 LOG.error("Trouble while pushing data between streams", e);
             }
         }
     }
 
 }
