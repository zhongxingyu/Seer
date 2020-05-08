 package net.cammann.tom.fyp.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jgap.Chromosome;
 import org.jgap.Configuration;
 import org.jgap.FitnessFunction;
 import org.jgap.Gene;
 import org.jgap.Genotype;
 import org.jgap.IChromosome;
 import org.jgap.InvalidConfigurationException;
 import org.jgap.impl.DefaultConfiguration;
 import org.jgap.impl.IntegerGene;
 
 /**
  * @author TC
  * @version 0.8
  * @since 31/01/2012
  * 
  */
 public class GeneLab {
 	
 	private int popSize;
 	private int evolutions;
 	private final int numCycles;
 	private final LifeFactory factory;
 	private Configuration conf;
 	private Genotype population;
 	private final List<EvolutionCycleListener> cycleListeners;
 	private int cycleCount;
 	private int evoCount;
 	
 	public GeneLab(LifeFactory factory) {
 		this.factory = factory;
		popSize = 2000;
 		evoCount = 0;
 		evolutions = 20;
 		cycleCount = 0;
 		numCycles = 10;
 		cycleListeners = new ArrayList<EvolutionCycleListener>();
 		initConfig();
 	}
 	
 	private void initConfig() {
 		
 		try {
 			conf = new DefaultConfiguration();
 			Chromosome chromo = getChromosome();
 			conf.setSampleChromosome(chromo);
 			
 			FitnessFunction ff = factory.getFitnessFunction();
 			
 			conf.setFitnessFunction(ff);
 			
 			conf.setPopulationSize(popSize);
 			
 			population = Genotype.randomInitialGenotype(conf);
 			
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void setPopulationSize(int popSize) {
 		this.popSize = popSize;
 		try {
 			conf.setPopulationSize(popSize);
 		} catch (InvalidConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public int getPopulationSize() {
 		return conf.getPopulationSize() == popSize ? popSize : conf
 				.getPopulationSize();
 	}
 	
 	private Chromosome getChromosome() {
 		
 		int RANGE_OF_COMMANDS = factory.nullInstance().getCommandList().length - 1;
 		
 		try {
 			Gene[] genes = new Gene[29];
 			
 			// START_ENERGY
 			genes[0] = new IntegerGene(conf, 0, 200);
 			// MEMORY_LENGTH
 			genes[1] = new IntegerGene(conf, 5, 15);
 			
 			genes[2] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			
 			genes[3] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			
 			genes[4] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			
 			genes[5] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			genes[6] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			genes[7] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			genes[8] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			
 			genes[9] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			
 			for (int i = 10; i < (12 + 17); i++) {
 				genes[i] = new IntegerGene(conf, 0, RANGE_OF_COMMANDS);
 			}
 			
 			Chromosome sampleChromosome = new Chromosome(conf, genes);
 			
 			return sampleChromosome;
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		throw new IllegalStateException("Could not configure gene array");
 	}
 	
 	public IChromosome getBestSolutionSoFar() {
 		return population.getFittestChromosome();
 	}
 	
 	public int getEvolutions() {
 		return evolutions;
 	}
 	
 	public void setEvolutions(int evolutions) {
 		this.evolutions = evolutions;
 	}
 	
 	public void addEvolutionCycleListener(EvolutionCycleListener ecl) {
 		cycleListeners.add(ecl);
 		
 	}
 	
 	public void removeEvolutionCycleListener(EvolutionCycleListener ecl) {
 		cycleListeners.remove(ecl);
 	}
 	
 	public void start() {
 		
 		for (int i = 0; i < numCycles; i++) {
 			for (EvolutionCycleListener e : cycleListeners) {
 				e.startCycle(new EvolutionCycleEvent(
 						population.getPopulation(), cycleCount, evoCount));
 			}
 			
 			cycle();
 			
 			for (EvolutionCycleListener e : cycleListeners) {
 				e.endCycle(new EvolutionCycleEvent(population.getPopulation(),
 						cycleCount, evoCount));
 			}
 		}
 		
 	}
 	
 	public void cycle() {
 		
 		int mEvo = evolutions / numCycles;
 		int extra = evolutions - (mEvo * numCycles);
 		
 		if (cycleCount < numCycles) {
 			
 			if (cycleCount == numCycles - 1 && extra != 0) {
 				population.evolve(mEvo + extra);
 				
 				evoCount += mEvo + extra;
 				
 			} else {
 				
 				population.evolve(mEvo);
 				evoCount += mEvo;
 			}
 			cycleCount++;
 			
 		}
 		// stats.setPopulation(population.getPopulation());
 		// stats.printNFittest(0);
 		// stats.printNFittest(1);
 		// stats.printNFittest(2);
 		// stats.showFitnessGraph("Generation " + (i + 1));
 		
 	}
 	
 }
