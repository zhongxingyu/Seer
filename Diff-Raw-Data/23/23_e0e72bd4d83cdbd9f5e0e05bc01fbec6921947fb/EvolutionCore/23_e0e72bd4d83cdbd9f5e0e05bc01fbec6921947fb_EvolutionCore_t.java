 package net.esiade.client;
 
 import com.google.gwt.user.client.Random;
 
 import net.esiade.client.sprite.Individual;
 
 /**
  * @author jonathan
  *
  */
 public class EvolutionCore {
 	public static int WIDTH, HEIGHT;
 	private static double mRate, cRate;
 	private static CType type;
 	
 	/**
 	 * @param width The width of the map matrix
 	 * @param height The height of the map matrix
 	 * @param mRate The mutation rate
 	 * @param cRate The crossover rate
 	 */
 	public EvolutionCore(int width, int height, double mRate, double cRate, CType type){
 		EvolutionCore.WIDTH = width;
 		EvolutionCore.HEIGHT = height;
 		EvolutionCore.mRate = mRate;
 		EvolutionCore.cRate = cRate;
 		EvolutionCore.type = type;
 	}
 	
 	/**
 	 * @param width 
 	 * @param height
 	 */
 	public static void setDimensions(int width, int height) {
 		EvolutionCore.WIDTH = width;
 		EvolutionCore.HEIGHT = height;
 	}
 
 	/**
 	 * @author jonathan
 	 * An enumeration of crossover-types. The names are self-explanatory.
 	 */
 	public static enum CType {
 		ONEPOINT, TWOPOINT, UNIFORM
 	}	
 	
 	/**
 	 * @return This function returns a genome with all vectors set at random.
 	 */
 	public static Vector2D[][] getRandomGenome() {
 		Vector2D[][] genome = new Vector2D[WIDTH][HEIGHT];
 		for (int x = 0;x < WIDTH;x++)
 			for (int y = 0; y < HEIGHT;y++)
 				genome[x][y] = new Vector2D();
 		return genome;
 	}
 
 	/**
 	 * This function will change the vector with the position (x,y) in the matrix between I1 and I2
 	 * @param I1 The first individual
 	 * @param I2 The second individual
 	 * @param x The x-coordinate in the matrix 
 	 * @param y The y-coordinate in the matrix
 	 * 
 	 */
 	public static void SwitchVectors(Individual I1, Individual I2, int x, int y){
 		Vector2D temp;		
 		temp = I1.genome[x][y];
 		I1.genome[x][y] = I2.genome[x][y];
 		I2.genome[x][y] = temp;
 	}
 	
 	
 	/**
 	 * This is a general function for crossover, this function redirects to the appropriate crossover subfunction. The subfunctions implement common crossover operations, but applied on vectors instead of bits.
 	 * @param I1 The first individual
 	 * @param I2 The second individual
 	 */
 	public static void Crossover(Individual I1, Individual I2) {
 	if (Random.nextDouble() < cRate)
 		if (type == CType.ONEPOINT)
 			OnePointCrossover(I1, I2);
 		else if (type == CType.TWOPOINT)
 			TwoPointCrossover(I1, I2);
 		else if (type == CType.UNIFORM)
 			UniformCrossover(I1, I2);
 
 		Mutation(I1);
 		Mutation(I2);
		I1.position = new Vector2D(Esiade.WIDTH,Esiade.HEIGHT);
		I2.position = new Vector2D(Esiade.WIDTH,Esiade.HEIGHT);
 	}
 
 	/**
 	 * One point crossover. The two individuals will be modified to two new individuals, in a 1-point crossover fashion. 
 	 * @param I1 The first individual
 	 * @param I2 The second individual
 	 */
 	private static void OnePointCrossover(Individual I1, Individual I2){
 
 		int randomPoint = Random.nextInt(WIDTH*HEIGHT);
		for(int y = 0;y < HEIGHT; y++)
			for (int x = 0; x < WIDTH; x++)
 				if (randomPoint>0){
 					SwitchVectors(I1, I2, x, y);
 					randomPoint--;
 				} else {	
 					break;
 				}
 	}
 	
 	/**
 	 * One point crossover. The two individuals will be modified to two new individuals, in a 2-point crossover fashion. 
 	 * @param I1 The first individual
 	 */
 	private static void TwoPointCrossover(Individual I1, Individual I2){
 		int randomPoint1 = Random.nextInt(WIDTH*HEIGHT);
 		int randomPoint2 = Random.nextInt(WIDTH*HEIGHT-randomPoint1);
 		
		for(int y = randomPoint1 / WIDTH;y < HEIGHT; y++)
			for (int x = randomPoint1 % WIDTH; x < WIDTH; x++)
 				if (randomPoint2 >= 0){
 					SwitchVectors(I1, I2, x, y);
 					randomPoint2--;
 				} else {	
 					break;
 				}
 	}
 	
 	/**
 	 * One point crossover. The two individuals will be modified to two new individuals, in a uniform crossover fashion. 
 	 * @param I1 The first individual
 	 * @param I2 The second individual
 	 */
 	private static void UniformCrossover(Individual I1, Individual I2){
		for(int y = 0;y<HEIGHT;y++)
			for(int x=0;x<WIDTH;x++)
				if (Random.nextDouble() > 0.5)
 					SwitchVectors(I1, I2, x, y);
 	}
 	
 	
 	
 	/**
 	 * This function will change a vector to a random new vector, with the probability set as mutation rate, see mRate.
 	 * @param I The individual to mutate.
 	 */
 	private static void Mutation(Individual I){
 		for (int y = 0;y < HEIGHT;y++)
 			for (int x=0;x< WIDTH;x++)
 				if (Random.nextDouble() < mRate)
 					I.genome[x][y] = new Vector2D();
 	}
 }
