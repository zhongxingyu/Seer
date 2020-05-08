 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 
 package net.rptools.maptool.model.drawing;
 
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.ZonePoint;
 
 /**
  * The radius template draws a highlight over all the squares effected from a specific
  * spine.
  * 
  * @author jgorrell
  * @version $Revision$ $Date$ $Author$
  */
 public class RadiusTemplate extends AbstractTemplate {
 
   /**
    * Paint the border at a specific radius.
    * 
    * @param g Where to paint
    * @param x Distance from vertex along X axis in cell coordinates.
    * @param y Distance from vertex along Y axis in cell coordinates.
    * @param xOff Distance from vertex along X axis in screen coordinates.
    * @param yOff Distance from vertex along Y axis in screen coordinates.
    * @param gridSize The size of one side of the grid in screen coordinates.
    * @param distance The distance in cells from the vertex to the cell which
    * is offset from the vertex by <code>x</code> & <code>y</code>.
    * @param radius The radius where the border is painted.
    * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int, int, int, int, int, int)
    */
   protected void paintBorderAtRadius(Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance, int radius) {
     
     // At the border? 
     if (distance == radius) {
       
       // Paint lines between vertical boundaries if needed
       if (getDistance(x + 1, y) > radius) {
         paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
         paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
         paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
         paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
       } // endif
       
       // Paint lines between horizontal boundaries if needed
       if (getDistance(x, y + 1) > radius) {
         paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
         paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
         paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
         paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
       } // endif
     } // endif
   }
   
   /*---------------------------------------------------------------------------------------------
    * Overridden AbstractTemplate Methods
    *-------------------------------------------------------------------------------------------*/
   
   /**
    * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int, int, int, int, int, int)
    */
   @Override
   protected void paintBorder(Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
     paintBorderAtRadius(g, x, y, xOff, yOff, gridSize, distance, getRadius());
 
     // At the center?
     if (x == 0 && y == 0)
       g.fillRect(xOff - 4, yOff - 4, 7, 7);
   }
 
   /**
    * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintArea(java.awt.Graphics2D, int, int, int, int, int, int)
    */
   @Override
   protected void paintArea(Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
 
     // Only squares w/in the radius
     if (distance <= getRadius()) {
       
       // Paint the squares
       paintArea(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
       paintArea(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
       paintArea(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
       paintArea(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
     } // endif
   }
 
   /*---------------------------------------------------------------------------------------------
    * Drawable Interface Methods
    *-------------------------------------------------------------------------------------------*/
   
   /**
    * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
    */
   public Rectangle getBounds() {
    int gridSize = MapTool.getCampaign().getZone(getZoneId()).getGrid().getSize();
     int quadrantSize = getRadius() * gridSize + BOUNDS_PADDING;
     ZonePoint vertex = getVertex();
     return new Rectangle(vertex.x - quadrantSize, vertex.y - quadrantSize, quadrantSize * 2, quadrantSize * 2);
   }
 }
