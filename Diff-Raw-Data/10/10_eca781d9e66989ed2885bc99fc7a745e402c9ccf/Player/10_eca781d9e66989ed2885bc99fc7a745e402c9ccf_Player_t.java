 package MainPackage;
 
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.io.File;
 import java.util.ArrayList;
 
 // @author Michael Kieburtz and Davis Freeman
 // might refractor to playerShip
 public class Player extends Ship {
 
     private String name;
     private double faceAngle = 360.0; // maybe move to Ship Class
     private double moveAngle = 0.0;
     private final double maxVel = 5.0;
     private final double angleIcrement = 4;
     private Point2D.Double velocity = new Point2D.Double(0, 0);
     private final double acceleration = .2;
     private final double deacceleration = .05;
 
     public String getName() {
         return this.name;
     }
 
     public Player(int x, int y, Type shipType) {
         location = new Point2D.Double(x, y);
         nextLocation = new Point2D.Double();
         type = shipType;
         imageFiles.add(new File("resources/FighterGreyIdle.png"));
         imageFiles.add(new File("resources/FighterGreyMoving.png"));
         imageFiles.add(new File("resources/FighterGreyTurningLeft.png"));
         imageFiles.add(new File("resources/FighterGreyTurningRight.png"));
        imageFiles.add(new File("resources/FillerBackground.png"));
         setUpShipImage();
         activeImage = images.get(0);
     }
 
     public Point2D.Double getLocation() {
         return location;
     }
 
     public void moveTo(double x, double y) {
         location.x = x;
         location.y = y;
     }
 
     public void moveTo(Point2D.Double location) {
         this.location.x = location.x;
         this.location.y = location.y;
     }
 
     public void moveRelitive(double dx, double dy) {
         this.location.x += dx;
         this.location.y += dy;
     }
 
     public void rotate(boolean positive) {
         if (positive) {
             faceAngle += angleIcrement;
             if (faceAngle > 360) {
                 faceAngle = angleIcrement;
             }
         } else {
             faceAngle -= angleIcrement;
             if (faceAngle <= 0) {
                 faceAngle = 360 - angleIcrement;
             }
         }
     }
 
     public void rotate(double amount) {
         faceAngle = amount;
     }
 
     public void move(boolean slowingDown) {
         moveAngle = faceAngle - 90;
 
         if (!slowingDown) {
             velocity.x += CalcAngleMoveX(moveAngle) * acceleration;
 
             if (velocity.x > maxVel) {
                 velocity.x = maxVel;
             } else if (velocity.x < -maxVel) {
                 velocity.x = -maxVel;
             }
 
             velocity.y += CalcAngleMoveY(moveAngle) * acceleration;
 
             if (velocity.y > maxVel) {
                 velocity.y = maxVel;
             } else if (velocity.y < -maxVel) {
                 velocity.y = -maxVel;
             }
 
         } else // the ship is slowing down, the keybind for the forward key is not being pressed.
         {
             if (velocity.x == 0 && velocity.y == 0) {
                 return;
             }
 
             if (velocity.x > 0) {
 
                 if (velocity.x - deacceleration < 0) {
                     velocity.x = 0;
                 } else {
                     velocity.x -= deacceleration;
                 }
 
             } else if (velocity.x < 0) {
 
                 if (velocity.x + deacceleration > 0) {
                     velocity.x = 0;
                 } else {
                     velocity.x += deacceleration;
                 }
 
             }
 
             if (velocity.y > 0) {
 
                 if (velocity.y - deacceleration < 0) {
                     velocity.y = 0;
                 } else {
                     velocity.y -= deacceleration;
                 }
 
             } else if (velocity.y < 0) {
                 if (velocity.y + deacceleration > 0) {
                     velocity.y = 0;
                 } else {
                     velocity.y += deacceleration;
                 }
             }
 
         }
 
         updatePosition();
     }
 
     private void updatePosition() {
         location.x += velocity.x;
         location.y += velocity.y;
     }
 
     public double getAngle() {
         return faceAngle;
     }
 
     public Point2D.Double getVel()
     {
         return this.velocity;
     }
     
     public void setVel(int vert, int hor)
     {
         this.velocity.x = vert;
         this.velocity.y = hor;
     }
 
     private double CalcAngleMoveX(double angle) {
         return (double) (Math.cos(angle * Math.PI / 180));
     }
 
     private double CalcAngleMoveY(double angle) {
         return (double) (Math.sin(angle * Math.PI / 180));
     }
 
     public ArrayList getImages() {
        return images;
     }
 }
