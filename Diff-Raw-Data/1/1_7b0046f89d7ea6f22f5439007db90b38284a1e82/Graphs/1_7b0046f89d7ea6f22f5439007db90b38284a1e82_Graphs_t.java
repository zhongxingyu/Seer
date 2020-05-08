 import java.awt.geom.Point2D;
 import java.util.*;
 
 public class Graphs {
 	public static int[][] FloydWarshall(int[][] c){
 		int n = c.length;
 		int[][] d = new int[n][n];
 		for(int i=0;i<n;i++){
 			d[i] = Arrays.copyOf(c[i], c[i].length);
 		}
 		for (int k = 0; k < n; k++ )
 		for (int i = 0; i < n; i++ )
 		for (int j = 0; j < n; j++ )
 			d[i][j] = Math.min( d[i][k] + d[k][j], d[i][j] );
 		return d;
 	}
 	
 	public static void dijkstra(Graph g, Vertex s){
 		for(Vertex v: g.Vertices){
 			v.dis = Integer.MAX_VALUE;
 			v.prev = null;
 		} 
 		s.dis = 0;// Distance from source to source
 		
 		PriorityQueue<Vertex> q = new PriorityQueue<Vertex>(g.order(),new sortBydis());
		q.add(s);
 		while(!q.isEmpty()){
 			Vertex u = q.poll();
 			if(u.dis==Integer.MAX_VALUE){
 				break;
 			}
 			for(Edge e: u.toedge){
 				int alt = u.dis+e.weight;
 				Vertex v = e.to;
 				if(alt<v.dis){
 					v.dis = alt;
 					v.prev = u;
 					q.remove(v);
 					q.add(v);
 				}
 			}
 		}
 	}
 }
 
 class Graph{
 	ArrayList<Vertex> Vertices;
 	int[][] EdgeDistance;
 //	ArrayList<Edge> Edges;
 	Graph(){
 		Vertices = new ArrayList<Vertex>();
 	}
 	int order(){
 		return Vertices.size();
 	}
 }
 class Edge{
 	Vertex from;
 	Vertex to;
 	int weight;
 }
 class Vertex{
 	ArrayList<Edge> fromedge;
 	ArrayList<Edge> toedge;
 	int id;
 	int dis;//useful if record distance
 	Vertex prev;//useful if we want to trace back for dp
 }
 
 class sortBydis implements Comparator<Vertex>{
     public int compare(Vertex v, Vertex u) {
     	if(v.dis<u.dis){
     		return -1;
     	}
     	if(v.dis>u.dis){
     		return 1;
     	}
     	return 0;
 	}
 }
