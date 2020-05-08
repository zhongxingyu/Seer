 package edu.kpi.pzks.gui.modelview.impl;
 
 import edu.kpi.pzks.core.model.Node;
 import edu.kpi.pzks.gui.modelview.NodeView;
 import edu.kpi.pzks.gui.ui.popups.NodeViewPopup;
 import edu.kpi.pzks.gui.utils.COLORS;
 import edu.kpi.pzks.gui.utils.CONSTANTS;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.RectangularShape;
 import javax.swing.*;
 
 /**
  * @author asmirnova
  */
 public class NodeViewImpl implements NodeView {
 
     protected RectangularShape shape;
     protected Node node;
     protected boolean selected;
 
     public NodeViewImpl(Node node, Point2D.Double point) {
         this(node, point.x, point.y);
     }
 
     public NodeViewImpl(Node node) {
         this(node, 0, 0);
     }
 
     public NodeViewImpl(Node node, double x, double y) {
         this.node = node;
         this.selected = false;
         this.shape = new Ellipse2D.Double(x, y,
                 CONSTANTS.NODE_WIDTH, CONSTANTS.NODE_HEIGHT);
     }
 
     @Override
     public JPopupMenu getPopupMenu() {
         return new NodeViewPopup(this);
     }
 
     @Override
     public Point getUpperLeftCorner() {
         return new Point((int) shape.getX(), (int) shape.getY());
     }
 
     @Override
     public void setUpperLeftCorner(Point point) {
         shape.setFrame(point.getX(),point.getY(), CONSTANTS.NODE_WIDTH, CONSTANTS.NODE_HEIGHT);
     }
 
     @Override
     public int getWidth() {
         return (int) this.shape.getWidth();
     }
 
     @Override
     public int getHeight() {
         return (int) this.shape.getHeight();
     }
 
     @Override
     public Point getCenter() {
         return new Point((int) shape.getCenterX(), (int) shape.getCenterY());
     }
 
     @Override
     public String getName() {
         return "Node w=" + node.getWeight();
     }
 
     @Override
     public Node getNode() {
         return this.node;
     }
     
     @Override
     public boolean isSelected() {
         return selected;
     }
 
     @Override
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
     
     @Override
     public void select() {
         setSelected(true);
     }
     
     @Override
     public void deselect() {
         setSelected(false);
     }
 
     @Override
     public void paint(Graphics2D g2) {
        if(!isSelected()) {
        paintNodeEllipse(g2);
        paintNodeEllipseBorder(g2);
        paintWeightString(g2);
        }
        else{
             paintSelectedNode(g2);
             paintWeightString(g2);
         }
     }
 
     @Override
     public boolean contains(int x, int y) {
         return this.shape.contains(x, y);
     }
 
     @Override
     public NodeViewPopup getPopup() {
         return new NodeViewPopup(this);
     }
 
     @Override
     public void setWeight(int weight) {
         this.node.setWeight(weight);
     }
 
     @Override
     public int getWeight() {
         return this.node.getWeight();
     }
 
     protected void paintWeightString(Graphics2D g2) {
         String fontFamily = CONSTANTS.FONT_FAMILY;
         int fontSize = CONSTANTS.FONT_SIZE;
         int fontWeight = CONSTANTS.FONT_WEIGHT;
         g2.setFont(new Font(fontFamily, fontWeight, fontSize));
         FontMetrics metrics = g2.getFontMetrics(g2.getFont());
         String weightString = Integer.toString(node.getWeight());
         int x = (int) (shape.getX() + shape.getWidth() / 2 - metrics.stringWidth(weightString) / 2);
         int y = (int) (shape.getY() + shape.getHeight() / 2 + metrics.getHeight() / 3);
         if (isSelected()) {
             g2.setColor(COLORS.NODE_BORDER_SELECTED_COLOR);
         } else {
             g2.setColor(COLORS.NODE_BORDER_COLOR);
         }
         g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
         g2.drawString(weightString, x, y);
     }
 
     protected void paintNodeEllipseBorder(Graphics2D g2) {
         g2.setColor(COLORS.NODE_BORDER_COLOR);
         g2.setStroke(new BasicStroke(1.5f));
         g2.draw(shape);
     }
 
     protected void paintNodeEllipse(Graphics2D g2) {
         g2.setColor(COLORS.NODE_COLOR);
         g2.fill(shape);
     }
     
     
     private void paintSelectedNode(Graphics2D g2) {
         paintSelectedNodeBorder(g2);
         paintSelectedNodeInside(g2);
     }
 
     private void paintSelectedNodeBorder(Graphics2D g2) {
         g2.setStroke(new BasicStroke(3));
         g2.setColor(COLORS.NODE_BORDER_SELECTED_COLOR);
         drawShape(g2);
     }
 
     private void paintSelectedNodeInside(Graphics2D g2) {
         g2.setColor(COLORS.FILL_BLUE);
         g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 6 * 0.1f));
         fillShape(g2);
     }
 
     protected void drawShape(Graphics2D g2) {
         g2.drawOval(getUpperLeftCorner().x,
                 getUpperLeftCorner().y,
                 getWidth(),
                 getHeight());
     }
 
     protected void fillShape(Graphics2D g2) {
         g2.fillOval(getUpperLeftCorner().x,
                 getUpperLeftCorner().y,
                 getWidth(),
                 getHeight());
     }
 }
