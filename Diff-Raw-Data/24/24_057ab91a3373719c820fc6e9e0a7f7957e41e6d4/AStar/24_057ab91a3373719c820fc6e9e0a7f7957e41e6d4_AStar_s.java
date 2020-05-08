 package org.misera.android.cavenav.graph;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.util.Log;
 
 public class AStar {
 	
 	private Graph graph;
 
 
 	public AStar(Graph graph) {
 		this.graph = graph;
 	}
 
 	public ArrayList<Edge> getRoute(Vertex start, Vertex goal) {
 		Log.d("CaveNav", "Calculating route...");
 		ArrayList<Vertex> closedSet = new ArrayList<Vertex>();
 		ArrayList<Vertex> openSet = new ArrayList<Vertex>();
 		start.g = 0;
 		start.h = getDistance(start, goal);
 		start.f = start.g + start.h;
 		openSet.add(start);
 		while (openSet.size() > 0) {
 			Collections.sort(openSet, new VertexComparator());
 			Vertex current = openSet.get(0);
 			if (current == goal) {
 				Log.d("CaveNav", "A route was found!");
 				return reconstructPath(start, goal);
 			}
 			openSet.remove(current);
 			closedSet.add(current);
 			for (Vertex neighbor : current.getNeighbors()) {
				neighbor.h = getDistance(goal, neighbor);
				neighbor.g = current.g + getDistance(current, neighbor);
				neighbor.f = neighbor.g + neighbor.h;
 				if (closedSet.contains(neighbor)) {
 					continue;
 				}
 				double tentativeG = current.g + getDistance(current, neighbor);
 				boolean tentativeIsBetter;
 				if (!openSet.contains(neighbor)) {
 					openSet.add(neighbor);
 					neighbor.h = getDistance(neighbor, goal);
 					tentativeIsBetter = true;
 				} else if (tentativeG < neighbor.g) {
 					tentativeIsBetter = true;
 				} else {
 					tentativeIsBetter = false;
 				}
 				
 				if (tentativeIsBetter) {
 					neighbor.prevOnRoute = current;
 					neighbor.g = tentativeG;
 					neighbor.f = neighbor.g + neighbor.h;
 				}
 				
 			}
 		}
 		
 		return null;
 	}
 	
 	private ArrayList<Edge> reconstructPath(Vertex start, Vertex goal) {
 		Log.d("CaveNav", "Reconstructing path...");
 		ArrayList<Edge> out = new ArrayList<Edge>();
 		Vertex first = goal;
 		Vertex prev = goal.prevOnRoute;
 		while (prev != null) {
 			out.add(graph.findEdge(first, prev));
 			first = prev;
 			prev = prev.prevOnRoute;
 		}
 		Log.d("CaveNav", "Path reconstructed.");
 		return out;
 	}
 
 	
 	private double getDistance(Vertex v1, Vertex v2) {
 		int a = v1.x - v2.x;
 		int b = v1.y - v2.y;
 		return Math.sqrt(a*a + b*b);
 	}
 	
 	
 }
