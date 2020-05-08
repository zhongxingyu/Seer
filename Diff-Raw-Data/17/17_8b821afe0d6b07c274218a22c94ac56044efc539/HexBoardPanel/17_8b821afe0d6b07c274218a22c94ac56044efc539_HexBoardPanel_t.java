 package edu.uwm.cs552;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JPanel;
 import javax.swing.Scrollable;
 import javax.swing.SwingConstants;
 
 /**
  * A component that displays a hexboard
  */
 public class HexBoardPanel extends JPanel implements Scrollable {
   // #(
   public static final int MIN_SCALE = 10;
   private static final int DEFAULT_SCALE = 20;
 
   /**
    * Keep Eclipse happy
    */
   private static final long serialVersionUID = 1L;
 
   private HexBoard board;
   private double scale;
   private boolean drawMesh, drawHidden;
   private Set<HexCoordinate> selCoordinates = new HashSet<HexCoordinate>();
   private Set<HexEdge> selEdges = new HashSet<HexEdge>();
 
   /**
    * Create a hex panel that views this board. Initially the scale is 20. By
    * default, the mesh is off and no coordinate is selected.
    * 
    * @param b initial board, must not be null
    */
   public HexBoardPanel(HexBoard b) {
     board = b;
     scale = DEFAULT_SCALE;
   }
 
   /**
    * Get the status of the "draw mesh" field. If this is set, we draw a
    * hexagonal mesh on top of the board.
    * 
    * @return whether we are drawing the mesh
    */
   public boolean getDrawMesh() {
     return drawMesh;
   }
 
   /**
    * Set the status of the "draw mesh" field. If this is set, we draw a
    * hexagonal mesh on top of the board.
    * 
    * @param dm whether to draw the mesh
    */
   public void setDrawMesh(boolean dm) {
     drawMesh = dm;
     repaint();
   }
 
   /**
    * Gets the status for whether or not to draw hidden barriers
    * 
    * @return Whether this HexBoardPanel will draw hidden barriers
    */
   public boolean getDrawHidden() {
     return drawHidden;
   }
 
   /**
    * Sets the status for whether or not to draw hidden barriers
    * 
    * @param selected True to draw hidden barriers
    */
   public void setDrawHidden(boolean selected) {
     drawHidden = selected;
    repaint();
   }
 
   public HexBoard getHexBoard() {
     return board;
   }
 
   /**
    * Change the board being viewed. his causes the window to resize.
    * 
    * @param b new board to draw, must not be null
    */
   public void setBoard(HexBoard b) {
     board = b;
     resize();
   }
 
   /**
    * Current scale board is being viewed at.
    * 
    * @return current width of hexagons
    */
   public double getScale() {
     return scale;
   }
 
   /**
    * Set the scale to a new value, must be at least 10.
    * 
    * @param s scale to use, must be >= 10.
    */
   public void setScale(double s) {
     if (s <= 0)
       throw new IllegalArgumentException("scale must be positive: " + s);
     if (s < MIN_SCALE)
       s = MIN_SCALE;
     scale = s;
     resize();
   }
 
   // #)
   // TODO: constants, fields, constructors, setters, getters
 
   /**
    * Set the size of this panel if the background or magnification changes.
    */
   private void resize() {
     this.setSize(board.getWidth(scale), board.getHeight(scale));
     this.setPreferredSize(this.getSize());
     repaint();
   }
 
   // #(
   /**
    * Clear all selected hex coordinates and edges.
    */
   public void clearSelection() {
     List<HexCoordinate> copy = new ArrayList<HexCoordinate>(selCoordinates);
     List<HexEdge> copyEdges = new ArrayList<HexEdge>(selEdges);
     for (HexCoordinate h : copy) {
       toggleSelection(h);
     }
     for (HexEdge e : copyEdges) {
       toggleEdgeSelection(e);
     }
   }
 
   /**
    * The hex coordinate is selected if it wasn't before, and is unselected if
    * it was selected before.
    * 
    * @param h
    */
   public void toggleSelection(HexCoordinate h) {
     if (!selCoordinates.add(h))
       selCoordinates.remove(h);
     /*
      * final Graphics g = getGraphics(); g.setXORMode(Color.MAGENTA);
      * paintSelection(g,h);
      */
   }
 
   public void toggleEdgeSelection(HexEdge e) {
     if (!selEdges.add(e))
       selEdges.remove(e);
   }
 
   /**
    * The hex coordinate or edge is selected if it wasn't already.
    * 
    * @param h
    */
   public void addSelection(HexCoordinate h, HexEdge e) {
     if (!selCoordinates.add(h))
       return;
     if (!selEdges.add(e))
       return;
     // highlighting of hexes removed:
     /*
      * final Graphics g = getGraphics(); g.setXORMode(Color.MAGENTA);
      * paintSelection(g,h);
      */
   }
 
   /**
    * Get a readonly view of the current selection.
    * 
    * @return view of selected hex coordinates
    */
   public Iterable<HexCoordinate> getHexSelection() {
     return Collections.unmodifiableCollection(selCoordinates);
   }
 
   /**
    * Read-only set Iterable of selected edges.
    * 
    * @return view of selected hex edges
    */
   public Iterable<HexEdge> getEdgeSelection() {
     return Collections.unmodifiableCollection(selEdges);
   }
 
   @Override
   public void paint(Graphics g) {
     super.paint(g);
     board.draw(g, scale, drawMesh, drawHidden);
   }
 
   // / The following methods are used to making scroll bars work.
 
   public Dimension getPreferredScrollableViewportSize() {
     return getPreferredSize();
   }
 
   public int getScrollableBlockIncrement(Rectangle visibleRect,
       int orientation, int direction) {
     if (orientation == SwingConstants.HORIZONTAL) {
       return visibleRect.width
           - getScrollableUnitIncrement(visibleRect, orientation, direction);
     }
     else {
       return visibleRect.height
           - getScrollableUnitIncrement(visibleRect, orientation, direction);
     }
   }
 
   public boolean getScrollableTracksViewportHeight() {
     return false;
   }
 
   public boolean getScrollableTracksViewportWidth() {
     return false;
   }
 
   private static final double SQRT32 = Math.sqrt(3) / 2;
 
   public int getScrollableUnitIncrement(Rectangle arg0, int orientation,
       int direction) {
     // if some clicks the control, we go one hexagon width/height at a time.
     if (orientation == SwingConstants.HORIZONTAL) {
       return (int) scale;
     }
     else {
       return (int) (scale * SQRT32);
     }
   }
 
 }
