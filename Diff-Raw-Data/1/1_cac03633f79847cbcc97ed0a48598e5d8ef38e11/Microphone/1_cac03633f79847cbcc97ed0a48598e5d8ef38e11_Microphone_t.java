 package tazam;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.*;
 import javax.swing.JFileChooser;
 
 public class Microphone {
 
     AudioFileFormat.Type aFF_T = AudioFileFormat.Type.WAVE;
     AudioFormat aF = new AudioFormat(8000.0F, 16, 1, true, false);
     TargetDataLine tD;
     File f;// = new File("Grabacio.wav");
 //    OutputStream out = new ByteArrayOutputStream();
     boolean running = true;
     CapThread capThread;// = new CapThread();
 
     public void stopRunning() {
         this.running = false;
         tD.close();
         StartFrame.getArea().append("\nGravació acabada!");
     }
 
     public boolean getRunning() {
         return running;
     }
     
     public Microphone() {
         DataLine.Info dLI = new DataLine.Info(TargetDataLine.class, aF);
         try {
             tD = (TargetDataLine) AudioSystem.getLine(dLI);
         } catch (LineUnavailableException ex) {
             Logger.getLogger(Microphone.class.getName()).log(Level.SEVERE, null, ex);
         }
        capThread = new CapThread();
     }
 
 //    public Microphone() {
 //        try {
 //            DataLine.Info dLI = new DataLine.Info(TargetDataLine.class, aF);
 //            tD = (TargetDataLine) AudioSystem.getLine(dLI);
 //            
 //            CapThread capThread = new CapThread();
 //            capThread.start();
 //            Thread.sleep(1);
 //            tD.close();
 //            stopRunning();
 //
 //        } catch (LineUnavailableException | InterruptedException e) {
 //        }
 //    }
     
     public void startRecording() {
 //        try {
 //            DataLine.Info dLI = new DataLine.Info(TargetDataLine.class, aF);
 //            tD = (TargetDataLine) AudioSystem.getLine(dLI);            
               capThread.start();
 //            Thread.sleep(1);
 //            tD.close();
 //            stopRunning();
 //        } catch (InterruptedException e) {
 //        }
     }
     
     /**
      * Save file dialog recorded with a microphone
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public void saveFileRecorded() throws FileNotFoundException, IOException, InterruptedException {
         JFileChooser fc = new JFileChooser();
         int res = fc.showSaveDialog(null);
         if (res == JFileChooser.APPROVE_OPTION) {
             String path = fc.getSelectedFile().getPath(); 
             if (!path.endsWith(".wav")) {
                 path += ".wav";
             }
             File filename = new File(path);
             if (filename != null) {
                 this.f = filename;
                 // start recording
                 StartFrame.getArea().append("\nGravant des del micròfon...");
                 startRecording();
             }
         }
     }
 
     private class CapThread extends Thread {
         @Override
         public void run() {
             try {
                 while (getRunning()) {
                     tD.open(aF);
                     tD.start();
                     AudioSystem.write(new AudioInputStream(tD), aFF_T, f);
 //                    Thread.sleep(1);
 //                    stopRunning();
                 }
             } catch (LineUnavailableException | IOException e) {
             }
         }
     }
 }
