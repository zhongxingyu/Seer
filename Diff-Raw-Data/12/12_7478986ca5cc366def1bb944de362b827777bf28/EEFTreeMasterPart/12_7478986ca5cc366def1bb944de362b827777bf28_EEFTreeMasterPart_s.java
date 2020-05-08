 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets.masterdetails.tree;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFCommandParameter;
 import org.eclipse.emf.eef.runtime.ui.layout.EEFFormLayoutFactory;
 import org.eclipse.emf.eef.runtime.ui.notify.DropDownSelectionListener;
 import org.eclipse.emf.eef.runtime.ui.widgets.masterdetails.AbstractEEFMasterDetailsBlock;
 import org.eclipse.emf.eef.runtime.ui.widgets.masterdetails.AbstractEEFMasterPart;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EEFTreeMasterPart extends AbstractEEFMasterPart {
 
 	private List<EEFCommandParameter> commandParameters;
 
 	private EObject modelRoot;
 
 	private EditingDomain editingDomain;
 
 	private Composite modelToolBar;
 
 	private boolean showModelToolBar = true;
 
 	/**
 	 * @param managedForm
 	 *            the form where this part will be
 	 * @param container
 	 *            the composite where to create the part
 	 * @param block
 	 *            the AbstractEEFMasterDetailsBlock which will contain this part
 	 */
 	public EEFTreeMasterPart(FormToolkit toolkit, Composite container, AbstractEEFMasterDetailsBlock block) {
 		super(toolkit, container, block);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.masterdetails.AbstractEEFMasterPart#createSectionClientContents(org.eclipse.swt.widgets.Composite,
 	 *      org.eclipse.ui.forms.widgets.FormToolkit)
 	 */
 	protected TreeViewer createSectionClientContents(Composite sectionContainer, FormToolkit toolkit) {
 		Tree tree = toolkit.createTree(sectionContainer, SWT.MULTI | SWT.BORDER);
 		TreeViewer result = new TreeViewer(tree);
 		result.setContentProvider(new AdapterFactoryContentProvider(getAdapterFactory()));
 		result.setLabelProvider(new AdapterFactoryLabelProvider(getAdapterFactory()));
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		tree.setLayoutData(gd);
 		return result;
 	}
 
 	/**
 	 * @return the list of command parameters
 	 */
 	public List<EEFCommandParameter> getCommandParameter() {
 		if (commandParameters == null) {
 			createCommandParameters();
 		}
 		return commandParameters;
 	}
 
 	/**
 	 * initialize the command parameters. If this method is called whether command parameters are already
 	 * initialized, old values will be erased
 	 */
 	private void createCommandParameters() {
 		if (modelRoot == null) {
 			commandParameters = new ArrayList<EEFCommandParameter>();
 		} else {
 			ViewerFilter[] filters = getModelViewer().getFilters();
 			commandParameters = new ArrayList<EEFCommandParameter>();
 			for (EReference reference : modelRoot.eClass().getEAllReferences()) {
 				if (!reference.isContainment()) {
 					// Create command parameter for containment references only.
 					continue;
 				} else if (filters.length > 0) {
 					// if filters has been added
 					for (ViewerFilter filter : filters) {
 						if (filter.select(modelViewer, modelRoot,
 								EcoreUtil.create(reference.getEReferenceType()))) {
 							commandParameters.add(new EEFCommandParameter(reference, reference
 									.getEReferenceType()));
 						}
 					}
 				} else {
 					commandParameters.add(new EEFCommandParameter(reference, reference.getEReferenceType()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createSectionClient(Composite sectionContainer, FormToolkit toolkit) {
 		sectionContainer.setLayout(new GridLayout());
 		createModelRoot();
 		if (modelRoot != null) {
 			createToolBar(sectionContainer, toolkit);
 		}
 		createModelViewer(sectionContainer, toolkit);
 	}
 
 	/**
 	 * initialize model root
 	 */
 	private void createModelRoot() {
 		editingDomain = getBlock().getEditingDomain();
 		if (editingDomain instanceof AdapterFactoryEditingDomain) {
 			AdapterFactoryEditingDomain afed = (AdapterFactoryEditingDomain)editingDomain;
 			setAdapterFactory(afed.getAdapterFactory());
 			EList<Resource> resources = afed.getResourceSet().getResources();
 			if (resources != null && resources.size() != 0) {
 				// Assuming the main resource is the first resource
 				Resource resource = resources.get(0);
 				if (resource.getContents() != null && resource.getContents().size() > 0) {
 					modelRoot = resource.getContents().get(0);
 				}
 			}
 		}
 	}
 
 	/**
 	 * create the tool bar (add button and delete button)
 	 * 
 	 * @param sectionContainer
 	 * @param toolkit
 	 */
 	private void createToolBar(Composite sectionContainer, FormToolkit toolkit) {
 		if (modelRoot != null) {
 			modelToolBar = new Composite(sectionContainer, SWT.NONE);
 			GridLayout layout = new GridLayout();
 			layout.numColumns = 2;
 			layout.makeColumnsEqualWidth = true;
 			modelToolBar.setLayout(layout);
 
 			// if the root element doesn't has any child, the toolbar (add and delete buttons) is hidden.
 			if (modelRoot.eClass().getEAllReferences().size() == 0)
 				modelToolBar.setVisible(false);
 
 			// Add button
 			createAddButton(toolkit, modelToolBar);
 
 			// delete button
 			createDeleteButton(toolkit, modelToolBar);
 		}
 	}
 
 	/**
 	 * Create the TreeView container
 	 * 
 	 * @param sectionContainer
 	 * @param toolkit
 	 */
 	private void createModelViewer(Composite sectionContainer, FormToolkit toolkit) {
 		Composite modelViewerComposite = new Composite(sectionContainer, SWT.NONE);
 		modelViewerComposite.setLayout(EEFFormLayoutFactory.createMasterGridLayout(false, 1));
 		modelViewerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		super.createSectionClient(modelViewerComposite, toolkit);
 	}
 
 	/**
 	 * initialize delete button
 	 * 
 	 * @param toolkit
 	 */
 	private void createDeleteButton(FormToolkit toolkit, Composite modelToolBar) {
 		Button delete = toolkit.createButton(modelToolBar, "", SWT.PUSH);
 		delete.setImage(EEFRuntimePlugin.getImage(EEFRuntimePlugin.ICONS_16x16 + "Delete_16x16.gif"));
 		delete.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				IStructuredSelection selection = (IStructuredSelection)getModelViewer().getSelection();
 				for (Object object : selection.toList()) {
 					if (editingDomain != null) {
 						Command command = RemoveCommand.create(editingDomain, object);
 						editingDomain.getCommandStack().execute(command);
 					}
 				}
 
 			}
 
 		});
 	}
 
 	/**
 	 * initialize add button
 	 * 
 	 * @param toolkit
 	 */
 	private void createAddButton(FormToolkit toolkit, Composite modelToolBar) {
 		Button add = toolkit.createButton(modelToolBar, "", SWT.PUSH);
 		add.setImage(EEFRuntimePlugin.getImage(EEFRuntimePlugin.ICONS_16x16 + "Add_16x16.gif"));
 
 		DropDownSelectionListener listenerAdd = new DropDownSelectionListener(add) {
 			boolean init = false;
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!init) {
 					// add every element into menu
 					for (EEFCommandParameter commandParameter : getCommandParameter()) {
 						add(commandParameter);
 					}
 					if (menu.getItemCount() == 0) {
 						((Button)e.getSource()).setEnabled(false);
 					}
 					init = true;
 				}
 				super.widgetSelected(e);
 			}
 
 			public void add(final EEFCommandParameter commandParameter) {
 				IItemLabelProvider adapte = (IItemLabelProvider)getAdapterFactory().adapt(
 						EcoreUtil.create(commandParameter.geteClass()), IItemLabelProvider.class);
 				URL image = (URL)adapte.getImage(EcoreUtil.create(commandParameter.geteClass()));
 
 				MenuItem menuItem = new MenuItem(menu, SWT.NONE);
 				menuItem.setText(adapte.getText(EcoreUtil.create(commandParameter.geteClass())));
 				menuItem.setImage(ImageDescriptor.createFromURL(image).createImage(menuItem.getDisplay()));
 
 				menuItem.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 
 						EObject value = EcoreUtil.create(commandParameter.geteClass());
 
 						if (editingDomain != null) {
 							Command command = AddCommand.create(editingDomain, modelRoot, null, value);
 							editingDomain.getCommandStack().execute(command);
 						}
						try {
							modelRoot.eResource().save(Collections.EMPTY_MAP);

						} catch (IOException e1) {
							e1.printStackTrace();
						}
 					}
 				});
 			}
 		};
 
 		add.addSelectionListener(listenerAdd);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void addFilter(ViewerFilter filter) {
 		super.addFilter(filter);
 		createCommandParameters();
 		if (modelToolBar != null) {
 			if (commandParameters.size() == 0) {
 				modelToolBar.setVisible(false);
 			} else if (showModelToolBar != false) {
 				modelToolBar.setVisible(true);
 			}
 		}
 	}
 
 	public void showToolBar(boolean value) {
 		if (modelToolBar != null) {
 			modelToolBar.setVisible(value);
 			showModelToolBar = value;
 		}
 	}
 
 }
