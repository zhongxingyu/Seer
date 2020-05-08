 package net.esiade.client.sprite;
 
 import com.google.gwt.user.client.Random;
 
 import net.esiade.client.Esiade;
 import net.esiade.client.EvolutionCore;
 import net.esiade.client.Vector2D;
 
 
 /**
  * @author Lukas Klingsbo
  * 
  */
 public class Individual extends MovingSprite implements Comparable<Individual> {
 	private int food;
 	private int generation;
 	private int veloCheck;
 	private int counter = 0;
 	private double mapTrust;
 	private int starveRate;
 	private int selfReproductionLimit;
 	private int reproductionLimit;
 	private int foodStart;
 	private double jumpLength;
 	public Vector2D[][] genome;
 	
 	/**
 	 * @param position Start position of the individual
 	 * @param velocity The velocity of the individual
 	 * @param genome The genome of the individual
 	 * @param veloCheck How often do we update the velocity, number of days
 	 * @param mapTrust How often the individual uses the matrix's vectors. Between 0.0 and 1.0
 	 * @param starveRate How many days the individual survives on a single food instance. (Only in collision-based mode)
 	 * @param selfReproductionLimit How much food that is needed for self-reproduction. (Only in collision-based mode)
 	 * @param reproductionLimit How much food that is needed for reproduction. (Only in collision-based mode)
 	 * @param foodStart Food started with.  (Only in collision-based mode)
 	 * @param jumpLength Additional speed, between 0.0 and 1.0 where 1.0 is the length of square in the map corresponding to a position in the matrix.
 	 * @param generation The generation of the individual.
 	 */
 	public Individual(Vector2D position, Vector2D velocity, Vector2D[][] genome, 
 					int veloCheck, double mapTrust,	int starveRate,
 					int selfReproductionLimit, int reproductionLimit, int foodStart, 
 					double jumpLength, int generation) {
 		super("./individual.png", position, velocity);
 		this.velocity = genome[(int)(position.x/Esiade.WIDTH*EvolutionCore.WIDTH)]
 							[(int)(position.y/Esiade.HEIGHT*EvolutionCore.HEIGHT)];
 		this.veloCheck = veloCheck;
 		this.mapTrust = mapTrust;
 		this.starveRate = starveRate;
 		this.selfReproductionLimit = selfReproductionLimit;
 		this.reproductionLimit = reproductionLimit;
 		this.foodStart = foodStart;
 		this.food = foodStart;
 		this.jumpLength = jumpLength;
 		this.genome = genome;
 		this.generation = generation;
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see net.esiade.client.sprite.MovingSprite#updatePos()
 	 */
 	public void updatePos() {
 //		Update the position to a new position with the vectors, 
 //		collisions have been taken care of.
 		if(counter==veloCheck) {
 			if(Random.nextDouble() <= mapTrust)
 				velocity = genome[(int)(position.x/Esiade.WIDTH*EvolutionCore.WIDTH)]
 								[(int)(position.y/Esiade.HEIGHT*EvolutionCore.HEIGHT)];
 			else
 				velocity = new Vector2D(jumpLength);
 			counter = 0;
 		} else {
 			counter++;
 		}
 		position.add(velocity);
 	}
 
 	/**
 	 * This function increases the amount of food stored in the individual.
 	 * @return If the amount of food is greater than selfReproductionLimit, return true, else return false
 	 */
 	public void eat() {
 		food++;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	public Individual clone() {
 		return new Individual(position, velocity, genome, veloCheck, 
 							mapTrust, starveRate, selfReproductionLimit, 
 							reproductionLimit, food, jumpLength, generation);
 	}
 	
 	/**
 	 * @return The number of food in an individual
 	 */
 	public int getFood() {
 		return food;
 	}
 	
	public double getJumpLength() {
		return jumpLength;
	}
	
 	/**
 	 * @return The generation-number of an individual
 	 */
 	public int getGeneration() {
 		return generation;
 	}
 
 	
 	/**
 	 * @return The number of food the individual needs in order to reproduce
 	 */
 	public int getReproductionLimit() {
 		return reproductionLimit;
 	}
 
 	/**
 	 * Decreases the number of food in an individual.
 	 */
 	public void starve() {
 		food--;
 	}
 	
 	/**
 	 * Set the number of food in an individual to the starting number.
 	 */
 	public void resetFood() {
 		food = foodStart;
 	}
 
 	/**
 	 * @return Return the speed in which the individual loses a single food instance.
 	 */
 	public int getStarveRate() {
 		return starveRate;
 	}
 
 	/**
 	 * Increases the generation of the individual by 1.
 	 */
 	public void increaseGen() {
 		generation++;
 	}
 
 	@Override
 	public int compareTo(Individual i) {
 		return i.getFood()-getFood();
 	}
 
 
 	public int getSelfReproductionLimit() {
 		return selfReproductionLimit;
 	}
 }
