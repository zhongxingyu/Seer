 /*
 * LineTool.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable.tools;
 
 import java.awt.*;
 
 import com.galactanet.gametable.GametableCanvas;
 import com.galactanet.gametable.GametableFrame;
 
 
 /**
  * Tool for measuring distances on the map.
  * 
  * @author ATW
  */
 public class RulerTool extends NullTool
 {
     private GametableCanvas m_canvas;
     private Point           m_mouseAnchor;
     private Point           m_mouseFloat;
 
 
 
     /**
      * Default Constructor.
      */
     public RulerTool()
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
         if (m_mouseAnchor != null)
         {
             m_mouseAnchor = null;
             m_mouseFloat = null;
             m_canvas.repaint();
         }
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
             Point drawAnchor = m_canvas.modelToDraw(m_mouseAnchor);
             Point drawFloat = m_canvas.modelToDraw(m_mouseFloat);
             g2.drawLine(drawAnchor.x, drawAnchor.y, drawFloat.x, drawFloat.y);
 
             if (squaresDistance >= 0.75)
             {
                 Graphics2D g3 = (Graphics2D)g.create();
                 g3.setFont(Font.decode("sans-12"));
                 Point midPoint = new Point(m_mouseAnchor.x + dx / 2, m_mouseAnchor.y + dy / 2);
                 Point drawMidPoint = m_canvas.modelToDraw(midPoint);
                 
                 String s = squaresDistance + "u";
                 FontMetrics fm = g3.getFontMetrics();
                 Rectangle rect = fm.getStringBounds(s, g3).getBounds();
 
                 rect.grow(3, 1);
 
                 g3.translate(drawMidPoint.x, drawMidPoint.y);
                 g3.setColor(new Color(0x00, 0x99, 0x00, 0xAA));
                 g3.fill(rect);
                 g3.setColor(new Color(0x00, 0x66, 0x00));
                 g3.draw(rect);
                 g3.setColor(new Color(0xFF, 0xFF, 0xFF, 0xCC));
                 g3.drawString(s, 0, 0);
                 g3.dispose();
             }
 
             g2.dispose();
         }
     }
 }
