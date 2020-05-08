 package no.ebakke.studycaster.screencasting;
 
 import java.awt.AWTException;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.io.OutputStream;
 import no.ebakke.studycaster.api.StudyCaster;
 import no.ebakke.studycaster.screencasting.CaptureScheduler.CaptureTask;
 import no.ebakke.studycaster.util.stream.NonBlockingOutputStream;
 
 public class ScreenRecorder {
   private NonBlockingOutputStream nbos;
   private CaptureEncoder enc;
   private boolean stopped = true;
   private IOException storedException;
   private CaptureScheduler pointerRecorder, frameRecorder;
   private CaptureTask pointerRecorderTask = new CaptureTask() {
     public void capture() {
       enc.capturePointer();
     }
 
     public double getMaxFrequency() {
       return 15.0;
     }
 
     public double getMaxDutyCycle() {
       return 0.1;
     }
 
     @Override
     public String toString() {
       return "pointerRecorderTask";
     }
   };
   private CaptureTask frameRecorderTask = new CaptureTask() {
     public void capture() {
       try {
         enc.captureFrame();
       } catch (IOException e) {
         pointerRecorder.interrupt();
         frameRecorder.interrupt();
         storedException = e;
       }
     }
 
     public double getMaxFrequency() {
       if (nbos == null) {
         return 5.0;
       } else {
         // TODO: Find a less ad-hoc way of doing this.
         double fillLevel = ((double) nbos.getRemainingBytes()) / ((double) nbos.getBufferLimitBytes());
         if (fillLevel > 0.9) {
           return 0.0;
         } else if (fillLevel > 0.75) {
           return 0.5;
         } else if (fillLevel > 0.50) {
           return 1.0;
         } else {
           return 5.0;
         }
       }
     }
 
     public double getMaxDutyCycle() {
       return 0.7;
     }
 
     @Override
     public String toString() {
       return "frameRecorderTask";
     }
   };
 
   public ScreenRecorder(OutputStream out, long serverSecondsAhead) throws IOException, AWTException {
     // TODO: Get rid of this hack.
     if (out instanceof NonBlockingOutputStream)
       nbos = (NonBlockingOutputStream) out;
     Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
     enc = new CaptureEncoder(out, screenRect);
     enc.setServerSecondsAhead(serverSecondsAhead);
   }
 
  public synchronized void setCensor(ScreenCensor censor) {
     enc.setCensor(censor);
   }
 
   public synchronized void start() {
     if (!stopped) {
       StudyCaster.log.warning("Already started");
       return;
     }
     stopped = false;
     pointerRecorder = new CaptureScheduler(pointerRecorderTask);
     frameRecorder   = new CaptureScheduler(frameRecorderTask);
   }
 
 
  public void stop() {
    // TODO: Remove these logging messages once we're confident that the deadlock bug has been resolved.
    StudyCaster.log.info("ScreenRecorder.stop() called");
    synchronized (this) {
      if (stopped) {
        StudyCaster.log.warning("Already stopped");
        return;
      }
      stopped = true;
     }
    StudyCaster.log.info("Calling pointerRecorder.finish()");
     pointerRecorder.finish();
    StudyCaster.log.info("Calling frameRecorder.finish()");
     frameRecorder.finish();
    StudyCaster.log.info("ScreenRecorder.stop() completed");
   }
 
   /** Closes the underlying OutputStream as well. */
   public void close() throws IOException {
     if (!stopped)
       stop();
     if (storedException != null)
       throw storedException;
     enc.finish();
   }
 }
