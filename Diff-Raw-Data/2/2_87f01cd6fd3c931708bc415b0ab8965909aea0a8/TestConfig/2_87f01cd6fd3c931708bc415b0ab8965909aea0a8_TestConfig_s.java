 package org.siraya.rent.utils;
 
 import org.junit.Test;
 import org.junit.Before;
 import java.util.Map;
 import java.util.List;
 import junit.framework.Assert;
 public class TestConfig {
 	ApplicationConfig config;
 	
 	@Before
 	public void setUp() throws Exception{
 		config = new ApplicationConfig();	
 	}
 	
 	@Test 
 	public void testGetMobileSetting(){
 		Map<String,Object> map= config.get("mobile_country_code");
 		Assert.assertEquals("TW", ((Map<String,Object>)map.get(new Integer(886))).get("country"));
 	}
 	@Test 
 	public void testGetGeneralSetting(){
 		int retryLimit = (Integer)config.get("general").get("auth_retry_limit");
 		Assert.assertEquals(3, retryLimit);
 	}
 	@Test 
 	public void testGetFilterExclude(){
 		List<String> exclude =  (List<String>)config.get("filter").get("exclude");		
		Assert.assertEquals("^/rest/sently_callback", exclude.get(0));
 	}
 }
