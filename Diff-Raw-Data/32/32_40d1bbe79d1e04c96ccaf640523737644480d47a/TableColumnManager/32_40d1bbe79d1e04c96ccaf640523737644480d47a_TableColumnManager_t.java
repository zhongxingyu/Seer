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
 
 import java.util.ResourceBundle;
 
 import net.karlmartens.ui.action.ResizeAllColumnsAction;
 import net.karlmartens.ui.action.ResizeColumnAction;
 import net.karlmartens.ui.widget.Table.KTableImpl;
 
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MenuDetectEvent;
 import org.eclipse.swt.events.MenuDetectListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 final class TableColumnManager {
 
   private final Table _container;
   private final KTableImpl _table;
   private final SortColumnsContribution _sortColumnsContribution;
   private final FilterGroupContribution _filterGroupContribution;
   private final ResizeColumnAction _resizeColumnAction;
   private final ResizeAllColumnsAction _resizeAllColumnsAction;
 
   private boolean _columnsSortable = false;
 
   private int _columnIndex;
   private Point _offset;
   private Image _image;
   private Shell _shell;
 
   private boolean _columnMove = false;
   private boolean _selection = false;
 
   TableColumnManager(Table container, KTableImpl table) {
     _container = container;
     _table = table;
 
     _sortColumnsContribution = new SortColumnsContribution(this, _container);
     _filterGroupContribution = new FilterGroupContribution(_container);
     _resizeColumnAction = new ResizeColumnAction(_container, -1);
     _resizeAllColumnsAction = new ResizeAllColumnsAction(_container);
 
     final ResourceBundle bundle = ResourceBundle
         .getBundle("net.karlmartens.ui.locale.messages");
     final IMenuManager showHideMenu = new MenuManager(
         bundle.getString("ShowHideColumns.TEXT"));
     showHideMenu.add(new GroupMarker(Table.GROUP_VISIBLE_COLUMNS));
     showHideMenu.add(new VisibleColumnsContribution(_container));
     showHideMenu.update();
 
     final IMenuManager columnMenu = container.getMenuManager();
     columnMenu.appendToGroup(Table.GROUP_COMMAND, _sortColumnsContribution);
     columnMenu.appendToGroup(Table.GROUP_COMMAND, _resizeColumnAction);
     columnMenu.appendToGroup(Table.GROUP_COMMAND, _resizeAllColumnsAction);
     columnMenu.appendToGroup(Table.GROUP_DATA, showHideMenu);
     columnMenu.appendToGroup(Table.GROUP_DATA, _filterGroupContribution);
     columnMenu.update();
 
     hookControl();
   }
 
   boolean isSortEnabled() {
     return _columnsSortable;
   }
 
   void enableColumnSort() {
     _columnsSortable = true;
   }
 
   private void hookControl() {
     _container.addDisposeListener(_widgetListener);
     _container.addMenuDetectListener(_widgetListener);
     _table.addMouseListener(_widgetListener);
     _table.addMouseMoveListener(_widgetListener);
   }
 
   private void releaseControl() {
     _container.removeDisposeListener(_widgetListener);
     _container.removeMenuDetectListener(_widgetListener);
     _table.removeMouseListener(_widgetListener);
     _table.removeMouseMoveListener(_widgetListener);
   }
 
   private Display getDisplay() {
     return _table.getDisplay();
   }
 
   private void initiateColumnMove(MouseEvent e, int colIndex) {
     _columnMove = true;
     _table.setIgnoreMouseMove(true);
 
     final Rectangle cellCords = _table.getCellRect(colIndex, 0);
     final int height = _table.getClientArea().height;
     _offset = new Point(cellCords.x - e.x, cellCords.y - e.y);
 
     if (_image != null)
       _image.dispose();
     _image = new Image(getDisplay(), new Rectangle(0, 0, cellCords.width,
         height));
     _image.getImageData().alpha = 0;
 
     final GC gc = new GC(_table);
     gc.copyArea(_image, cellCords.x, cellCords.y);
     gc.dispose();
   }
 
   private void showColumnMoveEffect() {
     _table.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
 
     _shell = new Shell(getDisplay(), SWT.NO_TRIM);
     _shell.setLayout(new FillLayout());
     _shell.setBounds(_image.getBounds());
 
     final Label l = new Label(_shell, SWT.NONE);
     l.setImage(_image);
     l.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
 
     _shell.open();
   }
 
   private void cancelColumnMove() {
     _columnMove = false;
     _offset = null;
     _table.setIgnoreMouseMove(false);
 
     if (_image != null && !_image.isDisposed())
       _image.dispose();
     _image = null;
 
     if (_shell != null && !_shell.isDisposed())
       _shell.dispose();
     _shell = null;
 
     _table.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
   }
 
   private boolean isColumnMoveActive() {
     return _columnMove;
   }
 
   private void initiateSelection() {
     _selection = true;
   }
 
   private void cancelSelection() {
     _selection = false;
   }
 
   private boolean isSelectionActive() {
     return _selection;
   }
 
   private final Listener _widgetListener = new Listener();
 
   private final class Listener implements MouseListener, MouseMoveListener,
       MenuDetectListener, DisposeListener {
 
     @Override
     public void mouseDoubleClick(MouseEvent e) {
       if (_table != e.getSource())
         return;

       final Point cellCord = _table.getCellForCoordinates(e.x, e.y);
       if (cellCord.y < 0
           || cellCord.y >= _table.getModel().getFixedHeaderRowCount())
         return;
 
       final TableColumn column = _container.getColumn(cellCord.x);
       if (!column.isVisible())
         return;
 
       final Rectangle r = _table.getCellRect(cellCord.x, cellCord.y);
       if (r.x + r.width - e.x <= 5) {
         _resizeColumnAction.setColumnIndex(cellCord.x);
         _resizeColumnAction.run();
         return;
       }
 
       if (e.x - r.x <= 5) {
         for (int i = cellCord.x - 1; i >= 0; i--) {
           final TableColumn pColumn = _container.getColumn(i);
           if (pColumn.isVisible()) {
             _resizeColumnAction.setColumnIndex(i);
             _resizeColumnAction.run();
             return;
           }
         }
       }
     }
 
     @Override
     public void mouseDown(MouseEvent e) {
       if (_table != e.getSource()) {
         cancelColumnMove();
         cancelSelection();
         return;
       }
 
       if (e.button != 1) {
         cancelColumnMove();
         cancelSelection();
         return;
       }
 
       final Point cellCord = _table.getCellForCoordinates(e.x, e.y);
       if (cellCord.y < 0
           || cellCord.y >= _table.getModel().getFixedHeaderRowCount()) {
         cancelColumnMove();
         cancelSelection();
         return;
       }
 
       if (cellCord.x < 0 || cellCord.x >= _container.getColumnCount()) {
         cancelColumnMove();
         cancelSelection();
         return;
       }
 
       final Rectangle r = _table.getCellRect(cellCord.x, cellCord.y);
       if (e.x - r.x <= 5 || r.x + r.width - e.x <= 5) {
         cancelColumnMove();
         cancelSelection();
         return;
       }
 
       _columnIndex = cellCord.x;
       initiateSelection();
 
       if (!_container.getColumn(_columnIndex).isMoveable()) {
         cancelColumnMove();
         return;
       }
 
       initiateColumnMove(e, cellCord.x);
     }
 
     @Override
     public void mouseUp(MouseEvent e) {
       if (!isColumnMoveActive() && !isSelectionActive())
         return;
 
       if (_table != e.getSource()) {
         cancelColumnMove();
         cancelSelection();
         return;
       }
 
       final Point cellCord = _table.getCellForCoordinates(e.x, e.y);
       if (cellCord.x < 0 || cellCord.x >= _container.getColumnCount()
           || _columnIndex == cellCord.x) {
         cancelColumnMove();
 
         if (isSelectionActive()) {
           if (_columnsSortable) {
             _container.sort(_columnIndex);
           }
 
           final TableColumn column = _container.getColumn(_columnIndex);
           column.notifyListeners(SWT.Selection, new Event());
           cancelSelection();
         }
         return;
       }
 
       if (isColumnMoveActive())
         _container.moveColumn(_columnIndex, cellCord.x);
 
       cancelColumnMove();
       cancelSelection();
     }
 
     @Override
     public void mouseMove(MouseEvent e) {
       cancelSelection();
       if (!isColumnMoveActive())
         return;
 
       if (_table != e.getSource()) {
         cancelColumnMove();
         return;
       }
 
       if (_shell == null) {
         showColumnMoveEffect();
       }
 
       final Point cellCord = _table.getCellForCoordinates(e.x, e.y);
       if (cellCord.x >= 0 && cellCord.x < _container.getColumnCount()) {
         final TableColumn column = _container.getColumn(cellCord.x);
         if (column.isMoveable()) {
           _shell.setAlpha(200);
         } else {
           _shell.setAlpha(100);
         }
       }
 
       final int max = _table.getClientArea().width - _shell.getBounds().width;
       final Point p = _container.toDisplay(_table.getLocation());
       p.x += Math.max(Math.min(e.x + _offset.x, max), 0);
       _shell.setLocation(p);
     }
 
     @Override
     public void menuDetected(MenuDetectEvent e) {
       final Point cord = _table.toControl(e.x, e.y);
       final Point cell = _table.getCellForCoordinates(cord.x, cord.y);
 
       final int columnIndex = cell.x;
       _sortColumnsContribution.setColumnIndex(columnIndex);
       _filterGroupContribution.setColumnIndex(columnIndex);
       _resizeColumnAction.setColumnIndex(columnIndex);
     }
 
     @Override
     public void widgetDisposed(DisposeEvent e) {
       releaseControl();
      _filterGroupContribution.dispose();
     }
   }
 }
