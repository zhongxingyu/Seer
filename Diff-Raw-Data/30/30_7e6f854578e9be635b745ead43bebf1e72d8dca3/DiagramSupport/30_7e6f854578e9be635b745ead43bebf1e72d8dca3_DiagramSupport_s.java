 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2011, 2013 Modular Mind, Ltd.
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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.ContextMenuProvider;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.KeyHandler;
 import org.eclipse.gef.KeyStroke;
 import org.eclipse.gef.SnapToGeometry;
 import org.eclipse.gef.SnapToGrid;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CommandStackEventListener;
 import org.eclipse.gef.editparts.GridLayer;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.AlignmentAction;
 import org.eclipse.gef.ui.actions.DirectEditAction;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.actions.MatchHeightAction;
 import org.eclipse.gef.ui.actions.MatchWidthAction;
 import org.eclipse.gef.ui.actions.ToggleGridAction;
 import org.eclipse.gef.ui.actions.ZoomInAction;
 import org.eclipse.gef.ui.actions.ZoomOutAction;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
 import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.graphiti.DiagramScrollingBehavior;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IPrintFeature;
 import org.eclipse.graphiti.features.ISaveImageFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ISaveImageContext;
 import org.eclipse.graphiti.features.context.impl.SaveImageContext;
 import org.eclipse.graphiti.internal.command.AddFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.FeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.internal.action.CopyAction;
 import org.eclipse.graphiti.ui.internal.action.DeleteAction;
 import org.eclipse.graphiti.ui.internal.action.FeatureExecutionHandler;
 import org.eclipse.graphiti.ui.internal.action.PasteAction;
 import org.eclipse.graphiti.ui.internal.action.PrintGraphicalViewerAction;
 import org.eclipse.graphiti.ui.internal.action.RemoveAction;
 import org.eclipse.graphiti.ui.internal.action.SaveImageAction;
 import org.eclipse.graphiti.ui.internal.action.ToggleContextButtonPadAction;
 import org.eclipse.graphiti.ui.internal.action.UpdateAction;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.config.ConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProviderInternal;
 import org.eclipse.graphiti.ui.internal.contextbuttons.ContextButtonManagerForPad;
 import org.eclipse.graphiti.ui.internal.contextbuttons.IContextButtonManager;
 import org.eclipse.graphiti.ui.internal.dnd.GFTemplateTransferDropTargetListener;
 import org.eclipse.graphiti.ui.internal.dnd.ObjectsTransferDropTargetListener;
 import org.eclipse.graphiti.ui.internal.editor.DiagramChangeListener;
 import org.eclipse.graphiti.ui.internal.editor.DomainModelChangeListener;
 import org.eclipse.graphiti.ui.internal.editor.GFCommandStack;
 import org.eclipse.graphiti.ui.internal.editor.GFFigureCanvas;
 import org.eclipse.graphiti.ui.internal.editor.GFScrollingGraphicalViewer;
 import org.eclipse.graphiti.ui.internal.editor.GraphitiScrollingGraphicalViewer;
 import org.eclipse.graphiti.ui.internal.util.gef.ScalableRootEditPartAnimated;
 import org.eclipse.graphiti.ui.platform.IConfigurationProvider;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.commands.ActionHandler;
 import org.eclipse.jface.util.TransferDropTargetListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 /**
  * Provides the common functionality needed to display and manage diagrams.
  * 
  * Diagrams can be displayed either in a simple SWT {@link Composite} or in an
  * {@link IEditorPart}, so it's not possible to provide common fucntionality
  * through sub-classing.
  * 
  * @since 0.10
  */
 public class DiagramSupport {
 
 	private DiagramEditor container;
 
 	private final DefaultUpdateBehavior updateBehavior;
 	private final DefaultPaletteBehavior paletteBehaviour;
 	private final DefaultPersistencyBehavior persistencyBehavior;
 	private final DefaultMarkerBehavior markerBehavior;
 	private final DefaultRefreshBehavior refreshBehavior;
 
 	private PictogramElement pictogramElementsForSelection[];
 	private IConfigurationProviderInternal configurationProvider;
 	private Point mouseLocation;
 	private KeyHandler keyHandler;
 	private DiagramScrollingBehavior diagramScrollingBehavior;
 	private boolean directEditingActive = false;
 	private CommandStackEventListener gefCommandStackListener;
 
 	private DiagramChangeListener diagramChangeListener;
 	private DomainModelChangeListener domainModelListener;
 
 	private IDiagramEditorInput diagramEditorInput;
 
 	private String editorInitializationError = null;
 
 	DiagramSupport(DiagramEditor container) {
 		this.container = container;
 		markerBehavior = createMarkerBehavior();
 		updateBehavior = createUpdateBehavior();
 		paletteBehaviour = createPaletteBehaviour();
 		persistencyBehavior = createPersistencyBehavior();
 		refreshBehavior = createRefreshBehavior();
 
 	}
 
 	void selectPictogramElements(PictogramElement[] pictogramElements) {
 		List<EditPart> editParts = new ArrayList<EditPart>();
 		Map<?, ?> editPartRegistry = this.container.getGraphicalViewer().getEditPartRegistry();
 		if (editPartRegistry != null) {
 			for (int i = 0; i < pictogramElements.length; i++) {
 				PictogramElement pe = pictogramElements[i];
 				Object obj = editPartRegistry.get(pe);
 				/*
 				 * Add all EditParts to a list to be selected. Bug 324556: Only
 				 * add EditParts that allow selection to the list, e.g.
 				 * invisible objects will cause an IllegalArgumentException in
 				 * AbstractEditPart.setSelected (GEF) when setSelected is
 				 * called.
 				 */
 				if (obj instanceof EditPart && ((EditPart) obj).isSelectable()) {
 					editParts.add((EditPart) obj);
 				}
 			}
 			this.container.getWorkbenchPart().getSite().getSelectionProvider()
 					.setSelection(new StructuredSelection(editParts));
 			if (editParts.size() > 0) {
 				final EditPart editpart = editParts.get(0);
 				// if the editPart is newly created it is possible that his
 				// figure has not a valid bounds. Hence we have to wait for
 				// the UI update (for the validation of the figure tree).
 				// Otherwise the reveal method can't work correctly.
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						container.getGraphicalViewer().reveal(editpart);
 					}
 				});
 			}
 		}
 	}
 
 	PictogramElement[] getSelectedPictogramElements() {
 		PictogramElement pe[] = new PictogramElement[0];
 		ISelectionProvider selectionProvider = this.container.getWorkbenchPart().getSite().getSelectionProvider();
 		if (selectionProvider != null) {
 			ISelection s = selectionProvider.getSelection();
 			if (s instanceof IStructuredSelection) {
 				IStructuredSelection sel = (IStructuredSelection) s;
 				List<PictogramElement> list = new ArrayList<PictogramElement>();
 				for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
 					Object o = iter.next();
 					if (o instanceof EditPart) {
 						EditPart editPart = (EditPart) o;
 						if (editPart.getModel() instanceof PictogramElement) {
 							list.add((PictogramElement) editPart.getModel());
 						}
 					}
 				}
 				pe = list.toArray(new PictogramElement[0]);
 			}
 		}
 		return pe;
 	}
 
 	void setPictogramElementForSelection(PictogramElement pictogramElement) {
 		if (pictogramElement == null) {
 			this.pictogramElementsForSelection = null;
 		} else {
 			this.pictogramElementsForSelection = new PictogramElement[] { pictogramElement };
 		}
 	}
 
 	void setPictogramElementsForSelection(PictogramElement[] pictogramElements) {
 		this.pictogramElementsForSelection = pictogramElements;
 	}
 
 	ResourceSet getResourceSet() {
 		ResourceSet ret = null;
 		TransactionalEditingDomain editingDomain = this.container.getEditingDomain();
 		if (editingDomain != null) {
 			ret = editingDomain.getResourceSet();
 		}
 		return ret;
 	}
 
 	IDiagramTypeProvider getDiagramTypeProvider() {
 		IConfigurationProvider cfgProvider = getConfigurationProvider();
 		if (cfgProvider != null)
 			return cfgProvider.getDiagramTypeProvider();
 		return null;
 	}
 
 	TransactionalEditingDomain getEditingDomain() {
 		return this.getUpdateBehavior().getEditingDomain();
 	}
 
 	void refresh() {
 		this.getRefreshBehavior().refresh();
 
 	}
 
 	boolean isDirty() {
 		TransactionalEditingDomain editingDomain = this.container.getEditingDomain();
 		// Check that the editor is not yet disposed
 		if (editingDomain != null && editingDomain.getCommandStack() != null) {
 			return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 		}
 		return false;
 	}
 
 	void refreshRenderingDecorators(PictogramElement pe) {
 		this.getRefreshBehavior().refreshRenderingDecorators(pe);
 	}
 
 	void refreshPalette() {
 		this.getPaletteBehavior().refreshPalette();
 	}
 
 	Object executeFeature(IFeature feature, IContext context) {
 		Object returnValue = null;
 
 		DefaultEditDomain domain = this.container.getEditDomain();
 
 		// Make sure the editor is valid
 		Assert.isNotNull(domain);
 		CommandStack commandStack = domain.getCommandStack();
 
 		// Create the correct feature command
 		FeatureCommandWithContext featureCommand = null;
 		if (feature instanceof IAddFeature) {
 			// Context must fit to the feature
 			Assert.isTrue(context instanceof IAddContext);
 			featureCommand = new AddFeatureCommandWithContext(feature, context);
 		} else {
 			featureCommand = new GenericFeatureCommandWithContext(feature, context);
 		}
 
 		// Execute the feature using the command
 		GefCommandWrapper commandWrapper = new GefCommandWrapper(featureCommand, this.container.getEditingDomain());
 		commandStack.execute(commandWrapper);
 
 		if (featureCommand instanceof AddFeatureCommandWithContext) {
 			// In case of an add feature, select the newly added shape
 			PictogramElement addedPictogramElement = ((AddFeatureCommandWithContext) featureCommand)
 					.getAddedPictogramElements();
 			if (addedPictogramElement != null) {
 				setPictogramElementForSelection(addedPictogramElement);
 			}
 
 			// Store the added pictogram element as return value
 			returnValue = addedPictogramElement;
 		}
 
 		return returnValue;
 	}
 
 	void disableAdapters() {
 		markerBehavior.disableProblemIndicationUpdate();
 		updateBehavior.setAdapterActive(false);
 	}
 
 	void enableAdapters() {
 		markerBehavior.enableProblemIndicationUpdate();
 		updateBehavior.setAdapterActive(true);
 	}
 
 	boolean isAlive() {
 		IConfigurationProvider cp = getConfigurationProvider();
 		if (cp != null) {
 			TransactionalEditingDomain editingDomain = this.container.getEditingDomain();
 			Diagram diagram = cp.getDiagram();
 			if (editingDomain != null && GraphitiInternal.getEmfService().isObjectAlive(diagram)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void selectBufferedPictogramElements() {
 		if (getPictogramElementsForSelection() != null) {
 			selectPictogramElements(getPictogramElementsForSelection());
 			setPictogramElementsForSelection(null);
 		}
 	}
 
 	void refreshContent() {
 		Diagram currentDiagram = getDiagramTypeProvider().getDiagram();
 		if (GraphitiInternal.getEmfService().isObjectAlive(currentDiagram)) {
 			refresh();
 		} else {
 			IDiagramEditorInput diagramEditorInput = this.container.getDiagramEditorInput();
 			// resolve diagram in reloaded resource
 			Diagram diagram = this.getPersistencyBehavior().loadDiagram(diagramEditorInput.getUri());
 			IDiagramTypeProvider diagramTypeProvider = getConfigurationProvider().getDiagramTypeProvider();
 			diagramTypeProvider.resourceReloaded(diagram);
 			// clean performance hashtables which have references
 			this.getRefreshBehavior().initRefresh();
 			// to old proxies
 			setPictogramElementsForSelection(null);
 			// create new edit parts
 			this.container.getGraphicalViewer().setContents(diagram);
 			this.getRefreshBehavior().handleAutoUpdateAtReset();
 		}
 	}
 
 	void editingDomainInitialized() {
 		markerBehavior.initialize();
 	}
 
 	Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
 		IConfigurationProvider cfgProvider = getConfigurationProvider();
 
 		if (cfgProvider != null) {
 			IToolBehaviorProvider tbp = cfgProvider.getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 			if (tbp != null) {
 				Object ret = tbp.getAdapter(type);
 				if (ret != null) {
 					return ret;
 				}
 			}
 		}
 
 		GraphicalViewer viewer = this.container.getGraphicalViewer();
 		if (type == ZoomManager.class && viewer != null) {
 			return viewer.getProperty(ZoomManager.class.toString());
 		}
 
 		if (type == IPropertySheetPage.class) {
 			if (cfgProvider != null && this.container instanceof ITabbedPropertySheetPageContributor) {
 				ITabbedPropertySheetPageContributor contributor = (ITabbedPropertySheetPageContributor) this.container;
 				if (contributor.getContributorId() != null) {
 					return new TabbedPropertySheetPage(contributor);
 				}
 			}
 			return null; // not yet initialized
 		}
 
 		if (type == Diagram.class) {
 			return getDiagramTypeProvider().getDiagram();
 		}
 		if (type == KeyHandler.class) {
 			return getCommonKeyHandler();
 		}
 		if (type == SelectionSynchronizer.class) {
 			return this.container.getSelectionSynchronizer();
 		}
 		if (type == IContextButtonManager.class) {
 			return ((IConfigurationProviderInternal) getConfigurationProvider()).getContextButtonManager();
 		}
 		if (type == IDiagramEditor.class) {
 			return this;
 		}
 
 		return null;
 	}
 
 	IConfigurationProvider getConfigurationProvider() {
 		return this.configurationProvider;
 	}
 
 	EditPart getContentEditPart() {
 		if (this.container.getGraphicalViewer() != null) {
 			return this.container.getGraphicalViewer().getContents();
 		}
 		return null;
 	}
 
 	GraphicalEditPart getEditPartForPictogramElement(PictogramElement pe) {
 		Map<?, ?> editPartRegistry = this.container.getGraphicalViewer().getEditPartRegistry();
 		if (editPartRegistry != null) {
 			Object obj = editPartRegistry.get(pe);
 			if (obj instanceof GraphicalEditPart) {
 				GraphicalEditPart ep = (GraphicalEditPart) obj;
 				return ep;
 			}
 		}
 		return null;
 	}
 
 	Point getMouseLocation() {
 		if (mouseLocation == null) {
 			mouseLocation = new Point();
 		}
 		return mouseLocation;
 	}
 
 	DefaultUpdateBehavior getUpdateBehavior() {
 		return this.updateBehavior;
 	}
 
 	Point calculateRealMouseLocation(Point nativeLocation) {
 		Point ret = new Point(nativeLocation);
 		Point viewLocation;
 		// view location depends on the current scroll bar position
 		if (getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			viewLocation = getGFFigureCanvas().getViewport().getViewLocation();
 		} else {
 			viewLocation = getFigureCanvas().getViewport().getViewLocation();
 		}
 
 		ret.x += viewLocation.x;
 		ret.y += viewLocation.y;
 
 		ZoomManager zoomManager = (ZoomManager) this.container.getGraphicalViewer().getProperty(
 				ZoomManager.class.toString());
 		ret = ret.getScaled(1 / zoomManager.getZoom());
 
 		return ret;
 	}
 
 	DefaultRefreshBehavior getRefreshBehavior() {
 		return this.refreshBehavior;
 	}
 
 	boolean isDirectEditingActive() {
 		return directEditingActive;
 	}
 
 	void setDirectEditingActive(boolean directEditingActive) {
 		this.directEditingActive = directEditingActive;
 		((IConfigurationProviderInternal) this.getConfigurationProvider()).getContextButtonManager()
 				.hideContextButtonsInstantly();
 	}
 
 	double getZoomLevel() {
 		ZoomManager zoomManager = (ZoomManager) getAdapter(ZoomManager.class);
 		if (zoomManager == null)
 			return 1;
 
 		/*
 		 * avoid long running calculations for large diagrams and zoom factors
 		 * below 5%
 		 */
 		return Math.max(0.05D, zoomManager.getZoom());
 	}
 
 	IFigure getFigureForPictogramElement(PictogramElement pe) {
 		GraphicalEditPart ep = getEditPartForPictogramElement(pe);
 		if (ep != null) {
 			return ep.getFigure();
 		}
 		return null;
 	}
 
 	DefaultPersistencyBehavior getPersistencyBehavior() {
 		return this.persistencyBehavior;
 	}
 
 	DefaultPaletteBehavior getPaletteBehavior() {
 		return this.paletteBehaviour;
 	}
 
 	DefaultMarkerBehavior getMarkerBehavior() {
 		return this.markerBehavior;
 	}
 
 	/**
 	 * Creates the behavior extension that deals with markers. See
 	 * {@link DefaultMarkerBehavior} for details and the default implementation.
 	 * Override to change the marker behavior.
 	 * 
 	 * @return a new instance of {@link DefaultMarkerBehavior}
 	 */
 	DefaultMarkerBehavior createMarkerBehavior() {
 		return new DefaultMarkerBehavior(this.container);
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the update handling. See
 	 * {@link DefaultUpdateBehavior} for details and the default implementation.
 	 * Override to change the update behavior.
 	 * 
 	 * @return a new instance of {@link DefaultUpdateBehavior}
 	 */
 	DefaultUpdateBehavior createUpdateBehavior() {
 		return new DefaultUpdateBehavior(this.container);
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the palette handling. See
 	 * {@link DefaultPaletteBehavior} for details and the default
 	 * implementation. Override to change the palette behavior.
 	 * 
 	 * @return a new instance of {@link DefaultPaletteBehavior}
 	 */
 	DefaultPaletteBehavior createPaletteBehaviour() {
 		return new DefaultPaletteBehavior(this.container);
 	}
 
 	/**
 	 * Delegates to the method (or the method in a subclass of)
 	 * {@link DefaultPaletteBehavior#createPaletteViewerProvider()
 	 * #createPaletteViewerProvider()} to create the
 	 * {@link PaletteViewerProvider} used inside the GEF editor.
 	 * 
 	 * @return the {@link PaletteViewerProvider} to use
 	 */
 	final PaletteViewerProvider createPaletteViewerProvider() {
 		if (editorInitializationError != null) {
 			// Editor input is erroneous, show error page instead of diagram and
 			// do not initialize the palette to avoid exceptions
 			return null;
 		}
 		return paletteBehaviour.createPaletteViewerProvider();
 	}
 
 	/**
 	 * Delegates to the method (or the method in a subclass of)
 	 * {@link DefaultPaletteBehavior#getPalettePreferences()}. To change the
 	 * palette override the behavior there.
 	 * 
 	 * @return the {@link PaletteViewerProvider} preferences to use.
 	 */
 	final FlyoutPreferences getPalettePreferences() {
 		return this.getPaletteBehavior().getPalettePreferences();
 	}
 
 	/**
 	 * Returns the {@link PaletteRoot} to use in the GEF editor by delegating to
 	 * {@link DefaultPaletteBehavior#getPaletteRoot()}.
 	 * 
 	 * @return the {@link PaletteRoot} to use
 	 */
 	final PaletteRoot getPaletteRoot() {
 		return this.getPaletteBehavior().getPaletteRoot();
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the persistence handling.
 	 * See {@link DefaultPersistencyBehavior} for details and the default
 	 * implementation. Override to change the persistence behavior.
 	 * 
 	 * @return a new instance of {@link DefaultPersistencyBehavior}
 	 */
 	DefaultPersistencyBehavior createPersistencyBehavior() {
 		return new DefaultPersistencyBehavior(this.container);
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the refresh handling. See
 	 * {@link DefaultRefreshBehavior} for details and the default
 	 * implementation. Override to change the refresh behavior.
 	 * 
 	 * @return a new instance of {@link DefaultRefreshBehavior}
 	 */
 	DefaultRefreshBehavior createRefreshBehavior() {
 		return new DefaultRefreshBehavior(this.container);
 	}
 
 	/**
 	 * Returns the {@link PictogramElement}s that are set for later selection.
 	 * <p>
 	 * The methods {@link #getPictogramElementsForSelection()},
 	 * {@link #setPictogramElementForSelection(PictogramElement)},
 	 * {@link #setPictogramElementsForSelection(PictogramElement[])} and
 	 * {@link #selectBufferedPictogramElements()} offer the possibility to use a
 	 * deferred selection mechanism: via the setters, {@link PictogramElement}s
 	 * can be stored for a selection operation that is triggered lateron during
 	 * a general refresh via the method
 	 * {@link #selectBufferedPictogramElements()}. This mechanism is used e.g.
 	 * in the Graphiti framework in direct editing to restore the previous
 	 * selection, but can also be used by clients.
 	 * 
 	 * @return the {@link PictogramElement}s stored for later selection
 	 */
 	PictogramElement[] getPictogramElementsForSelection() {
 		return pictogramElementsForSelection;
 	}
 
 	void setConfigurationProvider(IConfigurationProviderInternal configurationProvider) {
 		this.configurationProvider = configurationProvider;
 
 		// initialize configuration-provider depending on this editor
 		this.configurationProvider.setWorkbenchPart(this.container.getWorkbenchPart());
 
 		if (this.container.getGraphicalViewer() != null)
 			this.container.initializeGEFGraphicalViewer();
 
 		if (this.container instanceof IEditorPart) {
 			DefaultEditDomain editDomain = new DefaultEditDomain((IEditorPart) this.container);
 			this.container.setGEFEditDomain(editDomain);
 		}
 
 		CommandStack commandStack = new GFCommandStack(configurationProvider, getEditingDomain());
 		this.container.getEditDomain().setCommandStack(commandStack);
 	}
 
 	void initializeGraphicalViewer() {
 
 		// register Actions
 		IFeatureProvider featureProvider = getConfigurationProvider().getDiagramTypeProvider().getFeatureProvider();
 		if (featureProvider != null) {
 			IPrintFeature pf = featureProvider.getPrintFeature();
 			if (pf != null) {
 				registerAction(new PrintGraphicalViewerAction(this.container.getWorkbenchPart(), pf));
 			}
 		}
 
 		// setting ContextMenuProvider
 		ContextMenuProvider contextMenuProvider = createContextMenuProvider();
 		if (contextMenuProvider != null) {
 			this.container.getGraphicalViewer().setContextMenu(contextMenuProvider);
 			// the registration allows an extension of the context-menu by other
 			// plugins
 			if (shouldRegisterContextMenu()) {
 				this.container.getWorkbenchPart().getSite()
 						.registerContextMenu(contextMenuProvider, this.container.getGraphicalViewer());
 			}
 		}
 
 		// set contents
 		this.container.getGraphicalViewer().setEditPartFactory(
 				((IConfigurationProviderInternal) getConfigurationProvider()).getEditPartFactory());
 		this.container.getGraphicalViewer().setContents(getConfigurationProvider().getDiagram());
 
 		getPaletteBehavior().initializeViewer();
 
 		this.container.getGraphicalViewer().getControl().addMouseMoveListener(new MouseMoveListener() {
 
 			public void mouseMove(MouseEvent e) {
 				setMouseLocation(e.x, e.y);
 			}
 		});
 
 		this.container.getGraphicalViewer()
 				.addDropTargetListener(
 						(TransferDropTargetListener) new ObjectsTransferDropTargetListener(this.container
 								.getGraphicalViewer()));
 
 		this.container.getGraphicalViewer().addDropTargetListener(
 				new GFTemplateTransferDropTargetListener(this.container.getGraphicalViewer(), this.container));
 
 	}
 
 	private void setMouseLocation(int x, int y) {
 		getMouseLocation().setLocation(x, y);
 	}
 
 	/**
 	 * Returns a new {@link ContextMenuProvider}. Clients can return null, if no
 	 * context-menu shall be displayed.
 	 * 
 	 * @return A new instance of {@link ContextMenuProvider}.
 	 */
 	ContextMenuProvider createContextMenuProvider() {
 		return new DiagramEditorContextMenuProvider(this.container.getGraphicalViewer(),
 				this.container.getActionRegistry(), getDiagramTypeProvider());
 	}
 
 	/**
 	 * Allows subclasses to prevent that the diagram context menu should be
 	 * registered for extensions at Eclipse. By default others can provide
 	 * extensions to the menu (default return value of this method is
 	 * <code>true</code>). By returning <code>false</code> any extension of the
 	 * context menu can be prevented.
 	 * <p>
 	 * For details see Bugzilla 345347
 	 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=345347).
 	 * 
 	 * @return <code>true</code> in case extensions shall be allowed (default),
 	 *         <code>false</code> otherwise.
 	 * @since 0.9
 	 */
 	boolean shouldRegisterContextMenu() {
 		return true;
 	}
 
 	/**
 	 * Registers the given action with the Eclipse {@link ActionRegistry}.
 	 * 
 	 * @param action
 	 *            the action to register
 	 * @since 0.9
 	 */
 	void registerAction(IAction action) {
 		if (action == null) {
 			return;
 		}
 		this.container.getActionRegistry().registerAction(action);
 
 		if (action.getActionDefinitionId() != null) {
 			IHandlerService hs = (IHandlerService) this.container.getWorkbenchPart().getSite()
 					.getService(IHandlerService.class);
 			hs.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
 		}
 
 		@SuppressWarnings("unchecked")
 		List<String> selectionActions = this.container.getSelectionActions();
 		selectionActions.add(action.getId());
 	}
 
 	/**
 	 * Initializes the action registry with the predefined actions (update,
 	 * remove, delete, copy, paste, zooming, direct editing, alignment and
 	 * toggling actions for the diagram grip and hiding of the context button
 	 * pad.
 	 * 
 	 * @param zoomManager
 	 *            the GEF zoom manager to use
 	 */
 	void initActionRegistry(ZoomManager zoomManager) {
 		final ActionRegistry actionRegistry = this.container.getActionRegistry();
 		@SuppressWarnings("unchecked")
 		final List<String> selectionActions = this.container.getSelectionActions();
 
 		// register predefined actions (e.g. update, remove, delete, ...)
 		IAction action = new UpdateAction(this.container.getWorkbenchPart(), getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new RemoveAction(this.container.getWorkbenchPart(), getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new DeleteAction(this.container.getWorkbenchPart(), getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new CopyAction(this.container.getWorkbenchPart(), getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new PasteAction(this.container.getWorkbenchPart(), getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		IFeatureProvider fp = getConfigurationProvider().getDiagramTypeProvider().getFeatureProvider();
 		if (fp != null) {
 			ISaveImageFeature sf = fp.getSaveImageFeature();
 
 			if (sf != null) {
 				ISaveImageContext context = new SaveImageContext();
 				action = new SaveImageAction(sf, context, this.container);
 				actionRegistry.registerAction(action);
 				selectionActions.add(action.getId());
 			}
 		}
 
 		registerAction(new ZoomInAction(zoomManager));
 		registerAction(new ZoomOutAction(zoomManager));
 		registerAction(new DirectEditAction(this.container.getWorkbenchPart()));
 
 		registerAction(new AlignmentAction(this.container.getWorkbenchPart(), PositionConstants.LEFT));
 		registerAction(new AlignmentAction(this.container.getWorkbenchPart(), PositionConstants.RIGHT));
 		registerAction(new AlignmentAction(this.container.getWorkbenchPart(), PositionConstants.TOP));
 		registerAction(new AlignmentAction(this.container.getWorkbenchPart(), PositionConstants.BOTTOM));
 		registerAction(new AlignmentAction(this.container.getWorkbenchPart(), PositionConstants.CENTER));
 		registerAction(new AlignmentAction(this.container.getWorkbenchPart(), PositionConstants.MIDDLE));
 		registerAction(new MatchWidthAction(this.container.getWorkbenchPart()));
 		registerAction(new MatchHeightAction(this.container.getWorkbenchPart()));
 		IAction showGrid = new ToggleGridAction(this.container.getGraphicalViewer());
 		this.container.getActionRegistry().registerAction(showGrid);
 
 		// Bug 323351: Add button to toggle a flag if the context pad buttons
 		// shall be shown or not
 		IAction toggleContextButtonPad = new ToggleContextButtonPadAction(this.container);
 		toggleContextButtonPad.setChecked(false);
 		actionRegistry.registerAction(toggleContextButtonPad);
 		// End bug 323351
 
 		IHandlerService hs = (IHandlerService) this.container.getWorkbenchPart().getSite()
 				.getService(IHandlerService.class);
 		hs.activateHandler(FeatureExecutionHandler.COMMAND_ID, new FeatureExecutionHandler(getConfigurationProvider()));
 	}
 
 	/**
 	 * Returns the KeyHandler with common bindings to be used for both the
 	 * Outline and the Graphical Viewer.
 	 * 
 	 * @return The KeyHandler with common bindings for both the Outline and the
 	 *         Graphical Viewer.
 	 */
 	KeyHandler getCommonKeyHandler() {
 		if (keyHandler == null) {
 			keyHandler = new KeyHandler();
 			keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
 					this.container.getActionRegistry().getAction(ActionFactory.DELETE.getId()));
 			keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, SWT.SHIFT),
 					this.container.getActionRegistry().getAction(RemoveAction.ACTION_ID));
 			keyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
 					this.container.getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
 			keyHandler.put(KeyStroke.getPressed('c', SWT.CTRL),
 					this.container.getActionRegistry().getAction(ActionFactory.COPY.getId()));
 			keyHandler.put(KeyStroke.getPressed('v', SWT.CTRL),
 					this.container.getActionRegistry().getAction(ActionFactory.PASTE.getId()));
 			// _keyHandler.put(KeyStroke.getPressed((char) 1, 'a', SWT.CTRL),
 			// getActionRegistry().getAction(ActionFactory.SELECT_ALL.getId()));
 		}
 		return keyHandler;
 	}
 
 	void configureGraphicalViewer() {
 
 		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) this.container.getGraphicalViewer();
 
 		ScalableRootEditPartAnimated rootEditPart = new ScalableRootEditPartAnimated(viewer) {
 
 			protected GridLayer createGridLayer() {
 				return new org.eclipse.graphiti.ui.internal.util.draw2d.GridLayer(
 						(IConfigurationProviderInternal) getConfigurationProvider());
 			}
 
 		};
 
 		// configure ZoomManager
 		viewer.setRootEditPart(rootEditPart); // support
 
 		// animation of the zoom
 		ZoomManager zoomManager = rootEditPart.getZoomManager();
 		List<String> zoomLevels = new ArrayList<String>(3);
 		zoomLevels.add(ZoomManager.FIT_ALL);
 		zoomLevels.add(ZoomManager.FIT_WIDTH);
 		zoomLevels.add(ZoomManager.FIT_HEIGHT);
 		zoomManager.setZoomLevelContributions(zoomLevels);
 		IToolBehaviorProvider toolBehaviorProvider = getConfigurationProvider().getDiagramTypeProvider()
 				.getCurrentToolBehaviorProvider();
 		zoomManager.setZoomLevels(toolBehaviorProvider.getZoomLevels());
 
 		this.initActionRegistry(zoomManager);
 
 		// set the keyhandler.
 		viewer.setKeyHandler((new GraphicalViewerKeyHandler(viewer)).setParent(getCommonKeyHandler()));
 
 		// settings for grid and guides
 		Diagram diagram = getConfigurationProvider().getDiagram();
 
 		boolean snapToGrid = diagram.isSnapToGrid();
 		int horizontalGridUnit = diagram.getGridUnit();
 		int verticalGridUnit = diagram.getVerticalGridUnit();
 		if (verticalGridUnit == -1) {
 			// No vertical grid unit set (or old diagram before 0.8): use
 			// vertical grid unit
 			verticalGridUnit = horizontalGridUnit;
 		}
 		boolean gridVisisble = (horizontalGridUnit > 0) && (verticalGridUnit > 0);
 
 		viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, new Boolean(gridVisisble));
 		viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, new Boolean(snapToGrid));
 		viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING, new Dimension(horizontalGridUnit, verticalGridUnit));
 		viewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, toolBehaviorProvider.isShowGuides());
 
 		// context button manager
 		IConfigurationProviderInternal configurationProvider = (IConfigurationProviderInternal) this
 				.getConfigurationProvider();
 		configurationProvider.setContextButtonManager(new ContextButtonManagerForPad(this.container,
 				configurationProvider.getResourceRegistry()));
 
 		/* sw: make scroll bars always visible */
 		if (getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			GFFigureCanvas figureCanvas = getGFFigureCanvas();
 			if (figureCanvas != null) {
 				figureCanvas.setScrollBarVisibility(FigureCanvas.ALWAYS);
 			}
 		}
 	}
 
 	/**
 	 * Gets the diagram scrolling behavior.
 	 * 
 	 * @return the diagram scrolling behavior
 	 * @deprecated Scroll bar based infinite canvas is a workaround for GEF
 	 *             limitations.
 	 * 
 	 * @see DefaultToolBehaviorProvider#getDiagramScrollingBehavior()
 	 * @since 0.9
 	 */
 	@Deprecated
 	DiagramScrollingBehavior getDiagramScrollingBehavior() {
 		if (this.diagramScrollingBehavior == null) {
 			IToolBehaviorProvider tbp = getConfigurationProvider().getDiagramTypeProvider()
 					.getCurrentToolBehaviorProvider();
 			this.diagramScrollingBehavior = tbp.getDiagramScrollingBehavior();
 		}
 		return this.diagramScrollingBehavior;
 	}
 
 	private FigureCanvas getFigureCanvas() {
 		GraphicalViewer viewer = this.container.getGraphicalViewer();
 		if (viewer != null) {
 			Control control = viewer.getControl();
 			if (control instanceof FigureCanvas) {
 				return (FigureCanvas) control;
 			}
 		}
 		return null;
 	}
 
 	private GFFigureCanvas getGFFigureCanvas() {
 		GraphicalViewer viewer = this.container.getGraphicalViewer();
 		if (viewer != null) {
 			Control control = viewer.getControl();
 			if (control instanceof GFFigureCanvas) {
 				return (GFFigureCanvas) control;
 			}
 		}
 		return null;
 	}
 
 	void postCreatePartControl() {
 		getDiagramTypeProvider().postInit();
 		gefCommandStackListener = new CommandStackEventListener() {
 
 			public void stackChanged(CommandStackEvent event) {
 				// Only fire if triggered from UI thread
 				if (Display.getCurrent() != null) {
 					container.updateDirtyState();
 
 					// Promote the changes to the command stack also to the
 					// action bars and registered actions to correctly reflect
 					// e.g. undo/redo in the menu (introduced to enable removing
 					// NOP commands from the command stack
 					container.commandStackChanged(event);
 				}
 			}
 		};
 		this.container.getEditDomain().getCommandStack().addCommandStackEventListener(gefCommandStackListener);
 	}
 
 	CommandStackEventListener getGefCommandStackListener() {
 		return this.gefCommandStackListener;
 	}
 
 	/**
 	 * Hook to unregister the listeners for diagram changes.
 	 * 
 	 * @see #registerDiagramResourceSetListener()
 	 */
 	void unregisterDiagramResourceSetListener() {
 		if (diagramChangeListener != null) {
 			diagramChangeListener.stopListening();
 			TransactionalEditingDomain eDomain = this.container.getEditingDomain();
 			eDomain.removeResourceSetListener(diagramChangeListener);
 		}
 	}
 
 	/**
 	 * Hook that is called to unregister the listeners for changes of the
 	 * business objects (domain objects).
 	 * 
 	 * @see DiagramEditorImpl#registerBusinessObjectsListener()
 	 */
 	void unregisterBusinessObjectsListener() {
 		if (domainModelListener != null) {
 			TransactionalEditingDomain eDomain = this.container.getEditingDomain();
 			eDomain.removeResourceSetListener(domainModelListener);
 		}
 	}
 
 	/**
 	 * Hook to register listeners for diagram changes. The listener will be
 	 * notified with all events and has to filter for the ones regarding the
 	 * diagram.<br>
 	 * Note that additional listeners registered here should also be
 	 * unregistered in {@link #unregisterDiagramResourceSetListener()}.
 	 */
 	void registerDiagramResourceSetListener() {
 		diagramChangeListener = new DiagramChangeListener(this.container);
 		TransactionalEditingDomain eDomain = this.container.getEditingDomain();
 		eDomain.addResourceSetListener(diagramChangeListener);
 	}
 
 	/**
 	 * Hook that is called to register listeners for changes of the business
 	 * objects (domain objects) in the resource set of the editor. The default
 	 * implementation registers the {@link DomainModelChangeListener}.<br>
 	 * Note that additional listeners registered here should also be
 	 * unregistered in {@link #unregisterBusinessObjectsListener()}.
 	 */
 	void registerBusinessObjectsListener() {
 		domainModelListener = new DomainModelChangeListener(this.container);
 		TransactionalEditingDomain eDomain = this.container.getEditingDomain();
 		eDomain.addResourceSetListener(domainModelListener);
 	}
 
 	void setInput(IDiagramEditorInput input) {
 		// Check the input
 		if (input == null) {
 			throw new IllegalArgumentException("The IEditorInput must not be null"); //$NON-NLS-1$
 		}
 		if (!(input instanceof IDiagramEditorInput)) {
 			throw new IllegalArgumentException("The IEditorInput has the wrong type: " + input.getClass()); //$NON-NLS-1$
 		}
 
 		diagramEditorInput = (IDiagramEditorInput) input;
 		Diagram diagram = this.getPersistencyBehavior().loadDiagram(diagramEditorInput.getUri());
 
 		// can happen if editor is started with invalid URI
 		if (diagram == null) {
 			editorInitializationError = "No Diagram found for URI '" + diagramEditorInput.getUri().toString(); //$NON-NLS-1$
 			return;
 		}
 
 		String providerId = diagramEditorInput.getProviderId();
 		// if provider is null then take the first installed provider for
 		// this diagram type
 		if (providerId == null) {
 			providerId = GraphitiUi.getExtensionManager().getDiagramTypeProviderId(diagram.getDiagramTypeId());
 			diagramEditorInput.setProviderId(providerId);
 		}
 
 		if (providerId == null) {
 			editorInitializationError = "DiagramEditorInput does not convey a Provider ID '" + diagramEditorInput; //$NON-NLS-1$
 			return;
 		}
 
 		Assert.isNotNull(providerId, "DiagramEditorInput does not convey a Provider ID '" + diagramEditorInput //$NON-NLS-1$
 				+ "'. . See the error log for details."); //$NON-NLS-1$
 
 		// get according diagram-type-provider
 		// Get according diagram-type-provider
 		IDiagramTypeProvider diagramTypeProvider = GraphitiUi.getExtensionManager().createDiagramTypeProvider(
 				providerId);
 		if (diagramTypeProvider == null) {
 			editorInitializationError = "Could not find diagram type provider for " + diagram.getDiagramTypeId(); //$NON-NLS-1$
 			return;
 		}
 
 		diagramTypeProvider.init(diagram, this.container);
 		IConfigurationProviderInternal configurationProvider = new ConfigurationProvider(this.container,
 				diagramTypeProvider);
 		this.setConfigurationProvider(configurationProvider);
 		this.getRefreshBehavior().handleAutoUpdateAtStartup();
 
 		registerBusinessObjectsListener();
 		registerDiagramResourceSetListener();
 
 		this.container.refreshTitle();
 	}
 
 	IDiagramEditorInput getInput() {
 		return this.diagramEditorInput;
 	}
 
 	/**
 	 * Disposes this {@link DiagramEditorImpl} instance and frees all used
 	 * resources and clears all references. Also delegates to all the behavior
 	 * extensions to also free their resources (e.g. and most important is the
 	 * {@link TransactionalEditingDomain} held by the
 	 * {@link DefaultPersistencyBehavior}. Always delegate to
 	 * <code>super.dispose()</code> in case you override this method!
 	 */
 	void preSuperDispose() {
 		unregisterDiagramResourceSetListener();
 		unregisterBusinessObjectsListener();
 
 		if (getConfigurationProvider() != null) {
 			getConfigurationProvider().dispose();
 		}
 
 		if (paletteBehaviour != null) {
 			paletteBehaviour.dispose();
 		}
 
 		markerBehavior.dispose();
 
 		// unregister selection listener, registered during createPartControl()
 		if (this.container instanceof ISelectionListener) {
 			if (this.container.getSite() != null && this.container.getSite().getPage() != null) {
 				this.container.getSite().getPage().removeSelectionListener((ISelectionListener) this.container);
 			}
 		}
 
 		if (this.container.getEditDomain() != null && this.container.getEditDomain().getCommandStack() != null) {
 			this.container.getEditDomain().getCommandStack().removeCommandStackEventListener(gefCommandStackListener);
 			this.container.getEditDomain().getCommandStack().dispose();
 		}
 
 		DefaultUpdateBehavior behavior = getUpdateBehavior();
 		behavior.dispose();
 	}
 
 	void postSuperDispose() {
 		if (this.container.getEditDomain() != null) {
 			this.container.getEditDomain().setCommandStack(null);
 		}
 
 	}
 
 	/**
 	 * We provide migration from 0.8.0 to 0.9.0. You can override if you want to
 	 * migrate manually. WARNING: If your diagram is under version control, this
 	 * method can cause a check out dialog to be opened etc.
 	 * 
 	 * @since 0.9
 	 */
 	void migrateDiagramModelIfNecessary() {
 		final Diagram diagram = getDiagramTypeProvider().getDiagram();
 		if (Graphiti.getMigrationService().shouldMigrate080To090(diagram)) {
 			this.container.getEditingDomain().getCommandStack()
 					.execute(new RecordingCommand(this.container.getEditingDomain()) {
 						@Override
 						protected void doExecute() {
 							Graphiti.getMigrationService().migrate080To090(diagram);
 						}
 					});
 		}
 	}
 
 	void createErrorPartControl(Composite parent) {
 		Display display = parent.getDisplay();
 
 		// Define colors as desired, in high contrast mode use system defaults
 		Color backgroundColor;
 		final Color foregroundColor;
 		final Color separatorColor;
 		if (display.getHighContrast()) {
 			backgroundColor = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
 			foregroundColor = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
 			separatorColor = foregroundColor;
 		} else {
 			backgroundColor = display.getSystemColor(SWT.COLOR_WHITE);
 			foregroundColor = display.getSystemColor(SWT.COLOR_DARK_BLUE);
 			separatorColor = new Color(display, 152, 170, 203);
 		}
 
 		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
 		scrolledComposite.setAlwaysShowScrollBars(false);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 		scrolledComposite.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				if (separatorColor != foregroundColor) {
 					// Dispose the color only if it was created as additional
 					// color
 					separatorColor.dispose();
 				}
 			}
 		});
 
 		Composite composite = new Composite(scrolledComposite, SWT.NONE);
 		composite.setBackground(backgroundColor);
 		composite.setLayout(new GridLayout());
 
 		Composite separator = new Composite(composite, SWT.NO_FOCUS);
 		separator.setBackground(separatorColor);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.heightHint = 2;
 		data.verticalIndent = 50;
 		separator.setLayoutData(data);
 
 		StyledText widget = new StyledText(composite, SWT.READ_ONLY | SWT.MULTI);
 		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		widget.setText(editorInitializationError);
 		widget.setBackground(backgroundColor);
 		widget.setForeground(foregroundColor);
 		widget.setCaret(null);
 
 		scrolledComposite.setContent(composite);
 		scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 	}
 
 	/**
 	 * Creates the GraphicalViewer on the specified {@link Composite} and
 	 * initializes it.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	void createGraphicalViewer(Composite parent) {
 		GraphicalViewer viewer;
 		if (this.getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			viewer = new GFScrollingGraphicalViewer(this.container);
 			((GFScrollingGraphicalViewer) viewer).createGFControl(parent);
 		} else {
 			viewer = new GraphitiScrollingGraphicalViewer(this.container);
 			viewer.createControl(parent);
 		}
 		this.container.setGraphicalViewer(viewer);
 		configureGraphicalViewer();
 		this.container.hookGraphicalViewer();
 		initializeGraphicalViewer();
 	}
 
 	String getEditorInitializationError() {
 		return this.editorInitializationError;
 	}
 }
