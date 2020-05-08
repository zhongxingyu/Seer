 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.io.StringReader;
 import java.io.StreamTokenizer;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.File;
 import java.io.FileWriter;
 
 /*Weka imports*/
 import weka.core.Attribute;
 import weka.core.FastVector;
 import weka.core.Instance;
 import weka.core.Instances;
 
 public class Tokenizer {
 
 	/*Suggested structure:
 	* - This tokenizer class will deal with the content of the ReutersDocument objects.
 	* - methods will be used for tokenizing input and then applying preprocessing methods
 	* - suggested preprocessing methods: 
 	* -- case normalization (will be performed at tkn stg), smart detection on acronyms
 	* -- remove stopwords, stemming (remove prefixes/suffixes)
 	* -- these remaining words are then put back in the object to be used 
 	*/
 
 	private List<ReutersDocument> documentSet;
 	//private List<ReutersDocument> testSet;
 	//private List<ReutersDocument> trainingSet;
 	private StreamTokenizer	tokenizerObject;
 	private File stopwords;
 	private Hashtable<String, String> stopSet;
 	private BufferedReader bReader;
 	
 	public Tokenizer(List<ReutersDocument> documentSet) {
 	
 		this.documentSet = documentSet;
 		stopSet = new Hashtable<String,String>();
 	
 	}
 	
 	public List<ReutersDocument> tokenizeDocumentSet() {
 	
 		//variables for temporary storage of tokenized info.
 		String title;
 		String dateLine;
 		String body;
 		String[] tokenArray;
 		String tmpLine = "";
 		List<String> tokenList = new ArrayList<String>();
 		
 		//pull in stopwords and place in hash table to be used later.
 		try { 
 		
 			stopwords = new File("reuters21578/stopwords.txt");
 			bReader = new BufferedReader(new FileReader(stopwords));
 			
 			while((tmpLine = bReader.readLine())!=null) {
 				
 				String[] dataArray = tmpLine.split(",");
 						
 				for (String item:dataArray) { 
 					stopSet.put(item,item);
 				}
 			
 			}
 			
 			bReader.close();
 			
 			
 		} catch (FileNotFoundException err) {
 		
 			System.out.println(err.toString());
 			
 		} catch (IOException err) {
 		
 			System.out.println(err.toString());
 		
 		}
 		
 		//proceed to tokenize each document
 		
 		for (ReutersDocument document : documentSet) {
 			
 			//get body text from current document
 			body = document.getText();
 			
 			try {
 					
 				//enter text into a tokenizer object
 				tokenizerObject = new StreamTokenizer(new StringReader(body));
 				
 				while(tokenizerObject.nextToken() != StreamTokenizer.TT_EOF){
 				
 					if(tokenizerObject.ttype == StreamTokenizer.TT_WORD) {
 					
 						//normalize case of words.
 						if(!isStopWord(tokenizerObject.sval.toLowerCase())) {		
 							//pass to stem words method.
 							tokenList.add(stemWord(tokenizerObject.sval.toLowerCase()));
 						}
 						
 					} else if(tokenizerObject.ttype == StreamTokenizer.TT_EOL) {
 						System.out.println();	
 					}
 						
 				}
 			
 			} catch (Exception e) {
 			
 				e.toString();
 			
 			}
 			
 			//set body tokens
 			tokenArray = (String[]) tokenList.toArray(new String[0]);
 			document.setbodyTokens(tokenArray);
 			
 			//dispose contents of token list
 			tokenList.clear();
 		
 		}
 		
 		//stem documentset
 	
 		outputCollectionToArff(documentSet);
 	
 		return documentSet;
 		
 	}
 	
 	public boolean isStopWord(String word) {
 	
 		//checks existence in stopwords hashmap.
 		
 		if (stopSet.containsKey(word)) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 	
 	public String stemWord(String word) {
 	
 		String stemmed = "";
 		Stemmer stemmer = new Stemmer();
 		stemmer.add(word.toCharArray(),word.toCharArray().length);
 		stemmer.stem();
 		stemmed = stemmer.toString();
 		
 		return stemmed;
 		
 	}
 	
 	public void outputCollectionToArff(List<ReutersDocument> docSet) {
 	
 		//Create arff file from XML file for StringToWordVector representation.
 		//This is appropriate for Vector Space and Topic Based Vector Space Model.
 		//Convert to CSV and then to ARFF.
 		
 		File file = new File("reuters21578/arff/reut2-test.arff");
 		File topicsfile = new File("reuters21578/all-topics-strings.lc.txt");
 		String tmpLine = "";
 		String topicString = "";
 	
 		try {
  
 			// if file doesnt exists, then create it
 			if (!file.exists()) {
 				file.createNewFile();
 			}
 	 
 	 		//create writer object for file
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
 			BufferedReader reader = new BufferedReader(new FileReader(topicsfile));
 			
 			//read and store topics list
 			while((tmpLine = reader.readLine())!=null) {
 				
 				if (topicString=="") {
 					topicString = null;
 				} else {
 					topicString = topicString + "," + tmpLine;
 				}
 			
 			}
 			
 			reader.close();
 			
 			//write arff headers to File
 			writer.write("@RELATION 'documents'\n\n");
 			writer.write("@ATTRIBUTE words string\n");
 			writer.write("@ATTRIBUTE lewissplit {train,test} \n");		//this is needed to split training+test data 
			writer.write("@ATTRIBUTE class-att {"+topicString+"}\n\n");
 			
 			writer.write("@DATA\n");
 			
 			for (ReutersDocument doc : docSet) {
 				
 				writer.write("'");
 	
 				//write out string	
 				for (int i = 0; i < doc.getbodyTokens().length; i++) {
 				
 					if (doc.getbodyTokens()[i]!=null) {
 						writer.write(doc.getbodyTokens()[i].replace("\n","") + ":");
 					}
 				
 				}
 				
 				//write out topics for class (null if n/a)				
 				writer.write("'");
 				
 				//write out the lewissplit attribute
 				writer.write("," + doc.getLewis().toLowerCase());
 				
 				//write out the class 
 				if (!(null == doc.getTopicList())) {
 					writer.write(",'" + doc.getTopicList() + "'");
 				}
 				else {
 					writer.write("," + "?");
 				}
 				
 				writer.newLine();
 				writer.flush();
 			
 			}
 			
 			writer.close();
 		
 		} catch (Exception e) {
 		
 			System.out.println(e.toString());
 		
 		}
 	}
 
 }
