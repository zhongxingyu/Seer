 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.mpc.ui.CatalogDescriptor;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 
 /**
  * @author David Green
  */
 public class CatalogSelectionPage extends WizardPage {
 
 	private final MarketplaceCatalogConfiguration configuration;
 
 	private CatalogListViewer viewer;
 
 	public CatalogSelectionPage(MarketplaceCatalogConfiguration configuration) {
 		super(CatalogSelectionPage.class.getName());
 		this.configuration = configuration;
 		setTitle(Messages.CatalogSelectionPage_solutionMarketplaceCatalog);
 		setDescription(Messages.CatalogSelectionPage_selectASolutionCatalog);
 		setPageComplete(configuration.getCatalogDescriptor() != null);
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		container.setLayout(new GridLayout(1, true));
 
 		viewer = new CatalogListViewer(container, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, true).hint(500, 200).applyTo(viewer.getControl());
 		viewer.setSorter(new ViewerSorter() {
 			@Override
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if (e1 == e2) {
 					return 0;
 				}
 				CatalogDescriptor descriptor1 = (CatalogDescriptor) e1;
 				CatalogDescriptor descriptor2 = (CatalogDescriptor) e2;
 				int i = super.compare(viewer, descriptor1.getLabel(), descriptor2.getLabel());
 				if (i == 0) {
 					i = super.compare(viewer, descriptor1.getUrl().toString(), descriptor2.getUrl().toString());
 				}
 				return i;
 			}
 		});
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 				configuration.setCatalogDescriptor(selection.isEmpty() ? null
 						: (CatalogDescriptor) selection.getFirstElement());
 				setPageComplete(configuration.getCatalogDescriptor() != null);
 			}
 		});
 		viewer.setContentProvider(new IStructuredContentProvider() {
 
 			public void dispose() {
 			}
 
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			}
 
 			public Object[] getElements(Object inputElement) {
 				if (inputElement == configuration) {
 					return configuration.getCatalogDescriptors().toArray();
 				}
 				return new Object[0];
 			}
 
 		});
 		viewer.setInput(configuration);

 		setControl(container);
 		Dialog.applyDialogFont(container);
 		MarketplaceClientUi.setDefaultHelp(getControl());
 	}
 
 	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			if (configuration.getCatalogDescriptor() == null) {
				for (CatalogDescriptor descriptor : configuration.getCatalogDescriptors()) {
					if (descriptor.getUrl().getHost().endsWith(".eclipse.org")) { //$NON-NLS-1$
						configuration.setCatalogDescriptor(descriptor);
						break;
					}
				}
			}
			if (configuration.getCatalogDescriptor() != null) {
				viewer.setSelection(new StructuredSelection(configuration.getCatalogDescriptor()));
				setPageComplete(true);
			}
		}
		super.setVisible(visible);
	}

	@Override
 	public IWizardPage getPreviousPage() {
 		return null;
 	}
 
 	@Override
 	public void performHelp() {
 		getControl().notifyListeners(SWT.Help, new Event());
 	}
 }
