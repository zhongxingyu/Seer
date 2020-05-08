 package com.github.dreamrec;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 
 /**
  *
  */
 public class DebugDataProvider implements IDataProvider {
 
    private int delay = 10; //milliseconds
     private Timer timer;
     private LinkedList<Integer> eyeDataQueue = new LinkedList<Integer>();
     private int sampleCounter;
     private long startTime;
 
     public DebugDataProvider() {
         ActionListener taskPerformer = new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 appendEyeDataSample();
             }
         };
         timer = new Timer(delay, taskPerformer);
     }
 
     private void appendEyeDataSample() {
         sampleCounter++;
         eyeDataQueue.add((int)(100*Math.sin(sampleCounter/10.0)));
     }
 
 
     public void startRecording() {
         timer.start();
         startTime = System.currentTimeMillis();
     }
 
     public void stopRecording() {
         timer.stop();
     }
 
     public double getIncomingDataFrequency() {
        return 1000/delay;
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public int poll() {
        return eyeDataQueue.poll();
     }
 
     public int available() {
         return eyeDataQueue.size();
     }
 }
