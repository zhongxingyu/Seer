 package com.pace.base.ui;
 
 import java.io.File;
 
 public class PafServer implements Comparable, Cloneable {
 
 	private String name;
 	private String host;
 	private Integer port;
 	private String homeDirectory;
 	private boolean defaultServer; 
 	private String webappName;
 	private String wsdlServiceName;
 	private Long urlTimeoutInMilliseconds;
 	private String startupFile;
 	private String shutdownFile;
 	private Integer jndiPort;
 	private Long serverStartupTimeoutInMilliseconds;
 	private boolean doesNotPromptOnHotDeploy;
 	private boolean doesNotPromptOnHotRefresh;
 	private boolean https;
 	private String osServiceName;
 	private Integer jmsMessagingPort;
 		
 
 	public PafServer() {		
 	}
 	
 	public PafServer(String name, String host, Integer port) {
 		
 		this(name, host, port, null, false);
 				
 	}
 	
 	public PafServer(String name, String host, Integer port, String homeDirectory) {
 		
 		this(name, host, port, homeDirectory, false);
 				
 	}
 	
 	public PafServer(String name, String host, Integer port, String homeDirectory, boolean defaultServer) {
 		
 		this.name = name;
 		this.host = host;
 		this.port = port;
 		this.homeDirectory = homeDirectory;
 		this.defaultServer = defaultServer;
 		this.https = false;
 		
 	}
 
 	public String getHomeDirectory() {
 		
 		//if not null and doesn't end with file sep
 		if (homeDirectory != null && ! homeDirectory.endsWith(File.separator) ) {
 			
 			//append file sep
 			homeDirectory += File.separator;
 			
 		}
 		
 		return homeDirectory;
 	}
 
 	public void setHomeDirectory(String homeDirectory) {
 		this.homeDirectory = homeDirectory;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Integer getPort() {
 		return port;
 	}
 
 	public void setPort(Integer port) {
 		this.port = port;
 	}
 
 	public int compareTo(Object o) {
 		
 		PafServer otherServer = (PafServer) o;
 		
 		int outcome = 0;
 		
 		if ( otherServer.getName() != null && this.name != null) {
 			outcome = this.name.compareTo(otherServer.getName());			
 		}
 		
 		return outcome;
 	}
 	
 	/**
 	 * @return Returns the host.
 	 */
 	public String getHost() {
 		return host;
 	}
 
 	/**
 	 * @param host The host to set.
 	 */
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	/**
 	 * @return Returns the defaultServer.
 	 */
 	public boolean isDefaultServer() {
 		return defaultServer;
 	}
 
 	/**
 	 * @param defaultServer The defaultServer to set.
 	 */
 	public void setDefaultServer(boolean defaultServer) {
 		this.defaultServer = defaultServer;
 	}
 
 	/**
 	 * @return Returns the webappName.
 	 */
 	public String getWebappName() {
 		return webappName;
 	}
 
 	/**
 	 * @param webappName The webappName to set.
 	 */
 	public void setWebappName(String webappName) {
 		this.webappName = webappName;
 	}
 
 	/**
 	 * @return Returns the wsdlServiceName.
 	 */
 	public String getWsdlServiceName() {
 		return wsdlServiceName;
 	}
 
 	/**
 	 * @param wsdlServiceName The wsdlServiceName to set.
 	 */
 	public void setWsdlServiceName(String wsdlServiceName) {
 		this.wsdlServiceName = wsdlServiceName;
 	}
 
 	public String getCompleteWSDLService() {
 
 		StringBuilder sb = new StringBuilder();
 		
 		if(https){
 			sb.append("https");
 		} else {
 			sb.append("http");
 		}
 		
 		sb.append("://" + host + ":" + port + "/" + webappName);
 		
		if ( ! wsdlServiceName.equals("?wsdl")) {
 			
 			sb.append("/");
 		} 
 		
 		sb.append(wsdlServiceName);		
 		
 		return sb.toString();
 	}
 
 	/**
 	 * @return Returns the jndiPort.
 	 */
 	public Integer getJndiPort() {
 		return jndiPort;
 	}
 
 	/**
 	 * @param jndiPort The jndiPort to set.
 	 */
 	public void setJndiPort(Integer jndiPort) {
 		this.jndiPort = jndiPort;
 	}
 
 	/**
 	 * @return Returns the shutdownFile.
 	 */
 	public String getShutdownFile() {
 		return shutdownFile;
 	}
 
 	/**
 	 * @param shutdownFile The shutdownFile to set.
 	 */
 	public void setShutdownFile(String shutdownFile) {
 		this.shutdownFile = shutdownFile;
 	}
 
 	/**
 	 * @return Returns the startupFile.
 	 */
 	public String getStartupFile() {
 		return startupFile;
 	}
 
 	/**
 	 * @param startupFile The startupFile to set.
 	 */
 	public void setStartupFile(String startupFile) {
 		this.startupFile = startupFile;
 	}
 
 	/**
 	 * @return Returns the serverStartupTimeoutInMilliseconds.
 	 */
 	public Long getServerStartupTimeoutInMilliseconds() {
 		return serverStartupTimeoutInMilliseconds;
 	}
 
 	/**
 	 * @param serverStartupTimeoutInMilliseconds The serverStartupTimeoutInMilliseconds to set.
 	 */
 	public void setServerStartupTimeoutInMilliseconds(
 			Long serverStartupTimeoutInMilliseconds) {
 		this.serverStartupTimeoutInMilliseconds = serverStartupTimeoutInMilliseconds;
 	}
 
 	/**
 	 * @return the urlTimeoutInMilliseconds
 	 */
 	public Long getUrlTimeoutInMilliseconds() {
 		return urlTimeoutInMilliseconds;
 	}
 
 	/**
 	 * @param urlTimeoutInMilliseconds the urlTimeoutInMilliseconds to set
 	 */
 	public void setUrlTimeoutInMilliseconds(Long urlTimeoutInMilliseconds) {
 		this.urlTimeoutInMilliseconds = urlTimeoutInMilliseconds;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public Object clone() throws CloneNotSupportedException {
 		return super.clone();
 	}
 
 	/**
 	 * @return the doesNotPromptOnDeploy
 	 */
 	public boolean isDoesNotPromptOnHotDeploy() {
 		return doesNotPromptOnHotDeploy;
 	}
 
 	/**
 	 * @param doesNotPromptOnDeploy the doesNotPromptOnDeploy to set
 	 */
 	public void setDoesNotPromptOnHotDeploy(boolean doesNotPromptOnHotDeploy) {
 		this.doesNotPromptOnHotDeploy = doesNotPromptOnHotDeploy;
 	}
 
 	/**
 	 * @return the doesNotPromptOnHotRefresh
 	 */
 	public boolean isDoesNotPromptOnHotRefresh() {
 		return doesNotPromptOnHotRefresh;
 	}
 
 	/**
 	 * @param doesNotPromptOnHotRefresh the doesNotPromptOnHotRefresh to set
 	 */
 	public void setDoesNotPromptOnHotRefresh(boolean doesNotPromptOnHotRefresh) {
 		this.doesNotPromptOnHotRefresh = doesNotPromptOnHotRefresh;
 	}
 
 	public void setHttps(boolean https) {
 		this.https = https;
 	}
 
 	public boolean isHttps() {
 		return https;
 	}
 
 	
 
 	public String getOsServiceName() {
 		return osServiceName;
 	}
 
 	public void setOsServiceName(String osServiceName) {
 		this.osServiceName = osServiceName;
 	}
 
 	public Integer getJmsMessagingPort() {
 		return jmsMessagingPort;
 	}
 
 	public void setJmsMessagingPort(Integer jmsMessagingPort) {
 		this.jmsMessagingPort = jmsMessagingPort;
 	}
 
 	@Override
 	public String toString() {
 		return "PafServer ["
 				+ (name != null ? "name=" + name + ", " : "")
 				+ (host != null ? "host=" + host + ", " : "")
 				+ (port != null ? "port=" + port + ", " : "")
 				+ (homeDirectory != null ? "homeDirectory=" + homeDirectory
 						+ ", " : "")
 				+ "defaultServer="
 				+ defaultServer
 				+ ", "
 				+ (webappName != null ? "webappName=" + webappName + ", " : "")
 				+ (wsdlServiceName != null ? "wsdlServiceName="
 						+ wsdlServiceName + ", " : "")
 				+ (urlTimeoutInMilliseconds != null ? "urlTimeoutInMilliseconds="
 						+ urlTimeoutInMilliseconds + ", "
 						: "")
 				+ (startupFile != null ? "startupFile=" + startupFile + ", "
 						: "")
 				+ (shutdownFile != null ? "shutdownFile=" + shutdownFile + ", "
 						: "")
 				+ (jndiPort != null ? "jndiPort=" + jndiPort + ", " : "")
 				+ (serverStartupTimeoutInMilliseconds != null ? "serverStartupTimeoutInMilliseconds="
 						+ serverStartupTimeoutInMilliseconds + ", "
 						: "")
 				+ "doesNotPromptOnHotDeploy="
 				+ doesNotPromptOnHotDeploy
 				+ ", doesNotPromptOnHotRefresh="
 				+ doesNotPromptOnHotRefresh
 				+ ", https="
 				+ https
 				+ ", "
 				+ (osServiceName != null ? "osServiceName=" + osServiceName + ", "
 						: "")
 				+ (jmsMessagingPort != null ? "jmsMessagingPort="
 						+ jmsMessagingPort : "") + "]";
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		PafServer other = (PafServer) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 	
 	
 }
