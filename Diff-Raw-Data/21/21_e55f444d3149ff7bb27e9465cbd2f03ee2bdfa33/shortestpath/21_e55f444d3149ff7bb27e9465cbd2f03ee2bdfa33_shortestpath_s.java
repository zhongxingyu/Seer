 import java.util.*;
 
 public class shortestpath{
 
 	private static ArrayList<Vertex> g;
 	
 	/*
 	implement a breadth-first search for the single-source shortest path problem
 	-- SJ
 	*/
 	
 	public static void main(String[] args) throws Exception{
 		g = new ArrayList<Vertex>();
 		g = graph.buildGraph();
 		
 		int nextV, size;
 		int u = Integer.parseInt(args[0]);
 		int v = Integer.parseInt(args[1]);
 
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
 			currVert = g.get(q.peek());
 			//set the vertex to explored
 			currVert.setExplored(true);
 			//get the siz of the arraylist that has the tonodes of the curr vertex we're in.
 			size = currVert.getALSize();			
 			
 				//loop through each tonode of curr vertex
 				for(int i=0; i < size; i++){
 					
 					nextV = currVert.getToNode(i);
 						
 					// if the next tonode is equal to the end vertex then increment the dist by 1
 					// and add it to final distance arraylist (weight)
 					if(nextV == v){ weight.add(currVert.getDistace() + 1); }
 					
 					else{// else visit the new vertex, set it as explored and increment the distance it took
 						
 						// get the vertex
 						tempVert = g.get(nextV);
 						
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
 }
