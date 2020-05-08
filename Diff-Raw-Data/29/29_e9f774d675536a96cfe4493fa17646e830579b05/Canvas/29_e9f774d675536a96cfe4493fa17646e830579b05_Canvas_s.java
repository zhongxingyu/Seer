 package dungeon.ui;
 
 import dungeon.MoveEvent;
 import dungeon.events.Event;
 import dungeon.events.EventHandler;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class Canvas extends JPanel implements EventHandler {
   private static final int START_POSITION = 200;
  private int x = START_POSITION;
  private int y = START_POSITION;
 
   @Override
   public void handleEvent (Event event) {
     if (event instanceof MoveEvent) {
       move((MoveEvent)event);
     }
   }
 
   private void move (MoveEvent event) {
     switch (event) {
       case UP:
        x--;
         break;
       case DOWN:
        x++;
         break;
       case LEFT:
        y--;
         break;
       case RIGHT:
        y++;
         break;
       default:
     }
 
     repaint();
   }
 
   @Override
   protected void paintComponent (Graphics g) {
     super.paintComponent(g);
 
    g.drawLine(0, 0, x, y);
   }
 }
