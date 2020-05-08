 package models;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.mvc.Router;
 import play.test.UnitTest;
 import util.XStreamHelper;
 
 
 public class AppPropsTest extends UnitTest {
 	
 	private AppProps props;
 
 	@Before
 	public void init() {
 		props = new AppProps();
 		props.properties = new ArrayList<Property>();
 		props.properties.add( new Property("alpha", "1"));
 		props.properties.add( new Property("beta", "2"));
 	}
 	
 	//@Test
 	public void testGetConfFile() { 
 		//TODO 
 		fail();
 	}
 	
 	//@Test 
 	public void testGetBundleFolder() { 
 		//TODO 
 		fail();
 		
 	}
 
 	@Test 
 	public void fromXml() {
 		String xml = 
 			"<props>" +
 				"<property name='alpha' value='value' />" + 
 				"<property name='beta' value='other' />" + 
 			"</props>";
 		
 		AppProps props = XStreamHelper.fromXML(xml);
 		assertNotNull(props);
 		assertEquals("value", props.getString("alpha"));
 		assertEquals("other", props.getString("beta"));
 	}
 	
 	@Test 
 	public void testToXml() throws IOException {
 		AppProps props = new AppProps();
 		props.put("param1", "Hola");
 		props.put("param2", "Ciao");
 		
 		String xml = XStreamHelper.toXML(props);
 		BufferedReader reader = new BufferedReader(new StringReader(xml));
 		assertEquals("<props>", reader.readLine());
 		assertEquals("<property name=\"param1\" value=\"Hola\"/>", reader.readLine().trim());
 		assertEquals("<property name=\"param2\" value=\"Ciao\"/>", reader.readLine().trim());
 		assertEquals("</props>", reader.readLine());
 	}
 	
 	@Test 
 	public void testSingletonInstance() {
 		AppProps instance = AppProps.instance();
 		assertNotNull(instance);
 
 		/* being a singleton it MUST be the same instance */
 		AppProps instance2 = AppProps.instance();
 		assertTrue( instance == instance2 );
 		
 	}
 	
 	
 	@Test 
 	public void testGetNames() {
 		List<String> list = props.getNames();
 		assertEquals(2, list.size());
 		assertTrue(list.contains("alpha"));
 		assertTrue(list.contains("beta"));
 	}
 	
 	@Test
 	public void testGetProperty() {
 		assertEquals( "1", props.getString("alpha"));
 		assertEquals( "2", props.getString("beta"));
 		assertEquals( "3", props.get("delta", "3"));
 		assertNull( props.getString("delta"));
 	}	
 
 	@Test 
 	public void testContainsKey() {
 		assertTrue( props.containsKey("alpha") );
 		assertFalse( props.containsKey("xxx") );
 	}
 
 	@Test 
 	public void testAddIfNotExists() {
 		// do not add it because 'alpha' exists ' 
 		assertFalse( props.addIfNotExists("alpha","3") );
 
 		// ADD it because 'delta' does not exists ' 
 		assertTrue( props.addIfNotExists("delta","3") );
 		assertEquals( "3", props.getString("delta") );
 	}
 	
 	@Test
 	public void testGetWebmasterEmail() {
 		props.add("webmasterEmail", "paolo.ditommaso@grg.es");
 		assertEquals("paolo.ditommaso@grg.es", props.getWebmasterEmail());
 	}
 	
 	@Test
 	public void testGetDataPath() {
 		assertEquals( AppProps.WORKSPACE_FOLDER.getAbsolutePath(), props.getDataPath() );
 	}
 	
 	@Test 
 	public void testGetDataCacheDuration() {
		assertEquals(604800, props.getDataCacheDuration());
 
 		// set to 1 seconds 
 		props.put("data.cache.duration", "1s");
 		assertEquals(1, props.getDataCacheDuration());
 
 		// set to 1 minute 
 		props.put("data.cache.duration", "1min");
 		assertEquals(60, props.getDataCacheDuration());
 	
 	} 
 
 	@Test
 	public void testContextPath() { 
 		/* default root context */
 		assertEquals("", props.contextPath);
 		
 		/* simulate a top level context path */
 		Router.routes.add(0, Router.getRoute("GET", "/root/path", "Main.index", null, null));
 		props.contextPath = null; // force re-evaluating
 		assertEquals("/root", props.contextPath);
 
 		Router.routes.remove(0);
 	}
 }
