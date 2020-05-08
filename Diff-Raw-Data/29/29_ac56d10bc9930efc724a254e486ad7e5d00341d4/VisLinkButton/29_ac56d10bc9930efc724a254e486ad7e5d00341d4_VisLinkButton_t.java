 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 package org.nees.rpi.vis.ui;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.ActionEvent;
 import java.net.URL;
 import java.io.IOException;
 
 /**
  * 
  */
public class VisLinkButton extends VisButton
 {
 	private int paddingX = 5;
 	private int paddingY = 2;
 	private double imgScale;
 	private boolean hover = false;
 
 
 	public VisLinkButton(String text)
 	{
 		this(text, null);
 	}
 
 	public VisLinkButton(String text, URL icon)
 	{
 		super(text);
 
 		setBorderPainted(false);
 		setFocusPainted(false);
 		setContentAreaFilled(false);
 		Font font = new Font("Tahoma", Font.PLAIN, 13);
 		setFont(font);
 		setForeground(Color.decode("#1B4880"));
 		Dimension tDim = calcTextSize(font);
 		imgScale = tDim.getHeight() + (2*paddingY);
 
 		setIcon(icon);
 		addMouseListener(new MouseHandler());
 	}
 
 	public void setIcon(URL icon)
 	{
 		if (icon != null)
 			iconimage = processIconImage(icon);
 		else
 			iconimage = null;
 		
 		setPreferredSize(calcSize());
 	}
 
 	BufferedImage processIconImage(URL imageURL)
 	{
 		try
 		{
 			Image scaled = VisUtils.scaleImage(ImageIO.read(imageURL), imgScale);
 			return VisUtils.toBufferedImage(scaled, BufferedImage.TYPE_INT_ARGB);
 		}
 		catch (IOException e)
 		{
 			//TODO proper error management
 			e.printStackTrace();	//To change body of catch statement use File | Settings | File Templates.
 			return null;
 		}
 	}
final static float dash1[] = {3.0f};
final static BasicStroke dashed = new BasicStroke(1.0f,
									  BasicStroke.CAP_BUTT,
									  BasicStroke.JOIN_MITER,
									  10.0f, dash1, 0.0f);
 
 	public void paintComponent(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D) g;
 		int fontHeight = g2.getFontMetrics().getHeight();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		if (hover)
 			g2.setColor(Color.decode("#CC0000"));
 		if (!isEnabled())
 			g2.setColor(Color.decode("#999999"));
 		int posHeight = fontHeight;
 		if (getHeight()/2 > fontHeight)
 			posHeight = getHeight()/2 + fontHeight;
 		else
 			posHeight = getHeight()/2 + (fontHeight - getHeight()/2)/2;
 		int posWidth = g2.getFontMetrics().stringWidth(getText());
 		int imageW = 0;
 		if (iconimage != null)
 		{
 			imageW = iconimage.getWidth(this);
 			if (isEnabled())
				g2.drawImage(iconimage, paddingX, 0, this);
 			else
				g2.drawImage(VisUtils.getGrayScaleImage(iconimage), paddingX, 0,this);
			g2.drawString(getText(), paddingX * 2 + imageW, posHeight);
			g2.drawLine(paddingX * 2 + imageW, posHeight + 1, paddingX * 2 + imageW + posWidth, posHeight + 1);
 		}
 		else
 		{
			g2.drawString(getText(), paddingX, posHeight);
			g2.drawLine(paddingX, posHeight + 1, posWidth + paddingX, posHeight + 1);
		}
		if (isFocusOwner())
		{
			g2.setColor(Color.decode("#999999"));
			g2.setStroke(dashed);
			g2.drawRoundRect(0,0, getWidth() - 1, getHeight() - 1 - paddingY, 5, 5);
 		}
 	}
 
 	void toggle(ActionEvent e)
 	{
 		super.toggle(e);
 		setPreferredSize(calcSize());
 	}
 
 	private Dimension calcSize()
 	{
 		Dimension textDimension = calcTextSize(getFont());
 		int imageExtraW = 0;
 		int padx = (paddingX * 2), pady = (paddingY * 2);
 
 		if (iconimage != null)
 		{
 			imageExtraW = iconimage.getWidth();
 			padx += paddingX;
 		}
 
 		return
 			new Dimension(
 						(int) (textDimension.getWidth() + imageExtraW + padx),
 						(int) (textDimension.getHeight() + pady));
 	}
 
 	private Dimension calcTextSize(Font font)
 	{
 		BufferedImage bimage = new BufferedImage(40, 40,BufferedImage.TYPE_INT_RGB);
 		Graphics g = bimage.createGraphics();
 		FontMetrics fm = g.getFontMetrics(font);
 		return
 			new Dimension(fm.stringWidth(getText()),fm.getHeight());
 	}
 
 	private class MouseHandler implements MouseListener
 	{
 		public void mouseClicked(MouseEvent e) {}
 		public void mousePressed(MouseEvent e){}
 		public void mouseReleased(MouseEvent e) {}
 		public void mouseEntered(MouseEvent e)
 		{
 			VisLinkButton.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
 			VisLinkButton.this.hover = true;
 		}
 		public void mouseExited(MouseEvent e)
 		{
 			VisLinkButton.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 			VisLinkButton.this.hover = false;
 		}
 	}
 }
