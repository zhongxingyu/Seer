 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2013 SAP AG.
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
  *    mwenz - Bug 372753 - save shouldn't (necessarily) flush the command stack
  *    mwenz - Bug 376008 - Iterating through navigation history causes exceptions
  *    Felix Velasco - mwenz - Bug 379788 - Memory leak in DefaultMarkerBehavior
  *    mwenz - Bug 387971 - Features cant't be invoked from contextMenu
  *    fvelasco - Bug 323349 - Enable external invocation of features
  *    mwenz - Bug 393113 - Auto-focus does not work for connections
  *    mwenz - Bug 396893 - Enable the registration of the drop target listeners configurable
  *    pjpaulin - Bug 352120 - Main implementation of DiagramEditor - API BREAKAGE HERE
  *    pjpaulin - Bug 352120 - Renamed from DiagramEditorImpl so that classes extending DiagramEditor do not break
  *    mwenz - Bug 394315 - Enable injecting behavior objects in DiagramEditor
 *    pjpaulin - Bug 405314 - Should be able to override DefaultBehavior implementation without configuration
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.gef.ui.parts.GraphicalEditor;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.gef.ui.parts.SelectionSynchronizer;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.navigator.CommonNavigator;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 
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
  * {@link DiagramEditor} instance. This means they are instantiated when a
  * new diagram editor is created and exist until the editor is closed again.
  * There are default implementations for all of these aspects, see the
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
  * @since 0.10
  * 
  */
 public class DiagramEditor extends GraphicalEditorWithFlyoutPalette implements IDiagramContainerUI,
 		ITabbedPropertySheetPageContributor, IEditingDomainProvider {
 
 	private String contributorId;
 	private DiagramBehavior diagramBehavior;
 
 	/**
 	 * The ID of the {@link DiagramEditor} as it is registered with the
 	 * org.eclipse.ui.editors extension point.
 	 */
 	public static final String DIAGRAM_EDITOR_ID = "org.eclipse.graphiti.ui.editor.DiagramEditor"; //$NON-NLS-1$
 
 	/**
 	 * Creates a new diagram editor and cares about the creation of the
 	 * different behavior extensions by delegating to the various
 	 * create*Behavior() methods.
 	 */
 	public DiagramEditor() {
 		super();
 	}
 
 	/**
 	 * Returns the associated {@link DiagramSupport} instance to this editor.
 	 * 
 	 * @return The associated {@link DiagramSupport} instance
 	 * 
 	 * @since 0.10
 	 */
 	public DiagramBehavior getDiagramBehavior() {
 		return diagramBehavior;
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
 		diagramBehavior = createDiagramBehavior();
		diagramBehavior.setParentPart(this);
		diagramBehavior.initDefaultBehaviors();
 
 		// Eclipse may call us with other inputs when a file is to be
 		// opened. Try to convert this to a valid diagram input.
 		if (!(input instanceof IDiagramEditorInput)) {
 			input = convertToDiagramEditorInput(input);
 			if (input == null) {
 				throw new PartInitException(
 						"No DiagramEditorInput instance is available but it is required. The method convertToDiagramEditorInput illegally returned null."); //$NON-NLS-1$
 			}
 		}
 
 		diagramBehavior.getUpdateBehavior().createEditingDomain();
 
 		// The GEF GraphicalEditor init(...) functionality, adapted to provide a
 		// nice error message to the user in case of an error when opening an
 		// editor with e.g. an invalid diagram, see Bug 376008
 		setSite(site);
 		setInput(input);
 		if (diagramBehavior.getEditorInitializationError() != null) {
 			// In case of error simply show an primitive editor with a label
 			return;
 		}
 		getCommandStack().addCommandStackListener(this);
 		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
 		initializeActionRegistry();
 		// ... End of GEF functionality taken over
 
 		diagramBehavior.getUpdateBehavior().init();
 
 		diagramBehavior.migrateDiagramModelIfNecessary();
 		IContextService contextService = (IContextService) getSite().getService(IContextService.class);
 		contextService.activateContext(getDiagramTypeProvider().getContextId());
 	}
 
 	/**
 	 * Creates the behavior object that cares about the common (behavioral)
 	 * coding shared between editors, views and other composites. See
 	 * {@link DiagramBehavior} for details and the default implementation.
 	 * Override to change the behavior.
 	 * 
 	 * @return a new instance of {@link DiagramBehavior}
 	 * @since 0.10
 	 */
 	protected DiagramBehavior createDiagramBehavior() {
		return new DiagramBehavior(this);
 	}
 
 	/**
 	 * Is called by the {@link #init(IEditorSite, IEditorInput)} method in case
 	 * the {@link IEditorInput} instance passed is no {@link DiagramEditorInput}
 	 * . This method should try to convert the passed input object to a
 	 * {@link DiagramEditorInput} or throw an {@link PartInitException} in case
 	 * the conversion can (or should) not be done for any reason. The default
 	 * implementation uses the
 	 * {@link EditorInputAdapter#adaptToDiagramEditorInput(IEditorInput)} method
 	 * to do the conversion. Clients may adapt to do additional conversions or
 	 * to prohibit any conversion by simply throwing a {@link PartInitException}
 	 * .
 	 * 
 	 * @param input
 	 *            the original input
 	 * @return a {@link DiagramEditorInput} corresponding to the passed input
 	 *         instance in case a conversion is possible. This method must not
 	 *         return <code>null</code>, otherwise the editor initialization
 	 *         will fail.
 	 * @throws PartInitException
 	 *             in case the passed input object cannot or should not be
 	 *             converted to a {@link DiagramEditorInput} instance.
 	 * 
 	 * @since 0.9
 	 */
 	protected DiagramEditorInput convertToDiagramEditorInput(IEditorInput input) throws PartInitException {
 		IEditorInput newInput = EditorInputAdapter.adaptToDiagramEditorInput(input);
 		if (!(newInput instanceof IDiagramEditorInput)) {
 			throw new PartInitException("Unknown editor input: " + input); //$NON-NLS-1$
 		}
 		return (DiagramEditorInput) newInput;
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
 		if (!(input instanceof IDiagramEditorInput)) {
 			throw new IllegalArgumentException("The IEditorInput has the wrong type: " + input.getClass()); //$NON-NLS-1$
 		}
 		diagramBehavior.setInput((IDiagramEditorInput) input);
 	}
 
 	/**
 	 * Creates the UI of the editor by delegating to the
 	 * <code>super.createPartControl</code> method. The default implementation
 	 * here also registers the command stack listener to correctly reflect the
 	 * dirty state of the editor.
 	 */
 	public void createPartControl(Composite parent) {
 		if (diagramBehavior.getEditorInitializationError() != null) {
 			diagramBehavior.createErrorPartControl(parent);
 		} else {
 			super.createPartControl(parent);
 			diagramBehavior.addGefListeners();
 		}
 	}
 
 	/**
 	 * Creates the GraphicalViewer on the specified {@link Composite} and
 	 * initializes it. This method needs to be implemented here to fulfill the
 	 * interface of the underlying GEF editor but only delegates to
 	 * {@link DiagramSupport#createGraphicalViewer(Composite)}.
 	 * 
 	 * @param parent
 	 *            The parent composite
 	 */
 	protected void createGraphicalViewer(Composite parent) {
 		diagramBehavior.createGraphicalViewer(parent);
 	}
 
 	/**
 	 * Called to initialize the editor with its content. Here everything is
 	 * done, which is dependent of the IConfigurationProviderInternal.
 	 * 
 	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#initializeGraphicalViewer()
 	 * @since 0.10
 	 */
 	public void initializeGraphicalViewer() {
 
 		super.initializeGraphicalViewer();
 		diagramBehavior.initializeGraphicalViewer();
 
 		// this will cause the ActionBarContributor to refresh with the
 		// new actions (there is no specific refresh-action).
 		if (getEditorSite().getActionBarContributor() != null)
 			getEditorSite().getActionBarContributor().setActiveEditor(this);
 	}
 
 	/**
 	 * Called to configure the editor, before it receives its content. The
 	 * default-implementation is for example doing the following: configure the
 	 * ZoomManager, registering Actions... Here everything is done, which is
 	 * independent of the IConfigurationProviderInternal.
 	 * 
 	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
 	 */
 	protected void configureGraphicalViewer() {
 		super.configureGraphicalViewer();
 		diagramBehavior.configureGraphicalViewer();
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
 	 * delegates via {@link DiagramSupport} to
 	 * {@link DefaultPersistencyBehavior#saveDiagram(IProgressMonitor)}.
 	 * 
 	 * @param monitor
 	 *            the Eclipse progress monitor to report progress with.
 	 */
 	public void doSave(IProgressMonitor monitor) {
 		diagramBehavior.getPersistencyBehavior().saveDiagram(monitor);
 	}
 
 	/**
 	 * Returns if the editor is currently dirty and needs to be saved or not.
 	 * The default implementation delegates to {@link DiagramSupport#isDirty()}.
 	 * 
 	 * @return <code>true</code> in case the editor is dirty, <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isDirty() {
 		return diagramBehavior.isDirty();
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
 		return diagramBehavior.createPaletteViewerProvider();
 	}
 
 	/**
 	 * Delegates to the method (or the method in a subclass of)
 	 * {@link DefaultPaletteBehavior#getPalettePreferences()}. To change the
 	 * palette override the behavior there.
 	 * 
 	 * @return the {@link PaletteViewerProvider} preferences to use.
 	 */
 	protected final FlyoutPreferences getPalettePreferences() {
 		return diagramBehavior.getPalettePreferences();
 	}
 
 	/**
 	 * Returns the {@link PaletteRoot} to use in the GEF editor by delegating to
 	 * {@link DefaultPaletteBehavior#getPaletteRoot()}.
 	 * 
 	 * @return the {@link PaletteRoot} to use
 	 */
 	protected final PaletteRoot getPaletteRoot() {
 		return diagramBehavior.getPaletteRoot();
 	}
 
 	// ---------------------- Refresh --------------------------------------- //
 
 	/**
 	 * Refreshes the editor title to show the name of the diagram
 	 * 
 	 * @since 0.9
 	 */
 	public void refreshTitle() {
 		String name = getDiagramTypeProvider().getDiagramTitle();
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
 
 	// ====================== standard behavior ==============================
 
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
 		Object returnObj = diagramBehavior.getAdapter(type);
 		if (returnObj != null) {
 			return returnObj;
 		}
 		if (type == SelectionSynchronizer.class) {
 			return getSelectionSynchronizer();
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
 		if (diagramBehavior != null) {
 			diagramBehavior.disposeBeforeGefDispose();
 		}
 
 		RuntimeException exc = null;
 		if (getEditDomain() != null) {
 			// Avoid exception in case an error during editor initialization
 			// happened
 			try {
 				super.dispose();
 			} catch (RuntimeException e) {
 				exc = e;
 			}
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
 	public void setFocus() {
 		if (getGraphicalViewer() == null) {
 			return;
 		}
 
 		super.setFocus();
 		diagramBehavior.getUpdateBehavior().handleActivate();
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
 		return diagramBehavior.getSelectedPictogramElements();
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
 					Object selectedPage = ((MultiPageEditorPart) activeEditor).getAdapter(getClass());
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
 			/*
 			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=387971: When
 			 * embedding a diagram editor inside a multi page editor the
 			 * registered actions were not updated. The fix was simply not to
 			 * delegate to super.selectionChange where a check for the editor
 			 * being active only checks for the main editor (GEF editor is not
 			 * embeddable inside another editor) but to trigger the action
 			 * update ourself when our checks say the diagram editor is active.
 			 */
 			updateActions(getSelectionActions());
 		}
 
 	}
 
 	/**
 	 * Selects the given {@link PictogramElement}s in the diagram.
 	 * 
 	 * @param pictogramElements
 	 *            an array of {@link PictogramElement}s to select.
 	 * @since 0.9
 	 */
 	public void selectPictogramElements(PictogramElement[] pictogramElements) {
 		diagramBehavior.selectPictogramElements(pictogramElements);
 	}
 
 	/**
 	 * Sets one {@link PictogramElement} for later selection.
 	 * <p>
 	 * The methods {@link #getPictogramElementsForSelection()},
 	 * {@link #setPictogramElementForSelection(PictogramElement)},
 	 * {@link #setPictogramElementsForSelection(PictogramElement[])} and
 	 * {@link DiagramSupport#selectBufferedPictogramElements()} offer the
 	 * possibility to use a deferred selection mechanism: via the setters,
 	 * {@link PictogramElement}s can be stored for a selection operation that is
 	 * triggered lateron during a general refresh via the method
 	 * {@link DiagramSupport#selectBufferedPictogramElements()}. This mechanism
 	 * is used e.g. in the Graphiti framework in direct editing to restore the
 	 * previous selection, but can also be used by clients.
 	 * 
 	 * @param pictogramElement
 	 *            the {@link PictogramElement} that shall be stored for later
 	 *            selection
 	 * @since 0.9
 	 */
 	public void setPictogramElementForSelection(PictogramElement pictogramElement) {
 		diagramBehavior.setPictogramElementForSelection(pictogramElement);
 	}
 
 	/**
 	 * Sets {@link PictogramElement}s for later selection.
 	 * <p>
 	 * The methods {@link #getPictogramElementsForSelection()},
 	 * {@link #setPictogramElementForSelection(PictogramElement)},
 	 * {@link #setPictogramElementsForSelection(PictogramElement[])} and
 	 * {@link DiagramSupport#selectBufferedPictogramElements()} offer the
 	 * possibility to use a deferred selection mechanism: via the setters,
 	 * {@link PictogramElement}s can be stored for a selection operation that is
 	 * triggered lateron during a general refresh via the method
 	 * {@link DiagramSupport#selectBufferedPictogramElements()}. This mechanism
 	 * is used e.g. in the Graphiti framework in direct editing to restore the
 	 * previous selection, but can also be used by clients.
 	 * 
 	 * @param pictogramElements
 	 *            the {@link PictogramElement}s that shall be stored for later
 	 *            selection
 	 * @since 0.9
 	 */
 	public void setPictogramElementsForSelection(PictogramElement pictogramElements[]) {
 		diagramBehavior.setPictogramElementsForSelection(pictogramElements);
 	}
 
 	// ---------------------- Other ----------------------------------------- //
 
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
 	 * Returns the {@link IDiagramTypeProvider} instance associated with this
 	 * {@link DiagramEditor}. There is always a 1:1 relation between the editor
 	 * and the provider.
 	 * <p>
 	 * Note that this is a pure delegation method. Overrides should happen in
 	 * {@link DiagramBehavior}.
 	 * 
 	 * @return the associated {@link IDiagramTypeProvider} instance.
 	 * 
 	 * @since 0.9
 	 */
 	public IDiagramTypeProvider getDiagramTypeProvider() {
 		return diagramBehavior.getDiagramTypeProvider();
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
 		return diagramBehavior.getEditingDomain();
 	}
 
 	/**
 	 * @since 0.10
 	 */
 	public IDiagramEditorInput getDiagramEditorInput() {
 		return diagramBehavior.getInput();
 	}
 
 	/**
 	 * Returns the {@link IWorkbenchPart} for this container. Since this editor
 	 * itself is already a part the default implementation simply returns this.
 	 * 
 	 * @return This part
 	 * @since 0.10
 	 */
 	public IWorkbenchPart getWorkbenchPart() {
 		return this;
 	}
 
 	/**
 	 * @since 0.10
 	 */
 	public void close() {
 		getSite().getPage().closeEditor(this, false);
 	}
 
 	/* GEF methods that need to be part of the IGEFDiagramContainer interface. */
 
 	/**
 	 * @since 0.10
 	 */
 	public void setEditDomain(DefaultEditDomain editDomain) {
 		super.setEditDomain(editDomain);
 	}
 
 	public ActionRegistry getActionRegistry() {
 		return super.getActionRegistry();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public List getSelectionActions() {
 		return super.getSelectionActions();
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
 }
