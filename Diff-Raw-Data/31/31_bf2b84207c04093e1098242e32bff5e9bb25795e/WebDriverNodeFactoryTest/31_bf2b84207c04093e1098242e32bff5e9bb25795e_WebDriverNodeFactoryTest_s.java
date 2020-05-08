 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import static org.junit.Assert.fail;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class WebDriverNodeFactoryTest {
 
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 	}
 
 	@Test
 	public void testCreateNodeNull() {
 
 		try {
 			WebDriverNodeFactory.createNode(null);
 			fail();
 		} catch (IllegalArgumentException e) {
 			Assert.assertTrue(true);
 		}
 	}
 
 	@Test
 	public void testCreateNodeNone() {
 
 		try {
 			WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.NONE);
 			fail();
 		} catch (IllegalArgumentException e) {
 			Assert.assertTrue(true);
 		}
 	}
 
 	@Test
	public void testCreateNodeForXPathPrefix() {
 
 
 		for (WebDriverNodeTypeEnum t : WebDriverNodeTypeEnum.values()) {
 			if (t.getPrefixKey() != null) {
 				testCheckerBasedNodeCreation(t);
 			}
 		}
 	}
 
 	private void testCheckerBasedNodeCreation(WebDriverNodeTypeEnum t) {
 
 		WebDriverNode node = null;
 		String msg = null;
		
 		msg = t.name();
 		try {
 			node = WebDriverNodeFactory.createNode(t);
 			Assert.assertNotNull(msg, node);
 			Assert.assertTrue(msg, node instanceof WebDriverNodeCheckerBased);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			Assert.fail(msg + ":" + e.getLocalizedMessage());
 		}
 	}
 
 }
