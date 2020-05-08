 package cz.muni.fi.pb138.log4jconverter.configuration;
 
 import static org.junit.Assert.*;
 
 import java.util.Properties;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cz.muni.fi.pb138.log4jconverter.configuration.Level.Levels;
 
 public class RootGeneratePropertiesTest {
 	private String expectedKey;
 	private Root r;
 	private Level l;
 	private Properties p;
 
     @Before
     public void setUp() {
     	r = new Root();
     	l = new Level();
     	p = new Properties();
     	expectedKey = "log4j.rootLogger";
     }
 
 	@Test
 	public void rootTest1() {
 		l.setValues(Levels.ERROR);
 		r.setLevel(l);
 		r.addAppenderRef("A1");
 		r.addAppenderRef("A2");
 		r.generateProperties(p);
 
 		testExpected("ERROR, A1, A2");
 	}
 	
 	@Test
 	public void rootTest2() {
 		l.setValues(Levels.ALL);
 		r.setLevel(l);
 		r.addAppenderRef("A1");
 		r.addAppenderRef("A2");
 		r.generateProperties(p);
 
 		testExpected("ALL, A1, A2");
 	}
 
 	@Test
 	public void rootTest3() {
 		l.setValues(Levels.DEBUG);
 		r.setLevel(l);
 		r.addAppenderRef("A1");
 		r.generateProperties(p);
 		
 		testExpected("DEBUG, A1");
 	}
 
 	@Test
 	public void rootTest4() {
 		l.setValues(Levels.FATAL);
 		r.setLevel(l);
 		r.addAppenderRef("A9");
 		r.generateProperties(p);
 		r.isRootCategory(true);
 		
 		expectedKey = "log4j.rootCategory";
 		testExpected("FATAL, A9");
 	}
 
 	@Test
 	public void rootTest5() {
 		l.setValues(Levels.INFO);
 		r.setLevel(l);
 		r.addAppenderRef("A1");
		r.generateProperties(p);
 		
 		testExpected("INFO, A1");
 	}
 	
 	private void testExpected(String value){
 		assertTrue(p.containsKey(expectedKey));
 		assertEquals(value, p.get(expectedKey));
 	}
 
 }
