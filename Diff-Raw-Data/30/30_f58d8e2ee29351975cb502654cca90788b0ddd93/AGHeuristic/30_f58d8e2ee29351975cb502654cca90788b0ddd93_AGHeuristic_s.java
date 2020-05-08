 package planning.heuristic;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Map;
 
 import org.jgap.Chromosome;
 import org.jgap.Configuration;
 import org.jgap.Gene;
 import org.jgap.Genotype;
 import org.jgap.IChromosome;
 import org.jgap.InvalidConfigurationException;
 import org.jgap.impl.ChromosomePool;
 import org.jgap.impl.CrossoverOperator;
 import org.jgap.impl.DefaultConfiguration;
 import org.jgap.impl.IntegerGene;
 import org.jgap.impl.MutationOperator;
 import org.jgap.impl.StockRandomGenerator;
 import org.jgap.impl.WeightedRouletteSelector;
 
import commons.cloud.Contract;
 import commons.cloud.Provider;
 import commons.cloud.User;
 import commons.io.HistoryBasedWorkloadParser;
 import commons.util.SimulationData;
 
 public class AGHeuristic implements PlanningHeuristic{
 	
 	private int POPULATION_SIZE = 1000;
 	private double CROSSOVER_RATE = 0.7;
 	private int MUTATION_DENOMINATOR = 1000/5;//0.5%
 	private double MINIMUM_IMPROVEMENT = 0.01;//1%
	private int MINIMUM_NUMBER_OF_EVOLUTIONS = 200;
 	
 	private int resourcesReservationLimit = 0;
 	private int maximumReservedResources = 0;
 	
 	private List<SimulationData> bestConfigs = new ArrayList<SimulationData>();
 	
 	@Override
 	public void findPlan(HistoryBasedWorkloadParser workloadParser,
 			List<Provider> cloudProviders, List<User> cloudUsers) {
 		
 		initProperties(cloudProviders);
 		
 		//Configuring genetic algorithm
 		Configuration config = new DefaultConfiguration();
 		try {
 			config.setPopulationSize(POPULATION_SIZE);
 			config.setPreservFittestIndividual(true);
 			config.setRandomGenerator(new StockRandomGenerator());
 			config.setChromosomePool(new ChromosomePool());
 			
 			config.addGeneticOperator(new CrossoverOperator(config, CROSSOVER_RATE));
 			config.addGeneticOperator(new MutationOperator(config, MUTATION_DENOMINATOR));
 			config.removeNaturalSelectors(true);
 			config.removeNaturalSelectors(false);
 			config.addNaturalSelector(new WeightedRouletteSelector(), true);
 	//		config.setKeepPopulationSizeConstant(true);
 	//		config.setNaturalSelector(null);//Tournament, WeightedRoullete
 
 			PlanningFitnessFunction myFunc = createFitnessFunction(workloadParser, cloudUsers, cloudProviders);
 			config.setFitnessFunction(myFunc);
 			
 			IChromosome sampleChromosome = createSampleChromosome(config);
 			config.setSampleChromosome(sampleChromosome);
 			
 			Genotype population = Genotype.randomInitialGenotype(config);
 
 			//evaluating population
			for(int i = 0; i < MINIMUM_NUMBER_OF_EVOLUTIONS; i++){
				population.evolve();
			}
			IChromosome previosFittestChromosome = population.getFittestChromosome();
 			population.evolve();
 			IChromosome lastFittestChromosome = population.getFittestChromosome();
			while(!isEvolutionComplete(previosFittestChromosome, lastFittestChromosome)){
 				population.evolve();
				previosFittestChromosome = lastFittestChromosome;
 				lastFittestChromosome = population.getFittestChromosome();
 			}
 			
 			//store best config
 			IChromosome fittestChromosome = population.getFittestChromosome();
 			SimulationData simulationData = myFunc.getDetailedEntry((Integer) fittestChromosome.getGene(0).getAllele());
 			this.bestConfigs.add(simulationData);
 			
 			//Keeping number of reserved resources to be used to configure next accounting system!
 			this.maximumReservedResources = Math.max(maximumReservedResources, simulationData.numberOfMachinesReserved);
 			
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
	private boolean isEvolutionComplete(IChromosome fittestChromosome,
			IChromosome lastFittestChromosome) {
 		double firstFitnessValue = fittestChromosome.getFitnessValue();
 		double difference = lastFittestChromosome.getFitnessValue() - firstFitnessValue;
 		if(Math.abs(difference/firstFitnessValue) < MINIMUM_IMPROVEMENT){
 			return true;
 		}
 		return false;
 	}
 
 	private void initProperties(List<Provider> cloudProviders) {
 		resourcesReservationLimit = cloudProviders.iterator().next().reservationLimit;
 	}
 
 	private IChromosome createSampleChromosome(Configuration config) throws InvalidConfigurationException {
 		Gene[] genes = new IntegerGene[1];
 		genes[0] = new IntegerGene(config, 0, resourcesReservationLimit);
 		IChromosome sampleChromosome = new Chromosome(config, genes);
 		return sampleChromosome;
 	}
 
 	private PlanningFitnessFunction createFitnessFunction(HistoryBasedWorkloadParser workloadParser, List<User> cloudUsers, List<Provider> cloudProvider) {
 		workloadParser.setReadNextPeriod(true);
 		return new PlanningFitnessFunction(workloadParser, cloudUsers, cloudProvider, this.maximumReservedResources);
 	}
 
 	@Override
 	public double getEstimatedProfit(int period) {
 		if(period > 0 && period < this.bestConfigs.size()){
 			return this.bestConfigs.get(period).estimatedUtility;
 		}
 		
 		return Double.POSITIVE_INFINITY;
 	}
 
 	@Override
 	public List<String> getPlan() {
 		List<String> planningData = new ArrayList<String>();
 		for(SimulationData data : this.bestConfigs){
 			planningData.add(data.numberOfMachinesReserved + "\t" + data.totalReservedConsumedTime
 					+ "\t" + data.numberOfOnDemandMachines + "\t" + data.totalOnDemandConsumedTime +
 					"\t" + data.totalInTransferred + "\t" + data.estimatedUtility + "\t" + data.estimatedReceipt
 					+ "\t" + data.estimatedCost);
 		}
 		return planningData;
 	}
 }
