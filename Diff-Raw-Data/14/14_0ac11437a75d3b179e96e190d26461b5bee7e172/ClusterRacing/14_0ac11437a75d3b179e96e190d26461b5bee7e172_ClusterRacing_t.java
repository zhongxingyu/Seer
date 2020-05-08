 package edacc.configurator.aac.racing;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import edacc.api.API;
 import edacc.api.costfunctions.PARX;
 import edacc.configurator.aac.AAC;
 import edacc.configurator.aac.JobListener;
 import edacc.configurator.aac.Parameters;
 import edacc.configurator.aac.SolverConfiguration;
 import edacc.configurator.aac.search.ibsutils.SolverConfigurationIBS;
 import edacc.configurator.aac.solvercreator.Clustering;
 import edacc.model.Experiment;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultDAO;
 import edacc.model.Instance;
 import edacc.model.InstanceDAO;
 import edacc.model.StatusCode;
 
 public class ClusterRacing extends RacingMethods implements JobListener {
 
 	private Clustering clustering;
 	private HashMap<Integer, List<Integer>> seeds;
 	private HashMap<Integer, SolverConfigurationMetaData> scs;
 	private HashSet<Integer> removedSCIds;
 	private HashMap<Integer, List<ExperimentResult>> instanceJobs;
 	private HashSet<Integer> instanceJobsLowerLimit;
 	private HashMap<Integer, Integer> incumbentPoints;
 	
 	private PARX par1 = new PARX(Experiment.Cost.resultTime, true, 0, 1);
 	
 	private int unsolvedInstancesMaxJobs = 10;
 	private int unsolvedInstancesMinPoints = 10;
 	private int incumbentWinnerInstances = 3;
 	
 	private boolean useAdaptiveInstanceTimeouts = true;
 	private int instanceTimeoutsExpId = -1;
 	private int instanceTimeoutsMinNumJobs = 11;
 	private float instanceTimeoutFactor = 1.5f;
 	private int limitCPUTimeMaxCPUTime = 600;
 	
 	private float clusteringThreshold = 0.9f;
 	
 	private int maxParcoursExpansionFactor;
 	private boolean clusteringChanged = true;
 	private HashMap<Integer, List<Integer>> cachedClustering = null;
 	
 	public ClusterRacing(AAC pacc, Random rng, API api, Parameters parameters, List<SolverConfiguration> firstSCs, List<SolverConfiguration> referenceSCs) throws Exception {
 		super(pacc, rng, api, parameters, firstSCs, referenceSCs);
 		pacc.addJobListener(this);
 		
 		maxParcoursExpansionFactor = parameters.getMaxParcoursExpansionFactor();
 		String val;
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_useAdaptiveInstanceTimeouts")) != null)
 			useAdaptiveInstanceTimeouts = Boolean.parseBoolean(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_instanceTimeoutsExpId")) != null)
 			instanceTimeoutsExpId = Integer.parseInt(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_instanceTimeoutsMinNumJobs")) != null)
 			instanceTimeoutsMinNumJobs = Integer.parseInt(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_instanceTimeoutFactor")) != null)
 			instanceTimeoutFactor = Float.parseFloat(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_limitCPUTimeMaxCPUTime")) != null)
 			limitCPUTimeMaxCPUTime = Integer.parseInt(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_clusteringThreshold")) != null)
 			clusteringThreshold = Float.parseFloat(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_unsolvedInstancesMaxJobs")) != null)
 			unsolvedInstancesMaxJobs = Integer.parseInt(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_unsolvedInstancesMinPoints")) != null)
 			unsolvedInstancesMinPoints = Integer.parseInt(val);
 		if ((val = parameters.getRacingMethodParameters().get("ClusterRacing_incumbentWinnerInstances")) != null)
 			incumbentWinnerInstances = Integer.parseInt(val);
 		
 		scs = new HashMap<Integer, SolverConfigurationMetaData>();
 		seeds = new HashMap<Integer, List<Integer>>();
 		
 		for (SolverConfiguration sc : firstSCs) {
 			scs.put(sc.getIdSolverConfiguration(), new SolverConfigurationMetaData(sc, null, null));
 			
 			if (useAdaptiveInstanceTimeouts) {
 				for (ExperimentResult j : sc.getJobs()) {
 					if (pacc.getCPUTimeLimit(j.getInstanceId()) < j.getCPUTimeLimit()) {
 						// todo: max cpu time limit?? might be difficult to implement.
 						pacc.changeCPUTimeLimit(j.getInstanceId(), j.getCPUTimeLimit(), null, false, false);
 					}
 					
 					List<Integer> seedList = seeds.get(j.getInstanceId());
 					if (seedList == null) {
 						seedList = new LinkedList<Integer>();
 						seeds.put(j.getInstanceId(), seedList);
 					}
 					if (!seedList.contains(j.getSeed())) {
 						seedList.add(j.getSeed());
 					}
 					if (seedList.size() > maxParcoursExpansionFactor) {
 						maxParcoursExpansionFactor = seedList.size();
 						pacc.log("[ClusterRacing] Warning: maxParcoursExpansionFactor is now " + seedList.size());
 					}
 				}
 			}
 		}
 		HashSet<Integer> instanceIds = null;
 		if (instanceTimeoutsExpId != -1) {
 			instanceIds = new HashSet<Integer>();
 		}
 		
 		instanceJobs = new HashMap<Integer, List<ExperimentResult>>();
 		instanceJobsLowerLimit = new HashSet<Integer>();
 		removedSCIds = new HashSet<Integer>();
 		List<Integer> instances = new LinkedList<Integer>();
 		
 				
 		for (Instance i : InstanceDAO.getAllByExperimentId(parameters.getIdExperiment())) {
 			instances.add(i.getId());
 			List<Integer> seedList = seeds.get(i.getId());
 			boolean addToInstanceIds = seedList == null;
 			if (seedList == null) {
 				seedList = new LinkedList<Integer>();
 				seeds.put(i.getId(), seedList);
 			}
 			for (int j = seedList.size(); j < maxParcoursExpansionFactor; j++) {
 				seedList.add(rng.nextInt(Integer.MAX_VALUE-1));
 			}
 			if (instanceIds != null && addToInstanceIds)
 				instanceIds.add(i.getId());
 		}
 		
 		clustering = new Clustering(instances, new HashMap<Integer, float[]>());
 		
 		if (instanceIds != null) {
 			HashMap<Integer, Integer> timeouts = new HashMap<Integer, Integer>();
 			for (ExperimentResult er : ExperimentResultDAO.getAllByExperimentId(instanceTimeoutsExpId)) {
 				Integer v = timeouts.get(er.getInstanceId());
 				if (v == null) {
 					timeouts.put(er.getInstanceId(), Math.min(er.getCPUTimeLimit(), limitCPUTimeMaxCPUTime));
 				} else {
 					if (er.getCPUTimeLimit() < v) {
 						timeouts.put(er.getInstanceId(), er.getCPUTimeLimit());
 					}
 				}
 			}
 			for (Entry<Integer, Integer> e : timeouts.entrySet()) {
 				pacc.changeCPUTimeLimit(e.getKey(), e.getValue(), null, false, false);
 			}
 		}
 		
 		if (instances.isEmpty()) {
 			throw new IllegalArgumentException("No instances selected.");
 		}
 		
 		boolean oldUseAdaptiveInstanceTimouts = useAdaptiveInstanceTimeouts;
 		useAdaptiveInstanceTimeouts = false;
 		for (SolverConfiguration sc : firstSCs) {
 			jobsFinished(sc.getFinishedJobs());
 		}
 		useAdaptiveInstanceTimeouts = oldUseAdaptiveInstanceTimouts;
 		List<Integer> unsolved = new LinkedList<Integer>();
 		unsolved.addAll(clustering.getNotUsedInstances());
 		HashMap<Integer, List<Integer>> c = getClustering(); // clustering.getClusteringHierarchical(Clustering.HierarchicalClusterMethod.AVERAGE_LINKAGE, 10);
 		
 		for (SolverConfiguration sc : firstSCs) {
 			Set<Integer> unsolved_copy = new HashSet<Integer>();
 			unsolved_copy.addAll(unsolved);
 			HashMap<Integer, List<Integer>> c_copy = new HashMap<Integer, List<Integer>>();
 			for (Entry<Integer, List<Integer>> e : c.entrySet()) {
 				List<Integer> copy = new LinkedList<Integer>();
 				copy.addAll(e.getValue());
 				c_copy.put(e.getKey(), copy);
 			}
 			
 			scs.get(sc.getIdSolverConfiguration()).update(unsolved_copy, c_copy);
 		}
 		
 		incumbentPoints = new HashMap<Integer, Integer>();
 		
 		for (SolverConfiguration sc : firstSCs) {
 			initializeSolverConfiguration(sc);
 			updateName(scs.get(sc.getIdSolverConfiguration()));
 		}
 	}
 	
 	public HashMap<Integer, List<Integer>> getClustering() {
 		if (clusteringChanged) {
 			cachedClustering = clustering.getClustering(false, clusteringThreshold);
 			clusteringChanged = false;
 		}
 		HashMap<Integer, List<Integer>> res = new HashMap<Integer, List<Integer>>();
 		for (Entry<Integer, List<Integer>> e : cachedClustering.entrySet()) {
 			List<Integer> copy = new LinkedList<Integer>();
 			copy.addAll(e.getValue());
 			res.put(e.getKey(), copy);
 		}
 		
 		return res;
 	}
 	
 
 	@Override
 	public int compareTo(SolverConfiguration sc1, SolverConfiguration sc2) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public List<SolverConfiguration> getBestSolverConfigurations() {
 		HashMap<Integer, List<Integer>> bestSCsClustering = getClustering();
 		
 		List<SolverConfiguration> best = new LinkedList<SolverConfiguration>();
 		for (int id : bestSCsClustering.keySet()) {
 			best.add(scs.get(id).sc);
 		}
 		// TODO: sort best?
 		return best;
 	}
 
 	@Override
 	public void solverConfigurationsFinished(List<SolverConfiguration> configs) throws Exception {
 		HashMap<Integer, List<Integer>> c = clustering.getClustering(false);
 		
 		for (SolverConfiguration sc : configs) {			
 			SolverConfigurationMetaData data = scs.get(sc.getIdSolverConfiguration());
 			List<Integer> copy = new LinkedList<Integer>();
 			
 			boolean racedAll = true;
 			// race(..) can modify the racingScs list..
 			copy.addAll(data.racingScs);
 			for (int id : copy) {
 				SolverConfiguration inc = scs.get(id).sc;
 				racedAll &= race(inc, data);
 			}
 			if (!racedAll) {
 				// add solver configuration to list new sc, and try to race in next iteration
 				pacc.addSolverConfigurationToListNewSC(sc);
 			} 
 			
 			updateName(data);
 		}
 
		for (SolverConfigurationMetaData data : scs.values()) {
			if (data.racingScs.isEmpty() && data.competitors.isEmpty() && !c.containsKey(data.sc.getIdSolverConfiguration())) {
				if (!removedSCIds.contains(data.sc.getIdSolverConfiguration())) {
					pacc.addSolverConfigurationToListNewSC(data.sc); // will be marked as removed in next iteration
				}
			}
		}
 	}
 
 	@Override
 	public void solverConfigurationsCreated(List<SolverConfiguration> scs) throws Exception {
 		for (SolverConfiguration sc : scs) {
 			if (!this.scs.containsKey(sc.getIdSolverConfiguration()))
 				initializeSolverConfiguration(sc);
 		}
 
 	}
 
 	@Override
 	public int computeOptimalExpansion(int coreCount, int jobs, int listNewSCSize) {
 		int res = 0;
 		if (coreCount < parameters.getMinCPUCount() || coreCount > parameters.getMaxCPUCount()) {
 			pacc.log("w Warning: Current core count is " + coreCount);
 		}
 		
 		int min_sc = (Math.max(Math.round(2.f * coreCount), 8) - jobs) / (parameters.getMinRuns() * maxParcoursExpansionFactor);
 		if (min_sc > 0) {
 			res = (Math.max(Math.round(3.f * coreCount), 8) - jobs) / (parameters.getMinRuns() * maxParcoursExpansionFactor);
 		}
 		if (res == 0 && coreCount - jobs > 0) {
 			res = 1;
 		}
 
 		if (listNewSCSize == 0 && res == 0) {
 			res = 1;
 		}
 		return res;
 	}
 
 	@Override
 	public void stopEvaluation(List<SolverConfiguration> scs) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<String> getParameters() {
 		LinkedList<String> res = new LinkedList<String>();
 		res.add("% instance time limit specific settings");
 		res.add("ClusterRacing_useAdaptiveInstanceTimeouts = " + useAdaptiveInstanceTimeouts);
 		res.add("ClusterRacing_instanceTimeoutsExpId = " + instanceTimeoutsExpId);
 		res.add("ClusterRacing_instanceTimeoutsMinNumJobs = " + instanceTimeoutsMinNumJobs);
 		res.add("ClusterRacing_instanceTimeoutFactor = " + instanceTimeoutFactor);
 		res.add("ClusterRacing_limitCPUTimeMaxCPUTime = " + limitCPUTimeMaxCPUTime);
 		res.add("% racing specific settings");
 		res.add("ClusterRacing_clusteringThreshold = " + clusteringThreshold);
 		res.add("ClusterRacing_unsolvedInstancesMaxJobs = " + unsolvedInstancesMaxJobs);
 		res.add("ClusterRacing_unsolvedInstancesMinPoints = " + unsolvedInstancesMinPoints);
 		res.add("ClusterRacing_incumbentWinnerInstances = " + incumbentWinnerInstances);
 		return res;
 	}
 
 	@Override
 	public void raceFinished() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public void updateName(SolverConfigurationMetaData data) {		
 		data.sc.setNameRacing(data.sc.getNumber() + " races: " + data.racingScs.size() + " competitors: " + data.competitors.size() + " points: " + incumbentPoints.get(data.sc.getIdSolverConfiguration()) + (removedSCIds.contains(data.sc.getIdSolverConfiguration()) ? " (removed)" : ""));
 	}
 	
 	private void addRuns(SolverConfiguration sc, int instanceid, int priority) throws Exception {
 		SolverConfigurationMetaData data = scs.get(sc.getIdSolverConfiguration());
 		if (data != null) {
 			data.unsolved.remove(instanceid);
 		}
 		for (int seed : seeds.get(instanceid)) {
 			pacc.addJob(sc, seed, instanceid, priority);
 		}
 		pacc.addSolverConfigurationToListNewSC(sc);
 	}
 	
 	private void initializeSolverConfiguration(SolverConfiguration sc) throws Exception {
 		Set<Integer> unsolved = new HashSet<Integer>();
 		unsolved.addAll(clustering.getNotUsedInstances());
 		HashMap<Integer, List<Integer>> c = getClustering(); // clustering.getClusteringHierarchical(Clustering.HierarchicalClusterMethod.AVERAGE_LINKAGE, 10);
 		
 		List<Integer> possibleInstanceIds = new LinkedList<Integer>();
 		for (int iid : unsolved) {
 			if (instanceJobs.get(iid) == null || instanceJobs.get(iid).size() < unsolvedInstancesMaxJobs) {
 				possibleInstanceIds.add(iid);
 			}
 		}
 		
 		for (int i = 0; i < parameters.getMinRuns() - sc.getJobCount() && !possibleInstanceIds.isEmpty(); i++) {
 			int rand = rng.nextInt(possibleInstanceIds.size());
 			int instanceid = possibleInstanceIds.get(rand);
 			addRuns(sc, instanceid, 0);
 			unsolved.remove(instanceid);
 			possibleInstanceIds.remove(rand);
 		}
 		
 		SolverConfigurationMetaData data = null;
 		if (scs.containsKey(sc.getIdSolverConfiguration())) {
 			data = scs.get(sc.getIdSolverConfiguration());
 		} else {
 			data = new SolverConfigurationMetaData(sc, unsolved, c);
 			scs.put(sc.getIdSolverConfiguration(), data);
 		}
 		//if (!data.racingScs.isEmpty()) {
 			
 			
 			boolean racedAll = true;
 			// race(..) can modify racingScs list
 			LinkedList<Integer> copy = new LinkedList<Integer>();
 			copy.addAll(data.racingScs);
 			for (Integer id : copy) {
 				SolverConfiguration inc = scs.get(id).sc;
 				racedAll &= race(inc, data);
 			}
 			if (!racedAll) {
 				pacc.addSolverConfigurationToListNewSC(sc);
 			}
 		//}
 			updateName(data);
 	}
 	
 	private void updatePoints(int scid, int num) {
 		Integer numPoints = incumbentPoints.get(scid);
 		if (numPoints == null) {
 			numPoints = 0;
 		}
 		numPoints+=num;
 		incumbentPoints.put(scid, numPoints);
 		pacc.log("[ClusterRacing] Solver config " + scid + " has now " + numPoints + " points.");
 	}
 	
 	private void addUnsolvedJobs(SolverConfigurationMetaData data) throws Exception {
 		Integer numPoints = incumbentPoints.get(data.sc.getIdSolverConfiguration());
 		if (numPoints == null) {
 			numPoints = 0;
 		}
 		List<Integer> possibleInstances = new LinkedList<Integer>();
 		if (numPoints > unsolvedInstancesMinPoints) {
 			if (data.unsolved.isEmpty()) {
 				// TODO: cache next info!
 				HashSet<Integer> hasInstanceRuns = new HashSet<Integer>();
 				for (ExperimentResult er : data.sc.getJobs()) {
 					hasInstanceRuns.add(er.getInstanceId());
 				}
 				// TODO: use 1.0-clustering??
 				for (Entry<Integer, List<Integer>> e : getClustering().entrySet()) {
 					// TODO: can this happen? + inefficient?
 					if (data.racingScs.contains(new Integer(e.getKey()))) {
 						continue;
 					}
 					
 					boolean race = true;
 					for (Integer iid : e.getValue()) {
 						if (hasInstanceRuns.contains(iid)) {
 							race = false;
 							break;
 						}
 					}
 					if (race) {
 						data.racingScs.add(e.getKey());
 						SolverConfigurationMetaData inc = scs.get(e.getKey());
 						inc.competitors.add(e.getKey());
 						
 						this.race(inc.sc, data);
 					}
 				}
 				
 			} else {
 				possibleInstances.addAll(data.unsolved);
 			}
 		} else {
 			HashSet<Integer> currentUnsolved = clustering.getNotUsedInstances();
 			for (Integer iid : data.unsolved) {
 				if (!currentUnsolved.contains(iid)) {
 					possibleInstances.add(iid);
 				}
 			}
 		}
 		
 		int numInstances = incumbentWinnerInstances;
 		
 		while (numInstances > 0 && !possibleInstances.isEmpty()) {
 			int rand = rng.nextInt(possibleInstances.size());
 			int iid = possibleInstances.get(rand);
 			addRuns(data.sc, iid, Integer.MAX_VALUE - data.sc.getIdSolverConfiguration());
 			possibleInstances.remove(rand);
 			numInstances--;
 		}
 	}
 	
 	private boolean race(SolverConfiguration incumbent, SolverConfigurationMetaData data) throws Exception {
 		List<Integer> instanceIds = data.c.get(incumbent.getIdSolverConfiguration());
 		
 		HashMap<Integer, List<ExperimentResult>> myJobs = new HashMap<Integer, List<ExperimentResult>>();
 		
 		List<ExperimentResult> hisJobsAll = new LinkedList<ExperimentResult>();
 		List<ExperimentResult> myJobsAll = new LinkedList<ExperimentResult>();
 		
 		for (int id : instanceIds) {
 			myJobs.put(id, new LinkedList<ExperimentResult>());
 		}
 		
 		for (ExperimentResult er : data.sc.getJobs()) {
 			if (myJobs.containsKey(er.getInstanceId())) {
 				myJobsAll.add(er);
 				myJobs.get(er.getInstanceId()).add(er);
 			}
 		}
 		
 		boolean jobsFinished = true;
 		for (ExperimentResult er : incumbent.getJobs()) {
 			if (myJobs.containsKey(er.getInstanceId()) && !myJobs.get(er.getInstanceId()).isEmpty()) {
 				if (er.getStatus().equals(StatusCode.RUNNING) || er.getStatus().equals(StatusCode.NOT_STARTED)) {
 					jobsFinished = false;
 					break;
 				}
 				hisJobsAll.add(er);
 			} 
 		}
 		if (!jobsFinished) {
 			return false;
 		}
 		
 		if (myJobsAll.isEmpty()) {
 			int instanceid = instanceIds.get(rng.nextInt(instanceIds.size()));
 			addRuns(data.sc, instanceid, Integer.MAX_VALUE - data.sc.getIdSolverConfiguration());
 		} else {
 			List<Integer> instances = new LinkedList<Integer>();
 			for (Entry<Integer, List<ExperimentResult>> e : myJobs.entrySet()) {
 				if (!e.getValue().isEmpty()) {
 					instances.add(e.getKey());
 				}
 			}
 			pacc.log("Inc (" + incumbent.getIdSolverConfiguration() + ") Cost: " + parameters.getStatistics().getCostFunction().calculateCost(hisJobsAll) + " - Competitor (" + data.sc.getIdSolverConfiguration() + ") Cost: " + parameters.getStatistics().getCostFunction().calculateCost(myJobsAll));
 			pacc.log("Instances: " + instances);
 			boolean removeScFromRace = false;
 			if (parameters.getStatistics().compare(hisJobsAll, myJobsAll) <= 0) {
 				// race goes on..
 				pacc.log("=> better than incumbent.. trying to find more instances..");
 				
 				
 				LinkedList<Integer> possibleInstances = new LinkedList<Integer>();
 				int icount = 0;
 				for (int id : instanceIds) {
 					if (myJobs.get(id).isEmpty()) {
 						possibleInstances.add(id);
 					} else {
 						icount++;
 					}
 				}
 				
 				if (possibleInstances.isEmpty()) {
 					pacc.log("No more instances for " + data.sc.getIdSolverConfiguration() + " vs " + incumbent.getIdSolverConfiguration());
 					updatePoints(incumbent.getIdSolverConfiguration(), -1);
 					
 					addUnsolvedJobs(data);
 					
 					removeScFromRace = true;
 				} else {
 					for (int i = 0; i < icount && !possibleInstances.isEmpty(); i++) {
 						int rand = rng.nextInt(possibleInstances.size());
 						addRuns(data.sc, possibleInstances.get(rand), Integer.MAX_VALUE - data.sc.getIdSolverConfiguration());
 						possibleInstances.remove(rand);
 					}
 				}
 			} else {
 				// lost.
 				
 				
 				
 				pacc.log("Solver Configuration " + data.sc.getIdSolverConfiguration() + " lost against " + incumbent.getIdSolverConfiguration());
 				updatePoints(incumbent.getIdSolverConfiguration(), 1);
 				addUnsolvedJobs(scs.get(incumbent.getIdSolverConfiguration()));
 				
 				removeScFromRace = true;
 			}
 			if (removeScFromRace) {
 				data.racingScs.remove(new Integer(incumbent.getIdSolverConfiguration()));
 				scs.get(incumbent.getIdSolverConfiguration()).competitors.remove(new Integer(data.sc.getIdSolverConfiguration()));
 			}
 			
 			
 			// TODO: delete config? (inc and/or racing)
 			/*
 			if (data.racingScs.isEmpty()) {
 				pacc.log("Removing solver configuration " + data.sc.getIdSolverConfiguration() + " from list.");
 				removedSCIds.add(data.sc.getIdSolverConfiguration());
 				clustering.remove(data.sc.getIdSolverConfiguration());
 			}*/
 		}
 		return true;
 	}
 	
 	
 	private class SolverConfigurationMetaData {
 		SolverConfiguration sc;
 		Set<Integer> unsolved;
 		HashMap<Integer, List<Integer>> c;
 		List<Integer> racingScs;
 		List<Integer> competitors;
 		
 		public SolverConfigurationMetaData(SolverConfiguration sc, Set<Integer> unsolved, HashMap<Integer, List<Integer>> c) {
 			this.sc = sc;
 			this.competitors = new LinkedList<Integer>();
 			update(unsolved, c);
 		}
 		
 		public void update(Set<Integer> unsolved, HashMap<Integer, List<Integer>> c) {
 			this.unsolved = unsolved;
 			this.c = c;
 			this.racingScs = new LinkedList<Integer>();
 			if (c != null) {
 				if (sc instanceof SolverConfigurationIBS && !((SolverConfigurationIBS) sc).preferredInstanceIds.isEmpty()) {
 					
 					for (Entry<Integer, List<Integer>> e : c.entrySet()) {
 						if (e.getKey().equals(sc.getIdSolverConfiguration())) {
 							continue;
 						}
 						
 						boolean add = false;
 						for (Integer iid : e.getValue()) {
 							if (((SolverConfigurationIBS) sc).preferredInstanceIds.contains(iid)) {
 								add = true;
 								break;
 							}
 						}
 						if (add) {
 							this.racingScs.add(e.getKey());
 						}
 					}
 					
 					pacc.log("[ClusterRacing] Num racing scs for new solver configuration with preferred instances: " + this.racingScs.size());
 				} else {
 					this.racingScs.addAll(c.keySet());
 					this.racingScs.remove(new Integer(sc.getIdSolverConfiguration()));
 				}
 			}
 			for (Integer i : racingScs) {
 				scs.get(i).competitors.add(sc.getIdSolverConfiguration());
 			}
 		}
 	}
 	
 	private int median(List<ExperimentResult> jobs) {
 		Collections.sort(jobs, new Comparator<ExperimentResult>() {
 
 			@Override
 			public int compare(ExperimentResult arg0, ExperimentResult arg1) {
 				if (arg0.getResultCode().isCorrect() && !arg1.getResultCode().isCorrect()) {
 					return -1;
 				} else if (!arg0.getResultCode().isCorrect() && arg1.getResultCode().isCorrect()) {
 					return 1;
 				} else if (!arg0.getResultCode().isCorrect() && !arg1.getResultCode().isCorrect()) {
 					return 0;
 				} else {
 					if (arg0.getResultTime() < arg1.getResultTime()) {
 						return -1;
 					} else if (arg1.getResultTime() < arg0.getResultTime()) {
 						return 1;
 					} else {
 						return 0;
 					}
 				}
 			}
 			
 		});
 		
 		ExperimentResult m = jobs.get(jobs.size() >> 1);
 		if (m.getResultCode().isCorrect()) {
 			return Math.round(m.getResultTime());
 		} else {
 			return pacc.getCPUTimeLimit(m.getInstanceId());
 		}
 	}
 	
 	public void updateModel(SolverConfiguration sc, int instanceId) {
 		List<ExperimentResult> list = new LinkedList<ExperimentResult>();
 		boolean solved = false;
 		for (ExperimentResult er : sc.getJobs()) {
 			if (instanceId == er.getInstanceId()) {
 				list.add(er);
 				solved |= er.getResultCode().isCorrect();
 			}
 		}
 		float cost = (solved ? par1.calculateCost(list) : Float.POSITIVE_INFINITY);
 		clustering.update(sc.getIdSolverConfiguration(), instanceId, cost);
 		clusteringChanged = true;
 	}
 
 
 	@Override
 	public void jobsFinished(List<ExperimentResult> results) throws Exception {
 		for (ExperimentResult result: results) {
 			updateModel(scs.get(result.getSolverConfigId()).sc, result.getInstanceId());
 		}
 		
 		HashSet<Integer> instanceIds = new HashSet<Integer>();
 		for (ExperimentResult result : results) {
 			instanceIds.add(result.getInstanceId());
 
 			List<ExperimentResult> jobs = instanceJobs.get(result.getInstanceId());
 			if (jobs == null) {
 				jobs = new LinkedList<ExperimentResult>();
 				instanceJobs.put(result.getInstanceId(), jobs);
 			}
 			jobs.add(result);
 		}
 		
 		if (useAdaptiveInstanceTimeouts) {
 			
 			for (int instanceId : instanceIds) {
 				List<ExperimentResult> jobs = instanceJobs.get(instanceId);
 				for (int i = jobs.size() - 1; i >= 0; i--) {
 					if (removedSCIds.contains(jobs.get(i).getSolverConfigId())) {
 						jobs.remove(i);
 					}
 				}
 				
 				if (jobs.size() > instanceTimeoutsMinNumJobs) {
 					int current_limit = pacc.getCPUTimeLimit(instanceId);
 					int m = Math.round(median(jobs) * instanceTimeoutFactor);
 					if (m < 1)
 						m = 1;
 					if (m < current_limit) {
 						List<SolverConfiguration> tmp = new LinkedList<SolverConfiguration>();
 						for (SolverConfigurationMetaData data : scs.values()) {
 							if (!removedSCIds.contains(data.sc.getIdSolverConfiguration())) {
 								tmp.add(data.sc);
 							}
 						}
 						pacc.log("Setting CPU Timelimit of " + instanceId + " to " + m + " (lower).");
 						for (SolverConfiguration config : pacc.changeCPUTimeLimit(instanceId, m, tmp, true, true)) {
 							pacc.addSolverConfigurationToListNewSC(config);
 						}
 						
 						for (SolverConfiguration sc : tmp) {
 							updateModel(sc, instanceId);
 						}
 
 						instanceJobsLowerLimit.add(instanceId);
 					} else if (!instanceJobsLowerLimit.contains(instanceId)) {
 						List<SolverConfiguration> tmp = new LinkedList<SolverConfiguration>();
 						for (SolverConfigurationMetaData data : scs.values()) {
 							if (!removedSCIds.contains(data.sc.getIdSolverConfiguration())) {
 								tmp.add(data.sc);
 							}
 						}
 						m = Math.min(limitCPUTimeMaxCPUTime, Math.round(current_limit * instanceTimeoutFactor));
 						if (m > current_limit) {
 							pacc.log("Setting CPU Timelimit of " + instanceId + " to " + m + " (higher).");
 							for (SolverConfiguration config : pacc.changeCPUTimeLimit(instanceId, m, tmp, true, true)) {
 								pacc.addSolverConfigurationToListNewSC(config);
 							}
 							
 							for (SolverConfiguration sc : tmp) {
 								updateModel(sc, instanceId);
 							}
 						}
 					}
 
 					// remove restarted jobs
 					for (int i = jobs.size() - 1; i >= 0; i--) {
 						if (jobs.get(i).getStatus().equals(StatusCode.NOT_STARTED)) {
 							jobs.remove(i);
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
