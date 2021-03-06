 package org.gdms.source;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 import junit.framework.TestCase;
 
 import org.gdms.DBTestSource;
 import org.gdms.SourceTest;
 import org.gdms.data.DataSource;
 import org.gdms.data.DataSourceFactory;
 import org.gdms.data.SourceAlreadyExistsException;
 import org.gdms.data.db.DBSource;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
 import org.gdms.driver.memory.ObjectMemoryDriver;
 
 public class SourceManagementTest extends TestCase {
 
 	private static final String SOURCE = "source";
 	private static final String SOURCEMOD = "sourcd";
 	private SourceManager sm;
 	private DataSourceFactory dsf;
 	private File testFile;
 	private DBSource testDB;
 	private String sql;
 	private ObjectMemoryDriver obj;
 
 	public void testRegisterTwice() throws Exception {
 		try {
 			sm.register(SOURCE, new File("a"));
 			assertTrue(false);
 		} catch (SourceAlreadyExistsException e) {
 			// we check that the failed registration has broken nothing
 			sm.remove(SOURCE);
 			sm.register(SOURCE, testFile);
 			DataSource ds = dsf.getDataSource(SOURCE);
 			ds.open();
 			ds.cancel();
 		}
 	}
 
 	public void testRemoveAll() throws Exception {
 		Source src = sm.getSource(SOURCE);
 		associateFile(src, "statisticsFile");
 		associateString(src, "statistics");
 		sm.removeAll();
 		setUp();
 		src = sm.getSource(SOURCE);
 		assertTrue(src.getStringPropertyNames().length == 0);
 		assertTrue(src.getFilePropertyNames().length == 0);
 	}
 
 	public void testRemoveFileProperty() throws Exception {
 		Source source = sm.getSource(SOURCE);
 		String fileProp = "testFileProp";
 		associateFile(source, fileProp);
 		source.deleteProperty(fileProp);
 
 		File dir = sm.getSourceInfoDirectory();
 		File[] content = dir.listFiles();
 		assertTrue(content.length == 1);
 		assertTrue(content[0].getName().equals("directory.xml"));
 	}
 
 	public void testOverrideFileProperty() throws Exception {
 		Source source = sm.getSource(SOURCE);
 		String fileProp = "testFileProp";
 		associateFile(source, fileProp);
 
 		File file = source.createFileProperty(fileProp);
 		FileOutputStream fis = new FileOutputStream(file);
 		fis.write("newcontent".getBytes());
 		fis.close();
 
 		source.deleteProperty(fileProp);
 		File dir = sm.getSourceInfoDirectory();
 		File[] content = dir.listFiles();
 		assertTrue(content.length == 1);
 		assertTrue(content[0].getName().equals("directory.xml"));
 	}
 
 	public void testRemoveStringProperty() throws Exception {
 		Source source = sm.getSource(SOURCE);
 		String stringProp = "testFileProp";
 		associateString(source, stringProp);
 		source.deleteProperty(stringProp);
 
 		assertTrue(source.getStringPropertyNames().length == 0);
 		assertTrue(source.getFilePropertyNames().length == 0);
 	}
 
 	public void testAssociateFile() throws Exception {
 		String statistics = "statistics";
 		Source source = sm.getSource(SOURCE);
 		String rcStr = associateFile(source, statistics);
 
 		assertTrue(sm.getSource(SOURCE).getFilePropertyNames().length == 1);
 
 		sm.saveStatus();
 		instantiateDSF();
 
 		assertTrue(sm.getSource(SOURCE).getFilePropertyNames().length == 1);
 
 		String statsContent = source
 				.getFilePropertyContentsAsString(statistics);
 		assertTrue(statsContent.equals(rcStr));
 
 		File f = source.getFileProperty(statistics);
 		DataInputStream dis = new DataInputStream(new FileInputStream(f));
 		byte[] content = new byte[dis.available()];
 		dis.readFully(content);
 		assertTrue(new String(content).equals(rcStr));
 	}
 
 	private String associateFile(Source source, String propertyName)
 			throws Exception {
 		if (source.hasProperty(propertyName)) {
 			source.deleteProperty(propertyName);
 		}
 		File stats = source.createFileProperty(propertyName);
 		DataSource ds = dsf.getDataSource(source.getName());
 		ds.open();
 		long rc = ds.getRowCount();
 		ds.cancel();
 
 		FileOutputStream fis = new FileOutputStream(stats);
 		String rcStr = Long.toString(rc);
 		fis.write(rcStr.getBytes());
 		fis.close();
 
 		return rcStr;
 	}
 
 	public void testAssociateStringProperty() throws Exception {
 		Source source = sm.getSource(SOURCE);
 		String statistics = "statistics";
 		String rcStr = associateString(source, statistics);
 
 		assertTrue(sm.getSource(SOURCE).getStringPropertyNames().length == 1);
 
 		sm.saveStatus();
 		instantiateDSF();
 
 		assertTrue(sm.getSource(SOURCE).getStringPropertyNames().length == 1);
 
 		String statsContent = source.getProperty(statistics);
 		assertTrue(statsContent.equals(rcStr));
 	}
 
 	private String associateString(Source source, String propertyName)
 			throws Exception {
 		DataSource ds = dsf.getDataSource(SOURCE);
 		ds.open();
 		long rc = ds.getRowCount();
 		ds.cancel();
 
 		String rcStr = Long.toString(rc);
 		source.putProperty(propertyName, rcStr);
 		return rcStr;
 	}
 
 	public void testKeepPropertiesAfterRenaming() throws Exception {
 		Source source = sm.getSource(SOURCE);
 
 		associateString(source, "test");
 		associateFile(source, "testfile");
 		assertTrue(sm.getSource(SOURCE).getFilePropertyNames().length == 1);
 		assertTrue(sm.getSource(SOURCE).getStringPropertyNames().length == 1);
 
 		String memento = sm.getMemento();
 
 		sm.rename(SOURCE, SOURCEMOD);
 
 		assertTrue(memento.length() > SOURCE.length() + 2);
 		assertTrue(memento.substring(SOURCE.length() + 2).equals(
 				sm.getMemento().substring(SOURCEMOD.length() + 2)));
 
 	}
 
 	public void testReturnNullWhenNoProperty() throws Exception {
 		Source source = sm.getSource(SOURCE);
 		assert (source.getFileProperty("skjbnskb") == null);
 		assert (source.getProperty("skjbnskb") == null);
 	}
 
 	public void testChangeDirectory() throws Exception {
 		String statistics = "statistics";
 		Source source = sm.getSource(SOURCE);
 		associateFile(source, statistics);
 		associateString(source, statistics);
 		String memento = sm.getMemento();
 
 		sm.setSourceInfoDirectory(SourceTest.internalData
 				+ "source-management2");
 
 		sm.saveStatus();
 		instantiateDSF();
 
 		assertTrue(memento.equals(sm.getMemento()));
 	}
 
 	public void testSameSourceSameDSInstance() throws Exception {
 		DataSource ds1 = dsf.getDataSource(SOURCE, DataSourceFactory.NORMAL);
 		DataSource ds2 = dsf.getDataSource(SOURCE, DataSourceFactory.NORMAL);
 		ds1.open();
 		assertTrue(ds2.isOpen());
 		ds2.cancel();
 	}
 
 	public void testPersistence() throws Exception {
 		sm.removeAll();
 
 		DBTestSource dbTestSource = new DBTestSource("testhsqldb",
 				"org.hsqldb.jdbcDriver", SourceTest.internalData
 						+ "testhsqldb.sql", testDB);
 		dbTestSource.backup();
 		sql = "select count(id) from file;";
 
 		sm.register("file", testFile);
 		sm.register("db", testDB);
 		sm.register("sql", sql);
 		sm.register("obj", obj);
 
 		String fileContent = getContent("file");
 		String dbContent = getContent("db");
 		String sqlContent = getContent("sql");
 		String objContent = getContent("obj");
 
 		sm.saveStatus();
 		instantiateDSF();
 
 		assertTrue(fileContent.equals(getContent("file")));
 		assertTrue(dbContent.equals(getContent("db")));
 		assertTrue(sqlContent.equals(getContent("sql")));
 		assertTrue(objContent.equals(getContent("obj")));
 
 	}
 
 	private String getContent(String name) throws Exception {
 		DataSource ds = dsf.getDataSource(name);
 		ds.open();
 		String ret = ds.getAsString();
 		ds.cancel();
 
 		return ret;
 	}
 
 	public void testSelectDependencies() throws Exception {
 		sm.register("db", testDB);
 		sm.register("file", testFile);
 		String sql = "select 2*file.id from db, file "
 				+ "where file.id <> 234;";
 		sm.register("sql", sql);
 		DataSource ds = dsf.getDataSource("sql");
 		assertTrue(setIs(ds.getReferencedSources(), new String[] { "sql", "db",
 				"file" }));
 		ds = dsf.executeSQL(sql);
 		assertTrue(setIs(ds.getReferencedSources(), new String[] {
 				ds.getName(), "db", "file" }));
 		sql = "db union file;";
 		sm.register("sql2", sql);
 		ds = dsf.getDataSource("sql2");
 		assertTrue(setIs(ds.getReferencedSources(), new String[] { "sql2",
 				"db", "file" }));
 		ds = dsf.executeSQL(sql);
 		assertTrue(setIs(ds.getReferencedSources(), new String[] {
 				ds.getName(), "db", "file" }));
 
 		String[] srcDeps = dsf.getDataSource("file").getReferencedSources();
 		assertTrue(srcDeps.length == 1);
 		assertTrue(srcDeps[0].equals("file"));
 	}
 
 	public void testCannotDeleteDependedSource() throws Exception {
 		sm.register("db", testDB);
 		sm.register("file", testFile);
 		String sql = "select 2*file.id from db, file "
 				+ "where file.id <> 234;";
 		sm.remove("file");
 		sm.remove("db");
 
 		sm.register("db", testDB);
 		sm.register("file", testFile);
 		sm.register("sql", sql);
 
 		try {
 			sm.remove("file");
 			assertTrue(false);
 		} catch (IllegalStateException e) {
 		}
 		try {
 			sm.remove("db");
 			assertTrue(false);
 		} catch (IllegalStateException e) {
 		}
 
 		sm.remove("sql");
 		sm.remove("file");
 		sm.remove("db");
 	}
 
 	public void testCanDeleteIfDependentSourceIsNotWellKnown() throws Exception {
 		sm.register("db", testDB);
 		sm.register("file", testFile);
 		dsf.executeSQL("select 2*file.id from db, file "
 				+ "where file.id <> 234;");
 		sm.remove("file");
 		sm.remove("db");
 	}
 
 	public void testDependentDependingSync() throws Exception {
 		sm.removeAll();
 		sm.register("db", testDB);
 		sm.register("file", testFile);
 		String sql = "select 2*file.id from db, file "
 				+ "where file.id <> 234;";
 		sm.register("sql", sql);
 		sql = "select * from sql, file;";
 		sm.register("sql2", sql);
 		// Anonimous ds should not been taken into account for dependencies
 		dsf.executeSQL(sql);
 		Source src = sm.getSource("db");
 		assertTrue(setIs(src.getReferencingSources(), new String[] { "sql",
 				"sql2" }));
 		assertTrue(setIs(src.getReferencedSources(), new String[] {}));
 		src = sm.getSource("file");
 		assertTrue(setIs(src.getReferencingSources(), new String[] { "sql",
 				"sql2" }));
 		assertTrue(setIs(src.getReferencedSources(), new String[] {}));
 		src = sm.getSource("sql");
 		assertTrue(setIs(src.getReferencingSources(), new String[] { "sql2" }));
 		assertTrue(setIs(src.getReferencedSources(), new String[] { "file",
 				"db" }));
 		src = sm.getSource("sql2");
 		assertTrue(setIs(src.getReferencingSources(), new String[] {}));
 		assertTrue(setIs(src.getReferencedSources(), new String[] { "file",
 				"db", "sql" }));
 
 		sm.remove("sql2");
 		src = sm.getSource("db");
 		assertTrue(setIs(src.getReferencingSources(), new String[] { "sql" }));
 		assertTrue(setIs(src.getReferencedSources(), new String[] {}));
 		src = sm.getSource("file");
 		assertTrue(setIs(src.getReferencingSources(), new String[] { "sql" }));
 		assertTrue(setIs(src.getReferencedSources(), new String[] {}));
 		src = sm.getSource("sql");
 		assertTrue(setIs(src.getReferencingSources(), new String[] {}));
 		assertTrue(setIs(src.getReferencedSources(), new String[] { "file",
 				"db" }));
 		src = sm.getSource("sql2");
 		assertTrue(src == null);
 
 		sm.remove("sql");
 		src = sm.getSource("db");
 		assertTrue(setIs(src.getReferencingSources(), new String[] {}));
 		assertTrue(setIs(src.getReferencedSources(), new String[] {}));
 		src = sm.getSource("file");
 		assertTrue(setIs(src.getReferencingSources(), new String[] {}));
 		assertTrue(setIs(src.getReferencedSources(), new String[] {}));
 		src = sm.getSource("sql");
 		assertTrue(src == null);
 	}
 
	public void testObjectDriverType() throws Exception {
		ObjectMemoryDriver driver = new ObjectMemoryDriver(new String[] {
				"pk", "geom" }, new Type[] { TypeFactory.createType(Type.INT),
				TypeFactory.createType(Type.GEOMETRY) });
		sm.register("spatial", driver);
		Source src = sm.getSource("spatial");
		assertTrue((src.getType() & SourceManager.MEMORY) == SourceManager.MEMORY);
		assertTrue((src.getType() & SourceManager.VECTORIAL) == SourceManager.VECTORIAL);
		driver = new ObjectMemoryDriver(new String[] { "pk" },
				new Type[] { TypeFactory.createType(Type.INT) });
		sm.register("alpha", driver);
		src = sm.getSource("alpha");
		assertTrue((src.getType() & SourceManager.MEMORY) == SourceManager.MEMORY);
		assertTrue((src.getType() & SourceManager.VECTORIAL) == 0);
	}

 	private boolean setIs(String[] referencingSources, String[] test) {
 		if (referencingSources.length != test.length) {
 			return false;
 		} else {
 			ArrayList<String> set = new ArrayList<String>();
 			for (String string : referencingSources) {
 				set.add(string);
 			}
 			for (String string : test) {
 				set.remove(string);
 			}
 
 			return set.isEmpty();
 		}
 	}
 
 	// public void testGetAlreadyRegisteredSourceAnonimously() throws Exception
 	// {
 	// sm.removeAll();
 	//
 	// sm.register("myfile", testFile);
 	// sm.register("myDB", testDB);
 	// sm.register("myObj", obj);
 	//
 	// DataSource ds = dsf.getDataSource(testFile);
 	// assertTrue(ds.getName().equals("myFile"));
 	//
 	// ds = dsf.getDataSource(testDB);
 	// assertTrue(ds.getName().equals("myDB"));
 	//
 	// ds = dsf.getDataSource(obj);
 	// assertTrue(ds.getName().equals("myObj"));
 	// }
 
 	@Override
 	protected void setUp() throws Exception {
 		instantiateDSF();
 		sm.removeAll();
 		testFile = new File(SourceTest.internalData + "test.csv");
 		sm.register(SOURCE, testFile);
 		testDB = new DBSource(null, 0, SourceTest.internalData
 				+ "backup/testhsqldb", "sa", "", "gisapps", "jdbc:hsqldb:file");
 		obj = new ObjectMemoryDriver();
 	}
 
 	private void instantiateDSF() {
 		dsf = new DataSourceFactory(SourceTest.internalData
 				+ "source-management");
 		sm = dsf.getSourceManager();
 
 	}
 
 }
