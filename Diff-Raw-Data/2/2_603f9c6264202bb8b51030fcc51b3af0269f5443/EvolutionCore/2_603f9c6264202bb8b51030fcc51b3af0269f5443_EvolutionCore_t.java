 package net.esiade.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import java_cup.internal_error;
 
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 
 import net.esiade.client.sprite.Individual;
 
 /**
  * @author Jonathan
  *
  */
 public class EvolutionCore {
 	public static int WIDTH, HEIGHT, elitism, numImmigrants;
 	private static double mRate, cRate, chance;
 	private static CType type;
 	private static IType iType;
 	private static ArrayList<Individual> historicElites = new ArrayList<Individual>(0);
 	
 	/**
 	 * @param width The width of the map matrix
 	 * @param height The height of the map matrix
 	 * @param mRate The mutation rate
 	 * @param cRate The crossover rate
 	 */
 	public EvolutionCore(int width, int height, double mRate, double cRate, CType type,
 						int elitism, double chance, int numImmigrants, IType iType){
 		EvolutionCore.WIDTH = width;
 		EvolutionCore.HEIGHT = height;
 		EvolutionCore.mRate = mRate;
 		EvolutionCore.cRate = cRate;
 		EvolutionCore.type = type;
 		EvolutionCore.iType = iType;
 		EvolutionCore.elitism = elitism;
 		EvolutionCore.chance = chance;
 		EvolutionCore.numImmigrants = numImmigrants;
 	}
 
 	public static enum CType {
 		ONEPOINT, TWOPOINT, UNIFORM
 	}	
 	
 	public static enum IType {
 		NONE, ELITE, RANDOM
 	}	
 	/**
 	 * @return This function returns a genome with all vectors set at random.
 	 */
 	public static Vector2D[][] getRandomGenome(double k) {
 		Vector2D[][] genome = new Vector2D[WIDTH][HEIGHT];
 		for (int x = 0;x < WIDTH;x++)
 			for (int y = 0; y < HEIGHT;y++)
 				genome[x][y] = new Vector2D(k);
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
 	 * @param i1 The first individual
 	 * @param i2 The second individual
 	 */
 	public static void Crossover(Individual i1, Individual i2) {
 		if (Random.nextDouble() < cRate && 
 				i1.getReproductionLimit() <= i1.getFood() && 
 				i2.getReproductionLimit() <= i2.getFood()) {
 			if (type == CType.ONEPOINT)
 				OnePointCrossover(i1, i2);
 			else if (type == CType.TWOPOINT)
 				TwoPointCrossover(i1, i2);
 			else if (type == CType.UNIFORM)
 				UniformCrossover(i1, i2);
 	
 			Mutation(i1);
 			Mutation(i2);
 //			i1.position = new Vector2D(Esiade.WIDTH-i1.getWidth(),Esiade.HEIGHT-i1.getHeight());
 //			i2.position = new Vector2D(Esiade.WIDTH-i2.getWidth(),Esiade.HEIGHT-i2.getHeight());
 			i1.resetFood();
 			i2.resetFood();
 			i1.increaseGen();
 			i2.increaseGen();
 		}
 	}
 	
 	public static ArrayList<Individual> EpochReproduction(ArrayList<Individual> individuals) {
 		ArrayList<Individual> elites = new ArrayList<Individual>(0);
 		Collections.sort(individuals);
 		individuals.get(0).resetFood();
 		historicElites.add((individuals.get(0).clone()));
 		for(int x = 0; x<elitism; x++) 
 			elites.add(individuals.get(x).clone());
 			
 		for(Individual i : individuals) {
 			i.resetFood();
 			for(Individual j : individuals)
 				if((!i.equals(j) && Random.nextDouble() < chance) || 
 						individuals.get(individuals.size()-1).equals(j)) {
 					Crossover(i, j);
 					i.position = new Vector2D(Esiade.WIDTH, Esiade.HEIGHT);
 					j.position = new Vector2D(Esiade.WIDTH, Esiade.HEIGHT);
 					break;
 				}
 		}
 		for (int x = 1;x <= numImmigrants;x++){
 			if (iType == IType.RANDOM)
 				individuals.get(individuals.size()-elites.size()-x).genome = getRandomGenome(20);
 			else if (iType == IType.ELITE)
 				individuals.set(individuals.size()-elites.size()-x, getRandomElite());	
 		}
 		
 		int x = 1;
 		for (Individual e : elites) {
 			e.resetFood();
 			individuals.set(individuals.size()-x, e);
 			x++;
 		}
 			
 		return individuals;
 	}
 	
 	public static Individual getRandomElite(){
 		int number = Random.nextInt(historicElites.size());
		return historicElites.get(number).clone();
 	}
 	
 	public static Individual SelfReproduction(Individual i) {
 		Individual i2 = i.clone();
 		i2.increaseGen();
 		//i2.position = new Vector2D(WIDTH, HEIGHT);
 		i.resetFood();
 		i2.resetFood();
 		i2.position.add(new Vector2D(10));
 		Mutation(i2);
 		return i2;
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
 	private static void Mutation(Individual i){
 		for (int y = 0;y < HEIGHT;y++)
 			for (int x=0;x< WIDTH;x++)
 				if (Random.nextDouble() < mRate)
 					i.genome[x][y] = new Vector2D(i.getJumpLength());
 	}
 }
