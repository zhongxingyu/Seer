 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameState;
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.ModalMouseMotionInputDriver;
 import edu.mines.csci598.recycler.frontend.graphics.*;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import edu.mines.csci598.recycler.frontend.utils.Log;
 
 import java.awt.*;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 
 /**
  * The GameLogic is where the game play logic is. The main game update loop will go here.
  * TODO:*******NEED TO FIX sprite linked list.  Sometimes throws exception*******
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameLogic extends GameState {
     private static GameLogic INSTANCE;
     Player player1, player2;
     GameScreen gameScreen;
     GameManager gameManager;
     LinkedList<RecycleBin> recycleBins = new LinkedList<RecycleBin>();
     LinkedList<Recyclable> recyclables = new LinkedList<Recyclable>();
     LinkedList<Recyclable> recyclablesToRemove = new LinkedList<Recyclable>();
     double lastGenerateTime;
     double currentTime;
     long startTime;
     double generateTimeDelay;
     boolean generateMultiple;
     int numItemTypes;
     int score;
     int strikes;
 
     private GameLogic(GameManager gameManager) {
         this.gameManager = gameManager;
         gameScreen = GameScreen.getInstance();
         numItemTypes = GameConstants.INITIAL_NUMBER_OF_ITEM_TYPES;
         lastGenerateTime = 0;
         currentTime = 0;
         startTime = System.currentTimeMillis();
         generateTimeDelay = GameConstants.INITIAL_ITEM_GENERATION_DELAY_SECONDS;
         generateMultiple = true;
         if (!generateMultiple) {
             Recyclable r = new Recyclable(0, RecyclableType.getRandom(4));
             handleRecyclables(GameConstants.ADD_SPRITE, r);
         }
         setUpBins();
 
         // sets up the first player and adds its primary hand to the gameScreen
         // so that it can be displayed
         player1 = new Player(gameManager);
         gameScreen.addHandSprite(player1.primary.getSprite());
     }
 
     private void setUpBins() {
         RecycleBin bin1 = new RecycleBin(
                 GameConstants.BIN_1_SIDE, GameConstants.BIN_1_MIN_Y,
                 GameConstants.BIN_1_MAX_Y, GameConstants.BIN_1_TYPE);
         RecycleBin bin2 = new RecycleBin(
                 GameConstants.BIN_2_SIDE, GameConstants.BIN_2_MIN_Y,
                 GameConstants.BIN_2_MAX_Y, GameConstants.BIN_2_TYPE);
         RecycleBin bin3 = new RecycleBin(
                 GameConstants.BIN_3_SIDE, GameConstants.BIN_3_MIN_Y,
                 GameConstants.BIN_3_MAX_Y, GameConstants.BIN_3_TYPE);
         RecycleBin bin4 = new RecycleBin(
                 GameConstants.BIN_4_SIDE, GameConstants.BIN_4_MIN_Y,
                 GameConstants.BIN_4_MAX_Y, GameConstants.BIN_4_TYPE);
 
         recycleBins.add(bin1);
         recycleBins.add(bin2);
         recycleBins.add(bin3);
         recycleBins.add(bin4);
     }
 
     public static final GameLogic getInstance() {
         if (INSTANCE == null) {
             GameManager gameManager = new GameManager("Recycler", false);
             INSTANCE = new GameLogic(gameManager);
         }
         return INSTANCE;
     }
 
     private void generateItems(double currentTime) {
         //Function will decide on item type to generate
         //Function will create a new sprite of that type
         //Function will add sprite to screen
         if ((currentTime - lastGenerateTime) > generateTimeDelay) {
             //System.out.println("Ct:"+currentTime+",IT:"+itemType);
             try {
                 Recyclable r = new Recyclable(currentTime, RecyclableType.getRandom(numItemTypes));
                 handleRecyclables(GameConstants.ADD_SPRITE, r);
 
                 lastGenerateTime = currentTime;
             } catch (ConcurrentModificationException e) {
                 Log.logError("Trying to generate a new sprite");
             }
         }
     }
 
     private synchronized void updateRecyclables() {
         Sprite sprite;
         try {
             for (Recyclable recyclable : recyclables) {
                 sprite = recyclable.getSprite();
                 try {
                     sprite.updateLocation(currentTime);
                     //if(sprite.getX()>=700) sprites.remove(sprite);
                     if (sprite.getX() >= GameConstants.TOP_PATH_END_X) {
                         recyclablesToRemove.addLast(recyclable);
                         gameScreen.removeSprite(recyclable.getSprite());
                     }
                     if ((recyclable.getCurrentMotion() == Recyclable.MotionState.FALL_RIGHT &&
                             sprite.getX() >= GameConstants.VERTICAL_PATH_START_X + GameConstants.IN_BIN_OFFSET) ||
                         (recyclable.getCurrentMotion() == Recyclable.MotionState.FALL_LEFT &&
                             sprite.getX() <= GameConstants.VERTICAL_PATH_START_X - GameConstants.IN_BIN_OFFSET)) {
                         recyclablesToRemove.addLast(recyclable);
                         gameScreen.removeSprite(recyclable.getSprite());
                     }
 
                     if (sprite.getY() <= GameConstants.SPRITE_BECOMES_UNTOUCHABLE) {
                         sprite.setState(Sprite.TouchState.UNTOUCHABLE);
                     } else if (sprite.getY() <= GameConstants.SPRITE_BECOMES_TOUCHABLE) {
                         sprite.setState(Sprite.TouchState.TOUCHABLE);
                     }
                     checkCollision(recyclable);
 
                 } catch (ConcurrentModificationException e) {
                     Log.logError("Trying to update recyclable " + recyclable + " with time " + currentTime);
                 }
             }
         } catch (ExceptionInInitializerError e) {
             Log.logError("Trying to update sprites with time " + currentTime);
         }
         for (Recyclable recyclable : recyclablesToRemove) {
             recyclables.remove(recyclable);
         }
         //drawThis();
     }
 
     public synchronized void addRecyclable(Recyclable r) {
         try {
             recyclables.add(r);
             gameScreen.addSprite(r.getSprite());
         } catch (ExceptionInInitializerError e) {
             Log.logError("Trying to add Recyclable with time " + currentTime);
         }
     }
 
     public synchronized void removeRecyclable(Recyclable r) {
         recyclables.remove(r);
     }
 
     public synchronized void handleRecyclables(int flag, Recyclable r) {
         if      (flag == GameConstants.ADD_SPRITE)      addRecyclable(r);
         else if (flag == GameConstants.REMOVE_SPRITE)   removeRecyclable(r);
         else if (flag == GameConstants.UPDATE_SPRITES)  updateRecyclables();
     }
 
     private synchronized void checkCollision(Recyclable r) {
         //Log.logInfo("loc: " + player1.primary.getX() + " " + player1.primary.getY() + "  " + player1.primary.getVelocityX());
         if (r.getSprite().getState() == Sprite.TouchState.TOUCHABLE) {
             if (player1.primary.getX() >= r.getSprite().getX() + (GameConstants.SPRITE_X_OFFSET / 2) &&
                     player1.primary.getX() <= r.getSprite().getX() + (GameConstants.SPRITE_X_OFFSET * 2)) {
                 if (player1.primary.getY() >= r.getSprite().getY() + (GameConstants.SPRITE_Y_OFFSET / 2) &&
                         player1.primary.getY() <= r.getSprite().getY() + (GameConstants.SPRITE_Y_OFFSET * 2)) {
                     //Handle collision
                   // Log.logInfo("Collision detected on Sprite ");
                     Log.logInfo("velocity: " + player1.primary.getVelocityX());
 
                     Log.logError("Sx=" + r.getSprite().getX() + ",Sy=" + r.getSprite().getY() +
                             ",Hx=" + player1.primary.getX() + ",Hy=" + player1.primary.getY() +
                             ",Hvx=" + player1.primary.getVelocityX() + ",Hvy" + player1.primary.getVelocityY());
                     //System.out.println("Collision detected on sprite");

                    boolean pushed = false;
 
                     //Handle sprite collision
                     if (player1.primary.getVelocityX() > GameConstants.MIN_VELOCITY) {
                         Path path = new Path();
                         Log.logInfo("Pushed Right");
                         Line collideLine = new Line(r.getSprite().getX(), r.getSprite().getY(),
                                 r.getSprite().getX() + GameConstants.ITEM_PATH_END, r.getSprite().getY(),
                                 GameConstants.ITEM_PATH_TIME);
                         path.addLine(collideLine);
                         r.getSprite().setPath(path);
                         r.getSprite().setStartTime(currentTime);
                         r.setMotionState(Recyclable.MotionState.FALL_RIGHT);
                        pushed = true;
 
                         //r.getSprite().setHorizontalVelocity(GameConstants.HORIZONTAL_VELOCITY);
                     } else if (player1.primary.getVelocityX() < -1 * GameConstants.MIN_VELOCITY) {
                         Path path = new Path();
                         Log.logInfo("Pushed Left");
                         Line collideLine = new Line(r.getSprite().getX(), r.getSprite().getY(),
                                 r.getSprite().getX() - GameConstants.ITEM_PATH_END, r.getSprite().getY(),
                                 GameConstants.ITEM_PATH_TIME);
                         path.addLine(collideLine);
                         r.getSprite().setPath(path);
                         r.getSprite().setStartTime(currentTime);
                         r.setMotionState(Recyclable.MotionState.FALL_LEFT);
                        pushed = true;
                         //r.getSprite().setHorizontalVelocity(-1 * GameConstants.HORIZONTAL_VELOCITY);
                     }
                    if (pushed) {
                        r.getSprite().setState(Sprite.TouchState.UNTOUCHABLE);
                        RecyclableType binType = findRecycledBin(r);
                        handleScore(r, binType);
                    }
                 }
             }
         }
 
     }
 
     private RecyclableType findRecycledBin(Recyclable r) {
         int yCord = r.getSprite().getY();
 
         // finds the bin that the trash has gone into using the y coordinates since it can only fall to the right or
         // left of the conveyor we only need to check which way it's going and the y coordinates
         for (RecycleBin bin : recycleBins) {
             if ((r.getCurrentMotion() == Recyclable.MotionState.FALL_LEFT && bin.getSide() == RecycleBin.ConveyorSide.LEFT) ||
                     (r.getCurrentMotion() == Recyclable.MotionState.FALL_RIGHT && bin.getSide() == RecycleBin.ConveyorSide.RIGHT)) {
 
                 if (yCord >= bin.getMinY() && yCord <= bin.getMaxY()) {
                     return bin.getType();
                 }
             }
         }
         return RecyclableType.SKULL;
     }
 
     // given the recyclable and the bin it went into this function either increments the score or adds a strike
     private void handleScore(Recyclable r, RecyclableType binType) {
         if (r.getType() == binType) {
             score++;
         } else {
             strikes++;
         }
         Log.logInfo("Score: " + score + " Strikes: " + strikes + "\n");
     }
 
     public GameManager getGameManager() {
        return this.gameManager;
     }
 
     protected GameState updateThis(float elapsedTime) {
 
          //in seconds
         currentTime = (System.currentTimeMillis() - startTime) / 1000.0;
 
         if (generateMultiple) {
             generateItems(currentTime);
         }
 
         // display the hand
         player1.primary.updateLocation();
 
         //see if hand is going through item and handle it
         // check coordinates here
         //gameScreen.checkCollisions();
 
         //see if hand hits powerup and handle it
 
         Recyclable r = new Recyclable(currentTime, RecyclableType.SKULL);
         //handleRecyclables(GameConstants.UPDATE_SPRITES, r);
 
         updateRecyclables();
 
         //check for winning condition.
         return this;
     }
 
     protected void drawThis(Graphics2D g2d) {
         gameScreen.paint(g2d, gameManager.getCanvas());
     }
 
     public static void main(String[] args) {
         GameLogic gm = GameLogic.getInstance();
         ModalMouseMotionInputDriver mouse = new ModalMouseMotionInputDriver();
         gm.getGameManager().installInputDriver(mouse);
         gm.getGameManager().setState(gm);
         gm.getGameManager().run();
         gm.getGameManager().destroy();
     }
 }
