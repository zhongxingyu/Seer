 package dstc;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.PrintStream;
 
 import nlp.InstancesPair;
 import nlp.StringVectorWrapper;
 import nlp.WekaWrapper;
 
 import weka.core.Instances;
 
 public class SigDial2014Classifier {
 	
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
 	
 	void ClassifierwithCostMatrix(String train, String test, String cm) throws Exception{
 		WekaWrapper wekaWapper = new WekaWrapper();
 		
 		Instances dataTrain = new Instances(new BufferedReader(new FileReader(train+".arff")));
 		dataTrain.setClassIndex(dataTrain.numAttributes()-1);
 		
 		dataTrain = wekaWapper.applyCostMatrix(dataTrain, cm);
 		
 		Instances dataTest = new Instances(new BufferedReader(new FileReader(test+".arff")));
 		dataTest.setClassIndex(dataTest.numAttributes()-1);
 		
 		int n = dataTrain.numInstances();
 		System.out.println("train:" + train);
 		System.out.println("test:" + test);
 		System.out.println("Train: " + n);
 		System.out.println("Test: " + dataTest.numInstances());
 		
 		wekaWapper.TrianTest(dataTrain, dataTest, test+"_cost");
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
 	
 	void ClassifierTwoNgram(String train, String test) throws Exception{
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
 		InstancesPair p = ngramWrapper.ApplyStringVectorFilter(dataTrain, "Trans_System", dataTest);
 		dataTrain = p.a;
 		dataTest = p.b;
 		
 		p = ngramWrapper.ApplyStringVectorFilter(dataTrain, "Trans_SLU", dataTest);
 		dataTrain = p.a;
 		dataTest = p.b;
 		
 		wekaWapper.SaveInstances(dataTrain, train + "_bak.arff");
 		
 		wekaWapper.TrianTest(dataTrain, dataTest, test);
 	}
 	
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		PrintStream oldout = System.out;
 		File f = new File("log.txt");		
 		System.setOut(new PrintStream(f));
 		
 		SigDial2014Classifier classifier = new SigDial2014Classifier();
 		
 		//Act
 		/*
 		String train = "../SigDial2014/scripts/res/dstc2_train_act";
 		String dev = "../SigDial2014/scripts/res/dstc2_dev_act";
 		classifier.Classifier(train, train);
 		classifier.Classifier(train, dev);
 		*/
 		
 		//Act + Ngram
 		/*
 		String train = "../SigDial2014/scripts/res/dstc2_train_H3_actngram";
 		String dev = "../SigDial2014/scripts/res/dstc2_dev_H3_actngram";
 		
 		classifier.ClassifierNgram(train, train);
 		classifier.ClassifierNgram(train, dev);
 		*/
 		
 		//Act + Ngram + BinarySwitch
 		/*String train = "../SigDial2014/scripts/res/dstc2_train_H1_actngram_binaryswitch";
 		String dev = "../SigDial2014/scripts/res/dstc2_dev_H1_actngram_binaryswitch";
 		
 		classifier.ClassifierNgram(train, train);
 		classifier.ClassifierNgram(train, dev);*/
 		
 		//Act + Ngram + BinarySwitch, Top3
		/*String traintop1 = "../SigDial2014/scripts/res/dstc2_train_H1_actngram_binaryswitch_top1";
 		String traintop3 = "../SigDial2014/scripts/res/dstc2_train_H1_actngram_binaryswitch_top3";
 		
 		String train3 = "../SigDial2014/scripts/res/dstc2_train_H1_actngram_binaryswitch_top3_10";
 		String dev3 = "../SigDial2014/scripts/res/dstc2_dev_H1_actngram_binaryswitch_top3_10";
 		
 		String train1 = "../SigDial2014/scripts/res/dstc2_train_H1_actngram_binaryswitch_top1_10";
 		String dev1 = "../SigDial2014/scripts/res/dstc2_dev_H1_actngram_binaryswitch_top1_10";
 		
 		classifier.ClassifierNgram(traintop3, train3);
 		classifier.ClassifierNgram(traintop3, dev3);
 		
 		classifier.ClassifierNgram(traintop1, train1);
		classifier.ClassifierNgram(traintop1, dev1);*/
 		
 		//Act WithName + BinarySwitch
 		/*String train = "../SigDial2014/scripts/res/dstc2_train_H1_actWithName_binaryswitch";
 		String dev = "../SigDial2014/scripts/res/dstc2_dev_H1_actWithName_binaryswitch";
 		
 		classifier.ClassifierNgram(train, train);
 		classifier.ClassifierNgram(train, dev);*/
 		
 		//Act WithName + Out + SLU Ngram + BinarySwitch, Top3
		String train = "../SigDial2014/scripts/res/dstc2_train_H1_actngram_binaryswitchwithName";
		String dev = "../SigDial2014/scripts/res/dstc2_dev_H1_actngram_binaryswitchwithName";
 		
 		classifier.ClassifierTwoNgram(train, train);
		classifier.ClassifierTwoNgram(train, dev);
 				
 		System.setOut(oldout);
 		
 		System.out.println("Finish!");
 	}
 
 }
