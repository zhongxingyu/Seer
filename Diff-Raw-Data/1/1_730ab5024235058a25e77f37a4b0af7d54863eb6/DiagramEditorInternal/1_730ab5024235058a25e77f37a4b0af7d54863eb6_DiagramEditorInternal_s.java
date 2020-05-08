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
  *    mwenz - Bug 331715: Support for rectangular grids in diagrams
  *    mwenz - Bug 332964: Enable setting selection for non-EMF domain models and
  *                        when embedded into a multi-page editor
  *    mwenz - Bug 336075 - DiagramEditor accepts URIEditorInput
  *    mwenz - Bug 329523 - Add notification of DiagramTypeProvider after saving a diagram
  *    jpasch - Bug 323025 ActionBarContributor cleanup
  *    mwenz - Bug 345347 - There should be a way to not allow other plugins to contribute to the diagram context menu
  *    mwenz - Bug 346932 - Navigation history broken
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.editor;
 
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
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
 import org.eclipse.gef.Tool;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CommandStackEventListener;
 import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
 import org.eclipse.gef.editparts.GridLayer;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.tools.ConnectionCreationTool;
 import org.eclipse.gef.tools.CreationTool;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.actions.AlignmentAction;
 import org.eclipse.gef.ui.actions.DirectEditAction;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.actions.MatchHeightAction;
 import org.eclipse.gef.ui.actions.MatchWidthAction;
 import org.eclipse.gef.ui.actions.ToggleGridAction;
 import org.eclipse.gef.ui.actions.ZoomInAction;
 import org.eclipse.gef.ui.actions.ZoomOutAction;
 import org.eclipse.gef.ui.palette.DefaultPaletteViewerPreferences;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
 import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.graphiti.DiagramScrollingBehavior;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IDirectEditingInfo;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IPrintFeature;
 import org.eclipse.graphiti.features.ISaveImageFeature;
 import org.eclipse.graphiti.features.context.ISaveImageContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.SaveImageContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.internal.datatypes.impl.DimensionImpl;
 import org.eclipse.graphiti.internal.datatypes.impl.LocationImpl;
 import org.eclipse.graphiti.internal.pref.GFPreferences;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.editor.DiagramEditorContextMenuProvider;
 import org.eclipse.graphiti.ui.editor.DiagramEditorFactory;
 import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
 import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
 import org.eclipse.graphiti.ui.internal.IResourceRegistry;
 import org.eclipse.graphiti.ui.internal.IResourceRegistryHolder;
 import org.eclipse.graphiti.ui.internal.ResourceRegistry;
 import org.eclipse.graphiti.ui.internal.action.CopyAction;
 import org.eclipse.graphiti.ui.internal.action.DeleteAction;
 import org.eclipse.graphiti.ui.internal.action.PasteAction;
 import org.eclipse.graphiti.ui.internal.action.PrintGraphicalViewerAction;
 import org.eclipse.graphiti.ui.internal.action.RemoveAction;
 import org.eclipse.graphiti.ui.internal.action.SaveImageAction;
 import org.eclipse.graphiti.ui.internal.action.UpdateAction;
 import org.eclipse.graphiti.ui.internal.config.ConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProviderHolder;
 import org.eclipse.graphiti.ui.internal.contextbuttons.ContextButtonManagerForPad;
 import org.eclipse.graphiti.ui.internal.contextbuttons.IContextButtonManager;
 import org.eclipse.graphiti.ui.internal.dnd.GFTemplateTransferDropTargetListener;
 import org.eclipse.graphiti.ui.internal.dnd.ObjectsTransferDropTargetListener;
 import org.eclipse.graphiti.ui.internal.parts.IConnectionEditPart;
 import org.eclipse.graphiti.ui.internal.parts.IPictogramElementDelegate;
 import org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart;
 import org.eclipse.graphiti.ui.internal.parts.IShapeEditPart;
 import org.eclipse.graphiti.ui.internal.parts.ShapeEditPart;
