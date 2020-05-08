 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2010, Warlock LLC, and individual contributors as indicated
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
 
 package cc.warlock.rcp.stormfront.ui;
 
 import java.util.HashMap;
 
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
 import org.eclipse.swt.widgets.Display;
 
 import cc.warlock.core.stormfront.client.IStormFrontDialogMessage;
 import cc.warlock.core.stormfront.client.StormFrontProgressBar;
 
 /**
  * @author Marshall
  * 
  * This is a custom progress bar that mimics the L&F of StormFront's status bars.
  * It's sort of a dirty hack, but it suffices for now. It needs to handle being in a LayoutManager better...
  */
 public class StormFrontDialogControl extends Canvas
 {
 	protected Font progressFont;
 	protected int width, height;
 	protected int borderWidth;
 	protected HashMap<String, StormFrontProgressBar> progressBars =
 		new HashMap<String, StormFrontProgressBar>();
 	
 	protected Color healthFG, healthBG, healthBorder,
 		manaFG, manaBG, manaBorder,
 		fatigueFG, fatigueBG, fatigueBorder,
 		spiritFG, spiritBG, spiritBorder,
 		defaultFG, defaultBG, defaultBorder;
 	
 	public StormFrontDialogControl (Composite composite, int style)
 	{
 		super(composite, style);
 		Display display = this.getDisplay();
 		
 		// defaults
 		width = 100;
 		height = 15;
 		
 		Font textFont = JFaceResources.getDefaultFont();
 		FontData textData = textFont.getFontData()[0];
 		int minHeight = 8;
 		
 		healthBG = new Color(display, 0x80, 0, 0);
 		healthFG = new Color(display, 255, 255, 255);
 		healthBorder = new Color(display, 0x79, 0x6a, 0x6a);
 		
 		manaBG = new Color(display, 0, 0, 0xff);
 		manaFG = new Color(display, 255, 255, 255);
 		manaBorder = new Color(display, 0x72, 0x72, 0xff);
 		
 		fatigueBG = new Color(display, 0xd0, 0x98, 0x2f);
 		fatigueFG = new Color(display, 0, 0, 0);
 		fatigueBorder = new Color(display, 0xde, 0xcc, 0xaa);
 		
 		spiritBG = new Color(display, 150, 150, 150);
 		spiritFG = new Color(display, 0, 0, 0);
 		spiritBorder = new Color(display, 225, 225, 225);
 		
 		defaultBG = new Color(display, 150, 150, 150);
 		defaultFG = new Color(display, 0, 0, 0);
 		defaultBorder = new Color(display, 225, 225, 225);
 		
 		progressFont = new Font(getShell().getDisplay(),
 			textData.getName(), (int)Math.max(minHeight,textData.getHeight()), textData.getStyle());
 		
 		borderWidth = 1;
 		
 		addPaintListener(new PaintListener() {
 			public void paintControl(PaintEvent e) {
 				Rectangle bounds = getBounds();
 				
 				e.gc.setFont (progressFont);
 				
 				for(StormFrontProgressBar progressBar : progressBars.values()) {
 					int pbWidth = progressBar.getWidth();
 					int pbLeft = progressBar.getLeft();
 					int pbValue = progressBar.getValue();
 					
 					// This should probably all be abstracted out
 					int fullBarWidth = pbWidth * bounds.width / 100;
 					int barWidth = fullBarWidth - 2 * borderWidth;
 					int barHeight = bounds.height - 2 * borderWidth;
 					int filledWidth = pbValue * barWidth / 100;
 					int left = pbLeft * bounds.width / 100;
 					
 					// Draw the border
 					Color borderColor = getBorderColor(progressBar.id);
 					e.gc.setForeground(borderColor);
 					e.gc.setLineWidth(borderWidth);
 					e.gc.drawRectangle(left, 0, fullBarWidth, bounds.height);
 					
 					Color bgColor = getBgColor(progressBar.id);
 					
 					// draw the filled part of the rectangle
 					Color gradientColor = getGradientColor(25, true, bgColor);
 					e.gc.setBackground(gradientColor);
 					e.gc.setForeground(bgColor);
 					e.gc.fillGradientRectangle(borderWidth + left, borderWidth,
 							filledWidth, barHeight, false);
 					
 					// draw the background
 					e.gc.setBackground(borderColor);
 					e.gc.fillRectangle(borderWidth + left + filledWidth,
 							borderWidth, barWidth - filledWidth, barHeight);
 					
 					Color textColor = getTextColor(progressBar.id);
 					e.gc.setForeground(textColor);
 					
 					String text = progressBar.text;
 					Point extent = e.gc.textExtent(text);
 					
 					int text_left = left + (barWidth - extent.x) / 2;
 					int text_top = (bounds.height - 2 * borderWidth - e.gc.getFontMetrics().getHeight()) / 2;
 					e.gc.drawText (text, text_left, text_top, true);
 				}
 			}
 		});
 	}
 	
 	private Color getGradientColor (int factor, boolean lighter, Color color)
 	{
 		int red = 0;
 		int green = 0;
 		int blue = 0;
 		
 		if (lighter) 
 		{
 			red = color.getRed() < (255 - factor) ? color.getRed() + factor : 255;
 			green = color.getGreen() < (255 - factor) ? color.getGreen() + factor : 255;
 			blue = color.getBlue() < (255 - factor) ? color.getBlue() + factor : 255;
 		}
 		else {
 			red = color.getRed() > factor ? color.getRed() - factor : 0;
 			green = color.getRed() > factor ? color.getRed() - factor : 0;
 			blue = color.getRed() > factor ? color.getRed() - factor : 0;
 		}
 		
 		return new Color(getShell().getDisplay(), red, green, blue);
 	}
 	
 	public void setSize(int width, int height) {
 		this.width = width;
 		this.height = height;
 		
 		redraw();
 	}
 	
 	public Point computeSize(int wHint, int hHint, boolean changed) {
 		
 		return new Point (width, height);
 	}
 	
 	public void dispose() {
 		progressFont.dispose();
 		
 		super.dispose();
 	}
 
 	public void sendMessage(IStormFrontDialogMessage msg) {
 		if(msg instanceof StormFrontProgressBar) {
 			StormFrontProgressBar bar = (StormFrontProgressBar) msg;
 			progressBars.put(bar.id, bar);
 		}
 		redraw();
 	}
 	
 	private Color getTextColor(String id) {
 		
 		if(id.equals("health"))
 			return healthFG;
 		
 		if(id.equals("mana"))
 			return manaFG;
 		
 		if(id.equals("spirit"))
 			return spiritFG;
 		
		if(id.equals("fatigue"))
 			return fatigueFG;
 		
 		return defaultFG;
 	}
 	
 	private Color getBgColor(String id) {
 		
 		if(id.equals("health"))
 			return healthBG;
 		
 		if(id.equals("mana"))
 			return manaBG;
 		
 		if(id.equals("spirit"))
 			return spiritBG;
 		
		if(id.equals("fatigue"))
 			return fatigueBG;
 		
 		return defaultBG;
 	}
 	
 	private Color getBorderColor(String id) {
 		
 		if(id.equals("health"))
 			return healthBorder;
 		
 		if(id.equals("mana"))
 			return manaBorder;
 		
 		if(id.equals("spirit"))
 			return spiritBorder;
 		
		if(id.equals("fatigue"))
 			return fatigueBorder;
 		
 		return defaultBorder;
 	}
 }
