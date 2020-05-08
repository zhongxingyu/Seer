 import java.util.Vector;
 
 
 public class MaxHeap {
 	
 	protected Vector<Vertex> vertices;
 	
 	public MaxHeap(){
 		this.vertices = new Vector<Vertex>();
 		this.vertices.add(null);
 	}
 
 	public void insert(int id, int chave){
 		Vertex vertex = new Vertex(id, chave);
 		this.vertices.add(vertex);
 		insertHelper();
 	}
 	
 	public void extract(){
 		
 	}
 	
 	public void decrease(){
 		
 	}
 	
 	public void increase(){
 		
 	}
 	
 	public void maxHeapfy(){
 		
 	}
 	
 	public void insertHelper(){
 		int currentPosition = vertices.size() -1;
 		Vertex auxVertex;
 		
		while( (currentPosition != 1) && (vertices.get(currentPosition).currentCost > vertices.get(currentPosition/2).currentCost) ){
 			auxVertex = vertices.get(currentPosition);
 			vertices.set(currentPosition, vertices.get(currentPosition/2));
 			vertices.set(currentPosition/2, auxVertex);
 		}
 	}
 	
 	public static void main(String[] args){
 		MaxHeap hp = new MaxHeap();
 		hp.insert(1, 100);
 		hp.insert(2, 300);
 		hp.insert(3, 250);
		System.out.println(hp.vertices.get(1).currentCost+" "+hp.vertices.get(2).currentCost+" "+hp.vertices.get(3).currentCost+" ");
 
 	}
 	
 }
