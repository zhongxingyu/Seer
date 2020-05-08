 /**
  * 
  */
 package gr.auth.ee.lcs.evaluators;
 
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.data.IEvaluator;
 
 import java.util.Arrays;
 
 import weka.core.Instances;
 
 /**
  * An evaluator using an Weka Instance.
  * 
  * @author Miltos Allamanis
  * 
  */
 public class BinaryAccuracyEvalutor implements IEvaluator {
 
 	/**
 	 * The set of instances to evaluate on.
 	 */
 	private final Instances instanceSet;
 
 	/**
 	 * A boolean indicating if the evaluator is going to print the results.
 	 */
 	private final boolean printResults;
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param instances
 	 *            the set of instances to evaluate on
 	 * @param print
 	 *            true to turn printing on
 	 */
 	public BinaryAccuracyEvalutor(final Instances instances, final boolean print) {
 		instanceSet = instances;
 		printResults = print;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * gr.auth.ee.lcs.data.IEvaluator#evaluateSet(gr.auth.ee.lcs.classifiers
 	 * .ClassifierSet)
 	 */
 	@Override
 	public final double evaluateSet(final ClassifierSet classifiers) {
 		ClassifierTransformBridge bridge = ClassifierTransformBridge
 				.getInstance();
 
 		int tp = 0, fp = 0;
 		for (int i = 0; i < instanceSet.numInstances(); i++) {
 			final double[] instance = new double[instanceSet.numAttributes()];
 			for (int j = 0; j < instanceSet.numAttributes(); j++) {
 				instance[j] = instanceSet.instance(i).value(j);
 			}
 			final int[] classes = bridge.classify(classifiers, instance);
 			final int[] classification = bridge.getDataInstanceLabels(instance);
 			if (Arrays.equals(classes, classification))
 				tp++;
 			else
 				fp++;
 
 		}
 
 		final double errorRate = ((double) fp) / ((double) (fp + tp));
 
 		if (printResults) {
 			System.out.println("tp:" + tp + " fp:" + fp + " errorRate:"
 					+ errorRate + " total instances:"
					+ instanceSet.numInstances());
 		}
 		return errorRate;
 	}
 
 }
