 package vooga.fighter.view;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.util.Observable;
 import vooga.fighter.view.HUDElement;
 import util.Text;
 
 /**
  * Displays an integer number of seconds as a time in minutes and seconds.
  * 
  * @author Wayne You
  *
  */
 public class HUDTimer extends HUDElement {
     protected Text myTimerDisplay = new Text("");
 
     @Override
     public void update (Observable o, Object arg) {
         Integer time = 0;
         try {
             time = (Integer)this.getObservedValue(o);
         }
         catch (SecurityException e) {}
         catch (IllegalArgumentException e) {
             System.err.println("Expected Integer for HUDTimer");
         }
         catch (NoSuchFieldException e) {
             System.err.println(myFieldName + " is not a member of the class observed.");
         }
         catch (IllegalAccessException e) {
             System.err.println("Illegal access in HUDTimer.");
         }
         int minutes = time / 60;
         int seconds = time % 60;
         myTimerDisplay.setText(String.format("%03d", minutes) + ":" + String.format("%03d", seconds));
     }
 
    @Override
     public void paint (Graphics2D pen, Point2D center, Dimension size) {
         myTimerDisplay.paint(pen, center, java.awt.Color.BLACK);
     }
     
 }
