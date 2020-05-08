 package com.sonrlabs.test.sonr;
 
 import org.acra.ErrorReporter;
 
 public class AudioProcessor
       implements IAudioProcessor {
 
//   private static final String TAG = "SONR audio processor";
 
    private static final int BUFFER_AVAILABLE = 1;
    
    private static boolean PreambleIsCutOff = false;
    private static int Preamble_Offset = 0;
 
    private final ISampleBuffer sampleBuffer1;
    private final ISampleBuffer sampleBuffer2;
    private final short[] sample_buf, sample_buf2;
    private final int[][] trans_buf;
    private final int[][] sampleloc;
    private final int[] movingsum;
    private final int[] movingbuf;
    private final int[] byteInDec;
    private final int numSamples;
    private final MicSerialListener listener;
    private final Object lock;
    
    private int samplelocsize = 0;
    private int buffer;
    private boolean waiting = false;
    private boolean busy = false;
 
 
 
    AudioProcessor(MicSerialListener listener, Object lock, int numsamples, ISampleBuffer thesamples, ISampleBuffer thesamples2) {
       this.listener = listener;
       this.lock = lock;
       this.sampleBuffer1 = thesamples;
       this.sampleBuffer2 = thesamples2;
       numSamples = numsamples;
       sample_buf = thesamples.getArray();
       sample_buf2 = thesamples2.getArray();
       trans_buf = listener.trans_buf;
       buffer = 0;
       movingbuf = listener.movingbuf;
       movingsum = listener.movingsum;
       sampleloc = listener.sloc;
       byteInDec = listener.byteInDec;
    }
 
    @Override
    public void run() {
       try {
          /* Log.d(TAG, "AUDIO PROCESSOR BEGIN"); */
          busy = true;
          findSample();
          if (samplelocsize > 0) {
             processSample();
          }
          /* Log.d(TAG, "AUDIO PROCESSOR END"); */
       } catch (Exception e) {
          e.printStackTrace();
          ErrorReporter.getInstance().handleException(e);
       } finally {
          sampleBuffer1.release();
          sampleBuffer2.release();
          busy = false;
       }
    }
 
    public boolean isWaiting() {
       return waiting;
    }
 
    public boolean isBusy() {
       return busy;
    }
 
    private void processSample() {
       /* copy transmission down because the buffer could get overwritten */
       int count2 = 0;
       for (int j = 0; j < samplelocsize; j++) {
          for (int i = 0; i < MicSerialListener.TRANSMISSION_LENGTH; i++) {
             if (sampleloc[j / 3][j % 3] + i < numSamples) {
                trans_buf[j][i] = sample_buf[sampleloc[j / 3][j % 3] + i];
             } else if (buffer >= BUFFER_AVAILABLE && count2 < numSamples) {
                /* circular "queue" */
                trans_buf[j][i] = sample_buf2[count2++];
             } else {
                /* no extra buffer, wait */
               try {
                  synchronized (lock) {
                     /* Log.d(TAG, "WAITING"); */
                     waiting = true;
                     /* longest possible wait time */
                     lock.wait(300);
                   }
               } catch (InterruptedException e) {
                  // same as notify
               } finally {
                  /* redo */
                  i--;
                  buffer++;
                  waiting = false;
                  
                }
             }
          }
       }
 //         if (count2 != 0) {
 //            Log.d(TAG, "CUT OFF");
 
       for (int s = 0; s < samplelocsize; s++) {
          int arraypos = 0;
          movingsum[0] = 0;
          for (int i = 0; i < 9; i++) {
             movingbuf[i] = trans_buf[s][i];
             movingsum[0] += trans_buf[s][i];
          }
 
          for (int i = 9; i < MicSerialListener.TRANSMISSION_LENGTH; i++) {
             movingsum[i] = movingsum[i - 1] - movingbuf[arraypos];
             movingsum[i] += trans_buf[s][i];
             movingbuf[arraypos] = trans_buf[s][i];
             arraypos++;
             if (arraypos == 9) {
                arraypos = 0;
             }
          }
 
          /* we start out with a phase shift */
          boolean isinphase = true, switchphase = true; 
          int bitnum = 0;
          byteInDec[s] = 0;
 
          for (int i = MicSerialListener.FRAMES_PER_BIT + 1; i < MicSerialListener.TRANSMISSION_LENGTH; i++) {
             if (MicSerialListener.isPhase(movingsum[i - 1], movingsum[i], MicSerialListener.SIGNAL_MAX_SUM) && switchphase) {
                isinphase = !isinphase;
                /* already switched */
                switchphase = false; 
             }
 
             if (i % MicSerialListener.FRAMES_PER_BIT == 0) {
                if (!isinphase) {
                   /* i/MicSerialListener.FRAMES_PER_BIT-1 */
                   byteInDec[s] |= 0x1 << bitnum; 
                }
                bitnum++;
                /* reached a bit, can now switch */
                switchphase = true; 
             }
          }
 
 //         Log.d(TAG, "TRANSMISSION[" + s + "]: " + "0x"+ Integer.toHexString(byteInDec[s]));
 
 //         if(byteInDec[s] != 0x27 || samplelocsize < 3) {
 //            Log.d(TAG, "--------------");
 //         }
       }
 
       if (samplelocsize > 1) {
          /* 2 or more */
          for (int i = 0; i < samplelocsize; i += 3) { 
             /*
              *  receive byte using  best two out of three.
              */
             if ((byteInDec[i] == byteInDec[i + 1] || byteInDec[i] == byteInDec[i + 2]) && byteInDec[i] != 0x27) {
                listener.processAction(byteInDec[i]);
             } else if (byteInDec[i + 1] == byteInDec[i + 2] && byteInDec[i + 1] != 0x27) {
                listener.processAction(byteInDec[i + 1]);
             }
          }
       }
    }
 
    private void findSample() {
       int count = 0;
       int arraypos = 0;
       int numfoundsamples = 0;
 
       if (PreambleIsCutOff) {
          sampleloc[numfoundsamples++][0] = Preamble_Offset;
          PreambleIsCutOff = false;
          count += MicSerialListener.SAMPLE_LENGTH + MicSerialListener.END_OFFSET;
 //         Log.d(TAG, "PREAMBLE CUT OFF BEGIN");
       } else {
          count = MicSerialListener.SAMPLE_LENGTH;
       }
 
       while (count < numSamples - 1) {
          /* 1. find where the PSK signals begin */
          if (Math.abs(sample_buf[count] - sample_buf[count + 1]) > MicSerialListener.THRESHOLD) {
             if (count >= MicSerialListener.SAMPLE_LENGTH && count < MicSerialListener.SAMPLE_LENGTH * 2 && numfoundsamples == 0) {
                count -= MicSerialListener.SAMPLE_LENGTH;
                while (Math.abs(sample_buf[count] - sample_buf[count + 1]) < MicSerialListener.THRESHOLD) {
                   count++;
                }
             }
             if (count + MicSerialListener.PREAMBLE >= numSamples) {
 //               Log.d(TAG, "PREAMBLE CUT OFF");
                if (count + MicSerialListener.BEGIN_OFFSET <= numSamples) {
                   Preamble_Offset = 0;
                } else {
                   Preamble_Offset = count + MicSerialListener.BEGIN_OFFSET - numSamples;
                }
                PreambleIsCutOff = true;
                break;
             } else { 
                /* preamble not cut off */
                sampleloc[numfoundsamples++][0] = count + MicSerialListener.BEGIN_OFFSET;
                if (numfoundsamples >= MicSerialListener.MAX_TRANSMISSIONS) {
                   break;
                }
                count += MicSerialListener.SAMPLE_LENGTH + MicSerialListener.END_OFFSET;
             }
          }
          count++;
       }
 
       if (numfoundsamples > 0) {
          AGC();
       }
 
       int numsampleloc = 0;
       for (int n = 0; n < numfoundsamples; n++) {
          /*
           * 2. cycle through the found PSK locations and find the specific start points of individual transmissions
           */
          arraypos = 0;
          movingsum[0] = 0;
          for (int i = sampleloc[n][0]; i < sampleloc[n][0] + 9; i++) {
             movingbuf[i - sampleloc[n][0]] = sample_buf[i];
             movingsum[0] += sample_buf[i];
          }
 
          for (int i = sampleloc[n][0] + 9; i < sampleloc[n][0] + MicSerialListener.SAMPLE_LENGTH - MicSerialListener.BIT_OFFSET; i++) {
             movingsum[1] = movingsum[0] - movingbuf[arraypos];
             movingsum[1] += sample_buf[i];
             movingbuf[arraypos] = sample_buf[i];
             arraypos++;
             if (arraypos == 9) {
                arraypos = 0;
             }
 
             if (MicSerialListener.isPhase(movingsum[0], movingsum[1], MicSerialListener.SIGNAL_MAX_SUM)) {
                sampleloc[numsampleloc / 3][numsampleloc % 3] = i - 5;
 
                samplelocsize = ++numsampleloc;
                if (numsampleloc >= MicSerialListener.MAX_TRANSMISSIONS * 3) {
                   return;
                }
                /* next transmission */
                i += MicSerialListener.TRANSMISSION_LENGTH + MicSerialListener.BIT_OFFSET + MicSerialListener.FRAMES_PER_BIT + 1; 
                sampleloc[numsampleloc / 3][numsampleloc % 3] = i;
                samplelocsize = ++numsampleloc;
                /* next transmission */
                i += MicSerialListener.TRANSMISSION_LENGTH + MicSerialListener.BIT_OFFSET + MicSerialListener.FRAMES_PER_BIT + 1;
                sampleloc[numsampleloc / 3][numsampleloc % 3] = i;
                samplelocsize = ++numsampleloc;
 
                /*
                 * finished with this signal, go back to search through next
                 * signal
                 */
                break; 
 
                /*
                 * i += MicSerialListener.TRANSMISSION_LENGTH +
                 * MicSerialListener.BIT_OFFSET +
                 * MicSerialListener.FRAMES_PER_BIT +
                 * MicSerialListener.BEGIN_OFFSET;
                 * 
                 * movingsum[0] = 0; //re-set up the variables to continue
                 * searching for signals arraypos = 0; for(int t = i; t < i + 9;
                 * t++) { movingbuf[t - i] = sample_buf[t]; movingsum[0] +=
                 * sample_buf[t]; } i += 4;
                 */
             } else {
                movingsum[0] = movingsum[1];
             }
          }
       } // end loop through numsamplesfound
    }
 
    private void AGC() {
       MicSerialListener.SIGNAL_MAX_SUM = 0;
       int arraypos = 0;
       int startpos = sampleloc[0][0];
       movingsum[0] = 0;
       for (int i = startpos; i < startpos + 9; i++) {
          movingbuf[i - startpos] = sample_buf[i];
          movingsum[0] += sample_buf[i];
       }
       for (int i = startpos + 9; i < startpos + MicSerialListener.PREAMBLE - MicSerialListener.BEGIN_OFFSET + 3
             * (MicSerialListener.TRANSMISSION_LENGTH + MicSerialListener.BIT_OFFSET); i++) {
          movingsum[1] = movingsum[0] - movingbuf[arraypos];
          movingsum[1] += sample_buf[i];
          movingbuf[arraypos] = sample_buf[i];
          arraypos++;
          if (arraypos == 9) {
             arraypos = 0;
          }
 
          int temp = Math.abs(movingsum[0] - movingsum[1]);
          if (temp > MicSerialListener.SIGNAL_MAX_SUM) {
             MicSerialListener.SIGNAL_MAX_SUM = temp;
          }
 
          movingsum[0] = movingsum[1];
       }
 
       MicSerialListener.SIGNAL_MAX_SUM /= 1.375;
    }
 }
