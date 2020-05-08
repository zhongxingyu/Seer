 package com.livejournal.karino2.whiteboardcast;
 
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.MediaRecorder;
 import android.util.Log;
 
 import com.google.libvorbis.AudioFrame;
 import com.google.libvorbis.VorbisEncConfig;
 import com.google.libvorbis.VorbisEncoderC;
 import com.google.libvorbis.VorbisException;
 import com.google.libwebm.mkvmuxer.AudioTrack;
 import com.google.libwebm.mkvmuxer.MkvWriter;
 import com.google.libwebm.mkvmuxer.Segment;
 import com.google.libwebm.mkvmuxer.SegmentInfo;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * Created by karino on 6/30/13.
  */
 public class VorbisMediaRecorder {
     static final int TIMER_INTERVAL = 120;
 
     final short nChannels = 1;
     final int sampleRate = 44100;
     final short bSamples = 16;
     final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
     final int AUDIO_FORMAT =  AudioFormat.ENCODING_PCM_16BIT;
 
     int bufferSize;
     int framePeriod;
     AudioRecord audioRecorder;
     byte[] buffer;
     long beginMill;
     long prevBlockEndNano;
 
     public void setBeginMill(long beginMill) {
         this.beginMill = beginMill;
         prevBlockEndNano = 0;
     }
 
     enum State {INITIALIZING, READY, RECORDING, ERROR, STOPPED};
     State state;
     String filePath;
 
     long newAudioTrackNumber;
     Segment muxerSegment;
     MkvWriter mkvWriter;
 
 
     public VorbisMediaRecorder () {
         framePeriod = sampleRate * TIMER_INTERVAL / 1000;
        bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
         if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT))
         {
             bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT);
             framePeriod = bufferSize / ( 2 * bSamples * nChannels / 8 );
             Log.d("WBCast", "Extend buffer size: " + bufferSize);
         }
 
         audioRecorder = new AudioRecord(AUDIO_SOURCE, sampleRate, AudioFormat.CHANNEL_IN_MONO, AUDIO_FORMAT, bufferSize);
         state = State.INITIALIZING;
     }
 
     VorbisEncoderC vorbisEncoder;
 
     public void prepare() throws IOException, VorbisException {
         if (state == State.INITIALIZING) {
             if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                 throw new RuntimeException("AudioRecord initialization failed");
             audioRecorder.setRecordPositionUpdateListener(updateListener);
             audioRecorder.setPositionNotificationPeriod(framePeriod);
 
             // create vorbis encoder here.
             VorbisEncConfig vorbisConf = new VorbisEncConfig(nChannels, sampleRate, bSamples);
             vorbisConf.setTimebase(1, 1000000000);
 
             vorbisEncoder = new VorbisEncoderC(vorbisConf);
 
 
             mkvWriter = new MkvWriter();
             if (!mkvWriter.open(filePath)) {
                 throw new IOException("ogg Output name is invalid or error while opening.");
             }
 
             muxerSegment = new Segment();
             if (!muxerSegment.init(mkvWriter)) {
                 throw new RuntimeException("Could not initialize muxer segment.");
             }
 
             SegmentInfo muxerSegmentInfo = muxerSegment.getSegmentInfo();
             muxerSegmentInfo.setWritingApp("VorbisMediaRecoder");
 
             newAudioTrackNumber = muxerSegment.addAudioTrack(sampleRate, nChannels, 0);
             if (newAudioTrackNumber == 0) {
                 throw new RuntimeException("Could not add audio track.");
             }
 
             AudioTrack muxerTrack = (AudioTrack) muxerSegment.getTrackByNumber(newAudioTrackNumber);
             if (muxerTrack == null) {
                 throw new RuntimeException("Could not get audio track.");
             }
             byte[] privBuffer = vorbisEncoder.CodecPrivate();
             if (privBuffer == null) {
                 throw new RuntimeException("Could not get audio private data.");
             }
             if (!muxerTrack.setCodecPrivate(privBuffer)) {
                 throw new RuntimeException("Could not add audio private data.");
             }
 
             buffer = new byte[framePeriod*bSamples/8*nChannels];
             state = State.READY;
         }
 
     }
 
     public void setOutputFile(String argPath) {
         filePath = argPath;
     }
 
 
     public void start() {
         if(state == State.READY ||
                 state == State.STOPPED) {
             audioRecorder.startRecording();
             audioRecorder.read(buffer, 0, buffer.length);
             state = State.RECORDING;
         }
     }
 
     public void resume(long pasuedMil) {
         if(state == State.STOPPED) {
             beginMill += pasuedMil;
             start();
         } else {
             Log.d("WBCast", "VorbisRec: resume call but state is not stopped: " + state);
         }
     }
 
     public void stop()  {
         if(state == State.RECORDING) {
             updateListener.onPeriodicNotification(audioRecorder);
             audioRecorder.stop();
 
             state = State.STOPPED;
         }
     }
 
     public void release() {
         if(state == State.RECORDING) {
             stop();
         }
         if (!muxerSegment.finalizeSegment()) {
             throw new RuntimeException("Finalization of segment failed.");
         }
         if (mkvWriter != null) {
             mkvWriter.close();
         }
         audioRecorder.release();
     }
 
     AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
 
         @Override
         public void onMarkerReached(AudioRecord audioRecord) {
             // do nothing.
         }
 
         @Override
         public void onPeriodicNotification(AudioRecord audioRecord) {
             int readLen = audioRecorder.read(buffer, 0, buffer.length);
             if(readLen > 0 ) {
                 long currentMil = System.currentTimeMillis();
                 byte[] buf;
                 if(readLen == buffer.length) {
                     buf = buffer;
                 } else {
                     buf = Arrays.copyOf(buffer, readLen);
                 }
                 if (!vorbisEncoder.Encode(buf)) {
                     Log.d("WBCast", "Error encoding samples.");
                     return;
                 }
 
 
                 ArrayList<AudioFrame> frames = popAudioFrames();
 
                 long blockEndNano = (currentMil - beginMill)*1000000;
                 long blockDurNano = blockEndNano - prevBlockEndNano;
                 int frameNum = frames.size();
                 long blockStartNano = prevBlockEndNano; // Math.max(prevBlockEndNano,blockEndMil*1000000 - (frameNum*1000000000)/sampleRate);
                 for(int i = 0; i < frames.size(); i++){
                     AudioFrame frame = frames.get(i);
                     if (!muxerSegment.addFrame(
                             frame.buffer, newAudioTrackNumber, blockStartNano+((i+1)*blockDurNano)/frameNum, true)) {
                         Log.d("WBCast", "Could not add audio frame.");
                         return;
                     }
                 }
                 prevBlockEndNano = blockEndNano;
             }
 
         }
     };
 
     public long lastBlockEndMil() {
         return beginMill + prevBlockEndNano/1000000;
     }
 
     private ArrayList<AudioFrame> popAudioFrames() {
         ArrayList<AudioFrame> frames = new ArrayList<AudioFrame>();
         AudioFrame frame = null;
         while ((frame = vorbisEncoder.ReadCompressedFrame()) != null) {
             frames.add(frame);
         }
         return frames;
     }
 
 }
