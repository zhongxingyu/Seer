 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Eclipse
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.eclipse;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Menu;
 import java.awt.MenuItem;
 import java.awt.Paint;
 import java.awt.PopupMenu;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.UIManager;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.IController;
 import de.tuilmenau.ics.fog.eclipse.ui.Activator;
 import de.tuilmenau.ics.fog.eclipse.utils.SWTAWTConverter;
 import de.tuilmenau.ics.fog.routing.simulated.PartialRoutingService;
 import de.tuilmenau.ics.fog.routing.simulated.RemoteRoutingService;
 import de.tuilmenau.ics.fog.routing.simulated.RoutingServiceAddress;
 import de.tuilmenau.ics.fog.topology.Breakable;
 import de.tuilmenau.ics.fog.topology.Breakable.Status;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.DummyForwardingElement;
 import de.tuilmenau.ics.fog.transfer.Gate;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.ui.Decoration;
 import de.tuilmenau.ics.fog.ui.Decorator;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Marker;
 import de.tuilmenau.ics.fog.ui.MarkerContainer;
 import de.tuilmenau.ics.fog.ui.PacketLogger;
 import de.tuilmenau.ics.graph.RoutableGraph;
 import de.tuilmenau.ics.graph.Transformer;
 import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.ObservableGraph;
 import edu.uci.ics.jung.graph.event.GraphEvent;
 import edu.uci.ics.jung.graph.event.GraphEventListener;
 import edu.uci.ics.jung.graph.util.Context;
 import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
 import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
 import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
 import edu.uci.ics.jung.visualization.FourPassImageShaper;
 import edu.uci.ics.jung.visualization.Layer;
 import edu.uci.ics.jung.visualization.RenderContext;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
 import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;
 import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
 import edu.uci.ics.jung.visualization.renderers.Renderer;
 import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
 import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
 
 
 /**
  * Class for displaying a graph in an AWT window.
  * 
  * @param <NodeObject> Class for the vertices objects of the graph 
  * @param <LinkObject> Class for the edge objects of the graph
  */
 public class GraphViewer<NodeObject, LinkObject> implements Observer, Runnable
 {
 	// scaling factor for lines: useful for enlarging lines for presentations
 	private final static float SCALE_FACTOR = 1.0f;
 	
 	private final static float VERTEX_STROKE_NORMAL  = 1.0f *SCALE_FACTOR;
 	private final static float VERTEX_STROKE_ADD_MAX_MSG = 4.0f;
 	
 	private final static float EDGE_STROKE_NORMAL  = 1.0f *SCALE_FACTOR;
 	private final static float EDGE_STROKE_LOGICAL = 0.5f *SCALE_FACTOR;
 	private final static float EDGE_STROKE_GATE    = 1.5f *SCALE_FACTOR;
 	
 	public static final int VERTEX_RECTANGLE_HEIGHT = 20;
 	public static final int VERTEX_LABEL_CHAR_WIDTH = 7;
 	public static final int VERTEX_LABEL_ADD_WIDTH = 10;
 	
 	public static final String DEFAULT_DECORATION = "Default";
 	
 	/**
 	 * Font sizes for text; -1=default
 	 */
 	public static final int VERTEX_FONT_SIZE = -1;
 	public static final int EDGE_FONT_SIZE = -1;
 
 	private final static int   MAX_NUMBER_MSG_GATE = 1;
 	private final static float EDGE_STROKE_GATE_ADD_MAX_MSG = 1.0f;
 	
 	private static Color DEFAULT_VERTEX_COLOR = new Color(0f, 0f, 0f, 0.5f);
 	private static Color DEFAULT_EDGE_COLOR = Color.BLACK;
 	private static Color ERROR_EDGE_COLOR = Color.ORANGE;
 	
 	private static Color MULTIPLE_MARKING_COLOR = Color.RED;
 	
 	
 	public GraphViewer(IController pController)
 	{
 		mController = pController;
 	}
 	
 	public Component getComponent()
 	{
 		return mViewer;
 	}
 	
 	public void init(RoutableGraph<NodeObject, LinkObject> pGraph)
 	{
 		mViewUpdateRunning = false;
 		
 		createGraphView(pGraph.getGraphForGUI());
 		createInteraction();
 
 		pGraph.addObserver(this);
 	}
 
 	@Override
 	public void update(Observable observable, Object parameter)
 	{
 		synchronized (this) {
 			// if it is already running: do not proceed
 			if(mViewUpdateRunning) {
 				return;
 			}
 			
 			mViewUpdateRunning = true;
 		}
 		
 		if(EventQueue.isDispatchThread()) {
 			run();
 		} else {
 			// switch to AWT event queue
 			EventQueue.invokeLater(this);
 		}
 	}
 	
 	@Override
 	public void run()
 	{
 		mViewer.updateUI();
 		
 		synchronized (this) {
 			mViewUpdateRunning = false;
 		}
 	}
 	
 	/**
 	 * Calculated a load value [0, 1] for an element. According to this
 	 * value, the GUI can highlight the element.
 	 * 
 	 * @param obj Element to calculate the load for
 	 * @return load value [0, 1] OR negative if no load can be calculated
 	 */
 	protected static float getLoadLevel(Object obj)
 	{
 		PacketLogger log = PacketLogger.getLogger(obj);
 		if(log != null) {
 			return ((float)log.size()/(float)log.getMaxSize());
 		}
 		
 		if(obj instanceof Gate) {
 			return Math.min(1.0f, (float)((Gate) obj).getNumberMessages(false)/(float)MAX_NUMBER_MSG_GATE);
 		}
 
 		return -1;
 	}
 	
 	public class BentLine<V,E> extends AbstractEdgeShapeTransformer<V,E> implements IndexedRendering<V,E>
 	{
 		/**
 		 * singleton instance of the BentLine shape
 		 */
 		private GeneralPath instance = new GeneralPath();
 		
 		protected EdgeIndexFunction<V,E> parallelEdgeIndexFunction;
 
 		public void setEdgeIndexFunction(EdgeIndexFunction<V,E> parallelEdgeIndexFunction)
 		{
 			this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
 	//		loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
 		}
 
 		/**
 		 * @return the parallelEdgeIndexFunction
 		 */
 		public EdgeIndexFunction<V, E> getEdgeIndexFunction()
 		{
 			return parallelEdgeIndexFunction;
 		}
 
 		/**
 		 * Get the shape for this edge, returning either the
 		 * shared instance or, in the case of self-loop edges, the
 		 * Loop shared instance.
 		 */
 		public Shape transform(Context<Graph<V,E>,E> context)
 		{
 			E e = context.element;
 
 			instance.reset();
 			instance.moveTo(0.0f, 0.0f);
 
 			if(e instanceof NetworkInterface) {
 				NetworkInterface ni = (NetworkInterface) e;
 				
 				PacketLogger log = PacketLogger.getLogger(ni.getBus());
 				
 				if(log != null) {
 					int parts = log.size() +1;
 					float dist = 1.0f / (float) parts;
 					
 					for(int i=0; i<parts; i++) {
 						instance.lineTo(i*dist, 0.0f);
 						instance.lineTo(i*dist, 1.0f);
 						instance.lineTo(i*dist, -1.0f);
 						instance.moveTo(i*dist, 0.0f);
 					}
 				}
 			}
 			
 			instance.lineTo(1.0f, 0.0f);
 			
 			return instance;
 		}
 	}
 
 	public class vertexLabelRenderer extends BasicVertexLabelRenderer<NodeObject, LinkObject> {
 	
 		// the space between vertex shape and label
 		final double tLabelOffset = 10; 
 		
 		final Color labelBackgroundColor = new Color(255,255,128);
 	
 		public Component prepareRenderer(RenderContext<NodeObject,LinkObject> pRc, EdgeLabelRenderer pGraphLabelRenderer, Object pValue, boolean pIsSelected, NodeObject pNode) 
 		{
 			return pRc.getVertexLabelRenderer().<NodeObject>getVertexLabelRendererComponent(pRc.getScreenDevice(), pValue, pRc.getVertexFontTransformer().transform(pNode), pIsSelected, pNode);
 		}
 	    
 		public void labelVertex(RenderContext<NodeObject, LinkObject> pRc, Layout<NodeObject, LinkObject> pLayout, NodeObject pNode, String pLabel) 
 		{
 	        GraphicsDecorator tGraphicsDecorator = pRc.getGraphicsContext();
 	    	Shape tShape = pRc.getVertexShapeTransformer().transform(pNode);
 	    	Point2D tNodePos = pLayout.transform(pNode);
 	    	tNodePos = pRc.getMultiLayerTransformer().transform(Layer.LAYOUT, tNodePos);
 
 	    	// does the node itself contain decoration information?
 	    	if(pNode instanceof Decorator) {
 	    		String tExplicitLabel = ((Decorator) pNode).getText();
 	    		if(tExplicitLabel != null) {
 	    			pLabel = tExplicitLabel;
 	    		}
 	    	}
 	    	
 	    	// is there a decoration container with some additional information?
 	    	if(mDecoration != null) {
 	    		Decorator tDec = mDecoration.getDecorator(pNode);
 	    		if(tDec != null) {
 	    			String tAddLabel = tDec.getText();
 	    			if(tAddLabel != null) {
 	    				pLabel += " " +tAddLabel;
 	    			}
 	    		}
 	    	}
 	    	
 	        Component tComponent = prepareRenderer(pRc, pRc.getEdgeLabelRenderer(), (Object)pLabel, pRc.getPickedVertexState().isPicked(pNode), pNode);
 
 	        Dimension tDimension = tComponent.getPreferredSize();
 	        tDimension.width += VERTEX_LABEL_ADD_WIDTH;
 	        
 	        int tPosX = (int) (tNodePos.getX() - (tDimension.width - VERTEX_LABEL_ADD_WIDTH)/ 2);
 	        int tPosY = (int) (tNodePos.getY() - tDimension.height / 2);
 	        
 	        Rectangle tShapeBound = tShape.getBounds();
 	        if (tShapeBound.getHeight() != VERTEX_RECTANGLE_HEIGHT)
 	        {
 	        	tPosY += tLabelOffset + tShapeBound.getHeight() / 2;
 	        	
 		        //picture for hosts is special -> use an additional offset
 		        if (pNode instanceof Node)
 		        	tPosX += 4;
 	        }	        	        
 	        
 	        // finally draw the label component
 	        tGraphicsDecorator.draw(tComponent, pRc.getRendererPane(), tPosX, tPosY, tDimension.width, tDimension.height, true);
 		}
 	}
 
 	private Image loadImageFor(Object pObj)
 	{
 		if(pObj instanceof DummyForwardingElement) {
 			pObj = ((DummyForwardingElement) pObj).getObject();
 		}
 		
 		String imageName = pObj.getClass().getCanonicalName() +".gif";
 		
 		// is there a different name given by a decorator?
 		if(pObj instanceof Decorator) {
 			String decorationImageName = ((Decorator) pObj).getImageName();
 			if(decorationImageName != null) imageName = decorationImageName;
 		} else {
 			if(mDecoration != null) {
 				Decorator decorator = mDecoration.getDecorator(pObj);
 				if(decorator != null) {
 					String decorationImageName = decorator.getImageName();
 					if(decorationImageName != null) {
 						imageName = decorationImageName;
 					}
 				}
 			}
 		}
 		
 		return loadImage(imageName);
 	}
 	
 	private Image loadImage(String pFile)
 	{
 		BufferedImage tImage = mIcons.get(pFile);
 		if(tImage == null) {
 			if(!mIcons.containsKey(pFile)) {
 				ImageDescriptor tImageDescr = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/" + pFile);
 				
 				if(tImageDescr != null) {
 					tImage = SWTAWTConverter.convertToAWT(tImageDescr.getImageData());
 				}
 				
 				// if tImage is null, store the null pointer
 				// to indicate that there is no image available
 				mIcons.put(pFile, tImage);
 			}
 			// else: already tried, but stored null pointer
 		}
 		
 		return tImage;
 	}
 
 	private Icon loadIconFor(Object pObj)
 	{
 		try {
 			Image tImage = loadImageFor(pObj);
 			if(tImage != null) {
 				return new ImageIcon(tImage);
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Creates the objects needed for the view. Most of them bases on the
 	 * JUNG lib.
 	 * 
 	 * @param pX Width of the window
 	 * @param pY Height of the window
 	 * @param pGraph Graph, which should be displayed
 	 */
 	private void createGraphView(Graph<NodeObject, LinkObject> pGraph)
 	{
 		ObservableGraph<NodeObject, LinkObject> tObsGraph = new ObservableGraph<NodeObject, LinkObject>(pGraph);
 
 		tObsGraph.addGraphEventListener(new GraphEventListener<NodeObject, LinkObject>() {
 			public void handleGraphEvent(GraphEvent<NodeObject, LinkObject> pEvent) {
 				updateVisualization();
 			}
 		});
 		
 		// create layout
 		mLayout = new edu.uci.ics.jung.algorithms.layout.FRLayout2<NodeObject, LinkObject>(tObsGraph);
 		
 		Shell activeShell = Display.getDefault().getActiveShell();
 		org.eclipse.swt.graphics.Rectangle tRectangle;
 		if(activeShell != null) {
 			tRectangle = activeShell.getBounds();
 		} else {
 			tRectangle = new org.eclipse.swt.graphics.Rectangle(0,0, 300,300);
 		}
 		
 		double tWidth = tRectangle.width * 0.4;
 		double tHeight = tRectangle.height*0.4;
 		//Logging.log(this, "Old size was " + tRectangle.width + "x" + tRectangle.height + " while new size will be " + tWidth + "x" + tHeight);
 		
 		mLayout.setSize(new Dimension((int)tWidth, (int)tHeight));
 		
 		// create viewer
 		mViewer = new VisualizationViewer<NodeObject, LinkObject>(mLayout);
 		mViewer.setDoubleBuffered(true);
 		mViewer.setPreferredSize(new Dimension((int)tWidth, (int)tHeight));
 		
 		// setting up view of graph itself
 		// Setup up a new vertex to paint transformer...
 		Transformer<NodeObject, Paint> vertexPaint = new Transformer<NodeObject, Paint>()
 		{				
 			private final Color colorBlue = new Color(.1f, .3f, 1f);
 			private final Color colorYellow = new Color(1f, 1f, .3f);
 			private final Color colorGreen = new Color(.1f, .9f, .3f);
 			
 			public Paint transform(Object i)
 			{
 				if(i instanceof DummyForwardingElement) {
 					i = ((DummyForwardingElement) i).getObject();
 				}
 				
 				if (i instanceof AbstractGate) return Color.LIGHT_GRAY;
 				if (i instanceof GateContainer) return colorGreen;
 				if (i instanceof ILowerLayer) return colorYellow;
 				
 				// does the object define its color by itself?
 				if(i instanceof Decorator) {
 					Color decColor = ((Decorator) i).getColor();
 					if(decColor != null) {
 						return decColor;
 					}
 				}
 				
 				// is there an external decorator for the object?
 				if(mDecoration != null) {
 					Decorator decorator = mDecoration.getDecorator(i);
 					if(decorator != null) {
 						Color decColor = decorator.getColor();
 						if(decColor != null) {
 							return decColor;
 						}
 					}
 				}
 				
 				if (i instanceof PartialRoutingService) return Color.ORANGE;
 				if (i instanceof RemoteRoutingService) return colorBlue;
 				if (i instanceof RoutingServiceAddress) return colorBlue;
 
 				return Color.WHITE;
 			}
 		};
 		
 		Transformer<NodeObject, Shape> vertexShapeTransformer = new Transformer<NodeObject, Shape>()
 		{
 			public Shape transform(NodeObject pObj)
 			{
 				Image tImage = loadImageFor(pObj);
 				if(tImage != null) {
 					Shape tShape = FourPassImageShaper.getShape(tImage);
 					if(tShape.getBounds().getWidth() > 0 && tShape.getBounds().getHeight() > 0) {
 	                    AffineTransform transform = AffineTransform.getTranslateInstance( - tImage.getWidth(null) / 2, - tImage.getHeight(null) / 2);
 	                    tShape = transform.createTransformedShape(tShape);
 	                    return tShape;
 					}
 				}
 
 				String tLabel = mViewer.getRenderContext().getVertexLabelTransformer().transform(pObj);
 				return new Rectangle( - (tLabel.length() * VERTEX_LABEL_CHAR_WIDTH + VERTEX_LABEL_ADD_WIDTH) / 2, - VERTEX_RECTANGLE_HEIGHT / 2, tLabel.length() * VERTEX_LABEL_CHAR_WIDTH + VERTEX_LABEL_ADD_WIDTH, VERTEX_RECTANGLE_HEIGHT);
 			}
 		};
 		
 		Transformer<NodeObject, Icon> vertexIconTransformer = new Transformer<NodeObject, Icon>()
 		{
 			public Icon transform(NodeObject pObj)
 			{
 				return loadIconFor(pObj);
 			}
 		};
 		
 		// Set up a new stroke Transformer for the edges
 		Transformer<NodeObject, Stroke> vertexStrokeTransformer = new Transformer<NodeObject, Stroke>()
 		{
 			private final Stroke normalEdgeStroke = new BasicStroke(VERTEX_STROKE_NORMAL);
 			
 			public Stroke transform(Object pVertex)
 			{
 				float loadLevel = getLoadLevel(pVertex);
 
 				if(loadLevel > 0) {
 					return new BasicStroke(VERTEX_STROKE_NORMAL +VERTEX_STROKE_ADD_MAX_MSG * loadLevel);
 				} else {
 					return normalEdgeStroke;
 				}
 			}
 		};
 
 		Transformer<NodeObject, Paint> vertexDraw = new Transformer<NodeObject, Paint>() {
 			public Paint transform(Object pVertex)
 			{
 				Boolean tBroken = false;
 				if (pVertex instanceof Breakable) {
 					try {
 						tBroken = ((Breakable)pVertex).isBroken() != Status.OK;
 					}
 					catch(RemoteException exc) {
 						tBroken = true;
 					}
 				}
 
 				if (tBroken) {
 					return Color.RED;
 				} else {
 					Marker[] tMarkers = MarkerContainer.getInstance().get(pVertex);
 					
 					if(tMarkers.length > 0) {
 						if(tMarkers.length > 1) {
 							if(Config.Routing.USE_SPECIAL_MULTIPLE_MARKING_COLOR){
 								return MULTIPLE_MARKING_COLOR;
 							}else{
 								return tMarkers[tMarkers.length - 1].getColor();
 							}
 						} else {
 							return tMarkers[0].getColor();
 						}
 					} else {
 //						// if the background is colored with a special an R/G/B color, we use white as frame color in order to have readable strings 
 //						if(pVertex instanceof IElementDecorator && ((IElementDecorator)pVertex).getDecorationParameter() != null && ((IElementDecorator)pVertex).getDecorationParameter() instanceof IElementDecorator.Color) {
 //							IElementDecorator tDecorator = (IElementDecorator)pVertex;
 //							if(tDecorator.getDecorationParameter() != null){
 //								return Color.WHITE;								
 //							}
 //						}
 
 						return DEFAULT_VERTEX_COLOR;
 					}
 				}
 			}
 		};
 		
 		Transformer<LinkObject, Paint> edgeDraw = new Transformer<LinkObject, Paint>() {
 			@Override
 			public Paint transform(LinkObject pLink)
 			{
 				Marker[] tMarkers = MarkerContainer.getInstance().get(pLink);
 				
 				if(tMarkers.length > 0) {
 					if(tMarkers.length > 1) {
 						if(Config.Routing.USE_SPECIAL_MULTIPLE_MARKING_COLOR){
 							return MULTIPLE_MARKING_COLOR;
 						}else{
 							return tMarkers[tMarkers.length - 1].getColor();
 						}
 					} else {
 						return tMarkers[0].getColor();
 					}
 				} else {
 					if(pLink instanceof Gate) {
 						if(!((Gate)pLink).isOperational()) {
 							return ERROR_EDGE_COLOR;
 						}
 					}
 					
 					return DEFAULT_EDGE_COLOR;
 				}
 			}
 			
 		};
 		
 		Transformer<NodeObject, String> vertexLabeller = new Transformer<NodeObject, String>()
 		{
 			public String transform(Object s)
 			{
 				String tName = s.toString();
 				if (s instanceof RemoteRoutingService) {
 					try {
 						tName = ((RemoteRoutingService)s).getName();
 					} catch (RemoteException e) {
 						// ignore it; we will stick to the previous tName
 					}
 				}
 
 				return tName;
 			}
 		};
 		
 		// Set up a new stroke Transformer for the edges
 		Transformer<LinkObject, Stroke> edgeStrokeTransformer = new Transformer<LinkObject, Stroke>()
 		{
 			private final Stroke normalEdgeStroke = new BasicStroke(EDGE_STROKE_NORMAL);
 			private final Stroke logicalEdgeStroke = new BasicStroke(EDGE_STROKE_LOGICAL, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{10.0f}, 0.0f);
 			private final Stroke gateEdgeStroke = new BasicStroke(EDGE_STROKE_GATE);
 			
 			public Stroke transform(Object s)
 			{
 				float loadLevel = getLoadLevel(s);
 				
 				if(loadLevel > 0) {
 					return new BasicStroke(EDGE_STROKE_GATE +EDGE_STROKE_GATE_ADD_MAX_MSG * loadLevel);
 				}
 				else if(s instanceof Gate) {
 					return gateEdgeStroke;
 				}
 				else if(s.toString().regionMatches(0, "routing for", 0, "routing for".length())) {
 					return logicalEdgeStroke;
 				}
 				
 				return normalEdgeStroke;
 			}
 		};
 		
 		Transformer<LinkObject, String> edgeLabeller = new Transformer<LinkObject, String>()
 		{
 			public String transform(Object edge)
 			{
 				if(edge == null) return "null";
 				
 				// use StringBuilder in order to boost performance of string creation
 				StringBuilder tName = null;
 				
 				if(edge instanceof Gate) {
 					tName = new StringBuilder();
 					if(((Gate) edge).getGateID() != null) {
 						tName.append(((Gate) edge).getGateID());
 						tName.append(" ");
 					}
 					tName.append(edge.getClass().getSimpleName());
 					tName.append(" (");
 					tName.append(((Gate) edge).getNumberMessages(false));
 					tName.append(")");
 				}
 				else if(edge instanceof NetworkInterface) {
					return "LL_" +((NetworkInterface) edge).getLowerLayerID();
 				}
 
 				if(tName == null) return edge.toString();
 				else return tName.toString();
 			}
 		};
 		
 		EdgeLabelRenderer edgeLabelRenderer = new DefaultEdgeLabelRenderer(Color.BLACK)
 		{
 			private final Color colorBg = (Color)UIManager.getDefaults().get("Panel.background");
 			private final Color colorEdgeBackground = new Color(colorBg.getRed(), colorBg.getGreen(), colorBg.getBlue(), 128);
 			@Override
 			public <T> Component getEdgeLabelRendererComponent(JComponent arg0, Object arg1, Font arg2, boolean arg3, T arg4)
 			{
 				Component tRes = super.getEdgeLabelRendererComponent(arg0, arg1, arg2, arg3, arg4);
 				tRes.setBackground(colorEdgeBackground);
 				return tRes;
 			}
 		};
 		
 		Transformer<LinkObject, Font> edgeFontTransformer = new Transformer<LinkObject, Font>()
 		{
 			public Font transform(LinkObject edge)
 			{
 				if(edge == null) return null;
 
 				return edgeFont;
 			}
 			
 			private Font edgeFont = new Font("SansSerif", Font.PLAIN, EDGE_FONT_SIZE);
 		};
 		
 		Transformer<NodeObject, Font> vertexFontTransformer = new Transformer<NodeObject, Font>()
 		{
 			public Font transform(NodeObject vertex)
 			{
 				if(vertex == null) return null;
 
 				return vertexFont;
 			}
 			
 			private Font vertexFont = new Font("SansSerif", Font.PLAIN, VERTEX_FONT_SIZE);
 		};
 		
 		mViewer.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
 		mViewer.getRenderContext().setVertexDrawPaintTransformer(vertexDraw);
 		mViewer.getRenderContext().setVertexStrokeTransformer(vertexStrokeTransformer);
 		mViewer.getRenderContext().setVertexShapeTransformer(vertexShapeTransformer);
 		mViewer.getRenderContext().setVertexIconTransformer(vertexIconTransformer);
 		mViewer.getRenderer().setVertexLabelRenderer(new vertexLabelRenderer());
 		mViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
 //		mViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<NodeObject>());
 		mViewer.getRenderContext().setVertexLabelTransformer(vertexLabeller);
 		mViewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
 		if(VERTEX_FONT_SIZE > 0) {
 			mViewer.getRenderContext().setVertexFontTransformer(vertexFontTransformer);
 		}
 		
 //        mViewer.getRenderContext().setEdgeShapeTransformer(new BentLine<NodeObject,LinkObject>());
 		mViewer.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
 		mViewer.getRenderContext().setEdgeDrawPaintTransformer(edgeDraw);
 		mViewer.getRenderContext().setArrowDrawPaintTransformer(edgeDraw);
 		mViewer.getRenderContext().setArrowFillPaintTransformer(edgeDraw);
 		mViewer.getRenderContext().setEdgeLabelTransformer(edgeLabeller);
 		mViewer.getRenderContext().setEdgeLabelRenderer(edgeLabelRenderer);
 		if(EDGE_FONT_SIZE > 0) {
 			mViewer.getRenderContext().setEdgeFontTransformer(edgeFontTransformer);
 		}
 	}
 	
 	/**
 	 * Creating the objects reacting to user input.
 	 * In special, the handling of the mouse click events are defined here. 
 	 */
 	private void createInteraction()
 	{
 		// Create a graph mouse and add it to the visualization component
 		mMouse = new DefaultModalGraphMouse() {
 			@Override
 			public void mousePressed(MouseEvent pEvent)
 			{
 				super.mousePressed(pEvent);
 				
 				if(pEvent.isPopupTrigger()) {
 					Object pSelection = getSelection(pEvent);
 					PopupMenu popup = new PopupMenu();
 					mController.fillContextMenu(pSelection, popup);
 					
 					fillStdPopup(popup);
 					
 					// should the popup be displayed?
 					if(popup.getItemCount() > 0) {
 						if(popup.getParent() == null) {
 							pEvent.getComponent().add(popup);
 						}
 						    
 					    popup.show(pEvent.getComponent(), pEvent.getX(), pEvent.getY());
 					}
 				}
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent pEvent)
 			{
 				super.mouseReleased(pEvent);
 				
 				if(pEvent.isPopupTrigger()) {
 					Object pSelection = getSelection(pEvent);
 					PopupMenu popup = new PopupMenu();
 					
 					mController.fillContextMenu(pSelection, popup);
 					
 					fillStdPopup(popup);
 					
 					// should the popup be displayed?
 					if(popup.getItemCount() > 0) {
 						if(popup.getParent() == null) {
 							pEvent.getComponent().add(popup);
 						}
 						    
 					    popup.show(pEvent.getComponent(), pEvent.getX(), pEvent.getY());
 					}
 				}
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent pEvent)
 			{
 				super.mouseClicked(pEvent);
 
 				mController.selected(getSelection(pEvent), (pEvent.getButton() == MouseEvent.BUTTON1), pEvent.getClickCount());
 			}
 			
 			private Object getSelection(MouseEvent pEvent)
 			{
 				// try to get selected node or link
 				GraphElementAccessor<NodeObject, LinkObject> tAccessor = mViewer.getPickSupport();
 				Object tSelection = tAccessor.getVertex(mLayout, pEvent.getX(), pEvent.getY());
 				
 				if(tSelection == null) {
 					tSelection = tAccessor.getEdge(mLayout, pEvent.getX(), pEvent.getY());
 				}
 				
 				if(tSelection instanceof DummyForwardingElement) {
 					tSelection = ((DummyForwardingElement) tSelection).getObject();
 				}
 				
 				return tSelection;
 			}
 		};
 
 		mMouse.setMode(ModalGraphMouse.Mode.PICKING);
 		
 		mViewer.setGraphMouse(mMouse);	
 	}
 	
 	private void fillStdPopup(PopupMenu popup)
 	{
 		MenuItem mi;
 		
 		// if already some items listed, separate them from the default 
 		if(popup.getItemCount() > 0) {
 			popup.addSeparator();
 		}
 
 		mi = new MenuItem("Picking");
 		mi.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(mMouse != null)
 					mMouse.setMode(ModalGraphMouse.Mode.PICKING);
 			}
 		});
 		popup.add(mi);
 
 		mi = new MenuItem("Transforming");
 		mi.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(mMouse != null)
 					mMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
 			}
 		});
 		popup.add(mi);
 		
 		//
 		// Add decorator selection menu
 		//
 		Menu decMenu = new Menu("Decoration types");
 		popup.add(decMenu);
 		
 		mi = new MenuItem("None");
 		mi.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mDecoration = null;
 				update(null, null);
 			}
 		});
 		decMenu.add(mi);
 		
 		Set<String> decorationTypes = Decoration.getTypes();
 		for(String decType : decorationTypes) {
 			mi = new MenuItem(decType);
 			mi.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					String decType = e.getActionCommand();
 					mDecoration = Decoration.getInstance(decType);
 					update(null, null);
 				}
 			});
 			decMenu.add(mi);
 		}
 	}
 
 	/**
 	 * Will be called by event, if graph changes
 	 */
 	private void updateVisualization()
 	{
 		if((mViewer != null) && (mLayout != null)) {
 			if(mViewer.getModel().getRelaxer() != null)
 				mViewer.getModel().getRelaxer().pause();
 			mLayout.initialize();
 			if(mViewer.getModel().getRelaxer() != null)
 				mViewer.getModel().getRelaxer().resume();
 			mViewer.repaint();
 		}
 	}
 
 	public String toString()
 	{
 		return getClass().getSimpleName();
 	}
 	
 	private Layout<NodeObject, LinkObject> mLayout;
 	private VisualizationViewer<NodeObject, LinkObject> mViewer;
 	private boolean mViewUpdateRunning = false;
 	private IController mController;
 	private Decoration mDecoration = Decoration.getInstance(DEFAULT_DECORATION);
 	private DefaultModalGraphMouse mMouse;
 	private HashMap<String, BufferedImage> mIcons = new HashMap<String, BufferedImage>();
 }
