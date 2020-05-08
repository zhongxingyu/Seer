 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import static org.junit.Assert.fail;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.graph.nodes.Node;
 
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
 	public void testCreateNodeStart() {
 
 		WebDriverNode result = null;
 
 		result = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.START);
 		Assert.assertNotNull(result);
 		Assert.assertEquals(Node.START.toString(), result.toString());
 	}
 
 	@Test
 	public void testCreateNodeEnd() {
 
 		WebDriverNode result = null;
 
 		result = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.END);
 		Assert.assertNotNull(result);
 		Assert.assertEquals(Node.END.toString(), result.toString());
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
