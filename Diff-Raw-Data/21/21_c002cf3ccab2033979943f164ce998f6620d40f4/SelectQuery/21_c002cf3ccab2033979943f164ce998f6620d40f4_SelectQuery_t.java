 package hemera.utility.sql.query.result;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import hemera.utility.sql.data.DecryptColumn;
 import hemera.utility.sql.data.TableColumn;
 import hemera.utility.sql.interfaces.IResultsQuery;
 
 /**
  * <code>SelectQuery</code> defines a implementation
  * of a SQL query that selects values from specified
  * columns of a specified table based on a given set
  * of conditions. This implementation depends on the
  * <code>ESQLConfig</code> for database name.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public class SelectQuery extends AbstractSelectQuery implements IResultsQuery {
 	/**
 	 * The <code>List</code> of <code>TableColumn</code>
 	 * this select query is retrieving as results.
 	 * <p>
 	 * Must use a list to guarantee ordering so the
 	 * query template is the same for different values
 	 * allowing the statement to be reused.
 	 */
 	private final List<TableColumn> resultcolumns;
 
 	/**
 	 * Constructor of <code>SelectQuery</code>.
 	 * @param key The <code>String</code> key used to
 	 * identify the data source.
 	 */
 	public SelectQuery(final String key) {
 		super(key);
 		this.resultcolumns = new ArrayList<TableColumn>();
 	}
 
 	/**
 	 * Add the name of the column to retrieve result
 	 * from.
 	 * @param table The <code>String</code> name of
 	 * the table the column belongs.
 	 * @param column The <code>String</code> name of
 	 * the result column.
 	 */
 	public void addResultColumn(final String table, final String column) {
 		final TableColumn tablecolumn = new TableColumn(table, column);
 		if (!this.resultcolumns.contains(tablecolumn)) {
 			this.resultcolumns.add(tablecolumn);
 		}
 		if (!this.tables.contains(table)) {
 			this.tables.add(table);
 		}
 	}
 	
 	/**
 	 * Add the name of the column to retrieve result
 	 * and decrypt from.
 	 * @param table The <code>String</code> name of
 	 * the table the column belongs.
 	 * @param column The <code>String</code> name of
 	 * the result column.
 	 * @param key The <code>String</code> key used to
 	 * descrypt the data.
 	 */
 	public void addDecryptColumn(final String table, final String column, final String key) {
 		final DecryptColumn decryptcolumn = new DecryptColumn(table, column, key);
 		if (!this.resultcolumns.contains(decryptcolumn)) {
 			this.resultcolumns.add(decryptcolumn);
 		}
 		if (!this.tables.contains(table)) {
 			this.tables.add(table);
 		}
 	}
 
 	@Override
 	protected String buildResultTemplate() {
 		// Result columns.
 		final StringBuilder builder = new StringBuilder();
 		final int size = this.resultcolumns.size();
 		final int last = size - 1;
 		for (int i = 0; i < size; i++) {
 			final TableColumn col = this.resultcolumns.get(i);
 			if (col instanceof DecryptColumn) {
				builder.append("AES_DECRYPT(");
				builder.append("`").append(col.table).append("`.");
				builder.append("`").append(col.column).append("`");
				builder.append(",?)");
 			} else {
				builder.append("`").append(col.table).append("`.");
 				builder.append("`").append(col.column).append("`");
 			}
 			if (i != last) builder.append(",");
 		}
 		return builder.toString();
 	}
 
 	@Override
 	protected int insertResultValues(final PreparedStatement statement) throws SQLException {
 		final int size = this.resultcolumns.size();
 		int count = 1;
 		for (int i = 0; i < size; i++) {
 			final TableColumn column = this.resultcolumns.get(i);
 			if (column instanceof DecryptColumn) {
 				statement.setString(count++, ((DecryptColumn)column).key);
 			}
 		}
 		return count;
 	}
 }
