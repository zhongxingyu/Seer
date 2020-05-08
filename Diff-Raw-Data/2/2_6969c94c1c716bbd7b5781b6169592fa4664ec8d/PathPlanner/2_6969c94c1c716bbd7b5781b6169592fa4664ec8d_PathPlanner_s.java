 package robot;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 
 public class PathPlanner {
 	
 	public LinkedList<Point> dijkstra(Map map)
 	{
 		LinkedList<Point> path = new LinkedList<Point>();
 		ArrayList<Point> points = map.nodes;
 		map.start.dist = 0.0;
 		path.add(map.start);		
 		
 		PriorityQueue<Point> pq = new PriorityQueue<Point>(points.size());
 		
 		for(int i = 0; i < points.size(); i++)
 		{
 			pq.add(points.get(i));
 		}
 		
 		Point current;
 		while((current = pq.poll()) != null)
 		{
 			ArrayList<Point> next = possibleNextPoints(current, map);
 			Point adj = null;
 			for(int i = 0; i < next.size(); i++)
 			{
 				adj = next.get(i);
 				if(!adj.known)
 				{
 					//wrong
 					int j = points.indexOf(current);
 					if(current.dist + map.adjacencyMatrix[j][i] < adj.dist)
 					{
 						//wrong
 						adj.dist = current.dist + map.adjacencyMatrix[j][i];
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
 		return path;		
 	}
 	
 	private ArrayList<Point> possibleNextPoints(Point current, Map map)
 	{
 		//to be implemented
 		return new ArrayList<Point>();
 	}
 	
 	public static void main(String [] args) {
 		// fast test
 		Polygon pp = new Polygon(4);
 		pp.add(new Point(-1,-1));
 		pp.add(new Point(-1,1));
 		pp.add(new Point(1,1));
 		pp.add(new Point(1,-1));
 		
 		// open the map 
		Map map = new Map(args[0]);
 		PathPlanner planner = new PathPlanner();
 		planner.dijkstra(map);
 		// grow obstacles
 		// for(Polygon p : map.polygons) {
 		//    ? = p.grow();
 		// }	
 		
 		// create visibility graph
 		
 		// use dijkstra's to get shortest path
 	} 
 }
