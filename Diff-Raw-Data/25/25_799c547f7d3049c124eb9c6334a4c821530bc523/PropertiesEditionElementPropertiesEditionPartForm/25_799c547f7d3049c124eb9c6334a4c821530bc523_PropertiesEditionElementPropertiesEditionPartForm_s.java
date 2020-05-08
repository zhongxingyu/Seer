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
 package org.eclipse.emf.eef.components.parts.forms;
 
 // Start of user code for imports
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.components.parts.ComponentsViewsRepository;
 import org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart;
 import org.eclipse.emf.eef.components.providers.ComponentsMessages;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.part.impl.SectionPropertiesEditingPart;
 import org.eclipse.emf.eef.runtime.ui.parts.PartComposer;
 import org.eclipse.emf.eef.runtime.ui.parts.sequence.BindingCompositionSequence;
 import org.eclipse.emf.eef.runtime.ui.parts.sequence.CompositionSequence;
 import org.eclipse.emf.eef.runtime.ui.parts.sequence.CompositionStep;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.ButtonsModeEnum;
 import org.eclipse.emf.eef.runtime.ui.widgets.EMFModelViewerDialog;
 import org.eclipse.emf.eef.runtime.ui.widgets.EObjectFlatComboViewer;
 import org.eclipse.emf.eef.runtime.ui.widgets.FormUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.eobjflatcombo.EObjectFlatComboSettings;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableContentProvider;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
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
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.Form;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.views.properties.tabbed.ISection;
 
 
 
 // End of user code
 
 /**
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  * 
  */
 public class PropertiesEditionElementPropertiesEditionPartForm extends SectionPropertiesEditingPart implements IFormPropertiesEditionPart, PropertiesEditionElementPropertiesEditionPart {
 
 	protected Text name;
 	protected Text helpID;
 	protected EObjectFlatComboViewer model;
 	protected TableViewer views;
 	protected Button addViews;
 	protected Button removeViews;
 	protected List<ViewerFilter> viewsBusinessFilters = new ArrayList<ViewerFilter>();
 	protected List<ViewerFilter> viewsFilters = new ArrayList<ViewerFilter>();
 
 
 
 	/**
 	 * For {@link ISection} use only.
 	 */
 	public PropertiesEditionElementPropertiesEditionPartForm() { super(); }
 
 	/**
 	 * Default constructor
 	 * @param editionComponent the {@link IPropertiesEditionComponent} that manage this part
 	 * 
 	 */
 	public PropertiesEditionElementPropertiesEditionPartForm(IPropertiesEditionComponent editionComponent) {
 		super(editionComponent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart#
 	 *  createFigure(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
 	 * 
 	 */
 	public Composite createFigure(final Composite parent, final FormToolkit widgetFactory) {
 		ScrolledForm scrolledForm = widgetFactory.createScrolledForm(parent);
 		Form form = scrolledForm.getForm();
 		view = form.getBody();
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		view.setLayout(layout);
 		createControls(widgetFactory, view);
 		return scrolledForm;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart#
 	 *  createControls(org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.swt.widgets.Composite)
 	 * 
 	 */
 	public void createControls(final FormToolkit widgetFactory, Composite view) {
 		CompositionSequence propertiesEditionElementStep = new BindingCompositionSequence(propertiesEditionComponent);
 		CompositionStep propertiesStep = propertiesEditionElementStep.addStep(ComponentsViewsRepository.PropertiesEditionElement.Properties.class);
 		propertiesStep.addStep(ComponentsViewsRepository.PropertiesEditionElement.Properties.name);
 		propertiesStep.addStep(ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID);
 		
 		CompositionStep bindingStep = propertiesEditionElementStep.addStep(ComponentsViewsRepository.PropertiesEditionElement.Binding.class);
 		bindingStep.addStep(ComponentsViewsRepository.PropertiesEditionElement.Binding.model);
 		bindingStep.addStep(ComponentsViewsRepository.PropertiesEditionElement.Binding.views);
 		
 		
 		composer = new PartComposer(propertiesEditionElementStep) {
 
 			@Override
 			public Composite addToPart(Composite parent, Object key) {
 				if (key == ComponentsViewsRepository.PropertiesEditionElement.Properties.class) {
 					return createPropertiesGroup(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesEditionElement.Properties.name) {
 					return createNameText(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID) {
 					return createHelpIDText(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesEditionElement.Binding.class) {
 					return createBindingGroup(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesEditionElement.Binding.model) {
 					return createModelFlatComboViewer(parent, widgetFactory);
 				}
 				if (key == ComponentsViewsRepository.PropertiesEditionElement.Binding.views) {
 					return createViewsReferencesTable(widgetFactory, parent);
 				}
 				return parent;
 			}
 		};
 		composer.compose(view);
 	}
 	/**
 	 * 
 	 */
 	protected Composite createPropertiesGroup(FormToolkit widgetFactory, final Composite parent) {
 		Section propertiesSection = widgetFactory.createSection(parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
 		propertiesSection.setText(ComponentsMessages.PropertiesEditionElementPropertiesEditionPart_PropertiesGroupLabel);
 		GridData propertiesSectionData = new GridData(GridData.FILL_HORIZONTAL);
 		propertiesSectionData.horizontalSpan = 3;
 		propertiesSection.setLayoutData(propertiesSectionData);
 		Composite propertiesGroup = widgetFactory.createComposite(propertiesSection);
 		GridLayout propertiesGroupLayout = new GridLayout();
 		propertiesGroupLayout.numColumns = 3;
 		propertiesGroup.setLayout(propertiesGroupLayout);
 		propertiesSection.setClient(propertiesGroup);
 		return propertiesGroup;
 	}
 
 	
 	protected Composite createNameText(FormToolkit widgetFactory, Composite parent) {
 		createDescription(parent, ComponentsViewsRepository.PropertiesEditionElement.Properties.name, ComponentsMessages.PropertiesEditionElementPropertiesEditionPart_NameLabel);
 		name = widgetFactory.createText(parent, ""); //$NON-NLS-1$
 		name.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
 		widgetFactory.paintBordersFor(parent);
 		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
 		name.setLayoutData(nameData);
 		name.addFocusListener(new FocusAdapter() {
 			/**
 			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
 			 * 
 			 */
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void focusLost(FocusEvent e) {
 				if (propertiesEditionComponent != null) {
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(
 							PropertiesEditionElementPropertiesEditionPartForm.this,
 							ComponentsViewsRepository.PropertiesEditionElement.Properties.name,
 							PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, name.getText()));
 					propertiesEditionComponent
 							.firePropertiesChanged(new PropertiesEditionEvent(
 									PropertiesEditionElementPropertiesEditionPartForm.this,
 									ComponentsViewsRepository.PropertiesEditionElement.Properties.name,
 									PropertiesEditionEvent.FOCUS_CHANGED, PropertiesEditionEvent.FOCUS_LOST,
									null, null));
 				}
 			}
 
 			/**
 			 * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
 			 */
 			@Override
 			public void focusGained(FocusEvent e) {
 				if (propertiesEditionComponent != null) {
 					propertiesEditionComponent
 							.firePropertiesChanged(new PropertiesEditionEvent(
 									PropertiesEditionElementPropertiesEditionPartForm.this,
 									null,
 									PropertiesEditionEvent.FOCUS_CHANGED, PropertiesEditionEvent.FOCUS_GAINED,
 									null, null));
 				}
 			}
 		});
 		name.addKeyListener(new KeyAdapter() {
 			/**
 			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
 			 * 
 			 */
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void keyPressed(KeyEvent e) {
 				if (e.character == SWT.CR) {
 					if (propertiesEditionComponent != null)
 						propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesEditionElement.Properties.name, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, name.getText()));
 				}
 			}
 		});
 		EditingUtils.setID(name, ComponentsViewsRepository.PropertiesEditionElement.Properties.name);
 		EditingUtils.setEEFtype(name, "eef::Text"); //$NON-NLS-1$
 		FormUtils.createHelpButton(widgetFactory, parent, propertiesEditionComponent.getHelpContent(ComponentsViewsRepository.PropertiesEditionElement.Properties.name, ComponentsViewsRepository.FORM_KIND), null); //$NON-NLS-1$
 		return parent;
 	}
 
 	
 	protected Composite createHelpIDText(FormToolkit widgetFactory, Composite parent) {
 		createDescription(parent, ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID, ComponentsMessages.PropertiesEditionElementPropertiesEditionPart_HelpIDLabel);
 		helpID = widgetFactory.createText(parent, ""); //$NON-NLS-1$
 		helpID.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
 		widgetFactory.paintBordersFor(parent);
 		GridData helpIDData = new GridData(GridData.FILL_HORIZONTAL);
 		helpID.setLayoutData(helpIDData);
 		helpID.addFocusListener(new FocusAdapter() {
 			/**
 			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
 			 * 
 			 */
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void focusLost(FocusEvent e) {
 				if (propertiesEditionComponent != null) {
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(
 							PropertiesEditionElementPropertiesEditionPartForm.this,
 							ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID,
 							PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, helpID.getText()));
 					propertiesEditionComponent
 							.firePropertiesChanged(new PropertiesEditionEvent(
 									PropertiesEditionElementPropertiesEditionPartForm.this,
 									ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID,
 									PropertiesEditionEvent.FOCUS_CHANGED, PropertiesEditionEvent.FOCUS_LOST,
									null, null));
 				}
 			}
 
 			/**
 			 * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
 			 */
 			@Override
 			public void focusGained(FocusEvent e) {
 				if (propertiesEditionComponent != null) {
 					propertiesEditionComponent
 							.firePropertiesChanged(new PropertiesEditionEvent(
 									PropertiesEditionElementPropertiesEditionPartForm.this,
 									null,
 									PropertiesEditionEvent.FOCUS_CHANGED, PropertiesEditionEvent.FOCUS_GAINED,
 									null, null));
 				}
 			}
 		});
 		helpID.addKeyListener(new KeyAdapter() {
 			/**
 			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
 			 * 
 			 */
 			@Override
 			@SuppressWarnings("synthetic-access")
 			public void keyPressed(KeyEvent e) {
 				if (e.character == SWT.CR) {
 					if (propertiesEditionComponent != null)
 						propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, helpID.getText()));
 				}
 			}
 		});
 		EditingUtils.setID(helpID, ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID);
 		EditingUtils.setEEFtype(helpID, "eef::Text"); //$NON-NLS-1$
 		FormUtils.createHelpButton(widgetFactory, parent, propertiesEditionComponent.getHelpContent(ComponentsViewsRepository.PropertiesEditionElement.Properties.helpID, ComponentsViewsRepository.FORM_KIND), null); //$NON-NLS-1$
 		return parent;
 	}
 
 	/**
 	 * 
 	 */
 	protected Composite createBindingGroup(FormToolkit widgetFactory, final Composite parent) {
 		Section bindingSection = widgetFactory.createSection(parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
 		bindingSection.setText(ComponentsMessages.PropertiesEditionElementPropertiesEditionPart_BindingGroupLabel);
 		GridData bindingSectionData = new GridData(GridData.FILL_HORIZONTAL);
 		bindingSectionData.horizontalSpan = 3;
 		bindingSection.setLayoutData(bindingSectionData);
 		Composite bindingGroup = widgetFactory.createComposite(bindingSection);
 		GridLayout bindingGroupLayout = new GridLayout();
 		bindingGroupLayout.numColumns = 3;
 		bindingGroup.setLayout(bindingGroupLayout);
 		bindingSection.setClient(bindingGroup);
 		return bindingGroup;
 	}
 
 	/**
 	 * @param parent the parent composite
 	 * @param widgetFactory factory to use to instanciante widget of the form
 	 * 
 	 */
 	protected Composite createModelFlatComboViewer(Composite parent, FormToolkit widgetFactory) {
 		createDescription(parent, ComponentsViewsRepository.PropertiesEditionElement.Binding.model, ComponentsMessages.PropertiesEditionElementPropertiesEditionPart_ModelLabel);
 		model = new EObjectFlatComboViewer(parent, !propertiesEditionComponent.isRequired(ComponentsViewsRepository.PropertiesEditionElement.Binding.model, ComponentsViewsRepository.FORM_KIND));
 		widgetFactory.adapt(model);
 		model.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
 		GridData modelData = new GridData(GridData.FILL_HORIZONTAL);
 		model.setLayoutData(modelData);
 		model.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 			 */
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (propertiesEditionComponent != null)
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesEditionElement.Binding.model, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, getModel()));
 			}
 
 		});
 		model.setID(ComponentsViewsRepository.PropertiesEditionElement.Binding.model);
 		FormUtils.createHelpButton(widgetFactory, parent, propertiesEditionComponent.getHelpContent(ComponentsViewsRepository.PropertiesEditionElement.Binding.model, ComponentsViewsRepository.FORM_KIND), null); //$NON-NLS-1$
 		return parent;
 	}
 
 	/**
 	 * 
 	 */
 	protected Composite createViewsReferencesTable(FormToolkit widgetFactory, Composite parent) {
 		Label viewsLabel = createDescription(parent, ComponentsViewsRepository.PropertiesEditionElement.Binding.views, ComponentsMessages.PropertiesEditionElementPropertiesEditionPart_ViewsLabel);
 		GridData viewsLabelData = new GridData();
 		viewsLabelData.horizontalSpan = 3;
 		viewsLabel.setLayoutData(viewsLabelData);
 		views = createViewsViewer(parent, widgetFactory, adapterFactory);
 		GridData viewsData = new GridData(GridData.FILL_HORIZONTAL);
 		viewsData.horizontalSpan = 2;
 		viewsData.minimumHeight = 120;
 		viewsData.heightHint = 120;
 		views.getTable().setLayoutData(viewsData);
 		EditingUtils.setID(views.getTable(), ComponentsViewsRepository.PropertiesEditionElement.Binding.views);
 		EditingUtils.setEEFtype(views.getTable(), "eef::ReferencesTable::field"); //$NON-NLS-1$
 		createViewsControlPanel(parent, widgetFactory);
 		return parent;
 	}
 
 	/**
 	 * 
 	 */
 	protected TableViewer createViewsViewer(Composite container, FormToolkit widgetFactory, AdapterFactory adapter) {
 		org.eclipse.swt.widgets.Table table = widgetFactory.createTable(container, SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		gd.grabExcessVerticalSpace = true;
 		gd.verticalAlignment = GridData.FILL;
 		table.setLayoutData(gd);
 		table.setLinesVisible(true);
 		// Start of user code for table views s columns definition
 		TableColumn name = new TableColumn(table, SWT.NONE);
 		name.setWidth(200);
 		name.setText("Views"); //$NON-NLS-1$
 		// End of user code
 
 		TableViewer result = new TableViewer(table);
 		result.setLabelProvider(new ITableLabelProvider() {
 
 			// Start of user code for table views label provider
 			
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
 			// End of user code
 
 			public Image getColumnImage(Object element, int columnIndex) {
 				return null;
 			}
 
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
 	protected void createViewsControlPanel(Composite container, FormToolkit widgetFactory) {
 		Composite result = widgetFactory.createComposite(container, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		result.setLayout(layout);
 		addViews = widgetFactory.createButton(result, ComponentsMessages.PropertiesEditionPart_AddListViewerLabel, SWT.NONE);
 		GridData addData = new GridData(GridData.FILL_HORIZONTAL);
 		addViews.setLayoutData(addData);
 		addViews.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				addViews();
 			}
 
 		});
 		EditingUtils.setID(addViews, ComponentsViewsRepository.PropertiesEditionElement.Binding.views);
 		EditingUtils.setEEFtype(addViews, "eef::ReferencesTable::addbutton"); //$NON-NLS-1$
 		removeViews = widgetFactory.createButton(result, ComponentsMessages.PropertiesEditionPart_RemoveListViewerLabel, SWT.NONE);
 		GridData removeData = new GridData(GridData.FILL_HORIZONTAL);
 		removeViews.setLayoutData(removeData);
 		removeViews.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				if (views.getSelection() instanceof IStructuredSelection) {
 					removeViews((IStructuredSelection) views.getSelection());
 				}
 			}
 
 		});
 		EditingUtils.setID(removeViews, ComponentsViewsRepository.PropertiesEditionElement.Binding.views);
 		EditingUtils.setEEFtype(removeViews, "eef::ReferencesTable::removebutton"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	protected void addViews() {
 
 		EMFModelViewerDialog dialog = new EMFModelViewerDialog(new AdapterFactoryLabelProvider(adapterFactory), views.getInput(), viewsFilters, viewsBusinessFilters, false, true) {
 			public void process(IStructuredSelection selection) {
 				for (Iterator iter = selection.iterator(); iter.hasNext();) {
 					EObject elem = (EObject) iter.next();
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesEditionElement.Binding.views, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.ADD, null, elem));
 				}
 			}
 
 		};
 		dialog.open();
 		views.refresh();
 	}
 
 	/**
 	 * @param selection the views to remove
 	 * 
 	 */
 	protected void removeViews(IStructuredSelection selection) {
 		for (Iterator iter = selection.iterator(); iter.hasNext();) {
 			EObject elem = (EObject) iter.next();
 			propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesEditionElement.Binding.views, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.REMOVE, null, elem));
 		}
 		views.refresh();
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener#firePropertiesChanged(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
 	 * 
 	 */
 	public void firePropertiesChanged(IPropertiesEditionEvent event) {
 		// Start of user code for tab synchronization
 		// Nothing to do
 		// End of user code
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#getName()
 	 * 
 	 */
 	public String getName() {
 		return name.getText();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#setName(String newValue)
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
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#getHelpID()
 	 * 
 	 */
 	public String getHelpID() {
 		return helpID.getText();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#setHelpID(String newValue)
 	 * 
 	 */
 	public void setHelpID(String newValue) {
 		if (newValue != null) {
 			helpID.setText(newValue);
 		} else {
 			helpID.setText(""); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#getModel()
 	 * 
 	 */
 	public EObject getModel() {
 		if (model.getSelection() instanceof StructuredSelection) {
 			Object firstElement = ((StructuredSelection) model.getSelection()).getFirstElement();
 			if (firstElement instanceof EObject)
 				return (EObject) firstElement;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#initModel(EObjectFlatComboSettings)
 	 */
 	public void initModel(EObjectFlatComboSettings settings) {
 		model.setInput(settings);
 		if (current != null) {
 			model.setSelection(new StructuredSelection(settings.getValue()));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#setModel(EObject newValue)
 	 * 
 	 */
 	public void setModel(EObject newValue) {
 		if (newValue != null) {
 			model.setSelection(new StructuredSelection(newValue));
 		} else {
 			model.setSelection(new StructuredSelection()); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#setModelButtonMode(ButtonsModeEnum newValue)
 	 */
 	public void setModelButtonMode(ButtonsModeEnum newValue) {
 		model.setButtonMode(newValue);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#addFilterModel(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToModel(ViewerFilter filter) {
 		model.addFilter(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#addBusinessFilterModel(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToModel(ViewerFilter filter) {
 		model.addBusinessRuleFilter(filter);
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#initViews(org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings)
 	 */
 	public void initViews(ReferencesTableSettings settings) {
 		if (current.eResource() != null && current.eResource().getResourceSet() != null)
 			this.resourceSet = current.eResource().getResourceSet();
 		ReferencesTableContentProvider contentProvider = new ReferencesTableContentProvider();
 		views.setContentProvider(contentProvider);
 		views.setInput(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#updateViews()
 	 * 
 	 */
 	public void updateViews() {
 	views.refresh();
 }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#addFilterViews(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToViews(ViewerFilter filter) {
 		viewsFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#addBusinessFilterViews(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToViews(ViewerFilter filter) {
 		viewsBusinessFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesEditionElementPropertiesEditionPart#isContainedInViewsTable(EObject element)
 	 * 
 	 */
 	public boolean isContainedInViewsTable(EObject element) {
 		return ((ReferencesTableSettings)views.getInput()).contains(element);
 	}
 
 
 
 
 
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @see org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart#getTitle()
 	 * 
 	 */
 	public String getTitle() {
 		return ComponentsMessages.PropertiesEditionElement_Part_Title;
 	}
 
 	// Start of user code additional methods
  	
 	// End of user code
 
 
 }
