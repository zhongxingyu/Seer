 import java.util.PriorityQueue;
 import java.util.Comparator;
 import java.util.ArrayList;
 
 public class LPAStar extends AStar { 
 
 	final static int lineOfSight = 2;
 	final static Comparator<Node> kNodeComparator = new KNodeComparator();
 	static ArrayList<Node> path = new ArrayList<Node> ();
 	private static PriorityQueue<Node> open_set = null;
 
 	public static ArrayList<Integer> calculateKey(Node s, Node goal) 
 	{
 		ArrayList<Integer> key = new ArrayList<Integer>();
 		key.add(Math.min(s.getGScore(), s.getRhsScore() + hScore(s,goal)));
 		key.add(Math.min(s.getGScore(), s.getRhsScore()));
 		return key;
 	}
 
 	public static void initialize(Grid g, Node goal)
 	{
 		open_set = new PriorityQueue<Node>(11, kNodeComparator); 
 		
 		for (Node s : g.getVision(goal, lineOfSight)) 
 		{
 			s.setGScore(1000); 
 			s.setRhsScore(1000);
 		}
 		goal.setRhsScore(0);
 		goal.setKScore(null);
 		open_set.add(goal);
 	}
 
 	public static void updateVertex(Node u, Grid g, PriorityQueue<Node> open_set, Node start, Node goal)
 	{
 		if(!u.equals(start))
 		{
 			u.setRhsScore(findRhs(u, start, g));
 		}
 
		if (open_set.contains(u))
 			open_set.remove(u);
 		
 		if (u.getGScore() != u.getRhsScore())
 		{
 			u.setKScore(calculateKey(u, goal));
 			open_set.add(u);
 		}
 	}
 
 	public static boolean keyCompare(ArrayList<Integer> one, ArrayList<Integer> two)
 	{
 		if (one.get(0) < two.get(0))
 			return true;
 		else if (one.get(0) > two.get(0))
 			return true;
 		else
 			if (one.get(1) < two.get(1))
 				return true;
 			else if (one.get(1) > two.get(1))
 				return false;
 		return false;
 	}
 	
 	public static void computeShortestPath(PriorityQueue<Node> open_set, Node goal, Grid g, Node start)
 	{
 		//System.out.println(open_set.toString());
 		while(keyCompare(calculateKey(open_set.peek(), goal), calculateKey(goal, goal)) || 
 				goal.getRhsScore() != goal.getGScore())
 		{
 			Node u = open_set.poll();
 			if ((u.getGScore() > u.getRhsScore()) || u.getGScore() < 0)
 			{
 				u.setGScore(u.getRhsScore());
 				for (Node s : u.getConnections())
 					updateVertex(s, g, open_set, start, goal);
 			}
 			else
 			{
 				u.setGScore(1000);
 				for (Node s : g.getAdjacent(u)) 
 					updateVertex(s, g, open_set, start, goal);
 				updateVertex(u, g, open_set, start, goal);
 			}
 		}	
 	}
 
 	public static Node[] algorithm(Grid g, Node goal, Node start)
 	{
 		initialize(g, goal);
 		computeShortestPath(open_set, goal, g, start);
 		return reconstructPath(goal, start, g);
 	}
 
 	public static Node[] reconstructPath(Node goal, Node start, Grid g)
 	{
 
 		if(goal.equals(start))
 		{
 			Node[] result = path.toArray(new Node[path.size()]);
 			return result;
 		}
 		else
 		{
 			PriorityQueue<Node> values = new PriorityQueue<Node>(11, kNodeComparator);
 			for (Node s : goal.getConnections())
 			{
 				s.setKScore(calculateKey(s, goal));
 				values.add(s);
 			}
 			Node closestNode = values.peek();
 			path.add(closestNode);
 			return reconstructPath(closestNode, start, g);
 		}
 	}
 
 	public static int findRhs(Node u, Node start, Grid g)
 	{		
 		if(!u.equals(start))
 		{
 			PriorityQueue<Integer> values = new PriorityQueue<Integer>(11);
 			for (Node s : u.getConnections())
 			{
 				values.add(s.getGScore() + g.getEdgeLength(s,u));
 			}
 			//System.out.print(values.toString());
 			return values.peek();			
 		}
 		return 0;
 	}
 }
