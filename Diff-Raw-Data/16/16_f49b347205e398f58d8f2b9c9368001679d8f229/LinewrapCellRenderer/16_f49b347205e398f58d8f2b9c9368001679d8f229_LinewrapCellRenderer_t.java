 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.presentation.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.IProrCellRenderer;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 
 public class LinewrapCellRenderer implements IProrCellRenderer {
 
 	private Map<AttributeValue, LineWrapObject> cache = new HashMap<AttributeValue, LineWrapObject>();
 	
 	public int doDrawCellContent(GC gc, Rectangle rect, Object value) {
 		String text = "";
 		int textExtent = 15;
 		LineWrapObject lineWrapObject = getLineWrapObject(value, gc, rect.width);
 		if (lineWrapObject != null) {
 			text = lineWrapObject.text;
 			textExtent = lineWrapObject.textExtent.y;
 		}
 		gc.drawText(text, rect.x + 1, rect.y + 1);
 		return textExtent + 2;
 	}
 
 	/**
 	 * This method performs the line-breaking and can be used if new formatting
 	 * is provided (see {@link #formatCell(ViewerCell)}).
 	 */
 	protected String getText(Object object, GC gc, int width) {
 		LineWrapObject lineWrapObject = getLineWrapObject(object, gc, width);
 		if (lineWrapObject != null)
 			return lineWrapObject.text;
 		return "";
 	}
 
 	protected LineWrapObject getLineWrapObject(Object object, GC gc, int width) {
 
 		LineWrapObject lo = null;
 
 		if (object instanceof AttributeValueString) {
 			AttributeValueString av = (AttributeValueString) object;
 			String theValue = av.getTheValue();
 			lo = cache.get(av);
 			if (lo == null) {
 				String text = getLineWrapText(theValue, gc, width);
				lo = new LineWrapObject(theValue, text, width,
						gc.textExtent(text));
 				cache.put(av, lo);
			} else if (width != lo.width
					|| (theValue.compareTo(lo.origText) != 0)) {
 				String text = getLineWrapText(theValue, gc, width);
 				lo.textExtent = gc.textExtent(text);
 				lo.text = text;
 				lo.width = width;
				lo.origText = theValue;
 			}
 		}
 
 		return lo;
 
 	}
 	
 	private String getLineWrapText(String text, GC gc, int width) {
 
 		if(text == null)
 			return "";
 		
 		// Insert line breaks into the text
 		width = width - 2;
 		if (width <= 0)
 			width = 1;
 
 		StringBuilder sb = new StringBuilder();
 		String[] pars = text.split("\\r?\\n");
 
 		// Iterate over paragraphs
 		for (String par : pars) {
 			// Empty paragraph
 			if (par.length() == 0) {
 				sb.append("\n");
 				continue;
 			}
 
 			while (par.length() > 0) {
 				String line = "";
 				int lastValidPos = -1;
 
 				// Repeat until we exceed the width
 				while (gc.stringExtent(line).x < width) {
 					if (lastValidPos == line.length())
 						break;
 					lastValidPos = line.length();
 					int nextWS = par.indexOf(" ", lastValidPos + 1);
 					if (nextWS == -1) {
 						// We are done with the paragraph!
 						line = par;
 						nextWS = line.length();
 					}
 					line = par.substring(0, nextWS);
 				}
 
 				// If no position is found, then the current word is too
 				// long for the cell: We let it clip.
 				if (lastValidPos <= 0)
 					lastValidPos = line.length();
 				// We have a valid length. Add it and shrink par
 				line = par.substring(0, lastValidPos);
 				par = par.substring(lastValidPos, par.length()).trim();
 				sb.append(line + "\n");
 			}
 		}
 
 		// Remove the extra trailing LF
 		if (sb.length() > 0)
 			sb.deleteCharAt(sb.length() - 1);
 
 		return sb.toString();
 
 	}
 	
 	public String doDrawHtmlContent(AttributeValue value) {
 		AttributeValueString av = (AttributeValueString) value;
 		String text = av.getTheValue();
 		if (text != null)
 			text = text.replaceAll("\n", "<br>");
 		else
 			text = "";
 		return text;
 	}
 
 	public class LineWrapObject {
 
		public String origText;
 		public String text;
 		public int width;
 		public Point textExtent;
 
		public LineWrapObject(String origText, String text, int width,
				Point textExtent) {
			this.origText = origText;
 			this.text = text;
 			this.width = width;
 			this.textExtent = textExtent;
 		}
 
 	}
 	
 }
