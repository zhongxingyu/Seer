 package ecprac.tbr440;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import ecprac.torcs.client.Controller.Stage;
 import ecprac.torcs.genome.Utilities;
 import ecprac.torcs.race.Race;
 import ecprac.torcs.race.RaceResults;
 import ecprac.torcs.race.Race.Termination;
 import ecprac.torcs.race.Race.Track;
 import ecprac.torcs.client.SensorModel;
 
 public class EA {
 
     final static int POPULATION_SIZE = 10,
                      EVALUATIONS = 100,
                      DAMAGE = 10000;
                      
     final static double //ALPHA = 0.45, //must be between 0 and 1.
     P_WEIGHTS = 0.5, //must be between 0 and 1.
     		S = 1.5;
     
     Random r;
     Genome[] population,
     		 children,
     		 populationAndChildren;
     int evals;
     double globalFitness,
      	   alpha;
     double[] parentSelectionChance = new double[POPULATION_SIZE];
     FitnessComparator c;
     
    
     EA() {
     	r = new Random();
     	population = new Genome[POPULATION_SIZE];
     	children = new Genome[POPULATION_SIZE];
     	populationAndChildren = new Genome[POPULATION_SIZE*2];
     	evals = 0;
     	globalFitness = 0.0;
     	c = new FitnessComparator();
     }
     
     public void run() {
     	initialize();    	
     	evaluateAll(population);
     	evals += POPULATION_SIZE;
     	Arrays.sort(population, c); //genomes sorted from worst to best fitness
     	System.out.println("Initialization:"); //debug
     	debugPrintGenome(population); //debug
 
     	try {
     		Utilities.saveGenome(population[POPULATION_SIZE-1], "bestInitialization.genome");
     		while (evals < EVALUATIONS) { // the evolutionary loop
     		   for(int i = 0; i < POPULATION_SIZE; i++){
                    int parent_i = parentSelection();
 				   Utilities.saveGenome(population[parent_i], "child.genome");
                    children[i] = mutate((Genome) Utilities.loadGenome("child.genome"), P_WEIGHTS); //debug: mutate all
 				   children[i].nn.setPossibleParent(population[parent_i].nn);
                    //System.out.println("child mutated"); //debug
                } 
     			evaluateAll(children);
     			evals += POPULATION_SIZE;
     			Arrays.sort(children, c);
     			System.out.println("Children:"); //debug
     			debugPrintGenome(children); //debug
     			survivorSelection();
     			System.out.println("New population:"); //debug
     			debugPrintGenome(population); //debug
 
     			if ((double)evals/EVALUATIONS == 0.05) Utilities.saveGenome(population[POPULATION_SIZE-1], "best5%.genome");
     			else if ((double)evals/EVALUATIONS == 0.10) Utilities.saveGenome(population[POPULATION_SIZE-1], "best10%.genome");
     			else if ((double)evals/EVALUATIONS == 0.25) Utilities.saveGenome(population[POPULATION_SIZE-1], "best25%.genome");
     			else if ((double)evals/EVALUATIONS == 0.50) Utilities.saveGenome(population[POPULATION_SIZE-1], "best50%.genome");
     		}
     		Utilities.saveGenome(population[POPULATION_SIZE-1], "best.genome");
     	}catch (Exception e) {
     		e.printStackTrace();
     	}
     }
         
     private void debugPrintGenome(Genome[] genome) {
     	System.out.println("-------------Genomes------------------");
     	for (int i = 0; i < POPULATION_SIZE; i++){
     		System.out.println("Fitness is: " + genome[i].fitness);
     		System.out.println("damage is: " + genome[i].damage);
     		System.out.println("Percentage is: " + (double)evals/EVALUATIONS);
     		System.out.println("Weights: ");
     		//genome[i].nn.debugPrintWeights();
     		System.out.println("");
     	}
     	System.out.println("-------------/Genomes-----------------");
     }
 
     private void initialize() {
     	for (int i = 0; i < POPULATION_SIZE; i++) {
     		Genome genome = new Genome();    		
     		population[i] = genome;
     	}
     }
 
 	 private int parentSelection() {
 	    	int parentSelected = 0;
 	    	double sumParentSelectionChance = 0.0;
 	    	for (int i = 0; i < POPULATION_SIZE; i++) {
 	    		//Ranking selection (Page 61 in the book)
 	    		parentSelectionChance[i] = ((2-S)/POPULATION_SIZE)+((2*i*(S-1))/(POPULATION_SIZE*(POPULATION_SIZE-1)));
 	    		//Roulette wheel based on the probability of each rank position to be selected
 	    		sumParentSelectionChance += parentSelectionChance[i];
 	    		double rand = r.nextDouble()*sumParentSelectionChance;
 	    		if(parentSelectionChance[i] <= rand){
 	        		parentSelected = i;
 	        	}
 	    	}
 	    	return parentSelected;
 	    }
 
     /**
      * pWeights: propability a weight gets mutated must be between 0 and 1
      */
     private Genome mutate(Genome genome, double pWeights) {
     	Genome result = genome;
     	result.nn.mutate(pWeights, 0.1, 0.1, 0.5, 0.2, 0.5);
 
     	return result;
     }
 
     //new population is  the ten most fit individuals from children and population
     private void survivorSelection() {
     	for(int i = 0; i < POPULATION_SIZE; i++){
     		populationAndChildren[i] = children[i];
     	}
     	for(int i = POPULATION_SIZE; i < POPULATION_SIZE*2; i++){
     		populationAndChildren[i] = population[i-POPULATION_SIZE];
     	}
     	Arrays.sort(populationAndChildren, c);
 		
 		/*
         species = selectSpeciesfrompopulation;
 
 
         for (every species) {
             survivor_species = [];
             for (individual) {
                 if(individual  in species) {
                     add individual to survivor_species;
                 }
             }
             select best 2 from survivor_species
             survivor_species
         }
         */
 		
     	for(int i = POPULATION_SIZE; i < POPULATION_SIZE*2; i++){
     		population[i-POPULATION_SIZE] = populationAndChildren[i];
     	}
     }
 
     public void evaluateAll(Genome[] population) {
     	Race race = new Race();
 
     	// One of the two ovals
     	//race.setTrack( Track.fromIndex( (evals / 2) % 2 ));
     	race.setTrack(Track.alpine);
     	race.setStage(Stage.RACE);
     	race.setTermination(Termination.LAPS, 1);
 
     	// Add driver 1
     	Driver driver1 = new Driver();
     	driver1.init();
     	driver1.loadGenome(population[0]);
     	race.addCompetitor(driver1);
 
     	// Add driver 2
     	Driver driver2 = new Driver();
     	driver2.init();
     	driver2.loadGenome(population[1]);
     	race.addCompetitor(driver2);
 
 		// Add driver 3
 		Driver driver3 = new Driver();
 		driver3.init();
 		driver3.loadGenome(population[2]);
 		race.addCompetitor(driver3);
 		
 		// Add driver 4
 		Driver driver4 = new Driver();
 		driver4.init();
 		driver4.loadGenome(population[3]);
 		race.addCompetitor(driver4);
 		
 		// Add driver 5
 		Driver driver5 = new Driver();
 		driver5.init();
 		driver5.loadGenome(population[4]);
 		race.addCompetitor(driver5);
 		
 		// Add driver 6
 		Driver driver6 = new Driver();
 		driver6.init();
 		driver6.loadGenome(population[5]);
 		race.addCompetitor(driver6);
 		
 		// Add driver 7
 		Driver driver7 = new Driver();
 		driver7.init();
 		driver7.loadGenome(population[6]);
 		race.addCompetitor(driver7);
 		
 		// Add driver 8
 		Driver driver8 = new Driver();
 		driver8.init();
 		driver8.loadGenome(population[7]);
 		race.addCompetitor(driver8);
 		
 		// Add driver 9
 		Driver driver9 = new Driver();
 		driver9.init();
 		driver9.loadGenome(population[8]);
 		race.addCompetitor(driver9);
 		
 		// Add driver 10
 		Driver driver10 = new Driver();
 		driver10.init();
 		driver10.loadGenome(population[9]);
 		race.addCompetitor(driver10);
 		
 		// Run in Text Mode
 		System.out.println("start evaluation"); //debug
 		RaceResults results = race.run();
 
 		// Fitness = BestLap, or distance in case driver did not do at least one lap
 		if( Double.isInfinite(results.get(driver1).bestLapTime)) population[0].fitness = results.get(driver1).distance*(1+population[0].damage/DAMAGE);
 		else population[0].fitness = -1 * results.get(driver1).bestLapTime*(1+population[0].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver2).bestLapTime)) population[1].fitness = results.get(driver2).distance*(1+population[1].damage/DAMAGE);
 		else population[1].fitness = -1 * results.get(driver2).bestLapTime*(1+population[1].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver3).bestLapTime)) population[2].fitness = results.get(driver3).distance*(1+population[2].damage/DAMAGE);
 		else population[2].fitness = -1 * results.get(driver3).bestLapTime*(1+population[2].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver4).bestLapTime)) population[3].fitness = results.get(driver4).distance*(1+population[3].damage/DAMAGE);
 		else population[3].fitness = -1 * results.get(driver4).bestLapTime*(1+population[3].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver5).bestLapTime)) population[4].fitness = results.get(driver5).distance*(1+population[4].damage/DAMAGE);
 		else population[4].fitness = -1 * results.get(driver5).bestLapTime*(1+population[4].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver6).bestLapTime)) population[5].fitness = results.get(driver6).distance*(1+population[5].damage/DAMAGE);
 		else population[5].fitness = -1 * results.get(driver6).bestLapTime*(1+population[5].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver7).bestLapTime)) population[6].fitness = results.get(driver7).distance*(1+population[6].damage/DAMAGE);
 		else population[6].fitness = -1 * results.get(driver7).bestLapTime*(1+population[6].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver8).bestLapTime)) population[7].fitness = results.get(driver8).distance*(1+population[7].damage/DAMAGE);
 		else population[7].fitness = -1 * results.get(driver8).bestLapTime*(1+population[7].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver9).bestLapTime)) population[8].fitness = results.get(driver9).distance*(1+population[8].damage/DAMAGE);
 		else population[8].fitness = -1 * results.get(driver9).bestLapTime*(1+population[8].damage/DAMAGE);
 		if( Double.isInfinite(results.get(driver10).bestLapTime)) population[9].fitness = results.get(driver10).distance*(1+population[9].damage/DAMAGE);
 		else population[9].fitness = -1 * results.get(driver10).bestLapTime*(1+population[9].damage/DAMAGE);
 		
 		
 		/*
 		if( Double.isInfinite(results.get(driver1).bestLapTime) && Double.isInfinite(results.get(driver2).bestLapTime) &&
 			Double.isInfinite(results.get(driver3).bestLapTime) && Double.isInfinite(results.get(driver4).bestLapTime) &&
 			Double.isInfinite(results.get(driver5).bestLapTime) && Double.isInfinite(results.get(driver6).bestLapTime) &&
 			Double.isInfinite(results.get(driver7).bestLapTime) && Double.isInfinite(results.get(driver8).bestLapTime) &&
 			Double.isInfinite(results.get(driver9).bestLapTime) && Double.isInfinite(results.get(driver10).bestLapTime)){
 			population[0].fitness = results.get(driver1).distance;
 			population[1].fitness = results.get(driver2).distance;
 			population[2].fitness = results.get(driver3).distance;
 			population[3].fitness = results.get(driver4).distance;
 			population[4].fitness = results.get(driver5).distance;
 			population[5].fitness = results.get(driver6).distance;
 			population[6].fitness = results.get(driver7).distance;
 			population[7].fitness = results.get(driver8).distance;
 			population[8].fitness = results.get(driver9).distance;
 			population[9].fitness = results.get(driver10).distance;
 		} else {
 			population[0].fitness = -1 * results.get(driver1).bestLapTime;
 			population[1].fitness = -1 * results.get(driver2).bestLapTime;
 			population[2].fitness = -1 * results.get(driver3).bestLapTime;
 			population[3].fitness = -1 * results.get(driver4).bestLapTime;
 			population[4].fitness = -1 * results.get(driver5).bestLapTime;
 			population[5].fitness = -1 * results.get(driver6).bestLapTime;
 			population[6].fitness = -1 * results.get(driver7).bestLapTime;
 			population[7].fitness = -1 * results.get(driver8).bestLapTime;
 			population[8].fitness = -1 * results.get(driver9).bestLapTime;
 			population[9].fitness = -1 * results.get(driver10).bestLapTime;
 		}*/
 
     }
 
     public void show(){
     	try {
 
     		Race race = new Race();
     		race.setTrack(Track.michigan);
     		race.setStage(Stage.QUALIFYING);
     		race.setTermination(Termination.LAPS, 3);
 
     		Driver driver = new ecprac.tbr440.Driver();
     		driver.init();
     		Genome g = (Genome) Utilities.loadGenome("best.genome");
     		g.nn.debugPrintWeights();
     		driver.loadGenome(g);
     		race.addCompetitor(driver);
     		
         	// Add driver 2
     		/*
         	Driver driver2 = new ecprac.tbr440.Driver();
         	driver2.init();
         	driver2.loadGenome(Utilities.loadGenome("best50%.genome"));
         	race.addCompetitor(driver2);
 			*/
     		RaceResults results = race.runWithGUI();
     		if( Double.isInfinite(results.get(driver).bestLapTime)) System.out.println("distance is: " + results.get(driver).distance);    		
     		else System.out.println("time is: " + results.get(driver).bestLapTime);
     		
 
     	} catch (IOException e) {
     		e.printStackTrace();
     	} catch (ClassNotFoundException e) {
     		e.printStackTrace();
     	}
     }
 	
 	public void test() {
		try {
			Genome g = (Genome) Utilities.loadGenome("best.genome");
			Genome g2 = (Genome) Utilities.loadGenome("best50%.genome");
			g.nn.similarity(g2.nn);
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (ClassNotFoundException e) {
    		e.printStackTrace();
    	}
     }
 
     public static void main( String[] args ) {
 
     	/*
     	 *
     	 * Start without arguments to run the EA, start with -show to show the best found
     	 *
     	 */
 
     	/*if(args.length > 0 && args[0].equals("-show")){
     		new EA().show();
     	} else {
     		new EA().run();
     	}*/
 		
 		//qualifying race with best genome
     	//new EA().show();
 		//train population:
     	new EA().run(); 
     }
 }
