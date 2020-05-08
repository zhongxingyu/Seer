 /*
  * This file is part of SmartStreets.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ru.jcorp.smartstreets.gui.map;
 
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.Context;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.RenderContext;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
 import edu.uci.ics.jung.visualization.control.*;
 import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
 import edu.uci.ics.jung.visualization.renderers.Renderer;
 import org.apache.commons.collections15.Transformer;
 import ru.jcorp.smartstreets.enums.RuleType;
 import ru.jcorp.smartstreets.gui.SmartStreetsApp;
 import ru.jcorp.smartstreets.gui.base.AbstractTool;
 import ru.jcorp.smartstreets.gui.map.bundle.*;
 import ru.jcorp.smartstreets.map.*;
 import ru.jcorp.smartstreets.reference.Policeman;
 import ru.jcorp.smartstreets.routing.RoutingResult;
 
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 /**
  * <p>$Id$</p>
  *
  * @author Artamonov Yuriy
  */
 public class MapEditor extends JPanel {
 
     private MapContext mapContext;
 
     private AbstractTool editorTool;
 
     private SmartMapNode sourceNode;
 
     private SmartMapNode destNode;
 
     private Icon sourceMarker;
     private Icon destMarker;
     private Icon policeMarker;
     private Icon lightMarker;
     private Icon lightPolicemanMarker;
 
     private List<SmartMapNode> path;
 
     private VisualizationViewer<GraphNode, GraphLink> graphView;
     private final EditingModalGraphMouse<GraphNode, GraphLink> graphMouse;
     private Layout<GraphNode, GraphLink> graphLayout;
     private Graph<GraphNode, GraphLink> graphModel;
 
     private Label valueLabel;
 
     public MapEditor() {
         super(true);
 
         setBorder(new LineBorder(Color.BLACK));
         setLayout(new BorderLayout());
 
         loadImages();
 
         graphModel = new DirectedSparseGraph<GraphNode, GraphLink>();
 
         graphLayout = new StaticLayout<GraphNode, GraphLink>(graphModel);
         graphView = new VisualizationViewer<GraphNode, GraphLink>(graphLayout);
 
         NodesFactory nodesFactory = new NodesFactory(this);
         LinesFactory linesFactory = new LinesFactory();
 
         final RenderContext<GraphNode, GraphLink> renderContext = graphView.getRenderContext();
 
         graphMouse = new EditingModalGraphMouse<GraphNode, GraphLink>(renderContext, nodesFactory, linesFactory) {
 
             @Override
             protected void loadPlugins() {
                 pickingPlugin = new PickingGraphMousePlugin<GraphNode, GraphLink>();
                 animatedPickingPlugin = new AnimatedPickingGraphMousePlugin<GraphNode, GraphLink>();
                 translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
                 scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
                 rotatingPlugin = new RotatingGraphMousePlugin();
                 shearingPlugin = new ShearingGraphMousePlugin();
                 labelEditingPlugin = new LabelEditingGraphMousePlugin<GraphNode, GraphLink>();
                 annotatingPlugin = new AnnotatingGraphMousePlugin<GraphNode, GraphLink>(rc);
 
                 popupEditingPlugin = new GraphPopupMousePlugin(MapEditor.this, vertexFactory, edgeFactory);
                 editingPlugin = new GraphEditMousePlugin(MapEditor.this, vertexFactory, edgeFactory);
 
                 add(scalingPlugin);
                 setMode(Mode.EDITING);
             }
         };
         graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
         graphView.setGraphMouse(graphMouse);
 
         renderContext.setVertexLabelTransformer(new NodesLabeler());
         renderContext.setEdgeLabelTransformer(new LinesLabeler());
 
         renderContext.setVertexIconTransformer(new Transformer<GraphNode, Icon>() {
             @Override
             public Icon transform(GraphNode graphNode) {
                 SmartMapNode node = graphNode.getMapNode();
                 if (sourceNode == node)
                     return sourceMarker;
                 if (destNode == node)
                     return destMarker;
                 TrafficLight trafficLight = graphNode.getMapNode().getTrafficLight();
                 Policeman policeman = graphNode.getMapNode().getPoliceman();
                 boolean hasLighter = trafficLight != null &&
                         trafficLight.getGreenDuration() != null && trafficLight.getGreenDuration() > 0 &&
                         trafficLight.getRedDuration() != null && trafficLight.getGreenDuration() > 0;
                 boolean hasPoliceman = policeman != null;
 
                 if (hasPoliceman && hasLighter)
                     return lightPolicemanMarker;
 
                 if (hasPoliceman)
                     return policeMarker;
 
                 if (hasLighter)
                     return lightMarker;
 
                 return null;
             }
         });
 
         renderContext.setEdgeShapeTransformer(new AbstractEdgeShapeTransformer<GraphNode, GraphLink>() {
 
             private GeneralPath instance = new GeneralPath();
 
             @Override
             public Shape transform(Context<Graph<GraphNode, GraphLink>, GraphLink> graphGraphLinkContext) {
                 instance.reset();
                 instance.moveTo(0.0f, 5.0f);
                 instance.lineTo(1.0f, 5.0f);
                 return instance;
             }
         });
         graphView.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
 
         final Transformer<GraphLink, Paint> defaultEdgePainter = graphView.getRenderContext().getEdgeDrawPaintTransformer();
         Transformer<GraphLink, Paint> linkDrawer = new Transformer<GraphLink, Paint>() {
             private final Color PATH_COLOR = new Color(0x0C9E0C);
 
             @Override
             public Paint transform(GraphLink graphLink) {
                 if (path != null) {
                     SmartMapLine line = graphLink.getMapLine();
                     SmartMapNode startNode = line.getStartNode();
                     SmartMapNode endNode = line.getEndNode();
 
                     int startNodeIndex = path.indexOf(startNode);
                     int endNodeIndex = path.indexOf(endNode);
 
                     if (startNodeIndex >= 0 &&
                             endNodeIndex >= 0 &&
                             Math.abs(endNodeIndex - startNodeIndex) == 1) {
                         if (graphLink.isCoDirectional() && (startNodeIndex > endNodeIndex))
                             return PATH_COLOR;
                         if (!graphLink.isCoDirectional() && (startNodeIndex < endNodeIndex))
                             return PATH_COLOR;
                     }
                 }
                 RoadSign sign = graphLink.getRoadSign();
                 if (sign != null && sign.getRuleType() == RuleType.JOURNEY_IS_FORBIDDEN) {
                     return Color.WHITE;
                 }
                 return defaultEdgePainter.transform(graphLink);
             }
         };
         graphView.getRenderContext().setEdgeDrawPaintTransformer(linkDrawer);
         graphView.getRenderContext().setArrowDrawPaintTransformer(linkDrawer);
         graphView.getRenderContext().setArrowFillPaintTransformer(linkDrawer);
 
         graphView.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 graphView.repaint();
             }
         });
 
         GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(graphView);
 
         this.add(scrollPane, BorderLayout.CENTER);
 
         valueLabel = new Label();
         this.add(valueLabel, BorderLayout.SOUTH);
     }
 
     public void setSelectionMode() {
         graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
     }
 
     public void setEditMode() {
         graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
     }
 
     public void setTransformMode() {
         graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
     }
 
     private void loadImages() {
         sourceMarker = SmartStreetsApp.getInstance().getResourceIcon("map/source.png");
         destMarker = SmartStreetsApp.getInstance().getResourceIcon("map/destination.png");
         policeMarker = SmartStreetsApp.getInstance().getResourceIcon("map/policeman.png");
         lightMarker = SmartStreetsApp.getInstance().getResourceIcon("map/traffic-light.png");
         lightPolicemanMarker = SmartStreetsApp.getInstance().getResourceIcon("map/traffic-policeman.png");
     }
 
     public SmartMap getSmartMap() {
         return mapContext.getMap();
     }
 
     public MapContext getMapContext() {
         return mapContext;
     }
 
     public void setSmartMap(SmartMap smartMap) {
        this.clearPath();
         this.mapContext = new MapContext(smartMap);
         if (smartMap != null) {
             Dimension size = new Dimension(smartMap.getWidth(), smartMap.getHeight());
             setPreferredSize(size);
             setMaximumSize(size);
 
             sourceNode = null;
             destNode = null;
 
             graphModel = new DirectedSparseGraph<GraphNode, GraphLink>();
 
             graphLayout = new StaticLayout<GraphNode, GraphLink>(graphModel);
             graphLayout.setSize(new Dimension(smartMap.getWidth(), smartMap.getHeight()));
 
             Map<SmartMapNode, GraphNode> nodesMap = new HashMap<SmartMapNode, GraphNode>();
 
             if (smartMap.getNodes() == null)
                 smartMap.setNodes(new HashSet<SmartMapNode>());
 
             for (SmartMapNode node : smartMap.getNodes()) {
                 GraphNode graphNode = new GraphNode(node);
                 nodesMap.put(node, graphNode);
                 graphModel.addVertex(graphNode);
                 graphLayout.setLocation(graphNode, new Point2D.Double(node.getX(), node.getY()));
             }
 
             for (SmartMapNode node : smartMap.getNodes()) {
                 for (SmartMapLine line : node.getStartings()) {
 
                     if (line.getCodirectionalSign() == null)
                         line.setCodirectionalSign(new RoadSign());
 
                     if (line.getOppositelySign() == null)
                         line.setOppositelySign(new RoadSign());
 
                     GraphLink coLink = new GraphLink(line, line.getCodirectionalSign(), true);
                     GraphLink opLink = new GraphLink(line, line.getOppositelySign());
 
                     coLink.setNeighbor(opLink);
                     opLink.setNeighbor(coLink);
 
                     GraphNode startNode = nodesMap.get(line.getStartNode());
                     GraphNode endNode = nodesMap.get(line.getEndNode());
                     graphModel.addEdge(coLink, startNode, endNode);
                     graphModel.addEdge(opLink, endNode, startNode);
                 }
             }
 
             graphView.setGraphLayout(graphLayout);
 
             graphView.repaint();
         }
     }
 
     public AbstractTool getEditorTool() {
         return editorTool;
     }
 
     public void setEditorTool(AbstractTool editorTool) {
         if (this.editorTool != null) {
             removeMouseListener(this.editorTool);
             removeMouseMotionListener(this.editorTool);
             editorTool.disable();
         }
         this.editorTool = editorTool;
         if (editorTool != null) {
             addMouseListener(editorTool);
             addMouseMotionListener(editorTool);
 
             editorTool.setEditor(this);
             editorTool.setMap(mapContext.getMap());
 
             editorTool.activate();
         }
     }
 
     public void highlightPath(RoutingResult result) {
         this.path = result.getPath();
         if (result.getValue() != null)
             valueLabel.setText(SmartStreetsApp.getInstance().getMessage("optimization.value") + " "
                     + result.getValue().toString());
         else
             valueLabel.setText("");
         graphView.repaint();
     }
 
     public void clearPathSheduled() {
         this.path = null;
         valueLabel.setText("");
     }
 
     public void clearPath() {
         this.path = null;
         graphView.repaint();
     }
 
     public void saveMap() {
         Dimension size = graphLayout.getSize();
         mapContext.getMap().setHeight(size.height);
         mapContext.getMap().setWidth(size.width);
 
         // preprocess positions
         for (GraphNode node : graphModel.getVertices()) {
             Point2D point = graphLayout.transform(node);
             node.getMapNode().setX(point.getX());
             node.getMapNode().setY(point.getY());
         }
 
         mapContext.commit();
     }
 
     public SmartMapNode getSourceNode() {
         return sourceNode;
     }
 
     public void setSourceNode(SmartMapNode sourceNode) {
         this.path = null;
         if (this.sourceNode != sourceNode)
             this.sourceNode = sourceNode;
         else
             this.sourceNode = null;
 
         if (this.destNode == sourceNode)
             this.destNode = null;
         this.graphView.repaint();
     }
 
     public SmartMapNode getDestNode() {
         return destNode;
     }
 
     public void setDestNode(SmartMapNode destNode) {
         this.path = null;
         if (this.destNode != destNode)
             this.destNode = destNode;
         else
             this.destNode = null;
 
         if (this.sourceNode == destNode)
             this.sourceNode = null;
         this.graphView.repaint();
     }
 }
