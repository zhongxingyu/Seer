 /**
 	Copyright (c) 2011, Strata Health Solutions Inc.
  	All rights reserved.
 
 	Redistribution and use in source and binary forms, with or without modification, are permitted 
 	provided that the following conditions are met:
 
 	Redistributions of source code must retain the above copyright notice, this list of conditions 
 	and the following disclaimer.
 
 	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 	and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
 	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
 	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 	A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 	BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 	OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 	EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
 
 // $codepro.audit.disable fieldJavadoc
 
 package shelob.core;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import javax.annotation.concurrent.Immutable;
 
 /**
  * @author melllaguno
  * @version $Revision: 1.0 $
  *
  * The Application URL. This needs to be configurable to support different application environments
  * that may be used for testing.
  */
 @Immutable
 public class ApplicationURL {
 
 	private static final Integer DEFAULT_HTTPS_PORT = 443;
	private static final String SCHEME = "http://";
 	
 	private final String hostname;
 	private final Integer port;
 	private final String environmentPrefix;
 	private final String url;
 	
 	/**
 	 * Default ApplicationURL Constructor
 	 * 
 	 * @param hostname The hostname associated with the URL
 	 * @param port The port associated with the URL
 	 * @param environmentPrefix The cgi environment which prefixes the application URL
 	 * @param url The application URL
 	 */
 	public ApplicationURL(String hostname, Integer port, String environmentPrefix, String url) {
 		this.hostname = checkNotNull(hostname);
 		this.port = checkNotNull(port);
 		this.environmentPrefix = checkNotNull(environmentPrefix);
 		this.url = checkNotNull(url);
 	}
 	
 	/**
 	 * ApplicationURL Constructor (assumes the default HTTPS port)
 	 * 
 	 * @param hostname The hostname associated with the URL
 	 * @param environmentPrefix The cgi environment which prefixes the application URL
 	 * @param url The application URL
 	 */
 	public ApplicationURL(String hostname, String environmentPrefix, String url) { 
 		this(hostname, DEFAULT_HTTPS_PORT, environmentPrefix, url);
 	}
 	
 	/**
 	 * @return the complete URL as a String 
 	 */
 	public String getURL() {
 		return (! port.equals(DEFAULT_HTTPS_PORT)) ? SCHEME + hostname + ":" + port + getBaseURL()
 											: SCHEME + hostname + getBaseURL();
 	}
 	
 	/**
 	 * @return the hostname component of the ApplicationURL 
 	 */
 	public String getHostname() {
 		return hostname;
 	}
 	
 	/**
 	 * @return the port component of the ApplicationURL 
 	 */
 	public Integer getPort() {
 		return port;
 	}
 	
 	/**
 	 * @return the base url which consists of the environment prefix and url 
 	 */
 	public String getBaseURL() {
 		return "/" + environmentPrefix + "/" + url;
 	}
 	
 	/**
 	 * @return the cgi environment prefix component of hte ApplicationURL 
 	 */
 	public String getEnvironmentPrefix() {
 		return environmentPrefix;
 	}
 	
 	/**
 	 * Method toString.
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return "Application URL : " + getURL();
 	}
 }