import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.internal.util.gef.ScalableRootEditPartAnimated;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.commands.ActionHandler;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.TransferDropTargetListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.navigator.CommonNavigator;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 /**
  * The Class DiagramEditorInternal.
  */
 public class DiagramEditorInternal extends GraphicalEditorWithFlyoutPalette implements IConfigurationProviderHolder, IDiagramEditor,
 		ITabbedPropertySheetPageContributor, IResourceRegistryHolder, IEditingDomainProvider {
 
 	private final CommandStackEventListener cmdStackListener = new CommandStackEventListener() {
 		@Override
 		public void stackChanged(CommandStackEvent event) {
 			if (Display.getCurrent() != null) { // Only fire if triggered from
 												// UI thread
 				DiagramEditorInternal.this.firePropertyChange(IEditorPart.PROP_DIRTY);
 
 				// Promote the changes to the command stack also to the action
 				// bars and registered actions to
 				// correctly reflect e.g. undo/redo in the menu (introduced to
 				// enable removing NOP commands from
 				// the command stack
 				commandStackChanged(event);
 			}
 		}
 	};
 
 	protected class FWCommandStackListener implements CommandStackListener {
 
 		@Override
 		public void commandStackChanged(EventObject event) {
 			getEditorSite().getShell().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					firePropertyChange(IEditorPart.PROP_DIRTY);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Updates the editor's dirty toggle.
 	 */
 	final class DirtyFlagUpdater implements Runnable {
 		@Override
 		public void run() {
 			firePropertyChange(IEditorPart.PROP_DIRTY);
 		}
 	}
 
 	private final DiagramEditorBehavior behavior;
 	// As this may be called quite often, reuse the same instance of the
 	// runnable.
 	private final Runnable mDirtyFlagUpdater = new DirtyFlagUpdater();
 
 	private CommandStackListener fwListener;
 
 	/**
 	 * The Constant DEFAULT_PALETTE_SIZE.
 	 */
 	protected static final int DEFAULT_PALETTE_SIZE = 130;
 
 	/**
 	 * The Constant PALETTE_DOCK_LOCATION.
 	 */
 	protected static final String PALETTE_DOCK_LOCATION = "Dock location"; //$NON-NLS-1$
 
 	/**
 	 * The Constant PALETTE_SIZE.
 	 */
 	protected static final String PALETTE_SIZE = "Palette Size"; //$NON-NLS-1$
 
 	/**
 	 * The Constant PALETTE_STATE.
 	 */
 	public static final String PALETTE_STATE = "Palette state"; //$NON-NLS-1$
 
 	private static final boolean REFRESH_ON_GAINED_FOCUS = false;
 
 	// private static final boolean REFRESH_ON_COMMAND_STACK_CHANGES = false;
 
 	private DiagramScrollingBehavior diagramScrollingBehavior = null;
 
 	private PictogramElement pictogramElementsForSelection[] = null;
 
 	private IConfigurationProvider configurationProvider = null;
 
 	private KeyHandler keyHandler;
 
 	private PaletteRoot paletteRoot;
 
 	private Point mouseLocation;
 
 	private DiagramChangeListener diagramChangeListener;
 	private DomainModelChangeListener domainModelListener;
 
 	private IContextButtonManager contextButtonManager = null;
 
 	private String contributorId = null;
 
 	private boolean directEditingActive = false;
 
 	/* keep refreshed EP/GA/PE to avoid multiple refresh of same figure */
 	private HashSet<EditPart> refreshedFigure4EP = new HashSet<EditPart>();
 
 	private HashSet<GraphicsAlgorithm> refreshedFigure4GA = new HashSet<GraphicsAlgorithm>();
 
 	private HashSet<PictogramElement> refreshedFigure4PE = new HashSet<PictogramElement>();
 
 	private IResourceRegistry resourceRegistry = new ResourceRegistry();
 
 	private boolean autoRefresh = true;
 
 	private TransactionalEditingDomain editingDomain = null;
 
 	/**
 	 * Instantiates a new diagram editor.
 	 */
 	public DiagramEditorInternal() {
 		behavior = new DiagramEditorBehavior(this);
 	}
 
 	/**
 	 * Creates and registers the "New ..." actions. Those actions are dependent
 	 * on the IConfigurationProvider.
 	 */
 	private void buildNewObjectActions() {
 		if (getConfigurationProvider() == null) // can happen for example on
 			// first initialization
 			return;
 	}
 
 	/**
 	 * Calculates the location in dependence from scrollbars and zoom factor.
 	 * 
 	 * @param nativeLocation
 	 *            the native location
 	 * @return the point
 	 */
 	public Point calculateRealMouseLocation(Point nativeLocation) {
 
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
 
 		ZoomManager zoomManager = (ZoomManager) getGraphicalViewer().getProperty(ZoomManager.class.toString());
 		ret = ret.getScaled(1 / zoomManager.getZoom());
 
 		return ret;
 	}
 
 	/**
 	 * Called to configure the editor, before it receives its content. The
 	 * default-implementation is for example doing the following: configure the
 	 * ZoomManager, registering Actions... Here everything is done, which is
 	 * independent of the IConfigurationProvider.
 	 * 
 	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
 	 */
 	@Override
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 
 		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();
 
 		ScalableRootEditPartAnimated rootEditPart = new ScalableRootEditPartAnimated(viewer) {
 
 			@Override
 			protected GridLayer createGridLayer() {
 				return new org.eclipse.graphiti.ui.internal.util.draw2d.GridLayer(getConfigurationProvider());
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
 		IToolBehaviorProvider toolBehaviorProvider = getConfigurationProvider().getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 		zoomManager.setZoomLevels(toolBehaviorProvider.getZoomLevels());
 
 		initActionRegistry(zoomManager);
 
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
 		contextButtonManager = new ContextButtonManagerForPad(this);
 
 		/* sw: make scroll bars always visible */
 		if (getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			GFFigureCanvas figureCanvas = getGFFigureCanvas();
 			if (figureCanvas != null) {
 				figureCanvas.setScrollBarVisibility(FigureCanvas.ALWAYS);
 			}
 		}
 	}
 
 	/**
 	 * Returns a new ContextMenuProvider. Can be null, if no context-menu shall
 	 * be displayed.
 	 * 
 	 * @return A new ContextMenuProvider.
 	 */
 	protected ContextMenuProvider createContextMenuProvider() {
 		return new DiagramEditorContextMenuProvider(getGraphicalViewer(), getActionRegistry(), getConfigurationProvider());
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
 	 */
 	protected boolean shouldRegisterContextMenu() {
 		return true;
 	}
 
 	/**
 	 * Creates the GraphicalViewer AND navigation-bar on the specified
 	 * <code>Composite</code>.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	@Override
 	protected void createGraphicalViewer(Composite parent) {
 		GraphicalViewer viewer;
 		if (getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			viewer = new GFScrollingGraphicalViewer(this);
 			((GFScrollingGraphicalViewer) viewer).createGFControl(parent);
 		} else {
 			viewer = new GraphitiScrollingGraphicalViewer(this);
 			viewer.createControl(parent);
 		}
 		setGraphicalViewer(viewer);
 		configureGraphicalViewer();
 		hookGraphicalViewer();
 		initializeGraphicalViewer();
 	}
 
 	/**
 	 * Returns the object, which is used to store/provide the preferences for
 	 * the Palette. This implementation will use the IModelExtensionProvider as
 	 * persistent storage.
 	 * 
 	 * @return The object, which is used to store/provide the preferences for
 	 *         the Palette.
 	 */
 	protected FlyoutPreferences createPalettePreferences() {
 		return new DefaultFlyoutPalettePreferences();
 	}
 
 	/**
 	 * Creates the PaletteRoot of this editor.
 	 * 
 	 * @return the palette root
 	 * @see org.eclipse.graphiti.ui.editor.GraphicalEditorIncludingPalette#createPaletteRoot()
 	 */
 	protected PaletteRoot createPaletteRoot() {
 		return new GFPaletteRoot(getConfigurationProvider());
 	}
 
 	/**
 	 * Returns the PaletteViewerProvider, which can be used to create a new
 	 * PaletteViewer. This method can be overwritten to return a subclass of the
 	 * PaletteViewerProvider, which configures the PaletteViewer with a
 	 * different ContextMenu, with a PaletteCustomizer or with a different
 	 * IPreferencesStore. Do not call this method directly, instead call
 	 * getPaletteViewerProvider(), which buffers the created object.
 	 * <p>
 	 * By default this method returns a new PaletteViewerProvider.
 	 * 
 	 * @return The PaletteViewerProvider, which can be used to create a new
 	 *         PaletteViewer.
 	 */
 	@Override
 	protected PaletteViewerProvider createPaletteViewerProvider() {
 		return new PaletteViewerProvider(getEditDomain()) {
 			private KeyHandler paletteKeyHandler = null;
 
 			@Override
 			protected void configurePaletteViewer(PaletteViewer viewer) {
 				super.configurePaletteViewer(viewer);
 				viewer.getKeyHandler().setParent(getPaletteKeyHandler());
 				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
 			}
 
 			/**
 			 * @return Palette Key Handler for the palette
 			 */
 			private KeyHandler getPaletteKeyHandler() {
 
 				if (paletteKeyHandler == null) {
 
 					paletteKeyHandler = new KeyHandler() {
 
 						/**
 						 * Processes a <i>key released </i> event. This method
 						 * is called by the Tool whenever a key is released, and
 						 * the Tool is in the proper state. Override to support
 						 * pressing the enter key to create a shape or
 						 * connection (between two selected shapes)
 						 * 
 						 * @param event
 						 *            the KeyEvent
 						 * @return <code>true</code> if KeyEvent was handled in
 						 *         some way
 						 */
 						@Override
 						public boolean keyReleased(KeyEvent event) {
 							if (event.keyCode == SWT.Selection) {
 								Tool tool = getEditDomain().getPaletteViewer().getActiveTool().createTool();
 								if (tool instanceof CreationTool || tool instanceof ConnectionCreationTool) {
 									tool.keyUp(event, getGraphicalViewer());
 									// Deactivate current selection
 									getEditDomain().getPaletteViewer().setActiveTool(null);
 									return true;
 								}
 							}
 							return super.keyReleased(event);
 						}
 					};
 				}
 				return paletteKeyHandler;
 			}
 		};
 	}
 
 	// ====================== standard behaviour ==============================
 
 	@Override
 	public void dispose() {
 
 		unregisterDiagramResourceSetListener();
 
 		unregisterBOListener();
 
 		if (isDirty()) {
 			// TODO rollback
 		}
 
 		if (getConfigurationProvider() != null) {
 			getConfigurationProvider().dispose();
 		}
 
 		this.paletteRoot = null;
 
 		// unregister selection listener, registered during createPartControl()
 		if (getSite() != null && getSite().getPage() != null) {
 			getSite().getPage().removeSelectionListener(this);
 		}
 		
 		if (getEditDomain() != null && getEditDomain().getCommandStack() != null) {
 			getEditDomain().getCommandStack().removeCommandStackEventListener(cmdStackListener);
 			getEditDomain().getCommandStack().dispose();
 		}
 
 		if (resourceRegistry != null) {
 			resourceRegistry.dispose();
 		}
 
 		DiagramEditorBehavior behavior = getBehavior();
 		behavior.getEditingDomain().getCommandStack().removeCommandStackListener(fwListener);
 		fwListener = null;
 		behavior.dispose();
 		RuntimeException exc = null;
 		try {
 			super.dispose();
 		} catch (RuntimeException e) {
 			exc = e;
 		}
 
 		if (getEditDomain() != null)
 			getEditDomain().setCommandStack(null);
 
 		if (exc != null)
 			throw exc;
 
 
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		Resource[] savedResources = getBehavior().doSave(monitor);
 		commandStackChanged(null);
 
 		IDiagramTypeProvider provider = getConfigurationProvider().getDiagramTypeProvider();
 		provider.resourcesSaved(getDiagramTypeProvider().getDiagram(), savedResources);
 
 	}
 
 	@Override
 	protected void setInput(IEditorInput input) {
 		super.setInput(input);
 			// determine filename
 			if (input == null)
 				throw new IllegalArgumentException("The IEditorInput must not be null"); //$NON-NLS-1$
 			if (!(input instanceof DiagramEditorInput))
 				throw new IllegalArgumentException("The IEditorInput has the wrong type: " + input.getClass()); //$NON-NLS-1$
 
 			DiagramEditorInput diagramEditorInput = (DiagramEditorInput) input;
 			Diagram diagram = diagramEditorInput.getDiagram();
 
 			// can happen if editor is started with invalid URI
 			if (diagram == null) {
 				throw new IllegalStateException("No Diagram found for URI '" + diagramEditorInput.getUriString() //$NON-NLS-1$
 						+ "'. . See the error log for details."); //$NON-NLS-1$
 			}
 
 			// set editing domain
 			TransactionalEditingDomain editingDomain = getBehavior().getEditingDomain();
 			setTransactionalEditingDomain(editingDomain);
 
 			String providerId = diagramEditorInput.getProviderId();
 
 			// if provider is null then take the first installed provider for
 			// this diagram type
 			if (providerId == null) {
 				providerId = GraphitiUi.getExtensionManager().getDiagramTypeProviderId(diagram.getDiagramTypeId());
 				diagramEditorInput.setProviderId(providerId);
 			}
 
 			// get according diagram-type-provider
 			IDiagramTypeProvider diagramTypeProvider = GraphitiUi.getExtensionManager().createDiagramTypeProvider(
 					providerId);
 			if (diagramTypeProvider != null) {
 				diagramTypeProvider.init(diagram, this);
 				IConfigurationProvider configurationProvider = new ConfigurationProvider(this, diagramTypeProvider);
 				setConfigurationProvider(configurationProvider);
 				handleAutoUpdateAtStartup(diagram, diagramTypeProvider);
 			} else {
 				throw new IllegalArgumentException(
 						"could not find diagram type provider for " + diagram.getDiagramTypeId()); //$NON-NLS-1$
 			}
 
 			registerBOListener();
 			registerDiagramResourceSetListener();
 
 			refreshTitle();
 
 	}
 
 	/**
 	 * Returns the internal ActionRegistry (because {@link #getActionRegistry()}
 	 * is protected).
 	 * 
 	 * @return The internal ActionRegistry (because {@link #getActionRegistry()}
 	 *         is protected).
 	 */
 	public ActionRegistry getActionRegistryInternal() {
 		return getActionRegistry();
 	}
 
 	/**
 	 * This implementation returns the ZoomManager for the ZoomManager.class and
 	 * the OutlinePage for the IContentOutlinePage.class.
 	 * 
 	 * @param type
 	 *            the type
 	 * @return the adapter
 	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#getAdapter(Class)
 	 */
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
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
 
 		GraphicalViewer viewer = getGraphicalViewer();
 		if (type == ZoomManager.class && viewer != null) {
 			return viewer.getProperty(ZoomManager.class.toString());
 		}
 
 		if (type == IPropertySheetPage.class) {
 			if (cfgProvider != null && getContributorId() != null) {
 				return new TabbedPropertySheetPage(this);
 			}
 			return null; // not yet initialized
 		}
 		if (IUndoContext.class == type) {
 			return getBehavior().getUndoContext();
 		}
 		return super.getAdapter(type);
 	}
 
 	/**
 	 * Returns the KeyHandler with common bindings for both the Outline and the
 	 * Graphical Viewer.
 	 * 
 	 * @return The KeyHandler with common bindings for both the Outline and the
 	 *         Graphical Viewer.
 	 */
 	public KeyHandler getCommonKeyHandler() {
 		if (keyHandler == null) {
 			keyHandler = new KeyHandler();
 			keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
 			keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, SWT.SHIFT), getActionRegistry().getAction(RemoveAction.ACTION_ID));
 			keyHandler.put(KeyStroke.getPressed(SWT.F2, 0), getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
 			keyHandler.put(KeyStroke.getPressed('c', SWT.CTRL), getActionRegistry().getAction(ActionFactory.COPY.getId()));
 			keyHandler.put(KeyStroke.getPressed('v', SWT.CTRL), getActionRegistry().getAction(ActionFactory.PASTE.getId()));
 			// _keyHandler.put(KeyStroke.getPressed((char) 1, 'a', SWT.CTRL),
 			// getActionRegistry().getAction(ActionFactory.SELECT_ALL.getId()));
 		}
 		return keyHandler;
 	}
 
 	@Override
 	public IConfigurationProvider getConfigurationProvider() {
 		return configurationProvider;
 	}
 
 	/**
 	 * Returns the contents-editpart of this Editor. This is the topmost
 	 * EditPart, which contains business-data.
 	 * 
 	 * @return The contents-editpart of this Editor.
 	 */
 	protected EditPart getContentEditPart() {
 		if (getGraphicalViewer() != null) {
 			return getGraphicalViewer().getContents();
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the context button manager.
 	 * 
 	 * @return the context button manager
 	 */
 	public IContextButtonManager getContextButtonManager() {
 		return contextButtonManager;
 	}
 
 	/**
 	 * ID for tabbed property sheets.
 	 * 
 	 * @return the contributor id
 	 */
 	@Override
 	public String getContributorId() {
 
 		if (contributorId == null) {
 			IToolBehaviorProvider tbp = getToolBehaviorProvider();
 			if (tbp != null) {
 				contributorId = tbp.getContributorId();
 			}
 		}
 		return contributorId;
 	}
 
 	@Override
 	public IDimension getCurrentSize() {
 
 		if (getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			GFFigureCanvas canvas = getGFFigureCanvas();
 			// if this method is called during editor opening, it can fail, so
 			// we return "default size".
 			if (canvas != null) {
 				Dimension size = canvas.getContents().getSize();
 				return new DimensionImpl(size.width, size.height);
 			} else
 				return new DimensionImpl(1024, 768);
 		} else {
 			FigureCanvas canvas = getFigureCanvas();
 			// if this method is called during editor opening, it can fail, so
 			// we return "default size".
 			if (canvas != null) {
 				Dimension size = canvas.getContents().getSize();
 				return new DimensionImpl(size.width, size.height);
 			} else
 				return new DimensionImpl(1024, 768);
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
 	 */
 	@Deprecated
 	public DiagramScrollingBehavior getDiagramScrollingBehavior() {
 		if (this.diagramScrollingBehavior == null) {
 			IToolBehaviorProvider tbp = getConfigurationProvider().getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 			this.diagramScrollingBehavior = tbp.getDiagramScrollingBehavior();
 		}
 		return this.diagramScrollingBehavior;
 	}
 
 	@Override
 	public IDiagramTypeProvider getDiagramTypeProvider() {
 		IConfigurationProvider cfgProvider = getConfigurationProvider();
 		if (cfgProvider != null)
 			return cfgProvider.getDiagramTypeProvider();
 		return null;
 	}
 
 	@Override
 	public DefaultEditDomain getEditDomain() {
 		return super.getEditDomain();
 	}
 
 	public GraphicalEditPart getEditPartForPictogramElement(PictogramElement pe) {
 		Map<?, ?> editPartRegistry = getGraphicalViewer().getEditPartRegistry();
 		if (editPartRegistry != null) {
 			Object obj = editPartRegistry.get(pe);
 			if (obj instanceof GraphicalEditPart) {
 				GraphicalEditPart ep = (GraphicalEditPart) obj;
 				return ep;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the figure canvas.
 	 * 
 	 * @return the figure canvas
 	 */
 	public FigureCanvas getFigureCanvas() {
 		GraphicalViewer viewer = getGraphicalViewer();
 		if (viewer != null)
 			return (FigureCanvas) viewer.getControl();
 		else
 			return null;
 	}
 
 	public IFigure getFigureForPictogramElement(PictogramElement pe) {
 		GraphicalEditPart ep = getEditPartForPictogramElement(pe);
 		if (ep != null) {
 			return ep.getFigure();
 		}
 		return null;
 	}
 
 	private GFFigureCanvas getGFFigureCanvas() {
 		GraphicalViewer viewer = getGraphicalViewer();
 		if (viewer != null)
 			return (GFFigureCanvas) viewer.getControl();
 		else
 			return null;
 	}
 
 	@Override
 	public GraphicalViewer getGraphicalViewer() {
 		return super.getGraphicalViewer();
 	}
 
 	/**
 	 * Gets the mouse location.
 	 * 
 	 * @return the mouse location
 	 */
 	public Point getMouseLocation() {
 		if (mouseLocation == null) {
 			mouseLocation = new Point();
 		}
 		return mouseLocation;
 	}
 
 	@Override
 	public ILocation getCurrentMouseLocation() {
 		Point mL = getMouseLocation();
 		return new LocationImpl(mL.x, mL.y);
 	}
 
 	@Override
 	protected FlyoutPreferences getPalettePreferences() {
 		return new FlyoutPreferences() {
 			@Override
 			public int getDockLocation() {
 				return getPreferenceStore().getInt(PALETTE_DOCK_LOCATION);
 			}
 
 			@Override
 			public int getPaletteState() {
 				if (!getToolBehaviorProvider().isShowFlyoutPalette())
 					return 8;
 				return getPreferenceStore().getInt(PALETTE_STATE);
 			}
 
 			@Override
 			public int getPaletteWidth() {
 				return getPreferenceStore().getInt(PALETTE_SIZE);
 			}
 
 			@Override
 			public void setDockLocation(int location) {
 				getPreferenceStore().setValue(PALETTE_DOCK_LOCATION, location);
 			}
 
 			@Override
 			public void setPaletteState(int state) {
 				getPreferenceStore().setValue(PALETTE_STATE, state);
 			}
 
 			@Override
 			public void setPaletteWidth(int width) {
 				getPreferenceStore().setValue(PALETTE_SIZE, width);
 			}
 		};
 	}
 
 	private IPreferenceStore getPreferenceStore() {
 		IPreferenceStore ps = GraphitiUIPlugin.getDefault().getPreferenceStore();
 		ps.setDefault(DiagramEditorInternal.PALETTE_STATE, FlyoutPaletteComposite.STATE_PINNED_OPEN);
 		return ps;
 	}
 
 	@Override
 	protected PaletteRoot getPaletteRoot() {
 		if (paletteRoot == null)
 			paletteRoot = createPaletteRoot();
 		return paletteRoot;
 	}
 
 	/**
 	 * Gets the pictogram element for selection.
 	 * 
 	 * @return the pictogram element for selection
 	 */
 	protected PictogramElement[] getPictogramElementsForSelection() {
 		return pictogramElementsForSelection;
 	}
 
 	/**
 	 * Gets the refreshed figure4 ep.
 	 * 
 	 * @return the refreshed figure4 ep
 	 */
 	public HashSet<EditPart> getRefreshedFigure4EP() {
 		return refreshedFigure4EP;
 	}
 
 	/**
 	 * Gets the refreshed figure4 ga.
 	 * 
 	 * @return the refreshed figure4 ga
 	 */
 	public HashSet<GraphicsAlgorithm> getRefreshedFigure4GA() {
 		return refreshedFigure4GA;
 	}
 
 	/**
 	 * Gets the refreshed figure4 pe.
 	 * 
 	 * @return the refreshed figure4 pe
 	 */
 	public HashSet<PictogramElement> getRefreshedFigure4PE() {
 		return refreshedFigure4PE;
 	}
 
 	@Override
 	public IResourceRegistry getResourceRegistry() {
 		return resourceRegistry;
 	}
 
 	@Override
 	public PictogramElement[] getSelectedPictogramElements() {
 		PictogramElement pe[] = new PictogramElement[0];
 		ISelectionProvider selectionProvider = getSite().getSelectionProvider();
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
 
 	/**
 	 * Returns the internal SelectionSynchronizer (because
 	 * {@link #getSelectionSynchronizer()} is protected).
 	 * 
 	 * @return The internal SelectionSynchronizer (because
 	 *         {@link #getSelectionSynchronizer()} is protected).
 	 */
 	public SelectionSynchronizer getSelectionSynchronizerInternal() {
 		return getSelectionSynchronizer();
 	}
 
 	@Override
 	public String getTitleToolTip() {
 		if (getDiagramTypeProvider() != null && getDiagramTypeProvider().getCurrentToolBehaviorProvider() != null) {
 			IToolBehaviorProvider tbp = getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 			String titleToolTip = tbp.getTitleToolTip();
 			if (titleToolTip != null) {
 				return titleToolTip;
 			}
 		}
 		return super.getTitleToolTip();
 	}
 
 	private IToolBehaviorProvider getToolBehaviorProvider() {
 		IDiagramTypeProvider dtp = getDiagramTypeProvider();
 		if (dtp != null) {
 			return dtp.getCurrentToolBehaviorProvider();
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the zoom level.
 	 * 
 	 * @return the zoom level
 	 */
 	public double getZoomLevel() {
 		ZoomManager zoomManager = (ZoomManager) getAdapter(ZoomManager.class);
 		if (zoomManager == null)
 			return 1;
 
 		/*
 		 * avoid long running calculations for large diagrams and zoom factors
 		 * below 5%
 		 */
 		return Math.max(0.05D, zoomManager.getZoom());
 	}
 
 	/**
 	 * Handle auto update at startup.
 	 * 
 	 * @param diagram
 	 *            the diagram
 	 * @param diagramTypeProvider
 	 *            the diagram type provider
 	 */
 	private void handleAutoUpdateAtStartup(Diagram diagram, IDiagramTypeProvider diagramTypeProvider) {
 		if (diagramTypeProvider.isAutoUpdateAtStartup()) {
 			autoUpdate(diagram, diagramTypeProvider);
 		}
 	}
 
 	/**
 	 * Handle auto update at reset.
 	 * 
 	 * @param diagram
 	 *            the diagram
 	 * @param diagramTypeProvider
 	 *            the diagram type provider
 	 */
 	private void handleAutoUpdateAtReset(Diagram diagram, IDiagramTypeProvider diagramTypeProvider) {
 		if (diagramTypeProvider.isAutoUpdateAtReset()) {
 			autoUpdate(diagram, diagramTypeProvider);
 		}
 	}
 
 	private void autoUpdate(Diagram diagram, IDiagramTypeProvider diagramTypeProvider) {
 		IFeatureProvider featureProvider = diagramTypeProvider.getFeatureProvider();
 		IUpdateContext updateCtx = new UpdateContext(diagram);
 		featureProvider.updateIfPossible(updateCtx);
 		refresh();
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		// Eclipse may call us with other inputs when a file is to be
 		// opened. Also a disposed DiagramEditorInput (no editing domain)
 		// may be handed in (see Bug 346932). Try to convert this to a
 		// valid diagram input.
 		if (!(input instanceof DiagramEditorInput) || ((DiagramEditorInput) input).getAdapter(TransactionalEditingDomain.class) == null) {
 			IEditorInput newInput = new DiagramEditorFactory().createEditorInput(input);
 			if (!(newInput instanceof DiagramEditorInput)) {
 				// give up
 				throw new PartInitException("Unknown editor input: " + input); //$NON-NLS-1$
 			}
 			input = newInput;
 		}
 
 		getBehavior().init(site, input, mDirtyFlagUpdater);
 		// In next line GEF calls setSite(), setInput(),
 		// getEditingDomain(), ...
 		try {
 			super.init(site, input);
 		} catch (RuntimeException e) {
 			throw new PartInitException(e.getMessage());
 		}
 		fwListener = new FWCommandStackListener();
 		getBehavior().getEditingDomain().getCommandStack().addCommandStackListener(fwListener);
 
 	}
 
 	/**
 	 * Inits the action regsitry.
 	 */
 	protected void initActionRegistry(ZoomManager zoomManager) {
 		final ActionRegistry actionRegistry = getActionRegistry();
 		@SuppressWarnings("unchecked")
 		final List<String> selectionActions = getSelectionActions();
 
 		// register predefined actions (e.g. update, remove, delete, ...)
 		IAction action = new UpdateAction(this, getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new RemoveAction(this, getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new DeleteAction(this, getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new CopyAction(this, getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		action = new PasteAction(this, getConfigurationProvider());
 		actionRegistry.registerAction(action);
 		selectionActions.add(action.getId());
 
 		IFeatureProvider fp = getConfigurationProvider().getDiagramTypeProvider().getFeatureProvider();
 		if (fp != null) {
 			ISaveImageFeature sf = fp.getSaveImageFeature();
 
 			if (sf != null) {
 				ISaveImageContext context = new SaveImageContext();
 				action = new SaveImageAction(sf, context, this);
 				actionRegistry.registerAction(action);
 				selectionActions.add(action.getId());
 			}
 		}
 
 		registerAction(new ZoomInAction(zoomManager));
 		registerAction(new ZoomOutAction(zoomManager));
 		registerAction(new DirectEditAction((IWorkbenchPart) this));
 
 		registerAction(new AlignmentAction((IWorkbenchPart) this, PositionConstants.LEFT));
 		registerAction(new AlignmentAction((IWorkbenchPart) this, PositionConstants.RIGHT));
 		registerAction(new AlignmentAction((IWorkbenchPart) this, PositionConstants.TOP));
 		registerAction(new AlignmentAction((IWorkbenchPart) this, PositionConstants.BOTTOM));
 		registerAction(new AlignmentAction((IWorkbenchPart) this, PositionConstants.CENTER));
 		registerAction(new AlignmentAction((IWorkbenchPart) this, PositionConstants.MIDDLE));
 		registerAction(new MatchWidthAction(this));
 		registerAction(new MatchHeightAction(this));
 		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
 		getActionRegistry().registerAction(showGrid);
 	}
 
 	/**
 	 * Called to initialize the editor with its content. Here everything is
 	 * done, which is dependent of the IConfigurationProvider.
 	 */
 	@Override
 	protected void initializeGraphicalViewer() {
 
 		super.initializeGraphicalViewer();
 
 		// register Actions
 		IFeatureProvider featureProvider = getConfigurationProvider().getDiagramTypeProvider().getFeatureProvider();
 		if (featureProvider != null) {
 			IPrintFeature pf = featureProvider.getPrintFeature();
 			if (pf != null) {
 				registerAction(new PrintGraphicalViewerAction(getConfigurationProvider(), getConfigurationProvider().getWorkbenchPart(), pf));
 			}
 		}
 
 		buildNewObjectActions();
 
 		// this will cause the ActionBarContributor to refresh with the
 		// new actions (there is no specific refresh-action).
 		if (getEditorSite().getActionBarContributor() != null)
 			getEditorSite().getActionBarContributor().setActiveEditor(this);
 
 		// setting ContextMenuProvider
 		ContextMenuProvider contextMenuProvider = createContextMenuProvider();
 		if (contextMenuProvider != null) {
 			getGraphicalViewer().setContextMenu(contextMenuProvider);
 			// the registration allows an extension of the context-menu by other
 			// plugins
 			if (shouldRegisterContextMenu()) {
 				getSite().registerContextMenu(contextMenuProvider, getGraphicalViewer());
 			}
 		}
 
 		// set contents
 		getGraphicalViewer().setEditPartFactory(getConfigurationProvider().getEditPartFactory());
 		getGraphicalViewer().setContents(getConfigurationProvider().getDiagram());
 
 		// set preference-store for palette
 		PaletteViewer paletteViewer = getEditDomain().getPaletteViewer();
 		if (paletteViewer != null) {
 			IPreferenceStore store = GraphitiUIPlugin.getDefault().getPreferenceStore();
 			paletteViewer.setPaletteViewerPreferences(new DefaultPaletteViewerPreferences(store));
 
 			// Refresh the PaletteViewer
 			// This can be achieved by firing a font-change-event from the
 			// IPreferenceStore.
 			// It would be nicer, if the PaletteViewer would have some kind of
 			// refresh()-method directly.
 			store.firePropertyChangeEvent(PaletteViewerPreferences.PREFERENCE_FONT, null, null);
 		}
 
 		getGraphicalViewer().getControl().addMouseMoveListener(new MouseMoveListener() {
 			@Override
 			public void mouseMove(MouseEvent e) {
 				setMouseLocation(e.x, e.y);
 			}
 		});
 
 		getGraphicalViewer()
 				.addDropTargetListener((TransferDropTargetListener) new ObjectsTransferDropTargetListener(getGraphicalViewer()));
 
 		getGraphicalViewer().addDropTargetListener(new GFTemplateTransferDropTargetListener(getGraphicalViewer(), this));
 	}
 
 	/**
 	 * Internal refresh edit part.
 	 * 
 	 * @param editPart
 	 *            the edit part
 	 */
 	void internalRefreshEditPart(final EditPart editPart) {
 		if (Display.getCurrent() == null) {
 			Display.getDefault().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					internalRefreshEditPart(editPart);
 					// refreshOutline();
 				}
 			});
 			return;
 		}
 
 		long start = System.currentTimeMillis();
 
 		try {
 			getContextButtonManager().hideContextButtonsInstantly();
 
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
 	 * Checks if is alive.
 	 * 
 	 * @return TRUE, if editor contains a model connector and a valid Diagram
 	 */
 	public boolean isAlive() {
 		IConfigurationProvider cp = getConfigurationProvider();
 		if (cp != null) {
 			TransactionalEditingDomain editingDomain = getEditingDomain();
 			Diagram diagram = cp.getDiagram();
 			if (editingDomain != null && GraphitiInternal.getEmfService().isObjectAlive(diagram)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if is auto refresh.
 	 * 
 	 * @return true, if is auto refresh
 	 */
 	protected boolean isAutoRefresh() {
 		return autoRefresh;
 	}
 
 	/**
 	 * Inits the refresh.
 	 */
 	public void initRefresh() {
 		refreshedFigure4EP = new HashSet<EditPart>();
 		refreshedFigure4GA = new HashSet<GraphicsAlgorithm>();
 		refreshedFigure4PE = new HashSet<PictogramElement>();
 	}
 
 	@Override
 	public void refresh() {
 
 		if (!isAlive()) {
 			return;
 		}
 
 		if (GFPreferences.getInstance().isCPUProfilingTraceActive()) {
 			if (T.racer().info()) {
 				T.racer().info("DiagramEditorInternal.refresh()"); //$NON-NLS-1$
 			}
 		}
 
 		if (Display.getCurrent() == null) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					refresh();
 				}
 			});
 			return;
 		}
 
 		// this should indicate that the editor is already disposed
 		// perhaps you find a better way to do this
 		if (this.paletteRoot == null)
 			return;
 
 		long start = System.currentTimeMillis();
 
 		final EditPart contentEditPart = getContentEditPart();
 		if (contentEditPart == null) {
 			return;
 		}
 
 		internalRefreshEditPart(contentEditPart);
 
 		refreshTitle();
 
 		long stop = System.currentTimeMillis();
 		long time = (stop - start);
 		if (time > 500) {
 			String output = "refresh took " + time + " ms."; //$NON-NLS-1$ //$NON-NLS-2$
 			T.racer().warning("DiagramEditorInternal.refresh() ", output); //$NON-NLS-1$
 		}
 
 		// prove if switch to direct editing is required
 		IDirectEditingInfo dei = getConfigurationProvider().getFeatureProvider().getDirectEditingInfo();
 		if (dei.isActive()) {
 			EditPart editPart = (EditPart) getGraphicalViewer().getEditPartRegistry().get(dei.getMainPictogramElement());
 			if (editPart instanceof ShapeEditPart) {
 				ShapeEditPart shapeEditPart = (ShapeEditPart) editPart;
 				shapeEditPart.switchToDirectEditingMode(dei.getPictogramElement(), dei.getGraphicsAlgorithm());
 				// reset values
 				dei.reset();
 			}
 		}
 
 		selectBufferedPictogramElements();
 	}
 
 	@Override
 	public void refresh(PictogramElement pe) {
 		if (pe == null || !pe.isActive()) {
 			return;
 		}
 		GraphicalEditPart editPart = getEditPartForPictogramElement(pe);
 		if (editPart != null && editPart instanceof IPictogramElementEditPart) {
 			IPictogramElementEditPart ep = (IPictogramElementEditPart) editPart;
 			IPictogramElementDelegate delegate = ep.getPictogramElementDelegate();
 			delegate.setForceRefresh(true);
 			editPart.refresh();
 			delegate.setForceRefresh(false);
 		}
 	}
 
 	@Override
 	public void refreshPalette() {
 		PaletteRoot pr = getPaletteRoot();
 		if (pr instanceof GFPaletteRoot) {
 			GFPaletteRoot gpr = (GFPaletteRoot) pr;
 			gpr.updatePaletteEntries();
 		}
 	}
 
 	@Override
 	public void refreshRenderingDecorators(PictogramElement pe) {
 		GraphicalEditPart ep = getEditPartForPictogramElement(pe);
 		if (ep instanceof IShapeEditPart) {
 			IShapeEditPart sep = (IShapeEditPart) ep;
 			sep.refreshDecorators();
 		}
 	}
 
 	private void refreshTitle() {
 		String lastSegment = getDiagramTypeProvider().getDiagram().eResource().getURI().lastSegment();
 		setPartName(lastSegment);
 	}
 
 	@Override
 	public void refreshTitleToolTip() {
 		setTitleToolTip(getTitleToolTip());
 	}
 
 	/**
 	 * Register action.
 	 * 
 	 * @param action
 	 *            the action
 	 */
 	protected void registerAction(IAction action) {
 		if (action == null)
 			return;
 		getActionRegistry().registerAction(action);
 
 		if (action.getActionDefinitionId() != null) {
 			IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
 			hs.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
 		}
 
 		@SuppressWarnings("unchecked")
 		List<String> selectionActions = getSelectionActions();
 		selectionActions.add(action.getId());
 	}
 
 	/**
 	 * Register diagram change listener. Since the diagram comes from a
 	 * ModelEditorInput and ModelEditor input provides an own ResourceSet for
 	 * every editor instance, we can safely register a listener with the
 	 * provided ResourceSet: we will be notified only by events regarding the
 	 * diagram!
 	 * 
 	 */
 	private void registerDiagramResourceSetListener() {
 		diagramChangeListener = new DiagramChangeListener(this);
 		TransactionalEditingDomain eDomain = getEditingDomain();
 		eDomain.addResourceSetListener(diagramChangeListener);
 	}
 
 	/**
 	 * Registers a listener for changes of business objects in the resource set
 	 * of the editor.
 	 */
 	protected void registerBOListener() {
 		domainModelListener = new DomainModelChangeListener(this);
 		TransactionalEditingDomain eDomain = getEditingDomain();
 		eDomain.addResourceSetListener(domainModelListener);
 	}
 
 	private void unregisterDiagramResourceSetListener() {
 		if (diagramChangeListener != null) {
 			diagramChangeListener.stopListening();
 			TransactionalEditingDomain eDomain = getEditingDomain();
 			eDomain.removeResourceSetListener(diagramChangeListener);
 		}
 	}
 
 	protected void unregisterBOListener() {
 		if (domainModelListener != null) {
 			TransactionalEditingDomain eDomain = getEditingDomain();
 			eDomain.removeResourceSetListener(domainModelListener);
 		}
 	}
 
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		// If not the active editor, ignore selection changed.
 		boolean editorIsActive = getSite().getPage().isPartVisible(this);
 		if (!editorIsActive) {
 			// Check if we are a page of the active multipage editor
 			IEditorPart activeEditor = getSite().getPage().getActiveEditor();
 			if (activeEditor != null) {
 				// Check if the top level editor if it is active
 				editorIsActive = getSite().getPage().isPartVisible(activeEditor);
 				if (activeEditor instanceof MultiPageEditorPart) {
 					Object selectedPage = ((MultiPageEditorPart) activeEditor).getSelectedPage();
 					if (!(selectedPage instanceof DiagramEditorInternal)) {
 						// Editor is active but the diagram sub editor is not
 						// its active page
 						editorIsActive = false;
 					}
 				}
 			}
 		}
 		if (editorIsActive) {
 
 			// long start = System.nanoTime();
 			// this is where we should check the selection source (part)
 			// * for CNF view the link flag must be obeyed
 			// this would however require a dependency to
 			// org.eclipse.ui.navigator
 			if (part instanceof CommonNavigator) {
 				if (!((CommonNavigator) part).isLinkingEnabled()) {
 					return;
 				}
 			}
 			// useful selection ??
 			if (selection instanceof IStructuredSelection) {
 				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 				List<PictogramElement> peList = new ArrayList<PictogramElement>();
 				// Collect all Pictogram Elements for all selected domain
 				// objects into one list
 				for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
 					Object object = iterator.next();
 					if (object instanceof EObject) {
 						// Find the Pictogram Elements for the given domain
 						// object via the standard link service
 						List<PictogramElement> referencingPes = Graphiti.getLinkService().getPictogramElements(
 								getDiagramTypeProvider().getDiagram(), (EObject) object);
 						if (referencingPes.size() > 0) {
 							peList.addAll(referencingPes);
 						}
 					} else {
 						// For non-EMF domain objects use the registered
 						// notification service for finding
 						PictogramElement[] relatedPictogramElements = getDiagramTypeProvider().getNotificationService()
 								.calculateRelatedPictogramElements(new Object[] { object });
 						for (int i = 0; i < relatedPictogramElements.length; i++) {
 							peList.add(relatedPictogramElements[i]);
 						}
 					}
 				}
 
 				// Do the selection in the diagram (in case there is something
 				// to select)
 				PictogramElement[] pes = null;
 				if (peList.size() > 0) {
 					pes = peList.toArray(new PictogramElement[peList.size()]);
 				}
 				if (pes != null && pes.length > 0) {
 					selectPictogramElements(pes);
 				}
 
 			}
 		}
 		super.selectionChanged(part, selection);
 	}
 
 	@Override
 	public void selectPictogramElements(PictogramElement[] pictogramElements) {
 		List<EditPart> editParts = new ArrayList<EditPart>();
 		Map<?, ?> editPartRegistry = getGraphicalViewer().getEditPartRegistry();
 		if (editPartRegistry != null) {
 			for (int i = 0; i < pictogramElements.length; i++) {
 				PictogramElement pe = pictogramElements[i];
 				Object obj = editPartRegistry.get(pe);
 				if (obj instanceof EditPart) {
 					editParts.add((EditPart) obj);
 				}
 			}
 			getSite().getSelectionProvider().setSelection(new StructuredSelection(editParts));
 			if (editParts.size() > 0) {
 				final EditPart editpart = editParts.get(0);
 				if (!(editpart instanceof IConnectionEditPart)) {
 					// if the editPart is newly created it is possible that his
 					// figure
 					// has not a valid bounds. Hence we have to wait for the UI
 					// update
 					// (for the validation of the figure tree). Otherwise the
 					// reveal
 					// method can't work correctly.
 					Display.getDefault().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							getGraphicalViewer().reveal(editpart);
 						}
 					});
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the auto refresh.
 	 * 
 	 * @param b
 	 *            the new auto refresh
 	 */
 	protected void setAutoRefresh(boolean b) {
 		autoRefresh = b;
 	}
 
 	private void setConfigurationProvider(IConfigurationProvider configurationProvider) {
 
 		this.configurationProvider = configurationProvider;
 
 		// initialize configuration-provider depending on this editor
 		this.configurationProvider.setWorkbenchPart(this);
 
 		if (getGraphicalViewer() != null)
 			initializeGraphicalViewer();
 
 		DefaultEditDomain editDomain = new DefaultEditDomain(this);
 		CommandStack commandStack = new GFCommandStack(configurationProvider, getEditingDomain());
 		editDomain.setCommandStack(commandStack);
 		setEditDomain(editDomain);
 	}
 
 	@Override
 	public void setFocus() {
 		if (getGraphicalViewer() == null)
 			return;
 
 		super.setFocus();
 		getBehavior().setFocus();
 		if (REFRESH_ON_GAINED_FOCUS) {
 			refresh();
 		}
 	}
 
 	/**
 	 * Sets the mouse location.
 	 * 
 	 * @param x
 	 *            the x
 	 * @param y
 	 *            the y
 	 */
 	void setMouseLocation(int x, int y) {
 		getMouseLocation().setLocation(x, y);
 	}
 
 	@Override
 	public void setPictogramElementForSelection(PictogramElement pictogramElementForSelection) {
 		if (pictogramElementForSelection == null)
 			this.pictogramElementsForSelection = null;
 		else
 			this.pictogramElementsForSelection = new PictogramElement[] { pictogramElementForSelection };
 	}
 
 	@Override
 	public void setPictogramElementsForSelection(PictogramElement pictogramElementsForSelection[]) {
 		this.pictogramElementsForSelection = pictogramElementsForSelection;
 	}
 
 	public void refreshContent() {
 		Diagram currentDiagram = getDiagramTypeProvider().getDiagram();
 		if (GraphitiInternal.getEmfService().isObjectAlive(currentDiagram)) {
 			refresh();
 		} else {
 			DiagramEditorInput diagramEditorInput = (DiagramEditorInput) getEditorInput();
 			Diagram diagram = diagramEditorInput.getDiagram(); // resolve
 																// diagram in
 																// reloaded
 																// resource
 			IDiagramTypeProvider diagramTypeProvider = getConfigurationProvider().getDiagramTypeProvider();
 			diagramTypeProvider.resourceReloaded(diagram);
 			initRefresh(); // clean performance hashtables which have references
 							// to old proxies
 			setPictogramElementsForSelection(null);
 			getGraphicalViewer().setContents(diagram); // create new edit parts
 			handleAutoUpdateAtReset(diagram, diagramTypeProvider);
 		}
 	}
 
 	public boolean isDirectEditingActive() {
 		return directEditingActive;
 	}
 
 	public void setDirectEditingActive(boolean directEditingActive) {
 		this.directEditingActive = directEditingActive;
 		getContextButtonManager().hideContextButtonsInstantly();
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		getDiagramTypeProvider().postInit();
 		getEditDomain().getCommandStack().addCommandStackEventListener(cmdStackListener);
 	}
 
 	@Override
 	public boolean isDirty() {
 		return getBehavior().isDirty();
 
 	}
 
 	@Override
 	public TransactionalEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	/**
 	 * 
 	 * @return the resource set
 	 */
 	@Override
 	public ResourceSet getResourceSet() {
 		ResourceSet ret = null;
 		TransactionalEditingDomain editingDomain = getEditingDomain();
 		if (editingDomain != null) {
 			ret = editingDomain.getResourceSet();
 		}
 		return ret;
 	}
 
 	private void setTransactionalEditingDomain(TransactionalEditingDomain editingDomain) {
 		this.editingDomain = editingDomain;
 	}
 
 	/**
 	 * Gets the model editor.
 	 * 
 	 * @return the model editor
 	 */
 	private DiagramEditorBehavior getBehavior() {
 		return behavior;
 	}
 
 	public boolean isMultipleRefreshSupressionActive() {
 		return true;
 	}
 
 	protected void selectBufferedPictogramElements() {
 		if (getPictogramElementsForSelection() != null) {
 			selectPictogramElements(getPictogramElementsForSelection());
 			setPictogramElementsForSelection(null);
 		}
 	}
 }
