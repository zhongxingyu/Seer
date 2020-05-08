 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
 import edu.uci.ics.jung.graph.util.EdgeType;
 
 /**
  * Data storage for an abstracted topology view. 
  * See http://jung.sourceforge.net/site/apidocs/edu/uci/ics/jung/graph/Graph.html for documentation about inherited member functions.
  * 
  * @param <NodeObject> define what is used as node objects
  * @param <LinkObject> define what is used as link objects
  */
 public class AbstractRoutingGraph<NodeObject, LinkObject> extends RoutableGraph<NodeObject, LinkObject>
 {
 	public AbstractRoutingGraph()
 	{
 		super(null);
 	}
 
 	/**
 	 * Returns the neighbors of a graph node.
 	 * 
 	 * @param pNode the node for which the neighbors should be determined
 	 * 
 	 * @return a collection of found neighbor node
 	 */
 	public Collection<NodeObject> getNeighbors(NodeObject pNode)
 	{
 		LinkedList<NodeObject> tResult = new LinkedList<NodeObject>();
 		
 		synchronized (mRoutingGraph) {
 			if (mRoutingGraph.containsVertex(pNode)){
 				tResult = (LinkedList<NodeObject>) mRoutingGraph.getNeighbors(pNode);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * This method registers a link between two nodes in the routing graph. 
 	 * If the nodes don't exist in the routing graph, they are registered implicitly.
 	 * 
 	 * @param pFrom starting point of the link
 	 * @param pTo the ending point of the link
 	 * @param pLinkObject the link object
 	 */
 	@Override
 	public synchronized void link(NodeObject pFrom, NodeObject pTo, LinkObject pLinkObject)
 	{
 		// check if parameters are valid
 		if((pFrom != null) && (pTo != null) && (pLinkObject != null)) {
 			// make sure the starting point is known
 			pFrom = add(pFrom);
 			
 			// make sure the ending point is known
 			pTo = add(pTo);
 			
 			//Logging.trace(this, "Linking from " + pFrom + " to " + pTo + " via " + pLinkObject);
 			
			// check if there already exist a link between these two nodes
//			if(!isLinked(pFrom, pTo)) {
				// add the link to the routing graph
				if(mRoutingGraph.addEdge(pLinkObject, pFrom, pTo, EdgeType.UNDIRECTED)) {
					notifyObservers(new Event(EventType.ADDED, pLinkObject));
 				}
//			}
 		}
 	}
 	
 	/**
 	 * Get all nodes that are between the source and the target
 	 * 
 	 * @param pFrom This is the source of the path you want to get all nodes for.
 	 * @param pTo This is the target of the path you want to get all nodes for
 	 * @return a list of all nodes between source and target
 	 */
 //	public synchronized List<NodeObject> getIntermediateNodes(NodeObject pFrom, NodeObject pTo)
 //	{
 //		List<LinkObject> tPath = null;
 //		
 //		LinkedList<NodeObject> tNodes = new LinkedList<NodeObject>();
 //		
 //		pFrom = containsVertex(pFrom);
 //		pTo = containsVertex(pTo);
 //		
 //		if((pFrom != null) && (pTo != null)) {
 //			DijkstraShortestPath<NodeObject, LinkObject> tRoutingAlgo = new DijkstraShortestPath<NodeObject, LinkObject>(mRoutingGraph);
 //			tPath = tRoutingAlgo.getPath(pFrom, pTo);
 //			
 //			NodeObject tTarget = pFrom;
 //			
 //			for(LinkObject tLink : tPath) {
 //				tTarget = getOtherEndOfLink(tTarget, tLink);
 //				tNodes.add(tTarget);
 //			}
 //		}
 //
 //		return tNodes;
 //	}
 	
 	/**
 	 * Get the other end node of a link in the stored undirected graph.
 	 * 
 	 * @param pKnownEnd the known end node of the link
 	 * @param pLink the link for which the other end node should be determined
 	 * @return the other end node of the link
 	 */
 	public synchronized NodeObject getOtherEndOfLink(NodeObject pKnownEnd, LinkObject pLink)
 	{
 		NodeObject tResult = null;
 		
 		try {
 			tResult = mRoutingGraph.getOpposite(pKnownEnd, pLink);
 		} catch (IllegalArgumentException tExc) {
 			Logging.err(this, pKnownEnd + " isn't an end node of the link " + pLink + "(possible end nodes are: " + mRoutingGraph.getIncidentVertices(pLink) + ")", tExc);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines a route between two nodes
 	 * 
 	 * @param pFrom the starting point of the route
 	 * @param pTo the ending point of the route
 	 * 
 	 * @return the route between the two nodes
 	 */
 	@Override
 	public synchronized List<LinkObject> getRoute(NodeObject pFrom, NodeObject pTo)
 	{
 		List<LinkObject> tResult = null;
 
 		pFrom = containsVertex(pFrom);
 		pTo = containsVertex(pTo);
 
 		if((pFrom != null) && (pTo != null)) {
 			// use Djikstra over the routing graph
 			DijkstraShortestPath<NodeObject, LinkObject> tRoutingAlgo = new DijkstraShortestPath<NodeObject, LinkObject>(mRoutingGraph);
 			tResult = tRoutingAlgo.getPath(pFrom, pTo);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns a list of nodes which are ordered by their graph distance to a given root node  
 
 	 * @param pRootNode the root node
 	 * 
 	 * @return the list of nodes
 	 */
 	public List<NodeObject> getVerticesInOrderRadius(NodeObject pRootNode)
 	{
 		List<NodeObject> tResult = null;
 		
 		//HINT: http://jung.sourceforge.net/doc/api/edu/uci/ics/jung/algorithms/shortestpath/BFSDistanceLabeler.html
 		
 		// create "Breadth-First Search" (BFS) object
 		BFSDistanceLabeler<NodeObject, LinkObject> tBreadthFirstSearch = new BFSDistanceLabeler<NodeObject, LinkObject>();
 
 		// compute the distances of all the node from the specified root node (parent cluster).
 		tBreadthFirstSearch.labelDistances(getGraphForGUI(), pRootNode);
 
 		// the result
 		tResult = tBreadthFirstSearch.getVerticesInOrderVisited();
 		
 		return tResult;
 	}
 
 	/**
 	 * Checks if two nodes have a known link.
 	 * 
 	 * @param pFirst the first node
 	 * @param pSecond the second node
 	 * @return true if a link is known, otherwise false
 	 */
 	public synchronized boolean isLinked(NodeObject pFirst, NodeObject pSecond)
 	{
 		if(mRoutingGraph.containsVertex(pFirst)) {
 			return mRoutingGraph.getNeighbors(pFirst).contains(pSecond);
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Checks if a node is a known one.
 	 * 
 	 * @param pNode the node
 	 * @return true if the node is known, otherwise false
 	 */
 	public boolean isknown(NodeObject pNode)
 	{
 		return contains(pNode);
 	}
 
 	/**
 	 * Return a descriptive string
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName();
 	}
 }
