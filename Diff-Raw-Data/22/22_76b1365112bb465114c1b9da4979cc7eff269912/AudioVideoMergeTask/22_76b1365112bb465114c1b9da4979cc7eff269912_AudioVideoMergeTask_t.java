 package com.livejournal.karino2.whiteboardcast;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.media.AudioRecord;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.libvorbis.VorbisEncConfig;
 import com.google.libvorbis.VorbisEncoderC;
 import com.google.libvpx.LibVpxEnc;
 import com.google.libvpx.LibVpxEncConfig;
 import com.google.libvpx.Rational;
 import com.google.libvpx.VpxCodecCxPkt;
 import com.google.libwebm.mkvmuxer.AudioTrack;
 import com.google.libwebm.mkvmuxer.MkvWriter;
 import com.google.libwebm.mkvmuxer.Segment;
 import com.google.libwebm.mkvmuxer.SegmentInfo;
 import com.google.libwebm.mkvmuxer.VideoTrack;
 import com.google.utils.WavReader;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Created by karino on 6/28/13.
  */
 public class AudioVideoMergeTask extends AsyncTask<String, Integer, String> {
 
     public interface NotifyFinishListener {
         void onFinish();
     }
 
     private Context context;
     NotifyFinishListener finishListener;
 
     private ProgressDialog dialog;
     public AudioVideoMergeTask(Context ctx, NotifyFinishListener listener) {
         context = ctx;
         finishListener = listener;
     }
 
     @Override
     protected void onPreExecute() {
        dialog = new ProgressDialog(context);
         dialog.setTitle("Please wait");
         dialog.show();
     }
 
     class PCMReader {
         private FileInputStream is = null;
         PCMReader(File file) throws IOException {
             is = new FileInputStream(file);
         }
 
         private int channelNum = 2;
         private int bps = 16;
 
         byte[] readSamples(int numSamples) throws IOException {
             final int bytesToRead = numSamples * channelNum * bps;
             byte[] dataArray = new byte[bytesToRead];
             if (is.read(dataArray) == -1)
                 throw new IOException("Error reading samples.");
             return dataArray;
         }
 
         int samplesRemaining() {
             int samplesLeft = 0;
             try {
                 samplesLeft = (is.available()) / (channelNum * bps);
             } catch (Exception e) {
                 return 0;
             }
             return samplesLeft;
         }
 
     }
 
 
     @Override
     protected String doInBackground(String... files) {
         String videoPath = files[0];
         String audioPath = files[1];
         String resultPath = files[2];
 
         try {
             doMergeAudioVideo(videoPath, audioPath, resultPath);
         }catch (Exception e) {
             Log.d("WBCast", e.getMessage());
             return new String("Caught error in main encode loop. " + e.getMessage());
         }
         return null;
     }
 
     private void doMergeAudioVideo(String videoPath, String audioPath, String resultPath) throws Exception {
         MkvWriter mkvWriter = null;
 
         WebmReader webmReader = new WebmReader();
 
         if(!webmReader.open(videoPath)) {
             throw new Exception("Input file is invalid or error while opening.");
         }
         webmReader.initTracks();
         com.google.libwebm.mkvparser.VideoTrack firstTrack = (com.google.libwebm.mkvparser.VideoTrack)webmReader.getCurrentTrack();
 
 
         WebmReader audioReader = new WebmReader();
         if(!audioReader.open(audioPath)) {
             throw new Exception("Error opening ogg file:" + audioPath);
         }
         audioReader.initTracks();
 
         mkvWriter = new MkvWriter();
         if (!mkvWriter.open(resultPath)) {
             throw new Exception("WebM Output name is invalid or error while opening.");
         }
 
         try {
 
             Segment muxerSegment = new Segment();
             if (!muxerSegment.init(mkvWriter)) {
                 throw new Exception("Could not initialize muxer segment.");
             }
 
             SegmentInfo muxerSegmentInfo = muxerSegment.getSegmentInfo();
             muxerSegmentInfo.setWritingApp("MergeAudioVideo");
 
             long newVideoTrackNumber = 0;
 
             String trackName = firstTrack.getNameAsUtf8();
             long width = firstTrack.getWidth();
             long height = firstTrack.getHeight();
             newVideoTrackNumber = muxerSegment.addVideoTrack((int) width, (int) height, 0);
             if (newVideoTrackNumber == 0) {
                 throw new Exception("Could not add video track.");
             }
 
             VideoTrack muxerTrack = (VideoTrack) muxerSegment.getTrackByNumber(newVideoTrackNumber);
             if (muxerTrack == null) {
                 throw new Exception("Could not get video track.");
             }
 
             if (trackName != null) {
                 muxerTrack.setName(trackName);
             }
             double rate = firstTrack.getFrameRate();
             if (rate > 0) {
                 muxerTrack.setFrameRate(rate);
             }
 
             com.google.libwebm.mkvparser.AudioTrack firstAudioTrack = (com.google.libwebm.mkvparser.AudioTrack)audioReader.getCurrentTrack();
 
             long newAudioTrackNumber = muxerSegment.addAudioTrack((int)firstAudioTrack.getSamplingRate(), (int)firstAudioTrack.getChannels(), 0);
             if (newAudioTrackNumber == 0) {
                 throw new Exception("Could not add audio track.");
             }
 
             AudioTrack muxerAudioTrack = (AudioTrack) muxerSegment.getTrackByNumber(newAudioTrackNumber);
             if (muxerAudioTrack == null) {
                 throw new Exception("Could not get audio track.");
             }
 
             long[] outputPrivateSize = {0};
             byte[] privateData = firstAudioTrack.getCodecPrivate(outputPrivateSize);
             long privateSize = outputPrivateSize[0];
             if (privateSize > 0 && !muxerAudioTrack.setCodecPrivate(privateData)) {
                 throw new Exception("Could not add audio private data.");
             }
 
             long bitDepth = firstAudioTrack.getBitDepth();
             if (bitDepth > 0) {
                 muxerAudioTrack.setBitDepth(bitDepth);
             }
 
 
             muxerSegment.cuesTrack(newVideoTrackNumber);
             muxerSegment.cuesTrack(newAudioTrackNumber);
 
             if (!webmReader.initCluster() ) {
                 throw new Exception("initCluster fail. invalid webm");
             }
             if (!webmReader.initFrameQueue()) {
                 throw new Exception("initFrameQueue fail. invalid webm");
             }
             if (!audioReader.initCluster() ) {
                 throw new Exception("initCluster fail. invalid audio mkv");
             }
             if (!audioReader.initFrameQueue()) {
                 throw new Exception("initFrameQueue fail. invalid audio mkv");
             }
 
 
             byte[] vorbisFrame = null;
             byte[] webmFrame = null;
             vorbisFrame = audioReader.popFrame();
             webmFrame = webmReader.popFrame();
             // isDone return true after popFrame return null.
             try {
                 while (!audioReader.isDone() || !webmReader.isDone()) {
                     if(!audioReader.isDone() && !webmReader.isDone()) {
                         if(webmReader.getBlockTimeNS() > audioReader.getBlockTimeNS()) {
                             while(!audioReader.isDone() && webmReader.getBlockTimeNS() > audioReader.getBlockTimeNS()) {
                                 vorbisFrame = addAudioFrameAndPopNext(audioReader, muxerSegment, newAudioTrackNumber, vorbisFrame);
                             }
                         } else if(webmReader.getBlockTimeNS() < audioReader.getBlockTimeNS()) {
                             while(!webmReader.isDone() && webmReader.getBlockTimeNS() < audioReader.getBlockTimeNS()) {
                                 webmFrame = addVideoFrameAndPopNext(webmReader, muxerSegment, newVideoTrackNumber, webmFrame);
                             }
                         } else { // ==
                             while(!webmReader.isDone() && !audioReader.isDone() && webmReader.getBlockTimeNS() == audioReader.getBlockTimeNS()) {
                                 webmFrame = addVideoFrameAndPopNext(webmReader, muxerSegment, newVideoTrackNumber, webmFrame);
                                 vorbisFrame = addAudioFrameAndPopNext(audioReader, muxerSegment, newAudioTrackNumber, vorbisFrame);
                             }
 
                         }
                     }else if(!audioReader.isDone()) {
                         vorbisFrame = addAudioFrameAndPopNext(audioReader, muxerSegment, newAudioTrackNumber, vorbisFrame);
                     }else if(!webmReader.isDone()) {
                         webmFrame = addVideoFrameAndPopNext(webmReader, muxerSegment, newVideoTrackNumber, webmFrame);
                     }
                 }
             }finally {
                 audioReader.close();
                 webmReader.close();
             }
 
             if (!muxerSegment.finalizeSegment()) {
                 throw new Exception("Finalization of segment failed.");
             }
 
         } finally {
             if (mkvWriter != null) {
                 mkvWriter.close();
             }
         }
 
         finishListener.onFinish();
     }
 
     private byte[] addVideoFrameAndPopNext(WebmReader webmReader, Segment muxerSegment, long newVideoTrackNumber, byte[] webmFrame) throws Exception {
         if (!muxerSegment.addFrame(webmFrame, newVideoTrackNumber, webmReader.getBlockTimeNS(), webmReader.isKey())) {
             throw new Exception("Could not add video frame.");
         }
         webmFrame = webmReader.popFrame();
         return webmFrame;
     }
 
     private byte[] addAudioFrameAndPopNext(WebmReader audioReader, Segment muxerSegment, long newAudioTrackNumber, byte[] vorbisFrame) throws Exception {
         if (!muxerSegment.addFrame(vorbisFrame, newAudioTrackNumber, audioReader.getBlockTimeNS(), audioReader.isKey())) {
             throw new Exception("Could not add audio frame.");
         }
         vorbisFrame = audioReader.popFrame();
         return vorbisFrame;
     }
 
     protected void onProgressUpdate(Integer... progress) {
         // later.
     }
 
     protected void onPostExecute(String result) {
         if(result != null)
             Toast.makeText(context, result, Toast.LENGTH_LONG ).show();
         dialog.dismiss();
     }
 }
