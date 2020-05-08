 package edu.cmu.pandaa.module;
 
 import edu.cmu.pandaa.header.DistanceHeader;
 import edu.cmu.pandaa.header.ImpulseHeader;
 import edu.cmu.pandaa.header.ImpulseHeader.ImpulseFrame;
 import edu.cmu.pandaa.header.MultiHeader;
 import edu.cmu.pandaa.header.MultiHeader.MultiFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.*;
 
 import java.nio.channels.IllegalBlockingModeException;
 import java.util.*;
 
 public class TDOACrossModule implements StreamModule {
   DistanceHeader header;
   final double speedOfSound = 340.3;   // m/s at sea level
 
   // This constant is used/necessary for capturing the time synchronization difference between devices,
   // so it should be set large enough to encompass those differences.
   final double startingWindowMs = 50;
 
   // This constant is about capturing the distance difference between devices, so it should be set to
   // encompass the maximum plausible distance that we should be measuing.
   final double endingWindowMs = 1000.0 / speedOfSound * 5;
 
   private int lastSeqNum = -1;
   private double weightWindowUs = startingWindowMs * 1000;
   private double weightWindowWeight = 100.0;
 
   CalibrationManager cf;
   ImpulseFrame savedFrames[] = new ImpulseFrame[2];
 
   public StreamHeader init(StreamHeader inHeader) throws Exception {
     MultiHeader multiHeader = (MultiHeader) inHeader;
     if (!(multiHeader.getOne() instanceof ImpulseHeader)) {
       throw new IllegalArgumentException("Input multiheader should contain ImpulseHeaders");
     }
 
     StreamHeader[] impulseHeaders = multiHeader.getHeaders();
     if (impulseHeaders.length != 2) {
       throw new IllegalArgumentException("Input multiheader should contain two elements");
     }
 
     if (impulseHeaders[0].frameTime != impulseHeaders[1].frameTime) {
       throw new IllegalArgumentException("Frame duration must be equal for both input frames");
     }
 
     String[] deviceIds = new String[] {impulseHeaders[0].id, impulseHeaders[1].id};
     if (deviceIds[0].compareTo(deviceIds[1]) > 0) {
       throw new RuntimeException("Frames out of order init");
     }
 
     header = new DistanceHeader(inHeader.id, impulseHeaders[0].startTime, impulseHeaders[0].frameTime, deviceIds);
 
     return header;
   }
 
   private static class Peak implements Comparable<Peak> {
     final int ai, bi;
     final int ao, bo;
     final double weight;
     public Peak(int ai, int ao, int bi, int bo, double weight) {
       this.ai = ai;
       this.bi = bi;
       this.ao = ao;
       this.bo = bo;
       this.weight = weight;
     }
 
     public int compareTo(Peak other) {
       return Double.compare(other.weight, weight);
     }
   }
 
   public void setCalibrationManager(CalibrationManager calibrationManager) throws Exception {
     cf = calibrationManager;
   }
 
   private double calcWeight(int ao, int am, int bo, int bm, double calibration) {
     double dist = Math.abs(ao - bo); // difference in us
     if (dist > weightWindowUs)
       return 0;
     double mag = (double) am* (double) bm;
     return mag *(weightWindowUs - dist)/ weightWindowUs;
   }
 
   private ImpulseFrame getCombinedFrame(MultiFrame mf, int i) {
     ImpulseFrame newFrame = (ImpulseFrame) mf.getFrame(i);
     ImpulseFrame oldFrame = savedFrames[i];
     savedFrames[i] = newFrame;
    if (oldFrame == null || newFrame == null) {
       return newFrame;
     }
     return newFrame.prepend(oldFrame);
   }
 
   public StreamFrame process(StreamFrame inFrame) throws Exception {
     MultiFrame mf = (MultiFrame) inFrame;
     ImpulseFrame aFrame = getCombinedFrame(mf, 0);
     ImpulseFrame bFrame = getCombinedFrame(mf, 1);
    if (aFrame == null || bFrame == null) {
      return null;
    }
     if (aFrame.getHeader().id.compareTo(bFrame.getHeader().id) > 0) {
       throw new RuntimeException("Frames ids not ordered properly");
     }
 
     if (lastSeqNum >= 0) {
       for (; lastSeqNum < inFrame.seqNum; lastSeqNum++) {
         weightWindowUs = (weightWindowUs * weightWindowWeight + endingWindowMs * 1000.0)/(weightWindowWeight + 1);
       }
     }
     lastSeqNum = inFrame.seqNum;
 
     int aSize = aFrame.peakOffsets.length;
     int bSize = bFrame.peakOffsets.length;
     List<Peak> peaks = new ArrayList<Peak>(aSize * bSize);
     int cal = (int) cf.getCalibration();
     for (int ai = 0; ai < aSize; ai++) {
       for (int bi = 0; bi < bSize; bi++) {
         int ao = aFrame.peakOffsets[ai];
         int bo = bFrame.peakOffsets[bi];
         ao = ao - cal;
         double weight = calcWeight(ao, aFrame.peakMagnitudes[ai],
                 bo, bFrame.peakMagnitudes[bi], cal);
         if (weight > 0) {
           peaks.add(new Peak(ai, ao, bi, bo, weight));
         }
       }
     }
 
     // Totally not the most efficient data-structure, but I don't care
     boolean[] aMark = new boolean[aSize];
     boolean[] bMark = new boolean[bSize];
     List<Peak> output = new ArrayList<Peak>(Math.min(aSize, bSize));
 
     Peak[] peakArray = (Peak[]) peaks.toArray(new Peak[0]);
     Arrays.sort(peakArray);
     for (int i = 0;i < peakArray.length;i ++) {
       Peak p = peakArray[i];
       if (p.ao < 0 && p.bo < 0)
         continue;
       if (aMark[p.ai] || bMark[p.bi])
         continue;
       aMark[p.ai] = true;
       bMark[p.bi] = true;
       output.add(p);
     }
 
     ArrayList<Double> peakDeltas = new ArrayList<Double>();
     ArrayList<Double> peakMagnitudes = new ArrayList<Double>();
     ArrayList<Double> peakRaw = new ArrayList<Double>();
 
     for (int i = 0;i < output.size(); i++) {
       Peak p = output.get(i);
       int diff = p.ao - p.bo;
       if (cf.updateCalibration(diff, p.weight, inFrame)) {
         peakDeltas.add((double) diff);
         peakMagnitudes.add(p.weight);
         peakRaw.add((double) diff);
       }
     }
 
     // empty frames (no correlation) should lower the average...
     if (output.size() == 0) {
       cf.updateCalibration(0, 0, inFrame);
     }
 
     return header.makeFrame(peakDeltas, peakMagnitudes, peakRaw);
   }
 
   public void close() {
   }
 
   public static void main(String[] args) throws Exception {
     int arg = 0;
     boolean calibrated = false;
 
     int calMethod = 0;
     if (args[arg].startsWith("-c")) {
       String calStr = args[arg++];
       calibrated = true;
       if (calStr.length() > 2) {
         calMethod = Integer.parseInt(calStr.substring(2));
       } else
         calMethod = 1;
     }
 
     String outf = args[arg++];
     String in1 = args[arg++];
     String in2 = args[arg++];
     if (arg != args.length)
       throw new IllegalArgumentException("Excess arguments");
 
     System.out.println("TDOACrossModule: " + outf + " " + in1 + " " + in2);
     FileStream ifs1 = new ImpulseFileStream(in1);
     FileStream ifs2 = new ImpulseFileStream(in2);
 
     MultiFrameStream mfs = new MultiFrameStream("tdoa");
     StreamHeader h1, h2;
     mfs.setHeader(h1 = ifs1.getHeader());
     mfs.setHeader(h2 = ifs2.getHeader());
 
     CalibrationManager cf = new CalibrationManager(h1.id, h2.id, calMethod);
     cf.writeCalibration("calibration_" + h1.id + "_" + h2.id + ".txt", h1);
 
     TDOACrossModule tdoa = new TDOACrossModule();
     tdoa.setCalibrationManager(cf);
 
     FileStream ofs = new DistanceFileStream(outf, true);
     ofs.setHeader(tdoa.init(mfs.getHeader()));
 
     try {
       mfs.noblock = true;
       while (true) {
         mfs.sendFrame(ifs1.recvFrame());
         mfs.sendFrame(ifs2.recvFrame());
         ofs.sendFrame(tdoa.process(mfs.recvFrame()));
       }
     } catch (IllegalBlockingModeException e) {
       // normal termination due to no more data
     } catch (Exception e) {
       e.printStackTrace();
     }
     tdoa.close();
     ofs.close();
   }
 }
