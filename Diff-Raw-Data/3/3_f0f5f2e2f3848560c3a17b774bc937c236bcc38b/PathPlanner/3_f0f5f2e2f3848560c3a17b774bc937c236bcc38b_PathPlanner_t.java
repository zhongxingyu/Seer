 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 
 public class PathPlanner {
 	
 	public LinkedList<Point> dijkstra(Map map)
 	{
 		LinkedList<Point> path = new LinkedList<Point>();
 		ArrayList<Point> points = map.nodes;
 		map.start.dist = 0.0;
 		// path.add(map.start);		
 		
 		PriorityQueue<Point> pq = new PriorityQueue<Point>(points.size());
 		
 		for(int i = 0; i < points.size(); i++)
 		{
 			pq.add(points.get(i));
 		}
 		
 		Point current;
 		while((current = pq.poll()) != null)
 		{
 			current.known = true;
 			ArrayList<Point> next = possibleNextPoints(current, map);
 			Point adj = null;
 			
 			// finds index of the point
 			// so dumb, but no one cares
 			int j = 0;
 			while(!points.get(j).equals(current))
 				j++;
 			for(int i = 0; i < next.size(); i++)
 			{
 				adj = next.get(i);
 				
 				// finds index of the point
 				int ind = 0;
 				while(!points.get(ind).equals(adj))
 					ind++;
 				
 				if(!adj.known)
 				{
 					if(current.dist + map.adjacencyMatrix[j][ind] < adj.dist)
 					{
 						//wrong
 						adj.dist = current.dist + map.adjacencyMatrix[j][ind];
 						adj.path = current;
 					}
 					pq.add(adj);
 				}
 			}
 		}
 		Point end = map.goal;
 		// double pathLength = end.dist;
 		while(end.path != null)
 		{
 			path.add(end);
 			end = end.path;
 		}
 		path.add(map.start);
		java.util.Collections.reverse(path);
		return path;
 	}
 	
 	private ArrayList<Point> possibleNextPoints(Point current, Map map)
 	{
 		// gets all the points, except the immediate predecessor
 		ArrayList<Point> next = new ArrayList<Point>();
 		for(int i = 0; i < map.nodes.size(); i++)
 		{
 			if(map.nodes.get(i) != current.path)
 				next.add(map.nodes.get(i));
 		}
 		return next;
 	}
 	
 	public static void main(String [] args) {
 		// open the map 
 		Map map = new Map(args[0], args[1]);
 		// Map map = new Map(args[0], new Point(-1.0,-1.0), new Point(2.0,2.0));
 		PathPlanner planner = new PathPlanner();
 		System.out.println(planner.dijkstra(map));
 	} 
 }
