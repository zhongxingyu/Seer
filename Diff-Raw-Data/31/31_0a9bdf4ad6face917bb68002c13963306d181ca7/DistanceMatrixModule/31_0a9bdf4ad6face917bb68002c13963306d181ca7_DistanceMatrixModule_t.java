 package edu.cmu.pandaa.module;
 
 import java.nio.channels.IllegalBlockingModeException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import edu.cmu.pandaa.header.DistanceHeader.DistanceFrame;
 import edu.cmu.pandaa.header.GeometryHeader.GeometryFrame;
 import edu.cmu.pandaa.header.DistanceHeader;
 import edu.cmu.pandaa.header.GeometryHeader;
 import edu.cmu.pandaa.header.MultiHeader;
 import edu.cmu.pandaa.header.MultiHeader.MultiFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.DistanceFileStream;
 import edu.cmu.pandaa.stream.FileStream;
 import edu.cmu.pandaa.stream.GeometryFileStream;
 import edu.cmu.pandaa.stream.MultiFrameStream;
 
 
 public class DistanceMatrixModule implements StreamModule {
   GeometryHeader gHeader;
   DistanceHeader[] distanceHeaders;
   double[][] previous;
   int numDevices;
 
   public DistanceMatrixModule()
   {
   }
 
   public StreamHeader init(StreamHeader inHeader) {
     MultiHeader multiHeader = (MultiHeader) inHeader;
     if (!(multiHeader.getOne() instanceof DistanceHeader)) {
       throw new IllegalArgumentException("Input multiheader should contain Distance Headers");
     }
 
     distanceHeaders = (DistanceHeader[]) multiHeader.getHeaders(new DistanceHeader[multiHeader.size()]);
 
     for (int j = 1; j < distanceHeaders.length; j++) {
       if(distanceHeaders[0].frameTime != distanceHeaders[j].frameTime)
         throw new IllegalArgumentException("Frame duration must be equal for all input frames");
     }
     /*generate unique device id array*/
     String[] deviceIds = generateDeviceIds(distanceHeaders);
     setNumDevices(deviceIds.length);
     gHeader = new GeometryHeader(deviceIds, distanceHeaders[0].startTime,
             distanceHeaders[0].frameTime, numDevices, numDevices);
     return gHeader;
   }
 
   public void setNumDevices(int num)
   {
     this.numDevices = num;
   }
 
   public int getNumDevices()
   {
     return numDevices;
   }
 
   public String[] generateDeviceIds(DistanceHeader[] distanceHeaders)
   {
     int rows = distanceHeaders.length;
     int cols = distanceHeaders[0].getDeviceIds().length;
     Set<String> set = new HashSet<String>();
     for(int i = 0; i < rows; i++){
       for(int j = 0; j < cols; j++){
         if (!set.contains(distanceHeaders[i].getDeviceIds()[j]))
           set.add(distanceHeaders[i].getDeviceIds()[j]);
       }
     }
     return set.toArray(new String[0]);
   }
 
   public StreamFrame process(StreamFrame inFrame) {
     if (inFrame == null) {
       return null;
     }
     int numDevices = getNumDevices();
     StreamFrame[] frames = ((MultiFrame) inFrame).getFrames();
     for(int i = 0; i < frames.length; i++){
       if (frames[i] != null && !(frames[i] instanceof DistanceFrame)) {
         throw new IllegalArgumentException("Input multiframe should contain DistanceFrames");
       }
     }
 
     DistanceFrame[] dfIn = Arrays.copyOf(frames, frames.length, DistanceFrame[].class);
     double[][] distanceMatrix = new double[numDevices][numDevices];
     int count = 0;
 
     for(int i = 0; i < numDevices; i++) {
       for(int j = 0; j < numDevices; j++) {
         if(i == j)
           distanceMatrix[i][j] = 0.0; //distance of device with itself
         else if (j < i)
           distanceMatrix[i][j] = distanceMatrix[j][i]; //symmetric element
         else {
          if ((dfIn[count] == null || dfIn[count].peakDeltas.length == 0) && previous != null)
             distanceMatrix[i][j] = previous[i][j];
          else if (dfIn[count] != null) {
             distanceMatrix[i][j] = 0;
             for (int k = 0; k < dfIn[count].peakDeltas.length; k++)
               distanceMatrix[i][j] += dfIn[count].peakDeltas[k];
             distanceMatrix[i][j] /= dfIn[count].peakDeltas.length;
           }
           // TODO: Try to find a better way to do this because it seems very unstable through code changes
           count++; // only increment for the "active" half of the matrix we're populating
         }
       }
     }
 
     if (compareMatrix(distanceMatrix, previous))
       return null;
 
     GeometryFrame gfOut = gHeader.makeFrame(inFrame.seqNum, distanceMatrix);
     previous = distanceMatrix.clone();
     return gfOut;
   }
 
   public void close() {
   }
 
   private boolean compareMatrix(double[][] a, double[][] b) {
     if (a == null || b == null)
       return a == b;
 
     if (a.length != b.length)
       return false;
 
     for (int i = 0;i < a.length;i++) {
       if (a[i].length != b[i].length)
         return false;
       for (int j = 0;j < a[i].length;j++) {
         if (a[i][j] != b[i][j])
           return false;
       }
     }
 
     return true;
   }
 
 
   public static void main(String[] args) throws Exception {
     int i = 0;
     int numDev = args.length - 1;
     String[] inArg = new String[numDev];
     String outArg = args[i];
     for(i = 0; i < numDev; i++){
       inArg[i] = args[i+1];
     }
     if (i != numDev)
       throw new IllegalArgumentException("Invalid number of arguments");
 
     System.out.print("DistanceMatrix: " + outArg);
     for(i = 0; i < numDev; i++) {
       System.out.print(" " + inArg[i]);
     }
     System.out.println();
 
     FileStream[] ifs = new DistanceFileStream[numDev];
 
     for(i = 0; i < numDev; i++){
       ifs[i] = new DistanceFileStream(inArg[i]);
     }
 
     MultiFrameStream mfs = new MultiFrameStream("tdoa123");
 
     StreamHeader[] ifh = new StreamHeader[numDev];
     StreamFrame[] iff = new StreamFrame[numDev];
     for(i = 0; i < numDev; i++){
       ifh[i] = ifs[i].getHeader();
       mfs.setHeader(ifh[i]);
     }
 
     FileStream ofs = new GeometryFileStream(outArg, true, false);
 
     DistanceMatrixModule ppd = new DistanceMatrixModule();
     ofs.setHeader(ppd.init(mfs.getHeader()));
 
 
     try {
       mfs.noblock = true;
       while(true){
         int minSeq = 0;
         for(i=0; i < numDev; i++){
           if (iff[i] == null)
             iff[i] = ifs[i].recvFrame();
           if (iff[i] != null && (iff[i].seqNum < minSeq || minSeq == 0))
             minSeq = iff[i].seqNum;
         }
 
         for(i=0; i < numDev; i++){
           mfs.sendFrame(iff[i]);
           if (iff[i] != null && iff[i].seqNum == minSeq) {
             iff[i] = null;
           }
         }
 
         StreamFrame frameIn = mfs.recvFrame();
         if (frameIn == null)
           break;
         StreamFrame frameOut = ppd.process(frameIn);
         if(frameOut != null)
           ofs.sendFrame(frameOut);
       }
     } catch (IllegalBlockingModeException e) {
       // ran out of data
     } catch (Exception e) {
       e.printStackTrace();
     }
     ofs.close();
   }
 }
