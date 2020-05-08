 package SpriteAction;
 
 import java.awt.Point;
 
 import com.golden.gamedev.object.Sprite;
 import sprites.HomingProjectile;
 import sprites.HomingEnemy;
 
 import sprites.GeneralSprite;
 
 public class Homing extends SpriteAction {
 
     public Homing(GeneralSprite s) {
         super(s);
     }
 
     @Override
     public void actionPerformed(Object object) {
         Sprite target = ((HomingEnemy)mySprite).getMyTarget();
        double x = target.getX()+(target.getWidth()/2);
        double y = target.getY()+(target.getHeight()/2);
        double myX = mySprite.getX();
        double myY = mySprite.getY();
         double angleToTarget = Math.atan2( (y-myY),(x-myX));
 
         angleToTarget = Math.toDegrees(angleToTarget);
 
         if(angleToTarget< 0){
             angleToTarget+=360;
         }
         angleToTarget+=90;
        if(angleToTarget>360){
            angleToTarget-=360;
        }
         mySprite.setMovement(.04, angleToTarget);
 
     }
 
 }
