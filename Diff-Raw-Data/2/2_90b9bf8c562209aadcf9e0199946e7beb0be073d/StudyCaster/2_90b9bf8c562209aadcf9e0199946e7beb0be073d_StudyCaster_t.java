 package no.ebakke.studycaster.api;
 
 import java.awt.AWTException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.ebakke.studycaster.util.stream.NonBlockingOutputStream;
 import no.ebakke.studycaster.screencasting.ScreenCensor;
 import no.ebakke.studycaster.screencasting.ScreenRecorder;
 import no.ebakke.studycaster.screencasting.ScreenRecorderConfiguration;
 import no.ebakke.studycaster.util.stream.ConsoleTee;
 import no.ebakke.studycaster.util.stream.StreamProgressObserver;
 import org.apache.commons.io.IOUtils;
 
 /** This class will go away in a future version. */
 public class StudyCaster {
   // TODO: Figure out which members can be final or need to be volatile. Or just delete this class.
   private static final Logger LOG = Logger.getLogger("no.ebakke.studycaster");
   // TODO: Make this configurable.
   private static final int RECORDING_BUFFER_SZ = 4 * 1024 * 1024;
   private ServerContext serverContext;
   private ScreenRecorder recorder;
   private boolean concluded = false;
   private NonBlockingOutputStream recordingStream;
   private ConsoleTee consoleTee;
   private Thread shutdownHook = new Thread(new Runnable() {
     public void run() {
       LOG.warning("Study not explicitly concluded; concluding via shutdown hook.");
       concludeStudy();
     }
   }, "StudyCaster-shutdownHook");
 
   public ServerContext getServerContext() {
     return serverContext;
   }
 
   private void disconnectConsole() {
     if (consoleTee == null)
       return;
     LOG.info("Disconnecting console");
     try {
       consoleTee.close();
     } catch (IOException e) {
       LOG.log(Level.WARNING, "Error while disconnecting console tee", e);
     } finally {
       consoleTee = null;
     }
   }
 
   public StudyCaster() throws StudyCasterException {
     NonBlockingOutputStream consoleStream = new NonBlockingOutputStream(1024 * 128);
     ServerTimeLogFormatter logFormatter = new ServerTimeLogFormatter();
     consoleTee = new ConsoleTee(consoleStream, logFormatter);
 
     try {
       serverContext = new ServerContext();
     } catch (StudyCasterException e) {
       LOG.log(Level.SEVERE, "Error initializing StudyCaster", e);
       disconnectConsole();
       throw e;
     }
     Runtime.getRuntime().addShutdownHook(shutdownHook);
     logFormatter.setServerSecondsAhead(serverContext.getServerSecondsAhead());
     try {
       consoleStream.connect(serverContext.uploadFile("console.txt"));
     } catch (IOException e) {
       LOG.log(Level.WARNING, "Error creating remote log file", e);
       disconnectConsole();
     }
     recordingStream = new NonBlockingOutputStream(RECORDING_BUFFER_SZ);
     recordingStream.addObserver(new StreamProgressObserver() {
       private final AtomicBoolean warnedYet = new AtomicBoolean(false);
 
       public void updateProgress(NonBlockingOutputStream nbos) {
         long bytesRemaining = nbos.getBytesPosted() - nbos.getBytesWritten();
         // Hysteresis is applied to avoid flooding the console with warnings.
         if (bytesRemaining > recordingStream.getBufferLimitBytes() * 0.8 &&
            !warnedYet.getAndSet(true))
         {
           LOG.log(Level.WARNING, "Close to overfilled buffer ({0}/{1} bytes)",
               new Object[]{bytesRemaining, recordingStream.getBufferLimitBytes()});
         }
         if (bytesRemaining < recordingStream.getBufferLimitBytes() * 0.2)
           warnedYet.set(false);
       }
     });
     try {
       recordingStream.connect(serverContext.uploadFile("screencast.ebc"));
       recorder = new ScreenRecorder(recordingStream, serverContext.getServerSecondsAhead(),
           ScreenRecorderConfiguration.DEFAULT);
     } catch (IOException e) {
       LOG.log(Level.WARNING, "Failed to initialize screen recorder", e);
     } catch (AWTException e) {
       LOG.log(Level.WARNING, "Failed to initialize screen recorder", e);
     }
     if (recorder == null) {
       try {
         recordingStream.close();
       } catch (IOException e) { }
       recordingStream = null;
     }
     LOG.info("Constructed StudyCaster");
   }
 
   public void concludeStudy() {
     synchronized (this) {
       if (concluded)
         return;
       concluded = true;
     }
     LOG.info("Concluding study");
     try {
       Runtime.getRuntime().removeShutdownHook(shutdownHook);
     } catch (IllegalStateException e) {
     } catch (SecurityException e) {
     }
     try {
       waitForScreenCastUpload();
     } catch (StudyCasterException e) {
       LOG.log(Level.WARNING, "Failed to upload screencast while concluding study", e);
     }
     LOG.info("Concluded study");
     disconnectConsole();
   }
 
   public void downloadFile(String remoteName, File toFile) throws StudyCasterException {
     try {
       OutputStream os = new FileOutputStream(toFile);
       try {
         InputStream is = serverContext.downloadFile(remoteName);
         try {
           IOUtils.copy(is, os);
           return;
         } finally {
           is.close();
         }
       } finally {
         os.close();
       }
     } catch (IOException e) {
       throw new StudyCasterException(e);
     }
   }
 
   public void uploadFile(File f, String remoteName) throws StudyCasterException {
     try {
       //System.out.println("Uploading a file of length " + f.length());
       OutputStream os = serverContext.uploadFile(remoteName);
       try {
         IOUtils.copy(new FileInputStream(f), os);
       } finally {
         os.close();
       }
     } catch (IOException e) {
       throw new StudyCasterException(e);
     }
   }
 
   public void startRecording(ScreenCensor censor) throws StudyCasterException {
     if (recorder != null) {
       recorder.setCensor(censor);
       recorder.start();
     }
   }
 
   public void stopRecording() throws StudyCasterException {
     if (recorder != null) {
       try {
         recorder.stop();
       } catch (IOException e) {
         throw new StudyCasterException("Problem during screen recording", e);
       }
     }
   }
 
   public void waitForScreenCastUpload() throws StudyCasterException {
     if (recorder != null) {
       LOG.info("Waiting for screencast upload to complete as much as possible");
       try {
         recorder.close();
       } catch (IOException e) {
         throw new StudyCasterException("Error uploading screencast", e);
       } finally {
         recorder = null;
       }
     }
   }
 
   // TODO: Don't expose this.
   public NonBlockingOutputStream getRecordingStream() {
     return recordingStream;
   }
 }
