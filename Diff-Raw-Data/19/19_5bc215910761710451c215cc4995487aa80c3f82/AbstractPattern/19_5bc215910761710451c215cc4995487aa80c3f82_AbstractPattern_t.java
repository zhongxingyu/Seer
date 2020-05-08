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
  *    Volker Wegert - Bug 336828: patterns should support delete,
  *                    remove, direct editing and conditional palette
  *                    creation entry
  *    mwenz - Bug 325084 - Provide documentation for Patterns
  *    cbrand - Bug 376585 - Clean-up deprecations in Graphiti
 *    cbrand - Bug 385190 - Introduce constructor without parameters for patterns
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.pattern;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.graphiti.features.DefaultResizeConfiguration;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IDirectEditingInfo;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeConfiguration;
 import org.eclipse.graphiti.features.context.IAreaContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
 import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.func.IDelete;
 import org.eclipse.graphiti.func.IDirectEditing;
 import org.eclipse.graphiti.func.IProposalSupport;
 import org.eclipse.graphiti.func.IRemove;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.pattern.config.IPatternConfiguration;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 
 /**
  * This is the base class AbstractConnectionPattern that clients writing a
  * pattern for a shape domain object should subclass.
  */
 public abstract class AbstractPattern extends AbstractBasePattern implements IPattern {
 
 	/**
 	 * An empty string array used in direct editing.
 	 */
 	protected static final String[] EMPTY_STRING_ARRAY = new String[0];
 
 	private IPatternConfiguration patternConfiguration;
 
 	/**
 	 * To avoid code duplication, this base class uses a wrapped default
 	 * implementation of an {@link IDeleteFeature} to provide the default
 	 * deletion behaviour. Subclasses may decide to either override
 	 * {@link #createDeleteFeature(IDeleteContext)} to provide another
 	 * {@link IDeleteFeature} implementation or override and extend the
 	 * individual {@link IDelete} methods or return a {@link IDeleteFeature} by
 	 * overriding the method
 	 * {@link DefaultFeatureProviderWithPatterns#getDeleteFeature(IDeleteContext)}
 	 * .
 	 */
 	private IDeleteFeature wrappedDeleteFeature;
 
 	/**
 	 * To avoid code duplication, this base class uses a wrapped default
 	 * implementation of an {@link IRemoveFeature} to provide the default
 	 * removal behavior. Subclasses may decide to either override
 	 * {@link #createRemoveFeature(IRemoveContext)} to provide another
 	 * {@link IRemoveFeature} implementation or override and extend the
 	 * individual {@link IRemove} methods or return a {@link IRemoveFeature} by
 	 * overriding the method
 	 * {@link DefaultFeatureProviderWithPatterns#getRemoveFeature(IRemoveContext)}
 	 * .
 	 */
 	private IRemoveFeature wrappedRemoveFeature;
 
 	/**
 	 * Creates a new {@link AbstractPattern} holding the given
 	 * {@link IPatternConfiguration}.
 	 * 
 	 * @param patternConfiguration
 	 *            The pattern configuration to use within this pattern instance
 	 *            of <code>null</code> in case no configuration is needed.
 	 */
 	public AbstractPattern(IPatternConfiguration patternConfiguration) {
		this();
 		setPatternConfiguration(patternConfiguration);
 	}
 
 	/**
	 * Creates a new {@link AbstractPattern}. This is a convenience method for
	 * patterns working without any configuration.
	 * 
	 * @since 0.10
	 */
	public AbstractPattern() {
		super();
	}

	/**
 	 * Is queried by the Graphiti framework to check if the pattern should
 	 * create a new domain object entry in the editor palette.
 	 * 
 	 * @return <code>true</code> in case a palette entry shall be created,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isPaletteApplicable() {
 		return true;
 	}
 
 	/**
 	 * Clients must override this method to indicate that the pattern can be
 	 * used to create domain objects as defined in the given
 	 * {@link ICreateContext}. Corresponds to the method
 	 * {@link AbstractCreateFeature#canCreate(ICreateContext)} . The default
 	 * implementation simply returns <code>false</code>.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            created.
 	 * @return <code>true</code> in case this pattern can create such a domain
 	 *         object, <code>false</code> otherwise.
 	 */
 	public boolean canCreate(ICreateContext context) {
 		return false;
 	}
 
 	/**
 	 * Clients may override this method to indicate that the pattern can be used
 	 * to layout a shape for a domain objects as defined in the given
 	 * {@link ILayoutContext}. Corresponds to the method
 	 * {@link AbstractLayoutFeature#canLayout(ILayoutContext)}. The default
 	 * implementation checks if the {@link PictogramElement} in the given
 	 * context {@link #isPatternControlled(PictogramElement)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            layouted.
 	 * @return <code>true</code> in case this pattern can layout a shape for
 	 *         such a domain object, <code>false</code> otherwise.
 	 */
 	public boolean canLayout(ILayoutContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		return isPatternControlled(pictogramElement);
 	}
 
 	/**
 	 * Clients may override this method to indicate that the pattern can be used
 	 * to move the shape of a domain objects as defined in the given
 	 * {@link IMoveShapeContext}. Corresponds to the method
 	 * {@link DefaultMoveShapeFeature#canMoveShape(IMoveShapeContext)}. The
 	 * default implementation checks if the {@link PictogramElement} in the
 	 * given context {@link #isPatternControlled(PictogramElement)} and the
 	 * source and target containers of the shape are the same.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            moved.
 	 * @return <code>true</code> in case this pattern can move a shape for such
 	 *         a domain object, <code>false</code> otherwise.
 	 */
 	public boolean canMoveShape(IMoveShapeContext context) {
 		return context.getSourceContainer() != null
 				&& context.getSourceContainer().equals(context.getTargetContainer())
 				&& isPatternRoot(context.getPictogramElement());
 	}
 
 	/**
 	 * Clients may override this method to indicate that the pattern can be used
 	 * to resize the shape of a domain objects as defined in the given
 	 * {@link IResizeShapeContext}. Corresponds to the method
 	 * {@link DefaultResizeShapeFeature#canResizeShape(IResizeShapeContext)}.
 	 * The default implementation checks if the {@link PictogramElement} in the
 	 * given context fulfills {@link #isPatternRoot(PictogramElement)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            resized.
 	 * @return <code>true</code> in case this pattern can resize a shape for
 	 *         such a domain object, <code>false</code> otherwise.
 	 */
 	public boolean canResizeShape(IResizeShapeContext context) {
 		return isPatternRoot(context.getPictogramElement());
 	}
 
 	/**
 	 * Clients may override this method to indicate that the pattern can be used
 	 * to update the shape of a domain objects as defined in the given
 	 * {@link IUpdateContext}. Corresponds to the method
 	 * {@link AbstractUpdateFeature#canUpdate(IUpdateContext)}. The default
 	 * implementation checks if the {@link PictogramElement} in the given
 	 * context {@link #isPatternControlled(PictogramElement)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            updated.
 	 * @return <code>true</code> in case this pattern can update a shape for
 	 *         such a domain object, <code>false</code> otherwise.
 	 */
 	public boolean canUpdate(IUpdateContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		return isPatternControlled(pictogramElement);
 	}
 
 	/**
 	 * Clients must override this method to implement the functionality to
 	 * create a new domain object as defined in the given {@link ICreateContext}
 	 * . Corresponds to the method
 	 * {@link AbstractCreateFeature#create(ICreateContext)}. The default
 	 * implementation simply does nothing and returns an empty object array.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            created.
 	 * @return An array of the newly create domain objects.
 	 */
 	public Object[] create(ICreateContext context) {
 		return EMPTY;
 	}
 
 	/**
 	 * Client should override to return a string description of the type of
 	 * domain object that is created with this pattern. The Graphiti framework
 	 * uses this information to fill a tooltip for the creation tool entry in
 	 * the palette. The default implementation simply returns <code>null</code>
 	 * which indicates that no tooltip shall be displayed.
 	 * 
 	 * @return A {@link String} holding the tooltip
 	 */
 	public String getCreateDescription() {
 		return null;
 	}
 
 	/**
 	 * Client should override to return a string id of the the image icon for
 	 * the domain object that is created with this pattern. The Graphiti
 	 * framework uses this information to add an icon to the creation tool entry
 	 * in the palette. The default implementation simply returns
 	 * <code>null</code> which indicates that no icon shall be displayed.
 	 * 
 	 * @return A {@link String} holding the id of the icon as defined in the
 	 *         AbstractImageProvider.
 	 */
 	public String getCreateImageId() {
 		return null;
 	}
 
 	/**
 	 * Client should override to return a string id of the the large image icon
 	 * for the domain object that is created with this pattern. The Graphiti
 	 * framework uses this information to add a large icon to the creation tool
 	 * entry in the palette. The default implementation simply returns
 	 * <code>null</code> which indicates that no icon shall be displayed.
 	 * 
 	 * @return A {@link String} holding the id of the large icon as defined in
 	 *         the AbstractImageProvider.
 	 */
 	public String getCreateLargeImageId() {
 		return getCreateImageId();
 	}
 
 	/**
 	 * Client should override to return the name of the domain object that is
 	 * created with this pattern. The Graphiti framework uses this information
 	 * to fill the text for the creation tool entry in the palette. The default
 	 * implementation simply returns <code>null</code> which results in an empty
 	 * entry in the palette.
 	 * 
 	 * @return A {@link String} holding the name of the domain object.
 	 */
 	public String getCreateName() {
 		return null;
 	}
 
 	/**
 	 * Clients must override this method to indicate that the pattern uses the
 	 * given domain object as its main domain object.
 	 * 
 	 * @param mainBusinessObject
 	 *            The object to check if it is the main domain object of the
 	 *            pattern.
 	 * @return <code>true</code> in case the pattern has the given domain object
 	 *         as its main domain object, <code>false</code> otherwise.
 	 */
 	abstract public boolean isMainBusinessObjectApplicable(Object mainBusinessObject);
 
 	/**
 	 * Clients can override this method to implement the functionality to layout
 	 * a shape for the given domain object as defined in the given
 	 * {@link ILayoutContext} . Corresponds to the method
 	 * {@link AbstractLayoutFeature#layout(ILayoutContext)}. The default
 	 * implementation simply does nothing and returns <code>false</code> as
 	 * indication of this.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            layouted.
 	 * @return Should return <code>true</code> in case a layout happened and
 	 *         <code>false</code> in case none happened. Is used by the Graphiti
 	 *         framework for performance optimization.
 	 */
 	public boolean layout(ILayoutContext context) {
 		return false;
 	}
 
 	/**
 	 * Clients can override this method to implement the functionality to move a
 	 * shape for the given domain object as defined in the given
 	 * {@link IMoveShapeContext} . Corresponds to the method
 	 * {@link DefaultMoveShapeFeature#moveShape(IMoveShapeContext)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            moved.
 	 */
 	public void moveShape(IMoveShapeContext context) {
 		preMoveShape(context);
 		moveAllBendpoints(context);
 		internalMove(context);
 		postMoveShape(context);
 	}
 
 	/**
 	 * Hook clients can override to add additional steps after the move of the
 	 * shape happened.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object that was
 	 *            moved.
 	 */
 	protected void postMoveShape(IMoveShapeContext context) {
 	}
 
 	/**
 	 * Hook clients can override to add additional steps before the move of the
 	 * shape happens.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            moved.
 	 */
 	protected void preMoveShape(IMoveShapeContext context) {
 	}
 
 	/**
 	 * Default implementation of the move functionality. Moves shapes to new
 	 * coordinates and adapts parents in case this is needed.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            moved.
 	 */
 	protected void internalMove(IMoveShapeContext context) {
 		Shape shapeToMove = context.getShape();
 		ContainerShape oldContainerShape = context.getSourceContainer();
 		ContainerShape newContainerShape = context.getTargetContainer();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		if (oldContainerShape != newContainerShape) {
 			// the following is a workaround due to an MMR bug
 			if (oldContainerShape != null) {
 				Collection<Shape> children = oldContainerShape.getChildren();
 				if (children != null) {
 					children.remove(shapeToMove);
 				}
 			}
 
 			shapeToMove.setContainer(newContainerShape);
 			if (shapeToMove.getGraphicsAlgorithm() != null) {
 				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y,
 						avoidNegativeCoordinates());
 			}
 		} else { // move within the same container
 			if (shapeToMove.getGraphicsAlgorithm() != null) {
 				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y,
 						avoidNegativeCoordinates());
 			}
 		}
 	}
 
 	/**
 	 * Default implementation of the move functionality to move all bendpoints
 	 * within a container shape.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            moved.
 	 */
 	protected void moveAllBendpoints(IMoveShapeContext context) {
 
 		if (!(context.getShape() instanceof ContainerShape)) {
 			return;
 		}
 
 		ContainerShape shapeToMove = (ContainerShape) context.getShape();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		int deltaX = x - shapeToMove.getGraphicsAlgorithm().getX();
 		int deltaY = y - shapeToMove.getGraphicsAlgorithm().getY();
 
 		if (deltaX != 0 || deltaY != 0) {
 
 			List<Anchor> anchorsFrom = getAnchors(shapeToMove);
 			List<Anchor> anchorsTo = new ArrayList<Anchor>(anchorsFrom);
 
 			for (Anchor anchorFrom : anchorsFrom) {
 
 				Collection<Connection> outgoingConnections = anchorFrom.getOutgoingConnections();
 
 				for (Connection connection : outgoingConnections) {
 					for (Anchor anchorTo : anchorsTo) {
 
 						Collection<Connection> incomingConnections = anchorTo.getIncomingConnections();
 						if (incomingConnections.contains(connection)) {
 							if (connection instanceof FreeFormConnection) {
 								FreeFormConnection ffc = (FreeFormConnection) connection;
 								List<Point> points = ffc.getBendpoints();
 								for (int i = 0; i < points.size(); i++) {
 									Point point = points.get(i);
 									int oldX = point.getX();
 									int oldY = point.getY();
 									points.set(i, Graphiti.getGaService().createPoint(oldX + deltaX, oldY + deltaY));
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private List<Anchor> getAnchors(ContainerShape containerShape) {
 		List<Anchor> ret = new ArrayList<Anchor>();
 		ret.addAll(containerShape.getAnchors());
 
 		List<Shape> children = containerShape.getChildren();
 		for (Shape shape : children) {
 			if (shape instanceof ContainerShape) {
 				ret.addAll(getAnchors((ContainerShape) shape));
 			} else {
 				ret.addAll(shape.getAnchors());
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Clients can override this method to implement the functionality to resize
 	 * a shape for the given domain object as defined in the given
 	 * {@link IResizeShapeContext} . Corresponds to the method
 	 * {@link DefaultResizeShapeFeature#resizeShape(IResizeShapeContext)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            resized.
 	 */
 	public void resizeShape(IResizeShapeContext context) {
 		Shape shape = context.getShape();
 		int x = context.getX();
 		int y = context.getY();
 		int width = context.getWidth();
 		int height = context.getHeight();
 
 		if (shape.getGraphicsAlgorithm() != null) {
 			Graphiti.getGaService().setLocationAndSize(shape.getGraphicsAlgorithm(), x, y, width, height);
 		}
 
 		layoutPictogramElement(shape);
 	}
 
 	/**
 	 * Clients can override this method to implement the functionality to update
 	 * a shape for the given domain object as defined in the given
 	 * {@link IUpdateContext}. Corresponds to the method
 	 * {@link AbstractUpdateFeature#update(IUpdateContext)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            updated.
 	 */
 	public boolean update(IUpdateContext context) {
 		return false;
 	}
 
 	/**
 	 * Clients can override this method to indicate if an update of a shape for
 	 * the given domain object as defined in the given {@link IUpdateContext}
 	 * needs to be triggered. Corresponds to the method
 	 * {@link AbstractUpdateFeature#updateNeeded(IUpdateContext)}.
 	 * 
 	 * @param context
 	 *            The context holding information on the domain object to be
 	 *            updated.
 	 */
 	public IReason updateNeeded(IUpdateContext context) {
 		return Reason.createFalseReason();
 	}
 
 	/**
 	 * Adds the graphical representation of the given new {@link Object} with
 	 * the information in the given {@link IAreaContext}.
 	 * 
 	 * @param context
 	 *            The area context defining where the new object should placed
 	 * @param newObject
 	 *            The new object instance itself
 	 */
 	protected void addGraphicalRepresentation(IAreaContext context, Object newObject) {
 		getFeatureProvider().addIfPossible(new AddContext(context, newObject));
 	}
 
 	/**
 	 * Clients can override to indicate that moving to negative coordinates
 	 * should be possible. The default implementation prohibits this by
 	 * returning false.
 	 * 
 	 * @return <code>true</code> in case moving a shape to negative coordinates
 	 *         should be possible, <code>false</code> otherwise.
 	 */
 	protected boolean avoidNegativeCoordinates() {
 		return false;
 	}
 
 	/**
 	 * This method must be implemented by clients to indicate that the given
 	 * {@link PictogramElement} is controlled by this pattern.
 	 * 
 	 * @param pictogramElement
 	 *            The pictogram element to check
 	 * @return <code>true</code> in case the pictogram element is controlled by
 	 *         this pattern, <code>false</code> otherwise.
 	 */
 	abstract protected boolean isPatternControlled(PictogramElement pictogramElement);
 
 	/**
 	 * This method must be implemented by clients to indicate that the given
 	 * {@link PictogramElement} is the root shape of this pattern.
 	 * 
 	 * @param pictogramElement
 	 *            The pictogram element to check
 	 * @return <code>true</code> in case the pictogram element is the root shape
 	 *         of this pattern, <code>false</code> otherwise.
 	 */
 	abstract protected boolean isPatternRoot(PictogramElement pictogramElement);
 
 	/**
 	 * Helper method that triggers a layout of the given
 	 * {@link PictogramElement}. The default implementation queries the feature
 	 * provider and tries to find a functionality either in the pattern of an
 	 * additional {@link AbstractLayoutFeature} that can handle the request and
 	 * triggers the operation.
 	 * 
 	 * @param pe
 	 *            The pictogram element to layout
 	 */
 	protected void layoutPictogramElement(PictogramElement pe) {
 		LayoutContext context = new LayoutContext(pe);
 		getFeatureProvider().layoutIfPossible(context);
 	}
 
 	/**
 	 * Helper method that triggers an update of the given
 	 * {@link PictogramElement}. The default implementation queries the feature
 	 * provider and tries to find a functionality either in the pattern of an
 	 * additional {@link AbstractUpdateFeature} that can handle the request and
 	 * triggers the operation.
 	 * 
 	 * @param pe
 	 *            The pictogram element to update
 	 */
 	protected void updatePictogramElement(PictogramElement pe) {
 		UpdateContext context = new UpdateContext(pe);
 		getFeatureProvider().updateIfPossible(context);
 		layoutPictogramElement(pe);
 	}
 
 	/**
 	 * Sets the {@link IPatternConfiguration} instance to be used with this
 	 * pattern.
 	 * 
 	 * @param patternConfiguration
 	 *            The new patternConfiguration
 	 */
 	protected void setPatternConfiguration(IPatternConfiguration patternConfiguration) {
 		this.patternConfiguration = patternConfiguration;
 	}
 
 	/**
 	 * Returns the {@link IPatternConfiguration} instance used within this
 	 * pattern or <code>null</code> in case none is used.
 	 * 
	 * @return The patternConfiguration instance or <code>null</code> it there
	 *         is none set
 	 */
 	protected IPatternConfiguration getPatternConfiguration() {
 		return patternConfiguration;
 	}
 
 	/**
 	 * Clients can override to complete the {@link IDirectEditingInfo} info.
 	 * This information is needed to switch automatically into the direct
 	 * editing mode. (e.g. after creation of a new object).
 	 * 
 	 * @param info
 	 *            The direct editing info
 	 * @param bo
 	 *            The domain object
 	 */
 	public void completeInfo(IDirectEditingInfo info, Object bo) {
 	}
 
 	/**
 	 * Clients can override to complete the {@link IDirectEditingInfo} info.
 	 * This information is needed to switch automatically into the direct
 	 * editing mode. (e.g. after creation of a new object)
 	 * 
 	 * @param info
 	 *            The direct editing info
 	 * @param bo
 	 *            The domain object
 	 * @param keyProperty
 	 *            The key property
 	 */
 	public void completeInfo(IDirectEditingInfo info, Object bo, String keyProperty) {
 	}
 
 	/**
 	 * Clients may override to modify the resize behavior. The default
 	 * implementation returns a new instance of
 	 * {@link DefaultResizeConfiguration}, which allows bothe the horizontal and
 	 * vertical resize of a shape.
 	 * 
 	 * @param context
 	 *            Context object holding information about the shape to be
 	 *            resized.
 	 * @return An instance of {@link IResizeConfiguration} defining the resize
 	 *         behavior.
 	 */
 	public IResizeConfiguration getResizeConfiguration(IResizeShapeContext context) {
 		return new DefaultResizeConfiguration();
 	}
 
 	/**
 	 * Creates the {@link IDeleteFeature} instance that handles the deletion of
 	 * business objects and diagram elements. The default implementation just
 	 * creates a {@link DefaultDeleteFeature}. Concrete pattern implementations
 	 * may either override this method to provide their own subclass of
 	 * {@link DefaultDeleteFeature} or override and extend the individual
 	 * methods provided by {@link IDelete}.
 	 * 
 	 * @param context
 	 *            the deletion context
 	 * @return the {@link IDeleteFeature} instance to use for this pattern
 	 * @see #canDelete(IDeleteContext)
 	 * @see #preDelete(IDeleteContext)
 	 * @see #delete(IDeleteContext)
 	 * @see #postDelete(IDeleteContext)
 	 */
 	protected IDeleteFeature createDeleteFeature(IDeleteContext context) {
 		return new DefaultDeleteFeature(getFeatureProvider());
 	}
 
 	/**
 	 * Clients can override to modify the default behavior if the pattern can
 	 * (and wants to) handle a delete request. The default implementation calls
 	 * {@link #createDeleteFeature(IDeleteContext)} and asks the result's
 	 * canDelete method.
 	 * 
 	 * @param context
 	 *            The context describing the delete request
 	 * 
 	 * @return <code>true</code>, if the pattern can perform the delete
 	 *         operation, <code>false</code> otherwise
 	 */
 	public boolean canDelete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		return ((wrappedDeleteFeature != null) && wrappedDeleteFeature.canDelete(context));
 	}
 
 	/**
 	 * Clients can override to add actions before the default delete behavior is
 	 * triggered. The default implementation calls
 	 * {@link #createDeleteFeature(IDeleteContext)} and triggers the result's
 	 * preDelete method.
 	 * 
 	 * @param context
 	 *            The context describing the delete request
 	 */
 	public void preDelete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		if (wrappedDeleteFeature != null) {
 			wrappedDeleteFeature.preDelete(context);
 		}
 	}
 
 	/**
 	 * Clients can override to modify the default delete behavior. The default
 	 * implementation calls {@link #createDeleteFeature(IDeleteContext)} and
 	 * triggers the result's delete method.
 	 * 
 	 * @param context
 	 *            The context describing the delete request
 	 */
 	public void delete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		if (wrappedDeleteFeature != null) {
 			wrappedDeleteFeature.delete(context);
 		}
 	}
 
 	/**
 	 * Clients can override to add actions after the default delete behavior is
 	 * triggered. The default implementation calls
 	 * {@link #createDeleteFeature(IDeleteContext)} and triggers the result's
 	 * postDelete method.
 	 * 
 	 * @param context
 	 *            The context describing the delete request
 	 */
 	public void postDelete(IDeleteContext context) {
 		if (wrappedDeleteFeature == null) {
 			wrappedDeleteFeature = createDeleteFeature(context);
 		}
 		if (wrappedDeleteFeature != null) {
 			wrappedDeleteFeature.postDelete(context);
 		}
 	}
 
 	/**
 	 * Creates the {@link IRemoveFeature} instance that handles the removal of
 	 * diagram elements. The default implementation just creates a
 	 * {@link DefaultRemoveFeature}. Concrete pattern implementations may either
 	 * override this method to provide their own subclass of
 	 * {@link DefaultRemoveFeature} or override and extend the individual
 	 * methods provided by {@link IRemove}.
 	 * 
 	 * @param context
 	 *            the removal context
 	 * @return the {@link IRemoveFeature} instance to use for this pattern
 	 * @see #canRemove(IRemoveContext)
 	 * @see #preRemove(IRemoveContext)
 	 * @see #remove(IRemoveContext)
 	 * @see #postRemove(IRemoveContext)
 	 */
 	protected IRemoveFeature createRemoveFeature(IRemoveContext context) {
 		return new DefaultRemoveFeature(getFeatureProvider());
 	}
 
 	/**
 	 * Clients can override to modify the default behavior if the pattern can
 	 * (and wants to) handle a remove request. The default implementation calls
 	 * {@link #createRemoveFeature(IRemoveContext)} and asks the result's
 	 * canRemove method.
 	 * 
 	 * @param context
 	 *            The context describing the remove request
 	 * 
 	 * @return <code>true</code>, if the pattern can perform the delete
 	 *         operation, <code>false</code> otherwise
 	 */
 	public boolean canRemove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		return wrappedRemoveFeature.canRemove(context);
 	}
 
 	/**
 	 * Clients can override to add actions before the default remove behavior is
 	 * triggered. The default implementation calls
 	 * {@link #createRemoveFeature(IRemoveContext)} and triggers the result's
 	 * preRemove method.
 	 * 
 	 * @param context
 	 *            The context describing the remove request
 	 */
 	public void preRemove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		wrappedRemoveFeature.preRemove(context);
 	}
 
 	/**
 	 * Clients can override to modify the default remove behavior. The default
 	 * implementation calls {@link #createRemoveFeature(IRemoveContext)} and
 	 * triggers the result's remove method.
 	 * 
 	 * @param context
 	 *            The context describing the remove request
 	 */
 	public void remove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		wrappedRemoveFeature.remove(context);
 	}
 
 	/**
 	 * Clients can override to add actions after the default remove behavior is
 	 * triggered. The default implementation calls
 	 * {@link #createRemoveFeature(IRemoveContext)} and triggers the result's
 	 * postRemove method.
 	 * 
 	 * @param context
 	 *            The context describing the remove request
 	 */
 	public void postRemove(IRemoveContext context) {
 		if (wrappedRemoveFeature == null) {
 			wrappedRemoveFeature = createRemoveFeature(context);
 		}
 		wrappedRemoveFeature.postRemove(context);
 	}
 
 	/**
 	 * Clients can override this method to indicate that the pattern allows
 	 * direct editing for the shape described in the passed
 	 * {@link IDirectEditingContext}. Corresponds to the method
 	 * {@link AbstractDirectEditingFeature#canDirectEdit(IDirectEditingContext)}
 	 * . The default implementation simply returns <code>false</code>.
 	 * 
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 * @return <code>true</code> in case direct editing shall be allowed,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean canDirectEdit(IDirectEditingContext context) {
 		return false;
 	}
 
 	/**
 	 * This method will be called by the framework to check if the passed String
 	 * is valid as new value for the shape. This method's response time should
 	 * be small since the method is queried after each change of the value in
 	 * the direct edit UI. The default implementation simply returns null to
 	 * indicate that all values are valid. In case of a not valid value, the
 	 * returned string shall indicate the reason why the value is not valid.
 	 * Corresponds to the method
 	 * {@link AbstractDirectEditingFeature#checkValueValid(String, IDirectEditingContext)}
 	 * .
 	 * 
 	 * @param value
 	 *            The new vlue to check
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 * @return <code>null</code> in case of a vlid value, a string describing
 	 *         the reason for being not valid otherwise.
 	 */
 	public String checkValueValid(String value, IDirectEditingContext context) {
 		return null;
 	}
 
 	/**
 	 * Can be overridden by clients to define completion functionality for
 	 * direct editing. Corresponds to
 	 * {@link AbstractDirectEditingFeature#completeValue(String, int, String, IDirectEditingContext)}
 	 * . The default implementation simply returns the parameter chosenValue.
 	 * 
 	 * @param value
 	 *            The current value
 	 * @param caretPosition
 	 *            The current cursor position
 	 * @param choosenValue
 	 *            The value chosen by user
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 * @return The new value
 	 */
 	public String completeValue(String value, int caretPos, String chosenValue, IDirectEditingContext context) {
 		return chosenValue;
 	}
 
 	/**
 	 * This value will be used if the cell editor is a combo box. This
 	 * functionality only applies to TYPE_DROPDOWN. Corresponds to the method
 	 * {@link AbstractDirectEditingFeature#getPossibleValues(IDirectEditingContext)}
 	 * . The default implementation returns an empty string array.
 	 * 
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 * @return The possible values for the combo box.
 	 */
 	public String[] getPossibleValues(IDirectEditingContext context) {
 		return EMPTY_STRING_ARRAY;
 	}
 
 	/**
 	 * This proposals will be used for the completion list of a simple text cell
 	 * editor. This functionality only applies to TYPE_TEXT. Corresponds to the
 	 * method
 	 * {@link AbstractDirectEditingFeature#getValueProposals(String, int, IDirectEditingContext)}
 	 * . The default implementation returns an empty string array.
 	 * 
 	 * @param value
 	 *            The current value
 	 * @param caretPosition
 	 *            The current cursor position
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 * @return The proposed values
 	 */
 	public String[] getValueProposals(String value, int caretPos, IDirectEditingContext context) {
 		return EMPTY_STRING_ARRAY;
 	}
 
 	/**
 	 * Checks if auto completion is enabled. This functionality only applies to
 	 * TYPE_TEXT. Corresponds to method
 	 * {@link AbstractDirectEditingFeature#isAutoCompletionEnabled()}. The
 	 * default implementation simply returns <code>false</code>.
 	 * 
 	 * @return <code>true</code>, if proposals should appear automatically,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isAutoCompletionEnabled() {
 		return false;
 	}
 
 	/**
 	 * Checks if completion is available. This functionality only applies to
 	 * TYPE_TEXT. Corresponds to method
 	 * {@link AbstractDirectEditingFeature#isCompletionAvailable()}. The default
 	 * implementation simply returns <code>false</code>.
 	 * 
 	 * @return <code>true</code> if completion is / proposals are available at
 	 *         all, <code>false</code> otherwise.
 	 */
 	public boolean isCompletionAvailable() {
 		return false;
 	}
 
 	/**
 	 * Defines if the input field should be streched to fit its contents. This
 	 * functionality applies to TYPE_TEXT, TYPE_DROPDOWN and
 	 * TYPE_DROPDOWN_READ_ONLY. Corresponds to method
 	 * {@link AbstractDirectEditingFeature#stretchFieldToFitText()}. The default
 	 * implementation simply returns <code>false</code>.
 	 * 
 	 * @return <code>true</code> if the field should exactly fit the contents,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean stretchFieldToFitText() {
 		return false;
 	}
 
 	/**
 	 * The Graphiti framework calls this method to decide which UI to show up
 	 * for direct editing. Corresponds to the method
 	 * {@link AbstractDirectEditingFeature#getEditingType()}. The default
 	 * implementation return {@link IDirectEditing#TYPE_NONE}, other valid type
 	 * are defined by the TYPE_* constants in {@link IDirectEditing}.
 	 * 
 	 * @return The desired editing type
 	 */
 	public int getEditingType() {
 		return IDirectEditing.TYPE_NONE;
 	}
 
 	/**
 	 * Provides the initial value for display in the newly opened text editing
 	 * UI component. Corresponds to the methdo
 	 * {@link AbstractDirectEditingFeature#getInitialValue(IDirectEditingContext)}
 	 * . The default implementation always returns an empty string.
 	 * 
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 * @return The initial string value to be displayed for editing by the user.
 	 */
 	public String getInitialValue(IDirectEditingContext context) {
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * Set the new value after direct editing is finished. The value comes from
 	 * the text editing UI component. Corresponds to the method
 	 * {@link AbstractDirectEditingFeature#setValue(String, IDirectEditingContext)}
 	 * . The default implementation does nothing.
 	 * 
 	 * @param value
 	 *            The new value to be set
 	 * @param context
 	 *            A context object describing the direct edit request.
 	 */
 	public void setValue(String value, IDirectEditingContext context) {
 	}
 
 	/**
 	 * The direct editing mode contains controls for code completion and the
 	 * selection from a combo box. In both cases the standard implementation
 	 * supports only strings.
 	 * <p>
 	 * If the client wants to work with Objects he must provide an
 	 * implementation of {@link IProposalSupport}. In this case the following
 	 * methods of the pattern are ignored:
 	 * <p>
 	 * <code>
 	 * <br>* String checkValueValid(String value, IDirectEditingContext context);
 	 * <br>* String completeValue(String value, int caretPosition, String choosenValue, IDirectEditingContext context);
 	 * <br>* String[] getPossibleValues(IDirectEditingContext context);
 	 * <br>* String[] getValueProposals(String value, int caretPosition, IDirectEditingContext context);
 	 * <br>* void setValue(String value, IDirectEditingContext context);  
 	 * </code><br>
 	 * Corresponds to the method
 	 * {@link AbstractDirectEditingFeature#getProposalSupport()}. The default
 	 * implementation returns <code>null</code> to enable the standard
 	 * string-based direct editing functionality.
 	 * 
 	 * @return The special implementation to support Objects in code completion
 	 *         and combo box
 	 * @since 0.8
 	 */
 	public IProposalSupport getProposalSupport() {
 		return null;
 	}
 
 	/**
 	 * Is queried by the framework after a pattern has been executed to find out
 	 * if this pattern should appear in the undo stack. By default all patterns
 	 * should appear there (see implementation in AbstractPattern), but single
 	 * pattern functionality may decide to override this behavior. Note that
 	 * this is a dynamic attribute of the pattern that is queried each time
 	 * <b>after</b> the pattern functionality has been executed.
 	 * <p>
 	 * <b>IMPORTANT NOTE:</b> The implementor of the feature is responsible for
 	 * correctly implementing this method! It will lead to inconsistencies if
 	 * this method returns <code>false</code> although the pattern did changes.
 	 * 
 	 * @param actionType
 	 *            the followings types are currently supported:
 	 *            <code>IDelete.class, IRemove.class</code>
 	 * 
 	 * 
 	 * @return <code>true</code> if the last action of the pattern from this
 	 *         action type should appear in the undo stack, <code>false</code>
 	 *         otherwise
 	 * 
 	 * @since 0.9
 	 */
 	public boolean hasDoneChanges(Class<?> actionType) {
 		boolean ret = true;
 		if (IDelete.class.equals(actionType)) {
 			if (wrappedDeleteFeature != null) {
 				ret = wrappedDeleteFeature.hasDoneChanges();
 			}
 		} else if (IRemove.class.equals(actionType)) {
 			if (wrappedRemoveFeature != null) {
 				ret = wrappedRemoveFeature.hasDoneChanges();
 			}
 		}
 		return ret;
 	}
 }
