import java.io.File;
import java.io.FileWriter;
 import java.util.Random;
 
 import weka.classifiers.Evaluation;
 import weka.classifiers.bayes.*;
 import weka.core.Instances;
 import weka.core.converters.ConverterUtils.DataSource;
 
 public class NaiveBayesSample {
 	
 	public static void main (String[] args) throws Exception {
 		DataSource source = new DataSource("/home/tsai0606/Prospect/Prospect_Data/digit/mnist1000.pixel.arff");
		FileWriter output = new FileWriter(new File("NaiveBayesresult"));
 		Instances data = source.getDataSet();
 		if (data.classIndex() == -1)
 			data.setClassIndex(data.numAttributes() - 1);
 			
 		 String[] options = new String[1];
 		 options[0] = "-K";            
 		 NaiveBayes tree = new NaiveBayes();         
 		 tree.setOptions(options);     
 		 tree.buildClassifier(data);
 		 
 		 
 		 Evaluation eval = new Evaluation(data);
 		 eval.crossValidateModel(tree, data, 10, new Random(1));
 		 
 		 output.write(eval.toSummaryString());
		 System.out.println(eval.toSummaryString());
 		 System.out.println("done");
 	}
 }
