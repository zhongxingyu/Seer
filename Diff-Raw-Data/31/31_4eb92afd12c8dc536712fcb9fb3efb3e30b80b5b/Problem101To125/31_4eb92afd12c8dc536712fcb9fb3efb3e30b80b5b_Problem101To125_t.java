 /**
  * 
  */
 package com.ssparrow.projecteuler;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * @author Gao, Fei
  *
  */
 public class Problem101To125 {
 	/**
 	 * The following undirected network consists of seven vertices and twelve edges with a total weight of 243.
 
 
 The same network can be represented by the matrix below.
 
     	A	B	C	D	E	F	G
 A	-	16	12	21	-	-	-
 B	16	-	-	17	20	-	-
 C	12	-	-	28	-	31	-
 D	21	17	28	-	18	19	23
 E	-	20	-	18	-	-	11
 F	-	-	31	19	-	-	27
 G	-	-	-	23	11	27	-
 However, it is possible to optimise the network by removing some edges and still ensure that all points on the network remain connected. 
 The network which achieves the maximum saving is shown below. It has a weight of 93, representing a saving of 243  93 = 150 from the original network.
 
 
 Using network.txt (right click and 'Save Link/Target As...'), a 6K text file containing a network with forty vertices, and given in matrix form, 
 find the maximum saving which can be achieved by removing redundant edges whilst ensuring that the network remains connected.
 	 * @param matrix
 	 * @return
 	 */
 	public static int p107GetMaximumSaving(int [][] matrix){
 		int total=0;
 		for(int i=0;i<matrix.length;i++){
 			for(int j=i+1;j<matrix[i].length;j++){
 				total+=matrix[i][j];
 			}
 		}
 		
 		Graph graph=new Graph();
 		Map<Vertex, Status> statusMap=new HashMap<Vertex, Status>();
 		for(int i=0;i<matrix.length;i++){
 			String id = String.valueOf(i);
 			
 			Vertex vertex=graph.getVertex(id);
 			if(vertex==null){
 				vertex=new Vertex(id);
 				graph.addVertex(vertex);
 				statusMap.put(vertex, Status.UNVISITED);
 			}
 			
 			for(int j=i+1;j<matrix[i].length;j++){
 				if(matrix[i][j]>0){
 					String targetId=String.valueOf(j);
 					
 					Vertex target=graph.getVertex(targetId);
 					if(target==null){
 						target=new Vertex(targetId);
 						graph.addVertex(target);
 						statusMap.put(target, Status.UNVISITED);
 					}
 					
 					vertex.addEdge(target, matrix[i][j]);
 				}
 			}
 		}
 		
 		List<List<Edge>> allCircles = new ArrayList<List<Edge>>();
 		
		//TODO Optimization needed, currently duplicate circle will be considered
		for(Vertex vertex:graph.vertexes){
			ArrayList<Vertex> visitedVertexes = new ArrayList<Vertex>();
			visitedVertexes.add(vertex);
			ArrayList<Edge> path = new ArrayList<Edge>();
			getAllCirclesInGraph(allCircles, vertex, visitedVertexes,path);
		}
 		
 		int saving=0;
 		while(!allCircles.isEmpty()){
 			List<Edge> firstCircle = allCircles.get(0);
 			
 			Edge maxEdge=null;
 			int maxWeight=Integer.MIN_VALUE;
 			for(Edge edge:firstCircle){
 				if(edge.getWeight()>maxWeight){
 					maxEdge=edge;
 					maxWeight=maxEdge.getWeight();
 				}
 			}
 			
 			saving+=maxWeight;
 			
 			List<List<Edge>> circlesToBeRemoved=new ArrayList<List<Edge>>();
 			for(List<Edge> circle:allCircles){
 				if(circle.contains(maxEdge)){
 					circlesToBeRemoved.add(circle);
 				}
 			}
 			
 			allCircles.removeAll(circlesToBeRemoved);
 			
 		}
 		
 		return saving;
 	}
 		
 	private static void getAllCirclesInGraph(List<List<Edge>> result,Vertex vertex, List<Vertex> visitedVertexes, List<Edge> path){
 		for(Vertex adjacent:vertex.getAdjacentVertexes()){
			Edge edge = vertex.getEdge(adjacent);
			
 			if(visitedVertexes.size()>2 && visitedVertexes.get(0).equals(adjacent)){
 				List<Edge> circle=new ArrayList<Edge>(path);
				circle.add(edge);
 				result.add(circle);
 			}else if(!visitedVertexes.contains(adjacent)){
				path.add(edge);
				visitedVertexes.add(vertex);
 				getAllCirclesInGraph(result, adjacent, visitedVertexes, path);
				visitedVertexes.remove(vertex);
				path.remove(edge);
 			}
 		}
 		
 		visitedVertexes.remove(vertex);
 	}
 }
 
 enum Status{
 	UNVISITED,VISITED;
 }
 
 /**
  * @author Gao, Fei
  *
  */
 class Graph {
 	List<Vertex> vertexes=new ArrayList<Vertex>();
 	Map<String, Vertex> vertexMap=new HashMap<String, Vertex>();
 
 	/**
 	 * @return the vertexes
 	 */
 	public List<Vertex> getVertexes() {
 		return vertexes;
 	}
 	
 	public void addVertex(Vertex vertex){
 		vertexes.add(vertex);
 		vertexMap.put(vertex.getId(), vertex);
 	}
 	
 	public Vertex getVertex(String id) {
 		return vertexMap.get(id);
 	}
 }
 
 /**
  * @author Gao, Fei
  *
  */
 class Vertex{
 	private String id;
 	
 	private List<Edge> edges=new ArrayList<Edge>();
 	private Map<Vertex, Edge> vertexMap=new LinkedHashMap<Vertex, Edge>();
 	
 	/**
 	 * @param id
 	 */
 	public Vertex(String id) {
 		this.id = id;
 	}
 	
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	public void addEdge(Vertex target, int weight){
 		Edge edge=new Edge(this, target);
 		edge.setWeight(weight);
 		
 		edges.add(edge);
 		vertexMap.put(target, edge);
 		
 		target.addEdge(edge, this);
 	}
 	
 	public void addEdge(Edge edge, Vertex source){
 		edges.add(edge);
 		vertexMap.put(source, edge);
 	}
 	
 	public Edge getEdge(Vertex vertex){
 		return vertexMap.get(vertex);
 	}
 
 	/**
 	 * @return the edges
 	 */
 	public Set<Vertex> getAdjacentVertexes() {
 		return vertexMap.keySet();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Vertex other = (Vertex) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return id;
 	}
 	
 }
 
 /**
  * @author Gao, Fei
  *
  */
 class Edge{
 	private Vertex source;
 	private Vertex target;
 	private int weight;
 	/**
 	 * @param source
 	 * @param target
 	 */
 	public Edge(Vertex source, Vertex target) {
 		this.source = source;
 		this.target = target;
 	}
 	/**
 	 * @return the source
 	 */
 	public Vertex getSource() {
 		return source;
 	}
 	/**
 	 * @param source the source to set
 	 */
 	public void setSource(Vertex source) {
 		this.source = source;
 	}
 	/**
 	 * @return the target
 	 */
 	public Vertex getTarget() {
 		return target;
 	}
 	/**
 	 * @param target the target to set
 	 */
 	public void setTarget(Vertex target) {
 		this.target = target;
 	}
 	/**
 	 * @return the weight
 	 */
 	public int getWeight() {
 		return weight;
 	}
 	/**
 	 * @param weight the weight to set
 	 */
 	public void setWeight(int weight) {
 		this.weight = weight;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((source == null) ? 0 : source.hashCode());
 		result = prime * result + ((target == null) ? 0 : target.hashCode());
 		return result;
 	}
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Edge other = (Edge) obj;
 		if (source == null) {
 			if (other.source != null)
 				return false;
 		} else if (!source.equals(other.source))
 			return false;
 		if (target == null) {
 			if (other.target != null)
 				return false;
 		} else if (!target.equals(other.target))
 			return false;
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Edge [" + source + ", " + target + ", "+ weight + "]";
 	}
 	
 	
 }
