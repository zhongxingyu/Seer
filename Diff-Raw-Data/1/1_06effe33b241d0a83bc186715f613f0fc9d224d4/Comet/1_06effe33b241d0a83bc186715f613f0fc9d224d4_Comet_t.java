 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.core.rpc;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.OpenEvent;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.Command;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.kk_electronic.kkportal.core.event.FrameReceivedEvent;
 import com.kk_electronic.kkportal.core.event.FrameSentEvent;
 import com.kk_electronic.kkportal.core.event.ServerConnectEvent;
 import com.kk_electronic.kkportal.core.event.ServerDisconnectEvent;
 
 /**
  * The comet class tries to emulate the a socket connection to the server by
  * doing long polling. You should only ever use one of such class due to
  * limitations in the number of Http requests a client can make to the server.
  * As per RFC2616: "A single-user client SHOULD NOT maintain more than 2
  * connections with any server or proxy.". The real limit is somewhat varying
  * but designing the protocol should only rely on two. Long pooling is made with
  * a http request to the server that gets delayed on the server side until a
  * response from the server is made. Then the server closes the connection
  * immediately due to the fact some proxies only send the repsonse back to the
  * client after the server closes the connection, so the only real way to flush
  * is by closing the connection.
  * 
  * Transmission from client to server is done using the second available
  * request.
  * 
  * To cope with with the limit of connections only one such frame can be sent at
  * time. This can usually be coped with by bundling like seen in the
  * {@link RpcDispatcher}. This is true for both directions.
  * 
  * The interface to the comet class is made to be as similiar to a normal
  * websocket so a smooth transition can be made sometime in the future.
  * 
  * @author Jes Andersen
  * 
  */
 
 @Singleton
 public class Comet implements WebSocket {
 
 	private RequestCallback connectCallback = new RequestCallback() {
 		@Override
 		public void onError(Request request, Throwable exception) {
 			status = WebSocketStatus.CLOSED;
 			GWT.log("SOCKET-Could not open connection to portalserver",
 					exception);
 			eventBus.fireEventFromSource(new ServerDisconnectEvent(), Comet.this);
 		}
 
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 			switch (response.getStatusCode()) {
 			case Response.SC_CREATED:
 				status = WebSocketStatus.OPEN;
 				rxUrl = response.getText();
 				GWT.log("SOCKET-Connection to portalserver established");
 				eventBus.fireEventFromSource(new ServerConnectEvent(), Comet.this);
 				poll();
 				break;
 			default:
 				onError(request, new Exception(
 						"Unknown status code returned from portalserver"));
 				break;
 			}
 		}
 	};
 
 	private RequestCallback txCallback = new RequestCallback() {
 		@Override
 		public void onError(Request request, Throwable exception) {
 			GWT
 					.log("SOCKET-Failed to send requests to portalserver",
 							exception);
 		}
 
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 			txRequest = null;
 			switch (response.getStatusCode()) {
 			case Response.SC_CREATED:
 			case Response.SC_ACCEPTED:
 				rxUrl = response.getText();
 				break;
 			default:
 				onError(request, new Exception(
 						"Unknown Status Code returned from portalserver"));
 			}
 			eventBus.fireEventFromSource(new FrameSentEvent(response.getText()), this);
 		}
 	};
 
 	private RequestCallback rxCallback = new RequestCallback() {
 
 		@Override
 		public void onError(Request request, Throwable exception) {
 			GWT.log("SOCKET-Failure during communication with portalserver",
 					exception);
 			close();
 		}
 
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 			switch (response.getStatusCode()) {
 			case Response.SC_OK:
 				GWT.log("SOCKET-portalserver receiving @"
 						+ new Date().getTime() + " : " + response.getText());
 				eventBus.fireEventFromSource(new FrameReceivedEvent(response.getText()), Comet.this);
 				deferredPoll();
 				break;
 			case 0:
 			case Response.SC_GONE:
 				GWT.log("SOCKET-Lost connection to portalserver");
 				close();
 				break;
 			default:
 				onError(
 						request,
 						new Exception(
 								"Unknown status code returned from portal server when receiving responses"));
 				break;
 			}
 		}
 	};
 
 	private final EventBus eventBus;
 	/**
 	 * We simulate the status of the connection to the server.
 	 */
 	WebSocketStatus status = WebSocketStatus.CLOSED;
 	/**
 	 * This is the relative url to get messages from the server on
 	 */
 	protected String rxUrl;
 
 	Request txRequest;
 
 	private String url;
 
 	private void poll() {
 		if (!status.equals(WebSocketStatus.OPEN))
 			return;
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url
 				+ rxUrl);
 		try {
 			builder.sendRequest(null, rxCallback);
 		} catch (RequestException e) {
 			GWT.log("SOCKET-Failed to get responses to portalserver", e);
 		}
 	}
 
 	/**
 	 * Many browsers has a load indicator that can be stopped by delaying
 	 * the long pull using the deferredCommand
 	 */
 	protected void deferredPoll() {
 		Scheduler.get().scheduleDeferred(new Command() {
 			
 			@Override
 			public void execute() {
 				poll();
 			}
 		});
 	}
 
 	/**
 	 * Creates the class with no sideeffects.
 	 * 
 	 * @param eventBus
 	 *            the eventbus to send {@link FrameReceivedEvent},
 	 *            {@link FrameSentEvent}, {@link OpenEvent} and
 	 *            {@link CloseEvent} on.
 	 */
 	@Inject
 	public Comet(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 
 	/**
 	 * listen to the connection lost messages
 	 */
 	@Override
 	public HandlerRegistration addServerDisconnectHandler(ServerDisconnectEvent.Handler handler) {
 		return eventBus.addHandlerToSource(ServerDisconnectEvent.TYPE, this, handler);
 	}
 
 	/**
 	 * listen to incoming frames
 	 */
 	@Override
 	public HandlerRegistration addFrameReceivedHandler(
 			FrameReceivedEvent.Handler handler) {
 		return eventBus.addHandlerToSource(FrameReceivedEvent.TYPE, this,
 				handler);
 	}
 
 	/**
 	 * listen to frames successfully sent. This is most useful for if you have a
 	 * queue for frame for transmission since only one frame can be transmitting
 	 * at a time.
 	 */
 	@Override
 	public HandlerRegistration addFrameSentHandler(FrameSentEvent.Handler handler) {
 		return eventBus.addHandlerToSource(FrameSentEvent.TYPE, this, handler);
 	}
 
 	/**
 	 * listen to when connections have actually been made.
 	 */
 	@Override
 	public HandlerRegistration addServerConnectHandler(ServerConnectEvent.Handler handler) {
 		return eventBus.addHandlerToSource(ServerConnectEvent.TYPE,this, handler);
 	}
 
 	/**
 	 * Closes the current connection
 	 */
 	@Override
 	public void close() {
 		// TODO: Abort tx and rx
 		status = WebSocketStatus.CLOSED;
 		rxUrl = null;
 		eventBus.fireEventFromSource(new ServerDisconnectEvent(), this);
 	}
 
 	/**
 	 * The connect call POST an empty request to that and expects to receive a
 	 * HTTP 201 Created with the url of where it can receive server frames.
 	 * 
 	 * @param url
 	 *            when opening a connection this url is used
 	 * @param subprotocol
 	 *            not used yet, mostly here for compatibility with websocket
 	 *            protocol
 	 */
 	@Override
 	public void connect(String url, String subprotocol) {
 		if (status.equals(WebSocketStatus.CLOSED)) {
 			this.url = url;
 			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
 					url);
 			try {
 				/*
 				 * for now we post not an real empty request but the json
 				 * variant. TODO: change the server so it can accept an empty
 				 * body
 				 */
 				builder.sendRequest("[]", connectCallback);
 			} catch (RequestException e) {
 				GWT.log("SOCKET-Failed to connect to portalserver", e);
				return;
 			}
 			this.status = WebSocketStatus.CONNECTING;
 		}
 	}
 
 	@Override
 	public boolean isConnected() {
 		return status.equals(WebSocketStatus.OPEN);
 	}
 
 	/**
 	 * is currently transmitting a message so send() should not be called until
 	 * this returns false. add a
 	 * {@link Comet#addFrameSentHandler(FrameSentHandler)} for a efficient way
 	 * of knowning when it is possible to send again.
 	 */
 	@Override
 	public boolean isTxBusy() {
 		return txRequest != null;
 	}
 
 	/**
 	 * send a frame to the server. should not be called if
 	 * {@link Comet#isTxBusy()} returns true, since it this creates too many
 	 * connections to the server.
 	 */
 	@Override
 	public void send(String s) {
 		if (!status.equals(WebSocketStatus.OPEN))
 			return;
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url
 				+ rxUrl);
 		try {
 			builder.sendRequest(s, txCallback);
 		} catch (RequestException e) {
 			GWT.log("SOCKET-Failed to send requests to portalserver", e);
 		}
 		GWT.log("SOCKET-portalserver sending @" + new Date().getTime() + " : "
 				+ s);
 		eventBus.fireEventFromSource(new FrameSentEvent(s), this);
 	}
 }
