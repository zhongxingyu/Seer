 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.backend.GameManager;
 import edu.mines.csci598.recycler.frontend.graphics.*;
 import edu.mines.csci598.recycler.frontend.motion.ConveyorBelt;
 import edu.mines.csci598.recycler.frontend.motion.FeedbackDisplay;
 import edu.mines.csci598.recycler.frontend.motion.TheForce;
 import edu.mines.csci598.recycler.frontend.motion.Movable;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 
 
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
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
     private List<Hand> hands;
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
     private int strikes;
     private StrikeBar strikeBar;
     private boolean gameOverNotified = false;
     private boolean playerIsAComputer;
     private boolean debuggingCollisions;
     private GameStatusDisplay gameStatusDisplay;
     //TODO game manager should be removed from this class when the idea of players having hands is dissolved
     private GameManager gameManager;
     private double timeToRemovePowerUp;
 
 
     public GameLogic(RecycleBins recycleBins, Path conveyorPath, GameManager gameManager, GameStatusDisplay gameStatusDisplay,
                     boolean playerIsAComputer, boolean debuggingCollision) {
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
         startTime = System.currentTimeMillis();
         this.playerIsAComputer = playerIsAComputer;
         this.debuggingCollisions = debuggingCollision;
         strikeBar = new StrikeBar(gameStatusDisplay);
 
         hands = new ArrayList<Hand>();
 
         // sets up the first player and adds its primary hand to the gameScreen
         // so that it can be displayed
         if (!this.playerIsAComputer) {
            //   player1 = new Player(gameManager);
            // creates the max number of hands that can be displayed which is 4
            //TODO: This is going to be a problem - each player might have 2 hands, but how do we know whose hands are whose?
            for (int i = 0; i < 2; i++) {
                 hands.add(new PlayerHand(gameManager, i));
                 gameScreen.addHandSprite(hands.get(hands.size() - 1).getSprite());
             }
         } else {
             computerPlayer = new ComputerPlayer(recycleBins);
             gameScreen.addHandSprite(computerPlayer.primary.getSprite());
             hands.add(computerPlayer.getHand());
         }
         //Add recycle bins to game screen to be drawn
         for (RecycleBin bin : recycleBins.getRecycleBins()) {
             if (bin.hasSprite()) {
                 gameScreen.addRecycleBinSprite(bin.getSprite());
             }
         }
         //Add single item to conveyer for debugging collisions.
         if (this.debuggingCollisions) {
             logger.debug("Adding recyclable for collision detection");
             Recyclable r = factory.generateItemForDebugging(conveyorBelt.getConveyorPath());
             conveyorBelt.takeControlOfMovable(r);
             gameScreen.addSprite(r.getSprite());
         }
     }
 
     private void lookForAndHandleCollisions() {
 
         // checks to see if there is a collision with any one of the hands
         for (Hand hand : hands) {
 
             //If the hand isn't moving fast enough to swipe, skip it
             if (Math.abs(hand.getVelocityX()) < GameConstants.MIN_HAND_VELOCITY) {
                 continue;
             }
             // If we get here, it is causing collisions with anything at this location.
 
             List<Movable> swipedOffConveyor = conveyorBelt.releaseTouchableItemsAtPoint(hand.getPosition());
             logger.debug("swipedSize="+swipedOffConveyor.size());
             // We should really check the theForce also, but we're not allowing things it controls to be touchable, so it would be kind of silly.
 
             handleCollisions(hand, swipedOffConveyor);
 
         }
     }
 
     public void handleCollisions(Hand hand, List<Movable> swipedOffConveyor) {
         for (Movable m : swipedOffConveyor) {
 
             //Rough way to make items fall at different speeds
             double travelTime = Math.abs(GameConstants.ITEM_PATH_END / hand.getVelocityX());
             //Don't let it go too fast.
             travelTime = Math.max(travelTime, 0.3);
             //or too slow
             travelTime = Math.min(travelTime, 2);
             //Account for the speedup
             travelTime *= timeSpeedFactor;
 
             Coordinate position = m.getPosition();
             Line collideLine = null;
 
             if (m instanceof Recyclable) {
                 if (hand.getVelocityX() >= GameConstants.MIN_HAND_VELOCITY) {
                     //logger.debug("Pushed Right");
                     m.setMotionState(MotionState.FALL_RIGHT);
                     RecycleBin destBin = recycleBins.findBinForFallingRecyclable(m);
                     collideLine = new Line( position.getX(), position.getY(),
                              position.getX() + GameConstants.ITEM_PATH_END,  destBin.getMidPoint(),
                             travelTime,Math.PI*2);
 
                 } else if (hand.getVelocityX() <= -1 * GameConstants.MIN_HAND_VELOCITY) {
                     //logger.debug("Pushed Left");
                     m.setMotionState(MotionState.FALL_LEFT);
                     RecycleBin destBin = recycleBins.findBinForFallingRecyclable(m);
                     collideLine = new Line( position.getX(), position.getY(),
                              position.getX() - GameConstants.ITEM_PATH_END, destBin.getMidPoint(),
                             travelTime,Math.PI*2);
                 } else {
                     throw new IllegalStateException("It really shouldn't be possible to get here!  The hand wasn't moving fast enough to make the conveyor release control!");
                 }
             }
             else if (m instanceof PowerUp) {
                 m.setMotionState(MotionState.NONE);
                 collideLine = new Line( m.getPosition(), m.getPosition(), 0.5);
             }
 
             Path path = new Path(currentTimeSec);
             path.addLine(collideLine);
             m.setPath(path);
 
             // handle powerups
             if (m instanceof PowerUp) {
                 PowerUp p = (PowerUp) m;
                 if (p.getType() == PowerUp.PowerUpType.DYNAMITE) {
                     strikeBar.removeStrike();
                     feedbackDisplay.makeDisplay(m.getPosition(), currentTimeSec, true);
                 }else if (p.getType() == PowerUp.PowerUpType.BLASTER) {
                     logger.info("Rabbit Powerup");
                     feedbackDisplay.makeDisplay(m.getPosition(), currentTimeSec, true);
                     otherScreen.powerUpSpeedFactor = 1.5;
                     otherScreen.timeToRemovePowerUp = otherScreen.lastWallTimeSec + 15;
                 }
                 else if (p.getType() == PowerUp.PowerUpType.TURTLE) {
                     logger.info("Turtle Powerup");
                     feedbackDisplay.makeDisplay(m.getPosition(), currentTimeSec, true);
                     powerUpSpeedFactor = 0.5;
                     timeToRemovePowerUp = lastWallTimeSec + 15;
                 }
             }
         }
 
         // We now have a list of items that the conveyor belt has released, and they know where they are going.
         // Handing control to TheForce!
         theForce.takeControlOfMovables(swipedOffConveyor);
         conveyorBelt.releaseMovables(swipedOffConveyor);
     }
 
     /**
      * Given the recyclable and the bin it went into this function either increments the score or adds a strike
      *
      * @param m
      * @param bin
      */
     public void handleScore(Movable m, RecycleBin bin) {
         if (m instanceof Recyclable) {
             if (bin.isCorrectRecyclableType(m))  {
                 feedbackDisplay.makeDisplay(m.getPosition(), currentTimeSec, true);
                 SoundEffectEnum.CORRECT.playSound();
                 gameStatusDisplay.incrementScore(10);
             }
             else {
                 Movable feedback = feedbackDisplay.makeDisplay(m.getPosition(), currentTimeSec, false);
                 strikeBar.addStrike(feedback);
                 SoundEffectEnum.INCORRECT.playSound();
             }
 
         }
         //TODO: Do we need to handle powerups here?
     }
 
 
     public void addLinkToOtherScreen(GameLogic otherScreen) {
         this.otherScreen = otherScreen;
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
 
     public GameManager getGameManager() {
         return gameManager;
     }
 
     public GameScreen getGameScreen() {
         return gameScreen;
     }
 
     protected void updateThis() {
         updateTime();
 
         if (wallTimeSec >= timeToRemovePowerUp) {
             powerUpSpeedFactor = 1;
         }
         if(playerIsAComputer){
             computerPlayer.updateAI(conveyorBelt.getNextMovableThatIsTouchable(), currentTimeSec);
         }
 
             // display the hands
         for (Hand hand : hands) {
             hand.updateLocation();
         }
 
 
         // Handle existing item collisions
         lookForAndHandleCollisions();
 
         // Move Items
         conveyorBelt.moveItems(currentTimeSec);
         theForce.moveItems(currentTimeSec);
         feedbackDisplay.moveItems(currentTimeSec);
         // Release items at the end of the path
         List<Movable> itemsToRemove = conveyorBelt.releaseControlOfMovablesAtEndOfPath(currentTimeSec);
         for (Movable m : itemsToRemove) {
             handleScore(m, RecycleBin.TRASH_BIN);
             gameScreen.removeSprite(m.getSprite());
         }
         itemsToRemove = theForce.releaseControlOfMovablesAtEndOfPath(currentTimeSec);
         for (Movable m : itemsToRemove) {
             if (m instanceof Recyclable) {
                 handleScore(m, recycleBins.findBinForFallingRecyclable(m));
             }
             gameScreen.removeSprite(m.getSprite());
         }
 
         // Generate more items, if we feel like it
         if (!debuggingCollisions) {
             Movable m = factory.possiblyGenerateItem(conveyorBelt.getConveyorPath(), currentTimeSec);
             if (m != null) {
                 try {
                     conveyorBelt.takeControlOfMovable(m);
                     gameScreen.addSprite(m.getSprite());
                 } catch (ExceptionInInitializerError e) {
                     logger.error("ExceptionInInitializerError adding Recyclable with time " + currentTimeSec);
                 }
             }
         }
         increaseDifficulty();
 
     }
 
     private void updateTime() {
         wallTimeSec = (System.currentTimeMillis() - startTime) / 1000.0;
         currentTimeSec += (wallTimeSec - lastWallTimeSec) * timeSpeedFactor * powerUpSpeedFactor;
         lastWallTimeSec = wallTimeSec;
 
     }
 }
