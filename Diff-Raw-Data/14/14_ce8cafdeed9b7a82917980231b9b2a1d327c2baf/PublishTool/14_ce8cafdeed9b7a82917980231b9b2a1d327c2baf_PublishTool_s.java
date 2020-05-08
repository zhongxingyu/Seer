 /*
  * EraseTool.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable.tools;
 
 import java.awt.*;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import com.galactanet.gametable.GametableCanvas;
 import com.galactanet.gametable.Pog;
 import com.galactanet.gametable.LineSegment;
 import com.galactanet.gametable.GametableFrame;
 
 
 /**
  * Map tool for erasing lines.
  * 
  * @author iffy
  */
 public class PublishTool extends NullTool
 {
     private GametableCanvas m_canvas;
     private Point           m_mouseAnchor;
     private Point           m_mouseFloat;
    private boolean         m_bEraseColor;
 
     /**
      * Default Constructor.
      */
     public PublishTool()
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
     	if ( m_canvas.isPublicMap() )
     	{
     		// this tool is not useable on the public map. So we cancel this action
     		GametableFrame.g_gameTableFrame.getToolManager().cancelToolAction();
     		return;
     	}
     	
         m_mouseAnchor = new Point(x, y);
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
             setTints();
             m_canvas.repaint();
         }
     }
     
     // turns off all the tinting for the pogs
     public void clearTints()
     {
     	for ( int i=0 ; i<m_canvas.getActiveMap().getNumPogs() ; i++ )
     	{
     		m_canvas.getActiveMap().getPogAt(i).m_bTinted = false;
     	}
     }
     
     // sets all the pogs we're touching to be tinted
     public void setTints()
     {
     	Rectangle selRect = createRectangle(m_mouseAnchor, m_mouseFloat);
     	
     	for ( int i=0 ; i<m_canvas.getActiveMap().getNumPogs() ; i++ )
     	{
     		Pog pog = m_canvas.getActiveMap().getPogAt(i);
     		
     		int size = pog.getFaceSize()*GametableCanvas.BASE_SQUARE_SIZE;
     		Point tl = new Point(pog.getPosition());
     		Point br = new Point(pog.getPosition());
     		br.x += size;
     		br.y += size;
     		Rectangle pogRect = createRectangle(tl,br);
     		
     		if ( selRect.intersects(pogRect) )
     		{
     			// this pog will be sent
     			pog.m_bTinted = true;
     		}
     		else
     		{
     			pog.m_bTinted = false;
     		}
     	}
     }
 
     /*
      * @see com.galactanet.gametable.AbstractTool#mouseButtonReleased(int, int)
      */
     public void mouseButtonReleased(int x, int y, int modifierMask)
     {
         if (m_mouseAnchor != null && !m_mouseAnchor.equals(m_mouseFloat))
         {
        	GametableFrame frame = GametableFrame.g_gameTableFrame;
         	
         	// first off, copy all the pogs/underlays over to the public layer
         	for ( int i=0 ; i<m_canvas.getPrivateMap().getNumPogs() ; i++ )
         	{
         		Pog pog = m_canvas.getActiveMap().getPogAt(i); 
         		if ( pog.m_bTinted == true )
 				{
 					// this pog gets copied
 					Pog newPog = new Pog(pog);
 					newPog.getUniqueID();
 					
 					m_canvas.setActiveMap(m_canvas.getPublicMap());
 					m_canvas.addPog(newPog);
 					m_canvas.setActiveMap(m_canvas.getPrivateMap());
 				}
         	}
         	
         	// now, copy over all the line segments. we run through all the
         	// line segments on the private layer, and collect a list of the
         	// ones that are at least partially in the rect
         	List lineList = new ArrayList();
         	
         	for ( int i=0 ; i<m_canvas.getPrivateMap().getNumLines() ; i++ )
         	{
         		LineSegment ls = m_canvas.getPrivateMap().getLineAt(i);
         		LineSegment result = ls.getPortionInsideRect(m_mouseAnchor, m_mouseFloat);
         		
         		if ( result != null )
         		{
         			lineList.add(result);
         		}
         	}
         	
         	// now we have the list of lines to move over. Make them into
         	// a LineSegment[]
         	LineSegment[] toAdd = new LineSegment[lineList.size()];
         	for ( int i=0 ; i<toAdd.length ; i++ )
         	{
         		toAdd[i] = (LineSegment)lineList.get(i);
         	}
         	
 			m_canvas.setActiveMap(m_canvas.getPublicMap());
 			m_canvas.addLineSegments(toAdd);
 			m_canvas.setActiveMap(m_canvas.getPrivateMap());
         	
         	boolean bDeleteFromPrivate = false;
         	if ((modifierMask & MODIFIER_CTRL) == 0) // not holding control
         	{
         		bDeleteFromPrivate = true;
         	}
         	
         	// if bDeleteFromPrivate is set, then this is a MOVE, not a COPY,
         	// so we have to remove the pieces from the private layer. 
         	
         	if ( bDeleteFromPrivate )
         	{
         		// remove the pogs that we moved
             	for ( int i=0 ; i<m_canvas.getPrivateMap().getNumPogs() ; i++ )
             	{
             		Pog pog = m_canvas.getActiveMap().getPogAt(i); 
             		if ( pog.m_bTinted == true )
     				{
 						m_canvas.removePog(pog.m_ID);
 						i--;
     				}
             	}
             	
             	// remove the line segments
             	Rectangle eraseRect = createRectangle(m_mouseAnchor, m_mouseFloat);
             	m_canvas.erase(eraseRect, false, -1);
         	}
         }
         endAction();
     }
 
     public void endAction()
     {
     	clearTints();
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
             g2.setColor(Color.BLACK);
             g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {
                 2f
             }, 0f));
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
