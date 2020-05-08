 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.editors.pages;
 
 import java.util.List;
 
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
 import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
 import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
 import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
 import org.eclipse.emf.eef.runtime.ui.viewers.PropertiesEditionViewer;
 import org.eclipse.emf.eef.runtime.ui.widgets.masterdetails.AbstractEEFMasterDetailsBlock;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  *
  */
 public abstract class AbstractEEFMDFormPage extends AbstractEEFEditorPage {
 
 	/**
 	 * The page ID
 	 */
 	public static final String PAGE_ID = "EEF-md-form-page";  //$NON-NLS-1$
 	
 	/**
 	 * The form editor in which this page will be included
 	 */
 	private FormEditor editor;
 	
 	/**
 	 * The master/details block for model edition 
 	 */
 	protected AbstractEEFMasterDetailsBlock block;
 
 	/**
 	 * The managed form
 	 */
 	private IManagedForm managedForm;
 
 	private EditingDomainViewerDropAdapter dropAdapter;
 	
 	/**
 	 * @param editor the form editor in which this page will be included
 	 */
 	public AbstractEEFMDFormPage(FormEditor editor, String pageTitle) {
 		super(editor, PAGE_ID, pageTitle); 
 		this.editor = editor;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 		block = createMasterDetailsBlock();
 		block.setEditingDomain(editingDomain);
 		this.managedForm = managedForm;
 		form = managedForm.getForm();
 		managedForm.getToolkit().decorateFormHeading(form.getForm());
 		block.createContent(managedForm);
 		block.getMasterPart().addSelectionChangeListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				getManagedForm().fireSelectionChanged(block.getMasterPart(), event.getSelection());
 			}
 			
 		});
 		createContextMenuFor(block.getMasterPart().getModelViewer());
 		refresh();
 	}
 
 	/**
 	 * @return the MasterDetailsBlock to use for the page
 	 */
 	protected abstract AbstractEEFMasterDetailsBlock createMasterDetailsBlock();
 
 	/**
 	 * This creates a context menu for the viewer and adds a listener as well registering the menu for extension.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createContextMenuFor(StructuredViewer viewer) {
 		MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
 		contextMenu.add(new Separator("additions")); //$NON-NLS-1$
 		contextMenu.setRemoveAllWhenShown(true);
 		contextMenu.addMenuListener((IMenuListener) editor);
 		Menu menu= contextMenu.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
 
 		if (editingDomain != null) {
 			initDragnDrop(viewer);
 		}
 	}
 
 	private void initDragnDrop(StructuredViewer viewer) {
 		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
 		Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
 		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
 		dropAdapter = new EditingDomainViewerDropAdapter(editingDomain, viewer);
 		viewer.addDropSupport(dndOperations, transfers, dropAdapter);
 	}
 	
 	/**
 	 * @return the form
 	 */
 	public IManagedForm getManagedForm() {
 		return managedForm;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.editors.pages.EEFEditorPage#getModelViewer()
 	 */
 	public StructuredViewer getModelViewer() {
 		if (block != null && block.getMasterPart() != null) {
 			return block.getMasterPart().getModelViewer();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.editors.pages.EEFEditorPage#getPropertiesViewer()
 	 */
 	public PropertiesEditionViewer getPropertiesViewer() {
 		if (block != null) {
 			if (block.getLastDetailsPage() != null) {
 				return block.getLastDetailsPage().getViewer();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.ui.editors.pages.AbstractEEFEditorPage#refreshFormContents()
 	 */
 	protected void refreshFormContents() {
 		block.setAdapterFactory(adapterFactory);
 		block.setEditingDomain(editingDomain);
 		block.setInput(input);
 	}
 	
 	
 	public void setEditingDomain(EditingDomain editingDomain) {
 		super.setEditingDomain(editingDomain);
		if (dropAdapter == null && getModelViewer() != null) {
 			initDragnDrop(getModelViewer());
 		}
 	}
 
 	/**
 	 * @return the list of actions to add to the form toolbar
 	 */
 	protected List<Action> additionalPageUserActions() {
 		// nothing to add
 		return null;
 	}
 }
