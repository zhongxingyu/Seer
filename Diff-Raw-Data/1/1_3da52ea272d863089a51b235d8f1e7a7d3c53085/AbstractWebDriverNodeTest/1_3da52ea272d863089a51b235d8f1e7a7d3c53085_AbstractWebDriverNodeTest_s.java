 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.openqa.selenium.WebDriver;
 
 public class AbstractWebDriverNodeTest {

 	
 	AbstractWebDriverNode node = null;
 	WebDriver driver = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		
 		node = Mockito.mock(AbstractWebDriverNode.class);
 		driver = Mockito.mock(WebDriver.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		node = null;
 		driver = null;
 	}
 
 	@Test
 	public void testVerifyStateNull() {
 
 		AbstractWebDriverNode obj = null;
 
 		obj = new AbstractWebDriverNode() {
 			@Override
 			protected boolean verifyStateConditions(WebDriver driver) {
 			
 				return node.verifyStateConditions(driver);
 			}
 		};
 		try {
 			obj.verifyState(null);
 			Assert.fail();
 		} catch (IllegalArgumentException e) {
 			Assert.assertTrue(true);
 		}
 	}
 
 	@Test
 	public void testVerifyState() {
 
 		AbstractWebDriverNode obj = null;
 
 		obj = new AbstractWebDriverNode() {
 			@Override
 			protected boolean verifyStateConditions(WebDriver driver) {
 			
 				return node.verifyStateConditions(driver);
 			}
 		};
 		obj.verifyState(driver);
 		Mockito.verify(node).verifyStateConditions(driver);
 	}
 }
