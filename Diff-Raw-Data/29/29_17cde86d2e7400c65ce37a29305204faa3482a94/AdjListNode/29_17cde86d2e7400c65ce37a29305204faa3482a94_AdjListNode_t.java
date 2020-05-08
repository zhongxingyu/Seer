package thesaurus.parsingTemp;
 
 /**
  class to represent an entry in the adjacency list of a vertex
  in a graph
  */
 public class AdjListNode {
 
 	private int vertexNumber;
 
 	public AdjListNode(int n){
 		vertexNumber = n;
 	}
 	
 	public int getVertexNumber(){
 		return vertexNumber;
 	}
 	
 	public void setVertexNumber(int n){
 		vertexNumber = n;
 	}
 	
 	public String toString()
 	{
 		return Integer.toString(vertexNumber);
 	}
 	
 }
