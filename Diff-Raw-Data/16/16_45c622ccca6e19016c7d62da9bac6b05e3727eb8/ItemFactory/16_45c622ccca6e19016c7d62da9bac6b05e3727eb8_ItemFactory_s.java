 package edu.mines.csci598.recycler.frontend;
 
 import edu.mines.csci598.recycler.frontend.graphics.Path;
 import edu.mines.csci598.recycler.frontend.motion.Movable;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import org.apache.log4j.Logger;
 
 import java.util.Random;
 
 /**
  * A factory to create Recyclables
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/21/12
  * Time: 12:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public final class ItemFactory {
 
     /**
      * The percentage of time a powerup comes on the screen. 100% means it will come up everytime an item is going to be
      * generated
      */
    public static final int POWERUP_FREQUENCY_PERCENTAGE = 70;
     private final Logger logger = Logger.getLogger(ItemFactory.class);
     //logger.setLevel(Level.INFO);
     private final Random rand = new Random();
 	private double nextTimeToGenerate = 0;
     private double meanTimeBetweenItemGeneration = GameConstants.INITIAL_TIME_BETWEEN_GENERATIONS;
     private int numberOfItemTypes = 1;
 
     /**
      * Returns a random RecyclableType of the first <em>numberOfItemTypesInUse</em> types.
      * If <em>numberOfItemTypesInUse</em> is greater than the total number of items in the game,
      * the total number is used instead.
      *
      * @param numberOfItemTypesInUse
      * @return
      */
     // TODO this shouldn't really depend on the order we typed items above
     private Recyclable generateRandom(Path path, int numberOfItemTypesInUse) {
         int numRecyclableTypes = RecyclableType.values().length;
 
         if (numberOfItemTypesInUse > numRecyclableTypes) { // error checking, just use the number of recyclables instead
             numberOfItemTypesInUse = numRecyclableTypes;
         }
 
         // Generates a choice between 0 and the number of items in use passed in, or the total number available
         int randomChoiceIndex = rand.nextInt(numberOfItemTypesInUse);
         RecyclableType type = RecyclableType.values()[randomChoiceIndex];
 		String[] possibleImages = type.getImagePaths();
         return new Recyclable(type, path, possibleImages[(int)(Math.floor(Math.random() * possibleImages.length))]);
     }
 
     private PowerUp generateRandomPowerUp(Path path) {
         int randomChoiceIndex = rand.nextInt(PowerUp.PowerUpType.values().length);
         PowerUp.PowerUpType type = PowerUp.PowerUpType.values()[randomChoiceIndex];
         return new PowerUp(type, path, type.getImage());
     }
     
 
 
 	/**
 	 * Generates new item if necessary
 	 * @return A new recyclable if it's been long enough and we get lucky, null otherwise
 	 */
 	public Movable possiblyGenerateItem(Path outputPath, double currentTimeSec) {
         outputPath.setStartTime(currentTimeSec);
 		Movable movable = null;
 		if (needsItemGeneration(currentTimeSec)) {
             // a very simple way to have powerups not generated frequently
             Random randomGenerator = new Random();
             int ranNum = randomGenerator.nextInt(100);
             if (ranNum < POWERUP_FREQUENCY_PERCENTAGE) {
             	movable = generateRandomPowerUp(outputPath);
                 logger.debug("Powerup generated: " + movable);
             }
             else {
                 // Recyclables initially take the path of the conveyor belt
             	movable = generateRandom(outputPath, numberOfItemTypes);
                 logger.debug("Item generated: " + movable);
             }
 		}
 		return movable;
     }
     public Recyclable generateItemForDebugging(Path outputPath){
         Recyclable returned = generateRandom(outputPath,1);
         return returned;
     }
 	
     /**
      * Determines if item should be generated.  If true, sets the time for the next object to be generated
      *
      * @return true if item should be generated, false otherwise
      */
     private boolean needsItemGeneration(double currentTimeSec) {
 		if (currentTimeSec > nextTimeToGenerate) {
 			double timeToAdd = rand.nextGaussian()
 					+ meanTimeBetweenItemGeneration;
 			nextTimeToGenerate = currentTimeSec + Math.max(timeToAdd, GameConstants.MIN_TIME_BETWEEN_GENERATIONS);
 			
 			logger.debug("Current time: " + currentTimeSec);
 			logger.debug("Time for next item generation: " + nextTimeToGenerate);
 			
 			return true;
 		}
 		return false;
     }
 
 	public void setNumItemTypesInUse(int numItemTypesInUse) {
 		numberOfItemTypes = numItemTypesInUse;
 	}
 
 	/**
 	 * Reduces the time between item generations according to the perecentage of the game completed.  At 0% we will use
 	 * <code>GameConstants.INITIAL_TIME_BETWEEN_GENERATIONS</code> and at 100% we will use
 	 * <code>GameConstants.MIN_TIME_BETWEEN_GENERATIONS</code>
 	 * @param pctToMaxDifficulty
 	 */
 	public void increaseGenerationRate(double pctToMaxDifficulty) {
 		if(pctToMaxDifficulty < 0 || pctToMaxDifficulty > 1){
 			throw new IllegalArgumentException("Can only handle times between 0% and 100% of the game's time to max difficulty");
 		}
 		double timeRange = GameConstants.INITIAL_TIME_BETWEEN_GENERATIONS - GameConstants.MIN_TIME_BETWEEN_GENERATIONS;
 		meanTimeBetweenItemGeneration = GameConstants.INITIAL_TIME_BETWEEN_GENERATIONS - (timeRange * pctToMaxDifficulty);
 	}
 }
