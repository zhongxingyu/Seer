 package application;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * The processor is the top level controller of the whole project and
  * it has several functionalities as below: 
  * 1. Read in files/documents
  * 2. Output labeled or unlabeled output as input to MATLAB 
  * 3. Output relations to database for query
  */
 
 public class Processor {
 	 
 	private String inputFileName;
 	private String outputFileName;
 	
 	/* construct list of sentence object after parsing the JSON file */
 	private ArrayList<Article> articles;
 	
 	/* construct list of relation object as samples */
 	private ArrayList<Relation> relations;
 	
 	/* statistic used, get number of pos and neg samples seperately */
 	private int numPositive, numNegative;
 	
 	/* store the max column index in the traning matrix */
 	private int maxColIndex = 0;
 	
 	/* store word dictionary */
 	public static HashMap<String, Integer> dictionary;
 	/* store stop word dictionary */
 	public static HashMap<String, Integer> stopWordDictionary;
 	/* store part-of-speech dictionary */
 	public static HashMap<String, Integer> POSDictionary;
 	/* store part-of-speech sequence dictionary */
 	public static HashMap<String, Integer> POSSequenceDictonary;
 	/* store ner dictionary */
 	public static HashMap<String, Integer> nerDictionary;
 	/* store POS tagger for feature extraction */
 	public static Tagger tagger;
 	/* store NER for name entity extraction */
 	public static NER ner;
 
 	
 	public Processor(String inputFileName, String outputFileName){
 		this.inputFileName = inputFileName;
 		this.outputFileName = outputFileName;
 		articles = new ArrayList<Article>();
 		relations = new ArrayList<Relation>();
 		numNegative = 0;
 		numPositive = 0;
 		dictionary = new HashMap<String, Integer>();
 		stopWordDictionary = new HashMap<String, Integer>();
 		POSDictionary = new HashMap<String, Integer>();
 		POSSequenceDictonary = new HashMap<String, Integer>();
 		nerDictionary = new HashMap<String, Integer>();
 		tagger = new Tagger();
 		ner = new NER();
 	}
 
 	/**
 	 * Parse the input JSON file, construct sentence object from each JSON
 	 * object for further process and populate the sentence list
 	 * @param inf
 	 */
 	private void parseJSONFile(File inf) {
 		/* convert content of the file into string */
 		try {
 			BufferedReader reader;
 			reader = new BufferedReader(new FileReader(inf));
 
 			String line = null;
 			StringBuilder  stringBuilder = new StringBuilder();
 
 			while( ( line = reader.readLine() ) != null ) {
 				stringBuilder.append( line );
 			}
 			
 			// Parse the JSON string to JSON array
 			JSONArray records = new JSONArray(stringBuilder.toString());
 			
 			// construct one article object for each JSON object in JSON array
 			for (int i = 0; i < records.length(); ++i) {
 				JSONObject record = records.getJSONObject(i);
 				Article article = new Article(record.getString("url"), 
 						record.getString("body"));
 				articles.add(article);
 			}			
 			// close input stream
 			reader.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (JSONException e2) {
 			e2.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Read in necessary dictionary for feature construction
 	 * say, words dic., stop word dic., pos dic., ner dic.
 	 */
 	private void readDictionaries() {
 		this.readDictionary(dictionary, "3esl.txt");
 		this.readDictionary(stopWordDictionary, "stopword.txt");
 		this.readDictionary(POSDictionary, "pos.txt");
 		this.readDictionary(nerDictionary, "ner.txt");
 	}
 	
 	/**
 	 * Build word dictionary for feature construction for a particular file
 	 */
 	public void readDictionary(HashMap<String, Integer> dictionary, String fileName) {
 		File inf = Processor.readFile(fileName);
 		int wordIndex = 0;
 		try {
 			BufferedReader reader;
 			reader = new BufferedReader(new FileReader(inf));
 
 			String line = null;
 			while((line = reader.readLine()) != null ) {
 				if (dictionary.containsKey(line)) {
 					continue;
 				} else {
 					dictionary.put(line, ++wordIndex);
 				}
 			}
 			reader.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Read input file and return File object 
 	 * @param inputFileName String input file name
 	 * @return File object
 	 */
 	public static File readFile(String inputFileName) {
 		File inf = new File("files/" + inputFileName);
 		if(!inf.exists()) { 
 			System.err.println("ERROR: File: "+inf.getAbsolutePath()+" does not exist");
 			return null;
 		}
 		if(!inf.canRead()) { 
 			System.err.println("ERROR: File: "+inf.getAbsolutePath()+" cannot be read");
 			return null;
 		}
 		
 		System.out.println("INFO: finish reading file" + inf.getAbsolutePath());
 		return inf;
 	}
 	
 	/**
 	 * Extract relations for each article and populate current relations list
 	 */
 	private void extractRelations() {
 		LexicalizedParser lp = 
 				LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		relations.addAll(articles.get(3).extractRelations(lp));
 //		for (Article article : articles) {
 //			relations.addAll(article.extractRelations(lp));
 //		}
 	}
 	
 	/**
 	 * Output samples, output each relation extracted from all articles as a sample
 	 * in format of into output file 
 	 * label f1:v1 f2:v2 f3:v3, ... (same format used in libsvm)
 	 */
 	private void outputSamples(){
 		File outf = new File("files/" + outputFileName);
 		try {
 			Writer writer = new BufferedWriter(
 					new OutputStreamWriter(new FileOutputStream(outf), "UTF-8"));
 			StringBuffer sb = new StringBuffer();
 			for (Relation relation : relations) {
 				// append label
 				if (relation.getLabel()) {
 					numPositive++;
 					sb.append("1 ");
 				} else {
 					numNegative++;
 					sb.append("0 ");
 				}
 				// append features
 				sb.append(transformFeatureVector(relation.getFeaturesVector()));
 				// append end flag -1
 				sb.append("-1\n");
 			}
 			sb.insert(0, relations.size() + " " + maxColIndex + "\n");
 			sb.insert(0, "FEATURE_TRAIN_MATRIX\n");
 			writer.write(sb.toString());
 			System.out.println("num of positive relations: " + numPositive);
 			System.out.println("num of negative relations: " + numNegative);
 			writer.close();
 		} catch (IOException e1) {
 				e1.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This function is used to change format of feature vectors in format
 	 * that is convenient for ML module. Can be customized to any format.
 	 * @param rawFeatureVector
 	 * @return
 	 */
 	private String transformFeatureVector(String rawFeatureVector) {
 		StringBuffer sb = new StringBuffer();
 		Integer prevIndex = 0;
 		String[] pairs = rawFeatureVector.split(" ");
 		for (int i = 0; i < pairs.length; i++) {
 			String[] tokens = pairs[i].split(":");
 			Integer featureIndex = Integer.parseInt(tokens[0]);
 			Integer frequency = Integer.parseInt(tokens[1]);
 			sb.append((featureIndex - prevIndex) + " " + frequency + " ");
 			prevIndex = featureIndex;
 			// dynamically change the max column based on feature index of pos sequence
 			if ((i == pairs.length - 1) && featureIndex > maxColIndex)
 				maxColIndex = featureIndex;
 		}
 		return sb.toString();
 	}
 	
 	public static void main(String[] args) {
 		if (args.length != 2) {
 			System.err.println("usage: Processor inputfilename outputfilename");
 			return;
 		} else {
 			Processor processor = new Processor(args[0], args[1]);
 			File inf = processor.readFile(processor.inputFileName);
 			processor.parseJSONFile(inf);
 			processor.readDictionaries();
 			processor.extractRelations();
 			processor.outputSamples();
 		}
 	}
 	
 	/**
 	 * Set of utility print functions for debugging
 	 */
 	private void printArticles() {
 		for (Article article : articles) {
 			System.out.println(article.toString());
 		}
 	}
 	
 	private void printRelations() {
 		for (Relation relation : relations) {
 			System.out.println(relation.toString());
 		}
 	}
 }
