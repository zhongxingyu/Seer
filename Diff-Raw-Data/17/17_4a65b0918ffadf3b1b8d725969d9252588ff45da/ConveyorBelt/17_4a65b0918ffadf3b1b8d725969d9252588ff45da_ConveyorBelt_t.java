 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.frontend.graphics.GameScreen;
 import edu.mines.csci598.recycler.frontend.graphics.Line;
 import edu.mines.csci598.recycler.frontend.graphics.Path;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import org.apache.log4j.Logger;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 public class ConveyorBelt {
 	private static final Logger logger = Logger.getLogger(ConveyorBelt.class);
 	
     private Random random;
     private List<Recyclable> recyclables;
     private double lastMotionTimeSec;
 	private double nextTimeToGenerate;
     private double meanTimeBetweenItemGeneration;
     private GameLogic game;
     private GameScreen gameScreen;
     private Path path;
     private double speedPixPerSecond;
     
     private  boolean debuggingCollisions;
 
 
     //Left Path
     private static final Line bottomLineLeft = new Line(GameConstants.LEFT_BOTTOM_PATH_START_X, GameConstants.LEFT_BOTTOM_PATH_START_Y,
             GameConstants.LEFT_BOTTOM_PATH_END_X, GameConstants.LEFT_BOTTOM_PATH_END_Y);
     private static final Line verticalLineLeft = new Line(GameConstants.LEFT_VERTICAL_PATH_START_X, GameConstants.LEFT_VERTICAL_PATH_START_Y,
             GameConstants.LEFT_VERTICAL_PATH_END_X, GameConstants.LEFT_VERTICAL_PATH_END_Y);
     private static final Line topLineLeft = new Line(GameConstants.LEFT_TOP_PATH_START_X, GameConstants.LEFT_TOP_PATH_START_Y,
             GameConstants.LEFT_TOP_PATH_END_X, GameConstants.LEFT_TOP_PATH_END_Y);
     public static final Path CONVEYOR_BELT_PATH_LEFT = new Path(Arrays.asList(bottomLineLeft, verticalLineLeft, topLineLeft));
 
     //Right Path
     private static final Line bottomLineRight= new Line(GameConstants.RIGHT_BOTTOM_PATH_START_X, GameConstants.RIGHT_BOTTOM_PATH_START_Y,
             GameConstants.RIGHT_BOTTOM_PATH_END_X, GameConstants.RIGHT_BOTTOM_PATH_END_Y);
     private static final Line verticalLineRight = new Line(GameConstants.RIGHT_VERTICAL_PATH_START_X, GameConstants.RIGHT_VERTICAL_PATH_START_Y,
             GameConstants.RIGHT_VERTICAL_PATH_END_X, GameConstants.RIGHT_VERTICAL_PATH_END_Y);
     private static final Line topLineRight = new Line(GameConstants.RIGHT_TOP_PATH_START_X, GameConstants.RIGHT_TOP_PATH_START_Y,
             GameConstants.RIGHT_TOP_PATH_END_X, GameConstants.RIGHT_TOP_PATH_END_Y);
     public static final Path CONVEYOR_BELT_PATH_RIGHT = new Path(Arrays.asList(bottomLineRight,verticalLineRight,topLineRight));
 
     public ConveyorBelt(GameLogic game,GameScreen gameScreen,Path path,boolean debuggingCollisions) {
         this.game = game;
         this.gameScreen = gameScreen;
     	this.path = path;
     	recyclables = new ArrayList<Recyclable>();
         this.debuggingCollisions=debuggingCollisions;
         nextTimeToGenerate = 0;
         meanTimeBetweenItemGeneration = GameConstants.MIN_TIME_BETWEEN_GENERATIONS;
         
         speedPixPerSecond = GameConstants.INITIAL_SPEED_IN_PIXELS_PER_SECOND;
         
         //logger.setLevel(Level.DEBUG);
         random = new Random(System.currentTimeMillis());
     }
 
     public List<Recyclable> getRecyclables() {
         return recyclables;
     }
     
     public int getNumRecyclablesOnConveyor(){
         return recyclables.size();
     }
     
     /*
      * Returns the next touchable recyclable
      */
     public Recyclable getNextRecyclableThatIsTouchable(){
         Recyclable ret;
         int index=0;
         ret = recyclables.get(index);
         while(!(ret.isTouchable()) && index < recyclables.size()){
             index++;
             if(index<recyclables.size()-1)
                 ret = recyclables.get(index);
         }
 
         return ret;
     }
 
     public void addRecyclable(Recyclable r) {
         recyclables.add(r);
 		game.addRecyclable(r);
     }
 
     public void removeRecyclable(Recyclable r) {
         recyclables.remove(r);
     }
 
     public void setSpeed(double pctOfFullSpeed) {
     	final double baseSpeedPixPerSecond = GameConstants.INITIAL_SPEED_IN_PIXELS_PER_SECOND;
         final double maxSpeedPixPerSecond = GameConstants.FINAL_SPEED_IN_PIXELS_PER_SECOND;
         
     	if(pctOfFullSpeed >= 1){
     		speedPixPerSecond = maxSpeedPixPerSecond;
     	}
     	
     	speedPixPerSecond = baseSpeedPixPerSecond + (maxSpeedPixPerSecond - baseSpeedPixPerSecond) * pctOfFullSpeed;
     }
 
 	/**
 	 * Generates new item if necessary
 	 */
 	private void possiblyGenerateItem(double currentTimeSec) {
 		if (needsItemGeneration(currentTimeSec)) {
 			// Recyclables initially take the path of the conveyor belt
 			Recyclable r = RecyclableFactory.generateRandom(path, game.getNumItemTypesInUse());
 			addRecyclable(r);
 			logger.debug("Item generated: " + r);
 		}
     }
 	
     /**
      * Determines if item should be generated.  If true, sets the time for the next object to be generated
      *
      * @return true if item should be generated, false otherwise
      */
     private boolean needsItemGeneration(double currentTimeSec) {
 		if (currentTimeSec > nextTimeToGenerate) {
 			double timeToAdd = random.nextGaussian()
 					+ meanTimeBetweenItemGeneration;
 			nextTimeToGenerate = currentTimeSec + Math.max(timeToAdd, GameConstants.MIN_TIME_BETWEEN_GENERATIONS);
 			
 			logger.debug("Current time: " + currentTimeSec);
 			logger.debug("Time for next item generation: " + nextTimeToGenerate);
 			
 			return true;
 		}
 		return false;
     }
 
 	public void update(double currentTimeSec) {
 		// Move the conveyor
         moveConveyorBelt(currentTimeSec);
         
         // Generate more items, if we feel like it
         if (!debuggingCollisions) {
             possiblyGenerateItem(currentTimeSec);
         }
 	}    
 
 	/**
 	 * Advances the conveyor belt.  All the items on it will get carried along for the ride!
 	 * @param currentTimeSec in milliseconds
 	 */
 	private void moveConveyorBelt(double currentTimeSec) {
 		ArrayList<Recyclable> recyclablesToRemove = new ArrayList<Recyclable>();
         
 		// Figure out how much time has passed since we last moved
 		double timePassedSec = currentTimeSec - lastMotionTimeSec;
 
         // Update all the current items
 		for(Recyclable recyclable : recyclables){
             Point2D newPosition = recyclable.getPath().getLocation(recyclable.getPosition(), speedPixPerSecond, timePassedSec);
 			if(newPosition.getY()<GameConstants.SPRITE_BECOMES_UNTOUCHABLE){
                 recyclable.setMotionState(MotionState.CHUTE);
 
             }else if(newPosition.getY()<GameConstants.SPRITE_BECOMES_TOUCHABLE){
                 if(recyclable.getMotionState()==MotionState.CHUTE){
                 	recyclable.setMotionState(MotionState.CONVEYOR);
                 }
             }
             if(newPosition.equals(recyclable.getPath().finalPosition())){
                 recyclablesToRemove.add(recyclable);
                 game.handleScore(recyclable, RecycleBin.TRASH_BIN);
 			}
             recyclable.setPosition(newPosition);
 
 		}
 		
 		// Some need to be removed
         // Can't modify in previous loop because it is iterating need this to avoid
         // a concurrent modification exception.
         for (Recyclable recyclable : recyclablesToRemove) {
         	// Removal involves the item in the linked list
             removeRecyclable(recyclable);
             
             // And the image on the screen
             game.getGameScreen().removeSprite(recyclable.getSprite());
         }
 		lastMotionTimeSec = currentTimeSec;
 	}
 }
