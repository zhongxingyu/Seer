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
 
 package net.rptools.maptool.client.ui.token;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.drawing.RadiusTemplate;
 
 /**
  * This token template draws a radius around the token.
  * 
  * @author jgorrell
  * @version $Revision$ $Date$ $Author$
  */
 public class RadiusLightTokenTemplate extends RadiusTemplate implements TokenTemplate {
 
   /**
    * Color used to draw the bright light
    */
   private Color color;
   
   /**
    * The color used to paint the shadows
    */
   private Color shadowColor;
   
   /**
    * The radius where shadow illumination starts. It must be less than
    * or equal to the total radius.
    */
   private int shadowRadius;
 
   /**
    * The corner of the token where the light source is placed.
    */
   private String corner;
   
   /**
    * Default constructor sets a radius of 20' and gets the current pen.
    */
   public RadiusLightTokenTemplate() {
     setVertex(new ScreenPoint(0, 0));
     setRadius(4);
     setColor(Color.YELLOW);
   }
 
   /**
    * @see net.rptools.maptool.client.ui.token.TokenTemplate#paintTemplate(java.awt.Graphics2D, net.rptools.maptool.model.Token, java.awt.Rectangle, net.rptools.maptool.client.ui.zone.ZoneRenderer)
    */
   public void paintTemplate(Graphics2D aG, Token aToken, Rectangle aBounds, ZoneRenderer aRenderer) {
     
     // Offset for the corner
     ScreenPoint v = getVertex();
     Quadrant c = Quadrant.valueOf(corner);
     switch (c) {
     case NORTH_EAST:
      v.x = (int)aRenderer.getScaledGridSize();
       v.y = 0;
       break;
     case SOUTH_WEST:
       v.x = 0;
      v.y = (int)aRenderer.getScaledGridSize();
       break;
     case SOUTH_EAST:
      v.x = (int)aRenderer.getScaledGridSize();
      v.y = (int)aRenderer.getScaledGridSize();
       break;
     case NORTH_WEST:
     default:
       v.x = 0;
       v.y = 0;
       break;
     } // endswitch
     
     // Set scale and zone id
     setZoneId(aRenderer.getZone().getId());
     setScale(aRenderer.getScale());
     paint(aG, false, true);
   }
 
   /**
    * @see net.rptools.maptool.model.drawing.RadiusTemplate#paintArea(java.awt.Graphics2D, int, int, int, int, int, int)
    */
   @Override
   protected void paintArea(Graphics2D aG, int aX, int aY, int xOff, int yOff, int aGridSize, int aDistance) {
     if (aDistance <= shadowRadius)
       aG.setColor(getColor());
     else 
       aG.setColor(getShadowColor());
     super.paintArea(aG, aX, aY, xOff, yOff, aGridSize, aDistance);
   }
   /**
    * Get the shadowRadius for this RadiusTokenTemplate.
    *
    * @return Returns the current value of shadowRadius.
    */
   public int getShadowRadius() {
     return shadowRadius;
   }
 
   /**
    * Set the value of shadowRadius for this RadiusTokenTemplate.
    *
    * @param aShadowRadius The shadowRadius to set.
    */
   public void setShadowRadius(int aShadowRadius) {
     shadowRadius = aShadowRadius;
   }
 
   /**
    * Get the color for this RadiusTokenTemplate.
    *
    * @return Returns the current value of color.
    */
   public Color getColor() {
     return color;
   }
 
   /**
    * Set the value of color for this RadiusTokenTemplate.
    *
    * @param aColor The color to set.
    */
   public void setColor(Color aColor) {
     color = new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 255/6);
     if (shadowColor == null) setShadowColor(aColor);
   }
 
   /**
    * Get the shadowColor for this RadiusTokenTemplate.
    *
    * @return Returns the current value of shadowColor.
    */
   public Color getShadowColor() {
     return shadowColor;
   }
 
   /**
    * Set the value of shadowColor for this RadiusTokenTemplate.
    *
    * @param aColor The shadowColor to set.
    */
   public void setShadowColor(Color aColor) {
     shadowColor = new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 255/12);
   }
 
   /**
    * Get the corner for this RadiusLightTokenTemplate.
    *
    * @return Returns the current value of corner.
    */
   public Quadrant getCorner() {
     return Quadrant.valueOf(corner);
   }
 
   /**
    * Set the value of corner for this RadiusLightTokenTemplate.
    *
    * @param aCorner The corner to set.
    */
   public void setCorner(Quadrant aCorner) {
     corner = aCorner.toString();
   }
 }
