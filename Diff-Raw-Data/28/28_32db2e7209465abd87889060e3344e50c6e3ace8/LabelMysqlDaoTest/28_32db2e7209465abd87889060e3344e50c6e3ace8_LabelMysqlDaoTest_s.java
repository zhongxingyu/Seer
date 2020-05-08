 package org.weso.acota.persistence.mysql;
 
 import static org.junit.Assert.assertEquals;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.dbunit.database.DatabaseConfig;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
 import org.dbunit.ext.mysql.MySqlDataTypeFactory;
 import org.dbunit.operation.DatabaseOperation;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.weso.acota.persistence.LabelDAO;
 
 public class LabelMysqlDaoTest {
 	
 	private LabelDAO labelDao;
 	
 	@Before
 	public void init() throws Exception {
 		DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
 		this.labelDao = new LabelMysqlDAO(); 
 	}
 
 	@After
 	public void after() throws Exception {
 		DatabaseOperation.DELETE_ALL.execute(getConnection(), getDataSet());
 	}
 
 	protected IDatabaseConnection getConnection() throws Exception {
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection jdbcConnection = DriverManager.getConnection(
				"jdbc:mysql://localhost/acota", "root", "root");
 		
 		IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);
 		
 		DatabaseConfig dbConfig = connection.getConfig();
 		dbConfig.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
 		
 		return connection;
 	}
 
 	protected DatabaseOperation getSetUpOperation() throws Exception {
 		return DatabaseOperation.REFRESH;
 	}
 
 	protected IDataSet getDataSet() throws Exception {
 		return new FlatXmlDataSetBuilder().build(this.getClass().getResource(
 				"/resources/dbunit.xml"));
 	}
 
 	@Test
 	public void saveLabelTest() throws Exception {
 		labelDao.saveLabel("foo".hashCode(), "foo");
 
 		IDataSet databaseDataSet = getConnection().createDataSet();
 		ITable actualTable = databaseDataSet.getTable("labels");
 
 		IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(this
 				.getClass().getResource("/resources/dbunitExpected.xml"));
 		ITable expectedTable = expectedDataSet.getTable("labels");
 
 		assertEquals(expectedTable.getRowCount(), actualTable.getRowCount());
 
 	}
 	
 	@Test
 	public void getLabelByIdThatNotExists() throws SQLException, ClassNotFoundException{
 		assertEquals(null, labelDao.getLabelById(1000));
 	}
 	
 	@Test
 	public void getLabelByIdTest() throws SQLException, ClassNotFoundException{
 		assertEquals("group", labelDao.getLabelById(98629247));
 	}
 	
 	@Test
 	public void getLabelByHashThatNotExists() throws SQLException, ClassNotFoundException{
 		assertEquals(null, labelDao.getLabelByHash(1000));
 	}
 	
 	@Test
 	public void getLabelByHashTest() throws SQLException, ClassNotFoundException{
 		assertEquals("group", labelDao.getLabelByHash(98629247));
 	}
 	
 	@Test
 	public void getLabelsByIdsEmpty() throws SQLException, ClassNotFoundException{
 		assertEquals(Collections.EMPTY_SET, labelDao.getLabelsByIds(Collections.<Integer> emptySet()));
 	}
 	
 	@Test
 	public void getLabelsByIdsThatNotExist() throws SQLException, ClassNotFoundException{
 		Set<Integer> ids = new HashSet<Integer>();
 		ids.add(9238424);
 		ids.add(93247);
 		assertEquals(Collections.EMPTY_SET, labelDao.getLabelsByHashes(ids));
 	}
 	
 	@Test
 	public void getLabelsByIdsTest() throws SQLException, ClassNotFoundException{
 		Set<String> labels = new HashSet<String>();
 		labels.add("group");
 		labels.add("research");
 		
 		Set<Integer> ids = new HashSet<Integer>();
 		ids.add(98629247);
 		ids.add(-350895717);
 		assertEquals(labels, labelDao.getLabelsByIds(ids));
 	}
 	
 	@Test
 	public void getLabelsByHashesEmpty() throws SQLException, ClassNotFoundException{
 		assertEquals(Collections.EMPTY_SET, labelDao.getLabelsByHashes(Collections.<Integer> emptySet()));
 	}
 	
 	@Test
 	public void getLabelsByHashesThatNotExist() throws SQLException, ClassNotFoundException{
 		Set<Integer> hashes = new HashSet<Integer>();
 		hashes.add(9238424);
 		hashes.add(93247);
 		assertEquals(Collections.EMPTY_SET, labelDao.getLabelsByHashes(hashes));
 	}
 	
 	@Test
 	public void getLabelsByHashesTest() throws SQLException, ClassNotFoundException{
 		Set<String> labels = new HashSet<String>();
 		labels.add("group");
 		labels.add("research");
 		
 		Set<Integer> hashes = new HashSet<Integer>();
 		hashes.add(98629247);
 		hashes.add(-350895717);
 		assertEquals(labels, labelDao.getLabelsByHashes(hashes));
 	}
 	
 }
