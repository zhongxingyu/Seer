 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tschumacher.rally.el;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import org.junit.After;
 import org.junit.AfterClass;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import tschumacher.rally.el.context.DefaultContext;
 import tschumacher.rally.el.engine.DefaultEngine;
 import tschumacher.rally.el.template.DefaultTemplate;
 
 /**
  *
  * @author Timothy Schumacher, Ph.D. <schumact@gmail.com>
  */
 public class PropertiesTest {
 	
 	public PropertiesTest() {
 	}
 	
 	@BeforeClass
 	public static void setUpClass() {
 	}
 	
 	@AfterClass
 	public static void tearDownClass() {
 	}
 	
 	@Before
 	public void setUp() {
 	}
 	
 	@After
 	public void tearDown() {
 	}
 
 	@Test(expected=tschumacher.rally.el.Exception.class)
 	public void testNull01() {
 		Context context   = getContext(new Properties());
 		Engine  engine    = getEngine();
 		engine.evaluate(null, context);
 	}
 	
 	@Test(expected=tschumacher.rally.el.Exception.class)
 	public void testNull02() {
 		Template template = getTemplate("Hello, ${user}!");
 		Engine  engine    = getEngine();
 		engine.evaluate(template, null);
 	}
 
 	@Test(expected=tschumacher.rally.el.Exception.class)
 	public void testNull03() {
 		Engine  engine    = getEngine();
 		engine.evaluate(null, null);
 	}
 
 	@Test(expected=tschumacher.rally.el.Exception.class)
 	public void testPropertyNotFound01() {
 		Properties properties = new Properties();
 		properties.setProperty("firstName", "Timothy");
 
 		Context context = getContext(properties);
 		Template template = getTemplate("My name is ${firstName} ${lastName}");
 		Engine engine = getEngine();
 		engine.evaluate(template, context);
 	}
 	
 
 	@Test
 	public void test01() {
 		PerformTest(LoadProperties("test01.properties"));
 	}
 	
 	@Test
 	public void test02() {
 		PerformTest(LoadProperties("test02.properties"));
 	}
 
 	@Test
 	public void test03() {
 		PerformTest(LoadProperties("test03.properties"));
 	}
 	
 	@Test
 	public void test04() {
 		PerformTest(LoadProperties("test04.properties"));
 	}
 
 	@Test
 	public void test05() {
 		PerformTest(LoadProperties("test05.properties"));
 	}
 
 	@Test
 	public void test06() {
 		PerformTest(LoadProperties("test06.properties"));
 	}
 
 	@Test
 	public void test07() {
 		PerformTest(LoadProperties("test07.properties"));
 	}
 	
 	@Test
 	public void test08() {
 		PerformTest(LoadProperties("test08.properties"));
 	}
 
 	@Test
 	public void test09() {
 		PerformTest(LoadProperties("test09.properties"));
 	}
 
 	@Test
 	public void test10() {
 		PerformTest(LoadProperties("test10.properties"));
 	}
 
 	@Test
 	public void test11() {
 		Engine engine = new DefaultEngine();
 		Context context = getContext(new Properties());
 		context.setAttribute("test", 11);
 		context.setAttribute("int", 36);
 		Template template = getTemplate("test ${test}: an int = ${int}");
 		String output = engine.evaluate(template, context);
 		System.out.println(output);
 		assertEquals("test 11: an int = 36", output);
 	}
 
 	private static Properties LoadProperties(String resource) {
 		InputStream resourceAsStream = PropertiesTest.class.getClassLoader().getResourceAsStream(resource);
 		Properties properties = new Properties();
 		try {
 			properties.load(resourceAsStream);
 		} catch (IOException ex) {
 			throw new IllegalArgumentException("Could not load " + resource, ex);
 		}
 		return properties;
 	}
 
 	private static void PerformTest(Properties properties) {
 		final String TEMPLATE = properties.getProperty(PROP_TEMPLATE);
 		final String OUTPUT = properties.getProperty(PROP_OUTPUT);
 		
 		// clean out our properties:
 		properties.remove(PROP_OUTPUT);
 		properties.remove(PROP_TEMPLATE);
 
 		Context context   = getContext(properties);
 		Template template = getTemplate(TEMPLATE);
 		Engine  engine    = getEngine();
 		
 		String output = engine.evaluate(template, context);
 
 		/* now compare our desired output with our actual output. */
 		System.out.println(output);
 		assertEquals(OUTPUT, output);
 	}
 
 	private static tschumacher.rally.el.Context getContext(Properties properties) {
 		return new DefaultContext(properties);
 	}
 
 	private static tschumacher.rally.el.Template getTemplate(String TEMPLATE) {
 		return new DefaultTemplate(TEMPLATE);
 	}
 
 	private static tschumacher.rally.el.Engine getEngine() {
 		return new DefaultEngine();
 	}
 
 	private static final String PROP_TEMPLATE = "template";
 	private static final String PROP_OUTPUT   = "output";
 }
