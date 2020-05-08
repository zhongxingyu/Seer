 package org.liveSense.api.sql;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.liveSense.api.beanprocessors.TestBean;
 import org.liveSense.api.beanprocessors.TestBean2;
 import org.liveSense.api.sql.SQLExecute.StatementType;
 import org.liveSense.misc.queryBuilder.QueryBuilder;
 import org.liveSense.misc.queryBuilder.SimpleSQLQueryBuilder;
 import org.liveSense.misc.queryBuilder.clauses.OrderByClause;
 import org.liveSense.misc.queryBuilder.criterias.BetweenCriteria;
 import org.liveSense.misc.queryBuilder.criterias.EqualCriteria;
 import org.liveSense.misc.queryBuilder.criterias.GreaterCriteria;
 import org.liveSense.misc.queryBuilder.jdbcDriver.JdbcDrivers;
 import org.liveSense.misc.queryBuilder.operands.OperandSource;
 import org.liveSense.misc.queryBuilder.operators.AndOperator;
 
 @Ignore
 public abstract class SQLExecuteBaseTest {
 
 
 	//CONSTS
 	private static final String STRING_UTF8 = "ÁRVÍZTŰRŐTÜKÖRFÚRÓGÉPárvíztűrőtükörfúrógépཤེལ་སྒོ་ཟ་ནས་ང་ན་གི་མ་རེདஇனிதாவதுהאקדמיה ללשון העבריь私はガラスを食べられます";
 	private static final String STRING_LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
 
 	
 	//FIELDS
 	protected BasicDataSource dataSource;
 	protected Connection connection;
 	protected Connection connection2;
 
 	
 	//METHODS - abstract
 	protected abstract void createTestProcedure() throws Exception;
 	
 		
 	//METHODS - protected
 	//helper
 	protected void executeSql(
 		Connection connection,
 		String sql)
 		throws SQLException {
 
 		Statement statement = connection.createStatement();
 		statement.execute(sql);
 	}
 	
 	protected void init(String blobTypeName)
 		throws SQLException {
 		
 		//drop table (if exists) trough direct JDBC access
 		ResultSet rs = connection.getMetaData().getTables(null, null, "BEANTEST1", null);
 		if (rs.next()) {
 			executeSql(connection, "drop table BEANTEST1");
 			connection.commit();
 		}
 		rs = connection.getMetaData().getTables(null, null, "BEANTEST2", null);
 		if (rs.next()) {
 			executeSql(connection, "drop table BEANTEST2");
 			connection.commit();
 		}
 		rs = connection.getMetaData().getTables(null, null, "T1", null);
 		if (rs.next()) {
 			executeSql(connection, "drop table T1");
 			connection.commit();
 		}
 		
 		//create table trough direct JDBC access
 		executeSql(connection, "create table BEANTEST1 (" +
 			"ID integer, " +
 			"ID_CUSTOMER integer, "+
 			"PASSWORD_ANNOTATED varchar(20), " +
 			"FOUR_PART_COLUMN_NAME integer, "+
 			"DATE_FIELD_WITHOUT_ANNOTATION date," +
 			"DATE_FIELD_WITH_ANNOTATION date," +
 			"BLOB_FIELD "+blobTypeName+", "+
 			"FLOAT_FIELD NUMERIC(15,2) )");
 		connection.commit();
 		executeSql(connection, "create table BEANTEST2 (" +
 			"ID integer, " +
 			"ID_CUSTOMER integer, "+
 			"PASSWORD_ANNOTATED varchar(20), " +
 			"FOUR_PART_COLUMN_NAME integer, "+
 			"DATE_FIELD_WITHOUT_ANNOTATION date," +
 			"DATE_FIELD_WITH_ANNOTATION date," +
 			"BLOB_FIELD "+blobTypeName+", "+
 			"FLOAT_FIELD NUMERIC(15,2) )");
 		connection.commit();
 		
 		
 		//insert records trough direct JDBC access
 		executeSql(
 			connection,
 			"INSERT INTO BEANTEST1(ID, ID_CUSTOMER, PASSWORD_ANNOTATED, FOUR_PART_COLUMN_NAME, DATE_FIELD_WITH_ANNOTATION, DATE_FIELD_WITHOUT_ANNOTATION, BLOB_FIELD, FLOAT_FIELD)" +
 			"VALUES (1, 1, 'password', 1, '2011-08-30', '2011-08-30', '" + STRING_UTF8 + "', 0.3)");
 		executeSql(
 			connection,
 			"INSERT INTO BEANTEST1(ID, ID_CUSTOMER, PASSWORD_ANNOTATED, FOUR_PART_COLUMN_NAME, DATE_FIELD_WITH_ANNOTATION, DATE_FIELD_WITHOUT_ANNOTATION, BLOB_FIELD, FLOAT_FIELD)" +
 			"VALUES (2, 3, 'password2', 0, '2011-08-31', '2011-08-31', '" + STRING_LOREM_IPSUM + "', 0.6)");
 		connection.commit();
 	}
 	
 	protected void disconnect() throws SQLException {
 		
 		connection.rollback();
 		connection2.rollback();
 		connection.close();
 		connection2.close();
 		dataSource.close();
 	}
 	
 	
 	//METHODS - private
 	//helper
 	
 	private void assertExec(
 		SQLExecute<TestBean> exec,
 		StatementType preparedType,
 		List<String> preparedNamedParameters,
 		int preparedSQLParamCount,
 		List<String> preparedFields,
 		String preparedSQL,
 		Map<String, Object> lastNamedParams,
 		List<Object> lastSQLParams) {
 		
 		//type
 		assertTrue(exec.getPreparedStatementType() == preparedType);
 		//class
 		if (preparedType == StatementType.PROCEDURE)
 			assertTrue(exec.getPreparedStatementClass() == null);
 		else
 			assertTrue(exec.getPreparedStatementClass() == TestBean.class);
 		//connection
 		assertTrue(exec.getPreparedConnection() == connection);
 		//namedParameter
 		if (preparedNamedParameters != null) {
 			List<String> pnp = new ArrayList<String>(preparedNamedParameters);
 			assertTrue(pnp.size() == exec.getPreparedNamedParameters().size());
 			for (String paramName : exec.getPreparedNamedParameters()) {
 				pnp.remove(paramName);
 			}
 			assertTrue(pnp.size() == 0);
 		} 
 		else assertTrue(exec.getPreparedNamedParameters().size() == 0);
 		//paramCount
 		assertTrue(exec.getPreparedSQLParametersCount() == preparedSQLParamCount);
 		//fields
 		if (preparedFields != null) {
 			List<String> pf = new ArrayList<String>(preparedFields);
 			assertTrue(pf.size() == exec.getPreparedFields().size());
 			for (String fieldName : exec.getPreparedFields()) {
 				pf.remove(fieldName);
 			}
 			assertTrue(pf.size() == 0);
 		}
 		else assertTrue(exec.getPreparedFields().size() == 0);
 		//sql
 		assertTrue(exec.getPreparedSQL().equals(preparedSQL));
 		
 		
 		//lastNamedParameters
 		if (lastNamedParams != null) {
 			assertTrue(exec.getPreparedNamedParameters().size() == lastNamedParams.size());
 			
 			Map<String, Object> lnp = new HashMap<String, Object>(lastNamedParams);
 			assertTrue(lnp.size() == exec.getLastNamedParameters().size());
 			for (Entry<String, Object> entry : exec.getLastNamedParameters().entrySet()) {
 				assertTrue(lnp.get(entry.getKey()).equals(entry.getValue()));
 				lnp.remove(entry.getKey());
 			}
 			assertTrue(lnp.size() == 0);
 		}
 		else assertTrue(exec.getLastNamedParameters().size() == 0);		
 		//lastSQLParameters
 		if (lastSQLParams != null) {
 			assertTrue(exec.getPreparedSQLParametersCount() == lastSQLParams.size());
 			
 			List<Object> lp = new ArrayList<Object>(lastSQLParams);
 			assertTrue(lp.size() == exec.getLastSQLParameters().size());
 			for (Object paramValue : exec.getLastSQLParameters()) {
 				lp.remove(paramValue);
 			}
 			assertTrue(lp.size() == 0);
 		}
 		else assertTrue(exec.getLastSQLParameters().size() == 0);
 	}
 		
 	private void assertTestBeanID1(TestBean bean, Double val) throws ParseException {
 		assertTrue(bean.getId() == 1);
 		assertTrue(bean.getIdCustomer() == 1);
		assertTrue("password".equals(bean.getConfirmationPassword()));
 		assertTrue(bean.getFourPartColumnName());
 		assertTrue(bean.getDateFieldWithAnnotation().equals(new SimpleDateFormat("yyyy.MM.dd").parse("2011.08.30")));
 		assertTrue(bean.getBlob().equals(STRING_UTF8));
 		assertTrue(bean.getFloatField().equals(val));
 	}
 	
 	private void assertTestBeanID2(TestBean bean, Double val) throws ParseException {
 		assertTrue(bean.getId() == 2);
 		assertTrue(bean.getIdCustomer() == 3);
		assertTrue("password2".equals(bean.getConfirmationPassword()));
 		assertTrue(!bean.getFourPartColumnName());
 		assertTrue(bean.getDateFieldWithAnnotation().equals(new SimpleDateFormat("yyyy.MM.dd").parse("2011.08.31")));
 		assertTrue(bean.getBlob().equals(STRING_LOREM_IPSUM));
 		assertTrue(bean.getFloatField().equals(val));
 	}
 	
 	private void assertTestBeanID3(TestBean bean) throws ParseException {
 		assertTrue(bean.getId() == 3);
 		assertTrue(bean.getIdCustomer() == 4);
 		assertTrue(bean.getConfirmationPassword().equals("foo"));
 		assertTrue(!bean.getFourPartColumnName());
 		assertTrue(bean.getDateFieldWithAnnotation().equals(new SimpleDateFormat("yyyy.MM.dd").parse("2011.09.01")));
 		assertTrue(bean.getBlob().equals(StringUtils.repeat(STRING_UTF8+STRING_LOREM_IPSUM, 1000)));
 		assertTrue(bean.getFloatField() == 0.7);
 	}
 	
 	//TESTS
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareQueryMulti()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		
 		//tested method
 		exec.prepareQueryStatement(connection, builder);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			null,
 			null);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedQueryMulti()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		exec.prepareQueryStatement(connection, builder);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", new Integer(1));
 		
 		//tested method
 		List<TestBean> beans = exec.queryEntitiesWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			new HashMap<String, Object>() {{put("id", new Integer(1));}},
 			new ArrayList<Object>() {{add(new Integer(1));}});
 		assertTrue(beans.size() == 1);
 		assertTestBeanID1(beans.get(0), 0.3);
 		
 		//tested method
 		params.put("id", new Integer(2));
 		beans = exec.queryEntitiesWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			new HashMap<String, Object>() {{put("id", new Integer(2));}},
 			new ArrayList<Object>() {{add(new Integer(2));}});
 		assertTrue(beans.size() == 1);
 		assertTestBeanID2(beans.get(0), 0.6);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunQueryMulti()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", new Integer(1));
 		
 		//tested method
 		List<TestBean> beans = exec.queryEntities(connection, builder, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			params,
 			new ArrayList<Object>() {{add(new Integer(1));}});
 		assertTrue(beans.size() == 1);
 		assertTestBeanID1(beans.get(0), 0.3);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareQuerySingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.prepareQueryStatement(connection);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("ID");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			null,
 			null);
 	}
 		
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedQuerySingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		exec.prepareQueryStatement(connection);
 		TestBean bean = new TestBean();
 		bean.setId(1);
 		
 		//tested method
 		TestBean res = exec.queryEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("ID");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			new HashMap<String, Object>() {{put("ID", new Integer(1));}},
 			new ArrayList<Object>() {{add(new Integer(1));}});
 		assertTrue(res != null);
 		assertTestBeanID1(res, 0.3);
 		
 		//tested method
 		bean.setId(2);
 		res = exec.queryEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			new ArrayList<String>() {{ add("ID");}},
 			1,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=?)",
 			new HashMap<String, Object>() {{put("ID", new Integer(2));}},
 			new ArrayList<Object>() {{add(new Integer(2));}});
 		assertTrue(res != null);
 		assertTestBeanID2(res, 0.6);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked" })
 	public void testRunQuerySingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		TestBean bean = new TestBean();
 		bean.setId(1);
 		
 		//tested method
 		TestBean bean2 = exec.queryEntity(connection, bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.SELECT,
 			null,
 			0,
 			null,
 			"SELECT * FROM (SELECT * FROM BeanTest1 )  WHERE (ID=1)",
 			null,
 			null);
 		assertTestBeanID1(bean2, 0.3);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareLockMulti()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		
 		//tested method
 		exec.prepareLockStatement(connection, builder);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			null,
 			null);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedLockMulti()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		exec.prepareLockStatement(connection, builder);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", new Integer(1));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		exec2.prepareLockStatement(connection2, builder);
 		
 		//tested method
 		List<TestBean> beans = exec.lockEntitiesWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			new HashMap<String, Object>() {{ put("id", new Integer(1));}},
 			new ArrayList<Object>() {{ add(new Integer(1));}});
 		assertTrue(beans.size() == 1);
 		assertTestBeanID1(beans.get(0), 0.3);
 		
 		try {
 			exec2.lockEntitiesWithPreparedStatement(params);
 			assertTrue(false);
 		}
 		catch (SQLException e) {
 			if (dataSource.getDriverClassName().equals(JdbcDrivers.FIREBIRD.getDriverClass())) {
 				assertTrue(e.getErrorCode() == 335544345);
 			} else throw e;
 		}
 		
 		//tested method
 		params.put("id", new Integer(2));
 		beans = exec.lockEntitiesWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			new HashMap<String, Object>() {{ put("id", new Integer(2));}},
 			new ArrayList<Object>() {{ add(new Integer(2));}});
 		assertTrue(beans.size() == 1);
 		assertTestBeanID2(beans.get(0), 0.6);
 		
 		try {
 			exec2.lockEntitiesWithPreparedStatement(params);
 			assertTrue(false);
 		}
 		catch (SQLException e) {
 			if (dataSource.getDriverClassName().equals(JdbcDrivers.FIREBIRD.getDriverClass())) {
 				assertTrue(e.getErrorCode() == 335544345);
 			} else throw e;
 		}
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunLockMulti()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", new Integer(1));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		List<TestBean> beans = exec.lockEntities(connection, builder, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("id");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			new HashMap<String, Object>() {{ put("id", new Integer(1));}},
 			new ArrayList<Object>() {{add(new Integer(1));}});
 		assertTrue(beans.size() == 1);
 		assertTestBeanID1(beans.get(0), 0.3);
 		
 		try {
 			exec2.lockEntities(connection2, builder, params);
 			assertTrue(false);
 		}
 		catch (SQLException e) {
 			if (dataSource.getDriverClassName().equals(JdbcDrivers.FIREBIRD.getDriverClass())) {
 				assertTrue(e.getErrorCode() == 335544345);
 			} else throw e;
 		}
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareLockSingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.prepareLockStatement(connection);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("ID");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			null,
 			null);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedLockSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SimpleSQLQueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>("id", new OperandSource("", ":id", false))));
 		exec.prepareLockStatement(connection);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("id", new Integer(1));
 		TestBean bean = new TestBean();
 		bean.setId(1);
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		exec2.prepareLockStatement(connection2, builder);
 		
 		//tested method
 		TestBean res = exec.lockEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("ID");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			new HashMap<String, Object>() {{put("ID", new Integer(1));}},
 			new ArrayList<Object>() {{add(new Integer(1));}});
 		assertTrue(res != null);
 		assertTestBeanID1(res, 0.3);
 		
 		try {
 			exec2.lockEntitiesWithPreparedStatement(params);
 			assertTrue(false);
 		}
 		catch (SQLException e) {
 			if (dataSource.getDriverClassName().equals(JdbcDrivers.FIREBIRD.getDriverClass())) {
 				assertTrue(e.getErrorCode() == 335544345);
 			} else throw e;
 		}
 		
 		//tested method
 		params.put("id", new Integer(2));
 		bean.setId(2);
 		res = exec.lockEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			new ArrayList<String>() {{ add("ID");}},
 			1,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=?) WITH LOCK",
 			new HashMap<String, Object>() {{put("ID", new Integer(2));}},
 			new ArrayList<Object>() {{add(new Integer(2));}});
 		assertTrue(res != null);
 		assertTestBeanID2(res, 0.6);
 		
 		try {
 			exec2.lockEntitiesWithPreparedStatement(params);
 			assertTrue(false);
 		}
 		catch (SQLException e) {
 			if (dataSource.getDriverClassName().equals(JdbcDrivers.FIREBIRD.getDriverClass())) {
 				assertTrue(e.getErrorCode() == 335544345);
 			} else throw e;
 		}
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testRunLockSingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		TestBean bean = new TestBean();
 		bean.setId(1);
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		TestBean bean2 = exec.lockEntity(connection, bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.LOCK,
 			null,
 			0,
 			null,
 			"SELECT * FROM BeanTest1  WHERE (ID=1) WITH LOCK",
 			null,
 			null);
 		assertTestBeanID1(bean2, 0.3);
 		
 		try {
 			exec2.lockEntity(connection2, bean);
 			assertTrue(false);
 		}
 		catch (SQLException e) {
 			if (dataSource.getDriverClassName().equals(JdbcDrivers.FIREBIRD.getDriverClass())) {
 				assertTrue(e.getErrorCode() == 335544345);
 			} else throw e;
 		}
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareInsertSingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.prepareInsertStatement(connection);
 		//tests
 		assertExec(
 			exec,
 			StatementType.INSERT,
 			null,
 			7,
 			new ArrayList<String>() {{     
 				add("id"); 
 				add("idCustomer");
 				add("confirmationPassword");
 				add("fourPartColumnName");
 				add("blob");				
 				add("floatField");
 				add("dateFieldWithAnnotation"); }},
 			"INSERT INTO BeanTest1 (BLOB_FIELD,ID,PASSWORD_ANNOTATED,FLOAT_FIELD,ID_CUSTOMER,DATE_FIELD_WITH_ANNOTATION,FOUR_PART_COLUMN_NAME) VALUES (?,?,?,?,?,?,?)",
 			null,
 			null);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedInsertSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		final TestBean bean = new TestBean();
 		bean.setId(3);
 		bean.setIdCustomer(4);
 		bean.setConfirmationPassword("foo");
 		bean.setFourPartColumnName(false);
 		bean.setDateFieldWithAnnotation(new SimpleDateFormat("yyyy.MM.dd").parse("2011.09.01"));
 		bean.setBlob(StringUtils.repeat(STRING_UTF8+STRING_LOREM_IPSUM, 1000));
 		bean.setFloatField(0.7);
 		exec.prepareInsertStatement(connection);
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.insertEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.INSERT,
 			null,
 			7,
 			new ArrayList<String>() {{
 				add("id"); 
 				add("idCustomer");
 				add("confirmationPassword");
 				add("fourPartColumnName");
 				add("blob");				
 				add("floatField");
 				add("dateFieldWithAnnotation"); }},
 				"INSERT INTO BeanTest1 (BLOB_FIELD,ID,PASSWORD_ANNOTATED,FLOAT_FIELD,ID_CUSTOMER,DATE_FIELD_WITH_ANNOTATION,FOUR_PART_COLUMN_NAME) VALUES (?,?,?,?,?,?,?)",
 			null,
 			new ArrayList<Object>() {{ 
 				add(bean.getFloatField()); 
 				add(bean.getFourPartColumnName()); 
 				add(bean.getConfirmationPassword()); 
 				add(bean.getDateFieldWithAnnotation()); 
 				add(bean.getId()); 
 				add(bean.getIdCustomer());
 				add(bean.getBlob());}}
 		);
 		assertTestBeanID3(exec2.queryEntity(connection, bean));
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunInsertSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		final TestBean bean = new TestBean();
 		bean.setId(3);
 		bean.setIdCustomer(4);
 		bean.setConfirmationPassword("foo");
 		bean.setFourPartColumnName(false);
 		bean.setDateFieldWithAnnotation(new SimpleDateFormat("yyyy.MM.dd").parse("2011.09.01"));
 		bean.setBlob(StringUtils.repeat(STRING_UTF8+STRING_LOREM_IPSUM, 1000));
 		bean.setFloatField(0.7);
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 				
 		//tested method
 		exec.insertEntity(connection, bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.INSERT,
 			null,
 			7,
 			new ArrayList<String>() {{
 				add("id"); 
 				add("idCustomer");
 				add("confirmationPassword");
 				add("fourPartColumnName");
 				add("blob");				
 				add("floatField");
 				add("dateFieldWithAnnotation"); }},
 				"INSERT INTO BeanTest1 (BLOB_FIELD,ID,PASSWORD_ANNOTATED,FLOAT_FIELD,ID_CUSTOMER,DATE_FIELD_WITH_ANNOTATION,FOUR_PART_COLUMN_NAME) VALUES (?,?,?,?,?,?,?)",
 			null,
 			new ArrayList<Object>() {{ 
 				add(bean.getFloatField()); 
 				add(bean.getFourPartColumnName()); 
 				add(bean.getConfirmationPassword()); 
 				add(bean.getDateFieldWithAnnotation()); 
 				add(bean.getId()); 
 				add(bean.getIdCustomer());
 				add(bean.getBlob());}}
 		);
 		assertTestBeanID3(exec2.queryEntity(connection, bean));
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareUpdateMulti()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		Object condition = new AndOperator(new GreaterCriteria<OperandSource>("t", "id", new OperandSource("", ":id", false)));
 		
 		//tested method
 		exec.prepareUpdateStatement(connection, null, "t", Arrays.asList(new String[]{"floatField"}), condition);
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("id");}} ,			
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1 t SET t.FLOAT_FIELD = ? WHERE (t.ID>?)",
 			null,
 			null);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedUpdateMulti()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		Object condition = new AndOperator(new GreaterCriteria<OperandSource>("t", "floatField", new OperandSource("", ":floatfield", false)));
 		exec.prepareUpdateStatement(connection, null, "t", Arrays.asList(new String[]{"floatField"}), condition);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("floatfield", new Float(0.5));
 		TestBean bean = new TestBean();
 		bean.setFloatField(1.0);
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.updateEntitiesWithPreparedStatement(bean, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("floatfield"); }},
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1 t SET t.FLOAT_FIELD = ? WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.5)); }},
 			new ArrayList<Object>() {{
 				add(new Double(1.0));
 				add(new Float(0.5)); }}
 		);
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 1);
 		assertTestBeanID2(beans.get(0), 1.0);
 		
 		//tested method
 		params.put("floatfield", new Float(0.0));
 		bean.setFloatField(2.0);
 		exec.updateEntitiesWithPreparedStatement(bean, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("floatfield"); }},
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1 t SET t.FLOAT_FIELD = ? WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.0)); }},
 			new ArrayList<Object>() {{
 				add(new Double(2.0));
 				add(new Float(0.0)); }}
 		);
 		
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(2.0))));
 		builder2.setOrderBy(new OrderByClause("id", false));
 		beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 2);
 		assertTestBeanID1(beans.get(0), 2.0);
 		assertTestBeanID2(beans.get(1), 2.0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunUpdateMulti()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		Object condition = new AndOperator(new GreaterCriteria<OperandSource>("t", "floatField", new OperandSource("", ":floatfield", false)));
 		exec.prepareUpdateStatement(connection, null, "t", Arrays.asList(new String[]{"floatField"}), condition);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("floatfield", new Float(0.5));
 		TestBean bean = new TestBean();
 		bean.setFloatField(1.0);
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.updateEntities(connection, bean, "t", Arrays.asList(new String[]{"floatField"}), condition, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("floatfield"); }},
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1 t SET t.FLOAT_FIELD = ? WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.5)); }},
 			new ArrayList<Object>() {{
 				add(new Double(1.0));
 				add(new Float(0.5)); }}
 		);
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 1);
 		assertTestBeanID2(beans.get(0), 1.0);
 		
 		//tested method
 		params.put("floatfield", new Float(0.0));
 		bean.setFloatField(2.0);
 		exec.updateEntities(connection, bean, "t", Arrays.asList(new String[]{"floatField"}), condition, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("floatfield"); }},
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1 t SET t.FLOAT_FIELD = ? WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.0)); }},
 			new ArrayList<Object>() {{
 				add(new Double(2.0));
 				add(new Float(0.0)); }}
 		);
 		
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(2.0))));
 		builder2.setOrderBy(new OrderByClause("id", false));
 		beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 2);
 		assertTestBeanID1(beans.get(0), 2.0);
 		assertTestBeanID2(beans.get(1), 2.0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareUpdateSingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 			
 		//tested method
 		exec.prepareUpdateStatement(connection, TestBean.class, Arrays.asList(new String[]{"floatField"}));
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("ID"); }},
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1  SET FLOAT_FIELD = ? WHERE (ID=?)",
 			null,
 			null);
 	}
 
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedUpdateSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		exec.prepareUpdateStatement(connection, TestBean.class, Arrays.asList(new String[]{"floatField"}));
 		TestBean bean = new TestBean();
 		bean.setId(2);
 		bean.setFloatField(1.0);
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.updateEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			new ArrayList<String>() {{ add("ID"); }},
 			2,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1  SET FLOAT_FIELD = ? WHERE (ID=?)",
 			new HashMap<String, Object>() {{ put("ID", new Integer(2)); }},
 			new ArrayList<Object>() {{
 				add(new Double(1.0));
 				add(new Integer(2)); }}
 		);
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 1);
 		assertTestBeanID2(beans.get(0), 1.0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunUpdateSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		TestBean bean = new TestBean();
 		bean.setId(2);
 		bean.setFloatField(1.0);
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.updateEntity(connection, bean, Arrays.asList(new String[]{"floatField"}));
 		//tests
 		assertExec(
 			exec,
 			StatementType.UPDATE,
 			null,
 			1,
 			new ArrayList<String>() {{ add("floatField"); }},
 			"UPDATE BeanTest1  SET FLOAT_FIELD = ? WHERE (ID=2)",
 			null,
 			new ArrayList<Object>() {{ add(new Double(1.0)); }}
 		);
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 1);
 		assertTestBeanID2(beans.get(0), 1.0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareDeleteMulti()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		Object condition = new AndOperator(new GreaterCriteria<OperandSource>("t", "floatField", new OperandSource("", ":floatfield", false)));
 		
 		//tested method
 		exec.prepareDeleteStatement(connection, null, "t", condition);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("floatfield");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1 t  WHERE (t.FLOAT_FIELD>?)",
 			null,
 			null);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedDeleteMulti()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		Object condition = new AndOperator(new GreaterCriteria<OperandSource>("t", "floatField", new OperandSource("", ":floatfield", false)));
 		exec.prepareDeleteStatement(connection, null, "t", condition);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("floatfield", new Float(0.5));
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.deleteEntitiesWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("floatfield");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1 t  WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.5)); }},
 			new ArrayList<Object>() {{ add(new Float(0.5)); }});
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 0);
 		
 		//tested method
 		params.put("floatfield", new Float(0.0));
 		exec.deleteEntitiesWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("floatfield");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1 t  WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.0)); }},
 			new ArrayList<Object>() {{ add(new Float(0.0)); }});
 		
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(2.0))));
 		builder2.setOrderBy(new OrderByClause("id", false));
 		beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunDeleteMulti()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		Object condition = new AndOperator(new GreaterCriteria<OperandSource>("t", "floatField", new OperandSource("", ":floatfield", false)));
 		exec.prepareDeleteStatement(connection, null, "t", condition);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("floatfield", new Float(0.5));
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.deleteEntities(connection, null, "t", condition, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("floatfield");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1 t  WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.5)); }},
 			new ArrayList<Object>() {{ add(new Float(0.5)); }});
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 0);
 		
 		//tested method
 		params.put("floatfield", new Float(0.0));
 		exec.deleteEntities(connection, null, "t", condition, params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("floatfield");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1 t  WHERE (t.FLOAT_FIELD>?)",
 			new HashMap<String, Object>() {{ put("floatfield", new Float(0.0)); }},
 			new ArrayList<Object>() {{ add(new Float(0.0)); }});
 		assertTrue(exec.getPreparedSQL().equals("DELETE FROM BeanTest1 t  WHERE (t.FLOAT_FIELD>?)"));
 		
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(2.0))));
 		builder2.setOrderBy(new OrderByClause("id", false));
 		beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareDeleteSingle()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 			
 		//tested method
 		exec.prepareDeleteStatement(connection);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("ID");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1   WHERE (ID=?)",
 			null,
 			null);
 	}
 
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedDeleteSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		exec.prepareDeleteStatement(connection);
 		TestBean bean = new TestBean();
 		bean.setId(2);
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.deleteEntityWithPreparedStatement(bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			new ArrayList<String>() {{ add("ID");}} ,			
 			1,
 			null,
 			"DELETE FROM BeanTest1   WHERE (ID=?)",
 			new HashMap<String, Object>() {{ put("ID", new Integer(2)); }},
 			new ArrayList<Object>() {{ add(new Integer(2)); }});
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 0);
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testRunDeleteSingle()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		TestBean bean = new TestBean();
 		bean.setId(2);
 		SimpleBeanSQLQueryBuilder builder2 = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		builder2.setWhere(new AndOperator(new EqualCriteria<OperandSource>("floatField", new OperandSource(1.0))));
 		
 		SQLExecute<TestBean> exec2 = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		
 		//tested method
 		exec.deleteEntity(connection, bean);
 		//tests
 		assertExec(
 			exec,
 			StatementType.DELETE,
 			null ,			
 			0,
 			null,
 			"DELETE FROM BeanTest1   WHERE (ID=2)",
 			null,
 			null);
 		
 		List<TestBean> beans = exec2.queryEntities(connection, builder2);
 		assertTrue(beans.size() == 0);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testPrepareInsertSelect()
 		throws Exception {
 	
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 				
 		//tested method
 		exec.prepareInsertSelectStatement(connection,
 			TestBean2.class, new String[] {"id", "idCustomer"},
 			TestBean.class, "", new String[] {"id", "idCustomer"}, new AndOperator(new BetweenCriteria<OperandSource>("id", new OperandSource("",":from", false), new OperandSource("",":to", false))));
 		//tests
 		assertExec(
 			exec,
 			StatementType.INSERT_SELECT,
 			new ArrayList<String>() {{ 
 				add("from"); 
 				add("to"); }} ,			
 			2,
 			null,
 			"INSERT INTO BeanTest2 (ID,ID_CUSTOMER)\nSELECT ID,ID_CUSTOMER FROM (SELECT ID,ID_CUSTOMER FROM BeanTest1 )  WHERE (ID BETWEEN ? AND ?)",
 			null,
 			null);	
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunPreparedInsertSelect()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		exec.prepareInsertSelectStatement(connection,
 			TestBean2.class, new String[] {"id", "idCustomer", "confirmationPassword", "fourPartColumnName", "blob", "floatField", "dateFieldWithAnnotation"},
 			TestBean.class, "", new String[] {"id", "idCustomer", "confirmationPassword", "fourPartColumnName", "blob", "floatField", "dateFieldWithAnnotation"}, new AndOperator(new BetweenCriteria<OperandSource>("id", new OperandSource("",":from", false), new OperandSource("",":to", false))));
 		SQLExecute<TestBean2> exec2 = (SQLExecute<TestBean2>) SQLExecute.getExecuterByDataSource(dataSource, TestBean2.class);
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(TestBean2.class);
 		builder.setOrderBy(new OrderByClause("id", false));
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("from", new Integer(1));
 		params.put("to", new Integer(2));
 				
 		//tested method
 		exec.insertSelectWithPreparedStatement(params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.INSERT_SELECT,
 			new ArrayList<String>() {{ add("from"); add("to"); }} ,			
 			2,
 			null,
 			"INSERT INTO BeanTest2 (ID,ID_CUSTOMER,PASSWORD_ANNOTATED,FOUR_PART_COLUMN_NAME,BLOB_FIELD,FLOAT_FIELD,DATE_FIELD_WITH_ANNOTATION)\nSELECT ID,ID_CUSTOMER,PASSWORD_ANNOTATED,FOUR_PART_COLUMN_NAME,BLOB_FIELD,FLOAT_FIELD,DATE_FIELD_WITH_ANNOTATION FROM (SELECT ID,ID_CUSTOMER,PASSWORD_ANNOTATED,FOUR_PART_COLUMN_NAME,BLOB_FIELD,FLOAT_FIELD,DATE_FIELD_WITH_ANNOTATION FROM BeanTest1 )  WHERE (ID BETWEEN ? AND ?)",
 			new HashMap<String, Object>() {{ 
 				put("from", new Integer(1)); 
 				put("to", new Integer(2)); }},
 			new ArrayList<Object>() {{
 				add(new Integer(1));
 				add(new Integer(2)); }}
 		);	
 				
 		List<TestBean2> beans = exec2.queryEntities(connection, builder);
 		assertTrue(beans.size() == 2);
 		assertTestBeanID1(beans.get(0), 0.3);
 		assertTestBeanID2(beans.get(1), 0.6);
 	}
 	
 	@Test
 	@SuppressWarnings({ "unchecked", "serial" })
 	public void testRunInsertSelect()
 		throws Exception {
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		SQLExecute<TestBean2> exec2 = (SQLExecute<TestBean2>) SQLExecute.getExecuterByDataSource(dataSource, TestBean2.class);
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(TestBean2.class);
 		builder.setOrderBy(new OrderByClause("id", false));
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("from", new Integer(1));
 		params.put("to", new Integer(2));
 				
 		//tested method
 		exec.insertSelect(connection,
 			TestBean2.class, new String[] {"id", "idCustomer", "confirmationPassword", "fourPartColumnName", "blob", "floatField", "dateFieldWithAnnotation"},
 			TestBean.class, "", new String[] {"id", "idCustomer", "confirmationPassword", "fourPartColumnName", "blob", "floatField", "dateFieldWithAnnotation"}, new AndOperator(new BetweenCriteria<OperandSource>("id", new OperandSource("",":from", false), new OperandSource("",":to", false))), params);
 		//tests
 		assertExec(
 			exec,
 			StatementType.INSERT_SELECT,
 			new ArrayList<String>() {{ add("from"); add("to"); }} ,			
 			2,
 			null,
 			"INSERT INTO BeanTest2 (ID,ID_CUSTOMER,PASSWORD_ANNOTATED,FOUR_PART_COLUMN_NAME,BLOB_FIELD,FLOAT_FIELD,DATE_FIELD_WITH_ANNOTATION)\nSELECT ID,ID_CUSTOMER,PASSWORD_ANNOTATED,FOUR_PART_COLUMN_NAME,BLOB_FIELD,FLOAT_FIELD,DATE_FIELD_WITH_ANNOTATION FROM (SELECT ID,ID_CUSTOMER,PASSWORD_ANNOTATED,FOUR_PART_COLUMN_NAME,BLOB_FIELD,FLOAT_FIELD,DATE_FIELD_WITH_ANNOTATION FROM BeanTest1 )  WHERE (ID BETWEEN ? AND ?)",
 			new HashMap<String, Object>() {{ 
 				put("from", new Integer(1)); 
 				put("to", new Integer(2)); }},
 			new ArrayList<Object>() {{
 				add(new Integer(1));
 				add(new Integer(2)); }}
 		);
 				
 		List<TestBean2> beans = exec2.queryEntities(connection, builder);
 		assertTrue(beans.size() == 2);
 		assertTestBeanID1(beans.get(0), 0.3);
 		assertTestBeanID2(beans.get(1), 0.6);
 	}
 
 	@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
 	@Test
 	public void testPrepareExecuteProcedure()
 		throws Exception {
 		
 		//prepare
 		createTestProcedure();
 		SQLExecute exec = SQLExecute.getExecuterByDataSource(dataSource);
 		
 		//tested method
 		exec.prepareExecuteProcedure(connection, "testproc");
 		
 		//tests
 		assertExec(
 			exec,
 			StatementType.PROCEDURE,
 			new ArrayList<String>() {{ add("PARAM1"); add("PARAM2"); }} ,			
 			2,
 			null,
 			"EXECUTE PROCEDURE testproc(?,?)",
 			null,
 			null);
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
 	@Test
 	public void testRunPreparedExecuteProcedure()
 		throws Exception {
 		
 		//prepare
 		createTestProcedure();
 		SQLExecute exec = SQLExecute.getExecuterByDataSource(dataSource);
 		exec.prepareExecuteProcedure(connection, "testproc");
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("PARAM1", "10.5");
 		params.put("param2", 20);
 		
 		//tested method
 		Map<String, Object> res = exec.executeProcedureWithPreparedStatement(params);
 		
 		//tests
 		assertExec(
 			exec,
 			StatementType.PROCEDURE,
 			new ArrayList<String>() {{ add("PARAM1"); add("PARAM2"); }} ,			
 			2,
 			null,
 			"EXECUTE PROCEDURE testproc(?,?)",
 			new HashMap<String, Object>() {{ 
 				put("PARAM1", "10.5"); 
 				put("param2", new Integer(20)); }},
 			new ArrayList<Object>() {{
 				add("10.5");
 				add(new Integer(20)); }}
 		);
 		
 		assertTrue(res.size() == 1);
 		assertTrue(res.get("PARAM3").equals(new BigDecimal(new BigInteger("3050"), 2)));
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
 	@Test
 	public void testRunExecuteProcedure()
 		throws Exception {
 		
 		//prepare
 		createTestProcedure();
 		SQLExecute exec = SQLExecute.getExecuterByDataSource(dataSource);
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("PARAM1", "10.5");
 		params.put("param2", 20);
 		
 		//tested method
 		Map<String, Object> res = exec.executeProcedure(connection, "testproc", params);
 		
 		//tests
 		assertExec(
 			exec,
 			StatementType.PROCEDURE,
 			new ArrayList<String>() {{ add("PARAM1"); add("PARAM2"); }} ,			
 			2,
 			null,
 			"EXECUTE PROCEDURE testproc(?,?)",
 			new HashMap<String, Object>() {{ 
 				put("PARAM1", "10.5"); 
 				put("param2", new Integer(20)); }},
 			new ArrayList<Object>() {{
 				add("10.5");
 				add(new Integer(20)); }}
 		);
 		
 		assertTrue(res.size() == 1);
 		assertTrue(res.get("PARAM3").equals(new BigDecimal(new BigInteger("3050"), 2)));
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testQueryToMap()
 		throws Exception {
 
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(TestBean.class);
 		
 		//tested method
 		List<Map<String, ?>> resMap = exec.queryEntitiesAsMap(connection, "", builder, null);
 		
 		//tests
 		assertTrue(resMap.size() == 2);
 				
 		assertTrue((Integer)resMap.get(0).get("ID") == 1);
 		assertTrue((Integer)resMap.get(0).get("ID_CUSTOMER") == 1);
 		assertTrue(resMap.get(0).get("PASSWORD_ANNOTATED").equals("password"));
 		assertTrue((Integer)resMap.get(0).get("FOUR_PART_COLUMN_NAME") == 1);
 		assertTrue(resMap.get(0).get("BLOB_FIELD").equals(STRING_UTF8));
 		assertTrue(resMap.get(0).get("DATE_FIELD_WITH_ANNOTATION").equals(new SimpleDateFormat("yyyy.MM.dd").parse("2011.08.30")));
 		assertTrue((Double)resMap.get(0).get("FLOAT_FIELD") == 0.3);
 		
 		assertTrue((Integer)resMap.get(1).get("ID") == 2);
 		assertTrue((Integer)resMap.get(1).get("ID_CUSTOMER") == 3);
 		assertTrue(resMap.get(1).get("PASSWORD_ANNOTATED").equals("password2"));
 		assertTrue((Integer)resMap.get(1).get("FOUR_PART_COLUMN_NAME") == 0);
 		assertTrue(resMap.get(1).get("BLOB_FIELD").equals(STRING_LOREM_IPSUM));
 		assertTrue(resMap.get(1).get("DATE_FIELD_WITH_ANNOTATION").equals(new SimpleDateFormat("yyyy.MM.dd").parse("2011.08.31")));
 		assertTrue((Double)resMap.get(1).get("FLOAT_FIELD") == 0.6);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testExecuteScript()
 		throws org.liveSense.api.sql.exceptions.SQLException, SQLException{
 		
 		//prepare
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 
 		//tested method
 		exec.executeScript(connection, new File("./target/test-classes/test.sql"), "Create");
 		//tests
 		ResultSet rs = connection.getMetaData().getTables(null, null, "T1", null);
 		assertTrue(rs.next());
 		connection.commit();
 		connection.close();
 		connection = dataSource.getConnection();
 				
 		//tested method
 		exec.executeScript(connection, new File("./target/test-classes/test.sql"), "Insert");
 		connection.commit();
 		//tests
 		PreparedStatement stm = connection.prepareStatement("SELECT COUNT(0) FROM t1");
 		rs = stm.executeQuery();
 		assertTrue(rs.next());
 		assertTrue(rs.getInt(1) == 3);
 		connection.commit();
 		connection.close();
 		connection = dataSource.getConnection();
 		
 		//tested method
 		exec.executeScript(connection, new File("./target/test-classes/test.sql"), "Drop");
 		//tests
 		rs = connection.getMetaData().getTables(null, null, "T1", null);
 		assertTrue(!rs.next());
 		connection.commit();
 		connection.close();
 		connection = dataSource.getConnection();
 		
 		//tested method - execute script with different separator
 		exec.executeScript(connection, new File("./target/test-classes/test2.sql"), "Test", "[\\\\]");
 		//tests
 		rs = connection.getMetaData().getTables(null, null, "T1", null);
 		assertTrue(rs.next());
 		stm = connection.prepareStatement("SELECT COUNT(0) FROM t1");
 		rs = stm.executeQuery();
 		assertTrue(rs.next());
 		assertTrue(rs.getInt(1) == 1);
 		connection.commit();
 		connection.close();
 		connection = dataSource.getConnection();
 	
 		//prepare
 		exec.executeScript(connection, new File("./target/test-classes/test.sql"), "Drop");
 		//tested method - execute script with no separator and no section (single statement)
 		exec.executeScript(connection, new File("./target/test-classes/test3.sql"));
 		//tests
 		rs = connection.getMetaData().getTables(null, null, "T1", null);
 		assertTrue(rs.next());
 		connection.commit();
 		connection.close();
 		connection = dataSource.getConnection();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testCreateTable()
 		throws Exception{
 		
 		//prepare
 		connection.close();
 		connection = dataSource.getConnection();
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 		executeSql(connection, "DROP TABLE beantest1");
 		connection.commit();
 
 		//tested method
 		exec.createTable(connection);
 		//tests
 		ResultSet rs = connection.getMetaData().getTables(null, null, "BEANTEST1", null);
 		assertTrue(rs.next());
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testDropTable()
 		throws Exception{
 		
 		//prepare
 		connection.close();
 		connection = dataSource.getConnection();
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 
 		//tested method
 		exec.dropTable(connection);
 		//tests
 		ResultSet rs = connection.getMetaData().getTables(null, null, "BEANTEST1", null);
 		assertTrue(!rs.next());
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testExistsTable()
 		throws Exception{
 		
 		//prepare
 		connection.close();
 		connection = dataSource.getConnection();
 		SQLExecute<TestBean> exec = (SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource, TestBean.class);
 
 		//tested method
 		boolean exists = exec.existsTable(connection);
 		//tests
 		assertTrue(exists);
 		
 		//prepare
 		exec.dropTable(connection);
 		//tested method
 		exists = exec.existsTable(connection);
 		//tests
 		assertTrue(!exists);
 	}
 }
