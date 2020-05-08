 /**
  *  net.karlmartens.ui, is a library of UI widgets
  *  Copyright (C) 2011
  *  Karl Martens
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package net.karlmartens.ui.viewer;
 
 
 import net.karlmartens.ui.widget.TimeSeriesTable;
 import net.karlmartens.ui.widget.TimeSeriesTableColumn;
 import net.karlmartens.ui.widget.TimeSeriesTableItem;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.AbstractTableViewer;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerEditor;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Widget;
 import org.joda.time.LocalDate;
 
 public final class TimeSeriesTableViewer extends AbstractTableViewer {
 
 	private final TimeSeriesTable _control;
 	private TimeSeriesEditingSupport _editingSupport;
 	private TimeSeriesTableViewerRow _cachedRow;
 	private TimeSeriesTableViewerColumn _periodColumn;
 
 	public TimeSeriesTableViewer(Composite parent) {
 		this(new TimeSeriesTable(parent));
 	}
 	
 	public TimeSeriesTableViewer(TimeSeriesTable control) {
 		_control = control;		
 		hookControl(control);
 	}	
 	
 	public void setEditingSupport(TimeSeriesEditingSupport editingSupport) {
 		_editingSupport = editingSupport;
 	}
 	
 	TimeSeriesEditingSupport getEditingSupport() {
 		return _editingSupport;
 	}
 
 	@Override
 	protected TimeSeriesTableViewerRow internalCreateNewRowPart(int style, int rowIndex) {
 		final TimeSeriesTableItem item;
 		if (rowIndex >= 0) {
 			item = new TimeSeriesTableItem(_control, rowIndex);
 		} else {
 			item = new TimeSeriesTableItem(_control);
 		}
 
 		return getViewerRowFromItem(item);
 	}
 
 	@Override
 	protected int doIndexOf(Item item) {
 		return _control.indexOf((TimeSeriesTableItem)item);
 	}
 
 	@Override
 	protected int doGetItemCount() {
 		return _control.getItemCount();
 	}
 
 	@Override
 	protected void doSetItemCount(int count) {
 		_control.setItemCount(count);
 	}
 
 	@Override
 	protected TimeSeriesTableItem[] doGetItems() {
 		return _control.getItems();
 	}
 
 	@Override
 	protected TimeSeriesTableColumn doGetColumn(int index) {
 		return _control.getColumn(index);
 	}
 
 	@Override
 	protected TimeSeriesTableItem doGetItem(int index) {
 		return _control.getItem(index);
 	}
 
 	@Override
 	protected TimeSeriesTableItem[] doGetSelection() {
 		return _control.getSelection();
 	}
 
 	@Override
 	protected int[] doGetSelectionIndices() {
 		return _control.getSelectionIndices();
 	}
 
 	@Override
 	protected void doClearAll() {
 		_control.clearAll();
 	}
 
 	@Override
 	protected void doResetItem(Item item) {
 		final TimeSeriesTableItem tableItem = (TimeSeriesTableItem)item;
 		final int columnCount = Math.max(1, _control.getColumnCount());
 		for (int i=0; i<columnCount; i++) {
 			tableItem.setText(i, "");
 		}
 		
 		final int periodCount = Math.max(1, _control.getPeriodCount());
 		for (int i=0; i<periodCount; i++) {
 			tableItem.setValue(i, 0.0);
 		}
 	}
 
 	@Override
 	protected void doRemove(int start, int end) {
 		_control.remove(start, end);
 	}
 
 	@Override
 	protected void doRemoveAll() {
 		_control.removeAll();
 	}
 
 	@Override
 	protected void doRemove(int[] indices) {
 		_control.remove(indices);
 	}
 
 	@Override
 	protected void doShowItem(Item item) {
 		_control.showItem((TimeSeriesTableItem)item);
 	}
 
 	@Override
 	protected void doDeselectAll() {
 		_control.deselectAll();
 	}
 
 	@Override
 	protected void doSetSelection(Item[] items) {
 		final TimeSeriesTableItem[] tableItems = new TimeSeriesTableItem[items.length];
 		System.arraycopy(items, 0, tableItems, 0, tableItems.length);
 		_control.setSelection(tableItems);
 	}
 
 	@Override
 	protected void doShowSelection() {
 		_control.showSelection();
 	}
 
 	@Override
 	protected void doSetSelection(int[] indices) {
 		_control.setSelection(indices);
 	}
 
 	@Override
 	protected void doClear(int index) {
 		_control.clear(index);
 	}
 
 	@Override
 	protected void doSelect(int[] indices) {
 		_control.select(indices);
 	}
 
 	@Override
 	protected TimeSeriesTableViewerEditor createViewerEditor() {
 		return new TimeSeriesTableViewerEditor(this, 
 				new ColumnViewerEditorActivationStrategy(this), 
 				ColumnViewerEditor.DEFAULT);
 	}
 
 	@Override
 	protected TimeSeriesTableViewerRow getViewerRowFromItem(Widget item) {
 		if (_cachedRow == null) {
 			_cachedRow = new TimeSeriesTableViewerRow((TimeSeriesTableItem)item);
 		} else {
 			_cachedRow.setItem((TimeSeriesTableItem)item);
 		}
 
 		return _cachedRow;
 	}
 
 	@Override
 	protected TimeSeriesTableItem getItemAt(Point point) {
 		return _control.getItem(point);
 	}
 
 	@Override
 	protected int doGetColumnCount() {
 		return _control.getColumnCount() + _control.getPeriodCount();
 	}
 
 	@Override
 	public TimeSeriesTable getControl() {
 		return _control;
 	}
 	
 	@Override
 	protected void preservingSelection(Runnable updateCode) {
 		final Point[] selection = _control.getCellSelection();
 		try {
 			updateCode.run();
 		} finally {
 			_control.setCellSelection(selection, false);
 		}
 	}
 	
 	@Override
 	protected void internalRefresh(Object element, boolean updateLabels) {
 		if (updateLabels) {
 			final TimeSeriesContentProvider cp = (TimeSeriesContentProvider)getContentProvider();
 			if (cp == null)
 				throw new IllegalStateException();
 
 			final LocalDate[] dates = cp.getDates();
 			if (dates != null) {
 				_control.setPeriods(dates);

				if (_periodColumn == null) {
					_periodColumn = new TimeSeriesTableViewerColumn(this, _control.getColumn(_control.getColumnCount()));
					_periodColumn.setLabelProvider(new PeriodLabelProvider(cp));
					_periodColumn.setEditingSupport(new TimeSeriesTableValueEditingSupport(this));
				}
 			} else {
 				_control.setPeriods(new LocalDate[] {});
 			}
 		}
 		
 		super.internalRefresh(element, updateLabels);
 	}
 		
 	@Override
 	protected void assertContentProviderType(IContentProvider provider) {
 		Assert.isTrue(provider instanceof TimeSeriesContentProvider);
 	}
 	
 	private final class PeriodLabelProvider extends CellLabelProvider {
 
 		private final TimeSeriesContentProvider _base;
 
 		public PeriodLabelProvider(TimeSeriesContentProvider base) {
 			_base = base;
 		}
 
 		@Override
 		public void update(ViewerCell cell) {
 			final int index = cell.getColumnIndex() - _control.getColumnCount();	
 			final double value = _base.getValue(cell.getElement(), index);
 			((TimeSeriesTableItem)cell.getItem()).setValue(index, value);
 		}
 	}
 }
