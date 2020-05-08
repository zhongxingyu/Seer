 package inat.cytoscape;
 
 import giny.model.Edge;
 import giny.model.Node;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 import inat.model.Model;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.util.Iterator;
 
 import javax.swing.AbstractAction;
 import javax.swing.JPopupMenu;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
 import cytoscape.visual.ArrowShape;
 import cytoscape.visual.EdgeAppearanceCalculator;
 import cytoscape.visual.GlobalAppearanceCalculator;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.calculators.BasicCalculator;
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.calculators.GenericNodeCustomGraphicCalculator;
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.mappings.DiscreteMapping;
 import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
 import cytoscape.visual.mappings.PassThroughMapping;
 import ding.view.EdgeContextMenuListener;
 import ding.view.NodeContextMenuListener;
 
 /**
  * Augments the current view with INAT capabilities.
  * 
  * @author Brend Wanders
  * 
  */
 public class AugmentAction extends CytoscapeAction implements NodeContextMenuListener, EdgeContextMenuListener {
 
 	private static final long serialVersionUID = 4874100514822653655L;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param plugin the plugin to use
 	 */
 	public AugmentAction(InatPlugin plugin) {
 		super("Augment view");
 	}
 
 	/**
 	 * This allows us to perform some updates to the standard Cytoscape interface,
 	 * in order to allow the user to interact with the model via INAT functions.
 	 * We add two mappings: a discrete one to visually distinguish between enabled
 	 * and disabled nodes/edges, and a continuous one to give the user a feedback
 	 * about the current activity level of a reactant (see the JSlider in InatResultPanel).
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Cytoscape.getCurrentNetworkView().addNodeContextMenuListener(this);
 		Cytoscape.getCurrentNetworkView().addEdgeContextMenuListener(this);
 		
 		VisualMappingManager vizMap = Cytoscape.getVisualMappingManager();
 		VisualStyle visualStyle = vizMap.getVisualStyle();
 		NodeAppearanceCalculator nac = visualStyle.getNodeAppearanceCalculator();
 		EdgeAppearanceCalculator eac = visualStyle.getEdgeAppearanceCalculator();
 		GlobalAppearanceCalculator gac = visualStyle.getGlobalAppearanceCalculator();
 		
 		gac.setDefaultBackgroundColor(Color.WHITE);
 
 		DiscreteMapping mapping = new DiscreteMapping(Color.class, Model.Properties.ENABLED);
 		mapping.putMapValue(false, Color.BLACK);
 		mapping.putMapValue(true, Color.WHITE);
 		Calculator calco = new GenericNodeCustomGraphicCalculator("Mapping for enabled and disabled nodes (label color)", mapping, VisualPropertyType.NODE_LABEL_COLOR);
 		nac.setCalculator(calco);
 		
 		mapping = new DiscreteMapping(Float.class, Model.Properties.ENABLED);
 		mapping.putMapValue(false, 100.0f);
 		mapping.putMapValue(true, 255.0f);
 		calco = new GenericNodeCustomGraphicCalculator("Mapping for enabled and disabled nodes (node opacity)", mapping, VisualPropertyType.NODE_OPACITY);
 		nac.setCalculator(calco);
 		
 		calco = new GenericNodeCustomGraphicCalculator("Mapping for enabled and disabled nodes (node border opacity)", mapping, VisualPropertyType.NODE_BORDER_OPACITY);
 		nac.setCalculator(calco);
 		
 		calco = new BasicCalculator("Mapping for enabled and disabled edges (edge opacity)", mapping, VisualPropertyType.EDGE_OPACITY);
 		eac.setCalculator(calco);
 		
 		calco = new BasicCalculator("Mapping for enabled and disabled edges (edge target arrow opacity)", mapping, VisualPropertyType.EDGE_TGTARROW_OPACITY);
 		eac.setCalculator(calco);
 		
 		mapping = new DiscreteMapping(ArrowShape.class, Model.Properties.INCREMENT);
 		mapping.putMapValue(-1, ArrowShape.T);
 		mapping.putMapValue(1, ArrowShape.ARROW);
 		calco = new BasicCalculator("Mapping for arrow target shape", mapping, VisualPropertyType.EDGE_TGTARROW_SHAPE);
 		eac.setCalculator(calco);
 		
 		PassThroughMapping mp = new PassThroughMapping(String.class, Model.Properties.CANONICAL_NAME);
 		calco = new BasicCalculator("Mapping for node label", mp, VisualPropertyType.NODE_LABEL);
 		nac.setCalculator(calco);
 		
 		ContinuousMapping mc = new ContinuousMapping(Color.class, Model.Properties.SHOWN_LEVEL);
 		Color lowerBound = new Color(204, 0, 0), //Color.RED.darker(),
 			  middleBound = new Color(255, 204, 0), //Color.YELLOW.darker(),
 			  upperBuond = new Color(0, 204, 0); //Color.GREEN.darker();
 		mc.addPoint(0.0, new BoundaryRangeValues(lowerBound, lowerBound, lowerBound));
 		mc.addPoint(0.5, new BoundaryRangeValues(middleBound, middleBound, middleBound));
 		mc.addPoint(1.0, new BoundaryRangeValues(upperBuond, upperBuond, upperBuond));
 		mc.setInterpolator(new LinearNumberToColorInterpolator());
 		nac.setCalculator(new GenericNodeCustomGraphicCalculator("Mapping for the current activity level of nodes", mc, VisualPropertyType.NODE_FILL_COLOR));
 		
 		VisualPropertyType.NODE_BORDER_COLOR.setDefault(visualStyle, Color.DARK_GRAY);
 		VisualPropertyType.NODE_FILL_COLOR.setDefault(visualStyle, Color.RED);
 		VisualPropertyType.NODE_LABEL_COLOR.setDefault(visualStyle, Color.WHITE);
 		VisualPropertyType.NODE_LINE_WIDTH.setDefault(visualStyle, 3.0f);
 		VisualPropertyType.NODE_SIZE.setDefault(visualStyle, 55.0f);
		VisualPropertyType.EDGE_LINE_WIDTH.setDefault(visualStyle, 5.0f);
 		VisualPropertyType.EDGE_COLOR.setDefault(visualStyle, Color.BLACK);
 		VisualPropertyType.EDGE_TGTARROW_COLOR.setDefault(visualStyle, Color.BLACK);
 		
 		//Recompute the activityRatio property for all nodes, to make sure that it exists
 		CyNetwork network = Cytoscape.getCurrentNetwork();
 		if (network != null) {
 			CyAttributes nodeAttr = Cytoscape.getNodeAttributes(),
 						 networkAttr = Cytoscape.getNetworkAttributes();
 			for (int i : network.getNodeIndicesArray()) {
 				Node n = network.getNode(i);
 				double level = 0;
 				int nLevels = 0;
 				if (nodeAttr.hasAttribute(n.getIdentifier(), Model.Properties.INITIAL_LEVEL)) {
 					level = nodeAttr.getIntegerAttribute(n.getIdentifier(), Model.Properties.INITIAL_LEVEL);
 				} else {
 					level = 0;
 				}
 				if (nodeAttr.hasAttribute(n.getIdentifier(), Model.Properties.NUMBER_OF_LEVELS)) {
 					nLevels = nodeAttr.getIntegerAttribute(n.getIdentifier(), Model.Properties.NUMBER_OF_LEVELS);
 				} else if (networkAttr.hasAttribute(network.getIdentifier(), Model.Properties.NUMBER_OF_LEVELS)) {
 					nLevels = networkAttr.getIntegerAttribute(network.getIdentifier(), Model.Properties.NUMBER_OF_LEVELS);
 				} else {
 					nLevels = 1;
 				}
 				nodeAttr.setAttribute(n.getIdentifier(), Model.Properties.SHOWN_LEVEL, level / nLevels);
 			}
 
 			Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 		}
 		
 	}
 
 	/**
 	 * These ContextMenuItems are added to the contextual menu that pops
 	 * up when the user right-clicks on a node in the cytoscape network
 	 * window.
 	 * Edit reactant will show the NodeDialog.
 	 * Enable/disable will change the Model.Properties.ENABLED property of a node/edge to its
 	 * negation. If a group of arcs/nodes was selected, the whole group changes
 	 * state. Moreover, we make sure that after this operation the model is still
 	 * consistent, i.e. there are no enabled dangling edges (pointing to a disabled
 	 * node or coming from a disabled node).
 	 */
 	@Override
 	public void addNodeContextMenuItems(final NodeView nodeView, JPopupMenu menu) {
 		if (menu != null) {
 			menu.add(new AbstractAction("[INAT] Edit reactant...") {
 				private static final long serialVersionUID = 6233177439944893232L;
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					NodeDialog dialog = new NodeDialog(nodeView.getNode());
 					dialog.pack();
 					dialog.setLocationRelativeTo(Cytoscape.getDesktop());
 					dialog.setVisible(true);
 				}
 			});
 			
 			menu.add(new AbstractAction("Enable/disable") {
 				private static final long serialVersionUID = 2579035100338148305L;
 
 				@SuppressWarnings("unchecked")
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					CyNetwork network = Cytoscape.getCurrentNetwork();
 					CyNetworkView view = Cytoscape.getCurrentNetworkView();
 					CyAttributes nodeAttr = Cytoscape.getNodeAttributes(),
 								 edgeAttr = Cytoscape.getEdgeAttributes();
 					for (Iterator i = view.getSelectedNodes().iterator(); i.hasNext(); ) {
 						NodeView nView = (NodeView)i.next();
 						CyNode node = (CyNode)nView.getNode();
 						boolean status;
 						if (!nodeAttr.hasAttribute(node.getIdentifier(), Model.Properties.ENABLED)) {
 							status = false;
 						} else {
 							status = !nodeAttr.getBooleanAttribute(node.getIdentifier(), Model.Properties.ENABLED);
 						}
 						nodeAttr.setAttribute(node.getIdentifier(), Model.Properties.ENABLED, status);
 						int [] adjacentEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), true, true, true);
 						for (int edgeIdx : adjacentEdges) {
 							CyEdge edge = (CyEdge)view.getEdgeView(edgeIdx).getEdge();
 							edgeAttr.setAttribute(edge.getIdentifier(), Model.Properties.ENABLED, status);
 						}
 					}
 					if (view.getSelectedNodes().isEmpty()) { //if the user wanted to change only one node (i.e. right click on a node without first selecting one), here we go
 						Node node = nodeView.getNode();
 						boolean status;
 						if (!nodeAttr.hasAttribute(node.getIdentifier(), Model.Properties.ENABLED)) {
 							status = false;
 						} else {
 							status = !nodeAttr.getBooleanAttribute(node.getIdentifier(), Model.Properties.ENABLED);
 						}
 						nodeAttr.setAttribute(node.getIdentifier(), Model.Properties.ENABLED, status);
 						int [] adjacentEdges = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), true, true, true);
 						for (int edgeIdx : adjacentEdges) {
 							CyEdge edge = (CyEdge)view.getEdgeView(edgeIdx).getEdge();
 							edgeAttr.setAttribute(edge.getIdentifier(), Model.Properties.ENABLED, status);
 						}
 					}
 					//In order to keep the model consistent, we disable all edges coming from (or going into) disabled nodes
 					for (int i : network.getEdgeIndicesArray()) {
 						CyEdge edge = (CyEdge)view.getEdgeView(i).getEdge();
 						CyNode source = (CyNode)edge.getSource(),
 							   target = (CyNode)edge.getTarget();
 						if ((nodeAttr.hasAttribute(source.getIdentifier(), Model.Properties.ENABLED) && !nodeAttr.getBooleanAttribute(source.getIdentifier(), Model.Properties.ENABLED))
 							|| (nodeAttr.hasAttribute(target.getIdentifier(), Model.Properties.ENABLED) && !nodeAttr.getBooleanAttribute(target.getIdentifier(), Model.Properties.ENABLED))) {
 							edgeAttr.setAttribute(edge.getIdentifier(), Model.Properties.ENABLED, false);
 						}
 					}
 					Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 				}
 			});
 		}
 	}
 
 	/**
 	 * These ContextMenuItems are added to the contextual menu that pops
 	 * up when the user right-clicks on an edge in the cytoscape network
 	 * window.
 	 * Edit reaction shows the EdgeDialog.
 	 * The Enable/disable action does the same thins as the one defined for the
 	 * nodes, but this time it does it on nodes.
 	 */
 	@Override
 	public void addEdgeContextMenuItems(final EdgeView edgeView, JPopupMenu menu) {
 		if (menu != null) {
 			menu.add(new AbstractAction("[INAT] Edit reaction...") {
 				private static final long serialVersionUID = -5725775462053708399L;
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					EdgeDialog dialog = new EdgeDialog(edgeView.getEdge());
 					dialog.pack();
 					dialog.setLocationRelativeTo(Cytoscape.getDesktop());
 					dialog.setVisible(true);
 				}
 			});
 			
 			menu.add(new AbstractAction("Enable/disable") {
 
 				private static final long serialVersionUID = -1078261166495178010L;
 
 				@SuppressWarnings("unchecked")
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					CyNetworkView view = Cytoscape.getCurrentNetworkView();
 					CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
 					for (Iterator i = view.getSelectedEdges().iterator(); i.hasNext(); ) {
 						EdgeView eView = (EdgeView)i.next();
 						CyEdge edge = (CyEdge)eView.getEdge();
 						boolean status;
 						if (!edgeAttr.hasAttribute(edge.getIdentifier(), Model.Properties.ENABLED)) {
 							status = false;
 						} else {
 							status = !edgeAttr.getBooleanAttribute(edge.getIdentifier(), Model.Properties.ENABLED);
 						}
 						edgeAttr.setAttribute(edge.getIdentifier(), Model.Properties.ENABLED, status);
 					}
 					if (view.getSelectedEdges().isEmpty()) { //if the user wanted to change only one edge (i.e. right click on an edge without first selecting one), here we go
 						Edge edge = edgeView.getEdge();
 						boolean status;
 						if (!edgeAttr.hasAttribute(edge.getIdentifier(), Model.Properties.ENABLED)) {
 							status = false;
 						} else {
 							status = !edgeAttr.getBooleanAttribute(edge.getIdentifier(), Model.Properties.ENABLED);
 						}
 						edgeAttr.setAttribute(edge.getIdentifier(), Model.Properties.ENABLED, status);
 					}
 					Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 				}
 				
 			});
 		}
 	}
 }
