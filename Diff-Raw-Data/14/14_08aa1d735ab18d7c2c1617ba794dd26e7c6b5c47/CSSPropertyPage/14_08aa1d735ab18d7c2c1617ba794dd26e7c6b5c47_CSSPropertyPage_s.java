 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.css.properties;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.IPageSite;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.jboss.tools.jst.css.common.StyleContainer;
 import org.jboss.tools.jst.css.dialog.common.StyleAttributes;
 import org.jboss.tools.jst.css.view.CSSEditorView;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  */
 public class CSSPropertyPage extends TabbedPropertySheetPage implements
 		IChangeListener {
 
 	private DataBindingContext bindingContext;
 
 	private StyleAttributes styleAttributes;
 
 	private CSSEditorView part;
 
 	private Object selectedObject;
 
 	public CSSPropertyPage(
 			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor,
 			CSSEditorView part) {
 		super(tabbedPropertySheetPageContributor);
 		bindingContext = new DataBindingContext();
 		styleAttributes = new StyleAttributes();
 		styleAttributes.addChangeListener(this);
 		this.part = part;
 
 	}
 
 	@Override
 	public void init(IPageSite pageSite) {
 		super.init(pageSite);
 
 		// CSSSelectionListener.getInstance().addSelectionListener(this);
 
 		// FIXED FOR JBIDE-4791
 		pageSite.setSelectionProvider(new ISelectionProvider() {
 
 			public void setSelection(ISelection selection) {
 			}
 
 			public void removeSelectionChangedListener(
 					ISelectionChangedListener listener) {
 			}
 
 			public ISelection getSelection() {
 				return getCurrentSelection();
 			}
 
 			public void addSelectionChangedListener(
 					ISelectionChangedListener listener) {
 			}
 		});
 	}
 
 	@Override
 	public void dispose() {
 		// CSSSelectionListener.getInstance().removeSelectionListener(this);
 		super.dispose();
 	}
 
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 
 		if ((this.part != part) && (selection instanceof IStructuredSelection)) {
 			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 			Object newSelectedObject = structuredSelection.getFirstElement();

			if ((selectedObject == null)
					|| (!selectedObject.equals(newSelectedObject)))
 				super.selectionChanged(part, selection);
			selectedObject = newSelectedObject;

			update();

 		}
 
 	}
 
 	public void update() {
 
 		if (selectedObject instanceof StyleContainer) {
 			getStyleAttributes().removeChangeListener(this);
 			getStyleAttributes().setStyleProperties(
 					((StyleContainer) selectedObject).getStyleAttributes());
 			getStyleAttributes().addChangeListener(this);
 		}
 
 	}
 
 	public void handleChange(ChangeEvent event) {
 
 		if (selectedObject instanceof StyleContainer) {
 
 			((StyleContainer) selectedObject)
 					.applyStyleAttributes(getStyleAttributes()
 							.getStyleProperties());
 		}
 
 	}
 
 
 	/**
 	 * 
 	 * @return
 	 */
 	public DataBindingContext getBindingContext() {
 		return bindingContext;
 	}
 
 	public StyleAttributes getStyleAttributes() {
 		return styleAttributes;
 	}
 
 	public ISelection getCurrentSelection() {
 
 		Object currentSelectedObject = selectedObject;
 		if (selectedObject instanceof StyleContainer)
 			currentSelectedObject = ((StyleContainer) selectedObject)
 					.getStyleObject();
 		return currentSelectedObject != null ? new StructuredSelection(
 				currentSelectedObject) : StructuredSelection.EMPTY;
 
 	}
 }
