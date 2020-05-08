 package cc.game.TestGame.entities;
 
 import cc.game.TestGame.World;
 import org.newdawn.slick.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Calv
  * Date: 22/02/13
  * Time: 09:37
  * To change this template use File | Settings | File Templates.
  */
 public class Player extends Entity {
     private final float friction = 0.55f;
     private final float gravity = 0.73f;
     private final float horizontalSpeed = 1.45f;
 
     private static final int ANIMATION_RUNNING = 0;
     private static final int ANIMATION_IDLE = 1;
     private static final int ANIMATION_JUMPING = 2;
 
     private static int[] FRAME_COUNT = new int[]{
             4,  //Running
             4,  //Idle
             4   //Jumping
     };
     private static int[] FRAME_RATE = new int[]{
             83, //Running
             83, //Idle
             83  //Jumping
     };
     private boolean isJumping;
     private boolean isIdle;
 
     public Player(int posX, int posY, int SPRITE_WIDTH, int SPRITE_HEIGHT) {
         super(posX, posY, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, FRAME_RATE, 20f);
         this.SPRITE_WIDTH = SPRITE_WIDTH;
         this.SPRITE_HEIGHT = SPRITE_HEIGHT;
         this.setUpSpriteSheet("res/player.png");
     }
 
     public Player(int posX, int posY, int SPRITE_WIDTH, int SPRITE_HEIGHT, String imageLocation, int[] FRAME_COUNT, int[] FRAME_RATE) {
         super(posX, posY, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, FRAME_RATE, 20f);
         this.SPRITE_WIDTH = SPRITE_WIDTH;
         this.SPRITE_HEIGHT = SPRITE_HEIGHT;
         this.setUpSpriteSheet(imageLocation);
     }
 
     @Override
     public void update(GameContainer gc) {
         Input input = gc.getInput();
 
        if ((input.isKeyDown(Input.KEY_UP) || input.isKeyDown(Input.KEY_W) || input.isControllerUp(Input.ANY_CONTROLLER)) && (this.posY == World.getHeight() - this.boundingBox.getHeight()))
             this.velocity.y += 19f;
         if (input.isKeyDown(Input.KEY_DOWN) || input.isKeyDown(Input.KEY_S) || input.isControllerDown(Input.ANY_CONTROLLER))
             this.setCrouching(true);
         if (input.isKeyDown(Input.KEY_LEFT) || input.isKeyDown(Input.KEY_A) || input.isControllerLeft(Input.ANY_CONTROLLER))
             this.velocity.x -= horizontalSpeed;
         else
             this.setCrouching(false);
         if (input.isKeyDown(Input.KEY_RIGHT) || input.isKeyDown(Input.KEY_D) || input.isControllerRight(Input.ANY_CONTROLLER))
             this.velocity.x += horizontalSpeed;
 
 //        Sets the gravity, is the player is in the air, their velocity is affected by gravity, if the player is on the ground the velocity is zero.
        if (this.posY < World.getHeight() - this.boundingBox.getHeight())
             this.velocity.y -= gravity;
        else if (this.posY > World.getHeight() - this.boundingBox.getHeight()) {
             this.velocity.y = 0;
            this.posY = World.getHeight() - this.boundingBox.getHeight();
         }
 
         if (this.velocity.x > 0)
             this.velocity.x -= friction;
         if (this.velocity.x < 0)
             this.velocity.x += friction;
         if ((Math.abs(this.velocity.x) < Math.abs(friction)) && this.velocity.x != 0)
             this.velocity.x = 0;
 
        if (this.posX > World.getWidth() - this.boundingBox.getWidth()) {
            this.posX = (World.getWidth() - this.boundingBox.getWidth());
             this.velocity.x = 0;
         }
         else if (this.posX < 0) {
             this.posX = 0;
             this.velocity.x = 0;
         }
        if (this.posY > World.getHeight() + this.boundingBox.getHeight())
            this.posY = (World.getHeight() - this.boundingBox.getHeight());
 
         this.posX += this.velocity.x;
         this.posY -= this.velocity.y;
 
 //        System.out.println("x: " + this.velocity.x + "   y: " + this.velocity.y);
 
         this.boundingBox.setX(this.posX);
         this.boundingBox.setY(this.posY);
 
         isIdle = false;
         isJumping = false;
 
         if (this.velocity.y != 0)
             isJumping = true;
         if ((this.velocity.x == 0) && !isJumping)
             isIdle = true;
     }
 
     @Override
     public void render() {
         Animation animation;
         if (isJumping) animation = this.animations.get(ANIMATION_JUMPING);
         else if (isIdle) animation = this.animations.get(ANIMATION_IDLE);
         else animation = this.animations.get(ANIMATION_RUNNING);
         if (Math.min(velocity.x, 0) == 0) ; //running right
         else if (Math.max(velocity.x, 0) == 0) ; //running left
         animation.setAutoUpdate(!World.isPaused());
         animation.draw(this.posX, this.posY);
     }
 
     private void setCrouching(boolean isCrouching) {
         this.isCrouching = isCrouching;
     }
 
 }
