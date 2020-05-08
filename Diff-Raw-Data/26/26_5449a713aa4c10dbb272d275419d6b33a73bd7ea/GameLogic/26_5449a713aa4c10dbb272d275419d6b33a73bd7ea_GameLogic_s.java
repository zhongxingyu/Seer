 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameState;
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.backend.ModalMouseMotionInputDriver;
 import edu.mines.csci598.recycler.frontend.graphics.*;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import edu.mines.csci598.recycler.frontend.utils.Log;
 
 import java.awt.Graphics2D;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 
 /**
  * The GameLogic is where the game play logic is. The main game update loop will go here.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameLogic extends GameState {
     private static GameLogic INSTANCE;
     private Player player1, player2;
     private GameScreen gameScreen;
     private GameManager gameManager;
     private LinkedList<RecycleBin> recycleBins = new LinkedList<RecycleBin>();
     private ConveyorBelt conveyor;
     private LinkedList<Recyclable> recyclablesToRemove = new LinkedList<Recyclable>();
     private double lastGenerationTime;
     private double currentTimeSec;
     private long startTime;
     private double minTimeBetweenGenerations;
     private double itemGenerationDelay;
     private boolean generateMultiple;
     private double itemGenerationProb;
     private int numItemTypesInUse;
     private int score;
     private int strikes;
     private int itemType2ActivationTime;
     private int itemType3ActivationTime;
     private int itemType4ActivationTime;
     private int timeToMaxDifficulty;
     private int gameOverStrikes;
 
     private GameLogic(GameManager gameManager) {
         this.gameManager = gameManager;
         gameScreen = GameScreen.getInstance();
         
         numItemTypesInUse = GameConstants.INITIAL_NUMBER_OF_ITEM_TYPES;
         itemType2ActivationTime = GameConstants.ITEM_TYPE_2_ACTIVATION_TIME;
         itemType3ActivationTime = GameConstants.ITEM_TYPE_3_ACTIVATION_TIME;
         itemType4ActivationTime = GameConstants.ITEM_TYPE_4_ACTIVATION_TIME;
         timeToMaxDifficulty = GameConstants.TIME_TO_MAX_DIFFICULTY;
         minTimeBetweenGenerations = GameConstants.INITIAL_ITEM_GENERATION_DELAY_SECONDS;
         lastGenerationTime = 0;
         currentTimeSec = 0;
         itemGenerationDelay = 0;
         generateMultiple = true;
         itemGenerationProb = GameConstants.START_ITEM_GENERATION_PROB;
         gameOverStrikes = 3;
         
         conveyor = new ConveyorBelt();
         startTime = System.currentTimeMillis();
         if (!generateMultiple) {
             Recyclable r = new Recyclable(0, RecyclableType.getRandom(numItemTypesInUse));
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
     
     /*
     *Determines if item can be generated
      */
     private boolean possiblyGenerateItem(){
         boolean generated = false;
         //Log.logInfo("ct="+currentTime+",lgt="+lastGenerateTime+",rg="+randGen+",igm="+itemGenerationMin);
         if(Math.random() < itemGenerationProb){
             generated = true;
         }
         return generated;
     }
     
     /*
     *   Generates the item if possible
      */
     private void generateItems(double currentTime) {
         //Function will decide on item type to generate
         //Function will create a new recyclable of that type
         //Function will add recyclable to screen
         if ((currentTime - lastGenerationTime) > minTimeBetweenGenerations + itemGenerationDelay) {
             //Log.logInfo("Ct:"+currentTime+",IT:"+itemType);
             try {
                 if(possiblyGenerateItem()){
                     Recyclable r = new Recyclable(currentTime, RecyclableType.getRandom(numItemTypesInUse));
                     handleRecyclables(GameConstants.ADD_SPRITE, r);
                     lastGenerationTime=currentTime;
                     itemGenerationDelay = 0;
                 }else{
                     itemGenerationDelay+=GameConstants.ITEM_GENERATION_DELAY;
                 }
             } catch (ConcurrentModificationException e) {
                 Log.logError("Trying to generate a new recyclable");
             }
         }
     }
 
     private synchronized void updateRecyclables() {
         //Log.logInfo("Score: " + score + " Strikes: " + strikes + "\n");
         try {
             for (Recyclable recyclable : conveyor.getRecyclables()){
                 Sprite sprite = recyclable.getSprite();
                 try {
                     sprite.updateLocation(currentTimeSec);
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
                     // make sure the item is still on the conveyor before changing it's touch status
                     if (recyclable.getCurrentMotion() != Recyclable.MotionState.FALL_RIGHT &&
                             recyclable.getCurrentMotion() != Recyclable.MotionState.FALL_LEFT) {
                         if (sprite.getY() <= GameConstants.SPRITE_BECOMES_UNTOUCHABLE) {
                             sprite.setState(Sprite.TouchState.UNTOUCHABLE);
                         } else if (sprite.getY() <= GameConstants.SPRITE_BECOMES_TOUCHABLE) {
                             sprite.setState(Sprite.TouchState.TOUCHABLE);
                         }
                     }
                     checkCollision(recyclable);
 
                 } catch (ConcurrentModificationException e) {
                     Log.logError("Trying to update recyclable " + recyclable + " with time " + currentTimeSec);
                 }
             }
         } catch (ExceptionInInitializerError e) {
             Log.logError("Trying to update sprites with time " + currentTimeSec);
         }
         for (Recyclable recyclable : recyclablesToRemove) {
         	conveyor.removeRecyclable(recyclable);
         }
         //drawThis();
     }
 
     public synchronized void addRecyclable(Recyclable r) {
         try {
             conveyor.addRecyclable(r);
             gameScreen.addSprite(r.getSprite());
         } catch (ExceptionInInitializerError e) {
             Log.logError("Trying to add Recyclable with time " + currentTimeSec);
         }
     }
 
     public synchronized void removeRecyclable(Recyclable r) {
     	conveyor.removeRecyclable(r);
     }
 
     public synchronized void handleRecyclables(int flag, Recyclable r) {
         if      (flag == GameConstants.ADD_SPRITE)      addRecyclable(r);
         else if (flag == GameConstants.REMOVE_SPRITE)   removeRecyclable(r);
         else if (flag == GameConstants.UPDATE_SPRITES)  updateRecyclables();
     }
 
     private synchronized void checkCollision(Recyclable r) {
         //Log.logInfo("(x1,y1)-P1:("+player1.primary.getX()+"," + player1.primary.getY() +"),s:(" +
         //            r.getSprite().getScaledX()+","+r.getSprite().getScaledY()+"),state="+
         //            r.getSprite().getState());
         //Log.logInfo("P1("+player1.primary.getVelocityX()+","+player1.primary.getVelocityY()+")");
 
         if (r.getSprite().getState() == Sprite.TouchState.TOUCHABLE) {
             if (player1.primary.getX() >=
                   r.getSprite().getScaledX() - (GameConstants.SPRITE_X_OFFSET) &&
                   player1.primary.getX() <=
                   r.getSprite().getScaledX() + (GameConstants.SPRITE_X_OFFSET)) {
                 if (player1.primary.getY() >= r.getSprite().getScaledY() - (GameConstants.SPRITE_Y_OFFSET) &&
                         player1.primary.getY() <= r.getSprite().getScaledY()+ (GameConstants.SPRITE_Y_OFFSET)) {
                     //Handle collision
                     //Log.logInfo("Collision detected on Sprite ");
 
                     /*
                     Log.logError("Sx=" + r.getSprite().getX() + ",Sy=" + r.getSprite().getY() +
                             ",Hx=" + player1.primary.getX() + ",Hy=" + player1.primary.getY() +
                             ",Hvx=" + player1.primary.getVelocityX() + ",Hvy" + player1.primary.getVelocityY());
                     */
                     //System.out.println("Collision detected on sprite");
 
                     boolean pushed = false;
 
                     //Handle sprite collision
                     if (player1.primary.getVelocityX() > GameConstants.MIN_HAND_VELOCITY) {
                         Path path = new Path();
                         Log.logInfo("Pushed Right");
                         Line collideLine = new Line(r.getSprite().getX(), r.getSprite().getY(),
                                 r.getSprite().getX() + GameConstants.ITEM_PATH_END, r.getSprite().getY(),
                                 GameConstants.ITEM_PATH_TIME);
                         path.addLine(collideLine);
                         r.getSprite().setPath(path);
                         r.getSprite().setStartTime(currentTimeSec);
                         r.setMotionState(Recyclable.MotionState.FALL_RIGHT);
                         pushed = true;
 
                         //r.getSprite().setHorizontalVelocity(GameConstants.HORIZONTAL_VELOCITY);
                     } else if (player1.primary.getVelocityX() < -1 * GameConstants.MIN_HAND_VELOCITY) {
                         Path path = new Path();
                         Log.logInfo("Pushed Left");
                         Line collideLine = new Line(r.getSprite().getX(), r.getSprite().getY(),
                                 r.getSprite().getX() - GameConstants.ITEM_PATH_END, r.getSprite().getY(),
                                 GameConstants.ITEM_PATH_TIME);
                         path.addLine(collideLine);
                         r.getSprite().setPath(path);
                         r.getSprite().setStartTime(currentTimeSec);
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
         int yCord = r.getSprite().getScaledY();
 
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
 
         if(strikes >= gameOverStrikes){
             gameOver();
         }
         Log.logInfo("Score: " + score + " Strikes: " + strikes + "\n");
     }
 
     public String getScoreString(){
         return ("" + score);
     }
 
     public String getStrikesString(){
         return ("" + strikes);
     }
 
 
     private void gameOver(){
         System.out.println("GAME OVER");
         Sprite sprite = new Sprite("src/main/resources/SpriteImages/GameOverText.png", (GraphicsConstants.GAME_SCREEN_WIDTH/2) -220, (GraphicsConstants.GAME_SCREEN_HEIGHT/2) -200);
         gameScreen.addSprite(sprite);
         //If We want it to exit
         //gameManager.destroy();
 
     }
 
 
     private void increaseDifficulty(){
     	// Possibly add more items
         if(numItemTypesInUse < 2 && Math.round(currentTimeSec) > itemType2ActivationTime){
         	Log.logInfo("Increasing item types to 2!");
         	numItemTypesInUse++;
         }
         if(numItemTypesInUse < 3 && Math.round(currentTimeSec) > itemType3ActivationTime){
         	Log.logInfo("Increasing item types to 3!");
         	numItemTypesInUse++;
         }
         if(numItemTypesInUse < 4 && Math.round(currentTimeSec) > itemType4ActivationTime){
         	Log.logInfo("Increasing item types to 4!");
         	numItemTypesInUse++;
         }
         
         double pctToMaxDifficulty = Math.min(1, currentTimeSec/timeToMaxDifficulty);
         conveyor.setSpeed(pctToMaxDifficulty);
         
         // increase probability of item generation
         double startProbability = GameConstants.START_ITEM_GENERATION_PROB;
         itemGenerationProb = startProbability + (1-startProbability) * pctToMaxDifficulty;
     }
 
     public GameManager getGameManager() {
        return this.gameManager;
     }
 
     protected GameState updateThis(float elapsedTime) {
 
          //in seconds
         currentTimeSec = (System.currentTimeMillis() - startTime) / 1000.0;
         
         increaseDifficulty();
         
         if (generateMultiple) {
             generateItems(currentTimeSec);
         }
 
         // display the hand
         player1.primary.updateLocation();
 
         //see if hand is going through item and handle it
         // check coordinates here
         //gameScreen.checkCollisions();
 
         //see if hand hits powerup and handle it
 
         //Recyclable r = new Recyclable(currentTime, RecyclableType.SKULL);
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
