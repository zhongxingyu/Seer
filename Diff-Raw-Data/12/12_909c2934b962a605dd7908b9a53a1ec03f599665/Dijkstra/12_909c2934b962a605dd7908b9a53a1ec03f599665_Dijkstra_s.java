 package imo;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import edu.uci.ics.jung.graph.DelegateTree;
 import edu.uci.ics.jung.graph.Forest;
 import edu.uci.ics.jung.graph.Graph;
 
 public class Dijkstra
 {
 	private Display display;
 	private DijkstraQueue dQueue;
 	private Graph<Vertex, Edge> g;
 	private ArrayList<Vertex> vHigh;
 	private ArrayList<Edge> eHigh;
 	private Forest<Vertex, Edge> f;
 	
 	public Dijkstra()
 	{
 	}
 	
 	//Build the graph here and then setup the Queue
 	public void build()
 	{
 		//g = new DirectedSparseGraph<Vertex, Edge>();
 		vHigh = new ArrayList<Vertex>();
 		eHigh = new ArrayList<Edge>();
 		try {
			g = GraphGen.getGraph( "resources/TestGraph.csv");
 		} catch( FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		f = new DelegateTree<Vertex, Edge>();
 		dQueue = new DijkstraQueue( g.getVertices());
 		f.addVertex( dQueue.peek());
 		display = new Display( g, f, vHigh, eHigh);
 	}
 	
 	/*
 	 * Remove vertices from the queue. The first vertex in the queue is always the next vertex that needs to be added to the tree. Once that vertex has been removed update the distance values of all unremoved vertices connected to it.
 	 */
 	public void run()
 	{
 		while( dQueue.hasMore()) {
 			
 			Vertex v = dQueue.remove();
 			v.setFinal( true);
 			
 			Collection<Edge> edges = g.getOutEdges( v);
 			Iterator<Edge> iterator = edges.iterator();
 			
 			//Create variables to store chosen edge
 			Edge eChosen = new Edge( -1);
 			
 			while( iterator.hasNext()) {
 				//Get the current object out of the iterator
 				Edge e = iterator.next();
 				
 				//highlight v and e
 				vHigh.clear();
 				vHigh.add( v);
 				eHigh.clear();
 				eHigh.add( e);
 				
 				display.print();
 				
 				Vertex v2 = g.getDest( e);
 				
 				/*
 				 * Only check the vertex if it has not been added to the tree. After that update the vertex if it is now closer to the root then it was before v was added to the tree.
 				 */
 				if( !v2.isFinal()) {
 					if( v2.getDistance() > v.getDistance() + e.getWeight()) {
 						eChosen = e;
 						v2.setDistance( v.getDistance() + e.getWeight());
 						if( f.containsVertex( v2)) {
							Edge old = (Edge) f.getInEdges( v2).toArray()[0];
 							f.addEdge( eChosen, v, v2);
							f.removeEdge( old);
 						} else {
 							f.addEdge( eChosen, v, v2);
 						}
 					}
 					dQueue.update( v2);
 				}
 			}
 			
 			display.print();
 		}
 		System.out.println( "Finished");
 	}
 	
 }
