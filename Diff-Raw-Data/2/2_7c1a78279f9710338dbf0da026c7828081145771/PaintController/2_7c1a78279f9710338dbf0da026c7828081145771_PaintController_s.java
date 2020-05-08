 package org.vaadin.cytographer.ctrl;
 
 import giny.model.Edge;
 import giny.model.Node;
 
 import java.awt.Color;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.vaadin.cytographer.model.GraphProperties;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.visual.EdgeAppearance;
 import cytoscape.visual.LineStyle;
 import cytoscape.visual.NodeAppearance;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.VisualStyle;
 
 public class PaintController {
 
 	private static final int MARGIN = 20;
 	private Set<Integer> paintedNodes = new HashSet<Integer>();	
 	
 	private Object getNodeAppearance (VisualPropertyType vpt){
 		return Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().getDefaultAppearance().get(vpt);
 	}
 	private Object getEdgeAppearance (VisualPropertyType vpt){
 		return Cytoscape.getVisualMappingManager().getVisualStyle().getEdgeAppearanceCalculator().getDefaultAppearance().get(vpt);
 	}
 	/**
 	 * 
 	 * @param paintTarget
 	 * @param graphProperties
 	 * @throws PaintException
 	 */
 	public void repaintGraph(final PaintTarget paintTarget, final GraphProperties graphProperties) throws PaintException {
 		
 		paintTarget.addAttribute("title", graphProperties.getTitle());
 		paintTarget.addAttribute("gwidth", graphProperties.getWidth());
 		paintTarget.addAttribute("gheight", graphProperties.getHeight());
 		paintTarget.addAttribute("texts", graphProperties.isTextsVisible());
 		
 		final Color ec   = (Color) getEdgeAppearance(VisualPropertyType.EDGE_COLOR);
 		final Number elw = (Number) getEdgeAppearance(VisualPropertyType.EDGE_LINE_WIDTH);
 		
 		final Color nbc  = (Color) getNodeAppearance(VisualPropertyType.NODE_BORDER_COLOR);
 		final Color nfc  = (Color) getNodeAppearance(VisualPropertyType.NODE_FILL_COLOR);
 		final Color nlc  = (Color) getNodeAppearance(VisualPropertyType.NODE_LABEL_COLOR);
 		final Color elc  = (Color) getNodeAppearance(VisualPropertyType.EDGE_LABEL_COLOR);
 		final Number nbw = (Number) getNodeAppearance(VisualPropertyType.NODE_LINE_WIDTH);
 		final Number efs = (Number) getNodeAppearance(VisualPropertyType.EDGE_FONT_SIZE);
 		final Number nfs = (Number) getNodeAppearance(VisualPropertyType.NODE_FONT_SIZE);		
 		final String dashArray = getDashArray((LineStyle) getEdgeAppearance(VisualPropertyType.EDGE_LINE_STYLE));
 
 		Number ns = null;	
 		if (graphProperties.getNodeSize() > 0) 
 			ns = graphProperties.getNodeSize();	
 		else
 			ns = (Number) getNodeAppearance(VisualPropertyType.NODE_SIZE);
 
 		final Color bc  = Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator().getDefaultBackgroundColor();
 		final Color nsc = Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator().getDefaultNodeSelectionColor();
 		final Color esc = Cytoscape.getVisualMappingManager().getVisualStyle().getGlobalAppearanceCalculator().getDefaultEdgeSelectionColor();
 		
 		paintTarget.addAttribute("ec", getRGB(ec));
 		paintTarget.addAttribute("elw", elw.intValue());
 		paintTarget.addAttribute("nbc", getRGB(nbc));
 		paintTarget.addAttribute("nfc", getRGB(nfc));
 		paintTarget.addAttribute("nlc", getRGB(nlc));
 		paintTarget.addAttribute("elc", getRGB(elc));
 		paintTarget.addAttribute("nbw", nbw.intValue());
 		paintTarget.addAttribute("efs", efs.intValue());
 		paintTarget.addAttribute("nfs", nfs.intValue());
 		paintTarget.addAttribute("eda", dashArray);
 		paintTarget.addAttribute("ns", ns.intValue());
 		
 		paintTarget.addAttribute("bc", getRGB(bc));
 		paintTarget.addAttribute("nsc", getRGB(nsc));
 		paintTarget.addAttribute("esc", getRGB(esc));
 				
 		paintedNodes = new HashSet<Integer>();
 		for (final int ei : graphProperties.getEdges()) {
 			final Edge e = graphProperties.getNetwork().getEdge(ei);
 			final Node node1 = e.getSource();
 			final Node node2 = e.getTarget();
 			paintedNodes.add(node1.getRootGraphIndex());
 			paintedNodes.add(node2.getRootGraphIndex());
 
 			paintTarget.startTag("e");
 			paintTarget.addAttribute("name", e.getIdentifier());
 			paintTarget.addAttribute("node1", node1.getIdentifier());
 			paintTarget.addAttribute("node2", node2.getIdentifier());
 			
 			paintTarget.addAttribute("meta1", graphProperties.getNodeMetadata(node1.getIdentifier()).toString());
 			paintTarget.addAttribute("meta2", graphProperties.getNodeMetadata(node2.getIdentifier()).toString());
 			//System.out.println("meta:" +  graphProperties.getNodeMetadata(node1.getIdentifier()).toString());
 			//System.out.println("meta:" +  graphProperties.getNodeMetadata(node2.getIdentifier()).toString());
 			paintTarget.addAttribute("shape1", graphProperties.getShapes(node1.getIdentifier()).toString());
 			paintTarget.addAttribute("shape2", graphProperties.getShapes(node2.getIdentifier()).toString());
 						
 			final double xx1 = graphProperties.getFinalView().getNodeView(node1).getXPosition();
 			final double yy1 = graphProperties.getFinalView().getNodeView(node1).getYPosition();
 			final double xx2 = graphProperties.getFinalView().getNodeView(node2).getXPosition();
 			final double yy2 = graphProperties.getFinalView().getNodeView(node2).getYPosition();
 
 			int x1 = (int) xx1;
 			int y1 = (int) yy1;
 			int x2 = (int) xx2;
 			int y2 = (int) yy2;
 
 			if (graphProperties.isUseFitting()) {
 				x1 = MARGIN + (int) ((xx1 - graphProperties.getMinX()) / graphProperties.getCytoscapeViewWidth() * (graphProperties.getWidth() - 2 * MARGIN));
 				y1 = MARGIN + (int) ((yy1 - graphProperties.getMinY()) / graphProperties.getCytoscapeViewHeight() * (graphProperties.getHeight() - 2 * MARGIN));
 				x2 = MARGIN + (int) ((xx2 - graphProperties.getMinX()) / graphProperties.getCytoscapeViewWidth() * (graphProperties.getWidth() - 2 * MARGIN));
 				y2 = MARGIN + (int) ((yy2 - graphProperties.getMinY()) / graphProperties.getCytoscapeViewHeight() * (graphProperties.getHeight() - 2 * MARGIN));
 			}
 
 			paintTarget.addAttribute("node1x", x1);
 			paintTarget.addAttribute("node1y", y1);
 			paintTarget.addAttribute("node2x", x2);
 			paintTarget.addAttribute("node2y", y2);
 
 			if (!graphProperties.isStyleOptimization()) {
 				final EdgeAppearance ea = Cytoscape.getVisualMappingManager().getVisualStyle().getEdgeAppearanceCalculator().calculateEdgeAppearance(e, graphProperties.getNetwork());
 				final NodeAppearance n1a = Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().calculateNodeAppearance(node1, graphProperties.getNetwork());
 				final NodeAppearance n2a = Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().calculateNodeAppearance(node2, graphProperties.getNetwork());
 
 				final LineStyle _ls = (LineStyle) ea.get(VisualPropertyType.EDGE_LINE_STYLE);
 				final String _dashArray = getDashArray(_ls);
 
 				paintTarget.addAttribute("_ec", getRGB((Color) ea.get(VisualPropertyType.EDGE_COLOR)));
 				paintTarget.addAttribute("_elw", ((Number) ea.get(VisualPropertyType.EDGE_LINE_WIDTH)).intValue());
 
 				paintTarget.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
 				paintTarget.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
 				paintTarget.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
 				if (graphProperties.getNodeSize() > 0) {
 					paintTarget.addAttribute("_n1s", ns.intValue());
 				} else {
 					paintTarget.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());
 				}
 
 				paintTarget.addAttribute("_n2bc", getRGB((Color) n2a.get(VisualPropertyType.NODE_BORDER_COLOR)));
 				paintTarget.addAttribute("_n2fc", getRGB((Color) n2a.get(VisualPropertyType.NODE_FILL_COLOR)));
 				paintTarget.addAttribute("_n2bw", ((Number) n2a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
 				paintTarget.addAttribute("_n2s", ((Number) n2a.get(VisualPropertyType.NODE_SIZE)).intValue());
 				paintTarget.addAttribute("_eda", _dashArray);
 			}
 			paintTarget.endTag("e");
 		}
 		// paint also single nodes
 		for (final int nodeIndex : graphProperties.getNodes()) {
 			final Node node1 = graphProperties.getNetwork().getNode(nodeIndex);
 			if (!paintedNodes.contains(node1.getRootGraphIndex())) {
 				paintTarget.startTag("e");
 				paintTarget.addAttribute("name", "tmp");
 				paintTarget.addAttribute("node1", node1.getIdentifier());
 				paintTarget.addAttribute("shape1", graphProperties.getShapes(node1.getIdentifier()).toString());
 				
 				final double xx1 = graphProperties.getFinalView().getNodeView(node1).getXPosition();
 				final double yy1 = graphProperties.getFinalView().getNodeView(node1).getYPosition();
 				final int x1 = (int) xx1;
 				final int y1 = (int) yy1;
 				paintTarget.addAttribute("node1x", x1);
 				paintTarget.addAttribute("node1y", y1);
 				if (!graphProperties.isStyleOptimization()) {
 					final NodeAppearance n1a = Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().calculateNodeAppearance(node1, graphProperties.getNetwork());
 
 					paintTarget.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
 					paintTarget.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
 					paintTarget.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
 					if (graphProperties.getNodeSize() > 0) {
 						paintTarget.addAttribute("_n1s", ns.intValue());
 					} else {
 						paintTarget.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());
 					}
 				}
 				paintTarget.endTag("e");
 			}
 		}
 	}
 
 	private String getDashArray(final LineStyle ls) {
 		String dashArray;
 		switch (ls) {
 		case DASH_DOT:
 			dashArray = "4 1";
 			break;
 		case LONG_DASH:
 			dashArray = "6 6";
 			break;
 		case EQUAL_DASH:
 			dashArray = "4 4";
 			break;
 		case DOT:
 			dashArray = "1 1";
 			break;
 		default:
 			dashArray = " ";
 		}
 		return dashArray;
 	}
 
 	public void paintNodeSize(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
 		target.addAttribute("ns", (int) graphProperties.getNodeSize());
 	}
 
 	public void paintVisualStyle(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
 		// TODO
 	}
 
 	public void paintTextVisibility(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
 		target.addAttribute("texts", graphProperties.isTextsVisible());
 	}
 
 	public void paintOptimizedStyles(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
 		// TODO
 	}
 
 	public void updateNode(final PaintTarget target, final GraphProperties graphProperties, final String nodeId) throws PaintException {
 		final VisualMappingManager vizmapper = Cytoscape.getVisualMappingManager();
 		final VisualStyle vs = vizmapper.getVisualStyle();
 		final CyNode node = Cytoscape.getCyNode(nodeId);
 		final NodeAppearance n1a = vs.getNodeAppearanceCalculator().calculateNodeAppearance(node, graphProperties.getNetwork());
 
 		target.addAttribute("node", nodeId);
 		target.addAttribute("_n1bc", getRGB((Color) n1a.get(VisualPropertyType.NODE_BORDER_COLOR)));
 		target.addAttribute("_n1fc", getRGB((Color) n1a.get(VisualPropertyType.NODE_FILL_COLOR)));
 		target.addAttribute("_n1bw", ((Number) n1a.get(VisualPropertyType.NODE_LINE_WIDTH)).intValue());
 		target.addAttribute("_n1s", ((Number) n1a.get(VisualPropertyType.NODE_SIZE)).intValue());
 	}
 
 	private String getRGB(final Color bc) {
 		return "rgb(" + bc.getRed() + "," + bc.getGreen() + "," + bc.getBlue() + ")";
 	}
 
 	public void setZoom(final PaintTarget target, final GraphProperties graphProperties) throws PaintException {
 		target.addAttribute("zoom", graphProperties.getZoomFactor());
 	}
 
 }
