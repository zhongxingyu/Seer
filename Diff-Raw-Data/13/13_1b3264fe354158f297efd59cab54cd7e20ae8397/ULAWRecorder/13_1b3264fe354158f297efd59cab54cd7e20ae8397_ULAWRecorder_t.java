 package iax.audio.ulaw;
 
 import java.io.InputStream;
 import iax.audio.AudioListener;
 import iax.audio.Recorder;
 import iax.audio.RecorderException;
 import iax.protocol.call.Call;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 
 
 /**
  * ULAW audio recorder.
  * Reads audio from inStream and sends it to a class implementing AudioListener.
  * Analog to PCMRecorder
  */
 
 public class ULAWRecorder extends Recorder {
 
     private AudioListener call;
     private boolean recording;
     private final int buffer_size;
     private final int buffer_time;
     private Thread captureThread;
 
     private InputStream inStream;
 
     /**
      * Constructor. Initializes recorder.
      * @throws RecorderException
      */
     public ULAWRecorder(Call call, InputStream inStream) throws
             RecorderException {
         this.call = call;
         if (inStream == null)
             throw new RecorderException("inStream null");
         this.inStream = inStream;
         buffer_size = 160;
         buffer_time = 20;
         recording = false;
     }
 
     /**
      * Starts recording.
      * @param al Object that is going to process the recorded audio data.
      */
 
     public void record(AudioListener al) {
         call = al;
 
         captureThread = new Thread(new CaptureThread(), "UlawRecorder");
         recording = true;
         captureThread.start();
     }
 
 
     /**
      * Stops recording.
      */
     public void stop() {
         recording = false;
     }
 
     class CaptureThread implements Runnable {
 
         private FileOutputStream fos;
         private byte[] silence;
         private byte silenceByte;
 
         public CaptureThread() {
             try {
                 fos = new FileOutputStream("njiax_sent.raw");
             } catch (FileNotFoundException ex) {
                 ex.printStackTrace();
             }
             silenceByte = 0x7F;
             silence = new byte[buffer_size];
             for (int i = 0; i < buffer_size; i++) {
                 silence[i] = silenceByte;
             }
         }
 
         public void run() {
             byte buffer[] = new byte[buffer_size];
             int count;
             long startTime;
             long endTime;
 
            //byte silenceBuffer[] = new byte[buffer_size];
            /*for (int i = 0; i < silenceBuffer.length; i++) {
                 silenceBuffer[i] = 0x7f;
            }*/
 
             try {
                 while (recording) {
                     //Start measuring time
                     //startTime = System.currentTimeMillis();
                     startTime = (long)(System.nanoTime() * 1E-6);
                     //System.out.println("SSns: "+startTime + "\nSSss: " + System.currentTimeMillis() );
 
                      if (inStream.available() > 0) {
                         //Read buffer from app to send to Asterisk
                         count = inStream.read(buffer, 0, buffer.length);
                         //System.err.println("ULawRecorder DATA AVAILABLE: "+count);
                     }
                     else {
                         count = 0;
                         //System.err.println("ULawRecorder NO DATA AVAILABLE");
                     }
                     /*if (count == 0) {
                         //System.err.println("ULAWRECORDER.... WILL SEND SILENCE........");
                         System.arraycopy(silenceBuffer, 0, buffer, 0, buffer_size);
                         count = buffer_size;
                         try {
                             Thread.sleep(buffer_time);
                         } catch (InterruptedException ex1) {
                             ex1.printStackTrace();
                         }
                     }*/
 
                     if (count > 0) {
                         if (count != buffer_size) {
                             System.err.println(">>> WILL SEND LESS BYTES THAN EXPECTED!!!!!!!!!!!!!!!!!: "+count + " vs: " + buffer_size);
                         }
                         fos.write(buffer, 0, buffer_size); //not count because want to log read failures....
 
                         // synchronized (buffer) {
                         //long start = System.currentTimeMillis();
                         call.listen(buffer, 0, count);
                         //endTime = System.currentTimeMillis();
                         endTime = (long)(System.nanoTime() * 1E-6);
                         //}
 
                         double nBytes = ((endTime - startTime) / 8000f) * 1000f;
                         for (int i = (int) Math.ceil(nBytes); i-- > 0; ) {
                             fos.write(silenceByte);
                         }
 
                         long procTime = endTime - startTime;
                         long sleepTime = buffer_time;
                         if (procTime > 0) {
                             sleepTime -= procTime;
                         }
                         //sleepTime *= 0.95;
                        sleepTime -= 5;
                         if (sleepTime > 0) {
                             //try {
                                 // long startSleepTime = System.currentTimeMillis();
                                 //System.out.println("\t\t\t\t\t\tCaptureThread will sleep: "+sleepTime);
                                 //Thread.currentThread().sleep(0, (int)sleepTime * 1000);
                                 Thread.sleep(sleepTime);
                                 //Thread.sleep(0, 999999);
                                 //sleepTime--;
                                 //PreciseTimer.sleep(sleepTime);
                                 ///////////////////////////fos.write(silence);
                                // long endtSleepTime = System.currentTimeMillis();
                                // long sleptBy = endtSleepTime - startSleepTime;
                                /* if (sleptBy > sleepTime) {
                                     System.err.println("Slept more than it should: " + sleptBy + " should be: "+sleepTime);
                                 }*/
                             /*} catch (InterruptedException ex) {
                                 ex.printStackTrace();
                             }*/
                         }
                     } else if (count == 0) {
                         //System.out.println("nothing to write");
                         //try {
                             //PreciseTimer.sleep((long)(buffer_time));
                              Thread.sleep(buffer_time - 1);
                             //Thread.sleep(buffer_time);
                         /*} catch (InterruptedException ex1) {
                             ex1.printStackTrace();
                         }*/
                         fos.write(silence);
                     }
                }//while
 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 }
