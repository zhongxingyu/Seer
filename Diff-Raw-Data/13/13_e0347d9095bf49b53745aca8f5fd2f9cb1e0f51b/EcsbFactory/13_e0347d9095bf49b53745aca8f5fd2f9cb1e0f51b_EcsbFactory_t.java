 package ro.utcluj.ecsb.utils;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.log4j.Logger;
 import org.uncommons.maths.random.MersenneTwisterRNG;
 import org.uncommons.maths.random.Probability;
 import org.uncommons.watchmaker.framework.EvolutionEngine;
 import org.uncommons.watchmaker.framework.SelectionStrategy;
 import org.uncommons.watchmaker.framework.SequentialEvolutionEngine;
 import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
 import org.uncommons.watchmaker.framework.selection.*;
 import ro.utcluj.ecsb.ECSB;
 import ro.utcluj.ecsb.evaluation.DecsbFitnessEvaluator;
 import ro.utcluj.ecsb.evaluation.EcsbFitnessEvaluator;
 import ro.utcluj.ecsb.evaluation.EcsbModelEvaluator;
 import ro.utcluj.ecsb.metrics.FitnessMetric;
 import ro.utcluj.ecsb.metrics.MetricFactory;
 import ro.utcluj.ecsb.operators.EcsbCrossover;
 import ro.utcluj.ecsb.operators.EcsbMutation;
 import ro.utcluj.ecsb.population.EcsbCandidateFactory;
 import ro.utcluj.ecsb.population.EcsbIndividual;
 import ro.utcluj.ecsb.preprocess.ArffSpliter;
 import weka.classifiers.Classifier;
 import weka.classifiers.meta.CostSensitiveClassifier;
 import weka.core.AttributeStats;
 import weka.core.Instances;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.Random;
 
 /**
  * User: adibo
  * Date: 12.12.2011
  * Time: 16:29
  */
 public class EcsbFactory {
 
     private Properties configuration;
 
     public EcsbFactory(Properties configuration) {
         this.configuration = configuration;
     }
 
     public ECSB setUpECSB() {
 
         Instances[] splitDataset = splitDataset();
 
         EvolutionEngine<EcsbIndividual> engine = buildEvolutionEngine(splitDataset[0]);
 
         EcsbModelEvaluator modelEvaluator = buildValidator(splitDataset[0], splitDataset[1]);
 
         final int populationCount = Integer.parseInt(configuration.getProperty("population_count"));
         final int eliteCount = Integer.parseInt(configuration.getProperty("elite_count"));
         final int numberOfGenerations = Integer.parseInt(configuration.getProperty("number_of_generations"));
 
         return new ECSB(engine, numberOfGenerations, eliteCount, populationCount, modelEvaluator);
     }
 
     public EvolutionEngine<EcsbIndividual> buildEvolutionEngine(Instances trainSet) {
 
         final EcsbCandidateFactory candidateFactoryECSB = new EcsbCandidateFactory((float) 127.0);
 
         final Random rng = new MersenneTwisterRNG();
 
         EvolutionEngine<EcsbIndividual> engine = null;
         if (Boolean.valueOf(configuration.getProperty("distributed"))){
             ArffSpliter spliter = new ArffSpliter(trainSet,
                     new Path(configuration.getProperty("train.splits.dir")),
                     Integer.valueOf(configuration.getProperty("train.splits.number")));
             try {
                 spliter.split();
             } catch (IOException e) {
                 Logger.getLogger(EcsbFactory.class).error("Unable to split dataset.");
             }
 
             engine = new SequentialEvolutionEngine<EcsbIndividual>(candidateFactoryECSB,
                 buildEvolutionPipeline(),
                 new DecsbFitnessEvaluator(configuration),
                 buildStrategy(),
                 rng);
             //engine.addEvolutionObserver(new EcsbEvolutionObserver(fitnessEvaluator));
         }
         else{
             final EcsbFitnessEvaluator fitnessEvaluator = buildEvaluator(trainSet);
 
             engine = new SequentialEvolutionEngine<EcsbIndividual>(candidateFactoryECSB,
                     buildEvolutionPipeline(),
                     fitnessEvaluator,
                     buildStrategy(),
                     rng);
             engine.addEvolutionObserver(new EcsbEvolutionObserver(fitnessEvaluator));
         }
         return engine;
     }
 
     private EvolutionPipeline<EcsbIndividual> buildEvolutionPipeline() {
 
         final int crossoverPoints = Integer.parseInt(configuration.getProperty("crossover_points"));
         final EcsbCrossover crossoverECSB = new EcsbCrossover(crossoverPoints);
 
         final Probability mutationProbability = new Probability(Double.parseDouble(configuration.getProperty("mutation_rate")));
         final EcsbMutation mutationECSB = new EcsbMutation(mutationProbability);
 
         return new EvolutionPipeline<EcsbIndividual>(Arrays.asList(crossoverECSB, mutationECSB));
     }
 
     private Instances[] splitDataset() {
         final Instances instances = loadInstances(this.configuration.getProperty("dataset_path"));
         final int numFolds = Integer.valueOf(this.configuration.getProperty("num_folds"));
 
         instances.setClassIndex(getClassIndex(instances));
 
         instances.randomize(new Random(1));
         instances.stratify(numFolds);
 
         return new Instances[]{instances.trainCV(numFolds, 1), instances.testCV(numFolds, 1)};
     }
 
     private int getClassIndex(Instances instances) {
         return instances.numAttributes() - 1;
     }
 
     public EcsbFitnessEvaluator buildEvaluator(Instances trainSet) {
 
         AttributeStats classStats = trainSet.attributeStats(getClassIndex(trainSet));
 
         final int minorityClassIndex = classStats.nominalCounts[0] > classStats.nominalCounts[1] ? 1 : 0;
 
         final int numFolds = Integer.valueOf(this.configuration.getProperty("num_folds"));
 
         trainSet.randomize(new Random(1));
         trainSet.stratify(numFolds);
 
         final String baseClassifierName = this.configuration.getProperty("base_classifier");
 
         final Classifier costClassifier;
         try {
             Class<?> costClassifierClass = Class.forName(this.configuration.getProperty("cost_classifier"));
             costClassifier = (Classifier) costClassifierClass.newInstance();
             if (costClassifier instanceof CostSensitiveClassifier) {
                 ((CostSensitiveClassifier) costClassifier).setMinimizeExpectedCost(Boolean.parseBoolean(this.configuration.getProperty("use_reweight")));
             }
             final FitnessMetric fitnessMetric = buildFitnessMetric(this.configuration.getProperty("fitness_metric"), this.configuration.getProperty("beta"));
 
             return new EcsbFitnessEvaluator(trainSet, costClassifier, numFolds, baseClassifierName, fitnessMetric, minorityClassIndex);
         } catch (Exception e) {
             Logger.getLogger("ECSBLog").error("Unable to create the cost classifier. Fitness evaluator creation aborted.");
         }
         return null;
     }
 
     public EcsbModelEvaluator buildValidator(Instances trainSet, Instances testSet) {
 
         final String baseClassifierName = this.configuration.getProperty("base_classifier");
 
         final Classifier costClassifier;
 
         try {
             Class<?> costClassifierClass = Class.forName(this.configuration.getProperty("cost_classifier"));
             costClassifier = (Classifier) costClassifierClass.newInstance();
             if (costClassifier instanceof CostSensitiveClassifier) {
                 ((CostSensitiveClassifier) costClassifier).setMinimizeExpectedCost(Boolean.parseBoolean(this.configuration.getProperty("use_reweight")));
             }
             return new EcsbModelEvaluator(trainSet, testSet, costClassifier, baseClassifierName);
         } catch (Exception e) {
             Logger.getLogger("ECSBLog").error("Unable to create validator.");
         }
         return null;
     }
 
     private Instances loadInstances(String datasetPath) {
         Instances instances = null;
         try {
            //BufferedReader instancesReader = new BufferedReader(new FileReader(datasetPath));
            BufferedReader instancesReader =
                    new BufferedReader(new FileReader(
                            EcsbFactory.class.getClassLoader().getResource(configuration.getProperty("dataset_path")).getPath()));

             instances = new Instances(instancesReader);
             instancesReader.close(); // not safe
         } catch (Exception e) {
             Logger.getLogger("ECSBLog").error("Unable to load dataset.");
         }
         return instances;
     }
 
     private FitnessMetric buildFitnessMetric(String fitnessMetricName, String beta) {
         if ("GMMetric".equals(fitnessMetricName)) {
             return MetricFactory.getAGMMetric();
         } else if ("BAccMetric".equals(fitnessMetricName)) {
             return MetricFactory.getAGBAccMetric();
         } else if ("FMeasureMetric".equals(fitnessMetricName)) {
             return MetricFactory.getAFMeasureMetric(Double.valueOf(beta));
         } else if ("LinTPPrecisionMetric".equals(fitnessMetricName)) {
             return MetricFactory.getALinTPPrecisionMetric(Double.valueOf(beta));
         } else if ("LinTPTNMetric".equals(fitnessMetricName)) {
             return MetricFactory.getALinTPTNMetric(Double.valueOf(beta));
         } else {
             Logger.getLogger("ECSBLog").error("Unable to create the fitness metric.");
             return null;
         }
     }
 
     private SelectionStrategy<? super EcsbIndividual> buildStrategy() {
         final String strategyName = configuration.getProperty("selection_strategy");
         final double selectionParam = Double.parseDouble(configuration.getProperty("selection_param"));
 
         if (strategyName.contains("RankSelection")) {
             return new RankSelection();
         } else if (strategyName.contains("RouletteWheelSelection")) {
             return new RouletteWheelSelection();
         } else if (strategyName.contains("StochasticUniversalSampling")) {
             return new StochasticUniversalSampling();
         } else if (strategyName.contains("TournamentSelection")) {
             return new TournamentSelection(new Probability(selectionParam));
         } else if (strategyName.contains("TruncationSelection")) {
             return new TruncationSelection(selectionParam);
         } else {
             Logger.getLogger("ECSBLog").error("Unable to create the fitness metric.");
             return null;
         }
     }
 }
