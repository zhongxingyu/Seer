 package bigstreet.models;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Stroke;
 import java.awt.font.TextAttribute;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import org.apache.commons.collections15.Predicate;
 import org.apache.commons.collections15.Transformer;
 
 import edu.uci.ics.jung.algorithms.layout.DAGLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.algorithms.layout.SpringLayout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.Context;
 import edu.uci.ics.jung.visualization.Layer;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ViewScalingControl;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
 import bigstreet.controllers.AppController;
 import bigstreet.views.NNNPickingGraphMousePlugin;
 import bigstreet.views.NNNVertexLabelRenderer;
 import java.io.Serializable;
 
 public class NNNGraph implements Serializable {
 	//***** DATA MEMBERS *****//
 	private Graph<GraphNode, GraphEdge> g;
 	private Hashtable<String, GraphNode> diagnoses;
 	private Hashtable<String, GraphNode> outcomes;
 	private Hashtable<String, GraphNode> interventions;
 	
 	//private List<Diagnosis> displayedDiagnoses;
 	//private List<Diagnosis> correlatedDiagnoses;
 	
 	transient VisualizationViewer<GraphNode,GraphEdge> vv;
 	transient DefaultModalGraphMouse graphMouse;
 	transient ViewScalingControl zoomControl;
 
 	//***** CONSTRUCTORS *****//
 	public NNNGraph()
 	{
 		// Graph<V, E> where V is the type of the vertices 
 		// and E is the type of the edges 
 		g = new DirectedSparseMultigraph<GraphNode, GraphEdge>();
 		diagnoses = new Hashtable<String, GraphNode>();
 		outcomes = new Hashtable<String, GraphNode>();
 		interventions = new Hashtable<String, GraphNode>();		
 	}
 
 	//***** METHODS *****//
 	
 	// PUBLIC
 	public VisualizationViewer getView()
 	{
 		// Set up the vertex position transformer
 		Transformer<GraphNode, Point2D> vertexPositionTransformer = 
 			new Transformer<GraphNode, Point2D>(){
 
 				public Point2D transform(GraphNode node) {
 					return node.getLocation();
 				}
 		
 		};
 		
 		
 		// The Layout<V, E> is parameterized by the vertex and edge types 
 		Layout<GraphNode, GraphEdge> layout = new StaticLayout(g, vertexPositionTransformer);//new DAGLayout(g);
 		//((SpringLayout) layout).setForceMultiplier(10);
 		//((SpringLayout) layout).setRepulsionRange(15);
 		layout.setSize(new Dimension(AppController.GRAPH_SIZE)); // sets the initial size of the space
 		
 		
 		// The BasicVisualizationServer<V,E> is parameterized by the edge types 
 		vv = new VisualizationViewer<GraphNode, GraphEdge>(layout); 
 		vv.setPreferredSize(new Dimension(AppController.WINDOW_SIZE)); //Sets the viewing area size
 		
 		// Rotate so diagnoses are on top
 		//Dimension d = layout.getSize();
     	//Point2D center = new Point2D.Double(d.width/2, d.height/2);
     	//vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).rotate(Math.PI / 2.0, center);
 		
 		
 		
 		// Setup up a new vertex to paint transformer... 
 		Transformer<GraphNode,Paint> vertexPaintTransformer = new Transformer<GraphNode,Paint>() {
 			public Paint transform(GraphNode n) { 
 				Color color;
 				
 				if(n.getRenderMode() == RenderMode.NORMAL)
 				{
 					switch(n.getType())
 					{
 						case DIAGNOSIS:
 							color = new Color(0.0f, 0.0f, 0.5f, 0.8f);
 							
 							// Color correlated Diagnoses
 							if (AppController.getSelectedNodes().size() == 1)
 							{
 								// For now we can only handle selected diagnoses
 								NNNObject nnnObj = ((GraphNode)AppController.getSelectedNodes().get(0)).getNNNObject();
 								if (!(nnnObj instanceof Diagnosis)) break;
 								
 								Diagnosis thisDiagnosis = (Diagnosis)n.getNNNObject();
 								Diagnosis selectedDiagnosis = (Diagnosis)nnnObj;
 								
 								// Color positively correlated nodes blue
 								if (selectedDiagnosis.getCorrelatedDiagnoses().contains(thisDiagnosis))
 									color = Color.BLUE;
 								
 								// Color negatively correlated nodes red
 								if (selectedDiagnosis.getNegativelyCorrelatedDiagnoses().contains(thisDiagnosis))
 									color = Color.RED;
 							}
 							break;
 						case OUTCOME:
 							color = new Color(0.0f, 0.5f, 0.0f, 0.8f);
 							
 							// Color correlated Outcomes
 							if (AppController.getSelectedNodes().size() == 1)
 							{
 								// For now we can only handle selected diagnoses
 								NNNObject nnnObj = ((GraphNode)AppController.getSelectedNodes().get(0)).getNNNObject();
 								if (!(nnnObj instanceof Diagnosis)) break;
 								
 								Outcome thisOutcome = (Outcome)n.getNNNObject();
 								Diagnosis selectedDiagnosis = (Diagnosis)nnnObj;
 								
 								// Color positively correlated nodes blue
 								if (selectedDiagnosis.getCorrelatedOutcomes().contains(thisOutcome))
 									color = Color.BLUE;
 								
 								// Color negatively correlated nodes red
 								if (selectedDiagnosis.getNegativelyCorrelatedOutcomes().contains(thisOutcome))
 									color = Color.RED;
 							}
 							break;
 						case INTERVENTION:
 							color = new Color(0.5f, 0.0f, 0.0f, 0.8f);
 							break;
 						default:
 							color = Color.BLACK;
 							break;
 					}
 				}
 				else if (n.getRenderMode() == RenderMode.SELECTED)
 				{
 					color = new Color(0.0f, 0.5f, 1.0f, 1.0f);
 				}
 				else
 				{
 					color = Color.BLACK;
 				}
 				
 				
 				return color;
 			}
 		};
 		
 		// Set up a new stroke Transformer for the edges 
 		final Stroke defaultEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f); 
 		final Stroke outcomeEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f); 
 		final Stroke majorInterventionEdgeStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f); 
 		final Stroke suggestedInterventionEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
 		final float optionalInterventionDash[] = {10.0f};
 		final Stroke optionalInterventionEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, optionalInterventionDash, 0.0f);
 		
 		
 		Transformer<GraphEdge, Stroke> edgeStrokeTransformer = 
 			new Transformer<GraphEdge, Stroke>() { 
 				public Stroke transform(GraphEdge e) {
 					switch(e.getType())
 					{
 						case OUTCOME:
 							return outcomeEdgeStroke;
 						case MAJOR_INTERVENTION:
 							return majorInterventionEdgeStroke;
 						case SUGGESTED_INTERVENTION:
 							return suggestedInterventionEdgeStroke;
 						case OPTIONAL_INTERVENTION:
 							return optionalInterventionEdgeStroke;
 						default:
 							return defaultEdgeStroke;
 					}
 				}
 			};
 			
 		Transformer<GraphEdge, Paint> edgeDrawPaintTransformer = 
 			new Transformer<GraphEdge, Paint>() { 
 				public Paint transform(GraphEdge e) {
 					Color color;
 					
 					switch(e.getRenderMode())
 					{
 						case NORMAL:
 							color = Color.BLACK;
 							break;
 						case GHOSTED:
 							color = new Color(0.0f, 0.0f, 0.0f, 0.1f);
 							break;
 						default:
 							color = Color.BLACK;
 							break;
 					}
 					
 					return color;
 				}
 			};
 			
 		Transformer<GraphNode, Paint> vertexDrawPaintTransformer = 
 			new Transformer<GraphNode, Paint>() { 
 				public Paint transform(GraphNode n) {
 					Color color;
 					
 					switch(n.getRenderMode())
 					{
 						default:
 							color = Color.BLACK;
 							break;
 					}
 					
 					return color;
 				}
 			};
 			
 		Transformer<GraphNode, String> nodeLabelTransformer = 
 			new Transformer<GraphNode, String>() { 
 				public String transform(GraphNode n) {
 					String label;
 					
 					switch(n.getType())
 					{
 						case DIAGNOSIS:
 							label = "DIAGNOSIS<>" + n.getName();
 							break;
 						case OUTCOME:
 							label = "OUTCOME<>" + n.getName();
 							break;
 						case INTERVENTION:
 							label = "INTERVENTION<>" + n.getName();
 							break;
 						default:
 							label = n.toString();
 							break;
 					}
 					
 					return label;
 				}
 			};
 			
 		Transformer<GraphNode, Font> nodeFontTransformer = 
 			new Transformer<GraphNode, Font>() { 
 				public Font transform(GraphNode n) {
 					HashMap<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
 					
 					switch(n.getRenderMode())
 					{
 						case SELECTED:
 							attributes.put(TextAttribute.FOREGROUND, new Color(0.0f, 0.5f, 1.0f, 1.0f));
 							break;
 						case NORMAL:
 							attributes.put(TextAttribute.FOREGROUND, new Color(0.0f, 0.0f, 0.0f, 1.0f));
 							break;
 						default:
 							attributes.put(TextAttribute.FOREGROUND, new Color(0.0f, 0.0f, 0.0f, 1.0f));
 							break;
 					}
 					
 					return new Font(attributes);
 				}
 			};
 
 		class NNNVertexPredicate<V,E> 
 	    	implements Predicate<Context<Graph<GraphNode, GraphEdge>, GraphNode>> {
 
 	        public boolean evaluate(Context<Graph<GraphNode,GraphEdge>,GraphNode> c) {
 	            return true;//((GraphNode)c.element).getRenderMode() != RenderMode.INVISIBLE;
 	        }
 	        
 	    }
 		
 		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaintTransformer);
 		vv.getRenderContext().setVertexDrawPaintTransformer(vertexDrawPaintTransformer);
 		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
 		vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaintTransformer);
 		vv.getRenderContext().setVertexLabelTransformer(nodeLabelTransformer);
 		vv.getRenderContext().setArrowDrawPaintTransformer(edgeDrawPaintTransformer);
 		vv.getRenderContext().setArrowFillPaintTransformer(edgeDrawPaintTransformer);
 		vv.getRenderContext().setVertexFontTransformer(nodeFontTransformer);
 		vv.getRenderContext().setVertexIncludePredicate(new NNNVertexPredicate<GraphNode, GraphEdge>());
 		vv.getRenderer().setVertexLabelRenderer(new NNNVertexLabelRenderer<GraphNode, GraphEdge>());
 		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
 		
 		
 		
 		//Default mouse and keyboard interactivity
 		//DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
 		graphMouse = new DefaultModalGraphMouse();
 		graphMouse.setMode(DefaultModalGraphMouse.Mode.TRANSFORMING);
 		graphMouse.add(new NNNPickingGraphMousePlugin());
 		vv.setGraphMouse(graphMouse);
 		vv.addKeyListener(graphMouse.getModeKeyListener());
 		
 		zoomControl = new ViewScalingControl();
 		
 		return vv;
 	}
 	
 	
 	
 	public void setMouseMode(ModalGraphMouse.Mode mode)
 	{
 		graphMouse.setMode(mode);
 	}
 	
 	public void zoomIn()
 	{
 		zoomControl.scale(vv, 1.1f, new Point(0,0));
 	}
 	
 	public void zoomOut()
 	{
 		zoomControl.scale(vv, 0.9f, new Point(0,0));
 	}
 	
 	public void saveScreenShot(String path)
 	{
 //		Dimension size = myGraph.getSize();
 //		BufferedImage myImage =
 //		    new BufferedImage(size.width, size.height,
 //		        BufferedImage.TYPE_INT_RGB);
 //		Graphics2D g2 = myImage.createGraphics();
 //		myGraph.paintComponent(g2);
 //		try {
 //			OutputStream out = new FileOutputStream(filename);
 //		    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 //		    encoder.encode(myImage);
 //		    out.close();
 //		} catch (Exception e) {
 //			System.out.println(e);
 //		}
 		
 		
 		Dimension size = AppController.GRAPH_SIZE;
 
 	    BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_BGR);
 	    Graphics2D graphics = bi.createGraphics();
 	    graphics.setColor(Color.WHITE);
 	    graphics.fillRect(0,0, size.width, size.height);
 	    
 	    Rectangle oldBounds = vv.getBounds();
 	    vv.setBounds(0, 0, size.width, size.height);
 	    vv.paint(graphics);
 	    vv.setBounds(oldBounds);
 
 	    try{
 	       ImageIO.write(bi,"jpeg",new File(path));
 	    }catch(Exception e){e.printStackTrace();}
 	}
 	
 	public void addDiagnosis(Diagnosis diagnosis)
 	{
 		if (!diagnoses.containsKey(diagnosis.code))
 		{
 			// Add a node to the graph
 			GraphNode newNode = new GraphNode(diagnosis);
 			diagnoses.put(diagnosis.code, newNode);
 			
 			g.addVertex(newNode);
 			AppController.graphUpdated();
 			
 			// Check to see if diagnosis should be connected
 			// to any outcomes already present in the graph
 			for (Outcome o : diagnosis.getOutcomes()){
 				if (outcomes.containsKey(o.code)){
 					GraphNode outcomeNode = (GraphNode) outcomes.get(o.code);
 					g.addEdge(new GraphEdge(EdgeType.OUTCOME, RenderMode.GHOSTED, newNode, outcomeNode), newNode, outcomeNode);
 				}
 			}
 			
 			//for testing; remove:
 			//for (Outcome o : diagnosis.getOutcomes()){
 			//	addOutcome(o);
 			//}
 		}
 	}
 	
 	public void addOutcome(Outcome outcome)
 	{
 		if (!outcomes.containsKey(outcome.code))
 		{
 			// Add node to graph
 			GraphNode newNode = new GraphNode(outcome);
 			outcomes.put(outcome.code, newNode);
 			
 			g.addVertex(newNode);
 			AppController.graphUpdated();
 			
 			// Check to see if outcome should be connected
 			// to any diagnoses already present in the graph
 			for (Object obj : diagnoses.values()){
 				GraphNode diagnosisNode = (GraphNode)obj;
 				for (Outcome o : ((Diagnosis)diagnosisNode.getNNNObject()).getOutcomes()){
					if (o.code.equals(outcome.code)){
 						RenderMode renderMode;
 						if (diagnosisNode.getSelected())
 							renderMode = RenderMode.NORMAL;
 						else
 							renderMode = RenderMode.GHOSTED;
 						
 						g.addEdge(new GraphEdge(EdgeType.OUTCOME, renderMode, diagnosisNode, newNode), diagnosisNode, newNode);
 					}
 				}
 			}
 			
 			// Check to see if outcome should be connected
 			// to any interventions already present in the graph
 			for (EdgeType t : EdgeType.values())
 			{
 				if(t == EdgeType.OUTCOME) continue; //We only want interventions
 				
 				for (Intervention i : outcome.getInterventions(t))
 				{
 					if (interventions.containsKey(i.code)){
 						GraphNode interventionNode = (GraphNode) interventions.get(i.code);
 						g.addEdge(new GraphEdge(t, RenderMode.GHOSTED, newNode, interventionNode), newNode, interventionNode);
 					}
 					
 					// For testing; remove:
 					//addIntervention(i);
 				}
 			}
 			
 		}
 		else
 		{
 			((GraphNode)outcomes.get(outcome.code)).addNNNObject(outcome);
 		}
 	}
 	
 	public void addIntervention(Intervention intervention)
 	{
 		if (!interventions.containsKey(intervention.code))
 		{
 			// Add node to graph
 			GraphNode newNode = new GraphNode(intervention);
 			interventions.put(intervention.code, newNode);
 			
 			g.addVertex(newNode);
 			AppController.graphUpdated();
 			
 			// Check to see if intervention should be connected
 			// to any outcomes already present in the graph
 			for (Object obj : outcomes.values()){
 				GraphNode outcomeNode = (GraphNode)obj;
 				for (NNNObject nnnObj : outcomeNode.getNNNObjects()){
 					Outcome o = (Outcome)nnnObj;
 					for (EdgeType t : EdgeType.values())
 					{
 						if(t == EdgeType.OUTCOME) continue; //We only want interventions
 						
 						for (Intervention i : o.getInterventions(t))
 						{
 							if (i.code == intervention.code){
 								RenderMode renderMode;
 								if (outcomeNode.getSelected())
 									renderMode = RenderMode.NORMAL;
 								else
 									renderMode = RenderMode.GHOSTED;
 								
 								g.addEdge(new GraphEdge(t, renderMode, outcomeNode, newNode), outcomeNode, newNode);
 							}
 						}
 					}
 				}
 			}
 			
 		}
 		else
 		{
 			// necessary?
 			((GraphNode)interventions.get(intervention.code)).addNNNObject(intervention);
 		}
 	}
 	
 	
 //	private void oldaddDiagnosis(Diagnosis diagnosis)
 //	{
 //		// Get a list of names of nodes already in the graph
 //		Hashtable existingNodes = new Hashtable<String, GraphNode>();
 //		for (GraphNode n : g.getVertices())
 //		{
 //			existingNodes.put(n.getName(), n);
 //		}
 //		
 //		if (existingNodes.containsKey(diagnosis.getName())) return;
 //		
 //		//Add diagnosis node
 //		GraphNode diagnosisNode = new GraphNode(diagnosis);
 //		g.addVertex(diagnosisNode);
 //		existingNodes.put(diagnosisNode.getName(), diagnosisNode);
 //		
 //		//Add outcomes
 //		for (Outcome o : diagnosis.getOutcomes())
 //		{
 //			// Get a GraphNode representing Outcome o
 //			GraphNode outcomeNode;
 //			if (existingNodes.containsKey(o.getName()))
 //			{
 //				outcomeNode = (GraphNode) existingNodes.get(o.getName());
 //			}
 //			else
 //			{
 //				outcomeNode = new GraphNode(o);
 //				existingNodes.put(o.getName(), outcomeNode);
 //			}
 //			
 //			// Add outcomeNode as a vertex in g
 //			if(!g.containsVertex(outcomeNode))
 //				g.addVertex(outcomeNode);
 //			
 //			// Add an edge between diagnosisNode and outcomeNode
 //			g.addEdge(new GraphEdge(EdgeType.OUTCOME, diagnosisNode, outcomeNode), diagnosisNode, outcomeNode);
 //			
 //			
 //			//Add interventions
 //			for (EdgeType t : EdgeType.values())
 //			{
 //				if(t == EdgeType.OUTCOME) continue; //We only want interventions
 //				
 //				for (Intervention h : o.getInterventions(t))
 //				{
 //					//Get a GraphNode representing Intervention h
 //					GraphNode interventionNode;
 //					if(existingNodes.containsKey(h.getName()))
 //					{
 //						interventionNode = (GraphNode) existingNodes.get(h.getName());
 //					}
 //					else
 //					{
 //						interventionNode = new GraphNode(h);
 //						existingNodes.put(h.getName(), interventionNode);
 //					}
 //					
 //					// Add interventionNode as a vertex in g
 //					if(!g.getVertices().contains(interventionNode))
 //						g.addVertex(interventionNode);
 //					
 //					if(!g.getSuccessors(outcomeNode).contains(interventionNode))
 //					{
 //						// Add an edge between outcomeNode and interventionNode
 //						g.addEdge(new GraphEdge(t, outcomeNode, interventionNode), outcomeNode, interventionNode);
 //					}
 //				}
 //			}
 //		}
 //	}
 
 }
