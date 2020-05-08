 package com.macbury.unamed.component;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.macbury.unamed.Core;
 import com.macbury.unamed.ImagesManager;
 import com.macbury.unamed.level.LiquidBlock;
 import com.macbury.unamed.level.PassableBlock;
 
 public class CharacterAnimation extends RenderComponent {
   public final static int SPRITE_TILE_WIDTH  = 32;
   public final static int SPRITE_TILE_HEIGHT = 32;
   public final static int ANIMATION_SPEED    = 150;
   
   public final static byte FRAME_IDLE        = 1;
   public final static byte FRAME_LEFT_LEG    = 0;
   public final static byte FRAME_RIGHT_LEG   = 2;
   
   SpriteSheet spriteSheet;
   
   TileBasedMovement tileBasedMovement;
   
   private Animation walkingDown      = null;
   private Animation walkingLeft      = null;
   private Animation walkingRight     = null;
   private Animation walkingTop       = null;
   private Animation currentAnimation = null;
   
   boolean lastMovingStatus           = true;
   
   public void loadCharacterImage(String characteFileName) throws SlickException{
     spriteSheet = ImagesManager.shared().getSpriteSheet(characteFileName+".png", SPRITE_TILE_WIDTH, SPRITE_TILE_HEIGHT);
     buildAnimation();
     
     tileBasedMovement = (TileBasedMovement) this.owner.getComponent(TileBasedMovement.class);
     if (tileBasedMovement == null) {
       throw new SlickException("First include TileBasedMovement class into your entity!");
     }
   }
   
   private void buildAnimation() {
     walkingDown  = new Animation(getFramesForRow(0), ANIMATION_SPEED);
     walkingLeft  = new Animation(getFramesForRow(1), ANIMATION_SPEED);
     walkingRight = new Animation(getFramesForRow(2), ANIMATION_SPEED);
     walkingTop   = new Animation(getFramesForRow(3), ANIMATION_SPEED);
     
     walkingDown.setAutoUpdate(false);
     walkingLeft.setAutoUpdate(false);
     walkingRight.setAutoUpdate(false);
     walkingTop.setAutoUpdate(false);
   }
   
   public Image[] getFramesForRow(int y) {
     Image idle     = spriteSheet.getSprite(FRAME_IDLE,      y);
     Image leftLeg  = spriteSheet.getSprite(FRAME_LEFT_LEG,  y);
     Image rightLeg = spriteSheet.getSprite(FRAME_RIGHT_LEG, y);
     
     return new Image[] { idle, leftLeg, idle, rightLeg };
   }
   
   @Override
   public void update(GameContainer gc, StateBasedGame sb, int delta) {
     if (tileBasedMovement == null) {
       return;
     }
     
     Animation targetAnimation = null;
     
     switch (tileBasedMovement.direction) {
       case TileBasedMovement.DIRECTION_LEFT:
         targetAnimation = walkingLeft;
       break;
       
       case TileBasedMovement.DIRECTION_TOP:
         targetAnimation = walkingTop;
       break;
       
       case TileBasedMovement.DIRECTION_RIGHT:
         targetAnimation = walkingRight;
       break;
 
       default:
         targetAnimation = walkingDown;
       break;
     }
     
     if (targetAnimation != currentAnimation) {
       walkingLeft.stop();
       walkingTop.stop();
       walkingRight.stop();
       walkingDown.stop();
       if (currentAnimation != null) {
         currentAnimation.stop();
         currentAnimation.setCurrentFrame(0);
         lastMovingStatus = !lastMovingStatus;
       }
       
       currentAnimation = targetAnimation;
       currentAnimation.restart();
     }
     
    if(!lastMovingStatus && tileBasedMovement.isMoving()) {
      currentAnimation.setCurrentFrame(0);
       currentAnimation.restart();
      lastMovingStatus = true;
    } else if(lastMovingStatus && !tileBasedMovement.isMoving()) {
       currentAnimation.stop();
       currentAnimation.setCurrentFrame(0);
      lastMovingStatus = false;
     }
     
     currentAnimation.update(delta);
   }
 
   @Override
   public void render(GameContainer gc, StateBasedGame sb, Graphics gr) {
     if (currentAnimation != null) {
       Image image = currentAnimation.getCurrentFrame();
       LiquidBlock block = this.owner.getBlock().getAsLiquidBlock();
       if (block != null) {
         int diveLevel = Math.round(Core.TILE_SIZE * block.divePower());
         image.draw(0, 0, Core.TILE_SIZE, diveLevel, 0, 0, Core.TILE_SIZE, diveLevel);
       } else {
         image.draw();
       }
       
     }
   }
 
 }
