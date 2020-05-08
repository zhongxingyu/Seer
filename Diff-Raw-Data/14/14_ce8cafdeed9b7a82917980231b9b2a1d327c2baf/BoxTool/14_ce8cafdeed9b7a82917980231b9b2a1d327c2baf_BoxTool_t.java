 /*
  * EraseTool.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable.tools;
 
 import java.awt.*;
 
 import com.galactanet.gametable.GametableCanvas;
 import com.galactanet.gametable.GametableFrame;
 import com.galactanet.gametable.LineSegment;
 
 
 /**
  * Map tool for erasing lines.
  * 
  * @author iffy
  */
 public class BoxTool extends NullTool
 {
     private GametableCanvas m_canvas;
     private Point           m_mouseAnchor;
     private Point           m_mouseFloat;
 
     /**
      * Default Constructor.
      */
     public BoxTool()
     {
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#activate(com.galactanet.gametable.GametableCanvas)
      */
     public void activate(GametableCanvas canvas)
     {
         m_canvas = canvas;
         m_mouseAnchor = null;
         m_mouseFloat = null;
     }
 
     /*
      * @see com.galactanet.gametable.Tool#isBeingUsed()
      */
     public boolean isBeingUsed()
     {
         return (m_mouseAnchor != null);
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseButtonPressed(int, int)
      */
     public void mouseButtonPressed(int x, int y, int modifierMask)
     {
         m_mouseAnchor = new Point(x, y);
         if ((modifierMask & MODIFIER_CTRL) == 0)
         {
             m_mouseAnchor = m_canvas.snapPoint(m_mouseAnchor);
         }
         m_mouseFloat = m_mouseAnchor;
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseMoved(int, int)
      */
     public void mouseMoved(int x, int y, int modifierMask)
     {
         if (m_mouseAnchor != null)
         {
             m_mouseFloat = new Point(x, y);
             if ((modifierMask & MODIFIER_CTRL) == 0)
             {
                 m_mouseFloat = m_canvas.snapPoint(m_mouseFloat);
             }
             m_canvas.repaint();
         }
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
      */
     public void mouseButtonReleased(int x, int y, int modifierMask)
     {
         if (m_mouseAnchor != null && !m_mouseAnchor.equals(m_mouseFloat))
         {
         	// we're going to add 4 lines
             Color drawColor = GametableFrame.g_gameTableFrame.m_drawColor;
         	Point topLeft = new Point(m_mouseAnchor);
         	Point bottomRight = new Point(m_mouseFloat);
         	Point topRight = new Point(bottomRight.x, topLeft.y);
         	Point bottomLeft = new Point(topLeft.x, bottomRight.y);
         	
         	LineSegment top = new LineSegment(topLeft, topRight, drawColor);
         	LineSegment left = new LineSegment(topLeft, bottomLeft, drawColor);
         	LineSegment right = new LineSegment(topRight, bottomRight, drawColor);
         	LineSegment bottom = new LineSegment(bottomLeft, bottomRight, drawColor);
         	
         	LineSegment[] toAdd = new LineSegment[] {
         			top,
 					left,
 					right,
 					bottom
         	};
 				
         	m_canvas.addLineSegments(toAdd);
         }
         endAction();
     }
     
     public void endAction()
     {
         m_mouseAnchor = null;
         m_mouseFloat = null;
     	m_canvas.repaint();
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#paint(java.awt.Graphics)
      */
     public void paint(Graphics g)
     {
         if (m_mouseAnchor != null)
         {
             Graphics2D g2 = (Graphics2D)g.create();
 
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
             int dx = m_mouseFloat.x - m_mouseAnchor.x;
             int dy = m_mouseFloat.y - m_mouseAnchor.y;
             double dist = Math.sqrt(dx * dx + dy * dy);
             double squaresDistance = m_canvas.modelToSquares(dist);
             squaresDistance = Math.round(squaresDistance * 100) / 100.0;
 
             Color drawColor = GametableFrame.g_gameTableFrame.m_drawColor;
             g2.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 102));
             g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
             Rectangle rect = createRectangle(m_canvas.modelToDraw(m_mouseAnchor), m_canvas.modelToDraw(m_mouseFloat));
             g2.draw(rect);
             g2.dispose();
         }
     }
 
     private static Rectangle createRectangle(Point a, Point b)
     {
         int x = Math.min(a.x, b.x);
         int y = Math.min(a.y, b.y);
         int width = Math.abs(b.x - a.x) + 1;
         int height = Math.abs(b.y - a.y) + 1;
 
         return new Rectangle(x, y, width, height);
     }
 }
