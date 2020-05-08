 
 /*
  *
  *  RowIndex
  *  Copyright (C) 2012 Gaurav Vaidya
  *
  *  This file is part of TaxRef.
  *
  *  TaxRef is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TaxRef is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TaxRef.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.ggvaidya.TaxRef.Model;
 
 import com.ggvaidya.TaxRef.Common.*;
 import java.util.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 /**
  * A RowIndex indexes rows. Ideally, Rows would be Java objects, but that
  * takes up too much memory, so they're just an array here. We index columns
  * of interest, such as names and taxonids.
  * 
  * Note that this is EXTREMELY thread-unsafe: different threads could change
  * the index, or change a value, without other threads noticing, causing odd
  * changes. I'm not going to bother to fix this, since TaxRef should never have
  * more than two RowIndexes at any time, but if you do thready-stuff with this,
  * PLEASE make sure to sort this out.
  * 
  * @author Gaurav Vaidya <gaurav@ggvaidya.com>
  */
 public class RowIndex implements TableModel {
 	/** 
 	 * A list of columns, in lowercase, in the right order. This is the list
 	 * we use for figuring out column indexes and order, since we want column
 	 * name matches to be case-insensitive as far as possible.
 	 */
 	private List<String> columnsLowercase;
 	
 	/** 
 	 * A list of columns, in the right order. Since we use lowercased column
 	 * names for most things, this is really only for display and export.
 	 */
 	private List<String> columns;
 	
 	/**
 	 * A map of columns to their classes. This is used a LOT by TaxRef: columns
 	 * classified Name are the only ones rendered specially; eventually, we'd
 	 * like to support Integer and Float columns, which will support more advanced
 	 * summaries.
 	 * 
 	 * Note that this is completely independent of the column system; it's
 	 * perfectly possible for column names to be added here but not actually
 	 * exist in columnsLowercase. Which is fine, just as long as you know that
 	 * this is a thing that might happen.
 	 */
 	private HashMap<String, Class> columnClasses;
 	
 	/**
 	 * A list of rows. Being a list allows us to rearrange rows if we ever want 
 	 * to. Unfortunately, because of the array-of-strings restriction, we need
 	 * to do a pretty costly sequence of array copying when inserting a new 
 	 * column. So far, that seems to add around 4 seconds and ~100 MB to opening
 	 * ITIS; if we can save the memory increase, it's probably good enough.
 	 * 
 	 * Note that changing this into a LinkedList takes up ~100 MB. I have no 
 	 * idea why. Are pointers really that much of a memory hog?
 	 */
 	private List<Object[]> rows = new ArrayList<Object[]>();
 	
 	/**
 	 * A map of Names against the rows which refer to them. This can
 	 * be used to summarise information about particular names, or check for
 	 * overlaps (one name used in multiple columns in the same row), and so on.
 	 * 
 	 * This essential scheme will probably be reused for taxonids and other
 	 * things we'd like to track.
 	 */
 	private Map<Name, List<Object[]>> nameIndex = new HashMap<Name, List<Object[]>>();
 	
 	/**
 	 * @return A textual description of this object.
 	 */
 	@Override
 	public String toString() {
 		return "RowIndex of " + rows.size() + " rows across " + columns.size() + " columns, " + nameIndex.size() + " names indexed.";
 	}
 	
 	/**
 	 * Create a new RowIndex based off a set of column names and classes. This
 	 * was really just the simplest way to initialise this object from DarwinCSV.
 	 * It would be equally straightforward to do nothing here and write another
 	 * method to bulk-add a set of columns, but this is simpler for now.
 	 * 
 	 * @param columnNames A list of column names, in the correct order.
 	 * @param classes A list of column classes, in the correct order.
 	 */
 	public RowIndex(List<String> columnNames, List<Class> classes) {
 		columns = new ArrayList<String>(columnNames);
 		columnsLowercase = new ArrayList<String>(columns.size());
 		columnClasses = new HashMap<String, Class>();
 		
 		// Lowercase the column names and put them into columnsLowercase.
 		int index = 0;
 		for(String colName: columns) {
 			Class colClass = classes.get(index);
 			
 			columnsLowercase.add(colName.toLowerCase());
 			columnClasses.put(colName.toLowerCase(), colClass);
 			
 			index++;
 		}
 	}
 	
 	/**
 	 * Index the provided value in the provided row. This tries to add the value
 	 * to our indexes. Only particular classes (currently, only Name) will actually
 	 * be indexed; others will be silently ignored.
 	 * 
 	 * Some assumptions:
 	 *  - 'obj' has a distinct class so we know what to do with it. Right now, we
 	 *    only support obj of class Name, but we will eventually use URI to identify
 	 *	  identifiers.
 	 *  - This means null values are never indexed.
 	 *	- 'row' is complete and in the right order. In general, this should have
 	 *    come from a {@link #getRows()} or {@link #getNameRows(java.lang.String)} 
 	 *    call.
 	 * 
 	 * @param obj The value to index.
 	 * @param row The row this value appears in. The index supports indexing multiple
 	 *		values for the same row and the same value in multiple rows, but not
 	 *		multiple values in the same row.
 	 * @return True if the value was indexed, false otherwise.
 	 */
 	public boolean indexValue(Object obj, Object[] row) {
 		if(obj == null)
 			return false;
 		
 		if(Name.class.isAssignableFrom(obj.getClass())) {
 			// Index the names.
 			Name nameToIndex = (Name) obj;
 				
 			if(!nameIndex.containsKey(nameToIndex))
 				nameIndex.put(nameToIndex, new ArrayList<Object[]>());
 			
 			nameIndex.get(nameToIndex).add(row);
 			// System.err.println("Adding '" + nameToIndex + "' to name index.");
 			
 			return true;
 			
 		} else {
 			// Not a name? then no index for you.
 			return false;
 		}
 	}
 	
 	/**
 	 * Set column class. Note that this will IMMEDIATELY change the way that
 	 * this column is interpreted and displayed: if you're trying to change the 
 	 * class of an existing column, it's generally a lot easier/smarter to
 	 * create a new column (using {@link #createNewColumn(java.lang.String, int, com.ggvaidya.TaxRef.Common.ArrayMapOperation) })
 	 * instead of trying to change a column type on the fly. In TaxRef, this
 	 * is mainly used to set up a column class *before* the new column is
 	 * created.
 	 * 
 	 * @param colName The name of the column who's class needs setting.
 	 * @param colClass The class to set it to.
 	 */
 	public void setColumnClass(String colName, Class colClass) {
 		columnClasses.put(colName.toLowerCase(), colClass);
 	}
 	
 	/**
 	 * Checks if a particular column name is being used.
 	 * 
 	 * @param colName The column name to check.
 	 * @return True if this column exists, false otherwise.
 	 */
 	public boolean hasColumn(String colName) {
 		return columnsLowercase.contains(colName.toLowerCase());
 	}
 	
 	/**
 	 * Returns the index of the column. These are zero-based indexes.
 	 * 
 	 * @param colName The column name whose index is required.
 	 * @return The index of the column, or -1 if no such column exists.
 	 */
 	public int getColumnIndex(String colName) {
 		return columnsLowercase.indexOf(colName.toLowerCase());
 	}
 	
 	/**
 	 * Creates a new column based on an existing column's data. This ended up
 	 * being a special case of ArrayMapOperation.
 	 * 
 	 * It's a good idea to set a class for the new column beforehand.
 	 * 
 	 * @param newColumn The name of the new column.
 	 * @param insertAt The column index at which to insert the new column.
 	 * @param fromColumn The column to send data to the MapOperation from.
 	 * @param mop The MapOperation to perform on the data in fromColumn.
 	 */
 	public void createNewColumn(String newColumn, int insertAt, String fromColumn, MapOperation mop) {
 		int fromColumnIndex = getColumnIndex(fromColumn);
 		
 		// Yup, we just encapsulate the MapOperation inside an ArrayMapOperation.
 		// Ain't I a stinker?
 		class ArrayMapToMapOp implements ArrayMapOperation {
 			private int column;
 			private MapOperation mop;
 			
 			public ArrayMapToMapOp(MapOperation mop, int column) {
 				this.mop = mop;
 				this.column = column;
 			}
 		
 			@Override
 			public Object mapTo(Object[] values) {
 				return mop.mapTo(values[column]);
 			}
 		}
 		
 		createNewColumn(newColumn, insertAt, new ArrayMapToMapOp(mop, fromColumnIndex));
 	}
 	
 	/**
 	 * Creates a new column based on data in existing rows. The ArrayMapOperation is
 	 * given the entire row Object array, and is then free to do with it as it pleases.
 	 * 
 	 * It's a good idea to set a class for the new column beforehand.
 	 * 
 	 * @param newColumn The new column name.
 	 * @param insertAt The index to insert the new column at.
 	 * @param mop The ArrayMapOperation which determines values for the new array.
 	 */
 	public void createNewColumn(String newColumn, int insertAt, ArrayMapOperation mop) {
 		// Make the new column. 
 		columns.add(insertAt, newColumn);
 		columnsLowercase.add(insertAt, newColumn);
 				
 		// Iterate over all the rows, copying each row into the new one, 
 		// inserting the new object as we go.
 		List<Object[]> new_rows = new ArrayList<Object[]>(rows.size());
 		for(Object[] row: rows) {
 			Object[] new_row = new Object[row.length + 1];
 			
 			// 0...1...2...3...4...5...6...7...8...9...10
 			//             ^
 			// copy(0..2)->(0..2)
 			// insert 3
 			// copy(3..10)->(4..11) [7]
 			
 			System.arraycopy(row, 0, new_row, 0, insertAt);
 			Object toInsert = mop.mapTo(row);
 			indexValue(toInsert, row);
 			new_row[insertAt] = toInsert;
 			System.arraycopy(row, insertAt, new_row, insertAt + 1, new_row.length - insertAt - 1);
 			
 			new_rows.add(new_row);
 		}
 		
 		rows = new_rows;
 	}
 	
 	/**
 	 * Adds a new row to this dataset. Please make sure your columns are right,
 	 * otherwise everything will go nuts!
 	 * 
 	 * We index every possible value. This means that Name objects in non-Name
 	 * columns will be indexed! This may be a good or bad thing.
 	 * 
 	 * @param row An array of Objects which forms the new row.
 	 */
 	public void addRow(Object[] row) {
 		rows.add(row);
 		
 		// O(n)
 		for(int x = 0; x < row.length; x++) {
 			indexValue(row[x], row);
 		}
 	}
 	
 	/**
 	 * Returns a mutable list of rows. Again, it's a *mutable* list, so be
 	 * careful!
 	 * 
 	 * @return A list of all the rows we have.
 	 */
 	public List<Object[]> getRows() {
 		return rows;
 	}
 	
 	/**
 	 * Returns a list of all the column names. This is a copy, so tamper at
 	 * will!
 	 * 
 	 * @return A list of all column names.
 	 */
 	public List<String> getColumnNames() {
 		return new ArrayList(columns);
 	}
 	
 	/**
 	 * Returns a list of lower-cased column names. This is a copy, so tamper
 	 * at will!
 	 * 
 	 * @return A list of all (lower-cased) column names.
 	 */
 	public List<String> getColumnNamesLowercase() {
 		return new ArrayList(columnsLowercase);
 	}
 
 	/**
 	 * Checks whether this name exist in the name index. This method is used a
 	 * lot, and should be optimised to within an inch of its life.
 	 * 
 	 * @param n The name to query.
 	 * @return True if this name exists in the name index, false otherwise.
 	 */
	public boolean hasName(Name name) {
		if(name == null)
 			return false;
 		
 		// System.err.println("And still nothing for '" + n.getNamestringLC() + "': " + nameIndex.containsKey(n.getNamestringLC()));
 		
		return nameIndex.containsKey(name);
 	}
 	
 	/**
 	 * Checks whether a name-string exists in the name index. We just convert
 	 * the name-string into a Name and use that instead.
 	 * 
 	 * @param str The name-string to query.
 	 * @return True if this name exists, false otherwise.
 	 */
 	public boolean hasName(String str) {
 		if(str == null)
 			return false;
 		
 		// System.err.println("Ooo err: " + nameIndex.containsKey("palmaria palmata"));
 		// System.err.println("And yet nothing for '" + str.toLowerCase() + "': " + nameIndex.containsKey(str.toLowerCase()));
 		
 		return nameIndex.containsKey(Name.getName(str));
 	}
 
 	/**
 	 * Get all rows indexed against a particular name.
 	 * 
 	 * @param name The name to look up.
 	 * @return The list of rows, or null if no match could be found.
 	 */
 	public List<Object[]> getNameRows(Name name) {
 		if(nameIndex == null || name == null)
 			return null;
 		
 		return nameIndex.get(name);
 	}
 	
 	/* Matching code */
 	public RowIndexMatch matchAgainst(RowIndex against) {
 		return new RowIndexMatch(this, against);
 	}
 
 	/*
 	 * 
 	 * TABLE MODEL
 	 * 
 	 */
 
 	/**
 	 * @return The number of rows.
 	 */
 	@Override
 	public int getRowCount() {
 		return rows.size();
 	}
 	
 	/**
 	 * @return The number of rows.
 	 */
 	public int size() {
 		return rows.size();
 	}
 
 	/**
 	 * @return The number of columns.
 	 */
 	@Override
 	public int getColumnCount() {
 		return columns.size();
 	}
 
 	/**
 	 * Returns the name of the column at this index.
 	 * 
 	 * @param columnIndex The column index.
 	 * @return The name of the specified column.
 	 */
 	@Override
 	public String getColumnName(int columnIndex) {
 		return columns.get(columnIndex);
 	}
 
 	/**
 	 * Return the class of this column.
 	 * 
 	 * @param columnIndex The column index to check for column class.
 	 * @return The Class of this column index.
 	 */
 	@Override
 	public Class<?> getColumnClass(int columnIndex) {
 		return columnClasses.get(getColumnName(columnIndex).toLowerCase());
 	}
 
 	/**
 	 * Specifies whether a cell is editable. At the moment, EVERY cell is
 	 * editable. That's how it works.
 	 * 
 	 * @param rowIndex The index of the row.
 	 * @param columnIndex The index of the column.
 	 * @return Returns true if this cell should be editable, returns false
 	 *		if not.
 	 */
 	@Override
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return true;
 	}
 
 	/**
 	 * Returns the value at a particular cell identified by 
 	 * (rowIndex, columnIndex).
 	 * 
 	 * @param rowIndex The row index.
 	 * @param columnIndex The column index.
 	 * @return The value at the specified cell.
 	 */
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		// O(1), since we're just doing two array look ups here.
 		return rows.get(rowIndex)[columnIndex];
 	}
 
 	/**
 	 * Set value at the particular cell identified by (rowIndex, columnIndex).
 	 * 
 	 * @param aValue The value to set.
 	 * @param rowIndex The row index to set the value to.
 	 * @param columnIndex The column index to set the value to.
 	 */
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		if(aValue == null)
 			aValue = "";
 		
 		// Delete the previous value from the index.
 		Object prevValue = getValueAt(rowIndex, columnIndex);
 		if(Name.class.isAssignableFrom(prevValue.getClass())) {
 			Name prevName = (Name) prevValue;
 			Object[] row = rows.get(rowIndex);
 			nameIndex.get(prevName).remove(row);
 		}
 		
 		// Special case: if the aValue is a String (like if the user entered a
 		// new name), but the column is a Name column, then automatically create
 		// a new Name object for it.
 		if(Name.class.isAssignableFrom(getColumnClass(columnIndex)) &&
 			String.class.isAssignableFrom(aValue.getClass())) {
 			aValue = Name.getName((String)aValue);
 		}
 		
 		// System.err.println("Updated value at (" + rowIndex + ", " + columnIndex + ") from '" + 
 		// 		rows.get(rowIndex)[columnIndex] + "' to '" + aValue + "'");
 		
 		// Set the value and index it.
 		Object[] row = rows.get(rowIndex);
 		row[columnIndex] = aValue;
 		indexValue(aValue, row);
 		
 		// Inform all the TableModelListeners of the change.
 		for(TableModelListener tml: listeners) {
 			tml.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
 		}
 	}
 	
 	/** Our private list of TableModelListeners. */
 	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
 
 	/** Add a new TableModelListener to our list. */
 	@Override
 	public void addTableModelListener(TableModelListener l) {
 		listeners.add(l);
 	}
 
 	/** Remove a TableModelListener from our list. */
 	@Override
 	public void removeTableModelListener(TableModelListener l) {
 		listeners.remove(l);
 	}
 }
