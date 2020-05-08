 /*
  * SortedTableModel.java
  * Copyright (C) 2002 Klaus Rennecke.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package net.sourceforge.fraglets.swing;
 
 import javax.swing.table.TableModel;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.EventListenerList;
 import java.util.Comparator;
 import java.util.Arrays;
 
 
 /**
  * Proxy table model to sort a delegate table model.
  * @author  marion@users.sourceforge.net
  */
 public class SortedTableProxy implements SortedTableModel, TableModelListener {
     
     /** The row transformation. */
     protected Transformation transformation[];
     
     /** The sorted column. */
     protected int sortedColumn = -1;
     
     /** Whether to sort ascending. */
     protected boolean ascending = true;
     
     /** The comparator to use, or null. */
     protected Comparator comparator;
     
     /** Holds value of property model. */
     private TableModel model;
     
     /** Utility field used by event firing mechanism. */
     private EventListenerList listenerList =  null;
     
     /**
      * Creates a new instance of SortedTableModel
      * @param model the table model delegate
      */
     public SortedTableProxy(TableModel model) {
         setModel(model);
     }
     
     
     /**
      * Get the column object class at <var>index</var>.
      * @param index the column index
      * @return the object class at <var>index</var>.
      */    
     public Class getColumnClass(int index) {
         return model.getColumnClass(index);
     }
     
     /**
      * Get the number of columns in this model.
      * @return the column count.
      */    
     public int getColumnCount() {
         return model.getColumnCount();
     }
     
     /**
      * Get the column name for the column at <var>index</var>.
      * @param index the column index
      * @return the column name at <var>index</var>.
      */    
     public String getColumnName(int index) {
         return model.getColumnName(index);
     }
     
     /**
      * Get the number of rows in this model.
      * @return the number of rows
      */    
     public int getRowCount() {
         return model.getRowCount();
     }
     
     /**
      * Get the value at <var>row</var>,<var>col</col>.
      * @param row the row index
      * @param col the column index
      * @return the value at <var>row</var>,<var>col</col>.
      */    
     public Object getValueAt(int row, int col) {
         return model.getValueAt(toDelegate(row), col);
     }
     
     public boolean isCellEditable(int row, int col) {
         return model.isCellEditable(toDelegate(row), col);
     }
     
     public void setValueAt(Object value, int row, int col) {
         model.setValueAt(value, toDelegate(row), col);
     }
     
     /** Sort the column at index <var>column</var>, using the default
      * comparator. Sorting order is ascending when <var>ascending</var>
      * is true, else descending.
      * @param column the column index to sort
      * @param ascending whether to sort ascending
      */
     public void sortColumn(int column, boolean ascending) {
         sortColumn(column, ascending, null);
     }
     
     /** Sort the column at index <var>column</var>, using the given
      * <var>comparator</var>. Sorting order is ascending when
      * <var>ascending</var> is true, else descending.
      * @param column the column index to sort
      * @param ascending whether to sort ascending 
      * @param comparator the comparator to use for sorting
      */    
     public synchronized void sortColumn(int column, boolean ascending, Comparator comparator) {
         this.sortedColumn = column;
         this.ascending = ascending;
         this.comparator = comparator;
         if (sortedColumn >= 0) {
             Arrays.sort(transformation);
         }
         int scan = getRowCount();
         while (--scan >= 0) {
             transformation[transformation[scan].getIndex()].setInverse(scan);
         }
         
         // fire structure change
         if (listenerList == null) return;
         fireTableModelListenerTableChanged(new TableModelEvent(this));
     }
     
     /**
      * Getter for property model.
      * @return Value of property model.
      */
     public TableModel getModel() {
         return this.model;
     }
     
     /** Getter for property comparator.
      * @return Value of property comparator.
      */
     public java.util.Comparator getComparator() {
         return this.comparator;
     }
     
     /**
      * Transform the given row index to the delegate
      * @param row the proxy row index
      * @return the delegate row index
      */
     protected int toDelegate(int row) {
         if (row < 0) {
             return row;
         } else {
             return transformation[row].getIndex();
         }
     }
     
     /** Transform the given row index to the proxy
      * @param row the delegate row index
      * @return the proxy row index
      */
     protected int toProxy(int row) {
         if (row < 0) {
             return row;
         } else {
             return transformation[row].getInverse();
         }
     }
     
     /**
      * Setter for property model.
      * @param model New value of property model.
      */
     public void setModel(TableModel model) {
         if (model != this.model) {
             this.model = model;
             initTransformation();
             sortColumn(sortedColumn, ascending);
         } else {
             // no change
         }
     }
     
     protected void initTransformation() {
         int scan = getRowCount();
         this.transformation = new Transformation[scan];
         while (--scan >= 0) {
             transformation[scan] = new Transformation(scan);
         }
     }
     
     /**
      * Process TableModelEvent from delegate.
      * @param tableModelEvent the event to process.
      */    
     public void tableChanged(TableModelEvent ev) {
         if (ev.getColumn() == ev.ALL_COLUMNS || ev.getColumn() == sortedColumn) {
             // simplistic implementation...
             initTransformation();
             sortColumn(sortedColumn, ascending); // this fires its own event
         } else {
             if (listenerList == null) return;
             TableModelEvent proxyEvent = new TableModelEvent(this,
                 toProxy(ev.getFirstRow()),
                 toProxy(ev.getLastRow()),
                 ev.getColumn(), ev.getType());
             // simplistic implementation...
             fireTableModelListenerTableChanged(proxyEvent);
         }
     }
     
     /** Registers TableModelListener to receive events.
      * @param listener The listener to register.
      */
     public synchronized void addTableModelListener(TableModelListener listener) {
         if (listenerList == null ) {
             listenerList = new EventListenerList();
         }
         listenerList.add(TableModelListener.class, listener);
     }
     
     /**
      * Removes TableModelListener from the list of listeners.
      * @param listener The listener to remove.
      */
     public synchronized void removeTableModelListener(TableModelListener listener) {
         listenerList.remove(TableModelListener.class, listener);
     }
     
     /**
      * Notifies all registered listeners about the event.
      *
      * @param event The event to be fired
      */
     private void fireTableModelListenerTableChanged(TableModelEvent event) {
         if (listenerList == null) return;
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length-2; i>=0; i-=2) {
             if (listeners[i]==TableModelListener.class) {
                 ((TableModelListener)listeners[i+1]).tableChanged(event);
             }
         }
     }
     
     /** Getter for property ascending.
      * @return Value of property ascending.
      */
     public boolean isAscending() {
         return ascending;
     }
     
     /** Setter for property ascending.
      * @param ascending New value of property ascending.
      */
     public void setAscending(boolean ascending) {
         if (this.ascending != ascending) {
             sortColumn(sortedColumn, ascending);
         }
     }
     
     /** Getter for property sortedColumn.
      * @return Value of property sortedColumn.
      */
     public int getSortedColumn() {
         return this.sortedColumn;
     }
     
     /** Setter for property sortedColumn.
      * @param sortedColumn New value of property sortedColumn.
      */
     public void setSortedColumn(int sortedColumn) {
         if (this.sortedColumn != sortedColumn) {
             sortColumn(sortedColumn, isAscending());
         }
     }
     
     protected class Transformation implements Comparable {
         /** Holds the delegate index. */
         private int index;
         
         /** Holds value of property inverse. */
         private int inverse;
         
         /**
          * Create a new transformation for the delegate row at
          * <var>index</var>.
          * @param index index of the delegate row.
          */        
         public Transformation(int index) {
             this.index = index;
         }
         
         /** Comparision function to compare this row to another.
          * @param obj the other row transformation to compare to
          * @return -1, 0, or 1 depending on the row comparision
          */
         public int compareTo(Object obj) {
             int result;
             Object value0 = model.getValueAt(index, sortedColumn);
             Object value1 = model.getValueAt(((Transformation)obj).index, sortedColumn);
            if (comparator != null) {
                result = comparator.compare(value0, value1);
            } else if (value0 != null) {
                 result = ((Comparable)value0).compareTo(value1);
             } else {
                result = value1 == null ? 0 : -1;
             }
             return ascending ? result : 0 - result;
         }
         
         /** Getter for property index.
          * @return Value of property index.
          */
         public int getIndex() {
             return this.index;
         }
         
         /** Getter for property inverse.
          * @return Value of property inverse.
          */
         public int getInverse() {
             return this.inverse;
         }
         
         /** Setter for property inverse.
          * @param inverse New value of property inverse.
          */
         public void setInverse(int inverse) {
             this.inverse = inverse;
         }
     }
 }
