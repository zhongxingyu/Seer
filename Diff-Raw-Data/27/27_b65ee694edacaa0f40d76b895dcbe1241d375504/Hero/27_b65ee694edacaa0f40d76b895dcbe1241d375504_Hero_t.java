 package sandbox.core.entities;
 
 import static forplay.core.ForPlay.log;
 import sandbox.core.display.sprite.Animation;
 import sandbox.core.display.sprite.Sprite;
 import sandbox.core.display.sprite.SpriteLoader;
 import sandbox.core.display.sprite.Updatable;
 import sandbox.core.world.WorldObject;
 import forplay.core.GroupLayer;
 import forplay.core.Image;
 import forplay.core.ResourceCallback;
 
 public class Hero extends WorldObject implements Updatable {
 
     public static String TYPE = "hero";
     private static final float STEP = (float) 0.1;
     public static final int JUMP = 20;
     public static String JSON_IMAGE = "sprites/hero.json";
     private Sprite sprite;
 
     public float alpha;
 
     public boolean isIdle;
 
     public boolean isFacingLeft;
     public boolean isFacingRight;
 
     public boolean isMovingLeft;
     public boolean isMovingRight;
     public boolean isMovingUp;
     public boolean isMovingDown;
 
     private boolean isJumping;
     public boolean isJumpingRight;
     public boolean isJumpingLeft;
 
     public boolean isSlicing;
 
     public int speed;
 
     public float newX = 0;
     public float newY = 6;
 
     public float x = 0;
     public float y = 6;
 
     public boolean isFalling;
 
     private boolean isFallingRight;
     private boolean isFallingLeft;
 
     public boolean isFallingRight() {
         return isFallingRight;
     }
 
     public void setFallingRight(boolean isFallingRight) {
         this.isFallingRight = isFallingRight;
         if (isFallingRight) {
             sprite.setCurrentAnimation(null);
             sprite.setSprite("hero_jump_right1");
         }
     }
 
     public boolean isFallingLeft() {
         return isFallingLeft;
     }
 
     public void setFallingLeft(boolean isFallingLeft) {
         this.isFallingLeft = isFallingLeft;
         if (isFallingLeft) {
             sprite.setCurrentAnimation(null);
             sprite.setSprite("hero_jump_left1");
         }
     }
 
     public Hero(int x, int y) {
         this.x = x;
         this.y = y;
 
         sprite = SpriteLoader.getSprite(JSON_IMAGE);
 
         sprite.addCallback(new ResourceCallback<Sprite>() {
             @Override
             public void done(Sprite sprite) {
                 sprite.setSprite("hero_left");
             }
 
             @Override
             public void error(Throwable err) {
                 log().error("Error loading image!", err);
             }
         });
 
     }
 
     public Hero(final GroupLayer characterLayer, int x, int y) {
 
         sprite = SpriteLoader.getSprite(JSON_IMAGE);
         this.x = x;
         this.y = y;
 
         sprite.addCallback(new ResourceCallback<Sprite>() {
             @Override
             public void done(Sprite sprite) {
                 sprite.setSprite(0);
                 // sprite.layer().setTranslation(x, y);
                 characterLayer.add(sprite.layer());
             }
 
             @Override
             public void error(Throwable err) {
                 log().error("Error loading image!", err);
             }
         });
 
     }
 
     public void setPosition(float x, float y) {
         sprite.layer().setTranslation(x, y);
     }
 
     public void moveLeft() {
         newX = x - STEP;
 
         // System.out.println(newX);
         isMovingRight = false;
         isMovingLeft = true;
 
         if (!isJumping) {
             if (isFacingLeft) {
                 sprite.setCurrentAnimation("hero_walk_left");
                 sprite.getCurrentAnimation().start();
             } else {
                 // sprite.setSprite("hero_right");
             }
         }
 
         isFacingRight = false;
         isFacingLeft = true;
     }
 
     public void moveUp() {
         // newY = y - STEP;
         // isMovingUp = true;
         // isMovingDown = false;
         // System.out.println(newY);
         // y = y - STEP;
         // sprite.layer().setTranslation(x, y);
     }
 
     public void moveRight() {
         newX = x + STEP;
         // System.out.println(newX);
         isMovingRight = true;
         isMovingLeft = false;
 
         if (!isJumping) {
             if (isFacingRight) {
                 sprite.setCurrentAnimation("hero_walk_right");
                 sprite.getCurrentAnimation().start();
             } else {
                 // sprite.setSprite("hero_right");
             }
         }
 
         isFacingRight = true;
         isFacingLeft = false;
 
     }
 
     public void moveDown() {
         // newY = y + STEP;
         // isMovingUp = false;
         // isMovingDown = true;
         // System.out.println(newY);
         // y = y + STEP;
         // sprite.layer().setTranslation(x, y);
     }
 
     @Override
     public Image getImage() {
         // return sprite.current().image();
         return sprite.layer().image();
     }
 
     public void upLeft() {
         isMovingLeft = false;
         // FIXME is there a forplay bug ?
         // sprite.setCurrentAnimation(null);
         // sprite.setSprite("hero_left");
     }
 
     public void upUp() {
         isMovingUp = false;
     }
 
     public void upRight() {
         isMovingRight = false;
         // FIXME is there a forplay bug ?
         // sprite.setCurrentAnimation(null);
         // sprite.setSprite("hero_right");
     }
 
     public void upDown() {
         isMovingDown = false;
     }
 
     public void jump() {
         if (!isJumping && !isFalling) {
 
             if (isMovingRight) {
                 isJumpingRight = true;
                 sprite.setCurrentAnimation(null);
                 sprite.setSprite("hero_jump_move_right");
 
             } else if (isMovingLeft) {
                 isJumpingLeft = true;
                 sprite.setCurrentAnimation(null);
                 sprite.setSprite("hero_jump_move_left");
 
             } else {
 
                 // jump straight
                 if (isFacingLeft) {
                     sprite.setCurrentAnimation(null);
                     sprite.setSprite("hero_jump_left0");
                 } else {
                     sprite.setCurrentAnimation(null);
                     sprite.setSprite("hero_jump_right0");
                 }
             }
 
             setJumping(true);
             speed = JUMP;
 
         }
     }
 
     public void sword() {
         isSlicing = true;
 
         if (isFacingRight) {
             sprite.setCurrentAnimation("hero_sword_right");
         } else if (isFacingLeft) {
             sprite.setCurrentAnimation("hero_sword_left");
         }
         sprite.getCurrentAnimation().start();
     }
 
     @Override
     public boolean isCollidable() {
         return true;
     }
 
     @Override
     public String getType() {
         return TYPE;
     }
 
     public void dies() {
         sprite.setSprite("hero_die");
     }
 
     public void reset() {
         sprite.setSprite(0);
         setAlpha(1);
     }
 
     public void setAlpha(float alpha) {
         this.alpha = alpha;
         sprite.layer().setAlpha(alpha);
     }

     public void setJumping(boolean isJumping) {
         this.isJumping = isJumping;
         this.isMovingUp = isJumping;
     }

     public boolean isJumping() {
         return isJumping;
     }
 
     @Override
     public void update(float delta) {
         Animation currentAnimation = sprite.getCurrentAnimation();
 
         if (currentAnimation != null) {
             currentAnimation.update(delta);
             String currentFrameId = currentAnimation.getCurrentFrameId();
 
             if (currentFrameId != null) {
                 sprite.setSprite(currentFrameId);
             } else {
                 sprite.setCurrentAnimation(null);
 
                 isSlicing = false;
             }
         }
 
         // FIXME not very pretty ...
         // but keyUp() is problematic
         if ((!isMovingLeft) && (!isMovingRight) && (!isJumpingLeft) && (!isJumping) && (!isJumpingRight)
                && (!isFallingLeft) && (!isFallingRight) && (!isSlicing) && (!isFalling)) {
             if (isFacingLeft) {
                 sprite.setCurrentAnimation(null);
                 sprite.setSprite("hero_left");
 
             } else if (isFacingRight) {
                 sprite.setCurrentAnimation(null);
                 sprite.setSprite("hero_right");
 
             }
         }
 
     }
 
     public void makeFall() {
         setJumping(false);
 
         if (isJumpingRight) {
             setFallingRight(true);
             isJumpingRight = false;
         } else if (isJumpingLeft) {
             setFallingLeft(true);
             isJumpingLeft = false;
         }
 
        if (isFacingLeft) {
            sprite.setCurrentAnimation(null);
            sprite.setSprite("hero_jump_left1");
        } else if (isFacingRight) {
            sprite.setCurrentAnimation(null);
            sprite.setSprite("hero_jump_right1");
        }

         isFalling = true;
         isMovingDown = true;
         speed = Hero.JUMP;

     }
 
 }
