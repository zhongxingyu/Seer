 package com.operativus.senacrs.audit.properties.xpath;
 
 public enum XPathPrefixesEnum implements XPathKeyPrefix {
 
 	DASHBOARD("xpath.dashboard", 0),
 	ABOUT("xpath.about", 0),
 	LOGIN("xpath.login", 0),
 	PORTAL("xpath.portal", 0),
 	CLASS("xpath.class", 0),
 	GRADES("xpath.grades", 0),
	PLAN("xpath.plan", 0), ;
 
 	private final String keyPrefix;
 	private final int paramAmount;
 
 	private XPathPrefixesEnum(final String keyPrefix, final int paramAmount) {
 
 		this.keyPrefix = keyPrefix;
 		this.paramAmount = paramAmount;
 	}
 
 	@Override
 	public String getKeyPrefix() {
 
 		return this.keyPrefix;
 	}
 	
 	@Override
 	public int paramAmount() {
 
 		return paramAmount;
 	}
 }
