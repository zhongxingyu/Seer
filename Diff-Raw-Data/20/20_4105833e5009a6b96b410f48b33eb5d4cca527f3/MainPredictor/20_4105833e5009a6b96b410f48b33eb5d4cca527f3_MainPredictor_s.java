 /*
  * Combines different predictors with neural network predictor
  * License: GPL
  */
 package proteinprediction.prediction;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import proteinprediction.ProgramSettings;
 import weka.classifiers.Evaluation;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.converters.ArffSaver;
 
 /**
  * Combines different predictors with neural network predictor
  * @author Shen Wei
  */
 public class MainPredictor  implements Serializable {
     
     private static final long serialVersionUID = 84018477L;
     /**
      * neural network for combining results of other predictors
      */
     private WekaPredictor neuralNetwork;
     
     /**
      * set of low-level predictors
      */
     public final WekaPredictor[] predictors;
     
     /**
      * high-level training / prediction set for neural network
      */
     private Instances highlevelSet;
     
     /**
      * stores prediction scores
      */
     private double[] scores = null;
     
     /**
      * scores of low-level predictors
      */
     private List<double[]> lowlevelScores;
     
     public MainPredictor(){
         this.neuralNetwork = new MultilayerPerceptronPredictor();
         this.predictors = new WekaPredictor[] {
             new J48Predictor(),
             //new NaiveBayesPredictor(),
             //new SVMPredictor(),
             //new RBFNetworkPredictor(),
             new VotedPerceptronPredictor(),
             new SimpleLogisticPredictor()
         };
         this.lowlevelScores = new ArrayList<double[]>();
         this.highlevelSet = null;
     }
     
     /**
      * load trained predictor model
      * @param model input file
      * @return
      * @throws IOException
      * @throws ClassNotFoundException 
      */
     public void loadModel() 
             throws IOException, ClassNotFoundException 
     {
         this.neuralNetwork.loadModel();
         
         //load low-level predictors
         for (WekaPredictor predictor : predictors) {
             predictor.loadModel();
         }
     }
     
     /**
      * save this trained predictor to a model file
      * @param model
      * @throws IOException 
      */
     public void saveModel() 
             throws IOException 
     {
         this.neuralNetwork.saveModel();
     }
     
     /**
      * train neural network with results of other predictors
      * @param dataset
      * @throws IOException
      * @throws Exception 
      */
     public void train(Instances dataset) 
             throws IOException, Exception 
     {        
         dataset.setClassIndex(dataset.numAttributes() - 1);
         
         //train low-level predictors at first
         for (WekaPredictor predictor : predictors) {
             //save model
             System.err.println("Training " 
                     + predictor.classifier.getClass().getSimpleName() + " ...");
             predictor.train(dataset, null);
            predictor.saveModel();
         }
         
         //generate training set for neural network
         System.err.println("Generating new training set for neural network ...");
         Instances trainSet = generateHighLevelSet(dataset, true);
         
         //train neural network
         System.err.println("Training neural network ...");
         neuralNetwork.train(trainSet, null);
        neuralNetwork.saveModel();
     }
     
     /**
      * perform prediction over all instances
      * @param dataset
      * @return
      * @throws Exception 
      */
     public double[] predict(Instances dataset) 
             throws Exception {
         this.highlevelSet = generateHighLevelSet(dataset, false);
         
         ArffSaver saver = new ArffSaver();
         saver.setFile(new File(ProgramSettings.RESULT_DIR, "intermediate_result.arff"));
         saver.setInstances(this.highlevelSet);
         saver.writeBatch();
         
         double[] values = new double[this.highlevelSet.numInstances()];
         this.scores = new double[values.length];
         for (int i = 0; i < values.length; i++) {
             Instance inst = highlevelSet.instance(i);
             values[i] = this.neuralNetwork.predictInstance(inst);
             double distribution[] = neuralNetwork.classifier
                     .distributionForInstance(inst);
             this.scores[i] = distribution[(int) values[i]];
         }
         return values;
     }
     
     /**
      * generate high level training/test set for neural network
      * @param dataset
      * @param hasClass whether the new set contains class attribute
      * @return
      * @throws Exception 
      */
     private Instances generateHighLevelSet(Instances dataset, boolean hasClass) 
             throws Exception 
     {
         FastVector attrInfo = new FastVector();
         
         this.lowlevelScores.clear();
         
         //add result attributes
         for (int i = 0; i < predictors.length; i++) {
             WekaPredictor predictor = predictors[i];
             attrInfo.addElement(predictor.getResultNumericAttribute());
             this.lowlevelScores.add(new double[dataset.numInstances()]);
         }
         
         //add class attribute
         attrInfo.addElement(
                 dataset.attribute(dataset.numAttributes() - 1));
         
         Instances trainSet = new Instances(
                 "NeuralNetwork_dataset", 
                 attrInfo, 
                 0);
         trainSet.setClassIndex(trainSet.numAttributes() - 1);
         Enumeration enm = dataset.enumerateInstances();
         int instanceId = 0;
         while (enm.hasMoreElements()) {
             Instance inst = (Instance) enm.nextElement();
             Instance newInst = new Instance(trainSet.numAttributes());
             newInst.setDataset(trainSet);
             
             if (hasClass) {
                 //set class label
                 newInst.setClassValue(inst.classValue());
             } else {
                 newInst.setClassMissing();
             }
 
             //set prediction result for each predictors
             for (int i = 0; i < predictors.length; i++) {
                 //double value = predictors[i].predictInstance(inst);
                 double score = predictors[i].predictionScore(inst)[0];
                 this.lowlevelScores.get(i)[instanceId] = score;
                 newInst.setValue(i, score);
             }
             instanceId++;
             trainSet.add(newInst);
         }
         return trainSet;
     }
     
     /**
      * get prediction results of low-level predictors
      * @return 
      */
     public Instances getIntermediateResults() {
         return this.highlevelSet;
     }
     
     public Evaluation[] evaluate(Instances testSet) throws Exception {
         testSet.setClassIndex(testSet.numAttributes() - 1);
         Evaluation evals[] = new Evaluation[predictors.length + 1];
         //for low-level predictors
         int idx = 0;
         for (WekaPredictor predictor : predictors) {
             Evaluation eval = new Evaluation(testSet);
             eval.evaluateModel(predictor.classifier, testSet);
             evals[idx++] = eval;
         }
         
         //for top-level predictor
         Instances highlevel = generateHighLevelSet(testSet, true);
         evals[evals.length - 1] = new Evaluation(highlevel);
         evals[evals.length - 1].evaluateModel(
                 this.neuralNetwork.classifier, highlevel);
         
         return evals;
     }
     
     /**
      * get scores for prediction
      * @return 
      */
     public double[] getPredictionScores() {
         if (this.scores == null) {
             throw new IllegalStateException("Prediction not performed!");
         }
         return this.scores;
     }
     
     /**
      * get prediction scores of low-level predictors
      * @return 
      */
     public List<double[]> getLowlevelPredictionScores() {
         if (this.scores == null) {
             throw new IllegalStateException("Prediction not performed!");
         }
         return this.lowlevelScores;
     }
 }
