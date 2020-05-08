 package com.operativus.senacrs.audit.properties.xpath;
 
 public enum XPathParamKeyEnum implements XPathParamKey {
 
 	PORTAL_YEAR_SELECT("param.xpath.portal.year.select", 1),
 	PORTAL_YEAR_LIST("param.xpath.portal.year.list", 1),
	PORTAL_YEAR_IN_CLASSES("param.xpath.portal.year.in_classes", 1), ;
 
 	private final String key;
 	private final int paramAmount;
 
 	private XPathParamKeyEnum(final String key, final int paramAmount) {
 
 		this.key = key;
 		this.paramAmount = paramAmount;
 	}
 
 	@Override
 	public int paramAmount() {
 
 		return this.paramAmount;
 	}
 
 	@Override
 	public String getKey() {
 
 		return this.key;
 	}
 }
