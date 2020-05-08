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
  *    mwenz - Bug 331715: Support for rectangular grids in diagrams
  *    mwenz - Bug 332964: Enable setting selection for non-EMF domain models and
  *                        when embedded into a multi-page editor
  *    mwenz - Bug 336075 - DiagramEditor accepts URIEditorInput
  *    mwenz - Bug 329523 - Add notification of DiagramTypeProvider after saving a diagram
  *    jpasch - Bug 323025 ActionBarContributor cleanup
  *    mwenz - Bug 345347 - There should be a way to not allow other plugins to contribute to the diagram context menu
  *    mwenz - Bug 346932 - Navigation history broken
  *    mwenz - Bug 356828 - Escaped diagram name is used as editor title
  *    mwenz - Bug 356218 - Added hasDoneChanges updates to update diagram feature
  *                         and called features via editor command stack to check it
  *    Felix Velasco (mwenz) - Bug 323351 - Enable to suppress/reactivate the speed buttons
  *    Bug 336488 - DiagramEditor API
  *    mwenz - Bug 367204 - Correctly return the added PE inAbstractFeatureProvider's addIfPossible method
  *    mwenz - Bug 324556 - Prevent invisible shapes to be selected to avoid IllegalArgumentException
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
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
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
 import org.eclipse.gef.ui.parts.GraphicalEditor;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
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
 import org.eclipse.graphiti.ui.internal.action.PasteAction;
 import org.eclipse.graphiti.ui.internal.action.PrintGraphicalViewerAction;
 import org.eclipse.graphiti.ui.internal.action.RemoveAction;
 import org.eclipse.graphiti.ui.internal.action.SaveImageAction;
 import org.eclipse.graphiti.ui.internal.action.ToggleContextButtonPadAction;
 import org.eclipse.graphiti.ui.internal.action.UpdateAction;
 import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
 import org.eclipse.graphiti.ui.internal.config.ConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
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
 import org.eclipse.graphiti.ui.internal.parts.IConnectionEditPart;
 import org.eclipse.graphiti.ui.internal.util.gef.ScalableRootEditPartAnimated;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.commands.ActionHandler;
 import org.eclipse.jface.util.TransferDropTargetListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
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
  * This is the main class for the Graphiti diagram editor. It represents the
  * editor to Eclipse and therefore implements {@link IEditorPart}. The
  * implementation is based upon a GEF editor implementation (
  * {@link GraphicalEditorWithFlyoutPalette}) and enhances it with
  * Graphiti-specific stuff.<br>
  * This editor is registered as an Eclipse editor using the extension point
  * org.eclipse.ui.editors. Therefore the Eclipse standard methods can be used to
  * open a new diagram editor. The associated {@link IEditorInput} object is a
  * subclass of {@link DiagramEditorInput}, but using another type of input is
  * also ok as long as it can be adapted to an IFile that can be reolved within
  * the workspace of is a {@link URIEditorInput}. These types of input objects
  * will be converted to a corresponding {@link DiagramEditorInput} when the
  * editor is initialized (see {@link #init(IEditorSite, IEditorInput)}).<br>
  * Any clients extending this class should also contribute their editor to the
  * Eclipse editor extension point to gain full advantage of the Eclipse editor
  * integration of Graphiti.<br>
  * There are a lot of aspects this class needs to deal with; the larger aspects
  * are separated into other classes which share the lifecycle with the
  * {@link DiagramEditor} instance. This means they are instantiated when a new
  * diagram editor is created and exist until the editor is closed again. There
  * are default implementations for all of these aspects, see the
  * Default*Behavior classes in this package. The following aspects are
  * separated:
  * <ul>
  * <li>Markers: Handles everything about markers in the editor. See
  * {@link DefaultMarkerBehavior} for the default implementation. Override
  * {@link #createMarkerBehavior()} to change the default behavior.</li>
  * <li>Palette: Handles everything about the palette in the editor. See
  * {@link DefaultPaletteBehavior} for the default implementation. Override
  * {@link #createPaletteBehaviour()} to change the default behavior.</li>
  * <li>Persistence: Handles everything about loading, saving and the dirty state
  * in the editor. See {@link DefaultPersistencyBehavior} for the default
  * implementation. Override {@link #createPersistencyBehavior()} to change the
  * default behavior.</li>
  * <li>Refreshing: Handles everything about refreshing the editor (refreshing
  * means that the editor shows what's defined in the pictogram model). See
  * {@link DefaultRefreshBehavior} for the default implementation. Override
  * {@link #createRefreshBehavior()} to change the default behavior.</li>
  * <li>Update: Handles everything about updating the editor (updating means that
  * the pictogram model is updated to reflect any changes done to the domain
  * model - your business objects - or to the way objects shall be visualized).
  * See {@link DefaultMarkerBehavior} for the default implementation. Override
  * {@link #createMarkerBehavior()} to change the default behavior.</li>
  * </ul>
  * All the other aspects are dealt with directly within this class. One of the
  * larger aspects implemented here is selection handling, which would have been
  * awkward if separated out.
  * 
  */
 public class DiagramEditor extends GraphicalEditorWithFlyoutPalette implements IDiagramEditor,
 		ITabbedPropertySheetPageContributor, IEditingDomainProvider {
 
 	/**
 	 * The ID of the {@link DiagramEditor} as it is registed with the
 	 * org.eclipse.ui.editors extension point.
 	 */
 	public static final String DIAGRAM_EDITOR_ID = "org.eclipse.graphiti.ui.editor.DiagramEditor"; //$NON-NLS-1$
 
 	private final DefaultUpdateBehavior updateBehavior;
 	private final DefaultPaletteBehavior paletteBehaviour;
 	private final DefaultPersistencyBehavior persistencyBehavior;
 	private final DefaultMarkerBehavior markerBehavior;
 	private final DefaultRefreshBehavior refreshBehavior;
 
 	private CommandStackEventListener gefCommandStackListener;
 	private DiagramChangeListener diagramChangeListener;
 	private DomainModelChangeListener domainModelListener;
 
 	private DiagramScrollingBehavior diagramScrollingBehavior;
 
 	private PictogramElement pictogramElementsForSelection[];
 
 	private String contributorId;
 	private IConfigurationProvider configurationProvider;
 
 	private KeyHandler keyHandler;
 	private Point mouseLocation;
 
 	private boolean directEditingActive = false;
 
 	/**
 	 * Creates a new diagram editor and cares about the creation of the
 	 * different behavior extensions by delegating to the various
 	 * create*Behavior() methods.
 	 */
 	public DiagramEditor() {
 		super();
 		markerBehavior = createMarkerBehavior();
 		updateBehavior = createUpdateBehavior();
 		paletteBehaviour = createPaletteBehaviour();
 		persistencyBehavior = createPersistencyBehavior();
 		refreshBehavior = createRefreshBehavior();
 	}
 
 	// ------------------ Behaviors --------------------------------------------
 
 	/**
 	 * Creates the behavior extension that deals with markers. See
 	 * {@link DefaultMarkerBehavior} for details and the default implementation.
 	 * Override to change the marker behavior.
 	 * 
 	 * @return a new instance of {@link DefaultMarkerBehavior}
 	 * @since 0.9
 	 */
 	protected DefaultMarkerBehavior createMarkerBehavior() {
 		return new DefaultMarkerBehavior(this);
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the update handling. See
 	 * {@link DefaultUpdateBehavior} for details and the default implementation.
 	 * Override to change the update behavior.
 	 * 
 	 * @return a new instance of {@link DefaultUpdateBehavior}
 	 * @since 0.9
 	 */
 	protected DefaultUpdateBehavior createUpdateBehavior() {
 		return new DefaultUpdateBehavior(this);
 	}
 
 	/**
 	 * Returns the instance of the update behavior that is used with this
 	 * editor. To change the behavior override {@link #createUpdateBehavior()}.
 	 * 
 	 * @return the used instance of the update behavior, by default a
 	 *         {@link DefaultUpdateBehavior}.
 	 * @since 0.9
 	 */
 	public final DefaultUpdateBehavior getUpdateBehavior() {
 		return updateBehavior;
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the palette handling. See
 	 * {@link DefaultPaletteBehavior} for details and the default
 	 * implementation. Override to change the palette behavior.
 	 * 
 	 * @return a new instance of {@link DefaultPaletteBehavior}
 	 * @since 0.9
 	 */
 	protected DefaultPaletteBehavior createPaletteBehaviour() {
 		return new DefaultPaletteBehavior(this);
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the persistence handling.
 	 * See {@link DefaultPersistencyBehavior} for details and the default
 	 * implementation. Override to change the persistence behavior.
 	 * 
 	 * @return a new instance of {@link DefaultPersistencyBehavior}
 	 * @since 0.9
 	 */
 	protected DefaultPersistencyBehavior createPersistencyBehavior() {
 		return new DefaultPersistencyBehavior(this);
 	}
 
 	/**
 	 * Creates the behavior extension that deals with the refresh handling. See
 	 * {@link DefaultRefreshBehavior} for details and the default
 	 * implementation. Override to change the refresh behavior.
 	 * 
 	 * @return a new instance of {@link DefaultRefreshBehavior}
 	 * @since 0.9
 	 */
 	protected DefaultRefreshBehavior createRefreshBehavior() {
 		return new DefaultRefreshBehavior(this);
 	}
 
 	/**
 	 * Returns the instance of the refresh behavior that is used with this
 	 * editor. To change the behavior override {@link #createRefreshBehavior()}.
 	 * 
 	 * @return the used instance of the refresh behavior, by default a
 	 *         {@link DefaultRefreshBehavior}.
 	 * @since 0.9
 	 */
 	public final DefaultRefreshBehavior getRefreshBehavior() {
 		return refreshBehavior;
 	}
 
 	// ---------------------- Synchronization hooks between behaviors ------- //
 
 	/**
 	 * Hook that is called by the holder of the
 	 * {@link TransactionalEditingDomain} ({@link DefaultUpdateBehavior} or a
 	 * subclass of it) after the editing domain has been initialized. Can be
 	 * used to e.g. register additional listeners on the domain.<br>
 	 * The default implementation notifies the marker behavior extension to
 	 * register its listeners.
 	 * 
 	 * @since 0.9
 	 */
 	public void editingDomainInitialized() {
 		markerBehavior.initialize();
 	}
 
 	/**
 	 * Should be called (e.g. by the various behavior instances) before mass EMF
 	 * resource operations are triggered (e.g. saving all resources). Can be
 	 * used to disable eventing for performance reasons. See
 	 * {@link #enableAdapters()} as well.<br>
 	 * Important note: make sure that you re-enable eventing using
 	 * {@link #enableAdapters()} after the operation has finished (best in a
 	 * finally clause to do that also in case of exceptions), otherwise strange
 	 * errors may happen.
 	 * 
 	 * @since 0.9
 	 */
 	public void disableAdapters() {
 		markerBehavior.disableProblemIndicationUpdate();
 		updateBehavior.setAdapterActive(false);
 	}
 
 	/**
 	 * Should be called by the various behavior instances after mass EMF
 	 * resource operations have been triggered (e.g. saving all resources). Can
 	 * be used to re-enable eventing after it was disabled for performance
 	 * reasons. See {@link #disableAdapters()} as well.<br>
 	 * Must be called after {@link #disableAdapters()} has been called and the
 	 * operation has finshed (best in a finally clause to also enable the
 	 * exception case), otherwise strange errors may occur within the editor.
 	 * 
 	 * @since 0.9
 	 */
 	public void enableAdapters() {
 		markerBehavior.enableProblemIndicationUpdate();
 		updateBehavior.setAdapterActive(true);
 	}
 
 	// ------------------ Initializazion ---------------------------------------
 
 	/**
 	 * Does the initialization of the editor. The default implementation cares
 	 * about:
 	 * <ol>
 	 * <li>converting the passed {@link IEditorInput} to a
 	 * {@link DiagramEditorInput}. In case this fails, a
 	 * {@link PartInitException} is thrown.</li>
 	 * <li>creating the editing domain by delegating to the update behavior
 	 * extension, see {@link DefaultUpdateBehavior#createEditingDomain()} for
 	 * details</li>
 	 * <li>initializing the underlying GEF editor by delegating to super</li>
 	 * <li>initializing the update behavior extension (the order is important
 	 * here as this must happen after initializing the GEF editor!)</li>
 	 * <li>triggering the migration of diagram data if necessary</li>
 	 * </ol>
 	 * Any clients overriding this method have to make sure that they they
 	 * always call <code>super.init(site, input)</code>.
 	 * 
 	 * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
 	 * @param site
 	 *            the Eclipse {@link IEditorSite} that will host this editor
 	 * @param input
 	 *            the editor input that shall be used. Note that this method
 	 *            will exchange the input instance in case it is no
 	 *            {@link DiagramEditorInput}.
 	 * 
 	 */
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		// Eclipse may call us with other inputs when a file is to be
 		// opened. Try to convert this to a valid diagram input.
 		if (!(input instanceof IDiagramEditorInput)) {
 			IEditorInput newInput = EditorInputAdapter.adaptToDiagramEditorInput(input);
 			if (!(newInput instanceof IDiagramEditorInput)) {
 				throw new PartInitException("Unknown editor input: " + input); //$NON-NLS-1$
 			}
 			input = newInput;
 		}
 
 		getUpdateBehavior().createEditingDomain();
 
 		try {
 			// In next line GEF calls setSite(), setInput(),
 			// getEditingDomain(), ...
 			super.init(site, input);
 		} catch (RuntimeException e) {
 			throw new PartInitException("Can not initialize editor", e); //$NON-NLS-1$
 		}
 
 		getUpdateBehavior().init();
 
 		migrateDiagramModelIfNecessary();
 	}
 
 	/**
 	 * Sets the given {@link IEditorInput} object as the input for this editor.
 	 * It must be of type {@link DiagramEditorInput} otherwise an
 	 * {@link IllegalArgumentException} is thrown.<br>
 	 * The default implementation here cares about loading the diagram from the
 	 * EMF {@link Resource} the input points to, sets the ID of the
 	 * {@link IDiagramTypeProvider} for the diagram given in the input,
 	 * registers listeners (by delegating to
 	 * {@link #registerDiagramResourceSetListener()} and
 	 * {@link #registerBusinessObjectsListener()}) and does the refreshing of
 	 * the editor UI.
 	 * 
 	 * @param input
 	 *            the {@link DiagramEditorInput} instance to use within this
 	 *            editor.
 	 */
 	protected void setInput(IEditorInput input) {
 		super.setInput(input);
 		// determine filename
 		Assert.isNotNull(input, "The IEditorInput must not be null"); //$NON-NLS-1$
 
 		if (!(input instanceof IDiagramEditorInput)) {
 			throw new IllegalArgumentException("The IEditorInput has the wrong type: " + input.getClass()); //$NON-NLS-1$
 		}
 
 		IDiagramEditorInput diagramEditorInput = (IDiagramEditorInput) input;
 		Diagram diagram = persistencyBehavior.loadDiagram(diagramEditorInput.getUri());
 
 		// can happen if editor is started with invalid URI
 		Assert.isNotNull(diagram, "No Diagram found for URI '" //$NON-NLS-1$
 				+ "'. . See the error log for details."); //$NON-NLS-1$
 
 		String providerId = diagramEditorInput.getProviderId();
 		// if provider is null then take the first installed provider for
 		// this diagram type
 		if (providerId == null) {
 			providerId = GraphitiUi.getExtensionManager().getDiagramTypeProviderId(diagram.getDiagramTypeId());
 			diagramEditorInput.setProviderId(providerId);
 		}
 
 		Assert.isNotNull(providerId, "DiagramEditorInput does not convey a Provider ID '" + diagramEditorInput //$NON-NLS-1$
 				+ "'. . See the error log for details."); //$NON-NLS-1$
 
 		// get according diagram-type-provider
 		IDiagramTypeProvider diagramTypeProvider = GraphitiUi.getExtensionManager().createDiagramTypeProvider(
 				providerId);
 		Assert.isNotNull(diagramTypeProvider, "could not find diagram type provider for " + diagram.getDiagramTypeId()); //$NON-NLS-1$
 
 		diagramTypeProvider.init(diagram, this);
 		IConfigurationProvider configurationProvider = new ConfigurationProvider(this, diagramTypeProvider);
 		setConfigurationProvider(configurationProvider);
 		refreshBehavior.handleAutoUpdateAtStartup();
 
 		registerBusinessObjectsListener();
 		registerDiagramResourceSetListener();
 
 		refreshTitle();
 	}
 
 	/**
 	 * Initializes the action registry with the predefined actions (update,
 	 * remove, delete, copy, paste, zooming, direct editing, alignment and
 	 * toggling actions for the diagram grip and hiding of the context button
 	 * pad.
 	 * 
 	 * @param zoomManager
 	 *            the GEF zoom manager to use
 	 * 
 	 * @since 0.9
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
 
 		// Bug 323351: Add button to toggle a flag if the context pad buttons
 		// shall be shown or not
 		IAction toggleContextButtonPad = new ToggleContextButtonPadAction(this);
 		toggleContextButtonPad.setChecked(false);
 		actionRegistry.registerAction(toggleContextButtonPad);
 		// End bug 323351
 	}
 
 	/**
 	 * Creates the UI of the editor by delegating to the
 	 * <code>super.createPartControl</code> method. The default implementation
 	 * here also registers the command stack listener to correctly reflect the
 	 * dirty state of the editor.
 	 */
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		getDiagramTypeProvider().postInit();
 		gefCommandStackListener = new CommandStackEventListener() {
 
 			public void stackChanged(CommandStackEvent event) {
 				// Only fire if triggered from UI thread
 				if (Display.getCurrent() != null) {
 					firePropertyChange(IEditorPart.PROP_DIRTY);
 
 					// Promote the changes to the command stack also to the
 					// action bars and registered actions to correctly reflect
 					// e.g. undo/redo in the menu (introduced to enable removing
 					// NOP commands from the command stack
 					commandStackChanged(event);
 				}
 			}
 		};
 		getEditDomain().getCommandStack().addCommandStackEventListener(gefCommandStackListener);
 	}
 
 	/**
 	 * Creates the GraphicalViewer on the specified {@link Composite} and
 	 * initializes it.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
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
 	 * Called to initialize the editor with its content. Here everything is
 	 * done, which is dependent of the IConfigurationProvider.
 	 * 
 	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
 	 */
 	protected void initializeGraphicalViewer() {
 
 		super.initializeGraphicalViewer();
 
 		// register Actions
 		IFeatureProvider featureProvider = getConfigurationProvider().getDiagramTypeProvider().getFeatureProvider();
 		if (featureProvider != null) {
 			IPrintFeature pf = featureProvider.getPrintFeature();
 			if (pf != null) {
 				registerAction(new PrintGraphicalViewerAction(getConfigurationProvider().getWorkbenchPart(), pf));
 			}
 		}
 
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
 
 		paletteBehaviour.initializeViewer();
 
 		getGraphicalViewer().getControl().addMouseMoveListener(new MouseMoveListener() {
 
 			public void mouseMove(MouseEvent e) {
 				setMouseLocation(e.x, e.y);
 			}
 		});
 
 		getGraphicalViewer().addDropTargetListener(
 				(TransferDropTargetListener) new ObjectsTransferDropTargetListener(getGraphicalViewer()));
 
 		getGraphicalViewer()
 				.addDropTargetListener(new GFTemplateTransferDropTargetListener(getGraphicalViewer(), this));
 	}
 
 	/**
 	 * Called to configure the editor, before it receives its content. The
 	 * default-implementation is for example doing the following: configure the
 	 * ZoomManager, registering Actions... Here everything is done, which is
 	 * independent of the IConfigurationProvider.
 	 * 
 	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
 	 */
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 
 		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();
 
 		ScalableRootEditPartAnimated rootEditPart = new ScalableRootEditPartAnimated(viewer) {
 
 
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
 		configurationProvider.setContextButtonManager(new ContextButtonManagerForPad(this, configurationProvider
 				.getResourceRegistry()));
 
 		/* sw: make scroll bars always visible */
 		if (getDiagramScrollingBehavior() == DiagramScrollingBehavior.SCROLLBARS_ALWAYS_VISIBLE) {
 			GFFigureCanvas figureCanvas = getGFFigureCanvas();
 			if (figureCanvas != null) {
 				figureCanvas.setScrollBarVisibility(FigureCanvas.ALWAYS);
 			}
 		}
 	}
 
 	// ------------------- Dirty state -----------------------------------------
 
 	/**
 	 * Updates the UI to correctly reflect the dirty state of the editor. The
 	 * default implementation does this by firing a
 	 * {@link IEditorPart#PROP_DIRTY} property change.
 	 * 
 	 * @since 0.9
 	 */
 	public void updateDirtyState() {
 		firePropertyChange(IEditorPart.PROP_DIRTY);
 	}
 
 	/**
 	 * Called to perform the saving of the editor. The default implementation
 	 * delegates to
 	 * {@link DefaultPersistencyBehavior#saveDiagram(IProgressMonitor)}.
 	 * 
 	 * @param monitor
 	 *            the Eclipse progress monitor to report progress with.
 	 */
	public void doSave(IProgressMonitor monitor) {
 		persistencyBehavior.saveDiagram(monitor);
 	}
 
 	/**
 	 * Returns if the editor is currently dirty and needs to be saved or not.
 	 * The default implementation queries the command stack of the EMF
 	 * {@link TransactionalEditingDomain}.
 	 * 
 	 * @return <code>true</code> in case the editor is dirty, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isDirty() {
 		TransactionalEditingDomain editingDomain = getEditingDomain();
 		// Check that the editor is not yet disposed
 		if (editingDomain != null && editingDomain.getCommandStack() != null) {
 			return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 		}
 		return false;
 	}
 
 	// ---------------------- Palette --------------------------------------- //
 
 	/**
 	 * Delegates to the method (or the method in a subclass of)
 	 * {@link DefaultPaletteBehavior#createPaletteViewerProvider()
 	 * #createPaletteViewerProvider()} to create the
 	 * {@link PaletteViewerProvider} used inside the GEF editor.
 	 * 
 	 * @return the {@link PaletteViewerProvider} to use
 	 */
 	protected final PaletteViewerProvider createPaletteViewerProvider() {
 		return paletteBehaviour.createPaletteViewerProvider();
 	}
 
 	/**
 	 * Delegates to the method (or the method in a subclass of)
 	 * {@link DefaultPaletteBehavior#getPalettePreferences()}. To change the
 	 * palette override the behavior there.
 	 * 
 	 * @return the {@link PaletteViewerProvider} preferences to use.
 	 */
 	protected final FlyoutPreferences getPalettePreferences() {
 		return paletteBehaviour.getPalettePreferences();
 	}
 
 	/**
 	 * Returns the {@link PaletteRoot} to use in the GEF editor by delegating to
 	 * {@link DefaultPaletteBehavior#getPaletteRoot()}.
 	 * 
 	 * @return the {@link PaletteRoot} to use
 	 */
 	protected final PaletteRoot getPaletteRoot() {
 		return paletteBehaviour.getPaletteRoot();
 	}
 
 	/**
 	 * Refreshes to palette to correctly reflect all available creation tools
 	 * for the available create features and the currently enabled selection
 	 * tools
 	 * 
 	 * @since 0.9
 	 */
 	public final void refreshPalette() {
 		paletteBehaviour.refreshPalette();
 	}
 
 	// ---------------------- Context Menu ---------------------------------- //
 
 	/**
 	 * Returns a new {@link ContextMenuProvider}. Clients can return null, if no
 	 * context-menu shall be displayed.
 	 * 
 	 * @return A new instance of {@link ContextMenuProvider}.
 	 * @since 0.9
 	 */
 	protected ContextMenuProvider createContextMenuProvider() {
 		return new DiagramEditorContextMenuProvider(getGraphicalViewer(), getActionRegistry(), getDiagramTypeProvider());
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
 	protected boolean shouldRegisterContextMenu() {
 		return true;
 	}
 
 	// ---------------------- Listeners ------------------------------------- //
 
 	/**
 	 * Hook to register listeners for diagram changes. The listener will be
 	 * notified with all events and has to filter for the ones regarding the
 	 * diagram.<br>
 	 * Note that additional listeners registered here should also be
 	 * unregistered in {@link #unregisterDiagramResourceSetListener()}.
 	 * 
 	 * @since 0.9
 	 */
 	protected void registerDiagramResourceSetListener() {
 		diagramChangeListener = new DiagramChangeListener(this);
 		TransactionalEditingDomain eDomain = getEditingDomain();
 		eDomain.addResourceSetListener(diagramChangeListener);
 	}
 
 	/**
 	 * Hook that is called to register listeners for changes of the business
 	 * objects (domain objects) in the resource set of the editor. The default
 	 * implementation registers the {@link DomainModelChangeListener}.<br>
 	 * Note that additional listeners registered here should also be
 	 * unregistered in {@link #unregisterBusinessObjectsListener()}.
 	 * 
 	 * @since 0.9
 	 */
 	protected void registerBusinessObjectsListener() {
 		domainModelListener = new DomainModelChangeListener(this);
 		TransactionalEditingDomain eDomain = getEditingDomain();
 		eDomain.addResourceSetListener(domainModelListener);
 	}
 
 	/**
 	 * Hook to unregister the listeners for diagram changes.
 	 * 
 	 * @see #registerDiagramResourceSetListener()
 	 * @since 0.9
 	 */
 	protected void unregisterDiagramResourceSetListener() {
 		if (diagramChangeListener != null) {
 			diagramChangeListener.stopListening();
 			TransactionalEditingDomain eDomain = getEditingDomain();
 			eDomain.removeResourceSetListener(diagramChangeListener);
 		}
 	}
 
 	/**
 	 * Hook that is called to unregister the listeners for changes of the
 	 * business objects (domain objects).
 	 * 
 	 * @see DiagramEditor#registerBusinessObjectsListener()
 	 * @since 0.9
 	 */
 	protected void unregisterBusinessObjectsListener() {
 		if (domainModelListener != null) {
 			TransactionalEditingDomain eDomain = getEditingDomain();
 			eDomain.removeResourceSetListener(domainModelListener);
 		}
 	}
 
 	// ---------------------- Refresh --------------------------------------- //
 
 	/**
 	 * Refreshes the editor title to show the name of the diagram
 	 * 
 	 * @since 0.9
 	 */
 	public void refreshTitle() {
 		String name = getConfigurationProvider().getDiagramTypeProvider().getDiagramTitle();
 		if (name == null || name.length() == 0) {
 			name = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
 		}
 		if (name == null || name.length() == 0) {
 			name = URI.decode(getDiagramTypeProvider().getDiagram().eResource().getURI().lastSegment());
 		}
 		setPartName(name);
 	}
 
 	/**
 	 * Refreshes the tooltip displayed for the editor title tab according to
 	 * what is returned in {@link #getTitleToolTip()}.
 	 * 
 	 * @since 0.9
 	 */
 	public void refreshTitleToolTip() {
 		setTitleToolTip(getTitleToolTip());
 	}
 
 	/**
 	 * Triggers a complete refresh of the editor (content, title, tooltip,
 	 * palette and decorators) by delegating to
 	 * {@link DefaultRefreshBehavior#refresh()}.
 	 * 
 	 * @since 0.9
 	 */
 	public void refresh() {
 		refreshBehavior.refresh();
 	}
 
 	/**
 	 * Refreshes the content of the editor (what's shown inside the diagram
 	 * itself).
 	 * 
 	 * @since 0.9
 	 */
 	public void refreshContent() {
 		Diagram currentDiagram = getDiagramTypeProvider().getDiagram();
 		if (GraphitiInternal.getEmfService().isObjectAlive(currentDiagram)) {
 			refresh();
 		} else {
 			IDiagramEditorInput diagramEditorInput = (IDiagramEditorInput) getEditorInput();
 			// resolve diagram in reloaded resource
 			Diagram diagram = persistencyBehavior.loadDiagram(diagramEditorInput.getUri());
 			IDiagramTypeProvider diagramTypeProvider = getConfigurationProvider()
 					.getDiagramTypeProvider();
 			diagramTypeProvider.resourceReloaded(diagram);
 			// clean performance hashtables which have references
 			refreshBehavior.initRefresh();
 			// to old proxies
 			setPictogramElementsForSelection(null);
 			// create new edit parts
 			getGraphicalViewer().setContents(diagram);
 			refreshBehavior.handleAutoUpdateAtReset();
 		}
 	}
 
 	/**
 	 * Refreshes the rendering decorators (image decorators and the like) by
 	 * delegating to
 	 * {@link DefaultRefreshBehavior#refreshRenderingDecorators(PictogramElement)}
 	 * for the given {@link PictogramElement}.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} for which the decorators shall be
 	 *            refreshed.
 	 * 
 	 * @since 0.9
 	 */
 	public void refreshRenderingDecorators(PictogramElement pe) {
 		refreshBehavior.refreshRenderingDecorators(pe);
 	}
 
 	// ====================== standard behaviour ==============================
 
 	/**
 	 * Implements the Eclipse {@link IAdaptable} interface. This implementation
 	 * first delegates to the {@link IToolBehaviorProvider#getAdapter(Class)}
 	 * method and checks if something is returned. In case the return value is
 	 * <code>null</code> it returns adapters for ZoomManager,
 	 * IPropertySheetPage, Diagram, KeyHandler, SelectionSynchronizer and
 	 * IContextButtonManager. It also delegates to the super implementation in
 	 * {@link GraphicalEditorWithFlyoutPalette#getAdapter(Class)}.
 	 * 
 	 * @param type
 	 *            the type to which shall be adapted
 	 * @return the adapter instance
 	 */
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
 		if (type == Diagram.class) {
 			return getDiagramTypeProvider().getDiagram();
 		}
 		if (type == KeyHandler.class) {
 			return getCommonKeyHandler();
 		}
 		if (type == SelectionSynchronizer.class) {
 			return getSelectionSynchronizer();
 		}
 		if (type == IContextButtonManager.class) {
 			return getConfigurationProvider().getContextButtonManager();
 		}
 		return super.getAdapter(type);
 	}
 
 	/**
 	 * Disposes this {@link DiagramEditor} instance and frees all used resources
 	 * and clears all references. Also delegates to all the behavior extensions
 	 * to also free their resources (e.g. and most important is the
 	 * {@link TransactionalEditingDomain} held by the
 	 * {@link DefaultPersistencyBehavior}. Always delegate to
 	 * <code>super.dispose()</code> in case you override this method!
 	 */
 	public void dispose() {
 		unregisterDiagramResourceSetListener();
 		unregisterBusinessObjectsListener();
 
 		if (getConfigurationProvider() != null) {
 			getConfigurationProvider().dispose();
 		}
 
 		if (paletteBehaviour != null) {
 			paletteBehaviour.dispose();
 		}
 
 		// unregister selection listener, registered during createPartControl()
 		if (getSite() != null && getSite().getPage() != null) {
 			getSite().getPage().removeSelectionListener(this);
 		}
 		
 		if (getEditDomain() != null && getEditDomain().getCommandStack() != null) {
 			getEditDomain().getCommandStack().removeCommandStackEventListener(gefCommandStackListener);
 			getEditDomain().getCommandStack().dispose();
 		}
 
 		DefaultUpdateBehavior behavior = getUpdateBehavior();
 		behavior.dispose();
 		RuntimeException exc = null;
 		try {
 			super.dispose();
 		} catch (RuntimeException e) {
 			exc = e;
 		}
 
 		markerBehavior.dispose();
 
 		if (getEditDomain() != null) {
 			getEditDomain().setCommandStack(null);
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
 	public void setFocus() {
 		if (getGraphicalViewer() == null) {
 			return;
 		}
 
 		super.setFocus();
 		getUpdateBehavior().handleActivate();
 	}
 
 	// ---------------------- Selection ------------------------------------- //
 
 	/**
 	 * Returns the {@link PictogramElement}s that are currently selected in the
 	 * diagram editor.
 	 * 
 	 * @return an array of {@link PictogramElement}s.
 	 * 
 	 * @since 0.9
 	 */
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
 	 * Handles a selection changed event that is triggered by any selection
 	 * source, e.g. a browser with "Link to Editor" enabled.<br>
 	 * Checks if the currently active editor is a {@link MultiPageEditorPart}
 	 * with an opened diagram editor inside, tries to find any
 	 * {@link PictogramElement} for the objects in the selection and selects
 	 * them in the diagram.<br>
 	 * Note that in case of the {@link CommonNavigator} as event source, its
 	 * editor linking mechanism must be enabled.
 	 * 
 	 * @param part
 	 *            the source {@link IWorkbenchPart} that triggered the event
 	 * @param selection
 	 *            the new selection (mostly a {@link IStructuredSelection}
 	 *            instance.
 	 * 
 	 * @since 0.9
 	 */
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		// If not the active editor, ignore selection changed.
 		boolean editorIsActive = getSite().getPage().isPartVisible(this);
 		if (!editorIsActive) {
 			// Check if we are a page of the active multi page editor
 			IEditorPart activeEditor = getSite().getPage().getActiveEditor();
 			if (activeEditor != null) {
 				if (activeEditor instanceof MultiPageEditorPart) {
 					Object selectedPage = ((MultiPageEditorPart) activeEditor).getAdapter(DiagramEditor.class);
 					if (selectedPage instanceof DiagramEditor) {
 						// Editor is active and diagram sub editor is its active
 						// page
 						editorIsActive = true;
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
 
 	/**
 	 * Selects the given {@link PictogramElement}s in the diagram.
 	 * 
 	 * @param pictogramElements
 	 *            an array of {@link PictogramElement}s to select.
 	 * @since 0.9
 	 */
 	public void selectPictogramElements(PictogramElement[] pictogramElements) {
 		List<EditPart> editParts = new ArrayList<EditPart>();
 		Map<?, ?> editPartRegistry = getGraphicalViewer().getEditPartRegistry();
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
 			getSite().getSelectionProvider().setSelection(new StructuredSelection(editParts));
 			if (editParts.size() > 0) {
 				final EditPart editpart = editParts.get(0);
 				if (!(editpart instanceof IConnectionEditPart)) {
 					// if the editPart is newly created it is possible that his
 					// figure has not a valid bounds. Hence we have to wait for
 					// the UI update (for the validation of the figure tree).
 					// Otherwise the reveal method can't work correctly.
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							getGraphicalViewer().reveal(editpart);
 						}
 					});
 				}
 			}
 		}
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
 	 * @since 0.9
 	 */
 	protected PictogramElement[] getPictogramElementsForSelection() {
 		return pictogramElementsForSelection;
 	}
 
 	/**
 	 * Sets one {@link PictogramElement} for later selection.
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
 	 * @param pictogramElement
 	 *            the {@link PictogramElement} that shall be stored for later
 	 *            selection
 	 * @since 0.9
 	 */
 	public void setPictogramElementForSelection(PictogramElement pictogramElement) {
 		if (pictogramElement == null) {
 			this.pictogramElementsForSelection = null;
 		} else {
 			this.pictogramElementsForSelection = new PictogramElement[] { pictogramElement };
 		}
 	}
 
 	/**
 	 * Sets {@link PictogramElement}s for later selection.
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
 	 * @param pictogramElements
 	 *            the {@link PictogramElement}s that shall be stored for later
 	 *            selection
 	 * @since 0.9
 	 */
 	public void setPictogramElementsForSelection(PictogramElement pictogramElements[]) {
 		this.pictogramElementsForSelection = pictogramElements;
 	}
 
 	/**
 	 * Triggers the selection for the {@link PictogramElement}s that are stored
 	 * for later selection. Can be called e.g during a general refresh of the
 	 * editor or after another operation needing another selection is finished
 	 * (an example in the framework is direct editing).
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
 	 * @since 0.9
 	 */
 	public void selectBufferedPictogramElements() {
 		if (getPictogramElementsForSelection() != null) {
 			selectPictogramElements(getPictogramElementsForSelection());
 			setPictogramElementsForSelection(null);
 		}
 	}
 
 	// ---------------------- Mouse location -------------------------------- //
 
 	/**
 	 * Gets the current mouse location as a {@link Point}.
 	 * 
 	 * @return the mouse location
 	 * @since 0.9
 	 */
 	public Point getMouseLocation() {
 		if (mouseLocation == null) {
 			mouseLocation = new Point();
 		}
 		return mouseLocation;
 	}
 
 	/**
 	 * Calculates the mouse location depending on scrollbars and zoom factor.
 	 * 
 	 * @param nativeLocation
 	 *            the native location given as {@link Point}
 	 * @return the {@link Point} of the real mouse location
 	 * @since 0.9
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
 
 	private void setMouseLocation(int x, int y) {
 		getMouseLocation().setLocation(x, y);
 	}
 
 	// ---------------------- Other ----------------------------------------- //
 
 	/**
 	 * Returns the KeyHandler with common bindings to be used for both the
 	 * Outline and the Graphical Viewer.
 	 * 
 	 * @return The KeyHandler with common bindings for both the Outline and the
 	 *         Graphical Viewer.
 	 * @since 0.9
 	 */
 	protected KeyHandler getCommonKeyHandler() {
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
 
 	IConfigurationProvider getConfigurationProvider() {
 		return configurationProvider;
 	}
 
 	/**
 	 * Returns the contents {@link EditPart} of this Editor. This is the topmost
 	 * EditPart in the {@link GraphicalViewer}.
 	 * 
 	 * @return The contents {@link EditPart} of this Editor.
 	 * @since 0.9
 	 */
 	public EditPart getContentEditPart() {
 		if (getGraphicalViewer() != null) {
 			return getGraphicalViewer().getContents();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the ID for contributions in the tabbed property sheets by
 	 * delegating to the method {@link IToolBehaviorProvider#getContributorId()}
 	 * .
 	 * 
 	 * @return the contributor id as a {@link String}
 	 * @since 0.9
 	 */
 	public String getContributorId() {
 
 		if (contributorId == null) {
 			IToolBehaviorProvider tbp = getToolBehaviorProvider();
 			if (tbp != null) {
 				contributorId = tbp.getContributorId();
 			}
 		}
 		return contributorId;
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
 	private DiagramScrollingBehavior getDiagramScrollingBehavior() {
 		if (this.diagramScrollingBehavior == null) {
 			IToolBehaviorProvider tbp = getConfigurationProvider().getDiagramTypeProvider().getCurrentToolBehaviorProvider();
 			this.diagramScrollingBehavior = tbp.getDiagramScrollingBehavior();
 		}
 		return this.diagramScrollingBehavior;
 	}
 
 	/**
 	 * Returns the {@link IDiagramTypeProvider} instance associated with this
 	 * {@link DiagramEditor}. There is always a 1:1 relation between the editor
 	 * and the provider.
 	 * 
 	 * @return the associated {@link IDiagramTypeProvider} instance.
 	 * 
 	 * @since 0.9
 	 */
 	public IDiagramTypeProvider getDiagramTypeProvider() {
 		IConfigurationProvider cfgProvider = getConfigurationProvider();
 		if (cfgProvider != null)
 			return cfgProvider.getDiagramTypeProvider();
 		return null;
 	}
 
 	/**
 	 * Returns the GEF edit domain as needed for some of the feature
 	 * functionality in Graphiti; simply a public rewrite of the GEF editor
 	 * super method.
 	 * 
 	 * @return the {@link DefaultEditDomain} used in this editor
 	 * @see GraphicalEditor#getEditDomain()
 	 * 
 	 * @since 0.9
 	 */
 	public DefaultEditDomain getEditDomain() {
 		return super.getEditDomain();
 	}
 
 	/**
 	 * Method to retrieve the GEF {@link EditPart} for a given
 	 * {@link PictogramElement}.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} to retrieve the GEF
 	 *            representation for
 	 * @return the GEF {@link GraphicalEditPart} that represents the given
 	 *         {@link PictogramElement}.
 	 * @since 0.9
 	 */
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
 	 * Method to retrieve the Draw2D {@link IFigure} for a given
 	 * {@link PictogramElement}.
 	 * 
 	 * @param pe
 	 *            the {@link PictogramElement} to retrieve the Draw2D
 	 *            representation for
 	 * @return the Draw2D {@link IFigure} that represents the given
 	 *         {@link PictogramElement}.
 	 * 
 	 * @since 0.9
 	 */
 	public IFigure getFigureForPictogramElement(PictogramElement pe) {
 		GraphicalEditPart ep = getEditPartForPictogramElement(pe);
 		if (ep != null) {
 			return ep.getFigure();
 		}
 		return null;
 	}
 
 	private FigureCanvas getFigureCanvas() {
 		GraphicalViewer viewer = getGraphicalViewer();
 		if (viewer != null) {
 			Control control = viewer.getControl();
 			if (control instanceof FigureCanvas) {
 				return (FigureCanvas) control;
 			}
 		}
 		return null;
 	}
 
 	private GFFigureCanvas getGFFigureCanvas() {
 		GraphicalViewer viewer = getGraphicalViewer();
 		if (viewer != null) {
 			Control control = viewer.getControl();
 			if (control instanceof GFFigureCanvas) {
 				return (GFFigureCanvas) control;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the GEF {@link GraphicalViewer} as it is needed in some Graphiti
 	 * feature implementations. This is simply a public rewrite of the according
 	 * super method.
 	 * 
 	 * @return the {@link GraphicalViewer} used within this editor instance
 	 * @see GraphicalEditor#getGraphicalViewer()
 	 */
 	public GraphicalViewer getGraphicalViewer() {
 		return super.getGraphicalViewer();
 	}
 
 	/**
 	 * Returns the tooltip that shall be displayed when hovering over the editor
 	 * title tab.
 	 * 
 	 * @return the tooltip as a {@link String}
 	 */
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
 	 * Returns the zoom level currently used in the editor.
 	 * 
 	 * @return the zoom level
 	 * @since 0.9
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
 	 * We provide migration from 0.8.0 to 0.9.0. You can override if you want to
 	 * migrate manually. WARNING: If your diagram is under version control, this
 	 * method can cause a check out dialog to be opened etc.
 	 * 
 	 * @since 0.9
 	 */
 	protected void migrateDiagramModelIfNecessary() {
 		final Diagram diagram = getDiagramTypeProvider().getDiagram();
 		if (Graphiti.getMigrationService().shouldMigrate080To090(diagram)) {
 			getEditingDomain().getCommandStack().execute(new RecordingCommand(getEditingDomain()) {
 				@Override
 				protected void doExecute() {
 					Graphiti.getMigrationService().migrate080To090(diagram);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Checks if this editor is alive.
 	 * 
 	 * @return <code>true</code>, if editor contains a model connector and a
 	 *         valid Diagram, <code>false</code> otherwise.
 	 * @since 0.9
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
 	 * Registers the given action with the Eclipse {@link ActionRegistry}.
 	 * 
 	 * @param action
 	 *            the action to register
 	 * @since 0.9
 	 */
 	protected void registerAction(IAction action) {
 		if (action == null) {
 			return;
 		}
 		getActionRegistry().registerAction(action);
 
 		if (action.getActionDefinitionId() != null) {
 			IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
 			hs.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
 		}
 
 		@SuppressWarnings("unchecked")
 		List<String> selectionActions = getSelectionActions();
 		selectionActions.add(action.getId());
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
 
 	/**
 	 * Returns if direct editing is currently active for this editor.
 	 * 
 	 * @return <code>true</code> in case direct editing is currently active
 	 *         within this editor, <code>false</code> otherwise.
 	 * 
 	 * @since 0.9
 	 */
 	public boolean isDirectEditingActive() {
 		return directEditingActive;
 	}
 
 	/**
 	 * Sets that direct editing is now active in the editor or not. Note that
 	 * this flag set to <code>true</code> does not actually start direct editing
 	 * it is simply an indication that prevents certain operations from running
 	 * (e.g. refresh)
 	 * 
 	 * @param directEditingActive
 	 *            <code>true</code> to set the flag to direct editing currently
 	 *            active, <code>false</code> otherwise.
 	 * 
 	 * @since 0.9
 	 */
 	public void setDirectEditingActive(boolean directEditingActive) {
 		this.directEditingActive = directEditingActive;
 		configurationProvider.getContextButtonManager().hideContextButtonsInstantly();
 	}
 
 	/**
 	 * Returns the EMF {@link TransactionalEditingDomain} used within this
 	 * editor by delegating to the update behavior extension, by default
 	 * {@link DefaultUpdateBehavior#getEditingDomain()}.
 	 * 
 	 * @return the {@link TransactionalEditingDomain} instance used in the
 	 *         editor
 	 * 
 	 * @since 0.9
 	 */
 	public TransactionalEditingDomain getEditingDomain() {
 		return updateBehavior.getEditingDomain();
 	}
 
 	/**
 	 * Executes the given {@link IFeature} with the given {@link IContext} in
 	 * the scope of this {@link DiagramEditor}, meaning within its
 	 * {@link TransactionalEditingDomain} and on its
 	 * {@link org.eclipse.emf.common.command.CommandStack}.
 	 * 
 	 * @param feature
 	 *            the feature to execute
 	 * @param context
 	 *            the context to use
 	 * @return in case of an {@link IAddFeature} being passed as feature the
 	 *         newly added {@link PictogramElement} will be returned (in case
 	 *         the add method returning it), in all other cases
 	 *         <code>null</code>
 	 * 
 	 * @since 0.9
 	 */
 	public Object executeFeature(IFeature feature, IContext context) {
 		Object returnValue = null;
 
 		DefaultEditDomain domain = getEditDomain();
 
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
 		GefCommandWrapper commandWrapper = new GefCommandWrapper(featureCommand, getEditingDomain());
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
 
 	/**
 	 * The EMF {@link ResourceSet} used within this {@link DiagramEditor}. The
 	 * resource set is always associated in a 1:1 releation to the
 	 * {@link TransactionalEditingDomain}.
 	 * 
 	 * @return the resource set used within this editor
 	 * @since 0.9
 	 */
 	public ResourceSet getResourceSet() {
 		ResourceSet ret = null;
 		TransactionalEditingDomain editingDomain = getEditingDomain();
 		if (editingDomain != null) {
 			ret = editingDomain.getResourceSet();
 		}
 		return ret;
 	}
 }
