 package ee.webmedia.mikker;
 
 import ee.webmedia.mikker.events.RecordingEvent;
 import ee.webmedia.mikker.events.RecordingListener;
 
 import javax.sound.sampled.*;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class SoundRecorder implements Recorder {
     private ByteArrayOutputStream bout;
     private Thread thread;
     private TargetDataLine line;
 
     private List<RecordingListener> listeners = new ArrayList<RecordingListener>();
     private volatile boolean stopPlayback;
 
     public void startRecording() {
         System.out.println("start");
 
         try {
             AudioFormat format = audioFormat();
             
             DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
             line = (TargetDataLine) AudioSystem.getLine(info);
             line.open(format);
 
             line.start();
             bout = new ByteArrayOutputStream();
 
             Thread thread = new Thread() {
                 public void run() {
                     try {
                         System.out.println("writing ...");
                         AudioSystem.write(new AudioInputStream(line), AudioFileFormat.Type.AU, bout);
                         System.out.println("... done writing");
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     System.out.println("thread exiting");
                 }
             };
             this.thread = thread;
             this.thread.start();
             
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     private AudioFormat audioFormat() {
         int sampleSize = 16;
         int channels = 1;
         float rate = 44100f;
         AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, sampleSize, channels,
                 (sampleSize / 8) * channels, rate, true);
 
         return fmt;
     }
 
     public void stopRecording() {
         System.out.println("stop");
         try {
             line.stop();
             line.close();
             thread.join();
 
             System.out.println("... joined");
 
             bout.close();
             System.out.println("... closed");
             notifyListeners(new RecordingEvent(true, false));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void stopPlaying() {
         stopPlayback = true;
         System.out.println("Stopped: " + System.currentTimeMillis());
     }
 
     public void startPlaying() {
         System.out.println("replay");
         new ReplayThread().start();
         
     }
 
     public void deleteRecording() {
         this.bout = null;
         notifyListeners(new RecordingEvent(false, false));
     }
 
     public void addListener(RecordingListener deleteButton) {
         listeners.add(deleteButton);
     }
 
     private void notifyListeners(RecordingEvent event) {
         for (RecordingListener l : listeners) {
             l.onRecordingEvent(event);
         }
     }
 
     public void writeResult(OutputStream out) throws IOException {
         out.write(bout.toByteArray());
     }
 
     private class ReplayThread extends Thread {
         public void run() {
             try {
                 byte[] bytes = bout.toByteArray();
                 AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(bytes), audioFormat(), bytes.length);
                 AudioFormat audioFormat = audioInputStream.getFormat();
 
                 DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
 
                 SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
 
                sourceDataLine.open(audioFormat);
                sourceDataLine.start(); 

                 int bytesPerOneSecond = (int) (audioFormat.getFrameRate() * audioFormat.getFrameSize());
                 byte tempBuffer[] = new byte[bytesPerOneSecond];
                 
                 int cnt;
                 stopPlayback = false;
 
                while((cnt = audioInputStream.read(tempBuffer,0,tempBuffer.length)) != -1 && stopPlayback == false) {
                     if(cnt > 0){
                         sourceDataLine.write(tempBuffer, 0, cnt);
                     }
                }
 
                 if (stopPlayback == true) {
                     sourceDataLine.stop();
                     sourceDataLine.flush();
                     System.out.println("Stopped: " + System.currentTimeMillis());
                 } else {
                     sourceDataLine.drain();
                 }
                 sourceDataLine.close();
 
                 System.out.println("... finished");
 
                 stopPlayback = false;
                 notifyListeners(new RecordingEvent(true, false));
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 }
