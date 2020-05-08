 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Scanner;
 
 
 /**
  * The headquarters class
  * @author Administrator
  *
  */
 public class GBE {
 
 	//The input for this class
 	Joints jointsIndex = new Joints();
 	GRAIL grailIndex = new GRAIL();
 	Graph dataGraph = new Graph();
 	NeighborHood nh = new NeighborHood();
 	
 	//Mark the visited vertices for divide the graph and floodfill
 	HashSet<Integer> visited = new HashSet<Integer>();
 
 	//Store all the super edges
 	LinkedList<SuperEdge> superEdges = new LinkedList<SuperEdge>();
 	
 	public static void main(String[] args) throws Exception {
 		GBE test = new GBE();
 		test.jointsIndex.loadEdgeIndexFromFile("P2PEdges");
 		test.jointsIndex.loadJointIndexFromFile("P2PJoints");				
 		System.out.println("Finish Loading Index....");
 		
 		test.dataGraph.loadGraphFromFile("P2P");
 		System.out.println("Finish Loading Data Graph....");
 
 		test.nh.Encode(test.dataGraph);
 		System.out.println("Finish Building NeighborHood Index");		
 
 		System.out.println("Start Querying");
 		test.Query("querypattern.txt");
 		System.out.println("Done.");
 	}
 		
 	public void Query(String fileName) throws Exception {
 		long startTime = System.nanoTime();
 		//Divide the query pattern into subgraphs and super edges
 		List<Graph> sG =  DivideGraph(fileName);
 		
 		//Sort the graph list such that larger graph is going to be tested earlier
 		Collections.sort(sG);
 		
 		//Get the matching candidates for each subgraph
 		SubQuery sQ = new SubQuery(dataGraph, sG, jointsIndex, nh);
 		HashMap<Graph, LinkedList<MatchedCandidates>> subResult = sQ.Execute();
 		
 //		for (Graph gi : subResult.keySet()) {
 //			System.out.println("The component:");
 //			gi.print();
 //			System.out.println("The matches:");
 //			for (MatchedCandidates mi : subResult.get(gi))
 //				mi.Print();
 //			System.out.println();
 //		}
 		
 		//Try to release the memory
 //		jointsIndex = null;
 //		nh = null;
 		
 		//Determine the components on each super edge
 		for (SuperEdge se : superEdges) {
 			//Currently use this two marker to check errors
 			boolean sourceComponentAssigned = false;
 			boolean targetComponentAssigned = false;
 			
 			for (Graph g : sG) {
 				if (g.attributes.containsKey(se.source)) {
 					if (!sourceComponentAssigned) {
 						sourceComponentAssigned = true;
 						se.sourceComponent = g;
 					}
 					else
 						System.out.println("Error!");
 				}
 				
 				if (g.attributes.containsKey(se.target)) {
 					if (!targetComponentAssigned) {
 						targetComponentAssigned = true;
 						se.targetComponent = g;
 					}
 					else {
 						System.out.println("Error!");
 					}
 				}
 			}
 		}
 				
 		while (superEdges.size() > 0) {
 			//First check whether there is any super edge connecting the same component now
 			for (SuperEdge se : superEdges) {
 				if (se.sourceComponent.equals(se.targetComponent)) {
 					LinkedList<MatchedCandidates> mc = subResult.get(se.sourceComponent);
 					ProcessFirstCategorySE(se, mc);		
 					se.processed = true;
 				}				
 			}
 			
 			Iterator<SuperEdge> it = superEdges.iterator();
 			while (it.hasNext()) {
 				SuperEdge t = it.next();
 				if (t.processed)
 					it.remove();
 			}
 			
 			it = superEdges.iterator();
 			if (it.hasNext()) {
 				SuperEdge t = it.next();
 				
 				//Determine the depths
 				int forwardDepth = t.length / 2;
 				int backwardDepth = t.length - forwardDepth;
 				
 				//Process the next super edge
 				LinkedList<MatchedCandidates> mcSource = subResult.get(t.sourceComponent);
 				LinkedList<MatchedCandidates> mcTarget = subResult.get(t.targetComponent);
 				
 				//Store the buffered BFS
 				HashMap<Integer, ArrayList<HashMap<Integer, QueueNode>>> fBFS = new HashMap<Integer, ArrayList<HashMap<Integer, QueueNode>>>();
 				HashMap<Integer, ArrayList<HashMap<Integer, QueueNode>>> bBFS = new HashMap<Integer, ArrayList<HashMap<Integer, QueueNode>>>();			
 
 				HashSet<Integer> visitedMapping = new HashSet<Integer>();				
 				
 				//BFS for each of the candidate vertices
 				for (MatchedCandidates mci : mcSource) {
 					Integer mt = mci.mapping.get(t.source);
 					if (!visitedMapping.contains(mt))
 						fBFS.put(mt, ForwardBFS(mt, forwardDepth));
 				}
 				
 				visitedMapping.clear();			
 				for (MatchedCandidates mci : mcTarget) {
 					Integer mt = mci.mapping.get(t.target);
 					if (!visitedMapping.contains(mt))
 						bBFS.put(mt, BackwardBFS(mt, backwardDepth));
 				}
 				
 				//Mark for whether we find a match
 				boolean haveMatch = false;
 				
 				//Make a combination copy of the two components
 				//In case that there is a match, we need to update the result table
 				Graph combinedComponent = new Graph(t.sourceComponent);
 				combinedComponent.Combine(t.targetComponent);
 				
 				LinkedList<MatchedCandidates> newMC = new LinkedList<MatchedCandidates>();
 				
 				for (MatchedCandidates mci : mcSource) {
 					Integer m1 = mci.mapping.get(t.source);
 					ArrayList<HashMap<Integer, QueueNode>> fB = fBFS.get(m1);
 					for (MatchedCandidates mcj : mcTarget) {
 						Integer m2 = mcj.mapping.get(t.target);
 						ArrayList<HashMap<Integer, QueueNode>> bB = bBFS.get(m2);
 						List<String> ps = BuildPath(fB, bB);
 
 						//If there is path between the two vertices
 						if (ps.size() > 0) {
 							haveMatch = true;
 							MatchedCandidates temp = new MatchedCandidates(mci);
 							temp.Combine(mcj);
 							temp.paths.put(m1 + " " + m2, ps);
 							newMC.add(temp);						
 						}
 					}
 				}
 								
 				it.remove();
 
 				if (haveMatch) {
 					//Update the source and target component of each super edge					
 					Iterator<SuperEdge> itc = superEdges.iterator();
 					while (itc.hasNext()) {
 						SuperEdge ti = itc.next();
 						if (ti.sourceComponent.equals(t.sourceComponent))
 							ti.sourceComponent = combinedComponent;
 						if (ti.sourceComponent.equals(t.targetComponent))
 							ti.sourceComponent = combinedComponent;
 						if (ti.targetComponent.equals(t.sourceComponent))
 							ti.targetComponent = combinedComponent;
 						if (ti.targetComponent.equals(t.targetComponent))
 							ti.targetComponent = combinedComponent;							
 					}
 					subResult.remove(t.sourceComponent);
 					subResult.remove(t.targetComponent);
 					
 					//Update the result table
 					subResult.put(combinedComponent, newMC);
 				}
 				else {
 					//It means that there is no valid paths between the two vertices
 					//and thus there is no matching for the query pattern
 					return;
 				}				
 			}
 		}
 
 		long endTime = System.nanoTime();
 		long duration = endTime - startTime;		
 		
 		if (subResult.keySet().size() > 1) {
 			System.out.println("Error");
 			for (Graph i : subResult.keySet()) {
 				LinkedList<MatchedCandidates> matches = subResult.get(i);
 				System.out.println("Total matches: " + matches.size());
 				for (MatchedCandidates mi : matches)
 					mi.Print();
 				System.out.println();				
 			}			
 		}
 		else {
 			for (Graph i : subResult.keySet()) {
 				LinkedList<MatchedCandidates> matches = subResult.get(i);
 				System.out.println("Total matches: " + matches.size());
 				for (MatchedCandidates mi : matches)
 					mi.Print();
 				System.out.println();				
 			}
 			System.out.println("Total running time: " + duration);
 		}
 	}
 	
 	/**
 	 * This method is used to process the first category super edges
 	 * The main function of this method is to prune the false positive matching candidate using the paths info
 	 * @param se
 	 * @param mc
 	 * @throws Exception
 	 */
 	public void ProcessFirstCategorySE(SuperEdge se, LinkedList<MatchedCandidates> mc) throws Exception {
 		Iterator<MatchedCandidates> it = mc.iterator();
 		if (it != null) {
 			while (it.hasNext()) {
 				MatchedCandidates ca = it.next();
 				Integer sourceInData = ca.mapping.get(se.source);
 				Integer targetInData = ca.mapping.get(se.target);
				if (!sourceInData.equals(targetInData)) {
					List<String> pathReturned = BFSBuildShortestPath(sourceInData, targetInData, se.length);
					if (pathReturned.size() > 0) {
						ca.paths.put(sourceInData + " " + targetInData, pathReturned);
					}
					else {
						it.remove();
					}
 				}
 			}					
 		}
 		se.processed = true;		
 	}
 	
 	/**
 	 * This method is used to remove the super edge from the pattern
 	 * Store the necessary information for rebuilding the graph
 	 * @throws FileNotFoundException 
 	 */
 	public List<Graph> DivideGraph(String fileName) throws FileNotFoundException {
 		Graph g = new Graph();
 		superEdges.clear();
 		
 		//Since the format of graph pattern is different from data graph
 		//We still need to load it from file first
 		File file = new File(fileName);
 		Scanner sc = new Scanner(file);
 		
 		while (sc.hasNext()) {
 			//Get the ID and attributes line
 			String IdLine = sc.nextLine();
 
 			//Decode it
 			String[] s = IdLine.split(",");
 			
 			//Parse the ID first
 			Integer ID = Integer.parseInt(s[0]);
 			
 			//Initialize the attributes list
 			g.attributes.put(ID, new LinkedList<String>());
 			
 			//Insert all the attributes
 			for (int i = 1; i < s.length; i++)
 				g.attributes.get(ID).add(s[i]);
 			
 			//Set the primary attribute
 			if (s.length > 1) {
 				g.primaryAttribute.put(ID, s[1]);
 			}
 			else
 				g.primaryAttribute.put(ID, s[0]);
 			
 			//Process the neighbors
 			String neighborLine = sc.nextLine();
 			
 			//Decode it
 			s = neighborLine.split(",");
 			
 			//Initialize the children adjacency list
 			if (!g.children.containsKey(ID))
 				g.children.put(ID, new LinkedList<Integer>());
 			if (!g.parents.containsKey(ID))
 				g.parents.put(ID, new LinkedList<Integer>());
 			
 			if (s.length > 1) {//If the current vertex has at least one child, the length should be at least 2
 				for (int i = 0; i < s.length; i += 2) {
 					int l = Integer.parseInt(s[i + 1]);
 					
 					if (l > 1) {  //If current edge is a super edge
 						Integer t = Integer.parseInt(s[i]);
 						superEdges.add(new SuperEdge(ID, t, l));
 					} 
 					else { //normal edge
 						int tN = Integer.parseInt(s[i]);
 						
 						g.children.get(ID).add(tN);
 
 						//Also for the parents
 						if (!g.parents.containsKey(tN))
 							g.parents.put(tN, new LinkedList<Integer>());
 						g.parents.get(tN).add(ID);												
 					}
 				}
 			}
 		}
 		
 		sc.close();
 		
 		//The return result
 		List<Graph> re = new LinkedList<Graph>();
 		visited.clear();
 		
 		//Start from each vertex to floodfill
 		for (Integer i : g.children.keySet()) {
 			if (!visited.contains(i) && (g.children.get(i).size() > 0 || g.parents.get(i).size() > 0)) {				
 				Graph in = new Graph();
 
 				//DFS
 				FloodFill(i, in, g);
 				
 				//Calculate the degree first
 				in.calculateDegree();
 				
 				//Push into the return list
 				re.add(in);
 			}
 		}
 		
 		return re;
 	}
 
 	
 	/**
 	 * Using flood fill to get all the connected components
 	 * @param g
 	 * @return
 	 */
 	public void FloodFill(Integer source, Graph re, Graph g) {
 		visited.add(source);
 		
 		//Visit every child
 		for (Integer i : g.children.get(source)) {
 			re.addEdge(source, g.attributes.get(source), i, g.attributes.get(i));
 			if (!visited.contains(i)) {				
 				//DFS
 				FloodFill(i, re, g);
 			}			
 		}
 
 		//Visit every parent
 		for (Integer i : g.parents.get(source)) {
 			re.addEdge(i, g.attributes.get(i), source, g.attributes.get(source));
 			if (!visited.contains(i)){				
 				FloodFill(i, re, g);				
 			}			
 		}
 	}
 	
 	/**
 	 * The node in the queue for BFS 
 	 * We need to record the depth of each vertex in the queue
 	 * Therefore, we have to make this class
 	 */
 	class QueueNode {
 		Integer VertexID;
 		int depth;
 		
 		LinkedList<QueueNode> pathParent;
 		LinkedList<QueueNode> pathChild;
 		
 		QueueNode(Integer id, int d) {
 			VertexID = id;
 			depth = d;
 			pathParent = new LinkedList<QueueNode>();
 			pathChild = new LinkedList<QueueNode>();
 		}		
 	}
 
 	public List<String> BFSBuildShortestPath(Integer source, Integer target, int length) throws Exception {
 		List<String> re = new LinkedList<String>();
 
 		// //If the source vertex cannot reach the target vertex, halt immediately
 		// if (!grailIndex.CheckContainment(source, target))
 		// return;
 
 		//If the two vertices pass the first two exams, we can start to BFS
 		//In order to improve the performance, we use bidirectional BFS
 		Queue<QueueNode> forward = new LinkedList<QueueNode>();
 		Queue<QueueNode> backward = new LinkedList<QueueNode>();
 
 		//Determine the forward steps and backward steps
 		int forwardSteps = length / 2;
 		int backwardSteps = length - forwardSteps;
 
 		//Initialize the two queues
 		QueueNode s = new QueueNode(source, 0);
 		QueueNode t = new QueueNode(target, 0);
 
 		forward.add(s);
 		backward.add(t);
 
 		HashMap<Integer, QueueNode> forwardVisited = new HashMap<Integer, QueueNode>();
 		HashMap<Integer, QueueNode> backwardVisited = new HashMap<Integer, QueueNode>();
 
 		forwardVisited.put(s.VertexID, s);
 
 		backwardVisited.put(t.VertexID, t);
 
 		HashSet<Integer> intersections = new HashSet<Integer>();
 		
 		
 		while (forward.size() > 0 || backward.size() > 0) {
 
 			//Each time we pop a node from the forward queue and backward queue
 			if (forward.size() > 0) {
 				QueueNode head = forward.poll();	
 
 				if (head.depth + 1 <= forwardSteps && !backwardVisited.containsKey(head.VertexID)) {
 
 					for (Integer i : dataGraph.children.get(head.VertexID)) {
 						boolean flag = false;
 						//Indicate whether we have found a new path
 						boolean fVFlag = false;
 
 						QueueNode temp = new QueueNode(i, head.depth + 1);
 						temp.pathParent.add(head);							
 
 						if (!forwardVisited.containsKey(i)) {
 							forwardVisited.put(i, temp);
 							fVFlag = true;
 						}
 						else {
 							QueueNode q = forwardVisited.get(i);
 
 							//The depth of the current element should be equal to head.depth + 1
 							//If the depth is equal to the shortest one, we add this node to the list
 							//However, since this node is already visited before, we don't add it to the BFS queue
 							//Otherwise, we will expand the same vertex multiple times
 							if (head.depth + 1 == q.depth) {
 								q.pathParent.add(head);
 								fVFlag = true;
 							}
 							
 							//Since this vertex has been visited before
 							//No matter what we don't need to insert it into the queue again
 							flag = true;
 						}
 						
 						if (backwardVisited.containsKey(i) && fVFlag) {
 							flag = true;
 
 							if (!intersections.contains(i))
 								intersections.add(i);
 								
 						}	
 
 						if (!flag)
 							forward.add(temp);
 
 					}
 				}
 
 			}
 
 			//The backward search
 			if (backward.size() > 0) {
 				QueueNode tail = backward.poll();	
 
 				if (tail.depth + 1 <= backwardSteps && !forwardVisited.containsKey(tail.VertexID)) {
 					
 					for (Integer i : dataGraph.parents.get(tail.VertexID)) {
 						boolean flag = false;
 						//Find a path
 
 						QueueNode temp = new QueueNode(i, tail.depth + 1);
 						temp.pathChild.add(tail);
 
 						//Indicate whether we have found a new path
 						boolean fVFlag = false;
 
 						if (!backwardVisited.containsKey(i)) {
 							backwardVisited.put(i, temp);
 							fVFlag = true;
 						}
 						else {
 							QueueNode q =  backwardVisited.get(i);
 
 							if (tail.depth + 1 == q.depth) {
 								q.pathChild.add(tail);
 								fVFlag = true;
 							}
 							
 							flag = true;
 						}
 
 						
 						if (forwardVisited.containsKey(i) && fVFlag) {
 							flag = true;
 		
 							if (!intersections.contains(i))
 								intersections.add(i);
 															
 						}
 
 						if (!flag)
 							backward.add(temp);
 					}
 				}	
 			}	
 		}
 		
 		for (Integer i : intersections) {
 			//Build the paths here
 			QueueNode fNode = forwardVisited.get(i);
 			List<String> parentPaths = new LinkedList<String>();
 			DFSP(fNode, "", parentPaths);
 			
 			QueueNode bNode = backwardVisited.get(i);
 			List<String> childPaths = new LinkedList<String>();
 			DFSC(bNode,"", childPaths);
 			
 			for (String s1 : parentPaths) 
 				for (String s2 : childPaths) {
 					String p =  s1 + i + s2;
 					re.add(p);
 				}							
 		}
 
 		return re;
 	}
 	
 	/**
 	 * The forward breadth first search
 	 * @param source
 	 * @param depth
 	 * @return
 	 */
 	public  ArrayList<HashMap<Integer, QueueNode>> ForwardBFS(Integer source, int depth) {
 		ArrayList<HashMap<Integer, QueueNode>> result = new ArrayList<HashMap<Integer, QueueNode>>(depth + 1);
 		
 		//The source vertex
 		QueueNode s = new QueueNode(source, 0);
 
 		//The breadth first search queue
 		Queue<QueueNode> forward = new LinkedList<QueueNode>();
 		forward.add(s);
 		
 		//The mark hashmap
 		HashMap<Integer, QueueNode> visited = new HashMap<Integer, QueueNode>();
 		
 		visited.put(s.VertexID, s);
 		
 		while (forward.size() > 0) {
 			QueueNode current = forward.poll();
 			
 			if (current.depth + 1 <= depth && dataGraph.children.containsKey(current.VertexID)) {
 				for (Integer i : dataGraph.children.get(current.VertexID)) {					
 					if (!visited.containsKey(i)) {
 						//Initialize the node
 						QueueNode temp = new QueueNode(i, current.depth + 1);
 						temp.pathParent.add(current);
 						forward.add(temp);
 						visited.put(i, temp);
 					}
 					else {
 						int shortestdepth = visited.get(i).depth;
 						if (current.depth + 1 == shortestdepth) {
 							visited.get(i).pathParent.add(current);							
 						}
 					}
 				}
 			}
 		}
 		
 		for (int i = 0; i <= depth; i++)
 			result.add(new HashMap<Integer, QueueNode>());
 		
 		for (Integer i : visited.keySet()) {
 			QueueNode c = visited.get(i);
 			result.get(c.depth).put(i, c);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * The backward breadth first earch
 	 * @param target
 	 * @param depth
 	 * @return 
 	 */
 	
 	public ArrayList<HashMap<Integer, QueueNode>> BackwardBFS(Integer target, int depth) {
 		ArrayList<HashMap<Integer, QueueNode>> result = new ArrayList<HashMap<Integer, QueueNode>>(depth + 1);
 		
 		QueueNode t = new QueueNode(target, 0);
 		
 		Queue<QueueNode>  backward = new LinkedList<QueueNode>();
 		backward.add(t);
 		
 		HashMap<Integer, QueueNode> visited = new HashMap<Integer, QueueNode>();
 		visited.put(t.VertexID, t);
 		
 		while (backward.size() > 0) {
 			QueueNode current = backward.poll();
 			
 			if (current.depth + 1 <= depth && dataGraph.parents.containsKey(current.VertexID)) {
 				for (Integer i : dataGraph.parents.get(current.VertexID)) {
 					if (!visited.containsKey(i)) {
 						QueueNode temp = new QueueNode(i, current.depth + 1);
 						temp.pathChild.add(current);
 						backward.add(temp);
 						visited.put(i, temp);
 					}
 					else {
 						int shortestdepth = visited.get(i).depth;
 						if (current.depth + 1 == shortestdepth) {
 							visited.get(i).pathChild.add(current);
 						}
 					}
 				}
 			}
 		}
 		
 		for (int i = 0; i <= depth; i++)
 			result.add(new HashMap<Integer, QueueNode>());
 		
 		for (Integer i : visited.keySet()) {
 			QueueNode c = visited.get(i);
 			result.get(c.depth).put(i, c);
 		}
 		
 		return result;
 	}
 	
 
 	/**
 	 * Given the breadth first search of source and target, try to build the paths
 	 * @param source
 	 * @param target
 	 * @return
 	 */
 	public List<String> BuildPath(ArrayList<HashMap<Integer, QueueNode>> source, ArrayList<HashMap<Integer, QueueNode>> target) {
 		boolean found = false;
 		
 		List<String> result = new LinkedList<String>();
 		
 		search:
 		for (int i = 0; i < source.size(); i++) {
 			//The forward layers
 			HashMap<Integer, QueueNode> forwardLevel = source.get(i);			
 			
 			for (int j = target.size() - 1; j >= 0; j--) {
 				//The backward layers
 				HashMap<Integer, QueueNode> backwardLevel = target.get(j);
 				
 				for (Integer k : forwardLevel.keySet())
 					if (backwardLevel.containsKey(k)) {						
 						found = true;
 						
 						//Build the paths here
 						QueueNode fNode = forwardLevel.get(k);
 						List<String> parentPaths = new LinkedList<String>();
 						DFSP(fNode, "", parentPaths);
 						
 						QueueNode bNode = backwardLevel.get(k);
 						List<String> childPaths = new LinkedList<String>();
 						DFSC(bNode,"", childPaths);
 						
 						//Connect them
 						for (String s1 : parentPaths) 
 							for (String s2 : childPaths) {
 								String p =  s1 + k + s2;
 								result.add(p);
 							}
 					}
 				if (found) 
 					break search;
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Recursively build the path from source to the intersection point
 	 * @param node
 	 * @param s
 	 * @param re
 	 */
 	public void DFSP(QueueNode node, String s, List<String> re) {
 		if (node.pathParent.size() == 0) {
 			re.add(s);
 			return;
 		}	
 		for (QueueNode q : node.pathParent) {
 			DFSP(q,  q.VertexID + "," + s, re);			
 		}
 			
 	}
 	
 	/**
 	 * Recursively build the path from the inserction point to the target
 	 * @param node
 	 * @param s
 	 * @param re
 	 */
 	public void DFSC(QueueNode node, String s, List<String> re) {
 		if (node.pathChild.size() == 0) {
 			re.add(s);
 			return;
 		}
 		for (QueueNode q : node.pathChild) {
 			DFSC(q, s + "," + q.VertexID, re);
 		}
 	}
 }
