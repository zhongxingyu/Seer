 package edu.cmu.pandaa.header;
 
 public class DistanceHeader extends StreamHeader {
   public int rollingWindow = 1;
   public String[] deviceIds; // the devices providing feature/impulse data for distance calculation
 
   public DistanceHeader(String id, long startTime, int frameTime, String[] ids) {
     this(id, startTime, frameTime, 1, ids);
   }
 
   public DistanceHeader(String id, long startTime, int frameTime, int rolling, String[] ids) {
     super(id, startTime, frameTime);
     rollingWindow = rolling;
     deviceIds = ids;
   }
 
   public class DistanceFrame extends StreamFrame {
    public double[] peakDeltas; // time delta for each peak (micro-seconds)
     public double[] peakMagnitudes; // magnitude information for each peak
 
     DistanceFrame(double[] deltas, double[] magnitudes) {
       peakDeltas = deltas;
       peakMagnitudes = magnitudes;
     }
   }
 
   public DistanceFrame makeFrame(double[] deltas, double[] magnitudes) {
     return new DistanceFrame(deltas, magnitudes);
   }
 }
