 package edacc.configurator.proar.algorithm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import edacc.api.API;
 import edacc.configurator.proar.SolverConfiguration;
 import edacc.configurator.proar.StatisticFunction;
 import edacc.parameterspace.ParameterConfiguration;
 import edacc.parameterspace.graph.ParameterGraph;
 import edacc.util.Pair;
 
 public class GA extends PROARMethods {
 	private ParameterGraph graph;
 	private List<Individual> population;
 	private List<Individual> oldIndividuals;
 	private Individual bestInd;
 	HashSet<ParameterConfiguration> createdParamConfigs;
 	
 	private float probN = 0.6f;
 	private int maxAge = 10;
 	private float mutationProb = 0.1f;
 	private float crossoverPercentage = 0.6f;
 	private int childCountLimit = 12;
 	private int minSameAncestorAgeDiff = 10;
 	private static int idCounter = 0;
 	public GA(API api, int idExperiment, StatisticFunction statistics, Random rng, Map<String, String> params) throws Exception {
 		super(api, idExperiment, statistics, rng, params);
 		graph = api.loadParameterGraphFromDB(idExperiment);
 		population = new ArrayList<Individual>();
 		oldIndividuals = new ArrayList<Individual>();
 		bestInd = null;
 		createdParamConfigs = new HashSet<ParameterConfiguration>();
 		
 		String val;
 		
 		if ((val = params.get("GA_probN")) != null) {
 			probN = Float.parseFloat(val);
 		}
 		if ((val = params.get("GA_maxAge")) != null) {
 			maxAge = Integer.parseInt(val);
 		}
 		if ((val = params.get("GA_mutationProb")) != null) {
 			mutationProb = Float.parseFloat(val);
 		}
 		if ((val = params.get("GA_crossoverPercentage")) != null) {
 			crossoverPercentage = Float.parseFloat(val);
 		}
 		if ((val = params.get("GA_childCountLimit")) != null) {
 			childCountLimit = Integer.parseInt(val);
 		}
 		
 		if (probN < 0.f || probN > 1.f || maxAge < 0 || mutationProb < 0.f || mutationProb > 1.f || crossoverPercentage < 0.f || crossoverPercentage > 1.f || childCountLimit < 0)  {
 			throw new IllegalArgumentException();
 		}
 		System.out.println("[GA] probN: " + probN);
 		System.out.println("[GA] maxAge: " + maxAge);
 		System.out.println("[GA] mutationProb: " + mutationProb);
 		System.out.println("[GA] crossoverPercentage: " + crossoverPercentage);
 		System.out.println("[GA] childCountLimit: " + childCountLimit);
 	}
 
 	@Override
 	public List<SolverConfiguration> generateNewSC(int num, List<SolverConfiguration> lastBestSCs, SolverConfiguration currentBestSC, int level, int currentLevel) throws Exception {
 		if (bestInd == null) {
 			bestInd = new Individual(currentBestSC);
 		} else {
 			if (bestInd.sc != currentBestSC) {
 				for (Individual ind : oldIndividuals) {
 					if (ind.sc == currentBestSC) {
 						bestInd = ind;
 						break;
 					}
 				}
 			}
 		}
 		
 		// debugging
 		if (bestInd == null || bestInd.sc != currentBestSC) {
 			System.out.println("[GA] BUG?? bestInd.sc != currentBestSC. created one. could be added by user.");
 			bestInd = new Individual(currentBestSC);
 		}
 
 		System.out.println("[GA] GA generate Solver configs: " + num);
 		System.out.println("[GA] Population size: " + population.size());
 		for (int i = population.size()-1; i >= 0; i--) {
 			if (population.get(i).getAge(currentLevel -1) > maxAge || population.get(i).getChildCount() > population.get(i).getMaxChildCount()) {
 				population.remove(i);
 			}
 		}
 		int index = 0;
 
 		while (oldIndividuals.size() > index) {
 			Individual cur = oldIndividuals.get(index);
 			if (cur.getSolverConfig().getLevel() != currentLevel -1) {
 				index++;
 				continue;
 			}
 			oldIndividuals.remove(index);
 
 			if (cur.getSolverConfig().getNumSuccessfulJobs() < 2) {
 				continue;
 			}
 			int maxChildCount = Math.min(cur.getSolverConfig().getNumSuccessfulJobs() / 2, childCountLimit);
 			if (maxChildCount == 0) {
 				continue;
 			}
 			cur.maxChildCount = maxChildCount;
			//System.out.println("[GA] Created individual with max child count " + maxChildCount);
 			if (cur.sc.getNumSuccessfulJobs() >= cur.sc.getJobCount() / 2) {
 				population.add(cur);
 			} 
 		}
 		System.out.println("[GA] Sorting solver configurations");
 		Collections.shuffle(population, rng);
 		
 		float avg = 0.f;
 		for (Individual i : population) {
 			avg += statistics.getCostFunction().calculateCost(i.getSolverConfig().getFinishedJobs());
 		}
 		
 		avg /= population.size() != 0 ? population.size() : 1;
 
 		System.out.println("[GA] Current population contains " + population.size() + " individuals.");
 		System.out.println("[GA] Average cost is " + avg + ".");
 
 		LinkedList<Individual> best = new LinkedList<Individual>();
 		best.addAll(population);
 		
 		System.out.println("[GA] Generating solver configurations");
 		LinkedList<SolverConfiguration> res = new LinkedList<SolverConfiguration>();
 		
 		int noPartner = 0;
 		while (res.size() < num - Math.ceil((1 - crossoverPercentage) * num) && best.size() >= 2) {
 			Individual m = best.pollLast();
 			Individual f = null;
 			for (Individual ind : best) {
 				boolean goodPartner = true;
 				for (Individual anc : ind.ancestors) {
 					if (m.ancestors.contains(anc) && anc.getAge(level) < minSameAncestorAgeDiff) {
 						goodPartner = false;
 						break;
 					}
 					 
 				}
 				if (goodPartner) {
 					f = ind;
 					break;
 				}
 			}
 			if (f == null) {
 				noPartner++;
 				
 				ParameterConfiguration pConfig = new ParameterConfiguration(m.getSolverConfig().getParameterConfiguration()); 
 				graph.mutateParameterConfiguration(rng, pConfig);
 				int mutationCount = 1;
 				while (true) {
 					if (!createdParamConfigs.contains(pConfig)) {
 						createdParamConfigs.add(pConfig);
 						break;
 					}
 					if (mutationCount > 100) {
 						break;
 					}
 					graph.mutateParameterConfiguration(rng, pConfig);
 					mutationCount++;
 				}
 				int idSolverConfig = api.createSolverConfig(idExperiment, pConfig, api.getCanonicalName(idExperiment, pConfig) + " level " + level);
				SolverConfiguration sc = new SolverConfiguration(idSolverConfig, pConfig, statistics, level);
 				sc.setName(mutationCount + " mutations");
 				res.add(sc);
 				Individual newInd = new Individual(sc);
 				oldIndividuals.add(newInd);
 				continue;
 			}
 			best.remove(f);
 
 			
			Pair<ParameterConfiguration, ParameterConfiguration> configs = graph.crossover(m.getSolverConfig().getParameterConfiguration(), f.getSolverConfig().getParameterConfiguration(), rng);
 
 			int firstMutationCount = 0;
 			int secondMutationCount = 0;
 			
 			if (rng.nextFloat() < mutationProb) {
 				graph.mutateParameterConfiguration(rng, configs.getFirst());
 				firstMutationCount++;
 			}
 			if (rng.nextFloat() < mutationProb) {
 				graph.mutateParameterConfiguration(rng, configs.getSecond());
 				secondMutationCount++;
 			}
 			
 			while (true) {
 				if (!createdParamConfigs.contains(configs.getFirst())) {
 					createdParamConfigs.add(configs.getFirst());
 					break;
 				}
 				if (firstMutationCount > 100) {
 					break;
 				}
 				graph.mutateParameterConfiguration(rng, configs.getFirst());
 				firstMutationCount++;
 			}
 			
 			while (true) {
 				if (!createdParamConfigs.contains(configs.getSecond())) {
 					createdParamConfigs.add(configs.getSecond());
 					break;
 				}
 				if (secondMutationCount > 100) {
 					break;
 				}
 				graph.mutateParameterConfiguration(rng, configs.getSecond());
 				secondMutationCount++;
 			}
 			
 			m.incrementChildCount();
 			f.incrementChildCount();
 
 			int idSolverConfig = api.createSolverConfig(idExperiment, configs.getFirst(), api.getCanonicalName(idExperiment, configs.getFirst()) + " level " + level);
			SolverConfiguration firstSC = new SolverConfiguration(idSolverConfig, configs.getFirst(), statistics, level);
 			
 			res.add(firstSC);
 			Individual newInd = new Individual(firstSC);
 			newInd.ancestors.addAll(m.ancestors);
 			newInd.ancestors.addAll(f.ancestors);
 			newInd.ancestors.add(m);
 			newInd.ancestors.add(f);
 			firstSC.setName("crossover - child " + m.getChildCount() + "/" + f.getChildCount() + (firstMutationCount != 0 ? " " + firstMutationCount + " mutations" : "") + " - " + newInd.ancestors.size() + " ancestors");
 			oldIndividuals.add(newInd);
 			
 			idSolverConfig = api.createSolverConfig(idExperiment, configs.getSecond(), api.getCanonicalName(idExperiment, configs.getSecond()) + " level " + level);
			SolverConfiguration secondSC = new SolverConfiguration(idSolverConfig, configs.getSecond(), statistics, level);
 			res.add(secondSC);
 			newInd = new Individual(secondSC);
 			newInd.ancestors.addAll(m.ancestors);
 			newInd.ancestors.addAll(f.ancestors);
 			newInd.ancestors.add(m);
 			newInd.ancestors.add(f);
 			secondSC.setName("crossover - child " + m.getChildCount() + "/" + f.getChildCount() + (secondMutationCount != 0 ? " " + secondMutationCount + " mutations" : "") + " - " + newInd.ancestors.size() + " ancestors");
 			oldIndividuals.add(newInd);
 		}
 		if (noPartner > 0)
 			System.out.println("[GA] Did not find a partner for " + noPartner + " individuals.");
 		
 		while (res.size() != num) {
 			boolean random = true;
 			ParameterConfiguration paramconfig;
 			if (currentBestSC != null && rng.nextFloat() < probN) {
 				random = false;
 				paramconfig = graph.getRandomNeighbour(currentBestSC.getParameterConfiguration(), rng);
 			} else {
 				paramconfig = graph.getRandomConfiguration(rng);
 			}
 			int mutationCount = 0;
 			while (true) {
 				if (!createdParamConfigs.contains(paramconfig)) {
 					createdParamConfigs.add(paramconfig);
 					break;
 				}
 				if (mutationCount > 100) {
 					break;
 				}
 				graph.mutateParameterConfiguration(rng, paramconfig);
 				mutationCount ++;
 			}
 			
 			int idSolverConfig = api.createSolverConfig(idExperiment, paramconfig, api.getCanonicalName(idExperiment, paramconfig) + " level " + level);
			SolverConfiguration randomConfig = new SolverConfiguration(idSolverConfig, paramconfig, statistics, level);
 			
 			res.add(randomConfig);
 			Individual newInd = new Individual(randomConfig);
 			if (!random) {
 				newInd.ancestors.addAll(bestInd.ancestors);
 				newInd.ancestors.add(bestInd);
 			}
 			randomConfig.setName((random ? "random" : "neighbour of " + currentBestSC.getIdSolverConfiguration()) 
 					+ (mutationCount != 0 ? " " + mutationCount + " mutations" : "")
 					+ (newInd.ancestors.size() != 0 ? " - " + newInd.ancestors.size() + " ancestors" : ""));
 			oldIndividuals.add(newInd);
 		}
 		
 		System.out.println("[GA] done.");
 		System.out.println("[GA] Solver configurations generated (overall): " + createdParamConfigs.size());
 		return res;
 	}
 
 	private class Individual implements Comparable<Individual> {
 		private int id;
 		private SolverConfiguration sc;
 		private int childCount;
 		private int maxChildCount;
 		private Set<Individual> ancestors;
 		public Individual(SolverConfiguration sc) {
 			this.sc = sc;
 			this.maxChildCount = 0;
 			childCount = 0;
 			this.ancestors = new HashSet<Individual>();
 			id = idCounter++;
 		}
 		
 		public int getChildCount() {
 			return childCount;
 		}
 		
 		public void incrementChildCount() {
 			childCount++;
 		}
 		
 		public SolverConfiguration getSolverConfig() {
 			return sc;
 		}
 		
 		public int getAge(int date) {
 			return date - sc.getLevel();
 		}
 		
 		public int getMaxChildCount() {
 			return maxChildCount;
 		}
 		
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result + id;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Individual other = (Individual) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 				return false;
 			if (id != other.id)
 				return false;
 			return true;
 		}
 
 		@Override
 		public int compareTo(Individual arg0) {
 			return sc.compareTo(arg0.sc);
 		}
 
 		private GA getOuterType() {
 			return GA.this;
 		}
 	}
 }
