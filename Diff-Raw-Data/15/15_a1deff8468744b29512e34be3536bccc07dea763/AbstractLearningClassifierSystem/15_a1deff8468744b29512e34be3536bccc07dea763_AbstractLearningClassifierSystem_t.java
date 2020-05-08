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
 //comment
 
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.classifiers.Macroclassifier;
 import gr.auth.ee.lcs.classifiers.populationcontrol.FixedSizeSetWorstFitnessDeletion;
 import gr.auth.ee.lcs.classifiers.populationcontrol.SortPopulationControl;
 import gr.auth.ee.lcs.classifiers.statistics.MeanAttributeSpecificityStatistic;
 import gr.auth.ee.lcs.classifiers.statistics.MeanCoverageStatistic;
 import gr.auth.ee.lcs.classifiers.statistics.MeanFitnessStatistic;
 import gr.auth.ee.lcs.classifiers.statistics.MeanLabelSpecificity;
 import gr.auth.ee.lcs.classifiers.statistics.WeightedMeanAttributeSpecificityStatistic;
 import gr.auth.ee.lcs.classifiers.statistics.WeightedMeanCoverageStatistic;
 import gr.auth.ee.lcs.classifiers.statistics.WeightedMeanLabelSpecificity;
 import gr.auth.ee.lcs.data.AbstractUpdateStrategy;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.data.ILCSMetric;
 import gr.auth.ee.lcs.data.representations.complex.ComplexRepresentation;
 import gr.auth.ee.lcs.data.representations.complex.GenericMultiLabelRepresentation;
 import gr.auth.ee.lcs.data.representations.complex.GenericMultiLabelRepresentation.BestFitnessClassificationStrategy;
 import gr.auth.ee.lcs.data.representations.complex.GenericMultiLabelRepresentation.VotingClassificationStrategy;
 import gr.auth.ee.lcs.data.updateAlgorithms.MlASLCS3UpdateAlgorithm;
 import gr.auth.ee.lcs.data.updateAlgorithms.MlASLCS4UpdateAlgorithm;
 import gr.auth.ee.lcs.evaluators.AccuracyRecallEvaluator;
 import gr.auth.ee.lcs.evaluators.ExactMatchEvalutor;
 import gr.auth.ee.lcs.evaluators.FileLogger;
 import gr.auth.ee.lcs.evaluators.HammingLossEvaluator;
 import gr.auth.ee.lcs.evaluators.bamevaluators.IdentityBAMEvaluator;
 import gr.auth.ee.lcs.evaluators.bamevaluators.PositionBAMEvaluator;
 import gr.auth.ee.lcs.geneticalgorithm.selectors.RouletteWheelSelector;
 import gr.auth.ee.lcs.utilities.ExtendedBitSet;
 import gr.auth.ee.lcs.utilities.SettingsLoader;
 import gr.auth.ee.lcs.utilities.InstancesUtility;
 
 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Vector;
 import java.lang.String;
 
 import weka.clusterers.SimpleKMeans;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.Remove;
 import edu.rit.pj.ParallelTeam;
 
 
 /**
  * An abstract LCS class to be implemented by all LCSs.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public abstract class AbstractLearningClassifierSystem {
 	
 	
 	public String hookedMetricsFileDirectory;
 	
 	public final int UPDATE_MODE = (int) SettingsLoader.getNumericSetting("UPDATE_MODE", 0);
 	
 	public static final int UPDATE_MODE_IMMEDIATE = 0;
 	
 	public static final int UPDATE_MODE_HOLD = 1;
 
 	public double meanCorrectSetNumerosity = 0;
 	
 	private int cummulativeCurrentInstanceIndex = 0;
 	
 
 
 	/**
 	 * The train set.
 	 * @uml.property  name="instances" multiplicity="(0 -1)" dimension="2"
 	 */
 	public double[][] instances;
 	public double[][] testInstances;
 
 	public Instances trainSet;
 	public Instances testSet;
 	
 
 	/**
 	 * The LCS instance transform bridge.
 	 * @uml.property  name="transformBridge"
 	 * @uml.associationEnd  
 	 */
 	
 	public double labelCardinality = 1;
 	
 	public int numberOfCoversOccured = 0;
 
 	
 	
 	private ClassifierTransformBridge transformBridge;
 
 	/**
 	 * The Abstract Update Algorithm Strategy of the LCS.
 	 * @uml.property  name="updateStrategy"
 	 * @uml.associationEnd  
 	 */
 	protected AbstractUpdateStrategy updateStrategy;
 
 	/**
 	 * The rule population.
 	 * @uml.property  name="rulePopulation"
 	 * @uml.associationEnd  
 	 */
 	protected ClassifierSet rulePopulation;
 
 	/**
 	 * A vector of all evaluator hooks.
 	 * @uml.property  name="hooks"
 	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gr.auth.ee.lcs.data.ILCSMetric"
 	 */
 	private final Vector<ILCSMetric> hooks;
 
 	/**
 	 * Frequency of the hook callback execution.
 	 * @uml.property  name="hookCallbackRate"
 	 */
 	private int hookCallbackRate;
 	
 	
 	public int repetition;
 	
 	private final boolean thoroughlyCheckWIthPopulation = SettingsLoader.getStringSetting("thoroughlyCheckWIthPopulation", "true").equals("true");
 
 	
 	/**
 	 * Matrix used to store the time measurements for different phases of the train procedure.
 	 */
 	public double[][] timeMeasurements;
 	
 	public double[][] systemAccuracy;
 	
 	public Vector<Float> 	qualityIndexOfDeleted = new Vector<Float>();
 	public Vector<Float> 	qualityIndexOfClassifiersCoveredDeleted = new Vector<Float>();
 	public Vector<Float> 	qualityIndexOfClassifiersGaedDeleted = new Vector<Float>();
 	
 	public Vector<Float> 	accuracyOfDeleted = new Vector<Float>();
 	public Vector<Float> 	accuracyOfCoveredDeletion = new Vector<Float>();
 	public Vector<Float> 	accuracyOfGaedDeletion = new Vector<Float>();
 	
 	public Vector<Integer> iteration = new Vector<Integer>();
 	public Vector<Integer> originOfDeleted = new Vector<Integer>();
 
 	public Vector<Float> 	systemAccuracyInTraining = new Vector<Float>();
 	public Vector<Float> 	systemAccuracyInTestingWithPcut = new Vector<Float>();
 	public Vector<Float> 	systemCoverage = new Vector<Float>();
 
 	
 	public int numberOfClassifiersDeletedInMatchSets;
 	
 	
 	/**
 	 * Indicates whether the parallel implementation is employed or not.
 	 */
 	final private boolean smp;
 	
 	/**
 	 * The Parallel Team containing the threads that perform the parallel implementation
 	 * of the generateMatchSet function.
 	 */
 	final private ParallelTeam pt = new ParallelTeam();
 	
 	public int totalRepetition = 0;
 	
 	private Instances inst;
 	
 	public final int iterations;
 	/**
 	 * Constructor.
 	 * 
 	 */ 	
 	protected AbstractLearningClassifierSystem() {
 		try {
 			SettingsLoader.loadSettings();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		hooks = new Vector<ILCSMetric>();
 		hookCallbackRate = (int) SettingsLoader.getNumericSetting("callbackRate", 100);
 		smp = SettingsLoader.getStringSetting("SMP_run", "false").contains("true") ? true : false;
 		iterations = (int) SettingsLoader.getNumericSetting("trainIterations",1000);
 		
 		//blacklist = new ClassifierSet(null);
 		
 		if (smp)
 			System.out.println("smp: true");
 		else
 			System.out.println("smp: false");
 		
 	}
 	
 	
 	public void assimilateDuplicateClassifiers(ClassifierSet rulePopulation, 
 											final boolean evolve) {
 		//if (evolve) {
 			// if subsumption is only made by the parents and not the whole population, merge classifiers to avoid duplicates
 		
 				for (int j = 0; j < rulePopulation.getNumberOfMacroclassifiers() ; j++) {
 				//for (int j = rulePopulation.getNumberOfMacroclassifiers() -1; j >= 0 ; j--) {
 
 					Vector<Integer> indicesOfDuplicates    = new Vector<Integer>();
 					Vector<Float> 	fitnessOfDuplicates    = new Vector<Float>();
 					Vector<Integer> experienceOfDuplicates = new Vector<Integer>();
 
 					final Classifier aClassifier = rulePopulation.getMacroclassifiersVector().get(j).myClassifier;
 					
 					for (int i = rulePopulation.getNumberOfMacroclassifiers() - 1; i >= 0 ; i--) {
 					//for (int i = 0; i < rulePopulation.getNumberOfMacroclassifiers(); i++) {
 
 						Classifier theClassifier = rulePopulation.getMacroclassifiersVector().get(i).myClassifier;
 						
 						if (theClassifier.equals(aClassifier)) { 
 							indicesOfDuplicates.add(i);
 							float theClassifierFitness = (float) (rulePopulation.getMacroclassifiersVector().get(i).numerosity 
 									* getUpdateStrategy().getComparisonValue(theClassifier, AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION));
 							fitnessOfDuplicates.add(theClassifierFitness);
 							experienceOfDuplicates.add(theClassifier.experience);
 
 						}
 					} // exo brei ta indexes ton diplon kanonon sto vector myMacroclassifiers
 					
 					/*an bro enan mono, simainei oti aClassifier == theClassifier, opote den exei noima na ginei afomoiosi
 					 * an bro duo i kai perissoterous simainei oti prepei na epilekso poios apo olous 9a afomoiosei olous tous allous.
 					 * opoios exei megalutero fitness afomoionei tous upoloipous. an duo exoun to idio fitness, 9a afomoiosei autos me to megalutero experience
 					 * */
 					if (indicesOfDuplicates.size() >= 2) {
 						
 						int indexOfSurvivor = 0;
 						float maxFitness = 0;
 						for(int k = 0; k < indicesOfDuplicates.size(); k++) {
 							if (fitnessOfDuplicates.elementAt(k) > maxFitness) {
 								maxFitness = fitnessOfDuplicates.elementAt(k);
 								indexOfSurvivor = k;
 							}
 							else if (fitnessOfDuplicates.elementAt(k) == maxFitness) {
 								if (experienceOfDuplicates.elementAt(k) >= experienceOfDuplicates.elementAt(indexOfSurvivor)) {
 									indexOfSurvivor = k;
 								}
 									
 							}
 						}
 						// exo brei poios 9a einai o epizon classifier. initiate assimilation
 						//for (int k = indicesOfDuplicates.size() -1; k >= 0 ; k--) {
 						for (int k = 0; k < indicesOfDuplicates.size() ; k++) {
 
 							if (k != indexOfSurvivor) {
 								rulePopulation.getMacroclassifiersVector().get(indicesOfDuplicates.elementAt(indexOfSurvivor)).numerosity += 
 									rulePopulation.getMacroclassifiersVector().get(indicesOfDuplicates.elementAt(k)).numerosity;
 								rulePopulation.getMacroclassifiersVector().get(indicesOfDuplicates.elementAt(indexOfSurvivor)).numberOfSubsumptions++;
 								rulePopulation.totalNumerosity += rulePopulation.getMacroclassifiersVector().get(indicesOfDuplicates.elementAt(k)).numerosity;
 								rulePopulation.deleteMacroclassifier(indicesOfDuplicates.elementAt(k));
 							}
 						}
 						
 					}	
 					
 					//if (indicesOfDuplicates.size() != 0) {
 						indicesOfDuplicates.clear();
 						fitnessOfDuplicates.clear();
 						experienceOfDuplicates.clear();
 					//}
 					
 				}				
 			}
 
 	
 
 	/**
 	 * Classify a single instance.
 	 * 
 	 * @param instance
 	 *            the instance to classify
 	 * @return the labels the instance is classified in
 	 */
 	public abstract int[] classifyInstance(double[] instance);
 	
 	
 	/*
 	 * diagrapse tous classifiers pou exoun dei ola ta instances alla den exoun summetasxei se kanena matchSet
 	 * 
 	 * erxetai me orisma population, olo ton plh9usmo
 	 * 
 	 * */
 
 	private void cleanUpZeroCoverageClassifiers(final ClassifierSet aSet) {
 
 		final int setSize = aSet.getNumberOfMacroclassifiers();
 
 		for (int i = setSize - 1; i >= 0; i--) { // giati anapoda? giati diagrafei apo vector, an to ekane apo tin arxi 9a diegrafe otinanai
 			final Classifier aClassifier = aSet.getClassifier(i);
 			final boolean zeroCoverage = (aClassifier.getCheckedInstances() >= instances.length)
 				// einai dunaton getCheckedInstances() > instances.length ? see classifier.384 [...]
 					&& (aClassifier.getCoverage() == 0);
 			if (zeroCoverage) {
 				// meta tin allagi stin ClassifierSet.geerateMatchset, opou diagrafetai opoios kanonas exei zero coverage, auti i klisi den exei pleon noima
 				//aSet.deleteClassifier(i); // de 9a eprepe na diagrapsei olo ton macroclassifier?
 				aSet.deleteMacroclassifier(i);
 			}
 		}
 
 	}
 
 	/**
 	 * Creates a new instance of the actual implementation of the LCS.
 	 * 
 	 * @return a pointer to the new instance.
 	 */
 	public abstract AbstractLearningClassifierSystem createNew();
 
 	/**
 	 * Execute hooks.
 	 * 
 	 * @param aSet
 	 *            the set on which to run the callbacks
 	 */
 	private void executeCallbacks(final ClassifierSet aSet, 
 								    final int repetition) {
 		
 		for (int i = 0; i < hooks.size(); i++) {
 			hooks.elementAt(i).getMetric(this);
 		}
 		
 		// to sort edo ginetai mono gia optikous logous kai afora mono sto population.txt
 /*		final SortPopulationControl srt = new SortPopulationControl(AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);
 		srt.controlPopulation(this.rulePopulation);*/
 		
 		int numberOfClassifiersCovered = 0;
 		int numberClassifiersGaed = 0;
 		int numberOfSubsumptions = 0;
 		double meanNs = 0;
 		
 		for (int i = 0; i < rulePopulation.getNumberOfMacroclassifiers(); i++) {
 			if (this.getRulePopulation().getMacroclassifier(i).myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_COVER) {
 				numberOfClassifiersCovered++;
 			}
 			else if (this.getRulePopulation().getMacroclassifier(i).myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_GA) {
 				numberClassifiersGaed++;
 			}
 			numberOfSubsumptions += this.getRulePopulation().getMacroclassifier(i).numberOfSubsumptions;
 			meanNs += this.getRulePopulation().getMacroclassifier(i).myClassifier.getNs();
 
 
 		}
 		
 		meanNs /= this.getRulePopulation().getNumberOfMacroclassifiers();
 		
 		try {
 
 			// record the rule population and its metrics in population.txt
 			final FileWriter fstream = new FileWriter(this.hookedMetricsFileDirectory + "/population_" + repetition +".txt", true);
 			final BufferedWriter buffer = new BufferedWriter(fstream);
 			buffer.write(					
 					  String.valueOf(this.repetition) + "th repetition:"
 					+ System.getProperty("line.separator")
 					+ System.getProperty("line.separator")
 					+ "Population size: " + rulePopulation.getNumberOfMacroclassifiers()
 					+ System.getProperty("line.separator")
 					+ "Timestamp: " + rulePopulation.totalGAInvocations
 					+ System.getProperty("line.separator")
 					+ "Classifiers in population covered :" + numberOfClassifiersCovered
 					+ System.getProperty("line.separator")
 					+ "Classifiers in population ga-ed :" 	+ numberClassifiersGaed
 					+ System.getProperty("line.separator")
 					+ "Covers occured: " + numberOfCoversOccured
 					+ System.getProperty("line.separator")
 					+ "Subsumptions: " + numberOfSubsumptions
 					+ System.getProperty("line.separator")
 					+ "Mean ns: " + meanNs
 					+ System.getProperty("line.separator")
 					//+ rulePopulation
 					+ System.getProperty("line.separator"));
 			buffer.flush();
 			buffer.close();
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}	
 		
 		this.numberOfCoversOccured = 0;
 	
 	}
 
 	/**
 	 * Return the LCS's classifier transform bridge.
 	 * 
 	 * @return the lcs's classifier transform bridge
 	 */
 	public final ClassifierTransformBridge getClassifierTransformBridge() {
 		return transformBridge;
 	}
 	
 	
 	public int getCummulativeCurrentInstanceIndex() {
 		return cummulativeCurrentInstanceIndex;
 	}
 
 	/**
 	 * Returns a string array of the names of the evaluation metrics.
 	 * 
 	 * @return a string array containing the evaluation names.
 	 */
 	public abstract String[] getEvaluationNames();
 
 	/**
 	 * Returns the evaluation metrics for the given test set.
 	 * 
 	 * @param testSet
 	 *            the test set on which to calculate the metrics
 	 * @return a double array containing the metrics
 	 */
 	public abstract double[] getEvaluations(Instances testSet);
 	
 
 	/**
 	 * Create a new classifier for the specific LCS.
 	 * 
 	 * @return the new classifier.
 	 */
 	public final Classifier getNewClassifier() {
 		return Classifier.createNewClassifier(this);
 	}
 
 	/**
 	 * Return a new classifier object for the specific LCS given a chromosome.
 	 * 
 	 * @param chromosome
 	 *            the chromosome to be replicated
 	 * @return a new classifier containing information about the LCS
 	 */
 	public final Classifier getNewClassifier(final ExtendedBitSet chromosome) {
 		return Classifier.createNewClassifier(this, chromosome);
 	}
 
 	/**
 	 * Getter for the rule population.
 	 * @return  a ClassifierSet containing the LCSs population
 	 * @uml.property  name="rulePopulation"
 	 */
 	public final ClassifierSet getRulePopulation() {
 		return rulePopulation;
 	}
 
 	/**
 	 * Returns the LCS's update strategy.
 	 * @return  the update strategy
 	 * @uml.property  name="updateStrategy"
 	 */
 	public final AbstractUpdateStrategy getUpdateStrategy() {
 		return updateStrategy;
 	}
 
 	
 	/**
 	 * collect the system's multilabel accuracy per iteration, plus every classifier's accuracy per iteration(TODO)
 	 * */
 	public void harvestAccuracies(int iteration){
 		
 		//double acc = getSystemMultilabelAccuracy();
 		final AccuracyRecallEvaluator trainingAccuracy = new AccuracyRecallEvaluator(trainSet, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
 
 		final VotingClassificationStrategy str = ((GenericMultiLabelRepresentation) transformBridge).new VotingClassificationStrategy((float) this.labelCardinality);
 		((GenericMultiLabelRepresentation) transformBridge).setClassificationStrategy(str);
 		str.proportionalCutCalibration(this.instances, rulePopulation);
 		
 		final AccuracyRecallEvaluator testingAccuracyWithPcut  = new AccuracyRecallEvaluator(testSet, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY);
 		
 
 		final MeanCoverageStatistic coverage = new MeanCoverageStatistic();
 
 		double trainAcc = trainingAccuracy.getMetric(this);
 		double testAccPcut = testingAccuracyWithPcut.getMetric(this);
 		double cov = coverage.getMetric(this);
 		
 		/*
 		systemAccuracy[iteration][0] =  testAcc;
 		systemAccuracy[iteration][1] =  trainAcc;
 		*/
 		
 		systemAccuracyInTraining.add((float) trainAcc);
 		systemAccuracyInTestingWithPcut.add((float) testAccPcut);
 		systemCoverage.add((float) cov);
 		
 		
 	}
 	
 	
 	/**
 	 * Initialize the rule population by clustering the train set and producing rules based upon the clusters.
 	 * The train set is initially divided in as many partitions as are the distinct label combinations.
 	 * @throws Exception 
 	 * 
 	 * @param file
 	 * 			the .arff file
 	 * */
 	public ClassifierSet initializePopulation (final String file) throws Exception {
 		
 		final double gamma = SettingsLoader.getNumericSetting("CLUSTER_GAMMA", .2);
 		
 		int numberOfLabels = (int) SettingsLoader.getNumericSetting("numberOfLabels", 1);
 		
 		final Instances set = InstancesUtility.openInstance(file);
 
 		SimpleKMeans kmeans = new SimpleKMeans();
 		kmeans.setSeed(10);
 		kmeans.setPreserveInstancesOrder(true);
 		
 		/*
 		 * o pinakas partitions 9a periexei deigmata mono me attributes, 
 		 * anti9etws, o partitionsWithCLasses mono tis katigories
 		 */
 		Instances[] partitions = InstancesUtility.partitionInstances(this, file);
 		Instances[] partitionsWithCLasses = InstancesUtility.partitionInstances(this, file);
 		
 
 		/*
 		 * anti na exoume pollaples 9eseis idiou sunduasmou labels, bale mono mia. 
 		 * auti 9a einai kai auti pou 9a xrisimopoii9ei sto cover pano sta centroids 
 		 */
 		for (int i = 0; i <  partitionsWithCLasses.length; i++) {
 			Instance temp = partitionsWithCLasses[i].instance(0);
 			partitionsWithCLasses[i].delete();
 			partitionsWithCLasses[i].add(temp);
 		}
 		
 
 		
 		/*
 		 * diagrafoume tis klaseis apo ta partitions 
 		 */
 		String attributesIndicesForDeletion = "";
 		
 		for (int k = set.numAttributes() - numberOfLabels + 1; k <= set.numAttributes(); k++) {
 			if (k != set.numAttributes())
 				attributesIndicesForDeletion += k + ",";
 			else
 				attributesIndicesForDeletion += k;
 		}
 		// attributesIncicesForDeletion = 8,9,10,11,12,13,14 e.g. gia 7 attributes kai 7 labels. den ksekinaei apo to 7 giati 9eorei oti o xristis dinei ton ari9mo (api)
 		for (int i = 0; i < partitions.length; i++) {
 		     Remove remove = new Remove();
 		     remove.setAttributeIndices(attributesIndicesForDeletion);
 		     remove.setInvertSelection(false);
 		     remove.setInputFormat(partitions[i]);
 		     partitions[i] = Filter.useFilter(partitions[i], remove);	
 		     //System.out.println(partitions[i]);
 		}
 		// partitions now contains only attributes
 		
 		/*
 		 * diagrafoume ta attributes apo ton partitionsWithCLasses
 		 */
 		String labelsIndicesForDeletion = "";
 
 		for (int k = 1; k <= set.numAttributes() - numberOfLabels; k++) {
 			if (k != set.numAttributes() - numberOfLabels)
 				labelsIndicesForDeletion += k + ",";
 			else
 				labelsIndicesForDeletion += k;
 		}
 		// labelsIndicesForDeletion = 1,2,3,4,5,6,7 e.g. gia 7 attributes kai 7 labels. den ksekinaei apo to 0 giati 9eorei oti o xristis dinei ton ari9mo (api)
 		for (int i = 0; i < partitionsWithCLasses.length; i++) {
 		     Remove remove = new Remove();
 		     remove.setAttributeIndices(labelsIndicesForDeletion);
 		     remove.setInvertSelection(false);
 		     remove.setInputFormat(partitionsWithCLasses[i]);
 		     partitionsWithCLasses[i] = Filter.useFilter(partitionsWithCLasses[i], remove);	
 		     //System.out.println(partitionsWithCLasses[i]);
 		}
 		// partitionsWithCLasses now contains only labels
 		
 		
 		
 		
 		int populationSize = (int) SettingsLoader.getNumericSetting("populationSize", 1500);
 		// to set opou 9a apo9ikeutoun oi kanones apo ola ta clusters
 		ClassifierSet initialClassifiers = new ClassifierSet(
 															new FixedSizeSetWorstFitnessDeletion(this,
 																	 populationSize,
 																	 new RouletteWheelSelector(AbstractUpdateStrategy.COMPARISON_MODE_DELETION, true)));
 
 		for (int i = 0; i < partitions.length; i++) {
 			
 			try {
 				
 				kmeans.setNumClusters((int) Math.ceil(gamma * partitions[i].numInstances()));
 				kmeans.buildClusterer(partitions[i]);
 				int[] assignments = kmeans.getAssignments();
 
 /*				int k=0;
 				for (int j = 0; j < assignments.length; j++) {
 					System.out.printf("Instance %d => Cluster %d ", k, assignments[j]);
 					k++;
 					System.out.println();
 
 				}
 				System.out.println();*/
 					
 				Instances centroids = kmeans.getClusterCentroids();
 				int numOfCentroidAttributes = centroids.numAttributes();
 				
 				
 				/*
 				 * ta centroids se auto to stadio exoun mono attributes. gia na sunexisoume
 				 * prepei prota na tous dosoume labels. einai auta ta opoia afairesame proigoumenos.
 				 * 
 				 * anoikse prota 9eseis gia attributes.
 				 */
 				
 				for (int j = 0; j < numberOfLabels; j++) {
 					Attribute label = new Attribute("label" + j);
 					centroids.insertAttributeAt(label, numOfCentroidAttributes + j);
 				}
 				
 				
 				
 /*				for (int centroidInstances = 0; centroidInstances < centroids.numInstances(); centroidInstances++) {
 					for (int labels = centroids.numAttributes() - numberOfLabels; labels < centroids.numAttributes(); labels++) {
 						centroids.instance(centroidInstances).setValue(labels, partitionsWithCLasses[i].instance(0).value(labels - numberOfLabels));
 					}
 				}*/
 				
 				for (int centroidInstances = 0; centroidInstances < centroids.numInstances(); centroidInstances++) {
 					for (int labels = 0; labels < numberOfLabels; labels++) {
 						centroids.instance(centroidInstances).setValue(numOfCentroidAttributes + labels, partitionsWithCLasses[i].instance(0).value(labels));
 					}
 				}
 
 				double[][] centroidsArray = InstancesUtility.convertIntancesToDouble(centroids);
 
 				for (int j = 0; j < centroidsArray.length; j++) {
 					//System.out.printf("Instance %d => Cluster %d ", k, assignments[j]);
 					final Classifier coveringClassifier = this.getClassifierTransformBridge().createRandomClusteringClassifier(centroidsArray[j]);
					
					coveringClassifier.buildMatchesForNewClassifier();
					
					for (int ins = 0; ins < this.instances.length; ins++) {
						coveringClassifier.isMatchUnCached(ins);
					}
					
 					coveringClassifier.setClassifierOrigin(Classifier.CLASSIFIER_ORIGIN_INIT); 
 					initialClassifiers.addClassifier(new Macroclassifier(coveringClassifier, 1), false);	
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println(initialClassifiers);
 		return initialClassifiers;
 	}
 	
 	
 	
 	/**
 	 * Initialize the rule population by clustering the train set and producing rules based upon the clusters.
 	 * The train set is initially divided in as many partitions as are the distinct label combinations.
 	 * @throws Exception 
 	 * 
 	 * @param trainSet
 	 * 				the type of Instances train set
 	 * */
 	
 	public ClassifierSet initializePopulation (final Instances trainset) throws Exception {
 		
 		final double gamma = SettingsLoader.getNumericSetting("CLUSTER_GAMMA", .2);
 		
 		int numberOfLabels = (int) SettingsLoader.getNumericSetting("numberOfLabels", 1);
 		
 		final Instances set = trainset;
 
 		SimpleKMeans kmeans = new SimpleKMeans();
 		kmeans.setSeed(10);
 		kmeans.setPreserveInstancesOrder(true);
 		
 		/*
 		 * o pinakas partitions 9a periexei deigmata mono me attributes, 
 		 * anti9etws, o partitionsWithCLasses mono tis katigories
 		 */
 		Instances[] partitions = InstancesUtility.partitionInstances(this, trainset);
 		Instances[] partitionsWithCLasses = InstancesUtility.partitionInstances(this, trainset);
 		
 
 		/*
 		 * anti na exoume pollaples 9eseis idiou sunduasmou labels, bale mono mia. 
 		 * auti 9a einai kai auti pou 9a xrisimopoii9ei sto cover pano sta centroids 
 		 */
 		for (int i = 0; i <  partitionsWithCLasses.length; i++) {
 			Instance temp = partitionsWithCLasses[i].instance(0);
 			partitionsWithCLasses[i].delete();
 			partitionsWithCLasses[i].add(temp);
 		}
 		
 		
 		/*
 		 * diagrafoume tis klaseis apo ta partitions 
 		 */
 		String attributesIndicesForDeletion = "";
 		
 		for (int k = set.numAttributes() - numberOfLabels + 1; k <= set.numAttributes(); k++) {
 			if (k != set.numAttributes())
 				attributesIndicesForDeletion += k + ",";
 			else
 				attributesIndicesForDeletion += k;
 		}
 		// attributesIncicesForDeletion = 8,9,10,11,12,13,14 e.g. gia 7 attributes kai 7 labels. den ksekinaei apo to 7 giati 9eorei oti o xristis dinei ton ari9mo (api)
 		for (int i = 0; i < partitions.length; i++) {
 		     Remove remove = new Remove();
 		     remove.setAttributeIndices(attributesIndicesForDeletion);
 		     remove.setInvertSelection(false);
 		     remove.setInputFormat(partitions[i]);
 		     partitions[i] = Filter.useFilter(partitions[i], remove);	
 		}
 		// partitions now contains only attributes
 		
 		/*
 		 * diagrafoume ta attributes apo ton partitionsWithCLasses
 		 */
 		String labelsIndicesForDeletion = "";
 
 		for (int k = 1; k <= set.numAttributes() - numberOfLabels; k++) {
 			if (k != set.numAttributes() - numberOfLabels)
 				labelsIndicesForDeletion += k + ",";
 			else
 				labelsIndicesForDeletion += k;
 		}
 		// labelsIndicesForDeletion = 1,2,3,4,5,6,7 e.g. gia 7 attributes kai 7 labels. den ksekinaei apo to 0 giati 9eorei oti o xristis dinei ton ari9mo (api)
 		for (int i = 0; i < partitionsWithCLasses.length; i++) {
 		     Remove remove = new Remove();
 		     remove.setAttributeIndices(labelsIndicesForDeletion);
 		     remove.setInvertSelection(false);
 		     remove.setInputFormat(partitionsWithCLasses[i]);
 		     partitionsWithCLasses[i] = Filter.useFilter(partitionsWithCLasses[i], remove);	
 		     //System.out.println(partitionsWithCLasses[i]);
 		}
 		// partitionsWithCLasses now contains only labels
 		
 		
 		
 		
 		int populationSize = (int) SettingsLoader.getNumericSetting("populationSize", 1500);
 		// to set opou 9a apo9ikeutoun oi kanones apo ola ta clusters
 		ClassifierSet initialClassifiers = new ClassifierSet(
 															new FixedSizeSetWorstFitnessDeletion(this,
 																	 populationSize,
 																	 new RouletteWheelSelector(AbstractUpdateStrategy.COMPARISON_MODE_DELETION, true)));
 
 		for (int i = 0; i < partitions.length; i++) {
 			
 			
 			try {
 				
 				kmeans.setNumClusters((int) Math.ceil(gamma * partitions[i].numInstances()));
 				kmeans.buildClusterer(partitions[i]);
 				int[] assignments = kmeans.getAssignments();
 				
 /*				int k=0;
 				for (int j = 0; j < assignments.length; j++) {
 					System.out.printf("Instance %d => Cluster %d ", k, assignments[j]);
 					k++;
 					System.out.println();
 
 				}
 				System.out.println();*/
 					
 				Instances centroids = kmeans.getClusterCentroids();
 
 				int numOfCentroidAttributes = centroids.numAttributes();
 				
 				for (int j = 0; j < numberOfLabels; j++) {
 					Attribute label = new Attribute("label" + j);
 					centroids.insertAttributeAt(label, numOfCentroidAttributes + j);
 				}
 				
 				
 				for (int centroidInstances = 0; centroidInstances < centroids.numInstances(); centroidInstances++) {
 					for (int labels = 0; labels < numberOfLabels; labels++) {
 						centroids.instance(centroidInstances).setValue(numOfCentroidAttributes + labels, partitionsWithCLasses[i].instance(0).value(labels));
 					}
 				}
 
 				//System.out.println(centroids);
 				double[][] centroidsArray = InstancesUtility.convertIntancesToDouble(centroids);
 
 				for (int j = 0; j < centroidsArray.length; j++) {
 					//System.out.printf("Instance %d => Cluster %d ", k, assignments[j]);
 					final Classifier coveringClassifier = this.getClassifierTransformBridge().createRandomCoveringClassifier(centroidsArray[j]);
					
					coveringClassifier.buildMatchesForNewClassifier();
					
					for (int ins = 0; ins < this.instances.length; ins++) {
						coveringClassifier.isMatchUnCached(ins);
					}
					
 					coveringClassifier.setClassifierOrigin(Classifier.CLASSIFIER_ORIGIN_INIT); 
 					initialClassifiers.addClassifier(new Macroclassifier(coveringClassifier, 1), false);	
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		//System.out.println(initialClassifiers);
 		return initialClassifiers;
 	}
 	
 	
 	
 	
 	/**
 	 * Prints the population classifiers of the LCS.
 	 */
 	public final void printSet() {
 		rulePopulation.print();
 	}
 	
 	
 
 	/**
 	 * Register an evaluator to be called during training.
 	 * 
 	 * @param evaluator
 	 *            the evaluator to register
 	 * @return true if the evaluator has been registered successfully
 	 */
 	public final boolean registerHook(final ILCSMetric evaluator) {
 		return hooks.add(evaluator);
 	}
 
 	/**
 	 * Registration of hooks to perform periodical inspection using metrics.
 	 * 
 	 * @param numberOfLabels 
 	 *				the dataset's number of labels. 
 	 *
 	 *@param instances
 	 *			the set of instances on which we will evaluate on. (train or test)
 	 *
 	 * @author alexandros filotheou
 	 * 
 	 * 
 	 * */
 	public void registerMultilabelHooks(double[][] instances, int numberOfLabels) {
 		
 		new FileLogger(this);
 				
 		this.registerHook(new FileLogger("accuracy",
 				new AccuracyRecallEvaluator(instances, false, this, AccuracyRecallEvaluator.TYPE_ACCURACY)));
 		
 		this.registerHook(new FileLogger("recall",
 				new AccuracyRecallEvaluator(instances, false, this, AccuracyRecallEvaluator.TYPE_RECALL)));
 		
 		this.registerHook(new FileLogger("exactMatch", 
 				new ExactMatchEvalutor(instances, false, this)));
 		
 		this.registerHook(new FileLogger("hamming", 
 				new HammingLossEvaluator(instances, false, numberOfLabels, this)));
 		
 		this.registerHook(new FileLogger("meanFitness",
 				new MeanFitnessStatistic(AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION)));
 		
 		this.registerHook(new FileLogger("meanCoverage",
 				new MeanCoverageStatistic()));
 		
 		this.registerHook(new FileLogger("weightedMeanCoverage",
 				new WeightedMeanCoverageStatistic(AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION)));
 		
 		this.registerHook(new FileLogger("meanAttributeSpecificity",
 				new MeanAttributeSpecificityStatistic()));
 		
 		this.registerHook(new FileLogger("weightedMeanAttributeSpecificity",
 				new WeightedMeanAttributeSpecificityStatistic(AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION)));
 		
 		this.registerHook(new FileLogger("meanLabelSpecificity",
 				new MeanLabelSpecificity(numberOfLabels)));
 		
 		this.registerHook(new FileLogger("weightedMeanLabelSpecificity",
 				new WeightedMeanLabelSpecificity(numberOfLabels, AbstractUpdateStrategy.COMPARISON_MODE_EXPLOITATION)));
 		
 		
 		if (SettingsLoader.getStringSetting("filename", "").indexOf("position") != -1) {
 
 			this.registerHook(new FileLogger("BAM", new PositionBAMEvaluator
 															((int) SettingsLoader.getNumericSetting("numberOfLabels", 1), 
 																	PositionBAMEvaluator.GENERIC_REPRESENTATION, this))); 
 		}
 		
 		if (SettingsLoader.getStringSetting("filename", "").indexOf("identity") != -1) {
 			this.registerHook(new FileLogger("BAM", new IdentityBAMEvaluator
 															((int) SettingsLoader.getNumericSetting("numberOfLabels", 1), 
 																	IdentityBAMEvaluator.GENERIC_REPRESENTATION, this)));
 		}
 		
 	}
 	
 	
 	
 	/**
 	 * Save the rules to the given filename.
 	 * 
 	 * @param filename
 	 */
 	public final void saveRules(String filename) {
 		ClassifierSet.saveClassifierSet(rulePopulation, filename);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param bridge
 	 *            the classifier transform bridge
 	 * @param update
 	 *            the update strategy
 	 */
 	public final void setElements(final ClassifierTransformBridge bridge,
 									final AbstractUpdateStrategy update) {
 		transformBridge = bridge;
 		updateStrategy = update;
 	}
 
 	/**
 	 * @param rate
 	 * @uml.property  name="hookCallbackRate"
 	 */
 	public void setHookCallbackRate(int rate) {
 		hookCallbackRate = rate;
 	}
 
 	
 	public void setHookedMetricsFileDirectory(String file) {
 		hookedMetricsFileDirectory = file;
 	}
 	
 	/**
 	 * Sets the LCS's population.
 	 * @param population  the new LCS's population
 	 * @uml.property  name="rulePopulation"
 	 */
 	public final void setRulePopulation(ClassifierSet population) {
 		rulePopulation = population;
 	}
 
 	/**
 	 * Run the LCS and train it.
 	 */
 	public abstract void train();
 
 	/**
 	 * Train population with all train instances and perform evolution.
 	 * 
 	 * @param iterations
 	 *            the number of full iterations (one iteration the LCS is
 	 *            trained with all instances) to train the LCS
 	 * @param population
 	 *            the population of the classifiers to train.
 	 */
 	protected final void trainSet(final int iterations,
 								    final ClassifierSet population) {
 		
 		trainSet(iterations, population, true); // evolve = true
 	}
 
 	/**
 	 * Train a classifier set with all train instances.
 	 * 
 	 * @param iterations
 	 *            the number of full iterations (one iteration the LCS is
 	 *            trained with all instances) to train the LCS
 	 * @param population
 	 *            the population of the classifiers to train.
 	 * @param evolve
 	 *            set true to evolve population, false to only update it
 	 *            
 	 *            
 	 *            ekteleitai gia iterations fores me evolve = true
 	 *            kai (int) 0.1 * iterations fores me evolve = false
 	 */
 	public final void trainSet(final int iterations,
 							     final ClassifierSet population, 
 							     final boolean evolve) {
 
 		
 		
 		final int numInstances = instances.length;
 
 		repetition = 0;
 		
 		int trainsBeforeHook = 0;
 		while (repetition < iterations) { 		
 			System.out.print("[");
 			// train  me olo to trainset gia {iterations} fores
 			while ((trainsBeforeHook < hookCallbackRate) && (repetition < iterations)) { // ap! allios 9a ksefeuge kai anti na ekteleito gia iterations
 				System.out.print('/');													  // 9a ekteleito gia iterations * hookCallBackRate
 				
 				for (int i = 0; i < numInstances; i++) {
 					cummulativeCurrentInstanceIndex = totalRepetition * instances.length + i;
 					trainWithInstance(population, i, evolve);
 					//harvestAccuracies(cummulativeCurrentInstanceIndex);
 
 				}
 				//harvestAccuracies(totalRepetition);
 
 				repetition++;
 				totalRepetition++;
 				trainsBeforeHook++;
 
 				// check for duplicities on every repetition
 				if (!thoroughlyCheckWIthPopulation) {
 					assimilateDuplicateClassifiers(rulePopulation, evolve);
 				}
 			}
 
 			if (hookCallbackRate < iterations) {
 				System.out.print("] ");
 				System.out.print("(" + repetition + "/" + iterations + ")");	
 			}
 			executeCallbacks(population, repetition); 
 			trainsBeforeHook = 0;
 		}
 	}
 
 	/**
 	 * Train with instance main template. Trains the classifier set with a
 	 * single instance.
 	 * 
 	 * @param population
 	 *            the classifier's population. olos o plh9usmos dld, [P]
 	 * @param dataInstanceIndex
 	 *            the index of the training data instance
 	 * @param evolve
 	 *            whether to evolve the set or just train by updating it
 	 */
 	public final void trainWithInstance(final ClassifierSet population, final int dataInstanceIndex, final boolean evolve) {
 		
 /*		final ClassifierSet matchSet = population.generateMatchSet(dataInstanceIndex);
 
 		getUpdateStrategy().updateSet(population, matchSet, dataInstanceIndex, evolve);*/
 		
 		long time1,time2;
 		
 		int index = totalRepetition * instances.length + dataInstanceIndex;
 		//System.out.println(index);
 		
 		if(smp)
 		{
 			time1 = -System.currentTimeMillis();
 			final ClassifierSet matchSetSmp = population.generateMatchSetNewSmp(dataInstanceIndex,pt);
 			time1 += System.currentTimeMillis();
 			
 			timeMeasurements[index][0] = population.getTotalNumerosity();
 			timeMeasurements[index][1] = population.getNumberOfMacroclassifiers();
 			timeMeasurements[index][2] = ClassifierSet.firstTimeSetSmp.getNumberOfMacroclassifiers();
 			timeMeasurements[index][3] = ClassifierSet.deleteIndicesSmp2.size();
 			timeMeasurements[index][4] = matchSetSmp.getNumberOfMacroclassifiers();
 			
 			if (UPDATE_MODE == UPDATE_MODE_IMMEDIATE) 
 				getUpdateStrategy().updateSetSmp(population, matchSetSmp, dataInstanceIndex, evolve);
 			else if (UPDATE_MODE == UPDATE_MODE_HOLD) 
 				getUpdateStrategy().updateSetNewSmp(population, matchSetSmp, dataInstanceIndex, evolve);				
 
 			recordInTimeMeasurements(population, index);
 
 		}
 		else
 		{
 			time1 = -System.currentTimeMillis();
 			final ClassifierSet matchSet = population.generateMatchSetCached(dataInstanceIndex);
 			time1 += System.currentTimeMillis();
 			
 			timeMeasurements[index][0] = population.getTotalNumerosity();
 			timeMeasurements[index][1] = population.getNumberOfMacroclassifiers();
 			//timeMeasurements[index][2] = population.sumOfUnmatched;
 			//timeMeasurements[index][3] = population.deleteIndices.size();
 			timeMeasurements[index][4] = matchSet.getNumberOfMacroclassifiers();
 			
 			
 			if (UPDATE_MODE == UPDATE_MODE_IMMEDIATE) 
 				getUpdateStrategy().updateSet(population, matchSet, dataInstanceIndex, evolve);
 			else if (UPDATE_MODE == UPDATE_MODE_HOLD) 
 				getUpdateStrategy().updateSetNew(population, matchSet, dataInstanceIndex, evolve);
 			
 			
 			recordInTimeMeasurements(population, index);
 
 		}
 		
 		if ((int) SettingsLoader.getNumericSetting("updateAlgorithmVersion", 3) == 3) {
 			timeMeasurements[index][33] = (int) ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).generateCorrectSetTime;
 			timeMeasurements[index][34] = (int) ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).updateParametersTime;
 			timeMeasurements[index][35] = (int) time1;
 			timeMeasurements[index][36] = (int) ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).selectionTime;
 		}
 		else if ((int) SettingsLoader.getNumericSetting("updateAlgorithmVersion", 3) == 4) {
 			timeMeasurements[index][33] = (int) ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).generateCorrectSetTime;
 			timeMeasurements[index][34] = (int) ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).updateParametersTime;
 			timeMeasurements[index][35] = (int) time1;
 			timeMeasurements[index][36] = (int) ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).selectionTime;
 		}
 
 		
 	}
 
 	
 	private void recordInTimeMeasurements(ClassifierSet population, int index) {
 		
 		if ((int) SettingsLoader.getNumericSetting("updateAlgorithmVersion", 3) == 3) {
 		
 			timeMeasurements[index][5] = ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).numberOfEvolutionsConducted;
 			timeMeasurements[index][6] = (int)((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).evolutionTime;
 			timeMeasurements[index][7] = ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).numberOfSubsumptionsConducted;
 			timeMeasurements[index][8] = (int)((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).subsumptionTime;
 			timeMeasurements[index][9] = ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).numberOfNewClassifiers;
 			timeMeasurements[index][19] = ((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).numberOfDeletionsConducted;
 			timeMeasurements[index][20] = (int)((MlASLCS3UpdateAlgorithm)(getUpdateStrategy())).deletionTime;
 		}
 		
 		else if ((int) SettingsLoader.getNumericSetting("updateAlgorithmVersion", 3) == 4) {
 			timeMeasurements[index][5] = ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).numberOfEvolutionsConducted;
 			timeMeasurements[index][6] = (int)((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).evolutionTime;
 			timeMeasurements[index][7] = ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).numberOfSubsumptionsConducted;
 			timeMeasurements[index][8] = (int)((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).subsumptionTime;
 			timeMeasurements[index][9] = ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).numberOfNewClassifiers;
 			timeMeasurements[index][19] = ((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).numberOfDeletionsConducted;
 			timeMeasurements[index][20] = (int)((MlASLCS4UpdateAlgorithm)(getUpdateStrategy())).deletionTime;
 		}
 		
 		
 		int numberOfMacroclassifiersCovered = 0;
 		int numberOfClassifiersCovered = 0;
 		
 		int numberOfMacroclassifiersGaed = 0;
 		int numberOfClassifiersGaed = 0;
 		
 		int numberOfMacroclassifiersInited = 0;
 		int numberOfClassifiersInited = 0;
 		
 		int numberOfSubsumptions = 0;
 		
 		double meanNs = 0;
 		
 		double meanAcc = 0;
 		double meanCoveredAcc = 0;
 		double meanGaedAcc = 0;
 		
 		double meanExplorationFitness = 0;
 		double meanCoveredExplorationFitness = 0;
 		double meanGaedExplorationFitness = 0;
 		
 		double meanPureFitness = 0;
 		double meanCoveredPureFitness = 0;
 		double meanGaedPureFitness = 0;
 		
 		for (int i = 0; i < population.getNumberOfMacroclassifiers(); i++) {
 			
 			Macroclassifier macro = population.getMacroclassifiersVector().get(i);
 			numberOfSubsumptions +=  macro.numberOfSubsumptions;
 			
 			if (macro.myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_COVER) {
 				numberOfMacroclassifiersCovered++;
 				numberOfClassifiersCovered += macro.numerosity;
 			}
 			else if (macro.myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_GA) {
 				numberOfMacroclassifiersGaed++;
 				numberOfClassifiersGaed += macro.numerosity;
 			}
 			else if (macro.myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_INIT) {
 				numberOfMacroclassifiersInited++;
 				numberOfClassifiersInited += macro.numerosity;
 			}
 			
 			meanAcc += 					macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_PURE_ACCURACY);
 			meanExplorationFitness += 	macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);
 			meanPureFitness += 			macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_PURE_FITNESS);
 			meanNs += population.getClassifier(i).getNs();
 			
 			if (macro.myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_COVER || macro.myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_INIT) {
 				
 				meanCoveredAcc += 					macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_PURE_ACCURACY);
 				meanCoveredExplorationFitness += 	macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);
 				meanCoveredPureFitness += 			macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_PURE_FITNESS);
 			}
 			else if (macro.myClassifier.getClassifierOrigin() == Classifier.CLASSIFIER_ORIGIN_GA) {
 				
 				meanGaedAcc += 					macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_PURE_ACCURACY);
 				meanGaedExplorationFitness += 	macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_EXPLORATION);
 				meanGaedPureFitness += 			macro.numerosity * macro.myClassifier.getComparisonValue(AbstractUpdateStrategy.COMPARISON_MODE_PURE_FITNESS);
 
 			}
 		}
 		
 		meanAcc /= population.getTotalNumerosity();
 		meanNs /= population.getNumberOfMacroclassifiers();
 		meanCoveredAcc /= (numberOfClassifiersCovered + numberOfClassifiersInited);
 		meanGaedAcc /= numberOfClassifiersGaed;
 		
 		meanExplorationFitness /= population.getTotalNumerosity();
 		meanCoveredExplorationFitness/= (numberOfClassifiersCovered + numberOfClassifiersInited);
 		meanGaedExplorationFitness /= numberOfClassifiersGaed;
 
 		meanPureFitness /= population.getTotalNumerosity();
 		meanCoveredPureFitness /= (numberOfClassifiersCovered + numberOfClassifiersInited);
 		meanGaedPureFitness /= numberOfClassifiersGaed;
 		
 		timeMeasurements[index][10] = (int) numberOfMacroclassifiersCovered;
 		timeMeasurements[index][11] = (int) numberOfMacroclassifiersGaed;
 		//timeMeasurements[index][12] = (int) ClassifierSet.firstTimeSetSmp.getNumberOfMacroclassifiers();
 		timeMeasurements[index][13] = (int) population.getTotalNumerosity();
 		timeMeasurements[index][14] = (int) population.firstDeletionFormula;
 		timeMeasurements[index][15] = (int) population.secondDeletionFormula;
 		timeMeasurements[index][21] = (int) population.coveredDeleted;
 		timeMeasurements[index][22] = (int) population.gaedDeleted;
 		timeMeasurements[index][16] = (int) numberOfSubsumptions;
 		timeMeasurements[index][17] = (int) meanCorrectSetNumerosity;
 		timeMeasurements[index][18] = (int) meanNs;
 		
 		timeMeasurements[index][23] = meanAcc;
 		timeMeasurements[index][24] = meanCoveredAcc;
 		timeMeasurements[index][25] = meanGaedAcc;
 		
 		timeMeasurements[index][26] = meanExplorationFitness;
 		timeMeasurements[index][27] = meanCoveredExplorationFitness;
 		timeMeasurements[index][28] = meanGaedExplorationFitness;
 		
 		timeMeasurements[index][29] = meanPureFitness;
 		timeMeasurements[index][30] = meanCoveredPureFitness;
 		timeMeasurements[index][31] = meanGaedPureFitness;
 		
 		timeMeasurements[index][32] = numberOfClassifiersDeletedInMatchSets;
 
 	}
 	
 	/**
 	 * Unregister an evaluator.
 	 * 
 	 * @param evaluator
 	 *            the evaluator to register
 	 * @return true if the evaluator has been unregisterd successfully
 	 */
 	public final boolean unregisterEvaluator(final ILCSMetric evaluator) {
 		return hooks.remove(evaluator);
 	}
 
 	/**
 	 * Update population with all train instances but do not perform evolution.
 	 * 
 	 * @param iterations
 	 *            the number of full iterations (one iteration the LCS is
 	 *            trained with all instances) to update the LCS
 	 * @param population
 	 *            the population of the classifiers to update.
 	 */
 	public final void updatePopulation(final int iterations,
 									   final ClassifierSet population) {
 		
 		trainSet(iterations, population, false); // evolve = false
 	}
 	
 	
 
 	
 
 	
 
 }
