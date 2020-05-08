 package monitor;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.List;
 
 import communication.NodeId;
 
 public class Node implements Serializable
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -708954300495449217L;
 	private NodeId id;
	private List<Node> edges = new ArrayList<Node>();
 	private iNodeStatus status;
 	
 	public Node(NodeId id)
 	{
 		this.id = id;
 	}
 	
 	public void setNodeStatus(iNodeStatus status)
 	{
 		this.status = status;
 	}
 	
 	public iNodeStatus getNodeStatus()
 	{
 		return status;
 	}
 	
 	public void addConnection(Node n)
 	{
 		edges.add(n);
 	}
 	
 	public List<Node> getEdges()
 	{
 		return edges;
 	}
 	
 	public NodeId getNodeId()
 	{
 		return id;
 	}
 }
