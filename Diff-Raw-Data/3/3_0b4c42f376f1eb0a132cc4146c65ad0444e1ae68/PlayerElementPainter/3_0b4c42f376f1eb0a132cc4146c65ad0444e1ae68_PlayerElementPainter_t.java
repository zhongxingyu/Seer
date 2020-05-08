 package bdash.view;
 
 import bdash.model.*;
 
 import java.awt.*;
 
 public class PlayerElementPainter extends BlueSelectionPainter {
     public static PlayerElementPainter INSTANCE = new PlayerElementPainter();
 
     private PlayerElementPainter() {}
 
     public void paint(Graphics2D g, CaveElement e) {
         int offsetX = e.getX() * 30;
         int offsetY = e.getY() * 30;
 
         g.setColor(Color.GREEN);
         g.drawOval(offsetX, offsetY, 30, 30);
     }
 }
