 package org.lilian.graphs.jung;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Paint;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.collections15.Transformer;
import org.data2semantics.tools.graphs.Edge;
import org.data2semantics.tools.graphs.Vertex;
 import org.lilian.graphs.Node;
 import org.lilian.graphs.UTGraph;
 import org.lilian.graphs.UTLink;
 
 import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
 import edu.uci.ics.jung.algorithms.layout.KKLayout;
 import edu.uci.ics.jung.algorithms.layout.SpringLayout;
 import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.UndirectedGraph;
 import edu.uci.ics.jung.graph.UndirectedSparseGraph;
 import edu.uci.ics.jung.visualization.VisualizationImageServer;
 import edu.uci.ics.jung.visualization.decorators.EdgeShape;
 import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
 
 
 
 public class Graphs
 {
 	/**
 	 * Translates and Undirected Tagged graph to a JUNG graph.
 	 * 
 	 * @param graph
 	 * @return
 	 */
 	public static <L, T> UndirectedGraph<Vertex<L>, Edge<T>> toJUNG(UTGraph<L, T> graph)
 	{
 		UndirectedSparseGraph<Vertex<L>, Edge<T>> out = 
 			new UndirectedSparseGraph<Vertex<L>, Edge<T>>();
 		
 		Map<Node<L>, Vertex<L>> map = new HashMap<Node<L>, Vertex<L>>();
 		
 		for (Node<L> node : graph.nodes())
 		{
 			Vertex<L> vertex = new Vertex<L>(node.label());
 			map.put(node, vertex);
 			
 			out.addVertex(vertex);
 		}
 		
 		for(UTLink<L, T> link : graph.links())
 		{
 			Vertex<L> first = map.get(link.first());
 			Vertex<L> second = map.get(link.second());
 			
 			out.addEdge(new Edge<T>(link.tag()), Arrays.asList(first, second));
 		}
 		
 		return out;
 	}
 	
 	
 	/**
 	 * Renders an image of a graph
 	 * @param graph
 	 * @return
 	 */
 	public static <V, E> BufferedImage image(Graph<V, E> graph, int width, int height)
 	{
 		// Create the VisualizationImageServer
 		// vv is the VisualizationViewer containing my graph
 		VisualizationImageServer<V, E> vis =
 		    new VisualizationImageServer<V, E>(
 		    		new ISOMLayout<V, E>(graph), 
 		    		new Dimension(width, height));
 
 		vis.setBackground(Color.WHITE);
 //		vis.getRenderContext()
 //			.setEdgeLabelTransformer(new ToStringLabeller<E>());
 		vis.getRenderContext()
 			.setEdgeShapeTransformer(new EdgeShape.Line<V, E>());
 		vis.getRenderContext()
 			.setEdgeDrawPaintTransformer(new Transformer<E, Paint>()
 			{
 				Color c = new Color(0.0f, 0.0f, 0.0f, 0.05f);
 				public Paint transform(E input)
 				{
 					return c;
 				}
 			});
 		vis.getRenderContext().setVertexFillPaintTransformer(new Transformer<V, Paint>()
 		{
 			Color c = new Color(0.0f, 0.0f, 1.0f, 0.5f);
 			public Paint transform(V input)
 			{
 				return c;
 			}
 		});
 		vis.getRenderContext().setVertexStrokeTransformer(new Transformer<V, Stroke>()
 		{
 			public Stroke transform(V input)
 			{
 				return new BasicStroke(0.0f);
 			}
 		});
 		vis.getRenderContext().setVertexShapeTransformer(new Transformer<V, Shape>()
 		{
 			double r = 3.0;
 			Shape e = new Ellipse2D.Double(0.0, 0.0, r, r);
 			
 			public Shape transform(V input)
 			{
 				return e;
 			}
 		});		
 		
 //		vis.getRenderContext()
 //			.setVertexLabelTransformer(new ToStringLabeller<V>());
 		vis.getRenderer().getVertexLabelRenderer()
 		    .setPosition(Position.CNTR);
 
 		// Create the buffered image
 		BufferedImage image = (BufferedImage) vis.getImage(
 		    new Point2D.Double(width/2, height/2),
 		    new Dimension(width, height));
 		
 		return image;
 	}
 
 }
