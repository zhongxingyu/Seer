 package qos;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class GeneticAlgorithm extends Algorithm {
 	
 	private int populationSize;
 	private int terminationCriterion;
 	private int terminationCounter;
 	private int workPercentage = 0;
 	
 	private long runtime = 0;
 
 	private double elitismRate;
 	private double crossoverRate;
 	private double mutationRate;
 	private double minEquality;
 
 	private String selectionMethod;
 	private String crossoverMethod;
 	private String terminationMethod;
 	
 	private List<Integer> numberOfDifferentSolutions;
 	private List<Double> maxFitnessPerPopulation;
 	private List<Double> averageFitnessPerPopulation;
 	private List<ServiceClass> serviceClassesList;
 	private List<AlgorithmSolutionTier> algorithmSolutionTiers = 
 		new LinkedList<AlgorithmSolutionTier>();
 	
 	private Map<String, Constraint> constraintsMap;
 	
 	public GeneticAlgorithm(List<ServiceClass> serviceClassesList, 
 			Map<String, Constraint> constraintsMap, int populationSize, 
 			int terminationCriterion, String selectionMethod, 
 			int elitismRate, String crossoverMethod,
 			int crossoverRate, int mutationRate,
 			String terminationMethod, int minEquality) {
 		this.serviceClassesList = serviceClassesList;
 		this.constraintsMap = constraintsMap;
 		this.populationSize = populationSize;
 		this.terminationCriterion = terminationCriterion;
 		this.selectionMethod = selectionMethod;
 		this.elitismRate = elitismRate / 100.0;
 		this.crossoverMethod = crossoverMethod;
 		this.crossoverRate = crossoverRate / 100.0;
 		this.mutationRate = mutationRate / 1000.0;
 		this.terminationMethod = terminationMethod;
 		this.minEquality = minEquality / 100.0;
 	}
 
 	@Override
 	public void start() {
 		workPercentage = 0;
 		
 		terminationCounter = terminationCriterion;
 		
 		numberOfDifferentSolutions = new LinkedList<Integer>();
 		maxFitnessPerPopulation = new LinkedList<Double>();
 		averageFitnessPerPopulation = new LinkedList<Double>();
 		
 		runtime = System.nanoTime();
 		
 		List<Composition> population = generateInitialPopulation();
 		setVisualizationValues(population);
 		
 		int numberOfElites = (int) Math.round(
 				populationSize * elitismRate);
 		
 		while (terminationCounter > 0) {
 			// Temporarily save the elite compositions.
 			List<Composition> elites = doSelectionElitismBased(
 					population, numberOfElites);
 		
 			// SELECTION
 			List<Composition> matingPool;
 			// Roulette Wheel
 			if (selectionMethod.contains("Roulette Wheel")) {
 				matingPool = doSelectionRouletteWheel(population);
 			}
 			
 			// Linear Ranking
 			else if (selectionMethod.contains("Linear Ranking")) {
 				matingPool = doSelectionLinearRanking(population);
 			}
 			
 			// Binary Tournament
 			else {
 				matingPool = doSelectionBinaryTournament(population);
 			}
 
 			// CROSSOVER
 			// One-Point Crossover
 			if (crossoverMethod.contains("One-Point")) {
 				matingPool = doCrossoverOnePoint(matingPool, crossoverRate);
 			}
 			else if (crossoverMethod.contains("Two-Point")) {
 				matingPool = doCrossoverTwoPoint(matingPool, crossoverRate);
 			}
 			else {
 				matingPool = doCrossoverUniform(matingPool, crossoverRate);
 			}
 			
 
 			// MUTATION
 			doMutation(matingPool, mutationRate);
 			
 			// Update the fitness values.
			for (Composition composition : population) {
 				composition.computeFitness(constraintsMap);
 			}
 			
 			// Replace the worst compositions with the elites.
 			matingPool = doElitePreservation(matingPool, elites);
 			
 			boolean hasPopulationChanged = true;
 			if (terminationMethod.contains("Consecutive Equal Generations")) {
 				hasPopulationChanged = 
 						hasPopulationChanged(population, matingPool);
 			}
 			
 			// Update the population.
 			population.clear();
 			population.addAll(matingPool);
 			
 			// Save the values needed for visualization.
 			setVisualizationValues(population);
 			
 			// TERMINATION CRITERION
 			// Number of Iterations
 			if (terminationMethod.contains("Iteration")) {
 				terminationCounter--;
 				workPercentage = (int) ((1.0 - 1.0 * terminationCounter / 
 						terminationCriterion) * 100.0);
 			}
 
 			// Consecutive Equal Generations
 			else if (terminationMethod.contains(
 					"Consecutive Equal Generations")) {
 				if (hasPopulationChanged) {
 					terminationCounter = terminationCriterion;
 				}
 				else {
 					terminationCounter--;
 				}
 				workPercentage = Math.max(
 						(int) (((double) terminationCriterion - 
 								(double) terminationCounter) / 
 								(double) terminationCriterion * 100.0), 
 								workPercentage);
 			}
 			
 			// Fitness Value Convergence
 			else {
 				// "Unnecessary cast" is not unnecessary at all!
 				if ((double) maxFitnessPerPopulation.get(
 						maxFitnessPerPopulation.size() - 1) == (double)
 						maxFitnessPerPopulation.get(
 								maxFitnessPerPopulation.size() - 2)) {
 					terminationCounter--;
 				}
 				else {
 					terminationCounter = terminationCriterion;
 				}
 				workPercentage = Math.max(
 						(int) (((double) terminationCriterion - 
 								(double) terminationCounter) / 
 								(double) terminationCriterion * 100.0), 
 								workPercentage);
 			}
 		}
 		
 		// Sort the population according to the fitness of the 
 		// compositions. Thus, the first elements are the elite 
 		// elements.
 		Collections.sort(population, new FitnessComparator());
 		
 		// Show the best solution in the result table.
 		if (population.get(0).isWithinConstraints(constraintsMap)) {
 			List<Composition> optimalComposition = 
 					new LinkedList<Composition>();
 			optimalComposition.add(population.get(0));
 			algorithmSolutionTiers.add(
 					new AlgorithmSolutionTier(optimalComposition, 1));
 		}
 		
 		runtime = System.nanoTime() - runtime;		
 	}
 	
 	private List<Composition> generateInitialPopulation() {
 		// Randomly select the requested number of compositions.
 		List<Composition> population = new LinkedList<Composition>();
 		// Loop to construct the requested number of compositions.
 		for (int i = 0; i < populationSize; i++) {
 			Composition composition = new Composition();
 			// Loop to randomly select a service candidate for each 
 			// service class.
 			for (int j = 0; j < serviceClassesList.size(); j++) {
 				// Generate a random number between 0 and the number 
 				// of service candidates in this service class.
 				int random = (int) (Math.random() * serviceClassesList.get(
 						j).getServiceCandidateList().size());
 				// Select the corresponding service candidate 
 				// and add it to the new composition.
 				// QoS values are aggregated automatically.
 				ServiceCandidate serviceCandidate = serviceClassesList.get(
 						j).getServiceCandidateList().get((random));
 				composition.addServiceCandidate(serviceCandidate);
 			}
 			// Check if composition has already been created.
 			if (population.contains(composition)) {
 				i--;
 			}
 			else {
 				// Compute the composition's fitness value 
 				// before adding it to the population.
 				composition.computeFitness(constraintsMap);
 				population.add(composition);
 			}
 		}
 		return population;
 	}
 	
 	
 	
 	/*	+-----------------------------------------------------------+
 	 * 	| +-------------------------------------------------------+ |
 	 * 	| |														  | |
 	 * 	| |			      ELITE PRESERVATION METHODS			  | |
 	 * 	| |														  | |
 	 * 	| +-------------------------------------------------------+ |
 	 * 	+-----------------------------------------------------------+
 	 */
 	
 	private List<Composition> doElitePreservation(
 			List<Composition> matingPool, List<Composition> elites) {
 		Collections.sort(matingPool, new FitnessComparator());
 		// Remove the worst compositions by using the other part 
 		// of the population.
 		matingPool = matingPool.subList(0, matingPool.size() - elites.size());
 		// Add the elite compositions to the beginning of the list. 
 		// Note that they are not necessarily the elite compositions 
 		// in the new population. 
 		// So they might also be added at the end.
 		matingPool.addAll(0, elites);
 		
 		return matingPool;
 	}
 	
 	private List<Composition> doSelectionElitismBased(
 			List<Composition> population, int numberOfElites) {
 		List<Composition> elites = new LinkedList<Composition>();
 		// Sort the population according to the fitness of the 
 		// compositions. Thus, the first elements are the elite 
 		// elements.
 		Collections.sort(population, new FitnessComparator());
 		for (int i = 0; i < numberOfElites; i++) {
 			elites.add(population.get(i));
 		}
 		return elites;
 	}
 	
 	
 	
 	/*	+-----------------------------------------------------------+
 	 * 	| +-------------------------------------------------------+ |
 	 * 	| |														  | |
 	 * 	| |				     SELECTION METHODS					  | |
 	 * 	| |														  | |
 	 * 	| +-------------------------------------------------------+ |
 	 * 	+-----------------------------------------------------------+
 	 */
 	
 	private List<Composition> doSelectionRouletteWheel(
 			List<Composition> populationOld) {
 		double[] fitnessAreas = new double[populationOld.size()];
 		List<Composition> matingPool = new LinkedList<Composition>();
 		// Compute the cumulated fitness areas of every composition 
 		// of the population.
 		for (int i = 0; i < populationOld.size(); i++) {
 			fitnessAreas[i] = populationOld.get(i).getFitness();
 			if (i != 0) {
 				fitnessAreas[i] += fitnessAreas[i - 1];
 			}
 		}
 		// Save the fitnessAreaSum.
 		double fitnessAreaSum = fitnessAreas[populationOld.size() - 1];
 		// Randomly select the compositions of the new population 
 		// with respect to their fitness values.
 		for (int i = 0; i < populationOld.size(); i++) {
 			double random = Math.random() * fitnessAreaSum;
 			for (int j = 0; j < populationOld.size(); j++) {
 				if (random < fitnessAreas[j]) {
 					matingPool.add(populationOld.get(j));
 					break;
 				}
 			}
 		}
 		return matingPool;
 	}
 
 	private List<Composition> doSelectionLinearRanking(
 			List<Composition> populationOld) {
 		double[] fitnessRankAreas = new double[populationOld.size()];
 		// sp is short for Selection Pressure; it modifies the size
 		// of each rank area and can be chosen between 1.1 and 2.0.
 		// The expected sampling rate of the best individual is sp,
 		// the expected sampling rate of the worst individual is 2-sp
 		// and the selective pressure of all other population members
 		// can be interpreted by linear interpolation of the
 		// selective pressure according to rank.
 		double sp = 2.0;
 		Collections.sort(populationOld, new FitnessComparator());
 		// Compute the accumulated fitness rank areas  
 		// of every composition of the population.
 		for (int i = 0; i < populationOld.size(); i++) {
 			// rank(pos) = 2 - sp + (2 * (sp - 1) * (pos - 1) / (n - 1))
 			fitnessRankAreas[i] = 2.0 - sp + 
 					(2.0 * (sp - 1.0) * (i / (populationOld.size() - 1.0)));
 			if (i != 0) {
 				fitnessRankAreas[i] += fitnessRankAreas[i - 1];
 			}
 		}
 		// Save the fitnessRankAreaSum.
 		double fitnessRankAreaSum = fitnessRankAreas[populationOld.size() - 1];
 		List<Composition> matingPool = new LinkedList<Composition>();
 		// Randomly select the compositions of the new population 
 		// with respect to their ranks (like Roulette Wheel).
 		for (int i = 0; i < populationOld.size(); i++) {
 			double random = Math.random() * fitnessRankAreaSum;
 			for (int j = 0; j < populationOld.size(); j++) {
 				if (random < fitnessRankAreas[j]) {
 					matingPool.add(populationOld.get(j));
 					break;
 				}
 			}
 		}
 		return matingPool;
 	}
 
 	private List<Composition> doSelectionBinaryTournament(
 			List<Composition> populationOld) {
 		List<Composition> matingPool = 
 			new LinkedList<Composition>(populationOld);
 		// Permute the indices of the population.
 		int[] permutationIndices = permuteIndices(populationOld.size());
 		// Pairwise comparison between two compositions. 
 		// The opponents are determined by the permutation 
 		// created above.
 		for (int i = 0; i < populationOld.size(); i++) {
 			if (populationOld.get(i).getFitness() < 
 					populationOld.get(permutationIndices[i]).getFitness()) {
 				matingPool.set(i, populationOld.get(permutationIndices[i]));
 			}
 		}
 		return matingPool;
 	}
 	
 	
 	
 	/*	+-----------------------------------------------------------+
 	 * 	| +-------------------------------------------------------+ |
 	 * 	| |														  | |
 	 * 	| |				       MUTATION METHOD					  | |
 	 * 	| |														  | |
 	 * 	| +-------------------------------------------------------+ |
 	 * 	+-----------------------------------------------------------+
 	 */
 	
 	private void doMutation(List<Composition> population, 
 			double mutationRate) {
 		for (int i = 0; i < population.size(); i++) {
 			List<ServiceCandidate> serviceCandidates = 
 					new LinkedList<ServiceCandidate>(
 							population.get(i).getServiceCandidatesList());
 			Collections.copy(serviceCandidates, 
 					population.get(i).getServiceCandidatesList());
 			boolean mutate = false;
 			
 			for (int j = 0; j < serviceCandidates.size(); j++) {
 				if (Math.random() < mutationRate) {
 					mutate = true;
 					// Get the service candidates from the service 
 					// class that has to be mutated.
 					List<ServiceCandidate> newServiceCandidates = 
 							serviceClassesList.get(j).
 									getServiceCandidateList();
 					serviceCandidates.set(j, newServiceCandidates.get((int) 
 							(Math.random() * newServiceCandidates.size())));
 				}
 			}
 			if (mutate) {
 				Composition composition = new Composition();
 				composition.setServiceCandidateList(serviceCandidates);
 				
 				population.set(i, composition);
 			}
 		}
 	}
 	
 
 	
 	/*	+-----------------------------------------------------------+
 	 * 	| +-------------------------------------------------------+ |
 	 * 	| |														  | |
 	 * 	| |				     CROSSOVER METHODS					  | |
 	 * 	| |														  | |
 	 * 	| +-------------------------------------------------------+ |
 	 * 	+-----------------------------------------------------------+
 	 */
 	
 	private List<Composition> doCrossoverOnePoint(
 			List<Composition> matingPool, double crossoverRate) {
 		List<Composition> populationNew = new LinkedList<Composition>();
 		// If there is only one composition left in the mating pool, 
 		// simply add it to the new population.
 		while (matingPool.size() > 0) {
 			if (matingPool.size() == 1) {
 				populationNew.add(matingPool.get(0));
 				break;
 			}
 			// Pick the first composition for crossover.
 			Composition compositionA = matingPool.remove(0);
 			// Randomly select the second composition for crossover.
 			Composition compositionB = matingPool.remove(
 					(int) (Math.random() * matingPool.size()));
 
 			if (Math.random() < crossoverRate) {
 				// Randomly select the crossover point. 0 is excluded 
 				// from the different possibilities because the 
 				// resulting composition would be exactly the same as 
 				// the first input composition. The last crossover
 				// point that is possible is included, however,
 				// because then, at least the last service candidate 
 				// is changed. This is because of the definition of
 				// List.subList().
 				int crossoverPoint = (int) (Math.random() * 
 						(serviceClassesList.size() - 1) + 1);
 
 				// Do the crossover.
 				Composition compositionC = new Composition();
 				for (ServiceCandidate serviceCandidate : compositionA.
 						getServiceCandidatesList().subList(0, crossoverPoint)) {
 					compositionC.addServiceCandidate(serviceCandidate);
 				}
 				for (ServiceCandidate serviceCandidate : compositionB.
 						getServiceCandidatesList().subList(
 								crossoverPoint, serviceClassesList.size())) {
 					compositionC.addServiceCandidate(serviceCandidate);
 				}
 
 				Composition compositionD = new Composition();
 				for (ServiceCandidate serviceCandidate : compositionB.
 						getServiceCandidatesList().subList(0, crossoverPoint)) {
 					compositionD.addServiceCandidate(serviceCandidate);
 				}
 				for (ServiceCandidate serviceCandidate : compositionA.
 						getServiceCandidatesList().subList(
 								crossoverPoint, serviceClassesList.size())) {
 					compositionD.addServiceCandidate(serviceCandidate);
 				}
 				
 				populationNew.add(compositionC);
 				populationNew.add(compositionD);
 			}
 			else {
 				populationNew.add(compositionA);
 				populationNew.add(compositionB);
 			}
 		}
 		return populationNew;
 	}
 	
 	private List<Composition> doCrossoverTwoPoint(
 			List<Composition> matingPool, double crossoverRate) {
 		List<Composition> populationNew = new LinkedList<Composition>();
 		// If there is only one composition left in the mating pool, 
 		// simply add it to the new population.
 		while (matingPool.size() > 0) {
 			if (matingPool.size() == 1) {
 				populationNew.add(matingPool.get(0));
 				break;
 			}
 			// Pick the first composition for crossover.
 			Composition compositionA = matingPool.remove(0);
 			// Randomly select the second composition for crossover.
 			Composition compositionB = matingPool.remove(
 					(int) (Math.random() * matingPool.size()));
 			if (Math.random() < crossoverRate) {
 				// Randomly select the crossover points.
 				int crossoverPoint1 = 
 						(int) ((Math.random() * 
 								(serviceClassesList.size() - 2)) + 1);
 				int crossoverPoint2 = (int) ((Math.random() * 
 						(serviceClassesList.size() - crossoverPoint1 - 1)) + 
 						(crossoverPoint1 + 1));				
 				// Do the crossover.
 				Composition compositionC = new Composition();
 				for (ServiceCandidate serviceCandidate : compositionA.
 						getServiceCandidatesList().subList(
 								0, crossoverPoint1)) {
 					compositionC.addServiceCandidate(serviceCandidate);
 				}
 				for (ServiceCandidate serviceCandidate : compositionB.
 						getServiceCandidatesList().subList(
 								crossoverPoint1, crossoverPoint2)) {
 					compositionC.addServiceCandidate(serviceCandidate);
 				}
 				for (ServiceCandidate serviceCandidate : compositionA.
 						getServiceCandidatesList().subList(crossoverPoint2, 
 								serviceClassesList.size())) {
 					compositionC.addServiceCandidate(serviceCandidate);
 				} 
 
 				Composition compositionD = new Composition();
 				for (ServiceCandidate serviceCandidate : compositionB.
 						getServiceCandidatesList().subList(
 								0, crossoverPoint1)) {
 					compositionD.addServiceCandidate(serviceCandidate);
 				}
 				for (ServiceCandidate serviceCandidate : compositionA.
 						getServiceCandidatesList().subList(
 								crossoverPoint1, crossoverPoint2)) {
 					compositionD.addServiceCandidate(serviceCandidate);
 				}
 				for (ServiceCandidate serviceCandidate : compositionB.
 						getServiceCandidatesList().subList(crossoverPoint2, 
 								serviceClassesList.size())) {
 					compositionD.addServiceCandidate(serviceCandidate);
 				} 
 
 				populationNew.add(compositionC);
 				populationNew.add(compositionD);
 			}
 			else {
 				populationNew.add(compositionA);
 				populationNew.add(compositionB);
 			}
 		}
 		return populationNew;
 	}
 
 	private List<Composition> doCrossoverUniform(
 			List<Composition> matingPool, double crossoverRate) {
 		List<Composition> populationNew = new LinkedList<Composition>();
 		// If there is only one composition left in the mating pool, 
 		// simply add it to the new population.
 		while (matingPool.size() > 0) {
 			if (matingPool.size() == 1) {
 				populationNew.add(matingPool.get(0));
 				break;
 			}
 			// Pick the first composition for crossover.
 			Composition compositionA = matingPool.remove(0);
 			// Randomly select the second composition for crossover.
 			Composition compositionB = matingPool.remove(
 					(int) (Math.random() * matingPool.size()));
 			if (Math.random() < crossoverRate) {
 				// Do the crossover.
 				Composition compositionC = new Composition();
 				Composition compositionD = new Composition();
 				for (int i = 0; i < 
 						compositionA.getServiceCandidatesList().size(); i++) {
 					if (Math.random() < 0.5) {
 						compositionC.addServiceCandidate(
 								compositionA.getServiceCandidatesList().
 								get(i));
 						compositionD.addServiceCandidate(
 								compositionB.getServiceCandidatesList().
 								get(i));
 					}
 					else {
 						compositionD.addServiceCandidate(
 								compositionB.getServiceCandidatesList().
 								get(i));
 						compositionC.addServiceCandidate(
 								compositionA.getServiceCandidatesList().
 								get(i));
 					}
 				}
 				
 				populationNew.add(compositionC);
 				populationNew.add(compositionD);
 			}
 			else {
 				populationNew.add(compositionA);
 				populationNew.add(compositionB);
 			}
 		}
 		return populationNew;
 	}
 	
 	
 	
 	/*	+-----------------------------------------------------------+
 	 * 	| +-------------------------------------------------------+ |
 	 * 	| |														  | |
 	 * 	| |				     	HELPER METHODS					  | |
 	 * 	| |														  | |
 	 * 	| +-------------------------------------------------------+ |
 	 * 	+-----------------------------------------------------------+
 	 */
 	
 	private boolean hasPopulationChanged(List<Composition> population, 
 			List<Composition> matingPool) {
 		int deviation = population.size() - 
 				(int) Math.round(population.size() * minEquality);
 		for (int i = 0; i < matingPool.size(); i++) {
 			if (deviation <= 0) {
 				return true;
 			}
 			else if (population.contains(matingPool.get(i))) {
 				population.remove(matingPool.get(i));
 			}
 			else {
 				deviation--;	
 			}
 		}
 		if (deviation <= 0) {
 			return true;
 		}
 		return false;
 	}
 
 	private void setVisualizationValues(List<Composition> population) {
 		List<Composition> differentSolutions = new LinkedList<Composition>();
 		double maxFitness = 0.0;
 		double totalFitness = 0.0;
 		for (int i = 0; i < population.size(); i++) {
 			if (!differentSolutions.contains(population.get(i))) {
 				differentSolutions.add(population.get(i));
 			}
 			if (population.get(i).getFitness() > maxFitness) {
 				maxFitness = population.get(i).getFitness();
 			}
 			totalFitness += population.get(i).getFitness();
 		}
 		numberOfDifferentSolutions.add(differentSolutions.size());
 		maxFitnessPerPopulation.add(maxFitness);
 		averageFitnessPerPopulation.add(totalFitness / population.size());
 	}
 	
 	private int[] permuteIndices(int populationSize) {
 		int[] permutationArray = new int[populationSize];
 		List<Integer> indicesList = new LinkedList<Integer>();
 		for (int i = 0; i < populationSize; i++) {
 			indicesList.add(i);
 		}
 		for (int i = 0; i < populationSize; i++) {
 			int permutationIndex = 0;
 			boolean swapLastIndex = false;
 			int indexPosition = 0;
 			do {
 				indexPosition = (int) Math.round(
 						(Math.random() * (indicesList.size() - 1)));
 				permutationIndex = indicesList.get(indexPosition);
 				if (indicesList.size() == 1 && 
 						i == permutationIndex) {
 					swapLastIndex = true;
 					permutationArray[i] = permutationArray[i - 1];
 					permutationArray[i - 1] = permutationIndex;
 					break;
 				}
 			} while(permutationIndex == i);
 			if (!swapLastIndex) {
 				permutationArray[i] = permutationIndex;
 			}
 			indicesList.remove(indexPosition);
 		}
 		return permutationArray;
 	}
 
 		  
 	
 	// GETTERS AND SETTERS
 	public List<ServiceClass> getServiceClassesList() {
 		return serviceClassesList;
 	}
 	public void setServiceClassesList(List<ServiceClass> serviceClassesList) {
 		this.serviceClassesList = serviceClassesList;
 	}
 	public Map<String, Constraint> getConstraintList() {
 		return constraintsMap;
 	}
 	public void setConstraintList(Map<String, Constraint> constraintsMap) {
 		this.constraintsMap = constraintsMap;
 	}
 	public List<AlgorithmSolutionTier> getAlgorithmSolutionTiers() {
 		return algorithmSolutionTiers;
 	}
 	public void setAlgorithmSolutionTiers(
 			List<AlgorithmSolutionTier> algorithmSolutionTiers) {
 		this.algorithmSolutionTiers = algorithmSolutionTiers;
 	}
 	public long getRuntime() {
 		return runtime;
 	}
 	public void setRuntime(long runtime) {
 		this.runtime = runtime;
 	}
 	public List<Integer> getNumberOfDifferentSolutions() {
 		return numberOfDifferentSolutions;
 	}
 	public List<Double> getMaxUtilityPerPopulation() {
 		return maxFitnessPerPopulation;
 	}
 	public List<Double> getAverageUtilityPerPopulation() {
 		return averageFitnessPerPopulation;
 	}
 	public int getWorkPercentage() {
 		return workPercentage;
 	}
 	public double getOptimalUtility() {
 		return algorithmSolutionTiers.get(0).
 				getServiceCompositionList().get(0).getUtility();
 	}
 	
 	
 	
 	// Nested comparator class
 	private class FitnessComparator implements Comparator<Composition> {
 		@Override
 		public int compare(Composition o1, Composition o2) {
 			if (o1.getFitness() < o2.getFitness()) {
 				return 1;
 			}
 			else if (o1.getFitness() > o2.getFitness()) {
 				return -1;
 			}
 			else {
 				return 0;
 			}
 		}
 	}
 }
