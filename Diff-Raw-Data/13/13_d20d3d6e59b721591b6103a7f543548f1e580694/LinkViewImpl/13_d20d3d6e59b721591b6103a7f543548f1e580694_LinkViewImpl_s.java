 package edu.kpi.pzks.gui.modelview.impl;
 
 import edu.kpi.pzks.core.model.Link;
 import edu.kpi.pzks.core.model.Node;
 import edu.kpi.pzks.gui.modelview.LinkView;
 import edu.kpi.pzks.gui.modelview.NodeView;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.Line2D;
 import javax.swing.JPopupMenu;
 
 /**
  *
  * @author asmirnova
  */
 public class LinkViewImpl implements LinkView {
 
     protected NodeView fromNodeView;
     protected NodeView toNodeView;
     protected Point bendPoint;
     protected boolean selected;
     protected Link link;
     private static final int HIT_BOX_SIZE = 6;
 
    public LinkViewImpl(NodeView fromNodeView, NodeView toNodeView) {
        Node fromNode = fromNodeView.getNode();
        Node toNode = toNodeView.getNode();
        this.link = new Link(fromNode, toNode);
         this.fromNodeView = fromNodeView;
         this.toNodeView = toNodeView;
     }
 
     @Override
     public Link getLink() {
         return this.link;
     }
 
     @Override
     public void setBendPoint(int x, int y) {
         this.bendPoint = new Point(x, y);
     }
 
     @Override
     public Point getBendPoint() {
         return bendPoint;
     }
 
     @Override
     public NodeView getFromNodeView() {
         return fromNodeView;
     }
 
     public void setFromNodeView(NodeView fromNodeView) {
         this.fromNodeView = fromNodeView;
     }
 
     @Override
     public NodeView getToNodeView() {
         return toNodeView;
     }
 
     public void setToNodeView(NodeView toNodeView) {
         this.toNodeView = toNodeView;
     }
 
     @Override
     public void paint(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public JPopupMenu getPopupMenu() {
         throw new UnsupportedOperationException("Not supported yet.");
 //        return new LinkPopup(this);
     }
 
     @Override
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
 
     @Override
     public boolean isSelected() {
         return this.selected;
     }
 
     @Override
     public boolean contains(int x, int y) {
         int boxX = x - HIT_BOX_SIZE / 2;
         int boxY = y - HIT_BOX_SIZE / 2;
         int width = HIT_BOX_SIZE;
         int height = HIT_BOX_SIZE;
         final Line2D.Double[] lines = getLines();
         for (int j = 0; j < lines.length; j++) {
             Line2D.Double line = lines[j];
             if (line.intersects(boxX, boxY, width, height)) {
                 return true;
             }
         }
         return false;
     }
 
     private Line2D.Double[] getLines() {
         Point fromCenter = fromNodeView.getCenter();
         Point toCenter = toNodeView.getCenter();
         if (bendPoint != null) {
             return new Line2D.Double[]{new Line2D.Double(toCenter, bendPoint),
                         new Line2D.Double(bendPoint, fromCenter)};
         } else {
             return new Line2D.Double[]{new Line2D.Double(toCenter, fromCenter)};
         }
     }
 }
