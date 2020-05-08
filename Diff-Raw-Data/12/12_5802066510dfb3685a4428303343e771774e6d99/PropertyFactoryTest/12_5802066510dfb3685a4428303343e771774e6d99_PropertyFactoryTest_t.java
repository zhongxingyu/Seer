 package gov.usgs.gdp.helper;
 
 import static org.junit.Assert.*;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.junit.Test;
 
 public class PropertyFactoryTest {
 
 	@Test
 	public final void testGetKeys() {
 		Enumeration<Object> result = PropertyFactory.getKeys();
 		assertNotNull(result);
 		assertTrue(result.hasMoreElements());
 	}
 
 	@Test
 	public final void testGetProperty() {
 		String result = PropertyFactory.getProperty("does.not.exist");
 		assertEquals("", result);
 		
 		result = PropertyFactory.getProperty("thredds.url.0");
		assertEquals("NARR (North American Regional Reanalysis);http://nomads.ncdc.noaa.gov/thredds/catalog.html", result);
 	}
 
 	@Test
 	public final void testSetProperty() {
 		String result = PropertyFactory.getProperty("thredds.url.0");
		assertEquals("NARR (North American Regional Reanalysis);http://nomads.ncdc.noaa.gov/thredds/catalog.html", result);
 		PropertyFactory.setProperty("test...test", "...");
 		result = PropertyFactory.getProperty("test...test");
 		assertEquals("...", result);
 	}
 
 	@Test
 	public final void testGetValueList() {
 		List<String> result = null;
 		result = PropertyFactory.getValueList("thredds.url");
 		assertNotNull(result);
 		assertTrue(result.size() > 1);
 	}
 }
