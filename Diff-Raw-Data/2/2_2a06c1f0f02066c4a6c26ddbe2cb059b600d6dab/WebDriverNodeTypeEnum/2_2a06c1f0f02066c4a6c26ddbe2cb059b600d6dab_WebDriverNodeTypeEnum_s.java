 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import com.operativus.senacrs.audit.properties.xpath.XPathKeyPrefix;
 import com.operativus.senacrs.audit.properties.xpath.XPathPrefixesEnum;
 
 
 public enum WebDriverNodeTypeEnum implements WebDriverNodeType {
 	
 	NONE(null),
 	START(null),
	STOP(null),
 	DASHBOARD(XPathPrefixesEnum.DASHBOARD),
 	ABOUT(XPathPrefixesEnum.ABOUT),
 	LOGIN(XPathPrefixesEnum.LOGIN),
 	PORTAL(XPathPrefixesEnum.PORTAL),
 	YEAR(null),
 	CLASS(XPathPrefixesEnum.CLASS),
 	GRADES(XPathPrefixesEnum.GRADES),
 	PLAN(XPathPrefixesEnum.PLAN),
 	;
 	
 	private final XPathKeyPrefix prefixKey;
 	
 	private WebDriverNodeTypeEnum(XPathKeyPrefix prefixKey) {
 		
 		this.prefixKey = prefixKey;
 	}
 		
 	public XPathKeyPrefix getPrefixKey() {
 
 		return prefixKey;
 	}
 }
