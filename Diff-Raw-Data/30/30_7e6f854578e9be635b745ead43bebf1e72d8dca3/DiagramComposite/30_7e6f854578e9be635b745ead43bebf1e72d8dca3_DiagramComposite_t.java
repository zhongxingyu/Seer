 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2013 SRC
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    pjpaulin - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import java.util.EventObject;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 
 /**
  * An SWT composite that can display a Graphiti diagram. This implementation is
  * based on a custom {@link GraphicalComposite} class that works much like the
  * GEF GraphicalEditorWithFlyoutPalette class.
  * 
  * A DiagramComposite can be used anywhere a regular SWT Composite can. A
  * DiagramComposite can participate in a parent transaction or create one of
  * it's own. If the DiagramComposites is managing it's own transaction, it is up
  * to the enclosing workbench part to call the appropriate methods on the
  * composite.
  * 
  * @since 0.10
  */
 public class DiagramComposite extends GraphicalComposite implements DiagramEditor {
 
 	private DiagramSupport diagramSupport;
 
 	public DiagramComposite(IWorkbenchPart parentPart, Composite parent, int style) {
 		super(parentPart, parent, style);
 		this.diagramSupport = new DiagramSupport(this);
 		this.setEditDomain(new DefaultEditDomain(null));
 	}
 
 	public void setInput(IDiagramEditorInput input) {
 		TransactionalEditingDomain editingDomain = GraphitiUiInternal.getEmfService()
 				.createResourceSetAndEditingDomain();
 		this.setInput(editingDomain, input);
 	}
 
 	public void setInput(TransactionalEditingDomain editingDomain, IDiagramEditorInput input) {
 
 		// assign editing domain to update behavior
 		getUpdateBehavior().setEditingDomain(editingDomain);
 		this.diagramSupport.setInput(input);
 
 		if (this.diagramSupport.getEditorInitializationError() != null) {
 			this.diagramSupport.createErrorPartControl(this);
 			return;
 		}
 
 		getUpdateBehavior().init();
 		init();
 		this.diagramSupport.migrateDiagramModelIfNecessary();
 		this.diagramSupport.postCreatePartControl();
 	}
 
 	@Override
 	protected void initializeGraphicalViewer() {
 		super.initializeGraphicalViewer();
 		this.diagramSupport.initializeGraphicalViewer();
 	}
 
 	@Override
 	protected void createGraphicalViewer() {
 		this.diagramSupport.createGraphicalViewer(this);
 	}
 
 	@Override
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 		this.diagramSupport.configureGraphicalViewer();
 	}
 
 	public void refreshTitle() {
 		// String name = getConfigurationProvider().getDiagramTypeProvider()
 		// .getDiagramTitle();
 		// if (name == null || name.length() == 0) {
 		//			name = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
 		// }
 		// if (name == null || name.length() == 0) {
 		// name = URI.decode(getDiagramTypeProvider().getDiagram().eResource()
 		// .getURI().lastSegment());
 		// }
 		// setPartName(name);
 
 		/* TODO Need to figure out how to access this */
 		// this.getParentPart().setPartName("State View");
 	}
 
 	public void refreshTitleToolTip() {
 		/* TODO Need to figure out how to access this */
 		// this.parentPart.setTitleToolTip("Test Graphiti View");
 	}
 
 	public void updateDirtyState() {
 		// TODO Auto-generated method stub
 	}
 
 	public IWorkbenchPart getWorkbenchPart() {
 		return this.getParentPart();
 	}
 
 	public void shutdown() {
 		// TODO Auto-generated method stub
 	}
 
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
 		Object returnObj = this.diagramSupport.getAdapter(type);
 		if (returnObj != null)
 			return returnObj;
 		return super.getAdapter(type);
 	}
 
 	public void dispose() {
 		// unregister selection listener, registered during createPartControl()
 		if (this.getParentPart().getSite() != null && this.getParentPart().getSite().getPage() != null) {
 			this.getParentPart().getSite().getPage().removeSelectionListener(this);
 		}
 
 		this.diagramSupport.preSuperDispose();
 
 		RuntimeException exc = null;
 		try {
 			super.dispose();
 		} catch (RuntimeException e) {
 			exc = e;
 		}
 
 		this.diagramSupport.postSuperDispose();
 
 		if (exc != null) {
 			throw exc;
 		}
 	}
 
 	/**
 	 * Sets the focus by delegating to the super class implementation in the GEF
 	 * editor and additionally triggers a update of the diagram by delegating to
 	 * {@link DefaultUpdateBehavior#handleActivate()}.
 	 */
 	public boolean setFocus() {
 		if (getGraphicalViewer() == null) {
 			return false;
 		}
 
 		if (super.setFocus()) {
 			getUpdateBehavior().handleActivate();
 			return true;
 		}
 		return false;
 	}
 
 	/* Methods routed directly to DiagramSupport */
 
 	public final DefaultUpdateBehavior getUpdateBehavior() {
 		return this.diagramSupport.getUpdateBehavior();
 	}
 
 	public final DefaultRefreshBehavior getRefreshBehavior() {
 		return this.diagramSupport.getRefreshBehavior();
 	}
 
 	public void editingDomainInitialized() {
 		this.diagramSupport.editingDomainInitialized();
 	}
 
 	public void disableAdapters() {
 		this.diagramSupport.disableAdapters();
 	}
 
 	public void enableAdapters() {
 		this.diagramSupport.enableAdapters();
 	}
 
 	public boolean isDirty() {
 		return this.diagramSupport.isDirty();
 	}
 
 	protected final PaletteViewerProvider createPaletteViewerProvider() {
 		return this.diagramSupport.createPaletteViewerProvider();
 	}
 
 	protected final FlyoutPreferences getPalettePreferences() {
 		return this.diagramSupport.getPalettePreferences();
 	}
 
 	protected final PaletteRoot getPaletteRoot() {
 		return this.diagramSupport.getPaletteRoot();
 	}
 
 	public final void refreshPalette() {
 		this.diagramSupport.refreshPalette();
 	}
 
 	public void refresh() {
 		this.diagramSupport.refresh();
 	}
 
 	public void refreshContent() {
 		this.diagramSupport.refreshContent();
 	}
 
 	public void refreshRenderingDecorators(PictogramElement pe) {
 		this.diagramSupport.refreshRenderingDecorators(pe);
 	}
 
 	public PictogramElement[] getSelectedPictogramElements() {
 		return this.diagramSupport.getSelectedPictogramElements();
 	}
 
 	public void selectPictogramElements(PictogramElement[] pictogramElements) {
 		this.diagramSupport.selectPictogramElements(pictogramElements);
 	}
 
 	public void setPictogramElementForSelection(PictogramElement pictogramElement) {
 		this.diagramSupport.setPictogramElementForSelection(pictogramElement);
 	}
 
 	public void setPictogramElementsForSelection(PictogramElement pictogramElements[]) {
 		this.diagramSupport.setPictogramElementsForSelection(pictogramElements);
 	}
 
 	public void selectBufferedPictogramElements() {
 		this.diagramSupport.selectBufferedPictogramElements();
 	}
 
 	public Point getMouseLocation() {
 		return this.diagramSupport.getMouseLocation();
 	}
 
 	public Point calculateRealMouseLocation(Point nativeLocation) {
 		return this.diagramSupport.calculateRealMouseLocation(nativeLocation);
 	}
 
 	public IConfigurationProvider getConfigurationProvider() {
 		return this.diagramSupport.getConfigurationProvider();
 	}
 
 	public EditPart getContentEditPart() {
 		return this.diagramSupport.getContentEditPart();
 	}
 
 	public IDiagramTypeProvider getDiagramTypeProvider() {
 		return this.diagramSupport.getDiagramTypeProvider();
 	}
 
 	public GraphicalEditPart getEditPartForPictogramElement(PictogramElement pe) {
 		return this.diagramSupport.getEditPartForPictogramElement(pe);
 	}
 
 	public IFigure getFigureForPictogramElement(PictogramElement pe) {
 		return this.diagramSupport.getFigureForPictogramElement(pe);
 	}
 
 	public double getZoomLevel() {
 		return this.diagramSupport.getZoomLevel();
 	}
 
 	public boolean isAlive() {
 		return this.diagramSupport.isAlive();
 	}
 
 	public boolean isDirectEditingActive() {
 		return this.diagramSupport.isDirectEditingActive();
 	}
 
 	public void setDirectEditingActive(boolean directEditingActive) {
 		this.diagramSupport.setDirectEditingActive(directEditingActive);
 	}
 
 	public TransactionalEditingDomain getEditingDomain() {
 		return this.diagramSupport.getEditingDomain();
 	}
 
 	public Object executeFeature(IFeature feature, IContext context) {
 		return this.diagramSupport.executeFeature(feature, context);
 	}
 
 	public ResourceSet getResourceSet() {
 		return this.diagramSupport.getResourceSet();
 	}
 
 	public IDiagramEditorInput getDiagramEditorInput() {
 		return this.diagramSupport.getInput();
 	}
 
 	/* GEF methods that need to be part of the IGEFDiagramContainer interface. */
 
 	public void setGEFEditDomain(DefaultEditDomain editDomain) {
 		this.setEditDomain(editDomain);
 	}
 
 	public void initializeGEFGraphicalViewer() {
 		this.initializeGraphicalViewer();
 	}
 
 	public ActionRegistry getActionRegistry() {
 		return super.getActionRegistry();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public List getSelectionActions() {
 		return super.getSelectionActions();
 	}
 
 	public SelectionSynchronizer getSelectionSynchronizer() {
 		return super.getSelectionSynchronizer();
 	}
 
 	public void commandStackChanged(EventObject event) {
 		super.commandStackChanged(event);
 	}
 
 	public void setGraphicalViewer(GraphicalViewer viewer) {
 		super.setGraphicalViewer(viewer);
 	}
 
 	public void hookGraphicalViewer() {
 		super.hookGraphicalViewer();
 	}
 
 	public IWorkbenchPartSite getSite() {
 		return this.getWorkbenchPart().getSite();
 	}
 
 	public String getTitle() {
 		return this.getWorkbenchPart().getTitle();
 	}
 
 	public void addPropertyListener(IPropertyListener listener) {
 		this.getWorkbenchPart().addPropertyListener(listener);
 	}
 
 	public void removePropertyListener(IPropertyListener listener) {
 		this.getWorkbenchPart().removePropertyListener(listener);
 	}
 
 	public boolean isLocalEditingDomain() {
 		return false;
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		this.diagramSupport.getPersistencyBehavior().saveDiagram(monitor);
 	}
 }
