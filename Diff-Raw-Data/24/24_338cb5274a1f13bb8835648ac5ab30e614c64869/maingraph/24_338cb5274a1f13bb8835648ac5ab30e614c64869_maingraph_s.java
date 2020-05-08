 import java.io.*;
 import java.net.ConnectException;
 import java.util.*;
 
 
 public class maingraph {
 
 	private static ArrayList<Vertex> graph;
 	private static ArrayList<Integer> inner = new ArrayList<Integer>();// array list of tonodes
 	
 	//arraylist of class Vertex
 	private static ArrayList<Vertex> vertices = new ArrayList<Vertex>();// array list of classes
 
 	public static ArrayList<Vertex> buildGraph() throws Exception{
 		
 		// specify input file
 		Scanner fin = new Scanner(new File("email-Enron.txt"));
 		
 		int fromCounter = 0;
 
 		while(fin.hasNextInt()){
 			
 			int v = fin.nextInt(); // from 
 			int w = fin.nextInt(); // to
 			
 			if(fromCounter == v){
 				// add another connection to our current node
 				inner.add(w);
 			}
 			else{
 				// if the node v isn't already in our adjacency list
 				// create a new ArrayList,create a new vertex and add it at that index
 				// then add the edge v,w and increment our counter
 				vertices.add(new Vertex(0, false, false, inner));
 				inner = new ArrayList<Integer>();
 				inner.add(w);
 				fromCounter++;
 			}
 						
 				
 		}vertices.add(new Vertex(0, false, false, inner));
 		
 		return vertices;
 	}
 	
 	
 	/*
 	implement a breadth-first search for the single-source shortest path problem
 	-- SJ
 	 */
 	public static void shortestpath(int u, int v) throws Exception{
 		graph = new ArrayList<Vertex>();
 		graph = buildGraph();
 		System.out.println("we on node -> " + graph.size());
 		int nextV, size;
 		
 		Vertex currVert, tempVert;
 		
 		//Queue used for visited vertices
 		Queue<Integer> q = new LinkedList<Integer>();
 		
 		//arraylist that holds the weighted sum of each path to v we find
 		ArrayList<Integer> weight = new ArrayList<Integer>();
 		
 		q.add(u);
 		
 		//if the starrt and end vertices are the same (self loop) return 0
 		if(u == v){ 
 		
 			System.out.println(0);
 			System.exit(0);
 		
 		}	
 		
 		//runs until the queue of vistied vertices is empty
 		while(q.isEmpty()== false){
 			
 			//get the first vertex in the queue
 			currVert = graph.get(q.peek());
 			//set the vertex to explored
 			currVert.setExplored(true);
 			//get the siz of the arraylist that has the tonodes of the curr vertex we're in.
 			size = currVert.getALSize();			
 			
 				//loop through each tonode of curr vertex
 				for(int i=0; i < size; i++){
 					
 					nextV = currVert.getT0Node(i);
 						
 					// if the next tonode is equal to the end vertex then increment the dist by 1
 					// and add it to final distance arraylist (weight)
 					if(nextV == v){ weight.add(currVert.getDistace() + 1); }
 					
 					else{// else visit the new vertex, set it as explored and increment the distance it took
 						
 						// get the vertex
 						tempVert = graph.get(nextV);
 						
 						// if the vertex has not been explored, visit it, add it to the queue, and increment the distance it took
 						if(tempVert.getExplored() == false){
 							q.add(nextV);
 							tempVert.setExplored(true);
 							tempVert.setDistance(currVert.getDistace() + 1);
 						}
 					}				
 				}
 				q.remove();// when we're done exploring the curr vertex dequeue it.
 		}
 			//if the weighted arraylist is empty then no path was found and return -1
 			if(weight.isEmpty()){
 				System.out.println(-1);
 				System.exit(0);
 			}
 			
 			// sort the distances in assending order
 			else{ Collections.sort(weight);	}
 			
 			System.out.println(weight.get(0));
 			System.exit(0);	
 	}
 	
 	public static void isloop() throws Exception{
 		graph = new ArrayList<Vertex>();
 		graph = buildGraph();
 		
		int numLoops=0, nextV, size; 
		Vertex currVert, tempVert;
 		Stack<Integer> s =new Stack<Integer>();
 		
		for(int index=0; index < graph.size(); index++){
			currVert = graph.get(index);
 			
			if (currVert.getALSize()!= 1) {
 				
				s.push(index);
 				
 				while(s.isEmpty()== false){
					currVert = graph.get(index);
 				}
 			}
 
 		}
 	}
 	
 	public static void connectedComponents() throws Exception{
 		graph = new ArrayList<Vertex>();
 		graph = buildGraph();
 		
 		int numConnect=0, nextV, size;
 			
 		Vertex currVert, tempVert;
 		
 		//Queue used for visited vertices
 		Queue<Integer> q = new LinkedList<Integer>();
 		
 		//arraylist that holds the weighted sum of each path to v we find
 		
 		
 		for(int index=0; index < graph.size(); index++){
 			
 			currVert = graph.get(index);
 			
 			if(currVert.getExplored() == false){
 				
 				numConnect++;
 				q.add(index);	
 				
 				//runs until the queue of vistied vertices is empty
 				while(q.isEmpty()== false){
 					
 					//get the first vertex in the queue
 					currVert = graph.get(q.peek());
 					//set the vertex to explored
 					currVert.setExplored(true);
 					//get the siz of the arraylist that has the tonodes of the curr vertex we're in.
 					size = currVert.getALSize();			
 					
 						//loop through each tonode of curr vertex
 						for(int i=0; i < size; i++){
 							
 							nextV = currVert.getT0Node(i);
 							
 							// get the vertex
 							tempVert = graph.get(nextV);
 								
 							// if the vertex has not been explored, visit it, add it to the queue, and increment the distance it took
 							if(tempVert.getExplored() == false){
 								q.add(nextV);
 								tempVert.setExplored(true);
 								tempVert.setDistance(currVert.getDistace() + 1);
 							}				
 						}
 						q.remove();// when we're done exploring the curr vertex dequeue it.
 				}
 			}
 			
 		
 		}
 		
 		
 		System.out.println("This graph has " + numConnect + " connected components." );
 	}
 	
 
 
 	public static void main(String[] args) throws Exception{
 		
 		shortestpath(2,20);
 		//connectedComponents();
 		
 	}
 
 
 
 	/*
 		What basic methods do we need for our graphs?
 		addEdge(V v, V w, E e)
 		removeEdge(E e)
 
 		addVertex(V v)
 		removeVertex(V v)
 
 		boolean hasEdge(V v, V w)
 		
 		V getOpposite(V v, E e)
 	*/
 
 	/*
 		What does the data structure for our graph look like?
 
 	*/
 
 	/*
 	algorithms to code for assignment
 		shortestpath
 		isloop
 		connectedcomponents
 		msquare
 	may or may not need any of the following
 	need more res
 		dfs
 		bfs
 		dijkstra
 		bellman ford
 		floyd-warshall
 		prims
 		kruskal
 		buruvka
 		ford-fulkerson
 	*/
 }
