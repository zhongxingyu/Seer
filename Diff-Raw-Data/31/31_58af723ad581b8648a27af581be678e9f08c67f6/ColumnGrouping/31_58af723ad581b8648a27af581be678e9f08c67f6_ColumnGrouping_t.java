 package gov.usgs.cida.nude.column;
 
import com.google.common.base.Objects;
 import gov.usgs.cida.spec.jsl.mapping.ColumnMapping;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ColumnGrouping implements Iterable<Column> {
 	private static final Logger log = LoggerFactory
 			.getLogger(ColumnGrouping.class);
 	protected final Column primaryKeyColumn;
 	protected final Map<String, Integer> colToIndex;
 	protected final List<Column> columns;
 	//When adding column groupings to eachother, the primary key column must match!
 	
 	public ColumnGrouping(Column primaryKeyColumn) {
 		this(primaryKeyColumn, new ArrayList<Column>());
 	}
 	
 	/**
 	 * Takes the first column and expects it to be the primary key
 	 * @param columns
 	 */
 	public ColumnGrouping(List<Column> columns) {
 		this(columns.get(0), columns);
 	}
 	
 	/**
 	 * TODO NO DUPLICATE COLUMNS!
 	 * @param primaryKeyColumn
 	 * @param columns
 	 */
 	public ColumnGrouping(Column primaryKeyColumn, List<Column> columns) {
 		this.primaryKeyColumn = primaryKeyColumn;
 		if (null == this.primaryKeyColumn) {
 			throw new RuntimeException("Y U NO PRIMARY KEY?");
 		}
 		
 		Set<Column> colSet = new LinkedHashSet<Column>();
 		colSet.addAll(columns);
 		
 		List<Column> cols = new ArrayList<Column>();
 		cols.addAll(colSet);
 		
 		if (1 > cols.size()) {
 			cols.add(this.primaryKeyColumn);
 		}
 		
 		this.columns = Collections.unmodifiableList(cols);
 		
 		
 		Map<String, Integer> cti = new HashMap<String, Integer>();
 		for (int i = 0; i < this.columns.size(); i++) { //TODO Bimap?
 			cti.put(this.columns.get(i).getName(), new Integer(i));
 		}
 		
 		this.colToIndex = Collections.unmodifiableMap(cti);
 	}
 	
 	public Column getPrimaryKey() {
 		return this.primaryKeyColumn;
 	}
 	
 	public List<Column> getColumns() {
 		return this.columns;
 	}
 	
 	public ColumnGrouping join(ColumnGrouping columns) {
 		ColumnGrouping result = null;
 		
 		if (null != columns && this.getPrimaryKey().equals(columns.getPrimaryKey())) {
 			List<Column> cols = new ArrayList<Column>();
 			cols.addAll(this.getColumns());
 			cols.addAll(columns.getColumns());
 			
 			result = new ColumnGrouping(this.getPrimaryKey(), cols);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 1 indexed
 	 * @param index
 	 * @return
 	 */
 	public Column get(int index) {
 		return this.columns.get(index - 1);
 	}
 	
 	public int size() {
 		return this.columns.size();
 	}
 	
 	/**
 	 * 1 indexed
 	 * @param columnName
 	 * @return 
 	 */
 	public int indexOf(String columnName) {
 		int result = -1;
 		
 		Integer colIndex = this.colToIndex.get(columnName);
 		if (null != colIndex) {
 			result = colIndex + 1;
 		}
 		
 		return result;
 	}
 
 	@Override
 	public Iterator<Column> iterator() {
 		return this.columns.iterator();
 	}
 	
 	public static ColumnMapping[] getColumnMappings(ColumnGrouping colGroup) {
 		ColumnMapping[] result = null;
 		
 		List<ColumnMapping> cm = new ArrayList<ColumnMapping>();
 		for (Column col : colGroup) {
 			if (col.isDisplayable()) {
 				cm.add(new ColumnMapping(col.getName(), col.getName()));
 			}
 		}
 		
 		result = cm.toArray(new ColumnMapping[0]);
 		return result;
 	}
 	
 	public static ColumnGrouping getColumnGrouping(ResultSet rset) {
 		ColumnGrouping result = null;
 		
 		try {
 			if (null != rset) {
 				ResultSetMetaData md = rset.getMetaData();
 				if (null != md) {
 					if (md.isWrapperFor(CGResultSetMetaData.class)) {
 						CGResultSetMetaData cgmd = md.unwrap(CGResultSetMetaData.class);
 						result = cgmd.getColumnGrouping();
 					} else {
 						List<Column> cols = new ArrayList<Column>();
 						int numCols = md.getColumnCount();
 						
 						for (int i = 1; i <= numCols; i++) {
 							String colName = md.getColumnName(i);
 							String tabName = md.getTableName(i);
 							String schName = md.getSchemaName(i);
 							Class<?> valType = Class.forName(md.getColumnClassName(i));
 							
 							Column col = new SimpleColumn(colName, tabName, schName, valType, true);
 							
 							cols.add(col);
 						}
 						
 						result = new ColumnGrouping(cols);
 					}
 				} else {
 					log.trace("non-null ResultSet evaluated null ResultSetMetaData");
 				}
 			} else {
 				log.trace("null ResultSet passed to getColumnGrouping");
 			}
 		} catch (SQLException e) {
 			log.error("Exception caught when getting ColumnGrouping from ResultSet", e);
 		} catch (ClassNotFoundException e) {
 			log.error("Exception caught when deciphering valueType of Column", e);
 		}
 		
 		return result;
 	}
 	
 	public static ColumnGrouping join(Iterable<ColumnGrouping> colGroups) {
 		ColumnGrouping result = null;
 		
 		for (ColumnGrouping cg : colGroups) {
 			if (null == result) {
 				result = cg;
 			} else {
 				result = result.join(cg);
 			}
 		}
 		
 		return result;
 	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("columns", columns)
				.toString();
	}
 }
