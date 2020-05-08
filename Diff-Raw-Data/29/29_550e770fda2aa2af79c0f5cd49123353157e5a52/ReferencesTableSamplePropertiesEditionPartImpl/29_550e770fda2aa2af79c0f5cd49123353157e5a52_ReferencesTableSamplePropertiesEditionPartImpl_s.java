 /*******************************************************************************
  * Copyright (c) 2009 - 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.eefnr.parts.impl;
 
 // Start of user code for imports
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.eefnr.parts.EefnrViewsRepository;
 import org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart;
 import org.eclipse.emf.eef.eefnr.providers.EefnrMessages;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.EMFModelViewerDialog;
 import org.eclipse.emf.eef.runtime.ui.widgets.SWTUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableContentProvider;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 
 
 
 // End of user code	
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class ReferencesTableSamplePropertiesEditionPartImpl extends CompositePropertiesEditionPart implements ISWTPropertiesEditionPart, ReferencesTableSamplePropertiesEditionPart {
 
 	protected TableViewer referencestableRequiredProperty;
 	protected Button addReferencestableRequiredProperty;
 	protected Button removeReferencestableRequiredProperty;
 	protected List<ViewerFilter> referencestableRequiredPropertyBusinessFilters = new ArrayList<ViewerFilter>();
 	protected List<ViewerFilter> referencestableRequiredPropertyFilters = new ArrayList<ViewerFilter>();
 	protected TableViewer referencestableOptionalProperty;
 	protected Button addReferencestableOptionalProperty;
 	protected Button removeReferencestableOptionalProperty;
 	protected List<ViewerFilter> referencestableOptionalPropertyBusinessFilters = new ArrayList<ViewerFilter>();
 	protected List<ViewerFilter> referencestableOptionalPropertyFilters = new ArrayList<ViewerFilter>();
 
 
 
 	/**
 	 * Default constructor
 	 * @param editionComponent the {@link IPropertiesEditionComponent} that manage this part
 	 * 
 	 */
 	public ReferencesTableSamplePropertiesEditionPartImpl(IPropertiesEditionComponent editionComponent) {
 		super(editionComponent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart#
 	 * 			createFigure(org.eclipse.swt.widgets.Composite)
 	 * 
 	 */
 	public Composite createFigure(final Composite parent) {
 		view = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		view.setLayout(layout);
 		createControls(view);
 		return view;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart#
 	 * 			createControls(org.eclipse.swt.widgets.Composite)
 	 * 
 	 */
 	public void createControls(Composite view) { 
 		createPropertiesGroup(view);
 
 
 		// Start of user code for additional ui definition
 		
 		// End of user code
 	}
 
 	/**
 	 * 
 	 */
 	protected void createPropertiesGroup(Composite parent) {
 		Group propertiesGroup = new Group(parent, SWT.NONE);
 		propertiesGroup.setText(EefnrMessages.ReferencesTableSamplePropertiesEditionPart_PropertiesGroupLabel);
 		GridData propertiesGroupData = new GridData(GridData.FILL_HORIZONTAL);
 		propertiesGroupData.horizontalSpan = 3;
 		propertiesGroup.setLayoutData(propertiesGroupData);
 		GridLayout propertiesGroupLayout = new GridLayout();
 		propertiesGroupLayout.numColumns = 3;
 		propertiesGroup.setLayout(propertiesGroupLayout);
 		createReferencestableRequiredPropertyReferencesTable(propertiesGroup);
 		createReferencestableOptionalPropertyReferencesTable(propertiesGroup);
 	}
 
 	/**
 	 * @param parent
 	 * 
 	 */
 	protected void createReferencestableRequiredPropertyReferencesTable(Composite parent) {
 		Label referencestableRequiredPropertyLabel = SWTUtils.createPartLabel(parent, EefnrMessages.ReferencesTableSamplePropertiesEditionPart_ReferencestableRequiredPropertyLabel, propertiesEditionComponent.isRequired(EefnrViewsRepository.ReferencesTableSample.referencestableRequiredProperty, EefnrViewsRepository.SWT_KIND));
 		GridData referencestableRequiredPropertyLabelData = new GridData();
 		referencestableRequiredPropertyLabelData.horizontalSpan = 3;
 		referencestableRequiredPropertyLabel.setLayoutData(referencestableRequiredPropertyLabelData);
 		referencestableRequiredProperty = createReferencestableRequiredPropertyViewer(parent, adapterFactory);
 		GridData referencestableRequiredPropertyData = new GridData(GridData.FILL_HORIZONTAL);
 		referencestableRequiredPropertyData.horizontalSpan = 2;
 		referencestableRequiredPropertyData.minimumHeight = 120;
 		referencestableRequiredPropertyData.heightHint = 120;
 		referencestableRequiredProperty.getTable().setLayoutData(referencestableRequiredPropertyData);
 		EditingUtils.setID(referencestableRequiredProperty.getTable(), EefnrViewsRepository.ReferencesTableSample.referencestableRequiredProperty);
 		EditingUtils.setEEFtype(referencestableRequiredProperty.getTable(), "eef::ReferencesTable::field"); //$NON-NLS-1$		
 		createReferencestableRequiredPropertyControlPanel(parent);
 	}
 
 	/**
 	 * 
 	 */
 	protected TableViewer createReferencestableRequiredPropertyViewer(Composite container, AdapterFactory adapter) {
 		org.eclipse.swt.widgets.Table table = new org.eclipse.swt.widgets.Table(container, SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		gd.grabExcessVerticalSpace = true;
 		gd.verticalAlignment = GridData.FILL;
 		table.setLayoutData(gd);
 		table.setLinesVisible(true);
 		// Start of user code for table referencestableRequiredProperty s columns definition		
 				TableColumn name = new TableColumn(table, SWT.NONE);
 				name.setWidth(80);
 				name.setText("Label"); //$NON-NLS-1$
 		
 		// End of user code
 		
 		TableViewer result = new TableViewer(table);
 		result.setLabelProvider(new ITableLabelProvider() {
 	
 			// Start of user code for table referencestableRequiredProperty label provider
 						public String getColumnText(Object object, int columnIndex) {
 							AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 							if (object instanceof EObject) {
 								switch (columnIndex) {
 								case 0:
 									return labelProvider.getText(object);
 								}
 							}
 							return ""; //$NON-NLS-1$
 						}
 			
 						public Image getColumnImage(Object element, int columnIndex) {
 							return null;
 						}
 						
 			// End of user code
 
 			public void addListener(ILabelProviderListener listener) {
 			}
 
 			public void dispose() {
 			}
 
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 
 			public void removeListener(ILabelProviderListener listener) {
 			}
 
 		});
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	protected void createReferencestableRequiredPropertyControlPanel(Composite container) {
 		Composite result = new Composite(container, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		result.setLayout(layout);
 		addReferencestableRequiredProperty = new Button(result, SWT.NONE);
 		addReferencestableRequiredProperty.setText(EefnrMessages.PropertiesEditionPart_AddListViewerLabel);
 		GridData addData = new GridData(GridData.FILL_HORIZONTAL);
 		addReferencestableRequiredProperty.setLayoutData(addData);
 		addReferencestableRequiredProperty.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				addReferencestableRequiredProperty();
 			}
 
 		});
 		EditingUtils.setID(addReferencestableRequiredProperty, EefnrViewsRepository.ReferencesTableSample.referencestableRequiredProperty);
 		EditingUtils.setEEFtype(addReferencestableRequiredProperty, "eef::ReferencesTable::addbutton"); //$NON-NLS-1$
 		removeReferencestableRequiredProperty = new Button(result, SWT.NONE);
 		removeReferencestableRequiredProperty.setText(EefnrMessages.PropertiesEditionPart_RemoveListViewerLabel);
 		GridData removeData = new GridData(GridData.FILL_HORIZONTAL);
 		removeReferencestableRequiredProperty.setLayoutData(removeData);
 		removeReferencestableRequiredProperty.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				if (referencestableRequiredProperty.getSelection() instanceof IStructuredSelection) {
 					removeReferencestableRequiredProperty((IStructuredSelection) referencestableRequiredProperty.getSelection());
 				}
 			}
 
 		});
 		EditingUtils.setID(removeReferencestableRequiredProperty, EefnrViewsRepository.ReferencesTableSample.referencestableRequiredProperty);
 		EditingUtils.setEEFtype(removeReferencestableRequiredProperty, "eef::ReferencesTable::removebutton"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	protected void addReferencestableRequiredProperty() {
		EMFModelViewerDialog dialog = new EMFModelViewerDialog(new AdapterFactoryLabelProvider(adapterFactory), resourceSet, referencestableRequiredPropertyFilters, referencestableRequiredPropertyBusinessFilters, false, true) {
 			public void process(IStructuredSelection selection) {
 				for (Iterator iter = selection.iterator(); iter.hasNext();) {
 					EObject elem = (EObject) iter.next();
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(ReferencesTableSamplePropertiesEditionPartImpl.this, EefnrViewsRepository.ReferencesTableSample.referencestableRequiredProperty, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.ADD, null, elem));
 				}
 			}
 
 		};
 		dialog.open();
 		referencestableRequiredProperty.refresh();
 	}
 
 	/**
 	 * @param selection the referencestableRequiredProperty to remove
 	 * 
 	 */
 	protected void removeReferencestableRequiredProperty(IStructuredSelection selection) {
 		for (Iterator iter = selection.iterator(); iter.hasNext();) {
 			EObject elem = (EObject) iter.next();
 			propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(ReferencesTableSamplePropertiesEditionPartImpl.this, EefnrViewsRepository.ReferencesTableSample.referencestableRequiredProperty, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.REMOVE, null, elem));
 		}
 		referencestableRequiredProperty.refresh();
 	}
 
 	/**
 	 * @param parent
 	 * 
 	 */
 	protected void createReferencestableOptionalPropertyReferencesTable(Composite parent) {
 		Label referencestableOptionalPropertyLabel = SWTUtils.createPartLabel(parent, EefnrMessages.ReferencesTableSamplePropertiesEditionPart_ReferencestableOptionalPropertyLabel, propertiesEditionComponent.isRequired(EefnrViewsRepository.ReferencesTableSample.referencestableOptionalProperty, EefnrViewsRepository.SWT_KIND));
 		GridData referencestableOptionalPropertyLabelData = new GridData();
 		referencestableOptionalPropertyLabelData.horizontalSpan = 3;
 		referencestableOptionalPropertyLabel.setLayoutData(referencestableOptionalPropertyLabelData);
 		referencestableOptionalProperty = createReferencestableOptionalPropertyViewer(parent, adapterFactory);
 		GridData referencestableOptionalPropertyData = new GridData(GridData.FILL_HORIZONTAL);
 		referencestableOptionalPropertyData.horizontalSpan = 2;
 		referencestableOptionalPropertyData.minimumHeight = 120;
 		referencestableOptionalPropertyData.heightHint = 120;
 		referencestableOptionalProperty.getTable().setLayoutData(referencestableOptionalPropertyData);
 		EditingUtils.setID(referencestableOptionalProperty.getTable(), EefnrViewsRepository.ReferencesTableSample.referencestableOptionalProperty);
 		EditingUtils.setEEFtype(referencestableOptionalProperty.getTable(), "eef::ReferencesTable::field"); //$NON-NLS-1$		
 		createReferencestableOptionalPropertyControlPanel(parent);
 	}
 
 	/**
 	 * 
 	 */
 	protected TableViewer createReferencestableOptionalPropertyViewer(Composite container, AdapterFactory adapter) {
 		org.eclipse.swt.widgets.Table table = new org.eclipse.swt.widgets.Table(container, SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		gd.grabExcessVerticalSpace = true;
 		gd.verticalAlignment = GridData.FILL;
 		table.setLayoutData(gd);
 		table.setLinesVisible(true);
 		// Start of user code for table referencestableOptionalProperty s columns definition		
 				TableColumn name = new TableColumn(table, SWT.NONE);
 				name.setWidth(80);
 				name.setText("Label"); //$NON-NLS-1$
 		
 		// End of user code
 		
 		TableViewer result = new TableViewer(table);
 		result.setLabelProvider(new ITableLabelProvider() {
 	
 			// Start of user code for table referencestableOptionalProperty label provider
 						public String getColumnText(Object object, int columnIndex) {
 							AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 							if (object instanceof EObject) {
 								switch (columnIndex) {
 								case 0:
 									return labelProvider.getText(object);
 								}
 							}
 							return ""; //$NON-NLS-1$
 						}
 			
 						public Image getColumnImage(Object element, int columnIndex) {
 							return null;
 						}
 						
 			// End of user code
 
 			public void addListener(ILabelProviderListener listener) {
 			}
 
 			public void dispose() {
 			}
 
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 
 			public void removeListener(ILabelProviderListener listener) {
 			}
 
 		});
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	protected void createReferencestableOptionalPropertyControlPanel(Composite container) {
 		Composite result = new Composite(container, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		result.setLayout(layout);
 		addReferencestableOptionalProperty = new Button(result, SWT.NONE);
 		addReferencestableOptionalProperty.setText(EefnrMessages.PropertiesEditionPart_AddListViewerLabel);
 		GridData addData = new GridData(GridData.FILL_HORIZONTAL);
 		addReferencestableOptionalProperty.setLayoutData(addData);
 		addReferencestableOptionalProperty.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				addReferencestableOptionalProperty();
 			}
 
 		});
 		EditingUtils.setID(addReferencestableOptionalProperty, EefnrViewsRepository.ReferencesTableSample.referencestableOptionalProperty);
 		EditingUtils.setEEFtype(addReferencestableOptionalProperty, "eef::ReferencesTable::addbutton"); //$NON-NLS-1$
 		removeReferencestableOptionalProperty = new Button(result, SWT.NONE);
 		removeReferencestableOptionalProperty.setText(EefnrMessages.PropertiesEditionPart_RemoveListViewerLabel);
 		GridData removeData = new GridData(GridData.FILL_HORIZONTAL);
 		removeReferencestableOptionalProperty.setLayoutData(removeData);
 		removeReferencestableOptionalProperty.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				if (referencestableOptionalProperty.getSelection() instanceof IStructuredSelection) {
 					removeReferencestableOptionalProperty((IStructuredSelection) referencestableOptionalProperty.getSelection());
 				}
 			}
 
 		});
 		EditingUtils.setID(removeReferencestableOptionalProperty, EefnrViewsRepository.ReferencesTableSample.referencestableOptionalProperty);
 		EditingUtils.setEEFtype(removeReferencestableOptionalProperty, "eef::ReferencesTable::removebutton"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	protected void addReferencestableOptionalProperty() {
		EMFModelViewerDialog dialog = new EMFModelViewerDialog(new AdapterFactoryLabelProvider(adapterFactory), resourceSet, referencestableOptionalPropertyFilters, referencestableOptionalPropertyBusinessFilters, false, true) {
 			public void process(IStructuredSelection selection) {
 				for (Iterator iter = selection.iterator(); iter.hasNext();) {
 					EObject elem = (EObject) iter.next();
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(ReferencesTableSamplePropertiesEditionPartImpl.this, EefnrViewsRepository.ReferencesTableSample.referencestableOptionalProperty, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.ADD, null, elem));
 				}
 			}
 
 		};
 		dialog.open();
 		referencestableOptionalProperty.refresh();
 	}
 
 	/**
 	 * @param selection the referencestableOptionalProperty to remove
 	 * 
 	 */
 	protected void removeReferencestableOptionalProperty(IStructuredSelection selection) {
 		for (Iterator iter = selection.iterator(); iter.hasNext();) {
 			EObject elem = (EObject) iter.next();
 			propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(ReferencesTableSamplePropertiesEditionPartImpl.this, EefnrViewsRepository.ReferencesTableSample.referencestableOptionalProperty, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.REMOVE, null, elem));
 		}
 		referencestableOptionalProperty.refresh();
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 * 
 	 */
 	public void firePropertiesChanged(IPropertiesEditionEvent event) {
 		// Start of user code for tab synchronization
 		
 		// End of user code
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#initReferencestableRequiredProperty(org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings)
 	 */
 	public void initReferencestableRequiredProperty(ReferencesTableSettings settings) {
 		if (current.eResource() != null && current.eResource().getResourceSet() != null)
 			this.resourceSet = current.eResource().getResourceSet();
 		ReferencesTableContentProvider contentProvider = new ReferencesTableContentProvider();
 		referencestableRequiredProperty.setContentProvider(contentProvider);
 		referencestableRequiredProperty.setInput(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#updateReferencestableRequiredProperty(EObject newValue)
 	 * 
 	 */
 	public void updateReferencestableRequiredProperty(EObject newValue) {
 	referencestableRequiredProperty.refresh();
 }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#addFilterReferencestableRequiredProperty(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToReferencestableRequiredProperty(ViewerFilter filter) {
 		referencestableRequiredPropertyFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#addBusinessFilterReferencestableRequiredProperty(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToReferencestableRequiredProperty(ViewerFilter filter) {
 		referencestableRequiredPropertyBusinessFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#isContainedInReferencestableRequiredPropertyTable(EObject element)
 	 * 
 	 */
 	public boolean isContainedInReferencestableRequiredPropertyTable(EObject element) {
 		return ((ReferencesTableSettings)referencestableRequiredProperty.getInput()).contains(element);
 	}
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#initReferencestableOptionalProperty(org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings)
 	 */
 	public void initReferencestableOptionalProperty(ReferencesTableSettings settings) {
 		if (current.eResource() != null && current.eResource().getResourceSet() != null)
 			this.resourceSet = current.eResource().getResourceSet();
 		ReferencesTableContentProvider contentProvider = new ReferencesTableContentProvider();
 		referencestableOptionalProperty.setContentProvider(contentProvider);
 		referencestableOptionalProperty.setInput(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#updateReferencestableOptionalProperty(EObject newValue)
 	 * 
 	 */
 	public void updateReferencestableOptionalProperty(EObject newValue) {
 	referencestableOptionalProperty.refresh();
 }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#addFilterReferencestableOptionalProperty(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToReferencestableOptionalProperty(ViewerFilter filter) {
 		referencestableOptionalPropertyFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#addBusinessFilterReferencestableOptionalProperty(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToReferencestableOptionalProperty(ViewerFilter filter) {
 		referencestableOptionalPropertyBusinessFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.eefnr.parts.ReferencesTableSamplePropertiesEditionPart#isContainedInReferencestableOptionalPropertyTable(EObject element)
 	 * 
 	 */
 	public boolean isContainedInReferencestableOptionalPropertyTable(EObject element) {
 		return ((ReferencesTableSettings)referencestableOptionalProperty.getInput()).contains(element);
 	}
 
 
 
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart#getTitle()
 	 * 
 	 */
 	public String getTitle() {
 		return EefnrMessages.ReferencesTableSample_Part_Title;
 	}
 
 	// Start of user code additional methods
 	
 	// End of user code
 
 
 }
