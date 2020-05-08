 package de.uniluebeck.itm.servicepublisher;
 
import javax.annotation.Nullable;

 public class ServicePublisherConfig {
 
 	public static final int DEFAULT_PORT = 8080;
 
 	private int port = DEFAULT_PORT;
 
	@Nullable
 	private String shiroIni;
 
 	@SuppressWarnings("unused")
 	public ServicePublisherConfig(final int port) {
		this(port, null);
 	}
 
 	@SuppressWarnings("unused")
	public ServicePublisherConfig(final int port, @Nullable final String shiroIni) {
 		this.port = port;
 		this.shiroIni = shiroIni;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
	@Nullable
 	public String getShiroIni() {
 		return shiroIni;
 	}
 }
