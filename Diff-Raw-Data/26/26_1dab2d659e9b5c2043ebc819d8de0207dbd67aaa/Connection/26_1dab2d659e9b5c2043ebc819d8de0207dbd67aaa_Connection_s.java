 package net.sourceforge.guacamole.net.cvp;
 
 import javax.xml.bind.annotation.XmlElement;
 
 public class Connection implements java.io.Serializable {
 
 	private static final long serialVersionUID = 8896923818025706326L;
 	
 	private String name;
 	
 	private String protocol;
 	
 	private String host;
 	
 	private Integer port;
 	
 	private String password;
 
 	public String getName() {
 		return name;
 	}
 
 	@XmlElement(name="name")
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getProtocol() {
 		return protocol;
 	}
 
 	@XmlElement(name="protocol")
 	public void setProtocol(String protocol) {
 		this.protocol = protocol;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	@XmlElement(name="host")
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public Integer getPort() {
 		return port;
 	}
 
 	@XmlElement(name="port")
 	public void setPort(Integer port) {
 		this.port = port;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	@XmlElement(name="password")
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 }
