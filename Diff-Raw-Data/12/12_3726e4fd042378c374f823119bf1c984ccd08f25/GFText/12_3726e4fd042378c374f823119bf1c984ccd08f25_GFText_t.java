 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mgorning - Bug 365536 - Using BoxRelativeAnchor with relativeWidth = 1 display ellipsis in related Text
  *    mgorning - Bug 374190 - Vertically aligned text disappears after the height of TextImpl is increased
 *    mgorning - Bug 368124 - ConnectionDecorator with Text causes problems
 *    mwenz - Bug 405920 - Text background color is ignored on rotated text
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.figures;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.RotatableDecoration;
 import org.eclipse.draw2d.ScaledGraphics;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Insets;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProviderInternal;
 import org.eclipse.graphiti.ui.internal.parts.IPictogramElementDelegate;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GFText extends Label implements RotatableDecoration {
 	private Text text;
 
 	private int labelAlignment = CENTER;
 
 	private String subStringText;
 
 	private Dimension myPrefSize;
 
 	// rotation angle if text is used as an passive connection decorator
 	private double rotationAngle = 0d;
 
 	private IConfigurationProviderInternal configurationProvider;
 
 	public GFText(IPictogramElementDelegate pictogramElementDelegate, Text text) {
 		this.text = text;
 		configurationProvider = pictogramElementDelegate.getConfigurationProvider();
 	}
 
 	@Override
 	public int getLabelAlignment() {
 		return labelAlignment;
 	}
 
 	@Override
 	public void paintFigure(Graphics graphics) {
 		if (text != null && GraphitiInternal.getEmfService().isObjectAlive(text)) {
 			double transparency = Graphiti.getGaService().getTransparency(text, true);
 			int alpha = (int) ((1.0 - transparency) * 255.0);
 			graphics.setAlpha(alpha);
 
 			// Only use antialias for draw 2d rendering, for svg rendering we do
 			// not support this option
 			if (graphics instanceof ScaledGraphics)
 				graphics.setTextAntialias(SWT.ON);
 
 			if (rotationAngle != 0 && text.eContainer() instanceof ConnectionDecorator
 					&& !((ConnectionDecorator) text.eContainer()).isActive()) {
 				Rectangle rect = new Rectangle();
 				graphics.getClip(rect);
 				graphics.pushState();
 				Rectangle bounds = getBounds();
 				graphics.translate(bounds.x, bounds.y); // TODO caluclate
 				// the offset to x
 				// and y based on
 				// angle
 				graphics.rotate((float) rotationAngle);
 				rect = new Rectangle(0, 0, 5000, 5000); // TODO calculate the
 				// real clip rectangle
 				// from the angle
 				graphics.setClip(rect);
 				if (text.getStyleRegions().isEmpty()) {
 					graphics.drawText(getSubStringText(), getTextLocation());
 				} else {
 					GFFigureUtil.drawRichText(graphics, getSubStringText(), getTextLocation().x(), getTextLocation()
 							.y(), configurationProvider, text);
 				}
 				graphics.popState();
 				return;
 			}
 
 			int angle = 0;
 			angle = Graphiti.getGaService().getAngle(text, true);
 
 			if (angle != 0) {
				// Fix for Bug 405920. Set the background color for rotated
				// texts on basis of the original rectangle. Needs to be
				// reworked as part of the general rework of this class with Bug
				// 375922
				if (isOpaque()) {
					graphics.fillRectangle(getBounds());
				}

 				Rectangle rect = new Rectangle();
 				graphics.getClip(rect);
 				graphics.pushState();
 				Rectangle bounds = getBounds();
 				int w = bounds.width;
 				int h = bounds.height;
 				bounds.height = w;
 				bounds.width = h;
 				graphics.translate(bounds.x, bounds.y + h); // TODO caluclate
 				// the offset to x
 				// and y based on
 				// angle
 				graphics.rotate(angle);
 				rect = new Rectangle(0, 0, 5000, 5000); // TODO calculate the
 				// real clip rectangle
 				// from the angle
 				graphics.setClip(rect);
 				if (text.getStyleRegions().isEmpty()) {
 					graphics.drawText(getSubStringText(), getTextLocation());
 				} else {
 					GFFigureUtil.drawRichText(graphics, getSubStringText(), getTextLocation().x(), getTextLocation()
 							.y(), configurationProvider, text);
 				}
 
 				bounds.height = h;
 				bounds.width = w;
 
 				graphics.popState();
 				return;
 
 			}
 		}
 
 		if (text.getStyleRegions().isEmpty()) {
 			super.paintFigure(graphics);
 		} else {
 			if (isOpaque())
 				super.paintFigure(graphics);
 			Rectangle bounds = getBounds();
 			graphics.translate(bounds.x, bounds.y);
 
 			GFFigureUtil.drawRichText(graphics, getSubStringText(), getTextLocation().x(), getTextLocation().y(),
 					configurationProvider, text);
 
 			graphics.translate(-bounds.x, -bounds.y);
 		}
 	}
 
 	@Override
 	public void setLabelAlignment(int align) {
 		super.setLabelAlignment(align);
 		labelAlignment = align;
 	}
 
 	@Override
 	public String getSubStringText() {
 		if (subStringText != null)
 			return subStringText;
 
 		subStringText = getText();
 		int widthShrink = getPreferredSizeWithoutChilds().width - getSize().width;
 		if (widthShrink <= 0)
 			return subStringText;
 
 		Dimension effectiveSize = getTextSize().getExpanded(-widthShrink, 0);
 		Font currentFont = getFont();
 		int dotsWidth = getTextUtilities().getTextExtents(getTruncationString(), currentFont).width;
 
 		if (effectiveSize.width < dotsWidth)
 			effectiveSize.width = dotsWidth;
 
 		int subStringLength = getTextUtilities().getLargestSubstringConfinedTo(getText(), currentFont,
 				effectiveSize.width - dotsWidth);
 		subStringText = new String(getText().substring(0, subStringLength) + getTruncationString());
 		return subStringText;
 	}
 
 	protected Dimension getPreferredSizeWithoutChilds() {
 		if (myPrefSize == null) {
 			myPrefSize = calculateLabelSize(getTextSize());
 			Insets insets = getInsets();
 			myPrefSize.expand(insets.getWidth(), insets.getHeight());
 		}
 		return myPrefSize;
 	}
 
 	@Override
 	public void invalidate() {
 		subStringText = null;
 		myPrefSize = null;
 		super.invalidate();
 	}
 
 	public void setReferencePoint(Point p) {
 		Point tempRect = Point.SINGLETON.setLocation(p);
 		tempRect.negate().translate(getLocation());
 		rotationAngle = Math.toDegrees((Math.atan2(tempRect.y, tempRect.x))) - 180d;
 	}
 
 	public GraphicsAlgorithm getGraphicsAlgorithm() {
 		return text;
 	}
 }
