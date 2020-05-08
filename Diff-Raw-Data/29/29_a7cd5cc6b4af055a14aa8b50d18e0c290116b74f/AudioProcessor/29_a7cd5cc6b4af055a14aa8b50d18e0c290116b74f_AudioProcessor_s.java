 package com.sonrlabs.test.sonr;
 
 import org.acra.ErrorReporter;
 
 
 public class AudioProcessor
        extends Thread {
 
     // private static final String TAG = "SONR audio processor";
 
     private static boolean PreambleIsCutOff = false;
 
     private static int Preamble_Offset = 0;
 
     private ByteReceiver myByteReceiver;
 
     private short[] sample_buf, sample_buf2;
     private int[][] trans_buf;
     private int numSamples;
     private int[][] sampleloc;
     private int[] movingsum;
     private int[] movingbuf;
     private int[] byteInDec;
 
     private int samplelocsize = 0;
     private int buffer;
     private boolean waiting = false;
     private boolean busy = false;
 
     private static final int BUFFER_AVAILABLE = 1;
 
     // private int SIGNAL_MAX_SUM;
 
     AudioProcessor(ByteReceiver tempByteReceiver, int numsamples, short[] thesamples, short[] thesamples2, int[][] thetrans_buf,
                    int[] movsum, int[] movbuf, int[][] sloc, int[] b_in_dec) {
         /*
          * pass all of these values by reference so that memory is only
          * allocated once in MicSerialListener
          */
         myByteReceiver = tempByteReceiver;
         numSamples = numsamples;
         sample_buf = thesamples;
         sample_buf2 = thesamples2;
         trans_buf = thetrans_buf;
         buffer = 0;
         movingbuf = movbuf;
         movingsum = movsum;
         sampleloc = sloc;
         byteInDec = b_in_dec;
     }
 
     @Override
     public void run() {
         try {
             /* Log.d(TAG, "AUDIO PROCESSOR BEGIN"); */
             busy = true;
             processSample();
             /* Log.d(TAG, "AUDIO PROCESSOR END"); */
         } catch (Exception e) {
             e.printStackTrace();
             ErrorReporter.getInstance().handleException(e);
         } finally {
             busy = false;
         }
     }
 
    @Override
    public void destroy() {

    }

     boolean isWaiting() {
         return waiting;
     }
 
     boolean isBusy() {
         return busy;
     }
 
     private void processSample() {
         findSample();
 
         if (samplelocsize > 0) {
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
                             synchronized (this) {
                                 /* Log.d(TAG, "WAITING"); */
                                 waiting = true;
                                 /* longest possible wait time */
                                 this.wait(300);
                                 buffer++;
                                 waiting = false;
                             }
                             /* redo */
                             i--;
                         } catch (InterruptedException e) {
                         }
                     }
                 }
             }
             // if(count2 != 0)
             // Log.d(TAG, "CUT OFF");
         } else {
             return; // nothing found
         }
 
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
 
             boolean isinphase = true, switchphase = true; // we start out with a
             // phase shift
             int bitnum = 0;
             byteInDec[s] = 0;
 
             for (int i = MicSerialListener.FRAMES_PER_BIT + 1; i < MicSerialListener.TRANSMISSION_LENGTH; i++) {
                 if (MicSerialListener.isPhase(movingsum[i - 1], movingsum[i], MicSerialListener.SIGNAL_MAX_SUM) && switchphase) {
                     isinphase = !isinphase;
                     switchphase = false; // already switched
                 }
 
                 if (i % MicSerialListener.FRAMES_PER_BIT == 0) {
                     if (!isinphase) {
                         byteInDec[s] |= 0x1 << bitnum; // i/MicSerialListener.FRAMES_PER_BIT-1
                     }
                     bitnum++;
                     switchphase = true; // reached a bit, can now switch
                 }
             }
 
             // Log.d(TAG, "TRANSMISSION[" + s + "]: " + "0x"+
             // Integer.toHexString(byteInDec[s]));
 
             // if(byteInDec[s] != 0x27 || samplelocsize < 3)
             // Log.d(TAG, "--------------");
         }
 
         if (samplelocsize > 1) { // 2 or more
             for (int i = 0; i < samplelocsize; i += 3) { // receive byte using
                 // best two out of
                 // three
                 if ((byteInDec[i] == byteInDec[i + 1] || byteInDec[i] == byteInDec[i + 2]) && byteInDec[i] != 0x27) {
                     myByteReceiver.receiveByte(byteInDec[i]); // RECEIVED THE
                     // BYTE!
                 } else if (byteInDec[i + 1] == byteInDec[i + 2] && byteInDec[i + 1] != 0x27) {
                     myByteReceiver.receiveByte(byteInDec[i + 1]);
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
             // Log.d(TAG, "PREAMBLE CUT OFF BEGIN");
         } else {
             count = MicSerialListener.SAMPLE_LENGTH;
         }
 
         while (count < numSamples - 1) { // /1. find where the PSK signals begin
             if (Math.abs(sample_buf[count] - sample_buf[count + 1]) > MicSerialListener.THRESHOLD) {
                 if (count >= MicSerialListener.SAMPLE_LENGTH && count < MicSerialListener.SAMPLE_LENGTH * 2 && numfoundsamples == 0) {
                     count -= MicSerialListener.SAMPLE_LENGTH;
                     while (Math.abs(sample_buf[count] - sample_buf[count + 1]) < MicSerialListener.THRESHOLD) {
                         count++;
                     }
                 }
                 if (count + MicSerialListener.PREAMBLE >= numSamples) {
                     // Log.d(TAG, "PREAMBLE CUT OFF");
                     if (count + MicSerialListener.BEGIN_OFFSET <= numSamples) {
                         Preamble_Offset = 0;
                     } else {
                         Preamble_Offset = count + MicSerialListener.BEGIN_OFFSET - numSamples;
                     }
                     PreambleIsCutOff = true;
                     break;
                 } else { // preamble not cut off
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
         for (int n = 0; n < numfoundsamples; n++) { // //2. cycle through the
             // found PSK locations and
             // find the specific start
             // points of individual
             // transmissions
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
                     i +=
                             MicSerialListener.TRANSMISSION_LENGTH + MicSerialListener.BIT_OFFSET + MicSerialListener.FRAMES_PER_BIT
                                     + 1; // next
                     // transmission
                     sampleloc[numsampleloc / 3][numsampleloc % 3] = i;
                     samplelocsize = ++numsampleloc;
                     i +=
                             MicSerialListener.TRANSMISSION_LENGTH + MicSerialListener.BIT_OFFSET + MicSerialListener.FRAMES_PER_BIT
                                     + 1; // next
                     // transmission
                     sampleloc[numsampleloc / 3][numsampleloc % 3] = i;
                     samplelocsize = ++numsampleloc;
 
                     break; // finished with this signal, go back to search
                     // through next signal
 
                     /*
                      * i += MicSerialListener.TRANSMISSION_LENGTH +
                      * MicSerialListener.BIT_OFFSET +
                      * MicSerialListener.FRAMES_PER_BIT +
                      * MicSerialListener.BEGIN_OFFSET;
                      * 
                      * movingsum[0] = 0; //re-set up the variables to continue
                      * searching for signals arraypos = 0; for(int t = i; t < i
                      * + 9; t++) { movingbuf[t - i] = sample_buf[t];
                      * movingsum[0] += sample_buf[t]; } i += 4;
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
