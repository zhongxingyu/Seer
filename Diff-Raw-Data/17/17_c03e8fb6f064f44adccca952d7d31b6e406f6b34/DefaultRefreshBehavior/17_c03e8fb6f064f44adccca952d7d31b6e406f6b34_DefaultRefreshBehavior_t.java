 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2011, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Bug 336488 - DiagramEditor API
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.draw2d.Figure;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IDirectEditingInfo;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.internal.pref.GFPreferences;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.internal.editor.RefreshPerformanceCache;
 import org.eclipse.graphiti.ui.internal.parts.IPictogramElementDelegate;
 import org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart;
 import org.eclipse.graphiti.ui.internal.parts.IShapeEditPart;
 import org.eclipse.graphiti.ui.internal.parts.ShapeEditPart;
 import org.eclipse.swt.widgets.Display;
 
 /**
 * The default implementation for the {@link DiagramEditor} behavior extension
 * that controls the refresh behavior of the Graphiti diagram editor. Clients
 * may subclass to change the behavior; use
 * {@link DiagramEditor#createRefreshBehavior()} to return the instance that
 * shall be used.<br>
 * Note that there is always a 1:1 relation with a {@link DiagramEditor}.
  * 
  * @since 0.9
  */
 public class DefaultRefreshBehavior {
 
 	/**
	 * The associated {@link DiagramEditor}. Set on construction of this class.
 	 */
 	protected final DiagramEditor diagramEditor;
 
 	private RefreshPerformanceCache refreshPerformanceCache = new RefreshPerformanceCache();
 
 	/**
 	 * Creates a new standard refresh behaviour for a Graphiti diagram editor.
 	 * The passed {@link DiagramEditor} is closely linked to this instance (1:1
 	 * relation) and both instances will have a common lifecycle.
 	 * 
 	 * @param diagramEditor
 	 *            The associated {@link DiagramEditor}.
 	 */
 	public DefaultRefreshBehavior(DiagramEditor diagramEditor) {
 		super();
 		this.diagramEditor = diagramEditor;
 	}
 
 	/**
 	 * Initializes the performance cache and is called by the Graphiti framework
 	 * before a refresh is triggered. Should not be called by clients.
 	 * 
 	 * @noreference This method is not intended to be referenced by clients.
 	 * @since 0.9
 	 */
 	public void initRefresh() {
 		getRefreshPerformanceCache().initRefresh();
 	}
 
 	/**
 	 * Handles the auto update at startup of the editor and is called by the
 	 * Graphiti {@link DiagramEditor} when the input is set (
 	 * {@link DiagramEditor#setInput(org.eclipse.ui.IEditorInput)}). The default
 	 * implementation checks the desired behavior as defined in
 	 * {@link IDiagramTypeProvider#isAutoUpdateAtStartup()} and calls
 	 * {@link #autoUpdate(Diagram, IDiagramTypeProvider)} in case an update
 	 * shall be done.
 	 * 
 	 * @since 0.9
 	 */
 	protected void handleAutoUpdateAtStartup() {
 		IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 		if (diagramTypeProvider.isAutoUpdateAtStartup()) {
 			autoUpdate();
 		}
 	}
 
 	/**
 	 * Handles the auto update at rest of the editor (the editor performs a
 	 * reload of the EMF resources because e.g. the underlying file has been
 	 * changed by another editor) and is called by the Graphiti
 	 * {@link DiagramEditor} after the {@link Diagram} has been reloaded. The
 	 * default implementation checks the desired behavior as defined in
 	 * {@link IDiagramTypeProvider#isAutoUpdateAtReset()} and calls
 	 * {@link #autoUpdate(Diagram, IDiagramTypeProvider)} in case an update
 	 * shall be done.
 	 * 
 	 * @since 0.9
 	 */
 	protected void handleAutoUpdateAtReset() {
 		IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 		if (diagramTypeProvider.isAutoUpdateAtReset()) {
 			autoUpdate();
 		}
 	}
 
 	/**
 	 * Handles the auto update of the editor and triggers an update of the
 	 * {@link Diagram} by delegating to the {@link IUpdateFeature} regsitered
 	 * for the {@link Diagram}. In the end {@link #refresh()} is called to
 	 * reflect the update in the editor UI. This method is called by
 	 * {@link #handleAutoUpdateAtStartup()} and
 	 * {@link #handleAutoUpdateAtReset()}.
 	 * 
 	 * @since 0.9
 	 */
 	protected void autoUpdate() {
 		IDiagramTypeProvider diagramTypeProvider = diagramEditor.getDiagramTypeProvider();
 		Diagram diagram = diagramTypeProvider.getDiagram();
 		IFeatureProvider featureProvider = diagramTypeProvider.getFeatureProvider();
 		IUpdateContext updateCtx = new UpdateContext(diagram);
 		featureProvider.updateIfPossible(updateCtx);
 		refresh();
 	}
 
 	/**
 	 * Does the refresh of the editor, so that the UI reflects the current state
 	 * of the Graphiti pictograms model. Also the editor title is updated; in
 	 * case direct editing is active it is cancelled.
 	 */
 	public void refresh() {
 		if (!diagramEditor.isAlive()) {
 			return;
 		}
 
 		if (Display.getCurrent() == null) {
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					refresh();
 				}
 			});
 			return;
 		}
 
 		if (GFPreferences.getInstance().isCPUProfilingTraceActive()) {
 			if (T.racer().info()) {
 				T.racer().info("DiagramEditorInternal.refresh()"); //$NON-NLS-1$
 			}
 		}
 
 		long start = System.currentTimeMillis();
 
 		final EditPart contentEditPart = diagramEditor.getContentEditPart();
 		if (contentEditPart == null) {
 			return;
 		}
 
 		internalRefreshEditPart(contentEditPart);
 
 		diagramEditor.refreshTitle();
 
 		long stop = System.currentTimeMillis();
 		long time = (stop - start);
 		if (time > 500) {
 			String output = "refresh took " + time + " ms."; //$NON-NLS-1$ //$NON-NLS-2$
 			T.racer().warning("DiagramEditorInternal.refresh() ", output); //$NON-NLS-1$
 		}
 
 		// prove if switch to direct editing is required
 		IDirectEditingInfo dei = diagramEditor.getConfigurationProvider().getFeatureProvider().getDirectEditingInfo();
 		if (dei.isActive()) {
 			EditPart editPart = (EditPart) diagramEditor.getGraphicalViewer().getEditPartRegistry()
 					.get(dei.getMainPictogramElement());
 			if (editPart instanceof ShapeEditPart) {
 				ShapeEditPart shapeEditPart = (ShapeEditPart) editPart;
 				shapeEditPart.switchToDirectEditingMode(dei.getPictogramElement(), dei.getGraphicsAlgorithm());
 				// reset values
 				dei.reset();
 			}
 		}
 		diagramEditor.selectBufferedPictogramElements();
 	}
 
 	/**
	 * Refreshes the given {@link PictogramElement} so that the UI reflects the
 	 * current state in the Graphiti pictograms model.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} to refresh
 	 */
 	protected void refresh(PictogramElement pe) {
 		if (pe == null || !pe.isActive()) {
 			return;
 		}
 		GraphicalEditPart editPart = diagramEditor.getEditPartForPictogramElement(pe);
 		if (editPart != null && editPart instanceof IPictogramElementEditPart) {
 			IPictogramElementEditPart ep = (IPictogramElementEditPart) editPart;
 			IPictogramElementDelegate delegate = ep.getPictogramElementDelegate();
 			delegate.setForceRefresh(true);
 			editPart.refresh();
 			delegate.setForceRefresh(false);
 		}
 	}
 
 	/**
 	 * Refreshes all rendering decorators for the given {@link PictogramElement}
 	 * as defined in
 	 * {@link IToolBehaviorProvider#getDecorators(PictogramElement)}.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} to refresh the decorators for
 	 */
 	public void refreshRenderingDecorators(PictogramElement pe) {
 		GraphicalEditPart ep = diagramEditor.getEditPartForPictogramElement(pe);
 		if (ep instanceof IShapeEditPart) {
 			IShapeEditPart sep = (IShapeEditPart) ep;
 			sep.refreshDecorators();
 		}
 	}
 
 	/**
 	 * Internal refresh of a given {@link EditPart}.
 	 * 
 	 * @param editPart
 	 *            the edit part to refresh
 	 * @noreference This method is not intended to be referenced by clients.
 	 */
 	public void internalRefreshEditPart(final EditPart editPart) {
 		if (Display.getCurrent() == null) {
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					internalRefreshEditPart(editPart);
 					// refreshOutline();
 				}
 			});
 			return;
 		}
 
 		long start = System.currentTimeMillis();
 
 		try {
 			diagramEditor.getConfigurationProvider().getContextButtonManager().hideContextButtonsInstantly();
 
 			editPart.refresh();
 
 			long stop = System.currentTimeMillis();
 			long time = (stop - start);
 			if (time > 500) {
 				String output = "refreshEditPart took " + time + " ms."; //$NON-NLS-1$ //$NON-NLS-2$
 				T.racer().warning("DiagramEditorInternal.refreshEditPart() ", output); //$NON-NLS-1$
 			}
 		} catch (NullPointerException e) {
 			T.racer().error("refresh edit part problem", e); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns if auto refresh is enabled or not. In case it is enabled the
 	 * editor will automatically react on EMF resource changes.
 	 * <p>
 	 * The default implementation here simply returns <code>true</code>.
 	 * 
 	 * @return true, if is auto refresh
 	 */
 	public boolean isAutoRefresh() {
 		return true;
 	}
 
 	/**
 	 * Returns if multiple refreshes shall be omitted and a bundled refresh
 	 * should happen instead. Is called by the framework on creation and
 	 * refreshing of {@link Figure}s.
 	 * <p>
 	 * The default implementation simply returns <code>true</code>. <b>Note:</b>
 	 * returning false here might have large performance implications, so use
 	 * this option only with extra care!
 	 * 
 	 * @return
 	 */
 	public boolean isMultipleRefreshSupressionActive() {
 		return true;
 	}
 
 	/**
 	 * Checks the performance cache if a refresh shall be triggered for the
 	 * given object. Should not be called by external clients.
 	 * 
 	 * @noreference This method is not intended to be referenced by clients.
 	 * @since 0.9
 	 */
 	public boolean shouldRefresh(Object obj) {
 		return getRefreshPerformanceCache().shouldRefresh(obj);
 	}
 
 	private RefreshPerformanceCache getRefreshPerformanceCache() {
 		return refreshPerformanceCache;
 	}
 }
