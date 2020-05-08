 /**
  * 
  */
 package shtaag.network.curl.impl;
 
 import java.io.Serializable;
 import java.util.Map;
 import java.util.Queue;
 
 import shtaag.network.curl.framework.OutputWriter;
 
 /**
  * @author takei_s
  * @date 2011/12/25
  */
 public class Command implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private final Map<String, String> headers;
 	private final String command;
 	private Queue<UrlFileHandling> urlFilehandling;
 	private final boolean isVerbose;
 	private final boolean proxy;
 
 	private final OutputWriter writer;
 
 
 	/**
 	 * @param headers
 	 * @param command
 	 * @param urlFilehandling
 	 * @param writer 
 	 * @param isVerbose
 	 * @param proxy
 	 */
 	public Command(Map<String, String> headers, String command,
 			Queue<UrlFileHandling> urlFilehandling, OutputWriter writer, boolean isVerbose, boolean proxy) {
 		
 		this.headers = headers;
 		this.command = command;
 		this.urlFilehandling = urlFilehandling;
 		this.writer = writer;
 		this.isVerbose = isVerbose;
 		this.proxy = proxy;
 	}
 
 	/**
 	 * @return the headers
 	 */
 	public Map<String, String> getHeaders() {
 		return headers;
 	}
 
 	/**
 	 * @return the command
 	 */
 	public String getCommand() {
 		return command;
 	}
 
 
 	/**
 	 * @return the urlFilehandling
 	 */
 	public Queue<UrlFileHandling> getUrlFilehandling() {
 		return urlFilehandling;
 	}
 
 	/**
 	 * @param urlFilehandling the urlFilehandling to set
 	 */
 	public void setUrlFilehandling(Queue<UrlFileHandling> urlFilehandling) {
 		this.urlFilehandling = urlFilehandling;
 	}
 
 	/**
 	 * @return the isVerbose
 	 */
 	public boolean isVerbose() {
 		return isVerbose;
 	}
 
 	/**
 	 * @return the proxy
 	 */
 	public boolean isProxy() {
 		return proxy;
 	}
 
 	/**
 	 * @return the writer
 	 */
 	public OutputWriter getWriter() {
 		return writer;
 	}
 	
 }
