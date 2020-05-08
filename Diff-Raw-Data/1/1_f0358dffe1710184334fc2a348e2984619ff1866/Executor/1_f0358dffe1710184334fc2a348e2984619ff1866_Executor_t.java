 package com.udev.process;
 
 import com.udev.factory.FigureCreator;
 import com.udev.domain.Field;
 import com.udev.domain.figures.Figure;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author taipan
  *         Date: 04.05.13
  *         Time: 18:38
  */
 public class Executor {
 
     private static final Logger logger = LoggerFactory.getLogger(Executor.class);
 
     public static void main(String[] args) {
         logger.debug("Starting the application...");
         Field field = new Field();
         FigureCreator creator = null;
         FigureActionManager manager = new FigureActionManager();
         Figure figure = null;
         while (field.isNotFull()) {
             if (field.isPossibleMoveFigure()) {
                 try {
                    // TODO: Fix reading of the input data.
                     int ch = System.in.read();
                     while (ch == 49) {
                         manager.moveFigure(figure, field, FigureActionManager.Move.LEFT);
                         ch = System.in.read();
                     }
                     manager.moveFigure(figure, field, FigureActionManager.Move.DOWN);
                 } catch (IOException e) {
                     logger.error("Error during reading the input data.");
                     break;
                 }
             } else {
                 creator = manager.getCreator(0);
                 field.showData();
                 figure = creator.createFigure();
                 manager.addFigureToField(figure, field);
                 field.setPossibleMoveFigure(true);
             }
             field.showData();
             field.verifyFreeSpace();
         }
         System.out.println("You've won :)");
     }
 }
