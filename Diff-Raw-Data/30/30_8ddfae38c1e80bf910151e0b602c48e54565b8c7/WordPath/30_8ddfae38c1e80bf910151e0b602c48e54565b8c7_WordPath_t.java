 package seward.wordassociation;
 import java.util.*;
 
 public class WordPath implements Comparable<WordPath>{
	private ArrayList<String>vertices;
 	private ArrayList<String>wordList;
 	
 	double cost;
 	
 	WordPath(List<WeightedEdge> edgeList, String sourceWord, ArrayList<String> wordList){
 		this.wordList = wordList;
 		vertices = new ArrayList<String>();
 		convertEdgeListToVertices(edgeList);
 		orderList(sourceWord, 0);
 		removeDuplicatesInVerticesList();
 	}
 	
	private void convertEdgeListToVertices(List<WeightedEdge> edgeList){
 		for(int i=0; i < edgeList.size(); i++){
 			WeightedEdge edge = edgeList.get(i);
 			String sourceString = (String)edge.getSource();
 			vertices.add(wordForIndexString(sourceString));
 			String targetString = (String)edge.getTarget();
 			vertices.add(wordForIndexString(targetString));
 		}
 	}
 	
	private void orderList(String startWord, int index){
 		if(index >= vertices.size()-1)
 			return;
 		String str1 = vertices.get(index);
 		String str2 = vertices.get(index+1);
 		//TODO assert that one word matches startWord
 		if(!str1.equals(startWord)){
 			Collections.swap(vertices, index, index+1);
 			orderList(str1, index+2);
 		} 
 		else {
 			orderList(str2, index+2);
 		}
 	}
 	
	private void removeDuplicatesInVerticesList()
 	{
 		Iterator<String> iter = vertices.iterator();
 		String last = null;
 		while(iter.hasNext()){
 			String current = iter.next();
 			if(current.equals(last))
 				iter.remove();
 			last = current;
 		}
 		System.out.print("");
 	}
 	
 	private String wordForIndexString(String indexStr){
 		int index = Integer.parseInt(indexStr);
 		return wordList.get(index);
 	}
 	
 
 	
 	String getSource(){
 		return vertices.get(0);
 	}
 	
 	String getTarget(){
 		return vertices.get(vertices.size()-1);
 	}
 	
 	void setCost(double c){
 		this.cost = c;
 	}
 	
 	double getCost()
 	{
 		return cost;
 	}
 	
	ArrayList<String> getPathVertices(){
		return vertices;
	}
	
 	public int compareTo(WordPath comparePath) {
 		return (this.getCost() > comparePath.getCost()) ? 1 : -1;
 	}
 	
 	public String toString()
 	{
 		String str = "";
 		for(int i=0; i<vertices.size(); i++){
 			str += vertices.get(i) + " ";
 		}
 		str += " | cost: " + this.getCost();
 		return str;
 	}
 	
 }
