 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.util.draw2d;
 
 import org.eclipse.draw2d.ChopboxAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.graphiti.ui.internal.services.impl.GefService;
 
 /**
  * The purpose of this class is to fix bugs in the {@link ChopboxAnchor} by
  * overwriting the erroneous methods.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ChopboxAnchorFixed extends ChopboxAnchor {
 
 	public ChopboxAnchorFixed(IFigure figure) {
 		super(figure);
 	}
 
 	/**
 	 * CHANGED: if the reference is in the center of the figure, the result was
 	 * not correct. This method has to be kept in sync with
 	 * {@link GefService#getChopboxLocationOnBox(Point, Rectangle)}.
 	 */
 	@Override
 	public Point getLocation(Point reference) {

		Rectangle box = getBox();
		if (box.width() == 1 && box.height == 1) {
			Rectangle r = Rectangle.SINGLETON;
			r.setBounds(box);
			getOwner().translateToAbsolute(r); // consider zoom etc.
			Point point = new Point(r.x(), r.y());
			return point;
		}

 		Rectangle r = Rectangle.SINGLETON;
 		r.setBounds(getBox());
 		r.translate(-1, -1);
 		r.resize(1, 1);
 
 		getOwner().translateToAbsolute(r);
 		float centerX = r.x + 0.5f * r.width;
 		float centerY = r.y + 0.5f * r.height;
 
 		// CHANGED: returning the center in case of a divide-by-zero is not a
 		// good result
 		// if (r.isEmpty() || (reference.x == (int)centerX && reference.y ==
 		// (int)centerY))
 		// return new Point((int)centerX, (int)centerY); //This avoids
 		// divide-by-zero
 
 		float dx = reference.x - centerX;
 		float dy = reference.y - centerY;
 
 		// r.width, r.height, dx, and dy are guaranteed to be non-zero.
 
 		// CHANGED: in case of "nearly zero" (divide-by-zero or
 		// rounding-problems) would happen.
 		// Instead return a point on the border of the figure.
 		// Doesn't matter which one, because it is directly in the center, so
 		// take top-middle.
 		float max = Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);
 		if (max <= 0.001f) {
 			return new Point((int) centerX, r.y);
 		}
 
 		float scale = 0.5f / max;
 
 		dx *= scale;
 		dy *= scale;
 		centerX += dx;
 		centerY += dy;
 
 		return new Point(Math.round(centerX), Math.round(centerY));
 	}
 }
