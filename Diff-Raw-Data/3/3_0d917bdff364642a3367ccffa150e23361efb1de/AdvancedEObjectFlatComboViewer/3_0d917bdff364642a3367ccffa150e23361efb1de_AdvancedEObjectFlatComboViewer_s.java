 /*****************************************************************************
  * Copyright (c) 2008-2009 CEA LIST, Obeo
  *
  *    
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Patrick Tessier (CEA LIST) Patrick.tessier@cea.fr - Initial API and implementation
  *  Obeo
  *
  *****************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.eef.runtime.EMFPropertiesRuntime;
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
  * This is an Abstract class use to display a label with the referenced named Element For example type of a
  * property
  * 
  * @author Patrick Tessier
  * @author <a href="mailto:jerome.benois@obeo.fr">Jerome Benois</a>
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  */
 public class AdvancedEObjectFlatComboViewer<T extends EObject> implements IPropertiesFilteredWidget {
 
 	/** Image for the remove button */
 	protected final org.eclipse.swt.graphics.Image deleteImage = EMFPropertiesRuntime
 			.getImage(EMFPropertiesRuntime.ICONS_16x16 + "Delete_16x16.gif");
 
 	/** Image for the add button */
 	protected final org.eclipse.swt.graphics.Image addImage = EMFPropertiesRuntime
 			.getImage(EMFPropertiesRuntime.ICONS_16x16 + "Add_16x16.gif");
 
 	private static final String UNDEFINED_VALUE = "<UNDEFINED>";
 
 	/**
 	 * the dialog title
 	 */
 	private String dialogTitle = "";
 
 	/** Associated text */
 	protected Text valueText;
 
 	/** The parent Composite */
 	protected Composite parent;
 
 	protected T selection;
 
 	protected Object input;
 
 	protected Button browseButton;
 
 	protected EObjectFlatComboViewerListener<T> callback;
 
 	/**
 	 * The main composite
 	 */
 	protected Composite composite;
 
 	/**
 	 * The adapter factory.
 	 */
 	protected AdapterFactory adapterFactory = new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 	protected AdapterFactoryLabelProvider labelProvider;
 
 	private EClass restrictToEClass;
 
 	protected List<ViewerFilter> filters;
 
 	protected List<ViewerFilter> brFilters;
 
 	protected FormToolkit widgetFactory;
 
 	protected ButtonsModeEnum button_mode = ButtonsModeEnum.BROWSE;
 
 	private Resource mainResource;
 
 	/**
 	 * the constructor of this display
 	 * 
 	 * @param labeltoDisplay
 	 *            use to display the name is the label
 	 * @param filter
 	 *            use to look for the good element
 	 */
 	public AdvancedEObjectFlatComboViewer(String dialogTitle, Object input, ViewerFilter filter,
 			EClass restrictToEClass, EObjectFlatComboViewerListener<T> callback) {
 		this.restrictToEClass = restrictToEClass;
 		this.dialogTitle = dialogTitle;
 		this.input = input;
 		this.callback = callback;
 		this.labelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 		this.filters = new ArrayList<ViewerFilter>();
 		this.brFilters = new ArrayList<ViewerFilter>();
 	}
 
 	public void createControls(Composite parent, FormToolkit widgetFactory) {
 		this.widgetFactory = widgetFactory;
 		createControls(parent);
 	}
 
 	public void createControls(Composite parent) {
 
 		this.composite = createComposite(parent);
 		this.parent = parent;
 		if (parent instanceof ExpandableComposite) {
 			((ExpandableComposite)parent).setClient(composite);
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
 		Button removeButton = createButton(parent, "", SWT.PUSH);
 		removeButton.setImage(deleteImage);
 		FormData data = new FormData();
 		data.right = new FormAttachment(100, -5);
 		data.top = new FormAttachment(0, -2);
 		removeButton.setLayoutData(data);
 
 		this.browseButton = createButton(parent, "", SWT.PUSH);
 		browseButton.setImage(addImage);
 		data = new FormData();
 		data.right = new FormAttachment(removeButton, 2);
 		data.top = new FormAttachment(0, -2);
 		browseButton.setLayoutData(data);
 
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
 
 	private void createLabels(Composite parent) {
 		// Display label
 		// final Label displayLabel = createLabel(parent, dialogTitle, SWT.NONE);
 		// FormData data = new FormData();
 		// data.left = new FormAttachment(0, 0);
 		// data.top = new FormAttachment(0, 0);
 		// displayLabel.setLayoutData(data);
 
 		// Value Label
 		String value = UNDEFINED_VALUE;
 		if (selection != null) {
 			value = labelProvider.getText(selection);
 		}
 		this.valueText = createText(parent, value, SWT.NONE);
 		valueText.setEditable(false);
 		// TODO set background color and dispose!
 		// valueText.setEnabled(false);
 		// valueText.setBackground(...);
 		FormData data = new FormData();
 		// data.left = new FormAttachment(displayLabel, 5);
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(browseButton, 0);
 		data.top = new FormAttachment(0, 1);
 		valueText.setLayoutData(data);
 		valueText.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				callback.navigateTo(selection);
 			}
 		});
 	}
 
 	private Composite createComposite(Composite parent) {
 		Composite composite;
 		if (widgetFactory == null) {
 			composite = new Composite(parent, SWT.NONE);
 		} else {
 			composite = widgetFactory.createComposite(parent);
 		}
 		return composite;
 	}
 
 	private Button createButton(Composite parent, String text, int style) {
 		Button button;
 		if (widgetFactory == null) {
 			button = new Button(parent, style);
 			button.setText(text);
 		} else {
 			button = widgetFactory.createButton(parent, text, style);
 		}
 		return button;
 	}
 
 	// private Label createLabel(Composite parent, String text, int style) {
 	// Label label;
 	// if (widgetFactory == null) {
 	// label = new Label(parent, SWT.PUSH);
 	// label.setText(text);
 	// } else {
 	// label = widgetFactory.createLabel(parent, text, style);
 	// }
 	// return label;
 	// }
 
 	private Text createText(Composite parent, String value, int style) {
 		Text text;
 		if (widgetFactory == null) {
 			text = new Text(parent, SWT.PUSH);
 			text.setText(value);
 		} else {
 			text = widgetFactory.createText(parent, value, style);
 		}
 		return text;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setSelection(ISelection selection) {
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection structuredSelection = (StructuredSelection)selection;
 			if (!structuredSelection.isEmpty()) {
 				setSelection((T)structuredSelection.getFirstElement());
 			} else {
 				this.valueText.setText(UNDEFINED_VALUE);
 				// this.parent.pack();
 			}
 		}
 	}
 
 	public void setSelection(T selection) {
 		this.selection = selection;
 		String text = labelProvider.getText(selection);
 		if ("".equals(text))
 			this.valueText.setText(UNDEFINED_VALUE);
 		else
 			this.valueText.setText(text);
 		// this.parent.pack();
 	}
 
 	public T getSelection() {
 		return selection;
 	}
 
 	/**
 	 * Behavior executed when browse button is pressed.
 	 */
 	protected void browseButtonPressed() {
 		switch (button_mode) {
 			case BROWSE:
 				TabElementTreeSelectionDialog<T> dialog = new TabElementTreeSelectionDialog<T>(input,
 						filters, brFilters, dialogTitle, restrictToEClass, mainResource) {
 					@SuppressWarnings("unchecked")
 					@Override
 					public void process(IStructuredSelection selection) {
 						if (selection != null && !selection.isEmpty()) {
 							handleSelection((T)selection.getFirstElement());
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
 
 	// protected void browseButtonPressed() {
 	// TabElementTreeSelectionDialog<T> dialog = new TabElementTreeSelectionDialog<T>(input, filters,
 	// brFilters, dialogTitle, restrictToEClass) {
 	// @SuppressWarnings("unchecked")
 	// @Override
 	// public void process(IStructuredSelection selection) {
 	// if (selection != null && !selection.isEmpty()) {
 	// handleSelection((T)selection.getFirstElement());
 	// }
 	// }
 	// };
 	// // Select the actual element in dialog
 	// if (selection != null) {
 	// dialog.setSelection(new StructuredSelection(selection));
 	// }
 	// dialog.open();
 	// }
 
 	public void handleSelection(T selectedElement) {
 		setSelection(selectedElement);
 		callback.handleSet(selectedElement);
 	}
 
 	public void handleCreate() {
 		setSelection(callback.handleCreate());
 	}
 
 	public interface EObjectFlatComboViewerListener<T extends EObject> {
 		public void handleSet(T element);
 
 		public void navigateTo(T element);
 
 		public T handleCreate();
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
 		valueText.setEnabled(enabled);
 	}
 
 	/**
 	 * Sets the tooltip text on the viewer
 	 * 
 	 * @param tooltip
 	 *            the tooltip text
 	 */
 	public void setToolTipText(String tooltip) {
 		browseButton.setToolTipText(tooltip);
 		valueText.setToolTipText(tooltip);
 	}
 
 }
