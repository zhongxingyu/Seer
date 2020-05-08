 package featureselection;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import tree.TreeGrowthControl;
 
 public class GeneticAlgorithm {
 
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
 
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 		Integer[] trainingObsToUse = {};
 
 		TreeGrowthControl ctrl = new TreeGrowthControl();;
 		ctrl.isReplacementUsed = true;
 		ctrl.isStratifiedBootstrapUsed = true;
 		ctrl.numberOfTreesToGrow = 10;
 		ctrl.mtry = 10;
 		ctrl.minNodeSize = 1;
 		ctrl.trainingObservations = Arrays.asList(trainingObsToUse);
 
 		Map<String, Double> weights = new HashMap<String, Double>();
 		weights.put("Unlabelled", 1.0);
 		weights.put("Positive", 1.0);
 		//===================================================================
 		//==================== CONTROL PARAMETER SETTING ====================
 		//===================================================================
 
 		new chc.FeatureSelection(args, new TreeGrowthControl(ctrl), weights);
 
 		gaAnalysis(args[0], args[1], ctrl);
 	}
 
 	/**
 	 * @param inputLocation
 	 * @param resultDirLoc - directory containing results of the GA run
 	 * @param ctrl - the TreeGrowthCotrol object used to run the GA (or an equivalent one)
 	 */
 	static void gaAnalysis(String inputLocation, String resultsDirLoc, TreeGrowthControl ctrl)
 	{
 
 		File inputFile = new File(inputLocation);
 		if (!inputFile.isFile())
 		{
 			System.out.println("The first argument must be a valid file location, and must contain the data for feature selection.");
 			System.exit(0);
 		}
 
 		File outputDirectory = new File(resultsDirLoc);
 		if (!outputDirectory.isDirectory())
 		{
 			System.out.println("The second argument must be a valid directory location.");
 			System.exit(0);
 		}
 
 		// Get the best individuals from the GA runs.
 		List<List<String>> bestIndividuals = new ArrayList<List<String>>();
 		try (BufferedReader reader = Files.newBufferedReader(Paths.get(resultsDirLoc + "/BestConvergenceIndividuals.txt"), StandardCharsets.UTF_8))
 		{
 			String line;
 			line = reader.readLine();  // Strip the header.
 			while ((line = reader.readLine()) != null)
 			{
 				line = line.trim();
 				line = line.replace("[", "");
 				line = line.replace("]", "");
 				line = line.replace(" ", "");
 				if (line.trim().length() == 0)
 				{
 					// If the line is made up of all whitespace, then ignore the line.
 					continue;
 				}
 
 				List<String> individual = new ArrayList<String>();
 				String[] splitLine = line.split("\t");
				for (String p : splitLine[3].split(","))
 				{
 					individual.add(p);
 				}
 				bestIndividuals.add(individual);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
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
 
 		// Determine the features that are used in the GAs.
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
 
 		// Generate the output matrix.
 		Map<String, Integer> featureSums = new HashMap<String, Integer>();
 		try
 		{
 			String matrixOutputLocation = resultsDirLoc + "/MatrixOutput.txt";
 			FileWriter matrixOutputFile = new FileWriter(matrixOutputLocation);
 			BufferedWriter matrixOutputWriter = new BufferedWriter(matrixOutputFile);
 			for (String s : featuresUsed)
 			{
 				// Write out the feature name.
 				matrixOutputWriter.write(s);
 				matrixOutputWriter.write("\t");
 				// Write out the presence (1)/absence (0) of the feature for the given run, and sum up the occurrences
 				// of the feature in the runs.
 				int featureOccurreces = 0;
 				for (List<String> l : bestIndividuals)
 				{
 					int featurePresence = 0;
 					if (l.contains(s))
 					{
 						featurePresence = 1;
 					}
 					featureOccurreces += featurePresence;
 					matrixOutputWriter.write(Integer.toString(featurePresence));
 					matrixOutputWriter.write("\t");
 				}
 				double featureFractions = ((double) featureOccurreces) / bestIndividuals.size();
 				matrixOutputWriter.write(Double.toString(featureFractions));
 				matrixOutputWriter.newLine();
 				featureSums.put(s, featureOccurreces);
 			}
 			matrixOutputWriter.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		// Generate the feature subsets for features that occur in 10%, 20%, ..., 100% of the best individuals.
 		for (double d = 1; d < 11; d++)
 		{
 			double featureFraction = d / 10.0;
 			int numberOfOccurences = (int) Math.floor(featureFraction * bestIndividuals.size());  // Determine the number of times a feature must occur for it to be included in the feature set.
 			List<String> featureSet = new ArrayList<String>();
 			for (String s : featuresUsed)
 			{
 				if (featureSums.get(s) >= numberOfOccurences)
 				{
 					// If the ith feature occurred enough times.
 					featureSet.add(s);
 				}
 			}
 			// Write out the feature set.
 			try
 			{
 				String featureSetOutputLocation = resultsDirLoc + "/" + Integer.toString((int) (featureFraction * 100)) + "%FeatureSet.txt";
 				FileWriter featureSetOutputFile = new FileWriter(featureSetOutputLocation);
 				BufferedWriter featureSetOutputWriter = new BufferedWriter(featureSetOutputFile);
 				for (String s : featureSet)
 				{
 					featureSetOutputWriter.write(s);
 					featureSetOutputWriter.newLine();
 				}
 				featureSetOutputWriter.close();
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 	}
 
 }
