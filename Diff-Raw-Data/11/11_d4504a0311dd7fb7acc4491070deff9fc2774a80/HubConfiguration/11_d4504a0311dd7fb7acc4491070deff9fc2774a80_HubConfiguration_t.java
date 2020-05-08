 /*
  * ###
  * Framework Web Archive
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.util;
 
import java.util.List;

 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 public class HubConfiguration {
 
 	private String host;
     private int port;
 	private int newSessionWaitTimeout;
	private List<String> servlets;
 	private String prioritizer;
     private String capabilityMatcher;
     private boolean throwOnCapabilityNotPresent;
     private int nodePolling;
     private int cleanUpCycle;
     private int timeout;
     private int browserTimeout;
     private int maxSession;
 
 	public HubConfiguration() {
 	}
 
 	public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public int getNewSessionWaitTimeout() {
         return newSessionWaitTimeout;
     }
 
     public void setNewSessionWaitTimeout(int newSessionWaitTimeout) {
         this.newSessionWaitTimeout = newSessionWaitTimeout;
     }
 
    public List<String> getServlets() {
         return servlets;
     }
 
    public void setServlets(List<String> servlets) {
         this.servlets = servlets;
     }
 
     public String getPrioritizer() {
         return prioritizer;
     }
 
     public void setPrioritizer(String prioritizer) {
         this.prioritizer = prioritizer;
     }
 
     public String getCapabilityMatcher() {
         return capabilityMatcher;
     }
 
     public void setCapabilityMatcher(String capabilityMatcher) {
         this.capabilityMatcher = capabilityMatcher;
     }
 
     public boolean isThrowOnCapabilityNotPresent() {
         return throwOnCapabilityNotPresent;
     }
 
     public void setThrowOnCapabilityNotPresent(boolean throwOnCapabilityNotPresent) {
         this.throwOnCapabilityNotPresent = throwOnCapabilityNotPresent;
     }
 
     public int getNodePolling() {
         return nodePolling;
     }
 
     public void setNodePolling(int nodePolling) {
         this.nodePolling = nodePolling;
     }
 
     public int getCleanUpCycle() {
         return cleanUpCycle;
     }
 
     public void setCleanUpCycle(int cleanUpCycle) {
         this.cleanUpCycle = cleanUpCycle;
     }
 
     public int getTimeout() {
         return timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 
     public int getBrowserTimeout() {
         return browserTimeout;
     }
 
     public void setBrowserTimeout(int browserTimeout) {
         this.browserTimeout = browserTimeout;
     }
 
     public int getMaxSession() {
         return maxSession;
     }
 
     public void setMaxSession(int maxSession) {
         this.maxSession = maxSession;
     }
     
     public String toString() {
         return new ToStringBuilder(this,
                 ToStringStyle.DEFAULT_STYLE)
                 .append("host", getHost())
                 .append("port", getPort())
                 .append("newSessionWaitTimeout", getNewSessionWaitTimeout())
                 .append("servlets", getServlets())
                 .append("prioritizer", getPrioritizer())
                 .append("capabilityMatcher", getCapabilityMatcher())
                 .append("throwOnCapabilityNotPresent", isThrowOnCapabilityNotPresent())
                 .append("nodePolling", getNodePolling())
                 .append("cleanUpCycle", getCleanUpCycle())
                 .append("timeout", getTimeout())
                 .append("browserTimeout", getBrowserTimeout())
                 .append("maxSession", getMaxSession())
                 .toString();
     }
 }
