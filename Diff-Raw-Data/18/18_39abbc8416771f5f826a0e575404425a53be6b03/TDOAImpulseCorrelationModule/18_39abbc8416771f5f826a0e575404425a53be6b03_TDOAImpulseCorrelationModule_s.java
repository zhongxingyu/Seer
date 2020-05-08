 package edu.cmu.pandaa.module;
 
import edu.cmu.pandaa.header.FeatureHeader;
import edu.cmu.pandaa.header.FeatureHeader.FeatureFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.FrameStream;
 
 
 class TDOAImpulseCorrelationModule implements StreamModule {
   FrameStream inFeatureFrameStream1;
   FrameStream inFeatureFrameStream2;
   FrameStream outDistanceFrameStream;
   
   TDOAImpulseCorrelationModule(FrameStream inFeatureFrameStream1, 
 			       FrameStream inFeatureFrameStream2, 
 			       FrameStream outDistanceFrameStream) {
     this.inFeatureFrameStream1 = inFeatureFrameStream1;
     this.inFeatureFrameStream2 = inFeatureFrameStream2;
     this.outDistanceFrameStream = outDistanceFrameStream;
   }
   
   public void run() throws Exception {
     StreamHeader header = init(inFeatureFrameStream1.getHeader());
     outDistanceFrameStream.setHeader(header);
     
     StreamFrame frameIn1, frameIn2, frameOut;
     while ((frameIn1 = inFeatureFrameStream1.recvFrame()) != null && 
 	   (frameIn2 = inFeatureFrameStream2.recvFrame()) != null) {
       frameOut = process(frameIn1);
       outDistanceFrameStream.sendFrame(frameOut);
     }
 
     close();
   }
   
   @Override
 public StreamHeader init(StreamHeader inHeader) {
    if (!(inHeader instanceof FeatureHeader))
       throw new RuntimeException("Wrong header type");
 
     // TODO: would actually do work here to compute new header
     return null;
   }
 
   @Override
 public StreamFrame process(StreamFrame inFrame1) { // StreamFrame inFrame2
     StreamFrame inFrame2 = null;
    if (!(inFrame1 instanceof FeatureFrame) || !(inFrame2 instanceof FeatureFrame))
       throw new RuntimeException("Wrong frame type");
 
     // TODO: Would actually do work here to compute new frame
     return null;
   }
 
   @Override
 public void close() {
   }
 }
