 package org.kalibro.core.persistence;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.KalibroTestCase;
import org.kalibro.core.model.ConfigurationFixtures;
import org.kalibro.core.model.ModuleResult;
import org.kalibro.core.model.ModuleResultFixtures;
 
 public class CsvExportTest extends KalibroTestCase {
 
 	private static final File CSV_FILE = new File(TESTS_DIRECTORY, "HelloWorld.csv");
 
	private ModuleResult moduleResult;

 	@Before
 	public void setUp() {
 		assertFalse(CSV_FILE.exists());
		moduleResult = ModuleResultFixtures.helloWorldClassResult();
		moduleResult.setConfiguration(ConfigurationFixtures.simpleConfiguration());
 	}
 
 	@After
 	public void tearDown() {
 		CSV_FILE.delete();
 	}
 
 	@Test(timeout = INTEGRATION_TIMEOUT)
 	public void testExport() throws IOException {
 		InputStream expectedCsv = getClass().getResourceAsStream("HelloWorld.csv");
		new ModuleResultCsvExporter(moduleResult).exportTo(CSV_FILE);
 		assertEquals(IOUtils.toString(expectedCsv), FileUtils.readFileToString(CSV_FILE));
 	}
 }
