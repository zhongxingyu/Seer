 package org.eweb4j.orm.dao;
 
 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.sql.DataSource;
 
 import org.eweb4j.cache.DBInfoConfigBeanCache;
 import org.eweb4j.cache.ORMConfigBeanCache;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.orm.OrderType;
 import org.eweb4j.orm.Page;
 import org.eweb4j.orm.PageImpl;
 import org.eweb4j.orm.config.ORMConfigBeanUtil;
 import org.eweb4j.orm.dao.config.DAOConfigConstant;
 import org.eweb4j.orm.jdbc.DataSourceWrapCache;
 import org.eweb4j.orm.jdbc.JdbcUtil;
 import org.eweb4j.orm.sql.SelectSqlCreator;
 import org.eweb4j.orm.sql.SqlFactory;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.ReflectUtil;
 
 public class DAOImpl implements DAO {
 
 	private static Log log = LogFactory.getORMLogger(DAO.class);
 
 	private Map<String, Object> buffer = new HashMap<String, Object>();
 	
 	private String orderStr = "";
 	private StringBuilder groupBy = new StringBuilder();
 	private boolean express = false;
 	private Map<String, Object> map;
 	private Class<?> clazz;
 	private Class<?> targetEntity;
 	private String dsName;
 	private StringBuilder sql = new StringBuilder();
 	private StringBuilder condition = new StringBuilder();
 	private List<Object> args = new ArrayList<Object>();;
 	private String dbType;
 	private DataSource ds;
 	private Set<String> joins = new HashSet<String>();
 	private String table = null;
 	private String selectAllColumn;
 	private Set<String> unFetch = new HashSet<String>();
 	private Set<String> fetch = new HashSet<String>();
 	private List<String> updateFields = new ArrayList<String>();
 
 	private Map<String, _AliasField> aliasMap = new HashMap<String, _AliasField>();
 
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
 				table = (String) map.get("table") + " map";
 			}
 		} else {
 			this.table = ORMConfigBeanUtil.getTable(clazz, true);
 			selectAllColumn = ORMConfigBeanUtil.getSelectAllColumn(clazz);
 		}
 
 		if (selectAllColumn == null || selectAllColumn.trim().length() == 0)
 			selectAllColumn = "*";
 		
 		if (ORMConfigBeanUtil.getIdField(clazz) == null)
 			return ;
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
 
 	public DAO append(String query) {
 		Map<String, Object> map = handleFieldAlias(query);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		if (column == null)
 			column = query;
 
 		query = ORMConfigBeanUtil.parseQuery(query, clazz);
 
 		this.condition.append(" ").append(query).append(" ");
 		return this;
 	}
 	
 	public DAO field(String fieldName) {
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		
 		this.condition.append(" ").append(column).append(" ");
 		return this;
 	}
 
 	private Map<String, Object> handleFieldAlias(String fieldName) {
 		Map<String, Object> result = new HashMap<String, Object>(2);
 		result.put("field", fieldName);
 		result.put("class", this.clazz);
 		int dotIndex = fieldName.indexOf(".");
 		if (dotIndex > 0 && dotIndex < fieldName.length() -1){
 			String[] dots = fieldName.split("\\.");
 			StringBuilder builder = new StringBuilder();
 			Class<?> lastCls = null;
 			for (String dot : dots){
 				if (aliasMap.containsKey(dot)){
 					_AliasField a = aliasMap.get(dot);
 					dot = a.getField();
 					lastCls = a.getCls(); 
 				}if (builder.length() > 0)
 					builder.append(".");
 				
 				builder.append(dot);
 			}
 			if (builder.length() > 0){
 				fieldName = builder.toString();
 				result.put("field", fieldName);
 				result.put("class", lastCls);
 			}
 		}
 		
 		return result;
 	}
 
 	public DAO notLike(Object value) {
 		this.condition.append(" NOT LIKE ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("' ");
 		else{
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO notEqual(Object value) {
 		this.condition.append(" <> ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("' ");
 		else{
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO equal(Object value) {
 		this.condition.append(" = ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("' ");
 		else{
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO moreThan(Object value) {
 		this.condition.append(" > ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("' ");
 		else {
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO lessThan(Object value) {
 		this.condition.append(" < ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("' ");
 		else {
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			if (map == null)
 				return this;
 			
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO or(String fieldName) {
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		this.condition.append(" OR ").append(column).append(" ");
 		return this;
 	}
 
 	public DAO and(String fieldName) {
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		
 		this.condition.append(" AND ").append(column).append(" ");
 		return this;
 	}
 
 	public DAO orderBy(String fieldName) {
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		this.orderStr = " ORDER BY " + column;
 		this.buffer.put("orderField", column);
 		return this;
 	}
 	
 	public DAO desc(){
 		this.orderStr += " DESC ";
 		this.buffer.put("orderType", OrderType.DESC_ORDER);
 		return this;
 	}
 	
 	public DAO asc(){
 		this.orderStr += " ASC ";
 		this.buffer.put("orderType", OrderType.ASC_ORDER);
 		return this;
 	}
 	
 	public DAO order(String fieldName, String orderType){
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		this.orderStr = " ORDER BY " + column + " DESC ";
 		this.buffer.put("orderField", column);
 		if ("asc".equalsIgnoreCase(orderType))
 			this.buffer.put("orderType", OrderType.ASC_ORDER);
 		else
 			this.buffer.put("orderType", OrderType.DESC_ORDER);
 		
 		return this;
 	}
 	
 	public DAO desc(String fieldName) {
 		
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		this.orderStr = " ORDER BY " + column + " DESC ";
 		this.buffer.put("orderField", column);
 		this.buffer.put("orderType", OrderType.DESC_ORDER);
 		return this;
 	}
 
 	public DAO asc(String fieldName) {
 		Map<String, Object> map = handleFieldAlias(fieldName);
 		String _fieldName = (String)map.get("field");
 		Class<?> cls = (Class<?>)map.get("class");
 		
 		String column = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 		
 		this.orderStr = " ORDER BY " + column + " ASC ";
 		this.buffer.put("orderField", column);
 		this.buffer.put("orderType", OrderType.ASC_ORDER);
 		return this;
 	}
 	
 	public DAO sql(String sql){
 		if (sql == null)
 			return this;
 		this.sql = new StringBuilder(sql);
 		return this;
 	}
 	
 	// --------------------------------------------------------
 	public <T> DAO rowMapping(Class<T> targetEntity){
 		if (targetEntity == null)
 			return this;
 		this.targetEntity = targetEntity;
 		
 		return this;
 	}
 	public <T> T queryOne() {
 		Collection<T> list = query();
 		T result = list == null ? null : list.size() > 0 ? new ArrayList<T>(list).get(0) : null;
 		return result;
 	}
 	
 	public <T> Collection<T> query(){
 		return queryBySql(toSql());
 	}
 	
 	public <T> Collection<T> query(int max) {
 		return (Collection<T>) queryPage(1, max);
 	}
 	
 	public <T> Collection<T> query(int page, int length){
 		return (Collection<T>) queryPage(page, length);
 	}
 	
 	private <T> Collection<T> queryPage(final int page, final int length) {
 		String _sql = null;
 		final String orderField = (String) buffer.get("orderField");
 		final int oType = (Integer) buffer.get("orderType");
 		final String query = condition.toString().replace("'?'", "?") + groupBy.toString();
 		try {
 			Object obj = null;
 			if (Map.class.isAssignableFrom(clazz)){
 				obj = new HashMap<String, Object>();
 				((Map<String, Object>)obj).put("table", table);
 			}else {
 				obj = clazz.newInstance();
 			}
 			
 			SelectSqlCreator<Object> select =  SqlFactory.getSelectSql(obj, dbType);
 			
 			if (joins != null && !joins.isEmpty()){
 				StringBuilder sb = new StringBuilder();
 				for (String j : joins){
 					if (sb.length() > 0)
 						sb.append(", ");
 					sb.append(j);
 				}
 				select.setTable(table + ", " + sb.toString());
 			}
 			select.setSelectAllColumn(this.selectAllColumn);
 			Map<String, Object> a = this.handleFieldAlias(orderField);
 			_sql = select.divPage(page, length, orderField, (Class<?>)a.get("class"), oType, query.replace("WHERE", ""));
 		} catch (Exception e) {
 			String _table = table;
 			if (joins != null && !joins.isEmpty()){
 				StringBuilder sb = new StringBuilder();
 				for (String j : joins){
 					if (sb.length() > 0)
 						sb.append(", ");
 					sb.append(j);
 				}
 				_table = table + ", " + sb.toString();
 			}
 			
 			_sql = sql.append(orderStr).append(" LIMIT ").append((page - 1) * length).append(", ").append(length).toString().replace("${_TABLES_}", _table).replace("${_where_}", query);
 		}
 		
 		return queryBySql(_sql);
 	}
 	
 	public <T> Collection<T> queryBySql(final String sql) {
 		Class<T> mappingCls = null;
 		if (this.targetEntity == null)
 			mappingCls = (Class<T>) this.clazz;
 		else
 			mappingCls = (Class<T>) this.targetEntity;
 		
 		List<T> result = null;
 		try {
 			if (Map.class.isAssignableFrom(mappingCls)) {
 				Connection con = ds.getConnection();
 				if (args != null && args.size() > 0) {
 					result = (List<T>) JdbcUtil.getListWithArgs(con, mappingCls, sql, args.toArray(new Object[] {}));
 				} else {
 					result = (List<T>) JdbcUtil.getList(con, mappingCls, sql);
 				}
 
 			} else {
 				if (args != null && args.size() > 0) {
 					result = (List<T>) DAOFactory.getSelectDAO(dsName).selectBySQL(mappingCls, sql, args.toArray(new Object[] {}));
 				} else {
 					result = (List<T>) DAOFactory.getSelectDAO(dsName).selectBySQL(mappingCls, sql);
 				}
 
 			}
 
 			//this.clear();
 			if (result != null && result.size() > 0){
 				for (T t : result){
 					// ToOne relation class cascade select
 					final String[] fields = ORMConfigBeanUtil.getFields(t.getClass());
 					if (fields == null || fields.length == 0)
 						continue;
 					ReflectUtil ru = new ReflectUtil(t);
 					for (String f : fields){
 						Field field = ru.getField(f);
 						if (field == null)
 							continue;
 						if (field.getType() == null)
 							continue;
 						boolean isEntity = ORMConfigBeanCache.containsKey(field.getType().getName());
 						if (!isEntity)
 							continue;
 						
 						OneToOne o2o = field.getAnnotation(OneToOne.class);
 						ManyToOne m2o = field.getAnnotation(ManyToOne.class);
 						OneToMany o2m = field.getAnnotation(OneToMany.class);
 						ManyToMany m2m = field.getAnnotation(ManyToMany.class);
 						FetchType fetchType = null;
 						if (o2o != null)
 							fetchType = o2o.fetch();
 						if (m2o != null)
 							fetchType = m2o.fetch();
 						if (o2m != null)
 							fetchType = o2m.fetch();
 						if (m2m != null)
 							fetchType = m2m.fetch();
 						
 						if (fetchType == null)
 							continue;
 						
 						if (unFetch.contains(f))
 							continue;
 						
 						if (fetch.contains(f)){
 							DAOFactory.getCascadeDAO(dsName).select(t, f);
 							log.debug("cascade select -> " + t.getClass().getName() +"." + f);
 							continue;
 						}
 						
 						if (FetchType.LAZY.equals(fetchType))
 							continue ;
 						
 						log.debug("cascade select -> " + t.getClass().getName() +"." + f);
 						DAOFactory.getCascadeDAO(dsName).select(t, f);
 					}
 				}
 			}
 			
 			return result;
 		} catch (Exception e) {
 			log.error("sql-->" + sql + "exception:" + CommonUtil.getExceptionString(e));
 			throw new DAOException(sql + " execute exception", e);
 		}
 	}
 	
 	public long count(){
 		final String query = this.condition.toString().replace("WHERE", "").replace("'?'", "?");
 		String _table = this.table;
 		if (this.joins != null && !this.joins.isEmpty()){
 			StringBuilder sb = new StringBuilder();
 			for (String j : joins){
 				if (sb.length() > 0)
 					sb.append(", ");
 				sb.append(j);
 			}
 			_table = this.table + ", " + sb.toString();
 		}
 		
 		String sql = "SELECT COUNT(*) as count FROM " + _table ;;
 		if (query != null && query.trim().length() > 0)
 			sql += " WHERE " + ORMConfigBeanUtil.parseQuery(query, clazz);
 		
 		List<Map> maps = null;
 		if (args != null && args.size() > 0) 
 			maps = DAOFactory.getSelectDAO(dsName).selectBySQL(Map.class, sql, args);
 		else
 			maps = DAOFactory.getSelectDAO(dsName).selectBySQL(Map.class, sql);
 		
 		if (maps == null || maps.isEmpty())
 			return 0;
 		
 		return Long.parseLong(String.valueOf(maps.get(0).get("count")));
 	}
 
 	public DAO selectStr(String str) {
 		if (str == null || clazz == null)
 			return this;
 
 		this.sql.append(" SELECT ").append(str).append(" FROM ")
 				.append(" ${_TABLES_} ").append(" ");
 
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
 
 		this.sql.append(" INSERT INTO ").append(table.replace(" "+clazz.getSimpleName().toLowerCase(), "")).append("(").append(sb.toString()).append(") ");
 
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
 		int rs = 0;
 		String sql = this.sql.toString().replace("${_where_}", this.condition.toString()).replace("'?'", "?");
 		DataSource ds = DataSourceWrapCache.get(dsName);
 		try {
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
 
 					Number id = DAOUtil.selectMaxId(map, ds.getConnection(),dbType);
 					return id == null ? 0 : id.intValue();
 				} else {
 					Number id = DAOUtil.selectMaxId(clazz,ds.getConnection(), dbType);
 					return id == null ? 0 : id.intValue();
 				}
 			}
 
 		} catch (SQLException e) {
 			log.error("sql-->" + sql + "exception:" + CommonUtil.getExceptionString(e));
 			throw new DAOException(sql + " execute exception", e);
 		}
 
 		//this.clear();
 		return rs;
 	}
 
 	public DAO update(String... fields) {
 		if (clazz == null)
 			return this;
 		
 		if (fields == null || fields.length == 0)
 			return this;
 
 		StringBuilder sb = new StringBuilder();
 		String[] columns = ORMConfigBeanUtil.getColumns(clazz, fields);
 		for (int i = 0; i < columns.length; i++) {
 			if (sb.length() > 0)
 				sb.append(", ");
 			String col = columns[i];
 			this.updateFields.add(col);
 		}
 		
 		this.sql.append(" UPDATE ").append(table.replace(" "+clazz.getSimpleName().toLowerCase(), "")).append(" ");
 
 		return this;
 	}
 
 	public DAO set(Object... values) {
 		if (this.updateFields == null || values == null || this.updateFields.size() == 0
 				|| values.length == 0 || this.updateFields.size() != values.length)
 			return this;
 
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < values.length; i++) {
 			if (sb.length() > 0)
 				sb.append(", ");
 			String col = this.updateFields.get(i);
 			Object val = values[i];
 			sb.append(col).append(" = '").append(val).append("'");
 		}
 
 		this.sql.append("SET ").append(sb.toString()).append(" ");
 
 		return this;
 	}
 
 	public DAO update(Map<String, Object> map) {
 		int size = map.size();
 		List<String> fields = new ArrayList<String>(size);
 		List<Object> values = new ArrayList<Object>(size);
 		for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
 			Entry<String, Object> entry = it.next();
 			fields.add(entry.getKey());
 			values.add(entry.getValue());
 		}
 		
 		this.update(fields.toArray(new String[]{})).set(values.toArray(new Object[] {}));
 
 		return this;
 	}
 
 	public DAO delete() {
 		if (clazz == null)
 			return this;
 
 		this.sql.append(" DELETE FROM ").append(table.replace(" "+clazz.getSimpleName().toLowerCase(), "")).append(" ");
 
 		return this;
 	}
 
 	public DAO selectAll() {
 		this.sql.append(" SELECT ").append(selectAllColumn)
 				.append(" FROM ").append(" ${_TABLES_} ").append(" ");
 		return this;
 	}
 
 	public DAO select(Class<?>... classes){
 		if (classes == null || classes.length == 0)
 			return this;
 		this.sql.append(" SELECT ");
 		StringBuilder sb = new StringBuilder();
 		for(Class<?> cls : classes){
 			
 			String _selectAllColumn = ORMConfigBeanUtil.getSelectAllColumn(cls);
 			if (sb.length() > 0)
 				sb.append(", ");
 			sb.append(_selectAllColumn);
 		}
 		
 		if (sb.length() > 0)
 			selectAllColumn = sb.toString();
 		
 		this.sql.append(this.selectAllColumn).append(" FROM ").append(" ${_TABLES_} ").append(" ");
 		rowMapping(classes[0]);
 		
 		return this;
 	}
 	
 	public DAO select(String... fields) {
 		if (fields == null || clazz == null)
 			return this;
 
 		StringBuilder sb = new StringBuilder();
 		for (String field : fields) {
 			if (sb.length() > 0)
 				sb.append(", ");
 
 			Map<String, Object> map = handleFieldAlias(field);
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			String col = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 			sb.append(col);
 		}
 		this.sql.append(" SELECT ").append(sb.toString())
 				.append(" FROM ").append(" ${_TABLES_} ").append(" ");
 
 		return this;
 	}
 
 	public DAO likeLeft(Object value) {
 		this.condition.append(" LIKE ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("%' ");
 		else{
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO likeRight(Object value) {
 		this.condition.append(" LIKE ");
 		
 		if (!this.express)
 			condition.append("'%").append(value).append("' ");
 		else {
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO like(Object value) {
 		this.condition.append(" LIKE ");
 		
 		if (!this.express)
 			condition.append("'%").append(value).append("%' ");
 		else {
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 
 	public DAO clear() {
 		this.sql = null;
 		this.sql = new StringBuilder();
 		this.buffer.clear();
 		if (ORMConfigBeanUtil.getIdField(clazz) != null){
 			this.buffer.put("orderField", ORMConfigBeanUtil.getIdField(clazz));
 			this.buffer.put("orderType", OrderType.DESC_ORDER);
 		}
 		this.condition = null;
 		this.condition = new StringBuilder();
 		this.args.clear();
 		this.orderStr = "";
 		this.joins.clear();
 		this.express = false;
 		this.aliasMap.clear();
 		this.unFetch.clear();
 		this.fetch.clear();
 		this.updateFields.clear();
 		this.targetEntity = null;
 
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
 
 			if (!this.express)
 				sb.append("'").append(o).append("'");
 			else {
 				Map<String, Object> map = handleFieldAlias(String.valueOf(o));
 				String _fieldName = (String)map.get("field");
 				Class<?> cls = (Class<?>)map.get("class");
 				
 				sb.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 			}
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
 
 			if (!this.express)
 				sb.append("'").append(o).append("'");
 			else {
 				Map<String, Object> map = handleFieldAlias(String.valueOf(o));
 				String _fieldName = (String)map.get("field");
 				Class<?> cls = (Class<?>)map.get("class");
 				
 				sb.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 			}
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
 		String _table = this.table;
 		if (this.joins != null && !this.joins.isEmpty()){
 			StringBuilder sb = new StringBuilder();
 			for (String j : joins){
 				if (sb.length() > 0)
 					sb.append(", ");
 				sb.append(j);
 			}
 			_table = this.table + ", " + sb.toString();
 		}
 		
 		return sql.append(orderStr).toString().replace("${_TABLES_}", _table).replace("${_where_}", condition.toString()+groupBy.toString()).replace("'?'", "?");
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
 			this.table = ORMConfigBeanUtil.getTable(clazz, true);
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
 
 	public <T> Page<T> getPage(int pageIndex, int pageSize) {
 		Page<T> page = new PageImpl<T>(pageIndex, pageSize, this);
 		return page;
 	}
 
 	public DAO join(String fieldName){
 		return join(fieldName, fieldName);
 	}
 	
 	public DAO enableExpress(boolean flag){
 		this.express = flag;
 		return this;
 	}
 	
 	public DAO join(String fieldName, String alias) {
 		if (fieldName == null || alias == null)
 			return this;
 		
 		handleJoin(fieldName, alias);
 		
 		return this;
 	}
 	
 	public DAO alias(String alias){
 		if (alias == null || alias.trim().length() == 0)
 			return this;
 		_AliasField a = new _AliasField(alias, clazz.getSimpleName().toLowerCase(), clazz);
 		this.aliasMap.put(alias, a);
 		return this;
 	}
 
 	private void handleJoin(String _fieldName, String _alias) {
 		String[] fDots = _fieldName.split("\\.");
 		String[] aDots = _alias.split("\\.");
 		Class<?> currentClazz = this.clazz;
 		for (int i = 0; i < fDots.length; i++){
 			String fieldName = fDots[i];
 			String alias = aDots[i];
 		
 			ReflectUtil ru = null;
 			try {
 				ru = new ReflectUtil(currentClazz);
 				Field field = ru.getField(fieldName);
 				if (field == null)
 					throw new Exception("field->"+fieldName+" invalid");
 				Class<?> cls = ClassUtil.getGenericType(field);
 				if (cls == null)
 					throw new Exception("can not get the field->"+fieldName+" class");
 				if (!ORMConfigBeanCache.containsKey(cls.getName()))
					throw new Exception("field->" + fieldName + cls.getName() + " is not a entity");
 				String table = ORMConfigBeanUtil.getTable(cls, true);
 				joins.add(table);
 				_AliasField a = new _AliasField(alias, cls.getSimpleName().toLowerCase(), cls);
 				aliasMap.put(alias, a);
 				currentClazz = cls;
 			} catch (Exception e){
 				log.error(e.toString());
 			}
 		}
 	}
 
 	public DAO on() {
 		return null;
 	}
 
 	public DAO groupBy(String... fieldNames) {
 		if (fieldNames == null || fieldNames.length == 0)
 			return this;
 
 		StringBuilder builder = new StringBuilder();
 		for (String field : fieldNames){
 			Map<String, Object> map = handleFieldAlias(field);
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			String col = ORMConfigBeanUtil.getColumn(cls, _fieldName);
 			if (builder.length() > 0)
 				builder.append(", ");
 			builder.append(col);
 		}
 		this.groupBy.append(" group by ").append(builder.toString()).append(" ");
 
 		return this;
 	}
 
 	public DAO fetch(String... fields) {
 		if (fields == null)
 			return this;
 		for (String field : fields)
 			this.fetch.add(field);
 		return this;
 	}
 
 	public DAO unfetch(String... fields) {
 		if (fields == null)
 			return this;
 		for (String field : fields)
 			this.unFetch.add(field);
 		return this;
 	}
 
 	public DAO leftJoin(String fieldName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public DAO leftJoin(String fieldName, String alias) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public DAO rightJoin(String fieldName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public DAO rightJoin(String fieldName, String alias) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public DAO likeEqual(Object value) {
 		if (value == null)
 			return this;
 		
 		this.condition.append(" LIKE ");
 		
 		if (!this.express)
 			condition.append("'").append(value).append("' ");
 		else {
 			Map<String, Object> map = handleFieldAlias(String.valueOf(value));
 			String _fieldName = (String)map.get("field");
 			Class<?> cls = (Class<?>)map.get("class");
 			
 			condition.append(ORMConfigBeanUtil.getColumn(cls, _fieldName));
 		}
 		
 		return this;
 	}
 	
 	private static class _AliasField{
 		private String alias;
 		private String field;
 		private Class<?> cls;
 		
 		public _AliasField() {
 			super();
 		}
 		
 		public _AliasField(String alias, String field, Class<?> cls) {
 			super();
 			this.alias = alias;
 			this.field = field;
 			this.cls = cls;
 		}
 		public String getAlias() {
 			return alias;
 		}
 		public void setAlias(String alias) {
 			this.alias = alias;
 		}
 		public String getField() {
 			return field;
 		}
 		public void setField(String field) {
 			this.field = field;
 		}
 		public Class<?> getCls() {
 			return cls;
 		}
 		public void setCls(Class<?> cls) {
 			this.cls = cls;
 		}
 	}
 }
