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
  *    pjpaulin - Bug 352120 - Now uses IDiagramContainerUI interface
  *    mwenz - Bug 394315 - Enable injecting behavior objects in DiagramEditor
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.swt.widgets.Composite;
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
  * @experimental This API is in an experimental state and should be used by
  *               clients only with care, as it not final and can be removed or
  *               changed without prior notice!
  */
 public class DiagramComposite extends GraphicalComposite implements IDiagramContainerUI {
 
 	private DiagramBehavior diagramBehavior;
 	private IWorkbenchPart ownedPart;
 
 	public DiagramComposite(IWorkbenchPart ownedPart, Composite parent, int style) {
 		super(parent, style);
 		this.ownedPart = ownedPart;
 		setEditDomain(new DefaultEditDomain(null));
 	}
 
 	public DiagramComposite(Composite parent, int style) {
 		this(null, parent, style);
 	}
 
	protected DiagramBehavior createDiagramBehavior(IWorkbenchPart parentPart) {
		DiagramBehavior diagramBehavior = new DiagramBehavior(this);
		diagramBehavior.setParentPart(parentPart);
		diagramBehavior.initDefaultBehaviors();

		return diagramBehavior;
 	}
 
 	public void setInput(IDiagramEditorInput input) {
 		TransactionalEditingDomain editingDomain = GraphitiUiInternal.getEmfService()
 				.createResourceSetAndEditingDomain();
 		this.setInput(editingDomain, input);
 	}
 
 	public void setInput(TransactionalEditingDomain editingDomain, IDiagramEditorInput input) {
 		if (diagramBehavior == null) {
			diagramBehavior = createDiagramBehavior(ownedPart);
 		}
 
 		// assign editing domain to update behavior
 		getUpdateBehavior().setEditingDomain(editingDomain);
 		this.diagramBehavior.setInput(input);
 
 		if (this.diagramBehavior.getEditorInitializationError() != null) {
 			this.diagramBehavior.createErrorPartControl(this);
 			return;
 		}
 
 		diagramBehavior.getUpdateBehavior().init();
 		init();
 		this.diagramBehavior.migrateDiagramModelIfNecessary();
 		this.diagramBehavior.addGefListeners();
 	}
 
 	@Override
 	public void initializeGraphicalViewer() {
 		super.initializeGraphicalViewer();
 		this.diagramBehavior.initializeGraphicalViewer();
 	}
 
 	@Override
 	protected void createGraphicalViewer() {
 		this.diagramBehavior.createGraphicalViewer(this);
 	}
 
 	@Override
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 		this.diagramBehavior.configureGraphicalViewer();
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
 
 	@Override
 	public IWorkbenchPart getWorkbenchPart() {
 		return diagramBehavior.getParentPart();
 	}
 
 	public void close() {
 		// TODO Auto-generated method stub
 	}
 
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
 		Object returnObj = this.diagramBehavior.getAdapter(type);
 		if (returnObj != null)
 			return returnObj;
 		return super.getAdapter(type);
 	}
 
 	public void dispose() {
 		// unregister selection listener, registered during createPartControl()
 		if (getWorkbenchPart() != null && getWorkbenchPart().getSite() != null
 				&& getWorkbenchPart().getSite().getPage() != null) {
 			getWorkbenchPart().getSite().getPage().removeSelectionListener(this);
 		}
 
 		if (diagramBehavior != null) {
 			diagramBehavior.disposeBeforeGefDispose();
 		}
 
 		RuntimeException exc = null;
 		try {
 			super.dispose();
 		} catch (RuntimeException e) {
 			exc = e;
 		}
 
 		if (diagramBehavior != null) {
 			diagramBehavior.disposeAfterGefDispose();
 		}
 
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
 			diagramBehavior.getUpdateBehavior().handleActivate();
 			return true;
 		}
 		return false;
 	}
 
 	/* Methods routed directly to DiagramBehavior */
 
 	public final DefaultUpdateBehavior getUpdateBehavior() {
 		return this.diagramBehavior.getUpdateBehavior();
 	}
 
 	public final DefaultRefreshBehavior getRefreshBehavior() {
 		return this.diagramBehavior.getRefreshBehavior();
 	}
 
 	public void editingDomainInitialized() {
 		this.diagramBehavior.editingDomainInitialized();
 	}
 
 	public void disableAdapters() {
 		this.diagramBehavior.disableAdapters();
 	}
 
 	public void enableAdapters() {
 		this.diagramBehavior.enableAdapters();
 	}
 
 	public boolean isDirty() {
 		return this.diagramBehavior.isDirty();
 	}
 
 	protected final PaletteViewerProvider createPaletteViewerProvider() {
 		return this.diagramBehavior.createPaletteViewerProvider();
 	}
 
 	protected final FlyoutPreferences getPalettePreferences() {
 		return this.diagramBehavior.getPalettePreferences();
 	}
 
 	protected final PaletteRoot getPaletteRoot() {
 		return this.diagramBehavior.getPaletteRoot();
 	}
 
 	public PictogramElement[] getSelectedPictogramElements() {
 		return this.diagramBehavior.getSelectedPictogramElements();
 	}
 
 	public void selectPictogramElements(PictogramElement[] pictogramElements) {
 		this.diagramBehavior.selectPictogramElements(pictogramElements);
 	}
 
 	public void setPictogramElementForSelection(PictogramElement pictogramElement) {
 		this.diagramBehavior.setPictogramElementForSelection(pictogramElement);
 	}
 
 	public void setPictogramElementsForSelection(PictogramElement pictogramElements[]) {
 		this.diagramBehavior.setPictogramElementsForSelection(pictogramElements);
 	}
 
 	public IDiagramTypeProvider getDiagramTypeProvider() {
 		return this.diagramBehavior.getDiagramTypeProvider();
 	}
 
 	public GraphicalEditPart getEditPartForPictogramElement(PictogramElement pe) {
 		return this.diagramBehavior.getEditPartForPictogramElement(pe);
 	}
 
 	public double getZoomLevel() {
 		return this.diagramBehavior.getZoomLevel();
 	}
 
 	public boolean isAlive() {
 		return this.diagramBehavior.isAlive();
 	}
 
 	public boolean isDirectEditingActive() {
 		return this.diagramBehavior.isDirectEditingActive();
 	}
 
 	public void setDirectEditingActive(boolean directEditingActive) {
 		this.diagramBehavior.setDirectEditingActive(directEditingActive);
 	}
 
 	public IDiagramEditorInput getDiagramEditorInput() {
 		return this.diagramBehavior.getInput();
 	}
 
 	/* Methods from container interface */
 
 	public IWorkbenchPartSite getSite() {
 		return getWorkbenchPart().getSite();
 	}
 
 	public String getTitle() {
 		return getWorkbenchPart().getTitle();
 	}
 
 	public String getTitleToolTip() {
 		return getWorkbenchPart().getTitleToolTip();
 	}
 
 	public void doSave(IProgressMonitor monitor) {
 		this.diagramBehavior.getPersistencyBehavior().saveDiagram(monitor);
 	}
 
 	public DiagramBehavior getDiagramBehavior() {
 		return diagramBehavior;
 	}
 }
