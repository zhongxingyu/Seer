 package datasetgeneration;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 	public class CrossValidationFoldGeneration
 	{
 
 		/**
 		 * @param args
 		 */
 		public static void main(String[] args)
 		{
 			String inputFileLocation = args[0];
 			String outputLocation = args[1];
 			int numberOfFolds = Integer.parseInt(args[2]);
 
 			List<String> positiveObservations = new ArrayList<String>();
 			List<String> unlabelledObservations = new ArrayList<String>();
 
 			String headerOne = "";
 			String headerTwo = "";
 			String headerThree = "";
 			// Sort the observations into the positive and unlabelled lists.
 			try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFileLocation), StandardCharsets.UTF_8))
 			{
 				// Knock off the first three header lines.
 				headerOne = reader.readLine();
 				int indexOfClassification = headerOne.split("\t").length - 1;
 				headerTwo = reader.readLine();
 				headerThree = reader.readLine();
 
 				String line;
 				while ((line = reader.readLine()) != null)
 				{
 					String[] splitLine = line.split("\t");
 					if (splitLine[indexOfClassification].equals("Positive"))
 					{
 						positiveObservations.add(line);
 					}
 					else
 					{
 						unlabelledObservations.add(line);
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 
 			// Determine how to split the dataset into folds.
 			int numberObservations = positiveObservations.size() + unlabelledObservations.size();
 			int numberPositives = positiveObservations.size();
 			int numberUnlabelled = unlabelledObservations.size();
 			int minPosObsInFold = numberPositives / numberOfFolds;  // Determine the minimum number of positive observations in a fold.
 			int minUnlabObsInFold = numberUnlabelled / numberOfFolds;  // Determine the minimum number of unlabelled observations in a fold.
 			// Determine the indices between which to select the observations.
 			List<Integer> positiveObsIndices = new ArrayList<Integer>();
 			List<Integer> unlabelledObsIndices = new ArrayList<Integer>();
 			for (int i = 0; i <= numberOfFolds; i++)
 			{
 				positiveObsIndices.add(i * minPosObsInFold);
 				unlabelledObsIndices.add(i * minUnlabObsInFold);
 			}
			// The number of observations left over is not 0 when the folds does not evenly divide into the number of observations (e.g. 5 folds 13 observations).
			int leftOverPosObs = numberObservations - (minPosObsInFold * numberOfFolds);
			int leftOverUnlabObs = numberObservations - (minPosObsInFold * numberOfFolds);
 
 			// Select the observations for each fold.
 			Collections.shuffle(positiveObservations);  // Shuffle the observations to randomise the observations in each fold.
 			Collections.shuffle(unlabelledObservations);  // Shuffle the observations to randomise the observations in each fold.
 			List<List<String>> datasets = new ArrayList<List<String>>();
 			for (int i = 0; i < numberOfFolds; i++)
 			{
 				List<String> currentFold = new ArrayList<String>();
 				currentFold.addAll(positiveObservations.subList(positiveObsIndices.get(i), positiveObsIndices.get(i + 1)));
 				currentFold.addAll(unlabelledObservations.subList(unlabelledObsIndices.get(i), unlabelledObsIndices.get(i + 1)));
 				datasets.add(currentFold);
 			}
 			// Add the left over observations if there are any.
 			if (leftOverPosObs != 0)
 			{
 				// There are left over positive observations.
 				int foldIndex = 0;
 				int indexOfFirstLeftOver = numberOfFolds * minPosObsInFold;
 				for (int i = indexOfFirstLeftOver; i < numberPositives; i++)
 				{
 					datasets.get(foldIndex).add(positiveObservations.get(i));
 				}
 			}
 			if (leftOverUnlabObs != 0)
 			{
 				// there are left over unlabelled observations.
 				int foldIndex = 0;
 				int indexOfFirstLeftOver = numberOfFolds * minUnlabObsInFold;
 				for (int i = indexOfFirstLeftOver; i < numberUnlabelled; i++)
 				{
 					datasets.get(foldIndex).add(unlabelledObservations.get(i));
 				}
 			}
 
 			// Write out the datasets.
 			File outputDirectory = new File(outputLocation);
 			if (!outputDirectory.exists())
 			{
 				boolean isDirCreated = outputDirectory.mkdirs();
 				if (!isDirCreated)
 				{
 					System.out.println("The output directory does not exist, but could not be created.");
 					System.exit(0);
 				}
 			}
 			else if (!outputDirectory.isDirectory())
 			{
 				// Exists and is not a directory.
 				System.out.println("The second argument must be a valid directory location or location where a directory can be created.");
 				System.exit(0);
 			}
 			for (int i = 0; i < numberOfFolds; i++)
 			{
 				String foldOutputLocation = outputLocation + "/" + Integer.toString(i);
 				File foldOutputDirectory = new File(foldOutputLocation);
 				if (!foldOutputDirectory.exists())
 				{
 					boolean isDirCreated = foldOutputDirectory.mkdirs();
 					if (!isDirCreated)
 					{
 						System.out.println("The fold output directory does not exists, but could not be created.");
 						System.exit(0);
 					}
 				}
 				else if (!foldOutputDirectory.isDirectory())
 				{
 					// Exists and is not a directory.
 					System.out.println("The fold directory location exists but is not a directory.");
 					System.exit(0);
 				}
 				String trainDataOutputLoc = foldOutputLocation + "/Train.txt";
 				String testDataOutputLoc = foldOutputLocation + "/Test.txt";
 				List<String> trainingData = new ArrayList<String>();
 				List<String> testData = new ArrayList<String>();
 				for (int j = 0; j < numberOfFolds; j++)
 				{
 					if (i != j)
 					{
 						trainingData.addAll(datasets.get(j));
 					}
 					else
 					{
 						testData.addAll(datasets.get(j));
 					}
 				}
 				try
 				{
 					// Write out the training data.
 					FileWriter trainDataOutputFile = new FileWriter(trainDataOutputLoc);
 					BufferedWriter trainDataOutputWriter = new BufferedWriter(trainDataOutputFile);
 					trainDataOutputWriter.write(headerOne);
 					trainDataOutputWriter.newLine();
 					trainDataOutputWriter.write(headerTwo);
 					trainDataOutputWriter.newLine();
 					trainDataOutputWriter.write(headerThree);
 					trainDataOutputWriter.newLine();
 					for (String s : trainingData)
 					{
 						trainDataOutputWriter.write(s);
 						trainDataOutputWriter.newLine();
 					}
 					trainDataOutputWriter.close();
 
 					// Write out the testing data.
 					FileWriter testDataOutputFile = new FileWriter(testDataOutputLoc);
 					BufferedWriter testDataOutputWriter = new BufferedWriter(testDataOutputFile);
 					testDataOutputWriter.write(headerOne);
 					testDataOutputWriter.newLine();
 					testDataOutputWriter.write(headerTwo);
 					testDataOutputWriter.newLine();
 					testDataOutputWriter.write(headerThree);
 					testDataOutputWriter.newLine();
 					for (String s : testData)
 					{
 						testDataOutputWriter.write(s);
 						testDataOutputWriter.newLine();
 					}
 					testDataOutputWriter.close();
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 					System.exit(0);
 				}
 			}
 		}
 
 	}
 
