 package edu.utsa.tagger.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JComponent;
 
 @SuppressWarnings("serial")
 public class Collapser extends JComponent
 {
 	private static final Color COLLAPSER_UNCOLLAPSED_BORDER = Color.lightGray;
 	private static final Color COLLAPSER_MOUSEOVER_BORDER = new Color(0, 200, 255);
 	private static final Color COLLAPSER_COLLAPSED_BORDER = new Color(25, 25, 25);
 	private static final Color COLLAPSER_COLLAPSED_FILL = new Color(115, 115, 115);
 	
 	private boolean mouseover = false;
 	private boolean collapsed = false;
 	
 	public Collapser()
 	{
 		addMouseListener(new MouseAdapter()
 		{
			@Override public void mouseClicked(MouseEvent e){collapsed = !collapsed;}
 			@Override public void mouseEntered(MouseEvent e){mouseover = true;}
 			@Override public void mouseExited(MouseEvent e){mouseover = false;}
 		});
 	}
 	
 	public boolean isCollapsed()
 	{
 		return collapsed;
 	}
 	
 	public void setCollapsed(boolean collapsed)
 	{
 		this.collapsed = collapsed;
 	}
 	
 	@Override public Dimension getPreferredSize()
 	{
 		Rectangle r = getFont().getStringBounds(" ", ((Graphics2D) getGraphics()).getFontRenderContext()).getBounds();
 		return new Dimension(r.height-1, r.height-1);
 	}
 
 	@Override protected void paintComponent(Graphics graphics)
 	{
 		Graphics2D g = (Graphics2D) graphics;
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		double centerX = getWidth() / 2;
 		double centerY = getHeight() / 2;
 		double radius = getHeight() / 4;
 		double thetaA, thetaB, thetaC;
 
 		if (collapsed)
 		{
 			thetaA = Math.toRadians(-45);
 			thetaB = Math.toRadians(45);
 			thetaC = Math.toRadians(-135);
 		} else
 		{
 			thetaA = Math.toRadians(0);
 			thetaB = Math.toRadians(90);
 			thetaC = Math.toRadians(-90);
 		}
 
 		Polygon poly = new Polygon();
 		poly.addPoint((int)(centerX + radius * Math.cos(thetaA)), (int)(centerY - radius * Math.sin(thetaA)));
 		poly.addPoint((int)(centerX + radius * Math.cos(thetaB)), (int)(centerY - radius * Math.sin(thetaB)));
 		poly.addPoint((int)(centerX + radius * Math.cos(thetaC)), (int)(centerY - radius * Math.sin(thetaC)));
 
 		Color fill = collapsed && !mouseover ? COLLAPSER_COLLAPSED_FILL : null;
 		Color border = mouseover ? COLLAPSER_MOUSEOVER_BORDER : 
 			collapsed ? COLLAPSER_COLLAPSED_BORDER : COLLAPSER_UNCOLLAPSED_BORDER;
 		
 		if (fill != null)
 		{
 			g.setColor(fill);
 			g.fill(poly);
 		}
 		if (border != null)
 		{
 			g.setColor(border);
 			g.draw(poly);
 		}
 	}
 }
