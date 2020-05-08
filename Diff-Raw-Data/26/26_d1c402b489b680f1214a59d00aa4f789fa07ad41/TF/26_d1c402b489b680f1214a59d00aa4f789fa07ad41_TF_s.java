 import java.io.FileInputStream;
 import java.io.DataInputStream;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 public class TF {
 
 	private HashMap<String,Double> tfValues;
 	private HashMap<String,HashMap<String, Double>> docstf;
	int wordCount = 0;
 
 	//constructor for TF class
 	public TF(){
 		docstf = new HashMap<String,HashMap<String, Double>>();
 	}
 
 	/*
 	generate TF values for each word in each document and save it all in a HashMap of HashMaps
 	*/
 	public void calculateTF(String[] docs) {
 		 for(int j=0; j<docs.length;j++){
 			try{
 					// Open the file that is given
 					// command line parameter
 					FileInputStream fstream = new FileInputStream(docs[j]);
 					// Get the object of DataInputStream
 					DataInputStream in = new DataInputStream(fstream);
 					BufferedReader br = new BufferedReader(new InputStreamReader(in));
 					String strLine;
 					tfValues = new HashMap<String, Double>();   //initialize a new HashMap for the document tf values
 					//Read File Line By Line
 					while ((strLine = br.readLine()) != null)
 					{
 						strLine = strLine.toLowerCase();    //normalize all text to lowercase
 						String[] words = strLine.split("\\W");     //tokenize each line with everything not alphanumeric as delimiter
 						for (String s:words){
 							if(!s.isEmpty()){
 								if (tfValues.containsKey(s)) //word has appeared before in doc
 								{
 									tfValues.put(s,tfValues.get(s)+1.0);  //aggregate word appearance counter
 								}else{
 									tfValues.put(s,1.0);   //word has not appeared before
 								}
 								wordCount++;  //keep counter for total number of words in doc
 							}
 						}
 
 					}
 					for(Map.Entry<String,Double> entry : tfValues.entrySet()){
 						entry.setValue(entry.getValue()/wordCount);
 					}
 					docstf.put(docs[j],tfValues);
 					//Close the input stream
 					in.close();
 				}catch (Exception e){//Catch exception if any
 					System.err.println("Error: " + e.getMessage());
 				}
 			}
		//iterate over HashMap and replace each word's count with its TF value
		for(Map.Entry<String,Double> entry : tfValues.entrySet()){
			entry.setValue(entry.getValue()/wordCount);
		}
 	}
 
 	public double getTF(String doc,String word){
 		word = word.toLowerCase();     //normalize word to lowercase
 		HashMap<String, Double> tf = docstf.get(doc);
 		if(tf.containsKey(word)){
 			return tf.get(word);
 		}else{
 			return 0;
 		}
 	}
 
 	public HashMap<String,HashMap<String, Double>> getDocstf(){
 		return docstf;
 	}
 }
