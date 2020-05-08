 package com.shsrobotics.reinforcementlearning.estimators;
 
 import com.shsrobotics.reinforcementlearning.util.DataPoint;
 import com.shsrobotics.reinforcementlearning.util.RandomDecisionTree;
 
 /**
  * Estimates Q Values using a random forest ensemble classifier for use in a {@link QLearner}.
  * @author Team 2412.
  */
 public class RandomForestQEstimator {
 	
 	/**
 	 * The actual random forest.
 	 */
 	private RandomDecisionTree[] forest;
 	
 	
 	/**
 	 * Minimum input values.
 	 */
 	private double[] minimums;
 	/**
 	 * Maximum input values.
 	 */
 	private double[] maximums;
 	
 	/**
 	 * Create a Random Forest machine learner to estimate values.
 	 * @param trees the number of trees in the forest.
 	 */
 	public RandomForestQEstimator(int trees, double[] minimums, double[] maximums) {
 		this.forest = new RandomDecisionTree[trees];
 		this.minimums = minimums;
 		this.maximums = maximums;
 	}
 	
 	/**
 	 * Add sample to the forest.
 	 * Will erase existing sample and re-train forest. There should be at least
 	 * twenty examples per variable in the set.
 	 * @param sample the sample to use to train the trees in the forest.
 	 * @return array of variable importance.
 	 */
 	public double[] addInitialData(DataPoint[] data) {
 		int sampleSize = data.length;
 		int variableSize = data[0].getInputs().length;
		int subsetSize = (int) Math.ceil((double) variableSize / 3);
 		int numberOfTrees = forest.length;
 		BootstrapSample[] samples = new BootstrapSample[numberOfTrees];		
 		for (int i = 0; i < numberOfTrees; i++) {
 			samples[i] = takeBootstrapSample(sampleSize, data);
 			// add new tree and train it
 			forest[i] = new RandomDecisionTree(samples[i].sample, 
				subsetSize, minimums, maximums);
 		}
 		
 		double[] variableImportance = zeros(variableSize);		
 		for (int i = 0; i < numberOfTrees; i++) {
 			int oobCount = 0;
 			double sum = 0.0;
 			// use OOB data to find MSE
 			for (int j = 0; j < sampleSize; j++) {
 				if (samples[i].used[j]) { // data used in training
 					continue;
 				}
 				oobCount++;
 				DataPoint example = data[j];
 				double actual = run(example.getInputs());
 				double expected = example.getOutputs()[0];
 				sum += Math.pow(actual - expected, 2);
 			}
 			double treeMSE = sum / oobCount;
			
 			// randomly change variables to get MSE for each variable
			double[] variableMSE = new double[variableSize];
 			for (int j = 0; j < variableSize; j++) {
 				sum = 0.0;
 				for (int k = 0; k < sampleSize; k++) {
					if (samples[i].used[k] == true) { // data used in training
 						continue;
 					}
 					DataPoint example = data[k];
					double[] inputs = example.getInputs().clone();
 					// randomly change the variable
 					inputs[j] = (maximums[j] - minimums[j]) * Math.random() + minimums[j];
 					double actual = run(inputs);
 					double expected = example.getOutputs()[0];
 					sum += Math.pow(actual - expected, 2);
 				}
 				variableMSE[j] = sum / oobCount;
 			}
 			
 			// calculate variable importance based on MSE difference
 			for (int j = 0; j < variableSize; j++) {
 				variableImportance[j] += variableMSE[j] - treeMSE;
 			}
 		}
 		
 		for (int i = 0; i < variableSize ; i++) {
 			variableImportance[i] /= numberOfTrees;
 		}
 		
 		return variableImportance;
 	}
 	
 	/**
 	 * Get the random forest prediction.
 	 * @param input the input value.
 	 * @return the random forest output.
 	 */
 	public double run(double[] input) {
 		double sum = 0.0;
 		int numberOfTrees = forest.length;
 		for (int i = 0; i < numberOfTrees; i++) {
 			sum += forest[i].run(input);
 		}
 		return sum / numberOfTrees; // average each tree's output
 	}
 	
 	
 	/**
 	 * Take a bootstrap sample of the sample. 
 	 * Data is sampled with replacement {@code size} number of times.
 	 * @param size the size of the sample.
 	 * @param sample the sample to sample.
 	 * @return the sample of sample. Index 1 is the sample, index 2 is the OOB sample.
 	 */
 	private BootstrapSample takeBootstrapSample(int size, DataPoint[] data) {
 		DataPoint[] sample = new DataPoint[size]; // n
 		int cases = data.length;
 		boolean[] used = new boolean[cases];
 		for (int i = 0; i < size; i++) { // sample n times with replacement
 			int index = (int) (cases * Math.random());
 			sample[i] = data[index];
 			used[index] = true;
 		}
 		return new BootstrapSample(sample, used);
 	}
 	
 	/**
 	 * Holds result of a bootstrap sample.
 	 */
 	public class BootstrapSample {
 		/**
 		 * The sample.
 		 */
 		public DataPoint[] sample;
 		/**
 		 * List of which data points were used.
 		 */
 		public boolean[] used;
 		
 		/**
 		 * Create a bootstrap sample results list.
 		 * @param sample the data sample.
 		 * @param used a list of which data points were used.
 		 */
 		public BootstrapSample(DataPoint[] sample, boolean[] used) {
 			this.sample = sample;
 			this.used = used;
 		}
 	}
 	
 	/**
 	 * Fill an array with zeros
 	 * <p/>
 	 * @param size the size of the array
 	 * @return The array.
 	 */
 	private double[] zeros(int size) {
 		double[] toReturn = new double[size];
 		for (int i = 0; i < size; i++) {
 			toReturn[i] = 0;
 		}
 		return toReturn;
 	}
 
 }
