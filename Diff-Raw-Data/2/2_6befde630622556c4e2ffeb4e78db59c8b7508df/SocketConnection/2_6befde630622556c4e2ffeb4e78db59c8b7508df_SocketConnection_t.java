 /*******************************************************************************
  * Copyright (C) 2012 Henrik Voß und Sven Nobis
  * 
  * This file is part of DSI8AndroidCommunicationLibrary
  * (https://github.com/SvenTo/DSI8AndroidCommunicationLibrary)
  * 
  * DSI8AndroidCommunicationLibrary is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  ******************************************************************************/
 package de.dsi8.dsi8acl.connection.impl;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Looper;
 
 import com.fasterxml.jackson.core.JsonGenerator.Feature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 
 import de.dsi8.dsi8acl.connection.contract.IRemoteConnection;
 import de.dsi8.dsi8acl.connection.contract.IRemoteConnectionListener;
 import de.dsi8.dsi8acl.connection.contract.ISocket;
 import de.dsi8.dsi8acl.connection.contract.ISocketProtocol;
 import de.dsi8.dsi8acl.connection.model.ConnectionParameter;
 import de.dsi8.dsi8acl.connection.model.Message;
 
 /**
  * This class handles the communication with the other Android device (Host or Client).
  * It serialize the message to send into JSON
  * and deserialize the received messages.
  * @author sven
  */
 public class SocketConnection implements IRemoteConnection {
 	/**
 	 *  A TCP socket to the other Android device (Host/Client).
 	 */
 	private final ISocket socket;
 	/**
 	 * Will be informed, when something happen here.
 	 * @see IRemoteConnectionListener
 	 */
 	private IRemoteConnectionListener listener;
 	/**
 	 * @see MessageListenerThread
 	 */
 	private final MessageListenerThread messageListenerThread;
 	/**
 	 * For (De-)Serialization of {@link Message}s into JSON.
 	 */
 	private final ObjectMapper jsonMapper = new ObjectMapper();
 	
 	/**
 	 * Default Constructor.
 	 * @param dependencyContainer The {@link Socket}
 	 * @param listener Will be informed, when something happen here.
 	 */
 	public SocketConnection(ISocket socket) {
 		this.socket = socket;
 		jsonMapper.configure(Feature.AUTO_CLOSE_TARGET, false);
 		jsonMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
 		jsonMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
 		messageListenerThread = new MessageListenerThread(this, new Handler(Looper.getMainLooper()));
 	}
 	
 	static {
 		protocolList = Collections.synchronizedList(new ArrayList<ISocketProtocol>());
 	}
 	
 	private static final List<ISocketProtocol> protocolList;
 	
 	public static void registerProtocol(ISocketProtocol protocol)
 	{
 		if(protocol != null) {
 			protocolList.add(protocol);
 		}
 	}
 	
	public static SocketConnection connect(ConnectionParameter connectionParameter)
 			throws UnknownHostException, IOException, IllegalArgumentException
 	{
 		for(ISocketProtocol protocol : protocolList) {
 			if(protocol.getProtocolName().equals(connectionParameter.getProtocol())) {
 				return new SocketConnection(protocol.connect(connectionParameter));
 			}
 		}
 		throw new IllegalArgumentException(connectionParameter.getProtocol() + " is unknown.");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void startMessageListener() {
 		messageListenerThread.start();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void close() throws IOException {
 		messageListenerThread.interrupt();
 		if(!socket.isClosed())
 		{
 			socket.close();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void sendMessage(Message message) throws IOException {
 		AsyncTask<Message, Integer, Integer> asyncTask = new AsyncTask<Message, Integer, Integer>() {
 			@Override
 			protected Integer doInBackground(Message... params) {
 				try {
 					jsonMapper.writeValue(socket.getOutputStream(), params[0]);
 				} catch (IOException ex) {
 					connectionProblem(ex);
 				}
 				return 0;
 			}
 		};
 		asyncTask.execute(message);
 	}
 	
 	/**
 	 * Inform the {@link IRemoteConnectionListener} about a new message.
 	 * @param message a new message.
 	 */
 	void messageRecived(Message message) {
 		listener.messageReceived(message);
 	}
 	
 	/**
 	 * Inform the {@link IRemoteConnectionListener} that something went wrong.
 	 * @param e What went wrong.
 	 */
 	void connectionProblem(Exception e) {
 		if(listener != null) {
 			listener.connectionProblem(e);
 		}
 	}
 	
 	/**
 	 * For (De-)Serialization of {@link Message}s into JSON.
 	 * @return A {@link ObjectMapper}
 	 */
 	ObjectMapper getJsonObjectMapper() {
 		return jsonMapper;
 	}
 	
 	/**
 	 * Connection to the partner.
 	 * @return A {@link Socket}
 	 */
 	ISocket getSocket() {
 		return socket;
 	}
 
 	@Override
 	public void setListener(IRemoteConnectionListener listener) {
 		this.listener = listener;
 	}
 }
