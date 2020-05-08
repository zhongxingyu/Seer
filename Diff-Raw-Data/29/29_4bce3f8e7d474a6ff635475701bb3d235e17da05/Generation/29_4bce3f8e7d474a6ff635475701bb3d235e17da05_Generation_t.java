 package net.sprauer.sitzplaner.model;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import net.sprauer.sitzplaner.EA.Chromosome;
 import net.sprauer.sitzplaner.EA.Configuration;
 import net.sprauer.sitzplaner.EA.EAFactory;
 
 public class Generation {
 
 	private List<Chromosome> population = new Vector<Chromosome>();
 	Chromosome bestSolution = null;
 	private final Configuration configuration;
 
 	public Generation(Configuration conf) throws Exception {
 		this.configuration = conf;
 		add(EAFactory.greedy(conf));
 	}
 
 	public Chromosome getBestSolution() {
 		return bestSolution;
 	}
 
 	public void clear() {
 		population = new Vector<Chromosome>();
 		bestSolution = null;
 	}
 
 	private void add(List<Chromosome> children) {
 		for (Chromosome chromosome : children) {
 			add(chromosome);
 		}
 	}
 
 	public void add(Chromosome chrome) {
 		population.add(chrome);
 		if (bestSolution == null || chrome.getFitness() >= bestSolution.getFitness()) {
 			bestSolution = chrome;
 		}
 	}
 
 	public void evolve() {
 		Collections.sort(population);
 		List<Chromosome> parents = population.subList(0, Math.min(population.size(), configuration.getParents()));
 		clear();
 		if (configuration.isStrategiePlus()) {
 			population.addAll(parents);
 		}
 
 		for (Chromosome chromosome : parents) {
			add(chromosome.mutate());
 		}
 	}
 
 }
