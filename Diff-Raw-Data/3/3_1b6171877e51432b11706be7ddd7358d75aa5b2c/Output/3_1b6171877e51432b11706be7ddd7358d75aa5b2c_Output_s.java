 package org.xmms2.server.plugins;
 
 import android.content.Context;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 import android.media.AudioTrack;
 import org.xmms2.server.PlaybackStatusListener;
 import org.xmms2.server.Server;
 
 import java.util.ArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author Eclipser
  */
 public class Output implements PlaybackStatusListener, Runnable
 {
     private AudioTrack audioTrack;
     private int bufferSize;
     private AudioManager audioManager;
     private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
     private Thread audioThread = null;
     private boolean playing = false;
     private ArrayList<byte[]> pausedBuffers = new ArrayList<byte[]>();
 
     private final Object lock = new Object();
     private int rate;
     private int channelConfig;
     private int formatConfig;
 
     public Output(Server server)
     {
         this.audioManager = (AudioManager) server.getSystemService(Context.AUDIO_SERVICE);
         audioFocusChangeListener = server.getAudioFocusChangeListener();
         server.registerPlaybackListener(this);
     }
 
     public int getBufferSize()
     {
         return bufferSize;
     }
 
     public boolean open()
     {
         int ret = audioManager.requestAudioFocus(audioFocusChangeListener,
                                                  AudioManager.STREAM_MUSIC,
                                                  AudioManager.AUDIOFOCUS_GAIN);
         return ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
     }
 
     public void flush()
     {
         buffers.clear();
         pausedBuffers.clear();
         if (audioTrack != null) {
             audioTrack.flush();
         }
     }
 
     public void close()
     {
         if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
             audioThread.interrupt();
         }
         playing = false;
         buffers.clear();
         pausedBuffers.clear();
         audioManager.abandonAudioFocus(audioFocusChangeListener);
     }
 
     private final LinkedBlockingQueue<byte[]> free = new LinkedBlockingQueue<byte[]>(10);
     private final LinkedBlockingQueue<byte[]> buffers = new LinkedBlockingQueue<byte[]>(10);
     private byte[] a;
     private int bufpos = 0;
     private int latency = 0;
 
     public boolean write(byte[] buffer, int length)
     {
         try {
             if (audioThread == null || audioThread.isInterrupted() || !audioThread.isAlive()) {
                 initAudioThread();
             }
 
             if (buffers.size() >= 3) {
                 synchronized (buffers) {
                     buffers.notify();
                 }
             }
 
             fillBuffer(buffer, length);
 
             return true;
         } catch (InterruptedException e) {
             return false;
         }
     }
 
     private boolean fillBuffer(byte[] buffer, int length) throws InterruptedException
     {
         int byteCount = Math.min(a.length - bufpos, length);
         System.arraycopy(buffer, 0, a, bufpos, byteCount);
         bufpos = bufpos + byteCount;
 
         if (bufpos == a.length) {
             buffers.put(a);
             updateLatency();
             bufpos = 0;
             a = free.poll();
             if (a == null) {
                 a = new byte[4096];
             }
 
             if (byteCount < length) {
                 System.arraycopy(buffer, byteCount, a, bufpos, length - byteCount);
                 bufpos = length - byteCount;
             }
         }
         return false;
     }
 
     private void initAudioThread() throws InterruptedException
     {
         audioThread = new Thread(this, "XMMS2 Android Audio");
         playing = true;
         free.clear();
         while (free.remainingCapacity() > 0) {
             free.put(new byte[4096]);
         }
         a = free.take();
         if (!pausedBuffers.isEmpty()) {
             buffers.addAll(pausedBuffers);
             pausedBuffers.clear();
         }
         audioThread.start();
     }
 
     public boolean setFormat(int format, int channels, int rate)
     {
         int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
         if (channels == 2) {
             channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
         }
 
         int formatConfig = AudioFormat.ENCODING_PCM_16BIT;
         if (format != 2) {
             return false;
         }
 
         synchronized (lock) {
             if (audioTrack != null && audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                 try {
                     // wait for the audio thread to stop, it should since this is blocking any new buffers to come in
                     lock.wait();
                 } catch (InterruptedException ignored) {}
             }
         }
 
         this.rate = rate;
         this.channelConfig = channelConfig;
         this.formatConfig = formatConfig;
         this.bufferSize = AudioTrack.getMinBufferSize(rate, channelConfig, formatConfig) * 10;
 
         return true;
     }
 
     @Override
     public void adjustVolume(float left, float right)
     {
         if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
             audioTrack.setStereoVolume(left, right);
         }
     }
 
     // Because the server doesn't tell "non-status API" output plugin if it's a pause, we'll listen to this here
     // and stop the audiotrack in case of pause.
     @Override
     public void playbackStatusChanged(int newStatus)
     {
         if (newStatus == 2 && audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING && playing) {
             buffers.drainTo(pausedBuffers);
         }
     }
 
     @Override
     public void run()
     {
         if (buffers.size() < 3) {
             synchronized (buffers) {
                 try {
                     buffers.wait();
                 } catch (InterruptedException ignored) {}
             }
         }
 
         audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, rate,
                                    channelConfig, formatConfig,
                                    bufferSize, AudioTrack.MODE_STREAM);
 
         if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED &&
             audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
             audioTrack.play();
         }
 
         while (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING &&
                !audioThread.isInterrupted()) {
             byte b[];
             try {
                 b = buffers.poll(20, TimeUnit.MILLISECONDS);
                 if (b == null) {
                     break;
                 }
                 updateLatency();
                 // TODO: we might lose a buffer or two around here when the playback is paused
                 if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                     audioTrack.write(b, 0, b.length);
                     if (!pausedBuffers.isEmpty()) {
                         pausedBuffers.add(b);
                     } else {
                         free.offer(b);
                     }
                 }
             } catch (InterruptedException e) {
                 break;
             }
         }
 
        if (audioTrack != null) {
             audioTrack.stop();
         }
         synchronized (lock) {
             lock.notify();
         }
     }
 
     // TODO inaccurate
     private void updateLatency()
     {
         latency = buffers.size() * 4096 + bufpos;
     }
 
 }
