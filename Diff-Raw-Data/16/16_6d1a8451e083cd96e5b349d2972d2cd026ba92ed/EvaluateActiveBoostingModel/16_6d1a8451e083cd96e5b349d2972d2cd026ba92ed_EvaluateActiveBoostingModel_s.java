 package tr.gov.ulakbim.jDenetX.tasks;
 
 import tr.gov.ulakbim.jDenetX.classifiers.Classifier;
 import tr.gov.ulakbim.jDenetX.classifiers.SelfOzaBoost;
 import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
 import tr.gov.ulakbim.jDenetX.core.VotedInstance;
 import tr.gov.ulakbim.jDenetX.core.VotedInstancePool;
 import tr.gov.ulakbim.jDenetX.evaluation.ClassificationPerformanceEvaluator;
 import tr.gov.ulakbim.jDenetX.evaluation.LearningEvaluation;
 import tr.gov.ulakbim.jDenetX.options.ClassOption;
 import tr.gov.ulakbim.jDenetX.options.IntOption;
 import tr.gov.ulakbim.jDenetX.streams.CachedInstancesStream;
 import tr.gov.ulakbim.jDenetX.streams.InstanceStream;
 import weka.clusterers.ClusterEvaluation;
 import weka.clusterers.SimpleKMeans;
 import weka.clusterers.XMeans;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.Remove;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: caglar
  * Date: Oct 17, 2010
  * Time: 1:27:41 AM
  * To change this template use File | Settings | File Templates.
  */
 public class EvaluateActiveBoostingModel extends MainTask {
     @Override
     public String getPurposeString() {
         return "Evaluates a Cotrain model on a stream.";
     }
 
     /**
      * Generated Serial ID:
      */
     private static final long serialVersionUID = 1586815404797353243L;
 
     //private static final int PoolSizeRatio = 10;
     public ClassOption modelOption = new ClassOption("model", 'm',
             "Classifier to evaluate.", Classifier.class, "LearnClusterBaggingModel");
 
     public ClassOption testStreamOption = new ClassOption("test_stream",
             't', "test on a Stream to evaluate on.", InstanceStream.class,
             "generators.RandomTreeGenerator");
 
     public ClassOption streamOption = new ClassOption("stream", 's',
             "Stream to evaluate on.", InstanceStream.class,
             "generators.RandomTreeGenerator");
 
     public ClassOption evaluatorOption = new ClassOption("evaluator", 'e',
             "Classification performance evaluation method.",
             ClassificationPerformanceEvaluator.class,
             "BasicClassificationPerformanceEvaluator");
 
     public IntOption maxInstancesOption = new IntOption("maxInstances", 'i',
             "Maximum number of instances to test.", 1000000, 0,
             Integer.MAX_VALUE);
 
     public IntOption poolSizeOption = new IntOption("poolRatio", 'p',
             "Maximum amount ratio for pool size.", 1000, 0,
             Integer.MAX_VALUE);
 
     private static final int SAMPLING_LIMIT = 10;
     private static SelfOzaBoost model;
     private static int noOfClassesInPool = 1;
 
     public EvaluateActiveBoostingModel() {
     }
 
     public EvaluateActiveBoostingModel(Classifier model, InstanceStream stream,
                                        ClassificationPerformanceEvaluator evaluator, int maxInstances) {
         this.modelOption.setCurrentObject(model);
         this.streamOption.setCurrentObject(stream);
         this.evaluatorOption.setCurrentObject(evaluator);
         this.maxInstancesOption.setValue(maxInstances);
     }
 
     public Class<?> getTaskResultType() {
         return LearningEvaluation.class;
     }
 
     public Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
         //if (this.modelOption.getValueAsCLIString().equals("SelfOzaBoost")) {
         model = (SelfOzaBoost) getPreparedClassOption(this.modelOption);
         //}
 
         InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);
         InstanceStream testStream = (InstanceStream) getPreparedClassOption(this.testStreamOption);
         ClassificationPerformanceEvaluator evaluator = (ClassificationPerformanceEvaluator) getPreparedClassOption(this.evaluatorOption);
         Instance testInst = null;
         int maxInstances = this.maxInstancesOption.getValue();
         long instancesProcessed = 0;
         monitor.setCurrentActivity("Evaluating model...", -1.0);
         while (stream.hasMoreInstances()
                 && ((maxInstances < 0) || (instancesProcessed < maxInstances))) {
             testInst = (Instance) stream.nextInstance().copy();
             int trueClass = (int) testInst.classValue();
             testInst.setClassMissing();
             double[] prediction = model.getVotesForInstance(testInst);
             evaluator.addClassificationAttempt(trueClass, prediction, testInst
                     .weight());
             instancesProcessed++;
             if (instancesProcessed % INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                 if (monitor.taskShouldAbort()) {
                     return null;
                 }
                 long estimatedRemainingInstances = stream
                         .estimatedRemainingInstances();
                 if (maxInstances > 0) {
                     long maxRemaining = maxInstances - instancesProcessed;
                     if ((estimatedRemainingInstances < 0)
                             || (maxRemaining < estimatedRemainingInstances)) {
                         estimatedRemainingInstances = maxRemaining;
                     }
                 }
                 monitor
                         .setCurrentActivityFractionComplete(estimatedRemainingInstances < 0 ? -1.0
                                 : (double) instancesProcessed
                                 / (double) (instancesProcessed + estimatedRemainingInstances));
                 if (monitor.resultPreviewRequested()) {
                     monitor.setLatestResultPreview(new LearningEvaluation(
                             evaluator.getPerformanceMeasurements()));
                 }
             }
         }
 
         int maxInstances2 = this.maxInstancesOption.getValue();
         long instancesProcessed2 = 0;
         int poolSizeRatio = poolSizeOption.getValue();
         int poolLimit = maxInstances / poolSizeRatio;
         int poolCount = 0;
         VotedInstancePool vInstPool = SelfOzaBoost.getVotedInstancePool();
         noOfClassesInPool = vInstPool.getNoOfClasses();
         System.out.println("No of instances in the pool: " + vInstPool.getSize());
         System.out.println("No of classes in the pool: " + noOfClassesInPool);
         if (vInstPool.getSize() > 10) {
             ArrayList<Attribute> attrs = new ArrayList<Attribute>();
             for (int i = 0; i < testInst.numAttributes(); i++) {
                 attrs.add(testInst.attribute(i));
             }
             Instances instances = new Instances("instances", attrs, vInstPool.getSize());
             Iterator instanceIt = vInstPool.iterator();
             System.out.println("Size of pool: " + vInstPool.getSize());
             instancesProcessed = 0;
 
             //System.out.println("Size of instances: " + instances.size() + " All of them: " + instances.toString());
 
             while (instanceIt.hasNext() && poolCount < poolLimit) {
                 VotedInstance vInstance = (VotedInstance) instanceIt.next();
                 ((Instances) instances).add(vInstance.getInstance());
                 poolCount++;
             }
             System.out.println(SelfOzaBoost.instConfCount);
             System.out.println("Size of instances: " + instances.size());
             instances = clusterInstances(instances);
             InstanceStream activeStream = new CachedInstancesStream((Instances) instances);
 
             System.out.println("Selftraining have been started");
             System.out.println("Number of self training instances: " + instances.numInstances());
 
             // Self-train model now
             long treeSize = vInstPool.getSize();
             long limit = treeSize / SAMPLING_LIMIT;
             Instance inst = null;
 
             for (long j = 0; j < limit && activeStream.hasMoreInstances(); j++) {
                 inst = activeStream.nextInstance();
                 if (inst.numAttributes() == attrs.size()) {
                     model.trainOnInstance(inst);
                 }
             }
         }
 
         while (testStream.hasMoreInstances()
                 && ((maxInstances < 0) || (instancesProcessed2 < maxInstances2))) {
             testInst = (Instance) testStream.nextInstance().copy();
             int trueClass = (int) testInst.classValue();
             testInst.setClassMissing();
             double[] prediction = model.getVotesForInstance(testInst);
             evaluator.addClassificationAttempt(trueClass, prediction, testInst
                     .weight());
             instancesProcessed2++;
             if (instancesProcessed2 % INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                 if (monitor.taskShouldAbort()) {
                     return null;
                 }
                 long estimatedRemainingInstances = testStream
                         .estimatedRemainingInstances();
                 if (maxInstances2 > 0) {
                     long maxRemaining = maxInstances2 - instancesProcessed2;
                     if ((estimatedRemainingInstances < 0)
                             || (maxRemaining < estimatedRemainingInstances)) {
                         estimatedRemainingInstances = maxRemaining;
                     }
                 }
                 monitor
                         .setCurrentActivityFractionComplete(estimatedRemainingInstances < 0 ? -1.0
                                 : (double) instancesProcessed2
                                 / (double) (instancesProcessed2 + estimatedRemainingInstances));
                 if (monitor.resultPreviewRequested()) {
                     monitor.setLatestResultPreview(new LearningEvaluation(
                             evaluator.getPerformanceMeasurements()));
                 }
             }
         }
         return new LearningEvaluation(evaluator.getPerformanceMeasurements());
     }
 
     public Instances clusteredInstances(Instances data) {
         if (data == null) {
             throw new NullPointerException("Data is null at clusteredInstances method");
         }
         Instances sampled_data = data;
         for (int i = 0; i < sampled_data.numInstances(); i++) {
             sampled_data.remove(i);
         }
         // System.out.println(data);
         SimpleKMeans sKmeans = new SimpleKMeans();
         data.setClassIndex(data.numAttributes() - 1);
         Remove filter = new Remove();
         filter.setAttributeIndices("" + (data.classIndex() + 1));
         List assignments = new ArrayList();
         int numberOfEnsembles = model.ensembleSizeOption.getValue();
         try {
             filter.setInputFormat(data);
             Instances dataClusterer = Filter.useFilter(data, filter);
             String[] options = new String[3];
             options[0] = "-I";                 // max. iterations
             options[1] = "500";
             options[2] = "-O";
             sKmeans.setNumClusters(data.numClasses());
             sKmeans.setOptions(options);
             sKmeans.buildClusterer(dataClusterer);
             System.out.println("Kmeans\n:" + sKmeans);
             System.out.println(Arrays.toString(sKmeans.getAssignments()));
             assignments = Arrays.asList(sKmeans.getAssignments());
         } catch (Exception e) {
             e.printStackTrace();
         }
         System.out.println("Assignments\n: " + assignments);
         ClusterEvaluation eval = new ClusterEvaluation();
         eval.setClusterer(sKmeans);
         try {
             eval.evaluateClusterer(data);
         } catch (Exception e) {
             e.printStackTrace();
         }
         int classesToClustersMap[] = eval.getClassesToClusters();
         for (int i = 0; i < classesToClustersMap.length; i++) {
            if (assignments.equals((Integer) classesToClustersMap[(int) data.get(i).classValue()])) {
                 ((Instances) sampled_data).add(data.get(i));
             }
         }
         return ((Instances) sampled_data);
     }
 
     public static Instances clusterInstances(Instances data) {
         XMeans xmeans = new XMeans();
         Remove filter = new Remove();
         Instances dataClusterer = null;
         if (data == null) {
             throw new NullPointerException("Data is null at clusteredInstances method");
         }
         //Get the attributes from the data for creating the sampled_data object
         ArrayList<Attribute> attrList = new ArrayList<Attribute>();
         Enumeration attributes = data.enumerateAttributes();
         while (attributes.hasMoreElements()) {
             attrList.add((Attribute) attributes.nextElement());
         }
         Instances sampled_data = new Instances(data.relationName(), attrList, 0);
         data.setClassIndex(data.numAttributes() - 1);
         sampled_data.setClassIndex(data.numAttributes() - 1);
         filter.setAttributeIndices("" + (data.classIndex() + 1));
         //int numberOfEnsembles = model.ensembleSizeOption.getValue();
         data.remove(0);//In Wavelet Stream of MOA always the first element comes without class
         try {
             filter.setInputFormat(data);
             dataClusterer = Filter.useFilter(data, filter);
             String[] options = new String[4];
             options[0] = "-L";                 // max. iterations
             options[1] = Integer.toString(noOfClassesInPool - 1);
             if (noOfClassesInPool > 2) {
                 options[1] = Integer.toString(noOfClassesInPool - 1);
                 xmeans.setMinNumClusters(noOfClassesInPool - 1);
             } else {
                 options[1] = Integer.toString(noOfClassesInPool);
                 xmeans.setMinNumClusters(noOfClassesInPool);
             }
             xmeans.setMaxNumClusters(data.numClasses() + 1);
             System.out.println("No of classes in the pool: " + noOfClassesInPool);
             xmeans.setUseKDTree(true);
             //xmeans.setOptions(options);
             xmeans.buildClusterer(dataClusterer);
             System.out.println("Xmeans\n:" + xmeans);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         //System.out.println("Assignments\n: " + assignments);
         ClusterEvaluation eval = new ClusterEvaluation();
         eval.setClusterer(xmeans);
         try {
             eval.evaluateClusterer(data);
             int classesToClustersMap[] = eval.getClassesToClusters();
             //check the classes to cluster map
             int clusterNo = 0;
             for (int i = 0; i < data.size(); i++) {
                 clusterNo = xmeans.clusterInstance(dataClusterer.get(i));
                 //Check if the class value of instance and class value of cluster matches
                 if ((int) data.get(i).classValue() == classesToClustersMap[clusterNo]) {
                     sampled_data.add(data.get(i));
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ((Instances) sampled_data);
     }
 
 
 }
