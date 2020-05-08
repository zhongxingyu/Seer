 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.Mockito;
 import org.openqa.selenium.WebDriver;
 
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNode;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeFactory;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeTypeEnum;
 
 public class OpenDashboardTest {
 
 	private WebDriver driver = null;
 	private OpenDashboard edge = null;
 
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 
 	@Before
 	public void setUp() throws Exception {
 
 		this.driver = Mockito.mock(WebDriver.class);
		this.edge = new OpenDashboard(driver);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		this.edge = null;
 		this.driver = null;
 	}
 
 	@Test
 	public void testTraverseStartSource() {
				
 		WebDriverNode node = null;
		
 		node = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.START);
 		this.edge.traverse(node);
 	}
 }
