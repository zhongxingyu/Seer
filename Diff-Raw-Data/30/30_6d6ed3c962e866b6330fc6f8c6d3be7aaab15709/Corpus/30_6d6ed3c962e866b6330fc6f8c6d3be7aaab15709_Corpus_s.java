 package corpus;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Corpus {
 	//public static String delimiter = "\\+";
 	public String delimiter;
 	public InstanceList trainInstanceList = new InstanceList();
 	public InstanceList testInstanceList = new InstanceList();
 	
 	//for label
 	public int labelIdCount;
 	public Map<String, Integer> labelMap;
 	public ArrayList<String> labelIdToString;
 	//label id to frequency
 	public Map<Integer, Integer> labelFrequency = new HashMap<Integer, Integer>();
 	
 	public Map<Integer, Map<Integer, Integer>> featureLabelFrequency = new HashMap<Integer, Map<Integer, Integer>>();
 	
 	public Vocabulary corpusVocab; 
 	
 	public Corpus(String delimiter, int vocabThreshold, boolean testChiSquare) {
 		this.delimiter = delimiter;
 	}
 	
 	public void readTest(String inFile, boolean containsLabel) throws IOException {
 		BufferedReader br = new BufferedReader(new FileReader(inFile));
 		String line = null;
 		int totalWords = 0;
 		testInstanceList = new InstanceList();
 		while( (line = br.readLine()) != null ) {
 			line = line.trim();
 			if(! line.isEmpty()) {
 				Instance instance = new Instance(this, line, containsLabel);
 				testInstanceList.add(instance);
 				totalWords += instance.words.length;
 			}
 		}
 		System.out.println("Test Instances: " + testInstanceList.size());
 		System.out.println("Test token count: " + totalWords);
 		br.close();
 	}
 	
 	public void readTrain(String inFile, boolean containsLabel) throws IOException {
 		//Vocabulary trainVocab = new Vocabulary();
 		//trainVocab.readVocabFromFile(this, inFile, containsLabel);
 		BufferedReader br = new BufferedReader(new FileReader(inFile));
 		String line = null;
 		int totalWords = 0;
 		while( (line = br.readLine()) != null ) {
 			line = line.trim();
 			if(! line.isEmpty()) {
 				Instance instance = new Instance(this, line, containsLabel);
 				trainInstanceList.add(instance);
 				totalWords += instance.words.length;
 			}
 		}
 		//System.out.println("Train vocab size : " + trainVocab.vocabSize);
 		System.out.println("Train Instances: " + trainInstanceList.size());
 		System.out.println("Train token count: " + totalWords);
 		br.close();
 	}
 	
 	public void readVocab(String inFile, boolean containsLabel) throws IOException {
 		corpusVocab = new Vocabulary();
 		if(! containsLabel ) {
 			corpusVocab.testChiSquare = false;
 		}
 		if(containsLabel) {
 			labelIdCount = 0;
 			labelMap = new HashMap<String, Integer>();
 			labelIdToString = new ArrayList<String>();
 		}
 		corpusVocab.readVocabFromFile(this, inFile, containsLabel);
 		/*
 		BufferedReader br = new BufferedReader(new FileReader(inFile));
 		String line = null;
 		int totalWords = 0;
 		while( (line = br.readLine()) != null ) {
 			line = line.trim();
 			if(! line.isEmpty()) {
 				Instance instance = new Instance(this, line, containsLabel);
 				decodeInstanceList.add(instance);
 				totalWords += instance.words.length;
 			}
 		}
 		System.out.println("Vocab Instances: " + decodeInstanceList.size());
 		System.out.println("Vocab token count: " + totalWords);
 		br.close();
 		*/
 	}
 	
 	public void readVocabFromDictionary(String filename) {
 		corpusVocab = new Vocabulary();
 		corpusVocab.readVocabFromDictionary(filename);
 	}
 	
 	public void readLabels(String filename) throws IOException {
 		System.out.println("\treading labels...");
 		BufferedReader brLabel = new BufferedReader(new FileReader(filename));
 		String	line = brLabel.readLine().trim();
 		this.labelIdToString = new ArrayList<String>();
 		this.labelMap = new HashMap<String, Integer>();
 		while( (line = brLabel.readLine() ) != null) {
 			line = line.trim();
 			this.labelMap.put(line, this.labelIdToString.size());
 			this.labelIdToString.add(line);
 			
 		}
 		brLabel.close();
 	}
 	
 	public int getLabelMap(String label) {
 		if(labelMap.containsKey(label)) {
 			int labelId = labelMap.get(label); 
 			labelFrequency.put(labelId, labelFrequency.get(labelId) + 1);
 			return labelId;
 		} else {
 			labelMap.put(label, labelIdCount);
 			labelFrequency.put(labelIdCount, 1);
 			labelIdToString.add(label);
 			return labelIdCount++;
 		}
 	}
 	
 	public void debug() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("DEBUG: Corpus\n");
 		sb.append("=============\n");
 		sb.append("vocab size : " + corpusVocab.vocabSize);
 		sb.append("\nlabel size : " + labelMap.size());
 		sb.append("\nvocab frequency: \n");
 		for(int i=0; i<corpusVocab.vocabSize; i++) {
 			sb.append("\t" + corpusVocab.indexToWord.get(i) + " --> " + corpusVocab.indexToFrequency.get(i));
 			sb.append("\n");
 		}
 		sb.append("\nlabel frequency: \n");
 		for(int i=0; i<labelMap.size(); i++) {
 			sb.append("\t" + labelIdToString.get(i) + " --> " + labelFrequency.get(i));
 			sb.append("\n");
 		}
 		sb.append("\nvocab_label frequency: \n");
 		for(int i=1; i<corpusVocab.vocabSize; i++) {
 			for(int j=0; j<labelMap.size(); j++) {
 				sb.append("\t +" + corpusVocab.indexToWord.get(i) + "_" + labelIdToString.get(j) + " --> " +
 						featureLabelFrequency.get(i).get(j));
 				sb.append("\n");
 			}
 		}
 		System.out.println(sb.toString());
 	}
 	
 	public static void main(String[] args) throws IOException {
 		String inFile = "/home/anjan/workspace/naivebayes/data/weather.nominal.txt";
 		int vocabThreshold = 0;
 		boolean testChisquare = false;
 		Corpus c = new Corpus("\\,", vocabThreshold, testChisquare);
 		c.readVocab(inFile, true);
 	}
 }
