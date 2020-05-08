 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Stack;
 
 class run_tagger {
     
     /////////////////////////////////////
     // nested class for viterbi algorithm
     /////////////////////////////////////
     
 	static class TrellisNode {
 	    public String tag;
 	    public double prob;
 	    public TrellisNode backPtr;
 	}
 	
 	
 	
 	///////////////////////////////////////
 	// nested class for Hidden Markov Model
 	///////////////////////////////////////
 	
 	static class HMM {
 	    public Set<String> tagSet;
         public Set<String> wordSet;
 	    public String[] tagArray;
 	    public String[] wordArray;
 	    public Map<String, Map<String, Double>> tMatrix;
         public Map<String, Map<String, Double>> eMatrix;
 	}
     
     
     
 	public static void main(String args[]) {
	    // quick ref: java run_tagger sents.test model_file sents.out
 	    if (args.length != 3) {
 	        System.out.println("error: Wrong number of arguments.");
 	        System.out.println("usage: java run_tagger <sents.test> <model_file> <sents.out>");
 	        System.exit(1);
         }
         // take in params
         String testFile = args[0];
         String modelFile = args[1];
         String outFile = args[2];
         
         
         try {
             // load HMM
             FileReader modelReader = new FileReader(modelFile);
             BufferedReader modelBr = new BufferedReader(modelReader);
             
             
             Set<String> tagSet = new HashSet<String>();
             Set<String> wordSet = new HashSet<String>();
             ArrayList<String> tagList = new ArrayList<String>();
     	    ArrayList<String> wordList = new ArrayList<String>();
     	    // counts
     	    Map<String, Map<String, Integer>> TMatrix = new HashMap<String, Map<String, Integer>>();
             Map<String, Map<String, Integer>> EMatrix = new HashMap<String, Map<String, Integer>>();
             // probabilities
             Map<String, Map<String, Double>> tMatrix = new HashMap<String, Map<String, Double>>();
             Map<String, Map<String, Double>> eMatrix = new HashMap<String, Map<String, Double>>();
             
            String modelLine = "";
             int lineCounter = 1;
             
             while((modelLine = modelBr.readLine()) != null) {
                 // LINE_1: tagList
                 if (lineCounter == 1) {
                     // break each line into an array of "tag" tokens
                     String[] tags = modelLine.trim().split("\\s+");
                     for (int i = 0; i < tags.length; i++) {
                         tagList.add(tags[i]);
                         tagSet.add(tags[i]);
                     }
                 }
                 // LINE_2: wordList
                 else if (lineCounter == 2) {
                     // break each line into an array of "word" tokens
                     String[] words = modelLine.trim().split("\\s+");
                     for (int i = 0; i < words.length; i++) {
                         wordList.add(words[i]);
                         wordSet.add(words[i]);
                     }
                 }
                 // LINE_3 - LINE47: transition matrix
                 else if (lineCounter >= 3 && lineCounter <= 47) {
                     int tagListIndex = lineCounter - 3;
                     String currTag = tagList.get(tagListIndex);
                     
                     Map<String, Integer> hm = new HashMap<String, Integer>();
                     String[] counts = modelLine.trim().split("\\s+");
                     for (int i = 0; i < counts.length; i++) {
                         String currToTag = tagList.get(i);
                         hm.put( currToTag, new Integer(counts[i]));
                     }
                     TMatrix.put( currTag, hm );
                 }
                 // LINE_48 - LINE_92: emission matrix
                 else {
                     int tagListIndex = lineCounter - 48;
                     String currTag = tagList.get(tagListIndex);
                     
                     Map<String, Integer> hm = new HashMap<String, Integer>();
                     String[] counts = modelLine.trim().split("\\s+");
                     for (int i = 0; i < counts.length; i++) {
                         String currWord = wordList.get(i);
                         hm.put( currWord, new Integer(counts[i]));
                     }
                     EMatrix.put( currTag, hm );
                 }
             }
             
             String[] tagArray = tagList.toArray(new String[0]);
             String[] wordArray = wordList.toArray(new String[0]);
             
             // calculate probabilities from counts
             // transition probabilities
             for (int i = 0; i < tagArray.length; i++) {
                 // first loop for calculating the total count
                 int total = 0;
                 for (int j = 0; j < tagArray.length; j++)
                 {
                     Integer count = TMatrix.get(tagArray[i]).get(tagArray[j]);
                    total += (count == null) ? 0 : count.intValue();
                 }
                 // second loop for calculating probabilities
                 Map<String, Double> hm = new HashMap<String, Double>();
                 for (int j = 0; j < tagArray.length; j++)
                 {
                     Integer count = TMatrix.get(tagArray[i]).get(tagArray[j]);
                     double prob = ( (count == null) ? 0 : count.intValue() ) / (double) total;
                     hm.put( tagArray[j], new Double(prob) );
                 }
                 tMatrix.put( tagArray[i], hm );
             }
             // emission probilities
             for (int i = 0; i < tagArray.length; i++) {
                 // first loop for calculating the total count
                 int total = 0;
                 for (int j = 0; j < wordArray.length; j++)
                 {
                     Integer count = EMatrix.get(tagArray[i]).get(wordArray[j]);
                     total += (count == null) ? 0 : count.intValue();
                 }
                 // second loop for calculating probabilities
                 Map<String, Double> hm = new HashMap<String, Double>();
                 for (int j = 0; j < wordArray.length; j++)
                 {
                     Integer count = EMatrix.get(tagArray[i]).get(wordArray[j]);
                     double prob = ( (count == null) ? 0 : count.intValue() ) / (double) total;
                     hm.put( wordArray[j], new Double(prob) );
                 }
                 eMatrix.put( tagArray[i], hm );
             }
             
             HMM hmm = new HMM();
             hmm.tagSet = tagSet;
             hmm.wordSet = wordSet;
             hmm.tagArray = tagArray;
             hmm.wordArray = wordArray;
             hmm.tMatrix = tMatrix;
             hmm.eMatrix = eMatrix;
             // close model buffer
             modelBr.close();
             
            
              // read test input            
             FileReader testReader = new FileReader(testFile);
             BufferedReader testBr = new BufferedReader(testReader);
             
             // prepare output file
             FileWriter outWriter = new FileWriter(outFile);
             BufferedWriter outBw = new BufferedWriter(outWriter);
             
             // process input and output results from viterbi algorithm
             String currLine;
             while ((currLine = testBr.readLine()) != null) {
                 // break each line into an array of tokens
                 String[] tokens = currLine.trim().split("\\s+");
                 ArrayList<String> tags = Vite(tokens, hmm);
                 
                 // write to out file
                 String outputLine = "";
                 for (int i = 0; i < tokens.length; i++) {
                     outputLine += tokens[i] + "/" + tags.get(i) + " ";
                 }
                 outBw.write(outputLine.trim());
                 outBw.newLine();
             }
             // close input and output buffer
             testBr.close();
             outBw.close();
         } catch (Exception e) {
             System.err.println("Error: " + e.getMessage());
         }
 	}
 	
 	
 	
 	//////////////////////////////////////
 	// Implementaion of viterbi algorithm
 	//////////////////////////////////////
 	public static ArrayList<String> Vite(String[] words, HMM hmm) {
 	    // retrieve model paramenters
 	    String[] tagArray = hmm.tagArray;
 	    String[] wordArray = hmm.wordArray;
 	    Map<String, Map<String, Double>> tMatrix = hmm.tMatrix;
         Map<String, Map<String, Double>> eMatrix = hmm.eMatrix;
         
         // set up a trellis while calculating the forward probabilities
         ArrayList<ArrayList<TrellisNode>> trellis = new ArrayList<ArrayList<TrellisNode>>();
         // keep track the max probability for a column
         TrellisNode maxTerminalNode = null;
         for (int i = 0; i < words.length; i++) {
             // set up a column in trellis
             ArrayList<TrellisNode> col = new ArrayList<TrellisNode>();
             for (String tag : tagArray) {
                 TrellisNode tNode = new TrellisNode();
                 tNode.tag = tag;
                 tNode.backPtr = (i == 0) ? null : nodeWithMaxPrevTimesTran(trellis.get(i - 1), tNode.tag, tMatrix);
                tNode.prob = (i == 0) ? eMatrix.get(tag).get(words[i]) : tNode.backPtr.prob * tMatrix.get(tNode.backPtr.tag).get(tag) * eMatrix.get(tag).get(words[i]);
                 col.add(tNode);
             }
             trellis.add(col);
             // reset max terminal node is not the last  column
             if (i == words.length - 1) {
                 maxTerminalNode = nodeWithMaxPrevTimesTran(trellis.get(i - 1), "</s>", tMatrix);
             }
         }
         
         // backtrace from max node
         Stack<String> optimalPathStack = new Stack<String>();
         TrellisNode currNode = maxTerminalNode;
         while(currNode != null) {
             optimalPathStack.push(currNode.tag);
             currNode = currNode.backPtr;
         } 
         // pop stack to constrct the tag list in order
         ArrayList<String> optimalPath = new ArrayList<String>();
         while (!optimalPathStack.empty()) {
             optimalPath.add(optimalPathStack.pop());
         }
 	    
 	    return optimalPath;
 	}
 	
 	
 	
 	/////////////////////////////////////////////////////////////////////
 	// find the max node that will result in local max (for back tracing)
 	//////////////////////////////////////////////////////////////////////
 	public static TrellisNode nodeWithMaxPrevTimesTran(ArrayList<TrellisNode> col, String transToTag, Map<String, Map<String, Double>> tMatrix) {
 	    TrellisNode maxNode = null;
 	    for (TrellisNode node : col) {
             if (maxNode == null)
                 maxNode = node;
             else {
                 // assume equal probability won't update max
                if (maxNode.prob * tMatrix.get(maxNode.tag).get(transToTag) < node.prob * tMatrix.get(node.tag).get(transToTag))
                     maxNode = node;
             }
 	    }
 	    return maxNode;
 	}
 }
