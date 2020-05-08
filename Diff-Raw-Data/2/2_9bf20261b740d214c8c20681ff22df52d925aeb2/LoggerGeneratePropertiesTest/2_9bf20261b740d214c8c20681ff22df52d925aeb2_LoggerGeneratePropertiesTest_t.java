 package cz.muni.fi.pb138.log4jconverter.configuration;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cz.muni.fi.pb138.log4jconverter.PropertiesParser;
 
 public class LoggerGeneratePropertiesTest {
 	private Logger logger;
 	private Level level;
 	private Properties p;
 	private String loggerName;
 
 	@Before
 	public void setUp() throws Exception {
 		level = new Level();
 		p = new Properties();
 	} 
 
 	@Test
 	public void testOnlyLevel() {
 		loggerName = "org.juint.Assert";
 		logger = new Logger(loggerName);
 		level.setValues(Level.Levels.DEBUG);
 		logger.setLevel(level);
 		
 		testExpected("DEBUG");
 	}
 
 	@Test
 	public void testLevelOneAppender() {
 		loggerName = "org.juint.Assert";
 		logger = new Logger(loggerName);
 		level.setValues(Level.Levels.ERROR);
 		logger.setLevel(level);
 		logger.addAppenderRef("A1");
 		
 		testExpected("ERROR, A1");
 	}
 
 	@Test
 	public void testLevelMoreAppenders() {
 		loggerName = "org.juint.Assert";
 		logger = new Logger(loggerName);
 		level.setValues(Level.Levels.FATAL);
 		logger.setLevel(level);
 		logger.addAppenderRef("A1");
 		logger.addAppenderRef("A2");
 		logger.addAppenderRef("A3");
 		logger.addAppenderRef("A4");
 		logger.addAppenderRef("A5");
 		
		testExpected("FATAL, A1, A2, A3, A4, A5");
 	}
 
 	@Test
 	public void testAppendersOnly() {
 		loggerName = "org.juint.Assert";
 		logger = new Logger(loggerName);
 		logger.addAppenderRef("A1");
 		logger.addAppenderRef("A2");
 		logger.addAppenderRef("A3");
 		
 		testExpected(", A1, A2, A3");
 	}
 
 	@Test
 	public void testCategory() {
 		loggerName = "org.juint.Assert";
 		logger = new Logger(loggerName);
 		level.setValues(Level.Levels.FATAL);
 		logger.setLevel(level);
 		logger.addAppenderRef("A1");
 		logger.addAppenderRef("A2");
 		logger.addAppenderRef("A3");
 		logger.isCategory(true);
 		
 		testExpected("FATAL, A1, A2, A3", PropertiesParser.CATEGORY);
 	}
 	
 
 	private void testExpected(String value){
 		testExpected(value, PropertiesParser.LOGGER);
 	}
 	
 	private void testExpected(String value, String specifier){
 		logger.generateProperties(p);
 		String expectedKey = PropertiesParser.PREFIX + "." + specifier + "." + loggerName;
 		assertTrue(p.containsKey(expectedKey));
 		assertEquals(value, p.get(expectedKey));
 	}
 
 }
