 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Vector;
 
 import junit.framework.TestCase;
 import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;
 import net.antidot.semantic.rdf.rdb2rdf.dm.core.DirectMapper;
 import net.antidot.semantic.rdf.rdb2rdf.dm.core.DirectMappingEngine;
 import net.antidot.semantic.rdf.rdb2rdf.r2rml.core.R2RMLProcessor;
 import net.antidot.sql.model.core.DriverType;
 import net.antidot.sql.model.core.SQLConnector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.openrdf.rio.RDFFormat;
 
 @RunWith(Parameterized.class)
 public class MainIT extends TestCase {
 
     public enum NormTested {
 	DirectMapping, R2RML;
     }
 
     // Log
     private static Log log = LogFactory.getLog(MainIT.class);
 
     public static final File w3cDefinitionSearchPath = new File(
 	    "src/test/resources/");
     public static final String w3cDirPrefix = "rdb2rdf-ts";
 
     // Base URI
     private static final String baseURI = "http://example.com/base/";
 
     // R2RML suffix. To be improved!
     private static final String[] r2rmlSuffixes = { "", "a", "b", "c", "d",
 	    "e", "f", "g", "h", "i", "j", "k" };
 
     // Database TEST settings
     private static String userName = Settings.userName;
     private static String password = Settings.password;
     private static String url = Settings.url;
     private static DriverType driver = Settings.driver;
 
     // Instance field
 
     private NormTested tested = null;
     private String directory = null;
     private String r2rmlSuffix = null;
 
     public MainIT(NormTested tested, String directory, String suffix)
 	    throws SQLException, InstantiationException,
 	    IllegalAccessException, ClassNotFoundException {
 	this.tested = tested;
 	this.directory = directory;
 	this.r2rmlSuffix = suffix;
     }
 
     @Parameters
     public static Collection<Object[]> getTestsFiles() throws Exception {
 	Collection<Object[]> parameters = new Vector<Object[]>();
 	File[] w3cDirs = w3cDefinitionSearchPath
 		.listFiles(new FilenameFilter() {
 		    public boolean accept(File dir, String name) {
 			return name.startsWith(w3cDirPrefix);
 		    }
 		});
 
 	int i = 0;
 	for (File w3cDir : w3cDirs) {
 	    File[] files = w3cDir.listFiles();
 	    for (File f : files) {
 		if (f.isFile()) {
 		    continue;
 		}
 		final String file_path = f.getAbsolutePath();
 		// DirectMapping, create directly test
 		parameters.add(new Object[] { NormTested.DirectMapping,
 			file_path, null });
 		log.info("[W3CTester:test] Create parameter " + (i++)
 			+ " : {DirectMapping ; " + file_path + "}");
 		// Test suffix to create R2RML tests
 		for (String suffix : r2rmlSuffixes) {
 		    // Create R2RML Mapping
 		    final String test_filepath = file_path + "/r2rml" + suffix
 			    + ".ttl";
 		    if ((new File(test_filepath)).exists()) {
 			parameters.add(new Object[] { NormTested.R2RML,
 				file_path, suffix });
 			log.info("[W3CTester:test] Create parameter " + (i++)
 				+ " : {R2RML ; " + file_path + " ; " + suffix
 				+ "}");
 		    }
 		}
 	    }
 	}
 	return parameters;
     }
 
     private Connection getNewConnection(String parameter) throws SQLException,
 	    InstantiationException, IllegalAccessException,
 	    ClassNotFoundException {
 	log.info("[W3CTester:getNewConnection] Create new DB connection");
 	return SQLConnector.connect(userName, password, url, driver,
 		Settings.testDbName + parameter);
     }
 
     // @Test
     // public void testAsCaseSensitive() throws Exception {
     // Connection conn =
     // DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/test",
     // "root","root");
     // java.sql.Statement s = conn.createStatement(
     // ResultSet.HOLD_CURSORS_OVER_COMMIT, ResultSet.CONCUR_READ_ONLY);
     // s.executeQuery("Select ('Student' || \"ID\" ) AS StudentId, \"Name\", \"name\" from \"Student\";");
     // ResultSet rs = s.getResultSet();
     // ResultSetMetaData metaData = rs.getMetaData();
     // int n = metaData.getColumnCount();
     // for (int i = 1; i <= n; i++) {
     // System.out.println("Column: "+metaData.getColumnLabel(i)+" "+metaData.isCaseSensitive(i));
     // }
     // }
 
     @Test
     public void testNorm() throws Exception {
 	try {
 	    log.info("[W3CTester:testNorm] Run tests for " + tested.name()
 		    + " from : " + directory);
 
 	    {
 		// Clean TEST table
 		log.info("[W3CTester:testNorm] Clean test database...");
 		String parameters = "";
 		if (driver.equals(DriverType.MysqlDriver)) {
 		    parameters = "?characterEncoding=utf8&sessionVariables=sql_mode='ANSI',storage_engine=InnoDB";
 		}
 		Connection conn = getNewConnection(parameters);
 		SQLConnector.resetMySQLDatabase(conn, driver);
 		// Load TEST database
 		log.info("[W3CTester:testNorm] Load new tables...");
 		SQLConnector.updateDatabase(conn, directory + "/create.sql");
 		conn.close();
 	    }
 
 	    switch (tested) {
 	    case DirectMapping:
 		// Run Direct Mapping
 		runDirectMapping();
 		break;
 	    case R2RML:
 		// Run R2RML
 		runR2RML();
 		break;
 	    default:
 		fail("Norm is not recognized!!!!");
 	    }
 	}
 	catch (Exception e) {
 	    e.printStackTrace();
 	    throw e;
 	}
     }
 
     private void runDirectMapping() throws Exception {
 	// Load ref
 	SesameDataSet ref = new SesameDataSet();
 	final String ref_path = directory + "/directGraph.ttl";
 
 	if (!(new File(ref_path)).exists()) {
 	    log.info("[W3CTester:runDirectMapping] ref file is not present");
 	    return;
 	}
 
 	try {
 	    ref.loadDataFromFile(ref_path, RDFFormat.N3);
 	}
 	catch (Exception e) {
 	    e.printStackTrace();
 	    fail("Unable to load ref " + ref_path);
 	    return;
 	}
 
 	// Create Direct Mapping
 	SesameDataSet result;
 	Connection conn = null;
 	try {
 	    String parameters = "";
 	    if (driver.equals(DriverType.MysqlDriver)) {
 		parameters = "?characterEncoding=utf8&padCharsWithSpace=true";
 	    }
 	    conn = getNewConnection(parameters);
 	    result = DirectMapper.generateDirectMapping(conn,
 		    DirectMappingEngine.Version.WD_20120529, driver, baseURI,
 		    null, null);
 	}
 	catch (UnsupportedEncodingException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	    fail("UnsupportedEncodingException");
 	    return;
 	}
 	catch (Exception e) {
 	    throw e;
 	}
 	finally {
 	    if (conn != null) {
 		conn.close();
 	    }
 	}
 	// Serialize result
 	result.dumpRDF(directory + "/directGraph-db2triples.ttl",
 		RDFFormat.TURTLE);
 
 	// Compare
 	assertTrue("Direct Mapping test failed: " + directory,
 		ref.isEqualTo(result));
     }
 
     private void runR2RML() throws Exception {
 	// Create R2RML Mapping
 	final String test_filepath = directory + "/r2rml" + r2rmlSuffix
 		+ ".ttl";
 	final String mapped_filepath = directory + "/mapped" + r2rmlSuffix
 		+ ".nq";
 	File r2rml_def_file = new File(test_filepath);
 	if (r2rml_def_file.exists()) {
 	    log.info("[W3CTester:runR2RML] Working on test: " + test_filepath);
 	    SesameDataSet result;
 	    Connection conn = null;
 	    try {
 		String parameters = "";
 		if (driver.equals(DriverType.MysqlDriver)) {
		    parameters = "?padCharsWithSpace=true&sessionVariables=sql_mode='ANSI',storage_engine=InnoDB";
 		}
 		conn = getNewConnection(parameters);
 		result = R2RMLProcessor.convertDatabase(conn,
 			r2rml_def_file.getAbsolutePath(), baseURI, driver);
 	    }
 	    catch (Exception e) {
 		if ((new File(mapped_filepath)).exists()) {
 		    e.printStackTrace();
 		    fail("R2RML error: " + directory);
 		}
 		else {
 		    log.info("[W3CTester:runR2RML] R2RML data was not valid");
 		}
 		return;
 	    }
 	    finally {
 		if (conn != null) {
 		    conn.close();
 		}
 	    }
 	    // Serialize result
 	    result.dumpRDF(directory + "/mapped" + r2rmlSuffix
 		    + "-db2triples.nq", RDFFormat.NQUADS);
 
 	    // Load ref
 	    SesameDataSet ref = new SesameDataSet();
 	    try {
 		ref.loadDataFromFile(mapped_filepath, RDFFormat.NQUADS);
 	    }
 	    catch (Exception e) {
 		e.printStackTrace();
 		fail("Unable to load ref " + mapped_filepath);
 		return;
 	    }
 
 	    // Compare
 	    assertTrue("R2RML test failed: " + directory, ref.isEqualTo(result));
 	}
     }
 
     public static File[] listFiles(String directoryPath) {
 	File[] files = null;
 	File directoryToScan = new File(directoryPath);
 	files = directoryToScan.listFiles();
 	return files;
     }
 
 }
