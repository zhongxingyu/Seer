 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft, Jay Gorrell
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 
 package net.rptools.maptool.client.tool.drawing;
 
 import java.awt.Graphics2D;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.drawing.AbstractTemplate;
 import net.rptools.maptool.model.drawing.LineTemplate;
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;
 
 /**
  * Draw the effected area of a spell area type of line.
  * 
  * @author jgorrell
  * @version $Revision$ $Date$ $Author$
  */
 public class LineTemplateTool extends RadiusTemplateTool implements PropertyChangeListener {
 
   /*---------------------------------------------------------------------------------------------
    * Instance Variables
    *-------------------------------------------------------------------------------------------*/
 
   /**
    * Has the anchoring point been set? When false, the anchor point is being
    * placed. When true, the area of effect is being drawn on the display.
    */
   private boolean pathAnchorSet;
 
   /*---------------------------------------------------------------------------------------------
    * Constructor 
    *-------------------------------------------------------------------------------------------*/
 
   /**
    * Add the icon to the toggle button.
    */
   public LineTemplateTool() {
     try {
       setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
           "net/rptools/maptool/client/image/tool/temp-blue-line.png"))));
     } catch (IOException ioe) {
       ioe.printStackTrace();
     } // endtry
     AppState.addPropertyChangeListener(AppState.USE_DOUBLE_WIDE_PROP_NAME, this);
   }
 
   /*---------------------------------------------------------------------------------------------
    * Overidden RadiusTemplateTool Methods
    *-------------------------------------------------------------------------------------------*/
 
   /**
    * @see net.rptools.maptool.client.ui.Tool#getInstructions()
    */
   @Override
   public String getInstructions() {
     return "tool.linetemplate.instructions";
   }
 
   /**
    * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#createBaseTemplate()
    */
   @Override
   protected AbstractTemplate createBaseTemplate() {
     return new LineTemplate();
   }
 
   /**
    * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#resetTool(net.rptools.maptool.model.ZonePoint)
    */
   @Override
   protected void resetTool(ZonePoint aVertex) {
     super.resetTool(aVertex);
     pathAnchorSet = false;
     ((LineTemplate) template).setDoubleWide(AppState.useDoubleWideLine());
   }
   
   /**
    * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#getTooltip()
    */
   @Override
   public String getTooltip() {
     return "tool.linetemplate.tooltip";
   }
   
   /*---------------------------------------------------------------------------------------------
    * Overridden AbstractDrawingTool Methods
    *-------------------------------------------------------------------------------------------*/
 
   /**
    * @see net.rptools.maptool.client.ui.zone.ZoneOverlay#paintOverlay(net.rptools.maptool.client.ui.zone.ZoneRenderer,
    *      java.awt.Graphics2D)
    */
   @Override
   public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
     if (painting && renderer != null) {
       Pen pen = getPenForOverlay();
       AffineTransform old = g.getTransform();
       g.setTransform(getPaintTransform(renderer));
       ZonePoint vertex = template.getVertex();
       ZonePoint pathVertex = ((LineTemplate) template).getPathVertex();
       template.draw(g, pen);
       paintCursor(g, pen.getPaint().getPaint(), pen.getThickness(), vertex);
       if (pathVertex != null) {
         paintCursor(g, pen.getPaint().getPaint(), pen.getThickness(), pathVertex);
       }
       g.setTransform(old);
      if (pathVertex != null) {
          paintRadius(g, vertex);
      }
     }
   }
 
   /**
    * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#getRadiusAtMouse(java.awt.event.MouseEvent)
    */
   @Override
   protected int getRadiusAtMouse(MouseEvent aE) {
     int radius = super.getRadiusAtMouse(aE);
     return Math.max(0, radius - 1);
   }
   
   /**
    * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#mousePressed(java.awt.event.MouseEvent)
    */
   @Override
   public void mousePressed(MouseEvent aE) {
     if (!painting)
       return;
     
     if (SwingUtilities.isLeftMouseButton(aE)) {
         
         // Need to set the anchor?
         controlOffset = null;
         if (!anchorSet) {
             anchorSet = true;
             return;
         } // endif
         if (!pathAnchorSet) {
             pathAnchorSet = true;
             return;
         } // endif
     } // endif
     
     // Let the radius code finish the template
     super.mousePressed(aE);
   }
 
   /*---------------------------------------------------------------------------------------------
    * MouseMotionListener Interface Methods
    *-------------------------------------------------------------------------------------------*/
 
   /**
    * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
    */
   public void mouseMoved(MouseEvent e) {
     
     // Setting anchor point
     LineTemplate lt = (LineTemplate) template;
     ZonePoint pathVertex = lt.getPathVertex();
     ZonePoint vertex = lt.getVertex();
     if (!anchorSet) {
       setCellAtMouse(e, vertex);
       controlOffset = null;
     } else if (!pathAnchorSet && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
       handleControlOffset(e, vertex);
     } else if (!pathAnchorSet) {
       template.setRadius(getRadiusAtMouse(e));
       controlOffset = null;
       
       // The path vertex remains null until it is set the first time.
       if (pathVertex == null) {
         pathVertex = new ZonePoint(vertex.x, vertex.y);
         lt.setPathVertex(pathVertex);
       } // endif
       if (pathVertex != null && setCellAtMouse(e, pathVertex)) 
         lt.clearPath();
         
         // Determine which of the extra squares are used on diagonals
       if (pathVertex != null) { 
         double dx = pathVertex.x - vertex.x;
         double dy = pathVertex.y - vertex.y;
         if (dx != 0 && dy != 0) { // Ignore straight lines
           boolean mouseSlopeGreater = false;
           double m = Math.abs(dy / dx);
           double edx = e.getX() - vertex.x;
           double edy = e.getY() - vertex.y;
           if (edx != 0 && edy != 0) { // Handle straight lines differently
             double em = Math.abs(edy / edx);
             mouseSlopeGreater = em > m;
           } else if (edx == 0) {
             mouseSlopeGreater = true;
           } // endif
           if (mouseSlopeGreater != lt.isMouseSlopeGreater()) {
             lt.setMouseSlopeGreater(mouseSlopeGreater);
             renderer.repaint();
           } // endif
         } // endif
       } // endif
     } else if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
       handleControlOffset(e, pathVertex);
     } else {
       template.setRadius(getRadiusAtMouse(e));
       renderer.repaint();
       controlOffset = null;
       return;
     } // endif
     
     // Quadrant change?
     if (pathVertex != null) {
       int dx = e.getX() - vertex.x;
       int dy = e.getY() - vertex.y;
       AbstractTemplate.Quadrant quadrant = (dx < 0) ? (dy < 0 ? Quadrant.NORTH_WEST : Quadrant.SOUTH_WEST)
           : (dy < 0 ? Quadrant.NORTH_EAST : Quadrant.SOUTH_EAST);
       if (quadrant != lt.getQuadrant()) {
         lt.setQuadrant(quadrant);
         renderer.repaint();
       } // endif
     } // endif
   }
   
   /*---------------------------------------------------------------------------------------------
    * PropertyChangeListener Interface Methods
    *-------------------------------------------------------------------------------------------*/
   
   /**
    * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
    */
   public void propertyChange(PropertyChangeEvent aEvt) {
     ((LineTemplate)template).setDoubleWide(((Boolean)aEvt.getNewValue()).booleanValue());
   }
 }
