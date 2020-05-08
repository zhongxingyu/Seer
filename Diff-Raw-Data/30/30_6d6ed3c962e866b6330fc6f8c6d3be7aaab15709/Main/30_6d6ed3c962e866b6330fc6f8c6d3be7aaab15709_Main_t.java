 package program;
 
 import java.io.IOException;
 import java.util.Random;
 
 import model.NBayes;
 
 import corpus.Corpus;
 import corpus.Vocabulary;
 
 public class Main {
 	
 	/** user parameters **/
 	String delimiter = "\\+";
 	int numIter = 100;
 	long seed = 1;
 	
 	String trainFile;
 	String vocabFile;
 	String testFile;
 	String outFolderPrefix;
 	boolean containsLabel = true;
 	
 	int numClass = 100; //not used if containsLabel = true;
 	
	int vocabThreshold = 20;
	boolean testChiSquare = false; //this will be automatically false in Corpus if containsLabel = false
 	
 	/*
 	String trainFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.txt";
 	String vocabFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.txt";
 	String testFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.txt";
 	*/
 	
 	//String trainFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.nolabel.txt";
 	//String vocabFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.nolabel.txt";
 	//String testFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.nolabel.txt";
 	
 	/** user parameters end **/
 	
 	public static void main(String[] args) throws IOException {
 		Main main = new Main();
 		main.trainFile = args[0];
 		main.vocabFile = args[1];
 		main.testFile = args[2];
 		main.outFolderPrefix = args[3];
 		StringBuffer sb = new StringBuffer();
 		sb.append("Train file : " + main.trainFile);
 		sb.append("\nVocab file : " + main.vocabFile);
 		sb.append("\nTest file : " + main.testFile);
 		sb.append("\noutFolderPrefix : " + main.outFolderPrefix);
 		sb.append("\nIterations : " + main.numIter);
 		sb.append("\nNumClass : " + main.numClass);
 		sb.append("\ncontains label : " + main.containsLabel);
 		System.out.println(sb.toString());
 		main.train();
 		//main.continueTrain();
 		//main.test();
 	}
 	
 	public void continueTrain() throws IOException {
 		Corpus c = new Corpus(delimiter, vocabThreshold, testChiSquare);
 		c.readVocabFromDictionary(NBayes.base + "/" + outFolderPrefix + "/dictionary.txt");
 		c.readTrain(trainFile, containsLabel);
 		c.readTest(testFile, containsLabel);
 		NBayes model;
 		if(containsLabel) {
 			model = new NBayes(c, c.labelMap.size(), c.corpusVocab.vocabSize, containsLabel, outFolderPrefix);
 			model.load();
 			model.train(numIter);
 		} else {
 			model = new NBayes(c, numClass, c.corpusVocab.vocabSize, containsLabel, outFolderPrefix);
 			model.load();
 			model.train(numIter);
 		}
 		model.save();
 		if(containsLabel) {
 			model.decodeLabeled(testFile + ".decoded");
 		}
 		else {
 			model.decode(testFile + ".decoded");
 		}
 	}
 	
 	public void train() throws IOException {
 		Corpus c = new Corpus(delimiter, vocabThreshold, testChiSquare);
 		c.readVocab(vocabFile, containsLabel);
 		c.readTrain(trainFile, containsLabel);
 		c.readTest(testFile, containsLabel); 
 		Random r = new Random(seed);
 		NBayes model;
 		if(containsLabel) {
 			model = new NBayes(c, c.labelMap.size(), c.corpusVocab.vocabSize, containsLabel, outFolderPrefix);
 			model.initializeSupervised();
 			//model.train(1);
 		} else {
 			model = new NBayes(c, numClass, c.corpusVocab.vocabSize, containsLabel, outFolderPrefix);
 			model.initializeRandom(r);
 			model.train(numIter);
 		}
 		model.save();
 		if(containsLabel) {
 			model.decodeLabeled(testFile + ".decoded");
 		}
 		else {
 			model.decode(testFile + ".decoded");
 		}
 	}
 	
 	public void test() throws IOException {
 		Corpus c = new Corpus(delimiter, vocabThreshold, testChiSquare);
 		if(containsLabel) {
 			c.readLabels(NBayes.base + "/label.txt");
 		}
 		c.readVocabFromDictionary(NBayes.base + "/dictionary.txt");
 		c.readTest(testFile, containsLabel);
 		NBayes model;
 		if(containsLabel) {
 			model = new NBayes(c, c.labelMap.size(), c.corpusVocab.vocabSize, containsLabel, outFolderPrefix);
 			model.load();
 			model.decodeLabeledVector(testFile + ".decoded");
 		} else {
 			model = new NBayes(c, numClass, c.corpusVocab.vocabSize, containsLabel, outFolderPrefix);
 			model.load();
 			model.decode(testFile + ".decoded");
 		}
 	}
 }
