 /*
  * Copyright 2011 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.webconsole.impl;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 import org.araqne.httpd.WebSocket;
 import org.araqne.msgbus.AbstractSession;
 import org.araqne.msgbus.Message;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class WebSocketSession extends AbstractSession {
 	private final Logger logger = LoggerFactory.getLogger(WebSocketSession.class.getName());
 	private WebSocket socket;
 	private InetSocketAddress local;
 	private InetSocketAddress remote;
 
 	public WebSocketSession(WebSocket socket) {
 		this.socket = socket;
 		this.local = socket.getLocalAddress();
 		this.remote = socket.getRemoteAddress();
 	}
 
 	@Override
 	public InetAddress getLocalAddress() {
 		return local.getAddress();
 	}
 
 	@Override
 	public InetAddress getRemoteAddress() {
 		return remote.getAddress();
 	}
 
 	public void send(Message msg) {
 		String payload = AraqneMessageEncoder.encode(this, msg);
 		if (logger.isDebugEnabled())
 			logger.debug("araqne webconsole: sending [{}]", payload);
 
 		if (socket != null)
 			socket.send(payload);
 	}
 
	public void close() {
 		if (socket != null) {
 			socket.close();
 			socket = null;
 		}
 	}
 
 	@Override
 	public String toString() {
		return "websocket session, guid=" + getGuid() + ", remote=" + socket.getRemoteAddress() + ", lastAccessTime="
 				+ getLastAccessTime();
 	}
 }
