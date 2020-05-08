 package com.madalla;
 
 /**
  * @author Eugene Malan
  *
  */
public final class BuildInformation {
 
 	private String version;
 	private String webappVersion;
 
 	/**
 	 * @param version
 	 */
 	public void setVersion(final String version) {
 		this.version = version;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * @param webappVersion
 	 */
 	public void setWebappVersion(final String webappVersion) {
 		this.webappVersion = webappVersion;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getWebappVersion() {
 		return webappVersion;
 	}
 
 	@Override
 	public String toString() {
 		return super.toString();
 	}
 
 }
