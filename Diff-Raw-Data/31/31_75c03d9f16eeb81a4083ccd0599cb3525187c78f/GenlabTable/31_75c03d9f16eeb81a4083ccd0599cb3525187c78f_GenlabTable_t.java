 package genlab.core.model.meta.basics.flowtypes;
 
 import genlab.core.commons.ProgramException;
 import genlab.core.commons.WrongParametersException;
 import genlab.core.model.meta.IDumpableToText;
 import genlab.core.usermachineinteraction.GLLogger;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Optimized for a given number of columns .
  * 
  * TODO synchronicity !!!???
  * 
  * @author Samuel Thiriot
  *
  */
 public class GenlabTable implements IGenlabTable, IDumpableToText {
 
 	private ArrayList<Object[]> content = new ArrayList<Object[]>();
 	
 	private List<String> columnsTitles = new LinkedList<String>();
 	private Map<String,Integer> columnId2idx = new HashMap<String, Integer>(50);
 
 	private Map<String,Object> tableMetadata = null;
 	
 	private Map<String,Map<String,Object>> columns2Metadata = new HashMap<String, Map<String,Object>>();
 	
 	public GenlabTable() {
 	}
 	
 	public void setTableMetaData(String key, Object value) {
 		
 		if (tableMetadata == null) {
 			tableMetadata = new HashMap<String, Object>();
 		}
 		
 		tableMetadata.put(key, value);
 	}
 	
 	public Map<String,Object> getTableMetaData() {
 		if (tableMetadata == null)
 			return Collections.EMPTY_MAP;
 		else
 			return Collections.unmodifiableMap(tableMetadata);
 	}
 	
 	public Object getTableMetaData(String key) {
 		if (tableMetadata == null)
 			return null;
 		else
 			return tableMetadata.get(key);
 	}
 	
 
 	public boolean containsTableMetaData(String key) {
 		if (tableMetadata == null)
 			return false;
 		else
 			return tableMetadata.containsKey(key);
 	}
 	
 	public void setColumnMetaData(String column, String key, Object value) {
 		
 		Map<String,Object> columnMetadata = columns2Metadata.get(column);
 		
 		if (columnMetadata == null) {
 			if (!columnId2idx.containsKey(column))
 				throw new WrongParametersException("this column does not exists in the table: "+column);
 			
 			columnMetadata = new HashMap<String, Object>();
 			columns2Metadata.put(column, columnMetadata);
 		}
 		
 		columnMetadata.put(key, value);
 	}
 	
 	public Map<String,Object> getColumnMetaData(String column) {
 		
 		Map<String,Object> columnMetadata = columns2Metadata.get(column);
 		
 		if (columnMetadata == null)
 			return Collections.EMPTY_MAP;
 		else
 			return Collections.unmodifiableMap(columnMetadata);
 	}
 	
 	public Object getColumnMetaData(String column, String key) {
 		
 		Map<String,Object> columnMetadata = columns2Metadata.get(column);
 		
 		if (columnMetadata == null)
 			return null;
 		else
 			return tableMetadata.get(key);
 		
 	}
 	
 	public boolean containsColumnMetaData(String column, String key) {
 		
 		Map<String,Object> columnMetadata = columns2Metadata.get(column);
 		
 		if (columnMetadata == null) {
 			if (!columnId2idx.containsKey(column))
 				throw new WrongParametersException("this column does not exists in the table: "+column);
 			return false;
 		} else {
 			return columnMetadata.containsKey(key);
 		}
 		
 	}
 	
 	@Override
 	public boolean isEmpty() {
 		return content.isEmpty();
 	}
 
 	@Override
 	public int getColumnsCount() {
 		return columnId2idx.size();
 	}
 	
 	protected int createNewIdForColumn(String id) {
 		Integer idx = columnId2idx.size();
 		columnId2idx.put(id, idx);
 		columnsTitles.add(id);
 		return idx;
 	}
 
 	@Override
 	public int declareColumn(String id) {
 		
 		if (columnId2idx.containsKey(id))
 			throw new WrongParametersException("this column already exists: "+id);
 		
 		GLLogger.debugTech("declaring new column "+id, getClass());
 		
 		// create the column id...
 		int intId = createNewIdForColumn(id);
 		
 		// and resize all data
 		int newSize = columnId2idx.size();
 		for (int i = 0; i<content.size(); i++) {
 			content.set(i, Arrays.copyOf(content.get(i), newSize));
 		}
 		
 		return intId;
 		
 	}
 	
 	@Override
 	public Collection<Integer> declareColumns(Collection<String> ids) {
 		
 		Collection<Integer> intIds = new LinkedList<Integer>();
 		
 		// check parameters
 		for (String id : ids) {
 			if (columnId2idx.containsKey(id))
 				throw new WrongParametersException("this column already exists: "+id);
 		}
 		
 		// create ids 
 		for (String id : ids) {
 			intIds.add(createNewIdForColumn(id));
 		}
 		
 		// and resize all data
 		int newSize = columnId2idx.size();
 		for (int i = 0; i<content.size(); i++) {
 			content.set(i, Arrays.copyOf(content.get(i), newSize));
 		}
 
 		return intIds;
 	}
 
 	@Override
 	public boolean containsColumn(String id) {
 		return columnId2idx.containsKey(id);
 	}
 
 	@Override
 	public List<String> getColumnsId() {
 		return Collections.unmodifiableList(columnsTitles);
 	}
 
 	@Override
 	public String getColumnIdForIdx(int colIdx) {
 		return columnsTitles.get(colIdx);
 	}
 	
 
 	@Override
 	public Integer getIndexForColumnId(String id) {
 		for (int i=0; i<columnsTitles.size(); i++) {
 			if (columnsTitles.get(i)==id)
 				return i;
 		}
 		return null;
 	}
 	
 	@Override
 	public int addRow() {
 
 		Object[] emptyRow = new Object[columnId2idx.size()];
 		
 		content.add(emptyRow);
 		
 		return content.size()-1;
 	}
 
 	@Override
 	public int getRowsCount() {
 		return content.size();
 	}
 
 	@Override
 	public void setValue(int rowId, String columnId, Object value) {
 		
 		// TODO check index !
 		Object[] row = content.get(rowId);
 		if (row == null)
 			throw new ProgramException("row was not declared: "+rowId);
 		Integer colId = columnId2idx.get(columnId);
 		if (colId == null)
 			throw new ProgramException("column was not declared: "+columnId);
 		
 		row[colId] = value;
 		
 	}
 
 	@Override
 	public void setValue(int rowId, int columnIdx, Object value) {
 		content.get(rowId)[columnIdx] = value;
 	}
 
 
 	@Override
 	public void setValues(int rowId, Object[] values) {
 		// check the number of values is OK
 		if (values.length != columnId2idx.size())
 			throw new WrongParametersException("wrong count of columns: expected "+columnId2idx.size()+", but found "+values.length);
 		
 		// TODO index !:
 		content.set(rowId, values);
 	}
 
 	@Override
 	public int addRow(Object[] values) {
 
 		// check the number of values is OK
 		if (values.length != columnId2idx.size())
 			throw new WrongParametersException("wrong count of columns: expected "+columnId2idx.size()+", but found "+values.length);
 		
 		content.add(values);
 		
 		return content.size()-1;
 		
 	}
 
 
 	@Override
 	public String toString() {
 	
 		StringBuffer sb = new StringBuffer();
 		sb
 		 .append("a table with ")
 		 .append(columnId2idx.size())
 		 .append(" columns and ")
 		 .append(content.size())
 		 .append(" rows")
 		 .append("\n");
 		
 		return sb.toString();
 		
 	}
 
 	@Override
 	public Object[] getRow(int i) {
 		// TODO manage error
 		return content.get(i);
 	}
 
 	@Override
 	public void fillColumn(int colIndex, Object value) {
 		
 		for (Object[] values : content) {
 			values[colIndex] = value;
 		}
 		
 	}
 
 	@Override
	public final Object getValue(int rowId, String columnId) {
		final int idx = columnId2idx.get(columnId);
		if (idx >= 0)
			return content.get(rowId)[idx];
		else
			return null;
 	}
 	
 	@Override
	public final Object getValue(int rowId, int columnIdx) {
 		return content.get(rowId)[columnIdx];
 	}
 
 	@Override
	public final Object[] getValues(int rowId) {
 		return content.get(rowId);
 	}
 
 	@Override
 	public boolean isColumnEmpty(String columnId) {
 		
 		// TODO check parametrs
 		int colIdx = columnId2idx.get(columnId);
 		
 		for (Object[] values : content) {
 			if (values[colIdx] == null)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isColumnEmpty(int colIdx) {
 		
 		for (Object[] values : content) {
 			if (values[colIdx] == null)
 				return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public Collection<String> getEmptyColumnsIds() {
 
 		Collection<String> emptyColumnsIds = new LinkedList<String>();
 		
 		for (Map.Entry<String,Integer> id2idx : columnId2idx.entrySet()) {
 			if (isColumnEmpty(id2idx.getValue()))
 				emptyColumnsIds.add(id2idx.getKey());
 		}
 		
 		return emptyColumnsIds;
 	}
 
 	@Override
 	public Collection<Integer> getEmptyColumnsIndexes() {
 		
 		Collection<Integer> emptyColumnsIdxs = new LinkedList<Integer>();
 		
 		for (Integer idx : columnId2idx.values()) {
 			if (isColumnEmpty(idx))
 				emptyColumnsIdxs.add(idx);
 		}
 		
 		return emptyColumnsIdxs;
 	}
 
 	@Override
 	public void dumpAsText(PrintStream ps) {
 		
 		// add titles
 		ps.print("# columns: ");
 		for (String id : getColumnsId()) {
 			ps.print(id);
 			ps.print(";\t");
 		}
 		ps.println();
 		
 		Object[] row = null;
 		for (int i=0; i<getRowsCount(); i++) {
 
 			row = getRow(i);
 			
 			for (int j=0; j<row.length; j++) {
 				if (j>0)
 					ps.print(";\t");
 				ps.print(row[j]);	
 			}
 			ps.println();
 			
 		}
 		
 	}
 
 }
