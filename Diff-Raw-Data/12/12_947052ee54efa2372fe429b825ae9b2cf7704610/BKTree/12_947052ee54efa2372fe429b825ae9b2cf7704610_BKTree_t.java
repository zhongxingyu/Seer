 package iitm.apl.bktree;
 
 import java.util.HashMap;
 
 public class BKTree <T> 
 {
 	private HashMap<T, Integer> matches;
 	private Node root;
 	private LevenshteinDistance distance;
 	private T bTerm;
 	
 	public class Node
 	{
 		T term;
 		HashMap<Integer, Node> children;
 		
 		public Node(T term)
 		{
 			this.term = term;
 			children = new HashMap<Integer, Node>();
 		}
 		public void add(T term)
 		{
 			int value = distance.getLD(term, this.term);
 			
 			Node child = children.get(value);
 			if( child == null ) children.put(value, new Node(term));
 			else child.add(term);
 		}
 		public int bestMatch(T term, int bDist)
 		{
 			int distAtNode = distance.getLD(term, this.term);
 			if(distAtNode < bDist) 
 			{
 				bDist = distAtNode;
 				bTerm = this.term;
 			}
 			int distIter = bDist;
 			for(Integer value : children.keySet())
 			{
 				if(value < distAtNode + bDist)
 				{
 					distIter = children.get(value).bestMatch(term, bDist);
 					bDist = Math.min(bDist, distIter);
 				}
 			}
 			return bDist;
 		}
 		public void makeQuery(T term, int lbound, HashMap<T, Integer> hmap)
 		{
 			int distAtNode = distance.getLD(term, this.term);
			//if(distAtNode == lbound)
			//{
			//	hmap.put(this.term, distAtNode);
			//	return;
			//}
			if(distAtNode <= lbound)
 			{
 				hmap.put(this.term, distAtNode);
 			}
 			for( int i = distAtNode - lbound; i <= distAtNode + lbound; i++ )
 			{
 				Node child = children.get(i);
 				if(child != null) child.makeQuery(term, lbound, hmap);
 			}
 			
 		}
 		public T getbTerm()
 		{
 			return bTerm;
 		}
 	}
 	
 	public BKTree(LevenshteinDistance distance)
 	{
 		root = null;
 		this.distance = distance;
 	}
 	public void add	(T term)
 	{
 		if(root != null) root.add(term);
 		else root = new Node(term);
 	}
 	public HashMap<T, Integer> makeQuery(T key, int lbound)
 	{
 		matches = new HashMap<T, Integer>();		
 		root.makeQuery(key, lbound, matches);
 		return matches;
 	}
 	public int find(T term)
 	{
 		return root.bestMatch(term, Integer.MAX_VALUE);
 	}
 	public T bestWordMatch(T term)
 	{
 		root.bestMatch(term, Integer.MAX_VALUE);
 		return root.getbTerm();
 	}
 	public HashMap<T,Integer> bestWordMatchWithDistance(T term)
 	{
 		int dist = root.bestMatch(term, Integer.MAX_VALUE);
 		HashMap<T, Integer> rMap = new HashMap<T, Integer>();
 		rMap.put(root.getbTerm(), dist);
 		return rMap;
 	}
 	
 }
