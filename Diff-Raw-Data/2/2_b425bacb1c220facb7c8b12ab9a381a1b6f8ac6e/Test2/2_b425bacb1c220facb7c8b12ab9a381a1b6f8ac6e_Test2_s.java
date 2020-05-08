 // CSI4107  Assignment 2
 // 5572999  KEVIN FONG
 // 5567184  DANIEL ST JULES
 
 import weka.core.*;
 import weka.core.converters.*;
 import weka.classifiers.trees.*;
 import weka.filters.*;
 import weka.filters.unsupervised.attribute.*;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 
 public class Test2 
 {
 	public static void main(String[] args) throws Exception 
 	{
 		// Store hashmaps and buffered readers so we can iterate over them
 		String[] types= {"Positive", "Negative"};
 		
 		Map<String,BufferedReader> reader = new HashMap<String,BufferedReader>();
 		reader.put("Positive", new BufferedReader(new FileReader("PositiveWordList.txt")));
 		reader.put("Negative", new BufferedReader(new FileReader("NegativeWordList.txt")));
 		
 		Map<String,Map<String,Integer>> wordHash = new HashMap<String,Map<String,Integer>>();
 		wordHash.put("Positive", new HashMap<String,Integer>());
 		wordHash.put("Negative", new HashMap<String,Integer>());
 		
 		// Use our CustomAnalyzer from Assignment 1, once again, with the default 
 		// Lucene stop word list
 		Analyzer analyzer = new CustomAnalyzer(org.apache.lucene.util.Version.LUCENE_CURRENT);
 		
 		// We'll read in positive and negative word lists, stem them, and store 
 		// them in corresponding hash maps for use with our features
 		for (String type : types) {
 			String inputWord;
 			
 			while ((inputWord = reader.get(type).readLine()) != null) {
 				//Removes #X, where X is some number.
				inputWord = inputWord.replaceAll("#.*$",""); 
 				
 				// Stem the inputWord.
 				TokenStream stream  = analyzer.tokenStream(null, new StringReader(inputWord));
 				stream.reset();
 				stream.incrementToken();
 				inputWord = stream.getAttribute(CharTermAttribute.class).toString();
 				
 				// Add the word to the hash map if its length is greater than zero
 				// and isn't already present
 				Map<String,Integer> hashMap = wordHash.get(type);
 				if ((inputWord.length() > 0) && !(hashMap.containsKey(inputWord)))
 					hashMap.put(inputWord, 1);
 			}
 			reader.get(type).close();
 			System.out.println("Done with " + type + "WordList.txt");
 		}
 		
 		// Define lists of emoticons and give them a higher weight than normal 
 		// words. This will hopefully allow us to make use of emoticons in classifying 
 		// tweets while still being able to accommodate sarcasm to some extent.
 		Map<String,BufferedReader> eReader = new HashMap<String,BufferedReader>();
 		eReader.put("Positive", new BufferedReader(new FileReader("PositiveEmoticonList.txt")));
 		eReader.put("Negative", new BufferedReader(new FileReader("NegativeEmoticonList.txt")));
 		
 		for (String type : types) {
 			String emoticon;
 			
 			while ((emoticon = eReader.get(type).readLine()) != null) {
 				// Add the emoticon to the corresponding hash with a higher weight
 				Map<String,Integer> hashMap = wordHash.get(type);
 				if ((emoticon.length() > 0) && !(hashMap.containsKey(emoticon)))
 					hashMap.put(emoticon, 5);
 			}
 			eReader.get(type).close();
 			System.out.println("Done with " + type + "EmoticonList.txt");
 		}
 	
 		String[] fileNames = {"TrainingData.txt","TestData.txt"};
 		
 		// Process training and test data files
 		for (int i = 0; i < fileNames.length; i++) {
 			// Construct the BufferedReader object for input file fileNames[i].
 			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileNames[i]));
 			// Construct the BufferedWriter object for output file.
 			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
 					fileNames[i].replace(".txt","") + "_intermediate_data.txt"));
 			
 			// Write out a CSV header into output file.
 			String header = "attribute_sid" + "\t" + "attribute_uid" + "\t" + 
 				"positive_word_count" + "\t" + "negative_word_count" + "\t" + 
 				"class_topic";
 			bufferedWriter.write(header);
 			bufferedWriter.newLine();
 
 			// Read every line from input and extract features, and write out.
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				// The line to be written.
 				String newLine = "";
 				// Positive and Negative word counts.
 				int positiveWordCount = 0;
 				int negativeWordCount = 0;
 
 				// Split the String line on tab characters. 
 				// Element 1 is the SID, Element 2 is the UID, Element 3 is the 
 				// TOPIC, and Element 4 is TWITTER_MESSAGE
 				String[] stringArray = line.split("\\t");
 
 				// Build a TokenSteam object to tokenize and process the TwitterMessage 
 				// component of String line.
 				TokenStream stream  = analyzer.tokenStream(null, new StringReader(stringArray[3]));
 
 				// Process TWITTER_MESSAGE component of String line.
 				try {
 					String token = "";
 					stream.reset();
 					while(stream.incrementToken()) {
 						token = stream.getAttribute(CharTermAttribute.class).toString();
 						// Check if token is a positive word.
 						if (wordHash.get("Positive").containsKey(token)) {
 							positiveWordCount += wordHash.get("Positive").get(token);
 						}
 						// Check if token is a negative word.
 						if (wordHash.get("Negative").containsKey(token)) {
 							negativeWordCount += wordHash.get("Negative").get(token);
 						}
 					}
 				}
 				catch (Exception e) {
 					// Do nothing.
 				}
 
 				// Build the newLine, that conforms to the header.
 				newLine = stringArray[0] + " \t" + stringArray[1] + "\t" + 
 					positiveWordCount + "\t" + negativeWordCount+ "\t" +stringArray[2];
 				
 				// Write newLine to file, and skip to next line
 				bufferedWriter.write(newLine);   
 				bufferedWriter.newLine();
 			}
 			// Close bufferedWriter and bufferedReader.
 			bufferedWriter.close();
 			bufferedReader.close();
 
 			// Load the intermediate file into the Weka Instances object with a CSV loader.
 			CSVLoader loader = new CSVLoader();
 			File trainCSV = new File(fileNames[i].replace(".txt","") + "_intermediate_data.txt");
 			loader.setSource(trainCSV);
 			Instances dataRaw = loader.getDataSet();
 			
 			// Delete sid and uid attributes
 			dataRaw.deleteAttributeAt(dataRaw.attribute("attribute_sid").index());
 			dataRaw.deleteAttributeAt(dataRaw.attribute("attribute_uid").index());
 
 			// Change the relation name.
 			dataRaw.setRelationName(fileNames[i].replace(".txt","") + "_custom_features");
 			
 			// Save an arff file that Weka can use
 			ArffSaver saver = new ArffSaver();
 			saver.setInstances(dataRaw);
 			saver.setFile(new File(fileNames[i].replace(".txt","") + "_custom_features.arff"));
 			saver.writeBatch();
 			System.out.println("Created" + " " + fileNames[i].replace(".txt","") + 
 				"_custom_features.arff" +"!");
 			
 			// Remove the intermediate file.
 			trainCSV.delete();
 		}
 		
 		System.out.println("Done!");
 
 	}
 }
