 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.css2.widget;
 
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.jst.pagedesigner.css2.provider.DimensionInfo;
 import org.eclipse.jst.pagedesigner.css2.style.HiddenElementStyle;
 import org.eclipse.jst.pagedesigner.parts.EditProxyAdapter;
 import org.eclipse.jst.pagedesigner.parts.ElementEditPart;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.w3c.dom.Element;
 
 /**
  * @author mengbo
  */
 public class HiddenProvider extends ImageWidgetProvider {
 	private final static int GAP = 3;
 
 	private String _label = null;
 
 	private boolean _labelVisible = true;
 
 	private FontMetrics _fontMetrics;
     
 	/**
 	 * @param image
 	 * @param style
 	 */
 	public HiddenProvider(Image image, Element convertedElement) {
 		super(image, new HiddenElementStyle(convertedElement));
 	}
 
 	public HiddenProvider(Image image, ElementEditPart editPart) {
 		super(image, new HiddenElementStyle(new EditProxyAdapter(editPart)));
 	}
 
 	// public HiddenProvider(Image image, String label)
 	// {
 	// this(image);
 	// this._label = label;
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.provider.ICSSWidgetProvider#getPreferredDimension(int,
 	 *      int)
 	 */
 	public DimensionInfo getPreferredDimension(int width, int height) {
 		int preWidth = width;
 		int preHeight = height;
 		_fontMetrics = getFontMetrics();
 		if (width <= 0) {
 			preWidth = getLabelWidth() + _imageWidth;
 		}
 		if (height <= 0) {
 			preHeight = Math.max(getLabelHeight(), _imageHeight);
 		}
 		return new DimensionInfo(preWidth, preHeight, -1);
 	}
 
 	private FontMetrics getFontMetrics() {
 		Font swtfont = Display.getCurrent().getSystemFont();
 		return FigureUtilities.getFontMetrics(swtfont);
 	}
 
 	private int getLabelHeight() {
 		if (_labelVisible && (_label != null) && (!_label.equals(""))
 				&& (_fontMetrics != null)) {
 			return _fontMetrics.getHeight();
 		}
 		return 0;
 	}
 
 	public int getLabelWidth() {
 		if (_labelVisible && _label != null && !_label.equals("")
 				&& _fontMetrics != null) {
 			Font swtfont = Display.getCurrent().getSystemFont();
 			return FigureUtilities.getTextWidth(_label, swtfont) + GAP;
 		}
 		return 0;
 	}
 
 	public void paintFigure(Graphics g,
 			org.eclipse.draw2d.geometry.Rectangle rect) {
 		g.fillRectangle(rect);
 		g.setClip(rect);
 		int imageAreaWidth = Math.min(rect.width, _imageWidth);
 		int imageAreaHeight = Math.min(rect.height, _imageHeight);
		if (_image != null) {
 			g.drawImage(_image, 0, 0, _imageWidth, _imageHeight, rect.x, rect.y
 					+ (rect.height - imageAreaHeight) / 2, imageAreaWidth,
 					imageAreaHeight);
 		}
 		if (_label != null && _labelVisible) {
 			int leading = 0;
 			if (_fontMetrics != null) {
 				leading = _fontMetrics.getLeading();
 			}
             // TODO: adapt to bg: go lighter on colors that darker() would make black
 			// TODO: color registry
             Color fg = FigureUtilities.darker(g.getBackgroundColor());
             g.setForegroundColor(fg);
             g.drawString(_label, imageAreaWidth + GAP, rect.y
 					+ (rect.height - getLabelHeight()) / 2 + leading);
             fg.dispose();
 		}
 	}
 
 	public String getLabel() {
 		return _label;
 	}
 
 	public void setLabel(String label) {
 		this._label = label;
 	}
 
 	public boolean isLabelVisible() {
 		return _labelVisible;
 	}
 
 	public void setLabelVisible(boolean labelVisible) {
 		this._labelVisible = labelVisible;
 	}
 }
