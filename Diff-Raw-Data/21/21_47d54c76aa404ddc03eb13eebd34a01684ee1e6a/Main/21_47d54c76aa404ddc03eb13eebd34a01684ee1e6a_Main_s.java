 package classification;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import util.DatasetHelper;
 import util.FeatureSelection;
 import util.Utility;
 import weka.core.Instances;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.StringToWordVector;
 
 public class Main {
 
 	public static final List<String> labelsUsed = new ArrayList<String>(
 			Arrays.asList("earn", "acq", "money-fx", "grain", "crude", "trade", "interest", "wheat",
 					"ship", "corn", "money-supply", "dlr", "sugar", "oilseed", "coffee", "gnp", "gold",
 					"veg-oil", "soybean", "livestock"));
 
 	public static void main(String[] args) throws Exception {
		int fsMethod = Integer.parseInt(args[0]);
		String clsMethod = args[1];
		int wordsToKeep = Integer.parseInt(args[2]);
		int numToSelect = Integer.parseInt(args[3]);
		Utility.filename = args[4];
 		Utility.ensureFileExists();
 		
 		System.out.println("fsMethod " + fsMethod 
 				+ "\nclsMethod " 
 				+ clsMethod + "\nwordsToKeep " 
 				+ wordsToKeep + "\nnumToSelect " 
				+ numToSelect + "\noutfile " + args[4]);
 		
 		long startTime = System.currentTimeMillis();
 		
 		// Configure StringToWordVector using all words from the training set
 		DatasetHelper.wordsToKeep = wordsToKeep;
 		StringToWordVector filter = DatasetHelper.createWordVectorFilter(
 				DatasetHelper.loadData("alltrain_noclass.arff"));
 
 		// Load all training files. The instances in these files are not labelled.
 		Map<String, Instances> trainDatasets = DatasetHelper.loadAllDatasets("train/", filter);
 
 		// Add label/class column to training sets along with a label value for each instance.
 		DatasetHelper.labelAllDatasets("train/", trainDatasets);
 		
 		// Perform feature selection
		if(fsMethod == 0){
 			FeatureSelection fs = new FeatureSelection(fsMethod, numToSelect);
 			fs.selectAttributes();
 
 			// Keep only selected features in the training datasets.
 			Map<String, Instances> fstrainDatasets = new HashMap<String, Instances>();
 			for(Map.Entry<String, Instances> e : trainDatasets.entrySet()){
 				fstrainDatasets.put(e.getKey(), fs.filterOutAttributes(e.getValue()));
 			}
 			trainDatasets = fstrainDatasets;
 		}
 
 		// Load test dataset. So far, it's unlabelled.
 		Instances unlabeledTest = Filter.useFilter(DatasetHelper.loadData("test_noclass.arff"), filter);
 
 		// Load true labels of the test dataset from a file and transform them into a map representation
 		// where each entry is (label name, binary list of label values of each test instance).
 		List<String> testLabelsFromFile = DatasetHelper.loadLabelFile("test_noclass_rest.arff");
 		Map<String, List<Double>> testLabels = DatasetHelper.formatTestLabels(labelsUsed, testLabelsFromFile);
 
 		MyClassifier myClassifier = new MyClassifier(clsMethod);
 		Map<String, List<Double>> predictedLabels = myClassifier.classify(trainDatasets, unlabeledTest, testLabels);
 		
 		double[] PR = Utility.calcPrecisionRecall(testLabels, predictedLabels);
 		System.out.println("Precision = " + PR[0] + "\nRecall = " + PR[1]);
 		Utility.outputToFile("Precision = " + PR[0] + "\nRecall = " + PR[1]);
 
 		//		LibSVM svm = new LibSVM();
 		//		SelectedTag kt = new SelectedTag(0, LibSVM.TAGS_KERNELTYPE);
 		//		SelectedTag svmt = new SelectedTag(0, LibSVM.TAGS_SVMTYPE);
 		//		svm.setKernelType(kt);
 		//		svm.setSVMType(svmt);
 		//		svm.setProbabilityEstimates(true);		
 
 		long endTime   = System.currentTimeMillis();
 		long totalTime = endTime - startTime;
 		System.out.println("Took : " + (totalTime / 1000) + "s");
 	}
 
 }
