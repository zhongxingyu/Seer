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
 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import net.rptools.common.util.ImageUtil;
 import net.rptools.maptool.model.drawing.Drawable;
 import net.rptools.maptool.model.drawing.DrawnElement;
 
 /**
  */
 public class DrawableRenderer {
 
 	private int drawableCount = 0;
 	private List<DrawableEntry> drawableEntries = new ArrayList<DrawableEntry>();
 	
 	private boolean repaint = true;
 
 	public void renderDrawables(Graphics g, List<DrawnElement> drawableList, int offsetX, int offsetY, double scale) {
 
 		if (drawableCount != drawableList.size()) {
 	        // A drawable has been deleted, need to create the entries all over again
 	        // since many drawn elements can be in the same entry.
 	        if (drawableCount > drawableList.size()) {
 		  	    drawableEntries.clear();
 	        }
 			
 			consolidateBounds(drawableList);
 			validateEntries();
 			
 			for (DrawnElement element : drawableList) {
 				
 				Drawable drawable = element.getDrawable();
 				
 				DrawableEntry entry = getEntryFor(drawable.getBounds());
 				
 				Graphics2D g2 = entry.image.createGraphics();
 				drawable.draw(g2, element.getPen(), entry.bounds.x, entry.bounds.y);
 				g2.dispose();
 			}
 		}
 		drawableCount = drawableList.size();
 		
 		Rectangle clipBounds = g.getClipBounds();
 		for (DrawableEntry entry : drawableEntries) {
 			
 			int x = (int)((entry.bounds.x * scale) + offsetX);
 			int y = (int)((entry.bounds.y * scale) + offsetY);
 			int width = (int)(entry.bounds.width * scale);
 			int height = (int)(entry.bounds.height * scale);
 			
 			if (clipBounds.intersects(x, y, width, height)) {
 				g.drawImage(entry.image, x, y, width, height, null);
 			}
 		}
 	}
 	
 	private DrawableEntry getEntryFor(Rectangle bounds) {
 		
 		for (int i = 0; i < drawableEntries.size(); i++) {
 			
 			DrawableEntry entry = drawableEntries.get(i);
 			
			if (entry.bounds.contains(bounds.x, bounds.y, bounds.width > 0 ? bounds.width : 1, bounds.height > 0 ? bounds.height : 1)) {
 				return entry;
 			}
 		}
 		
 		throw new IllegalStateException ("Could not find appropriate back buffer.");
 	}
 
 	private void validateEntries() {
 		
 		for (DrawableEntry entry : drawableEntries) {
 			entry.validate();
 		}
 	}
 	
 	private synchronized void consolidateBounds(List<DrawnElement> drawableList) {
 
 		// Make sure each drawable has a place to be drawn
 		OUTTER:
 		for (int i = 0; i < drawableList.size(); i++) {
 			
 			DrawnElement drawable = drawableList.get(i);
             int padding = (int)drawable.getPen().getThickness();
             
             // This should give 50% pen width drawing buffer 
 			Rectangle bounds = drawable.getDrawable().getBounds();
             bounds.x -= padding;
             bounds.y -= padding;
             bounds.width += padding * 2;
             bounds.height += padding * 2;
 			
 			for (int j = 0; j < drawableEntries.size(); j++) {
 
 				DrawableEntry entry = drawableEntries.get(j);
 				
 				// If they are completely within an existing space, then we're done
 				if (entry.bounds.contains(bounds)) {
 					continue OUTTER;
 				}
 			}
 			
 			// Otherwise, add a new area
 			drawableEntries.add(new DrawableEntry(bounds));
 		}
 	
 		// Combine any areas that are now overlapping
 		boolean changed = true;
 		while (changed) {
 			changed = false;
 			
 			for (int i = 0; i < drawableEntries.size(); i++) {
 				
 				DrawableEntry outterEntry = drawableEntries.get(i);
 				
 				// Combine with the rest of the list
 				for (ListIterator<DrawableEntry> iter = drawableEntries.listIterator(i + 1); iter.hasNext();) {
 					
 					DrawableEntry innerEntry = iter.next();
 					
 					// OPTIMIZE: This could be optimized to delay image creation
 					// until all bounds have been consolidated
 					if (outterEntry.bounds.intersects(innerEntry.bounds)) {
 						outterEntry = new DrawableEntry (outterEntry.bounds.union(innerEntry.bounds));
 						iter.remove();
 						
 						changed = true;
 					}
 				}
 				
 				if (changed) {
 					drawableEntries.set(i, outterEntry);
 				}
 			}
 		}
 	}
 	
 	private static class DrawableEntry {
 		
 		public Rectangle bounds;
 		public BufferedImage image;
 		
 		public DrawableEntry (Rectangle bounds) {
 			this.bounds = bounds;
 		}
 		
 		void validate() {
 			if (image == null) {
 				image = ImageUtil.createCompatibleImage(bounds.width, bounds.height, Transparency.BITMASK);
 			}
 		}
 	}
 }
