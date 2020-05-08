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
 import org.weso.acota.persistence.DocumentDAO;
 
 public class DocumentMysqlDaoTest {
 	
 	private DocumentDAO documentDao;
 	
 	@Before
 	public void init() throws Exception {
 		DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
 		this.documentDao = new DocumentMysqlDAO(); 
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
 	public void saveDocumentTest() throws Exception {
 		documentDao.saveDocument("foo".hashCode(), "foo");
 
 		IDataSet databaseDataSet = getConnection().createDataSet();
 		ITable actualTable = databaseDataSet.getTable("documents");
 
 		IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(this
 				.getClass().getResource("/resources/dbunitExpected.xml"));
 		ITable expectedTable = expectedDataSet.getTable("documents");
 
 		assertEquals(expectedTable.getRowCount(), actualTable.getRowCount());
 
 	}
 	
 	@Test
 	public void getDocumentByIdThatNotExists() throws SQLException, ClassNotFoundException{
 		assertEquals(null, documentDao.getDocumentById(1000));
 	}
 	
 	@Test
 	public void getDocumentByIdTest() throws SQLException, ClassNotFoundException{
 		assertEquals("http://www.weso.es", documentDao.getDocumentById(-220345143));
 	}
 	
 	@Test
 	public void getDocumentByHashThatNotExists() throws SQLException, ClassNotFoundException{
 		assertEquals(null, documentDao.getDocumentByHash(1000));
 	}
 	
 	@Test
 	public void getDocumentByHashTest() throws SQLException, ClassNotFoundException{
 		assertEquals("http://www.weso.es", documentDao.getDocumentByHash(-220345143));
 	}
 	
 	@Test
 	public void getDocumentsByIdsEmpty() throws SQLException, ClassNotFoundException{
 		assertEquals(Collections.EMPTY_SET, documentDao.getDocumentsByIds(Collections.<Integer> emptySet()));
 	}
 	
 	@Test
 	public void getDocumentsByIdsThatNotExist() throws SQLException, ClassNotFoundException{
 		Set<Integer> ids = new HashSet<Integer>();
 		ids.add(9238424);
 		ids.add(93247);
 		assertEquals(Collections.EMPTY_SET, documentDao.getDocumentsByHashes(ids));
 	}
 	
 	@Test
 	public void getDocumentsByIdsTest() throws SQLException, ClassNotFoundException{
 		Set<String> Documents = new HashSet<String>();
 		Documents.add("http://www.weso.es");
 		Documents.add("http://www.example.es");
 		
 		Set<Integer> ids = new HashSet<Integer>();
 		ids.add(-220345143);
 		ids.add(778016507);
 		assertEquals(Documents, documentDao.getDocumentsByIds(ids));
 	}
 	
 	@Test
 	public void getDocumentsByHashesEmpty() throws SQLException, ClassNotFoundException{
 		assertEquals(Collections.EMPTY_SET, documentDao.getDocumentsByHashes(Collections.<Integer> emptySet()));
 	}
 	
 	@Test
 	public void getDocumentsByHashesThatNotExist() throws SQLException, ClassNotFoundException{
 		Set<Integer> hashes = new HashSet<Integer>();
 		hashes.add(9238424);
 		hashes.add(93247);
 		assertEquals(Collections.EMPTY_SET, documentDao.getDocumentsByHashes(hashes));
 	}
 	
 	@Test
 	public void getDocumentsByHashesTest() throws SQLException, ClassNotFoundException{
 		Set<String> Documents = new HashSet<String>();
 		Documents.add("http://www.weso.es");
 		Documents.add("http://www.example.es");
 		
 		Set<Integer> hashes = new HashSet<Integer>();
 		hashes.add(-220345143);
 		hashes.add(778016507);
 		assertEquals(Documents, documentDao.getDocumentsByHashes(hashes));
 	}
 	
 }
