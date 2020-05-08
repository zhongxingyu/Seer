 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entities;
 
 import org.cognitive.Graphics;
 import org.newdawn.slick.Color;
 import org.cognitive.Window;
 
 import java.util.LinkedList;
 import org.cognitive.Position;
 import org.lwjgl.opengl.GL11;
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  *
  * @author kristoffer
  */
 public class Unit extends TexturedEntity {
 
   private LinkedList<Order> orders = new LinkedList();
   private double movementRate = 0.05;
   private TilePosition tilePosition = new TilePosition();
   
   public Unit(double x, double y, String spriteName) {
     super(x, y, spriteName);
     this.sprite.animated = true;
     int animationID = this.sprite.addAnimation(); // Running left!
     this.sprite.addAnimationFrame(animationID, 0, 1);
     this.sprite.addAnimationFrame(animationID, 1, 1);
     this.sprite.addAnimationFrame(animationID, 2, 1);
     animationID = this.sprite.addAnimation(); // Running right!
     this.sprite.addAnimationFrame(animationID, 0, 3);
     this.sprite.addAnimationFrame(animationID, 1, 3);
     this.sprite.addAnimationFrame(animationID, 2, 3);
     animationID = this.sprite.addAnimation(); // Running down
     this.sprite.addAnimationFrame(animationID, 0, 0);
     this.sprite.addAnimationFrame(animationID, 1, 0);
     this.sprite.addAnimationFrame(animationID, 2, 0);
     animationID = this.sprite.addAnimation(); // Running up
     this.sprite.addAnimationFrame(animationID, 0, 2);
     this.sprite.addAnimationFrame(animationID, 1, 2);
     this.sprite.addAnimationFrame(animationID, 2, 2);
   }
 
   public void addOrder(double x, double y) {
     orders.add(new Order(x, y));
   }
 
   @Override
   public void update(int delta) {
     super.update(delta);
     tilePosition = identifyTile(pos.x, pos.y, 0, 0);
   }
 
   @Override
   public void draw() {
     super.draw();
     if (selected) {
       Graphics.drawString((int)pos.x+34, (int)pos.y, "Tile: " + tilePosition.x + ", " + tilePosition.y, Graphics.FontSize.Small, Color.yellow);
       
       if (orders.peek() != null) Graphics.drawString((int)pos.x+34, (int)pos.y+12, "Target: " + orders.peek().tileTarget.x + ", " + orders.peek().tileTarget.y,Graphics.FontSize.Small, Color.yellow);
       Graphics.drawLineBox(hitbox.x, hitbox.y, hitbox.width , hitbox.height, true);
    
      executeOrders();
     }
     if (orders.size() > 0) {
       glPushMatrix();
       Graphics.drawLineBox(10, 10, 10, 10, false);
       for (Order order : orders) {
       
       }
       glPopMatrix();
       
     }
   }
   
   public void executeOrders() {
     Order order = orders.peek();
     if (order != null) {
      // System.out.println("We have orders to move! Target tile: " + order.tileTarget.x + "," + order.tileTarget.y);
       TilePosition currentTile = identifyTile(pos.x, pos.y, 0, 0);
       if (currentTile.x == order.tileTarget.x && currentTile.y == order.tileTarget.y) {
        // System.out.println("Unit has reached destination!");
         //System.out.println("We are at tile: " + currentTile.x + ", " + currentTile.y);
         orders.remove(order);
         this.dx = 0;
         this.dy = 0;
       } 
       else { // We should be moving to the position then.
         // Major hack!
         //System.out.println("We are at tile: " + currentTile.x + ", " + currentTile.y);
         if (order.target.x > pos.x) {
           this.dx = 2 * movementRate;
         }
         if (order.target.x < pos.x) {
           this.dx = -2 * movementRate;
         }
         if (order.target.y > pos.y) {
           this.dy = 2 * movementRate;
         }
         if (order.target.y < pos.y) {
           this.dy = -2 * movementRate;
         }
       }
     }
   }
 
   public class TilePosition {
 
     public int x;
     public int y;
   }
   public class Order {
     public Position target;
     public TilePosition tileTarget;
 
     public Order(double x, double y) {
       target = new Position(x, y);
       tileTarget = identifyTile(x, y, 0, 0); // No camera yet, so 0,0 is topleft.
     }
 }
   public TilePosition identifyTile(double x, double y, int camerax1, int cameray1) {
     TilePosition tilePosition = new TilePosition();
     tilePosition.x = (int) (x + camerax1) / 32;
     tilePosition.y = (int) (y + cameray1) / 32;
     return tilePosition;
   }
 }
