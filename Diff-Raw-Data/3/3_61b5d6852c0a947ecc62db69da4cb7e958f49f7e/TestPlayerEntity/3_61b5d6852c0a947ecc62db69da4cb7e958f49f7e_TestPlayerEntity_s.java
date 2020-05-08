 package epsilon.map.entity;
 
 import epsilon.game.Collision;
 import epsilon.game.Input;
 import epsilon.game.Physics;
 import epsilon.game.Sprite;
 import epsilon.map.Map;
 import epsilon.map.ShotStore;
 import java.awt.Graphics;
 
 /**
  *
  * @author Marius
  */
 public class TestPlayerEntity extends MoveableEntity {
 
     // keeps track of when to change pictures in the sprite
     protected int ticker;
     // used for checking if the entity can jump
     private boolean touchesGround;
     // the name of the player
     private String name;
     private double origPosX;
     private double origPosY;
     // the different sprites this entity uses
     protected Sprite rightSprite;
     protected Sprite standSpriteRight;
     protected Sprite leftSprite;
     protected Sprite standSpriteLeft;
     protected boolean facingRight = true;
     
     private boolean isDead = false;
 
     private ShotStore shots;
     private int shotTimer;
     private int lastShot;
 
     private int hp;
 
     /**
      * Constructor for the entity that initialises sprites
      *
      * @param posX The starting X position of the entity
      * @param posY The starting Y position of the entity
      */
     public TestPlayerEntity(double posX, double posY, String name, Map m) {
         super(posX, posY, m);
         ticker = 0;
         touchesGround = false;
         origPosX = posX;
         origPosY = posY;
 
         this.name = name;
 
         // Create the different sprites used in this entity, and assign them hitboxes
         HitBox[] hitbox = new HitBox[1];
 
         hitbox[0] = new HitBox(37, 28, 20, 63);
 
         rightSprite = new Sprite(new String[]{"/pics/guy01.png", "/pics/guy02.png", "/pics/guy03.png", "/pics/guy04.png", "/pics/guy05.png"}, false, hitbox);
         standSpriteRight = new Sprite(new String[]{"/pics/guy01.png"}, false, hitbox);
         leftSprite = new Sprite(new String[]{"/pics/guy01.png", "/pics/guy02.png", "/pics/guy03.png", "/pics/guy04.png", "/pics/guy05.png"}, true, hitbox);
         standSpriteLeft = new Sprite(new String[]{"/pics/guy01.png"}, true, hitbox);
 
         currentSprite = standSpriteRight;
 
         shots = new ShotStore(mapReferance);
         lastShot = 0;
         shotTimer = 0;
     }
 
     @Override
     public void calculateMovement() {
 
         // handle input, and chose the right sprite for the job
         newPosX = posX;
         newPosY = posY;
 
         shotTimer++;
 
         // checking if the player is dead
         if (!isDead) {
             if (Input.get().right() && !Input.get().left()) {
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
 
             // shots
             if (Input.get().attack() && shotTimer - lastShot > 30) {
                 //sound.close();
                 addShot(0);
             }
             updateShots();
 
 
             // checking if the player has falled down below the floor threshold.
             // If player posY is larger or equal to 598, the player dies.
             if (posY >= 598) {
                 isDead = true;
             } // Handle falling
             else if (posY < 600 && !touchesGround) {
                 double temp = Physics.calculateGravity(posY, pposY, 16);
                 newPosY = posY - temp;
             } else if (Input.get().jump()) {
                 // if it touches the ground, jump!
                 newPosY -= 7;
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
     public double getXRenderPosition() {
         return posX - 400 + currentSprite.getWidth() / 2;
     }
 
     @Override
     public double getYRenderPosition() {
         //return posY - 300 + currentSprite.getHeight()/2;
         return 0;
     }
 
     @Override
     public void renderHitBox(Graphics g, double x, double y) {
 
         double posX = this.posX - x;
         double posY = this.posY - y;
 
         g.drawRect((int) posX, (int) posY, this.getWidth(), this.getHeight());
 
         HitBox[] hitbox = currentSprite.getHitBox();
 
         for (int i = 0; i < hitbox.length; i++) {
             hitbox[i].draw(g, posX, posY);
         }
     }
 
     @Override
     public void collided(Collision c) {
 
         if (c.collidedWith instanceof World || c.collidedWith instanceof TestNetworkEntity || c.collidedWith instanceof Enemy) {
 
             // overlap between the two entities in pixels
             double dty = c.deltaTop;
             double dby = c.deltaBottom;
 
             // movement if this entity collides on the left side of something
             if (c.crossedLeft && pposX < posX && dty > 8 && dby > 6) {
                 newPosX = pposX;
                 c = c.collidingEntity.collision(this);
             } else if (c.crossedRight && pposX > posX && dty > 8 && dby > 6) {
                 newPosX = pposX;
                 c = c.collidingEntity.collision(this);
             } else if (c.crossedTop && posY > pposY) {
                 newPosY -= dty;
                 posY = newPosY;
                 touchesGround = true;
             } else if (c.crossedBottom && posY < pposY) {
                 newPosY = pposY;
             }
         } else if (c.collidedWith instanceof Shot && ((Shot) c.collidedWith).getShooter() != this) {
             hp -= 20;
             if (hp <= 0) {
                 isDead = true;
             }
         }
     }
 
     /**
      * Getter method for the name
      *
      * @return the name of the player
      */
     public String getName() {
         return name;
     }
 
     public void resetPosition() {
         posX = origPosX;
         newPosX = origPosX;
         pposX = origPosX;
 
         posY = origPosY;
         newPosY = origPosY;
         pposY = origPosY;
 
         isDead = false;
     }
 
     public boolean isDead() {
         return isDead;
     }
 
     public int lastShot() {
         return lastShot;
     }
 
     protected void addShot(int lastShot) {
         if (facingRight) {
             shots.addShot(posX + 75, posY + 45, facingRight, this, mapReferance);
         } else {
             shots.addShot(posX + 15, posY + 45, facingRight, this, mapReferance);
 
         }
         if (lastShot != 0) {
             this.lastShot = lastShot;
         } else {
             this.lastShot = shotTimer;
         }
     }
 
     protected void updateShots() {
         shots.update();
     }
 }
