 package com.crispy;
 
 import java.lang.reflect.Constructor;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import sun.org.mozilla.javascript.internal.ObjArray;
 
 public class Table {
 	public enum WhereOp {
 		EQUALS("="), NOT_EQUALS("!="), GREATER_THAN(">"), LESS_THAN("<"), LIKE(
 				" LIKE ");
 
 		String sqlOp;
 
 		private WhereOp(String op) {
 			sqlOp = op;
 		}
 
 		public String sqlOp() {
 			return sqlOp;
 		}
 	}
 
 	static class WhereExp {
 		String exp;
 		Object values[];
 
 		static WhereExp operator(String table, WhereOp op, String column, Object value) {
 			WhereExp where = new WhereExp();
 			where.exp = table + ".`" + column + "`" + op.sqlOp() + "?";
 			where.values = new Object[1];
 			where.values[0] = value;
 			return where;
 		}
 		
		static WhereExp in(String table, String column, Object ... value) {
 			WhereExp where = new WhereExp();
 			where.exp = table + ".`" + column + "` IN (" + StringUtils.join(Collections.nCopies(value.length, "?"), ",") + ")";
 			where.values = new Object[value.length];
 			for (int i = 0; i < value.length; i++) {
				where.values[i] = value;
 			}
 			return where;
 		}
 	}
 
 	private final Logger LOG;
 
 	private ArrayList<Table> joins;
 
 	private boolean random;
 	private boolean unique;
 	private String name;
 	private ArrayList<WhereExp> where;
 	// private HashMap<String, Pair<WhereOp, Object>> where;
 	private ArrayList<String> columnNames;
 	private ArrayList<String> overwriteColumns;
 	private ArrayList<Column> newColumns;
 	private ArrayList<Index> newIndexes;
 	private ArrayList<Constraint> newConstraints;
 
 	private Index newPrimaryKey;
 
 	private ArrayList<Object> values;
 	private boolean deleteOldColumns;
 
 	private int limit;
 
 	private String orderBy;
 
 	private JSONObject comment;
 
 	private String average;
 	
 	private boolean ignore;
 
 	private int start;
 
 	private boolean ignoreNull;
 
 	private String groupBy;
 
 	public static Table get(String name) {
 		Table t = new Table(name);
 		return t;
 	}
 
 	public Table overwrite(String... column) {
 		this.overwriteColumns = new ArrayList<String>();
 		this.overwriteColumns.addAll(Arrays.asList(column));
 		return this;
 	}
 
 	private Table(String name) {
 		LOG = Log.getInstance().getLogger("db");
 		deleteOldColumns = false;
 
 		random = false;
 		unique = false;		
 		ignore = false;
 		limit = -1;
 		start = -1;
 		this.name = name;
 		joins = new ArrayList<Table>();
 		where = new ArrayList<Table.WhereExp>();
 		joins.add(this);
 		comment = new JSONObject();
 	}
 
 	public Table deleteOldColumns() {
 		deleteOldColumns = true;
 		return this;
 	}
 
 	public Table columns(String... name) {
 		columnNames = new ArrayList<String>();
 		columnNames.addAll(Arrays.asList(name));
 		return this;
 	}
 
 	public Table columns(Column... c) {
 		newColumns = new ArrayList<Column>();
 		newColumns.addAll(Arrays.asList(c));
 		for (Column cc : newColumns) {
 			if (cc.autoIncrement) {
 				newPrimaryKey = new Index(null, cc.name);
 				newPrimaryKey.isAuto = true;
 			}
 		}
 		return this;
 	}
 
 	public Table indexes(Index... i) {
 		newIndexes = new ArrayList<Index>();
 		newIndexes.addAll(Arrays.asList(i));
 		return this;
 	}
 
 	public Table constraints(Constraint... c) {
 		newConstraints = new ArrayList<Constraint>();
 		newConstraints.addAll(Arrays.asList(c));
 		for (Constraint nc : newConstraints)
 			nc.sourceTable = name;
 		return this;
 	}
 
 	public Table primary(String... name) {
 		if (newPrimaryKey != null)
 			throw new IllegalStateException("Already got one primary key");
 		newPrimaryKey = new Index(null, name);
 		return this;
 	}
 
 	public void create() {
 		try {
 			LOG.debug("create " + name);
 			DB.loadMetadata(name);
 			// If table does not exist.
 			Metadata m = DB.getMetadata(name);
 			if (m == null) {
 				List<String> defs = new ArrayList<String>();
 				for (Column column : newColumns) {
 					defs.addAll(Arrays.asList(column.createDefinitions()));
 				}
 				DB.updateQuery("CREATE TABLE `" + name + "` ("
 						+ StringUtils.join(defs, ',') + ")");
 				if (!name.equals("_metadata")) {
 					Table.get("_metadata").columns("table", "metadata")
 							.values(name, comment.toString())
 							.overwrite("metadata").add();
 				}
 			} else {
 				for (Column column : newColumns) {
 					Column oldColumn = Column
 							.findByName(m.columns, column.name);
 					if (oldColumn == null) {
 						DB.updateQuery("ALTER TABLE `" + name + "` ADD COLUMN "
 								+ column.createDefinitions());
 					} else {
 						if (!oldColumn.equals(column)) {
 							DB.updateQuery("ALTER TABLE `" + name
 									+ "` MODIFY COLUMN "
 									+ column.createDefinitions());
 						}
 					}
 				}
 
 				if (deleteOldColumns) {
 					for (Column oldColumn : m.columns) {
 						Column newColumn = Column.findByName(newColumns,
 								oldColumn.name);
 						if (newColumn == null) {
 							DB.updateQuery("ALTER TABLE `" + name
 									+ "` DROP COLUMN `" + oldColumn.name + "`");
 						}
 					}
 				}
 
 				if (!m.comment.toString().equals(comment.toString())) {
 					Table.get("_metadata").columns("table", "metadata")
 							.values(name, comment.toString())
 							.overwrite("metadata").add();
 					DB.updateQuery("ALTER TABLE `" + name + "` COMMENT=?",
 							comment.toString());
 				}
 			}
 
 			if (newIndexes == null)
 				newIndexes = new ArrayList<Index>();
 
 			for (Index i : newIndexes) {
 				Index oldIndex = (m == null) ? null : m.getIndex(i.name);
 				if (oldIndex == null) {
 					DB.updateQuery("ALTER TABLE `" + name + "` ADD "
 							+ i.createDefinition());
 				} else if (!oldIndex.equals(i)) {
 					DB.updateQuery("ALTER TABLE `" + name + "` DROP INDEX `"
 							+ oldIndex.name + "`");
 					DB.updateQuery("ALTER TABLE `" + name + "` ADD "
 							+ i.createDefinition());
 					LOG.info("CREATING NEW INDEX " + i.createDefinition());
 				}
 			}
 
 			if (m != null) {
 				for (Index i : m.indexes) {
 					Index newIndex = Index.findByName(newIndexes, i.name);
 					if (newIndex == null) {
 						DB.updateQuery("ALTER TABLE `" + name
 								+ "` DROP INDEX `" + i.name + "`");
 					}
 				}
 			}
 
 			if (newConstraints == null)
 				newConstraints = new ArrayList<Constraint>();
 
 			for (Constraint c : newConstraints) {
 				Constraint old = (m == null) ? null : m
 						.getConstraint(c.sourceColumn);
 				if (old == null) {
 					c.create(name);
 				} else if (!old.equals(c)) {
 					old.drop();
 					c.create(name);
 				}
 			}
 
 			if (m != null) {
 				for (Constraint c : m.constraints) {
 					boolean found = false;
 					for (Constraint newC : newConstraints) {
 						if (newC.sourceColumn.equals(c.sourceColumn)) {
 							found = true;
 						}
 					}
 					if (!found) {
 						c.drop();
 					}
 				}
 			}
 
 			if (newPrimaryKey != null) {
 				Index oldPrimary = (m == null) ? null : m.primary;
 				if (oldPrimary == null) {
 					if (!newPrimaryKey.isAuto)
 						DB.updateQuery("ALTER TABLE `" + name
 								+ "` ADD PRIMARY KEY "
 								+ newPrimaryKey.createDefinition());
 				} else if (!oldPrimary.equals(newPrimaryKey)) {
 					DB.updateQuery("ALTER TABLE `" + name
 							+ "` DROP PRIMARY KEY");
 					if (!newPrimaryKey.isAuto)
 						DB.updateQuery("ALTER TABLE `" + name
 								+ "` ADD PRIMARY KEY "
 								+ newPrimaryKey.createDefinition());
 				}
 			} else {
 				if (m != null) {
 					if (m.primary != null) {
 						DB.updateQuery("ALTER TABLE `" + name
 								+ "` DROP PRIMARY KEY");
 					}
 				}
 			}
 
 			// Reload and reorder metadata
 			DB.loadMetadata(name).reorderAndRetain(newColumns);
 		} catch (Throwable t) {
 			LOG.error(t.getMessage(), t);
 			throw new IllegalStateException(t);
 		}
 
 	}
 
 	public Table values(Object... values) {
 		if (this.values == null) {
 			this.values = new ArrayList<Object>();
 		}
 
 		Metadata m = DB.getMetadata(name);
 
 		for (int i = 0; i < values.length; i++) {
 			String columnName = columnNames.get(i);
 			Column c = Column.findByName(m.columns, columnName);
 			this.values.add(c.parseObject(values[i]));
 		}
 		return this;
 	}
 
 	private static String safeJoin(List<String> e) {
 		List<String> temp = new ArrayList<String>();
 		for (String s : e) {
 			temp.add("`" + s + "`");
 		}
 		return StringUtils.join(temp, ',');
 	}
 
 	public Table ignore() {
 		ignore = true;
 		return this;
 	}
 
 	public Table ignoreNull() {
 		ignoreNull = true;
 		return this;
 	}
 	
 	public Table random() {
 		this.random = true;
 		return this;
 	}
 
 	public void update() throws SQLException {
 		Connection con = DB.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("UPDATE");
 			if (ignore) {
 				sb.append(" IGNORE");
 			}
 			sb.append(" `" + name + "` SET ");
 			ArrayList<String> updates = new ArrayList<String>();
 			for (int c = 0; c < columnNames.size(); c++) {
 				if (!ignoreNull || values.get(c) != null)
 					updates.add("`" + columnNames.get(c) + "`=?");
 			}
 			sb.append(StringUtils.join(updates, ','));
 
 			whereStatement(sb);
 			LOG.info("update=" + sb.toString());
 			PreparedStatement pstmt = con.prepareStatement(sb.toString());
 			int c = 1;
 
 			for (int v = 0; v < values.size(); v++) {
 				if (!ignoreNull || values.get(v) != null)
 					pstmt.setObject(c++, values.get(v));
 			}
 
 			whereValues(pstmt, c);
 
 			pstmt.executeUpdate();
 			pstmt.close();
 		} finally {
 			con.close();
 		}
 	}
 
 	private Object valueForColumn(String column) {
 		int index = columnNames.indexOf(column);
 		return values.get(index);
 	}
 
 	public void add() throws Exception {
 		Connection con = DB.getConnection();
 		try {
 			StringBuilder sb = new StringBuilder();
 			sb.append("INSERT "
 					+ (((overwriteColumns != null) || ignore) ? "IGNORE " : "")
 					+ "INTO `" + name + "`(");
 			sb.append(safeJoin(columnNames));
 			sb.append(") VALUES ("
 					+ StringUtils.join(
 							Collections.nCopies(columnNames.size(), "?"), ',')
 					+ ")");
 
 			if (overwriteColumns != null && overwriteColumns.size() > 0) {
 				sb.append(" ON DUPLICATE KEY UPDATE ");
 				ArrayList<String> updates = new ArrayList<String>();
 				for (String column : overwriteColumns) {
 					if (!ignoreNull || valueForColumn(column) != null)
 						updates.add("`" + column + "`=?");
 				}
 				sb.append(StringUtils.join(updates, ','));
 			}
 
 			PreparedStatement pstmt = con.prepareStatement(sb.toString());
 			int c = 1;
 			for (Object value : values) {
 				pstmt.setObject(c++, value);
 			}
 			if (overwriteColumns != null && overwriteColumns.size() > 0) {
 				for (String column : overwriteColumns) {
 					if (!ignoreNull || valueForColumn(column) != null) {
 						pstmt.setObject(c++, valueForColumn(column));
 					}
 				}
 			}
 
 			pstmt.executeUpdate();
 			pstmt.close();
 		} finally {
 			con.close();
 		}
 	}
 
 	private PreparedStatement createJoinSelectstatement(Connection con,
 			boolean count) throws SQLException {
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT ");
 
 		if (!count) {
 			ArrayList<String> names = new ArrayList<String>();
 			for (Table t : joins) {
 				if (t.columnNames == null) {
 					Metadata m = DB.getMetadata(t.name);
 					for (Column column : m.columns) {
 						names.add("`" + t.name + "`.`" + column.name + "`");
 					}
 				} else {
 					for (String columnName : t.columnNames) {
 						names.add("`" + t.name + "`.`" + columnName + "`");
 					}
 				}
 			}
 			sb.append(StringUtils.join(names, ","));
 		} else {
 			sb.append("COUNT(*)");
 		}
 
 		sb.append(" FROM ");
 		{
 			ArrayList<String> tables = new ArrayList<String>();
 			for (Table t : joins) {
 				tables.add("`" + t.name + "`");
 			}
 			sb.append(StringUtils.join(tables, " JOIN "));
 		}
 		sb.append(" ON ");
 		{
 			ArrayList<String> joinCondition = new ArrayList<String>();
 			for (int t = 0; t < joins.size() - 1; t++) {
 				Table t1 = joins.get(t);
 				Table t2 = joins.get(t + 1);
 				Metadata m1 = DB.getMetadata(t1.name);
 				Metadata m2 = DB.getMetadata(t2.name);
 
 				Constraint c = Constraint.to(m1.constraints, t2.name);
 				if (c == null)
 					c = Constraint.to(m2.constraints, t1.name);
 
 				joinCondition.add("`" + c.sourceTable + "`.`" + c.sourceColumn
 						+ "`=`" + c.destTable + "`.`" + c.destColumn + "`");
 			}
 			sb.append(StringUtils.join(joinCondition, ","));
 		}
 
 		ArrayList<String> items = new ArrayList<String>();
 		for (Table t : joins) {
 			if (t.where.size() > 0) {
 				for (WhereExp exp : t.where) {
 					items.add(exp.exp);
 				}
 			}
 		}
 		if (items.size() > 0) {
 			sb.append(" WHERE " + StringUtils.join(items, " AND "));
 		}
 
 		for (Table t : joins) {
 			if (t.groupBy != null) {
 				sb.append(" GROUP BY `" + t.name + "`." + t.groupBy);
 				break;
 			}
 		}
 		
 		for (Table t : joins) {
 			if (t.orderBy != null) {
 				sb.append(" ORDER BY `" + t.name + "`." + t.orderBy);
 				break;
 			}
 		}
 
 		if (limit != -1) {
 			if (start != -1) {
 				sb.append(" LIMIT " + start + "," + limit);
 			} else {
 				sb.append(" LIMIT " + limit);
 			}
 		}
 
 		LOG.trace(sb.toString());
 		PreparedStatement pstmt = con.prepareStatement(sb.toString());
 		int c = 1;
 		for (Table t : joins) {
 			c = t.whereValues(pstmt, c);
 			System.out.println(c + " " + t.where.size());
 		}
 		return pstmt;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	private PreparedStatement createSelectStatement(Connection con,
 			boolean count) throws SQLException {
 		if (joins.size() > 1)
 			return createJoinSelectstatement(con, count);
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT ");
 
 		if (unique)
 			sb.append("DISTINCT ");
 		
 		if (columnNames != null) {
 			ArrayList<String> names = new ArrayList<String>();
 			for (String columnName : columnNames) {
 				names.add("`" + columnName + "`");
 			}
 			sb.append(StringUtils.join(names, ','));
 		} else {
 			if (count) {
 				sb.append("COUNT(*)");
 			} else if (average != null) {
 				sb.append("AVG(" + average + ")");
 			} else {
 				sb.append("*");
 			}
 		}
 		sb.append(" FROM `" + name + "`");
 		whereStatement(sb);
 		if (groupBy != null) {
 			sb.append(" GROUP BY `" + groupBy + "`");
 		}
 		
 		if (orderBy != null) {
 			sb.append(" ORDER BY " + orderBy);
 		}
 		if (orderBy == null && random) {
 			sb.append(" ORDER BY RAND()");
 		}
 		if (limit != -1) {
 			if (start != -1) {
 				sb.append(" LIMIT " + start + "," + limit);
 			} else {
 				sb.append(" LIMIT " + limit);
 			}
 		}
 
 		LOG.info(sb.toString());
 		PreparedStatement pstmt = con.prepareStatement(sb.toString());
 		int c = 1;
 		whereValues(pstmt, c);
 		return pstmt;
 	}
 
 	public Row row() {
 		Connection con = DB.getConnection();
 		try {
 			PreparedStatement pstmt = createSelectStatement(con, false);
 			Row ret = null;
 			ResultSet results = pstmt.executeQuery();
 			if (results.next())
 				ret = new Row(results);
 
 			pstmt.close();
 
 			return ret;
 		} catch (Throwable t) {
 			LOG.error(t.getMessage(), t);
 			throw new IllegalStateException(t);
 		} finally {
 			try {
 				con.close();
 			} catch (Throwable t) {
 			}
 		}
 	}
 
 	public List<Row> rows() {
 		Connection con = DB.getConnection();
 		try {
 			PreparedStatement pstmt = createSelectStatement(con, false);
 
 			List<Row> ret = new ArrayList<Row>();
 			ResultSet results = pstmt.executeQuery();
 			while (results.next()) {
 				ret.add(new Row(results));
 			}
 
 			pstmt.close();
 			return ret;
 		} catch (Throwable t) {
 			LOG.error(t.getMessage(), t);
 			throw new IllegalStateException(t);
 		} finally {
 			try {
 				con.close();
 			} catch (Throwable t) {
 			}
 		}
 	}
 	
 	public <T> List<T> customRows(Class<T> c) {
 		Connection con = DB.getConnection();
 		try {
 			PreparedStatement pstmt = createSelectStatement(con, false);
 
 			List<T> ret = new ArrayList<T>();
 			ResultSet results = pstmt.executeQuery();
 			Constructor<T> cons = c.getConstructor(ResultSet.class);
 			while (results.next()) {
 				ret.add(cons.newInstance(results));
 			}
 			pstmt.close();
 			return ret;
 		} catch (Throwable t) {
 			LOG.error(t.getMessage(), t);
 			throw new IllegalStateException(t);
 		} finally {
 			try {
 				con.close();
 			} catch (Throwable t) {
 			}
 		}
 	}
 
	public Table where(String column, Object ... value) {
		if (value.length == 0) {
			return this;
		}
		if (value.length == 1) {
			return where(column, value[0], WhereOp.EQUALS);
		}
		
 		Metadata m = DB.getMetadata(name);
 		Column c = Column.findByName(m.columns, column);
 		if (c == null) {
 			throw new IllegalStateException("No column exists for " + column + " in table " + name);
 		}
 		Object[] parsed = new Object[value.length];
 		for (int i = 0; i < value.length; i++) {
 			parsed[i] = c.parseObject(value[i]);
 		}
 		where.add(WhereExp.in(name, column, parsed));
 		return this;
 	}
 
 	public Table where(String column, Object value, WhereOp op) {
 		Metadata m = DB.getMetadata(name);
 		Column c = Column.findByName(m.columns, column);
 		if (c == null) {
 			throw new IllegalStateException("No column exists for " + column + " in table " + name);
 		}
 		where.add(WhereExp.operator(name, op, column, c.parseObject(value)));
 		return this;
 	}
 
 	public double average(String column) throws SQLException {
 		average = column;
 		Connection con = DB.getConnection();
 		try {
 			PreparedStatement pstmt = createSelectStatement(con, false);
 			ResultSet results = pstmt.executeQuery();
 			if (results.next())
 				return results.getDouble(1);
 			return 0;
 		} catch (Throwable t) {
 			LOG.error(t.getMessage(), t);
 			throw new IllegalStateException(t);
 		} finally {
 			con.close();
 		}
 		
 	}
 	
 	public long count() throws SQLException {
 		Connection con = DB.getConnection();
 		try {
 			PreparedStatement pstmt = createSelectStatement(con, true);
 			ResultSet results = pstmt.executeQuery();
 			if (results.next())
 				return results.getLong(1);
 			return 0;
 		} catch (Throwable t) {
 			LOG.error(t.getMessage(), t);
 			throw new IllegalStateException(t);
 		} finally {
 			con.close();
 		}
 	}
 
 	public Table limit(int l) {
 		limit = l;
 		return this;
 	}
 
 	public Table lastNDays(String column, int N) {
 		WhereExp exp = new WhereExp();
 		exp.exp = "(DATE(`" + column + "`)<=CURRENT_DATE() AND DATE(`" + column
 				+ "`)>=DATE_SUB(CURRENT_DATE(), INTERVAL " + N + " DAY))";
 		exp.values = new Object[0];
 		where.add(exp);
 		return this;
 	}
 
 	public Table nextNDays(String column, int N) {
 		WhereExp exp = new WhereExp();
 		exp.exp = "(DATE(`" + column + "`)>CURRENT_DATE() AND DATE(`" + column
 				+ "`)<=DATE_ADD(CURRENT_DATE(), INTERVAL " + N + " DAY))";
 		exp.values = new Object[0];
 		where.add(exp);
 		return this;
 	}
 
 	public void drop(boolean ignore) {
 		try {
 			DB.updateQuery("DROP TABLE `" + name + "`");
 			DB.loadMetadata(name);
 		} catch (Throwable e) {
 			if (!ignore)
 				throw new IllegalStateException(e);
 		}
 	}
 
 	public Table ascending(String column) {
 		this.orderBy = "`" + column + "` ASC";
 		return this;
 	}
 
 	public Table descending(String column) {
 		this.orderBy = column + " DESC";
 		return this;
 	}
 
 	public void delete() throws SQLException {
 		StringBuilder sb = new StringBuilder();
 		sb.append("DELETE FROM `" + name + "`");
 		whereStatement(sb);
 		Connection con = DB.getConnection();
 		try {
 			PreparedStatement pstmt = con.prepareStatement(sb.toString());
 			int c = 1;
 			whereValues(pstmt, c);
 			pstmt.executeUpdate();
 			pstmt.close();
 		} finally {
 			con.close();
 		}
 	}
 
 	private void whereStatement(StringBuilder sb) {
 		if (where.size() > 0) {
 			sb.append(" WHERE ");
 			ArrayList<String> items = new ArrayList<String>();
 			for (WhereExp exp : where) {
 				items.add(exp.exp);
 			}
 			sb.append(StringUtils.join(items, " AND "));
 		}
 	}
 
 	private int whereValues(PreparedStatement pstmt, int ctr)
 			throws SQLException {
 		for (WhereExp exp : where) {
 			for (Object value : exp.values) {
 				pstmt.setObject(ctr++, value);
 			}
 		}
 		return ctr;
 	}
 
 	public Table display(String column) throws JSONException {
 		comment.put("display", column);
 		return this;
 	}
 
 	public Table noDataEntry() throws JSONException {
 		comment.put("no-data-entry", true);
 		return this;
 	}
 
 	public Table join(Table t) {
 		joins.add(t);
 		return this;
 	}
 	
 	public Table groupBy(String column) {
 		this.groupBy = column;
 		return this;
 	}
 
 	public Table isNull(String column) {
 		WhereExp exp = new WhereExp();
 		exp.exp = "ISNULL(`" + column + "`)";
 		exp.values = new Object[0];
 		where.add(exp);
 		return this;
 	}
 	
 	public Table unique() {
 		this.unique = true;
 		return this;
 	}
 
 	public Table isNotNull(String column) {
 		WhereExp exp = new WhereExp();
 		exp.exp = "!ISNULL(`" + column + "`)";
 		exp.values = new Object[0];
 		where.add(exp);
 		return this;
 	}
 
 	public Table start(int s) {
 		start = s;
 		if (limit == -1)
 			limit = 10;
 		return this;
 	}
 
 }
