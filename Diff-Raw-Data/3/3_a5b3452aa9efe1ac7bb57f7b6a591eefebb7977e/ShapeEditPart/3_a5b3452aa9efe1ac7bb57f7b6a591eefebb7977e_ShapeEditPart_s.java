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
  *    mwenz - Bug 327756 - Cancelled double click feature marked editor dirty
  *    Bug 336488 - DiagramEditor API
  *    mgorning - Bug 347262 - DirectEditingFeature with TYPE_DIALOG type
  *    mwenz - Bug 341898 - Support for AdvancedPropertySheet
  *    mgorning - Bug 386913 - Support also Single-Click-Features
  *    pjpaulin - Bug 352120 - Eliminated assumption that diagram is in an IEditorPart
  *    pjpaulin - Bug 352120 - Now uses IDiagramContainerUI interface
  *    Hallvard Traetteberg - Félix Velasco - Bug 403272 - Set the location of DoubleClickContext for DoubleClickFeature
  *    mwenz - Bug 425750 - getSelection not working
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.parts;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.draw2d.ConnectionAnchor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.NodeEditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.editparts.LayerManager;
 import org.eclipse.gef.requests.ChangeBoundsRequest;
 import org.eclipse.gef.requests.CreateConnectionRequest;
 import org.eclipse.gef.requests.DirectEditRequest;
 import org.eclipse.gef.requests.LocationRequest;
 import org.eclipse.gef.requests.SelectionRequest;
 import org.eclipse.gef.tools.DragEditPartsTracker;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.impl.DirectEditingContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.func.IDirectEditing;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.features.context.impl.base.DoubleClickContext;
 import org.eclipse.graphiti.internal.features.context.impl.base.SingleClickContext;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.editor.DiagramBehavior;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProviderInternal;
 import org.eclipse.graphiti.ui.internal.contextbuttons.IContextButtonManager;
 import org.eclipse.graphiti.ui.internal.parts.directedit.GFDirectEditManager;
 import org.eclipse.graphiti.ui.internal.parts.directedit.TextCellLocator;
 import org.eclipse.graphiti.ui.internal.policy.DefaultEditPolicyFactory;
 import org.eclipse.graphiti.ui.internal.util.draw2d.GFChopboxAnchor;
 import org.eclipse.graphiti.ui.platform.GraphitiShapeEditPart;
 import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
 import org.eclipse.graphiti.util.ILocationInfo;
 import org.eclipse.graphiti.util.LocationInfo;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * A GraphicalEditPart, which model is of the type Shape.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ShapeEditPart extends GraphitiShapeEditPart implements IShapeEditPart, NodeEditPart {
 
 	private static final boolean TRACE_OUT = false;
 
 	private final IAnchorContainerDelegate delegate;
 
 	private boolean directEditingDelayed = false;
 
 	/**
 	 * Creates a new ShapeEditPart.
 	 * 
 	 * @param configurationProvider
 	 *            the configuration provider
 	 * @param shape
 	 *            the shape
 	 */
 	public ShapeEditPart(IConfigurationProviderInternal configurationProvider, Shape shape) {
 		delegate = new AnchorContainerDelegate(configurationProvider, shape, this);
 		setModel(shape);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
 	 */
 	@Override
 	public void activate() {
 		super.activate();
 		IContextButtonManager contextButtonManager = getConfigurationProvider().getContextButtonManager();
 		contextButtonManager.register(this);
 		delegate.activate();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
 	 */
 	@Override
 	public void deactivate() {
 		delegate.deactivate();
 		IContextButtonManager contextButtonManager = getConfigurationProvider().getContextButtonManager();
 		contextButtonManager.deRegister(this);
 		super.deactivate();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart
 	 * #deleteChildAndRefresh(org.eclipse.gef.EditPart)
 	 */
 	public void deleteChildAndRefresh(EditPart childEditPart) {
 		removeChild(childEditPart);
 		super.refresh();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang
 	 * .Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
 		Object ret = delegate.getAdapter(key);
 		if (ret == null) {
 			ret = super.getAdapter(key);
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Gets the configuration provider.
 	 * 
 	 * @return The IConfigurationProviderInternal of this EditPart
 	 */
 	public IConfigurationProviderInternal getConfigurationProvider() {
 		return delegate.getConfigurationProvider();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editparts.AbstractGraphicalEditPart#getDragTracker(org
 	 * .eclipse.gef.Request)
 	 */
 	@Override
 	public DragTracker getDragTracker(Request request) {
 
 		// return a modified drag tracker,
 		// which does not add the connection layer to the exclusion set
 		// coz we want to have add requests on connections
 		return new DragEditPartsTracker(this) {
 
 			@Override
 			protected Collection<?> getExclusionSet() {
 
 				Collection<?> exclusionSet2 = super.getExclusionSet();
 
 				LayerManager layerManager = (LayerManager) getCurrentViewer().getEditPartRegistry()
 						.get(LayerManager.ID);
 				if (layerManager != null) {
 					exclusionSet2.remove(layerManager.getLayer(LayerConstants.CONNECTION_LAYER));
 				}
 
 				return exclusionSet2;
 			}
 
 		};
 	}
 
 	@Override
 	public List<PictogramElement> getModelChildren() {
 		List<PictogramElement> ret = new ArrayList<PictogramElement>();
 		ret.addAll(delegate.getModelChildren());
 		return ret;
 	}
 
 	/**
 	 * Returns the source-connections of this EditPart.
 	 * 
 	 * @return the model source connections
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
 	 */
 	@Override
 	public List<Connection> getModelSourceConnections() {
 
 		List<Connection> connections = new ArrayList<Connection>();
 
 		// if this shape has a chopboxanchor return all source-connections of
 		// this anchor
 
 		Anchor anchor = getChopboxAnchor((Shape) getModel());
 		if (anchor != null) {
 			connections.addAll(anchor.getOutgoingConnections());
 		}
 		return connections;
 	}
 
 	// /**
 	// * Returns the source-connections of this EditPart.
 	// *
 	// * @see
 	// org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
 	// */
 	// private List<Connection> getModelSourceConnectionsExpensive() {
 	//
 	// List<Connection> connections = new ArrayList<Connection>();
 	//
 	// // if this shape has a chopboxanchor return all source-connections of
 	// // this anchor
 	//
 	// Anchor anchor = getChopboxAnchor((Shape) getModel());
 	// if (anchor != null) {
 	// Collection cs = getConfigurationProvider().getDiagram().getConnections();
 	// for (Iterator iter = cs.iterator(); iter.hasNext();) {
 	// Connection c = (Connection) iter.next();
 	// if (anchor.equals(c.getStart())) {
 	// connections.add(c);
 	// }
 	// }
 	// }
 	//
 	// return connections;
 	// }
 
 	/**
 	 * Returns the target-connections of this EditPart.
 	 * 
 	 * @return the model target connections
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
 	 */
 	@Override
 	public List<Connection> getModelTargetConnections() {
 
 		List<Connection> connections = new ArrayList<Connection>();
 
 		// if this shape has a chopboxanchor return all target-connections of
 		// this anchor
 
 		Anchor anchor = getChopboxAnchor((Shape) getModel());
 		if (anchor != null) {
 			connections.addAll(anchor.getIncomingConnections());
 		}
 
 		// getModelTargetConnectionsExpensive();
 		return connections;
 	}
 
 	// /**
 	// * Returns the _target-connections of this EditPart.
 	// *
 	// * @see
 	// org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
 	// */
 	// private List<Connection> getModelTargetConnectionsExpensive() {
 	//
 	// List<Connection> connections = new ArrayList<Connection>();
 	//
 	// // if this shape has a chopboxanchor return all target-connections of
 	// // this anchor
 	//
 	// Anchor anchor = getChopboxAnchor((Shape) getModel());
 	// if (anchor != null) {
 	// Collection cs = getConfigurationProvider().getDiagram().getConnections();
 	// for (Iterator iter = cs.iterator(); iter.hasNext();) {
 	// Connection c = (Connection) iter.next();
 	// if (anchor.equals(c.getEnd())) {
 	// connections.add(c);
 	// }
 	// }
 	// }
 	//
 	// return connections;
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart
 	 * #getPictogramElement()
 	 */
 	public PictogramElement getPictogramElement() {
 		return delegate.getPictogramElement();
 	}
 
 	/**
 	 * this is just a fix getParent sometimes returns null - seems to be an
 	 * update problem.
 	 * 
 	 * @return the root
 	 */
 	@Override
 	public RootEditPart getRoot() {
 		return getConfigurationProvider().getDiagramContainer().getGraphicalViewer().getRootEditPart();
 	}
 
 	// ======================= overwriteable behaviour ========================
 
 	/**
 	 * Returns the ConnectionAnchor, which is to be displayed at the source-side
 	 * of an existing connection. By default it returns a new ChopboxAnchor.
 	 * 
 	 * @param connection
 	 *            the connection
 	 * @return the source connection anchor
 	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(ConnectionEditPart)
 	 */
 	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
 		// org.eclipse.graphiti.mm.pictograms.Connection
 		// connectionRefObject =
 		// (org.eclipse.graphiti.mm.pictograms.Connection) connection
 		// .getModel();
 		// for (Iterator iterChildren = getChildren().iterator();
 		// iterChildren.hasNext();) {
 		// EditPart editPart = (EditPart) iterChildren.next();
 		// if (editPart instanceof AnchorEditPart) {
 		// Anchor anchorRefObject = (Anchor) editPart.getModel();
 		// if (connectionRefObject.getStart().equals(anchorRefObject)) {
 		// if (anchorRefObject instanceof
 		// org.eclipse.graphiti.mm.pictograms.ChopboxAnchor)
 		// return new ChopboxAnchor(getFigure());
 		// else if (anchorRefObject instanceof
 		// org.eclipse.graphiti.mm.pictograms.FixPointAnchor)
 		// return new EllipseAnchor(((AnchorEditPart) editPart).getFigure());
 		// }
 		// }
 		// }
 
 		// default is ChopBoxAnchor
 		return createGFChopboxAnchor();
 	}
 
 	/**
 	 * Returns the ConnectionAnchor, which is to be displayed at the source-side
 	 * when creating a new connection. By default it returns a new ChopboxAnchor
 	 * if the source-side is already connected to a ConnectionEditPart, and it
 	 * returns null if the source-side is still dragging and not yet connected
 	 * to a ConnectionEditPart. If the ConnectionAnchor is null, this means that
 	 * the line always ends directly at the mouse-pointer.
 	 * 
 	 * @param request
 	 *            the request
 	 * @return the source connection anchor
 	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(Request)
 	 */
 	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
 		// when a connection-starts, then the connection-line should start at
 		// the EditPart
 		if (request instanceof CreateConnectionRequest) {
 			return createGFChopboxAnchor();
 		}
 		// when moving or creating connections, the line should always end
 		// directly at the mouse-pointer.
 		return null;
 	}
 
 	/**
 	 * Returns the ConnectionAnchor, which is to be displayed at the
 	 * _target-side of an existing connection. By default it returns a new
 	 * ChopboxAnchor.
 	 * 
 	 * @param connection
 	 *            the connection
 	 * @return the target connection anchor
 	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(ConnectionEditPart)
 	 */
 	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
 		// org.eclipse.graphiti.mm.pictograms.Connection
 		// connectionRefObject =
 		// (org.eclipse.graphiti.mm.pictograms.Connection) connection
 		// .getModel();
 		// for (Iterator iterChildren = getChildren().iterator();
 		// iterChildren.hasNext();) {
 		// EditPart editPart = (EditPart) iterChildren.next();
 		// if (editPart instanceof AnchorEditPart) {
 		// Anchor anchorRefObject = (Anchor) editPart.getModel();
 		// if (connectionRefObject.getEnd().equals(anchorRefObject)) {
 		// if (anchorRefObject instanceof
 		// org.eclipse.graphiti.mm.pictograms.ChopboxAnchor)
 		// return new ChopboxAnchor(getFigure());
 		// else if (anchorRefObject instanceof
 		// org.eclipse.graphiti.mm.pictograms.FixPointAnchor)
 		// return new EllipseAnchor(((AnchorEditPart) editPart).getFigure());
 		// }
 		// }
 		// }
 
 		// default is ChopBoxAnchor
 		return createGFChopboxAnchor();
 	}
 
 	private GFChopboxAnchor createGFChopboxAnchor() {
 		IDiagramTypeProvider dtp = getConfigurationProvider().getDiagramTypeProvider();
 		IToolBehaviorProvider tbp = dtp.getCurrentToolBehaviorProvider();
 		GraphicsAlgorithm ga = tbp.getChopboxAnchorArea(getPictogramElement());
 		IFigure f = delegate.getFigureForGraphicsAlgorithm(ga);
 		if (f == null) {
 			f = getFigure();
 		}
 		return new GFChopboxAnchor(f);
 	}
 
 	// ========================= standard behaviour ===========================
 
 	/**
 	 * Returns the ConnectionAnchor, which is to be displayed at the
 	 * _target-side when creating a new connection. By default it returns null.
 	 * If the ConnectionAnchor is null, this means that the line always ends
 	 * directly at the mouse-pointer.
 	 * 
 	 * @param request
 	 *            the request
 	 * @return the target connection anchor
 	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(Request)
 	 */
 	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
 		// when moving or creating connections, the line should always end
 		// directly at the mouse-pointer.
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.EditPart#isSelectable()
 	 */
 	@Override
 	public boolean isSelectable() {
 		PictogramElement pe = getPictogramElement();
 		return super.isSelectable() && pe != null && GraphitiInternal.getEmfService().isObjectAlive(pe)
 				&& pe.isActive();
 	}
 
 	/**
 	 * This method tries to perform a direct-editing with the given request (see
 	 * getLabels()). Additionaly it tries to forward certain requests to this
 	 * EditPart (e.g. RequestConstants.REQ_OPEN). If this is not possbile, it
 	 * forwards the request to super.performRequest(request).
 	 * 
 	 * @param request
 	 *            the request
 	 * @see org.eclipse.gef.EditPart#performRequest(Request)
 	 */
 
 	@Override
 	public void performRequest(Request request) {
 
 		Point point = getRelativeMouseLocation();
 
 		Shape shape = (Shape) getModel();
 		ILocationInfo locationInfo;
 
 		if (shape instanceof ConnectionDecorator && shape.getGraphicsAlgorithm() instanceof Text) {
 			locationInfo = new LocationInfo(shape, shape.getGraphicsAlgorithm());
 		} else {
 			locationInfo = Graphiti.getLayoutService().getLocationInfo(shape, point.x, point.y);
 		}
 
 		// trigger: click on already selected editpart
 		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT && getModel() instanceof Shape) {
 
 			// pressed F2 outside the current shape edit part
 			// location for direct editing is still unknown
 			IToolBehaviorProvider tbp = getConfigurationProvider().getDiagramTypeProvider()
 					.getCurrentToolBehaviorProvider();
 			locationInfo = tbp.getLocationInfo(shape, locationInfo);
 
 			if (locationInfo != null) {
 
 				DirectEditingContext directEditingContext = new DirectEditingContext(locationInfo.getShape(),
 						locationInfo.getGraphicsAlgorithm());
 
 				IFeatureProvider featureProvider = getConfigurationProvider().getDiagramTypeProvider()
 						.getFeatureProvider();
 
 				IDirectEditingFeature directEditingFeature = featureProvider
 						.getDirectEditingFeature(directEditingContext);
 				if (directEditingFeature != null && directEditingFeature.canExecute(directEditingContext)) {
 
 					IFigure figure = delegate.getFigureForGraphicsAlgorithm(locationInfo.getGraphicsAlgorithm());
 
 					if (figure != null) {
 						bringUpDirectEditField(directEditingFeature, directEditingContext, figure);
 					}
 				} else if (request instanceof DirectEditRequest) {
 					// if direct editing feature is not available, ask for a
 					// single click feature
 					// it must be a DirectEditRequest otherwise F2 was pressed
 					SingleClickContext scc = new SingleClickContext(getPictogramElement(), locationInfo.getShape(),
 							locationInfo.getGraphicsAlgorithm());
 
 					IToolBehaviorProvider currentToolBehaviorProvider = getConfigurationProvider()
 							.getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 
 					IFeature singleClickFeature = currentToolBehaviorProvider.getSingleClickFeature(scc);
 
 					if (singleClickFeature != null && singleClickFeature.canExecute(scc)) {
 						GenericFeatureCommandWithContext commandWithContext = new GenericFeatureCommandWithContext(
 								singleClickFeature, scc);
 						DiagramBehavior diagramBehavior = getConfigurationProvider().getDiagramBehavior();
 						CommandStack commandStack = diagramBehavior.getEditDomain().getCommandStack();
 						commandStack.execute(new GefCommandWrapper(commandWithContext, diagramBehavior
 								.getEditingDomain()));
 					}
 				}
 			}
 		} else if (request.getType().equals(REQ_OPEN)) {
 
 			if (locationInfo != null) {
 				DoubleClickContext dcc = new DoubleClickContext(getPictogramElement(), locationInfo.getShape(),
 						locationInfo.getGraphicsAlgorithm());
 
 				DiagramBehavior diagramBehavior = getConfigurationProvider().getDiagramBehavior();
 				if (request instanceof LocationRequest) {
 					Point location = ((LocationRequest) request).getLocation();
 					location = diagramBehavior.calculateRealMouseLocation(location);
 					dcc.setLocation(location.x, location.y);
 				}
 
 				IToolBehaviorProvider currentToolBehaviorProvider = getConfigurationProvider().getDiagramTypeProvider()
 						.getCurrentToolBehaviorProvider();
 
 				IFeature doubleClickFeature = currentToolBehaviorProvider.getDoubleClickFeature(dcc);
 
 				if (doubleClickFeature != null && doubleClickFeature.canExecute(dcc)) {
 					GenericFeatureCommandWithContext commandWithContext = new GenericFeatureCommandWithContext(
 							doubleClickFeature, dcc);
 					CommandStack commandStack = diagramBehavior.getEditDomain().getCommandStack();
 					commandStack.execute(new GefCommandWrapper(commandWithContext, diagramBehavior.getEditingDomain()));
 				}
 
 			}
 
 		}
 
 		super.performRequest(request);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#refresh()
 	 */
 	@Override
 	public void refresh() {
 
 		if (getConfigurationProvider().getDiagramBehavior().isAlive()) {
 
 			long start = System.currentTimeMillis();
 			super.refresh();
 			long end = System.currentTimeMillis();
 			if (TRACE_OUT) {
 				if (T.racer().info()) {
 					T.racer().info("ShapeEditPart.refresh() took " + (end - start) + "ms. Model: " + getModel()); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 	}
 
 	/**
 	 * Switch to direct editing mode.
 	 * 
 	 * @param pictogramElement
 	 *            the pictogram element
 	 * @param graphicsAlgorithm
 	 *            the graphics algorithm
 	 */
 	public void switchToDirectEditingMode(PictogramElement pictogramElement, GraphicsAlgorithm graphicsAlgorithm) {
 
 		if (pictogramElement == null || graphicsAlgorithm == null) {
 			return;
 		}
 
 		DirectEditingContext directEditingContext = new DirectEditingContext(pictogramElement, graphicsAlgorithm);
 
 		IFeatureProvider featureProvider = getConfigurationProvider().getDiagramTypeProvider().getFeatureProvider();
 
 		IDirectEditingFeature directEditingFeature = featureProvider.getDirectEditingFeature(directEditingContext);
 		if (directEditingFeature != null && directEditingFeature.canExecute(directEditingContext)) {
 
 			IFigure figure = delegate.getFigureForGraphicsAlgorithm(graphicsAlgorithm);
 
 			if (figure != null) {
 				bringUpDirectEditField(directEditingFeature, directEditingContext, figure);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
 	 */
 	@Override
 	protected void createEditPolicies() {
 		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, getConfigurationProvider().getEditPolicyFactory()
 				.createShapeHighlightEditPolicy());
 		installEditPolicy(EditPolicy.LAYOUT_ROLE, getConfigurationProvider().getEditPolicyFactory()
 				.createShapeForbidLayoutEditPolicy());
 		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, getConfigurationProvider().getEditPolicyFactory()
 				.createDirectEditPolicy());
 		installEditPolicy(EditPolicy.COMPONENT_ROLE, getConfigurationProvider().getEditPolicyFactory()
 				.createModelObjectDeleteEditPolicy(getConfigurationProvider()));
 		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, getConfigurationProvider().getEditPolicyFactory()
 				.createConnectionEditPolicy());
 		installEditPolicy(DefaultEditPolicyFactory.HOVER_POLICY_KEY, getConfigurationProvider().getEditPolicyFactory()
 				.createShapeHoverEditPolicy());
 	}
 
 	/**
 	 * Creates the Figure of this editpart. This determines how the editpart
 	 * will be displayed. The actual data for this figure should be provided in
 	 * refreshVisuals(). Creates the Figure of this editpart. This determines
 	 * how the editpart will be displayed. The actual data for this figure
 	 * should be provided in refreshVisuals().
 	 * 
 	 * @return the i figure
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
 	 */
 	@Override
 	protected IFigure createFigure() {
 		IFigure theFigure = delegate.createFigure();
 		return theFigure;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshChildren()
 	 */
 	@Override
 	protected void refreshChildren() {
 		super.refreshChildren();
 
 		// refresh edit parts for child PEs as well
 		delegate.refreshEditPartsForModelChildrenAndSourceConnections(this);
 	}
 
 	/**
 	 * This method is called, whenever the data of the underlying ModelObject
 	 * changes. It must update the figures to display the changed data.
 	 * Sub-classes will nearly always overwrite this method.
 	 * <p>
 	 * By default this method takes care to update the layout-informations and
 	 * to update the labels of the attributes (if existing), so sub-classes
 	 * should call super.refreshVisuals().
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
 	 */
 	@Override
 	protected void refreshVisuals() {
 		super.refreshVisuals();
 		refreshSourceConnections();
 		refreshTargetConnections();
 		delegate.refreshFigureForEditPart();
 	}
 
 	/**
 	 * shows the direct edit field, depending on the direct edit feature /
 	 * context
 	 * 
 	 * @param directEditingFeature
 	 * @param directEditingContext
 	 * @param figure
 	 *            The IFigure on which the direct editing should appear
 	 */
 	private void bringUpDirectEditField(IDirectEditingFeature directEditingFeature,
 			IDirectEditingContext directEditingContext, IFigure figure) {
 
 		if (isDirectEditingDelayed()) {
 			return;
 		}
 
 		if (directEditingFeature.getEditingType() == IDirectEditing.TYPE_NONE) {
 			return;
 		}
 
 		TextCellLocator cellEditorLocator = new TextCellLocator(figure, directEditingFeature);
 
 		GFDirectEditManager directEditManager = new GFDirectEditManager(this, cellEditorLocator);
 		directEditManager.setDirectEditingFeature(directEditingFeature);
 		directEditManager.setDirectEditingContext(directEditingContext);
 		directEditManager.show();
 
 		getConfigurationProvider().getDiagramBehavior().setPictogramElementForSelection(getPictogramElement());
 	}
 
 	private Anchor getChopboxAnchor(AnchorContainer anchorContainer) {
 		if (GraphitiInternal.getEmfService().isObjectAlive(anchorContainer)) {
 			List<Anchor> existingAnchors = anchorContainer.getAnchors();
 			for (Anchor anchor : existingAnchors) {
 				if (anchor instanceof ChopboxAnchor) {
 					return anchor;
 				}
 			}
 		}
 		return null;
 	}
 
 	private Point getRelativeMouseLocation() {
 		DiagramBehavior diagramBehavior = getConfigurationProvider().getDiagramBehavior();
 
 		// get current mouse location from the viewer
 		Point mouseLocation = new Point(diagramBehavior.getMouseLocation());
 
 		// calculate location in pictogram model in dependence from scroll and
 		// zoom
 		mouseLocation = diagramBehavior.calculateRealMouseLocation(mouseLocation);
 
 		Rectangle bounds = getFigure().getBounds().getCopy();
 
 		// set bounds location in dependence from scroll and zoom
 		// bounds dimension is not of interest
 		bounds.setLocation(diagramBehavior.calculateRealMouseLocation(bounds.getLocation()));
 
 		// get bounds in dependence to the main figure
 		// this method also considers sroll and zoom
 		getFigure().translateToAbsolute(bounds);
 
 		// calculate mouselocation relative to the figure
 		mouseLocation = mouseLocation.getTranslated(bounds.getLocation().getNegated());
 
 		return mouseLocation;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editparts.AbstractEditPart#getTargetEditPart(org.eclipse
 	 * .gef.Request)
 	 */
 	@Override
 	public EditPart getTargetEditPart(Request request) {
 		if (request instanceof SelectionRequest
				|| (request != null && request.getType() != null && "selection".equalsIgnoreCase(request.getType()
 						.toString()))) {
 
 			IConfigurationProvider configurationProvider = getConfigurationProvider();
 
 			PictogramElement alternativeSelection = configurationProvider
 					.getDiagramTypeProvider()
 					.getCurrentToolBehaviorProvider()
 					.getSelection(getPictogramElement(),
 							configurationProvider.getDiagramBehavior().getSelectedPictogramElements());
 			if (alternativeSelection != null) {
 				Object object = configurationProvider.getDiagramContainer().getGraphicalViewer().getEditPartRegistry()
 						.get(alternativeSelection);
 				if (object instanceof EditPart) {
 					return (EditPart) object;
 				}
 			}
 		}
 
 		return super.getTargetEditPart(request);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.gef.editparts.AbstractEditPart#showSourceFeedback(org.eclipse
 	 * .gef.Request)
 	 */
 	@Override
 	public void showSourceFeedback(Request request) {
 		// suppress source feedback if no move feature available
 		if ((REQ_MOVE.equals(request.getType()) || REQ_ADD.equals(request.getType()))
 				&& (request instanceof ChangeBoundsRequest)) {
 			Shape shape = (Shape) getPictogramElement();
 			ContainerShape containerShape = shape.getContainer();
 			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
 			Point oldLocation = getFigure().getBounds().getLocation();
 			Point currentDelta = changeBoundsRequest.getMoveDelta();
 			IMoveShapeContext context = createMoveShapeContext(shape, containerShape, containerShape, oldLocation,
 					currentDelta);
 			IMoveShapeFeature moveShapeFeature = getConfigurationProvider().getFeatureProvider().getMoveShapeFeature(
 					context);
 			if (moveShapeFeature == null) {
 				return;
 			}
 		}
 		super.showSourceFeedback(request);
 	}
 
 	private IMoveShapeContext createMoveShapeContext(Shape shape, ContainerShape source, ContainerShape target,
 			Point location, Point delta) {
 
 		MoveShapeContext ret = new MoveShapeContext(shape);
 		ret.setSourceContainer(source);
 		ret.setTargetContainer(target);
 		ret.setX(location.x);
 		ret.setY(location.y);
 		ret.setDeltaX(delta.x);
 		ret.setDeltaY(delta.y);
 
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.features.IFeatureProviderHolder#getFeatureProvider()
 	 */
 	public IFeatureProvider getFeatureProvider() {
 		IFeatureProvider ret = null;
 		if (delegate != null) {
 			ret = delegate.getFeatureProvider();
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart#
 	 * getPictogramElementDelegate()
 	 */
 	public IPictogramElementDelegate getPictogramElementDelegate() {
 		return delegate;
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getName() + "@" + Integer.toHexString(hashCode()); //$NON-NLS-1$
 	}
 
 	public void refreshDecorators() {
 		delegate.refreshDecorators();
 	}
 
 	public void delayDirectEditing() {
 		setDirectEditingDelayed(true);
 		Runnable runnable = new Runnable() {
 			public void run() {
 				setDirectEditingDelayed(false);
 			}
 		};
 		Display.getCurrent().timerExec(600, runnable);
 	}
 
 	/**
 	 * @return the directEditingDelayed
 	 */
 	private boolean isDirectEditingDelayed() {
 		return directEditingDelayed;
 	}
 
 	/**
 	 * @param directEditingDelayed
 	 *            the directEditingDelayed to set
 	 */
 	private void setDirectEditingDelayed(boolean directEditingDelayed) {
 		this.directEditingDelayed = directEditingDelayed;
 	}
 }
