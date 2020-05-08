 package net.sf.testium.executor.webdriver;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import net.sf.testium.configuration.SeleniumConfiguration.BROWSER_TYPE;
 
 public class BrowserTypeTester extends TestCase 
 {
 	@Test
 	public void testcase_BrowserType_null()
 	{
		BROWSER_TYPE type = BROWSER_TYPE.valueOf(BROWSER_TYPE.class, null);
 		assertEquals(BROWSER_TYPE.HTMLUNIT.toString(), type.toString());
 	}
 
 	@Test
 	public void testcase_BrowserType_empty()
 	{
		BROWSER_TYPE type = BROWSER_TYPE.valueOf(BROWSER_TYPE.class, "");
 		assertEquals(BROWSER_TYPE.HTMLUNIT.toString(), type.toString());
 	}
 
 	@Test
 	public void testcase_BrowserType_invalid()
 	{
		BROWSER_TYPE type = BROWSER_TYPE.valueOf(BROWSER_TYPE.class, "invalid");
 		assertEquals(BROWSER_TYPE.HTMLUNIT.toString(), type.toString());
 	}
 
 	@Test
 	public void testcase_BrowserType_firefox()
 	{
		BROWSER_TYPE type = BROWSER_TYPE.valueOf(BROWSER_TYPE.class, "firefox");
 		assertEquals(BROWSER_TYPE.FIREFOX.toString(), type.toString());
 	}
 }
