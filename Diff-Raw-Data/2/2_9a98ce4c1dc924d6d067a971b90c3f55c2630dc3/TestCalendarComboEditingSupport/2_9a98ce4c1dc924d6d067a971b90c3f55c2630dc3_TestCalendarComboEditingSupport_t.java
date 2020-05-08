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
 
 import net.karlmartens.ui.widget.CalendarCombo;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.swt.widgets.Composite;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 
 final class TestCalendarComboEditingSupport extends EditingSupport {
 
   private final DateTimeFormatter _formatter = CalendarCombo.createDateFormat();
   private final ColumnViewer _viewer;
   private final int _index;
 
   private CalendarComboCellEditor _cellEditor;
 
   public TestCalendarComboEditingSupport(ColumnViewer viewer, int index) {
     super(viewer);
     _viewer = viewer;
     _index = index;
   }
 
   @Override
   protected CellEditor getCellEditor(Object element) {
     if (_cellEditor == null) {
       _cellEditor = new CalendarComboCellEditor(
           (Composite) _viewer.getControl());
     }
     return _cellEditor;
   }
 
   @Override
   protected boolean canEdit(Object element) {
     return true;
   }
 
   @Override
   protected Object getValue(Object element) {
     final Object[] data = (Object[]) element;
     if (data[_index] == null)
       return "";
 
     return _formatter.print((LocalDate) data[_index]);
   }
 
   @Override
   protected void setValue(Object element, Object value) {
     final Object[] data = (Object[]) element;
     try {
       final LocalDate ld = _formatter.parseLocalDate((String) value);
       data[_index] = ld;
    } catch (Throwable e) {
       data[_index] = null;
     }
   }
 }
