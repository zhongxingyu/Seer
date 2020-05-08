 package com.github.rabid_fish.proxy.mock;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class MockUrlHelperTest {
 
 	public static final String TEST_JSON_PATH = "/html/example_url.json";
	public static final String TEST_URL = "/form/example1";
	public static final String TEST_BAD_URL = "/form/bad";
 	
 	MockMapUrlHelper helper;
 
 	@Before
 	public void setUp() {
 		helper = new MockMapUrlHelper(TEST_JSON_PATH);
 	}
 	
 	@Test
 	public void getKeyForRequestUrl() {
 		String key = helper.getKeyForRequestUrl(TEST_URL);
 		assertNotNull(key);
 	}
 	
 	@Test
 	public void getKeyForRequestUrlWithBadUrl() {
 		String key = helper.getKeyForRequestUrl(TEST_BAD_URL);
 		assertNull(key);
 	}
 
 	@Test
 	public void getBodyForKey() throws Exception {
 		String body = helper.getMockBodyForUrl(TEST_URL);
 		assertNotNull(body);
 	}
 
 }
