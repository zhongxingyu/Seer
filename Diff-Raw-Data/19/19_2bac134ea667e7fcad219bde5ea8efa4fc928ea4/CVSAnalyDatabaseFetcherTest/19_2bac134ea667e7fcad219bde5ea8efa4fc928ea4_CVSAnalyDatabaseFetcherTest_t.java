 package org.cvsanaly;
 
 import static org.eclipse.persistence.config.PersistenceUnitProperties.*;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.KalibroTestCase;
 
 
 public class CVSAnalyDatabaseFetcherTest extends KalibroTestCase {
 	private static final File DATABASE_PATH = new File("/tmp/teste");
 	
 	private CVSAnalyDatabaseFetcher fetcher;
 	
 	@Before
 	public void setUp() {
 		fetcher = new CVSAnalyDatabaseFetcher(DATABASE_PATH);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void testPersistenceProperties() {
 		Map<String, String> expected = new HashMap<String, String>();
 		expected.put(DDL_GENERATION, NONE);
 		expected.put(JDBC_DRIVER, "org.sqlite.JDBC");
		expected.put(JDBC_URL, "jdbc:sqlite:/tmp/teste");
 		assertDeepEquals(expected, fetcher.getPersistenceProperties());
 	}
 	
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldGetResultsFromEntityManager() {
 		//TODO test this method 
 	}
 
 }
