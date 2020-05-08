 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class PathPlanner {
 	
 	private MapComponent comp;
 	
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
 			ArrayList<Point> next = PathPlanner.possibleNextPoints(current, map);
 			Point adj = null;
 			
 			int j = points.indexOf(current);
 			for(int i = 0; i < next.size(); i++)
 			{
 				adj = next.get(i);
 				
 				// finds index of the point
 				int ind = points.indexOf(adj); //built-in
 				
 				if(!adj.known)
 				{
 					if(current.dist + map.adjacencyMatrix[j][ind] < adj.dist)
 					{
 						adj.dist = current.dist + map.adjacencyMatrix[j][ind];
 						adj.path = current;
 						// comp.drawPossiblePaths(adj, )
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
 	
 	public static ArrayList<Point> possibleNextPoints(Point current, Map map)
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
 	
 	public static void writeOutPath(String path, LinkedList<Point> rendezvousPoints)
 	{
 		/* first, translate everything so the origin is at the start point */
 		LinkedList<Point> copy = new LinkedList<Point>();
 		Point begin = rendezvousPoints.get(0);
 		for(Point p : rendezvousPoints)
 		{
 			Point translated = p.translate(begin.mult(-1.0));
 			copy.add(translated);
 		}
 		/* and then we write out the file */
 		try {
 			FileWriter fstream = new FileWriter(path);
 			BufferedWriter out = new BufferedWriter(fstream);
 			for(Point p : copy)
 			{
 				out.write(p.x + " " + p.y +"\n");	
 			}
 			out.close();
 		} catch (IOException e){
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 	
 	public void createGUI(Map robotMap, LinkedList<Point> path)
 	{
 		JFrame frame = new JFrame();
 		comp = new MapComponent(robotMap);
 		frame.add(comp);
 		comp.setPath(path);
 		
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 	}
 	
 	public static void main(String [] args) {
 		// open the map 
 		Map map = new Map(args[0], args[1]);
 		// Map map = new Map(args[0], new Point(-1.0,-1.0), new Point(2.0,2.0));
 		PathPlanner planner = new PathPlanner();
 		LinkedList<Point> path = planner.dijkstra(map);
 		planner.createGUI(map, path);
 		System.out.println(path);
 		writeOutPath("path.txt", path);
 	} 
 }
