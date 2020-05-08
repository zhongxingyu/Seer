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
 package org.eclipse.emf.eef.runtime.ui.viewers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
 import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionListener;
 import org.eclipse.emf.eef.runtime.api.parts.IFormPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.api.parts.ISWTPropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
 import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
 import org.eclipse.emf.eef.runtime.ui.utils.EEFRuntimeUIMessages;
 import org.eclipse.emf.eef.runtime.ui.viewers.filters.PropertiesEditionPartFilter;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.forms.IFormColors;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class PropertiesEditionViewer extends StructuredViewer {
 
 	/**
 	 * Graphical purpose fields
 	 */
 	private FormToolkit toolkit;
 
 	/**
 	 * TODO: sets this private 
 	 */
 	protected CTabFolder folder = null;
 
 	/**
 	 * TODO: sets this private 
 	 */
 	protected Composite control;
 
 	protected boolean dynamicTabHeader = true;
 
 	/**
 	 * The expected kind for the part.
 	 */
 	private int kind;
 
 	private List<ViewerFilter> filters;
 
 	private boolean initState = false;
 
 	protected ResourceSet allResources;
 
 	private ItemListener listener;
 
 	/**
 	 * TODO: sets this private 
 	 */
 	protected ScrolledComposite scroll;
 
 	/**
 	 * Create an Viewer for EEF properties editing in the given parent composite.
 	 * 
 	 * @param parent
 	 *            the parent control
 	 * @param style
 	 *            the SWT style bits
 	 * @param kind
 	 *            the kind of the part
 	 */
 	public PropertiesEditionViewer(Composite container, ResourceSet allResources, int style, int kind) {
 		control = new Composite(container, SWT.NONE);
 		control.setLayout(new FillLayout());
 		control.setLayoutData(new GridData(GridData.FILL_BOTH));
 		scroll = new ScrolledComposite(control, SWT.V_SCROLL | SWT.H_SCROLL);
 		folder = new CTabFolder(scroll, style);
 		folder.setSimple(false);
 		this.allResources = allResources;
 		this.kind = kind;
 		scroll.setContent(folder);
 		scroll.setExpandHorizontal(true);
 		scroll.setExpandVertical(true);
 		listener = new ItemListener();
 		folder.addSelectionListener(new SelectionAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 			 */
 			public void widgetSelected(SelectionEvent e) {
 				listener.updateControlListener();
 			}
 
 		});
 		control.addControlListener(listener);
 	}
 
 	/**
 	 * Create an Viewer for EEF properties editing in the given parent composite
 	 * 
 	 * @param parent
 	 *            the parent control
 	 * @param kind
 	 *            the kind of the part
 	 */
 	public PropertiesEditionViewer(Composite container, ResourceSet allResources, int kind) {
 		this(container, allResources, SWT.BORDER, kind);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#getControl()
 	 */
 	public Control getControl() {
 		return control;
 	}
 
 	/**
 	 * @return the toolkit
 	 */
 	public FormToolkit getToolkit() {
 		return toolkit;
 	}
 
 	/**
 	 * @param toolkit
 	 *            the toolkit to set
 	 */
 	public void setToolkit(FormToolkit toolkit) {
 		this.toolkit = toolkit;
 		toolkit.adapt(control);
 		toolkit.adapt(scroll);
 		toolkit.adapt(folder);
 	}
 
 	/**
 	 * @return <code>true</code> if the viewer is initializing
 	 */
 	public boolean isInitializing() {
 		return initState;
 	}
 
 	/**
 	 * Defines if the tab headers are visible when there is more than 1 tab.
 	 * 
 	 * @param dynamic
 	 *            the dynamic tab header
 	 */
 	public void setDynamicTabHeader(boolean dynamic) {
 		dynamicTabHeader = dynamic;
 	}
 
 	/**
 	 * @param listener
 	 *            the properties listener to add
 	 */
 	public void addPropertiesListener(IPropertiesEditionListener listener) {
 		if (getContentProvider() != null)
 			((PropertiesEditionContentProvider)getContentProvider()).addPropertiesListener(listener);
 	}
 
 	/**
 	 * Returns the root element.
 	 * <p>
 	 * The default implementation of this framework method forwards to <code>getInput</code>. Override if the
 	 * root element is different from the viewer's input element.
 	 * </p>
 	 * 
 	 * @return the root element, or <code>null</code> if none
 	 */
 	protected Object getRoot() {
 		return getEObjectFromInput();
 	}
 
 	/**
 	 * @return
 	 */
 	public EObject getEObjectFromInput() {
 		EObject result = null;
 		if (getInput() instanceof EObject) {
 			result = (EObject)getInput();
 		} else if (getInput() instanceof PropertiesEditingContext) {
 			result = ((PropertiesEditingContext)getInput()).getEObject();
 		}
 		return result;
 	}
 
 	/*
 	 * =============================== Filters management ===============================
 	 */
 
 	/**
 	 * Returns this viewer's filters.
 	 * 
 	 * @return an array of viewer filters
 	 * @see StructuredViewer#setFilters(ViewerFilter[])
 	 */
 	public ViewerFilter[] getFilters() {
 		if (filters == null) {
 			return new ViewerFilter[0];
 		}
 		ViewerFilter[] result = new ViewerFilter[filters.size()];
 		filters.toArray(result);
 		return result;
 	}
 
 	/**
 	 * Adds the given filter to this viewer, and triggers refiltering and resorting of the elements. If you
 	 * want to add more than one filter consider using {@link StructuredViewer#setFilters(ViewerFilter[])}.
 	 * 
 	 * @param filter
 	 *            a viewer filter
 	 * @see StructuredViewer#setFilters(ViewerFilter[])
 	 */
 	public void addFilter(ViewerFilter filter) {
 		if (filters == null) {
 			filters = new ArrayList<ViewerFilter>();
 		}
 		filters.add(filter);
 		refresh();
 	}
 
 	/**
 	 * Removes the given filter from this viewer, and triggers refiltering and resorting of the elements if
 	 * required. Has no effect if the identical filter is not registered. If you want to remove more than one
 	 * filter consider using {@link StructuredViewer#setFilters(ViewerFilter[])}.
 	 * 
 	 * @param filter
 	 *            a viewer filter
 	 * @see StructuredViewer#setFilters(ViewerFilter[])
 	 */
 	public void removeFilter(ViewerFilter filter) {
 		Assert.isNotNull(filter);
 		if (filters != null) {
 			// Note: can't use List.remove(Object). Use identity comparison
 			// instead.
 			for (Iterator<ViewerFilter> i = filters.iterator(); i.hasNext();) {
 				Object o = i.next();
 				if (o == filter) {
 					i.remove();
 					refresh();
 					if (filters.size() == 0) {
 						filters = null;
 					}
 					return;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the filters, replacing any previous filters, and triggers refiltering and resorting of the
 	 * elements.
 	 * 
 	 * @param filters
 	 *            an array of viewer filters
 	 * @since 3.3
 	 */
 	public void setFilters(ViewerFilter[] filters) {
 		if (filters.length == 0) {
 			resetFilters();
 		} else {
 			this.filters = new ArrayList<ViewerFilter>(Arrays.asList(filters));
 			refresh();
 		}
 	}
 
 	/**
 	 * Discards this viewer's filters and triggers refiltering and resorting of the elements.
 	 */
 	public void resetFilters() {
 		if (filters != null) {
 			filters = null;
 			refresh();
 		}
 	}
 
 	/**
 	 * Returns the result of running the given elements through the filters.
 	 * 
 	 * @param elements
 	 *            the elements to filter
 	 * @return only the elements which all filters accept
 	 */
 	private boolean selectPart(String key, IPropertiesEditionPart propertiesEditionPart) {
 		boolean result = true;
 		if (filters != null) {
 			boolean select = true;
 			for (int j = 0; j < filters.size(); j++) {
 				ViewerFilter viewerFilter = filters.get(j);
 				if (viewerFilter instanceof PropertiesEditionPartFilter) {
 					select = viewerFilter.select(this, key, propertiesEditionPart);
 					if (!select) {
 						result = false;
 						break;
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * ========================== Component edition management ==========================
 	 */
 
 	/**
 	 * Compute the edition command to perform to update the model
 	 * 
 	 * @param editingDomain
 	 *            the editingDomain where the command have to be performed
 	 * @return the command to perform
 	 */
 	// public CompoundCommand getPropertiesEditionCommand(EditingDomain
 	// editingDomain) {
 	// if (getContentProvider() != null)
 	// return ((PropertiesEditionContentProvider)getContentProvider())
 	// .getPropertiesEditionCommand(editingDomain);
 	// return null;
 	// }
 
 	/**
 	 * Update and return the given EObject
 	 * 
 	 * @param eObject
 	 *            the EObject to update
 	 * @return the updated EObject
 	 */
 	// public EObject getPropertiesEditionObject(EObject eObject) {
 	// if (getContentProvider() != null)
 	// return ((PropertiesEditionContentProvider)getContentProvider())
 	// .getPropertiesEditionObject(eObject);
 	// return null;
 	// }
 
 	/**
 	 * Validate the model and return the resulting Diagnostic
 	 * 
 	 * @param event
 	 *            the event triggering the validation
 	 * @return the resulting value
 	 */
 	public Diagnostic validateValue(IPropertiesEditionEvent event) {
 		if (getContentProvider() != null)
 			return ((PropertiesEditionContentProvider)getContentProvider()).validateValue(event);
 		return null;
 	}
 
 	/*
 	 * ============================== Selection management ==============================
 	 */
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#getSelection()
 	 */
 	public ISelection getSelection() {
 		Object root = getRoot();
 		if (root != null)
 			return new StructuredSelection(root);
 		return new StructuredSelection();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
 	 */
 	public void setSelection(ISelection selection, boolean reveal) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List, boolean)
 	 */
 	protected void setSelectionToWidget(List l, boolean reveal) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
 	 */
 	protected List getSelectionFromWidget() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/*
 	 * ============================== Graphical management ==============================
 	 */
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
 	 */
 	protected void internalRefresh(Object element) {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object, java.lang.Object)
 	 */
 	protected void inputChanged(Object input, Object oldInput) {
 		super.inputChanged(input, oldInput);
 		initControl();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
 	 */
 	public void reveal(Object element) {
 		// Nothing to do
 	}
 
 	/**
 	 * @param propertiesEditionContentProvider
 	 * @return
 	 */
 	public CompositePropertiesEditionPart getSelectedPart() {
 		PropertiesEditionContentProvider propertiesEditionContentProvider = (PropertiesEditionContentProvider)getContentProvider();
 		String selectedPartTitle = folder.getSelection().getText();
 		return (CompositePropertiesEditionPart)propertiesEditionContentProvider.getPropertiesEditionPart(
 				kind, selectedPartTitle);
 	}
 
 	/**
 	 * Create the control content
 	 */
 	protected void initControl() {
 		if (getContentProvider() != null) {
 			PropertiesEditionContentProvider propertiesEditionProvider = (PropertiesEditionContentProvider)getContentProvider();
 			// first set initState to true to not handle changes yet
 			initState = true;
 			String[] partsList = propertiesEditionProvider.partsList();
 			initTabbedControl(propertiesEditionProvider, partsList);
 			initState = false;
 		}
 	}
 
 	/**
 	 * Initialize the control of the viewer in "Tabbed case". Defines one tab for each PropertiesEditionPart
 	 * of the component.
 	 * 
 	 * @param propertiesEditionProvider
 	 * @param partsList
 	 */
 	protected void initTabbedControl(PropertiesEditionContentProvider propertiesEditionProvider,
 			String[] partsList) {
 		resetTab();
 		List<String> selectedParts = new ArrayList<String>();
 		if (kind == 1) {
 			toolkit.adapt(folder, true, true);
 			toolkit.getColors().initializeSectionToolBarColors();
 			folder.setSelectionBackground(
 					new Color[] {toolkit.getColors().getColor(IFormColors.H_GRADIENT_START), toolkit.getColors().getColor(IFormColors.H_GRADIENT_END)}, 
 					new int[] {50}, 
 					true);
 			folder.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
 			folder.setSelectionForeground(toolkit.getColors().getColor(IFormColors.TITLE));
 		}
 		for (int i = 0; i < partsList.length; i++) {
 			String nextComponentKey = partsList[i];
 			IPropertiesEditionPart part = propertiesEditionProvider.getPropertiesEditionPart(kind,
 					nextComponentKey);
 			if (selectPart(nextComponentKey, part)) {
 				selectedParts.add(nextComponentKey);
 				addPartTab(propertiesEditionProvider, part, nextComponentKey);
 			}
 		}
 		if (dynamicTabHeader) {
 			if (selectedParts.size() > 1)
 				folder.setTabHeight(SWT.DEFAULT);
 			else
 				folder.setTabHeight(0);
 		}
 		folder.setSelection(0);
 	}
 
 	/**
 	 * Create a new tab in the folder for a given PropertiesEditionPart.
 	 * 
 	 * @param propertiesEditionProvider
 	 *            the EEF properties provider
 	 * @param key
 	 *            the key of the part
 	 * @param tabText
 	 *            the title of the tab
 	 */
 	private void addPartTab(PropertiesEditionContentProvider propertiesEditionProvider,
 			IPropertiesEditionPart part, String key) {
 		Composite editComposite = null;
 		if (part instanceof ISWTPropertiesEditionPart)
 			editComposite = ((ISWTPropertiesEditionPart)part).createFigure(folder);
 		if (part instanceof IFormPropertiesEditionPart) {
 			Assert.isNotNull(toolkit, EEFRuntimeUIMessages.PropertiesEditionViewer_widget_factory_not_defined);
 			editComposite = ((IFormPropertiesEditionPart)part).createFigure(folder, toolkit);
 		}
 		if (editComposite != null) {
 			if (allResources == null && getEObjectFromInput() != null)
 				propertiesEditionProvider.initPart(propertiesEditionProvider.translatePart(key), kind,
 						((EObject)getEObjectFromInput()));
 			else
 				propertiesEditionProvider.initPart(propertiesEditionProvider.translatePart(key), kind,
 						((EObject)getEObjectFromInput()), allResources);
 		} else
 			editComposite = new Composite(folder, SWT.NONE);
 		CTabItem tab = new CTabItem(folder, SWT.NONE);
 		tab.setControl(editComposite);
 		tab.setText(key);
 	}
 
 	/**
 	 * Reset all the tabs of the folder.
 	 */
 	private void resetTab() {
 		if (folder.getItemCount() > 0) {
 			CTabItem[] items = folder.getItems();
 			for (int i = 0; i < items.length; i++) {
 				CTabItem cTabItem = items[i];
 				cTabItem.dispose();
 			}
 		}
 	}
 
 	/*
 	 * ================================= Search methods =================================
 	 */
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
 	 */
 	protected Widget doFindInputItem(Object element) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
 	 */
 	protected Widget doFindItem(Object element) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
 	 *      java.lang.Object, boolean)
 	 */
 	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * ================================= Scroll management =================================
 	 */
 	/**
 	 * update the scroll composite size
 	 */
 	private void updateScrollSize() {
 		if (folder.getSelection() != null && folder.getSelection().getControl() != null) {
 			scroll.setMinSize(folder.getSelection().getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		}
 	}
 
 	private class ItemListener implements ControlListener {
 
 		Control listenedControl = null;
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
 		 */
 		public void controlMoved(ControlEvent e) {
 			// Nothing to do
 
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
 		 */
 		public void controlResized(ControlEvent e) {
 			updateScrollSize();
 		}
 
 		/**
 		 * update listeners managing scroll composite size
 		 */
 		public void updateControlListener() {
 			if (listener.listenedControl != null) {
 				listener.listenedControl.removeControlListener(listener);
 			}
 			Control control2 = folder.getSelection().getControl();
 			if (control2 != null) {
 				listener.listenedControl = control2;
 				control2.addControlListener(listener);
 			}
 
 		}
 
 	}
 
 }
