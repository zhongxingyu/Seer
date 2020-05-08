 /**
  * 
  */
 package redneck.hadoop;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Robb
  * 
  */
 public class BasicWordCountTest {
 
 	private BasicWordCount driver;
 	private final static String inPath;
 	private final static String outPath;
 
 	static {
 		File in = new File("build/tmp/junitInPath");
 		File out = new File("build/tmp/junitOutPath");
 		boolean success = true;
 		success = in.mkdir();
 		success = out.mkdir();
 		inPath = in.getAbsolutePath();
 		outPath = out.getAbsolutePath();
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		driver = new BasicWordCount(inPath, outPath);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		driver = null;
 	}
 
 	@Test
 	public void verifyConstructor() {
 		assertNotNull(driver.conf);
 		assertEquals(WordCountMapper.class, driver.conf.getMapperClass());
 		assertEquals(WordCountReducer.class, driver.conf.getReducerClass());
 	}
 
 	/**
 	 * TODO - make this more meaningful or remove it once all the individual
 	 * pieces are tested.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testStaticMain_OneArgShouldFail() {
 		String[] args = new String[1];
 		args[0] = inPath;
 		try {
 			BasicWordCount.main(args);
 			fail("Main should have failed with an exception, but did not.");
 		} catch (Exception e) {
 			if (!BasicWordCount.PARAMETERS_ERR_MSG.equals(e.getMessage())) {
 				fail("Method threw an unexpected exception.");
 				e.printStackTrace();
 			}
 		}
 	}
 }
