 package algorithms;
 
 import district.*;
 import graph.Graph;
 import graph.Tuple;
 
 import java.util.Random;
 
 //Runs the spring embedding algorithm in combination with simulated annealing
 public class SimulatedAnnealing {
 	
 	//Private vars:
 	private Graph optimalplan;
 	private Pair startTValues;
 	private Graph currentplan;
 	private Graph nextplan;
 	private SpringEmbedding springembedding;
 	private boolean isNextTrialAccepted;
 	private int iterator;
 	private Random random;
 	
 	//Constructor:
 	public SimulatedAnnealing(Groundplan plan) {
 		isNextTrialAccepted = false;
 		random = new Random();
 		optimalplan = new Graph(plan);
 		springembedding = new SpringEmbedding();
 		currentplan = new Graph(plan.clone());
 		nextplan = new Graph(plan.clone());
 		
 		double startValueOfT;
 	}
 
 	//Runs the algorithms
 	public Groundplan getOptimalSolution(int maxiter){
 		
 		double currentvalue,nextvalue;
 		
 		for(int i=0;i<=maxiter;i++)
 		{
 			springembedding.springEmbed(currentplan.getVertices());
 			
 			//bereken startCurrentValue en startNextValue (deze zijn nodig voor berekenen T)
 			currentvalue=currentplan.getGroundplan().getPlanValue();
 			nextvalue=nextplan.getGroundplan().getPlanValue();
 			
			if(i==0) setT(currentvalue,nextvalue);
 			if(currentvalue<nextvalue){
 				cloneGroundPlans(nextvalue);
 			}
 			else
 			{
 				if(determineAcception(currentvalue, nextvalue))
 					cloneGroundPlans(nextvalue);
 			}
 		}
 		return optimalplan.getGroundplan();
 	}
 		
 	
 	
 	private void setT(double currentvalue, double nextvalue) {
 		startTValues = new Pair<Double,Double>(currentvalue,nextvalue);
 	}
 
 	private void cloneGroundPlans(double nextValue) {
 		if(nextValue>optimalplan.getGroundplan().getPlanValue())
 			optimalplan=nextplan.clone();
 		currentplan=nextplan.clone();
 	}
 
 
 	private boolean determineAcception(double currentValue, double nextValue) {
 		double x = (nextValue - currentValue) / getValueOfT();
 		double acceptanceP = Math.pow(Math.E, -x);
 		if(random.nextDouble()<=acceptanceP){
 			return true;
 		}
 		return false;
 	}
 
 
 	private double getValueOfT() {
 		return -((double)startTValues.getRight() - (double)startTValues.getLeft())
 				/Math.log(0.8-iterator*0.00008); //10.000 iteraties
 	}
 	
 
 }
