 package featureselection;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.math3.distribution.BinomialDistribution;
 
 import tree.Forest;
 import tree.IndexedDoubleData;
 import tree.ProcessDataForGrowing;
 import tree.TreeGrowthControl;
 
 public class Boruta
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		// Required inputs.
 		String inputLocation = args[0];  // The location of the file containing the data to use in the feature selection.
 		File inputFile = new File(inputLocation);
 		if (!inputFile.isFile())
 		{
 			System.out.println("The first argument must be a valid file location, and must contain the data for feature selection.");
 			System.exit(0);
 		}
 		String outputLocation = args[1];  // The location to store any and all results.
 		File outputDirectory = new File(outputLocation);
 		if (!outputDirectory.exists())
 		{
 			boolean isDirCreated = outputDirectory.mkdirs();
 			if (!isDirCreated)
 			{
 				System.out.println("The output directory could not be created.");
 				System.exit(0);
 			}
 		}
 		else if (!outputDirectory.isDirectory())
 		{
 			// Exists and is not a directory.
 			System.out.println("The second argument must be a valid directory location or location where a directory can be created.");
 			System.exit(0);
 		}
 
 		// Read in the user input.
 		double confidenceLevel = 0.999;
 		int maxRuns = 100;
 		boolean quickRun = false;
 		boolean verbose = false;
 		int numberOfTrees = 5000;
 		boolean isCompareSelf = false;
 		boolean isMultipleTestingUsed = false;
 		int argIndex = 2;
 		while (argIndex < args.length)
 		{
 			String currentArg = args[argIndex];
 			switch (currentArg)
 			{
 			case "-c":
 				argIndex += 1;
 				confidenceLevel = Double.parseDouble(args[argIndex]);
 				argIndex += 1;
 				break;
 			case "-r":
 				argIndex += 1;
 				maxRuns = Integer.parseInt(args[argIndex]);
 				argIndex += 1;
 				break;
 			case "-q":
 				argIndex += 1;
 				quickRun = true;
 				break;
 			case "-v":
 				argIndex += 1;
 				verbose = true;
 				break;
 			case "-t":
 				argIndex += 1;
 				numberOfTrees = Integer.parseInt(args[argIndex]);
 				argIndex += 1;
 				break;
 			case "-s":
 				argIndex += 1;
 				isCompareSelf = true;
 				break;
 			case "-m":
 				argIndex += 1;
 				isMultipleTestingUsed = true;
 				break;
 			default:
 				System.out.format("Unexpeted argument : %s.\n", currentArg);
 				System.exit(0);
 			}
 		}
 
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 		Integer[] trainingObsToUse = {};
 
 		TreeGrowthControl ctrl = new TreeGrowthControl();
 		ctrl.isReplacementUsed = true;
 		ctrl.numberOfTreesToGrow = numberOfTrees;
 		ctrl.mtry = 10;
 		ctrl.isStratifiedBootstrapUsed = true;
 		ctrl.isCalculateOOB = false;
 		ctrl.trainingObservations = Arrays.asList(trainingObsToUse);
 
 		Map<String, Double> weights = new HashMap<String, Double>();
 		weights.put("Unlabelled", 1.0);
 		weights.put("Positive", 1.0);
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 
 		ProcessDataForGrowing processedData = new ProcessDataForGrowing(inputLocation, ctrl);
 		for (String s : processedData.covariableData.keySet())
 		{
 			if (s.contains("-Rand"))
 			{
 				System.out.println("one of the variables contains '-Rand' in its name. This is reserved for the random permuation variables. Please rename your variable.");
 				System.exit(0);
 			}
 		}
 
 		// Record the parameters used.
 		String parameterLocation = outputLocation + "/Parameters.txt";
 		try
 		{
 			FileWriter parameterOutputFile = new FileWriter(parameterLocation);
 			BufferedWriter parameterOutputWriter = new BufferedWriter(parameterOutputFile);
 			parameterOutputWriter.write("Confidence level - " + Double.toString(confidenceLevel));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Max runs - " + Integer.toString(maxRuns));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Quick run used - " + Boolean.toString(quickRun));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Compare self used - " + Boolean.toString(isCompareSelf));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Multiple testing used - " + Boolean.toString(isMultipleTestingUsed));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Weights used - " + weights.toString());
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Training observations used - " + Arrays.toString(trainingObsToUse));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Number of trees - " + Integer.toString(ctrl.numberOfTreesToGrow));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.close();
 
 			ctrl.save(outputLocation + "/TreeGrowthController.txt");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		// Initialise variable decisions (Tentative, Confirmed or Rejected).
 		Map<String, String> variableDecisions = new HashMap<String, String>();
 		for (String s : processedData.covariableData.keySet())
 		{
 			variableDecisions.put(s, "Tentative");
 		}
 
 		// Initialise the number of runs needed in a round (so that the probability of the least important real variable having 0
 		// hits is smaller than 1 - confidenceLEvel).
 		int numberOfRunsForSignificance = (int) Math.ceil(-(Math.log(1 - confidenceLevel) / Math.log(2)));  // Want the base 2 -log(1-confidenceLevel), so have to do -(log(x)/log(2))
 
 		//==============================================================================
 		// Preliminary round 1.
 		//==============================================================================
 		if (verbose)
 		{
 			System.out.println("Performing preliminary round 1.");
 		}
 		// Setup the record of the hits for the non-rejected variables.
 		Map<String, Integer> hits = new HashMap<String, Integer>();
 		for (String s : variableDecisions.keySet())
 		{
 			hits.put(s, 0);
 		}
 		// Grow the forest on the permuted datasets, and record the hits for each run.
 		for (int i = 0; i < numberOfRunsForSignificance; i++)
 		{
 			Set<String> hitVariables = randomforestRunner(processedData, variableDecisions, ctrl, 5, quickRun, isCompareSelf, weights);
 			for (String s : hitVariables)
 			{
 				hits.put(s, hits.get(s) + 1);
 			}
 		}
 		// Determine whether any variables have a significantly different number of hits than expected.
 		Map<String, String> decisionsMadeThisRound = doTest(hits, numberOfRunsForSignificance, confidenceLevel, false);  // Don't allow variables to be Confirmed.
 		for (String s : decisionsMadeThisRound.keySet())
 		{
 			if (variableDecisions.get(s).equals("Tentative"))
 			{
 				// If a variable is currently Tentative, and a decision to reject it has been made, then mark it as Rejected.
 				variableDecisions.put(s, decisionsMadeThisRound.get(s));
 				if (verbose)
 				{
 					System.out.format("Variable %s is %s.\n", s, decisionsMadeThisRound.get(s));
 				}
 			}
 		}
 
 		//==============================================================================
 		// Preliminary round 2.
 		//==============================================================================
 		if (verbose)
 		{
 			System.out.println("Performing preliminary round 2.");
 		}
 		// Setup the record of the hits for the non-rejected variables.
 		hits = new HashMap<String, Integer>();
 		for (String s : variableDecisions.keySet())
 		{
 			if (!variableDecisions.get(s).equals("Rejected"))
 			{
 				hits.put(s, 0);
 			}
 		}
 		// Grow the forest on the permuted datasets, and record the hits for each run.
 		for (int i = 0; i < numberOfRunsForSignificance; i++)
 		{
 			Set<String> hitVariables = randomforestRunner(processedData, variableDecisions, ctrl, 3, quickRun, isCompareSelf, weights);
 			for (String s : hitVariables)
 			{
 				hits.put(s, hits.get(s) + 1);
 			}
 		}
 		// Determine whether any variables have a significantly different number of hits than expected.
 		decisionsMadeThisRound = doTest(hits, numberOfRunsForSignificance, confidenceLevel, false);  // Don't allow variables to be Confirmed.
 		for (String s : decisionsMadeThisRound.keySet())
 		{
 			if (variableDecisions.get(s).equals("Tentative"))
 			{
 				// If a variable is currently Tentative, and a decision to reject it has been made, then mark it as Rejected.
 				variableDecisions.put(s, decisionsMadeThisRound.get(s));
 				if (verbose)
 				{
 					System.out.format("Variable %s is %s.\n", s, decisionsMadeThisRound.get(s));
 				}
 			}
 		}
 
 		//==============================================================================
 		// Preliminary round 3.
 		//==============================================================================
 		if (verbose)
 		{
 			System.out.println("Performing preliminary round 3.");
 		}
 		// Setup the record of the hits for the non-rejected variables.
 		hits = new HashMap<String, Integer>();
 		for (String s : variableDecisions.keySet())
 		{
 			if (!variableDecisions.get(s).equals("Rejected"))
 			{
 				hits.put(s, 0);
 			}
 		}
 		// Grow the forest on the permuted datasets, and record the hits for each run.
 		for (int i = 0; i < numberOfRunsForSignificance; i++)
 		{
 			Set<String> hitVariables = randomforestRunner(processedData, variableDecisions, ctrl, 2, quickRun, isCompareSelf, weights);
 			for (String s : hitVariables)
 			{
 				hits.put(s, hits.get(s) + 1);
 			}
 		}
 		// Determine whether any variables have a significantly different number of hits than expected.
 		decisionsMadeThisRound = doTest(hits, numberOfRunsForSignificance, confidenceLevel, false);  // Don't allow variables to be Confirmed.
 		for (String s : decisionsMadeThisRound.keySet())
 		{
 			if (variableDecisions.get(s).equals("Tentative"))
 			{
 				// If a variable is currently Tentative, and a decision to reject it has been made, then mark it as Rejected.
 				variableDecisions.put(s, decisionsMadeThisRound.get(s));
 				if (verbose)
 				{
 					System.out.format("Variable %s is %s.\n", s, decisionsMadeThisRound.get(s));
 				}
 			}
 		}
 
 		//==============================================================================
 		// Final round.
 		//==============================================================================
 		if (verbose)
 		{
 			System.out.println("Performing final round.");
 		}
 		// Setup the record of the hits for the non-rejected variables.
 		hits = new HashMap<String, Integer>();
 		for (String s : variableDecisions.keySet())
 		{
 			if (!variableDecisions.get(s).equals("Rejected"))
 			{
 				hits.put(s, 0);
 			}
 		}
 		int currentRun = 0;
 		// Determine if there are any Tentative variables left (if not at this stage it means that all have been rejected).
 		boolean isTentativesRemain = false;  // Records whether there are any variables that are not Confirmed or Rejected.
 		for (String s : variableDecisions.keySet())
 		{
 			if (variableDecisions.get(s).equals("Tentative"))
 			{
 				isTentativesRemain = true;
 			}
 		}
 
 		// Keep performing runs until there are either all variables have been Confirmed or Rejected, or the maximum number of runs has been performed.
 		while (isTentativesRemain && currentRun < maxRuns)
 		{
 			currentRun += 1;
 			if (verbose)
 			{
 				System.out.format("\tNow performing run: %d.\n", currentRun);
 			}
 
 			// Grow the forest on the permuted datasets, and record the hits for the run.
 			Set<String> hitVariables = randomforestRunner(processedData, variableDecisions, ctrl, 1, quickRun, isCompareSelf, weights);
 			for (String s : hitVariables)
 			{
 				hits.put(s, hits.get(s) + 1);
 			}
 
 			// Determine whether testing for Confirmed and Rejected variables should be performed on this run.
 			// This is done either if multiple testing is being used and the number of runs has reached the level for significance
 			// or if multiple testing is not being used and the number of runs has reached the maximum specified.
			if ((isMultipleTestingUsed && currentRun >= numberOfRunsForSignificance) || currentRun + 1 == maxRuns)
 			{
 				decisionsMadeThisRound = doTest(hits, currentRun, confidenceLevel, true);  // Allow variables to be Confirmed.
 				for (String s : decisionsMadeThisRound.keySet())
 				{
 					if (variableDecisions.get(s).equals("Tentative"))
 					{
 						// If a variable is currently Tentative, and a decision about it has been made, then record the decision (Confirmed or Rejected).
 						variableDecisions.put(s, decisionsMadeThisRound.get(s));
 						if (verbose)
 						{
 							System.out.format("Variable %s is %s.\n", s, decisionsMadeThisRound.get(s));
 						}
 					}
 				}
 				// Determine whether any variables remain that have not been Confirmed or Rejected.
 				isTentativesRemain = false;
 				for (String s : variableDecisions.keySet())
 				{
 					if (variableDecisions.get(s).equals("Tentative"))
 					{
 						isTentativesRemain = true;
 					}
 				}
 			}
 		}
 		if (verbose)
 		{
 			if (isTentativesRemain)
 			{
 				System.out.println("Maximum runs reached. Tentatives remain.");
 			}
 			else
 			{
 				System.out.println("No tentatives remain.");
 			}
 		}
 
 		// Write out the results.
 		String confirmedOutputLocation = outputLocation + "/Confirmed.txt";
 		String confirmedAndTentativeOutputLocation = outputLocation + "/ConfirmedAndTentative.txt";
 		String tentativeOutputLocation = outputLocation + "/Tentative.txt";
 		String rejectedOutputLocation = outputLocation + "/Rejected.txt";
 		try
 		{
 			FileWriter confirmedOutputFile = new FileWriter(confirmedOutputLocation);
 			BufferedWriter confirmedOutputWriter = new BufferedWriter(confirmedOutputFile);
 			FileWriter confirmedAndTentativeOutputFile = new FileWriter(confirmedAndTentativeOutputLocation);
 			BufferedWriter confirmedAndTentativeOutputWriter = new BufferedWriter(confirmedAndTentativeOutputFile);
 			FileWriter tentativeOutputFile = new FileWriter(tentativeOutputLocation);
 			BufferedWriter tentativeOutputWriter = new BufferedWriter(tentativeOutputFile);
 			FileWriter rejectedOutputFile = new FileWriter(rejectedOutputLocation);
 			BufferedWriter rejectedOutputWriter = new BufferedWriter(rejectedOutputFile);
 			for (String s : variableDecisions.keySet())
 			{
 				if (variableDecisions.get(s).equals("Confirmed"))
 				{
 					confirmedOutputWriter.write(s);
 					confirmedOutputWriter.newLine();
 					confirmedAndTentativeOutputWriter.write(s);
 					confirmedAndTentativeOutputWriter.newLine();
 				}
 				else if (variableDecisions.get(s).equals("Tentative"))
 				{
 					confirmedAndTentativeOutputWriter.write(s);
 					confirmedAndTentativeOutputWriter.newLine();
 					tentativeOutputWriter.write(s);
 					tentativeOutputWriter.newLine();
 				}
 				else
 				{
 					rejectedOutputWriter.write(s);
 					rejectedOutputWriter.newLine();
 				}
 			}
 			confirmedOutputWriter.close();
 			confirmedAndTentativeOutputWriter.close();
 			tentativeOutputWriter.close();
 			rejectedOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Test whether any variables have significantly more/less hits than expected.
 	 * 
 	 * @param hits - A record of the number of times that a variable was greater than the random variables.
 	 * @param runsPerformed - The number of runs performed.
 	 * @param isAcceptanceEnabled - Whether or not variables should be marked as accepted if they have significantly more hits than expected.
 	 * @return - A mapping from variables that differed significantly to whether they should be Confirmed or Rejected.
 	 */
 	static Map<String, String> doTest(Map<String, Integer> hits, int runsPerformed, double confidenceLevel, boolean isAcceptanceEnabled)
 	{
 		Map<String, String> significantlyDifferent = new HashMap<String, String>();
 
 		// Determine the number of hits needed to be Confirmed or Rejected.
 		BinomialDistribution bd = new BinomialDistribution(runsPerformed, 0.5);
 		int significantlyMoreHits = bd.inverseCumulativeProbability(confidenceLevel);
 		int significantlyFewerHits = 0;
 		while ((1 - bd.cumulativeProbability(significantlyFewerHits) > confidenceLevel))
 		{
 			significantlyFewerHits += 1;
 		}
 
 		if (isAcceptanceEnabled)
 		{
 			for (String s : hits.keySet())
 			{
 				if (hits.get(s) > significantlyMoreHits)
 				{
 					significantlyDifferent.put(s, "Confirmed");
 				}
 			}
 		}
 		for (String s : hits.keySet())
 		{
 			if (hits.get(s) < significantlyFewerHits)
 			{
 				significantlyDifferent.put(s, "Rejected");
 			}
 		}
 
 		return significantlyDifferent;
 	}
 
 	/**
 	 * Perform one run of the comparison between random permuted variables and real variables.
 	 * 
 	 * @param procData - The processed data without random permuted variables added.
 	 * @param variableDecisions - Decisions made so far about the variables.
 	 * @param ctrl - The controller object for growing the tree.
 	 * @param levelOfRandomness - The nth most important random permuted variable that a real variable must be greater than to score a hit.
 	 * 							- For example, if it is 5 then the real variables must be more important than the 5th most important random permuted variable.
 	 * @param quickRun - Whether or not to perform a quick run using less random variables (only the non-rejected ones).
 	 * @return - A set of the variables that scored a hit.
 	 */
 	static Set<String> randomforestRunner(ProcessDataForGrowing procData, Map<String, String> variableDecisions, TreeGrowthControl ctrl,
 			int levelOfRandomness, boolean quickRun, boolean isCompareSelf, Map<String, Double> weights)
 	{
 		// Generate a deep copy of the data.
 		ProcessDataForGrowing copyData = new ProcessDataForGrowing(procData);
 
 		// Choose variables to generate permutations of.
 		List<String> nonRejectedVariables = new ArrayList<String>();
 		List<String> tempVarsToPermute = new ArrayList<String>();
 		for (String s : variableDecisions.keySet())
 		{
 			if (!variableDecisions.get(s).equals("Rejected"))
 			{
 				nonRejectedVariables.add(s);
 				if (!quickRun)
 				{
 					// If quickRun is chosen, then only generate permutations of non-rejected variables.
 					tempVarsToPermute.add(s);
 				}
 			}
 			else
 			{
 				tempVarsToPermute.add(s);
 			}
 		}
 		List<String> varsToPermute = new ArrayList<String>(tempVarsToPermute);
 //		while (varsToPermute.size() < 5)
 //		{
 //			// Ensure that there are at least 5 random permutation variables.
 //			// Do this by randomly adding a variable to the list of variables to permute until 5 are in the list.
 //			Collections.shuffle(tempVarsToPermute);
 //			varsToPermute.add(tempVarsToPermute.get(0));
 //		}
 
 		// Generate the random permutation variables, and add them to the dataset.
 		for (String s : varsToPermute)
 		{
 			List<Double> permutedData = new ArrayList<Double>(copyData.covariableData.get(s));
 			Collections.shuffle(permutedData);
 			copyData.addCovariable(s + "-Rand", permutedData);
 		}
 
 		// Learn a forest.
 		TreeGrowthControl copyCtrl = new TreeGrowthControl(ctrl);
 		for (String s : variableDecisions.keySet())
 		{
 			if (variableDecisions.get(s).equals("Rejected"))
 			{
 				// If the variable is Rejected, then ignore it when growing the tree.
 				copyCtrl.variablesToIgnore.add(s);
 			}
 		}
 		Forest forest = new Forest(copyData, copyCtrl);
 		forest.setWeightsByClass(weights);
 		forest.growForest();
 
 		// Determine importance of variables, and the importance of the nth most important random permutation variable.
 		Map<String, Double> varImp = forest.variableImportance().second;
 		List<String> varsWithImportance = new ArrayList<String>(varImp.keySet());
 		List<IndexedDoubleData> sortedRandomImportances = new ArrayList<IndexedDoubleData>();
 		for (int i = 0; i < varsWithImportance.size(); i++)
 		{
 			String currentVar = varsWithImportance.get(i);
 			if (currentVar.contains("-Rand"))
 			{
 				// Only add the importances for the random permutation variables.
 				IndexedDoubleData newEntry = new IndexedDoubleData(varImp.get(currentVar), i);
 				sortedRandomImportances.add(newEntry);
 			}
 		}
 		Collections.sort(sortedRandomImportances, Collections.reverseOrder());
 		double nthMostImportantPermutedVarValue = sortedRandomImportances.get(levelOfRandomness - 1).getData();
 
 		// Determine hits.
 		Set<String> greaterThanRandom = new HashSet<String>();
 		for (String s : nonRejectedVariables)
 		{
 			// Only consider the variables that have not been rejected yet as potentials for hits.
 			if (varImp.get(s) > nthMostImportantPermutedVarValue)
 			{
 				greaterThanRandom.add(s);
 			}
 		}
 		return greaterThanRandom;
 	}
 
 }
