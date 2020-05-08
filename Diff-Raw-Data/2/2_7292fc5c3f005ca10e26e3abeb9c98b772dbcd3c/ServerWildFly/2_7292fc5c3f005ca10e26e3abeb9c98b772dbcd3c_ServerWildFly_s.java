 package org.jboss.tools.runtime.reddeer.impl;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.jboss.tools.runtime.reddeer.Namespaces;
 
 /**
  * WildFly Server
  * 
  * @author apodhrad
  * 
  */
 @XmlRootElement(name = "wildfly", namespace = Namespaces.SOA_REQ)
 public class ServerWildFly extends ServerAS {
 
 	private final String category = "JBoss Community";
 
 	private final String label = "WildFly";
 
 	@Override
 	public String getCategory() {
 		return category;
 	}
 
 	@Override
 	public String getServerType() {
 		return label + " " + getVersion();
 	}
 
 	@Override
 	public String getRuntimeType() {
		return label + " Runtime " + getVersion();
 	}
 
 }
