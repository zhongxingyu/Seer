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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import datasetgeneration.BootstrapGeneration;
 import datasetgeneration.CrossValidationFoldGenerationMultiClass;
 
 import tree.Forest;
 import tree.ImmutableThreeValues;
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
 		String entireDatasetVarImpLocation = args[2];  // The location for the repetitions of the variable importance calculations for the entire dtaset.
 		File varImpFile = new File(entireDatasetVarImpLocation);
 		if (!varImpFile.isFile())
 		{
 			System.out.println("The third argument must be a valid file location, and must contain the variable importances calculated for the entire dataset.");
 			System.exit(0);
 		}
 
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 		int externalSubsamplesToGenerate = 100;
		double fractionToReserveAsValidation = 0.1;
 		int internalSubsamplesToGenerate = 10;
		int validationIterations = 1;
 		double fractionToElim = 0.02;  // Eliminating a fraction allows you to remove lots of variables when there are lots remaining, and get better resolution when there are few remaining.
 		boolean continueRun = false;  // Whether or not you want to continue a run in progress or restart the whole process.
 		Integer[] trainingObsToUse = {};
 
 		TreeGrowthControl ctrl = new TreeGrowthControl();
 		ctrl.isReplacementUsed = true;
 		ctrl.numberOfTreesToGrow = 500;
 		ctrl.mtry = 10;
 		ctrl.isStratifiedBootstrapUsed = true;
 		ctrl.isCalculateOOB = false;
 		ctrl.minNodeSize = 1;
 		ctrl.trainingObservations = Arrays.asList(trainingObsToUse);
 
 		TreeGrowthControl varImpCtrl = new TreeGrowthControl(ctrl);
		varImpCtrl.numberOfTreesToGrow = 500;
 		varImpCtrl.trainingObservations = Arrays.asList(trainingObsToUse);
 
 		Map<String, Double> weights = new HashMap<String, Double>();
 		weights.put("Unlabelled", 1.0);
 		weights.put("Positive", 1.0);
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
 		String subsetSizeGMeans = outputLocation + "/GMeans.txt";
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
 				featureFractionsOutputWriter.write("GMean\tErrorRate");
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
 				String featureNumberHeader = "";
 				double featuresToEliminate;
 				while (numberOfFeaturesRemaining > 0)
 				{
 					featureNumberHeader += Integer.toString(numberOfFeaturesRemaining) + "\t";
 					featuresToEliminate = (int) Math.ceil(numberOfFeaturesRemaining * fractionToElim);
 					numberOfFeaturesRemaining -= featuresToEliminate;
 				}
 				featureNumberHeader = featureNumberHeader.substring(0, featureNumberHeader.length() - 1);
 				errorRateOutputWriter.write(featureNumberHeader);
 				errorRateOutputWriter.newLine();
 				errorRateOutputWriter.close();
 				FileWriter gMeanOutputFile = new FileWriter(subsetSizeGMeans);
 				BufferedWriter gMeanOutputWriter = new BufferedWriter(gMeanOutputFile);
 				gMeanOutputWriter.write(featureNumberHeader);
 				gMeanOutputWriter.newLine();
 				gMeanOutputWriter.close();
 
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
 
 			ImmutableThreeValues<Integer, Map<Integer, Double>, Map<Integer, Double>> internalSelectionResults = internalSelection(subsampleTrainingSet,
 					internalFoldDirLoc, internalSubsamplesToGenerate, weights, ctrl, varImpCtrl, featuresUsed, fractionToElim);
 			int bestNumberOfFeatures = internalSelectionResults.first;
 			Map<Integer, Double> averageErrorRates = internalSelectionResults.second;
 			Map<Integer, Double> averageGMeans = internalSelectionResults.third;
 
 			// Determine and validate best feature subset.
 			currentTime = new Date();
 		    sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		    strDate = sdfDate.format(currentTime);
 			System.out.format("\tNow validating the feature set at %s.\n", strDate);
 			ImmutableThreeValues<List<String>, Double, Double> validationResults = validateSubset(subsampleTrainingSet, subsampleTestingSet,
 					bestNumberOfFeatures, weights, new TreeGrowthControl(varImpCtrl), featuresUsed, validationIterations);
 			List<String> bestFeatureSet = validationResults.first;
 			double validatedError = validationResults.second;
 			double validatedGMean = validationResults.third;
 
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
 				featureFractionsOutputWriter.write(Double.toString(validatedGMean));
 				featureFractionsOutputWriter.write("\t");
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
 				FileWriter gMeanOutputFile = new FileWriter(subsetSizeGMeans, true);
 				BufferedWriter gMeanOutputWriter = new BufferedWriter(gMeanOutputFile);
 				String errorOutput = "";
 				String gMeanOutput = "";
 				for (Integer j : featureSetSizes)
 				{
 					errorOutput += Double.toString(averageErrorRates.get(j)) + "\t";
 					gMeanOutput += Double.toString(averageGMeans.get(j)) + "\t";
 				}
 				errorOutput.substring(0, errorOutput.length() - 1);
 				errorRateOutputWriter.write(errorOutput);
 				errorRateOutputWriter.newLine();
 				errorRateOutputWriter.close();
 				gMeanOutput.substring(0, gMeanOutput.length() - 1);
 				gMeanOutputWriter.write(gMeanOutput);
 				gMeanOutputWriter.newLine();
 				gMeanOutputWriter.close();
 				
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
 		String finalSelectionGMeans = finalSelectionOutputLoc + "/GMeans.txt";
 		String finalSelectionResults = finalSelectionOutputLoc + "/Results.txt";
 
 		ImmutableThreeValues<Integer, Map<Integer, Double>, Map<Integer, Double>> internalSelectionResults = internalSelection(inputLocation,
 				finalSelectionOutputLoc, internalSubsamplesToGenerate, weights, ctrl, varImpCtrl, featuresUsed, 0.0001);  // Use 0.0001 as the feature elimination fraction to ensure that only one feature is eliminated per iteraton.
 		int bestNumberOfFeatures = internalSelectionResults.first;
 		Map<Integer, Double> averageErrorRates = internalSelectionResults.second;
 		Map<Integer, Double> averageGMeans = internalSelectionResults.third;
 
 		// Determine and validate best feature subset.
 		currentTime = new Date();
 	    sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	    strDate = sdfDate.format(currentTime);
 		System.out.format("\tNow validating the feature set at %s.\n", strDate);
 
 		List<StringsSortedByDoubles> sortedVariables = new ArrayList<StringsSortedByDoubles>();
 		try (BufferedReader reader = Files.newBufferedReader(Paths.get(entireDatasetVarImpLocation), StandardCharsets.UTF_8))
 		{
			String line;
 			while ((line = reader.readLine()) != null)
 			{
 				line = line.replaceAll("\n", "");
 				String[] chunks = line.split("\t");
 				double averageRank = 0.0;
				for (int i = 1; i < chunks.length; i++)
 				{
 					averageRank += Integer.parseInt(chunks[i]);
 				}
 				averageRank /= (chunks.length - 1);
 				sortedVariables.add(new StringsSortedByDoubles(averageRank, chunks[0]));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 		Collections.sort(sortedVariables);  // Smaller ranks first.
 		List<String> orderedFeaturesByImportance = new ArrayList<String>();
 		for (StringsSortedByDoubles ssbd : sortedVariables)
 		{
 			orderedFeaturesByImportance.add(ssbd.getId());
 		}
 		List<String> bestFeatureSet = new ArrayList<String>();
 		for (int i = 0; i < bestNumberOfFeatures; i++)
 		{
 			bestFeatureSet.add(orderedFeaturesByImportance.get(i));
 		}
 
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
 
 			featureSetSizes = new ArrayList<Integer>(averageGMeans.keySet());
 			Collections.sort(featureSetSizes, Collections.reverseOrder());
 			FileWriter gMeanOutputFile = new FileWriter(finalSelectionGMeans, true);
 			BufferedWriter gMeanOutputWriter = new BufferedWriter(gMeanOutputFile);
 			String gMeanOutput = "";
 			for (Integer j : featureSetSizes)
 			{
 				gMeanOutput += Double.toString(averageGMeans.get(j)) + "\t";
 			}
 			gMeanOutput.substring(0, gMeanOutput.length() - 1);
 			gMeanOutputWriter.write(gMeanOutput);
 			gMeanOutputWriter.newLine();
 			gMeanOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 
 	static ImmutableTwoValues<Map<Integer, Double>, Map<Integer, Double>> internalEvaluation(String internalSubsampleTrainingSet,
 			String internalSubsampleTestingSet, long internalSubsampleSeed, Map<String, Double> weights, TreeGrowthControl eliminationControl,
 			TreeGrowthControl variableImportanceControl, List<String> fullFeatureSet, double fractionToElim)
 	{
 		ProcessDataForGrowing processedInputData = new ProcessDataForGrowing(internalSubsampleTestingSet, eliminationControl);
 		Map<String, Integer> classCounts = new HashMap<String, Integer>();
 		for (String s : new HashSet<String>(processedInputData.responseData))
 		{
 			classCounts.put(s, Collections.frequency(processedInputData.responseData, s));
 		}
 
 		//--------------------------------------------------------------------//
 		// Determine the order of the features by feature importance ranking. //
 		//--------------------------------------------------------------------//
 		Forest forest = new Forest(internalSubsampleTrainingSet, variableImportanceControl, internalSubsampleSeed);
 		forest.setWeightsByClass(weights);
 		forest.growForest();
 		Map<String, Double> varImp = forest.variableImportance().second;
 
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
 		Map<Integer, Double> gMeans = new HashMap<Integer, Double>();
 		int featuresToEliminate;
 		while (!orderedFeaturesByImportance.isEmpty())
 		{
 			List<String> variablesToIgnore = new ArrayList<String>(fullFeatureSet);
 			variablesToIgnore.removeAll(orderedFeaturesByImportance);
 			eliminationControl.variablesToIgnore = variablesToIgnore;
 			forest = new Forest(internalSubsampleTrainingSet, eliminationControl, internalSubsampleSeed);
 			forest.setWeightsByClass(weights);
 			forest.growForest();
 			ImmutableTwoValues<Double, Map<String, Map<String, Double>>> predictionResults = forest.predict(new ProcessDataForGrowing(internalSubsampleTestingSet, eliminationControl));
 			errorRates.put(orderedFeaturesByImportance.size(), predictionResults.first);
 			Map<String, Map<String, Double>> confusionMatrix = predictionResults.second;
 			// Determine the macro G mean.
 	    	double macroGMean = 1.0;
 	    	for (String s : confusionMatrix.keySet())
 	    	{
 	    		double TP = confusionMatrix.get(s).get("TruePositive");
 	    		double FN = classCounts.get(s) - TP;  // The number of false positives is the number of observations from the class in the test set - the number of true positives.
 	    		double recall = TP / (TP + FN);
 	    		macroGMean *= recall;
 	    	}
 	    	gMeans.put(orderedFeaturesByImportance.size(), Math.pow(macroGMean, (1.0 / classCounts.size())));
 			featuresToEliminate = (int) Math.ceil(orderedFeaturesByImportance.size() * fractionToElim);
 			for (int j = 0; j < featuresToEliminate; j++)
 			{
 				orderedFeaturesByImportance.remove(orderedFeaturesByImportance.size() - 1);
 			}
 		}
 
 		return new ImmutableTwoValues<Map<Integer, Double>, Map<Integer, Double>>(errorRates, gMeans);
 	}
 
 
 	static ImmutableThreeValues<Integer, Map<Integer, Double>, Map<Integer, Double>> internalSelection(String entireTrainingSet,
 			String locatonForInternalFolds, int internalSubsamplesToGenerate, Map<String, Double> weights, TreeGrowthControl ctrl,
 			TreeGrowthControl varImpCtrl, List<String> featuresUsed, double fractionToElim)
 	{
 		ctrl.variablesToIgnore = new ArrayList<String>();
 		CrossValidationFoldGenerationMultiClass.main(entireTrainingSet, locatonForInternalFolds, internalSubsamplesToGenerate);
 
 		List<Map<Integer, Double>> errorRates = new ArrayList<Map<Integer, Double>>();
 		List<Map<Integer, Double>> gMeans = new ArrayList<Map<Integer, Double>>();
 		List<Long> usedSeeds = new ArrayList<Long>();
 		Random seedGenerator = new Random();
 		for (int j = 0; j < internalSubsamplesToGenerate; j++)
 		{
 			Date currentTime = new Date();
 		    DateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		    String strDate = sdfDate.format(currentTime);
 			System.out.format("\tNow performing elimination for CV fold %d at %s.\n", j, strDate);
 			long seedToUse = seedGenerator.nextLong();  // Determine the seed to use for the feature selection process on this bootstrap sample.
 			while (usedSeeds.contains(seedToUse))
 			{
 				seedToUse = seedGenerator.nextLong();
 			}
 			usedSeeds.add(seedToUse);
 			String internalFoldTrainingSet = locatonForInternalFolds + "/" + Integer.toString(j) + "/Train.txt";
 			String internalFoldTestingSet = locatonForInternalFolds + "/" + Integer.toString(j) + "/Test.txt";
 			ImmutableTwoValues<Map<Integer, Double>, Map<Integer, Double>> internalEvalResults = internalEvaluation(internalFoldTrainingSet,
 					internalFoldTestingSet, seedToUse, weights, new TreeGrowthControl(ctrl), varImpCtrl, featuresUsed, fractionToElim);
 			errorRates.add(internalEvalResults.first);
 			gMeans.add(internalEvalResults.second);
 		}
 
 		// Determine the average error rate and gMean for each size of feature subset, and the best feature set size.
 		Map<Integer, Double> averageErrorRates = new HashMap<Integer, Double>();
 		Map<Integer, Double> averageGMeans = new HashMap<Integer, Double>();
 		int bestNumberOfFeatures = featuresUsed.size();
 		double bestGMean = 0.0;
 		List<Integer> featureSetSizes = new ArrayList<Integer>(errorRates.get(0).keySet());
 		Collections.sort(featureSetSizes, Collections.reverseOrder());
 		for (Integer j : featureSetSizes)
 		{
 			double averageError = 0.0;
 			double averageGMean = 0.0;
 			for (int k = 0; k < internalSubsamplesToGenerate; k++)
 			{
 				averageError += errorRates.get(k).get(j);
 				averageGMean += gMeans.get(k).get(j);
 			}
 			averageError /= internalSubsamplesToGenerate;
 			averageErrorRates.put(j, averageError);
 			averageGMean /= internalSubsamplesToGenerate;
 			averageGMeans.put(j, averageGMean);
 			if (averageGMean > bestGMean)
 			{
 				bestNumberOfFeatures = j;
 				bestGMean = averageGMean;
 			}
 		}
 
 		return new ImmutableThreeValues<Integer, Map<Integer, Double>, Map<Integer, Double>>(bestNumberOfFeatures, averageErrorRates, averageGMeans);
 	}
 	
 
 	static ImmutableThreeValues<List<String>, Double, Double> validateSubset(String externalSubsampleTrainingSet, String externalSubsampleTestingSet,
 			int numberOfFeatures, Map<String, Double> weights, TreeGrowthControl variableImportanceControl,
 			List<String> fullFeatureSet, int validationIterations)
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
 
 		ProcessDataForGrowing processedInputData = new ProcessDataForGrowing(externalSubsampleTestingSet, variableImportanceControl);
 		Map<String, Integer> classCounts = new HashMap<String, Integer>();
 		for (String s : new HashSet<String>(processedInputData.responseData))
 		{
 			classCounts.put(s, Collections.frequency(processedInputData.responseData, s));
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
 			Forest forest = new Forest(externalSubsampleTrainingSet, variableImportanceControl, seedsToUse.get(i));
 			forest.setWeightsByClass(weights);
 			forest.growForest();
 			Map<String, Double> varImp = forest.variableImportance().second;
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
 		Collections.sort(sortedVariables);  // Smaller ranks first.
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
 		double validatedGMean = 0.0;
 		for (int i = 0; i < validationIterations; i++)
 		{
 			Forest forest = new Forest(externalSubsampleTrainingSet, variableImportanceControl, seedsToUse.get(i));
 			forest.setWeightsByClass(weights);
 			forest.growForest();
 			ImmutableTwoValues<Double, Map<String, Map<String, Double>>> validationResults = forest.predict(new ProcessDataForGrowing(externalSubsampleTestingSet, variableImportanceControl));
 			validatedErrorRate += validationResults.first;
 			// Determine the macro G mean.
 			Map<String, Map<String, Double>> confusionMatrix = validationResults.second;
 	    	double macroGMean = 1.0;
 	    	for (String s : confusionMatrix.keySet())
 	    	{
 	    		double TP = confusionMatrix.get(s).get("TruePositive");
 	    		double FN = classCounts.get(s) - TP;  // The number of false positives is the number of observaitons from the class - the number of true positives.
 	    		double recall = TP / (TP + FN);
 	    		macroGMean *= recall;
 	    	}
 	    	validatedGMean += Math.pow(macroGMean, (1.0 / classCounts.size()));
 		}
 		validatedErrorRate /= validationIterations;
 		validatedGMean /= validationIterations;
 		return new ImmutableThreeValues<List<String>, Double, Double>(bestFeatures, validatedErrorRate, validatedGMean);
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
