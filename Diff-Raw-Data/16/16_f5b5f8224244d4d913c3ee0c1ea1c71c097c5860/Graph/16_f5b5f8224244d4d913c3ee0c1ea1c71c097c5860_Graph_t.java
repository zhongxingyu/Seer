 package monitor;
 
 import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import utilities.A;
 
 import communication.NodeId;
 
 public class Graph implements Serializable
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5293390993631660455L;
	private List<Node> allNodes = new ArrayList<Node>();
	private Map<NodeId, Node> nodeMap = new HashMap<NodeId, Node>();
 	private Node distributor;
 	private Node rootNode; //Traversal iterators always start here.
 	
 	public void addNode(NodeId id)
 	{
 		Node node = new Node(id);
 		
 		allNodes.add(node);
 		nodeMap.put(id, node);
 	}
 	
 	public void setRootNode(Node root)
 	{
 		this.rootNode = root;
 	}
 	
 	public void setDistributor(Node distributor)
 	{
 		this.distributor = distributor;
 	}
 	
 	
 	public List<Node> getAllNodes()
 	{
 		return allNodes;
 	}
 
 
 	public Map<NodeId, Node> getNodeMap()
 	{
 		return nodeMap;
 	}
 
 	public Node getDistributor()
 	{
 		return distributor;
 	}
 
 	public Node getRootNode()
 	{
 		return rootNode;
 	}
 
 	/**
 	 * Adds a directional edge from node a to b.
 	 * 
 	 * @param a - The orign node.
 	 * @param b - The destination node.
 	 */
 	public void addEdge(NodeId a, NodeId b)
 	{
 		if(!nodeMap.containsKey(a))
 			A.fatalError("Adding an edge with a non-existent node.  Add the node first.");
 		
 		if(!nodeMap.containsKey(b))
 			A.fatalError("Adding an edge with a non-existent node.  Add the node first.");
 		
 		Node A = nodeMap.get(a);
 		Node B = nodeMap.get(b);
 		
 		A.addConnection(B);
 	}
 	
 //	public Iterator<Node> getDFSIterator()
 //	{
 //		return null;
 //		
 //	}
 //	
 //	public Iterator<Node> getBFSIterator()
 //	{
 //		return null;
 //	}
 	
 //	private class DFSIterator implements Iterator<Node>
 //	{
 //		private Node currentNode;
 //		
 //		@Override
 //		public boolean hasNext()
 //		{
 //			// TODO Auto-generated method stub
 //			return false;
 //		}
 //
 //		@Override
 //		public Node next()
 //		{
 //			// TODO Auto-generated method stub
 //			return null;
 //		}
 //
 //		@Override
 //		public void remove()
 //		{
 //			// TODO Auto-generated method stub
 //			
 //		}
 //		
 //	}
 	
 //	private class BFSIterator implements Iterator<Node>
 //	{
 //		private Node currentNode;
 //		private Set<Node> closedList;
 //		
 //		public BFSIterator()
 //		{
 //			currentNode = rootNode;
 //		}
 //		
 //		@Override
 //		public boolean hasNext()
 //		{
 //			boolean nodesRemaining = false;
 //			
 //			for(Node n : currentNode.getEdges())
 //			{
 //				if(!closedList.contains(n))
 //					nodesRemaining = true;
 //			}
 //			
 //			return false;
 //		}
 //
 //		@Override
 //		public Node next()
 //		{
 //			for(Node n : currentNode.getEdges())
 //			{
 //				if(!closedList.contains(n))
 //					return n;
 //			}
 //			
 //			return null;
 //		}
 //
 //		@Override
 //		public void remove()
 //		{
 //			//NO idea what the fuck.
 //		}
 //		
 //	}
  }
