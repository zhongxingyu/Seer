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
 		
		while( (currentPosition > 1) && (vertices.get(currentPosition).currentCost > vertices.get(currentPosition/2).currentCost) ){
 			auxVertex = vertices.get(currentPosition);
 			vertices.set(currentPosition, vertices.get(currentPosition/2));
 			vertices.set(currentPosition/2, auxVertex);
			currentPosition = currentPosition/2;
 		}
 	}
 	
 	public static void main(String[] args){
 		MaxHeap hp = new MaxHeap();
 		hp.insert(1, 100);

 		hp.insert(2, 300);

 		hp.insert(3, 250);

		hp.insert(4, 1000);

		hp.insert(5, 500);
		hp.insert(6, 254);
		hp.insert(7, 15);
		for(int i = 1; i < hp.vertices.size(); i++){
			System.out.println(hp.vertices.get(i).currentCost);
		}
 
 	}
 	
 }
