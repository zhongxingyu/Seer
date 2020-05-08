 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.IntrospectionException;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
 import org.eclipse.core.databinding.observable.set.ISetChangeListener;
 import org.eclipse.core.databinding.observable.set.SetChangeEvent;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
 import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.internal.databinding.viewers.SelectionProviderMultipleSelectionObservableList;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 import org.eclipse.riena.ui.ridgets.tree.IObservableTreeModel;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Ridget for SWT {@link Tree} widgets.
  */
 public class TreeRidget extends AbstractSelectableRidget implements ITreeRidget {
 
 	private final SelectionListener selectionTypeEnforcer;
 	private final DoubleClickForwarder doubleClickForwarder;
 	private final Queue<ExpansionCommand> expansionStack;
 
 	private Collection<IActionListener> doubleClickListeners;
 	private DataBindingContext dbc;
 	private TreeViewer viewer;
 
 	/*
 	 * The original array of elements given as input to the ridget via the
 	 * #bindToModel method. The ridget however works with the copy (treeRoots)
 	 * in order to be independend of modification to the original array.
 	 * 
 	 * Calling #updateFromModel will synchronize the treeRoots array with the
 	 * model array.
 	 */
 	private Object[] model;
 	private Object[] treeRoots;
 	private Class<? extends Object> treeElementClass;
 	private String childrenAccessor;
 	private String parentAccessor;
 	private String[] valueAccessors;
 	private String[] columnHeaders;
 	private boolean showRoots = true;
 
 	public TreeRidget() {
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 		doubleClickForwarder = new DoubleClickForwarder();
 		expansionStack = new LinkedList<ExpansionCommand>();
 	}
 
 	@Override
 	protected void bindUIControl() {
 		Tree control = getUIControl();
 		if (control != null && treeRoots != null) {
 			// TODO [ev] reproduce in a snippet and file a bug
 			// Bug workaround: deselect pre-existing selection in tree.
 			// The tree viewer tries to preserve the selection in the tree.
 			// However we have just put new content into it, so the "preserve
 			// selection" code will NPE
 			control.deselectAll();
 			bindToViewer(control);
 			bindToSelection();
 			control.addSelectionListener(selectionTypeEnforcer);
 			control.addMouseListener(doubleClickForwarder);
 			updateExpansionState();
 			applyTableColumnHeaders(control);
 		}
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, Tree.class);
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		if (viewer != null) {
 			Object[] elements = viewer.getExpandedElements();
 			ExpansionCommand cmd = new ExpansionCommand(ExpansionState.RESTORE, elements);
 			expansionStack.add(cmd);
 		}
 		if (dbc != null) {
 			dbc.dispose();
 			dbc = null;
 		}
 		Tree control = getUIControl();
 		if (control != null) {
 			control.removeSelectionListener(selectionTypeEnforcer);
 			control.removeMouseListener(doubleClickForwarder);
 		}
 		viewer = null;
 	}
 
 	@Override
 	protected final List<?> getRowObservables() {
 		List<?> result = null;
 		if (viewer != null) {
 			ObservableListTreeContentProvider cp = (ObservableListTreeContentProvider) viewer.getContentProvider();
 			result = new ArrayList<Object>(cp.getKnownElements());
 		}
 		return result;
 	}
 
 	protected void bindToModel(Object[] treeRoots, Class<? extends Object> treeElementClass, String childrenAccessor,
 			String parentAccessor, String[] valueAccessors, String[] columnHeaders) {
 		Assert.isNotNull(treeRoots);
 		Assert.isLegal(treeRoots.length > 0, "treeRoots must have at least one entry"); //$NON-NLS-1$
 		Assert.isNotNull(treeElementClass);
 		Assert.isNotNull(childrenAccessor);
 		Assert.isNotNull(parentAccessor);
 		Assert.isNotNull(valueAccessors);
 		Assert.isLegal(valueAccessors.length > 0, "valueAccessors must have at least one entry"); //$NON-NLS-1$
 		if (columnHeaders != null) {
 			String msg = "Mismatch between number of valueAccessors and columnHeaders"; //$NON-NLS-1$
 			Assert.isLegal(valueAccessors.length == columnHeaders.length, msg);
 		}
 
 		unbindUIControl();
 
 		this.model = treeRoots;
 		this.treeRoots = new Object[model.length];
 		System.arraycopy(model, 0, this.treeRoots, 0, this.treeRoots.length);
 		this.treeElementClass = treeElementClass;
 		this.childrenAccessor = childrenAccessor;
 		this.parentAccessor = parentAccessor;
 		this.valueAccessors = new String[valueAccessors.length];
 		System.arraycopy(valueAccessors, 0, this.valueAccessors, 0, this.valueAccessors.length);
 		if (columnHeaders != null) {
 			this.columnHeaders = new String[columnHeaders.length];
 			System.arraycopy(columnHeaders, 0, this.columnHeaders, 0, this.columnHeaders.length);
 		} else {
 			this.columnHeaders = null;
 		}
 
 		expansionStack.clear();
 		if (treeRoots.length == 1) {
 			ExpansionCommand cmd = new ExpansionCommand(ExpansionState.EXPAND, treeRoots[0]);
 			expansionStack.add(cmd);
 		}
 
 		bindUIControl();
 	}
 
 	/**
 	 * Returns the TreeViewer instance used by this ridget or null.
 	 */
 	protected final TreeViewer getViewer() {
 		return viewer;
 	}
 
 	// public methods
 	// ///////////////
 
 	@Override
 	public Tree getUIControl() {
 		return (Tree) super.getUIControl();
 	}
 
 	public void addDoubleClickListener(IActionListener listener) {
 		Assert.isNotNull(listener, "listener is null"); //$NON-NLS-1$
 		if (doubleClickListeners == null) {
 			doubleClickListeners = new ArrayList<IActionListener>();
 		}
 		doubleClickListeners.add(listener);
 	}
 
 	/**
 	 * @deprecated see
 	 *             {@link #bindToModel(Object[], Class, String, String, String)}
 	 */
 	public void bindToModel(IObservableTreeModel observableTreeModel) {
 		throw new UnsupportedOperationException("deprecated"); //$NON-NLS-1$
 	}
 
 	public void bindToModel(Object[] treeRoots, Class<? extends Object> treeElementClass, String childrenAccessor,
 			String parentAccessor, String valueAccessor) {
 		String[] valueAccessors = new String[] { valueAccessor };
 		this.bindToModel(treeRoots, treeElementClass, childrenAccessor, parentAccessor, valueAccessors, null);
 	}
 
 	/** @deprecated */
 	public void collapse(org.eclipse.riena.ui.ridgets.tree.ITreeNode node) {
 		collapse((Object) node);
 	}
 
 	public void collapse(Object element) {
 		ExpansionCommand cmd = new ExpansionCommand(ExpansionState.COLLAPSE, element);
 		expansionStack.add(cmd);
 		updateExpansionState();
 	}
 
 	public void collapseTree() {
 		ExpansionCommand cmd = new ExpansionCommand(ExpansionState.FULLY_COLLAPSE, null);
 		expansionStack.add(cmd);
 		updateExpansionState();
 	}
 
 	/** @deprecated */
 	public void expand(org.eclipse.riena.ui.ridgets.tree.ITreeNode node) {
 		expand((Object) node);
 	}
 
 	public void expand(Object element) {
 		ExpansionCommand cmd = new ExpansionCommand(ExpansionState.EXPAND, element);
 		expansionStack.add(cmd);
 		updateExpansionState();
 	}
 
 	public void expandTree() {
 		ExpansionCommand cmd = new ExpansionCommand(ExpansionState.FULLY_EXPAND, null);
 		expansionStack.add(cmd);
 		updateExpansionState();
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public IObservableTreeModel getRidgetObservable() {
 		throw new UnsupportedOperationException("deprecated"); //$NON-NLS-1$
 	}
 
 	public void removeDoubleClickListener(IActionListener listener) {
 		if (doubleClickListeners != null) {
 			doubleClickListeners.remove(listener);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * This implementation will try to expand the path to the give option, to
 	 * ensure that the corresponding tree element exists.
 	 */
 	@Override
 	public boolean containsOption(Object option) {
 		reveal(new Object[] { option });
 		return super.containsOption(option);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * For each selection candidate in the List <tt>newSelection</tt>, this
 	 * implementation will try to expand the path to the corresponding tree
 	 * node, to ensure that the corresponding tree element is selectable.
 	 */
 	@Override
 	public final void setSelection(List<?> newSelection) {
 		reveal(newSelection.toArray());
 		super.setSelection(newSelection);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * For each selection candidate in the List <tt>newSelection</tt>, this
 	 * implementation will try to expand the path to the corresponding tree
 	 * node, to ensure that the corresponding tree element is selectable.
 	 */
 	@Override
 	public final void setSelection(Object candidate) {
 		if (candidate != null) {
 			reveal(new Object[] { candidate });
 		}
 		super.setSelection(candidate);
 	}
 
 	@Override
 	public void updateFromModel() {
 		treeRoots = new Object[model.length];
 		System.arraycopy(model, 0, treeRoots, 0, treeRoots.length);
 		if (viewer != null) {
 			Object[] expandedElements = viewer.getExpandedElements();
 			viewer.setInput(treeRoots);
 			viewer.setExpandedElements(expandedElements);
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void applyTableColumnHeaders(Tree control) {
 		boolean headersVisible = columnHeaders != null;
 		control.setHeaderVisible(headersVisible);
 		if (headersVisible) {
 			TreeColumn[] columns = control.getColumns();
 			for (int i = 0; i < columns.length; i++) {
 				String columnHeader = ""; //$NON-NLS-1$
 				if (i < columnHeaders.length && columnHeaders[i] != null) {
 					columnHeader = columnHeaders[i];
 				}
 				columns[i].setText(columnHeader);
 			}
 		}
 	}
 
 	/**
 	 * @deprecated temporary - TODO [ev] keep or remove?
 	 */
 	public void setRootsVisible(boolean showRoots) {
 		if (this.showRoots != showRoots) {
 			this.showRoots = showRoots;
 		}
 	}
 
 	public boolean isRootVisible() {
 		return showRoots;
 	}
 
 	/**
 	 * Initialize databining for tree viewer.
 	 */
 	private void bindToViewer(final Tree control) {
 		viewer = new TreeViewer(control);
 		// content
 		Realm realm = Realm.getDefault();
 		// how to create a list of children from a given object (expansion)
 		IObservableFactory listFactory = BeansObservables.listFactory(realm, childrenAccessor, treeElementClass);
 		// how to get the parent from a give object
 		TreeStructureAdvisor structureAdvisor = new GenericTreeStructureAdvisor(parentAccessor, treeElementClass);
 		// how to create the content/structure for the tree
 		TreeContentProvider viewerCP = new TreeContentProvider(listFactory, structureAdvisor);
 		// refresh icons on addition / removal
 		viewerCP.getKnownElements().addSetChangeListener(new TreeContentChangeListener(viewerCP, structureAdvisor));
 		viewer.setContentProvider(viewerCP);
 		// labels
 		IObservableMap[] attributeMap = BeansObservables.observeMaps(viewerCP.getKnownElements(), treeElementClass,
 				valueAccessors);
 		ILabelProvider viewerLP = new TreeRidgetLabelProvider(viewer, attributeMap);
 		viewer.setLabelProvider(viewerLP);
 		// input
 		if (showRoots) {
 			viewer.setInput(treeRoots);
 		} else {
 			// TODO [ev] productize if it works for Thorsten
 			final Object root0 = treeRoots[0];
 			final String accessor = "get" + capitalize(childrenAccessor);
 			List root0Children = ReflectionUtils.invoke(root0, accessor);
 			final List inputList = new ArrayList(root0Children);
 			viewer.setInput(inputList);
 			PropertyChangeListener listener = new PropertyChangeListener() {
 				private String ACCESSOR = childrenAccessor.toUpperCase();
 
 				public void propertyChange(PropertyChangeEvent evt) {
 					if (evt.getPropertyName().toUpperCase().endsWith(ACCESSOR)) {
 						List root0Children = ReflectionUtils.invoke(root0, accessor);
 						inputList.clear();
 						for (Object child : root0Children) {
 							inputList.add(child);
 						}
 						viewer.setInput(inputList);
 						viewer.refresh(); // TODO [tsc]
 					}
 				}
 			};
 			ReflectionUtils.invoke(root0, "addPropertyChangeListener", listener);
 		}
 	}
 
 	private String capitalize(String name) {
 		String result = name.substring(0, 1).toUpperCase();
 		if (name.length() > 1) {
 			result += name.substring(1);
 		}
 		return result;
 	}
 
 	/**
 	 * Initialize databinding related to selection handling (single/multi).
 	 */
 	private void bindToSelection() {
 		StructuredSelection currentSelection = new StructuredSelection(getSelection());
 
 		dbc = new DataBindingContext();
 		IObservableValue viewerSelection = ViewersObservables.observeSingleSelection(viewer);
 		dbc.bindValue(viewerSelection, getSingleSelectionObservable(), new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
 		IObservableList viewerSelections = new SelectionProviderMultipleSelectionObservableList(dbc
 				.getValidationRealm(), viewer, Object.class);
 		dbc.bindList(viewerSelections, getMultiSelectionObservable(), new UpdateListStrategy(
 				UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_UPDATE));
 
 		viewer.setSelection(currentSelection);
 	}
 
 	/**
 	 * Expand tree paths to candidates before selecting them. This ensures the
 	 * tree items to the candidates are created and the candidates become
 	 * "known elements" (if they exist).
 	 */
 	private void reveal(Object[] candidates) {
 		if (viewer != null) {
 			Control control = viewer.getControl();
 			control.setRedraw(false);
 			try {
 				for (Object candidate : candidates) {
 					viewer.expandToLevel(candidate, 0);
 				}
 			} finally {
 				control.setRedraw(true);
 			}
 		}
 	}
 
 	/**
 	 * Updates the expand / collapse state of the viewers model, based on a FIFO
 	 * queue of {@link ExpansionCommand}s.
 	 */
 	private void updateExpansionState() {
 		if (viewer != null) {
 			viewer.getControl().setRedraw(false);
 			try {
 				while (!expansionStack.isEmpty()) {
 					ExpansionCommand cmd = expansionStack.remove();
 					ExpansionState state = cmd.state;
 					if (state == ExpansionState.FULLY_COLLAPSE) {
 						Object[] expanded = viewer.getExpandedElements();
 						viewer.collapseAll();
 						for (Object wasExpanded : expanded) {
 							viewer.update(wasExpanded, null); // update icon
 						}
 					} else if (state == ExpansionState.FULLY_EXPAND) {
 						viewer.expandAll();
 						viewer.refresh(); // update all icons
 					} else if (state == ExpansionState.COLLAPSE) {
 						viewer.collapseToLevel(cmd.element, 1);
 						viewer.update(cmd.element, null); // update icon
 					} else if (state == ExpansionState.EXPAND) {
 						viewer.expandToLevel(cmd.element, 1);
 						viewer.update(cmd.element, null); // update icon
 					} else if (state == ExpansionState.RESTORE) {
 						Object[] elements = (Object[]) cmd.element;
 						viewer.setExpandedElements(elements);
 					} else {
 						String errorMsg = "unknown expansion state: " + state; //$NON-NLS-1$
 						throw new IllegalStateException(errorMsg);
 					}
 				}
 			} finally {
 				viewer.getControl().setRedraw(true);
 			}
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Enumeration with the expansion states of this ridget.
 	 */
 	private enum ExpansionState {
 		FULLY_COLLAPSE, FULLY_EXPAND, COLLAPSE, EXPAND, RESTORE
 	}
 
 	/**
 	 * An operation that modifies the expansion state of the tree ridget.
 	 */
 	private static class ExpansionCommand {
 		/** An expansion modification */
 		final ExpansionState state;
 		/** The element to expand / collapse (only for COLLAPSE, EXPAND ops) */
 		final Object element;
 
 		/**
 		 * Creates a new ExpansionCommand instance.
 		 * 
 		 * @param state
 		 *            an expansion modification
 		 * @param element
 		 *            the element to expand / collapse (null for FULLY_EXPAND /
 		 *            FULLY_COLLAPSE)
 		 */
 		ExpansionCommand(ExpansionState state, Object element) {
 			this.state = state;
 			this.element = element;
 		}
 	}
 
 	/**
 	 * Disallows multiple selection is the selection type of the ridget is
 	 * {@link ISelectableRidget.SelectionType#SINGLE}.
 	 */
 	private final class SelectionTypeEnforcer extends SelectionAdapter {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			if (SelectionType.SINGLE.equals(getSelectionType())) {
 				Tree control = (Tree) e.widget;
 				if (control.getSelectionCount() > 1) {
 					// ignore this event
 					e.doit = false;
 					// set selection one item
 					TreeItem firstItem = control.getSelection()[0];
 					// (
 					// control);
 					control.setSelection(firstItem);
 					// fire event
 					Event event = new Event();
 					event.type = SWT.Selection;
 					event.doit = true;
 					control.notifyListeners(SWT.Selection, event);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Notifies doubleClickListeners when the bound widget is double clicked.
 	 */
 	private final class DoubleClickForwarder extends MouseAdapter {
 		@Override
 		public void mouseDoubleClick(MouseEvent e) {
 			if (doubleClickListeners != null) {
 				for (IActionListener listener : doubleClickListeners) {
 					listener.callback();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Extends a standard observable tree content provider with support for:
 	 * <ul>
 	 * <li>handling Object[] <b>and</b> Object input</li> <li>knowing when we
 	 * have a valid input</li>
 	 * </ul>
 	 */
 	private static class TreeContentProvider extends ObservableListTreeContentProvider {
 
 		private boolean hasInput = false;
 
 		TreeContentProvider(IObservableFactory listFactory, TreeStructureAdvisor structureAdvisor) {
 			super(listFactory, structureAdvisor);
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof List) {
 				return ((List) inputElement).toArray();
 			}
 			return (Object[]) inputElement;
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// this is a workaround to allow our set change listener, which
 			// is in charge triggering an update of the tree icons, to skip the
 			// update when the viewer is in the process of disposing itself
 			// (newInput == null)
 			hasInput = (newInput != null);
 			super.inputChanged(viewer, oldInput, newInput);
 		}
 
 		/** Returns true if we have a valid (i.e. non-null) input. */
 		boolean hasInput() {
 			return hasInput;
 		}
 	}
 
 	/**
 	 * Advisor class for the Eclipse 3.4 tree databinding framework. See {link
 	 * TreeStructureAdvisor}.
 	 * <p>
 	 * This advisor uses the supplied property name and beanClass to invoke an
 	 * appropriate accessor (get/isXXX method) on a element in the tree.
 	 * <p>
 	 * This functionality is used by the databinding framework to perform expand
 	 * operations.
 	 * 
 	 * @see TreeStructureAdvisor
 	 */
 	private static final class GenericTreeStructureAdvisor extends TreeStructureAdvisor {
 
 		private static final Object[] EMPTY_ARRAY = new Object[0];
 
 		private final Class<?> beanClass;
 		private PropertyDescriptor descriptor;
 
 		GenericTreeStructureAdvisor(String propertyName, Class<?> beanClass) {
 			Assert.isNotNull(propertyName);
 			String errorMsg = "propertyName cannot be empty"; //$NON-NLS-1$
 			Assert.isLegal(propertyName.trim().length() > 0, errorMsg);
 			Assert.isNotNull(beanClass);
 
 			String readMethodName = "get" + capitalize(propertyName); //$NON-NLS-1$
 			try {
 				descriptor = new PropertyDescriptor(propertyName, beanClass, readMethodName, null);
 			} catch (IntrospectionException exc) {
 				Activator.log(exc);
 				descriptor = null;
 			}
 			this.beanClass = beanClass;
 		}
 
 		@Override
 		public Object getParent(Object element) {
 			Object result = null;
 			if (element != null && beanClass.isAssignableFrom(element.getClass()) && descriptor != null) {
 				Method readMethod = descriptor.getReadMethod();
 				if (!readMethod.isAccessible()) {
 					readMethod.setAccessible(true);
 				}
 				try {
 					result = readMethod.invoke(element, EMPTY_ARRAY);
 				} catch (InvocationTargetException exc) {
 					Activator.log(exc);
 				} catch (IllegalAccessException exc) {
 					Activator.log(exc);
 				}
 			}
 			return result;
 		}
 
 		private String capitalize(String name) {
 			String result = name.substring(0, 1).toUpperCase();
 			if (name.length() > 1) {
 				result += name.substring(1);
 			}
 			return result;
 		}
 	}
 
 	/**
 	 * This change listener reacts to additions / removals of objects from the
 	 * tree and is responsible for updating the image of the <b>parent</b>
 	 * element. Specifically:
 	 * <ul>
 	 * <li>if B gets added to A we have to refresh the icon of A, if A did not
 	 * have any children beforehand</li> <li>if B gets removed to A we have to
 	 * refresh the icon of A, if B was the last child underneath A</li>
 	 * <ul>
 	 */
 	private final class TreeContentChangeListener implements ISetChangeListener {
 
 		private final TreeContentProvider viewerCP;
 		private final TreeStructureAdvisor structureAdvisor;
 
 		private TreeContentChangeListener(TreeContentProvider viewerCP, TreeStructureAdvisor structureAdvisor) {
 			Assert.isNotNull(viewerCP);
 			Assert.isNotNull(structureAdvisor);
 			this.structureAdvisor = structureAdvisor;
 			this.viewerCP = viewerCP;
 		}
 
 		/**
 		 * Updates the icons of the parent elements on addition / removal
 		 */
 		public void handleSetChange(SetChangeEvent event) {
 			if (viewerCP.hasInput()) { // continue only when viewer has input
 				Set<Object> parents = new HashSet<Object>();
 				for (Object element : event.diff.getAdditions()) {
 					Object parent = structureAdvisor.getParent(element);
 					if (parent != null) {
 						parents.add(parent);
 					}
 				}
 				for (Object element : event.diff.getRemovals()) {
 					Object parent = structureAdvisor.getParent(element);
 					if (parent != null) {
 						parents.add(parent);
 					}
 				}
 				for (Object parent : parents) {
 					if (!viewer.isBusy()) {
 						viewer.update(parent, null);
 					}
 				}
 			}
 		}
 	}
 
 }
