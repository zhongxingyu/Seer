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
 
 import java.awt.AlphaComposite;
 import java.awt.Composite;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Shape;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.ZonePoint;
 
 /**
  * The blast template draws a square for D&D 4e
  * 
  * @author jgorrell
  * @version $Revision: $ $Date: $ $Author: $
  */
 public class BlastTemplate extends ConeTemplate {
 
     /*---------------------------------------------------------------------------------------------
      * Instance Variables
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * Renderer for the blast. The {@link Shape} is just a rectangle. 
      */
     private ShapeDrawable renderer = new ShapeDrawable(new Rectangle());
     
     /*---------------------------------------------------------------------------------------------
      * Instance Methods
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * This methods adjusts the rectangle in the renderer to match the new radius, vertex, or direction.
      * Due to the fact that it is impossible to draw to the cardinal directions evenly when the radius
      * is an even number and still stay in the squares, that case isn't allowed. 
      */
     private void adjustRectangle() {
         if (getZoneId() == null) return;
         int gridSize = MapTool.getCampaign().getZone(getZoneId()).getGrid().getSize();
         int half = (getRadius() / 2) * gridSize;
         int size = getRadius() * gridSize;
         Rectangle r = (Rectangle)renderer.getShape();
         r.setBounds(getVertex().x, getVertex().y, size, size);
         switch (getDirection()) {
         case WEST:
             r.x -= size;
             r.y -= half;
             break;
         case NORTH_WEST:
             r.x -= size;
             r.y -= size;
             break;
         case NORTH:
             r.x -= half;
             r.y -= size;
             break;
         case NORTH_EAST:
             r.y -= size;
             r.x += gridSize;
             break;
         case EAST:
             r.y -= half;
             r.x += gridSize;
             break;
         case SOUTH_EAST:
             r.x += gridSize;
             r.y += gridSize;
             break;
         case SOUTH:
             r.x -= half;
             r.y += gridSize;
             break;
         case SOUTH_WEST:
             r.x -= size;
             r.y += gridSize;
             break;
         } // endswitch
     }
 
     /*---------------------------------------------------------------------------------------------
      * Overridden *Template Methods
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * @see net.rptools.maptool.model.drawing.ConeTemplate#setDirection(net.rptools.maptool.model.drawing.AbstractTemplate.Direction)
      */
     @Override
     public void setDirection(Direction direction) {
         super.setDirection(direction);
         adjustRectangle();
     }
 
     /**
      * @see net.rptools.maptool.model.drawing.AbstractTemplate#setRadius(int)
      */
     @Override
     public void setRadius(int squares) {
         super.setRadius(squares);
         adjustRectangle();
     }
 
     /**
      * @see net.rptools.maptool.model.drawing.AbstractTemplate#setVertex(net.rptools.maptool.model.ZonePoint)
      */
     @Override
     public void setVertex(ZonePoint vertex) {
         super.setVertex(vertex);
         adjustRectangle();
     }
 
     /**
      * @see net.rptools.maptool.model.drawing.AbstractTemplate#getDistance(int, int)
      */
     @Override
     public int getDistance(int x, int y) {
         return Math.max(x, y);
     }
 
     /*---------------------------------------------------------------------------------------------
      * Overridden AbstractDrawing Methods
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * @see net.rptools.maptool.model.drawing.AbstractDrawing#draw(java.awt.Graphics2D)
      */
     @Override
     protected void draw(Graphics2D g) {
       renderer.draw(g);
     }
 
     /**
      * @see net.rptools.maptool.model.drawing.AbstractDrawing#drawBackground(java.awt.Graphics2D)
      */
     @Override
     protected void drawBackground(Graphics2D g) {    
       Composite old = g.getComposite();
       if (old != AlphaComposite.Clear)
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DEFAULT_BG_ALPHA));
       renderer.drawBackground(g);
       g.setComposite(old);
     }
 }
