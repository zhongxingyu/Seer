 /*******************************************************************************
  * Copyright (c) 2007-2011 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.ltk.ui.sourceediting;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.IPostSelectionProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.editors.text.IEncodingSupport;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.part.IShowInSource;
 import org.eclipse.ui.part.IShowInTarget;
 import org.eclipse.ui.part.IShowInTargetList;
 import org.eclipse.ui.part.ShowInContext;
 import org.eclipse.ui.services.IServiceLocator;
 import org.eclipse.ui.texteditor.ITextEditorActionConstants;
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 import de.walware.ecommons.ui.SharedUIResources;
 import de.walware.ecommons.ui.actions.HandlerCollection;
 import de.walware.ecommons.ui.util.UIAccess;
 import de.walware.ecommons.ui.workbench.AbstractEditorOutlinePage;
 
 import de.walware.ecommons.ltk.AstInfo;
 import de.walware.ecommons.ltk.IModelElement;
 import de.walware.ecommons.ltk.IModelElement.Filter;
 import de.walware.ecommons.ltk.IModelElementDelta;
 import de.walware.ecommons.ltk.ISourceStructElement;
 import de.walware.ecommons.ltk.ISourceUnit;
 import de.walware.ecommons.ltk.ISourceUnitModelInfo;
 import de.walware.ecommons.ltk.internal.ui.EditingMessages;
 import de.walware.ecommons.ltk.ui.IModelElementInputListener;
 import de.walware.ecommons.ltk.ui.ISelectionWithElementInfoListener;
 import de.walware.ecommons.ltk.ui.LTKInputData;
 
 
 /**
  * Abstract content outline page for a {@link SourceEditor1} with model info.
  */
 public abstract class SourceEditor1OutlinePage extends AbstractEditorOutlinePage
 		implements IContentOutlinePage, IAdaptable, ISourceEditorAssociated,
 			IShowInSource, IShowInTargetList, IShowInTarget,
 			IPostSelectionProvider, IModelElementInputListener {
 	
 	
 	protected class OutlineContentProvider implements ITreeContentProvider {
 		
 		public OutlineContentProvider() {
 		}
 		
 		public long getStamp(final Object inputElement) {
 			if (inputElement instanceof ISourceUnit) {
 				final ISourceUnitModelInfo info = ((ISourceUnit) inputElement).getModelInfo(fMainType, 0, null); 
 				if (info != null) {
 					return info.getStamp();
 				}
 			}
 			return ISourceUnit.UNKNOWN_MODIFICATION_STAMP;
 		}
 		
 		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
 		}
 		
 		public Object[] getElements(final Object inputElement) {
 			if (inputElement instanceof ISourceUnit) {
 				final ISourceUnitModelInfo info = ((ISourceUnit) inputElement).getModelInfo(fMainType, 0, null); 
 				if (info != null) {
 					fCurrentModelStamp = info.getStamp();
 					final List<? extends ISourceStructElement> children = info.getSourceElement().getSourceChildren(getContentFilter());
 					return children.toArray(new ISourceStructElement[children.size()]);
 				}
 			}
 			return new Object[0];
 		}
 		
 		public void dispose() {
 		}
 		
 		public Object getParent(final Object element) {
 			final ISourceStructElement o = (ISourceStructElement) element;
 			return o.getSourceParent();
 		}
 		
 		public boolean hasChildren(final Object element) {
 			final ISourceStructElement o = (ISourceStructElement) element;
 			return o.hasSourceChildren(getContentFilter());
 		}
 		
 		public Object[] getChildren(final Object parentElement) {
 			final ISourceStructElement o = (ISourceStructElement) parentElement;
 			final List<? extends ISourceStructElement> children = o.getSourceChildren(getContentFilter());
 			return children.toArray(new ISourceStructElement[children.size()]);
 		}
 	}
 	
 	public class AstContentProvider extends OutlineContentProvider {
 		
 		
 		public AstContentProvider() {
 			super();
 		}
 		
 		@Override
 		public long getStamp(final Object inputElement) {
 			if (inputElement instanceof ISourceUnit) {
 				final AstInfo info = ((ISourceUnit) inputElement).getAstInfo(fMainType, false, null); 
 				if (info != null) {
 					return info.stamp;
 				}
 			}
 			return ISourceUnit.UNKNOWN_MODIFICATION_STAMP;
 		}
 		
 		@Override
 		public Object[] getElements(final Object inputElement) {
 			if (inputElement instanceof ISourceUnit) {
 				final AstInfo info = ((ISourceUnit) inputElement).getAstInfo(fMainType, false, null); 
 				if (info != null) {
 					fCurrentModelStamp = info.stamp;
 					return new Object[] { info.root };
 				}
 			}
 			return new Object[0];
 		}
 		
 	}
 	
 	
 	/**
 	 * @deprecated use {@link AbstractToggleHandler}
 	 */
 	@Deprecated
 	protected abstract class ToggleAction extends Action {
 		
 		private final String fSettingsKey;
 		private final int fTime;
 		
 		public ToggleAction(final String checkSettingsKey, final boolean checkSettingsDefault, 
 				final int expensive) {
 			assert (checkSettingsKey != null);
 			
 			fSettingsKey = checkSettingsKey;
 			fTime = expensive;
 			
 			final IDialogSettings settings = getDialogSettings();
 			final boolean on = (settings.get(fSettingsKey) == null) ?
 					checkSettingsDefault : getDialogSettings().getBoolean(fSettingsKey);
 			setChecked(on);
 			configure(on);
 		}
 		
 		protected void init() {
 		}
 		
 		@Override
 		public void run() {
 			final Runnable runnable = new Runnable() {
 				public void run() {
 					final boolean on = isChecked();
 					configure(on);
 					getDialogSettings().put(fSettingsKey, on); 
 				}
 			};
 			if (fTime == 0) {
 				runnable.run();
 			}
 			else {
 				BusyIndicator.showWhile(Display.getCurrent(), runnable);
 			}
 		}
 		
 		protected abstract void configure(boolean on);
 		
 	}
 	
 	private class SyncWithEditorAction extends ToggleAction implements ISelectionWithElementInfoListener {
 		
 		public SyncWithEditorAction() {
 			super("sync.editor", true, 0); //$NON-NLS-1$
 			setText(EditingMessages.SyncWithEditor_label);
 			setImageDescriptor(SharedUIResources.getImages().getDescriptor(SharedUIResources.LOCTOOL_SYNCHRONIZED_IMAGE_ID));
 		}
 		
 		@Override
 		protected void configure(final boolean on) {
 			if (on) {
 				fEditor.addPostSelectionWithElementInfoListener(this);
 			}
 			else {
 				fEditor.removePostSelectionWithElementInfoListener(this);
 			}
 		}
 		
 		public void inputChanged() {
 		}
 		
 		public void stateChanged(final LTKInputData state) {
 			if (!state.isStillValid()) {
 				return;
 			}
 			if (fCurrentModelStamp != state.getInputInfo().getStamp()) {
 				elementUpdatedInfo(state.getInputElement(), null);
 			}
 			UIAccess.getDisplay().syncExec(new Runnable() {
 				public void run() {
 					if (state.isStillValid() && isChecked()) {
 						select(state.getModelSelection());
 					}
 				}
 			});
 		}
 		
 	}
 	
 	
 	private final SourceEditor1 fEditor;
 	private final String fMainType;
 	private OutlineContentProvider fContentProvider;
 	
 	private long fCurrentModelStamp;
 	
 	private IModelElement fInputUnit;
 	
 	private SyncWithEditorAction fSyncWithEditorAction;
 	
 	
 	public SourceEditor1OutlinePage(final SourceEditor1 editor, final String mainType, final String contextMenuId) {
 		super(contextMenuId);
 		if (editor == null) {
 			throw new NullPointerException();
 		}
 		if (mainType == null) {
 			throw new NullPointerException();
 		}
 		fEditor = editor;
 		fMainType = mainType;
 	}
 	
 	
 	@Override
 	public void init(final IPageSite pageSite) {
 		super.init(pageSite);
 		pageSite.setSelectionProvider(this);
 	}
 	
	protected IModelElement.Filter<ISourceStructElement> getContentFilter() {
 		return null;
 	}
 	
 	@Override
 	public void createControl(final Composite parent) {
 		super.createControl(parent);
 		
 		fEditor.getModelInputProvider().addListener(this);
 		getViewer().setInput(fInputUnit);
 	}
 	
 	protected OutlineContentProvider createContentProvider() {
 		return new OutlineContentProvider();
 	}
 	
 	@Override
 	protected void configureViewer(final TreeViewer viewer) {
 		fContentProvider = createContentProvider();
 		viewer.setContentProvider(fContentProvider);
 	}
 	
 	
 	@Override
 	protected void initActions(final IServiceLocator serviceLocator, final HandlerCollection handlers) {
 		super.initActions(serviceLocator, handlers);
 		
 		fSyncWithEditorAction = new SyncWithEditorAction();
 	}
 	
 	@Override
 	protected void contributeToActionBars(final IServiceLocator serviceLocator,
 			final IActionBars actionBars, final HandlerCollection handlers) {
 		super.contributeToActionBars(serviceLocator, actionBars, handlers);
 		
 		actionBars.setGlobalActionHandler(ITextEditorActionConstants.UNDO, fEditor.getAction(ITextEditorActionConstants.UNDO));
 		actionBars.setGlobalActionHandler(ITextEditorActionConstants.REDO, fEditor.getAction(ITextEditorActionConstants.REDO));
 		
 //		actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, fEditor.getAction(ITextEditorActionConstants.NEXT));
 		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fEditor.getAction(ITextEditorActionConstants.NEXT));
 //		actionBars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, fEditor.getAction(ITextEditorActionConstants.PREVIOUS));
 		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fEditor.getAction(ITextEditorActionConstants.PREVIOUS));
 		
 		final IMenuManager menuManager = actionBars.getMenuManager();
 		
 		menuManager.add(fSyncWithEditorAction);
 	}
 	
 	@Override
 	protected void contextMenuAboutToShow(final IMenuManager m) {
 	}
 	
 	
 	public void elementChanged(final IModelElement element) {
 		fInputUnit = element;
 		fCurrentModelStamp = ISourceUnit.UNKNOWN_MODIFICATION_STAMP;
 		final TreeViewer viewer = getViewer();
 		if (UIAccess.isOkToUse(viewer)) {
 			viewer.setInput(fInputUnit);
 		}
 	}
 	
 	public void elementInitialInfo(final IModelElement element) {
 		elementUpdatedInfo(element, null);
 	}
 	
 	public void elementUpdatedInfo(final IModelElement element, final IModelElementDelta delta) {
 		if (element != fInputUnit || (element == null && fInputUnit == null)) {
 			return;
 		}
 		final Display display = UIAccess.getDisplay();
 		display.syncExec(new Runnable() {
 			public void run() {
 				final TreeViewer viewer = getViewer();
 				
 				if (element != fInputUnit 
 						|| !UIAccess.isOkToUse(viewer)
 						|| (fCurrentModelStamp != ISourceUnit.UNKNOWN_MODIFICATION_STAMP && fContentProvider.getStamp(element) == fCurrentModelStamp)) {
 					return;
 				}
 				beginIgnoreSelection();
 				try {
 					viewer.refresh(true);
 				}
 				finally {
 					endIgnoreSelection(false);
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void dispose() {
 		fEditor.getModelInputProvider().removeListener(this);
 		fEditor.handleOutlinePageClosed();
 		
 		super.dispose();
 	}
 	
 	
 	@Override
 	protected void selectInEditor(final ISelection selection) {
 		fEditor.setSelection(selection, fSyncWithEditorAction);
 	}
 	
 	protected void select(ISourceStructElement element) {
 		final TreeViewer viewer = getViewer();
 		if (UIAccess.isOkToUse(viewer)) {
 			beginIgnoreSelection();
 			try {
 				final Filter filter = getContentFilter();
 				Object selectedElement = null;
 				final IStructuredSelection currentSelection = ((IStructuredSelection) viewer.getSelection());
 				if (currentSelection.size() == 1) {
 					selectedElement = currentSelection.getFirstElement();
 				}
 				while (element != null 
 						&& (element.getElementType() & IModelElement.MASK_C2) != IModelElement.C2_SOURCE_FILE) {
 					if (selectedElement != null && element.equals(selectedElement)) {
 						return;
 					}
 					if (filter == null || filter.include(element)) {
 						selectedElement = null;
 						viewer.setSelection(new StructuredSelection(element), true);
 						if (!viewer.getSelection().isEmpty()) {
 							return;
 						}
 					}
 					final IModelElement parent = element.getSourceParent();
 					if (parent instanceof ISourceStructElement) {
 						element = (ISourceStructElement) parent;
 						continue;
 					}
 					else {
 						break;
 					}
 				}
 				if (!viewer.getSelection().isEmpty()) {
 					viewer.setSelection(StructuredSelection.EMPTY);
 				}
 			}
 			finally {
 				endIgnoreSelection(true);
 			}
 		}
 	}
 	
 	
 	public ShowInContext getShowInContext() {
 		return new ShowInContext(fEditor.getEditorInput(), null);
 	}
 	
 	public String[] getShowInTargetIds() {
 		return new String[] { IPageLayout.ID_PROJECT_EXPLORER };
 	}
 	
 	public boolean show(final ShowInContext context) {
 		final IModelElement inputUnit = fInputUnit;
 		final ISelection selection = context.getSelection();
 		if (selection instanceof LTKInputData) {
 			final LTKInputData data = (LTKInputData) selection;
 			data.update();
 			if (inputUnit.equals(data.getInputElement())) {
 				select(data.getModelSelection());
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	public ISourceEditor getSourceEditor() {
 		return fEditor;
 	}
 	
 	@Override
 	public Object getAdapter(final Class required) {
 		if (ISourceEditorAssociated.class.equals(required)) {
 			return this;
 		}
 		if (IEncodingSupport.class.equals(required)) {
 			return fEditor.getAdapter(IEncodingSupport.class);
 		}
 		return null;
 	}
 	
 }
