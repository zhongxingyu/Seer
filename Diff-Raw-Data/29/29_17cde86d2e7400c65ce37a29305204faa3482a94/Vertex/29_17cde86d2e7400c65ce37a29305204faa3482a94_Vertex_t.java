package thesaurus.parsingTemp;
 import java.util.LinkedList;
 
 
 
 /**
  class to represent a vertex in a graph
 */
 public class Vertex {
    
     private LinkedList<AdjListNode> adjList ; 
     private int index; 
     String word;
 
  
     public Vertex(String i)
     {
     	adjList = new LinkedList<AdjListNode>();
     	index = Integer.valueOf(i);
     }
     
     
     
     public String getWord() {
 		return word;
 	}
 
 
 	public void setWord(String word) {
 		this.word = word;
 	}
 
 
 
     
     public String toString()
     {
     	StringBuilder sb = new StringBuilder();
     	sb.append("Vertex "); sb.append(this.index+" ");
     	sb.append(this.word +": ");
     	for(AdjListNode n : this.adjList)
     	{
     		sb.append(n+",");
     	}
     	return sb.toString();
     }
      
   
     
     public LinkedList<AdjListNode> getAdjList(){
         return adjList;
     }
     
     public int getIndex(){
     	return index;
     }
     
     public void setIndex(int n){
     	index = n;
     }
     
     
    
     
     public void addToAdjList(int n){
         adjList.addLast(new AdjListNode(n));
     }
     
     public int vertexDegree(){
         return adjList.size();
     }
 }
