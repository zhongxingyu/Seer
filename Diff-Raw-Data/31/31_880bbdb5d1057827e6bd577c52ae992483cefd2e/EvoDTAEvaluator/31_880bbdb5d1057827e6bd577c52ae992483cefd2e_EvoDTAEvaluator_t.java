 package com.aarribas1.evodta.ecj;
 
 import static java.util.Arrays.asList;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Scanner;
 
 import org.jppf.task.storage.DataProvider;
 
 import rinde.ecj.GPBaseNode;
 import rinde.ecj.GPEvaluator;
 import rinde.ecj.GPProgram;
 import rinde.ecj.GPProgramParser;
 
 import com.aarribas.dtasim.TrafficSimulator;
 import com.aarribas1.evodta.TrafficSwappingHeuristicGP;
 import com.aarribas1.evodta.TrafficSwappingHeuristicGP.GPStatus;
 import com.aarribas1.evodta.ecj.EvoDTAEvaluator.EvoDTAContext;
 import com.aarribas1.evodta.ecj.EvoDTAEvaluator.EvoDTATask;
 import rinde.jppf.ComputationTask;
 import ec.EvolutionState;
 import ec.gp.GPTree;
 
 
 /**
  * EvoDTAEvaluator is the class that contains the evaluation logic. Each individual will run its own EvoDTATask,
  * EvoDTATask contains the code to launch the simulator, run the simulator with different networks and compute
  * the fitness.
  * 
  * @author andresaan
  *
  */
 public class EvoDTAEvaluator extends GPEvaluator<EvoDTATask, EvoDTAResult, GPProgram<EvoDTAContext>> {
 
 	@Override
 	protected Collection<EvoDTATask> createComputationJobs(DataProvider dataProvider, GPTree[] trees,
 			EvolutionState state) {
 		final GPProgram<EvoDTAContext> prog = GPProgramParser
 				.convertToGPProgram((GPBaseNode<EvoDTAContext>) trees[0].child);
 		return asList(new EvoDTATask(prog));
 	}
 
 	@Override
 	protected int expectedNumberOfResultsPerGPIndividual(EvolutionState state) {
 		return 1;
 	}
 
 	public static class EvoDTATask extends ComputationTask<EvoDTAResult, GPProgram<EvoDTAContext>> {
 	
 		private float fitness;
 
 		public EvoDTATask(GPProgram<EvoDTAContext> p) {
 			super(p);
 			fitness = 0;
 
 		}
 
 		public void run() {
 			
 //		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 //		Calendar cal = Calendar.getInstance();
 //		System.out.println(dateFormat.format(cal.getTime()));
 //			
 			//System.out.println(Math.random());
 		
 			TrafficSimulator sim;
 			TrafficSwappingHeuristicGP heuristic;
 			float cumulativeFitness = 0;
 			
 			for(int network = 1;  network < 6; network++ ){
 				//simulate one network per iteration
 				switch(network){
				case 1: sim = new TrafficSimulator("../files/networks/net1.mat", 0.75, 0.0025, 50, TrafficSimulator.VERBOSITY.VERY_VERBOSE); break;
 				case 2: sim = new TrafficSimulator("../files/networks/net2.mat", 0.75, 0.0025, 50, TrafficSimulator.VERBOSITY.SILENT); break;
 				case 3: sim = new TrafficSimulator("../files/networks/net3.mat", 0.75, 0.0025, 50, TrafficSimulator.VERBOSITY.SILENT); break;
 				case 4: sim = new TrafficSimulator("../files/networks/net4.mat", 0.75, 0.0025, 50, TrafficSimulator.VERBOSITY.SILENT); break;
 				case 5: sim = new TrafficSimulator("../files/networks/net5.mat", 0.75, 0.0025, 50, TrafficSimulator.VERBOSITY.SILENT); break;
 				default: sim = new TrafficSimulator("../files/networks/net1.mat", 0.75, 0.0025, 50, TrafficSimulator.VERBOSITY.SILENT); break;
 				}
 				
 				heuristic = new TrafficSwappingHeuristicGP();
 				heuristic.setupGenParams(sim, this);
 				switch(network){
 				case 1: 
 				case 2:
 				case 3:
 				case 4: sim.runDTA(500, 0.00015, heuristic); break;
				case 5: sim.runDTA(1500, 0.0015, heuristic); break; //more iterations and a lower convergence objective -- network is far more demanding
 				default: sim.runDTA(500, 0.00015, heuristic);
 				}
 				float tfitness = computeFitness(heuristic, sim); 
 				
 				if(tfitness == Float.MAX_VALUE ){
 					cumulativeFitness = tfitness;
 					break;
 				}
 				else{
					//sim.displayRouteFractionPerRouteInterval();
					//sim.displayRouteTravelTimesPerRoute();
					
					System.out.println("net: " + network + " final gap: " + sim.getGap() + " iterations: " + sim.getIteration());
 					if(network == 5){
 						cumulativeFitness += tfitness*3;
 					}
 					else{
 						cumulativeFitness += tfitness;
 					}
 				}
 			}
		
 			
 			//temporal fitness
 			if (cumulativeFitness == Float.MAX_VALUE){
 				fitness = Float.MAX_VALUE;
 			}
 			else{
 				
 				fitness = cumulativeFitness / 7.0f;
 			}
 			setResult(new EvoDTAResult(fitness, taskData.getId()));
 //			dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 //			cal = Calendar.getInstance();
 //			System.out.println(dateFormat.format(cal.getTime()));
 		}
 
 		public float computeFitness(TrafficSwappingHeuristicGP heuristic, TrafficSimulator sim ){
 			
 			//if aborted set the max fitness
 			if(heuristic.getGpStatus() != GPStatus.GP_STATUS_NORMAL){
 				
 				return Float.MAX_VALUE;
 			} 
 			else{
 				if (Double.isInfinite(sim.getGap()) || Double.isNaN(sim.getGap())) {
 					return Float.MAX_VALUE;
 				}
 				else{
 					
 					
 					//if the progression was negative assign max fitness
 					if(sim.getGap() >= heuristic.getFirstGap()){
 						return Float.MAX_VALUE;
 					}
 				//	System.out.println(sim.getIteration() + " " + heuristic.getFirstGap() +  " " + sim.getGap());
 					//otherwise compute the fitness as the difference between the given progressino and best possible progression.		
 					return (float) ((sim.getIteration() / (heuristic.getFirstGap() - sim.getGap())  - (2/heuristic.getFirstGap())) / (2/heuristic.getFirstGap())) ;
 				}
 			}
 
 		}
 	}
 
 	public static class EvoDTAContext {
 		public final double cDemand;
 		public final double oDemand;
 		public final double invIteration;
 		public final double normCostDiff;
 		public final double cumuDelta;
 
 		public EvoDTAContext(double cDemand,
 				double oDemand,
 				double invIteration,
 				double normCostDiff,
 				double cumuDelta) {
 			this.cDemand = cDemand;
 			this.oDemand = oDemand;
 			this.invIteration = invIteration;
 			this.normCostDiff = normCostDiff;
 			this.cumuDelta = cumuDelta;
 		}
 	}
 }
