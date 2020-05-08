 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import org.openqa.selenium.WebDriver;
 
import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.graph.nodes.Node;
import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNode;
 
 public class AbstractWebDriverEdge
 		implements WebDriverEdge {
 
 	private final WebDriver driver;
 
 	public AbstractWebDriverEdge(final WebDriver driver) {
 
 		super();
 
		if (driver == null) {
			throw RuntimeExceptionFactory.getNullArgumentException("driver");
		}
 		this.driver = driver;
 	}
 
 	@Override
 	public WebDriver getWebDriver() {
 
 		return this.driver;
 	}
 
 	@Override
 	public void traverse(final Node source) {
 
		WebDriverNode castedNode = null;

		if (source == null) {
			throw RuntimeExceptionFactory.getNullArgumentException("source");
		}
		if (!(source instanceof WebDriverNode)) {
			throw new IllegalSourceNodeClassException(source);
		}
		castedNode = (WebDriverNode) source;
		internTraverse(castedNode);
	}

	private void internTraverse(final WebDriverNode source) {

		// TODO Auto-generated method stub

 	}
 }
