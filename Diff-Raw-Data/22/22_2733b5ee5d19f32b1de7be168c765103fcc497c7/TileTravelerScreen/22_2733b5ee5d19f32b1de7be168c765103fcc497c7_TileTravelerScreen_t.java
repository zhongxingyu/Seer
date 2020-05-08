 package cs2114.tiletraveler;
 
 import java.util.Locale;
 import sofia.graphics.Shape;
 import sofia.app.OptionsMenu;
 import sofia.util.Timer;
 import sofia.graphics.Anchor;
 import sofia.app.ShapeScreen;
 import android.widget.TextView;
 import sofia.graphics.RectangleShape;
 import sofia.graphics.Color;
 
 // -------------------------------------------------------------------------
 /**
  * Screen class to set up the tile traveler game screen
  *
  * @author Luciano Biondi (lbiondi)
  * @author Ezra Richards (MrZchuck)
  * @author Jacob Stenzel (sjacob95)
  * @version 2013.12.08
  */
 @OptionsMenu
 public class TileTravelerScreen
     extends ShapeScreen
 {
     private float            tileSize;
     private Map              currentMap;
     private EnemyMap         currentEnMap;
     private Stage            currentStage;
     private static final int SCREENDIM = 15;                // tile size of one
 // side in a single view
     private int              mapDim;
     private Player           player;
     private Timer enemyTimer;
 
     private TextView         status;
 
 
     /**
      * the map location currently at the bottom left of the screen
      */
     private Location         origin    = new Location(0, 0);
 
 
     public void initialize()
     {
         getCoordinateSystem().origin(Anchor.BOTTOM_LEFT).flipY();
         tileSize = Math.min(getWidth(), getHeight()) / SCREENDIM;
 
         currentStage = new Stage1(tileSize);
 
         reset();
     }
 
 
     // ------------------------------------------------------------------------
     // MENU OPTIONS
     // ------------------------------------------------------------------------
 
     /**
      * Loads Stage 1 when the appropriate menu item is clicked
      */
     public void stage1Clicked()
     {
         currentStage = new Stage1(tileSize);
         reset();
     }
 
 
     /**
      * Loads Stage 2 when the appropriate menu item is clicked
      */
     public void stage2Clicked()
     {
         currentStage = new Stage2(tileSize);
         reset();
     }
 
 
     /**
      * Loads Stage 2 when the appropriate menu item is clicked
      */
     public void stage3Clicked()
     {
         currentStage = new Stage3(tileSize);
         reset();
     }
 
 
     // -------------------------------------------------------------------------
     // BUTTONS
     // -------------------------------------------------------------------------
 
     // ----------------------------------------------------------
     /**
      * Moves the Player west when the left button is clicked
      */
     public void leftClicked()
     {
         player.act(Direction.WEST);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Moves the Player east when the right button is clicked
      */
     public void rightClicked()
     {
         player.act(Direction.EAST);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Moves the Player north when the up button is clicked
      */
     public void upClicked()
     {
         player.act(Direction.NORTH);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Moves the Player south when the down button is clicked
      */
     public void downClicked()
     {
         player.act(Direction.SOUTH);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Resets the Stage when reset is clicked
      */
     public void resetClicked()
     {
         reset();
     }
 
 
     /**
      * Recenters the screen when Center Screen is clicked
      */
     public void centerScreenClicked()
     {
         if (!player.isMoving() && player.isAlive())
         {
             Location loc = player.getLocation();
             origin =
                 new Location(loc.x() - (SCREENDIM / 2), loc.y()
                     - (SCREENDIM / 2));
             redraw();
         }
     }
 
 
     // -------------------------------------------------------------------------
     // SCREEN METHODS
     // -------------------------------------------------------------------------
 
     // ----------------------------------------------------------
     /**
      * Completely resets the Stage
      */
     public void reset()
     {
         if (enemyTimer != null)
         {
             enemyTimer.stop();
         }
         currentStage = currentStage.reset(tileSize);
         currentMap = currentStage.getMap();
         currentEnMap = currentStage.getEnemyMap();
         mapDim = currentMap.getMapDim();
         player = new Player(currentStage.getStartLoc(), tileSize, currentStage);
         status.setText("Using the arrow buttons, escape the dungeon!");
 
         centerScreenClicked();
         redraw();
         enemyTimer = Timer.callRepeatedly(this, "moveEnemies", 0, MovingEnemy.getMoveTime());
     }
 
 
     // ----------------------------------------------------------
     /**
      * Draws the screen and adds the Player to the screen and begins observing
      * it
      */
     public void draw()
     {
         drawScreen();
         add(player.getShape());
         player.addObserver(this);
         addEnemies();
         checkOrigin();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Adds all Enemies to the screen
      */
     public void addEnemies()
     {
         for (int x = 0; x < currentEnMap.getMapDim(); x++)
         {
             for (int y = 0; y < currentEnMap.getMapDim(); y++)
             {
                 Enemy tempEnemy = currentEnMap.getEnemy(x, y);
                 if (tempEnemy != null)
                 {
                     add(tempEnemy.getShape());
                     tempEnemy.addObserver(this);
                 }
             }
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Redraws all Enemies (Does not add to screen; consult drawEnemies)
      */
     public void redrawEnemies()
     {
         for (int x = 0; x < currentEnMap.getMapDim(); x++)
         {
             for (int y = 0; y < currentEnMap.getMapDim(); y++)
             {
                 Enemy tempEnemy = currentEnMap.getEnemy(x, y);
                 if (tempEnemy != null)
                 {
                     remove(tempEnemy.getShape());
                     tempEnemy.redrawShape(origin);
                 }
             }
         }
     }
 
     // ----------------------------------------------------------
     /**
      * Redraws all Enemies (Does not add to screen; consult drawEnemies)
      */
     public void adjustEnemies()
     {
         for (int x = 0; x < currentEnMap.getMapDim(); x++)
         {
             for (int y = 0; y < currentEnMap.getMapDim(); y++)
             {
                 Enemy tempEnemy = currentEnMap.getEnemy(x, y);
                 if (tempEnemy != null)
                 {
                     adjustEntity(tempEnemy);
                 }
             }
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Redraws the Player and the Stage
      */
     public void redraw()
     {
         remove(player.getShape());
         player.redrawShape(origin);
         redrawEnemies();
         draw();
     }
 
 
     /**
      * Adjusts the Entity's position to correct timing/lag related problems
      *
      * @param entity
      *            The Entity
      */
     public void adjustEntity(Entity entity)
     {
         entity.getShape().setLeft(
             (entity.getLocation().x() - origin.x()) * tileSize);
         entity.getShape().setTop(
             (entity.getLocation().y() - origin.y()) * tileSize);
 
     }
 
 
     /**
      * Adjusts the Player's position to correct timing/lag related problems
      */
     public void adjustPlayer()
     {
         player.getShape().setLeft(
             (player.getLocation().x() - origin.x()) * tileSize);
         player.getShape().setTop(
             (player.getLocation().y() - origin.y()) * tileSize);
         if (player.isAlive())
         {
             checkOrigin();
         }
     }
 
 
     /**
      * Redraws an Entity
      *
      * @param entity
      *            The Entity to be redrawn
      */
     public void redrawEntity(Entity entity)
     {
         entity.getShape().setLeft(
             (entity.getLocation().x() - origin.x()) * tileSize);
         entity.getShape().setTop(
             (entity.getLocation().y() - origin.y()) * tileSize);
     }
 
 
     /**
      * Draws the Map according to the value of origin
      */
     public void drawScreen()
     {
         clear();
         RectangleShape background =
             new RectangleShape(0, 0, getWidth(), getHeight());
         background.setFillColor(Color.black);
         add(background);
         for (int y = 0; y < mapDim; y++)
         {
             for (int x = 0; x < mapDim; x++)
             {
                 drawTile(x, y);
             }
         }
     }
 
 
     /**
      * Draws a Tile
      *
      * @param x
      *            the x coordinate of the Tile to be drawn
      * @param y
      *            the y coordinate of the Tile to be drawn
      */
     public void drawTile(int x, int y)
     {
         Tile tile = currentMap.getTile(x, y);
         if (tile == null)
             return;
         switch (tile)
         {
             case DOOR:
                 add(new Door((x - origin.x()), (y - origin.y()), tileSize));
                 break;
 
             case FLOOR:
                 add(new Floor((x - origin.x()), (y - origin.y()), tileSize));
                 break;
 
             case LILY:
                 add(new Water((x - origin.x()), (y - origin.y()), tileSize));
                 add(new Lily((x - origin.x()), (y - origin.y()), tileSize));
                 break;
 
             case PILLAR:
                 add(new Pillar((x - origin.x()), (y - origin.y()), tileSize));
                 break;
 
             case WALL:
                 add(new Wall((x - origin.x()), (y - origin.y()), tileSize));
                 break;
 
             case WATER:
                 add(new Water((x - origin.x()), (y - origin.y()), tileSize));
                 break;
 
             default:
                 break;
 
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * @return the Tile size in pixels
      */
     public float getTileSize()
     {
         return tileSize;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Checks whether or not the player has moved outside the bounds set by
      * SCREENDIM. If this is the case, the screen shifts by SCREENDIM Tiles in
      * the direction of the Player
      */
     public void checkOrigin()
     {
         if (player.isAlive())
         {
             boolean changed = false;
             if (player.getLocation().y() - origin.y() < 0)
             {
                 origin = origin.south(SCREENDIM);
                 changed = true;
             }
             else if (player.getLocation().x() - origin.x() < 0)
             {
                 origin = origin.west(SCREENDIM);
                 changed = true;
             }
             else if (player.getLocation().y() - origin.y() >= SCREENDIM)
             {
                 origin = origin.north(SCREENDIM);
                 changed = true;
             }
             else if (player.getLocation().x() - origin.x() >= SCREENDIM)
             {
                 origin = origin.east(SCREENDIM);
                 changed = true;
             }
             if (changed && !player.isJumping())
             {
                 redraw();
             }
         }
     }
 
     /**
      * Moves all Enemies and then checks whether or not they have killed the Player
      */
     public void moveEnemies()
     {
         EnemyMap tempMap = new EnemyMap(mapDim);
         MovingEnemy lastMovingEnemy = null;
         for (int x = 0; x < currentEnMap.getMapDim(); x++)
         {
             for (int y = 0; y < currentEnMap.getMapDim(); y++)
             {
                 Enemy tempEnemy = currentEnMap.getEnemy(x, y);
                 if (tempEnemy instanceof MovingEnemy)
                 {
                     lastMovingEnemy = (MovingEnemy)tempEnemy;
                     lastMovingEnemy.move();
                     tempMap.addEnemy(lastMovingEnemy);
                 }
             }
         }
         currentStage.setEnemyMap(tempMap);
         currentEnMap = tempMap;
         player.checkEnemyCollision();
     }
 
     // -------------------------------------------------------------------------
     // MOVINGENEMY OBSERVER METHODS
     // -------------------------------------------------------------------------
 
     // ----------------------------------------------------------
     /**
      * Moves a MovingEnemy
      *
      * @param entity
      *            The MovingEnemy to be moved
      * @param fractionMoveTime
      *            The fraction of the MovingEnemy's MoveTime that this move will take
      *            to execute
      * @param x
      *            The x coordinate to be moved to
      * @param y
      *            The y coordinate to be moved to
      */
     public void changeWasObserved(
         MovingEnemy entity,
         double fractionMoveTime,
         int x,
         int y)
     {
         Shape.Animator<?> anim =
             entity
                 .getShape()
                 .animate(MovingEnemy.getMoveTime() * Math.round(fractionMoveTime))
                 .position(
                     (float)((x - origin.x() +.5) * tileSize),
                     (float)((y - origin.y() + .5) * tileSize));
         anim.play();
         entity.setLastAnimation(anim);
     }
 
     // -------------------------------------------------------------------------
     // PLAYER OBSERVER METHODS
     // -------------------------------------------------------------------------
 
     // ----------------------------------------------------------
     /**
      * Checks whether or not the player is alive and responds appropriately
      *
      * @param entity
      *            The player
      * @param alive
      *            Whether or not he or she is alive
      */
     public void changeWasObserved(Player entity, boolean alive)
     {
         if (!alive)
         {
             status.setText("You are died!");
         }
         else if (alive)
         {
             status.setText("You did the win!");
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Moves the Player
      *
      * @param entity
      *            The player
      * @param fractionMoveTime
      *            The fraction of the player's MoveTime that this move will take
      *            to execute
      * @param x
      *            The x distance to be traveled
      * @param y
      *            The y distance to be traveled
      */
     public void changeWasObserved(
         Player entity,
         double fractionMoveTime,
         int x,
         int y)
     {
         Shape.Animator<?> anim =
             player.getShape()
                .animate((long)(player.getMoveTime() * (fractionMoveTime)))
                 .moveBy(x * getTileSize(), y * getTileSize());
         anim.play();
         while (anim.isPlaying())
         {
             // intentionally blank
         }
         adjustPlayer();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the screen method associated with the Player method specified by
      * methodName after a delay, calls the adjustPlayer() method to fix any
      * offset to lag, and then calls the checkOrigin() method to make sure the
      * Player stays within the screen's bounds
      *
      * @param entity
      *            The Player
      * @param methodName
      *            The name of the Player method to be called
      * @param fractionMoveTime
      *            The fraction of the Player's MoveTime that the method's delay
      *            will take
      */
     public void changeWasObserved(
         Player entity,
         String methodName,
         double fractionMoveTime)
     {
         StringBuilder thisMethodName = new StringBuilder();
         thisMethodName.append("callPlayer");
         thisMethodName.append(methodName.substring(0, 1).toUpperCase(Locale.US));
         thisMethodName.append(methodName.substring(1, methodName.length()));
         Timer.callOnce(
             this,
             thisMethodName.toString(),
            ((long)(entity.getMoveTime() * (fractionMoveTime))));
         Timer.callOnce(
             this,
             "checkOrigin",
            ((long)(entity.getMoveTime() * (fractionMoveTime))));
     }
 
 
     // -------------------------------------------------------------------------
     // PLAYER CALL METHODS
     // -------------------------------------------------------------------------
 
     // ----------------------------------------------------------
     /**
      * Calls the player's resumeInput() method
      */
     public void callPlayerResumeInput()
     {
         player.resumeInput();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's movingStopped() method
      */
     public void callPlayerMovingStopped()
     {
         player.movingStopped();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's checkAndMove() method
      */
     public void callPlayerCheckAndMove()
     {
         player.checkAndMove();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's incJumpCount() method
      */
     public void callPlayerIncJumpCount()
     {
         player.incJumpCount();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's nextMove() method
      */
     public void callPlayerNextMove()
     {
         player.nextMove();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's setRestImage() method
      */
     public void callPlayerSetRestImage()
     {
         player.setRestImage();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's setWalkImage() method
      */
     public void callPlayerSetWalkImage()
     {
         player.setWalkImage();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls the player's setJumpImage() method
      */
     public void callPlayerSetJumpImage()
     {
         player.setJumpImage();

     }
 }
