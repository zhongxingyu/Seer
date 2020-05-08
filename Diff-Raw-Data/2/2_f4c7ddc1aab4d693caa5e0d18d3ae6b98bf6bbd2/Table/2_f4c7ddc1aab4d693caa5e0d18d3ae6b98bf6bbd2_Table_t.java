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
 
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.Comparator;
 
 import net.karlmartens.platform.util.ArraySupport;
 import net.karlmartens.platform.util.NumberStringComparator;
 
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.TypedListener;
 
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
 
 public final class Table extends Composite {
 
   public static final String DATA_COLUMN = "TimeSeriesTable.Column";
 
   public static final String GROUP_COMMAND = "TimeSeriesTable.Group.Command";
   public static final String GROUP_VISIBLE_COLUMNS = "TimeSeriesTable.Group.VisibleColumns";
 
   public static final int SORT_DECENDING = -1;
   public static final int SORT_ASCENDING = 1;
 
   private final TableColumnManager _columnManager;
   private final CellSelectionManager _cellSelectionManager;
 
   private final KTableImpl _table;
   private final TableListener _listener;
 
   private boolean _requiresRedraw = true;
   private boolean _showHeader = false;
   private int _rowHeight;
 
   private int _fixedColumnCount = 0;
   private int _itemCount = 0;
   private TableItem[] _items = new TableItem[0];
   private int _columnCount = 0;
   private TableColumn[] _columns = new TableColumn[0];
   private int[] _lastRowSelection = new int[0];
 
   public Table(Composite parent) {
     this(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
   }
 
   public Table(Composite parent, int style) {
     super(parent, checkStyle(style));
 
     _listener = new TableListener();
     updateFontData();
 
     _table = new KTableImpl(this, style | SWTX.MARK_FOCUS_HEADERS);
     _table.setBackground(getBackground());
     _table.setForeground(getForeground());
     _table.setModel(_model);
     
     _cellSelectionManager = new CellSelectionManager(this);
     _columnManager = new TableColumnManager(this, _table);
 
     final PassthoughEventListener passthroughListener = new PassthoughEventListener(this);
     passthroughListener.addSource(_table);
 
     hookControls();
   }
   
   public IMenuManager getColumnMenuManager() {
     checkWidget();
     return _columnManager.getMenuManager();
   }
   
   @Override
   public void setBackground(Color color) {
     super.setBackground(color);
     _table.setBackground(color);
   }
 
   @Override
   public void setFont(Font font) {
     super.setFont(font);
     updateFontData();
   }
 
   @Override
   public void setForeground(Color color) {
     super.setForeground(color);
     _table.setForeground(color);
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
 
   @Override
   public void redraw() {
     checkWidget();
     _table.redraw();
     super.redraw();
   }
 
   public void setHeaderVisible(boolean show) {
     checkWidget();
     _showHeader = show;
     redraw();
   }
 
   public int getFixedColumnCount() {
     checkWidget();
     return _fixedColumnCount;
   }
 
   public void setFixedColumnCount(int count) {
     checkWidget();
     if (count < 0 || count > _columnCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     _fixedColumnCount = count;
     redraw();
   }
 
   public int getItemCount() {
     checkWidget();
     return _itemCount;
   }
 
   private int _lastIndexOf = -1;
 
   public int indexOf(TableItem item) {
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
 
   public TableItem getItem(int index) {
     checkWidget();
     checkRowIndex(index);
     return _items[index];
   }
 
   public TableItem getItem(Point point) {
     checkWidget();
     checkNull(point);
 
     final Point dPoint = this.toDisplay(point);
     final Point tPoint = _table.toControl(dPoint);
     final Point cell = _table.getCellForCoordinates(tPoint.x, tPoint.y);
     final int row = computeRow(cell.y);
     if (row < 0)
       return null;
 
     return _items[row];
   }
 
   public TableItem[] getItems() {
     checkWidget();
     final TableItem[] items = new TableItem[_itemCount];
     System.arraycopy(_items, 0, items, 0, items.length);
     return items;
   }
 
   public int getColumnCount() {
     checkWidget();
     return _columnCount;
   }
 
   public int indexOf(TableColumn column) {
     checkWidget();
     checkNull(column);
 
     for (int i = 0; i < _columnCount; i++) {
       if (_columns[i] == column) {
         return i;
       }
     }
 
     return -1;
   }
 
   public TableColumn getColumn(int index) {
     checkWidget();
     checkColumnIndex(index);
     return _columns[index];
   }
 
   public int[] getSelectionIndices() {
     checkWidget();
 
     final BitSet selectedRows = new BitSet();
     for (Point selection : _table.getCellSelection()) {
       if (_showHeader && selection.y < _table.getModel().getFixedHeaderRowCount())
         continue;
 
       selectedRows.set(computeRow(selection.y));
     }
 
     return ArraySupport.toArray(selectedRows);
   }
 
   public TableItem[] getSelection() {
     checkWidget();
 
     final int[] indices = getSelectionIndices();
     final TableItem[] selected = new TableItem[indices.length];
     for (int i = 0; i < indices.length; i++) {
       selected[i] = _items[indices[i]];
     }
 
     return selected;
   }
 
   public Rectangle getVisibleScrollableCells() {
     final KTableModel model = _table.getModel();
 
     final Rectangle rect = _table.getVisibleCells();
     final Point tableTopLeft = new Point(rect.x, rect.y);
     rect.y = computeRow(rect.y);
 
     // Required because KTable report more visible column when scrolled
     // all the way to the right.
     final int hCorrection = Math.min(_columnCount - (rect.x + rect.width), 0);
     rect.width += hCorrection;
 
     // Adjust height for fully visible rows
     final int rows = _table.getVisibleRowCount() - model.getFixedHeaderRowCount() - model.getFixedSelectableRowCount();
     rect.height = Math.min(rows, rect.height);
 
     // Adjust height for fully visible columns
    while (!_table.isCellFullyVisible(tableTopLeft.x + rect.width - 1, tableTopLeft.y + rect.height - 1) && rect.width > 0) {
       rect.width--;
     }
 
     return rect;
   }
 
   public int getVisibleColumnCount() {
     checkWidget();
     return getVisibleScrollableCells().width;
   }
 
   public int getVisibleRowCount() {
     checkWidget();
     return getVisibleScrollableCells().height;
   }
 
   public void deselectAll() {
     checkWidget();
     _table.clearSelection();
     notifyListeners(SWT.Selection, new Event());
   }
 
   public void setSelection(TableItem[] items) {
     checkWidget();
     checkNull(items);
 
     final int[] indices = new int[items.length];
     int i = 0;
     for (TableItem item : items) {
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
 
     final int width = _columnCount;
     final Point[] selections = new Point[indices.length * width];
     for (int i = 0; i < indices.length; i++) {
       for (int j = 0; j < width; j++) {
         selections[i * width + j] = new Point(j, computeKTableRow(indices[i]));
       }
     }
 
     _table.clearSelection();
     _table.setSelection(selections, false);
 
     final int[] newRowSelection = Arrays.copyOf(indices, indices.length);
     Arrays.sort(newRowSelection);
     _lastRowSelection = newRowSelection;
 
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
       selection[i] = new Point(pt.x, computeRow(pt.y));
     }
 
     return selection;
   }
 
   public Point getFocusCell() {
     checkWidget();
     return _cellSelectionManager.getFocusCell();
   }
 
   public void setFocusCell(Point cell, boolean multi) {
     checkWidget();
     _cellSelectionManager.setFocusCell(cell, multi);
   }
 
   public void setCellSelections(Point[] selected) {
     checkWidget();
     checkNull(selected);
 
     final BitSet rSelected = new BitSet();
     final Point[] tSelected = new Point[selected.length];
     for (int i = 0; i < tSelected.length; i++) {
       final Point pt = selected[i];
       tSelected[i] = new Point(pt.x, computeKTableRow(pt.y));
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
   }
 
   public void showSelection() {
     checkWidget();
     final TableItem[] items = getSelection();
     if (items.length == 0)
       return;
 
     showItem(items[0]);
   }
 
   public void showItem(TableItem item) {
     checkWidget();
     checkNull(item);
 
     final int index = indexOf(item);
     if (index < 0)
       return;
 
     final Rectangle r = getVisibleScrollableCells();
     if (r.y <= index && (r.y + r.height) > index)
       return;
 
     if (index < r.y) {
       scroll(new Point(r.x, index));
       return;
     }
 
     scroll(new Point(r.x, index - r.height + 1));
   }
 
   public void showColumn(int index) {
     checkWidget();
     checkColumnIndex(index);
 
     if (index < _fixedColumnCount)
       return;
 
     final Rectangle r = getVisibleScrollableCells();
     if (r.x <= index && (r.x + r.width) > index)
       return;
 
     if (index < r.x) {
       scroll(new Point(index, r.y));
       return;
     }
 
     scroll(new Point(index - r.width + 1, r.y));
   }
 
   public void scroll(Point cell) {
     checkWidget();
     checkNull(cell);
     
     _table.scroll(cell.x, computeKTableRow(cell.y));
   }
 
   public void setItemCount(int count) {
     checkWidget();
     final int c = Math.max(0, count);
     if (c == _itemCount)
       return;
 
     if (c > _itemCount) {
       for (int i = _itemCount; i < c; i++) {
         new TableItem(this, i);
       }
       return;
     }
 
     for (int i = c; i < _itemCount; i++) {
       final TableItem item = _items[i];
       if (item != null && !item.isDisposed())
         item.release();
       _items[i] = null;
     }
 
     final int length = Math.max(4, (c + 3) / 4 * 4);
     final TableItem[] newItems = new TableItem[length];
     System.arraycopy(_items, 0, newItems, 0, c);
     _items = newItems;
     _itemCount = c;
     _table.redraw();
   }
 
   public void setColumnCount(int count) {
     checkWidget();
     final int c = Math.max(0, count);
     if (c == _columnCount)
       return;
 
     if (c > _columnCount) {
       for (int i = _columnCount; i < c; i++) {
         new TableColumn(this, SWT.NONE, i);
       }
       return;
     }
 
     for (int i = c; i < _columnCount; i++) {
       final TableColumn column = _columns[i];
       if (column != null && !column.isDisposed()) {
         column.release();
       }
 
       _columns[i] = null;
     }
 
     final int length = Math.max(4, (c + 3) / 4 * 4);
     final TableColumn[] newColumns = new TableColumn[length];
     System.arraycopy(_columns, 0, newColumns, 0, c);
     _columns = newColumns;
     _columnCount = c;
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
 
     final TableColumn t = _columns[fromIndex];
     _columns[fromIndex] = _columns[toIndex];
     _columns[toIndex] = t;
 
     for (int i = 0; i < _itemCount; i++) {
       _items[i].swapColumns(fromIndex, toIndex);
     }
 
     _columns[fromIndex].notifyListeners(SWT.Move, new Event());
     _columns[toIndex].notifyListeners(SWT.Move, new Event());
 
     _table.redraw();
   }
 
   public void sort(int index) {
     checkWidget();
     checkColumnIndex(index);
 
     if (_itemCount <= 1)
       return;
 
     final NumberStringComparator comparator = new NumberStringComparator();
     for (int i = 1; i < _itemCount; i++) {
       final TableItem first = _items[i - 1];
       final TableItem second = _items[i];
       if (comparator.compare(first.getText(index), second.getText(index)) > 0) {
         sort(index, SORT_ASCENDING);
         return;
       }
     }
 
     sort(index, SORT_DECENDING);
   }
 
   public void sort(int index, int direction) {
     checkWidget();
     checkColumnIndex(index);
     if (direction != SORT_ASCENDING && direction != SORT_DECENDING)
       SWT.error(SWT.ERROR_INVALID_ARGUMENT);
 
     if (_itemCount <= 1)
       return;
 
     final TableItem[] newItems = Arrays.copyOf(_items, _items.length);
     final Comparator<TableItem> comparator = new TableItemComparator(new NumberStringComparator(), index, direction);
     Arrays.sort(newItems, 0, _itemCount, comparator);
 
     _items = newItems;
     notifyListeners(SWT.Selection, new Event());
     redraw();
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
 
   public void addColumnSortSupport() {
     _columnManager.enableColumnSort();
   }
 
   void createItem(TableColumn item, int index) {
     checkWidget();
     if (index < 0 || index > _columnCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     if (_columns.length == _columnCount) {
       final TableColumn[] newColumns = new TableColumn[_columns.length + 4];
       System.arraycopy(_columns, 0, newColumns, 0, _columns.length);
       _columns = newColumns;
     }
 
     System.arraycopy(_columns, index, _columns, index + 1, _columnCount++ - index);
     _columns[index] = item;
   }
 
   void createItem(TableItem item, int index) {
     checkWidget();
     if (index < 0 || index > _itemCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     if (_items.length == _itemCount) {
       final int length = Math.max(4, _items.length * 3 / 2);
       final TableItem[] newItems = new TableItem[length];
       System.arraycopy(_items, 0, newItems, 0, _items.length);
       _items = newItems;
     }
 
     System.arraycopy(_items, index, _items, index + 1, _itemCount++ - index);
     _items[index] = item;
   }
 
   Rectangle getBounds(TableItem item, int index) {
     checkWidget();
     checkNull(item);
     checkColumnIndex(index);
 
     final int mRow = indexOf(item);
     final int tRow = computeKTableRow(mRow);
     final Rectangle r = _table.getCellRect(index, tRow);
     final Point dPoint = _table.toDisplay(r.x, r.y);
     final Point pt = this.toControl(dPoint);
     return new Rectangle(pt.x, pt.y, r.width, r.height);
   }
 
   Rectangle getBounds(TableItem item) {
     checkWidget();
     checkNull(item);
 
     final int mRow = indexOf(item);
     final int tRow = computeKTableRow(mRow);
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
 
   Rectangle getImageBounds(TableItem item, int index) {
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
 
   private void hookControls() {
     _table.addCellResizeListener(_listener);
     _table.addPaintListener(_listener);
 
     
     addControlListener(_listener);
     addPaintListener(_listener);
     addDisposeListener(_listener);
   }
 
   private void releaseControls() {
     _table.removeCellResizeListener(_listener);
     _table.removePaintListener(_listener);
 
     removePaintListener(_listener);
     removeDisposeListener(_listener);
   }
 
   private void doUpdateRows(int[] indices) {
     if (indices.length <= 0)
       return;
 
     final int width = getVisibleScrollableCells().width + _fixedColumnCount + 1;
     Arrays.sort(indices);
 
     int previous = computeKTableRow(indices[0]);
     int height = 1;
     for (int i = 1; i < indices.length; i++) {
       final int index = computeKTableRow(indices[i]);
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
 
   private void updateFontData() {
     final GC gc = new GC(getShell());
     gc.setFont(getFont());
     _rowHeight = gc.getFontMetrics().getHeight() + 6;
     gc.dispose();
   }
 
   private int computeRow(int ktableRow) {
     if (_showHeader)
       return ktableRow - _table.getModel().getFixedHeaderRowCount();
 
     return ktableRow;
   }
 
   private int computeKTableRow(int row) {
     if (_showHeader)
       return row + _table.getModel().getFixedHeaderRowCount();
 
     return row;
   }
   
   private static int checkStyle(int style) {
     final int mask = SWT.BORDER | SWT.MULTI;
     return style & mask;
   }
 
   private void checkColumnIndex(int index) {
     if (index < 0 || index >= _columnCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
   }
 
   private void checkRowIndex(int index) {
     if (index < 0 || index >= _itemCount)
       SWT.error(SWT.ERROR_INVALID_RANGE);
   }
 
   private void checkNull(Object o) {
     if (o == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
   }
 
   private final KTableModel _model = new KTableDefaultModel() {
 
     @Override
     public int getFixedHeaderRowCount() {
       return _showHeader ? 1 : 0;
     }
 
     @Override
     public int getFixedSelectableRowCount() {
       return 0;
     }
 
     @Override
     public int getFixedHeaderColumnCount() {
       return 0;
     }
 
     @Override
     public int getFixedSelectableColumnCount() {
       return _fixedColumnCount;
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
     public int getRowHeightMinimum() {
       return 0;
     }
 
     public int getColumnWidth(int col) {
       if (col < 0 || col >= _columnCount)
         return 0;
 
       final TableColumn column = getColumn(col);
       if (!column.isVisible())
         return 0;
 
       return column.getWidth();
     }
 
     public void setColumnWidth(int col, int value) {
       if (!isColumnResizable(col))
         return;
 
       final TableColumn column = getColumn(col);
       column.setWidth(value);
     }
 
     @Override
     public int getInitialColumnWidth(int column) {
       throw new UnsupportedOperationException();
     }
 
     @Override
     public int getInitialRowHeight(int row) {
       return _rowHeight;
     }
 
     @Override
     public Object doGetContentAt(int col, int row) {
       // This seems weird but it a KTable behaviour to ask for data that
       // doesn't exist.
       if (col < 0 || col >= _columnCount)
         return "";
 
       final TableColumn column = getColumn(col);
       if (_showHeader && row == 0)
         return column.getText();
 
       final TableItem item = getItem(computeRow(row));
       final String text = item.getText(col);
       if ((SWT.CHECK & column.getStyle()) > 0)
         return Boolean.valueOf(text);
 
       if (text == null)
         return "";
 
       return text;
     }
 
     @Override
     public KTableCellEditor doGetCellEditor(int col, int row) {
       // Not used
       return null;
     }
 
     @Override
     public void doSetContentAt(int col, int row, Object value) {
       // Not used
     }
 
     private final FixedCellRenderer _headerRenderer = new FixedCellRenderer(SWT.BOLD | DefaultCellRenderer.INDICATION_FOCUS_ROW);
     private final TextCellRenderer _renderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS);
     private final CheckableCellRenderer _checkRenderer = new CheckableCellRenderer(DefaultCellRenderer.INDICATION_FOCUS);
 
     @Override
     public KTableCellRenderer doGetCellRenderer(int col, int row) {
       if (getFixedRowCount() > 0 && row == 0) {
         _headerRenderer.setDefaultBackground(getBackground());
         _headerRenderer.setDefaultForeground(getForeground());
         _headerRenderer.setFont(getFont());
         return _headerRenderer;
       }
 
       if (col < 0 || col >= _columnCount)
         return _renderer;
 
       final TableColumn column = getColumn(col);
       final DefaultCellRenderer renderer;
       if ((SWT.CHECK & column.getStyle()) > 0) {
         renderer = _checkRenderer;
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER | SWTX.ALIGN_VERTICAL_CENTER);
       } else {
         renderer = _renderer;
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_CENTER);
       }
 
       final int style = column.getStyle();
       if ((style & SWT.LEFT) > 0) {
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_CENTER);
       } else if ((style & SWT.RIGHT) > 0) {
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
       } else if ((style & SWT.CENTER) > 0) {
         renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER | SWTX.ALIGN_VERTICAL_CENTER);
       }
 
       final int modelRow = computeRow(row);
       final TableItem item = getItem(modelRow);
       renderer.setDefaultBackground(item.getBackground(col));
       renderer.setDefaultForeground(item.getForeground(col));
       renderer.setFont(item.getFont(col));
 
       return renderer;
     }
 
     @Override
     public int doGetRowCount() {
       return getFixedRowCount() + _itemCount;
     }
 
     @Override
     public int doGetColumnCount() {
       return _columnCount;
     }
 
   };
 
   private final class TableListener implements ControlListener, KTableCellResizeListener, PaintListener, DisposeListener {
 
     @Override
     public void controlMoved(ControlEvent e) {
       // Ignore
     }
     
     @Override
     public void controlResized(ControlEvent e) {
       if (e.getSource() == Table.this) {
         final Rectangle ca = getClientArea();
         _table.setBounds(0, 0, ca.width, ca.height);
         _table.redraw();
       }
     }
     
     @Override
     public void paintControl(PaintEvent e) {
       if (e.getSource() == Table.this && _requiresRedraw) {
         _requiresRedraw = false;
         _table.redraw();
       }
     }
 
     @Override
     public void columnResized(int col, int newWidth) {
       if (col < _columnCount) {
         _columns[col].notifyListeners(SWT.Resize, new Event());
       }
     }
 
     @Override
     public void rowResized(int row, int newHeight) {
       _items[computeRow(row)].notifyListeners(SWT.Resize, new Event());
     }
 
     @Override
     public void widgetDisposed(DisposeEvent e) {
       releaseControls();
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
