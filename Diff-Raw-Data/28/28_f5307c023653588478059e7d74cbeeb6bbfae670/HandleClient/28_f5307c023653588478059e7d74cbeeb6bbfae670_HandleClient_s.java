 /*
  * Encog(tm) Core v3.1 - Java Version
  * http://www.heatonresearch.com/encog/
  * http://code.google.com/p/encog-java/
  
  * Copyright 2008-2012 Heaton Research, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *   
  * For more information on Heaton Research copyrights, licenses 
  * and trademarks visit:
  * http://www.heatonresearch.com/copyright
  */
 package org.encog.cloud.indicator.server;
 
 import org.encog.cloud.indicator.IndicatorError;
 import org.encog.cloud.indicator.IndicatorListener;
 import org.encog.util.logging.EncogLogging;
 
 public class HandleClient implements Runnable {
 
 	private IndicatorLink link;
 	private boolean done;
 	private IndicatorServer server;
 	private String userID;
 	private String remoteType = "Unknown";
 	private IndicatorListener listener;
 	
 	public HandleClient(IndicatorServer s, IndicatorLink l, IndicatorListener theListener) {
 		this.link = l;
 		this.server = s;
 		this.listener = theListener;
 	}
 		
 
 	
 	public String getUserID() {
 		return userID;
 	}
 
 	public void setUserID(String userID) {
 		this.userID = userID;
 	}
 
 	@Override
 	public void run() {
 		EncogLogging.log(EncogLogging.LEVEL_DEBUG,"Waiting for packets");
 		this.listener.notifyConnect(this.link);
 		while(!done) {					
 			try {
 				IndicatorPacket packet = this.link.readPacket();
 				
 				// really do not care if we timeout, just keep listening
 				if( packet==null ) {
 					continue;
 				} else {
 					if( packet.getCommand().equalsIgnoreCase("hello") ) {
 						this.remoteType = packet.getArgs()[0];
 					}
 					else if( packet.getCommand().equalsIgnoreCase("goodbye") ) {
 						this.done = true;
 					}
 					else {
 						this.listener.notifyPacket(packet);
 					}
 				}				
 			} catch (IndicatorError ex) {
 				EncogLogging.log(EncogLogging.LEVEL_DEBUG,"Client ended connection.");
 				this.done = true;
 			} 
 		}		
 		this.link.close();
 		this.server.getConnections().remove(this);
 		this.listener.notifyTermination();
 		this.server.notifyListenersConnections(this.link,false);
 		EncogLogging.log(EncogLogging.LEVEL_DEBUG,"Shutting down client handler");
 	}
 	
 	public String getRemoteType() {
 		return this.remoteType;
 	}
 
 
 
 	public IndicatorLink getLink() {
 		return this.link;
 	}
 }
