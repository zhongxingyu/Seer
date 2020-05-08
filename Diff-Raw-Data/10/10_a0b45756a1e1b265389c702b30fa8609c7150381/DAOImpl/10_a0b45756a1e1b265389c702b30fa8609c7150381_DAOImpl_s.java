 package org.eweb4j.orm.dao;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.sql.DataSource;
 
 import org.eweb4j.cache.DBInfoConfigBeanCache;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.orm.OrderType;
 import org.eweb4j.orm.config.ORMConfigBeanUtil;
 import org.eweb4j.orm.dao.config.DAOConfigConstant;
 import org.eweb4j.orm.jdbc.DataSourceWrapCache;
 import org.eweb4j.orm.jdbc.JdbcUtil;
 import org.eweb4j.orm.sql.SqlFactory;
 import org.eweb4j.util.StringUtil;
 
 public class DAOImpl implements DAO {
 
 	private static Log log = LogFactory.getORMLogger(DAOImpl.class);
 
 	private Map<String, Object> buffer = new HashMap<String, Object>();
 	
 	private String orderStr = "";
 
 	private Map<String, Object> map;
 	private Class<?> clazz;
 	private String dsName;
 	private StringBuilder sql = new StringBuilder("");
 	private StringBuilder condition = new StringBuilder("");
 	private List<Object> args = new ArrayList<Object>();;
 	private String dbType;
 	private DataSource ds;
 	private String table;
 	private String selectAllColumn;
 
 	public DAOImpl(String dsName) {
 		this.dsName = dsName;
 
 		this.dsName = dsName;
 		this.ds = DataSourceWrapCache.get(dsName);
 
 		dbType = DBInfoConfigBeanCache.get(dsName).getDataBaseType();
 	}
 
 	private void init(Class<?> clazz, String dsName) {
 		if (dsName == null)
 			dsName = DAOConfigConstant.MYDBINFO;
 
 		this.clazz = clazz;
 		this.dsName = dsName;
 		this.ds = DataSourceWrapCache.get(dsName);
 
 		dbType = DBInfoConfigBeanCache.get(dsName).getDataBaseType();
 		if (Map.class.isAssignableFrom(clazz)) {
 			if (this.map != null) {
 				selectAllColumn = ORMConfigBeanUtil.getSelectAllColumn(map);
 				table = (String) map.get("table");
 			}
 		} else {
 			this.table = ORMConfigBeanUtil.getTable(clazz);
 			selectAllColumn = ORMConfigBeanUtil.getSelectAllColumn(clazz);
 		}
 
 		if (selectAllColumn == null || selectAllColumn.trim().length() == 0)
 			selectAllColumn = "*";
 		
 		this.buffer.put("orderField", ORMConfigBeanUtil.getIdField(clazz));
 		this.buffer.put("orderType", OrderType.DESC_ORDER);
 	}
 
 	public DAOImpl(Map<String, Object> map) {
 		this.map = map;
 		init(Map.class, null);
 	}
 
 	public DAOImpl(Map<String, Object> map, String dsName) {
 		this.map = map;
 		init(Map.class, dsName);
 	}
 
 	public DAOImpl(Class<?> clazz) {
 		init(clazz, null);
 	}
 
 	public DAOImpl(Class<?> clazz, String dsName) {
 		init(clazz, dsName);
 	}
 
 	public DAOImpl append(String query) {
 		String column = ORMConfigBeanUtil.getColumn(clazz, query);
 		if (column == null)
 			column = query;
 
 		query = ORMConfigBeanUtil.parseQuery(query, clazz);
 
 		this.condition.append(" ").append(query).append(" ");
 		return this;
 	}
 
 	public DAOImpl field(String fieldName) {
 		String column = ORMConfigBeanUtil.getColumn(clazz, fieldName);
 		this.condition.append(" ").append(column).append(" ");
 		return this;
 	}
 
 	public DAOImpl notLike(Object value) {
 		this.condition.append(" NOT LIKE '").append(value).append("' ");
 		return this;
 	}
 
 	public DAOImpl notEqual(Object value) {
 		this.condition.append(" <> '").append(value).append("' ");
 		return this;
 	}
 
 	public DAOImpl equal(Object value) {
 		this.condition.append(" = '").append(value).append("' ");
 		return this;
 	}
 
 	public DAOImpl moreThan(Object value) {
 		this.condition.append(" > '").append(value).append("' ");
 		return this;
 	}
 
 	public DAOImpl lessThan(Object value) {
 		this.condition.append(" < '").append(value).append("' ");
 		return this;
 	}
 
 	public DAOImpl or(String fieldName) {
 		String column = ORMConfigBeanUtil.getColumn(clazz, fieldName);
 		this.condition.append(" OR ").append(column).append(" ");
 		return this;
 	}
 
 	public DAOImpl and(String fieldName) {
 		String column = ORMConfigBeanUtil.getColumn(clazz, fieldName);
 		this.condition.append(" AND ").append(column).append(" ");
 		return this;
 	}
 
 	public DAOImpl desc(String fieldName) {
 		String column = ORMConfigBeanUtil.getColumn(clazz, fieldName);
 		this.orderStr = " ORDER BY " + column + " DESC ";
 		this.buffer.put("orderField", column);
 		this.buffer.put("orderType", OrderType.DESC_ORDER);
 		return this;
 	}
 
 	public DAOImpl asc(String fieldName) {
 		String column = ORMConfigBeanUtil.getColumn(clazz, fieldName);
 		this.orderStr = " ORDER BY " + column + " ASC ";
 		this.buffer.put("orderField", column);
 		this.buffer.put("orderType", OrderType.ASC_ORDER);
 		return this;
 	}
 
 	// --------------------------------------------------------
 
 	private <T> List<T> query(String sql) {
 		List<T> result = null;
 		try {
 
 			if (Map.class.isAssignableFrom(this.clazz)) {
 				Connection con = ds.getConnection();
 				if (args != null && args.size() > 0) {
 					result = (List<T>) JdbcUtil.getListWithArgs(con, clazz, sql, args.toArray(new Object[] {}));
 				} else {
 					result = (List<T>) JdbcUtil.getList(con, clazz, sql);
 				}
 
 			} else {
 				if (args != null && args.size() > 0) {
 					result = (List<T>) DAOFactory.getSelectDAO(dsName).selectBySQL(clazz, sql, args.toArray(new Object[] {}));
 				} else {
 					result = (List<T>) DAOFactory.getSelectDAO(dsName).selectBySQL(clazz, sql);
 				}
 
 			}
 
 			//this.clear();
 			if (result != null && result.size() > 0){
 				for (T t : result){
 					// ToOne relation class cascade select
 					final String[] fields = ORMConfigBeanUtil.getToOneField(t.getClass());
 					if (fields == null || fields.length == 0)
 						continue;
 					
 					DAOFactory.getCascadeDAO(dsName).select(t, fields);
 				}
 			}
 			
 			return result;
 		} catch (Exception e) {
 			log.error("sql-->" + sql + "exception:" + StringUtil.getExceptionString(e));
 			throw new DAOException(sql + " execute exception", e);
 		}
 	}
 	
 	public long count(){
 		final String query = this.condition.toString().replace("WHERE", "").replace("'?'", "?");
 		if (args != null && args.size() > 0) {
 			return DAOFactory.getSelectDAO(dsName).selectCount(this.clazz, query, args.toArray(new Object[] {}));
 		}
 		
 		return DAOFactory.getSelectDAO(dsName).selectCount(this.clazz, query);
 	}
 
 	public <T> List<T> query() {
 		return query(toSql());
 
 	}
 
 	public <T> List<T> query(int max) {
 		return query(1, max);
 	}
 
 	public <T> List<T> query(int page, int length) {
 		String sql = null;
 		final String orderField = (String) buffer.get("orderField");
 		final int oType = (Integer) buffer.get("orderType");
 		final String query = this.condition.toString().replace("'?'", "?");
 		try {
			sql = SqlFactory.getSelectSql(clazz.newInstance(), dbType).divPage(page, length, orderField, oType, query.replace("WHERE", ""));
 		} catch (Exception e) {
 			e.printStackTrace();
 			sql = this.sql.append(orderStr).append(" LIMIT ").append((page - 1) * length).append(", ").append(length).toString().replace("${_where_}", query);
 		}
 		
 		return query(sql);
 	}
 
 	public <T> T queryOne() {
 		List<T> list = query();
 		T result = list == null ? null : list.size() > 0 ? list.get(0) : null;
 		return result;
 	}
 
 	public DAO selectStr(String str) {
 		if (str == null || clazz == null)
 			return this;
 
 		this.sql.append(" SELECT ").append(str).append(" FROM ")
 				.append(table).append(" ");
 
 		return this;
 	}
 
 	public DAO insert(String... fields) {
 		if (clazz == null)
 			return this;
 
 		StringBuilder sb = new StringBuilder();
 		String[] columns;
 		if (fields == null)
 			columns = ORMConfigBeanUtil.getColumns(clazz);
 		else
 			columns = ORMConfigBeanUtil.getColumns(clazz, fields);
 
 		for (String col : columns) {
 			if (sb.length() > 0)
 				sb.append(", ");
 			sb.append(col);
 		}
 
 		this.sql.append(" INSERT INTO ").append(table).append("(").append(sb.toString()).append(") ");
 
 		return this;
 	}
 
 	public DAO values(Object... values) {
 		if (values == null) {
 			values = new Object[ORMConfigBeanUtil.getColumns(clazz).length];
 
 			for (int i = 0; i < values.length; i++)
 				values[i] = "?";
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (Object val : values) {
 			if (sb.length() > 0)
 				sb.append(", ");
 			sb.append("'").append(val).append("'");
 		}
 		this.sql.append(" VALUES(").append(sb.toString()).append(") ");
 
 		return this;
 	}
 
 	public DAO insert(Map<String, Object> map) {
 		int size = map.size();
 		List<String> fields = new ArrayList<String>(size);
 		List<Object> values = new ArrayList<Object>(size);
 		for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
 			Entry<String, Object> entry = it.next();
 			fields.add(entry.getKey());
 			values.add(entry.getValue());
 		}
 
 		this.insert(fields.toArray(new String[] {})).values(
 				values.toArray(new Object[] {}));
 
 		return this;
 	}
 
 	public DAO where() {
 		this.sql.append("${_where_}");
 		this.condition.append(" WHERE ");
 		return this;
 	}
 	
 	public DAO isNull(){
 		this.condition.append(" IS NULL ");
 		return this;
 	}
 	
 	public DAO isNotNull(){
 		this.condition.append(" IS NOT NULL ");
 		return this;
 	}
 
 	public int execute() {
 		int id = -1;
 		String sql = this.sql.toString().replace("${_where_}", this.condition.toString()).replace("'?'", "?");
 		DataSource ds = DataSourceWrapCache.get(dsName);
 		try {
 			int rs = 0;
 			if (args != null && args.size() > 0) {
 				rs = (Integer) JdbcUtil.updateWithArgs(ds.getConnection(), sql, args.toArray(new Object[] {}));
 			} else {
 				rs = (Integer) JdbcUtil.update(ds.getConnection(), sql);
 			}
 
 			if (rs > 0 && sql.contains("INSERT INTO")) {
 				if (Map.class.isAssignableFrom(clazz)) {
 					if (map == null) {
 						map = new HashMap<String, Object>();
 						map.put("idColumn", "id");
 						map.put("table", this.table);
 					} else if (map.get("idColumn") == null) {
 						map.put("idColumn", "id");
 					}
 
 					id = (Integer) DAOUtil.selectMaxId(map, ds.getConnection(),dbType);
 				} else {
 					id = (Integer) DAOUtil.selectMaxId(clazz,ds.getConnection(), dbType);
 				}
 			}
 
 		} catch (SQLException e) {
 			log.error("sql-->" + sql + "exception:" + StringUtil.getExceptionString(e));
 			throw new DAOException(sql + " execute exception", e);
 		}
 
 		//this.clear();
 		return id;
 	}
 
 	public DAO update() {
 		if (clazz == null)
 			return this;
 
 		this.sql.append(" UPDATE ").append(table).append(" ");
 
 		return this;
 	}
 
 	public DAO set(String[] fields, Object... values) {
 		if (fields == null || values == null || fields.length == 0
 				|| values.length == 0 || fields.length != values.length)
 			return this;
 
 		StringBuilder sb = new StringBuilder();
 		String[] columns = ORMConfigBeanUtil.getColumns(clazz, fields);
 		for (int i = 0; i < values.length; i++) {
 			if (sb.length() > 0)
 				sb.append(", ");
 			String col = columns[i];
 			Object val = values[i];
 			sb.append(col).append(" = '").append(val).append("'");
 		}
 
 		this.sql.append(" SET ").append(sb.toString()).append(" ");
 
 		return this;
 	}
 
 	public DAO set(Map<String, Object> map) {
 		int size = map.size();
 		List<String> fields = new ArrayList<String>(size);
 		List<Object> values = new ArrayList<Object>(size);
 		for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
 			Entry<String, Object> entry = it.next();
 			fields.add(entry.getKey());
 			values.add(entry.getValue());
 		}
 
 		this.set(fields.toArray(new String[] {}),
 				values.toArray(new Object[] {}));
 
 		return this;
 	}
 
 	public DAO delete() {
 		if (clazz == null)
 			return this;
 
 		this.sql.append(" DELETE FROM ").append(table).append(" ");
 
 		return this;
 	}
 
 	public DAO selectAll() {
 		this.sql.append(" SELECT ").append(selectAllColumn)
 				.append(" FROM ").append(table).append(" ");
 		return this;
 	}
 
 	public DAO select(String... fields) {
 		if (fields == null || clazz == null)
 			return this;
 
 		StringBuilder sb = new StringBuilder();
 		for (String field : fields) {
 			if (sb.length() > 0)
 				sb.append(", ");
 			String col = ORMConfigBeanUtil.getColumn(clazz, field);
 			sb.append(col);
 		}
 		this.sql.append(" SELECT ").append(sb.toString())
 				.append(" FROM ").append(table).append(" ");
 
 		return this;
 	}
 
 	public DAO likeLeft(Object value) {
 		this.condition.append(" LIKE '").append(value).append("%' ");
 		return this;
 	}
 
 	public DAO likeRight(Object value) {
 		this.condition.append(" LIKE '%").append(value).append("' ");
 		return this;
 	}
 
 	public DAO like(Object value) {
 		this.condition.append(" LIKE '%").append(value).append("%' ");
 		return this;
 	}
 
 	public DAO clear() {
 		this.sql = null;
 		this.sql = new StringBuilder();
 		this.buffer.clear();
 		this.buffer.put("orderField", ORMConfigBeanUtil.getIdField(clazz));
 		this.buffer.put("orderType", OrderType.DESC_ORDER);
 		this.condition = null;
 		this.condition = new StringBuilder();
 		this.args.clear();
 		this.orderStr = "";
 
 		return this;
 	}
 
 	public DAO in(Object... values) {
 		if (values == null)
 			return this;
 
 		this.condition.append(" in(");
 
 		StringBuilder sb = new StringBuilder();
 		for (Object o : values) {
 			if (sb.length() > 0)
 				sb.append(", ");
 
 			sb.append("'").append(o).append("'");
 		}
 
 		this.condition.append(sb.toString());
 
 		this.condition.append(") ");
 
 		return this;
 	}
 
 	public DAO notIn(Object... values) {
 		if (values == null)
 			return this;
 
 		this.condition.append(" not in(");
 
 		StringBuilder sb = new StringBuilder();
 		for (Object o : values) {
 			if (sb.length() > 0)
 				sb.append(", ");
 
 			sb.append("'").append(o).append("'");
 		}
 
 		this.condition.append(sb.toString());
 
 		this.condition.append(") ");
 
 		return this;
 	}
 
 	public DAO inSql(String sql) {
 		if (sql == null)
 			return this;
 
 		this.condition.append(" in(").append(sql).append(") ");
 
 		return this;
 	}
 
 	public DAO notInSql(String sql) {
 		if (sql == null)
 			return this;
 
 		this.condition.append(" not in(").append(sql).append(") ");
 
 		return this;
 	}
 
 	public String toSql() {
 		return sql.toString().replace("${_where_}", condition.toString()).replace("'?'", "?");
 	}
 
 	public DAO fillArgs(Object... args) {
 		if (args != null) {
 			for (Object arg : args)
 				this.args.add(arg);
 		}
 
 		return this;
 	}
 
 	public DAO setTable(String table) {
 		this.table = table;
 		return this;
 	}
 
 	public DAO setClass(Class<?> clazz) {
 		this.clazz = clazz;
 
 		if (Map.class.isAssignableFrom(clazz)) {
 			if (this.map != null) {
 				selectAllColumn = ORMConfigBeanUtil.getSelectAllColumn(map);
 				table = (String) map.get("table");
 			}
 		} else {
 			this.table = ORMConfigBeanUtil.getTable(clazz);
 			selectAllColumn = ORMConfigBeanUtil.getSelectAllColumn(clazz);
 		}
 
 		if (selectAllColumn == null || selectAllColumn.trim().length() == 0)
 			selectAllColumn = "*";
 
 		return this;
 	}
 
 	public DAO setMap(Map<String, Object> map) {
 		this.map = map;
 		return this;
 	}
 
 	public String getDsName() {
 		return dsName;
 	}
 	
 	
 }
