 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.sql;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import zcu.xutil.Constants;
 import zcu.xutil.Logger;
 import zcu.xutil.sql.handl.FirstField;
 import zcu.xutil.utils.Accessor;
 import zcu.xutil.utils.LRUCache;
 import static zcu.xutil.Objutil.*;
 
 /**
  * 
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public class DBTool {
 	private static final LRUCache<Class, EntityMap<Accessor>> cache = new LRUCache<Class, EntityMap<Accessor>>(
 			systring(Constants.XUTILS_SQL_DBTOOL_CACHE, 95), null);
 	private static final Logger logger = Logger.getLogger(DBTool.class);
 
 	static EntityMap<Accessor> getAllAccessor(Class clazz) {
 		EntityMap<Accessor> result = cache.get(clazz);
 		if (result != null)
 			return result;
 		result = new EntityMap<Accessor>();
 		Accessor.build(clazz, result, result);
 		return ifNull(cache.putIfAbsent(clazz, result), result);
 	}
 
 	public static <T> T query(Connection conn, String sql, Handler<T> rsh) throws SQLException {
 		logger.debug(sql);
 		Statement stmt = rsh.getOptions().createStatement(conn);
 		try {
 			return rsh.handle(stmt.executeQuery(sql));
 		} finally {
 			stmt.close();
 		}
 	}
 
 	private static PreparedStatement fillParams(PreparedStatement statement, Object[] params) throws SQLException {
 		Object o;
 		int i = params.length;
 		while (--i >= 0) {
 			if ((o = params[i]) == null)
 				statement.setNull(i + 1, Types.NULL);
 			else if (o instanceof Class)
 				SQLType.setNull(statement, i + 1, (Class) o);
 			else
 				SQLType.setValue(statement, i + 1, o);
 		}
 		return statement;
 	}
 
 	public static <T> T query(Connection conn, String sql, Handler<T> rsh, Object... params) throws SQLException {
 		if (params == null)
 			return query(conn, sql, rsh);
 		logger.debug("{} ,params: {}", sql, params);
 		PreparedStatement stmt = rsh.getOptions().prepareStatement(conn, sql);
 		try {
 			return rsh.handle(fillParams(stmt, params).executeQuery());
 		} finally {
 			stmt.close();
 		}
 	}
 
 	public static int update(Connection conn, String sql) throws SQLException {
 		logger.debug(sql);
 		Statement stmt = conn.createStatement();
 		try {
 			return stmt.executeUpdate(sql);
 		} finally {
 			stmt.close();
 		}
 	}
 
 	public static int update(Connection conn, String sql, Object... params) throws SQLException {
 		if (params == null)
 			return update(conn, sql);
 		logger.debug("{} ,params: {}", sql, params);
 		PreparedStatement stmt = conn.prepareStatement(sql);
 		try {
 			return fillParams(stmt, params).executeUpdate();
 		} finally {
 			stmt.close();
 		}
 	}
 
 	public static <T> T updateGenerateKey(Connection conn, String sql, Class<T> keyClass, Object... params)
 			throws SQLException {
 		logger.debug("{} ,params: {}", sql, params);
 		PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
 		try {
 			(params == null ? stmt : fillParams(stmt, params)).executeUpdate();
 			return FirstField.get(keyClass).handle(stmt.getGeneratedKeys());
 		} finally {
 			stmt.close();
 		}
 	}
 
 	public static int[] batch(Connection conn, String sql, Object[]... params) throws SQLException {
 		logger.debug("{} ,batchs: {}", sql, params.length);
 		PreparedStatement stmt = conn.prepareStatement(sql);
 		try {
 			for (Object[] array : params)
 				fillParams(stmt, array).addBatch();
 			return stmt.executeBatch();
 		} finally {
 			stmt.close();
 		}
 	}
 
 	public static <T> T mapQuery(Connection conn, NpSQL npsql, Handler<T> rsh, Map<String, ?> params)
 			throws SQLException {
 		SQLParams sql = npsql.sqlFromMap(params);
 		return query(conn, sql.sqlstr, rsh, sql.params);
 	}
 
 	public static int mapUpdate(Connection conn, NpSQL npsql, Map<String, Object> params) throws SQLException {
 		IDGenerator id;
 		SQLParams sql = npsql.sqlFromMap(params);
 		if (!npsql.insert || !(params instanceof EntityMap) || (id = ((EntityMap) params).getGenerator()) == null)
 			return update(conn, sql.sqlstr, sql.params);
 		Object primaryKey;
 		if (id.idGetter == null)
 			primaryKey = updateGenerateKey(conn, sql.sqlstr, id.type, sql.params);
 		else {
 			update(conn, sql.sqlstr, sql.params);
 			primaryKey = query(conn, id.idGetter, FirstField.get(id.type));
 		}
 		params.put(id.name, notNull(primaryKey, "null primaryKey"));
 		return 1;
 	}
 
 	public static int[] mapBatch(Connection conn, NpSQL npsql, Map<String, Object>... maps) throws SQLException {
 		int len = maps.length;
 		Object[][] params = new Object[len][];
 		while (--len >= 0)
 			params[len] = npsql.mapToParams(maps[len]);
 		return batch(conn, npsql.fullsql, params);
 	}
 
 	public static <T> T entityQuery(Connection conn, NpSQL npsql, Handler<T> rsh, Object entity) throws SQLException {
 		SQLParams sql = npsql.sqlFromBean(entity);
 		return query(conn, sql.sqlstr, rsh, sql.params);
 	}
 
 	public static int entityUpdate(Connection conn, NpSQL npsql, Object entity) throws SQLException {
 		IDGenerator id;
 		EntityMap<Accessor> map;
 		SQLParams sql = npsql.sqlFromBean(entity);
 		if (!npsql.insert || (id = (map = getAllAccessor(entity.getClass())).getGenerator()) == null)
 			return update(conn, sql.sqlstr, sql.params);
 		Accessor acs = map.get(id.name);
 		Object primaryKey;
 		if (id.idGetter == null)
			primaryKey = updateGenerateKey(conn, sql.sqlstr, id.type, sql.params);
 		else {
 			update(conn, sql.sqlstr, sql.params);
 			primaryKey = query(conn, id.idGetter, FirstField.get(id.type));
 		}
 		acs.setValue(entity, notNull(primaryKey, "null primaryKey"));
 		return 1;
 	}
 
 	public static int[] entityBatch(Connection conn, NpSQL npsql, Object... entitys) throws SQLException {
 		int len = entitys.length;
 		Object[][] params = new Object[len][];
 		while (--len >= 0)
 			params[len] = npsql.beanToParams(entitys[len]);
 		return batch(conn, npsql.fullsql, params);
 	}
 
 	private final DataSource ds;
 
 	public DBTool(DataSource datasource) {
 		this.ds = datasource;
 	}
 
 	Connection connect() throws SQLException {
 		return ds.getConnection();
 	}
 
 	/**
 	 * 判断表是否存在.
 	 * 
 	 * @param tableName
 	 *            the table name
 	 * 
 	 * @return true, if successful
 	 */
 	public final boolean tableExist(String tableName) throws SQLException {
 		Connection conn = connect();
 		try {
 			Statement stmt = conn.createStatement();
 			try {
 				return stmt.executeQuery("SELECT count(*) FROM " + tableName + " WHERE 1=0").next();
 			} catch (SQLException e) {
 				return false;
 			} finally {
 				stmt.close();
 			}
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 普通SQL查询.
 	 * 
 	 * @param sql
 	 *            标准SQL查询语句
 	 * @param rsh
 	 *            ResultSet 处理器
 	 * @param params
 	 *            查询语句参数
 	 * 
 	 * @return {@link Handler} 处理结果.
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final <T> T query(String sql, Handler<T> rsh, Object... params) throws SQLException {
 		Connection conn = connect();
 		try {
 			return query(conn, sql, rsh, params);
 		} finally {
 			conn.close();
 		}
 	}
 
 	public final <T> T query(String sql, Handler<T> rsh) throws SQLException {
 		return query(sql, rsh, (Object[]) null);
 	}
 
 	/**
 	 * 普通SQL执行.
 	 * 
 	 * @param sql
 	 *            标准SQL执行语句,an SQL INSERT, UPDATE or DELETE statement or an SQL
 	 *            statement that returns nothing.
 	 * @param params
 	 *            执行语句参数
 	 * 
 	 * @return either the row count for INSERT, UPDATE or DELETE statements, or
 	 *         0 for SQL statements that return nothing
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final int update(String sql, Object... params) throws SQLException {
 		Connection conn = connect();
 		try {
 			return update(conn, sql, params);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 普通SQL执行,返回自增字段值.
 	 * 
 	 * @param sql
 	 *            标准SQL执行语句,an SQL INSERT, UPDATE or DELETE statement or an SQL
 	 *            statement that returns nothing.
 	 * @param params
 	 *            执行语句参数
 	 * @param keyClass
 	 *            generated key class.
 	 * @return generated key
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 
 	public <T> T updateGenerateKey(String sql, Class<T> keyClass, Object... params) throws SQLException {
 		Connection conn = connect();
 		try {
 			return updateGenerateKey(conn, sql, keyClass, params);
 		} finally {
 			conn.close();
 		}
 	}
 
 	public final int update(String sql) throws SQLException {
 		return update(sql, (Object[]) null);
 	}
 
 	/**
 	 * 普通SQL批处理执行.
 	 * 
 	 * @param sql
 	 *            标准SQL执行语句
 	 * @param params
 	 *            执行语句参数数组
 	 * 
 	 * @return 每组参数对应的执行结果.
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final int[] batch(String sql, Object[]... params) throws SQLException {
 		Connection conn = connect();
 		try {
 			return batch(conn, sql, params);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 命名参数SQL查询. 命名参数SQL: 用 :name 代替 ? 的SQL.
 	 * 
 	 * @param npsql
 	 *            {@link NpSQL}
 	 * @param rsh
 	 *            ResultSet 处理器
 	 * @param params
 	 *            命名参数
 	 * 
 	 * @return {@link Handler} 处理结果.
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final <T> T mapQuery(NpSQL npsql, Handler<T> rsh, Map<String, ?> params) throws SQLException {
 		Connection conn = connect();
 		try {
 			return mapQuery(conn, npsql, rsh, params);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 命名参数SQL执行.insert 语句执行成功后.产生的主键值放回(put)参数paramsMap.
 	 * 
 	 * @param npsql
 	 *            {@link NpSQL}
 	 * @param params
 	 *            命名参数<br>
 	 *            凡是 entry 中的地值为 {@link Class}的当成null.<br>
 	 *            如果为 {@link EntityMap}, insert 语句执行成功后.产生的主键值放回(put)参数paramsMap<br>
 	 * @return row counts
 	 * @see #update(String, Object...)
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final int mapUpdate(NpSQL npsql, Map<String, Object> params) throws SQLException {
 		Connection conn = connect();
 		try {
 			return mapUpdate(conn, npsql, params);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 命名参数SQL批处理执行.
 	 * 
 	 * @param npsql
 	 *            {@link NpSQL}
 	 * @param maps
 	 *            命名参数数组。
 	 * @see #mapUpdate(NpSQL,Map).
 	 * 
 	 * @return 每组参数对应的执行结果.
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final int[] mapBatch(NpSQL npsql, Map<String, Object>... maps) throws SQLException {
 		Connection conn = connect();
 		try {
 			return mapBatch(conn, npsql, maps);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 命名参数SQL查询.用实体entity的属性作为参数
 	 * 
 	 * @param npsql
 	 *            {@link NpSQL}
 	 * @param rsh
 	 *            ResultSet 处理器
 	 * @param entity
 	 *            实体参数
 	 * 
 	 * @return {@link Handler} 处理结果.
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final <T> T entityQuery(NpSQL npsql, Handler<T> rsh, Object entity) throws SQLException {
 		Connection conn = connect();
 		try {
 			return entityQuery(conn, npsql, rsh, entity);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 命名参数SQL执行.用实体entity的属性作为参数.insert 语句 执行成功后.更新主键属性。
 	 * 
 	 * @param npsql
 	 *            {@link NpSQL}
 	 * @param entity
 	 *            实体参数.
 	 * 
 	 * @return row counts
 	 * @see #update(String, Object...)
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final int entityUpdate(NpSQL npsql, Object entity) throws SQLException {
 		Connection conn = connect();
 		try {
 			return entityUpdate(conn, npsql, entity);
 		} finally {
 			conn.close();
 		}
 	}
 
 	/**
 	 * 命名参数SQL批处理执行.用实体entity的属性作为参数
 	 * 
 	 * @param npsql
 	 *            {@link NpSQL}
 	 * @param entitys
 	 *            实体参数数组
 	 * 
 	 * @return 每组参数对应的执行结果.
 	 * 
 	 * @throws SQLException
 	 *             the SQL exception
 	 */
 	public final int[] entityBatch(NpSQL npsql, Object... entitys) throws SQLException {
 		Connection conn = connect();
 		try {
 			return entityBatch(conn, npsql, entitys);
 		} finally {
 			conn.close();
 		}
 	}
 
 	public <T> T execute(Callback<T> callback) throws SQLException {
 		ConnectSession session = new ConnectSession(ds);
 		try {
 			return callback.call(session);
 		} finally {
 			session.closePhysics();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public final <T> T createDao(Class<T> iface) {
 		return (T) newProxy(iface, new InvocationHandler() {
 			@Override
 			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 				return method.getDeclaringClass() == Object.class ? proxyHaEqTostr(proxy, method, args) : MethodDefine
 						.get(method).invoke(DBTool.this, method, args);
 			}
 		});
 	}
 }
