 package local.radioschedulers.ga;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.HashMap;
 import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
 
 import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
 import local.radioschedulers.LSTTime;
 import local.radioschedulers.Schedule;
 import local.radioschedulers.ScheduleSpace;
 import local.radioschedulers.preschedule.RequirementGuard;
 
 public abstract class GeneticAlgorithmScheduler implements IScheduler {
 
 	private static Logger log = Logger
 			.getLogger(GeneticAlgorithmScheduler.class);
 
	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
 	protected RequirementGuard requirementGuard;
 	protected int ngenes;
 
 	private double crossoverProbability = 0.1;
 	private double mutationProbability = 0.3;
 	private int populationSize = 10;
 	private int eliteSize = 1;
 	private int numberOfGenerations = 10;
 
 	private List<Schedule> population = new ArrayList<Schedule>();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IScheduler#schedule(java.util.Collection)
 	 */
 	public Schedule schedule(ScheduleSpace possibles) {
 		this.ngenes = calculateNGenes(possibles);
 		// log.debug("got ndays=" + ndays + " plus last minute="
 		// + possibles.findLastEntry().minute);
 		// log.debug("so I'd reckon we need " + ngenes + " Genes");
 
 		if (population == null)
 			population = new ArrayList<Schedule>();
 
 		try {
			population = evolveSchedules(possibles, population);
 			Schedule bestschedule = population.get(0);
 			return bestschedule;
 		} catch (Exception e) {
 			log.error(e);
 			return null;
 		}
 	}
 
 	public static int calculateNGenes(ScheduleSpace possibles) {
 		LSTTime last = possibles.findLastEntry();
 		return (int) (last.day.intValue() * ScheduleSpace.LST_SLOTS_PER_DAY
 				+ last.minute / ScheduleSpace.LST_SLOTS_MINUTES + 1);
 	}
 
 	/**
 	 * @return the full population from the last schedule() call
 	 */
 	public List<Schedule> getPopulation() {
 		return population;
 	}
 
 	/**
 	 * set the initial population
 	 * 
 	 * @param population
 	 */
 	public void setPopulation(List<Schedule> population) {
 		this.population = population;
 	}
 
 	/**
 	 * 
 	 * @param possibles
 	 *            space of possible schedules
 	 * @param s
 	 *            prior schedules to use
 	 * @return
 	 * @throws Exception
 	 */
 	protected abstract List<Schedule> evolveSchedules(ScheduleSpace possibles,
 			Collection<Schedule> s) throws Exception;
 
 	public void setCrossoverProbability(double crossoverProbability) {
 		this.crossoverProbability = crossoverProbability;
 	}
 
 	public double getCrossoverProbability() {
 		return crossoverProbability;
 	}
 
 	public void setMutationProbability(double mutationProbability) {
 		this.mutationProbability = mutationProbability;
 	}
 
 	public double getMutationProbability() {
 		return mutationProbability;
 	}
 
 	public int getPopulationSize() {
 		return populationSize;
 	}
 
 	public void setPopulationSize(int populationSize) {
 		this.populationSize = populationSize;
 	}
 
 	public void setEliteSize(int eliteSize) {
 		this.eliteSize = eliteSize;
 	}
 
 	public int getEliteSize() {
 		return eliteSize;
 	}
 
 	public int getNumberOfGenerations() {
 		return numberOfGenerations;
 	}
 
 	public void setNumberOfGenerations(int numberOfGenerations) {
 		this.numberOfGenerations = numberOfGenerations;
 	}
 }
