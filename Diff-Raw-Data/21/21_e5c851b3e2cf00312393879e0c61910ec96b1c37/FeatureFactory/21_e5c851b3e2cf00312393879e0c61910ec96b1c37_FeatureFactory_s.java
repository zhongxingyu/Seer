 package cs224n.deep;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 import org.ejml.simple.*;
 
 
 public class FeatureFactory {
 
 
 	private FeatureFactory() {
 
 	}
 
 	 
 	static List<Datum> trainData;
 	/** Do not modify this method **/
 	public static List<Datum> readTrainData(String filename) throws IOException {
         if (trainData==null) trainData= read(filename);
         return trainData;
 	}
 	
 	static List<Datum> testData;
 	/** Do not modify this method **/
 	public static List<Datum> readTestData(String filename) throws IOException {
         if (testData==null) testData= read(filename);
         return testData;
 	}
 	
 	private static List<Datum> read(String filename)
 			throws FileNotFoundException, IOException {
 		List<Datum> data = new ArrayList<Datum>();
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 		for (String line = in.readLine(); line != null; line = in.readLine()) {
 			if (line.trim().length() == 0) {
 				continue;
 			}
 			String[] bits = line.split("\\s+");
 			String word = bits[0];
 			String label = bits[1];
 
 			Datum datum = new Datum(word, label);
 			data.add(datum);
 		}
 
 		return data;
 	}
  
  
 	// Look up table matrix with all word vectors as defined in lecture with dimensionality n x |V|
 	static SimpleMatrix allVecs; //access it directly in WindowModel
 	public static SimpleMatrix readWordVectors(String vecFilename) throws IOException {
 		if (allVecs != null) 
 			return allVecs;
 		
		allVecs = new SimpleMatrix(numToWord.size(), 50);
 		
		int currRow = 0;
 		
 		BufferedReader in = new BufferedReader(new FileReader(vecFilename));
 		for(String line = in.readLine(); line != null; line = in.readLine()) {
			int currCol = 0;
 			Scanner f = new Scanner(line);
 			f.useLocale(Locale.US);
 			f.useDelimiter("\\s+");
 			while(f.hasNextDouble()) {
				allVecs.set(currRow, currCol, f.nextDouble());
 			}
			currRow++;
			if(currRow % 10000 == 0)
				System.out.println("Loaded " + currRow + " vectors into the matrix.");
 		}
 		in.close();
 
		System.out.println("Loaded " + currRow + " vectors into the matrix. Done.");
 		return allVecs;
 	}
 	// might be useful for word to number lookups, just access them directly in WindowModel
 	public static HashMap<String, Integer> wordToNum = new HashMap<String, Integer>(); 
 	public static HashMap<Integer, String> numToWord = new HashMap<Integer, String>();
 	
 	public static HashMap<String, Integer> initializeVocab(String vocabFilename) throws IOException {
 		HashMap<String, Integer> h = new HashMap<String, Integer>();
 		
 		int i = 0;
 		BufferedReader in = new BufferedReader(new FileReader(vocabFilename));
 		for(String line = in.readLine(); line != null; line = in.readLine()) {
 			wordToNum.put(line, i);
 			numToWord.put(i, line);
 			h.put(line, i);
 			i++;
 		}
 		in.close();
 
 		System.out.println("Vocab initialized to " + i + " words. Done.");
 		
 		return wordToNum;
 	}
 
 
 }
