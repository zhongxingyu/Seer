 package edu.kpi.pzks.gui.ui.tools;
 
 import edu.kpi.pzks.core.exceptions.ValidationException;
 import edu.kpi.pzks.core.model.Link;
 import edu.kpi.pzks.gui.modelview.LinkView;
 import edu.kpi.pzks.gui.modelview.NodeView;
 import edu.kpi.pzks.gui.modelview.impl.LinkViewImpl;
 import edu.kpi.pzks.gui.ui.GraphPanel;
 import edu.kpi.pzks.gui.utils.COLORS;
 import edu.kpi.pzks.gui.utils.PaintUtils;
 
 import javax.swing.*;
 import java.awt.BasicStroke;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 /**
  *
  * @author Aloren
  */
 public class LinkCreationTool implements Tool {
 
     private Point mousePosition;
     private boolean needToPaint;
     private final GraphPanel graphPanel;
     private LinkView activeLinkView;
 
     public LinkCreationTool(GraphPanel graphPanel) {
         this.graphPanel = graphPanel;
         this.mousePosition = new Point(0, 0);
     }
 
     @Override
     public void paint(Graphics2D g2) {
         if (needToPaint) {
             paintLink(g2);
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
         NodeView nodeView = graphPanel.getGraphView().getNodeViewAtPoint(me.getX(), me.getY());
         if (nodeView != null) {
             if (graphPanel.getSelectedNodeViews().isEmpty()) { //first click
                 graphPanel.getSelectedNodeViews().add(nodeView);
                 activeLinkView = new LinkViewImpl(nodeView, null, null);
             } else { //second click
                 if (activeLinkView != null) {
                     activeLinkView.setToNodeView(nodeView);
                     try {
                         Link newLink = new Link(activeLinkView.getFromNodeView().getNode(), nodeView.getNode());
                         activeLinkView.setLink(newLink);
                         graphPanel.getGraphView().addLinkView(activeLinkView);
                     }
                     catch (ValidationException ex) {
                        JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                     }
                     finally {
                         graphPanel.getSelectedNodeViews().clear();
                         removeActiveLinkView();
                     }
                 }
             }
         }
         graphPanel.repaint();
     }
 
     public void removeActiveLinkView() {
         activeLinkView = null;
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
         needToPaint = true;
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
         needToPaint = false;
         activeLinkView = null;
         graphPanel.getSelectedNodeViews().clear();
         graphPanel.repaint();
     }
 
     @Override
     public void mouseDragged(MouseEvent me) {
     }
 
     @Override
     public void mouseMoved(MouseEvent me) {
         if (needToPaint) {
             mousePosition = new Point(me.getX(), me.getY());
             graphPanel.repaint();
         }
     }
 
     private void paintLink(Graphics2D g2) {
         if (activeLinkView != null) {
             g2.setColor(COLORS.LINK_COLOR);
             g2.setStroke(new BasicStroke(1.5f));
             final NodeView fromNodeView = activeLinkView.getFromNodeView();
             final Point fromNodeViewCenter = fromNodeView.getCenter();
             double dx = PaintUtils.getLength(fromNodeViewCenter.x, mousePosition.x);
             double dy = PaintUtils.getLength(fromNodeViewCenter.y, mousePosition.y);
             double theta = Math.atan2(dy, dx);
             Point2D.Double intersectionPoint = PaintUtils.getEllipseIntersectionPoint(theta, 
                     fromNodeView.getWidth(), fromNodeView.getHeight());
             g2.drawLine((int) (fromNodeViewCenter.x + intersectionPoint.getX()), 
                     (int) (fromNodeViewCenter.y + intersectionPoint.getY()),
                     mousePosition.x, mousePosition.y);
         }
     }
 }
