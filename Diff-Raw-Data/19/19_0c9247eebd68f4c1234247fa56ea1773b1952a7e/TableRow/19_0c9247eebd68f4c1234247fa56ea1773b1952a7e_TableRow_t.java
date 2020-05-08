 package gov.usgs.cida.nude.resultset.inmemory;
 
 import com.google.common.base.Objects;
 import gov.usgs.cida.nude.column.Column;
 import gov.usgs.cida.nude.column.ColumnGrouping;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class TableRow implements Comparable<TableRow>{
	private static final Logger log = LoggerFactory.getLogger(TableRow.class);
 	protected final Map<Column, String> row;
 	protected final ColumnGrouping columns;
 	
 	public TableRow(Column primaryKey, String value) {
 		this.columns = new ColumnGrouping(primaryKey);
 		this.row = new HashMap<Column, String>();
 		this.row.put(primaryKey, value);
 	}
 	
 	public TableRow(ColumnGrouping colGroup) {
 		this(colGroup, null);
 	}
 	
 	public TableRow(ColumnGrouping colGroup, Map<Column, String> row) {
 		if (null == colGroup) {
 			throw new RuntimeException("ColumnGroup cannot be null");
 		}
 		Map<Column, String> modRow = new HashMap<Column, String>();
 		
 		if (null != row) {
 			modRow.putAll(row);
 		}
 		
 		this.row = Collections.unmodifiableMap(modRow);
 		this.columns = colGroup;
 	}
 	
 	public String getValue(Column column) {
 		return this.row.get(column);
 	}
 	
 	public ColumnGrouping getColumns() {
 		return this.columns;
 	}
 	
 	public Set<Entry<Column, String>> getEntries() {
 		return this.row.entrySet();
 	}
 	
 	public Map<Column, String> getMap() {
 		return this.row;
 	}
 
 	/**
	 * Compares the values of the primary keys. (Values are compared as Longs)
 	 */
 	@Override
 	public int compareTo(TableRow o) {
		int result = -1;
		
		try {
			result = new Long(this.getValue(this.getColumns().getPrimaryKey())).compareTo(new Long(o.getValue(this.getColumns().getPrimaryKey())));
		} catch (NumberFormatException e) {
			log.info("Could not compare primary keys as Longs: " + e.getMessage());
			result = this.getValue(this.getColumns().getPrimaryKey()).compareTo(o.getValue(this.getColumns().getPrimaryKey()));
		}
		
		return result;
 	}
 	
 	public static TableRow buildTableRow(ResultSet rs) throws SQLException {
 		TableRow result = null;
 		
 		if (null != rs) {
 			ColumnGrouping cg = ColumnGrouping.getColumnGrouping(rs);
 			Map<Column, String> row = new HashMap<Column, String>();
 			for (Column col : cg) {
 				String strVal = rs.getString(col.getName());
 				row.put(col, strVal);
 			}
 
 			result = new TableRow(cg, row);
 		}
 		
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return Objects.toStringHelper(this)
 				.add("columns", this.columns)
 				.add("row", this.row)
 				.toString();
 	}
 }
