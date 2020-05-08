 /*******************************************************************************
  * Copyright (c) 2009 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Kristian Duske - initial API and implementation
  ******************************************************************************/
 package org.eclipse.draw3d.ui.viewer;
 
 import org.eclipse.draw3d.IFigure3D;
 import org.eclipse.draw3d.ShapeFigure3D;
 import org.eclipse.draw3d.geometry.Vector3fImpl;
 import org.eclipse.draw3d.shapes.CompositeShape;
 import org.eclipse.draw3d.shapes.CuboidShape;
 import org.eclipse.draw3d.shapes.ParaxialBoundsFigureShape;
 import org.eclipse.draw3d.shapes.Shape;
 import org.eclipse.draw3d.shapes.TransparentShape;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * ShapeViewer There should really be more documentation here.
  * 
  * @author Kristian Duske
  * @version $Revision$
  * @since 03.06.2009
  */
 public class ShapeViewer extends Draw3DViewer {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.draw3d.ui.viewer.Draw3DViewer#createContents()
 	 */
 	@Override
 	protected IFigure3D createContents() {
 
 		IFigure3D figure = new ShapeFigure3D() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.draw3d.ShapeFigure3D#createShape()
 			 */
 			@Override
 			protected Shape createShape() {
 
 				CompositeShape composite = new CompositeShape();
 				// composite.addOpaque(new CylinderShape(this.getPosition3D(),
 				// 12,
 				// 0.3f));
 
 				composite.addOpaque(new CuboidShape(this.getPosition3D()));
 
 				ParaxialBoundsFigureShape pbShape =
 					new ParaxialBoundsFigureShape(this);
 
				composite.addTransparent(pbShape);
 
 				return composite;
 				// return new SphereShape(this.getPosition3D(), 4);
 			}
 		};
 
 		figure.getPosition3D().setLocation3D(new Vector3fImpl(0, 0, 0));
 		figure.getPosition3D().setSize3D(new Vector3fImpl(200, 300, 100));
 		figure.getPosition3D().setRotation3D(new Vector3fImpl(20, 0, 0));
 
 		Device dev = Display.getCurrent();
 		figure.setBackgroundColor(dev.getSystemColor(SWT.COLOR_DARK_BLUE));
 		figure.setForegroundColor(dev.getSystemColor(SWT.COLOR_CYAN));
 
 		return figure;
 	}
 }
