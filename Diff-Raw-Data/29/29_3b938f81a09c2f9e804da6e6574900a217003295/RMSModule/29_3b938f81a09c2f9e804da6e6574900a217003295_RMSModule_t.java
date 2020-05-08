 package edu.cmu.pandaa.module;
 
 import edu.cmu.pandaa.header.DistanceHeader;
 import edu.cmu.pandaa.header.GeometryHeader;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.GeometryHeader.GeometryFrame;
 import edu.cmu.pandaa.header.DistanceHeader.DistanceFrame;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.DistanceFileStream;
 import edu.cmu.pandaa.stream.FrameStream;
 import edu.cmu.pandaa.stream.GeometryFileStream;
 
 public class RMSModule implements StreamModule{
   DistanceHeader dOut;
   GeometryFrame actual;
  int flipped = 0;
 
   public void extractActual(FrameStream inStream, FrameStream outGS) throws Exception
   {
     outGS.setHeader(inStream.getHeader());
     actual = (GeometryFrame) inStream.recvFrame();
     actual.adjustAxes();
  }

  public void finalizeActual(FrameStream outGS) throws Exception {
    if (flipped < 0) {
       actual.flip();
    }
     outGS.sendFrame(actual);
     outGS.close();
   }
 
   public void runModule(FrameStream inGS1, FrameStream inGS2,
                         FrameStream outDS, FrameStream outGS) throws Exception {
     try{
       StreamHeader header = init(inGS1.getHeader());
       outDS.setHeader(header);
       extractActual(inGS2, outGS);
       StreamFrame f1;
       while ((f1 = inGS1.recvFrame()) != null) {
         StreamFrame frameOut = process(f1);
         outDS.sendFrame(frameOut);
       }
     }catch(Exception e){
       e.printStackTrace();
     }
    finalizeActual(outGS);
     outDS.close();
     inGS1.close();
     inGS2.close();
   }
 
   public StreamHeader init(StreamHeader i1) {
     if (!(i1 instanceof GeometryHeader))
       throw new RuntimeException("Wrong header type");
     GeometryHeader hIn = (GeometryHeader)i1 ;
     String id = "";
     for(int i = 0; i < hIn.deviceIds.length; i++)
     {
       id += hIn.deviceIds[i];
     }
     dOut = new DistanceHeader(id, hIn.startTime, hIn.frameTime, hIn.deviceIds);
     return dOut;
   }
 
   public StreamFrame process(StreamFrame f1) {
     if (!(f1 instanceof GeometryFrame))
       throw new RuntimeException("Wrong frame type");
     GeometryFrame estimated = (GeometryFrame) f1 ; // estimate
     int numDevices = dOut.deviceIds.length;
 
     double rmsA = 0, rmsB = 0;
     for(int j = 0; j < numDevices; j++){
       rmsA +=  Math.pow((estimated.geometry[0][j] - actual.geometry[0][j]),2) +
               Math.pow((estimated.geometry[1][j] - actual.geometry[1][j]),2);
       rmsB +=  Math.pow((estimated.geometry[0][j] + actual.geometry[0][j]),2) +
               Math.pow((estimated.geometry[1][j] - actual.geometry[1][j]),2);
     }
 
    flipped += rmsA < rmsB ? 1 : (rmsB < rmsA ? -1 : 0);
 
    double[] rms = {  Math.sqrt((flipped > 0 ? rmsA : rmsB) )/ numDevices };
     double[] magnitudes = { 0.0 };
     DistanceFrame dfOut = dOut.makeFrame(rms,magnitudes);
 
     return dfOut ;
   }
 
   public static void main(String[] args) throws Exception
   {
     int arg = 0;
     String outArg = args[arg++];
     String inArg1 = args[arg++];
     String inArg2 = args[arg++];
     if (args.length > arg || args.length < arg) {
       throw new IllegalArgumentException("Invalid number of arguments");
     }
     System.out.println("RMS: " + outArg + " " + inArg1 + " " + inArg2);
     GeometryFileStream rIn1 = new GeometryFileStream(inArg1); //geometryOut
     GeometryFileStream rIn2 = new GeometryFileStream(inArg2); //config file
     DistanceFileStream rOut = new DistanceFileStream(outArg, true); //rmsOut
     int eIndex = outArg.lastIndexOf('.');
     outArg = outArg.substring(0, eIndex) + "-actual" + outArg.substring(eIndex);
     GeometryFileStream rOut2 = new GeometryFileStream(outArg, true, true); //rmsOut
 
     try {
       RMSModule rms = new RMSModule();
       rms.runModule(rIn1, rIn2, rOut, rOut2);
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   @Override
   public void close() {
     // TODO Auto-generated method stub
 
   }
 }
