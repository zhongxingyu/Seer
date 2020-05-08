 /*
  * Copyright 2013 Eediom Inc.
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
 package org.araqne.logdb.client.http.impl;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.araqne.logdb.client.AbstractLogDbSession;
 import org.araqne.logdb.client.Message;
 import org.araqne.logdb.client.MessageException;
 import org.araqne.logdb.client.Message.Type;
 import org.araqne.websocket.WebSocket;
 import org.araqne.websocket.WebSocketListener;
 import org.araqne.websocket.WebSocketMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 웹소켓 프로토콜을 이용하여 메시지버스 RPC 통신을 구현합니다.
  * 
  * @since 0.5.0
  * @author xeraph@eediom.com
  * 
  */
 public class WebSocketSession extends AbstractLogDbSession implements WebSocketListener {
 	private final Logger logger = LoggerFactory.getLogger(WebSocketSession.class);
 	private Object sendLock = new Object();
 	private WebSocket websocket;
 	private WebSocketBlockingTable table = new WebSocketBlockingTable();
 	private final Timer timer;
 
 	public WebSocketSession(String host, int port) throws IOException {
 		this(host, port, false, 0);
 	}
 	
 	public WebSocketSession(String host, int port, boolean secure) throws IOException {
 		this(host, port, secure, 0);
 	}
 
 	public WebSocketSession(String host, int port, boolean secure, int timeout) throws IOException {
 		URI uri = null;
 		try {
 			String scheme = secure ? "wss://" : "ws://";
 			uri = new URI(scheme + host + ":" + port + "/websocket");
 			this.websocket = new WebSocket(uri, timeout);
 			websocket.addListener(this);
 		} catch (URISyntaxException e) {
 			throw new IllegalArgumentException("invalid host: " + host);
 		}
 
 		this.timer = new Timer("WebSocket [" + uri + "] Ping Timer", true);
 		timer.scheduleAtFixedRate(new PingTask(), new Date(), 2000);
 	}
 
 	@Override
 	public boolean isClosed() {
 		return websocket.isClosed();
 	}
 
 	@Override
 	public Message rpc(Message req, int timeout) throws IOException {
 		WaitingCall call = table.set(req.getGuid());
 
 		String json = MessageCodec.encode(req);
 
 		synchronized (sendLock) {
 			websocket.send(json);
 		}
 
 		// wait response infinitely
 		Message m;
 		try {
 			if (timeout == 0)
 				m = table.await(call);
 			else
 				m = table.await(call, timeout);
 		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted");
 		}
 
 		if (m.getErrorCode() != null)
 			throw new MessageException(m.getErrorCode(), m.getErrorMessage(), m.getParameters());
 
 		return m;
 	}
 
 	@Override
 	public void onMessage(WebSocketMessage msg) {
 		String json = (String) msg.getData();
 		if (json.isEmpty())
 			return;
 
 		Message m = MessageCodec.decode(json);
 		logger.debug("araqne logdb client: received {}", msg.getData());
 
 		if (m.getType() == Type.Response)
 			table.signal(m.getRequestId(), m);
 
 		else if (m.getType() == Type.Trap) {
 			for (TrapListener listener : listeners) {
 				try {
 					listener.onTrap(m);
 				} catch (Throwable t) {
 					logger.error("araqne logdb client: trap listener should not throw any exception", t);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onError(Throwable t) {
 	}
 
 	@Override
 	public void onClose(Throwable t) {
 		for (TrapListener listener : listeners) {
 			try {
 				listener.onClose(t);
 			} catch (Throwable t2) {
 				logger.error("araqne logdb client: trap listener should not throw any exception", t2);
 			}
 		}
 
 		// close for server side reset
 		try {
 			releaseResources();
 		} catch (IOException e) {
 		}
 	}
 
 	@Override
 	public void close() throws IOException {
 		if (isClosed())
 			return;
 
 		releaseResources();
 	}
 
 	private void releaseResources() throws IOException {
 		super.close();
 		timer.cancel();
 		table.close();
 		websocket.close();
 	}
 
 	private class PingTask extends TimerTask {
 
 		@Override
 		public void run() {
 			if (websocket.isClosed())
 				return;
 
 			try {
 				synchronized (sendLock) {
 					// send msgbus ping
 					websocket.send("ping");
 				}
 			} catch (Throwable t) {
 				// ignore ping fail
 			}
 		}
 	}
 }
