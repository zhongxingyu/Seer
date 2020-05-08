 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
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
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ISortableByColumn;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.databinding.IUnboundPropertyObservable;
 import org.eclipse.riena.ui.ridgets.databinding.UnboundPropertyWritableList;
import org.eclipse.riena.ui.ridgets.util.beans.Person;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 /**
  * Ridget for SWT {@link Table} widgets.
  */
 public class TableRidget extends AbstractSelectableRidget implements ITableRidget {
 
 	private static final String SORTABILITY_SUFFIX = " \u2195"; //$NON-NLS-1$
 
 	private final SelectionListener selectionTypeEnforcer;
 	private final MouseListener doubleClickForwarder;
 	private final ColumnSortListener sortListener;
 
 	private Collection<IActionListener> doubleClickListeners;
 	private DataBindingContext dbc;
 	private TableViewer viewer;
 	private String[] renderingMethods;
 	private String[] columnHeaders;
 
 	private boolean isSortedAscending;
 	private int sortedColumn;
 	private final Map<Integer, Boolean> sortableColumnsMap;
 	private final Map<Integer, Comparator<Object>> comparatorMap;
 
 	public TableRidget() {
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 		doubleClickForwarder = new DoubleClickForwarder();
 		sortListener = new ColumnSortListener();
 		isSortedAscending = true;
 		sortedColumn = -1;
 		sortableColumnsMap = new HashMap<Integer, Boolean>();
 		comparatorMap = new HashMap<Integer, Comparator<Object>>();
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, Table.class);
 	}
 
 	@Override
 	protected void bindUIControl() {
 		final Table control = (Table) getUIControl();
 		if (control != null && getRowObservables() != null) {
 			viewer = new TableViewer(control);
 			final ObservableListContentProvider viewerCP = new ObservableListContentProvider();
			IObservableMap[] attrMap = BeansObservables.observeMaps(viewerCP.getKnownElements(), Person.class,
 					renderingMethods);
 			viewer.setLabelProvider(new ObservableMapLabelProvider(attrMap));
 			viewer.setContentProvider(viewerCP);
 
 			applyColumnsMoveable(control);
 			applyTableColumnHeaders(control);
 			applyComparator();
 
 			viewer.setInput(getRowObservables());
 
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
 
 			for (TableColumn column : control.getColumns()) {
 				column.addSelectionListener(sortListener);
 			}
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
 		Table control = getUIControl();
 		if (control != null) {
 			for (TableColumn column : control.getColumns()) {
 				column.removeSelectionListener(sortListener);
 			}
 			control.removeSelectionListener(selectionTypeEnforcer);
 			control.removeMouseListener(doubleClickForwarder);
 		}
 		viewer = null;
 	}
 
 	@Override
 	public Table getUIControl() {
 		return (Table) super.getUIControl();
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
 
 		renderingMethods = new String[columnPropertyNames.length];
 		System.arraycopy(columnPropertyNames, 0, renderingMethods, 0, renderingMethods.length);
 		setRowObservables(listObservableValue);
 
 		if (columnHeaders != null) {
 			this.columnHeaders = new String[columnHeaders.length];
 			System.arraycopy(columnHeaders, 0, this.columnHeaders, 0, this.columnHeaders.length);
 		} else {
 			this.columnHeaders = null;
 		}
 
 		bindUIControl();
 	}
 
 	public void bindToModel(Object listBean, String listPropertyName, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] columnHeaders) {
 		IObservableList listObservableValue = new UnboundPropertyWritableList(listBean, listPropertyName);
 		bindToModel(listObservableValue, rowBeanClass, columnPropertyNames, columnHeaders);
 	}
 
 	public void updateFromModel() {
 		super.updateFromModel();
 		if (viewer != null) {
 			// prevent flicker during update
 			viewer.getControl().setRedraw(false);
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
 
 	public void setComparator(int columnIndex, Comparator<Object> compi) {
 		checkColumnRange(columnIndex);
 		Integer key = Integer.valueOf(columnIndex);
 		if (compi != null) {
 			comparatorMap.put(key, compi);
 		} else {
 			comparatorMap.remove(key);
 		}
 		if (columnIndex == sortedColumn) {
 			applyComparator();
 		}
 	}
 
 	public int getSortedColumn() {
 		int result = -1;
 		Table table = getUIControl();
 		if (table != null) {
 			TableColumn column = table.getSortColumn();
 			if (column != null) {
 				result = getColumnIndex(column);
 			}
 		}
 		return result;
 	}
 
 	public boolean isColumnSortable(int columnIndex) {
 		checkColumnRange(columnIndex);
 		boolean result = false;
 		Integer key = Integer.valueOf(columnIndex);
 		Boolean sortable = sortableColumnsMap.get(columnIndex);
 		if (sortable == null || Boolean.TRUE.equals(sortable)) {
 			result = comparatorMap.get(key) != null;
 		}
 		return result;
 	}
 
 	public boolean isSortedAscending() {
 		boolean result = false;
 		Table table = getUIControl();
 		if (table != null) {
 			int sortDirection = table.getSortDirection();
 			result = (sortDirection == SWT.DOWN);
 		}
 		return result;
 	}
 
 	public void setColumnSortable(int columnIndex, boolean sortable) {
 		checkColumnRange(columnIndex);
 		Integer key = Integer.valueOf(columnIndex);
 		Boolean newValue = Boolean.valueOf(sortable);
 		Boolean oldValue = sortableColumnsMap.put(key, newValue);
 		Table control = getUIControl();
 		if (control != null) {
 			applyTableColumnHeaders(control);
 		}
 		if (oldValue == null) {
 			oldValue = Boolean.TRUE;
 		}
 		if (!newValue.equals(oldValue)) {
 			firePropertyChange(ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null, columnIndex);
 		}
 	}
 
 	public void setSortedAscending(boolean ascending) {
 		if (isSortedAscending != ascending) {
 			boolean oldSortedAscending = isSortedAscending;
 			isSortedAscending = ascending;
 			applyComparator();
 			firePropertyChange(ISortableByColumn.PROPERTY_SORT_ASCENDING, oldSortedAscending, isSortedAscending);
 		}
 	}
 
 	public void setSortedColumn(int columnIndex) {
 		checkColumnRange(columnIndex);
 		if (sortedColumn != columnIndex) {
 			int oldSortedColumn = sortedColumn;
 			sortedColumn = columnIndex;
 			applyComparator();
 			firePropertyChange(ISortableByColumn.PROPERTY_SORTED_COLUMN, oldSortedColumn, sortedColumn);
 		}
 	}
 
 	public int getSelectionIndex() {
 		Table control = getUIControl();
 		return control == null ? -1 : control.getSelectionIndex();
 	}
 
 	public int[] getSelectionIndices() {
 		Table control = getUIControl();
 		return control == null ? new int[0] : control.getSelectionIndices();
 	}
 
 	public int indexOfOption(Object option) {
 		Table control = getUIControl();
 		if (control != null) {
 			// implies viewer != null
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
 
 	private static void applyColumnsMoveable(Table control) {
 		for (TableColumn column : control.getColumns()) {
 			column.setMoveable(true);
 		}
 	}
 
 	private void applyComparator() {
 		if (viewer != null) {
 			Table table = viewer.getTable();
 			Integer key = Integer.valueOf(sortedColumn);
 			Comparator<Object> compi = comparatorMap.get(key);
 			if (compi != null) {
 				SortableComparator sortableComparator = new SortableComparator(compi);
 				viewer.setComparator(new ViewerComparator(sortableComparator));
 				TableColumn column = table.getColumn(sortedColumn);
 				table.setSortColumn(column);
 			} else {
 				viewer.setComparator(null);
 				table.setSortColumn(null);
 			}
 			int direction = isSortedAscending ? SWT.DOWN : SWT.UP;
 			table.setSortDirection(direction);
 		}
 	}
 
 	private void applyTableColumnHeaders(Table control) {
 		boolean headersVisible = columnHeaders != null;
 		control.setHeaderVisible(headersVisible);
 		if (headersVisible) {
 			TableColumn[] columns = control.getColumns();
 			for (int i = 0; i < columns.length; i++) {
 				String columnHeader = ""; //$NON-NLS-1$
 				if (i < columnHeaders.length) {
 					columnHeader = String.valueOf(columnHeaders[i]);
 				}
 				if (i != sortedColumn && isColumnSortable(i)) {
 					columnHeader += SORTABILITY_SUFFIX;
 				}
 				columns[i].setText(columnHeader);
 			}
 		}
 	}
 
 	private void checkColumnRange(int columnIndex) {
 		Table table = getUIControl(); // table may be null if unbound
 		int range = table.getColumnCount();
 		String msg = "columnIndex out of range (0 - " + range + " ): " + columnIndex; //$NON-NLS-1$ //$NON-NLS-2$
 		Assert.isLegal(-1 < columnIndex, msg);
 		Assert.isLegal(columnIndex < range, msg);
 	}
 
 	private static int getColumnIndex(TableColumn column) {
 		Table table = column.getParent();
 		TableColumn[] columns = table.getColumns();
 		int result = -1;
 		for (int i = 0; result == -1 && i < columns.length; i++) {
 			if (columns[i] == column) {
 				result = i;
 			}
 		}
 		return result;
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
 				Table control = (Table) e.widget;
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
 
 	/**
 	 * Selection listener for table headers that changes the sort order of a
 	 * column according to the information stored in the ridget.
 	 */
 	private final class ColumnSortListener extends SelectionAdapter {
 		public void widgetSelected(SelectionEvent e) {
 			TableColumn column = (TableColumn) e.widget;
 			int columnIndex = getColumnIndex(column);
 			if (columnIndex == sortedColumn) {
 				setSortedAscending(!isSortedAscending);
 			} else if (isColumnSortable(columnIndex)) {
 				setSortedColumn(columnIndex);
 				applyTableColumnHeaders(column.getParent());
 			}
 		}
 	}
 
 }
