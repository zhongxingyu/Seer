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
 import org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart;
 import org.eclipse.emf.eef.components.providers.ComponentsMessages;
 import org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.impl.EObjectPropertiesEditionContext;
 import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.part.impl.SectionPropertiesEditingPart;
 import org.eclipse.emf.eef.runtime.policies.PropertiesEditingPolicy;
 import org.eclipse.emf.eef.runtime.providers.PropertiesEditingProvider;
 import org.eclipse.emf.eef.runtime.ui.parts.PartComposer;
 import org.eclipse.emf.eef.runtime.ui.parts.sequence.BindingCompositionSequence;
 import org.eclipse.emf.eef.runtime.ui.parts.sequence.CompositionSequence;
 import org.eclipse.emf.eef.runtime.ui.parts.sequence.CompositionStep;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.EMFModelViewerDialog;
 import org.eclipse.emf.eef.runtime.ui.widgets.FormUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.ReferencesTable;
 import org.eclipse.emf.eef.runtime.ui.widgets.ReferencesTable.ReferencesTableListener;
 import org.eclipse.emf.eef.runtime.ui.widgets.TabElementTreeSelectionDialog;
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
 public class PropertiesMultiEditionElementPropertiesEditionPartForm extends SectionPropertiesEditingPart implements IFormPropertiesEditionPart, PropertiesMultiEditionElementPropertiesEditionPart {
 
 	protected Text name;
 	protected Text helpID;
 	protected TableViewer model;
 	protected Button addModel;
 	protected Button removeModel;
 	protected List<ViewerFilter> modelBusinessFilters = new ArrayList<ViewerFilter>();
 	protected List<ViewerFilter> modelFilters = new ArrayList<ViewerFilter>();
 	protected ReferencesTable views;
 	protected List<ViewerFilter> viewsBusinessFilters = new ArrayList<ViewerFilter>();
 	protected List<ViewerFilter> viewsFilters = new ArrayList<ViewerFilter>();
 
 
 
 	/**
 	 * For {@link ISection} use only.
 	 */
 	public PropertiesMultiEditionElementPropertiesEditionPartForm() { super(); }
 
 	/**
 	 * Default constructor
 	 * @param editionComponent the {@link IPropertiesEditionComponent} that manage this part
 	 * 
 	 */
 	public PropertiesMultiEditionElementPropertiesEditionPartForm(IPropertiesEditionComponent editionComponent) {
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
 		CompositionSequence propertiesMultiEditionElementStep = new BindingCompositionSequence(propertiesEditionComponent);
 		CompositionStep propertiesStep = propertiesMultiEditionElementStep.addStep(ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.class);
 		propertiesStep.addStep(ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name);
 		propertiesStep.addStep(ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID);
 		
 		CompositionStep bindingStep = propertiesMultiEditionElementStep.addStep(ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.class);
 		bindingStep.addStep(ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model);
 		bindingStep.addStep(ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views);
 		
 		
 		composer = new PartComposer(propertiesMultiEditionElementStep) {
 
 			@Override
 			public Composite addToPart(Composite parent, Object key) {
 				if (key == ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.class) {
 					return createPropertiesGroup(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name) {
 					return createNameText(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID) {
 					return createHelpIDText(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.class) {
 					return createBindingGroup(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model) {
 					return createModelReferencesTable(widgetFactory, parent);
 				}
 				if (key == ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views) {
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
 		propertiesSection.setText(ComponentsMessages.PropertiesMultiEditionElementPropertiesEditionPart_PropertiesGroupLabel);
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
 		createDescription(parent, ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name, ComponentsMessages.PropertiesMultiEditionElementPropertiesEditionPart_NameLabel);
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
 							PropertiesMultiEditionElementPropertiesEditionPartForm.this,
 							ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name,
 							PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, name.getText()));
 					propertiesEditionComponent
 							.firePropertiesChanged(new PropertiesEditionEvent(
 									PropertiesMultiEditionElementPropertiesEditionPartForm.this,
 									ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name,
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
 									PropertiesMultiEditionElementPropertiesEditionPartForm.this,
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
 						propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, name.getText()));
 				}
 			}
 		});
 		EditingUtils.setID(name, ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name);
 		EditingUtils.setEEFtype(name, "eef::Text"); //$NON-NLS-1$
 		FormUtils.createHelpButton(widgetFactory, parent, propertiesEditionComponent.getHelpContent(ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.name, ComponentsViewsRepository.FORM_KIND), null); //$NON-NLS-1$
 		return parent;
 	}
 
 	
 	protected Composite createHelpIDText(FormToolkit widgetFactory, Composite parent) {
 		createDescription(parent, ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID, ComponentsMessages.PropertiesMultiEditionElementPropertiesEditionPart_HelpIDLabel);
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
 							PropertiesMultiEditionElementPropertiesEditionPartForm.this,
 							ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID,
 							PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, helpID.getText()));
 					propertiesEditionComponent
 							.firePropertiesChanged(new PropertiesEditionEvent(
 									PropertiesMultiEditionElementPropertiesEditionPartForm.this,
 									ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID,
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
 									PropertiesMultiEditionElementPropertiesEditionPartForm.this,
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
 						propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.SET, null, helpID.getText()));
 				}
 			}
 		});
 		EditingUtils.setID(helpID, ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID);
 		EditingUtils.setEEFtype(helpID, "eef::Text"); //$NON-NLS-1$
 		FormUtils.createHelpButton(widgetFactory, parent, propertiesEditionComponent.getHelpContent(ComponentsViewsRepository.PropertiesMultiEditionElement.Properties.helpID, ComponentsViewsRepository.FORM_KIND), null); //$NON-NLS-1$
 		return parent;
 	}
 
 	/**
 	 * 
 	 */
 	protected Composite createBindingGroup(FormToolkit widgetFactory, final Composite parent) {
 		Section bindingSection = widgetFactory.createSection(parent, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
 		bindingSection.setText(ComponentsMessages.PropertiesMultiEditionElementPropertiesEditionPart_BindingGroupLabel);
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
 	 * 
 	 */
 	protected Composite createModelReferencesTable(FormToolkit widgetFactory, Composite parent) {
 		Label modelLabel = createDescription(parent, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model, ComponentsMessages.PropertiesMultiEditionElementPropertiesEditionPart_ModelLabel);
 		GridData modelLabelData = new GridData();
 		modelLabelData.horizontalSpan = 3;
 		modelLabel.setLayoutData(modelLabelData);
 		model = createModelViewer(parent, widgetFactory, adapterFactory);
 		GridData modelData = new GridData(GridData.FILL_HORIZONTAL);
 		modelData.horizontalSpan = 2;
 		modelData.minimumHeight = 120;
 		modelData.heightHint = 120;
 		model.getTable().setLayoutData(modelData);
 		EditingUtils.setID(model.getTable(), ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model);
 		EditingUtils.setEEFtype(model.getTable(), "eef::ReferencesTable::field"); //$NON-NLS-1$
 		createModelControlPanel(parent, widgetFactory);
 		return parent;
 	}
 
 	/**
 	 * 
 	 */
 	protected TableViewer createModelViewer(Composite container, FormToolkit widgetFactory, AdapterFactory adapter) {
 		org.eclipse.swt.widgets.Table table = widgetFactory.createTable(container, SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		GridData gd = new GridData();
 		gd.grabExcessHorizontalSpace = true;
 		gd.horizontalAlignment = GridData.FILL;
 		gd.grabExcessVerticalSpace = true;
 		gd.verticalAlignment = GridData.FILL;
 		table.setLayoutData(gd);
 		table.setLinesVisible(true);
 		// Start of user code for table model s columns definition
 		TableColumn name = new TableColumn(table, SWT.NONE);
 		name.setWidth(80);
 		name.setText("Label"); //$NON-NLS-1$
 		// End of user code
 
 		TableViewer result = new TableViewer(table);
 		result.setLabelProvider(new ITableLabelProvider() {
 
 			// Start of user code for table model label provider
 			
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
 	protected void createModelControlPanel(Composite container, FormToolkit widgetFactory) {
 		Composite result = widgetFactory.createComposite(container, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		result.setLayout(layout);
 		addModel = widgetFactory.createButton(result, ComponentsMessages.PropertiesEditionPart_AddListViewerLabel, SWT.NONE);
 		GridData addData = new GridData(GridData.FILL_HORIZONTAL);
 		addModel.setLayoutData(addData);
 		addModel.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				addModel();
 			}
 
 		});
 		EditingUtils.setID(addModel, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model);
 		EditingUtils.setEEFtype(addModel, "eef::ReferencesTable::addbutton"); //$NON-NLS-1$
 		removeModel = widgetFactory.createButton(result, ComponentsMessages.PropertiesEditionPart_RemoveListViewerLabel, SWT.NONE);
 		GridData removeData = new GridData(GridData.FILL_HORIZONTAL);
 		removeModel.setLayoutData(removeData);
 		removeModel.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 * 
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				if (model.getSelection() instanceof IStructuredSelection) {
 					removeModel((IStructuredSelection) model.getSelection());
 				}
 			}
 
 		});
 		EditingUtils.setID(removeModel, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model);
 		EditingUtils.setEEFtype(removeModel, "eef::ReferencesTable::removebutton"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	protected void addModel() {
 
 		EMFModelViewerDialog dialog = new EMFModelViewerDialog(new AdapterFactoryLabelProvider(adapterFactory), model.getInput(), modelFilters, modelBusinessFilters, false, true) {
 			public void process(IStructuredSelection selection) {
 				for (Iterator iter = selection.iterator(); iter.hasNext();) {
 					EObject elem = (EObject) iter.next();
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.ADD, null, elem));
 				}
 			}
 
 		};
 		dialog.open();
 		model.refresh();
 	}
 
 	/**
 	 * @param selection the model to remove
 	 * 
 	 */
 	protected void removeModel(IStructuredSelection selection) {
 		for (Iterator iter = selection.iterator(); iter.hasNext();) {
 			EObject elem = (EObject) iter.next();
 			propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.model, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.REMOVE, null, elem));
 		}
 		model.refresh();
 	}
 
 	/**
 	 * 
 	 */
 	protected Composite createViewsReferencesTable(FormToolkit widgetFactory, Composite parent) {
 		this.views = new ReferencesTable(getDescription(ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views, ComponentsMessages.PropertiesMultiEditionElementPropertiesEditionPart_ViewsLabel), new ReferencesTableListener	() {
 			public void handleAdd() { addViews(); }
 			public void handleEdit(EObject element) { editViews(element); }
 			public void handleMove(EObject element, int oldIndex, int newIndex) { moveViews(element, oldIndex, newIndex); }
 			public void handleRemove(EObject element) { removeFromViews(element); }
 			public void navigateTo(EObject element) { }
 		});
 		this.views.setHelpText(propertiesEditionComponent.getHelpContent(ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views, ComponentsViewsRepository.FORM_KIND));
 		this.views.createControls(parent, widgetFactory);
 		this.views.addSelectionListener(new SelectionAdapter() {
 			
 			public void widgetSelected(SelectionEvent e) {
 				if (e.item != null && e.item.getData() instanceof EObject) {
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views, PropertiesEditionEvent.CHANGE, PropertiesEditionEvent.SELECTION_CHANGED, null, e.item.getData()));
 				}
 			}
 			
 		});
 		GridData viewsData = new GridData(GridData.FILL_HORIZONTAL);
 		viewsData.horizontalSpan = 3;
 		this.views.setLayoutData(viewsData);
 		this.views.disableMove();
 		views.setID(ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views);
 		views.setEEFType("eef::AdvancedReferencesTable"); //$NON-NLS-1$
 		return parent;
 	}
 
 	/**
 	 * 
 	 */
 	protected void addViews() {
 		TabElementTreeSelectionDialog dialog = new TabElementTreeSelectionDialog(views.getInput(), viewsFilters, viewsBusinessFilters,
 		"views", propertiesEditionComponent.getEditingContext().getAdapterFactory(), current.eResource()) {
 			@Override
 			public void process(IStructuredSelection selection) {
 				for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
 					EObject elem = (EObject) iter.next();
 					propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views,
 						PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.ADD, null, elem));
 				}
 				views.refresh();
 			}
 		};
 		dialog.open();
 	}
 
 	/**
 	 * 
 	 */
 	protected void moveViews(EObject element, int oldIndex, int newIndex) {
 		propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.MOVE, element, newIndex));
 		views.refresh();
 	}
 
 	/**
 	 * 
 	 */
 	protected void removeFromViews(EObject element) {
 		propertiesEditionComponent.firePropertiesChanged(new PropertiesEditionEvent(PropertiesMultiEditionElementPropertiesEditionPartForm.this, ComponentsViewsRepository.PropertiesMultiEditionElement.Binding.views, PropertiesEditionEvent.COMMIT, PropertiesEditionEvent.REMOVE, null, element));
 		views.refresh();
 	}
 
 	/**
 	 * 
 	 */
 	protected void editViews(EObject element) {
 		EObjectPropertiesEditionContext context = new EObjectPropertiesEditionContext(propertiesEditionComponent.getEditingContext(), propertiesEditionComponent, element, adapterFactory);
 		PropertiesEditingProvider provider = (PropertiesEditingProvider)adapterFactory.adapt(element, PropertiesEditingProvider.class);
 		if (provider != null) {
 			PropertiesEditingPolicy policy = provider.getPolicy(context);
 			if (policy != null) {
 				policy.execute();
 				views.refresh();
 			}
 		}
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
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#getName()
 	 * 
 	 */
 	public String getName() {
 		return name.getText();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#setName(String newValue)
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
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#getHelpID()
 	 * 
 	 */
 	public String getHelpID() {
 		return helpID.getText();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#setHelpID(String newValue)
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
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#initModel(org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings)
 	 */
 	public void initModel(ReferencesTableSettings settings) {
 		if (current.eResource() != null && current.eResource().getResourceSet() != null)
 			this.resourceSet = current.eResource().getResourceSet();
 		ReferencesTableContentProvider contentProvider = new ReferencesTableContentProvider();
 		model.setContentProvider(contentProvider);
 		model.setInput(settings);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#updateModel()
 	 * 
 	 */
 	public void updateModel() {
 	model.refresh();
 }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#addFilterModel(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToModel(ViewerFilter filter) {
 		modelFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#addBusinessFilterModel(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToModel(ViewerFilter filter) {
 		modelBusinessFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#isContainedInModelTable(EObject element)
 	 * 
 	 */
 	public boolean isContainedInModelTable(EObject element) {
 		return ((ReferencesTableSettings)model.getInput()).contains(element);
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#initViews(org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings)
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
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#updateViews()
 	 * 
 	 */
 	public void updateViews() {
 	views.refresh();
 }
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#addFilterViews(ViewerFilter filter)
 	 * 
 	 */
 	public void addFilterToViews(ViewerFilter filter) {
 		viewsFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#addBusinessFilterViews(ViewerFilter filter)
 	 * 
 	 */
 	public void addBusinessFilterToViews(ViewerFilter filter) {
 		viewsBusinessFilters.add(filter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.eef.components.parts.PropertiesMultiEditionElementPropertiesEditionPart#isContainedInViewsTable(EObject element)
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
 		return ComponentsMessages.PropertiesMultiEditionElement_Part_Title;
 	}
 
 	// Start of user code additional methods
  	
 	// End of user code
 
 
 }
