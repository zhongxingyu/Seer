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
 import com.operativus.senacrs.audit.model.config.Configuration;
 import com.operativus.senacrs.audit.testutils.TestBoilerplateUtils;
 
 public class OpenDashboardTest {
 
 	private WebDriver driver = null;
 	private OpenDashboard edge = null;
 	private Configuration config = null;
 
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 
 	@Before
 	public void setUp() throws Exception {
 
 		this.driver = Mockito.mock(WebDriver.class);
 		this.edge = new OpenDashboard(this.driver);
 		this.config = Mockito.mock(Configuration.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		this.edge = null;
 		this.driver = null;
 		this.config = null;
 	}
 
 	@Test
 	public void testTraverseNonStartSource() {
 
 		WebDriverNode node = null;
 
		this.thrown.expect(IllegalSourceNodeClassException.class);
 		this.thrown.expectMessage(WebDriverNodeTypeEnum.START.toString());
 		node = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.ABOUT);
 		this.edge.traverse(node);
 	}
 
 	@Test
 	public void testTraverseStartSource() {
 
 		WebDriverNode node = null;
 		String baseUrl = null;
 
 		baseUrl = TestBoilerplateUtils.randomString();
 		node = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.START);
 		this.edge.traverse(node);
 		Mockito.when(this.config.getBaseUrl()).thenReturn(baseUrl);
 		Mockito.verify(this.config).getBaseUrl();
 		Mockito.verify(this.driver).get(baseUrl);
 	}
 }
