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
 
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.AttributeValueString;
 import org.eclipse.rmf.reqif10.pror.editor.presentation.service.IProrCellRenderer;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 
 public class LinewrapCellRenderer implements IProrCellRenderer {
 
 	public int doDrawCellContent(GC gc, Rectangle rect, Object value) {
 		String text = getText(value, gc, rect.width);
 		gc.drawText(text, rect.x + 1, rect.y + 1);
 		return gc.textExtent(text).y + 2;
 	}
 
 	/**
 	 * This method performs the line-breaking and can be used if new formatting
 	 * is provided (see {@link #formatCell(ViewerCell)}).
 	 */
 	protected String getText(Object object, GC gc, int width) {
 		if (!(object instanceof AttributeValueString)) {
 			return "";
 		}
 
 		AttributeValueString av = (AttributeValueString) object;
 		String text = av.getTheValue();
 		if (text == null) {
 			return "";
 		}
 
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
 
 }
