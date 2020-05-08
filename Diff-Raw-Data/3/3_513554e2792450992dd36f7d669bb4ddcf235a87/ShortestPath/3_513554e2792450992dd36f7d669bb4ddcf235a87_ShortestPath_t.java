 import java.util.*;
 import RunningCircuit325.model.Vertex;
 import RunningCircuit325.model.Graph;
 import RunningCircuit325.model.Edge;
 
 public class ShortestPath{
 		public LinkedList<Edge> edges = new LinkedList<Edge>
 		public LinkedList<Vertex> vertices = new LinkedList<Vertex>;
 		public LinkedList<Edge> adjacentEdge = new LinkedList<Edge>;
 		public Vertex at;
 		public Vertex too;
 		
 	public List<Vertex> ShortestPath(Vertex a, Vertex b, Graph graph){
 		LinkedList<Edge> edges = graph.edges;
 		LinkedList<Vertex> vertices = graph.vertices;
 		at=a;
 		too=b;
 		for(int i=0;i<vertices.size;i++){
 			Vertex temp =vertices.get(i);
 			temp.setValue=null;
 		}
 		scan(a,b);
 	}
 	
 	public getAdjacent(Vertex a){
 		for(int i=0;i<edges.size();i++){
 			if(edges.get(i).getVertex1()=a && !edges.get(i).scanned){
 				adjacentEdge.add(Edge edges.get(i));
 			}
 		}
 	}
 
 	public scan(Vertex a, Vertex b){
 		getAdjacent(at);
 		for(int i=0;i<adjacentEdge.size();i++){
			if(adjacentEdge.get(i).getVertex2().getvalue()<(adjacentEdge.get(i).getVertex2().getvalue() + adjacentEdge.get(i).getWeight()))
				adjacentEdge.get(i).getVertex2().setValue(adjacentEdge.get(i).getVertex2().getvalue() + adjacentEdge.get(i).getWeight());
 		}
 		/*
 		*scan for smallest value
 		*set vertex boolean to scanned
 		*save taken route to list(maybe have list set @ the vertex, idk ideas)
 		*scan to the next thing
 		*/
 	}
