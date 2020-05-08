 package applets.Termumformungen$in$der$Technik_02_Kondensatoren;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 
 public class ElectronicCircuit {
 		
 	static class Conn {
 		Node start, end;
 		void clear() {
 			Node oldS = start, oldE = end;
 			start = null; end = null;
 			if(oldS != null) oldS.clear();
 			if(oldE != null) oldE.clear();
 		}
 		void remove() {
 			if(start != null) { start.out.remove(this); start = null; }
 			if(end != null) { end.in.remove(this); end = null; }
 		}
 		void overtake(Conn c) {
 			remove();
 			c.start.addOut(this);
 			c.end.addIn(this);
 			c.remove();
 		}
 		void split(Node newMiddleNode, Conn c) {
 			end.in.remove(this);
 			end.addIn(c);
 			newMiddleNode.addIn(this);
 			newMiddleNode.addOut(c);
 		}
 		
 		PGraph.Point getStringDrawPos(PGraph.Point start, PGraph.Point end) {
 			PGraph.Point pos = start.sum(end).mult(0.5);
			PGraph.Point dist = end.diff(start);
			pos.x += 5 + Math.abs(dist.x) * 0.03;
 			pos.y -= 5;
 			return pos;
 		}
 		
 		double getStringWidth(Graphics g) { return g.getFontMetrics().getStringBounds(userString(), g).getWidth(); }
 		double getStringHeight(Graphics g) { return g.getFontMetrics().getHeight(); }
 		
 		void drawUserString(Graphics g, PGraph.Point start, PGraph.Point end) {
 			String s = userString();
 			if(s.isEmpty()) return;
 			PGraph.Point pos = getStringDrawPos(start, end);
 			g.drawString(s, (int) pos.x, (int) pos.y);
 		}
 
 		void draw(Graphics g, PGraph.Point start, PGraph.Point end) {
 			g.drawLine((int)start.x, (int)start.y, (int)end.x, (int)end.y);
 			drawUserString(g, start, end);
 		}
 
 		Utils.OperatorTree getFlowFromIn() { return end.getFlow(this); }
 		Utils.OperatorTree getFlowFromOut() { return start.getFlow(this); }
 		Utils.OperatorTree getVoltageFromOut() { return Utils.OperatorTree.Zero(); }
 		Utils.OperatorTree getVoltageFrom(Node n) {
 			Utils.OperatorTree voltageFromOut = getVoltageFromOut();
 			if(n == end) return voltageFromOut;
 			if(n == start) return voltageFromOut.minusOne();
 			throw new AssertionError("node must be either start or end");
 		}
 		
 		void initVarNames(int index) {}
 		String userString() { return ""; } // usually varname(s)
 		List<String> vars() { return Utils.listFromArgs(); }		
 	}
 			
 	static class EResistance extends Conn {
 		String varRes = "_R" + this.hashCode();
 		String varFlow = "_I" + this.hashCode();
 		Utils.OperatorTree getFlowFromIn() { return Utils.OperatorTree.Variable(varFlow); }
 		Utils.OperatorTree getFlowFromOut() { return Utils.OperatorTree.Variable(varFlow); }
 		Utils.OperatorTree getVoltageFromOut() { return Utils.OperatorTree.Product(Utils.listFromArgs(Utils.OperatorTree.Variable(varRes).asEntity(), Utils.OperatorTree.Variable(varFlow).asEntity())); }
 		@Override void initVarNames(int index) {
 			varRes = "R" + index;
 			varFlow = "I" + index;
 		}
 		@Override String userString() { return varRes + ", " + varFlow; }
 		@Override List<String> vars() { return Utils.listFromArgs(varRes, varFlow); }
 		void draw(Graphics g, PGraph.Point start, PGraph.Point end) {
 			// resistance in/out connection
 			PGraph.Point diff = end.diff(start).mult(1.0/3.0);			
 			PGraph.Point p1 = start.sum(diff);
 			PGraph.Point p2 = start.sum(diff.mult(2));
 			g.drawLine((int)start.x, (int)start.y, (int)p1.x, (int)p1.y);
 			g.drawLine((int)p2.x, (int)p2.y, (int)end.x, (int)end.y);
 
 			// resistance body
 			PGraph.Point diff2 = diff.rot90().mult(1.0/8.0);
 			PGraph.Point p3 = p1.sum(diff2);
 			PGraph.Point p4 = p1.diff(diff2);
 			PGraph.Point p5 = p2.diff(diff2);
 			PGraph.Point p6 = p2.sum(diff2);
 			g.drawLine((int)p3.x, (int)p3.y, (int)p4.x, (int)p4.y);
 			g.drawLine((int)p4.x, (int)p4.y, (int)p5.x, (int)p5.y);
 			g.drawLine((int)p5.x, (int)p5.y, (int)p6.x, (int)p6.y);
 			g.drawLine((int)p6.x, (int)p6.y, (int)p3.x, (int)p3.y);
 
 			// arrow next to it to show direction
 			PGraph.Point arrowCenter = getStringDrawPos(start, end);
 			arrowCenter.x += getStringWidth(g) * 0.5;
 			arrowCenter.y += Math.abs(diff2.y) * 2.5 + Math.abs(diff.y) * 0.2 + 4;
 			PGraph.Point p7 = arrowCenter.sum(diff2);
 			PGraph.Point p8 = arrowCenter.diff(diff2);
 			PGraph.Point p9 = arrowCenter.sum(diff.mult(0.2));
 			PGraph.Point p10 = arrowCenter.diff(diff.mult(0.2));
 			g.drawLine((int)p7.x, (int)p7.y, (int)p9.x, (int)p9.y);
 			g.drawLine((int)p8.x, (int)p8.y, (int)p9.x, (int)p9.y);
 			g.drawLine((int)p10.x, (int)p10.y, (int)p9.x, (int)p9.y);
 			
 			drawUserString(g, start, end);
 		}		
 	}
 	
 	static class VoltageSource extends Conn {
 		String varVolt = "_U" + this.hashCode();
 		String varFlow = "_I" + this.hashCode();
 		@Override void initVarNames(int index) {
 			varVolt = "U" + index;
 			varFlow = "I" + index;
 		}
 		@Override String userString() { return varVolt + ", " + varFlow; }
 		@Override List<String> vars() { return Utils.listFromArgs(varVolt, varFlow); }
 		Utils.OperatorTree getFlowFromIn() { return Utils.OperatorTree.Variable(varFlow); }
 		Utils.OperatorTree getFlowFromOut() { return Utils.OperatorTree.Variable(varFlow); }
 		Utils.OperatorTree getVoltageFromOut() { return Utils.OperatorTree.Variable(varVolt).minusOne(); }
 		void draw(Graphics g, PGraph.Point start, PGraph.Point end) {
 			PGraph.Point diff = end.diff(start).mult(1.0/2.0);
 			PGraph.Point diff2 = diff.mult(1.0/24.0);
 			PGraph.Point p1 = start.sum(diff).sum(diff2);
 			PGraph.Point p2 = start.sum(diff).diff(diff2);
 			g.drawLine((int)end.x, (int)end.y, (int)p1.x, (int)p1.y);
 			g.drawLine((int)p2.x, (int)p2.y, (int)start.x, (int)start.y);
 			
 			PGraph.Point diff3 = diff.mult(1.0/12.0).rot90();
 			PGraph.Point p3 = p1.diff(diff3.mult(3.0));
 			PGraph.Point p4 = p1.sum(diff3.mult(3.0));
 			PGraph.Point p5 = p2.diff(diff3.mult(1.8));
 			PGraph.Point p6 = p2.sum(diff3.mult(1.8));
 			g.drawLine((int)p3.x, (int)p3.y, (int)p4.x, (int)p4.y);
 			g.drawLine((int)p5.x, (int)p5.y, (int)p6.x, (int)p6.y);
 
 			drawUserString(g, start, end);
 		}
 	}
 	
 	static class ECapacitor extends Conn {
 		String varCap = "_C" + this.hashCode();
 		@Override void initVarNames(int index) { varCap = "C" + index; }
 		@Override String userString() { return varCap; }
 		@Override List<String> vars() { return Utils.listFromArgs(varCap); }
 	}
 	
 	static class Node {
 		Set<Conn> in = new HashSet<Conn>();
 		Set<Conn> out = new HashSet<Conn>();
 		void addIn(Conn c) { c.end = this; in.add(c); }
 		void addOut(Conn c) { c.start = this; out.add(c); }
 		void clear() {
 			List<Conn> conns = new LinkedList<Conn>();
 			conns.addAll(in); conns.addAll(out);
 			in.clear(); out.clear();
 			for(Conn c : conns) c.clear(); 
 		}
 
 		static class NodeCollection extends Node {
 			Set<Node> nodes = new HashSet<Node>();
 			Set<Conn> conns = new HashSet<Conn>();
 			
 			NodeCollection() {
 				Iterable<Set<Conn>> allInConns = Utils.map(nodes, new Utils.Function<Node,Set<Conn>>() {
 					public java.util.Set<Conn> eval(Node obj) { return obj.in; }
 				});
 				in = Utils.mergedSetView(allInConns);
 				in = Utils.substractedSet(in, conns);
 				Iterable<Set<Conn>> allOutConns = Utils.map(nodes, new Utils.Function<Node,Set<Conn>>() {
 					public java.util.Set<Conn> eval(Node obj) { return obj.out; }
 				});
 				out = Utils.mergedSetView(allOutConns);
 				out = Utils.substractedSet(out, conns);
 			}
 			
 			void addFlowInvariants(Node n) {
 				if(nodes.contains(n)) return;
 				nodes.add(n);
 				for(Conn c : Utils.concatCollectionView(n.in, n.out)) {
 					if(c.vars().isEmpty()) { // exactly those connection types are invariant
 						conns.add(c);
 						addFlowInvariants(c.start);
 						addFlowInvariants(c.end);
 					}
 				}
 			}
 		}
 		
 		NodeCollection collectFlowInvariantGroup() {
 			NodeCollection coll = new NodeCollection();
 			coll.addFlowInvariants(this);
 			return coll;
 		}
 		
 		Iterable<MeshPart> inNodes() {
 			return Utils.map(in, new Utils.Function<Conn,MeshPart>() {		
 				public MeshPart eval(Conn obj) { return new MeshPart(obj, obj.start); }
 			});
 		}
 		Iterable<MeshPart> outNodes() {
 			return Utils.map(out, new Utils.Function<Conn,MeshPart>() {
 				public MeshPart eval(Conn obj) { return new MeshPart(obj, obj.end); }
 			});
 		}
 		
 		Set<List<MeshPart>> allMeshsFromHere() {
 			Set<List<MeshPart>> meshs = new HashSet<List<MeshPart>>();
 			collectAllMeshsFromHere(this, new LinkedList<MeshPart>(), new HashSet<Node>(), meshs);
 			return meshs;
 		}
 		void collectAllMeshsFromHere(Node startNode, List<MeshPart> incompleteMesh, Set<Node> exceptNodes, Set<List<MeshPart>> meshs) {
 			if(exceptNodes.contains(this)) return;
 			exceptNodes.add(this);
 			for(MeshPart meshPart : Utils.concatCollectionView(inNodes(), outNodes())) {
 				List<MeshPart> newMesh = new LinkedList<MeshPart>(incompleteMesh);
 				newMesh.add(meshPart);
 				if(meshPart.nextNode == startNode)
 					// we have a new complete mesh
 					meshs.add(newMesh);
 				else
 					// go recursive
 					meshPart.nextNode.collectAllMeshsFromHere(startNode, newMesh, exceptNodes, meshs);
 			}
 		}
 		
 		Utils.OperatorTree getFlow() { return getFlow(null); }
 		Utils.OperatorTree getFlow(Conn conn) {
 			Utils.OperatorTree sum = Utils.OperatorTree.Zero();
 			for(Conn c : in)
 				if(c != conn)
 					sum = sum.sum(c.getFlowFromOut().minusOne());
 			for(Conn c : out)
 				if(c != conn)
 					sum = sum.sum(c.getFlowFromIn());
 			return sum;
 		}
 		EquationSystem.Equation getFlowEquation() {
 			Utils.OperatorTree left = Utils.OperatorTree.Zero();
 			for(Conn c : in)
 				left = left.sum(c.getFlowFromOut());
 			Utils.OperatorTree right = Utils.OperatorTree.Zero();
 			for(Conn c : out)
 				right = right.sum(c.getFlowFromIn());
 			return new EquationSystem.Equation(left, right);
 		}
 	}
 	
 	Node rootNode;
 	
 	Set<Node> allNodes() {
 		Set<Node> s = new HashSet<Node>();
 		collectAllNodes(rootNode, s);
 		return s;
 	}
 	
 	Set<Node.NodeCollection> allNodesPartitionedByFlowInvariant() {
 		Set<Node.NodeCollection> res = new HashSet<Node.NodeCollection>();
 		Set<Node> allNodes = allNodes();
 		while(!allNodes.isEmpty()) {
 			Node n = allNodes.iterator().next();
 			Node.NodeCollection nodeColl = n.collectFlowInvariantGroup();
 			res.add(nodeColl);
 			allNodes.removeAll(nodeColl.nodes);
 		}
 		return res;
 	}
 	
 	static class MeshPart {
 		Conn conn;
 		Node nextNode;
 		MeshPart(Conn c, Node n) { conn = c; nextNode = n; }
 		// these are so that we have every mesh unique in allMeshs(),
 		// independent of the order (thus ignore nextNode)
 		@Override public int hashCode() {
 			return 31 + ((conn == null) ? 0 : conn.hashCode());
 		}
 		@Override public boolean equals(Object obj) {
 			if (!(obj instanceof MeshPart)) return false;
 			MeshPart other = (MeshPart) obj;
 			return conn == other.conn;
 		}
 	}
 	Set<List<MeshPart>> allMeshs() {
 		Set<List<MeshPart>> meshs = new HashSet<List<MeshPart>>();
 		for(Node n : allNodes())
 			meshs.addAll(n.allMeshsFromHere());
 		return meshs;
 	}
 	
 	EquationSystem.Equation equationFromMesh(List<MeshPart> mesh) {
 		EquationSystem.Equation eq = new EquationSystem.Equation();
 		for(MeshPart part : mesh) {
 			Utils.OperatorTree prod = part.conn.getVoltageFrom(part.nextNode);
 			if(prod.isNegative())
 				eq.left = eq.left.sum(prod.minusOne());
 			else if(!prod.isZero())
 				eq.right = eq.right.sum(prod);		
 		}
 		return eq;
 	}
 	
 	boolean isShortCircuit(List<MeshPart> mesh) {
 		boolean haveNegative = false;
 		boolean havePositive = false;
 		for(MeshPart part : mesh) {
 			Utils.OperatorTree volt = part.conn.getVoltageFrom(part.nextNode);
 			if(volt.isNegative()) haveNegative = true;
 			else if(!volt.isZero()) havePositive = true;
 		}
 		return haveNegative ^ havePositive;
 	}
 	boolean haveShortCircuit() {
 		for(List<MeshPart> mesh : allMeshs())
 			if(isShortCircuit(mesh)) return true;
 		return false;
 	}
 	
 	private static void collectAllNodes(Node n, Set<Node> s) {
 		if(s.contains(n)) return;
 		s.add(n);
 		for(Conn c : n.out)
 			collectAllNodes(c.end, s);
 	}
 
 	Set<Conn> allConns() {
 		Set<Conn> s = new HashSet<Conn>();
 		collectAllConns(rootNode, s);		
 		return s;
 	}
 	
 	private static void collectAllConns(Node n, Set<Conn> s) {
 		for(Conn c : n.out)
 			collectAllConns(c, s);
 	}
 
 	private static void collectAllConns(Conn c, Set<Conn> s) {
 		if(s.contains(c)) return;
 		s.add(c);
 		collectAllConns(c.end, s);
 	}
 
 	static class NodePoint extends PGraph.GraphPoint {
 		Node node;
 		NodePoint(Node n, PGraph.Point p) { node = n; point = p; }
 	}
 
 	// returns grid of nodes
 	private Map<Point,Node> visuallyOrderPoints() {
 		Set<Node> s = new HashSet<Node>();
 		Map<Point,Node> grid = new HashMap<Point,Node>();
 		Point rootPt = new Point(0,0);
 		s.add(rootNode);
 		grid.put(rootPt, rootNode);
 		visuallyOrderPoints(grid, rootPt, rootNode, s);
 		return grid;
 	}
 	
 	private Set<Node> neighboarNodes(Node n) {
 		Set<Node> nextNodes = new HashSet<Node>();
 		for(Conn c : n.out) { nextNodes.add(c.end); }		
 		for(Conn c : n.in) { nextNodes.add(c.start); }
 		return nextNodes;
 	}
 	
 	private Set<Node> neighboarNodes(Node n, Set<Node> ignoreNodes) {
 		Set<Node> nextNodes = new HashSet<Node>();
 		for(Conn c : n.out) { if(!ignoreNodes.contains(c.end)) nextNodes.add(c.end); }		
 		for(Conn c : n.in) { if(!ignoreNodes.contains(c.start)) nextNodes.add(c.start); }
 		return nextNodes;
 	}
 
 	private int nodeDistance(Node n, Set<Node> destNodes, boolean ignoreDirect) {
 		if(destNodes.contains(n)) return 0;
 		Set<Node> nodes = new HashSet<Node>();
 		Set<Node> newNodes = new HashSet<Node>();  
 		nodes.add(n);
 		newNodes.add(n);
 		int d = 1;
 		while(!newNodes.isEmpty()) {
 			Set<Node> oldNodes = newNodes;
 			newNodes = new HashSet<Node>();
 			for(Node on : oldNodes)
 				for(Node nn : neighboarNodes(on))
 					if(!nodes.contains(nn)) {
 						if(destNodes.contains(nn)) {
 							if(!ignoreDirect || d > 1)
 								return d;
 						}
 						else {
 							newNodes.add(nn);
 							nodes.add(nn);
 						}
 					}
 			d++;
 		}
 		throw new AssertionError("no connection");
 	}
 			
 	class PointAndNode {
 		Point point;
 		Node node;
 	}
 	
 	private double minDistance(Point p, Iterable<Point> ps) {
 		double min = Double.MAX_VALUE;
 		for(Point pp : ps) {
 			double d = pp.distance(p);
 			if(d < min) min = d;
 		}
 		return min;
 	}
 	
 	private List<Point> ptsForGridMap(Map<Point,Node> grid, Set<Node> nodes) {
 		List<Point> pts = new LinkedList<Point>();
 		for(Point gp : grid.keySet()) {
 			if(nodes.contains(grid.get(gp)))
 				pts.add(gp);
 		}
 		return pts;
 	}
 	
 	private PointAndNode bestFreePoint(final Map<Point,Node> grid, Point srcPoint, final Set<Node> srcNodes, final Set<Node> dstNodes) {
 		final Set<Node> bestDestNodes = Utils.minSet(dstNodes, new Comparator<Node>() {
 			public int compare(Node o1, Node o2) {
 				return nodeDistance(o1, srcNodes, true) - nodeDistance(o2, srcNodes, true);
 			}
 		});
 		
 		final List<Point> bestDstNodesPoints = ptsForGridMap(grid, bestDestNodes);
 		
 		for(int r = 1; ; ++r) {
 			List<Point> pts = new LinkedList<Point>();
 			for(int x = -r; x <= r; ++x)
 				for(int y = -r; y <= r; ++y) {
 					if(x == 0 && y == 0) continue;
 					if(x*x + y*y < (r-1)*(r-1) || x*x + y*y > r*r) continue;
 					Point newPt = new Point( srcPoint.x + x, srcPoint.y + y );
 					if(!grid.containsKey(newPt))
 						pts.add(newPt);
 				}
 			if(pts.isEmpty()) continue;
 			
 			PointAndNode ret = new PointAndNode();
 			ret.point = Collections.min(pts, new Comparator<Point>() {
 				public int compare(Point o1, Point o2) {
 					double d1 = minDistance(o1, bestDstNodesPoints);
 					double d2 = minDistance(o2, bestDstNodesPoints);
 					if(d1 < d2) return -1;
 					if(d1 > d2) return 1;
 					return 0;
 				}
 			});
 			ret.node = Collections.min(srcNodes, new Comparator<Node>() {
 				public int compare(Node o1, Node o2) {
 					return nodeDistance(o1, bestDestNodes, true) - nodeDistance(o2, bestDestNodes, true);
 				}
 			});
 			return ret;
 		}
 	}
 	
 	private void visuallyOrderPoints(final Map<Point,Node> grid, Point p, final Node n, final Set<Node> s) {
 		Set<Node> nextNodes = neighboarNodes(n, s); // filters out by ignoring all in s
 		List<PointAndNode> nextNodesCopy = new LinkedList<PointAndNode>(); // we are going to modify nextNodes but need the original in the end
 		while(!nextNodes.isEmpty()) {
 			PointAndNode pointAndNode = bestFreePoint(grid, p, nextNodes, s);
 			s.add(pointAndNode.node);
 			grid.put(pointAndNode.point, pointAndNode.node);
 			nextNodes.remove(pointAndNode.node);
 			nextNodesCopy.add(pointAndNode);
 		}
 		
 		for(PointAndNode pointAndNode : nextNodesCopy) {
 			visuallyOrderPoints(grid, pointAndNode.point, pointAndNode.node, s);
 		}
 	}
 
 	void registerOnPGraph(PGraph graph) {
 		registerOnPGraph(graph, visuallyOrderPoints());
 	}
 	
 	static class ConnObj extends PGraph.GraphObject {
 		Conn conn;
 		NodePoint start, end;
 		ConnObj(Conn c, NodePoint s, NodePoint e) { conn = c; start = s; end = e; }
 		void draw(PGraph p, java.awt.Graphics g) {
 			g.setColor(Color.blue);
 			conn.draw(g, start.point.transform(p), end.point.transform(p));
 		}
 	}
 
 	void registerOnPGraph(PGraph graph, Map<Point,Node> visuallyOrderedPoints) {
 		Map<Node, NodePoint> nodes = new HashMap<Node, NodePoint>();
 		for(Point p : visuallyOrderedPoints.keySet()) {
 			Node n = visuallyOrderedPoints.get(p);
 			nodes.put(n, new NodePoint(n, new PGraph.Point(p.x, p.y)));
 		}
 				
 		List<ConnObj> conns = new LinkedList<ConnObj>();		
 		for(Conn c : allConns()) conns.add(new ConnObj(c, nodes.get(c.start), nodes.get(c.end)));
 		
 		graph.dragablePoints.addAll(nodes.values());
 		graph.graphObjects.addAll(conns);
 		
 		constructOnPGraph_Final(graph);
 	}
 	
 	NodePoint findNodePointOnPGraph(PGraph graph, Point target) {
 		for(PGraph.GraphPoint p : graph.dragablePoints) {
 			if(!(p instanceof NodePoint)) continue;
 			NodePoint np = (NodePoint) p;
			if(np.point.distance(target) <= 0.1)
 				return np;
 		}
 		return null;
 	}
 	
 	NodePoint getNodePointOnPGraph(PGraph graph, Point target) {
 		NodePoint np = findNodePointOnPGraph(graph, target);
 		if(np != null) return np;
 
 		// create new node
 		Node n = new Node();
 		if(rootNode == null) rootNode = n;
 		np = new NodePoint(n, new PGraph.Point(target));
 		graph.dragablePoints.add(np);
 		return np;
 	}
 	
 	<T extends Conn> T constructOnPGraph(PGraph graph, Class<T> clazz, Point startPt, Point endPt) {
 		T c;
 		try {
 			c = clazz.newInstance();
 		} catch (Exception e) {
 			// should not happen
 			e.printStackTrace();
 			return null;
 		}
 		
 		NodePoint startNodePt = getNodePointOnPGraph(graph, startPt);
 		NodePoint endNodePt = getNodePointOnPGraph(graph, endPt);
 		startNodePt.node.addOut(c);
 		endNodePt.node.addIn(c);
 		graph.graphObjects.add(new ConnObj(c, startNodePt, endNodePt));
 		return c;
 	}
 
 	Point constructOnPGraph_LastPt = null;
 	void constructOnPGraph_Start(PGraph graph, int x, int y) {
 		constructOnPGraph_LastPt = new Point(x, y);
 	}
 	<T extends Conn> T constructOnPGraph_Next(PGraph graph, Class<T> clazz, int x, int y) {
 		Point oldPt = constructOnPGraph_LastPt;
 		constructOnPGraph_LastPt = new Point(x, y);
 		return constructOnPGraph(graph, clazz, oldPt, constructOnPGraph_LastPt);
 	}
 	void constructOnPGraph_Final(PGraph graph) {
		initVarNames();

 		Collection<NodePoint> nodes = Utils.collFromIter(Utils.filterType(graph.dragablePoints, NodePoint.class));
 		
		double borderSize = 0.2;
 		graph.x_l = Collections.min(Utils.map(nodes, new Utils.Function<NodePoint,Double>() {
 			public Double eval(NodePoint obj) { return obj.point.x; }
 		})) - borderSize;
 		graph.x_r = Collections.max(Utils.map(nodes, new Utils.Function<NodePoint,Double>() {
 			public Double eval(NodePoint obj) { return obj.point.x; }
 		})) + borderSize + 0.2;
 		graph.y_o = Collections.min(Utils.map(nodes, new Utils.Function<NodePoint,Double>() {
 			public Double eval(NodePoint obj) { return obj.point.y; }
 		})) - borderSize;
 		graph.y_u = Collections.max(Utils.map(nodes, new Utils.Function<NodePoint,Double>() {
 			public Double eval(NodePoint obj) { return obj.point.y; }
 		})) + borderSize;
 		
 		graph.showAxes = false;
 	}
 	
 	void clear() {
 		if(rootNode != null) rootNode.clear();
 		rootNode = null;
 	}
 	
 	private Random r = new Random();
 
 	private <T> void shuffleArray(T[] a) {
 		List<T> l = new LinkedList<T>();
 		for(T o : a) l.add(o);
 		Collections.shuffle(l, r);
 		int i = 0;
 		for(T o : l) { a[i] = o; ++i; }
 	}
 	
 	private void randomLine(Node[] nodes, int minEffObjs, int maxEffObjs) {
 		boolean mustAddResistance = false;
 		{
 			// first, connect all to test if we have a short circuit
 			Set<Conn> tempConns = new HashSet<Conn>();
 			for(int i = 0; i < nodes.length - 1; ++i) {
 				Conn c = new VoltageSource();
 				tempConns.add(c);
 				Node s = nodes[i], e = nodes[i + 1];
 				s.addOut(c);
 				e.addIn(c);
 			}
 			if(haveShortCircuit())
 				mustAddResistance = true;
 			// remove temp conns again
 			for(int i = 0; i < nodes.length; ++i) {
 				nodes[i].in.removeAll(tempConns);
 				nodes[i].out.removeAll(tempConns);
 			}
 		}
 		
 		maxEffObjs = Math.max(minEffObjs, maxEffObjs);
 		maxEffObjs = Math.min(nodes.length - 1, maxEffObjs);
 		int numEffObjs = minEffObjs + r.nextInt(maxEffObjs - minEffObjs + 1);
 		int numERes =
 			(minEffObjs >= 2)
 				? (1 + r.nextInt(numEffObjs - 1))
 				: (r.nextInt(numEffObjs + 1));
 		if(mustAddResistance) numERes = Math.max(numERes, 1);
 		int numVoltSrc = Math.max(numEffObjs - numERes, 0);	
 		
 		Integer[] nodeIndexes = new Integer[nodes.length - 1];
 		for(int i = 0; i < nodes.length - 1; ++i) nodeIndexes[i] = i;
 		shuffleArray(nodeIndexes);
 		for(int i = 0; i < nodeIndexes.length; ++i) {
 			Conn c;
 			if(i < numERes) c = new EResistance();
 			else if(i < numERes + numVoltSrc) c = new VoltageSource();
 			else c = new Conn();
 			
 			// replace old simple connection
 			Node s = nodes[nodeIndexes[i]], e = nodes[nodeIndexes[i] + 1];
 			s.addOut(c);
 			e.addIn(c);
 		}
 	}
 	
 	private void randomSetup(int W, int H, Node[] nodes) {
 		if(H <= 2 || W <= 2) return; // one of them ready -> stop (don't fill the rest possible ones)
 		boolean horiz = (H <= 2) ? false : r.nextBoolean();
 		Node[][] ns;
 		Node[] line;
 		int[] ws = new int[] {W,W};
 		int[] hs = new int[] {H,H};		
 		if(horiz) {
 			int y = r.nextInt(H - 2) + 1;
 			ns = splitHoriz(W, H, y, nodes);
 			line = getHorizLine(W, H, y, nodes);
 			hs[0] = y + 1; hs[1] = H - y;
 		}
 		else {
 			int x = r.nextInt(W - 2) + 1;
 			ns = splitVert(W, H, x, nodes);
 			line = getVertLine(W, H, x, nodes);
 			ws[0] = x + 1; ws[1] = W - x;
 		}
 		randomLine(line, 1, 2);
 		randomSetup(ws[0], hs[0], ns[0]);
 		randomSetup(ws[1], hs[1], ns[1]);
 	}
 	
 	static Node[][] splitHoriz(int W, int H, int _y, Node[] nodes) {
 		Node[] top = new Node[(_y + 1)*W];
 		Node[] bottom = new Node[(H - _y)*W];
 		for(int x = 0; x < W; ++x)
 			for(int y = 0; y <= _y; ++y)
 				top[y*W + x] = nodes[y*W + x];
 		for(int x = 0; x < W; ++x)
 			for(int y = 0; y < H - _y; ++y)
 				bottom[y*W + x] = nodes[(y + _y)*W + x];		
 		return new Node[][] {top, bottom};
 	}
 
 	static Node[] getHorizLine(int W, int H, int y, Node[] nodes) {
 		Node[] line = new Node[W];
 		for(int x = 0; x < W; ++x)
 			line[x] = nodes[y*W + x];
 		return line;
 	}
 	
 	static Node[][] splitVert(int W, int H, int _x, Node[] nodes) {
 		Node[] left = new Node[(_x + 1)*H];
 		Node[] right = new Node[(W - _x)*H];
 		for(int x = 0; x <= _x; ++x)
 			for(int y = 0; y < H; ++y)
 				left[y*(_x+1) + x] = nodes[y*W + x];
 		for(int x = 0; x < W - _x; ++x)
 			for(int y = 0; y < H; ++y)
 				right[y*(W-_x) + x] = nodes[y*W + x + _x];		
 		return new Node[][] {left, right};
 	}
 
 	static Node[] getVertLine(int W, int H, int x, Node[] nodes) {
 		Node[] line = new Node[H];
 		for(int y = 0; y < H; ++y)
 			line[y] = nodes[y*W + x];
 		return line;
 	}
 	
 	void initVarNames() {
 		int index = 1;
 		for(Conn c : allConns()) {
 			if(!c.vars().isEmpty()) {
 				c.initVarNames(index);
 				index++;
 			}
 		}
 	}
 	
 	// returns straightforward visual ordered points
 	Map<Point,Node> randomSetup(int W, int H) {		
 		clear();
 		Node[] nodes = new Node[W * H];
 		for(int i = 0; i < W*H; ++i) nodes[i] = new Node();
 		rootNode = nodes[0];
 
 		List<Node> border = new LinkedList<Node>();
 		for(int i = 0; i < W; ++i) border.add(nodes[i]);
 		for(int i = 1; i < H; ++i) border.add(nodes[i*W + W-1]);
 		for(int i = W - 2; i >= 0; --i) border.add(nodes[W*(H-1) + i]);
 		for(int i = H - 2; i >= 0; --i) border.add(nodes[i*W]);
 		randomLine(border.toArray(new Node[] {}), 2, border.size() / 2);
 		
 		randomSetup(W, H, nodes);
 		
 		initVarNames();
 		
 		//dump(nodes);
 		
 		Map<Point,Node> visualOrderedPoints = new HashMap<Point,Node>();
 		for(int x = 0; x < W; ++x)
 			for(int y = 0; y < H; ++y)
 				visualOrderedPoints.put(new Point(x,y), nodes[y*W + x]);
 		return visualOrderedPoints;
 	}
 	
 	void dump(Node[] nodes) {
 		System.out.println("[");
 		boolean first = true;
 		for(int i = 0; i < nodes.length; ++i) {
 			if(!first) System.out.println(",");
 			System.out.print("( in: {");
 			boolean first2 = true;
 			for(Conn c : nodes[i].in) {
 				if(!first2) System.out.print(",");
 				System.out.print( "" + Utils.indexInArray(nodes, c.start) + "→" + Utils.indexInArray(nodes, c.end) );
 				first2 = false;
 			}
 			System.out.print("}, out: {");
 			first2 = true;
 			for(Conn c : nodes[i].out) {
 				if(!first2) System.out.print(",");
 				System.out.print( "" + Utils.indexInArray(nodes, c.start) + "→" + Utils.indexInArray(nodes, c.end) );
 				first2 = false;
 			}
 			System.out.print("} )");
 			first = false;
 		}
 		System.out.println("");
 		System.out.println("]");
 	}
 
 	public EquationSystem getEquationSystem() {
 		EquationSystem eqSys = new EquationSystem();
 		for(Conn c : allConns())
 			for(String var : c.vars())
 				eqSys.registerVariableSymbol(var);
 		for(Node n : allNodesPartitionedByFlowInvariant()) {
 			EquationSystem.Equation eq = n.getFlowEquation();
 			if(!eq.isTautology() && !eqSys.containsNormed(eq))
 				eqSys.equations.add(eq);
 		}
 		for(List<MeshPart> mesh : allMeshs()) {
 			EquationSystem.Equation eq = equationFromMesh(mesh);
 			if(!eq.isTautology() && !eqSys.containsNormed(eq))
 				eqSys.equations.add(eq);
 		}
 		return eqSys;
 	}
 	
 	static class EquationQuestion {
 		String wantedExpr;
 		Set<String> allowedVars = new HashSet<String>();
 	};
 	
 	EquationQuestion randomEquationQuestion() {
 		EquationQuestion q = new EquationQuestion();
 		VoltageSource voltSrc = Utils.randomChoiceFrom(new ArrayList<VoltageSource>(Utils.collFromIter(Utils.filterType(allConns(), VoltageSource.class))), r);
 		q.wantedExpr = voltSrc.varVolt + "/" + voltSrc.varFlow;
 		for(Conn c : allConns()) {
 			if(c instanceof EResistance)
 				q.allowedVars.add(((EResistance)c).varRes);
 			else if(c instanceof VoltageSource)
 				q.allowedVars.add(((VoltageSource)c).varVolt);				
 		}
 		return q;
 	}
 	
 }
