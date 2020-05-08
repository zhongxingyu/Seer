 package dungeon.ui;
 
 import dungeon.MoveEvent;
 import dungeon.events.Event;
 import dungeon.events.EventHandler;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class Canvas extends JPanel implements EventHandler {
   private static final int START_POSITION = 200;

  private Point point = new Point(START_POSITION, START_POSITION);
 
   @Override
   public void handleEvent (Event event) {
     if (event instanceof MoveEvent) {
       move((MoveEvent)event);
     }
   }
 
   private void move (MoveEvent event) {
     switch (event) {
       case UP:
        point.y--;
         break;
       case DOWN:
        point.y++;
         break;
       case LEFT:
        point.x--;
         break;
       case RIGHT:
        point.x++;
         break;
       default:
     }
 
     repaint();
   }
 
   @Override
   protected void paintComponent (Graphics g) {
     super.paintComponent(g);
 
    g.drawLine(0, 0, point.x, point.y);
   }
 }
