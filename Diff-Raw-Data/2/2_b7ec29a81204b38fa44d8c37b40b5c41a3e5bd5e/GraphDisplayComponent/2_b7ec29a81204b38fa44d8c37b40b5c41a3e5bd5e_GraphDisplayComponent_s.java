 package com.ryanantkowiak.jUlamSpiral;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.JComponent;
 
 /**
  * This class paints the Ulam Spiral
  * 
  * @author Ryan Antkowiak (antkowiak@gmail.com)
  *
  */
 public class GraphDisplayComponent extends JComponent
 {
 	private static final long serialVersionUID = -5322578887924136534L;
 
 	private List<Point> m_pointList;
 	private BufferedImage m_image;
 	
 	// Constructor
 	public GraphDisplayComponent()
 	{
 		m_pointList = null;
 		m_image = null;
 	}
 	
 	/**
 	 * Sets the list of points that will be drawn
 	 * 
 	 * @param pointList
 	 */
 	public void setPointList(List<Point> pointList)
 	{
 		// Cache the point list as a member
 		m_pointList = pointList;
 		
 		// Create a buffered image of the right size
 		m_image = new BufferedImage(
 				getPreferredSize().width,
 				getPreferredSize().height,
 				BufferedImage.TYPE_INT_RGB);
 		
 		Graphics g = m_image.getGraphics();
 		
		// Start out with a whtie background
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);
 		
 		if (null != m_pointList)
 		{
 			// Draw each point black
 			g.setColor(Color.BLACK);
 			
 			for (Point p : m_pointList)
 				g.drawLine(p.x, p.y, p.x, p.y);
 		}
 	}
 	
 	/**
 	 * Paint the component (draw the buffered image)
 	 */
 	protected void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		
 		if (null != m_image)
 			g.drawImage(m_image, 0, 0, null);
 	}
 
 }
