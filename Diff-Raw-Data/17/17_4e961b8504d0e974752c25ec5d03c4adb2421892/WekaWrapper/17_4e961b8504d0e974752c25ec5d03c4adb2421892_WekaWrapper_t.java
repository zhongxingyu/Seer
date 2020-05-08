 package nlp;
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Random;
 
 import weka.attributeSelection.CfsSubsetEval;
 import weka.attributeSelection.GreedyStepwise;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 import weka.core.Range;
 import weka.core.SelectedTag;
 import weka.core.converters.ArffSaver;
 import weka.classifiers.trees.J48; //Decision Tree
 import weka.classifiers.bayes.NaiveBayes;
 import weka.classifiers.functions.SMO;
 import weka.classifiers.meta.Bagging;
 import weka.classifiers.meta.AdaBoostM1;
 import weka.classifiers.meta.FilteredClassifier;
 import weka.classifiers.lazy.IBk;
 
 import weka.classifiers.functions.LibLINEAR;
 import weka.classifiers.functions.Logistic;
 
 import weka.classifiers.Classifier;
 import weka.classifiers.Evaluation;
 import weka.classifiers.CostMatrix;
 import weka.filters.Filter;
 import weka.filters.supervised.attribute.AttributeSelection;
 import weka.filters.unsupervised.attribute.AddID;
 import weka.filters.unsupervised.attribute.PrincipalComponents;
 import weka.filters.unsupervised.attribute.Remove;
 
 import weka.classifiers.meta.Vote;
 
 import java.util.regex.*;
 
 public class WekaWrapper {
 	
 	public static Instances AddID(Instances data) throws Exception{
 		AddID addid = new AddID();
 		addid.setInputFormat(data);
 		data = Filter.useFilter(data, addid);
 		
 		return data;
 	}
 	
 	public static Instances applyCostMatrix(Instances dataset, String costmatrix){
 		CostMatrix matrix;
 		Instances newData = null;
 		try {
 			matrix = new CostMatrix(new BufferedReader(new FileReader(costmatrix)));
 			newData = matrix.applyCostMatrix(dataset, new Random(1));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return newData;
 	}
 	
 	public static void SaveInstances(Instances dataset, String file) throws IOException{
 		 BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 		 writer.write(dataset.toString());
 		 writer.flush();
 		 writer.close();
 		 
 		 //ArffSaver saver = new ArffSaver();
 		 //saver.setInstances(dataset);
 		 //saver.setFile(new File(file));
 		 //saver.writeBatch();
 	}
 		 
 	public static void PrintResult(Evaluation eval) throws Exception{
 		System.out.println(eval.toClassDetailsString());
 		System.out.println(eval.toMatrixString());
 	}
 	
 	public static Instances LoadData(String data) throws Exception{
 	
 		BufferedReader reader = new BufferedReader(new FileReader(data));
 		Instances dataset = new Instances(reader);
 		reader.close();
 		
 		//setting class attribute
 		dataset.setClassIndex(dataset.numAttributes() - 1);
 		
 		return dataset;
 	}
 	
 	public static Vote newVote(){
 		Classifier[] classifiers = {
 				new NaiveBayes(),
 				new J48(),
 				new SMO(),
 				};
 	
 		Vote voting = new Vote();
 		voting.setClassifiers(classifiers);
 		//voting.setSeed(0);
 		voting.setCombinationRule(new SelectedTag(Vote.MAJORITY_VOTING_RULE, Vote.TAGS_RULES));//Majority Voting
 
 		return voting;
 	}
 	
 	public static void GetPridictionDistribution(Classifier classifier, Instances testset, String output) throws Exception{
 		
 		//output the labels
 		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
 		
 		writer.write("True"+"\t");
 		for(int i=0; i< testset.classAttribute().numValues();i++){
 			String label = testset.classAttribute().value(i);
 			writer.write(label + "\t");
 		}
 		writer.newLine();
 		
 		Instances labeled = new Instances(testset);
 		
 		for(int j=0; j< testset.numInstances(); j++)
 		{
 			Instance instance = testset.instance(j);
 			
 			String label = labeled.classAttribute().value((int)instance.classValue());
 			writer.write(label + "\t");
 			
 			double clslable = classifier.classifyInstance(instance);
 			double p[] = classifier.distributionForInstance(instance);
 			
 			for(int i=0;i<p.length;i++){
 				//writer.write(Double.toString(p[i]) + "\t");
 				writer.write(String.format("%.3f", p[i]) + "\t");
 			}
 			
 			writer.newLine();
 		}
 		
 		writer.flush();
 		writer.close();
 	}
 
 	public static void GetPridiction(Classifier classifier, Instances testset, String output) throws Exception{
 		//output the labels
 		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
 		writer.write("True" + "\t" + "Predict");
 		writer.newLine();
 		
 		Instances labeled = new Instances(testset);
 		
 		for(int j=0; j< testset.numInstances();j++)
 		{
 			Instance instance = testset.instance(j);
 			
 			String label = labeled.classAttribute().value((int)instance.classValue());
 			writer.write(label + "\t");
 			
 			double clslable = classifier.classifyInstance(instance);
 			
 			//instance.setClassValue(clslable);
 			label = labeled.classAttribute().value((int)clslable);
 			
 			writer.write(label);
 			writer.newLine();
 		}
 		
 		writer.flush();
 		writer.close();
 	}
 	
 	public static void TrianTest(Instances trainset, Instances testset, String test) throws Exception{
 		Classifier[] classifiers = {
 		//new Bagging(),
 		//new AdaBoostM1(),
 		//new IBk(),
 		//new IBk(3),
 		//new IBk(5),
		new NaiveBayes(),
 		//new J48(),
 		//new SMO(),
		//newVote(),
 		};
 		
 		for(int i = 0; i < classifiers.length; i++)
 		{
 			Classifier classifier = classifiers[i];
 			classifier.buildClassifier(trainset);
 			//System.out.println(classifier);
 		
 			System.out.println("Classifier is done!");
 			
 			Evaluation eval = new Evaluation(trainset);
 		
 			eval.evaluateModel(classifier, testset);
 		
 			PrintResult(eval);
 			
			//GetPridiction(classifier, testset, test + ".label");
			GetPridictionDistribution(classifier, testset, test + ".dis.label");
 		}
 	}
 	
 	public static void TrianTest(Instances trainset, Instances testset) throws Exception{
 		TrianTest(trainset, testset, "test.label.arff");
 	}
 	
 	public static void TrianTest(String train, String test) throws Exception{
 		System.out.print(train + "\t");
 		Instances trainset = LoadData(train);
 		System.out.println(trainset.numAttributes());
 		
 		System.out.print(test + "\t");
 		Instances testset = LoadData(test);
 		System.out.println(trainset.numInstances());
 		
 		TrianTest(trainset, testset, test);
 	}
 	
 	public static Instances FeatureSelection(Instances data) throws Exception{
 		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
 		CfsSubsetEval eval = new CfsSubsetEval();
 		GreedyStepwise search = new GreedyStepwise();
 		
 		search.setSearchBackwards(true);
 		filter.setEvaluator(eval);
 		filter.setSearch(search);
 		filter.setInputFormat(data);
 		
 		// generate new data
 		Instances newData = Filter.useFilter(data, filter);
 		
 		return newData;
 	}
 	
 	public static Instances PerformPCA(Instances dataset, int k) throws Exception{
 		PrincipalComponents pca = new PrincipalComponents();
 		pca.setInputFormat(dataset);
         pca.setMaximumAttributes(k);
         Instances newData = Filter.useFilter(dataset, pca);
 		return newData;
 	}
 	
 	public static Instances AddWeight(Instances dataset, String weightfile) throws Exception{
 		Instances weights = LoadData(weightfile);
 		Attribute wid = weights.attribute("Weight");
 		
 		for(int i=0;i<weights.numInstances();i++){
 			Instance ins_w = weights.instance(i);
 			double weight = ins_w.value(wid);
 			dataset.instance(i).setWeight(weight);
 		}
 
 		return dataset;
 	}
 	
 	public static void Crossvalidataion(Instances dataset, int folder) throws Exception
 	{
 		Boolean OutputPredictionFlag = false;
 		
 		System.out.println(dataset.numInstances());
 		
 		//LibLINEAR liblinear = new LibLINEAR(); 
 		//liblinear.setOptions(weka.core.Utils.splitOptions("-S 1 -C 1.0 -E 0.01 -B 1.0"));
 		//SMO smo = new SMO();
 		//smo.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.01\""));
 		
 		//WLSVM libsvm = new WLSVM();
 		//libsvm.setOptions(weka.core.Utils.splitOptions("-S 1 -C 1.0 -E 0.01 -B 1.0"));
 		
 		FilteredClassifier fc = new FilteredClassifier();
 		fc.setClassifier(new SMO());
 		
 		Attribute id = dataset.attribute("ID");
 		if(id != null){
 			int attributes[] = new int[1];
 			attributes[0] = id.index();
 			
 			Remove filter = new Remove();
 			filter.setAttributeIndicesArray(attributes);
 			fc.setFilter(filter);
 		}
 
 		Classifier[] classifiers = { 
 				//new NaiveBayes(),
 				//new J48(),
 				new SMO(),
 				//fc,
 				//libsvm,
 				//smo,
 				//new IBk(3),
 				//new Logistic(),
 				//liblinear,
 				//new AdaBoostM1(),
 				//new Bagging(),
 				};
 		
 		for(int i = 0; i < classifiers.length; i++)
 		{
 			Classifier classifier = classifiers[i];
 			classifier.buildClassifier(dataset);
 			Evaluation eval = new Evaluation(dataset);
 		
 			if(OutputPredictionFlag){
 				StringBuffer forPredictionsPrinting = new StringBuffer();
 			    
 			    //the prediction format is: StringBuffer, Attribute Range, setOutputDistribution
 			    eval.crossValidateModel(classifier, dataset, folder, new Random(1), forPredictionsPrinting, new Range("first"), false);
 			    PrintResult(eval);
 			    
 			    ArrayList< ArrayList<String> > predictions = new ArrayList< ArrayList<String> >();
 			    
 			    String[] lines = forPredictionsPrinting.toString().split("\n");
 				System.out.println(lines[0]);
 				Pattern pattern = Pattern.compile("\\d+\\s+\\d+:(\\w+)\\s+\\d+:(\\w+).*\\((\\d+)\\)");
 				for(String line: lines){
 					Matcher m = pattern.matcher(line);
 					if(m.find()){
 						ArrayList<String> tmp = new ArrayList<String>();
 						tmp.add(m.group(1));
 						tmp.add(m.group(2));
 						tmp.add(m.group(3));
 						predictions.add(tmp);
 						//System.out.println(m.group(1) + "\t" + m.group(2) + "\t" + m.group(3));
 					}
 				}
 				
 				//sort according to ID
 				Collections.sort(predictions, new Comparator< ArrayList<String> >(){
 						public int compare(ArrayList<String> ins1, ArrayList<String> ins2) {
 							int id1 = Integer.parseInt(ins1.get(2));
 							int id2 = Integer.parseInt(ins2.get(2));
 					        return id1 - id2;
 					    }
 					}
 				);
 				
 				for(ArrayList<String> predict: predictions){
 					for(String entry: predict){
 						System.out.print(entry + "\t");
 					}
 					System.out.println();
 				}
 				
 			}else{
 				eval.crossValidateModel(classifier, dataset, folder, new Random(1));
 				PrintResult(eval);
 			}
 
 			//System.out.println(classifierOutput.getAttributes());
 			//System.out.println(classifierOutput.getDisplay());
 		}
 	}
 	
 	public static void Crossvalidataion(String data, int folder) throws Exception
 	{
 		Instances dataset = LoadData(data);
 		System.out.print(data + "\t");
 		Crossvalidataion(dataset, folder);
 	}
 	
 	public static void main(String[] args) {
 		
 		String logfile = "log.txt";
 		try{
 		
 			PrintStream out = new PrintStream(new FileOutputStream(logfile));
 			PrintStream orgStream = System.out;
 			
 			System.setOut(out);
 
 			System.setOut(orgStream);
 			
 			System.out.println("Finsh!");
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.toString());
 		}
 	}
 }
