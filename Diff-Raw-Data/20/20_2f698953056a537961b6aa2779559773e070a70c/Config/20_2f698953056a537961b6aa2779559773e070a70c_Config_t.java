 package com.rom.server.configuration;
 
 import java.net.InetAddress;
 
 /**
  * Config - class
  * @author mweidele
  */
 public class Config implements IConfig {
 	public static String file = "config.xml";
	private String stringIP = null;
 	private Integer serverPort = null;
 	
 	public InetAddress getIp() {
		InetAddress inetIP = InetAddress.getByName(this.stringIP);

		return inetIP;
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
		this.stringIP = ip.toString();
 		
		if (this.stringIP != null) {
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
 
 
