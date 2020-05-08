 package com.rom.server.configuration;
 
 import java.net.InetAddress;
 
 /**
  * Config - class
  * @author mweidele
  */
 public class Config implements IConfig {
 	public static String file = "config.xml";
	private InetAddress serverIp = null;
 	private Integer serverPort = null;
 	
 	public InetAddress getIp() {
		return this.serverIp;
 	}
 	
 	/**
 	 * getPort - method
 	 */
 	public Integer getPort() {
 		return this.serverPort;
 	}
 	
 	
 	/**
 	 * setIp
 	 * @param ip
 	 * @return true if successful else false
 	 */
 	public boolean setIp(InetAddress ip) {
 		boolean ret = false;
		this.serverIp = ip;
 		
		if (this.serverIp != null) {
 			ret = true;
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * setPort
 	 * @param port
 	 * @return
 	 */
 	public boolean setPort(Integer port) {
 		boolean ret = false;
 		this.serverPort = port;
 		
 		if (this.serverPort != null ) {
 			ret = true;
 		}
 
 		return ret;
 	}
 
 }
 
 
