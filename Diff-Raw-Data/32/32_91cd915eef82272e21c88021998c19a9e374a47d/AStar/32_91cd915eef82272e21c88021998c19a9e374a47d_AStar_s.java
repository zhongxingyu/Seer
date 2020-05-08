 package no.uib.bjo013.mspacman.map;
 
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 public class AStar {
 	
 	private static class Node implements Comparable<Node> {
 		public Point p;
 		public double g_score = 1;
 		public double h_score;
 		public double f_score = Double.MAX_VALUE;
 
 		public Node(Point p) {
 			this.p = p;
 		}
 
 		@Override
 		public int compareTo(Node o) {
 			if (this.f_score <= o.f_score) {
 				return -1;
 			} else if (this.f_score > o.f_score) {
 				return 1;
 			} else {
 				return 0;
 			}
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (o instanceof Node) {
 				return ((Node) o).p.equals(this.p);
 			} else {
 				return false;
 			}
 		}
 
 		@Override
 		public int hashCode() {
 			return this.p.hashCode();
 		}
 	}
 	
 	public static List<Point> computePath(Map<Point, List<Point>> map, Point from, Point goal, Map<Point, Double> weights) {
 		Map<Point, Point> path = new HashMap<Point, Point>();
 		Set<Point> closedSet = new HashSet<Point>();
 		PriorityQueue<Node> openSet = new PriorityQueue<Node>();
 		
 		Node start = new Node(from);
 		start.g_score = weights.containsKey(start) ? weights.get(start) : 1;
 		start.h_score = calculateHeuristics(start.p, goal);
 		start.f_score = start.g_score + start.h_score;
 		openSet.add(start);
 		
 		while(!openSet.isEmpty()) {
 			Node current = openSet.poll();
 			if(current.p.equals(goal)) {
 				LinkedList<Point> ret = new LinkedList<Point>();
 				for (Point p = current.p; !p.equals(start) && path.containsKey(p); p = path.get(p)) {
 					ret.offerFirst(p);
 				}
 				return ret;
 			}
 			
 			closedSet.add(current.p);
 			
 			List<Point> successors = map.containsKey(current.p) ? map.get(current.p) : new LinkedList<Point>();
 			for (Point succ : successors) {
 				Node neighbor = new Node(succ);
 				if (closedSet.contains(neighbor.p)) {
 					continue;
 				} else {
 					double weight = 0;
 					if (weights.containsKey(neighbor.p)) {
 						weight = weights.get(neighbor.p);
 					}
 					
 					double tentativeGScore = 
 							current.g_score 
 							+ weight
 							+ calculateHeuristics(current.p, neighbor.p);
 					boolean tentativeIsBetter = false;
 					if (!openSet.contains(neighbor)) {
 						neighbor.h_score = (calculateHeuristics(neighbor.p, goal));
 						openSet.add(neighbor);
 						tentativeIsBetter = true;
 					} else if (tentativeGScore < neighbor.g_score) {
 						tentativeIsBetter = true;
 					}
 					if (tentativeIsBetter) {
 						neighbor.g_score = tentativeGScore;
 						neighbor.f_score = neighbor.g_score + neighbor.h_score;
 						
 						path.put(neighbor.p, current.p);
 					}
 				}
 			}
 		}
 		
 		return new LinkedList<Point>();
 	}
 	
 	private static Double calculateHeuristics(Point from, Point to) {
 		if (from.x == 223 && to.x == 0) {
 			return 1.0;
 		} else if (from.x == 0 && to.x == 223) {
 			return 1.0;
 		}
 		
 		return from.distance(to);
 	}
 }
