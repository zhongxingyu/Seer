 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
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
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 import org.eclipse.emf.eef.runtime.impl.utils.EMFListEditUtil;
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
 	 * Virtual list for widget
 	 */
 	protected EMFListEditUtil viewsEditUtil;
 
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
 
 	/**
 	 * Default contructor
 	 * @param parent the parent widget
 	 */
 	public FlatReferencesTable(Composite parent) {
 		super(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		this.setLayout(layout);
 		selection = new Text(this, SWT.BORDER);
 		GridData selectionData = new GridData(GridData.FILL_HORIZONTAL);
 		selection.setLayoutData(selectionData);
 		selection.setEditable(false);
 		editer = new Button(this, SWT.PUSH);
 		editer.setText("..."); //$NON-NLS-1$
 
 		filters = new ArrayList<ViewerFilter>();
 		brFilters = new ArrayList<ViewerFilter>();
 		listeners = new ArrayList<ISelectionChangedListener>();
 
 		// ADD EXTENSION: CNO
 		editer.addSelectionListener(getSelectionAdapter());
 
 	}
 
 	protected SelectionAdapter getSelectionAdapter() {
 
 		return new SelectionAdapter() {
 
 			/*
 			 * (non-Javadoc)
 			 * @see
 			 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				switch (button_mode) {
 				case BROWSE:
 					EEFFeatureEditorDialog dialog = new EEFFeatureEditorDialog(
 							getParent().getShell(),	delegatedLabelProvider,	editedElement,
							feature.getEType(),	viewsEditUtil.getVirtualList(),	"Edit Feature",
 							getChoiceOfValues(), false,	true, filters, brFilters);
 					dialog.open();
 					EList<?> newValues = dialog.getResult();
 					if (newValues != null)
 						applyDiff(viewsEditUtil, newValues);
 					selectionChanged(new StructuredSelection(viewsEditUtil));
 					refresh();
 					break;
 
 				default:
 					break;
 				}
 			}
 		};
 
 	}
 
 	/**
 	 * @return
 	 */
 	private List<?> getChoiceOfValues() {
 		List result;
 		// Initializing result with the input
 		if (input instanceof List)
 			result = new ArrayList((List)input);
 		else if (input instanceof ResourceSet)
 			result = new ArrayList(EEFUtils.asList(	EcoreUtil.getAllContents((ResourceSet)input, true)));
 		else if (input instanceof Resource)
 			result = new ArrayList(EEFUtils.asList(	EcoreUtil.getAllContents((Resource)input, true)));
 		else
 			result = new ArrayList();
 		return result;
 	}
 
 
 	/**
 	 * @param list
 	 * @param newValues
 	 */
 	protected void applyDiff(EMFListEditUtil list, List newValues) {
 		List virtualList = new ArrayList(list.getVirtualList());
 		if (newValues != null) {
 			for (Object element : virtualList) {
 				if (element instanceof EObject) {
 					if (!newValues.contains((EObject) element)) {
 						list.removeElement((EObject) element);
 					}
 				}
 			}
 			for (Object element : newValues) {
 				if (element instanceof EObject) {
 					if (!virtualList.contains(element))
 						list.addElement((EObject) element);
 				}
 			}
 		}
 	}
 
 
 	/****************************************************************************************************************************************
 	 * 
 	 * Widget configuration
 	 * 
 	 ****************************************************************************************************************************************/
 
 	/**
 	 * Defines the labelProvider of the widget
 	 * @param provider the labelProvider to use
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
 						StringBuilder result = new StringBuilder("");
 						final List collec = (List)element;
 						if (collec.size() > 0) {
 							result.append(delegatedLabelProvider.getText(collec.get(0)));
 							if (collec.size() > 1) {
 								for (int i = 1; i < collec.size(); i++) {
 									result.append(", ");
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
 	 * @param button_mode the mode to set
 	 */
 	public void setButtonMode(ButtonsModeEnum button_mode) {
 		this.button_mode = button_mode;
 	}
 
 	/**
 	 * Defines the input for the choice of values
 	 * @param input the input
 	 */
 	public void setInput(Object input) {
 		if (this.input != input) {
 			this.input = input;
 		}
 	}
 
 	/**
 	 * Initialize the widget
 	 * @param current the editedElement 
 	 * @param editedFeature the feature to edit
 	 * @param containingFeature  the containing feature in "rebound" case
 	 */
 	public void initComponent(EObject current, EReference containingFeature, EReference editedFeature) {
 		this.feature = editedFeature;
 		if (containingFeature != null)
 			viewsEditUtil = new EMFListEditUtil(current, containingFeature, editedFeature);
 		else
 			viewsEditUtil = new EMFListEditUtil(current, editedFeature);
 		refresh();
 	}
 
 	/**
 	 * Update this component with new values
 	 * @param newValue the new values
 	 */
 	public void updateComponent(EObject newValue) {
 		if(viewsEditUtil != null){
 			viewsEditUtil.reinit(newValue);
 			refresh();
 		}
 	}
 
 
 
 	/****************************************************************************************************************************************
 	 * 
 	 * Selection management
 	 * 
 	 ****************************************************************************************************************************************/
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		// TODO: what this method is supposed to do ???
 		throw new UnsupportedOperationException("Nothing to do for the moment ...");
 		///		if (editedElement != null)
 		//			return new StructuredSelection(editedElement);
 		//		return new StructuredSelection(Collections.EMPTY_LIST);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection pSelection) {
 		// TODO: what this method is supposed to do ???
 		throw new UnsupportedOperationException("Nothing to do for the moment ...");
 		//		if (pSelection instanceof StructuredSelection && ((StructuredSelection)pSelection).getFirstElement() instanceof EObject) {
 		//			this.editedElement = (EObject) ((StructuredSelection)pSelection).getFirstElement();
 		//			selectedElements = (List)editedElement.eGet(feature);
 		//			if (listLabelProvider != null)
 		//				selection.setText(listLabelProvider.getText(selectedElements));
 		//			else {						
 		//				StringBuilder result = new StringBuilder("");
 		//				final List collec = (List)selectedElements;
 		//				if (collec.size() > 0) {
 		//					result.append(collec.get(0).toString());
 		//					if (collec.size() > 1) {
 		//						for (int i = 1; i < collec.size(); i++) {
 		//							result.append(", ");
 		//							result.append(collec.get(i).toString());
 		//						}
 		//					}
 		//				}
 		//				selection.setText(result.toString());
 		//			} 
 		//		} else
 		//			selection.setText(""); //$NON-NLS-1$
 	}
 
 	public void refresh() {
 		if (viewsEditUtil != null) { 
 			if (listLabelProvider != null)
 				selection.setText(listLabelProvider.getText(viewsEditUtil.getVirtualList()));
 			else {						
 				StringBuilder result = new StringBuilder("");
 				final List collec = viewsEditUtil.getVirtualList();
 				if (collec.size() > 0) {
 					result.append(collec.get(0).toString());
 					if (collec.size() > 1) {
 						for (int i = 1; i < collec.size(); i++) {
 							result.append(", ");
 							result.append(collec.get(i).toString());
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
 	 * @param selection the new selection
 	 */
 	protected void selectionChanged(ISelection selection) {
 		if (listeners != null && !listeners.isEmpty()) {
 			for (ISelectionChangedListener nextListener : listeners) {
 				nextListener.selectionChanged(new SelectionChangedEvent(this, selection));
 			}
 		}
 	}
 
 	/****************************************************************************************************************************************
 	 * 
 	 * Filter management
 	 * 
 	 ****************************************************************************************************************************************/
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#addFilter(org.eclipse.jface.viewers.ViewerFilter)
 	 */
 	public void addFilter(ViewerFilter filter) {
 		filters.add(filter);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#removeFilter(org.eclipse.jface.viewers.ViewerFilter)
 	 */
 	public void removeFilter(ViewerFilter filter) {
 		filters.remove(filter);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#addBusinessRuleFilter(org.eclipse.jface.viewers.ViewerFilter)
 	 */
 	public void addBusinessRuleFilter(ViewerFilter filter) {
 		brFilters.add(filter);
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.eef.runtime.ui.widgets.IPropertiesFilteredWidget#removeBusinessRuleFilter(org.eclipse.jface.viewers.ViewerFilter)
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
 
 	public List getElementsToAdd() {
 		return viewsEditUtil.getElementsToAdd();
 	}
 
 	public List getElementsToRemove() {
 		return viewsEditUtil.getElementsToRemove();
 	}
 	public List getVirtualList() {
 		return viewsEditUtil.getVirtualList();
 	}
 
 	public boolean virtualListContains(EObject element) {
 		return viewsEditUtil.contains(element);
 	}
 
 }
