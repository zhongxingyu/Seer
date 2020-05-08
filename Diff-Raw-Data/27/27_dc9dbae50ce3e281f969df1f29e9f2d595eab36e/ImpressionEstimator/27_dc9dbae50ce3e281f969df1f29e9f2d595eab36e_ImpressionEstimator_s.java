 package models.queryanalyzer.iep;
 
 import ilog.concert.IloException;
 import models.queryanalyzer.ds.QAInstance;
 import models.queryanalyzer.probsample.SampleProbability;
 
 import java.util.*;
 
 public class ImpressionEstimator implements AbstractImpressionEstimator {
 
    private boolean IE_DEBUG = true;
    QAInstance _instance;
    int MIN_PADDED_AGENT_ID = 100;
    
    public enum ObjFun {
       NONE,
       MINIMIZE_IMPS,
       MAXIMIZE_IMPS,
       MAXIMIZE_FINISH_SAME_TIME,
       MINIMIZE_SLOT_DIFF
    }
 
    private ObjFun objectiveFun = ObjFun.NONE;
 
   private boolean EXACT_AVGPOS = false;
    private int _numSamples = 10;
    private int _fractionalBranches = 2; //The number of branches will be double this(1 above and 1 below solved imps)
    SampleProbability _sampleP;
 
    private static int SAMPLING_FACTOR = 100;
    private static int MAX_PROBE_IMPRESSIONS = 1; //warning this must be greater than 0
    private int _samplingImpressions;
 
    private int _nodeId;
    private Map<Integer, IESolution> _solutions;
    private int _bestNode;
    private double _bestImprObjVal;
    private double _combinedObjectiveBound;
    private double _combinedObjectiveBoundWithSample;
 
    private int _advertisers;
    private int _slots;
    private double[] _trueAvgPos;
    private int _ourIndex;
    private int _ourImpressions;
    private int _imprUB;
    private int[] _agentImprUB;
    private int[] _agentImprLB;
    private boolean[] _fractionalAvgPos;
 
    private double[] _agentImpressionDistributionMean;
    private double[] _agentImpressionDistributionStdev;
    private boolean[] _agentSawSample; //true if the agent saw at least one sample.
    private boolean[] _agentIsPadded; //true if the agent is a dummy or "padded" agent.
    private int[] _agentIds;
    
    private double _startTime;
    private double _timeOut = 1; //in seconds
 
    public ImpressionEstimator(QAInstance inst) {
       _instance = inst;
       _advertisers = inst.getNumAdvetisers();
       _slots = inst.getNumSlots();
 
       //_trueAvgPos should contain exact average positions whenever possible.
       //If there is no exact average position, use sampled average position.
       _trueAvgPos = inst.getAvgPos().clone();
       for (int i = 0; i < _trueAvgPos.length; i++) {
          if (_trueAvgPos[i] == -1) {
             _trueAvgPos[i] = inst.getSampledAvgPos()[i];
          }
       }
 
       //Determine which agents saw at least one sample
       double[] sampledAvgPos = inst.getSampledAvgPos();
       _agentSawSample = new boolean[sampledAvgPos.length];
       for (int i = 0; i < sampledAvgPos.length; i++) {
          if (!Double.isNaN(sampledAvgPos[i])) {
             _agentSawSample[i] = true;
          }
       }
 
       _ourIndex = inst.getAgentIndex();
       _ourImpressions = inst.getImpressions();
       _imprUB = inst.getImpressionsUB();
 
       _agentImpressionDistributionMean = inst.getAgentImpressionDistributionMean();
       _agentImpressionDistributionStdev = inst.getAgentImpressionDistributionStdev();
 
       _agentIds = inst.getAgentIds();
       
       //Initially, none of the agents are padded
       //(This isn't really needed when we know the rankings of everybody)
       _agentIsPadded = new boolean[_advertisers];
 
       
       
       //Optionally pad agents. How the agents are padded will depend on whether or not
       //the ordering is known exactly.
       //TODO: This will get more complicated when we don't know the initial positions.
       // (We'll have to add Carleton's padding algorithm from QAInstance.)
       // (We'll also have to remove these padded agents before returning the IEResult.)
       if (inst.allInitialPositionsKnown()) {
          padAgentsWithKnownPositions(inst.getInitialPositionOrdering());
       } else {
     	  removeUnsampledAgents();
     	  padAgentsWithUnknownPositions();  
       }
 
       //TODO: IT IS POSSIBLE TO HAVE A WHOLE # AVG POSITION, BUT HAVE BEEN IN MORE THAN ONE SLOT...
       _fractionalAvgPos = new boolean[_advertisers];
       int wholeAvgPos = 0;
       for (int i = 0; i < _advertisers; i++) {
          _fractionalAvgPos[i] = ((_trueAvgPos[i] * 100000) % 100000) != 0;
          if (!_fractionalAvgPos[i]) {
             wholeAvgPos += 1;
          }
       }
 
       _samplingImpressions = Math.max(MAX_PROBE_IMPRESSIONS + 1, _imprUB / SAMPLING_FACTOR * wholeAvgPos * wholeAvgPos); //value must be at least 2, becouse it must be greater than MAX_PROBE_IMPRESSIONS
 
       assert _ourImpressions > 0;
 
       _agentImprLB = new int[_advertisers];
       _agentImprUB = new int[_advertisers];
 
       for (int i = 0; i < _advertisers; i++) {
          _agentImprLB[i] = 1;
          _agentImprUB[i] = inst.getImpressionsUB();
       }
 
       if (!_fractionalAvgPos[_ourIndex]) {
          _agentImprLB[_ourIndex] = _ourImpressions - _ourImpressions / 10;
          _agentImprUB[_ourIndex] = _ourImpressions + _ourImpressions / 10;
       }
 
       if (!EXACT_AVGPOS) {
          _sampleP = new SampleProbability();
       }
 
 
       if (IE_DEBUG) {
          System.out.println("IEDebug: avgPos=" + Arrays.toString(inst.getAvgPos()) + ", sampledAvgPos=" + Arrays.toString(inst.getSampledAvgPos()) + ", _trueAvgPos=" + Arrays.toString(_trueAvgPos) + ", _agentSawSample=" + Arrays.toString(_agentSawSample));
       }
 
    }
 
    
    private String allDataString() {
 	   StringBuffer sb = new StringBuffer();
 	   sb.append("sampImp=" + _samplingImpressions + "\n");
 	   sb.append("sol=" + _solutions + "\n");
 	   sb.append("advertisers=" + _advertisers + "\n");
 	   sb.append("slots=" + _slots + "\n");
 	   sb.append("trueavgPos=" + Arrays.toString(_trueAvgPos) + "\n");
 	   sb.append("ourIdx=" + _ourIndex + "\n");
 	   sb.append("ourImps=" + _ourImpressions + "\n");
 	   sb.append("impsUB=" + _imprUB + "\n");
 	   sb.append("agentImprUB=" + Arrays.toString(_agentImprUB) + "\n");
 	   sb.append("agentImprLB=" + Arrays.toString(_agentImprLB) + "\n");
 	   sb.append("fractionalAvgPos=" + Arrays.toString(_fractionalAvgPos) + "\n");
 	   sb.append("agentImprDistMean=" + Arrays.toString(_agentImpressionDistributionMean) + "\n");
 	   sb.append("agentImprDistStdev=" + Arrays.toString(_agentImpressionDistributionStdev) + "\n");
 	   sb.append("agentSawSample=" + Arrays.toString(_agentSawSample) + "\n");
 	   sb.append("agentIsPadded=" + Arrays.toString(_agentIsPadded) + "\n");
 	   sb.append("agentIds=" + Arrays.toString(_agentIds) + "\n");
 	   return sb.toString();
    }
    
    /**
     * This gets an IEResult that corresponds to the original QAInstance, before any 
     * unsampled agents were removed or any padded agents were added.
     * Any unsampled agent is given a NaN prediction.
     * 
     * @param result
     * @return
     */
    private IEResult reduceIEResult(IEResult result) {
 	   //FIXME: Add something here
 	   int[] originalIds = _instance.getAgentIds();
 	   
 	   int[] paddedImpressions = result.getSol();
 	   int[] paddedOrder = result.getOrder();
 	   int[] paddedSlotImpressions = result.getSlotImpressions();
 	   
 	   int[] reducedImpressions = new int[originalIds.length];
 	   Arrays.fill(reducedImpressions, -1);
 	   for (int i=0; i<originalIds.length; i++) {
 		   int originalId = originalIds[i];
 		   for (int j=0; j<_agentIds.length; j++) {
 			   if (originalId == _agentIds[j]) {
 				   reducedImpressions[i] = paddedImpressions[j];
 				   //TODO: What should we do with reduced order? [2, 0, 1]. 
 			   }
 		   }
 	   }
 	   
 	   //order [A, B, C] says that agent A was in the first slot, B was in the 2nd, etc.
 	   //If a padded agent was in some slot, change that value to -1.
 	   //In other words, we'll still acknowledge that there was SOME agent in that slot, but we don't know who.
 	   int[] reducedOrder = paddedOrder.clone();
 	   Arrays.fill(reducedOrder, -1);
 	   for (int i=0; i<paddedOrder.length; i++) {
 		   int paddedAgentIdx = paddedOrder[i];
 		   int paddedAgentId = _agentIds[paddedAgentIdx];
 		   
 		   //Find this agentID in the original instance
 		   for (int j=0; j<originalIds.length; j++) {
 			   if (paddedAgentId == originalIds[j]) {
 				   int originalIdx = j;
 				   reducedOrder[i] = originalIdx;
 			   }
 		   }
 	   }
 	   
 	   return new IEResult(result.getObj(), reducedImpressions, reducedOrder, paddedSlotImpressions);
    }
    
    
    /**
     * This removes any agents that have an either NaN or unknown average position.
     * In effect, it is removing opponent agents that weren't sampled
     */
    private void removeUnsampledAgents() {
 
 	   //Determine how many agents must be removed
 	   int numAgentsToRemove = 0;
 	   int ourIndexOffset = 0;
 	   for (int i=0; i<_advertisers; i++) {
 		   if (Double.isNaN(_trueAvgPos[i]) || _trueAvgPos[i] == -1) {
 			   numAgentsToRemove++;
 			   if (i < _ourIndex) ourIndexOffset++; //Adjust our agent's index accordingly
 		   }
 	   }
 	   
 	   //If nobody needs to be removed, just return now.
 	   if (numAgentsToRemove == 0) return;
 	   
 	   //Otherwise, remove anyone that didn't see a sample.
 	   int numAgents = _advertisers - numAgentsToRemove;
 	   int[] agentIds = new int[numAgents];
 	   double[] avgPos = new double[numAgents];
 	   boolean[] agentIsPadded = new boolean[numAgents];
 	   double[] agentImpressionDistributionMean = new double[numAgents];
 	   double[] agentImpressionDistributionStdev = new double[numAgents];
 	   boolean[] agentSawSample = new boolean[numAgents];
 	   int idx=0;
 	   for (int i=0; i<_advertisers; i++) {
 		   if (!Double.isNaN(_trueAvgPos[i]) && _trueAvgPos[i] != -1) {
 			   agentIds[idx] = _agentIds[i];
 			   avgPos[idx] = _trueAvgPos[i];
 			   agentIsPadded[idx] = _agentIsPadded[i];
 			   agentImpressionDistributionMean[idx] = _agentImpressionDistributionMean[i];
 			   agentImpressionDistributionStdev[idx] = _agentImpressionDistributionStdev[i];
 			   agentSawSample[idx] = _agentSawSample[i];
 			   idx++;
 		   }
 	   }
 	   _advertisers = numAgents;
 	   _agentIds = agentIds;
 	   _trueAvgPos = avgPos;
 	   _agentIsPadded = agentIsPadded;
 	   _agentImpressionDistributionMean = agentImpressionDistributionMean;
 	   _agentImpressionDistributionStdev = agentImpressionDistributionStdev;
 	   _agentSawSample = agentSawSample;
 	   _ourIndex = _ourIndex - ourIndexOffset;
    }
 
    
    private void padAgentsWithUnknownPositions() {
       //if (_considerPaddingAgents) {
       while (!feasibleOrder(QAInstance.getAvgPosOrder(_trueAvgPos))) {
          int[] apOrder = QAInstance.getAvgPosOrder(_trueAvgPos);
 
          int foundTooBigStart = 0;
          int foundTooBigStop = 0;
          for (int i = 0; i < _slots && i < _advertisers; i++) {
             if (_trueAvgPos[apOrder[i]] > i + 1) {
                foundTooBigStart = i + 1;
                foundTooBigStop = (int) Math.ceil(_trueAvgPos[apOrder[i]]);
                break;
             }
          }
 
          if (foundTooBigStart > 0) {
             addPaddingAgents(foundTooBigStart, foundTooBigStop);
          }
       }
       assert (feasibleOrder(QAInstance.getAvgPosOrder(_trueAvgPos))) : "addPaddingAgents broke did not work...";
       //  }
    }
 
    //pads the auction with "fake" advertisers so that the instance is feasible
    //Feasible means every agent starts in a position greater or equal to their avg pos
    //assumes the highest agent ID is 99
    private void addPaddingAgents(int startSlot, int stopSlot) {
       int oldAdvertisers = _advertisers;
       double[] oldAvgPos = _trueAvgPos;
       int[] oldAgentIds = _agentIds;
       boolean[] oldAgentIsPadded = _agentIsPadded;
       double[] oldAgentImpressionDistributionMean = _agentImpressionDistributionMean;
       double[] oldAgentImpressionDistributionStdev = _agentImpressionDistributionStdev;
       boolean[] oldAgentSawSample = _agentSawSample; //true if the agent saw at least one sample.
 
 
       int newAdvertisers = oldAdvertisers + stopSlot - startSlot;
       double[] newAvgPos = new double[newAdvertisers];
       int[] newAgentIds = new int[newAdvertisers];
       boolean[] newAgentIsPadded = new boolean[newAdvertisers];
       double[] newAgentImpressionDistributionMean = new double[newAdvertisers];
       double[] newAgentImpressionDistributionStdev = new double[newAdvertisers];
       boolean[] newAgentSawSample = new boolean[newAdvertisers];
 
       for (int i = 0; i < oldAdvertisers; i++) {
          newAvgPos[i] = oldAvgPos[i];
          newAgentIds[i] = oldAgentIds[i];
          newAgentIsPadded[i] = oldAgentIsPadded[i];
          newAgentImpressionDistributionMean[i] = oldAgentImpressionDistributionMean[i];
          newAgentImpressionDistributionStdev[i] = oldAgentImpressionDistributionStdev[i];
          newAgentSawSample[i] = oldAgentSawSample[i];
       }
 
       for (int i = 0; i < stopSlot - startSlot; i++) {
          newAvgPos[oldAdvertisers + i] = startSlot + i;
          newAgentIds[oldAdvertisers + i] = MIN_PADDED_AGENT_ID + i;
          newAgentIsPadded[oldAdvertisers + i] = true;
 
          //TODO: What prior impressions values do we want for padded advertisers?
          newAgentImpressionDistributionMean[oldAdvertisers + i] = -1;
          newAgentImpressionDistributionStdev[oldAdvertisers + i] = -1;
       }
 
       _advertisers = newAdvertisers;
       _trueAvgPos = newAvgPos;
       _agentIds = newAgentIds;
       _agentIsPadded = newAgentIsPadded;
       _agentImpressionDistributionMean = newAgentImpressionDistributionMean;
       _agentImpressionDistributionStdev = newAgentImpressionDistributionStdev;
       _agentSawSample = newAgentSawSample;
       
       System.out.println(
     		  "advertisers=" + _advertisers +
     		  ", trueAvgPos=" + Arrays.toString(_trueAvgPos) + 
     		  ", isPadded=" + Arrays.toString(_agentIsPadded) +
     		  ", sawSample=" + Arrays.toString(_agentSawSample) +
     		  ", impsMeanPrior=" + Arrays.toString(_agentImpressionDistributionMean) + 
     		  ", impsStdDevPrior=" + Arrays.toString(_agentImpressionDistributionStdev));
    }
 
 
    public double[] getApproximateAveragePositions() {
       return _trueAvgPos.clone();
    }
 
 
    private void padAgentsWithKnownPositions(int[] ordering) {
       //If any agents have a NaN sampled average position (and also a NaN unsampled average position),
       //give them a dummy average position equal to min(their starting position, numSlots).
       for (int i = 0; i < _trueAvgPos.length; i++) {
          if (Double.isNaN(_trueAvgPos[ordering[i]])) {
             _trueAvgPos[ordering[i]] = Math.min(i + 1, _slots);
          }
       }
    }
 
    public QAInstance getInstance() {
       return _instance;
    }
 
    public ObjectiveGoal getObjectiveGoal() {
       //At least with the old LDS, lower objective values are better. Change this if necessary.
       return ObjectiveGoal.MINIMIZE;
    }
 
    private boolean feasibleOrder(int[] order) {
       for (int i = 0; i < order.length; i++) {
          int startPos = Math.min(i + 1, _slots);
          if (startPos < _trueAvgPos[order[i]]) {
             return false;
          }
       }
       return true;
    }
 
    public IEResult search(int[] order) {
 	   //System.out.println("Search on order=" + Arrays.toString(order));
 	   //System.out.println(allDataString());
 	   
       if (!feasibleOrder(order)) {
          System.out.println("Not a feasible ordering: " + Arrays.toString(order));
          System.out.print("_trueAvgPos (sorted by order): " );
          for (int i = 0; i < order.length; i++) {
             System.out.print(_trueAvgPos[order[i]] + " ");
          }
          System.out.println();
          
          assert (false) : "should have been eliminated in LDS search";
          return null;
       }
 
       _nodeId = 0;
       _solutions = new HashMap<Integer, IESolution>();
       _combinedObjectiveBound = Double.MAX_VALUE;
       _combinedObjectiveBoundWithSample = Double.MAX_VALUE;
       _bestImprObjVal = Double.MAX_VALUE;
 
       int[] agentImpr = new int[_advertisers];
       for (int i = 0; i < _advertisers; i++) {
          agentImpr[i] = 0;
       }
       int[] slotImpr = new int[_slots];
       for (int i = 0; i < _slots; i++) {
          slotImpr[i] = 0;
       }
 
       _startTime = System.currentTimeMillis();
       checkImpressions(0, agentImpr, slotImpr, order);
 
       IESolution sol = _solutions.get(_bestNode);
       if (sol == null) {
     	  System.out.println("IE Solution is null");
     	  //System.exit(-1);
          return null;
       } else {
     	  IEResult result = new IEResult(_combinedObjectiveBound, sol._agentImpr, copyArray(order), sol._slotImpr); 
     	  IEResult reducedResult = reduceIEResult(result);
     	  System.out.println("Search completed for order " + Arrays.toString(order) + "\n" + result + "\nReduced " + reducedResult);
     	  return reducedResult;
       }
    }
 
    void checkImpressions(int currIndex, int[] agentImpr, int[] slotImpr, int[] order) {
       _nodeId++;
 
     // System.out.println("nodeId=" + _nodeId + ", idx=" + currIndex + ", imps=" + Arrays.toString(agentImpr));
     		  //+ ", slotImps=" + Arrays.toString(slotImpr) );
       
 //      double stop = System.currentTimeMillis();
 //      double elapsed = (stop - _startTime) / 1000.0;
 //      if (elapsed > _timeOut) {
 //         return;
 //      }
 
       if (slotImpr[0] > _imprUB) {
          return; //this is infeasible
       }
 
       if (currIndex >= _advertisers) {
          // We've come up with feasible impression estimates for all advertisers.
          // Compute objective value for this solution.
          assert (slotImpr[0] - _imprUB >= 0);
 
          int imprObjVal = Math.abs(agentImpr[_ourIndex] - _ourImpressions) + 1;
 
          if (imprObjVal > _bestImprObjVal) {
             return;
          }
 
          double probWaterfall = getImpressionModelObjective(agentImpr, order) + 1;
 
          double sampleProb;
          if (!EXACT_AVGPOS) {
             double avgPosDiff = 0.0;
             int[][] impsPerSlot = greedyAssign(_slots, agentImpr.length, order, agentImpr);
             for (int i = 0; i < order.length; i++) {
                int currAgent = order[i];
                if (!_agentIsPadded[currAgent]) {
                   double trueAvgPos = _trueAvgPos[currAgent];
                   double weightedImps = 0.0;
                   double imps = 0.0;
                   for (int j = 0; j < _slots; j++) {
                      weightedImps += (j + 1) * impsPerSlot[currAgent][j];
                      imps += impsPerSlot[currAgent][j];
                   }
                   double waterfallAvgPos = weightedImps / imps;
                   avgPosDiff += Math.abs(waterfallAvgPos - trueAvgPos);
                }
             }
             sampleProb = avgPosDiff + 1;
          } else {
             double avgPosDiff = 0.0;
             int[][] impsPerSlot = greedyAssign(_slots, agentImpr.length, order, agentImpr);
             for (int i = 0; i < order.length; i++) {
                int currAgent = order[i];
                double trueAvgPos = _trueAvgPos[currAgent];
                double weightedImps = 0.0;
                double imps = 0.0;
                for (int j = 0; j < _slots; j++) {
                   weightedImps += (j + 1) * impsPerSlot[currAgent][j];
                   imps += impsPerSlot[currAgent][j];
                }
                double waterfallAvgPos = weightedImps / imps;
                avgPosDiff += Math.abs(waterfallAvgPos - trueAvgPos);
             }
             sampleProb = avgPosDiff + 1;
          }
 
          double combinedObj = Double.MAX_VALUE;
          if (objectiveFun == ObjFun.NONE) {
             combinedObj = imprObjVal * probWaterfall * sampleProb;
          } else if (objectiveFun == ObjFun.MINIMIZE_IMPS) {
             combinedObj = imprObjVal * probWaterfall * sampleProb * slotImpr[0];
          } else if (objectiveFun == ObjFun.MAXIMIZE_IMPS) {
             combinedObj = imprObjVal * probWaterfall * sampleProb / slotImpr[0];
          } else if (objectiveFun == ObjFun.MAXIMIZE_FINISH_SAME_TIME) {
             int finishSameTime = 1;
             for (int i = 0; i < slotImpr.length - 1; i++) {
                if ((slotImpr[i] - slotImpr[i + 1]) == 0) {
                   finishSameTime++;
                } else {
                   break;
                }
             }
             combinedObj = imprObjVal * probWaterfall * sampleProb / finishSameTime;
          } else if (objectiveFun == ObjFun.MINIMIZE_SLOT_DIFF) {
             int slotDiff = 1 + slotImpr[0] * (slotImpr.length - 1);
             for (int i = 1; i < slotImpr.length; i++) {
                slotDiff -= slotImpr[i];
             }
             combinedObj = imprObjVal * probWaterfall * slotDiff * sampleProb;
          } else {
             //this should never happen
             new RuntimeException();
          }
 
          if (combinedObj <= _combinedObjectiveBound) {
 
             if (!EXACT_AVGPOS) {
                try {
                   double[] sampleAverages = _instance.getSampledAvgPos();
                   sampleProb = 1.0 - _sampleP.getProbabilityofSample(sampleAverages, order, agentImpr, _imprUB, _slots, _numSamples);
                } catch (IloException e) {
                   e.printStackTrace();
                   throw new RuntimeException("CPLEX Failed to Load");
                }
 
                double combinedObjectiveBoundWithSample = _combinedObjectiveBound * sampleProb;
                if (combinedObjectiveBoundWithSample < _combinedObjectiveBoundWithSample) {
                   _combinedObjectiveBoundWithSample = combinedObjectiveBoundWithSample;
                   _combinedObjectiveBound = combinedObj;
                   _bestImprObjVal = imprObjVal;
                   _solutions.put(_nodeId, new IESolution(agentImpr, slotImpr));
                   _bestNode = _nodeId;
                }
             } else {
                _combinedObjectiveBound = combinedObj;
                _bestImprObjVal = imprObjVal;
                _solutions.put(_nodeId, new IESolution(agentImpr, slotImpr));
                _bestNode = _nodeId;
             }
          }
 
          //System.out.println("combinedObjBound=" + _combinedObjectiveBound + ", bestImprObj=" + _bestImprObjVal + ", bestNode=" + _bestNode);
 
          return;
       }
 
       int currAgent = order[currIndex];
 
       int bestImpr = calcMinImpressions(slotImpr, currIndex, _trueAvgPos[currAgent]);
 
       if (bestImpr > 0) {
 
          // Some #imps for this agent was successfully found.
          // If "this agent" is us, calculate how far the computed #imps is to our actual #imps.
          //   If prediction is not close enough, return (and possibly search with some new #imps estimate for opponents)
          // Go on to the next agent (assuming the #imps just found for this agent is true)
 
          if (currAgent == _ourIndex) {
             int imprObjVal = Math.abs(bestImpr - _ourImpressions);
             if (imprObjVal > _bestImprObjVal) {
                return;
             }
          }
 
          LinkedList<Integer> branches = new LinkedList<Integer>();
          branches.add(bestImpr);
 
          if (!EXACT_AVGPOS && _fractionalBranches > 0) {
             int maxImpr = _agentImprUB[currAgent];
             LinkedList<Integer> upBranches = new LinkedList<Integer>();
             for (int i = bestImpr + 1; i <= maxImpr; i++) {
                if (i % _samplingImpressions == 0 || i == maxImpr) {
                   upBranches.add(i);
                   if (upBranches.size() >= _fractionalBranches) {
                      break;
                   }
                }
             }
 
 
             int minImpr = _agentImprLB[currAgent];
             LinkedList<Integer> downBranches = new LinkedList<Integer>();
             for (int i = bestImpr - 1; i >= minImpr; i--) {
                if (i % _samplingImpressions == 0 || i == minImpr) {
                   downBranches.add(i);
                   if (downBranches.size() >= _fractionalBranches) {
                      break;
                   }
                }
             }
 
 
             for (int i = 0; i < Math.max(upBranches.size(), downBranches.size()); i++) {
                if (i < upBranches.size()) {
                   branches.add(upBranches.get(i));
                }
                if (i < downBranches.size()) {
                   branches.add(downBranches.get(i));
                }
             }
          }
 
 
          Integer impPred = (int) _agentImpressionDistributionMean[currAgent];
          if (impPred >= 0) {
             if (!branches.contains(impPred)) {
                branches.addFirst(impPred);
                ;
             }
          }
 
 //         if (currAgent == _ourIndex) {
 //            if (!branches.contains(_ourImpressions)) {
 //               branches.addFirst(_ourImpressions);
 //            }
 //         }
 
          for (Integer branch : branches) {
             int[] agentImprCopy = copyArray(agentImpr);
             agentImprCopy[currAgent] = branch;
             int[] newSlotImpr = fillSlots(slotImpr, branch);
 
             checkImpressions(currIndex + 1, agentImprCopy, newSlotImpr, order);
          }
       } else {
 
          // BestImpr <= 0, i.e. avgPos is an integer for this agent (and thus #imps is not well specified).
          // Iterate through different possible #imps.
          // Iterate increasing/decreasing depending on whether or not this agent is in the 1st position.
 
          int firstSlot = Math.min(currIndex + 1, _slots);
          int maxImpr = (firstSlot == 1) ? _agentImprUB[currAgent] : slotImpr[firstSlot - 2];
 
          LinkedList<Integer> branches = new LinkedList<Integer>();
          if (firstSlot == 1) {
             for (int i = _agentImprLB[currAgent]; i <= maxImpr; i++) {
                if (i % _samplingImpressions == 0 || i == maxImpr || i <= _agentImprLB[currAgent] + MAX_PROBE_IMPRESSIONS) {
                   branches.add(i);
                }
             }
          } else {
             for (int i = maxImpr; i >= _agentImprLB[currAgent]; i--) {
                if (i % _samplingImpressions == 0 || i == maxImpr || i <= _agentImprLB[currAgent] + MAX_PROBE_IMPRESSIONS) {
                   branches.add(i);
                }
             }
          }
 
          Integer impPred = (int) _agentImpressionDistributionMean[currAgent];
          if (impPred >= 0) {
             if (branches.contains(impPred)) {
                branches.remove(impPred);
             }
             branches.addFirst(impPred);
          }
 
          if (currAgent == _ourIndex) {
             if (!branches.contains(_ourImpressions)) {
                branches.addFirst(_ourImpressions);
             }
          }
 
          for (Integer branch : branches) {
             int[] agentImprCopy = copyArray(agentImpr);
             agentImprCopy[currAgent] = branch;
             int[] newSlotImpr = fillSlots(slotImpr, branch);
 
             checkImpressions(currIndex + 1, agentImprCopy, newSlotImpr, order);
          }
       }
    }
 
    private double getImpressionModelObjective(int[] agentImpr, int[] order) {
       return getImpressionModelObjective(agentImpr, order, order.length);
    }
 
    private double getImpressionModelObjective(int[] agentImpr, int[] order, int numSlotsFilled) {
       double obj = 1.0;
       numSlotsFilled = Math.min(numSlotsFilled, agentImpr.length);
       for (int i = 0; i < numSlotsFilled; i++) {
          int currAgent = order[i];
          double mean = _agentImpressionDistributionMean[currAgent];
          double stdDev = _agentImpressionDistributionStdev[currAgent];
          if (mean != -1 && stdDev != -1) {
             double imps = agentImpr[currAgent];
             double prob = gaussianPDF(imps, mean, stdDev);
             obj *= prob;
             //System.out.println("currAgent=" + currAgent + ", gaussianPDF(imps=" + imps +", mean="+ mean + ", stdDev=" + stdDev + ") = " + prob);
          }
       }
 
 //      if (obj == 1.0) {
 //         //This means we didn't place anyone with a prediction yet
 //         //so return a bad objective
 //         return 1.0;
 //      } else {
 //         return (1.0 - obj);
 //      }
       return obj;
    }
 
    private double gaussianPDF(double x, double mean, double sigma) {
       double diff = x - mean;
       double sigma2 = sigma * sigma;
       return Math.abs(.5 * Math.log(2.0 * Math.PI * sigma2) + (diff * diff) / (2.0 * sigma2));
 //      return (diff * diff) / (2.0 * sigma2);
    }
 
    private int calcMinImpressions(int[] slotImpr, int currIndex, double trueAvgPos) {
       int firstSlot = currIndex + 1;
       if (currIndex + 1 > _slots) {
          //Calculate the number of people who simultaneously dropped if any
          HashSet<Integer> duplicateMap = new HashSet<Integer>();
          for (int i = 0; i < slotImpr.length - 1; i++) {
             for (int j = i + 1; j < slotImpr.length; j++) {
                if (slotImpr[i] == slotImpr[j]) {
                   duplicateMap.add(slotImpr[i]);
                }
             }
          }
 
          //Duplicates of the # of imps seen in slot 1 do not indicate simultaneous dropouts
          if (duplicateMap.contains(slotImpr[0])) {
             duplicateMap.remove(slotImpr[0]);
          }
 
          //Make sure the dupes have happened
          ArrayList<Integer> toRemoveList = new ArrayList<Integer>();
          for (Integer dupe : duplicateMap) {
             if (dupe > slotImpr[slotImpr.length - (currIndex + 1 - _slots)]) {
                toRemoveList.add(dupe);
             }
          }
 
          for (Integer toRemove : toRemoveList) {
             duplicateMap.remove(toRemove);
          }
 
          if (duplicateMap.size() > 0) {
             int dupeCount = 0;
             for (Integer dupe : duplicateMap) {
                for (int i = 0; i < slotImpr.length; i++) {
                   if (slotImpr[i] == dupe) {
                      dupeCount++;
                   }
                }
             }
 
             firstSlot = _slots - dupeCount + 1;
          } else {
             firstSlot = _slots;
          }
       }
 
       double approxAvgPos = firstSlot;
       if (approxAvgPos == trueAvgPos) {
          return -1;
       }
 
       int fullSlots = firstSlot;
       for (int i = firstSlot; i > 1; i--) {
          int impTmp = 0;
          int wImpTmp = 0;
          for (int j = firstSlot; j >= i; j--) {
             impTmp += slotImpr[j - 2] - slotImpr[j - 1];
             wImpTmp += j * (slotImpr[j - 2] - slotImpr[j - 1]);
          }
          approxAvgPos = wImpTmp / (double) impTmp;
 
          if (approxAvgPos < trueAvgPos) {
             break;
          }
 
          fullSlots = i;
       }
       int finalSlot = fullSlots - 1;
       int fullSlotImp = 0;
       int wImp = 0;
 
       for (int i = fullSlots; i <= firstSlot; i++) {
          fullSlotImp += slotImpr[i - 2] - slotImpr[i - 1];
          wImp += i * (slotImpr[i - 2] - slotImpr[i - 1]);
       }
 
       if (fullSlotImp <= 0) {
          //TODO this is bad and it happens... so this is an ugly hack to fix it,
          fullSlotImp = 1;
          wImp = firstSlot;
       }
 
       double finalSlotImpFloat = (wImp - fullSlotImp * trueAvgPos) / (trueAvgPos - finalSlot);
       int finalSlotImp = (int) (finalSlotImpFloat + .5);
 
       return fullSlotImp + finalSlotImp;
    }
 
    private int[] fillSlots(int[] slotImpr, int newImpr) {
       int[] newSlotImpr = copyArray(slotImpr);
 
       int firstSlot = 0;
       while (firstSlot < slotImpr.length - 1) {
          if (slotImpr[firstSlot] > 0) {
             firstSlot++;
          } else {
             break;
          }
       }
 
       int remainingImp = newImpr;
       for (int s = firstSlot; s >= 0; s--) {
          if (s == 0) {
             newSlotImpr[s] += remainingImp;
          } else {
             int r = slotImpr[s - 1] - slotImpr[s];
             assert (r >= 0);
             if (r < remainingImp) {
                remainingImp -= r;
                newSlotImpr[s] += r;
             } else {
                newSlotImpr[s] += remainingImp;
                break;
             }
          }
       }
 
       return newSlotImpr;
    }
 
    private int[] copyArray(int[] arr) {
       int[] newArr = new int[arr.length];
       for (int i = 0; i < arr.length; i++) {
          newArr[i] = arr[i];
       }
       return newArr;
    }
 
    /*
   Function input
   number of slots: int slots
 
   number of agents: int agents
 
   order of agents: int[]
   example: order = {1, 6, 0, 4, 3, 5, 2} means agent 1 was 1st, agent 6 2nd, 0 3rd, 4 4th, 3 5th, 5 6th, 2 7th
   NOTE: these agents are zero numbered 0 is first... other note agents that are not in the "auction" are
   ommitted so there might be less than 8 agents but that means the numbering must go up to the last agents
   number -1 so if there are 6 agents in the auction the ordering numbers are 0...5
 
   impressions: int[] impressions
   example: impressions  = {294,22, 8, 294,294,272,286} agent 0 (not the highest slot) has 294 impressions agent 1 22... agent 6 286 impressions
   NOTE: same as order of agents they only reflect the agents in the auction
 
   Function output
   This is a matrix where one direction is for each agent and the other direction is for the slot.
   The matrix represents is the number of impressions observed at that slot for each of the agents.
   *
   * -gnthomps
    */
    public int[][] greedyAssign(int slots, int agents, int[] order, int[] impressions) {
       int[][] impressionsBySlot = new int[agents][slots];
 
       int[] slotStart = new int[slots];
       int a;
 
       for (int i = 0; i < agents; ++i) {
          a = order[i];
          //System.out.println(a);
          int remainingImp = impressions[a];
          //System.out.println("remaining impressions "+ impressions[a]);
          for (int s = Math.min(i + 1, slots) - 1; s >= 0; --s) {
             if (s == 0) {
                impressionsBySlot[a][0] = remainingImp;
                slotStart[0] += remainingImp;
             } else {
 
                int r = slotStart[s - 1] - slotStart[s];
                //System.out.println("agent " +a + " r = "+(slotStart[s-1] - slotStart[s]));
                assert (r >= 0);
                if (r < remainingImp) {
                   remainingImp -= r;
                   impressionsBySlot[a][s] = r;
                   slotStart[s] += r;
                } else {
                   impressionsBySlot[a][s] = remainingImp;
                   slotStart[s] += remainingImp;
                   break;
                }
             }
          }
       }
       return impressionsBySlot;
    }
 
    public String getName() {
       return "CJC";
    }
 
    private class IESolution {
       public int[] _agentImpr;
       public int[] _slotImpr;
 
       public IESolution(int[] agentImpr, int[] slotImpr) {
          _agentImpr = agentImpr;
          _slotImpr = slotImpr;
       }
    }
    
    
    
    public static void main(String[] args) {
 
      for (int ourAgentIdx = 1; ourAgentIdx <= 1; ourAgentIdx++) {
         //These aren't actually used; everything is -1 except the current agentIdx
         double[] I_aFull = {6, 1009, 1057, 268};
         double[] mu_aFull = {2.0, 1.0, 2.2138126773888365, 2.0223880597014925};
 
         //Get priors on impressions
         double[] agentImpressionDistributionMean = {-1, -1, -1, -1};
         double[] agentImpressionDistributionStdev = {-1, -1, -1, -1};
 
         //Get observed exact average positions (we only see one)
         double[] mu_a = new double[mu_aFull.length];
         Arrays.fill(mu_a, -1);
         mu_a[ourAgentIdx] = mu_aFull[ourAgentIdx];
 
         double[] I_aPromoted = {-1, -1, -1};
         boolean[] isKnownPromotionEligible = {false, false, false};
         double[] knownSampledMu_a = {Double.NaN, 1.0, 2.3, 2.0};
         int numSlots = 5;
         int numPromotedSlots = 0;
 
         int ourImpressions = (int) I_aFull[ourAgentIdx];
         int ourPromotedImpressions = (int) I_aPromoted[ourAgentIdx];
         boolean ourPromotionKnownAllowed = isKnownPromotionEligible[ourAgentIdx];
         int impressionsUB = 1500;
         int numAgents = mu_a.length;
 
         //Did we hit our budget? (added constraint if we didn't)
         boolean hitOurBudget = true;			
         int[] predictedOrder = {1, 0, 3, 2};
         
         //Give arbitrary agent IDs
         int[] agentIds = new int[numAgents];
         for (int i = 0; i < agentIds.length; i++) {
            agentIds[i] = -(i + 1);
         }
 
         QAInstance carletonInst = new QAInstance(numSlots, numPromotedSlots, numAgents, mu_a, knownSampledMu_a, agentIds, ourAgentIdx, ourImpressions, ourPromotedImpressions, impressionsUB, true, ourPromotionKnownAllowed, hitOurBudget, agentImpressionDistributionMean, agentImpressionDistributionStdev, true, predictedOrder);
         ImpressionEstimator carletonImpressionEstimator = new ImpressionEstimator(carletonInst);
 
         double[] cPos = carletonImpressionEstimator.getApproximateAveragePositions();
         int[] cOrder = predictedOrder; //QAInstance.getAvgPosOrder(cPos);
         //int[] cOrder = {1, 2, 0};
         System.out.println("cPos: " + Arrays.toString(cPos) + ", cOrder: " + Arrays.toString(cOrder));
         IEResult carletonResult = carletonImpressionEstimator.search(cOrder);
 
         System.out.println("ourAgentIdx=" + ourAgentIdx);
         System.out.println("  Carleton: " + carletonResult + "\tactual=" + Arrays.toString(I_aFull));
      }
   }
 
    
    
    
 }
