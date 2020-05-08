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
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
import org.eclipse.riena.ui.ridgets.IGroupedTreeTableRidget;
import org.eclipse.riena.ui.ridgets.ISortableByColumn;
import org.eclipse.riena.ui.ridgets.ITreeTableRidget;

 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * Ridget for SWT @link {@link Tree} widgets, that shows a tree with multiple
  * columns.
  */
 public class TreeTableRidget extends TreeRidget implements ITreeTableRidget, IGroupedTreeTableRidget {
 
 	private final ColumnSortListener sortListener;
 	private final Listener groupedTableListener;
 
 	private boolean isSortedAscending;
 	private int sortedColumn;
 	private final Map<Integer, Boolean> sortableColumnsMap;
 	private final Map<Integer, Comparator<Object>> comparatorMap;
 
 	private boolean isGroupingEnabled;
 
 	public TreeTableRidget() {
 		sortListener = new ColumnSortListener();
 		groupedTableListener = new GroupedTablePaintListener();
 		isSortedAscending = true;
 		sortedColumn = -1;
 		sortableColumnsMap = new HashMap<Integer, Boolean>();
 		comparatorMap = new HashMap<Integer, Comparator<Object>>();
 	}
 
 	@Override
 	protected void bindUIControl() {
 		super.bindUIControl();
 		Tree control = getUIControl();
 		if (control != null) {
 			for (TreeColumn column : control.getColumns()) {
 				column.addSelectionListener(sortListener);
 			}
 			applyComparator();
 			applyGrouping();
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		Tree control = getUIControl();
 		if (control != null) {
 			for (TreeColumn column : control.getColumns()) {
 				column.removeSelectionListener(sortListener);
 			}
 			control.removeListener(SWT.EraseItem, groupedTableListener);
 			control.removeListener(SWT.PaintItem, groupedTableListener);
 		}
 	}
 
 	// ITreeTableRidget methods
 	// /////////////////////////
 
 	@Override
 	public void bindToModel(Object[] treeRoots, Class<? extends Object> treeElementClass, String childrenAccessor,
 			String parentAccessor, String[] valueAccessors, String[] columnHeaders) {
 		super.bindToModel(treeRoots, treeElementClass, childrenAccessor, parentAccessor, valueAccessors, columnHeaders);
 	}
 
 	// IGroupedTableRidget methods
 	// ////////////////////////////
 
 	public boolean isGroupingEnabled() {
 		return isGroupingEnabled;
 	}
 
 	public void setGroupingEnabled(boolean grouping) {
 		boolean oldValue = isGroupingEnabled;
 		isGroupingEnabled = grouping;
 		if (oldValue != isGroupingEnabled) {
 			firePropertyChange(IGroupedTreeTableRidget.PROPERTY_GROUPING_ENABLED, oldValue, isGroupingEnabled);
 			applyGrouping();
 		}
 	}
 
 	// ISortableByColumn methods
 	// //////////////////////////
 
 	public int getSortedColumn() {
 		int result = -1;
 		Tree tree = getUIControl();
 		if (tree != null) {
 			TreeColumn column = tree.getSortColumn();
 			if (column != null) {
 				result = tree.indexOf(column);
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
 		Tree tree = getUIControl();
 		if (tree != null) {
 			int sortDirection = tree.getSortDirection();
 			result = (sortDirection == SWT.DOWN);
 		}
 		return result;
 	}
 
 	public void setColumnSortable(int columnIndex, boolean sortable) {
 		checkColumnRange(columnIndex);
 		Integer key = Integer.valueOf(columnIndex);
 		Boolean newValue = Boolean.valueOf(sortable);
 		Boolean oldValue = sortableColumnsMap.put(key, newValue);
 		if (oldValue == null) {
 			oldValue = Boolean.TRUE;
 		}
 		if (!newValue.equals(oldValue)) {
 			firePropertyChange(ISortableByColumn.PROPERTY_COLUMN_SORTABILITY, null, columnIndex);
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
 
 	public void setSortedAscending(boolean ascending) {
 		if (isSortedAscending != ascending) {
 			boolean oldSortedAscending = isSortedAscending;
 			isSortedAscending = ascending;
 			applyComparator();
 			firePropertyChange(ISortableByColumn.PROPERTY_SORT_ASCENDING, oldSortedAscending, isSortedAscending);
 		}
 	}
 
 	public void setSortedColumn(int columnIndex) {
 		if (columnIndex != -1) {
 			checkColumnRange(columnIndex);
 		}
 		if (sortedColumn != columnIndex) {
 			int oldSortedColumn = sortedColumn;
 			sortedColumn = columnIndex;
 			applyComparator();
 			firePropertyChange(ISortableByColumn.PROPERTY_SORTED_COLUMN, oldSortedColumn, sortedColumn);
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void applyComparator() {
 		TreeViewer viewer = getViewer();
 		if (viewer != null) {
 			Tree tree = viewer.getTree();
 			tree.setRedraw(false);
 			try {
 				Comparator<Object> compi = null;
 				if (sortedColumn != -1) {
 					Integer key = Integer.valueOf(sortedColumn);
 					compi = comparatorMap.get(key);
 				}
 				if (compi != null) {
 					TreeColumn column = tree.getColumn(sortedColumn);
 					tree.setSortColumn(column);
 					int direction = isSortedAscending ? SWT.DOWN : SWT.UP;
 					tree.setSortDirection(direction);
 					SortableComparator sortableComparator = new SortableComparator(this, compi);
 					viewer.setComparator(new ViewerComparator(sortableComparator));
 				} else {
 					viewer.setComparator(null);
 					tree.setSortColumn(null);
 					tree.setSortDirection(SWT.NONE);
 				}
 				// we have to update the expanded / collapsed icons
 				viewer.refresh();
 			} finally {
 				tree.setRedraw(true);
 			}
 		}
 	}
 
 	private void applyGrouping() {
 		Control control = getUIControl();
 		if (control != null) {
 			control.setRedraw(false);
 			try {
 				control.removeListener(SWT.EraseItem, groupedTableListener);
 				control.removeListener(SWT.PaintItem, groupedTableListener);
 				if (isGroupingEnabled) {
 					control.addListener(SWT.EraseItem, groupedTableListener);
 					control.addListener(SWT.PaintItem, groupedTableListener);
 				}
 			} finally {
 				control.setRedraw(true);
 				control.redraw();
 			}
 		}
 	}
 
 	private void checkColumnRange(int columnIndex) {
 		Tree tree = getUIControl(); // tree may be null if unbound
 		int range = tree.getColumnCount();
 		String msg = "columnIndex out of range (0 - " + range + " ): " + columnIndex; //$NON-NLS-1$ //$NON-NLS-2$
 		Assert.isLegal(-1 < columnIndex, msg);
 		Assert.isLegal(columnIndex < range, msg);
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Selection listener for table headers that changes the sort order of a
 	 * column according to the information stored in the ridget.
 	 */
 	private final class ColumnSortListener extends SelectionAdapter {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			TreeColumn column = (TreeColumn) e.widget;
 			int columnIndex = column.getParent().indexOf(column);
 			int direction = column.getParent().getSortDirection();
 			if (columnIndex == sortedColumn) {
 				if (direction == SWT.DOWN) {
 					setSortedAscending(false);
 				} else if (direction == SWT.UP) {
 					setSortedColumn(-1);
 				}
 			} else if (isColumnSortable(columnIndex)) {
 				setSortedColumn(columnIndex);
 				if (direction == SWT.NONE) {
 					setSortedAscending(true);
 				}
 			}
 			column.getParent().showSelection();
 		}
 	}
 
 	/**
 	 * Listener for EraseItem / PaintItem events that is repsonsible for greated
 	 * the "grouped" look in tree tables.
 	 * <p>
 	 * Refer to '<a href="http://www.eclipse.org/articles/article.php?file=Article-CustomDrawingTableAndTreeItems/index.html"
 	 * >Custom Drawing Table and Tree Items</a>' for additional info.
 	 */
 	private final static class GroupedTablePaintListener implements Listener {
 
 		/*
 		 * Called EXTREMELY frequently. Must be as efficient as possible.
 		 */
 		public void handleEvent(Event event) {
 			TreeItem item = (TreeItem) event.item;
 			/*
 			 * let SWT draw the cell content if: (a) the item has no children or
 			 * (b) we are in the first column
 			 */
 			if (item.getItemCount() == 0 || event.index == 0) {
 				return;
 			}
 			if (event.type == SWT.EraseItem) {
 				// indicate we are responsible for drawing the cell's content
 				event.detail &= ~SWT.FOREGROUND;
			} // else if (event.type == SWT.PaintItem) {
			// paint nothing to leave the cell blank
			// }
 		}
 	}
 
 }
