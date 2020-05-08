 package com.flexion.demo.service;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class MongoStoreTest {
 
 	@Test
 	public void testStore() {
 		final ApplicationContext context = new ClassPathXmlApplicationContext("/META-INF/spring/integration/MongoContext.xml");
 
 		Store s = context.getBean("mongo-store", Store.class);
 
 		long b4 = s.count();
		Map<String,Object> thingie = new HashMap<String,Object>();
 		String myval = this.toString();
 		String mykey = "myself" + System.currentTimeMillis();
 		thingie.put(mykey,myval);
 		thingie.put("answer", "42");
		s.storeMap(thingie);
 		try{
 			long after = s.count();
 			assertEquals("store size has increased by one", b4+1, after);
 		} finally {
 			boolean did = s.remove(mykey,myval);
 			assertTrue("removed inserted object", did);
 		}
 	}
 
 }
