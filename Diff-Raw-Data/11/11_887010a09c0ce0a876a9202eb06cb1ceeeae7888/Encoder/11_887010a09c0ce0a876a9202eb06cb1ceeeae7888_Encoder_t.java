 package com.livejournal.karino2.whiteboardcast;
 
 import android.util.Log;
 
 import com.google.libvpx.LibVpxEnc;
 import com.google.libvpx.LibVpxEncConfig;
 import com.google.libvpx.LibVpxException;
 import com.google.libvpx.Rational;
 import com.google.libvpx.VpxCodecCxPkt;
 import com.google.libwebm.mkvmuxer.MkvWriter;
 import com.google.libwebm.mkvmuxer.Segment;
 import com.google.libwebm.mkvmuxer.SegmentInfo;
 
 import java.util.ArrayList;
 
 /**
  * Created by karino on 6/27/13.
  */
 public class Encoder {
     public interface FinalizeListener {
         void done();
     }
 
     boolean pendingDone = false;
 
     FinalizeListener listener;
     public boolean doneEncoder(StringBuilder error, FinalizeListener listen) {
         listener = listen;
         if(duringEncoding) {
             pendingDone = true;
             return true;
         }
         return doneEncoderCore(error);
     }
 
     private boolean doneEncoderCore(StringBuilder error) {
         try {
             if (!muxerSegment.finalizeSegment()) {
                 error.append("Finalization of segment failed.");
                 return false;
             }
         } finally {
             finalizeEncoder();
         }
         listener.done();
         return true;
     }
 
     public void finalizeEncoder() {
         if (encoder != null) {
             encoder.close();
         }
         if (encoderConfig != null) {
             encoderConfig.close();
         }
         if (mkvWriter != null) {
             mkvWriter.close();
         }
     }
 
     private boolean duringEncoding = false;
 
     public boolean encodeFrames(int[] srcFrame,int framesToEncode, long fourcc, StringBuilder error) {
         duringEncoding = true;
         try {
             if(!encodeOneFrame(srcFrame,  framesToEncode, fourcc, error))
                 return false;
             framesIn = framesToEncode;
 
             if(pendingDone) {
                 pendingDone = false;
                 return doneEncoderCore(error);
             }
             return true;
         }finally {
             duringEncoding = false;
         }
     }
 
     LibVpxEncConfig encoderConfig = null;
     LibVpxEnc encoder = null;
     MkvWriter mkvWriter = null;
     Segment muxerSegment = null;
     int framesIn = 1;
     Rational timeMultiplier = null;
     long newVideoTrackNumber = 0;
 
     public boolean initEncoder(String webmOutputName,
                                int width, int height, int rate, int scale,
                                StringBuilder error) {
        // Log.d("WBCast", "width, height=" + width + "," + height);
         try {
             encoderConfig = new LibVpxEncConfig(width, height);
             encoder = new LibVpxEnc(encoderConfig);
 
             // libwebm expects nanosecond units
             encoderConfig.setTimebase(1, 1000000000);
             Rational timeBase = encoderConfig.getTimebase();
             Rational frameRate = new Rational(rate, scale);
             timeMultiplier = timeBase.multiply(frameRate).reciprocal();
             framesIn = 1;
 
             mkvWriter = new MkvWriter();
             if (!mkvWriter.open(webmOutputName)) {
                 error.append("WebM Output name is invalid or error while opening.");
                 return false;
             }
 
             muxerSegment = new Segment();
             if (!muxerSegment.init(mkvWriter)) {
                 error.append("Could not initialize muxer segment.");
                 return false;
             }
 
             SegmentInfo muxerSegmentInfo = muxerSegment.getSegmentInfo();
             muxerSegmentInfo.setWritingApp("y4mEncodeSample");
 
             newVideoTrackNumber = muxerSegment.addVideoTrack(width, height, 0);
             if (newVideoTrackNumber == 0) {
                 error.append("Could not add video track.");
                 return false;
             }
         }
         catch (LibVpxException e) {
             error.append("Encoder error : " + e);
             return false;
         }
         return true;
     }
 
     private boolean encodeOneFrame(int[] srcFrame, int endFrame,  long fourcc,
                                    StringBuilder error) {
         long frameStart = timeMultiplier.multiply(framesIn - 1).toLong();
         long nextFrameStart = timeMultiplier.multiply(endFrame).toLong();
 
         ArrayList<VpxCodecCxPkt> encPkt = null;
         try {
             encPkt = encoder.convertIntEncodeFrame(
                     srcFrame, frameStart, nextFrameStart - frameStart, fourcc);
             for (int i = 0; i < encPkt.size(); i++) {
                 VpxCodecCxPkt pkt = encPkt.get(i);
                 final boolean isKey = (pkt.flags & 0x1) == 1;
 
                 if (!muxerSegment.addFrame(pkt.buffer, newVideoTrackNumber, pkt.pts, isKey)) {
                     error.append("Could not add frame.");
                     return false;
                 }
             }
         } catch (LibVpxException e) {
             error.append("Encoder error : " + e);
             return false;
         }
         return true;
     }
 
 }
