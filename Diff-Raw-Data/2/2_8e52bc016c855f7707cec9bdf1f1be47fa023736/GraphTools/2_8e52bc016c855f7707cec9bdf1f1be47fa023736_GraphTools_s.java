 /**
  *
  */
 package se.iroiro.md.hangeul;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import se.iroiro.math.IntMatrix;
 import se.iroiro.math.SubgraphMapper;
 import se.iroiro.md.graph.Coordinate;
 import se.iroiro.md.graph.Graph;
 import se.iroiro.md.graph.XYEdge;
 import se.iroiro.md.graph.XYNode;
 import se.iroiro.md.graph.simple.SimpleGraph;
 import se.iroiro.md.graph.simple.SimpleXYEdge;
 import se.iroiro.md.graph.simple.SimpleXYNode;
 import se.iroiro.md.imagereader.GraphMatrix;
 
 /**
  * The <code>GraphTools</code> contains methods for manipulating and creating graphs of lines etc.
  * @author j
  *
  */
 public class GraphTools {
 
 	/**
 	 * Returns a graph created from data in the specified graph matrix.
 	 * @param gm	the matrix to read
 	 * @return	a list of lines found in the graph matrix
 	 */
 	@SuppressWarnings("unchecked")
 	public static Graph<Object,Line> graphMatrixToGraph(GraphMatrix gm){	//TODO Check result
 		Graph<Object,Line> graph = new SimpleGraph<Object,Line>();
 		XYNode<?,?>[][] nodematrix = new XYNode<?,?>[gm.getY_size()][gm.getX_size()];
 		for(int y = 0; y < gm.getY_size(); y++){
 			for(int x = 0; x < gm.getX_size(); x++){
 				XYNode<Object,Line> node = graph.newNode();
 				node.setPosition(x,-y);
 				if(gm.getCell(x,y)){
 					nodematrix[y][x] = node;
 					graph.addNode(node);
 					for(int e = 2; e < 6 ; e++){
 						if(gm.getEdge(x,y,e)){
 							XYNode<Object,Line> neighbour = (XYNode<Object,Line>) nodematrix[gm.getNeighbourY(x,y,e)][gm.getNeighbourX(x,y,e)];
 							XYEdge<Object,Line> edge = graph.newEdge(node,neighbour);
 							graph.addEdge(edge);
 						}
 					}
 				}
 			}
 		}
 		return graph;
 	}
 
 	/**
 	 * Returns a list of lines created from data in the specified graph.
	 * If there are no §s in the graph, the method returns null.
 	 * @param g	the graph to read
 	 * @return	a list of lines found in the graph, or null if there are no lines to be found
 	 */
 	public static List<Line> graphToLines(Graph<Object,Line> g){	//TODO Check result
 		List<Line> lines = null;
 		if(g.getEdges().size() > 0){	// if there is a graph
 			lines = new ArrayList<Line>();
 			for(XYEdge<Object,Line> e : g.getEdges()){	// loop through edges in graph
 				if(e.getPiggybackObject() == null){	// if the edge does not already belong to a line
 					lines.add(createLine(e));	// find and add the line which the edge is part of
 				}
 			}
 		}
 		return lines;
 	}
 
 	/**
 	 * Creates the line that <code>startEdge</code> is part of. <code>startEdge</code> can be any edge in the line.
 	 * If the start edge is already part of a line, this method will return null.
 	 * @param startEdge	the edge to start from
 	 * @return	the new line
 	 */
 	private static Line createLine(XYEdge<Object,Line> startEdge){	//TODO Check result
 		Line line = null;
 		if(startEdge.getPiggybackObject() == null){	// if start edge is not already part of line
 			line = new Line();	// make a new line
 			line.addEdge(startEdge);	// add the start edge to the line
 			startEdge.setPiggybackObject(line);	// set the line of the edge to the new line
 
 			// search edges to the left of start edge
 			XYEdge<Object,Line> left = nextEdge(startEdge, true);	// find the left connecting edge
 			while(left != null && left != startEdge && left.getPiggybackObject() == null){
 				line.addEdgeFirst(left);
 				left.setPiggybackObject(line);
 				left = nextEdge(left, true);
 			}
 			// search edges to the right of start edge
 			XYEdge<Object,Line> right = nextEdge(startEdge, false);	// find the right connecting edge
 			while(right != null && right != startEdge && right.getPiggybackObject() == null){
 				line.addEdge(right);
 				right.setPiggybackObject(line);
 				right = nextEdge(right, false);
 			}
 
 			Iterator<XYEdge<Object,Line>> it = line.getGraph().getEdges().iterator();
 			Graph<Object,Line> linegraph = line.getGraph();
 			XYEdge<Object,Line> e = null;	// add the nodes as well
 			while(it.hasNext()){			//TODO Check result.
 				e = it.next();
 				linegraph.addNode(e.getFrom());
 			}
 			if(e != null && !linegraph.getNodes().contains(e.getTo())){
 				linegraph.addNode(e.getTo());
 			}
 		}
 		return line;
 	}
 
 	/**
 	 * Returns the edge connecting with the edge specified. If <code>searchBackwards</code> is <code>true</code>,
 	 * the method will return the edge connecting with this node's from-node,
 	 * otherwise it will return the edge connecting with the to-node.
 	 * If there is no connecting edge, or if the connecting edge does not belong to the same line,
 	 * the method will return null.
 	 * This method will also reverse edges which are oriented <emphasis>against</emphasis> the stream.
 	 * @param startEdge	the edge for which to find a neighbour
 	 * @param searchBackwards	if <code>true</code>, from-edge will be found instead of to-edge.
 	 * @return	the edge connecting with the specified edge, in the specified direction
 	 */
 	private static XYEdge<Object,Line> nextEdge(XYEdge<Object,Line> startEdge, boolean searchBackwards){
 		XYEdge<Object,Line> edge = null;
 		XYNode<Object,Line> node;
 		if(!searchBackwards){
 			node = startEdge.getTo();
 		}else{
 			node = startEdge.getFrom();
 		}
 		if(node.getDegree() == 2){
 			Iterator<XYEdge<Object,Line>> it = node.getEdges().iterator();
 			while(it.hasNext() && (edge == startEdge || edge == null)) edge = it.next();
 			if(edge == startEdge){
 				edge = null;
 			}
 			if(edge != null && (edge.getFrom() == startEdge.getFrom() || edge.getTo() == startEdge.getTo())) edge.reverse();
 		}
 		return edge;
 	}
 
 	/**
 	 * Returns a list of groups created from data in the specified list of lines.
 	 * A collection of lines that are connected to each other form one group.
 	 * If the specified list contains no lines, this method returns null.
 	 * @param lines	the list of lines to read
 	 * @return	a list of groups
 	 */
 	public static List<LineGroup> linesToLineGroups(List<Line> lines) {
 		if(lines == null || lines.size() == 0) return null;
 		List<LineGroup> groups = new ArrayList<LineGroup>();
 		for(Line l : lines){
 			if(l.getGroup() == null){
 				LineGroup lg = createLineGroup(l);
 				setEdgePorts(lg);
 				groups.add(lg);
 			}
 		}
 		return groups;
 	}
 
 
 	/**
 	 * Creates a line group and adds the specified line and its neighbours (and their neighbours and so on) to it.
 	 * If the specified line is already part of a group, and empty group is returned.
 	 * @param startLine	the line to create a group for
 	 * @return	the group created
 	 */
 	private static LineGroup createLineGroup(Line startLine) {	//TODO Check result
 		LineGroup group = new LineGroup();
 		Stack<Line> stack = new Stack<Line>();
 		stack.add(startLine);
 		while(stack.size() > 0){
 			Line l = stack.pop();
 			if(l.getGroup() == null){
 				group.add(l);
 				l.setGroup(group);
 				stack.addAll(getConnectingLines(l));
 			}
 		}
 		for(Line l : group.getMap().keySet()){	// connect each line to its adjacent lines
 			List<Line> connecting = getConnectingLines(l);
 			for(Line c : connecting){
 				if(!group.isConnected(l,c)){
 					group.connect(l,c);
 				}
 			}
 		}
 		return group;
 	}
 
 	/**
 	 * Returns a list of all lines adjacent to the specified line.
 	 * @param l	the line for which to find neighbours
 	 * @return	a list of all lines adjacent to the specified line
 	 */
 	private static List<Line> getConnectingLines(Line l){	//TODO Check result
 		List<Line> connecting = new ArrayList<Line>();
 		List<XYNode<Object,Line>> nodes = l.getGraph().getNodes();
 		Line pbo;
 		for(XYEdge<Object,Line> e : nodes.get(0).getEdges()){
 			pbo = e.getPiggybackObject();
 			if(pbo != null && pbo != l) connecting.add(pbo);
 		}
 		for(XYEdge<Object,Line> e : nodes.get(nodes.size()-1).getEdges()){
 			pbo = e.getPiggybackObject();
 			if(pbo != null && pbo != l) connecting.add(pbo);
 		}
 		return connecting;
 	}
 
 	/**
 	 * Returns the end of the specified line which is closest to the top left.
 	 * For horizontal lines, this is always the left end.
 	 * For vertical lines, this is always the top end.
 	 * For diagonal lines, always the top end.
 	 * If the top left end can not be determined, <code>null</code> is returned.
 	 * @param l	the line to check
 	 * @return	the top left end point of the specified line or <code>null</code>
 	 */
 	private static XYNode<Object,Line> getTopLeftEnd(Line l){
 		Coordinate from = l.getFrom().getPosition();
 		Coordinate to = l.getTo().getPosition();
 		switch(l.getType()){
 		case HORIZONTAL:
 			return from.getX() < to.getX() ? l.getFrom() : l.getTo();
 		case VERTICAL:
 		case DIAGONAL_LEFT:
 		case DIAGONAL_RIGHT:
 			return from.getY() > to.getY() ? l.getFrom() : l.getTo();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the end-node of the specified line which is not equal to the specified node.
 	 * If the specified node is not an end-node of the specified line, <code>null</code> is returned.
 	 * If both end nodes are the same node, this node is returned.
 	 * @param l	the line to check
 	 * @param n	the node to check
 	 * @return	the node opposite to the specified one or <code>null</code>
 	 */
 	private static XYNode<Object,Line> getOppositeEnd(Line l, XYNode<Object,Line> n){
 		if(n == l.getFrom()){
 			return l.getTo();
 		}else if(n == l.getTo()){
 			return l.getFrom();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns <code>true</code> if the specified line has the specified node as its start point or end point.
 	 * @param l	the line to check
 	 * @param n	the node to find
 	 * @return	<code>true</code> if the specified line has the specified node as its start point or end point
 	 */
 	private static boolean isEndOf(Line l, XYNode<Object,Line> n){
 		return n == l.getFrom() || n == l.getTo();
 	}
 
 	/**
 	 * Sets the edge ports of all the edges in the specified line group.
 	 * @param l	the line group
 	 */
 	private static void setEdgePorts(LineGroup lg){
 		for(XYEdge<Line,LineGroup> e : lg.getGraph().getEdges()){	// loop through all edges in the line group graph
 			Line from = e.getFrom().getPiggybackObject();	// get the line which the edge connects from
 			Line to = e.getTo().getPiggybackObject();		// get the line which the edge connects to
 			e.setTailPort(getPort(from,to));
 			e.setHeadPort(getPort(to,from));
 		}
 	}
 
 	/**
 	 * Returns which port of line <code>a</code> connects with line <code>b</code>.
 	 * @param a	the line for which to find the port
 	 * @param b	the connecting line
 	 * @return	which port of line <code>a</code> connects with line <code>b</code>
 	 */
 	private static int getPort(Line a, Line b){
 		final int  C = 0;
 		final int  E = 1;
 		final int NE = 2;
 		final int  N = 3;
 		final int NW = 4;
 		final int  W = 5;
 		final int SW = 6;
 		final int  S = 7;
 		final int SE = 8;
 		int port = C;	// default port is Centre
 		XYNode<Object,Line> tl = getTopLeftEnd(a);	// get the top left node
 		XYNode<Object,Line> op = getOppositeEnd(a,tl);	// get the opposite node
 		switch(a.getType()){	// check the line type of the from-line
 			case HORIZONTAL:	// if it is horizontal
 				if(isEndOf(b,tl)){	// check if the to-line also has this node ( = found the connecting node)
 					port = W;
 				}else if(isEndOf(b,op)){	// otherwise, if the to-line instead has the opposite node ("bottom right" node)
 					port = E;
 				}
 				break;
 			case VERTICAL:	// if it is vertical
 				if(isEndOf(b,tl)){	// check if the to-line also has this node ( = found the connecting node)
 					port = N;
 				}else if(isEndOf(b,op)){	// otherwise, if the to-line instead has the opposite node ("bottom right" node)
 					port = S;
 				}
 				break;
 			case DIAGONAL_LEFT:	// if it is left-diagonal
 				if(isEndOf(b,tl)){	// check if the to-line also has this node ( = found the connecting node)
 					port = NE;
 				}else if(isEndOf(b,op)){	// otherwise, if the to-line instead has the opposite node ("bottom right" node)
 					port = SW;
 				}
 				break;
 			case DIAGONAL_RIGHT:	// if it is right-diagonal
 				if(isEndOf(b,tl)){	// check if the to-line also has this node ( = found the connecting node)
 					port = NW;
 				}else if(isEndOf(b,op)){	// otherwise, if the to-line instead has the opposite node ("bottom right" node)
 					port = SE;
 				}
 				break;
 			}
 		return port;
 	}
 
 	/**
 	 * Returns <code>true</code> if the two specified line groups have the same structure.
 	 * Line type and edge ports are compared, line lengths are ignored.
 	 * If either argument is <code>null</code>, <code>false</code> is returned.
 	 * @param one	line group one
 	 * @param two	line group two
 	 * @return	<code>true</code> if the two specified line groups have the same structure
 	 */
 	public static boolean sameStructure(LineGroup one, LineGroup two) {
 		if(one != null && two != null){
 			if(one.getGraph().getNodes().size() != two.getGraph().getNodes().size()) return false;	// not same if node count differs
 			for(Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> map : getNodeMappings(one,two)){
 				if(two.getGraph().getNodes().size() == map.size()) return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * TODO write proper javadoc
 	 * Returns -1 if groups do not share the same structure. Otherwise, similarity index.
 	 * Zero means that line groups are the same. Higher value means more different.
 	 * @param one	line group one
 	 * @param two	line group two
 	 * @return	similarity index, lower value = more similar, but -1 = not similar at all
 	 */
 	public static double similarityIndex(LineGroup one, LineGroup two){
 		if(one.getMap().size() != two.getMap().size()) return -1;	// if not same line count, not similar.
 		Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> mapping = getBestNodeMapping(one,two);
 		if(mapping == null || mapping.size() == 0) return -1;	// return -1 if not similar
 		if(two.getMap().size() != mapping.size()) return -1;	// return -1 if the whole graph could be matched
 		return similarityIndex(mapping);
 	}
 
 	/**
 	 * TODO write proper javadoc
 	 * Similarity index for entire mapping.
 	 * @param map
 	 * @return
 	 */
 	public static double similarityIndexSum(Map<LineGroup,LineGroup> map){
 		LineGroup two;
 		double sim;
 		double simSum = 0;
 		for(LineGroup one : map.keySet()){
 			two = map.get(one);
 			sim = similarityIndex(one,two);
 			if(sim == -1) return -1;
 			simSum += sim;
 		}
 		return simSum;
 	}
 
 	/**
 	 * TODO fix javadoc
 	 * Scans the list of mappings, and returns the one with best similarity ranking.
 	 * If the list of mappings is <code>null</code> or empty, <code>null</code> is returned.
 	 * @return
 	 */
 	public static Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> getBestMapping(List<Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>>> mappings){
 		if(mappings == null || mappings.size() == 0) return null;
 		if(mappings.size() == 1) return mappings.get(0);
 		Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> best = mappings.get(0);
 		double bestSim = similarityIndex(best);
 		double sim = bestSim;
 		for(Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> mapping : mappings){
 			sim = similarityIndex(mapping);
 			if(sim < bestSim){
 				best = mapping;
 				bestSim = sim;
 			}
 		}
 		return best;
 	}
 
 	/**
 	 * Returns the best mapping found between the two line groups.
 	 * Similarity index is used for ranking of all possible mappings.
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	public static Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> getBestNodeMapping(LineGroup one, LineGroup two){
 		return getBestMapping(getNodeMappings(one,two));
 	}
 
 	/**
 	 * TODO write proper javadoc
 	 * Compares the entries in map. Return zero means 100% similar.
 	 * Higher value means more different.
 	 * @param map
 	 * @return
 	 */
 	public static double similarityIndex(Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> map){
 		double totalLength1 = 0;
 		double totalLength2 = 0;
 		Line one, two;
 		for(XYNode<Line,LineGroup> n : map.keySet()){
 			one = n.getPiggybackObject();
 			two = map.get(n).getPiggybackObject();
 			totalLength1 += one.getLength();
 			totalLength2 += two.getLength();
 		}
 		double similarity = 0;
 		for(XYNode<Line,LineGroup> n : map.keySet()){
 			one = n.getPiggybackObject();
 			two = map.get(n).getPiggybackObject();
 			similarity += Math.abs((one.getLength()/totalLength1) - (two.getLength()/totalLength2));
 		}
 		return similarity;
 	}
 
 	/**
 	 * Returns all possible node mappings between the two specified line groups
 	 * based on line type and edges. Group one may be a subgraph of group two.
 	 * @param one	group one
 	 * @param two	group two
 	 * @return	a list of node mappings one-&gt;two
 	 */
 	public static List<Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>>> getNodeMappings(LineGroup one, LineGroup two) {
 		List<Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>>> nodeMaps = new ArrayList<Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>>>();
 
 		if((one.getGraph().getNodes().size() == 0) || (two.getGraph().getNodes().size() == 0)) return null;
 
 		IntMatrix M0 = new IntMatrix(one.getGraph().getNodes().size(), two.getGraph().getNodes().size());
 
 		for(int i = 0; i < M0.rowCount(); i++){		// find compatible nodes
 			for(int j = 0; j < M0.colCount(); j++){
 				XYNode<Line,LineGroup> n1 = one.getGraph().getNodes().get(i);
 				XYNode<Line,LineGroup> n2 = two.getGraph().getNodes().get(j);
 				if(nodeMatch(n1,n2)){
 					M0.set(i,j,1);
 				}
 			}
 		}
 
 		IntMatrix A = new IntMatrix(M0.rowCount(),M0.rowCount());
 		for(int i = 0; i < M0.rowCount(); i++){		// make adjacency matrix A
 			for(int i2 = 0; i2 < M0.rowCount(); i2++){
 				XYNode<Line,LineGroup> n1 = one.getGraph().getNodes().get(i);
 				XYNode<Line,LineGroup> n2 = one.getGraph().getNodes().get(i2);		// TODO remove redundancy, create a "make adjacency matrix" method
 				if(n1.isNeighbour(n2)){
 					A.set(i,i2,1);
 					A.set(i2,i,1);
 				}
 			}
 		}
 
 		IntMatrix B = new IntMatrix(M0.colCount(),M0.colCount());
 		for(int i = 0; i < M0.colCount(); i++){		// make adjacency matrix B
 			for(int i2 = 0; i2 < M0.colCount(); i2++){
 				XYNode<Line,LineGroup> n1 = two.getGraph().getNodes().get(i);
 				XYNode<Line,LineGroup> n2 = two.getGraph().getNodes().get(i2);
 				if(n1.isNeighbour(n2)){
 					B.set(i,i2,1);
 					B.set(i2,i,1);
 				}
 			}
 		}
 
 		List<IntMatrix> mappings = SubgraphMapper.getMapping(M0,A,B);
 
 		Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> nodeMap;
 		if(mappings.size() > 0){
 			for(IntMatrix mapping : mappings){
 				nodeMap = new IdentityHashMap<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>>();
 				for(int i = 0; i < mapping.rowCount(); i++){
 					for(int j = 0; j < mapping.colCount(); j++){
 						if(mapping.get(i,j) == 1){
 							nodeMap.put(one.getGraph().getNodes().get(i), two.getGraph().getNodes().get(j));	// TODO optimise, store nodes-list in local variables
 						}
 					}
 				}
 				nodeMaps.add(nodeMap);
 			}
 		}
 
 
 		return nodeMaps;
 	}
 
 	/**
 	 * TODO tidy javadoc
 	 * Returns <code>true</code> if all edges in node on can be mapped to edges in node two,
 	 * and piggyback lines have the same line type.
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private static boolean nodeMatch(XYNode<Line,LineGroup> one, XYNode<Line,LineGroup> two){
 		if(one.getPiggybackObject().getType() == two.getPiggybackObject().getType()){
 			if(one.getDegree() <= two.getDegree()){		// use line below to only allow incomplete matching of head/tail nodes
 //			if(one.getDegree() == two.getDegree() || one.getDegree() == 1){
 				Map<XYEdge<Line,LineGroup>,XYEdge<Line,LineGroup>> edgeMap =
 					new IdentityHashMap<XYEdge<Line,LineGroup>,XYEdge<Line,LineGroup>>();
 				outer:
 					for(XYEdge<Line,LineGroup> e : one.getEdges()){
 						for(XYEdge<Line,LineGroup> e2 : two.getEdges()){
 							if(portMatch(e,e2)){
 								edgeMap.put(e,e2);
 								continue outer;
 							}
 						}
 					}
 				if(edgeMap.size() == one.getDegree()) return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns <code>true</code> if the edges have the same port values,
 	 * regardless of direction.
 	 * Head port of <code>one</code> equals head port of <code>two</code> and
 	 * tail port of <code>one</code> equals tail port of <code>two</code>
 	 * <br />-or-<br />
 	 * Head port of <code>one</code> equals tail port of <code>two</code> and
 	 * tail port of <code>one</code> equals head port of <code>two</code>
 	 * @param one	edge one
 	 * @param two	edge two
 	 * @return	<code>true</code> if the edges look the same, ignoring direction
 	 */
 	private static boolean portMatch(XYEdge<?,?> one, XYEdge<?,?> two){
 		if(one.getHeadPort() == two.getHeadPort() &&
 				one.getTailPort() == two.getTailPort()) return true;
 		if(one.getHeadPort() == two.getTailPort() &&
 				one.getTailPort() == two.getHeadPort()) return true;
 		return false;
 	}
 
 	/**
 	 * TODO method not complete
 	 * Finds all head and tail nodes of the specified lines between which the distance is less than <code>n</code> distance units,
 	 * and links them with an edge.
 	 * @param lines	the lines to search
 	 * @param d	the minimum distance allowed between two separate line ends
 	 */
 	public static void proximityLink(List<Line> lines, double d) {
 		if(lines == null || lines.size() == 0) return;
 		Set<XYNode<Object,Line>> endNodes = new HashSet<XYNode<Object,Line>>(lines.size());	// init set. //TODO need identity set?
 		for(Line l : lines){	// get all head/tail nodes
 			endNodes.add(l.getFrom());
 			endNodes.add(l.getTo());
 		}
 	}
 
 
 	/**
 	 * TODO verify result
 	 * Searches the specified line group for crossings that have only two connecting lines
 	 * and eliminates these through merging the two lines into one.
 	 * The graph of the line group is updated to reflect the new structure.
 	 * Apart from the area of mending, the line group and its graph will be left as it was before.
 	 * @param lg	the group of lines to mend
 	 */
 	public static void mendLines(LineGroup lg){
 		XYNode<Object,Line> twoNode = null;
 		List<Line> lines = null;
 		boolean first = true;
 		while(first || twoNode != null){
 			first = false;
 			if(twoNode != null){
 				lines = new ArrayList<Line>();
 				for(XYEdge<Object,Line> e : twoNode.getEdges()){
 					lines.add(e.getPiggybackObject());
 				}
 				twoNode = null;
 				Line merged = merge(lines.get(0),lines.get(1));
 				lg.remove(lines.get(0));
 				lg.remove(lines.get(1));
 				lg.add(merged);
 				connectAllLines(lg);
 			}
 			for(Line l : lg.getMap().keySet()){	// loop through every line in the line group
 				if(l.isClosedPolygon()) continue;
 				if(l.getFrom().getDegree() == 2){
 					twoNode = l.getFrom();
 					break;
 				}
 				if(l.getTo().getDegree() == 2){
 					twoNode = l.getTo();
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * TODO verify result
 	 * Merges the two specified lines into one.
 	 * The direction of line two may be altered.
 	 * @param one	line one
 	 * @param two	line two
 	 * @return	line one + line two
 	 */
 	private static Line merge(Line one, Line two){
 		if(one == two) return one;
 		Line l = new Line();
 		for(XYNode<Object,Line> n : one.getGraph().getNodes()){
 			l.addNode(n);
 		}
 		for(XYEdge<Object,Line> e : one.getGraph().getEdges()){
 			l.addEdge(e);
 			e.setPiggybackObject(l);
 		}
 		if((l.getFrom() == two.getFrom()) || (l.getTo() == two.getTo())) two.reverse();
 		if(l.getFrom() == two.getTo()){
 			l.reverse();
 			two.reverse();
 		}
 		XYNode<Object,Line> last = l.getTo();
 		for(XYNode<Object,Line> n : two.getGraph().getNodes()){
 			if(n != last) l.addNode(n);
 		}
 		for(XYEdge<Object,Line> e : two.getGraph().getEdges()){
 			l.addEdge(e);
 			e.setPiggybackObject(l);
 		}
 		if(l.getFrom() == l.getTo()){
 			Iterator<XYEdge<Object,Line>> it = l.getTo().getEdges().iterator();
 			while(it.hasNext()){
 				if(it.next().getPiggybackObject() == l) it.remove();
 			}
 			l.getGraph().removeNode(l.getTo());
 		}
 
 		if(l.isClosedPolygon()){	// correct OCP and Circles that have gotten their seams off because of line disconnection
 			XYNode<Object,Line> newSeam = null;
 			for(XYNode<Object,Line> n : l.getGraph().getNodes()){
 				if(n.getDegree() > 2){
 //					Helper.p(n);
 //					for(XYEdge<Object,Line> e : n.getEdges()){
 //						Helper.p(e.getPiggybackObject()+" ");
 //					}
 //					Helper.p("\n");
 					newSeam = n;
 					break;
 				}
 			}
 			if(newSeam != null){
 				shiftSeam(newSeam,l);
 			}
 		}
 
 		return l;
 	}
 
 	/**
 	 * TODO verify edge list
 	 * Shifts the seam of a closed polygon so that its from-node is at <code>newSeam</code>.
 	 * If <code>newSeam</code> or the line is <code>null</code>, or the line is not a closed polygon,
 	 * or if <code>newSeam</code> is not part of the line, the method will silently return.
 	 * @param newSeam	the new seam to set
 	 * @param l	the line (closed polygon) to alter
 	 */
 	private static void shiftSeam(XYNode<Object,Line> newSeam, Line l){
 		if(newSeam != null && l.isClosedPolygon() && l.getGraph().getNodes().contains(newSeam)){
 			List<XYNode<Object,Line>> nodes = l.getGraph().getNodes();
 			List<XYEdge<Object,Line>> edges = l.getGraph().getEdges();
 			int seamPos = nodes.indexOf(newSeam);
 			nodes.addAll(nodes.subList(0, seamPos));
 			nodes.subList(0, seamPos).clear();
 			edges.addAll(edges.subList(0, seamPos-1));
 			edges.subList(0, seamPos-1).clear();
 		}
 	}
 
 	/**
 	 * TODO fix javadoc
 	 * Returns <code>true</code> if the specified line can validly be disconnected from the line or lines it is connected to,
 	 * cutting connection at the specified end node.
 	 * If the specified is determined to continue in another line, the line may not be disconnected.
 	 * @param l	the line
 	 * @param end	the point of disconnection - this must be either end node
 	 * @return	<code>true</code> if the specified line can validly be disconnected at <code>end</code> from its adjacent line(s)
 	 */
 	public static boolean canDisconnect(Line l, XYNode<Object,Line> end){
 		if((end == l.getFrom() || end == l.getTo()) && end.getDegree() > 1){
 
 			if(l.getType() == Line.LineType.CIRCLE) return true;	// circles can always be disconnected
 			if(l.getType() == Line.LineType.OTHER_CLOSED_POLYGON) return true;	// OCPs can always be disconnected
 
 			// Head
 			if(end.getDegree() < 4){	// only check when there is a three-way crossing
 				for(XYEdge<Object,Line> e : end.getEdges()){	// for all edges connected to the head of the line
 					if(e.getPiggybackObject() != l){	// get the lines they are part of
 						if(isContinuous(l,e.getPiggybackObject())) return false;	// return false if they are continuous
 					}
 				}
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns <code>true</code> if the specified lines extend each other.
 	 * If either argument is a circle or other closed polygon, the method will return <code>false</code>.
 	 * See the section on line disconnecting for more info.
 	 * @param one	line one
 	 * @param two	line two
 	 * @return	<code>true</code> if the specified lines extend each other
 	 */
 	public static boolean isContinuous(Line one, Line two){
 		if(one == two) return false;	// can't continue self
 		if(one.getType() == Line.LineType.CIRCLE || one.getType() == Line.LineType.OTHER_CLOSED_POLYGON) return false;
 		if(two.getType() == Line.LineType.CIRCLE || two.getType() == Line.LineType.OTHER_CLOSED_POLYGON) return false;
 		int fractions = 5;
 		Line fracOne = null;
 		Line fracTwo = null;
 		if(one.getFrom() == two.getFrom()){				// determine which part of the lines connect
 			fracOne = one.getFraction(1, fractions);	// and get adjacent fractions
 			fracTwo = two.getFraction(1, fractions);
 		}else if(one.getFrom() == two.getTo()){
 			fracOne = one.getFraction(1, fractions);
 			fracTwo = two.getFraction(fractions, fractions);
 		}else if(one.getTo() == two.getFrom()){
 			fracOne = one.getFraction(fractions, fractions);
 			fracTwo = two.getFraction(1, fractions);
 		}else if(one.getTo() == two.getTo()){
 			fracOne = one.getFraction(fractions, fractions);
 			fracTwo = two.getFraction(fractions, fractions);
 		}
 		if(fracOne != null && fracTwo != null){
 			return fracOne.hasAngleOrOpposite(fracTwo.getAngle(), Math.PI / 8);	// if the fractions are of roughly the same angle, return true.
 		}
 		return false;
 	}
 
 	/**
 	 * Returns <code>true</code> if line one and two are connected
 	 * head-head, tail-tail, head-tail or tail-head.
 	 * @param one	line one
 	 * @param two	line two
 	 * @return	<code>true</code> if line one and two are connected
 	 */
 	public static boolean isConnected(Line one, Line two){
 		return (one.getFrom() == two.getFrom()) ||
 		       (one.getTo()   == two.getTo()) ||
 		       (one.getFrom() == two.getTo()) ||
 		       (one.getTo()   == two.getFrom());
 	}
 
 	/**
 	 * Searches specified <code>inputGroups</code> for specified <code>structure</code>.
 	 * Returns <code>null</code> if not found. Otherwise list of mappings.
 	 * This method does not examine group positions or other properties,
 	 * it only generates a list of possible mappings by finding isomorph line groups.
 	 * @param structure	the structure to find
 	 * @param inputGroups	the groups to search
 	 * @return	mappings or <code>null</code>
 	 */
 	public static List<Map<LineGroup,LineGroup>> getStructureMappings(
 			List<LineGroup> structure, List<LineGroup> inputGroups) {
 
 		List<Map<LineGroup,LineGroup>> groupMappings = new ArrayList<Map<LineGroup,LineGroup>>();	// this is the list that will be returned later
 
 		if(structure == null || structure.size() == 0) return null;	// return null if no structure
 		if(inputGroups == null || inputGroups.size() == 0) return null;	// return null if no input groups
 
 		IntMatrix M0 = new IntMatrix(structure.size(),inputGroups.size());	// map matrix
 		IntMatrix A = new IntMatrix(structure.size(),structure.size(),1);	// adjacency matrix for structure	(dummy for the algo to work)
 		IntMatrix B = new IntMatrix(inputGroups.size(),inputGroups.size(),1);	// adjacency matrix for input groups	(dummy for the algo to work)
 
 		for(int n = 0; n < A.rowCount(); n++){		// init adjacency map A, set all nodes adjacent except self-self.
 			A.set(n,n,0);
 		}
 
 		for(int n = 0; n < B.rowCount(); n++){		// init adjacency map B, set all nodes adjacent except self-self.
 			B.set(n,n,0);
 		}
 
 		for(int i = 0; i < structure.size(); i++){	// init M0
 			for(int j = 0; j < inputGroups.size(); j++){	// for each structure group compared to each input group
 				if(sameStructure(structure.get(i),inputGroups.get(j))){					// if they are equal, set corresponding map matrix field to 1.
 					M0.set(i,j,1);
 				}
 			}
 		}
 
 		List<IntMatrix> mappings = SubgraphMapper.getMapping(M0,A,B);	// get all possible mappings
 
 		Map<LineGroup,LineGroup> groupMapping;
 		if(mappings.size() > 0){
 			for(IntMatrix mapping : mappings){
 				groupMapping = new IdentityHashMap<LineGroup,LineGroup>();	// translate integer matrix to object map
 				for(int i = 0; i < mapping.rowCount(); i++){
 					for(int j = 0; j < mapping.colCount(); j++){
 						if(mapping.get(i,j) == 1){
 							groupMapping.put(structure.get(i), inputGroups.get(j));
 						}
 					}
 				}
 				groupMappings.add(groupMapping);
 			}
 		}
 
 		if(groupMappings.size() == 0) return null;
 
 		return groupMappings;
 	}
 
 	/**
 	 * Searches specified <code>inputGroups</code> for specified <code>structure</code>.
 	 * Returns <code>null</code> if not found. Otherwise best mapping.
 	 * This method gets a list of possible mappings from {@link GraphTools#getStructureMappings(List, List)},
 	 * then searches to find the best match by examining the distance and angle between groups, and line group similarity index.
 	 * @param structure	the structure to find
 	 * @param inputGroups	the groups to search
 	 * @return	mapping or <code>null</code>
 	 */
 	public static Map<LineGroup,LineGroup> getBestStructureMapping(List<LineGroup> structure, List<LineGroup> inputGroups){
 
 		final double MAX_ANGLE_DIFFERENCE = Math.PI / 6;	// if the difference in angle between two groups are greater than this, match is invalid
 		//TODO measure and alter this number until good result
 
 		if(structure == null || structure.size() == 0) return null;		// if no structure, return null
 		if(inputGroups == null || inputGroups.size() == 0) return null;	// if no input groups, return null
 
 		List<Map<LineGroup,LineGroup>> mappings = getStructureMappings(structure,inputGroups);	// all candidates found
 
 		Map<Map<LineGroup,LineGroup>,Double> candDistances = new IdentityHashMap<Map<LineGroup,LineGroup>,Double>();
 
 		double dist;	// for storing distance sum
 
 		if(mappings != null && mappings.size() > 0){
 
 			Iterator<Map<LineGroup,LineGroup>> it = mappings.iterator();
 			nextMapping:
 				while(it.hasNext()){	// first, remove all candidates with wrong angle, and compute distance sums for all.
 					Map<LineGroup,LineGroup> cand = it.next();
 					dist = 0;
 					LineGroup inputLG, inputLGcomp;
 
 					for(LineGroup structLG : cand.keySet()){
 						inputLG = cand.get(structLG);
 						for(LineGroup structLGcomp : cand.keySet()){
 							if(structLG == structLGcomp) continue;
 							inputLGcomp = cand.get(structLGcomp);
 							if(getAngleDifference(structLG.getAngleTo(structLGcomp),inputLG.getAngleTo(inputLGcomp)) > MAX_ANGLE_DIFFERENCE){
 								continue nextMapping;	// if angle is too off, go to next without adding
 							}
 //							if(canSee(structLG,structLGcomp,cand.keySet()) ^ canSee(inputLG,inputLGcomp,inputGroups)){
 							if(thereAreObstacles(inputLG,inputLGcomp,inputGroups,cand.values())){
 								continue nextMapping;	// if visibility between lines is blocked, go to next without adding
 							}
 							dist += inputLG.getPosition().distanceTo(inputLGcomp.getPosition());
 						}
 					}
 					candDistances.put(cand,dist);	// store calculated distance sum
 				}
 
 			if(candDistances.size() > 0){
 				double lowestDist = Double.NaN;
 				for(Map<LineGroup,LineGroup> cand : candDistances.keySet()){	// further reduce list to only include the ones with lowest distance
 					dist = candDistances.get(cand);	// get lowest distance
 					if(Double.isNaN(lowestDist) || dist < lowestDist){
 						lowestDist = dist;
 					}
 				}
 				it = candDistances.keySet().iterator();
 				while(it.hasNext()){
 					Map<LineGroup,LineGroup> cand = it.next();	// remove all which are not equal to lowest distance
 					if(candDistances.get(cand) > lowestDist){
 						it.remove();
 					}
 				}
 				double lowestSim = Double.NaN;
 				double sim;
 				Map<LineGroup,LineGroup> mostSimilar = null;
 				it = candDistances.keySet().iterator();
 				LineGroup inputLG;
 				while(it.hasNext()){	// get lowest similarity index for remaining candidates
 					Map<LineGroup,LineGroup> cand = it.next();
 					sim = 0;
 					for(LineGroup structLG : cand.keySet()){
 						inputLG = cand.get(structLG);
 						sim += similarityIndex(structLG,inputLG);
 					}
 					if(Double.isNaN(lowestSim) || sim < lowestSim){
 						lowestSim = sim;
 						mostSimilar = cand;
 					}
 				}
 				return mostSimilar;
 			}else{
 				return null;	// if no candDistances, return null
 			}
 		}else{
 			return null;
 		}
 	}
 
 	/**
 	 * Returns <code>true</code> if there is an obstacle blocking the sight between <code>one</code>
 	 * and <code>two</code>. The list of line groups <code>groups</code> will be searched for obstacles.
 	 * Any line group in the ignore list <code>ignore</code> will not be considered an obstacle,
 	 * regardless of its position.
 	 * @param one	line group one
 	 * @param two	line group two
 	 * @param groups	the groups to search for an obstacle
 	 * @param ignore	the groups to ignore in <code>groups</code>
 	 * @return	<code>true</code> if there is an obstacle blocking the sight between <code>one</code> and <code>two</code>
 	 */
 	private static boolean thereAreObstacles(LineGroup one, LineGroup two,
 			List<LineGroup> groups, Collection<LineGroup> ignore) {
 		XYNode<Object,Line> n1 = new SimpleXYNode<Object,Line>();
 		XYNode<Object,Line> n2 = new SimpleXYNode<Object,Line>();
 		n1.setPosition(one.getPosition());
 		n2.setPosition(two.getPosition());
 		XYEdge<Object,Line> baseLine = new SimpleXYEdge<Object,Line>(n1,n2);
 		for(LineGroup lg : groups){
 			if(ignore.contains(lg)) continue;
 			for(Line l : lg.getMap().keySet()){
 				for(XYEdge<Object,Line> e : l.getGraph().getEdges()){
 					if(e.touches(baseLine)){
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the difference between the two angles specified.
 	 * Return value is always <= PI.
 	 * @param one
 	 * @param two
 	 * @return	the difference between the two angles specified
 	 */
 	public static double getAngleDifference(double one, double two){
 		double a = Math.abs(one - two) % (Math.PI*2);
 		if(a > Math.PI){
 			a = Math.PI*2-a;
 		}
 		return a;
 	}
 
 	/**
 	 * Returns <code>true</code> if a line can be drawn from line group one to line group two
 	 * without crossing any lines in <code>groups</code> (excluding the two line groups).
 	 * @param one	line group one
 	 * @param two	line group two
 	 * @param groups	the list of groups to search
 	 * @return	<code>true</code> if <code>one</code> can see <code>two</code>
 	 */
 	public static boolean canSee(LineGroup one, LineGroup two, Collection<LineGroup> groups){
 		List<LineGroup> g = new ArrayList<LineGroup>(groups);
 		g.remove(one);
 		g.remove(two);
 		XYNode<Object,Line> n1 = new SimpleXYNode<Object,Line>();
 		XYNode<Object,Line> n2 = new SimpleXYNode<Object,Line>();
 		n1.setPosition(one.getPosition());
 		n2.setPosition(two.getPosition());
 		XYEdge<Object,Line> baseLine = new SimpleXYEdge<Object,Line>(n1,n2);
 		for(LineGroup lg : g){
 			for(Line l : lg.getMap().keySet()){
 				for(XYEdge<Object,Line> e : l.getGraph().getEdges()){
 					if(e.touches(baseLine)){
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Tries to disconnect <code>part</code> from </code>whole</code>.
 	 * <code>whole</code> is though never modified.
 	 * Returns a list containing the two line groups <code>part</code>
 	 * and the part of <code>whole</code> which is not <code>part</code>,
 	 * both line groups properly set up.
 	 * If <code>part</code> cannot be disconnected from <code>whole</code>,
 	 * <code>null</code> is returned.
 	 * @param structGroup	the group whose corresponding lines to disconnect
 	 * @param map	the map, mapping <code>structGroup</code> to lines in <code>whole</code>
 	 * @param whole	the whole from which to disconnect
 	 * @return	a list containing the two line groups
 	 */
 	public static List<LineGroup> disconnect(LineGroup structGroup, Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> map, LineGroup whole){
 		LineGroup firstPart = new LineGroup();
 		LineGroup secondPart = new LineGroup();
 		List<LineGroup> parts = new ArrayList<LineGroup>();
 
 		Map<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>> invMap = new IdentityHashMap<XYNode<Line,LineGroup>,XYNode<Line,LineGroup>>();
 		for(XYNode<Line,LineGroup> key : map.keySet()){	// invert map (keys -> values, values -> keys)
 			invMap.put(map.get(key),key);
 		}
 
 		for(XYNode<Line,LineGroup> n : whole.getGraph().getNodes()){
 			XYNode<Line,LineGroup> mapped = invMap.get(n);
 			if(structGroup.getGraph().getNodes().contains(mapped)){	// cannot do this. need map.
 				firstPart.addNode(n);
 			}else{
 				secondPart.addNode(n);
 			}
 		}
 
 		connectAllLines(firstPart);
 		connectAllLines(secondPart);
 
 		// try to split firstPart and secondPart
 
 		if(firstPart.getMap().size() == 0 || secondPart.getMap().size() == 0){
 			return null;	// if no disconnection could be made, return
 		}
 
 		Set<XYNode<Object,Line>> mutualPoints = getMutualPoints(firstPart,secondPart);
 		for(Line one : firstPart.getMap().keySet()){
 			if(mutualPoints.contains(one.getFrom()) && !canDisconnect(one,one.getFrom())){
 				return null;
 			}
 			if(mutualPoints.contains(one.getTo()) && !canDisconnect(one,one.getTo())){
 				return null;
 			}
 		}
 		System.out.println(mutualPoints);
 
 		mendLines(firstPart);
 		mendLines(secondPart);
 
 //		Helper.p(firstPart+" + "+secondPart+"\n");
 
 		parts.add(firstPart);
 		parts.add(secondPart);
 		return parts;
 	}
 
 	/**
 	 * Traverses the line in the specified line group and
 	 * creates edges for adjacent lines.
 	 * Also sets edge ports for the group.
 	 * Use this method to re-create lost edges in a line group.
 	 * @param group	the group to add edges to
 	 */
 	private static void connectAllLines(LineGroup group) {
 		for(Line l : group.getMap().keySet()){
 			List<Line> lines = getConnectingLines(l);
 			for(Line l2 : lines){
 				if(!group.isConnected(l,l2)){
 					group.connect(l,l2);
 				}
 			}
 		}
 		setEdgePorts(group);
 	}
 
 	/**
 	 * Returns the nodes which connect the specified line groups.
 	 * @param one	line group one
 	 * @param two	line group two
 	 * @return	a list of nodes common to both line groups
 	 */
 	public static Set<XYNode<Object,Line>> getMutualPoints(LineGroup one, LineGroup two){
 		if(one == null || two == null) return null;
 		XYNode<Object,Line> l1From,l1To;
 		XYNode<Object,Line> l2From,l2To;
 		Set<XYNode<Object,Line>> mutualNodes = new HashSet<XYNode<Object,Line>>();
 		for(Line l1 : one.getMap().keySet()){
 			l1From = l1.getFrom();
 			l1To = l1.getTo();
 			for(Line l2 : two.getMap().keySet()){
 				l2From = l2.getFrom();
 				l2To = l2.getTo();
 				if(l1From == l2From || l1From == l2To){
 					mutualNodes.add(l1From);
 				}
 				if(l1To == l2To || l1To == l2From){
 					mutualNodes.add(l1To);
 				}
 			}
 		}
 		return mutualNodes;
 	}
 
 	/**
 	 * Removes lines that are too short to be regarded as proper lines.
 	 * Percentage of median group line length is below certain threshold -> remove line.
 	 * Skips vertical lines to preserve many of the actual character components.
 	 * @param groups	the group to remove short lines from
 	 */
 	public static void removeShortLines(List<LineGroup> groups) {
 		if(groups == null || groups.size() == 0) return;
 		double threshold;
 		final double MIN_LENGTH = 20;	// minimum allowed line length in percent
 		List<Line> remove;				// relative to the median length of the lines in the group
 		List<Double> lengths;
 		boolean noRemove;
 		for(LineGroup lg : groups){
 			lengths = new ArrayList<Double>();
 			remove = new ArrayList<Line>();
 			for(Line l : lg.getMap().keySet()){
 				lengths.add(l.getLength());
 			}
 			Collections.sort(lengths);
 			threshold = lengths.get(lengths.size() / 2);
 			for(Line l : lg.getMap().keySet()){
 				noRemove = false;
 				if(l.getType() != Line.LineType.VERTICAL && l.getLength()*100 / threshold <= MIN_LENGTH){
 					List<Line> neighbours = getNeighbours(l);
 					Line.LineType type;
 					if(l.getFrom().getDegree() > 1 && l.getTo().getDegree() > 1){	// prevent removal of lines in the middle of structures
 						noRemove = true;
 					}
 					if(!noRemove && neighbours != null && neighbours.size() > 0){
 						type = neighbours.get(0).getType();
 						for(Line neighbour : neighbours){
 							if(neighbour.getType() != type){
 								noRemove = true;
 							}
 						}
 					}
 					if(!noRemove){
 						remove.add(l);
 					}
 				}
 			}
 			for(Line l : remove){
 				lg.remove(l);
 				l.kill();
 			}
 			mendLines(lg);
 		}
 	}
 
 	/**
 	 * Returns a list of lines adjacent to the specified line.
 	 * @param l	the line for which to find adjacent lines
 	 * @return	a list of lines adjacent to the specified line
 	 */
 	public static List<Line> getNeighbours(Line l){
 		List<Line> n = new ArrayList<Line>();
 		for(XYEdge<Object,Line> e : l.getFrom().getEdges()){
 			if(e.getPiggybackObject() != l){
 				if(!n.contains(l)) n.add(l);
 			}
 		}
 		return n;
 	}
 
 }
