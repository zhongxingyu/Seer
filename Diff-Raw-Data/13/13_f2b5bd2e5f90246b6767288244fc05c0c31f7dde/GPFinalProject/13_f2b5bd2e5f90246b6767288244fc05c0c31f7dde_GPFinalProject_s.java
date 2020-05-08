 package GPFinalProject;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class GPFinalProject
 {
 
 	/**
 	 * @param args
 	 */
 
 	public static void main(String[] args)
 	{		
 		// Default Config Numbers, if config file is found these will be overwritten
 		int numCandidates = 1000;
 		int maxNumberOfGenerations = 500000;		
 
 		double crossoverHighProbabilityCrossPoint = 0.11;
 		double crossoverHighProbabilityRate = 0.91;
 		double crossoverRate = 0.21;
 		
 		double mutationHighProbabilityCrossPoint = 0.51;
 		double mutationHighProbabilityRate = 0.51;
 		double mutationRate = 0.051;
 		
 		double naturalSelectionHighProbabilityCrossPoint = 0.91;
 		double naturalSelectionHighProbabilityRate = 0.91;
 		double naturalSelectionRate = 0.000002;
 
 		double [] trainingData = {-500, -100, -10.0, 0, 10, 25, 100, 500};
 		
 		// TODO Auto-generated method stub
 		Config myConfig = new Config();
 		
 		try
 		{
 			myConfig.initializeData("GP_Config.txt", "=");
 			
 			numCandidates = myConfig.getInitialPopulationSize();
 			maxNumberOfGenerations = myConfig.getMaxGenerationCount();
 			crossoverHighProbabilityCrossPoint = 0.01 * myConfig.getCrossOverHighProbabilityPoint();
 			crossoverHighProbabilityRate = 0.01 * myConfig.getCrossOverHighProbabilityRate();
 			crossoverRate = 0.01 * myConfig.getCrossOverRate();
 			mutationHighProbabilityCrossPoint = 0.01 * myConfig.getMutationHighProbabilityCrossPoint();
 			mutationHighProbabilityRate = 0.01 * myConfig.getMutationHighProbabilityRate();
 			mutationRate = 0.01 * myConfig.getMutationRate();
 			naturalSelectionHighProbabilityCrossPoint = 0.01 * myConfig.getNaturalSelectionProbabilityCrossOverPoint();
 			naturalSelectionHighProbabilityRate = 0.01 * myConfig.getNaturalSelectionProbabilityCrossOverRate();
 			naturalSelectionRate = 0.01 * myConfig.getNaturalSelectionRate();
 			GPNode.operator_probability = myConfig.getOperatorProbability();
 			GPNode.operand_x_probability = myConfig.getVariableXProbability();
 			GPNode.random_value_minimum = myConfig.getRandomMinimum();
 			GPNode.random_value_maximum = myConfig.getRandomMaximum();
 			GPNode.maximum_tree_depth = myConfig.getMaxInitialDepth();
 		}
 		catch (Exception e)
 		{
 			
 		}
 		
 		// Output Config Data
 		System.out.println("*** CONFIG DATA BEING USED ***");
 		System.out.println("Number of Initial Candidates:     " + numCandidates);
 		System.out.println("Maximum Number of Generations:    " + maxNumberOfGenerations);
 		System.out.println("Crossover High Probability Point: " + crossoverHighProbabilityCrossPoint);
 		System.out.println("Crossover High Probability Rate:  " + crossoverHighProbabilityRate);
 		System.out.println("Crossover Rate                    " + crossoverRate);
 		System.out.println("Mutation High Probability Point:  " + mutationHighProbabilityCrossPoint);
 		System.out.println("Mutation High Probability Rate:   " + mutationHighProbabilityRate);
 		System.out.println("Mutation Rate:                    " + mutationRate);
 		System.out.println("Natural Select High Prob Point:   " + naturalSelectionHighProbabilityCrossPoint);
 		System.out.println("Natural Select High Prob Rate:    " + naturalSelectionHighProbabilityRate);
 		System.out.println("Natural Selection Rate:           " + naturalSelectionRate);		
 		System.out.println("Operator Probability:             " + GPNode.operator_probability);
 		System.out.println("Operand X Probability:            " + GPNode.operand_x_probability);
 		System.out.println("Constant Minimum Value:           " + GPNode.random_value_minimum);
 		System.out.println("Constant Maximum Value:           " + GPNode.random_value_maximum);
 		System.out.println("Maximum Initial Tree Depth:       " + GPNode.maximum_tree_depth);
 		
 		ArrayList<GPCandidate> gpCandidates = new ArrayList<GPCandidate>();
 
 		System.out.println("");
 		System.out.println("***Training Data***");
 		
 		double [] expectedValues = new double[trainingData.length];		
 		for (int i = 0; i < expectedValues.length; i++)
 		{
 			expectedValues[i] =  (trainingData[i]*trainingData[i]) - 1;
 			System.out.println("X value: " + trainingData[i] + "; Output:" + expectedValues[i]);
 		}
 		
 		System.out.println("");
 		System.out.println("***Training Data***");
 		
 		/* Create the candidates */
 		for (int i = 0; i < numCandidates; i++)
 		{
 			gpCandidates.add(new GPCandidate());
 			gpCandidates.get(i).updateFitnessValue(trainingData, expectedValues);
 		}
 
 		/* Print them to console */
 		for (int i = 0; i < gpCandidates.size(); i++)
 		{
 			System.out.println(i + ":" + gpCandidates.get(i).getTopNode().GetGPString() );
 		}
 		
 		// When the numbers get small enough the carryovers allow us to meet our required rates when round off errors could happen
 		double crossoverCarryOver = 0.0;
 		double mutationCarryOver = 0.0;
 		double naturalSelectionCarryOver = 0.0;
 		
 		for (int i = 0; i < maxNumberOfGenerations; i++)
 		{			
 			// Need to rank the candidates
 			Collections.sort(gpCandidates, new GPFitnessValueComparator());
 
 			// Print out the best one we have
 			System.out.println(/*"(" + gpCandidates.size() + ")*/ "Best fitness value of generation: " + i + " is: " + gpCandidates.get(0).getFitnessValue() + "; Candidate is: " + gpCandidates.get(0).getTopNode().GetGPString());
 
 			
 			double fitnessValue = gpCandidates.get(0).getFitnessValue();
 			if (fitnessValue < 0.000001)
 			{
 				System.out.println("Generation:" + i + ": Candidate found!  Error is: " + fitnessValue + " String is: " + gpCandidates.get(0).candidate.GetGPString());
 				return;
 			}			
 	
 			naturalSelectionCarryOver += gpCandidates.size() * naturalSelectionRate;
 			int numNaturalSelections = (int) Math.floor(naturalSelectionCarryOver);
 			naturalSelectionCarryOver -= (double) numNaturalSelections;
			
 			/* Natural Selection */
 			for (int j = 0; j < numNaturalSelections; j++)
 			{
 				double hp = Utilities.GetRandomDouble();
 				
 				int minimum = 0;
 				int maximum = (int) (naturalSelectionHighProbabilityCrossPoint * gpCandidates.size());
 				if (hp > naturalSelectionHighProbabilityRate)
 				{
 					minimum = maximum + 1;
 					maximum = gpCandidates.size()-1;
 				}
 					
 				int rand1 = Utilities.GetRandomNumber(minimum, maximum);
 				gpCandidates.remove(rand1);
 			}
 			
 			
 			/* Crossover */
 			crossoverCarryOver += gpCandidates.size() * crossoverRate;
 			int numCrossovers = (int) Math.floor(crossoverCarryOver);
 			crossoverCarryOver -= (double) numCrossovers;
 			for (int j = 0; j < numCrossovers; j++)
 			{
 				double hp = Utilities.GetRandomDouble();
 				
 				int minimum = 0;
 				int maximum = (int) (crossoverHighProbabilityCrossPoint * gpCandidates.size());
 				if (hp > crossoverHighProbabilityRate)
 				{
 					minimum = maximum + 1;
 					maximum = gpCandidates.size()-1;
 				}
 					
 				int rand1 = Utilities.GetRandomNumber(minimum, maximum);
 				
 				hp = Utilities.GetRandomDouble();
 				minimum = 0;
 				maximum = (int) (crossoverHighProbabilityCrossPoint * gpCandidates.size());
 				if (hp > crossoverHighProbabilityRate)
 				{
 					minimum = maximum + 1;
 					maximum = gpCandidates.size()-1;
 				}
 				int rand2 = Utilities.GetRandomNumber(minimum, maximum);
 			
 				GPNode.crossoverNodes(gpCandidates.get(rand1).getTopNode(), gpCandidates.get(rand2).getTopNode());
 				gpCandidates.get(rand1).updateFitnessValue(trainingData, expectedValues);
 				gpCandidates.get(rand2).updateFitnessValue(trainingData, expectedValues);
 			}
 			
 			/* Mutation */
 			mutationCarryOver += gpCandidates.size() * mutationRate;
 			int numMutations = (int) Math.floor(mutationCarryOver);
 			mutationCarryOver -= (double) numMutations;
 			for (int j = 0; j < numMutations; j++)
 			{
 				double hp = Utilities.GetRandomDouble();
 				
 				int minimum = 0;
 				int maximum = (int) (mutationHighProbabilityCrossPoint * gpCandidates.size());
 				if (hp > mutationHighProbabilityRate)
 				{
 					minimum = maximum + 1;
 					maximum = gpCandidates.size()-1;
 				}
 					
 				int rand1 = Utilities.GetRandomNumber(minimum, maximum);
 				GPNode.mutateNodes(gpCandidates.get(rand1).getTopNode());
 				gpCandidates.get(rand1).updateFitnessValue(trainingData, expectedValues);
 			}
 		}	
 		
 		System.out.println("Maximum number of generations reached.  No candidate found.");
 		return;
 	}
 }
