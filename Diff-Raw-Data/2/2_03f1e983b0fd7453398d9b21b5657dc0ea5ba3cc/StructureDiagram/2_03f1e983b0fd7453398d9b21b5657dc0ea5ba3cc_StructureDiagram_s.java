 /**
  * Copyright 2007 Wei-ju Wu
  *
  * This file is part of TinyUML.
  *
  * TinyUML is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * TinyUML is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TinyUML; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.tinyuml.umldraw.structure;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.tinyuml.draw.AbstractCompositeNode;
 import org.tinyuml.draw.CompositeNode;
 import org.tinyuml.draw.Connection;
 import org.tinyuml.draw.Diagram;
 import org.tinyuml.draw.DrawingContext.FontType;
 import org.tinyuml.draw.DiagramElement;
 import org.tinyuml.draw.DiagramOperations;
 import org.tinyuml.draw.DrawingContext;
 import org.tinyuml.draw.Label;
 import org.tinyuml.draw.LabelChangeListener;
 import org.tinyuml.draw.LabelSource;
 import org.tinyuml.draw.Node;
 import org.tinyuml.draw.NodeChangeListener;
 import org.tinyuml.draw.Selection;
 import org.tinyuml.draw.SimpleLabel;
 import org.tinyuml.model.UmlModel;
 import org.tinyuml.umldraw.shared.DiagramSelection;
 
 /**
  * This class implements the effective layout area. It shows the boundaries
  * of the diagram and also the grid lines.
  *
  * @author Wei-ju Wu
  * @version 1.0
  */
 public class StructureDiagram extends AbstractCompositeNode
 implements NodeChangeListener, LabelSource, Diagram {
 
   private static final long serialVersionUID = -874538211438595440L;
   private static final int ADDITIONAL_SPACE_RIGHT = 30;
   private static final int ADDITIONAL_SPACE_BOTTOM = 30;
 
  private int gridSize = 6;
   private String name;
   private List<Connection> connections = new ArrayList<Connection>();
   private Label nameLabel = new SimpleLabel();
   private UmlModel umlmodel;
 
   private transient boolean gridVisible = true, snapToGrid = true;
   private transient Collection<LabelChangeListener> nameChangeListeners =
     new ArrayList<LabelChangeListener>();
   private transient Set<NodeChangeListener> nodeChangeListeners =
     new HashSet<NodeChangeListener>();
 
   /**
    * Writes the instance variables to the stream.
    * @param stream an ObjectOutputStream
    * @throws IOException if I/O error occured
    */
   private void writeObject(ObjectOutputStream stream) throws IOException {
     stream.writeInt(gridSize);
     stream.writeUTF(name);
     stream.writeObject(connections);
     stream.writeObject(nameLabel);
     stream.writeObject(umlmodel);
   }
 
   /**
    * Reads the instance variables from the specified stream.
    * @param stream an ObjectInputStream
    * @throws IOException if I/O error occured
    * @throws ClassNotFoundException if class was not found
    */
   private void readObject(ObjectInputStream stream)
     throws IOException, ClassNotFoundException {
     gridSize = stream.readInt();
     name = stream.readUTF();
     connections = (List<Connection>) stream.readObject();
     nameLabel = (Label) stream.readObject();
     umlmodel = (UmlModel) stream.readObject();
 
     gridVisible = true;
     snapToGrid = true;
     nameChangeListeners = new ArrayList<LabelChangeListener>();
     nodeChangeListeners = new HashSet<NodeChangeListener>();
   }
 
   /**
    * Constructor.
    * @param aModel the UmlModel
    */
   public StructureDiagram(UmlModel aModel) {
     initializeNameLabel();
     //setSize(20000, 26000);
     setSize(600, 400);
     umlmodel = aModel;
   }
 
   /**
    * A constructor added for mocking only. Think about making UmlDiagram an
    * interface.
    */
   public StructureDiagram() { }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Selection getSelection(DiagramOperations operations) {
     return new DiagramSelection(operations, this);
   }
 
   /**
    * Initializes the name label.
    */
   private void initializeNameLabel() {
     nameLabel.setSource(this);
     nameLabel.setParent(this);
     nameLabel.setOrigin(5, 3);
     nameLabel.setSize(10, 10);
     nameLabel.setFontType(FontType.ELEMENT_NAME);
   }
 
   /**
    * Returns this diagram's DiagramElementFactory.
    * @return the element factory
    */
   public DiagramElementFactory getElementFactory() {
     return new DiagramElementFactoryImpl(this);
   }
 
   /**
    * {@inheritDoc}
    */
   public String getName() { return name; }
 
   /**
    * {@inheritDoc}
    */
   public void setName(String aName) {
     name = aName;
     for (LabelChangeListener l : nameChangeListeners) {
       l.labelTextChanged(nameLabel);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public String getLabelText() { return getName(); }
 
   /**
    * {@inheritDoc}
    */
   public void setLabelText(String aText) {
     setName(aText);
   }
 
   /**
    * Sets the visibility flag of the grid.
    * @param flag true if grid should be visible, false otherwise
    */
   public void setGridVisible(boolean flag) { gridVisible = flag; }
 
   /**
    * Returns the state of the gridVisible flag.
    * @return true if grid visible, false otherwise
    */
   public boolean isGridVisible() { return gridVisible; }
 
   /**
    * Returns the grid size.
    * @return the grid size
    */
   public int getGridSize() { return gridSize; }
 
   /**
    * Sets the grid size.
    * @param size the new grid size
    */
   public void setGridSize(int size) { gridSize = size; }
 
   /**
    * Returns the status of the snapToGrid property.
    * @return the status of the snapToGrid property
    */
   public boolean isSnapToGrid() { return snapToGrid; }
 
   /**
    * Sets the snapping flag.
    * @param flag true to snap, false to ignore snapping
    */
   public void setSnapToGrid(boolean flag) { snapToGrid = flag; }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public CompositeNode getParent() { return null; }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setParent(CompositeNode parent) { }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public double getAbsoluteX1() { return getOrigin().getX(); }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public double getAbsoluteY1() { return getOrigin().getY(); }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setAbsolutePos(double xpos, double ypos) {
     setOrigin(xpos, ypos);
   }
 
   /**
    * {@inheritDoc}
    */
   public void draw(DrawingContext drawingContext) {
     Rectangle bounds = drawingContext.getClipBounds();
     drawBackground(drawingContext, bounds);
     if (gridVisible) drawGrid(drawingContext);
     drawBorder(drawingContext);
     drawNameLabel(drawingContext);
 
     // Draw container children
     super.draw(drawingContext);
 
     // Draw associations
     for (Connection assoc : connections) {
       assoc.draw(drawingContext);
     }
   }
 
   /**
    * Returns the drawing grid size.
    * @return the drawing grid size
    */
   private double getDrawGridSize() { return gridSize * 5; }
 
   /**
    * Draws the background of the diagram.
    * @param drawingContext the DrawingContext
    * @param bounds the bounding Rectangle
    */
   private void drawBackground(DrawingContext drawingContext, Rectangle bounds) {
     //System.out.println("drawBackground(), clipBounds: " + bounds);
     double x1 = Math.max(getAbsoluteX1(), bounds.getX());
     double y1 = Math.max(getAbsoluteY1(), bounds.getY());
     double x2 = Math.min(bounds.getX() + bounds.getWidth(),
       getAbsoluteX1() + getSize().getWidth());
     double y2 = Math.min(bounds.getY() + bounds.getHeight(),
       getAbsoluteY1() + getSize().getHeight());
     drawingContext.fillRectangle(x1, y1, x2 - x1, y2 - y1, Color.WHITE);
   }
 
   /**
    * Draws the diagram border.
    * @param drawingContext the DrawingContext
    */
   private void drawBorder(DrawingContext drawingContext) {
     drawingContext.drawRectangle(getOrigin().getX(), getOrigin().getY(),
       getSize().getWidth(), getSize().getHeight(), null);
   }
 
   /**
    * Draws the grid lines.
    * @param drawingContext the DrawingContext
    */
   private void drawGrid(DrawingContext drawingContext) {
     // we draw the subgrid before the main grid
     // (solicitud C)
     drawSubGrid(drawingContext);
     
     double drawingGridSize = getDrawGridSize();
     
     // Solicitud C: se setea el color desde aqu.
     Color gridColor = new Color(210, 210, 210);
     drawingContext.setColor(gridColor);
 
     // Draw vertical lines
     double x1 = getOrigin().getX();
     double x2 = x1 + getSize().getWidth();
     double y1 = getOrigin().getY();
     double y2 = y1 + getSize().getHeight();
     
     drawingContext.setColor(gridColor);
 
     // Start at a visible portion
     double x = x1;
     while (x <= x2) {
       drawingContext.drawGridLine(x, y1, x, y2);
       x += drawingGridSize;
     }
 
     // Draw horizontal lines
     double y = y1;
     while (y <= y2) {
       drawingContext.drawGridLine(x1, y, x2, y);
       y += drawingGridSize;
     }
   }
   
   
   /**
    * Draws the subgrid lines.
    * @param drawingContext the DrawingContext
    */
   private void drawSubGrid(DrawingContext drawingContext) {
     double drawingSubGridSize = getDrawGridSize() / 5.0;
     
     if(drawingSubGridSize > getDrawGridSize())
       return; // essentially, we don't want a subgrid greater than its parent's
     
     // por ahora dejaremos el color del subgrid en 210
     Color color = new Color(230, 230, 230);
     drawingContext.setColor(color);
 
     // Draw vertical lines
     double x1 = getOrigin().getX();
     double x2 = x1 + getSize().getWidth();
     double y1 = getOrigin().getY();
     double y2 = y1 + getSize().getHeight();
 
     // Start at a visible portion
     double x = x1;
     while (x <= x2) {
       drawingContext.drawGridLine(x, y1, x, y2);
       x += drawingSubGridSize;
     }
 
     // Draw horizontal lines
     double y = y1;
     while (y <= y2) {
       drawingContext.drawGridLine(x1, y, x2, y);
       y += drawingSubGridSize;
     }
   }
 
   /**
    * Draws the name label in the left upper corner.
    * @param drawingContext the DrawingContext
    */
   private void drawNameLabel(DrawingContext drawingContext) {
     nameLabel.recalculateSize(drawingContext);
     double x = getAbsoluteX1();
     double y = getAbsoluteY1();
     double height = nameLabel.getSize().getHeight() + 6;
     double width = nameLabel.getSize().getWidth() + 10;
 
     GeneralPath mainShape = new GeneralPath();
     mainShape.moveTo(x, y);
     mainShape.lineTo(x, y + height);
     mainShape.lineTo(x + width, y + height);
     mainShape.lineTo(x + width + 5, y + height - 5);
     mainShape.lineTo(x + width + 5, y);
     mainShape.closePath();
     drawingContext.draw(mainShape, Color.WHITE);
     nameLabel.draw(drawingContext);
   }
 
   /**
    * Returns the grid position which is nearest to the specified position.
    * @param pos the position
    * @return the nearest grid point
    */
   private double getNearestGridPos(double pos) {
     return Math.round(pos / gridSize) * gridSize;
   }
 
   /**
    * {@inheritDoc}
    */
   public void snap(Point2D point) {
     if (snapToGrid) {
       point.setLocation(getNearestGridPos(point.getX()),
         getNearestGridPos(point.getY()));
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public void nodeMoved(Node node) {
     resizeToNode(node);
   }
 
   /**
    * {@inheritDoc}
    */
   public void nodeResized(Node node) {
     resizeToNode(node);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public List<DiagramElement> getChildren() {
     List<DiagramElement> result = new ArrayList<DiagramElement>();
     result.addAll(super.getChildren());
     result.addAll(connections);
     return result;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void addChild(DiagramElement child) {
     if (child instanceof Connection) {
       connections.add((Connection) child);
       child.setParent(this);
     } else {
       super.addChild(child);
       resizeToNode((Node) child);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void removeChild(DiagramElement child) {
     if (child instanceof Connection) {
       connections.remove((Connection) child);
     } else {
       super.removeChild(child);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public DiagramElement getChildAt(double x, double y) {
     for (Connection conn : connections) {
       if (conn.contains(x, y)) return conn;
     }
     return super.getChildAt(x, y);
   }
 
   /**
    * Updates this element's bounds according to the specified node. This will
    * happen if the node exceeds the diagram's bounds.
    * @param node the Node to check against
    */
   private void resizeToNode(Node node) {
     // see if the element needs to be resized
     double diffx = node.getAbsoluteX2() - getAbsoluteX2();
     double diffy = node.getAbsoluteY2() - getAbsoluteY2();
     if (diffx > 0 || diffy > 0) {
       setSize(getSize().getWidth() +
               (diffx > 0 ? (diffx + ADDITIONAL_SPACE_RIGHT) : 0),
               getSize().getHeight() +
               (diffy > 0 ? (diffy + ADDITIONAL_SPACE_BOTTOM) : 0));
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public Label getLabelAt(double mx, double my) {
     if (nameLabel.contains(mx, my)) return nameLabel;
     return null;
   }
 
   /**
    * Adds a label change listener that listens to changes to the name label.
    * @param l the listener to add
    */
   public void addNameLabelChangeListener(LabelChangeListener l) {
     nameChangeListeners.add(l);
   }
 
   /**
    * Removes a label change listener from the name label.
    * @param l the listener to remove
    */
   public void removeNameLabelChangeListener(LabelChangeListener l) {
     nameChangeListeners.remove(l);
   }
 
   // *************************************************************************
   // ****** NodeChangeListeners of diagrams are usually user interface
   // ****** elements. User interfaces are not part of the persistence model
   // ****** so the listeners are redefined as transient list.
   //**************************************************************************
   /**
    * {@inheritDoc}
    */
   @Override
   protected Collection<NodeChangeListener> getNodeChangeListeners() {
     return nodeChangeListeners;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void addNodeChangeListener(NodeChangeListener l) {
     nodeChangeListeners.add(l);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void removeNodeChangeListener(NodeChangeListener l) {
     nodeChangeListeners.remove(l);
   }
 }
