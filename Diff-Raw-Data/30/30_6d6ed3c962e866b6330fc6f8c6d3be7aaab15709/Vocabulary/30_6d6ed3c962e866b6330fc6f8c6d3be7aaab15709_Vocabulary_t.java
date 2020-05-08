 package corpus;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class Vocabulary {
 	boolean debug = false;
	public int featureThreshold = 0;
 	public boolean testChiSquare = true;
 	//index zero reserved for __OOV__ (low freq features)
 	private int index = 1;
 	public int vocabSize = -1;
 	public String UNKNOWN = "_UNKNOWN_";
 	public Map<String, Integer> wordToIndex = new HashMap<String, Integer>();
 	public ArrayList<String> indexToWord = new ArrayList<String>();
 	public Map<Integer, Integer> indexToFrequency = new HashMap<Integer, Integer>();
 	
	public int CHISQUARE_CRITICAL_INDEX = 0; //0 = 90%, 1 = 95%
 	public double CHI_SQUARE_TABLE[][] = {
 			//p-values
 			//0.90      0.95       0.975      0.99      0.999
 			{0,			0,			0,			0,			0},  //for proper indexing
 			{2.706,     3.841,     5.024,     6.635,    10.828}, //dof=1
 			{4.605,     5.991,     7.378,     9.210,    13.816}, //dof=2
 			{6.251,     7.815,     9.348,    11.345,    16.266},
 			{7.779,     9.488,    11.143,    13.277,    18.467},
 			{9.236,    11.070,    12.833,    15.086,    20.515}, //dof=5
 	};
 
 	private int addItem(String word) {
 		int returnId = -1;
 		if(wordToIndex.containsKey(word)) {
 			int wordIndex = wordToIndex.get(word);
 			int oldFreq = indexToFrequency.get(wordIndex);
 			indexToFrequency.put(wordIndex, oldFreq + 1);
 			returnId = wordIndex;
 		} else {
 			wordToIndex.put(word, index);
 			indexToWord.add(word);
 			indexToFrequency.put(index, 1);
 			returnId = index;
 			index++;
 		}
 		return returnId;
 	}
 	
 	public boolean passesChiSquareTest(int wordId, Corpus c) {
 		if(! testChiSquare) {
 			return true;
 		} else {
 			
 			int labelCount = c.labelIdToString.size();
 			/*			label1								label2				label3
 			 * x = 0    label1count - x1_label1_count		...					...
 			 * x = 1	x1_label1_count						x1_label2_count		...	
 			 * 
 			 */
 			double[][] observed = new double[2][labelCount];
 			double[][] expected = new double[2][labelCount];
 			double[] sum = new double[2]; //sum over all labels for feature = 0 and feature = 1
 			//fill observed table
 			for(int j=0; j<labelCount; j++) {
 				Integer featureLabelcount = c.featureLabelFrequency.get(wordId).get(j);
 				observed[1][j] = (featureLabelcount == null) ? 0 : featureLabelcount;
 				observed[0][j] = c.labelFrequency.get(j) - observed[1][j];
 				sum[0] += observed[0][j];
 				sum[1] += observed[1][j];
 			}
 			double total = sum[0] + sum[1];
 			//fill expected
 			for(int j=0; j<labelCount; j++) {
 				expected[0][j] = sum[0] * c.labelFrequency.get(j) / total;
 				expected[1][j] = sum[1] * c.labelFrequency.get(j) / total;
 			}
 			if(debug) {
 				System.out.println("Feature : " + indexToWord.get(wordId));
 				System.out.println("Observed Table: ");
 				for(int j=0; j<labelCount; j++) {
 					System.out.print(c.labelIdToString.get(j) + "\t");
 				}
 				System.out.println();
 				for(int i=0; i<2; i++) {
 					for(int j=0; j<labelCount; j++) {
 						
 						System.out.print(observed[i][j] + "\t");
 					}
 					System.out.println();
 				}
 				System.out.println();
 				System.out.println("Expected Table: ");
 				for(int i=0; i<2; i++) {
 					for(int j=0; j<labelCount; j++) {
 						System.out.print(expected[i][j] + "\t");
 					}
 					System.out.println();
 				}
 				System.out.println();
 			}
 			double chiSquare = 0;
 			for(int i=0; i<2; i++) {
 				for(int j=0; j<labelCount; j++) {
 					chiSquare += Math.pow(expected[i][j] - observed[i][j], 2) / expected[i][j];
 				}
 			}
 			if(debug) {
 				System.out.println("Chisquare value = " + chiSquare);
 			}
 			if(chiSquare > CHI_SQUARE_TABLE[labelCount - 1][CHISQUARE_CRITICAL_INDEX]) {
 			//if(chiSquare > 0.05) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	}
 	
 	private void reduceVocab(Corpus c) {
 		System.out.println("Reducing vocab");
 		Map<String, Integer> wordToIndexNew = new HashMap<String, Integer>();
 		ArrayList<String> indexToWordNew = new ArrayList<String>();
 		Map<Integer, Integer> indexToFrequencyNew = new HashMap<Integer, Integer>();
 		wordToIndexNew.put(UNKNOWN, 0);
 		indexToFrequencyNew.put(0, -1); //TODO: decide if this matters
 		indexToWordNew.add(UNKNOWN);
 		int featureIndex = 1;
 		for(int i=1; i<indexToWord.size(); i++) {
 			if(indexToFrequency.get(i) > featureThreshold) {
 				if( passesChiSquareTest(i, c)) {
 					wordToIndexNew.put(indexToWord.get(i), featureIndex);
 					indexToWordNew.add(indexToWord.get(i));
 					indexToFrequencyNew.put(featureIndex, indexToFrequency.get(i));
 					featureIndex = featureIndex + 1;
 				}
 			}
 		}
 		indexToWord = null; indexToFrequency = null; wordToIndex = null;
 		indexToWord = indexToWordNew;
 		indexToFrequency = indexToFrequencyNew;
 		wordToIndex = wordToIndexNew;
 		vocabSize = wordToIndex.size();
 		System.out.println("New vocab size : " + vocabSize);
 		
 	}
 	
 	public void readVocabFromFile(Corpus c, String filename, boolean containsLabel) throws IOException {
 		BufferedReader br = new BufferedReader(new FileReader(filename));
 		String line = null;
 		wordToIndex.put(UNKNOWN, 0);
 		indexToFrequency.put(0, -1); //TODO: decide if this matters
 		indexToWord.add(UNKNOWN);
 		while( (line = br.readLine()) != null) {
 			line = line.trim();
 			if(! line.isEmpty()) {
 				String words[] = line.split(c.delimiter);
 				int label = -1;
 				if(containsLabel) {
 					label = c.getLabelMap(words[words.length-1]); //also counts the labels
 				}
 				for(int i=0; i<words.length; i++) {
 					if(i == words.length-1) {
 						if(containsLabel) {							
 							continue;
 						}
 					}
 					String word = words[i];
 					int wordId = addItem(word);
 					if(containsLabel) {
 						//update feature_label count
 						if(! c.featureLabelFrequency.containsKey(wordId)) {
 							HashMap<Integer, Integer> labelToFrequency= new HashMap<Integer, Integer>();
 							c.featureLabelFrequency.put(wordId, labelToFrequency);
 						}
 						if(c.featureLabelFrequency.get(wordId).containsKey(label)) {
 							int oldFreq = c.featureLabelFrequency.get(wordId).get(label);
 							c.featureLabelFrequency.get(wordId).put(label, oldFreq + 1);
 						} else {
 							c.featureLabelFrequency.get(wordId).put(label, 1);
 						}
 					}
 				}
 			}
 		}
 		vocabSize = wordToIndex.size();
 		System.out.println("Original Vocab Size: " + vocabSize);
 		if(debug) {
 			System.out.println("Before reducing vocab : ");
 			c.debug();
 		}
 		reduceVocab(c);
 		br.close();
 		
 	}
 	
 	//reads from the dictionary
 	public void readVocabFromDictionary(String filename) {
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(filename));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		
 		String line = null;
 		try {
 			line = br.readLine().trim();
 			vocabSize = Integer.parseInt(line);
 			while( (line = br.readLine()) != null) {
 				line = line.trim();
 				if(line.isEmpty()) {
 					continue;
 				}
 				addItem(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.err.println("error reading vocab file");
 		}
 		if(vocabSize != wordToIndex.size()) {
 			System.out.println("Vocab file corrputed: header size and the vocab size do not match");
 			System.exit(-1);
 		}
 	}
 	
 	public int getIndex(String word) {
 		if(wordToIndex.containsKey(word)) {
 			return wordToIndex.get(word);
 		} else {
 			//word not found in vocab
 			if(debug) {
 				System.out.println(word + " not found in vocab");
 			}
 			return 0; //unknown id
 		}
 	}
 	
 }
