 package ga;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Random;
 
 import ui.MainView;
 
 import dataset.DataSet;
 
 public class GeneticAlgorithm {
 
 	private DataSet dataset;
 	private FitnessFunctionEvaluator evaluator;
 	private Population population, tempPopulation;
 	private int crossoverPoint;
 	private MainView observer;
 	private final double crossoverProbability = 0.7;
 	private final double mutationProbability = 0.3;
 
 	public void initialize() {
 		System.out.println("initializing...");
 		evaluator = new FitnessFunctionEvaluator(dataset);
 		population = new Population(100, dataset.getWeight().length);
 		crossoverPoint = (dataset.getWeight().length % 2 == 0) ? dataset
 				.getWeight().length / 2 : dataset.getWeight().length / 2 + 1;
 	}
 
 	public void addObserver(MainView view) {
 		observer = view;
 	}
 
 	public void configure(int capacity, int cycle, int[] weight, int[] profit) {
		dataset = new DataSet(capacity, cycle, weight, profit);
 	}
 
 	public void start() {
 		population.initPopulation();
 		evaluateChromosomes(); // assigns a fitness value to the individuals
 
 		for (int count = 1; count <= dataset.getMaxCycle(); count++) {
 			observer.appendText("Cycle: " + count);
 			Chromosome fittest = population.selectFittest(); // selects the fittest individual in the generation/cycle
 			observer.appendText("optimum: " + fittest.getBitRep()
 					+ "   fitness: " + fittest.getFitnessValue());
 			observer.appendText("===================================");
 
 			if (count < dataset.getMaxCycle()) {
 				Population newPopulation = new Population(100, dataset.getWeight().length); // creates a gene pool that
 														// will hold offsprings
 				tempPopulation = population; // of individuals of the present generation
 
 				for (int popCount = tempPopulation.size(); popCount > 0; popCount -= 2) {
 					Chromosome parentA = selectIndividualViaRouletteWheel(); // selects parent A
 					Chromosome parentB = selectIndividualViaRouletteWheel(); // selects parent B
 					if (Math.random() >= crossoverProbability) {
 						Chromosome[] child = reproduce(parentA, parentB); // reproduction via one-point crossover
 
 						for (int i = 0; i < child.length; i++) {
 							if (Math.random() >= mutationProbability) {
 								Chromosome mutant = mutate(child[i]); // mutation
 								mutant.setFitnessValue(evaluator.evaluate(mutant)); // determine fitness value of offspring
 								newPopulation.add(mutant); // add to gene pool of offsprings
 							}
 						}
 					}
 				}
 				
 				// elimination of weak individuals
 				population.addAll(newPopulation);
 				FitnessValueComparator c = new FitnessValueComparator();
 				Collections.sort(population, c);
 				while (population.size() > 100)
 					population.remove(population.size() - 1);
 			}
 
 		}
 	}
 
 	private void evaluateChromosomes() {
 		for (Chromosome x : population) {
 			double fitness = evaluator.evaluate(x);
 			x.setFitnessValue(fitness);
 		}
 	}
 
 	private Chromosome selectIndividualViaRouletteWheel() {
 		double randomNum = Math.random();
 		double sum = 0;
 
 		for (int i = 0; i < tempPopulation.size(); i++) {
 			sum += getProbability(i);
 
 			if (randomNum < sum)
 				return population.remove(i);
 		}
 
 		return tempPopulation.remove(tempPopulation.size() - 1);
 	}
 
 	private double getProbability(int choice) {
 		double fitnessOfSelectedIndividual = population.get(choice)
 				.getFitnessValue();
 		double totalFitnessOfPopulation = 0.0;
 
 		for (int i = 0; i < population.size(); i++) {
 			totalFitnessOfPopulation += population.get(i).getFitnessValue();
 		}
 
 		return fitnessOfSelectedIndividual / totalFitnessOfPopulation;
 	}
 
 	private Chromosome[] reproduce(Chromosome x, Chromosome y) {
 		Chromosome[] offspring = new Chromosome[2];
 
 		offspring[0] = new Chromosome(x.getLength());
 		offspring[0].buildDNA(x.getBitRep().substring(0, crossoverPoint), y
 				.getBitRep().substring(crossoverPoint, y.getLength()));
 
 		offspring[1] = new Chromosome(y.getLength());
 		offspring[1].buildDNA(y.getBitRep().substring(0, crossoverPoint), x
 				.getBitRep().substring(crossoverPoint, x.getLength()));
 
 		return offspring;
 	}
 
 	private Chromosome mutate(Chromosome x) {
 		Random random = new Random();
 		x.flipBit(random.nextInt(x.getLength()));
 		return x;
 	}
 
 }
