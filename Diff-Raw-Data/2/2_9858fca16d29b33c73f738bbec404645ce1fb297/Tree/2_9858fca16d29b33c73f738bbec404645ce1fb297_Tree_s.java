 package randomjyrest;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import utilities.ImmutableFourValues;
 import utilities.ImmutableTwoValues;
 
 public class Tree
 {
 	
 	private Node tree;
 	
 	public final void main(Map<String, double[]> dataset, Map<String, int[]> dataIndices, Map<String, double[]> classData,
 			int[] inBagObservations, int mtry, Random treeRNG, int numberOfUniqueObservations)
 	{
 		// Determine the features in the dataset.
 		this.tree = this.growTree(dataset, dataIndices, classData, inBagObservations, mtry, treeRNG, numberOfUniqueObservations);
 	}
 
 	private final Node growTree(Map<String, double[]> dataset, Map<String, int[]> dataIndices, Map<String, double[]> classData,
 			int[] inBagObservations, int mtry, Random treeRNG, int numberOfUniqueObservations)
 	{
 		Set<String> classesPresent = this.classesPresent(classData, inBagObservations);
 		
 		// Create a terminal node if there are only observations of one class remaining.
 		if (classesPresent.size() < 2)
 		{
 			return new NodeTerminal(classesPresent, classData, inBagObservations);
 		}
 		
 		// Determine the best split that can be made.
 		String featureUsedForSplit = null;
 		double splitValue = 0.0;
 		while (featureUsedForSplit == null)
 		{
 			List<String> datasetFeatures = new ArrayList<String>(dataset.keySet());
 			Collections.shuffle(datasetFeatures, treeRNG);
 			int numVarsToSelect = Math.min(datasetFeatures.size(), mtry);
 			List<String> featuresToSplitOn = datasetFeatures.subList(0, numVarsToSelect);
 			ImmutableTwoValues<String, Double> bestSplit = FindBestSplit.main(dataset, dataIndices, classData, inBagObservations,
 					featuresToSplitOn, numberOfUniqueObservations);
 			featureUsedForSplit = bestSplit.first;
 			splitValue = bestSplit.second;
 			//TODO warning and exit if it goes through X loops without finding an adequate split
 			//TODO put out the indices of the observations so that they can be checked
 			//TODO and say that X iterations went without being able to split the classes.
 		}
 		
 		// Split the dataset into observations going to the left child and those going to the right one based on the feature to
 		// split on and split value.
 		ImmutableFourValues<int[], Integer, int[], Integer> splitObservations = this.splitDataset(dataset.get(featureUsedForSplit),
 				dataIndices.get(featureUsedForSplit), inBagObservations, splitValue);
 		int[] leftChildInBagObservations = splitObservations.first;
 		int leftChildNumberOfUniqueObservations = splitObservations.second.intValue();
 		int[] rightChildInBagObservations = splitObservations.third;
 		int rightChildNumberOfUniqueObservations = splitObservations.fourth.intValue();
 		
 		// Generate the children of this node.
 		Node leftChild = growTree(dataset, dataIndices, classData, leftChildInBagObservations, mtry, treeRNG,
 				leftChildNumberOfUniqueObservations);
 		Node rightChild = growTree(dataset, dataIndices, classData, rightChildInBagObservations, mtry, treeRNG,
 				rightChildNumberOfUniqueObservations);
 		return new NodeNonTerminal(featureUsedForSplit, splitValue, leftChild, rightChild);
 		
 	}
 	
 	
 	private final Set<String> classesPresent(Map<String, double[]> classData, int[] inBagObservations)
 	{
 		Set<String> classesPresent = new HashSet<String>();
 		Set<String> allClasses = new HashSet<String>(classData.keySet());
 		
 		// Add the classes that have non-zero weight (and are therefore present).
 		int numberOfObservations = inBagObservations.length;
 		for (String s : allClasses)
 		{
 			double[] classWeights = classData.get(s);
 			double classWeightSum = 0.0;
 			for (int i = 0; i < numberOfObservations; i++)
 			{
 				classWeightSum += (classWeights[i] * inBagObservations[i]);
 			}
 			if (classWeightSum != 0.0)
 			{
 				classesPresent.add(s);
 			}
 		}
 
 		return classesPresent;
 	}
 
 	
 	public final Map<String, double[]> predict(Map<String, double[]> datasetToPredict, Set<Integer> obsToPredict,
 			Map<String, double[]> predictions)
 	{
 		return this.tree.predict(datasetToPredict, obsToPredict, predictions);
 	}
 	
 	
 	private final ImmutableFourValues<int[], Integer, int[], Integer> splitDataset(double[] splitFeatureData,
 			int[] splitDataIndices, int[] inBagObservations, double splitValue)
 	{
 		int numberOfObservations = inBagObservations.length;
 		int[] leftChildInBag = new int[numberOfObservations];
 		int numberOfUniqueLeftObservations = 0;
 		int[] rightChildInBag = new int[numberOfObservations];
 		int numberOfUniqueRightObservations = 0;
 
 		for (int i = 0; i < numberOfObservations; i++)
 		{
 			double dataValue = splitFeatureData[i];
 			int originalObsIndex = splitDataIndices[i];
 			int inBagCount = inBagObservations[originalObsIndex];
 			int inBagUnique = (inBagCount == 0 ? 0 : 1);
 			if (dataValue <= splitValue)
 			{
 				leftChildInBag[originalObsIndex] = inBagCount;
 				numberOfUniqueLeftObservations += inBagUnique;
 			}
 			else
 			{
				rightChildInBag[i] = inBagCount;
 				numberOfUniqueRightObservations += inBagUnique;
 			}
 		}
 		
 		return new ImmutableFourValues<int[], Integer, int[], Integer>(leftChildInBag, numberOfUniqueLeftObservations,
 				rightChildInBag, numberOfUniqueRightObservations);
 	}
 	
 }
