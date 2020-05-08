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
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.emf.eef.runtime.ui.utils.EditingUtils;
 import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class FlatReferencesTable extends Composite implements ISelectionProvider, IPropertiesFilteredWidget {
 
 	/**
 	 * The text displaying the current values of the feature
 	 */
 	private Text selection;
 
 	/**
 	 * Button to edit the feature
 	 */
 	protected Button editer;
 
 	/**
 	 * The static filters
 	 */
 	protected List<ViewerFilter> filters;
 
 	/**
 	 * The filters that can be enabled/disabled
 	 */
 	protected List<ViewerFilter> brFilters;
 
 	/**
 	 * The widget listener
 	 */
 	private List<ISelectionChangedListener> listeners;
 
 	/**
 	 * The labelProvider to use
 	 */
 	protected ILabelProvider delegatedLabelProvider;
 
 	/**
 	 * Label provider able to display lists
 	 */
 	protected ILabelProvider listLabelProvider;
 
 	/**
 	 * The mode of the button
 	 */
 	private ButtonsModeEnum button_mode = ButtonsModeEnum.BROWSE;
 
 	/**
 	 * The edited element
 	 */
 	protected EObject editedElement;
 
 	/**
 	 * The edited feature
 	 */
 	protected EStructuralFeature feature;
 
 	/**
 	 * The input for the choice of values
 	 */
 	protected Object input;
 
 	protected EList<?> result;
 
 	private EReference containingFeature;
 
 	/**
 	 * Default constructor
 	 * 
 	 * @param parent
 	 *            the parent widget
 	 */
 	public FlatReferencesTable(Composite parent) {
 		super(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		this.setLayout(layout);
 		selection = new Text(this, SWT.BORDER);
 		GridData selectionData = new GridData(GridData.FILL_HORIZONTAL);
		selectionData.widthHint = 400;
 		selection.setLayoutData(selectionData);
 		selection.setEditable(false);
 		EditingUtils.setEEFtype(selection, "eef::FlatReferencesTable::field");
 
 		editer = new Button(this, SWT.PUSH);
 		editer.setText(EEFRuntimeUIMessages.EObjectFlatComboViewer_add_button);
 
 		filters = new ArrayList<ViewerFilter>();
 		brFilters = new ArrayList<ViewerFilter>();
 		listeners = new ArrayList<ISelectionChangedListener>();
 
 		editer.addSelectionListener(getSelectionAdapter());
 		EditingUtils.setEEFtype(selection, "eef::FlatReferencesTable::browsebutton");
 
 	}
 
 	protected SelectionAdapter getSelectionAdapter() {
 
 		return new SelectionAdapter() {
 
 			/*
 			 * (non-Javadoc)
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse
 			 * .swt.events.SelectionEvent)
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				switch (button_mode) {
 					case BROWSE:
 						if (input instanceof ReferencesTableSettings) {
 							List currentValues = Arrays.asList(((ReferencesTableSettings)input).getValue());
 							Object choiceOfValues2 = ((ReferencesTableSettings)input).choiceOfValues(null);
 							List cloneOfValues = new ArrayList();
 							if (choiceOfValues2 instanceof List) {
 								cloneOfValues.addAll((List)choiceOfValues2);
 							} else {
 								cloneOfValues.add(choiceOfValues2);
 							}
 							EEFFeatureEditorDialog dialog = new EEFFeatureEditorDialog(
 									getParent().getShell(),
 									EEFRuntimeUIMessages.FlatReferencesTable_featureEditor_title,
 									delegatedLabelProvider, currentValues, cloneOfValues, false, true,
 									filters, brFilters);
 							int open = dialog.open();
 							if (open == Dialog.OK) {
 								selectionChanged(new StructuredSelection(dialog.getResult()));
 								refresh();
 							}
 						}
 						break;
 
 					default:
 						break;
 				}
 			}
 		};
 
 	}
 
 	/****************************************************************************************************************************************
 	 * Widget configuration
 	 ****************************************************************************************************************************************/
 
 	/**
 	 * Defines the labelProvider of the widget
 	 * 
 	 * @param provider
 	 *            the labelProvider to use
 	 */
 	public void setLabelProvider(ILabelProvider provider) {
 		this.delegatedLabelProvider = provider;
 		if (listLabelProvider == null) {
 			listLabelProvider = new ILabelProvider() {
 
 				public void addListener(ILabelProviderListener listener) {
 					delegatedLabelProvider.addListener(listener);
 				}
 
 				public void dispose() {
 					delegatedLabelProvider.dispose();
 
 				}
 
 				public Image getImage(Object element) {
 					return delegatedLabelProvider.getImage(element);
 				}
 
 				public String getText(Object element) {
 					if (element instanceof List) {
 						StringBuilder result = new StringBuilder(""); //$NON-NLS-1$
 						final List collec = (List)element;
 						if (collec.size() > 0) {
 							result.append(delegatedLabelProvider.getText(collec.get(0)));
 							if (collec.size() > 1) {
 								for (int i = 1; i < collec.size(); i++) {
 									result.append(", "); //$NON-NLS-1$
 									result.append(delegatedLabelProvider.getText(collec.get(i)));
 								}
 							}
 						}
 						return result.toString();
 					}
 					return delegatedLabelProvider.getText(element);
 				}
 
 				public boolean isLabelProperty(Object element, String property) {
 					return delegatedLabelProvider.isLabelProperty(element, property);
 				}
 
 				public void removeListener(ILabelProviderListener listener) {
 					delegatedLabelProvider.removeListener(listener);
 				}
 
 			};
 		}
 	}
 
 	/**
 	 * Defines the mode of the button
 	 * 
 	 * @param button_mode
 	 *            the mode to set
 	 */
 	public void setButtonMode(ButtonsModeEnum button_mode) {
 		this.button_mode = button_mode;
 	}
 
 	/**
 	 * @return the input
 	 */
 	public Object getInput() {
 		return input;
 	}
 
 	/**
 	 * Defines the input for the choice of values
 	 * 
 	 * @param input
 	 *            the input
 	 */
 	public void setInput(Object input) {
 		if (this.input != input) {
 			this.input = input;
 		}
 		refresh();
 	}
 
 	/**
 	 * Sets the given ID to the EMFComboViewer
 	 * 
 	 * @param id
 	 *            the ID to give
 	 */
 	public void setID(Object id) {
 		EditingUtils.setID(selection, id);
 		EditingUtils.setID(editer, id);
 	}
 
 	/**
 	 * @return the ID of the EObjectFlatComboViewer
 	 */
 	public Object getID() {
 		return EditingUtils.getID(selection);
 	}
 
 	/****************************************************************************************************************************************
 	 * Selection management
 	 ****************************************************************************************************************************************/
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
 	 * (org.eclipse.jface.viewers. ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
 	 * (org.eclipse.jface.viewers .ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		throw new UnsupportedOperationException(EEFRuntimeUIMessages.FlatReferencesTable_nothing_to_do);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse .jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection pSelection) {
 		throw new UnsupportedOperationException(EEFRuntimeUIMessages.FlatReferencesTable_nothing_to_do);
 	}
 
 	public void refresh() {
 		if (input instanceof ReferencesTableSettings) {
 			List<Object> values = Arrays.asList(((ReferencesTableSettings)input).getValue());
 			if (listLabelProvider != null) {
 				selection.setText(listLabelProvider.getText(values));
 			} else {
 				StringBuilder result = new StringBuilder(""); //$NON-NLS-1$
 				if (values.size() > 0) {
 					result.append(values.get(0).toString());
 					if (values.size() > 1) {
 						for (int i = 1; i < values.size(); i++) {
 							result.append(", "); //$NON-NLS-1$
 							result.append(values.get(i).toString());
 						}
 					}
 				}
 				selection.setText(result.toString());
 			}
 		} else
 			selection.setText(""); //$NON-NLS-1$
 	}
 
 	/**
 	 * The selection has changed
 	 * 
 	 * @param selection
 	 *            the new selection
 	 */
 	protected void selectionChanged(ISelection selection) {
 		if (listeners != null && !listeners.isEmpty()) {
 			for (ISelectionChangedListener nextListener : listeners) {
 				nextListener.selectionChanged(new SelectionChangedEvent(this, selection));
 			}
 		}
 	}
 
 	/****************************************************************************************************************************************
 	 * Filter management
 	 ****************************************************************************************************************************************/
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#addFilter
 	 * (org.eclipse.jface.viewers .ViewerFilter)
 	 */
 	public void addFilter(ViewerFilter filter) {
 		filters.add(filter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#removeFilter
 	 * (org.eclipse.jface.viewers .ViewerFilter)
 	 */
 	public void removeFilter(ViewerFilter filter) {
 		filters.remove(filter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#
 	 * addBusinessRuleFilter(org.eclipse. jface.viewers.ViewerFilter)
 	 */
 	public void addBusinessRuleFilter(ViewerFilter filter) {
 		brFilters.add(filter);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#
 	 * removeBusinessRuleFilter(org.eclipse .jface.viewers.ViewerFilter)
 	 */
 	public void removeBusinessRuleFilter(ViewerFilter filter) {
 		brFilters.remove(filter);
 	}
 
 	/**
 	 * Clear the list of static filters
 	 */
 	public void resetFilters() {
 		filters.clear();
 	}
 
 }
