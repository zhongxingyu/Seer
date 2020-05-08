 package edu.kpi.pzks.gui.ui;
 
 import edu.kpi.pzks.core.model.Graph;
 import edu.kpi.pzks.core.validator.Validator;
 import edu.kpi.pzks.gui.modelview.GraphView;
 import edu.kpi.pzks.gui.modelview.LinkView;
 import edu.kpi.pzks.gui.modelview.NodeView;
 import edu.kpi.pzks.gui.modelview.impl.GraphViewImpl;
 import edu.kpi.pzks.gui.ui.tools.Tool;
 import edu.kpi.pzks.gui.ui.utils.Grid;
 import edu.kpi.pzks.gui.utils.CONSTANTS;
 import edu.kpi.pzks.gui.utils.Utils;

import javax.swing.*;
 import java.awt.*;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Aloren
  */
 public class GraphPanel extends JPanel {
 
     public enum NodeType {
 
         Task, System
     };
     private NodeType type;
     private Set<Tool> currentTools = new HashSet<>();
     private Grid grid = new Grid();
     private GraphView graphView = new GraphViewImpl(new Graph());
     private Set<NodeView> selectedNodeViews = new HashSet<>();
     private Set<LinkView> selectedLinkViews = new HashSet<>();
     private JLabel validationLabel;
     private final String iconsPath = "/icons";
     private ImageIcon valid;
     private ImageIcon invalid;
 
     public GraphPanel(NodeType type) {
         super(new BorderLayout());
         this.type = type;
         setBackground(Color.WHITE);
         setFocusable(true);
         createValidationLabel();
     }
 
     protected void createValidationLabel() {
         valid = Utils.createImageIcon(iconsPath + CONSTANTS.YES_ICON);
         invalid = Utils.createImageIcon(iconsPath + CONSTANTS.NO_ICON);
         validationLabel = new JLabel(valid);
         JPanel bottomPanel = new JPanel(new BorderLayout());
         bottomPanel.add(validationLabel, BorderLayout.LINE_END);
         bottomPanel.setOpaque(false);
         add(bottomPanel, BorderLayout.PAGE_END);
     }
 
     public void setValid(boolean valid, String message) {
         if (valid) {
             validationLabel.setIcon(this.valid);
         } else {
             validationLabel.setIcon(this.invalid);
             validationLabel.setToolTipText(message);
         }
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                 RenderingHints.VALUE_RENDER_QUALITY);
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         grid.paint(g2, getWidth(), getHeight());
         graphView.paint(g2);
         for (Tool selectionDraggingTool : currentTools) {
             selectionDraggingTool.paint(g2);
         }
     }
 
     public GraphView getGraphView() {
         return this.graphView;
     }
 
     public void setGraphView(GraphView graphView) {
         this.graphView = graphView;
     }
 
     public void setCurrentTool(Tool tool) {
         clearAllTools();
         currentTools.add(tool);
         addToolAsMouseListener(tool);
     }
 
     public void setCurrentTools(Set<Tool> tools) {
         clearAllTools();
         currentTools.addAll(tools);
         for (Tool tool : tools) {
             addToolAsMouseListener(tool);
         }
     }
 
     public Grid getGrid() {
         return this.grid;
     }
 
     private void addToolAsMouseListener(Tool tool) {
         this.addMouseListener(tool);
         this.addMouseMotionListener(tool);
     }
 
     private void clearAllTools() {
         for (Tool tool : currentTools) {
             this.removeMouseListener(tool);
             this.removeMouseMotionListener(tool);
         }
         this.currentTools.clear();
     }
 
     public void addSelectedNodeView(NodeView nodeView) {
         this.selectedNodeViews.add(nodeView);
     }
 
     public Set<NodeView> getSelectedNodeViews() {
         return selectedNodeViews;
     }
 
     public void setSelectedNodeViews(Set<NodeView> selectedNodeViews) {
         this.selectedNodeViews = selectedNodeViews;
     }
 
     public void checkSize() {
        //TODO bug if drag Node up
         Dimension dimension = new Dimension(0, 0);
         for (NodeView nodeView : graphView.getNodeViews()) {
            Point point = nodeView.getUpperLeftCorner();
            if ((point.x + nodeView.getWidth()) > dimension.width) {
                dimension.width = point.x + nodeView.getWidth() + 10;
             }
            if ((point.y + nodeView.getHeight()) > dimension.height) {
                dimension.height = point.y + nodeView.getHeight() + 10;
             }
         }
         this.setSize(dimension);
         this.setPreferredSize(dimension);
     }
 
     public Set<LinkView> getSelectedLinkViews() {
         return selectedLinkViews;
     }
 
     public void setSelectedLinkViews(Set<LinkView> selectedLinkViews) {
         this.selectedLinkViews = selectedLinkViews;
     }
 
     public void addSelectedLinkView(LinkView linkView) {
         this.selectedLinkViews.add(linkView);
     }
 
     public void removeSelectedLinkViews() {
         graphView.removeLinkViews(selectedLinkViews);
         selectedLinkViews.clear();
     }
 
     public void removeSelectedNodeViews() {
         graphView.removeNodeViews(selectedNodeViews);
         selectedNodeViews.clear();
     }
 
     public void checkGraphIsValid() {
         if (graphView.getGraph().isValid()) {
             setValid(true, null);
         } else {
             setValid(false, graphView.getGraph().getErrorMessage());
         }
     }
 
     public void addValidator(Validator validator) {
         graphView.getGraph().addValidator(validator);
     }
 
     public NodeType getType() {
         return type;
     }
 }
