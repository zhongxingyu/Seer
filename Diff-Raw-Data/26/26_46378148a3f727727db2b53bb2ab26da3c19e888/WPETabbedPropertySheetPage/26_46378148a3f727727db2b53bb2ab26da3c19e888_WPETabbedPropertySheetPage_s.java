 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.properties;
 
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.editors.HTMLEditor;
 import org.eclipse.jst.pagedesigner.properties.internal.QuickEditTabManager;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.w3c.dom.Node;
 
 /**
  * @author mengbo
  */
 public class WPETabbedPropertySheetPage extends TabbedPropertySheetPage {
 
 	// TODO: when we want to extend this page, HTMLEditor would not be the sole
 	// type of editor part.
 	private HTMLEditor _htmlEditor;
 
 	private NavigationHiearchyAction _hiearchAction = new NavigationHiearchyAction(
 			this);
 	
 	private QuickEditTabManager manager;
 
 	private ISelectionListener _selListener;
 
 	/**
 	 * Constructor
 	 * @param tabbedPropertySheetPageContributor
 	 * @param editor 
 	 */
 	public WPETabbedPropertySheetPage(
 			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor,
 			HTMLEditor editor) {
 		super(tabbedPropertySheetPageContributor);
 		_htmlEditor = editor;
 	}
 
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		if (part == null) {
 			part = _htmlEditor;
 		}
 		if (part instanceof HTMLEditor || part instanceof ContentOutline) {
 			Node node = DesignerPropertyTool.normalizeSelectionToElement(part,
 					selection, _htmlEditor);
 			if (node != null) {
 				try {
 					_hiearchAction.refresh(node, node);
 					this.getSite().getActionBars().getToolBarManager().update(
 							true);
 					//setInput(part, node);
 					super.selectionChanged(part, new StructuredSelection(node));
 				} catch (Exception e) {
 					// Some synchronization would cause this, it does not damage
 					// the data.
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This method should be called from internal of the property page. Normally
 	 * means user did some action inside the property sheet to change current
 	 * selection.
 	 * 
 	 * @param selectedNode
 	 * @param innerNode
 	 */
 	public void internalChangeSelection(Node selectedNode, Node innerNode) {
 		getEditor().setFocus();
 		_hiearchAction.refresh(selectedNode, innerNode);
 		this.getSite().getActionBars().getToolBarManager().update(true);
 		super.selectionChanged(getEditor(), new StructuredSelection(selectedNode));
 	}
 
 	/**
 	 * @return EditorPart instance that this property sheet is for.   Will return instance of WPE (htmlEditor)
 	 */
 	public EditorPart getEditor() {
 		return this._htmlEditor;
 	}
 
 	public void init(IPageSite pageSite) {
 		super.init(pageSite);
 		setSelectionProvider();
 		setSelectionListener();
 		IToolBarManager toolbar = pageSite.getActionBars().getToolBarManager();
 		toolbar.add(_hiearchAction);		
 		_hiearchAction.refresh(null, null);
 	}
 
 	private void setSelectionListener() {
 		this.getSite().getWorkbenchWindow().getSelectionService()
 				.addPostSelectionListener(getSelectionListener());
 	}
 
 	private ISelectionListener getSelectionListener() {
 		if (_selListener == null){
 			_selListener = new ISelectionListener() {
 				public void selectionChanged(IWorkbenchPart part,
 						ISelection selection) {
 					if (getEditor() == part)//only fire if the selection applies to this tabbed prop sheet instance
 						WPETabbedPropertySheetPage.this.selectionChanged(
 							part, selection);
 				}
 			};
 		}
 		return _selListener;
 	}
 
 	private void setSelectionProvider() {
 		this.getSite().setSelectionProvider(new ISelectionProvider() {
 			private ISelection _selection;
 
 			public void addSelectionChangedListener(
 					ISelectionChangedListener listener) {
 			    // do nothing
 			}
 
 			/**
 			 * Returns the current selection for this provider.
 			 * 
 			 * @return the current selection
 			 */
 			public ISelection getSelection() {
 				return _selection;
 			}
 
 			/**
 			 * Removes the given selection change listener from this selection
 			 * provider. Has no affect if an identical listener is not
 			 * registered.
 			 * 
 			 * @param listener
 			 *            a selection changed listener
 			 */
 			public void removeSelectionChangedListener(
 					ISelectionChangedListener listener) {
 				// do nothing
 			}
 
 			/**
 			 * Sets the current selection for this selection provider.
 			 * 
 			 * @param selection
 			 *            the new selection
 			 */
 			public void setSelection(ISelection selection) {
 				_selection = selection;
 			}
 		});
 
 	}
 
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 		PlatformUI
 				.getWorkbench()
 				.getHelpSystem()
 				.setHelp(
 						getControl(),
 						PDPlugin
 								.getResourceString("WPETabbedPropertySheetPage.help.id")); //$NON-NLS-1$
 	}
 
 	/**
 	 * @return acquires an instance of QuickEditTabManager
 	 */
 	public QuickEditTabManager getTabManager() {
 		if (manager == null) {
 			manager = QuickEditTabManager.acquireInstance(this);
 		}
 		return manager;
 	}
 
 	@Override
 	public void dispose() {
 		this.getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(getSelectionListener());
 		this.getSite().setSelectionProvider(null);
		manager.releaseInstance();
		manager.dispose();
		manager = null;
 		_selListener = null;
 		_htmlEditor = null;
 		super.dispose();
 	}
 	
 	
 }
