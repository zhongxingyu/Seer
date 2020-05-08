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
 package net.karlmartens.ui.viewer;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.BitSet;
 
 import net.karlmartens.platform.text.LocalDateFormat;
 import net.karlmartens.platform.util.ArraySupport;
 import net.karlmartens.platform.util.NullSafe;
 import net.karlmartens.ui.widget.CellNavigationStrategy;
 import net.karlmartens.ui.widget.SparklineScrollBar;
 import net.karlmartens.ui.widget.Table;
 import net.karlmartens.ui.widget.TableColumn;
 import net.karlmartens.ui.widget.TableItem;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 
 public final class TimeSeriesTableViewer extends TableViewer {
 
   public enum ScrollDataMode {
     FOCUS_CELL, SELECTED_ROWS
   }
 
   private final Table _table;
   private SparklineScrollBar _scroll;
 
   private TimeSeriesEditingSupport _editingSupport;
   private LocalDateFormat _dateFormat = new LocalDateFormat(DateTimeFormat.shortDate());
   private NumberFormat _numberFormat = NumberFormat.getNumberInstance();
   private ScrollDataMode _scrollDataMode = ScrollDataMode.FOCUS_CELL;
 
   private TimeSeriesTableViewer(Table table) {
     super(table);
     _table = table;
     hook();
   }
 
   TimeSeriesEditingSupport getEditingSupport() {
     return _editingSupport;
   }
 
   public void setEditingSupport(TimeSeriesEditingSupport editingSupport) {
     if (NullSafe.equals(_editingSupport, editingSupport))
       return;
 
     _editingSupport = editingSupport;
     refresh(true);
   }
 
   public void setDateFormat(LocalDateFormat format) {
     if (format == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     _dateFormat = format;
     refresh(true);
   }
   
   NumberFormat getNumberFormat() {
     return _numberFormat;
   }
 
   public void setNumberFormat(NumberFormat format) {
     if (format == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     _numberFormat = format;
     refresh(false);
   }
 
   public void setScrollDataMode(ScrollDataMode mode) {
     if (mode == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     _scrollDataMode = mode;
     refresh(false);
   }
 
   public static TimeSeriesTableViewer newTimeSeriesTable(Composite parent) {
     final Table table = new Table(parent, SWT.V_SCROLL | SWT.MULTI);
     table.setBackground(parent.getBackground());
     table.setForeground(parent.getForeground());
     table.setFont(parent.getFont());
     table.setLayout(new FormLayout());
     
     final Font labelFont = new Font(parent.getDisplay(), "Arial", 7, SWT.BOLD);
     
     final SparklineScrollBar scroll = new SparklineScrollBar(table, SWT.BORDER);
     scroll.setMinimum(0);
     scroll.setLabelFont(labelFont);
 
     final FormData tableData = new FormData();
     tableData.top = new FormAttachment(0, 100, 0);
     tableData.left = new FormAttachment(scroll, 0, SWT.LEFT);
     tableData.bottom = new FormAttachment(scroll, -5, SWT.TOP);
     tableData.right = new FormAttachment(scroll, 0, SWT.RIGHT);
 
     final FormData scrollData = new FormData();
     scrollData.left = new FormAttachment(0, 100, 0);
     scrollData.bottom = new FormAttachment(100, 100, 0);
     scrollData.right = new FormAttachment(100, 100, 0);
     scrollData.height = 40;
 
     table.getChildren()[0].setLayoutData(tableData);
     scroll.setLayoutData(scrollData);
 
     final TimeSeriesTableViewer viewer = new TimeSeriesTableViewer(table);
     viewer.register(scroll);
     
     table.addDisposeListener(new DisposeListener() {
       @Override
       public void widgetDisposed(DisposeEvent e) {
         labelFont.dispose();
       }
     });
 
     return viewer;
   }
 
   @Override
   protected void internalRefresh(Object element, boolean updateLabels) {
     final TimeSeriesContentProvider cp = (TimeSeriesContentProvider) getContentProvider();
     if (updateLabels && cp != null) {
       LocalDate[] dates = cp.getDates();
       if (dates == null)
         dates = new LocalDate[0];
 
       final int fixedColumnCount = _table.getFixedColumnCount();
       final int tsColumnCount = _table.getColumnCount() - fixedColumnCount;
       if (tsColumnCount > dates.length) {
         _updateThumb = true;
         _table.setColumnCount(fixedColumnCount + dates.length);
       } else if (tsColumnCount < dates.length) {
         _updateThumb = true;
 
         final GC gc = new GC(_table);
         gc.setFont(_table.getFont());
         final int defaultWidth = gc.getCharWidth('W') * 8;
         gc.dispose();
 
         for (int i = tsColumnCount; i < dates.length; i++) {
           final TableColumn column = new TableColumn(_table, SWT.RIGHT);
           column.setMoveable(false);
           column.setHideable(false);
           column.setWidth(defaultWidth);
 
           final TableViewerColumn viewerColumn = new TableViewerColumn(this, column);
           viewerColumn.setLabelProvider(new PeriodLabelProvider(cp));
 
           if (_editingSupport != null) {
             viewerColumn.setEditingSupport(new TimeSeriesTableValueEditingSupport(this));
           }
         }
       }
 
       if (_updateThumb) {
         _scroll.setMaximum(Math.max(dates.length - 1, 1));
       }
 
       final int scrollSelection = _scroll.getSelection();
       for (int i = 0; i < dates.length; i++) {
         final TableColumn column = _table.getColumn(fixedColumnCount + i);
         final String text = _dateFormat.format(dates[i]);
         column.setText(text);
 
         if (i == scrollSelection) {
           _scroll.setLabel(text);
         }
       }
       
       updateHighlights();
       updateData();
     }
 
     super.internalRefresh(element, updateLabels);
   }
 
   @Override
   protected void assertContentProviderType(IContentProvider provider) {
     Assert.isTrue(provider instanceof TimeSeriesContentProvider);
   }
 
   private void register(SparklineScrollBar scroll) {
     release();
     _scroll = scroll;
     hook();
   }
 
   private void hook() {
     if (_scroll == null || _table == null)
       return;
 
     _scroll.addListener(SWT.Selection, _listener);
     
     _table.addListener(SWT.Resize, _listener);
     _table.addListener(SWT.Paint, _listener);
     _table.addListener(SWT.KeyDown, _listener);
     _table.addListener(SWT.MouseDown, _listener);
     _table.addListener(SWT.MouseUp, _listener);
     _table.addListener(SWT.MouseMove, _listener);
     _table.addListener(SWT.Selection, _listener);
   }
 
   private void release() {
     if (_scroll != null) {
       _scroll.removeListener(SWT.Selection, _listener);
     }
 
     if (_table != null) {
       _table.removeListener(SWT.Resize, _listener);
       _table.removeListener(SWT.Paint, _listener);
       _table.removeListener(SWT.KeyDown, _listener);
       _table.removeListener(SWT.MouseDown, _listener);
       _table.removeListener(SWT.MouseUp, _listener);
       _table.removeListener(SWT.MouseMove, _listener);
       _table.removeListener(SWT.Selection, _listener);
     }
 
   }
 
   private int _scrollEventId = 0;
 
   private void handleSelection(Event e) {
     final Object source = e.widget;
     if (source == _table) {
       updateData();
     }
     
     if (source == _scroll) {
       final int id = ++_scrollEventId;
       final Runnable runnable = new Runnable() {
         @Override
         public void run() {
           if (id != _scrollEventId)
             return;
 
           final Rectangle rect = _table.getVisibleScrollableCells();
           final Point pt = new Point(rect.x, rect.y);
           pt.x = _table.getFixedColumnCount() + _scroll.getSelection();
           _table.scroll(pt);
           
           final TableColumn column = _table.getColumn(pt.x);
           _scroll.setLabel(column.getText());
         }
       };
 
       final Display display = _table.getDisplay();
       if (_scrollEventId % 7 == 0) {
         display.syncExec(runnable);
       } else {
         display.asyncExec(runnable);
       }
     }
   }
 
   private final CellNavigationStrategy _navigationStrategy = new CellNavigationStrategy();
 
   private void handleKeyPressed(Event e) {
     if (!_navigationStrategy.isNavigationEvent(e) && !_navigationStrategy.isExpandEvent(e))
       return;
     
     final Rectangle rect = _table.getVisibleScrollableCells();
     _scroll.setSelection(rect.x - _table.getFixedColumnCount());
     
     final TableColumn column = _table.getColumn(rect.x);
     _scroll.setLabel(column.getText());
     
     updateHighlights();
   }
   
   private boolean _updateThumb = true;
   
   private void handleResize() {
     _updateThumb = true;
   }
   
   private void handlePaint() {
     if (!_updateThumb)
       return;
     
     final Rectangle rect = _table.getVisibleScrollableCells();
    final int max = _scroll.getMaximum() - _scroll.getMinimum() - 1;
     final int tWidth = Math.min(Math.max(1, rect.width), max);
     _scroll.setThumb(tWidth);
     _scroll.setEnabled(tWidth != max);
   }
   
   private boolean _mouseActive = false;
 
   private void handleMouseDown(Event e) {
     _mouseActive = (e.button == 1);
     if (_mouseActive) {
       updateHighlights();
     }
   }
   
   private void handleMouseMove() {
     if (_mouseActive)
       updateHighlights();
   }
   
   private void handleMouseUp(Event e) {
     if (e.button == 1) {
       updateHighlights();
       _mouseActive = false;
     }
   }
   
   private void updateHighlights() {
     final int fixedColumnCount = _table.getFixedColumnCount();
     final int min = _scroll.getMinimum();
     final int max = _scroll.getMaximum();
     
     final BitSet selected = new BitSet();
     for (Point pt : _table.getCellSelections()) {
       final int index = pt.x - fixedColumnCount;
       if (index < min || index > max)
         continue;
       
       selected.set(index);
 
       final int[] indices = ArraySupport.toArray(selected);
       _scroll.setHighlights(indices);
     }
   }
   
   private void updateData() {
     final int[] indices;
     switch (_scrollDataMode) {
       case FOCUS_CELL:
         final Point focus = _table.getFocusCell();
         if (focus != null) {
           indices = new int[] { focus.y };
         } else {
           indices = new int[] {};
         }
         break;
 
       case SELECTED_ROWS:
         indices = _table.getSelectionIndices();
         break;
 
       default:
         indices = new int[] {};
     }
     
     final int fixedColumnCount = _table.getFixedColumnCount();
     final double[] data = new double[_scroll.getMaximum() - _scroll.getMinimum() + 1];
     Arrays.fill(data, 0.0);
     for (int index : indices) {
       final TableItem item = _table.getItem(index);
       for (int j = 0; j < data.length; j++) {
         try {
           data[j] += _numberFormat.parse(item.getText(fixedColumnCount + j)).doubleValue();
         } catch (ParseException e) {
           // ignore
         }
       }
     }
     _scroll.setDataPoints(data);
   }
 
   private final class PeriodLabelProvider extends CellLabelProvider {
 
     private final TimeSeriesContentProvider _base;
 
     public PeriodLabelProvider(TimeSeriesContentProvider base) {
       _base = base;
     }
 
     @Override
     public void update(ViewerCell cell) {
       final int index = cell.getColumnIndex();
       final double value = _base.getValue(cell.getElement(), index - _table.getFixedColumnCount());
       
       final String string;
       if (value == 0.0) {
         string = "";
       } else {
         string = _numberFormat.format(value);
       }
       
       final TableItem item = ((TableItem) cell.getItem()); 
       item.setText(index, string);
     }
   }
 
   private final Listener _listener = new Listener() {
     @Override
     public void handleEvent(Event event) {
       switch (event.type) {
         case SWT.Selection:
           handleSelection(event);
           break;
 
         case SWT.KeyDown:
           handleKeyPressed(event);
           break;
           
         case SWT.Resize:
           handleResize();
           break;
           
         case SWT.Paint:
           handlePaint();
           break;
           
         case SWT.MouseDown:
           handleMouseDown(event);
           break;
           
         case SWT.MouseUp:
           handleMouseUp(event);
           break;
           
         case SWT.MouseMove:
           handleMouseMove();
           break;
       }
     }
   };
 }
