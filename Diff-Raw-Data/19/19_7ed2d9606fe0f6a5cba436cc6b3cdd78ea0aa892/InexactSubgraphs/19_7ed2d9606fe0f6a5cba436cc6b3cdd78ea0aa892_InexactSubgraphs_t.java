 package org.lilian.util.graphs.algorithms;
 
 import static org.lilian.util.Series.series;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import org.lilian.util.Pair;
 import org.lilian.util.Series;
 import org.lilian.util.graphs.BaseGraph;
 import org.lilian.util.graphs.Graph;
 import org.lilian.util.graphs.Node;
 
 public class InexactSubgraphs<L, N extends Node<L, N>>
 {
 	
 	private BaseGraph<L> graph1; 
 	private Graph<L, N> graph2;
 	 
 	private double threshold;
 	private boolean returnBest;
 	
 	private List<BaseGraph<L>.Node> nodeList1;
 	private List<N> nodeList2; 
 	
 	private InexactCost<L> costFunction;
 	
 	// * The number of links required to connect each substructure to the graph
 	public List<Integer> numLinks = new ArrayList<Integer>();
 	// * The transformation cost connected with each substructure
 	public List<Integer> transCosts = new ArrayList<Integer>();
 
 	public InexactSubgraphs(Graph<L, N> graph1, Graph<L, N> graph2,
 			InexactCost<L> cost, double threshold, boolean returnBest)
 	{
		this.graph1 = BaseGraph.copy(graph1);
 		this.graph2 = graph2;
 		
 		this.threshold = threshold;
 		this.returnBest = returnBest;
 		
 		nodeList1 = new ArrayList<BaseGraph<L>.Node>(this.graph1);
 		nodeList2 = new ArrayList<N>(graph2);
 		
 		this.costFunction = cost;
 		
 		State best = null;
 		do 
 		{
 			best = search();
 			
 			// System.out.println("Found " + best);
 
 			if(best != null)
 			{
 				// * remove the matched nodes from the graph
 				
 				// ** Sort the indices from largest to smallest so that we can 
 				//    remove them in order without affecting the rest of the
 				//    indices to be removed 
 				List<Integer> rm = new ArrayList<Integer>(best.nodes1.length);
 				for(int i : best.nodes1)
 					rm.add(i);
 				
 				Collections.sort(rm, Collections.reverseOrder());
 				
 				Set<BaseGraph<L>.Node> nodes = new HashSet<BaseGraph<L>.Node>();
 				for(int i : rm)
 					if(i >= 0)
 					{
 						BaseGraph<L>.Node node = nodeList1.remove(i);
 						nodes.add(node);
 					}
 				
 				// * record transformation cost and linking cost
 				int links = 0;
 				for(BaseGraph<L>.Node node : nodes)
 					for(BaseGraph<L>.Node neighbour : node.neighbours())
 						if(! nodes.contains(neighbour))
 							links++;
 				
 				numLinks.add(links);
 				
 				transCosts.add((int)best.cost());
 				
 				// * remove the nodes from the graph
 				for(BaseGraph<L>.Node node : nodes)
 					this.graph1.remove(node);
 			}
 //			
 //			 System.out.println(nodeList1);
 //			 System.out.println(this.graph1 + " " + this.graph1.size());
 		} while(best != null && ! this.graph1.isEmpty());
 	}
 
 	private State search()
 	{
 		State best = null;
 		double bestCost = Double.POSITIVE_INFINITY;
 		
 		PriorityQueue<State> buffer = new PriorityQueue<State>();
 		buffer.add(new State());
 
 		while(! buffer.isEmpty())
 		{
 			State top = buffer.poll();
 			
 			// * If the lowest state has cost higher than threshold, we 
 			//   will never reach a state below it.
 			if(top.cost() > threshold)
 				return best;
 			
 			// * Cost estimation is optimistic so if our current cost is worse 
 			//   than the best yet observed, it'll never get better.
 			if(top.cost() > bestCost)
 				continue;
 			
 			if(top.complete())
 			{
 				if(returnBest)
 				{
 					if(top.cost() < bestCost)
 					{
 						best = top;
 						bestCost = top.cost();
 					}
 				} else
 				{
 					return top;
 				}
 			}
 			
 			for(State child : top)
 				buffer.add(child);
 		}
 		
 		return best;
 	}
 	
 	public int numMatches()
 	{
 		return numLinks.size();
 	}
 	
 	/** 
 	 * The graph with all substructures removed
 	 * @return
 	 */
 	public BaseGraph<L> silhouette()
 	{
 		return graph1;
 	}
 	
 	/**
 	 * The number of links required to add each substructure to the graph
 	 * @return
 	 */
 	public List<Integer> numLinks()
 	{
 		return numLinks;
 	}
 	
 	/**
 	 * The number of links required to add each substructure to the graph
 	 * @return
 	 */
 	public List<Integer> transCosts()
 	{
 		return transCosts;
 	}
 	
 	private class State implements Iterable<State>, Comparable<State>
 	{
 		// * representation of the pairs
 		// - non-negative values represent mappings between nodes. negative 
 		//   values map to new nodes. 
 		private int[] nodes1;
 		private int[] nodes2;
 		
 		private double cost;
 		private boolean complete;
 		
 		/**
 		 * Creates a root state
 		 */
 		public State()
 		{
 			nodes1 = new int[0];
 			nodes2 = new int[0];
 			cost = 0.0;
 			complete = false;
 		}
 		
 		/**
 		 * Creates a child state of the given state with the given pair added.
 		 * 
 		 * @param parent
 		 * @param g1Node
 		 * @param g2Node
 		 */
 		public State(State parent, int g1Node, int g2Node, boolean complete)
 		{
 			int n = parent.size();
 			nodes1 = new int[n+1];
 			nodes2 = new int[n+1];
 			
 			System.arraycopy(parent.nodes1, 0, nodes1, 0, n);
 			System.arraycopy(parent.nodes2, 0, nodes2, 0, n);
 
 			nodes1[n] = g1Node;
 			nodes2[n] = g2Node;
 			
 			this.complete = complete;
 						
 			// * Calculate score
 			cost = expectedCost() + currentCost();
 		}
 		
 		private double expectedCost()
 		{
 			double cost = 0.0;
 			
 			// * The nodes that are currently paired
 			Set<BaseGraph<L>.Node> a1 = new HashSet<BaseGraph<L>.Node>();
 			Set<N> a2 = new HashSet<N>();
 			
 			for(int i : series(nodes1.length))
 				if(node1(i) != null)
 					a1.add(node1(i));
 			for(int i : series(nodes2.length))
 				if(node2(i) != null)
 					a2.add(node2(i));
 			
 			// * The shells around a1 and a2
 			Set<BaseGraph<L>.Node> b1 = new LinkedHashSet<BaseGraph<L>.Node>();
 			Set<N> b2 = new LinkedHashSet<N>();
 			
 			for(BaseGraph<L>.Node node : a1)
 				for(BaseGraph<L>.Node neighbour : node.neighbours())
 					if(!a1.contains(neighbour))
 						b1.add(neighbour);
 			
 			for(N node : a2)
 				for(N neighbour : node.neighbours())
 					if(!a2.contains(neighbour))
 						b2.add(neighbour);
 			if(b2.size() > b1.size())
 				cost += Math.abs(b1.size() - b2.size());
 			
 			// * NOTE: The 1983 paper is not absolutely clear on this step
 			//   this may not be right
 			int b1Links = 0, b2Links = 0;
 			for(BaseGraph<L>.Node node : b1)
 				b1Links += node.neighbours().size();
 			for(N node : b2)
 				b2Links += node.neighbours().size();
 						
 			// * We need to be optimistic so we assume that every link addition 
 			//   fixes two of the missing neighbours counted above.
 			if(b2Links > b1Links)
 				cost += Math.abs(b1Links - b2Links) / 2.0;
 			
 			return cost;
 		}
 		
 		private double currentCost()
 		{
 			// * if this is a complete match, then at least some of the pairs 
 			//   must be definite (ie. not represent the removal of addition of
 			//   a node.) 
 			if(complete())
 			{
 				int connected = 0;
 				for(int i : series(size()))
 					if(nodes1[i] >= 0 && nodes2[i] >= 0)
 						connected++;
 				
 				if(connected == 0)
 					return Double.POSITIVE_INFINITY;
 			}
 					
 			
 			double cost = 0.0;
 			
 			// * relabeling penalty
 			for(int i : Series.series(size()))
 			{
 				if(node1(i) != null && node2(i) != null)
 					if(! node1(i).label().equals(node2(i).label()) )
 						cost += costFunction.relabel(
 							node1(i).label(), 
 							node2(i).label());
 			}
 			
 			// * Node addition penalty
 			for(int i : series(size()))
 				if(node1(i) == null)
 					cost += costFunction.addNode(node2(i).label());
 
 			// * Node deletion penalty
 			for(int i : series(size()))
 				if(node2(i) == null)
 					cost += costFunction.addNode(node1(i).label());
 			
 			// * Link penalties
 			for(int i : series(size()))
 				for(int j : series(i+1, size()))
 				{
 					BaseGraph<L>.Node n1i = node1(i), n1j = node1(j); 
 					 N n2i = node2(i), n2j = node2(j);
 					
 					boolean n1connected;
 					if(n1i == null || n1j == null)
 						n1connected = false;
 					else
 						n1connected = n1i.connected(n1j);
 					
 					boolean n2connected;
 					if(n2i == null || n2j == null)
 						n2connected = false;
 					else
 						n2connected = n2i.connected(n2j); 
 										
 					if(n1connected && !n2connected)
 						cost += costFunction.addLink();
 					
 					if(!n1connected && n2connected)
 						cost += costFunction.removeLink();
 
 				}
 			
 			// Maybe this can be optimized by iterating over edges (once
 			// we have that implemented).
 			
 			return cost;
 		}
 		
 		public BaseGraph<L>.Node node1(int i)
 		{
 			if(nodes1[i] < 0)
 				return null;
 				
 			return nodeList1.get(nodes1[i]);
 		}
 		
 		public N node2(int i)
 		{
 			if(nodes2[i] < 0)
 				return null;
 			return nodeList2.get(nodes2[i]);
 		}
 		
 		@Override
 		public Iterator<State> iterator()
 		{
 			return new StateIterator();
 		}
 		
 		private class StateIterator implements Iterator<State>
 		{
 			// * Lists of the nodes that have not yet been pairs
 			private List<Integer> remaining1, remaining2;
 			// * indices of the nodes to add to the next state to return.
 			private int i, j;
 			
 			public StateIterator()
 			{
 				remaining1 = new ArrayList<Integer>(graph1.size() + 1);
 				remaining2 = new ArrayList<Integer>(graph2.size() + 1);
 				
 				remaining1.addAll(Series.series(graph1.size()));
 				remaining2.addAll(Series.series(graph2.size()));
 
 				remaining1.add(-1);
 				remaining2.add(-1);
 				
 				for(int n1 : nodes1)
 					if(n1 >= 0)
 						remaining1.remove((Integer)n1);
 				for(int n2 : nodes2)
 					if(n2 >= 0)
 						remaining2.remove((Integer)n2);
 				
 //				System.out.println(State.this + " remaining: " + remaining1 + " " + remaining2);
 				
 				i = 0;
 				j = 0;
 			}
 			
 			@Override
 			public boolean hasNext()
 			{
 				if(i >= remaining1.size() - 1 && j >= remaining2.size() - 1)
 					return false;
 				
 				return true;
 			}
 
 			@Override
 			public State next()
 			{
 				if(! hasNext())
 					throw new NoSuchElementException();
 				
 				int n1 = remaining1.get(i),
 				    n2 = remaining2.get(j);
 				
 				boolean complete2 = (n2 < 0 && remaining2.size() == 1) || (n2 >= 0 && remaining2.size() <= 2 );				
 	
 				State result = new State(State.this, n1, n2, complete2);
 				
 				j++;
 				if(j > remaining2.size() - 1)
 				{
 					j = 0;
 					i ++;
 				}
 				
 				return result;
 			}
 
 			@Override
 			public void remove()
 			{
 				throw new UnsupportedOperationException();
 			}
 		}
 
 		public double cost()
 		{
 			return cost;
 		}
 		
 		/**
 		 * Whether this state represents a complete mapping from g1 nodes to g2 
 		 * nodes.
 		 * @return
 		 */
 		public boolean complete()
 		{
 			return complete;
 		}
 		
 		/**
 		 * Number of pairs (not the number of elements returned by iteration).
 		 * @return
 		 */
 		public int size()
 		{
 			return nodes1.length;
 		}
 
 		@Override
 		public int compareTo(State other)
 		{
 			return Double.compare(this.cost, other.cost);
 		}
 		
 		public String toString()
 		{
 			StringBuffer sb = new StringBuffer();
 			
 			sb.append("[");
 			boolean first = true;
 			for(int i : Series.series(size()))
 			{
 				if(first)
 					first = false;
 				else
 					sb.append(", ");
 					
 				sb.append(
 					(nodes1[i] == -1 ? "λ" : nodeList1.get(nodes1[i]))
 					+ "-" + 
 					(nodes2[i] == -1 ? "λ" :nodeList2.get(nodes2[i]))
 				);
 			}
 			
 			sb.append("] ").append(cost() + " ("+expectedCost()+", "+currentCost()+") ").append(complete() ? "c" : "i");
 				
 			return sb.toString();
 		}
 		
 	}
 }
