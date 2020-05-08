 package MainPackage;
 
 import java.awt.*;
 import java.awt.geom.*;
 // import javax.vecmath.Vector2d;
 
 // @author Michael Kieburtz
 
 public class Renderer {
     
     private double radius;
     private double angle;
     
     public Renderer() {
         radius = 0;
         angle = 0;
     }
 
     public void drawScreen(Graphics g, Player player, int xRot, int yRot, Ellipse2D.Double playerCircle) {
         radius = playerCircle.getWidth() / 2;
         angle = player.getAngle();
         
         Graphics2D g2d = (Graphics2D) g; // turns it into 2d graphics
         AffineTransform origXform = g2d.getTransform();
         AffineTransform newXform = (AffineTransform) (origXform.clone());
 
         newXform.rotate(Math.toRadians(player.getAngle()), xRot, yRot);
         g2d.setTransform(newXform);
         
        g2d.drawLine(xRot, yRot, (int)(playerCircle.getCenterX() + radius + Math.sin(Math.toRadians(player.getAngle()))) - 7,
        (int)(playerCircle.getCenterY() + radius + Math.cos(Math.toRadians(player.getAngle()))) - 7);
         
         g2d.drawImage(player.getImage(), player.getLocation().x, player.getLocation().y, null);
         g2d.draw(playerCircle);
       
         
         /*
          * a point on the outer edge of a circle given the center of the circle
          * (cx, cy), the radius (r) and the angle where the ship is pointing
          * (a) is
          * x = cx + r + Math.cos(Math.toRadians(a));
          * y is cy + r + Math.sin(Math.toRadians(a));
          * 
          * 
          */
     }
}
