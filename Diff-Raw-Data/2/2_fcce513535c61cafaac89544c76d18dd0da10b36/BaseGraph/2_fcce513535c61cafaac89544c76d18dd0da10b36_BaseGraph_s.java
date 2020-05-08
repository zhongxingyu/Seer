 package org.lilian.util.graphs;
 
 import java.util.AbstractCollection;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.lilian.util.Pair;
 import org.lilian.util.Series;
 import org.lilian.util.graphs.algorithms.UndirectedVF2;
 
 /**
  * A basic implementation of the {@link Graph} interface
  * 
  * 
  * OPEN QUESTIONS:
  * - Should equals be based on object instance or on isomorphism? I think instance
  * 
  * @author peter
  *
  * @param <L>
  */
 public class BaseGraph<L> extends AbstractCollection<BaseGraph<L>.Node> 
 	implements Graph<L, BaseGraph<L>.Node>
 {
 	protected Map<L, Set<Node>> nodes = new LinkedHashMap<L, Set<Node>>();
 	protected int size = 0;
 	protected int numEdges = 0;
 	protected long modCount = 0;
 	
	public BaseGraph()
 	{
 	}
 	
 	/**
 	 * Returns a graph with the same structure and labels as that in the 
 	 * argument.
 	 * 
 	 * @param graph
 	 * @return
 	 */
 	public <N extends org.lilian.util.graphs.Node<L, N>> BaseGraph(Graph<L, N> graph)
 	{	
 		List<N> nodes = new ArrayList<N>(graph);
 		List<BaseGraph<L>.Node> outNodes = new ArrayList<BaseGraph<L>.Node>(graph.size());
 		
 		for(int i : Series.series(graph.size()))
 		{
 			BaseGraph<L>.Node newNode = addNode(nodes.get(i).label());
 			outNodes.add(newNode);
 		}
 		
 		for(int i : Series.series(graph.size()))
 			for(int j : Series.series(i, graph.size()))
 				if(nodes.get(i).connected(nodes.get(j)))
 					outNodes.get(i).connect(outNodes.get(j));
 	}
 	
 	public class Node implements org.lilian.util.graphs.Node<L, Node>
 	{
 		private Set<Node> neighbours = new LinkedHashSet<Node>();
 		private L label;
 		
 		// A node is dead when it is removed from the graph. Since there is no way 
 		// to ensure that clients don't maintain copies of node objects we 
 		// keep check of nodes that are no longer part of the graph. 
 		private boolean dead = false;
 		
 		private Integer labelId = null;
 		private Long labelIdMod;
 
 		public Node(L label)
 		{
 			this.label = label;
 		}
 
 		@Override
 		public Set<Node> neighbours()
 		{
 			return Collections.unmodifiableSet(neighbours);
 		}
 
 		@Override
 		public Node neighbour(L label)
 		{
 			for(Node node : neighbours)
 				if(node.equals(label))
 					return node;
 			
 			return null;			 
 		}
 
 		@Override
 		public L label()
 		{
 			return label;
 		}
 
 		@Override
 		public Set<Node> neighbours(L label)
 		{
 			Set<Node> result = new HashSet<Node>();
 			for(Node node: neighbours)
 				if(node.equals(label))
 					result.add(node);
 			
 			return result;
 			
 		}
 
 		@Override
 		public void connect(Node other)
 		{
 			if(this.graph().hashCode() != other.graph().hashCode())
 				throw new IllegalArgumentException("Can only connect nodes in the same graph.");
 			
 			if(connected(other))
 				return;
 			
 			neighbours.add(other);
 			other.neighbours.add(this);
 			
 			numEdges++;
 			modCount++;
 		}
 
 		@Override
 		public void disconnect(Node other)
 		{
 			if(!connected(other))
 				return;
 			
 			neighbours.remove(other);
 			other.neighbours.remove(this);
 			
 			numEdges--;
 			modCount++;	
 		}
 
 		@Override
 		public boolean connected(Node other)
 		{
 			return neighbours.contains(other);
 		}
 
 		@Override
 		public Graph<L, Node> graph()
 		{
 			return BaseGraph.this;
 		}
 		
 		public int id()
 		{
 			return ((Object) this).hashCode();
 		}
 		
 		/** 
 		 * An id to identify this node among nodes with the same label.
 		 * @return
 		 */
 		public int labelId()
 		{
 			if(labelIdMod == null || labelIdMod != modCount)
 			{
 				Set<Node> others = nodes.get(label);
 				
 				int i = 0;
 				for(Node other : others)
 				{
 					if(other.equals(this))
 					{
 						labelId = i;
 						break;
 					}
 					i++;
 				}
 				labelIdMod = modCount;
 			}	
 			return labelId;
 		
 		}
 		
 		public String toString()
 		{
 			boolean unique = nodes.get(label).size() <= 1;
 
 			return label + (unique ? "" : "_" + labelId());
 		}
 		
 		/**
 		 * Since clients can maintain links to nodes that have been removed 
 		 * from the graph, there is a danger of these nodes being used and 
 		 * causing mayhem. 
 		 * 
 		 * To prevent such situations we will explicitly give such nodes a state 
 		 * of 'dead'. Using dead nodes in any way (except calling this method) 
 		 * can result in an IllegalStateException
 		 * 
 		 * @return
 		 */
 		public boolean dead()
 		{
 			return dead;
 		}
 		
 		/**
 		 * Removes this node from the graph
 		 */
 		protected void remove()
 		{
 			boolean contained = nodes.get(label()).remove(this);
 			dead = true;
 			
 			
 			Iterator<Node> it = neighbours.iterator();
 			while(it.hasNext())
 			{
 				Node neighbour = it.next();
 				neighbour.neighbours.remove(this);
 				it.remove();
 			}
 		}
 	}
 
 	@Override
 	public Node node(L label)
 	{
 		Set<Node> n = nodes.get(label);
 		if(n == null)
 			return null;
 	
 		return n.iterator().next();
 	}
 
 	@Override
 	public int size()
 	{
 		return size;
 	}
 
 	@Override
 	public Set<Node> nodes(L label)
 	{
 		Set<Node> n = nodes.get(label);
 		if(n == null)
 			return Collections.emptySet();
 		
 		return Collections.unmodifiableSet(n); 
 	}
 
 	@Override
 	public Iterator<Node> iterator()
 	{
 		return new BGIterator();
 	}
 	
 	private class BGIterator implements Iterator<Node>
 	{
 		private static final int BUFFER_SIZE = 10;
 		private Deque<Node> buffer = new LinkedList<Node>();
 		private Iterator<L> labelIterator;
 		private Iterator<Node> nodeIterator;
 
 		public BGIterator()
 		{
 			labelIterator = nodes.keySet().iterator();
 
 		}
 		
 		@Override
 		public boolean hasNext()
 		{
 			buffer();
 			return ! buffer.isEmpty();
 		}
 
 		@Override
 		public Node next()
 		{
 			buffer();
 			return buffer.pop();
 		}
 
 		@Override
 		public void remove()
 		{
 			// * TODO support?
 			throw new UnsupportedOperationException();
 		}
 		
 		public void buffer()
 		{
 			while(buffer.size() < BUFFER_SIZE)
 			{
 				
 				while(nodeIterator == null || ! nodeIterator.hasNext())
 				{
 					if(! labelIterator.hasNext())
 						return;
 					nodeIterator = nodes.get(labelIterator.next()).iterator();
 				}
 				
 				buffer.add(nodeIterator.next());
 			}
 		}
 		
 	}
 
 	@Override
 	public int numEdges()
 	{
 		return numEdges;
 	}
 
 	@Override
 	public Node addNode(L label)
 	{
 		size ++;
 		modCount++;
 		
 		Node node = new Node(label);
 		if(!nodes.containsKey(label))
 			nodes.put(label, new LinkedHashSet<Node>());
 		
 		nodes.get(label).add(node);
 		return node;
 	}
 	
 	@Override
 	public boolean remove(Object object)
 	{
 		size--;
 		modCount++;
 		
 		if(! (object instanceof BaseGraph.Node))
 			return false;
 		
 		@SuppressWarnings("unchecked")
 		Node node = (Node) object;
 		L label = node.label();
 
 		if(node.graph() != this)
 			throw new IllegalArgumentException("Node does not belong to this graph.");
 		
 		node.remove();
 		
 		// If this is the last node with the given label, remove it from the nodes 
 		// Map
 		if(nodes.get(label) != null && nodes.get(label).size() == 0)
 			nodes.remove(label);
 		
 		return true;
 	}
 
 	/**
 	 * Returns true if each label currently describes a unique node. 
 	 * 
 	 * 
 	 * @return
 	 */
 	public boolean unique()
 	{
 		for(L label : nodes.keySet())
 			if(nodes.get(label).size() > 1)
 				return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Returns a representation of the graph in Dot language format.
 	 */
 	public String toString()
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("graph {");
 		
 		Set<Node> done = new HashSet<Node>();
 		
 		int c = 0;
 		boolean first = true;
 		for(L label : nodes.keySet())
 			for(Node node : nodes.get(label))
 			{
 				
 				for(Node neighbour : node.neighbours())
 					if(! done.contains(neighbour))
 					{
 						if(first)
 							first = false;
 						else
 							sb.append("; ");
 					
 						sb.append(node.toString() + " -- " + neighbour.toString());
 						c++;
 					}
 				
 				if(node.neighbours().isEmpty())
 				{
 					if(first)
 						first = false;
 					else
 						sb.append("; ");
 					
 					sb.append(node.toString());
 					c++;
 				}
 				
 				done.add(node);
 			}
 		
 		sb.append("}");
 		
 		System.out.println(sb.length());
 		return sb.toString();
 	}
 
 	@Override
 	public boolean connected(L first, L second)
 	{
 		for(Node f : nodes.get(first))
 			for(Node s : nodes.get(second))
 				if(f.connected(s))
 					return true;
 		return false;
 	}
 
 	@Override
 	public Set<L> labels()
 	{
 		return Collections.unmodifiableSet(nodes.keySet());
 	}
 	
 
 }
