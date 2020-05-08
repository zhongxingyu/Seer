 package org.araqne.logdb.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @since 0.8.0
  * @author xeraph
  */
 public class ParserInfo {
 	private String name;
 	private String factoryName;
 	private Map<String, String> configs = new HashMap<String, String>();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getFactoryName() {
 		return factoryName;
 	}
 
 	public void setFactoryName(String factoryName) {
 		this.factoryName = factoryName;
 	}
 
 	public Map<String, String> getConfigs() {
 		return configs;
 	}
 
 	public void setConfigs(Map<String, String> configs) {
 		this.configs = configs;
 	}
 
 	@Override
 	public String toString() {
		return "name=" + name + ", factory=" + factoryName + ", configs=" + configs;
 	}
 }
