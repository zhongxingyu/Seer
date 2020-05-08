 package org.kevoree.library.javase.basicGossiper;
 
 import java.util.List;
 
 public interface GossiperComponent {
 
 	public List<String> getAddresses(String remoteNodeName);
 
 	public int parsePortNumber(String nodeName);
 
 	public String getName();
 
 	public String getNodeName();
 
 	public void localNotification(Object data);
 }
