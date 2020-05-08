 // 05/21/11 23:50 Initial creation. Wrote constructors
 // 05/22/11 00:47 Corrected error in memorize function
 //		  Removed debugging print statements
 // 05/22/11 01:10 Added basic utter method (no weighting)
 
 import java.lang.*;
 import java.util.*;
 
 public class reverseIdx {
 //Data structure
 // Each word is kept in a central Map
 // Each map entry references a second Map with all of the words and follow percentages
 // Memory: O(n^2) (can we reduce this by only keeping non zero maps?) DONE
 
 	//Internal Data
 	//Central 'memory' structure
 	private HashMap<String,HashMap<String,Integer>> centralMap;
 	
 	//Word signalling end of sentence
 	private String endSent = "ENDSENT";
 
 	//Default Constructor
 	public reverseIdx() {
 		//Initialize the central memory array
 		centralMap = new HashMap<String,HashMap<String,Integer>>();
 	}
 
 	//Constructor with first string Init
 	public reverseIdx(String firstWords) {
 		//Initialize central memory array
 		centralMap = new HashMap<String,HashMap<String,Integer>>();
 
 		this.memorize(firstWords);
 	}
 
 	//Learning new words
 	public int memorize(String memStr) {
 
 		//Tokenize input string (we don't care about nonword characters)
 		String [] tokens = memStr.toLowerCase().split("[^a-zA-Z0-9\\.]+");
 
 		//Cycle them all
 		// No last/first word probability implementation yet
 		for(int i = 0; i<tokens.length-1;i++) {
 			//We skip over empty tokens
 			if (tokens[i].length()<=1) {
 				continue;
 			}
			
 			//Special case for end sentence
 			if(tokens[i].charAt(tokens[i].length()-1)=='.'){
 				String currInp = tokens[i].substring(0,tokens[i].length()-1);
 				//System.out.printf("attempt to append: %s\n",currInp);
 				//Check for existence of current key
 				if(centralMap.containsKey(currInp)) {
 					
 					//Check for existence of reference to next token key
 					if(centralMap.get(currInp).containsKey(endSent)) {
 						Integer temp = 1;
 						temp += centralMap.get(currInp).get(endSent);
 						centralMap.get(currInp).remove(endSent);
 						centralMap.get(currInp).put(endSent,temp);
 					}
 					else {
 						centralMap.get(currInp).put(endSent,new Integer(1));
 					}
 				}
 				else {
 					HashMap<String,Integer> temp = new HashMap<String,Integer>();
 					temp.put(endSent,new Integer(1));
 					centralMap.put(currInp,temp);
 				}
 			}
 			else {
 				//We only care about the next word
 		
 				//Check for existence of current key
 				if(centralMap.containsKey(tokens[i])) {
 
 					//Check for existence of reference to next token key
 					if(centralMap.get(tokens[i]).containsKey(tokens[i+1])) {
 						Integer temp = 1;
 						temp += centralMap.get(tokens[i]).get(tokens[i+1]);
 						centralMap.get(tokens[i]).remove(tokens[i+1]);
 						centralMap.get(tokens[i]).put(tokens[i+1],temp);
 					}
 					else {
						centralMap.get(tokens[i]).put(tokens[i+1],new Integer(1));
 					}
 				}
 				else {
 					HashMap<String,Integer> temp = new HashMap<String,Integer>();
					temp.put(tokens[i+1],new Integer(1));
 					centralMap.put(tokens[i],temp);
 				}
 			}
 		}
 
 		// Success!
 		return 0;
 	}
 
 	// Now for the fun part
 	// Novel utterance generation
 	public String utter(int phraseLen) {
 
 		// Get relevant values
 		int totalWords = centralMap.size();
 		Random r = new Random();
 		Object [] keys = centralMap.keySet().toArray();
 		String sayOut = (String)(keys[r.nextInt(totalWords)]);
 		String currWord = sayOut;
 
 		// Loop until a phrase of a proper length is generated.
 		for(int i = 1; i< phraseLen; i++) {
 			Object[] nextKeys = centralMap.get(currWord).keySet().toArray();
 			
 			//Basic weighting
 			ArrayList<String> weightKeys = new ArrayList<String>();
 			for(int k = 0; k<nextKeys.length; k++){
 				for(int j = 0;j<=centralMap.get(currWord).get((String)nextKeys[k]);j++) {
 					weightKeys.add((String)nextKeys[k]);
 				}
 			}
 			
 			int temp = r.nextInt(weightKeys.size());
 			currWord = (String)(weightKeys.get(temp));
 			if(currWord.equals(endSent)){
 				sayOut+=".";
				currWord = (String)(keys[r.nextInt(nextKeys.length)]);
 			}
 			else if(!centralMap.containsKey(currWord)) {
				sayOut += " "+currWord;
				currWord = (String)(keys[r.nextInt(nextKeys.length)]);
 			}
 			
 			sayOut += " "+currWord;
 		}
 
 		return sayOut;
 	}
 
 	//Debugging purposes only
 	public void testPrint() {
 		System.out.println(centralMap.toString());
 	}
 
 }
