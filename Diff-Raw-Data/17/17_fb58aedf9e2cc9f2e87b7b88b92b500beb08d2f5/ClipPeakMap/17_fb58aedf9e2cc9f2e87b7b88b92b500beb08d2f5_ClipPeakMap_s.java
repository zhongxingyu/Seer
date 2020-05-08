 package org.songbird;
 
 import net.bluecow.spectro.Clip;
 import net.bluecow.spectro.Frame;
 
 import java.util.HashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ian
  * Date: 28/04/11
  * Time: 14:37
  * To change this template use File | Settings | File Templates.
  */
 public class ClipPeakMap {
     private HashMap<Integer,Double> peaks;
     private Clip clip;
 
     private int frameSize;
     private int numOfFrames;
 
     private static final int MAX_NEAREST_PEAK_DIST = 10;
 
     public ClipPeakMap(Clip clip, int lowPassFrequencyThreshold, int highPassFrequencyThreshold) {
         this(clip.getFrameFreqSamples(), clip.getFrameCount());
         this.clip = clip;
 
         new ClipPeakFinder() {
             @Override
             protected void handlePeak(int frame, int frequency, double amplitude) {
                 peaks.put(getLookupKey(frame, frequency), amplitude);
             }
         }.findPeaks(clip, lowPassFrequencyThreshold, highPassFrequencyThreshold);
     }
 
     protected ClipPeakMap(int frameSize, int numOfFrames) {
         peaks = new HashMap<Integer,Double>();
         this.frameSize = frameSize;
         this.numOfFrames = numOfFrames;
     }
 
     public int getNearestPeakDistance(int frame, int frequency) {
         boolean found = false;
         int distance = 0;
 
         for(; distance < MAX_NEAREST_PEAK_DIST; distance++) {
 
             final int startFrame = Math.max(frame - distance, 0);
             final int endFrame = Math.min(frame + distance, numOfFrames);
 
             for(int searchFrame = startFrame; searchFrame <= endFrame; searchFrame++) {
                if(isPeak(searchFrame, frequency + distance) || isPeak(searchFrame, frequency - distance)) {
                     found = true;
                     break;
                 }
             }
 
             if(found) {
                 break;
             }
         }
 
         return distance;
     }
 
     public double getAmplitude(int frame, int frequency) {
         return peaks.get(getLookupKey(frame, frequency));
     }
 
     public boolean isPeak(int frame, int frequency) {
         return peaks.containsKey(getLookupKey(frame, frequency));
     }
 
     private Integer getLookupKey(int frame, int frequency) {
         return frame * frameSize + frequency;
     }
 
     public Clip getClip() {
         return clip;
     }
 }
