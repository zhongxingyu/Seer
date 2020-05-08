
 
 import java.awt.Color;
 import java.awt.geom.Line2D;
 
 /**
  * Basic projectile, as in just a line that will be shot.
  * @author Jere
  */
 public class BasicProjectile extends Projectile{
 
     final int HEIGHT = 5;
     final int WIDTH = 1;
 
     public BasicProjectile(int x, int y)
     {
         // FIXME Need to check the values for speed, damage, etc.
         super (x,y,10, 100, false,
                 new Line2D.Double(x,y,1,5),
                 Color.RED);
     }
 
     @Override
     public void move(int x, int y) {
         super.setX(x);
         super.setY(y);
         super.setShape(new Line2D.Double(x, y, WIDTH, HEIGHT));
     }
 
     @Override
     public void doMove() {
         
     }
 }
