 /**
  * 
  */
 package alviz2.algo;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javafx.scene.chart.XYChart;
 import javafx.scene.paint.Color;
 
 import org.jgrapht.EdgeFactory;
 import org.jgrapht.Graph;
 import org.jgrapht.VertexFactory;
 
 import alviz2.graph.Node;
 import alviz2.graph.Node.PropChanger;
 import alviz2.util.AlgorithmRequirements;
 import alviz2.util.GraphInit;
 import alviz2.util.GraphType;
 import alviz2.graph.Edge;
 /**
  * @author akhilesh
  *
  */
 @AlgorithmRequirements (
 		graphType = GraphType.COMPLETE_GRAPH,
 		graphInitOptions = {GraphInit.EDGE_COST}
 	)
 
 public class NearestInsertionTSP implements Algorithm<Node, Edge> {
 
 	Graph<Node, Edge> gph;
 	Color c;
 	Node.PropChanger npr;
 	Edge.PropChanger epr;
 	Node curNode;
 	Set<Node> closedSet;
 	Set<Node> cycleNodeSet;
 	Set<Edge> cycleEdgeSet;
 	Set<Node> nonCycleNodeSet;
 	
 	public NearestInsertionTSP(){
 			gph = null;
 			npr = null;
 			c = Color.RED;
 			closedSet = new HashSet<Node>();
 			cycleNodeSet = new HashSet<Node>();
 			cycleEdgeSet = new HashSet<Edge>();
 			nonCycleNodeSet = new HashSet<Node>();
 	}
 	
 	public static class VFac implements VertexFactory<Node> {
 		int id;
 		
 		private VFac() {
 			id = 0;
 		}
 
 		@Override public Node createVertex() {
 			return new Node(id++);
 		}
 	}
 
 	public static class EFac implements EdgeFactory<Node, Edge> {
 		int id;
 
 		private EFac() {
 			id = 0;
 		}
 
 		@Override public Edge createEdge(Node s, Node d) {
 			return new Edge(id++, s, d);
 		}
 	}
 
 
 	@Override
 	public void setGraph(Graph<Node, Edge> graph, PropChanger npr,
 			Edge.PropChanger epr, Set<Node> Start, Set<Node> Goal) {
 		// TODO Auto-generated method stub
 		gph = graph;
 		this.npr = npr;
 		this.epr = epr;
 		for (Node n: gph.vertexSet()) {
 			npr.setVisible(n, true);
 			curNode = n;
 			nonCycleNodeSet.add(n);
 		}
 		npr.setFillColor(curNode, Color.BLUE);
 		closedSet.add(curNode);
 		cycleNodeSet.add(curNode);
 		nonCycleNodeSet.remove(curNode);
 	}
 
 	@Override
 	public void setChart(XYChart<Number, Number> chart) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public VertexFactory<Node> getVertexFactory() {
 		// TODO Auto-generated method stub
 		return new VFac();
 	}
 
 	@Override
 	public EdgeFactory<Node, Edge> getEdgeFactory() {
 		// TODO Auto-generated method stub
 		return new EFac();
 	}
 
 	@Override
 	public boolean executeSingleStep() {
 		// TODO Auto-generated method stub
 		double cost = Double.MAX_VALUE;
 		Node nearestNode = null;
 		Edge minE = null;
 		if(cycleNodeSet.size() == gph.vertexSet().size())
 			return false;
 		
 		//Finding a Node nearest to the cycle
 		for(Node cyc : cycleNodeSet) {
 			for (Node notCyc : nonCycleNodeSet){
 				Edge e = gph.getEdge(cyc, notCyc);
 				if(e.getCost() < cost){
 					nearestNode = notCyc;
 					cost = e.getCost();
 				}
 			}
 		}
 		//Finding an edge (i,j) in the cycle such that d(i,k) + d(k,j) - d(i,j) is minimum
 		if(cycleEdgeSet.isEmpty()){
 			cycleNodeSet.add(nearestNode);
 			nonCycleNodeSet.remove(nearestNode);
 			minE = gph.getEdge(curNode, nearestNode);
 			cycleEdgeSet.add(minE);
 			epr.setVisible(minE, true);
 			epr.setStrokeColor(minE, Color.RED);
 			npr.setFillColor(nearestNode, Color.BLUE);
 			System.out.println("Number of edges in the cycle : " + cycleEdgeSet.size());
 			System.out.println("Number of nodes in the cycle : " + cycleNodeSet.size());
 			System.out.println("Number of nodes not in the cycle : " + nonCycleNodeSet.size());
 			System.out.println("-------------------------------------------------------------------");
 		
 			return true;
 		}
 		else{
 			cost = Double.MAX_VALUE;
 			Edge IK = null, KJ = null, IJ = null;
 			for(Edge ij : cycleEdgeSet){
 				Node i = gph.getEdgeSource(ij);
 				Node j = gph.getEdgeTarget(ij);
 				Node k = nearestNode;
 				Edge ik = gph.getEdge(i,k);
 				if(ik == null){
 					ik= gph.getEdge(k, i);
 				}
 				Edge kj = gph.getEdge(k,j);
 				if(kj == null){
 					kj = gph.getEdge(j, k);
 				}
 				double computedCost = ik.getCost() + kj.getCost() - ij.getCost();
				System.out.print(computedCost + " ");
 				if(computedCost < cost){
 					cost = computedCost;
 					IK = ik;
 					IJ = ij;
 					KJ = kj;
 				}
 			}
 			epr.setVisible(IK, true);
 			epr.setVisible(KJ, true);
 			// Now forms the initial cycle
 			if(cycleEdgeSet.size() != 1)
 				epr.setVisible(IJ, false);
 			
 			epr.setStrokeColor(IK, Color.RED);
 			epr.setStrokeColor(KJ, Color.RED);
 			npr.setFillColor(nearestNode, Color.BLUE);
 			if(cycleEdgeSet.size() != 1)
 				cycleEdgeSet.remove(IJ);
 			cycleEdgeSet.add(IK);
 			cycleEdgeSet.add(KJ);
 			cycleNodeSet.add(nearestNode);
 			nonCycleNodeSet.remove(nearestNode);
 			System.out.println("Number of edges in the cycle : " + cycleEdgeSet.size());
 			System.out.println("Number of nodes in the cycle : " + cycleNodeSet.size());
 			System.out.println("Number of nodes not in the cycle : " + nonCycleNodeSet.size());
 			System.out.println("-------------------------------------------------------------------");
 			return true;
 		}
 	}
 
 }
