 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import com.operativus.senacrs.audit.properties.xpath.XPathKeyPrefix;
 
 
 public enum WebDriverNodeTypeEnum implements WebDriverNodeType {
 	
 	NONE(null),
 	START(null),
 	STOP(null),
	DASHBOARD(null),
	LOGIN(null),
	PORTAL(null),
 	YEAR(null),
	CLASS(null),
	GRADES(null),
	PLAN(null),
 	;
 	
 	private final XPathKeyPrefix prefixKey;
 	
 	private WebDriverNodeTypeEnum(XPathKeyPrefix prefixKey) {
 		
 		this.prefixKey = prefixKey;
 	}
 		
 	public XPathKeyPrefix getPrefixKey() {
 
 		return prefixKey;
 	}
 }
