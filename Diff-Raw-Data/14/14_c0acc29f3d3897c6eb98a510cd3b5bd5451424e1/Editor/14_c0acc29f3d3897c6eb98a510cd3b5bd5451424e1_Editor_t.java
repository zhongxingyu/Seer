 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.editor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage;
 import org.eclipse.tcf.te.ui.views.extensions.EditorPageBinding;
 import org.eclipse.tcf.te.ui.views.extensions.EditorPageBindingExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.extensions.EditorPageExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.interfaces.IEditorPage;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistable;
 import org.eclipse.ui.IPersistableEditor;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.XMLMemento;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.IFormPage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 
 /**
  * Editor implementation.
  */
 public final class Editor extends FormEditor implements IPersistableEditor, ITabbedPropertySheetPageContributor {
 
 	// The reference to an memento to restore once the editor got activated
 	private IMemento mementoToRestore;
 
 	// The editor event listener instance
 	private EditorEventListener listener;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
 	 */
 	@Override
 	protected void addPages() {
 		// Read extension point and add the contributed pages.
 		IEditorInput input = getEditorInput();
 		// Get all applicable editor page bindings
 		EditorPageBinding[] bindings = EditorPageBindingExtensionPointManager.getInstance().getApplicableEditorPageBindings(input);
 		for (EditorPageBinding binding : bindings) {
 			processPageBinding(binding);
 		}
 
 		if (mementoToRestore != null) {
 			// Loop over all registered pages and pass on the editor specific memento
 			// to the pages which implements IPersistableEditor as well
 			for (Object page : pages) {
 				if (page instanceof IPersistableEditor) {
 					((IPersistableEditor)page).restoreState(mementoToRestore);
 				}
 			}
 			mementoToRestore = null;
 		}
 	}
 
 	/**
 	 * Override this method to delegate the setFocus to
 	 * the active form page.
 	 */
 	@Override
 	public void setFocus() {
 		IFormPage fpage = getActivePageInstance();
 		if(fpage != null) {
 			fpage.setFocus();
 		}
 		else super.setFocus();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormEditor#getActivePageInstance()
 	 */
 	@Override
 	public IFormPage getActivePageInstance() {
 		int index = getActivePage();
 		if (index != -1) {
 			return getPage(index);
 		}
 		return super.getActivePageInstance();
 	}
 
 	/**
 	 * Returns the page which has the specified index.
 	 *
 	 * @param index The page's index.
 	 * @return The page object or null if it does not exists.
 	 */
 	private IFormPage getPage(int index) {
 		for(int i=0;i<pages.size();i++) {
 			Object page = pages.get(i);
 			if (page instanceof IFormPage) {
 				IFormPage fpage = (IFormPage)page;
 				if (fpage.getIndex() == index)
 					return fpage;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Update the editor page list. Pages which are not longer valid
 	 * will be removed and pages now being valid gets added.
 	 */
 	public void updatePageList() {
 		// Get the editor input object
 		IEditorInput input = getEditorInput();
 		// Get all applicable editor page bindings
 		List<EditorPageBinding> bindings = new ArrayList<EditorPageBinding>(Arrays.asList(EditorPageBindingExtensionPointManager.getInstance().getApplicableEditorPageBindings(input)));
 		// Get a copy of the currently added pages
 		List<Object> oldPages = pages != null ? new ArrayList<Object>(Arrays.asList(pages.toArray())) : new ArrayList<Object>();
 		// Loop through the old pages and determine if the page is still applicable
 		Iterator<Object> iterator = oldPages.iterator();
 		while (iterator.hasNext()) {
 			Object element = iterator.next();
 			// Skip over pages not being a form page.
 			if (!(element instanceof IFormPage)) continue;
 			IFormPage page = (IFormPage)element;
 			// Find the corresponding page binding
 			EditorPageBinding binding = null;
 			for (EditorPageBinding candidate : bindings) {
 				if (candidate.getPageId().equals(page.getId())) {
 					binding = candidate;
 					break;
 				}
 			}
 			if (binding != null) {
 				// Found binding -> page is still applicable
 				bindings.remove(binding);
 			} else {
 				// No binding found -> page is not longer applicable
 				removePage(pages.indexOf(page));
 			}
 		}
 		// If the are remaining bindings left, this are new pages.
 		// --> Process them now
 		for (EditorPageBinding binding : bindings) {
 			processPageBinding(binding);
 		}
 	}
 
 	/**
 	 * Process the given editor page binding.
 	 *
 	 * @param binding The editor page binding. Must not be <code>null</code>.
 	 */
 	protected void processPageBinding(EditorPageBinding binding) {
 		Assert.isNotNull(binding);
 
 		String pageId = binding.getPageId();
 		if (pageId != null) {
 			// Get the corresponding editor page instance
 			IEditorPage page = EditorPageExtensionPointManager.getInstance().getEditorPage(pageId, true);
 			if (page != null) {
 				try {
 					// Associate this editor with the page instance.
 					// This is typically done in the constructor, but we are
 					// utilizing a default constructor to instantiate the page.
 					page.initialize(this);
 
 					// Read in the "insertBefore" and "insertAfter" properties of the binding
 					String insertBefore = binding.getInsertBefore().trim();
 					String insertAfter = binding.getInsertAfter().trim();
 
 					boolean pageAdded = false;
 
 					// insertBefore will be processed before insertAfter.
 					if (!"".equals(insertBefore)) { //$NON-NLS-1$
 						String[] pageIds = insertBefore.split(","); //$NON-NLS-1$
 						for (String insertBeforePageId : pageIds) {
 							// If it is "first", we insert the page at index 0
 							if ("first".equalsIgnoreCase(insertBeforePageId)) { //$NON-NLS-1$
								if (getIndexOf(page.getId()) == -1) addPage(0, page);
 								pageAdded = true;
 								break;
 							}
 
 							// Find the index of the page we shall insert this page before
 							int index = getIndexOf(insertBeforePageId);
 							if (index != -1) {
								if (getIndexOf(page.getId()) == -1) addPage(index, page);
 								pageAdded = true;
 								break;
 							}
 						}
 					}
 
 					// If the page hasn't been added till now, process insertAfter
 					if (!pageAdded && !"".equals(insertAfter)) { //$NON-NLS-1$
 						String[] pageIds = insertAfter.split(","); //$NON-NLS-1$
 						for (String insertAfterPageId : pageIds) {
 							// If it is "last", we insert the page at the end
 							if ("last".equalsIgnoreCase(insertAfterPageId)) { //$NON-NLS-1$
								if (getIndexOf(page.getId()) == -1) addPage(page);
 								pageAdded = true;
 								break;
 							}
 
 							// Find the index of the page we shall insert this page after
 							int index = getIndexOf(insertAfterPageId);
 							if (index != -1 && index + 1 < pages.size()) {
								if (getIndexOf(page.getId()) == -1) addPage(index + 1, page);
 								pageAdded = true;
 								break;
 							}
 						}
 					}
 
 					// Add the page to the end if not added otherwise
					if (!pageAdded && getIndexOf(page.getId()) == -1) addPage(page);
 
 				} catch (PartInitException e) { /* ignored on purpose */ }
 			}
 		}
 	}
 
 	/**
 	 * Returns the index of the page with the given id.
 	 *
 	 * @param pageId The page id. Must not be <code>null</code>.
 	 * @return The page index or <code>-1</code> if not found.
 	 */
 	private int getIndexOf(String pageId) {
 		Assert.isNotNull(pageId);
 		for (int i = 0; i < pages.size(); i++) {
 			Object page = pages.get(i);
 			if (page instanceof IFormPage) {
 				IFormPage fpage = (IFormPage)page;
 				if (fpage.getId().equals(pageId))
 					return i;
 			}
 		}
 		return -1;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
 	 */
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
 		super.init(site, input);
 
 		// Update the part name
 		if (!"".equals(input.getName())) setPartName(input.getName()); //$NON-NLS-1$
 
 		// Dispose an existing event listener instance
 		if (listener != null) { listener.dispose(); listener = null; }
 		// Create the event listener. The event listener does register itself.
 		listener = new EditorEventListener(this);
 	}
 
 	/**
 	 * Update the editor part name based on the current editor input.
 	 */
 	public void updatePartName() {
 		IEditorInput input = getEditorInput();
 		String oldPartName = getPartName();
 
 		if (input instanceof EditorInput) {
 			// Reset the editor input name to trigger recalculation
 			((EditorInput)input).name = null;
 			// If the name changed, apply the new name
 			if (!oldPartName.equals(input.getName())) setPartName(input.getName());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// Dispose an existing event listener instance
 		if (listener != null) { listener.dispose(); listener = null; }
 
 	    super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		commitPages(true);
 		// The pages may require some save post processing
 		for (Object page : pages) {
 			if (page instanceof AbstractEditorPage) {
 				((AbstractEditorPage)page).postDoSave(monitor);
 			}
 		}
 		editorDirtyStateChanged();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
 	 */
 	@Override
 	public void doSaveAs() {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
 	 */
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IPersistableEditor#restoreState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void restoreState(IMemento memento) {
 		// Get the editor specific memento
 		mementoToRestore = internalGetMemento(memento);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void saveState(IMemento memento) {
 		// Get the editor specific memento
 		memento = internalGetMemento(memento);
 		// Loop over all registered pages and pass on the editor specific memento
 		// to the pages which implements IPersistable as well
 		for (Object page : pages) {
 			if (page instanceof IPersistable) {
 				((IPersistable)page).saveState(memento);
 			}
 		}
 	}
 
 	/**
 	 * Internal helper method accessing our editor local child memento
 	 * from the given parent memento.
 	 */
 	private IMemento internalGetMemento(IMemento memento) {
 		// Assume the editor memento to be the same as the parent memento
 		IMemento editorMemento = memento;
 
 		// If the parent memento is not null, create a child within the parent
 		if (memento != null) {
 			editorMemento = memento.getChild(Editor.class.getName());
 			if (editorMemento == null) {
 				editorMemento = memento.createChild(Editor.class.getName());
 			}
 		} else {
 			// The parent memento is null. Create a new internal instance
 			// of a XMLMemento. This case is happening if the user switches
 			// to another perspective an the view becomes visible by this switch.
 			editorMemento = XMLMemento.createWriteRoot(Editor.class.getName());
 		}
 
 		return editorMemento;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.part.MultiPageEditorPart#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (adapter == IPropertySheetPage.class) {
 			return new TabbedPropertySheetPage(this);
 		}
 		// We pass on the adapt request to the currently active page
 		Object adapterInstance = getActivePageInstance() != null ? getActivePageInstance().getAdapter(adapter) : null;
 		if (adapterInstance == null) {
 			// If failed to adapt via the currently active page, pass on to the super implementation
 			adapterInstance = super.getAdapter(adapter);
 		}
 		return adapterInstance;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
 	 */
 	@Override
     public String getContributorId() {
 	    return IUIConstants.TABBED_PROPERTIES_CONTRIBUTOR_ID;
     }
 }
