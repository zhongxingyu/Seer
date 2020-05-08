 package featureselection;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import datasetgeneration.BootstrapGeneration;
 
 import tree.Forest;
 import tree.ImmutableTwoValues;
 import tree.ProcessDataForGrowing;
 import tree.TreeGrowthControl;
 
 public class BackwardsElimination
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		// Required inputs.
 		String inputLocation = args[0];  // The location of the file containing the dataset to use in the feature selection.
 		File inputFile = new File(inputLocation);
 		if (!inputFile.isFile())
 		{
 			System.out.println("The first argument must be a valid file location, and must contain the dataset for feature selection.");
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
 
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
		int externalSubsamplesToGenerate = 50;
 		double fractionToReserveAsValidation = 0.3;
		int internalSubsamplesToGenerate = 2;
 		int validationIterations = 5;
		double fractionToElim = 0.5;  // Eliminating a fraction allows you to remove lots of variables when there are lots remaining, and get better resolution when there are few remaining.
 		double featuresToEliminate;
 		boolean continueRun = false;  // Whether or not you want to continue a run in progress or restart the whole process.
 		Integer[] trainingObsToUse = {};
 
 		TreeGrowthControl ctrl = new TreeGrowthControl();
 		ctrl.isReplacementUsed = true;
		ctrl.numberOfTreesToGrow = 2500;
 		ctrl.mtry = 10;
 		ctrl.isStratifiedBootstrapUsed = true;
 		ctrl.isCalculateOOB = false;
 		ctrl.minNodeSize = 1;
 		ctrl.trainingObservations = Arrays.asList(trainingObsToUse);
 
 		TreeGrowthControl varImpCtrl = new TreeGrowthControl(ctrl);
		varImpCtrl.numberOfTreesToGrow = 5000;
 		varImpCtrl.trainingObservations = Arrays.asList(trainingObsToUse);
 
 		Map<String, Double> weights = new HashMap<String, Double>();
 		weights.put("Unlabelled", 1.0);
 		weights.put("Positive", 1.5);
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 
 		// Get the names and types of the features (the types are so that you know which features are the response and which aren't used).
 		String featureNames[] = null;
 		String featureTypes[] = null;
 		try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputLocation), StandardCharsets.UTF_8))
 		{
 			String line = reader.readLine();
 			line = line.replaceAll("\n", "");
 			featureNames = line.split("\t");
 
 			line = reader.readLine();
 			line = line.toLowerCase();
 			line = line.replaceAll("\n", "");
 			featureTypes = line.split("\t");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		// Determine the features that are used in growing the trees.
 		List<String> featuresUsed = new ArrayList<String>();
 		for (int i = 0; i < featureNames.length; i++)
 		{
 			if (featureTypes[i].equals("x") || featureTypes[i].equals("r"))
 			{
 				// If the feature is a response variable or is to be skipped.
 				continue;
 			}
 			featuresUsed.add(featureNames[i]);
 		}
 
 		// Determine whether bootstrap samples need to be generated.
 		String resultsOutputLoc = outputLocation + "/Results.txt";
 		String parameterLocation = outputLocation + "/Parameters.txt";
 		String subsetSizeErrorRates = outputLocation + "/ErrorRates.txt";
 		if (!continueRun)
 		{
 			// Generate bootstraps and recreate the results file.
 			removeDirectoryContent(outputDirectory);
 			BootstrapGeneration.main(inputLocation, outputLocation, externalSubsamplesToGenerate, false, 1 - fractionToReserveAsValidation);
 			try
 			{
 				FileWriter featureFractionsOutputFile = new FileWriter(resultsOutputLoc);
 				BufferedWriter featureFractionsOutputWriter = new BufferedWriter(featureFractionsOutputFile);
 				for (String s : featuresUsed)
 				{
 					featureFractionsOutputWriter.write(s + "\t");
 				}
 				featureFractionsOutputWriter.write("ErrorRate");
 				featureFractionsOutputWriter.newLine();
 				featureFractionsOutputWriter.close();
 
 				FileWriter parameterOutputFile = new FileWriter(parameterLocation);
 				BufferedWriter parameterOutputWriter = new BufferedWriter(parameterOutputFile);
 				parameterOutputWriter.write("External subsamples generated - " + Integer.toString(externalSubsamplesToGenerate));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Fraction to reserve as validation - " + Double.toString(fractionToReserveAsValidation));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Internal subsamples generated - " + Integer.toString(internalSubsamplesToGenerate));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Validation iterations performed - " + Integer.toString(validationIterations));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Fraction of features to eliminate each round - " + Double.toString(fractionToElim));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Trees used in training - " + Integer.toString(ctrl.numberOfTreesToGrow));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Trees used for variable importance - " + Integer.toString(varImpCtrl.numberOfTreesToGrow));
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.write("Weights used - " + weights.toString());
 				parameterOutputWriter.newLine();
 				parameterOutputWriter.close();
 
 				FileWriter errorRateOutputFile = new FileWriter(subsetSizeErrorRates);
 				BufferedWriter errorRateOutputWriter = new BufferedWriter(errorRateOutputFile);
 				int numberOfFeaturesRemaining = featuresUsed.size();
 				String errorRateHeader = "";
 				while (numberOfFeaturesRemaining > 0)
 				{
 					errorRateHeader += Integer.toString(numberOfFeaturesRemaining) + "\t";
 					featuresToEliminate = (int) Math.ceil(numberOfFeaturesRemaining * fractionToElim);
 					numberOfFeaturesRemaining -= featuresToEliminate;
 				}
 				errorRateHeader = errorRateHeader.substring(0, errorRateHeader.length() - 1);
 				errorRateOutputWriter.write(errorRateHeader);
 				errorRateOutputWriter.newLine();
 				errorRateOutputWriter.close();
 
 				ctrl.save(outputLocation + "/RegularCtrl.txt");
 				varImpCtrl.save(outputLocation + "/VariableImportanceCtrl.txt");
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 
 		for (int i = 0; i < externalSubsamplesToGenerate; i++)
 		{
 			String subsampleDirectory = outputLocation + "/" + Integer.toString(i);
 			if ((new File(subsampleDirectory + "/ErrorForThisSubsample.txt")).exists())
 			{
 				continue;
 			}
 			Date currentTime = new Date();
 		    DateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		    String strDate = sdfDate.format(currentTime);
 			System.out.format("Now working on subsample %d at %s.\n", i, strDate);
 
 			String subsampleTrainingSet = subsampleDirectory + "/Train.txt";
 			String subsampleTestingSet = subsampleDirectory + "/Test.txt";
 			String internalFoldDirLoc = subsampleDirectory + "/Folds";
 
 			ImmutableTwoValues<Integer, Map<Integer, Double>> internalSelectionResults = internalSelection(subsampleTrainingSet,
 					internalFoldDirLoc, internalSubsamplesToGenerate, weights, ctrl, varImpCtrl, featuresUsed, fractionToElim);
 			int bestNumberOfFeatures = internalSelectionResults.first;
 			Map<Integer, Double> averageErrorRates = internalSelectionResults.second;
 
 			// Determine and validate best feature subset.
 			currentTime = new Date();
 		    sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		    strDate = sdfDate.format(currentTime);
 			System.out.format("\tNow validating the feature set at %s.\n", strDate);
 			ImmutableTwoValues<List<String>, Double> validationResults = validateSubset(subsampleTrainingSet, subsampleTestingSet,
 					bestNumberOfFeatures, weights, new TreeGrowthControl(varImpCtrl), featuresUsed, validationIterations, false);
 			List<String> bestFeatureSet = validationResults.first;
 			double validatedError = validationResults.second;
 
 			// Write out the results for this external subsample.
 			try
 			{
 				FileWriter featureFractionsOutputFile = new FileWriter(resultsOutputLoc, true);
 				BufferedWriter featureFractionsOutputWriter = new BufferedWriter(featureFractionsOutputFile);
 				for (String s : featuresUsed)
 				{
 					if (bestFeatureSet.contains(s))
 					{
 						featureFractionsOutputWriter.write("1\t");
 					}
 					else
 					{
 						featureFractionsOutputWriter.write("0\t");
 					}
 				}
 				featureFractionsOutputWriter.write(Double.toString(validatedError));
 				featureFractionsOutputWriter.newLine();
 				featureFractionsOutputWriter.close();
 
 				FileWriter subsampleOutputFile = new FileWriter(subsampleDirectory + "/ErrorForThisSubsample.txt");
 				BufferedWriter subsampleOutputWriter = new BufferedWriter(subsampleOutputFile);
 				subsampleOutputWriter.write("ErrorRate = ");
 				subsampleOutputWriter.write(Double.toString(validatedError));
 				subsampleOutputWriter.close();
 
 				List<Integer> featureSetSizes = new ArrayList<Integer>(averageErrorRates.keySet());
 				Collections.sort(featureSetSizes, Collections.reverseOrder());
 				FileWriter errorRateOutputFile = new FileWriter(subsetSizeErrorRates, true);
 				BufferedWriter errorRateOutputWriter = new BufferedWriter(errorRateOutputFile);
 				String errorOutput = "";
 				for (Integer j : featureSetSizes)
 				{
 					errorOutput += Double.toString(averageErrorRates.get(j)) + "\t";
 				}
 				errorOutput.substring(0, errorOutput.length() - 1);
 				errorRateOutputWriter.write(errorOutput);
 				errorRateOutputWriter.newLine();
 				errorRateOutputWriter.close();
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 
 		//---------------------------------------------//
 		// Perform the whole dataset feature selection //
 		//---------------------------------------------//
 		Date currentTime = new Date();
 	    DateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    String strDate = sdfDate.format(currentTime);
 		System.out.format("Now working on the full feature set selection at %s.\n", strDate);
 
 		String finalSelectionOutputLoc = outputLocation + "/FinalSelection";
 		String finalSelectionErrorRates = finalSelectionOutputLoc + "/ErrorRates.txt";
 		String finalSelectionResults = finalSelectionOutputLoc + "/Results.txt";
 
 		ImmutableTwoValues<Integer, Map<Integer, Double>> internalSelectionResults = internalSelection(inputLocation,
 				finalSelectionOutputLoc, internalSubsamplesToGenerate, weights, ctrl, varImpCtrl, featuresUsed, fractionToElim);
 		int bestNumberOfFeatures = internalSelectionResults.first;
 		Map<Integer, Double> averageErrorRates = internalSelectionResults.second;
 
 		// Determine and validate best feature subset.
 		currentTime = new Date();
 	    sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    strDate = sdfDate.format(currentTime);
 		System.out.format("\tNow validating the feature set at %s.\n", strDate);
 		ImmutableTwoValues<List<String>, Double> validationResults = validateSubset(inputLocation, bestNumberOfFeatures, weights,
 				new TreeGrowthControl(varImpCtrl), featuresUsed, validationIterations);
 		List<String> bestFeatureSet = validationResults.first;
 
 		// Write out the results for the whole dataset selection.
 		try
 		{
 			FileWriter resultsOutputFile = new FileWriter(finalSelectionResults);
 			BufferedWriter resultsOutputWriter = new BufferedWriter(resultsOutputFile);
 			for (String s : featuresUsed)
 			{
 				if (bestFeatureSet.contains(s))
 				{
 					resultsOutputWriter.write(s);
 					resultsOutputWriter.newLine();
 				}
 			}
 			resultsOutputWriter.close();
 
 			List<Integer> featureSetSizes = new ArrayList<Integer>(averageErrorRates.keySet());
 			Collections.sort(featureSetSizes, Collections.reverseOrder());
 			FileWriter errorRateOutputFile = new FileWriter(finalSelectionErrorRates, true);
 			BufferedWriter errorRateOutputWriter = new BufferedWriter(errorRateOutputFile);
 			String errorOutput = "";
 			String errorRateHeader = "";
 			for (Integer j : featureSetSizes)
 			{
 				errorRateHeader += Integer.toString(j) + "\t";
 				errorOutput += Double.toString(averageErrorRates.get(j)) + "\t";
 			}
 			errorRateHeader = errorRateHeader.substring(0, errorRateHeader.length() - 1);
 			errorOutput.substring(0, errorOutput.length() - 1);
 			errorRateOutputWriter.write(errorRateHeader);
 			errorRateOutputWriter.newLine();
 			errorRateOutputWriter.write(errorOutput);
 			errorRateOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 
 	static Map<Integer, Double> internalEvaluation(String internalSubsampleTrainingSet, String internalSubsampleTestingSet, long internalSubsampleSeed,
 			Map<String, Double> weights, TreeGrowthControl eliminationControl, TreeGrowthControl variableImportanceControl, List<String> fullFeatureSet,
 			double fractionToElim)
 	{
 		//--------------------------------------------------------------------//
 		// Determine the order of the features by feature importance ranking. //
 		//--------------------------------------------------------------------//
 		Forest forest = new Forest(internalSubsampleTrainingSet, variableImportanceControl, weights, internalSubsampleSeed);
 		Map<String, Double> varImp = forest.variableImportance();
 
 		// Rank the variables by importance.
 		List<StringsSortedByDoubles> sortedVariables = new ArrayList<StringsSortedByDoubles>();
 		for (String s : varImp.keySet())
 		{
 			sortedVariables.add(new StringsSortedByDoubles(varImp.get(s), s));
 		}
 		Collections.sort(sortedVariables, Collections.reverseOrder());  // Larger importance first.
 		List<String> orderedFeaturesByImportance = new ArrayList<String>();
 		for (StringsSortedByDoubles ssbd : sortedVariables)
 		{
 			orderedFeaturesByImportance.add(ssbd.getId());
 		}
 
 		//--------------------------//
 		// Perform the elimination. //
 		//--------------------------//
 		Map<Integer, Double> errorRates = new HashMap<Integer, Double>();
 		int featuresToEliminate;
 		while (!orderedFeaturesByImportance.isEmpty())
 		{
 			List<String> variablesToIgnore = new ArrayList<String>(fullFeatureSet);
 			variablesToIgnore.removeAll(orderedFeaturesByImportance);
 			eliminationControl.variablesToIgnore = variablesToIgnore;
 			forest = new Forest(internalSubsampleTrainingSet, eliminationControl, weights, internalSubsampleSeed);
 			errorRates.put(orderedFeaturesByImportance.size(), forest.predict(new ProcessDataForGrowing(internalSubsampleTestingSet, eliminationControl)).first);
 			featuresToEliminate = (int) Math.ceil(orderedFeaturesByImportance.size() * fractionToElim);
 			for (int j = 0; j < featuresToEliminate; j++)
 			{
 				orderedFeaturesByImportance.remove(orderedFeaturesByImportance.size() - 1);
 			}
 		}
 
 		return errorRates;
 	}
 
 
 	static ImmutableTwoValues<Integer, Map<Integer, Double>> internalSelection(String entireTrainingSet,
 			String locatonForInternalFolds, int internalSubsamplesToGenerate, Map<String, Double> weights, TreeGrowthControl ctrl,
 			TreeGrowthControl varImpCtrl, List<String> featuresUsed, double fractionToElim)
 	{
 		ctrl.variablesToIgnore = new ArrayList<String>();
 		BootstrapGeneration.main(entireTrainingSet, locatonForInternalFolds, internalSubsamplesToGenerate, true, 0.0);
 
 		List<Map<Integer, Double>> errorRates = new ArrayList<Map<Integer, Double>>();
 		List<Long> usedSeeds = new ArrayList<Long>();
 		Random seedGenerator = new Random();
 		for (int j = 0; j < internalSubsamplesToGenerate; j++)
 		{
 			Date currentTime = new Date();
 		    DateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		    String strDate = sdfDate.format(currentTime);
 			System.out.format("\tNow performing elimination for internal bootstrap %d at %s.\n", j, strDate);
 			long seedToUse = seedGenerator.nextLong();  // Determine the seed to use for the feature selection process on this bootstrap sample.
 			while (usedSeeds.contains(seedToUse))
 			{
 				seedToUse = seedGenerator.nextLong();
 			}
 			usedSeeds.add(seedToUse);
 			String internalFoldTrainingSet = locatonForInternalFolds + "/" + Integer.toString(j) + "/Train.txt";
 			String internalFoldTestingSet = locatonForInternalFolds + "/" + Integer.toString(j) + "/Test.txt";
 			errorRates.add(internalEvaluation(internalFoldTrainingSet, internalFoldTestingSet, seedToUse, weights,
 					new TreeGrowthControl(ctrl), varImpCtrl, featuresUsed, fractionToElim));
 		}
 
 		// Determine the average error rate for each size of feature subset, and the best feature set size.
 		Map<Integer, Double> averageErrorRates = new HashMap<Integer, Double>();
 		int bestNumberOfFeatures = featuresUsed.size();
 		double lowestErrorRate = 100.0;
 		for (Integer j : errorRates.get(0).keySet())
 		{
 			double averageError = 0.0;
 			for (int k = 0; k < internalSubsamplesToGenerate; k++)
 			{
 				averageError += errorRates.get(k).get(j);
 			}
 			averageError /= internalSubsamplesToGenerate;
 			averageErrorRates.put(j, averageError);
 			if (averageError < lowestErrorRate)
 			{
 				bestNumberOfFeatures = j;
 				lowestErrorRate = averageError;
 			}
 		}
 
 		return new ImmutableTwoValues<Integer, Map<Integer, Double>>(bestNumberOfFeatures, averageErrorRates);
 	}
 	
 
 	static ImmutableTwoValues<List<String>, Double> validateSubset(String externalSubsampleTrainingSet,
 			int numberOfFeatures, Map<String, Double> weights, TreeGrowthControl variableImportanceControl,
 			List<String> fullFeatureSet, int validationIterations)
 	{
 		return validateSubset(externalSubsampleTrainingSet, "", numberOfFeatures, weights, variableImportanceControl,
 				fullFeatureSet, validationIterations, true);
 	}
 
 	static ImmutableTwoValues<List<String>, Double> validateSubset(String externalSubsampleTrainingSet, String externalSubsampleTestingSet,
 			int numberOfFeatures, Map<String, Double> weights, TreeGrowthControl variableImportanceControl,
 			List<String> fullFeatureSet, int validationIterations, boolean isValidationSkipped)
 	{
 		Random seedGenerator = new Random();
 		List<Long> seedsToUse = new ArrayList<Long>();
 		for (int i = 0; i < validationIterations; i++)
 		{
 			long newSeed = seedGenerator.nextLong();
 			while (seedsToUse.contains(newSeed))
 			{
 				newSeed = seedGenerator.nextLong();
 			}
 			seedsToUse.add(newSeed);
 		}
 		//--------------------------------------------------------------------//
 		// Determine the order of the features by feature importance ranking. //
 		//--------------------------------------------------------------------//
 		Map<String, List<Integer>> importanceRanking = new HashMap<String, List<Integer>>();
 		for (String s : fullFeatureSet)
 		{
 			importanceRanking.put(s, new ArrayList<Integer>());
 		}
 		for (int i = 0; i < validationIterations; i++)
 		{
 			Forest forest = new Forest(externalSubsampleTrainingSet, variableImportanceControl, weights, seedsToUse.get(i));
 			Map<String, Double> varImp = forest.variableImportance();
 			List<StringsSortedByDoubles> sortedVariables = new ArrayList<StringsSortedByDoubles>();
 			for (String s : varImp.keySet())
 			{
 				sortedVariables.add(new StringsSortedByDoubles(varImp.get(s), s));
 			}
			Collections.sort(sortedVariables, Collections.reverseOrder());  // Larger importance first.
 			for (int j = 0; j < varImp.size(); j++)
 			{
				String feature = sortedVariables.get(j).getId();
				importanceRanking.get(feature).add(j + 1);
 			}
 		}
 
 		// Rank the variables by importance.
 		List<StringsSortedByDoubles> sortedVariables = new ArrayList<StringsSortedByDoubles>();
 		for (String s : importanceRanking.keySet())
 		{
 			double averageRank = 0.0;
 			for (Integer i : importanceRanking.get(s))
 			{
 				averageRank += i;
 			}
 			averageRank /= validationIterations;
 			sortedVariables.add(new StringsSortedByDoubles(averageRank, s));
 		}
 		Collections.sort(sortedVariables, Collections.reverseOrder());  // Larger importance first.
 		List<String> orderedFeaturesByImportance = new ArrayList<String>();
 		for (StringsSortedByDoubles ssbd : sortedVariables)
 		{
 			orderedFeaturesByImportance.add(ssbd.getId());
 		}
 
 		//---------------------------//
 		// Select the best features. //
 		//---------------------------//
 		List<String> bestFeatures = new ArrayList<String>();
 		for (int i = 0; i < numberOfFeatures; i++)
 		{
 			bestFeatures.add(orderedFeaturesByImportance.get(i));
 		}
 		List<String> variablesToIgnore = new ArrayList<String>(fullFeatureSet);
 		variablesToIgnore.removeAll(bestFeatures);
 		variableImportanceControl.variablesToIgnore = variablesToIgnore;
 		double validatedErrorRate = 0.0;
 		if (!isValidationSkipped)
 		{
 			for (int i = 0; i < validationIterations; i++)
 			{
 				Forest forest = new Forest(externalSubsampleTrainingSet, variableImportanceControl, weights, seedsToUse.get(i));
 				validatedErrorRate += forest.predict(new ProcessDataForGrowing(externalSubsampleTestingSet, variableImportanceControl)).first;
 			}
 		}
 		validatedErrorRate /= validationIterations;
 		return new ImmutableTwoValues<List<String>, Double>(bestFeatures, validatedErrorRate);
 	}
 
 
 	static void removeDirectoryContent(File directory)
 	{
 		String directoryLocation = directory.getAbsolutePath();
 		if (directory.isDirectory())
 		{
 			String dirFiles[] = directory.list();
 			for (String s : dirFiles)
 			{
 				File subFile = new File(directoryLocation + "/" + s);
 				if (subFile.isDirectory())
 				{
 					removeDirectoryContent(subFile);
 				}
 				else
 				{
 					subFile.delete();
 				}
 			}
 		}
 		directory.delete();
 	}
 }
