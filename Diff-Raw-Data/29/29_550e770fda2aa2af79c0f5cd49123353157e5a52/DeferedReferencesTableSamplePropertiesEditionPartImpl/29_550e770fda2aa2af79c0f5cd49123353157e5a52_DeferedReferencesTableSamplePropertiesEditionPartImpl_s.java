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
 package org.eclipse.emf.eef.eefnr.navigation.parts.impl;
 
 // Start of user code for imports
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.eefnr.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart;
 import org.eclipse.emf.eef.eefnr.navigation.parts.NavigationViewsRepository;
 import org.eclipse.emf.eef.eefnr.navigation.providers.NavigationMessages;
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
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
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
 import org.eclipse.swt.widgets.Text;
 
 
 
 // End of user code	
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class DeferedReferencesTableSamplePropertiesEditionPartImpl extends CompositePropertiesEditionPart implements ISWTPropertiesEditionPart, DeferedReferencesTableSamplePropertiesEditionPart {
 
 	protected Text name;
 	protected TableViewer referencesTableSampleEditor;
 	protected Button addReferencesTableSampleEditor;
 	protected Button removeReferencesTableSampleEditor;
 	protected List<ViewerFilter> referencesTableSampleEditorBusinessFilters = new ArrayList<ViewerFilter>();
 	protected List<ViewerFilter> referencesTableSampleEditorFilters = new ArrayList<ViewerFilter>();
 
 
 
 	/**
 	 * Default constructor
 	 * @param editionComponent the {@link IPropertiesEditionComponent} that manage this part
 	 * 
 	 */
 	public DeferedReferencesTableSamplePropertiesEditionPartImpl(IPropertiesEditionComponent editionComponent) {
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
 		propertiesGroup.setText(NavigationMessages.DeferedReferencesTableSamplePropertiesEditionPart_PropertiesGroupLabel);
 		GridData propertiesGroupData = new GridData(GridData.FILL_HORIZONTAL);
 		propertiesGroupData.horizontalSpan = 3;
 		propertiesGroup.setLayoutData(propertiesGroupData);
 		GridLayout propertiesGroupLayout = new GridLayout();
 		propertiesGroupLayout.numColumns = 3;
 		propertiesGroup.setLayout(propertiesGroupLayout);
 		createNameText(propertiesGroup);
 		createReferencesTableSampleEditorReferencesTable(propertiesGroup);
 	}
 
 	
 	protected void createNameText(Composite parent) {
 		SWTUtils.createPartLabel(parent, NavigationMessages.DeferedReferencesTableSamplePropertiesEditionPart_NameLabel, propertiesEditionComponent.isRequired(NavigationViewsRepository.DeferedReferencesTableSample.name, NavigationViewsRepository.SWT_KIND));
 		name = new Text(parent, SWT.BORDER);
 		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
 		name.setLayoutData(nameData);
 		name.addFocusListener(new FocusAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
 			 * 
 			 */
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void focusLost(FocusEvent e) {
 				if (propertiesEditionComponent != null)
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(DeferedReferencesTableSamplePropertiesEditionPartImpl.this, NavigationViewsRepository.DeferedReferencesTableSample.name, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, name.getText()));
 			}
 
 		});
 		name.addKeyListener(new KeyAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
 			 * 
 			 */
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void keyPressed(KeyEvent e) {
 				if (e.character == SWT.CR) {
 					if (propertiesEditionComponent != null)
 						propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(DeferedReferencesTableSamplePropertiesEditionPartImpl.this, NavigationViewsRepository.DeferedReferencesTableSample.name, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, name.getText()));
 				}
 			}
 
 		});
 		EditingUtils.setID(name, NavigationViewsRepository.DeferedReferencesTableSample.name);
 		EditingUtils.setEEFtype(name, "eef::Text"); //$NON-NLS-1$
 		SWTUtils.createHelpButton(parent, propertiesEditionComponent.getHelpContent(NavigationViewsRepository.DeferedReferencesTableSample.name, NavigationViewsRepository.SWT_KIND), null); //$NON-NLS-1$
 	}
 
 	/**
 	 * @param parent
 	 * 
 	 */
 	protected void createReferencesTableSampleEditorReferencesTable(Composite parent) {
 		Label referencesTableSampleEditorLabel = SWTUtils.createPartLabel(parent, NavigationMessages.DeferedReferencesTableSamplePropertiesEditionPart_ReferencesTableSampleEditorLabel, propertiesEditionComponent.isRequired(NavigationViewsRepository.DeferedReferencesTableSample.referencesTableSampleEditor, NavigationViewsRepository.SWT_KIND));
 		GridData referencesTableSampleEditorLabelData = new GridData();
 		referencesTableSampleEditorLabelData.horizontalSpan = 3;
 		referencesTableSampleEditorLabel.setLayoutData(referencesTableSampleEditorLabelData);
 		referencesTableSampleEditor = createReferencesTableSampleEditorViewer(parent, adapterFactory);
 		GridData referencesTableSampleEditorData = new GridData(GridData.FILL_HORIZONTAL);
 		referencesTableSampleEditorData.horizontalSpan = 2;
 		referencesTableSampleEditorData.minimumHeight = 120;
 		referencesTableSampleEditorData.heightHint = 120;
 		referencesTableSampleEditor.getTable().setLayoutData(referencesTableSampleEditorData);
 		EditingUtils.setID(referencesTableSampleEditor.getTable(), NavigationViewsRepository.DeferedReferencesTableSample.referencesTableSampleEditor);
 		EditingUtils.setEEFtype(referencesTableSampleEditor.getTable(), "eef::ReferencesTable::field"); //$NON-NLS-1$		
 		createReferencesTableSampleEditorControlPanel(parent);
 	}
 
 	/**
 	 * 
 	 */
 	protected TableViewer createReferencesTableSampleEditorViewer(Composite container, AdapterFactory adapter) {
 		org.eclipse.swt.widgets.Table table = new org.eclipse.swt.widgets.Table(container, SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		gd.grabExcessVerticalSpace = true;
 		gd.verticalAlignment = GridData.FILL;
 		table.setLayoutData(gd);
 		table.setLinesVisible(true);
 		// Start of user code for table referencesTableSampleEditor s columns definition		
 				TableColumn name = new TableColumn(table, SWT.NONE);
 				name.setWidth(80);
 				name.setText("Label"); //$NON-NLS-1$
 		
 		// End of user code
 		
 		TableViewer result = new TableViewer(table);
 		result.setLabelProvider(new ITableLabelProvider() {
 	
 			// Start of user code for table referencesTableSampleEditor label provider
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
 	protected void createReferencesTableSampleEditorControlPanel(Composite container) {
 		Composite result = new Composite(container, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		result.setLayout(layout);
 		addReferencesTableSampleEditor = new Button(result, SWT.NONE);
 		addReferencesTableSampleEditor.setText(NavigationMessages.PropertiesEditionPart_AddListViewerLabel);
 		GridData addData = new GridData(GridData.FILL_HORIZONTAL);
 		addReferencesTableSampleEditor.setLayoutData(addData);
 		addReferencesTableSampleEditor.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				addReferencesTableSampleEditor();
 			}
 
 		});
 		EditingUtils.setID(addReferencesTableSampleEditor, NavigationViewsRepository.DeferedReferencesTableSample.referencesTableSampleEditor);
 		EditingUtils.setEEFtype(addReferencesTableSampleEditor, "eef::ReferencesTable::addbutton"); //$NON-NLS-1$
 		removeReferencesTableSampleEditor = new Button(result, SWT.NONE);
 		removeReferencesTableSampleEditor.setText(NavigationMessages.PropertiesEditionPart_RemoveListViewerLabel);
 		GridData removeData = new GridData(GridData.FILL_HORIZONTAL);
 		removeReferencesTableSampleEditor.setLayoutData(removeData);
 		removeReferencesTableSampleEditor.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				if (referencesTableSampleEditor.getSelection() instanceof IStructuredSelection) {
 					removeReferencesTableSampleEditor((IStructuredSelection) referencesTableSampleEditor.getSelection());
 				}
 			}
 
 		});
 		EditingUtils.setID(removeReferencesTableSampleEditor, NavigationViewsRepository.DeferedReferencesTableSample.referencesTableSampleEditor);
 		EditingUtils.setEEFtype(removeReferencesTableSampleEditor, "eef::ReferencesTable::removebutton"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	protected void addReferencesTableSampleEditor() {
		EMFModelViewerDialog dialog = new EMFModelViewerDialog(new AdapterFactoryLabelProvider(adapterFactory), resourceSet, referencesTableSampleEditorFilters, referencesTableSampleEditorBusinessFilters, false, true) {
 			public void process(IStructuredSelection selection) {
 				for (Iterator iter = selection.iterator(); iter.hasNext();) {
 					EObject elem = (EObject) iter.next();
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(DeferedReferencesTableSamplePropertiesEditionPartImpl.this, NavigationViewsRepository.DeferedReferencesTableSample.referencesTableSampleEditor, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.ADD, null, elem));
 				}
 			}
 
 		};
 		dialog.open();
 		referencesTableSampleEditor.refresh();
 	}
 
 	/**
 	 * @param selection the referencesTableSampleEditor to remove
 	 * 
 	 */
 	protected void removeReferencesTableSampleEditor(IStructuredSelection selection) {
 		for (Iterator iter = selection.iterator(); iter.hasNext();) {
 			EObject elem = (EObject) iter.next();
 			propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(DeferedReferencesTableSamplePropertiesEditionPartImpl.this, NavigationViewsRepository.DeferedReferencesTableSample.referencesTableSampleEditor, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.REMOVE, null, elem));
 		}
 		referencesTableSampleEditor.refresh();
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
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#getName()
 	 * 
 	 */
 	public String getName() {
 		return name.getText();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#setName(String newValue)
 	 * 
 	 */
 	public void setName(String newValue) {
 		if (newValue != null) {
 			name.setText(newValue);
 		} else {
 			name.setText(""); //$NON-NLS-1$
 		}
 	}
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#initReferencesTableSampleEditor(org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings)
 	 */
 	public void initReferencesTableSampleEditor(ReferencesTableSettings settings) {
 		if (current.eResource() != null && current.eResource().getResourceSet() != null)
 			this.resourceSet = current.eResource().getResourceSet();
 		ReferencesTableContentProvider contentProvider = new ReferencesTableContentProvider();
 		referencesTableSampleEditor.setContentProvider(contentProvider);
 		referencesTableSampleEditor.setInput(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#updateReferencesTableSampleEditor(EObject newValue)
 	 * 
 	 */
 	public void updateReferencesTableSampleEditor(EObject newValue) {
 	referencesTableSampleEditor.refresh();
 }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#addFilterReferencesTableSampleEditor(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToReferencesTableSampleEditor(ViewerFilter filter) {
 		referencesTableSampleEditorFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#addBusinessFilterReferencesTableSampleEditor(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToReferencesTableSampleEditor(ViewerFilter filter) {
 		referencesTableSampleEditorBusinessFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.navigation.parts.DeferedReferencesTableSamplePropertiesEditionPart#isContainedInReferencesTableSampleEditorTable(EObject element)
 	 * 
 	 */
 	public boolean isContainedInReferencesTableSampleEditorTable(EObject element) {
 		return ((ReferencesTableSettings)referencesTableSampleEditor.getInput()).contains(element);
 	}
 
 
 
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart#getTitle()
 	 * 
 	 */
 	public String getTitle() {
 		return NavigationMessages.DeferedReferencesTableSample_Part_Title;
 	}
 
 	// Start of user code additional methods
 	
 	// End of user code
 
 
 }
