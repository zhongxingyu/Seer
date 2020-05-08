 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.frontend.ai.ComputerPlayer;
 import edu.mines.csci598.recycler.frontend.graphics.GameScreen;
 import edu.mines.csci598.recycler.frontend.graphics.Path;
 import edu.mines.csci598.recycler.frontend.hands.Hand;
 import edu.mines.csci598.recycler.frontend.items.ItemFactory;
 import edu.mines.csci598.recycler.frontend.items.PowerUp;
 import edu.mines.csci598.recycler.frontend.items.Recyclable;
 import edu.mines.csci598.recycler.frontend.motion.ConveyorBelt;
 import edu.mines.csci598.recycler.frontend.motion.FeedbackDisplay;
 import edu.mines.csci598.recycler.frontend.motion.Movable;
 import edu.mines.csci598.recycler.frontend.motion.TheForce;
 import edu.mines.csci598.recycler.splashscreen.highscores.SavePlayer;
 import org.apache.log4j.Logger;
 
 
 import java.util.List;
 
 
 /**
  * The GameLogic is where the game play logic is. The main game update loop will go here.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GameLogic {
 
     private static final Logger logger = Logger.getLogger(GameLogic.class);
 
     private GameLogic otherScreen;
     private  List<Hand> hands;
     private ComputerPlayer computerPlayer;
     private GameScreen gameScreen;
     private ItemFactory factory;
     private RecycleBins recycleBins;
     private ConveyorBelt conveyorBelt;
     private TheForce theForce;
     private double currentTimeSec;
     private double wallTimeSec;
     private FeedbackDisplay feedbackDisplay;
     private double lastWallTimeSec;
     private double timeSpeedFactor;
     private double powerUpSpeedFactor;
     private long startTime;
     private double nextItemTypeGenerationTime;
     private int numItemTypesInUse;
     private StrikeBar strikeBar;
    private boolean started;
     private boolean playerIsAComputer;
     private boolean debuggingCollisions;
     private GameStatusDisplay gameStatusDisplay;
     private double timeToRemovePowerUp;
     private boolean scoreSubmitted =false;
     private GameManager gameManager;
     private boolean isPlaying;
 
    /* public GameLogic(RecycleBins recycleBins, Path conveyorPath, GameManager gameManager, GameStatusDisplay gameStatusDisplay,
                      boolean rightSide, boolean debuggingCollision) {
         this.gameManager = gameManager;
         */
 
 
 
     public GameLogic(RecycleBins recycleBins, Path conveyorPath, GameManager gameManager, GameStatusDisplay gameStatusDisplay,
                      boolean debuggingCollision, List<Hand> hands) {
         isPlaying = true;
         this.gameManager = gameManager;
         gameScreen = GameScreen.getInstance();
         factory = new ItemFactory();
         this.recycleBins = recycleBins;
 
         numItemTypesInUse = GameConstants.INITIAL_NUMBER_OF_ITEM_TYPES;
         currentTimeSec = 0;
         lastWallTimeSec = 0;
         timeSpeedFactor = 1;
         powerUpSpeedFactor = 1;
         timeToRemovePowerUp = 0.0;
         feedbackDisplay = new FeedbackDisplay(0);
         nextItemTypeGenerationTime = GameConstants.TIME_TO_ADD_NEW_ITEM_TYPE;
         this.gameStatusDisplay = gameStatusDisplay;
         conveyorBelt = new ConveyorBelt(this, gameScreen, conveyorPath);
         theForce = new TheForce();
        started = false;
         this.playerIsAComputer = playerIsAComputer;
         this.debuggingCollisions = debuggingCollision;
         strikeBar = new StrikeBar(gameStatusDisplay);
         this.hands = hands;
 
         //Add recycle bins to game screen to be drawn
         for (RecycleBin bin : recycleBins.getRecycleBins()) {
             if (bin.hasSprite()) {
                 gameScreen.addRecycleBinSprite(bin.getSprite());
             }
         }
     }
 
     /**
      * This function will turn on the computer player, otherwise it will be left as a regular people player
      * This only works on the right side at the moment
      */
     public void turnOnComputer() {
         playerIsAComputer = true;
         computerPlayer = new ComputerPlayer(recycleBins);
         gameScreen.addHandSprite(computerPlayer.primary.getSprite());
         hands.add(computerPlayer.getHand());
     }
 
     /**
      * Checks to see if there is a collision with each hand
      * If there's a collision, handle it in handleCollisions
      */
     private void lookForAndHandleCollisions() {
         for (Hand hand : hands) {
 
             //If the hand isn't moving fast enough to swipe, skip it
             if (Math.abs(hand.getVelocityX()) < GameConstants.MIN_HAND_VELOCITY) {
                 continue;
             }
             // If we get here, it is causing collisions with anything at this location.
 
             List<Movable> swipedOffConveyor = conveyorBelt.releaseTouchableItemsAtPoint(hand.getPosition());
             logger.debug("swipedSize=" + swipedOffConveyor.size());
             // We should really check the theForce also, but we're not allowing things it controls to be touchable, so it would be kind of silly.
 
             handleCollisions(hand, swipedOffConveyor);
         }
     }
 
     public void handleCollisions(Hand hand, List<Movable> swipedOffConveyor) {
         for (Movable m : swipedOffConveyor) {
             m.reactToCollision(hand, currentTimeSec);
             makePowerUpChangesAndUpdateDisplayIfPowerup(m);
         }
 
         // We now have a list of items that the conveyor belt has released, and they know where they are going.
         // Handing control to TheForce!
         theForce.takeControlOfMovables(swipedOffConveyor);
         conveyorBelt.releaseMovables(swipedOffConveyor);
     }
 
     /**
      * Given a movable, determines if it is a powerup and handle it accordingly
      * Also displays correct image for powerup
      *
      * @param m
      */
     private void makePowerUpChangesAndUpdateDisplayIfPowerup(Movable m) {
         if (m instanceof PowerUp) {
             PowerUp p = (PowerUp) m;
             if (p.getType() == PowerUp.PowerUpType.DYNAMITE) {
                 SoundEffectEnum.EXPLODE.playSound();
                 strikeBar.removeStrike();
             } else if (p.getType() == PowerUp.PowerUpType.BLASTER) {
                 SoundEffectEnum.SPEED_UP.playSound();
                 otherScreen.powerUpSpeedFactor = 1.5;
                 otherScreen.timeToRemovePowerUp = otherScreen.lastWallTimeSec + 15;
             } else if (p.getType() == PowerUp.PowerUpType.TURTLE) {
                 SoundEffectEnum.SLOW_DOWN.playSound();
                 powerUpSpeedFactor = 0.5;
                 timeToRemovePowerUp = lastWallTimeSec + 15;
             }
             feedbackDisplay.makeDisplay(m, currentTimeSec, true); //Display correct image for powerup
         }
     }
 
     /**
      * Given the recyclable and the bin it went into this function either increments the score or adds a strike
      *
      * @param movableRecyclable
      * @param bin
      */
     public void handleScore(Movable movableRecyclable, RecycleBin bin) {
         if (movableRecyclable instanceof Recyclable) {
             if (bin.isCorrectRecyclableType(movableRecyclable)) {
                 feedbackDisplay.makeDisplay(movableRecyclable, currentTimeSec, true);
                 SoundEffectEnum.CORRECT.playSound();
                 bin.addItem();
                 boolean comboScore = bin.handleBinLevel(recycleBins.getSide());
                 if (isPlaying) {
                     gameStatusDisplay.incrementScore(GameConstants.ITEM_SCORE);
                     if (comboScore) {
                         gameStatusDisplay.incrementScore(GameConstants.COMBO_SCORE);
                     }
                 }
             } else {
                 Movable feedback[] = feedbackDisplay.makeDisplay(movableRecyclable, currentTimeSec, false);
                 Boolean barFull = strikeBar.addStrike(feedback);
                 if(barFull){
                     if (!playerIsAComputer){
                       if (!scoreSubmitted){
                           SavePlayer currentPlayer = new SavePlayer();
                           currentPlayer.submitPlayerScore(gameStatusDisplay.getScore());
                           scoreSubmitted = true;
                       }
                     }
                     isPlaying = false;
 
                 }
                 SoundEffectEnum.INCORRECT.playSound();
             }
         }
     }
 
     /**
      * Allows the two sides of the screen to be aware of each other so that the powerup that speeds up
      * the other sides conveyor can function
      *
      * @param otherScreen
      */
     public void addLinkToOtherScreen(GameLogic otherScreen) {
         this.otherScreen = otherScreen;
     }
 
     protected void updateThis() {
         updateTime();
 
         if (wallTimeSec >= timeToRemovePowerUp) {
             powerUpSpeedFactor = 1;
         }
         if (playerIsAComputer) {
             computerPlayer.updateAI(conveyorBelt.getNextMovableThatIsTouchable(), currentTimeSec);
             computerPlayer.primary.updateLocation();
         }
 
 
         // Handle existing item collisions
         lookForAndHandleCollisions();
 
         // Move Items
         conveyorBelt.moveItems(currentTimeSec);
         theForce.moveItems(currentTimeSec);
         feedbackDisplay.moveItems(currentTimeSec);
 
         // Release items at the end of the path
         List<Movable> itemsToRemove = conveyorBelt.releaseControlOfMovablesAtEndOfPath(currentTimeSec);
         if(itemsToRemove.size()>0) SoundEffectEnum.TRASH_BIN.playSound();
         for (Movable m : itemsToRemove) {
             handleScore(m, RecycleBin.TRASH_BIN);
             gameScreen.removeSprite(m.getSprite());
         }
 
         itemsToRemove = theForce.releaseControlOfMovablesAtEndOfPath(currentTimeSec);
         for (Movable m : itemsToRemove) {
             if (m instanceof Recyclable) {
                 RecycleBin bin = recycleBins.findBinForFallingRecyclable(m);
                 bin.getSoundEffect().playSound();
 
                 handleScore(m,bin);
             }
             gameScreen.removeSprite(m.getSprite());
         }
 
         // Generate more items, if we feel like it
         if (!debuggingCollisions) {
             Movable m = factory.possiblyGenerateItem(conveyorBelt.getConveyorPath(), currentTimeSec, recycleBins);
             if (m != null) {
                 try {
                     conveyorBelt.takeControlOfMovable(m);
                     gameScreen.addSprite(m.getSprite());
                     SoundEffectEnum.ITEM_EXIT_CHUTE.playSound();
                 } catch (ExceptionInInitializerError e) {
                     logger.error("ExceptionInInitializerError adding Recyclable with time " + currentTimeSec);
                 }
             }
         }
         increaseDifficulty();
     }
 
     private void updateTime() {
        if(started == false){
            startTime = System.currentTimeMillis() ;
            started = true;
        }
         wallTimeSec = (System.currentTimeMillis() - startTime) / 1000.0;
         currentTimeSec += (wallTimeSec - lastWallTimeSec) * timeSpeedFactor * powerUpSpeedFactor;
         lastWallTimeSec = wallTimeSec;
     }
 
     private void increaseDifficulty() {
         possiblyIncreaseTypesInUse();
         increaseSpeed();
         increaseItemGenerationProbability();
     }
 
     private void possiblyIncreaseTypesInUse() {
         if (numItemTypesInUse < GameConstants.MAX_ITEM_COUNT && Math.round(currentTimeSec) > nextItemTypeGenerationTime) {
             numItemTypesInUse++;
             nextItemTypeGenerationTime += GameConstants.TIME_TO_ADD_NEW_ITEM_TYPE;
             factory.setNumItemTypesInUse(numItemTypesInUse);
         }
     }
 
     private void increaseSpeed() {
         double pctToMaxDifficulty = Math.min(1, wallTimeSec / GameConstants.TIME_TO_MAX_DIFFICULTY);
         timeSpeedFactor = pctToMaxDifficulty * (GameConstants.FINAL_TIME_SPEED_FACTOR - 1) + 1;
     }
 
     private void increaseItemGenerationProbability() {
         double pctToMaxDifficulty = Math.min(1, wallTimeSec / GameConstants.TIME_TO_MAX_DIFFICULTY);
         factory.increaseGenerationRate(pctToMaxDifficulty);
     }
 
     public boolean getState(){
         return isPlaying;
     }
 
 }
