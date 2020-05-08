 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.propertiesview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.Page;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 
 /**
  * An alternative Property View for ProR that uses the same renderers as the
  * main Specification Editor.
  * 
  * @author jastram
  * 
  */
 public class ProrPropertySheetPage extends Page implements IPropertySheetPage {
 
 	protected EditingDomain editingDomain;
 
 	private ProrPropertyViewer viewer;
 
 	protected IAction locateValueAction = new LocateValueAction();
 
 	private AdapterFactory adapterFactory;
 
 	private List<AttributeValue> objectsToSelect = new ArrayList<AttributeValue>();;
 
 	public ProrPropertySheetPage(EditingDomain editingDomain,
 			AdapterFactory adapterFactory) {
 		super();
 		this.editingDomain = editingDomain;
 		this.adapterFactory = adapterFactory;
 		
 	}
 
 	@Override
 	public void createControl(Composite parent) {
		// Create new ProR Properties Viewer
		viewer = new ProrPropertyViewer(parent, editingDomain, adapterFactory);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleEntrySelection(event.getSelection());
			}
		});
 	}
 
 	/**
 	 * Handles a selection change in the entry table.
 	 */
 	public void handleEntrySelection(ISelection selection) {
 
 		objectsToSelect.clear();
 
 		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
 
 			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 
 			if (structuredSelection.size() == 1) {
 				AttributeValue atrVal = (AttributeValue) structuredSelection
 						.getFirstElement();
 				objectsToSelect.add(atrVal);
 			} else {
 				Object[] atrVals = structuredSelection.toArray();
 				for (Object obj : atrVals) {
 					objectsToSelect.add((AttributeValue) obj);
 				}
 			}
 
 		}
 
 		locateValueAction.setEnabled(!selection.isEmpty());
 
 	}
 
 	@Override
 	public Control getControl() {
 		if (viewer == null) {
 			return null;
 		}
 		return viewer.getControl();
 	}
 
 	@Override
 	public void setFocus() {
 		getControl().setFocus();
 	}
 
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 
 		if (viewer == null)
 			return;
 
 		if (viewer.getControl().isDisposed())
 			return;
 
 		IStructuredSelection sel = (IStructuredSelection) selection;
 
 		if (sel.size() == 1) {
 			Object obj = sel.getFirstElement();
 			viewer.setInput(obj);
 		} else {
 			viewer.setInput(null);
 		}
 
 	}
 
 	public void refresh() {
 		if (viewer == null) {
 			return;
 		}
 		viewer.refresh();
 	}
 
 	/**
 	 * This method should be overridden to set the selection.
 	 */
 	protected void setSelectionToViewer(List<?> selection) {
 		System.out.println(selection);
 		handleEntrySelection(new StructuredSelection(selection));
 	}
 
 	@Override
 	public void makeContributions(IMenuManager menuManager,
 			IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
 		super.makeContributions(menuManager, toolBarManager, statusLineManager);
 		toolBarManager.add(locateValueAction);
 	}
 
 	protected class LocateValueAction extends Action {
 		public LocateValueAction() {
 			setText(EMFEditUIPlugin.INSTANCE
 					.getString("_UI_LocateValue_action"));
 			setToolTipText(EMFEditUIPlugin.INSTANCE
 					.getString("_UI_LocateValue_action_tool_tip"));
 			setImageDescriptor(ExtendedImageRegistry.INSTANCE
 					.getImageDescriptor(EMFEditUIPlugin.INSTANCE
 							.getImage("full/elcl16/LocateValue")));
 			setDisabledImageDescriptor(ExtendedImageRegistry.INSTANCE
 					.getImageDescriptor(EMFEditUIPlugin.INSTANCE
 							.getImage("full/dlcl16/LocateValue")));
 		}
 
 		@Override
 		public void run() {
 			setSelectionToViewer(objectsToSelect);
 		}
 	}
 
 }
