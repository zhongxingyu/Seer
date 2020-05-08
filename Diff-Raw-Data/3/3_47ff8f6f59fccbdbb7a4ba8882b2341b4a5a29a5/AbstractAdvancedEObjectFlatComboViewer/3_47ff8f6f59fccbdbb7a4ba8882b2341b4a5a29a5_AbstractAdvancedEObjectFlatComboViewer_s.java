 /*******************************************************************************
  * Copyright (c) 2008, 2011 CEA LIST and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
  *     Obeo - Some improvements
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettings;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * This is an Abstract class use to display a label with the referenced named
  * Element For example type of a property
  * 
  * @author Patrick Tessier
  * @author <a href="mailto:jerome.benois@obeo.fr">Jerome Benois</a>
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  */
 public abstract class AbstractAdvancedEObjectFlatComboViewer implements
 		IPropertiesFilteredWidget {
 
 	/** Image for the remove button */
 	protected final org.eclipse.swt.graphics.Image deleteImage = EEFRuntimePlugin
 			.getImage(EEFRuntimePlugin.ICONS_16x16 + "Delete_16x16.gif"); //$NON-NLS-1$
 
 	/** Image for the add button */
 	protected final org.eclipse.swt.graphics.Image addImage = EEFRuntimePlugin
 			.getImage(EEFRuntimePlugin.ICONS_16x16 + "Add_16x16.gif"); //$NON-NLS-1$
 
 	protected static final String UNDEFINED_VALUE = "<UNDEFINED>"; //$NON-NLS-1$
 
 	/**
 	 * the dialog title
 	 */
 	private String dialogTitle = ""; //$NON-NLS-1$
 
 	/** The parent Composite */
 	protected Composite parent;
 
 	protected EObject selection;
 
 	protected Object input;
 
 	protected Button browseButton;
 
 	protected EObjectFlatComboViewerListener callback;
 
 	/**
 	 * The main composite
 	 */
 	protected Composite composite;
 
 	/**
 	 * The adapter factory.
 	 */
 	protected AdapterFactory adapterFactory;
 
 	protected AdapterFactoryLabelProvider labelProvider;
 
 	protected List<ViewerFilter> filters;
 
 	protected List<ViewerFilter> brFilters;
 
 	protected FormToolkit widgetFactory;
 
 	protected ButtonsModeEnum button_mode = ButtonsModeEnum.BROWSE;
 
 	private Resource mainResource;
 
 	private Button removeButton;
 
 	protected Text field;
 
 	/**
 	 * the constructor of this display
 	 * 
 	 * @param labeltoDisplay
 	 *            use to display the name is the label
 	 * @param filter
 	 *            use to look for the good element
 	 */
 	public AbstractAdvancedEObjectFlatComboViewer(String dialogTitle, Object input,
 			ViewerFilter filter, AdapterFactory adapterFactory,
 			EObjectFlatComboViewerListener callback) {
 		this.dialogTitle = dialogTitle;
 		this.input = input;
 		this.callback = callback;
 		this.labelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 		this.filters = new ArrayList<ViewerFilter>();
 		this.brFilters = new ArrayList<ViewerFilter>();
 		this.adapterFactory = adapterFactory;
 	}
 
 	public void createControls(Composite parent, FormToolkit widgetFactory) {
 		this.widgetFactory = widgetFactory;
 		createControls(parent);
 	}
 
 	public void createControls(Composite parent) {
 
 		this.composite = createComposite(parent);
 		this.parent = parent;
 		if (parent instanceof ExpandableComposite) {
 			((ExpandableComposite) parent).setClient(composite);
 		}
 
 		FormLayout layout = new FormLayout();
 		layout.marginWidth = 1;// 7;
 		layout.marginHeight = 1;// 4;
 		layout.spacing = 7;
 		// layout.marginTop = 7;
 		composite.setLayout(layout);
 
 		// browse and remove Buttons
 		createButtons(composite);
 		// display and value labels
 		createLabels(composite);
 
 	}
 
 	private void createButtons(Composite parent) {
 		removeButton = createButton(parent, "", SWT.PUSH);
 		removeButton.setImage(deleteImage);
 		FormData data = new FormData();
 		data.right = new FormAttachment(100, -5);
 		data.top = new FormAttachment(0, -2);
 		removeButton.setLayoutData(data);
 		removeButton
 				.setToolTipText(EEFRuntimeUIMessages.AdvancedEObjectFlatComboViewer_remove_tooltip);
 		EditingUtils.setEEFtype(removeButton, "eef::AdvancedEObjectFlatComboViewer::removebutton");
 
 		this.browseButton = createButton(parent, "", SWT.PUSH); //$NON-NLS-1$
 		browseButton.setImage(addImage);
 		data = new FormData();
 		data.right = new FormAttachment(removeButton, 2);
 		data.top = new FormAttachment(0, -2);
 		browseButton.setLayoutData(data);
 		browseButton.setToolTipText(EEFRuntimeUIMessages.AdvancedEObjectFlatComboViewer_set_tooltip);
 		EditingUtils.setEEFtype(browseButton, "eef::AdvancedEObjectFlatComboViewer::browsebutton");
 
 		// listeners setting
 		removeButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				// reset value
 				handleSelection(null);
 			}
 		});
 		browseButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				browseButtonPressed();
 			}
 		});
 	}
 
 	public void setInput(Object input) {
 		this.input = input;
 	}
 
 	/**
 	 * Sets the given ID to the EObjectFlatComboViewer
 	 * 
 	 * @param id
 	 *            the id of the widget
 	 */
 	public void setID(Object id) {
 		EditingUtils.setID(field, id);
 		EditingUtils.setID(removeButton, id);
 		EditingUtils.setID(browseButton, id);
 	}
 
 	/**
 	 * @return the ID of the EObjectFlatComboViewer
 	 */
 	public Object getID() {
 		return EditingUtils.getID(field);
 	}
 
 	protected abstract void createLabels(Composite parent);
 
 	private Composite createComposite(Composite parent) {
 		Composite composite;
 		if (widgetFactory == null) {
 			composite = new Composite(parent, SWT.NONE);
 		} else {
 			composite = widgetFactory.createComposite(parent);
 		}
 		return composite;
 	}
 
 	protected Button createButton(Composite parent, String text, int style) {
 		Button button;
 		if (widgetFactory == null) {
 			button = new Button(parent, style);
 			button.setText(text);
 		} else {
 			button = widgetFactory.createButton(parent, text, style);
 		}
 		return button;
 	}
 
 	public abstract void setSelection(ISelection selection);
 
 	public abstract void setSelection(EObject selection);
 
 	public EObject getSelection() {
 		return selection;
 	}
 
 	/**
 	 * Behavior executed when browse button is pressed.
 	 */
 	protected void browseButtonPressed() {
 		switch (button_mode) {
 		case BROWSE:
 			TabElementTreeSelectionDialog dialog = new TabElementTreeSelectionDialog(
 					input, filters, brFilters, dialogTitle, adapterFactory,
 					getMainResource()) {
 				@Override
 				public void process(IStructuredSelection selection) {
 					if (selection != null && !selection.isEmpty()) {
 						handleSelection((EObject) selection.getFirstElement());
 					}
 				}
 			};
 			// Select the actual element in dialog
 			if (selection != null) {
 				dialog.setSelection(new StructuredSelection(selection));
 			}
 			dialog.open();
 			break;
 		case CREATE:
 			handleCreate();
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void handleSelection(EObject selectedElement) {
 		setSelection(selectedElement);
 		callback.handleSet(selectedElement);
 	}
 
 	public void handleCreate() {
 		setSelection(callback.handleCreate());
 	}
 	
 	public void handleEdit(EObject element) {
 		callback.handleEdit(element);
 		// refresh the text;
 		setSelection(element);
 	}
 
 	public interface EObjectFlatComboViewerListener {
 		public void handleSet(EObject element);
 
 		public void navigateTo(EObject element);
 
 		public EObject handleCreate();
 		
 		public void handleEdit(EObject element);
 	}
 
 	/**
 	 * @param layoutData
 	 *            the layoutData to set
 	 */
 	public void setLayoutData(Object layoutData) {
 		composite.setLayoutData(layoutData);
 	}
 
 	public void addFilter(ViewerFilter filter) {
 		filters.add(filter);
 	}
 
 	public void addBusinessRuleFilter(ViewerFilter filter) {
 		brFilters.add(filter);
 	}
 
 	public void removeBusinessRuleFilter(ViewerFilter filter) {
 		brFilters.remove(filter);
 	}
 
 	public void removeFilter(ViewerFilter filter) {
 		filters.remove(filter);
 	}
 
 	public void setButtonMode(ButtonsModeEnum button_mode) {
 		this.button_mode = button_mode;
 	}
 	
 	/**
 	 * Returns the main resource.
 	 * 
 	 * @return the main resource.
 	 */
 
 	public Resource getMainResource() {
 		if (this.mainResource == null && this.input instanceof EEFEditorSettings) {
 			Resource mainResourceFromSettings = null;
 
 			// Gets the mainResource from the settings.
 			EEFEditorSettings settings = (EEFEditorSettings) this.input;
 			if (settings.getSource() != null) {
 				// The default mainResource is the resource of the edited
 				// object.
 				mainResourceFromSettings = settings.getSource().eResource();
 				if (settings.getValue() instanceof EObject && ((EObject) settings.getValue()).eResource() != null) {
 
 					// The mainResource is the resource of the value.
 					mainResourceFromSettings = ((EObject) settings.getValue()).eResource();
 				}
 			}
 			if (mainResourceFromSettings != null) {
 				return mainResourceFromSettings;
 			}
 		}
 
 		//
 
 		// Client has specified a main resource. Let's use it !
 
 		return this.mainResource;
 
 	}
 
 	public void setMainResource(Resource mainResource) {
 		this.mainResource = mainResource;
 	}
 
 	/**
 	 * Sets the viewer readonly
 	 * 
 	 * @param enabled
 	 *            sets the viewer read only or not.
 	 */
 	public void setEnabled(boolean enabled) {
 		browseButton.setEnabled(enabled);
 		removeButton.setEnabled(enabled);
 	}
 	
 	/**
 	 * @return if the table is enabled
 	 */
 	public boolean isEnabled() {
 		return true;
 	}
 
 	/**
 	 * Sets the tooltip text on the viewer
 	 * 
 	 * @param tooltip
 	 *            the tooltip text
 	 */
 	public void setToolTipText(String tooltip) {
 		browseButton.setToolTipText(tooltip);
 	}
 
 }
