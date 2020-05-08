 package finalclassification;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import randomjyrest.DetermineObservationProperties;
 import randomjyrest.Forest;
 import utilities.ImmutableTwoValues;
 
 public class Main
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 		int numberOfTrees = 1000;  // Number of trees in the forest.
 		long seed = 0L;  // The seed for growing the forest.
 
 		int mtry = 10;  // The number of features to consider at each split in a tree.
 		
 		// Specify the features in the input dataset that should be ignored.
		String[] featuresToUse = new String[]{};
		List<String> featuresToRemove = Arrays.asList(featuresToUse);
 		
 		int numberOfThreads = 1;  // The number of threads to use when growing the trees.
 
 		// Define the weights for each class in the input dataset.
 		Map<String, Double> classWeights = new HashMap<String, Double>();
 		classWeights.put("Positive", 1.0);
 		classWeights.put("Unlabelled", 1.0);
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 		
 		// Mandatory parameters for growing the forest.
 		boolean isCalcualteOOB = true;
 
 		// Parse the input arguments.
 		String trainingDataset = args[0];  // The dataset that is to be used to grow the forest.
 		String resultsDirLocation = args[1];  // The location where the results will be written.
 		String testingDataset = null;  // The optional dataset that will be predicted.
 		if (args.length > 2)
 		{
 			testingDataset = args[2];
 		}
 		File resultsDirectory = new File(resultsDirLocation);
 		if (!resultsDirectory.exists())
 		{
 			boolean isDirCreated = resultsDirectory.mkdirs();
 			if (!isDirCreated)
 			{
 				System.out.println("The output directory could not be created.");
 				System.exit(0);
 			}
 		}
 		else
 		{
 			System.out.println("The results directory already exists.");
 			System.exit(0);
 		}
 
 		// Write out the parameters used.
 		String parameterLocation = resultsDirLocation + "/Parameters.txt";
 		try
 		{
 			FileWriter parameterOutputFile = new FileWriter(parameterLocation);
 			BufferedWriter parameterOutputWriter = new BufferedWriter(parameterOutputFile);
 			parameterOutputWriter.write("Number of trees - " + Integer.toString(numberOfTrees));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Weights used");
 			parameterOutputWriter.newLine();
 			for (String s : classWeights.keySet())
 			{
 				parameterOutputWriter.write(s + "\t" + Double.toString(classWeights.get(s)));
 				parameterOutputWriter.newLine();
 			}
 			parameterOutputWriter.write("Mtry used - " + Integer.toString(mtry));
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Features - " + featuresToRemove.toString());
 			parameterOutputWriter.newLine();
 			parameterOutputWriter.write("Seed - " + Long.toString(seed));
 			parameterOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		// Determine the weights for the training observatons.
 		List<String> classOfTrainingObservations = DetermineObservationProperties.determineObservationClasses(trainingDataset);
 		double[] trainingSetWeights = DetermineObservationProperties.determineObservationWeights(classOfTrainingObservations, "Positive",
 				classWeights.get("Positive"), "Unlabelled", classWeights.get("Unlabelled"));
 		
 		// Determine the predictions.
 		Forest forest = new Forest();
 		Map<String, double[]> oobPredictions = forest.main(trainingDataset, numberOfTrees, mtry, featuresToRemove, trainingSetWeights,
 				seed, numberOfThreads, isCalcualteOOB);
		Map<String, double[]> testSetPredictions = forest.predict(testingDataset, featuresToRemove);
 		
 		// Define the names of the class and UniProt accession columns.
 		String classFeatureColumnName = "Classification";
 		String accessionColumnName = "UPAccession";
 
 		// Determine the accessions and classes of the proteins in the training dataset.
 		ImmutableTwoValues<List<String>, List<String>> accessionsAndClasses = determineAccessionsAndClasses(trainingDataset,
 				accessionColumnName, classFeatureColumnName);
 		List<String> trainingDatasetAccessions = accessionsAndClasses.first;
 		List<String> trainingDatasetClasses = accessionsAndClasses.second;
 		
 		List<String> testingDatasetAccessions = new ArrayList<String>();
 		List<String> testingDatasetClasses = new ArrayList<String>();
 		if (testingDataset != null)
 		{
 			// If a testing dataset has been supplied, then determine the accessions and classes of the proteins in the training dataset.
 			accessionsAndClasses = determineAccessionsAndClasses(testingDataset, accessionColumnName, classFeatureColumnName);
 			testingDatasetAccessions = accessionsAndClasses.first;
 			testingDatasetClasses = accessionsAndClasses.second;
 		}
 
 		// Write out the protein accessions and their predictions.
 		String predictionResultsLocation = resultsDirLocation + "/Predictions.txt";
 		try
 		{
 			FileWriter proteinPredictionFile = new FileWriter(predictionResultsLocation);
 			BufferedWriter proteinPredictionWriter = new BufferedWriter(proteinPredictionFile);
 			proteinPredictionWriter.write("UPAccession\tPositiveWeight\tUnlabelledWeight\tOriginalClass");
 			proteinPredictionWriter.newLine();
 			
 			for (int i = 0; i < trainingDatasetAccessions.size(); i++)
 			{
 				String acc = trainingDatasetAccessions.get(i);
 				String originalClass = trainingDatasetClasses.get(i);
 				double posPredictionWeight = oobPredictions.get("Positive")[i];
 				double unlabPredictionWeight = oobPredictions.get("Unlabelled")[i];
 				proteinPredictionWriter.write(acc);
 				proteinPredictionWriter.write("\t");
 				proteinPredictionWriter.write(Double.toString(posPredictionWeight));
 				proteinPredictionWriter.write("\t");
 				proteinPredictionWriter.write(Double.toString(unlabPredictionWeight));
 				proteinPredictionWriter.write("\t");
 				proteinPredictionWriter.write(originalClass);
 				proteinPredictionWriter.newLine();
 			}
 			
 			for (int i = 0; i < testingDatasetAccessions.size(); i++)
 			{
 				String acc = testingDatasetAccessions.get(i);
 				String originalClass = testingDatasetClasses.get(i);
 				double posPredictionWeight = testSetPredictions.get("Positive")[i];
 				double unlabPredictionWeight = testSetPredictions.get("Unlabelled")[i];
 				proteinPredictionWriter.write(acc);
 				proteinPredictionWriter.write("\t");
 				proteinPredictionWriter.write(Double.toString(posPredictionWeight));
 				proteinPredictionWriter.write("\t");
 				proteinPredictionWriter.write(Double.toString(unlabPredictionWeight));
 				proteinPredictionWriter.write("\t");
 				proteinPredictionWriter.write(originalClass);
 				proteinPredictionWriter.newLine();
 			}
 			
 			proteinPredictionWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 	
 	
 	/**
 	 * @param dataset
 	 * @param accessionColumnName
 	 * @param classFeatureColumnName
 	 */
 	private static final ImmutableTwoValues<List<String>, List<String>> determineAccessionsAndClasses(String dataset,
 			String accessionColumnName, String classFeatureColumnName)
 	{
 		List<String> proteinAccessions = new ArrayList<String>();
 		List<String> proteinClasses = new ArrayList<String>();
 		BufferedReader reader = null;
 		try
 		{
 			reader = new BufferedReader(new FileReader(dataset));
 			
 			// Determine the class and accession column indices.
 			String[] features = reader.readLine().trim().split("\t");
 			int classColumnIndex = -1;
 			int accessionColumnIndex = -1;
 			for (int i = 0; i < features.length; i++)
 			{
 				String feature = features[i];
 				if (feature.equals(classFeatureColumnName))
 				{
 					classColumnIndex = i;
 				}
 				else if (feature.equals(accessionColumnName))
 				{
 					accessionColumnIndex = i;
 				}
 			}
 			
 			if (classColumnIndex == -1)
 			{
 				// No class column was provided.
 				System.out.println("No class column was provided. Please include a column headed Classification.");
 				System.exit(0);
 			}
 			else if (accessionColumnIndex == -1)
 			{
 				// No class column was provided.
 				System.out.println("No UniProt accession column was provided. Please include a column headed UPAccession.");
 				System.exit(0);
 			}
 			
 			String line = null;
 			while ((line = reader.readLine()) != null)
 			{
 				if (line.trim().length() == 0)
 				{
 					// If the line is made up of all whitespace, then ignore the line.
 					continue;
 				}
 				line = line.trim();
 				String[] splitLine = line.split("\t");
 				proteinAccessions.add(splitLine[accessionColumnIndex]);
 				proteinClasses.add(splitLine[classColumnIndex]);
 			}
 		}
 		catch (IOException e)
 		{
 			// Caught an error while reading the file. Indicate this and exit.
 			System.out.println("An error occurred while determining the accessions of the training dataset proteins.");
 			e.printStackTrace();
 			System.exit(0);
 		}
 		finally
 		{
 			try
 			{
 				if (reader != null)
 				{
 					reader.close();
 				}
 			}
 			catch (IOException e)
 			{
 				// Caught an error while closing the file. Indicate this and exit.
 				System.out.println("An error occurred while closing the training dataset.");
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 		
 		return new ImmutableTwoValues<List<String>, List<String>>(proteinAccessions, proteinClasses);
 	}
 
 }
