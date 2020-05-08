 package com.stacktraceinc.hypercut2d;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class FancyCanvas extends JPanel {
 	JPanel childPanel = new JPanel();
 	CompEventListener compEventListener = new CompEventListener();
 	List<Part> partList = new ArrayList<Part>();
 	Dimension d;
 
 	public FancyCanvas(Dimension d){
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.fill = GridBagConstraints.CENTER;
 		c.anchor = GridBagConstraints.CENTER;
 
 		panelResize(d);
 		childPanel.addComponentListener(compEventListener);
 		add(childPanel);
 	}
 	
 	public void panelResize(Dimension d) {
 		this.d = d;
 		childPanel.setPreferredSize(d);
 		childPanel.setMinimumSize(null);
 		childPanel.setMinimumSize(d);
 		compEventListener.resize(d);
		childPanel.revalidate();
 	}
 
 	public void refresh() {
 		repaint();
 	}
 	
 	public RenderedImage getImage() {
 		BufferedImage bi = new BufferedImage(childPanel.getWidth(), childPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
 
 	    Graphics2D g = bi.createGraphics();
 	    paintOffset(g, 0, 0);
 	    g.dispose();
 	    return bi;
 	}	
 	
 	public void paint(Graphics g){
 		paintOffset(g, childPanel.getX(), childPanel.getY());
 		g.setColor(Color.GRAY);
 		g.fillRect(0, 0, getWidth(), childPanel.getY());
     g.fillRect(0, 0, childPanel.getX(), getHeight());
     g.fillRect(childPanel.getX()+childPanel.getWidth(), 0, getWidth(), getHeight());
     g.fillRect(0, childPanel.getY()+childPanel.getHeight(), getWidth(), getHeight());
 	}
 	
 	public void paintOffset(Graphics g, int x, int y){
 		g.setColor(Color.white);
 		g.fillRect(x, y, childPanel.getWidth(), childPanel.getHeight());
 
 		for (Part part: partList) {
 			if (part.getX() != -1 && part.getY() != -1){
 				g.setColor(Color.GRAY);
				g.drawRect(x + part.getX() -2, y + part.getY() -2, part.getFirstValue() + 4, part.getSecondValue() + 4);
 
 				g.setColor(part.getColor());
 				g.fillRect(x + part.getX(), y + part.getY(), part.getFirstValue(), part.getSecondValue());
 				g.setColor(Color.BLACK);
 				g.drawRect(x + part.getX(), y + part.getY(), part.getFirstValue(), part.getSecondValue());
 
 				g.drawString(part.getName(),
 						x + part.getX() + 5,
 						y + part.getY() + 15);
 				g.drawString(Integer.toString(part.getFirstValue()) + "x" + Integer.toString(part.getSecondValue()),
 						x + part.getX() + 5,
 						y + part.getY() + 27);
 			}
 		}
 	}
 	
 	/*public void paintOffset(Graphics g, int x, int y){
 		g.setColor(Color.white);
 		g.fillRect(x, y, childPanel.getWidth(), childPanel.getHeight());
 		
 		for (Part part: partList) {
 			if (part.getX() != -1 && part.getY() != -1 && part.getX() != -3 && part.getY() != -3)
 			{
 				g.setColor(part.getColor());
 				g.fillRect(x + part.getX(), y + part.getY(), part.getFirstValue(), part.getSecondValue());
 				g.setColor(Color.BLACK);
 				g.drawRect(x + part.getX(), y + part.getY(), part.getFirstValue(), part.getSecondValue());
 			
 				g.drawString(part.getName(), 
 						x + part.getX() + 5, 
 						y + part.getY() + 15);
 				g.drawString(Integer.toString(part.getFirstValue()) + "x" + Integer.toString(part.getSecondValue()), 
 							x + part.getX() + 5, 
 							y + part.getY() + 27);
 			}
 		}
 	}*/
 
 	public void setPartList(List<Part> newPartList){
 		this.partList = newPartList;
 	}
 }
