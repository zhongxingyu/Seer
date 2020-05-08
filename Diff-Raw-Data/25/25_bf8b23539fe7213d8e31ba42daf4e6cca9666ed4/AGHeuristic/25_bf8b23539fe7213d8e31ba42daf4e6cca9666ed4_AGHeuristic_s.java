 package planning.heuristic;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.apache.commons.configuration.ConfigurationException;
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
 
 import planning.io.PlanningWorkloadParser;
 import planning.util.PlanIOHandler;
 import planning.util.Summary;
 import provisioning.Monitor;
 
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.User;
 import commons.sim.components.LoadBalancer;
 import commons.sim.jeevent.JEEventScheduler;
 import commons.sim.util.SimulatorProperties;
 
 public class AGHeuristic implements PlanningHeuristic{
 	
 	private static final long YEAR_IN_HOURS = 8640;
 	
 	private int POPULATION_SIZE = 2000;
 	private double CROSSOVER_RATE = 0.6;
 	private int MUTATION_DENOMINATOR = 1000/5;//0.5%
 	private double MINIMUM_IMPROVEMENT = 0.01;//1%
 	private int MINIMUM_NUMBER_OF_EVOLUTIONS = 40;
 	
 	private Map<User, List<Summary>> summaries;
 	private List<MachineType> types;
 
 	private IChromosome fittestChromosome;
 	
 	public AGHeuristic(JEEventScheduler scheduler, Monitor monitor, LoadBalancer[] loadBalancers){
 		this.types = new ArrayList<MachineType>();
 		this.summaries = new HashMap<User, List<Summary>>();
 	}
 	
 	@Override
 	public void findPlan(Provider[] cloudProviders, User[] cloudUsers){
 		
 		//Reading workload data
 		readWorkloadData(cloudUsers);
 	
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
 			config.addNaturalSelector(new WeightedRouletteSelector(config), true);
 	//		config.setKeepPopulationSizeConstant(true);
 	//		config.setNaturalSelector(null);//Tournament, WeightedRoullete
 
 			IChromosome sampleChromosome = createSampleChromosome(config, cloudProviders[0]);
 			config.setSampleChromosome(sampleChromosome);
 
 			PlanningFitnessFunction myFunc = createFitnessFunction(cloudUsers, cloudProviders);
 			config.setFitnessFunction(myFunc);
 			
 			for(int i = 0; i < 30; i++){
 				Genotype population = Genotype.randomInitialGenotype(config);
 	
 				//evaluating population
 				IChromosome previousFittestChromosome = null;
 				population.evolve();
 				IChromosome lastFittestChromosome = population.getFittestChromosome();
 				int evolutionsWithoutImprovement = 0;
 				
 				while(evolutionsWithoutImprovement < MINIMUM_NUMBER_OF_EVOLUTIONS){
 					population.evolve();
 					previousFittestChromosome = lastFittestChromosome;
 					lastFittestChromosome = population.getFittestChromosome();
 					if(isEvolutionComplete(previousFittestChromosome, lastFittestChromosome)){
 						evolutionsWithoutImprovement++;
 					}else{
 						evolutionsWithoutImprovement = 0;
 					}
 				}
 				
 				//store best config
 				IChromosome currentFittest = population.getFittestChromosome();
 				
 				if(fittestChromosome == null || currentFittest.getFitnessValue() > fittestChromosome.getFitnessValue()){
 					fittestChromosome = currentFittest;
 				}
 			}
 			
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		Map<MachineType, Integer> plan = this.getPlan(cloudUsers);
 		try {
 			PlanIOHandler.createPlanFile(plan, cloudProviders);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 	
 	private boolean isEvolutionComplete(IChromosome previousFittestChromosome, IChromosome lastFittestChromosome) {
 		double previousFitnessValue = previousFittestChromosome.getFitnessValue();
 		double difference = lastFittestChromosome.getFitnessValue() - previousFitnessValue;
 		if(Math.abs(difference/previousFitnessValue) < MINIMUM_IMPROVEMENT){
 			return true;
 		}
 		return false;
 	}
 
 	private void readWorkloadData(User[] cloudUsers) {
 		commons.config.Configuration simConfig = commons.config.Configuration.getInstance();
 		String[] workloads = simConfig.getWorkloads();
 		
 		//Applying a planning error in workload if it was defined!
 		try{
 			double error = simConfig.getDouble(SimulatorProperties.PLANNING_ERROR);
 			int totalWorkloads = (int)Math.round(workloads.length * (1+error));
 			String [] newWorkloads = new String[totalWorkloads];
 			
 			if(totalWorkloads > workloads.length){//Should add some workloads
 				for(int i = 0; i < workloads.length; i++){
 					newWorkloads[i] = workloads[i];
 				}
 				int difference = totalWorkloads - workloads.length;
 				int index = workloads.length;
 				for(int i = 0; i < difference; i++){
 					newWorkloads[index++] = workloads[i];
 				}
 			}else{//Should remove some workloads
 				for(int i = 0; i < totalWorkloads; i++){
 					newWorkloads[i] = workloads[i];
 				}
 			}
 			workloads = newWorkloads;
 		}catch(NoSuchElementException e){
 		}
 		
 		this.summaries = new HashMap<User, List<Summary>>();
 		
 		int index = 0;
 		for(String workload : workloads){
 			PlanningWorkloadParser parser;
 			try {
 				parser = new PlanningWorkloadParser(workload);
 				parser.readData();
 				this.summaries.put(cloudUsers[index++], parser.getSummaries());
 				
 			} catch (ConfigurationException e) {
 				throw new RuntimeException(e.getMessage());
 			}
 		}
 	}
 
 
 	private IChromosome createSampleChromosome(Configuration config, Provider cloudProvider) throws InvalidConfigurationException {
 		Gene[] genes = new IntegerGene[cloudProvider.getAvailableTypes().length];
 		
 		Map<MachineType, Integer> limits = findReservationLimits(cloudProvider);
 		int i = 0;
 		for(MachineType type : limits.keySet()){
 			types.add(type);
 			genes[i++] = new IntegerGene(config, 0, limits.get(type));
 		}
 		
 		IChromosome sampleChromosome = new Chromosome(config, genes);
 		return sampleChromosome;
 	}
 
 	private Map<MachineType, Integer> findReservationLimits(Provider cloudProvider) {
 		Map<MachineType, Integer> typesLimits = new HashMap<MachineType, Integer>();
 		
 		MachineType[] machineTypes = cloudProvider.getAvailableTypes();
 		long totalDemand = calcTotalDemand();
 		
 		for(MachineType type : machineTypes){
 			double yearFee = cloudProvider.getReservationOneYearFee(type);
 			double reservedCpuCost = cloudProvider.getReservedCpuCost(type);
 			double onDemandCpuCost = cloudProvider.getOnDemandCpuCost(type);
 			
 			long minimumHoursToBeUsed = Math.round(yearFee / (onDemandCpuCost - reservedCpuCost));
 			double usageProportion = 1.0 * minimumHoursToBeUsed / YEAR_IN_HOURS;
 			
 			int maximumNumberOfMachines = (int)Math.round(totalDemand / (usageProportion * YEAR_IN_HOURS));
 			typesLimits.put(type, maximumNumberOfMachines);
 		}
 		
 		return typesLimits;
 	}
 
 	private long calcTotalDemand() {
		long totalDemand = 0;
 		for(List<Summary> summaries : this.summaries.values()){
 			for(Summary summary : summaries){
 				totalDemand += summary.getTotalCpuHrs();
 			}
 		}
 		
		return totalDemand;
 	}
 
 	private PlanningFitnessFunction createFitnessFunction(User[] cloudUsers, Provider[] cloudProviders) {
 		return new PlanningFitnessFunction(this.summaries, cloudUsers, cloudProviders, this.types);
 	}
 
 	@Override
 	public double getEstimatedProfit(int period) {
 		return 0;
 	}
 
 	@Override
 	public Map<MachineType, Integer> getPlan(User[] cloudUsers) {
 		Map<MachineType, Integer> plan = new HashMap<MachineType, Integer>();
 		Gene[] genes = fittestChromosome.getGenes();
 		int index = 0;
 		
 		for(MachineType type : this.types){
 			plan.put(type, (Integer) genes[index++].getAllele());
 		}
		System.out.println("CONFIG: "+genes[0].getAllele()+" "+genes[1].getAllele()+" "+genes[2].getAllele());
		System.out.println("BEST: "+fittestChromosome.getFitnessValue());
 		
 		return plan;
 	}
 }
