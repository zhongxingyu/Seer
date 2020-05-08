 /**
  *   Copyright 2011 Karl Martens
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *       
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  *   net.karlmartens.ui, is a library of UI widgets
  */
 package net.karlmartens.ui.widget;
 
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.BitSet;
 
 import net.karlmartens.platform.text.LocalDateFormat;
 import net.karlmartens.platform.util.ArraySupport;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.TypedListener;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 
 import de.kupzog.ktable.KTable;
 import de.kupzog.ktable.KTableCellEditor;
 import de.kupzog.ktable.KTableCellRenderer;
 import de.kupzog.ktable.KTableCellResizeListener;
 import de.kupzog.ktable.KTableDefaultModel;
 import de.kupzog.ktable.KTableModel;
 import de.kupzog.ktable.SWTX;
 import de.kupzog.ktable.renderers.CheckableCellRenderer;
 import de.kupzog.ktable.renderers.DefaultCellRenderer;
 import de.kupzog.ktable.renderers.FixedCellRenderer;
 import de.kupzog.ktable.renderers.TextCellRenderer;
 
 public final class TimeSeriesTable extends Composite {
 
   public enum ScrollDataMode {
     FOCUS_CELL, SELECTED_ROWS
   };
 
   private final CellSelectionManager _cellSelectionManager;
 
   private final TimeSeriesTableListener _listener;
   private final Font _defaultFont;
   private final TimeSeriesTableColumn _periodColumn;
   private final KTableImpl _table;
   private final SparklineScrollBar _hscroll;
   private final int _defaultWidth;
   private final int _rowHeight;
 
   private boolean _showHeader = false;
   private ScrollDataMode _scrollDataMode = ScrollDataMode.FOCUS_CELL;
   private LocalDateFormat _dateFormat = new LocalDateFormat(DateTimeFormat.shortDate());
   private NumberFormat _numberFormat = NumberFormat.getNumberInstance();
 
   private int _columnCount = 0;
   private int _itemCount = 0;
   private TimeSeriesTableColumn[] _columns = {};
   private TimeSeriesTableItem[] _items = {};
   private LocalDate[] _periods = {};
   private int[] _widths = {};
 
   private boolean _requiresRedraw = true;
   private boolean _inUpdate = false;
   private int _lastPeriodColumnIndex = -1;
   private int[] _lastRowSelection = new int[0];
   private int _lastIndexOf = -1;
 
   public TimeSeriesTable(Composite parent) {
     super(parent, SWT.NONE);
     setLayout(new FormLayout());
 
     _defaultFont = new Font(getDisplay(), "Arial", 10, SWT.BOLD);
     _listener = new TimeSeriesTableListener();
 
     final GC gc = new GC(getShell());
     gc.setFont(getFont());
     _periodColumn = new TimeSeriesTableColumn(this);
     _periodColumn.setMoveable(false);
 
     _defaultWidth = gc.getCharWidth('W') * 8;
     _rowHeight = gc.getFontMetrics().getHeight() + 6;
     gc.dispose();
 
     _table = new KTableImpl(this, SWT.FLAT | SWT.V_SCROLL | SWT.MULTI | SWTX.MARK_FOCUS_HEADERS);
     _table.setBackground(getBackground());
     _table.setForeground(getForeground());
     _table.setModel(new TimeSeriesTableModel());
 
     _hscroll = new SparklineScrollBar(this, SWT.BORDER);
     _hscroll.setMinimum(0);
     _hscroll.setMaximum(1);
     _hscroll.setSelection(0);
     _hscroll.setThumb(2);
     _hscroll.setLabelFont(_defaultFont);
 
     final FormData tableData = new FormData();
     tableData.top = new FormAttachment(0, 100, 0);
     tableData.left = new FormAttachment(_hscroll, 0, SWT.LEFT);
     tableData.bottom = new FormAttachment(_hscroll, -5, SWT.TOP);
     tableData.right = new FormAttachment(_hscroll, 0, SWT.RIGHT);
 
     final FormData scrollData = new FormData();
     scrollData.left = new FormAttachment(0, 100, 0);
     scrollData.bottom = new FormAttachment(100, 100, 0);
     scrollData.right = new FormAttachment(100, 100, 0);
     scrollData.height = 40;
 
     _table.setLayoutData(tableData);
     _hscroll.setLayoutData(scrollData);
 
     _cellSelectionManager = new CellSelectionManager(this);
     final PassthoughEventListener passthroughListener = new PassthoughEventListener(this);
     passthroughListener.addSource(_table);
     new TimeSeriesColumnManager(this, _table);
     hookControls();
   }
 
   @Override
   public void setBackground(Color color) {
     super.setBackground(color);
     _table.setBackground(color);
   }
 
   @Override
   public void setForeground(Color color) {
     super.setForeground(color);
     _table.setForeground(color);
   }
 
   public int getColumnCount() {
     checkWidget();
     return _columnCount;
   }
 
   public int getPeriodCount() {
     checkWidget();
     return _periods.length;
   }
 
   public int getItemCount() {
     checkWidget();
     return _itemCount;
   }
 
   public int indexOf(TimeSeriesTableItem item) {
     checkWidget();
     checkNull(item);
 
     if (_lastIndexOf >= 1 && _lastIndexOf < _itemCount - 1) {
       if (_items[_lastIndexOf] == item)
         return _lastIndexOf;
       if (_items[_lastIndexOf + 1] == item)
         return ++_lastIndexOf;
       if (_items[_lastIndexOf - 1] == item)
         return --_lastIndexOf;
     }
 
     if (_lastIndexOf < _itemCount / 2) {
       for (int i = 0; i < _itemCount; i++) {
         if (_items[i] == item) {
           _lastIndexOf = i;
           return i;
         }
       }
     } else {
       for (int i = _itemCount - 1; i >= 0; i--) {
         if (_items[i] == item) {
           _lastIndexOf = i;
           return i;
         }
       }
     }
 
     return -1;
   }
 
   public int indexOf(TimeSeriesTableColumn column) {
     checkWidget();
     checkNull(column);
 
     if (column == _periodColumn) {
       final Object o = _periodColumn.getData();
       if (o == null)
         return -1;
 
       return ((Integer) o).intValue() + _columnCount;
     }
 
     for (int i = 0; i < _columnCount; i++) {
       if (_columns[i] == column) {
         return i;
       }
     }
 
     return -1;
   }
 
   public TimeSeriesTableItem[] getItems() {
     checkWidget();
     final TimeSeriesTableItem[] items = new TimeSeriesTableItem[_itemCount];
     System.arraycopy(_items, 0, items, 0, items.length);
     return items;
   }
 
   public TimeSeriesTableItem getItem(int index) {
     checkWidget();
     checkRowIndex(index);
     return _items[index];
   }
 
   public TimeSeriesTableItem getItem(Point point) {
     checkWidget();
     checkNull(point);
 
     final Point dPoint = this.toDisplay(point);
     final Point tPoint = _table.toControl(dPoint);
     final Point cell = _table.getCellForCoordinates(tPoint.x, tPoint.y);
     final int row = computeModelRow(cell.y);
     if (row < 0)
       return null;
 
     return _items[row];
   }
 
   public int[] getSelectionIndices() {
     checkWidget();
 
     final BitSet selectedRows = new BitSet();
     for (Point selection : _table.getCellSelection()) {
       if (_showHeader && selection.y < _table.getModel().getFixedHeaderRowCount())
         continue;
 
       selectedRows.set(computeModelRow(selection.y));
     }
 
     return ArraySupport.toArray(selectedRows);
   }
 
   public TimeSeriesTableItem[] getSelection() {
     checkWidget();
 
     final int[] indices = getSelectionIndices();
     final TimeSeriesTableItem[] selected = new TimeSeriesTableItem[indices.length];
     for (int i = 0; i < indices.length; i++) {
       selected[i] = _items[indices[i]];
     }
 
     return selected;
   }
 
   public TimeSeriesTableColumn getColumn(int index) {
     checkWidget();
     checkColumnIndex(index);
 
     if (index < _columnCount)
       return _columns[index];
 
     final int periodIndex = index - _columnCount;
     if (periodIndex == _lastPeriodColumnIndex)
       return _periodColumn;
 
     if (_lastPeriodColumnIndex != -1 && _lastPeriodColumnIndex < _widths.length)
       _widths[_lastPeriodColumnIndex] = _periodColumn.getWidth();
 
     try {
       _inUpdate = true;
       final LocalDate date = _periods[periodIndex];
       _periodColumn.setText(date == null ? "" : _dateFormat.format(date));
       _periodColumn.setWidth(_widths[periodIndex]);
       _periodColumn.setData(periodIndex);
       _lastPeriodColumnIndex = periodIndex;
     } finally {
       _inUpdate = false;
     }
     return _periodColumn;
   }
 
   public Rectangle getVisibleDataCells() {
     checkWidget();
 
     final Rectangle r = doGetVisibleDataCells();
     r.x -= _columnCount;
     r.y = computeModelRow(r.y);
 
     if (r.x < 0 || r.x >= _periods.length || r.y < 0 || r.y >= _itemCount)
       return new Rectangle(0, 0, 0, 0);
 
     return r;
   }
 
   @Override
   public boolean setFocus() {
     return _table.forceFocus();
   }
 
   @Override
   public boolean isFocusControl() {
     if (_table.isFocusControl())
       return true;
 
     return super.isFocusControl();
   }
 
   public void setHeaderVisible(boolean show) {
     checkWidget();
     _showHeader = show;
     _table.redraw();
   }
 
   public void setPeriods(LocalDate[] periods) {
     checkWidget();
     checkNull(periods);
 
     final LocalDate[] newPeriods = new LocalDate[periods.length];
     System.arraycopy(periods, 0, newPeriods, 0, newPeriods.length);
     Arrays.sort(newPeriods);
     _periods = periods;
 
     final int len = Math.min(_widths.length, _periods.length);
     final int[] newWidths = new int[_periods.length];
     System.arraycopy(_widths, 0, newWidths, 0, len);
     if (len < newWidths.length) {
       Arrays.fill(newWidths, len, newWidths.length, _defaultWidth);
     }
     _widths = newWidths;
 
     _hscroll.setMaximum(Math.max(1, _periods.length - 1));
     _table.redraw();
   }
 
   public void setDateFormat(LocalDateFormat format) {
     checkWidget();
     checkNull(format);
 
     _dateFormat = format;
     _table.redraw();
   }
 
   public void setNumberFormat(NumberFormat format) {
     checkWidget();
     checkNull(format);
 
     _numberFormat = format;
     _table.redraw();
   }
 
   public void setScrollDataMode(ScrollDataMode mode) {
     checkWidget();
     checkNull(mode);
 
     _scrollDataMode = mode;
     doUpdateScrollData();
   }
 
   public void deselectAll() {
     checkWidget();
     _table.clearSelection();
     notifyListeners(SWT.Selection, new Event());
   }
 
   public void setSelection(TimeSeriesTableItem[] items) {
     checkWidget();
     checkNull(items);
 
     final int[] indices = new int[items.length];
     int i = 0;
     for (TimeSeriesTableItem item : items) {
       final int index = indexOf(item);
       if (index < 0)
         continue;
 
       indices[i++] = index;
     }
 
     final int[] result = new int[i];
     System.arraycopy(indices, 0, result, 0, i);
     setSelection(result);
   }
 
   public void setSelection(int[] indices) {
     checkWidget();
     checkNull(indices);
 
     final int width = _columnCount + _periods.length;
     final Point[] selections = new Point[indices.length * width];
     for (int i = 0; i < indices.length; i++) {
       for (int j = 0; j < width; j++) {
         selections[i * width + j] = new Point(j, computeTableRow(indices[i]));
       }
     }
 
     _table.clearSelection();
     _table.setSelection(selections, false);
 
     final Event e = new Event();
     e.item = indices.length > 0 ? getItem(indices[0]) : null;
     notifyListeners(SWT.Selection, e);
   }
 
   public void select(int[] indices) {
     checkWidget();
     checkNull(indices);
 
     final BitSet selected = new BitSet();
     for (int index : getSelectionIndices()) {
       selected.set(index);
     }
 
     for (int index : indices) {
       selected.set(index);
     }
 
     final int[] newSelection = new int[selected.cardinality()];
     int index = 0;
     for (int i = selected.nextSetBit(0); i >= 0; i = selected.nextSetBit(i + 1)) {
       newSelection[index++] = i;
     }
     setSelection(newSelection);
   }
 
   public Point[] getCellSelections() {
     checkWidget();
     final Point[] pts = _table.getCellSelection();
     final Point[] selection = new Point[pts.length];
     for (int i = 0; i < selection.length; i++) {
       final Point pt = pts[i];
       selection[i] = new Point(pt.x, computeModelRow(pt.y));
     }
 
     return selection;
   }
 
   public CellSelectionManager getCellSelectionManager() {
     return _cellSelectionManager;
   }
 
   public void setCellSelections(Point[] selected) {
     checkWidget();
     checkNull(selected);
 
     final BitSet rSelected = new BitSet();
     final Point[] tSelected = new Point[selected.length];
     for (int i = 0; i < tSelected.length; i++) {
       final Point pt = selected[i];
       tSelected[i] = new Point(pt.x, computeTableRow(pt.y));
       rSelected.set(selected[i].y);
     }
 
     _table.clearSelection();
     _table.setSelection(tSelected, false);
 
     final int[] selectedRows = ArraySupport.toArray(rSelected);
     final int[] update = ArraySupport.minus(selectedRows, _lastRowSelection);
     if (update.length > 0) {
       _lastRowSelection = selectedRows;
       doUpdateRows(update);
 
       final Event e = new Event();
       e.item = selected.length > 0 ? getItem(selected[0].y) : null;
       notifyListeners(SWT.Selection, e);
     }
 
     doUpdateScrollSelection();
     doUpdateScrollHighlights();
     doUpdateScrollData();
   }
 
   public void showSelection() {
     checkWidget();
     final TimeSeriesTableItem[] items = getSelection();
     if (items.length == 0)
       return;
 
     showItem(items[0]);
   }
 
   public void showItem(TimeSeriesTableItem item) {
     checkWidget();
     checkNull(item);
 
     final int index = indexOf(item);
     if (index < 0)
       return;
 
     final int tIndex = computeTableRow(index);
     final Rectangle r = _table.getVisibleCells();
     if (r.y <= tIndex && (r.y + r.height) > tIndex)
       return;
 
     if (tIndex < r.y) {
       _table.scroll(r.x, tIndex);
       return;
     }
 
     _table.scroll(r.x, tIndex - r.height + 2);
   }
 
   public void showColumn(int index) {
     checkWidget();
     checkColumnIndex(index);
 
     if (index < _columnCount)
       return;
 
     final Rectangle r = doGetVisibleDataCells();
     if (r.x <= index && (r.x + r.width) > index)
       return;
 
     if (index < r.x) {
       scrollColumnTo(index);
       return;
     }
 
     scrollColumnTo(index - r.width + 1);
   }
 
   public void scrollColumnTo(LocalDate date) {
     checkWidget();
     checkNull(date);
 
     int index = Arrays.binarySearch(_periods, date);
     if (index < 0) {
       index = -(index + 1);
     }
 
     scrollColumnTo(index + _columnCount);
   }
 
   private void scrollColumnTo(int index) {
     checkWidget();
     checkColumnIndex(index);
 
     if (index < _columnCount)
       return;
 
     final Rectangle r = doGetVisibleDataCells();
     final int row = Math.max(0, Math.min(r.y, _itemCount - _table.getVisibleRowCount() + 1));
     _table.scroll(index, row);
     _hscroll.setSelection(index - _columnCount);
   }
 
   public void setItemCount(int count) {
     checkWidget();
     final int c = Math.max(0, count);
     if (c == _itemCount)
       return;
 
     if (c > _itemCount) {
       for (int i = _itemCount; i < c; i++) {
         new TimeSeriesTableItem(this, i);
       }
       return;
     }
 
     for (int i = c; i < _itemCount; i++) {
       final TimeSeriesTableItem item = _items[i];
       if (item != null && !item.isDisposed())
         item.release();
       _items[i] = null;
     }
 
     final int length = Math.max(4, (c + 3) / 4 * 4);
     final TimeSeriesTableItem[] newItems = new TimeSeriesTableItem[length];
     System.arraycopy(_items, 0, newItems, 0, c);
     _items = newItems;
     _itemCount = c;
     _table.redraw();
   }
 
   public void remove(int start, int end) {
     checkWidget();
     if (start < 0 || start > end || end >= _itemCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     for (int i = end; i >= start; i--) {
       doRemove(i);
     }
     _table.redraw();
   }
 
   public void remove(int[] indices) {
     checkWidget();
     checkNull(indices);
     if (indices.length == 0)
       return;
 
     final int[] idxs = new int[indices.length];
     System.arraycopy(indices, 0, idxs, 0, idxs.length);
     Arrays.sort(idxs);
     for (int i = idxs.length - 1; i >= 0; i--) {
       doRemove(idxs[i]);
     }
     _table.redraw();
   }
 
   public void removeAll() {
     checkWidget();
     for (int i = 0; i < _itemCount; i++) {
       _items[i].release();
       _items[i] = null;
     }
     _itemCount = 0;
     _table.redraw();
   }
 
   public void clear(int index) {
     checkWidget();
     checkRowIndex(index);
 
     _items[index].clear();
     _table.redraw();
   }
 
   public void clearAll() {
     checkWidget();
     for (int i = 0; i < _itemCount; i++) {
       _items[i].clear();
     }
     _table.redraw();
   }
 
   public void moveColumn(int fromIndex, int toIndex) {
     checkWidget();
     checkColumnIndex(fromIndex);
     checkColumnIndex(toIndex);
     if (fromIndex == toIndex)
       return;
 
     if (!_columns[fromIndex].isMoveable() || !_columns[toIndex].isMoveable())
       return;
 
     final TimeSeriesTableColumn t = _columns[fromIndex];
     _columns[fromIndex] = _columns[toIndex];
     _columns[toIndex] = t;
 
     for (int i = 0; i < _itemCount; i++) {
       _items[i].swapColumns(fromIndex, toIndex);
     }
 
     _columns[fromIndex].notifyListeners(SWT.Move, new Event());
     _columns[toIndex].notifyListeners(SWT.Move, new Event());
 
     _table.redraw();
   }
 
   public void addSelectionListener(SelectionListener listener) {
     checkWidget();
     checkNull(listener);
 
     final TypedListener tListener = new TypedListener(listener);
     addListener(SWT.Selection, tListener);
     addListener(SWT.DefaultSelection, tListener);
   }
 
   public void removeSelectionListener(SelectionListener listener) {
     checkWidget();
     checkNull(listener);
 
     final TypedListener tListener = new TypedListener(listener);
     removeListener(SWT.Selection, tListener);
     removeListener(SWT.DefaultSelection, tListener);
   }
 
   @Override
   public void redraw() {
     checkWidget();
     if (_inUpdate)
       return;
 
     _requiresRedraw = true;
     super.redraw();
   }
 
   void createItem(TimeSeriesTableColumn item, int index) {
     checkWidget();
     if (index < 0 || index > _columnCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     if (_columns.length == _columnCount) {
       final TimeSeriesTableColumn[] newColumns = new TimeSeriesTableColumn[_columns.length + 4];
       System.arraycopy(_columns, 0, newColumns, 0, _columns.length);
       _columns = newColumns;
     }
 
     System.arraycopy(_columns, index, _columns, index + 1, _columnCount++ - index);
     _columns[index] = item;
   }
 
   void createItem(TimeSeriesTableItem item, int index) {
     checkWidget();
     if (index < 0 || index > _itemCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     if (_items.length == _itemCount) {
       final int length = Math.max(4, _items.length * 3 / 2);
       final TimeSeriesTableItem[] newItems = new TimeSeriesTableItem[length];
       System.arraycopy(_items, 0, newItems, 0, _items.length);
       _items = newItems;
     }
 
     System.arraycopy(_items, index, _items, index + 1, _itemCount++ - index);
     _items[index] = item;
   }
 
   NumberFormat getNumberFormat() {
     checkWidget();
     return _numberFormat;
   }
 
   Rectangle getBounds(TimeSeriesTableItem item, int index) {
     checkWidget();
     checkNull(item);
     checkColumnIndex(index);
 
     final int mRow = indexOf(item);
     final int tRow = computeTableRow(mRow);
     final Rectangle r = _table.getCellRect(index, tRow);
     final Point dPoint = _table.toDisplay(r.x, r.y);
     final Point pt = this.toControl(dPoint);
     return new Rectangle(pt.x, pt.y, r.width, r.height);
   }
 
   Rectangle getBounds(TimeSeriesTableItem item) {
     checkWidget();
     checkNull(item);
 
     final int mRow = indexOf(item);
     final int tRow = computeTableRow(mRow);
     Rectangle bounds = null;
     for (int i = 0; i < _columnCount; i++) {
       final Rectangle r = _table.getCellRect(i, tRow);
       if (bounds == null) {
         final Point dPoint = _table.toDisplay(r.x, r.y);
         final Point pt = this.toControl(dPoint);
         bounds = new Rectangle(pt.x, pt.y, 0, 0);
       }
 
       bounds.width += r.width;
       bounds.height = Math.max(bounds.height, r.height);
     }
 
     return bounds;
   }
 
   Rectangle getImageBounds(TimeSeriesTableItem item, int index) {
     checkWidget();
     checkNull(item);
     checkColumnIndex(index);
 
     final Rectangle r = getBounds(item, index);
     r.width = 0;
     r.height = 0;
     return r;
   }
 
   Composite getTableComposite() {
     checkWidget();
     return _table;
   }
 
   int getVisibleRowCount() {
     checkWidget();
     final KTableModel model = _table.getModel();
     return _table.getVisibleRowCount() - model.getFixedHeaderRowCount() - model.getFixedSelectableRowCount();
   }
 
   int getVisibleColumnCount() {
     checkWidget();
     return doGetVisibleDataCells().width;
   }
 
   // TODO consolidate with getVisibleRowCount and getVisibleColumnCount
   private Rectangle doGetVisibleDataCells() {
     final Rectangle r = _table.getVisibleCells();
 
     if (r.width > 0 && (_showHeader || r.y < _itemCount)) {
       final int y = _showHeader ? 0 : r.y;
       if (!_table.isCellFullyVisible(r.x + r.width - 1, y)) {
         r.width--;
       }
     }
 
     if (r.height > 0 && (_columnCount > 0 || r.x < _periods.length)) {
       final int x = (_columnCount > 0) ? 0 : r.x;
       if (!_table.isCellFullyVisible(x, r.y + r.height - 1)) {
         r.height--;
       }
     }
 
     final int correction = _columnCount + _periods.length - r.x - r.width;
     if (correction < 0) {
       // this is required because KTable reports more visible columns
       // when scrolled all the way to the right.
       r.width += correction;
     }
 
     return r;
   }
 
   private int computeTableRow(int row) {
     if (_showHeader)
       return row + _table.getModel().getFixedHeaderRowCount();
 
     return row;
   }
 
   private int computeModelRow(int row) {
     if (_showHeader)
       return row - _table.getModel().getFixedHeaderRowCount();
 
     return row;
   }
 
   private void hookControls() {
     _table.addCellResizeListener(_listener);
     _table.addPaintListener(_listener);
     _hscroll.addSelectionListener(_listener);
 
     addPaintListener(_listener);
     addDisposeListener(_listener);
   }
 
   private void releaseControls() {
     _table.removeCellResizeListener(_listener);
     _table.removePaintListener(_listener);
     _hscroll.removeSelectionListener(_listener);
 
     removePaintListener(_listener);
     removeDisposeListener(_listener);
   }
 
   private void checkNull(Object o) {
     if (o == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
   }
 
   private void checkColumnIndex(int index) {
     if (index < 0 || index >= (_columnCount + Math.max(1, _periods.length)))
       SWT.error(SWT.ERROR_INVALID_RANGE);
   }
 
   private void checkRowIndex(int index) {
     if (index < 0 || index >= _itemCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
   }
 
   private void doUpdateScrollSelection() {
     final Point focus = _cellSelectionManager.getFocusCell();
     if (focus == null || focus.x < _columnCount)
       return;
 
     final Rectangle r = doGetVisibleDataCells();
     if (focus.x < r.x) {
       _hscroll.setSelection(focus.x - _columnCount);
       return;
     }
 
     if (focus.x < (r.x + r.width))
       return;
 
     final int delta = focus.x - r.x - r.width + 1;
     _hscroll.setSelection(r.x + delta - _columnCount);
   }
 
   private void doUpdateScrollHighlights() {
     final BitSet selectedColumns = new BitSet();
     for (Point p : _table.getCellSelection()) {
       if (p.x < _columnCount)
         continue;
 
       if (_showHeader && p.y == 0)
         continue;
 
       selectedColumns.set(p.x - _columnCount);
     }
 
     final int[] indices = ArraySupport.toArray(selectedColumns);
     _hscroll.setHighlights(indices);
   }
 
   private void doUpdateScrollData() {
     final int[] indices;
     switch (_scrollDataMode) {
       case FOCUS_CELL:
         final Point focus = _cellSelectionManager.getFocusCell();
         if (focus != null && focus.y >= 0 && focus.y < _itemCount) {
           indices = new int[] { focus.y };
         } else {
           indices = new int[] {};
         }
         break;
 
       case SELECTED_ROWS:
         indices = getSelectionIndices();
         break;
 
       default:
         indices = new int[] {};
     }
 
     final double[] data = new double[_periods.length];
     Arrays.fill(data, 0.0);
     for (int index : indices) {
       for (int j = 0; j < data.length; j++) {
         data[j] += _items[index].getValue(j);
       }
     }
     _hscroll.setDataPoints(data);
   }
 
   private void doUpdateRows(int[] indices) {
     if (indices.length <= 0)
       return;
 
     final int width = doGetVisibleDataCells().width + _columnCount + 1;
     Arrays.sort(indices);
 
     int previous = computeTableRow(indices[0]);
     int height = 1;
     for (int i = 1; i < indices.length; i++) {
       final int index = computeTableRow(indices[i]);
       final int delta = index - previous;
       if (delta <= 1) {
         height += delta;
         previous = index;
         continue;
       }
 
       _table.redraw(0, previous - height + 1, width, height);
       previous = index;
     }
 
     _table.redraw(0, previous - height + 1, width, height);
   }
 
   private void doRemove(int index) {
     _items[index].release();
 
     System.arraycopy(_items, index + 1, _items, index, --_itemCount - index);
     _items[_itemCount] = null;
   }
 
   private final class TimeSeriesTableModel extends KTableDefaultModel {
 
     @Override
     public int getFixedHeaderColumnCount() {
       return 0;
     }
 
     @Override
     public int getFixedHeaderRowCount() {
       return _showHeader ? 1 : 0;
     }
 
     @Override
     public int getFixedSelectableColumnCount() {
       return _columnCount;
     }
 
     @Override
     public int getFixedSelectableRowCount() {
       return 0;
     }
 
     @Override
     public int getRowHeightMinimum() {
       return 0;
     }
 
     @Override
     public boolean isColumnResizable(int col) {
       return getColumn(col).isVisible();
     }
 
     @Override
     public boolean isRowResizable(int row) {
       return false;
     }
 
     @Override
     public KTableCellEditor doGetCellEditor(int col, int row) {
       // Not used
       return null;
     }
 
     private final FixedCellRenderer _headerRenderer = new FixedCellRenderer(SWT.BOLD | DefaultCellRenderer.INDICATION_FOCUS_ROW);
     private final TextCellRenderer _renderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS);
     private final CheckableCellRenderer _checkRenderer = new CheckableCellRenderer(DefaultCellRenderer.INDICATION_FOCUS);
 
     @Override
     public KTableCellRenderer doGetCellRenderer(int col, int row) {
       if (_showHeader && row == 0) {
         _headerRenderer.setDefaultBackground(getBackground());
         _headerRenderer.setDefaultForeground(getForeground());
         _headerRenderer.setFont(getFont());
         return _headerRenderer;
       }
 
       final TimeSeriesTableColumn column = getColumn(col);
       final DefaultCellRenderer renderer;
       if ((SWT.CHECK & column.getStyle()) > 0) {
         renderer = _checkRenderer;
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER | SWTX.ALIGN_VERTICAL_CENTER);
       } else {
         renderer = _renderer;
         if (column == _periodColumn) {
           renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
         } else {
           renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_CENTER);
         }
       }
 
       final int style = column.getStyle();
       if ((style & SWT.LEFT) > 0) {
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_CENTER);
       } else if ((style & SWT.RIGHT) > 0) {
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
       } else if ((style & SWT.CENTER) > 0) {
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER | SWTX.ALIGN_VERTICAL_CENTER);
       }
 
       final int modelRow = computeModelRow(row);
       final TimeSeriesTableItem item = getItem(modelRow);
       renderer.setDefaultBackground(item.getBackground(col));
       renderer.setDefaultForeground(item.getForeground(col));
       renderer.setFont(item.getFont(col));
 
       return renderer;
     }
 
     @Override
     public int doGetColumnCount() {
       return getFixedColumnCount() + _columnCount + _periods.length;
     }
 
     @Override
     public Object doGetContentAt(int col, int row) {
       if (col < 0 || col >= (_columnCount + _periods.length))
         return "";
 
       final TimeSeriesTableColumn column = getColumn(col);
       if (_showHeader && row == 0) {
         return column.getText();
       }
 
       final TimeSeriesTableItem item = _items[computeModelRow(row)];
       final String text = item.getText(col);
       if ((SWT.CHECK & column.getStyle()) > 0)
         return Boolean.valueOf(text);
 
       if (text == null)
         return "";
 
       return text;
     }
 
     @Override
     public int doGetRowCount() {
       return getFixedRowCount() + _itemCount;
     }
 
     @Override
     public void doSetContentAt(int col, int row, Object newValue) {
       // Not used
     }
 
     @Override
     public int getColumnWidth(int col) {
       if (col < 0 || col >= (_columnCount + _periods.length))
         return 0;
 
       final TimeSeriesTableColumn column = getColumn(col);
       if (!column.isVisible())
         return 0;
 
       return column.getWidth();
     }
 
     @Override
     public void setColumnWidth(int col, int value) {
       if (!isColumnResizable(col))
         return;
 
       final TimeSeriesTableColumn column = getColumn(col);
       column.setWidth(value);
     }
 
     @Override
     public int getInitialColumnWidth(int col) {
       throw new UnsupportedOperationException();
     }
 
     @Override
     public int getInitialRowHeight(int row) {
       return _rowHeight;
     }
   }
 
   private final class TimeSeriesTableListener implements KTableCellResizeListener, SelectionListener, PaintListener, DisposeListener {
 
     @Override
     public void widgetSelected(SelectionEvent e) {
       if (e.getSource() != _hscroll)
         return;
 
       final int selection = _hscroll.getSelection();
       if (selection < 0 || selection >= _periods.length)
         return;
 
       _hscroll.setLabel(_dateFormat.format(_periods[selection]));
       scrollColumnTo(_hscroll.getSelection() + _columnCount);
     }
 
     @Override
     public void widgetDefaultSelected(SelectionEvent e) {
       // Ignore event
     }
 
     @Override
     public void paintControl(PaintEvent e) {
       if (e.getSource() == TimeSeriesTable.this && _requiresRedraw) {
         _requiresRedraw = false;
         _table.redraw();
       }
 
       if (e.getSource() != _table)
         return;
 
       final Rectangle visible = doGetVisibleDataCells();
       if (visible.width <= 0) {
         _hscroll.setThumb(_hscroll.getMaximum() + 1);
         _hscroll.setEnabled(false);
         return;
       }
 
       _hscroll.setThumb(Math.max(1, visible.width));
       _hscroll.setEnabled(true);
     }
 
     @Override
     public void columnResized(int col, int newWidth) {
       if (col < _columnCount) {
         _columns[col].notifyListeners(SWT.Resize, new Event());
       }
     }
 
     @Override
     public void rowResized(int row, int newHeight) {
       _items[computeModelRow(row)].notifyListeners(SWT.Resize, new Event());
     }
 
     @Override
     public void widgetDisposed(DisposeEvent e) {
       releaseControls();
       _defaultFont.dispose();
     }
   }
 
   final class KTableImpl extends KTable {
 
     private boolean _ignoreMouseMove = false;
 
     private KTableImpl(Composite parent, int style) {
       super(parent, style);
     }
 
     void setIgnoreMouseMove(boolean b) {
       _ignoreMouseMove = b;
     }
 
     @Override
     protected void onMouseMove(MouseEvent e) {
       if (_ignoreMouseMove)
         return;
 
       super.onMouseMove(e);
     }
 
     @Override
     protected void onMouseDoubleClick(MouseEvent e) {
       // Disable default double click event handling
     }
 
     @Override
     protected void onMouseDown(MouseEvent e) {
       // Disable default event handling
 
       if (e.button == 1) {
         setCapture(true);
         m_Capture = true;
 
         // Resize column?
         int columnIndex = getColumnForResize(e.x, e.y);
         if (columnIndex >= 0) {
           m_ResizeColumnIndex = columnIndex;
           m_ResizeColumnLeft = getColumnLeft(columnIndex);
           return;
         }
       }
     }
 
     @Override
     protected void onKeyDown(KeyEvent e) {
       // Disable default even handling
     }
   }
 }
