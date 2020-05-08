 /*******************************************************************************
  * Copyright (c) 2008 Kristian Duske and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Kristian Duske - initial API and implementation
  ******************************************************************************/
 package org.eclipse.gef3d.examples.ecore.figures;
 
 import org.eclipse.draw2d.FreeformLayout;
 import org.eclipse.draw3d.FigureSurface;
 import org.eclipse.draw3d.ISurface;
 import org.eclipse.draw3d.ShapeFigure3D;
 import org.eclipse.draw3d.shapes.CuboidFigureShape;
 import org.eclipse.draw3d.shapes.Shape;
 import org.eclipse.draw3d.shapes.TransparentShape;
 
 /**
  * A 3D figure that represents a diagram. The diagram elements are drawn on the
  * figure's surface.
  * 
  * @author Kristian Duske
  * @version $Revision$
  * @since 08.04.2009
  */
 public class DiagramFigure3D extends ShapeFigure3D {
 
 	protected int headerStyle;
 
 	/**
 	 * The surface of this figure. This is where 2D children are placed.
 	 */
 	private ISurface m_surface = new FigureSurface(this);
 
 	public DiagramFigure3D() {
 
 		setLayoutManager(new FreeformLayout());
 	}
 
 	/**
 	 * Returns a transparent cuboid figure shape (i.e. a
 	 * {@link CuboidFigureShape} nested into a {@link TransparentShape}.
 	 * 
 	 * @see org.eclipse.draw3d.ShapeFigure3D#createShape()
 	 */
 	@Override
 	protected Shape createShape() {
		Shape shape = new CuboidFigureShape(this);
		return new TransparentShape(this, shape);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.draw3d.Figure3D#getSurface()
 	 */
 	@Override
 	public ISurface getSurface() {
 
 		return m_surface;
 	}
 }
