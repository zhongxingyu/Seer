 
 package alviz2.algo;
 
 import org.jgrapht.Graph;
 import org.jgrapht.Graphs;
 import org.jgrapht.VertexFactory;
 import org.jgrapht.EdgeFactory;
 
 import javafx.scene.paint.Color;
 import javafx.scene.chart.XYChart;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Queue;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Random;
 
 import alviz2.graph.Node;
 import alviz2.graph.Edge;
 import alviz2.util.*;
 
 @AlgorithmRequirements (
 	graphType = GraphType.COMPLETE_GRAPH,
 	graphInitOptions = {GraphInit.EDGE_COST}
 )
 public class GreedyHeuristic implements Algorithm<Node, Edge> {
 
 	Graph<Node, Edge> gph;
 	Color c;
 	Node.PropChanger npr;
 	Edge.PropChanger epr;
 	Node curNode;
 	ArrayList<Edge> edgeList;
 	int currEdgeNum;
 	Set<Edge> selectedEdges;
 	Set<Node> addedVtices;
 
 	public GreedyHeuristic() {
 		gph = null;
 		npr = null;
 		c = Color.RED;
 		currEdgeNum = 0;
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
 
 	@Override public void setGraph(Graph<Node,Edge> graph, Node.PropChanger npr, Edge.PropChanger epr, Set<Node> Start, Set<Node> Goal)
 	{
 		gph = graph;
 		this.npr = npr;
 		this.epr = epr;
 		edgeList = new ArrayList<Edge>(gph.edgeSet());
 		Collections.sort(edgeList);
 		selectedEdges = new HashSet<Edge>();
 		addedVtices = new HashSet<Node>();
 		
 		for (Node n: gph.vertexSet()) {
 			npr.setVisible(n, true);
 			curNode = n;
 		}
 		npr.setFillColor(curNode, Color.BLUE);
 	}
 
 	@Override public void setChart(XYChart<Number,Number> chart)
 	{
 		return;
 	}
 
 	@Override public VertexFactory<Node> getVertexFactory()
 	{
 		return new VFac();
 	}
 
 	@Override public EdgeFactory<Node,Edge> getEdgeFactory()
 	{
 		return new EFac();
 	}
 
 	@Override public boolean executeSingleStep()
 	{
 		
 		while(true){
 			assert(currEdgeNum < edgeList.size());
 			Edge currEdge = edgeList.get(currEdgeNum);
 			currEdgeNum++;
 			boolean isCycle = bfs(gph.getEdgeSource(currEdge), gph.getEdgeTarget(currEdge), selectedEdges);
 			Node src, dest;
 			src = gph.getEdgeSource(currEdge);
 			dest = gph.getEdgeTarget(currEdge);
 			if(!isCycle && src.deg < 2 && dest.deg < 2) {
 				selectedEdges.add(currEdge);
 				addedVtices.add(src);
 				addedVtices.add(dest);
 				src.deg++;
 				dest.deg++;
 				epr.setVisible(currEdge, true);
 				return true;
 			}
 			else if(isCycle){
 				int connNodeNum = addedVtices.size();
				if(selectedEdges.size()+1 == gph.vertexSet().size()&& src.deg < 2 && dest.deg < 2){
 					selectedEdges.add(currEdge);
 					epr.setVisible(currEdge, true);
 					return false;
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	boolean bfs(Node start, Node end, Set<Edge> edges){
 		Node curr = start;
		//System.out.println("BFS: start " + start.id + " end " + end.id);
 		Set<Node> closed = new HashSet<Node>();
 		Queue<Node> nq = new LinkedList<Node>();
 		nq.add(start);
 		closed.add(start);
 		while(!nq.isEmpty() && curr != end){
 			curr = nq.poll();
 			for(Edge e:gph.edgesOf(curr)){
 				Node other = otherEnd(e,curr);
 				if(edges.contains(e) && !closed.contains(other)) {
 					nq.add(other);
 					closed.add(other);
 				}
 			}
 		}
 		boolean ret = curr.id == end.id;
		System.out.println(curr.id+ " " + end.id + " " + ret);
 		return ret;
 	}
 	
 	Node otherEnd(Edge e, Node s) {
 		Node temps, tempt, other;
 		temps = gph.getEdgeSource(e);
 		tempt = gph.getEdgeTarget(e);
 		other = temps.equals(s) ? tempt : temps;
		//System.out.print("first:" + s.id + " other " + other.id);
 		return other;
 	}
 	
 }
