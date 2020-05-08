 /***************************************************************************
  * Copyright (c) 2011, 2012 by Sonr Labs Inc (http://www.sonrlabs.com)
  * Questions/Comments: joe@sonrlabs.com
  * 
  *You can redistribute this program and/or modify it under the terms of the GNU General Public License v. 2.0 as published by the Free Software Foundation
  *This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  *
  **************************************************************************/
 
 package com.sonrlabs.test.sonr.signal;
 
 import com.sonrlabs.test.sonr.AudioProcessorQueue;
 
 /**
  * 
  * This class provides support for audio processing functionality that's
  * common to both the initial connection to the dock and the processing of
  * signals once that connection is established.
  * 
  * <p>
  *  Sonr uses differential phase shift keying for modulation and HDLC Framing.
  *  
  * @see <a href="http://en.wikipedia.org/wiki/Phase-shift_keying#Differential_phase-shift_keying_ .28DPSK.29">Phase Shift Keying</a>
  * @see <a href="http://en.wikipedia.org/wiki/High-Level_Data_Link_Control">HDLC Framing</a>
  */
 abstract class SignalConstructor
       implements AudioSupportConstants {
 
    // how many repetitions in the transmission required for valid data
    private static final int MIN_MATCHES = 2;
    
    /**
     * Used as the threshold to detect phase changes. It's computed by
     * {@link #computeSignalMax()}.
     */
    private float signalMaxSum;
    
    // TODO: Document these
    private final int[] movingbuf = new int[MOVING_SIZE];
    private final int[] movingsum = new int[TRANSMISSION_LENGTH];
    
    /**
     * Stores whatever signal was found in each sample.  The signal
     * value for a given index is computed by {@link #constructSignal(int index)}.
     */
    private final int[] signals = new int[SAMPLES_PER_BUFFER];
    
    /**
     * Look for at least {@value #MIN_MATCHES} matches of the values in {@link #signals}.
     * If found, send that matching value off to the processor.
     */
    void processSignalIfMatch() {
       for (int i = 0; i <= signals.length-MIN_MATCHES; i++) {
          int baseSignal = signals[i];
          if (baseSignal != 0 && baseSignal != BOUNDARY) {
             int matchCount = 1;
             for (int j = i+1; j < signals.length; j++) {
               if (baseSignal == signals[j] && ++matchCount >= MIN_MATCHES) {
                   AudioProcessorQueue.processAction(baseSignal);
                   return;
                }
             }
          }
       }
    }
 
    int countBoundSignals() {
       int matchCount = 0;
       for (int value : signals) {
          if (value == BOUNDARY) {
             ++matchCount;
          }
       }
       return matchCount;
    }
 
    boolean isPhaseChange(int movingSumIndex) {
       int sum1 = movingsum[movingSumIndex];
       int sum2 = movingsum[movingSumIndex + 1];
       return Math.abs(sum1 - sum2) > signalMaxSum;
    }
 
    int findSample(int startpos, short[] samples, int numsampleloc, int[] sampleStartIndices) {
       movingsum[0] = 0;
       int start = startpos + MOVING_SIZE;
       for (int i = startpos; i < start; i++) {
          movingbuf[i - startpos] = samples[i];
          movingsum[0] += samples[i];
       }
       int arraypos = 0;
       int end = startpos + SAMPLE_LENGTH - BIT_OFFSET;
       for (int i = start; i < end; i++) {
          movingsum[1] = movingsum[0] - movingbuf[arraypos];
          movingsum[1] += samples[i];
          movingbuf[arraypos] = samples[i];
          arraypos++;
          if (arraypos == MOVING_SIZE) {
             arraypos = 0;
          }
 
          if (isPhaseChange(0)) {
             sampleStartIndices[numsampleloc++] = i - 5;
             if (numsampleloc >= SAMPLES_PER_BUFFER) {
                break;
             }
             // next transmission
             i += TRANSMISSION_LENGTH + BIT_OFFSET + FRAMES_PER_BIT + 1;
             sampleStartIndices[numsampleloc++] = i;
             // next transmission
             i += TRANSMISSION_LENGTH + BIT_OFFSET + FRAMES_PER_BIT + 1;
             sampleStartIndices[numsampleloc] = i;
             break;
          } else {
             movingsum[0] = movingsum[1];
          }
       }
       return numsampleloc;
    }
 /* It seems to me we should compute the amplitude of the signal 
  * (which I presume is what we're doing here) By finding the two extreme points 
  * along the curve and getting the difference.
  */
    void computeSignalMax(short[] samples, int startpos) {
        
        signalMaxSum = 0;
 
       int endpos = startpos + PREAMBLE - BEGIN_OFFSET + SAMPLES_PER_BUFFER * (TRANSMISSION_LENGTH + BIT_OFFSET);
       for (int i = startpos + MOVING_SIZE; i < endpos; i++) {
          int max = 0;
          int min = 0;
          if (samples[i] > max) {
             max = samples[i];
          }
          if (samples[i] < min){
             min = samples[i];
          }
          int temp = Math.abs(max - min);
          if (temp > signalMaxSum) {
             signalMaxSum = temp;
          
          
          }
 
 
       }
    
   
       
       /* 
        * My guess is that the phase shifts don't always occur at the max amplitude points and thus we want to 
        * provide a bit of a cushion if the signal and phase shifts slip a bit out of phase
        * who knows?  1.0 and 3.0 seem to work slightly worse on EVO 3D than 1.5 in terms of missed button presses.
       */
       signalMaxSum /= AMPLITUDE_THRESHOLD;
    }
 
    void constructSignal(short[] samples, int[] sampleStartIndices) {
       for (int signalIndex = 0; signalIndex < SAMPLES_PER_BUFFER; signalIndex++) {
          if (sampleStartIndices[signalIndex] != 0) {
             int index = 0;
             movingsum[0] = 0;
             for (int i = 0; i < MOVING_SIZE; i++) {
                short value = samples[i + sampleStartIndices[signalIndex]];
                movingbuf[i] = value;
                movingsum[0] += value;
             }
             for (int i = MOVING_SIZE; i < TRANSMISSION_LENGTH; i++) {
                movingsum[i] = movingsum[i - 1] - movingbuf[index];
                short value = samples[i + sampleStartIndices[signalIndex]];
                movingsum[i] += value;
                movingbuf[index] = value;
                index++;
                if (index == MOVING_SIZE) {
                   index = 0;
                }
             }
 
             constructSignal(signalIndex);
          }
       }
    }
 
    private void constructSignal(int signalIndex) {
       /* we start out with a phase shift and 0 signal */
       boolean inphase = true, switchphase = true;
       signals[signalIndex] = 0;
 
       for (int i = FRAMES_PER_BIT + 1, bitnum = 0; i < TRANSMISSION_LENGTH; i++) {
          if (switchphase && isPhaseChange(i - 1)) {
             inphase = !inphase;
             switchphase = false;
          }
 
          boolean atFrameBoundary = i % FRAMES_PER_BIT == 0;
          if (atFrameBoundary) {
             if (!inphase) {
                signals[signalIndex] |= 0x1 << bitnum;
             }
             bitnum++;
             /* reached a bit, can now switch */
             switchphase = true;
          }
       }
    }
 }
