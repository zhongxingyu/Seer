 package com.livejournal.karino2.whiteboardcast;
 
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.os.Environment;
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
 import java.util.TimerTask;
 
 /**
  * Created by karino on 6/26/13.
  */
 public class EncoderTask extends TimerTask {
 
     Bitmap bitmap;
     FrameRetrieval retrieval;
     int[] pixelBuf;
     Encoder encoder;
     StringBuilder errorBuf;
     int width;
     int height;
     long beginMillis=0;
 
     public EncoderTask(FrameRetrieval frameR, Bitmap parentBmp) {
         retrieval = frameR;
         updateBitmap(parentBmp);
         errorBuf = new StringBuilder();
         encoder = new Encoder();
     }
 
     static final int FOURCC = 0x30385056;
     static final int FPS_NUM = 24;
     static final int FPS_DENOM = 1;
     /*
     static final int FPS_NUM = 24000;
     static final int FPS_DENOM = 1001;
     */
 
     public boolean initEncoder() {
         boolean res = encoder.initEncoder(Environment.getExternalStorageDirectory() + "/temp.webm", width, height, FPS_NUM, FPS_DENOM, errorBuf);
         beginMillis = System.currentTimeMillis();
         return res;
     }
 
 
     int stride;
     public void updateBitmap(Bitmap parentBmp) {
         bitmap = Bitmap.createBitmap(parentBmp);
         width = bitmap.getWidth();
         height = bitmap.getHeight();
         Log.d("WBCast", "width=" + width + ", height=" + height);
         int bufLen = width*height;
         stride = width;
         if(pixelBuf == null || pixelBuf.length != bufLen) {
             pixelBuf = new int[bufLen];
             bitmap.getPixels(pixelBuf, 0, width, 0, 0, width, height);
         }
     }
 
     public boolean encodeFrame(int[] frame) {
         long curr = System.currentTimeMillis();
         long diff = curr-beginMillis;
        return encoder.encodeFrames(frame,  (int)(diff*FPS_NUM/1000), LibVpxEnc.FOURCC_ARGB, errorBuf);
     }
 
     // when call doneEncoder, you do not need to call finalizeEncoder()
     public boolean doneEncoder() {
         return encoder.doneEncoder(errorBuf);
     }
 
     public void finalizeEncoder() {
         encoder.finalizeEncoder();
     }
 
     Rect invalRect = new Rect();
     // scheduleAtFixedRate(TimerTask task, long delay, long period)
     @Override
     public void run() {
         retrieval.pullUpdateRegion(pixelBuf, invalRect);
         encodeFrame(pixelBuf);
     }
 
     public StringBuilder getErrorBuf() {
         return errorBuf;
     }
 
 }
