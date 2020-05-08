 package com.lemondo.commons.db;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.DriverManager;
 import java.util.Properties;
 
 import org.dbunit.Assertion;
 import org.dbunit.DatabaseTestCase;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 
 public class SampleTest extends DatabaseTestCase {
 
 	private static final String LOGIN_PROPERTIES_FILE = "src/test/login.properties";
 
 	private final String driverName;
 	private final String uri;
 	private final String userName;
 	private final String password;
 
 	private final String inputDataXml;
 
 	private IDataSet loadedDataSet;
 
 	public SampleTest() throws FileNotFoundException, IOException, ClassNotFoundException {
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(LOGIN_PROPERTIES_FILE));
 
 		driverName = prop.getProperty("com.lemondo.commons.db.test.DriverName");
 		uri = prop.getProperty("com.lemondo.commons.db.test.URI");
 		userName = prop.getProperty("com.lemondo.commons.db.test.User");
 		password = prop.getProperty("com.lemondo.commons.db.test.Password");
 
 		inputDataXml = prop.getProperty("com.lemondo.commons.db.test.InputDataSet");
 
 		Class.forName(driverName);
 	}
 
 	@Override
 	protected IDatabaseConnection getConnection() throws Exception {
 		return new DatabaseConnection(DriverManager.getConnection(uri, userName, password));
 	}
 
 	@Override
 	protected IDataSet getDataSet() throws Exception {
 		loadedDataSet = new FlatXmlDataSet(new FileInputStream(inputDataXml));
 		return loadedDataSet;
 	}
 
 	public void testTheTest() throws Exception {
 		assertNotNull(loadedDataSet);
		assertEquals(3, loadedDataSet.getTable("test_table").getRowCount());
 		Assertion.assertEquals(loadedDataSet, getConnection().createDataSet());
 	}
 
 }
