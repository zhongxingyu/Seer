 package com.megalogika.sv.service;
 
 import com.megalogika.sv.model.Product;
 
 public class EmailConfig {
 	static final String LOGIN_URL = "/login";
 	static final String PRODUCT_URL = "/product?id=";
 
 	private String siteUrlBase = "http://se.inbelly.com/spring";
 	private String fromEmail = "team@se.inbelly.com";
 	private String fromName = "InBelly (http://se.inbelly.com)";
 	private String problemsEmail = "susikaupk@gmail.com";
 
 	public String getFromEmail() {
 		return fromEmail;
 	}
 
 	public void setFromEmail(String fromEmail) {
 		this.fromEmail = fromEmail;
 	}
 
 	public String getFromName() {
 		return fromName;
 	}
 
 	public void setFromName(String fromName) {
 		this.fromName = fromName;
 	}
 
 	public String getProblemsEmail() {
 		return problemsEmail;
 	}
 
 	public void setProblemsEmail(String problemsEmail) {
 		this.problemsEmail = problemsEmail;
 	}
 
 	String getProductLink(EmailActions emailActions, Product product) {
		return product.getName() + " - " + emailActions.emailConfig.getProductUrl(product);
 	}
 
 	String getProductUrl(Product product) {
 		return siteUrlBase + EmailConfig.PRODUCT_URL + product.getId();
 	}
 
 	public String getSiteUrlBase() {
 		return siteUrlBase;
 	}
 
 	public void setSiteUrlBase(String siteUrlBase) {
 		this.siteUrlBase = siteUrlBase;
 	}
 
 	public Object getLoginUrl() {
 		return siteUrlBase + LOGIN_URL;
 	}
 }
