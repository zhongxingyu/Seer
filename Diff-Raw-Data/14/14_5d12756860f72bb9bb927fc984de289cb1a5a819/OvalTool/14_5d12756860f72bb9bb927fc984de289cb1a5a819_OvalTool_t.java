 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
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
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Ellipse2D;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.tool.ToolHelper;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.drawing.DrawableColorPaint;
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.model.drawing.ShapeDrawable;
 
 
 /**
  * @author drice
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OvalTool extends AbstractDrawingTool implements MouseMotionListener {
     private static final long serialVersionUID = 3258413928311830323L;
 
     protected Rectangle oval;
     private ZonePoint originPoint;
     
     public OvalTool() {
         try {
             setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/drawcirc.png"))));
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }
     
     @Override
     public String getTooltip() {
         return "Draw an oval";
     }
     
     @Override
     public String getInstructions() {
     	return "tool.oval.instructions";
     }
 
     public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
         if (oval != null) {
         	Pen pen = getPen();
         	
             if (pen.isEraser()) {
                 pen = new Pen(pen);
                 pen.setEraser(false);
                 pen.setPaint(new DrawableColorPaint(Color.white));
                 pen.setBackgroundPaint(new DrawableColorPaint(Color.white));
             }
 
             paintTransformed(g, renderer, new ShapeDrawable(new Ellipse2D.Float(oval.x, oval.y, oval.width, oval.height)), pen);
             
             ToolHelper.drawBoxedMeasurement(renderer, g, ScreenPoint.fromZonePoint(renderer, oval.x, oval.y), ScreenPoint.fromZonePoint(renderer, oval.x + oval.width, oval.y+oval.height));
         }
     }
 
     public void mousePressed(MouseEvent e) {
 
     	if (SwingUtilities.isLeftMouseButton(e)) {
 	    	ZonePoint zp = getPoint(e);
 	        
 	        if (oval == null) {
 	        	originPoint = zp;
 	        	oval = createRect(zp, zp);
 	        } else {
	        	oval = createRect(originPoint, zp);
 	            
 	            completeDrawable(renderer.getZone().getId(), getPen(), new ShapeDrawable(new Ellipse2D.Float(oval.x, oval.y, oval.width, oval.height), true));
 	            oval = null;
 	        }
 	
 	    	setIsEraser(isEraser(e));
     	}
     	
     	super.mousePressed(e);
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
     	
     	if (oval == null) {
     		super.mouseDragged(e);
     	}
     }
 
     /* (non-Javadoc)
      * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
      */
     public void mouseMoved(MouseEvent e) {
     	
     	if (oval != null) {
 
     		ZonePoint p = getPoint(e);
     		
     		oval = createRect(originPoint, p);
 	        
 	        renderer.repaint();
     	}
     }
     
   /**
    * @see net.rptools.maptool.client.ui.Tool#resetTool()
    */
   @Override
   protected void resetTool() {
 
 	  if (oval != null) {
 	    oval = null;
 	    renderer.repaint();
 	  } else {
 		  super.resetTool();
 	  }
   }
 }
