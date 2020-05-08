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
 package org.eclipse.graphiti.ui.internal.parts;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.LayoutManager;
 import org.eclipse.draw2d.XYLayout;
 import org.eclipse.gef.CompoundSnapToHelper;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.SnapToGeometry;
 import org.eclipse.gef.SnapToGrid;
 import org.eclipse.gef.SnapToGuides;
 import org.eclipse.gef.SnapToHelper;
 import org.eclipse.gef.rulers.RulerProvider;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.util.ui.sfx.GFSnapFeedbackPolicy;
 
 /**
  * A GraphicalEditPart, which model is of the type ContainerShape.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ContainerShapeEditPart extends ShapeEditPart implements IContainerShapeEditPart {
 
 	private IFigure contentPaneFigureCache;
 
 	private Integer contentPaneChilds = 0;
 
 	/**
 	 * Creates a new ContainerShapeEditPart.
 	 * 
 	 * @param configurationProvider
 	 *            the configuration provider
 	 * @param containerShape
 	 *            the container shape
 	 */
 	public ContainerShapeEditPart(IConfigurationProvider configurationProvider, ContainerShape containerShape) {
 		super(configurationProvider, containerShape);
 	}
 
 	// ======================= overwriteable behaviour ========================
 
 	/**
 	 * Creates the EditPolicies of this EditPart. Subclasses often overwrite
 	 * this method to change the behaviour of the editpart. This implementation
 	 * adds the layout-specific EditPolicy to the super-implementation.
 	 * 
 	 * @see ShapeEditPart
 	 */
 	@Override
 	protected void createEditPolicies() {
 		super.createEditPolicies();
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, getConfigurationProvider().getEditPolicyFactory()
 				.createShapeXYLayoutEditPolicy());
 		// installEditPolicy(EditPolicy.CONTAINER_ROLE,
 		// getConfigurationProvider().getEditPolicyFactory().createShapeXYLayoutEditPolicy());
 		installEditPolicy("Snap Feedback", new GFSnapFeedbackPolicy()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Creates the Figure of this editpart. This determines how the editpart
 	 * will be displayed. The actual data for this figure should be provided in
 	 * refreshVisuals().
 	 * 
 	 * @return the i figure
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
 	 */
 	@Override
 	protected IFigure createFigure() {
 		resetContentPaneFigureCache();
 		IFigure figure = super.createFigure();
 		if (figure != null) {
 			if (figure.getBackgroundColor() == null && !(figure instanceof IGraphicsAlgorithmRenderer))
 				figure.setBackgroundColor(ColorConstants.lightGray);
 			figure.setOpaque(true);
 		}
 		setFigure(figure);
 		contentPaneChilds = getContentPane().getChildren().size(); // initialize
 																	// contentPaneChilds
 		return figure;
 	}
 
 	// ========================= standard behaviour ===========================
 
 	/**
 	 * Returns the children of this EditPart.
 	 * 
 	 * @return the model children
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
 	 */
 	@Override
 	public List<PictogramElement> getModelChildren() {
 		List<PictogramElement> result = new ArrayList<PictogramElement>();
 		if (getPictogramElementDelegate().isValid()) {
 			ContainerShape containerShape = (ContainerShape) getPictogramElement();
 			List<PictogramElement> activeChildren = collectActiveChildrenRecursively(containerShape);
 			result.addAll(activeChildren);
 
 			result.addAll(super.getModelChildren());
 		}
 		return result;
 	}
 
 	/**
 	 * @param containerShape
 	 * @return
 	 */
 	private List<PictogramElement> collectActiveChildrenRecursively(ContainerShape containerShape) {
 		Collection<? extends Shape> coll = containerShape.getChildren();
 		List<PictogramElement> activeChildren = new ArrayList<PictogramElement>();
 		for (Iterator<? extends Shape> iter = coll.iterator(); iter.hasNext();) {
 			Object obj = iter.next();
 			if (obj != null && obj instanceof PictogramElement) {
 				PictogramElement pe = (PictogramElement) obj;
 				if (pe.isActive()) {
 					activeChildren.add(pe);
 				} else if (pe instanceof ContainerShape) {
 					activeChildren.addAll(collectActiveChildrenRecursively((ContainerShape) pe));
 				}
 			}
 		}
 		return activeChildren;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#addChildVisual(org
 	 * .eclipse.gef.EditPart, int)
 	 */
 	@Override
 	protected void addChildVisual(EditPart childEditPart, int index) {
 		resetContentPaneFigureCache();
 		int realIndex = index + getContentPaneChildCount();
 		super.addChildVisual(childEditPart, realIndex);
 	}
 
 	private void resetContentPaneFigureCache() {
 		contentPaneFigureCache = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.ui.internal.parts.ShapeEditPart#getAdapter(java.
 	 * lang.Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (adapter == SnapToHelper.class) {
 			List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
 			Boolean val = (Boolean) getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
 			if (val != null && val.booleanValue())
 				snapStrategies.add(new SnapToGuides(this));
 			val = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
 			if (val != null && val.booleanValue())
 				snapStrategies.add(new SnapToGeometry(this));
 			val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
 			if (val != null && val.booleanValue())
 				snapStrategies.add(new SnapToGrid(this));
 
 			if (snapStrategies.size() == 0)
 				return null;
 			if (snapStrategies.size() == 1)
 				return snapStrategies.get(0);
 
 			SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
 			for (int i = 0; i < snapStrategies.size(); i++)
 				ss[i] = snapStrategies.get(i);
 			return new CompoundSnapToHelper(ss);
 		}
 		return super.getAdapter(adapter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getContentPane()
 	 */
 	@Override
 	public IFigure getContentPane() {
 		if (getContentPaneFigureCache() != null) {
 			return getContentPaneFigureCache();
 		}
 		IToolBehaviorProvider tbp = getConfigurationProvider().getDiagramTypeProvider()
 				.getCurrentToolBehaviorProvider();
 		PictogramElement pe = getPictogramElement();
 		if (pe instanceof ContainerShape && !(this instanceof DiagramEditPart)) {
 			ContainerShape cs = (ContainerShape) pe;
 			GraphicsAlgorithm contentGa = tbp.getContentArea(cs);
 			if (contentGa != null) {
 				setConentPaneFigureCache(getPictogramElementDelegate().getFigureForGraphicsAlgorithm(contentGa));
 				if (getContentPaneFigureCache() != null) {
 					LayoutManager lm = getContentPaneFigureCache().getLayoutManager();
 					if (!(lm instanceof XYLayout)) {
 						getContentPaneFigureCache().setLayoutManager(new XYLayout());
 					}
 					return getContentPaneFigureCache();
 				}
 			}
 		}
 
 		IFigure ret = super.getContentPane();
 		return ret;
 	}
 
 	private void setConentPaneFigureCache(IFigure figure) {
 		contentPaneFigureCache = figure;
 	}
 
 	private IFigure getContentPaneFigureCache() {
 		return contentPaneFigureCache;
 	}
 
 	private Integer getContentPaneChildCount() {
 		return contentPaneChilds;
 	}
 }
