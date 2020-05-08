 package edacc.configurator.aac.racing;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.rosuda.JRI.Rengine;
 
 import edacc.api.API;
 import edacc.configurator.aac.AAC;
 import edacc.configurator.aac.InstanceIdSeed;
 import edacc.configurator.aac.Parameters;
 import edacc.configurator.aac.SolverConfiguration;
 import edacc.configurator.aac.course.StratifiedClusterCourse;
 import edacc.configurator.aac.util.RInterface;
 import edacc.model.ConfigurationScenarioDAO;
 import edacc.model.ExperimentDAO;
 import edacc.model.ExperimentResult;
 import edacc.model.Instance;
 import edacc.model.InstanceDAO;
 import edacc.model.ResultCode;
 import edacc.model.StatusCode;
 import edacc.model.Experiment.Cost;
 import edacc.configurator.aac.JobListener;
 
 public class DefaultSMBO extends RacingMethods implements JobListener {
 	SolverConfiguration bestSC;
 	int incumbentNumber;
 	int num_instances;
 	private int numSCs = 0;
 	private int curThreshold = 0;
 	
 	private List<InstanceIdSeed> completeCourse;
 	private Rengine rengine;
 	
 	private Map<Integer, Integer> limitByInstance = new HashMap<Integer, Integer>();
 	
 	private int increaseIncumbentRunsEvery = 32;
     private String featureFolder = null;
     private String featureCacheFolder = null;
     private boolean useClusterCourse = false;
 	// when selecting jobs from the incumbent, prefer jobs that didn't time out
 	private boolean aggressiveJobSelection = false;
 	private boolean adaptiveCapping = false;
 	private float slackFactor = 1.5f;
 	private boolean clusterSizeExpansion = false;
 	
 	StratifiedClusterCourse course = null;
 	
 	//public boolean initialDesignMode = true;
 	
 	HashSet<Integer> stopEvalSolverConfigIds = new HashSet<Integer>();
 	Set<SolverConfiguration> challengers = new HashSet<SolverConfiguration>();
 
 	public DefaultSMBO(AAC aac, Random rng, API api, Parameters parameters, List<SolverConfiguration> firstSCs, List<SolverConfiguration> referenceSCs) throws Exception {
 		super(aac, rng, api, parameters, firstSCs, referenceSCs);
 		aac.addJobListener(this);
 		incumbentNumber = 0;
 		num_instances = ConfigurationScenarioDAO.getConfigurationScenarioByExperimentId(parameters.getIdExperiment()).getCourse().getInitialLength();
 	
 		String val;
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_increaseIncumbentRunsEvery")) != null)
             increaseIncumbentRunsEvery = Integer.valueOf(val);
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_aggressiveJobSelection")) != null)
             aggressiveJobSelection = Integer.valueOf(val) == 1;
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_featureFolder")) != null)
             featureFolder = val;
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_featureCacheFolder")) != null)
             featureCacheFolder = val;
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_useClusterCourse")) != null)
             useClusterCourse = Integer.valueOf(val) == 1;
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_adaptiveCapping")) != null)
             adaptiveCapping = Integer.valueOf(val) == 1;
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_slackFactor")) != null)
             slackFactor = Float.valueOf(val);
         if ((val = parameters.getRacingMethodParameters().get("DefaultSMBO_clusterSizeExpansion")) != null)
             clusterSizeExpansion = Integer.valueOf(val) == 1;
         
         if (useClusterCourse) {
             rengine = RInterface.getRengine();
             if (rengine.eval("library(asbio)") == null) {
                 rengine.end();
                 throw new Exception("Did not find R library asbio (try running install.packages(\"asbio\")).");
             }
             if (rengine.eval("library(survival)") == null) {
                 rengine.end();
                 throw new Exception("Did not find R library survival (should come with R though).");
             }
             
             pacc.log("[DefaultSMBO] Calculating instance hardness (" + parameters.getStatistics().getCostFunction().databaseRepresentation() + ") from reference configuration results:");
             Map<Integer, Double> instanceHardness = new HashMap<Integer, Double>();
             for (SolverConfiguration refConfig: referenceSCs) 
             	pacc.log("Found reference solver: "+ refConfig.getName() + " with ID: "+refConfig.getIdSolverConfiguration()+ " with a total of "+refConfig.getNumFinishedJobs());
             for (Instance instance: InstanceDAO.getAllByExperimentId(parameters.getIdExperiment())) {
                 double instanceAvg = 0.0;
                 int count = 0;
                 for (SolverConfiguration refConfig: referenceSCs) {
                     for (ExperimentResult run: refConfig.getJobs()) {
                         if (run.getInstanceId() == instance.getId()) {
                             instanceAvg += parameters.getStatistics().getCostFunction().singleCost(run);
                             count++;
                         }
                     }
                 }
                 if (count != 0) instanceAvg /= count;
                 else instanceAvg = Double.MAX_VALUE;
                 instanceHardness.put(instance.getId(), instanceAvg);
                // pacc.log("[DefaultSMBO] " + instance.getName() + ": " + instanceAvg);
             }
 
             course = new StratifiedClusterCourse(rengine, api.getExperimentInstances(parameters.getIdExperiment()), null, null, parameters.getMaxParcoursExpansionFactor(), rng, featureFolder, featureCacheFolder, instanceHardness);
             this.completeCourse = course.getCourse();
             List<Instance> instances = InstanceDAO.getAllByExperimentId(parameters.getIdExperiment());
             Map<Integer, Instance> instanceById = new HashMap<Integer, Instance>();
             for (Instance i: instances) instanceById.put(i.getId(), i);
             pacc.log("[DefaultSMBO] Clustered instances into " + course.getK() + " clusters. Complete course:");
            /* for (InstanceIdSeed isp: completeCourse) {
                 pacc.log("[DefaultSMBO] " + instanceById.get(isp.instanceId) + ", " + isp.seed);
             }*/
         }
         
         curThreshold = increaseIncumbentRunsEvery;
 		
 		if (!firstSCs.isEmpty()) {
 			initBestSC(firstSCs.get(0));
 		}
 	}
 	
 	private void initBestSC(SolverConfiguration sc) throws Exception {
 		this.bestSC = firstSCs.get(0);
 		bestSC.setIncumbentNumber(incumbentNumber++);
 		pacc.log("i " + pacc.getWallTime() + " ," + bestSC.getCost() + ", n.A.," + bestSC.getIdSolverConfiguration() + ", n.A.," + bestSC.getParameterConfiguration().toString());
 		
 		int expansion = 0;
 		if (bestSC.getJobCount() < parameters.getMaxParcoursExpansionFactor() * num_instances) {
 			if (clusterSizeExpansion)
 					expansion = Math.min(parameters.getMaxParcoursExpansionFactor() * num_instances - bestSC.getJobCount(), course.getK());
 			else
 				expansion = Math.min(parameters.getMaxParcoursExpansionFactor() * num_instances - bestSC.getJobCount(), parameters.getInitialDefaultParcoursLength());
             if (useClusterCourse) {
                 for (int i = 0; i < expansion; i++) {
                     pacc.addJob(bestSC, completeCourse.get(bestSC.getJobCount()).seed,
                             completeCourse.get(bestSC.getJobCount()).instanceId, parameters.getMaxParcoursExpansionFactor()
                                     * num_instances - bestSC.getJobCount());
                 }
             } else {
                 pacc.expandParcoursSC(bestSC, expansion);
             }
 		}
 		if (expansion > 0) {
 			pacc.log("c Expanding parcours of best solver config " + bestSC.getIdSolverConfiguration() + " by " + expansion);
 		}
 		// update the status of the jobs of bestSC and if first level wait
 		// also for jobs to finish
 		if (expansion > 0) {
 			pacc.log("c Waiting for currently best solver config " + bestSC.getIdSolverConfiguration() + " to finish " + expansion + "job(s)");
 			while (true) {
 				pacc.updateJobsStatus(bestSC);
 				if (bestSC.getNotStartedJobs().isEmpty() && bestSC.getRunningJobs().isEmpty()) {
 					break;
 				}
 				pacc.sleep(1000);
 			}
 			pacc.validateIncumbent(bestSC);
 		} else {
 			pacc.updateJobsStatus(bestSC);
 		}
 	}
 	
 
 	public String toString(){
 		return "DefaultSMBO racing method";
 	}
 	@Override
 	public int compareTo(SolverConfiguration sc1, SolverConfiguration sc2) {
 		return sc1.compareTo(sc2);
 	}
 
 	@Override
 	public void solverConfigurationsFinished(List<SolverConfiguration> scs) throws Exception {
 	    /*if (initialDesignMode) {
 	        pacc.updateJobsStatus(bestSC);
 	        scs.clear();
 	        scs.addAll(challengers);
 	        scs.add(bestSC);
 	    }*/
 	    
 		for (SolverConfiguration sc : scs) {
 		    if (sc.getJobCount() != sc.getFinishedJobs().size()) continue;
 			if (sc == bestSC) {
 			    continue;
 			}
 			
             for (ExperimentResult er : sc.getJobs()) {
                 ExperimentResult apiER = api.getJob(er.getId());
                 if (limitByInstance.get(er.getInstanceId()) == null) continue;
                 er.setCPUTimeLimit(limitByInstance.get(er.getInstanceId()));
                 apiER.setCPUTimeLimit(limitByInstance.get(er.getInstanceId()));
                 if (er.getResultTime() > limitByInstance.get(er.getInstanceId()) && er.getResultCode().isCorrect()) {
                     pacc.log("Setting time limit exceeded to job " + er.getId() + ".");
                     er.setStatus(StatusCode.TIMELIMIT);
                     apiER.setStatus(StatusCode.TIMELIMIT);
                     er.setResultCode(ResultCode.UNKNOWN);
                     apiER.setResultCode(ResultCode.UNKNOWN);
                 }
             }
 
 			int comp = compareTo(sc, bestSC);
 			if (!stopEvalSolverConfigIds.contains(sc.getIdSolverConfiguration()) && comp >= 0) {
 				if (sc.getJobCount() == bestSC.getJobCount()) {
 					sc.setFinished(true);
 					// all jobs from bestSC computed and won against
 					// best:
 					if (comp > 0) {
 					    challengers.remove(sc);
 						bestSC = sc;
 						sc.setIncumbentNumber(incumbentNumber++);
 						pacc.log("new incumbent: " + sc.getIdSolverConfiguration() + ":" + pacc.getWallTime() + ":" + pacc.getCumulatedCPUTime() + ":" + sc.getCost());
 						pacc.log("i " + pacc.getWallTime() + "," + sc.getCost() + ",n.A. ," + sc.getIdSolverConfiguration() + ",n.A. ," + sc.getParameterConfiguration().toString());
 						pacc.validateIncumbent(bestSC);
 					} else {
 					    pacc.log("c Configuration " + sc.getIdSolverConfiguration() + " tied with incumbent");
 					}
 				} else {
 				    int generated = 0;
 				    if (clusterSizeExpansion){
 					    if (aggressiveJobSelection) {
 					        generated = pacc.addRandomJobAggressive(course.getK(), sc, bestSC, sc.getJobCount());
 					    } else {
 					        generated = pacc.addRandomJob(course.getK(), sc, bestSC, sc.getJobCount());
 					    }
 				    }
 				    else{
 				    	if (aggressiveJobSelection) {
 					        generated = pacc.addRandomJobAggressive(sc.getJobCount(), sc, bestSC, sc.getJobCount());
 					    } else {
 					        generated = pacc.addRandomJob(sc.getJobCount(), sc, bestSC, sc.getJobCount());
 					    }
 				    }
 				    	
 				    if (generated > 0) {
 				        pacc.log("c Generated " + generated + " jobs for solver config id " + sc.getIdSolverConfiguration());
 				    }
 				    sc.setNameRacing(" Racing ");
 					pacc.addSolverConfigurationToListNewSC(sc);
 				}
 			} else {// lost against best on part of the actual (or should not be
 				// evaluated anymore)
 				// parcours:
 				sc.setNameRacing("Looser");
 				stopEvalSolverConfigIds.remove(sc.getIdSolverConfiguration());
 
 				challengers.remove(sc);
 				sc.setFinished(true);
 				if (parameters.isDeleteSolverConfigs())
 					api.removeSolverConfig(sc.getIdSolverConfiguration());
 				pacc.log("d Solver config " + sc.getIdSolverConfiguration() + " with cost " + sc.getCost() + " lost against best solver config on "
 						+ sc.getJobCount() + " runs.");
 				//api.updateSolverConfigurationName(sc.getIdSolverConfiguration(), "* " + sc.getName()); wird eh ueberschrieben!
 
 				numSCs += 1;
				if (numSCs > curThreshold) {
					if (bestSC.getJobCount() < parameters.getMaxParcoursExpansionFactor() * num_instances) {
						int expansionSize = 1;
 						if (useClusterCourse) {
							if (bestSC.getJobCount() < completeCourse.size()) {
								if (clusterSizeExpansion) {
									expansionSize = Math.min(course.getK(), completeCourse.size() - bestSC.getJobCount());
									for (int i = 0; i < expansionSize; i++)
										pacc.addJob(bestSC, completeCourse.get(bestSC.getJobCount()).seed, completeCourse.get(bestSC.getJobCount()).instanceId, bestSC.getJobCount());
								} else {
 									pacc.addJob(bestSC, completeCourse.get(bestSC.getJobCount()).seed, completeCourse.get(bestSC.getJobCount()).instanceId, bestSC.getJobCount());
								}
 							} else {
 							}
 						} else {
 							pacc.expandParcoursSC(bestSC, expansionSize);
 						}
 						pacc.addSolverConfigurationToListNewSC(bestSC);
 						curThreshold += increaseIncumbentRunsEvery;
 						pacc.log("c Expanding parcours of best solver config " + bestSC.getIdSolverConfiguration() + " by " + expansionSize);
					} else {
 						pacc.log("c Incumbent reached maximum number of evaluations. No more jobs are generated for it.");
					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void solverConfigurationsCreated(List<SolverConfiguration> scs) throws Exception {
 		if (scs.isEmpty())
 			return;
 		
 		if (bestSC == null) {
 			initBestSC(scs.get(0));
 			scs.remove(0);
 		}
 		        
         // First, check if we can update the incumbent
         this.solverConfigurationsFinished(new LinkedList<SolverConfiguration>(challengers));
 		
 		for (SolverConfiguration sc : scs) {
             /*if (initialDesignMode) {
                 if (useClusterCourse) {
                     for (int i = 0; i < parameters.getInitialDefaultParcoursLength(); i++) {
                         pacc.addJob(sc, completeCourse.get(sc.getJobCount()).seed,
                                 completeCourse.get(sc.getJobCount()).instanceId, sc.getJobCount());
                     }
                 } else {
                     pacc.expandParcoursSC(sc, parameters.getInitialDefaultParcoursLength());
                 }
 
             } else {*/
 			sc.setNameRacing(" Racing ");
 			if (clusterSizeExpansion){
                 if (aggressiveJobSelection) {
                     pacc.addRandomJobAggressive(course.getK(), sc, bestSC, sc.getJobCount());
                 } else {
                     pacc.addRandomJob(course.getK(), sc, bestSC, sc.getJobCount());
                 }
 			}
 			else{
 				if (aggressiveJobSelection) {
                     pacc.addRandomJobAggressive(parameters.getMinRuns(), sc, bestSC, sc.getJobCount());
                 } else {
                     pacc.addRandomJob(parameters.getMinRuns(), sc, bestSC, sc.getJobCount());
                 }
 			}
             //}
 			pacc.addSolverConfigurationToListNewSC(sc);
 		}
 		
 		//if (!initialDesignMode) {
     	/*	for (int i = 0; i < scs.size(); i++) {
     		    numSCs += 1;
     	        if (numSCs > curThreshold && bestSC.getJobCount() < parameters.getMaxParcoursExpansionFactor() * num_instances) {
     	            pacc.log("c Expanding parcours of best solver config " + bestSC.getIdSolverConfiguration() + " by 1");
     	            if (useClusterCourse) {
     	                if (bestSC.getJobCount() < completeCourse.size()) {
                             pacc.addJob(bestSC, completeCourse.get(bestSC.getJobCount()).seed,
                                     completeCourse.get(bestSC.getJobCount()).instanceId, bestSC.getJobCount());
     	                } else {
     	                    pacc.log("c Incumbent reached maximum number of evaluations. No more jobs are generated for it.");
     	                }
     	            } else {
     	                pacc.expandParcoursSC(bestSC, 1);
     	            }
     	            pacc.addSolverConfigurationToListNewSC(bestSC);
     	            curThreshold += increaseIncumbentRunsEvery;
     	        }
     		}*/
 		//}
 
 		challengers.addAll(scs);
 	}
 
 	@Override
 	public int computeOptimalExpansion(int coreCount, int jobs, int listNewSCSize) {
 		if (coreCount < parameters.getMinCPUCount() || coreCount > parameters.getMaxCPUCount()) {
 			pacc.log("w Warning: Current core count is " + coreCount);
 		}
 		
 		if (parameters.getJobCPUTimeLimit() > 10) {
 		    if (Math.max(0, coreCount - jobs) > 0) {
 		        pacc.log("c [DefaultSMBO] coreCount: " + coreCount + ", Jobs to finish: " + jobs);
 		    }
 		    return Math.max(0, coreCount - jobs);
 		} else {
 		    return Math.max(0, 2 * coreCount - jobs);
 		}
 
 		/*if (challengers.size() > 0) {
 		    return 0;
 		}
 		else {
 		    pacc.log("c Requesting " + coreCount + " configurations from search");
 		    return coreCount;
 		}*/
 		
 		/*int min_sc = (Math.max(Math.round(4.f * coreCount), 8) - jobs) / parameters.getMinRuns();
 		if (min_sc > 0) {
 			res = (Math.max(Math.round(6.f * coreCount), 8) - jobs) / parameters.getMinRuns();
 		}
 		if (listNewSCSize == 0 && res == 0) {
 			res = 1;
 		}
 		return res;*/
 	}
 
 	@Override
 	public List<String> getParameters() {
 		List<String> p = new LinkedList<String>();
         p.add("% --- Default_SMBO parameters ---");
         p.add("DefaultSMBO_adaptiveCapping = " + (adaptiveCapping ? 1 : 0) + " % (Use adaptive capping mechanism)");
         p.add("DefaultSMBO_increaseIncumbentRunsEvery = " + increaseIncumbentRunsEvery + " % (How many challengers does the incumbent have to beat to gain additional runs)");
         p.add("DefaultSMBO_aggressiveJobSelection = " + (aggressiveJobSelection ? 1 : 0) + " % (Challengers should start on instances where the best configuration did not time out)");
         p.add("DefaultSMBO_featureFolder = " + featureFolder + " % (Instance feature computation folder containing a features.propertiers file)");
         p.add("DefaultSMBO_featureCacheFolder = " + featureCacheFolder + " % (Temporary folder used for caching instance features)");
         p.add("DefaultSMBO_useClusterCourse = " + (useClusterCourse ? 1 : 0) + " % (Cluster instances using instance properties for improved handling of heterogenous instance sets)");
         p.add("DefaultSMBO_slackFactor = " + slackFactor + " % (Slack factor used with adaptive capping (new timeout = slackFactor * best known time))");
         p.add("DefaultSMBO_adaptiveCapping = " + (adaptiveCapping ? 1 : 0) + " % (Lower time limit on instances according to the results of the best configuration)");
         p.add("DefaultSMBO_clusterSizeExpansion = " + clusterSizeExpansion + " % (If the cluster course is used, give the incumbent configuration k additional runs instead of one (k = no. of clusters))");
 		return p;
 	}
 
 	@Override
 	public List<SolverConfiguration> getBestSolverConfigurations() {
 		List<SolverConfiguration> res = new LinkedList<SolverConfiguration>();
 		if (bestSC != null) {
 			res.add(bestSC);
 		}
 		return res;
 	}
 
 	@Override
 	public void stopEvaluation(List<SolverConfiguration> scs) throws Exception {
 		for (SolverConfiguration sc : scs) {
 			stopEvalSolverConfigIds.add(sc.getIdSolverConfiguration());
 		}
 	}
 
 	@Override
 	public void raceFinished() {
 		// TODO Auto-generated method stub
 		
 	}
 
     @Override
     public void jobsFinished(List<ExperimentResult> result) throws Exception {
         // adapt instance specific limits
         if (adaptiveCapping && ExperimentDAO.getById(parameters.getIdExperiment()).getDefaultCost().equals(Cost.resultTime)) {
             boolean anyIncumbentRunsFinished = false;
             for (ExperimentResult run: result) {
                 if (run.getSolverConfigId() == bestSC.getIdSolverConfiguration()) {
                     anyIncumbentRunsFinished = true;
                     break;
                 }
             }
             
             if (!anyIncumbentRunsFinished) return;
             
             for (Instance instance: api.getExperimentInstances(parameters.getIdExperiment())) {
                 double incumbentAvg = 0.0f;
                 int count = 0;
                 for (ExperimentResult run: bestSC.getFinishedJobs()) {
                     if (run.getInstanceId() == instance.getId()) {
                         incumbentAvg += parameters.getStatistics().getCostFunction().singleCost(run);
                         count++;
                     }
                 }
 
                 if (count > 0) {
                     incumbentAvg /= count;
                     int newLimit = Math.max(1, Math.min((int)Math.ceil(slackFactor * incumbentAvg), parameters.getJobCPUTimeLimit()));
                     if (limitByInstance.get(instance.getId()) != null && limitByInstance.get(instance.getId()) == newLimit) {
                         // limit did not change
                         continue;
                     }
                     limitByInstance.put(instance.getId(), newLimit);
                     pacc.changeCPUTimeLimit(instance.getId(), newLimit, new LinkedList<SolverConfiguration>(challengers), false, false);
                 }
             }
         }
 
     }
 
 }
