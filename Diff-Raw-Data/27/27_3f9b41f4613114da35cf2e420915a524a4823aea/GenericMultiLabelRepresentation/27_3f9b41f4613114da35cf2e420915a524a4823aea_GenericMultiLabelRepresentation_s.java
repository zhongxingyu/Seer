 /**
  * 
  */
 package gr.auth.ee.lcs.data.representations;
 
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.data.AbstractUpdateAlgorithmStrategy;
 import gr.auth.ee.lcs.utilities.ExtendedBitSet;
 import gr.auth.ee.lcs.utilities.ILabelSelector;
 import gr.auth.ee.lcs.utilities.ProportionalCut;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import weka.core.Instances;
 
 /**
  * A class for representing multilabel rules. For each rule each label can be
  * represented as 0,1,#.
  * 
  * @author Miltos Allamanis
  * 
  */
 public final class GenericMultiLabelRepresentation extends
 		ComplexRepresentation {
 
 	/**
 	 * The accuracy don't (#) care value.
 	 */
 	public static final double ACCURACY_DONT_CARE_VALUE = 0.5;
 
 	/**
 	 * The Hamming don't care (#) value.
 	 */
 	public static final double HAMMING_DONT_CARE_VALUE = 1;
 
 	/**
 	 * A boolean label representation with dont'cares. The only difference
 	 * between this class an the BooleanAttribute is that generalization is
 	 * reversed
 	 * 
 	 * @author Miltos Allamanis
 	 * 
 	 */
 	public class GenericLabel extends AbstractAttribute {
 
 		/**
 		 * A boolean variable permitting to temporarily deactivating a label.
 		 */
 		private boolean active;
 
 		/**
 		 * The constructor.
 		 * 
 		 * @param startPosition
 		 *            the position attribute gene starts in chromosome
 		 * @param attributeName
 		 *            the name of the attribute
 		 * @param generalizationRate
 		 *            the generalization rate, used at covering
 		 */
 		public GenericLabel(final int startPosition,
 				final String attributeName, final double generalizationRate) {
 			super(startPosition, attributeName, generalizationRate);
 			lengthInBits = 2;
 			chromosomeSize += lengthInBits;
 			active = true;
 		}
 
 		/**
 		 * A setter for the active variable.
 		 * 
 		 * @param isActive
 		 *            true if we want to set the label active
 		 */
 		public final void setActive(final boolean isActive) {
 			active = isActive;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * gr.auth.ee.lcs.data.representations.ComplexRepresentation.Attribute
 		 * #fixAttributeRepresentation
 		 * (gr.auth.ee.lcs.classifiers.ExtendedBitSet)
 		 */
 		@Override
 		public final void fixAttributeRepresentation(
 				final ExtendedBitSet generatedClassifier) {
 			if (active)
 				return;
 			generatedClassifier.clear(positionInChromosome);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * gr.auth.ee.lcs.data.representations.ComplexRepresentation.Attribute
 		 * #isEqual(gr.auth.ee.lcs.classifiers.ExtendedBitSet,
 		 * gr.auth.ee.lcs.classifiers.ExtendedBitSet)
 		 */
 		@Override
 		public final boolean isEqual(final ExtendedBitSet baseChromosome,
 				final ExtendedBitSet testChromosome) {
 			if (!active)
 				return true;
 			// if the base classifier is specific and the test is # return false
 			if (baseChromosome.get(positionInChromosome) != testChromosome
 					.get(positionInChromosome))
 				return false;
 			else if ((baseChromosome.get(positionInChromosome + 1) != testChromosome
 					.get(positionInChromosome + 1))
 					&& baseChromosome.get(positionInChromosome))
 				return false;
 
 			return true;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * gr.auth.ee.lcs.data.representations.ComplexRepresentation.Attribute
 		 * #isMatch(float, gr.auth.ee.lcs.classifiers.ExtendedBitSet)
 		 */
 		@Override
 		public final boolean isMatch(final float attributeVision,
 				final ExtendedBitSet testedChromosome) {
 			if (!active)
 				return true;
 			if (testedChromosome.get(this.positionInChromosome)) {
 				if ((attributeVision == 0 ? false : true) == testedChromosome
 						.get(this.positionInChromosome + 1))
 					return true;
 				else
 					return false;
 			} else {
 				return true;
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * gr.auth.ee.lcs.data.representations.ComplexRepresentation.Attribute
 		 * #isMoreGeneral(gr.auth.ee.lcs.classifiers.ExtendedBitSet,
 		 * gr.auth.ee.lcs.classifiers.ExtendedBitSet)
 		 */
 		@Override
 		public final boolean isMoreGeneral(final ExtendedBitSet testChromosome,
 				final ExtendedBitSet baseChromosome) {
 			if (!active)
 				return true;
 			// if the base classifier is specific and the test is # return false
 			if (baseChromosome.get(positionInChromosome)
 					&& !testChromosome.get(positionInChromosome))
 				return false;
 			if ((baseChromosome.get(positionInChromosome + 1) != testChromosome
 					.get(positionInChromosome + 1))
 					&& baseChromosome.get(positionInChromosome))
 				return false;
 
 			return true;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * gr.auth.ee.lcs.data.representations.ComplexRepresentation.Attribute
 		 * #randomCoveringValue(float, gr.auth.ee.lcs.classifiers.Classifier)
 		 */
 		@Override
 		public final void randomCoveringValue(final float attributeValue,
 				final Classifier generatedClassifier) {
 			if (!active) {
 				generatedClassifier.clear(positionInChromosome);
 				generatedClassifier.clear(positionInChromosome + 1);
 				return;
 			}
 			if (attributeValue == 0)
 				generatedClassifier.clear(positionInChromosome + 1);
 			else
 				generatedClassifier.set(positionInChromosome + 1);
 
 			if (Math.random() < generalizationRate)
 				generatedClassifier.clear(positionInChromosome);
 			else
 				generatedClassifier.set(positionInChromosome);
 
 		}
 
 		/**
 		 * Enforce label deactivation.
 		 * 
 		 * @param aClassifier
 		 *            the classifier to deactivate
 		 */
 		public final void enforceDeactivation(final Classifier aClassifier) {
 			if (active)
 				return;
 			aClassifier.clear(positionInChromosome);
 			aClassifier.clear(positionInChromosome + 1);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * gr.auth.ee.lcs.data.representations.ComplexRepresentation.Attribute
 		 * #toString(gr.auth.ee.lcs.classifiers.ExtendedBitSet)
 		 */
 		@Override
 		public final String toString(final ExtendedBitSet convertingClassifier) {
 			if (!active)
 				return "#";
 
 			if (convertingClassifier.get(this.positionInChromosome)) {
 				return convertingClassifier.get(this.positionInChromosome + 1) ? "1"
 						: "0";
 			} else {
 				return "#";
 			}
 
 		}
 
 	}
 
 	/**
 	 * A classification strategy selecting the labels according to the highest
 	 * fitness of a classifier. If a classifier has an # then the next
 	 * classifier is selected.
 	 * 
 	 * @author Miltos Allamanis
 	 * 
 	 */
 	public final class BestFitnessClassificationStrategy implements
 			IClassificationStrategy {
 
 		@Override
 		public int[] classify(final ClassifierSet aSet,
 				final double[] visionVector) {
 			final double[] decisionTable = new double[numberOfLabels];
 			final double[] confidenceTable = new double[numberOfLabels];
 			Arrays.fill(decisionTable, 0);
 			Arrays.fill(confidenceTable, 0);
 
 			final ClassifierSet matchSet = aSet.generateMatchSet(visionVector);
 			final int setSize = matchSet.getNumberOfMacroclassifiers();
 			for (int i = 0; i < setSize; i++) {
 				// For each classifier
 				for (int label = 0; label < numberOfLabels; label++) {
 					final Classifier currentClassifier = matchSet
 							.getClassifier(i);
 					final int numerosity = matchSet.getClassifierNumerosity(i);
 					final double fitness = numerosity
 							* currentClassifier
 									.getComparisonValue(AbstractUpdateAlgorithmStrategy.COMPARISON_MODE_EXPLOITATION);
 					if (fitness > confidenceTable[label]) {
 						final String cons = (attributeList[attributeList.length
 								- numberOfLabels + label])
 								.toString(currentClassifier);
 						if (cons == "#")
 							continue;
 						confidenceTable[label] = fitness;
 						if (cons == "1")
 							decisionTable[label] = 1;
 						else
 							decisionTable[label] = 0;
 
 					}
 				}
 			}
 
 			int numberOfActiveLabels = 0;
 			for (int i = 0; i < decisionTable.length; i++)
 				if (decisionTable[i] == 1)
 					numberOfActiveLabels++;
 
 			final int[] result = new int[numberOfActiveLabels];
 			int currentIndex = 0;
 			for (int i = 0; i < decisionTable.length; i++)
 				if (decisionTable[i] == 1) {
 					result[currentIndex] = i;
 					currentIndex++;
 				}
 
 			return result;
 		}
 
 	}
 
 	/**
 	 * A Voting Classification Strategy.
 	 * 
 	 * @author Miltos Allamanis
 	 * 
 	 */
 	public final class VotingClassificationStrategy implements
 			IClassificationStrategy {
 
 		private final float targetLC;
 
 		private double voteThreshold;
 
 		public VotingClassificationStrategy(float targetLabelCardinality) {
 			targetLC = targetLabelCardinality;
 		}
 
 		private final float[] getConfidenceArray(final ClassifierSet aSet,
 				final double[] visionVector) {
 			final float[] votingTable = new float[numberOfLabels];
			for (int i = 0; i < numberOfLabels; i++)
				votingTable[i] = 0;
 
 			final ClassifierSet matchSet = aSet.generateMatchSet(visionVector);
 			// Let each classifier vote
 			final int setSize = matchSet.getNumberOfMacroclassifiers();
 			for (int i = 0; i < setSize; i++) {
 				// For each classifier
 				for (int label = 0; label < numberOfLabels; label++) {
					final Classifier currentClassifier = matchSet
							.getClassifier(i);
					final int classifierNumerosity = matchSet
							.getClassifierNumerosity(i);
					final double fitness = currentClassifier
							.getComparisonValue(AbstractUpdateAlgorithmStrategy.COMPARISON_MODE_EXPLOITATION);
 					final String cons = (attributeList[attributeList.length
 							- numberOfLabels + label])
 							.toString(currentClassifier);
 					if (cons == "#")
 						continue;
 					if (cons == "1")
 						votingTable[label] += classifierNumerosity * fitness;
 					else
 						votingTable[label] -= classifierNumerosity * fitness;
 
 				}
 			}
 
 			// Find mean to make all numbers positive
			double minVote = Double.MAX_VALUE;
 			for (int i = 0; i < votingTable.length; i++) {
 				if (votingTable[i] < minVote)
 					minVote = votingTable[i];
 			}
 
 			// Find sum (and make all positive)
 			double sumVote = 0;
 			for (int i = 0; i < votingTable.length; i++) {
 				votingTable[i] -= minVote;
 				sumVote += votingTable[i];
 			}
 
 			// Normalize
			for (int i = 0; i < votingTable.length; i++) {
				votingTable[i] /= sumVote;
 			}
 			return votingTable;
 		}
 
 		/**
 		 * Perform a proportional Cut (Pcut) on a set of instances to calibrate
 		 * threshold.
 		 * 
 		 * @param instances
 		 *            the instances to calibrate threshold on
 		 * @param rules
 		 *            the rules used to classify the instances and provide
 		 *            confidence values.
 		 * @param targetLc
 		 *            the target Label Cardinality (LC) we are tring to achieve.
 		 */
 		public void proportionalCutCalibration(final double[][] instances,
 				final ClassifierSet rules) {
 			float[][] confidenceValues = new float[instances.length][];
 			for (int i = 0; i < instances.length; i++) {
 				confidenceValues[i] = getConfidenceArray(rules, instances[i]);
 			}
 
 			ProportionalCut pCut = new ProportionalCut();
 			this.voteThreshold = pCut.calibrate(targetLC, confidenceValues);
 			System.out.println("Threshold set to " + this.voteThreshold);
 
 		}
 
 		@Override
 		public int[] classify(final ClassifierSet aSet,
 				final double[] visionVector) {
 
 			float[] votingTable = getConfidenceArray(aSet, visionVector);
 
 			int numberOfActiveLabels = 0;
 			for (int i = 0; i < votingTable.length; i++)
 				if (votingTable[i] > voteThreshold)
 					numberOfActiveLabels++;
 
 			final int[] result = new int[numberOfActiveLabels];
 
 			int currentIndex = 0;
 			for (int i = 0; i < votingTable.length; i++)
 				if (votingTable[i] > voteThreshold) {
 					result[currentIndex] = i;
 					currentIndex++;
 				}
 
 			return result;
 		}
 
 	}
 
 	/**
 	 * The metric type used for calculating classifier's ability to classify an
 	 * instance.
 	 */
 	private final int metricType;
 
 	/**
 	 * The label generalization rate.
 	 */
 	private final double labelGeneralizationRate;
 
 	/**
 	 * The exact-match metric.
 	 */
 	public static final int EXACT_MATCH = 0;
 
 	/**
 	 * The relative accuracy metric.
 	 */
 	public static final int RELATIVE_ACCURACY = 1;
 
 	/**
 	 * The Hamming loss metric.
 	 */
 	public static final int HAMMING_LOSS = 2;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param attributes
 	 *            the attributes to use
 	 * @param ruleConsequentsNames
 	 *            the rule consequent names
 	 * @param labels
 	 *            the number of labels to be used
 	 * @param type
 	 *            the type of metric to be used (see static int's)
 	 * @param lblgeneralizationRate
 	 *            the generalization rate to be used for the labels
 	 * @param attributeGeneralizationRate
 	 *            the generalization rate to be used for the attributes
 	 */
 	public GenericMultiLabelRepresentation(
 			final AbstractAttribute[] attributes,
 			final String[] ruleConsequentsNames, final int labels,
 			final int type, final double lblgeneralizationRate,
 			final double attributeGeneralizationRate) {
 		super(attributes, ruleConsequentsNames, labels,
 				attributeGeneralizationRate);
 		metricType = type;
 		labelGeneralizationRate = lblgeneralizationRate;
 	}
 
 	/**
 	 * A constructor from an .arff file.
 	 * 
 	 * @param inputArff
 	 *            the input .arff filename
 	 * @param precision
 	 *            the number of bits to be used for precision
 	 * @param labels
 	 *            the number of labels used at the program
 	 * @param type
 	 *            the type of metric to be used
 	 * @param lblgeneralizationRate
 	 *            the generalization rate of the labels (P#))
 	 * @param attributeGeneralizationRate
 	 *            the attribute generalization rate
 	 * @throws IOException
 	 *             when file is not found
 	 */
 	public GenericMultiLabelRepresentation(final String inputArff,
 			final int precision, final int labels, final int type,
 			final double lblgeneralizationRate,
 			final double attributeGeneralizationRate) throws IOException {
 		super(inputArff, precision, labels, attributeGeneralizationRate);
 		metricType = type;
 		labelGeneralizationRate = lblgeneralizationRate;
 	}
 
 	@Override
 	public float classifyAbilityAll(final Classifier aClassifier,
 			final int instanceIndex) {
 		switch (metricType) {
 		case EXACT_MATCH:
 			return classifyAbsolute(aClassifier, instanceIndex);
 		case RELATIVE_ACCURACY:
 			return classifyAccuracy(aClassifier, instanceIndex);
 		case HAMMING_LOSS:
 			return classifyHamming(aClassifier, instanceIndex);
 		default:
 			return 0;
 		}
 	}
 
 	@Override
 	public float classifyAbilityLabel(final Classifier aClassifier,
 			final int instanceIndex, final int label) {
 		final int currentLabelIndex = attributeList.length - numberOfLabels
 				+ label;
 		if (attributeList[currentLabelIndex].isMatch(
 				(float) instances[instanceIndex][currentLabelIndex],
 				aClassifier)) {
 			final String value = attributeList[currentLabelIndex]
 					.toString(aClassifier);
 			if (value != "#")
 				return 1;
 			return 0;
 		}
 		return -1;
 	}
 
 	/**
 	 * Absolute Classification.
 	 * 
 	 * @param aClassifier
 	 *            a classifier to be used for classification
 	 * @param instanceIndex
 	 *            the index of the instance
 	 * @return a float indicating the classifier's ability to exact match the
 	 *         instance
 	 */
 	public float classifyAbsolute(final Classifier aClassifier,
 			final int instanceIndex) {
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			if (!attributeList[currentLabelIndex].isMatch(
 					(float) instances[instanceIndex][currentLabelIndex],
 					aClassifier))
 				return 0;
 		}
 
 		// Check for overgeneral
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			final String value = attributeList[currentLabelIndex]
 					.toString(aClassifier);
 			if (value != "#")
 				return 1;
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Find the accuracy at which the classifier classifies a given instance.
 	 * 
 	 * @param aClassifier
 	 *            the classifier
 	 * @param instanceIndex
 	 *            the instance index
 	 * @return a float representing the classifier's accuracy
 	 */
 	public float classifyAccuracy(final Classifier aClassifier,
 			final int instanceIndex) {
 		float correct = 0;
 		float wrong = 0;
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			final String actualLabel = instances[instanceIndex][currentLabelIndex] == 1 ? "1"
 					: "0";
 			final String classifiedLabel = attributeList[currentLabelIndex]
 					.toString(aClassifier);
 			if (classifiedLabel == "#") {
 				if (actualLabel == "1")
 					correct += ACCURACY_DONT_CARE_VALUE;
 			} else if (classifiedLabel == actualLabel) {
 				if (actualLabel == "1")
 					correct++;
 			} else {
 				wrong++;
 			}
 		}
 		if (wrong + correct > 0)
 			return ((float) correct) / ((float) (wrong + correct));
 		else
 			return 0;
 	}
 
 	/**
 	 * Evaluate classify ability of an instance through Hamming distance.
 	 * 
 	 * @param aClassifier
 	 *            the classifier to evaluate
 	 * @param instanceIndex
 	 *            the index of the instance
 	 * @return the hamming distance of the classifier and the instance.
 	 */
 	public float classifyHamming(final Classifier aClassifier,
 			final int instanceIndex) {
 		float result = 0;
 		float totalClassifications = 0;
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			final String value = attributeList[currentLabelIndex]
 					.toString(aClassifier);
 			if (value == "#")
 				continue;
 			totalClassifications++;
 			if (attributeList[currentLabelIndex].isMatch(
 					(float) instances[instanceIndex][currentLabelIndex],
 					aClassifier))
 				result++;
 		}
 
 		final float hammingWin = ((float) result)
 				/ ((float) totalClassifications);
 
 		return Double.isNaN(hammingWin) ? 0 : hammingWin;
 	}
 
 	@Override
 	protected void createClassRepresentation(final Instances instances) {
 		for (int i = 0; i < numberOfLabels; i++) {
 
 			final int labelIndex = attributeList.length - numberOfLabels + i;
 
 			final String attributeName = instances.attribute(labelIndex).name();
 
 			attributeList[labelIndex] = new GenericLabel(chromosomeSize,
 					attributeName, labelGeneralizationRate);
 		}
 	}
 
 	@Override
 	public int[] getClassification(final Classifier aClassifier) {
 		int[] labels = new int[numberOfLabels];
 		int labelIndex = 0;
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			final String value = attributeList[currentLabelIndex]
 					.toString(aClassifier);
 			if (value == "1") {
 				labels[labelIndex] = i;
 				labelIndex++;
 			}
 		}
 		int[] result = new int[labelIndex];
 
 		for (int i = 0; i < labelIndex; i++) {
 			result[i] = labels[i];
 		}
 
 		return result;
 	}
 
 	@Override
 	public int[] getDataInstanceLabels(final double[] dataInstance) {
 		int numOfLabels = 0;
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			if (dataInstance[currentLabelIndex] == 1)
 				numOfLabels++;
 		}
 		int[] result = new int[numOfLabels];
 		int resultIndex = 0;
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			if (dataInstance[currentLabelIndex] == 1) {
 				result[resultIndex] = i;
 				resultIndex++;
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void setClassification(final Classifier aClassifier, final int action) {
 		final int labelIndex = attributeList.length - numberOfLabels + action;
 		attributeList[labelIndex].randomCoveringValue(1, aClassifier);
 	}
 
 	/**
 	 * Activate all labels.
 	 */
 	public void activateAllLabels() {
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			((GenericLabel) attributeList[currentLabelIndex]).setActive(true);
 		}
 	}
 
 	/**
 	 * Activate only a specific label.
 	 * 
 	 * @param labelIndex
 	 *            the index of the label to be activated
 	 */
 	public void activateLabel(final ILabelSelector selector) {
 		for (int i = 0; i < numberOfLabels; i++) {
 			final int currentLabelIndex = attributeList.length - numberOfLabels
 					+ i;
 			((GenericLabel) attributeList[currentLabelIndex])
 					.setActive(selector.getStatus(i));
 		}
 	}
 
 	/**
 	 * Make sure all labels in the set are correctly deactivated.
 	 * 
 	 * @param aSet
 	 *            the set to deactivate labels from
 	 */
 	public void reinforceDeactivatedLabels(final ClassifierSet aSet) {
 		final int setSize = aSet.getNumberOfMacroclassifiers();
 		for (int k = 0; k < setSize; k++) {
 			final Classifier cl = aSet.getClassifier(k);
 			for (int i = 0; i < numberOfLabels; i++) {
 				final int currentLabelIndex = attributeList.length
 						- numberOfLabels + i;
 				((GenericLabel) attributeList[currentLabelIndex])
 						.enforceDeactivation(cl);
 			}
 		}
 	}
 
 }
