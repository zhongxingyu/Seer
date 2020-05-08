 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import static org.junit.Assert.fail;
 
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
 	public void testCreateNodeForXPathPrefix() {
 		
 		WebDriverNode node = null;
 
 		for (WebDriverNodeTypeEnum t : WebDriverNodeTypeEnum.values()) {
 			if (t.getPrefixKey() != null) {
 				node = WebDriverNodeFactory.createNode(t);
 				Assert.assertNotNull(node);
 				Assert.assertTrue(node instanceof WebDriverNodeCheckerBased);
 			}
 		}
 	}
 
 }
