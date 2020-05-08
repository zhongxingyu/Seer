 package vooga.scroller.sprites.test_sprites;
 
 import java.awt.Dimension;
 import util.Location;
 import vooga.scroller.level_editor.ISpriteLibrary;
 import vooga.scroller.sprites.movement.LeftAndRight;
 import vooga.scroller.sprites.movement.TrackPlayer;
 import vooga.scroller.sprites.movement.UpAndDown;
 import vooga.scroller.sprites.superclasses.NonStaticEntity;
 import vooga.scroller.sprites.superclasses.StaticEntity;
 import vooga.scroller.util.Pixmap;
 import vooga.scroller.util.Sprite;
 import util.Vector;
 
 
 /**
  * This class is a convenient way to gather all the classes and use reflection
  * to retrieve all classes. Not sure if this is the best way to implement this.
  * But it is definitely better than having to parse the package. -DF
  * The Classes are static to allow instantiation w/o an instance of MarioLib.
  * TODO - Decide Whether to keep this implementation or switch to ENUM
  * Moreover, it is an example of the sprite-specification file the game designer
  * will need to provide.
  */
 public class MarioLib implements ISpriteLibrary {
     private static final Dimension DEFAULT_SIZE = new Dimension(32, 32);
     private static final Location DEFAULT_LOC = new Location(32, 32);
 
     public static class Coin extends StaticEntity {
 
        private static final String DEFAULT_IMG = "coin.gif";
         private static final int DEFAULT_COIN_VALUE = 900;
 
         public Coin () {
             this(DEFAULT_LOC);
         }
 
         public Coin (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, DEFAULT_SIZE);
         }
 
         public void print () {
             System.out.println("Coin");
         }
 
         /**
          * Gives a value of the coin
          * 
          * @return an integer that represents the 
          */
         public int getValue () {
             
             return DEFAULT_COIN_VALUE;
         }
     }
     
     public static class LilMario extends NonStaticEntity {
         
         private static final String DEFAULT_IMG = "lilMario.png";
         private static final Dimension LILMARIO_SIZE = new Dimension(32, 32);
 
         public LilMario () {
             this(DEFAULT_LOC);
             
         }
         
         public LilMario (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, LILMARIO_SIZE);
         }
     }
     
  public static class BigMario extends NonStaticEntity {
         
         private static final String DEFAULT_IMG = "bigMario.png";
         private static final Dimension BIGMARIO_SIZE = new Dimension(32, 64);
 
         public BigMario () {
             this(DEFAULT_LOC);
             
         }
         
         public BigMario (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, BIGMARIO_SIZE);
         }
     }
 
     public static class Koopa extends NonStaticEntity {
 
         private static final String DEFAULT_IMG = "koopa.png";
         private static final Dimension KOOPA_SIZE = new Dimension(32, 64);
 
         public Koopa () {
             this(DEFAULT_LOC);
         }
 
         public Koopa (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, KOOPA_SIZE);
         }
 
         public void print () {
             System.out.println("Koopa");
         }
 
         public void update (double elapsedTime, Dimension bounds) {
             TrackPlayer movement = new TrackPlayer(this);
             changeVelocity(movement.execute(45, 100, getPlayer())); // want to make this call every
                                                                     // X seconds
             super.update(elapsedTime, bounds);
         }
 
     }
 
     public static class Turtle extends NonStaticEntity {
 
         private static final String DEFAULT_IMG = "turtle.gif";
         private int health = 1;
 
         public Turtle () {
             this(DEFAULT_LOC);
         }
 
         public Turtle (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, DEFAULT_SIZE);
             // TODO Auto-generated constructor stub
         }
 
         public void print () {
             System.out.println("Turtle");
         }
 
         public void update (double elapsedTime, Dimension bounds) {
             // changeVelocity(trackPlayer(70, 150)); //want to make this call every X seconds
             super.update(elapsedTime, bounds);
         }
 
     }
 
     public static class Platform extends StaticEntity {
 
         private static final String DEFAULT_IMG = "block.png";
 
         public Platform () {
             this(DEFAULT_LOC);
         }
 
         public Platform (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, DEFAULT_SIZE);
         }
         public Platform (String img, Location center, Dimension size) {
             super(new Pixmap(img), center, size);
         }
 
         public void print () {
             System.out.println("Platform");
         }
     }
     
     public static class Plant extends StaticEntity {
         private static final String DEFAULT_IMG = "plant.png";
         
         public Plant () {
             this(DEFAULT_LOC);
         }
 
         public Plant (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, new Dimension(32, 32));
         }
 
     }
 
     /**
      * Represents a moving platform that moves in the up/down direction
      * 
      * @author Jay Wang
      */
     public static class MovingPlatformOne extends NonStaticEntity {
 
         private static final String DEFAULT_IMG = "platform.gif";
         private static final int DEFAULT_SPEED = 60;
         private static final Vector DEFAULT_VELOCITY = new Vector(Sprite.DOWN_DIRECTION,
                                                                   DEFAULT_SPEED);
 
         public MovingPlatformOne () {
             this(DEFAULT_LOC);
         }
 
         public MovingPlatformOne (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, new Dimension(96, 32));
             this.changeVelocity(DEFAULT_VELOCITY);
         }
 
         public void update (double elapsedTime, Dimension bounds) {
             UpAndDown movement = new UpAndDown(this);
             changeVelocity(movement.execute(100, 250, DEFAULT_SPEED)); // want to make this call
                                                                        // every X seconds
             super.update(elapsedTime, bounds);
         }
     }
 
     /**
      * Represents a moving platform that moves in the left/right direction
      * 
      * @author Jay Wang
      */
     public static class MovingPlatformTwo extends NonStaticEntity {
 
         private static final String DEFAULT_IMG = "platform.gif";
         private static final int DEFAULT_SPEED = 60;
         private static final Vector DEFAULT_VELOCITY = new Vector(Sprite.RIGHT_DIRECTION,
                                                                   DEFAULT_SPEED);
 
         public MovingPlatformTwo () {
             this(DEFAULT_LOC);
         }
 
         public MovingPlatformTwo (Location center) {
             super(new Pixmap(DEFAULT_IMG), center, new Dimension(96, 32));
             this.changeVelocity(DEFAULT_VELOCITY);
         }
 
         public void update (double elapsedTime, Dimension bounds) {
             LeftAndRight movement = new LeftAndRight(this);
             changeVelocity(movement.execute(500, 1000, DEFAULT_SPEED));
             super.update(elapsedTime, bounds);
         }
     }
     
 
     public static class LevelTwoBlockOne extends Platform {
 
         private static final String DEFAULT_IMG = "leveltwoblock1.png";
         private static final Dimension DEFAULT_SIZE = new Dimension(128, 96);
 
         public LevelTwoBlockOne () {
             this(DEFAULT_LOC);
         }
 
         public LevelTwoBlockOne (Location center) {
             super(DEFAULT_IMG, center, DEFAULT_SIZE);
         }
 
         public void print () {
         }
     }
     
     public static class LevelTwoBlockTwo extends Platform {
         
         private static final String DEFAULT_IMG = "leveltwoblock2.png";
         private static final Dimension DEFAULT_SIZE = new Dimension(96, 32);
 
         public LevelTwoBlockTwo () {
             this(DEFAULT_LOC);
         }
 
         public LevelTwoBlockTwo (Location center) {
             super(DEFAULT_IMG, center, DEFAULT_SIZE);
         }
         
         public void print () {
         }
     }
     
     public static class LevelTwoBlockThree extends Platform {
         
         private static final String DEFAULT_IMG = "leveltwoblock3.png";
         private static final Dimension DEFAULT_SIZE = new Dimension(768, 192);
 
         public LevelTwoBlockThree () {
             this(DEFAULT_LOC);
         }
 
         public LevelTwoBlockThree (Location center) {
             super(DEFAULT_IMG, center, DEFAULT_SIZE);
         }
         
         public void print () {
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Class<? extends Sprite>[] getSpritesClasses () {
         return (Class<? extends Sprite>[]) this.getClass().getClasses();
     }
 
 }
