 /*
  * Copyright (C) 2010 Daniel Jacobi
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.questmaster.fatremote.datastructures;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 /**
  * This class represents a FAT+ device and its configuration data.
  * 
  * @author daniel
  *
  */
 public class FATDevice {
 	/**
 	 * Default port on FAT+ for remote service.
 	 */
 	public static final int FAT_REMOTE_PORT = 9999;
 	
 	/**
 	 * Name of this entry
 	 */
 	private String name = "";
 	/**
 	 * IP of device
 	 */
 	private String ip = null;
 	/**
 	 * Port of device
 	 */
 	private int port = FAT_REMOTE_PORT;
 	/**
 	 * Flag if the device was entered manually or discovered by this application.
 	 */
 	private boolean autoDetected = false;
 
 	/**
 	 * Create a new FAT+ device.
 	 * 
 	 * @param name Name of the device
 	 * @param ip IP of the device
 	 * @param auto Autodetected flag
 	 */
 	public FATDevice(String name, InetAddress ip, boolean auto) {
 		this.name = name;
 		this.ip = ip.getHostAddress();
 		this.autoDetected = auto;
 	}
 
 	/**
 	 * Create a new FAT+ device.
 	 * 
 	 * @param name Name of the device
 	 * @param ip IP of the device
 	 * @param auto Autodetected flag
 	 */
 	public FATDevice(String name, String ip, boolean auto) throws UnknownHostException {
 		this.name = name;
 		this.ip = InetAddress.getByName(ip).getHostAddress();
 		this.autoDetected = auto;
 	}
 
 	/**
 	 * Get name of the device.
 	 * @return Device name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set name of the device.
 	 * 
 	 * @param name Device name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Get IP as String of the device.
 	 * 
 	 * @return IP as String
 	 */
 	public String getIp() {
 		return ip;
 	}
 
 	/**
 	 * Set IP of the device as String.
 	 * 
 	 * @param ip Device IP
 	 * @throws UnknownHostException
 	 */
 	public void setIp(String ip) throws UnknownHostException {
 		if (InetAddress.getByName(ip) != null) {
 			this.ip = ip;
 		}
 	}
 
 	/**
 	 * Get IP as InetAddress of the device.
 	 * 
 	 * @return IP as InetAdress
 	 */
 	public InetAddress getInetAddress() throws UnknownHostException {
 		return InetAddress.getByName(ip);
 	}
 
 	/**
 	 * Set IP of the device as InetAddress.
 	 * 
 	 * @param ip Device IP
 	 */
 	public void setInetAdress(InetAddress ip) {
 		this.ip = ip.getHostAddress();
 	}
 
 	/**
 	 * Get port of device.
 	 * 
 	 * @return Device port
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/** 
 	 * Set port of device.
 	 * 
 	 * @param port Device port
 	 */
 	public void setPort(int port) {
 		if (port > 0 && port <= 65535) {
 			this.port = port;
 		} else {
 			this.port = 0;
 		}
 	}
 
 	/**
 	 * Check if device was automatically discovered.
 	 * 
 	 * @return true - Automatically discovered, false - else
 	 */
 	public boolean isAutoDetected() {
 		return autoDetected;
 	}
 
 	/**
 	 * Set if device was automatically discovered.
 	 * 
 	 * @param autoDetected Autodetected device
 	 */
 	public void setAutoDetected(boolean autoDetected) {
 		this.autoDetected = autoDetected;
 	}
 	
 	/**
 	 * Returns a string representation of the object. In general, 
 	 * the {@code toString} method returns a string that "textually represents" 
 	 * this object. The result should be a concise but informative 
 	 * representation that is easy for a person to read.
      *
 	 * The {@code toString} method for class {@code FATDevice} returns a 
 	 * string consisting of the name of the device entry, its IP and its port.
 	 * In other words, this method returns a string equal to the value of:
      *
 	 * {@code getName() + ";" + getIp() + ":" + getPort()}
 	 *
 	 * @returns a string representation of the object.
 	 */
 	@Override
 	public String toString() {
 		return this.getName() + ";" + this.getIp() + ":" + this.getPort();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 *
      * @param other {@inheritDoc}
 	 * @returns {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object other) {
 		if (this == other) {
 			return true;
 		}
 		
 		if ( other instanceof FATDevice) {
 			FATDevice dev = (FATDevice) other;
			if ( this.getIp() == dev.getIp()
 				&& this.getName().equals(dev.getName())
 				&& this.getPort() == dev.getPort()) {
 					return true;
 				}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @returns {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		return this.getName().hashCode() 
 			+ this.getIp().hashCode()
 			+ Integer.valueOf(this.getPort()).hashCode();
 	}
 }
