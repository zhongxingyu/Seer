 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: EmfContentOutlinePage.java,v 1.2 2010/01/04 13:27:35 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.internal.editor;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.ui.dnd.*;
 import org.eclipse.emf.edit.ui.provider.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.pde.emfforms.editor.EmfFormEditor;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 
 public class EmfContentOutlinePage extends ContentOutlinePage {
 
 	private EmfFormEditor<? extends EObject> editor;
 
 	/**
 	 * This is the content outline page's viewer.
 	 */
 	protected TreeViewer contentOutlineViewer;
 
 	protected IStatusLineManager contentOutlineStatusLineManager;
 
 	private Object viewerInput;
 
	private ViewerComparator viewerComparator;

 	public EmfContentOutlinePage(EmfFormEditor<? extends EObject> editor) {
 		this.editor = editor;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 		contentOutlineViewer = getTreeViewer();
 		contentOutlineViewer.addSelectionChangedListener(this);
 
 		// Set up the tree viewer.
 		contentOutlineViewer.setContentProvider(new AdapterFactoryContentProvider(editor.getAdapterFactory()));
 		contentOutlineViewer.setLabelProvider(new DecoratingLabelProvider(new AdapterFactoryLabelProvider(editor.getAdapterFactory()), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
 		contentOutlineViewer.setInput(viewerInput);
		contentOutlineViewer.setComparator(viewerComparator);
 		contentOutlineViewer.addFilter(new ViewerFilter() {
 			@Override
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				return (!(AdapterFactoryEditingDomain.unwrap(element) instanceof String));
 			}
 		});
 
 		// Make sure our popups work.
 		createContextMenuFor(contentOutlineViewer);
 
 		if (!editor.getEditingDomain().getResourceSet().getResources().isEmpty()) {
 			// Select the root object in the view.
 			contentOutlineViewer.setSelection(new StructuredSelection(editor.getEditingDomain().getResourceSet().getResources().get(0)), true);
 		}
 
 	}
 
 	@Override
 	public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
 		super.makeContributions(menuManager, toolBarManager, statusLineManager);
 		contentOutlineStatusLineManager = statusLineManager;
 	}
 
 	@Override
 	public void setActionBars(IActionBars actionBars) {
 		super.setActionBars(actionBars);
 		if (editor.getActionBarContributor() != null) {
 			editor.getActionBarContributor().shareGlobalActions(this, actionBars);
 		}
 	}
 
 	/**
 	 * This creates a context menu for the viewer and adds a listener as well registering the menu for extension.
 	 */
 	protected void createContextMenuFor(StructuredViewer viewer) {
 		MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
 		contextMenu.add(new Separator("additions")); //$NON-NLS-1$
 		contextMenu.setRemoveAllWhenShown(true);
 		// TODO
 		// contextMenu.addMenuListener(this);
 		Menu menu = contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		editor.getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
 
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] {LocalTransfer.getInstance()};
 		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
 		viewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(editor.getEditingDomain(), viewer));
 	}
 
 	public void setViewerInput(Object viewerInput) {
 		this.viewerInput = viewerInput;
 	}
 
	public void setViewerComparator(ViewerComparator comparator) {
		this.viewerComparator = comparator;
	}

 	public TreeViewer getViewer() {
 		return contentOutlineViewer;
 	}
 
 }
