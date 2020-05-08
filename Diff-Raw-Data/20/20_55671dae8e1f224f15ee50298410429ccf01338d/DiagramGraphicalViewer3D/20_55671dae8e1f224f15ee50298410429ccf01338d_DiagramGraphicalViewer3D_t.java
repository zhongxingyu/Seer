 /*******************************************************************************
 * Copyright (c) 2009 Kristian Duske and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *    Kristian Duske - initial API and implementation
  ******************************************************************************/
 package org.eclipse.gef3d.gmf.runtime.diagram.ui.parts;
 
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.draw2d.UpdateManager;
 import org.eclipse.draw3d.Draw3DCanvas;
 import org.eclipse.draw3d.LightweightSystem3D;
 import org.eclipse.draw3d.camera.ICamera;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef3d.ui.parts.GraphicalViewer3D;
 import org.eclipse.gef3d.ui.parts.IScene;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.opengl.GLCanvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 /**
  * 3D diagram graphical viewer.
  * 
  * @author Kristian Duske
  * @version $Revision$
  * @since Apr 7, 2009
  */
 public class DiagramGraphicalViewer3D extends DiagramGraphicalViewer implements
 		GraphicalViewer3D, IScene {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef.ui.parts.ScrollingGraphicalViewer#reveal(org.eclipse.gef.EditPart)
 	 */
 	@Override
 	public void reveal(EditPart i_part) {
 
 		// TODO: implement this properly
 	}
 
 	/**
 	 * Creates a new GL canvas, sets it as this viewer's control and returns it.
 	 * 
 	 * @param i_composite the parent composite
 	 * @return the GL canvas
 	 */
 	public Control createControl3D(Composite i_composite) {
 
 		GLCanvas canvas = Draw3DCanvas.createCanvas(i_composite, SWT.NONE,
 				getLightweightSystem3D());
 
 		setControl(canvas);
 		return getControl();
 	}
 	
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer#createLightweightSystem()
 	 */
 	@Override
 	protected LightweightSystem createLightweightSystem() {
 
 		return new LightweightSystem3D();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef3d.ui.parts.IScene#getCamera()
 	 */
 	public ICamera getCamera() {
 
 		LightweightSystem3D lightweightSystem3D = getLightweightSystem3D();
 		if (lightweightSystem3D == null)
 			return null;
 
 		return lightweightSystem3D.getCamera();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef.ui.parts.ScrollingGraphicalViewer#getFigureCanvas()
 	 */
 	@Override
 	protected FigureCanvas getFigureCanvas() {
 
 		return null;
 	}
 
 	/**
 	 * Returns the 3D lightweight system if there is one.
 	 * 
 	 * @return the 3D lightweight system or <code>null</code> if the current
 	 *         lightweight system is not 3D
 	 * @see org.eclipse.gef3d.ui.parts.GraphicalViewer3D#getLightweightSystem3D()
 	 */
 	public LightweightSystem3D getLightweightSystem3D() {
 
 		LightweightSystem lws = getLightweightSystem();
 		if (!(lws instanceof LightweightSystem3D))
 			return null;
 
 		return (LightweightSystem3D) lws;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef3d.ui.parts.IScene#getUpdateManager()
 	 */
 	public UpdateManager getUpdateManager() {
 
 		return getLightweightSystem().getUpdateManager();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef3d.ui.parts.IScene#render()
 	 */
 	public void render() {
 
 		// nothing to do
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef3d.ui.parts.IScene#setBackgroundColor(org.eclipse.swt.graphics.Color)
 	 */
 	public void setBackgroundColor(Color i_backgroundColor) {
 
 		LightweightSystem3D lightweightSystem3D = getLightweightSystem3D();
 		if (lightweightSystem3D == null)
 			return;
 
 		lightweightSystem3D.setBackgroundColor(i_backgroundColor);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef3d.ui.parts.IScene#setCamera(org.eclipse.draw3d.camera.ICamera)
 	 */
 	public void setCamera(ICamera i_camera) {
 
 		LightweightSystem3D lightweightSystem3D = getLightweightSystem3D();
 		if (lightweightSystem3D == null)
 			return;
 
 		lightweightSystem3D.setCamera(i_camera);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef3d.ui.parts.IScene#setDrawAxes(boolean)
 	 */
 	public void setDrawAxes(boolean i_drawAxes) {
 
 		LightweightSystem3D lightweightSystem3D = getLightweightSystem3D();
 		if (lightweightSystem3D == null)
 			return;
 
 		lightweightSystem3D.setDrawAxes(i_drawAxes);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef.ui.parts.ScrollingGraphicalViewer#setRootFigure(org.eclipse.draw2d.IFigure)
 	 */
 	@Override
 	protected void setRootFigure(IFigure i_figure) {
 
 		super.setRootFigure(i_figure);
 		getLightweightSystem().setContents(i_figure);
 	}
 }
