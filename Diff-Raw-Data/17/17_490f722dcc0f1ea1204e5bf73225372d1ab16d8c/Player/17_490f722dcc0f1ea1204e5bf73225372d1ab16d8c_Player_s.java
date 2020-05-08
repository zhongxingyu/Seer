 package MainPackage;
 import java.awt.*;
 import java.io.File;
 
 // @author Michael Kieburtz
 // might refractor to playerShip
 public class Player extends Ship {
     
     private String name;
    private double angle = 135; // maybe move to Ship Class
     
     public String getName()
     {
         return this.name;
     }
     
     public Player(int x, int y, Type shipType)
     {
         location = new Point(x, y);
         type = shipType;
         imageFile = new File("resources/FighterGrey.png");
         setUpShipImage();
     }
     
     
     public Point getLocation()
     {
         return location;
     }
     
     public void moveTo(int x, int y)
     {
         location.move(x, y);
     }
     
     public void moveRelitive(int dx, int dy)
     {
         location.move(location.x + dx, location.y + dy);
     }
 
     public Player() {
     }
     
     public void rotate(boolean positive)
     {
         if (positive)
             angle += 5;
         else if (!positive && angle == 0)
         {
             angle = 360;
             angle -= 5;
         }
         else
             angle -= 5;
             
         
         if (angle == 360)
             angle = 0;
         
     }
     
     public double getAngle()
     {
         return angle;
     }
 }
