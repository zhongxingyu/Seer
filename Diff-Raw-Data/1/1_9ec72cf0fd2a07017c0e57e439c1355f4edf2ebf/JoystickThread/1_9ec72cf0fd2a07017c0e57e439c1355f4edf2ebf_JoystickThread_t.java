 package ttProject.controller.main;
 
 import ttProject.controller.input.TTInput;
 import ttProject.controller.measurements.signals.Signal;
 import ttProject.model.logger.Logger;
 import ttProject.model.logger.LoggerManager;
 import ttProject.view.items.ItemsManager;
 import ttProject.view.items.Target;
 import ttProject.view.items.Tracker;
 import ttProject.view.listeners.ReactionThreadObserver;
 import ttProject.view.main.GraphicsPanel;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Michal
  * Date: 13.5.12
  * Time: 23:33
  * To change this template use File | Settings | File Templates.
  */
 public class JoystickThread extends Thread {
 
     private GraphicsPanel jPanel;
     private TTInput input;
     private Tracker tracker;
     private Target target;
     private Signal signal;
     private long cTime;
     private boolean fireButtonPressed;
     private long d1Time;
     private long d2Time;
     private Logger logger;
     private ReactionThread reactionThread;
 
     public JoystickThread(GraphicsPanel jPanel, TTInput input) {
         this.jPanel = jPanel;
         this.input = input;
         this.fireButtonPressed = false;
         this.logger = LoggerManager.getLoggerManager().getNormLogger();
         this.target = ItemsManager.getItemsManager().getTarget();
         this.tracker = ItemsManager.getItemsManager().getTracker();
     }
 
     public void setGraphicPanel(GraphicsPanel jPanel) {
         this.jPanel = jPanel;
     }
 
     public void setSignal(Signal signal) {
         this.signal = signal;
     }
 
     public void setFireButtonPressed(boolean fireButtonPressed) {
         this.fireButtonPressed = fireButtonPressed;
        jPanel.setFireButtonPressed(fireButtonPressed);
     }
 
     public long getcTime() {
         return cTime;
     }
 
     @Override
     public void run() {
 
         cTime = 0;
         d1Time = 0;
         d2Time = 0;
         jPanel.repaint();
         while (true) {
 
 
             if (!fireButtonPressed) {
                 checkFireButtonPressed();
                 resetVariables();
             }
             if (fireButtonPressed) {
                 d1Time = System.currentTimeMillis();
                 if (d2Time == 0) {
                     startReactionTest();
                 }
                 if (d2Time != 0) {
                     setTargetProperties();
                 }
                 setTrackerProperties();
                 logger.logValues(cTime, tracker, target);
                 jPanel.setTime(cTime);
                 jPanel.repaint();
                 d2Time = System.currentTimeMillis();
                 if (input.stopButtonPressed()) {
                     endMeasurement();
                 }
                 if (TimeUnit.MILLISECONDS.toMinutes(cTime) >= signal.getmTime()) {
                     endMeasurement();
                 }
 
             }
             try {
                 Thread.sleep(20);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
 
     }
 
     private void startReactionTest() {
         this.reactionThread = new ReactionThread();
         reactionThread.addObserver(new ReactionThreadObserver(this));
         reactionThread.start();
     }
 
     private void setTrackerProperties() {
         input.retrieveNewValues();
         tracker.setXincr(input.getX());
         tracker.setYincr(input.getY());
     }
 
     private void setTargetProperties() {
         cTime = (cTime + (d1Time - d2Time));
         target.setXincr(signal.getXCoordinate(cTime));
         target.setYincr(signal.getYCoordinate(cTime));
     }
 
     private void checkFireButtonPressed() {
         input.retrieveNewValues();
         this.fireButtonPressed = input.fireButtonPressed();
         jPanel.setFireButtonPressed(fireButtonPressed);
         jPanel.repaint();
     }
 
     public void endMeasurement() {
         this.fireButtonPressed = false;
         resetVariables();
         this.reactionThread.setAlive(false);
         target.setReactionActive(false);
         logToFile();
         logToReactionFile();
     }
 
     private void resetVariables() {
         this.cTime = 0;
         this.d1Time = 0;
         this.d2Time = 0;
         jPanel.resetStates();
         jPanel.repaint();
     }
 
     private void logToFile() {
         try {
             logger.logValuesToFile();
         } catch (IOException e) {
             JOptionPane.showMessageDialog(jPanel, "Cannot create logging file", "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private void logToReactionFile() {
 
         try {
             LoggerManager.getLoggerManager().getTtReactionLogger().logValuesToFile();
         } catch (IOException e) {
             JOptionPane.showMessageDialog(jPanel, "Cannot create logging file", "Error", JOptionPane.ERROR_MESSAGE);
         }
 
     }
 
 }
