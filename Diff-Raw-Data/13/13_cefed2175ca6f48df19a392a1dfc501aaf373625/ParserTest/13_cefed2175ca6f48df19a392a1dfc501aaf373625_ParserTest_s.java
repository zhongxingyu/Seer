 package what.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import what.Facade;
 import what.sp_parser.ParserMediator;
 
 public class ParserTest {
 	
 	private static Facade f;
 	private static ParserMediator pm;
 	private static String sourcePath;
 	private static String seperator;
 	
 	@BeforeClass
 	public static void initialize() {
 		f = Facade.getFacadeIstance();
 		sourcePath = System.getProperty("user.dir");
 		seperator = System.getProperty("file.separator");
 	}
 	
 	@Before
 	public void resetFacade() {
 		f.reset();
 		f.init(sourcePath + seperator + "conf\\ConfigurationFile.json");
	//	pm = f.getParserMediator();
 	}
 	
 	@Test
 	public void smallParseTestStandardSize() {
 		assertTrue(f.parseLogFile(sourcePath + seperator + "example\\result10.csv"));
 		assertEquals(10, pm.getLogfile().getLines());
 		assertEquals(pm.getPoolsize(), pm.getFinishedTasks());
 	}
 	
 	@Test
 	public void smallParseTestSize10() {
 		pm.setPoolsizeParsing(10);
 		assertTrue(f.parseLogFile(sourcePath + seperator + "example\\result10.csv"));
 		assertEquals(10, pm.getLogfile().getLines());
 		assertEquals(10, pm.getPoolsize());
 		assertEquals(pm.getPoolsize(), pm.getFinishedTasks());
 	}
 	
 	@Test
 	public void smallParseTestTooBig() {
 		pm.setPoolsizeParsing(100);
 		assertFalse(f.parseLogFile(sourcePath + seperator + "example\\result10.csv"));
 		assertEquals(pm.getError(), "Error #2: ParserMediator.poolsizeParsing is bigger than 50.");
 	}
 	
 	@Test
 	public void negativePoolsize() {
 		pm.setPoolsizeParsing(-2);
 		assertFalse(f.parseLogFile(sourcePath + seperator + "example\\result10.csv"));
 		assertEquals(pm.getError(), "Error #1: ParserMediator.poolsizeParsing is smaller than 1.");
 	}
 	
 	@Test
 	public void nonexistentFile() {
 		assertFalse(f.parseLogFile("nonexistentpath.csv"));
 		assertEquals(pm.getError(), "Error #5: The path is wrong.");
 	}
 	
 	@Test
 	public void wrongFile() {
 		assertFalse(f.parseLogFile(sourcePath + seperator + "example\\wrongFile.txt"));
 		assertEquals(pm.getError(), "Error #5: The path is wrong.");
 	}
 	
 	@Test
 	public void flawedFile() {
 		assertFalse(f.parseLogFile(sourcePath + seperator + "example\\flawedFile.csv"));
 		assertEquals(pm.getError(), "Error #11: The configuration file got a different format.");
 		//flawedFile.csv is a copy of result10.csv with a spelling mistake in the type.
 	}
 	
 	@Test
 	public void mediumParseTestStandardSize() {
 		assertTrue(f.parseLogFile(sourcePath + seperator + "example\\result1000.csv"));
 		assertEquals(1000, pm.getLogfile().getLines());
 	}
 	
 	@Test
 	public void mediumParseTest50Tasks() {
 		pm.setPoolsizeParsing(50);
 		assertTrue(f.parseLogFile(sourcePath + seperator + "example\\result1000.csv"));
 		assertEquals(1000, pm.getLogfile().getLines());
 		assertEquals(50, pm.getPoolsize());
 		assertEquals(pm.getPoolsize(), pm.getFinishedTasks());
 	}
 	
 	@Test
 	public void bigParseTestStandardSize() {
 		assertTrue(f.parseLogFile(sourcePath + seperator + "example\\resultDay.csv"));
 		assertEquals(32041, pm.getLogfile().getLines());
 	}
 	
 	@Ignore
 	public void veryBigParseTestStandardSize() {
 		assertTrue(f.parseLogFile("C:\\Users\\Alex\\Downloads\\resultDez.csv"));
 		assertEquals(1226224, pm.getPoolsize());
 	}
 
 
 }
