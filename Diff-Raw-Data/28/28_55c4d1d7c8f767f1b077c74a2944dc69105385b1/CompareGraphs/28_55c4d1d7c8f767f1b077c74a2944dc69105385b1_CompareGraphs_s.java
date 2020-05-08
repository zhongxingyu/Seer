 import java.awt.Color;
 import java.util.ArrayList;
 
 import y.base.EdgeCursor;
 import y.base.Node;
 import y.base.NodeCursor;
 import y.view.Graph2D;
 
 
 public final class CompareGraphs {
 	static public void compareColorize (IncrementalHierarchicLayout graph1, IncrementalHierarchicLayout graph2) {
 		ArrayList<Node> top1 = graph1.topnodes;
 		ArrayList<Node> top2 = graph2.topnodes;
 		assert(top1.size() == top2.size());
 		ArrayList<Node> changes = new ArrayList<Node>();
 		compareSubGraph(top1, top2, changes, "return");
		//System.out.println("left comparegraph, changes: " + changes);
 		for (Node x : changes)
 			getR(x).setDifferent(true);
 	}
 	
 	static public void resetComparison (IncrementalHierarchicLayout g) {
 		for (NodeCursor nc = g.graph.nodes(); nc.ok(); nc.next())
 			getR(nc.node()).setDifferent(false);
 	}
 	
 	static private ArrayList<Node> cfSuccs (Node n) {
 		ArrayList<Node> res = new ArrayList<Node>();
 		if (getR(n).getComparableText().equals("continue"))
 			return res;
 		for (EdgeCursor ec = n.outEdges(); ec.ok(); ec.next())
 			if (((Graph2D)n.getGraph()).getRealizer(ec.edge()).getLineColor() != Color.BLUE)
 				res.add(ec.edge().target());
 		return res;
 	}
 	
 	static private GraphNodeRealizer getR (Node n) {
 		return (GraphNodeRealizer)(((Graph2D)n.getGraph()).getRealizer(n));		
 	}
 	
 	static private int compareGraph (Node n, Node m, ArrayList<Node> changes, String stop) {
 		//System.out.println("entering compareGraph with [n]:" + getR(n).getComparableText() + " [m]:" + getR(m).getComparableText() + " [stop]:" + stop + " [changes.size]:" + changes.size());
 		String realstop = stop;
 		if (getR(n).getComparableText().equals("if"))
 			realstop = "if-merge";
		if (getR(n).getComparableText().equals(realstop))
			return 0;
 		ArrayList<Node> succn = cfSuccs(n);
 		Node succ = m;
 		int drop = 0;
 		while (true) {
 			if (getR(succ).getComparableText().equals(realstop))
				return 0;
 			if (compareNode(n, succ))
 				break;
 			ArrayList<Node> succm = cfSuccs(succ);
 			if (succm.size() == 1)
 				succ = succm.get(0);
 			else {
 				drop = Integer.MAX_VALUE;
 				break;
 			}
 			drop++;
 		}
 		if (drop == Integer.MAX_VALUE) {
 			//System.out.println("hit max value");
 			changes.add(n);
 			ArrayList<Node> ms = new ArrayList<Node>();
 			ms.add(m);
 			return compareSubGraph(succn, ms, changes, realstop);
 		} else {
 			//System.out.println("else, drop is " + drop);
 			Node succ2 = m;
 			ArrayList<Node> succm = cfSuccs(m);
 			for (int i = 0; i < drop; i++) {
 				changes.add(succ2);
 				succm = cfSuccs(succ2);
 				succ2 = succm.get(0);
 			}
 			return drop + compareSubGraph(succn, cfSuccs(succ2), changes, realstop);
 		}
 	}
 
 	static private int compareSubGraph (ArrayList<Node> ns, ArrayList<Node> ms, ArrayList<Node> changes, String stop) {
 		//System.out.println("entering comparesubgraph: ns: " + ns + " (size: " + ns.size() + ") ms: " + ms + " (size: " + ms.size() + ") ch.s " + changes.size() + " stop " + stop);
 		if ((ns.size() == ms.size()) && (ns.size() > 0))
 			for (int i = 0; i < ns.size(); i++)
 				compareGraph(ns.get(i), ms.get(i), changes, stop);
 		return changes.size();
 	}
 	
 	static private boolean compareNode (Node n, Node m) {
 		return getR(n).getComparableText().equals(getR(m).getComparableText());
 	}
 }
