 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 
 public class DStarLite extends LPAstar{
 
 	private static int k;
 	private static PriorityQueue<Node> open_set;
 	private static Node start;
 	private static Node goal;
 	private static Grid g;
 	
 	public static ArrayList<Integer> calculateKey(Node s) 
 	{
 		ArrayList<Integer> key = new ArrayList<Integer>();
 		key.add(Math.min(s.getGScore(), s.getRhsScore()) + hScore(start,s) + k);
 		key.add(Math.min(s.getGScore(), s.getRhsScore()));
 		return key;
 	}
 	
 	public static void initialize(Grid gridInput, Node gInput, Node sInput)
 	{
 		g = gridInput;
 		start = sInput;
 		goal = gInput;
 		k = 0;
 		g.setPos(start);
 		
 		open_set = new PriorityQueue<Node>(11, kNodeComparator);
 		goal.setKScore(calculateKey(goal));
 		goal.setRhsScore(0);
 		open_set.add(goal);
 	}
 	
 	public static void updateVertex(Node u)
 	{
 		if (!u.equals(goal)){
 			Node temp = minimize(u.getConnections());
 			u.setRhsScore(g.getEdgeLength(temp, u) + temp.getGScore()); 
 		}
 
 		if (open_set.contains(u))
 			open_set.remove(u);
 		
 		if (u.getGScore() != u.getRhsScore())
 		{
 			u.setKScore(calculateKey(u));
 			if(!(open_set.contains(u)))
 				open_set.add(u);
 		}
 	}
 	
 	public static void computeShortestPath()
 	{
 		while(keyCompare(calculateKey(open_set.peek()), calculateKey(goal)) || start.getRhsScore() != start.getGScore())
 		{
 			ArrayList<Integer> kOld = open_set.peek().getKScore();
 			Node u = open_set.poll();
 			
 			if(keyCompare(kOld, calculateKey(u)))
 			{
 				u.setKScore(calculateKey(u));
 				open_set.add(u);
 			}
 			else if ((u.getGScore() > u.getRhsScore()))
 			{
 				u.setGScore(u.getRhsScore());
 				for (Node s : u.getConnections())
 					updateVertex(s);
 			}
 			else
 			{
 				u.setGScore(10000);
 				for (Node s : u.getConnections())
 					updateVertex(s);
 				updateVertex(u);
 			}
 		}	
 	}
 	
 	public static Node[] algorithm(Grid gInput, Node goalInput, Node startInput)
 	{
 		Node last = startInput;		
 		initialize(gInput, goalInput, startInput);
 		computeShortestPath();
		int counter = 0;
 		while(!start.equals(goal))
 		{
			System.out.println(counter++);
 			//if (start.getGScore() == 10000) 
			//	return null;
 			start = minimize(start.getConnections());
 			//g.setPos(start);
 			
			for(Node n: g.getVision(start, 2)){
 				Edge[] changedEdges = n.getNewEdges();
 				if(changedEdges != null){
 					k = k + hScore(last, start);  
 					last = start;
 					for (Edge e : changedEdges)
 					{
 						Node begin = e.getBegin();
 						updateVertex(begin);
 						//Node end = e.getEnd();
 						//updateVertex(end);
 					}
 				}
 				computeShortestPath();
 			}
 		}
 		return reconstructPath(startInput, goal);
 	}
 	/* 
 	 * 	km = km + h(slast, sstart)
 	 * 	slast = sstart
 	 * 	for all directed edges (u, v) with changed edge costs
 	 * 		update the edge cost c(u,v)
 	 * 		update vertex (u)
 	 * 
 	 * 	compute shortestpath()
 	 * changedEdges contains all of the Edges that were presumed to exist (when the Node's not visible),
 	 * but actually do not exist.
 	 * Do the rest of the stuff here.
 	 * Also note getVision now returns only the nodes that are newly visible
 	 * (i.e. does not return Nodes around the current position that were already visible)
 	 */
 	
 	private static Node minimize (Node[] nodeList)
 	{
 		Node min = nodeList[0];
 		
 		for(Node s : nodeList)
 		{
 			if (g.getEdgeLength(s, start) + s.getGScore() < g.getEdgeLength(min, start) + min.getGScore())
 				min = s;
 		}
 		return min;
 	}
 }
