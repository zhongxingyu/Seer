 /*******************************************************************************
  * Copyright (c) 2011, 2012 Alex Bradley.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Alex Bradley    - initial API and implementation
  *******************************************************************************/
 package org.eclipselabs.collage.ui;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.IUndoContext;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.draw2d.Cursors;
 import org.eclipse.gef.SharedImages;
 import org.eclipse.gef.Tool;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CommandStackEventListener;
 import org.eclipse.gef.tools.SelectionTool;
 import org.eclipse.gef.ui.actions.DeleteAction;
 import org.eclipse.gef.ui.actions.UpdateAction;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.TypedListener;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.part.IPage;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.ui.views.properties.PropertySheet;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 import org.eclipselabs.collage.CollageActivator;
 import org.eclipselabs.collage.actions.CollageLayersActionRegistry;
 import org.eclipselabs.collage.actions.SelectAllViewerAction;
 import org.eclipselabs.collage.model.resourceid.ResourceIdentifier;
 import org.eclipselabs.collage.model.resourceid.ResourceIdentifierFactory;
 import org.eclipselabs.collage.operations.WrappingOperation;
 import org.eclipselabs.collage.parts.ShapesEditPartFactory;
 import org.eclipselabs.collage.parts.TextViewerRootEditPart;
 import org.eclipselabs.collage.tools.ToolChangeListener;
 import org.eclipselabs.collage.ui.gef.CollageEditDomain;
 import org.eclipselabs.collage.ui.gef.TransparentGraphicalViewer;
 import org.eclipselabs.collage.util.CollageUtilities;
 import org.eclipselabs.collage.util.CollageExtensions;
 
 /**
  * The Collage user interface that is installed on top of a {@link StyledText} editor.
  * @author Alex Bradley
  */
 public class CollageUI implements CommandStackEventListener, ISelectionChangedListener, IPartListener, ToolChangeListener {
 	/**
 	 * Boolean property indicating whether toolbar has been disposed. Listen for changes to detect disposal.
 	 */
 	public static final String TOOLBAR_DISPOSED_PROPERTY_ID = "org.eclipselabs.collage.collageUI.toolbarDisposed";
 	/**
 	 * Boolean property indicating whether editing of Collage elements is currently enabled. Listen for changes to
 	 * detect enablement/disablement of editing.
 	 */
 	public static final String EDIT_ENABLED_PROPERTY_ID = "org.eclipselabs.collage.collageUI.editEnabled";
 
 	/**
 	 * Side length of a square image used in the toolbar.
 	 */
 	public static final int TOOLBAR_IMAGE_SIZE = 16;
 
 	// Unfortunately, these don't seem to be available as public constants anywhere.
 	private static final String VIEWS_PLUGIN_ID = "org.eclipse.ui.views";
 	private static final String PROPERTIES_VIEW_ID = VIEWS_PLUGIN_ID + ".PropertySheet"; 
 	private static final String PROPERTIES_VIEW_ICON_PATH = "$nl$/icons/full/eview16/prop_ps.gif";
 	
 	/**
 	 * Source ID used when registering actions generated here with {@link EditorActionManager}
 	 */
 	private static final String ACTION_SOURCE_ID = CollageUI.class.getName();
 
 	private static final String[] UNDO_REDO_IDS = { ActionFactory.UNDO.getId(), ActionFactory.REDO.getId() };
 	
 	private static final int[] INTERCEPT_LISTENER_TYPES = { SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick, SWT.KeyDown, SWT.KeyUp };
 	
 	private static final String REGULAR_EDIT_BUTTON_TOOLTIP = "Regular text editing";
 	private static final String SELECT_BUTTON_TOOLTIP = "Select Collage elements";
 	private static final String LAYERS_BUTTON_TOOLTIP = "Collage Layers View";
 	private static final String PROPERTIES_BUTTON_TOOLTIP = "Properties View";
 
 	private final SelectionTool SELECTION_TOOL = new SelectionTool();
 	
 	private List<IAction> selectionActions = new ArrayList<IAction> (3);	
 	private List<IAction> otherActions = new ArrayList<IAction> (3);	
 	private Map<String, IAction> savedPropertiesGlobalActions = new HashMap<String, IAction>();
 	private PropertySheet activePropertySheet = null;
 	private boolean shouldHandlePropertiesUndoRedo = true;
 	
 	private HashMap<Integer, Listener[]> savedListenersMap = new HashMap<Integer, Listener[]>();
 	private ISelectionProvider savedSelectionProvider = null;
 	
 	private Object editingModeChangeLock = new Object();
 	
 	private ITextEditor editor = null;
 	private StyledText textWidget = null;
 	
 	private DeleteAction deleteAction = null;
 	
 	private ToolBar toolbar = null;
 	
 	private TransparentGraphicalViewer viewer;
 	private CollageEditDomain editDomain;
 	private TextViewerRootEditPart rootEditPart;
 
 	private Map<String, Object> properties = new HashMap<String, Object>();
 	private Object propertiesLock = new Object();
 	private PropertyChangeSupport pcsDelegate = new PropertyChangeSupport(this);
 	
 	private Map<Class<? extends Tool>, ToolItem> registeredTools = new HashMap<Class<? extends Tool>, ToolItem>();
 	private Object registeredToolsLock = new Object();
 	
 	public CollageUI () {
 		// Default values
 		setProperty(TOOLBAR_DISPOSED_PROPERTY_ID, false);
 		setProperty(EDIT_ENABLED_PROPERTY_ID, false);
 		
 		SELECTION_TOOL.setDefaultCursor(Cursors.ARROW);
 	}
 	
 	public void install (ITextEditor editor, ITextViewer textViewer) {
 		ResourceIdentifier resourceId = ResourceIdentifierFactory.getResourceIdentifier(editor.getEditorInput());
 		if (resourceId == null) {
 			// Couldn't get resource, so we won't install the Collage UI.
 			return;
 		}
 		
 		this.editor = editor;
 		textWidget = textViewer.getTextWidget();
 		
 		if (!textWidget.isDisposed()) {
 			CollageUIRegistry.getDefault().addCollageUI(this);
 			
 			rootEditPart = new TextViewerRootEditPart(this, textViewer, resourceId);
 			editDomain = new CollageEditDomain();
 			editDomain.addToolChangeListener(this);
 			editDomain.setDefaultTool(SELECTION_TOOL);
 			editDomain.getCommandStack().addCommandStackEventListener(this);
 			editDomain.getCommandStack().addCommandStackEventListener(CollageLayersActionRegistry.getDefault());
 			
 			createTopMenu();
 			
 			viewer = new TransparentGraphicalViewer(this);
 			viewer.setControl(textWidget);
 			viewer.setEditPartFactory(new ShapesEditPartFactory());
 			viewer.setRootEditPart(rootEditPart);
 
 			viewer.setContents(CollageActivator.getDefault().getDefaultCollageRoot());
 			
 			editDomain.addViewer(viewer);
 			editDomain.setActiveTool(null);
 			
 			getTopParent().layout();
 		}
 	}
 	
 	public void uninstall() {
 		if (toolbar != null)
 			toolbar.dispose();
 		
 		setProperty(TOOLBAR_DISPOSED_PROPERTY_ID, true);
 
 		CollageUIRegistry.getDefault().removeCollageUI(this);
 
 		if (viewer != null) {
 			Control viewerControl = viewer.getControl();
 			
 			if (editDomain != null && viewerControl != null && !viewerControl.isDisposed())
 				editDomain.removeViewer(viewer);
 			
 			viewer.setControl(null);
 			
 			if (textWidget != null && !textWidget.isDisposed()) {
 				setEditingMode(false);
 				
 				getTopParent().layout();
 				textWidget.redraw();
 			}
 		}
 	}
 	
 	public Object getProperty (String key) {
 		synchronized (propertiesLock) {
 			return properties.get(key);
 		}
 	}
 	
 	public void setProperty (String key, Object value) {
 		synchronized (propertiesLock) {
 			Object oldValue = properties.put(key, value);
 			pcsDelegate.firePropertyChange(key, oldValue, value);
 		}
 	}
 	
 	public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
 		pcsDelegate.addPropertyChangeListener(propertyName, listener);
 	}
 	
 	public void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
 		pcsDelegate.removePropertyChangeListener(propertyName, listener);
 	}
 	
 	public ITextEditor getEditor () {
 		return editor;
 	}
 	
 	public IAction getDeleteAction () {
 		return deleteAction;
 	}
 	
 	/**
 	 * @return True if editing of the Collage layers is currently enabled, false otherwise.
 	 */
 	public boolean editingEnabled () {
 		return (Boolean)getProperty(EDIT_ENABLED_PROPERTY_ID);
 	}
 
 	public void setActiveTool (Tool tool) {
 		setEditingMode(true);
 		editDomain.setActiveTool(tool);
 	}
 	
 	public void registerTool (Class<? extends Tool> toolClass, ToolItem button) {
 		synchronized (registeredToolsLock) {
 			registeredTools.put(toolClass, button);
 		}
 	}
 	
 	@Override
 	public void stackChanged(CommandStackEvent event) {
 		switch (event.getDetail()) {
 		case CommandStack.POST_EXECUTE:
 			CommandStack commandStack = editDomain.getCommandStack();
 			Command undoCommand = commandStack.getUndoCommand();
 			if (undoCommand != null) {
 				IUndoContext context = CollageUtilities.getTextViewerUndoContext(rootEditPart.getTextViewer());
 				if (context != null) {
 					IUndoableOperation op = new WrappingOperation(undoCommand.getLabel(), context, commandStack);						
 					OperationHistoryFactory.getOperationHistory().add(op);
 				}
 			}
 			break;
 		case CommandStack.POST_UNDO:
 		case CommandStack.POST_REDO:
 			if (activePropertySheet != null) {
 				IPage page = activePropertySheet.getCurrentPage();
 				if (page instanceof PropertySheetPage) {
 					((PropertySheetPage)page).refresh();
 				}
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void selectionChanged(SelectionChangedEvent event) {
 		updateActions(selectionActions);		
 	}
 
 	public void updateUndoRedo () { 
 		IUndoContext context = CollageUtilities.getTextViewerUndoContext(rootEditPart.getTextViewer());
 		IOperationHistory history = OperationHistoryFactory.getOperationHistory();
 		updateIfWrappingOperation(history.getRedoOperation(context), history);
 		updateIfWrappingOperation(history.getUndoOperation(context), history);		
 	}
 	
 	@Override
 	public void partActivated(IWorkbenchPart part) {
 		if (part instanceof PropertySheet) {
 			activePropertySheet = (PropertySheet)part;
 			if (shouldHandlePropertiesUndoRedo) {
 				installPropertySheetActions();
 			}
 		} else if (part == getEditor()) {
 			shouldHandlePropertiesUndoRedo = true;
 			installPropertySheetActions();
 		} else {
 			shouldHandlePropertiesUndoRedo = false;
 			uninstallPropertySheetActions();
 			activePropertySheet = null;
 		}
 	}
 
 	@Override public void partBroughtToTop(IWorkbenchPart part) { }
 	@Override public void partClosed(IWorkbenchPart part) { }
 	@Override public void partDeactivated(IWorkbenchPart part) { }
 	@Override public void partOpened(IWorkbenchPart part) { }
 
 	@Override
 	public void toolChange(Tool oldTool, Tool newTool) {
 		selectToolItem((newTool != null) ? newTool.getClass() : null);
 	}
 
 	private void createTopMenu () {
 		Composite topParent = getTopParent();
 		GridLayout topLayout = new GridLayout(1, false);
 		topLayout.marginHeight = 0;
 		topLayout.marginWidth = 0;
 		topLayout.verticalSpacing = 0;
 		topLayout.horizontalSpacing = 0;
 		topParent.setLayout(topLayout);
 
 		GridData editorLData = new GridData();
 		editorLData.grabExcessHorizontalSpace = true;
 		editorLData.grabExcessVerticalSpace = true;
 		editorLData.horizontalAlignment = SWT.FILL;
 		editorLData.verticalAlignment = SWT.FILL;
 		textWidget.getParent().setLayoutData(editorLData);
 		
 		toolbar = new ToolBar(topParent, SWT.HORIZONTAL);
 		populateToolBar(toolbar);
 		GridData toolbarLayoutData = new GridData();
 		toolbarLayoutData.grabExcessHorizontalSpace = true;
 		toolbarLayoutData.horizontalAlignment = SWT.FILL;
 		toolbar.setLayoutData(toolbarLayoutData);
 		
 		toolbar.moveAbove(textWidget.getParent());
 	}
 		
 	private void populateToolBar (final ToolBar toolBar) {
 		final ToolItem regularEditingButton = new ToolItem(toolBar, SWT.RADIO);
 		regularEditingButton.setImage(CollageActivator.getImage(CollageActivator.IBEAM_ICON));
 		regularEditingButton.setToolTipText(REGULAR_EDIT_BUTTON_TOOLTIP);
 		regularEditingButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				setEditingMode(!regularEditingButton.getSelection());
 			}
 		});
 
 		final ToolItem selectButton = new ToolItem(toolBar, SWT.RADIO);
 		selectButton.setImage(CollageActivator.getImage(SharedImages.DESC_SELECTION_TOOL_16));
 		selectButton.setToolTipText(SELECT_BUTTON_TOOLTIP);
 		registerTool(SELECTION_TOOL.getClass(), selectButton);
 		selectButton.addSelectionListener(new SelectionAdapter () {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (selectButton.getSelection()) {
 					setActiveTool(SELECTION_TOOL);
 				}
 			}			
 		});
 				
 		CollageExtensions.setupExtensionTools(toolBar, this);
 		
 		new ToolItem(toolBar, SWT.SEPARATOR);
 		
 		CollageExtensions.setupExtensionToolbarWidgets(toolBar, this);
 
 		// SWT.SEPARATOR_FILL doesn't actually work across platforms (https://bugs.eclipse.org/bugs/show_bug.cgi?id=350044),
 		// so we have to use this kludgy workaround to get the desired behaviour.
 		final ToolItem filler = new ToolItem(toolBar, SWT.SEPARATOR);
 		// Don't draw a separator
 		Label label = new Label(toolBar, SWT.NONE);
 		filler.setControl(label);
 		label.setVisible(false); // gives transparency
 		
 		toolBar.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlResized(ControlEvent e) {
 				if (!toolBar.isDisposed()) {
 					ToolItem[] items = toolBar.getItems();
 					if (items.length > 2) {
 						int rightHandButtonsWidth = items[items.length - 2].getBounds().width +
 								items[items.length - 1].getBounds().width; 
 						int left = filler.getBounds().x;
 						int right = toolBar.getSize().x - rightHandButtonsWidth;
 						filler.setWidth(right - left - 2);
 					}
 				}
 			}
 		});
 
 		makeViewOpenButton(toolBar, CollageActivator.getImage(VIEWS_PLUGIN_ID, PROPERTIES_VIEW_ICON_PATH), PROPERTIES_BUTTON_TOOLTIP,
 				PROPERTIES_VIEW_ID, "properties");
 		makeViewOpenButton(toolBar, CollageActivator.getImage(CollageActivator.LAYERS_VIEW_ICON), LAYERS_BUTTON_TOOLTIP,
 				CollageLayersTreeView.VIEW_ID, "Collage Layers");
 		
 		// Default is regular text editor behaviour.
 		regularEditingButton.setSelection(true);
 		
 		toolBar.pack();
 	}
 	
 	private void makeViewOpenButton (ToolBar toolBar, Image icon, String tooltip, final String viewID, final String viewDescription) {
 		ToolItem viewOpenButton = new ToolItem(toolBar, SWT.PUSH);
 		viewOpenButton.setImage(icon);
 		viewOpenButton.setToolTipText(tooltip);
 		viewOpenButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					if (editor != null) {
 						editor.getSite().getPage().showView(viewID);
 					}
 				} catch (PartInitException e1) {
 					CollageUtilities.showError(CollageActivator.PLUGIN_NAME, String.format("Opening %s view failed: %s", 
 							viewDescription, e1.getMessage()));
 				}
 			}
 		});
 	}
 	
 	private Composite getTopParent () {
 		Assert.isNotNull(textWidget);
 		return textWidget.getParent().getParent();
 	}
 	
 	private void installAllEditorActions () {
 		List<IAction> allActions = new ArrayList<IAction> ();
 		allActions.addAll(selectionActions);
 		allActions.addAll(otherActions);
 		EditorActionManager.getDefault().installGlobalActions(ACTION_SOURCE_ID, allActions);
 		EditorActionManager.getDefault().installEditorActions(ACTION_SOURCE_ID, allActions);
 	}
 	
 	private void uninstallAllActions () {
 		uninstallPropertySheetActions();
 		
 		EditorActionManager.getDefault().uninstallAllActions();
 		selectionActions.clear();
 		otherActions.clear();
 	}
 	
 	private void selectToolItem (Class<? extends Tool> toolClass) {
 		synchronized (registeredToolsLock) {
 			for (Entry<Class<? extends Tool>, ToolItem> entry : registeredTools.entrySet()) {
 				ToolItem button = entry.getValue();
 				if (entry.getKey().equals(toolClass)) {
 					if (!button.getSelection()) {
 						button.setSelection(true);
 					}
 				} else {
 					if (button.getSelection()) {
 						button.setSelection(false);
 					}
 				}
 			}
 		}		
 	}
 	
 	private void interceptListeners () {
 		for (int listenerType : INTERCEPT_LISTENER_TYPES) {
 			Listener[] savedListeners = textWidget.getListeners(listenerType);
 			for (Listener listener : savedListeners) {
 				// TODO: TypedListener is not supposed to be accessed outside SWT. Figure out a different way to
 				// differentiate the Draw2D listeners from the rest.
 				if (listener instanceof TypedListener &&
 						((TypedListener)listener).getEventListener().getClass().getCanonicalName().startsWith("org.eclipse.draw2d")) {
 					continue;
 				}
 				textWidget.removeListener(listenerType, listener);
 			}
 			savedListenersMap.put(listenerType, savedListeners);
 		}
 	}
 	
 	private void stopInterceptingListeners () {
 		for (Entry<Integer, Listener[]> entry : savedListenersMap.entrySet()) {
 			// Restore listeners
 			for (Listener listener : textWidget.getListeners(entry.getKey())) {
 				textWidget.removeListener(entry.getKey(), listener);
 			}
 
 			for (Listener listener : entry.getValue()) {
 				textWidget.addListener(entry.getKey(), listener);
 			}
 		}
 	}
 	
 	private void setEditingMode (boolean enabled) {
 		synchronized (editingModeChangeLock) {
 			boolean currentlyEnabled = editingEnabled();
 			if (!currentlyEnabled && enabled) {
				setProperty(EDIT_ENABLED_PROPERTY_ID, enabled);
				
 				textWidget.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
 
 				// This disables all underlying editor actions. Not using for now as it would seem useful
 				// to keep some things active (e.g., find). Also, some (Java-specific) actions weren't getting
 				// re-enabled when we used this.
 				// getEditor().getEditorSite().getActionBarContributor().setActiveEditor(null);
 				
 				interceptListeners();
 
 				savedSelectionProvider = getEditor().getSite().getSelectionProvider();
 				getEditor().getSite().setSelectionProvider(viewer);
 				
 				// See GraphicalEditor#createActions()
 				
 				deleteAction = new DeleteAction ((IWorkbenchPart)getEditor()) {
 					@Override
 					protected CommandStack getCommandStack() {
 						return editDomain.getCommandStack();
 					}
 				};
 				deleteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
 				deleteAction.setSelectionProvider(viewer);
 				selectionActions.add(deleteAction);
 
 				SelectAllViewerAction selectAllAction = new SelectAllViewerAction(viewer);
 				selectAllAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
 				otherActions.add(selectAllAction);
 				
 				installAllEditorActions();
 				
 				viewer.addSelectionChangedListener(this);
 
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(this);
 				
 				reactivateEditorPartForProperties();
 			} else if (currentlyEnabled && !enabled) {
 				// Must fire this first - edit parts may need to listen and do cleanup
 				setProperty(EDIT_ENABLED_PROPERTY_ID, enabled);
 				
 				textWidget.setCursor(null);
 				
 				stopInterceptingListeners();
 				
 				this.getEditor().getSite().setSelectionProvider(savedSelectionProvider);
 				
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(this);
 
 				viewer.deselectAll();
 				viewer.removeSelectionChangedListener(this);
 				editDomain.setActiveTool(null);
 
 				uninstallAllActions();
 
 				reactivateEditorPartForProperties();
 
 				// This re-enables all editor actions (see above)
 				// getEditor().getEditorSite().getActionBarContributor().setActiveEditor(getEditor());
 			}
 		}
 	}
 
 	/**
 	 * This is slightly kludgy.
 	 * 
 	 * If the properties view is present, reactivate the editor view so the properties view will pick up our
 	 * new selection provider (or a restored selection provider from elsewhere.) Reactivation is done by switching 
 	 * activation to the properties view and back to the editor. (We can't just call 
 	 * {@link IWorkbenchPage#activate(IWorkbenchPart)} on the editor because it won't reactivate the currently 
 	 * activated part.)
 	 */
 	private void reactivateEditorPartForProperties() {
 		for (IWorkbenchPage page : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()) {
 			for (IViewReference ref : page.getViewReferences()) {
 				if (PROPERTIES_VIEW_ID.equals(ref.getId())) {
 					page.activate(ref.getPart(false));
 					page.activate(getEditor());
 					return;
 				}
 			}
 		}
 	}
 
 	private static void updateActions (List<IAction> actions) {
 		for (IAction action : actions) {
 			if (action instanceof UpdateAction) {
 				((UpdateAction)action).update();
 			}
 		}		
 	}
 	
 	private static void updateIfWrappingOperation (IUndoableOperation operation, IOperationHistory history) {
 		if (operation instanceof WrappingOperation) {
 			history.operationChanged(operation);
 		}
 	}
 
 	private void installPropertySheetActions () {
 		if (activePropertySheet != null) {
 			IActionBars actionBars = activePropertySheet.getViewSite().getActionBars();
 			boolean changed = false;
 			for (String actionId : UNDO_REDO_IDS) {
 				IAction action = getEditor().getAction(actionId);
				if (actionBars.getGlobalActionHandler(actionId) != action) {
					savedPropertiesGlobalActions.put(actionId, actionBars.getGlobalActionHandler(actionId));
					actionBars.setGlobalActionHandler(actionId, action);
 					changed = true;
 				}
 			}
 			if (changed)
 				actionBars.updateActionBars();
 		}
 	}
 	
 	private void uninstallPropertySheetActions() {
 		if (activePropertySheet != null) {
 			IActionBars actionBars = activePropertySheet.getViewSite().getActionBars();
 			boolean changed = false;
 			for (String actionId : UNDO_REDO_IDS) {
 				IAction action = getEditor().getAction(actionId);
				if (actionBars.getGlobalActionHandler(actionId) == action) {
					actionBars.setGlobalActionHandler(actionId, savedPropertiesGlobalActions.get(actionId));
 					changed = true;
 				}
 			}
 			if (changed)
 				actionBars.updateActionBars();
 		}
 	}
 }
