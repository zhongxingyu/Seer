 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
 *    mwenz - Bug 363464: Return IReason to pass on information if layout has been performed
  *
  * </copyright>
  *
  *******************************************************************************/
 /*
  * Created on 13.07.2005
  */
 package org.eclipse.graphiti.features.impl;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IProgress;
import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.context.IAreaContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.ILinkService;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.util.IColorConstant;
 
 /**
  * Abstract base class for all features. Prefer extending this class over
  * inheriting interface {@link org.eclipse.graphiti.features.IFeature}.
  */
 public abstract class AbstractFeature implements IFeature {
 
 	private IFeatureProvider fp;
 
 	private IProgress progressCallback;
 
 	/**
 	 * Creates a new {@link AbstractFeature}.
 	 * 
 	 * @param fp
 	 *            the fp
 	 */
 	public AbstractFeature(IFeatureProvider fp) {
 		super();
 		this.fp = fp;
 	}
 
 	public boolean canUndo(IContext context) {
 		return true;
 	}
 
 	public boolean hasDoneChanges() {
 		return true;
 	}
 
 	public String getDescription() {
 		return toString();
 	}
 
 	public IFeatureProvider getFeatureProvider() {
 		return this.fp;
 	}
 
 	public String getName() {
 		return this.getClass().getName();
 	}
 
 	public boolean isAvailable(IContext context) {
 		return true;
 	}
 
 	/**
 	 * Sets the progress callback.
 	 * 
 	 * @param progress
 	 *            the new progress callback
 	 */
 	public void setProgressCallback(IProgress progress) {
 		this.progressCallback = progress;
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 
 	/**
 	 * Adds the graphical representation.
 	 * 
 	 * @param context
 	 *            the context
 	 * @param newObject
 	 *            the new object
 	 * 
 	 * @return the added pictogram element
 	 */
 	protected PictogramElement addGraphicalRepresentation(IAreaContext context, Object newObject) {
 		return getFeatureProvider().addIfPossible(new AddContext(context, newObject));
 	}
 
 	/**
 	 * Gets the all business objects for pictogram element.
 	 * 
 	 * @param pe
 	 *            the pe
 	 * 
 	 * @return the all business objects for pictogram element
 	 */
 	protected Object[] getAllBusinessObjectsForPictogramElement(PictogramElement pe) {
 		return getFeatureProvider().getAllBusinessObjectsForPictogramElement(pe);
 	}
 
 	/**
 	 * Gets the business object for pictogram element.
 	 * 
 	 * @param pe
 	 *            the pe
 	 * 
 	 * @return the business object for pictogram element
 	 */
 	protected Object getBusinessObjectForPictogramElement(PictogramElement pe) {
 		return getFeatureProvider().getBusinessObjectForPictogramElement(pe);
 	}
 
 	/**
 	 * Gets the diagram.
 	 * 
 	 * @return the diagram
 	 */
 	protected Diagram getDiagram() {
 		return getFeatureProvider().getDiagramTypeProvider().getDiagram();
 	}
 
 	/**
 	 * Gets the diagram editor.
 	 * 
 	 * @return the diagram editor
 	 */
 	protected IDiagramEditor getDiagramEditor() {
 		return getFeatureProvider().getDiagramTypeProvider().getDiagramEditor();
 	}
 
 	/**
 	 * Gets the progress callback.
 	 * 
 	 * @return the progress callback
 	 */
 	protected IProgress getProgressCallback() {
 		return this.progressCallback;
 	}
 
 	/**
 	 * Gets the user decision.
 	 * 
 	 * @return the user decision
 	 */
 	protected boolean getUserDecision() {
 		return true;
 	}
 
 	/**
	 * Layouts the given {@link PictogramElement}. This implementation asks the
 	 * feature provider for available layout features and processes the first
 	 * one.
 	 * 
 	 * @param pe
	 *            the {@link PictogramElement} to layout
	 * @return a {@link IReason} object that indicates if a layout operation has
	 *         been performed ({@link IReason#toBoolean()} is <code>true</code>)
	 * @since 0.9 this method returns now the {@link IReason} object, before it
	 *        simply returned null
 	 */
	protected IReason layoutPictogramElement(PictogramElement pe) {
 		LayoutContext context = new LayoutContext(pe);
		return getFeatureProvider().layoutIfPossible(context);
 	}
 
 	/**
 	 * Links the given {@link PictogramElement} to the given business or domain
 	 * object. After doing that, the domain object can be retrieved from the
 	 * {@link PictogramElement} using the methods
 	 * {@link #getBusinessObjectForPictogramElement(PictogramElement)} and
 	 * {@link #getAllBusinessObjectsForPictogramElement(PictogramElement)}; also
 	 * {@link ILinkService#getPictogramElements(Diagram, org.eclipse.emf.ecore.EObject)}
 	 * and
 	 * {@link ILinkService#getPictogramElements(Diagram, java.util.List, boolean)}
 	 * will return the {@link PictogramElement} for the domain object.<br>
 	 * Note: Identity of domain or business objects is determined by default
 	 * using the method
 	 * {@link EcoreUtil#equals(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)}
 	 * , which relies on the compared EMF objects having different IDs or
 	 * attributes. The way Graphiti used to compare EMF business objects can be
 	 * changed by overriding
 	 * {@link IToolBehaviorProvider#equalsBusinessObjects(Object, Object)}.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} to link to the domain object
 	 * @param businessObject
 	 *            the business object to link to the {@link PictogramElement}
 	 */
 	protected void link(PictogramElement pe, Object businessObject) {
 		link(pe, new Object[] { businessObject });
 	}
 
 	/**
 	 * Links the given {@link PictogramElement} to the given business or domain
 	 * objects. After doing that, the domain object can be retrieved from the
 	 * {@link PictogramElement} using the methods
 	 * {@link #getBusinessObjectForPictogramElement(PictogramElement)} and
 	 * {@link #getAllBusinessObjectsForPictogramElement(PictogramElement)}; also
 	 * {@link ILinkService#getPictogramElements(Diagram, org.eclipse.emf.ecore.EObject)}
 	 * and
 	 * {@link ILinkService#getPictogramElements(Diagram, java.util.List, boolean)}
 	 * will return the {@link PictogramElement} for the domain object.<br>
 	 * Note: Identity of domain or business objects is determined by default
 	 * using the method
 	 * {@link EcoreUtil#equals(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)}
 	 * , which relies on the compared EMF objects having different IDs or
 	 * attributes. The way Graphiti used to compare EMF business objects can be
 	 * changed by overriding
 	 * {@link IToolBehaviorProvider#equalsBusinessObjects(Object, Object)}.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} to link to the domain object
 	 * @param businessObjects
 	 *            the business objects to link to the {@link PictogramElement}
 	 */
 	protected void link(PictogramElement pe, Object businessObjects[]) {
 		getFeatureProvider().link(pe, businessObjects);
 	}
 
 	/**
 	 * Manage color.
 	 * 
 	 * @param colorConstant
 	 *            the color constant
 	 * 
 	 * @return the color
 	 */
 	protected Color manageColor(IColorConstant colorConstant) {
 		return Graphiti.getGaService().manageColor(getDiagram(), colorConstant);
 	}
 
 	/**
 	 * Manage color.
 	 * 
 	 * @param red
 	 *            the red
 	 * @param green
 	 *            the green
 	 * @param blue
 	 *            the blue
 	 * 
 	 * @return the color
 	 */
 	protected Color manageColor(int red, int green, int blue) {
 		return Graphiti.getGaService().manageColor(getDiagram(), red, green, blue);
 	}
 
 	/**
 	 * Updates the given pictogram element. This implementation asks the feature
 	 * provider for available update features and processes the first one.
 	 * 
 	 * @param pe
 	 *            the pe
 	 */
 	protected void updatePictogramElement(PictogramElement pe) {
 		UpdateContext context = new UpdateContext(pe);
 		getFeatureProvider().updateIfPossible(context);
 	}
 }
