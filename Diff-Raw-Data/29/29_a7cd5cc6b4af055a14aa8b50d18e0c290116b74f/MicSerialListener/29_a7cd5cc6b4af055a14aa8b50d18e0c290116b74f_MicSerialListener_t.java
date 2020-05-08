 package com.sonrlabs.test.sonr;
 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

 import org.acra.ErrorReporter;
 
 import android.media.AudioRecord;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class MicSerialListener
         extends Thread {
     private static final String TAG = "MicSerialListener";
 
     public static final short SERIAL_TRANSMITTER_BAUD = 2400;
     public static final int SAMPLE_RATE = 44100; // In Hz
     public static final int FRAMES_PER_BIT = SAMPLE_RATE / SERIAL_TRANSMITTER_BAUD;
     public static final int TRANSMISSION_LENGTH = FRAMES_PER_BIT * 8;
     public static final int BIT_OFFSET = MicSerialListener.FRAMES_PER_BIT * 2;
     public static final int PREAMBLE = 64 * FRAMES_PER_BIT;
     public static final int SAMPLE_LENGTH = PREAMBLE + 3 * (TRANSMISSION_LENGTH + BIT_OFFSET);
     public static final int AVE_LEN = 9;
     public static final int BEGIN_OFFSET = PREAMBLE - TRANSMISSION_LENGTH - BIT_OFFSET; // allow
     /* phone's internal AGC to stabilize first */
     public static final int END_OFFSET = TRANSMISSION_LENGTH + BIT_OFFSET; // allow
     /* phone's internal AGC to stabilize first */
 
     public static final int THRESHOLD = 4000; // beginning of a sample
 
     public static final int MAX_TRANSMISSIONS = 10; // no more than 10
     // transmissions in a single
     // sample
 
     public static int SIGNAL_MAX_SUM = 0;
 
     private static long CHECK_TIME = 1150; // 1.15 seconds
 
     private AudioRecord inStream; // Serial input catcher
     private int bufferSize; // size of inStream's buffer, set by
     // AudioRecord.getMinBufferSize() in constructor
     private boolean running; // condition for run() loop - constructor sets it
     // to true, stopDriver() sets to false
     private ByteReceiver myByteReceiver; // Received bytes are sent to
     // myByteReciver.reciveByte(byte
     // recivedByte)
 
     private int numSamples;
     private short sample_buf1[], sample_buf2[]; // switch out the buffers
 
     // we want to declare AudioProcessor data here so that we don't keep making
     // new arrays and run out of memory
     private int[][] trans_buf = new int[MAX_TRANSMISSIONS * 3][TRANSMISSION_LENGTH + BIT_OFFSET];
     private int[] test_buf = new int[SAMPLE_LENGTH];
     private int[] movingsum = new int[TRANSMISSION_LENGTH];
     private int[] movingsum2 = new int[PREAMBLE + 3 * (TRANSMISSION_LENGTH + BIT_OFFSET) + 1];
     private int[] movingbuf = new int[9];
     private int[][] sloc = new int[MicSerialListener.MAX_TRANSMISSIONS][3];
     private int[] byteInDec = new int[MicSerialListener.MAX_TRANSMISSIONS * 3];
 
     private boolean switchbuffer = false;
     private boolean readnewbuf = true;
 
     private AudioProcessor myaudioprocessor = null;
 
     private boolean found_dock = false;
 
     private long start_check = 0;
     
    private int sampleloc[] = new int[3];
    
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
     
     MicSerialListener(AudioRecord theaudiorecord, int buffsize, ByteReceiver theByteReceiver) {
         super(TAG);
         try {
             if (inStream == null) { // screen turned sideways, dont
                 // re-initialize to null
                 myByteReceiver = theByteReceiver;
 
                 Log.d(TAG, "STARTED");
 
                 inStream = theaudiorecord;
                 bufferSize = buffsize;
 
                 if (inStream != null) {
                     sample_buf1 = new short[bufferSize];
                     sample_buf2 = new short[bufferSize];
 
                     // set up thread
                     running = true;
                     this.setDaemon(true);
                     inStream.startRecording();
                 } else {
                     // LogFile.MakeLog("Failed to initialize AudioRecord");
                     Log.d(TAG, "Failed to initialize AudioRecord");
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             ErrorReporter.getInstance().handleException(e);
         }
     }
     
     boolean foundDock() {
         return found_dock;
     }
 
     void searchSignal() {
         try {
             start_check = SystemClock.elapsedRealtime();
             boolean problem = false;
             while (SystemClock.elapsedRealtime() - start_check < CHECK_TIME && !found_dock) {
                 numSamples = inStream.read(sample_buf1, 0, bufferSize);
                 if (numSamples > 0) {
                     found_dock = AutoGainControl();
                 } else {
                     problem = true;
                 }
             }
             if (problem) {
                 Log.d("MicSerialListener", "Mic input was unavailable to be read");
                 ErrorReporter.getInstance().putCustomData("MicSerialListener", "Mic input was unavailable to be read");
             }
         } catch (Exception e) {
             e.printStackTrace();
             ErrorReporter.getInstance().handleException(e);
         }
     }
 
     @Override
     public void run() { // thread reads in from mic and dispatches an audio
         // processor to process the data
         try {
             while (running) {
                 // Log.d("SONR audio processor", "NEW RECORDING");
                 if (switchbuffer && readnewbuf) {
                     // this is so that the buffer doesn't get overwritten
                     // while audio processor is
                     // working
                     numSamples = inStream.read(sample_buf2, 0, bufferSize);
                     readnewbuf = false;
                     // Log.d("SONR audio processor", "READ BUFFER 2");
                 } else if (!switchbuffer && readnewbuf) {
                     readnewbuf = false;
                     numSamples = inStream.read(sample_buf1, 0, bufferSize);
                     // Log.d("SONR audio processor", "READ BUFFER 1");
                 }
 
                 if (numSamples > 0 && myaudioprocessor != null && myaudioprocessor.isWaiting()) {
                     /* if a signal got cut off and audioprocessor is waiting */
                     synchronized (myaudioprocessor) {
                         myaudioprocessor.notify();
                     }
                     
                     readnewbuf = true;
                 }
 
                 if (myaudioprocessor == null || numSamples > 0 && myaudioprocessor != null && !myaudioprocessor.isWaiting()
                         && !myaudioprocessor.isBusy()) {
                     /* if there are samples and not waiting */
                     switchbuffer = !switchbuffer;
                     readnewbuf = true;
                    if (myaudioprocessor == null || !myaudioprocessor.isBusy()) {
                         startNextProcessorThread();
                     } // end if thread not alive
                 } // end if samples and thread not waiting
             }
         } catch (Exception e) {
             e.printStackTrace();
             ErrorReporter.getInstance().handleException(e);
         }
         Log.d("MicSerialListener", "LISTENER ENDED");
         // LogFile.MakeLog("LISTENER ended");
     }
 
     private void startNextProcessorThread() {
         if (switchbuffer) {
             myaudioprocessor =
                     new AudioProcessor(myByteReceiver,
                                        numSamples,
                                        sample_buf1,
                                        sample_buf2,
                                        trans_buf,
                                        movingsum,
                                        movingbuf,
                                        sloc,
                                        byteInDec);
         } else {
             myaudioprocessor =
                     new AudioProcessor(myByteReceiver,
                                        numSamples,
                                        sample_buf2,
                                        sample_buf1,
                                        trans_buf,
                                        movingsum,
                                        movingbuf,
                                        sloc,
                                        byteInDec);
         }
 
         synchronized (myaudioprocessor) {
            executor.execute(myaudioprocessor);
         }
     }
 
     boolean AutoGainControl() {
         boolean found = false;
         int startpos = SAMPLE_LENGTH;
         int arraypos = 0;
 
         while (startpos < numSamples - 1 && Math.abs(sample_buf1[startpos] - sample_buf1[startpos + 1]) < THRESHOLD) {
             startpos++;
         }
 
         if (startpos < numSamples - 1 && startpos >= SAMPLE_LENGTH && startpos < SAMPLE_LENGTH * 2) {
             startpos -= SAMPLE_LENGTH;
             while (Math.abs(sample_buf1[startpos] - sample_buf1[startpos + 1]) < THRESHOLD) {
                 // && startpos < numSamples-1)
                 startpos++;
             }
         }
 
         startpos += BEGIN_OFFSET;
 
         if (startpos < numSamples - (SAMPLE_LENGTH - BEGIN_OFFSET)) {
             Log.d(TAG, "Found a sample...");
 
             movingsum[0] = 0;
             for (int i = startpos; i < startpos + 9; i++) {
                 movingbuf[i - startpos] = sample_buf1[i];
                 movingsum[0] += sample_buf1[i];
             }
             SIGNAL_MAX_SUM = 0;
             for (int i = startpos + 9; i < startpos + PREAMBLE - BEGIN_OFFSET + 3 * (TRANSMISSION_LENGTH + BIT_OFFSET); i++) {
                 movingsum[1] = movingsum[0] - movingbuf[arraypos];
                 movingsum[1] += sample_buf1[i];
                 movingbuf[arraypos] = sample_buf1[i];
                 arraypos++;
                 if (arraypos == 9) {
                     arraypos = 0;
                 }
 
                 int temp = Math.abs(movingsum[0] - movingsum[1]);
                 if (temp > SIGNAL_MAX_SUM) {
                     SIGNAL_MAX_SUM = temp;
                 }
 
                 test_buf[i - startpos - 9] = sample_buf1[i];
                 movingsum2[i - startpos - 9] = movingsum[0];
                 movingsum[0] = movingsum[1];
             }
 
             SIGNAL_MAX_SUM /= 1.375;
             findSample(startpos);
 
             int[] byteInDec = new int[3];
             for (int n = 0; n < 3; n++) {
                 if (sampleloc[n] != 0) {
                     arraypos = 0;
                     movingsum[0] = 0;
                     for (int i = 0; i < 9; i++) {
                         movingbuf[i] = sample_buf1[i + sampleloc[n]];
                         movingsum[0] += sample_buf1[i + sampleloc[n]];
                     }
                     for (int i = 9; i < MicSerialListener.TRANSMISSION_LENGTH; i++) {
                         movingsum[i] = movingsum[i - 1] - movingbuf[arraypos];
                         movingsum[i] += sample_buf1[i + sampleloc[n]];
                         movingbuf[arraypos] = sample_buf1[i + sampleloc[n]];
                         arraypos++;
                         if (arraypos == 9) {
                             arraypos = 0;
                         }
                     }
 
                     boolean isinphase = true, switchphase = true; // we start
                     // out with
                     // a phase
                     // shift
                     int bitnum = 0;
 
                     for (int i = MicSerialListener.FRAMES_PER_BIT + 1; i < MicSerialListener.TRANSMISSION_LENGTH; i++) {
                         if (isPhase(movingsum[i - 1], movingsum[i], SIGNAL_MAX_SUM) && switchphase) {
                             isinphase = !isinphase;
                             switchphase = false; // already switched
                         }
 
                         if (i % MicSerialListener.FRAMES_PER_BIT == 0) {
                             if (!isinphase) {
                                 byteInDec[n] |= 0x1 << bitnum;
                             }
                             bitnum++;
                             switchphase = true; // reached a bit, can now switch
                             // again if phase shifts
                         }
                     }
 
                     Log.d(TAG, "TRANSMISSION[" + n + "]: " + "0x" + Integer.toHexString(byteInDec[n]));
                     // LogFile.MakeLog("TRANSMISSION[" + n + "]: " + "0x"+
                     // Integer.toHexString(byteInDec[n]));
                 }
             }
 
             if (byteInDec[0] == 0x27 && byteInDec[1] == 0x27 || byteInDec[1] == 0x27 && byteInDec[2] == 0x27
                     || byteInDec[0] == 0x27 && byteInDec[2] == 0x27) {
                 found = true;
             }
         }// end if found a start position
 
         return found;
     }
 
     private void findSample(int startpos) {
         int arraypos = 0;
         int numsampleloc = 0;
         movingsum[0] = 0;
         for (int i = startpos; i < startpos + 9; i++) {
             movingbuf[i - startpos] = sample_buf1[i];
             movingsum[0] += sample_buf1[i];
         }
 
         for (int i = startpos + 9; i < startpos + SAMPLE_LENGTH - MicSerialListener.BIT_OFFSET; i++) {
             movingsum[1] = movingsum[0] - movingbuf[arraypos];
             movingsum[1] += sample_buf1[i];
             movingbuf[arraypos] = sample_buf1[i];
             arraypos++;
             if (arraypos == 9) {
                 arraypos = 0;
             }
 
             if (isPhase(movingsum[0], movingsum[1], SIGNAL_MAX_SUM)) {
                 sampleloc[numsampleloc++] = i - 5;
                 i += TRANSMISSION_LENGTH + BIT_OFFSET + FRAMES_PER_BIT + 1; // next
                 // transmission
                 sampleloc[numsampleloc++] = i;
                 i += TRANSMISSION_LENGTH + BIT_OFFSET + FRAMES_PER_BIT + 1; // next
                 // transmission
                 sampleloc[numsampleloc++] = i;
                 return;
             }
 
             movingsum[0] = movingsum[1];
         }
     }
 
     public void onDestroy() {
         running = false;
         if (inStream != null) {
             inStream.release();
         }
         inStream = null;
         Log.d(TAG, "STOPPED");
     }
 
    
     public static boolean isPhase(int sum1, int sum2, int SIG_MAX_SUM) {
         if (Math.abs(sum1 - sum2) > SIG_MAX_SUM) {
             return true;
         } else {
             return false;
         }
     }
 }
