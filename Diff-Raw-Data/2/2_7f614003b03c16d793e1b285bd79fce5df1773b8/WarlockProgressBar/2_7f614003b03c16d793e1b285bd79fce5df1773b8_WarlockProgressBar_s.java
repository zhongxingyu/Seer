 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 /*
  * Created on Jan 15, 2005
  */
 package cc.warlock.rcp.ui;
 
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * @author Marshall
  * 
  * This is a custom progress bar that mimics the L&F of StormFront's status bars.
  * It's sort of a dirty hack, but it suffices for now. It needs to handle being in a LayoutManager better...
  */
 public class WarlockProgressBar extends Canvas
 {
 	protected Font progressFont;
 	protected String label;
 	protected Color foreground, background, borderColor;
 	protected int min, max, selection;
 	protected int width, height;
 	protected int borderWidth;
 	protected boolean showText;
 	
 	public WarlockProgressBar (Composite composite, int style)
 	{
 		super(composite, style);
 		
 		// defaults
 		width = 100; height = 15;
 		showText = true;
 		
 		Font textFont = JFaceResources.getDefaultFont();
 		FontData textData = textFont.getFontData()[0];
 		int minHeight = 8;
 		
 		progressFont = new Font(getShell().getDisplay(),
 			textData.getName(), (int)Math.max(minHeight,textData.getHeight()), textData.getStyle());
 		foreground = new Color(getShell().getDisplay(), 255, 255, 255);
 		background = new Color(getShell().getDisplay(), 0, 0, 0);
 		borderColor = new Color(getShell().getDisplay(), 25, 25, 25);
 		
 		borderWidth = 1;
 		
 		addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				if (label != null) {
 					Rectangle bounds = getBounds();
 					
 					e.gc.setFont (progressFont);
 					
 					Point extent = e.gc.textExtent(label);
 					
 					int totalPixels = 0;
 					for (int i = 0; i < label.length(); i++)
 					{
 						totalPixels += e.gc.getCharWidth(label.charAt(i));
 						totalPixels += e.gc.getAdvanceWidth(label.charAt(i));
 					}
 					
 					int left = (int) Math.floor(((bounds.width - (borderWidth * 2)) - extent.x) / 2.0);
 					int top = (int) Math.floor(((bounds.height - (borderWidth * 2)) - e.gc.getFontMetrics().getHeight()) / 2.0);
 					
 					int barWidth = 0;
 					int fullBarWidth = (bounds.width - (borderWidth*2));
 					int fullBarHeight = (bounds.height - (borderWidth*2));
 					
 					if (max > min)
 					{
 						double decimal = (selection / ((double)(max - min)));
 						barWidth = (int) Math.floor(decimal * fullBarWidth - 1);
 					}
 					
 					Color gradientColor = getGradientColor(25, true);
 					e.gc.setBackground(gradientColor);
 					e.gc.setForeground(background);
 					e.gc.fillGradientRectangle(borderWidth, borderWidth, barWidth, fullBarHeight, false);
 					
 					e.gc.setBackground(borderColor);
 					e.gc.fillRectangle(borderWidth + barWidth, borderWidth, fullBarWidth, fullBarHeight);
 					
 					e.gc.setForeground(borderColor);
 					e.gc.setLineWidth(borderWidth);
 					e.gc.drawRectangle(0, 0, bounds.width, bounds.height);
 					
 					if (showText)
 					{
 						e.gc.setForeground(foreground);
 						e.gc.drawText (label, left, top, true);
 					}
 				}
 			}
 		});
 	}
 	
 	private Color getGradientColor (int factor, boolean lighter)
 	{
 		int red = 0;
 		int green = 0;
 		int blue = 0;
 		
 		if (lighter) 
 		{
 			red = background.getRed() < (255 - factor) ? background.getRed() + factor : 255;
 			green = background.getGreen() < (255 - factor) ? background.getGreen() + factor : 255;
 			blue = background.getBlue() < (255 - factor) ? background.getBlue() + factor : 255;
 		}
 		else {
 			red = background.getRed() > factor ? background.getRed() - factor : 0;
 			green = background.getRed() > factor ? background.getRed() - factor : 0;
 			blue = background.getRed() > factor ? background.getRed() - factor : 0;
 		}
 		
 		return new Color(getShell().getDisplay(), red, green, blue);
 	}
 	
 	public void setSize(int width, int height) {
 		this.width = width;
 		this.height = height;
 	}
 	
 	public Point computeSize(int wHint, int hHint, boolean changed) {
 		
 		return new Point (width, height);
 	}
 	
 	public void setForeground (Color color)
 	{
 		foreground = color;
 		redraw();
 	}
 	
 	public void setBackground (Color color)
 	{
 		background = color;
 		redraw();
 	}
 	
 	public void setLabel (String label)
 	{
 		this.label = label;
 		redraw();
 	}
 	
 	public void setMinimum (int min)
 	{
 		this.min = min;
 	}
 	
 	public void setMaximum (int max)
 	{
 		this.max = max;
 	}
 	
 	public int getSelection ()
 	{
 		return selection;
 	}
 	
 	public void setSelection (int selection)
 	{
 		this.selection = selection;
 		redraw();
 	}
 	
 	public void setShowText (boolean showText)
 	{
 		this.showText = showText;
 		redraw();
 	}
 	
 	public void dispose() {
 		background.dispose();
 		foreground.dispose();
 		progressFont.dispose();
 		
 		super.dispose();
 	}
 
 	public Color getBorderColor() {
 		return borderColor;
 	}
 
 	public void setBorderColor(Color borderColor) {
 		this.borderColor = borderColor;
 	}
 
 	public int getBorderWidth() {
 		return borderWidth;
 	}
 
 	public void setBorderWidth(int borderWidth) {
 		this.borderWidth = borderWidth;
 	}
 }
