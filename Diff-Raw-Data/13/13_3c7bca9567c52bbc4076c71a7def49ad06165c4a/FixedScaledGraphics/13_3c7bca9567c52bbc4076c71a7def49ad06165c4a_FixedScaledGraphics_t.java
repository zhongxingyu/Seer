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
 package org.eclipse.graphiti.ui.internal.fixed;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.ScaledGraphics;
 import org.eclipse.swt.graphics.Path;
 
 /**
  * This class is a subclass of ScaledGraphics, which only purpose is to fix bugs
  * in ScaledGraphics or implement previously not implemented methods. This class
  * does not add any GFW specific coding.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class FixedScaledGraphics extends ScaledGraphics {
 
 	private Graphics graphics;
 
 	/**
 	 * Instantiates a new fixed scaled graphics.
 	 * 
 	 * @param graphics
 	 *            the graphics
 	 */
 	public FixedScaledGraphics(Graphics graphics) {
 		super(graphics);
 		this.graphics = graphics;
 	}
 
 	// ========================================================================
 	// ===================== overwritten changed methods ======================
 	// ========================================================================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.ScaledGraphics#fillGradient(int, int, int, int,
 	 * boolean)
 	 */
 	@Override
 	// Do not zoom anymore
 	// Just forward to the wrapped Graphics
 	public void fillGradient(int x, int y, int w, int h, boolean vertical) {
 		graphics.fillGradient(x, y, w, h, vertical);
 	}
 
 	// ========================================================================
 	// ===================== overwritten missing methods ======================
 	// ========================================================================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.Graphics#rotate(float)
 	 */
 	@Override
 	// Just forward to the wrapped Graphics
 	public void rotate(float degrees) {
 		graphics.rotate(degrees);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.Graphics#translate(float, float)
 	 */
 	@Override
 	// Just forward to translate(int, int)
 	public void translate(float dx, float dy) {
 		graphics.translate(Math.round(dx), Math.round(dy));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.Graphics#drawPath(org.eclipse.swt.graphics.Path)
 	 */
 	@Override
 	// Just forward to the wrapped Graphics
 	public void drawPath(Path path) {
 		graphics.drawPath(path);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.Graphics#fillPath(org.eclipse.swt.graphics.Path)
 	 */
 	@Override
 	// Just forward to the wrapped Graphics
 	public void fillPath(Path path) {
 		graphics.fillPath(path);
 	}
 
	public void clipPath(Path path) {
			graphics.clipPath(path);
	}

 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.Graphics#setClip(org.eclipse.swt.graphics.Path)
 	 */
 	@Override
 	// Just forward to the wrapped Graphics
 	public void setClip(Path path) {
 		graphics.setClip(path);
 	}
 }
