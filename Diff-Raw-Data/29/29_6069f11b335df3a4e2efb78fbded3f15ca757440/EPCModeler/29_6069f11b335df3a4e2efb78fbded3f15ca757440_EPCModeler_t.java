 package lu.sven.epcmodeler;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import org.apache.commons.collections15.Transformer;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import lu.sven.epcmodeler.graph.Edge;
 import lu.sven.epcmodeler.graph.EdgeFactory;
 import lu.sven.epcmodeler.graph.Node;
 import lu.sven.epcmodeler.graph.NodeFactory;
 import lu.sven.epcmodeler.graph.NodeType;
 import lu.sven.epcmodeler.graph.NodeVisibility;
 import lu.sven.epcmodeler.graph.OurObservableGraph;
 import lu.sven.epcmodeler.http.HTTPClient;
 import lu.sven.epcmodeler.http.HTTPServer;
 import lu.sven.epcmodeler.mouse.CreateMenu;
 import lu.sven.epcmodeler.mouse.EdgeMenu;
 import lu.sven.epcmodeler.mouse.NodeMenu;
 import lu.sven.epcmodeler.mouse.PopupVertexEdgeMenuMousePlugin;
 import lu.sven.epcmodeler.util.GraphChangeListener;
 import lu.sven.epcmodeler.util.NodeUtil;
 import lu.sven.epcmodeler.util.SaveGraph;
 import lu.sven.epcmodeler.util.Transformers;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedGraph;
 import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
 import edu.uci.ics.jung.io.GraphIOException;
 import edu.uci.ics.jung.io.graphml.EdgeMetadata;
 import edu.uci.ics.jung.io.graphml.GraphMLReader2;
 import edu.uci.ics.jung.io.graphml.GraphMetadata;
 import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
 import edu.uci.ics.jung.io.graphml.NodeMetadata;
 import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
 
 public class EPCModeler {
 	private static List<InetAddress> peers = new LinkedList<InetAddress>();
 	private static Logger logger = Logger.getRootLogger();
 	public static List<Node> receivedNodes = new LinkedList<Node>(); 
 	public static List<Edge> receivedEdges = new LinkedList<Edge>();
 	public static StaticLayout<Node, Edge> layout;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// Initialize LOG4J
 		try {
 			PatternLayout layout = new PatternLayout( "<%d{yyyy-MM-dd HH:mm:ss}> [%-5p] | %-10C{1} : %m%n" );
 			ConsoleAppender consoleAppender = new ConsoleAppender( layout );
 			logger.addAppender( consoleAppender );
 			FileAppender fileAppender = new FileAppender( layout, "logs/logger.log", false );
 			logger.addAppender( fileAppender );
 			// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
 			logger.setLevel( Level.ALL );
 		} catch( Exception ex ) {
 			System.out.println( ex );
 		}
 		
 		// Load list of peers
 		Properties p = new Properties();
 	    try {
 			p.load(new FileInputStream("config.props"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		for(Object o : p.keySet()) {
 			Object k = p.get(o);
 			try {
 				peers.add(InetAddress.getByName((String)k));
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		// Start displaying the Viewer
 		EPCViewer sgv = new EPCViewer();
 		
 		// Get up2date graph
 		InetAddress update = EPCModeler.getMaxNodeGraph();
 		// TODO implement Graph merge
 		// TODO refactor this sh**
 		Graph<Node, Edge> g;
 		HTTPClient httpClient;
 		try {
 			httpClient = new HTTPClient(update.getHostName(), "/getGraph", "");
 			String peerGraphGML = httpClient.getResponseString();
 			BufferedReader stringreader = new BufferedReader(new StringReader(peerGraphGML));
 			Transformer<GraphMetadata, Graph<Node, Edge>>
 			 graphTransformer = new Transformer<GraphMetadata,
 			                           Graph<Node, Edge>>() {
 
 			   public Graph<Node, Edge>
 			       transform(GraphMetadata metadata) {
 			         metadata.getEdgeDefault();
 					if (metadata.getEdgeDefault().equals(
 			         EdgeDefault.DIRECTED)) {
 			             return new DirectedSparseMultigraph<Node, Edge>();
 			         } else {
 			             return new
 			             UndirectedSparseMultigraph<Node, Edge>();
 			         }
 			       }
 			 };
 			 
 			 /* Create the Vertex Transformer */
 			 Transformer<NodeMetadata, Node> vertexTransformer
 			 = new Transformer<NodeMetadata, Node>() {
 			     public Node transform(NodeMetadata metadata) {
 			    	 Node n =
 			             NodeFactory.getInstance().create();
 			         // Set the saved values
 			    	 n.setID(metadata.getId());
 			         n.setLabel(metadata.getProperty("label"));
 			         n.setNodeType(NodeType.valueOf(metadata.getProperty("type")));
 			         n.setState(NodeVisibility.valueOf(metadata.getProperty("state")));
 			         n.setTimeStamp(metadata.getProperty("timestamp"));
 			         // Set position
 			         Point2D p = new Point2D.Double(
 			        		 Double.parseDouble(metadata.getProperty("x")),
 			        		 Double.parseDouble(metadata.getProperty("y"))
 			         	);
 			         n.setPoint(p);
 			         
 			         return n;
 			     }
 			 };
 			 
 			 /* Create the Edge Transformer */
 			 Transformer<EdgeMetadata, Edge> edgeTransformer =
 			 new Transformer<EdgeMetadata, Edge>() {
 			     public Edge transform(EdgeMetadata metadata) {
 			    	 Edge e = EdgeFactory.getInstance().create();
 			         return e;
 			     }
 			 };
 			 
 			 // We do not have hyperedges, but it is needed by the interface
 			 Transformer<HyperEdgeMetadata, Edge> hyperEdgeTransformer =
 				 new Transformer<HyperEdgeMetadata, Edge>() {
 				     public Edge transform(HyperEdgeMetadata metadata) {
 				    	 Edge e = EdgeFactory.getInstance().create();
 				         return e;
 				     }
 				 };
 			
 			/* Create the graphMLReader2 */
 			GraphMLReader2<Graph<Node, Edge>, Node, Edge>
 			graphReader = new GraphMLReader2<Graph<Node, Edge>, Node, Edge>
 				(stringreader, graphTransformer, vertexTransformer,
 				edgeTransformer, hyperEdgeTransformer);
 			
 			try {
 			    /* Get the new graph object from the GraphML file */
 			    g = (DirectedGraph<Node, Edge>) graphReader.readGraph();
 			    EPCViewer.EPCGraph = new OurObservableGraph(g);
 			    GraphChangeListener gel = new GraphChangeListener();
 		    	((OurObservableGraph) EPCViewer.EPCGraph).addGraphEventListener(gel);			    
 			} catch (GraphIOException ex) {}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
         // Layout<Node, Edge>, VisualizationViewer<Node,Edge>
         layout = new StaticLayout<Node, Edge>(EPCViewer.EPCGraph, Transformers.locationTransformer);
         layout.setSize(new Dimension(700,700));
         
         // Start the HTTP Server
 		@SuppressWarnings("unused")
 		HTTPServer server = new HTTPServer(EPCViewer.EPCGraph);
         
         VisualizationViewer<Node, Edge> vv = new VisualizationViewer<Node, Edge>(layout);
         vv.setPreferredSize(new Dimension(750,750));
         
         // Show vertex and edge labels
         vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Node>());
         vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Edge>());
         
         // Set the Transformers
         vv.getRenderContext().setVertexFillPaintTransformer(Transformers.vertexPaint);
 		vv.getRenderContext().setVertexShapeTransformer(Transformers.vertexForm);
 		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Node>());
 		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
 		vv.getRenderContext().setEdgeShapeTransformer(Transformers.edgeShape);
 		vv.setBackground(Color.WHITE);
 
         
         JFrame frame = new JFrame("EPC Modeler");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().add(vv);
         
         // Create a graph mouse and add it to the visualization viewer
         EditingModalGraphMouse<Node, Edge> gm = new EditingModalGraphMouse<Node, Edge>(vv.getRenderContext(), sgv.vertexFactory, sgv.edgeFactory);
         
         PopupVertexEdgeMenuMousePlugin<Node, Edge> mousePlugin = new PopupVertexEdgeMenuMousePlugin<Node, Edge>();
         // Add some popup menus for the edges and vertices to our mouse plugin.
         JPopupMenu edgeMenu = new EdgeMenu(frame);
         JPopupMenu nodeMenu = new NodeMenu(frame);
         JPopupMenu createMenu = new CreateMenu(frame);
         mousePlugin.setCreatePopup(createMenu);
         mousePlugin.setEdgePopup(edgeMenu);
         mousePlugin.setVertexPopup(nodeMenu);
         gm.remove(gm.getPopupEditingPlugin());  // Removes the existing popup editing plugin
         gm.add(mousePlugin);   // Add our new plugin to the mouse
         
         vv.setGraphMouse(gm);
         
         // Let's add a menu for changing mouse modes
         JMenuBar menuBar = new JMenuBar();
         
         JMenu fileMenu = new JMenu("File");
 		fileMenu.setPreferredSize(new Dimension(80,20));
 		JMenuItem a = new JMenuItem();
 		SaveGraph saveGraph = new SaveGraph(layout);
 		a.setAction(saveGraph);
 		a.setText("Save");
 		fileMenu.add(a);
 		menuBar.add(fileMenu);
         
         JMenu modeMenu = gm.getModeMenu();
         modeMenu.setText("Mode");
         modeMenu.setIcon(null);
         modeMenu.setPreferredSize(new Dimension(80,20)); // Change the size so I can see the text
         
         menuBar.add(modeMenu);
         frame.setJMenuBar(menuBar);
         gm.setMode(ModalGraphMouse.Mode.EDITING); // Start off in editing mode
         frame.pack();
         frame.setVisible(true); 
 
 	}
 	
 	public static void pushToPeers(String gml, String target) {
 		List<InetAddress> allowedPeers = new LinkedList<InetAddress>();
 		SAXBuilder parser = new SAXBuilder();
 		String accessString = "";
 		try {
 			Document doc = parser.build(new StringReader(gml));
 			Element root = doc.getRootElement();
 			for (Object e : root.getChildren()) {
 				Element f = (Element)e;
 				if(f.getName().equals("data")) {
 					String key = f.getAttribute("key").getValue();
 					if(key.equals("access")) {
 						accessString = f.getValue();
 					}
 				}
 			}
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		if(!accessString.equals("") && !accessString.equals("public") && !accessString.equals("private")) {
 			allowedPeers = NodeUtil.getAllowedPeers(accessString);
 		} else if(!accessString.equals("private")) {
 			allowedPeers = EPCModeler.peers;
 		} else {
 			allowedPeers = new LinkedList<InetAddress>();
 		}
 		
 		for(InetAddress ia : allowedPeers) {
 			try {
 				new HTTPClient(ia.getHostName(), target, gml);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	public static InetAddress getMaxNodeGraph(){
 		List<InetAddress> ias = new LinkedList<InetAddress>();
 		ias = EPCModeler.peers;
 		InetAddress returnIa= null;
 		String nodeSize="0";
		String peerNodeSize = "0";
 		for(InetAddress ia : ias) {
 			try {
 				HTTPClient httpClient = new HTTPClient(ia.getHostName(), "/getNodeSize", "");
				peerNodeSize = httpClient.getResponseString();
				
 				if(Integer.getInteger(peerNodeSize)>Integer.getInteger(nodeSize)){
 					nodeSize=peerNodeSize;
 					returnIa = ia;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return returnIa;
 	}
 
 }
