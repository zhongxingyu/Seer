 /*
  *	Copyright (C) 2011 by Allamanis Miltiadis
  *
  *	Permission is hereby granted, free of charge, to any person obtaining a copy
  *	of this software and associated documentation files (the "Software"), to deal
  *	in the Software without restriction, including without limitation the rights
  *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *	copies of the Software, and to permit persons to whom the Software is
  *	furnished to do so, subject to the following conditions:
  *
  *	The above copyright notice and this permission notice shall be included in
  *	all copies or substantial portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *	THE SOFTWARE.
  */
 /**
  * 
  */
 package gr.auth.ee.lcs;
 
 import gr.auth.ee.lcs.utilities.InstancesUtility;
 import gr.auth.ee.lcs.utilities.SettingsLoader;
 
 import java.io.IOException;
 import java.util.Random;
 
 import weka.core.Instances;
 
 /**
  * n-fold evaluator.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class FoldEvaluator {
 
 	/**
 	 * The number of folds to separate the dataset.
 	 */
 	private final int numOfFolds;
 
 	/**
 	 * The LCS prototype to be evaluated.
 	 */
 	private final AbstractLearningClassifierSystem prototype;
 
 	/**
 	 * The instances that the LCS will be evaluated on.
 	 */
 	private final Instances instances;
 
 	/**
 	 * The train set.
 	 */
 	private Instances trainSet;
 
 	/**
 	 * The test set.
 	 */
 	private Instances testSet;
 
 	/**
 	 * The evaluations.
 	 */
 	private double[][] evals;
 
 	/**
 	 * The runs to run.
 	 */
 	final int runs;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param folds
 	 *            the number of folds
 	 * @param myLcs
 	 *            the LCS instance to be evaluated
 	 * @param filename
 	 *            the filename of the .arff containing the instances
 	 * @throws IOException
 	 *             if the file is not found.
 	 */
 	public FoldEvaluator(int folds, AbstractLearningClassifierSystem myLcs,
 			final String filename) throws IOException {
 		numOfFolds = folds;
 		prototype = myLcs;
 
 		instances = InstancesUtility.openInstance(filename);
 		runs = (int) SettingsLoader.getNumericSetting("foldsToRun", numOfFolds);
 		instances.randomize(new Random());
 
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param folds
 	 *            the number of folds used at evaluation
 	 * @param numberOfRuns
 	 *            the number of runs
 	 * @param myLcs
 	 *            the LCS under evaluation
 	 * @param inputInstances
 	 *            the instances to evaluate the LCS on
 	 */
 	public FoldEvaluator(int folds, int numberOfRuns,
 			AbstractLearningClassifierSystem myLcs, Instances inputInstances) {
 		numOfFolds = folds;
 		prototype = myLcs;
 		instances = inputInstances;
 		runs = numberOfRuns;
 	}
 
 	/**
 	 * Calculate the mean of all fold metrics.
 	 * 
 	 * @param results
 	 *            the results double array
 	 * @return the mean for each row
 	 */
 	public double[] calcMean(double[][] results) {
 		final double[] means = new double[results[0].length];
 		for (int i = 0; i < means.length; i++) {
 			double sum = 0;
 			for (int j = 0; j < results.length; j++) {
 				sum += results[j][i];
 			}
 			means[i] = (sum) / (results.length);
 		}
 		return means;
 	}
 
 	/**
 	 * Perform evaluation.
 	 */
 	public void evaluate() {
		final int metricOptimizationIndex = (int) SettingsLoader.getNumericSetting(
				"metricOptimizationIndex", 0);
		final int numOfFoldRepetitions = (int) SettingsLoader.getNumericSetting(
				"numOfFoldRepetitions", 1);
		
 		for (int i = 0; i < runs; i++) {
 			double[][] results = new double[numOfFoldRepetitions][];
 			for (int repetition = 0; repetition < numOfFoldRepetitions; repetition++) {
				AbstractLearningClassifierSystem foldLCS = prototype.createNew();
 				System.out.println("Training Fold " + i);
 				loadFold(i, foldLCS);
 				foldLCS.train();
	
 				// Gather results...
 				results[repetition] = foldLCS.getEvaluations(testSet);
 			}
			
			//Determine better repetition
 			int best = 0;
 			for (int j = 1; j < numOfFoldRepetitions; j++) {
				if (results[j][metricOptimizationIndex] < results[best][metricOptimizationIndex])
 					best = j;
 			}
 
 			// Gather to fold stats
 			gatherResults(results[best], i);
 		}
 
 		final double[] means = calcMean(this.evals);
 		// print results
 		printEvaluations(means);
 	}
 
 	/**
 	 * Gather the results from a specific fold.
 	 * 
 	 * @param results
 	 *            the results array
 	 * @param fold
 	 *            the fold the function is currently gathering
 	 * 
 	 * @return the double containing all evaluations (up to the point being
 	 *         added)
 	 */
 	public double[][] gatherResults(double[] results, int fold) {
 		if (evals == null) {
 			evals = new double[runs][results.length];
 		}
 
 		evals[fold] = results;
 
 		return evals;
 
 	}
 
 	/**
 	 * Print the evaluations.
 	 * 
 	 * @param means
 	 *            the array containing the evaluation means
 	 */
 	public void printEvaluations(double[] means) {
 		final String[] names = prototype.getEvaluationNames();
 
 		for (int i = 0; i < means.length; i++) {
 			System.out.println(names[i] + ": " + means[i]);
 		}
 	}
 
 	/**
 	 * Load a fold into the evaluator.
 	 * 
 	 * @param foldNumber
 	 *            the fold's index
 	 * @param lcs
 	 *            the LCS that will be used to evaluate this fold
 	 */
 	private void loadFold(int foldNumber, AbstractLearningClassifierSystem lcs) {
 
 		trainSet = instances.trainCV(numOfFolds, foldNumber);
 		lcs.instances = InstancesUtility.convertIntancesToDouble(trainSet);
 		testSet = instances.testCV(numOfFolds, foldNumber);
 	}
 }
