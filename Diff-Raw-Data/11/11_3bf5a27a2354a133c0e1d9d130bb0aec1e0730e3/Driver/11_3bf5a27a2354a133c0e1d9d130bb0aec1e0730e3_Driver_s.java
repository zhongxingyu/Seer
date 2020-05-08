 package controller;
 
 import java.util.BitSet;
 import java.util.Random;
 
 import model.Crossover;
 import model.Genome;
 import model.Mutation;
 import model.PopMember;
 import model.Population;
 import knapsack.KnapSackInterface;
 import knapsack.KnapSackMutation;
 import knapsack.KnapSackSinglePointCrossover;
 import knapsack.KnapSackSmartCrossover;
 import knapsack.ReadIn;
 import knapsack.SackItem;
 import knapsack.SortedPopulation;
 import view.UserInterface;
 
 /**
  * Driver.java
  * @author John Rutherford
  * @version 5/3/13
  * 
  * Runs a genetic algorithm.
 */
 
 public class Driver {
 	
 	private SackItem[] items;
 	
 	private Read read;
 	private Population pop;
 	private Crossover cross;
 	private Mutation mute;
 	private UserInterface ui;
 	private double mRate; // the mutation rate
 	
 	/**
 	 * MAIN
 	 * Change these settings to change basic genetic algorithm functions such as
 	 * mutation technique, user interface, crossover technique, population, and mutation rate
 	 */
 	public static void main(String[] args) {
 		Read read = new ReadIn();
 		UserInterface ui = new KnapSackInterface();
 		Population pop = new SortedPopulation();
 		Crossover cross = new KnapSackSmartCrossover();
 		Mutation mute = new KnapSackMutation();
 		
 		Driver driver = new Driver(read, ui, pop, cross, mute);
 		driver.run();
 	}
 	
 	/**
 	 * Creates a driver that will use the input as the elements for its genetic algorithm
 	 * @param population The population of data that the program will run on
 	 * @param crossover The specific crossover implementation that will recombine two PopMembers
 	 * @param mutation A function object that will slightly alter a PopMember
 	 * @param mutationRate the percent chance, as a double, of a mutation occurring
 	 */
 	public Driver(Read read, UserInterface userInterface, Population population, Crossover crossover, Mutation mutation) {
 		// Initialize fields
 		this.read = read;
 		pop = population;
 		cross = crossover;
 		mute = mutation;
 		ui = userInterface;
 	}
 	
 	/**
 	 * Creates an empty driver for progressive initialization using the getter/setter methods
 	 */
 	public Driver() {
 		// TODO: Something here
 	}
 	
 	/**
 	 * I don't remember why this method exists
 	 */
 	public void setup() {
 		
 	}
 	
 	/**
 	 * This method begins the execution of the genetic algorithm
 	 */
 	public void run() {
 		setup();
 		ui.promptUser();
 		mRate = ui.getMRate();
 		read.read(ui.getName());
 		items = read.getItems();
 		
 		pop.setItems(items);
 		pop.setCapacity(read.getCapacity());
         pop.populate();
 		
 		PopMember best = pop.getBest();
 		displayBestSolution(best);
 		ui.displayBestScore(best.getFitness());
 		
 		do {
 			PopMember[] chosen = pop.returnParents(2);
 			Genome[] genes = new Genome[2];
 			genes[0] = chosen[0].getGenome();
 			genes[1] = chosen[1].getGenome();			
 			Genome[] children = cross.cross(genes, pop.getPopulation().length);
 			
 			Random rand = new Random();
 			for (int i=0; i < children.length; i++) {
				if (rand.nextDouble() > mRate) {
 					children[i] = mute.mutate(children[i]);
                }
 			}
 			
 			PopMember[] childMembers = new PopMember[2];
 			childMembers[0] = new PopMember(children[0], pop.evaluateFitness(children[0]));
 			childMembers[1] = new PopMember(children[1], pop.evaluateFitness(children[1]));
 		
 			pop.insert(childMembers[0]);
 			pop.insert(childMembers[1]);
 			
 			if (!best.equals(pop.getBest())) {
 				best = pop.getBest();
 				displayBestSolution(best);
 				ui.displayBestScore(best.getFitness());
 			}
 		} while(!stopCriteriaMet());
 	}
 	
 	/**
 	 * Crosses the Genotypes contained within the parameter array and adds the child/children
 	 * (depending upon what crossover is being employed) to the population
 	 */
 	public void cross(PopMember[] parents) {
 		Genome[] genomes = new Genome[parents.length];
 		for (int i = 0; i < parents.length; i++) {
 			genomes[i] = parents[i].getGenome();
 		}
 		cross.cross(genomes, pop.getPopulation().length);
 	}
 	
 	/**
 	 * Checks whether the stopping criteria for this algorithm has been met
 	 * @return true if the algorithm should stop, false otherwise
 	 */
 	public boolean stopCriteriaMet() {
 		if (pop.getBest().getFitness() - pop.getWorst().getFitness() <= .1) {
 			return true;
 		}
 		return false;
 	}
 	
 	public void displayBestSolution(PopMember best) {
 		BitSet bits = new BitSet(items.length);
 		bits = best.getGenome().getBits();
 		
 		String soln = "";
 		int next = bits.nextSetBit(0);
 		while (next != -1) {
 			soln = soln + items[next].toString();
 			next = bits.nextSetBit(next + 1);
 		}
 		ui.displayBestSolution(soln);
 	}
 	
 	/*
 	 * GETTER AND SETTER METHODS
 	 */
 	
 	/**
 	 * @return the population
 	 */
 	public Population getPop() {
 		return pop; 
 	}
 
 	/**
 	 * @param pop the population to set
 	 */
 	public void setPop(Population pop) {
 		this.pop = pop; 
 	}
 
 	/**
 	 * @return the crossover
 	 */
 	public Crossover getCross() {
 		return cross;
 	}
 
 	/**
 	 * @param cross the crossover to set
 	 */
 	public void setCross(Crossover cross) {
 		this.cross = cross;
 	}
 
 	/**
 	 * @return the mutation object
 	 */
 	public Mutation getMute() {
 		return mute;
 	}
 
 	/**
 	 * @param mute the mutation object to set
 	 */
 	public void setMute(Mutation mute) {
 		this.mute = mute;
 	}
 
 	/**
 	 * @return the mutation rate
 	 */
 	public double getmRate() {
 		return mRate;
 	}
 
 	/**
 	 * @param mRate the mutation rate to set
 	 */
 	public void setmRate(double mRate) {
 		this.mRate = mRate;
 	}
 }
