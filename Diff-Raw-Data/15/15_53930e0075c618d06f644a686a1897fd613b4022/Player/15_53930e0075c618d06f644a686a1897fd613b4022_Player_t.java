 package cs2114.tiletraveler;
 
 import sofia.graphics.Color;
 
 // -------------------------------------------------------------------------
 /**
  * Our Player Extends the MovingEntity class, which extends Entity, which
  * contains a RectangleShape. The Player has multi-directional double-jump
  * capabilities and acts when a button is pressed
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards (MrZchuck)
  * @author Jacob Stenzel (sjacob95)
  * @version 2013.12.08
  */
 public class Player
     extends Entity
 {
     //private static final long MOVETIME         = 250;
     private boolean           moving;
     private boolean           receiveInput     = true;
     private boolean           alive            = true;
     private boolean           won              = false;
     private Direction         nextAction       = null;
     private int               jumpCount        = 0;
     private Direction         currentDirection = Direction.SOUTH;
 
 
     /**
      * Constructor for the Player that takes in an x and a y coordinate, as well
      * as a Tile size.
      *
      * @param x
      *            The x coordinate for the Player's Location.
      * @param y
      *            The y coordinate for the Player's Location.
      * @param tileSize
      *            The size of one Tile in pixels.
      * @param stage
      *            The stage the Player resides in
      */
     public Player(float x, float y, float tileSize, Stage stage)
     {
         super(x, y, tileSize, stage);
         getShape().setImage("heroback");
         movingStopped();
     }
 
 
     /**
      * Constructor for the Player that takes in a Location object, as well as
      * the Tile size.
      *
      * @param loc
      *            The Location of the Tile
      * @param tileSize
      *            The size of one Tile in pixels.
      * @param stage
      *            The stage the Player resides in
      */
     public Player(Location loc, float tileSize, Stage stage)
     {
         super(loc, tileSize, stage);
         getShape().setImage("heroback");
         movingStopped();
     }
 
 
     /**
      * The act method that checks to see if the object is already moving and,
      * depending on the Player's state, either moves or add a movement to the
      * queue
      *
      * @param direction
      *            The direction that the object should be moving in.
      */
     public void act(Direction direction)
     {
         if (!moving && receiveInput)
         {
             nextAction = direction;
             move(direction);
         }
         else if (receiveInput)
         {
             nextAction = direction;
         }
     }
 
 
     /**
      * Attempts to move the player in a Direction
      *
      * @param direction
      *            - the Direction to be moved in
      */
     public void move(Direction direction)
     {
         if (direction != null && !moving)
         {
             currentDirection = direction;
             movingStarted();
             nextAction = null;
             Tile currentTile = getStage().getMap().getTile(getLocation());
             Tile newMoveTile =
                 getStage().getMap().getTile(
                     getLocation().getNeighbor(direction));
             if (newMoveTile.equals(Tile.WALL)
                 || newMoveTile.equals(Tile.PILLAR)
                 || newMoveTile.equals(Tile.INVALID))
             {
                 nudge(direction);
             }
 
             else if (((newMoveTile.equals(Tile.EMPTY) || newMoveTile
                .equals(Tile.WATER)))
                 || ((currentTile.equals(Tile.EMPTY) || currentTile
                    .equals(Tile.WATER))))
             {
                if(jumpCount < 2)
                {
                    jump(direction);
                }
                else
                {
                    checkCurrentStatus();
                }
             }
 
             else if ((newMoveTile.equals(Tile.EMPTY) || newMoveTile
                 .equals(Tile.WATER)) && jumpCount >= 2)
             {
                 checkAndMove();
             }
 
             else
             {
                 jumpCount = 0;
                 walk(direction);
             }
         }
         else
         {
             checkCurrentStatus();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Causes the Player to nudge against a wall, representing a failed move
      *
      * @param direction
      *            - the direction in which to nudge
      */
     public void nudge(Direction direction)
     {
         // A change of image, if you so choose
         notifyObservers("movingStopped", 0.5);
         notifyObservers("checkAndMove", 0.5);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Causes the Player to jump in a Direction
      *
      * @param direction
      *            - the Direction to be jumped in
      */
     public void jump(Direction direction)
     {
         incJumpCount();
 
         notifyObservers(
             1.0,
             Location.convertToDisplacement(direction).x(),
             Location.convertToDisplacement(direction).y());
 
         setLocation(getLocation().getNeighbor(direction));
 
         setJumpImage();
         notifyObservers("setRestImage", 0.7);
         notifyObservers("movingStopped", 1.0);
         notifyObservers("nextMove", 1.0);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Walks the Player in a Direction
      *
      * @param direction
      *            - the Direction to be walked in
      */
     public void walk(Direction direction)
     {
 
         notifyObservers(
             1.0,
             Location.convertToDisplacement(direction).x(),
             Location.convertToDisplacement(direction).y());
 
         setLocation(getLocation().getNeighbor(direction));
 
         setWalkImage();
         notifyObservers("setRestImage", 0.7);
         notifyObservers("movingStopped", 1.0);
         notifyObservers("checkAndMove", 1.0);
     }
 
 
     /**
      * Among other things, checks if the Player is dead
      *
      * @return whether or not the player is dead
      */
     public boolean checkCurrentStatus()
     {
         Tile currentTile = getStage().getMap().getTile(getLocation());
         Enemy enemy = getStage().getEnemyMap().getEnemy(getLocation());
         if ((currentTile.equals(Tile.EMPTY) || currentTile.equals(Tile.WATER))
             || enemy != null)
         {
             die();
             return false;
         }
         else if (currentTile.equals(Tile.DOOR))
         {
             isWon();
             return false;
         }
         else
         {
             jumpCount = 0;
         }
         return true;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Checks whether the Player has collided with an enemy
      *
      * @return whether or not the Player has collided with an enemy
      */
     public boolean checkEnemyCollision()
     {
         Enemy enemy = getStage().getEnemyMap().getEnemy(getLocation());
         if (enemy != null)
         {
             die();
             return false;
         }
         return true;
     }
 
 
     /**
      * Kills the player, ceasing input and setting its color to red
      */
     public void die()
     {
         blockInput();
         nextAction = null;
         alive = false;
         getShape().setColor(Color.red);
         notifyObservers(alive);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Called when the game is won! Ceases input, and sets the color to green.
      */
     public void isWon()
     {
         blockInput();
         nextAction = null;
         won = true;
         getShape().setColor(Color.green);
         notifyObservers(won);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Blocks user input
      */
     public void blockInput()
     {
         receiveInput = false;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Resumes accepting user input
      */
     public void resumeInput()
     {
         nextAction = null;
         receiveInput = true;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Changes the boolean value designating whether the Player is currently in
      * motion to false
      */
     public void movingStopped()
     {
         moving = false;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Changes the boolean value designating whether the Player is currently in
      * motion to true
      */
     public void movingStarted()
     {
         moving = true;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Checks whether or not the Player is alive and, if so, carries out the
      * next move in the queue
      */
     public void checkAndMove()
     {
         if (checkCurrentStatus())
         {
             nextMove();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Carries out the next move in the queue
      */
     public void nextMove()
     {
         if (checkEnemyCollision())
         {
             move(nextAction);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Increments the jump counter
      */
     public void incJumpCount()
     {
         jumpCount++;
     }
 
 
     // ----------------------------------------------------------
     /**
      * @return the Player's moveTime constant
      */
     public long getMoveTime()
     {
         return 250;
         //return MOVETIME;
     }
 
 
     // ----------------------------------------------------------
     /**
      * @return whether or not the Player is currently jumping
      */
     public boolean isJumping()
     {
         return jumpCount > 0;
     }
 
 
     // ----------------------------------------------------------
     /**
      * @return whether or not the Player is currently alive
      */
     public boolean isAlive()
     {
         return alive;
     }
 
 
     /**
      * @return whether or not the Player is currently moving
      */
     public boolean isMoving()
     {
         return moving;
     }
 
 
     // -----------------------------------------------------------
     // When player is redrawn, currentDirection becomes South
     // doesn't change position correctly and so neither does image
     // ----------------------------------------------------------
     /**
      * Sets the Player's image to a standing one that is dependent on
      * currentDirection
      */
     public void setRestImage()
     {
         if (currentDirection == Direction.SOUTH)
         {
             getShape().setImage("heroback");
         }
         else if (currentDirection == Direction.WEST)
         {
             getShape().setImage("heroleft");
         }
         else if (currentDirection == Direction.EAST)
         {
             getShape().setImage("heroright");
         }
         else
         {
             getShape().setImage("heroforward");
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the Player's image to a walking one that is dependent on
      * currentDirection
      */
     public void setWalkImage()
     {
         if (currentDirection == Direction.SOUTH)
         {
             getShape().setImage("heroback");
         }
         else if (currentDirection == Direction.WEST)
         {
             getShape().setImage("heroleft");
         }
         else if (currentDirection == Direction.EAST)
         {
             getShape().setImage("heroright");
         }
         else
         {
             getShape().setImage("heroforward");
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Sets the Player's image to a jumping one that is dependent on
      * currentDirection
      */
     public void setJumpImage()
     {
         if (currentDirection == Direction.SOUTH)
         {
             getShape().setImage("herobackjump");
         }
         else if (currentDirection == Direction.WEST)
         {
             getShape().setImage("heroleftjump");
         }
         else if (currentDirection == Direction.EAST)
         {
             getShape().setImage("herorightjump");
         }
         else
         {
             getShape().setImage("heroforwardjump");
         }
     }
 }
