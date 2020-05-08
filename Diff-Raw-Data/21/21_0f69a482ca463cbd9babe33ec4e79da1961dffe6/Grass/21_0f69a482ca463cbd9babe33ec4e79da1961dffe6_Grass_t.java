 package VK.model.actors;
 
 import java.util.List;
 
 import VK.view.Field;
 import VK.view.Location;
 
 /**
  * The class Grass
  * Creates and has all the settings for Grass
  * @author Pim
  *
  */
 public class Grass extends Animal {
 
 	// The age at which a fox can start to breed.
 	public static int breedingAge = 1;
 	// The likelihood of a fox breeding.
 	public static double breedingProbability = 0.45;
 	// The maximum number of births.
 	public static int maxLitterSize = 1;
 
 	/**
 	 * Constructs a new piece of Grass
 	 * 
 	 * @param Field fieldInput
 	 * @param LocationlocationInput
 	 */
 	public Grass(Field fieldInput, Location locationInput) {
 		super(fieldInput, locationInput);
 		this.age = 1;
 	}
 
 	/**
 	 * This is what the grass does most of the time: "breeding".
 	 * In the process, it might breed 
 	 * @param newGrass A list to add newly born grass to.
 	 */
	@Override
 	public void act(List<Actor> newGrass)
 	{
 		if(isAlive()) {
 			giveBirth(newGrass);
 		}
 	}
 
 	/**
 	 * Returns the breeding probability of a piece of grass
 	 * @return the breedingprobability
 	 */
	@Override
 	protected double getBreedingProbability() {
 		return Fox.breedingProbability;
 	}
 
 	/**
 	 * Returns the breeding probability as integer
 	 * @return the breeding probability as interger
 	 */
 	public static int getBreedingProbabilityInt() {
 		int i = ((int) (breedingProbability*100));
 		return i;
 	}
 
 	/**
 	 * Sets the breeding probability
 	 * @param int j the new breeding probability
 	 */
 	public static void setBreedingProbabilityInt(int j) {
 		breedingProbability = ((double) j/100);
 	}
 
 	/**
 	 * Gets the maximum litter size of a piece of Grass
 	 * @return int the maximum litter size
 	 */
	@Override
 	protected int getMaxLitterSize(){
 		return maxLitterSize;
 	}
 	
 	/**
 	 * Gets the minimum breeding age of a piece of Grass
 	 * @return int the breeding age
 	 */
	@Override
 	protected int getBreedingAge() {
 		return breedingAge;
 	}
 
 	/**
 	 * Returns the maximum age a bear can get
 	 * @return int 0 by default, Grass has no age.
 	 */
 	@Override
 	protected int getMaxAge(){
 		return 0;
 	}
 }
