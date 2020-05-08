 /*
  * Entry point of predictors
  * License: GPL
  */
 package proteinprediction;
 
 import proteinprediction.io.FastaWriter;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import org.apache.commons.io.output.TeeOutputStream;
 import org.apache.commons.lang3.StringUtils;
 import proteinprediction.RunOptions.RunActions;
 import proteinprediction.prediction.MainPredictor;
 import proteinprediction.utils.DatasetGenerator;
 import proteinprediction.utils.DatasetPreprocessor;
 import weka.classifiers.Evaluation;
 import weka.core.Attribute;
 import weka.core.Instances;
 import weka.core.converters.ArffSaver;
 import java.util.LinkedList;
 import java.util.List;
 import proteinprediction.prediction.MetaPredictor;
 
 /**
  * Entry point of predictors
  * @author Shen Wei
  */
 public class Main {
 
     /**
      * file contains all selected features
      */
     public static final File fileFeatures =
             new File(ProgramSettings.DATASET_DIR, "features.txt");
     /**
      * attribute name of prediction result
      */
     public static final String predictionResultAttr = "TMH_TML_prediction";
     
     /**
      * attribute name for prediction scores
      */
     public static final String predictionScoreAttr = "TMH_TML_prediction_score";
 
     /**
      * main entry point
      * @param args 
      */
     public static void main(String[] args) {
 
         //initialize
         ProgramSettings.initialize();
 
         //parse arguments
         RunOptions option = RunOptions.parseArguments(args);
         try {
             if (option.getRunAction() == RunActions.ACTION_TRAIN) {
                 //training
                 train(option);
             } else if (option.getRunAction() == RunActions.ACTION_PREDICT) {
                 //predicting
                 predict(option);
             } else if (option.getRunAction() == RunActions.ACTION_VALIDATE) {
                 //validation
                 validate(option);
             } else if (option.getRunAction() == RunActions.ACTION_DATA) {
                 //prepare data
                 prepareData(option);
             } else if (option.getRunAction() == RunActions.ACTION_TRAIN_META) {
                 //train meta predictor
                 trainMetaPredictor(option);
             } else if (option.getRunAction() == RunActions.ACTION_PREDICT_META) {
                 //prediction through meta predictor
                 predictMetaPredictor(option);
             } else {
                 //show help
                 showHelp();
             }
         } catch (Exception e) {
             //System.err.println(e.getLocalizedMessage());
             e.printStackTrace();
         }
     }
 
     /**
      * print usage information
      */
     private static void showHelp() {
         System.err.println(
                 "Predicts transmembrane helices and transmembrane loops.");
         System.err.println(RunOptions.getUsageString());
     }
 
     /**
      * perform predictions
      * @param option 
      */
     private static void predict(RunOptions option)
             throws IOException, ClassNotFoundException, Exception {
         String inputArff = option.inputArff;
         String outputArff = option.outputArff;
         String outputFasta = option.outputFasta;
 
         System.err.println("Loading data set ...");
         Instances dataset = new Instances(new FileReader(inputArff));
         Instances original = dataset;
 
         System.err.println("Check and reduce feature space ...");
         String features = loadSelectedAttributes();
         String featureIDs =
                 getIndicesOfSelectedFeatures(dataset, features.split(","));
         dataset = DatasetPreprocessor.selectFeatures(
                 dataset, featureIDs);
 
         System.err.println("Loading prediction model ...");
         MainPredictor predictor = new MainPredictor();
         predictor.loadModel();
 
         System.err.println("Predicting ...");
         dataset.setClassIndex(dataset.numAttributes() - 1);
         double[] result = predictor.predict(dataset);
         double[] scores = predictor.getPredictionScores();
 
         System.err.println("Writing results ...");
         // output fasta file
         if (outputFasta != null) {
             try {
                 FastaWriter fw = new FastaWriter(new File(
                         ProgramSettings.RESULT_DIR, outputFasta));
                 fw.writeDataset(original, result);
                 fw.close();
             } catch (Exception e) {
                 System.err.println("Error writing Fasta output!");
                 System.err.println(e);
                 e.printStackTrace();
             }
         }
         
         //add low-level prediction scores
         List<double[]> lowlevelScores = predictor.getLowlevelPredictionScores();
         int scoreIndices[] = new int[lowlevelScores.size()];
         for (int i = 0; i < predictor.predictors.length; i++) {
             String attrName = String.format(
                     "%s_%s", 
                     predictor.predictors[i].getClass().getSimpleName(),
                     "score");
             scoreIndices[i] = original.numAttributes();
             original.insertAttributeAt(
                     new Attribute(attrName), 
                     original.numAttributes());
         }
         
         //add prediction score into original data set
         original.insertAttributeAt(
                 new Attribute(predictionScoreAttr), 
                 original.numAttributes());
         //add prediction result into original data set
         original.insertAttributeAt(
                 new Attribute(
                 predictionResultAttr,
                 DatasetGenerator.getClassLabels()),
                 original.numAttributes());
         original.setClassIndex(original.numAttributes() - 1);
         for (int i = 0; i < result.length; i++) {
             original.instance(i).setValue(original.classIndex() - 1, scores[i]);
             original.instance(i).setClassValue(result[i]);
             
             for (int j = 0; j < lowlevelScores.size(); j++) {
                 original.instance(i).setValue(
                         scoreIndices[j], 
                         lowlevelScores.get(j)[i]);
             }
         }
         ArffSaver saver = new ArffSaver();
         saver.setFile(new File(ProgramSettings.RESULT_DIR, outputArff));
         saver.setInstances(original);
         saver.writeBatch();
     }
 
     private static void train(RunOptions option) throws IOException, Exception {
         String inputArff = option.inputArff;
         int features = option.features;
 
         System.err.println("Loading data set ...");
         Instances dataset = new Instances(new FileReader(inputArff));
 
         if (dataset.numAttributes() - 1 > features) {
             //feature selection
             System.err.println("Selecting features ...");
             dataset = DatasetPreprocessor.featureSelection(
                     dataset, dataset.numAttributes() - 1, features);
         }
         //gather list of selected attributes
         String[] attrNames = getAttributeSet(dataset);
         //write 
         writeSelectedAttributes(attrNames);
 
         //training
         System.err.println("Training model ...");
         MainPredictor predictor = new MainPredictor();
         predictor.train(dataset);
 
         System.err.println("All done! Please check out files in directory: "
                 + ProgramSettings.MODEL_DIR);
 
     }
 
     private static void validate(RunOptions option) throws IOException, Exception {
         String inputArff = option.inputArff;
         String outputText = option.outputStatistics;
 
         //load validation set
         System.err.println("Loading validation set ...");
         Instances dataset = new Instances(new FileReader(inputArff));
         dataset.deleteStringAttributes();
 
         //reduce feature space
         System.err.println("Check and reduce feature space ...");
         String[] features = loadSelectedAttributes().split(",");
         String featureIDs = getIndicesOfSelectedFeatures(dataset, features);
         dataset = DatasetPreprocessor.selectFeatures(dataset, featureIDs);
         //load models
         System.err.println("Loading models ...");
         MainPredictor predictor = new MainPredictor();
         predictor.loadModel();
 
         //validate
         System.err.println("Evaluating models ...");
         Evaluation[] evals = predictor.evaluate(dataset);
 
         //print out results
         OutputStream os = (outputText == null)
                 ? System.out
                 : new TeeOutputStream(
                 System.out,
                 new FileOutputStream(
                 new File(ProgramSettings.RESULT_DIR, outputText)));
         PrintWriter writer = new PrintWriter(os);
 
         for (int i = 0; i < evals.length; i++) {
             Evaluation eval = evals[i];
             if (i < predictor.predictors.length) {
                 writer.println("CLASSIFIER: " + predictor.predictors[i].getClass().getSimpleName());
             } else {
                 writer.println("CLASSIFIER: " + predictor.getClass().getSimpleName());
             }
             writer.println("=== Summary ===");
             writer.println(eval.toSummaryString());
             writer.println(eval.toClassDetailsString());
             writer.println(eval.toMatrixString());
         }
         writer.flush();
         System.err.println("Done!");
     }
 
     /**
      * prepare data for training, testing etc.
      * @param option 
      */
     private static void prepareData(RunOptions option) throws IOException, Exception {
         String inputArff = option.inputArff;
         String inputFasta = option.inputFasta;
         String weights = option.weights;
         int features = option.features;
 
         //load dataset
         System.err.println("Loading data set ...");
         DatasetGenerator dg = new DatasetGenerator(
                 new File(inputArff), new File(inputFasta));
         
         //introduce class labels but do not remove string attributes
         Instances dataset = dg.generateDataset(false);
 
         ArffSaver saver1 = new ArffSaver();
         saver1.setFile(new File(ProgramSettings.DATASET_DIR, "generated_raw_dataset.arff"));
         saver1.setInstances(dataset);
         saver1.writeBatch();
         
         //remove string attributes before feature selection
         dataset.deleteStringAttributes();
 
         //feature selection over full balanced data set
         System.err.println("Selecting features ...");
         Instances fullBalanced = DatasetPreprocessor.getBalancedDataset(
                 dataset, true, dataset.numAttributes() - 1);
         String selected = DatasetPreprocessor.featureIndicesStringSelection(
                 fullBalanced, fullBalanced.numAttributes() - 1, features);
         //include class attribute
         selected = selected.replaceAll(",[^,]+$", ",last");
 
         //filter original dataset
         dataset = DatasetPreprocessor.selectFeatures(dataset, selected);
 
         //split data set into subsets
         System.err.println("Splitting data set ...");
         Instances[] subsets = DatasetPreprocessor.splitDataset(
                 dataset, DatasetPreprocessor.getWeights(weights));
 
         //balance data sets
         System.err.println("Writing output files ...");
         ArffSaver saver = new ArffSaver();
         for (int i = 0; i < subsets.length; i++) {
             subsets[i] = DatasetPreprocessor.getBalancedDataset(
                     subsets[i], true, subsets[i].numAttributes() - 1);
 
             //output arff file
             File output = new File(
                     ProgramSettings.DATASET_DIR, "subset_" + (i + 1) + ".arff");
             saver.resetOptions();
             saver.setFile(output);
             saver.setInstances(subsets[i]);
             saver.writeBatch();
         }
         System.err.println("All done! Please check out files in directory: "
                 + ProgramSettings.DATASET_DIR.getPath());
     }
 
     /**
      * get set of attribute names from a data set
      * @param dataset
      * @return 
      */
     private static String[] getAttributeSet(Instances dataset) {
         String[] features = new String[dataset.numAttributes()];
         for (int i = 0; i < dataset.numAttributes(); i++) {
             Attribute attr = dataset.attribute(i);
             features[i] = attr.name();
         }
         return features;
     }
 
     /**
      * write list of selected attributes
      * @param attrNames
      * @throws IOException 
      */
     private static void writeSelectedAttributes(String[] attrNames)
             throws IOException {
         PrintWriter writer = new PrintWriter(fileFeatures);
         writer.println(StringUtils.join(attrNames, ','));
         writer.flush();
         writer.close();
     }
 
     /**
      * load list of selected features from the training set
      * @return
      * @throws IOException 
      */
     private static String loadSelectedAttributes()
             throws IOException {
         BufferedReader reader = new BufferedReader(
                 new FileReader(fileFeatures));
         String string = reader.readLine();
         reader.close();
         return string;
     }
 
     /**
      * get indices of selected features in the given data set
      * @param dataset
      * @param features
      * @return 
      */
     private static String getIndicesOfSelectedFeatures(
             Instances dataset, String[] features) {
         LinkedList<Integer> ids = new LinkedList<Integer>();
         for (int i = 0; i < features.length; i++) {
             Attribute attr = dataset.attribute(features[i]);
             if (attr != null)
                 ids.add(attr.index() + 1);
             else
                 ids.add(dataset.numAttributes());
         }
         return StringUtils.join(ids, ',');
     }
 
     private static void trainMetaPredictor(RunOptions option) 
     throws Exception {
         String inputArff = option.inputArff;
 
         System.err.println("Loading data set ...");
         Instances dataset = new Instances(new FileReader(inputArff));
 
         //training
         System.err.println("Training model ...");
         MetaPredictor predictor = new MetaPredictor();
         predictor.train(dataset);
         predictor.saveModel();
 
         System.err.println("All done! Please check out files in directory: "
                 + ProgramSettings.MODEL_DIR);
     }
 
     private static void predictMetaPredictor(RunOptions option) 
     throws Exception {
         String inputArff = option.inputArff;
         String outputArff = option.outputArff;
         String outputFasta = option.outputFasta;
 
         System.err.println("Loading data set ...");
         Instances dataset = new Instances(new FileReader(inputArff));
         Instances original = dataset;
 
         System.err.println("Check and reduce feature space ...");
         String features = loadSelectedAttributes();
         String featureIDs =
                 getIndicesOfSelectedFeatures(dataset, features.split(","));
         dataset = DatasetPreprocessor.selectFeatures(
                 dataset, featureIDs);
 
         System.err.println("Loading prediction model ...");
         MetaPredictor predictor = new MetaPredictor();
         predictor.loadModel();
 
         System.err.println("Predicting ...");
         dataset.setClassIndex(dataset.numAttributes() - 1);
         double[] result = predictor.predict(dataset);
         double[] scores = predictor.getPredictionScores();
 
         System.err.println("Writing results ...");
         // output fasta file
         if (outputFasta != null) {
             try {
                 FastaWriter fw = new FastaWriter(new File(
                         ProgramSettings.RESULT_DIR, outputFasta));
                 fw.writeDataset(original, result);
                 fw.close();
             } catch (Exception e) {
                 System.err.println("Error writing Fasta output!");
                 System.err.println(e);
                 e.printStackTrace();
             }
         }
         
         //add prediction score into original data set
         original.insertAttributeAt(
                 new Attribute(predictionScoreAttr), 
                 original.numAttributes());
         //add prediction result into original data set
         original.insertAttributeAt(
                 predictor.getResultAttribute(),
                 original.numAttributes());
         original.setClassIndex(original.numAttributes() - 1);
         for (int i = 0; i < result.length; i++) {
             original.instance(i).setValue(original.classIndex() - 1, scores[i]);
             original.instance(i).setClassValue(result[i]);
         }
         ArffSaver saver = new ArffSaver();
         saver.setFile(new File(ProgramSettings.RESULT_DIR, outputArff));
         saver.setInstances(original);
         saver.writeBatch();
     }
 }
