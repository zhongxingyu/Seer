 /**
  *  net.karlmartens.ui, is a library of UI widgets
  *  Copyright (C) 2010,2011
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
 package net.karlmartens.ui.widget;
 
 import static net.karlmartens.ui.Images.ARROW_BOTTOM;
 import static net.karlmartens.ui.Images.ARROW_DOWN;
 import static net.karlmartens.ui.Images.ARROW_LEFT;
 import static net.karlmartens.ui.Images.ARROW_RIGHT;
 import static net.karlmartens.ui.Images.ARROW_TOP;
 import static net.karlmartens.ui.Images.ARROW_UP;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableFontProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TableDragSourceEffect;
 import org.eclipse.swt.dnd.TableDropTargetEffect;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.TypedListener;
 
 public final class GridChooser extends Composite {
 
 	private final PassthoughEventListener _passthroughListener;
 	private final TableViewer _available;
 	private final TableViewer _selected;
 	private final Button _left;
 	private final Button _right;
 	private final Button _top;
 	private final Button _up;
 	private final Button _down;
 	private final Button _bottom;
 	
 	private Image[] _images;
 	private int _columnCount;
 	private int _itemCount;
 	private GridChooserColumn[] _columns;
 	private GridChooserItem[] _items;
 	private int _lastIndexOf;
 
 	public GridChooser(Composite parent) {
 		super(parent, SWT.NONE);
 		final ResourceBundle bundle = ResourceBundle.getBundle("net.karlmartens.ui.locale.messages");
 		
 		_columnCount = 0;
 		_columns = new GridChooserColumn[0];
 		
 		_itemCount = 0;
 		_items = new GridChooserItem[0];
 		
 		_images = new Image[] {
 				ARROW_LEFT.createImage(), //
 				ARROW_RIGHT.createImage(), //
 				ARROW_TOP.createImage(), //
 				ARROW_UP.createImage(), //
 				ARROW_DOWN.createImage(), //
 				ARROW_BOTTOM.createImage() //
 		};
 		
 		setLayout(new FormLayout());
 		
 		_passthroughListener = new PassthoughEventListener(this);
 		
 		final Label availableLabel = new Label(this, SWT.NONE);
 		availableLabel.setText(bundle.getString("GridChooser.Available"));
 		
 		final int style = SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER;
 		_available = new TableViewer(this, style);
 		_available.setContentProvider(ArrayContentProvider.getInstance());
 		_available.setLabelProvider(new TableLabelProviderImpl());
 		_available.addFilter(new ItemSelectionFilter(false));
 		_available.addSelectionChangedListener(_selectionChangedListener);
 		new DragSourceListenerImpl(_available);
 		new SelectedDropTargetListener(_available, false);
 		_passthroughListener.addSource(_available.getControl());
 		
 		final Composite centerPart = new Composite(this, SWT.NONE);
 		centerPart.setLayout(new RowLayout(SWT.VERTICAL));
 		
 		_left = createButton(centerPart, _images[0], _changeSelectionListener);
 		_right = createButton(centerPart, _images[1], _changeSelectionListener);
 		_top = createButton(centerPart, _images[2], _changeSelectionListener);
 		_up = createButton(centerPart, _images[3], _changeSelectionListener);
 		_down = createButton(centerPart, _images[4], _changeSelectionListener);
 		_bottom = createButton(centerPart, _images[5], _changeSelectionListener);
 		
 		final Label selectedLabel = new Label(this, SWT.NONE);
 		selectedLabel.setText(bundle.getString("GridChooser.Selected"));
 				
 		_selected = new TableViewer(this, style);
 		_selected.setContentProvider(ArrayContentProvider.getInstance());
 		_selected.setLabelProvider(new TableLabelProviderImpl());
 		_selected.addFilter(new ItemSelectionFilter(true));
 		_selected.addSelectionChangedListener(_selectionChangedListener);
 		_selected.setComparator(new SelectionOrderViewerComparator());
 		new DragSourceListenerImpl(_selected);
 		new SelectedDropTargetListener(_selected, true);
 		_passthroughListener.addSource(_selected.getControl());
 		
 		final FormData availableLabelFormData = new FormData();
 		availableLabelFormData.top = new FormAttachment(0, 100, 10);
 		availableLabelFormData.left = new FormAttachment(0, 100, 10);
 		
 		final FormData availableFormData = new FormData();
 		availableFormData.top = new FormAttachment(availableLabel, 5, SWT.BOTTOM);
 		availableFormData.bottom = new FormAttachment(100, 100, -10);
 		availableFormData.left = new FormAttachment(0, 100, 10);
 		availableFormData.right = new FormAttachment(centerPart, -10, SWT.LEFT);
 		
 		final FormData centerFormData = new FormData();
 		centerFormData.top = new FormAttachment(_available.getControl(), 0, SWT.CENTER);
 		centerFormData.left = new FormAttachment(50, 100, -10);
 
 		final FormData selectedLabelFormData = new FormData();
 		selectedLabelFormData.top = new FormAttachment(availableLabel, 0, SWT.TOP);
 		selectedLabelFormData.left = new FormAttachment(_selected.getControl(), 0, SWT.LEFT);
 		
 		final FormData selectedFormData = new FormData();
 		selectedFormData.top = new FormAttachment(_available.getControl(), 0, SWT.TOP);
 		selectedFormData.bottom = new FormAttachment(_available.getControl(), 0, SWT.BOTTOM);
 		selectedFormData.left = new FormAttachment(centerPart, 10);
 		selectedFormData.right = new FormAttachment(100, 100, -10);
 		
 		availableLabel.setLayoutData(availableLabelFormData);
 		_available.getControl().setLayoutData(availableFormData);
 		centerPart.setLayoutData(centerFormData);
 		selectedLabel.setLayoutData(selectedLabelFormData);
 		_selected.getControl().setLayoutData(selectedFormData);
 		
 		addDisposeListener(_disposeListener);
 	}
 
 	public void setHeaderVisible(boolean show) {
 		checkWidget();
 		_available.getTable().setHeaderVisible(show);
 		_selected.getTable().setHeaderVisible(show);
 	}
 	
 	public int getColumnCount() {
 		checkWidget();
 		return _columnCount;
 	}
 	
 	public GridChooserColumn getColumn(int index) {
 		checkWidget();
 		if (index < 0 || index >= _columnCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		
 		return _columns[index];
 	}
 
 	public int getItemCount() {
 		checkWidget();
 		return _itemCount;
 	}
 	
 	public GridChooserItem getItem(int index) {
 		checkWidget();
 		if (index < 0 || index >= _itemCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		
 		return _items[index];
 	}
 	
 	public GridChooserItem getItem(Point point) {
 		checkWidget();
 		
 		GridChooserItem item = getItem(_available, point);
 		if (item != null)
 			return item;
 		
 		return getItem(_selected, point);
 	}
 	
 	public int indexOf(GridChooserItem item) {
 		checkWidget();
 		if (item == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		
 		if (_lastIndexOf  >= 1 && _lastIndexOf < _itemCount - 1) {
 			if (_items[_lastIndexOf] == item) return _lastIndexOf;
 			if (_items[_lastIndexOf + 1] == item) return ++_lastIndexOf;
 			if (_items[_lastIndexOf - 1] == item) return --_lastIndexOf;
 		}
 		
 		if (_lastIndexOf < _itemCount / 2) {
 			for (int i=0; i<_itemCount; i++) {
 				if (_items[i] == item) {
 					_lastIndexOf = i;
 					return i;
 				}
 			}
 		} else {
 			for (int i=_itemCount-1; i>=0; i--) {
 				if (_items[i] == item) {
 					_lastIndexOf = i;
 					return i;
 				}
 			}
 		}
 		
 		return -1;
 	}
 	
 	public GridChooserItem[] getItems() {
 		checkWidget();
 		final GridChooserItem[] items = new GridChooserItem[_itemCount];
 		System.arraycopy(_items, 0, items, 0, _itemCount);
 		return items; 
 	}
 	
 	public int[] getSelectionIndices() {
 		checkWidget();
 		final Integer[] selected = new Integer[_itemCount];
 		int i=0;
 		for (int j=0; j<_itemCount;j++) {
 			if (!_items[j].isSelected())
 				continue;
 			selected[i++] = Integer.valueOf(j);
 		}
 		
 		Arrays.sort(selected, 0, i, _selectionItemComparator);
 		
 		final int[] result = new int[i];
 		for (int j=0; j<i; j++) {
 			result[j] = selected[j].intValue();
 		}
 		return result;
 	}
 	
 	public GridChooserItem[] getSelection() {
 		checkWidget();
 		final int[] selected = getSelectionIndices();
 		final GridChooserItem[] result = new GridChooserItem[selected.length];
 		for (int i=0; i<result.length; i++) {
 			result[i] = _items[selected[i]];
 		}
 		return result;
 	}
 	
 	public int getSelectionCount() {
 		checkWidget();
 		return getSelection().length;
 	}
 	
 	public void setItemCount(int count) {
 		checkWidget();
 		final int c = Math.max(0, count);
 		if (c == _itemCount)
 			return;
 		
 		if (c > _itemCount) {
 			for (int i=_itemCount; i<c; i++) {
 				new GridChooserItem(this, SWT.NONE, i);
 			}
 			return;
 		}
 		
 		for (int i=c; i<_itemCount; i++) {
 			final GridChooserItem item = _items[i];
 			if (item != null && !item.isDisposed())
 				item.release();
 			_items[i] = null;
 		}
 		
 		final int length = Math.max(4, (c + 3) / 4 * 4);
 		final GridChooserItem[] newItems = new GridChooserItem[length];
 		System.arraycopy(_items, 0, newItems, 0, c);
 		_items = newItems;
 		_itemCount = c;
 		refresh();
 	}
 	
 	public void setSelection(GridChooserItem[] items) {
 		checkWidget();
 		if (items == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		
 		deselectAll();
 		
 		final int[] indices = new int[items.length];
 		int i=0;
 		for (GridChooserItem item : items) {
 			final int idx = indexOf(item);
 			if (idx < 0)
 				continue;
 			indices[i++] = idx;
 		}
 		
 		final int[] result = new int[i];
 		System.arraycopy(indices, 0, result, 0, i);
 		select(result);
 	}
 	
 	public void setSelection(int[] indices) {
 		checkWidget();
 		if (indices == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		
 		deselectAll();
 		select(indices);
 	}
 	
 	public void select(int[] indices) {
 		checkWidget();
 		if (indices == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		
 		boolean selectionChanged = false;
 		for(int index : indices) {
 			if (index < 0 || index > _itemCount) {
 				continue;
 			}
 			selectionChanged |= _items[index].setSelected(true);
 		}
 		
 		if (!selectionChanged)
 			return;
 		
 		redraw();
 		notifyListeners(SWT.Selection, new Event());
 	}
 	
 	public void deselectAll() {
 		checkWidget();
 		
 		boolean selectionChanged = false;
 		for (GridChooserItem item : getSelection()) {
 			selectionChanged |= item.setSelected(false);
 		}
 		
 		if (!selectionChanged)
 			return;
 		
 		redraw();
 		notifyListeners(SWT.Selection, new Event());
 	}
 	
 	public void clear(int index) {
 		checkWidget();
 		if (index < 0 || index >= _itemCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		_items[index].clear();
 		redraw();
 	}
 	
 	public void clearAll() {
 		checkWidget();
 		for (int i=0; i<_itemCount; i++) {
 			_items[i].clear();
 		}
 		redraw();
 	}
 	
 	public void remove(int[] indices) {
 		checkWidget();
 		if (indices == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		if (indices.length == 0) return;
 		
 		final int[] working = new int[indices.length];
 		System.arraycopy(indices, 0, working, 0, working.length);
 		Arrays.sort(working);
 		
 		final int start = working[0];
 		final int end = working[working.length - 1];
 		if (start < 0 || start > end || end >= _itemCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		
 		int last = -1;
 		for (int i=working.length-1; i>0; i--) {
 			final int index = working[i];
 			if (index != last) {
 				internalRemove(index);
 				last = index;
 			}
 		}
 		
 		refresh();
 	}
 	
 	public void remove(int start, int end) {
 		checkWidget();
 		if (start < 0 || start > end || end >= _itemCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		
		for (int i=end; i>start; i--) {
 			internalRemove(i);
 		}
 		
 		refresh();
 	}
 	
 	public void removeAll() {
 		checkWidget();
 		if (_itemCount <= 0) return;
 		
 		for (int i=0; i<_itemCount; i++) {
 			final GridChooserItem item = _items[i];
 			if (item != null) {
 				item.release();
 			}
 			
 			_items[i] = null;
 		}
 		
 		_itemCount = 0;
 		refresh();
 	}
 	
 	public void showItem(GridChooserItem item) {
 		checkWidget();
 		if (item == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		if (item.isDisposed())
 			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
 		final int index = indexOf(item);
 		if (index < 0)
 			return;
 		
 		if (item.isSelected()) {
 			_selected.reveal(item);
 		} else {
 			_available.reveal(item);
 		}
 	}
 	
 	public void showSelection() {
 		// nothing to do - always visible
 	}
 	
 	public void refresh() {
 		_available.setInput(getItems());
 		_selected.setInput(getItems());
 		redraw();		
 	}
 
 	@Override
 	public void redraw() {
 		_available.refresh();
 		_selected.refresh();
 		updateButtons();
 		super.redraw();
 	}
 
 	public void addSelectionListener(SelectionListener listener) {
 		checkWidget();
 		if (listener == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		
 		final TypedListener typedListener = new TypedListener(listener);
 		addListener(SWT.Selection, typedListener);
 		addListener(SWT.DefaultSelection, typedListener);
 	}
 	
 	public void removeSelectionListener(SelectionListener listener) {
 		checkWidget();
 		if (listener == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 		
 		removeListener(SWT.Selection, listener);
 		removeListener(SWT.DefaultSelection, listener);
 	}
 	
 	void createItem(GridChooserColumn item, int index) {
 		if (index < 0 || index > _columnCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		
 		if (_columnCount == _columns.length) {
 			final GridChooserColumn[] newColumns = new GridChooserColumn[_columns.length + 4];
 			System.arraycopy(_columns, 0, newColumns, 0, _columns.length);
 			_columns = newColumns;
 		}
 		
 		final TableColumn[] columns = new TableColumn[] { //
 				new TableColumn(_available.getTable(), item.getStyle(), index), //
 				new TableColumn(_selected.getTable(), item.getStyle(), index) //
 		};
 		item.registerWidgets(columns);
 		
 		System.arraycopy(_columns, index, _columns, index+1, _columnCount++-index);
 		_columns[index] = item;
 	}
 
 	void createItem(GridChooserItem item, int index) {
 		if (index < 0 || index > _itemCount)
 			SWT.error(SWT.ERROR_INVALID_RANGE);
 		
 		if (index == _items.length) {
 			final GridChooserItem[] newItems = new GridChooserItem[_items.length + 4];
 			System.arraycopy(_items, 0, newItems, 0, _items.length);
 			_items = newItems;
 		}		
 		
 		System.arraycopy(_items, index, _items, index+1, _itemCount++-index);
 		_items[index] = item;
 
 		_available.setInput(getItems());
 		_selected.setInput(getItems());
 	}
 	
 	Rectangle getBounds(GridChooserItem item) {
 		checkWidget();
 		if (item == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
 		if (item.isSelected()) {
 			return getBounds(_selected, item);
 		}
 
 		return getBounds(_available, item);
 	}
 
 	Rectangle getBounds(GridChooserItem item, int index) {
 		checkWidget();
 		if (item == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
 		if (item.isSelected()) {
 			return getBounds(_selected, item, index);
 		}
 		
 		return getBounds(_available, item, index);
 	}
 
 	Rectangle getImageBounds(GridChooserItem item, int index) {
 		checkWidget();
 		if (item == null)
 			SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
 		if (item.isSelected()) {
 			return getImageBounds(_selected, item, index);
 		}
 		
 		return getImageBounds(_available, item, index);
 	}
 
 	Table getSelectedComposite() {
 		return _selected.getTable();
 	}
 	
 	Table getAvailableComposite() {
 		return _available.getTable();
 	}
 	
 	private Button createButton(Composite parent, Image image, SelectionListener listener) {
 		final Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
 		button.setImage(image);
 		button.setEnabled(false);
 		button.addSelectionListener(listener);
 		return button;
 	}
 	
 	private void updateButtons() {
 		final Table selected = _selected.getTable();
 		final int itemCount = selected.getItemCount();
 		boolean firstSelected = false;
 		boolean lastSelected = false;
 		for (int idx : selected.getSelectionIndices()) {
 			firstSelected |= idx == 0;
 			lastSelected |= idx == (itemCount - 1);
 		}
 		
 		final boolean hasSelection = !_selected.getSelection().isEmpty(); 
 		_left.setEnabled(hasSelection);
 		_up.setEnabled(hasSelection && !firstSelected);
 		_top.setEnabled(hasSelection && !firstSelected);
 		_down.setEnabled(hasSelection && !lastSelected);
 		_bottom.setEnabled(hasSelection && !lastSelected);
 		_right.setEnabled(!_available.getSelection().isEmpty());
 	}
 	
 	private void updateSelection(TableViewer viewer, boolean selected) {
 		final Table table = viewer.getTable();
 		final int originalLastItemIndex = table.getItemCount() - 1;
 		final int[] originalIndicies = table.getSelectionIndices();
 		
 		@SuppressWarnings("unchecked")
 		final List<GridChooserItem> selection = ((StructuredSelection)viewer.getSelection()).toList();
 		boolean selectionChanged = false;
 		for (GridChooserItem item : selection) {
 			selectionChanged |= item.setSelected(selected);
 		}
 		
 		if (!selectionChanged)
 			return;
 		
 		final int newItemCount = table.getItemCount();
 		if (newItemCount <= 0)
 			return;
 
 		int i=0;
 		for (int j=0; j<originalIndicies.length; j++) {
 			final int index = originalIndicies[j];
 			if (index == originalLastItemIndex) {
 				originalIndicies[i++] = newItemCount - 1;
 			} else if (index < newItemCount) {
 				i++;
 			}
 		}
 		
 		final int[] newIndicies = new int[i];
 		System.arraycopy(originalIndicies, 0, newIndicies, 0, i);
 		table.setSelection(newIndicies);
 		redraw();
 		notifyListeners(SWT.Selection, new Event());
 	}
 	
 	private void updateSelection(TableViewer viewer, int movement) {
 		@SuppressWarnings("unchecked")
 		final List<GridChooserItem> selection = ((StructuredSelection)viewer.getSelection()).toList();
 		if (selection.size() <= 0)
 			return;
 		
 		if (movement > 0) {
 			Collections.reverse(selection);
 		}
 		
 		boolean selectionChanged = false;
 		for (GridChooserItem item : selection) {
 			selectionChanged |= item.setSelectionOrder(item.getSelectionOrder() + movement, false);
 		}
 		
 		if (selectionChanged) {
 			notifyListeners(SWT.Selection, new Event());
 		}
 	}
 	
 	private void internalRemove(int index) {
 		final GridChooserItem item = _items[index];
 		if (item != null) {
 			item.release();
 		}
 		
 		System.arraycopy(_items, index+1, _items, index, --_itemCount-index);
 		_items[_itemCount] = null;
 	}
 	
 	private static GridChooserItem getItem(TableViewer viewer, Point point) {
 		final Rectangle r1 = viewer.getControl().getBounds();
 		if (r1.contains(point)) {
 			final Point clientPoint = new Point(point.x - r1.x, point.y - r1.y);
 			final TableItem item = viewer.getTable().getItem(clientPoint);
 			if (item != null) {
 				return (GridChooserItem) item.getData();
 			}
 		}
 		
 		return null;
 	}
 	
 	private static Rectangle getBounds(TableViewer viewer, GridChooserItem item) {
 		for (TableItem tableItem : viewer.getTable().getItems()) {
 			if (tableItem.getData() == item) {
 				final Rectangle parentBounds = viewer.getTable().getBounds();
 				final Rectangle childBounds = tableItem.getBounds();
 				return new Rectangle(parentBounds.x + childBounds.x, parentBounds.y + childBounds.y, childBounds.width, childBounds.height);
 			}
 		}
 		
 		throw new IllegalArgumentException();
 	}
 	
 	private static Rectangle getBounds(TableViewer viewer, GridChooserItem item,
 			int index) {
 		for (TableItem tableItem : viewer.getTable().getItems()) {
 			if (tableItem.getData() == item) {
 				final Rectangle parentBounds = viewer.getTable().getBounds();
 				final Rectangle childBounds = tableItem.getBounds(index);
 				return new Rectangle(parentBounds.x + childBounds.x, parentBounds.y + childBounds.y, childBounds.width, childBounds.height);
 			}
 		}
 		
 		throw new IllegalArgumentException();
 	}
 
 	private static Rectangle getImageBounds(TableViewer viewer,
 			GridChooserItem item, int index) {
 		for (TableItem tableItem : viewer.getTable().getItems()) {
 			if (tableItem.getData() == item) {
 				final Rectangle parentBounds = viewer.getTable().getBounds();
 				final Rectangle childBounds = tableItem.getImageBounds(index);
 				return new Rectangle(parentBounds.x + childBounds.x, parentBounds.y + childBounds.y, childBounds.width, childBounds.height);
 			}
 		}
 		
 		throw new IllegalArgumentException();
 	}
 
 	private static int minSelectionOrder(TableViewer viewer) {
 		final StructuredSelection selection = (StructuredSelection)viewer.getSelection();
 		if (selection.isEmpty())
 			return -1;
 		
 		final GridChooserItem[] items = new GridChooserItem[selection.size()];
 		System.arraycopy(selection.toArray(), 0, items, 0, selection.size());
 
 		int index = items[0].getSelectionOrder();
 		for (int i=1; i<items.length; i++) {
 			final int candidate = items[i].getSelectionOrder();
 			if (index > candidate) {
 				index = candidate;
 			}
 		}
 		return index;
 	}
 
 	private static int maxSelectionOrder(TableViewer viewer) {
 		final StructuredSelection selection = (StructuredSelection)viewer.getSelection();
 		if (selection.isEmpty())
 			return -1;
 		
 		final GridChooserItem[] items = new GridChooserItem[selection.size()];
 		System.arraycopy(selection.toArray(), 0, items, 0, selection.size());
 
 		int index = items[0].getSelectionOrder();
 		for (int i=1; i<items.length; i++) {
 			final int candidate = items[i].getSelectionOrder();
 			if (index < candidate) {
 				index = candidate;
 			}
 		}
 		return index;
 	}
 
 	private DisposeListener _disposeListener = new DisposeListener() {
 		@Override
 		public void widgetDisposed(DisposeEvent e) {
 			for (GridChooserItem item : _items) {
 				if (item == null)
 					continue;
 				
 				item.release();
 			}
 			_items = new GridChooserItem[0];
 			_itemCount = 0;
 			
 			for (GridChooserColumn column : _columns) {
 				if (column == null)
 					continue;
 				
 				column.release();
 			}
 			_columns = new GridChooserColumn[0];
 			_columnCount = 0;
 			
 			for (Image image : _images) {
 				if (image == null)
 					continue;
 				
 				image.dispose();
 			}
 			_images = new Image[0];
 		}
 	};
 	
 	private ISelectionChangedListener _selectionChangedListener = new ISelectionChangedListener() {
 		@Override
 		public void selectionChanged(SelectionChangedEvent event) {
 			updateButtons();
 		}
 	}; 
 	
 	private SelectionListener _changeSelectionListener = new SelectionListener() {
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			handle(e);
 		}
 		
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 			handle(e);
 		}
 		
 		private void handle(SelectionEvent e) {
 			if (_left.equals(e.widget)) {
 				updateSelection(_selected, false);
 				return;
 			} 
 				
 			if (_right.equals(e.widget)) {
 				updateSelection(_available, true);
 				return;
 			}
 			
 			if (_up.equals(e.widget)) {
 				updateSelection(_selected, -1);
 				return;
 			}
 			
 			if (_down.equals(e.widget)) {
 				updateSelection(_selected, 1);
 				return;
 			}
 			
 			if (_top.equals(e.widget)) {
 				updateSelection(_selected, -1 * minSelectionOrder(_selected));
 				return;
 			}
 			
 			if (_bottom.equals(e.widget)) {
 				updateSelection(_selected, getSelectionCount() - maxSelectionOrder(_selected) - 1);
 				return;
 			}
 		}
 	};
 	
 	private final Comparator<Integer> _selectionItemComparator = new Comparator<Integer>() {
 		private GridChooserItemSelectionOrderComparator _comparator = new GridChooserItemSelectionOrderComparator();
 		
 		@Override
 		public int compare(Integer o1, Integer o2) {
 			if (o1 == o2)
 				return 0;
 			
 			if (o1 == null)
 				return -1;
 			
 			if (o2 == null)
 				return 1;
 			
 			final GridChooserItem i0 = _items[o1.intValue()];
 			final GridChooserItem i1 = _items[o2.intValue()];
 			return _comparator.compare(i0, i1);
 		}
 	}; 
 	
 	private static class TableLabelProviderImpl extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
 
 		@Override
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (element instanceof GridChooserItem) {
 				return ((GridChooserItem)element).getImage(columnIndex);
 			}
 			
 			return null;
 		}
 
 		@Override
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof GridChooserItem) {
 				return ((GridChooserItem)element).getText(columnIndex);
 			}
 
 			return "";
 		}
 
 		@Override
 		public Color getForeground(Object element, int columnIndex) {
 			if (element instanceof GridChooserItem) {
 				return ((GridChooserItem)element).getForeground(columnIndex);
 			}
 			
 			return null;
 		}
 
 		@Override
 		public Color getBackground(Object element, int columnIndex) {
 			if (element instanceof GridChooserItem) {
 				return ((GridChooserItem)element).getBackground(columnIndex);
 			}
 			
 			return null;
 		}
 
 		@Override
 		public Font getFont(Object element, int columnIndex) {
 			if (element instanceof GridChooserItem) {
 				return ((GridChooserItem)element).getFont(columnIndex);
 			}
 			
 			return null;
 		}
 		
 	}
 	
 	private static class ItemSelectionFilter extends ViewerFilter {
 		
 		private final boolean _selected;
 		
 		public ItemSelectionFilter(boolean selected) {
 			_selected = selected;
 		}
 
 		@Override
 		public boolean select(Viewer viewer, Object parentElement,
 				Object element) {
 			if (element instanceof GridChooserItem) {
 				return ((GridChooserItem)element).isSelected() == _selected;
 			}
 			
 			return false;
 		}
 		
 	}
 
 	private static class SelectionOrderViewerComparator extends ViewerComparator {
 		
 		private final Comparator<GridChooserItem> _comparator = new GridChooserItemSelectionOrderComparator();
 		
 		@Override
 		public int compare(Viewer viewer, Object e1, Object e2) {
 			if (e1 instanceof GridChooserItem && e2 instanceof GridChooserItem) {
 				return _comparator.compare((GridChooserItem)e1, (GridChooserItem)e2);
 			}
 			
 			return super.compare(viewer, e1, e2);
 		}
 	}
 	
 	private static class DragSourceListenerImpl extends TableDragSourceEffect {
 		private final TableViewer _viewer;
 
 		public DragSourceListenerImpl(TableViewer viewer) {
 			super(viewer.getTable());
 			_viewer = viewer;
 			_viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] {LocalSelectionTransfer.getTransfer()}, this);
 		}
 		
 		@Override
 		public void dragStart(DragSourceEvent event) {
 			if (_viewer.getSelection().isEmpty())
 				event.doit = false;
 
 			super.dragStart(event);
 		}
 
 		@Override
 		public void dragSetData(DragSourceEvent event) {
 			final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
 			if (transfer.isSupportedType(event.dataType)) {
 				transfer.setSelection(_viewer.getSelection());
 			}
 		}
 	}
 	
 	private class SelectedDropTargetListener extends TableDropTargetEffect {
 		private final TableViewer _viewer;
 		private final boolean _selected;
 		
 		public SelectedDropTargetListener(TableViewer viewer, boolean selected) {
 			super(viewer.getTable());
 			viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] {LocalSelectionTransfer.getTransfer()}, this);
 			_viewer = viewer;
 			_selected = selected;
 		}
 		
 		@Override
 		public void dragEnter(DropTargetEvent event) {
 			checkOperation(event);			
 			super.dragEnter(event);
 		}
 		
 		@Override
 		public void dragOperationChanged(DropTargetEvent event) {
 			checkOperation(event);
 			super.dragOperationChanged(event);
 		}
 		
 		private void checkOperation(DropTargetEvent event) {
 			if (event.detail == DND.DROP_DEFAULT) {
 				if ((event.operations & DND.DROP_MOVE) != 0) {
 					event.detail = DND.DROP_MOVE;
 				} else {
 					event.detail = DND.DROP_NONE;
 				}
 			}
 		}
 
 		@Override
 		public void drop(DropTargetEvent event) {
 			final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
 			if (transfer.isSupportedType(event.currentDataType)) {
 				@SuppressWarnings("unchecked")
 				final List<GridChooserItem> selected = new ArrayList<GridChooserItem>(((StructuredSelection)transfer.getSelection()).toList());
 				if (!_selected) {
 					boolean selectionChanged = false;
 					for (GridChooserItem item : selected) {
 						selectionChanged |= item.setSelected(_selected);
 					}
 					
 					if (selectionChanged) {
 						_viewer.setSelection(new StructuredSelection(selected), true);
 						_viewer.getTable().setFocus();
 						notifyListeners(SWT.Selection, new Event());
 					}
 					return;
 				}
 				
 				int targetIndex;
 				if (event.item != null) {
 					final GridChooserItem targetItem = (GridChooserItem)((TableItem)event.item).getData();
 					targetIndex = targetItem.getSelectionOrder();
 				} else {
 					targetIndex = getSelectionCount();
 				}
 				
 				boolean selectionChanged = false;
 				boolean inserted = false;
 				for (GridChooserItem item : selected) {
 					if (item.getSelectionOrder() < targetIndex && item.getSelectionOrder() >= 0) {
 						inserted = true;
 						selectionChanged |= item.setSelectionOrder(targetIndex, false);
 					} else {
 						if (inserted) {
 							targetIndex++;
 							inserted = false;
 						}
 						selectionChanged |= item.setSelectionOrder(targetIndex++, false);
 					}
 				}
 				
 				if (!selectionChanged)
 					return;
 				
 				_viewer.setSelection(new StructuredSelection(selected), true);
 				_viewer.getTable().setFocus();
 				notifyListeners(SWT.Selection, new Event());
 			}
 		}
 	}
 }
