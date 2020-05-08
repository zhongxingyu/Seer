 package net.ech.config;
 
 public class Bean implements IBean
 {
 	private String property;
 	public Bean() {}
 	public Bean(String property) { setProperty(property); }
 	public String getProperty() { return property; }
 	public void setProperty(String property) { this.property = property; }
 }
