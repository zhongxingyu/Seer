 
 
 
 package overwatch.db;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import overwatch.gui.NameRefPair;
 import overwatch.gui.NameRefPairList;
 
 
 
 
 
 /**
  * Provides a nicer interface for reading SQL results.
  * It's not tied to a DB connection, is iterable, has random access, and
  * allows you to access rows and columns as arrays.
  * 
  * Uses zero-based indeces.
  * 
  * @author  Lee Coakley
  * @version 4
  */
 
 
 
 
 
 public class EnhancedResultSet implements Iterable<Object[]>
 {
 	private ArrayList<Object[]>	rows;
 	private String[]			columnNames;
 	private int[]				columnTypes;
 	private int					columnCount;
 	
 	
 	
 	
 	
 	public EnhancedResultSet( ResultSet set ) throws SQLException
 	{
 		populateColumnInfo( set );
 		populateRowData   ( set );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Access an element in the set.
 	 * @param row
 	 * @param col
 	 * @return Object
 	 */
 	public Object getElem( int row, int col ) {
 		return getRow( row )[ col ];
 	}
 	
 	
 	
 	public Object getElem( int col ) {
 		return getRow( 0 )[ col ];
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Same as before, except you access the column by name.
 	 * @param row
 	 * @param col
 	 * @return Object
 	 */
 	public Object getElem( int row, String col ) {
 		return getElem( row, getColumnIndex(col) );
 	}
 	
 	
 	
 	public Object getElem( String col ) {
 		return getElem( 0, getColumnIndex(col) );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * As as before except you can specify the return type.
 	 * @param row
 	 * @param col
 	 * @param type (for example: String.class)
 	 * @return typed element
 	 */
 	public <T> T getElemAs( int row, int col, Class<T> type ) {
 		return (T) getRow( row )[ col ];
 	}
 	
 	
 	
 	public <T> T getElemAs( int col, Class<T> type ) {
 		return (T) getRow( 0 )[ col ];
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Same as before, except you access the column by name.
 	 * @param row
 	 * @param col
 	 * @param type (for example: String.class)
 	 * @return typed element
 	 */
 	public <T> T getElemAs( int row, String col, Class<T> type ) {
 		return (T) getElem( row, col );
 	}
 	
 	
 	
 	public <T> T getElemAs( String col, Class<T> type ) {
 		return (T) getElem( 0, col );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get total number of rows.
 	 */
 	public int size() {
 		return getRowCount();
 	}
 	
 	
 	
 	
 	/**
 	 * Check if empty.
 	 * @return boolean
 	 */
 	public boolean isEmpty() {
 		return size() == 0;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Access specific row.
 	 * @param row
 	 */
 	public Object[] getRow( int row ) {
 		return rows.get( row );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get total number of rows.
 	 */
 	public int getRowCount() {
 		return rows.size();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get total number of columns.
 	 */
 	public int getColumnCount() {
 		return columnCount;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get names of each column.
 	 */
 	public String[] getColumnNames() {
 		return columnNames;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get types of each column.
 	 * These can be compared with java.sql.Types.
 	 */
 	public int[] getColumnTypes() {
 		return columnTypes;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Access an entire column as an array.
 	 */
 	public Object[] getColumn( int column )
 	{
 		Object[] columnArray = new Object[ getRowCount() ];
 		
 		for (int row=0; row<columnArray.length; row++) {
 			columnArray[row] = getElem( row, column );
 		}
 		
 		return columnArray;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Access an entire column as an array.
 	 */
 	public Object[] getColumn( String name ) {
 		int index = getColumnIndex( name );
 		return getColumn( index );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Copy column and cast it to a specific type.
 	 * Example:
 	 *     Integer[] array = ers.getColumnAs( 0, Integer[].class );
 	 */
 	public <T> T[] getColumnAs( int column, Class<? extends T[]> type ) {
 		return Arrays.copyOf( getColumn(column), size(), type );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Copy column and cast it to a specific type.
 	 */
 	public <T> T[] getColumnAs( String name, Class<? extends T[]> type ) {
 		return getColumnAs( getColumnIndex(name), type );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Given the column name, find the index.
 	 * If you used an "AS" command, it will be named as such.
 	 * @throws RuntimeException If no column with the given name exists.  (use columnExists for that)
 	 */
 	public int getColumnIndex( String name )
 	{
 		int index = getColumnIndexInternal( name );
 		
 		if (index != -1) {
 			return index;
 		} else {
 			throw new RuntimeException(
 				"No column named '" + name + "' exists.  Columns are: " +
 				Arrays.toString( getColumnNames() )
 			);
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if a column exists.
 	 * @param name
 	 * @return boolean
 	 */
 	public boolean columnExists( String name ) {
 		return (-1 != getColumnIndexInternal(name));
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Create an ArrayList<NameRefPair<T>> from two columns, one which is cast to refType, one which is a string type.
 	 * @param refColumn
 	 * @param refType
 	 * @param nameColumn
 	 * @return ArrayList<NameRefPair<T>>
 	 */
 	public <T> ArrayList<NameRefPair<T>> getNameRefPairArrayList( String refColumn, Class<? extends T[]> refType, String nameColumn )
 	{
 		T[]      keys  = getColumnAs( refColumn,  refType        );
 		String[] names = getColumnAs( nameColumn, String[].class );
 		
 		return new NameRefPairList<T>( keys, names );
 	}
 	
 	
 	
 	
 	
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append( Arrays.toString( getColumnNames() ) );
 		
 		for (Object[] row: this) {
 			sb.append( "\n" + Arrays.toString( row ) );
 		}
 		
 		return sb.toString();
 	}
 		
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////	
 	
 	
 	private int getColumnIndexInternal( String name )
 	{
 		for (int col=0; col<columnNames.length; col++) 
 			if (columnNames[col].equals( name )) 
 				return col;
 		
 		return -1;
 	}
 	
 	
 	
 	
 	
 	private void populateColumnInfo( ResultSet set ) throws SQLException
 	{
 		ResultSetMetaData meta = set.getMetaData();
 		
 		columnCount = meta.getColumnCount();
 		columnNames = new String[ columnCount ];
 		columnTypes = new int   [ columnCount ];
 		
 		for (int i=0; i<columnCount; i++) {
 			columnNames[i] = meta.getColumnLabel( sqlIndex(i) );
 			columnTypes[i] = meta.getColumnType ( sqlIndex(i) );
 		}
 	}
 	
 	
 	
 	
 	
	private synchronized void populateRowData( ResultSet set ) throws SQLException
 	{
 		rows = new ArrayList< Object[] >();
 		
 		while (set.next()) {
 			Object[] row = new Object[ columnCount ];
 			
 			for (int i=0; i<columnCount; i++) 
 				row[i] = set.getObject( sqlIndex(i) );
 			
 			rows.add( row );
 		}
 	}
 	
 	
 	
 	
 	
 	private int sqlIndex( int zeroIndex ) {
 		return ++zeroIndex;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Defines iterator so you can do stuff like:
 	 * 		for (Object[] row: set)
 	 * 			whatever( row );
 	 */
 	public Iterator<Object[]> iterator()
 	{
 		return new Iterator<Object[]>()
 		{
 			private int index = -1;
 			
 			public boolean  hasNext() {	 return (index + 1) < size();		  		 }
 		    public Object[] next()    {  return getRow( ++index );  				 }
 			public void     remove()  {  throw new UnsupportedOperationException();  }
 		};
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
