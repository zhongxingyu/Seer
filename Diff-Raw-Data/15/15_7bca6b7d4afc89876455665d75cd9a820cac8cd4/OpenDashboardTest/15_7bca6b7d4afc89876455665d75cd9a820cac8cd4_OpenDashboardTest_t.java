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
 import com.operativus.senacrs.audit.testutils.TestBoilerplateUtils;
 
 public class OpenDashboardTest {
 
	private String baseUrl = null;
 	private WebDriver driver = null;
 	private OpenDashboard edge = null;
 
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 
 	@Before
 	public void setUp() throws Exception {
 
		this.baseUrl = TestBoilerplateUtils.randomAlphanumericString();
 		this.driver = Mockito.mock(WebDriver.class);
		this.edge = new OpenDashboard(this.driver, this.baseUrl);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		this.edge = null;
 		this.driver = null;
		this.baseUrl = null;
 	}
 
 	@Test
 	public void testNullBaseUrl() {
 
 		this.thrown.expect(IllegalArgumentException.class);
 		new OpenDashboard(this.driver, null);
 	}
 
 	@Test
 	public void testEmptyBaseUrl() {
 
 		this.thrown.expect(IllegalArgumentException.class);
 		new OpenDashboard(this.driver, "");
 	}
 
 	@Test
 	public void testTraverseNonStartSource() {
 
 		WebDriverNode node = null;
 
 		this.thrown.expect(IllegalSourceNodeTypeException.class);
 		this.thrown.expectMessage(WebDriverNodeTypeEnum.START.toString());
 		node = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.ABOUT);
 		this.edge.traverse(node);
 	}
 
 	@Test
 	public void testTraverseStartSource() {
 
 		WebDriverNode node = null;
 
 		node = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.START);
 		this.edge.traverse(node);
		Mockito.verify(this.driver).get(this.baseUrl);
 	}
 }
