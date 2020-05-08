 package dstc;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintStream;
 
 import nlp.InstancesPair;
 import nlp.StringVectorWrapper;
 import nlp.WekaWrapper;
 
 import weka.classifiers.Classifier;
 import weka.classifiers.Evaluation;
 import weka.classifiers.functions.SMO;
 import weka.core.Attribute;
 import weka.core.Instance;
 import weka.core.Instances;
 
 public class SigDial2014GoalClassifier {
 	
 	void Classifier(String train, String test) throws Exception{
 		WekaWrapper wekaWapper = new WekaWrapper();
 		
 		Instances dataTrain = new Instances(new BufferedReader(new FileReader(train+".arff")));
 		dataTrain.setClassIndex(dataTrain.numAttributes()-1);
 		
 		Instances dataTest = new Instances(new BufferedReader(new FileReader(test+".arff")));
 		dataTest.setClassIndex(dataTest.numAttributes()-1);
 		
 		int n = dataTrain.numInstances();
 		System.out.println("train:" + train);
 		System.out.println("test:" + test);
 		System.out.println("Train: " + n);
 		System.out.println("Test: " + dataTest.numInstances());
 		
 		wekaWapper.TrianTest(dataTrain, dataTest, test);
 	}
 	
 	void ClassifierNgram(String train, String test) throws Exception{
 		WekaWrapper wekaWapper = new WekaWrapper();
 		
 		Instances dataTrain = new Instances(new BufferedReader(new FileReader(train+".arff")));
 		dataTrain.setClassIndex(dataTrain.numAttributes()-1);
 		
 		Instances dataTest = new Instances(new BufferedReader(new FileReader(test+".arff")));
 		dataTest.setClassIndex(dataTest.numAttributes()-1);
 		
 		int n = dataTrain.numInstances();
 		System.out.println("train:" + train);
 		System.out.println("test:" + test);
 		System.out.println("Train: " + n);
 		System.out.println("Test: " + dataTest.numInstances());
 		
 		StringVectorWrapper ngramWrapper = new StringVectorWrapper();
 		InstancesPair p = ngramWrapper.ApplyStringVectorFilter(dataTrain, "ASR", dataTest);
 		dataTrain = p.a;
 		dataTest = p.b;
 		
 		wekaWapper.SaveInstances(dataTrain, train + "_bak.arff");
 		
 		wekaWapper.TrianTest(dataTrain, dataTest, test);
 	}
 	
 	void ClassifierNgramOnline(String train, String test) throws Exception{
 		WekaWrapper wekaWapper = new WekaWrapper();
 		
 		Instances dataTrain = new Instances(new BufferedReader(new FileReader(train+".arff")));
 		dataTrain.setClassIndex(dataTrain.numAttributes()-1);
 		
 		Instances dataTest = new Instances(new BufferedReader(new FileReader(test+".arff")));
 		dataTest.setClassIndex(dataTest.numAttributes()-1);
 		
 		int n = dataTrain.numInstances();
 		System.out.println("train:" + train);
 		System.out.println("test:" + test);
 		System.out.println("Train: " + n);
 		System.out.println("Test: " + dataTest.numInstances());
 		
 		StringVectorWrapper ngramWrapper = new StringVectorWrapper();
 		InstancesPair p = ngramWrapper.ApplyStringVectorFilter(dataTrain, "ASR", dataTest);
 		dataTrain = p.a;
 		dataTest = p.b;
 		
 		wekaWapper.SaveInstances(dataTrain, train + "_bak.arff");
 		
 		//wekaWapper.TrianTest(dataTrain, dataTest, test);
 		//Build the model
 		Classifier classifier = new SMO();
 		classifier.buildClassifier(dataTrain);
 		//Instances labeled = new Instances(dataTest);
 		
 		Attribute attPtag = dataTest.attribute("PreviousTag");
 		Attribute atrFirstTurn = dataTest.attribute("firstTurn");
 
 		//Test it one by one
 		for(int j=0; j< dataTest.numInstances();j++)
 		{
 			Instance instance = dataTest.instance(j);
 			
 			String firstTurn = instance.stringValue(atrFirstTurn);
 			if(firstTurn.equals("True")){
 				instance.setValue(attPtag, "none");
 			}
 			
 			double clslable = classifier.classifyInstance(instance);
 			String label = dataTest.classAttribute().value((int)clslable);
 			System.out.println(label);
 			
 			if (j == dataTest.numInstances() - 1) continue;
 			
 			dataTest.instance(j+1).setValue(attPtag, label);	
 		}
 	
 		wekaWapper.TrianTest(dataTrain, dataTest, test);
 	}
 	
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		PrintStream oldout = System.out;
		File f = new File("res_goals_enrich_more.txt");		
 		System.setOut(new PrintStream(f));
 		
 		SigDial2014GoalClassifier classifier = new SigDial2014GoalClassifier();
 
 		String[] Goals = {"area", "food", "name", "pricerange"};
 		
 		//Act + Ngram, MindChange
 		for(String goal: Goals){
 			//String train = "../SigDial2014/scripts/res/dstc2_train_goals_actWithNamengram_L" + goal;
 			//String dev = "../SigDial2014/scripts/res/dstc2_dev_goals_actWithNamengram_L" + goal;
 			
 			//String train = "../SigDial2014/scripts/res/dstc2_train_goals_enrich_L" + goal;
 			//String dev = "../SigDial2014/scripts/res/dstc2_dev_goals_enrich_L" + goal;
 			
			//String train = "../SigDial2014/scripts/res/dstc2_train_goals_enrich_trans_L" + goal;
			//String dev = "../SigDial2014/scripts/res/dstc2_dev_goals_enrich_trans_L" + goal;
			
			String train = "../SigDial2014/scripts/res/dstc2_train_goals_enrich_more_L" + goal;
			String dev = "../SigDial2014/scripts/res/dstc2_dev_goals_enrich_more_L" + goal;
 			
 			classifier.ClassifierNgram(train, train);
 			classifier.ClassifierNgram(train, dev);
 		}
 		
 		System.setOut(oldout);
 		
 		System.out.println("Finish!");
 	}
 
 }
