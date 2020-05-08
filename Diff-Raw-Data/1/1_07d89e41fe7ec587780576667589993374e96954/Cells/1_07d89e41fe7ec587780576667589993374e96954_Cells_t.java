 /*
  * @(#) $RCSfile: Cells.java,v $ $Revision: 1.13 $ $Date: 2004/08/12 21:00:02 $ $Name: TableView1_3_2 $
  *
  * Center for Computational Genomics and Bioinformatics
  * Academic Health Center, University of Minnesota
  * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * see: http://www.gnu.org/copyleft/gpl.html
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  */
 package edu.umn.genomics.table;
 
 import java.util.*;
 import java.math.*;
 import java.text.DecimalFormat;
 import java.text.ParsePosition;
 import javax.swing.table.TableModel;
 import javax.swing.ListSelectionModel;
 import edu.umn.genomics.graph.LineFormula;
 
 /**
  * Cells
  *
  * @author J Johnson
  * @version $Revision: 1.13 $ $Date: 2004/08/12 21:00:02 $ $Name: TableView1_3_2
  * $
  * @since 1.0
  * @see javax.swing.table.TableModel
  */
 public class Cells {
 
     /**
      * Sort by ascending row number.
      */
     public static final int ROWNUM_ASCENDING = Integer.MAX_VALUE;
     /**
      * Sort by descending row number.
      */
     public static final int ROWNUM_DESCENDING = Integer.MIN_VALUE;
     /**
      * Sort by select rows first.
      */
     public static final int SELECTED_ROWS_FIRST = Integer.MAX_VALUE - 1;
     /**
      * Sort by select rows first.
      */
     public static final int SELECTED_ROWS_LAST = Integer.MIN_VALUE + 1;
     /**
      * Compares the string representations of 2 objects such that numeric parts
      * of the strings are compared as numbers, such that "id1" = "id0001".
      *
      */
     public static Comparator alphaNumericComparator = new Comparator() {
 
         public int compare(Object o1, Object o2) {
             if (o1 == null) {
                 return o2 == null ? 0 : -1;
             } else if (o2 == null) {
                 return 1;
             }
             String s1 = o1.toString();
             String s2 = o2.toString();
             int l1 = s1.length();
             int l2 = s2.length();
             for (int i1 = 0, i2 = 0;;) {
                 if (i1 >= l1 && i2 >= l2) {
                     return 0;
                 }
                 if (i1 >= l1) {
                     return -1;
                 }
                 if (i2 >= l2) {
                     return 1;
                 }
                 if (!Character.isDigit(s1.charAt(i1))
                         || !Character.isDigit(s2.charAt(i2))) {
                     if (s1.charAt(i1) < s2.charAt(i2)) {
                         return -1;
                     } else if (s1.charAt(i1) > s2.charAt(i2)) {
                         return 1;
                     }
                     i1++;
                     i2++;
                 } else {
                     DecimalFormat df = new DecimalFormat();
                     ParsePosition pp = new ParsePosition(0);
                     Number n1 = df.parse(s1.substring(i1), pp);
                     i1 += pp.getIndex();
                     pp.setIndex(0);
                     Number n2 = df.parse(s2.substring(i2), pp);
                     i2 += pp.getIndex();
                     int nc = numberComparator.compare(n1, n2);
                     if (nc != 0) {
                         return nc;
                     }
                 }
             }
         }
 
         public boolean equals(Object obj) {
             return super.equals(obj);
         }
     };
     /**
      * Compares two objects as Numbers, if the Objects are Strings it will
      * attempt to parse them as Numbers.
      */
     public static Comparator numberComparator = new Comparator() {
 
         public int compare(Object o1, Object o2) {
             if (o1 == null) {
                 return o2 == null ? 0 : -1;
             } else if (o2 == null) {
                 return 1;
             }
             if (o1 instanceof Number && o2 instanceof Number) {
                 try {
                     int c = ((Comparable) o1).compareTo(o2);
                     return c;
                 } catch (Exception ex) {
                 }
                 if (o1 instanceof BigDecimal) {
                     if (o2 instanceof BigInteger) {
                         return ((BigDecimal) o1).compareTo(
                                 new BigDecimal((BigInteger) o2));
                     } else {
                         return ((BigDecimal) o1).compareTo(
                                 new BigDecimal(((Number) o2).doubleValue()));
                     }
                 }
                 if (o2 instanceof BigDecimal) {
                     if (o1 instanceof BigInteger) {
                         return -((BigDecimal) o2).compareTo(
                                 new BigDecimal((BigInteger) o1));
                     } else {
                         return -((BigDecimal) o2).compareTo(
                                 new BigDecimal(((Number) o1).doubleValue()));
                     }
                 }
                 double diff = ((Number) o1).doubleValue()
                         - ((Number) o2).doubleValue();
                 if (diff == 0.) {
                     return 0;
                 }
                 if (diff < 0.) {
                     return (int) Math.floor(diff);
                 } else {
                     return (int) Math.ceil(diff);
                 }
             }
             throw new ClassCastException("numberComparator only compares Number");
         }
 
         public boolean equals(Object obj) {
             return super.equals(obj);
         }
     };
     /**
      * Compares two objects.
      */
     public static Comparator defaultComparator = new Comparator() {
 
         public int compare(Object o1, Object o2) {
             if (o1 == null) {
                 return o2 == null ? 0 : -1;
             } else if (o2 == null) {
                 return 1;
             }
             if (o1.equals(o2)) {
                 return 0;
             }
             if (o1 instanceof Comparable) {
                 try {
                     int c = ((Comparable) o1).compareTo(o2);
                     return c;
                 } catch (Exception ex) {
                     ExceptionHandler.popupException(""+ex);
                 }
             }
             if (o1 instanceof Number && o2 instanceof Number) {
                 return numberComparator.compare(o1, o2);
             }
             if (o1 instanceof Date && o2 instanceof Date) {
                 return ((Date) o1).before((Date) o2) ? -1 : 1;
             }
             if (o1 instanceof Boolean && o2 instanceof Boolean) {
                 return ((Boolean) o2).booleanValue() ? -1 : 1;
             }
             if (o1 instanceof Comparable) {
                 try {
                     int c = ((Comparable) o1).compareTo(o2);
                     return c;
                 } catch (Exception ex) {
                     ExceptionHandler.popupException(""+ex);
                 }
             }
             return alphaNumericComparator.compare(o1, o2);
         }
 
         public boolean equals(Object obj) {
             return super.equals(obj);
         }
     };
 
     public Cells() {
     }
 
     /**
      * A convience method to return the column index for the given column name.
      * It tries first for a case sensitive match, then a case insensitive match.
      *
      * @param tm The TableModel
      * @param name The column heading to search for.
      * @return The index of the column (0 to columnCount-1), or -1 if not found.
      */
     public static int getColumn(TableModel tm, String name) {
         if (tm != null && name != null) {
             int cc = tm.getColumnCount();
             for (int i = 0; i < cc; i++) {
                 if (name.equals(tm.getColumnName(i))) {
                     return i;
                 }
             }
             for (int i = 0; i < cc; i++) {
                 if (name.equalsIgnoreCase(tm.getColumnName(i))) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /**
      * Swaps arr[a] with arr[b].
      *
      * @param arr The array in which to swap values.
      * @param a The index to swap.
      * @param b The index to swap.
      */
     private static void swap(int arr[], int a, int b) {
         int i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     private static void swap(byte arr[], int a, int b) {
         byte i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     private static void swap(char arr[], int a, int b) {
         char i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     private static void swap(short arr[], int a, int b) {
         short i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     private static void swap(long arr[], int a, int b) {
         long i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     private static void swap(float arr[], int a, int b) {
         float i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     private static void swap(double arr[], int a, int b) {
         double i = arr[a];
         arr[a] = arr[b];
         arr[b] = i;
     }
 
     /**
      * Swaps x[a] with x[b].
      */
     private static void swap(Object x[], int a, int b) {
         Object t = x[a];
         x[a] = x[b];
         x[b] = t;
     }
 
     /**
      * Swaps x[a] with x[b] and ia[a] with ia[b].
      */
     private static void swap(Object x[], int ia[], int a, int b) {
         Object t = x[a];
         x[a] = x[b];
         x[b] = t;
         int i = ia[a];
         ia[a] = ia[b];
         ia[b] = i;
     }
 
     /**
      * qsort an array of Objects and the corresponding index array using the
      * given Comparator.
      */
     private static void qsort(Object a[], int ia[], int lo0, int hi0, Comparator c) {
         int lo = lo0;
         int hi = hi0;
         int mid;
         if (hi0 > lo0) {
             /*
              * Arbitrarily establishing partition element as the midpoint of the
              * array.
              */
             mid = (lo0 + hi0) / 2;
             Object mo = a[mid];
             // loop through the array until indices cross
             while (lo <= hi) {
                 /*
                  * find the first element that is greater than or equal to the
                  * partition element starting from the left Index.
                  */
                 while ((lo < hi0) && (c.compare(a[lo], mo) < 0)) {
                     ++lo;
                 }
                 /*
                  * find an element that is smaller than or equal to the
                  * partition element starting from the right Index.
                  */
                 while ((hi > lo0) && (c.compare(a[hi], mo) > 0)) {
                     --hi;
                 }
                 // if the indexes have not crossed, swap
                 if (lo <= hi) {
                     if (lo < hi) {
                         swap(a, ia, lo, hi);
                     }
                     ++lo;
                     --hi;
                 }
             }
             /*
              * If the right index has not reached the left side of array must
              * now sort the left partition.
              */
             if (lo0 < hi) {
                 qsort(a, ia, lo0, hi, c);
             }
             /*
              * If the left index has not reached the right side of array must
              * now sort the right partition.
              */
             if (lo < hi0) {
                 qsort(a, ia, lo, hi0, c);
             }
         }
     }
 
     /**
      * qsort an array of Objects and the corresponding index array.
      */
     private static void qsort(double arr[], int ia[], int lo0, int hi0) {
         int lo = lo0;
         int hi = hi0;
         int mid;
         double midval;
         if (hi0 > lo0) {
             // Arbitrarily establishing partition element as the midpoint of the array.
             mid = (lo0 + hi0) / 2;
             midval = arr[mid];
             // loop through the array until indices cross
             while (lo <= hi) {
                 // find the first element that is greater than or equal to
                 //  the partition element starting from the left Index.
                 while ((lo < hi0) && (arr[lo] < midval)) {
                     ++lo;
                 }
                 // find an element that is smaller than or equal to
                 //  the partition element starting from the right Index.
                 while ((hi > lo0) && (arr[hi] > midval)) {
                     --hi;
                 }
                 // if the indexes have not crossed, swap
                 if (lo <= hi) {
                     if (lo < hi) {
                         swap(arr, lo, hi);
                         swap(ia, lo, hi);
                     }
                     ++lo;
                     --hi;
                 }
             }
             // If the right index has not reached the left side of array
             // must now sort the left partition.
             if (lo0 < hi) {
                 qsort(arr, ia, lo0, hi);
             }
             // If the left index has not reached the right side of array
             // must now sort the right partition.
             if (lo < hi0) {
                 qsort(arr, ia, lo, hi0);
             }
         }
     }
 
     /**
      * qsort an array of Objects using the given Comparator.
      */
     private static void qsort(Object a[], int lo0, int hi0, Comparator c) {
         int lo = lo0;
         int hi = hi0;
         int mid;
         if (hi0 > lo0) {
             /*
              * Arbitrarily establishing partition element as the midpoint of the
              * array.
              */
             mid = (lo0 + hi0) / 2;
             Object mo = a[mid];
             // loop through the array until indices cross
             while (lo <= hi) {
                 /*
                  * find the first element that is greater than or equal to the
                  * partition element starting from the left Index.
                  */
                 while ((lo < hi0) && (c.compare(a[lo], mo) < 0)) {
                     ++lo;
                 }
                 /*
                  * find an element that is smaller than or equal to the
                  * partition element starting from the right Index.
                  */
                 while ((hi > lo0) && (c.compare(a[hi], mo) > 0)) {
                     --hi;
                 }
                 // if the indexes have not crossed, swap
                 if (lo <= hi) {
                     if (lo < hi) {
                         swap(a, lo, hi);
                     }
                     ++lo;
                     --hi;
                 }
             }
             /*
              * If the right index has not reached the left side of array must
              * now sort the left partition.
              */
             if (lo0 < hi) {
                 qsort(a, lo0, hi, c);
             }
             /*
              * If the left index has not reached the right side of array must
              * now sort the right partition.
              */
             if (lo < hi0) {
                 qsort(a, lo, hi0, c);
             }
         }
     }
 
     /**
      * Sorts both arrays based on the values in the valueArray array.
      *
      * @param valueArray The values to sort.
      * @param indexArray An index array that is sorted to provide an index from
      * the orignal order.
      */
     public static void sort(double valueArray[], int indexArray[]) {
         qsort(valueArray, indexArray, 0, valueArray.length - 1);
     }
 
     /**
      * Get a sort index array for the list of values. The list itself will not
      * be sorted.
      *
      * @param cells The values to sort.
      * @return An index array that provides that accesses the list in sorted
      * order.
      */
     public static int[] getSortIndex(List cells) {
         if (cells == null) {
             return null;
         }
         Object a[] = new Object[cells.size()];
         cells.toArray(a);
         int idx[] = new int[a.length];
         for (int i = 0; i < idx.length; i++) {
             idx[i] = i;
         }
         qsort(a, idx, 0, a.length - 1, defaultComparator);
         return idx;
     }
 
     /**
      * Get a sort index array for the list of values. Use the comparator given
      * to compare values of the list. The list itself will not be sorted.
      *
      * @param cells The values to sort.
      * @param comparator The comparator for sorting values.
      * @return An index array that provides that accesses the list in sorted
      * order.
      */
     public static int[] getSortIndex(List cells, Comparator comparator) {
         if (cells == null) {
             return null;
         }
         Object a[] = new Object[cells.size()];
         cells.toArray(a);
         int idx[] = new int[a.length];
         for (int i = 0; i < idx.length; i++) {
             idx[i] = i;
         }
         qsort(a, idx, 0, a.length - 1, comparator);
         return idx;
     }
 
     /**
      * Compare rows from a table based on the values of the columns in the given
      * columnIndex array.
      *
      * @param tm The tableModel to be sorted.
      * @param lsm The row selection model for the tableModel to be sorted.
      * @param columnIndex The column indices on which to sort, The value for a
      * column index is interpretted as follows: if the index == ROWNUM_ASCENDING
      * sort by table row number if the index == ROWNUM_DESCENDING sort by
      * reverse table row number if the index == SELECTED_ROWS_FIRST sort by row
      * selection status if the index == SELECTED_ROWS_LAST sort by row selection
      * status if positive sort ascending, if negative sort descending and the
      * column index = Math.abs(value) - 1.
      * @param rowIndex1 The first row index of the rows to compare.
      * @param rowIndex2 The other row index of the rows to compare.
      * @return negative if rowIndex1 should be sorted before rowIndex2, positive
      * if rowIndex1 is sorted after rowIndex2, else zero.
      * @see #ROWNUM_ASCENDING
      * @see #ROWNUM_DESCENDING
      */
     private static int compareRows(TableModel tm, ListSelectionModel lsm, int columnIndex[],
             int rowIndex1, int rowIndex2, Comparator c) throws InterruptedException {
         if (Thread.interrupted()) {
             throw new InterruptedException("Sort Interrupted");
         }
         if (rowIndex1 == rowIndex2) {
             return 0;
         }
         int ord = 0;
         Object obj1;
         Object obj2;
         for (int i = 0; i < columnIndex.length; i++) {
             int col = Math.abs(columnIndex[i]);
             if (columnIndex[i] >= 0) {
                 if (columnIndex[i] == ROWNUM_ASCENDING) {
                     return rowIndex1 - rowIndex2;
                 } else if (columnIndex[i] == SELECTED_ROWS_FIRST) {
                     if (lsm != null) {
                         boolean s1 = lsm.isSelectedIndex(rowIndex1);
                         boolean s2 = lsm.isSelectedIndex(rowIndex2);
                         if (s1 != s2) {
                             return s1 && !s2 ? -1 : 1;
                         }
                     }
                 }
                 obj1 = tm.getValueAt(rowIndex1, columnIndex[i]);
                 obj2 = tm.getValueAt(rowIndex2, columnIndex[i]);
             } else {
                 if (columnIndex[i] == ROWNUM_DESCENDING) {
                     return rowIndex2 - rowIndex1;
                 } else if (columnIndex[i] == SELECTED_ROWS_LAST) {
                     if (lsm != null) {
                         boolean s1 = lsm.isSelectedIndex(rowIndex1);
                         boolean s2 = lsm.isSelectedIndex(rowIndex2);
                         if (s1 != s2) {
                             return s1 && !s2 ? 1 : -1;
                         }
                     }
                 }
                 obj1 = tm.getValueAt(rowIndex2, -columnIndex[i] - 1);
                 obj2 = tm.getValueAt(rowIndex1, -columnIndex[i] - 1);
             }
             ord = c.compare(obj1, obj2);
             if (ord != 0) {
                 break;
             }
         }
         return ord;
     }
 
     /**
      * Sort the rows of a table on the given columns.
      *
      * @param tm The tableModel to be sorted.
      * @param lsm The row selection model for the tableModel to be sorted.
      * @param columnIndex The column indices on which to sort, The value for a
      * column index is interpretted as follows: if the index == ROWNUM_ASCENDING
      * sort by table row number if the index == ROWNUM_DESCENDING sort by
      * reverse table row number if the index == SELECTED_ROWS_FIRST sort by row
      * selection status if the index == SELECTED_ROWS_LAST sort by row selection
      * status if positive sort ascending, if negative sort descending and the
      * column index = Math.abs(value) - 1.
      * @return An index array that gives the sort order for the table.
      */
     private static void qsort(TableModel tm, ListSelectionModel lsm, int ia[],
             int lo0, int hi0, int ci[], Comparator comparator) throws InterruptedException {
         int lo = lo0;
         int hi = hi0;
         int mid;
         if (hi0 > lo0) {
             // Arbitrarily establishing partition element as the midpoint of the array.
             mid = ia[(lo0 + hi0) / 2];
             // loop through the array until indices cross
             while (lo <= hi) {
                 // find the first element that is greater than or equal to
                 //  the partition element starting from the left Index.
                 while ((lo < hi0) && (compareRows(tm, lsm, ci, ia[lo], mid, comparator) < 0)) {
                     ++lo;
                 }
                 // find an element that is smaller than or equal to
                 //  the partition element starting from the right Index.
                 while ((hi > lo0) && (compareRows(tm, lsm, ci, ia[hi], mid, comparator) > 0)) {
                     --hi;
                 }
                 // if the indexes have not crossed, swap
                 if (lo <= hi) {
                     if (lo < hi) {
                         swap(ia, lo, hi);
                     }
                     ++lo;
                     --hi;
                 }
             }
             // If the right index has not reached the left side of array
             // must now sort the left partition.
             if (lo0 < hi) {
                 qsort(tm, lsm, ia, lo0, hi, ci, comparator);
             }
             // If the left index has not reached the right side of array
             // must now sort the right partition.
             if (lo < hi0) {
                 qsort(tm, lsm, ia, lo, hi0, ci, comparator);
             }
         }
     }
 
     /**
      * Sort the rows of a table on the given columns, or row selection status,
      * returning the order in an index array. Compare rows from a table based on
      * the values of the columns in the given columnIndex array.
      *
      * @param tm The tableModel to be sorted.
      * @param lsm The row selection model for the tableModel to be sorted.
      * @param columnIndex The column indices on which to sort, The value for a
      * column index is interpretted as follows: if the index == ROWNUM_ASCENDING
      * sort by table row number if the index == ROWNUM_DESCENDING sort by
      * reverse table row number if the index == SELECTED_ROWS_FIRST sort by row
      * selection status if the index == SELECTED_ROWS_LAST sort by row selection
      * status if positive sort ascending, if negative sort descending and the
      * column index = Math.abs(value) - 1.
      * @return An index array that gives the sort order for the table.
      * @see #ROWNUM_ASCENDING
      * @see #ROWNUM_DESCENDING
      */
     public static int[] getSortIndex(TableModel tm, ListSelectionModel lsm, int[] columnIndex)
             throws InterruptedException {
         if (tm == null || columnIndex == null) {
             return null;
         }
         int rcnt = tm.getRowCount();
         int idx[] = new int[rcnt];
         for (int i = 0; i < idx.length; i++) {
             idx[i] = i;
         }
         try {
             qsort(tm, lsm, idx, 0, idx.length - 1, columnIndex, defaultComparator);
         } catch (Exception ex) {
             ExceptionHandler.popupException(""+ex);
         }
         return idx;
     }
 
     public static Vector vsort(List cells) {
         return vsort(cells, defaultComparator);
     }
 
     public static Vector vsort(List cells, Comparator comparator) {
         if (cells == null) {
             return null;
         }
         Object a[] = new Object[cells.size()];
         cells.toArray(a);
         a = sort(a, comparator);
         Vector v = new Vector(a.length);
         for (int i = 0; i < a.length; i++) {
             v.addElement(a[i]);
         }
         return v;
     }
 
     public static Object[] sort(Object[] cells, Comparator comparator) {
         if (cells != null) {
             qsort(cells, 0, cells.length - 1, comparator);
         }
         return cells;
     }
 
     public static Object[] sort(Object[] cells) {
         return sort(cells, defaultComparator);
     }
 
     public static Vector getValuesFrom(TableModel tableModel,
             int fromRowIndex, int fromColumnIndex,
             int toRowIndex, int toColumnIndex) {
         int fr = fromRowIndex < 0 ? 0
                 : fromRowIndex >= tableModel.getRowCount()
                 ? tableModel.getRowCount() - 1
                 : fromRowIndex;
         int tr = toRowIndex < 0 ? 0
                 : toRowIndex >= tableModel.getRowCount()
                 ? tableModel.getRowCount() - 1
                 : toRowIndex;
         int fc = fromColumnIndex < 0 ? 0
                 : fromColumnIndex >= tableModel.getColumnCount()
                 ? tableModel.getColumnCount() - 1
                 : fromColumnIndex;
         int tc = toColumnIndex < 0 ? 0
                 : toColumnIndex >= tableModel.getColumnCount()
                 ? tableModel.getColumnCount() - 1
                 : toColumnIndex;
         int cnt = (int) Math.abs((tc + 1 - fc) * (tr + 1 - fr));
         Vector v = new Vector(cnt);
         if (fc < tc) {
             for (int c = fc; c <= tc; c++) {
                 if (fr < tr) {
                     for (int r = fr; r <= tr; r++) {
                         v.addElement(tableModel.getValueAt(r, c));
                     }
                 } else {
                     for (int r = tr; r >= fr; r--) {
                         v.addElement(tableModel.getValueAt(r, c));
                     }
                 }
             }
         } else {
             for (int c = tc; c >= fc; c--) {
                 if (fr < tr) {
                     for (int r = fr; r <= tr; r++) {
                         v.addElement(tableModel.getValueAt(r, c));
                     }
                 } else {
                     for (int r = tr; r >= fr; r--) {
                         v.addElement(tableModel.getValueAt(r, c));
                     }
                 }
             }
         }
         return v;
     }
 
     public static List distinct(List cells) {
         if (cells != null) {
             int n = cells.size();
             Vector set = new Vector(n);
             Hashtable ht = new Hashtable();
             for (int i = 0; i < n; i++) {
                 Object o = cells.get(i);
                 if (o != null) {
                     if (ht.get(o) == null) {
                         ht.put(o, o);
                         set.addElement(o);
                     }
                 }
             }
             return set;
         }
         return cells;
     }
 
     public static double sum(List cells) {
         double sum = 0.;
         for (ListIterator li = cells.listIterator(); li.hasNext();) {
             Object o = li.next();
             if (o instanceof Number) {
                 sum += ((Number) o).doubleValue();
             }
         }
         return sum;
     }
 
     public static Object median(List cells) {
         Vector ll = vsort(cells);
         return ll.get(ll.size() / 2);
     }
 
     public static double average(List cells) {
         double sum = 0.;
         int count = 0;
         for (ListIterator li = cells.listIterator(); li.hasNext();) {
             Object o = li.next();
             if (o instanceof Number) {
                 sum += ((Number) o).doubleValue();
                 count++;
             }
         }
         if (count < 1) {
             return Double.NaN;
         }
         return sum / count;
     }
 
     public static double variance(List cells) {
         double mean = average(cells);
         return variance(cells, mean);
     }
     // Add methods for Standard deviations ?
 
     public static double variance(List cells, double mean) {
         if (mean == Double.NaN) {
             return Double.NaN;
         }
         double dval = Double.NaN;
         double sum = 0.;
         int count = 0;
         for (ListIterator li = cells.listIterator(); li.hasNext();) {
             Object o = li.next();
             if (o instanceof Number) {
                 dval = ((Number) o).doubleValue();
                 if (!Double.isNaN(dval)) {
                     double diff = dval - mean;
                     sum += diff * diff;
                     count++;
                 }
             }
         }
         if (count < 2) {
             return Double.NaN;
         }
         return sum / (count - 1);
     }
 
     public static double stddev(List cells) {
         return Math.sqrt(variance(cells));
     }
 
     public static double stddev(List cells, double mean) {
         return Math.sqrt(variance(cells, mean));
     }
 
     public static double[] stdvals(List cells) {
         double vals[] = null;
         if (cells != null) {
             double mean = average(cells);
             double stddev = stddev(cells, mean);
             if (stddev != 0 && !Double.isNaN(mean) && !Double.isNaN(stddev)) {
                 vals = new double[cells.size()];
                 for (int i = 0; i < vals.length; i++) {
                     Object o = cells.get(i);
                     if (o != null && o instanceof Number) {
                         double dval = ((Number) o).doubleValue();
                         if (!Double.isNaN(dval)) {
                             vals[i] = (dval - mean) / stddev;
                         }
                     }
                 }
             }
         }
         return vals;
     }
 
     // correlation
     // r = (1/(n-1)) * sum( ((x[i] - mean) / stdvals[i])*((y[i] - mean) / stdvals[i]))
     public static double correlation(List xcells, List ycells) {
         double r = Double.NaN;
         double xsd[] = stdvals(xcells);
         double ysd[] = stdvals(ycells);
         if (xsd != null && ysd != null && xsd.length > 1 && xsd.length == ysd.length) {
             r = 0.;
             int n = xsd.length - 1;
             for (int i = 0; i < xsd.length; i++) {
                 r += xsd[i] * ysd[i] / n;
             }
         }
         return r;
     }
 
     // least squares regression
     // Should have a line class that returns a y val for a given x
     public static LineFormula regressionLine(List xcells, List ycells) {
         double meanx = average(xcells);
         double meany = average(ycells);
         double sx = stddev(xcells, meanx);
         double sy = stddev(ycells, meany);
         double r = correlation(xcells, ycells);
         double m = r * sy / sx;
         double i = meany - m * meanx;
         return new LineFormula(m, i);
     }
 
     public static Vector residuals(List xcells, List ycells) {
         return residuals(xcells, ycells, regressionLine(xcells, ycells));
     }
 
     public static Vector residuals(List xcells, List ycells,
             LineFormula regressionLine) {
         if (xcells == null || ycells == null || regressionLine == null) {
             return null;
         }
         Vector r = new Vector(xcells.size());
         for (int i = 0; i < xcells.size(); i++) {
             double rv = Double.NaN;
             Object xo = xcells.get(i);
             if (xo != null && xo instanceof Number) {
                 double x = ((Number) xo).doubleValue();
                 if (!Double.isNaN(x)) {
                     Object yo = ycells.get(i);
                     if (yo != null && yo instanceof Number) {
                         double y = ((Number) yo).doubleValue();
                         double ry = regressionLine.getY(x);
                         rv = y - ry;
                     }
                 }
             }
             r.addElement(new Double(rv));
         }
         return r;
     }
 
     public static int count(List cells) {
         return cells.size();
     }
 
     public static int count(List cells, Object obj) {
         int count = 0;
         for (ListIterator li = cells.listIterator(); li.hasNext();) {
             Object o = li.next();
             if (obj.equals(o)) {
                 count++;
             }
         }
         return count;
     }
 
     public static int count(List cells, Class javaClass) {
         int count = 0;
         for (ListIterator li = cells.listIterator(); li.hasNext();) {
             Object o = li.next();
             if (javaClass.isInstance(o)) {
                 count++;
             }
         }
         return count;
     }
 
     /**
      * Return the classes that are common to all members of the given
      * collection.
      *
      * @param collection The Vector to examine.
      * @return The common classes for the collection members.
      */
     public static Vector getCommonClasses(List collection) {
         Vector commonClasses = null;
         if (collection != null && collection.size() > 0) {
             for (ListIterator li = collection.listIterator(); li.hasNext();) {
                 Object o = li.next();
                 Vector cl = new Vector();
                 for (Class cc = o.getClass(); cc != null; cc = cc.getSuperclass()) {
                     if (commonClasses == null || commonClasses.contains(cc)) {
                         cl.addElement(cc);
                     }
                 }
                 commonClasses = cl;
             }
         }
         return commonClasses;
     }
 
     /**
      * Return the most specific class that is common to all members of the given
      * collection.
      *
      * @param collection The Collection to examine.
      * @return The common classes for the collection members.
      */
     public static Class getCommonClass(List collection) {
         Vector commonClasses = getCommonClasses(collection);
         if (commonClasses != null && commonClasses.size() > 0) {
             return (Class) commonClasses.get(0);
         }
         return null;
     }
 
     /**
      * Return the interfaces that are common to all members of the given
      * collection.
      *
      * @param collection The Collection to examine.
      * @return The common interfaces for the collection members.
      */
     public static Vector getCommonInterfaces(List collection) {
         Vector commonInterfaces = null;
         if (collection != null && collection.size() > 0) {
             for (ListIterator li = collection.listIterator(); li.hasNext();) {
                 Object o = li.next();
                 Class ic[] = o.getClass().getInterfaces();
                 Vector vi = new Vector();
                 for (int j = 0; j < ic.length; j++) {
                     if ((commonInterfaces == null || commonInterfaces.contains(ic[j]))
                             && !vi.contains(ic[j])) {
                         vi.addElement(ic[j]);
                     }
                 }
                 commonInterfaces = vi;
             }
         }
         return commonInterfaces;
     }
 
     // Algorithms
     /**
      * Sorts the specified list into ascending order, according to the
      * <i>natural ordering</i> of its elements. All elements in the list must
      * implement the <tt>Comparable</tt> interface. Furthermore, all elements in
      * the list must be <i>mutually comparable</i> (that is,
      * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
      * for any elements <tt>e1</tt> and <tt>e2</tt> in the list).<p>
      *
      * This sort is guaranteed to be <i>stable</i>: equal elements will not be
      * reordered as a result of the sort.<p>
      *
      * The specified list must be modifiable, but need not be resizable.<p>
      *
      * The sorting algorithm is a modified mergesort (in which the merge is
      * omitted if the highest element in the low sublist is less than the lowest
      * element in the high sublist). This algorithm offers guaranteed n log(n)
      * performance, and can approach linear performance on nearly sorted
      * lists.<p>
      *
      * This implementation dumps the specified list into an array, sorts the
      * array, and iterates over the list resetting each element from the
      * corresponding position in the array. This avoids the n<sup>2</sup> log(n)
      * performance that would result from attempting to sort a linked list in
      * place.
      *
      * @param list the list to be sorted.
      * @return the sorted list.
      * @throws ClassCastException if the list contains elements that are not
      * <i>mutually comparable</i> (for example, strings and integers).
      * @throws UnsupportedOperationException if the specified list's
      * list-iterator does not support the <tt>set</tt> operation.
      * @see Comparable
      */
     public static List sort(List list) {
         return sort(list, defaultComparator);
     }
 
     /**
      * Sorts the specified list according to the order induced by the specified
      * comparator. All elements in the list must be <i>mutually comparable</i>
      * using the specified comparator (that is, <tt>c.compare(e1, e2)</tt> must
      * not throw a <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
      * <tt>e2</tt> in the list).<p>
      *
      * This sort is guaranteed to be <i>stable</i>: equal elements will not be
      * reordered as a result of the sort.<p>
      *
      * The sorting algorithm is a modified mergesort (in which the merge is
      * omitted if the highest element in the low sublist is less than the lowest
      * element in the high sublist). This algorithm offers guaranteed n log(n)
      * performance, and can approach linear performance on nearly sorted
      * lists.<p>
      *
      * The specified list must be modifiable, but need not be resizable. This
      * implementation dumps the specified list into an array, sorts the array,
      * and iterates over the list resetting each element from the corresponding
      * position in the array. This avoids the n<sup>2</sup> log(n) performance
      * that would result from attempting to sort a linked list in place.
      *
      * @param list the list to be sorted.
      * @param c the comparator to determine the order of the array.
      * @return the sorted list.
      * @throws ClassCastException if the list contains elements that are not
      * <i>mutually comparable</i> using the specified comparator.
      * @throws UnsupportedOperationException if the specified list's
      * list-iterator does not support the <tt>set</tt> operation.
      * @see Comparator
      */
     public static List sort(List list, Comparator c) {
         if (list == null) {
             return list;
         }
         Object a[] = new Object[list.size()];
         list.toArray(a);
         a = sort(a, c);
         for (int i = 0; i < a.length; i++) {
             list.set(i, a[i]);
         }
         return list;
     }
 
     /**
      * Searches the specified list for the specified object using the binary
      * search algorithm. The list must be sorted into ascending order according
      * to the <i>natural ordering</i> of its elements (as by the
      * <tt>sort(List)</tt> method, above) prior to making this call. If it is
      * not sorted, the results are undefined. If the list contains multiple
      * elements equal to the specified object, there is no guarantee which one
      * will be found.<p>
      *
      * This method runs in log(n) time for a "random access" list (which
      * provides near-constant-time positional access). It may run in n log(n)
      * time if it is called on a "sequential access" list (which provides
      * linear-time positional access).</p>
      *
      * If the specified list implements the <tt>AbstracSequentialList</tt>
      * interface, this method will do a sequential search instead of a binary
      * search; this offers linear performance instead of n log(n) performance if
      * this method is called on a <tt>LinkedList</tt> object.
      *
      * @param list the list to be searched.
      * @param key the key to be searched for.
      * @return index of the search key, if it is contained in the list;
      * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion
      * point</i> is defined as the point at which the key would be inserted into
      * the list: the index of the first element greater than the key, or
      * <tt>list.size()</tt>, if all elements in the list are less than the
      * specified key. Note that this guarantees that the return value will be
      * &gt;= 0 if and only if the key is found.
      * @throws ClassCastException if the list contains elements that are not
      * <i>mutually comparable</i> (for example, strings and integers), or the
      * search key in not mutually comparable with the elements of the list.
      * @see Comparable
      * @see #sort(List)
      */
     public static int binarySearch(List list, Object key) {
         return binarySearch(list, key, defaultComparator);
     }
 
     /**
      * Searches the specified list for the specified object using the binary
      * search algorithm. The list must be sorted into ascending order according
      * to the specified comparator (as by the <tt>Sort(List, Comparator)</tt>
      * method, above), prior to making this call. If it is not sorted, the
      * results are undefined. If the list contains multiple elements equal to
      * the specified object, there is no guarantee which one will be found.<p>
      *
      * This method runs in log(n) time for a "random access" list (which
      * provides near-constant-time positional access). It may run in n log(n)
      * time if it is called on a "sequential access" list (which provides
      * linear-time positional access).</p>
      *
      * If the specified list implements the <tt>AbstracSequentialList</tt>
      * interface, this method will do a sequential search instead of a binary
      * search; this offers linear performance instead of n log(n) performance if
      * this method is called on a <tt>LinkedList</tt> object.
      *
      * @param list the list to be searched.
      * @param key the key to be searched for.
      * @param c the comparator by which the list is ordered.
      * @return index of the search key, if it is contained in the list;
      * otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion
      * point</i> is defined as the point at which the key would be inserted into
      * the list: the index of the first element greater than the key, or
      * <tt>list.size()</tt>, if all elements in the list are less than the
      * specified key. Note that this guarantees that the return value will be
      * &gt;= 0 if and only if the key is found.
      * @throws ClassCastException if the list contains elements that are not
      * <i>mutually comparable</i> using the specified comparator, or the search
      * key in not mutually comparable with the elements of the list using this
      * comparator.
      * @see Comparable
      * @see #sort(List, Comparator)
      */
     public static int binarySearch(List list, Object key, Comparator c) {
         int low = 0;
         int high = list.size() - 1;
         while (low <= high) {
             int mid = (low + high) / 2;
             Object midVal = list.get(mid);
             int cmp = c.compare(midVal, key);
             if (cmp < 0) {
                 low = mid + 1;
             } else if (cmp > 0) {
                 high = mid - 1;
             } else {
                 return mid; // key found
             }
         }
         return -(low + 1);  // key not found
     }
 
     /**
      * Reverses the order of the elements in the specified list.<p>
      *
      * This method runs in linear time.
      *
      * @param list the list whose elements are to be reversed.
      * @return the reversed list.
      * @throws UnsupportedOperationException if the specified list's
      * list-iterator does not support the <tt>set</tt> operation.
      */
     public static List reverse(List list) {
         if (list != null) {
             for (int i = 0, j = list.size() - 1; i < j; i++, j--) {
                 Object tmp = list.get(i);
                 list.set(i, list.get(j));
                 list.set(j, tmp);
             }
         }
         return list;
     }
     private static Random random = new Random();
 
     /**
      * Randomly permutes the specified list using a default source of
      * randomness. All permutations occur with approximately equal
      * likelihood.<p>
      *
      * The hedge "approximately" is used in the foregoing description because
      * default source of randomenss is only approximately an unbiased source of
      * independently chosen bits. If it were a perfect source of randomly chosen
      * bits, then the algorithm would choose permutations with perfect
      * uniformity.<p>
      *
      * This implementation traverses the list backwards, from the last element
      * up to the second, repeatedly swapping a randomly selected element into
      * the "current position". Elements are randomly selected from the portion
      * of the list that runs from the first element to the current position,
      * inclusive.<p>
      *
      * This method runs in linear time for a "random access" list (which
      * provides near-constant-time positional access). It may require quadratic
      * time for a "sequential access" list.
      *
      * @param list the list to be shuffled.
      * @return the shuffled list.
      * @throws UnsupportedOperationException if the specified list's
      * list-iterator does not support the <tt>set</tt> operation.
      */
     public static List shuffle(List list) {
         return shuffle(list, random);
     }
 
     /**
      * Randomly permute the specified list using the specified source of
      * randomness. All permutations occur with equal likelihood assuming that
      * the source of randomness is fair.<p>
      *
      * This implementation traverses the list backwards, from the last element
      * up to the second, repeatedly swapping a randomly selected element into
      * the "current position". Elements are randomly selected from the portion
      * of the list that runs from the first element to the current position,
      * inclusive.<p>
      *
      * This method runs in linear time for a "random access" list (which
      * provides near-constant-time positional access). It may require quadratic
      * time for a "sequential access" list.
      *
      * @param list the list to be shuffled.
      * @param rnd the source of randomness to use to shuffle the list.
      * @return the shuffled list.
      * @throws UnsupportedOperationException if the specified list's
      * list-iterator does not support the <tt>set</tt> operation.
      */
     public static List shuffle(List list, Random rnd) {
         if (list == null) {
             return list;
         }
         for (int i = list.size() - 1; i > 1; i--) {
             int j = (int) (rnd.nextDouble() * (i - 1));
             Object tmp = list.get(i);
             list.set(i, list.get(j));
             list.set(j, tmp);
         }
         return list;
     }
 
     /**
      * Replaces all of the elements of the specified list with the specified
      * element. <p>
      *
      * This method runs in linear time.
      *
      * @param list the list to be filled with the specified element.
      * @param o The element with which to fill the specified list.
      * @return the list.
      * @throws UnsupportedOperationException if the specified list's
      * list-iterator does not support the <tt>set</tt> operation.
      */
     public static List fill(List list, Object o) {
         for (int i = 0; i < list.size(); i++) {
             list.set(i, o);
         }
         return list;
     }
 
     /**
      * Copies all of the elements from one list into another. After the
      * operation, the index of each copied element in the destination list will
      * be identical to its index in the source list. The destination list must
      * be at least as long as the source list. If it is longer, the remaining
      * elements in the destination list are unaffected. <p>
      *
      * This method runs in linear time.
      *
      * @param dest The destination list.
      * @param src The source list.
      * @return the dest list.
      * @throws IndexOutOfBoundsException if the destination list is too small to
      * contain the entire source List.
      * @throws UnsupportedOperationException if the destination list's
      * list-iterator does not support the <tt>set</tt> operation.
      */
     public static List copy(List dest, List src) {
         for (int i = 0; i < src.size(); i++) {
             if (i < dest.size()) {
                 dest.set(i, src.get(i));
             } else {
                 dest.add(src.get(i));
             }
         }
         return dest;
     }
 
     /**
      * Returns the minimum element of the given collection, according to the
      * <i>natural ordering</i> of its elements. All elements in the collection
      * must implement the <tt>Comparable</tt> interface. Furthermore, all
      * elements in the collection must be <i>mutually comparable</i> (that is,
      * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
      * for any elements <tt>e1</tt> and <tt>e2</tt> in the collection).<p>
      *
      * This method iterates over the entire collection, hence it requires time
      * proportional to the size of the collection.
      *
      * @param coll the collection whose minimum element is to be determined.
      * @return the minimum element of the given collection, according to the
      * <i>natural ordering</i> of its elements.
      * @throws ClassCastException if the collection contains elements that are
      * not <i>mutually comparable</i> (for example, strings and integers).
      * @throws NoSuchElementException if the collection is empty.
      * @see Comparable
      */
     public static Object min(List coll) {
         try {
             if (Class.forName("java.lang.Number").equals(getCommonClass(coll))) {
                 return min(coll, numberComparator);
             }
         } catch (Exception ex) {
             ExceptionHandler.popupException(""+ex);
         }
         return min(coll, defaultComparator);
     }
 
     /**
      * Returns the minimum element of the given collection, according to the
      * order induced by the specified comparator. All elements in the collection
      * must be <i>mutually comparable</i> by the specified comparator (that is,
      * <tt>comp.compare(e1, e2)</tt> must not throw a
      * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and <tt>e2</tt>
      * in the collection).<p>
      *
      * This method iterates over the entire collection, hence it requires time
      * proportional to the size of the collection.
      *
      * @param coll the collection whose minimum element is to be determined.
      * @return the minimum element of the given collection, according to the
      * specified comparator.
      * @throws ClassCastException if the collection contains elements that are
      * not <i>mutually comparable</i> using the specified comparator.
      * @throws NoSuchElementException if the collection is empty.
      * @see Comparable
      */
     public static Object min(List coll, Comparator comp) {
         Object min = null;
         if (coll != null) {
             for (int i = 0; i < coll.size(); i++) {
                 Object val = coll.get(i);
                 if (val != null) {
                     if (min == null || comp.compare(val, min) < 0) {
                         min = val;
                     }
                 }
             }
         }
         return min;
     }
 
     /**
      * Returns the maximum element of the given collection, according to the
      * <i>natural ordering</i> of its elements. All elements in the collection
      * must implement the <tt>Comparable</tt> interface. Furthermore, all
      * elements in the collection must be <i>mutually comparable</i> (that is,
      * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
      * for any elements <tt>e1</tt> and <tt>e2</tt> in the collection).<p>
      *
      * This method iterates over the entire collection, hence it requires time
      * proportional to the size of the collection.
      *
      * @param coll the collection whose maximum element is to be determined.
      * @return the maximum element of the given collection, according to the
      * <i>natural ordering</i> of its elements.
      * @throws ClassCastException if the collection contains elements that are
      * not <i>mutually comparable</i> (for example, strings and integers).
      * @throws NoSuchElementException if the collection is empty.
      * @see Comparable
      */
     public static Object max(List coll) {
         try {
             if (Class.forName("java.lang.Number").equals(getCommonClass(coll))) {
                 return max(coll, numberComparator);
             }
         } catch (Exception ex) {
             ExceptionHandler.popupException(""+ex);
         }
         return max(coll, defaultComparator);
     }
 
     /**
      * Returns the maximum element of the given collection, according to the
      * order induced by the specified comparator. All elements in the collection
      * must be <i>mutually comparable</i> by the specified comparator (that is,
      * <tt>comp.compare(e1, e2)</tt> must not throw a
      * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and <tt>e2</tt>
      * in the collection).<p>
      *
      * This method iterates over the entire collection, hence it requires time
      * proportional to the size of the collection.
      *
      * @param coll the collection whose maximum element is to be determined.
      * @return the maximum element of the given collection, according to the
      * specified comparator.
      * @throws ClassCastException if the collection contains elements that are
      * not <i>mutually comparable</i> using the specified comparator.
      * @throws NoSuchElementException if the collection is empty.
      * @see Comparable
      */
     public static Object max(List coll, Comparator comp) {
         Object max = null;
         if (coll != null) {
             for (int i = 0; i < coll.size(); i++) {
                 Object val = coll.get(i);
                 if (val != null) {
                     if (max == null || comp.compare(val, max) > 0) {
                         max = val;
                     }
                 }
             }
         }
         return max;
     }
 }
