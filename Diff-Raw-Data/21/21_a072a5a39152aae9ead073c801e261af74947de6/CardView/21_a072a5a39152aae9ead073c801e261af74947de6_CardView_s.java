 package gui;
 
import animation.GameObject;
import animation.Viewport;
 import brawllogic.Card;
 import brawllogic.Fighter;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 /**
  * @author Patrick Herrmann
  */
 public class CardView extends GameObject {
 
     public static final double MOVEMENT_RATE = 20.0;
     public static final double ROTATION_RATE = 0.04;
     private boolean visible = false;
     private Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, 100, 200);
     private double rotation = 0;
     private volatile Rectangle2D.Double targetRect = new Rectangle2D.Double(0, 0, 200, 300);
     private volatile double targetRotation = 0;
     private Card card;
     private Fighter fighter;
     private Image image = null;
 
     public CardView(Card card, Fighter fighter) {
         this.card = card;
         this.fighter = fighter;
 
         try {
             image = ImageIO.read(new File("image.jpg"));
         } catch (IOException e) {
         }
     }
 
     @Override
     public void paint(Viewport v) {
 
         if (visible) {
 
             Point centerPixel = v.getPixel(new Point2D.Double(rect.x, rect.y));
             int width = (int) rect.width;
             int height = (int) rect.height;
 
             Graphics2D g = v.getGraphics();
 
             g.translate(centerPixel.x, centerPixel.y);
             g.rotate(-rotation);
 
             // draw the image
 //            g.setColor(card.isFaceUp() ? Color.LIGHT_GRAY : Color.DARK_GRAY);
 //            g.fillRect(-width / 2, -height / 2, width, height);
 //            g.setColor(Color.DARK_GRAY);
 //            g.drawRect(-width / 2, -height / 2, width, height);
 //            g.drawString(card.getShorthand(), 0, 0);
             g.drawImage(image, -width / 2, -height / 2, width, height, null);
             g.setColor(Color.RED);
             g.drawString(card.getShorthand(), 0, 0);
 
             g.rotate(rotation);
             g.translate(-centerPixel.x, -centerPixel.y);
 
         }
     }
 
     private double moveToward(double from, double to, double rate) {
         double diff = to - from;
         if (Math.abs(diff) < rate) {
             return to;
         } else if (diff > 0) {
             return from + rate;
         } else {
             return from - rate;
         }
     }
 
     @Override
     public void update() {
 
         if (visible) {
 
             double theta = Math.atan2(targetRect.y - rect.y, targetRect.x - rect.x);
 
             rect.x = moveToward(rect.x, targetRect.x, Math.abs(MOVEMENT_RATE * Math.cos(theta)));
             rect.y = moveToward(rect.y, targetRect.y, Math.abs(MOVEMENT_RATE * Math.sin(theta)));
             rotation = moveToward(rotation, targetRotation, ROTATION_RATE);
         }
     }
 
     public void setPosition(double x, double y) {
         if (!visible) {
             rect.x = x;
             rect.y = y;
         }
         targetRect.x = x;
         targetRect.y = y;
     }
 
     public void setRotation(double target) {
         if (!visible) {
             rotation = target;
         }
         this.targetRotation = target;
     }
 
     public void setVisibility(boolean visible) {
         this.visible = visible;
     }
 }
