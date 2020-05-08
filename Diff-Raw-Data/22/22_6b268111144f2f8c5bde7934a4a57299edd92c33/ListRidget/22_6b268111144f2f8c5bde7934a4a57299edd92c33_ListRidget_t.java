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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.IObservable;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.internal.databinding.viewers.SelectionProviderMultipleSelectionObservableList;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ISortableByColumn;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.databinding.IUnboundPropertyObservable;
 import org.eclipse.riena.ui.ridgets.databinding.UnboundPropertyWritableList;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.List;
 
 /**
  * Ridget for SWT {@link List} widgets.
  */
 public class ListRidget extends AbstractSelectableRidget implements ITableRidget {
 
 	private final SelectionListener selectionTypeEnforcer;
 	private final MouseListener doubleClickForwarder;
 
 	private Collection<IActionListener> doubleClickListeners;
 	private DataBindingContext dbc;
 	private ListViewer viewer;
 	private ViewerComparator comparator;
 	private boolean isSortedAscending;
 	private String renderingMethod;
	private Class<?> rowBeanClass;
 
 	public ListRidget() {
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 		doubleClickForwarder = new DoubleClickForwarder();
 		isSortedAscending = true;
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, List.class);
 	}
 
 	@Override
 	protected void bindUIControl() {
 		final List control = (List) getUIControl();
 		if (control != null && getRowObservables() != null) {
 			viewer = new ListViewer(control);
 			final ObservableListContentProvider viewerCP = new ObservableListContentProvider();
			IObservableMap[] attrMap = BeansObservables.observeMaps(viewerCP.getKnownElements(), rowBeanClass,
 					new String[] { renderingMethod });
 			viewer.setLabelProvider(new ObservableMapLabelProvider(attrMap));
 			viewer.setContentProvider(viewerCP);
 			viewer.setInput(getRowObservables());
 
 			applyComparator();
 
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
 
 			control.addSelectionListener(selectionTypeEnforcer);
 			control.addMouseListener(doubleClickForwarder);
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		if (dbc != null) {
 			dbc.dispose();
 			dbc = null;
 		}
 		List control = getUIControl();
 		if (control != null) {
 			control.removeSelectionListener(selectionTypeEnforcer);
 			control.removeMouseListener(doubleClickForwarder);
 		}
 		viewer = null;
 	}
 
 	@Override
 	public List getUIControl() {
 		return (List) super.getUIControl();
 	}
 
 	public void addDoubleClickListener(IActionListener listener) {
 		Assert.isNotNull(listener, "listener is null"); //$NON-NLS-1$
 		if (doubleClickListeners == null) {
 			doubleClickListeners = new ArrayList<IActionListener>();
 		}
 		doubleClickListeners.add(listener);
 	}
 
 	public void bindToModel(IObservableList listObservableValue, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] columnHeaders) {
 
 		unbindUIControl();
 
		this.rowBeanClass = rowBeanClass;
 		renderingMethod = columnPropertyNames[0];
 		setRowObservables(listObservableValue);
 
 		bindUIControl();
 	}
 
 	public void bindToModel(Object listBean, String listPropertyName, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] columnHeaders) {
 		IObservableList listObservableValue = new UnboundPropertyWritableList(listBean, listPropertyName);
 		bindToModel(listObservableValue, rowBeanClass, columnPropertyNames, columnHeaders);
 	}
 
 	@Override
 	public void updateFromModel() {
 		super.updateFromModel();
 		if (viewer != null) {
 			viewer.getControl().setRedraw(false); // prevent flicker during
 			// update
 			StructuredSelection currentSelection = new StructuredSelection(getSelection());
 			try {
 				IObservable model = getRowObservables();
 				if (model instanceof IUnboundPropertyObservable) {
 					((UnboundPropertyWritableList) model).updateFromBean();
 				}
 				viewer.refresh(true);
 			} finally {
 				viewer.setSelection(currentSelection);
 				viewer.getControl().setRedraw(true);
 			}
 		}
 	}
 
 	public IObservableList getObservableList() {
 		return getRowObservables();
 	}
 
 	public void removeDoubleClickListener(IActionListener listener) {
 		if (doubleClickListeners != null) {
 			doubleClickListeners.remove(listener);
 		}
 	}
 
 	public void setComparator(int columnIndex, Comparator<Object> comparator) {
 		Assert.isLegal(columnIndex == 0, "columnIndex out of bounds (must be 0)"); //$NON-NLS-1$
 		if (comparator != null) {
 			SortableComparator sortableComparator = new SortableComparator(comparator);
 			this.comparator = new ViewerComparator(sortableComparator);
 		} else {
 			this.comparator = null;
 		}
 		applyComparator();
 	}
 
 	public int getSortedColumn() {
 		return comparator != null ? 0 : -1;
 	}
 
 	public boolean isColumnSortable(int columnIndex) {
 		Assert.isLegal(columnIndex == 0, "columnIndex out of bounds (must be 0)"); //$NON-NLS-1$
 		return comparator != null;
 	}
 
 	public boolean isSortedAscending() {
 		return isSortedAscending;
 	}
 
 	/**
 	 * This method is not supported by this ridget.
 	 * 
 	 * @throws UnsupportedOperationException
 	 * 		this is not supported by this ridget
 	 */
 	public final void setColumnSortable(int columnIndex, boolean sortable) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void setSortedAscending(boolean ascending) {
 		if (ascending != isSortedAscending) {
 			boolean oldSortedAscending = isSortedAscending;
 			isSortedAscending = ascending;
 			if (viewer != null) {
 				viewer.refresh();
 			}
 			firePropertyChange(ISortableByColumn.PROPERTY_SORT_ASCENDING, oldSortedAscending, isSortedAscending);
 		}
 	}
 
 	/**
 	 * <p>
 	 * This method is not supported by this ridget.
 	 * </p>
 	 * <p>
 	 * Use {@link #setComparator(int, Comparator)} to enable / disable sorting
 	 * for column-0.
 	 * </p>
 	 * 
 	 * @see #setComparator(int, Comparator)
 	 * @throws UnsupportedOperationException
 	 * 		this is not supported by this ridget
 	 * 
 	 */
 	public final void setSortedColumn(int columnIndex) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int getSelectionIndex() {
 		List control = getUIControl();
 		return control == null ? -1 : control.getSelectionIndex();
 	}
 
 	@Override
 	public int[] getSelectionIndices() {
 		List control = getUIControl();
 		return control == null ? new int[0] : control.getSelectionIndices();
 	}
 
 	@Override
 	public int indexOfOption(Object option) {
 		List control = getUIControl();
 		if (control != null) {
 			int optionCount = control.getItemCount();
 			for (int i = 0; i < optionCount; i++) {
 				if (viewer.getElementAt(i).equals(option)) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void applyComparator() {
 		if (viewer != null) {
 			viewer.setComparator(this.comparator);
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Disallows multiple selection is the selection type of the ridget is
 	 * {@link ISelectableRidget.SelectionType#SINGLE}.
 	 */
 	private final class SelectionTypeEnforcer extends SelectionAdapter {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			if (SelectionType.SINGLE.equals(getSelectionType())) {
 				List control = (List) e.widget;
 				if (control.getSelectionCount() > 1) {
 					// ignore this event
 					e.doit = false;
 					// set selection to most recent item
 					control.setSelection(control.getSelectionIndex());
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
 	 * Changes the result of the given <tt>comparator</tt> according to the
 	 * <tt>sortedAscending</tt> setting in the ridget.
 	 */
 	private final class SortableComparator implements Comparator<Object> {
 
 		private final Comparator<Object> orgComparator;
 
 		SortableComparator(Comparator<Object> comparator) {
 			orgComparator = comparator;
 		}
 
 		public int compare(Object o1, Object o2) {
 			int result = orgComparator.compare(o1, o2);
 			return isSortedAscending ? result : result * -1;
 		}
 	}
 
 }
