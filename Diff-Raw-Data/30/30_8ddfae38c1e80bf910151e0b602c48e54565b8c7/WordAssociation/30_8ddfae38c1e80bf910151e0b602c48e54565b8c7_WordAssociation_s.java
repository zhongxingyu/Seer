 package seward.wordassociation;
 
 import org.jgrapht.alg.*;
 import java.util.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class WordAssociation {
 
 	AssociationGraph  graph;
 	ArrayList<String> wordList;
 	BellmanFordShortestPath<String, WeightedEdge> bfPath;
 	String sourceWord;
 
 	public static void main(String[] args) {
 		
 		WordAssociation wa = new WordAssociation("master_word_list.txt", "adjusted_edges_list.txt");		
 		
 		try{
			wa.setSourceWord("DEATH");
 			ArrayList<WordPath> paths = wa.getAllPaths();
			for(int i=0; i < 1000; i++){
 				WordPath p = paths.get(i);
 				System.out.println(p.toString());
 			}
 		}catch(Exception e){
 			System.out.println(e);
 		}
 	}
 	
 	//constructor
 	public WordAssociation(String wordListPath, String edgeListPath){		
 		final String dir = System.getProperty("user.dir");
         System.out.println("current dir = " + dir);
 
 		graph = new AssociationGraph();
         loadWordList(wordListPath);
         loadEdges(edgeListPath);
 	}	
 
 	public ArrayList<WordPath> getAllPaths() throws Exception
 	{
 		if(this.sourceWord == null)
 			throw new Exception("Source word has not been set");
 		ArrayList<WordPath> list = new ArrayList<WordPath>();
 		for(int i=0; i<wordList.size(); i++){
 			String targetWord = wordList.get(i);
 			if(targetWord.equals(this.sourceWord) == false){
 				try{
 					WordPath p = pathForWord(targetWord);
 					list.add(p);
 				}catch(java.lang.IllegalArgumentException e){
 					System.out.println("Bad target word: " + targetWord);
 				}
 			}
 		}
 		Collections.sort(list);
 		return list;
 	}
 	
 	public WordPath pathForWord(String word) throws Exception
 	{
 		if(this.sourceWord == null)
 			throw new Exception("Source word not set");
 		String wordId = wordIdForWord(word);
 		List<WeightedEdge> list = bfPath.getPathEdgeList(wordId);
 		WordPath p = new WordPath(list, this.sourceWord, wordList);
 		p.setCost(bfPath.getCost(wordId));
 		return p;
 	}
 	
 	public void setSourceWord(String word) throws Exception
 	{
 		String wordId = wordIdForWord(word);
 		this.bfPath = new BellmanFordShortestPath<String,WeightedEdge>(this.graph, wordId);
 		this.sourceWord = word;
 	}
 	
 	private String wordIdForWord(String word) throws Exception{
 		//TODO throw error if word is not found
 		//TODO optimize this, use a hash
 		String uppercaseWord = word.toUpperCase();
 		String wordId = null;
 		for(int i=0; i < wordList.size(); i++){
 			if(wordList.get(i).equals(uppercaseWord))
 			{
 				wordId = i + "";
 				break;
 			}
 		}
 		if(wordId == null)
 			throw new Exception("Word not found");
 		return wordId;
 	}
 	
 	public ArrayList<WordPath> listFromWord(String word){
 		ArrayList<WordPath> list = new ArrayList<WordPath>();
 		
 		return list;
 	}
 	
 	private String wordForIndexString(String indexStr){
 		int index = Integer.parseInt(indexStr);
 		return wordList.get(index);
 	}
 	
 	/// Load Data from files ///
 	
 	public void loadWordList(String fileName){
 		BufferedReader br = null;
 		wordList = new ArrayList<String>();
 
 		try {
 			String sCurrentLine;
 			br = new BufferedReader(new FileReader(fileName));
 
 			while ((sCurrentLine = br.readLine()) != null) {
 				wordList.add(sCurrentLine);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	public void loadEdges(String fileName) {
 		BufferedReader br = null;
 
 		try {
 			String sCurrentLine;
 			br = new BufferedReader(new FileReader(fileName));
 
 			while ((sCurrentLine = br.readLine()) != null) {
 				//System.out.println(sCurrentLine);
 				parseAndLoadLine(sCurrentLine);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (br != null)br.close();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	private void parseAndLoadLine(String line){
 		String[] elements = line.split("\t");
 		if(elements.length < 3)
 			return;
 		String cue = elements[0];
 		String target = elements[1];
 		String weightStr = elements[2];
 		
 		double weight = 1.0 - Double.parseDouble(weightStr);
 		
 		graph.addVertex(cue);
 		graph.addVertex(target);
 		WeightedEdge e = graph.addEdge(cue, target);
 		graph.setEdgeWeight(e, weight);
 	}
 }
 
 
