 package demo.custom;
 
 import io.annotations.Modifiable;
 
 import com.golden.gamedev.GameObject;
 import com.golden.gamedev.object.Timer;
 
 import core.characters.GameElement;
 import core.characters.Character;
 import core.configuration.key.KeyAnnotation;
 import core.physicsengine.physicsplugin.PhysicsAttributes;
 
 /**
  * @author Kuang Han
  */
 @SuppressWarnings("serial")
 public class Mario extends Character {
 
     private static final String IMAGE_FILE = "resources/Mario1.png";
     private double strengthUp, strengthDown, strengthLeft, strengthRight;
 
     private boolean jumpEnable, reversed;
     private Timer jumpTimer;
     @Modifiable(classification = "Gameplay")
     private int jumpTime;
 
     public Mario(GameObject game, PhysicsAttributes physicsAttribute) {
         super(game, physicsAttribute);
         setImages(VoogaUtilities.getImages(IMAGE_FILE, 1, 1));
         resetStrength();
         setMaximumSpeedInX(0.8);
         jumpTime = 220;
         jumpTimer = new Timer(jumpTime);
         jumpTimer.setActive(false);
         reversed = false;
         addAttribute("hitPoints", 10);
         addAttribute("points", 0);
         addAttribute("lives", 3);
         setTag("Mario");
     }
 
     @Override
     public void update(long t) {
         super.update(t);
         resetStrength();
         if (jumpTimer.action(t)) {
             jumpTimer.setActive(false);
             jumpEnable = false;
         }
     }
 
     @KeyAnnotation(action = "sequence")
     public void sequenceKey() {
         setImages(myGame.getImages("resources/Mushroom.png", 1, 1));
     }
 
     @KeyAnnotation(action = "up")
     public void keyUpPressed() {
         giveStrengthUp();
     }
 
     @KeyAnnotation(action = "down")
     public void keyDownPressed() {
         addAcceleration(0, strengthDown * -this.getPhysicsAttribute().getGravitationalAcceleration());
     }
 
     @KeyAnnotation(action = "left")
     public void keyLeftPressed() {
         addAcceleration(strengthLeft * -Math.abs(this.getPhysicsAttribute().getGravitationalAcceleration()), 0);
     }
 
     @KeyAnnotation(action = "right")
     public void keyRightPressed() {
         addAcceleration(strengthRight * Math.abs(this.getPhysicsAttribute().getGravitationalAcceleration()), 0);
     }
 
     @KeyAnnotation(action = "space")
     public void keySpacePressed() {
         useWeapon();
     }
 
     public void specialSkill() {
     }
 
     public void giveStrengthUp() {
         if (!jumpEnable)
             return;
         else {
             if (jumpTimer.isActive() == false) {
                 jumpTimer.setActive(true);
                 jumpTimer.refresh();
             }
             addAcceleration(0, strengthUp * this.getPhysicsAttribute().getGravitationalAcceleration());
         }
     }
 
     public void setStrengthUp(double s) {
         strengthUp = s;
     }
 
     public void setStrengthDown(double s) {
         strengthDown = s;
     }
 
     public void setStrengthLeft(double s) {
         strengthLeft = s;
     }
 
     public void setStrengthRight(double s) {
         strengthRight = s;
     }
 
     public void setStrength(double s) {
         setStrengthDown(s);
         setStrengthLeft(s);
         setStrengthRight(s);
         setStrengthUp(s);
     }
 
     // this is only used for swimming
     public void resetStrength() {
         strengthUp = 2;
         strengthDown = 0;
         strengthLeft = strengthRight = 1;
     }
 
     @Override
     public void afterHitFromTopBy(GameElement e, String tag) {
         if (tag.equals("Water")) {
             this.handleWaterCollision((Water)e);
         }
         else if (reversed) {
             jumpEnable = true;
             jumpTimer.setActive(false);
         }
     }
 
     @Override
     public void afterHitFromBottomBy(GameElement e, String tag) {
         if (tag.equals("Water")) {
             this.handleWaterCollision((Water)e);
         }
         else if (!reversed) {
             jumpEnable = true;
             jumpTimer.setActive(false);
         }
     }
 
     @Override
     public void afterHitFromLeftBy(GameElement e, String tag) {
         if (tag.equals("Water")) {
             this.handleWaterCollision((Water)e);
         }
     }
 
     @Override
     public void afterHitFromRightBy(GameElement e, String tag) {
         if (tag.equals("Water")) {
             this.handleWaterCollision((Water)e);
         }
     }
 
     public void setReversed(boolean b) {
         reversed = b;
     }
 
     private void handleWaterCollision(Water w) {
         jumpEnable = true;
         jumpTimer.setActive(false);
         setStrength(w.getStrength());
     }
 
 }
