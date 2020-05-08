 package de.uniluebeck.itm.servicepublisher;
 
 public class ServicePublisherConfig {
 
 	public static final int DEFAULT_PORT = 8080;
 
 	private int port = DEFAULT_PORT;
 
 	private String shiroIni;
 
 	@SuppressWarnings("unused")
 	public ServicePublisherConfig(final int port) {
		this(port, "classpath:shiro.ini");
 	}
 
 	@SuppressWarnings("unused")
	public ServicePublisherConfig(final int port, final String shiroIni) {
 		this.port = port;
 		this.shiroIni = shiroIni;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public String getShiroIni() {
 		return shiroIni;
 	}
 }
