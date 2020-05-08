 package com.udev.process;
 
 import com.udev.events.KeyboardEventDispatcher;
 import com.udev.events.KeyboardEventListener;
 import com.udev.events.PaintEventDispatcher;
 import com.udev.factory.FigureCreator;
 import com.udev.domain.Field;
 import com.udev.domain.figures.Figure;
 import com.udev.ui.TetrisForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.util.*;
 import java.util.Timer;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author taipan
  *         Date: 04.05.13
  *         Time: 18:38
  */
 public class Executor implements Runnable {
 
     /**
      * Logger.
      */
     private static final Logger logger = LoggerFactory.getLogger(Executor.class);
 
     /**
      * Creator of the figures.
      */
     private FigureCreator creator;
 
     /**
      * Dispatcher that notifies the form that data need to be repainted.
      */
     private PaintEventDispatcher dispatcher = new PaintEventDispatcher();
 
     /**
      * The field that contains data.
      */
     private Field field = new Field();
 
     /**
      * The current figure.
      */
     private Figure figure = null;
 
     /**
      * Manager of action with the figure.
      */
     private FigureActionManager manager = new FigureActionManager();
 
     /**
      * Generates different type of figures.
      */
     private Random rand = new Random();
 
     /**
      * Timer to move the figure downwards.
      */
     private Timer timer = new Timer();
 
     // TODO: Rotation.
     // TODO: JavaDocs
     // TODO: WIDTH and HEIGHT should be used instead of 10 and 20.
     /**
      *  Executes the logic
      */
     public void execute() {
         TetrisForm frame = new TetrisForm();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(30 * 10 + 100, 30 * 20 + 100);
         frame.setVisible(true);
 
         dispatcher.addEventListener(frame);
         final KeyListenerThread listenerThread = new KeyListenerThread();
         frame.getKeyboardEventDispatcher().addEventListener(listenerThread);
 
        creator = manager.getCreator(5);
         dispatcher.paintField(field);
 
         figure = creator.createFigure();
         boolean hasFreeSpace = manager.addFigureToField(figure, field);
         if (hasFreeSpace) {
             field.setPossibleMoveFigure(true);
         } else {
             field.setNotFull(hasFreeSpace);
         }
         dispatcher.paintField(field);
 
         listenerThread.start();
 
         TimerTask timerTask = new TimerTask() {
             @Override
             public void run() {
                 listenerThread.keyPressed(40);
             }
         };
         timer.schedule(timerTask, 0, 500);
     }
 
     @Override
     public void run() {
         execute();
     }
 
     public static void main(String[] args) {
         logger.debug("Starting the application...");
         Runnable exec = new Executor();
         ExecutorService executorService = Executors.newFixedThreadPool(1);
         executorService.execute(exec);
     }
 
     /**
      * Thread that listens is responsible for key handling.
      */
     private class KeyListenerThread extends Thread implements KeyboardEventListener {
         @Override
         public void keyPressed(int keyCode) {
             synchronized (Executor.this) {
                 if (field.isNotFull()) {
                     if (field.isPossibleMoveFigure()) {
                         if (keyCode == 37) {
                             manager.moveFigure(figure, field, FigureActionManager.Move.LEFT);
                         } else if (keyCode == 39) {
                             manager.moveFigure(figure, field, FigureActionManager.Move.RIGHT);
                         } else if (keyCode == 32) {
                             manager.moveFigure(figure, field, FigureActionManager.Move.FAST_DOWN);
                         } else if (keyCode == 38) {
                             manager.rotateFigure(figure, field);
                         } else {
                             manager.moveFigure(figure, field, FigureActionManager.Move.DOWN);
                         }
                         field.checkScores();
                         dispatcher.paintField(field);
                     } else {
                         creator = manager.getCreator(rand.nextInt(7));
                         dispatcher.paintField(field);
 
                         figure = creator.createFigure();
                         boolean hasFreeSpace = manager.addFigureToField(figure, field);
                         if (hasFreeSpace) {
                             field.setPossibleMoveFigure(true);
                         } else {
                             field.setNotFull(hasFreeSpace);
                         }
                     }
                 } else {
                     JOptionPane.showMessageDialog(null, "Your score is: " + field.getScores());
                     timer.cancel();
                 }
             }
         }
     }
 }
