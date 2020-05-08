 package nl.helixsoft.graph;
 
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import nl.helixsoft.util.AttributesTable;
 
 import org.apache.commons.collections15.BidiMap;
 
 import edu.uci.ics.jung.algorithms.util.Indexer;
 import edu.uci.ics.jung.graph.DirectedGraph;
 
public class GmlFormat 
 {
 	/** same as other writeGml, but sorts nodes and edges, for stable output. */
 	public static <V, E> void writeGml(OutputStream fos, final DirectedGraph<V, E> graph, AttributesTable<V> nodeAttr, AttributesTable<E> edgeAttr, Comparator<V> comp)
 	{
 		List<V> sortedV = new ArrayList<V>(graph.getVertices());
 		Collections.sort (sortedV, comp);
 
 		// GML uses integers as node ids, so start by creating integer inex.
 		final BidiMap<V, Integer> idx = Indexer.create(sortedV);
 		
 		GmlEmitter gml = new GmlEmitter(fos);
 		gml.startList("graph");
 
 		writeNodes (nodeAttr, idx, gml, sortedV);
 
 		List<E> sortedE = new ArrayList<E>(graph.getEdges());
 		Collections.sort (sortedE, new Comparator<E>() {
 			
 				@Override	
 				public int compare (E e1, E e2)
 				{
 					int i1 = idx.get(graph.getSource(e1));
 					int i2 = idx.get(graph.getSource(e2));
 					
 					if (i1 < i2) return -1;
 					if (i1 > i2) return 1;
 					
 					int j1 = idx.get(graph.getDest(e1));
 					int j2 = idx.get(graph.getDest(e2));
 					
 					if (j1 < j2) return -1;
 					if (j1 > j2) return 1;
 					return 0;
 				}
 			}
 		);
 		
 		writeEdges(graph, edgeAttr, idx, gml, sortedE);
 
 		gml.closeList();

 	}
 
 	private static <V, E> void writeEdges(final DirectedGraph<V, E> graph,
 			AttributesTable<E> edgeAttr, final BidiMap<V, Integer> idx,
 			GmlEmitter gml, Collection<E> edges) 
 	{
 		for (E e : edges)
 		{
 			V s = graph.getSource(e);
 			V t = graph.getDest(e);
 			gml.startList("edge");
 			gml.intLiteral("source", idx.get(s));
 			gml.intLiteral("target", idx.get(t));
 			for (Map.Entry<String, Object> entry : edgeAttr.getAttributes(e))
 			{
 				Object val = entry.getValue();
 				if (val instanceof Number)
 				{
 					gml.numberLiteral(entry.getKey(), (Number)val);
 				}
 				else
 				{
 					gml.stringLiteral(entry.getKey(), "" + val);
 				}
 			}
 			gml.closeList();
 		}
 	}
 	
 	public static <V, E> void writeGml(OutputStream fos, DirectedGraph<V, E> graph, AttributesTable<V> nodeAttr, AttributesTable<E> edgeAttr)
 	{
 		// GML uses integers as node ids, so start by creating integer inex.
 		final BidiMap<V, Integer> idx = Indexer.create(graph.getVertices());
 		
 		GmlEmitter gml = new GmlEmitter(fos);
 		gml.startList("graph");
 
 		writeNodes(nodeAttr, idx, gml, graph.getVertices());
 		writeEdges(graph, edgeAttr, idx, gml, graph.getEdges());
 		gml.closeList();
 	}
 
 	private static <V> void writeNodes(AttributesTable<V> nodeAttr,
 			final BidiMap<V, Integer> idx, GmlEmitter gml, Collection<V> nodes) 
 	{
 		for (V v : nodes)
 		{
 			gml.startList("node");
 			gml.intLiteral("id", idx.get(v));
 			for (Map.Entry<String, Object> entry : nodeAttr.getAttributes(v))
 			{
 				Object val = entry.getValue();
 				if (val instanceof Number)
 				{
 					gml.numberLiteral(entry.getKey(), (Number)val);
 				}
 				else
 				{
 					gml.stringLiteral(entry.getKey(), "" + val);
 				}
 			}
 			gml.closeList();
 		}
 	};
 }
