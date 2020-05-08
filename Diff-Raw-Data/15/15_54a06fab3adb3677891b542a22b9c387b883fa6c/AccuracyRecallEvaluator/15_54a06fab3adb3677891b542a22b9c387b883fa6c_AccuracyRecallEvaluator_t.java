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
 package gr.auth.ee.lcs.evaluators;
 
 import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.data.IEvaluator;
 import gr.auth.ee.lcs.utilities.InstanceToDoubleConverter;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 
 import weka.core.Instances;
 
 /**
  * An evaluator to evaluate on the accuracy of the classifiers.
  * 
  * @author Miltos Allamanis
  * 
  */
 public class AccuracyRecallEvaluator implements IEvaluator {
 
 	/**
 	 * Accuracy.
 	 */
 	public final static int TYPE_ACCURACY = 0;
 
 	/**
 	 * Recall.
 	 */
 	public final static int TYPE_RECALL = 1;
 
 	private final int currentType;
 
 	/**
 	 * The set of instances to evaluate on.
 	 */
 	private final double[][] instances;
 
 	/**
 	 * A boolean indicating if the evaluator is going to print the results.
 	 */
 	private final boolean printResults;
 
 	/**
 	 * The LCS instance being used.
 	 */
 	private final AbstractLearningClassifierSystem myLcs;
 
 	/**
 	 * A constructor using only instances.
 	 * 
 	 * @param instances
 	 *            the instance double[][] array
 	 * @param print
 	 *            true to print results to stdout
 	 * @param lcs
 	 *            the LCS instance used
 	 */
 	public AccuracyRecallEvaluator(final double[][] instances,
 			final boolean print, final AbstractLearningClassifierSystem lcs,
 			final int type) {
 		this.printResults = print;
 		this.instances = instances;
 		myLcs = lcs;
 		currentType = type;
 	}
 
 	/**
 	 * Constructor for creating evaluator with a Weka instance set.
 	 * 
 	 * @param instances
 	 *            the instances to be used
 	 * @param print
 	 *            true to print results to stdout
 	 * @param lcs
 	 *            the LCS instance used
 	 */
 	public AccuracyRecallEvaluator(final Instances instances,
 			final boolean print, final AbstractLearningClassifierSystem lcs,
 			final int type) {
 		this.instances = InstanceToDoubleConverter.convert(instances);
 		printResults = print;
 		myLcs = lcs;
 		currentType = type;
 	}
 
 	/**
 	 * Constructor for creating evaluator with .arff file.
 	 * 
 	 * @param arffFileName
 	 *            the arff file
 	 * @param print
 	 *            true to print output to stdout
 	 * @param lcs
 	 *            the LCS instance used
 	 * @param type
 	 *            classification type
 	 * @throws IOException
 	 *             when file is not found
 	 */
 	public AccuracyRecallEvaluator(final String arffFileName,
 			final boolean print, final AbstractLearningClassifierSystem lcs,
 			final int type) throws IOException {
 		printResults = print;
 		final FileReader reader = new FileReader(arffFileName);
 		this.instances = InstanceToDoubleConverter
 				.convert(new Instances(reader));
 		myLcs = lcs;
 		currentType = type;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * gr.auth.ee.lcs.data.IEvaluator#evaluateSet(gr.auth.ee.lcs.classifiers
 	 * .ClassifierSet)
 	 */
 	@Override
 	public final double evaluateLCS(final AbstractLearningClassifierSystem lcs) {
 		final ClassifierTransformBridge bridge = myLcs
 				.getClassifierTransformBridge();
 
 		double sumOfAccuracies = 0;
 		double sumOfRecall = 0;

		int emptySamples = 0;

 		for (int i = 0; i < instances.length; i++) {
 			int unionOfLabels = 0;
 			int intersectionOfLabels = 0;
 
 			final int[] classes = lcs.classifyInstance(instances[i]);
 			final int[] classification = bridge
 					.getDataInstanceLabels(instances[i]);
 
 			// Find symmetric differences
 			Arrays.sort(classes);
 			Arrays.sort(classification);
 			for (int j = 0; j < classes.length; j++) {
 				if (Arrays.binarySearch(classification, classes[j]) < 0) {
 					unionOfLabels++;
 				} else {
 					intersectionOfLabels++;
 					unionOfLabels++;
 				}
 			}
 			for (int j = 0; j < classification.length; j++) {
 				if (Arrays.binarySearch(classes, classification[j]) < 0)
 					unionOfLabels++;
 			}
 			final double instanceAccuracy = ((double) intersectionOfLabels)
 					/ ((double) unionOfLabels);
 			sumOfAccuracies += Double.isNaN(instanceAccuracy) ? 0
 					: instanceAccuracy;
 
 			final double instanceRecall = ((double) intersectionOfLabels)
 					/ ((double) classification.length);
 			sumOfRecall += Double.isNaN(instanceRecall) ? 0 : instanceRecall;

			if (unionOfLabels == 0)
				emptySamples++;
 		}
		final double accuracy = sumOfAccuracies
				/ (instances.length - emptySamples);
		final double recall = sumOfRecall / (instances.length - emptySamples);
 
 		if (printResults) {
 			System.out.println("Accuracy: " + accuracy);
 			System.out.println("Recall: " + recall);
 		}
 		if (currentType == TYPE_ACCURACY)
 			return accuracy;
 		else
 			return recall;
 	}
 
 }
