 package lu.uni.routegeneration.jCell;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Vector;
 
 import jcell.Individual;
 import jcell.Problem;
 import jcell.RealIndividual;
 import jcell.Target;
 import lu.uni.routegeneration.evaluation.Detector;
 import lu.uni.routegeneration.generation.RouteGeneration;
 
 public class RouteGenerationProblem extends Problem{
 
 	public static int[] GeneGroupLengths = {3,4,2,2,1,1}; 
 	public static boolean discrete = false;
 	public static String bestDetectors = "";
	public static Individual bestIndividual = new RealIndividual();
 	private static double bestFitness = 1.7976931348623157E308; //new Double(0).MAX_VALUE;
 
 	RouteGeneration routeGen;
 	
 	public RouteGenerationProblem(){
 		super();
 	
 		Target.maximize = false;
 		variables = 13; 
 		maxFitness = 0.0;
 		
 		
 		//Set the maximum and minimum values for each of the solution variables 
 		//Structure  Tr/Ti/Tc/Zc1/Zc2/Zc3/Zcd/Zi1/Zid/Zr1/Zrd/IR/SR
 		
 		Double minValues[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 30.0, 20.0};
 		Double maxValues[] = {100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 70.0, 80.0};
 
 		minAllowedValues = new Vector<Double>(Arrays.asList(minValues));
         maxAllowedValues = new Vector<Double>(Arrays.asList(maxValues));
 	    
 		routeGen = new RouteGeneration();
 	}
 	
 	
 	@Override
 	public Object eval(Individual ind) {
 		
 		//for(int i = 0; i < ind.getLength(); i++)
 			//System.out.printf("Ind("+i +"):" + ind.getAllele(i));
 		
 		RouteGenerationProblem.NormaliseIndividual(ind);
 		
 		if(RouteGenerationProblem.discrete)
 		{
 			RouteGenerationProblem.DiscretiseIndividual(ind);
 		}
 		
 		double value = routeGen.evaluate(ind);
 		
 		synchronized (bestIndividual)
 		{
 			if (value < bestFitness)
 			{
 				bestFitness = value; 
 				bestDetectors = getCurrentDectectors();
 				bestIndividual = (Individual)ind.clone();
 			}
 		}
 				
 		return value;
 	}
 	
 	public String getCurrentDectectors()
 	{
 		String result = "Detectors:\r\n";
 		HashMap<String, Detector> currentSolution = routeGen.getCurrentSolution();
 		for(Detector d : currentSolution.values())
 		{
 			result += d + " ";
 		}
 		
 		result += "\r\nControls:\r\n";				
 		HashMap<String, Detector> controls = routeGen.getControls();
 		for(Detector d : controls.values())
 		{
 			result += d + " ";
 		}
 		
 		return result;
 	}
 		
 	/**
 	 * Discretises alleles of individual to integer values maintaining the group sums of 100
 	 * @param individual
 	 */
 	public static void DiscretiseIndividual(Individual individual)
     {
     	int locus = 0;
     	
     	for(int alleleGroup = 0; alleleGroup < RouteGenerationProblem.GeneGroupLengths.length; alleleGroup++)
     	{
     		int groupLength = RouteGenerationProblem.GeneGroupLengths[alleleGroup];
     		
     		double remainder = 0, value = 0;
     		for (int i = locus; i < locus + groupLength; i++)
     		{	    			
     			if (i != locus + groupLength - 1)
     			{
     				value = Math.round((double)individual.getAllele(i));
     				// accumulate decimals
     				remainder += (double)individual.getAllele(i) - value;    				
     			}
     			else
     			{
     				// add accumulated remaining decimals to last value in group (to maintain group sum of 100), round to eliminate numeric precision errors
     				value =  Math.round((double)individual.getAllele(i) + remainder);
     			}
     			
     			individual.setAllele(i, value);
     		}
     		
     		locus += groupLength;
     	}
     }
 	
 	/**
 	 * Normalises the groups of the individual to sum up to 100
 	 * @param individual
 	 */
 	public static void NormaliseIndividual(Individual individual)
     {
     	int locus = 0;
     	
     	for(int alleleGroup = 0; alleleGroup < RouteGenerationProblem.GeneGroupLengths.length; alleleGroup++)
     	{
     		int groupLength = RouteGenerationProblem.GeneGroupLengths[alleleGroup];
     	
     		// compute the actual sum of the group
     		double sum = 0;
     		for (int i = locus; i < locus + groupLength; i++)
     		{
     			sum += (double)individual.getAllele(i);
     		}
 
     		// if the group sum is different from 100 and group is composed of 2 alleles or more
     		if ((sum > 100.001 || sum < 99.999) && groupLength >= 2)
     		{	
     			// normalise alleles
 				for (int i = locus; i < locus + groupLength; i++)
 	        	{
 	    			double targetValue = 100 * (double)individual.getAllele(i) / sum;
 
 	    			individual.setAllele(i, targetValue);
         		}
     		}
     		
     		locus += groupLength;
     	}
 
     }
 
 }
