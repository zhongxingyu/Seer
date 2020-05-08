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
 package org.eclipse.rmf.pror.presentation.headline.ui;
 
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.service.IProrCellRenderer;
 import org.eclipse.rmf.reqif10.AttributeValueSimple;
 import org.eclipse.rmf.reqif10.util.ReqIF10Util;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 
 public class HeadlineCellRenderer implements IProrCellRenderer {
 
	private String PROR_HEADLINE_FONT = "pror_headline_font-";
 	private Font font;
 	private int fontSize;
 
 	public HeadlineCellRenderer(String identifier) {
		this.PROR_HEADLINE_FONT = "pror_headline_font-" + identifier;
 	}
 
 	public void setDatatypeId(String identifier) {
		this.PROR_HEADLINE_FONT = "pror_headline_font-" + identifier;
 		setFontSize(fontSize);
 
 	}
 
 	public void setFontSize(final int fontSize) {
 		this.fontSize = fontSize;
 		// TODO: commented out, since maven java invoke can not handle UI
 		// PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 		// public void run() {
		FontRegistry fr = JFaceResources.getFontRegistry();
 		FontData[] fontData = { new FontData("Arial", fontSize, SWT.BOLD) };
 		if (font != null)
 			font.dispose();
		fr.put(PROR_HEADLINE_FONT, fontData);
		font = fr.get(PROR_HEADLINE_FONT);
 		// }
 		// });
 	}
 
 	public int doDrawCellContent(GC gc, Rectangle rect, Object value) {
 		AttributeValueSimple av = (AttributeValueSimple) value;
 		String text = " ";
 		if (av != null && ReqIF10Util.getTheValue(av) != null) {
 			text = ReqIF10Util.getTheValue(av).toString();
 		}
 		gc.setFont(font);
 		gc.drawText(text, rect.x, rect.y);
 		return gc.textExtent(text).y;
 	}
 
 	public String doDrawHtmlContent(Object value) {
 		AttributeValueSimple av = (AttributeValueSimple) value;
 		return "<div style='font-size: " + fontSize
 				+ "pt; font-weight: bold; padding-top: 4pt;'>"
 				+ ReqIF10Util.getTheValue(av) + "</div>";
 	}
 
 }
 
