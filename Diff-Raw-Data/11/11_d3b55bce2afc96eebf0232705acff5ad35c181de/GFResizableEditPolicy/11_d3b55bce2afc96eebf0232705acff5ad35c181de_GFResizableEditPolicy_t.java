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
  *    jpasch - BUG 341180: Graphiti fails to handle resize after custom feature addition in the tutorial
  *    Patch 185019 from Bug 332360 contributed by Volker Wegert
  *    mwenz - Bug 346487 - No selection feedback for non-resizable diagram nodes
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.policy;
 
 import java.util.List;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.editpolicies.ResizableEditPolicy;
 import org.eclipse.graphiti.features.IResizeConfiguration;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.util.draw2d.GFHandleHelper;
 import org.eclipse.graphiti.ui.internal.util.draw2d.TransparentGhostFigure;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class GFResizableEditPolicy extends ResizableEditPolicy {
 	private GFEditPolicyDelegate delegate;
 	private IResizeShapeContext resizeShapeContext;
 
 	public GFResizableEditPolicy(IConfigurationProvider cfgProvider) {
 		setDelegate(new GFEditPolicyDelegate(cfgProvider));
 	}
 
 	public GFResizableEditPolicy(IConfigurationProvider configurationProvider, IResizeShapeContext resizeShapeContext) {
 		this(configurationProvider);
 		setResizeShapeContext(resizeShapeContext);
 	}
 
 	@Override
 	protected IFigure createDragSourceFeedbackFigure() {
 		IFigure feedback = new TransparentGhostFigure(getHostFigure(), 70, getConfigurationProvider()
 				.getDiagramEditor().getZoomLevel());
 		addFeedback(feedback);
 		return feedback;
 	}
 
 	@Override
 	protected List<?> createSelectionHandles() {
		boolean resizeAllowed = false;
		if (getResizeShapeContext() != null) {
			resizeAllowed = !(getResizeShapeFeature() == null || !getResizeShapeFeature().canResizeShape(
					getResizeShapeContext()));
		}
 
 		GraphicalEditPart owner = (GraphicalEditPart) getHost();
 		List<?> list = GFHandleHelper.createShapeHandles(owner, getConfigurationProvider(), getResizeDirections(),
 				isDragAllowed(), resizeAllowed);
 		return list;
 	}
 
 	@Override
 	public void eraseSourceFeedback(Request request) {
 		/* dispose ghosts */
 		@SuppressWarnings("unchecked")
 		List<IFigure> children = getFeedbackLayer().getChildren();
 		for (IFigure child : children) {
 			if (child instanceof TransparentGhostFigure) {
 				((TransparentGhostFigure) child).dispose();
 			}
 		}
 		super.eraseSourceFeedback(request);
 	}
 
 	protected IConfigurationProvider getConfigurationProvider() {
 		return getDelegate().getConfigurationProvider();
 	}
 
 	private GFEditPolicyDelegate getDelegate() {
 		return this.delegate;
 	}
 
 	@Override
 	public int getResizeDirections() {
 		int ret = 0;
 		if (!(getResizeShapeContext() == null)) {
 			IResizeShapeFeature resizeShapeFeature = getResizeShapeFeature();
 			if (resizeShapeFeature != null) {
 				IResizeConfiguration resizeConfiguration = resizeShapeFeature
 						.getResizeConfiguration(getResizeShapeContext());
 				if (resizeConfiguration.isHorizontalResizeAllowed()) {
 					ret = ret | PositionConstants.EAST_WEST;
 				}
 				if (resizeConfiguration.isVerticalResizeAllowed()) {
 					ret = ret | PositionConstants.NORTH_SOUTH;
 				}
 			}
 		}
 		return ret;
 	}
 
 	private IResizeShapeContext getResizeShapeContext() {
 		return this.resizeShapeContext;
 	}
 
 	private IResizeShapeFeature getResizeShapeFeature() {
		if (getResizeShapeContext() == null) {
			return null;
		}
 		return getConfigurationProvider().getFeatureProvider().getResizeShapeFeature(getResizeShapeContext());
 	}
 
 	private void setDelegate(GFEditPolicyDelegate delegate) {
 		this.delegate = delegate;
 	}
 
 	private void setResizeShapeContext(IResizeShapeContext resizeShapeContext) {
 		this.resizeShapeContext = resizeShapeContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#hideSelection()
 	 */
 	@Override
 	protected void hideSelection() {
 		getDelegate().hideSelection(getHostFigure());
 		removeSelectionHandles();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editpolicies.SelectionEditPolicy#showPrimarySelection()
 	 */
 	@Override
 	protected void showPrimarySelection() {
 		if (GraphitiInternal.getEmfService().isObjectAlive((PictogramElement) getHost().getModel())) {
 			getDelegate().showPrimarySelection(getHostFigure());
 			addSelectionHandles();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#showSelection()
 	 */
 	@Override
 	protected void showSelection() {
 		getDelegate().showSelection(getHostFigure());
 		addSelectionHandles();
 	}
 }
