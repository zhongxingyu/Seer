 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import org.openqa.selenium.WebDriver;
 
 import com.operativus.senacrs.audit.graph.nodes.Node;
 
 public class AbstractWebDriverEdge
 		implements WebDriverEdge {
 
 	private final WebDriver driver;
 
 	public AbstractWebDriverEdge(final WebDriver driver) {
 
 		super();
 
 		this.driver = driver;
 	}
 
 	@Override
 	public WebDriver getWebDriver() {
 
 		return this.driver;
 	}
 
 	@Override
 	public void traverse(final Node source) {
 
 	}
 }
