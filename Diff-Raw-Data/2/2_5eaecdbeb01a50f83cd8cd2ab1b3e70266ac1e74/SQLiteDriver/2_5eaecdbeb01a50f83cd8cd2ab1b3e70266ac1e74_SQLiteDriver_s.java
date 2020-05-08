 package es.udc.cartolab.com.hardcode.gdbms.driver.sqlite;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.hardcode.gdbms.driver.exceptions.OpenDriverException;
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.data.driver.AbstractJDBCDriver;
 import com.hardcode.gdbms.engine.values.Value;
 
 public class SQLiteDriver extends AbstractJDBCDriver {
 
 	public final static String NAME = "SQLite Alphanumeric";
 	protected SQLiteJDBCSupport sqliteJdbcSupport;
 
 	static {
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void open(Connection con, String sql) throws SQLException,
 			OpenDriverException {
 		sqliteJdbcSupport = SQLiteJDBCSupport.newJDBCSupport(con, sql);
 		Statement st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
 				ResultSet.CONCUR_READ_ONLY);
 		ResultSet res = st.executeQuery(sql);
 		jdbcWriter.initialize(con, res);
 		jdbcWriter.setCreateTable(false);
 		jdbcWriter.setWriteAll(false);
 	}
 
 	@Override
 	public Connection getConnection(String host, int port, String dbName,
 			String user, String password) throws SQLException {
 		String connString = "jdbc:sqlite:" + host;
 
 		return DriverManager.getConnection(connString);
 	}
 
 	@Override
 	public String getDefaultPort() {
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	@Override
 	public int getFieldCount() throws ReadDriverException {
 		return sqliteJdbcSupport.getFieldCount();
 	}
 
 	@Override
 	public String getFieldName(int fieldId) throws ReadDriverException {
 		return sqliteJdbcSupport.getFieldName(fieldId);
 	}
 
 	@Override
 	public int getFieldType(int i) throws ReadDriverException {
 		return sqliteJdbcSupport.getFieldType(i);
 	}
 
 	@Override
 	public Value getFieldValue(long rowIndex, int fieldId)
 			throws ReadDriverException {
 		return sqliteJdbcSupport.getFieldValue(rowIndex, fieldId);
 	}
 
 	@Override
 	public long getRowCount() throws ReadDriverException {
 		return sqliteJdbcSupport.getRowCount();
 	}
 
 	@Override
 	public int getFieldWidth(int i) throws ReadDriverException {
 		return sqliteJdbcSupport.getFieldWidth(i);
 	}
 
 	@Override
 	public void close() throws SQLException {
		jdbcSupport.close();
 		jdbcWriter.close();
 	}
 
 }
