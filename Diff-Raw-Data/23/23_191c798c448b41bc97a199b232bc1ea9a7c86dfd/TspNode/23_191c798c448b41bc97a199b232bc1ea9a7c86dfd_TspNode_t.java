 package bnb.tsp;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 
 import bnb.Problem;
 import bnb.Solution;
 import bnb.BnbNode;
 
 public class TspNode extends BnbNode {
 	private static final Logger LOG = Logger.getLogger(TspNode.class);
 	
 	private boolean isEvaluated;
 	
 	private boolean bounded;
 	private double parentTourCost = -1.0;
 	private double tourCost = -1.0;
 	
 	private int numChosen;
 	private City city;
 	
 	private TspProblem problem;
 	
 	//children that are returned with nextChild are added to this list
 	//TODO: take this into account when constructing a node
 	private List<City> exploredChildren;
 	
 	private List<City> remainingCities;
 	
 	private City startCity;
 	
 	//if parent is null, and this isn't the root, this shouldn't be null
 	private ArrayList<City> prevCities;
 	
 	public TspNode() {
 		super(null);
 	}
 	
 	public TspNode(City startCity, City city, int numChosen, TspNode parent, List<City> remCities, TspProblem problem) {
 		super(parent);
 		this.startCity = startCity;
 		this.city = city;
 		this.remainingCities = remCities;
 		
 		this.numChosen = numChosen;
 		if (parent != null) {
 			parentTourCost = parent.getTourCost();
 		}
 		this.problem = problem;
 	}
 	
 	@Override
 	public boolean isEvaluated() {
 		return isEvaluated;
 	}
 	
 	@Override
 	public boolean isLeaf() {
 		return numChosen == problem.getNumCities();
 	}
 	
 	@Override
 	public boolean isSolution() {
 		if (!isEvaluated) {
 			throw new IllegalStateException("Tsp node not yet evaluated.");
 		}
 		return !bounded && numChosen == problem.getNumCities();
 	}
 	
 	public double getTourCost() {
 		return tourCost;
 	}
 	
 	
 	private List<City> buildRemainingCities(Iterator<City> iter) {
 		//build a new remainingCities
 		for (City city : problem.getCities()) {
 			city.threadLocalMark.set(0);
 		}
 		while (iter.hasNext()) {
 			iter.next().threadLocalMark.set(1);
 		}
 		if (exploredChildren != null) {
 			for (City city : exploredChildren) {
 				city.threadLocalMark.set(2);
 			}
 		}
 		LinkedList<City> remCities = new LinkedList<City>();
 		for (City city : problem.getCities()) {
 			if (city.threadLocalMark.get() == 0) {
 				remCities.addFirst(city);
 			} else if (city.threadLocalMark.get() == 2) {
 				//explored children should get added to the back of the list
 				remCities.addLast(city);
 			}
 		}
 		
 		return remCities;
 	}
 	
 	public City getCity() {
 		return city;
 	}
 	
 	public ArrayList<City> getPrevCities() {
 		return prevCities;
 	}
 	
 	@Override
 	public void evaluate(double bound) {
 
 		if (!doEvaluate(bound)) {
 			bounded = true;
 //			System.out.println("bounded");
 		}
 		isEvaluated = true;
 		exploredChildren = new LinkedList<City>();
 	}
 		
 	/**
 	 * 
 	 * @return
 	 * 		true if not bounded, false is yes bounded
 	 */
 	private boolean doEvaluate(double minCost) {
 		if (isEvaluated) {
 			LOG.warn("node about to be reevaluated");
 		}
 //		LOG.debug("about to evaluate " + this);
 		
 		if (numChosen == 2) {
 			tourCost = 2 * startCity.dist(city);
 		} else if (numChosen > 2) {
 			Iterator<City> iter = new ParentCityIterator(this);
 			iter.next();
 			City prevCity = iter.next();
 			tourCost = parentTourCost - prevCity.dist(startCity) + 
 				prevCity.dist(city) + city.dist(startCity);
 		}
 		
 		if (tourCost >= minCost) {
 			return false;
 		}
 		
 /*		//if 2 or 3 opt give us better solutions, continue
 		ParentCityIterator iter = new ParentCityIterator(this);
 		iter.next();
 		City prevCity = iter.next();
 		City temp = iter.next();
 		while (iter.hasNext()) {
 			City next = temp;
 			if (!iter.hasNext()) {
 				//don't use last city
 				break;
 			}
 			temp = iter.next();
 			
 			TspUtils.cost2opt(prevCity, city, temp, next);
 			TspUtils.cost2opt(next, temp2, index2);
 		}
 		for (int j = 1; j < numChosen-3; j++)
 		{
 			if (TspUtils.cost2opt(cities, numChosen-2, j) < 0 || TspUtils.cost2opt(cities, j, numChosen-2) < 0)
 			{
 				//cost2opt gives something better, so discard
 				return false;
 			}
 		}
 		
 		for (int j = 1; j < numChosen-3; j++)
 		{
 			for (int k = 1; k < numChosen-3; k++)
 			{
 				if (j == k || (j+1) == k || (j-1) == k)
 					continue;
 				
 				if (TspUtils.cost3opt(cities, j, k, numChosen-2) < 0 || 
 					TspUtils.cost3opt(cities, k, j, numChosen-2) < 0 ||
 					TspUtils.cost3opt(cities, j, numChosen-2, k) < 0 ||
 					TspUtils.cost3opt(cities, numChosen-2, k, j) < 0 ||
 					TspUtils.cost3opt(cities, k, numChosen-2, j) < 0 ||
 					TspUtils.cost3opt(cities, numChosen-2, j, k) < 0)
 				{
 					return false;
 				}
 			}
 		}
 		*/
 		return true;
 	}
 	
 	@Override
 	public boolean hasNextChild() {
 		//TODO: make sure we're incrementing numChildrenReleased whenever we call nextChild
 		return !bounded && !isSolution() && numChosen + exploredChildren.size() < problem.getNumCities();
 	}
 	
 	@Override
 	public BnbNode nextChild() {
 		if (!hasNextChild()) {
 			throw new NoSuchElementException("Node has no next child.");
 		}
 		List<City> remCities = remainingCities;
 		int count = activeChildCount.getAndIncrement();
 		if (count > 0) {
 //			LOG.info("Splitting, activeChildCount before increment=" + count);
 			remCities = buildRemainingCities(new ParentCityIterator(this));
 		}
 		City city = remCities.remove(0);
 		exploredChildren.add(city);
 		TspNode child = new TspNode(startCity, city, numChosen+1, this, remCities, problem);
 		return child;
 	}
 	
 	@Override
 	public void whenAllChildrenDone() {
 		remainingCities.add(city);
 	}
 	
 	@Override
 	public void initFromBytes(byte[] bytes, Problem prob) {
 		if (!(prob instanceof TspProblem)) {
 			throw new IllegalArgumentException("problem must be TspProblem");
 		}
 		problem = (TspProblem)prob;
 		try {
 			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
 			DataInputStream dis = new DataInputStream(bais);
 			
 			isEvaluated = dis.readBoolean();
 			int numCities = dis.readInt();
			//TODO: how do we reconstruct this in the most efficient way?
 			prevCities = new ArrayList<City>(numCities);
 			for (int i = 0; i < numCities; i++) {
 				prevCities.add(null);
 			}
 			City[] problemCities = problem.getCities();
 			for (int i = numCities-1; i >= 0; i--) {
 				int id = dis.readInt();
 				prevCities.set(i, problemCities[id]);
 				numChosen++;
 			}
 			
 			startCity = problemCities[0];
 			remainingCities = buildRemainingCities(prevCities.iterator());
 			
 			//remove what's set to city. we only kept it in as a shortcut to add it
 			//to the list for buildRemainingCities
			city = prevCities.remove(prevCities.size()-1);
 			
 			//TODO: explored children
 		} catch (IOException ex) {
 			LOG.error("IOException reading from byte array, this should never happen", ex);
 		}
 	}
 	
 	@Override
 	public byte[] toBytes() {
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			DataOutputStream dos = new DataOutputStream(baos);
 
 			//TODO: check for off by ones
 			
 			//isEvaluated
 			dos.writeBoolean(isEvaluated);
 			
 			//num nodes in path
 			dos.writeInt(numChosen);
 
 			//nodes in path
 			Stack<City> stack = new Stack<City>();
 			ParentCityIterator iter = new ParentCityIterator(this);
 			while (iter.hasNext()) {
 				stack.add(iter.next());
 			}
 			for (City city : stack) {
 				dos.writeInt(city.id);
 			}
 			//TODO: explored children
 			for (int i = 0; i < numChosen; i++) {
 			}
 //			//num explored children
 //			dos.writeInt(exploredChildren.size());
 //			//explored children
 //			for (City child : exploredChildren) {
 //				dos.writeInt(child.id);
 //			}
 			
 			return baos.toByteArray();
 		} catch (IOException ex) {
 			LOG.error("IOException writing to byte array, this should never happen", ex);
 			return null;
 		}
 	}
 	
 	@Override
 	public double getCost() {
 		return tourCost;
 	}
 	
 	@Override
 	public Solution getSolution() {
 		return new TspSolution(new ParentCityIterator(this), problem.getNumCities());
 	}
 	
 	@Override
 	public String toString() {
 		Stack<Integer> stack = new Stack<Integer>();
 		ParentCityIterator iter = new ParentCityIterator(this);
 		while (iter.hasNext()) {
 			stack.add(iter.next().id);
 		}
 		StringBuilder sb = new StringBuilder();
 		while (!stack.isEmpty()) {
 			sb.append(stack.pop() + ", ");
 		}
 		sb.delete(sb.length() - 2, sb.length());
 		return sb.toString();
 	}
 	
 /*	
 	private int bound(City[] nodes, boolean[] remainingVector, int numChosen, Collection<Edge> edges)
 	{
 		List<Edge> usableEdges = new ArrayList<Edge>(edges.size());
 		City startNode = nodes[0], endNode = nodes[numChosen-1];
 		
 		//compute edges we can still use
 		for (Edge edge : edges)
 		{
 			if (edge.node1 == startNode || edge.node2 == startNode || edge.node1 == endNode || edge.node2 == endNode)
 				if ((edge.node1 == startNode && edge.node2 == endNode) || (edge.node1 == endNode && edge.node2 == startNode))
 					continue;
 			if (edge.node1 != startNode && edge.node1 != endNode && !remainingVector[edge.node1.id])
 				continue;
 			if (edge.node2 != startNode && edge.node2 != endNode && !remainingVector[edge.node2.id])
 				continue;
 //			if (Utils.edgeCrossesPath(nodes, numChosen, edge.node1, edge.node2))
 //				continue;
 			
 			usableEdges.add(edge);
 		}
 		
 		int lowerBound = Integer.MIN_VALUE;
 		
 		for (int i = 0; i < remainingNodesList.size(); i++)
 		{
 			City oneTreeNode = remainingNodesList.remove(0);
 			LinkedList<Edge> removedEdges = new LinkedList<Edge>();
 			Iterator<Edge> iter = usableEdges.iterator();
 			Edge bestEdge = null, secondBestEdge = null;
 			
 //			boolean[] nodesTouched = new boolean[nodes.length];
 //			int numNodesTouched = 0;
 //			for (Node node : remainingNodesList)
 //				nodesTouched[node.id] = true;
 			
 			while (iter.hasNext())
 			{
 				Edge e = iter.next();
 				if (e.node1 == oneTreeNode || e.node2 == oneTreeNode)
 				{
 					iter.remove();
 					removedEdges.add(e);
 					if (bestEdge == null || e.dist < bestEdge.dist)
 					{
 						secondBestEdge = bestEdge;
 						bestEdge = e;
 					}
 					else if (secondBestEdge == null || e.dist < secondBestEdge.dist)
 					{
 						secondBestEdge = e;
 					}
 				}
 //				else
 //				{
 //					if (nodesTouched[e.node1.id] == true)
 //					{
 //						nodesTouched[e.node1.id] = false;
 //						numNodesTouched++;
 //					}
 //					if (nodesTouched[e.node2.id] == true)
 //					{
 //						nodesTouched[e.node2.id] = false;
 //						numNodesTouched++;
 //					}
 //				}
 			}
 //			if (numNodesTouched != remainingNodesList.size())
 //			{
 //				remainingNodesList.add(oneTreeNode);
 //				return Integer.MAX_VALUE / 2;
 //			}
 			
 			//held and karp!
 			int[] nodeCosts = new int[nodes.length], nodeEdges = new int[nodes.length];
 			final double limit = .5;
 			double stepSize = Double.MAX_VALUE, stepScale = 2.0, stepChange = .75;
 			
 			for (int j = 0; j < MAX_ITERATIONS; j++)
 			{
 				for (Edge edge : usableEdges)
 					edge.extra = -nodeCosts[edge.node1.id] - nodeCosts[edge.node2.id];
 				Arrays.fill(nodeEdges, 0);
 
 				PriorityQueue<Edge> edgesHeap = new PriorityQueue<Edge>(usableEdges);
 				int cost = mstCost(startNode, endNode, remainingNodesList, edgesHeap, nodeEdges) + bestEdge.dist + secondBestEdge.dist;
 //				System.out.println ("before cost: "+ cost);
 				for (Node node : remainingNodesList)
 					cost += 2 * nodeCosts[node.id];
 				cost += nodeCosts[startNode.id];
 				cost += nodeCosts[endNode.id];
 //				System.out.println ("after cost: " + cost);
 				if (cost > upper_bound)
 				{
 					remainingNodesList.add(oneTreeNode);
 					return cost;
 				}
 				if (cost > lowerBound)
 					lowerBound = cost;
 				
 				//calculate step size
 				int sumSquareDiffs = 0;
 				for (Node node : remainingNodesList)
 				{
 //					if (nodeEdges[node.id] < 1)
 //						System.out.println ("this is wrong");
 					int optimalNumEdges = (node == bestEdge.node1 || node == bestEdge.node2 || node == secondBestEdge.node1 || node == secondBestEdge.node2) ? 1 : 2;
 					sumSquareDiffs += (optimalNumEdges-nodeEdges[node.id]) * (optimalNumEdges-nodeEdges[node.id]);
 				}
 				sumSquareDiffs += (1-nodeEdges[startNode.id]) * (1-nodeEdges[startNode.id]);
 				sumSquareDiffs += (1-nodeEdges[endNode.id]) * (1-nodeEdges[endNode.id]);
 					
 				stepSize = (stepScale * (upper_bound - cost)) / sumSquareDiffs;
 				if (stepSize < limit)
 				{
 //					System.out.println ("j: " + j);
 					break;
 				}
 				
 				for (City node : remainingNodesList)
 				{
 					int optimalNumEdges = (node == bestEdge.node1 || node == bestEdge.node2 || node == bestEdge.node1 || node == bestEdge.node2) ? 1 : 2;
 					nodeCosts[node.id] += (int)(stepSize * (optimalNumEdges - nodeEdges[node.id]));
 				}
 				startNode.cost += (int)(stepSize * (1 - nodeEdges[startNode.id]));
 				endNode.cost += (int)(stepSize * (1 - nodeEdges[endNode.id]));
 				
 				stepScale *= stepChange;
 			}
 			
 			//add stuff back in
 			remainingNodesList.add(oneTreeNode);
 			usableEdges.addAll(removedEdges);
 		}
 		return lowerBound;
 	}*/
 }
 
 
 //public class BranchAndBound
 //{
 //	private static final int MAX_ITERATIONS = 50;
 //	
 //	private int upper_bound = 0;
 //	private Node[] best_sol = null;
 //	
 //	public void runBranchAndBound(Node[] nodes, int upperBound)
 //	{
 //		Node[] sol = new Node[nodes.length];
 //		sol[0] = nodes[0];
 //		upper_bound = upperBound;
 //		best_sol = new Node[nodes.length];
 //		System.arraycopy(nodes, 0, best_sol, 0, nodes.length);
 //		
 //		List<Node> remainingNodes = new LinkedList<Node>();
 //		boolean[] remainingVector = new boolean[nodes.length];
 //		for (Node node : nodes)
 //			if (node != sol[0])
 //			{
 //				remainingNodes.add(node);
 //				remainingVector[node.id] = true;
 //			}
 //		
 //		Collection<Edge> edges = new ArrayList<Edge>((nodes.length - 1) * nodes.length / 2);
 //		for (int i = 0; i < nodes.length; i++)
 //			for (int j = i+1; j < nodes.length; j++)
 //			{
 //				Edge e = new Edge(nodes[i], nodes[j]);
 //				edges.add(e);
 //			}
 //		
 //		recurseBranchAndBound(sol, remainingNodes, remainingVector, 1, edges, 0, 0);
 //	}
 //	
 //	public int getCost()
 //	{
 //		return upper_bound;
 //	}
 //	
 //	public Node[] getSolution()
 //	{
 //		return best_sol;
 //	}
 //	
 //	/**
 //	 * @return the min tour dist under this branch.
 //	 */
 //	private void recurseBranchAndBound(Node[] nodes, List<Node> remainingNodes, boolean[] remainingVector, int numChosen, Collection<Edge> edges, int sumDist, int level)
 //	{
 //		//base case
 //		if (numChosen == nodes.length-1)
 //		{
 //			nodes[numChosen] = remainingNodes.get(0);
 //			sumDist += Utils.dist(nodes[numChosen-1], nodes[numChosen]) + Utils.dist(nodes[numChosen], nodes[0]);
 //			if (sumDist < upper_bound)
 //			{
 //				upper_bound = sumDist;
 //				System.arraycopy(nodes, 0, best_sol, 0, nodes.length);
 //			}
 //			return;
 //		}
 //		//else
 //		outerloop:
 //		for (int i = 0; i < remainingNodes.size(); i++)
 //		{
 ////			if(level <= 7)
 ////				System.out.println("at loop: " + level + "\t" + i);
 //			Node node = remainingNodes.remove(0);
 //			// or the added edge crosses an edge, continue
 //			if (Utils.edgeCrossesPath(nodes, numChosen, nodes[numChosen-1], node))
 //			{
 //				remainingNodes.add(node);
 ////				System.out.println ("pruned w/crossing edge");
 //				continue;
 //			}
 //			
 //			nodes[numChosen] = node;
 //			remainingVector[node.id] = false;
 //			//if 2 or 3 opt give us better solutions, continue
 //			for (int j = 1; j < numChosen-2; j++)
 //			{
 //				if (LocalSearch.cost2opt(nodes, numChosen-1, j) < 0 || LocalSearch.cost2opt(nodes, j, numChosen-1) < 0)
 //				{
 //					remainingNodes.add(node);
 //					nodes[numChosen] = null;
 //					remainingVector[node.id] = true;
 ////					System.out.println ("found better with cost2opt");
 //					continue outerloop;
 //				}
 //			}
 //			
 //			for (int j = 1; j < numChosen-2; j++)
 //			{
 //				for (int k = 1; k < numChosen-2; k++)
 //				{
 //					if (j == k || (j+1) == k || (j-1) == k)
 //						continue;
 //					
 //					if (cost3opt(nodes, j, k, numChosen-1) < 0 || cost3opt(nodes, k, j, numChosen-1) < 0 || cost3opt(nodes, j, numChosen-1, k) < 0
 //						|| cost3opt(nodes, numChosen-1, k, j) < 0 || cost3opt(nodes, k, numChosen-1, j) < 0 || cost3opt(nodes, numChosen-1, j, k) < 0)
 //					{
 //						remainingNodes.add(node);
 //						nodes[numChosen] = null;
 //						remainingVector[node.id] = true;
 ////						System.out.println ("found better with cost3opt");
 //						continue outerloop;
 //					}
 //				}
 //			}
 //			
 //			//obtain lower bound using held & karp
 //			int lowerBound = (numChosen < nodes.length-3) ? sumDist + bound(nodes, remainingNodes, remainingVector, numChosen+1, edges) : -1;
 //			if (lowerBound > upper_bound)
 //			{
 ////				System.out.println("pruned using held & karp " + lowerBound + "\t" + numChosen);
 //			}
 //			else if (lowerBound == upper_bound)
 //			{
 ////				System.out.println ("held & karp lower bound equals upper bound");
 //			}
 //			else
 //			{
 //				recurseBranchAndBound(nodes, remainingNodes, remainingVector, numChosen+1, edges, sumDist + Utils.dist(nodes[numChosen-1], node), level+1);
 //			}
 //			remainingNodes.add(node);
 //			
 //			remainingVector[node.id] = true;
 //			
 ////			remainingNodes.(node);
 //		}
 //	}
 //	
 //	//banned edges:
 //	//edges on the path
 //	//edges incident on nodes on the path that aren't the start and finish
 //	//edge from start to finish
 //	//crossing edges
 //	
 //	private int bound(Node[] nodes, List<Node> remainingNodesList, boolean[] remainingVector, int numChosen, Collection<Edge> edges)
 //	{
 //		List<Edge> usableEdges = new ArrayList<Edge>(edges.size());
 //		Node startNode = nodes[0], endNode = nodes[numChosen-1];
 //		
 //		//compute edges we can still use
 //		for (Edge edge : edges)
 //		{
 //			if (edge.node1 == startNode || edge.node2 == startNode || edge.node1 == endNode || edge.node2 == endNode)
 //				if ((edge.node1 == startNode && edge.node2 == endNode) || (edge.node1 == endNode && edge.node2 == startNode))
 //					continue;
 //			if (edge.node1 != startNode && edge.node1 != endNode && !remainingVector[edge.node1.id])
 //				continue;
 //			if (edge.node2 != startNode && edge.node2 != endNode && !remainingVector[edge.node2.id])
 //				continue;
 ////			if (Utils.edgeCrossesPath(nodes, numChosen, edge.node1, edge.node2))
 ////				continue;
 //			
 //			usableEdges.add(edge);
 //		}
 //		
 //		int lowerBound = Integer.MIN_VALUE;
 //		
 //		for (int i = 0; i < remainingNodesList.size(); i++)
 //		{
 //			Node oneTreeNode = remainingNodesList.remove(0);
 //			LinkedList<Edge> removedEdges = new LinkedList<Edge>();
 //			Iterator<Edge> iter = usableEdges.iterator();
 //			Edge bestEdge = null, secondBestEdge = null;
 //			
 ////			boolean[] nodesTouched = new boolean[nodes.length];
 ////			int numNodesTouched = 0;
 ////			for (Node node : remainingNodesList)
 ////				nodesTouched[node.id] = true;
 //			
 //			while (iter.hasNext())
 //			{
 //				Edge e = iter.next();
 //				if (e.node1 == oneTreeNode || e.node2 == oneTreeNode)
 //				{
 //					iter.remove();
 //					removedEdges.add(e);
 //					if (bestEdge == null || e.dist < bestEdge.dist)
 //					{
 //						secondBestEdge = bestEdge;
 //						bestEdge = e;
 //					}
 //					else if (secondBestEdge == null || e.dist < secondBestEdge.dist)
 //					{
 //						secondBestEdge = e;
 //					}
 //				}
 ////				else
 ////				{
 ////					if (nodesTouched[e.node1.id] == true)
 ////					{
 ////						nodesTouched[e.node1.id] = false;
 ////						numNodesTouched++;
 ////					}
 ////					if (nodesTouched[e.node2.id] == true)
 ////					{
 ////						nodesTouched[e.node2.id] = false;
 ////						numNodesTouched++;
 ////					}
 ////				}
 //			}
 ////			if (numNodesTouched != remainingNodesList.size())
 ////			{
 ////				remainingNodesList.add(oneTreeNode);
 ////				return Integer.MAX_VALUE / 2;
 ////			}
 //			
 //			//held and karp!
 //			int[] nodeCosts = new int[nodes.length], nodeEdges = new int[nodes.length];
 //			final double limit = .5;
 //			double stepSize = Double.MAX_VALUE, stepScale = 2.0, stepChange = .75;
 //			
 //			for (int j = 0; j < MAX_ITERATIONS; j++)
 //			{
 //				for (Edge edge : usableEdges)
 //					edge.extra = -nodeCosts[edge.node1.id] - nodeCosts[edge.node2.id];
 //				Arrays.fill(nodeEdges, 0);
 //
 //				PriorityQueue<Edge> edgesHeap = new PriorityQueue<Edge>(usableEdges);
 //				int cost = mstCost(startNode, endNode, remainingNodesList, edgesHeap, nodeEdges) + bestEdge.dist + secondBestEdge.dist;
 ////				System.out.println ("before cost: "+ cost);
 //				for (Node node : remainingNodesList)
 //					cost += 2 * nodeCosts[node.id];
 //				cost += nodeCosts[startNode.id];
 //				cost += nodeCosts[endNode.id];
 ////				System.out.println ("after cost: " + cost);
 //				if (cost > upper_bound)
 //				{
 //					remainingNodesList.add(oneTreeNode);
 //					return cost;
 //				}
 //				if (cost > lowerBound)
 //					lowerBound = cost;
 //				
 //				//calculate step size
 //				int sumSquareDiffs = 0;
 //				for (Node node : remainingNodesList)
 //				{
 ////					if (nodeEdges[node.id] < 1)
 ////						System.out.println ("this is wrong");
 //					int optimalNumEdges = (node == bestEdge.node1 || node == bestEdge.node2 || node == secondBestEdge.node1 || node == secondBestEdge.node2) ? 1 : 2;
 //					sumSquareDiffs += (optimalNumEdges-nodeEdges[node.id]) * (optimalNumEdges-nodeEdges[node.id]);
 //				}
 //				sumSquareDiffs += (1-nodeEdges[startNode.id]) * (1-nodeEdges[startNode.id]);
 //				sumSquareDiffs += (1-nodeEdges[endNode.id]) * (1-nodeEdges[endNode.id]);
 //					
 //				stepSize = (stepScale * (upper_bound - cost)) / sumSquareDiffs;
 //				if (stepSize < limit)
 //				{
 ////					System.out.println ("j: " + j);
 //					break;
 //				}
 //				
 //				for (Node node : remainingNodesList)
 //				{
 //					int optimalNumEdges = (node == bestEdge.node1 || node == bestEdge.node2 || node == bestEdge.node1 || node == bestEdge.node2) ? 1 : 2;
 //					nodeCosts[node.id] += (int)(stepSize * (optimalNumEdges - nodeEdges[node.id]));
 //				}
 //				nodeCosts[startNode.id] += (int)(stepSize * (1 - nodeEdges[startNode.id]));
 //				nodeCosts[endNode.id] += (int)(stepSize * (1 - nodeEdges[endNode.id]));
 //				
 //				stepScale *= stepChange;
 //			}
 //			
 //			//add stuff back in
 //			remainingNodesList.add(oneTreeNode);
 //			usableEdges.addAll(removedEdges);
 //		}
 //		return lowerBound;
 //	}
 //	
 //	private int mstCost(Node startNode, Node endNode, Collection<Node> remainingNodes, PriorityQueue<Edge> edgesQueue, int[] nodeEdges)
 //	{
 //		int totalCost = 0;
 //		int numEdges = 0;
 //		
 //		UnionFind unionFind = new UnionFind(remainingNodes, startNode, endNode, nodeEdges.length);
 //		/*for (Edge edge : requiredEdges)
 //		{
 //			Node root1 = unionFind.find(edge.node1), root2 = unionFind.find(edge.node2);
 //			if (root1 == root2)
 //				System.err.println ("We are not creating a tree here! minSpanningTreeCost");
 //			unionFind.union(root1, root2);
 //			totalCost += edge.dist;
 //		}*/
 //		while (numEdges < remainingNodes.size()+2-1 && edgesQueue.size() > 0)
 //		{
 ////			System.out.println ("in while loop");
 //			Edge e = edgesQueue.remove();
 //			Node root1 = unionFind.find(e.node1);
 //			Node root2 = unionFind.find(e.node2);
 ////			System.out.println ("finished finds");
 //			if (root1 != root2)
 //			{
 //				totalCost += e.cost();
 //				nodeEdges[e.node1.id]++;
 //				nodeEdges[e.node2.id]++;
 //				unionFind.union(root1, root2);
 //				numEdges++;
 //			}
 //		}
 //		
 //		return totalCost;
 //	}
 //}
