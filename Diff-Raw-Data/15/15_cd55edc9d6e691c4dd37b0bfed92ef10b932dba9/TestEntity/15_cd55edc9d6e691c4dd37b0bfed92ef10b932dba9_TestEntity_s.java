 package epsilon.map.entity;
 
 import epsilon.game.Collision;
 import epsilon.game.Physics;
 import epsilon.game.Sprite;
 import epsilon.game.Input;
 import java.awt.Graphics;
 
 /**
  * Test class that extends entity
  *
  * @author Marius
  */
 public class TestEntity extends MoveableEntity {
 
     // keeps track of when to change pictures in the sprite
     private int ticker;
 
     // used for checking if the entity can jump
     private boolean touchesGround;
 
     // the different sprites this entity uses
     private Sprite rightSprite;
     private Sprite standSpriteRight;
     private Sprite leftSprite;
     private Sprite standSpriteLeft;
     private boolean isDead = false;
     private boolean facingRight = true;
 
     /**
      * Constructor for the entity that initialises sprites
      *
      * @param posX The starting X position of the entity
      * @param posY The starting Y position of the entity
      */
     public TestEntity(int posX,int posY) {
         super(posX, posY);
         ticker = 0;
         touchesGround = false;
 
         // Create the different sprites used in this entity, and assign them hitboxes
         HitBox[] hitbox = new HitBox[3];
 
         hitbox[0] = new HitBox(37, 75, 17, 16);
         hitbox[1] = new HitBox(45,46,5,29);
         hitbox[2] = new HitBox(36,28,19,18);
 
         rightSprite = new Sprite(new String[]{"/pics/guy01.png","/pics/guy02.png","/pics/guy03.png","/pics/guy04.png","/pics/guy05.png"}, false, hitbox);
         standSpriteRight = new Sprite(new String[]{"/pics/guy01.png"}, false, hitbox);
         leftSprite = new Sprite(new String[]{"/pics/guy01.png","/pics/guy02.png","/pics/guy03.png","/pics/guy04.png","/pics/guy05.png"},true, hitbox);
         standSpriteLeft = new Sprite(new String[]{"/pics/guy01.png"},true, hitbox);
 
         currentSprite = standSpriteRight;
     }
 
     @Override
     public void calculateMovement() {
 
         // handle input, and chose the right sprite for the job
         newPosX = posX;
         newPosY = posY;
 
         // checking if the player is dead
         if (!isDead) {
             if(Input.get().right() && !Input.get().left()) {
                 if (currentSprite != rightSprite) {
                     newPosX += (currentSprite.getOffset());
                     currentSprite.resetImage();
                     currentSprite = rightSprite;
                     rightSprite.resetImage();
                     ticker = 0;
                     facingRight = true;
                 }
                 newPosX += 4;
             } else if (Input.get().left() && !Input.get().right()) {
                 if (currentSprite != leftSprite) {
                     newPosX += (currentSprite.getOffset());
                     currentSprite.resetImage();
                     currentSprite = leftSprite;
                     leftSprite.resetImage();
                     ticker = 0;
                     facingRight = false;
                 }
                 newPosX -= 4;
             } else {
                 if (currentSprite != standSpriteRight && currentSprite != standSpriteLeft) {
                     currentSprite.resetImage();
                     // if the guy was moving right, he should be face right when stopped
                     if (currentSprite == rightSprite) {
                         currentSprite = standSpriteRight;
                         standSpriteRight.resetImage();
                         facingRight = true;
                     } else { // last moved left, animation should be inverted
                         currentSprite = standSpriteLeft;
                         standSpriteLeft.resetImage();
                         facingRight = false;
                     }
                     ticker = 0;
                 }
             }
 
 
             // checking if the player has falled down below the floor threshold.
             // If player posY is larger or equal to 598, the player dies.
             if (posY>=598) {
                 System.out.print("You are dead!");
                 isDead = true;
             }
             // Handle falling
             else if (posY<600 && !touchesGround) {
                 double temp = Physics.calculateGravity(posY, pposY, 16);
                 newPosY = posY-temp;
             } else if (Input.get().jump()) {
                 // if it touches the ground, jump!
                 newPosY -= 6;
             }
 
             // go to the next picture in the sprite if it is time
             if (ticker < 5) {
                 ticker++;
             } else {
                 ticker = 0;
                 currentSprite.nextImage();
             }
 
             touchesGround = false;
         }
     }
 
     public boolean facingRight() {
         return facingRight;
     }
 
     @Override
     public double getXRenderPosition () {
         return posX - 400 + currentSprite.getWidth()/2;
     }
 
     @Override
     public double getYRenderPosition () {
         //return posY - 300 + currentSprite.getHeight()/2;
         return 0;
     }
 
     @Override
     public Collision collision(Entity entity) {
         return new Collision(); // yet to be implemented
     }
 
     @Override
     public void renderHitBox(Graphics g, double x, double y) {
 
         double posX = this.posX - x;
         double posY = this.posY - y;
 
         g.drawRect((int)posX, (int)posY, this.getWidth(), this.getHeight());
 
         HitBox[] hitbox = currentSprite.getHitBox();
 
         for (int i=0;i<hitbox.length;i++) {
             hitbox[i].draw(g, posX, posY);
         }
     }
 
     @Override
     public void collided(Collision c) {
 
         if (c.collidedWith instanceof World) {
 
             // overlap between the two entities in pixels
             double dlx = c.deltaLeft;
             double drx = c.deltaRight;
             double dty = c.deltaTop;
             double dby = c.deltaBottom;
 
             // movement if this entity collides on the left side of something
             if (c.crossedLeft && pposX < posX && dty > 8 && dby > 6) {
                 newPosX -= dlx;
             }
 
             // movement if this entity collides on the right side of something
             if (c.crossedRight && pposX > posX && dty> 8 && dby > 6) {
                 newPosX += drx;
             }
 
             // movement if it collides on the bottom of this entity
             if (c.crossedTop && posY > pposY && (drx > 8 && dlx > 8) ) {
                 newPosY -= dty;
                 touchesGround = true;
             }
 
             // movement if it collides on the top of this entity
             if (c.crossedBottom && posY < pposY && (drx > 8 && dlx > 8)) {
                pposY += dby;
             }
         }
     }
 }
