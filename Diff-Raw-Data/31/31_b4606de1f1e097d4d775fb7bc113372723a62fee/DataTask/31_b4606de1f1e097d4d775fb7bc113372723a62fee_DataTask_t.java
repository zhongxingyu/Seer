 
 
 package project6867;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ch.idsia.agents.Agent;
 import ch.idsia.benchmark.mario.engine.GlobalOptions;
 import ch.idsia.benchmark.tasks.LearningTask;
 import ch.idsia.tools.MarioAIOptions;
 import erekspeed.ActionWrapper;
 import erekspeed.CuckooSubAgent;
 
 
 
 public class DataTask extends LearningTask
 {
 
 private Map<BitSet, Map<ActionWrapper, List<DataFitness> > > dataMap = new HashMap<BitSet, Map<ActionWrapper, List<DataFitness> > >();
 
 private int maxEvals = 30000;
 private int refineEvals = 1000;
private int startRefine = 0;
 private boolean hasWon = false;
 private int curEvals = 0;
 
 public DataTask(MarioAIOptions marioAIOptions)
 {
    super(marioAIOptions);
 }
 
 /**
  * @param repetitionsOfSingleEpisode
  * @return boolean flag whether controller is disqualified or not
  */
 public boolean runSingleEpisode(final int repetitionsOfSingleEpisode)
 {
     long c = System.currentTimeMillis();
     for (int r = 0; r < repetitionsOfSingleEpisode; ++r)
     {
         this.reset();
         CuckooSubAgent myAgent = (CuckooSubAgent)agent;
         float prevFit = environment.getIntermediateEval().computeWeightedFitness();
         ArrayList<DataFitness> newPoints = new ArrayList<DataFitness>();
         while (!environment.isLevelFinished())
         {
             environment.tick();
             if (!GlobalOptions.isGameplayStopped)
             {
                 c = System.currentTimeMillis();
                 
                 agent.integrateObservation(environment);
                 BitSet data = myAgent.mergedObservationBit;
                 boolean[] action = agent.getAction();
                 ActionWrapper wrap = new ActionWrapper(action);
                 
                 agent.giveIntermediateReward(environment.getIntermediateReward());
                 
                 float intFit = environment.getIntermediateEval().computeWeightedFitness();
                 
                 DataFitness fit = new DataFitness(false, Math.signum(intFit-prevFit)*(float)Math.pow(intFit - prevFit, 2));
                 newPoints.add(fit);
                 prevFit = intFit;
                 recordData(data, wrap, fit); 
                 
                 
                 
                 if (System.currentTimeMillis() - c > COMPUTATION_TIME_BOUND)
                     return false;
 //                System.out.println("action = " + Arrays.toString(action));
 //            environment.setRecording(GlobalOptions.isRecording);
                 environment.performAction(action);
             }
         }
         environment.closeRecorder(); //recorder initialized in environment.reset
         environment.getEvaluationInfo().setTaskName(name);
         this.evaluationInfo = environment.getEvaluationInfo().clone();
        if(this.evaluationInfo.marioStatus == 1) {
        	if(!hasWon) startRefine = curEvals;
         	hasWon = true;
        }
     
         updateData(evaluationInfo.computeWeightedFitness(), newPoints);
     }
 
     return true;
 }
 
 public int evaluate(Agent agent)
 {
     if ((hasWon && curEvals > refineEvals) || curEvals > maxEvals)
         return 0;
     curEvals++;
     options.setAgent(agent);
     environment.reset(options);
     this.runSingleEpisode(1);
     return this.getEvaluationInfo().computeWeightedFitness();
 }
 
 void recordData(BitSet data, ActionWrapper wrap, DataFitness fit) {
 	Map<ActionWrapper, List<DataFitness> > actions;
 	if(dataMap.containsKey(data)) {
 		actions = dataMap.get(data);
 		//System.out.println("dup1" + Arrays.deepToString(actions.keySet().toArray()));
 	}
 	else {
 		Map<ActionWrapper, List<DataFitness> > tMap = new HashMap<ActionWrapper, List<DataFitness> >();
 		dataMap.put(data, tMap);
 		actions = tMap;
 		
 	}
 	if(actions.containsKey(wrap)) {
 		actions.get(wrap).add(fit);
 		//System.out.println("dup2 " + Arrays.deepToString(actions.get(wrap).toArray()));
 	}
 	else {
 		List<DataFitness> l = new ArrayList<DataFitness>();
 		l.add(fit);
 		actions.put(wrap, l);
 	}
 	
 }
 
 void updateData(float fitness, List<DataFitness> d) {
 	
 	for(DataFitness df : d) {
 		df.add(fitness);
 		df.hasVisited();
 	}
 //	for( BitSet d : dataMap.keySet()) {
 //		for( ActionWrapper act : dataMap.get(d).keySet() ) {
 //			for(DataFitness df : dataMap.get(d).get(act)){
 //				if(!df.hasVisited()) {
 //					df.add(fitness);
 //				}
 //			}
 //		}
 //	}
 }
 
 }
