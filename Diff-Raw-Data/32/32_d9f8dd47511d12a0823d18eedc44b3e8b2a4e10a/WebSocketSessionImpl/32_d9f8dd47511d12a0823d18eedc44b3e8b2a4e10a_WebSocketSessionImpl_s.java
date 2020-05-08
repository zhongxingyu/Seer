 /**
  * Copyright 2012 NetDigital Sweden AB
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
 */
 
 package com.nginious.http.websocket;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import com.nginious.http.application.ControllerService;
 import com.nginious.http.server.Connection;
 import com.nginious.http.stats.WebSocketSessionStatistics;
 
 /**
  * A web socket session handles the receiving and sending of messages between a web socket client and server
  * from the time the client connects until one of the endpoints closes the connection.
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 public class WebSocketSessionImpl implements WebSocketSession {
 	
 	private enum State {
 		CONNECTING, OPEN, CLOSING, CLOSED;
 	}
 	
 	private int BUFFER_SIZE = 8192;
 	
 	private int HEADER_SIZE = 4;
 	
 	private ControllerService service;
 	
 	private WebSocketConnection conn;
 	
 	private WebSocketSessionStatistics statistics;
 	
 	private State state;
 	
 	private boolean pinging;
 	
 	/**
 	 * Constructs a new web socket session which uses the specified web socket service for executing
 	 * incoming messages.
 	 * 
 	 * @param statistics the web socket service statistics
 	 */
 	public WebSocketSessionImpl(WebSocketSessionStatistics statistics) {
 		this.statistics = statistics;
 		this.state = State.CONNECTING;
 	}
 	
 	/**
 	 * Sets the web socket service for this session to the specified service.
 	 * 
 	 * @param service the web socket service
 	 */
 	public void setService(ControllerService service) {
 		this.service = service;
 	}
 	
 	/**
 	 * Sets connection to the specified web socket connection for this web socket session.
 	 * 
 	 * @param conn the web socket connection
 	 */
 	void setConnection(WebSocketConnection conn) {
 		this.conn = conn;
 		this.state = State.OPEN;
 		statistics.addNewSession();
 	}
 	
 	/**
 	 * Executes the specified web socket message which calls the appropriate web socket service to
 	 * execute the message.
 	 * 
 	 * @param message the web socket message
 	 */
 	void execute(WebSocketMessage message) {
 		try {
 			Opcode opcode = message.getOpcode();
 			long length = message.length();
 			statistics.addIncomingMessage((int)length);
 			
 			switch(opcode) {
 			case TEXT:
 				if(this.state == State.OPEN) {
 					service.executeTextMessage((WebSocketTextMessage)message, this);
 				}
 				break;
 				
 			case BINARY:
 				if(this.state == State.OPEN) {
 					service.executeBinaryMessage((WebSocketBinaryMessage)message, this);
 				}
 				break;
 			
 			case CLOSE:
 				if(this.state == State.OPEN) {
 					this.state = State.CLOSING;
 					sendClose((WebSocketBinaryMessage)message);
 				} else {
 					this.state = State.CLOSED;
 					conn.close();
 					statistics.addClosedSession();
 				}
 				
 				service.executeClose(this);
 				break;
 				
 			case PING:
 				sendPong((WebSocketBinaryMessage)message);
 				break;
 				
 			case PONG:
 				if(this.pinging) {
 					this.pinging = false;
 				}
 				break;
 				
 			default:
 				sendClose(StatusCode.INTERNAL_SERVER_ERROR, "Received frame with invalid opcode");
 				break;
 			}
 		} catch(IOException e) {
 			conn.close();
 		}
 	}
 	
 	/**
 	 * Sends a close message to the client using the web socket connection associated with this
 	 * web socket session.
 	 * 
 	 * @param message the payload to include in the close message
 	 * @throws IOException if unable to send close
 	 */
 	void sendClose(WebSocketBinaryMessage message) throws IOException {
 		send(Opcode.CLOSE, message.getMessage(), true);
 	}
 	
 	/**
 	 * Sends a pong message in response to a previous ping message from the client. The message
 	 * is sent using the web socket conection associated with this web socket session.
 	 * 
 	 * @param message the payload to include in the close message
 	 * @throws IOException if unable to send close
 	 */
 	void sendPong(WebSocketBinaryMessage message) throws IOException {
 		if(this.state == State.CLOSING) {
 			return;
 		}
 		
 		send(Opcode.PONG, message.getMessage(), false);
 	}
 	
 	/**
 	 * Switches the specified connection to a web socket connection by moving the connections
 	 * underlying socket channel to a new web socket connection.
 	 * 
 	 * @param conn the connection to switch
 	 * @return the new web socket connection
 	 * @throws IOException if unable to switch
 	 */
 	public WebSocketConnection switchFromConnection(Connection conn) throws IOException {
 		WebSocketConnection wsConn = new WebSocketConnection();
 		wsConn.setSession(this);
 		conn.switchConnection(wsConn);
 		setConnection(wsConn);
 		return wsConn;
 	}
 	
 	/**
 	 * Sends a close message to the client with the specified status code and message.
 	 * 
 	 * @param statusCode the status code
 	 * @param msg the message
 	 * @return <code>true</code> if close message was sent, <code>false</code> if connection is already closing
 	 * @throws IOException if an I/O error occurs which the underlying connection
 	 */
 	public boolean sendClose(StatusCode statusCode, String msg) throws IOException {
 		if(this.state == State.CLOSING) {
 			return false;
 		}
 		
 		this.state = State.CLOSING;
 		byte[] msgBytes = msg == null ? new byte[0] : msg.getBytes("utf-8");
 		byte[] data = new byte[2 + msgBytes.length];
 		
 		data[0] = (byte)((statusCode.value & 0xFF00) >> 8);
 		data[1] = (byte)(statusCode.value & 0x00FF);
 		
 		if(msgBytes != null) {
 			System.arraycopy(msgBytes, 0, data, 2, msgBytes.length);
 		}
 		
 		send(Opcode.CLOSE, data, false);
 		return true;
 	}
 	
 	/**
 	 * Sends a ping message to the client with the specified data as payload.
 	 * 
 	 * @param data the data to use as payload
 	 * @return <code>true</code> if ping message sent, <code>false</code> if session is not open
 	 * @throws IOException if an I/O error occurs which the underlying connection
 	 */
 	public boolean sendPing(byte[] data) throws IOException {
 		if(this.state != State.OPEN) {
 			return false;
 		}
 		
 		this.pinging = true;
 		send(Opcode.PING, data, false);
 		return true;
 	}
 	
 	/**
 	 * Sends the specified data as a binary message to the client.
 	 * 
 	 * @param data the data to send
 	 * @return <code>true</code> if message sent, <code>false</code> if session is not open
 	 * @throws IOException if an I/O error occurs which the underlying connection
 	 */
 	public boolean sendBinaryData(byte[] data) throws IOException {
 		if(this.state != State.OPEN) {
 			return false;
 		}
 		
 		send(Opcode.BINARY, data, false);
 		return true;
 	}
 	
 	/**
 	 * Sends the specified text as a text message to the client.
 	 * 
 	 * @param data the data to send
 	 * @return <code>true</code> if message sent, <code>false</code> if session is not open
 	 * @throws IOException if an I/O error occurs which the underlying connection
 	 */
 	public boolean sendTextData(String data) throws IOException {
 		if(this.state != State.OPEN) {
 			return false;
 		}
 		
 		byte[] enc = data.getBytes("utf-8");
 		send(Opcode.TEXT, enc, false);
 		return true;
 	}
 	
 	/**
 	 * Sends a message to the client with the specified opcode, payload data. The message is split into multiple
 	 * frames if necessary. Maximum frame size is 8192 bytes.
 	 * 
 	 * @param opcode the message opcode
 	 * @param data the payload data
 	 * @param queueClose whether or not to close the underlying connection once message has been sent
 	 * @throws IOException if an I/O error occurs which the underlying connection
 	 */
 	private void send(Opcode opcode, byte[] data, boolean queueClose) throws IOException {
 		synchronized(this) {
 			int pos = 0;
 			
 			while(pos < data.length) {
 				boolean finalFrame = data.length - pos <= BUFFER_SIZE - HEADER_SIZE;
 				int payloadLen = data.length - pos > BUFFER_SIZE - HEADER_SIZE ? BUFFER_SIZE - HEADER_SIZE : data.length - pos;
 				int bufferLen = payloadLen > 125 ? payloadLen + 4 : payloadLen + 2;
 				ByteBuffer buffer = ByteBuffer.allocate(bufferLen);
 				
 				byte flags = finalFrame ? (byte)0x80 : (byte)0x00;
 				flags += pos == 0 ? opcode.value : Opcode.CONTINUATION.value;
 				buffer.put(flags);
 				
 				if(payloadLen > 125) {
 					buffer.put((byte)126);
 					buffer.put((byte)((payloadLen & 0x0000FF00) >> 8));
 					buffer.put((byte)(payloadLen & 0x000000FF));
 				} else {
 					buffer.put((byte)payloadLen);
 				}
 				
 				buffer.put(data, pos, payloadLen);
 				buffer.rewind();
 				conn.queueWrite(buffer);
 				pos += payloadLen;
 			}
 			
 			statistics.addOutgoingMessage(data.length);
 			
 			if(queueClose) {
 				conn.close();
 				this.state = State.CLOSED;
 				statistics.addClosedSession();
 			} else {
 				conn.switchToRead();
 			}
 		}
 	}
 }
